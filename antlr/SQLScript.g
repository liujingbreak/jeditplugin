grammar SQLScript;

options{
    backtrack=false;
	memoize=true;
	//k=1;
	superClass=LJBaseParser;
}
@header {
    package org.liujing.ironsword;
    import org.liujing.ironsword.grammar.*;
    import java.util.logging.*;
    import java.io.*;
    import org.liujing.parser.*;
    import org.liujing.ironsword.dao.DBWorker;
}
@members{
    private DBWorker.SQLHandler handler;
    public void setSQLHandler(DBWorker.SQLHandler h){
        handler = h;
    }
}
@lexer::header{
    package org.liujing.ironsword;
    import org.liujing.parser.LJLexerHelper;
    import java.util.logging.*;
    import java.util.*;
    

}
@lexer::members {
    @Override
    public String getErrorMessage(RecognitionException e, String[] tokenNames) {
        String classname = this.getClass().getName();
        StringBuilder buf = new StringBuilder();
        for(StackTraceElement ee : e.getStackTrace()){
            if(ee.getClassName().equals(classname)){
                buf.append(ee.getMethodName());
                buf.append(",");
            }
        }
        buf.append(" | ");
        buf.append(super.getErrorMessage(e, tokenNames));
        return buf.toString();
    }
    
    private LJLexerHelper lexerHelper = new LJLexerHelper();
            
    @Override
    public Token nextToken(){
        Token t = lexerHelper.nextToken(this,  state);
        return t;
    }
    public void emitMore(int type, String text){
        CommonToken t = new CommonToken(type, text);
        t.setLine(input.getLine());
        t.setCharPositionInLine(input.getCharPositionInLine());
        t.setStartIndex(input.index());
        t.setStopIndex(input.index());
        lexerHelper.emitMore(t);
    }
}
@rulecatch {
    catch (RecognitionException e) {
        reportError(e);
    }
}

script: ( command ';' )*;
command
    @after{
        String sql = ruleText($start, $stop);
        if(sql.length() > 0)
            handler.onSQL(sql);
    }
    : (~ COMMA)* ;

COMMA: ';';
SP: (' ' 
    |'\t' 
    | '\n' ){skip();};
Comment: '--' ({input.LA(1)!='\n'}?=> ~'\n' )* '\n'? {skip();} ;
ENTER: '\r' {skip();};
StringLit: '\'' (~'\''|Escapeseq)* '\'';
fragment Escapeseq: '\'\'';
ID: ('a'..'z'|'A'..'Z'|'0'..'9')+;
ANYCHAR: .;
//OTHER: (~(';'|' '|'\n'|'\r'|'\''))+;

