package liujing.magdownload.gwt.server;

import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;

public class DeferredImageServlet extends HttpServlet{
    protected  void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws IOException, ServletException {
        ServletOutputStream out = resp.getOutputStream();
        String path = req.getRealPath(req.getParameter("path"));
        System.out.println("---------------"+path);
        File imageFile = new File(path);
        InputStream in = new BufferedInputStream(new FileInputStream(imageFile));
        byte[] buf = new byte[10*1024];
        int len = in.read(buf);
        while(len > 0){
            out.write(buf, 0, len);
            try{
                Thread.sleep(500);
            }catch(InterruptedException e){

            }
            len = in.read(buf);
        }
        in.close();
    }
}