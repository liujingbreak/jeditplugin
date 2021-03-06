package org.liujing.parser;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import sidekick.*;
import javax.swing.*;
import javax.swing.text.Position;
import sidekick.util.*;

public class YellowSquareNode extends JsNode{
    public static Icon ICON;
    static{
		ICON = new ImageIcon(JsNode.class.getResource(
		    "/org/liujing/parser/icons/yellow_square_dot.jpg"));
	}
	
    public YellowSquareNode(String name,String desc){
		super(name, desc);
	}

	public YellowSquareNode(String name,String desc, int type){
		super(name, desc, type);
	}
	
	public Icon getIcon(){
		return ICON;
	}
}
