package org.liujing.ironsword.ctl;

import java.util.*;
import java.io.*;
import java.util.zip.*;
import java.util.logging.*;
import java.sql.*;
import java.util.regex.*;
import org.liujing.ironsword.dao.*;
import org.liujing.ironsword.IronException;

public class ZipFileController{
    private static Logger log = Logger.getLogger(ZipFileController.class.getName());
    
    private static Pattern zipSuffixPat = Pattern.compile("\\.(?:zip|jar)", Pattern.CASE_INSENSITIVE );
    
    public File extractToFileIfNeed(SrcFile srcFile)throws IOException{
	    Info_fileInZip zipinfo = zipFileOf(srcFile);
        File extracted = zipinfo.extractedFile();
        if(! (extracted.exists() && extracted.isFile() 
            && extracted.lastModified() > srcFile.getLastModified().getTime()))
        {
            BufferedOutputStream out = new BufferedOutputStream( 
                new FileOutputStream(extracted));
            extractToStream(zipinfo, srcFile, out);
        }else{
            log.info(srcFile.getName() + " has already been extracted");
        }
        return extracted;
	}
	
    protected void extractToStream(Info_fileInZip zipinfo, SrcFile srcFile, OutputStream out)
    throws IOException
    {
        
        ZipFile zfile = new ZipFile(zipinfo.zipPath);
		BufferedInputStream bin = new BufferedInputStream( zfile.getInputStream(
		    zfile.getEntry(zipinfo.entryPath)) );
		int b = bin.read();
		while(b != -1 ){
			out.write(b);
			b = bin.read();
		}
		bin.close();
		out.close();
	}
	
    private static class Info_fileInZip{
        public String zipPath;
        public String entryPath;
        public SrcFile srcfile;
        
        public File extractedFile(){
            String base = System.getProperty("tempdir", System.getProperty("java.io.tmpdir"));
            String name = srcfile.getName();
            int dot = name.lastIndexOf(".");
            if(dot >= 0)
                name = name.substring(0, dot);
            return new File(base, String.format("%1$s-%2$d.%3$s", 
                name , srcfile.getId(), srcfile.getSrcType()));
        }
    }
    
    private Info_fileInZip zipFileOf(SrcFile srcfile){
        String fullpath = srcfile.fullpath();
        Matcher m = zipSuffixPat.matcher(fullpath);
        if(m.find()){
            Info_fileInZip res = new Info_fileInZip();
            res.zipPath = fullpath.substring(0, m.end());
            res.entryPath = fullpath.substring(m.end() + 1);
            if(File.separatorChar == '\\')
                res.entryPath = res.entryPath.replaceAll("\\\\", "/");
            res.srcfile = srcfile;
            return res;
        }else{
            log.severe("zip file suffix does not match: "+ fullpath);
            throw new IronException("zip file suffix does not match: "+ fullpath);
        }
    }
    
    
}
