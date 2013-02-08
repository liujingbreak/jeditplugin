package org.liujing.jeditplugin.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import java.util.*;
import java.io.File;
import java.util.logging.*;
import java.awt.image.BufferedImage;
import org.liujing.jeditplugin.v2.*;
import org.liujing.awttools.ShadowBorder;
import org.liujing.awttools.MyDefaultTable;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.regex.*;
import org.liujing.awttools.classview.*;
import liujing.util.BackgroundWorkController;
import liujing.swing.*;


public class ProjectModulePanel extends JPanel implements FocusListener{
	private static Logger log = Logger.getLogger(ProjectModulePanel.class.getName());
	//MyTextField					rootNameFd;
	JTree						moduleTree;
	DefaultMutableTreeNode		moduleTreeNode;
	MyTextField					nameFd;
	JComboBox					typeSelect;
	MyTextField					directoryFd;
	FileSelectButton 			fileSel;
	JCheckBox					beListedBox;
	JButton                     cachedBtn;
	/** current selected module */
	ProjectModule				currentModule;
	JTable 						includeTable;
	JTable 						excludeTable;
	FileSetTableEditor			includeTableEditor;
	FileSetTableEditor			excludeTableEditor;
	JSplitPane					subModuleSplitPane;
	JSplitPane					patternSplitPane;

	JButton						createSubModuleBtn;
	JButton						deleteSubModuleBtn;
	JButton						saveModuleDetailsBtn;

	JButton						deleteIncludesBtn;
	JButton						deleteExcludesBtn;
	JButton						okBtn;
	JButton						cancelBtn;
	ClassSearchConfigUI			cpUI;
	static String				TAXT_FIELD_CMD = "textField";
	public static String		OK_CMD = "ok";
	public static String		CANCEL_CMD = "cancel";
	PMPEventHandler				eventHandler = new PMPEventHandler();
	Pattern						backSlashPt = Pattern.compile("\\\\");
	private BackgroundWorkController worker;
	private StateCreationTask stateCreationTask = new StateCreationTask();

	public ProjectModulePanel(String defaultName, BackgroundWorkController worker){
	    this.worker = worker;
		setOpaque(false);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel panel = new JPanel();
		//panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		//panel.add( new JLabel("Project Name: "));

		//if(defaultName != null)
		//	rootNameFd = new MyTextField(defaultName);
		//else
		//	rootNameFd = new MyTextField("Untitled");
		//panel.add(rootNameFd);
		//panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
		//add(panel);

		add(Box.createRigidArea(new Dimension(0, 2)));
		moduleTreeNode = new DefaultMutableTreeNode();
		moduleTree = new JTree(moduleTreeNode);
		moduleTree.setExpandsSelectedPaths(true);
		moduleTree.addTreeSelectionListener(eventHandler);

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		createSubModuleBtn = new JButton("Create Sub Module");
		createSubModuleBtn.addActionListener(eventHandler);
		deleteSubModuleBtn = new JButton("Delete Module");
		deleteSubModuleBtn.addActionListener(eventHandler);
		buttonPanel.add(createSubModuleBtn);
		buttonPanel.add(deleteSubModuleBtn);
		buttonPanel.add(Box.createHorizontalGlue());

		panel.add(buttonPanel);

		cpUI = new ClassSearchConfigUI();

		JTabbedPane tabPanel = new JTabbedPane();
		tabPanel.addTab("Module", createModuleDetailPanel());
		tabPanel.addTab("Class Path", cpUI);
		panel.add(tabPanel);

		saveModuleDetailsBtn = new JButton("Save");
		saveModuleDetailsBtn.addActionListener(eventHandler);
		saveModuleDetailsBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panel.add(saveModuleDetailsBtn);

		JPanel panel2 = new JPanel();
		panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));

		JScrollPane treePanel = new JScrollPane( moduleTree);
		treePanel.setMinimumSize(new Dimension(120, 0));
		treePanel.setPreferredSize(new Dimension(120, 100));
		treePanel.setMaximumSize(new Dimension(120, Integer.MAX_VALUE));
		panel2.add(treePanel);

		panel2.add(panel);
		//subModuleSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane( moduleTree), panel);
		//subModuleSplitPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(panel2);
		//----------------------------------------------
		add(Box.createRigidArea(new Dimension(0, 2)));
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		okBtn = new JButton("OK");
		cancelBtn = new JButton("Cancel");
		panel.add(okBtn);
		okBtn.setActionCommand(OK_CMD);
		panel.add(Box.createRigidArea(new Dimension(4, 0)));
		panel.add(cancelBtn);

		cancelBtn.setActionCommand(CANCEL_CMD);
		add(panel);
	}

	protected JPanel createModuleDetailPanel(){
		JPanel modulePanel = new JPanel();
		modulePanel.setOpaque(false);
		modulePanel.setLayout(new BoxLayout(modulePanel, BoxLayout.Y_AXIS));
		//modulePanel.setBorder(new TitledBorder("Module Details"));
		// --------------------------
		JPanel panel = new JPanel();
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		panel.add( new JLabel("Name : "));
		nameFd = new MyTextField("");
		panel.add(nameFd);
		panel.add(Box.createRigidArea(new Dimension(3, 0)));
		typeSelect = new JComboBox(new Object[]{"standard", "zip source"});
		panel.add(typeSelect);
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 23));
		//panel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 23));
		//panel.setMinimumSize(new Dimension(Integer.MAX_VALUE, 23));
		//panel.setBorder(new ShadowBorder(panel));
		modulePanel.add(panel);
		modulePanel.add(Box.createRigidArea(new Dimension(0, 2)));

		// --------------------------
		panel = new JPanel();
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setOpaque(false);

		directoryFd = new MyTextField();
		panel.add(new JLabel("Directory : "));
		panel.add(directoryFd);

		fileSel = new FileSelectButton("Folder");
		fileSel.boundTextField(directoryFd);
		fileSel.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		panel.add(fileSel);

		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 23));
		modulePanel.add(panel);
		modulePanel.add(Box.createRigidArea(new Dimension(0, 2)));
		// --------------------------

		panel = new JPanel();
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setOpaque(false);

		beListedBox = new JCheckBox("Be listed in file tables", true);
		panel.add(beListedBox);

		cachedBtn = new JButton("Cache");
		panel.add(Box.createHorizontalGlue());
		panel.add(cachedBtn);
		cachedBtn.setActionCommand("cache");
		cachedBtn.addActionListener(eventHandler);
		cachedBtn.setEnabled(false);
		modulePanel.add(panel);
		// --------------------------

		includeTable = new AutoSaveEditTable();
		includeTable.addMouseListener(eventHandler);

		JScrollPane includeScroller = new JScrollPane( includeTable );

		excludeTable = new AutoSaveEditTable();
		excludeTable.addMouseListener(eventHandler);
		JScrollPane excludeScroller = new JScrollPane( excludeTable );

		TableCellRenderer tableRenderer = new FileSetTableRenderer();

		includeTable.setDefaultRenderer(Object.class, tableRenderer);
		excludeTable.setDefaultRenderer(Object.class, tableRenderer);

		includeTableEditor = new FileSetTableEditor();
		excludeTableEditor = new FileSetTableEditor();

		includeTable.setDefaultEditor(Object.class, includeTableEditor);
		excludeTable.setDefaultEditor(Object.class, excludeTableEditor);
		includeTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		excludeTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		includeScroller.addFocusListener(this);

		JPanel includesPanel = new JPanel();
		includesPanel.setLayout(new BoxLayout(includesPanel, BoxLayout.Y_AXIS));
		includesPanel.setOpaque(false);


		deleteIncludesBtn = new JButton("Delete");
		includesPanel.add(includeScroller);
		//includesPanel.add(deleteIncludesBtn);

		patternSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, includesPanel, excludeScroller);
		patternSplitPane.setDividerLocation(0.5d);
		patternSplitPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		//splitPane.setBorder(new ShadowBorder(splitPane));
		modulePanel.add( patternSplitPane);
		modulePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		return modulePanel;
	}

	protected void freshModuleDetailPanel(ProjectModule m){
	    cachedBtn.setEnabled(m.getName() != null && m.getName().trim().length() > 0 );
		int type = m.getType();
		if(type == ProjectModule.TYPE_DIR){
			typeSelect.setSelectedIndex(0);
		}else if(type == ProjectModule.TYPE_ZIP){
			typeSelect.setSelectedIndex(1);
		}
		nameFd.setText(m.getName());
		directoryFd.setText(m.getDirectory());
		cpUI.setDefaultDirectory(m.getDirectory());
		beListedBox.setSelected(m.isListed());
		includeTable.setModel(new FileSetTableModel( m.getIncludes(), "Includes"));
		excludeTable.setModel(new FileSetTableModel( m.getExcludes(), "Excludes"));
		ProjectModule parent = m.getParent();
		if(parent != null)
			fileSel.setDefaultPath(parent.getDirFile());
		log.fine("fresh module " + m.getClassPaths() );
		cpUI.setClassPath(m.getClassPaths());
	}

	public void addActionListener(ActionListener l){
		okBtn.addActionListener(l);
		cancelBtn.addActionListener(l);
	}

	@Override
	public void addNotify(){
		super.addNotify();
		SwingUtilities.invokeLater(new Runnable()
			{
				public void run(){
					patternSplitPane.setDividerLocation(0.5d);
				}
		});
	}

	public void setProjectModule( ProjectModule module){
		currentModule = module;
		log.fine(module.toString() + " " + module.hashCode());

		moduleTreeNode.setUserObject(module);
		moduleTreeNode.removeAllChildren();
		fillModuleTreeNode(moduleTreeNode, module);
		moduleTree.requestFocusInWindow();

		//((DefaultTreeCellRenderer)moduleTree.getCellRenderer()).setTextSelectionColor(Color.RED);
		//moduleTree.clearSelection();
		//moduleTree.addSelectionPath(new TreePath(moduleTreeNode));
		((DefaultTreeModel)moduleTree.getModel()).reload();
		moduleTree.getSelectionModel().setSelectionPath(new TreePath(moduleTreeNode));
		for( int i = 0; i< moduleTree.getRowCount(); i++){
			moduleTree.expandRow(i);
		}
	}

	protected void fillModuleTreeNode(DefaultMutableTreeNode node, ProjectModule module ){
		Iterator<ProjectModule> it = module.allSubModule();
		while(it.hasNext()){
			ProjectModule m = it.next();
			if(log.isLoggable(Level.FINE))
				log.fine(m.toString());
			DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(m);
			node.add(subNode);
			fillModuleTreeNode(subNode, m);
		}

	}

	protected boolean validateSaveCurrentModule(){
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)moduleTree.getLastSelectedPathComponent();
		if(node == null){
			return true;
		}
		ProjectModule projectModule = (ProjectModule)node.getUserObject();
		if(log.isLoggable(Level.FINE))
			log.fine("saving module " + projectModule);
		if(nameFd.getText() == null || nameFd.getText().trim().length() == 0 ){
			nameFd.requestFocusInWindow();
			JOptionPane.showMessageDialog(null, "Field can't be empty");
			return false;
		}
		projectModule.setName(nameFd.getText());
		//if(directoryFd.getText() == null || directoryFd.getText().trim().length() == 0 ){
		//	directoryFd.requestFocusInWindow();
		//	JOptionPane.showMessageDialog(null, "Field can't be empty");
		//	return false;
		//}
		if(directoryFd.getText().trim().length()>0)
			projectModule.setDirectory(directoryFd.getText());
		projectModule.setListed(beListedBox.isSelected());
		if(includeTable.isEditing()){
			includeTableEditor.doStopEdit();
		}
		if(excludeTable.isEditing()){
			excludeTableEditor.doStopEdit();
		}
		projectModule.setIncludes( ((FileSetTableModel)includeTable.getModel()).files );
		projectModule.setExcludes( ((FileSetTableModel)excludeTable.getModel()).files );
		((DefaultTreeModel)moduleTree.getModel()).nodeChanged(node);
		log.fine("reload");

		int type = typeSelect.getSelectedIndex();
		projectModule.setType( (type == 0)?ProjectModule.TYPE_DIR:ProjectModule.TYPE_ZIP);
		projectModule.setClassPaths(cpUI.getClassPath());
		cachedBtn.setEnabled(true);
		return true;
	}

	public boolean validateSave(){
		//if(rootNameFd.getText() == null || rootNameFd.getText().trim().length() == 0 ){
		//	rootNameFd.requestFocusInWindow();
		//	return false;
		//}

		return validateSaveCurrentModule();
	}

	public void focusGained(FocusEvent e){

	}

	public void focusLost(FocusEvent e){
		//value = field.getText();
		log.fine(" lost ");
		//JTable table = (JTable) e.getSource();
		//if( table.isEditing() ){
		//	table.removeEditor();
		//}
		//if(dirty){
		//	table.getModel().setValueAt(value, row, column);
		//	dirty = false;
		//	((AbstractTableModel)table.getModel()).fireTableCellUpdated(row, column);
		//}
	    ////fireEditingStopped();
	}

	class AutoSaveEditTable extends FitWidthTable{

		@Override
		public void removeEditor(){
			//log.info(" removeEditor");
			//((FileSetTableEditor)getDefaultEditor(Object.class)).saveDirtyField();
			super.removeEditor();

		}
	}

	class FileSetTableEditor extends AbstractCellEditor implements
	TableCellEditor, ActionListener, FileSelectButton.FileSelectListener
	{
		JCheckBox 			checkBox = new JCheckBox();
		MyTextField			field = new MyTextField();
		FileSelectButton	fileButton = new FileSelectButton(" Select ");
		Object				value;

		JPanel				pathEditorPanel;
		int					row;
		int					column;
		JTable				table;
		boolean				dirty = false;

		public FileSetTableEditor(){
			//field.getDocument().addDocumentListener(this);
			field.setActionCommand(TAXT_FIELD_CMD);
			field.addActionListener(this);

			field.setMinimumSize(new Dimension(150, 20));
			field.setMaximumSize(new Dimension(400, 20));
			//field.setPreferredSize(new Dimension(250, 20));
			fileButton.addFileSelectListener(this);

			//fileButton.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			pathEditorPanel = new JPanel();
			pathEditorPanel.setOpaque(false);
			pathEditorPanel.setLayout(new BoxLayout(pathEditorPanel, BoxLayout.X_AXIS));
			pathEditorPanel.add(field);
			pathEditorPanel.add(fileButton);
		}


		public String getEditingValue(){
			return field.getText();
		}

		public void doStopEdit(){
			//value = field.getText();
			dirty = false;
			fireEditingStopped();
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
			int newWidth = pathEditorPanel.getPreferredSize().width;
			if( newWidth > oldWidth )
				table.getColumnModel().getColumn(column).setPreferredWidth(newWidth);
			//log.info(row + ", " + column + "= " + value
			//	+ " model's value = " + table.getModel().getValueAt(row, column )
			//	+ " " + hashCode());
			return pathEditorPanel;

		}


		public void actionPerformed(ActionEvent evt) {
			if( TAXT_FIELD_CMD.equals(evt.getActionCommand()) ){
				value = field.getText();
				dirty = false;
				fireEditingStopped();
			}
		}
		public void onFileSelected(File[] files, JButton source){
			String path = files[0].getPath();
			String rootdir = directoryFd.getText();
			if( rootdir != null && rootdir.trim().length()>0 && path.startsWith(rootdir)){
				//log.fine( "" + path.charAt(rootdir.length()) + " ? " + File.separatorChar);
				if(path.charAt(rootdir.length()) == File.separatorChar )
					path = path.substring(rootdir.length()+1);
				else
					path = path.substring(rootdir.length());
			}
			path = backSlashPt.matcher(path).replaceAll("/");
			if(files[0].isDirectory()){
				path = path + "/**/*";
			}
			field.setText(path);
			this.value = path;
			dirty = false;
			fireEditingStopped();
		}

		public void onFileSelectButtonClicked(JButton source){
			File df = new File(directoryFd.getText());
			if(df.exists())
				fileButton.setDefaultPath(df);
		}

		public void saveDirtyField(){
			if(dirty){

				try{
				String lastValue = field.getText();
				table.getModel().setValueAt(lastValue, this.row, this.column);
				}catch(Exception e){
					log.log(Level.WARNING, "", e);
				}
			}
		}
	}

	private static class FileSetTableModel extends AbstractTableModel{
		public java.util.List<String> 		files;
		String 						name;
		boolean						editable = false;

		public FileSetTableModel(java.util.List<String> files, String name){
			this.files = new ArrayList(files);
			this.name = name;
		}

		public int getColumnCount()
		{
			return 1;
		}
		public int getRowCount()
		{
			//log.info(""+files.size());
			return files.size() + 1;
		}
		public Object getValueAt(int rowIndex,
                  int columnIndex)
		{
			//log.info("rowIndex= "+ rowIndex + " columnIndex= " + columnIndex
			//	+ ", value=" + (rowIndex <files.size()?files.get(rowIndex):"")
			//	+ ", size=" + files.size());
			if(rowIndex < files.size() ){
				switch(columnIndex){
					//case 0:
					//	return Boolean.TRUE;
					default :
						return files.get(rowIndex);
				}
			}else{
				switch(columnIndex){
					default :
						return "";
				}
			}
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex){
			//log.fine("set "+ rowIndex + ", " + columnIndex + " = " + aValue);
			if(columnIndex == 0){
				if(rowIndex >= files.size()){
					String sValue = (String)aValue;
					if(sValue != null && sValue.trim().length() > 0){
						files.add( sValue );
						int lastrow = rowIndex + 1;
						fireTableRowsInserted(lastrow, lastrow);
					}
				}else{
					String sValue = (String)aValue;
					if(sValue != null && sValue.trim().length() > 0){
						files.set( rowIndex, (String)aValue );
					}else{
						files.remove( rowIndex );
						fireTableRowsDeleted(rowIndex, rowIndex);
					}
				}
			}
		}

		public boolean isCellEditable(int row, int col) {
			return ( (row == files.size())?true: editable );

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



	class FileSetTableRenderer extends DefaultTableCellRenderer{
		//JCheckBox 		checkBox = new JCheckBox();

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){

			Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			int oldW = table.getColumnModel().getColumn(column).getPreferredWidth();
			int w = comp.getPreferredSize().width;

			if(table.isCellEditable(row, column)){
	        	TableCellEditor editor = table.getCellEditor(row, column);
	        	// remove below code, calling 'prepareEditor' will cause issue while the table is in Editing state.

	        	//Component editcomp = table.prepareEditor(editor, row, column);
	        	//if(editcomp.getPreferredSize().width > comp.getPreferredSize().width)
	        	//	w = editcomp.getPreferredSize().width + 4;

	        }
	        if(w > oldW){
	        	log.fine("");
	        	table.getColumnModel().getColumn(column).setPreferredWidth(w);
	        }
	        return comp;
		}
	}

	class PMPEventHandler extends MouseAdapter implements ActionListener, TreeSelectionListener{

		public void mouseClicked(MouseEvent e){
			if(e.getSource().equals(includeTable)){
				if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2){
					FileSetTableModel model = (FileSetTableModel) includeTable.getModel();
					model.setEditable(true);
					int row = includeTable.rowAtPoint(e.getPoint());
					includeTable.editCellAt(row, 0);
					model.setEditable(false);
				}
			}else if(e.getSource().equals(excludeTable)){
				if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2){
					FileSetTableModel model = (FileSetTableModel) excludeTable.getModel();
					model.setEditable(true);
					int row = excludeTable.rowAtPoint(e.getPoint());
					excludeTable.editCellAt(row, 0);
					model.setEditable(false);
				}
			}

		}

		public void actionPerformed(ActionEvent e){
			try{
				if(e.getSource().equals(saveModuleDetailsBtn)){
					validateSaveCurrentModule();

				}else if(e.getSource().equals(createSubModuleBtn)){
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)moduleTree.getLastSelectedPathComponent();
					Object[] path = moduleTree.getSelectionPath().getPath();

					if(node == null){
						JOptionPane.showMessageDialog(null, "Please select a module first");
						return ;
					}
					ProjectModule selectedModule = (ProjectModule)node.getUserObject();
					ProjectModule subM = new ProjectModule();
					selectedModule.addSubModule(subM);
					DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(subM);
					node.add(newNode);
					((DefaultTreeModel)moduleTree.getModel()).reload(node);
					Object[] newPath = new Object[path.length + 1];
					System.arraycopy(path, 0, newPath, 0, path.length);
					newPath[path.length] = newNode;
					moduleTree.setSelectionPath(new TreePath(newPath));
				}else if(e.getSource().equals(deleteSubModuleBtn)){
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)moduleTree.getLastSelectedPathComponent();
					if(node == null){
						JOptionPane.showMessageDialog(null, "Please select a module first");
						return ;
					}
					if( JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "Are you sure to remove this module?",
						"Confirm",  JOptionPane.YES_NO_OPTION))
					{
						ProjectModule selectedModule = (ProjectModule)node.getUserObject();
						if(selectedModule.getParent() == null){
							JOptionPane.showMessageDialog(null, "You can't remove root module");
							return ;
						}
						log.fine("Remove sub module from " + selectedModule.getParent().hashCode());
						selectedModule.getParent().removeSubModule(selectedModule);
						//node.removeFromParent();
						((DefaultTreeModel)moduleTree.getModel()).removeNodeFromParent(node);
					}
				}else if("cache".equals(e.getActionCommand())){
				    setCursor(new Cursor(Cursor.WAIT_CURSOR));
				    worker.addTask(stateCreationTask);
				    //StateCacheManagement.getInstance().createState(projectModule);
				}
			}catch(Exception ex){
				log.log(Level.SEVERE, "", ex);
			}
		}

		public void valueChanged(TreeSelectionEvent e){
			try{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)moduleTree.getLastSelectedPathComponent();
				if(log.isLoggable(Level.FINE))
						log.fine("select changed: "+ node);
				//TreePath path = e.getNewLeadSelectionPath();
				if(node != null){
					//DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
					ProjectModule selectedModule = (ProjectModule)node.getUserObject();
					currentModule = selectedModule;
					if(log.isLoggable(Level.FINE))
						log.fine("select module: "+ selectedModule);
					freshModuleDetailPanel(selectedModule);
				}
			}catch(Exception ex){
				log.log(Level.SEVERE, "", ex);
			}
		}
	}

	private class StateCreationTask implements BackgroundWorkController.Task<Void>{
	    private StateCacheManagement cacheMgr = new StateCacheManagement(null);

	    public Void execute() throws Exception{
	        cacheMgr.createState(currentModule);
	        return null;
	    }
		public void onTaskDone(Void result){
		    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		public void onTaskFail(Exception thr){
		    setCursor(new Cursor(Cursor.DEFAULT_CURSOR  ));
		    log.log(Level.WARNING, "Failed to create state", thr);
		}
	}

	private void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException
    {
        log.fine("reading module panel !!!!!!!!!!!!!");
    }
}
