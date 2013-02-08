package org.liujing.jeditplugin.v2;

import org.antlr.runtime.*;
import java.util.*;
import java.util.logging.*;
import java.io.*;
import liujing.jedit.parser.*;
import org.liujing.parser.*;

/** All those method named "onXxxx()" is called by JavaParser.<br>
    This class is a handler for parsing java file
*/
public class JavaFileAnalysisTool implements JavaFileAnalysisHandler{
    private static Logger log = Logger.getLogger(JavaFileAnalysisTool.class.getName());

    private Set<String> usedTypes = new HashSet();
    private Set<String> typeParameters = new HashSet();
    private String packageName;
    private String mainClassFullName;
    private List<TypeDeclaration> typeDecList = new ArrayList();
    private Map<String, TypeDeclaration> typeDecMap = new HashMap();
    private List<String> imports = new ArrayList();
    private long timeStamp = 0;

    private StringBuilder buf = new StringBuilder();

    public void onTypeAssociated(List<Token> ids){
        buf.setLength(0);
        for(Token id : ids){
            buf.append(id.getText());
            buf.append(".");
        }
        buf.delete(buf.length()-1, buf.length());
        String qualifiedName = buf.toString();
        onTypeAssociated(qualifiedName);
    }

    public void onImport(String qualifiedName){
        imports.add(qualifiedName);
    }

    public void onStaticImport(String qualifiedName){
        log.info("there are static import:" + qualifiedName);
    }

    public List<String> getImports(){
        return imports;
    }

    public void onTypeAssociated(String qualifiedName){
        if(usedTypes.add(qualifiedName)){
            log.fine("type: " + qualifiedName);
        }
    }

    public void onTypeParameter(String name){
        typeParameters.add(name);
    }

    public Set<String> getAssociatedTypes(){
        return usedTypes;
    }

    public void onPackageName(String name){
        packageName = name;
        log.fine("package: " + packageName);
    }



    public void onTypeDeclaration(String name, int nestLevel, int lineNo){
        if(mainClassFullName == null)
            mainClassFullName = (packageName == null? "":packageName + ".") + name;
        log.fine("type declaration: " + name + " nestLevel="+ nestLevel + " line="+ lineNo);
        //log.log(Level.FINE, "", new Throwable("test"));
        TypeDeclaration typeDec = new TypeDeclaration(name, nestLevel, lineNo);
        typeDecList.add(typeDec);
        typeDecMap.put(name, typeDec);
    }

    public String getMainClassFullName(){
        return mainClassFullName;
    }

    public void parseJava(Reader in)throws Exception{

        ANTLRReaderStream stream = new ANTLRReaderStream(in);
        JavaLexer lexer = new JavaLexer(stream);
        RemovableTokenStream tokens = new RemovableTokenStream(lexer);
        JavaParser p = new JavaParser(tokens);
        p.compilationUnit(this);

        // --- Past process ---
        for(String name: typeDecMap.keySet()){
            usedTypes.remove(name);
        }

        usedTypes.removeAll(typeParameters);
        timeStamp = System.currentTimeMillis();
    }

    public static class TypeDeclaration{
        public String name;
        public int nestLevel; // start from 1
        public int lineNo;

        public TypeDeclaration(String name, int level, int lineno){
            this.name = name;
            this.nestLevel = level;
            this.lineNo = lineno;
        }
    }

    /** get timeStamp
     @return timeStamp
    */
    public long getTimeStamp(){
        return timeStamp;
    }


}
