/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package configs;
public class comandosServerCliente {

    public static void inserirComando(String mensagem) {
        Iniciar.painel.textAreaEnviar.setText(mensagem);
    }

    public static void desligarPc() {
        inserirComando("cmd:" + "shutdown -s -f -t 00");
    }

    public static void cacelarDesligarPc() {
        inserirComando("cmd:" + "shutdown /a");
    }

    public static void ReiniciarPc() {
        inserirComando("cmd:" + "shutdown -r -f -t 00");
    }

    public static void cancelarReiniciarPc() {
        inserirComando("cmd:" + "shutdown /a");
    }

    public static void bloquearTela() {
        inserirComando("pr:" + "bloqueioTela");
    }

    public static void DesbloquearTela() {
        inserirComando("pr:" + "desbloqueioTela");
    }

    public static void bloquearUser() {
        inserirComando("cmd:" + "rundll32.exe user32.dll,LockWorkStation");
    }

    public static void pesquisarWeb(String link) {
        inserirComando("cmd:" + "\"C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe\" " + link);
    }

    public static void bloquearInternet() {
        inserirComando("pr:BloquearInternet");
    }

    public static void DesbloquearInternet() {
        inserirComando("pr:" + "Desbloquearinternet");
    }
    public static String IniciarTranmissaoVideo() {
        String comando = "StartRemoteDesktop";
        inserirComando("pr:" + "StartRemoteDesktop");
        return comando;
        
    }
    public static String EncerrarTranmissaoVideo() {
        String comando = "StopRemoteDesktop";
        inserirComando("pr:" + "StopRemoteDesktop");
        return comando;
    }

    public static void AutenticadorRede(String login, String senha) {
        if (login.isEmpty()) {
            inserirComando("cmd:" + "java -jar \"z:/modulo_autenticador.jar\"");
        } else {
            inserirComando("cmd:" + "java -jar \"z:/modulo_autenticador.jar\" " + login + " " + senha);
        }
    }
    public static void setCaminhoAtualizacao(){
        inserirComando("altcaminho:z:/atualizacao/");
    }
    public static void fecharExplorer(){
        inserirComando("cmd:"+"taskkill /f /im explorer.exe");
    }
    public static void AbrirExplorer(){
        inserirComando("cmd:"+"start explorer.exe");
    }
    public static void setDesligarUSb(){
        inserirComando("cmd:"+"reg add HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Services\\USBSTOR /v Start /t REG_DWORD /d 4 /f & taskkill /f /im explorer.exe & start explorer.exe");
    }
    public static void setLigarUSb(){
        inserirComando("cmd:"+"reg add HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Services\\USBSTOR /v Start /t REG_DWORD /d 3 /f & taskkill /f /im explorer.exe & start explorer.exe");
    }
    public static void LimparPc(){

    }
    
}
