package org.liujing.jeditplugin;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.awt.image.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.buffer.*;
import org.liujing.util.*;
import org.liujing.awttools.*;
import org.liujing.jeditplugin.ui.*;
import java.util.logging.*;
import java.beans.*;
import org.liujing.awttools.classview.*;

public class MyJeditPluginPanel extends JPanel implements KeyListener
	,ActionListener,WindowListener
{
	private static LogThread log=new LogThread(MyJeditPluginPanel.class);
	private static Logger logger=Logger.getLogger(MyJeditPluginPanel.class.getName());
	
	protected JFileChooser chooseDirDialog=new JFileChooser(".");
	protected MyFileListTable bufferList;
	protected JTextField indexField;
	protected MyFileListTable fileTable;
	protected ProjectAddFileDialog addDialog;
	protected FileRenameDialog renameDialog;
	protected JSplitPane splitPane;
	protected JComboBox projectList;
	protected JPopupMenu fileItemMenu=new JPopupMenu();
	protected javax.swing.Timer timer;
	protected static int SEARCH_TIMER_DELAY=777;
	protected MyProjectOptionPanel optionPanel;
	protected JCheckBox sortButton=null;
	protected JButton prjActionBtn;
	private View parentView;
	private JPopupMenu actionMenu;
	private JToggleButton classview;
	private ClassSearchUI classSearcher;
	private BufferedImage backgroundImg;
	
	public static final String PRJ_ADD_ITEM="Add New Project...";
	/**
	Basic constructor for MyJeditPluginPanel
	*/
	public MyJeditPluginPanel()
	{
		setPreferredSize(new Dimension(280,2000));
		setOpaque(false);
		try{
			backgroundImg = javax.imageio.ImageIO.read(
			this.getClass().getClassLoader().getResourceAsStream("org/liujing/res/panelBg.jpg"));
		}catch(java.io.IOException ioe){
			    logger.log(Level.SEVERE,"failed to load background image",ioe);
		}
		optionPanel=new MyProjectOptionPanel();
		optionPanel.addChangeListener(new PropertyChangeListener()
			{
				public  void propertyChange(PropertyChangeEvent evt)
				{
					try{
						if("OK".equals(evt.getPropertyName())){
							String name=getCurrentProjectName();
							MyProjectOption o=MyProject.getInstance().getOption(name);
							MyProjectOptionPanel op=(MyProjectOptionPanel)evt.getSource();
							op.getValue(o);
							MyProject.getInstance().saveOption(name);
							logger.fine("ok");
						}
					}catch(Exception ex){
						logger.log(Level.SEVERE,"Can't save options",ex);
					}
				}
			});
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		splitPane = new MySplitPane(JSplitPane.VERTICAL_SPLIT,createBufferPanel(),createProjectPanel());
		splitPane.setOpaque(false);
		
		SwingUtilities.invokeLater(new Runnable()
			{
				public void run(){
					splitPane.setDividerLocation(0.45d);
				}
		});
		add(splitPane);
		//SwingUtilities.getWindowAncestor(this);
		synchronized(MyJeditPlugin.getInstance().panels){
			MyJeditPlugin.getInstance().panels.add(this);
		}
	}
	private boolean slipPaneDivider=false;
	public void paint(Graphics g)
	{
		super.paint(g);
		//logger.info(""+(int)splitPane.getSize().getHeight()/2);
		//if(!slipPaneDivider){
		//splitPane.setDividerLocation((int)splitPane.getSize().getHeight()/2);
			slipPaneDivider=true;
	}
	@Override
	protected void paintComponent(Graphics g) {
		if (ui != null) {
		    Graphics scratchGraphics = (g == null) ? null : g.create();
		    try {
			//if (this.isOpaque()) {
			fillBgImgAtColumn(0, backgroundImg, g, this);
			
			//}
		    }catch(Exception ex){
			    logger.severe(ex.toString());
		    }finally {
			scratchGraphics.dispose();
		    }
		}
	}
	
	private static void fillBgImgAtColumn(int x, BufferedImage image, Graphics g, JComponent c){
		
		int y = 0;
		do{
			g.drawImage(image, x, y, null);
			y += image.getHeight();
			
		}while(y<c.getHeight());			
	}
	static public class MySplitPane extends JSplitPane
	{
		public MySplitPane(int newOrientation, Component newLeftComponent, Component newRightComponent){
			super(newOrientation, newLeftComponent, newRightComponent);
		}
		public void paint(Graphics g)
		{
			super.paint(g);
			//logger.info("split "+getMaximumDividerLocation());
		}
		
	}
	
	public void actionPerformed(ActionEvent e)
	{
		try{
			if(e.getSource()==prjActionBtn){
				actionMenu.show(prjActionBtn,0,prjActionBtn.getSize().height);
			}else if(e.getSource()==classview){
				if(classSearcher==null){
					classSearcher=MyJeditPluginFactory.createClassSearchUI();
					classSearcher.getSimpleDialog(jEdit.getActiveView()).addWindowListener(this);
					classSearcher.setBgWorker(MyJeditPlugin.singleWorker);
				}
				MyProjectOption mypo=MyProject.getInstance().getOption(getCurrentProjectName());
				classSearcher.getSimpleDialog(jEdit.getActiveView()).setVisible(classview.isSelected());
				classSearcher.getClassScanner().setClasspath(mypo.getClasspath());
				
			}
		}catch(Exception ex){
			logger.log(Level.SEVERE,"",ex);
		}
	}
	public void windowActivated(WindowEvent e){}
	public void windowClosing(WindowEvent e)
	{
		//if(e.getWindow()==classRefDg){
			classview.setSelected(false);
		//}
	}
	public void windowDeactivated(WindowEvent e){}
	public void windowDeiconified(WindowEvent e){}
	public void windowIconified(WindowEvent e){}
	public void windowOpened(WindowEvent e) {}
	public void windowClosed(WindowEvent e){}
	/**
	 * Returns the value of bufferList.
	 */
	public JTable getBufferList()
	{
		return bufferList;
	}
	/**
	 * Returns the value of projectList.
	 */
	public JComboBox getProjectList()
	{
		return projectList;
	}
	
	protected JComponent createBufferList()
	{
		bufferList=new MyFileListTable();
		bufferList.setDefaultRenderer(Object.class,new MyBufferListTableRender());
		bufferList.setModel(MyJeditPlugin.getInstance().getBufferList());
		bufferList.addMouseListener(new MouseAdapter() {
		     public void mouseClicked(MouseEvent e) {
			 if (e.getClickCount() == 2 && e.getButton()==e.BUTTON1) {
				 int index=bufferList.rowAtPoint(e.getPoint());
			     MyJeditPlugin.getInstance().activeBuffer(index);
			  }
		     }
		});
		bufferList.setOpaque(false);
		JScrollPane listScroller = new JScrollPane(bufferList);
		listScroller.setOpaque(false);
		listScroller.getViewport().setOpaque(false);
		return listScroller;
	}
	
	protected JComponent createButtons()
	{
		JPanel p=new JPanel();
		p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));
		JButton delBut=new JButton("C");
		delBut.setToolTipText("Close selected buffer");
		delBut.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				try{
				MyJeditPlugin.getInstance().closeBuffer(bufferList.getSelectedRows());
				}catch(Exception ex){
					log.debug("",ex);
				}
			}
		});
		p.add(delBut);

		return delBut;
	}
	protected JComponent createBufferPanel()
	{
		JPanel bufferPanel=new JPanel();
		bufferPanel.setOpaque(false);
		JComponent comp=null;
		bufferPanel.setLayout(new BoxLayout(bufferPanel,BoxLayout.Y_AXIS));
		comp=createBufferList();
		comp.setAlignmentX(Component.LEFT_ALIGNMENT);
		bufferPanel.add(comp);
		//comp=createButtons();
		//comp.setAlignmentX(Component.LEFT_ALIGNMENT);
		//bufferPanel.add(comp);		
		return bufferPanel;
	}
	protected void prepareSearchTimer()
	{
		timer=new javax.swing.Timer(SEARCH_TIMER_DELAY, new ActionListener() {
		      public void actionPerformed(ActionEvent evt) {
			      search();
			      //...Perform a task...
		      }
		});
		
	}
	protected void search(){
		//log.debug(indexField.getText());
		try{
			MyProject.getInstance().search(getCurrentProjectName(),indexField.getText());
		}
		catch(Exception ex){
			log.debug("",ex);
		}
	}
	private void searchStart()
	{
		if(timer.isRunning())
			timer.stop();
		timer.setDelay(SEARCH_TIMER_DELAY);
		timer.setRepeats(false);
		timer.start();
	}
	protected String getCurrentProjectName()
	{
		return (String)projectList.getSelectedItem();
	}
	protected void prepareFileItemPopupMenu()
	{
		JMenuItem menuItem = new JMenuItem("Remove from list");
		menuItem.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					try{
					MyProject.getInstance().remove(	getCurrentProjectName(),
						fileTable.getSelectedRows());
					}catch(Exception ex){
						log.debug("",ex);
					}
				}
			}
			);
		fileItemMenu.add(menuItem);
		
		menuItem = new JMenuItem("Delete from disk");
		menuItem.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					// todo: delete file from disk
					if(JOptionPane.YES_OPTION==JOptionPane.showConfirmDialog(
						jEdit.getActiveView(),"Are you sure you want delete this file from disk?",
						"Confirm Delete",JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE))
					{
						try{
							MyProject.getInstance().removeAndDelete(getCurrentProjectName(),
								fileTable.getSelectedRows());
						}catch(Exception ex){
						log.debug("",ex);
					}
					}
				}
			}
			);
		fileItemMenu.add(menuItem);
		
		menuItem = new JMenuItem("Rename");
		menuItem.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					try{
						int index=fileTable.getSelectedRow();
						File f=MyProject.getInstance().getFile(getCurrentProjectName(),index);
						//if(item instanceof String[]){
						if(renameDialog==null)
							renameDialog=new FileRenameDialog();
						renameDialog.popup(jEdit.getActiveView(),f.getPath());
						if(renameDialog.isOkClicked()){
							MyProject.getInstance().renameFile(getCurrentProjectName(),index,renameDialog.getResult());
						}
						//}
					}catch(Exception ex){
						logger.log(Level.SEVERE,"",ex);
					}
				}
				}
			);
		fileItemMenu.add(menuItem);
	}
	protected JComponent createProjectPanel()
	{
		JPanel p=new JPanel();
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
		p.setOpaque(false);
		JComponent comp=createPrjActionButtons();
		comp.setAlignmentX(Component.LEFT_ALIGNMENT);
		p.add(comp);
		//----------project list box
		projectList=new JComboBox(MyProject.getInstance().getProjectList());
		projectList.setToolTipText("Select a Project from this list");
		String selectedProjectName=MyJeditPlugin.prop.getProperty(MyJeditPlugin.PROP_OPENED_PROJECT);
		if(selectedProjectName!=null){
			projectList.setSelectedItem(selectedProjectName);
		}
		projectList.setEditable(true);
		//comboboxModel.addElement(PRJ_ADD_ITEM);
		projectList.setMaximumSize(new Dimension(3000,20));
		projectList.setMinimumSize(new Dimension(20,10));
		projectList.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					try{
					//log.debug("e.getActionCommand()="+e.getActionCommand());
					if("comboBoxChanged".equals(e.getActionCommand()))
					{
						if(classSearcher!=null)
							classSearcher.cleanup();
						//System.gc();
						fileTable.setModel(MyProject
							.getInstance()
							.getProjectTableModel(
								(String)projectList.getSelectedItem()));
						MyJeditPlugin.prop.setProperty(MyJeditPlugin.PROP_OPENED_PROJECT,(String)projectList.getSelectedItem());
						if(sortButton.isSelected()){
							MyProject.getInstance().sortFileListByDir(getCurrentProjectName());
						}
						System.gc();
					}
					//else if("comboBoxEdited".equals(e.getActionCommand()))
					//{
					//}
					}catch(Exception ex){
						log.debug("",ex);
					}
				}
		});
		JPanel projectPanel=new JPanel();
		projectPanel.setOpaque(false);
		projectPanel.setLayout(new BoxLayout(projectPanel,BoxLayout.X_AXIS));
		projectPanel.add(new JLabel("Project: "));
		projectPanel.add(projectList);
		projectPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		p.add(projectPanel);
		
		//----text field
		indexField=new MyTextField("");
		prepareSearchTimer();
		indexField.getDocument().addDocumentListener(new DocumentListener(){
				public void changedUpdate(DocumentEvent e){
					searchStart();
				}
				public void insertUpdate(DocumentEvent e) {
					searchStart();
				}
				public  void removeUpdate(DocumentEvent e){
					searchStart();
				}
				
				
		});
		indexField.setAlignmentX(Component.LEFT_ALIGNMENT);
		indexField.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
		//indexField.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,);
		indexField.addKeyListener(new KeyAdapter(){
				public void keyReleased(KeyEvent e)
				{
					if(e.getKeyCode()==e.VK_DOWN){
						if(fileTable.getModel().getRowCount()<=0)
							return;
						fileTable.requestFocus();
						fileTable.getSelectionModel().clearSelection();
						fileTable.getSelectionModel().addSelectionInterval(0,0);
					}
				}
		});
		p.add(indexField);
		
		//JList prjFileList=new JList(MyProject.getInstance().getProjectListModel());
		//JScrollPane listScroller = new JScrollPane(prjFileList);
		//listScroller.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		fileTable=new MyFileListTable();
		fileTable.setOpaque(false);
		fileTable.setModel(MyProject.getInstance().getProjectTableModel((String)projectList.getModel().getSelectedItem()));
		//fileTable.getTableHeader().setDefaultRenderer(new MyFileListTableRendar());
		
		/*fileTable.setRowHeight(20);
		fileTable.setRowMargin(0);
		fileTable.enableAutoPack(true);*/
		
		prepareFileItemPopupMenu();
		fileTable.addMouseListener(new MouseAdapter() {
		     public void mouseClicked(MouseEvent e) {
			 if (e.getClickCount() == 2 && e.getButton()==e.BUTTON1) {
				 int row=fileTable.rowAtPoint(e.getPoint());
				 selectProjectItem(row);
			     //MyJeditPlugin.getInstance().activeBuffer(index);
			  }
		     }
		     public void mousePressed(MouseEvent e)
			{
				if (e.isPopupTrigger()){
					  int row=fileTable.getSelectedRow();
					  if(row>=0)
						  fileItemMenu.show(e.getComponent(),e.getX(), e.getY());
				  }
			}
			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger()){
					  int row=fileTable.getSelectedRow();
					  if(row>=0)
						  fileItemMenu.show(e.getComponent(),e.getX(), e.getY());
				  }
			}
		});
		fileTable.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e)
			{
				try{
					if(e.getKeyCode()==e.VK_ENTER){
						selectProjectItem(fileTable.getSelectionModel().getMinSelectionIndex());
						/*MyProject.getInstance().openFileByIndex(
							(String)projectList.getModel().getSelectedItem(),
							fileTable.getSelectionModel().getMinSelectionIndex());*/
					}
					else if(e.getKeyCode()==e.VK_CONTEXT_MENU){
						 int row=fileTable.getSelectedRow();
						 if(row>=0)
							 fileItemMenu.show(e.getComponent(),1, 1);
					}
				}catch(Exception ex){
					log.debug("",ex);
				}
			}
		});
		fileTable.setDragEnabled(true);
		PrjFileDragInTransferHandle pTransferhandle=new PrjFileDragInTransferHandle();
		fileTable.setTransferHandler(pTransferhandle);
		JScrollPane listScroller = new JScrollPane(fileTable);
		listScroller.setTransferHandler(pTransferhandle);
		listScroller.setAlignmentX(Component.LEFT_ALIGNMENT);
		listScroller.setOpaque(false);
		listScroller.getViewport().setOpaque(false);
		//fileTable.getColumnModel();
		p.add(listScroller);
		
		return p;
	}
	protected JComponent createPrjActionButtons()
	{
		JPanel p=new JPanel();
		p.setOpaque(true);
		p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));
		p.setMinimumSize(new Dimension(20,20));
		JMenuItem add=new JMenuItem("Add");
		add.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				try{
					MyProject bean=MyProject.getInstance();
					String prjName=getCurrentProjectName();
					MyProjectOption myoption=bean.getOption(prjName);
					if(myoption.getRootPath()!=null && myoption.getRootPath().length()>0){
						chooseDirDialog.setCurrentDirectory(new File(myoption.getRootPath()));
					}
					chooseDirDialog.setDialogTitle("Select Files or Direcotries");
					chooseDirDialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
					chooseDirDialog.setMultiSelectionEnabled(true);
					int returnVal = chooseDirDialog.showOpenDialog(null);
					if (returnVal == JFileChooser.APPROVE_OPTION)
					{
						//setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						File[] files = chooseDirDialog.getSelectedFiles();
						
						int sum=0;
						String []filter=myoption.getFileFilterReg();
						for(int i=0;i<files.length;i++){
							if(files[i].isFile()){
								sum+=bean.addFiles(new File[]{files[i]},prjName,filter);
							}else{
								sum+=bean.addFiles(new File[]{files[i]},prjName,filter);
							}
						};
						//int sum=bean.addFiles(files,prjName,regexFd.getText());
						JOptionPane.showMessageDialog(null,"Total: "+sum,
								"Completed",JOptionPane.PLAIN_MESSAGE);
					}
				}catch(Exception ex){
					logger.log(Level.SEVERE,"",ex);
				}
			}
		});
		add.setToolTipText("You may also add files by drag and drop them to the list directly.");
		//p.add(add);
		JToolBar toolbar=new JToolBar("Actions");
		prjActionBtn=new JButton("A");
		prjActionBtn.setToolTipText("Project Actions");
		prjActionBtn.addActionListener(this);
		toolbar.add(prjActionBtn);
		p.add(toolbar);
		
		//JPopupMenu bar=new JPopupMenu();		
		actionMenu=new JPopupMenu("Actions");
		//bar.add(menu);
		actionMenu.add(add);
		toolbar.add(actionMenu);
		
		JMenuItem prjOptions=new JMenuItem("Project");
		prjOptions.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				try{
					optionPanel.setValues(MyProject.getInstance().getOption(getCurrentProjectName()));
					optionPanel.showDialog(getCurrentProjectName());
					//logger.fine("popup dialog");
				}catch(Exception ex){
					logger.log(Level.SEVERE,"",ex);
				}
			}
		});
		
		actionMenu.add(prjOptions);
		p.add(toolbar);
		
		sortButton=new JCheckBox("Sort by dir");
		sortButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				try{
					JCheckBox bt=(JCheckBox)e.getSource();
					if(bt.isSelected()){
						MyProject.getInstance().sortFileListByDir(getCurrentProjectName());
					}else{
						MyProject.getInstance().cancelSortByDir(getCurrentProjectName());
					}
					//logger.fine("popup dialog");
				}catch(Exception ex){
					logger.log(Level.SEVERE,"",ex);
				}
			}
		});
		//p.add(sortButton);
		actionMenu.add(sortButton);
		
		toolbar.add(createButtons());
		//----------------------------
		classview=new JToggleButton("S");
		toolbar.add(classview);
		classview.addActionListener(this);
		return p;
	}
	/*protected void popupAddFileDialog()
	{
		View v=jEdit.getActiveView();
		if(addDialog==null)
			addDialog=new ProjectAddFileDialog(v.getBuffer().getDirectory());
		addDialog.setProjectName((String)projectList.getSelectedItem());
		addDialog.setPath(v.getBuffer().getDirectory());
		addDialog.setLocationRelativeTo(v);
		addDialog.setVisible(true);
		
	}*/
	
	protected void selectProjectItem(int row)
	{
		MyProject.getInstance().openFileByIndex(
		 (String)projectList.getModel().getSelectedItem(),row);
	}
	// These JComponent methods provide the appropriate points
	public void addNotify()
	{
		super.addNotify();
		Container c=getParent();
		while(!(c instanceof View)){
			c=c.getParent();
		}
		c.addKeyListener(this);
		parentView=(View)c;
	}
	
	
	public void removeNotify()
	{
		try{
		synchronized(MyJeditPlugin.getInstance().panels){
			MyJeditPlugin.getInstance().panels.remove(this);
			
		}
		}catch(Exception e){
			log.debug("",e);
		}
		parentView.removeKeyListener(this);
		if(classSearcher!=null)
			classSearcher.disposeDialog();
		classSearcher=null;
		//log.debug("removeNotify()");
		super.removeNotify();
	}
	public void keyPressed(KeyEvent e){
		if(e.isActionKey()){
			// TODO:....
		}else{
			
		}
	}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	protected class PrjFileDragInTransferHandle extends DnDFileTransferHandler
	{
		protected boolean processImportFiles(JComponent c,java.util.List files)
		{
			try{
			//if(c==fileTable){
				Object[] objects=files.toArray();
				File[] fs=(File[])objects;
				//logger.info("num:"+objects.length);
				MyProject bean=MyProject.getInstance();
				String []filter=bean.getOption(getCurrentProjectName()).getFileFilterReg();
				int sum=bean.addFiles(fs,getCurrentProjectName(),filter);
				JOptionPane.showMessageDialog(jEdit.getActiveView(),"Total: "+sum,
								"Completed",JOptionPane.PLAIN_MESSAGE);
				
			}catch(Exception ex){
				logger.log(Level.SEVERE,"",ex);
			}
			return true;
		}
	}

	
	protected class MyBufferListTableRender extends javax.swing.table.DefaultTableCellRenderer
	{
		protected JLabel currentItem;
		protected Color origForeColor;
		//protected JLabel normalItem;
		public MyBufferListTableRender(){
			currentItem=new JLabel();
			//Font font=new Font(null,Font.BOLD,14);
			currentItem.setForeground(Color.WHITE);
			currentItem.setOpaque(true);
			//currentItem.setFont(font);
			currentItem.setBackground(Color.GRAY);
			origForeColor=getBackground();
		}
		
		public  Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			if(row==0){
				//setForeground(Color.BLUE);
				if(isSelected){
					currentItem.setBackground(table.getSelectionBackground());
				}
				else{
					currentItem.setBackground(Color.GRAY);
				}
				currentItem.setText((String)value);
				return currentItem;
			}
			else{
				String sValue=(String)value;
				if(sValue.startsWith("*")){
					setBackground(Color.RED);
				}else{
					setBackground(origForeColor);
				}
				return super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
				
			}
		}
	}
	
}
