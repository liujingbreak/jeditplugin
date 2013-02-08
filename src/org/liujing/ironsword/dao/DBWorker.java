package org.liujing.ironsword.dao;

import org.apache.commons.dbutils.*;
import java.util.*;
import java.sql.*;
import java.io.*;
import java.util.logging.*;
import org.antlr.runtime.*;
import org.liujing.ironsword.*;
import org.liujing.parser.*;

public class DBWorker{
  private static Logger log = Logger.getLogger(DBWorker.class.getName());
  private static boolean db_inited =false;
  static{
    try{
    Class.forName("org.h2.Driver");
    }catch(Exception e){
      log.log(Level.SEVERE, "Failed to load driver", e);
    }
  }
  
  protected Connection conn;
  protected int commandNestedLevel = 0;
  
  private DBWorker(){
      this(System.getProperty("dbpath"));
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
      commandNestedLevel++;
    try{
      long time = System.nanoTime();
      T result = cmd.run(conn);
      log.fine("time used: "+ (System.nanoTime() - time));
      if(commandNestedLevel == 1)
          conn.commit();
      return result;
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
        commandNestedLevel--;
    }
  }
  
  private void initConnection(String dbname){
    try{
        String url = "jdbc:h2:"+ dbname;
        log.fine(url);
        conn = DriverManager.getConnection(url, "sa", "");
        conn.setAutoCommit(false);
    }catch(Exception e){
      log.log(Level.SEVERE, "", e);
    }
  }
  
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
      close();// it seems not be invoked when jvm shutdown
  }
  
  public void close(){
      log.fine("close DB");
      DbUtils.closeQuietly(conn);
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
