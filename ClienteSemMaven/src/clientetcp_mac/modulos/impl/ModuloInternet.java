package clientetcp_mac.modulos.impl;

import clientetcp_mac.ArmazenarInstacias;
import clientetcp_mac.modulos.IModulo;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ModuloInternet implements IModulo {

    @Override
    public boolean podeProcessar(String comando) {
        return comando.equalsIgnoreCase("pr:BloquearInternet") || comando.equalsIgnoreCase("pr:Desbloquearinternet");
    }

    @Override
    public void executar(String comando, ArmazenarInstacias instancias, ArrayList<String> textoRecebido) {
        if (comando.equalsIgnoreCase("pr:BloquearInternet")) {
            bloquearInternet();
        } else if (comando.equalsIgnoreCase("pr:Desbloquearinternet")) {
            DesbloquearInternet();
        }
    }

    private void bloquearInternet() {
        String comand = "reg add \"HKEY_CURRENT_USER\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\Connections\" "
                + "/v DefaultConnectionSettings /t REG_BINARY /d "
                + "460000000a0000000b0000000a000000302e302e302e303a38300000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000 /f";
        executarCmd(comand);
        executarCmd("ipconfig /flushdns");
    }

    private void DesbloquearInternet() {
        String comand = "reg add \"HKEY_CURRENT_USER\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\Connections\" "
                + "/v DefaultConnectionSettings /t REG_BINARY /d "
                + "46000009000000090000000a000000302e302e302e303a3830000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000 /f";
        executarCmd(comand);
    }

    private void executarCmd(String comando) {
        try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", comando);
            builder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
