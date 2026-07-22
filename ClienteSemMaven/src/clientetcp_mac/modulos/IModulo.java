package clientetcp_mac.modulos;

import clientetcp_mac.ArmazenarInstacias;
import java.util.ArrayList;

public interface IModulo {
    boolean podeProcessar(String comando);
    void executar(String comando, ArmazenarInstacias instancias, ArrayList<String> textoRecebido);
}
