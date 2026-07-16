/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servicos;

import Servicos.*;
import BaseDados.*;
import ControleServerTCP.*;
import Iniciar.*;


public class Instancias extends Thread{

    private static ControleBase controlebaseIntancia;
    private static ExecucaoAtividadesServer execucaoAtividadesInstacia;
    private static Conexao conexaoIntancia;
    public static painel painel = new painel();
    private static CarregarTable carregarTableInstacia;
    
    private static clientesConectados clientesConctadosTela;
    public Instancias() {
        
        controlebaseIntancia = new ControleBase();
        execucaoAtividadesInstacia = new ExecucaoAtividadesServer();
        conexaoIntancia = new Conexao();
        painel = new painel();
        carregarTableInstacia  = new CarregarTable();
        
        clientesConctadosTela = new clientesConectados();
        clientesConectados.tablePanneDialogCliente.removeAll();
        
    }
    
    public static ControleBase getControlebaseIntancia() {
        return controlebaseIntancia;
    }

    public static ExecucaoAtividadesServer getExecucaoAtividadesInstacia() {
        return execucaoAtividadesInstacia;
    }

    public static Conexao getConexaoIntancia() {
        return conexaoIntancia;
    }

    public static CarregarTable getCarregarTableInstacia() {
        return carregarTableInstacia;
    }

    public static painel getPainelStatic() {
        return painel;
    }

    public static painel getPainel() {
        return painel;
    }

    public synchronized static clientesConectados getClientesConctadosTela() {
        return clientesConctadosTela;
    }

    
}
