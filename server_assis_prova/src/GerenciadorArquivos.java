

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.util.concurrent.Semaphore;

/**
 * Responsável por receber bytes de arquivo do cliente e gravá-los em disco de forma segura.
 *
 * <p>Garantias implementadas:
 * <ul>
 *   <li>Gravação atômica: dados são escritos em arquivo {@code .tmp} e só movidos para o
 *       destino final após validação bem-sucedida do SHA-256.</li>
 *   <li>Versão válida preservada: se a validação falhar, o {@code .tmp} é deletado e
 *       a versão anterior do arquivo permanece intacta.</li>
 *   <li>Isolamento por MAC: cada estação possui sua própria subpasta.</li>
 *   <li>Proteção contra path traversal: caminhos relativos são sanitizados antes do uso.</li>
 *   <li>Concorrência limitada: um {@link Semaphore} controla gravações simultâneas.</li>
 * </ul>
 */
public class GerenciadorArquivos {

    private final Path pastaBase;
    private final Semaphore semaforo;

    public GerenciadorArquivos(String pastaArmazenamento) throws IOException {
        this.pastaBase = Paths.get(pastaArmazenamento).toAbsolutePath();
        Files.createDirectories(pastaBase);
        this.semaforo = new Semaphore(Config.MAX_UPLOADS_SIMULTANEOS, true);
    }

    /**
     * Recebe e salva um arquivo enviado pelo cliente.
     *
     * @param mac             endereço MAC da estação (usado como subdiretório)
     * @param caminhoRelativo caminho relativo com '/' como separador
     * @param tamanho         tamanho exato em bytes declarado pelo cliente
     * @param lastModified    data de modificação em milissegundos
     * @param sha256Esperado  hash SHA-256 declarado pelo cliente (hex minúsculo)
     * @param entrada         stream TCP da qual os bytes serão lidos
     * @return {@code true} se o arquivo foi salvo e validado com sucesso
     */
    public boolean salvar(String mac, String caminhoRelativo, long tamanho, long lastModified,
                          String sha256Esperado, InputStream entrada)
            throws InterruptedException, IOException {

        semaforo.acquire();
        try {
            // 1. Sanitizar o caminho relativo
            String caminhoSanitizado = sanitizar(caminhoRelativo);
            if (caminhoSanitizado == null) {
                System.err.println("[ARQUIVO] Caminho rejeitado (path traversal): " + caminhoRelativo);
                return false;
            }

            // 2. Converter separador '/' para o SO atual
            String caminhoOS = caminhoSanitizado.replace('/', File.separatorChar);

            // 3. Resolver destino final e arquivo temporário
            Path destino = pastaBase.resolve(mac).resolve(caminhoOS).normalize();
            Path temp    = Paths.get(destino + ".tmp");

            // Verificação de segurança extra: destino deve continuar dentro de pastaBase
            if (!destino.startsWith(pastaBase)) {
                System.err.println("[ARQUIVO] Destino fora da pasta base: " + destino);
                return false;
            }

            // 4. Garantir que o diretório pai existe
            Files.createDirectories(destino.getParent());

            // 5. Ler bytes, gravar no .tmp e calcular SHA-256 ao mesmo tempo
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (OutputStream out = new BufferedOutputStream(
                    Files.newOutputStream(temp, StandardOpenOption.CREATE,
                                               StandardOpenOption.TRUNCATE_EXISTING))) {
                byte[] buffer = new byte[Config.BUFFER_SIZE];
                long totalLido = 0;
                int lido;
                while (totalLido < tamanho) {
                    int restante = (int) Math.min(buffer.length, tamanho - totalLido);
                    lido = entrada.read(buffer, 0, restante);
                    if (lido == -1) break;
                    out.write(buffer, 0, lido);
                    digest.update(buffer, 0, lido);
                    totalLido += lido;
                }
                if (totalLido != tamanho) {
                    Files.deleteIfExists(temp);
                    throw new IOException("Bytes incompletos recebidos (" + totalLido + "/" + tamanho + ") para " + caminhoRelativo);
                }
            }

            // 6. Validar SHA-256
            String sha256Calculado = bytesParaHex(digest.digest());
            if (!sha256Calculado.equalsIgnoreCase(sha256Esperado)) {
                Files.deleteIfExists(temp);
                System.err.println("[ARQUIVO] Falha de integridade em " + caminhoRelativo
                        + " — esperado: " + sha256Esperado + " calculado: " + sha256Calculado);
                return false;
            }

            // 7. Mover para o destino final com fallback caso o SO não suporte rename atômico por cima de arquivo existente
            try {
                Files.move(temp, destino,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException eAtomic) {
                Files.move(temp, destino, StandardCopyOption.REPLACE_EXISTING);
            }

            // 8. Restaurar timestamp da última modificação se fornecido
            if (lastModified > 0) {
                try {
                    Files.setLastModifiedTime(destino, FileTime.fromMillis(lastModified));
                } catch (Exception ignored) {}
            }

            return true;

        } catch (IOException e) {
            System.err.println("[ARQUIVO] Falha crítica de I/O em " + caminhoRelativo + ": " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("[ARQUIVO] Erro ao salvar " + caminhoRelativo + ": " + e.getMessage());
            return false;
        } finally {
            semaforo.release();
        }
    }

    /**
     * Remove um arquivo ou diretório do servidor correspondente à estação informada.
     *
     * @param mac             endereço MAC da estação
     * @param caminhoRelativo caminho relativo sanitizado
     * @return {@code true} se a deleção foi realizada com sucesso
     */
    public boolean deletar(String mac, String caminhoRelativo) {
        String caminhoSanitizado = sanitizar(caminhoRelativo);
        if (caminhoSanitizado == null) {
            System.err.println("[ARQUIVO] Caminho rejeitado para deleção: " + caminhoRelativo);
            return false;
        }

        String caminhoOS = caminhoSanitizado.replace('/', File.separatorChar);
        Path destino = pastaBase.resolve(mac).resolve(caminhoOS).normalize();

        if (!destino.startsWith(pastaBase)) {
            System.err.println("[ARQUIVO] Destino de deleção fora da pasta base: " + destino);
            return false;
        }

        if (!Files.exists(destino)) {
            // Já não existe no disco
            return true;
        }

        try {
            if (Files.isDirectory(destino)) {
                Files.walkFileTree(destino, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.deleteIfExists(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.deleteIfExists(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                Files.deleteIfExists(destino);
            }
            return true;
        } catch (IOException e) {
            System.err.println("[ARQUIVO] Erro ao deletar " + caminhoRelativo + ": " + e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------------

    /**
     * Sanitiza e valida um caminho relativo.
     *
     * @return caminho normalizado com separador '/', ou {@code null} se inválido
     */
    private String sanitizar(String caminho) {
        if (caminho == null || caminho.isEmpty()) return null;
        try {
            String caminhoOS = caminho.replace('/', File.separatorChar);
            Path normalizado = Paths.get(caminhoOS).normalize();

            // Rejeitar caminhos absolutos
            if (normalizado.isAbsolute()) return null;

            // Rejeitar qualquer componente ".."
            for (Path componente : normalizado) {
                if ("..".equals(componente.toString())) return null;
            }

            return normalizado.toString().replace(File.separatorChar, '/');
        } catch (Exception e) {
            return null;
        }
    }

    private static String bytesParaHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
