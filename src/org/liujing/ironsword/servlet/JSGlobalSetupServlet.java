package org.liujing.ironsword.servlet;

import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.logging.*;

public class JSGlobalSetupServlet extends HttpServlet{
    final static Logger log = Logger.getLogger("JSGlobalSetupServlet");
    static Pattern underlinePat = Pattern.compile("_");
    public static int URL_TOKEN = new Random().nextInt();
    
    protected void doGet(HttpServletRequest req, HttpServletResponse resp){
        PrintWriter out = null;
        try{
            out = resp.getWriter();
            Locale locale = req.getLocale();
            resp.setContentType("application/x-javascript");
            out.write("var browser_locale = '");
            Matcher m = underlinePat.matcher(locale.toString());
            out.write(m.replaceAll("-"));
            out.write("';");
            out.println();
            System.out.println("access for locale: "+ locale);
            
            out.write("var urlToken = '");
            out.print(URL_TOKEN);
            out.write("';");
        }catch(IOException e){
            log.log(Level.SEVERE, "JSGlobalSetupServlet error encountered", e);
            out.write("//server message: ");
            out.write(e.getMessage());
            out.println();
            out.print("var browser_locale= 'en';");
        }finally{
            try{
                if(out != null)
                    out.flush();
            }catch(Exception e){
                
            }
        }
    }
}

