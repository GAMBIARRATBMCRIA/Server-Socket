/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inicio;

/**
 *
 * @author LAB01
 */
public class inicio {
    public static void main(String[] args) {

        System.out.println("paramentros recebidos");
        tela tela = new tela();
        String username = "", password = "", link = "";
        System.out.println("ta;" + args.length);
        if (args.length > 1) {
            username = args[0];
            password = args[1];
            
            if (args.length > 2) {
                link = args[3];
                tela.link.setText(link);
            }
            
            tela.user.setText(username);
            tela.senha.setText(password);
            tela.texto.append("Recebido de parâmetros externo");
            tela.ir = true;
        }else{
            tela.texto.append("Inserir o Login e senha do SIGAA");
        }
        tela.setVisible(true);

        System.out.println("login:" + tela.user.getText());
        System.out.println("senha:" + tela.senha.getText());

        if (tela.ir) {
            execucao exe = new execucao();
            //exe.executar(tela.user.getText(), tela.senha.getText());
            exe.execute();
        }

    }

}
