/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import ControleGlobal.configsControleGlobal;
import Servicos.bandejaSistemaServer;
import Telas.Tela_Principal;
import java.awt.SystemTray;

import linguagem.inserirLinguagemPtBr;

/**
 *
 * @author  WESLLEY
 */
public class IniciarSistema {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if (configsControleGlobal.temaProjeto.equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Tela_Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Tela_Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Tela_Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Tela_Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
       
        ///////////////////// aplicando linguagem 
        System.out.println("Iserindo");
        inserirLinguagemPtBr inserirLinguage = new inserirLinguagemPtBr();
        inserirLinguage.inserirPtBr();
        
        System.out.println("chamando Tela principal");
        
        ///Chamando a tela inicial
        Tela_Principal tela = new Tela_Principal();
        //tela.setIconImage(configsControleGlobal.getIconImage());
        tela.setVisible(true);
        new bandejaSistemaServer().abriBandeja(tela);
    }

}
