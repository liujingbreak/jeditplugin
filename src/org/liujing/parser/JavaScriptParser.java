package org.liujing.parser;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import org.liujing.tools.compiler.*;
import java.util.regex.*;
import javax.swing.tree.*;

import sidekick.*;

/**
@author liujing
*/
public class JavaScriptParser extends SideKickParser{
	private Logger log = Logger.getLogger(JavaScriptParser.class.getName());
	
	@RegularExpDef(name = "SPACE")
	public Pattern whitespaces = Pattern.compile(" |\\n|\\r|\\t|\\f",Pattern.MULTILINE);
	
	@RegularExpDef(name = "EOL")
	public Pattern EOL = Pattern.compile("\\n",Pattern.MULTILINE);
	
	@RegularExpDef(name = "ANYCHAR")
	public Pattern ANYCHAR = Pattern.compile(".|\\n|\\r",Pattern.MULTILINE);
	
	@RegularExpDef(name = "SINGLE_LINE_COMMENT")
	public Pattern commentp = Pattern.compile("//.*$",Pattern.MULTILINE);
	
	@RegularExpDef(name = "STRING_LITERAL")
	public Pattern STRING_LITERAL = Pattern.compile("(?:\"([^\"\\\\\\n\\r]|(\\\\([ntbrf\\\\'\"]|[0-7]([0-7])?)))*\")|(?:'([^'\\\\\\n\\r]|(\\\\([ntbrf\\\\'\"]|[0-7]([0-7])?)))*')");
	
	@RegularExpDef(name = "QUOTE2")
	public Pattern quote2p = Pattern.compile("\"([^\"\\\\\\n\\r]|(\\\\([ntbrf\\\\'\"]|[0-7]([0-7])?)))*\"");
	//@RegularExpDef(name = "DECIMAL_LITERAL")
	//public Pattern decimalP = Pattern.compile("[1-9](?:[0-9])*");
	//
	//@RegularExpDef(name = "HEX_LITERAL")
	//public Pattern hexP = Pattern.compile("0[xX][0-9a-fA-F]+");
	//
	//@RegularExpDef(name = "OCTAL_LITERAL")
	//public Pattern octalP = Pattern.compile("0[0-7]*");
	
	@RegularExpDef(name = "INTEGER_LITERAL")
	public Pattern INTEGER_LITERAL = Pattern.compile("[1-9](?:[0-9])*|0[xX][0-9a-fA-F]+|0[0-7]*");
	
	@RegularExpDef(name = "FLOATING_POINT_LITERAL")
	public Pattern FLOATING_POINT_LITERAL = Pattern.compile("[0-9]+\\.[0-9]*(?:[eE][+-]?[0-9]+)?|\\.[0-9]+(?:[eE][+-]?[0-9]+)?|[0-9]+[eE][+-]?[0-9]+");
	
	@RegularExpDef(name = "IDENTIFIER")
	public Pattern IDENTIFIER = Pattern.compile("[$A-Z_a-z][$A-Z_a-z0-9]*");
	
	@RegularExpDef(name = "DO")
	public Pattern DO = Pattern.compile("do(?![$A-Z_a-z0-9])");
	@RegularExpDef(name = "IF")
	public Pattern IF = Pattern.compile("if(?![$A-Z_a-z0-9])");
	@RegularExpDef(name = "ELSE")
	public Pattern ELSE = Pattern.compile("else(?![$A-Z_a-z0-9])");
	@RegularExpDef(name = "IN")
	public Pattern IN = Pattern.compile("in(?![$A-Z_a-z0-9])");
	@RegularExpDef(name = "DEFAULT")
	public Pattern DEFAULT = Pattern.compile("default(?![$A-Z_a-z0-9])");
	@RegularExpDef(name = "WHILE")
	public Pattern WHILE = Pattern.compile("while(?![$A-Z_a-z0-9])");
	
	@RegularExpDef(name = "FOR")
	public Pattern FOR = Pattern.compile("for(?![$A-Z_a-z0-9])");
	
	@RegularExpDef(name = "THIS")
	public Pattern THIS = Pattern.compile("this(?![$A-Z_a-z0-9])");
	
	@RegularExpDef(name = "SWITCH")
	public Pattern SWITCH = Pattern.compile("switch(?![$A-Z_a-z0-9])");
	
	@RegularExpDef(name = "CASE")
	public Pattern CASE = Pattern.compile("case(?![$A-Z_a-z0-9])");
	@RegularExpDef(name = "RETURN")
	public Pattern RETURN = Pattern.compile("return(?![$A-Z_a-z0-9])");
	@RegularExpDef(name = "THROW")
	public Pattern THROW = Pattern.compile("throw(?![$A-Z_a-z0-9])");
	@RegularExpDef(name = "CONTINUE")
	public Pattern CONTINUE = Pattern.compile("continue(?![$A-Z_a-z0-9])");
	@RegularExpDef(name = "CATCH")
	public Pattern CATCH = Pattern.compile("catch(?![$A-Z_a-z0-9])");
	@RegularExpDef(name = "BREAK")
	public Pattern BREAK = Pattern.compile("break(?![$A-Z_a-z0-9])");
	@RegularExpDef(name = "FINALLY")
	public Pattern FINALLY = Pattern.compile("finally(?![$A-Z_a-z0-9])");
	@RegularExpDef(name = "TRUE")
	public Pattern TRUE = Pattern.compile("true(?![$A-Z_a-z0-9])");
	@RegularExpDef(name = "FALSE")
	public Pattern FALSE = Pattern.compile("false(?![$A-Z_a-z0-9])");
	@RegularExpDef(name = "NULL")
	public Pattern NULL = Pattern.compile("null(?![$A-Z_a-z0-9])");
	@RegularExpDef(name = "NEW")
	public Pattern NEW = Pattern.compile("new(?![$A-Z_a-z0-9])");
	@RegularExpDef(name = "UNDEFINED")
	public Pattern UNDEFINED = Pattern.compile("undefined(?![$A-Z_a-z0-9])");
	@RegularExpDef(name = "TRY")
	public Pattern TRY = Pattern.compile("try(?![$A-Z_a-z0-9])");
	@RegularExpDef(name = "VAR")
	public Pattern VAR = Pattern.compile("var(?![$A-Z_a-z0-9])");

	@RegularExpDef(name = "TYPEOF")
	public Pattern TYPEOF = Pattern.compile("typeof(?![$A-Z_a-z0-9])");
	@RegularExpDef(name = "FUNCTION")
	public Pattern FUNCTION = Pattern.compile("function(?![$A-Z_a-z0-9])");
	@RegularExpDef(name = "SYNCHRONIZED")
	public Pattern SYNCHRONIZED = Pattern.compile("synchronized(?![$A-Z_a-z0-9])");
	
	
	
	@RegularExpDef(name = "ASSIGN")
	public Pattern ASSIGN = Pattern.compile("=(?!=)");
	
	@RegularExpDef(name = "LT")
	public Pattern LT = Pattern.compile("<(?![<=])");
	
	@RegularExpDef(name = "PLUS")
	public Pattern PLUS = Pattern.compile("\\+(?![+=])");
	
	@RegularExpDef(name = "MINUS")
	public Pattern MINUS = Pattern.compile("-(?![-=])");
	
	@RegularExpDef(name = "STAR")
	public Pattern STAR = Pattern.compile("\\*(?![=])");
	
	@RegularExpDef(name = "SLASH")
	public Pattern SLASH = Pattern.compile("/(?![=])");
	@RegularExpDef(name = "BIT_AND")
	public Pattern BIT_AND = Pattern.compile("&(?![&=])");
	@RegularExpDef(name = "BIT_OR")
	public Pattern BIT_OR = Pattern.compile("\\|(?![|=])");
	
	@RegularExpDef(name = "XOR")
	public Pattern XOR = Pattern.compile("\\^(?![=])");
	@RegularExpDef(name = "REM")
	public Pattern REM = Pattern.compile("%(?![=])");
	@RegularExpDef(name = "EQ")
	public Pattern EQ = Pattern.compile("==(?![=])");
	@RegularExpDef(name = "NE")
	public Pattern NE = Pattern.compile("!=(?![=])");
	
	@RegularExpDef(name = "BANG")
	public Pattern BANG = Pattern.compile("!(?![=])");
	@RegularExpDef(name = "LSHIFT")
	public Pattern LSHIFT = Pattern.compile("<<(?![=])");
	@RegularExpDef(name = "GT")
	public Pattern GT = Pattern.compile(">(?![>=])");
	@RegularExpDef(name = "RSIGNEDSHIFT")
	public Pattern RSIGNEDSHIFT = Pattern.compile(">>(?![>=])");
	@RegularExpDef(name = "RUNSIGNEDSHIFT")
	public Pattern RUNSIGNEDSHIFT = Pattern.compile(">>>(?![=])");
	@RegularExpDef(name = "DOT")
	public Pattern DOT = Pattern.compile("\\.(?![.])");
	//< TILDE: "~" >
	private SideKickParsedData data;
	private org.gjt.sp.jedit.Buffer buffer;
	private AnotationTranslater core;
	private DefaultMutableTreeNode parentNode;
	
	public JavaScriptParser()throws Exception{
		super("myJs");
		core = new AnotationTranslater(this);
	}
	
	public SideKickParsedData parse(org.gjt.sp.jedit.Buffer buffer, errorlist.DefaultErrorSource errorSource)
	{
		data=new SideKickParsedData(buffer.getName());
		try{
			this.buffer = buffer;
			parse(buffer.getText(0,buffer.getLength()));
			//node=new DefaultMutableTreeNode("test");
			//data.root.add(node);
			
		}catch(Exception e){
			log.log(Level.WARNING,"parse failed",e);
		}
		return data;
	}
	
	public void parse(CharSequence input)throws Exception{
		core.parse(input);
	}
	
	@SkipBnf("<SPACE>|multlineComment()|<SINGLE_LINE_COMMENT>")
	public void skip(LLLACompiler p){
	}
	
	@BnfMethod("'/*'(!LOOKAHEAD('*/') <ANYCHAR>)* '*/' ")
	public void multlineComment(LLLACompiler p){}
	
	@RootBnf ("(BlockStatement())*<EOF>")
	public void Start(LLLACompiler p,@Variable(name="parentNode") DefaultMutableTreeNode parentNode){
		if(parentNode!=null && data!=null)
			data.root.add(parentNode);
	}
	
	@BnfMethod("'{' ( BlockStatement() )*'}'")
	public void Block(LLLACompiler p){
		
	}
	
	@BnfMethod("<VAR>")
	public void Type(LLLACompiler p){
	}
	
	@BnfMethod("LOOKAHEAD( Type() <IDENTIFIER> )LocalVariableDeclaration() [';'] | Statement()")
	public void BlockStatement(LLLACompiler p){
	}
	@BnfMethod("Type() VariableDeclarator() ( ',' VariableDeclarator() )*")
	public void LocalVariableDeclaration(LLLACompiler p){
	}
	@BnfMethod("';'")
	public void EmptyStatement(LLLACompiler p){}
	
	@BnfMethod("id = <IDENTIFIER> [ <ASSIGN> VariableInitializer() ]")
	public void VariableDeclarator(LLLACompiler p,
		@Variable(name="id") Token id,
		@Variable(name="expType")Object expType)
	{
		
		if(expType!=null && expType instanceof DefaultMutableTreeNode){
			DefaultMutableTreeNode subNode = (DefaultMutableTreeNode)expType;
			JsNode jsnode = (JsNode)subNode.getUserObject();
			String name = id.getImage().toString();
			jsnode.setName(name);
			getParentNode(p).add(subNode);
		}
	}
	
	private DefaultMutableTreeNode getParentNode(LLLACompiler p){
		try{
			DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)p.getVariable("parentNode");
			if(parentNode==null){
				parentNode = new DefaultMutableTreeNode("*");
				p.setVariable("parentNode",parentNode);
			}
			return parentNode;
		}catch( LLLAParserException le){
			log.log(Level.SEVERE,"",le);
			return null;
		}
	}

	
	@BnfMethod("ArrayInitializer()|Expression()")
	public void VariableInitializer(LLLACompiler p){
		
	}
	
	@BnfMethod("'[' [ VariableInitializer() ( LOOKAHEAD(2) ',' VariableInitializer() )* ] ']'")
	public void ArrayInitializer(LLLACompiler p){
	}
	
	@BnfMethod("expType=JSONExpression()| expType=FunctionExpression()|ConditionalExpression() [LOOKAHEAD(2)AssignmentOperator() Expression()]")
	public void Expression(LLLACompiler p){
	}
	
	@BnfMethod({"'{' [JSONObjectUnit() (',' JSONObjectUnit())*] '}'| '[' [Expression() (',' Expression())*] ']'"})
	public void JSONExpression(LLLACompiler p,@Variable(name="jsonNodes") List jsonNodes ){
		if((jsonNodes!=null) && jsonNodes.size()>0){
			DefaultMutableTreeNode node = new DefaultMutableTreeNode("{}");
			for(int i=0,l=jsonNodes.size();i<l;i++)
				node.add((DefaultMutableTreeNode)jsonNodes.get(i));
			getParentNode(p).add(node);
		}
	}
	
	@BnfMethod("jsonPName=<IDENTIFIER> ':' Expression()")
	public void JSONObjectUnit(LLLACompiler p,@Variable(name="jsonPName")Token jsonPName,
		@Variable(name="expType")Object result)throws LLLAParserException{
		log.fine("JSON object property:"+jsonPName.getImage());
		if(result!=null && result instanceof DefaultMutableTreeNode){
			DefaultMutableTreeNode subNode = (DefaultMutableTreeNode)result;
			JsNode jsnode = (JsNode)subNode.getUserObject();
			String name = jsonPName.getImage().toString();
			jsnode.setName(name);
			List jsonNodes = (List)p.getVariable("jsonNodes");
			if(jsonNodes==null){
				jsonNodes = new ArrayList();
				p.setVariable("jsonNodes",jsonNodes);
			}
			jsonNodes.add(subNode);
		}
	}
	
	@BnfMethod("functionTk=<FUNCTION> [functionName=<IDENTIFIER>]'(' [<IDENTIFIER>(',' <IDENTIFIER>)*] paramsLR=')' Block()")
	public DefaultMutableTreeNode FunctionExpression(LLLACompiler p, @Variable(name="functionTk") Token functionTk,
		@Variable(name="functionName") Token functionName, 
		@Variable(name="paramsLR") Token paramsLR){
		log.fine("function def");
		int start = functionTk.getStartPos();
		int endParam = paramsLR.getEndPos();
		String name = functionName!=null?functionName.getImage().toString():"<function>";
		String jnodeName = "<func>";
		if(buffer!=null){
			jnodeName = buffer.getText(start,endParam-start);			
		}
		JsNode jnode = new JsNode(name,jnodeName);
		jnode.setStartOffset(start);
		jnode.setEndOffset(endParam);
		DefaultMutableTreeNode subnode = new DefaultMutableTreeNode(jnode);
		
		return subnode;
	}
	
	@BnfMethod("ConditionalOrExpression() [ '?' Expression() ':' Expression() ]")
	public void ConditionalExpression(LLLACompiler p){
	}
	
	@BnfMethod("ConditionalAndExpression() ( '||' ConditionalAndExpression() )*")
	public void ConditionalOrExpression(LLLACompiler p){
	}
	
	@BnfMethod("InclusiveOrExpression() ( '&&' InclusiveOrExpression() )*")
	public void ConditionalAndExpression(LLLACompiler p){
	}
	
	@BnfMethod("ExclusiveOrExpression() ( <BIT_OR> ExclusiveOrExpression() )*")
	public void InclusiveOrExpression(LLLACompiler p){
	}
	
	@BnfMethod("AndExpression() ( <XOR> AndExpression() )*")
	public void ExclusiveOrExpression(LLLACompiler p){
	}
	
	@BnfMethod("EqualityExpression() (  <BIT_AND> EqualityExpression() )*")
	public void AndExpression(LLLACompiler p){
	}
	
	@BnfMethod("InstanceOfExpression() ( ( <EQ> | <NE> |'==='|<IN> ) InstanceOfExpression() )*")
	public void EqualityExpression(LLLACompiler p){
	}
	
	@BnfMethod("RelationalExpression() [ 'instanceof' RelationalExpression() ]")
	public void InstanceOfExpression(LLLACompiler p){
	}
	
	@BnfMethod("ShiftExpression() ( ( '<=' | '>=' | <LT> | <GT> ) ShiftExpression() )*")
	public void RelationalExpression(LLLACompiler p){
	}
	
	@BnfMethod("  AdditiveExpression() ( ( add1=<LSHIFT> | add1=<RUNSIGNEDSHIFT> | add1=<RSIGNEDSHIFT> ) AdditiveExpression() )*")
	public void ShiftExpression(LLLACompiler p,@Variable(name="add1")Token add1){
		log.fine("............"+add1);
	}
	                                   
	@BnfMethod("MultiplicativeExpression() ( ( <PLUS> | <MINUS> ) MultiplicativeExpression() )*")
	public void AdditiveExpression(LLLACompiler p){
	}
	
	@BnfMethod("UnaryExpression() ( ( <STAR> | <SLASH> | <REM> ) UnaryExpression() )*")
	public void MultiplicativeExpression(LLLACompiler p){
	}
	
	@BnfMethod({"<TYPEOF> UnaryExpression()",
	"| ( <PLUS> | <MINUS> ) UnaryExpression()|PreIncrementExpression()|PreDecrementExpression()|UnaryExpressionNotPlusMinus()"})
	public void UnaryExpression(LLLACompiler p){
		log.fine("UnaryExpression");
	}
	
	@BnfMethod("'++' PrimaryExpression()")
	public void PreIncrementExpression(LLLACompiler p){
		log.fine("++");
	}
	
	@BnfMethod("'--' PrimaryExpression()")
	public void PreDecrementExpression(LLLACompiler p){
	}

	@BnfMethod(" ( '~' | <BANG> ) UnaryExpression() | PostfixExpression()")
	public void UnaryExpressionNotPlusMinus(LLLACompiler p){
	}
	
	@BnfMethod("PrimaryExpression() [ '++' | '--' ]")
	public void PostfixExpression(LLLACompiler p){
	}
	
	@BnfMethod("PrimaryPrefix() ( LOOKAHEAD(2) PrimarySuffix() )*")
	public void PrimaryExpression(LLLACompiler p){
	}
	
	@BnfMethod("<DOT> <IDENTIFIER>")
	public void MemberSelector(LLLACompiler p){
	}
	
	@BnfMethod("Literal()| <THIS> |'(' Expression() ')'| AllocationExpression()|Name()")
	public void PrimaryPrefix(LLLACompiler p){
	}
	
	@BnfMethod({"LOOKAHEAD(2) <DOT>  AllocationExpression()",
	"|LOOKAHEAD(3) memberFunctionCall()",
	"|LOOKAHEAD(3) MemberSelector()",
	"|'[' Expression() ']'"})
	public void PrimarySuffix(LLLACompiler p){
	}
	
	@BnfMethod("[<DOT>  memberFunName=<IDENTIFIER>] Arguments()")
	public void memberFunctionCall(LLLACompiler p,@Variable(name="memberFunName")Token funName){
		log.fine("function call");
		if(funName!=null)
			log.fine("function name = "+funName.getImage());
	}
	
	@BnfMethod("<INTEGER_LITERAL>|<FLOATING_POINT_LITERAL>|<STRING_LITERAL>|(<TRUE>|<FALSE>) |(<NULL>|<UNDEFINED>)")
	public void Literal(LLLACompiler p){
	}
	
	@BnfMethod("<IDENTIFIER>( LOOKAHEAD(2) <DOT>  <IDENTIFIER>)*")
	public void Name(LLLACompiler p){
	}
	
	@BnfMethod("<NEW> PrimaryExpression()")
	public void AllocationExpression(LLLACompiler p){
	}
	
	
	@BnfMethod("'(' [ Expression() ( ',' Expression() )* ] ')'")
	public void Arguments(LLLACompiler p){
	}
	
	@BnfMethod(" assignSym=<ASSIGN> | '*=' | '/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '>>>=' | '&=' | '^=' | '|='")
	public Token AssignmentOperator(LLLACompiler p,@Variable(name="assignSym")Token assignSym){
		return assignSym;
	}
	
	/**
	StatementExpression() must be placed at the last of choices, otherwise parser will regard those 'if' or 'while' key words
	as a IDENTIFIER token in StatementExpression(), the parser will never go to other BNF.
	*/
	@BnfMethod({"SwitchStatement()|IfStatement()|WhileStatement()|DoStatement()|ForStatement()|BreakStatement()"
	,"|ContinueStatement()|ReturnStatement()|ThrowStatement()|SynchronizedStatement()|TryStatement()",
	"|LOOKAHEAD(2)  LabeledStatement() | Block()|EmptyStatement()|StatementExpression() [';']"})
	public void Statement(LLLACompiler p){
		log.fine("statement");
	}
	
	@BnfMethod("<IDENTIFIER> ':' Statement()")
	public void LabeledStatement(LLLACompiler p){
	}
	
	@BnfMethod("PreIncrementExpression()|PreDecrementExpression()|PrimaryExpression()['++'|'--'|AssignmentOperator() Expression()]")
	public void StatementExpression(LLLACompiler p,@Variable(name="expType")Object expType){
	}
	
	@BnfMethod("<SWITCH> '(' Expression() ')' '{'( SwitchLabel() ( BlockStatement() )* )* '}'")
	public void SwitchStatement(LLLACompiler p){
	}
	
	@BnfMethod("<CASE> Expression() ':'|<DEFAULT> ':'")
	public void SwitchLabel(LLLACompiler p){
	}
	
	@BnfMethod(" <IF> '(' Expression() ')' Statement() [ LOOKAHEAD(1) <ELSE> Statement() ]")
	public void IfStatement(LLLACompiler p){
		log.fine("if");
	}
	
	@BnfMethod("<WHILE> '(' Expression() ')' Statement()")
	public void WhileStatement(LLLACompiler p){
	}
	
	@BnfMethod("<DO> Statement() <WHILE> '(' Expression() ')' [';']")
	public void DoStatement(LLLACompiler p){
	}
	//
	@BnfMethod("<FOR> '('(LOOKAHEAD( Type() <IDENTIFIER> <IN>) Type() <IDENTIFIER> <IN> Expression()| [ ForInit() ] ';' [ Expression() ] ';' [ ForUpdate() ])')' Statement()")
	public void ForStatement(LLLACompiler p){
	}
	
	@BnfMethod("LOOKAHEAD(Type() <IDENTIFIER> ) LocalVariableDeclaration() | StatementExpressionList()")
	public void ForInit(LLLACompiler p){
		
	}
	
	@BnfMethod("StatementExpression() ( ',' StatementExpression() )*")
	public void StatementExpressionList(LLLACompiler p){
		
	}
	
	@BnfMethod("StatementExpressionList()")
	public void ForUpdate(LLLACompiler p){
		
	}
	
	@BnfMethod("<BREAK> [ <IDENTIFIER> ] [';']")
	public void BreakStatement(LLLACompiler p){
		
	}
	
	@BnfMethod("<CONTINUE> [ <IDENTIFIER> ] [';']")
	public void ContinueStatement(LLLACompiler p){
		
	}
	@BnfMethod("<RETURN> [ Expression() ] [';']")
	public void ReturnStatement(LLLACompiler p){
		
	}
	@BnfMethod("<THROW>  Expression() [';']")
	public void ThrowStatement(LLLACompiler p){
		
	}
	
	@BnfMethod("<SYNCHRONIZED> '(' Expression() ')' Block()")
	public void SynchronizedStatement(LLLACompiler p){
		
	}
	
	@BnfMethod({"<TRY> Block()",
  "( <CATCH> '(' <IDENTIFIER> ')' Block() )*",
  "[ <FINALLY> Block() ]"})
	public void TryStatement(LLLACompiler p){
		
	}
}
