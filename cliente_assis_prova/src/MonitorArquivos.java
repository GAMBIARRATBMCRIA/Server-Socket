

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Monitora recursivamente a pasta Documentos usando Java NIO WatchService.
 *
 * <p>Funcionalidades:
 * <ul>
 *   <li>Uso de {@code ExtendedWatchEventModifier.FILE_TREE} no Windows para monitoramento
 *       recursivo nativo de todas as subpastas instantaneamente em um único registro.</li>
 *   <li>Fallback para registro recursivo tradicional caso FILE_TREE não esteja disponível.</li>
 *   <li>Ignora diretórios e arquivos ocultos, de sistema e sem permissão de acesso.</li>
 *   <li>Debounce com proteção contra cancelamento acidental da thread do timer.</li>
 *   <li>Verificação de disponibilidade do arquivo antes de despachar o evento.</li>
 * </ul>
 */
public class MonitorArquivos {

    private final Path pastaRaiz;
    private final Consumer<Path> callback;
    private final Consumer<Path> callbackDelecao;

    private WatchService watchService;
    private final Map<WatchKey, Path> mapaChaves = new ConcurrentHashMap<>();
    private final Map<Path, Long> pendentes = new ConcurrentHashMap<>();
    private final Map<Path, Integer> tentativasPendentes = new ConcurrentHashMap<>();
    private final Map<Path, Long> deletadosPendentes = new ConcurrentHashMap<>();

    private final ScheduledExecutorService schedulerDebounce =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "Debounce-Worker");
                t.setDaemon(true);
                return t;
            });

    private volatile boolean rodando = false;
    private Thread threadMonitor;
    private boolean usaFileTreeNativo = false;

    public MonitorArquivos(Path pastaRaiz, Consumer<Path> callback) {
        this(pastaRaiz, callback, null);
    }

    public MonitorArquivos(Path pastaRaiz, Consumer<Path> callback, Consumer<Path> callbackDelecao) {
        this.pastaRaiz = pastaRaiz;
        this.callback  = callback;
        this.callbackDelecao = callbackDelecao;
    }

    /** Inicia o monitoramento em tempo real. */
    public void iniciar() throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        rodando = true;

        WatchEvent.Modifier modFileTree = obterModificadorFileTree();
        if (modFileTree != null) {
            try {
                WatchKey chave = pastaRaiz.register(watchService,
                        new WatchEvent.Kind<?>[]{
                                StandardWatchEventKinds.ENTRY_CREATE,
                                StandardWatchEventKinds.ENTRY_MODIFY,
                                StandardWatchEventKinds.ENTRY_DELETE
                        },
                        modFileTree);
                mapaChaves.put(chave, pastaRaiz);
                usaFileTreeNativo = true;
                System.out.println("[MONITOR] Monitoramento nativo em árvore (FILE_TREE) ativado em: " + pastaRaiz);
            } catch (Throwable t) {
                System.out.println("[MONITOR] FILE_TREE indisponível, usando registro tradicional: " + t.getMessage());
            }
        }

        if (!usaFileTreeNativo) {
            registrarRecursivo(pastaRaiz);
            System.out.println("[MONITOR] Monitoramento tradicional ativo em " + mapaChaves.size() + " pastas.");
        }

        // Worker de debounce — verifica pendentes a cada 200ms de forma blindada
        schedulerDebounce.scheduleAtFixedRate(this::executarProcessamentoSeguro,
                Config.DEBOUNCE_MS, 200, TimeUnit.MILLISECONDS);

        threadMonitor = new Thread(this::loopEventos, "WatchService-Monitor");
        threadMonitor.setDaemon(true);
        threadMonitor.start();
    }

    /** Para o monitoramento de forma limpa. */
    public void parar() {
        rodando = false;
        schedulerDebounce.shutdownNow();
        try {
            if (watchService != null) {
                watchService.close();
            }
        } catch (IOException ignored) {}
        if (threadMonitor != null) {
            threadMonitor.interrupt();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void loopEventos() {
        while (rodando) {
            WatchKey chave;
            try {
                chave = watchService.take(); // Bloqueia aguardando evento do sistema de arquivos
            } catch (InterruptedException | ClosedWatchServiceException e) {
                break;
            }

            try {
                Path diretorioBase = mapaChaves.get(chave);
                if (diretorioBase == null) {
                    chave.cancel();
                    continue;
                }

                for (WatchEvent<?> evento : chave.pollEvents()) {
                    WatchEvent.Kind<?> tipo = evento.kind();

                    if (tipo == StandardWatchEventKinds.OVERFLOW) continue;

                    @SuppressWarnings("unchecked")
                    Path nomeRelativo = ((WatchEvent<Path>) evento).context();
                    Path caminhoAbsoluto = diretorioBase.resolve(nomeRelativo);

                    if (tipo == StandardWatchEventKinds.ENTRY_DELETE) {
                        pendentes.remove(caminhoAbsoluto);
                        tentativasPendentes.remove(caminhoAbsoluto);
                        String nome = caminhoAbsoluto.getFileName() != null ? caminhoAbsoluto.getFileName().toString() : "";
                        if (!nome.endsWith(".tmp") && !nome.startsWith("~$") && !nome.startsWith(".")) {
                            System.out.println("[MONITOR] Exclusão detectada: " + nome);
                            deletadosPendentes.put(caminhoAbsoluto, System.currentTimeMillis());
                        }
                        continue;
                    }

                    // Se não estiver usando FILE_TREE nativo e um novo diretório foi criado, registra recursivo
                    if (!usaFileTreeNativo && tipo == StandardWatchEventKinds.ENTRY_CREATE
                            && Files.isDirectory(caminhoAbsoluto)) {
                        deletadosPendentes.remove(caminhoAbsoluto);
                        registrarRecursivo(caminhoAbsoluto);
                        continue;
                    }

                    // Se for arquivo (ou novo item ainda sendo criado)
                    if (!Files.isDirectory(caminhoAbsoluto)) {
                        String nome = caminhoAbsoluto.getFileName() != null ? caminhoAbsoluto.getFileName().toString() : "";
                        // Ignora arquivos temporários comuns de editores (~$, .tmp, etc.)
                        if (!nome.endsWith(".tmp") && !nome.startsWith("~$") && !nome.startsWith(".")) {
                            deletadosPendentes.remove(caminhoAbsoluto);
                            System.out.println("[MONITOR] Alteração detectada: " + nome + " (" + tipo.name() + ")");
                            pendentes.put(caminhoAbsoluto, System.currentTimeMillis());
                            tentativasPendentes.put(caminhoAbsoluto, 0);
                        }
                    }
                }
            } catch (Throwable t) {
                System.err.println("[MONITOR] Erro ao processar evento: " + t.getMessage());
            } finally {
                boolean valido = chave.reset();
                if (!valido) {
                    mapaChaves.remove(chave);
                }
            }
        }
    }

    /**
     * Executa o processamento de debounce com tratamento total de exceções
     * para garantir que o ScheduledExecutor nunca seja suprimido pelo Java.
     */
    private void executarProcessamentoSeguro() {
        try {
            processarPendentes();
        } catch (Throwable t) {
            System.err.println("[MONITOR] Erro inesperado no processamento de pendentes: " + t.getMessage());
        }
    }

    private void processarPendentes() {
        long agora = System.currentTimeMillis();
        Iterator<Map.Entry<Path, Long>> it = pendentes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Path, Long> entrada = it.next();
            Path arquivo = entrada.getKey();
            long ultimoEvento = entrada.getValue();

            if (agora - ultimoEvento < Config.DEBOUNCE_MS) continue;

            // Se o arquivo foi deletado antes de estabilizar
            if (!Files.exists(arquivo)) {
                it.remove();
                tentativasPendentes.remove(arquivo);
                continue;
            }

            // Ignorar arquivos comprovadamente ocultos
            if (Utils.isOculto(arquivo)) {
                it.remove();
                tentativasPendentes.remove(arquivo);
                continue;
            }

            // Verificar se o arquivo está disponível para leitura (não está com lock exclusivo de escrita)
            if (!estaDisponivel(arquivo)) {
                int tentativas = tentativasPendentes.getOrDefault(arquivo, 0) + 1;
                tentativasPendentes.put(arquivo, tentativas);

                if (tentativas <= 25) { // Aguarda até ~5 segundos adicionais para o processo liberá-lo
                    entrada.setValue(agora);
                    continue;
                }
                // Se após 5s ainda estiver bloqueado, aguarda mais um pouco espaçando as tentativas
                if (tentativas <= 40) {
                    entrada.setValue(agora + 500); // testa a cada 500ms
                    continue;
                }
                System.err.println("[MONITOR] Arquivo bloqueado por muito tempo por outro processo: " + arquivo.getFileName());
            }

            it.remove();
            tentativasPendentes.remove(arquivo);

            try {
                System.out.println("[MONITOR] Arquivo estabilizado, disparando sincronização: " + arquivo.getFileName());
                callback.accept(arquivo);
            } catch (Throwable e) {
                System.err.println("[MONITOR] Erro no callback para " + arquivo + ": " + e.getMessage());
            }
        }

        // Processamento de deleções com debounce para evitar falsos positivos
        Iterator<Map.Entry<Path, Long>> itDel = deletadosPendentes.entrySet().iterator();
        while (itDel.hasNext()) {
            Map.Entry<Path, Long> entradaDel = itDel.next();
            Path arquivoDel = entradaDel.getKey();
            long horaEvento = entradaDel.getValue();

            if (agora - horaEvento < Config.DEBOUNCE_MS) continue;

            itDel.remove();

            // Confirma se o arquivo realmente NÃO existe mais após o período de debounce
            if (!Files.exists(arquivoDel)) {
                if (callbackDelecao != null) {
                    try {
                        System.out.println("[MONITOR] Exclusão estabilizada, disparando remoção: " + arquivoDel.getFileName());
                        callbackDelecao.accept(arquivoDel);
                    } catch (Throwable e) {
                        System.err.println("[MONITOR] Erro no callback de deleção para " + arquivoDel + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    private boolean estaDisponivel(Path arquivo) {
        if (!Files.isReadable(arquivo)) {
            return false;
        }
        try (InputStream is = Files.newInputStream(arquivo)) {
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    private void registrarRecursivo(Path inicio) {
        if (inicio == null || !Files.exists(inicio)) {
            return;
        }
        try {
            Files.walkFileTree(inicio, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (!dir.equals(pastaRaiz)) {
                        if (Utils.isOculto(dir) || !Utils.isLegivel(dir)) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    }
                    try {
                        WatchKey chave = dir.register(watchService,
                                StandardWatchEventKinds.ENTRY_CREATE,
                                StandardWatchEventKinds.ENTRY_MODIFY,
                                StandardWatchEventKinds.ENTRY_DELETE);
                        mapaChaves.put(chave, dir);
                    } catch (Throwable t) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Throwable t) {
            System.err.println("[MONITOR] Erro ao registrar diretórios: " + t.getMessage());
        }
    }

    private static WatchEvent.Modifier obterModificadorFileTree() {
        try {
            Class<?> clazz = Class.forName("com.sun.nio.file.ExtendedWatchEventModifier");
            Field campo = clazz.getField("FILE_TREE");
            return (WatchEvent.Modifier) campo.get(null);
        } catch (Throwable t) {
            return null;
        }
    }
}
