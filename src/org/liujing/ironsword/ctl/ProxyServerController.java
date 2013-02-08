package org.liujing.ironsword.ctl;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.logging.*;
import java.sql.*;
import javax.servlet.http.*;
import javax.servlet.*;
import java.net.*;
import java.util.zip.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.*;
import org.eclipse.jetty.util.thread.*;
import org.liujing.ironsword.*;
import java.util.concurrent.atomic.*;

public class ProxyServerController{
    private static Logger log = Logger.getLogger(ProxyServerController.class.getName());
    protected static Server server;
    private static int PORT_NUM = 19817;
    
    public static boolean startServer()throws Exception{

        
    if(server == null){
            server = new Server(PORT_NUM);
            server.setThreadPool(new QueuedThreadPool(6));
            //ResourceHandler resource_handler = new ResourceHandler();
            //resource_handler.setDirectoriesListed(true);
            //resource_handler.setWelcomeFiles(new String[]{ "index.html" });
            //resource_handler.setResourceBase("web");
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);
            
            context.addServlet(new ServletHolder(
                new ProxyServlet()),"/*");
            
            HandlerList handlers = new HandlerList();
            //handlers.addHandler(resource_handler);
            handlers.addHandler(context);
            server.setHandler(handlers);
        }
        if(server.isStopped() ||  server.isFailed()){
            server.start();
            System.out.println("server is started in 19817");
            return true;
        }else{
            log.info("web server has already started");
            return false;
        }
        //server.join();
    }
    
    public static void stopServer()throws Exception{
        if(server != null && server.isStarted()){
            server.stop();
            System.out.println("server is stopped");
        }
    }
    
    public static void testRequest(){
        log.info("test request >>>>");
        List<String[]> headers = new ArrayList();
        
        //headers.add(new String[]{"Cookie","BAIDUID=F790307483E1708500096B0186C89319:FG=1"});
        headers.add(new String[]{"Host","qacand.successfactors.com"});
        //headers.add(new String[]{"Host","www.yahoo.com"});
        headers.add(new String[]{"Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"});
        headers.add(new String[]{"Accept-Charset","ISO-8859-1,utf-8;q=0.7,*;q=0.3"});
        headers.add(new String[]{"Proxy-Connection","keep-alive"});
        headers.add(new String[]{"Accept-Language","en-US,en;q=0.8"});
        headers.add(new String[]{"User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"});
        headers.add(new String[]{"Cache-Control","max-age=0"});
        headers.add(new String[]{"Accept-Encoding","gzip,deflate,sdch"});
        ResponseData res = delegateRequest("GET", "https://qacand.successfactors.com/login", headers, null);
        
    }
    
    public static void _testRequest(){
        try{
        HttpURLConnection conn = createConnection("GET", "http://www.baidu.com");
        conn.addRequestProperty("Cookie","BAIDUID=F790307483E1708500096B0186C89319:FG=1");
        conn.addRequestProperty("Host","www.baidu.com");
        conn.addRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.addRequestProperty("Accept-Charset","ISO-8859-1,utf-8;q=0.7,*;q=0.3");
        conn.addRequestProperty("Proxy-Connection","keep-alive");
        conn.addRequestProperty("Accept-Language","en-US,en;q=0.8");
        conn.addRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        conn.addRequestProperty("Cache-Control","max-age=0");
        conn.addRequestProperty("Accept-Encoding","gzip,deflate,sdch");
        int resLen = conn.getContentLength();
        int status = 0;
        log.info("response content len: "+ resLen);
        if(resLen > 0){
            status = conn.getResponseCode();
            log.info("status="+ status);
        }
        
        StringBuilder resHeaderBuf = new StringBuilder();
        
        Map<String, List<String>> responseHeaders = conn.getHeaderFields();
        
        for( Map.Entry<String, List<String>> header: responseHeaders.entrySet()){
            resHeaderBuf.setLength(0);
            String name = header.getKey();
            if(name == null)
                continue;
            List<String> values = header.getValue();
            int i= 0;
            for(String v : values){
                if(i > 0)
                    resHeaderBuf.append("; ");
                resHeaderBuf.append(v);
                i++;
            }
            log.info("response header: "+name+ "="+ resHeaderBuf.toString());
            //res.addHeader(name, resHeaderBuf.toString());
        }
        FileOutputStream out = new FileOutputStream("ProxyServer-test-out.dat",false);
        
        if(resLen > 0){
            InputStream replyContent = conn.getInputStream();
            if(replyContent != null){
                copyStream(out, replyContent);
            }
        }
        out.close();
        
        }catch(Exception e){
            throw new IronException("", e);
        }
    }
    
    public static ResponseData delegateRequest(String method, String address,
        List<String[]> header, InputStream requestContent){
        try{
            log.info(" ---- Thread name ---- "+ Thread.currentThread());
            HttpURLConnection conn = createConnection(method, address);
            for(String[] h: header){
                conn.addRequestProperty(h[0], h[1]);
                log.info(h[0] + "="+ h[1]);
            }
            
            if(requestContent != null){
                OutputStream reqOut = conn.getOutputStream();
                copyStream(reqOut, requestContent);
            }
      
            int resLen = conn.getContentLength();
            int status = 0;
            log.info("response content len: "+ resLen);
            //if(resLen > 0){
                status = conn.getResponseCode();
                log.info("status="+ status);
            //}
            
            StringBuilder resHeaderBuf = new StringBuilder();
            
            Map<String, List<String>> responseHeaders = conn.getHeaderFields();
            List<String[]> replyHeaders = new ArrayList();
            for( Map.Entry<String, List<String>> resheader: responseHeaders.entrySet()){
                resHeaderBuf.setLength(0);
                String name = resheader.getKey();
                if(name == null)
                    continue;
                List<String> values = resheader.getValue();
                int i= 0;
                for(String v : values){
                    if(i > 0)
                        resHeaderBuf.append("; ");
                    resHeaderBuf.append(v);
                    i++;
                }
                log.info("response header: "+name+ "="+ resHeaderBuf.toString());
                replyHeaders.add(new String[]{name, resHeaderBuf.toString()});
                //res.addHeader(name, resHeaderBuf.toString());
            }
            
            InputStream replyContent = conn.getInputStream();
            String cntEncoding = conn.getContentEncoding();
            if(cntEncoding != null && cntEncoding.contains("gzip"))
                replyContent = new GZIPInputStream(replyContent);
            //ByteArrayOutputStream out = new ByteArrayOutputStream();
            FileOutputStream out = new FileOutputStream("test-proxy-response.txt", true);
            
                
                if(replyContent != null){
                    int copied = copyStream(out, replyContent);
                    log.info("response bytes: "+ copied);
                }
            
            out.close();
            
            return new ResponseData(null, replyHeaders,status);
        }catch(Exception e){
            log.severe("method: "+ method + ", URL: "+ address);
            throw new IronException("", e);
        }
    }
    
    private static class ResponseData{
        InputStream dataIn;
        List<String[]> headers;
        int statusCode;;
        
        public ResponseData(byte[] data, List<String[]> responseHeaders, 
            int statusCode){
            if(data != null)
                this.dataIn = new ByteArrayInputStream(data);
            this.headers = responseHeaders;
            this.statusCode = statusCode;
        }
        
        
        public int statusCode(){
            return statusCode;
        }
        
        public InputStream responseContent(){
            return this.dataIn;
        }
        
        public List<String[]> getHeaders(){
            return headers;
        }
    }
    
    private static Map<String, HeaderValueReplacer> headerActions = new HashMap();
    private static HostHeaderReplacer hostReplacer = new HostHeaderReplacer();
    private static DefaultHeaderReplacer noChangeReplacer = new DefaultHeaderReplacer();
    static{
        
        //headerActions.put("Referer", hostReplacer);
        //headerActions.put("Host", hostReplacer);
    }
    
    private static String checkReplaceHeader(String headerName, String headerValue){
        HeaderValueReplacer r = headerActions.get(headerName);
        if(r == null)
            r = noChangeReplacer;
        return r.value(headerName, headerValue);
    }
    
    private interface HeaderValueReplacer{
        public String value(String name, String origValue);
    }
    
    private static class DefaultHeaderReplacer implements HeaderValueReplacer{
        public String value(String name, String origValue){
            return origValue;
        }
    }
    
    static Pattern localhostPat = Pattern.compile("\\Qlocalhost:"+ PORT_NUM + "\\E");
    
    private static class HostHeaderReplacer implements HeaderValueReplacer{
        public String value(String name, String origValue){
            return replaceHost(origValue);
        }
    }
    
    private static String replaceHost(String content){
        //return localhostPat.matcher(content).replaceAll("www.baidu.com");
        return content;
    }
    
    public static class ProxyServlet extends HttpServlet{
        private static AtomicInteger count = new AtomicInteger(0);
        
        protected void service( HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException{
            try{
                log.info("+++++ Thread name ++++ "+ Thread.currentThread());
            String url, fullAddress = req.getRequestURL().toString();
            fullAddress = replaceHost(fullAddress);
            final List<String[]> requestHeaders = new ArrayList();
            String q = req.getQueryString();
            if(q != null)
                fullAddress += "?" + q;
            final String fAddress = fullAddress;
            log.info(count.incrementAndGet() + "---------- proxying -------------\n\t"+ fullAddress); 
            //fullAddress="http://www.baidu.com";
            HttpURLConnection conn = createConnection(req.getMethod(), fullAddress);
            final String method = req.getMethod();
            log.info(req.getMethod());
            Enumeration<String> headers = req.getHeaderNames();
            while(headers.hasMoreElements()){
                String name = headers.nextElement();
                String value = req.getHeader(name);
                log.info(name + "="+ value);
                //if("Cookie".equals(name))
                    //continue;
                //requestHeaders.add(new String[]{name, value});
                conn.addRequestProperty(name, checkReplaceHeader(name, value));
            }
            byte[] reqBytes = null;
            if(req.getContentLength() >0){
                log.info("request len:"+ req.getContentLength());
                InputStream reqContent = req.getInputStream();
                ByteArrayOutputStream reqContentByte = null;
                if(reqContent != null){
                    reqContentByte = new ByteArrayOutputStream();
                    copyStream(reqContentByte, reqContent);
                }
                reqBytes = reqContentByte.toByteArray();
            }
            final byte[] fbytes = reqBytes;
            //Thread t = new Thread(){
            //    public void run(){
            //    ResponseData resPack = delegateRequest(method, fAddress, requestHeaders, 
            //    fbytes == null? null: new ByteArrayInputStream(fbytes));
            //    }
            //};
            //t.start();
            
            
            log.info("reponse >>>>>>>>> ");
            
            int resLen = conn.getContentLength();
            int status = 0;
            log.info("response content len: "+ resLen);
            if(resLen > 0){
                status = conn.getResponseCode();
                log.info("status="+ status);
            }
            
            StringBuilder resHeaderBuf = new StringBuilder();
            
            Map<String, List<String>> responseHeaders = conn.getHeaderFields();
            
            for( Map.Entry<String, List<String>> header: responseHeaders.entrySet()){
                resHeaderBuf.setLength(0);
                String name = header.getKey();
                if(name == null)
                    continue;
                List<String> values = header.getValue();
                int i= 0;
                for(String v : values){
                    if(i > 0)
                        resHeaderBuf.append("; ");
                    resHeaderBuf.append(v);
                    i++;
                }
                log.info("response header: "+name+ "="+ resHeaderBuf.toString());
                res.addHeader(name, resHeaderBuf.toString());
            }
            
            //if(resLen > 0){
                InputStream replyContent = conn.getInputStream();
                if(replyContent != null){
                    copyStream(res.getOutputStream(), replyContent);
                }
                res.setStatus(status);
            //}
            
            
            }catch(SocketTimeoutException te){
                log.log(Level.WARNING,"timeout:", te);
                res.sendError(res.SC_REQUEST_TIMEOUT, te.toString());
                try{
                    stopServer();
                }catch(Exception se){}
            }catch(Exception e){
                log.log(Level.WARNING,"failed to proxy request", e);
                //throw new ServletException("", e);
                try{
                    stopServer();
                }catch(Exception se){}
            }
        }
        
        
        
    }
    
    
    private static HttpURLConnection createConnection(String method, String addr){
        try{
            URL url = new URL(addr);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(10000);
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setFollowRedirects(true);
            return connection;
        }catch(Exception ex){
            throw new IronException("can not proxy this address :"+ addr, ex);
        }
    }
    
    private static int copyStream(OutputStream out, InputStream in)throws IOException{
            
        byte[] bytes = new byte[2048];
        int readB = in.read(bytes);
        int len = 0;
        while(readB >= 0){
            out.write(bytes, 0, readB);
            readB = in.read(bytes);
            len += readB;
        }
        out.flush();
        out.close();
        return len;
    }
}
