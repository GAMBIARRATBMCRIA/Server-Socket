/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controleCliente;

import Servicos.AtualizarTable;
import Telas.Saida;

import java.io.IOException;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import linguagem.textoProjeto;

/**
 *
 * @author WESLLEY
 */
public class GereciadorClientes extends Thread {

    private Socket socket;
    private String codigoCliente;
    private PrintWriter saida;
    private Scanner entrada;
    private String perfil;
    private String ip;
    private String bancada;
    private String pc;
    private String mac;
    private String grupo;
    
    public static final Map<String, GereciadorClientes> mapClientesCenected = new HashMap<String, GereciadorClientes>();

    public GereciadorClientes(Socket socket) {
        this.socket = socket;
        start();
    }

    /**
     *
     */
    @Override
    public void run() {
        
        try {
            entrada = new Scanner(socket.getInputStream());
            saida = new PrintWriter(socket.getOutputStream(), true);
            
            saida.println("12345678");
            
            String msg = entrada.nextLine();

            this.codigoCliente = msg.substring(6);

            mac = entrada.nextLine().substring(9);

            perfil = msg.substring(6, 13);
            bancada = msg.substring(14, 17);
            pc = msg.substring(18);
            grupo = "LABORATÓRIO 01";
            
            ip = socket.getInetAddress().getHostAddress();
            mapClientesCenected.put(this.codigoCliente, this);
            
            AtualizarTable.atualizaTable();

            while (socket.isConnected()) {
                System.out.println("Mensagem do cliente:"+this.getCodigoCliente()+"-----------------------------");
                msg = entrada.nextLine();
                
                
                if (msg.equalsIgnoreCase("ok")) {
                    Saida.areaSaida02.append(this.getCodigoCliente()+":"+msg);
                }
                Saida.areaSaida02.append("\n"+this.getCodigoCliente()+":"+msg);
            }
            
            
        } catch (Exception e) {
            
            if (e.getMessage().equals("No line found")) {
                Telas.Tela_Principal.areaSaida.append("\nCliente desconectado:"+this.codigoCliente);
            } else {
                Telas.Tela_Principal.areaSaida.append("GerenciadorCliente 02:"+this.getCodigoCliente()+":"+textoProjeto.mensagemErro + e.getMessage());
            }
            
            try {                
                socket.close();
                saida.close();
                entrada.close();
                mapClientesCenected.remove(this.codigoCliente);
                AtualizarTable.atualizaTable();
                this.interrupt();
            } catch (IOException ex) {
                Telas.Tela_Principal.areaSaida.append("GernciadorCliente 03:"+this.getCodigoCliente()+":"+textoProjeto.mensagemErro + ex.getMessage());
            }
        }

    }

    public String getCodigoCliente() {
        return codigoCliente;
    }

    public String getPerfil() {
        return perfil;
    }

    public String getIp() {
        return ip;
    }

    public String getBancada() {
        return bancada;
    }

    public String getPc() {
        return pc;
    }

    public static Map<String, GereciadorClientes> getMapClientesCenected() {
        return mapClientesCenected;
    }

    public PrintWriter getSaida() {
        return saida;
    }

    public Scanner getLeitura() {
        return entrada;
    }
 
   public String getMac() {
        return mac;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public Socket getSocket() {
        return socket;
    }

    public Scanner getEntrada() {
        return entrada;
    }

}
