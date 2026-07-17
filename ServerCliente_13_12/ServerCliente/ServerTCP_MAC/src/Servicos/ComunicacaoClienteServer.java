package Servicos;

import BaseDados.ClientesConectados;
import BaseDados.infomacaoMaquinas;
import ControleServerTCP.ServerVideo;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JProgressBar;

import javax.swing.JTextArea;

public class ComunicacaoClienteServer extends Thread {

    private BufferedReader in;
    private BufferedWriter enviarBuffer;
    private PrintWriter out;
    private Socket clienteSocket;

    private ClientesConectados infoCliente;

    public ComunicacaoClienteServer(Socket socketCliente) {
        try {
            clienteSocket = socketCliente;  

            in = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
            out = new PrintWriter(clienteSocket.getOutputStream(), true);
            enviarBuffer = new BufferedWriter(new OutputStreamWriter(clienteSocket.getOutputStream()));

            //para o vídeo
            
            start();
        } catch (Exception ex) {
            System.out.println("Erro no cliente Construtor Comunicação cliente:" + ex.getMessage());

            desconectarCliente(1);

            if (clienteSocket.isClosed()) {
                System.out.println("socket fechado");
            }

            this.interrupt();
            Thread.currentThread().interrupt();

            if (this.isInterrupted()) {
                System.out.println("interrompida");
            }
        }
    }

    @Override
    public void run() {
        try {
            // System.out.println("Cliente conectado: " + clienteSocket.getInetAddress().getHostAddress());
            ExecucaoAtividadesTela.publicarAvido("Cliente conectado: " + clienteSocket.getInetAddress().getHostAddress());

            if (!autenticarCliente()) {
                System.out.println("Cliente não encontrado!");
                ExecucaoAtividadesTela.publicarAvido("Cliente não cadatrado!");
            }

            this.infoCliente.setReceberComando(true);
            infoCliente.setTextoArea(new JTextArea());
            infoCliente.setBarraProgresso(new JProgressBar(0, 100));

            Instancias.getControlebaseIntancia().setClistesLista(this);
            Instancias.getCarregarTableInstacia().tabelaConectados();

            String linha;

            while ((linha = in.readLine()) != null) {

                infoCliente.getTextoArea().append(linha + "\n");
//                System.out.println("recebido do cliente:" + linha);

                if (linha.startsWith("iniservice:")) {
                    carregarService(linha);
                }

                Thread.sleep(100);
            }

        } catch (Exception ex) {
            System.out.println("Erro no cliente1:" + ex.getMessage());
            desconectarCliente(1);
            try {

                this.interrupt();
                Thread.currentThread().interrupt();

            } catch (Exception ex1) {
                ExecucaoAtividadesTela.publicarAvido("Erro (ComunicacaoClienteServer):" + ex1.getMessage());
            }
        }
    }

    public void enviarMensagemSimples(String mensagem) {
        out.println(mensagem);
        out.flush();
    }

    private Boolean autenticarCliente() {
        boolean validado = false;
        String versaoCliente = "0";
        try {

            String line = in.readLine();
            String dados[] = line.split(":");
            line = dados[0];
            if (dados.length > 1) {
                versaoCliente = dados[1];
            }

            System.out.println("linha recebida:" + line);

            if (line.contains("Erro:mac")) {
                validado = false;
            } else {

                if (verificaClienteBanco(line)) {
                    validado = true;
                    enviarMensagemSimples("autenticado");
                } else {
                    System.out.println("Validacao falhou:" + line);
                    validado = false;
                }
            }
        } catch (Exception ex) {
            ExecucaoAtividadesTela.publicarAvido("Erro na validação:" + ex.getMessage());
        }

        if (versaoCliente.equalsIgnoreCase(configs.configuracao.currentVersionClient)) {
            this.infoCliente.setStatusCliente("Ativo/Atualizado");
        } else {
            this.infoCliente.setStatusCliente("Ativo/Desatualizado");
        }

        return validado;

    }

    public Boolean enviarMensagemPesada(String mensagem) {
        Boolean enviado = false;
        try {
            enviarBuffer.write(mensagem);
            enviarBuffer.newLine();
            enviarBuffer.flush();
            enviado = true;
        } catch (Exception ex) {
            enviado = false;
        }
        return enviado;
    }

    public Boolean fecharSocket() {
        try {
            clienteSocket.close();
            return true;
        } catch (Exception ex) {
            ExecucaoAtividadesTela.publicarAvido("Erro ao fechar socket:" + ex.getMessage());
            return false;
        }
    }

    public Boolean verificaClienteBanco(String mac) {
        Boolean achou = false;

        infomacaoMaquinas inf = Servicos.Instancias.getExecucaoAtividadesInstacia().procuraClientes(mac);

        //System.out.println("inf:" + inf);
        infoCliente = new ClientesConectados();

        if (inf != null) {
            System.out.println("Bancada:" + inf.getBancada());
            System.out.println("Posicao:" + inf.getPosicao());
            System.out.println("Setor:" + inf.getSetor());
            System.out.println("Mac:" + inf.getMacAddres());
            System.out.println("Tombo:" + inf.getTombo());

            infoCliente.setBancada(inf.getBancada());
            infoCliente.setClientesocket(this.clienteSocket);
            infoCliente.setEntrada(this.in);
            infoCliente.setSaidaBuffer(this.enviarBuffer);
            infoCliente.setSaidaPrint(this.out);
            infoCliente.setMacAddres(inf.getMacAddres());
            infoCliente.setPosicao(inf.getPosicao());
            infoCliente.setSetor(inf.getSetor());
            infoCliente.setTombo(inf.getTombo());
            infoCliente.setThreandCliente(this);
            
            infoCliente.setIpAddress(this.clienteSocket.getInetAddress().getHostAddress().toString());
            achou = true;
        } else {
            if (!naoContemBaco(mac)) {
                System.out.println("Não foi inserido");
            }
            achou = false;
        }

        return achou;
    }

    private Boolean naoContemBaco(String mac) {

        infoCliente.setBancada("====");
        infoCliente.setClientesocket(this.clienteSocket);
        infoCliente.setEntrada(this.in);
        infoCliente.setSaidaBuffer(this.enviarBuffer);
        infoCliente.setSaidaPrint(this.out);
        infoCliente.setMacAddres(mac);
        infoCliente.setPosicao("====");
        infoCliente.setSetor("====");
        infoCliente.setTombo("====");
        infoCliente.setThreandCliente(this);

        return true;
    }

    public ClientesConectados getonformacaoesCliente() {
        return infoCliente;
    }

    public void setInformacoesCliente(ClientesConectados atualizacoesCliente) {
        infoCliente = atualizacoesCliente;
    }

    public Boolean desconectarCliente(int codigo) {
        Boolean desconectador = false;
        //codigo 1 para indicar que vai ser fechado por um erro ao executar o cliente
        //diferente de 1 para idicar que foi o servidor que mandou encerrar
        boolean removido = false;
        if (codigo == 1) {
            while (!clienteSocket.isClosed()) {
                System.out.println("Cliente closed:" + clienteSocket.isClosed());
                try {
                    out.flush();
                    out.close();
                    in.close();
                    clienteSocket.close();
                    desconectador = true;
                } catch (Exception ex) {
                    System.out.println("Erro (Comunicação cliente)" + ex.getMessage());
                    desconectador = false;
                }
            }

            Instancias.getCarregarTableInstacia().tabelaConectados();

            while (removido == false) {
//                System.out.println("removendo:" + infoCliente.getMacAddres());
                removido = Instancias.getCarregarTableInstacia().removerClienteConexaoTable(infoCliente.getMacAddres());
            }

        } else {
            while (!clienteSocket.isClosed()) {
                System.out.println("Cliente closed:" + clienteSocket.isClosed());
                try {
                    out.flush();
                    out.close();
                    in.close();
                    clienteSocket.close();
                    desconectador = true;
                    removido = true;
                } catch (Exception ex) {
                    System.out.println("Erro (Comunicação cliente)" + ex.getMessage());
                    desconectador = false;
                }
            }
            Instancias.getCarregarTableInstacia().tabelaConectados();
            this.interrupt();
        }

        return desconectador;
    }

    public void enviarArquivo(String caminho) {
        Thread enviarThread = new Thread(() -> {
            try {
                OutputStream outputStream = this.clienteSocket.getOutputStream();
                File fileToSend = new File(caminho);
                System.out.println("Enviando arquivo do caminho: " + caminho);

                FileInputStream fileInputStream = new FileInputStream(fileToSend);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

                byte[] buffer = new byte[1024];
                int bytesRead;
                long fileSize = fileToSend.length();
                long totalBytesRead = 0;
                int progresso_atual = 0;

                infoCliente.getBarraProgresso().setVisible(true);
                infoCliente.getBarraProgresso().setValue(0);
                Boolean paraEnvio = false;
                //this.clienteSocket.setSoTimeout(100);
//                Thread tr = new Thread(){
//                   
//                };
//                tr.start();
                while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    System.out.println("Bytes enviados: " + bytesRead);

                    int progress = (int) ((totalBytesRead * 100) / fileSize);

                    if (progress > progresso_atual) {
                        infoCliente.getBarraProgresso().setValue(progress);
                        progresso_atual = progress;
                    }
                }

                bufferedInputStream.close();
//                if (bytesRead >= 1024) {
//                    //outputStream.write(0);
//                }

                outputStream.flush();

                System.out.println("Arquivo enviado com sucesso. Total de bytes enviados: " + totalBytesRead);

                infoCliente.getBarraProgresso().setVisible(false);

            } catch (Exception e) {
                ExecucaoAtividadesTela.publicarAvido(
                        "Erro ao enviar um arquivo para: " + this.infoCliente.getMacAddres()
                        + " BC: " + this.infoCliente.getBancada()
                        + " PO: " + this.infoCliente.getPosicao() + " | " + e.getMessage());
            }
        });

        enviarThread.start();
    }

    private String[] formatarService(String linha) {
        String[] texto = new String[3];

        String input = linha;

        int primeiroPipe = input.indexOf('|'); // Encontra o primeiro |
        int segundoPipe = input.indexOf('|', primeiroPipe + 1); // Encontra o segundo |

        if (primeiroPipe != -1 && segundoPipe != -1) { // Se encontrar ambos os |
            String parte1 = input.substring(0, primeiroPipe).trim(); // Extrai a primeira parte
            String parte2 = input.substring(primeiroPipe + 1, segundoPipe).trim(); // Extrai a segunda parte
            String parte3 = input.substring(segundoPipe + 1).trim(); // Extrai a terceira parte

            texto[0] = parte2;
            texto[1] = parte1;
            texto[2] = parte3;

        } else {
            System.out.println("Não foi possível separar o texto corretamente.");
        }
        return texto;
    }

    public void carregarService(String linha) {
        ArrayList<String[]> dados = new ArrayList<>();
        dados.add(formatarService(linha.replaceAll("iniservice:", "")));
        String line;

        try {
            while ((line = this.in.readLine()) != "fimservice") {

                if (line.equalsIgnoreCase("fimservice")) {
                    break;
                }
                if (line.contains("|") && !line.contains("---") && !line.contains("ServiceNameDisplayName")) {
//                    if (line.contains("iniservice:")) {
//                        System.out.println("linha serviço:"+line);
//                        line = line.replaceAll("iniservice:", "");
//                    }
                    dados.add(formatarService(line));
                }

            }

        } catch (IOException ex) {
            ExecucaoAtividadesTela.publicarAvido("Erro ao carregar os serviços!");
        }

        ExecucaoAtividadesTela.preencharServices(dados);
    }

}
