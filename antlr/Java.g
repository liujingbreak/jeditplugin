grammar Java;

options
{
	backtrack=false;
	memoize=true;
	//k=1;
	superClass=LJBaseParser;
}
scope fieldDecScope{
    String name;
    String typeStr;
}
@header {
package liujing.jedit.parser;

import java.util.logging.*;
import java.io.*;
import org.liujing.parser.*;
import org.liujing.jeditplugin.v2.JavaFileAnalysisHandler;
import org.liujing.jeditplugin.v2.JavaParserFieldDecHandler;
import org.liujing.jeditplugin.v2.JavaParserMethodDecHandler;

import org.liujing.ironsword.grammar.AntlrGrammarHandler;
}

@members {
    static Logger log = Logger.getLogger(JavaParser.class.getName());
    private JavaFileAnalysisHandler handler;
    private JavaParserFieldDecHandler fdHandler;
    private int classNestLevel = 0;
    private AntlrGrammarHandler agh = null;
    
    public void setAntlrGrammarHandler(AntlrGrammarHandler h){
        agh = h;
    }
}
@rulecatch {
    catch (RecognitionException e) {
        reportError(e);
        throw e;
    }
}

@lexer::header{
package liujing.jedit.parser;
import java.util.logging.*;
}
@lexer::members {
    static Logger log = Logger.getLogger(JavaLexer.class.getName());
    public boolean inHTML = false;
    public String lastDocText;
    
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
//----------- Parser -----------------
fieldDeclarations [JavaParserFieldDecHandler fdHandler]
    @init{
        this.fdHandler = fdHandler;
    }
    :   fieldDeclaration*
    ;

compilationUnit[JavaFileAnalysisHandler handler]
    scope fieldDecScope;
    @init{
        this.handler = handler;
        if(agh != null)
         agh.onRuleStart("Java");
    }
    @after{
        if(agh != null)
            agh.onRuleStop($start, $stop);
    }
    :   ((annotations? 'package')=>   (annotations
            )?
            packageDeclaration
        )?
        (importDeclaration
        )*
        (typeDeclaration
        )* EOF
    ;
onlyParsePackageDef
    :((annotations? 'package')=>   (annotations
            )?
            packageDeclaration
        )?
        ;
//------------- insert documentation comment --------------
docFieldOrMethod[JavaParserMethodDecHandler methodHandler]
    :    (methodDeclarationLA3)=> docMethod[methodHandler]
    |    (modifiers  (IDENTIFIER|primitiveType))=> docField[methodHandler]
    ;

docField[JavaParserMethodDecHandler methodHandler]
    scope fieldDecScope;
    @init{
        CommonToken typeT = null;
    }
    :   modifiers
        t=type      { $fieldDecScope::typeStr = $t.text; }
        variableDeclarator  { methodHandler.onFieldDec($fieldDecScope::name, $fieldDecScope::typeStr); }
        (',' variableDeclarator { methodHandler.onFieldDec($fieldDecScope::name, $fieldDecScope::typeStr); }
        )*
        ';'
    ;
docMethod[JavaParserMethodDecHandler methodHandler]
    @init{
        List<String> throwsList = null;
        String returnType = null;
    }
    :
        modifiers (typeParameters)?
        (   /* For constructor, return type is null, name is 'init' */
            name=IDENTIFIER
            ps=formalParameters ('throws' qlist=doc_qualifiedNameList {throwsList = $qlist.types;} )?
            block
            {
                methodHandler.onConstructDeclaration($name.text, $ps.paramNames, (throwsList==null? new ArrayList(): throwsList));
            }
        |   (   tp=type     {returnType = $tp.text;}
                | 'void')
            name=IDENTIFIER
            ps=formalParameters ('[' ']' )* ('throws'  qlist=doc_qualifiedNameList {throwsList = $qlist.types;} )?
            ('{' | ';')
            {
                methodHandler.onMethodDeclaration($name.text, $ps.paramNames, returnType, (throwsList==null? new ArrayList(): throwsList));
            }
        )
    ;
doc_qualifiedNameList returns[List<String> types]
    @init{
        types = new ArrayList();
    }
    :   q1=qualifiedName        {types.add($q1.name);}
        (',' q2=qualifiedName   {types.add($q2.name);}
        )*
    ;
//--------------------------------------------------------------------
typeDeclaration
    :   classOrInterfaceDeclaration
    |   ';'
    ;

modifiers
    :
    (    annotation
    |   'public' { if(agh != null ) agh.addNode("access", "public", $start, $start);}
    |   'protected' { if(agh != null ) agh.addNode("access", "public", $start, $start);}
    |   'private' { if(agh != null ) agh.addNode("access", "private", $start, $start);}
    |   'static' { if(agh != null ) agh.addNode("static", "true", $start, $start);}
    |   'abstract'
    |   'final'
    |   'native'
    |   'synchronized'
    |   'transient'
    |   'volatile'
    |   'strictfp'
    )*
    ;


variableModifiers
    :   (   'final'
        |   annotation
        )*
    ;
classOrInterfaceDeclaration
    :  (modifiers  ('class'|'enum'))=>  classDeclaration
    |   interfaceDeclaration
    ;
classOrInterfaceDeclarationLA2 //todo
    :   modifiers  ('class'|'interface'|'enum')
    ;

classDeclaration
    @init{
        classNestLevel ++;
        if(agh != null) agh.onRuleStart("class");
    }
    @after{
        if(agh!= null) agh.onRuleStop($start, $stop);
        classNestLevel --;
    }
    :   (modifiers  'class')=> normalClassDeclaration
    |   enumDeclaration
    ;
interfaceDeclaration
    @init{
        if(agh != null) agh.onRuleStart("class");
        classNestLevel ++;
    }
    @after{
        if(agh!= null) agh.onRuleStop($start, $stop);
        classNestLevel --;
    }
    :   normalInterfaceDeclaration
    //todo|   annotationTypeDeclaration
    ;
normalClassDeclaration
    @init{
        if(agh != null) agh.addNode("type","class", $start, $start);
    }
    :   modifiers  'class' name=IDENTIFIER
             {
                if(agh != null)
                    agh.setName($name.text);
                if(handler != null)
                    handler.onTypeDeclaration($IDENTIFIER.text, classNestLevel, $IDENTIFIER.getLine());
             }
        (typeParameters
        )?
        ('extends' ext=type { if(agh != null) agh.addNode("extended", $ext.text, $start, $start); }
        )?
        ('implements' impl_typeList
        )?
        classBody
    ;

impl_typeList
    : t= type { if(agh != null) agh.addNode("extended", $t.text, $start, $start); }
        (',' t= type { if(agh != null) agh.addNode("extended", $t.text, $start, $start); }
        )*;
    
normalInterfaceDeclaration
    @init{
        if(agh != null) agh.addNode("type","interface", $start, $start);
    }
    :   modifiers 'interface' name=IDENTIFIER
            {
                if(agh != null)
                    agh.setName($name.text);
                if(handler != null)
                    handler.onTypeDeclaration($IDENTIFIER.text, classNestLevel, $IDENTIFIER.getLine());
             }
        (typeParameters
        )?
        ('extends' typeList
        )?
        interfaceBody
    ;
interfaceBody
    :   '{'
        (interfaceBodyDeclaration
        )*
        '}'
    ;
classBody
    :   '{'
        (classBodyDeclaration
        )*
        '}'
    ;

classBodyDeclaration
    :   ';'
    |   ('static' )? block
    |   memberDecl
    ;
interfaceBodyDeclaration
    :   (methodDeclarationLA3)=> interfaceMethodDeclaration
    |   (modifiers type )=> interfaceFieldDeclaration
    |   (modifiers  'interface')=> interfaceDeclaration
    |   classDeclaration
    |   ';'
    ;

interfaceMethodDeclaration
    @init{ if(agh!= null) {
            agh.onRuleStart("method"); 
            }
        
    }
    @after{
        if(agh!= null){
            agh.setName($name.text);
            CommonToken doct = (CommonToken) ((CommonTokenStream)input).get( $start.getTokenIndex() -1);
            if(doct.getChannel() == Token.HIDDEN_CHANNEL)
                agh.addNode("doc", doct.getText(), doct, doct);
            agh.onRuleStop($start, $stop);
        }
    }
    :   modifiers (typeParameters )? (type |'void' ) name=IDENTIFIER
        { if(agh!= null) agh.scopeStart("params"); }
        formalParameters ('[' ']' )*  { if(agh!= null) agh.scopeEnd(); }
        ('throws' qualifiedNameList )? ';'
    ;

/**
 * NOTE, should not use variableDeclarator here, as it doesn't necessary require
 * an initializer, while an interface field does, or judge by the returned value.
 * But this gives better diagnostic message, or antlr won't predict this rule.
 */
interfaceFieldDeclaration
    @init{
        if(agh != null)
            agh.onRuleStart("field");
    }
    @after{
        if(agh != null){
            CommonToken doct = (CommonToken) ((CommonTokenStream)input).get( $start.getTokenIndex() -1);
            if(doct.getChannel() == Token.HIDDEN_CHANNEL)
                agh.addNode("doc", doct.getText(), doct, doct);
            agh.onRuleStop($start, $stop);
        }
    }
    :   modifiers type variableDeclarator
        (',' {if(agh != null) {
                agh.onRuleStop($start, $stop);
                agh.onRuleStart("field");
                }
            }
            variableDeclarator
        )*
        ';'
    ;

memberDecl
    :    (methodDeclarationLA3)=> methodDeclaration
    |    (modifiers  (IDENTIFIER|primitiveType))=> fieldDeclaration
    |    (modifiers  ('class'|'enum'))=> classDeclaration
    |    interfaceDeclaration
    ;

fieldDeclaration
    scope fieldDecScope;
    @init{
        CommonToken typeT = null;
        if(agh != null)
            agh.onRuleStart("field");
    }
    @after{
        if(agh != null){
            agh.addNode("field_type", $t.text, $start, $stop);
            CommonToken doct = (CommonToken) ((CommonTokenStream)input).get( $start.getTokenIndex() -1);
            if(doct.getChannel() == Token.HIDDEN_CHANNEL)
                agh.addNode("doc", doct.getText(), doct, doct);
            agh.onRuleStop($start, $stop);
            
        }
    }
    :   modifiers
        t=type      { $fieldDecScope::typeStr = $t.text; }
        variableDeclarator  { if(fdHandler != null) fdHandler.onFieldDeclaration($fieldDecScope::name, $fieldDecScope::typeStr); }
        (
            {if(agh != null) {
                agh.onRuleStop($start, $stop);
                agh.onRuleStart("field");
                agh.addNode("field_type", $t.text, $start, $stop);
                }
            }
        ',' variableDeclarator { if(fdHandler != null) fdHandler.onFieldDeclaration($fieldDecScope::name, $fieldDecScope::typeStr); }
        )*
        ';'
    ;
variableDeclarator
    @after{
        if(agh != null && agh.isType("field") )
            agh.setName($id.text);
    }
    :   id=IDENTIFIER   {$fieldDecScope::name = $id.text; }
        ('[' ']'        {$fieldDecScope::typeStr += "[]";}
        )*
        ('=' variableInitializer
        )?
    ;

variableInitializer
    :   arrayInitializer
    |   expression
    ;

arrayInitializer
    :   '{'
            (variableInitializer
                (',' variableInitializer
                )*
            )?
            (',')?
        '}'             //Yang's fix, position change.
    ;
typeParameters
    :   '<'
            typeParameter (',' typeParameter )*
        '>'
    ;

typeParameter
    @after{
        if(handler != null)
            handler.onTypeParameter($id.text);
    }
    :   id=IDENTIFIER
        ('extends' typeBound
        )?
    ;

typeList
    :   type
        (',' type
        )*
    ;
typeBound
    :   type
        ('&' type
        )*
    ;

methodDeclarationLA3
    :   modifiers (
            typeParameters
            |'void'
            | IDENTIFIER '('
            | type IDENTIFIER '('
            )
    ;
methodDeclaration
    @init{ if(agh!= null) {
            agh.onRuleStart("method"); 
            }
        
    }
    @after{ if(agh!= null){
        agh.setName($name.text);
        CommonToken doct = (CommonToken) ((CommonTokenStream)input).get( $start.getTokenIndex() -1);
        if(doct.getChannel() == Token.HIDDEN_CHANNEL)
            agh.addNode("doc", doct.getText(), doct, doct);
        agh.onRuleStop($start, $stop); 
        }
    }
    :

        modifiers (typeParameters)?
        (   /* For constructor, return type is null, name is 'init' */
            name = IDENTIFIER { if(agh!= null) agh.scopeStart("params"); }
            formalParameters { if(agh!= null) agh.scopeEnd(); } ('throws' qualifiedNameList )?
            block
        |   (typestr= type 
                {   if(agh!= null) {
                        agh.addNode("return", $typestr.text, $typestr.start, $typestr.stop);
                    } 
                }
            |   v='void' 
                {   if(agh!= null) {
                        agh.addNode("return", "void", $v, $v);
                    } 
                }
            ) 
            name = IDENTIFIER
            { if(agh!= null) agh.scopeStart("params"); }
            formalParameters ('[' ']' )* { if(agh!= null) agh.scopeEnd(); }
            ('throws' qualifiedNameList )?
            ( block |   ';' )
        )
    ;

block
    :   '{'
        (blockStatement
        )*
        '}'
    ;
blockStatement
    :   (variableModifiers type IDENTIFIER)=>localVariableDeclarationStatement
    |   (classOrInterfaceDeclarationLA2)=> classOrInterfaceDeclaration
    |   statement
    ;

localVariableDeclarationStatement
    :   localVariableDeclaration
        ';'
    ;

localVariableDeclaration
    scope fieldDecScope;
    :   variableModifiers type
        variableDeclarator
        (',' variableDeclarator
        )*
    ;

statement
    :   block
    |   'assert'  expression (':' expression)? ';'
    |   'if' parExpression statement (('else')=> 'else' statement)?
    |   forstatement
    |   'while' parExpression statement
    |   'do' statement 'while' parExpression ';'
    |   trystatement
    |   'switch' parExpression '{' switchBlockStatementGroups '}'
    |   'synchronized' parExpression block
    |   'return' (expression )? ';'
    |   'throw' expression ';'
    |   'break'
            (IDENTIFIER
            )? ';'
    |   'continue'
            (IDENTIFIER
            )? ';'
    |   expression  ';'
    |   IDENTIFIER ':' statement
    |   ';'

    ;

switchBlockStatementGroups
    :   (switchBlockStatementGroup )*
    ;

switchBlockStatementGroup
    :
        switchLabel
        (blockStatement
        )*
    ;

switchLabel
    :   'case' expression ':'
    |   'default' ':'
    ;


trystatement
    :   'try' block  catches? ('finally' block)?
     ;

catches
    :   catchClause+
    ;
catchClause
    :   'catch' '(' formalParameter
        ')' block
    ;

formalParameter
    :   variableModifiers type IDENTIFIER
        ('[' ']'
        )*
    ;
forstatement
    :
        // enhanced for loop
        ('for' '(' variableModifiers type IDENTIFIER ':')=>
        'for' '(' variableModifiers type IDENTIFIER ':'
        expression ')' statement

        // normal for loop
    |   'for' '('
                (forInit
                )? ';'
                (expression
                )? ';'
                (expressionList
                )? ')' statement
    ;

forInit
    :   (variableModifiers type IDENTIFIER)=>localVariableDeclaration
    |   expressionList
    ;
qualifiedNameList
    :   q1=qualifiedName  {if(handler != null) handler.onTypeAssociated($q1.name);
                            }
        (',' q2=qualifiedName {if(handler != null) handler.onTypeAssociated($q2.name); }
        )*
    ;
formalParameters returns[List<String>paramNames]
    scope{
        List<String> paramNameList;
    }
    @init{
        $formalParameters::paramNameList = new ArrayList();
    }
    @after{
        $paramNames = $formalParameters::paramNameList;
    }
    :   '(' (formalParameterDecls )? ')'
    ;
parExpression
    :   '(' expression ')'
    ;
expressionList
    :   expression
        (',' expression
        )*
    ;
formalParameterDecls
    :   (variableModifiers type  '...')=> ellipsisParameterDecl
    |   normalParameterDecl ((',' variableModifiers type IDENTIFIER)=>',' normalParameterDecl )* (',' ellipsisParameterDecl)?
    //|   (normalParameterDecl ',' )+  ellipsisParameterDecl
    ;

normalParameterDecl
    @init{ StringBuilder typeSuffix = null;}
    @after{
        $formalParameters::paramNameList.add($id.text);
        if(agh != null && agh.isScope("params")){
            agh.addNode("paramName", $id.text, $id, $id);
            agh.addNode("paramType", $tp.text + (typeSuffix==null? "" : typeSuffix.toString()), $id, $id);
            
        }
    }
    :   variableModifiers tp=type id=IDENTIFIER ('[' ']' {
            if(typeSuffix == null) typeSuffix = new StringBuilder();
            typeSuffix.append("[]");} 
            )*
    ;

ellipsisParameterDecl
    @after{
        $formalParameters::paramNameList.add($id.text);
        if(agh != null && agh.isScope("params")){
            agh.addNode("paramName", $id.text, $id, $id);
            agh.addNode("paramType", $tp.text + "...", $id, $id);
        }
    }
    :   variableModifiers tp=type  '...'
        id=IDENTIFIER
    ;

packageDeclaration
    @init{
        if(agh != null) agh.onRuleStart("package");
    }
    @after{
        
        if(agh != null) agh.onRuleStop($start, $stop);
       
        if(handler != null)
            handler.onPackageName($q.name);
    }
    :   'package' q=qualifiedName
        ';'
    ;

importDeclaration
@after{
        if(handler != null){
            boolean isStatic = ($st != null);
            String qualifiedName = substring( ((CommonToken)$id).getStartIndex(),
                ((CommonToken)$importDeclaration.stop).getStopIndex()).trim();
            if(isStatic)
                handler.onStaticImport(qualifiedName);
            else
                handler.onImport(qualifiedName);
        }
        if(agh != null){
            if(st == null)
                agh.addNode("import", joinTokens(((CommonToken)$id).getTokenIndex(), ((CommonToken)$stop).getTokenIndex()-1), $start, $stop);
            else
                agh.addNode("import_static", joinTokens(((CommonToken)$id).getTokenIndex(), ((CommonToken)$stop).getTokenIndex()-1), $start, $stop);
        }
       }
    :  // 'import'  ('static' )? IDENTIFIER '.' '*' ';'
       'import' (st='static' )? id=IDENTIFIER (options{k=2;}: '.' IDENTIFIER )* ('.' '*' )? ';'

    ;

qualifiedImportName
    :   IDENTIFIER
        ('.' IDENTIFIER
        )*
    ;
qualifiedName returns[String name]
    @after{
        if(agh != null && agh.getNode() != null && 
        agh.getNode().getType().equals("package"))
            agh.setName(ruleText($start, $stop));
        $name = substring( ((CommonToken)$qualifiedName.start).getStartIndex(),
            ((CommonToken)$qualifiedName.stop).getStopIndex()+1);
    }
    :   IDENTIFIER
        ('.' IDENTIFIER
        )*
    ;
annotations
    :   (annotation )+
    ;

/**
 *  Using an annotation.
 * '@' is flaged in modifier
 */
annotation
    :   '@' qn=qualifiedName {if(handler != null) handler.onTypeAssociated($qn.name);}
        (   '('
                  (   elementValuePairs
                  |   elementValue
                  )?
            ')'
        )?
    ;
elementValuePairs
    :   elementValuePair
        (',' elementValuePair
        )*
    ;

elementValuePair
    :   IDENTIFIER '=' elementValue
    ;

elementValue
    :   conditionalExpression
    |   annotation
    |   elementValueArrayInitializer
    ;
elementValueArrayInitializer
    :   '{'
        (elementValue
            (options{k=2;}:',' elementValue
            )*
        )? (',')? '}'
    ;

conditionalExpression
    :   conditionalOrExpression
        ('?' expression ':' conditionalExpression
        )?
    ;
expression
    :   conditionalExpression
        (assignmentOperator expression
        )?
    ;
assignmentOperator
    options{k=3;}:   '='
    |   '+='
    |   '-='
    |   '*='
    |   '/='
    |   '&='
    |   '|='
    |   '^='
    |   '%='
    |    '<' '<' '='
    |    '>' '>' '>' '='
    |    '>' '>' '='
    ;

conditionalOrExpression
    :   conditionalAndExpression
        ('||' conditionalAndExpression
        )*
    ;

conditionalAndExpression
    :   inclusiveOrExpression
        ('&&' inclusiveOrExpression
        )*
    ;

inclusiveOrExpression
    :   exclusiveOrExpression
        ('|' exclusiveOrExpression
        )*
    ;

exclusiveOrExpression
    :   andExpression
        ('^' andExpression
        )*
    ;

andExpression
    :   equalityExpression
        ('&' equalityExpression
        )*
    ;

equalityExpression
    :   instanceOfExpression
        (
            (   '=='
            |   '!='
            )
            instanceOfExpression
        )*
    ;

instanceOfExpression
    :   relationalExpression
        ('instanceof' type
        )?
    ;

type returns[String text]
    @after{ 
        $text = joinTokens($start, $stop);
    }
    :   c=classOrInterfaceType ('[' ']' )*
    |   p=primitiveType ('[' ']' )*
    ;

classOrInterfaceType returns[String cleanText]
    @after{
        String typeName = substring( ((CommonToken)$classOrInterfaceType.start).getStartIndex(),
            ((CommonToken)$classOrInterfaceType.stop).getStopIndex()+1);
        if(handler != null){
            handler.onTypeAssociated($id);
        }
        $cleanText = joinTokens($start, $stop);
    }
    :   id+=IDENTIFIER ((typeArguments)=>typeArguments)?
        ('.' id+=IDENTIFIER ((typeArguments)=>typeArguments )?
        )*
    ;

primitiveType returns[String text]
    @after{ $text = $start.getText();}
    :   'boolean'
    |   'char'
    |   'byte'
    |   'short'
    |   'int'
    |   'long'
    |   'float'
    |   'double'
    ;

typeArguments
    :   '<' typeArgument
        (',' typeArgument
        )*
        '>'
    ;

typeArgument
    :   type
    |   '?'
        (
            ('extends'
            |'super'
            )
            type
        )?
    ;

relationalExpression
    :   shiftExpression
        (options{k=3;}:relationalOp shiftExpression )*
    ;

relationalOp
    options{k=2;}:    '<' '='
    |    '>' '='
    |   '<'
    |   '>'
    ;

shiftExpression
    :   additiveExpression
        ( (shiftOp additiveExpression) )*
    ;


shiftOp
    options{k=3;}:    '<' '<'
    |    '>' '>' '>'
    |    '>' '>'
    ;


additiveExpression
    :   multiplicativeExpression
        (
            (   '+'
            |   '-'
            )
            multiplicativeExpression
         )*
    ;

multiplicativeExpression
    :
        unaryExpression
        (
            (   '*'
            |   '/'
            |   '%'
            )
            unaryExpression
        )*
    ;

/**
 * NOTE: for '+' and '-', if the next token is int or long interal, then it's not a unary expression.
 *       it's a literal with signed value. INTLTERAL AND LONG LITERAL are added here for this.
 */
unaryExpression
    :   '+'  unaryExpression
    |   '-' unaryExpression
    |   '++' unaryExpression
    |   '--' unaryExpression
    |   unaryExpressionNotPlusMinus
    ;

unaryExpressionNotPlusMinus
    :   '~' unaryExpression
    |   '!' unaryExpression
    |   (castExpression_LA)=> castExpression
    |   primary (selector )* ('++' |'--' )?
    ;

castExpression
    :   '(' type ')' unaryExpression
    ;
castExpression_LA
    :   '(' type ')' ( '+'|'-'|'++'|'--'|'!'|'~'|'('|IDENTIFIER|'super'|'this'|literal|'new'|primitiveType)
    ;
/**
 * have to use scope here, parameter passing isn't well supported in antlr.
 */
primary
    :   parExpression
    |   IDENTIFIER
    |   'super'
    |   'this'
    |   literal
    |   creator
    |   primitiveType ('[' ']' )* '.' 'class'
    |   'void' '.' 'class'
    ;

selector
    :   '.' (typeArguments)? IDENTIFIER
    |   '.' ('this' | 'super' | 'class')
    |   innerCreator
    |   '[' expression? ']'
    |   arguments
    ;
arguments
    :   '(' (expressionList)? ')'
    ;

creator
    :  ('new' nonWildcardTypeArguments? classOrInterfaceType '(')=> 'new' nonWildcardTypeArguments? classOrInterfaceType classCreatorRest
    //|   'new' classOrInterfaceType classCreatorRest
    |   arrayCreator
    ;
innerCreator
    : '.' creator
    ;

classCreatorRest
    :   arguments (classBody)?
    ;
nonWildcardTypeArguments
    :   '<' typeList '>'
    ;
arrayCreator
    :   'new' (classOrInterfaceType| primitiveType) (('[')=>'[' expression? ']' )+
        arrayInitializer?
    ;

enumDeclaration
    @init{
        if(agh != null) agh.addNode("type","enum", $start, $start);
    }
    :   modifiers 'enum'
        name=IDENTIFIER {
                if(agh != null) agh.setName($name.text);
                if(handler != null)
                    handler.onTypeDeclaration($IDENTIFIER.text, classNestLevel, $IDENTIFIER.getLine());
             }
        ('implements' typeList )? enumBody
    ;


enumBody
    :   '{' (enumConstants )? ','? (enumBodyDeclarations )? '}'
    ;

enumConstants
    :   enumConstant (',' enumConstant )*
    ;

/**
 * NOTE: here differs from the javac grammar, missing TypeArguments.
 * EnumeratorDeclaration = AnnotationsOpt [TypeArguments] IDENTIFIER [ Arguments ] [ "{" ClassBody "}" ]
 */
enumConstant
    @after{
        if(agh != null) agh.addNode("field",$id.getText(), $start, $stop);
    }
    :   (annotations )? id=IDENTIFIER (arguments )? (classBody )?
        /* TODO: $GScope::name = names.empty. enum constant body is actually
        an anonymous class, where constructor isn't allowed, have to add this check*/
    ;

enumBodyDeclarations
    :   ';' (classBodyDeclaration )*
    ;


literal
    :   INTLITERAL
    |   LONGLITERAL
    |   FLOATLITERAL
    |   DOUBLELITERAL
    |   CHARLITERAL
    |   STRINGLITERAL
    |   TRUE
    |   FALSE
    |   NULL
    ;
//----------- Lexer ------------------
LONGLITERAL
    :   IntegerNumber LongSuffix
    ;


INTLITERAL
    :   IntegerNumber
    ;

fragment
IntegerNumber
    :   '0'
    |   '1'..'9' ('0'..'9')*
    |   '0' ('0'..'7')+
    |   HexPrefix HexDigit+
    ;

fragment
HexPrefix
    :   '0x' | '0X'
    ;

fragment
HexDigit
    :   ('0'..'9'|'a'..'f'|'A'..'F')
    ;

fragment
LongSuffix
    :   'l' | 'L'
    ;


fragment
NonIntegerNumber
    :   ('0' .. '9')+ '.' ('0' .. '9')* Exponent?
    |   '.' ( '0' .. '9' )+ Exponent?
    |   ('0' .. '9')+ Exponent
    |   ('0' .. '9')+
    |
        HexPrefix (HexDigit )*
        (    ()
        |    ('.' (HexDigit )* )
        )
        ( 'p' | 'P' )
        ( '+' | '-' )?
        ( '0' .. '9' )+
        ;

fragment
Exponent
    :   ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+
    ;

fragment
FloatSuffix
    :   'f' | 'F'
    ;

fragment
DoubleSuffix
    :   'd' | 'D'
    ;

FLOATLITERAL
    :   NonIntegerNumber FloatSuffix
    ;

DOUBLELITERAL
    :   NonIntegerNumber DoubleSuffix?
    ;

CHARLITERAL
    :   '\''
        (   EscapeSequence
        |   ~( '\'' | '\\' | '\r' | '\n' )
        )
        '\''
    ;

STRINGLITERAL
    :   '"'
        (   EscapeSequence
        |   ~( '\\' | '"' | '\r' | '\n' )
        )*
        '"'
    ;

fragment
EscapeSequence
    :   '\\' (
                 'b'
             |   't'
             |   'n'
             |   'f'
             |   'r'
             |   'u'
             |   '\"'
             |   '\''
             |   '\\'
             |
                 ('0'..'3') ('0'..'7') ('0'..'7')
             |
                 ('0'..'7') ('0'..'7')
             |
                 ('0'..'7')
             )
;
WS
    :   (
             ' '
        |    '\r'
        |    '\t'
        |    '\u000C'
        |    '\n'
        )
            {
                skip();
            }
    ;

COMMENT
         @init{
            boolean isJavaDoc = false;
        }
    :   '/*'
            {
                if((char)input.LA(1) == '*'){
                    isJavaDoc = true;
                }
            }
        (options {greedy=false;} : . )*
        '*/'
            {
                if(isJavaDoc==true){
                    //skip();
                    $channel=HIDDEN;
                }else{
                    skip();
                }
            }
    ;

LINE_COMMENT
    :   '//' ~('\n'|'\r')*  ('\r\n' | '\r' | '\n')
            {
                skip();
            }
    |   '//' ~('\n'|'\r')*     // a line comment could appear at the end of the file without CR/LF
            {
                skip();
            }
    ;

ABSTRACT
    :   'abstract'
    ;

ASSERT
    :   'assert'
    ;

BOOLEAN
    :   'boolean'
    ;

BREAK
    :   'break'
    ;

BYTE
    :   'byte'
    ;

CASE
    :   'case'
    ;

CATCH
    :   'catch'
    ;

CHAR
    :   'char'
    ;

CLASS
    :   'class'
    ;

CONST
    :   'const'
    ;

CONTINUE
    :   'continue'
    ;

DEFAULT
    :   'default'
    ;

DO
    :   'do'
    ;

DOUBLE
    :   'double'
    ;

ELSE
    :   'else'
    ;

ENUM
    :   'enum'
    ;

EXTENDS
    :   'extends'
    ;

FINAL
    :   'final'
    ;

FINALLY
    :   'finally'
    ;

FLOAT
    :   'float'
    ;

FOR
    :   'for'
    ;

GOTO
    :   'goto'
    ;

IF
    :   'if'
    ;

IMPLEMENTS
    :   'implements'
    ;

IMPORT
    :   'import'
    ;

INSTANCEOF
    :   'instanceof'
    ;

INT
    :   'int'
    ;

INTERFACE
    :   'interface'
    ;

LONG
    :   'long'
    ;

NATIVE
    :   'native'
    ;

NEW
    :   'new'
    ;

PACKAGE
    :   'package'
    ;

PRIVATE
    :   'private'
    ;

PROTECTED
    :   'protected'
    ;

PUBLIC
    :   'public'
    ;

RETURN
    :   'return'
    ;

SHORT
    :   'short'
    ;

STATIC
    :   'static'
    ;

STRICTFP
    :   'strictfp'
    ;

SUPER
    :   'super'
    ;

SWITCH
    :   'switch'
    ;

SYNCHRONIZED
    :   'synchronized'
    ;

THIS
    :   'this'
    ;

THROW
    :   'throw'
    ;

THROWS
    :   'throws'
    ;

TRANSIENT
    :   'transient'
    ;

TRY
    :   'try'
    ;

VOID
    :   'void'
    ;

VOLATILE
    :   'volatile'
    ;

WHILE
    :   'while'
    ;

TRUE
    :   'true'
    ;

FALSE
    :   'false'
    ;

NULL
    :   'null'
    ;

LPAREN
    :   '('
    ;

RPAREN
    :   ')'
    ;

LBRACE
    :   '{'
    ;

RBRACE
    :   '}'
    ;

LBRACKET
    :   '['
    ;

RBRACKET
    :   ']'
    ;

SEMI
    :   ';'
    ;

COMMA
    :   ','
    ;

DOT
    :   '.'
    ;

ELLIPSIS
    :   '...'
    ;

EQ
    :   '='
    ;

BANG
    :   '!'
    ;

TILDE
    :   '~'
    ;

QUES
    :   '?'
    ;

COLON
    :   ':'
    ;

EQEQ
    :   '=='
    ;

AMPAMP
    :   '&&'
    ;

BARBAR
    :   '||'
    ;

PLUSPLUS
    :   '++'
    ;

SUBSUB
    :   '--'
    ;

PLUS
    :   '+'
    ;

SUB
    :   '-'
    ;

STAR
    :   '*'
    ;

SLASH
    :   '/'
    ;

AMP
    :   '&'
    ;

BAR
    :   '|'
    ;

CARET
    :   '^'
    ;

PERCENT
    :   '%'
    ;

PLUSEQ
    :   '+='
    ;

SUBEQ
    :   '-='
    ;

STAREQ
    :   '*='
    ;

SLASHEQ
    :   '/='
    ;

AMPEQ
    :   '&='
    ;

BAREQ
    :   '|='
    ;

CARETEQ
    :   '^='
    ;

PERCENTEQ
    :   '%='
    ;

MONKEYS_AT
    :   '@'
    ;

BANGEQ
    :   '!='
    ;

GT
    :   '>'
    ;

LT
    :   '<'
    ;

IDENTIFIER
    :   IdentifierStart IdentifierPart*
    ;

fragment
SurrogateIdentifer
    :   ('\ud800'..'\udbff') ('\udc00'..'\udfff')
    ;

fragment
IdentifierStart
    :   '\u0024'
    |   '\u0041'..'\u005a'
    |   '\u005f'
    |   '\u0061'..'\u007a'
    |   '\u00a2'..'\u00a5'
    |   '\u00aa'
    |   '\u00b5'
    |   '\u00ba'
    |   '\u00c0'..'\u00d6'
    |   '\u00d8'..'\u00f6'
    |   '\u00f8'..'\u0236'
    |   '\u0250'..'\u02c1'
    |   '\u02c6'..'\u02d1'
    |   '\u02e0'..'\u02e4'
    |   '\u02ee'
    |   '\u037a'
    |   '\u0386'
    |   '\u0388'..'\u038a'
    |   '\u038c'
    |   '\u038e'..'\u03a1'
    |   '\u03a3'..'\u03ce'
    |   '\u03d0'..'\u03f5'
    |   '\u03f7'..'\u03fb'
    |   '\u0400'..'\u0481'
    |   '\u048a'..'\u04ce'
    |   '\u04d0'..'\u04f5'
    |   '\u04f8'..'\u04f9'
    |   '\u0500'..'\u050f'
    |   '\u0531'..'\u0556'
    |   '\u0559'
    |   '\u0561'..'\u0587'
    |   '\u05d0'..'\u05ea'
    |   '\u05f0'..'\u05f2'
    |   '\u0621'..'\u063a'
    |   '\u0640'..'\u064a'
    |   '\u066e'..'\u066f'
    |   '\u0671'..'\u06d3'
    |   '\u06d5'
    |   '\u06e5'..'\u06e6'
    |   '\u06ee'..'\u06ef'
    |   '\u06fa'..'\u06fc'
    |   '\u06ff'
    |   '\u0710'
    |   '\u0712'..'\u072f'
    |   '\u074d'..'\u074f'
    |   '\u0780'..'\u07a5'
    |   '\u07b1'
    |   '\u0904'..'\u0939'
    |   '\u093d'
    |   '\u0950'
    |   '\u0958'..'\u0961'
    |   '\u0985'..'\u098c'
    |   '\u098f'..'\u0990'
    |   '\u0993'..'\u09a8'
    |   '\u09aa'..'\u09b0'
    |   '\u09b2'
    |   '\u09b6'..'\u09b9'
    |   '\u09bd'
    |   '\u09dc'..'\u09dd'
    |   '\u09df'..'\u09e1'
    |   '\u09f0'..'\u09f3'
    |   '\u0a05'..'\u0a0a'
    |   '\u0a0f'..'\u0a10'
    |   '\u0a13'..'\u0a28'
    |   '\u0a2a'..'\u0a30'
    |   '\u0a32'..'\u0a33'
    |   '\u0a35'..'\u0a36'
    |   '\u0a38'..'\u0a39'
    |   '\u0a59'..'\u0a5c'
    |   '\u0a5e'
    |   '\u0a72'..'\u0a74'
    |   '\u0a85'..'\u0a8d'
    |   '\u0a8f'..'\u0a91'
    |   '\u0a93'..'\u0aa8'
    |   '\u0aaa'..'\u0ab0'
    |   '\u0ab2'..'\u0ab3'
    |   '\u0ab5'..'\u0ab9'
    |   '\u0abd'
    |   '\u0ad0'
    |   '\u0ae0'..'\u0ae1'
    |   '\u0af1'
    |   '\u0b05'..'\u0b0c'
    |   '\u0b0f'..'\u0b10'
    |   '\u0b13'..'\u0b28'
    |   '\u0b2a'..'\u0b30'
    |   '\u0b32'..'\u0b33'
    |   '\u0b35'..'\u0b39'
    |   '\u0b3d'
    |   '\u0b5c'..'\u0b5d'
    |   '\u0b5f'..'\u0b61'
    |   '\u0b71'
    |   '\u0b83'
    |   '\u0b85'..'\u0b8a'
    |   '\u0b8e'..'\u0b90'
    |   '\u0b92'..'\u0b95'
    |   '\u0b99'..'\u0b9a'
    |   '\u0b9c'
    |   '\u0b9e'..'\u0b9f'
    |   '\u0ba3'..'\u0ba4'
    |   '\u0ba8'..'\u0baa'
    |   '\u0bae'..'\u0bb5'
    |   '\u0bb7'..'\u0bb9'
    |   '\u0bf9'
    |   '\u0c05'..'\u0c0c'
    |   '\u0c0e'..'\u0c10'
    |   '\u0c12'..'\u0c28'
    |   '\u0c2a'..'\u0c33'
    |   '\u0c35'..'\u0c39'
    |   '\u0c60'..'\u0c61'
    |   '\u0c85'..'\u0c8c'
    |   '\u0c8e'..'\u0c90'
    |   '\u0c92'..'\u0ca8'
    |   '\u0caa'..'\u0cb3'
    |   '\u0cb5'..'\u0cb9'
    |   '\u0cbd'
    |   '\u0cde'
    |   '\u0ce0'..'\u0ce1'
    |   '\u0d05'..'\u0d0c'
    |   '\u0d0e'..'\u0d10'
    |   '\u0d12'..'\u0d28'
    |   '\u0d2a'..'\u0d39'
    |   '\u0d60'..'\u0d61'
    |   '\u0d85'..'\u0d96'
    |   '\u0d9a'..'\u0db1'
    |   '\u0db3'..'\u0dbb'
    |   '\u0dbd'
    |   '\u0dc0'..'\u0dc6'
    |   '\u0e01'..'\u0e30'
    |   '\u0e32'..'\u0e33'
    |   '\u0e3f'..'\u0e46'
    |   '\u0e81'..'\u0e82'
    |   '\u0e84'
    |   '\u0e87'..'\u0e88'
    |   '\u0e8a'
    |   '\u0e8d'
    |   '\u0e94'..'\u0e97'
    |   '\u0e99'..'\u0e9f'
    |   '\u0ea1'..'\u0ea3'
    |   '\u0ea5'
    |   '\u0ea7'
    |   '\u0eaa'..'\u0eab'
    |   '\u0ead'..'\u0eb0'
    |   '\u0eb2'..'\u0eb3'
    |   '\u0ebd'
    |   '\u0ec0'..'\u0ec4'
    |   '\u0ec6'
    |   '\u0edc'..'\u0edd'
    |   '\u0f00'
    |   '\u0f40'..'\u0f47'
    |   '\u0f49'..'\u0f6a'
    |   '\u0f88'..'\u0f8b'
    |   '\u1000'..'\u1021'
    |   '\u1023'..'\u1027'
    |   '\u1029'..'\u102a'
    |   '\u1050'..'\u1055'
    |   '\u10a0'..'\u10c5'
    |   '\u10d0'..'\u10f8'
    |   '\u1100'..'\u1159'
    |   '\u115f'..'\u11a2'
    |   '\u11a8'..'\u11f9'
    |   '\u1200'..'\u1206'
    |   '\u1208'..'\u1246'
    |   '\u1248'
    |   '\u124a'..'\u124d'
    |   '\u1250'..'\u1256'
    |   '\u1258'
    |   '\u125a'..'\u125d'
    |   '\u1260'..'\u1286'
    |   '\u1288'
    |   '\u128a'..'\u128d'
    |   '\u1290'..'\u12ae'
    |   '\u12b0'
    |   '\u12b2'..'\u12b5'
    |   '\u12b8'..'\u12be'
    |   '\u12c0'
    |   '\u12c2'..'\u12c5'
    |   '\u12c8'..'\u12ce'
    |   '\u12d0'..'\u12d6'
    |   '\u12d8'..'\u12ee'
    |   '\u12f0'..'\u130e'
    |   '\u1310'
    |   '\u1312'..'\u1315'
    |   '\u1318'..'\u131e'
    |   '\u1320'..'\u1346'
    |   '\u1348'..'\u135a'
    |   '\u13a0'..'\u13f4'
    |   '\u1401'..'\u166c'
    |   '\u166f'..'\u1676'
    |   '\u1681'..'\u169a'
    |   '\u16a0'..'\u16ea'
    |   '\u16ee'..'\u16f0'
    |   '\u1700'..'\u170c'
    |   '\u170e'..'\u1711'
    |   '\u1720'..'\u1731'
    |   '\u1740'..'\u1751'
    |   '\u1760'..'\u176c'
    |   '\u176e'..'\u1770'
    |   '\u1780'..'\u17b3'
    |   '\u17d7'
    |   '\u17db'..'\u17dc'
    |   '\u1820'..'\u1877'
    |   '\u1880'..'\u18a8'
    |   '\u1900'..'\u191c'
    |   '\u1950'..'\u196d'
    |   '\u1970'..'\u1974'
    |   '\u1d00'..'\u1d6b'
    |   '\u1e00'..'\u1e9b'
    |   '\u1ea0'..'\u1ef9'
    |   '\u1f00'..'\u1f15'
    |   '\u1f18'..'\u1f1d'
    |   '\u1f20'..'\u1f45'
    |   '\u1f48'..'\u1f4d'
    |   '\u1f50'..'\u1f57'
    |   '\u1f59'
    |   '\u1f5b'
    |   '\u1f5d'
    |   '\u1f5f'..'\u1f7d'
    |   '\u1f80'..'\u1fb4'
    |   '\u1fb6'..'\u1fbc'
    |   '\u1fbe'
    |   '\u1fc2'..'\u1fc4'
    |   '\u1fc6'..'\u1fcc'
    |   '\u1fd0'..'\u1fd3'
    |   '\u1fd6'..'\u1fdb'
    |   '\u1fe0'..'\u1fec'
    |   '\u1ff2'..'\u1ff4'
    |   '\u1ff6'..'\u1ffc'
    |   '\u203f'..'\u2040'
    |   '\u2054'
    |   '\u2071'
    |   '\u207f'
    |   '\u20a0'..'\u20b1'
    |   '\u2102'
    |   '\u2107'
    |   '\u210a'..'\u2113'
    |   '\u2115'
    |   '\u2119'..'\u211d'
    |   '\u2124'
    |   '\u2126'
    |   '\u2128'
    |   '\u212a'..'\u212d'
    |   '\u212f'..'\u2131'
    |   '\u2133'..'\u2139'
    |   '\u213d'..'\u213f'
    |   '\u2145'..'\u2149'
    |   '\u2160'..'\u2183'
    |   '\u3005'..'\u3007'
    |   '\u3021'..'\u3029'
    |   '\u3031'..'\u3035'
    |   '\u3038'..'\u303c'
    |   '\u3041'..'\u3096'
    |   '\u309d'..'\u309f'
    |   '\u30a1'..'\u30ff'
    |   '\u3105'..'\u312c'
    |   '\u3131'..'\u318e'
    |   '\u31a0'..'\u31b7'
    |   '\u31f0'..'\u31ff'
    |   '\u3400'..'\u4db5'
    |   '\u4e00'..'\u9fa5'
    |   '\ua000'..'\ua48c'
    |   '\uac00'..'\ud7a3'
    |   '\uf900'..'\ufa2d'
    |   '\ufa30'..'\ufa6a'
    |   '\ufb00'..'\ufb06'
    |   '\ufb13'..'\ufb17'
    |   '\ufb1d'
    |   '\ufb1f'..'\ufb28'
    |   '\ufb2a'..'\ufb36'
    |   '\ufb38'..'\ufb3c'
    |   '\ufb3e'
    |   '\ufb40'..'\ufb41'
    |   '\ufb43'..'\ufb44'
    |   '\ufb46'..'\ufbb1'
    |   '\ufbd3'..'\ufd3d'
    |   '\ufd50'..'\ufd8f'
    |   '\ufd92'..'\ufdc7'
    |   '\ufdf0'..'\ufdfc'
    |   '\ufe33'..'\ufe34'
    |   '\ufe4d'..'\ufe4f'
    |   '\ufe69'
    |   '\ufe70'..'\ufe74'
    |   '\ufe76'..'\ufefc'
    |   '\uff04'
    |   '\uff21'..'\uff3a'
    |   '\uff3f'
    |   '\uff41'..'\uff5a'
    |   '\uff65'..'\uffbe'
    |   '\uffc2'..'\uffc7'
    |   '\uffca'..'\uffcf'
    |   '\uffd2'..'\uffd7'
    |   '\uffda'..'\uffdc'
    |   '\uffe0'..'\uffe1'
    |   '\uffe5'..'\uffe6'
    |   ('\ud800'..'\udbff') ('\udc00'..'\udfff')
    ;

fragment
IdentifierPart
    :   '\u0000'..'\u0008'
    |   '\u000e'..'\u001b'
    |   '\u0024'
    |   '\u0030'..'\u0039'
    |   '\u0041'..'\u005a'
    |   '\u005f'
    |   '\u0061'..'\u007a'
    |   '\u007f'..'\u009f'
    |   '\u00a2'..'\u00a5'
    |   '\u00aa'
    |   '\u00ad'
    |   '\u00b5'
    |   '\u00ba'
    |   '\u00c0'..'\u00d6'
    |   '\u00d8'..'\u00f6'
    |   '\u00f8'..'\u0236'
    |   '\u0250'..'\u02c1'
    |   '\u02c6'..'\u02d1'
    |   '\u02e0'..'\u02e4'
    |   '\u02ee'
    |   '\u0300'..'\u0357'
    |   '\u035d'..'\u036f'
    |   '\u037a'
    |   '\u0386'
    |   '\u0388'..'\u038a'
    |   '\u038c'
    |   '\u038e'..'\u03a1'
    |   '\u03a3'..'\u03ce'
    |   '\u03d0'..'\u03f5'
    |   '\u03f7'..'\u03fb'
    |   '\u0400'..'\u0481'
    |   '\u0483'..'\u0486'
    |   '\u048a'..'\u04ce'
    |   '\u04d0'..'\u04f5'
    |   '\u04f8'..'\u04f9'
    |   '\u0500'..'\u050f'
    |   '\u0531'..'\u0556'
    |   '\u0559'
    |   '\u0561'..'\u0587'
    |   '\u0591'..'\u05a1'
    |   '\u05a3'..'\u05b9'
    |   '\u05bb'..'\u05bd'
    |   '\u05bf'
    |   '\u05c1'..'\u05c2'
    |   '\u05c4'
    |   '\u05d0'..'\u05ea'
    |   '\u05f0'..'\u05f2'
    |   '\u0600'..'\u0603'
    |   '\u0610'..'\u0615'
    |   '\u0621'..'\u063a'
    |   '\u0640'..'\u0658'
    |   '\u0660'..'\u0669'
    |   '\u066e'..'\u06d3'
    |   '\u06d5'..'\u06dd'
    |   '\u06df'..'\u06e8'
    |   '\u06ea'..'\u06fc'
    |   '\u06ff'
    |   '\u070f'..'\u074a'
    |   '\u074d'..'\u074f'
    |   '\u0780'..'\u07b1'
    |   '\u0901'..'\u0939'
    |   '\u093c'..'\u094d'
    |   '\u0950'..'\u0954'
    |   '\u0958'..'\u0963'
    |   '\u0966'..'\u096f'
    |   '\u0981'..'\u0983'
    |   '\u0985'..'\u098c'
    |   '\u098f'..'\u0990'
    |   '\u0993'..'\u09a8'
    |   '\u09aa'..'\u09b0'
    |   '\u09b2'
    |   '\u09b6'..'\u09b9'
    |   '\u09bc'..'\u09c4'
    |   '\u09c7'..'\u09c8'
    |   '\u09cb'..'\u09cd'
    |   '\u09d7'
    |   '\u09dc'..'\u09dd'
    |   '\u09df'..'\u09e3'
    |   '\u09e6'..'\u09f3'
    |   '\u0a01'..'\u0a03'
    |   '\u0a05'..'\u0a0a'
    |   '\u0a0f'..'\u0a10'
    |   '\u0a13'..'\u0a28'
    |   '\u0a2a'..'\u0a30'
    |   '\u0a32'..'\u0a33'
    |   '\u0a35'..'\u0a36'
    |   '\u0a38'..'\u0a39'
    |   '\u0a3c'
    |   '\u0a3e'..'\u0a42'
    |   '\u0a47'..'\u0a48'
    |   '\u0a4b'..'\u0a4d'
    |   '\u0a59'..'\u0a5c'
    |   '\u0a5e'
    |   '\u0a66'..'\u0a74'
    |   '\u0a81'..'\u0a83'
    |   '\u0a85'..'\u0a8d'
    |   '\u0a8f'..'\u0a91'
    |   '\u0a93'..'\u0aa8'
    |   '\u0aaa'..'\u0ab0'
    |   '\u0ab2'..'\u0ab3'
    |   '\u0ab5'..'\u0ab9'
    |   '\u0abc'..'\u0ac5'
    |   '\u0ac7'..'\u0ac9'
    |   '\u0acb'..'\u0acd'
    |   '\u0ad0'
    |   '\u0ae0'..'\u0ae3'
    |   '\u0ae6'..'\u0aef'
    |   '\u0af1'
    |   '\u0b01'..'\u0b03'
    |   '\u0b05'..'\u0b0c'
    |   '\u0b0f'..'\u0b10'
    |   '\u0b13'..'\u0b28'
    |   '\u0b2a'..'\u0b30'
    |   '\u0b32'..'\u0b33'
    |   '\u0b35'..'\u0b39'
    |   '\u0b3c'..'\u0b43'
    |   '\u0b47'..'\u0b48'
    |   '\u0b4b'..'\u0b4d'
    |   '\u0b56'..'\u0b57'
    |   '\u0b5c'..'\u0b5d'
    |   '\u0b5f'..'\u0b61'
    |   '\u0b66'..'\u0b6f'
    |   '\u0b71'
    |   '\u0b82'..'\u0b83'
    |   '\u0b85'..'\u0b8a'
    |   '\u0b8e'..'\u0b90'
    |   '\u0b92'..'\u0b95'
    |   '\u0b99'..'\u0b9a'
    |   '\u0b9c'
    |   '\u0b9e'..'\u0b9f'
    |   '\u0ba3'..'\u0ba4'
    |   '\u0ba8'..'\u0baa'
    |   '\u0bae'..'\u0bb5'
    |   '\u0bb7'..'\u0bb9'
    |   '\u0bbe'..'\u0bc2'
    |   '\u0bc6'..'\u0bc8'
    |   '\u0bca'..'\u0bcd'
    |   '\u0bd7'
    |   '\u0be7'..'\u0bef'
    |   '\u0bf9'
    |   '\u0c01'..'\u0c03'
    |   '\u0c05'..'\u0c0c'
    |   '\u0c0e'..'\u0c10'
    |   '\u0c12'..'\u0c28'
    |   '\u0c2a'..'\u0c33'
    |   '\u0c35'..'\u0c39'
    |   '\u0c3e'..'\u0c44'
    |   '\u0c46'..'\u0c48'
    |   '\u0c4a'..'\u0c4d'
    |   '\u0c55'..'\u0c56'
    |   '\u0c60'..'\u0c61'
    |   '\u0c66'..'\u0c6f'
    |   '\u0c82'..'\u0c83'
    |   '\u0c85'..'\u0c8c'
    |   '\u0c8e'..'\u0c90'
    |   '\u0c92'..'\u0ca8'
    |   '\u0caa'..'\u0cb3'
    |   '\u0cb5'..'\u0cb9'
    |   '\u0cbc'..'\u0cc4'
    |   '\u0cc6'..'\u0cc8'
    |   '\u0cca'..'\u0ccd'
    |   '\u0cd5'..'\u0cd6'
    |   '\u0cde'
    |   '\u0ce0'..'\u0ce1'
    |   '\u0ce6'..'\u0cef'
    |   '\u0d02'..'\u0d03'
    |   '\u0d05'..'\u0d0c'
    |   '\u0d0e'..'\u0d10'
    |   '\u0d12'..'\u0d28'
    |   '\u0d2a'..'\u0d39'
    |   '\u0d3e'..'\u0d43'
    |   '\u0d46'..'\u0d48'
    |   '\u0d4a'..'\u0d4d'
    |   '\u0d57'
    |   '\u0d60'..'\u0d61'
    |   '\u0d66'..'\u0d6f'
    |   '\u0d82'..'\u0d83'
    |   '\u0d85'..'\u0d96'
    |   '\u0d9a'..'\u0db1'
    |   '\u0db3'..'\u0dbb'
    |   '\u0dbd'
    |   '\u0dc0'..'\u0dc6'
    |   '\u0dca'
    |   '\u0dcf'..'\u0dd4'
    |   '\u0dd6'
    |   '\u0dd8'..'\u0ddf'
    |   '\u0df2'..'\u0df3'
    |   '\u0e01'..'\u0e3a'
    |   '\u0e3f'..'\u0e4e'
    |   '\u0e50'..'\u0e59'
    |   '\u0e81'..'\u0e82'
    |   '\u0e84'
    |   '\u0e87'..'\u0e88'
    |   '\u0e8a'
    |   '\u0e8d'
    |   '\u0e94'..'\u0e97'
    |   '\u0e99'..'\u0e9f'
    |   '\u0ea1'..'\u0ea3'
    |   '\u0ea5'
    |   '\u0ea7'
    |   '\u0eaa'..'\u0eab'
    |   '\u0ead'..'\u0eb9'
    |   '\u0ebb'..'\u0ebd'
    |   '\u0ec0'..'\u0ec4'
    |   '\u0ec6'
    |   '\u0ec8'..'\u0ecd'
    |   '\u0ed0'..'\u0ed9'
    |   '\u0edc'..'\u0edd'
    |   '\u0f00'
    |   '\u0f18'..'\u0f19'
    |   '\u0f20'..'\u0f29'
    |   '\u0f35'
    |   '\u0f37'
    |   '\u0f39'
    |   '\u0f3e'..'\u0f47'
    |   '\u0f49'..'\u0f6a'
    |   '\u0f71'..'\u0f84'
    |   '\u0f86'..'\u0f8b'
    |   '\u0f90'..'\u0f97'
    |   '\u0f99'..'\u0fbc'
    |   '\u0fc6'
    |   '\u1000'..'\u1021'
    |   '\u1023'..'\u1027'
    |   '\u1029'..'\u102a'
    |   '\u102c'..'\u1032'
    |   '\u1036'..'\u1039'
    |   '\u1040'..'\u1049'
    |   '\u1050'..'\u1059'
    |   '\u10a0'..'\u10c5'
    |   '\u10d0'..'\u10f8'
    |   '\u1100'..'\u1159'
    |   '\u115f'..'\u11a2'
    |   '\u11a8'..'\u11f9'
    |   '\u1200'..'\u1206'
    |   '\u1208'..'\u1246'
    |   '\u1248'
    |   '\u124a'..'\u124d'
    |   '\u1250'..'\u1256'
    |   '\u1258'
    |   '\u125a'..'\u125d'
    |   '\u1260'..'\u1286'
    |   '\u1288'
    |   '\u128a'..'\u128d'
    |   '\u1290'..'\u12ae'
    |   '\u12b0'
    |   '\u12b2'..'\u12b5'
    |   '\u12b8'..'\u12be'
    |   '\u12c0'
    |   '\u12c2'..'\u12c5'
    |   '\u12c8'..'\u12ce'
    |   '\u12d0'..'\u12d6'
    |   '\u12d8'..'\u12ee'
    |   '\u12f0'..'\u130e'
    |   '\u1310'
    |   '\u1312'..'\u1315'
    |   '\u1318'..'\u131e'
    |   '\u1320'..'\u1346'
    |   '\u1348'..'\u135a'
    |   '\u1369'..'\u1371'
    |   '\u13a0'..'\u13f4'
    |   '\u1401'..'\u166c'
    |   '\u166f'..'\u1676'
    |   '\u1681'..'\u169a'
    |   '\u16a0'..'\u16ea'
    |   '\u16ee'..'\u16f0'
    |   '\u1700'..'\u170c'
    |   '\u170e'..'\u1714'
    |   '\u1720'..'\u1734'
    |   '\u1740'..'\u1753'
    |   '\u1760'..'\u176c'
    |   '\u176e'..'\u1770'
    |   '\u1772'..'\u1773'
    |   '\u1780'..'\u17d3'
    |   '\u17d7'
    |   '\u17db'..'\u17dd'
    |   '\u17e0'..'\u17e9'
    |   '\u180b'..'\u180d'
    |   '\u1810'..'\u1819'
    |   '\u1820'..'\u1877'
    |   '\u1880'..'\u18a9'
    |   '\u1900'..'\u191c'
    |   '\u1920'..'\u192b'
    |   '\u1930'..'\u193b'
    |   '\u1946'..'\u196d'
    |   '\u1970'..'\u1974'
    |   '\u1d00'..'\u1d6b'
    |   '\u1e00'..'\u1e9b'
    |   '\u1ea0'..'\u1ef9'
    |   '\u1f00'..'\u1f15'
    |   '\u1f18'..'\u1f1d'
    |   '\u1f20'..'\u1f45'
    |   '\u1f48'..'\u1f4d'
    |   '\u1f50'..'\u1f57'
    |   '\u1f59'
    |   '\u1f5b'
    |   '\u1f5d'
    |   '\u1f5f'..'\u1f7d'
    |   '\u1f80'..'\u1fb4'
    |   '\u1fb6'..'\u1fbc'
    |   '\u1fbe'
    |   '\u1fc2'..'\u1fc4'
    |   '\u1fc6'..'\u1fcc'
    |   '\u1fd0'..'\u1fd3'
    |   '\u1fd6'..'\u1fdb'
    |   '\u1fe0'..'\u1fec'
    |   '\u1ff2'..'\u1ff4'
    |   '\u1ff6'..'\u1ffc'
    |   '\u200c'..'\u200f'
    |   '\u202a'..'\u202e'
    |   '\u203f'..'\u2040'
    |   '\u2054'
    |   '\u2060'..'\u2063'
    |   '\u206a'..'\u206f'
    |   '\u2071'
    |   '\u207f'
    |   '\u20a0'..'\u20b1'
    |   '\u20d0'..'\u20dc'
    |   '\u20e1'
    |   '\u20e5'..'\u20ea'
    |   '\u2102'
    |   '\u2107'
    |   '\u210a'..'\u2113'
    |   '\u2115'
    |   '\u2119'..'\u211d'
    |   '\u2124'
    |   '\u2126'
    |   '\u2128'
    |   '\u212a'..'\u212d'
    |   '\u212f'..'\u2131'
    |   '\u2133'..'\u2139'
    |   '\u213d'..'\u213f'
    |   '\u2145'..'\u2149'
    |   '\u2160'..'\u2183'
    |   '\u3005'..'\u3007'
    |   '\u3021'..'\u302f'
    |   '\u3031'..'\u3035'
    |   '\u3038'..'\u303c'
    |   '\u3041'..'\u3096'
    |   '\u3099'..'\u309a'
    |   '\u309d'..'\u309f'
    |   '\u30a1'..'\u30ff'
    |   '\u3105'..'\u312c'
    |   '\u3131'..'\u318e'
    |   '\u31a0'..'\u31b7'
    |   '\u31f0'..'\u31ff'
    |   '\u3400'..'\u4db5'
    |   '\u4e00'..'\u9fa5'
    |   '\ua000'..'\ua48c'
    |   '\uac00'..'\ud7a3'
    |   '\uf900'..'\ufa2d'
    |   '\ufa30'..'\ufa6a'
    |   '\ufb00'..'\ufb06'
    |   '\ufb13'..'\ufb17'
    |   '\ufb1d'..'\ufb28'
    |   '\ufb2a'..'\ufb36'
    |   '\ufb38'..'\ufb3c'
    |   '\ufb3e'
    |   '\ufb40'..'\ufb41'
    |   '\ufb43'..'\ufb44'
    |   '\ufb46'..'\ufbb1'
    |   '\ufbd3'..'\ufd3d'
    |   '\ufd50'..'\ufd8f'
    |   '\ufd92'..'\ufdc7'
    |   '\ufdf0'..'\ufdfc'
    |   '\ufe00'..'\ufe0f'
    |   '\ufe20'..'\ufe23'
    |   '\ufe33'..'\ufe34'
    |   '\ufe4d'..'\ufe4f'
    |   '\ufe69'
    |   '\ufe70'..'\ufe74'
    |   '\ufe76'..'\ufefc'
    |   '\ufeff'
    |   '\uff04'
    |   '\uff10'..'\uff19'
    |   '\uff21'..'\uff3a'
    |   '\uff3f'
    |   '\uff41'..'\uff5a'
    |   '\uff65'..'\uffbe'
    |   '\uffc2'..'\uffc7'
    |   '\uffca'..'\uffcf'
    |   '\uffd2'..'\uffd7'
    |   '\uffda'..'\uffdc'
    |   '\uffe0'..'\uffe1'
    |   '\uffe5'..'\uffe6'
    |   '\ufff9'..'\ufffb'
    |   ('\ud800'..'\udbff') ('\udc00'..'\udfff')
    ;