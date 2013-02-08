package org.liujing.ironsword.dao;

import java.util.*;
import java.sql.*;
import java.io.*;
import java.util.logging.*;
import org.apache.commons.dbutils.*;
import org.apache.commons.dbutils.handlers.*;

public abstract class SqlDAO{
    private static Logger log = Logger.getLogger(SqlDAO.class.getName());
    /**
    load one row from a map data
    */
    protected abstract void loadMap(Map<String, Object> rowData);
    
    public static <T extends SqlDAO> ResultSetHandler<List<T>> createResultSetHandler(Class<T> cls){
        return new RSHandler(cls);
    }
    
    protected static class RSHandler<T extends SqlDAO> extends AbstractListHandler<T>{
        private PublicMapListHandler maph = new PublicMapListHandler();
        private Class<T> cls;
        public RSHandler(Class<T> cls){
            this.cls = cls;
        }
        protected T handleRow(ResultSet rs)throws SQLException{
            try{
                T dao = cls.newInstance();
                Map<String, Object> map = maph.handleRow(rs);
                dao.loadMap(map);
                return dao;
            }catch(InstantiationException ie){
                log.log(Level.SEVERE, "", ie);
                return null;
            }catch(IllegalAccessException le){
                log.log(Level.SEVERE, "", le);
                return null;
            }
        }
    }
    
    
    
    protected static class PublicMapListHandler extends MapListHandler{
        public  Map<String,Object> handleRow(ResultSet rs)throws SQLException{
            return super.handleRow(rs);
        }
    }
}
