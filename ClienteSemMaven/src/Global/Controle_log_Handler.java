/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Global;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import javax.swing.JTextArea;

/**
 *
 * @author LAB_01
 */
public class Controle_log_Handler extends Handler {
    public JTextArea txt = telas.telaLog.textologs;
    //public Controle_log_Handler(telas.telaLog)
    @Override
    public void publish(LogRecord lr) {

       txt.append(getFormatter().format(lr));

    }

    @Override
    public void flush() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() throws SecurityException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
