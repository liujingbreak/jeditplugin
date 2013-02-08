package org.liujing.jeditplugin.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import java.util.logging.*;
import java.awt.event.*;
import org.liujing.jeditplugin.MyJeditPlugin;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import org.liujing.jeditplugin.v2.*;
import liujing.swing.*;

public class PluginPanel extends MyPanel{
	private static Logger log = Logger.getLogger(PluginPanel.class.getName());

	protected BufferController			bufferCtl;
	protected ProjectController			projectCtl;
	protected JSplitPane				splitPane;
	protected JTable					bufferList;
	protected BufferTableModel			bufferTableModel;
	protected JPopupMenu				bufferPopup;
	protected BufferListHandler			bufferListHandler = new BufferListHandler();


	public PluginPanel(){
		this.bufferCtl = new BufferController();
		projectCtl = new ProjectController();
		init();
		log.fine("new PluginPanel 0");
	}

	public PluginPanel(BufferController bufferCtl, ProjectController prjCtl){
		this.bufferCtl = bufferCtl;
		projectCtl = prjCtl;
		init();
		if(log.isLoggable(Level.FINE))
			log.fine("new PluginPanel (bufferCtl: "+ bufferCtl +")");
	}

	public<T> T getController(Class<T> cls){
		if(BufferController.class.isAssignableFrom(cls)){
			return (T)bufferCtl;
		}else if(ProjectController.class.isAssignableFrom(cls)){
			return (T)projectCtl;
		}else{
			return null;
		}
	}

	private void init(){
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		ProjectPanel projectPanel = new ProjectPanel(projectCtl, bufferCtl);

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createBufferList(), projectPanel);
		splitPane.setOpaque(false);

		projectPanel.setBufferTable(bufferList);


		add(splitPane);

		bufferPopup = new JPopupMenu();

		JMenuItem closeBufMi = new JMenuItem("Close Buffer");
		bufferPopup.add(closeBufMi);
		closeBufMi.addActionListener(bufferListHandler);
		try{
		    setBackgroundImg("Perast1920x12009-16-2010 9_32_46 AM.JPG", true,true,true);
		}catch(Exception e){
		    log.log(Level.WARNING, "failed to load background image", e);
		}
	}

	@Override
	public void addNotify(){
		super.addNotify();
		SwingUtilities.invokeLater(new Runnable()
				{
					public void run(){
						splitPane.setDividerLocation(0.45d);
						log.fine("resize splitPane");
					}
			});
	}
	@Override
	public void removeNotify(){
		log.fine("removeNotify");
		super.removeNotify();
		//MyJeditPlugin.getInstance().getPanelFactory().recycle(this);
	}

	protected JComponent createBufferList()
	{
		bufferList = new FitWidthTable();
		bufferList.setFont(new Font("Ebrima", Font.PLAIN , 14));
		bufferList.setDefaultRenderer(Object.class, new MyBufferListTableRender());

		bufferTableModel = new BufferTableModel(bufferCtl.getBufferList());
		bufferCtl.addBufferListener(bufferTableModel);

		bufferList.setModel(bufferTableModel);
		bufferList.addMouseListener(bufferListHandler);
		bufferList.setOpaque(false);
		bufferList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane listScroller = new JScrollPane(bufferList);
		listScroller.setOpaque(false);
		listScroller.getViewport().setOpaque(false);


		return listScroller;
	}


	/*@Override
	public void addNotify(){
		log.fine("plugin addNotify");
		super.addNotify();
	}

	@Override
	public void removeNotify(){
		log.fine("plugin removeNotify");
		org.liujing.jeditplugin.MyJeditPlugin.getInstance().removePluginPanel(this);
		//bufferCtl = null;
		//projectCtl = null;

	}*/

	@Override
	public void finalize(){
		log.fine("finalize");
	}

	class BufferListHandler extends MouseAdapter implements ActionListener{
		 public void mouseClicked(MouseEvent e) {
			 if (e.getClickCount() == 2 && e.getButton()==e.BUTTON1) {
				 int index = bufferList.rowAtPoint(e.getPoint());
				 if(log.isLoggable(Level.FINE))
				 	 log.fine("clicked " + bufferCtl);
				 bufferCtl.activateBuffer(index);
				 if(log.isLoggable(Level.FINE))
				 	 log.fine("bufferCtl.activateBuffer(index) done " + bufferCtl);
			 }else if(e.getButton()==e.BUTTON3){
			 	 int[] items = bufferList.getSelectedRows();
			 	 if(items != null && items.length>0)
			 	 	 bufferPopup.show(bufferList, e.getX(), e.getY());
			 }
		 }

		 public void actionPerformed(ActionEvent e){

		 	 bufferCtl.closeBuffer(bufferList.getSelectedRows());
		 }
	}

	protected class MyBufferListTableRender extends MyListTableRender
	{
		protected JLabel currentItem;
		protected Color origForeColor;
		protected Color origBackColor;

		//protected JLabel normalItem;
		public MyBufferListTableRender(){
			currentItem=new JLabel();
			currentItem.setFont(new Font("Ebrima", Font.PLAIN , 14));
			currentItem.setForeground(Color.WHITE);
			currentItem.setOpaque(true);
			//currentItem.setFont(font);
			currentItem.setBackground(Color.GRAY);
			origBackColor = getBackground();
			origForeColor = getForeground();
			setHightlightColor(Color.WHITE, new Color(0xff, 0, 0, 200));
		}

		public  Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			if(row==0){
				//setForeground(Color.BLUE);
				if(isSelected){
					currentItem.setBackground(table.getSelectionBackground());
				}else if(bufferCtl.isBufferDirty(row)){
					currentItem.setBackground(Color.RED);
					currentItem.setForeground(Color.WHITE);
				}else{
					currentItem.setBackground(Color.GRAY);
				}
				currentItem.setText((String)value);
				return currentItem;
			}
			else{
				String sValue=(String)value;

				if(bufferCtl.isBufferDirty(row)){
					hightlight();
					//cell.setBackground(Color.RED);
					//cell.setForeground(Color.WHITE);
					//cell.setOpaque(true);
				}else{
					normal();
					//cell.setForeground(Color.BLACK);
					////cell.setBackground();
					//cell.setOpaque(false);
				}
				JComponent cell = (JComponent)super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
				return cell;

			}
		}
	}

	private static class BufferTableModel extends AbstractTableModel implements BufferController.BufferListener
	{
		public java.util.List<? extends BufferController.BufferInterf> buffer;

		/**
		Basic constructor for MyTableModel
		*/
		public BufferTableModel(java.util.List<? extends BufferController.BufferInterf> bufferList)
		{
			buffer = bufferList;
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
			BufferController.BufferInterf b = buffer.get(rowIndex);
			if(columnIndex==0){
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

		public void bufferClosed(int i, BufferController.BufferInterf buf)
		{
			fireTableRowsDeleted(i,i);
		}

		public void bufferActivated(int index, BufferController.BufferInterf buf){
			fireTableRowsDeleted(index, index);
			fireTableRowsInserted(0, 0);
		}

		public void bufferCreated(int index, BufferController.BufferInterf buf){
			if(index < 0)
				index = this.buffer.size() + 1 + index;

			fireTableRowsInserted(index, index);
		}

		public void bufferUpdated(int index, BufferController.BufferInterf buf){
			fireTableCellUpdated(index, 0);
			fireTableCellUpdated(index, 1);
		}

		public void listOrderChange(){
			fireTableDataChanged();
		}
	}
}
