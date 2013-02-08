package org.liujing.parser;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import org.liujing.tools.compiler.*;
import java.util.regex.*;
import javax.swing.tree.*;

import sidekick.*;

public class PatchParser extends SideKickParser{
	private Logger log = Logger.getLogger(PatchParser.class.getName());

	public PatchParser(String name){
		super(name);
		//parser = new JavascriptParser();
	}
	public SideKickParsedData parse(org.gjt.sp.jedit.Buffer buffer, errorlist.DefaultErrorSource errorSource)
	{
		//try{
		//	return parser.parse(buffer,errorSource);
		//}catch(Exception e){
		//	log.log(Level.WARNING,"parse failed",e);
		//}
		return null;
	}

}
