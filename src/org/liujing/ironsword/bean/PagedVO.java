package org.liujing.ironsword.bean;

import java.util.*;
import org.directwebremoting.annotations.*;

/**
    display a hierarchical structure in a plain list view, but with indent information
*/

@DataTransferObject
public class PagedVO extends DaoPagination<TableRowVO>{
    private TableStyleVO vo;
    private int size;
    private List<TableRowVO> listCache;
    
    public PagedVO(int offset, int limit){
        super(offset, limit);
    }
    public PagedVO(PagingRequest copy){
        super(copy);
    }
    
    public PagedVO(DaoPagination copy, TableStyleVO vo){
        super(copy);
        setVo(vo);
        setSize(copy.getSize());
        setTotal(copy.getTotal());
        setMore(copy.hasMore());
    }
    
    /** get vo
     @return vo
    */
    @RemoteProperty
    public TableStyleVO getVo(){
        return vo;
    }

    /** set vo
     @param vo vo
    */
    public void setVo(TableStyleVO vo){
        this.vo = vo;
    }
    
    /** get size
     @return size
    */
    @RemoteProperty
    public int getSize(){
        return size;
    }

    /** set size
     @param size size
    */
    public void setSize(int size){
        this.size = size;
    }


    @Override
    public PagingRequest prepareNextPage(){
        clear();
        setOffset(getOffset() + size);
        return this;
    }
    @Override
    public void clear(){
        vo = null;
        listCache = null;
    }
    @RemoteProperty
    public boolean isMore(){
        return hasMore();
    }
    
    public TableRowVO getRow(int i){
        if(listCache == null){
            forEach(null);
        }
        return listCache.get(i);
    }
    
    public void forEach(EachRowHandler rh){
        if(listCache == null)
            listCache = new ArrayList();
        else
            listCache.clear();
        traverseTable(vo, rh, 0);
        
    }
    
    protected void traverseTable(TableStyleVO t, EachRowHandler h, int indentLevel){
        
        for(TableRowVO r1 : t.getRows()){
            if(h != null)
                h.eachRow(indentLevel, r1);
            listCache.add(r1);
            TableStyleVO subTab = r1.getSubTable();
            if(subTab != null){
                //h.eachRow(indentLevel, subTab);
                traverseTable(subTab, h, indentLevel + 1);
            }
        }
    }
    
    public interface EachRowHandler{
        /** @param row could be TableStyleVO or TableRowVO
        */
        void eachRow(int indentLevel, Object row);
    }
    
    public String toString(){
        return vo.toString();
    }
}
