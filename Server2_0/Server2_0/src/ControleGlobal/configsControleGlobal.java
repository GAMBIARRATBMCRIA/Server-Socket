/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ControleGlobal;

import java.awt.Image;
import java.awt.Toolkit;

/**
 *
 * @author WESLLEY
 */
public class configsControleGlobal {
    ///Server

    public static int portaServidor = 3325;
    public static String liguagem = "PtBr";
    //Formatação e template
    public static String fonteTexto = "Arial";
    public static int tamanhoFonte = 10;

    public static String temaProjeto = "Nimbus";

    ///Banco de dados
    public static final String URL = "jdbc:postgresql://localhost:";
    public static final String PORTA = "5432";
    public static final String BANCO = "SERAF";
    public static final String USUARIO = "postgres";
    public static final String SENHA = "qwe123";
    
    ///Paramentros
    public static String paramentroMensagem = "msg::";
    public static String paramentroComandoCmd = "comd::";
    public static String paramentroConfirmacaoDoClienteSim = "cfS::";
    public static String paramentroVerfificacao = "vfc::";
    public static String caminhoImagemIcon = "imagens/imgICoonBarra01.png";

    public static String caminhoImagemIconMenu = "imagens/imgIConBarra20v20.png";

    public static Image getImageIconMenu() {
        Image Icone = null;
        try {

            Icone = Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource(caminhoImagemIconMenu));

        } catch (Exception e) {
            System.out.println("Erro na imagem:" + e.getMessage());
        }
        return Icone;
    }

    public static Image getIconImage() {
        Image Icone = null;
        try {

            Icone = Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource(caminhoImagemIcon));

        } catch (Exception e) {
            System.out.println("Erro na imagem:" + e.getMessage());
        }
        return Icone;
    }
}
