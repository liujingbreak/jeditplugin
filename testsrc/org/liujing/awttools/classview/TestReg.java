package org.liujing.awttools.classview;
import java.util.*;
import java.io.*;
import java.util.logging.*;
import org.liujing.awttools.*;
import java.util.regex.*;
import java.util.jar.*;
import org.junit.*;
import static org.junit.Assert.*;

public class TestReg
{
	@Test
	public void testParse()
	{
		//parseDoc(new StringBuilder("Compiled from \"MyJeditPluginPanel.java\"\npublic class org.liujing.jeditplugin.MyJeditPluginPanel extends javax.swing.JPanel implements java.awt.event.KeyListener,java.awt.event.ActionListener,java.awt.event.WindowListener{"));
		Pattern p=Pattern.compile("[\\w\\.]+");
		Matcher m=p.matcher("abc.efg_opq123");
		Assert.assertTrue(m.matches());
		
		p=Pattern.compile("[\\w\\.]+");
		m=p.matcher("abcefg_opq123");
		Assert.assertTrue(m.matches());
		
		p=Pattern.compile("[\\w]+");
		m=p.matcher("abcefg.opq123");
		Assert.assertFalse(m.matches());
		
		p=Pattern.compile("[\\w.]+");
		m=p.matcher("abcefg_o$pq123$");
		Assert.assertFalse(m.matches());
		
		m=Javaprint.primTypePat.matcher("int[]");
		Assert.assertTrue(m.matches());
		m=Javaprint.primTypePat.matcher("int[][]");
		Assert.assertTrue(m.matches());
		m=Javaprint.primTypePat.matcher("unsigned char");		
		Assert.assertTrue(m.matches());
		m=Javaprint.primTypePat.matcher("long");		
		Assert.assertTrue(m.matches());
		m=Javaprint.primTypePat.matcher("Long");		
		Assert.assertFalse(m.matches());
		m=Javaprint.primTypePat.matcher("my.String");		
		Assert.assertFalse(m.matches());
	}
}
