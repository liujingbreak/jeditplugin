package org.liujing.jeditplugin;

//import org.gjt.sp.jedit.*;
//import org.gjt.sp.jedit.msg.*;
import org.liujing.util.*;
import java.util.*;
import java.util.logging.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

/**
@author liujing.nono@gmail.com
*/
public class QuickProject<E>
{
	private static LogThread log=new LogThread(QuickProject.class);
	private static Logger logger=Logger.getLogger(QuickProject.class.getName());
	//protected LinkedList list;
	//protected TreeMap map;
	protected String name;
	protected int size;
	protected MyTableModel<E> tableModel;
	protected IndexTreeNode indexRoot=new IndexTreeNode((char)0);
	protected boolean isNew=true;

	protected File[] importedFiles=null;
	/**
	Basic constructor for QuickProject
	*/
	public QuickProject(String name)
	{
		setName(name);
		//logger.setLevel(Level.FINEST);
		//list=new LinkedList();
		//map=new TreeMap(new Comp());
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
	public void setNew(boolean isNew)
	{
		this.isNew=isNew;
	}
	public boolean isNew()
	{
		return isNew;
	}
	/**
	@return false if the item already exists
	*/
	public boolean add(String item)
	{
		return indexRoot.addEntry(new ProjectItem(item));
	}
	
	public boolean add(String zipFilePath,String zipItem)
	{
		return indexRoot.addEntry(new ZipProjectItem(zipFilePath,zipItem));
	}
	public void iterateAllItems(ProjectItemScanner sc)
	{
		indexRoot.scanTree(sc);
	}
	protected void scan(List list){
		list.clear();
		indexRoot.scanTree(list);
	}
	protected void scan(List list,String path){
		list.clear();
		IndexTreeNode node=indexRoot.getNode(path);
		if(node!=null)
			node.scanTree(list);
	}
	protected void scan(){
		indexRoot.scanTree();
	}
	public void freshView()
	{
		tableModel.fireTableRowsDeleted(0,tableModel.getRowCount());
		scan(tableModel.buffer);
		tableModel.fireTableRowsInserted(0,tableModel.getRowCount());
	}
	public void freshView(String path)
	{
		tableModel.fireTableRowsDeleted(0,tableModel.getRowCount());
		scan(tableModel.buffer,path);
		tableModel.fireTableRowsInserted(0,tableModel.getRowCount());
	}


	public void remove(int[] ids)throws Exception
	{
		Arrays.sort(ids);
		for(int index:ids){
			ProjectItem o=get(index);
			indexRoot.removeValue(o.getName(),o);
		}
	}
	/*public void renameItem(int index,String newName)throws Exception
	{
		File f=new File((String)get(index));
		File nf=new File(newName);
		nf.getParentFile().mkdirs();
		if(f.renameTo(nf)){
			remove(new int[]{index},false);
			add(newName);
			freshView();
			save();
		}else{
			logger.log(Level.SEVERE,"Can't rename file");
		}
		//logger.fine(f.getName());
	}*/
	/**
	@return -1 if not found
	*/
	public int quickIndexOf(String itemPrefix)
	{
		return -1;
	}

	public TableModel tableModel()
	{
		
		if(tableModel==null){
			List ls=new ArrayList();
			tableModel=new MyTableModel(ls);
			indexRoot.scanTree(ls);
		}
		return tableModel;
	}
	public ProjectItem get(int i)
	{
		return (ProjectItem)tableModel.buffer.get(i);
	}
	public void save()throws Exception
	{
		File f=MyJeditPlugin.getInstance().getPluginHome();
		f.mkdirs();
		//log.debug("edit home:"+f.getPath());
		BufferedWriter w=new BufferedWriter(new FileWriter(new File(f,getName()+".prj")));
		indexRoot.scanTree(w);
		w.close();
	}
	
	/**
	@return true if project exists
	*/
	public boolean load(String rootPath)
	{
		try{
			File f=MyJeditPlugin.getInstance().getPluginHome();
			f.mkdirs();
			File readF=new File(f,getName()+".prj");
			if(!readF.exists()){
				setNew(true);
				return false;
			}
			setNew(false);
			BufferedReader r=new BufferedReader(new FileReader(readF));
			String s=r.readLine();
			while(s!=null){
				if(s.startsWith("+")){
					indexRoot.addEntry(new ZipProjectItem(s.substring(1)));
				}
				else{
					//f=new File(s);
					//IndexTreeNode.addEntry(indexRoot,f.getName(),f);
					ProjectItem pi=new ProjectItem(s);
					indexRoot.addEntry(pi);
				}
				s=r.readLine();
			}
			r.close();
			return true;
		}catch(Exception e){
			logger.log(Level.SEVERE,"",e);
			return false;
		}
	}
	public void sortByItemDir()
	{
		tableModel.fireTableRowsDeleted(0,tableModel.getRowCount());
		Collections.sort(tableModel.buffer,new Comparator(){
				public int compare(Object o1,Object o2)
				{
					String dir1=((ProjectItem)o1).getDir();
					String dir2=((ProjectItem)o2).getDir();
					return dir1.compareTo(dir2);
				}
				public boolean equals(Object obj){
					return this==obj;
				}
		});
		tableModel.fireTableRowsInserted(0,tableModel.getRowCount());
	}
	private static class MyTableModel<E> extends AbstractTableModel
	{
		public List<E> buffer;
		
		/**
		Basic constructor for MyTableModel
		*/
		public MyTableModel(List list)
		{
			buffer=list;
		}
		
		public int getColumnCount()
		{
			return 2;
		}
		public int getRowCount()
		{
			return buffer.size();
		}
		public Object getValueAt(int rowIndex,
                  int columnIndex)
		{
			Object o=buffer.get(rowIndex);
			if(o instanceof ZipProjectItem){
				ZipProjectItem p=(ZipProjectItem)o;
				if(columnIndex==0){
					return p.getName();
				}else{
					return p.item+" ("+p.getProjectDir()+")";
				}
			}else if(o instanceof ProjectItem){
				ProjectItem p=(ProjectItem)o;
				if(columnIndex==0){
					return p.getName();
				}else{
					return p.getDir();
				}
			}
			return "";
		}
		public String getColumnName(int c)
		{
			switch(c){
				case 0:
				return "File Name";
				case 1:return "Path";
			}
			return "";
		}
	}

	/*private static class Entry<E> {
	E element;
	Entry<E> next;
	Entry<E> previous;

	Entry(E element, Entry<E> next, Entry<E> previous) {
	    this.element = element;
	    this.next = next;
	    this.previous = previous;
	}
    }*/
    	public static class ProjectItem
	{
		private String name;
		private String dir;
		

		
		private ProjectItem()
		{
		}
		public ProjectItem(String path)
		{
			resetDir(path);
		}
		public void resetDir(String path)
		{
			int idx=path.lastIndexOf(File.separator);
			if(idx<0){
				name=path;
				dir="";
			}
			else{
				name=path.substring(idx+1);
				dir=path.substring(0,idx);
			}
		}
		
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
			this.name =  name;
		}
		/**
		 * Returns the value of dir.
		 */
		public String getDir()
		{
			return dir;
		}
		/**
		 * Sets the value of dir.
		 * @param dir The value to assign dir.
		 */
		public void setDir(String dir)
		{
			this.dir = dir;
		}
		public String filePath()
		{
			return dir+File.separator+name;
		}
		public boolean equals(Object obj) {
			if(obj instanceof ProjectItem)
			{
				ProjectItem item=(ProjectItem)obj;
				logger.fine("this name="+this.name+", this dir="+this.dir+"item="+item);
				return item.name.equals(name) && item.dir.equals(dir);
			}
			else{
				return (this == obj);
			}
		}
		public void writeTo(Writer w)throws IOException
		{
			w.write(dir);
			w.write(File.separator);
			w.write(name);
		}
		public String toString()
		{
			return dir+File.separator+name;
		}
	}
    	public static class ZipProjectItem extends ProjectItem
	{
		private String projectDir;//zip dir
		public Object item;

		public ZipProjectItem(String name,String projectDir,Object item)
		{
			setName(name);
			this.item=item;
			this.projectDir=projectDir;
		}
		public ZipProjectItem(String path)
		{
			loadFromLine(path);
		}
		public ZipProjectItem(String zipFilePath,String zipItem)
		{
			int i=zipItem.lastIndexOf('/');
			int j=zipItem.lastIndexOf('\\');
			i=i>j?i:j;
			String itemName=zipItem.substring(i+1);
			setName(itemName);
			projectDir=zipFilePath;
			item=zipItem.substring(0,i+1);
		}
		/**
		set new path
		*/
		public void resetDir(String path)
		{
			projectDir=path;
		}
		/**
		 * Returns the value of dir.
		 */
		public String getProjectDir()
		{
			return projectDir;
		}
		
		public boolean equals(Object obj) {
			if(obj instanceof ZipProjectItem){
				ZipProjectItem pi=(ZipProjectItem) obj;
				return item.equals(pi.item);
			}
			else{
				return (this == obj);
			}
		}
		public String filePath()
		{
			return projectDir;
		}
		public void writeTo(Writer w)throws IOException
		{
			w.append('+');
			w.write(getProjectDir());
			w.append(':');
			w.write((String)item);
			w.write(getName());
		}
		public String getDir()
		{
			return projectDir+item;
		}
		public String toString()
		{
			StringBuilder tempSb=new StringBuilder();
			tempSb.setLength(0);
			tempSb.append('+');
			tempSb.append(projectDir);
			tempSb.append(':');
			tempSb.append((String)item);
			//tempSb.append(File.separatorChar);
			tempSb.append(getName());
			return tempSb.toString();
		}
		protected void loadFromLine(String line)
		{
			int p=line.lastIndexOf(":");
			int i=line.lastIndexOf('/');
			int j=line.lastIndexOf('\\');
			int p1=i>j?i:j;
			//int p1=s.lastIndexOf(File.separator);
			String dir=line.substring(0,p);
			String item=line.substring(p+1,p1+1);
			String itemName=line.substring(p1+1);
			//(itemName,dir,item)
			setName(itemName);
			this.item=item;
			this.projectDir=dir;
		}
	}
 	private static class LetterComparator implements Comparator<Character>
	{
		boolean ignoreCase=true;
		/**
		Basic constructor for LetterComparator
		*/
		public LetterComparator(boolean ignoreCase)
		{
			this.ignoreCase=ignoreCase;
		}

		public int compare(Character f1,Character f2)
		{
			char c1=f1.charValue();
			char c2=f2.charValue();
			if(ignoreCase){
				if(Character.isLowerCase(c1) ){
					c1=Character.toUpperCase(c1);
				}
				if(Character.isLowerCase(c2) ){
					c2=Character.toUpperCase(c2);
				}
			}
			//log.debug("c1="+c1+" c2="+c2+" c1-c2="+(c1-c2));
			return c1-c2;
		}
	}
    private static class IndexTreeNode
    {
	   
	    public char c;//index
	    public Object values=null;
	    public TreeMap<Character,IndexTreeNode> nexts;
	    public static LetterComparator comparetor=new LetterComparator(true);
	    
	    //protected StringBuilder tempSb=new StringBuilder();
	    
	    public IndexTreeNode(char c)
	    {
		    this.c=c;
		   
	    }
	    /*public IndexTreeNode (char c,Object v)
	    {
		    this(c);
		    value=v;
	    }*/
	    /**
	    @return false if the value already exists
	    */
	    public boolean addValue(ProjectItem o)
	    {
		if(values==null){
			//values=new ArrayList(1);
			//values.add(o);
			values=o;
			return true;
		}else if(values instanceof java.util.List){
			java.util.List list=(java.util.List)values;
			if(list.contains(o)){
				return false;
			}
			list.add(o);
			return true;
		}
		else{
			if(o.equals(values)){
				return false;
			}
			java.util.List list=new ArrayList(2);
			list.add(values);
			list.add(o);
			values=list;
			return true;
		}
		//return false;
	    }
	    public IndexTreeNode getNext(char c)
	    {
		    if(nexts==null) return null;
		    return nexts.get(Character.valueOf(c));
	    }
	    
	    
	    /**
	     if next node already exists, it will return the exist node
	    */
	    public IndexTreeNode createNext(char c)
	    {
		    if(nexts==null){
			    // TODO:
			     nexts=new TreeMap(comparetor);
		    }
		    Character ca=Character.valueOf(c);
		    IndexTreeNode n=nexts.get(ca);
		    if(n==null){
			    n=new IndexTreeNode(c);
			    nexts.put(ca,n);
		    }
		    return n;
	    }
	     /**
	     @return false if the entry already exists
	    */
	    public boolean addEntry(ProjectItem entry)
	    {
		IndexTreeNode root=this;
		String indexName=entry.getName();
		int l=0;
		do{
			char c=indexName.charAt(l++);
			root=root.createNext(c);
		}while(indexName.length()>l);
		return root.addValue(entry);
		
	    }
	    
	    public static boolean addEntry(IndexTreeNode root,ProjectItem entry)
	    {
		int l=0;
		String indexName=entry.getName();
		do{
			char c=indexName.charAt(l++);
			root=root.createNext(c);
		}while(indexName.length()>l);
		return root.addValue(entry);
		
	    }

	    public void scanTree(ProjectItemScanner sc){
		if(values!=null){
			if(values instanceof java.util.List){
				java.util.List list=(java.util.List)values;
				for(Object o:list){
					sc.processItem((ProjectItem)o);
				}
			}else{
				sc.processItem((ProjectItem)values);
			} 
		}
		if(nexts==null) return;
		//log.debug("scan tree:"+result.size());
		Iterator<IndexTreeNode> children=nexts.values().iterator();
		while(children.hasNext()){
			IndexTreeNode ch=children.next();
			ch.scanTree(sc);
		}
	    }
	    public void scanTree(){
		if(values!=null){
			
			if(values instanceof java.util.List){
				java.util.List list=(java.util.List)values;
				for(Object o:list)
					log.debug("~~ "+o);
			}
			else{
				log.debug("~~ "+values);
			}
		}
		if(nexts==null) return;
		Iterator<IndexTreeNode> children=nexts.values().iterator();
		while(children.hasNext()){
			IndexTreeNode ch=children.next();
			
			scanTree();
		}
	    }
	    public void scanTree(BufferedWriter w)throws IOException
	    {
		    ProjectItem it=null;
		if(values!=null){
			
			if(values instanceof java.util.List){
				java.util.List list=(java.util.List)values;
				for(Object o:list){
					it=(ProjectItem)o;
					it.writeTo(w);		
					w.newLine();
				}
			}else{
				it=(ProjectItem)values;
				it.writeTo(w);		
				w.newLine();
			}
		}
		if(nexts==null) return;
		Iterator<IndexTreeNode> children=nexts.values().iterator();
		while(children.hasNext()){
			IndexTreeNode ch=children.next();
			ch.scanTree(w);
		}
	    }
	    
	    public List scanTree(List result){		
		if(values!=null){
			//log.debug("values:"+values);
			if(values instanceof java.util.List){
				java.util.List list=(java.util.List)values;
				for(Object o:list){
					result.add(o);
				}
			}else{
				result.add(values);
			} 
		}
		if(nexts==null) return result;
		//log.debug("scan tree:"+result.size());
		Iterator<IndexTreeNode> children=nexts.values().iterator();
		while(children.hasNext()){
			IndexTreeNode ch=children.next();
			ch.scanTree(result);
		}
		return result;
	    }
	    public IndexTreeNode getNode(String path)
	    {		
		if(path==null || path.length()==0)return this;
		int l=0;
		IndexTreeNode root=this;
		do{
			char c=path.charAt(l++);
			if(root.nexts==null) break;
			root=root.nexts.get(Character.valueOf(c));
			if(root==null) break;
		}while(path.length()>l);
		return root;
	    }
	    public boolean removeValue(String path,Object toRemove)
	    {
		    IndexTreeNode node=getNode(path);
		    if(node==null) return false;
		    if(node.values==null) return false;
		    if(node.values instanceof List){
			List list=(List)node.values;
			return list.remove(toRemove);
		    }else{
			    if(node.values.equals(toRemove)){
				    node.values=null;
				    return true;
			    }
		    }
		    return false;
	    }
	    
    }
    
    public interface ProjectItemScanner
    {
	    public void processItem(ProjectItem item);
    }

}
