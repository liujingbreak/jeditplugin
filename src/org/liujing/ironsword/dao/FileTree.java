package org.liujing.ironsword.dao;

import org.apache.commons.dbutils.*;
import org.apache.commons.dbutils.handlers.*;
import java.util.*;
import java.sql.*;
import java.io.*;
import java.util.logging.*;
import org.liujing.ironsword.IronException;
import org.liujing.ironsword.bean.*;
import org.directwebremoting.annotations.*;
@DataTransferObject
public class FileTree extends SqlDAO{
    private static final Logger log = Logger.getLogger(FileTree.class.getName());
    protected Number id;
    private String name;
    private FileTree parent;
    private String path;
    private RootFolder rootFolder;
    private Number parentId;
    /** number of files */
    private Number numFiles = Integer.valueOf(0);
    private Number checkFlag = 0;
    public static String ROOT_PATH = "";
    
    public FileTree(){
        
    }
    
    public FileTree(File f){
        this.name = f.getName();
    }
    
    public static FileTree getById(Connection conn, int id)throws SQLException{
        Map<String,Object> result = new QueryRunner().query(conn,
            "select * from FILE_TREE f join ROOT_FOLDER r on f.ROOT_FOLDER_ID=r.ROOT_FOLDER_ID where FILE_TREE_ID=?", new MapHandler(), id);
        if(result == null || result.size() == 0)
            return null;
        FileTree f = new FileTree();
        f.loadMap(result);
        RootFolder r = new RootFolder();
        r.loadMap(result);
        f.setRootFolder(r);
        return f;
    }
  
    /** get name
     @return name
    */
    public String getName(){
        return name;
    }

    ///** set name
    // @param name name
    //*/
    //public void setName(String name){
    //    this.name = name;
    //}

    /** get parent
     @return parent
    */
    public FileTree getParent(){
        return parent;
    }

    /** set parent
     @param parent parent
    */
    public void setParent(FileTree parent){
        this.parent = parent;
    }

    /** get path
     @return path
    */
    public String getPath(){
        return path;
    }

    /** set path
     @param path path
    */
    public void setPath(String path){
        this.path = path;
        if(path.length() == 0)
            this.name = "";
        else{
            int slash = path.lastIndexOf("/");
            if(slash >=0 )
                this.name = path.substring(slash + 1);
            else
                this.name = path;
        }
    }
    /** get rootFolder
     @return rootFolder
    */
    public RootFolder getRootFolder(){
        return rootFolder;
    }

    /** set rootFolder
     @param rootFolder rootFolder
    */
    public void setRootFolder(RootFolder rootFolder){
        this.rootFolder = rootFolder;
    }
    /** get numFiles
     @return numFiles
    */
    public Number getNumFiles(){
        return numFiles;
    }

    /** set numFiles
     @param numFiles numFiles
    */
    public void setNumFiles(Number numFiles){
        this.numFiles = numFiles;
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



    // insert into file_tree (ID, NAME, parent_id, path, dir, modified) values (1, 'test', NULL, ' ', TRUE, CURRENT_TIMESTAMP());
    public void save(Connection conn)throws SQLException{
        if(id == null){
            if(path.length() != 0){
                if(parent == null){
                    parentId = parentIdByPath(conn);
                }else
                    parentId = parent.id;
            }
            Object[] rets = new QueryRunner().query(conn,
                "select FILE_TREE_SEQ.nextval from dual", new ArrayHandler());
            id = (Number)rets[0];
            String sql = 
            "insert into file_tree (FILE_TREE_ID, FT_NAME, parent_id, path, ROOT_FOLDER_ID, dir, NUM_FILE,FT_CHECK_FLAG, modified) values (?, ?, ?, ?, ?, TRUE, ?, 0, CURRENT_TIMESTAMP())";
            new QueryRunner().update(conn, sql, id, name, parentId, path, rootFolder.id, this.numFiles);
        }
    }
    
    public void increaseFile(Connection conn)throws SQLException{
        if(id == null){
            throw new IronException("FileTree must be saved firstly");
        }else{
            this.numFiles = Integer.valueOf(this.numFiles.intValue() + 1);
            new QueryRunner().update(conn, 
                "update FILE_TREE set NUM_FILE = ? where FILE_TREE_ID=?",
                this.numFiles, this.id);
        }
    }
    
    public void decreaseFile(Connection conn)throws SQLException{
        if(id == null){
            throw new IronException("FileTree must be saved firstly");
        }else{
            int v = this.numFiles.intValue() - 1;
            this.numFiles = Integer.valueOf(v < 0? 0: v);
            new QueryRunner().update(conn, 
                "update FILE_TREE set NUM_FILE = ? where FILE_TREE_ID=?",
                this.numFiles, this.id);
        }
    }
    
    /** create or find existing recursively*/
    protected Number parentIdByPath(Connection conn)throws SQLException{
        String parentPath = "";
        int slash = path.lastIndexOf("/");
        if(slash >=0 ){
            parentPath = path.substring(0, slash);
        }
        Object[] rets = new QueryRunner().query(conn,
            "select FILE_TREE_ID from FILE_TREE where PATH = ? and ROOT_FOLDER_ID = ?",
            new ArrayHandler(), parentPath, rootFolder.id);
        if(rets != null && rets.length > 0){
            return (Number)rets[0];
        }else{
            FileTree ptree = new FileTree();
            ptree.setPath(parentPath);
            ptree.setRootFolder(rootFolder);
            ptree.save(conn);
            return ptree.id;
        }
    }
    
    public SrcFile findSrcFile(Connection conn, String fileName)throws SQLException{
        String sql = "select * from SRC_FILE where FILE_TREE_ID = ? and SF_NAME = ?";
        Map<String, Object> rets = new QueryRunner().query(conn, sql, new MapHandler(), 
          this.id, fileName);
        if(rets != null){
            SrcFile f = new SrcFile();
            f.loadMap(rets);
            f.setFileTree(this);
            return f;
        }
        return null;
    }
    
    public DaoPagination<FileTree> fetchChildFileTree(Connection conn, PagingRequest pr)throws SQLException{
        DaoPagination<FileTree> page = new DaoPagination(pr);
        String sql = "from file_tree f where f.PARENT_ID=?";
        String orderSql = "order by PATH";
        return DAOUtil.loadPage(conn, FileTree.class, page, sql, orderSql, id);
    }
    
    public DaoPagination<SrcFile> fetchSrcFile(Connection conn, PagingRequest pr)throws SQLException{
        DaoPagination<SrcFile> page = new DaoPagination(pr);
        String sql = "from src_file s where s.FILE_TREE_ID=?";
        String orderSql = "order by s.sf_name";
        return DAOUtil.loadPage(conn, SrcFile.class, page, sql, orderSql, id);
    }
    
    public FileTree fetchParent(Connection conn)throws SQLException{
        Map<String, Object> map = new QueryRunner().query(conn, 
          "select * from file_tree where FILE_TREE_ID =?", new MapHandler(), parentId);
        parent = new FileTree();
        parent.loadMap(map);
        return parent;
    }
    
    public static int clearEmpties(Connection conn, String coverPath, Number rootFolderId)throws SQLException{
        String path = coverPath;
        if(coverPath != null && coverPath.length()> 0 && coverPath.charAt(coverPath.length() -1) == '/')
            path = coverPath.substring(0, coverPath.length() -1);
        String pathClause = (path != null && path.length() > 0)? 
                " and (ft.path like '"+ path +"/%' or ft.path = '"+ path +"')" 
                : "";
        
        //int num = new QueryRunner().update(conn, 
        //    "insert into FILE_TREE_EMPTY select ft.FILE_TREE_ID from FILE_TREE ft left join SRC_FILE sf on ft.FILE_TREE_ID=sf.FILE_TREE_ID where ft.ROOT_FOLDER_ID=? and sf.FILE_TREE_ID is null"
        //    + pathClause + "", rootFolderId);
        int num = new QueryRunner().update(conn, 
            "insert into FILE_TREE_EMPTY select ft.FILE_TREE_ID from FILE_TREE ft where ft.ROOT_FOLDER_ID=? "
            + pathClause + " and not exists (select 1 from SRC_FILE sf where ft.FILE_TREE_ID=sf.FILE_TREE_ID ) ", rootFolderId);
        StringBuilder sb = new StringBuilder();
        //sb.append("delete from FILE_TREE where FILE_TREE_ID in ");
        //sb.append("(select ft.FILE_TREE_ID from FILE_TREE ft left join FILE_TREE c on ft.FILE_TREE_ID=c.PARENT_ID where c.FILE_TREE_ID is null and ft.ROOT_FOLDER_ID = ?"+ 
        //    pathClause +" ) ");
        //sb.append("and FILE_TREE_ID in (select FILE_TREE_ID from FILE_TREE_EMPTY)");
        
        sb.append("delete from FILE_TREE ft where ft.ROOT_FOLDER_ID = ? "+ pathClause + " and not exists ");
        sb.append("(select 1 from FILE_TREE c where ft.FILE_TREE_ID=c.PARENT_ID) ");
        sb.append("and exists (select 1 from FILE_TREE_EMPTY e where e.FILE_TREE_ID = ft.FILE_TREE_ID)");
        
        int delNum = new QueryRunner().update(conn, sb.toString(), rootFolderId);
        int totalDel = delNum;
        while(delNum > 0){
            log.fine("delete folders: "+ delNum);
            delNum = new QueryRunner().update(conn, sb.toString(), rootFolderId);
            totalDel += delNum;
        }
        log.fine(sb.toString());
        return totalDel;
    }
    
    public static void  clearTemp(Connection conn)throws SQLException{
        new QueryRunner().update(conn, "delete from FILE_TREE_EMPTY");
    }
    
    public void loadByPath(Connection conn)throws SQLException{
        if(path != null && rootFolder != null){
            if(log.isLoggable(Level.FINE))
                log.fine("load file_tree by path..." + path);
            Object[] rets = new QueryRunner().query(conn,
            "select FILE_TREE_ID, NAME from FILE_TREE where PATH = ? and ROOT_FOLDER_ID = ?",
            new ArrayHandler(), path, rootFolder.id);
            this.name = (String)rets[1];
            this.id = (Number)rets[0];
        }else{
            log.warning("lack of query info: tree_file's path and root_folder_id");
        }
    }
    @Override
     protected void loadMap(Map<String, Object> dataMap){
        try{
            id = (Number)dataMap.get("FILE_TREE_ID");
            name = (String)dataMap.get("FT_NAME");
            path = (String)dataMap.get("PATH");
            numFiles = (Number)dataMap.get("NUM_FILE");
            checkFlag = (Number)dataMap.get("FT_CHECK_FLAG");
            parentId = (Number)dataMap.get("PARENT_ID");
        }catch(Exception ex){
            throw new IronException("", ex);
        }
    }
    
    public String toString(){
        return path.length() == 0? "<ROOT>": path + "/";
    }
  
}
