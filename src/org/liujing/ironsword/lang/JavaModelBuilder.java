package org.liujing.ironsword.lang;

import java.io.*;
import java.util.*;
import org.antlr.runtime.*;
import org.liujing.parser.*;
import org.liujing.ironsword.grammar.*;
import liujing.jedit.parser.*;
import org.liujing.ironsword.*;

public class JavaModelBuilder{
    
    public JavaModelBuilder(){}
    
    public List<CommonLanguageModel> build(Reader in){
        
        return null;
    }
    
    public String recognizePackage(File f){
        try{
        JavaLexer lexer = new JavaLexer(new ANTLRFileStream(f.getPath()));
        JavaParser parser = new JavaParser(new RemovableTokenStream(lexer));
        
        AntlrGrammarHandler h = new AntlrGrammarHandler();
        parser.setAntlrGrammarHandler(h);
        parser.onlyParsePackageDef();
        String packagename = "";
        if(h.getNode() == null)
            return "";
        for(GrammarNode n: h.getNode().getChildren()){
            if(n.getType().equals("package")){
                packagename = n.getName();
                break;
            }
        }
        return packagename;
        }catch(Exception e){
            throw new IronException("failed to parse source file: "+ f.getPath(), e);
        }
    }
    
    public CommonLanguageModel build(File f){
        try{
            JavaLexer lexer = new JavaLexer(new ANTLRFileStream(f.getPath()));
            JavaParser parser = new JavaParser(new RemovableTokenStream(lexer));
            
            AntlrGrammarHandler h = new AntlrGrammarHandler();
            parser.setAntlrGrammarHandler(h);
            parser.compilationUnit(null);
            h.setName(f.getName());
            return parseRootNode(h.getNode());
        }catch(Exception e){
            throw new IronException("failed to parse source file", e);
        }
    }
    
    protected CommonLanguageModel parseRootNode(GrammarNode node){
        CommonLanguageModel model = new CommonLanguageModel(node.getName());
        for(GrammarNode n: node.getChildren()){
            if(n.getType().equals("package"))
                model.setPackageName(n.getName());
            else if("import".equals(n.getType())){
                model.addImport(n.getName());
            }else if("class".equals(n.getType())){
                model.addClassModel(parseClassNode(n));
            }else if("method".equals(n.getType())){
                parseMethodNode(n);
            }
        }
        return model;
    }
    
    protected ClassModel parseClassNode(GrammarNode node){
        ClassModel model = new ClassModel(node.getName());
        grammarNode2Model(node, model);
        for(GrammarNode n: node.getChildren()){
            if("type".equals(n.getType())){
                if("class".equals(n.getName())){
                    model.setType(LanguageModelConstants.TYPE_CLASS);
                }else if("interface".equals(n.getName())){
                    model.setType(LanguageModelConstants.TYPE_INTERFACE);
                }else if("enum".equals(n.getName())){
                    model.setType(LanguageModelConstants.TYPE_ENUM);
                }else if("anotation".equals(n.getName())){
                    model.setType(LanguageModelConstants.TYPE_ANNOTATION);
                }
            }else if("access".equals(n.getType())){
                model.setAccessType(n.getName());
            }else if("extended".equals(n.getType())){
                model.addSuper(n.getName());
            }else if("method".equals(n.getType())){
                model.addMethod(parseMethodNode(n));
            }else if("field".equals(n.getType())){
                FieldModel fm = new FieldModel(n.getName());
                grammarNode2Model(n, model);
                model.addField(fm);
                for(GrammarNode nn: n.getChildren()){
                    if("access".equals(nn.getType()))
                        fm.setAccessType(nn.getName());
                    else if("field_type".equals(nn.getType()))
                        fm.setType(nn.getName());
                }
            }else if("class".equals(n.getType())){
                model.addInnerClass(parseClassNode(n));
            }
        }
        
        return model;
    }
    
    protected MethodModel parseMethodNode(GrammarNode node){
        MethodModel model = new MethodModel(node.getName());
        grammarNode2Model(node, model);
        ParamModel param = null;
        Iterator<GrammarNode> chit = node.getChildren().iterator();
        while(chit.hasNext()){
            GrammarNode n = chit.next();
            if("access".equals(n.getType())){
                model.setAccessType(n.getName());
            }else if("paramName".equals(n.getType())){
                param = new ParamModel(n.getName(), chit.next().getName());
                model.addParam(param);
            }else if("return".equals(n.getType())){
                model.setType(n.getName());
            }
        }
        return model;
    }
    
    public static void grammarNode2Model(GrammarNode node, BaseLanguageModel model){
        model.setStartLine(node.getStartLine());
        model.setStartOffset(node.getStartOffset());
        model.setEndLine(node.getEndLine());
        model.setEndOffset(node.getEndOffset());
    }
}
