package org.liujing.jeditplugin.v2;

import java.io.*;
import java.util.zip.*;
import java.util.*;
import java.util.logging.*;
import org.junit.*;
import static org.junit.Assert.*;

public class DirectoryScanTest{
	TestScanner scan;
	@Before
	public void start(){
		scan = new TestScanner();
	}
	@Ignore
	@Test
	public void testScan1()throws Exception{
		//scan.compilePattern("/liujing/myprojects/*");
		//scan.patternDrivenScan("../*.{jar,xml}", new File("."));
		assertTrue(new DirectoryScan("test1/*/test2.txt").pathMatchesPattern("test1/a/test2.txt"));
		assertTrue(new DirectoryScan("test1/*/*.txt").pathMatchesPattern("test1/a/test2.txt"));
		assertTrue(new DirectoryScan("test1/**/*.txt").pathMatchesPattern("test1/a/b/c/test2.txt"));
		assertTrue(new DirectoryScan("**/*.txt").pathMatchesPattern("test1/a/b/c/test2.txt"));
		assertTrue(new DirectoryScan("test1/**/b/**/test2.txt").pathMatchesPattern("test1/a/b/c/test2.txt"));
		assertTrue(new DirectoryScan("**/svn/**/*").pathMatchesPattern("test1/a/svn/c/test2.txt"));
		assertTrue(!new DirectoryScan("**/svn/**/*").pathMatchesPattern("test1/a/b/c/test2.txt"));
	}
	
	
	@Test
	public void testScan2()throws Exception{
		scan.patternDrivenScan("e:\\jboss\\jboss-4.0.5.GA\\server\\main\\lib\\*.jar", new File("C:\\liujing\\myproject\\jeditplugin"));
	}
	
	//@Test
	//public void testScan3()throws Exception{
	//	scan.patternDrivenScan("d:\\myproject\\mylib\\*.jar", new File("."));
	//}
	//
	//@Test
	//public void testScan4()throws Exception{
	//	scan.patternDrivenScan("d:\\myproject\\mylib\\build\\**\\classes", new File("."));
	//}
	@Ignore
	@Test
	public void testScan5()throws Exception{
		assertTrue(new DirectoryScan("org/**/DirectoryScanTest.class").pathMatchesPattern(new File("."), new File("D:/myproject/myjedit/build/test-classes/org/liujing/jeditplugin/v2/DirectoryScanTest.class")));
	}
	
	static class TestScanner extends DirectoryScan{
		protected void processFile(File f){
			System.out.println("#### find: " + f.getPath());
		}
	}

}
