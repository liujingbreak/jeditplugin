grammar Python;
options
{
	backtrack=false;
	memoize=true;
	//k=*;
	superClass=LJBaseParser;
	//output=AST;
}
tokens {
    INDENT;
    DEDENT;
}

@header {
package org.liujing.jedit.parser;

import java.util.logging.*;
import java.io.*;
import org.liujing.parser.*;
import org.liujing.ironsword.grammar.*;
}
@members{
    AntlrGrammarHandler handler;
    
    public void setHandler(AntlrGrammarHandler h){
        handler = h;
    }
}
@lexer::header{
package org.liujing.jedit.parser;
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


    /** Handles context-sensitive lexing of implicit line joining such as
     *  the case where newline is ignored in cases like this:
     *  a = [3,
     *       4]
     */
    private int indentLevel = 0;
    
    private boolean setIndentEnabled(boolean enable){
        
        if(enable)
            indentLevel++;
        else
            indentLevel--;
        
        if(enable && indentLevel == 1 || (!enable && indentLevel == 0)){
        //PythonLexer lex = (PythonLexer)input.getTokenSource();
            setIndentDetect(enable);
        }
        //System.out.println(" indentLevel = "+ indentLevel + " " +enable + " line "+ input.getLine() + " noIndentCount="+ noIndentCount);
        return true;
    }
    private LinkedList<Integer> indents = new LinkedList();
    private boolean lineStart = true;
    private boolean noIndentCount = false;
    private LJLexerHelper lexerHelper = new LJLexerHelper();
            
    @Override
    public Token nextToken(){
        Token t = lexerHelper.nextToken(this,  state);
        lineStart = false;
        //lineStart = t.getType() == NEWLINE;
        return t;
    }
    public void emitMore(int type, String text){
                CommonToken t = new CommonToken(type, text);
                t.setLine(input.getLine());
                t.setCharPositionInLine(input.getCharPositionInLine());
                t.setStartIndex(input.index());
                t.setStopIndex(input.index());
                emitMore(t);
            }
            
    public void emitMore(Token... tokens){
        lexerHelper.emitMore(tokens);
    }
    
    public void emitMore(List<Token> tokens){
        lexerHelper.emitMore(tokens);
    }
    
    protected int calculateIndentChars(String blankText){
        return blankText.length();
    }
    
    protected void emitIndentsIfNeed(String blankText){
        int currIndent = calculateIndentChars(blankText);
        
        if(currIndent > lastIndent()){
            CommonToken t = new CommonToken(INDENT, "->");
            emitMore(t);
            t.setLine(input.getLine());
            t.setCharPositionInLine(input.getCharPositionInLine());
            t.setStartIndex(input.index());
            t.setStopIndex(input.index());
            //System.out.println("add last="+ currIndent);
            indents.add(currIndent);
        }else{
            for(int lastIndent = lastIndent(); currIndent < lastIndent;
            lastIndent = lastIndent())
            {
                CommonToken t = new CommonToken(DEDENT, "<-");
                emitMore(t);
                t.setLine(input.getLine());
                t.setCharPositionInLine(input.getCharPositionInLine());
                t.setStartIndex(input.index());
                t.setStopIndex(input.index());
                if(lastIndent > 0){
                    //System.out.println("last="+ lastIndent);
                    indents.removeLast();
                }
            }
        }
    }
    private int lastIndent(){
        Integer last = indents.peekLast();
        return last == null? 0: last.intValue();
    }
    
    private boolean setIndentDetect(boolean yes){
        noIndentCount = yes;
        return true;
    }
}

atom:
    ID | literal | enclosure;
enclosure :
             (generator_exp_LA)=> generator_expression
             | list_display
             | dict_display
             | '(' ( expression_list | yield_expression)? ')'
             | string_conversion 
;
literal :
             stringliteral | Integer | Longinteger
                | Floatnumber | Imagnumber
;
stringliteral :
             Stringliteralpiece;
parenth_form : 
             '(' expression_list? ')';

list_display 
:
            '[' (
                   (expression 'for')=> list_comprehension
                   | expression_list 
                   )?
                  ']';
  
list_comprehension :
             expression list_for;
  
list_for :
             'for' target_list 'in' old_expression_list
              list_iter?
;
old_expression_list :
             old_expression ((',' old_expression)+ ','?)?
             ;
list_iter : 
             list_for | list_if
;  
list_if : 
             'if' old_expression list_iter?
;
generator_exp_LA:
    '(' expression 'for' ;
generator_expression : 
             '(' expression genexpr_for ')'
;
genexpr_for : 
             'for' target_list 'in' or_test genexpr_iter?
;
genexpr_iter : 
             genexpr_for | genexpr_if
;
genexpr_if : 
             'if' old_expression genexpr_iter?
;

dict_display :
             '{' key_datum_list? '}'
;  
key_datum_list :
             key_datum (',' key_datum)* ','?
;
key_datum :
             expression ':' expression;

string_conversion :
             '`' expression_list '`';

             
//yield_atom : 
//             '(' yield_expression ')'
//;
yield_expression : 
             'yield' expression_list?
;
primary : 
             atom  
             ('.' ID 
                 | '['  slice_list ']'
                 | '(' ( (expression 'for')=> expression genexpr_for
                         | argument_list ','? 
                        )?
                    ')'
                 )*
;
 
slice_list : 
             slice_item (','  slice_item)* (',')?
;
slice_item : 
            ( expression? ':')=> proper_slice
             | expression
             | ellipsis
;
proper_slice : expression? ':' expression? (':' expression?)?
            // short_slice | long_slice
;

short_slice: expression? ':' expression? ;
long_slice: short_slice ':' expression?;

ellipsis : 
             '...'
;
argument_list : 
             positional_arguments (',' keyword_arguments)?
                                     (',' '*' expression)?
                                     (',' '**' expression)?
                | keyword_arguments (',' '*' expression)?
                                    (',' '**' expression)?
                | '*' expression (',' '**' expression)?
                | '**' expression
;
positional_arguments : 
             expression (',' expression)*
;
keyword_arguments : 
             keyword_item (',' keyword_item)*
;
keyword_item : 
             ID '=' expression
;
power : 
             primary ('**' u_expr)?
;
u_expr : 
             power
             |( '-' | '+'  | '~' ) u_expr
;
m_expr : 
             u_expr  ('*' u_expr
             |  '//' u_expr
             |  '/' u_expr
             |  '%' u_expr ) *
;
a_expr : 
             m_expr ( '+' m_expr | '-' m_expr)*
;
shift_expr : 
             a_expr (( '<<' | '>>' ) a_expr)*
;
and_expr : 
             shift_expr ( '&' shift_expr)*
;
xor_expr : 
             and_expr ( '^' and_expr)*
;
or_expr : 
             xor_expr ( '|' xor_expr)*
;
comparison : 
             or_expr ( comp_operator or_expr )*
;
comp_operator : 
             '<' | '>' | '==' | '>=' | '<=' | '<>' | '!='
             | 'is' 'not'? | 'not'? 'in'
;
expression 
    @init{ handler.onRuleStartByParent("extended", "extends");}
    @after{
        if(handler.isParentType("extends"))
            handler.setName(ruleText($start, $stop));
        handler.onRuleStopByParent($start, $stop, "extends");
    }
    : 
             conditional_expression | lambda_form
;
old_expression : 
             or_test | old_lambda_form
;
conditional_expression : 
             or_test ('if' or_test 'else' expression)?
;
or_test : 
             and_test ('or' and_test)*
;
and_test : 
             not_test ( 'and' not_test)*
;
not_test : 
             comparison | 'not' not_test
;
lambda_form : 
             'lambda' parameter_list? ':' expression
;
old_lambda_form : 
             'lambda' parameter_list? ':' old_expression
;
expression_list: 
             expression 
             ( (',' expression)=> ',' expression )* (',' )?
;
simple_stmt : 
                assert_stmt
                | (target_list ('='|augop))=> assignment_stmt
                | expression_stmt
                | pass_stmt
                | del_stmt
                | print_stmt
                | return_stmt
                | yield_stmt
                | raise_stmt
                | break_stmt
                | continue_stmt
                | import_stmt
                | global_stmt
                | exec_stmt
;
expression_stmt : 
             expression_list
;
assert_stmt : 
             'assert' expression (',' expression)?
;
assignment_stmt : 
             ((target_list ('='|augop))=> target_list ('='|augop) )+ (expression_list | yield_expression)
;
target_list 
: 
             target (',' target)* ','?
;
target :  primary 
;
//augmented_assignment_stmt : 
//             target augop
//              (expression_list | yield_expression)
//;
augop : 
             '+=' | '-=' | '*=' | '/=' | '//=' | '%=' | '**='
                | '>>=' | '<<=' | '&=' | '^=' | '|='
;
pass_stmt : 
             'pass'
;
del_stmt : 
             'del' target_list
;
print_stmt : 
             'print' (  (expression (',' expression)* ','? )?
                      | '>>' expression ((',' expression)+ ','? )? )
;
return_stmt : 
             'return' expression_list?
;
yield_stmt : 
             yield_expression
;
raise_stmt : 
             'raise' (expression (',' expression
              (',' expression)? )? )?
;
break_stmt : 
             'break'
;
continue_stmt : 
             'continue'
;
import_stmt : 
             'import' module ('as' name)?
                ( ',' module ('as' name)? )*
                | 'from' relative_module 'import' ID
                    ('as' name)?
                  ( ',' ID ('as' name)? )*
                | 'from' relative_module 'import' '('
                    ID ('as' name)?
                  ( ',' ID ('as' name)? )* ','? ')'
                | 'from' module 'import' '*'
  ;
module : 
             (ID '.')* ID
  ;
relative_module : 
             '.'* module | '.'+
  ;
name : 
             ID
;
global_stmt : 
             'global' ID (',' ID)*
;
exec_stmt : 
             'exec' or_expr
              ('in' expression (',' expression)?)?
;
compound_stmt : 
             if_stmt
             | while_stmt
             | for_stmt
             | try_stmt
             | with_stmt
             | funcdef
             | classdef
  ;
suite 
    : 
    stmt_list new_line
     | NEWLINE INDENT statement+ (EOF|DEDENT)
  ;
statement : 
             stmt_list new_line | compound_stmt | NEWLINE
  ;
stmt_list : 
             simple_stmt (';' simple_stmt)* ';'?
;
if_stmt : 
             'if' expression ':' suite
                ( 'elif' expression ':' suite )*
                ('else' ':' suite)?
;
while_stmt : 
             'while' expression ':' suite
                ('else' ':' suite)?
;
for_stmt : 
             'for' target_list 'in' expression_list
              ':' suite
                ('else' ':' suite)?
;
try_stmt :  try1_stmt
;
try1_stmt : 
             'try' ':' suite
                ('except' (expression
                             (',' target)?)? ':' suite)*
                ('else' ':' suite)?
                ('finally' ':' suite)?
;
//try2_stmt : 
//             'try' ':' suite
//                'finally' ':' suite
//;
with_stmt : 
  'with' expression ('as' target)? ':' suite
;
funcdef
    @init{
        handler.onRuleStart("func");
    }
    @after{
        handler.setName($funcName.text);
        handler.onRuleStop($start, $stop);
    }: 
             decorators? 'def' funcName=funcname '(' parameter_list? ')'
              ':' suite
              {
                System.out.println("func: "+ $funcname.text);
              }
;
decorators : 
             decorator+
;
decorator : 
             '@' dotted_name ('(' (argument_list ','? )? ')')? NEWLINE
;
dotted_name : 
             ID ('.' ID)*
;
parameter_list : 
     defparameter (',' defparameter)* ','? 
                 
;
defparameter : 
             parameter ('=' expression)?
             | '*' ID 
             | '**' ID 
;
sublist : 
             parameter (',' parameter)* ','?
;
parameter : 
             ID | '(' sublist ')'
;
funcname : 
             ID
;
classdef 
    @init{
        handler.onRuleStart("class");
    }
    @after{
        handler.onRuleStop($start, $stop);
    }
    : 
             'class' classname (inheritance)? ':'
              suite
;
inheritance 
    @init{
        handler.onRuleStart("extends");
    }
    @after{
        String ext = joinTokens($lp, $rp);
        handler.setName(ext.substring(1, ext.length()-1));
        handler.onRuleStop($start, $stop);
    }: 
             lp='(' expression_list? rp=')'
;
classname : 
             tk= ID {handler.setName($tk.getText());}
;
file_input
    @init{
        handler.onRuleStart("python");
    }
    @after{
        handler.onRuleStop($start, $stop);
    }
    : 
             (statement)+ EOF
;
interactive_input : 
             stmt_list? NEWLINE | compound_stmt NEWLINE
;
eval_input : 
             expression_list NEWLINE*
;
input_input : 
             expression_list NEWLINE
;
             
             
             
             

//fragment BackSlash: '\\' {  System.out.println("!!!!!!!!!!!!!!!"); };
LBracket @init{setIndentEnabled(true);}:'(';
RBracket @init{setIndentEnabled(false);}:')';
LB @init{setIndentEnabled(true);}:  '{';  
RB  @init{setIndentEnabled(false);}:  '}';
LS  @init{setIndentEnabled(true);}: //{setIndentEnabled(true)}?
'[' ;
RS @init{setIndentEnabled(false);}: //{setIndentEnabled(false)}?
']' ;
NoNewline: '\\' '\r'? '\n' {skip();};
new_line: NEWLINE|EOF;
NEWLINE
    @init{
        boolean hasSpace = false;
        boolean hasComment = false;
    }
    : 
    ( 
        '\r'? '\n' 
        (space = SP { hasSpace = true; } | { hasSpace = false; })
        ( COMMENT {hasComment = true;} | {hasComment = false;}) 
    )+
    { 
            if(noIndentCount || hasComment){
                skip();
            }else{
                emitMore(NEWLINE, "<New Line>"); 
                if(hasSpace)
                    emitIndentsIfNeed($space.getText());
                else
                    emitIndentsIfNeed("");
            }
    }
    ;
SP: (' '|'\t')+
    {  if(lineStart)
            emitIndentsIfNeed($text);
       else
            skip();
    };

COMMENT: '#' (~'\n')* {skip();}
    ;
//Nomeaning: '\r' {skip();} ;
fragment Letter: Lowercase|UpperCase
;

ID:
    (Letter|'_')
    (Letter | Digit | '_')* ;

fragment Lowercase:'a'..'z';
fragment UpperCase:'A'..'Z';
fragment Digit: '0'..'9';
Stringliteralpiece:
    ('u' | 'U' | 'r' | 'ur' | 'R' | 'UR' | 'Ur' | 'uR')?
    (Shortstring | Longstring)
    ;
//fragment Rawstringprefix: 'r' | 'ur' | 'R' | 'UR' | 'Ur' | 'uR' ;
fragment LongstringStart1:
    '\'\'\'';
fragment LongstringStart2:
    '"""';
fragment ShortstringStart1: '\'';
fragment ShortstringStart2: '"';

fragment Shortstring:     
    ShortstringStart1 (~('\''|'\n'| '\\') | Escapeseq)* ShortstringStart1
    | ShortstringStart2 (~('"'|'\n'| '\\') | Escapeseq)* ShortstringStart2

    ; 
fragment Longstring:
    //{input.LT(1) =='\'' && input.LT(2) =='\'' && input.LT(3) =='\''}?=>  
    LongstringStart1 (~(ShortstringStart1))+ LongstringStart1
    | //{input.LT(1) =='"' && input.LT(2) =='"' && input.LT(3) =='"'}?=>  
    LongstringStart2 (options {greedy=false;}: a= ~(LongstringStart2) )+ LongstringStart2
    ;
fragment Escapeseq: '\\' (Lowercase|UpperCase| Digit+ |'\''|'\\'|'.')
    ;
Integer: Decimalinteger | Octinteger | Hexinteger;
Longinteger :
             Integer ('l' | 'L'); 
fragment Decimalinteger: '1'..'9' Digit*
    | '0'
    ;
fragment Octinteger: '0' ('0'..'7')+;
fragment Hexinteger: '0' ('x' | 'X') (Digit|'a'..'f' |'A'..'F')+ ;
Star: '*';

Floatnumber: Pointfloat | Exponentfloat;
fragment Pointfloat: (Digit+) '.' Digit+
    | Digit+ '.'
    ;

fragment Exponentfloat: (Digit+ | Pointfloat) ('e' | 'E') ('+' | '-')? Digit+;
Imagnumber: (Floatnumber | Digit+) ('j'|'J');

ANYCHAR: . {  System.out.println("unknown char "+ $text); skip();};

//fragment Rawlongstring:
//    ;
//fragment Rawshortstring:
//    ;
