package org.liujing.parser;

import org.antlr.runtime.*;
import java.util.*;

/**
    Use <code>LJLexerHelper</code> instead, current ANTLR 3.3 doesn't support user defined 
    superclass for lexer
    
    Support multiple emits per nextToken invocation
    
*/
public abstract class LJBaseLexer extends Lexer{
    
    protected LinkedList<Token> emittedTokens = new LinkedList();
    
    public LJBaseLexer(CharStream input) {
        super(input);
    }
    
    public LJBaseLexer(CharStream input, RecognizerSharedState state) {
        super(input, state);
    }
    
    /** Support multiple emits per nextToken invocation
     @param tokens tokens
    */
	public void emit(Token... tokens){
	    for(Token t : tokens){
	        emittedTokens.add(t);
	    }
	}
	
	public void emit(List<Token> tokens) {
		emittedTokens.addAll(tokens);
	}
	
	/**
	the invokation sequence is
	<pre>
	    nextToken()  
	         ->mTokens()  
	                ->emit(token);//might call this
	         ->if(state.token == null)
	            emit();
	</pre>
	*/
	@Override
	public Token nextToken() {
	    Token emitted = emittedTokens.pollFirst(); // for mutiple emit
	    if(emitted != null){
	        return emitted;
	    }
	    while (true) {
	        
			state.token = null;
			state.channel = Token.DEFAULT_CHANNEL;
			state.tokenStartCharIndex = input.index();
			state.tokenStartCharPositionInLine = input.getCharPositionInLine();
			state.tokenStartLine = input.getLine();
			state.text = null;
			if ( input.LA(1)==CharStream.EOF ) {
                Token eof = new CommonToken((CharStream)input,Token.EOF,
                                            Token.DEFAULT_CHANNEL,
                                            input.index(),input.index());
                eof.setLine(getLine());
                eof.setCharPositionInLine(getCharPositionInLine());
                return eof;
			}
			try {
				mTokens();
				if ( state.token==null && emittedTokens.getFirst() == null) {
					emit();
					return state.token;
				}
				else if ( state.token==Token.SKIP_TOKEN ) {
					continue;
				}
				return emittedTokens.pollFirst(); // for mutiple emit
			}
			catch (NoViableAltException nva) {
				reportError(nva);
				recover(nva); // throw out current char and try again
			}
			catch (RecognitionException re) {
				reportError(re);
				// match() routine has already called recover()
			}
		}
	}
}
