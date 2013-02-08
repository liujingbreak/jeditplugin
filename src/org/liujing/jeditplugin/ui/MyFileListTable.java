package org.liujing.jeditplugin.ui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;
import javax.swing.table.*;
import org.liujing.util.*;
import org.liujing.awttools.*;
import java.util.logging.*;

public class MyFileListTable extends MyDefaultTable
{
	private static Logger log=Logger.getLogger(MyFileListTable.class.getName());
	public MyFileListTable()
	{
		super();
	}
	public MyFileListTable(TableModel dm)
	{
		super(dm);
	}
	
	protected void init()
	{
		super.init();
		setRowHeight(16);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		enableAutoPack(true);
	}
	public TableCellRenderer getCellRenderer(int row, int column)
	{
		//log.info(row+", "+column);
		return super.getCellRenderer(row, column);
	}
	protected  void resizeTable()
	{
		if(getColumnCount()>1){
		packColumn(this,0);
		packColumn(this,1);
		}
	}
	
	/**
	Overrides <code>JComponent</code>'s <code>getToolTipText</code>
	*/
	public String getToolTipText(MouseEvent e)
	{
		String tip = null;
		java.awt.Point p = e.getPoint();
		int rowIndex = rowAtPoint(p);
		//int colIndex = columnAtPoint(p);
		//int realColumnIndex = convertColumnIndexToModel(colIndex);
		TableModel model = getModel();
		tip=(String)model.getValueAt(rowIndex,1);
		return tip;
	}

}
