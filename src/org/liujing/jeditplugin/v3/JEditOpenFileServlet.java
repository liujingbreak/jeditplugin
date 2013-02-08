package org.liujing.jeditplugin.v3;

import javax.servlet.http.*;
import java.util.logging.*;
import org.gjt.sp.jedit.*;
import javax.swing.SwingUtilities;

public class JEditOpenFileServlet extends HttpServlet{
    private final static Logger log = Logger.getLogger(JEditOpenFileServlet.class.getName());
    
    protected  void	doPost(HttpServletRequest req, HttpServletResponse resp){
        final String filePath = req.getParameter("openfile");
        log.fine("to open: "+ filePath);
        SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    jEdit.openFile(jEdit.getActiveView(), filePath);
                }
        });
    }
}
