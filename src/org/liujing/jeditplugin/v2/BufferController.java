package org.liujing.jeditplugin.v2;

//import org.liujing.jeditplugin.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

public class BufferController{
	private static Logger log = Logger.getLogger(BufferController.class.getName());
	
	private List<BufferListener>		listeners = new LinkedList();
	
	public BufferController(){
	}
	
	public List<? extends BufferInterf> getBufferList(){
		return new LinkedList();
	}
	
	public boolean isBufferDirty( int index){
		return false;
	}
	
	public BufferInterf activateBuffer(int index){
		return null;
	}
	
	public int closeBuffer(int[] indices){
		return indices.length;
	}
	
	public void addBufferListener(BufferListener lis){
		listeners.add(lis);
	}
	
	public void removeBufferListener(BufferListener lis){
		listeners.remove(lis);
	}
	
	protected void fireBufferClosed(int index, BufferInterf buffer){
		Iterator<BufferListener> it = listeners.iterator();
		while(it.hasNext())
			it.next().bufferClosed(index, buffer);
	}
	
	protected void fireBufferActivated(int index, BufferInterf buffer){
		Iterator<BufferListener> it = listeners.iterator();
		while(it.hasNext())
			it.next().bufferActivated(index, buffer);
	}
	
	protected void fireBufferCreated(int index, BufferInterf buffer){
		Iterator<BufferListener> it = listeners.iterator();
		while(it.hasNext())
			it.next().bufferCreated(index, buffer);
	}
	
	protected void fireBufferUpdated(int index, BufferInterf buffer){
		Iterator<BufferListener> it = listeners.iterator();
		while(it.hasNext())
			it.next().bufferUpdated(index, buffer);
	}
	
	protected void fireListOrderChange(){
		Iterator<BufferListener> it = listeners.iterator();
		while(it.hasNext())
			it.next().listOrderChange();
	}
	
	public interface BufferListener{
		public void bufferClosed(int index, BufferInterf buffer);
		
		public void bufferActivated(int index, BufferInterf buffer);
		
		public void bufferCreated(int index, BufferInterf buffer);
		
		public void listOrderChange();
		
		public void bufferUpdated(int index, BufferInterf buffer);
	}
	
	public interface BufferInterf{
		public String getName();
		public String getDirectory();
	}
}
