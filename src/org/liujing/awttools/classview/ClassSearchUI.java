package org.liujing.awttools.classview;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.util.logging.*;
import java.util.concurrent.*;
import java.util.regex.*;

public class ClassSearchUI extends JPanel
implements ActionListener,LinkExplorerListener, HistoryListener<String>
{

	private static Logger log=Logger.getLogger(ClassSearchUI.class.getName());
	private JTextField searchBox;
	private LinkExplorer explorer;
	private JScrollPane expScrllPane;
	private Javaprint classScanner;
	private JLabel statusLb;
	private JLabel statusLb2;

	private JDialog dialog;
	private SearchTask searchTask;
	private ExecutorService bgWorker;
	private JButton stopBtn;
	private JButton goBtn;
	private Future<?> future;
	private LinkedList<String> history;
	private JTextField findBox;
	private int currentHisPos=-1;


	private JButton clearFoundBtn;
	private java.util.List<int[]> foundList;
	private int foundIdx=0;
	private JButton nextFoundBtn;

	private BrowserHistory<String> historyComp;

	private static int HISTORY_SIZE=15;

	public ClassSearchUI()
	{
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

		history=new LinkedList<String>();

		JButton dpHisBtn = new JButton("H");
		dpHisBtn.setToolTipText("History");
		historyComp = new BrowserHistory<String>(15);
		historyComp.boundPopup(dpHisBtn);
		historyComp.addHistoryListener(this);
		//searchBox=new JComboBox();
		searchBox=new JTextField();
		searchBox.setEditable(true);
		searchBox.addActionListener(this);

		JPanel controlPn=new JPanel();
		controlPn.setLayout(new BoxLayout(controlPn,BoxLayout.X_AXIS));
		controlPn.add(dpHisBtn);
		controlPn.add(searchBox);

		stopBtn=new JButton("Stop");

		stopBtn.addActionListener(this);

		goBtn=new JButton("Go");
		goBtn.addActionListener(this);
		controlPn.add(goBtn);
		controlPn.add(stopBtn);
		controlPn.setMaximumSize(new Dimension(Integer.MAX_VALUE,28));
		controlPn.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(controlPn);

		explorer=new LinkExplorer();
		explorer.addLinkExpListener(this);
		expScrllPane=new JScrollPane(explorer);
		expScrllPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(expScrllPane);


		findBox=new JTextField();
		findBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		findBox.setMaximumSize(new Dimension(Integer.MAX_VALUE,28));
		findBox.addActionListener(this);
		findBox.setToolTipText("Input regular expression and press Enter.");

		clearFoundBtn=new JButton("Clear");
		clearFoundBtn.setToolTipText("Clear all highlight result");
		clearFoundBtn.addActionListener(this);

		nextFoundBtn=new JButton("Next");
		nextFoundBtn.setToolTipText("Go to next found place");
		nextFoundBtn.addActionListener(this);

		JPanel findPanel=new JPanel();
		findPanel.setLayout(new BoxLayout(findPanel,BoxLayout.X_AXIS));
		findPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		findPanel.add(new JLabel("Find"));
		findPanel.add(findBox);
		findPanel.add(clearFoundBtn);
		findPanel.add(nextFoundBtn);
		add(findPanel);

		JPanel statusPanel=new JPanel();
		statusPanel.setLayout(new BoxLayout(statusPanel,BoxLayout.X_AXIS));
		statusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		statusLb=new JLabel("Ready");

		statusPanel.add(statusLb);
		statusPanel.add(Box.createHorizontalGlue());
		statusLb2 = new JLabel(" ");
		statusPanel.add(statusLb2);
		add(statusPanel);

		searchTask=new SearchTask();

	}
	public void cleanup()
	{
		clearFoundList();
		history.clear();
	}

	/**
	required
	*/
	public void setBgWorker(ExecutorService threadpool)
	{
		this.bgWorker=threadpool;
	}
	protected JDialog createSimpleDialog(Window owner)
	{
		JDialog dialog=new JDialog(owner,"Class Reference");
		dialog.getContentPane().add(this);
		dialog.setSize(new Dimension(400,300));
		dialog.setDefaultCloseOperation(dialog.HIDE_ON_CLOSE);
		dialog.setLocationRelativeTo(null);
		return dialog;
	}


	public synchronized JDialog getSimpleDialog(Window owner)
	{
		if(dialog==null){
			dialog=createSimpleDialog(owner);
		}
		return dialog;
	}

	public void setClassScanner(Javaprint jp)
	{
		classScanner=jp;
		classScanner.setExplorer(explorer);
	}
	public Javaprint getClassScanner()
	{
		return classScanner;
	}
	public void testInit()
	{
		setBgWorker(Executors.newSingleThreadExecutor());
		Javaprint javap=new Javaprint();
		setClassScanner(javap);
	}
	public void actionPerformed(ActionEvent e)
	{
		try{
			if(e.getSource()==searchBox||e.getSource()==goBtn){
				//log.info("..");
				statusLb.setText("Seaching ...");
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				future=bgWorker.submit(searchTask);
				historyComp.addHistory(searchBox.getText());
			}else if(e.getSource()==stopBtn){
				/*if(future!=null && !future.isDone()){
					future.cancel(true);
				}*/
				classScanner.setStopSearch(true);
			}else if(e.getSource()==findBox){
				findText(findBox.getText());
			}else if(e.getSource()==clearFoundBtn){
				clearFoundList();
			}else if(e.getSource()==nextFoundBtn){
				if(foundList==null||foundList.size()==0){
					findText(findBox.getText());
				}else{

					if(foundIdx>=foundList.size())
						foundIdx=0;

					explorer.select(foundList.get(foundIdx)[0],
						foundList.get(foundIdx)[1]);
					explorer.getCaret().setSelectionVisible(true);//defualt is not visible
					foundIdx++;
				}
			}
		}catch(Exception ex){
			log.log(Level.SEVERE,"Failed to search class "+searchBox.getText(),ex);
			statusLb.setText(ex.toString());
			setCursor(Cursor.getDefaultCursor());
		}
	}
	public void onLink(LinkElement e){
		statusLb.setText(e.getHref());
	}
	public void outLink(LinkElement e){
		statusLb.setText(" ");//can't leave blank here, the label will disappear
	}
	public void doLink(LinkElement e,boolean doubleClick)
	{
		searchBox.setText(e.getHref());
		searchBox.requestFocusInWindow();
		if(doubleClick){
			goBtn.doClick();
		}
	}
	public void onHistoryLink(String linkAddr){
		searchBox.setText(linkAddr);
		statusLb.setText("Seaching ...");
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		future=bgWorker.submit(searchTask);
	}

	public void disposeDialog()
	{
		dialog.dispose();
		dialog=null;
	}
	private void findText(String regex)
	{
		if(regex.length()<=0) return;

		if(foundList==null)
			foundList=new ArrayList<int[]>();
		else
			clearFoundList();
		int count=explorer.findText(regex,foundList);

		if(count>0){
			foundIdx = 0;
			explorer.select(foundList.get(foundIdx)[0],foundList.get(foundIdx)[1]);
			explorer.getCaret().setSelectionVisible(true);
			foundIdx++;
		}
		statusLb.setText(count+" found");
	}
	private void clearFoundList()
	{
		if(foundList == null) return;
		for(int[] offsets:foundList){
			explorer.unHighLight(offsets[0],offsets[1]);
		}
		foundList.clear();
		foundIdx = 0;
	}

	private class SearchTask implements Runnable
	{
		public synchronized void run()
		{
			try{

				classScanner.print(searchBox.getText());
				SwingUtilities.invokeLater(
					new Runnable(){
						public void run()
						{
							statusLb.setText("Complete.");
							setCursor(Cursor.getDefaultCursor());
							//((JViewport)explorer.getParent()).setViewPosition(new Point(0,0));
						}
					});


			}catch(Exception ex){
				log.log(Level.SEVERE,"Failed to search class "+searchBox.getText(),ex);
			}
		}
	}


}
