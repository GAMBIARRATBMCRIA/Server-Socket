/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servicos;


import ControleGlobal.configsControleGlobal;
import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;

import java.awt.TrayIcon;
import java.awt.SystemTray;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author LAB_01
 */
public class bandejaSistemaServer extends Object {

    public void abriBandeja(Telas.Tela_Principal telaPricipal) {
        System.out.println("Abrindo bandeja de sitema");
        TrayIcon trayIcon = null;
        if (SystemTray.isSupported()) {
            // get the SystemTray instance
            
            SystemTray tray = SystemTray.getSystemTray();
            // load an image
           // Image image = Toolkit.getDefaultToolkit().getImage(configsControleGlobal.caminhoImagemIcon);
            // create a action listener to listen for default action executed on the tray icon
            ActionListener listener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    System.out.println("Evento:"+ae.getActionCommand());
                    
                    if (ae.getActionCommand().equals("Expandir")) {
                        telaPricipal.setVisible(true);
                    }
                    if (ae.getActionCommand().equalsIgnoreCase("Minimizar")) {
                        telaPricipal.dispose();
                    }
                    //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            };
            // create a popup menu
            PopupMenu popup = new PopupMenu();
            // create menu item for the default action
            MenuItem intemExpandir = new MenuItem("Expandir");
            MenuItem itemMinimiza = new MenuItem("Minimizar");
            intemExpandir.addActionListener(listener);
            itemMinimiza.addActionListener(listener);
            popup.add(intemExpandir);
            popup.add(itemMinimiza);
            /// ... add other items
            // construct a TrayIcon
            trayIcon = new TrayIcon(configsControleGlobal.getImageIconMenu(), "Testando", popup);
            // set the TrayIcon properties
            trayIcon.addActionListener(listener);
            // ...
            // add the tray image
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println(e);
            }
            // ...
        } else {
            // disable tray option in your application or
            // perform other actions
            
        }
        // ...
        // some time later
        // the application state has changed - update the image
        if (trayIcon != null) {
            trayIcon.setImage(configsControleGlobal.getImageIconMenu());
        }
    }

}
