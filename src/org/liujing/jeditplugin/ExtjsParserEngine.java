package org.liujing.jeditplugin;

import java.io.*;
import java.util.*;
import org.liujing.util.SimpleTokenizer;
import java.util.logging.*;

public class ExtjsParserEngine
{
	private static Logger log=Logger.getLogger(ExtjsParserEngine.class.getName());
	private File targetFile=null;
	private String text;
	private LinkedList<Token> tokenCache=new LinkedList();
	
	public ExtjsParserEngine(File f){
		targetFile=f;
	}
	public ExtjsParserEngine(String text){
		this.text=text;
	}
	
	public void read()throws Exception
	{
		
		BufferedReader reader=new BufferedReader(new FileReader(targetFile));
		Scanner scanner=new Scanner(reader).useDelimiter("=");
		String found=scanner.findWithinHorizon("[\\s]*\\w*(\\.\\w*)*.*",100);
		System.out.println("scanner catch "+found);
		
		/*SimpleTokenizer tk=new SimpleTokenizer(reader);
		tk.wordChars('_','_');
		tk.wordChars('.','.');
		tk.slashStarComments(true);
		tk.slashSlashComments(true);
		next(tk);
		while(tk.token()!=null){
			
			if(tk.type()=='{'){
				
			}else{
				next(tk);
			}
		}*/
		reader.close();
	}
	
	protected void onToken(int level,String name)
	{
		log.info("token "+name);
		
	}
	
	private void next(SimpleTokenizer tk)throws Exception
	{
		tk.readNextToken();
		tokenCache.add(new Token(tk.type(),tk.token()));
		if(tokenCache.size()>10){
			tokenCache.removeFirst();
		}
	}
	public static class Token
	{
		public int type=0;
		public String token=null;
		public Token(int type,String tk)
		{
			this.type=type;
			this.token=tk;
		}
	}
	public static void main(String[] args)
	{
		try{
			ExtjsParserEngine ps=new ExtjsParserEngine(new File(args[0]));
			ps.read();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
