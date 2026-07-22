package clientetcp_mac.modulos;

import java.util.ArrayList;
import java.util.List;
import clientetcp_mac.modulos.impl.ModuloCmd;
import clientetcp_mac.modulos.impl.ModuloTela;
import clientetcp_mac.modulos.impl.ModuloValidacao;
import clientetcp_mac.modulos.impl.ModuloInternet;
import clientetcp_mac.modulos.impl.ModuloServicos;
import clientetcp_mac.modulos.impl.ModuloSistema;
import clientetcp_mac.modulos.impl.ModuloMensagem;
import clientetcp_mac.modulos.impl.ModuloRemoteDesktop;

public class ModuloRegistry {
    private static final ModuloRegistry instance = new ModuloRegistry();
    private List<IModulo> modulos = new ArrayList<>();

    private ModuloRegistry() {
        registrarModulo(new ModuloCmd());
        registrarModulo(new ModuloInternet());
        registrarModulo(new ModuloServicos());
        registrarModulo(new ModuloSistema());
        registrarModulo(new ModuloRemoteDesktop());
        registrarModulo(new ModuloMensagem());
        registrarModulo(new ModuloValidacao());
        registrarModulo(new ModuloTela());
    }

    public static ModuloRegistry getInstance() {
        return instance;
    }

    public void registrarModulo(IModulo modulo) {
        modulos.add(modulo);
    }

    public List<IModulo> getModulos() {
        return modulos;
    }
}
