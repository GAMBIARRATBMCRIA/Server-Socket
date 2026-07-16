/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Iniciar;

import Servicos.Instancias;



/**
 *
 * @author LAB01
 */
public class TelaInicial extends javax.swing.JFrame {

    /**
     * Creates new form NewJFrame
     */
    public TelaInicial() {
      // pteste = new painel();
        
        initComponents();
        this.setLocationRelativeTo(null);
        addPainel();
        
    }
    void addPainel(){
        painel p = Instancias.getPainelStatic();
           
        javax.swing.GroupLayout ptesteLayout = new javax.swing.GroupLayout(p);
        //p.setLayout(ptesteLayout);
        ptesteLayout.setHorizontalGroup(
            ptesteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 892, Short.MAX_VALUE)
        );
        ptesteLayout.setVerticalGroup(
            ptesteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 572, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout painelPrLayout = new javax.swing.GroupLayout(painelPr);
        
        painelPr.setLayout(painelPrLayout);
        painelPrLayout.setHorizontalGroup(
            painelPrLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelPrLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        painelPrLayout.setVerticalGroup(
            painelPrLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        painelPr = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        painelPr.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout painelPrLayout = new javax.swing.GroupLayout(painelPr);
        painelPr.setLayout(painelPrLayout);
        painelPrLayout.setHorizontalGroup(
            painelPrLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1484, Short.MAX_VALUE)
        );
        painelPrLayout.setVerticalGroup(
            painelPrLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 713, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelPr, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(painelPr, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel painelPr;
    // End of variables declaration//GEN-END:variables
}
