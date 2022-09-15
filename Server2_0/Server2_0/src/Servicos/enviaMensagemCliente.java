/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servicos;

import ControleGlobal.configsControleGlobal;
import Telas.Saida;
import Telas.Tela_Principal;
import static Telas.Tela_Principal.areaSaida;
import controleCliente.GereciadorClientes;
import static controleCliente.GereciadorClientes.mapClientesCenected;
import javax.swing.JTextArea;

/**
 *
 * @author LAB_01
 */
public class enviaMensagemCliente {

    public void enviaMensagem(String parametro, String mensagem) {

        String paramatroEnviar = parametro;

        //////////////////////////////////////////////////////////////////////////////////////
        if (paramatroEnviar.equalsIgnoreCase(configsControleGlobal.paramentroComandoCmd)) {
            paramatroEnviar = "";
            if (Tela_Principal.permitir.isSelected()) {
                paramatroEnviar = configsControleGlobal.paramentroConfirmacaoDoClienteSim;
            }
        } else if (paramatroEnviar.equalsIgnoreCase(configsControleGlobal.paramentroMensagem)) {
            paramatroEnviar = configsControleGlobal.paramentroMensagem;
        } else if (paramatroEnviar.equalsIgnoreCase(configsControleGlobal.paramentroVerfificacao)) {
            paramatroEnviar = "";
        }

        System.out.println("Mesangem para enviar:" + mensagem);
        for (GereciadorClientes cli : mapClientesCenected.values()) {
            if (cli.getBancada().equals(Tela_Principal.maquinaSelect.getText())
                    || cli.getCodigoCliente().equals(Tela_Principal.maquinaSelect.getText())
                    || cli.getGrupo().equals(Tela_Principal.maquinaSelect.getText())) {

                cli.getSaida().println(paramatroEnviar + mensagem);
                cli.getSaida().flush();
                areaSaida.append("\nComando enviado para " + cli.getCodigoCliente());
                Saida.areaSaida02.setText("\nComando enviado para " + cli.getCodigoCliente());

            }
        }
        paramatroEnviar = "";

    }

    public void esceverMensagem(JTextArea areaDeTexto) {
        String formatacao = configsControleGlobal.paramentroMensagem + areaDeTexto.getText();
        System.out.println("Texto:" + formatacao);
        for (GereciadorClientes cli : mapClientesCenected.values()) {
            if (cli.getBancada().equals(Tela_Principal.maquinaSelect.getText())
                    || cli.getCodigoCliente().equals(Tela_Principal.maquinaSelect.getText())
                    || cli.getGrupo().equals(Tela_Principal.maquinaSelect.getText())) {

                cli.getSaida().println(formatacao);

                areaSaida.append("\nComando enviado para " + cli.getCodigoCliente());
                Saida.areaSaida02.setText("\nComando enviado para " + cli.getCodigoCliente());

            }
        }
    }
    public static void verficarCliente(){
      
        for (GereciadorClientes cli : mapClientesCenected.values()) {
                cli.getSaida().println(configsControleGlobal.paramentroVerfificacao);

                areaSaida.append("\nComando enviado para " + cli.getCodigoCliente());
                Saida.areaSaida02.setText("\nComando enviado para " + cli.getCodigoCliente());
      
        }
    }
}
