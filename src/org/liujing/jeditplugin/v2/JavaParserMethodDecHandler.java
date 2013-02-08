package org.liujing.jeditplugin.v2;

import java.util.*;
import java.io.*;
import java.util.logging.*;

/**
 JavaParserMethodDecHandler
 @author Break(Jing) Liu
*/
public interface JavaParserMethodDecHandler{

    public void onConstructDeclaration(String name,
        List<String> params, List<String> throwTypes);

    public void onMethodDeclaration(String name,
        List<String> params, String returnType, List<String> throwTypes);

    public void onFieldDec(String name, String type);
}
