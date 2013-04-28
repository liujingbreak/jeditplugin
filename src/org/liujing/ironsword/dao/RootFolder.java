package org.liujing.ironsword.dao;

import org.apache.commons.dbutils.*;
import org.apache.commons.dbutils.handlers.*;
import java.util.*;
import java.sql.*;
import java.io.*;
import java.util.logging.*;
import java.text.SimpleDateFormat;
import org.liujing.ironsword.bean.*;
import org.liujing.ironsword.IronException;
import org.directwebremoting.annotations.*;

@DataTransferObject
public class RootFolder extends SqlDAO{
    private static Logger log = Logger.getLogger(RootFolder.class.getName());
    protected Number id;
    private String path;
    private List<String> includes;
    private List<String> excludes;
    private Timestamp scanDate;
    private static SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SrcFileRowHandler srcFileRowHandler = new SrcFileRowHandler();
    
    public RootFolder(){}
    /** get id
     @return id
    */
    public Number getId(){
        return id;
    }

    /** set id
     @param id id
    */
    public void setId(Number id){
        this.id = id;
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
    }

    /** get includes
     @return includes
    */
    public List<String> getIncludes(){
        return includes;
    }

    /** set includes
     @param includes includes
    */
    public void setIncludes(List<String> includes){
        this.includes = includes;
    }

    /** get excludes
     @return excludes
    */
    public List<String> getExcludes(){
        return excludes;
    }

    /** set excludes
     @param excludes excludes
    */
    public void setExcludes(List<String> excludes){
        this.excludes = excludes;
    }
    /** get scanDate
     @return scanDate
    */
    public Timestamp getScanDate(){
        return scanDate;
    }

    /** set scanDate
     @param scanDate scanDate
    */
    public void setScanDate(Timestamp scanDate){
        this.scanDate = scanDate;
    }
    
    public ListPage<FileTree> fetchOneLvlFileTree(Connection conn, String path,
        PagingRequest pr)throws SQLException
    {
        
        if(path == null)
            path = "";
        String sql = "from file_tree f "
        +" join file_tree p on f.parent_id=p.file_tree_id where p.ROOT_FOLDER_ID = ? and p.path=?";
        //+" where f.parent_id in (select p.file_tree_id from file_tree p where p.ROOT_FOLDER_ID = ? and p.path=?)";
        //+" where exists (select 1 from file_tree p where p.ROOT_FOLDER_ID = ? and p.path=? and f.parent_id=p.file_tree_id)";
        ListPage<FileTree> page = DAOUtil.loadPage(conn, 
            new ListPage<FileTree>(pr),
            "f.*", sql, "order by f.FT_NAME", createResultSetHandler(FileTree.class), this.id, path);
        
        return page;
    }
    
    /**  fetchFileTree
     @param conn conn
     @param path path
     @param onlyContainFile only return those folders which contain sub-files
     @param page page
     @return DaoPagination<FileTree>
     @throws SQLException if SQLException occurs
    */
    public DaoPagination<FileTree> fetchFileTree(Connection conn, String path,
        boolean onlyContainFile, DaoPagination page)throws SQLException
    {
        // total count
        String cond = "";
        List params = new ArrayList();
        params.add(this.id);
        if(path != null && path.length() > 0){
            cond = " and t.PATH like ? ";
            params.add(path.endsWith("/")? (path+ "%"): (path + "/%"));
        }
        if(onlyContainFile){
            cond += " and exists (select 1 from SRC_FILE s where s.file_tree_id = t.file_tree_id)";// NUM_FILE > 0
            //cond += " and t.file_tree_id in (select s.file_tree_id from SRC_FILE s)";// NUM_FILE > 0
        }
        if(page.getOffset() == 0){
          Object[] rets = new QueryRunner().query(conn,
              "select count(*) from FILE_TREE t where t.ROOT_FOLDER_ID = ?"+ cond,
          new ArrayHandler(), params.toArray(new Object[0]));
          
          page.setTotal(((Number)rets[0]).intValue());
        }
        // detail records fetch
        params.add(page.getOffset());
        params.add(page.getLimit() + 1);
        
        List<Map<String,Object>> list = new QueryRunner().query(conn,
            "select * from FILE_TREE t where t.ROOT_FOLDER_ID = ? " + cond 
            +" order by t.PATH limit ?, ?", new MapListHandler(), params.toArray(new Object[0]));
               
        boolean hasMore = list.size() > page.getLimit();
        
        FileTree[] folders = new FileTree[hasMore ? page.getLimit() : list.size()];
        int i = 0;
        for(Map<String,Object> rowMap : list){
            if(i >= folders.length) break;
            folders[i] = new FileTree();
            folders[i++].loadMap(rowMap);
        }
        //DaoPagination<FileTree> page = new DaoPagination(offset, folders.length);
        //page.setSize(folder.length);
        page.setMore(hasMore);
        page.setData(folders);
        return page;
    }
    
    public DaoPagination<SrcFile> listFileByPath(Connection conn, String path,
        PagingRequest pr)throws SQLException
    {
        if(path == null)
            path = "";
        String sql = "select count(s.SRC_FILE_ID) from SRC_FILE s join FILE_TREE f on f.FILE_TREE_ID = s.FILE_TREE_ID ";
        sql += "where f.ROOT_FOLDER_ID = ? and f.PATH = ?";
        DaoPagination<SrcFile> page = null;
        if(pr instanceof DaoPagination)
            page = (DaoPagination<SrcFile>)pr;
        else
            page = new DaoPagination(pr);
        if(pr.getOffset() == 0){
            Object[] rets = new QueryRunner().query(conn, sql, new ArrayHandler(),
                this.id, path);
            
            page.setTotal(((Number)rets[0]).intValue());
        }
        
        sql = "select s.*, f.* from SRC_FILE s join FILE_TREE f on f.FILE_TREE_ID = s.FILE_TREE_ID ";
        sql += "where f.ROOT_FOLDER_ID = ? and f.PATH = ? limit ?, ?";
        List<Map<String,Object>> list = new QueryRunner().query(conn, sql, new MapListHandler(),
            this.id, path, page.getOffset(), page.getLimit() + 1);
        
        boolean hasMore = list.size() > page.getLimit();
        SrcFile[] files = new SrcFile[hasMore ? page.getLimit() : list.size()];
        int i = 0;
        for(Map<String,Object> rowMap : list){
            if(i >= files.length) break;
            files[i] = new SrcFile();
            FileTree folder = new FileTree();
            folder.loadMap(rowMap);
            files[i].setFileTree(folder);
            files[i].loadMap(rowMap);
            i++;
        }
        //DaoPagination page = new DaoPagination(offset, folders.length);
        page.setMore(hasMore);
        page.setData(files);
        return page;
    }
    
    public FileTree findFileTree(Connection conn, String path)throws SQLException{
        String sql = "select * from FILE_TREE f where f.PATH = ? and f.ROOT_FOLDER_ID = ?";
        Map<String, Object> rets = new QueryRunner().query(conn, sql, new MapHandler(),
          path, this.id);
        //log.info(" path="+ path + ", id=" + id);
        if(rets == null)
          return null;
        FileTree ft = new FileTree();
        ft.loadMap(rets);
        return ft;
    }
    
    public SrcFile findSrcFile(Connection conn, String parentPath, String fileName)throws SQLException{
        String sql = "select s.*, f.* SRC_FILE s left join FILE_TREE f left join ROOT_FOLDER r on f.ROOT_FOLDER_ID  = r.ROOT_FOLDER_ID  on s.FILE_TREE_ID = f.FILE_TREE_ID"
        + " where f.PATH = ? and f.ROOT_FOLDER_ID = ? and s.SF_NAME = ?";
        Map<String, Object> rets = new QueryRunner().query(conn, sql, new MapHandler(), parentPath, this.id, fileName);
        if(rets !=null){
          SrcFile sf = new SrcFile();
          sf.loadMap(rets);
          FileTree ft = new FileTree();
          ft.loadMap(rets);
          sf.setFileTree(ft);
          ft.setRootFolder(this);
          return sf;
        }else
          return null;
    }
    
    public ListPage<SrcFile> listSrcFile(Connection conn, ListPage<SrcFile> page)throws SQLException{
        ListPage<SrcFile> res = DAOUtil.loadPageByRow(conn, page, "*",
            "from src_file s join file_tree f on s.FILE_TREE_ID=f.FILE_TREE_ID where s.ROOT_FOLDER_ID=?",
            "order by file_tree_id", srcFileRowHandler, id);
        return res;
    }
    
    private class SrcFileRowHandler implements ResultMapHandler<SrcFile>{
        public SrcFile handleRowMap(Map<String, Object> row){
            SrcFile s = new SrcFile();
            s.loadMap(row);
            FileTree f = new FileTree();
            f.loadMap(row);
            s.setFileTree(f);
            f.setRootFolder(RootFolder.this);
            return s;
        }
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Path: ");
        sb.append(path);
        if(includes != null && includes.size() > 0){
            sb.append("\tincludes: ");
            for(String s : includes){
                sb.append(s);
                sb.append(";");
            }
            sb.delete(sb.length() - 1, sb.length());
        }
        if(excludes != null && excludes.size() > 0){
            sb.append("\texcludes: ");
            for(String s : excludes){
                sb.append(s);
                sb.append(";");
            }
            sb.delete(sb.length() - 1, sb.length());
        }
        if(scanDate != null)
            sb.append("\n\t[ last scan date: ").append(dateformat.format(scanDate)).
        append(" ]");
        else
            sb.append("\n\t[ no scan ]");
        return sb.toString();
    }
    public void save(Connection conn)throws SQLException{
        if(includes == null) includes = new ArrayList();
        if(excludes == null) excludes = new ArrayList();
        if(id == null){
            Object[] rets = new QueryRunner().query(conn,
            "select ROOT_FOLDER_SEQ.nextval from dual", new ArrayHandler());
            id = (Number)rets[0];
            
            new QueryRunner().update(conn,
                "insert into ROOT_FOLDER values (?, ?, ?, ?, ?)",
                id, path, createBlog(conn,includes), createBlog(conn, excludes), scanDate);
        }else{
            new QueryRunner().update(conn,
                "update ROOT_FOLDER set ROOT_PATH=?, INCLUDES=?, EXCLUDES=?, RF_SCAN_DATE=? where ROOT_FOLDER_ID =?",
                path, createBlog(conn, includes), createBlog(conn, excludes), scanDate, id);
        }
    }
    
    private static Blob createBlog(Connection conn, Object o)throws SQLException{
        try{
            Blob blob = conn.createBlob();
            ObjectOutputStream out = new ObjectOutputStream(blob.setBinaryStream(1));
            out.writeObject(o);
            out.close();
            return blob;
        }catch(IOException ioe){
            throw new IronException("Failed to Serialize", ioe);
        }
    }
    
    public static DaoPagination listAll(Connection conn, PagingRequest pr)
    throws SQLException, IOException{
        DaoPagination page = new DaoPagination(pr);
        if(pr.getOffset() == 0){
            Object[] rets = new QueryRunner().query(conn, "select count(*) from ROOT_FOLDER",
                new ArrayHandler());
            
            page.setTotal(((Number)rets[0]).intValue());
        }
        List<Map<String,Object>> list = new QueryRunner().query(conn,
            "select * from ROOT_FOLDER order by root_folder_id limit ?, ?", new MapListHandler(), page.getOffset(), page.getLimit() + 1);
        boolean hasMore = list.size() > pr.getLimit();
        
        RootFolder[] folders = new RootFolder[hasMore ? pr.getLimit() : list.size()];
        int i = 0;
        for(Map<String,Object> rowMap : list){
            if(i >= folders.length) break;
            folders[i] = new RootFolder();
            folders[i++].loadMap(rowMap);
        }
        //DaoPagination page = new DaoPagination(offset, folders.length);
        page.setMore(hasMore);
        page.setData(folders);
        return page;
    }
    
    /**  findByIndex
     @param conn conn
     @param index index
     @return null if not found
     @throws SQLException if SQLException occurs
    */
    public static RootFolder findByIndex(Connection conn, int index)throws SQLException
    {
        Map<String,Object> result = new QueryRunner().query(conn,
            "select * from ROOT_FOLDER order by ROOT_FOLDER_ID limit ?, 1", new MapHandler(), index);
        if(result == null || result.size() == 0)
            return null;
        RootFolder r = new RootFolder();
        r.loadMap(result);
        return r;
    }
    
    public static RootFolder getById(Connection conn, int id)throws SQLException{
        Map<String,Object> result = new QueryRunner().query(conn,
            "select * from ROOT_FOLDER where ROOT_FOLDER_ID=?", new MapHandler(), id);
        if(result == null || result.size() == 0)
            return null;
        RootFolder r = new RootFolder();
        r.loadMap(result);
        return r;
    }
    
    public static RootFolder findByPath(Connection conn, String path)throws SQLException{
        Map<String,Object> result = new QueryRunner().query(conn,
            "select * from ROOT_FOLDER where ROOT_PATH = ?", new MapHandler(), path);
        if(result == null || result.size() == 0)
            return null;
        RootFolder r = new RootFolder();
        r.loadMap(result);
        return r;
    }
    
    public int delete(Connection conn)throws SQLException{
        return new QueryRunner().update(conn,
            "delete from ROOT_FOLDER where ROOT_FOLDER_ID =?", id);
    }
    
    protected void loadMap(Map<String, Object> dataMap){
        try{
            id = (Number)dataMap.get("ROOT_FOLDER_ID");
            path = (String)dataMap.get("ROOT_PATH");
            Blob bIncludes = (Blob)dataMap.get("INCLUDES");
            if(bIncludes != null){
                ObjectInputStream objin = new ObjectInputStream(
                    bIncludes.getBinaryStream());
                includes = (List<String>)objin.readObject();
            }
            Blob bExcludes = (Blob)dataMap.get("EXCLUDES");
            if(bExcludes != null){
                ObjectInputStream objin = new ObjectInputStream(
                    bExcludes.getBinaryStream());
                excludes = (List<String>)objin.readObject();
            }
            scanDate = (Timestamp)dataMap.get("RF_SCAN_DATE");
        }catch(Exception ex){
            throw new IronException("", ex);
        }
    }
    
    public FileTree getFileTree(Connection conn, String path)throws SQLException{
        Map<String,Object> result = new QueryRunner().query(conn,
            "select * from FILE_TREE where path = ? and ROOT_FOLDER_ID =?",
            new MapHandler(), path, id);
        if(result == null || result.size() == 0)
            return null;
        else{
            FileTree ft = new FileTree();
            ft.loadMap(result);
            ft.setRootFolder(this);
            return ft;
        }
    }
    
    public void resetFlag(Connection conn, String underPath, int value)
    throws SQLException
    {
        int num = SrcFile.resetFlag(conn, value, underPath, this.id);
        log.fine(num + " files are reset");
    }
    
    public int deleteByFlag(Connection conn, String underPath, int value)throws SQLException{
        int num = SrcFile.deleteFilesByFlag(conn, value, this.id);
        log.fine(num + " files are removed");
        int fnum = FileTree.clearEmpties(conn, underPath, this.id);
        log.fine(fnum + " folders are removed");
        return num;
    }
    
    public int deleteNotInTemp(Connection conn, String underPath)throws SQLException{
        int delFileNum = SrcFile.deleteNotInTemp(conn, id, underPath);
        log.fine(delFileNum + " files are removed");
        int delFoldernum = FileTree.clearEmpties(conn, underPath, this.id);
        log.fine(delFoldernum + " folders are removed");
        return delFileNum;
    }
    
    //public void saveSrcFile(Connection conn, SrcFile f)throws SQLException{
    //    if(id == null){
    //        save(conn);
    //    }
    //    f.save(conn, this);
    //}
}
