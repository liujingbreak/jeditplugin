package org.liujing.jeditplugin.v2;

import java.io.*;
import java.util.zip.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;
import org.liujing.jeditplugin.v2.dirscan.*;

public class DirectoryScan implements Serializable{
	private static final long serialVersionUID = 99L;
	private static Logger log = Logger.getLogger(DirectoryScan.class.getName());
	private List 		compiledPattern;
	private String 		patternString;
	private boolean skipHiddenFile = true;
	private Pattern diskRootPat = Pattern.compile("\\w[:]");
	
	public DirectoryScan(){
		
	}
	
	public DirectoryScan(String pattern){
		compiledPattern = compilePattern(pattern);
		
	}
	
	/**
	@param pattern will be saved to member field patterString
	@return compiled ArrayList of path patterns
	*/
	protected List compilePattern(String pattern){
		String[] sc = pattern.split("[/\\\\]");
		//Scanner sc = new Scanner(pattern);
		//sc.useDelimiter("[/\\\\]");
		List paths = new ArrayList();
		//while(sc.hasNext()){
		for(String p: sc){
			//String p = sc.next();
			//log.info(p);
			if( (!p.equals("**")) && (p.contains("*") || p.contains("{")) ){
				p = "\\Q" + p + "\\E";
				p = p.replaceAll("\\*", "\\\\E.*\\\\Q");
				p = p.replaceAll("\\{", "\\\\E(?:\\\\Q");
				p = p.replaceAll("\\}", "\\\\E)\\\\Q");
				p = p.replaceAll(",", "\\\\E|\\\\Q");
				log.fine(p);
				Pattern pat = Pattern.compile(p, Pattern.CASE_INSENSITIVE);
				paths.add(pat);
			}else{
				paths.add(p);
			}
		}
		patternString = pattern;
		return paths;
	}
	
	public void patternDrivenScan(File currDir, ScanHandler handler){
		boolean startsWithSlash = patternString.startsWith("/") || patternString.startsWith("\\");
		boolean startsWidthDisk = (patternString.length() > 1 && patternString.charAt(1) == ':');
		//log.fine("pattern:" + patternString + " " + (startsWithSlash?"startsWithSlash":""
		//    + (startsWidthDisk?" startsWidthDisk":"")));
		List paths = compiledPattern;
		File dir = null;
		if(startsWithSlash){
			dir = new File(File.separator);
			pathAnalyse(dir, 1, paths, handler);
		}else if(startsWidthDisk){
			dir = new File((String)paths.get(0));
			log.fine("dir="+ dir.getPath() + (dir.isHidden()?" hidden":" not hidden"));
			pathAnalyse(dir, 1, paths, handler);
		}else{
			dir = currDir;
			pathAnalyse(dir, 0, paths, handler);
		}
	}
	
	public void patternDrivenScan(File currDir){
		patternDrivenScan(currDir, null);
	}
	
	public void patternDrivenScan(String pattern, File currDir){
		compiledPattern = compilePattern(pattern);
		patternDrivenScan(currDir);
	}
	
	private void pathAnalyse(File dir, int idx, List paths, ScanHandler handler){
	    // For using external SSD hard disk, system will show root directory of SSD as a hidden file
	    // I have to check this special case, so I add the determine statement !dir.getName().contains(":")
	    if(skipHiddenFile && (dir.isHidden() && !diskRootPat.matcher(dir.getPath()).matches() ))
			return;
		if(idx < (paths.size()) ){
			if(log.isLoggable(Level.FINE))
				log.fine(dir.getPath() + " idx:"+ idx + " path: "+ paths.get(idx));
			Object p = paths.get(idx);
			if(p.equals("**"))
				dirRecursion(dir, idx, paths, handler);
			else if(p instanceof Pattern){
				dirIterate(dir, idx, paths, handler);
			}else{
				dirLocate(dir, idx, paths, handler);
			}
		}else{
			if(log.isLoggable(Level.FINE))
				log.fine("###### processFile " + dir.getPath());
			if(handler != null)
				handler.processFile(dir);
			else
				processFile(dir);
		}
	}
	

	
	private void dirRecursion(File dir, int idx, List paths, ScanHandler handler){
		if(log.isLoggable(Level.FINE))
			log.fine(dir.getPath() + " idx:"+ idx + " path: "+ (String)paths.get(idx));
		
		if(!dir.isDirectory())
			return;
		if(skipHiddenFile && dir.isHidden())
			return;
		pathAnalyse(dir, idx + 1, paths, handler);
		File[] files = dir.listFiles();
		
		for( File f: files){
			dirRecursion(f, idx, paths, handler);
			//pathAnalyse(f, idx + 1, paths, handler);
		}
		
	}
	
	private void dirIterate(File dir, int idx, List paths, ScanHandler handler){
		if(log.isLoggable(Level.FINE))
			log.fine(dir.getPath() + " idx:"+ idx );
		if(!dir.isDirectory())
			return;
		if(skipHiddenFile && dir.isHidden())
			return;
		File[] files = dir.listFiles();
		String pathPattern = null;
		for( File f: files){
			Pattern pat = (Pattern)paths.get(idx);
			if(pat.matcher(f.getName()).matches()){
				pathAnalyse(f, idx + 1, paths, handler);
			}
		}
	}
	
	private void dirLocate(File dir, int idx, List paths, ScanHandler handler){
		if(log.isLoggable(Level.FINE))
			log.fine(dir.getPath() + " idx:"+ idx + " path: "+ (String)paths.get(idx));
		File f = new File(dir, (String)paths.get(idx));
		if(f.exists()){
			pathAnalyse(f, idx + 1, paths, handler);
		}else{
			if(log.isLoggable(Level.FINE))
				log.fine(f.getPath() + " does not exist" );
		}
	}
	
	protected void processFile(File f){
		
	}
	
	public boolean pathMatchesPattern(String path){
		String[] dirs = path.split("[/\\\\]");
		boolean startsWithSlash = path.startsWith("/") || path.startsWith("\\");
		boolean startsWidthDisk = path.length() > 1 && path.charAt(1) == ':';
		int patIdx, dirIdx = 0;
		
		if(startsWithSlash){
			patIdx = 1;
		}else if(startsWidthDisk){
			patIdx = 0;
		}else{
			patIdx = 0;
		}
		return analysePath(compiledPattern, patIdx, dirs, dirIdx);
	}
	
	public boolean pathMatchesPattern(File currDir, File f)throws IOException{
		String ca = currDir.getCanonicalPath();
		String fa = f.getCanonicalPath();
		String path = null;
		//log.info("currDir=" + ca + " f=" + fa);
		if(fa.startsWith(ca)){
			int start = (ca.endsWith("\\") || ca.endsWith("/"))? ca.length(): (ca.length() +1);
			path = fa.substring(start);
		}else{
			path = f.getPath();
		}
		//log.info(path);
		return pathMatchesPattern(path);
	}
	
	private boolean analysePath( List patterns, int patIdx, String[] dirs, int dirIdx){
		
		if(patIdx < (patterns.size()) ){
			if(dirIdx >= dirs.length)
				return false;
			Object p = patterns.get(patIdx);
			if(p.equals("**")){
				patIdx ++;
				while(dirIdx < dirs.length){
					if(analysePath(patterns, patIdx, dirs, dirIdx))
						return true;
					dirIdx++;
				}
				return false;
			}else if(p instanceof Pattern){
				Pattern pat = (Pattern)p;
				if(pat.matcher(dirs[dirIdx]).matches())
					return analysePath(patterns, patIdx +1, dirs, dirIdx +1);
				
			}else{
				String name = (String)p;
				if(name.equalsIgnoreCase(dirs[dirIdx]))
					return analysePath(patterns, patIdx +1, dirs, dirIdx +1);
			}
			return false;
		}else{
			return dirIdx == dirs.length;
		}
	}
	
	public boolean pathMatchesDirPattern(String path){
		//todo
		String[] dirs = path.split("[/\\\\]");
		boolean startsWithSlash = path.startsWith("/") || path.startsWith("\\");
		boolean startsWithDisk = (path.length() > 1 && path.charAt(1) == ':');
		int patIdx, dirIdx = 0;
		
		if(startsWithSlash){
			patIdx = 1;
		}else if(startsWithDisk){
			patIdx = 0;
		}else{
			patIdx = 0;
		}
		
		return analyseDirPath(compiledPattern, patIdx, dirs, dirIdx);
	}
	
	private boolean analyseDirPath( List patterns, int patIdx, String[] dirs, int dirIdx){
		
		if(patIdx < (patterns.size()-1) ){
			if(dirIdx >= dirs.length)
				return true;
			Object p = patterns.get(patIdx);
			if(p.equals("**")){
				patIdx ++;
				dirIdx = dirs.length;
				return true;
			}else if(p instanceof Pattern){
				Pattern pat = (Pattern)p;
				if(pat.matcher(dirs[dirIdx]).matches())
					return analyseDirPath(patterns, patIdx +1, dirs, dirIdx +1);
				
			}else{
				String name = (String)p;
				if(name.equalsIgnoreCase(dirs[dirIdx]))
					return analyseDirPath(patterns, patIdx +1, dirs, dirIdx +1);
			}
			return false;
		}else{
			return dirIdx == dirs.length;
		}
	}
	
	public String toString(){
		return "DirectoryScan "+ patternString;
	}
}
