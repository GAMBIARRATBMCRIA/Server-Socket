

import java.io.*;
import java.net.Socket;
import java.util.*;

/**
 * Manipula a conexão TCP com um único cliente de sincronização.
 *
 * <p>Executado em thread própria. O protocolo segue três fases:
 * <ol>
 *   <li><b>Handshake</b> — identifica o cliente pelo MAC e verifica versão do protocolo.</li>
 *   <li><b>Reconciliação sem Deadlock</b> — consome toda a lista do cliente até
 *       {@code SINC_LISTA_FIM}, compara com o registro em memória e SOMENTE ENTÃO
 *       envia os arquivos que precisa.</li>
 *   <li><b>Loop de uploads</b> — recebe arquivos individuais (do diff e de eventos em
 *       tempo real) até a conexão ser encerrada.</li>
 * </ol>
 */
public class ManipuladorCliente implements Runnable {

    private final Socket socket;
    private final RegistroSincronizacao registro;
    private final GerenciadorArquivos gerenciador;

    public ManipuladorCliente(Socket socket,
                              RegistroSincronizacao registro,
                              GerenciadorArquivos gerenciador) {
        this.socket = socket;
        this.registro = registro;
        this.gerenciador = gerenciador;
    }

    @Override
    public void run() {
        String mac = "DESCONHECIDO";
        try {
            socket.setKeepAlive(true);
            // Timeout inicial (fases de handshake e reconciliação):
            // se o cliente não enviar a lista completa em 2 minutos, encerra a sessão.
            socket.setSoTimeout(120_000); // 120 segundos
            // Stream de texto para o protocolo linha-a-linha
            BufferedReader entrada = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "UTF-8"));
            PrintWriter saida = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            // Stream bruto para bytes de arquivo
            InputStream bytesEntrada = socket.getInputStream();

            // ── Fase 1: Handshake ────────────────────────────────────────────
            mac = handshake(entrada, saida);
            if (mac == null) return;

            // ── Fase 2: Reconciliação (Sem Deadlock) ─────────────────────────
            List<String> caminhosPendentes = reconciliar(mac, entrada, saida);

            // ── Fase 3: Loop de uploads ──────────────────────────────────────
            // Timeout de 60s: clientes enviam ping a cada 15s. Se o cliente desaparecer
            // sem fechar a conexão por 60s, a sessão é encerrada limpa.
            socket.setSoTimeout(60_000);
            loopUploads(mac, entrada, saida, bytesEntrada);

        } catch (IOException e) {
            System.out.println("[HANDLER:" + mac + "] Desconectado — " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            fecharSocket();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fase 1 — Handshake
    // ─────────────────────────────────────────────────────────────────────────

    private String handshake(BufferedReader entrada, PrintWriter saida) throws IOException {
        String linha = entrada.readLine();
        if (linha == null || !linha.startsWith("SINC_HELLO:")) {
            saida.println("SINC_REJECT:protocolo_invalido");
            return null;
        }
        String[] partes = linha.substring("SINC_HELLO:".length()).split(":", 2);
        if (partes.length != 2) {
            saida.println("SINC_REJECT:formato_invalido");
            return null;
        }
        String mac = partes[0].trim();
        String versao = partes[1].trim();

        if (!Config.VERSAO_PROTOCOLO.equals(versao)) {
            saida.println("SINC_REJECT:versao_incompativel");
            System.err.println("[HANDLER:" + mac + "] Versão incompatível: " + versao);
            return null;
        }

        saida.println("SINC_OK");
        System.out.println("[HANDLER:" + mac + "] Conectado de " + socket.getInetAddress().getHostAddress());
        return mac;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fase 2 — Reconciliação (Half-Duplex sem Deadlock)
    // ─────────────────────────────────────────────────────────────────────────

    private List<String> reconciliar(String mac, BufferedReader entrada, PrintWriter saida)
            throws IOException {

        String linha = entrada.readLine();
        if (!"SINC_LISTA_INICIO".equals(linha)) {
            System.err.println("[HANDLER:" + mac + "] Esperado SINC_LISTA_INICIO, recebido: " + linha);
            return Collections.emptyList();
        }

        List<String> pendentes = new ArrayList<>();
        Set<String> caminhosLocais = new HashSet<>();
        int totalRecebidos = 0;

        // IMPORTANTE: apenas lê do cliente até SINC_LISTA_FIM.
        // NÃO responde nada pelo socket aqui para evitar esgotar buffer TCP (deadlock).
        while (true) {
            linha = entrada.readLine();
            if (linha == null) {
                throw new IOException("Conexão encerrada durante reconciliação.");
            }
            if ("SINC_LISTA_FIM".equals(linha)) {
                break;
            }

            if (linha.startsWith("SINC_ARQUIVO:")) {
                // Formato: SINC_ARQUIVO:{caminho}:{tamanho}:{lastModified}
                String corpo = linha.substring("SINC_ARQUIVO:".length());
                int ultimoDois = corpo.lastIndexOf(':');
                if (ultimoDois < 0) continue;
                String campo3 = corpo.substring(ultimoDois + 1);

                String restante = corpo.substring(0, ultimoDois);
                int penultimoDois = restante.lastIndexOf(':');
                if (penultimoDois < 0) continue;
                String campo2 = restante.substring(penultimoDois + 1);
                String caminhoRelativo = restante.substring(0, penultimoDois);

                totalRecebidos++;
                caminhosLocais.add(caminhoRelativo);

                long tamanho = 0;
                long lastModified = 0;
                boolean parseado = false;
                try {
                    tamanho = Long.parseLong(campo2);
                    lastModified = Long.parseLong(campo3);
                    parseado = true;
                } catch (NumberFormatException ignored) {}

                if (parseado) {
                    if (!registro.jaSincronizado(mac, caminhoRelativo, tamanho, lastModified)) {
                        pendentes.add(caminhoRelativo);
                    }
                } else {
                    // Fallback para formato antigo com sha256
                    String sha256 = campo3;
                    String registrado = registro.obterSha256(mac, caminhoRelativo);
                    if (registrado == null || !registrado.equalsIgnoreCase(sha256)) {
                        pendentes.add(caminhoRelativo);
                    }
                }
            }
        }

        // Deletar no servidor arquivos que existiam no registro mas não estão mais na pasta do cliente
        Set<String> caminhosRegistrados = registro.obterCaminhos(mac);
        int totalDeletados = 0;
        for (String caminhoRegistrado : caminhosRegistrados) {
            if (!caminhosLocais.contains(caminhoRegistrado)) {
                gerenciador.deletar(mac, caminhoRegistrado);
                registro.remover(mac, caminhoRegistrado);
                totalDeletados++;
            }
        }

        System.out.println("[HANDLER:" + mac + "] Reconciliação concluída: "
                + totalRecebidos + " arquivos analisados, "
                + pendentes.size() + " a sincronizar, "
                + totalDeletados + " deletado(s) no servidor.");

        // AGORA SIM: cliente já enviou tudo e está esperando a resposta.
        for (String p : pendentes) {
            saida.println("SINC_PRECISA:" + p);
        }
        saida.println("SINC_DIFF_FIM");

        return pendentes;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fase 3 — Loop de uploads
    // ─────────────────────────────────────────────────────────────────────────

    private void loopUploads(String mac, BufferedReader entrada,
                              PrintWriter saida, InputStream bytesEntrada)
            throws IOException, InterruptedException {

        String linha;
        while ((linha = entrada.readLine()) != null) {
            if ("SINC_PING".equals(linha)) {
                saida.println("SINC_PONG");
                continue;
            }

            if (linha.startsWith("SINC_DELETE:")) {
                String caminhoRelativo = linha.substring("SINC_DELETE:".length()).trim();
                boolean ok = gerenciador.deletar(mac, caminhoRelativo);
                registro.remover(mac, caminhoRelativo);
                if (ok) {
                    System.out.println("[HANDLER:" + mac + "] Arquivo deletado com sucesso: " + caminhoRelativo);
                    saida.println("SINC_ACK_DEL:" + caminhoRelativo);
                } else {
                    System.err.println("[HANDLER:" + mac + "] Falha ao deletar arquivo: " + caminhoRelativo);
                    saida.println("SINC_NACK_DEL:" + caminhoRelativo);
                }
                continue;
            }

            if (!linha.startsWith("SINC_UPLOAD:")) {
                System.err.println("[HANDLER:" + mac + "] Comando desconhecido: " + linha);
                continue;
            }

            // Formato: SINC_UPLOAD:{caminho}:{tamanho}:{lastModified}:{sha256}
            // (ou legado: SINC_UPLOAD:{caminho}:{tamanho}:{sha256})
            String corpo = linha.substring("SINC_UPLOAD:".length());
            int idx1 = corpo.lastIndexOf(':');
            if (idx1 < 0) { saida.println("SINC_NACK:formato_invalido"); continue; }
            String sha256 = corpo.substring(idx1 + 1).trim();

            String r1 = corpo.substring(0, idx1);
            int idx2 = r1.lastIndexOf(':');
            if (idx2 < 0) { saida.println("SINC_NACK:formato_invalido"); continue; }
            String campo2 = r1.substring(idx2 + 1).trim();

            String r2 = r1.substring(0, idx2);
            int idx3 = r2.lastIndexOf(':');

            long tamanho;
            long lastModified = System.currentTimeMillis();
            String caminhoRelativo;

            if (idx3 >= 0) {
                // Formato com 4 partes: caminho:tamanho:lastModified:sha256
                caminhoRelativo = r2.substring(0, idx3);
                try {
                    tamanho = Long.parseLong(r2.substring(idx3 + 1).trim());
                    lastModified = Long.parseLong(campo2);
                } catch (NumberFormatException e) {
                    saida.println("SINC_NACK:tamanho_invalido");
                    continue;
                }
            } else {
                // Formato legado com 3 partes: caminho:tamanho:sha256
                caminhoRelativo = r2;
                try {
                    tamanho = Long.parseLong(campo2);
                } catch (NumberFormatException e) {
                    saida.println("SINC_NACK:tamanho_invalido");
                    continue;
                }
            }

            // Verificar limite de tamanho
            if (tamanho > Config.TAMANHO_MAXIMO_ARQUIVO) {
                saida.println("SINC_NACK:arquivo_muito_grande");
                System.err.println("[HANDLER:" + mac + "] Arquivo rejeitado (> 1 GB): " + caminhoRelativo);
                continue;
            }

            // Autorizar recebimento dos bytes
            saida.println("SINC_PRONTO");

            // Receber bytes e salvar
            boolean ok = gerenciador.salvar(mac, caminhoRelativo, tamanho, lastModified, sha256, bytesEntrada);

            if (ok) {
                registro.registrar(mac, caminhoRelativo, tamanho, lastModified, sha256);
                saida.println("SINC_ACK:" + caminhoRelativo);
            } else {
                saida.println("SINC_NACK:falha_integridade");
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void fecharSocket() {
        try { socket.close(); } catch (IOException ignored) {}
    }
}
