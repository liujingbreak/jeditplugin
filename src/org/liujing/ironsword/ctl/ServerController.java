package org.liujing.ironsword.ctl;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.logging.*;
import java.sql.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.*;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.directwebremoting.servlet.DwrServlet;
import org.eclipse.jetty.webapp.*;
import org.liujing.ironsword.servlet.JSGlobalSetupServlet;

public class ServerController {
    private static Logger log = Logger.getLogger(ServerController.class.getName());
    protected static Server server;
    private static String CONFIG_FILE_PROP = "resList";
    
    public static boolean startWebServer()throws Exception{
        if(server == null){
            String base = System.getProperty("webres");
            String yui = System.getProperty("yui");
            
            server = new Server(19815);
            XmlConfiguration configPlus = new XmlConfiguration(
                new File(base+"/WEB-INF/jetty-plus.xml").toURI().toURL()
                );
            configPlus.configure(server);
            
            WebAppContext context0 = new WebAppContext();
            context0.setResourceBase(yui);
            context0.setContextPath("/yui");
            context0.setParentLoaderPriority(true);
            
            WebAppContext jsContext = new WebAppContext();
            jsContext.setResourceBase(base+"/js");
            jsContext.setContextPath("/js"+ JSGlobalSetupServlet.URL_TOKEN);
            jsContext.setParentLoaderPriority(true);
            
            
            WebAppContext context = new WebAppContext();
            //context.setParentLoaderPriority(true);
            //context.setDescriptor(base +"/WEB-INF/web.xml");
            context.setResourceBase(base);
            context.setContextPath("/");
            
            ContextHandlerCollection handlers = new ContextHandlerCollection();
            //HandlerList handlers = new HandlerList();
            handlers.addHandler(context0);
            handlers.addHandler(context);
            handlers.addHandler(jsContext);
            server.setHandler(handlers);
            
        }
        if(server.isStopped() ||  server.isFailed()){
            server.start();
            return true;
        }else{
            log.info("web server has already started");
            return false;
        }
    }
    
    public static void stopWebServer()throws Exception{
        if(server != null && server.isStarted()){
            server.stop();
            server = null;
        }
    }
}
