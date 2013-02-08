package org.liujing.awttools.classview;

import java.io.*;
import java.util.*;
import java.util.logging.*;

public class ClassSearchPersist{
    static String defaultFileName = "classSearchViewer.conf";
    String LN;

    public ClassSearchPersist(){
        defaultFileName = System.getProperty("classSearchConf");
        LN = System.getProperty("line.separator");
    }

    public Collection<String> load()throws IOException{
        File f = new File(defaultFileName);
        List<String> cps = new ArrayList();
        if(f.exists()){
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String line = reader.readLine();
            while(line != null){
                if(line.trim().length() >0)
                    cps.add(line);
                line = reader.readLine();
            }
            reader.close();
        }
        return cps;
    }

    public void save(Collection<String> data)throws IOException{
        File f = new File(defaultFileName);
        BufferedWriter writer = new BufferedWriter(new FileWriter(f, false));
        for(String p: data){
            writer.write(p);
            writer.write(LN);
        }
        writer.close();
    }
}
