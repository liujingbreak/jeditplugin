package org.liujing.jeditplugin.v2;

import java.util.*;
import java.io.*;
import java.util.logging.*;
import org.gjt.sp.jedit.*;
import java.awt.datatransfer.*;
import org.gjt.sp.jedit.textarea.*;

public class MyPluginMenuActions{
    private static Logger log = Logger.getLogger(MyPluginMenuActions.class.getName());

    private static MyPluginMenuActions instance = new MyPluginMenuActions();
    public static String Line_sep;

    static{
        Line_sep = "\n";
    }

    public MyPluginMenuActions(){
    }

    public static MyPluginMenuActions getInstance(){
        return instance;
    }

    public void copyJavaClassCreationTemplate(String name){
        try{
            if(name == null)
                name = "JavaClassCreationTemplate01.txt";
            String str= textResourceToString("/" + name);
            JEditTextArea textArea = jEdit.getActiveView().getTextArea();
            Buffer buffer = jEdit.getActiveView().getBuffer();
            int start = textArea.getCaretPosition();
            buffer.insert(start, str);

            //Clipboard clip = jEdit.getActiveView().getToolkit().getSystemClipboard();
            //StringSelection data = new StringSelection(str);
            //clip.setContents(data, data);
        }catch(Exception e){
            log.log(Level.SEVERE, "", e);
        }
    }

    protected String textResourceToString(String path)throws IOException{
        InputStreamReader reader = new InputStreamReader(
            MyPluginMenuActions.class.getResourceAsStream(path));
        StringWriter writer = new StringWriter();
        int chr = reader.read();
        int lastChr = -1;
        while(chr != -1){
            if(chr != '\r' && chr != '\n'){
                writer.write(chr);
            }
            else if(chr == '\n'){
                writer.write(Line_sep);
            }
            lastChr = chr;
            chr = reader.read();
        }
        reader.close();
        return writer.toString();
    }
}
