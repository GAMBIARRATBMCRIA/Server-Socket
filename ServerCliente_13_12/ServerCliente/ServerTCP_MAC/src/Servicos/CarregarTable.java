/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servicos;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class CarregarTable {
    
    private DefaultTableCellRenderer labelPersonalizado() {
        // Crie um renderizador personalizado para renderizar JLabels
        DefaultTableCellRenderer labelRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                // Chame a implementação padrão para inicializar os componentes de renderização
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Se o valor for um JLabel, use-o diretamente
                if (value instanceof JLabel) {
                    return (JLabel) value;
                } else {
                    // Se não, retorne um JLabel com o texto do valor
                    JLabel label = new JLabel(value.toString());
                    return label;
                }
            }
        };
        
        return labelRenderer;
    }

    public synchronized void tabelaConectados() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException ex) {
            Logger.getLogger(CarregarTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        DefaultTableModel model;
        model = (DefaultTableModel) Iniciar.painel.tableInterface.getModel();
        // Preenche a tabela com os dados do vetor
        model.setNumRows(0);

        // Defina o renderizador personalizado para a coluna que deve exibir JLabels
        Iniciar.painel.tableInterface.getColumnModel().getColumn(4).setCellRenderer(labelPersonalizado());
        ArrayList<ComunicacaoClienteServer> ordenar = new ArrayList<>(Instancias.getControlebaseIntancia().getClistesConectadosLista());

        // Ordenar a lista original com base na string da banda
        Collections.sort(ordenar, new Comparator<ComunicacaoClienteServer>() {
            @Override
            public int compare(ComunicacaoClienteServer o1, ComunicacaoClienteServer o2) {
                // Comparar bancadas primeiro
                int compareBancada = o1.getonformacaoesCliente().getBancada().compareTo(o2.getonformacaoesCliente().getBancada());
                
                if (compareBancada != 0) {
                    return compareBancada;
                }
                // Se as bancadas forem iguais, compare os PCs
                return o1.getonformacaoesCliente().getPosicao().compareTo(o2.getonformacaoesCliente().getPosicao());
            }
        });

        // Exibir a lista ordenada
        for (ComunicacaoClienteServer cliente : ordenar) {
            
            String bancada = cliente.getonformacaoesCliente().getBancada();
            JLabel label = new JLabel(bancada);
            label.setOpaque(true);
            label.setBackground(atrbuir_cor(bancada));
            
            model.addRow(new Object[]{
                cliente.getonformacaoesCliente().getNomeCliete(),
                cliente.getonformacaoesCliente().getMacAddres(),
                cliente.getonformacaoesCliente().getClientesocket().getInetAddress(),
                cliente.getonformacaoesCliente().getSetor(),
                label,
                cliente.getonformacaoesCliente().getPosicao(),
                cliente.getonformacaoesCliente().getStatusCliente(),
                cliente.getonformacaoesCliente().getReceberComando()
            });
            
            ExecucaoAtividadesTela.adicionarTabPanner(cliente.getonformacaoesCliente());
        }
        
        ExecucaoAtividadesTela.qtdConectados(ordenar.size() + "");
        
    }
    
    public Color atrbuir_cor(String bancada) {
        String bc = bancada;
        // System.out.println("Att cor bc:" + bc);
        if (bc.equalsIgnoreCase("B01")) {
            return configs.configuracao.B01;
        } else if (bc.equalsIgnoreCase("B02")) {
            return configs.configuracao.B02;
        } else if (bc.equalsIgnoreCase("B03")) {
            return configs.configuracao.B03;
        } else if (bc.equalsIgnoreCase("B04")) {
            return configs.configuracao.B04;
        } else if (bc.equalsIgnoreCase("B05")) {
            return configs.configuracao.B05;
        } else if (bc.equalsIgnoreCase("B06")) {
            return configs.configuracao.B06;
        } else if (bc.equalsIgnoreCase("B07")) {
            return configs.configuracao.B07;
        } else if (bc.equalsIgnoreCase("B08")) {
            return configs.configuracao.B08;
        } else if (bc.equalsIgnoreCase("B09")) {
            return configs.configuracao.B09;
        } else if (bc.equalsIgnoreCase("B10")) {
            return configs.configuracao.B10;
        } else if (bc.equalsIgnoreCase("B11")) {
            return configs.configuracao.B11;
        } else if (bc.equalsIgnoreCase("B12")) {
            return configs.configuracao.B12;
        } else {
            return configs.configuracao.B00;
        }
        
    }
    
    public synchronized Boolean removerClienteConexaoTable(String mac) {
        boolean removido = false;
        
        for (int i = 0; i < Instancias.getControlebaseIntancia().getClistesConectadosLista().size(); i++) {
            // System.out.println("tamanho1 (CarregarTable):" + Instancias.getControlebaseIntancia().getClistesConectadosLista().size());
            if (mac.equalsIgnoreCase(Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().getMacAddres())) {
                Instancias.getControlebaseIntancia().getClistesConectadosLista().remove(i);

                //ExecucaoAtividadesTela.removerTabPanne(mac);
                ExecucaoAtividadesTela.removerTabPanne(mac);
                
                removido = true;
            }
        }
        tabelaConectados();
        //System.out.println("removido table");

        removido = true;
        
        return removido;
    }
    
    public synchronized void selecTodos(Boolean selecionartodos) {
        for (int i = 0; i < Instancias.getControlebaseIntancia().getClistesConectadosLista().size(); i++) {
            Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().setReceberComando(selecionartodos);
        }
        tabelaConectados();
    }
    
    public synchronized void setHabilitadoEnvio(Boolean habilitar, String mac) {
        System.out.println("habilitar:" + habilitar + " mac:" + mac);
        for (int i = 0; i < Instancias.getControlebaseIntancia().getClistesConectadosLista().size(); i++) {
            if (mac.equalsIgnoreCase(Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().getMacAddres())) {
                Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().setReceberComando(habilitar);
                
                System.out.println("MAC:" + Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().getMacAddres() + " habilitado:"
                        + Instancias.getControlebaseIntancia().getClistesConectadosLista().get(i).getonformacaoesCliente().getReceberComando());
                
            }
            
        }
        tabelaConectados();
    }
}
