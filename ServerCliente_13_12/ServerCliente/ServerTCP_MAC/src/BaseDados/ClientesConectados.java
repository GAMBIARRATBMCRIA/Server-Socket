/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BaseDados;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

/**
 *
 * @author LAB01
 */
public class ClientesConectados extends infomacaoMaquinas {

    private Socket clientesocket;
    private BufferedReader entrada;
    private BufferedWriter saida;
    private PrintWriter saidaPrint;
    private Thread threandCliente;
    private Boolean receberComando;
    private JTextArea textoArea;
    private JProgressBar barraProgresso;
    private String statusCliente;

    public Socket getClientesocket() {
        return clientesocket;
    }

    public void setClientesocket(Socket clientesocket) {
        this.clientesocket = clientesocket;
    }

    public BufferedReader getEntrada() {
        return entrada;
    }

    public void setEntrada(BufferedReader entrada) {
        this.entrada = entrada;
    }

    public BufferedWriter getSaida() {
        return saida;
    }

    public void setSaidaBuffer(BufferedWriter saida) {
        this.saida = saida;
    }

    public PrintWriter getSaidaPrint() {
        return saidaPrint;
    }

    public void setSaidaPrint(PrintWriter saidaPrint) {
        this.saidaPrint = saidaPrint;
    }

    public Thread getThreandCliente() {
        return threandCliente;
    }

    public void setThreandCliente(Thread threandCliente) {
        this.threandCliente = threandCliente;
    }

    public Boolean getReceberComando() {
        return receberComando;
    }

    public void setReceberComando(Boolean receberComando) {
        this.receberComando = receberComando;
    }

    public String getNomeCliete() {  
        return getSetor()+"_"+getBancada()+"_"+getPosicao();
    }

    public JTextArea getTextoArea() {
        return textoArea;
    }

    public void setSaida(BufferedWriter saida) {
        this.saida = saida;
    }

    public void setTextoArea(JTextArea textoArea) {
        this.textoArea = textoArea;
    }

    public JProgressBar getBarraProgresso() {
        return barraProgresso;
    }

    public void setBarraProgresso(JProgressBar barraProgresso) {
        this.barraProgresso = barraProgresso;
    }

    public String getStatusCliente() {
        return statusCliente;
    }

    public void setStatusCliente(String statusCliente) {
        this.statusCliente = statusCliente;
    }
    
}
