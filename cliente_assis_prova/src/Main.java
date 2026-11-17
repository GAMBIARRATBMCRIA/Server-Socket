

/**
 * Ponto de entrada do cliente de sincronização de arquivos.
 *
 * <p>Uso: {@code java -jar cliente_sinc.jar}
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("[BOOT] Iniciando cliente_assistente_prova...");

        ClienteSinc cliente = new ClienteSinc("");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[BOOT] Encerrando cliente...");
            cliente.parar();
        }, "Shutdown-Hook"));

        cliente.iniciar(); //bloqueia indefinidamente
    }
}

