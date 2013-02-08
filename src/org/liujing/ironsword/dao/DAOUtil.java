package org.liujing.ironsword.dao;

import org.apache.commons.dbutils.*;
import org.apache.commons.dbutils.handlers.*;
import java.util.*;
import java.sql.*;
import java.io.*;
import java.util.logging.*;
import org.liujing.ironsword.bean.*;
import org.liujing.ironsword.IronException;

public class DAOUtil{
    //static Pattern orderByPat = Pattern.compile("order\\s+by\\s+[a-zA-Z0-9_]");
    /**
    Used to return a simple list page of SqlDAO
    */
    public static <T extends SqlDAO> DaoPagination<T> loadPage(Connection conn,
        Class<T> cls, DaoPagination<T> page, String fromSQL, String orderByClause,
        Object... params)throws SQLException
    {
        try{
            Object[] fullParams = new Object[params.length + 2];
            System.arraycopy(params, 0, fullParams, 0, params.length);
            fullParams[fullParams.length - 2] = page.getOffset();
            fullParams[fullParams.length - 1] = page.getLimit() + 1;
            if(page.getOffset() == 0){
                Object[] rets = new QueryRunner().query(conn, "select count(*) "+ fromSQL,
                   new ArrayHandler(),params);
                page.setTotal(((Number)rets[0]).intValue());
            }
            
            List<Map<String,Object>> list = new QueryRunner().query(conn,
                "select * "+ fromSQL + (orderByClause==null?"":" "+ orderByClause) +" limit ?, ?", new MapListHandler(), fullParams
                );
            
            boolean hasMore = list.size() > page.getLimit();
            
            int size = hasMore ? page.getLimit() : list.size();
            T[] arrays = (T[]) java.lang.reflect.Array.newInstance(cls, size); 
            int i = 0;
            for(Map<String,Object> rowMap : list){
                if(i >= arrays.length) break;
                arrays[i] = cls.newInstance();
                arrays[i].loadMap(rowMap);
                i++;
            }
            page.setMore(hasMore);
            page.setData(arrays);
            return page;
        }catch(InstantiationException e){
            throw new IronException("", e);
        }catch(IllegalAccessException ie){
            throw new IronException("", ie);
        }
    }
    
    /**
    More flexible way to fetch a list of object which can be composite SqlObject
    or simple Map etc.
    @param h user defined ResultMapHandler, user may define theire own ResultMapHandler
    to create special type row object
    */
    public static <T> ListPage<T> loadPageByRow(Connection conn, ListPage<T> page, 
        String columnClause ,String fromClause, String orderByClause,
        ResultMapHandler<T> h, Object... params)throws SQLException
    {
        return loadPage(conn, page, columnClause, fromClause, orderByClause,
            new FlexibleRSHandler(h), params);
    }
    
    protected static class FlexibleRSHandler<T> extends AbstractListHandler<T>{
        private SqlDAO.PublicMapListHandler maph = new SqlDAO.PublicMapListHandler();
        
        private ResultMapHandler<T> rmh;
        
        public FlexibleRSHandler(ResultMapHandler<T> h){
            this.rmh = h;
        }
        
        protected T handleRow(ResultSet rs)throws SQLException{
                Map<String, Object> map = maph.handleRow(rs);
                return rmh.handleRowMap(map);
        }
    }
    
    
    public static <T> ListPage<T> loadPage(Connection conn, ListPage<T> page, 
        String columnClause ,String fromClause, String orderByClause,
        ResultSetHandler<List<T>> handler, Object... params)throws SQLException
    {
            Object[] fullParams = new Object[params.length + 2];
            System.arraycopy(params, 0, fullParams, 0, params.length);
            fullParams[fullParams.length - 2] = page.getOffset();
            fullParams[fullParams.length - 1] = page.getLimit() + 1;
            if(page.getOffset() == 0){
                Object[] rets = new QueryRunner().query(conn, "select count(*) "+ fromClause,
                   new ArrayHandler(),params);
              
                page.setTotal(((Number)rets[0]).intValue());
            }
            StringBuilder sb = new StringBuilder();
            sb.append("select ").append(columnClause).append(" ").append(fromClause)
                .append(orderByClause==null?"":" "+ orderByClause).append(" limit ?, ?");
            List<T> list = new QueryRunner().query(conn, sb.toString(), handler,
                fullParams);
            
            boolean hasMore = list.size() > page.getLimit();
            
            if(hasMore)
                list.remove(list.size() -1);
            page.setMore(hasMore);
            page.setListData(list);
            return page;
    }
    
    
}
