package org.liujing.awttools.classview;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import javax.swing.text.*;
import java.util.*;
import java.io.*;
import java.util.logging.*;
import java.util.regex.*;
import org.liujing.awttools.TextFieldContextMenu;
import javax.swing.plaf.basic.BasicTextPaneUI;
/**

*/
public class LinkExplorer extends JTextPane
{
	private static Logger log=Logger.getLogger(LinkExplorer.class.getName());
	private java.util.List<LinkExplorerListener> listeners;
	private SimpleAttributeSet defaultAttr=new SimpleAttributeSet();
	private SimpleAttributeSet linkAttr=new SimpleAttributeSet();
	private SimpleAttributeSet linkexceptionAttr=new SimpleAttributeSet();
	private SimpleAttributeSet nameAttr=new SimpleAttributeSet();
	private SimpleAttributeSet accessAttr1=new SimpleAttributeSet();
	private SimpleAttributeSet accessAttr2=new SimpleAttributeSet();
	private SimpleAttributeSet paramAttr=new SimpleAttributeSet();
	private SimpleAttributeSet exceptionAttr=new SimpleAttributeSet();
	private SimpleAttributeSet typeAttr=new SimpleAttributeSet();
	private SimpleAttributeSet genericAttr=new SimpleAttributeSet();
	private SimpleAttributeSet linkOverAttr=new SimpleAttributeSet();
	private SimpleAttributeSet linkOutAttr=new SimpleAttributeSet();
	private SimpleAttributeSet highBackgroundAttr=new SimpleAttributeSet();
	private SimpleAttributeSet unHighBackgroundAttr=new SimpleAttributeSet();
	private SimpleAttributeSet fieldNameAttr=new SimpleAttributeSet();
	private SimpleAttributeSet classNameAttr=new SimpleAttributeSet();
	//public static int NAME_STYLE=0;
	//public static int LINK_STYLE=1;
	//public static int ACCESS_STYLE=2;
	//public static int GENERIC_STYLE=3;

	private TreeMap<Integer,LinkElement> linkElements;
	private TextFieldContextMenu tfCtMenu=new TextFieldContextMenu();
	private UIListener lis;
	private ElementComparator eComparator;
	private PrintTask printTask;
	private CharSequence charSequence;
	public LinkExplorer()
	{
	    setFont(new Font("DialogInput", Font.PLAIN ,13));
		//getCaret().setSelectionVisible(true);//defualt is not visible
		//getCaret().setVisible(true);
		StyleConstants.setForeground(defaultAttr,new Color(0xcccccc));
		StyleConstants.setForeground(nameAttr,new Color(0x66ccff));
		//StyleConstants.setBold(nameAttr,true);
		StyleConstants.setUnderline(linkOutAttr,false);
		StyleConstants.setForeground(linkAttr,new Color(0.7f,0.7f,1.0f));
		StyleConstants.setUnderline(linkOverAttr,true);
		StyleConstants.setForeground(linkexceptionAttr,Color.WHITE);
		StyleConstants.setBold(linkexceptionAttr,true);
		StyleConstants.setForeground(exceptionAttr,new Color(0xff,0x20,0x20));
		StyleConstants.setBackground(highBackgroundAttr,new Color(0x662020));
		StyleConstants.setBackground(unHighBackgroundAttr,new Color(1f,1f,1f,0f));
		StyleConstants.setForeground(fieldNameAttr,new Color(0xff99ff));
		StyleConstants.setForeground(classNameAttr,Color.WHITE);
		StyleConstants.setBold(classNameAttr,true);
		StyleConstants.setForeground(accessAttr1,new Color(0x99ff99));
		StyleConstants.setForeground(accessAttr2,new Color(0xffff00));
		//StyleConstants.setForeground(highBackgroundAttr,new Color(0x00,0x00,0x00));
		setCharacterAttributes(defaultAttr,true);
		lis=new UIListener();
		addMouseListener(lis);
		addMouseMotionListener(lis);
		setEditable(false);
		setOpaque(true);
		setBackground(Color.BLACK);

		tfCtMenu.addBoundComponent(this);
		linkElements=new TreeMap();

		listeners=
			new LinkedList<LinkExplorerListener>();

	}

	@Override
	public void updateUI()
	{
	    setUI(BasicTextPaneUI.createUI(this));
	}

	//@Override
	//protected void paintComponent(Graphics oldG){
	//
	//    Graphics g = oldG.create();
	//    g.setColor(Color.BLACK);
	//    g.fillRect(0, 0, getWidth(), getHeight());
	//    g.dispose();
	//    super.paintComponent(oldG);
	//}

	public void highLight(int start,int end)
	{
		getStyledDocument().setCharacterAttributes(start,end-start,highBackgroundAttr,false);
	}
	public void unHighLight(int start,int end)
	{
		getStyledDocument().setCharacterAttributes(start,end-start,unHighBackgroundAttr,false);
	}
	/**
	@param scrollToVis true if need to scroll the first find text
	@return how many found
	*/
	public int findText(String regex,java.util.List<int[]> result)
	{
		CharSequence cq=getCharSequence();
		Pattern p=Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
		Matcher m=p.matcher(cq);
		int i=0;

		while(m.find()){
			highLight(m.start(),m.end());
			result.add(new int[]{m.start(),m.end()});
			i++;
		}
		return i;
	}
	public synchronized CharSequence getCharSequence()
	{
		if(charSequence==null)
			charSequence=new DocCharSequence();
		return charSequence;
	}
	public synchronized void addLinkExpListener(LinkExplorerListener l)
	{
		listeners.add(l);
	}
	public synchronized void removeLinkExpListener(LinkExplorerListener l)
	{
		listeners.remove(l);
	}
	private synchronized void fireOnLink(LinkElement e)
	{
		for(LinkExplorerListener l: listeners){
			l.onLink(e);
		}
	}
	private synchronized void fireDoLink(LinkElement e,boolean b)
	{
		for(LinkExplorerListener l: listeners){
			l.doLink(e,b);
		}
	}
	private synchronized void fireOutlink(LinkElement e)
	{
		for(LinkExplorerListener l: listeners){
			l.outLink(e);
		}
	}
	public void appendLink(String link)
	{

	}
	public void printName(String str){
		invokePrintSyn(str,nameAttr);
	}


	public void printDefault(String str){
		invokePrintSyn(str,defaultAttr);
	}
	public void printLink(String str){
		invokePrintSyn(str,linkAttr);
		int end=getStyledDocument().getLength();
		int len=str.length();
		int start=end-len;
		linkElements.put(start,new LinkElement(start,end,str));
	}
	public void printLink(LinkElement le)
	{
		invokePrintSyn(le.getText(),linkAttr);
		int end=getStyledDocument().getLength();
		int len=le.getText().length();
		int start=end-len;
		le.setStart(start);
		le.setEnd(end);
		linkElements.put(start,le);
	}
	public void addCompoundLink(String str,String highlightStr,String cmpltStr){

		invokePrintSyn(str,linkAttr);
		invokePrintSyn(highlightStr,linkexceptionAttr);
		int end=getStyledDocument().getLength();
		int len=cmpltStr.length();
		LinkElement e=new  LinkElement(end-len,end,cmpltStr);
		linkElements.put(e.start(),e);
		//log.info(e.start()+", "+e.end());
	}

	/**
	@param attr This method will sort the list first.

	*/
	public void print(CharSequence cs,int start,int end,
		java.util.List<ExplorerElement> attr)
	{

		if(eComparator==null)
			eComparator=new ElementComparator();
		Collections.sort(attr,eComparator);
		for(ExplorerElement a:attr){
			//log.info(""+start+"->"+a.start());
			String text=null;
			try{
				if(start<=a.start()){
					printDefault(cs.subSequence(start,a.start()).toString());
				}
				text=a.getText();
				if(text==null){
					text=cs.subSequence(a.start(),a.end()).toString();
				}
				start=a.end();//a.end() may be changed by printLink method
				//log.info("end:"+start+" "+text);
				if(a instanceof LinkElement){
					printLink((LinkElement)a);
				}else {
					if(a.getAttr()!=null){
						invokePrintSyn(text,a.getAttr());
					}else if(a.getStyle()==ExplorerElement.LINK_STYLE){
						printLink(text);
					}else if(a.getStyle()==ExplorerElement.DEFAULT_STYLE){
						printDefault(text);
					}else if(a.getStyle()==ExplorerElement.HIGH_STYLE){
						invokePrintSyn(text,exceptionAttr);
					}else if(a.getStyle()==ExplorerElement.ACCESS_STYLE1){
						invokePrintSyn(text,accessAttr1);
					}else if(a.getStyle()==ExplorerElement.ACCESS_STYLE2){
						invokePrintSyn(text,accessAttr2);
					}else if(a.getStyle()==ExplorerElement.F_NAME_STYLE){
						invokePrintSyn(text,fieldNameAttr);
					}else if(a.getStyle()==ExplorerElement.C_NAME_STYLE){
						invokePrintSyn(text,classNameAttr);
					}else if(a.getStyle()==ExplorerElement.NAME_STYLE){
						printName(text);
					}
				}
			}catch(Exception ex){
				log.log(Level.SEVERE,"start="+start+", element.start()="+a.start()+", element.end()="+a.end()+"\ntext="+text,ex);
				break;
			}

		}
		printDefault(cs.subSequence(start,end).toString());
	}
	private class PrintTask implements Runnable
	{
		String str;
		AttributeSet attr;
		public PrintTask( String s,AttributeSet a)
		{
			setAll(s,a);
		}
		public void setAll( String s,AttributeSet a)
		{
			str=s;
			attr=a;
		}
		public void run()
		{
			try{
				int caretPos=getCaretPosition();
				StyledDocument doc=getStyledDocument();
				doc.insertString(doc.getLength(),str,attr);
				//log.info(""+getCaretPosition()+" "+str);
				if(caretPos!=getCaretPosition()){
					setCaretPosition(caretPos);//diable scroll pane auto scroll
				}

			}catch(BadLocationException ex){
				log.log(Level.SEVERE,"UI error",ex);
			}
		}
	}
	private void invokePrintSyn(String str,AttributeSet attr)
	{
		//log.info("invokePrintSyn");
		if(printTask==null)
			printTask=new PrintTask(str,attr);
		else
			printTask.setAll(str,attr);
		if(!SwingUtilities.isEventDispatchThread()){
			//log.info("swing invoke and wait");
			try{
				SwingUtilities.invokeAndWait(printTask);

			}catch(Exception iex)
			{
				log.info(iex.toString());
			}
		}else{
			//log.info("swing invoke and wait");
			new PrintTask(str,attr).run();
		}
	}
	public void cleanup()
	{
		try{
		setSelectionStart(0);setSelectionEnd(0);
		StyledDocument doc=getStyledDocument();
		doc.remove(0,doc.getLength());

		linkElements.clear();
		lis.cleanup();
		//setCaretPosition(0);
		//log.info("setCaretPosition(0);");
		}catch(BadLocationException ex){
			log.log(Level.SEVERE,"UI error",ex);
		}
	}

	private class UIListener implements MouseListener,MouseMotionListener
	{
		int lastPressStart=-1;
		int lastPressEnd=-1;
		//Map.Entry<Integer,Integer> lastHightLink;
		int lastLinkStart=-1;
		int lastLinkEnd=-1;
		public void cleanup()
		{
			lastPressStart=-1;
			lastPressEnd=-1;
			lastLinkStart=-1;
			lastLinkEnd=-1;
		}
		public void mouseClicked(java.awt.event.MouseEvent e)
		{

			if(e.getButton()!=e.BUTTON1)
				return;

			Point p=e.getPoint();
			int offset=viewToModel(p);
			if(offset<=lastPressEnd && offset>=lastPressStart){
				try{
					//String selectedLink=getStyledDocument().getText(lastPressStart,lastPressEnd-lastPressStart);

					fireDoLink(linkElements.get(lastPressStart),e.getClickCount()>1);
				}catch(Exception ex){
					log.log(Level.SEVERE,"",ex);
				}
			}
		}
		public void mousePressed(java.awt.event.MouseEvent e){
			Point p=e.getPoint();
			int offset=viewToModel(p);
			Map.Entry<Integer,LinkElement> en= linkElements.floorEntry(offset);
			if(en!=null && offset<=en.getValue().end()){
				try{
					lastPressStart=en.getKey();
					lastPressEnd=en.getValue().end();

				}catch(Exception ex){
					log.log(Level.SEVERE,"",ex);
				}
			}
		}
		public void mouseReleased(java.awt.event.MouseEvent e)
		{
			//Point p=e.getPoint();
			//int offset=viewToModel(p);
		}
		public void mouseEntered(java.awt.event.MouseEvent e){}
		public void mouseExited(java.awt.event.MouseEvent e){}
		public void mouseDragged(java.awt.event.MouseEvent e)
		{
		}
		public  void mouseMoved(java.awt.event.MouseEvent e)
		{
			Point p=e.getPoint();
			int offset=viewToModel(p);
			if(offset<=lastLinkEnd && offset>=lastLinkStart){
				//mouse moving on previous link
				return;
			}
			Map.Entry<Integer,LinkElement> en= linkElements.floorEntry(offset);
			if(en!=null&& offset<=en.getValue().end()){
				// mouse over new link

				try{
					if(lastLinkEnd>=0){
						getStyledDocument().setCharacterAttributes(lastLinkStart,lastLinkEnd-lastLinkStart,linkOutAttr,false);
					}
					int start=en.getValue().start();
					int end=en.getValue().end();
					getStyledDocument().setCharacterAttributes(start,end-start,linkOverAttr,false);
					lastLinkStart=start;
					lastLinkEnd=end;
					fireOnLink(en.getValue());
					//selectedLink=getStyledDocument().getText(en.getKey(),en.getValue());
				}catch(Exception ex){
					log.log(Level.SEVERE,"",ex);
				}
			}else{
				//mouse out a link
				if(lastLinkEnd>=0){
					fireOutlink(linkElements.get(lastLinkStart));
					getStyledDocument().setCharacterAttributes(lastLinkStart,lastLinkEnd-lastLinkStart,linkOutAttr,false);
					cleanup();
				}
			}
		}
	}

	/**
	* overridden from JEditorPane
	* to suppress line wraps
	*
	*

	*/
	@Override
   	public boolean getScrollableTracksViewportWidth() {
		if (getParent() instanceof JViewport) {
			javax.swing.plaf.TextUI ui = getUI();

			return (((JViewport)getParent()).getWidth() > ui.getPreferredSize(this).width);
		}
		return false;
	}

	private class ElementComparator<T> implements Comparator<T>
	{
		public int compare(T o1,T o2)
		{
			return ((ExplorerElement)o1).start()-((ExplorerElement)o2).start();
		}
		public boolean equals(Object o)
		{
			return this.equals(o);
		}
	}

	public class DocCharSequence implements CharSequence
	{
		public int length()
		{
			return getStyledDocument().getLength();
		}
		public char charAt(int i)
		{
			try{
			return getStyledDocument().getText(i,1).charAt(0);
			}catch(Exception e){
				log.log(Level.SEVERE,"DocCharSequence error, offset="+i,e);
				return '^';
			}
		}
		public CharSequence subSequence(int i,int j){
			try{
			return getStyledDocument().getText(i,j-i);
			}catch(Exception e){
				log.log(Level.SEVERE,"DocCharSequence error, start="+i+" end="+j,e);
				return "ERROR";
			}
		}
		public String toString()
		{
			return getText();
		}
	}
}
