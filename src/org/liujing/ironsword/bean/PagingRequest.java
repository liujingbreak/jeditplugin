package org.liujing.ironsword.bean;

import java.util.*;
import org.directwebremoting.annotations.*;

@DataTransferObject(javascript="PagingRequest")
public class PagingRequest{
    protected int offset;
    protected int limit;
    
    public PagingRequest(){}
    
    public PagingRequest(int offset, int limit){
        this.offset = offset;
        this.limit = limit;
    }
    
    public PagingRequest(PagingRequest copy){
        this.offset = copy.offset;
        this.limit = copy.limit;
    }
    @RemoteProperty
    public void setOffset(int offset){
        this.offset = offset;
    }    
    @RemoteProperty
    public void setLimit(int limit){
        this.limit = limit;
    }
    @RemoteProperty
    public int getOffset(){
        return offset;
    }
    @RemoteProperty
    public int getLimit(){
        return limit;
    }
}
