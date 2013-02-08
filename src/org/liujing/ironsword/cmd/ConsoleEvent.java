
package org.liujing.ironsword.cmd;

import java.util.*;
import java.io.*;

public class ConsoleEvent{
    public static final int SELECT_TYPE = 1;
    public static final int CANCEL_TYPE = 2;
    public static final int OK_TYPE = 3;
    public static final int INPUT_DONE = 4;
    
    private Object source;
    private int type;
    private String input;
    private Object data;
    
    public ConsoleEvent(Object source, int type, String input){
        setSource(source);
        setType(type);
        setInput(input);
        setData(input);
    }
    
    public ConsoleEvent(Object source, int type, String input, Object data){
        this(source, type, input);
        setData(data);
    }
    
    /** get source
     @return source
    */
    public Object getSource(){
        return source;
    }

    /** set source
     @param source source
    */
    public void setSource(Object source){
        this.source = source;
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
    public void setType( int type){
        this.type = type;
    }

    /** get input
     @return input
    */
    public String getInput(){
        return input;
    }

    /** set input
     @param input input
    */
    public void setInput(String input){
        this.input = input;
    }

    /** get data
     @return data
    */
    public Object getData(){
        return data;
    }

    /** set data
     @param data data
    */
    public void setData(Object data){
        this.data = data;
    }


}
