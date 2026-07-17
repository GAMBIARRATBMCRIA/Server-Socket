package clientetcp_mac.modulos.impl;

import clientetcp_mac.ArmazenarInstacias;
import clientetcp_mac.modulos.IModulo;
import java.util.ArrayList;

public class ModuloValidacao implements IModulo {

    @Override
    public boolean podeProcessar(String comando) {
        return comando.contains("validacao") || comando.contains("autenticado");
    }

    @Override
    public void executar(String comando, ArmazenarInstacias instancias, ArrayList<String> textoRecebido) {
        if (comando.contains("validacao")) {
            enviarMensagem(instancias.getMacLocal(), instancias);
        } else if (comando.contains("autenticado")) {
            System.out.println("Autenticação efetuada");
        }
    }
    
    private void enviarMensagem(String mensagem, ArmazenarInstacias instancias) {
        instancias.getOut().println(mensagem);
        instancias.getOut().flush();
    }
}
