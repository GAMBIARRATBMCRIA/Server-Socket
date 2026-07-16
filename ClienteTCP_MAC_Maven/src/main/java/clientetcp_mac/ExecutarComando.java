/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientetcp_mac;

import Global.InstanciasGlobais;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

/**
 *
 * @author LAB01
 */
public class ExecutarComando extends Thread {

    private String mensagemRecebida;
    private ArmazenarInstacias instancias;
    private ArrayList<String> textoRecebido;

    public ExecutarComando() {
    }

    public ExecutarComando(String mensagem, ArmazenarInstacias instacias, ArrayList<String> listaLinhas) {
        mensagemRecebida = mensagem;
        this.instancias = instacias;
        textoRecebido = new ArrayList<>(listaLinhas);
        //  textoRecebido = listaLinhas;
        start();
    }

    @Override
    public void run() {
        //controle executando comando
        InstanciasGlobais.setExecutandoComando(true);
        //

        int confirmar = JOptionPane.OK_OPTION;
        System.out.println("mensagem recebida:" + mensagemRecebida);

        if (mensagemRecebida.startsWith("aceitar:")) {
            confirmar = JOptionPane.showConfirmDialog(null, "Um comando remoto precisa de permissão pra executar.\nPermitir execução do comando?",
                    "Administrador Remoto", JOptionPane.OK_CANCEL_OPTION);
            mensagemRecebida = mensagemRecebida.replaceAll("aceitar:", "");
        }

        if (confirmar == JOptionPane.OK_OPTION) {

            if (mensagemRecebida.contains("cmd:")) {
                executarCmd(mensagemRecebida.replaceAll("cmd:", ""));
            } else if (mensagemRecebida.contains("validacao")) {
                enviarMensagem(instancias.getMacLocal());
            } else if (mensagemRecebida.startsWith("cmds:")) {
                executarScriptCmd(textoRecebido);
            } else if (mensagemRecebida.equalsIgnoreCase("pr:bloqueioTela")) {
                bloqueioTela(true);
            } else if (mensagemRecebida.equalsIgnoreCase("pr:desbloqueioTela")) {
                bloqueioTela(false);
            } else if (mensagemRecebida.equalsIgnoreCase("pr:BloquearInternet")) {
                bloquearInternet();
            } else if (mensagemRecebida.equalsIgnoreCase("pr:Desbloquearinternet")) {
                DesbloquearInternet();
            } else if (mensagemRecebida.equalsIgnoreCase("liserv:")) {
                listarServicos();
            } else if (mensagemRecebida.contains("altcaminho:")) {
                if (verifica_diretório(mensagemRecebida.replaceAll("altcaminho:", ""))) {
                    instancias.setCamnhoGravacao(mensagemRecebida.replaceAll("altcaminho:", ""));
                    enviarMensagem("caminho de armazenenamento alterado para:" + instancias.getCamnhoGravacao());
                }else{
                    enviarMensagem("caminho não é válido!");
                }                
            } else if (mensagemRecebida.contains("atualizar:")) {
                enviarMensagem("Atualização será instalada");
                atualizar();
            } else if (mensagemRecebida.contains("desconectar:")) {
                enviarMensagem("O cliente será encerrado!");
                desligar();
            } else if (mensagemRecebida.contains("msgcliente:")) {
                exibirMensagem(textoRecebido, mensagemRecebida.replaceAll("msgcliente:", ""));
            } else if (mensagemRecebida.contains("autenticado")) {
                System.out.println("Autenticação efetuada");
            } else {
                System.out.println("Paramentro nao reconhecido:" + mensagemRecebida);
                enviarMensagem("Parâmetro não reconhecido");
            }

        } else {
            enviarMensagem("Cliente recusou a execução do comando!");
        }
        this.interrupt();

        //controle executando comando
        InstanciasGlobais.setExecutandoComando(false);

        System.out.println("interrompida:" + Thread.currentThread().isInterrupted());
    }

    boolean verifica_diretório(String caminho) {
        File dir = new File(caminho);
        enviarMensagem(dir.getAbsolutePath());
        return dir.isDirectory();
    }

    private Boolean enviarMensagem(String mensagem) {
        instancias.getOut().println(mensagem);
        instancias.getOut().flush();
        return true;
    }

    public void executarCmd(String mensagem) {
        String comando = mensagem; // Comando a ser executado
        //String retorno = "";

        try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", comando);
            builder.redirectErrorStream(true);
            Process processo = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(processo.getInputStream()));
            String linha;
            while ((linha = reader.readLine()) != null) {
                System.out.println(linha);
                instancias.getOut().println(linha);
            }
            instancias.getOut().println("Finalizado com êxito!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void executarScriptCmd(ArrayList<String> textoRecebidoarray) {
        System.out.println("Executar Script:" + textoRecebidoarray.get(0));

        for (int i = 0; i < textoRecebidoarray.size(); i++) {

            System.out.println("\nExecutando comando:" + i);
            String comando = textoRecebidoarray.get(i);

            if (comando.startsWith("cmds:")) {
                comando = comando.replaceAll("cmds:", "");
            }

            if (!comando.trim().isEmpty()) {
                try {
                    ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", comando);
                    builder.redirectErrorStream(true);
                    Process processo = builder.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(processo.getInputStream()));
                    String linha;
                    while ((linha = reader.readLine()) != null) {
                        System.out.println(linha);
                        instancias.getOut().println(linha);
                    }

                    int exitCode = processo.waitFor();
                    System.out.println("\nCódigo de saída: " + exitCode);
                    instancias.getOut().println("Script finalizado com êxito!");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        textoRecebidoarray.clear();
    }

    private void bloqueioTela(Boolean bloquear) {
        if (bloquear) {
            if (!InstanciasGlobais.getBloqueiotelaInstacia().isVisible()) {
                InstanciasGlobais.getBloqueiotelaInstacia().setVisible(true);
            }
        } else {
            InstanciasGlobais.getBloqueiotelaInstacia().dispose();
        }
    }

    private void bloquearInternet() {
        String comand
                = "reg add \"HKEY_CURRENT_USER\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\Connections\" "
                + "/v DefaultConnectionSettings /t REG_BINARY /d "
                + "460000000a0000000b0000000a000000302e302e302e303a38300000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000 /f";
        executarCmd(comand);
        executarCmd("ipconfig /flushdns");
    }

    private void DesbloquearInternet() {
        String comand
                = "reg add \"HKEY_CURRENT_USER\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\Connections\" "
                + "/v DefaultConnectionSettings /t REG_BINARY /d "
                + "46000009000000090000000a000000302e302e302e303a38300000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000 /f";
        executarCmd(comand);
    }

    private void listarServicos() {
        try {

            ProcessBuilder processBuilder = new ProcessBuilder("powershell.exe", "Get-Service | Select-Object Status, ServiceName, DisplayName");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            //Process process = Runtime.getRuntime().exec("powershell -Command \"Get-Service | Select-Object Status, ServiceName, DisplayName\"");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            //PrintStream utf8Out = new PrintStream(System.out, true, "UTF-8");

            ArrayList<String> servicos = new ArrayList<>();

            String line;

            while ((line = reader.readLine()) != null) {

                //System.out.println(line);
//                // Formatar a linha para exibir o nome real primeiro
                String serviceName = "";
                String displayName = "";
                String status = "";

                if (line.startsWith("Stopped ")) {
                    status = "Stopped";
                    line = line.replaceAll("Stopped ", "");

                } else if (line.startsWith("Running ")) {
                    status = "Running ";
                    line = line.replaceAll("Running ", "");
                }

                int index = line.indexOf(" "); // Encontrar o índice do primeiro espaço

                if (index != -1) { // Se encontrar um espaço
                    serviceName = line.substring(0, index); // Extrair a primeira parte
                    displayName = line.substring(index + 1); // Extrair a segunda parte
                    displayName = displayName.replaceAll("  ", "");

                }
//                // Exibir os serviços formatados
                System.out.println(serviceName + " - " + displayName + " - " + status);

                if (!line.contains("---") && !line.contains("ServiceNameDisplayName")) {

//                    if (numeracao == 0) {
//                        
//                        enviarMensagem("iniservice:" + displayName+ "|" + serviceName + "|" + status);
//                        numeracao++;
//                    } else {
//                        enviarMensagem(displayName+ "|" + serviceName + "|" + status);
//                    }
                    servicos.add(displayName + "|" + serviceName + "|" + status);
                }
            }
            Collections.sort(servicos);

            for (int i = 0; i < servicos.size(); i++) {
                if (i == 0) {
                    enviarMensagem("iniservice:" + servicos.get(i));
                } else {
                    enviarMensagem(servicos.get(i));
                }
            }
            enviarMensagem("fimservice");
            System.out.println("tamanho do array:" + servicos.size());
            servicos.clear();

            reader.close();
        } catch (IOException e) {
            enviarMensagem("Não foi possível carregar os Serviços!");
        }
    }

    private void atualizar() {
        if (verificarArquivoCliente2()) {
            this.enviarMensagem("Atualizar Cliente requerido. Cliente.exe econtrado!");
            InstanciasGlobais.setExecutandoComando(false);
            System.out.println("codigodeatualizacao:12345");
            System.exit(33);

        } else {
            this.enviarMensagem("Atualizacao cancelada, Cliente.exe não econtrado!");
        }

    }

    public static boolean verificarArquivoCliente2() {
        // Diretório local onde o Cliente2.exe está localizado
        File diretorioLocal = new File(System.getProperty("user.dir"));

        // Arquivo local do Cliente2.exe
        File arquivoCliente2 = new File(diretorioLocal, "\\atualizacao\\Cliente.exe");

        return arquivoCliente2.exists();
    }

    private void desligar() {
        System.out.println("Desligar Cliente");
        try {
            // while (!instancias.getSocket().isClosed()) {

            instancias.getOut().flush();
            instancias.getOut().close();
            instancias.getIn().close();
            instancias.getSocket().shutdownInput();
            instancias.getSocket().shutdownOutput();
            instancias.getSocket().close();
            instancias = null;
            // }
        } catch (Exception ex) {
            System.out.println("Erro ao desconectar" + ex.getMessage());

        }
        this.interrupt();
        InstanciasGlobais.setExecutandoComando(false);
        System.exit(1);
    }

    private void exibirMensagem(ArrayList<String> textoRecebidoarray, String mensagemRecebida) {
        JTextArea area = new JTextArea();
        textoRecebidoarray.set(0, mensagemRecebida);
        for (int i = 0; i < textoRecebidoarray.size(); i++) {

            area.append(textoRecebidoarray.get(i) + "\n");
            // System.out.println("linha array:" + textoRecebidoarray.get(i));

        }

        // System.out.println(area.getText());
        InstanciasGlobais.getMensagemConversaTelaInstacia().exibirMensagem(area);
    }
}
