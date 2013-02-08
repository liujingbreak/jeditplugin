package org.liujing.ironsword.lang;

import java.io.*;
import java.util.*;
import java.util.logging.*;

public class BaseLanguageModel{
    private static Logger log = Logger.getLogger(BaseLanguageModel.class.getName());
    private int startLine;
    private int endLine;
    private int startOffset;
    private int endOffset;
    public BaseLanguageModel(){}
    
    /** get startLine
     @return startLine
    */
    public int getStartLine(){
        return startLine;
    }

    /** set startLine
     @param startLine startLine
    */
    public void setStartLine(int startLine){
        this.startLine = startLine;
    }

    /** get endLine
     @return endLine
    */
    public int getEndLine(){
        return endLine;
    }

    /** set endLine
     @param endLine endLine
    */
    public void setEndLine(int endLine){
        this.endLine = endLine;
    }

    /** get startOffset
     @return startOffset
    */
    public int getStartOffset(){
        return startOffset;
    }

    /** set startOffset
     @param startOffset startOffset
    */
    public void setStartOffset(int startOffset){
        this.startOffset = startOffset;
    }

    /** get endOffset
     @return endOffset
    */
    public int getEndOffset(){
        return endOffset;
    }

    /** set endOffset
     @param endOffset endOffset
    */
    public void setEndOffset(int endOffset){
        this.endOffset = endOffset;
    }

    public static String indentStr(int level, String src){
        try{
            StringWriter sw = new StringWriter();
            PrintWriter indentp = new PrintWriter(sw);
            for(int i = 0; i< level; i++)
                indentp.write("\t");
            indentp.close();
            String indent = sw.toString();
            
            sw = new StringWriter();
            PrintWriter p = new PrintWriter(sw);
            BufferedReader reader = new BufferedReader(new StringReader(src));
            int chr = reader.read();
            boolean lineStart = true;
            while(chr != -1){
                if(lineStart){
                    p.print(indent);
                    lineStart = false;
                }
                if(chr == '\n')
                    lineStart = true;
                p.print((char)chr);
                chr = reader.read();
            }
            
            p.close();
            return sw.toString();
        }catch(IOException e){
            log.log(Level.SEVERE, "", e);
            return src;
        }
    }
}
