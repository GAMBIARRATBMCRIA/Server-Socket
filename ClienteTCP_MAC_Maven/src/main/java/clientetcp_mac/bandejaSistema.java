/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientetcp_mac;

import Global.InstanciasGlobais;
import Global.config;
import java.awt.Component;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.TrayIcon;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

/**
 *
 * @author LAB_01
 */
public class bandejaSistema extends Object {

    private TrayIcon trayIcon = null;

    private String status = "Iniciando conexao na porta:" + config.portaServidor;

//    public void abriBandeja() {
//        try {
//            if (SystemTray.isSupported()) {
//                // get the SystemTray instance
//                SystemTray tray = SystemTray.getSystemTray();
//
//                JPopupMenu menupopup = new JPopupMenu();
//
//                JMenuItem itemSair = new JMenuItem();
//                itemSair.setText("Sair");
//
//                itemSair.addMouseListener(new MouseAdapter() {
//                    @Override
//                    public void mouseClicked(MouseEvent e) {
//                        // Verifica se o clique foi com o botão direito do mouse
//                        if (SwingUtilities.isRightMouseButton(e)) {
//                            // Verifica se a tecla Shift estava pressionada
//                            if (e.isShiftDown()) {
//                                System.out.println("Shift e clique direito do mouse detectados.");
//                            } else {
//                                System.out.println("Clique direito do mouse detectado, sem Shift.");
//                            }
//                        } else {
//                            System.out.println("Clique com botão diferente do direito.");
//                        }
//                    }
//                });
//                menupopup.add(itemSair);
//
//                PopupMenu popupMenu = new PopupMenu();
//                for (Component menuItem : menupopup.getComponents()) {
//                    if (menuItem instanceof JMenuItem) {
//                        JMenuItem jMenuItem = (JMenuItem) menuItem;
//                        MenuItem popupMenuItem = new MenuItem(jMenuItem.getText());
//                        popupMenu.add(popupMenuItem);
//                    }
//                }
//
//                trayIcon = new TrayIcon(config.getImageIconMenu(), "Cliente", popupMenu);
//                tray.add(trayIcon);
//                trayIcon.setToolTip("Google Chrome:");
//
//            }
////            if (trayIcon != null) {
////                trayIcon.setImage(config.getImageIconMenu());
////                 System.out.println("imagem foi 1");
////            }
//        } catch (Exception e) {
//            System.err.println("Erro:");
//            e.printStackTrace();
//        }
//    }
    public void abriBandeja() {
        try {
            if (SystemTray.isSupported()) {
                // Obtém a instância do SystemTray
                SystemTray tray = SystemTray.getSystemTray();

                // Cria o menu pop-up
                PopupMenu popupMenu = new PopupMenu();

                // Cria o item do menu "Sair"
                MenuItem itemSair = new MenuItem("Sair");
                MenuItem itemLog = new MenuItem("Log Saída");

                itemLog.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        InstanciasGlobais.getTelaLogInstandia().setVisible(true);
                        popupMenu.remove(itemLog);
                    }
                });
                itemSair.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                       // popupMenu.add(itemLog);
                    }
                });
                popupMenu.add(itemSair);

                // Cria o TrayIcon e adiciona ao SystemTray
                trayIcon = new TrayIcon(config.getImageIconMenu(), "Cliente", popupMenu);
                trayIcon.setToolTip("Google Chrome:");
                tray.add(trayIcon);

                // Configura o evento de clique no TrayIcon
                trayIcon.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            if (e.isShiftDown() && e.isControlDown()) {
                                System.out.println("\n\n\nShift e clique direito do mouse, control e shift detectados.\n\n\n");
                                popupMenu.add(itemLog);
                            } else {
                                System.out.println("\n\n\nClique direito do mouse detectado, sem Shift.\n\n\n");
                            }
                        }
                    }
                });

            } else {
                System.out.println("SystemTray não suportado.");
            }
        } catch (Exception e) {
            System.err.println("Erro:");
            e.printStackTrace();
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        //this.status = status;
        this.getTrayIcon().setToolTip(status);
    }

    public TrayIcon getTrayIcon() {
        return trayIcon;
    }

}
