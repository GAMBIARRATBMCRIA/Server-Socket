package clientetcp_mac;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class BuscaServer {

    public String buscaServer() {
        String ip = "";
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            socket.setSoTimeout(3000);
            System.out.println("buscando");
            byte[] sendData = "DISCOVER_SERVER_REQUEST".getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("10.130.79.255"), 8888);
            socket.send(sendPacket);

            byte[] recvBuf = new byte[15000];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            socket.receive(receivePacket);

            String serverIP = receivePacket.getAddress().getHostAddress();
            System.out.println("Servidor encontrado: " + serverIP);
            ip = serverIP;
            // Agora você pode se conectar ao servidor usando o IP recebido
            //Socket socketConnection = new Socket(serverIP, 12346);
            // Continue com a lógica de comunicação...
        } catch (Exception ex) {
            //ex.printStackTrace();
            
            System.out.println("erro:"+ex.getMessage());
            buscaServer();
        }

        return ip;
    }
}
