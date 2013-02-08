package org.liujing.ironsword.dao;

import java.util.*;
import java.sql.*;


public interface SQLCommand<T>{
  
  public T run(Connection conn)throws Exception;
}
