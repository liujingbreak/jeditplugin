package org.liujing.ironsword.bean;

import java.util.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.liujing.ironsword.lang.BaseLanguageModel;
import org.directwebremoting.annotations.*;

@DataTransferObject
public class TableStyleVO{
    private String name;
    private List<TableRowVO> rows = new ArrayList();
    private List<String> columnNames;
    
    public TableStyleVO(){
        columnNames = new ArrayList();
    }
    
    public TableStyleVO(List<String> columnNames){
        this.columnNames = columnNames;
    }
    
    public TableStyleVO(String... columns){
        columnNames = new ArrayList();
        for(String col: columns)
            columnNames.add(col);
    }

    /** get name
     @return name
    */
    @RemoteProperty
    public String getName(){
        return name;
    }

    /** set name
     @param name name
    */
    public void setName(String name){
        this.name = name;
    }

    
    public void addRow(TableRowVO row){
        rows.add(row);
    }
    
    public TableRowVO addRow(Object... cells){
        TableRowVO row = new TableRowVO();
        for(Object cell : cells)
            row.addCell(cell);
        rows.add(row);
        return row;
    }
    
    public TableRowVO addRow(TableStyleVO subTable, Object... cells){
        TableRowVO row = addRow(cells);
        row.setSubTable(subTable);
        return row;
    }
    
    public TableRowVO getRow(int i){
        return rows.get(i);
    }
    @RemoteProperty
    public Iterable<TableRowVO> getRows(){
        return rows;
    }
    
    public String toString(){
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        if(name != null){
            pr.print(name);
            pr.println();
        }
        if(columnNames != null){
            int i = 0;
            for(String cn : columnNames){
                if(i != 0)
                    pr.append(" | ");
                pr.append(cn);
                i++;
            }
        }
        int i = 0;
        for(TableRowVO row : rows){
            if(i != 0)
                pr.println();
            pr.print(BaseLanguageModel.indentStr(1, row.toString()));
            i++;
        }
        return sw.toString();
    }
}
