

import java.io.IOException;

/**
 * Ponto de entrada do servidor de sincronização de arquivos.
 *
 * <p>Uso: {@code java -jar servidor_sinc.jar}
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("[BOOT] Iniciando servidor_assistente_prova...");

        ServidorSinc servidor;
        try {
            servidor = new ServidorSinc();
        } catch (IOException e) {
            System.err.println("[BOOT] Falha ao inicializar servidor: " + e.getMessage());
            System.exit(1);
            return;
        }

        // Encerramento limpo ao pressionar Ctrl+C ou ao terminar o processo
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[BOOT] Sinal de encerramento recebido.");
            servidor.parar();
        }, "Shutdown-Hook"));

        try {
            servidor.iniciar(); // bloqueia até parar() ser chamado
        } catch (IOException e) {
            System.err.println("[BOOT] Erro fatal: " + e.getMessage());
            System.exit(1);
        }
    }
}

