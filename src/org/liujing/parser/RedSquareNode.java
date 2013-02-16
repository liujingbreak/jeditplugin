package org.liujing.parser;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import sidekick.*;
import javax.swing.*;
import javax.swing.text.Position;
import sidekick.util.*;

public class RedSquareNode extends JsNode{
    public static Icon ICON;
    static{
		ICON = new ImageIcon(JsNode.class.getResource(
		    "/org/liujing/parser/icons/red_square_dot.jpg"));
	}
	
    public RedSquareNode(String name,String desc){
		super(name, desc);
	}

	public RedSquareNode(String name,String desc, int type){
		super(name, desc, type);
	}
	
	public Icon getIcon(){
		return ICON;
	}
}
