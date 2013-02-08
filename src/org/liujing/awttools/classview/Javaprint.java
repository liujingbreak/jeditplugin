package org.liujing.awttools.classview;

import java.util.*;
import java.io.*;
import java.util.logging.*;
import org.liujing.awttools.*;
import java.util.regex.*;
import java.util.jar.*;
import java.io.File;
import java.util.logging.Logger;
import java.io.InputStreamReader;
import java.util.jar.JarEntry;
import java.util.List;
import java.util.logging.Level;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Javaprint
{
	private static Logger log=Logger.getLogger(Javaprint.class.getName());
	private ProcessBuilder pb;
	private static String JAVAP_PATH;
	private String classpath;
	private Pattern pathPat=Pattern.compile("/|\\\\");
	private Pattern fullClassNmPat=Pattern.compile("[^\\*]+\\.[^\\*]+");
	private static File rt;
	private StringBuilder strBd=new StringBuilder(500);
	private boolean stopSearch=false;
	private static String classNameToken="[a-zA-Z_0-9\\.\\$]+";
	private static String typeToken="[a-zA-Z_0-9\\$\\.]+(\\[\\s*\\])?";
	private static String primTypeToken="(\\w+\\s+)?(void|int|byte|long|char|float|double|boolean)(\\s*\\[.*\\])*";
	//private static Pattern arrayTypePat=Pattern.compile(

	protected static Pattern primTypePat=Pattern.compile(primTypeToken);
	protected static Pattern headerPat=Pattern.compile(
		"\\s*Compiled from \"(.*)\"\\s*$",Pattern.MULTILINE);
	protected static Pattern classHeaderPat=Pattern.compile(
		"^.*(class|interface)\\s+([a-zA-Z_0-9\\.\\$]+)(\\s+extends\\s+("+classNameToken+"(?:,"+classNameToken+")*))?(\\s+implements\\s+("+classNameToken+"(?:,"+classNameToken+")*))?\\s*\\{",Pattern.MULTILINE);
	protected static Pattern methodPat=Pattern.compile(
		"^(\\s*)(?:(public|protected|private)\\s+)?(?:\\w+\\s+)*("
		+typeToken
		+")\\s+([\\w\\$_]+)\\s*\\(([^\\)]*)\\)\\s*(throws\\s+([a-zA-Z_0-9\\.,\\s]+))?;\\s*$"
		,Pattern.MULTILINE);

	protected static Pattern fieldPat=Pattern.compile(
		"^(\\s*)(?:(public|protected|private)\\s+)?(?:\\w+\\s+)*("+typeToken+")\\s+([\\w_\\$]+)\\s*;\\s*$",Pattern.MULTILINE);
	private String[] command;
	static{
		JAVAP_PATH="\""+System.getenv("JAVA_HOME")+"\\bin\\javap\"";
		rt=new File(System.getProperty("java.home")+File.separator+"lib","rt.jar");
	}

	private LinkExplorer exp;
	public Javaprint()
	{
		command=new String[]{JAVAP_PATH,"-classpath",null,
				null};
		//File rt=new File(System.getProperty("java.home")+File.separator+"lib","rt.jar");
	}
	public Javaprint(String classpath)
	{
		this();
		setClasspath(classpath);

	}

	/**
	 * Returns the value of classpath.
	 */
	public String getClasspath()
	{
		return classpath;
	}

	/**
	 * Sets the value of classpath.
	 * @param classpath The value to assign classpath.
	 */
	public void setClasspath(String classpath)
	{
		this.classpath = classpath;
		command[command.length-2]=classpath;
	}
	public void setExplorer(LinkExplorer exp)
	{
		this.exp=exp;
	}
	private String getEnv(String name)
	{
		return null;
	}
	private void search(String className)
	{

		className=className.replaceAll("\\*","\\\\E\\.*\\\\Q");
		className="\\Q"+className+"\\E";
		//className=className.toLowerCase();
		Pattern p=Pattern.compile(className,Pattern.CASE_INSENSITIVE);
		if(getClasspath()!=null&& getClasspath().length()>0){
			searchClass(p);
		}
		searchInJre(p);
	}
	public void print(String classname)throws Exception
	{
		stopSearch=false;
		exp.cleanup();
		String[] cmd=command;
		classname=classname.trim();
		if(classname.length()==0) return ;

		Matcher m=fullClassNmPat.matcher(classname);
		if(!m.matches()){
			search(classname);
			return;
		}
		//JAVAP_PATH=System.getenv("JAVA_HOME")+"\\bin\\javap";
		command[command.length-1]=classname;
		if(getClasspath()==null || getClasspath().length()==0)
		{
			//remove the argument "-classpath"
			cmd=new String[2];
			cmd[0]=command[0];
			cmd[1]=command[command.length-1];
		}

		if(pb==null){
			pb=new ProcessBuilder(cmd);
		}else{
			pb.command(cmd);
		}
		pb.redirectErrorStream(true);
		Process p=pb.start();
		//StringWriter sw=new StringWriter();
		strBd.setLength(0);
		InputStreamReader is=new InputStreamReader(p.getInputStream());
		//Scanner scn=new Scanner(is);
		try{

			int c=is.read();
			while(c!=-1){
				strBd.append((char)c);
				c=is.read();
			}
			p.waitFor();

		}catch(Exception ex){
			throw ex;
		}finally{
			is.close();
		}

		int code=p.exitValue();
		if(code!=0){
			exp.printDefault(strBd.toString());
			exp.printDefault("\nSearch result:\n");
			search(classname);
			return;
			//strBd.append("\n----command------\n");
			//for(String c:command){
			//	strBd.append(c);
			//	strBd.append("\n");
			//}
			//throw new Exception("return code="+code+"\n"+strBd.toString());
		}
		parseDoc(strBd);

		return ;
	}

	protected void parseDoc(CharSequence cs)
	{
		int start=0;
		ArrayList<ExplorerElement> alist=new ArrayList<ExplorerElement>();
		Matcher m=headerPat.matcher(cs);
		if(m.find()){
			//log.info("fine header from "+m.start()+" to "+m.end());
			alist.add(new ExplorerElement(ExplorerElement.C_NAME_STYLE, m.start(1),m.end(1)));
			start=m.end();
		}
		String fullClassName=null;
		m=classHeaderPat.matcher(cs);

		if(m.find(start)){
			fullClassName=m.group(2);
			String extendsTk=m.group(3);
			alist.add(new ExplorerElement(ExplorerElement.C_NAME_STYLE,m.start(2),m.end(2)));
			if(extendsTk!=null){

				alist.add(new ExplorerElement(
					m.start(3),m.start(3),"\n"));
				String clssNm=m.group(4).trim();
				String[] ss=clssNm.split(",");
				for(int i=0;i<ss.length;i++){
					if(i!=0){
						alist.add(new ExplorerElement(m.start(4),m.end(4),","));
					}
					String s=ss[i];
					s=s.trim();
					LinkElement e=new LinkElement(m.start(4),m.end(4),
						shortenClassName(s));
					e.setTipText(s);
					e.setHref(s);
					alist.add(e);
				}
			}
			int implementsGpNum=5;
			int implementsLsGpNum=implementsGpNum+1;
			String implementsTk=m.group(implementsGpNum);
			if(implementsTk!=null){
				alist.add(new ExplorerElement(
					m.start(implementsGpNum),m.start(implementsGpNum),"\n"));

				String[] ss=m.group(implementsLsGpNum).split(",");
				for(int i=0;i<ss.length;i++){
					if(i!=0){
						alist.add(new ExplorerElement(m.start(implementsLsGpNum),m.end(implementsLsGpNum),","));
					}
					String s=ss[i];
					s=s.trim();
					LinkElement e=new LinkElement(m.start(implementsLsGpNum),m.end(implementsLsGpNum),
						shortenClassName(s));
					e.setTipText(s);
					e.setHref(s);
					alist.add(e);

				}
			}

			//log.info("fine class from "+m.start()+" to "+m.end()+"\nname: "+m.group(1));
			//log.info("extends "+m.group(3));
			//log.info("implements "+m.group(6));

		}else{
			log.warning("Not find the class header definition matches "+classHeaderPat.pattern());
			exp.print(cs,0,cs.length(),alist);
			return;
		}
		start=m.end();

		int constructorOffset=start;
		m=methodPat.matcher(cs);
		//------------------------Methods analys---------------------

		while(m.find(start)){
			if(m.group(1)!=null){
				alist.add(new ExplorerElement(m.start(1),m.end(1),""));
			}
			int accessGpNum=2;
			String access=m.group(accessGpNum);
			if(access!=null){
				int accessSyntax=ExplorerElement.ACCESS_STYLE1;
				if(access.equals("public"))
					accessSyntax=ExplorerElement.ACCESS_STYLE1;
				else if(access.equals("protected"))
					accessSyntax=ExplorerElement.ACCESS_STYLE2;
				alist.add(new ExplorerElement(accessSyntax,m.start(accessGpNum),m.end(accessGpNum)));
			}
			int typeGpNm=3;
			String type=m.group(typeGpNm);
			//log.info("type="+type);

			Matcher primM=primTypePat.matcher(type);
			if(!primM.matches()){
				String shortNm=shortenClassName(type);
				String text=null;
				LinkElement elm=null;
				int endOffset=m.end(typeGpNm);
				if(m.group(typeGpNm+1)!=null){
					endOffset=m.start(typeGpNm+1);
					//it's an array type, need to skip sign "[]"
				}
				text=cs.subSequence(m.start(typeGpNm),endOffset).toString().trim();
				shortNm=shortenClassName(text);
				elm=new LinkElement(m.start(typeGpNm),endOffset,shortNm);
				elm.setTipText(text);
				elm.setHref(text);
				alist.add(elm);
			}
			int methodNameGpNm=typeGpNm+2;
			//log.info(cs.subSequence( m.start(methodNameGpNm),m.end(methodNameGpNm)).toString());
			alist.add(new ExplorerElement(ExplorerElement.NAME_STYLE,m.start(methodNameGpNm),m.end(methodNameGpNm)));


			int argumentsGpNm=methodNameGpNm+1;
			String arguments=m.group(argumentsGpNm).trim();
			if(arguments.length()>0){
				parseArguments(arguments,m.start(argumentsGpNm),m.end(argumentsGpNm),alist);
			}

			int throwsListGpNm=argumentsGpNm+2;
			String throwsList=m.group(throwsListGpNm);
			if(throwsList!=null){
				int offset=m.end(argumentsGpNm)+1;
				alist.add(new ExplorerElement(ExplorerElement.HIGH_STYLE,m.end(argumentsGpNm)+1,m.end(argumentsGpNm)+1,"throws "));

				String[] ss=throwsList.split(",");
				for(int i=0;i<ss.length;i++){
					if(i!=0){
						alist.add(new ExplorerElement(offset,m.end(throwsListGpNm),","));
					}
					String s=ss[i];
					s=s.trim();
					LinkElement e=new LinkElement(offset,m.end(throwsListGpNm),
						shortenClassName(s));
					e.setTipText(s);
					e.setHref(s);
					alist.add(e);

				}
			}
			start=m.end();
		}
		Pattern constructorPat=Pattern.compile(
			"^(\\s*)(?:(public|protected|private)\\s+)?(?:\\w+\\s+)*("+fullClassName+")\\s*\\((.*)\\)\\s*(throws\\s+([a-zA-Z_0-9\\.,]+))?;\\s*$"
		,Pattern.MULTILINE);

		m=constructorPat.matcher(cs);

		//--constructor
		String shortClassNm=shortenClassName(fullClassName);
		while(m.find(constructorOffset)){
			if(m.group(1)!=null){
				alist.add(new ExplorerElement(m.start(1),m.end(1),""));
			}
			int accessGpNum=2;
			String access=m.group(accessGpNum);
			if(access!=null){
				int accessSyntax=ExplorerElement.ACCESS_STYLE1;
				if(access.equals("public"))
					accessSyntax=ExplorerElement.ACCESS_STYLE1;
				else if(access.equals("protected"))
					accessSyntax=ExplorerElement.ACCESS_STYLE2;
				alist.add(new ExplorerElement(accessSyntax,m.start(accessGpNum),m.end(accessGpNum)));
			}

			alist.add(new ExplorerElement(ExplorerElement.C_NAME_STYLE,m.start(3),m.end(3),shortClassNm));

			String arguments=m.group(4).trim();
			if(arguments.length()>0)
				parseArguments(arguments,m.start(4),m.end(4),alist);

			String throwsList=m.group(5);
			if(throwsList!=null){
				alist.add(new ExplorerElement(ExplorerElement.HIGH_STYLE,m.end(4)+1,m.start(5)+6,"throws"));
			}

			constructorOffset=m.end();
		}
		//----------fields--------------
		m=fieldPat.matcher(cs);
		while(m.find()){
			if(m.group(1)!=null){
				alist.add(new ExplorerElement(m.start(1),m.end(1),""));
			}
			int accessGpNum=2;
			String access=m.group(accessGpNum);
			if(access!=null){
				int accessSyntax=ExplorerElement.ACCESS_STYLE1;
				if(access.equals("public"))
					accessSyntax=ExplorerElement.ACCESS_STYLE1;
				else if(access.equals("protected"))
					accessSyntax=ExplorerElement.ACCESS_STYLE2;
				alist.add(new ExplorerElement(accessSyntax,m.start(accessGpNum),m.end(accessGpNum)));
			}
			int typeGpNm=3;
			String type=m.group(typeGpNm);
			Matcher primM=primTypePat.matcher(type);
			if(!primM.matches()){
				String shortNm=shortenClassName(type);
				String text=null;
				LinkElement elm=null;
				int endOffset=m.end(typeGpNm);
				if(m.group(typeGpNm+1)!=null){
					endOffset=m.start(typeGpNm+1);
					//it's an array type, need to skip sign "[]"
				}
				text=cs.subSequence(m.start(typeGpNm),endOffset).toString().trim();
				shortNm=shortenClassName(text);
				elm=new LinkElement(m.start(typeGpNm),endOffset,shortNm);
				elm.setTipText(text);
				elm.setHref(text);
				alist.add(elm);
			}
			int nameGpNm=5;
			alist.add(new ExplorerElement(ExplorerElement.F_NAME_STYLE,m.start(nameGpNm),m.end(nameGpNm)));
		}
		exp.print(cs,0,cs.length(),alist);

		//exp.printDefault(cs.toString());
	}
	private void parseArguments(String arguments,int startOfBuf,int endOfBuf,List alist)
	{
		String[] args=arguments.split(",");
		for(int i=0;i<args.length;i++){
			String type=args[i].trim();

			if(i!=0)
				alist.add(new ExplorerElement(startOfBuf,endOfBuf,", "));
			Matcher primM=primTypePat.matcher(type);

			if(primM.matches()){
				// a prim type
				ExplorerElement exe=new ExplorerElement(startOfBuf,endOfBuf,type);
				alist.add(exe);
			}else{//not a prim type
				String shortNm=null;
				int arOffset=type.indexOf("[");
				if(arOffset>=0){
					//array type
					type=type.substring(0,arOffset).trim();
					shortNm=shortenClassName(type);
					//insert type link
					LinkElement elm=new LinkElement(startOfBuf,endOfBuf,shortNm);
					elm.setTipText(type);
					elm.setHref(type);
					alist.add(elm);
					//insert '[]'
					ExplorerElement exe=new ExplorerElement(startOfBuf,endOfBuf,"[]");
					alist.add(exe);
				}else{
					shortNm=shortenClassName(type);
					LinkElement elm=new LinkElement(startOfBuf,endOfBuf,shortNm);
					elm.setTipText(type);
					elm.setHref(type);
					alist.add(elm);
				}
			}
		}
	}
	private boolean isStopSearch()
	{
		return stopSearch;
	}
	public void setStopSearch(boolean b)
	{
		stopSearch=b;
	}

	private void searchClass(Pattern Pattern)
	{
		String[] paths=classpath.split(File.pathSeparator);
		for(String path:paths){
			File f=new File(path);
			if(!f.exists()){
				exp.printDefault(path+" doesn't exists!\n");
				continue;
			}
			if(f.isFile()){
				try{
					JarFile zip=new JarFile(f);
					Enumeration<JarEntry> en=zip.entries();
					String fileName=null;
					int fCount=0;
					while(en.hasMoreElements()&& !isStopSearch()){
						JarEntry ze=en.nextElement();
						if(ze.isDirectory())
							continue;
						fileName=ze.toString();
						if(fileName.length()<6 || !fileName.substring(fileName.length()-6).
							equalsIgnoreCase(".class"))
							continue;
						//if(fileName.contains("$"))
						//	continue;
						fileName=convertPath2Clss(fileName);
						Matcher m=Pattern.matcher(fileName);
						if(m.matches()){
							drawClassLink(fileName);
							fCount++;
						}
					}
					if(fCount>0){
						exp.printDefault(fCount+" found in "+f.getPath()+"\n");
					}
				}catch(Exception ex){
					log.log(Level.SEVERE,"Failed to seach class in a JAR file "+ f.getPath(),ex);
				}
			}else{
				int c=searchClassInDir(f,f.getPath().length(),Pattern);
				if(c>0){
					exp.printDefault(c+" found in "+f.getPath()+"\n");
				}
			}
		}
	}
	private int searchClassInDir(File dir,int rootPathLen,Pattern Pattern)
	{
		int fCount=0;
		File[] fs=dir.listFiles();
		//log.info("dir="+dir.getPath());
		if(isStopSearch()) return 0;
		if(fs==null) return 0;
		for(File f: fs){
			if(isStopSearch()) return fCount;
			if(f.isFile()){
				if(!f.getName().toLowerCase().endsWith(".class"))
					continue;
				//if(f.getName().contains("$"))
				//	continue;
				//log.info(f.getPath());
				//log.info("len="+rootPathLen);
				String path=f.getPath().substring(rootPathLen+1);
				String fileName=convertPath2Clss(path);
				Matcher m=Pattern.matcher(fileName);
				if(m.matches()){
					fCount++;
					drawClassLink(fileName);
				}
			}else if(f.isDirectory()){
				fCount+=searchClassInDir(f,rootPathLen,Pattern);
			}
		}
		return fCount;
	}
	private void searchInJre(Pattern Pattern)
	{
		try{
			JarFile zip=new JarFile(rt);
			Enumeration<JarEntry> en=zip.entries();
			String fileName=null;
			int fCount = 0;
			while(en.hasMoreElements()&& ! isStopSearch()){
				JarEntry ze=en.nextElement();
				if(ze.isDirectory())
					continue;
				fileName=ze.toString();
				//if(fileName.contains("$"))
				//	continue;
				if(fileName.length()<6 || !fileName.substring(fileName.length()-6).
					equalsIgnoreCase(".class"))
					continue;
				fileName=convertPath2Clss(fileName);
				Matcher m=Pattern.matcher(fileName);
				if(m.matches()){
					drawClassLink(fileName);
					fCount++;
				}

			}
			if(fCount>0){
				exp.printDefault(fCount+" found in "+rt.getPath()+"\n");
			}
		}catch(Exception ex){
			log.log(Level.SEVERE,"Can't seach class in a JRE "+rt.getPath(),ex);
		}
	}
	private String convertPath2Clss(String path)
	{
		String clazz=pathPat.matcher(path).replaceAll(".");
		if(clazz.substring(clazz.length()-6).equalsIgnoreCase(".class")){
			clazz=clazz.substring(0,clazz.length()-6);
		}
		return clazz;
	}
	private String shortenClassName(String fullName)
	{
		String className=null;
		int dot=fullName.lastIndexOf(".");
		if(dot>=0){
			//packageName=s.substring(0,dot+1);
			className=fullName.substring(dot+1);
		}
		else{
			//packageName="";
			className=fullName;
		}
		return className;
	}
	protected void drawClassLink(String path)
	{
		String packageName=null;
		String className=null;
		String s=path;
		int dot=s.lastIndexOf(".");
		if(dot>=0){
			packageName=s.substring(0,dot+1);
			className=s.substring(dot+1);
		}
		else{
			packageName="";
			className=s;
		}
		//log.info("packageName,className");
		exp.addCompoundLink(packageName,className,s);
		exp.printDefault("\n");
	}
}
