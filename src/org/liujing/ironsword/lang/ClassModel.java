package org.liujing.ironsword.lang;

import java.io.*;
import java.util.*;

public class ClassModel extends BaseLanguageModel implements Serializable, LanguageModelConstants{
    private LinkedHashMap<String, FieldModel> fields = EMPTY_MAP;
    //private LinkedHashMap<String, List<MethodModel>> methods = EMPTY_MAP;
    private List<MethodModel> methods = EMPTY_LIST;
    
    private LinkedHashMap<String, ClassModel> classes = EMPTY_MAP;
    private static LinkedHashMap EMPTY_MAP = new LinkedHashMap();
    private static List EMPTY_LIST = new ArrayList();
    
    private List<String> supers = new ArrayList();
    /** qualified super classes name */
    private List<String> qsupers = new ArrayList();
    private int type;
    private String name;
    private int accessType;
    private String accessTypeStr;
    
    public boolean isstatic = false;
    private String doc;
    
    public ClassModel(String name){
        this.name = name;
    }
    
    /** get type
     @return type
    */
    public int getType(){
        return type;
    }

    /** set type
     @param type type
    */
    public void setType(int type){
        this.type = type;
    }
    /** get name
     @return name
    */
    public String getName(){
        return name;
    }

    /** set name
     @param name name
    */
    public void setName(String name){
        this.name = name;
    }


    /** get accessType
     @return accessType
    */
    public int getAccessType(){
        return accessType;
    }

    /** set accessType
     @param accessType accessType
    */
    public void setAccessType(int accessType){
        this.accessType = accessType;
    }
    
    public void setAccessType(String accessStr){
        accessTypeStr = accessStr;
        if("public".equals(accessTypeStr)){
            setAccessType(LanguageModelConstants.ACCESS_PUBLIC);
        }else if("protected".equals(accessTypeStr)){
            setAccessType(LanguageModelConstants.ACCESS_PROTECTED);
        }else if("private".equals(accessTypeStr)){
            setAccessType(LanguageModelConstants.ACCESS_PRIVATE);
        }
    }
    /** get supers
     @return supers
    */
    public List<String> getSupers(){
        return supers;
    }

    /** set supers
     @param supers supers
    */
    public void setSupers(List<String> supers){
        this.supers = supers;
    }

    
    public void addSuper(String supername){
        if(this.supers == null)
            supers = new ArrayList();
        supers.add(supername);
    }

    /** get qsupers
     @return qsupers
    */
    public List<String> getQsupers(){
        return qsupers;
    }

    /** set qsupers
     @param qsupers qsupers
    */
    public void setQsupers(List<String> qsupers){
        this.qsupers = qsupers;
    }

    /** get doc
     @return doc
    */
    public String getDoc(){
        return doc;
    }

    /** set doc
     @param doc doc
    */
    public void setDoc(String doc){
        this.doc = doc;
    }
    
    public void addField(FieldModel field){
        if(fields == EMPTY_MAP)
            fields = new LinkedHashMap();
        fields.put(field.getName(), field);
    }
    
    public void addMethod(MethodModel method){
        if(methods == EMPTY_LIST)
            methods = new ArrayList();
        methods.add(method);
        //if(methods == EMPTY_MAP)
        //    methods = new LinkedHashMap();
        //List<MethodModel> ms = methods.get(method.getName());
        //if(ms == null){
        //    ms = new ArrayList(2);
        //    methods.put(method.getName(), ms);
        //}
        //ms.add(method);
    }

    public void addInnerClass(ClassModel m){
        if(classes == EMPTY_MAP)
            classes = new LinkedHashMap();
        classes.put(m.getName(), m);
    }
    
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Class: ");
        sb.append(getName());
        sb.append(" -> ");
        for(String superclass: getSupers()){
            sb.append(superclass);
            sb.append(" ");
        }
        if(methods.size() > 0){
            sb.append("\nMethods: ");
            for(MethodModel method : methods){
                sb.append("\n\t");
                sb.append(method.toString());
            }
            sb.append("\n");
        }
        if(fields.size() > 0){
            sb.append("\nFields: ");
            for(FieldModel f : fields.values()){
                sb.append("\n\t");
                sb.append(f.toString());
            }
            sb.append("\n");
        }
        if(classes.size() > 0){
            sb.append("\nInner Class: ");
            for(ClassModel c : classes.values()){
                sb.append(indentStr(1, c.toString()));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
