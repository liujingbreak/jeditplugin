package org.liujing.jeditplugin;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.msg.*;
import org.liujing.util.*;
import java.util.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.logging.*;
import java.util.concurrent.*;
import java.lang.reflect.Method;
import org.liujing.jeditplugin.v2.ProjectController;
import org.liujing.jeditplugin.ui.*;
import org.liujing.jeditplugin.v3.JEditOpenFileServlet;

import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.*;


public class MyJeditPlugin extends EditPlugin implements EBComponent
{
    private static int PORT_NUMBER = 19816;
	public static ExecutorService singleWorker=Executors.newSingleThreadExecutor();
	//private static LogThread log=new LogThread(MyJeditPlugin.class);
	private static Logger log=Logger.getLogger(MyJeditPlugin.class.getName());
	private MyTableModel bufferListModel;
	private Map<View,MyJeditPluginPanel> myPanels=new HashMap();

	//private HashMap<Buffer,String> bufferMap;
	private static MyJeditPlugin instance=null;
	private Handler myLogHandler=new org.liujing.util.logging.MyLogHandler();
	private Handler fileLogHandler;

	public static Properties prop;
	public static String PROP_OPENED_PROJECT="opened_project_name";
	private static final String SYS_PROP_FILE_NAME="config.properties";

	PluginPanelFactory panelFactory = new PluginPanelFactory();

	private LinkedList<PluginPanel>	pluginPanels = new LinkedList();

	protected static Server server;
	private java.util.Timer initTimer = new java.util.Timer();
	/**
	 * It must be synchronized.
	*/
	public java.util.List<MyJeditPluginPanel> panels=new LinkedList();
	/**
	Basic constructor for MyJeditPlugin
	*/
	public MyJeditPlugin()
	{

		if(instance!=null){
			log.severe("error: new MyJeditPlugin again!");

		}
		instance=this;
		prop=new Properties();
	}
	public static MyJeditPlugin getInstance()
	{
		return instance;
	}
	public void start()
	{
	    try{
	        File logDir = new File(System.getProperty("user.home"), "log");
	        boolean fileLogEnabled = true;
	        if(!logDir.isDirectory()){
	            if(!logDir.exists()){
	                fileLogEnabled = logDir.mkdirs();
	            }else
	                fileLogEnabled = false;
	        }
	            
	        if(fileLogEnabled){
                fileLogHandler = new FileHandler(logDir.getPath() 
                    + File.separator +"myJeditplugin-%g.log", 500000, 5, true);
                fileLogHandler.setFormatter(new LogFormater());
                Logger.getLogger("org").addHandler(fileLogHandler);
            }else{
                log.warning(String.format("Log folder is not availabel (%1$s), file log is disabled",
	                logDir.getPath()));
            }
            
            myLogHandler.setLevel(Level.WARNING);
            Logger.getLogger("org").addHandler(myLogHandler);
            
            Logger.getLogger("org").setLevel(Level.INFO);
            Logger.getLogger("org.liujing.filesync").setLevel(Level.INFO);
            Logger.getLogger("org.liujing.parser").setLevel(Level.INFO);
            Logger.getLogger("liujing.swing").setLevel(Level.INFO);
            Logger.getLogger("liujing").addHandler(fileLogHandler);
            //Logger.getLogger("org.liujing.jeditplugin").setLevel(Level.FINE);
            //Logger.getLogger("org.liujing.jeditplugin.v2.DirectoryScan").setLevel(Level.INFO);
            log.fine("start");
            EditBus.addToBus(this);
            EditBus.addToBus(MyProject.getInstance());


			File f=new File(MyJeditPlugin.getInstance().getPluginHome(),SYS_PROP_FILE_NAME);
			log.info("plugin location: "+ f.getPath());
			if(f.exists())
				prop.load(new FileInputStream(f));
			panelFactory.start();
			EditBus.addToBus(panelFactory);
			TimerTask initTask  = new TimerTask(){
			    public void run(){
			        try{
                        enableAllOSXFullscreen();
                        startWebServer();
                    }catch(Exception ex){
                        log.log(Level.WARNING, "failed to execute init deferred task", ex);
                    }
                }
            };
            initTimer.schedule(initTask, 5000);
		}catch(Exception ioe){
			log.log(Level.SEVERE,"",ioe);
		}
	}
	
	public static void enableAllOSXFullscreen(){
	    View[] views = jEdit.getViews();
	    for(View w: views){
	        enableOSXFullscreen(w);
	    }
	}
	
	//com.apple.eawt.FullScreenUtilities.setWindowCanFullScreen(view, true)
	public static void enableOSXFullscreen(Window window) {
        if(window == null)
            return;
        try {
            Class util = Class.forName("com.apple.eawt.FullScreenUtilities");
            Class params[] = new Class[]{Window.class, Boolean.TYPE};
            Method method = util.getMethod("setWindowCanFullScreen", params);
            method.invoke(util, window, true);
	        log.info("try to enable OS X full screen feature");
            
        } catch (ClassNotFoundException e1) {
            log.info("can't enable OS X full screen feature, not class found com.apple.eawt.FullScreenUtilities");
        } catch (Exception e) {
            log.log(Level.WARNING, "OS X Fullscreen FAIL", e);
        }
    }
    
    public static void toggleOSXFullscreen(Window window) {
        try {
            Class appClass = Class.forName("com.apple.eawt.Application");

            Method method = appClass.getMethod("getApplication");
            Object appInstance = method.invoke(appClass);

            Class params[] = new Class[]{Window.class};
            method = appClass.getMethod("requestToggleFullScreen", params);
            method.invoke(appInstance, window);
        } catch (ClassNotFoundException e1) {
        } catch (Exception e) {
            System.out.println("Failed to toggle Mac Fullscreen: "+e);
        }
    }

	public void stop()
	{
	    log.fine("--------------- stop ---------------------");

		myLogHandler=null;
		EditBus.removeFromBus(this);
		EditBus.removeFromBus(MyProject.getInstance());
		EditBus.removeFromBus(panelFactory);

		//instance=null;
		new FileOperator().delete(new File(MyJeditPlugin.getInstance().getPluginHome(),"temp"));
		MyProject.instance=null;
		try{
			panelFactory.stop();
			prop.store(new FileOutputStream(new File(MyJeditPlugin.getInstance().getPluginHome(),SYS_PROP_FILE_NAME),false),"save");
		}catch(IOException ioe){
			log.log(Level.SEVERE,"",ioe);
		}
		Logger.getLogger("org").removeHandler(myLogHandler);
		Logger.getLogger("org").removeHandler(fileLogHandler);
		myLogHandler = null;
		fileLogHandler.close();
		System.gc();
	}

	/**
	not used now

	public MyJeditPluginPanel getPanel(View view)
	{
		if(myPanels==null)
			myPanels=new HashMap();
		MyJeditPluginPanel panel=myPanels.get(view);
		if(panel==null){
			panel=new MyJeditPluginPanel();
			myPanels.put(view,panel);
		}
		return panel;
	}*/

	public void handleMessage(EBMessage message)
	{
		//log.fine(message.toString());
		if(message instanceof BufferUpdate){

			//BufferUpdate m=(BufferUpdate)message;
			//if(m.getWhat().equals(BufferUpdate.CLOSED)){
			//	bufferCtl.bufferClosed(m.getBuffer());
			//}
			//else if(m.getWhat().equals(m.CREATED)){
			//	bufferCtl.bufferCreated(m.getBuffer());
			//}
			//else if(m.DIRTY_CHANGED.equals(m.getWhat())){
			//	bufferCtl.bufferUpdated(m.getBuffer());
			//}
		}
		else if(message instanceof EditPaneUpdate){
			//EditPaneUpdate m=(EditPaneUpdate)message;
			//if(m.BUFFER_CHANGED==m.getWhat()){
			//	Buffer b=m.getEditPane().getBuffer();
			//	log.fine("open buffer");
			//	//select the active buffer
			//	bufferCtl.bufferActivated(b);
			//
			//}
		}
		else if(message instanceof ViewUpdate){
			ViewUpdate m=(ViewUpdate)message;
			if(m.EDIT_PANE_CHANGED.equals(m.getWhat())){
				//change selected buffer item from list, when user change EditPane
				moveBufferItem2First(m.getView().getBuffer());
				/*for(int i=0;i<panels.size();i++){
					JList l=panels.get(i).getBufferList();
					l.clearSelection();
					l.setSelectedValue(item,true);
				}*/
			}
			else if(m.getWhat()==m.CLOSED){
				//window closed
				myPanels.remove(m.getView());
				MyView.removeInstance(m.getView());
			}
		}
	}

	public PluginPanelFactory getPanelFactory(){
		return panelFactory;
	}

	/**
	init buffer list when first call
	*/
	public TableModel getBufferList()
	{
		if(bufferListModel==null){
			//init buffer list
			bufferListModel=new MyTableModel();
			Buffer[] buffers=jEdit.getBuffers();
			for(Buffer b:buffers){
				bufferListModel.buffer.add(b);
			}
			moveBufferItem2First(jEdit.getActiveView().getBuffer());
		}
		return bufferListModel;
	}
	public void closeBuffer(int [] selectedIdx)
	{
		Buffer []buffers=new Buffer[selectedIdx.length];
		for(int i=0;i<selectedIdx.length;i++){
			buffers[i]=bufferListModel.buffer.get(selectedIdx[i]);
		}
		for(Buffer b:buffers)
			jEdit.closeBuffer(jEdit.getActiveView(),b);
	}
	public void activeBuffer(int idx)
	{
		Buffer b=bufferListModel.buffer.get(idx);
		jEdit.getActiveView().setBuffer(b);
	}

	/**
	move buffer to first item in buffer list
	*/
	protected void moveBufferItem2First(Buffer item)
	{
		bufferListModel.deleteRow(item);
		bufferListModel.insertRow(item,0);
	}

	private static class MyTableModel extends AbstractTableModel
	{
		public java.util.List<Buffer> buffer;

		/**
		Basic constructor for MyTableModel
		*/
		public MyTableModel()
		{
			buffer=new LinkedList();
		}

		public int getColumnCount()
		{
			return 2;
		}
		public int getRowCount()
		{
			return buffer.size();
		}
		public Object getValueAt(int rowIndex,
                  int columnIndex)
		{
			Buffer b=buffer.get(rowIndex);
			if(columnIndex==0){
				if(b.isDirty())
					return "* "+b.getName();
				return b.getName();
			}else{
				return b.getDirectory();
			}
		}
		public String getColumnName(int c)
		{
			switch(c){
				case 0:
				return "File Name";
				case 1:return "Path";
			}
			return "";
		}

		public void deleteRow(Buffer b)
		{
			int i=buffer.indexOf(b);
			buffer.remove(i);
			fireTableRowsDeleted(i,i);
		}
		public void insertRow(Buffer b,int index)
		{
			buffer.add(index,b);
			fireTableRowsInserted(index,index);
		}
		public void updateRow(Buffer b)
		{
			int i=buffer.indexOf(b);
			fireTableCellUpdated(i,0);
			fireTableCellUpdated(i,1);
		}
	}
	
	public static boolean startWebServer()throws Exception{
	    
        if(server == null){
            server = new Server(PORT_NUMBER);
        
            //ResourceHandler resource_handler = new ResourceHandler();
            //resource_handler.setDirectoriesListed(true);
            //resource_handler.setWelcomeFiles(new String[]{ "index.html" });
            //resource_handler.setResourceBase("web");
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);
            
            context.addServlet(new ServletHolder(new JEditOpenFileServlet()),"/openfile");
            
            HandlerList handlers = new HandlerList();
            //handlers.addHandler(resource_handler);
            handlers.addHandler(context);
            server.setHandler(handlers);
        }
        if(server.isStopped() ||  server.isFailed()){
            server.start();
            log.info("JEdit plugin web server is started in port: "+ PORT_NUMBER);
            return true;
        }else{
            log.info("web server has already started");
            return false;
        }
        //server.join();
    }
    
    public static void stopWebServer()throws Exception{
        if(server != null && server.isStarted())
            server.stop();
    }
}
