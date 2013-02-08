package org.liujing.ironsword.cmd.woodenaxe;

import java.util.*;
import java.io.*;
import java.util.logging.*;
import org.liujing.ironsword.bean.*;
import org.liujing.ironsword.cmd.*;
import java.util.regex.Pattern;

public class ConsoleFoundFileView extends ConsoleDataPagination implements ConsoleEventHandler{
    protected boolean viewInFull;//view in full path
    private ConsoleChoiceList opt;
    
    private int indentLevels = 3;
    
    public ConsoleFoundFileView(){
        init();
    }
    
    public ConsoleFoundFileView(CmdLinePagingHandler dataSource){
        super(dataSource);
        init();
    }
    
    public int getIndentLevels(){
        return indentLevels;
    }

    public void setIndentLevels(int indentLevels){
        this.indentLevels = indentLevels;
    }
    
    private void init(){
        opt = new ConsoleChoiceList();
        add(opt);
        opt.addItem("Toggle view mode");
        opt.addSelEvtHandler(this);
    }
    
    public void toggleViewFullPath(boolean yes){
        viewInFull = yes;
    }
    
    protected void printContent(PrintWriter p){
        if(page == null && dataSource != null){
          page = dataSource.fetchPage(pr);
        }
        if(page == null){
            startChoiceNo = -1;
            endChoiceNo = -1;
            return;
        }
        PagedVO pagevo = (PagedVO)page;
        startChoiceNo = ConsolePrintContext.getInstance().currChoiceNum() + 1;
        pagevo.forEach(new RowHandler(p));
        if(page.getTotal()>= 0){
            p.print("-- total ");
            p.print(page.getTotal());
            p.println("--");
        }
        if(page.hasMore())
            p.println("[Enter] Press Enter to continue...");
        endChoiceNo = ConsolePrintContext.getInstance().currChoiceNum();
    }
    
    private class RowHandler implements PagedVO.EachRowHandler{
        PrintWriter p;
        Number fileTreeId;
        Number rootFolderId;
        String rootFolderPath;
        String fileTreePath;
        int rowNo = 1;
        
        public RowHandler(PrintWriter p){
            this.p = p;
        }
        
        public void eachRow(int indentLevel, Object row){
            int cn = ConsolePrintContext.getInstance().nextChoiceNum();
            p.print('[');
            p.print(cn); p.print("] ");
            if(indentLevel + 1 == indentLevels){
                p.print( rowNo + pr.getOffset());
                p.print(". ");
                rowNo++;
            }
            for(int i =0;i<indentLevel;i++)
                p.print("\t");
            if(row instanceof TableStyleVO){
                //TableStyleVO tb = (TableStyleVO) row;
                //p.println(tb.getName());
            }else if(row instanceof TableRowVO){
                TableRowVO tr = (TableRowVO) row;
                int colLast = tr.getCells().size() -1;
                if(indentLevel == 0){
                    rootFolderId = (Number)tr.getCells().get(colLast);
                    rootFolderPath = (String)tr.getCells().get(1);
                    if(File.separatorChar != '/')
                        rootFolderPath = rootFolderPath.replaceAll("/", "\\\\");
                }else if(indentLevel == 1){
                    fileTreeId = (Number)tr.getCells().get(colLast);
                    fileTreePath = (String)tr.getCells().get(1);
                    if(File.separatorChar != '/')
                        fileTreePath = fileTreePath.replaceAll("/", "\\\\");
                }
                if(viewInFull)
                    printFullPath(tr, indentLevel);
                else
                    printCompact(tr, indentLevel);
            }
        }
        
        private void printFullPath(TableRowVO tr, int indentLevel){
            p.print(rootFolderPath);
            p.print(File.separator);
            if(indentLevel > 0){
                if(fileTreePath.length() > 0){
                    p.print(fileTreePath);
                    p.print(File.separator);
                }
                if(indentLevel > 1)
                    p.print(tr.getCells().get(1));
                
            }
            p.println();
        }
        
        private void printCompact(TableRowVO tr, int indentLevel){
            p.print(tr.getCells().get(1));
            String type = (String)tr.getCells().get(0);
            if(type.equals("r") || type.equals("d"))
                p.print("/");
            p.println();
        }
    }
    
    public void handleEvent(ConsoleEvent evt){
        if(evt.getSource() == opt){
            int sel = ((Number)evt.getData()).intValue();
            if(sel == 0){
                viewInFull = !viewInFull;
            }
        }
    }
    
}
