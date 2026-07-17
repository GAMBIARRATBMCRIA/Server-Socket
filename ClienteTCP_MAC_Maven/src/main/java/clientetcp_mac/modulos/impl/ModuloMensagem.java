package clientetcp_mac.modulos.impl;

import Global.InstanciasGlobais;
import clientetcp_mac.ArmazenarInstacias;
import clientetcp_mac.modulos.IModulo;
import java.util.ArrayList;
import javax.swing.JTextArea;

public class ModuloMensagem implements IModulo {

    @Override
    public boolean podeProcessar(String comando) {
        return comando.contains("msgcliente:");
    }

    @Override
    public void executar(String comando, ArmazenarInstacias instancias, ArrayList<String> textoRecebido) {
        String mensagemLimpa = comando.replaceAll("msgcliente:", "");
        exibirMensagem(textoRecebido, mensagemLimpa);
    }

    private void exibirMensagem(ArrayList<String> textoRecebidoarray, String mensagemRecebida) {
        JTextArea area = new JTextArea();
        textoRecebidoarray.set(0, mensagemRecebida);
        for (int i = 0; i < textoRecebidoarray.size(); i++) {
            area.append(textoRecebidoarray.get(i) + "\n");
        }
        InstanciasGlobais.getMensagemConversaTelaInstacia().exibirMensagem(area);
    }
}
