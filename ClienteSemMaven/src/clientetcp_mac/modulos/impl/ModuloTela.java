package clientetcp_mac.modulos.impl;

import Global.InstanciasGlobais;
import clientetcp_mac.ArmazenarInstacias;
import clientetcp_mac.modulos.IModulo;
import java.util.ArrayList;

public class ModuloTela implements IModulo {

    @Override
    public boolean podeProcessar(String comando) {
        return comando.equalsIgnoreCase("pr:bloqueioTela") || comando.equalsIgnoreCase("pr:desbloqueioTela");
    }

    @Override
    public void executar(String comando, ArmazenarInstacias instancias, ArrayList<String> textoRecebido) {
        if (comando.equalsIgnoreCase("pr:bloqueioTela")) {
            bloqueioTela(true);
        } else if (comando.equalsIgnoreCase("pr:desbloqueioTela")) {
            bloqueioTela(false);
        }
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
}
