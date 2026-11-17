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

    /**
     * Atualiza a tabela de clientes conectados.
     * CORREÇÃO BUG 4: executado dentro de SwingUtilities.invokeLater() para
     * garantir que todas as operações de UI ocorram na Event Dispatch Thread (EDT).
     */
    public void tabelaConectados() {
        // Captura o snapshot thread-safe antes de entrar na EDT
        final ArrayList<ComunicacaoClienteServer> snapshot = Instancias.getControlebaseIntancia().getClistesConectadosLista();

        javax.swing.SwingUtilities.invokeLater(() -> {
            DefaultTableModel model;
            model = (DefaultTableModel) Iniciar.painel.tableInterface.getModel();
            model.setNumRows(0);

            Iniciar.painel.tableInterface.getColumnModel().getColumn(4).setCellRenderer(labelPersonalizado());
            Iniciar.painel.tableInterface.getColumnModel().getColumn(8).setCellRenderer(labelPersonalizado());

            ArrayList<ComunicacaoClienteServer> ordenar = new ArrayList<>(snapshot);

            // Ordenar a lista pela bancada, depois posição
            Collections.sort(ordenar, new Comparator<ComunicacaoClienteServer>() {
                @Override
                public int compare(ComunicacaoClienteServer o1, ComunicacaoClienteServer o2) {
                    int compareBancada = o1.getonformacaoesCliente().getBancada().compareTo(o2.getonformacaoesCliente().getBancada());
                    if (compareBancada != 0) {
                        return compareBancada;
                    }
                    return o1.getonformacaoesCliente().getPosicao().compareTo(o2.getonformacaoesCliente().getPosicao());
                }
            });

            for (ComunicacaoClienteServer cliente : ordenar) {
                String bancada = cliente.getonformacaoesCliente().getBancada();
                JLabel label = new JLabel(bancada);
                JLabel labelVideo = new JLabel();

                label.setOpaque(true);
                labelVideo.setOpaque(true);
                label.setBackground(atrbuir_cor(bancada));

                if (cliente.getonformacaoesCliente().getTransmissaoAtiva()) {
                    labelVideo.setBackground(configs.configuracao.VIDEO_CONNECTED);
                } else {
                    labelVideo.setBackground(configs.configuracao.VIDEO_DECCONNECTED);
                }

                model.addRow(new Object[]{
                    cliente.getonformacaoesCliente().getNomeCliete(),
                    cliente.getonformacaoesCliente().getMacAddres(),
                    cliente.getonformacaoesCliente().getClientesocket().getInetAddress(),
                    cliente.getonformacaoesCliente().getSetor(),
                    label,
                    cliente.getonformacaoesCliente().getPosicao(),
                    cliente.getonformacaoesCliente().getStatusCliente(),
                    cliente.getonformacaoesCliente().getReceberComando(),
                    labelVideo
                });

                ExecucaoAtividadesTela.adicionarTabPannerComunicacao(cliente.getonformacaoesCliente());
            }

            ExecucaoAtividadesTela.qtdConectados(ordenar.size() + "");
        }); // fim invokeLater
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

    /**
     * Remove o cliente da lista de conectados e atualiza a tabela.
     * CORREÇÃO BUG 4: usa removerCliente() do ConcurrentHashMap (atômico)
     * em vez de iterar por índice num ArrayList, eliminando o risco de
     * ConcurrentModificationException e salto de elementos.
     */
    public synchronized Boolean removerClienteConexaoTable(String mac) {
        boolean removido = Instancias.getControlebaseIntancia().removerCliente(mac);
        ExecucaoAtividadesTela.removerTabPanne(mac);
        tabelaConectados();
        return true; // sempre retorna true (tabela já foi atualizada)
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
