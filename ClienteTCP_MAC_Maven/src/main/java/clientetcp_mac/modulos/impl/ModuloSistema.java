package clientetcp_mac.modulos.impl;

import Global.InstanciasGlobais;
import clientetcp_mac.ArmazenarInstacias;
import clientetcp_mac.modulos.IModulo;
import java.io.File;
import java.util.ArrayList;

public class ModuloSistema implements IModulo {

    @Override
    public boolean podeProcessar(String comando) {
        return comando.contains("altcaminho:") || comando.contains("atualizar:") || comando.contains("desconectar:");
    }

    @Override
    public void executar(String comando, ArmazenarInstacias instancias, ArrayList<String> textoRecebido) {
        if (comando.contains("altcaminho:")) {
            String caminho = comando.replaceAll("altcaminho:", "");
            if (caminho.startsWith("//att//")) {
                File diretorioAtualizacao = new File(System.getProperty("user.dir"), "atualizacao");
                if (!diretorioAtualizacao.exists()) {
                    diretorioAtualizacao.mkdirs();
                }
                caminho = diretorioAtualizacao.getAbsolutePath() + "\\";
                instancias.setCamnhoGravacao(caminho);
                enviarMensagem("caminho de armazenenamento alterado para:" + instancias.getCamnhoGravacao(), instancias);
            } else {
                
                if (verifica_diretorio(caminho, instancias)) {
                    
                    instancias.setCamnhoGravacao(caminho);
                    enviarMensagem("caminho de armazenenamento alterado para:" + instancias.getCamnhoGravacao(), instancias);
                } else {
                    enviarMensagem("caminho não é válido!", instancias);
                }
            }
        } else if (comando.contains("atualizar:")) {
            enviarMensagem("Atualização será instalada", instancias);
            atualizar(instancias);
        } else if (comando.contains("desconectar:")) {
            enviarMensagem("O cliente será encerrado!", instancias);
            desligar(instancias);
        }
    }

    private boolean verifica_diretorio(String caminho, ArmazenarInstacias instancias) {
        File dir = new File(caminho);
        enviarMensagem(dir.getAbsolutePath(), instancias);
        return dir.isDirectory();
    }

    private void atualizar(ArmazenarInstacias instancias) {
        if (verificarArquivoCliente2()) {
            enviarMensagem("Atualizar Cliente requerido. Cliente.exe econtrado!", instancias);
            InstanciasGlobais.setExecutandoComando(false);
            System.out.println("codigodeatualizacao:12345");
            System.exit(33);
        } else {
            enviarMensagem("Atualizacao cancelada, Cliente.exe não econtrado!", instancias);
        }
    }

    private boolean verificarArquivoCliente2() {
        File diretorioLocal = new File(System.getProperty("user.dir"));
        File arquivoCliente2 = new File(diretorioLocal, "\\atualizacao\\Cliente.exe");
        return arquivoCliente2.exists();
    }

    private void desligar(ArmazenarInstacias instancias) {
        System.out.println("Desligar Cliente");
        try {
            instancias.getOut().flush();
            instancias.getOut().close();
            instancias.getIn().close();
            instancias.getSocket().shutdownInput();
            instancias.getSocket().shutdownOutput();
            instancias.getSocket().close();
        } catch (Exception ex) {
            System.out.println("Erro ao desconectar" + ex.getMessage());
        }
        InstanciasGlobais.setExecutandoComando(false);
        System.exit(1);
    }

    private void enviarMensagem(String mensagem, ArmazenarInstacias instancias) {
        instancias.getOut().println(mensagem);
        instancias.getOut().flush();
    }
}
