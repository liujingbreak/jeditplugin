package org.liujing.jeditplugin.v2;

//import org.liujing.jeditplugin.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.zip.*;
import liujing.util.dirscan.DirectoryScan2;
import liujing.util.dirscan.ScanHandler2;
import org.liujing.awttools.classview.*;

public class ProjectController implements Serializable{
	private static Logger log = Logger.getLogger(ProjectController.class.getName());

	static final long 							serialVersionUID 		= 5L;
	UserPref									userpref;
	File										rootDir;
	private ProjectModule						currentProject;
	int											preferredView;
	transient List<String>						projectList;
	transient SortedSet<FileItem>					projectFilesSet;
	transient SortedSet<FileItem>					searchResultCacheSet;
	public static final Comparator<FileItem>	prjFileNamecomp 		= new PrjFileNameComp();
	transient LinkedList<ProjectEventListener>	listeners 				= new LinkedList();
	public static final int						EVENT_OPEN_FILE 		= 1;
	private transient String					lastSearchText;
	private transient Set<File>					classPathList			= new HashSet();
	protected transient Javaprint classSearchEngine;
	private transient ProjectModule selectedModule;

	private static class UserPref implements Serializable{
		public String currProjName;

		public UserPref(){

		}

		private void readObjectNoData()throws ObjectStreamException{
			log.fine("no data to deserialize");
		}
	}

	public ProjectController(){
		rootDir = new File(System.getProperty("user.home"));
	}

	private void readObject(java.io.ObjectInputStream in)
	throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		log.fine("currentProject " + currentProject);
		//log.fine("name " + userpref.currProjName);

		listeners = new LinkedList();
	}

	public static Comparator<FileItem> getProjItemComp(){
		return prjFileNamecomp;
	}

	public void setSelectedModule(ProjectModule m){
	    if(!m.equals(selectedModule)){
	        lastSearchText = null;
	        selectedModule = m;
	    }
	}

	public ProjectModule getSearchTargetModule(){
	    if(selectedModule == null){
	        selectedModule = currentProject;
	    }
	    return selectedModule;
	}

	public void setRootDir(File dir){
		rootDir = dir;
	}

	public String getCurrProjName(){
		return userpref.currProjName;
	}

	public void setPreferredView(int view){
		preferredView = view;
	}

	public int getPreferredView(){
		return preferredView;
	}

	public Javaprint getClassSearchEngine(){
	    if(classSearchEngine == null)
	        classSearchEngine = new Javaprint();
	    return classSearchEngine;
	}

	public List<String> listProjects(){
		if(log.isLoggable(Level.FINE))
			log.fine("list projects: "+ rootDir + "\n" + projectList);
		if(projectList == null){
			projectList = new ArrayList();
		}else{
			projectList.clear();
		}
		if(!rootDir.exists()){
			rootDir.mkdirs();
		}
		File[] files = rootDir.listFiles();
		for(File f: files){
			String n = f.getName();
			if(n.endsWith(".pro")){
				projectList.add(n.substring(0, n.length() - 4));
			}
		}

		return projectList;
	}

	public void clear(){
		projectFilesSet = null;
		searchResultCacheSet = null;
		selectedModule = null;
	}

	public String classpathString(){
	    if(currentProject == null)
	        return "";
		StringBuilder buf = new StringBuilder(50);
		classpathString(buf, currentProject );
		String cpStr = buf.toString();
		//log.info(cpStr);
		return cpStr;
	}

	public void classpathString(StringBuilder buf, ProjectModule module){
	    try{
            DirectoryScan2 cpScanner = module.getClassPathScanner();
            if(cpScanner == null)
                return;
            ClasspathScan scan = new ClasspathScan(buf);
            cpScanner.scan(module.getDirFile(), scan);

            Iterator<ProjectModule> itm = module.allSubModule();
            while(itm.hasNext()){
                ProjectModule subModule = itm.next();
                classpathString(buf, subModule);
            }
		}catch(IOException ioe){
		    log.log(Level.WARNING, "Encounter error while analysing class path", ioe);
		}
	}

	private class ClasspathScan implements ScanHandler2{
		StringBuilder classpath;
		HashSet pathSet = new HashSet();

		public ClasspathScan(){}

		public ClasspathScan(StringBuilder p){
			setPaths(p);
		}

		public void setPaths(StringBuilder p){
			classpath = p;
		}

		public void processFile(File f, String relativePath){
			if(! pathSet.contains(f)){
				classpath.append(f.getPath());
				classpath.append(File.pathSeparator);
				pathSet.add(f);
				//log.info("cp file "+ f.getPath());
			}else{
				log.fine("Duplicate Classpath " + f.getPath());
			}
		}
	}

	public ProjectModule openProject(String name)throws IOException, ClassNotFoundException{
		//long curr = System.currentTimeMillis();
		clear();
		currentProject = loadProject(name);
		//log.info("opennig duration "+ (System.currentTimeMillis() - curr));
		return currentProject;
	}

	public void setCurrentProject(ProjectModule m){
		currentProject = m;
	}

	public ProjectModule getCurrentProject(){
		return currentProject;
	}

	public SortedSet<FileItem> listCurrentPrjFiles(){
		try{
			if(projectFilesSet == null){
				projectFilesSet = new TreeSet( prjFileNamecomp );
			}else{
				projectFilesSet.clear();
			}
			currentProject.fillFilesMap( projectFilesSet );
		}catch(Exception ex){
			log.log(Level.SEVERE, "", ex);
		}
		return projectFilesSet;
	}

	public SortedSet<FileItem> travelFileFolder(ProjectModule m, String path){
	    //long curr = System.currentTimeMillis();
		SortedSet<FileItem> items = new TreeSet( prjFileNamecomp );
		try{
			m.fillFileFolder(items, path);
		}catch(Exception ex){
			log.log(Level.SEVERE, "", ex);
		}
		//log.info("travel time "+ (System.currentTimeMillis() - curr) + " " + path);
		return items;
	}
	/**
	Called by table view searching
	*/
	public SortedSet<FileItem> searchCurrentPrjFiles(String text){
		if(projectFilesSet == null || text.length() == 0){
			listCurrentPrjFiles();
		}
		return projectFilesSet.tailSet(new FileItem(text, "", null));
	}

	/**
	Called by tree view searching
	*/
	public SortedSet<FileItem> searchFile(String text, SearchResponseHandler handler){
		try{
			if( lastSearchText != null && text.startsWith(lastSearchText) && searchResultCacheSet != null){
				log.fine(" continue search from existing search result, last search:" + lastSearchText);
				searchResultCacheSet = searchResultCacheSet.subSet(new FileItem(text, "", null),
					new FileItem(text + "zzzzzzzzzzzzzzzzzzzzz", "", null));
				if(handler != null)
					handler.searchDone(projectFilesSet);
			}
			else{
				if(projectFilesSet != null){
					log.fine(" search is based on existing file table data");
					// To use the existing table view data, so that can skip scan all files again
					searchResultCacheSet = projectFilesSet.subSet(new FileItem(text, "", null),
						new FileItem(text + "zzzzzzzzzzzzzzzzzzzzz", "", null));
					if(handler != null)
						handler.searchDone(searchResultCacheSet);
				}else{
					log.fine("search starts from zero");
					searchResultCacheSet = getSearchTargetModule().searchFile(text);
				}
				if(log.isLoggable(Level.FINE))
					log.fine("find " + searchResultCacheSet.size());
			}
			lastSearchText = text;
		}catch(Exception ex){
			log.log(Level.SEVERE, "", ex);

		}
		return searchResultCacheSet;
	}

	public void clearSearch(){
	  lastSearchText = null;
	  searchResultCacheSet = null;
	}

	//public void searchResponsed(FileItem newFound, SortedSet<FileItem> allFound){
	//
	//}
	//
	//public void searchDone(SortedSet<FileItem> allFound){
	//
	//}

	public void openFile(FileItem item){
		ProjectModule pm = item.module;
		if(pm.getType() == pm.TYPE_ZIP){
			try{
				log.fine("open zip file " + item.name);
				String subDir = "myjedit/" + pm.fullModulePath("_");
				log.fine("subDir=" + subDir);
				File tempDir = new File(System.getProperty("java.io.tmpdir"), subDir);
				log.fine("tempDir " + tempDir.getPath());
				tempDir.mkdirs();
				tempDir = new File(tempDir, item.path);
				tempDir.mkdirs();
				File tempFile = new File(tempDir, item.name);
				BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream(tempFile));
				ZipFile zfile = new ZipFile(pm.getDirFile());
				BufferedInputStream bin = new BufferedInputStream( zfile.getInputStream(zfile.getEntry(item.path + item.name)) );
				int b = bin.read();
				while(b != -1 ){
					out.write(b);
					b = bin.read();
				}
				bin.close();
				out.close();
				fireProjectEvent(EVENT_OPEN_FILE, tempFile.getPath());
			}catch(Exception ex){
				log.log(Level.SEVERE, "", ex);
			}

		}else
			fireProjectEvent(EVENT_OPEN_FILE, item.fullPath());
	}

	//protected String fullPath( FileItem item ){
	//	log.fine(rootDir.getPath() + " | "+ item.path + " | "+ item.name);
	//	return rootDir.getPath() + File.separator + item.path + item.name;
	//}

	public static class PrjFileNameComp implements Comparator<FileItem>, Serializable{
		public PrjFileNameComp(){}

		public int compare(FileItem o1, FileItem o2){
			//return o1.charAt(0) - o2.charAt(0);
			if(o1 instanceof FolderItem && !(o2 instanceof FolderItem))
				return -1;
			else if(o2 instanceof FolderItem && !(o1 instanceof FolderItem))
				return 2;

			int r = compareString(o1.name, o2.name);
			if( r == 0 ){
				r = compareString(o1.path, o2.path);
			}
			return r;
		}

		private int compareString(String s1, String s2){
			s1 = s1.toLowerCase();
			s2 = s2.toLowerCase();
			int i = 0;
			for(; i< s1.length(); i++){
				if(i >= s2.length())
					return 1;
				int r = s1.charAt(i) - s2.charAt(i);
				if(r != 0 )
					return r;
			}
			if( i< s2.length() ){
				return -1;
			}
			return 0;
		}
	}

	public void load()throws IOException, ClassNotFoundException{
		File serFile = new File(rootDir, "my_proj_pref.ser");
		if(serFile.exists()){
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
				serFile));
			userpref = (UserPref) in.readObject();
			in.close();
		}else{
			userpref = new UserPref();
		}
	}

	public void saveProject(ProjectModule p)throws IOException{
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
			new File(rootDir, p.getName() + ".pro"), false));
		out.writeObject(p);
		out.close();
	}

	public void deleteProject(ProjectModule p)throws IOException{
		File f = new File(rootDir, p.getName() + ".pro");
		f.delete();
		if(projectList != null)
			projectList.remove(p.getName());
	}

	public void deleteProject(String name){
	    File f = new File(rootDir, name + ".pro");
	    if(f.exists())
	        f.delete();
		if(projectList != null)
			projectList.remove(name);
	}

	protected ProjectModule loadProject(String name)throws IOException, ClassNotFoundException{
	    File pFile = new File(rootDir, name + ".pro");
	    if(!pFile.exists())
	        return null;
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(
			new File(rootDir, name + ".pro")));
		ProjectModule p = (ProjectModule) in.readObject();
		in.close();
		return p;
	}

	public void save()throws IOException{
		if(currentProject != null)
			userpref.currProjName = currentProject.getName();
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
			new File(rootDir, "my_proj_pref.ser"), false));
		out.writeObject(userpref);
		out.close();
	}

	//private void writeObject(java.io.ObjectOutputStream out)
	//throws IOException
	//{
	//	projectList = null;
	//	out.defaultWriteObject();
	//}

	public static class FileItem{
		public String 			name;
		public String 			path;
		public ProjectModule	module;

		public FileItem(String name, String path, ProjectModule module){
			this.name = name;
			this.path = path;
			this.module = module;
		}

		public String fullPath(){
			return module.getDirectory() + File.separator + longPath();
		}

		public String longPath(){
			if(path.endsWith("/") || path.endsWith("\\"))
				return path + name;
			else
				return path + File.separator + name;
		}

		public String toString(){
			return name;
		}
	}

	public static class FolderItem extends FileItem{
		public FolderItem(String name, String path, ProjectModule module){
			super(name, path, module);
		}

		@Override
		public String toString(){
			return name + "/";
		}
	}

	public void addProjectEventListener(ProjectEventListener l){
		listeners.add(l);
	}

	public void removeProjectEventListener(ProjectEventListener l){
		listeners.remove(l);
	}

	protected void fireProjectEvent(int eventType, Object param){
		Iterator<ProjectEventListener> it = listeners.iterator();
		while(it.hasNext()){
			it.next().projectAction(eventType, param);
		}
	}

	public interface ProjectEventListener{
		public void projectAction(int eventType, Object param);
	}

}
