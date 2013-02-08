package org.liujing.jeditplugin;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.File;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.buffer.*;
import org.gjt.sp.jedit.browser.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.io.*;
import org.liujing.util.*;


public class ProjectAddFileDialog extends JDialog
{
	private static LogThread log=new LogThread(ProjectAddFileDialog.class);
	protected VFSBrowser vfs;
	public JFileChooser chooseDirDialog=new JFileChooser(".");
	protected String path;
	protected String prjName;
	protected JTextField regexFd;
	
	/**
	Basic constructor for ProjectAddFileDialog
	*/
	public ProjectAddFileDialog(String path)
	{
		super(jEdit.getActiveView(),"Add Files",true);
		setPath(path);
		//vfs=new VFSBrowser(jEdit.getActiveView(),path,VFSBrowser.BROWSER,true,DockableWindowManager.RIGHT);
		init();
	}
	
	protected void init()
	{
		
		Container cp=getContentPane();
		setLayout(new BoxLayout(cp,BoxLayout.Y_AXIS));
		
		//chooseDirDialog=new JFileChooser(".");
		//vfs.setAlignmentX(Component.LEFT_ALIGNMENT);
		//cp.add(vfs);
		JComponent comp=createButtons();
		comp.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		regexFd=new JTextField(".*\\.((java)|(py)|(xml)|(jsp)|(properties))");
		regexFd.setAlignmentX(Component.LEFT_ALIGNMENT);
		cp.add(regexFd);
		cp.add(comp);
		pack();
	}
	/**
	 * Returns the value of path.
	 */
	public String getPath()
	{
		return path;
	}

	/**
	 * Sets the value of path.
	 * @param path The value to assign path.
	 */
	public void setPath(String path)
	{
		this.path = path;
	}
	public void setProjectName(String name)
	{
		prjName=name;
	}

	protected JComponent createButtons()
	{
		JPanel p=new JPanel();
		p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));
		JButton addBt=new JButton("Brows");
		addBt.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				try{
					/*VFSFile[] vfses=vfs.getSelectedFiles();
					log.debug("select num: "+vfses.length);
					File[] fs=new File[vfses.length];
					for(int i=0;i<vfses.length;i++){
						fs[i]=new File(vfses[i].getPath());
					}*/
					chooseDirDialog.setCurrentDirectory(new File(path));
					chooseDirDialog.setDialogTitle("Select Files or Direcotries");
					chooseDirDialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
					chooseDirDialog.setMultiSelectionEnabled(true);
					int returnVal = chooseDirDialog.showOpenDialog(ProjectAddFileDialog.this);
					if (returnVal == JFileChooser.APPROVE_OPTION)
					{
						//setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						File[] files = chooseDirDialog.getSelectedFiles();
						MyProject bean=MyProject.getInstance();
						int sum=0;
						for(int i=0;i<files.length;i++){
							if(files[i].isFile()){
								sum+=bean.addFiles(new File[]{files[i]},prjName,".*");
							}else{
								sum+=bean.addFiles(new File[]{files[i]},prjName,regexFd.getText());
							}
						};
						//int sum=bean.addFiles(files,prjName,regexFd.getText());
						JOptionPane.showMessageDialog(ProjectAddFileDialog.this,"Total: "+sum,
								"Completed",JOptionPane.PLAIN_MESSAGE);
					}
					

				}catch(Exception ex){
					log.debug("",ex);
				}
			}
		});
		p.add(addBt);
		return p;
	}

}
