package org.liujing.awttools.classview;

import java.util.*;
import java.io.*;
import java.util.logging.*;
import java.util.regex.*;
import java.util.jar.*;
import javax.swing.text.AttributeSet;

public class ExplorerElement
{
	public static int NAME_STYLE=0;
	public static int LINK_STYLE=1;
	public static int ACCESS_STYLE1=2;
	
	public static int GENERIC_STYLE=3;
	public static int DEFAULT_STYLE=4;
	public static int NAME2_STYLE=0;
	public static int HIGH_STYLE=5;
	public static int ACCESS_STYLE2=6;
	public static int F_NAME_STYLE=7;
	public static int C_NAME_STYLE=8;
	
	private int style=0;
	protected int start=0;
	protected int end=0;
	private String text;
	private AttributeSet attr;
	
	/**
	create default element
	*/
	public ExplorerElement(int start,int end,String str)
	{
		this.style=DEFAULT_STYLE;
		this.start=start;
		this.end=end;
		this.text=str;
	}
	/**
	create specificed element
	*/
	public ExplorerElement(int style,int start,int end)
	{
		this.style=style;
		this.start=start;
		this.end=end;
	}
	public ExplorerElement(int style,int start,int end,String replaceStr)
	{
		this(style,start,end);
		this.text=replaceStr;
	}
	public int getStyle()
	{
		return style;
	}
	public int start()
	{
		return start;
	}
	public void setStart(int s)
	{
		start=s;
	}
	public void setEnd(int e)
	{
		end=e;
	}
	public int end()
	{
		return end;
	}
	public String getText()
	{
		return text;
	}
	/**
	 * Returns the value of attr.
	 */
	public AttributeSet getAttr()
	{
		return attr;
	}

	/**
	 * Sets the value of attr.
	 * @param attr The value to assign attr.
	 */
	public void setAttr(AttributeSet attr)
	{
		this.attr = attr;
	}
}
