package org.liujing.jeditplugin;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.msg.*;
import org.liujing.util.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class MyContextHandle implements EBComponent
{
	
	private MyContextHandle instance=null;
	protected MyContextHandle()
	{
		
	}
	
	public MyContextHandle getInstance()
	{
		if(instance==null)
			instance=new MyContextHandle();
		return instance;
	}
	
	public void handleMessage(EBMessage message){
		
	}
}
