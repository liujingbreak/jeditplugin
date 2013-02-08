package org.liujing.ironsword.lang;

import java.io.*;
import java.util.*;

public class MethodModel extends BaseLanguageModel implements Serializable, LanguageModelConstants{
    private String type;
    private int accessType;
    private String accessTypeStr;
    private String name;
    public boolean isstatic = false;
    private static List<ParamModel> EMPTY_PARAMS = new ArrayList();
    private List<ParamModel> params = EMPTY_PARAMS;
    private String doc;
    
    public MethodModel(String name){
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
    /** get accessTypeStr
     @return accessTypeStr
    */
    public String getAccessTypeStr(){
        return accessTypeStr;
    }

    /** set accessTypeStr
     @param accessTypeStr accessTypeStr
    */
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
    
    /** get params
     @return params
    */
    public List<ParamModel> getParams(){
        return params;
    }

    /** set params
     @param params params
    */
    public void setParams(List<ParamModel> params){
        this.params = params;
    }
    
    public void addParam(ParamModel pm){
        if(params == EMPTY_PARAMS)
            params = new ArrayList();
        params.add(pm);
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
        sb.append(" (");
        int i = 0;
        for(ParamModel pm : params){
            if( i > 0)
                sb.append(", ");
            sb.append(pm.toString());
            i++;
        }
        sb.append(")");
        
        if(type != null)
            sb.append("\t:").append(getType());
        return sb.toString();
    }
}
