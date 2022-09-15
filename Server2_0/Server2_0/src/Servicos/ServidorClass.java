/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servicos;

import controleCliente.GereciadorClientes;
import static controleCliente.GereciadorClientes.mapClientesCenected;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;

import java.util.Scanner;
import linguagem.textoProjeto;

/**
 *
 * @author WESLLEY
 */
public class ServidorClass extends Thread {

    private static ServerSocket serverSocket;


    public static ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void criaServerSocket(int porta) throws IOException {
        serverSocket = new ServerSocket(porta);
    }

    public void DesligaServer() throws IOException {
        for (GereciadorClientes cli : mapClientesCenected.values()) {
            cli.getSaida().close();
            cli.getEntrada().close();
            cli.getSocket().close();
        }
        serverSocket.close();

    }

    public String ipLocal() {
        String ip = textoProjeto.mensagemErro;
        InetAddress dadosLocais = serverSocket.getInetAddress();
        ip = dadosLocais.getHostAddress();

        return ip;
    }

    public String hostLocal() {
        String hostLocal = textoProjeto.mensagemErro;
        InetAddress dadosLocais = serverSocket.getInetAddress();
        hostLocal = dadosLocais.getHostName();
        hostLocal = System.getProperty("user.name");
        return hostLocal;
    }

    public static void enviarComando(String mensagem, OutputStream outputStream) {

        PrintStream saida = new PrintStream(outputStream);
        saida.println(mensagem);

    }

    public void fechaStream(PrintStream saida, Scanner leituraScanner) {
        saida.flush();
        saida.close();
        leituraScanner.close();
    }

}
