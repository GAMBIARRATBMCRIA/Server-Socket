

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Núcleo do servidor de sincronização.
 *
 * <p>Mantém dois sockets em execução simultânea:
 * <ul>
 *   <li><b>TCP :12347</b> — aceita conexões dos clientes e despacha
 *       {@link ManipuladorCliente} em threads separadas.</li>
 *   <li><b>UDP :8889</b> — responde a broadcasts de descoberta {@code SINC_DISCOVERY}
 *       com {@code SINC_SERVER} para que os clientes encontrem o servidor automaticamente.</li>
 * </ul>
 */
public class ServidorSinc {

    private final RegistroSincronizacao registro;
    private final GerenciadorArquivos gerenciador;

    private ServerSocket serverSocketTCP;
    private DatagramSocket serverSocketUDP;

    private final ExecutorService poolClientes = Executors.newCachedThreadPool();

    private volatile boolean rodando = false;

    public ServidorSinc() throws IOException {
        this.registro   = new RegistroSincronizacao(Config.ARQUIVO_REGISTRO);
        this.gerenciador = new GerenciadorArquivos(Config.PASTA_ARMAZENAMENTO);
    }

    /** Inicia os dois servidores (TCP e UDP) em threads dedicadas. */
    public void iniciar() throws IOException {
        rodando = true;

        serverSocketTCP = new ServerSocket(Config.PORTA_TCP);
        serverSocketUDP = new DatagramSocket(Config.PORTA_UDP_DESCOBERTA, InetAddress.getByName("0.0.0.0"));
        serverSocketUDP.setBroadcast(true);

        exibirBanner();

        // Thread de descoberta UDP
        Thread threadUDP = new Thread(this::loopUDP, "UDP-Discovery");
        threadUDP.setDaemon(true);
        threadUDP.start();

        // Loop de aceitação TCP (na thread chamadora)
        loopTCP();
    }

    /** Para o servidor de forma limpa. */
    public void parar() {
        rodando = false;
        try { if (serverSocketTCP != null) serverSocketTCP.close(); } catch (IOException ignored) {}
        try { if (serverSocketUDP != null) serverSocketUDP.close(); } catch (Exception ignored) {}
        poolClientes.shutdownNow();
        System.out.println("[SERVER] Servidor encerrado.");
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void loopTCP() {
        System.out.println("[SERVER] Aguardando clientes na porta TCP " + Config.PORTA_TCP + "...");
        while (rodando) {
            try {
                java.net.Socket clienteSocket = serverSocketTCP.accept();
                System.out.println("[SERVER] Nova conexão: "
                        + clienteSocket.getInetAddress().getHostAddress());
                poolClientes.submit(
                        new ManipuladorCliente(clienteSocket, registro, gerenciador));
            } catch (IOException e) {
                if (rodando) {
                    System.err.println("[SERVER] Erro ao aceitar conexão: " + e.getMessage());
                }
            }
        }
    }

    private void loopUDP() {
        System.out.println("[SERVER] Descoberta UDP ativa na porta " + Config.PORTA_UDP_DESCOBERTA + " (0.0.0.0).");
        byte[] buffer = new byte[1024];
        while (rodando) {
            try {
                DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
                serverSocketUDP.receive(pacote);

                String mensagem = new String(pacote.getData(), 0, pacote.getLength(), "UTF-8").trim();
                if (Config.TOKEN_DISCOVERY.equals(mensagem)) {
                    System.out.println("[UDP] Pedido de descoberta recebido de "
                            + pacote.getAddress().getHostAddress() + ":" + pacote.getPort());
                    byte[] resposta = Config.TOKEN_SERVER.getBytes("UTF-8");
                    DatagramPacket pacoteResposta = new DatagramPacket(
                            resposta, resposta.length,
                            pacote.getAddress(), pacote.getPort());
                    serverSocketUDP.send(pacoteResposta);
                    System.out.println("[UDP] Resposta enviada para " + pacote.getAddress().getHostAddress());
                }
            } catch (Exception e) {
                if (rodando) {
                    System.err.println("[UDP] Erro: " + e.getMessage());
                }
            }
        }
    }

    private void exibirBanner() {
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            System.out.println("╔══════════════════════════════════════════════╗");
            System.out.println("║      Servidor Assistente de Prova — SINC     ║");
            System.out.println("╠══════════════════════════════════════════════╣");
            System.out.printf( "║  IP local  : %-31s║%n", ip);
            System.out.printf( "║  Porta TCP : %-31d║%n", Config.PORTA_TCP);
            System.out.printf( "║  Porta UDP : %-31d║%n", Config.PORTA_UDP_DESCOBERTA);
            System.out.printf( "║  Armazenam.: %-31s║%n", Config.PASTA_ARMAZENAMENTO);
            System.out.println("╚══════════════════════════════════════════════╝");
        } catch (Exception e) {
            System.out.println("[SERVER] Servidor iniciado.");
        }
    }
}

