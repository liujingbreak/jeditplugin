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

public class ServerController {
    private static Logger log = Logger.getLogger(ServerController.class.getName());
    protected static Server server;
    private static String CONFIG_FILE_PROP = "resList";
    
    public static boolean startWebServer()throws Exception{
        if(server == null){
            server = new Server(19815);
            List<String> reses = new ArrayList();
            reses.add(System.getProperty("webres"));
            reses.add(System.getProperty("yui"));
            
            ResourceCollection resources = new ResourceCollection(reses.toArray(new String[0]));
            log.info(System.getProperty("yui"));
            log.info(System.getProperty("webres"));
            ResourceHandler resource_handler = new ResourceHandler();
            resource_handler.setDirectoriesListed(true);
            //resource_handler.setWelcomeFiles(new String[]{ "index.html" });
            resource_handler.setBaseResource(resources);
            
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            ResourceCollection servletRes = new ResourceCollection(
                new String[]{System.getProperty("webres")});
            context.setBaseResource(servletRes);
            setupServlet(context);
            
            HandlerList handlers = new HandlerList();
            handlers.addHandler(resource_handler);
            handlers.addHandler(context);
            
            server.setHandler(handlers);
        }
        if(server.isStopped() ||  server.isFailed()){
            server.start();
            return true;
        }else{
            log.info("web server has already started");
            return false;
        }
        //server.join();
    }
    
    private static void setupServlet(ServletContextHandler context){
        ServletHolder dwrHolder = new ServletHolder(new DwrServlet());
        StringBuilder classNames = new StringBuilder();
        classNames.append("org.liujing.ironsword.ctl.ProjectController,");
        classNames.append("org.liujing.ironsword.bean.DaoPagination,");
        classNames.append("org.liujing.ironsword.bean.PagingRequest,");
        classNames.append("org.liujing.ironsword.dao.ProjectDAO");
        classNames.append(",org.liujing.ironsword.dao.FileTree");
        classNames.append(",org.liujing.ironsword.dao.RootFolder");
        classNames.append(",org.liujing.ironsword.dao.SrcFile");
        classNames.append(",org.liujing.ironsword.bean.PagedVO");
        classNames.append(",org.liujing.ironsword.bean.TableStyleVO");
        classNames.append(",org.liujing.ironsword.bean.TableRowVO");
        classNames.append(",org.liujing.ironsword.ctl.FileScanController");
        dwrHolder.setInitParameter("classes", classNames.toString());
        dwrHolder.setInitParameter("debug", "true");
        dwrHolder.setInitParameter("generateDtoClasses", "");
        dwrHolder.setInitParameter("customConfigurator", "org.liujing.ironsword.ctl.DWRConfiguration");
        
        context.addServlet(dwrHolder, "/dwr/*");
        
    }
    
    private static Pattern resConfigPat = Pattern.compile("\\s*([^=\\s]+)\\s*=>\\s*([^=\\s]+)\\s*");
    
    private static ContextHandlerCollection loadResFolderConfig()throws IOException{
        //List<ContextHandler> folders = new ArrayList();
        ContextHandlerCollection collection = new ContextHandlerCollection();
        String configPath = System.getProperty(CONFIG_FILE_PROP);
        if(configPath != null){
            File configFile = new File(configPath);
            if(configFile.exists()){
                BufferedReader cr = new BufferedReader(new FileReader(configFile));
                String line = cr.readLine();
                while(line != null){
                    Matcher m = resConfigPat.matcher(line);
                    if(m.matches()){
                        //ContextHandler context = new ContextHandler();
                        //context.setContextPath("/"+m.group(1));
                        //context.setResourceBase(m.group(2));
                        log.info("map res: /"+ m.group(1) + " -> "+ m.group(2));
                        collection.addContext("/"+ m.group(1), m.group(2));
                    }else{
                        log.warning("invalid config line: "+ line);
                    }
                    line = cr.readLine();
                }
                cr.close();
            }
        }
        return collection;
    }
    
    public static void stopWebServer()throws Exception{
        if(server != null && server.isStarted())
            server.stop();
    }
}
