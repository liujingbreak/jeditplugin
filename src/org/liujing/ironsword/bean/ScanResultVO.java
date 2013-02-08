package org.liujing.ironsword.bean;

import java.util.*;
import java.io.*;

public class ScanResultVO implements Serializable{
    private int updatedCount;
    private int removedCount;
    private int addedCount;
    private int duplicateCount;
    
    public ScanResultVO(int addedCount, int updatedCount, int removedCount){
        this.addedCount = addedCount;
        this.removedCount = removedCount;
        this.updatedCount = updatedCount;
    }
    
    public ScanResultVO(int addedCount, int updatedCount, int removedCount,
        int duplicate){
        this.addedCount = addedCount;
        this.removedCount = removedCount;
        this.updatedCount = updatedCount;
        this.duplicateCount = duplicate;
    }
    
    /** get updatedCount
     @return updatedCount
    */
    public int getUpdatedCount(){
        return updatedCount;
    }

    /** set updatedCount
     @param updatedCount updatedCount
    */
    public void setUpdatedCount(int updatedCount){
        this.updatedCount = updatedCount;
    }

    /** get removedCount
     @return removedCount
    */
    public int getRemovedCount(){
        return removedCount;
    }

    /** set removedCount
     @param removedCount removedCount
    */
    public void setRemovedCount(int removedCount){
        this.removedCount = removedCount;
    }

    /** get addedCount
     @return addedCount
    */
    public int getAddedCount(){
        return addedCount;
    }

    /** set addedCount
     @param addedCount addedCount
    */
    public void setAddedCount(int addedCount){
        this.addedCount = addedCount;
    }
    
    /** get duplicateCount
     @return duplicateCount
    */
    public int getDuplicateCount(){
        return duplicateCount;
    }

    /** set duplicateCount
     @param duplicateCount duplicateCount
    */
    public void setDuplicateCount(int duplicateCount){
        this.duplicateCount = duplicateCount;
    }


    public String toString(){
        return String.format("File added: %1$d, Updated: %2$d, Deleted: %3$d, %4$s",
           addedCount, updatedCount, removedCount,
           duplicateCount==0? "": "duplicate:" + duplicateCount);
    }
    
}
