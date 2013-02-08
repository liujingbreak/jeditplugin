package org.liujing.awttools.classview;

public interface LinkExplorerListener
{
	public void onLink(LinkElement element);
	public void outLink(LinkElement element);
	public void doLink(LinkElement element,boolean doubleClick);
}
