package ControleServerTCP;

import Iniciar.PainelVideoIndividual;
import Servicos.ComunicacaoClienteServer;
import Servicos.ExecucaoAtividadesTela;
import Servicos.Instancias;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.util.ArrayList;

public class ServerVideo {

    private ServerSocket serverSocketVideo;
    private Thread threadVideo;

    public void iniciarServidorVide(int porta) {
        threadVideo = new Thread(() -> {
            try {
                serverSocketVideo = new ServerSocket(porta);
                System.out.println("Servidor de Vídeo aguardando conexão na porta " + porta + "...");

                while (!serverSocketVideo.isClosed()) {
                    Socket clientSocket = serverSocketVideo.accept();
                    System.out.println("Nova conexão de vídeo recebida de: " + clientSocket.getInetAddress().getHostAddress());

                    // Thread para escutar os pacotes de vídeo deste cliente
                    new Thread(() -> {
                        PainelVideoIndividual painelDoCliente = new PainelVideoIndividual();
                        ComunicacaoClienteServer clienteVideo = procurarClienteConectado(clientSocket.getInetAddress().getHostAddress());
                        clienteVideo.getonformacaoesCliente().setPainelVideo(painelDoCliente);

                        try (DataInputStream in = new DataInputStream(clientSocket.getInputStream())) {
                            clienteVideo.getonformacaoesCliente().setTransmissaoAtiva(Boolean.TRUE);
                            ExecucaoAtividadesTela.adicionarTabPannerVideo(clienteVideo.getonformacaoesCliente());

                            //adicionarTabPannerVideo
                            while (!clientSocket.isClosed()) {
                                int tipo = in.readInt();
                                int tamanho = in.readInt();
                                byte[] payload = new byte[tamanho];
                                in.readFully(payload);

                                // Futuramente: despachar payload (array de bytes da imagem) para a UI de
                                // Monitoramento
                                if (painelDoCliente != null) {
                                    painelDoCliente.atualizarFrame(payload);
                                }
                            }
                            ExecucaoAtividadesTela.removerTabPanneVideo(clienteVideo.getonformacaoesCliente().getMacAddres());
                        } catch (Exception e) {
                            System.out.println("Conexão de vídeo encerrada para o cliente.");
                            
                            clienteVideo.getonformacaoesCliente().setTransmissaoAtiva(Boolean.FALSE);
                            ExecucaoAtividadesTela.removerTabPanneVideo(clienteVideo.getonformacaoesCliente().getMacAddres());

                        }
                    }).start();
                }
            } catch (Exception ex) {
                System.out.println("Erro no Servidor de Vídeo: " + ex.getMessage());
            }
        });
        threadVideo.start();
    }

    private ComunicacaoClienteServer procurarClienteConectado(String IP) {
        ComunicacaoClienteServer clienteVideo = null;
        ArrayList<ComunicacaoClienteServer> listaClienteConectados = new ArrayList<>(Instancias.getControlebaseIntancia().getClistesConectadosLista());

        for (ComunicacaoClienteServer listaClienteConectado : listaClienteConectados) {
            if (listaClienteConectado.getonformacaoesCliente().getClientesocket().getInetAddress().getHostAddress().toString().contains(IP)) {
                clienteVideo = listaClienteConectado;
                break;
            }
        }

        return clienteVideo;

    }

    public void desligarServidorVideo() {
        try {
            serverSocketVideo.close();
            if (serverSocketVideo != null && !serverSocketVideo.isClosed()) {
                serverSocketVideo.close();
            }
            if (threadVideo != null) {
                threadVideo.interrupt();
            }
        } catch (Exception ex) {
            System.out.println("Erro ao desligar Servidor de Vídeo: " + ex.getMessage());
        }
    }
}
