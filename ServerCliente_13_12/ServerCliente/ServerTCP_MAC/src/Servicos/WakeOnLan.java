package Servicos;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class WakeOnLan {

    private static final int PORT = 9;

    public static void ligar(String macStr) {

        try {
            byte[] macBytes = getMacBytes(macStr);
            byte[] packetBytes = createMagicPacket(macBytes);

            InetAddress address = InetAddress.getByName(configs.configuracao.enderecoBradcast); // Endereço de broadcast
            
            DatagramPacket packet = new DatagramPacket(packetBytes, packetBytes.length, address, PORT);
            DatagramSocket socket = new DatagramSocket();
            
            socket.setBroadcast(true); // Permitir envio de broadcast
            socket.send(packet);
            socket.close();

            System.out.println("Magic Packet enviado para "+macStr);
        } catch (Exception e) {
            System.out.println("Erro ao enviar o Magic Packet: " + e.getMessage());
        }
    }

    private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Endereço MAC inválido.");
        }
        byte[] bytes = new byte[6];
        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) Integer.parseInt(hex[i], 16);
        }
        return bytes;
    }

    private static byte[] createMagicPacket(byte[] macBytes) {
        byte[] bytes = new byte[6 + 16 * macBytes.length];
        // Preenchendo com 6 bytes de 0xFF
        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) 0xFF;
        }
        // Repetindo o MAC address 16 vezes
        for (int i = 6; i < bytes.length; i += macBytes.length) {
            System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
        }
        return bytes;
    }
    
    public static void ligarMac(){
        for (int i = 0; i < Instancias.getControlebaseIntancia().getBaseCadastradaInfoMaquinas().size(); i++) {
            String mac = Instancias.getControlebaseIntancia().getBaseCadastradaInfoMaquinas().get(i).getMacAddres();
            ligar(mac);
        }
    }
}
