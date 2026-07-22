/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Global;

import clientetcp_mac.ArmazenarInstacias;
import clientetcp_mac.bandejaSistema;
import java.util.logging.Logger;
import telas.MensagemConversa;
import telas.bloqueioTela;
import telas.telaLog;

public class InstanciasGlobais {
    
    public static ArmazenarInstacias amazenarInstancias;
    public static String caminhoUsuario;
    private static bloqueioTela bloqueiotelaInstacia;
    private static bandejaSistema bandejaSistema;
    private static MensagemConversa mensagemConversaTelaInstacia;
    private static telaLog telaLogInstandia;
    public static Logger log;

    //variaveis globais
    private static boolean executandoComando = false;
    private static boolean recebendoArquivo = false;
    
    
    public InstanciasGlobais(ArmazenarInstacias instancias, bloqueioTela tela, MensagemConversa mensagemTela, telaLog telalog, Logger log) {
       amazenarInstancias = instancias;
       bloqueiotelaInstacia = tela;
       mensagemConversaTelaInstacia = mensagemTela;
       telaLogInstandia = telalog;
       this.log = log;
    }

    public InstanciasGlobais() {
//        telaLogInstandia = new telaLog();
//        log = Logger.getLogger(ClienteTCP_MAC.class.getName());
    }
    

    public synchronized static bloqueioTela getBloqueiotelaInstacia() {
        return bloqueiotelaInstacia;
    }
    
    public static ArmazenarInstacias getAmazenarInstancias() {
        return amazenarInstancias;
    }

    public static void setAmazenarInstancias(ArmazenarInstacias amazenarInstancias) {
        InstanciasGlobais.amazenarInstancias = amazenarInstancias;
    }

    public static bandejaSistema getBandejaSistema() {
        return bandejaSistema;
    }

    public static void setBandejaSistema(bandejaSistema bandejaSistema) {
        InstanciasGlobais.bandejaSistema = bandejaSistema;
    }
    
    public synchronized static boolean isExecutandoComando() {
        return executandoComando;
    }

    public synchronized static void setExecutandoComando(boolean executandoComando) {
        InstanciasGlobais.executandoComando = executandoComando;
    }

    public synchronized static boolean isRecebendoArquivo() {
        return recebendoArquivo;
    }

    public synchronized static void setRecebendoArquivo(boolean recebendoArquivo) {
        InstanciasGlobais.recebendoArquivo = recebendoArquivo;
    }

    public static MensagemConversa getMensagemConversaTelaInstacia() {
        return mensagemConversaTelaInstacia;
    }

    public static telaLog getTelaLogInstandia() {
        return telaLogInstandia;
    }

    public static void setTelaLogInstandia(telaLog telaLogInstandia) {
        InstanciasGlobais.telaLogInstandia = telaLogInstandia;
    }

    
}
