

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * Descobre automaticamente o endereço IP do servidor de sincronização
 * enviando broadcasts UDP para todas as interfaces de rede ativas e
 * para o endereço de broadcast da sub-rede do laboratório.
 */
public class BuscaServidor {

    /**
     * Realiza a descoberta do servidor via broadcast UDP ou configuração manual.
     *
     * @return endereço IP do servidor, ou {@code null} se não encontrado
     */
    public static InetAddress descobrir() {
        // 1. Verificar se há IP configurado manualmente em Config.java
        if (Config.IP_SERVIDOR_MANUAL != null && !Config.IP_SERVIDOR_MANUAL.trim().isEmpty()) {
            try {
                InetAddress ip = InetAddress.getByName(Config.IP_SERVIDOR_MANUAL.trim());
                System.out.println("[DISCOVERY] Usando IP fixo configurado: " + ip.getHostAddress());
                return ip;
            } catch (Exception e) {
                System.err.println("[DISCOVERY] IP manual inválido: " + Config.IP_SERVIDOR_MANUAL);
            }
        }

        // 2. Verificar se há arquivo local 'servidor_ip.txt' com o IP do servidor
        File arquivoIp = new File("servidor_ip.txt");
        if (arquivoIp.exists() && arquivoIp.canRead()) {
            try (BufferedReader br = new BufferedReader(new FileReader(arquivoIp))) {
                String linha = br.readLine();
                if (linha != null && !linha.trim().isEmpty()) {
                    InetAddress ip = InetAddress.getByName(linha.trim());
                    System.out.println("[DISCOVERY] IP obtido de 'servidor_ip.txt': " + ip.getHostAddress());
                    return ip;
                }
            } catch (Exception e) {
                System.err.println("[DISCOVERY] Erro ao ler 'servidor_ip.txt': " + e.getMessage());
            }
        }

        // 3. Montar lista de todos os endereços de broadcast possíveis
        Set<InetAddress> destinos = new LinkedHashSet<>();

        // Broadcast específico da sub-rede do laboratório (10.130.76.0/22 -> 10.130.79.255)
        try {
            destinos.add(InetAddress.getByName(Config.BROADCAST_PADRAO_LAB));
        } catch (Exception ignored) {}

        // Broadcast calculado para cada interface de rede ativa na máquina
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
                for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                    InetAddress bcast = ia.getBroadcast();
                    if (bcast != null) {
                        destinos.add(bcast);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[DISCOVERY] Aviso ao listar interfaces: " + e.getMessage());
        }

        // Limited broadcast universal
        try {
            destinos.add(InetAddress.getByName("255.255.255.255"));
        } catch (Exception ignored) {}

        System.out.println("[DISCOVERY] Procurando servidor na porta UDP "
                + Config.PORTA_UDP_DESCOBERTA + " nos destinos: " + destinos);

        byte[] dados = Config.TOKEN_DISCOVERY.getBytes();
        byte[] buffer = new byte[1024];

        // Tentar até 3 vezes (UDP pode sofrer perda de pacote na rede)
        for (int tentativa = 1; tentativa <= 3; tentativa++) {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);
                socket.setSoTimeout(2000); // 2 segundos por tentativa

                // Envia pacote de descoberta para cada endereço de broadcast
                for (InetAddress dest : destinos) {
                    try {
                        DatagramPacket envio = new DatagramPacket(dados, dados.length, dest, Config.PORTA_UDP_DESCOBERTA);
                        socket.send(envio);
                    } catch (Exception eEnvio) {
                        // Ignora falha de envio em uma interface específica (ex: IPv6 ou interface desconectada)
                    }
                }

                // Aguarda resposta
                DatagramPacket resposta = new DatagramPacket(buffer, buffer.length);
                socket.receive(resposta);

                String mensagem = new String(resposta.getData(), 0, resposta.getLength(), "UTF-8").trim();
                if (Config.TOKEN_SERVER.equals(mensagem)) {
                    InetAddress ip = resposta.getAddress();
                    System.out.println("[DISCOVERY] Servidor encontrado: " + ip.getHostAddress());
                    return ip;
                }
            } catch (SocketTimeoutException e) {
                System.out.println("[DISCOVERY] Tentativa " + tentativa + "/3 expirou sem resposta.");
            } catch (IOException e) {
                System.err.println("[DISCOVERY] Erro na tentativa " + tentativa + ": " + e.getMessage());
            }
        }

        System.out.println("[DISCOVERY] Nenhum servidor respondeu nas 3 tentativas.");
        return null;
    }

    private BuscaServidor() {}
}

