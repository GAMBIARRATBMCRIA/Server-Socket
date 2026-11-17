

/**
 * Constantes de configuração do servidor de sincronização.
 * Altere aqui para ajustar portas, caminhos e limites.
 */
public class Config {

    /** Porta TCP principal — clientes conectam aqui para sincronizar */
    public static final int PORTA_TCP = 12347;

    /** Porta UDP para descoberta automática do servidor na rede local */
    public static final int PORTA_UDP_DESCOBERTA = 8889;

    /** Pasta raiz onde os arquivos sincronizados são armazenados */
    public static final String PASTA_ARMAZENAMENTO = "D:\\teste";

    /** Arquivo de texto que persiste o registro de sincronizações realizadas */
    public static final String ARQUIVO_REGISTRO = "D:\\teste\\sinc_registro.txt";

    /**
     * Tamanho máximo permitido por arquivo, em bytes.
     * 1 GB = 1.073.741.824 bytes
     */
    public static final long TAMANHO_MAXIMO_ARQUIVO = 1_073_741_824L;

    /**
     * Número máximo de gravações em disco simultâneas.
     * Evita saturação de I/O com 30+ clientes enviando ao mesmo tempo.
     */
    public static final int MAX_UPLOADS_SIMULTANEOS = 10;

    /** Tamanho do buffer de leitura/escrita de bytes (64 KB) */
    public static final int BUFFER_SIZE = 65536;

    /** Versão do protocolo SINC_* — deve coincidir com o cliente */
    public static final String VERSAO_PROTOCOLO = "1";

    /** Token enviado pelo cliente para descoberta UDP */
    public static final String TOKEN_DISCOVERY = "SINC_DISCOVERY";

    /** Resposta do servidor ao token de descoberta */
    public static final String TOKEN_SERVER = "SINC_SERVER";

    private Config() {
        /* utilitário estático */ }
}