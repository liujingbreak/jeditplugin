grammar MyPython;
options{
    backtrack=false;
	memoize=true;
	//k=1;
	superClass=LJBaseParser;
	//output=AST;
}
tokens{
    INDENT;
    DEDENT;
}
@header {
package org.liujing.jedit.parser;

import java.util.logging.*;
import org.liujing.parser.*;
import org.liujing.magdown.parser.*;
import org.liujing.parser.antlr.*;
}
@members {
    static Logger log = Logger.getLogger(MyPythonParser.class.getName());
    
}
@rulecatch {
    catch (RecognitionException e) {
        reportError(e);
    }
}
@lexer::header{
package org.liujing.jedit.parser;
import java.util.logging.*;
}

@lexer::members {
    static Logger log = Logger.getLogger(Antlr3Lexer.class.getName());
    
    @Override
    public void reportError(RecognitionException e) {
        if(e instanceof NoViableAltException){
            String hdr = getErrorHeader(e);
            String msg = getErrorMessage(e, getTokenNames());
            log.fine(hdr+" "+msg);
        }else{
            super.reportError(e);
        }
    }

    @Override
    public void emitErrorMessage(String msg) {
		log.fine(msg);
		}
}
