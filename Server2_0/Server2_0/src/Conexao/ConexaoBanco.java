/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Conexao;

import ControleGlobal.configsControleGlobal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author Jarod
 */
public class ConexaoBanco {

    public static Connection conectarbd() throws ClassNotFoundException {
        try {
            Class.forName("org.postgresql.Driver");
            Connection con = DriverManager.getConnection(configsControleGlobal.URL + configsControleGlobal.PORTA + "/" + configsControleGlobal.BANCO,
                    configsControleGlobal.USUARIO, configsControleGlobal.SENHA);
            return con;
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, erro);
            return null;
        }

    }
}
