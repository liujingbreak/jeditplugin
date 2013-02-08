package org.liujing.ironsword.dao;

import java.util.*;

public interface ResultMapHandler<T>{
    public T handleRowMap(Map<String, Object> row);
    
}
