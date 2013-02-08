package org.liujing.jeditplugin.v2;

import org.liujing.jeditplugin.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import org.junit.*;
import static org.junit.Assert.*;

public class ProjectControllerTest{
	private static Logger log = Logger.getLogger(ProjectControllerTest.class.getName());

	File rootDir;

	public ProjectControllerTest(){
	}

	@Before
	public void init(){
		rootDir = new File("testPrjCtlRoot");
		rootDir.delete();
		rootDir.mkdirs();
	}

	/*@Test
	public void test_save_load()throws Exception{
		ProjectController ctl = new ProjectController();
		ctl.setRootDir(rootDir);
		ctl.load();

		ctl.currentProject = new ProjectModule();
		ctl.currentProject.setName("testname");
		ctl.save();

		log.info("location: "+ rootDir.getPath());

		ctl = new ProjectController();
		ctl.setRootDir(rootDir);
		ctl.load();

		assertEquals("testname", ctl.getCurrProjName());
	}*/

	@Test
	public void test_SaveProject_listProject()throws Exception{
		ProjectController ctl = new ProjectController();
		ctl.setRootDir(rootDir);
		ctl.load();

		for(int i = 0;i<2;i++){
			ProjectModule p = new ProjectModule();
			p.setName("project "+ i);
			ctl.saveProject(p);
		}

		List<String> list = ctl.listProjects();
		assertTrue(list.contains("project 0"));
		assertTrue(list.contains("project 1"));
	}
}