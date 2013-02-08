package org.liujing.ironsword.lang;

import java.util.*;
public class ParamModel  implements java.io.Serializable{
    private String name;
    private String type;
    private String qtype;
    
    public ParamModel(String name){
        this.name = name;
    }
    
    public ParamModel(String name, String type){
        this.name = name;
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

        /** get qtype
     @return qtype
    */
    public String getQtype(){
        return qtype;
    }

    /** set qtype
     @param qtype qtype
    */
    public void setQtype(String qtype){
        this.qtype = qtype;
    }

    public boolean equals(Object o){
        if(!( o instanceof ParamModel))
            return false;
        ParamModel pm = (ParamModel)o;
        return name.equals(pm.name) && qtype.equals(pm.qtype);
    }
    
    public String toString(){
        return name + ": " + type;
    }
}
