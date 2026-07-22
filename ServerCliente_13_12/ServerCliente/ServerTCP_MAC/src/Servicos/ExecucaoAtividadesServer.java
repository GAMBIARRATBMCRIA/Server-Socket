/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servicos;

import BaseDados.infomacaoMaquinas;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

/**
 *
 * @author LAB01
 */
public class ExecucaoAtividadesServer {

    public synchronized List lerTxt(String caminho) {
        List<String> linhas = new ArrayList<>();
        BufferedReader buffRead;

        try {
            buffRead = new BufferedReader(new FileReader(caminho));
            String linha = " ";
            while (linha != null) {
                linha = buffRead.readLine();
                if (linha != null) {
                    linhas.add(linha);
                }
            }
            buffRead.close();

        } catch (Exception ex) {
            System.out.println("Erro:" + ex.getMessage());
        }

        return linhas;
    }

    public synchronized Boolean escreverTxtBase(String Mac, String bancada, String posicao, String tombo, String setor) {
        List<String> linhas = lerTxt(configs.configuracao.localBase);
        linhas.add(Mac + "|" + bancada + "|" + posicao + "|" + setor + "|" + tombo);
        try {
            Path caminho = Paths.get(configs.configuracao.localBase);
            Files.write(caminho, linhas, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Arquivo escrito com sucesso!");
            linhas.clear();
            return true;
        } catch (Exception e) {
            System.err.println("Erro ao escrever no arquivo: " + e.getMessage());
            return false;
        }

    }

    public Boolean IniciarServidor() {
        Boolean sucesso = false;
        sucesso = Instancias.getConexaoIntancia().IniciarServer();
        if (sucesso) {
            Instancias.getConexaoIntancia().receberClientes();
        }
        return sucesso;
    }

    public Boolean DeligarServer() {
        Boolean desligado = false;
        System.out.println(" f 1");
        if (desconectarTodosClientes()) {
            if (Instancias.getConexaoIntancia().desligarServerCliente()) {
                desligado = true;
            }
        }

        return desligado;
    }

    Boolean desconectarTodosClientes() {
        Boolean desconectados = false;
        Boolean closed = false;
        int tamanho = Instancias.getControlebaseIntancia().getClistesConectadosLista().size();

        for (int i = 0; i < tamanho; i++) {
            closed = Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).desconectarCliente(2);
        }

        if (closed == true) {
            Instancias.getControlebaseIntancia().getClistesConectadosLista().clear();
        }

        if (Instancias.getControlebaseIntancia().getClistesConectadosLista().size() > 0) {
            ExecucaoAtividadesTela.publicarAvido("Ainda existe coneções ativas");
            desconectados = false;
        } else {
            ExecucaoAtividadesTela.publicarAvido("Todos os clientes foram desconectados!");
            desconectados = true;
        }

        return desconectados;
    }

    public synchronized infomacaoMaquinas procuraClientes(String macAddress) {
        infomacaoMaquinas info = null;

        for (int i = 0; i < Servicos.Instancias.getControlebaseIntancia().getBaseCadastradaInfoMaquinas().size(); i++) {
            info = Servicos.Instancias.getControlebaseIntancia().getBaseCadastradaInfoMaquinas().get(i);

            if (info.getMacAddres().equalsIgnoreCase(macAddress)) {
                return info;
            }
        }
        return null;
    }

    public void enviarComando(JTextArea areaTexto, String receptor, String maquina, Boolean clienteAceitarComando) {
        //recpetor pode ser o mac, grupo ou bancada

        String mensagem = areaTexto.getText();

        if (clienteAceitarComando) {
            mensagem = "aceitar:" + mensagem;//alterar ainda pra o cliente aceitar ou n a mensagem   
        }

        if (receptor.equalsIgnoreCase("grupo")) {
            for (int i = 0; i < Instancias.getControlebaseIntancia().getClistesConectadosLista().size(); i++) {

                if (maquina.equalsIgnoreCase(Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().getSetor())) {
                    if (Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().getReceberComando()) {
                        Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).enviarMensagemPesada(mensagem);
                        ExecucaoAtividadesTela.publicarAvido("Comando enviado para:" + receptor);
                    }
                }

            }
        } else if (receptor.equalsIgnoreCase("bc")) {

            for (int i = 0; i < Instancias.getControlebaseIntancia().getClistesConectadosLista().size(); i++) {

                if (maquina.equalsIgnoreCase(Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().getBancada())) {

                    if (Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().getReceberComando()) {
                        Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).enviarMensagemPesada(mensagem);
                        ExecucaoAtividadesTela.publicarAvido("Comando enviado para:" + receptor);
                    }
                }

            }
        } else {

            for (int i = 0; i < Instancias.getControlebaseIntancia().getClistesConectadosLista().size(); i++) {
                if (maquina.equalsIgnoreCase(Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().getMacAddres())) {

                    if (Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().getReceberComando()) {
                        Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).enviarMensagemPesada(mensagem);
                        ExecucaoAtividadesTela.publicarAvido("Comando enviado para:" + receptor);
                    }

                }

            }
        }

    }

    public synchronized void habilitarClienteReceberComando(Boolean habilitar, String mac) {
        for (int i = 0; i < Instancias.getControlebaseIntancia().getClistesConectadosLista().size(); i++) {
            if (Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().getMacAddres().equalsIgnoreCase(mac)) {
                Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().setReceberComando(habilitar);
                System.out.println("Mac:" + mac + " habilitado:" + habilitar);
            }
        }
    }

    public synchronized Boolean enviarArquivo(String caminho, String receptor, String maquina, String nomeArquivo) {
        Boolean concluidoSucesso = false;
        System.out.println("envia arquivo:" + nomeArquivo);

        try {
            String hash = calcularHashArquivo(caminho);

            if (receptor.equalsIgnoreCase("grupo")) {
                for (int i = 0; i < Instancias.getControlebaseIntancia().getClistesConectadosLista().size(); i++) {

                    if (maquina.equalsIgnoreCase(Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().getSetor())) {

                        if (Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().getReceberComando()) {
                            ExecucaoAtividadesTela.publicarAvido("Enviando arquivo para:" + receptor);

                            Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).enviarMensagemSimples("tipodadoarquivo:" + nomeArquivo);
                            Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).enviarMensagemSimples("hash:" + hash);
                            Thread.sleep(100);

                            Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).enviarArquivo(caminho);
                            Thread.sleep(100);

                            //Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).enviarMensagemSimples("tipodadotexto");
                            ExecucaoAtividadesTela.publicarAvido("Arquivo enviado para:" + receptor);
                        }
                    }

                }
            } else if (receptor.equalsIgnoreCase("bc")) {

                for (int i = 0; i < Instancias.getControlebaseIntancia().getClistesConectadosLista().size(); i++) {

                    if (maquina.equalsIgnoreCase(Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().getBancada())) {

                        if (Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().getReceberComando()) {
                            ExecucaoAtividadesTela.publicarAvido("Enviando arquivo para:" + receptor);

                            Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).enviarMensagemSimples("tipodadoarquivo:" + nomeArquivo);
                            Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).enviarMensagemSimples("hash:" + hash);

                            Thread.sleep(100);

                            Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).enviarArquivo(caminho);
                            Thread.sleep(100);

                            //Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).enviarMensagemSimples("tipodadotexto");
                            ExecucaoAtividadesTela.publicarAvido("Arquivo enviado para:" + receptor);
                        }
                    }

                }
            } else {

                for (int i = 0; i < Instancias.getControlebaseIntancia().getClistesConectadosLista().size(); i++) {
                    if (maquina.equalsIgnoreCase(Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().getMacAddres())) {

                        if (Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().getReceberComando()) {
                            ExecucaoAtividadesTela.publicarAvido("Enviando arquivo para:" + receptor);

                            Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).enviarMensagemSimples("tipodadoarquivo:" + nomeArquivo);
                            Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).enviarMensagemSimples("hash:" + hash);

                            Thread.sleep(100);

                            Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).enviarArquivo(caminho);
                            Thread.sleep(100);

                            // Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).enviarMensagemSimples("tipodadotexto");
                            ExecucaoAtividadesTela.publicarAvido("Arquivo enviado para:" + receptor);
                        }

                    }

                }
            }
        } catch (Exception ex) {
            String erro = "Erro ao enviar o arquivo:" + ex.getMessage();
            System.out.println(erro);
            JOptionPane.showMessageDialog(null, erro);
            concluidoSucesso = false;
        }
        return concluidoSucesso;
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

    public synchronized void enviarMensagemTexto(String texto, String receptor, String maquina) {
        try {
            if (receptor.equalsIgnoreCase("grupo")) {
                for (int i = 0; i < Instancias.getControlebaseIntancia().getClistesConectadosLista().size(); i++) {

                    if (maquina.equalsIgnoreCase(Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().getSetor())) {

                        if (Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().getReceberComando()) {
                            ExecucaoAtividadesTela.publicarAvido("Enviando mensagem para:" + receptor);

                            Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).enviarMensagemSimples("msgcliente:" + texto);
                            Thread.sleep(100);

                            ExecucaoAtividadesTela.publicarAvido("mensagem enviada para:" + receptor);
                        }
                    }

                }
            } else if (receptor.equalsIgnoreCase("bc")) {

                for (int i = 0; i < Instancias.getControlebaseIntancia().getClistesConectadosLista().size(); i++) {

                    if (maquina.equalsIgnoreCase(Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().getBancada())) {

                        if (Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().getReceberComando()) {
                            ExecucaoAtividadesTela.publicarAvido("Enviando arquivo para:" + receptor);

                            Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).enviarMensagemSimples("msgcliente:" + texto);
                            Thread.sleep(100);

                            //Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).enviarMensagemSimples("tipodadotexto");
                            ExecucaoAtividadesTela.publicarAvido("Arquivo enviado para:" + receptor);
                        }
                    }

                }
            } else {

                for (int i = 0; i < Instancias.getControlebaseIntancia().getClistesConectadosLista().size(); i++) {
                    if (maquina.equalsIgnoreCase(Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().getMacAddres())) {

                        if (Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().getReceberComando()) {
                            ExecucaoAtividadesTela.publicarAvido("Enviando arquivo para:" + receptor);

                            Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).enviarMensagemSimples("msgcliente:" + texto);
                            Thread.sleep(100);
                            // Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).enviarMensagemSimples("tipodadotexto");

                            ExecucaoAtividadesTela.publicarAvido("Arquivo enviado para:" + receptor);
                        }

                    }

                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(ExecucaoAtividadesServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
