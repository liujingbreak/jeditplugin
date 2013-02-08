package org.liujing.jeditplugin;

import org.liujing.awttools.classview.*;

public class MyJeditPluginFactory
{
	
	public synchronized static ClassSearchUI createClassSearchUI()
	{
		ClassSearchUI cui=new ClassSearchUI();
		Javaprint javap=new Javaprint();		
		cui.setClassScanner(javap);
		return cui;
	}
}

