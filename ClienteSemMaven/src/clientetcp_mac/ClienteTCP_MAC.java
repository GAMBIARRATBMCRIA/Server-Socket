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

    /**
     * Loop de conexão iterativo com backoff exponencial.
     * Substitui a recursão anterior que causava StackOverflowError.
     * O status da bandeja só é atualizado como "Conectado" APÓS autenticação bem-sucedida.
     */
    public void conecta() {
        int tentativa = 0;
        long delayMs = 2000; // backoff inicial: 2s, dobra a cada falha até 30s
        final long MAX_DELAY_MS = 30000;

        while (true) {
            tentativa++;
            System.out.println("Tentativa de conexão #" + tentativa + " em: " + ipServer);
            InstanciasGlobais.getBandejaSistema().setStatus("Conectando... (tentativa " + tentativa + ")");

            try {
                // Passo 1: descobrir IP do servidor se necessário
                if (ipServer == null || ipServer.isEmpty() || ipServer.equalsIgnoreCase("ADM")) {
                    BuscaServer b = new BuscaServer();
                    ipServer = b.buscaServer();
                    if (ipServer == null) {
                        System.out.println("Servidor não encontrado via broadcast. Aguardando " + delayMs / 1000 + "s...");
                        InstanciasGlobais.getBandejaSistema().setStatus("Servidor não encontrado. Aguardando...");
                        Thread.sleep(delayMs);
                        delayMs = Math.min(delayMs * 2, MAX_DELAY_MS);
                        continue;
                    }
                }
                config.enderecoSevidor = ipServer;

                // Passo 2: estabelecer socket TCP
                socket = new Socket(ipServer, config.portaServidor);
                socket.setSoTimeout(60000); // 60s de timeout para detectar sockets mortos
                // NÃO atualiza bandeja aqui — aguarda autenticação

                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Passo 3: validar e autenticar MAC
                String mac = validar();
                if (mac == null || mac.isEmpty()) {
                    System.out.println("Erro: MAC address não pôde ser obtido.");
                    out.println("Erro:mac");
                    desconectar();
                    Thread.sleep(delayMs);
                    delayMs = Math.min(delayMs * 2, MAX_DELAY_MS);
                    continue;
                }
                out.println(mac + ":" + config.versao_cliente);

                // Passo 4: só após autenticação enviada, inicializar e atualizar status
                instacias = new ArmazenarInstacias(in, out, socket, mac, caminhoUser + "\\Desktop\\");
                globalInstacia = new InstanciasGlobais(instacias, telaBloqueio, mensagemtela, telalog, log2);
                InstanciasGlobais.getBandejaSistema().setStatus("Conectado ao servidor: " + ipServer);

                // Passo 5: entrar no loop de recepção de mensagens (bloqueante)
                aguardarMensagem();

                // Se aguardarMensagem() retornou, o servidor fechou a conexão — reconectar
                System.out.println("Conexão encerrada pelo servidor. Reconectando...");
                desconectar();
                delayMs = 2000; // reset do backoff após desconexão limpa

            } catch (InterruptedException ie) {
                System.out.println("Conexão interrompida.");
                Thread.currentThread().interrupt();
                break;

            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : "erro desconhecido";
                System.out.println("Erro na conexão (tentativa " + tentativa + "): " + msg);
                InstanciasGlobais.getBandejaSistema().setStatus("Erro: " + msg);
                desconectar();

                // Se o IP atual falhou, forçar redescoberta na próxima tentativa
                if (ipServer != null && msg.contains(ipServer)) {
                    ipServer = "";
                }

                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
                delayMs = Math.min(delayMs * 2, MAX_DELAY_MS);
            }
        }
    }

    /**
     * Fecha todos os recursos de rede de forma segura, sem recursão.
     */
    void desconectar() {
        try {
            if (out != null) {
                out.flush();
                out.close();
            }
        } catch (Exception ex) {
            System.out.println("Erro ao fechar PrintWriter: " + ex.getMessage());
        }
        try {
            if (in != null) {
                in.close();
            }
        } catch (Exception ex) {
            System.out.println("Erro ao fechar BufferedReader: " + ex.getMessage());
        }
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception ex) {
            System.out.println("Erro ao fechar socket: " + ex.getMessage());
        }
        instacias = null;
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

    /**
     * Loop de recepção de mensagens do servidor.
     * Corrigido: checa null do readLine() (conexão encerrada pelo servidor)
     * e trata SocketTimeoutException para detectar sockets zumbis.
     */
    void aguardarMensagem() throws Exception {
        ArrayList<String> textoRecebido = new ArrayList<>();
        String caminho_inicial = instacias.getCamnhoGravacao();

        while (instacias.getSocket() != null && !instacias.getSocket().isClosed()) {
            String line;
            try {
                line = instacias.getIn().readLine();
            } catch (java.net.SocketTimeoutException ste) {
                // Timeout de leitura — socket pode estar morto, envia ping
                System.out.println("Timeout de leitura. Verificando conexao com ping...");
                instacias.getOut().println("ping");
                if (instacias.getOut().checkError()) {
                    System.out.println("Erro ao enviar ping. Servidor pode estar inativo.");
                    break;
                }
                continue;
            }

            // readLine() retorna null quando o servidor fecha a conexão
            if (line == null) {
                System.out.println("Servidor encerrou a conexão (readLine retornou null).");
                break;
            }

            if (line.equalsIgnoreCase("pong")) {
                continue;
            }

            if (line.contains("tipodadoarquivo:")) {
                instacias.setTipodado("tipodadoarquivo:");
                instacias.setCamnhoGravacao(instacias.getCamnhoGravacao() + line.replaceAll("tipodadoarquivo:", ""));
                System.out.println("O arquivo vai ser gravado em:" + instacias.getCamnhoGravacao());
            }

            if (!instacias.getTipodado().contains("tipodadoarquivo:")) {

                textoRecebido.add(line.replaceAll("\n", ""));

                while (instacias.getIn().ready()) {
                    line = instacias.getIn().readLine();
                    if (line == null) break;
                    textoRecebido.add(line.replaceAll("\n", ""));
                }

                if (!textoRecebido.isEmpty() && textoRecebido.get(0).length() > 0) {
                    new ExecutarComando(textoRecebido.get(0), instacias, textoRecebido);
                    textoRecebido.clear();
                    caminho_inicial = instacias.getCamnhoGravacao();
                }

            } else {
                System.out.println("recebendo arquivo...");
                new receberArquivo(instacias).join();
                instacias.setCamnhoGravacao(caminho_inicial);
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
