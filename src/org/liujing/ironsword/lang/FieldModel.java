package org.liujing.ironsword.lang;

import java.io.*;
import java.util.*;

public class FieldModel extends BaseLanguageModel implements Serializable, LanguageModelConstants{
    
    private String type;
    private String accessTypeStr;
    /** qualified */
    private String qtype;
    private int accessType;
    private String name;
    private String doc;
    public boolean isstatic = false;
    
    public FieldModel(String name){
        this.name = name;
    }
    
    /** get type
     @return type
    */
    public String getType(){
        return type;
    }

    /** set type
     @param type type
    */
    public void setType(String type){
        this.type = type;
    }

    /** get qualified type
     @return qualified type
    */
    public String getQtype(){
        return qtype;
    }

    /** set qualified type
     @param qtype qualified type
    */
    public void setQtype(String qtype){
        this.qtype = qtype;
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
    public void setAccessType(String accessTypeStr){
        this.accessTypeStr = accessTypeStr;
        if("public".equals(accessTypeStr)){
            setAccessType(ACCESS_PUBLIC);
        }else if("protected".equals(accessTypeStr)){
            setAccessType(ACCESS_PROTECTED);
        }else if("private".equals(accessTypeStr)){
            setAccessType(ACCESS_PRIVATE);
        }
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

    public String toString(){
        StringBuilder sb = new StringBuilder();
        if(accessType == ACCESS_PUBLIC)
            sb.append("+ ");
        else if(accessType == ACCESS_PRIVATE)
            sb.append("- ");
        else
            sb.append("  ");
        sb.append(name);
        if(type != null){
            sb.append(": ");
            sb.append(type);
        }
        return sb.toString();
    }
}
