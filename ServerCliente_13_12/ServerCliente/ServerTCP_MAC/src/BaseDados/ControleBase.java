package BaseDados;

import java.util.ArrayList;
import BaseDados.infomacaoMaquinas;
import Servicos.ComunicacaoClienteServer;
import java.util.List;
import javax.swing.JOptionPane;

public class ControleBase {

    private ArrayList<infomacaoMaquinas> BaseCadastradaInfoMaquinas;
    private ArrayList<ComunicacaoClienteServer> clistesLista;

    public ControleBase() {
        this.intanciarArray();
    }

    private Boolean intanciarArray() {
        this.setBaseCadastradaInfoMaquinas(new ArrayList<>());
        return true;
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
        //imprimirArray();
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
            JOptionPane.showMessageDialog(null, "Erro ao cadastrar!\n"+mensagem);
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

    public synchronized ArrayList<ComunicacaoClienteServer> getClistesConectadosLista() {
        return clistesLista;
    }

    public void setClistesLista(ComunicacaoClienteServer valor) {
        this.clistesLista.add(valor);
    }

    public void clienteListaInstaciar() {
        this.clistesLista = new ArrayList<>();
    }

    public void atualizarListadeConectados() {
        for (int i = 0; i < this.clistesLista.size(); i++) {
            for (int j = 0; j < BaseCadastradaInfoMaquinas.size(); j++) {
                String macConectado = this.clistesLista.get(i).getonformacaoesCliente().getMacAddres();
                String macCadastradoBase = this.BaseCadastradaInfoMaquinas.get(j).getMacAddres();

                if (macConectado.equalsIgnoreCase(macCadastradoBase)) {
                    if (this.clistesLista.get(i).getonformacaoesCliente().getBancada().contains("==")) {

                        this.clistesLista.get(i).verificaClienteBanco(macConectado);
                    }
                }
            }

        }

    }

}
