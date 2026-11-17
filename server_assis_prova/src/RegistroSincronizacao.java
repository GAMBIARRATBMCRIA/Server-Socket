

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Mantém o registro persistente de todos os arquivos já sincronizados com sucesso.
 *
 * <p>Estrutura interna: {@code MAC → (caminhoRelativo → InfoRegistro)}.
 * Persistida em disco no formato {@code MAC|caminho|tamanho|lastModified|sha256}, uma entrada por linha.
 *
 * <p>Thread-safe com append direto em disco para evitar reescritas completas do arquivo.
 */
public class RegistroSincronizacao {

    public static class InfoRegistro {
        public final long tamanho;
        public final long lastModified;
        public final String sha256;

        public InfoRegistro(long tamanho, long lastModified, String sha256) {
            this.tamanho = tamanho;
            this.lastModified = lastModified;
            this.sha256 = sha256;
        }
    }

    /** MAC → { caminhoRelativo → InfoRegistro } */
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, InfoRegistro>> registro
            = new ConcurrentHashMap<>();

    private final Path arquivoRegistro;

    /** Lock para serializar escritas em modo append */
    private final Object lockSalvar = new Object();

    public RegistroSincronizacao(String caminhoArquivo) {
        this.arquivoRegistro = Paths.get(caminhoArquivo);
        carregar();
    }

    /**
     * Registra (ou atualiza) um arquivo como sincronizado com sucesso.
     * Faz append imediato no arquivo de disco.
     *
     * @param mac             endereço MAC da estação
     * @param caminhoRelativo caminho relativo com '/' como separador
     * @param tamanho         tamanho do arquivo em bytes
     * @param lastModified    data de modificação em milissegundos
     * @param sha256          hash do arquivo recebido
     */
    public void registrar(String mac, String caminhoRelativo, long tamanho, long lastModified, String sha256) {
        InfoRegistro info = new InfoRegistro(tamanho, lastModified, sha256);
        registro.computeIfAbsent(mac, k -> new ConcurrentHashMap<>())
                .put(caminhoRelativo, info);

        synchronized (lockSalvar) {
            try (PrintWriter pw = new PrintWriter(new BufferedWriter(
                    new FileWriter(arquivoRegistro.toFile(), true)))) {
                pw.println(mac + "|" + caminhoRelativo + "|" + tamanho + "|" + lastModified + "|" + sha256);
            } catch (IOException e) {
                System.err.println("[REGISTRO] Erro ao gravar registro: " + e.getMessage());
            }
        }
    }

    /**
     * Verifica se um arquivo já está sincronizado comparando tamanho e data de modificação.
     *
     * @return {@code true} se o arquivo já existe no servidor com mesmo tamanho e data
     */
    public boolean jaSincronizado(String mac, String caminhoRelativo, long tamanho, long lastModified) {
        ConcurrentHashMap<String, InfoRegistro> arquivosDoMac = registro.get(mac);
        if (arquivosDoMac == null) return false;
        InfoRegistro info = arquivosDoMac.get(caminhoRelativo);
        if (info == null) return false;

        // Compara tamanho e timestamp (com tolerância de até 2 segundos para diferenças de FS)
        return info.tamanho == tamanho && Math.abs(info.lastModified - lastModified) <= 2000;
    }

    /**
     * Retorna o SHA-256 do último arquivo sincronizado para o par (MAC, caminho).
     *
     * @return hash registrado, ou {@code null} se não houver registro
     */
    public String obterSha256(String mac, String caminhoRelativo) {
        ConcurrentHashMap<String, InfoRegistro> arquivosDoMac = registro.get(mac);
        if (arquivosDoMac == null) return null;
        InfoRegistro info = arquivosDoMac.get(caminhoRelativo);
        return info != null ? info.sha256 : null;
    }

    /**
     * Retorna o conjunto de todos os caminhos atualmente registrados para um dado MAC.
     *
     * @param mac endereço MAC da estação
     * @return conjunto de caminhos relativos (cópia independente)
     */
    public Set<String> obterCaminhos(String mac) {
        ConcurrentHashMap<String, InfoRegistro> arquivosDoMac = registro.get(mac);
        if (arquivosDoMac == null) return Collections.emptySet();
        return new HashSet<>(arquivosDoMac.keySet());
    }

    /**
     * Remove o registro de um arquivo ou diretório sincronizado.
     * Faz append no arquivo em disco com status DELETED para manter persistência ordenada.
     *
     * @param mac             endereço MAC da estação
     * @param caminhoRelativo caminho relativo do arquivo ou diretório
     */
    public void remover(String mac, String caminhoRelativo) {
        ConcurrentHashMap<String, InfoRegistro> arquivosDoMac = registro.get(mac);
        if (arquivosDoMac != null) {
            arquivosDoMac.remove(caminhoRelativo);
            String prefixo = caminhoRelativo.endsWith("/") ? caminhoRelativo : caminhoRelativo + "/";
            arquivosDoMac.keySet().removeIf(k -> k.startsWith(prefixo));
        }

        synchronized (lockSalvar) {
            try (PrintWriter pw = new PrintWriter(new BufferedWriter(
                    new FileWriter(arquivoRegistro.toFile(), true)))) {
                pw.println(mac + "|" + caminhoRelativo + "|-1|-1|DELETED");
            } catch (IOException e) {
                System.err.println("[REGISTRO] Erro ao gravar deleção no registro: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------

    private void carregar() {
        if (!Files.exists(arquivoRegistro)) return;
        try (BufferedReader br = new BufferedReader(new FileReader(arquivoRegistro.toFile()))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty()) continue;
                String[] partes = linha.split("\\|");
                if (partes.length >= 5) {
                    try {
                        String mac = partes[0];
                        String caminho = partes[1];
                        long tamanho = Long.parseLong(partes[2]);
                        long lastModified = Long.parseLong(partes[3]);
                        String sha256 = partes[4];

                        if ("DELETED".equalsIgnoreCase(sha256)) {
                            ConcurrentHashMap<String, InfoRegistro> arquivosDoMac = registro.get(mac);
                            if (arquivosDoMac != null) {
                                arquivosDoMac.remove(caminho);
                                String prefixo = caminho.endsWith("/") ? caminho : caminho + "/";
                                arquivosDoMac.keySet().removeIf(k -> k.startsWith(prefixo));
                            }
                        } else {
                            registro.computeIfAbsent(mac, k -> new ConcurrentHashMap<>())
                                    .put(caminho, new InfoRegistro(tamanho, lastModified, sha256));
                        }
                    } catch (NumberFormatException ignored) {}
                } else if (partes.length == 3) {
                    // Compatibilidade com formato antigo MAC|caminho|sha256
                    String mac = partes[0];
                    String caminho = partes[1];
                    String sha256 = partes[2];
                    registro.computeIfAbsent(mac, k -> new ConcurrentHashMap<>())
                            .put(caminho, new InfoRegistro(-1, -1, sha256));
                }
            }
            System.out.println("[REGISTRO] " + contarEntradas() + " entradas carregadas.");
        } catch (IOException e) {
            System.err.println("[REGISTRO] Erro ao carregar registro: " + e.getMessage());
        }
    }

    private int contarEntradas() {
        return registro.values().stream().mapToInt(Map::size).sum();
    }
}
