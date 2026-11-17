

import java.io.IOException;
import java.net.*;
import java.nio.file.Path;
import java.util.List;

/**
 * Loop principal do cliente de sincronização.
 *
 * <p>Fluxo de execução:
 * <ol>
 *   <li>Descobrir IP do servidor via UDP broadcast.</li>
 *   <li>Abrir conexão TCP na porta {@link Config#PORTA_TCP}.</li>
 *   <li>Realizar handshake e reconciliação instantânea (metadados).</li>
 *   <li>Enviar arquivos pendentes com barra de progresso.</li>
 *   <li>Manter conexão aberta, monitorando Documentos em tempo real.</li>
 *   <li>Em caso de falha, aguardar backoff e recomeçar do passo 1.</li>
 * </ol>
 */
public class ClienteSinc {

    private static final int[] BACKOFFS_SEGUNDOS = {5, 10, 30, 60};

    private volatile boolean rodando = false;
    private volatile Socket socketAtual;
    private MonitorArquivos monitor;
    private EnviadorArquivos enviador;

    private final String mac;
    private final Path pastaDocumentos;

    public ClienteSinc(String caminhoPastaOrigem) {
        this.mac = Utils.getMacAddress();
        this.pastaDocumentos = Config.getPastaDocumentos(caminhoPastaOrigem);
    }

    /** Inicia o loop principal — bloqueia indefinidamente. */
    public void iniciar() {
        rodando = true;
        int tentativa = 0;

        System.out.println("[CLIENT] MAC desta estação: " + mac);
        System.out.println("[CLIENT] Pasta monitorada: " + pastaDocumentos);

        while (rodando) {
            try {
                // ── Passo 1: Descobrir servidor ──────────────────────────────
                InetAddress enderecoServidor = descobrirServidor();
                if (enderecoServidor == null) {
                    aguardarBackoff(tentativa++);
                    continue;
                }
                tentativa = 0; // reset backoff após descoberta bem-sucedida

                // ── Passo 2: Conectar TCP ────────────────────────────────────
                socketAtual = conectar(enderecoServidor);
                if (socketAtual == null) {
                    aguardarBackoff(tentativa++);
                    continue;
                }

                System.out.println("[CLIENT] Conectado em "
                        + enderecoServidor.getHostAddress() + ":" + Config.PORTA_TCP);

                // Obtém o MAC da interface real vinculada ao socket conectado (mesmo método do ClienteSemMaven)
                String macEstacao = Utils.getMacAddress(socketAtual);
                System.out.println("[CLIENT] MAC desta estação (via conexão ativa): " + macEstacao);

                try {
                    enviador = new EnviadorArquivos(socketAtual, pastaDocumentos, macEstacao);

                    // ── Passo 3: Handshake ───────────────────────────────────
                    if (!enviador.handshake()) {
                        fecharSocketAtual();
                        aguardarBackoff(tentativa++);
                        continue;
                    }

                    // ── Passo 4: Reconciliação Instantânea ───────────────────
                    List<String> pendentes = enviador.reconciliar();

                    // ── Passo 5: Enviar arquivos do diff ─────────────────────
                    int totalUploads = pendentes.size();
                    if (totalUploads > 0) {
                        System.out.println("[CLIENT] Iniciando sincronização de " + totalUploads + " arquivo(s)...");
                        int enviadosComSucesso = 0;
                        for (int i = 0; i < totalUploads; i++) {
                            if (!rodando) break;
                            String caminhoRelativo = pendentes.get(i);
                            try {
                                boolean ok = enviador.uploadArquivo(caminhoRelativo);
                                if (ok) {
                                    enviadosComSucesso++;
                                }
                                if ((i + 1) % 50 == 0 || (i + 1) == totalUploads || totalUploads <= 20) {
                                    System.out.printf("[CLIENT] Progresso: %d/%d (%.1f%%) sincronizados.%n",
                                            (i + 1), totalUploads, ((i + 1) * 100.0 / totalUploads));
                                }
                            } catch (IOException e) {
                                System.err.println("[CLIENT] Falha de comunicação ao enviar "
                                        + caminhoRelativo + ": " + e.getMessage() + ". Reconectando...");
                                throw e; // Força reconexão imediata se o socket falhar
                            }
                        }
                        System.out.println("[CLIENT] Sincronização inicial finalizada: "
                                + enviadosComSucesso + "/" + totalUploads + " arquivos transferidos.");
                    } else {
                        System.out.println("[CLIENT] Todos os arquivos já estão atualizados no servidor!");
                    }

                    // ── Passo 6: Monitoramento em tempo real ─────────────────
                    iniciarMonitor();
                    System.out.println("[CLIENT] Monitoramento em tempo real ativo.");

                    // Manter thread viva enquanto o socket estiver conectado.
                    // Envia SINC_PING periodicamente e valida resposta imediata.
                    while (rodando && socketAtual != null && !socketAtual.isClosed()) {
                        Thread.sleep(15_000); // Checagem a cada 15 segundos
                        try {
                            enviador.enviarPing();
                        } catch (IOException e) {
                            System.err.println("[CLIENT] Falha no teste de conexão: " + e.getMessage());
                            break;
                        }
                    }

                } catch (IOException e) {
                    System.err.println("[CLIENT] Conexão perdida: " + e.getMessage());
                } finally {
                    pararMonitor();
                    fecharSocketAtual();
                    System.out.println("[CLIENT] Desconectado. Reconectando...");
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            if (rodando) {
                aguardarBackoff(tentativa++);
            }
        }

        System.out.println("[CLIENT] Encerrado.");
    }

    /** Para o cliente de forma limpa. */
    public void parar() {
        rodando = false;
        pararMonitor();
    }

    // ─────────────────────────────────────────────────────────────────────────

    private InetAddress descobrirServidor() {
        return BuscaServidor.descobrir();
    }

    private Socket conectar(InetAddress enderecoServidor) {
        try {
            Socket s = new Socket();
            s.connect(new InetSocketAddress(enderecoServidor, Config.PORTA_TCP), 10_000);
            s.setKeepAlive(true);
            s.setSoTimeout(30_000); // 30 segundos de timeout de leitura para evitar bloqueio eterno
            return s;
        } catch (IOException e) {
            System.err.println("[CLIENT] Falha ao conectar: " + e.getMessage());
            return null;
        }
    }

    private void fecharSocketAtual() {
        try {
            if (socketAtual != null && !socketAtual.isClosed()) {
                socketAtual.close();
            }
        } catch (Exception ignored) {}
    }

    private void iniciarMonitor() throws IOException {
        pararMonitor();
        monitor = new MonitorArquivos(pastaDocumentos, this::aoDetectarAlteracao, this::aoDetectarDelecao);
        monitor.iniciar();
    }

    private void pararMonitor() {
        if (monitor != null) {
            monitor.parar();
            monitor = null;
        }
    }

    private void aoDetectarAlteracao(java.nio.file.Path arquivoAbsoluto) {
        if (enviador == null) return;
        try {
            boolean ok = enviador.uploadArquivoAbsoluto(arquivoAbsoluto);
            if (ok) {
                System.out.println("[CLIENT] Alteração sincronizada com sucesso: " + arquivoAbsoluto.getFileName());
            } else {
                System.err.println("[CLIENT] Não foi possível enviar alteração de: " + arquivoAbsoluto.getFileName());
            }
        } catch (IOException e) {
            System.err.println("[CLIENT] Erro de rede/socket durante upload de " + arquivoAbsoluto.getFileName()
                    + ": " + e.getMessage() + ". Forçando reconexão limpa...");
            fecharSocketAtual();
        } catch (Throwable e) {
            System.err.println("[CLIENT] Erro inesperado ao sincronizar alteração: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void aoDetectarDelecao(java.nio.file.Path arquivoAbsoluto) {
        if (enviador == null) return;
        try {
            boolean ok = enviador.deletarArquivoAbsoluto(arquivoAbsoluto);
            if (ok) {
                System.out.println("[CLIENT] Deleção sincronizada com sucesso: " + arquivoAbsoluto.getFileName());
            } else {
                System.err.println("[CLIENT] Falha ao sincronizar deleção de: " + arquivoAbsoluto.getFileName());
            }
        } catch (IOException e) {
            System.err.println("[CLIENT] Erro de rede/socket durante deleção de " + arquivoAbsoluto.getFileName()
                    + ": " + e.getMessage() + ". Forçando reconexão limpa...");
            fecharSocketAtual();
        } catch (Throwable e) {
            System.err.println("[CLIENT] Erro inesperado ao sincronizar deleção: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void aguardarBackoff(int tentativa) {
        int segundos = BACKOFFS_SEGUNDOS[Math.min(tentativa, BACKOFFS_SEGUNDOS.length - 1)];
        System.out.println("[CLIENT] Aguardando " + segundos + "s antes de tentar novamente...");
        try { Thread.sleep(segundos * 1000L); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
