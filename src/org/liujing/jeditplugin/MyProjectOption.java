package org.liujing.jeditplugin;

import org.liujing.util.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.beans.*;


public class MyProjectOption
{
	
	private static Logger log=Logger.getLogger(MyProjectOption.class.getName());
	private String rootPath;
	private String[] fileFilterReg;
	private String name;
	private String classpath;

	
	private final PropertyChangeSupport pcs = new PropertyChangeSupport( this );
	/**
	* indicate the project data always in memory
	*/
	private boolean stayMemory=false;
	
	
	public MyProjectOption()
	{
		fileFilterReg=new String[]{".*\\.java",".*\\.xml",".*\\.properties",".*\\.py",".*\\.jsp"};
	}
	public MyProjectOption(String projectName)
	{
		this();
		name=projectName;
	}
	public void load(File storeFile)throws IOException,FileNotFoundException
	{
		if(!storeFile.exists())
			return ;
		Properties props=new Properties();
		FileInputStream in=new FileInputStream(storeFile);
		props.load(in);
		rootPath=props.getProperty("root_path");
		stayMemory=Boolean.parseBoolean(props.getProperty("stay_memory"));
		setFilter(props.getProperty("filter"));
		classpath=props.getProperty("classpath");
		in.close();
	}
	public void store(File storeFile)throws IOException
	{
		Properties props=new Properties();
		props.setProperty("root_path",getRootPath());
		props.setProperty("stay_memory",Boolean.valueOf(isStayMemory()).toString());
		props.setProperty("filter",getFilter());
		props.setProperty("classpath",classpath);
		props.store(new FileOutputStream(storeFile,false),"");
	}
	/**
	 * Returns the value of rootPath.
	 */
	public String getRootPath()
	{
		return rootPath;
	}

	/**
	 * Sets the value of rootPath.
	 * @param rootPath The value to assign rootPath.
	 */
	public void setRootPath(String rootPath)
	{
		if(rootPath!=null && rootPath.length()>0){
			char lastchar=rootPath.charAt(rootPath.length()-1);
			if(lastchar=='/'||lastchar=='\\'){
				rootPath=rootPath.substring(0,rootPath.length()-1);
			}
		}
		if(!rootPath.equals(this.rootPath)){
			log.fine("rootPath changed");
			String oldRootPath=this.rootPath;
			this.rootPath = rootPath;
			pcs.firePropertyChange("rootPath",oldRootPath,rootPath);
		}
	}
	
	public void setFilter(String str)
	{
		fileFilterReg=str.split(",");
	}
	public String getFilter()
	{
		StringBuilder buf=new StringBuilder();
		if(fileFilterReg!=null){
			for(int i=0;i<fileFilterReg.length;i++){
				buf.append(fileFilterReg[i]);
				buf.append(",");
			}
		}
		buf.setLength(buf.length()-1);
		return buf.toString();
	}
		/**
	 * Returns the value of fileFilterReg.
	 */
	public String[] getFileFilterReg()
	{
		return fileFilterReg;
	}

	/**
	 * Sets the value of fileFilterReg.
	 * @param fileFilterReg The value to assign fileFilterReg.
	 */
	public void setFileFilterReg(String[] fileFilterReg)
	{
		this.fileFilterReg = fileFilterReg;
	}

	/**
	 * Returns the value of stayMemory=false.
	 */
	public boolean isStayMemory()
	{
		return stayMemory;
	}
	/**
	 * Returns the value of name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the value of name.
	 * @param name The value to assign name.
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	/**
	 * Sets the value of stayMemory=false.
	 * indicate the project data always in memory
	 * @param stayMemory=false The value to assign stayMemory=false.
	 */
	public void setStayMemory(boolean stayMemory)
	{
		this.stayMemory= stayMemory;
	}
	
	public void setClasspath(String cp)
	{
		classpath=cp;
	}
	
	public String getClasspath()
	{
		return classpath;
	}

	public void addPropertyChangeListener( PropertyChangeListener listener )
	{
		this.pcs.addPropertyChangeListener( listener );
	}
	
	public void removePropertyChangeListener( PropertyChangeListener listener )
	{
		this.pcs.removePropertyChangeListener( listener );
	}
}
