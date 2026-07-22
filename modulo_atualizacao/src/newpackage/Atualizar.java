/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newpackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author LAB_01
 */
public class Atualizar {

    private static String PID_Starter = "";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            PID_Starter = args[0];
        }
        System.out.println("args:" + args.length);
        Atualizar att = new Atualizar();
        att.atualizar();

    }

    void atualizar() {
        System.out.println("iniciando...");
        if (verificarArquivoCliente("\\atualizacao\\Cliente.exe")) {
            System.out.println("parando starter");
//parar_starter();
            System.out.println("deletando atual");
            apagar_atual();
            System.out.println("copiando novo");
            copiar_novo();
            System.out.println("executando starter");
            executarCMD(System.getProperty("user.dir") + "\\starterCliente.exe");
        }
        System.out.println("finalizando");
        System.exit(0);
    }

    void parar_starter() {
        executarCMD("taskkill /f /pid " + PID_Starter);
    }

    public void apagar_atual() {
        File clienteatual = new File(System.getProperty("user.dir"), "Cliente.exe");
        clienteatual.delete();

        while (verificarArquivoCliente("Cliente.exe") != false) {
            clienteatual.delete();
        }
    }

    void copiar_novo() {
        String cmd = "robocopy " + System.getProperty("user.dir") + "\\atualizacao\\ " + System.getProperty("user.dir") + " /S /E /W:5 /mov";
        executarCMD(cmd);
    }

    public static boolean verificarArquivoCliente(String caminhoArquivo) {
        // Diretório local onde o Cliente2.exe está localizado
        File diretorioLocal = new File(System.getProperty("user.dir"), caminhoArquivo);

        System.out.println(diretorioLocal.getAbsolutePath());

        return diretorioLocal.exists();
    }

    void executarCMD(String cmd) {
        System.out.println("comando:" + cmd);
        ProcessBuilder comando = new ProcessBuilder("cmd.exe", "/c", cmd);
        Process processo = null;
        try {
            processo = comando.start();
            BufferedReader ler = new BufferedReader(new InputStreamReader(processo.getInputStream()));
            String linha = "";

            while ((linha = ler.readLine()) != null) {
                System.out.println("linha:" + linha);
            }
        } catch (IOException ex) {
            System.out.println("erro:" + ex.getMessage());
        }
        //System.out.println("finalizado");
        processo.destroy();
    }
}
