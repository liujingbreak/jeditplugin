package org.liujing.ironsword.bean;

import java.util.*;
import org.liujing.ironsword.lang.BaseLanguageModel;

public class SubFilesVO extends DaoPagination<DaoPagination>{
    private DaoPagination subFolders;
    private DaoPagination subSrcFiles;
    
    public SubFilesVO(int offset, int limit){
        super(offset, limit);
    }
    
    public SubFilesVO(PagingRequest copy){
        super(copy);
    }
    
    
    /** get subFolders
     @return subFolders
    */
    public DaoPagination getSubFolders(){
        return subFolders;
    }

    /** set subFolders
     @param subFolders subFolders
    */
    public void setSubFolders(DaoPagination subFolders){
        this.subFolders = subFolders;
    }

    /** get subSrcFiles
     @return subSrcFiles
    */
    public DaoPagination getSubSrcFiles(){
        return subSrcFiles;
    }

    /** set subSrcFiles
     @param subSrcFiles subSrcFiles
    */
    public void setSubSrcFiles(DaoPagination subSrcFiles){
        this.subSrcFiles = subSrcFiles;
    }
    
    @Override
    public PagingRequest prepareNextPage(){
        
        int off = getOffset();
        for(DaoPagination obj : getData()){
            if(obj != null)
                off += obj.getSize();
        }
        clear();
        setOffset(off);
        return this;
    }
    
    @Override
    public DaoPagination[] getData(){
        return new DaoPagination[]{ subSrcFiles, subFolders};
    }
    
    @Override
    public void clear(){
        super.clear();
        if(subFolders != null)
            subFolders.clear();
        if(subSrcFiles != null)
            subSrcFiles.clear();
    }
   
    public String toString(){
        StringBuilder sb = new StringBuilder();
        
        for(DaoPagination obj : getData()){
            if(obj == null) continue;
            sb.append(BaseLanguageModel.indentStr(1, obj.toString()));
            sb.append("\n\n");
        }
        return sb.toString();
    }
}
