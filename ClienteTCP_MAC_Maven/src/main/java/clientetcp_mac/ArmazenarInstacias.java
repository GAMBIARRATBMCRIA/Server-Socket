/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientetcp_mac;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author LAB01
 */
public class ArmazenarInstacias {

    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    private String macLocal;
    private String camnhoGravacao;
    private String tipodado;

    public ArmazenarInstacias() {
    }

    public ArmazenarInstacias(BufferedReader in, PrintWriter out, Socket socket, String macLocal, String caminhoUser) {
        this.in = in;
        this.out = out;
        this.socket = socket;
        this.macLocal = macLocal;
        camnhoGravacao = caminhoUser;
        this.tipodado = "tipodadotexto";
    }

    public synchronized BufferedReader getIn() {
        return in;
    }

    public synchronized PrintWriter getOut() {
        return out;
    }

    public synchronized Socket getSocket() {
        return socket;
    }

    public String getMacLocal() {
        return macLocal;
    }

    public String getCamnhoGravacao() {
        return camnhoGravacao;
    }

    public void setCamnhoGravacao(String camnhoGravacao) {
        this.camnhoGravacao = camnhoGravacao;
    }

    public String getTipodado() {
        return tipodado;
    }

    public void setTipodado(String tipodado) {
        this.tipodado = tipodado;
    }

}
