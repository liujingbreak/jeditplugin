package org.liujing.jeditplugin.ui;

import java.awt.*;
import javax.swing.*;
import java.io.File;
import javax.swing.border.*;
import java.util.*;
import java.util.logging.*;
import java.awt.image.BufferedImage;
import org.liujing.jeditplugin.v2.*;
import java.awt.event.*;

public class FileSelectButton extends JButton implements ActionListener{
	private static Logger log = Logger.getLogger(ProjectPanel.class.getName());
	
	private static JFileChooser 		chooseDirDialog = new JFileChooser("/");
	private File 						defaultDir;
	private int 						selectMode = JFileChooser.FILES_AND_DIRECTORIES;
	boolean								multiSelection = false;
	JTextField							textField = null;
	StringBuilder						textbuf = new StringBuilder();
	LinkedList<FileSelectListener>		fileSelectLis;
	
	public FileSelectButton(){
		addActionListener(this);
	}
	
	public FileSelectButton(String name){
		super(name);
		addActionListener(this);
	}
	
	public FileSelectButton(Icon icon){
		super(icon);
		addActionListener(this);
	}
	
	public FileSelectButton(Action a){
		super(a);
		addActionListener(this);
	}
	
	public FileSelectButton(String text, Icon icon) {
		super(text, icon);
		addActionListener(this);
	}
	
	public void setDefaultPath(File f){
		defaultDir = f;
	}
	
	public void boundTextField(JTextField f){
		textField = f;
	}
	
	public void addFileSelectListener(FileSelectListener l){
		if(fileSelectLis == null){
			fileSelectLis = new LinkedList();
		}
		fileSelectLis.add(l);
	}
	
	public void removeFileSelectListener(FileSelectListener l){
		fileSelectLis.remove( l);
	}
	
	protected void fireFileSelected(File[] file){
		if(fileSelectLis == null)
			return;
		Iterator<FileSelectListener> it = fileSelectLis.iterator();
		while(it.hasNext()){
			it.next().onFileSelected(file, this);
		}
	}
	
	protected void fireButtonClicked(){
		if(fileSelectLis == null)
			return;
		Iterator<FileSelectListener> it = fileSelectLis.iterator();
		while(it.hasNext()){
			it.next().onFileSelectButtonClicked(this);
		}
	}
	
	/**
	JFileChooser.FILES_ONLY 
	JFileChooser.DIRECTORIES_ONLY 
	JFileChooser.FILES_AND_DIRECTORIES 
	*/
	public void setFileSelectionMode(int mode){
		selectMode = mode;
	}
	
	public void setMultiSelectionEnabled(boolean enable){
		multiSelection = enable;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		try{
			fireButtonClicked();
			if( textField != null && textField.getText().trim().length()>0){
				File f =  new File(textField.getText());
				if(!f.exists()){
					//log.info(f.getPath() + " not exists, parent=" + f.getParent());
					f = f.getParentFile();
					//log.info("get parent dir"+ f);
					
				}
				if( f != null && f.exists())
					chooseDirDialog.setCurrentDirectory(f);
			}else{
				chooseDirDialog.setCurrentDirectory(defaultDir);
			}
			//chooseDirDialog.setDialogTitle("Select Direcotries");
			chooseDirDialog.setFileSelectionMode(selectMode);
			chooseDirDialog.setMultiSelectionEnabled(multiSelection);
			int returnVal = chooseDirDialog.showDialog(null, getText());
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				if( chooseDirDialog.isMultiSelectionEnabled() ){
					File[] selectedFiles = chooseDirDialog.getSelectedFiles();
					textbuf.setLength(0);
					//log.fine(" selectedFiles len = " + selectedFiles.length);
					for(int i = 0; i< selectedFiles.length; i++){
						File f = selectedFiles[i];
						if( i > 0 )
							textbuf.append(',');
						textbuf.append( f.getPath() );
					}
					if( textField != null)
						textField.setText( textbuf.toString());
					fireFileSelected(selectedFiles);
				}else{
					if( textField != null)
						textField.setText(chooseDirDialog.getSelectedFile().getPath());
					fireFileSelected(new File[]{chooseDirDialog.getSelectedFile()});
				}
			}
		}catch(Exception ex){
			log.log(Level.SEVERE,"",ex);
		}
	}
	
	public static interface FileSelectListener{
		public void onFileSelectButtonClicked(JButton source);
		public void onFileSelected(File[] file, JButton source);
	}
}
