

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.*;

/**
 * Implementa o protocolo SINC_* do lado cliente de forma otimizada.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Handshake com o servidor.</li>
 *   <li>Envio de metadados rápidos (tamanho + timestamp) para reconciliação instantânea sem hashing prévio.</li>
 *   <li>Processamento do diff respondido pelo servidor.</li>
 *   <li>Upload individual de arquivos com cálculo pontual de SHA-256 e validação de integridade.</li>
 * </ul>
 */
public class EnviadorArquivos {

    private final BufferedReader entrada;
    private final PrintWriter saida;
    private final OutputStream bytesRaw;
    private final Path pastaDocumentos;
    private final String mac;

    public EnviadorArquivos(Socket socket, Path pastaDocumentos, String mac) throws IOException {
        this.entrada = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), "UTF-8"));
        this.saida = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        this.bytesRaw = socket.getOutputStream();
        this.pastaDocumentos = pastaDocumentos;
        this.mac = mac;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fase 1 — Handshake
    // ─────────────────────────────────────────────────────────────────────────

    public boolean handshake() throws IOException {
        saida.println("SINC_HELLO:" + mac + ":" + Config.VERSAO_PROTOCOLO);
        String resposta = entrada.readLine();
        if ("SINC_OK".equals(resposta)) {
            System.out.println("[ENVIO] Handshake OK — MAC: " + mac);
            return true;
        }
        System.err.println("[ENVIO] Handshake rejeitado: " + resposta);
        return false;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fase 2 — Reconciliação Instantânea (Sem Deadlock e Sem Hashing Prévio)
    // ─────────────────────────────────────────────────────────────────────────

    public List<String> reconciliar() throws IOException {
        System.out.println("[ENVIO] Analisando pasta: " + pastaDocumentos + "...");
        List<Utils.InfoArquivoLocal> arquivos = Utils.listarArquivosComMetadados(pastaDocumentos);
        int total = arquivos.size();
        System.out.println("[ENVIO] Total de arquivos encontrados: " + total);

        saida.println("SINC_LISTA_INICIO");

        int ignorados = 0;
        int enviadosLista = 0;

        for (int i = 0; i < total; i++) {
            Utils.InfoArquivoLocal item = arquivos.get(i);

            if (item.tamanho > Config.TAMANHO_MAXIMO_ARQUIVO) {
                ignorados++;
                continue;
            }

            String caminhoRelativo = Utils.caminhoRelativo(pastaDocumentos, item.caminhoAbsoluto);
            saida.println("SINC_ARQUIVO:" + caminhoRelativo + ":" + item.tamanho + ":" + item.lastModified);
            enviadosLista++;

            if ((i + 1) % 5000 == 0 || (i + 1) == total) {
                System.out.println("[ENVIO] Enviados metadados: " + (i + 1) + "/" + total + "...");
            }
        }

        saida.println("SINC_LISTA_FIM");

        if (ignorados > 0) {
            System.out.println("[ENVIO] " + ignorados + " arquivo(s) ignorado(s) por exceder 1 GB.");
        }

        System.out.println("[ENVIO] Aguardando resposta do servidor...");

        // Ler resposta do servidor (diff enviado de uma só vez)
        List<String> pendentes = new ArrayList<>();
        String linha;
        while ((linha = entrada.readLine()) != null) {
            if ("SINC_DIFF_FIM".equals(linha)) break;
            if (linha.startsWith("SINC_PRECISA:")) {
                pendentes.add(linha.substring("SINC_PRECISA:".length()));
            }
        }

        System.out.println("[ENVIO] Reconciliação concluída — "
                + pendentes.size() + " arquivo(s) necessitam sincronização.");
        return pendentes;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fase 3 — Upload pontual com SHA-256 (Thread-Safe)
    // ─────────────────────────────────────────────────────────────────────────

    public synchronized boolean uploadArquivo(String caminhoRelativo) throws IOException {
        Path arquivo = pastaDocumentos.resolve(
                caminhoRelativo.replace('/', File.separatorChar));

        if (!Files.exists(arquivo)) {
            System.out.println("[ENVIO] Arquivo inexistente ou já removido: " + caminhoRelativo);
            return false;
        }

        if (Utils.isOculto(arquivo)) {
            System.out.println("[ENVIO] Arquivo oculto ignorado: " + caminhoRelativo);
            return false;
        }

        if (!Utils.isLegivel(arquivo)) {
            System.out.println("[ENVIO] Arquivo sem permissão de leitura: " + caminhoRelativo);
            return false;
        }

        long tamanho;
        long lastModified;
        try {
            tamanho = Files.size(arquivo);
            lastModified = Files.getLastModifiedTime(arquivo).toMillis();
        } catch (IOException e) {
            System.err.println("[ENVIO] Erro ao obter metadados de " + caminhoRelativo + ": " + e.getMessage());
            return false;
        }

        if (tamanho > Config.TAMANHO_MAXIMO_ARQUIVO) {
            System.out.println("[ENVIO] Arquivo muito grande (> 1 GB), ignorado: " + caminhoRelativo);
            return false;
        }

        // Tenta calcular o SHA-256 (com até 3 tentativas breves se outro processo estiver finalizando a escrita)
        String sha256 = null;
        for (int tent = 0; tent < 3; tent++) {
            sha256 = Utils.calcularSha256(arquivo);
            if (sha256 != null) break;
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }

        if (sha256 == null) {
            System.err.println("[ENVIO] Não foi possível calcular hash (arquivo em uso exclusivo?): " + caminhoRelativo);
            return false;
        }

        // Enviar cabeçalho do upload: caminho:tamanho:lastModified:sha256
        saida.println("SINC_UPLOAD:" + caminhoRelativo + ":" + tamanho + ":" + lastModified + ":" + sha256);

        // Aguardar autorização do servidor
        String resposta = entrada.readLine();
        if (resposta == null) {
            throw new IOException("Conexão encerrada pelo servidor ao aguardar autorização de upload.");
        }
        if (!"SINC_PRONTO".equals(resposta)) {
            if (resposta.startsWith("SINC_NACK:")) {
                System.err.println("[ENVIO] Servidor rejeitou upload de " + caminhoRelativo + ": " + resposta);
                return false;
            }
            throw new IOException("Protocolo dessincronizado no upload de " + caminhoRelativo + " (recebido: " + resposta + ")");
        }

        // Enviar bytes do arquivo de forma blindada contra deadlock
        long enviado = 0;
        try (InputStream is = Files.newInputStream(arquivo)) {
            byte[] buffer = new byte[Config.TAMANHO_BUFFER];
            int lido;
            while (enviado < tamanho && (lido = is.read(buffer, 0, (int) Math.min(buffer.length, tamanho - enviado))) != -1) {
                bytesRaw.write(buffer, 0, lido);
                enviado += lido;
            }
            bytesRaw.flush();
        } catch (IOException e) {
            System.err.println("[ENVIO] Erro ao ler arquivo durante upload: " + e.getMessage());
        }

        // Se o arquivo tiver sido truncado no disco durante o envio,
        // preenche com zeros para completar exatamente o tamanho declarado e NÃO travar o socket TCP
        if (enviado < tamanho) {
            byte[] preenchimento = new byte[(int) Math.min(Config.TAMANHO_BUFFER, tamanho - enviado)];
            while (enviado < tamanho) {
                int faltam = (int) Math.min(preenchimento.length, tamanho - enviado);
                bytesRaw.write(preenchimento, 0, faltam);
                enviado += faltam;
            }
            bytesRaw.flush();
        }

        // Verificar confirmação
        String ack = entrada.readLine();
        if (ack == null) {
            throw new IOException("Conexão encerrada pelo servidor aguardando confirmação de upload.");
        }
        if (ack.startsWith("SINC_ACK:")) {
            System.out.println("[ENVIO] ✓ Sincronizado com sucesso: " + caminhoRelativo + " (" + tamanho + " bytes)");
            return true;
        } else {
            System.err.println("[ENVIO] ✗ Falha em " + caminhoRelativo + ": " + ack);
            return false;
        }
    }

    public boolean uploadArquivoAbsoluto(Path arquivoAbsoluto) throws IOException {
        String caminhoRelativo = Utils.caminhoRelativo(pastaDocumentos, arquivoAbsoluto);
        return uploadArquivo(caminhoRelativo);
    }

    /**
     * Notifica o servidor para deletar um arquivo que foi excluído na máquina local.
     *
     * @param caminhoRelativo caminho relativo com '/' como separador
     * @return true se o servidor confirmou a deleção
     * @throws IOException em caso de erro no socket
     */
    public synchronized boolean deletarArquivo(String caminhoRelativo) throws IOException {
        saida.println("SINC_DELETE:" + caminhoRelativo);
        String ack = entrada.readLine();
        if (ack == null) {
            throw new IOException("Conexão encerrada pelo servidor durante SINC_DELETE.");
        }
        if (ack.startsWith("SINC_ACK_DEL:")) {
            System.out.println("[ENVIO] ✓ Removido do servidor com sucesso: " + caminhoRelativo);
            return true;
        } else {
            System.err.println("[ENVIO] ✗ Falha ao remover no servidor " + caminhoRelativo + ": " + ack);
            return false;
        }
    }

    public boolean deletarArquivoAbsoluto(Path arquivoAbsoluto) throws IOException {
        String caminhoRelativo = Utils.caminhoRelativo(pastaDocumentos, arquivoAbsoluto);
        return deletarArquivo(caminhoRelativo);
    }

    /**
     * Envia um ping para verificar se a conexão com o servidor ainda está ativa.
     * Consome imediatamente a resposta SINC_PONG para manter o stream limpo.
     *
     * @throws IOException se a conexão estiver quebrada
     */
    public synchronized void enviarPing() throws IOException {
        saida.println("SINC_PING");
        String resposta = entrada.readLine();
        if (resposta == null || !"SINC_PONG".equals(resposta)) {
            throw new IOException("Falha no keep-alive (resposta: " + resposta + ")");
        }
    }
}
