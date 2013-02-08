package org.liujing.parser;

import org.antlr.runtime.*;
import java.util.*;

/**
    Support multiple emits per nextToken invocation
    <pre>
    usage:
        in ANTLR grammar file,
        @lexer::header{
        package ...
        import org.liujing.parser.LJLexerHelper;
        }
        @lexer::members {
            private LJLexerHelper lexerHelper = new LJLexerHelper();
            
            @Override
            public Token nextToken(){
                return lexerHelper.nextToken(this, state);
            }
            
            public void emitMore(Token... tokens){
                lexerHelper.emitMore(tokens);
            }
            
            public void emitMore(List<Token> tokens){
                lexerHelper.emitMore(tokens);
            }
            
            public void emitMore(int type, String text){
                CommonToken t = new CommonToken(type, text);
                t.setLine(input.getLine());
                t.setCharPositionInLine(input.getCharPositionInLine());
                emitMore(t);
            }
        }
        
    </pre>
*/
public class LJLexerHelper{
    protected LinkedList<Token> emittedTokens = new LinkedList();
    public LJLexerHelper(){
        
    }
    
    /** Support multiple emits per nextToken invocation
     @param tokens tokens
    */
	public void emitMore(Token... tokens){
	    for(Token t : tokens){
	        if(t != null)
	            emittedTokens.add(t);
	    }
	}
	
	public void emitMore(List<Token> tokens) {
		for(Token t : tokens){
	        if(t != null)
	            emittedTokens.add(t);
	    }
	}
	
	
	
	public Token nextToken(Lexer lexer, RecognizerSharedState state) {
	    CharStream input = lexer.getCharStream();
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
                eof.setLine(lexer.getLine());
                eof.setCharPositionInLine(lexer.getCharPositionInLine());
                return eof;
			}
			try {
				lexer.mTokens();
				emitted = emittedTokens.pollFirst();
				if(emitted != null){
                    return emitted;
                }
				if ( state.token==null) {
					lexer.emit();
					return state.token;
				}
				else if ( state.token==Token.SKIP_TOKEN ) {
					continue;
				}
				return state.token;
			}
			catch (NoViableAltException nva) {
				lexer.reportError(nva);
				lexer.recover(nva); // throw out current char and try again
			}
			catch (RecognitionException re) {
				lexer.reportError(re);
				// match() routine has already called recover()
			}
		}
	}
}
