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
import org.liujing.ironsword.grammar.*;
import sidekick.util.*;

public class PythonSideKickParser extends SideKickParser{
    private Logger log = Logger.getLogger(PythonSideKickParser.class.getName());
    
    public PythonSideKickParser(String name){
        super(name);
    }
    
    public SideKickParsedData parse(org.gjt.sp.jedit.Buffer buffer, DefaultErrorSource errorSource)
	{
	    SideKickParsedData data = null;
		try{
		    data = new SideKickParsedData(buffer.getName());
		    
			ANTLRStringStream in = new ANTLRStringStream(buffer.getText(0,buffer.getLength()));
			PythonLexer lexer = new PythonLexer(in);
            RemovableTokenStream tokens = new RemovableTokenStream(lexer);
            PythonParser parser = new PythonParser(tokens);
            AntlrGrammarHandler h = new AntlrGrammarHandler();
            parser.setHandler(h);
            parser.file_input();
            
            GrammarNode gnode = h.currentNode();
            for(GrammarNode chr: gnode.getChildren()){
                data.root.add(sidekickNode(chr));
            }
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
	
	private DefaultMutableTreeNode sidekickNode(GrammarNode gnode){
	     DefaultMutableTreeNode currNode = new DefaultMutableTreeNode();
	     JsNode node = null;
	     if(gnode.getType().equals("func"))
	         node = new JsNode(gnode.getName(), gnode.getName());
	     else if(gnode.getType().equals("class"))
	         node = new YellowSquareNode(gnode.getName(), gnode.getName());
	     else if(gnode.getType().equals("extends"))
	         node = new BlueSquareNode("->" + gnode.getName(), gnode.getName());
	     else
	         return null;
         node.setStartOffset(gnode.getStartOffset());
         node.setEndOffset(gnode.getEndOffset());
         
	     currNode.setUserObject(node);
	     
	     for(GrammarNode chr: gnode.getChildren()){
	         DefaultMutableTreeNode tnode = sidekickNode(chr);
	         if(tnode != null)
	             currNode.add(tnode);
	     }
         return currNode;
	}
}
