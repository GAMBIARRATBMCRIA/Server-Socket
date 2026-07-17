/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientetcp_mac;

import Global.Controle_log_Handler;
import clientetcp_mac.*;
import Global.InstanciasGlobais;
import Global.config;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import telas.MensagemConversa;
import telas.bloqueioTela;
import telas.telaLog;

/**
 *
 * @author LAB01
 */
public class ClienteTCP_MAC {
    BufferedReader in = null;
    PrintWriter out = null;
    Socket socket = null;
    static String caminhoUser;
    ArmazenarInstacias instacias = null;
    InstanciasGlobais globalInstacia;
    bloqueioTela telaBloqueio = new bloqueioTela();
    MensagemConversa mensagemtela = new MensagemConversa();
    telaLog telalog = new telaLog();
    Logger log2;
    static String currentPID = "0";
    String ipServer = "";

    public static void main(String[] args) {
        ClienteTCP_MAC cli = new ClienteTCP_MAC();
        cli.log2 = Logger.getLogger(ClienteTCP_MAC.class.getName());

        PrintStream printStream = new PrintStream(new CustomOutputStream(cli.telalog.textologs));
        System.setOut(printStream);

        Controle_log_Handler hnd = new Controle_log_Handler();
        hnd.setFormatter(new SimpleFormatter());

        cli.log2.addHandler(hnd);
        InstanciasGlobais.setTelaLogInstandia(cli.telalog);

        Boolean inicializar = false;
        // Obtém a bean de gerenciamento da máquina virtual em execução
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

        // Obtém o nome do processo, que geralmente contém o PID
        String nomeDoProcesso = runtimeMXBean.getName();

        // Extrai o PID do nome do processo
        long pid = Long.parseLong(nomeDoProcesso.split("@")[0]);

        currentPID = pid + "";
        System.out.println("PID:" + currentPID);

        try {
            //String comando2 = "tasklist /v /fi \"USERNAME eq NT AUTHORITY\\SYSTEM\" | find \"" + currentPID + "\"";
            String comando = "tasklist /v /fi \"USERNAME eq AUTORIDADE NT\\SISTEMA\" | find \"" + currentPID + "\"";

            Process process = executaComando(comando);
            // Lê a saída do processo
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String linha;
            //ExecutarComando exe = new ExecutarComando();

            Process pro = executaComando("echo %USERPROFILE%");
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            
            caminhoUser = reader2.readLine();
            InstanciasGlobais.caminhoUsuario = caminhoUser;
            reader2.close();

            if ((linha = reader.readLine()) != null) {
                reader.close();
                System.out.println("Existe:" + linha);
                Process processoEncerra = executaComando("taskkill /f /pid " + currentPID);
                int codi = processoEncerra.waitFor();
                System.out.println("Codigo de saida encerramento" + codi);
            } else {
                System.out.println("user local:" + caminhoUser);
                inicializar = true;
            }

        } catch (Exception e) {
            System.out.println("Erro ao inicializar" + e.getMessage());

        }

        if (inicializar) {
            InstanciasGlobais.setBandejaSistema(new bandejaSistema());
            //System.out.println("atribuindo bandeja");;
            InstanciasGlobais.getBandejaSistema().abriBandeja();
            //System.out.println("==============================================================");
            cli.ipServer = config.enderecoSevidor;
            cli.conecta();

        } else {
            JOptionPane.showMessageDialog(null, "Por erro interno, o programa está fechando!");
            System.exit(0);
        }

    }

    static Process executaComando(String comando) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", comando);
        Process process = processBuilder.start();
        return process;
    }

    public void conecta() {
        try {
            System.out.println("Abrindo conexão em:" + ipServer);
            InstanciasGlobais.getBandejaSistema().setStatus("Conectando na porta:" + config.portaServidor);
            BuscaServer b = new BuscaServer();

            if (ipServer.isEmpty()) {
                ipServer = b.buscaServer();
            }
            config.enderecoSevidor = ipServer;
            String mac = "";
            socket = new Socket(ipServer, config.portaServidor);

            InstanciasGlobais.getBandejaSistema().setStatus("Conexão estabelecida.");

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            mac = validar();
            if (mac == null) {
                out.println("Erro:mac");
                throw new Exception("finalizar");
            } else {
                out.println(mac+":"+config.versao_cliente);
            }
            instacias = new ArmazenarInstacias(in, out, socket, mac, caminhoUser+"\\Desktop\\");
            globalInstacia = new InstanciasGlobais(instacias, telaBloqueio, mensagemtela, telalog, log2);
            aguardarMensagem();

        } catch (Exception e) {
            System.out.println("Erro desconhecido:" + e.getMessage());
            InstanciasGlobais.getBandejaSistema().setStatus("Erro:" + e.getMessage());
            desconectar();

            try {
                System.out.println("Erro na conexao:" + e.getMessage() + "\nTentando novamente");

                if (!e.getMessage().equalsIgnoreCase("finalizar")) {

                    if (e.getMessage().contains(config.enderecoSevidor)) {
                        ipServer = "";
                    }
                    Thread.sleep(100);
                    conecta();
                }
                Thread.sleep(1000);
            } catch (Exception efd) {
                desconectar();
                try {
                    Thread.sleep(5000);

                    conecta();
                } catch (InterruptedException ex) {
                    System.out.println("vlcx:" + ex.getMessage());
                }
                System.out.println("Erro:" + efd.getMessage());
            }

        }
    }

    void desconectar() {
        try {
            out.flush();
            out.close();
            in.close();
            socket.shutdownInput();
            socket.shutdownOutput();
            socket.close();
            instacias = null;
            while (!socket.isClosed()) {
                desconectar();
            }
        } catch (Exception ex) {
            System.out.println("Erro ao desconectar" + ex.getMessage());
        }
    }

    public String validar() {
        String macAddres = "";
        try {
            InetAddress ipAddress = socket.getLocalAddress();
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(ipAddress);
            if (networkInterface != null) {
                byte[] macAddress = networkInterface.getHardwareAddress();

                if (macAddress != null) {
                    StringBuilder macAddressStr = new StringBuilder();
                    for (int i = 0; i < macAddress.length; i++) {
                        macAddressStr.append(String.format("%02X%s", macAddress[i], (i < macAddress.length - 1) ? "-" : ""));
                    }

                    macAddres = macAddressStr.toString();
                } else {
                    System.out.println("Endereço MAC não encontrado para esta interface.");
                }
            } else {
                System.out.println("Interface de rede não encontrada para este endereço IP.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return macAddres;
    }

    void aguardarMensagem() throws Exception {
        ArrayList<String> textoRecebido = new ArrayList<>();
        String caminho_inicial = instacias.getCamnhoGravacao();

        while (instacias.getSocket().isConnected()) {
            String line = instacias.getIn().readLine();

            if (line.contains("tipodadoarquivo:")) {
                instacias.setTipodado("tipodadoarquivo:");
                instacias.setCamnhoGravacao(instacias.getCamnhoGravacao() + line.replaceAll("tipodadoarquivo:", ""));

                System.out.println("O arquivo vai ser gravado em:" + instacias.getCamnhoGravacao());
            }

            if (!instacias.getTipodado().contains("tipodadoarquivo:")) {

                textoRecebido.add(line.replaceAll("\n", ""));

                while (instacias.getIn().ready()) {
                    line = instacias.getIn().readLine();
                    textoRecebido.add(line.replaceAll("\n", ""));
                }

                if (textoRecebido.get(0).toString().length() > 0) {

                    new ExecutarComando(textoRecebido.get(0).toString(), instacias, textoRecebido);

                    textoRecebido.clear();
                    caminho_inicial = instacias.getCamnhoGravacao();
                }

            } else {
                System.out.println("recebendo arquivo...");
                new receberArquivo(instacias).join();

                instacias.setCamnhoGravacao(caminho_inicial);
                //System.out.println("Recebido");
            }

            Thread.sleep(100);

        }
    }

    void antesDeEcerrar() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            while (InstanciasGlobais.isExecutandoComando()) {
                System.out.println("Aguardando execução de comando terminar");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    System.out.println("Erro:" + ex.getMessage());
                }
            }
            while (InstanciasGlobais.isRecebendoArquivo()) {
                System.out.println("Aguardando receber todos os pacotes de arquivos!");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    System.out.println("Erro:" + ex.getMessage());
                }
            }
            desconectar();

        }));
    }

    static class CustomOutputStream extends OutputStream {

        private JTextArea textArea;
        private PrintStream saidaSistema;

        public CustomOutputStream(JTextArea textArea) {
            this.textArea = textArea;
            saidaSistema = System.out;
        }

        @Override
        public void write(int b) {
            textArea.append(String.valueOf((char) b));
            textArea.setCaretPosition(textArea.getDocument().getLength());
            saidaSistema.append(String.valueOf((char) b));
        }

        @Override
        public void write(byte[] b, int off, int len) {
            textArea.append(new String(b, off, len));
            textArea.setCaretPosition(textArea.getDocument().getLength());

            saidaSistema.append(new String(b, off, len));
        }
    }
}
