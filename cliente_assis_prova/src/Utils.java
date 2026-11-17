

import java.io.*;
import java.net.Socket;
import java.net.NetworkInterface;
import java.net.InetAddress;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.security.MessageDigest;
import java.util.*;

/**
 * Funções utilitárias do cliente: SHA-256, MAC address e listagem recursiva de arquivos com metadados.
 */
public class Utils {

    public static class InfoArquivoLocal {
        public final Path caminhoAbsoluto;
        public final long tamanho;
        public final long lastModified;

        public InfoArquivoLocal(Path caminhoAbsoluto, long tamanho, long lastModified) {
            this.caminhoAbsoluto = caminhoAbsoluto;
            this.tamanho = tamanho;
            this.lastModified = lastModified;
        }
    }

    /**
     * Calcula o hash SHA-256 de um arquivo e retorna em formato hexadecimal minúsculo.
     *
     * @param arquivo caminho do arquivo
     * @return string hex do hash, ou {@code null} se o arquivo estiver inacessível/bloqueado
     */
    public static String calcularSha256(Path arquivo) {
        if (arquivo == null || isOculto(arquivo) || !isLegivel(arquivo)) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream is = Files.newInputStream(arquivo)) {
                byte[] buffer = new byte[Config.TAMANHO_BUFFER];
                int lido;
                while ((lido = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, lido);
                }
            }
            return bytesParaHex(digest.digest());
        } catch (Throwable e) {
            // Arquivo bloqueado por outro processo ou inacessível — ignorar silenciosamente
            return null;
        }
    }

    /**
     * Obtém o endereço MAC utilizando o mesmo método do ClienteSemMaven:
     * recupera o IP local vinculado ao socket conectado e extrai o MAC da interface real.
     *
     * @param socket socket ativo conectado ao servidor
     * @return string do MAC address formatado com hifens (AA-BB-CC-DD-EE-FF)
     */
    public static String getMacAddress(Socket socket) {
        String macAddress = "";
        try {
            if (socket != null && socket.isConnected()) {
                InetAddress ipAddress = socket.getLocalAddress();
                NetworkInterface networkInterface = NetworkInterface.getByInetAddress(ipAddress);
                if (networkInterface != null) {
                    byte[] mac = networkInterface.getHardwareAddress();
                    if (mac != null) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < mac.length; i++) {
                            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                        }
                        macAddress = sb.toString();
                        if (!macAddress.isEmpty()) {
                            return macAddress;
                        }
                    } else {
                        System.err.println("[UTILS] Endereço MAC não encontrado para esta interface.");
                    }
                } else {
                    System.err.println("[UTILS] Interface de rede não encontrada para o IP: " + ipAddress);
                }
            }
        } catch (Exception e) {
            System.err.println("[UTILS] Erro ao obter MAC via socket: " + e.getMessage());
        }

        // Fallback caso o socket não esteja conectado
        return getMacAddressFallback();
    }

    /**
     * Fallback para obter o MAC quando o socket ainda não foi conectado.
     */
    public static String getMacAddressFallback() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface nif = interfaces.nextElement();
                if (!nif.isUp() || nif.isLoopback() || nif.isVirtual()) continue;
                byte[] mac = nif.getHardwareAddress();
                if (mac != null && mac.length == 6) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    return sb.toString();
                }
            }
        } catch (Exception e) {
            System.err.println("[UTILS] Erro no fallback de MAC: " + e.getMessage());
        }
        return "00-00-00-00-00-00";
    }

    public static String getMacAddress() {
        return getMacAddressFallback();
    }

    /**
     * Verifica de forma segura se um caminho (arquivo ou diretório) é oculto ou de sistema.
     * Trata erros de permissão considerando o item como oculto/inacessível.
     *
     * @param path caminho a ser verificado
     * @return true se for oculto, de sistema ou inacessível
     */
    public static boolean isOculto(Path path) {
        if (path == null) {
            return false;
        }
        try {
            Path fileName = path.getFileName();
            if (fileName != null) {
                String nome = fileName.toString();
                // Ignora arquivos do sistema/editores por convenção
                if (nome.startsWith(".") || nome.startsWith("~$") || nome.endsWith(".tmp")
                        || nome.endsWith(".crdownload") || nome.endsWith(".part")) {
                    return true;
                }
            }

            File file = path.toFile();
            if (file.isHidden()) {
                return true;
            }

            try {
                DosFileAttributes dosAttrs = Files.readAttributes(path, DosFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                if (dosAttrs.isHidden() || dosAttrs.isSystem()) {
                    return true;
                }
            } catch (Throwable ignored) {
                // Arquivo temporariamente em uso exclusivo ou sistema de arquivos não-DOS:
                // NÃO considerar oculto!
            }
        } catch (Throwable t) {
            return false;
        }
        return false;
    }

    /**
     * Verifica de forma segura se o arquivo ou diretório possui permissão de leitura.
     *
     * @param path caminho a ser verificado
     * @return true se tiver permissão de leitura
     */
    public static boolean isLegivel(Path path) {
        if (path == null) {
            return false;
        }
        try {
            if (!Files.isReadable(path)) {
                return false;
            }
            if (!path.toFile().canRead()) {
                return false;
            }
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Lista recursivamente todos os arquivos válidos com seus metadados (tamanho e mtime),
     * ignorando arquivos/pastas ocultos, de sistema, sem permissão de acesso e temporários (.tmp).
     *
     * @param raiz diretório raiz
     * @return lista de itens com caminho absoluto, tamanho e timestamp
     */
    public static List<InfoArquivoLocal> listarArquivosComMetadados(Path raiz) {
        List<InfoArquivoLocal> resultado = new ArrayList<>();
        if (raiz == null || !Files.exists(raiz)) {
            return resultado;
        }

        try {
            Files.walkFileTree(raiz, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (!dir.equals(raiz)) {
                        if (isOculto(dir) || !isLegivel(dir)) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (isOculto(file) || !isLegivel(file)) {
                        return FileVisitResult.CONTINUE;
                    }
                    if (file.getFileName().toString().endsWith(".tmp")) {
                        return FileVisitResult.CONTINUE;
                    }
                    resultado.add(new InfoArquivoLocal(file, attrs.size(), attrs.lastModifiedTime().toMillis()));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Throwable e) {
            System.err.println("[UTILS] Erro ao listar arquivos: " + e.getMessage());
        }
        return resultado;
    }

    /**
     * Converte um caminho absoluto para relativo à pasta raiz, usando '/' como separador.
     *
     * @param raiz     diretório base
     * @param arquivo  arquivo cujo caminho relativo se quer
     * @return string com caminho relativo separado por '/'
     */
    public static String caminhoRelativo(Path raiz, Path arquivo) {
        if (raiz == null || arquivo == null) return "";
        try {
            Path raizNorm = raiz.toAbsolutePath().normalize();
            Path arqNorm = arquivo.toAbsolutePath().normalize();
            return raizNorm.relativize(arqNorm).toString().replace(File.separatorChar, '/');
        } catch (Exception e) {
            String r = raiz.toAbsolutePath().toString().replace(File.separatorChar, '/');
            String a = arquivo.toAbsolutePath().toString().replace(File.separatorChar, '/');
            if (a.toLowerCase().startsWith(r.toLowerCase())) {
                String sub = a.substring(r.length());
                if (sub.startsWith("/")) sub = sub.substring(1);
                return sub;
            }
            return arquivo.getFileName() != null ? arquivo.getFileName().toString() : "";
        }
    }

    // -------------------------------------------------------------------------

    private static String bytesParaHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private Utils() {}
}
