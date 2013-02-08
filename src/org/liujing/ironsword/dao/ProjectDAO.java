package org.liujing.ironsword.dao;

import org.apache.commons.dbutils.*;
import org.apache.commons.dbutils.handlers.*;
import java.util.*;
import java.sql.*;
import java.io.*;
import java.util.logging.*;
import org.liujing.ironsword.bean.*;
import org.liujing.ironsword.IronException;
import org.directwebremoting.annotations.*;
@DataTransferObject
public class ProjectDAO extends SqlDAO{
    private static Logger log = Logger.getLogger(ProjectDAO.class.getName());
    
    private Number id;
    private String name;
    private String desc;
    
    public static final int FIND_NAME_START = 1;
    public static final int FIND_NAME_CONTAIN = 2;
    public static final int FIND_NAME_IS = 0;
    
    public ProjectDAO(){
    }
    
    public Number getId(){
        return id;
    }

    public void setId(Integer id){
        this.id = id;
    }


    public static DaoPagination<ProjectDAO> listProjects(Connection conn,
        PagingRequest page)throws SQLException
    {
        return DAOUtil.loadPage(conn, ProjectDAO.class, new DaoPagination<ProjectDAO>(page), "from PROJECT", 
             "order by project_id");
    }
    
    protected void loadMap(Map<String, Object> dataMap){
        id = (Number)dataMap.get("PROJECT_ID");
        name = (String)dataMap.get("PROJECT_NAME");
        desc = (String)dataMap.get("PROJECT_DESC");
    }
    
    
    public void save(Connection conn)throws SQLException{
        if(this.id == null){
            Object[] rets = new QueryRunner().query(conn,
            "select PROJECT_SEQ.nextval from dual", new ArrayHandler());
            id = (Number)rets[0];
            
            new QueryRunner().update(conn,
                "insert into PROJECT (project_id, project_name, project_desc, MODIFIED) values (?, ?, ?, CURRENT_TIMESTAMP())",
                id, name, desc);
        }else{
            new QueryRunner().update(conn,
                "update PROJECT set project_name=?, project_desc=?, MODIFIED=CURRENT_TIMESTAMP() where project_id=?",
                name, desc, id);
        }
    }
    
    public void linkFolder(Connection conn, RootFolder folder)throws SQLException{
        new QueryRunner().update(conn,
                "insert into PROJECT_FOLDERS (project_id, ROOT_FOLDER_ID) values (?, ?)",
                id, folder.id);
    }
    
    public void unlinkFolder(Connection conn, RootFolder folder)throws SQLException{
        new QueryRunner().update(conn,
                "delete from PROJECT_FOLDERS where project_id=? and ROOT_FOLDER_ID=?",
                id, folder.id);
    }
    
    public DaoPagination<RootFolder> listUnlikedFolders(Connection conn, PagingRequest pr)
    throws SQLException{
        String fromSql = "from ROOT_FOLDER rf where not exists (select 1 from PROJECT_FOLDERS pf where pf.project_id = ? and ROOT_FOLDER_ID = rf.ROOT_FOLDER_ID)";
        DaoPagination<RootFolder> page = null;
        if(pr instanceof DaoPagination)
            page = (DaoPagination<RootFolder>) pr;
        else
            page = new DaoPagination(pr);
        return DAOUtil.loadPage(conn, RootFolder.class, page, fromSql, null,
            this.getId());
    }
    
    public void delete(Connection conn)throws SQLException{
        new QueryRunner().update(conn,
                "delete from PROJECT where project_id=?",
                id);
    }
    
    public static int deleteAll(Connection conn, int[] ids)throws SQLException{
        StringBuilder sb = new StringBuilder();
        int i =0;
        for(int id : ids){
            if(i> 0)
                sb.append(",");
            sb.append(String.valueOf(id));
            i++;
        }
        String idList = sb.toString();
        log.fine("delete by id: "+ idList);
        return new QueryRunner().update(conn,
                "delete from PROJECT where project_id in ("+ idList + ")");
        
    }
    
    public DaoPagination<RootFolder> listFolders(Connection conn, DaoPagination<RootFolder> page)
    throws SQLException{
        return DAOUtil.loadPage(conn, RootFolder.class, page, 
            "from (select p.*, pf.root_folder_id rdid from project p inner join project_folders pf on pf.project_id =p.project_id where p.project_id=?) pff inner join root_folder  r on pff.rdid= r.root_folder_id"
            , " order by root_folder_id", id);
    }
    
    public RootFolder getFolderByIndex(Connection conn, int index)throws SQLException{
        Map<String,Object> result = new QueryRunner().query(conn,
            "select r.* from (select p.*, pf.root_folder_id rdid from project p left join project_folders pf on pf.project_id =p.project_id where p.project_id=?) pff left join root_folder  r on pff.rdid= r.root_folder_id  order by root_folder_id limit ?, 1",
            new MapHandler(), id, index);
        if(result == null || result.size() == 0)
            return null;
        RootFolder r = new RootFolder();
        r.loadMap(result);
        return r;
    }
    
    public static ProjectDAO findByName(Connection conn, String name)throws SQLException{
        Map<String,Object> row= new QueryRunner().query(conn, 
            "select * from PROJECT where project_name = ?",
                new MapHandler(), name);
        if(row != null){
            ProjectDAO dao = new ProjectDAO();
            dao.loadMap(row);
            return dao;
        }else{
            return null;
        }
    }
    
    public static ProjectDAO getById(Connection conn, int id)throws SQLException{
        Map<String,Object> row= new QueryRunner().query(conn, 
            "select * from PROJECT where PROJECT_ID = ?",
                new MapHandler(), id);
        if(row != null){
            ProjectDAO dao = new ProjectDAO();
            dao.loadMap(row);
            return dao;
        }else{
            return null;
        }
    }
    
    public ListPage findFileTreeByName(Connection conn, String name,
        int findNameOption, PagingRequest pr)throws SQLException
    {
        return findFileTreeByField(conn, name, findNameOption, "FT_NAME", pr);
    }
    
    public ListPage findFileTreeByPath(Connection conn, String name,
        int findNameOption, PagingRequest pr)throws SQLException
    {
        return findFileTreeByField(conn, name, findNameOption, "PATH", pr);
    }
    
    private ListPage findFileTreeByField(Connection conn, String name,
        int findNameOption, String field, PagingRequest pr)throws SQLException
    {
        String cond = "", nameValue = null;
        if(findNameOption == FIND_NAME_START){
            cond = " like ?";
            nameValue = name.toUpperCase() + "%";
        }else if(findNameOption == FIND_NAME_CONTAIN){
            cond = " like ?";
            nameValue = "%" + name.toUpperCase() + "%";
        }else if(findNameOption == FIND_NAME_IS){
            cond = " = ?";
            nameValue = name.toUpperCase();
        }
        
        String sql = " from root_folder R  join file_tree F on  F.root_folder_id = R.root_folder_id "+ 
            " where F.root_folder_id in (select root_folder_id from project_folders where project_id = ?)  and UPPER(F."+ field+ ") "+ cond;
        
        ListPage<Object[]> page = null;
        if(pr != null && pr instanceof ListPage)
            page = (ListPage)pr;
        else
            page = new ListPage(pr);
        Object params = null;
        
        DAOUtil.loadPage(conn, page, 
        "F.path, R.root_path, F.file_tree_id, R.root_folder_id", sql, "order by R.root_folder_id, F.path",
        new ArrayListHandler(), this.id, nameValue);
        
        return page;
    }
    
    /**
    @param suffixName if value is empty or '*', it will be ignore
    */
    public ListPage findFilesByName(Connection conn, String name,
        int findNameOption, String suffixName, PagingRequest pr)throws SQLException
    {
        boolean byName = name != null && !name.equals("") && !name.equals("*");
        boolean bySuffix = (suffixName != null && !suffixName.equals("*") && !suffixName.equals(""));
        List<Object> params = new ArrayList(2);
        params.add(this.id);
        String cond = "", nameValue = null;
        if(byName){
            cond = " and UPPER(S.sf_name)";
            if(findNameOption == FIND_NAME_START){
                cond += " like ?";
                nameValue = name.toUpperCase() + "%";
            }else if(findNameOption == FIND_NAME_CONTAIN){
                cond += " like ?";
                nameValue = "%" + name.toUpperCase() + "%";
            }else if(findNameOption == FIND_NAME_IS){
                cond += " = ?";
                nameValue = name.toUpperCase();
            }
            params.add(nameValue);
        };
        
        if(bySuffix){
            cond += " and SRC_TYPE=?";
            params.add(suffixName.trim());
        }
        //String sql = "from src_file S inner join file_tree F on S.file_tree_id=F.file_tree_id inner join root_folder R on F.root_folder_id = R.root_folder_id"+
        //    " where  UPPER(S.sf_name) "+ cond+ " and F.root_folder_id in (select root_folder_id from project_folders where project_id = ?) ";
        String sql = " from root_folder R  join file_tree F on  F.root_folder_id = R.root_folder_id join src_file S on S.file_tree_id=F.file_tree_id"+ 
            " where F.root_folder_id in (select root_folder_id from project_folders where project_id = ?) "+ cond;
        ListPage<Object[]> page = null;
        if(pr != null && pr instanceof ListPage)
            page = (ListPage)pr;
        else
            page = new ListPage(pr);
        
        String columns = "S.sf_name, F.path, R.root_path, F.file_tree_id, R.root_folder_id, S.src_file_id";
        DAOUtil.loadPage(conn, page, columns, sql, "order by R.root_folder_id, F.path, S.sf_name",
            new ArrayListHandler(), params.toArray(new Object[0]));
        //if(!bySuffix){
        //    DAOUtil.loadPage(conn, page, columns, sql, "order by R.root_folder_id, F.path, S.sf_name",
        //    new ArrayListHandler(), this.id, nameValue);
        //}else{
        //    DAOUtil.loadPage(conn, page, columns, sql, "order by R.root_folder_id, F.path, S.sf_name",
        //    new ArrayListHandler(), this.id, nameValue, suffixName);
        //}
        
        /*
        
         select s.*, j.* from src_file s inner join 
         (select f.*, r.root_path from file_tree f inner join root_folder r on f.root_folder_id = r.root_folder_id where exists 
            (select 1 from project_folders pf where pf.root_folder_id = f.root_folder_id and pf.project_id = 9) )
            j on  s.file_tree_id = j.file_tree_id where s.sf_name like 'M%'
         (7 rows, 14 ms)
         
         select S.*, F2.* from src_file S inner join 
         ( select F.* from  file_tree F inner join 
            ( select r.root_folder_id  from root_folder r inner join project_folders pf on r.root_folder_id = pf.root_folder_id where pf.project_id = 9) J 
            on F.root_folder_id = J.root_folder_id )F2
         on S.file_tree_id = F2.file_tree_id   where S.sf_name like 'M%'
         
         (7 rows, 68 ms)
         
         select * from src_file S, file_tree F, root_folder R 
         where S.file_tree_id=F.file_tree_id and F.root_folder_id = R.root_folder_id and  S.file_tree_id in (select file_tree_id from file_tree where root_folder_id in (select root_folder_id from project_folders where project_id = 9) ) and S.sf_name like 'M%'
         (7 rows, 6 ms)
         
         select * from src_file S  inner join (select F0.file_tree_id, F0.path, R.* from file_tree F0 inner join root_folder R on  F0.root_folder_id = R.root_folder_id ) F on S.file_tree_id = F.file_tree_id 
where S.file_tree_id in (select file_tree_id from file_tree where root_folder_id in (select root_folder_id from project_folders where project_id = 9) ) and S.sf_name like 'M%'
        (7 rows, 11 ms)
        
        
        select * from (select S.*, F.path, F.root_folder_id from src_file S  inner join file_tree F on S.file_tree_id = F.file_tree_id) S2 inner join 
  root_folder R on R.root_folder_id = S2.root_folder_id
where S2.file_tree_id in (select file_tree_id from file_tree where root_folder_id in (select root_folder_id from project_folders where project_id = 9) ) and S2.sf_name like 'M%'
        (7 rows, 15 ms)
        
        select * from src_file S  inner join (select F0.* from file_tree F0 inner join root_folder R on  F0.root_folder_id = R.root_folder_id ) F on S.file_tree_id = F.file_tree_id 
        where exists (select 1 from (select file_tree_id from file_tree where exists ( select 1 from  (select root_folder_id from project_folders where project_id = 9) PF  where PF.root_folder_id= file_tree .root_folder_id   )    ) F1  where 
        S.file_tree_id = F1.file_tree_id) and S.sf_name like 'M%'
        
        (7 rows, 21 ms)
        
        select * from src_file S, file_tree F, root_folder R 
where S.file_tree_id=F.file_tree_id and F.root_folder_id = R.root_folder_id and 
 F.root_folder_id in (select root_folder_id from project_folders where project_id = 9)  and S.sf_name like 'M%'
        (7 rows, 4 ms)
        
        select * from src_file S inner join file_tree F on S.file_tree_id=F.file_tree_id inner join root_folder R on F.root_folder_id = R.root_folder_id
where F.root_folder_id in (select root_folder_id from project_folders where project_id = 9)  and S.sf_name like 'M%'
         (7 rows, 3 ms)
         Fastest:
         select * from root_folder R  join file_tree F on  F.root_folder_id = R.root_folder_id join src_file S on S.file_tree_id=F.file_tree_id 
where F.root_folder_id in (select root_folder_id from project_folders where project_id = 1)  and S.sf_name like 'M%'

        select * from src_file S inner join file_tree F on S.file_tree_id=F.file_tree_id inner join root_folder R on F.root_folder_id = R.root_folder_id
where  S.sf_name like 'M%' and F.root_folder_id in (select root_folder_id from project_folders where project_id = 1) 
        
         */
        return page;
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

    /** get desc
     @return desc
    */
    public String getDesc(){
        return desc;
    }

    /** set desc
     @param desc desc
    */
    public void setDesc(String desc){
        this.desc = desc;
    }


    public String toString(){
        return "Project " + name + ", "+ desc;
    }
}
