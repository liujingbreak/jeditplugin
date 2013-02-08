package org.liujing.tool;

import java.util.*;
import java.io.*;
import java.util.logging.*;

public interface JSHandler{
    public void onFunctionStart(int line, String name, String params, int streamOffset);

        public void onFunctionEnd(int streamOffset);

        public void onJSONStart(int line, int streamOffset);

        public void onJSONEnd(int streamOffset);

        public void onJSONProperty(String name, int line);

        public void onFunctionAssign(String varname, Object tree);
        
        public void onDoc(int line, int streamOffset, int streamEnd, String docContent);
}
