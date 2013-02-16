package org.liujing.parser;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import sidekick.*;
import javax.swing.*;
import javax.swing.text.Position;
import sidekick.util.*;

public class MutableIconNode extends JsNode{
    public static Icon RED_ICON = RedSquareNode.ICON;
    public static Icon YELLOW_ICON = YellowSquareNode.ICON;
    public static Icon BLUE_ICON = BlueSquareNode.ICON;
    public static Icon ORIGIN_ICON = JsNode.ICON;
    private Icon icon = JsNode.ICON;
	
    public MutableIconNode(String name,String desc){
		super(name, desc);
	}

	public MutableIconNode(String name,String desc, int type){
		super(name, desc, type);
	}
	
	public Icon getIcon(){
		return icon;
	}
	
	public void setIcon(Icon icon){
	    this.icon = icon;
	}
}
