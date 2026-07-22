/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package startercliente;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author LAB01
 */
public class atualizar {

    public Boolean atualizar() {
        Boolean atualizadoSucess = false;
        try {
            Thread.sleep(5000);
            //aguardar um tempo para garantir que deu tempo fechar
        } catch (InterruptedException ex) {
            Logger.getLogger(atualizar.class.getName()).log(Level.SEVERE, null, ex);
        }
        String diretorioProjeto = configs.configuracoes.diretorioProjeto;
        System.out.println("procurando em:"+diretorioProjeto);
       
        File diretorio = new File(diretorioProjeto);

        if (diretorio.exists() && diretorio.isDirectory()) {
          
            if (apagarCliente(diretorio) && renomearCliente2(diretorio)) {
                System.out.println("Operações concluídas com sucesso.");
                atualizadoSucess = true;
            }
            
        } else {
            System.err.println("Diretório especificado não existe ou não é um diretório válido.");
        }
        
        return atualizadoSucess;
    }

    private static Boolean apagarCliente(File diretorio) {
         Boolean apagarSucess = false;
        File[] files = diretorio.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().equals("Cliente.exe")) {
                    System.out.println("Cliente.exe encontrado");
                    // Apaga o arquivo "Cliente.exe"
                    file.delete();
                    System.out.println("Cliente.exe deletado");
                    apagarSucess = true;
                    break; // Encerra o loop após encontrar e apagar o arquivo
                }
            }
        }
        return apagarSucess;
    }

    private static boolean renomearCliente2(File diretorio) {
         Boolean renameSucess = false;
        File[] files = diretorio.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().equals("Cliente2.exe")) {
                    // Renomeia "Cliente2.exe" para "Cliente.exe"
                    File novoNome = new File(file.getParentFile(), "Cliente.exe");
                    
                    if (!file.renameTo(novoNome)) {
                        System.err.println("Falha ao renomear o arquivo: " + file.getName());
                    }else{
                        renameSucess = true;
                    }
                   
                    break; // Encerra o loop após encontrar e renomear o arquivo
                }
            }
        }
        return renameSucess;
    }

}
