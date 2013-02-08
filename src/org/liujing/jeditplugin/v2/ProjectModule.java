package org.liujing.jeditplugin.v2;

import org.liujing.jeditplugin.*;
import java.io.*;
import java.util.zip.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;
import static org.liujing.jeditplugin.v2.ProjectController.FileItem;
import static org.liujing.jeditplugin.v2.ProjectController.FolderItem;
import org.liujing.jeditplugin.v2.dirscan.*;
import liujing.util.dirscan.DirectoryScan2;
import liujing.util.dirscan.ScanHandler2;
import org.liujing.jeditplugin.MyJeditPlugin;
import org.liujing.filesync.*;
import org.liujing.filesync.StateReadController.IndexEntity;

public class ProjectModule implements Serializable{
	private static Logger log = Logger.getLogger(ProjectModule.class.getName());

	private static final long 				serialVersionUID = 43L;

	private String 							name;
	private String 							directory;
	private File							dirFile;
	//private List<Pattern>					includesPats;
	private List<Pattern>					excludesPats;
	private List<String> 					includes;
	private List<String> 					excludes;

	//private List<Pattern>					classPathsPats;
	private Collection<String>					classPaths;

	private int 							type 			= -1;
	public static final int 				TYPE_ZIP		= 1;
	public static final int 				TYPE_DIR		= 0;
	private ProjectModule 					parent;
	private List< ProjectModule> 			subModules = new ArrayList();
	private static String					dirSeperator	= "\\\\/"; //File.separatorChar == '\\'?"\\\\":File.separator;
	private boolean							listed			= true;
	private transient TreeMap<String, Object> zipFilesMap;
	private transient SortedSet<FileItem> 	cacheFileItems;
	private static StringComp					nameComp = new StringComp();

	private DirectoryScan2 classPathScanner;
	private DirectoryScan2 fileScanner = new DirectoryScan2();

	private transient StateReadController _stateReader;
	private transient File stateFile;

	public ProjectModule(){
		includes = new ArrayList();
		excludes = new ArrayList();
		//includesPats = new ArrayList();

		//classPathsPats = new ArrayList();
		classPaths = new ArrayList();
	}

	public void clear(){
		zipFilesMap = null;
		cacheFileItems = null;
		classPathScanner = null;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public void setDirectory(String dir){
		directory = dir;
		//if(dirFile == null)
		dirFile = new File(directory);
	}

	public String getDirectory(){
		return directory;
	}

	public File getDirFile(){
		return dirFile;
	}

	/**
	@param type TYPE_ZIP or TYPE_DIR
	*/
	public void setType(int type){
		this.type = type;
	}

	/**
	@return TYPE_ZIP, TYPE_DIR or -1
	*/
	public int getType(){
		return type;
	}

	public boolean isListed(){
		return listed;
	}

	public void setListed(boolean b){
		listed = b;
	}

	public void addSubModule(ProjectModule m){
		if(subModules == null)
			subModules = new ArrayList();
		if(log.isLoggable(Level.FINE))
			log.fine("before add " + subModules.size() + " hashcode:" + this.hashCode());
		subModules.add(m);
		m.parent = this;
		if(log.isLoggable(Level.FINE))
			log.fine("after add " + subModules.size() +" name:" + m.getName());
	}

	public void removeSubModule(ProjectModule m){
		subModules.remove(m);
	}

	public Iterator<ProjectModule> allSubModule(){
		return subModules.iterator();
	}

	public List<String> getIncludes(){
		return includes;
	}

	public List<String> getExcludes(){
		return excludes;
	}

	public ProjectModule getParent(){
		return parent;
	}

	public void delete(){
	}

	public void setIncludes(List<String> list){
		//includesPats.clear();
		//for(String s: list){
		//	log.fine(s);
		//	includesPats.add( Pattern.compile(convertPattern(s)) );
		//}
		includes = list;



		// new Scanner
		if(fileScanner == null)
		    fileScanner = new DirectoryScan2();
		fileScanner.setIncludes(list);
	}

	public void setExcludes(List<String> list){
		excludes = list;
		// new Scanner
		if(fileScanner == null)
		    fileScanner = new DirectoryScan2();
		fileScanner.setExcludes(list);
	}

	public void setClassPaths(Collection<String> list){
		classPaths = list;
		classPathScanner = new DirectoryScan2(list, false);
		classPathScanner.enableDirectory(true);
	}

	public Collection<String> getClassPaths(){
		return classPaths;
	}

	/**
	@return null if there is no class path setting
	*/
	public DirectoryScan2 getClassPathScanner(){
	    if(classPathScanner == null){
	        classPathScanner = new DirectoryScan2(classPaths);
	        classPathScanner.enableDirectory(true);
	    }
	    return classPathScanner;
	}

	public static String convertPattern(String s){
		String name = "[^" + dirSeperator + "]+";
		String subDirs = "(?:"+ name + "[" + dirSeperator + "])*";

		StringBuilder buf = new StringBuilder("\\Q");
		int start = 0, i = 0;
		for(; i< s.length(); i++){
			if(s.charAt(i) == '/'){
				buf.append(s.substring(start, i));
				buf.append("\\E[\\\\|/]\\Q");
				start = i + 1;
			}else if(s.charAt(i) == '*'){
				if(start < i){
					buf.append(s.substring(start, i));
				}
				buf.append("\\E");
				if( (i + 2) < s.length() && s.charAt(i + 1) == '*' && s.charAt(i + 2) == '/'){
					buf.append(subDirs);
					i+= 2;
				}else{
					buf.append(name);
				}
				buf.append("\\Q");
				start = i + 1;
			}
		}
		if(start < i){
			buf.append(s.substring(start, i));
		}
		buf.append( "\\E" );
		s = buf.toString();

		log.fine("1   "+ s);

		return s;
	}

	//private boolean _isValidPath(String path){
	//	boolean flag = true;
	//
	//	if(includes != null){
	//		for(Pattern pat: includesPats){
	//			if( !pat.matcher(path).matches() ){
	//				flag = false;
	//				//if(log.isLoggable(Level.FINE))
	//				//	log.fine(path + " not match include "+ pat);
	//			}else{
	//				flag = true;
	//				break;
	//			}
	//		}
	//	}
	//	if(!flag)
	//		return false;
	//	if(excludes == null)
	//		return true;
	//	//log.fine(path + " excludesPats size " + excludesPats.size() );
	//	for(Pattern pat: excludesPats){
	//		if( pat.matcher(path).matches() ){
	//			//if(log.isLoggable(Level.FINE))
	//			//	log.fine(path + " match exclude "+ pat);
	//			return false;
	//		}
	//	}
	//	return true;
	//}

	/**
	@return StateReadController if found cache, else it returns null
	*/
	protected StateReadController detectState()throws FileNotFoundException, IOException{
	    if(_stateReader != null)
	        return _stateReader;
	    if(stateFile == null){
	        stateFile = stateFile();
	    }
	    if(stateFile.exists()){
	        _stateReader = new StateReadController(stateFile.getParentFile());
	        _stateReader.setStateFile(stateFile);
	        return _stateReader;
	    }
	    return null;
	}

	public boolean isStateCacheEnabled()throws FileNotFoundException, IOException{
	    return detectState() != null;
	}

	public File stateFile(){
            String dirPath = fullModulePath(File.separator);
            File pluginHome = MyJeditPlugin.getInstance().getPluginHome();
            File parentFolder = new File(pluginHome, dirPath);
            if(!parentFolder.exists()){
                parentFolder.mkdirs();
            }
            File stateFile = new File(parentFolder, name + "-state");
            log.fine("state file: "+ stateFile);
            return stateFile;
	}

	public void releaseStateCache()throws IOException{
	    if(_stateReader != null){
	        _stateReader.close();
	        _stateReader = null;
	    }
	}

	private boolean maybeContainValidFile(String dirPath)throws IOException{
	    return fileScanner.maybePathInclude(dirPath);

		//boolean flag = true;
		//if(inScans != null){
		//	for(DirectoryScan scan: inScans){
		//		if(!scan.pathMatchesDirPattern(dirPath)){
		//			flag = false;
		//		}else{
		//			flag = true;
		//			break;
		//		}
		//	}
		//}
		//return flag;
	}

	private boolean isValidPath(String path){
	    return fileScanner.isPathInclude(path) && !fileScanner.isPathExclude(path);
		//boolean flag = true;
		//if(inScans != null){
		//	for(DirectoryScan scan: inScans){
		//		if(!scan.pathMatchesPattern(path)){
		//			flag = false;
		//		}else{
		//			flag = true;
		//			break;
		//		}
		//	}
		//}
		//if(!flag)
		//	return false;
		//if(exScans == null)
		//	return true;
		//for(DirectoryScan scan: exScans){
		//	if(scan.pathMatchesPattern(path)){
		//		return false;
		//	}
		//}
		//return true;
	}

	public SortedSet<ProjectController.FileItem> searchFile(String text)
	throws IOException, ZipException, ClassNotFoundException{
	    if(cacheFileItems != null){
	        return cacheFileItems.subSet(new FileItem(text, "", null), new FileItem(text + "zzzzzzzzzzzzzzzzzzzzz", "", null));
	    }else{
	        SortedSet<ProjectController.FileItem> set = new TreeSet(ProjectController.prjFileNamecomp);
	        findFile(set, text, null);
	        return set;
	    }
	}

	public void createCache()	throws IOException, ZipException, ClassNotFoundException{
	    cacheFileItems = new TreeSet(ProjectController.prjFileNamecomp);
	    findFile(cacheFileItems, "", null);
	}

	@Deprecated
	public void findFile(SortedSet<ProjectController.FileItem> set, String text, SearchResponseHandler handler)
	throws IOException, ZipException, ClassNotFoundException
	{
		if(dirFile == null)
			return ;
		if(parent == null && text != null)
			text = text.toLowerCase();
		StateReadController stateReader = detectState();
		if(stateReader != null){
		    Collection<EntityPersistRef<IndexEntity>> indices = stateReader.findFileByName(getDirFile(), text);
		    for(EntityPersistRef<IndexEntity> ref : indices){
		        IndexEntity entity = stateReader.getIndexEntity(ref);
		        set.add(new ProjectController.FileItem(entity.getName(),
		            entity.getParentPath(), this));
		    }
		    return;
		}
		if(listed){
			if(type == TYPE_DIR)
				findFileInDir(set, dirFile, text, handler);
			else{
				findFileInZip(set, dirFile, text, handler);
			}
		}
		//Iterator<ProjectModule> modules = subModules.iterator();
		//while(modules.hasNext()){
		//	ProjectModule m = modules.next();
		//	m.findFile(set, text, handler);
		//}
		if(handler != null)
			handler.searchDone(set);
	}

	protected void findFileInDir(SortedSet<ProjectController.FileItem> set, File dir, String text, SearchResponseHandler handler)
	throws IOException{
		FileScanHandler h = new FileScanHandler(set, dir, text);
		fileScanner.scan(dir, h);
	}



	protected void findFileInZip(SortedSet<ProjectController.FileItem> set, File zipFile, String text, SearchResponseHandler handler)
	throws IOException, ZipException
	{
		ZipFile zfile = new ZipFile(zipFile);


		Enumeration entries = zfile.entries();
		while(entries.hasMoreElements()){
			ZipEntry entry = (ZipEntry)entries.nextElement();
			if(entry.isDirectory())
				continue;
			String path = entry.getName();

			if( isValidPath(path) ){

				int lastSep = path.lastIndexOf("/");
				String name = path.substring(lastSep + 1);
				if(text != null && !name.toLowerCase().startsWith(text))
					continue;
				path = path.substring(0, lastSep + 1);
				FileItem item = new ProjectController.FileItem(name, path, this);
				set.add(item);
				if(handler != null)
					handler.searchResponsed(item, set);
			}
		}
	}

	public void classPathFiles(Set<File> set, SearchResponseHandler handler)
	{
	}



	public void fillFilesMap(SortedSet<ProjectController.FileItem> set)
	throws IOException, ZipException, ClassNotFoundException{
		findFile(set, null, null);
	}

	class FileScanHandler implements ScanHandler, ScanHandler2{
		private SortedSet<ProjectController.FileItem> set;
		private File currentDir;
		private String text;

		public FileScanHandler(SortedSet<ProjectController.FileItem> set, File dir, String searchText){
			this.set = set;
			List<String> exList = getExcludes();
			currentDir = dir;
			text = searchText.toLowerCase();
		}

		@Deprecated
		public void processFile(File f){
			try{
				if(f.isDirectory())
					return;
				String rootPath = dirFile.getPath();
				String path = f.getPath().substring(rootPath.length()+1);
				String name = f.getName();
				if(text != null && text.length()>0 && !name.toLowerCase().startsWith(text))
					return;
				int lastSep = path.lastIndexOf(File.separatorChar);
				path = path.substring(0, lastSep + 1);
				set.add(new ProjectController.FileItem(name, path, ProjectModule.this));
			}catch(Exception ex){
				log.log(Level.SEVERE, "Failed to build File Table", ex);
			}
		}

		public void processFile(File f, String relativePath){
		    try{
				String rootPath = dirFile.getPath();
				String path = f.getPath().substring(rootPath.length()+1);
				String name = f.getName();
				if(text != null && text.length()>0 && !name.toLowerCase().startsWith(text))
					return;
				int lastSep = path.lastIndexOf(File.separatorChar);
				path = path.substring(0, lastSep + 1);
				set.add(new ProjectController.FileItem(name, path, ProjectModule.this));
		    }catch(Exception ex){
				log.log(Level.SEVERE, "Failed to build File Table", ex);
			}
		}
	}


	/**
	@param dirPath if is null or emptry string, then root directroy will be used;
	*/
	public void fillFileFolder(SortedSet<ProjectController.FileItem> set, String dirPath)
	throws IOException, ZipException, ClassNotFoundException{
		if(dirFile == null || (!listed))
			return ;
		//log.info("\"" + dirPath+ "\"");
		StateReadController stateReader = detectState();
		if(stateReader != null){
		    List<File> tempList = new ArrayList<File>();
		    if(dirPath == null)
		        dirPath = "";
		    //log.info("state " + dirPath);
		    stateReader.listDirs(getDirFile(), dirPath, tempList);
		    //log.info("subfolders : " + tempList.size());
		    for(File d: tempList){
		        set.add(new ProjectController.FolderItem(d.getName(), dirPath, this));
		        //log.info("sub folder : " + d.getName());
		    }
		    tempList.clear();
		    stateReader.listFiles(getDirFile(), dirPath, tempList);
		    for(File d: tempList){
		        set.add(new ProjectController.FileItem(d.getName(), dirPath, this));
		    }
		    return;
		}
		if(type == TYPE_DIR){
			File folder = null;
			if(dirPath == null || dirPath.length() == 0)
				folder = dirFile;
			else
				folder =  new File(dirFile, dirPath);
			fillFileFolderByDir(set, folder);
		}else{
			fillFileFolderByZipFile(set, dirPath);
		}
	}

	protected void fillFileFolderByDir(SortedSet<ProjectController.FileItem> set, File dir)
	throws IOException{
		File[] files = dir.listFiles();
		if(files == null)
		    return;
		String rootPath = dirFile.getPath();
		for(File f: files){
			if(f.isHidden())
				continue;
			String path = f.getPath().substring(rootPath.length()+1);
			String name = f.getName();
			if(f.isDirectory()){
			    if(maybeContainValidFile(path)){
				int lastSep = path.lastIndexOf(File.separatorChar);
				path = path.substring(0, lastSep + 1);
				set.add(new ProjectController.FolderItem(name, path, this));
				}
			}else{
				if( isValidPath(path) ){
					int lastSep = path.lastIndexOf(File.separatorChar);
					path = path.substring(0, lastSep + 1);
					set.add(new ProjectController.FileItem(name, path, this));
				}
			}
		}
	}

	protected void fillFileFolderByZipFile(SortedSet<ProjectController.FileItem> set,
		String dir)throws ZipException, IOException
	{
		if(zipFilesMap == null){
			zipFilesMap = new TreeMap(nameComp);
			ZipFile zfile = new ZipFile(dirFile);
			Enumeration entries = zfile.entries();
			while(entries.hasMoreElements()){
				ZipEntry entry = (ZipEntry)entries.nextElement();
				if(entry.isDirectory())
					continue;
				String path = entry.getName();

				if( isValidPath(path)){
					int lastSep = path.lastIndexOf("/");
					String name = path.substring(lastSep + 1);
					path = path.substring(0, lastSep + 1);
					TreeMap<String, Object> cacheMap = zipFilesMap;
					String[] paths = path.split("[/|\\\\]");
					for(String s: paths){
						if(!cacheMap.containsKey(s)){
							TreeMap<String, Object> subMap = new TreeMap(nameComp);
							cacheMap.put(s, subMap);
							cacheMap = subMap;
						}else{
							cacheMap = (TreeMap<String, Object>)cacheMap.get(s);
						}
					}
					cacheMap.put(name, new ProjectController.FileItem(name, path, this));
				}
			}
		}
		//log.fine("dir = " + dir);
		TreeMap<String, Object> cacheMap = zipFilesMap;
		if(dir != null){
			String[] paths = dir.split("[/|\\\\]");
			for(String s: paths){
				if(s.length() == 0)
					continue;
				cacheMap = (TreeMap<String, Object>)cacheMap.get(s);
			}
		}
		//log.fine("cacheMap = " + (cacheMap==null?"null":cacheMap.size()));
		Iterator<Map.Entry<String, Object>> it = cacheMap.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, Object> entry = it.next();
			Object item = entry.getValue();
			if(item instanceof FileItem){
				set.add((FileItem)item);
			}else{
				//log.fine("folder=" + dir);
				set.add(new FolderItem(entry.getKey(), (dir==null?"":dir), this));
			}
		}
	}

	public String fullModulePath(String delimiter){
		StringBuilder sb = new StringBuilder(50);
		List<String> names = new ArrayList(5);

		ProjectModule m = this;
		while(m != null){
			names.add(m.getName());
			m = m.getParent();
		}
		for(int i = names.size() -1; i >= 0; i--){
			sb.append(names.get(i));
			sb.append(delimiter);
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}

	public String fullModulePath(){
		return fullModulePath("/");
	}

	public String toString(){
		return "[" + name + "]";
	}

	private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException
    {
        log.fine("reading module " + name);
        in.defaultReadObject();
        log.fine("module name is " + name);
    }

    private void writeObject(java.io.ObjectOutputStream out)
    throws IOException
    {
        releaseStateCache();
        out.defaultWriteObject();
        log.fine("writen module name is " + name);
    }

}
