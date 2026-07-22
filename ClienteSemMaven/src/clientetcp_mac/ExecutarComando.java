package clientetcp_mac;

import Global.InstanciasGlobais;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import clientetcp_mac.modulos.IModulo;
import clientetcp_mac.modulos.ModuloRegistry;

/**
 *
 * @author LAB01
 */
public class ExecutarComando extends Thread {

    private String mensagemRecebida;
    private ArmazenarInstacias instancias;
    private ArrayList<String> textoRecebido;

    public ExecutarComando() {
    }

    public ExecutarComando(String mensagem, ArmazenarInstacias instacias, ArrayList<String> listaLinhas) {
        mensagemRecebida = mensagem;
        this.instancias = instacias;
        textoRecebido = new ArrayList<>(listaLinhas);
        start();
    }

    @Override
    public void run() {
        //controle executando comando
        InstanciasGlobais.setExecutandoComando(true);

        int confirmar = JOptionPane.OK_OPTION;
        System.out.println("mensagem recebida:" + mensagemRecebida);

        if (mensagemRecebida.startsWith("aceitar:")) {
            confirmar = JOptionPane.showConfirmDialog(null, "Um comando remoto precisa de permissão pra executar.\nPermitir execução do comando?",
                    "Administrador Remoto", JOptionPane.OK_CANCEL_OPTION);
            mensagemRecebida = mensagemRecebida.replaceAll("aceitar:", "");
        }

        if (confirmar == JOptionPane.OK_OPTION) {

            for (IModulo modulo : ModuloRegistry.getInstance().getModulos()) {
                if (modulo.podeProcessar(mensagemRecebida)) {
                    modulo.executar(mensagemRecebida, instancias, textoRecebido);
                    this.interrupt();
                    InstanciasGlobais.setExecutandoComando(false);
                    return;
                }
            }

            System.out.println("Paramentro nao reconhecido:" + mensagemRecebida);
            instancias.getOut().println("Parâmetro não reconhecido");
            instancias.getOut().flush();

        } else {
            instancias.getOut().println("Cliente recusou a execução do comando!");
            instancias.getOut().flush();
        }
        this.interrupt();

        //controle executando comando
        InstanciasGlobais.setExecutandoComando(false);

        System.out.println("interrompida:" + Thread.currentThread().isInterrupted());
    }
}
