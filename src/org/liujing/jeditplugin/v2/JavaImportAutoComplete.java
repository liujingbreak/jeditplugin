package org.liujing.jeditplugin.v2;

import java.util.*;
import java.io.*;
import java.util.logging.*;

public class JavaImportAutoComplete{
    private static Logger log = Logger.getLogger(JavaImportAutoComplete.class.getName());

    private Set<String> wildImports = new HashSet();
    private Set<String> importedTypes = new HashSet();
    private Set<String> needToImport = new HashSet();
    private Collection<String> allTypes;

    public JavaImportAutoComplete(){
    }

    public JavaImportAutoComplete(String classpath, Collection<String> imports,
        Collection<String> types)
    {
        setClasspath(classpath);
        setImportClauses(imports);
        setAllTypes(types);
    }


    public void bind(JavaFileAnalysisTool parserTool){
        setImportClauses(parserTool.getImports());
        setAllTypes(parserTool.getAssociatedTypes());
    }

    public void setClasspath(String cp){

    }

    /**
    set existing import clauses
    */
    public void setImportClauses(Collection<String> imports){
        for(String name: imports){
            if(name.contains("*"))
                wildImports.add(name);
            else{
                int p = name.lastIndexOf(".")+ 1;
                String importedType = name.substring(p);
                importedTypes.add(importedType);
                log.info("imported type:"+ importedType);
            }
        }
    }

    public void setAllTypes(Collection<String> types){
        allTypes = types;
    }

    public String complete(){
        for(String type : allTypes){
            log.fine("type: " + type);
            if(!importedTypes.contains(type)){
                needToImport.add(type);
                log.info(type);
            }
        }
        //findInWildImport(needToImport);
        //findInJRE(needToImport);
        //findInClasspath(needToImport);
        return "";
    }

    //protected List<String> findInClasspath(Collection<String> types){
    //    log.info("to find import for type: "+ types);
    //}
    //
    //protected List<String> findInWildImport(Collection<String> types){
    //
    //}
    //
    //protected List<String> findInJRE(Collection<String> types){
    //    File rt = new File(System.getProperty("java.home")+File.separator+"lib","rt.jar");
    //}
}

