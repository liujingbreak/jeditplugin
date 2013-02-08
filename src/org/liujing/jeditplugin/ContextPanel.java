package org.liujing.jeditplugin;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.buffer.*;
import org.liujing.util.*;

public class ContextPanel extends JPanel
{
	protected JLabel label;
	/**
	Basic constructor for ContextPanel
	*/
	public ContextPanel()
	{
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		label=new JLabel("...");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(label);
	}
	
	

}
