package org.liujing.jeditplugin.v2;

import org.antlr.runtime.*;
import java.util.*;
import java.util.logging.*;
import java.io.*;
import liujing.jedit.parser.*;
import org.liujing.parser.*;

/** All those method named "onXxxx()" is called by JavaParser.<br>
    This class is a handler for parsing java file
*/
public interface JavaFileAnalysisHandler{
    public void onTypeAssociated(List<Token> ids);

    public void onImport(String qualifiedName);

    public void onStaticImport(String qualifiedName);

    public void onTypeAssociated(String qualifiedName);

    public void onTypeParameter(String name);

    public void onPackageName(String name);

    public void onTypeDeclaration(String name, int nestLevel, int lineNo);

}
