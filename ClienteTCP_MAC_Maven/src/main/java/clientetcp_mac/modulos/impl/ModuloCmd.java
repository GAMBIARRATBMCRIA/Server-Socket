package clientetcp_mac.modulos.impl;

import clientetcp_mac.ArmazenarInstacias;
import clientetcp_mac.modulos.IModulo;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ModuloCmd implements IModulo {

    @Override
    public boolean podeProcessar(String comando) {
        return comando.contains("cmd:") || comando.startsWith("cmds:");
    }

    @Override
    public void executar(String comando, ArmazenarInstacias instancias, ArrayList<String> textoRecebido) {
        if (comando.contains("cmd:")) {
            executarCmd(comando.replaceAll("cmd:", ""), instancias);
        } else if (comando.startsWith("cmds:")) {
            executarScriptCmd(textoRecebido, instancias);
        }
    }

    private void executarCmd(String comando, ArmazenarInstacias instancias) {
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
            instancias.getOut().flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void executarScriptCmd(ArrayList<String> textoRecebidoarray, ArmazenarInstacias instancias) {
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
                    instancias.getOut().flush();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        textoRecebidoarray.clear();
    }
}
