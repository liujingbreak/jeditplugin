package org.liujing.parser;

/**
    Use this class to create new tree info of parsed data.
*/
public interface BaseParseHandler{
    /**  addNewChild
     @return the new node;
    */
    public Object ruleStart(String name, int type, int line, int offset);
    
    public Object ruleEnd(String name, int type);
    
    /**  addChildTo
     @param parentNode the parent node to which the new node should be added 
     @param name name
     @param line line
     @param offset offset
     @return the new node
    */
    public Object addChildTo(Object parentNode, String name, int type, 
        int line, int offset);
    
    
}
