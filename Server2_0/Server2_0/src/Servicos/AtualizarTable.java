/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servicos;

import Telas.Tela_Principal;
import controleCliente.GereciadorClientes;
import static controleCliente.GereciadorClientes.mapClientesCenected;

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author LAB_01
 */
public class AtualizarTable extends Thread {

    public static void atualizaTable() {
        int quantidade = 0;
        DefaultTableModel modelo = (DefaultTableModel) Tela_Principal.tabelaMaquinas.getModel();
        DefaultTableModel modelGroup = (DefaultTableModel) Tela_Principal.tableGrupo.getModel();
        modelo.setNumRows(0);
        for (GereciadorClientes cli : mapClientesCenected.values()) {
            quantidade++;
            modelo.addRow(new Object[]{ ///Computador
                //Nome
                cli.getCodigoCliente(),
                ///Status
                cli.getMac().toString(),
                ///Ip
                cli.getIp(),
                ///Grupo     
                "LABORATÓRIO 01",
                ///Bancada,
                cli.getBancada(),
                ///numero do pc
                cli.getPc()
            });
        }
        Tela_Principal.qtdMaquinas.setText("" + quantidade);
        Boolean contido;
        modelGroup.setNumRows(0);
        int colunaGrupo = 3;
        for (int i = 0; i < modelo.getRowCount(); i++) {
            contido = false;
            
            for (int j = 0; j < Tela_Principal.tableGrupo.getModel().getRowCount(); j++) {
                if (Tela_Principal.tableGrupo.getValueAt(j, 0).toString().equalsIgnoreCase(Tela_Principal.tabelaMaquinas.getValueAt(i, colunaGrupo).toString())) {
                    contido = true;
                }
            }
            
            if (contido == false) {
                 modelGroup.addRow(new Object[]{
                     Tela_Principal.tabelaMaquinas.getValueAt(i, colunaGrupo)
                 });
            }
           
        }
    }

}
