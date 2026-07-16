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

    public void receberClientes() {
        tr = new Thread() {
            @Override
            public void run() {
                Instancias.getControlebaseIntancia().clienteListaInstaciar();
                System.out.println("Servidor aguardando conexão...");

                while (!serverSocket.isClosed()) {
                    try {
                        clientSocket = new Socket();
                        clientSocket = serverSocket.accept();
                        new ComunicacaoClienteServer(clientSocket);
                    } catch (Exception ex) {
                        System.out.println("Erro ao criar novo socket:" + ex.getMessage());
                    }
                }
                desligarServerCliente();
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

                    System.out.println("Servidor esperando por requisições de descoberta...");

                    while (true) {
                        // Prepara um buffer para receber os dados
                        byte[] recvBuf = new byte[15000];
                        DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);

                        // Recebe o pacote
                        serverSocket.receive(receivePacket);

                        // Converte os dados recebidos para String
                        String message = new String(receivePacket.getData()).trim();

                        // Verifica se a mensagem recebida é uma solicitação de descoberta
                        if (message.equals("DISCOVER_SERVER_REQUEST")) {
                            System.out.println("Solicitação de descoberta recebida de: " + receivePacket.getAddress());

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
            iniciado = true;
        } catch (Exception ex) {
            System.out.println("Erro Iniciar Server:" + ex.getMessage());
            iniciado = false;
        }

        return iniciado;
    }

    public Boolean desligarServerCliente() {
        Boolean fechado = false;
        Iniciar.clientesConectados.tablePanneDialogCliente.removeAll();
        try {
            clientSocket.close();
            serverSocket.close();
            tr.interrupt();
            Instancias.getControlebaseIntancia().getClistesConectadosLista().clear();

        } catch (Exception ex) {
            System.out.println("Erro ao fechar sockets de conexão:" + ex.getMessage());
        }

        if (!tr.isInterrupted()) {
            desligarServerCliente();
        } else {
            System.out.println("Tr interrompida");
        }

        if (clientSocket.isClosed()) {
            fechado = true;
        } else {
            desligarServerCliente();
        }

        if (serverSocket.isClosed()) {
            clientSocket = null;
            serverSocket = null;
            fechado = true;
        } else {
            clientSocket = null;
            serverSocket = null;
            desligarServerCliente();
        }

        return fechado;
    }

}
