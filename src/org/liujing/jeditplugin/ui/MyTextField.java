package org.liujing.jeditplugin.ui;
import javax.swing.*;
import org.liujing.awttools.*;
import javax.swing.text.*;

public class MyTextField extends JTextField
{
	private static TextFieldContextMenu contexMenu=new TextFieldContextMenu();
	public MyTextField()
	{
		contexMenu.addBoundComponent(this);
	}
	public MyTextField(String text)
	{
		super(text);
		contexMenu.addBoundComponent(this);
	}
	public MyTextField(Document doc,
                  String text,
                  int columns)
	{
		super(doc,text,columns);
		contexMenu.addBoundComponent(this);
	}
	
	public MyTextField(String text,
                  int columns)
	{
		super(text,columns);
		contexMenu.addBoundComponent(this);
	}
	
	public MyTextField(int columns)
	{
		super(columns);
		contexMenu.addBoundComponent(this);
	}
}
