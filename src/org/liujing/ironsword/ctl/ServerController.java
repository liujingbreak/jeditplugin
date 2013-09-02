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
            
            WebAppContext yuiCtx = new WebAppContext();
            yuiCtx.setResourceBase(yui);
            yuiCtx.setContextPath("/yui");
            yuiCtx.setParentLoaderPriority(true);
            
            
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
            handlers.addHandler(yuiCtx);
            
            handlers.addHandler(context);
            handlers.addHandler(jsContext);
            server.setHandler(handlers);
            add3rdPartyContext(handlers);
        }
        if(server.isStopped() ||  server.isFailed()){
            server.start();
            return true;
        }else{
            log.info("web server has already started");
            return false;
        }
    }
    
    private static void add3rdPartyContext(ContextHandlerCollection handlers){
        String angular = System.getProperty("angularJS");
        if(angular != null && angular.trim().length()>0){
            WebAppContext angularCtx = new WebAppContext();
            angularCtx.setResourceBase(angular);
            angularCtx.setContextPath("/angular");
            angularCtx.setParentLoaderPriority(true);
            handlers.addHandler(angularCtx);
        }
    }
    
    public static void stopWebServer()throws Exception{
        if(server != null && server.isStarted()){
            server.stop();
            server = null;
        }
    }
}
