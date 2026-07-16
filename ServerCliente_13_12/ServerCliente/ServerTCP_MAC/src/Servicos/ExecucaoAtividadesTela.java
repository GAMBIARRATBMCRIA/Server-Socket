/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servicos;

import BaseDados.ClientesConectados;
import configs.comandosServerCliente;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author LAB01
 */
public class ExecucaoAtividadesTela {

    public synchronized static void publicarAvido(String aviso) {
        //Iniciar.painel.areaAviso.append(aviso + "\n");
    }

    public synchronized static void qtdConectados(String quantidade) {
        Iniciar.painel.labelConcAtivas.setText(quantidade);
    }

    public synchronized static void adicionarTabPanner(ClientesConectados infor) {
        if (!verificarExistenciadeTab(infor.getMacAddres())) {
            JLabel labelNome = new JLabel();
            JLabel LabelMac = new JLabel();
            JLabel labelSetor = new JLabel();
            JLabel labelBancada = new JLabel();
            JLabel labelPosicao = new JLabel();
            JButton botaoLimpar = new JButton();
            JPanel painelModelo = new JPanel();

            JProgressBar barraprogresso = infor.getBarraProgresso();
            barraprogresso.setStringPainted(true);
            barraprogresso.setForeground(Color.BLUE);

            painelModelo.setName(infor.getMacAddres());

            JTextArea textoAreaTablePanne = infor.getTextoArea();

            JScrollPane barraRolagem = new JScrollPane();

            barraRolagem.setViewportView(textoAreaTablePanne);

            labelNome.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
            labelNome.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            labelNome.setText("Nome:");

            LabelMac.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
            LabelMac.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            LabelMac.setText("MAC:" + infor.getMacAddres());

            labelSetor.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
            labelSetor.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            labelSetor.setText("Setor:" + infor.getSetor());

            labelBancada.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
            labelBancada.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            labelBancada.setText("Bancada:" + infor.getBancada());

            labelPosicao.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
            labelPosicao.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            labelPosicao.setText("Posição:" + infor.getPosicao());

            botaoLimpar.setText("Limpar");
            botaoLimpar.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    infor.getTextoArea().setText("");
                }
            });

            textoAreaTablePanne.setEditable(false);
            textoAreaTablePanne.setColumns(20);
            textoAreaTablePanne.setRows(5);
            textoAreaTablePanne.setBorder(javax.swing.BorderFactory.createTitledBorder("Respostas: " + infor.getMacAddres()));

            javax.swing.GroupLayout painelModeloLayout = new javax.swing.GroupLayout(painelModelo);
            painelModelo.setLayout(painelModeloLayout);
            painelModeloLayout.setHorizontalGroup(
                    painelModeloLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelModeloLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(painelModeloLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(barraRolagem)
                                            .addGroup(painelModeloLayout.createSequentialGroup()
                                                    .addComponent(labelNome, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGap(40, 40, 40)
                                                    .addComponent(LabelMac, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGap(40, 40, 40)
                                                    .addComponent(labelBancada, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGap(40, 40, 40)
                                                    .addComponent(labelPosicao, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 121, Short.MAX_VALUE)
                                                    .addComponent(botaoLimpar, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelModeloLayout.createSequentialGroup()
                                                    .addGap(0, 0, Short.MAX_VALUE)
                                                    .addComponent(barraprogresso, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addContainerGap())
            );
            painelModeloLayout.setVerticalGroup(
                    painelModeloLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelModeloLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(barraRolagem, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(painelModeloLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(labelBancada, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(LabelMac, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(labelPosicao, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(labelNome, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(botaoLimpar))
                                    .addGap(7, 7, 7)
                                    .addComponent(barraprogresso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addContainerGap())
            );

            //tablePanneDialogCliente.addTab("MAC:", painelModelo);
            barraprogresso.setVisible(false);
            barraprogresso.setValue(0);
            Iniciar.clientesConectados.tablePanneDialogCliente.add(infor.getBancada() + "_" + infor.getPosicao() + ":" + infor.getMacAddres(), painelModelo);
            //Iniciar.clientesConectados.tablePanneDialogCliente.getse
        }
    }

    public synchronized static Boolean verificarExistenciadeTab(String MAC) {
        Boolean tem = false;
        for (int i = 0; i < Iniciar.clientesConectados.tablePanneDialogCliente.getTabCount(); i++) {
            if (Iniciar.clientesConectados.tablePanneDialogCliente.getTitleAt(i).contains(MAC)) {
                tem = true;
                break;
            }
        }

        return tem;
    }

    public synchronized static void removerTabPanne(String Mac) {
        System.out.println("qtd:" + Iniciar.clientesConectados.tablePanneDialogCliente.getTabCount());

        for (int i = Iniciar.clientesConectados.tablePanneDialogCliente.getTabCount() - 1; i >= 0; i--) {
            JPanel painelRemover;
            Component component = Iniciar.clientesConectados.tablePanneDialogCliente.getComponentAt(i);

            if (component instanceof JPanel) {
                painelRemover = (JPanel) component;

                String tiulo = painelRemover.getName();

                if (tiulo.contains(Mac)) {
                    Iniciar.clientesConectados.tablePanneDialogCliente.removeTabAt(i);
                    //Instancias.getCarregarTableInstacia().tabelaConectados();
                }
            }
        }

    }

    public static void importarListaComandos() {
        JFileChooser fileChooser = new JFileChooser();

        List<String> array = new ArrayList<>();

        // Define o filtro para exibir apenas os tipos de arquivo desejados
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Arquivos de texto (.txt, .ps1, .bat)", "txt", "ps1", "bat", "cmd");
        fileChooser.setFileFilter(filter);
        // Abre o File Chooser para selecionar um arquivo
        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File arquivoSelecionado = fileChooser.getSelectedFile();

            String caminho = arquivoSelecionado.getAbsolutePath();
            array = Instancias.getExecucaoAtividadesInstacia().lerTxt(caminho);
            Iniciar.painel.areatextoExpandida.setText("");
            for (int i = 0; i < array.size(); i++) {
                Iniciar.painel.areatextoExpandida.append(array.get(i) + "\n");
            }
        }
    }

    public static void enviarArquivo(String lbMaquina) {

        JFileChooser fileChooser = new JFileChooser();
        // Abre o File Chooser para selecionar um arquivo
        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File arquivoSelecionado = fileChooser.getSelectedFile();
            String caminho = arquivoSelecionado.getAbsolutePath();
            //System.out.println("caminho:"+caminho);
            String nomeArquivo = arquivoSelecionado.getName();

            if (lbMaquina.startsWith("Bancada:")) {
                Instancias.getExecucaoAtividadesInstacia().enviarArquivo(caminho, "bc", lbMaquina.replaceAll("Bancada:", ""), nomeArquivo);
            } else if (lbMaquina.startsWith("Setor:")) {
                Instancias.getExecucaoAtividadesInstacia().enviarArquivo(caminho, "grupo", lbMaquina.replaceAll("Setor:", ""), nomeArquivo);
            } else {

                Instancias.getExecucaoAtividadesInstacia().enviarArquivo(caminho, "mac", lbMaquina.replaceAll("Mac:", ""), nomeArquivo);
            }

        }
    }

    public static void enviarMensagemTexto(String lbMaquina, JTextArea areaTexto) {
        if (lbMaquina.startsWith("Bancada:")) {
            Instancias.getExecucaoAtividadesInstacia().enviarMensagemTexto(areaTexto.getText(), "bc", lbMaquina.replaceAll("Bancada:", ""));
        } else if (lbMaquina.startsWith("Setor:")) {
            Instancias.getExecucaoAtividadesInstacia().enviarMensagemTexto(areaTexto.getText(), "grupo", lbMaquina.replaceAll("Setor:", ""));
        } else {
            Instancias.getExecucaoAtividadesInstacia().enviarMensagemTexto(areaTexto.getText(), "mac", lbMaquina.replaceAll("Mac:", ""));
        }
    }
    
    public static void autenticarNaRede(String login, String senha){
        comandosServerCliente.AutenticadorRede(login, senha);
    }

    public synchronized static void preencharServices(ArrayList<String[]> dados) {
        //ArrayList<String[]> dados1 = dados;

        DefaultTableModel modelo = (DefaultTableModel) Iniciar.painel.tableaServicos.getModel();
        modelo.setNumRows(0);
        // Adicionando os dados do ArrayList ao DefaultTableModel
        for (String[] linha : dados) {
            modelo.addRow(linha);
        }
    }
    
    public static void setHabilitarEnvioMaquina(Boolean habilitar, String mac){
        Instancias.getCarregarTableInstacia().setHabilitadoEnvio(habilitar, mac);
        
        System.out.println("Mac:"+mac+" habilitado para envio:"+habilitar);
    }
    
    public static void selectTablePane(String mac){
        for (int i = 0; i < Iniciar.clientesConectados.tablePanneDialogCliente.getTabCount(); i++) {
            
            if (Iniciar.clientesConectados.tablePanneDialogCliente.getTitleAt(i).contains(mac)) {
                Iniciar.clientesConectados.tablePanneDialogCliente.setSelectedIndex(i);
            }  
        }
        
    }
}
