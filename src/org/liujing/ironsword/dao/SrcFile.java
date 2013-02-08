package org.liujing.ironsword.dao;

import org.apache.commons.dbutils.*;
import org.apache.commons.dbutils.handlers.*;
import java.util.*;
import java.sql.*;
import java.io.*;
import java.util.logging.*;
import org.liujing.ironsword.IronException;
import org.liujing.ironsword.lang.*;
import org.directwebremoting.annotations.*;

@DataTransferObject
public class SrcFile extends SqlDAO{
    private static Logger log = Logger.getLogger(SrcFile.class.getName());
    private Number id;
    private String name;
    private String packageName;
    private String path;
    private String srcType;
    private FileTree fileTree;
    private Number checkFlag = 0;
    public static int IN_ZIP_FLAG = 2;
    public static final int UPDATED_FLAG = 1;
    private java.util.Date lastModified;
    /** grammar data */
    private CommonLanguageModel gramData;
    
    
    public SrcFile(){}
    
    public static SrcFile getById(Connection conn, int id)throws SQLException{
        Map<String,Object> row= new QueryRunner().query(conn, 
            "select * from src_file s join file_tree t on s.FILE_TREE_ID=t.FILE_TREE_ID join root_folder r on t.root_folder_id=r.root_folder_id where src_file_id = ? ",
                new MapHandler(), id);
        SrcFile f = new SrcFile();
        f.loadMap(row);
        FileTree t = new FileTree();
        t.loadMap(row);
        RootFolder r = new RootFolder();
        r.loadMap(row);
        f.setFileTree(t);
        t.setRootFolder(r);
        return f;
    }
    
    public Number getId(){
        return id;
    }

    public void setId(Number id){
        this.id = id;
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

    /** get packageName
     @return packageName
    */
    public String getPackageName(){
        return packageName;
    }

    /** set packageName
     @param packageName packageName
    */
    public void setPackageName(String packageName){
        this.packageName = packageName;
    }
    /** get checkFlag
     @return checkFlag
    */
    public Number getCheckFlag(){
        return checkFlag;
    }

    /** set checkFlag
     @param checkFlag checkFlag
    */
    public void setCheckFlag(Number checkFlag){
        this.checkFlag = checkFlag;
    }
    
    public void setCheckFlagBit(int flag){
        this.checkFlag = Integer.valueOf(this.checkFlag.intValue() | (1 << flag));
    }
    
    public boolean testCheckFlagBit(int flag){
        return (checkFlag.intValue() & (1<< flag) ) != 0;
    }
    
    public boolean isInZip(){
        return testCheckFlagBit(IN_ZIP_FLAG);
    }

    /** get path
     @return path
    */
    public String getPath(){
        return fileTree ==null ? path : fileTree.getPath();
    }

    ///** set path
    // @param path path
    //*/
    //public void setPath(String path){
    //    this.path = path;
    //}

    /** get srcType
     @return srcType
    */
    public String getSrcType(){
        return srcType;
    }

    /** set srcType
     @param srcType srcType
    */
    public void setSrcType(String srcType){
        this.srcType = srcType;
    }
    /** get lastModified
     @return lastModified
    */
    public java.util.Date getLastModified(){
        return lastModified;
    }

    /** set lastModified
     @param lastModified lastModified
    */
    public void setLastModified(java.util.Date lastModified){
        if(lastModified instanceof Timestamp)
            this.lastModified = lastModified;
        else
            this.lastModified = new Timestamp(lastModified.getTime());
    }


    /** get gramData
     @return gramData
    */
    public CommonLanguageModel getGramData(){
        return gramData;
    }

    /** set gramData
     @param gramData gramData
    */
    public void setGramData(CommonLanguageModel gramData){
        this.gramData = gramData;
    }
    /** get fileTree
     @return fileTree
    */
    public FileTree getFileTree(){
        return fileTree;
    }

    /** set fileTree
     @param fileTree fileTree
    */
    public void setFileTree(FileTree fileTree){
        this.fileTree = fileTree;
    }
    
    
    public void save(Connection conn, FileTree folder)throws SQLException{
        if(id == null){
            Object[] rets = new QueryRunner().query(conn,
                "select src_file_seq.nextval from dual", new ArrayHandler());
            id = (Number)rets[0];
            new QueryRunner().update(conn,
                    "insert into SRC_FILE (SRC_FILE_ID, SF_NAME, PACKAGE,  SRC_TYPE, GRAM_DATA, FILE_TREE_ID, SF_CHECK_FLAG, MODIFIED, SF_LAST_MODIF) values (?, ?, ?, ?, ?, ?,?, CURRENT_TIMESTAMP(), ?)",
                    id, name, packageName, srcType, gramData, 
                    folder == null? null: folder.id, checkFlag, lastModified);
        }else{
            new QueryRunner().update(conn,
                    "update SRC_FILE set SF_NAME=?, PACKAGE=?, SRC_TYPE=?, GRAM_DATA=?, FILE_TREE_ID=?, SF_CHECK_FLAG=?, MODIFIED=CURRENT_TIMESTAMP(), SF_LAST_MODIF=? where SRC_FILE_ID=?",
                    name, packageName, srcType, gramData, 
                    folder == null? null: folder.id, checkFlag, lastModified,  id);
        }
    }
    
    public static void clearTemp(Connection conn)throws SQLException{
        QueryRunner runner =  new QueryRunner();
        runner.update(conn, "delete from SRC_FILE_UPDATED");
        runner.update(conn, "delete from SRC_FILE_DELETE");
    }
    
    /**  markInTemp
     @param conn conn
     @return true if file already marked
     @throws SQLException if SQLException occurs
    */
    public boolean markInTemp(Connection conn)throws SQLException{
        Object[] rets = new QueryRunner().query(conn,
                "select * from SRC_FILE_UPDATED where SRC_FILE_ID = ?", new ArrayHandler(), id);
        if(rets == null || rets.length <= 0){
            new QueryRunner().update(conn, "insert into SRC_FILE_UPDATED values (?)", id);
            return false;
        }else{
            return true;
        }
    }
    
    public static int deleteNotInTemp(Connection conn, Number rootFolderId, 
        String coverPath)throws SQLException
    {
        String path = coverPath;
        if(coverPath != null && coverPath.length()> 0 && coverPath.charAt(coverPath.length() -1) == '/')
            path = coverPath.substring(0, coverPath.length() -1);
        String pathClause = (path != null && path.length() > 0)? 
                "and (f.path like '"+ path +"/%' or f.path = '"+ path +"')" 
                : "";
        
        //String query = "select j.SRC_FILE_ID from (select s.SRC_FILE_ID from SRC_FILE s left join FILE_TREE f on s.FILE_TREE_ID= f.FILE_TREE_ID where f.ROOT_FOLDER_ID=? "
        //    + pathClause +" ) j left join SRC_FILE_UPDATED t on j.SRC_FILE_ID = t.SRC_FILE_ID where t.SRC_FILE_ID is null";
            
        String query = "select j.SRC_FILE_ID from (select s.SRC_FILE_ID from SRC_FILE s left join FILE_TREE f on s.FILE_TREE_ID= f.FILE_TREE_ID where f.ROOT_FOLDER_ID=? "
            + pathClause +") j where not exists (select 1 from SRC_FILE_UPDATED u where u.SRC_FILE_ID = j.SRC_FILE_ID)";
            
        
        log.fine("delete query="+ query);
        int toDelete = new QueryRunner().update(conn, "insert into SRC_FILE_DELETE "
            + query, rootFolderId);
        log.fine("ready to delete: "+ toDelete);
            
        String sql = "delete from src_file s where exists "
            +" (select 1 from SRC_FILE_DELETE d where s.SRC_FILE_ID = d.SRC_FILE_ID) ";
        int num = new QueryRunner().update(conn, sql);
        return num;
    }
    
    @Override
    protected void loadMap(Map<String, Object> dataMap){
        try{
        this.id = (Number)dataMap.get("SRC_FILE_ID");
        this.name = (String)dataMap.get("SF_NAME");
        this.path = (String)dataMap.get("PATH");
        this.packageName = (String)dataMap.get("PACKAGE");
        this.srcType = (String)dataMap.get("SRC_TYPE");
        this.checkFlag = (Number)dataMap.get("SF_CHECK_FLAG");
        this.lastModified = (Timestamp)dataMap.get("SF_LAST_MODIF");
        Blob gram = (Blob)dataMap.get("GRAM_DATA");
        if(gram != null){
            ObjectInputStream objin = new ObjectInputStream(
                gram.getBinaryStream());
            this.gramData = (CommonLanguageModel)objin.readObject();
        }
        }catch(Exception e){
            throw new IronException("Failed to convert database data", e);
        }
    }
    
    public static int resetFlag(Connection conn, int value, String underPath, Number rootFolderId)
    throws SQLException
    {
        StringBuilder sb = new StringBuilder();
        sb.append("update SRC_FILE set SF_CHECK_FLAG=? where FILE_TREE_ID in (select FILE_TREE_ID from FILE_TREE where ROOT_FOLDER_ID = ?");
        if(underPath != null && underPath.length() > 0){
            String path = underPath;
            if(underPath.charAt(underPath.length() -1) == '/')
                path = underPath.substring(0, underPath.length() -1);
            sb.append(" and PATH like '").append(path).append("/%'");
        }
        sb.append(" )");
        
        return new QueryRunner().update(conn,sb.toString(), value, rootFolderId);
    }
    
    public static int deleteFilesByFlag(Connection conn, int value, Number rootFolderId)
    throws SQLException
    {
        return new QueryRunner().update(conn,
            "delete from SRC_FILE where SF_CHECK_FLAG=? and FILE_TREE_ID in (select FILE_TREE_ID from FILE_TREE where ROOT_FOLDER_ID = ?)",
            value, rootFolderId);
    }
    
    public void load(){
        //SELECT * FROM SRC_FILE s left join FILE_TREE f left join ROOT_FOLDER r on f.ROOT_FOLDER_ID  = r.ROOT_FOLDER_ID  on s.FILE_TREE_ID = f.FILE_TREE_ID
        
    }
    
    public String toString(){
        return name;
    }
    
    public String fullpath(){
        FileTree ft = getFileTree();
        if(ft != null){
            RootFolder rf = ft.getRootFolder();
            if(rf != null){
                String path = rf.getPath() + (ft.getPath().length() > 0? "/"+ ft.getPath() + "/": "/")
                    + getName();
                if(File.separatorChar == '\\'){
                    return path.replaceAll("/", "\\\\");
                }else{
                    return path;
                }
            }else
                return toString();
        }else{
            return toString();
        }
    }
}
