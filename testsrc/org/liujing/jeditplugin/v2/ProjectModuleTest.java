package org.liujing.jeditplugin.v2;

import org.liujing.jeditplugin.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;
import org.junit.*;
import static org.junit.Assert.*;

public class ProjectModuleTest{
	
	@Test
	public void testConvertPattern1()throws Exception{
		
		String s = "c:/liujing/test/*wang";
		System.out.println(s);
		
		String news = ProjectModule.convertPattern(s);
		
		System.out.println(news);
		assertTrue(Pattern.compile(news).matcher("c:\\liujing\\test\\312313kwang").matches());
	}
	
	
	@Test
	public void testConvertPattern2()throws Exception{
		
		String s = "c:/liujing/test/**/wang/*";
		System.out.println(s);
		
		String news = ProjectModule.convertPattern(s);
		
		System.out.println(news);
		assertTrue(Pattern.compile(news).matcher("c:\\liujing\\test\\312\\313k\\wang\\1").matches());
	}
	
	@Test
	public void testConvertPattern3()throws Exception{
		
		String s = "c:/liujing/test/**/*.java";
		System.out.println(s);
		
		String news = ProjectModule.convertPattern(s);
		
		System.out.println(news);
		assertTrue(Pattern.compile(news).matcher("c:\\liujing\\test\\1.java").matches());
	}
}