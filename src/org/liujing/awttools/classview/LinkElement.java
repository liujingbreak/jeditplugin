package org.liujing.awttools.classview;

import java.util.*;
import java.io.*;
import java.util.logging.*;
import java.util.regex.*;
import java.util.jar.*;

public class LinkElement extends ExplorerElement
{
	private String tipText;
	private String href;
	public LinkElement(int start,int end,String text)
	{
		super(ExplorerElement.LINK_STYLE,start,end,text);
	}
	//public LinkElement(int start,int end,String setTipText,String)
	//{
	//	super(ExplorerElement.LINK_STYLE,start,end);
	//}
	public LinkElement(int start,int end)
	{
		super(ExplorerElement.LINK_STYLE,start,end);
	}
	public void setTipText(String s)
	{
		tipText=s;
	}
	
	public void setHref(String s)
	{
		href=s;
	}
	
	public String getTipText()
	{
		return tipText;
	}
	public String getHref()
	{
		if(href==null)
			href=getText();
		return href;
	}
}
