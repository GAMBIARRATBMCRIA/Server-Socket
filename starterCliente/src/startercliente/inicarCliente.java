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

    /**
     * Verifica se ja existe um processo do cliente rodando no sistema.
     * CORRECAO BUG 8: evita iniciar dois processos cliente para o mesmo MAC,
     * o que causaria duplicatas na tabela do servidor.
     *
     * @return true se o processo ja estiver rodando, false caso contrario
     */
    private boolean clienteJaRodando() {
        try {
            String nomeExe = "Cliente.exe";
            ProcessBuilder pb = new ProcessBuilder("tasklist", "/FI", "IMAGENAME eq " + nomeExe, "/FO", "CSV", "/NH");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String linha;
            while ((linha = reader.readLine()) != null) {
                if (linha.toLowerCase().contains(nomeExe.toLowerCase())) {
                    System.out.println("[Starter] Processo " + nomeExe + " ja esta rodando: " + linha);
                    reader.close();
                    process.destroy();
                    return true;
                }
            }
            reader.close();
            process.destroy();
        } catch (Exception e) {
            System.out.println("[Starter] Erro ao verificar processo existente: " + e.getMessage());
        }
        return false;
    }

    public void iniciarcliente() {
        // CORRECAO BUG 8: verifica se ja existe processo cliente antes de iniciar
        if (clienteJaRodando()) {
            System.out.println("[Starter] Cliente ja esta em execucao. Nao iniciando novo processo.");
            return;
        }

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
                    String senha = JOptionPane.showInputDialog(null,
                            "Esta tentando fechar um servico de Administracao!\nSenha de autorizacao:\n\nTentativas:" + tentativas + " de 3",
                            "Erro", JOptionPane.ERROR_MESSAGE);
                    if (senha == null) {
                        System.out.println("------------------------------------------------------------------------------------------");
                        break;
                    } else if (senha.equals(configs.configuracoes.senhaAdm)) {
                        JOptionPane.showMessageDialog(null, "Processo fechado com sucesso!");
                        autorizadoServidor = true;
                    }
                    tentativas++;
                }

                if (autorizadoServidor == false) {
                    JOptionPane.showMessageDialog(null, "Tentativa nao autorizada!");
                    controle.controle.processo.destroy();
                    iniciarcliente();
                } else {
                    controle.controle.processo.destroy();
                }

            } else {
                // atualizar
                System.out.println("fechando para atualizar");
                controle.controle.processo.destroy();
                ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "taskkill /f /pid " + controle.controle.pidProcess);
                Process process = processBuilder.start();

                int prossoFechamento = process.waitFor();
                System.out.println("Codigo de fechamento:" + prossoFechamento);

                System.out.println(configs.configuracoes.diretorioProjeto + "\\modulo_atualizacao.exe");
                ProcessBuilder modulo_atualizar = new ProcessBuilder(
                        configs.configuracoes.diretorioProjeto + "\\modulo_atualizacao.exe", PID_starter);
                modulo_atualizar.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void pidStarter() {
        // Obtem a bean de gerenciamento da maquina virtual em execucao
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

        // Obtem o nome do processo, que geralmente contem o PID
        String nomeDoProcesso = runtimeMXBean.getName();

        // Extrai o PID do nome do processo
        long pid = Long.parseLong(nomeDoProcesso.split("@")[0]);
        PID_starter = pid + "";
    }
}
