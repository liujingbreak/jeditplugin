package org.liujing.parser;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import org.liujing.tools.compiler.*;
import java.util.regex.*;
import javax.swing.tree.*;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.liujing.jedit.parser.*;
import org.liujing.parser.*;
import javax.swing.tree.DefaultMutableTreeNode;
import org.liujing.tool.JSHandler;
import errorlist.*;
import sidekick.*;

public class JsParser extends SideKickParser{
	private Logger log = Logger.getLogger(JsParser.class.getName());

	static Pattern docPat = Pattern.compile("@(class|method|function|property|attribute)\\s+(\\w+)", Pattern.MULTILINE );

	public JsParser(String name){
		super(name);
		//parser = new JavascriptParser();
	}
	public SideKickParsedData parse(org.gjt.sp.jedit.Buffer buffer, DefaultErrorSource errorSource)
	{
		try{
			return start(buffer,errorSource);
		}catch(Exception e){
			log.log(Level.WARNING,"parse failed",e);
		}
		return null;
	}

	protected SideKickParsedData start(org.gjt.sp.jedit.Buffer buffer, DefaultErrorSource errorSource){
	    ANTLRStringStream in = new ANTLRStringStream(buffer.getText(0,buffer.getLength()));
        JavaScript4JeditLexer lexer = new JavaScript4JeditLexer(in);
        RemovableTokenStream tokens = new RemovableTokenStream(lexer);
        JavaScript4JeditParser p = new JavaScript4JeditParser(tokens);
        SideKickParsedData data = new SideKickParsedData(buffer.getName());
        JSHandler h = new ParsingHandler(data.root);
        p.setHandler(h);
        try{
            p.program();
        }catch(RecognitionException e){
            errorSource.addError(ErrorSource.ERROR, buffer.getPath(), e.line, ((CommonToken)e.token).getStartIndex(), ((CommonToken)e.token).getStopIndex()+1, e.getMessage());
        }
        return data;
	}

	class ParsingHandler implements JSHandler{
	    DefaultMutableTreeNode root;
	    DefaultMutableTreeNode parentNode;
        DefaultMutableTreeNode currNode;
        JsNode lastNode;
        String methodDocName;

	    public ParsingHandler(DefaultMutableTreeNode root){
	        this.root = root;
	        parentNode = root;
	    }

	    public void onFunctionStart(int line, String name, String params, int streamOffset){

	        currNode = new DefaultMutableTreeNode();
	        JsNode jsinfo = null;
	        //if(name != null && name.startsWith("_")){
	        //    jsinfo = new RedSquareNode(name, params);
	        //}else{
	        jsinfo = new MutableIconNode(name, params);
	        //}
	        functionNodeIconSet(jsinfo, name);
	        jsinfo.setStartOffset(streamOffset);
	        currNode.setUserObject(jsinfo);
	        parentNode.add(currNode);
	        parentNode = currNode;
	        //log.info( line + ": "+ name+ " start parentNode="+ parentNode);
	    }

        public void onFunctionEnd(int streamOffset){
            //log.info( name + " end parentNode="+ parentNode);
            JsNode jsinfo = (JsNode)parentNode.getUserObject();
            lastNode = jsinfo;
            jsinfo.setEndOffset(streamOffset);
            parentNode = (DefaultMutableTreeNode)parentNode.getParent();
        }

        public void onJSONStart(int line, int streamOffset){
            //log.info( line + ": "+ name+ " start parentNode="+ parentNode);
            currNode = new DefaultMutableTreeNode();
            JsNode jsinfo = new YellowSquareNode("{}", "{}");
	        jsinfo.setStartOffset(streamOffset);
	        currNode.setUserObject(jsinfo);

            parentNode.add(currNode);
            parentNode = currNode;
        }

        public void onJSONEnd(int streamOffset){
            JsNode jsinfo = (JsNode)parentNode.getUserObject();
            jsinfo.setEndOffset(streamOffset);
            DefaultMutableTreeNode pp = (DefaultMutableTreeNode)parentNode.getParent();
            if(parentNode.getChildCount() == 0){
                pp.remove(pp.getChildCount() -1);
            }
            parentNode = pp;

        }
        
        private void functionNodeIconSet(JsNode node, String name){
            if(name != null && (node instanceof MutableIconNode) && name.startsWith("_")){
                MutableIconNode mnode = (MutableIconNode)node;
                mnode.setIcon(RedSquareNode.ICON);
            }
        }

        public void onJSONProperty(String name, int line){
            lastNode.setName(name);
            functionNodeIconSet(lastNode, name);
        }

        public void onFunctionAssign(String varname, Object tree){
            lastNode.setName(varname);
            functionNodeIconSet(lastNode, varname);
        }
        
        public void onDoc(int line, int streamOffset,int streamEnd,String docContent){
            //todo parse doc content
            Matcher m = docPat.matcher(docContent);
            if(m.find()){
                if(m.group(1).equals("method"))
                    methodDocName = m.group(2);
                else{
                    currNode = new DefaultMutableTreeNode();
                    JsNode jsinfo = new BlueSquareNode("@"+ m.group(1).substring(0,1)+ ":" + m.group(2), m.group(1));
                    jsinfo.setStartOffset(streamOffset);
                    currNode.setUserObject(jsinfo);
                    parentNode.add(currNode);
                    jsinfo.setEndOffset(streamEnd);
                }
            }
        }
    }

}
