package clientetcp_mac.modulos.impl;

import clientetcp_mac.ArmazenarInstacias;
import clientetcp_mac.modulos.IModulo;
import java.util.ArrayList;
import java.util.Collections;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class ModuloServicos implements IModulo {

    @Override
    public boolean podeProcessar(String comando) {
        return comando.equalsIgnoreCase("liserv:");
    }

    @Override
    public void executar(String comando, ArmazenarInstacias instancias, ArrayList<String> textoRecebido) {
        listarServicos(instancias);
    }

    private void listarServicos(ArmazenarInstacias instancias) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("powershell.exe", "Get-Service | Select-Object Status, ServiceName, DisplayName");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            ArrayList<String> servicos = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
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

                int index = line.indexOf(" ");
                if (index != -1) {
                    serviceName = line.substring(0, index);
                    displayName = line.substring(index + 1);
                    displayName = displayName.replaceAll("  ", "");
                }

                System.out.println(serviceName + " - " + displayName + " - " + status);

                if (!line.contains("---") && !line.contains("ServiceNameDisplayName")) {
                    servicos.add(displayName + "|" + serviceName + "|" + status);
                }
            }
            Collections.sort(servicos);

            for (int i = 0; i < servicos.size(); i++) {
                if (i == 0) {
                    enviarMensagem("iniservice:" + servicos.get(i), instancias);
                } else {
                    enviarMensagem(servicos.get(i), instancias);
                }
            }
            enviarMensagem("fimservice", instancias);
            System.out.println("tamanho do array:" + servicos.size());
            servicos.clear();

            reader.close();
        } catch (IOException e) {
            enviarMensagem("Não foi possível carregar os Serviços!", instancias);
        }
    }
    
    private void enviarMensagem(String mensagem, ArmazenarInstacias instancias) {
        instancias.getOut().println(mensagem);
        instancias.getOut().flush();
    }
}
