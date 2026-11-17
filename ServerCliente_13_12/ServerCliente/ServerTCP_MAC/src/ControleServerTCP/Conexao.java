/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ControleServerTCP;

import Servicos.ComunicacaoClienteServer;
import Servicos.Instancias;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Conexao {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private Thread tr;
    private ServerVideo serverVideo = new ServerVideo();

    public void receberClientes() {
        tr = new Thread() {
            @Override
            public void run() {
                Instancias.getControlebaseIntancia().clienteListaInstaciar();
                System.out.println("Servidor aguardando conexao...");

                while (!serverSocket.isClosed()) {
                    try {
                        clientSocket = new Socket();
                        clientSocket = serverSocket.accept();
                        new ComunicacaoClienteServer(clientSocket);
                    } catch (Exception ex) {
                        // Quando serverSocket.close() e chamado, accept() lanca excecao
                        // Isso e esperado durante o desligamento intencional
                        if (!serverSocket.isClosed()) {
                            System.out.println("Erro ao criar novo socket:" + ex.getMessage());
                        }
                    }
                }
                System.out.println("Loop de aceite de conexoes encerrado.");
            }
        };

        tr.start();

        Thread td = new Thread() {
            @Override
            public void run() {
                try {
                    // Cria um socket UDP na porta 8888
                    DatagramSocket serverSocket = new DatagramSocket(8888, InetAddress.getByName("0.0.0.0"));
                    serverSocket.setBroadcast(true);

                    System.out.println("Servidor esperando por requisicoes de descoberta...");

                    while (true) {
                        // Prepara um buffer para receber os dados
                        byte[] recvBuf = new byte[15000];
                        DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);

                        // Recebe o pacote
                        serverSocket.receive(receivePacket);

                        // Converte os dados recebidos para String
                        String message = new String(receivePacket.getData()).trim();

                        // Verifica se a mensagem recebida e uma solicitacao de descoberta
                        if (message.equals("DISCOVER_SERVER_REQUEST")) {
                            System.out.println("Solicitacao de descoberta recebida de: " + receivePacket.getAddress());

                            // Responde ao cliente com o IP do servidor
                            String responseMessage = "DISCOVER_SERVER_RESPONSE";
                            byte[] sendData = responseMessage.getBytes();

                            DatagramPacket sendPacket = new DatagramPacket(
                                    sendData,
                                    sendData.length,
                                    receivePacket.getAddress(),
                                    receivePacket.getPort()
                            );

                            // Envia a resposta de volta ao cliente
                            serverSocket.send(sendPacket);

                            System.out.println("Resposta enviada ao cliente: " + receivePacket.getAddress().getHostAddress());
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        td.start();
    }

    public Boolean IniciarServer() {
        Boolean iniciado = false;
        try {
            serverSocket = new ServerSocket(configs.configuracao.portaConexao); // Porta do servidor
            serverVideo.iniciarServidorVide(5001); // Inicia servidor de video secundario
            iniciado = true;
        } catch (Exception ex) {
            System.out.println("Erro Iniciar Server:" + ex.getMessage());
            iniciado = false;
        }

        return iniciado;
    }

    /**
     * Desliga o servidor de forma segura.
     * CORRECAO BUG 5: elimina as auto-chamadas recursivas que causavam StackOverflowError.
     * Fecha os recursos em sequencia com try/catch independentes, sem recursao.
     */
    public Boolean desligarServerCliente() {
        Boolean fechado = false;

        // Limpa o painel de clientes conectados na UI
        try {
            Iniciar.clientesConectados.tablePanneDialogCliente.removeAll();
        } catch (Exception ex) {
            System.out.println("Erro ao limpar painel: " + ex.getMessage());
        }

        // Para o servidor de video
        try {
            serverVideo.desligarServidorVideo();
        } catch (Exception ex) {
            System.out.println("Erro ao desligar servidor de video: " + ex.getMessage());
        }

        // Fecha o socket do ultimo cliente aceito
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (Exception ex) {
            System.out.println("Erro ao fechar clientSocket: " + ex.getMessage());
        }

        // Fecha o ServerSocket (faz serverSocket.accept() lancar excecao e sair do loop)
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception ex) {
            System.out.println("Erro ao fechar serverSocket: " + ex.getMessage());
        }

        // Interrompe a thread de aceite de conexoes
        try {
            if (tr != null && tr.isAlive()) {
                tr.interrupt();
            }
        } catch (Exception ex) {
            System.out.println("Erro ao interromper thread: " + ex.getMessage());
        }

        // Limpa a lista de clientes conectados (via ConcurrentHashMap)
        try {
            Instancias.getControlebaseIntancia().clienteListaInstaciar();
        } catch (Exception ex) {
            System.out.println("Erro ao limpar lista de clientes: " + ex.getMessage());
        }

        clientSocket = null;
        serverSocket = null;
        fechado = true;
        System.out.println("Servidor desligado com sucesso.");
        return fechado;
    }

}
