package org.liujing.ironsword.bean;

import java.util.*;
import org.liujing.ironsword.lang.BaseLanguageModel;
import org.directwebremoting.annotations.*;

@DataTransferObject
public class DaoPagination<T> extends PagingRequest{
    private String name;
    
    private int total = -1;
    @RemoteProperty
    private boolean more;
    private final static Object[] EMPTY_DATA = new Object[0];
    private T[] data = (T[])EMPTY_DATA;
    
    /**  construct DaoPagination
     @param offset start from 0
     @param limit capacity in this page
    */
    public DaoPagination(int offset, int limit){
        super(offset, limit);
    }
    
    public DaoPagination(String name, int offset, int limit){
        super(offset, limit);
        this.name = name;
    }
    
    public DaoPagination(PagingRequest copy){
        super(copy);
    }
    
    /** get data
     @return data
    */
    @RemoteProperty
    public T[] getData(){
        return data;
    }

    /** set data
     @param data data
    */
    public void setData(T[] data){
        this.data = data;
    }

    /** get more
     @return more
    */
    @RemoteProperty
    public boolean hasMore(){
        return more;
    }

    /** set more
     @param more more
    */
    public void setMore(boolean more){
        this.more = more;
    }
    
    public boolean isMore(){
        return more;
    }

    /** get total
     @return total
    */
    @RemoteProperty
    public int getTotal(){
        return total;
    }

    /** set total
     @param total total
    */
    public void setTotal(int total){
        this.total = total;
    }
    @RemoteProperty
    public int getSize(){
        return data.length;
    }
    
    public T getRow(int i){
        return data[i];
    }
    
    public PagingRequest prepareNextPage(){
        clear();
        this.offset = offset + data.length;
        return this;
    }
    
    public void clear(){
        data = (T[])EMPTY_DATA;
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
    
    public String toString(){
        StringBuilder sb = new StringBuilder();
        if(name != null && name.trim().length() > 0)
            sb.append(name).append("\n");
        int i = offset + 1;
        for(T obj : getData()){
            if(obj == null) continue;
            sb.append((i++) + ".");
            sb.append(BaseLanguageModel.indentStr(1, obj.toString()));
            sb.append("\n");
        }
        if(total >= 0){
            sb.append("\ttotal: ").append(total);
        }
        return sb.toString();
    }
}
