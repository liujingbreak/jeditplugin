package org.liujing.jeditplugin;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.msg.*;
import org.liujing.util.*;
import java.util.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.util.zip.*;
import java.util.logging.*;
import java.beans.*;
import java.util.concurrent.*;
/**
use getInstance() to get the instance for using.
*/
public class MyProject implements EBComponent
{
	private static LogThread log=new LogThread(MyProject.class);
	private static Logger logger=Logger.getLogger(MyProject.class.getName());
	
	public final static String DEFAULT_PROJECT_NAME="default";
	public static MyProject instance=null;
	protected HashMap<String,QuickProject<String>> projects=new HashMap();
	protected HashMap<String,MyProjectOption> options=new HashMap();
	public ExecutorService threadpool;
	/**
	Basic constructor for MyProject
	*/
	protected MyProject()
	{
		threadpool=Executors.newSingleThreadExecutor();
	}
	
	public static synchronized MyProject getInstance()
	{
		if(instance==null)
			instance=new MyProject();
		return instance;
	}
	/**
	list all projects in a drop down list
	*/
	public ComboBoxModel getProjectList()
	{
		DefaultComboBoxModel model=null;
		if(model==null)
				model=new DefaultComboBoxModel();
		File homedir=MyJeditPlugin.getInstance().getPluginHome();
		if(!homedir.exists()){
			homedir.mkdirs();
			model.addElement(DEFAULT_PROJECT_NAME);
			return model;
		}
		File[] allfiles=homedir.listFiles();
		for(File f:allfiles){
			String nm=f.getName();
			if(nm.endsWith(".prj"))
			model.addElement(nm.substring(0,nm.length()-4));		
		}
		if(model.getSize()==0){
			model.addElement(DEFAULT_PROJECT_NAME);
		}
		return model;
	}
	protected void addProject(String name){
		
		java.util.List<MyJeditPluginPanel> panels=MyJeditPlugin.getInstance().panels;
		for(MyJeditPluginPanel panel:panels){
			((DefaultComboBoxModel)panel.getProjectList().getModel()).addElement(name);
		}
	}
	public MyProjectOption getOption(String prjName)
	{
		MyProjectOption o=options.get(prjName);
		if(o==null){
			try{
			o=loadOption(prjName);
			}catch(Exception ex){
				logger.log(Level.SEVERE,"Failed to load prject options from disk",ex);
			}
		}
		return o;
	}
	
	public void saveOption(String prjName)throws IOException
	{
		MyProjectOption o=options.get(prjName);
		File homedir=MyJeditPlugin.getInstance().getPluginHome();
		if(!homedir.exists()){
			homedir.mkdirs();
		}
		File savedFile=new File(homedir,prjName+"_prj.properties");
		//logger.fine("o="+o+" savedFile="+savedFile);
		o.store(savedFile);
	}
	protected MyProjectOption loadOption(String prjName)throws IOException,FileNotFoundException
	{
		File homedir=MyJeditPlugin.getInstance().getPluginHome();
		
		MyProjectOption o=new MyProjectOption(prjName);
		o.addPropertyChangeListener(new OptionChangeListener());
		if(!homedir.exists()){
			homedir.mkdirs();
			return o;
		}
		File savedFile=new File(homedir,prjName+"_prj.properties");
		o.load(savedFile);
		options.put(prjName,o);
		return o;
	}
	/**
	add files into project. If the project is new, it will add this project name into project drop down list
	*/
	public int addFiles(File[] paths,String projectName,String... regex)throws Exception
	{
		//log.debug("add files");
		QuickProject<String> pj=projects.get(projectName);
		/*if(pj==null){
			pj=new 	QuickProject(projectName);
			projects.put(projectName,pj);
			if(!projectName.equals(DEFAULT_PROJECT_NAME)){
				addProject(projectName);
			}
		}
		else */
		if(pj.isNew()){
			addProject(projectName);
			pj.setNew(false);
		}
		
		int sum= addFiles(paths,projectName,pj,regex);
		pj.freshView();
		pj.save();
		//MyJeditPlugin.getInstance().getPanel();
		return sum;
	}
	/**
	remove from project
	*/
	public void remove(String projectName,int[] indices)throws Exception
	{
		QuickProject qp=projects.get(projectName);
		qp.remove(indices);
		qp.freshView();
		qp.save();
	}
	/**
	remove from project and delete from disk
	*/
	public void removeAndDelete(String projectName,int[] indices)throws Exception
	{
		QuickProject qp=projects.get(projectName);
		qp.remove(indices);
		
		for(int i=0;i<indices.length;i++)
		{
			try{
				File f=getFile(projectName,indices[i]);
				if(!f.delete()){
					throw new Exception("Can't delete "+f.getAbsolutePath());
				}
			}catch(Exception ex){
				logger.log(Level.SEVERE,"cant't delete project file: "+indices[i],ex);
			}
		}
		qp.freshView();
		qp.save();
	}                                          
	public void renameFile(String projectName,int index,String newName)throws Exception
	{
		QuickProject qp=projects.get(projectName);
		//qp.renameItem(index,newName);
		File f=getFile(projectName,index);
		File nf=new File(newName);
		nf.getParentFile().mkdirs();
		if(f.renameTo(nf)){
			qp.remove(new int[]{index});
			addFiles(new File[]{nf},projectName,qp);
			//qp.add(newName);
			qp.freshView();
			qp.save();
		}else{
			logger.log(Level.SEVERE,"Can't rename file");
		}
	}
	public void search(String projectName,String name){
		QuickProject<String> pj=projects.get(projectName);
		if(pj==null){
			pj=new 	QuickProject(projectName);
			projects.put(projectName,pj);
		}
		pj.freshView(name);
	}
	
	public QuickProject.ProjectItem getItem(String projectName,int index)
	{
		QuickProject qp=projects.get(projectName);
		return qp.get(index);
	}
	
	public File getFile(String projectName,int index)throws Exception
	{
		QuickProject.ProjectItem o=getItem(projectName,index);
		String rootPath=getOption(projectName).getRootPath();
		if(! (o instanceof QuickProject.ZipProjectItem))
		{
			String path=o.getDir()+File.separator+o.getName();
			
			File f=null;
			if(rootPath!=null && (!isAbsolutePath(path))){
				f=new File(rootPath,path);
			}else{
				f=new File(path);
			}
			return f;
		}else{
			QuickProject.ZipProjectItem it=(QuickProject.ZipProjectItem)o;
			String path=it.getProjectDir();
			File f=null;
			if(rootPath!=null && (!isAbsolutePath(path))){
				f=new File(rootPath,path);
			}else{
				f=new File(path);
			}
			ZipFile zipFile=new ZipFile(f);
			//logger.fine("get entry"+(String)it.item+it.name);
			ZipEntry entry=zipFile.getEntry((String)it.item+it.getName());
			//logger.fine("entry size:"+entry.getSize());
			InputStream is=zipFile.getInputStream(entry);
			return new File(extractFileToTemp(it.getName(),is));
		}
	}

	public TableModel getProjectTableModel(String name)
	{
		//log.debug(name);
		QuickProject qp=projects.get(name);
		if(qp==null){
			qp=new QuickProject(name);
			qp.load(getOption(name).getRootPath());
			//if(!qp.load())
			//	addProject(name);
			projects.put(name,qp);
		}
		releaseProjectExcept(name);
		return qp.tableModel();
	}
	public void openFileByIndex(String prjName,int index){
		try{
			Object o=projects.get(prjName).get(index);
			File f=getFile(prjName,index);
			jEdit.openFile(jEdit.getActiveView(),f.getPath());		
		}catch(Exception ex){
				logger.log(Level.SEVERE,"",ex);
		}
	}
	public void sortFileListByDir(String projectName)
	{
		QuickProject qp=projects.get(projectName);
		qp.sortByItemDir();
	}
	public void cancelSortByDir(String projectName)
	{
		QuickProject qp=projects.get(projectName);
		qp.freshView();
	}
	protected boolean isAbsolutePath(String path)
	{
		return path.charAt(1)==':'||path.startsWith("/");
	}
	protected int addFiles(File[] paths,String projectName,QuickProject project,String... regex)throws ZipException,IOException
	{
		
		int sum=0;
		for(int i=0;i<paths.length;i++)
		{
			File dest=paths[i];
			String postFix=null;
			if(dest.isDirectory()){
				File[] subDests=dest.listFiles();
				sum+=addFiles(subDests,projectName,project,regex);				
			}
			else{
				int nameLen=dest.getName().length();
				if(nameLen>4){
					postFix=dest.getName().substring(nameLen-4);
				}
				if(".zip".equalsIgnoreCase(postFix)||".jar".equalsIgnoreCase(postFix)){
					sum+=_addZipFile(dest,project,regex);
				}else{
					String prjItem=converFilePathByProjectName(projectName,dest.getAbsolutePath());
					sum+=_addFile(prjItem,regex,project);
				}
			}
		}
		return sum;
	}
	protected int _addZipFile(File zipFile,QuickProject project,String... regex)throws ZipException,IOException
	{
		int count=0;
		ZipFile zip=new ZipFile(zipFile);
		String zipPath=zipFile.getPath();
		Enumeration<? extends ZipEntry> entries=zip.entries();
		while(entries.hasMoreElements()){
			ZipEntry entry=entries.nextElement();
			if(!entry.isDirectory()){
				for(String reg:regex){
					if(entry.getName().matches(reg)){
						project.add(zipPath,entry.getName());
						count++;
						break;
					}
				}
				
			}
		}
		return count;
	}
	protected void releaseProjectExcept(String... prjNames)
	{
		ArrayList<String> keys=new ArrayList(projects.keySet());
		for(String k:keys){
			for(String pn:prjNames){
				if(!k.equals(pn)){
					projects.remove(k);
				}
			}
		}
		System.gc();
	}
	protected int _addFile(String filepath,String[] regex,QuickProject pj){
		//log.debug("add file "+f.getName());
		if(regex==null){
			return pj.add(filepath)?1:0;
		}
		for(String reg:regex){
			if(filepath.matches(reg)){
				return pj.add(filepath)?1:0;
			}
		}
		return 0;
	}
	
	protected String extractFileToTemp(String name,InputStream is)throws Exception
	{
		File tempDir=new File(MyJeditPlugin.getInstance().getPluginHome(),"temp");
		if(!tempDir.exists())
			tempDir.mkdirs();
		String filenms[]=tempDir.list();
		int num=filenms.length;
		File temp=new File(tempDir,num+1+"-"+name);
		logger.fine(temp.getPath());
		BufferedOutputStream bout=new BufferedOutputStream(
			new FileOutputStream(temp,false));
		byte[] buffer=new byte[1048];
		int readnum=is.read(buffer);
		while(readnum>=0){
			bout.write(buffer,0,readnum);
			readnum=is.read(buffer);
		}
		temp.setReadOnly();
		bout.close();
		return temp.getPath();
	}
	protected String converFilePathByProjectName(String projectName,String filepath)
	{
		MyProjectOption o=getOption(projectName);
		String prjRoot=o.getRootPath();
		return converFilePathByProjectPath(prjRoot,filepath);
	}
	protected String converFilePathByProjectPath(String prjRoot,String filepath)
	{
		if(prjRoot==null || prjRoot.length()==0) return filepath;
		
		int index=filepath.indexOf(prjRoot);
		if(index>=0){
			return filepath.substring(prjRoot.length()+1);
		}
		else
			return filepath;
	}
	/**
	conver projectitem by new rootPath
	*/
	protected void replaceProjectItemFilePath(String projectName,String oldRootPath,String newRootPath,QuickProject.ProjectItem pi)
	{
		if(oldRootPath==null)
			oldRootPath="";
		//if(pi instanceof QuickProject.ZipProjectItem){
		//}else{
		String filepath=null;
		if(isAbsolutePath(pi.filePath())){
			filepath=pi.filePath();
		}else{
			filepath=oldRootPath+File.separator+pi.filePath();
		}
		pi.resetDir(converFilePathByProjectPath(newRootPath,filepath));
		logger.fine(filepath);
		//}
	}
	public void handleMessage(EBMessage message){
	}

	private class OptionChangeListener implements PropertyChangeListener
	{
		public  void propertyChange(PropertyChangeEvent evt)
		{
			try{
				
				if("rootPath".equals(evt.getPropertyName())){
					MyProjectOption option=(MyProjectOption)evt.getSource();
					String name=option.getName();
					QuickProject qp=projects.get(name);
					qp.iterateAllItems(new ProjectItemsReplacer(name,(String)evt.getOldValue(),(String)evt.getNewValue()));
					qp.freshView();
					qp.save();
				}
			}catch(Exception ex){
				logger.log(Level.SEVERE,"Can't save options",ex);
			}
		}
	}
	
	private class ProjectItemsReplacer implements QuickProject.ProjectItemScanner
	{
		private String projectName;
		private String oldRootPath;
		private String newRootPath;
		private ProjectItemsReplacer(){}
		public ProjectItemsReplacer(String projectName,String oldRootPath,String newRootPath)
		{
			this.projectName=projectName;
			this.oldRootPath=oldRootPath;
			this.newRootPath=newRootPath;
		}
		public void processItem(QuickProject.ProjectItem item)
		{
			replaceProjectItemFilePath(projectName,oldRootPath,newRootPath,item);
		}
	}
}
