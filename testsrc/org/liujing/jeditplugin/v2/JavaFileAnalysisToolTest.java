package org.liujing.jeditplugin.v2;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import org.junit.*;
import org.liujing.magdown.parser.*;
import org.liujing.parser.*;
import org.antlr.runtime.*;

public class JavaFileAnalysisToolTest{

    @Test
    public void test1()throws Exception{
        JavaFileAnalysisTool tool = new JavaFileAnalysisTool();
        tool.parseJava(new InputStreamReader
              (JavaFileAnalysisToolTest.class.getResourceAsStream("test.txt")));
        for(String s: tool.getAssociatedTypes()){
            System.out.println("associated type: "+ s);
        }
    }
}
