package org.liujing.ironsword.bean;

import java.util.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.liujing.ironsword.lang.BaseLanguageModel;
import org.directwebremoting.annotations.*;

@DataTransferObject
public class TableRowVO{
    private TableStyleVO subTable;
    private List<Object> cells = new ArrayList();
    public TableRowVO(){
    }
    
    
    /** get subTable
     @return subTable
    */
    @RemoteProperty
    public TableStyleVO getSubTable(){
        return subTable;
    }

    /** set subTable
     @param subTable subTable
    */
    public void setSubTable(TableStyleVO subTable){
        this.subTable = subTable;
    }
    
    public void addCell(Object cell){
        cells.add(cell);
    }
    
    @RemoteProperty
    public List<Object> getCells(){
        return cells;
    }
    
    public String toString(){
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        for(Object cell : cells){
            pr.print(cell);
            pr.print("\t");
        }
        if(subTable != null){
            pr.println();
            pr.print(subTable.toString());
        }
        return sw.toString();
    }
}
