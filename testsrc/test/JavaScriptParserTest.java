package test;

import org.junit.*;
import java.io.*;
import java.util.logging.*;
import java.util.*;
import org.liujing.jj.*;
import java.util.regex.*;

public class JavaScriptParserTest{
	Logger log = Logger.getLogger(JavaScriptParserTest.class.getName());
	CharSequence input;
	@Before
	public void inputReady()throws  IOException{
		input = createCharSequence();
	}
	
	@Test
	public void parser()throws Exception{
		log.info("here");
		JavascriptParser parser = new JavascriptParser();
		parser.parse(this.getClass().getClassLoader()
			.getResourceAsStream("test/test02.txt"));
	}
	
	protected String createCharSequence()throws IOException{
		InputStream testFileIn = this.getClass().getClassLoader()
			.getResourceAsStream("test/test02.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(testFileIn));
		
		StringWriter writer = new StringWriter();
		int chr = reader.read();
		while(chr!=-1){
			writer.write(chr);
			chr = reader.read();
		}
		//LLLACompiler p = new LLLACompiler(writer.toString());
		return writer.toString();
	}
	
	public static void main(String args[])throws Exception{
		JavaScriptParserTest testcase = new JavaScriptParserTest();
		testcase.inputReady();
		testcase.parser();
	}
}
