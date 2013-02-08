grammar MyCSS;

options
{
	backtrack=false;
	memoize=true;
	//k=1;
	superClass=LJBaseParser;
	output=AST;
}
@header {
package org.liujing.jedit.parser;

import java.util.logging.*;
import java.io.*;
import org.liujing.parser.*;
import org.liujing.ironsword.grammar.AntlrGrammarHandler;
}
@members {
    static Logger log = Logger.getLogger(MyCSSParser.class.getName());
    
    private AntlrGrammarHandler agh = null;
    
    public void setHandler(AntlrGrammarHandler h){
        agh = h;
    }
    public void addDocNode(Token start){
        if(agh == null)
            return;
        int docTokenIdx = start.getTokenIndex() -1;
        if(docTokenIdx >= 0){
            CommonToken doct = (CommonToken) ((CommonTokenStream)input).get( start.getTokenIndex() -1);
            if(doct.getChannel() == Token.HIDDEN_CHANNEL)
                agh.addNode("doc", doct.getText(), doct, doct);
        }
    }
}
@rulecatch {
    catch (RecognitionException e) {
        reportError(e);
        throw e;
    }
}
@lexer::header {
package org.liujing.jedit.parser;
}
cssfile[String fileName]
    @init{
        if(agh != null) {
            agh.onRuleStart("css",$start);
            agh.setName($fileName);
        }
    }
    @after{
        if(agh != null) agh.onRuleStop($stop);
    }:
    cssUnit*
    ;
cssUnit
    @init{
        addDocNode($start);
        if(agh != null) agh.onRuleStart("unit",$start);
    }
    @after{
        if(agh != null) agh.onRuleStop($stop);
    }
    :
    selector '{' (~'}')* '}'
    ;
selector
    @after{ if(agh != null) agh.setName(ruleText($start, $stop)); }:
    (~'{')+
    ;
STRING_LITERAL: (
    '"' DOUBLE_STRING_CHARACTERS '"'
    | '\'' SINGLE_STRING_CHARACTERS '\'');
fragment DOUBLE_STRING_CHARACTERS: (~('"'|'\\'|'\n'|'\r'|'\u2028'|'\u2029')
        | '\\' ~('\n'|'\r'|'\u2028'|'\u2029'))*;
fragment SINGLE_STRING_CHARACTERS:(~('\''|'\\'|'\n'|'\r'|'\u2028'|'\u2029')
        | '\\' ~('\n'|'\r'|'\u2028'|'\u2029'))*;
WHITE_SPACE // Tab, vertical tab, form feed, space, non-breaking space and any other unicode "space separator".
	: ('\r'|'\n'|'\t' | '\u000b' | '' |'f'| ' ' | '\u00a0'|USP)	{skip();}
	;
fragment USP: '\u2000'..'\u200b' | '\u3000';
LBRACE: '{';
RBRACE: '}';
MultiLineComment
    @init{
            boolean isJavaDoc = false;
        }: '/*' 
    {
         if((char)input.LA(1) == '*'){
             isJavaDoc = true;
         }
    }
    .* '*/' ('\n'|'\r')* 
    {
        if(isJavaDoc){
            $channel=HIDDEN;
        }else{
            skip();
        }
    };
ANYCHAR: .;
