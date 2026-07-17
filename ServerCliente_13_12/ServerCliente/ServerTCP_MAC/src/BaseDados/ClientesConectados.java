/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BaseDados;

import Iniciar.PainelVideoIndividual;
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
    private Socket clienteSoketVideo;
    
    private BufferedReader entrada;
    private BufferedWriter saida;
    private PrintWriter saidaPrint;
    private Thread threandCliente;
    private Boolean receberComando;
    private JTextArea textoArea;
    private JProgressBar barraProgresso;
    private String statusCliente;
    
    private PainelVideoIndividual painelVideo;
    private Boolean transmissaoAtiva;

    public ClientesConectados() {
        painelVideo = new PainelVideoIndividual();
        transmissaoAtiva = false;
    }
    
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

    public PainelVideoIndividual getPainelVideo() {
        return painelVideo;
    }

    public void setPainelVideo(PainelVideoIndividual painelVideo) {
        this.painelVideo = painelVideo;
    }

    public Socket getClienteSoketVideo() {
        return clienteSoketVideo;
    }

    public void setClienteSoketVideo(Socket clienteSoketVideo) {
        this.clienteSoketVideo = clienteSoketVideo;
    }

    public Boolean getTransmissaoAtiva() {
        return transmissaoAtiva;
    }

    public void setTransmissaoAtiva(Boolean transmissaoAtiva) {
        this.transmissaoAtiva = transmissaoAtiva;
    }

    
}
