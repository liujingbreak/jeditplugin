package org.liujing.ironsword.ctl;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.logging.*;
import java.sql.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.*;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.directwebremoting.servlet.DwrServlet;
import org.eclipse.jetty.webapp.*;
import org.liujing.ironsword.servlet.JSGlobalSetupServlet;

public class ServerController {
    private static Logger log = Logger.getLogger(ServerController.class.getName());
    protected static Server server;
    private static String CONFIG_FILE_PROP = "resList";
    
    //public static boolean _startWebServer()throws Exception{
    //    if(server == null){
    //        server = new Server(19815);
    //        List<String> reses = new ArrayList();
    //        reses.add(System.getProperty("webres"));
    //        reses.add(System.getProperty("yui"));
    //        
    //        ResourceCollection resources = new ResourceCollection(reses.toArray(new String[0]));
    //        ResourceHandler resource_handler = new ResourceHandler();
    //        resource_handler.setDirectoriesListed(true);
    //        resource_handler.setBaseResource(resources);
    //        
    //        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    //        context.setContextPath("/");
    //        ResourceCollection servletRes = new ResourceCollection(
    //            new String[]{System.getProperty("webres")});
    //        context.setBaseResource(servletRes);
    //        setupServlet(context);
    //        
    //        HandlerList handlers = new HandlerList();
    //        handlers.addHandler(resource_handler);
    //        handlers.addHandler(context);
    //        
    //        server.setHandler(handlers);
    //    }
    //    if(server.isStopped() ||  server.isFailed()){
    //        server.start();
    //        return true;
    //    }else{
    //        log.info("web server has already started");
    //        return false;
    //    }
    //    //server.join();
    //}
    
    public static boolean startWebServer()throws Exception{
        if(server == null){
            String base = System.getProperty("webres");
            String yui = System.getProperty("yui");
            
            server = new Server(19815);
            
            WebAppContext context0 = new WebAppContext();
            context0.setResourceBase(yui);
            context0.setContextPath("/yui");
            context0.setParentLoaderPriority(true);
            
            WebAppContext jsContext = new WebAppContext();
            jsContext.setResourceBase(base+"/js");
            jsContext.setContextPath("/js"+ JSGlobalSetupServlet.URL_TOKEN);
            jsContext.setParentLoaderPriority(true);
            
            
            WebAppContext context = new WebAppContext();
            context.setDescriptor(base +"/WEB-INF/web.xml");
            context.setResourceBase(base);
            context.setContextPath("/");
            context.setParentLoaderPriority(true);
            
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
    
    //private static void setupServlet(ServletContextHandler context){
    //    ServletHolder dwrHolder = new ServletHolder(new DwrServlet());
    //    String classNames = 
    //    "org.liujing.ironsword.ctl.ProjectController,"
    //    +"org.liujing.ironsword.bean.DaoPagination,"
    //    +"org.liujing.ironsword.bean.PagingRequest,"
    //    +"org.liujing.ironsword.dao.ProjectDAO"
    //    +",org.liujing.ironsword.dao.FileTree"
    //    +",org.liujing.ironsword.dao.RootFolder"
    //    +",org.liujing.ironsword.dao.SrcFile"
    //    +",org.liujing.ironsword.bean.PagedVO"
    //    +",org.liujing.ironsword.bean.TableStyleVO"
    //    +",org.liujing.ironsword.bean.TableRowVO"
    //    +",org.liujing.ironsword.ctl.FileScanController";
    //    dwrHolder.setInitParameter("classes", classNames);
    //    dwrHolder.setInitParameter("debug", "true");
    //    dwrHolder.setInitParameter("generateDtoClasses", "");
    //    dwrHolder.setInitParameter("customConfigurator", "org.liujing.ironsword.ctl.DWRConfiguration");
    //    
    //    context.addServlet(dwrHolder, "/dwr/*");
    //    
    //}
    
    
    
    public static void stopWebServer()throws Exception{
        if(server != null && server.isStarted()){
            server.stop();
            server = null;
        }
    }
}
