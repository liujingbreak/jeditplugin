package org.liujing.ironsword.cmd;

import java.util.*;
import org.liujing.ironsword.grammar.GrammarNode;

public class ConsoleCommandModel{
    /** command names */
    private LinkedList<String> keywords = new LinkedList();
    
    private Iterator<String> keywordItr;
    private Map<String, String> options = EMPTY_MAP;
    private static Map<String, String> EMPTY_MAP = new HashMap();
    
    public ConsoleCommandModel(){
    
    }
    
    public ConsoleCommandModel(GrammarNode cmdNode){
        boolean nextIsOptValue = false;
        String lastOptName = null;
        if(cmdNode.getType().equals("cmd")){
            for(GrammarNode n : cmdNode.getChildren()){
                if(n.getType().equals("keyword")){
                    keywords.add(n.getName());
                    if(nextIsOptValue){
                        nextIsOptValue = false;
                        options.put(lastOptName, n.getName());
                    }
                }else if(n.getType().equals("option")){
                    String value = "";
                    for(GrammarNode optchr : n.getChildren()){
                        if(optchr.getType().equals("value"))
                            value = optchr.getName();
                    }
                    String optName = n.getName().substring(1);
                    if(value.length()== 0){
                        nextIsOptValue = true;
                        lastOptName = optName;
                    }else{
                        nextIsOptValue = false;
                    }
                    addOption(optName, value);
                }
            }
        }
    }
    
    public void addKeyword(String k){
        keywords.add(k);
    }
    
    public Iterator<String> itr(){
        if(keywordItr == null)
            keywordItr = keywords.iterator();
        return keywordItr;
    }
    
    public void addOption(String name, String value){
        if(options == EMPTY_MAP)
            options = new HashMap();
        options.put(name, value);
    }
    
    public void addOption(String name){
        if(options == EMPTY_MAP)
            options = new HashMap();
        options.put(name, "");
    }
    
    /** get options
     @return options
    */
    public Map<String, String> getOptions(){
        return options;
    }
    
    public String getOption(String name){
        return options.get(name);
    }
    /** get keywords
     @return keywords
    */
    public LinkedList<String> getKeywords(){
        return keywords;
    }

    /** set keywords
     @param keywords keywords
    */
    public void setKeywords(LinkedList<String> keywords){
        this.keywords = keywords;
    }


}
