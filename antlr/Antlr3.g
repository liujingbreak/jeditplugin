grammar Antlr3;

options{
    backtrack=false;
	memoize=true;
	//k=1;
	superClass=LJBaseParser;
	output=AST;
	ASTLabelType=CommonTree;
}

@header {
package org.liujing.jedit.parser;

import java.util.logging.*;
import java.io.*;
import org.liujing.parser.*;
import org.liujing.magdown.parser.*;
import org.liujing.parser.antlr.*;
}

@members {
    static Logger log = Logger.getLogger(Antlr3Parser.class.getName());
    
    private ANTLRGrammarInfo info;
    
    public void setInfo(ANTLRGrammarInfo info){
        this.info = info;
    }
    
    private void addOtherInfo(String name, Token tk, Token endTk){
        if(info != null){
            CommonToken token = (CommonToken)tk;
            CommonToken tokenEnd = (CommonToken)endTk;
            info.addOtherInfo(name, token.getLine(), token.getStartIndex()
                , tokenEnd.getStopIndex()+ 1);
        }
    }
    private void addParserRule(String name, Token tk, Token endTk){
        if(info != null){
            CommonToken token = (CommonToken)tk;
            CommonToken tokenEnd = (CommonToken)endTk;
            info.addParserRule(name, token.getLine(), token.getStartIndex(), tokenEnd.getStopIndex()+ 1);
        }
    }
    private void addLexerRule(String name, Token tk, Token endTk, boolean fragment){
        if(info != null){
            CommonToken token = (CommonToken)tk;
            CommonToken tokenEnd = (CommonToken)endTk;
            info.addLexerRule(name, token.getLine(), token.getStartIndex(),
                tokenEnd.getStopIndex()+ 1, fragment);
        }
    }
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

parseGrammar
    :
    ({input.LT(1).getText().equals("tree")}? ID)? 'grammar' id=ID ';' 
    { addOtherInfo("grammar "+ $id.text, $parseGrammar.start, $id);}
    parseOptions? parseTokens? scopeSet* 
    (globalAction)*
    rule* EOF
;
    
parseOptions
    @after{ addOtherInfo("options", $parseOptions.start, $parseOptions.stop); }
    :
    'options'^ 
    braceBody!
    ;
parseTokens
    @after{ addOtherInfo("tokens", $parseTokens.start, $parseTokens.stop);}
    :
    'tokens'^  braceBody!
    ;
scopeSet
@after{ addOtherInfo("scope", $scopeSet.start, $scopeSet.stop);}:
    'scope'^ id=ID braceBody!
    ;
    
globalAction
    @init{
        String scope_name = "@";
        }
    @after{ 
    addOtherInfo(scope_name + $id.text, $globalAction.start,
        $globalAction.stop);
    }
	:	'@' (scopeName=actionScopeName '::' {scope_name = "@"+ $scopeName.text;})?
	    id=ID braceBody
	;
actionScopeName
	:	ID
	|	l='lexer'
    |   p='parser'
	;

//parseRulecatch
//    @after{ addOtherInfo("@rulecatch", $parseRulecatch.start, $parseRulecatch.stop);}:
//    '@' 'rulecatch'^ braceBody! 
//    
//    ;
//parseSynpredgate
//    @after{ addOtherInfo("@synpredgate", $parseSynpredgate.start, $parseSynpredgate.stop);}:
//    '@' 'synpredgate'^ braceBody!
//    
//    ;
//parseLexerHeader
//    @after{ addOtherInfo("@lexer::header", $parseLexerHeader.start, $parseLexerHeader.stop);}:
//    '@lexer' '::' 'header' braceBody!
//    
//    ;
//    
//parseLexerMembers
//    @after{ addOtherInfo("@lexer::members", $parseLexerMembers.start, $parseLexerMembers.stop);}:
//    '@lexer' '::' 'members' braceBody!
//    
//    ;
rule
    @init{
        boolean isFragment = false;
    }
    @after{
        if(Character.isUpperCase($id.text.charAt(0)))
            addLexerRule($id.text, $rule.start, $rule.stop, isFragment);
        else
            addParserRule($id.text, $rule.start, $rule.stop);
        //log.fine("---- rule "+ $id.text + " end -----");
    }
    :
    ('fragment'{isFragment = true;})? id=ID '!'? { log.fine("rule:"+ $id.text);}
    ParamList?
    ('returns' ParamList)?
    ('throws' ID ( ',' ID )*)?
    ('options' braceBody )?
    ruleScopeSpec
    ( 
         '@init' braceBody
        | '@after' braceBody
    )*
    ':'
    callChoice
    ';'
    exceptionGroup?
    ;
    
ruleScopeSpec
	:	('scope' action)? ('scope' ID (',' ID)* ';')?
	;
exceptionGroup
	:	( exceptionHandler )+ ( finallyClause )?
	|	finallyClause
    ;

exceptionHandler
    :    'catch'^ ParamList action
    ;

finallyClause
    :    'finally' action -> ^('finally')
    ;
ParamList:
    '[' (~']')* ']'
    ;
callChoice
    :
        callChoiceOption
        ('|' callChoiceOption  )*
    ;

callChoiceOption:
    embeddedOptions? ((predicates)=> predicates)? callRules? treeRewriters?
    ;

    
callRules:
    (   (ID ('='|'+='))? '~'? callRule ('?'|'*'| '+'|'^'|'!')*
        | action
        
    )+
    ;
callRule:
    callSingleRule
    | '(' callChoice ')'
    ;
    
embeddedOptions:
    'options' braceBody ':'
    ;
predicates:
    semantic_predic
    | syntactic_predic
    ;
semantic_predic:
    braceBody '?' '=>'?;

syntactic_predic:
    '(' callChoice ')' '=>';  // backtrack has to be used here against the rule "callChoice"
    
action:
    braceBody
    ;
callSingleRule:
    
    	  '[' (~']')+ ']'		//regular expresssion
        | id=ID ParamList?  (('[')=> '[' (~']')+ ']')?  
        | str=STRING_LITERAL  ('..' STRING_LITERAL)?  
        | '.'
        | EOF_KEY
    
    
    ;

treeRewriters:
        ('->' ((braceBody '?')=> braceBody '?')? treeRewriter)+
    ;
    
//treeRewriter:
//    treeRewriterBody
//    ;
treeRewriter:
    (treeRewriteElement ('?'|'*'| '+')?)+
    ;
treeRewriteElement:
    '(' treeRewriter ')'
    | ID ParamList?
    | STRING_LITERAL
    | EOF_KEY
    | action
    | '^' '(' treeRewriter ')'
    ;
braceBody:
    '{' (  ~('{'|'}')!
        | braceBody!
        )*
    '}'
;

OPTIONS_: 'options';
TOKENS_: 'tokens';
SCOPE: 'scope';
AT: '@';
INIT_PREFIX: '@init';
AFTER_PREFIX: '@after';
COLON: ':';
COMMAR: ';';
CC: '::';
LBRACE: '{';
RBRACE: '}';
LPAREN: '(';
RPAREN: ')';
BANG: '!';
SYNTACTIC_PREDICATES: '=>';
START: '*';
HOOK: '?';
OR: '|';
NOT: '~';
EQ: '=';
ADD: '+=';
LBRACKET: '[';
RBRACKET: ']';
FRAGMENT:'fragment';
EOF_KEY: 'EOF';
GRAMMAR :'grammar';
DOUBLE_DOT: '..';
DOT: '.';
ARRA: '^';
RETURNS: 'returns';
ARROW:'->';

ID: ID_START (ID_PART)*;
fragment ID_START: '$'|'_'|UNICODE_LETTER;
fragment ID_PART:
    (ID_START|'0'..'9');
fragment UNICODE_LETTER:
        'A'..'Z'
        | 'a'..'z'
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
MultiLineComment: '/*' .* '*/' ('\n'|'\r')*
    {skip();
        //log.fine("comment: "+ $text);
    };
SingleLineComment: '//' (~('\n'|'\r'))* ('\n'|'\r')* {skip(); };
ANYCHAR: .;
