package org.liujing.jeditplugin;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.File;

public class FileRenameDialog extends JDialog
{
	JTextField input;
	private boolean okClicked=false;
	
	public FileRenameDialog()
	{
		setTitle("Rename File");
		Container cp=getContentPane();
		setLayout(new BoxLayout(cp,BoxLayout.Y_AXIS));
		
		
		
		JPanel p=new JPanel();
		p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));
		p.add(new JLabel("New Name "));
		input=new JTextField("");
		p.setAlignmentX(Component.LEFT_ALIGNMENT);
		p.add(input);
		cp.add(p);
		
		JComponent comp=createButtons();
		comp.setAlignmentX(Component.LEFT_ALIGNMENT);
		cp.add(comp);
		setModal(true);
	}
	
	public void popup(Component relative,String defaultName)
	{
		input.setText(defaultName);
		setLocationRelativeTo(relative);
		pack();
		setVisible(true);
		
	}
	public boolean isOkClicked()
	{
		return okClicked;
	}
	public String getResult()
	{
		return input.getText();
	}
	protected JComponent createButtons()
	{
		JPanel p=new JPanel();
		p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));
		
		JButton okbt=new JButton("Ok");
		okbt.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				try{
					okClicked=true;
					setVisible(false);
				}catch(Exception ex){
				}
			}
		});
		p.add(okbt);
		
		JButton cancelbt=new JButton("Cancel");
		cancelbt.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				try{
					okClicked=false;
					setVisible(false);
				}catch(Exception ex){
				}
			}
		});
		p.add(cancelbt);
		return p;
	}
}
