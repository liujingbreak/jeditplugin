package org.liujing.ironsword.lang;

import java.io.*;
import java.util.*;

public class CommonLanguageModel extends BaseLanguageModel implements Serializable{
    private String fileName;
    private String packageName;
    private List<String> imports = new ArrayList();
    private List<ClassModel> classes = new ArrayList(1);
    
    private List<MethodModel> methods = new ArrayList(1);
    
    public CommonLanguageModel(String fileName){
        this.fileName = fileName;
    }
    
    

    /** get packageName
     @return packageName
    */
    public String getPackageName(){
        return packageName;
    }

    /** set packageName
     @param packageName packageName
    */
    public void setPackageName(String packageName){
        this.packageName = packageName;
    }

    public void addClassModel(ClassModel cls){
        classes.add(cls);
    }
    
    public void addMethodModel(MethodModel method){
        methods.add(method);
    }
    
    public void addImport(String importname){
        imports.add(importname);
    }
    
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("File: "+ fileName);
        sb.append("\npackage: ");
        sb.append(packageName);
        sb.append("\n");
        //sb.append("imports: \n");
        //for(String imp: imports){
        //    sb.append("\t");
        //    sb.append(imp);
        //    sb.append("\n");
        //}
        sb.append("\n");
        for(ClassModel cls : classes){
            sb.append(indentStr(1, cls.toString()));
            sb.append("\n");
        }
        for(MethodModel m : methods){
            sb.append(indentStr(1, m.toString()));
            sb.append("\n");
        }
        //sb.append();
        return sb.toString();
    }
}
