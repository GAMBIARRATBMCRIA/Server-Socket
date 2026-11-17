
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Constantes de configuração do cliente de sincronização.
 */
public class Config {

    /**
     * Porta TCP do servidor assistente
     */
    public static final int PORTA_TCP = 12347;

    /**
     * Porta UDP para descoberta do servidor
     */
    public static final int PORTA_UDP_DESCOBERTA = 8889;

    /**
     * Versão do protocolo — deve coincidir com o servidor
     */
    public static final String VERSAO_PROTOCOLO = "1";

    /**
     * Tempo de debounce em milissegundos. Um arquivo só é enviado após ficar
     * estável por este período sem novos eventos.
     */
    public static final long DEBOUNCE_MS = 1000;

    /**
     * Timeout de descoberta UDP em milissegundos
     */
    public static final int TIMEOUT_DESCOBERTA_MS = 5000;

    /**
     * Tamanho do buffer de leitura/escrita de arquivos (64 KB)
     */
    public static final int TAMANHO_BUFFER = 65536;

    /**
     * Tamanho máximo de arquivo permitido (1 GB). Arquivos maiores são
     * ignorados com aviso em log.
     */
    public static final long TAMANHO_MAXIMO_ARQUIVO = 1_073_741_824L;

    /**
     * Token enviado no broadcast de descoberta
     */
    public static final String TOKEN_DISCOVERY = "SINC_DISCOVERY";

    /**
     * Resposta esperada do servidor
     */
    public static final String TOKEN_SERVER = "SINC_SERVER";

    /**
     * IP manual do servidor (opcional).
     * Se definido (ex: "10.130.76.30"), o cliente conecta diretamente sem busca UDP.
     * Deixe vazio "" para usar a descoberta automática por broadcast UDP.
     */
    public static String IP_SERVIDOR_MANUAL = "";

    /**
     * Endereço de broadcast padrão da sub-rede do laboratório
     */
    public static final String BROADCAST_PADRAO_LAB = "10.130.79.255";

    /**
     * Retorna o caminho da pasta Documentos do usuário atual. Tenta "Documents"
     * (inglês/padrão Windows) e, se não existir, "Documentos" (português).
     */
    public static Path getPastaDocumentos(String pasta) {
        String home = System.getProperty("user.home");

        Path pastaOrigem = Paths.get(home, "Documents\\" + pasta);

        if (Files.exists(pastaOrigem)) {
            System.out.println(pastaOrigem.toAbsolutePath().toString());
            return pastaOrigem;
        }

        if (!pasta.isEmpty()) {
            pastaOrigem.toFile().mkdirs();
            System.out.println("criado"+pastaOrigem.toAbsolutePath().toString());

            return pastaOrigem;
        }

// Tenta "Documents" (Windows em inglês / padrão internacional)
        Path docs = Paths.get(home, "Documents");
        if (Files.exists(docs)) {
            return docs;
        }

        // Tenta "Documentos" (Windows em português)
        Path docspt = Paths.get(home, "Documentos");
        if (Files.exists(docspt)) {
            return docspt;
        }

        // Fallback: cria Documents se nenhum existir
        docs.toFile().mkdirs();
        return docs;
    }

    private Config() {
    }
}
