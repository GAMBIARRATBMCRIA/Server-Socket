/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package linguagem;

/**
 *
 * @author WESLLEY
 */
public class inserirLinguagemPtBr {

    public void inserirPtBr() {
        ///////////////   aplicando texto de mensagens
        this.inserirMensagem();
        ///////////////   aplicando texto dos labels
        this.inserirLabel();
        //////////////    aplicando texto doas botoes
        this.inserirBotoes();

    }

    public void inserirMensagem() {
        textoProjeto.mensagemErro = "\nOcorreu um erro durante a execução do processo!";
        textoProjeto.mensagemSucesso = "\nOperação efetuada com Sucesso";
        textoProjeto.mensagemServidorIniciadoSucesso = "\nServidor Iniciado com Sucesso.\nAguardando Conexão dos Agentes...";
        textoProjeto.mensagemServidorIniciadoErro = "\nErro ao iniciar o Servidor ";
        textoProjeto.mensagemServidorDesligado = "\nSevidor Desligado";
    }

    public void inserirLabel() {
        //opçoes painel 1
        textoProjeto.label1Grupo = "Opções de Grupo";
        textoProjeto.label1Maquina = "Opções de Máquinas";
        textoProjeto.label1Server = "Opções do Servidor";
        textoProjeto.label1Porta = "Porta: ";
        // painel 01
        ////Servidor
        textoProjeto.label1StatusServer = "Ligado";
        textoProjeto.label1StatusServerDesligado = "Desligado";

        //painel 02 maquinas
        textoProjeto.label2TituloPainel = "Ações";
        textoProjeto.label2MaquinaSelecionada = "Máquina";
        //painel 03 saida
        textoProjeto.label4TituloPainel = "Maquinas na Rede";
        //painel 04 tabelas
        textoProjeto.painelTitulo = "Saída";
    }

    public void inserirBotoes() {
        //painel 01
        textoProjeto.btn1NovoGrupo = "Novo Grupo";
        textoProjeto.btn1ExcluirGrupo = "Excluir Grupo";

        textoProjeto.btn1NovaMaquina = "Nova Máquina";
        textoProjeto.btn1ExcluirMaquina = "Excluir Máquina";

        textoProjeto.btn1LigarServer = "Ligar Servidor";
        textoProjeto.btn1DesligarServer = "Desligar Server";
       textoProjeto.btn1VerificarConexoes = "Verificar Conexões";

        //Painel 02
        textoProjeto.btn2Desligar = "Desligar";
        textoProjeto.btn2DesligarCancelar = "Cancelar Desligamento";
        textoProjeto.btn2Reiniciar = "Reiniciar";
        textoProjeto.btn2ReiniciarCancelar = "Cancelar Reinicio";
        textoProjeto.btn2BloquarTela = "Bloquear Tela";
        textoProjeto.btn2DesbloquearTela = "Desbloquear Tela";
        
    }

}
