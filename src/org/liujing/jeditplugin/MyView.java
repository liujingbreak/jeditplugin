package org.liujing.jeditplugin;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.msg.*;
import org.liujing.util.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class MyView
{
	protected ProjectAddFileDialog addDialog;
	
	private static Map<View,MyView> instances=new HashMap();
	protected MyView()
	{
		
	}
	
	public static MyView getInstance(View v)
	{
		MyView instance=instances.get(v);
		if(instance==null){
			instance=new MyView();
			instances.put(v,instance);
			//EditBus.addToBus(this);
		}
		return instance;
	}
	public static void removeInstance(View v)
	{
		instances.remove(v);
	}
	
	
	public void popupAddFileDialog(String path)
	{
		View v=jEdit.getActiveView();
		if(addDialog==null)
			addDialog=new ProjectAddFileDialog(v.getBuffer().getDirectory());
		addDialog.setPath(path);
		addDialog.setLocationRelativeTo(v);
		addDialog.setVisible(true);
		
	}
}
