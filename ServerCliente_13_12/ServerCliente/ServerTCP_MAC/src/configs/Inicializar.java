/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package configs;

import Iniciar.TelaInicial;
import Servicos.Instancias;

/**
 *
 * @author LAB01
 */
public class Inicializar {

    public static void main(String[] args) {
        Inicializar ini = new Inicializar();
        ini.carregar();
    }

    public void carregar() {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(Inicializar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        Servicos.Instancias instacias = new Instancias();
        Servicos.Instancias.getControlebaseIntancia().importarDados();
        TelaInicial tela = new TelaInicial();
        tela.setVisible(true);

//        painel p = new painel();
//        p.setVisible(true);
        //Servicos.Instancias.getExecucaoAtividadesInstacia().lerTxt(configs.configuracao.localBase);
    }
}
