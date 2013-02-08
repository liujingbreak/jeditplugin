package org.liujing.jeditplugin.v2;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.*;
import java.util.*;
import liujing.util.*;
import org.junit.*;

public class BackgroundWorkControllerTest implements BackgroundWorkController.StatusListener{
	static Logger log = Logger.getLogger(BackgroundWorkControllerTest.class.getName());
	@Ignore
	@Test
	public void test1()throws Exception{
		log.info("-------------------------------------------------------");
		BackgroundWorkController c = new BackgroundWorkController();
		c.setStatusListener(this);
		for(int i=0; i<5; i++)
			c.addTask(new TestTask());
		//c.joinAll();
		Thread.sleep(7000);
	}
	@Ignore
	@Test
	public void test2()throws Exception{
		log.info("-------------------------------------------------------");
		BackgroundWorkController c = new BackgroundWorkController(3, 3);
		c.setStatusListener(this);
		for(int i=0; i<5; i++)
			c.addTask(new TestTask());
		//c.joinAll();
		Thread.sleep(7000);
	}

	@Test
	public void test3()throws Exception{
		log.info("-------------------------------------------------------");
		BackgroundWorkController c = new BackgroundWorkController(3, 1);
		c.setStatusListener(this);
		for(int i=0; i<5; i++)
			c.addTask(new TestTask());
		//c.joinAll();
		Thread.sleep(7000);
		log.info("--- add more tasks-------------------");
		c.addTask(new TestTask());
		c.addTask(new TestTask());
		Thread.sleep(5000);
	}
	@Ignore
	@Test
	public void test4()throws Exception{
		log.info("-------------------------------------------------------");
		BackgroundWorkController c = new BackgroundWorkController(3, 0);
		c.setStatusListener(this);
		for(int i=0; i<5; i++)
			c.addTask(new TestTask());
		//c.joinAll();
		Thread.sleep(7000);
	}

	public void onTextMessage(String msg){
		log.info(msg);
	}

	public class TestTask implements BackgroundWorkController.Task<String>{
		public String execute() throws Exception{
			Thread.sleep(1000);
			return null;
		}

		public void onTaskDone(String result){
			log.info(Thread.currentThread().getName()+ " done");
		}

		public void onTaskFail(Exception thr){
			thr.printStackTrace();
		}
	}

}
