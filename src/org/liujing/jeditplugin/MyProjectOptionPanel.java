package org.liujing.jeditplugin;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.logging.*;
import java.io.*;
import java.beans.*;

import org.liujing.jeditplugin.ui.*;

/**
*/
public class MyProjectOptionPanel extends JPanel implements ActionListener
{
	private static Logger log=Logger.getLogger(MyProjectOptionPanel.class.getName());
	private JFileChooser chooseDirDialog=new JFileChooser("/");
	private MyTextField rootDir;
	private MyTextField fileFilter;
	private JCheckBox inMemory;
	private JButton addCPBtn;
	private JTextPane cpTf;
	private final PropertyChangeSupport pcs= new PropertyChangeSupport( this );
	
	protected JDialog dialog;
	
	public MyProjectOptionPanel()
	{
		
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		JPanel p1=new JPanel();
		p1.setLayout(new BoxLayout(p1,BoxLayout.X_AXIS));
		p1.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		p1.add(new JLabel("Project Root Directory: "));
		rootDir=new MyTextField();
		p1.add(rootDir);
		JButton jb=new JButton("Select");
		jb.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				try{
					chooseDirDialog.setCurrentDirectory(new File(rootDir.getText()));
					chooseDirDialog.setDialogTitle("Select Direcotries");
					chooseDirDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					chooseDirDialog.setMultiSelectionEnabled(false);
					int returnVal = chooseDirDialog.showDialog(null,"Set as Root Directry");
					if (returnVal == JFileChooser.APPROVE_OPTION)
					{
						File selectedFile= chooseDirDialog.getSelectedFile();
						pcs.firePropertyChange("rootDir",rootDir.getText(),selectedFile.getAbsolutePath());
						rootDir.setText(selectedFile.getAbsolutePath());
						
					}
				}catch(Exception ex){
					log.log(Level.SEVERE,"",ex);
				}
			}
		});
		p1.add(jb);
		p1.setMaximumSize(new Dimension(Integer.MAX_VALUE,28));
		add(p1);
		
		//==============
		JPanel p2=new JPanel();
		p2.setLayout(new BoxLayout(p2,BoxLayout.X_AXIS));
		p2.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		p2.add(new JLabel("File Filter: "));
		fileFilter=new MyTextField();
		p2.add(fileFilter);
		p2.setMaximumSize(new Dimension(Integer.MAX_VALUE,28));
		add(p2);
		
		//----------class path---------
		JPanel cpPan=new JPanel();
		cpPan.setLayout(new BoxLayout(cpPan,BoxLayout.X_AXIS));
		addCPBtn=new JButton("Class path");
		addCPBtn.addActionListener(this);
		cpPan.add(addCPBtn);
		cpTf=new JTextPane();
		JScrollPane jp=new JScrollPane(cpTf);
		cpPan.add(jp);
		//jp.setMaximumSize(new Dimension(250,900));
		add(cpPan);
		
		inMemory=new JCheckBox("Always in memory");
		//inMemory.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(inMemory);
		//================
		/*JPanel p3=new JPanel();
		p3.setLayout(new BoxLayout(p3,BoxLayout.X_AXIS));
		p3.add();*/
		
	}
	/**
	add listener to listen for property change
	<ul>
	<li>property "OK" change - OK button clicked
	<li>property "rootDir" change- brow button selected
	</ul>
	*/
	public void addChangeListener( PropertyChangeListener listener )
	{
		//super.addPropertyChangeListener(listener);
		this.pcs.addPropertyChangeListener( listener );
		
	}
	
	public void removeChangeListener( PropertyChangeListener listener )
	{
		//super.removePropertyChangeListener(listener);
		this.pcs.removePropertyChangeListener( listener );
	}

	public void setValues(MyProjectOption b)
	{
		rootDir.setText(b.getRootPath());
		fileFilter.setText(b.getFilter());
		inMemory.setSelected(b.isStayMemory());
		cpTf.setText(b.getClasspath());
	}
	
	public void getValue(MyProjectOption b)
	{
		b.setRootPath(rootDir.getText());
		b.setFilter(fileFilter.getText());
		b.setStayMemory(inMemory.isSelected());
		b.setClasspath(cpTf.getText());
	}
	public void actionPerformed(ActionEvent e)
	{
		try{
			if(e.getSource()==addCPBtn){
				chooseDirDialog.setCurrentDirectory(new File(rootDir.getText()));
				chooseDirDialog.setDialogTitle("Add Class Path");
				chooseDirDialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				chooseDirDialog.setMultiSelectionEnabled(true);
				int returnVal = chooseDirDialog.showDialog(null,"Add");
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					File []files=chooseDirDialog.getSelectedFiles();
					StringBuilder sb=new StringBuilder(cpTf.getText().trim());
					
					if(sb.length()>0 && sb.charAt(sb.length()-1)!=File.pathSeparatorChar)
						sb.append(File.pathSeparatorChar);
					for(int i=0;i<files.length;i++){
						File f=files[i];
						addFileToClasspath(sb,f);
					}
					if(sb.length()>0 &&
						sb.charAt(sb.length()-1)==File.pathSeparatorChar){
						sb.deleteCharAt(sb.length()-1);
					}
					cpTf.setText(sb.toString());
				}
			}
		}catch(Exception ex){
			log.log(Level.SEVERE,"",ex);
		}
	}
	private void addFileToClasspath(StringBuilder s,File f)
	{
		if(f.isFile()){
			String name=f.getName();
			int len=name.length();
			if(len>4 && name.substring(len-4).equalsIgnoreCase(".jar")){
				s.append(f.getPath());
				s.append(f.pathSeparator);
			}
		}else if(f.isDirectory()){
			s.append(f.getPath());
			s.append(f.pathSeparator);
		}
	}
	
	public void showDialog(String projectName)
	{
		if(dialog==null)
		{
			
			dialog=new JDialog((Frame)null,"Project Option: "+projectName,true);
			dialog.getContentPane().add(this);
			
			JPanel p1=new JPanel();
			p1.setLayout(new BoxLayout(p1,BoxLayout.X_AXIS));
			p1.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			JButton okbt=new JButton("Ok");
			okbt.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					try{
						pcs.firePropertyChange("OK",false,true);
						dialog.setVisible(false);
					}catch(Exception ex){
					}
				}
			});
			p1.add(okbt);
			
			JButton cancelbt=new JButton("Cancel");
			cancelbt.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					try{
						dialog.setVisible(false);
					}catch(Exception ex){
					}
				}
			});
			p1.add(cancelbt);
			add(p1);
		}
		dialog.setSize(400,300);
		dialog.setLocationRelativeTo(null);
		
		dialog.setVisible(true);
	}
}
