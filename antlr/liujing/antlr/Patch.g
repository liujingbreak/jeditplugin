grammar Patch;

@header {
package liujing.antlr;
import java.util.logging.*;
import java.io.*;
}

@lexer::header{
package liujing.antlr;
}

parse:

    ;

fileIndex:
    INDEX path
    ;

/** todo */
path:
    {input.LT(1).getText().equals("path")}? INDEX
    ;

lineSep:
    ('=')* LT
    ;


INDEX:
    'index'
    ;

COMMENT:
    '/*' ~('*/')+ '*/'
    ;

LT
	: '\n'		// Line feed.
	| '\r'		// Carriage return.
	| '\u2028'	// Line separator.
	| '\u2029'	// Paragraph separator.
	;

WS:
    '\t'|' '
    ;
