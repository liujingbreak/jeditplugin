package org.liujing.jeditplugin;

import sidekick.*;

import java.util.*;
import java.io.*;
import org.gjt.sp.jedit.Buffer;
import javax.swing.tree.*;

public class ExtjsParser extends SideKickParser
{
	public ExtjsParser(){
		super("extjs");
		name="Extjs";
	}
	public SideKickParsedData parse(org.gjt.sp.jedit.Buffer buffer, errorlist.DefaultErrorSource errorSource)
	{
		SideKickParsedData data=new SideKickParsedData(buffer.getName());
		MutableTreeNode node=new DefaultMutableTreeNode("test");
		data.root.add(node);
		return data;
	}
}
