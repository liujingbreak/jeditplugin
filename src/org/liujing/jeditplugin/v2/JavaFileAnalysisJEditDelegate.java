package org.liujing.jeditplugin.v2;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import org.gjt.sp.jedit.*;
import java.awt.datatransfer.*;
import org.liujing.awttools.classview.Javaprint;
import org.liujing.jeditplugin.MyJeditPlugin;
import liujing.jedit.parser.*;
import org.liujing.parser.*;
import org.antlr.runtime.*;
import org.gjt.sp.jedit.textarea.*;

public class JavaFileAnalysisJEditDelegate implements JavaParserFieldDecHandler,
JavaParserMethodDecHandler
{

    private static Logger log = Logger.getLogger(JavaFileAnalysisJEditDelegate.class.getName());
    private static JavaFileAnalysisJEditDelegate instance;
    private JavaFileAnalysisTool tool;
    private JavaImportAutoComplete importComplete = new JavaImportAutoComplete();
    private long lastParsedTime = 0;
    private Buffer lastBuffer;
    private StringBuilder sb = new StringBuilder();
    private LinkedHashMap<String, JavaFileAnalysisTool> cacheParseResult
    = new LinkedHashMap();
    private static int CACHE_PARSE_SIZE = 5;

    static {
            instance = new JavaFileAnalysisJEditDelegate();
    }

    /**  construct JavaFileAnalysisJEditDelegate
    */
    protected JavaFileAnalysisJEditDelegate(){}

    public static JavaFileAnalysisJEditDelegate getInstance(){
        return instance;
    }

    private JavaFileAnalysisTool mightParse()throws Exception{

        Buffer buf = jEdit.getActiveView().getBuffer();

        tool = cacheParseResult.get(buf.getPath());
        if(buf.isDirty() || tool == null || buf.getLastModified() > tool.getTimeStamp()){
            log.fine("start to parse "+buf.getPath());
            tool = new JavaFileAnalysisTool();
            tool.parseJava(new StringReader(buf.getText(0, buf.getLength())));
            lastParsedTime = System.currentTimeMillis();
            lastBuffer = buf;
            cacheParseResult.put(buf.getPath(), tool);
            if(cacheParseResult.size() > CACHE_PARSE_SIZE ){
                Iterator it = cacheParseResult.keySet().iterator();
                it.next();
                it.remove();
            }
        }
        //todo fix the bug here:
        //if(lastBuffer != null && buf.getPath().equals(lastBuffer.getPath()) &&
        //    buf.getLastModified() <= lastParsedTime)
        //{
        //    log.fine("use cached parsing result");
        //    return tool;
        //}

        return tool;
    }

    public void copyFullClassName(){
        try{
            mightParse();
            Clipboard clip = jEdit.getActiveView().getToolkit().getSystemClipboard();
            StringSelection data = new StringSelection(tool.getMainClassFullName());
            clip.setContents(data, data);
        }catch(Exception e){
            log.log(Level.SEVERE, "", e);

        }
    }

    public void copyAllAssociatedTypes(){
        try{
            mightParse();
            StringBuilder buf = new StringBuilder();
            for(String type : tool.getAssociatedTypes()){
                buf.append(type);
                buf.append("\n");
            }
            Clipboard clip = jEdit.getActiveView().getToolkit().getSystemClipboard();
            StringSelection data = new StringSelection(buf.toString());
            clip.setContents(data, data);
        }catch(Exception e){
            log.log(Level.SEVERE, "", e);
        }
    }

    public void copyGeterSetter(){
        try{
            JEditTextArea textArea = jEdit.getActiveView().getTextArea();
            sb.setLength(0);
            for(Iterator<Selection> it = textArea.getSelectionIterator(); it.hasNext();){
                Selection sel = it.next();
                String text = jEdit.getActiveView().getBuffer().getText(sel.getStart(), sel.getEnd() - sel.getStart());
                log.fine("Selection:\n" + text);
                ANTLRReaderStream stream = new ANTLRReaderStream(new StringReader(text));
                JavaLexer lexer = new JavaLexer(stream);
                RemovableTokenStream tokens = new RemovableTokenStream(lexer);
                JavaParser p = new JavaParser(tokens);
                p.fieldDeclarations(this);
            }

            Clipboard clip = jEdit.getActiveView().getToolkit().getSystemClipboard();
            StringSelection data = new StringSelection(sb.toString());
            clip.setContents(data, data);
        }catch(Exception e){
            log.log(Level.SEVERE, "", e);
        }
    }

    public void insertDocumentation(){
        try{
            JEditTextArea textArea = jEdit.getActiveView().getTextArea();
            Buffer buffer = jEdit.getActiveView().getBuffer();
            int start = textArea.getCaretPosition();
            int len = Math.min(500, buffer.getLength() - start);
            String text = buffer.getText(start, len);

            ANTLRReaderStream stream = new ANTLRReaderStream(new StringReader(text));
            JavaLexer lexer = new JavaLexer(stream);
            RemovableTokenStream tokens = new RemovableTokenStream(lexer);
            JavaParser p = new JavaParser(tokens);
            sb.setLength(0);
            p.docFieldOrMethod(this);

            buffer.insert(start, sb.toString());
        }catch(Exception e){
            log.log(Level.SEVERE, "", e);
        }
    }

    public void onConstructDeclaration(String name, List<String> params, List<String> throwTypes)
    {

        sb.append("/**  construct ");
        sb.append(name);
        sb.append("\n");
        for(String paramName: params){
            sb.append("     @param ");
            sb.append(paramName);
            sb.append(" ");
            sb.append(paramName);
            sb.append("\n");
        }
        for(String throwType: throwTypes){
            sb.append("     @throws ");
            sb.append(throwType);
            sb.append(" if ");
            sb.append(throwType);
            sb.append(" occurs\n");
        }
        sb.append("    */");
    }

    public void onMethodDeclaration(String name,
        List<String> params, String returnType, List<String> throwTypes)
    {

        sb.append("/**  ");
        sb.append(name);
        sb.append("\n");
        for(String paramName: params){
            sb.append("     @param ");
            sb.append(paramName);
            sb.append(" ");
            sb.append(paramName);
            sb.append("\n");
        }
        if(returnType != null)
            sb.append("     @return " + returnType +"\n");
        for(String throwType: throwTypes){
            sb.append("     @throws ");
            sb.append(throwType);
            sb.append(" if ");
            sb.append(throwType);
            sb.append(" occurs\n");
        }
        sb.append("    */");
    }

    public void onFieldDec(String name, String type){
        sb.append("/**  ");
        sb.append(name);
        sb.append("    */");
        log.fine("name="+ name +" type="+ type);
    }

    public void onFieldDeclaration(String name, String type){

        //sb.append("    /** get ");
        //sb.append(name);
        //sb.append("\n");
        //sb.append("     @return ");
        //sb.append(name);
        //sb.append("\n    */\n");
        sb.append("    public ");
        sb.append(type);
        if(type.equals("boolean")){
            sb.append(" is");
        }else{
            sb.append(" get");
        }
        sb.append(name.substring(0,1).toUpperCase());
        sb.append(name.substring(1));
        sb.append("(){\n");
        sb.append("        return ");
        sb.append(name);
        sb.append(";\n    }\n\n");

        //sb.append("    /** set ");
        //sb.append(name);
        //sb.append("\n");
        //sb.append("     @param ");
        //sb.append(name);
        //sb.append(" ");
        //sb.append(name);
        //sb.append("\n    */\n");
        sb.append("    public void set");
        sb.append(name.substring(0,1).toUpperCase());
        sb.append(name.substring(1));
        sb.append("(");
        sb.append(type);
        sb.append(" ");
        sb.append(name);
        sb.append("){\n");
        sb.append("        this.");
        sb.append(name);
        sb.append(" = ");
        sb.append(name);
        sb.append(";\n    }\n\n");
    }

    public void checkImports(){
        try{
            mightParse();
            Javaprint engine = getCurrentProjectJavaprint();
            importComplete.bind(tool);
            importComplete.setClasspath(engine.getClasspath());
            String clause = importComplete.complete();

            Clipboard clip = jEdit.getActiveView().getToolkit().getSystemClipboard();
            StringSelection data = new StringSelection(clause);
            clip.setContents(data, data);
        }catch(Exception e){
            log.log(Level.SEVERE, "", e);

        }

    }

    protected Javaprint getCurrentProjectJavaprint()throws IOException, ClassNotFoundException{

        ProjectController projectCtl =
            MyJeditPlugin.getInstance().getPanelFactory().get(jEdit.getActiveView()).
                getController(ProjectController.class);
        Javaprint engine = projectCtl.getClassSearchEngine();
        //log.info(engine.getClasspath());
        return engine;
    }



}
