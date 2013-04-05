package org.liujing.parser;

import java.util.*;
import java.util.regex.*;
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
import org.liujing.ironsword.grammar.*;

public class CSSParser extends SideKickParser{
    private Logger log = Logger.getLogger(CSSParser.class.getName());

	static Pattern docKeywordPat = Pattern.compile("[a-zA-Z0-9_$'\\.\"]+(?: [a-zA-Z0-9_$'\\.\"]+)*", Pattern.MULTILINE );

	public CSSParser(String name){
		super(name);
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
        MyCSSLexer lexer = new MyCSSLexer(in);
        RemovableTokenStream tokens = new RemovableTokenStream(lexer);
        MyCSSParser p = new MyCSSParser(tokens);
        SideKickParsedData data = new SideKickParsedData(buffer.getName());
        AntlrGrammarHandler h = new AntlrGrammarHandler();
        p.setHandler(h);
        try{
            p.cssfile(buffer.getName());
            parseNodeToSidekick(data.root, h);
        }catch(RecognitionException e){
            errorSource.addError(ErrorSource.ERROR, buffer.getPath(), e.line, ((CommonToken)e.token).getStartIndex(), ((CommonToken)e.token).getStopIndex()+1, e.getMessage());
        }
        return data;
	}
    
	private void parseNodeToSidekick(DefaultMutableTreeNode uiNode, AntlrGrammarHandler h){
	    GrammarNode gnode = h.getNode();
	    parseNodeToSidekick(uiNode, gnode);
	}
	
	private void parseNodeToSidekick(DefaultMutableTreeNode uiNode, GrammarNode gnode){
	    for(GrammarNode cg : gnode.getChildren()){
	        if(cg.isType("doc")){
	            createUINode_doc(uiNode, cg);
	        }else if(cg.isType("unit")){
	            createUINode_green(uiNode, cg);
	        }else if(cg.isType("rule")){
	            createUINode_rule(uiNode, cg);
	        }
	    }
	}
	
	private void createUINode_doc(DefaultMutableTreeNode parent, 
	    GrammarNode node)
	{
	    DefaultMutableTreeNode uiNode = new DefaultMutableTreeNode();
	    Matcher m = docKeywordPat.matcher(node.getName());
	    if(m.find()){
	        YellowSquareNode icon = new YellowSquareNode(m.group(), node.getName());
	        icon.setStartOffset(node.getStartOffset());
	        icon.setEndOffset(node.getEndOffset());
	        uiNode.setUserObject(icon);
	        parent.add( uiNode);
	    }
	}
	
	private void createUINode_green(DefaultMutableTreeNode parent,
	    GrammarNode node)
	{
	    DefaultMutableTreeNode uiNode = new DefaultMutableTreeNode();
	    JsNode icon = new JsNode(node.getName(), "");
	    icon.setStartOffset(node.getStartOffset());
	    icon.setEndOffset(node.getEndOffset());
	    uiNode.setUserObject(icon);
	    parent.add(uiNode);
	}
	
	private void createUINode_rule(DefaultMutableTreeNode parent,
	    GrammarNode node)
	{
	    DefaultMutableTreeNode uiNode = new DefaultMutableTreeNode();
	    JsNode icon = new BlueSquareNode(node.getName(), "");
	    icon.setStartOffset(node.getStartOffset());
	    icon.setEndOffset(node.getEndOffset());
	    uiNode.setUserObject(icon);
	    parent.add(uiNode);
	    parseNodeToSidekick(uiNode, node);
	}
}
