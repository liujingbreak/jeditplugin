package org.liujing.ironsword.dao;

import org.apache.commons.dbutils.*;
import org.apache.commons.dbutils.handlers.*;
import java.util.*;
import java.sql.*;
import java.io.*;
import java.util.logging.*;
import org.liujing.ironsword.IronException;

public class SnapShotFileDAO extends SqlDAO{
    private static Logger log = Logger.getLogger(SnapShotFileDAO.class.getName());
    
    private Number id;
    private String fullPath;
    private Timestamp lastModified;
    private Number rootFolderId;
    private Number srcFileId;
    private Number snapShortId;
     
    private SnapShotDAO snapShot;
    private SrcFile srcFile;
    private RootFolder rootFolder;
    
    public SnapShotFileDAO(){
    }
    
    public Number getId(){
        return id;
    }

    public void setId(Number id){
        this.id = id;
    }

    public String getFullPath(){
        return fullPath;
    }

    public void setFullPath(String fullPath){
        this.fullPath = fullPath;
    }

    public Timestamp getLastModified(){
        return lastModified;
    }

    //public void setLastModified(Timestamp lastModified){
    //    this.lastModified = lastModified;
    //}

    public Number getRootFolderId(){
        return rootFolderId;
    }

    public void setRootFolderId(Number rootFolderId){
        this.rootFolderId = rootFolderId;
    }

    public Number getSrcFileId(){
        return srcFileId;
    }

    public void setSrcFileId(Number srcFileId){
        this.srcFileId = srcFileId;
    }

    public Number getSnapShortId(){
        return snapShortId;
    }

    public void setSnapShortId(Number snapShortId){
        this.snapShortId = snapShortId;
    }

    public SnapShotDAO getSnapShot(){
        return snapShot;
    }

    public void setSnapShot(SnapShotDAO snapShot){
        this.snapShot = snapShot;
        if(snapShot.getId() != null)
            setSnapShortId(snapShot.getId());
    }

    public SrcFile getSrcFile(){
        return srcFile;
    }

    public void setSrcFile(SrcFile srcFile){
        this.srcFile = srcFile;
        srcFileId = srcFile.getId();
    }

    public RootFolder getRootFolder(){
        return rootFolder;
    }

    public void setRootFolder(RootFolder rootFolder){
        this.rootFolder = rootFolder;
        if(rootFolder.getId() != null)
            setRootFolderId(rootFolder.getId());
    }
    
    protected void loadMap(Map<String, Object> rowData){
        id = (Number)rowData.get("ssf_id");
        fullPath = (String)rowData.get("ssf_full_path");
        lastModified = (Timestamp)rowData.get("ssf_LAST_MODIF");
        rootFolderId = (Number)rowData.get("root_folder_id");
        srcFileId = (Number)rowData.get("SRC_FILE_ID");
        snapShortId = (Number)rowData.get("ss_id"); 
    }
    
    public void save(Connection conn)throws SQLException{
        
        Object[] rets = new QueryRunner().query(conn,
        "select snap_shot_file_seq.nextval from dual", new ArrayHandler());
        id = (Number)rets[0];
        
        new QueryRunner().update(conn,
            "insert into snap_shot_file (ssf_id, ssf_full_path,ssf_LAST_MODIF,root_folder_id,SRC_FILE_ID,ss_id) values (?, ?,CURRENT_TIMESTAMP,?,?,?)",
            id, fullPath, rootFolderId, srcFileId, snapShortId);
    }
    
    public void delete(Connection conn)throws SQLException{
        new QueryRunner().update(conn,
            "delete from snap_shot_file where ssf_id=?", id);
    }

}

    
    
