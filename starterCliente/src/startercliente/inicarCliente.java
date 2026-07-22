/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package startercliente;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import javax.swing.JOptionPane;

/**
 *
 * @author LAB01
 */
public class inicarCliente {

    public static String PID_starter = "";

    public void iniciarcliente() {
        pidStarter();
        try {
            ProcessBuilder builder = new ProcessBuilder(configs.configuracoes.caminhocliente);
            builder.redirectErrorStream(true);
            controle.controle.processo = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(controle.controle.processo.getInputStream()));
            String linha;
            boolean atualizar = false;
            boolean autorizadoServidor = false;

            while ((linha = reader.readLine()) != null) {
                System.out.println(linha);
                if (linha.startsWith("PID:")) {
                    controle.controle.pidProcess = linha.replaceAll("PID:", "");
                    System.out.println("pid " + controle.controle.pidProcess + " indetificado!");
                }
                if (linha.startsWith("codigodeatualizacao:12345")) {
                    atualizar = true;
                    break;
                }
            }

            int resultado = controle.controle.processo.waitFor();
            System.out.println("res1:" + resultado);

             if (atualizar == false) {
                int tentativas = 1;

                while (tentativas <= 3 && autorizadoServidor == false) {
                    String senha = JOptionPane.showInputDialog(null, "Está tentando fechar um serviço de Administração!\nSenha de autorização:\n\nTentativas:" + tentativas + " de 3", "Erro", JOptionPane.ERROR_MESSAGE);
                    if (senha == null) {
                        System.out.println("------------------------------------------------------------------------------------------");
                        //controle.controle.processo = builder.start();
                        break;
                    } else if (senha.equals(configs.configuracoes.senhaAdm)) {
                        JOptionPane.showMessageDialog(null, "Processo fechado com sucesso!");
                        autorizadoServidor = true;
                    }
                    tentativas++;
                }

                if (autorizadoServidor == false) {
                    JOptionPane.showMessageDialog(null, "Tentativa não autorizada!");
                    controle.controle.processo.destroy();
                    iniciarcliente();
                } else {
                    controle.controle.processo.destroy();
                }

            } else {
                //atualizar
                System.out.println("fechando");
                controle.controle.processo.destroy();
                ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "taskkill /f /pid " + controle.controle.pidProcess);
                Process process = processBuilder.start();

                int prossoFechamento = process.waitFor();
                System.out.println("Codigo de fechamento:" + prossoFechamento);

                //System.out.println("verificado se está ativo:" + controle.controle.processo.isAlive());
                System.out.println(configs.configuracoes.diretorioProjeto + "\\modulo_atualizacao.exe");
                ProcessBuilder modulo_atualizar = new ProcessBuilder(configs.configuracoes.diretorioProjeto + "\\modulo_atualizacao.exe", PID_starter);
                modulo_atualizar.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void pidStarter() {
        // Obtém a bean de gerenciamento da máquina virtual em execução
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

        // Obtém o nome do processo, que geralmente contém o PID
        String nomeDoProcesso = runtimeMXBean.getName();

        // Extrai o PID do nome do processo
        long pid = Long.parseLong(nomeDoProcesso.split("@")[0]);
        PID_starter = pid + "";
    }
}
