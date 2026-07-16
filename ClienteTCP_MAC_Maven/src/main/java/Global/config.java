/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Global;

import java.awt.Image;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 *
 * @author Weslley
 */
public class config {

    public static int portaServidor = 12346;//12346
    public static String enderecoSevidor = "ADM";
    public static String versao_cliente = "4";

    public static String paramentroMensagem = "msg::";
    public static String paramentroComandoCmd = "comd::";
    public static String paramentroConfirmacaoDoClienteSim = "cfS::";
    public static String paramentroVerfificacao = "vfc::";

    public static String caminhoImagemIcon = "telaBloqueio.png";
    public static String caminhoImagemIconMenu = "imgIConBarra20v20.png";

    public static Image getImageIconMenu() {
        try {
            InputStream inputStream = config.class.getResourceAsStream("/imagens/"+caminhoImagemIconMenu);
            if (inputStream != null) {
                return ImageIO.read(inputStream); // Ler a imagem do InputStream
            } else {
                System.out.println("O recurso não pôde ser encontrado: " + "/imagens/"+caminhoImagemIconMenu);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace(); // Exibir o erro detalhado
            return null;
        }
    }

    public static Image getIconImage() {
         try {
            InputStream inputStream = config.class.getResourceAsStream("/imagens/"+caminhoImagemIcon);
            if (inputStream != null) {
                return ImageIO.read(inputStream); // Ler a imagem do InputStream
            } else {
                System.out.println("O recurso não pôde ser encontrado: " + "/imagens/"+caminhoImagemIcon);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace(); // Exibir o erro detalhado
            return null;
        }

    }

}
