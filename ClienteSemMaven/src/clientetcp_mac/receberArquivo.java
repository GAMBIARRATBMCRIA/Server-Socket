/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientetcp_mac;

import Global.InstanciasGlobais;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;


/**
 *
 * @author LAB01
 */
public class receberArquivo extends Thread {

    private ArmazenarInstacias instancias;
    private String hash = "";

    public receberArquivo(ArmazenarInstacias instancias) {
        this.instancias = instancias;
        start();
    }

    @Override
    public void run() {
        //controle executando comando
        InstanciasGlobais.setRecebendoArquivo(true);
        receberArquivo();
        this.interrupt();
        //controle executando comando
        InstanciasGlobais.setRecebendoArquivo(false);

    }

    private void receberArquivo() {
        System.out.println("Aguardandando Hash....");
        String hashRecebido;
        File arquivo = new File(instancias.getCamnhoGravacao());
        BufferedOutputStream bufferedOutputStream = null;
        try {
            hashRecebido = instancias.getIn().readLine();
            if (hashRecebido.contains("hash:")) {
                hashRecebido = hashRecebido.replaceAll("hash:", "");
                System.out.println("hash recebido:" + hashRecebido);

                InputStream inputStream = instancias.getSocket().getInputStream();
                
                instancias.getOut().println("Guardando no caminho: " + instancias.getCamnhoGravacao());
                
                FileOutputStream fileOutputStream = new FileOutputStream(arquivo);
                bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

                byte[] buffer = new byte[1024];
                int bytesRead;
                long totalBytesRead = 0;
                System.out.println("aguardando arquivo");
                boolean fim = false;

                while (fim == false) {
                    bytesRead = inputStream.read(buffer);

                    if (bytesRead < 1024) {
                        fim = true;
                        instancias.getOut().println("Fim da recepção de aquivos. Aguadando validar...");
                    }
                    System.out.println(bytesRead);
                    bufferedOutputStream.write(buffer, 0, bytesRead);
                }

                bufferedOutputStream.flush();
                bufferedOutputStream.close();

                System.out.println("Arquivo recebido com sucesso. Validando arquivo. Total de bytes recebidos: " + totalBytesRead);
                if (hashRecebido.equalsIgnoreCase(calcularHashArquivo(instancias.getCamnhoGravacao()))) {
                    instancias.getOut().println("Arquivo recebido com sucesso");
                } else {
                    instancias.getOut().println("cancelar_envio_arquivo");
                    instancias.getOut().println("Arquivo corrompido");
                }
                instancias.setTipodado("tipodadotexto");
            }

        } catch (Exception e) {
            //arquivo = new File(instancias.getCamnhoGravacao());
            System.out.println("apagando arquivo");
            try {
                bufferedOutputStream.close();
            } catch (IOException ex) {
                System.out.println("erro ao fechar buffer após erro de gravação:" + ex.getMessage());
            }
//            while (!arquivo.delete()) {
//                System.out.println("tentando remover:"+instancias.getCamnhoGravacao());
//            }

            System.out.println("enviando cancelamento");
            instancias.getOut().println("cancelar_envio_arquivo");
            instancias.setTipodado("tipodadotexto");
            System.out.println("Erro ao receber o arquivo: " + e.getMessage());
            instancias.getOut().println("Erro ao receber o arquivo: " + e.getMessage());
        }
    }

    public String calcularHashArquivo(String caminhoArquivo) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(caminhoArquivo))) {
            byte[] buffer = new byte[8192]; // Tamanho do buffer
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead); // Atualiza o hash com os dados lidos
            }
        }
        // Converte os bytes do hash para uma string hexadecimal
        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
