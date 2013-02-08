package org.liujing.jeditplugin.v2;

import java.util.*;
import java.io.*;
import java.util.logging.*;

/**
 JavaParserFieldDecHandler
 @author Break(Jing) Liu
*/
public interface JavaParserFieldDecHandler{

    public void onFieldDeclaration(String name, String type);
}

