package org.liujing.jeditplugin.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import java.util.*;
import java.io.*;
import java.util.logging.*;
import java.awt.image.BufferedImage;
import org.liujing.jeditplugin.v2.*;
import java.awt.event.*;
import javax.swing.event.*;
import org.liujing.awttools.classview.*;
import java.util.concurrent.Executors;
import liujing.util.BackgroundWorkController;
import liujing.swing.*;

public class ProjectPanel extends JPanel{
	private static Logger log = Logger.getLogger(ProjectPanel.class.getName());

	private JButton 				dropDownBtn;
	private JComboBox 				projectListBox;
	private JTextField 				searchText;
	private JButton 				searchRootBox;
	private JButton                 clearFilterBtn;
	public  BufferedImage 			dropdownImg 			= null;
	public  BufferedImage 			clearFilterImg 			= null;
	private JTable 					fileTable;
	private JTree					fileTree;
	Frame							frameOwner;
	JTable 							bufferList;
	JPopupMenu						dropDownMenu;
	JMenuItem 						closeBufMi;
	JMenuItem 						newProjectMi;
	JMenuItem 						editProjectMi;
	JMenuItem 						deleteProjectMi;
	JMenuItem 						switchMi;
	JMenuItem 						classSearchMi;

	JPanel							filePanel;
	CardLayout						cardLayout;

	DefaultMutableTreeNode			fileTreeNode;
	transient DefaultMutableTreeNode			fileTreeNodeSave; //backup during searching

	ProjectPanelHandler				eventHandler 			= new ProjectPanelHandler();
	JDialog							projDialog;
	ProjectModulePanel				projectModulePanel;
	protected javax.swing.Timer 	timer;
	static int 						SEARCH_TIMER_DELAY		= 777;
	static String					SEARCH_TIMER_CMD 		= "search";

	static int						TABLE_CARD				= 1;
	static int						TREE_CARD				= 0;
	int								switchCard				= 0;
	boolean							treeLoaded				= false;
	boolean							tableLoaded				= false;
	ProjectController 				prjControl;
	BufferController				buffControl;

	ProjectModule					currentPrjModule;
	ClassSearchUI					classSearchUI;
	Javaprint						classSearchEngine;
	JDialog							clsDiaolog;

	private static String			msg_tree_view = "Folder Tree View";
	private static String			msg_table_view = "File Table View";

	static Comparator<String>		stringComparator = new NodeMapCompa();
	private boolean autoExpandEnabled = true;
	private boolean searchStarted = false;

	private byte status = 0;
	/** in search mode */
	private static final byte STATUS_SEARCHED = 1;

	private ProjectModule selectedModule;
	private String selectedPath;
	private String selectedName;
	private StatusPopup statusPopup = new StatusPopup();
	private JPopupMenu treeContextMenu;
	private JMenuItem treeMenuItem_scan;
	private StateCacheManagement stateMgr;
	protected Font defaultFont = new Font("Ebrima", Font.PLAIN , 14);

	private BackgroundWorkController worker = new BackgroundWorkController(true);

	public ProjectPanel(ProjectController pc, BufferController bc){
		prjControl = pc;
		buffControl = bc;
		try{
			dropdownImg = javax.imageio.ImageIO.read(
			ProjectPanel.class.getResourceAsStream("dropDown.png"));
			clearFilterImg = javax.imageio.ImageIO.read(
			ProjectPanel.class.getResourceAsStream("clear.png"));
		}catch(java.io.IOException ioe){
				log.log(Level.SEVERE,"failed to load image dropDown.png",ioe);
		}

		setOpaque(false);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		classSearchUI = new ClassSearchUI();
		classSearchEngine = pc.getClassSearchEngine();

		JPanel controlPanel = new JPanel();
		controlPanel.setOpaque(false);
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
		controlPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
		add(controlPanel);
		add(Box.createRigidArea(new Dimension(0,2)));


		dropDownBtn = new JButton(new ImageIcon(dropdownImg));
		dropDownBtn.addActionListener(eventHandler);
		//log.info("min size "+ dropDownBtn.getMinimumSize());
		Dimension minSize = new Dimension(dropdownImg.getWidth() + 6, dropDownBtn.getMinimumSize().height);
		dropDownBtn.setPreferredSize(minSize);
		dropDownBtn.setMaximumSize(minSize);
		dropDownBtn.addActionListener(eventHandler);
		controlPanel.add(dropDownBtn);

		projectListBox = new JComboBox(new DefaultComboBoxModel());
		projectListBox.setEditable(true);
		projectListBox.addActionListener(eventHandler);
		controlPanel.add(projectListBox);


		searchText = new MyTextField();
		searchText.addKeyListener(eventHandler);
		searchText.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
		searchText.getDocument().addDocumentListener(eventHandler);
		searchText.addFocusListener(eventHandler);

		searchRootBox = new JButton("Srch Opt");
		Font font = searchRootBox.getFont();
		searchRootBox.setFont(new Font(font.getName(), font.getStyle(), 7));
		searchRootBox.setPreferredSize(new Dimension(50, 22));

		clearFilterBtn = new JButton(new ImageIcon(clearFilterImg));
		clearFilterBtn.setActionCommand("clf");
		clearFilterBtn.addActionListener(eventHandler);
		Dimension clearFDim = new Dimension(clearFilterImg.getWidth() + 6,
		    clearFilterBtn.getMinimumSize().height);
		clearFilterBtn.setPreferredSize(clearFDim);
		clearFilterBtn.setMaximumSize(clearFDim);

		JPanel searchPanel = new JPanel();
		searchPanel.setOpaque(false);
		searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
		searchPanel.add(searchRootBox);
		searchPanel.add(searchText);
		searchPanel.add(clearFilterBtn);

		add(searchPanel);
		add(Box.createRigidArea(new Dimension(0,2)));

		filePanel = new JPanel();
		cardLayout = new CardLayout();
		filePanel.setLayout(cardLayout);
		filePanel.setOpaque(false);
		add(filePanel);

		fileTable = new FitWidthTable();
		fileTable.setFont(defaultFont);
		fileTable.setDefaultRenderer(Object.class, new ProjFileTableRenderer());
		fileTable.setOpaque(false);
		fileTable.setModel(new ProjFilesTableModel(new ArrayList()));
		fileTable.addMouseListener(eventHandler);
		fileTable.addMouseMotionListener(eventHandler);

		fileTable.setTransferHandler(new FileDragHandler());
		JScrollPane listScroller = new JScrollPane(fileTable);
		//listScroller.setAlignmentX(Component.LEFT_ALIGNMENT);
		listScroller.setOpaque(false);
		listScroller.getViewport().setOpaque(false);
		filePanel.add(listScroller, "table");


		fileTreeNode = new DefaultMutableTreeNode("loading...");
		fileTree = new JTree(fileTreeNode);
		fileTree.setCellRenderer(new MyTreeCellRender());
		fileTree.setOpaque(false);
		fileTree.setFont(defaultFont);
		fileTree.addTreeWillExpandListener(eventHandler);
		fileTree.addTreeExpansionListener(eventHandler);
		fileTree.addTreeSelectionListener(eventHandler);
		fileTree.addMouseListener(eventHandler);
		JScrollPane treeScroller = new JScrollPane(fileTree);
		treeScroller.getViewport().setOpaque(false);
		treeScroller.setOpaque(false);
		filePanel.add(treeScroller, "tree");

		switchCard = prjControl.getPreferredView();
		cardLayout.show(filePanel, switchCard == TREE_CARD ? "tree": "table");
		setupMenu();
		freshProjectList();

		classSearchUI.setBgWorker(Executors.newSingleThreadExecutor());
		classSearchUI.setClassScanner(classSearchEngine);
		timer = new javax.swing.Timer(SEARCH_TIMER_DELAY, eventHandler);
		timer.setActionCommand(SEARCH_TIMER_CMD);

		treeContextMenu = new JPopupMenu();
		treeMenuItem_scan = treeContextMenu.add("Scan this folder");
		treeMenuItem_scan.setEnabled(false);
		treeMenuItem_scan.setActionCommand("scanFolder");
		treeMenuItem_scan.addActionListener(eventHandler);
		treeContextMenu.add(treeMenuItem_scan);
		stateMgr = new StateCacheManagement(frameOwner);
	}

	protected void setupMenu(){
		dropDownMenu = new JPopupMenu();

		closeBufMi = new JMenuItem("Close Buffer");
		dropDownMenu.add(closeBufMi);
		closeBufMi.addActionListener(eventHandler);

		newProjectMi = new JMenuItem("New Project");
		dropDownMenu.add(newProjectMi);
		newProjectMi.addActionListener(eventHandler);

		editProjectMi = new JMenuItem("Project Properties");
		dropDownMenu.add(editProjectMi);
		editProjectMi.addActionListener(eventHandler);

		deleteProjectMi = new JMenuItem("Delete Current Project");
		deleteProjectMi.addActionListener(eventHandler);
		dropDownMenu.add(deleteProjectMi);

		switchMi = new JMenuItem("Switch View");
		switchMi.setActionCommand("switchView");
		switchMi.addActionListener(eventHandler);
		dropDownMenu.add(switchMi);

		classSearchMi = new JMenuItem("Class Search");
		classSearchMi.setActionCommand("class");
		classSearchMi.addActionListener(eventHandler);
		dropDownMenu.add(classSearchMi);
	}

	protected void setupProjectDialog(){
		if( projectModulePanel != null )
			return;
		projectModulePanel = new ProjectModulePanel(null, worker);
		projectModulePanel.setBorder(new EmptyBorder(1, 3, 3, 3));
		projectModulePanel.addActionListener(eventHandler);

		projDialog = new JDialog(frameOwner, "New Project", false);
		projDialog.getContentPane().add( projectModulePanel );
		log.fine("new project module panel ="+ projectModulePanel);
	}

	private Frame findFrameOwner(){
		Component comp = this;
		while(! (comp instanceof Frame)){
			comp = comp.getParent();
		}
		return (Frame)comp;
	}

	protected void fillFileTreeNode(DefaultMutableTreeNode node, Object nodeObject){
		//SortedSet<ProjectController.FileItem> fileSet =
		if(nodeObject instanceof ProjectModule){
			ProjectModule pm = (ProjectModule)nodeObject;
			Iterator<ProjectModule> modules = pm.allSubModule();
			while(modules.hasNext()){
				ProjectModule subM = modules.next();
				DefaultMutableTreeNode unloadedNode = new DefaultMutableTreeNode(subM);
				unloadedNode.add(new DefaultMutableTreeNode(""));
				node.add(unloadedNode);
			}
			fillFileTreeNodeByFolder(node, pm, null);
		}else if(nodeObject instanceof ProjectController.FolderItem){
			ProjectController.FolderItem folder = (ProjectController.FolderItem)nodeObject;
			fillFileTreeNodeByFolder(node, folder.module, folder);
		}
	}

	protected boolean fillFileTreeNodeByFolder(DefaultMutableTreeNode node, ProjectModule module,
		ProjectController.FolderItem folder)
	{
		//log.info("fill node " + folder);
		Iterator<ProjectController.FileItem> items = null;
		if( folder != null)
			items = prjControl.travelFileFolder(module, folder.longPath()).iterator();
		else{
			SortedSet<ProjectController.FileItem> set = prjControl.travelFileFolder(module, null);
			items = set.iterator();
		}
		boolean hasChild = false;
		while( items.hasNext()){
			ProjectController.FileItem item = items.next();

			if(item instanceof ProjectController.FolderItem){
				ProjectController.FolderItem folderItem = (ProjectController.FolderItem)item;
				//log.info("if isFolderEmpty? " + folderItem.longPath());
				if(!isFolderEmpty(folderItem.module, folderItem)){
					hasChild = true;
					DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(item);
					node.add(subNode);
					subNode.add(new DefaultMutableTreeNode(""));
				}
				//log.info("end if isFolderEmpty? " + folderItem.longPath());
			}else{
				hasChild = true;
				//log.info("add node " + item);
				DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(item);
				node.add(subNode);
			}
		}
		return hasChild;
	}

	protected boolean isFolderEmpty(ProjectModule pm,
		ProjectController.FolderItem folder)
	{
		//log.info("isFolderEmpty " + folder.longPath());
		Iterator<ProjectController.FileItem> items = prjControl.travelFileFolder(pm, folder.longPath()).iterator();
		while( items.hasNext()){
			ProjectController.FileItem item = items.next();
			if(item instanceof ProjectController.FolderItem){
				if(!isFolderEmpty(pm, (ProjectController.FolderItem)item))
					return false;
			}else{
				return false;
			}
		}
		return true;
	}

	public void setBufferTable(JTable bufferTable){
		this.bufferList = bufferTable;
	}

	@Override
	public void addNotify(){
		log.fine("addNotify");
		super.addNotify();
		frameOwner = findFrameOwner();
		setupProjectDialog();
		SwingUtilities.invokeLater(new Runnable()
			{
				public void run(){
					try{
						String item = (String)projectListBox.getSelectedItem();
						if(item != null){
							//prjControl.openProject(item);
							log.fine("+++++++++"+ item);
							prjControl.openProject(item);
							refreshProjectFiles();
						}
					}catch(Exception ex){
						log.log(Level.SEVERE, "", ex);
					}
				}
		});
	}

	@Override
	public void removeNotify(){
		log.fine("removeNotify");
		super.removeNotify();
	}

	protected void freshProjectList(){
		currentPrjModule = prjControl.getCurrentProject();
		//DefaultComboBoxModel model = (DefaultComboBoxModel)projectListBox.getModel();
		//model.removeAllElements();
		java.util.List<String> projects = prjControl.listProjects();
		//Iterator<String> it = projects.iterator();
		//while(it.hasNext()){
		//	String prjName = it.next();
		//	model.addElement(prjName);
		//}
		projectListBox.setModel(new DefaultComboBoxModel(projects.toArray()));

		if(currentPrjModule != null){
			projectListBox.setSelectedItem(currentPrjModule.getName());
		}
	}

	protected void refreshProjectFiles(){
		searchText.setText("");
		clearProjectFilesView();
		//long curr = System.currentTimeMillis();
		if(TABLE_CARD == switchCard){
			refreshProjectTable();
		}else{
			refreshProjectTree();
		}
		classSearchEngine.setClasspath(prjControl.classpathString());
		prjControl.clearSearch();
		//log.info("refresh time "+ (System.currentTimeMillis() - curr));
	}

	protected void clearProjectFilesView(){
		fileTreeNodeSave = null; // clear old save
	}

	protected void refreshProjectTable(){
		tableLoaded = true;
		SortedSet<ProjectController.FileItem> set = prjControl.listCurrentPrjFiles();
		if(set != null){
			ProjFilesTableModel tableModel = (ProjFilesTableModel)fileTable.getModel();
			tableModel.refresh(set);
		}
		switchMi.setText(msg_tree_view);
	}

	protected void refreshProjectTree(){
	    //long curr = System.currentTimeMillis();
		treeLoaded = true;
		//fileTreeNodeSave = null; // clear old save
		currentPrjModule = prjControl.getCurrentProject();

		resetProjectTreeNode(fileTreeNode, currentPrjModule);
		switchMi.setText(msg_table_view);
		fileTreeNodeSave = fileTreeNode;
		//log.info("tree time "+ (System.currentTimeMillis() - curr));
	}

	protected void resetProjectTreeNode(DefaultMutableTreeNode node, Object userObject){
	    if(userObject == null){
			node.setUserObject("N/A");
			return;
		}
		node.setUserObject(userObject);
		node.removeAllChildren();

		DefaultTreeModel treeModel = (DefaultTreeModel)fileTree.getModel();
		treeModel.nodeChanged(node);

		fillFileTreeNode(node, userObject);
		treeModel.nodeStructureChanged(node);
	}

	protected void openProjectItem(int row)
	{
		ProjFilesTableModel model = (ProjFilesTableModel)fileTable.getModel();
		prjControl.openFile(model.files.get(row));
		//MyProject.getInstance().openFileByIndex(
		// (String)projectList.getModel().getSelectedItem(),row);
	}

	protected void searchStart(){
		if(timer.isRunning())
			timer.stop();
		timer.setDelay(SEARCH_TIMER_DELAY);
		timer.setRepeats(false);
		timer.start();
	}



	private void setSelectedItem(ProjectModule module, String parentPath, String name){
	    searchRootBox.setText(module.toString());
	    selectedModule = module;
	    selectedPath = parentPath;
	    selectedName = name;
	    prjControl.setSelectedModule(module);
	}

	private static class _NodeMap{
		public Map map = new TreeMap(ProjectPanel.stringComparator);
		private int type = 1;
		public ProjectModule module = null;
		public ProjectController.FolderItem folder;
		/**
		default constructor is for folder type
		*/
		public _NodeMap(ProjectModule pm){
			this.module = pm;
		}

		public _NodeMap(ProjectController.FolderItem f){
			folder = f;
		}

		public boolean isModule(){
			return module != null;
		}

		public boolean isFolder(){
			return folder != null;
		}

		public Object getNodeObject(){
			return module == null? folder: module;
		}

		public void put(Object k, Object v){
			map.put(k, v);
		}

		public Object get(Object k){
			return map.get(k);
		}
	}

	public static class NodeMapCompa implements Comparator, Serializable{
		public int compare(Object o1, Object o2){
			if((o1 instanceof ProjectModule) && (o2 instanceof ProjectModule) ){
				ProjectModule m1 = (ProjectModule)o1;
				ProjectModule m2 = (ProjectModule)o2;
				return StringComp.compareString(m1.getName(), m2.getName());
			}else if(o1 instanceof ProjectModule){
				return -1;
			}else if(o2 instanceof ProjectModule){
				return 1;
			}else{
				return StringComp.compareString((String)o1, (String)o2);
			}
		}
	}

	protected void buildTreeForSearchResult(DefaultMutableTreeNode root, SortedSet<ProjectController.FileItem> set){
		_NodeMap dirMap = new _NodeMap((ProjectModule)root.getUserObject());
		Iterator<ProjectController.FileItem> it = set.iterator();
		LinkedList<ProjectModule> modulePath = new LinkedList();
		while(it.hasNext()){
			ProjectController.FileItem item = it.next();
			_NodeMap tempMap = dirMap;
			modulePath.clear();
			ProjectModule m = item.module;
			while(m != null){
				modulePath.addFirst(m);
				m = m.getParent();
			}

			modulePath.removeFirst();
			Iterator<ProjectModule> moduleIt = modulePath.iterator();
			while(moduleIt.hasNext()){
				ProjectModule theModule = moduleIt.next();
				if(!tempMap.map.containsKey(theModule)){
					_NodeMap subMap = new _NodeMap(theModule);
					tempMap.put(theModule, subMap);
					tempMap = subMap;
				}else{
					tempMap = (_NodeMap)tempMap.get(theModule);
				}
			}
			String path = item.path;
			if(log.isLoggable(Level.FINE)) log.fine("folder path " + path);
			String[] paths = path.split("[/|\\\\]");
			for(String s: paths){
				if(!tempMap.map.containsKey(s)){
					_NodeMap subMap = new _NodeMap(new ProjectController.FolderItem(s, "path is not set", item.module));
					tempMap.put(s, subMap);
					tempMap = subMap;
				}else{
					tempMap = (_NodeMap)tempMap.get(s);
				}
			}
			tempMap.put(item.name, item);
		}
		fillTreeNodeByDirMap(root, dirMap);
	}

	protected void fillTreeNodeByDirMap(DefaultMutableTreeNode node, _NodeMap dirMap){
		Iterator<Map.Entry> it = dirMap.map.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry en = it.next();
			Object v = en.getValue();
			if(v instanceof _NodeMap){
				_NodeMap map = (_NodeMap)v;
				DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(map.getNodeObject());
				node.insert(subNode, node.getChildCount());
				fillTreeNodeByDirMap(subNode, map);
			}else{
				node.insert(new DefaultMutableTreeNode(v), node.getChildCount());
			}
		}
	}

	class FileDragHandler extends TransferHandler{
		public FileDragHandler(){}

	}


	class ProjectPanelHandler extends MouseAdapter implements ActionListener,
	DocumentListener, KeyListener, TreeWillExpandListener, TreeExpansionListener,
	TreeSelectionListener, FocusListener
	{
		boolean fileTableDragging 	= false;


		public void actionPerformed(ActionEvent e){
			try{
				if(e.getSource() == dropDownBtn){
					dropDownMenu.show(dropDownBtn, 2, dropDownBtn.getHeight());
				}else if(e.getSource() == closeBufMi){
				 if(bufferList != null)
					 buffControl.closeBuffer(bufferList.getSelectedRows());
				}else if(e.getSource().equals(newProjectMi)){
					currentPrjModule = new ProjectModule();
					currentPrjModule.setName("Untitled");
					log.fine("when new project, project module panel ="+ projectModulePanel);
					projectModulePanel.setProjectModule( currentPrjModule );
					projDialog.setTitle("New Project");
					projDialog.setSize(new Dimension(500, 400));
					projDialog.setLocationRelativeTo(null);
					projDialog.setVisible(true);
				}else if( e.getSource().equals(editProjectMi) ){
					currentPrjModule = prjControl.getCurrentProject();
					if(prjControl == null){
						currentPrjModule = new ProjectModule();
						currentPrjModule.setName("Untitled");
					}
					projectModulePanel.setProjectModule( currentPrjModule );
					projDialog.setTitle(currentPrjModule.getName());
					projDialog.setSize(new Dimension(500, 400));
					projDialog.setLocationRelativeTo(frameOwner);
					projDialog.setVisible(true);
				}else if( e.getSource().equals(deleteProjectMi)){
					if( JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "Are you sure to remove this project?",
						"Confirm",  JOptionPane.YES_NO_OPTION))
					{
						prjControl.deleteProject(prjControl.getCurrentProject());
						freshProjectList();
						refreshProjectFiles();
					}
				}else if( e.getSource().equals(projectListBox)){
					String name = (String)projectListBox.getSelectedItem();
					if(name != null){
					    //long curr = System.currentTimeMillis();
						if(prjControl.openProject(name) == null){
						    prjControl.deleteProject(name);
						    freshProjectList();
						}
						refreshProjectFiles();
						//log.info("switch time "+ (System.currentTimeMillis() - curr));
					}
				}else if( ProjectModulePanel.OK_CMD.equals(e.getActionCommand()) ){
					if(projectModulePanel.validateSave()){
						projDialog.setVisible(false);
						prjControl.saveProject(currentPrjModule);
						prjControl.setCurrentProject(currentPrjModule);
						freshProjectList();
						//freshProjectFiles();
					}
				}else if( ProjectModulePanel.CANCEL_CMD.equals(e.getActionCommand()) ){
					projDialog.setVisible(false);
				}else if( SEARCH_TIMER_CMD.equals(e.getActionCommand()) ){
					if(switchCard == TABLE_CARD){
						SortedSet<ProjectController.FileItem> set = prjControl.searchCurrentPrjFiles( searchText.getText() );
						if(set != null){
							ProjFilesTableModel tableModel = (ProjFilesTableModel)fileTable.getModel();
							tableModel.refresh(set);
						}
						SwingUtilities.invokeLater(new Runnable()
						{
							public void run(){
								fileTable.getSelectionModel().setSelectionInterval(0,0);
							}
						});
					}else{
						if(searchText.getText().trim().length() >0){



							BackgroundWorkController.Task<SortedSet<ProjectController.FileItem>> task
							    = new BackgroundWorkController.Task<SortedSet<ProjectController.FileItem>>()
							    {

                                    public SortedSet<ProjectController.FileItem> execute() throws Exception{
                                        SwingUtilities.invokeLater(new Runnable(){
                                                public void run(){
                                                    statusPopup.show(ProjectPanel.this, 10, 80);
                                                }
                                        });
                                      SortedSet<ProjectController.FileItem> set = prjControl.searchFile( searchText.getText(), null );
                                      if(set == null)
                                        return null;
                                      return set;
                                    }

                                    public void onTaskDone(SortedSet<ProjectController.FileItem> set){
                                        status |= STATUS_SEARCHED;
                                        fileTreeNode = new DefaultMutableTreeNode(fileTreeNodeSave.getUserObject());
                                        buildTreeForSearchResult(fileTreeNode, set);
                                        DefaultTreeModel treeModel = (DefaultTreeModel)fileTree.getModel();
                                        treeModel.setRoot(fileTreeNode);

                                        boolean oldAuto = autoExpandEnabled;
                                        searchStarted = true;
                                        for( int i = 0; i< fileTree.getRowCount(); i++){
                                            fileTree.expandRow(i);
                                        }
                                        searchStarted = false;
                                        autoExpandEnabled = oldAuto;
                                        statusPopup.hide();
                                    }

                                    public void onTaskFail(Exception thr){
                                        log.log(Level.SEVERE, "", thr);
                                        statusPopup.hide();
                                    }
                             };
                             worker.addTask(task);
						}else{
							fileTreeNode = fileTreeNodeSave;
							DefaultTreeModel treeModel = (DefaultTreeModel)fileTree.getModel();
							treeModel.setRoot(fileTreeNode);
							prjControl.clearSearch();
							statusPopup.hide();
							status &= (~STATUS_SEARCHED);
						}
					}

				}else if( "switchView".equals(e.getActionCommand())){
					String cardName = null;
					if(TREE_CARD == switchCard){
						switchCard = TABLE_CARD;
						if(!tableLoaded)
							refreshProjectTable();
						switchMi.setText(msg_tree_view);
						cardName = "table";
					}else{
						switchCard = TREE_CARD;
						if(!treeLoaded)
							refreshProjectTree();
						switchMi.setText(msg_table_view);
						cardName = "tree";
					}

					cardLayout.show(filePanel, cardName);
					prjControl.setPreferredView(switchCard);
				}else if( "class".equals(e.getActionCommand())){
					classSearchUI.getSimpleDialog(frameOwner).setVisible(true);
				}else if("scanFolder".equals(e.getActionCommand())){
				    String path = null;
				    if(selectedPath != null && selectedName != null){
				        path = selectedPath + "/"+ selectedName;
				    }else{
				        path = "";
				    }
				    log.fine("to scan "+ selectedPath + "/"+ selectedName);
				    stateMgr.updateState(selectedModule, path);
				    DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode)fileTree.getLastSelectedPathComponent();
				    resetProjectTreeNode(targetNode, targetNode.getUserObject());
				}else if("clf".equals(e.getActionCommand())){
				    searchText.setText("");
				}
				else{
					log.fine(e.getActionCommand());
				}
		 	}catch(Throwable ex){
		 		log.log(Level.SEVERE, "", ex);
		 	}
		 }

		 public void mouseClicked(MouseEvent e){
		     try{
			if(e.getComponent().equals(fileTable)){
				if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2){
					int row = fileTable.rowAtPoint(e.getPoint());
					openProjectItem(row);
				}
			}else if(e.getComponent().equals(fileTree)){
				if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2){
					int selRow = fileTree.getRowForLocation(e.getX(), e.getY());
					TreePath selPath = fileTree.getPathForLocation(e.getX(), e.getY());
					if(selRow != -1) {
						DefaultMutableTreeNode node = (DefaultMutableTreeNode)selPath.getLastPathComponent();
						if(! (node.getUserObject() instanceof ProjectController.FileItem)){
						    return;
						}
						ProjectController.FileItem item = (ProjectController.FileItem)node.getUserObject();
						if(item instanceof ProjectController.FolderItem)
							return;
						prjControl.openFile(item);
					}
				}else if(e.getButton() == MouseEvent.BUTTON3){
				    //popup context menu
				    TreePath treePath = fileTree.getPathForLocation(e.getX(), e.getY());
				    DefaultMutableTreeNode node =
				    (DefaultMutableTreeNode)treePath.getLastPathComponent();
				    if(node.getUserObject() instanceof ProjectModule){
						    fileTree.setSelectionPath(treePath);
						    treeMenuItem_scan.setEnabled(
						        ((ProjectModule)node.getUserObject()).isStateCacheEnabled());
					}else if(node.getUserObject() instanceof ProjectController.FileItem){
                        ProjectController.FileItem item = (ProjectController.FileItem)node.getUserObject();
                        fileTree.setSelectionPath(treePath);
                        if(item instanceof ProjectController.FolderItem &&
                            item.module.isStateCacheEnabled())
                        {
                            treeMenuItem_scan.setEnabled(true);
                        }else{
                            treeMenuItem_scan.setEnabled(false);
                        }
                    }
					treeContextMenu.show(e.getComponent(),e.getX(), e.getY());
				}
			}
			 }catch(Exception ex){
			     log.log(Level.WARNING, "", ex);
			 }
		 }

		 public void mouseReleased(MouseEvent e ){
		 	 if(e.getSource().equals(fileTable)){
		 	 	 fileTableDragging = false;
		 	 }
		 }

		 public void mouseDragged(MouseEvent e){
		 	 if(fileTableDragging)
		 	 	 return;
		 	 fileTableDragging = true;
		 	 JComponent c = (JComponent)e.getSource();
		 	 TransferHandler handler = c.getTransferHandler();
		 	 handler.exportAsDrag(c, e, TransferHandler.COPY);
		 	 log.fine("drap start");
		 }

		 public void mouseMoved(MouseEvent e){}

		 public void keyPressed(KeyEvent e){
		 	 if(e.getSource().equals(searchText) && e.getKeyCode()==e.VK_ENTER){
		 	 	 log.fine("open file by press 'enter'");
		 	 	 //int row = fileTable.getSelectedRow();
		 	 	 openProjectItem(0);
		 	 	 searchText.setText("");
		 	 }
		 }
		 public void keyReleased(KeyEvent e) {}
		 public void keyTyped(KeyEvent e) {}

		 public void changedUpdate(DocumentEvent e){
			searchStart();
		}
		public void insertUpdate(DocumentEvent e) {
			searchStart();
		}
		public  void removeUpdate(DocumentEvent e){
			searchStart();
		}

		public void treeWillCollapse(TreeExpansionEvent event)throws ExpandVetoException{
			//throw new ExpandVetoException(event);
		}

		public void treeWillExpand(TreeExpansionEvent event)throws ExpandVetoException{
			TreePath tp = event.getPath();
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)tp.getLastPathComponent();
			if(node.getChildCount()== 1
				&& "".equals( ((DefaultMutableTreeNode)node.getChildAt(0)).getUserObject() ) )
			{
				node.removeAllChildren();
				fillFileTreeNode(node, node.getUserObject());
				DefaultTreeModel model = (DefaultTreeModel)fileTree.getModel();
				model.nodeStructureChanged(node);
			}
			//throw new ExpandVetoException(event);
		}

		public void treeCollapsed(TreeExpansionEvent event){}

		public void treeExpanded(TreeExpansionEvent event){
			try{
				TreePath tp = event.getPath();
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)tp.getLastPathComponent();
				if(node.getChildCount() == 1
					&& ( ((DefaultMutableTreeNode)node.getChildAt(0)).getUserObject() instanceof ProjectController.FolderItem))
				{
					node = (DefaultMutableTreeNode)node.getChildAt(0);
					TreePath path = new TreePath(node.getPath());
					fileTree.expandPath(path);
				}else if(autoExpandEnabled){
					TreePath path = new TreePath(node.getPath());
					SwingUtilities.invokeLater(new ScrollTreeViewTask(path));
					if(searchStarted)
						autoExpandEnabled = false;
				}
				if((status & STATUS_SEARCHED) != 0){
				    TreePath expandPath = event.getPath();
				    log.fine("expanded " + ((DefaultMutableTreeNode)expandPath.getLastPathComponent()).getUserObject());
				}
			}catch(Exception ex){
				log.log(Level.SEVERE, "", ex);
			}
		}

		public void valueChanged(TreeSelectionEvent e){
		    try{
		        if(e.isAddedPath()){
                TreePath path = e.getNewLeadSelectionPath();
                for(int i = path.getPathCount() - 1; i >= 0; i--){
                    Object o = ((DefaultMutableTreeNode)path.getPathComponent(i)).getUserObject();

                    if(o instanceof ProjectModule){
                        setSelectedItem((ProjectModule)o, null, null);
                        break;
                    }else if(o instanceof ProjectController.FolderItem){
                        ProjectController.FolderItem f = (ProjectController.FolderItem)o;
                        setSelectedItem(f.module, f.path, f.name);
                        break;
                    }
                }
		        }
		    }catch(Exception ex){
		        log.log(Level.SEVERE, "", ex);
				}
		}

		public  void focusGained(FocusEvent e){
			if(e.getSource() == searchText){
				searchText.setSelectionStart(0);
				searchText.setSelectionEnd(searchText.getDocument().getLength());
			}
		}

		public void focusLost(FocusEvent e){

		}
	}

	private class ScrollTreeViewTask implements Runnable{
		TreePath path;
		public ScrollTreeViewTask(TreePath p){
			path = p;
		}

		public void run(){
			Rectangle r = fileTree.getPathBounds(path);
			Point point = new Point(r.x, r.y);
			JViewport vp = (JViewport)fileTree.getParent();
			Dimension viewSize = vp.getViewSize();
			Dimension extentSize = vp.getExtentSize();
			int dw = viewSize.width - extentSize.width;
			int x = point.x < dw ? point.x : dw;

			int dy = viewSize.height - extentSize.height;
			int y = point.y < dy ? point.y : dy;
			point.setLocation(x, y);
			vp.setViewPosition(point);
		}
	}

	//static class FileTableItem{
	//	public String name;
	//	public String path;
	//	public FileTableItem(String name, String path){
	//		this.name = name;
	//		this.path = path;
	//	}
	//}

	private class ProjFileTableRenderer extends MyListTableRender{
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
			JComponent comp = (JComponent)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			ProjFilesTableModel model = (ProjFilesTableModel)table.getModel();
			comp.setToolTipText(model.files.get(row).module.getName());

			//int oldW = table.getColumnModel().getColumn(column).getPreferredWidth();
			//int w = comp.getPreferredSize().width + 4;
			////log.fine("oldW = " + oldW + " w=" + w);
			//if(table.isCellEditable(row, column)){
	        //	TableCellEditor editor = table.getCellEditor(row, column);
	        //	Component editcomp = table.prepareEditor(editor, row, column);
	        //	if(editcomp.getPreferredSize().width > comp.getPreferredSize().width)
	        //		w = editcomp.getPreferredSize().width + 4;
	        //
	        //}
	        //if(w > oldW){
	        //	log.fine("");
	        //	table.getColumnModel().getColumn(column).setPreferredWidth(w);
	        //}

			return comp;
		}
	}

	private static class ProjFilesTableModel extends AbstractTableModel{
		public java.util.List<ProjectController.FileItem> 		files;
		public boolean						editable = false;

		public ProjFilesTableModel(java.util.List<ProjectController.FileItem> items){
			files = items;
		}

		public void refresh(SortedSet<ProjectController.FileItem> data){
			files.clear();
			Iterator<ProjectController.FileItem> it = data.iterator();
			while(it.hasNext()){
				ProjectController.FileItem e = it.next();
				files.add(e);
			}

			fireTableDataChanged();
		}

		public int getColumnCount(){
			return 2;
		}

		public int getRowCount(){
			return files.size();
		}

		public Object getValueAt(int row, int column)
		{
			switch(column){
			case 0:
				return files.get(row).name;
			case 1:
				return files.get(row).path;
			default:
				return "";
			}
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex){

		}

		public boolean isCellEditable(int row, int col) {
			return editable;

		}

		public void setEditable(boolean b){
			editable = b;
		}

		public String getColumnName(int column)
		{
			switch(column){
			case 0:
				return "Name";
			case 1:
				return "Path";
			default:
				return "";
			}
		}
	}

	private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException
    {
        log.fine("reading project panel !!!!!!!!!!!!!");
    }
}