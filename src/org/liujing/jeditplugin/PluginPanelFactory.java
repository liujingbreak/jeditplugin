package org.liujing.jeditplugin;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.concurrent.*;

import org.liujing.jeditplugin.ui.*;
import org.liujing.jeditplugin.v2.*;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.msg.*;

public class PluginPanelFactory implements EBComponent{
	private static Logger log = Logger.getLogger( PluginPanelFactory.class.getName() );

	private ConcurrentHashMap<View, PluginPanel> panels = new ConcurrentHashMap();
	private PluginPanel originalPanel;
	//private ConcurrentLinkedQueue<PluginPanel> recycled = new ConcurrentLinkedQueue();

	public PluginPanelFactory(){

	}

	public void start()throws IOException, ClassNotFoundException{
		log.fine("start");
		JeditBufferController	bufferCtl = null;
		ProjectController		projectCtl = null;
		File serFile = new File(MyJeditPlugin.getInstance().getPluginHome(), "jedit_buffer.ser");
		if(serFile.exists()){
			try{
				ObjectInputStream in = new ObjectInputStream( new FileInputStream(
				serFile) );
				bufferCtl = (JeditBufferController) in.readObject();
				in.close();
			}catch(IOException ive){
				bufferCtl = new JeditBufferController();
				log.log(Level.WARNING, "", ive);
			}
		}else{
			bufferCtl = new JeditBufferController();
		}
		bufferCtl.init();
		bufferCtl.syncBufferList();

		serFile = new File(MyJeditPlugin.getInstance().getPluginHome(), "jedit_project.ser");
		if(serFile.exists()){
			try{
				ObjectInputStream in = new ObjectInputStream( new FileInputStream(
				serFile) );
				projectCtl = (ProjectController) in.readObject();
				in.close();
			}catch(IOException ive){
				projectCtl = new ProjectController();
				log.log(Level.WARNING, "", ive);
			}
		}else{
			projectCtl = new ProjectController();
		}
		projectCtl.setRootDir(MyJeditPlugin.getInstance().getPluginHome());
		bufferCtl.boundProjectEvent(projectCtl);
		PluginPanel p = new PluginPanel(bufferCtl, projectCtl);
		log.fine("started");
		originalPanel = p;
		//recycle(p);
	}

	public void stop()throws IOException{
		Iterator<PluginPanel> it = panels.values().iterator();
		if( ! it.hasNext()){
		    log.warning("no panel");
		    return;
		}
		PluginPanel copyFrom = it.next();
		saveForQuit(copyFrom);
	}
	
	protected void saveForQuit(PluginPanel copyFrom)throws IOException{
		
		JeditBufferController bufferCtl0 = copyFrom.getController(JeditBufferController.class);
		ProjectController projectCtl0 = copyFrom.getController(ProjectController.class);

		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
			new File(MyJeditPlugin.getInstance().getPluginHome(), "jedit_buffer.ser"), false));
		out.writeObject(bufferCtl0);
		out.close();

		out = new ObjectOutputStream(new FileOutputStream(
			new File(MyJeditPlugin.getInstance().getPluginHome(), "jedit_project.ser"), false));
		out.writeObject(projectCtl0);
		out.close();
		log.fine("stop and save all .ser files");
	}

	public PluginPanel get(View view)throws IOException, ClassNotFoundException{
	    PluginPanel p = panels.get(view);
	    if(p == null){
	        p = clonePluginPanelForView(view);
	    }
	    return p;
	}

	protected PluginPanel clonePluginPanelForView(View view)throws IOException, ClassNotFoundException{
		PluginPanel p = null;
		if(log.isLoggable(Level.FINE))
			log.fine("New view "+ view + "/" + view.hashCode());
		JeditBufferController bufferCtl = null;
		ProjectController projectCtl = null;
		Iterator<PluginPanel> it = panels.values().iterator();
		if(originalPanel != null){
		    p = originalPanel;
		    originalPanel = null;
		    bufferCtl = p.getController(JeditBufferController.class);
            projectCtl = p.getController(ProjectController.class);
    
		}else{
		    PluginPanel copyFrom = it.next();
		    p = new PluginPanel(bufferCtl, projectCtl);
            JeditBufferController bufferCtl0 = copyFrom.getController(JeditBufferController.class);
            ProjectController projectCtl0 = copyFrom.getController(ProjectController.class);
            bufferCtl = deepCopy(bufferCtl0);            
            projectCtl = deepCopy(projectCtl0);
		}
		bufferCtl.syncBufferList();
		bufferCtl.boundProjectEvent(projectCtl);
		p.getController(JeditBufferController.class).setJeditView(view);
		panels.put(view, p);
		return p;
	}

	//public void recycle(PluginPanel pluginPanel){
	//	recycled.add(pluginPanel);
	//}

	public void handleMessage(EBMessage message)
	{
	    try{
		//log.fine(message.toString());
		if(message instanceof BufferUpdate){

			BufferUpdate m=(BufferUpdate)message;

			JeditBufferController bufferCtl = null;
			if(m.getWhat().equals(BufferUpdate.CLOSED)){
				Iterator<PluginPanel> it = panels.values().iterator();
				while(it.hasNext()){
					bufferCtl = it.next().getController(JeditBufferController.class);
					bufferCtl.bufferClosed(m.getBuffer());
				}
			}
			else if(m.getWhat().equals(m.CREATED)){
				Iterator<PluginPanel> it = panels.values().iterator();
				while(it.hasNext()){
					bufferCtl = it.next().getController(JeditBufferController.class);
					bufferCtl.bufferCreated(m.getBuffer());
				}
			}
			else if(m.DIRTY_CHANGED.equals(m.getWhat())){
				Iterator<PluginPanel> it = panels.values().iterator();
				while(it.hasNext()){
					bufferCtl = it.next().getController(JeditBufferController.class);
					bufferCtl.bufferUpdated(m.getBuffer());
				}
			}else if(m.getWhat().equals(m.SAVED) ){
				log.fine("buffer saved");
				Iterator<PluginPanel> it = panels.values().iterator();
				while(it.hasNext()){
					bufferCtl = it.next().getController(JeditBufferController.class);
					bufferCtl.bufferUpdated(m.getBuffer());
				}
			}
		}
		else if(message instanceof EditPaneUpdate){
			EditPaneUpdate m=(EditPaneUpdate)message;
			if(m.BUFFER_CHANGED.equals(m.getWhat())){
			    PluginPanel pluginPanel = panels.get( m.getEditPane().getView() );
			    if(pluginPanel != null){
                    JeditBufferController bufferCtl = pluginPanel.getController(JeditBufferController.class);
                    Buffer b=m.getEditPane().getBuffer();
                    log.fine("BUFFER_CHANGED");
                    //select the active buffer
                    bufferCtl.bufferActivated(b);
				}

			}
		}
		else if(message instanceof ViewUpdate){
		    ViewUpdate viewUpdateMsg = (ViewUpdate)message;
		    if(ViewUpdate.CLOSED.equals(viewUpdateMsg.getWhat())){
		        log.fine("view closed");
		        if(panels.size() == 1){
		            saveForQuit(panels.values().iterator().next());
		        }
		        panels.remove(viewUpdateMsg.getView());
		    }

		}else if(message instanceof EditorStarted){
			log.fine("EditorStarted");
			if(originalPanel == null)
			    return;
			PluginPanel p = originalPanel;
			JeditBufferController bufferCtl = p.getController(JeditBufferController.class);
			bufferCtl.syncBufferList();
		}
		}catch(IOException ie){
		    log.log(Level.SEVERE, "", ie);
		}
	}

	private<T> T deepCopy(T sourceObject)throws IOException, ClassNotFoundException{
		ByteArrayOutputStream objBuf = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(objBuf);
		out.writeObject(sourceObject);
		out.close();

		ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(objBuf.toByteArray()));
		T o = (T)objIn.readObject();
		objIn.close();
		return o;
	}

}
