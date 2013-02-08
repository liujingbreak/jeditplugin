package org.liujing.jeditplugin.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import java.util.logging.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import javax.swing.plaf.basic.*;

public class MyListTableRender extends javax.swing.table.DefaultTableCellRenderer{
	static Logger log = Logger.getLogger(MyListTableRender.class.getName());
	
	private Color c1 = new Color(0xff, 0xff, 0xcc, 170);
	private Color c2 = new Color(0xff, 0xff, 0xff, 170);
	private CellComponent cell = new CellComponent();
	private Color HLForeground;
	private Color HLBackground;
	private Color origForeground = Color.BLACK;
	private Color foreground = Color.BLACK;
	private Color background;
	 
	public Component getTableCellRendererComponent(
		JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		try{
			JComponent origComp = (JComponent)super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
			
			if(isSelected || hasFocus){
				origComp.setOpaque(true);			
				return origComp;
			}
			
			if(row%2 == 1){
				cell.setTransparentColor(c1);
				origComp.setOpaque(false);
				//origComp.setBackground(c1);
			}else{
				cell.setTransparentColor(c2);
				origComp.setOpaque(false);
			}
			if(background != null){
				cell.setTransparentColor(background);
			}
				
			if(foreground != null)
				origComp.setForeground(foreground);
			Rectangle rect = table.getCellRect(row, column, true);
			cell.setCopy(origComp, rect);
			return cell;
		}catch(Exception ex){
			log.log(Level.WARNING, "", ex);
			return null;
		}
	}
	
	public void setHightlightColor(Color foreground, Color background){
		HLForeground = foreground;
		HLBackground = background;
	}
	
	public void hightlight(){
		foreground = HLForeground;
		background = HLBackground;
	}
	
	public void normal(){
		foreground = origForeground;
		background = null;
	}
	
	private class CellComponent extends JPanel{
		private JComponent comp;
		private Color color;
		private Dimension size;
		private Rectangle rect;
		
		public CellComponent(){
			setLayout(null);
			setOpaque(false);
		}
		
		public void setCopy(JComponent comp, Rectangle rect){
			this.comp = comp;
			removeAll();
			add(comp);
			size = comp.getPreferredSize();
			comp.setBounds(0, 0, rect.width, rect.height);
			this.rect = rect;
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
					scratchGraphics.fillRect(0, 0, rect.width, rect.height);
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
		 *
		 * @since 1.5
		 */
		public void invalidate() {}
		
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
		public void repaint(Rectangle r) { }
		
		/**
		 * Overridden for performance reasons.
		 * See the <a href="#override">Implementation Note</a> 
		 * for more information.
		 *
		 * @since 1.5
		 */
		public void repaint() {
		}
		/**
		     * Overridden for performance reasons.
		     * See the <a href="#override">Implementation Note</a> 
		     * for more information.
		     */
		    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { }
	}
	
	
}
