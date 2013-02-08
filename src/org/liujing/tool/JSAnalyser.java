package org.liujing.tool;

import java.util.*;
import java.io.*;
import java.util.regex.*;
import java.util.logging.*;
import org.liujing.parser.*;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.liujing.jedit.parser.*;
import liujing.util.dirscan.*;

/**
 JSAnalyser
 @author Break(Jing) Liu
*/
public class JSAnalyser implements ScanHandler2{
    /** log */
    private static Logger log = Logger.getLogger(JSAnalyser.class.getName());
    private static StringBuilder sb = new StringBuilder();
    private String encode;

    public JSAnalyser(){
    }

    public static void main(String[] args)throws Exception
    {
        if(args.length == 0)
            System.out.println("usage:\n\tJSanalyser <dir1/**/*.java,dir2/**/*,...> [root dir] [char encoding]");
        String[] includes = args[0].split(",");
        List<String> includeList = new ArrayList();
        Collections.addAll(includeList, includes);
        DirectoryScan2 scanner = new DirectoryScan2(includeList, false);
        String rootPath = ".";
        JSAnalyser an = new JSAnalyser();
        if(args.length > 1)
            rootPath = args[1];
        if(args.length > 2 )
            an.encode = args[2];
        scanner.scan(new File(rootPath), an);

    }

    public void processFile(File f, String relativePath)
    {
        try{
            System.out.println("======================================");
            System.out.println("========= "+ f.getPath() +" ==========");
            log.info("start parsing " + f.getPath());
            ANTLRInputStream in = null;
            if(encode != null)
                in = new ANTLRInputStream(new FileInputStream(f), encode);
            else
                in = new ANTLRInputStream(new FileInputStream(f));
            JavaScript4JeditLexer lexer = new JavaScript4JeditLexer(in);
            RemovableTokenStream tokens = new RemovableTokenStream(lexer);
            JavaScript4JeditParser p = new JavaScript4JeditParser(tokens);
            JSHandlerImpl h = new JSHandlerImpl();
            p.setHandler(h);
            p.program();
            traverseJSNode(h.root, 0);
        }catch(Exception e){
            log.log(Level.SEVERE, "error encountered in parsing Javascript file: "
              + f.getPath(), e);
            //System.out.println("!!!!!!!!!!! "+ f.getPath() +" !!!!!!!!!!!");
            //e.printStackTrace();
        }
    }

    private void traverseJSNode(JSNode root, int level){

        if("json".equals(root.type) && root.getChildren() != null){
            System.out.print(space(level));
            System.out.println("{ :"+ root.getLine());
        }else if("function".equals(root.type)){
            System.out.print(space(level));
            System.out.println(root.getName() + root.getDesc()+ "  :"+ root.getLine());
        }else if("@".equals(root.type)){
            System.out.print(space(level));
             System.out.println(root.getName() +" "+ root.getDesc()+ "  :"+ root.getLine());
        }
        if(root.getChildren() != null){
            //log.info("child num "+ root.getChildren().size());
            for(JSNode child: root.getChildren()){
                traverseJSNode(child, level + 1);
            }
        }
        if("json".equals(root.type) && root.getChildren() != null){
            System.out.print(space(level));
            System.out.println("}");
        }
    }

    private String space(int num){
            sb.setLength(0);
            for(int i =0; i<num ;i ++)
                sb.append("\t");
            return sb.toString();
    }

    public static class JSNode{
        public String type;
        public CommonTree antlrTree;
        public List<JSNode> children;
        public JSNode parent;
        public String name;
        private String desc;
        public int line = -1;

        public JSNode(String type){
            this.type = type;
        }

        public void setTree(Object antlrTree){
            this.antlrTree = (CommonTree)antlrTree;
        }

        /** get children
         @return children
        */
        public List<JSNode> getChildren(){
            return children;
        }

        /** set children
         @param children children
        */
        public void setChildren(List<JSNode> children){
            this.children = children;
        }

        public void addChild(JSNode c){
            if(children == null)
                children = new ArrayList();
            children.add(c);
            c.parent = this;
        }
        /** get parent
         @return parent
        */
        public JSNode getParent(){
            return parent;
        }

        /** set parent
         @param parent parent
        */
        public void setParent(JSNode parent){
            this.parent = parent;
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

        /** get line
         @return line
        */
        public int getLine(){
            return line;
        }

        /** set line
         @param line line
        */
        public void setLine(int line){
            this.line = line;
        }


    }



    public static class JSHandlerImpl implements JSHandler{
        int level = 0;
        public JSNode root;
        JSNode parentNode;
        JSNode currNode;
        JSNode lastNode;
        String methodDocName;
        static Pattern docPat = Pattern.compile("@(class|method|function|property|attribute)\\s+(\\w+)", Pattern.MULTILINE );

        public JSHandlerImpl(){
            parentNode = new JSNode("program");
            root = parentNode;
        }



        public void onFunctionStart(int line, String name, String params, int streamOffset){
            level++;
            currNode = new JSNode("function");
            currNode.name = name;
            currNode.line = line;
            currNode.setDesc(" ("+ params + ")");
            parentNode.addChild(currNode);
            parentNode = currNode;
            //System.out.println(space(level)+ " function " + name +" line: "+ line);
        }

        public void onFunctionEnd(int streamOffset){
            level--;
            lastNode = parentNode;
            parentNode = parentNode.getParent();
        }

        public void onJSONStart(int line, int streamOffset){
            //System.out.println(space(level)+ "{");
            currNode = new JSNode("json");
            currNode.setLine(line);
            parentNode.addChild(currNode);
            parentNode = currNode;
            level++;
        }

        public void onJSONEnd(int streamOffset){
            level--;
            parentNode = parentNode.getParent();
            //System.out.println(space(level)+ "}");
        }

        public void onJSONProperty(String name, int line){
            lastNode.setName(name);
            lastNode.setLine(line);
            //System.out.println(space(level) + name);
        }

        public void onFunctionAssign(String varname, Object tree){
            //System.out.println("assign function " + varname +" = "+ tree);
            lastNode.setName(varname);
        }
        
        public void onDoc(int line, int streamOffset,int streamEnd,String docContent){
            //todo parse doc content
            Matcher m = docPat.matcher(docContent);
            if(m.find()){
                if(m.group(1).equals("method"))
                    methodDocName = m.group(2);
                else{
                    currNode = new JSNode("@");
                    currNode.setName("@"+ m.group(1));
                    currNode.setDesc(m.group(2));
                    currNode.setLine(line);
                    parentNode.addChild(currNode);
                }
            }
        }
        
    }
}

