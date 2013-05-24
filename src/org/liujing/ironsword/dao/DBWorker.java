package org.liujing.ironsword.dao;

import org.apache.commons.dbutils.*;
import java.util.*;
import java.sql.*;
import javax.sql.DataSource;
import java.io.*;
import java.util.logging.*;
import org.antlr.runtime.*;
import org.liujing.ironsword.*;
import org.liujing.parser.*;
import javax.naming.*;

/**
   JDBC Transaction management
*/
public class DBWorker{
  private static Logger log = Logger.getLogger(DBWorker.class.getName());
  private static boolean db_inited =false;
  private static DataSource dataSource;
  private static ThreadLocal<Connection> threadConn = new ThreadLocal();
  static{
    try{
        //Class.forName("org.h2.Driver");
        Context ctx = new InitialContext();
        dataSource = (DataSource)ctx.lookup("java:comp/env/jdbc/irondb");
        
    
    }catch(Exception e){
      log.log(Level.SEVERE, "Failed to load driver", e);
    }
  }
  
  //protected Connection conn;
  protected int commandNestedLevel = 0;
  protected String dbUrl;
  
  private DBWorker(){
      this(System.getProperty("dbpath"));
      log.info("datasource:"+ dataSource);
  }
  
  private DBWorker(String dbname){
      if(dbname == null){
          dbname="data/unknownNameDB";
          log.warning("please give a system property '-Ddbpath=xxxx' for db name");
      }
    initConnection(dbname);
    if(!db_inited){
      initdatabase();
      db_inited = true;
    }
  }
  
  public <T> T execute(SQLCommand<T> cmd){
      
      Connection conn = null;
    try{
        conn = threadConn.get();
        if(conn == null){
            conn = dataSource.getConnection();
            threadConn.set(conn);
        }
      commandNestedLevel++;
      long time = System.nanoTime();
      log.fine("connection gotten "+ conn.hashCode());
      //if(conn == null){
      //    renewConnection();
      //    if(conn == null){
      //        throw new IronException("DB connection encounters previous problem");
      //    }
      //}
      T result = cmd.run(conn);
      log.fine("time used: "+ (System.nanoTime() - time));
      if(commandNestedLevel == 1)
          conn.commit();
      return result;
    }catch(SQLException e){
        log.log(Level.SEVERE, "", e);
        try{
            DbUtils.rollback(conn);
            log.info("rollback DB");
        }catch(Exception de){
            log.log(Level.SEVERE, "failed to rollback DB connection", de);
        }
        conn = null;
        throw new IronException("", e);
    }catch(Exception e){
        log.log(Level.SEVERE, "", e);
        try{
            DbUtils.rollback(conn);
            log.info("rollback DB");
        }catch(Exception de){
            log.log(Level.SEVERE, "failed to rollback DB connection", de);
        }
        throw new IronException("", e);
    }finally{
        if(commandNestedLevel == 1)
        DbUtils.closeQuietly(conn);
        threadConn.set(null);
        
        commandNestedLevel--;
    }
  }
  
  private void initConnection(String dbname){
    //try{
    //    this.dbUrl = "jdbc:h2:"+ dbname;
    //    log.fine(dbUrl);
    //    conn = DriverManager.getConnection(dbUrl, "sa", "");
    //    conn.setAutoCommit(false);
    //}catch(SQLException dbe){
    //    try{
    //        renewConnection();
    //    }catch(IronException ie){
    //        log.warning("reconnect failed");
    //    }
    //}catch(Exception e){
    //  log.log(Level.SEVERE, "", e);
    //}
  }
  
  /* public void renewConnection(){
      SQLException lastDbe = null;
      for(int i =0; i<4; i++){
          log.info("retry connecting");
          try{
              Thread.sleep(1000);
          }catch(InterruptedException ie){
              
          }
          try{
              conn = DriverManager.getConnection(dbUrl, "sa", "");
              conn.setAutoCommit(false);
              return;
          }catch(SQLException e){
              //log.log(Level.SEVERE, "failed to renew DB connection");
              lastDbe = e;
          }
      }
      throw new IronException("DB Connection", lastDbe);
  } */
  
  protected void initdatabase(){
    try{
      InputStream in = DBWorker.class.getResourceAsStream("/ironsword_create_tables.sql");
      Reader r = new InputStreamReader(in, "utf-8");
      SQLScriptLexer lex = new SQLScriptLexer(new ANTLRReaderStream(r));
      SQLScriptParser par = new SQLScriptParser(new RemovableTokenStream(lex));
      par.setSQLHandler(new InitSQLHandler());
      par.script();
      r.close();
    }catch(Exception ex){
      log.log(Level.SEVERE, "", ex);
    }
  }
  
  protected void dropTables(){
      try{
          InputStream in = DBWorker.class.getResourceAsStream("/ironsword_drop_tables.sql");
          Reader r = new InputStreamReader(in, "utf-8");
          SQLScriptLexer lex = new SQLScriptLexer(new ANTLRReaderStream(r));
          SQLScriptParser par = new SQLScriptParser(new RemovableTokenStream(lex));
          par.setSQLHandler(new InitSQLHandler());
          par.script();
          r.close();
      }catch(Exception ex){
          log.log(Level.SEVERE, "", ex);
      }
  }
  
  public static class UpdateSQLCommand implements SQLCommand
  {
    private String sql;
    public UpdateSQLCommand(){}
    /** set sql
     @param sql sql
    */
    public void setSql(String sql){
        this.sql = sql;
    }
    
    public Object run(Connection con)throws Exception{
      QueryRunner runner = new QueryRunner();
      log.fine(sql);
      int n = runner.update(con, sql);
      log.fine(n + " succeed");
      return null;
    }
  }
  
  @Override
  protected void finalize(){
      //close();// it seems not be invoked when jvm shutdown
  }
  
  public void close(){
      //log.fine("close DB");
      //DbUtils.closeQuietly(conn);
  }
  
  public static void closeAll(){
      //getInstance().dropTables();
      getInstance().close();
  }
  
  public static DBWorker getInstance(){
    return SingletonWorker.WORKER;
  }
  
  private static class SingletonWorker{
    public static final DBWorker WORKER = new DBWorker();
  }
  
  public static interface SQLHandler{
    public void onSQL(String sql);
  }
  
  public class InitSQLHandler implements SQLHandler{
    UpdateSQLCommand cmd = new UpdateSQLCommand();
    public void onSQL(String sql){
      //log.fine(":: "+sql);
      cmd.setSql(sql);
      execute(cmd);
    }
  }
}
