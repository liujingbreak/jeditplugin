package org.liujing.awttools.classview;

import java.util.*;
import java.io.*;
import java.util.logging.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;

public class BrowserHistory<T> implements ActionListener
{
	private static Logger log = Logger.getLogger(BrowserHistory.class.getName());
	private LinkedList<T> list = new LinkedList<T>();
	private int pointer = -1;
	private JPopupMenu popupmenu;
	private EventListenerList listeners = new EventListenerList();
	private int size = 0;
	
	private Component popupComp;
	
	public BrowserHistory(int size){
		popupmenu = new JPopupMenu("History");
		this.size = size;
	}
	
	public void addHistoryListener(HistoryListener<T> lis){
		listeners.add(HistoryListener.class, lis);
	}
	
	public void removeHistoryListener(HistoryListener<T> lis){
		listeners.remove(HistoryListener.class, lis);
	}
	
	@SuppressWarnings("unchecked")
	protected void fireHistoryLink(T e) {
     // Guaranteed to return a non-null array
     Object[] lisObjs = listeners.getListenerList();
     // Process the listeners last to first, notifying
     // those that are interested in this event
     for (int i = lisObjs.length-2; i>=0; i-=2) {
		 if(lisObjs[i] == HistoryListener.class){
			 ((HistoryListener<T>)lisObjs[i+1]).onHistoryLink(e);
		 }
     }
 }

	
	public void boundPopup(JButton btn){
		popupComp = btn;
		btn.addActionListener(this);
	}
	
	public void popupMenu(Component comp,int x,int y){
		popupmenu.show(comp,x,y);
	}
	
	public int getSize(){
		return size;
	}
	
	public int getPointer(){
		return pointer;
	}
	
	public void addHistory(T item){
		JMenuItem mi = null;
		if(list.size() > 0){
			mi = (JMenuItem)popupmenu.getComponent(getCurrMenuItemIdx());
			mi.setEnabled(true);
		}
		removeRest();
		list.add(item);
		
		mi = new JMenuItem(item.toString());
		popupmenu.insert(mi,0);
		mi.addActionListener(this);
		mi.setEnabled(false);
		while(list.size() > size){
			int last = list.size() - 1;
			list.removeFirst();
			mi = (JMenuItem)popupmenu.getComponent(last);
			mi.removeActionListener(this);
			popupmenu.remove(last);
		}
		pointer = list.size()-1;
		
	}
	private int getCurrMenuItemIdx(){
		return list.size() - 1 - pointer;
	}
	
	private void removeRest(){
		while(pointer < (list.size() - 1) ){
			list.removeLast();
			JMenuItem mi = (JMenuItem)popupmenu.getComponent(0);
			mi.removeActionListener(this);
			popupmenu.remove(0);
		}
	}
	
	public T getCurrent(){
		return list.get(pointer);
	}
	
	public T back(){
		if(!isBackable()){
			return null;
		}
		pointer--;
		return getCurrent();
	}
	
	public T forward(){
		if(!isForwardable()){
			return null;
		}
		pointer++;
		return getCurrent();
	}
	
	public boolean isBackable(){
		return (pointer-1) >= 0;
	}
	
	public boolean isForwardable(){
		return (pointer+1) < list.size();
	}
	
	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e){
		try{
			Object source = e.getSource();
			if(source == popupComp){
				Dimension d = popupComp.getSize();
				popupmenu.show(popupComp, 0, d.height);
			}else if(source instanceof JMenuItem){
				((JMenuItem)popupmenu.getComponent(getCurrMenuItemIdx()))
					.setEnabled(true);
				((JMenuItem)source).setEnabled(false);
				fireHistoryLink((T)e.getActionCommand());
				pointer = getPointerByMenuItem((JMenuItem)source);
				//log.info("pointer = "+pointer);
			}
		}catch(Exception ex){
			log.log(Level.SEVERE,"",ex);
		}	
	}
	
	private int getPointerByMenuItem(Component menuItem){
		int idx = 0;
		int c = popupmenu.getComponentCount();
		Component[] comps = popupmenu.getComponents();
		for(int i= 0; i<c; i++){
			if(comps[i] == menuItem){
				idx = i;
				break;
			}
		}
		return c - 1 - idx;
	}
}
