package org.liujing.jeditplugin;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import org.gjt.sp.jedit.*;
import org.liujing.jeditplugin.v2.*;


public class JeditBufferController extends BufferController implements Serializable, ProjectController.ProjectEventListener{
	private static Logger log = Logger.getLogger( JeditBufferController.class.getName() );
	static final long serialVersionUID = 2L;

	private LinkedList <BufferImpl> bufferList;
	private File					rootDir;
	private int						lastChangedIdx 			= -1;
	private boolean					bufferSynced 			= false;
	private transient View			jeditView;

	public JeditBufferController(){
		rootDir = new File(System.getProperty("user.home"));
		bufferList = new LinkedList();
	}

	public void setRootDir(File dir){
		rootDir = dir;
	}

	public void setJeditView(View v){
		jeditView = v;
	}

	public void boundProjectEvent(ProjectController pj){
		pj.addProjectEventListener(this);
	}

	/**
	Invoke this method after this instance is deserialized from a file and is firt loaded to jedit
	*/
	public void init(){
		bufferSynced = false;
	}

	//public void load()throws IOException, ClassNotFoundException{
	//	log.fine("load");
	//	File serFile = new File(rootDir, "jedit_buffer_list.ser");
	//	if(serFile.exists()){
	//		ObjectInputStream in = new ObjectInputStream( new FileInputStream(
	//			serFile) );
	//		bufferList = (LinkedList <BufferImpl>) in.readObject();
	//		in.close();
	//	}else{
	//		bufferList = new LinkedList();
	//	}
	//
	//}
	//
	//
	//public void save()throws IOException{
	//	ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
	//		new File(rootDir, "jedit_buffer_list.ser"), false));
	//	out.writeObject(bufferList);
	//	out.close();
	//}

	protected void syncBufferList(){
		Buffer[] buffers = jEdit.getBuffers();
		if(log.isLoggable(Level.FINE))
			log.fine("Num of buffers: "+ buffers.length + " bufferList:" + bufferList.size());
		if(buffers.length <= 0)
			return;
		int i = 0;
		for(; i< buffers.length; i++ ){
			boolean exist = false;
			Iterator<BufferImpl> it = bufferList.iterator();
			while(it.hasNext()){
				BufferImpl bi = it.next();
				if( bi.getName().equals( buffers[i].getName() ) &&
					bi.getDirectory().equals( buffers[i].getDirectory() ) )
				{
					bi.setJBuffer(buffers[i]);
					exist = true;
					break;
				}
			}
			if(!exist){
				bufferList.add(new BufferImpl(buffers[i]));
				log.fine("sync buffer "+ buffers[i].getName());
			}
		}
		// remaining BufferImpls
		Iterator<BufferImpl> it = bufferList.iterator();
		while(it.hasNext()){
			BufferImpl bi = it.next();
			if( bi.getJBuffer() == null )
				it.remove();
		}
		fireListOrderChange();
		log.fine("buffer list synced");
		bufferSynced = true;
	}

	public List<? extends BufferInterf> getBufferList(){
		return bufferList;
	}

	public boolean isBufferDirty( int index){
		Buffer b = bufferList.get(index).getJBuffer();
		if(b != null)
			return b.isDirty();
		else
			return false;
	}

	@Override
	public BufferInterf activateBuffer(int index){
		try{
			//log.fine("activate in "+ jEdit.getActiveView().hashCode());
			BufferImpl impl = bufferList.get(index);
			Buffer b = impl.getJBuffer();
			lastChangedIdx = index;

			jeditView.setBuffer(b);
			return impl;
		}catch(RuntimeException thr){
			log.log(Level.SEVERE, "", thr);
			throw thr;
		}
	}

	@Override
	public int closeBuffer(int[] indices){
		Buffer []buffers = new Buffer[ indices.length ];
		for(int i=0; i<indices.length; i++){
			buffers[i] = bufferList.get( indices[i] ).getJBuffer();
		}
		for(Buffer b: buffers)
			jEdit.closeBuffer(jEdit.getActiveView(),b);
		return indices.length;
	}

	protected void bufferActivated(Buffer buf){

		Iterator<BufferImpl> it = bufferList.iterator();
		for(int i = 0; it.hasNext(); i++){
			BufferImpl bi = it.next();
			if(bi.getJBuffer().equals(buf) ){
				fireBufferActivated(i, bi);
				if(log.isLoggable(Level.FINE))
					log.fine("Buffer "+ i +" name:"+ bi.getName() + " is Activated.");
				it.remove();
				bufferList.addFirst(bi);
				break;
			}
		}

	}

	protected void bufferClosed(Buffer buf){
		if(log.isLoggable(Level.FINE))
			log.fine("bufferClosed");
		Iterator<BufferImpl> it = bufferList.iterator();
		for(int i = 0; it.hasNext(); i++){
			BufferImpl bi = it.next();
			if(bi.getJBuffer().equals(buf) ){
				fireBufferClosed(i, bi);
				if(log.isLoggable(Level.FINE))
					log.fine("Buffer "+ i +" name:"+ bi.getName() + " is closed.");
				it.remove();
				break;
			}
		}
		if(bufferList.size() > 0)
		    activateBuffer(0);
	}

	protected void bufferCreated(Buffer buf){
		if(!bufferSynced)
			return ;
		BufferImpl bi = new BufferImpl(buf);

		int idx = 0;
		//log.fine("current actived buffer: " + jEdit.getActiveView().getBuffer());
		if(jeditView.getBuffer().equals(buf)){
			bufferList.addFirst(bi);
			idx = 0;
		}else{
			idx = bufferList.size();
			bufferList.add(bi);
		}
		//if(log.isLoggable(Level.FINE)) log.fine("create at : " + idx + ", view = " + jeditView);
		fireBufferCreated(idx, bi);
		//if(log.isLoggable(Level.FINE)) log.fine("Buffer name:"+ bi.getName() + " is created.");
	}

	protected void bufferUpdated(Buffer buf){
		Iterator<BufferImpl> it = bufferList.iterator();
		for(int i = 0; it.hasNext(); i++){
			BufferImpl bi = it.next();
			if(bi.getJBuffer().equals(buf) ){
				bi.update();
				fireBufferUpdated(i, bi);
				break;
			}
		}
	}

	public void projectAction(int eventType, Object param){
		if(eventType == ProjectController.EVENT_OPEN_FILE){
			log.fine("open file");
			jEdit.openFile(jeditView, (String)param);
		}
	}

	protected static class BufferImpl implements BufferInterf, Serializable{
		transient Buffer	jbuff;
		String				name;
		String				directory;

		public BufferImpl(Buffer jeditBuffer){
			setJBuffer(jeditBuffer);
		}

		public void setJBuffer(Buffer buff){
			jbuff = buff;
			update();
		}

		public Buffer getJBuffer(){
			return jbuff;
		}

		public String getName(){
			return name;
		}

		public String getDirectory(){
			return directory;
		}

		public void update(){
			name = jbuff.getName();
			directory = jbuff.getDirectory();
		}
	}
}
