grammar CommandLine;

options{
    backtrack=false;
	memoize=true;
	//k=1;
	superClass=LJBaseParser;
	//output=AST;
	//ASTLabelType=CommonTree;
}

@header {
    package org.liujing.ironsword;
    import org.liujing.ironsword.grammar.*;
    //import org.liujing.ironsword.cmd.ConsoleCommandModel;
    import java.util.logging.*;
    import java.io.*;
    import org.liujing.parser.*;
}
@members{

    private AntlrGrammarHandler handler;
    
    public void setHandler(AntlrGrammarHandler h){
        handler = h;
    }
}
@lexer::header{
    package org.liujing.ironsword;
    import org.liujing.parser.LJLexerHelper;
    import java.util.logging.*;
    import java.util.*;
    

}
@rulecatch {
    catch (RecognitionException e) {
        reportError(e);
    }
}

input
    @init{
        handler.onRuleStart("cmd");
    }
    @after{ handler.onRuleStop($start, $stop);}
    :
    (   lineItem* { handler.setName("cmd");}
        | name=WORD  { handler.setName("prop"); handler.addNode("name", $name.text, $start, $start);} '=' 
        (   value=WORD { handler.addNode("value", $value.text, $start, $start); }
            | value=Strlit { handler.addNode("value", $value.text.substring(1, $value.text.length()-1 ), $start, $start); }
            )
    )
    EOF
    ;
    
lineItem
    :
        (   id=WORD {   handler.addNode("keyword", $id.text, $id, $id); }
            | str=Strlit 
                {   String value = $str.text.substring(1, $str.text.length()-1 );
                    handler.addNode("keyword", value, $str, $str);
                }
            | option
        )
    ;
option
    @init{ handler.onRuleStart("option"); }
    @after{ handler.onRuleStop($start, $stop); }
    :
    optname = OptionName {handler.setName($optname.text);}
    (   '='
        (   value = WORD  {handler.addNode("value", $value.text, $value, $value);}
          | strValue = Strlit 
            {
                String str = $strValue.text.substring(1, $strValue.text.length()-1 );
                handler.addNode("value", str, $strValue, $strValue);
            }
        )
    )?
    ;
    
    
WORD: WORD_START (WORD_START|'-')*;
fragment WORD_START:
    (~(' '|'\t'| '\r' | '\n' | '\'' | '"'|'='|'-'));
OptionName: '-' (WORD_START|'-')+;
Strlit: '\'' (~ '\'')* '\''
    | '"' (~ '"')* '"';
Space: (' '|'\t'| '\r' | '\n' )
    { skip(); }
;

