package org.liujing.ironsword.dao;

import org.apache.commons.dbutils.*;
import org.apache.commons.dbutils.handlers.*;
import java.util.*;
import java.sql.*;
import java.io.*;
import java.util.logging.*;
import org.liujing.ironsword.IronException;
import org.liujing.ironsword.bean.*;
import java.text.SimpleDateFormat;

public class SnapShotDAO extends SqlDAO{
    private static Logger log = Logger.getLogger(SnapShotDAO.class.getName());
    
    private Number id;
    private Timestamp time;
    private String desc;
    private static SimpleDateFormat formater = new SimpleDateFormat("YYYY-MM-dd HH:mm");
    
    public SnapShotDAO(){
        
    }
    
    public Number getId(){
        return id;
    }

    public void setId(Number id){
        this.id = id;
    }

    public Timestamp getTime(){
        return time;
    }
    
    public String getDesc(){
        return desc;
    }

    public void setDesc(String desc){
        this.desc = desc;
    }

    public static DaoPagination<SnapShotDAO> listAll(Connection conn, PagingRequest pr)
    throws SQLException
    {
        DaoPagination<SnapShotDAO> listpage = null;
        if(pr instanceof DaoPagination)
            listpage = (DaoPagination<SnapShotDAO>) pr;
        else
            listpage = new DaoPagination(pr);
        return DAOUtil.loadPage(conn, SnapShotDAO.class, listpage, "from snap_shot", 
            "order by ss_time desc");
        
    }
    
    public void save(Connection conn)throws SQLException{
        
        Object[] rets = new QueryRunner().query(conn,
        "select snap_shot_seq.nextval from dual", new ArrayHandler());
        id = (Number)rets[0];
        
        new QueryRunner().update(conn,
            "insert into snap_shot values (?, CURRENT_TIMESTAMP, ?)", id, desc);
    }
    
    public void delete(Connection conn)throws SQLException{
        new QueryRunner().update(conn,
            "delete from snap_shot where ss_id=?", id);
    }


    protected void loadMap(Map<String, Object> rowData){
        id = (Number)rowData.get("ss_id");
        time = (Timestamp)rowData.get("ss_time");
        desc = (String)rowData.get("ss_desc");
    }
    
    public String toString(){
        return formater.format(time) + " "+ desc;
    }
}
