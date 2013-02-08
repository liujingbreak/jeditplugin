package org.liujing.ironsword;

public class IronException extends RuntimeException{
    public IronException(String msg){
        super(msg);
    }
    
    public IronException(String msg, Throwable e){
        super(msg, e);
    }
        
}
