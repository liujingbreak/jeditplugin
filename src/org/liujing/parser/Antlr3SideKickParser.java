package org.liujing.parser;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import errorlist.*;
import sidekick.*;
import org.liujing.jedit.parser.*;
import org.antlr.runtime.*;
import org.liujing.parser.antlr.*;
import javax.swing.tree.*;

public class Antlr3SideKickParser extends SideKickParser{
    private Logger log = Logger.getLogger(JsParser.class.getName());
    
    
    public Antlr3SideKickParser(String name){
		super(name);
		
		//parser = new JavascriptParser();
	}
	
	public SideKickParsedData parse(org.gjt.sp.jedit.Buffer buffer, DefaultErrorSource errorSource)
	{
	    SideKickParsedData data = null;
		try{
		    data = new SideKickParsedData(buffer.getName());
		    
			ANTLRStringStream in = new ANTLRStringStream(buffer.getText(0,buffer.getLength()));
			Antlr3Lexer lexer = new Antlr3Lexer(in);
            RemovableTokenStream tokens = new RemovableTokenStream(lexer);
            Antlr3Parser p = new Antlr3Parser(tokens);
            p.setInfo(new ParseHandler(data));
            p.parseGrammar();
        }catch(RecognitionException e){
            errorSource.addError(ErrorSource.ERROR, buffer.getPath(),
                e.line,
                ((CommonToken)e.token).getStartIndex(),
                ((CommonToken)e.token).getStopIndex()+1, e.getMessage());
        
		}catch(Exception e){
			log.log(Level.WARNING,"parse failed",e);
		}
		return data;
	}
	
	private class ParseHandler  extends ANTLRGrammarInfo{
	    DefaultMutableTreeNode parentNode;
        DefaultMutableTreeNode currNode;
        SideKickParsedData data;
	    public ParseHandler(SideKickParsedData data){
	        this.data = data;
	    }
	    
	    public void addParserRule(String name, int line, int offset, int endOffset){
	        
	        currNode = new DefaultMutableTreeNode();
	        JsNode node = new JsNode(name, name);
	        node.setStartOffset(offset);
	        node.setEndOffset(endOffset);
	        currNode.setUserObject(node);
            data.root.add(currNode);
        }
        
        public void addLexerRule(String name, int line, int offset, int endOffset,
            boolean fragment)
        {
            currNode = new DefaultMutableTreeNode();
	        JsNode node = new JsNode(name, name);
	        if(fragment){
	            node = new YellowSquareNode(name, name);
	        }else{
	            node = new BlueSquareNode(name, name);
	        }
	        node.setStartOffset(offset);
	        node.setEndOffset(endOffset);
	        currNode.setUserObject(node);
            data.root.add(currNode);
        }
        
        public void addOtherInfo(String name, int line, int offset, int endOffset){
            if(name.startsWith("grammar ")){
                data.root.setUserObject(name);
                return;
            }
                
            currNode = new DefaultMutableTreeNode();
	        JsNode node = new JsNode(name, name);
	        node.setStartOffset(offset);
	        node.setEndOffset(endOffset);
	        currNode.setUserObject(node);
            data.root.add(currNode);
        }
	}
}
