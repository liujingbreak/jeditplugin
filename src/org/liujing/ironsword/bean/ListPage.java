package org.liujing.ironsword.bean;

import java.util.*;
import org.liujing.ironsword.lang.BaseLanguageModel;
import org.directwebremoting.annotations.*;

@DataTransferObject
public class ListPage<T> extends DaoPagination<T>{
    
    private final static List EMPTY_LIST = new ArrayList(0);
    
    private List<T> listData = (List<T>)EMPTY_LIST;
    
    /**  construct DaoPagination
     @param offset start from 0
     @param limit capacity in this page
    */
    public ListPage(int offset, int limit){
        super(offset, limit);
    }
    
    public ListPage(String name, int offset, int limit){
        super(name, offset, limit);
    }
    
    public ListPage(PagingRequest copy){
        super(copy);
    }
    /** get listData
     @return listData
    */
    @RemoteProperty
    public List<T> getListData(){
        return listData;
    }

    /** set listData
     @param listData listData
    */
    public void setListData(List<T> listData){
        this.listData = listData;
    }
    
    @Override
    public int getSize(){
        return listData.size();
    }
    
    public T getRow(int i){
        return listData.get(i);
    }

    @Override
    public void clear(){
        super.clear();
        listData = EMPTY_LIST;
    }
    @Override
    public PagingRequest prepareNextPage(){
        setOffset(getOffset() + listData.size());
        clear();
        return this;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        if(getName() != null && getName().trim().length() > 0)
            sb.append(getName()).append("\n");
        int i = getOffset() + 1;
        for(T row : getListData()){
            if(row == null) continue;
            sb.append((i++) + ".");
            sb.append(BaseLanguageModel.indentStr(1, row2String(row)));
            sb.append("\n");
        }
        if(getTotal() >= 0){
            sb.append("\ttotal: ").append(getTotal());
        }
        return sb.toString();
    }
    
    protected String row2String(T row){
        StringBuilder sb = new StringBuilder();
        if(row instanceof Object[]){
            for(Object col : (Object[])row){
                sb.append(col).append(" | ");
            }
        }else if(row instanceof Map){
            for(Object col : ((Map)row).values()){
                sb.append(col).append(" | ");
            }
        }else{
            return row.toString();
        }
        return sb.toString();
    }
}
