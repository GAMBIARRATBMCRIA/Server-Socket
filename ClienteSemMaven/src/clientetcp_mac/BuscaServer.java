package clientetcp_mac;

import Global.config;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class BuscaServer {

    private static final int MAX_TENTATIVAS = 10;
    private static final int PORTA_DISCOVERY = 8888;
    private static final int TIMEOUT_MS = 3000;

    /**
     * Busca o servidor via UDP broadcast.
     * Tenta até MAX_TENTATIVAS vezes antes de desistir.
     *
     * @return IP do servidor encontrado, ou null se não encontrado após todas as tentativas.
     */
    public String buscaServer() {
        String broadcastAddr = config.enderecoSevidor;

        // Se o endereço configurado for "ADM" (placeholder), usa o padrão de broadcast
        if (broadcastAddr == null || broadcastAddr.equalsIgnoreCase("ADM") || broadcastAddr.isEmpty()) {
            broadcastAddr = config.enderecoBroadcast;
        }

        for (int tentativa = 1; tentativa <= MAX_TENTATIVAS; tentativa++) {
            System.out.println("Buscando servidor (tentativa " + tentativa + "/" + MAX_TENTATIVAS + ")...");
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);
                socket.setSoTimeout(TIMEOUT_MS);

                byte[] sendData = "DISCOVER_SERVER_REQUEST".getBytes();
                DatagramPacket sendPacket = new DatagramPacket(
                        sendData,
                        sendData.length,
                        InetAddress.getByName(broadcastAddr),
                        PORTA_DISCOVERY
                );
                socket.send(sendPacket);

                byte[] recvBuf = new byte[15000];
                DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(receivePacket);

                String serverIP = receivePacket.getAddress().getHostAddress();
                System.out.println("Servidor encontrado: " + serverIP);
                return serverIP;

            } catch (Exception ex) {
                System.out.println("Tentativa " + tentativa + " falhou: " + ex.getMessage());
                if (tentativa < MAX_TENTATIVAS) {
                    try {
                        Thread.sleep(2000); // aguarda antes de tentar novamente
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        System.out.println("Servidor não encontrado após " + MAX_TENTATIVAS + " tentativas.");
        return null;
    }
}
