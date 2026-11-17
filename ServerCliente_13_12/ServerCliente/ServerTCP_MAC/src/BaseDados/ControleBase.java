package BaseDados;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import BaseDados.infomacaoMaquinas;
import Servicos.ComunicacaoClienteServer;
import javax.swing.JOptionPane;

/**
 * Controla a base de máquinas cadastradas e a lista de clientes ativos.
 *
 * CORREÇÃO BUG 1 + 4: usa ConcurrentHashMap<MAC, ComunicacaoClienteServer>
 * como estrutura primária para clientes conectados, garantindo: - Unicidade por
 * MAC (sem duplicatas na tabela) - Thread-safety nativa - Remoção atômica
 */
public class ControleBase {

    private ArrayList<infomacaoMaquinas> BaseCadastradaInfoMaquinas;

    /**
     * Mapa thread-safe: MAC → handler de comunicação do cliente. Garante que
     * apenas UMA sessão por MAC existe ao mesmo tempo.
     */
    private final ConcurrentHashMap<String, ComunicacaoClienteServer> clientesConectadosMap;

    public ControleBase() {
        this.BaseCadastradaInfoMaquinas = new ArrayList<>();
        this.clientesConectadosMap = new ConcurrentHashMap<>();
    }

    public Boolean importarDados() {
        List maquinasLista = Servicos.Instancias.getExecucaoAtividadesInstacia().lerTxt(configs.configuracao.localBase);

        for (int i = 0; i < maquinasLista.size(); i++) {
            infomacaoMaquinas info = new infomacaoMaquinas();
            String maquinaLinha = maquinasLista.get(i).toString();

            if (!maquinaLinha.contains("#")) {
                String barras = "";
                int intervaloInicio = 0;
                for (int letra = 0; letra < maquinaLinha.length(); letra++) {

                    switch (barras.length()) {
                        case 0:
                            info.setMacAddres(maquinaLinha.substring(intervaloInicio, letra));
                            break;
                        case 1:
                            info.setBancada(maquinaLinha.substring(intervaloInicio, letra));
                            break;
                        case 2:
                            info.setPosicao(maquinaLinha.substring(intervaloInicio, letra));
                            break;
                        case 3:
                            info.setSetor(maquinaLinha.substring(intervaloInicio, letra));
                            break;
                        case 4:
                            info.setTombo(maquinaLinha.substring(intervaloInicio, letra));
                            break;
                        default:
                            break;
                    }

                    if (maquinaLinha.substring(letra, letra + 1).equalsIgnoreCase("|")) {
                        barras = barras + "|";
                        intervaloInicio = letra + 1;
                    }
                }
                BaseCadastradaInfoMaquinas.add(info);
            }
        }
        return true;
    }

    public void cadastrar(String Mac, String bancada, String posicao, String tombo, String setor) {
        List maquinasLista = Servicos.Instancias.getExecucaoAtividadesInstacia().lerTxt(configs.configuracao.localBase);
        boolean naoContemMacCadastrado = true;
        String linha;
        String mensagem = "Cadastrado com sucesso!";
        for (int i = 0; i < maquinasLista.size(); i++) {
            linha = maquinasLista.get(i).toString();
            if (linha.contains(Mac)) {
                mensagem = "Máquina já cadastrada!";
                naoContemMacCadastrado = false;
            } else if (linha.contains(bancada) && linha.contains(posicao)) {
                mensagem = "Já existe uma máquina cadastrada:" + bancada + " " + posicao;
                naoContemMacCadastrado = false;
            }
        }
        if (naoContemMacCadastrado && Servicos.Instancias.getExecucaoAtividadesInstacia().escreverTxtBase(Mac, bancada, posicao, tombo, setor)) {
            JOptionPane.showMessageDialog(null, mensagem);
        } else {
            JOptionPane.showMessageDialog(null, "Erro ao cadastrar!\n" + mensagem);
        }
    }

    public void imprimirArray() {
        for (infomacaoMaquinas info : BaseCadastradaInfoMaquinas) {
            System.out.println("mac:" + info.getMacAddres());
            System.out.println("Bancada:" + info.getBancada());
            System.out.println("Posicao:" + info.getPosicao());
            System.out.println("Setor:" + info.getSetor());
            System.out.println("");
        }
    }

    public void setBaseCadastradaInfoMaquinas(ArrayList<infomacaoMaquinas> infoMaquinas) {
        this.BaseCadastradaInfoMaquinas = infoMaquinas;
    }

    public ArrayList<infomacaoMaquinas> getBaseCadastradaInfoMaquinas() {
        return BaseCadastradaInfoMaquinas;
    }

    // -------------------------------------------------------------------------
    // Gerenciamento de Clientes Conectados (thread-safe por ConcurrentHashMap)
    // -------------------------------------------------------------------------
    /**
     * Adiciona um cliente à lista de conectados. CORREÇÃO BUG 1: se já existir
     * uma sessão com o mesmo MAC, desconecta a sessão antiga antes de registrar
     * a nova, evitando duplicatas na tabela.
     *
     * @param mac endereço MAC do cliente
     * @param handler handler de comunicação da nova sessão
     */
    public void adicionarCliente(String mac, ComunicacaoClienteServer handler) {
        String macNormalizado = mac.toUpperCase();
        ComunicacaoClienteServer sessaoAnterior = clientesConectadosMap.put(macNormalizado, handler);
        if (sessaoAnterior != null) {
            System.out.println("[ControleBase] Sessão duplicada detectada para MAC: " + macNormalizado
                    + ". Encerrando sessão anterior.");
            try {
                sessaoAnterior.desconectarCliente(1);
            } catch (Exception ex) {
                System.out.println("[ControleBase] Erro ao encerrar sessão anterior: " + ex.getMessage());
            }
        }
    }

    /**
     * Remove um cliente pelo MAC.
     *
     * @param mac endereço MAC do cliente a remover
     * @return true se existia e foi removido
     */
    public boolean removerCliente(String mac) {
        return clientesConectadosMap.remove(mac.toUpperCase()) != null;
    }

    /**
     * Remove um cliente pelo MAC apenas se o handler for o mesmo.
     * Isso evita que uma sessão antiga remova a nova sessão da lista.
     *
     * @param mac endereço MAC do cliente a remover
     * @param handler a instância do handler que está solicitando a remoção
     * @return true se foi removido
     */
    public boolean removerClienteSeIgual(String mac, ComunicacaoClienteServer handler) {
        return clientesConectadosMap.remove(mac.toUpperCase(), handler);
    }

    /**
     * Retorna um snapshot (cópia) thread-safe da lista de clientes conectados.
     * Use este método para iterar sem risco de ConcurrentModificationException.
     */
    public synchronized ArrayList<ComunicacaoClienteServer> getClistesConectadosLista() {
        return new ArrayList<>(clientesConectadosMap.values());
    }

    /**
     * @deprecated Use adicionarCliente(mac, handler) para garantir unicidade
     * por MAC.
     */
    @Deprecated
    public void setClistesLista(ComunicacaoClienteServer valor) {
        if (valor != null && valor.getonformacaoesCliente() != null) {
            adicionarCliente(valor.getonformacaoesCliente().getMacAddres(), valor);
        }
    }

    public void clienteListaInstaciar() {
        this.clientesConectadosMap.clear();
    }

    public void atualizarListadeConectados() {
        for (ComunicacaoClienteServer cliente : getClistesConectadosLista()) {
            String macConectado = cliente.getonformacaoesCliente().getMacAddres();
            for (infomacaoMaquinas info : BaseCadastradaInfoMaquinas) {
                if (macConectado.equalsIgnoreCase(info.getMacAddres())) {
                    if (cliente.getonformacaoesCliente().getBancada().contains("==")) {
                        cliente.verificaClienteBanco(macConectado);
                    }
                }
            }
        }
    }

//    public ControleBase() {
//        this.intanciarArray();
//    }
//
//    private Boolean intanciarArray() {
//        this.setBaseCadastradaInfoMaquinas(new ArrayList<>());
//        return true;
//    }
//
//    public Boolean importarDados() {
//        List maquinasLista = Servicos.Instancias.getExecucaoAtividadesInstacia().lerTxt(configs.configuracao.localBase);
//
//        for (int i = 0; i < maquinasLista.size(); i++) {
//            infomacaoMaquinas info = new infomacaoMaquinas();
//            String maquinaLinha = maquinasLista.get(i).toString();
//
//            if (!maquinaLinha.contains("#")) {
//                String barras = "";
//                int intervaloInicio = 0;
//                for (int letra = 0; letra < maquinaLinha.length(); letra++) {
//
//                    switch (barras.length()) {
//                        case 0:
//                            info.setMacAddres(maquinaLinha.substring(intervaloInicio, letra));
//                            break;
//
//                        case 1:
//                            info.setBancada(maquinaLinha.substring(intervaloInicio, letra));
//                            break;
//
//                        case 2:
//                            info.setPosicao(maquinaLinha.substring(intervaloInicio, letra));
//                            break;
//
//                        case 3:
//                            info.setSetor(maquinaLinha.substring(intervaloInicio, letra));
//                            break;
//                        case 4:
//                            info.setTombo(maquinaLinha.substring(intervaloInicio, letra));
//                            break;
//
//                        default:
//                            break;
//                    }
//
//                    if (maquinaLinha.substring(letra, letra + 1).equalsIgnoreCase("|")) {
//                        barras = barras + "|";
//                        intervaloInicio = letra + 1;
//                    }
//                }
//                BaseCadastradaInfoMaquinas.add(info);
//            }
//        }
//        //imprimirArray();
//        return true;
//    }
//
//    public void cadastrar(String Mac, String bancada, String posicao, String tombo, String setor) {
//        List maquinasLista = Servicos.Instancias.getExecucaoAtividadesInstacia().lerTxt(configs.configuracao.localBase);
//        boolean naoContemMacCadastrado = true;
//        String linha;
//        String mensagem = "Cadastrado com sucesso!";
//        for (int i = 0; i < maquinasLista.size(); i++) {
//            linha = maquinasLista.get(i).toString();
//            if (linha.contains(Mac)) {
//                mensagem = "Máquina já cadastrada!";
//                naoContemMacCadastrado = false;
//            } else if (linha.contains(bancada) && linha.contains(posicao)) {
//                mensagem = "Já existe uma máquina cadastrada:" + bancada + " " + posicao;
//                naoContemMacCadastrado = false;
//            }
//
//        }
//        if (naoContemMacCadastrado && Servicos.Instancias.getExecucaoAtividadesInstacia().escreverTxtBase(Mac, bancada, posicao, tombo, setor)) {
//            JOptionPane.showMessageDialog(null, mensagem);
//        } else {
//            JOptionPane.showMessageDialog(null, "Erro ao cadastrar!\n" + mensagem);
//        }
//    }
//
//    public void imprimirArray() {
//        for (infomacaoMaquinas info : BaseCadastradaInfoMaquinas) {
//
//            System.out.println("mac:" + info.getMacAddres());
//            System.out.println("Bancada:" + info.getBancada());
//            System.out.println("Posicao:" + info.getPosicao());
//            System.out.println("Setor:" + info.getSetor());
//            System.out.println("");
//        }
//    }
//
//    public void setBaseCadastradaInfoMaquinas(ArrayList<infomacaoMaquinas> infoMaquinas) {
//        this.BaseCadastradaInfoMaquinas = infoMaquinas;
//    }
//
//    public ArrayList<infomacaoMaquinas> getBaseCadastradaInfoMaquinas() {
//        return BaseCadastradaInfoMaquinas;
//    }
//
//    public synchronized ArrayList<ComunicacaoClienteServer> getClistesConectadosLista() {
//        return clistesLista;
//    }
//
//    public void setClistesLista(ComunicacaoClienteServer valor) {
//        this.clistesLista.add(valor);
//    }
//
//    public void clienteListaInstaciar() {
//        this.clistesLista = new ArrayList<>();
//    }
//
//    public void atualizarListadeConectados() {
//        for (int i = 0; i < this.clistesLista.size(); i++) {
//            for (int j = 0; j < BaseCadastradaInfoMaquinas.size(); j++) {
//                String macConectado = this.clistesLista.get(i).getonformacaoesCliente().getMacAddres();
//                String macCadastradoBase = this.BaseCadastradaInfoMaquinas.get(j).getMacAddres();
//
//                if (macConectado.equalsIgnoreCase(macCadastradoBase)) {
//                    if (this.clistesLista.get(i).getonformacaoesCliente().getBancada().contains("==")) {
//
//                        this.clistesLista.get(i).verificaClienteBanco(macConectado);
//                    }
//                }
//            }
//
//        }
//
//    }

}
