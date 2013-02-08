package org.liujing.awttools.classview;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.util.logging.*;
import java.util.concurrent.*;
import java.util.regex.*;

import liujing.swing.MyTextField;
import liujing.swing.FitWidthTable;
import liujing.swing.FileSelectButton;

public class ClassSearchConfigUI extends JPanel{
	private static Logger 	log 			= Logger.getLogger(ClassSearchConfigUI.class.getName());
	JTable					pathTable;
	PathTableModel			pathTableModel;
	TableEditor				tableEditor;
	EventHandler			handler 		= new EventHandler();
	private String defaultDirectory = ".";

	public ClassSearchConfigUI(){
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		pathTableModel = new PathTableModel(new ArrayList(), "Path Pattern");

		pathTable = new AutoSaveEditTable(pathTableModel);
		pathTable.setFont(new Font("DialogInput", Font.PLAIN ,14));
		tableEditor = new TableEditor();
		pathTable.setDefaultEditor(Object.class, tableEditor);
		pathTable.addMouseListener(handler);
		add(new JScrollPane(pathTable));
	}

	public void setDefaultDirectory(String target){
	    if(target == null)
	        defaultDirectory = ".";
	    else
	        defaultDirectory = target;
	}

	public void setClassPath(Collection<String> list){
		log.fine("list " + list);
		int size = pathTableModel.paths.size();

		pathTableModel.paths.clear();
		if(size>0)
		    pathTableModel.fireTableRowsDeleted(0, size - 1);
		if(list != null){
			pathTableModel.paths.addAll(list);
		}
		pathTableModel.fireTableRowsInserted(0, list.size() - 1);
		//pathTableModel.fireTableDataChanged();
		log.fine("list.hashCode() " + list.hashCode());
	}

	public Collection<String> getClassPath(){
		//tableEditor.saveDirtyField();
		//tableEditor.fireEditingStopped();
		if(pathTable.isEditing())
			tableEditor.doStopEdit();
		log.fine("");
		return new ArrayList(pathTableModel.paths);
	}

	public class EventHandler extends MouseAdapter{
		public void mouseClicked(MouseEvent e){
			if(e.getSource().equals(pathTable)){
				if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2){
					pathTableModel.setEditable(true);
					int row = pathTable.rowAtPoint(e.getPoint());
					pathTable.editCellAt(row, 0);
					pathTableModel.setEditable(false);
				}
			}
		}
	}

	class AutoSaveEditTable extends FitWidthTable{
		public AutoSaveEditTable(TableModel dm)
		{
			super(dm);
		}
		@Override
		public void removeEditor(){
			((TableEditor)getDefaultEditor(Object.class)).saveDirtyField();
			super.removeEditor();
		}
	}

	class PathTableModel  extends AbstractTableModel{
		public java.util.List<String> 		paths;
		String 						name;
		boolean						editable = false;

		public PathTableModel(java.util.List<String> paths, String name){
			this.paths = paths;
			this.name = name;
		}

		public int getColumnCount()
		{
			return 1;
		}
		public int getRowCount()
		{
			//log.fine(""+ (paths.size() + 1));
			return paths.size() + 1;
		}
		public Object getValueAt(int rowIndex,
                  int columnIndex)
		{
			if(rowIndex != paths.size() ){
				switch(columnIndex){
					//case 0:
					//	return Boolean.TRUE;
					default :

						return paths.get(rowIndex);
				}
			}else{
				switch(columnIndex){
					//case 0:
					//	return Boolean.FALSE;
					default :
						return "";
				}
			}
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex){
			//log.fine("set "+ rowIndex + ", " + columnIndex + " = " + aValue);
			if(columnIndex == 0){
				if(rowIndex >= paths.size()){
					String sValue = (String)aValue;
					if(sValue != null && sValue.trim().length() > 0){
						paths.add( sValue );
						int lastrow = rowIndex + 1;
						fireTableRowsInserted(lastrow, lastrow);
					}
				}else{
					String sValue = (String)aValue;
					if(sValue != null && sValue.trim().length() > 0){
						paths.set( rowIndex, (String)aValue );
					}else{
						paths.remove( rowIndex );
						fireTableRowsDeleted(rowIndex, rowIndex);
					}
				}
			}
		}

		public boolean isCellEditable(int row, int col) {
			return ( (row == paths.size())?true: editable );

		}

		public void setEditable(boolean b){
			editable = b;
		}

		public String getColumnName(int c)
		{
			switch(c){
				//case 0:
				//	return " ";
				default :
					return name;
			}
		}
	}

	class TableEditor extends AbstractCellEditor implements
	TableCellEditor, ActionListener, FileSelectButton.FileSelectListener
	{
		MyTextField			field 		= new MyTextField();
		FileSelectButton	fileButton = new FileSelectButton(" Select ");

		Object				value;
		int					row;
		int					column;
		JTable				table;
		boolean				dirty 		= false;
		JPanel				pathEditorPanel;

		public TableEditor(){
			field.setActionCommand("t");
			field.addActionListener(this);
			field.setFont(new Font("DialogInput", Font.PLAIN ,14));
			Dimension pdim = field.getPreferredSize();
			field.setMinimumSize(new Dimension(200, pdim.height));
			field.setMaximumSize(new Dimension(800, pdim.height));
			field.setPreferredSize(new Dimension(300, pdim.height));
			fileButton.addFileSelectListener(this);

			pathEditorPanel = new JPanel();
			pathEditorPanel.setOpaque(false);
			pathEditorPanel.setLayout(new BoxLayout(pathEditorPanel, BoxLayout.X_AXIS));
			pathEditorPanel.add(field);
			pathEditorPanel.add(fileButton);
		}

		public Object getCellEditorValue(){
			return value;
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column){

			this.row = row;
			this.column = column;
			this.table = table;

			dirty = true;

			this.value = value;
			field.setText((String)value);
			int oldWidth = table.getColumnModel().getColumn(column).getPreferredWidth();
			pathEditorPanel.invalidate();
			//int newWidth = pathEditorPanel.getPreferredSize().width;
			//if( newWidth > oldWidth )
			//	table.getColumnModel().getColumn(column).setPreferredWidth(newWidth);
			return pathEditorPanel;
		}

		public void actionPerformed(ActionEvent evt) {
			if( "t".equals(evt.getActionCommand()) ){
				value = field.getText();
				dirty = false;
				fireEditingStopped();
			}
		}

		public void doStopEdit(){
			value = field.getText();
			dirty = false;
			fireEditingStopped();
		}

		public void saveDirtyField(){
			if(dirty){
				try{
					String lastValue = field.getText();
					table.getModel().setValueAt(lastValue, this.row, this.column);
					dirty = false;
				}catch(Exception e){
					log.log(Level.WARNING, "", e);
				}
			}
		}

		/**
		implementation of FileSelectButton.FileSelectListener
		*/
		public void onFileSelected(File[] files, JButton source){
			String path = files[0].getPath();
			if(files[0].isDirectory())
			    path = path + File.separator + "*.jar";
			File defFile = files[0].getParentFile();
			if(defFile != null && defFile.exists())
			    fileButton.setDefaultPath(defFile);
			String rootdir = defaultDirectory;
			field.setText(path);

			this.value = path;
			dirty = false;
			fireEditingStopped();
		}

		/**
		implementation of FileSelectButton.FileSelectListener
		*/
		public void onFileSelectButtonClicked(JButton source){
			File df = new File(field.getText());
			//
			//if(df.exists())
			//	fileButton.setDefaultPath(df);
		}
	}
}
