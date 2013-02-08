package org.liujing.jeditplugin.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.util.*;
import java.util.logging.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;

public class MyTreeCellRender extends DefaultTreeCellRenderer{
	private static Logger log = Logger.getLogger(MyTreeCellRender.class.getName());
	
	private boolean selected;
	private boolean expended;
	private boolean leaf;
	private boolean hasFocus;

	private static Color leafBgCol = new Color(0, 0, 0, 140);
	private static Color noLeafBgCol = new Color(100, 100, 0, 140);
	private static Color expendedBgCol = new Color(0, 0, 100, 140);
	
	public MyTreeCellRender(){
	    //setOpaque(false);
		setBackgroundNonSelectionColor(new Color(0, 0, 0, 0));
		setTextNonSelectionColor(new Color(0xff, 0xff, 0xff, 0xff));
	}
	
	@Override
	protected void paintComponent(Graphics oldG){
	    if(! (selected || hasFocus)){
	        Graphics g = oldG.create();
	        if(leaf)
	        {
	            g.setColor(leafBgCol);
	        }else if(expended)
	        {
	            g.setColor(expendedBgCol);
	        }
	        else
	        {
	            g.setColor(noLeafBgCol);
	        }
	        
	        g.fillRect(0, 0, getWidth(), getHeight());
	        g.dispose();
	    }
	    super.paintComponent(oldG);
	}
	
	public Component getTreeCellRendererComponent(JTree tree, Object value,
						  boolean sel,
						  boolean expanded,
						  boolean leaf, int row,
						  boolean hasFocus)
	{
		JComponent orig = (JComponent)super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		this.selected = sel;
		this.expended = expanded;
		this.leaf = leaf;
		this.hasFocus = hasFocus;
		return orig;
	}
	
	
	private class CellComponent extends JPanel{
		private JComponent comp;
		private Color color = new Color(0xcc, 0xff, 0x99, 170);//#ccff66
		private Dimension size;
		//private Rectangle rect;
		
		public CellComponent(){
			setLayout(null);
			setOpaque(false);
		}
		
		public void setCopy(JComponent comp){
			this.comp = comp;
			removeAll();
			add(comp);
			size = comp.getPreferredSize();
			comp.setBounds(0, 0, size.width, size.height);
			//this.rect = rect;
			setPreferredSize(size);
		}
		
		
		public void setTransparentColor(Color c){
			color = c;
		}
		
		@Override
		public void paintComponent(Graphics g){
			try{
				Graphics scratchGraphics = (g == null) ? null : g.create();
				try {
					scratchGraphics.setColor(color);
					//Rectangle rect = table.getCellRect(row, column, true);
					// log.info(rect.toString());
					scratchGraphics.fillRect(0, 0, size.width, size.height);
				}finally {
					scratchGraphics.dispose();
				}
				super.paintComponent(g);
			}catch(Exception ex){
				log.log(Level.WARNING, "", ex);
			}
		}
		
		/**
		    * Overridden for performance reasons.
		    * See the <a href="#override">Implementation Note</a>
		    * for more information.
		    */
		    public void validate() {}
		
		   /**
		    * Overridden for performance reasons.
		    * See the <a href="#override">Implementation Note</a>
		    * for more information.
		    *
		    * @since 1.5
		    */
		    public void invalidate() {}
		
		   /**
		    * Overridden for performance reasons.
		    * See the <a href="#override">Implementation Note</a>
		    * for more information.
		    */
		    public void revalidate() {}
		
		   /**
		    * Overridden for performance reasons.
		    * See the <a href="#override">Implementation Note</a>
		    * for more information.
		    */
		    public void repaint(long tm, int x, int y, int width, int height) {}
		
		   /**
		    * Overridden for performance reasons.
		    * See the <a href="#override">Implementation Note</a>
		    * for more information.
		    */
		    public void repaint(Rectangle r) {}
		
		   /**
		    * Overridden for performance reasons.
		    * See the <a href="#override">Implementation Note</a>
		    * for more information.
		    *
		    * @since 1.5
		    */
		    public void repaint() {}
		
		   /**
		    * Overridden for performance reasons.
		    * See the <a href="#override">Implementation Note</a>
		    * for more information.
		    */
		    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {	
			// Strings get interned...
			if (propertyName == "text"
				|| ((propertyName == "font" || propertyName == "foreground")
				    && oldValue != newValue
				    && getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null)) {
		
			    super.firePropertyChange(propertyName, oldValue, newValue);
			}
		    }
		
		   /**
		    * Overridden for performance reasons.
		    * See the <a href="#override">Implementation Note</a>
		    * for more information.
		    */
		    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {}
		
		   /**
		    * Overridden for performance reasons.
		    * See the <a href="#override">Implementation Note</a>
		    * for more information.
		    */
		    public void firePropertyChange(String propertyName, char oldValue, char newValue) {}
		
		   /**
		    * Overridden for performance reasons.
		    * See the <a href="#override">Implementation Note</a>
		    * for more information.
		    */
		    public void firePropertyChange(String propertyName, short oldValue, short newValue) {}
		
		   /**
		    * Overridden for performance reasons.
		    * See the <a href="#override">Implementation Note</a>
		    * for more information.
		    */
		    public void firePropertyChange(String propertyName, int oldValue, int newValue) {}
		
		   /**
		    * Overridden for performance reasons.
		    * See the <a href="#override">Implementation Note</a>
		    * for more information.
		    */
		    public void firePropertyChange(String propertyName, long oldValue, long newValue) {}
		
		   /**
		    * Overridden for performance reasons.
		    * See the <a href="#override">Implementation Note</a>
		    * for more information.
		    */
		    public void firePropertyChange(String propertyName, float oldValue, float newValue) {}
		
		   /**
		    * Overridden for performance reasons.
		    * See the <a href="#override">Implementation Note</a>
		    * for more information.
		    */
		    public void firePropertyChange(String propertyName, double oldValue, double newValue) {}
		
		   /**
		    * Overridden for performance reasons.
		    * See the <a href="#override">Implementation Note</a>
		    * for more information.
		    */
		    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
	}
}
