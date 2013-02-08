package org.liujing.awttools.classview;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.event.*;
import javax.swing.text.*;
import javax.imageio.*;
import liujing.swing.*;
import org.liujing.util.LogWindow;
import liujing.util.dirscan.*;
import java.util.concurrent.*;

public class ClassSearchFrame extends JPanel{
    private static Logger log = Logger.getLogger(ClassSearchFrame.class.getName());

    JButton bt_save;
    JButton bt_refresh;
    JTextPane tp_info;
    JSplitPane splitP;
    JScrollPane textScroll;
    JTabbedPane mainTabPanel;

    protected SimpleAttributeSet infoAttr = new SimpleAttributeSet();
    protected SimpleAttributeSet defaultAttr = new SimpleAttributeSet();
    protected SimpleAttributeSet errorAttr = new SimpleAttributeSet();
    protected ClassSearchConfigUI setupUI;
    protected ClassSearchPersist persist = new ClassSearchPersist();

    private EventHandler eventHandler = new EventHandler();
    private ClassSearchUI classSearchUI;
    private Javaprint classSearchEngine;
    private ExecutorService extor;


    public ClassSearchFrame(){
        mainTabPanel = new JTabbedPane();
        JPanel setupPan = new JPanel();
        setFont(new Font("DialogInput", Font.PLAIN ,14));
        setupPan.setLayout(new BoxLayout(setupPan, BoxLayout.Y_AXIS));

        setupUI = new ClassSearchConfigUI();
        setupPan.add(setupUI);

        JPanel buttonP = new JPanel();
        buttonP.add(Box.createHorizontalGlue());
        buttonP.setLayout(new BoxLayout(buttonP, BoxLayout.X_AXIS));
        bt_save = new JButton("Save");
        bt_save.setAlignmentX(0.5f);
        bt_save.addActionListener(eventHandler);
        bt_save.setActionCommand("S");
        buttonP.add(bt_save);
        buttonP.add(Box.createHorizontalGlue());

        bt_refresh = new JButton("Refresh");
        bt_refresh.setToolTipText("Rescan and Refresh the classpath setting");
        bt_refresh.addActionListener(eventHandler);
        bt_refresh.setActionCommand("R");
        buttonP.add(bt_refresh);
        buttonP.add(Box.createHorizontalGlue());
        setupPan.add(buttonP);

        tp_info = new LogWindow.MyTextPane();
        //.setBorder(new EmptyBorder(new Insets(2, 5, 5, 5)));
        textScroll = new JScrollPane(tp_info);
        splitP = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            setupPan, textScroll
            );
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(new Insets(2, 5, 5, 5)));
        add(splitP);
        setupStyleDoc();

        classSearchUI = new ClassSearchUI();
        classSearchEngine = new Javaprint();
        extor = Executors.newSingleThreadExecutor();
        classSearchUI.setBgWorker(extor);
		classSearchUI.setClassScanner(classSearchEngine);

		printHelp();
        try{
            setupUI.setClassPath(persist.load());
        }catch(Exception ex){
            log.log(Level.WARNING, "", ex);
            addErrorMessage(ex.toString());
        }

    }

    private void setupStyleDoc(){
        StyleConstants.setForeground(infoAttr,Color.WHITE);
        StyleConstants.setBackground(infoAttr,Color.BLUE);
        //StyleConstants.setForeground(defaultAttr,Color.RED);
        StyleConstants.setForeground(errorAttr,Color.RED);
        StyleConstants.setFontFamily(defaultAttr, "DialogInput");
        StyleConstants.setFontSize(defaultAttr, 14);
    }

    private void printHelp(){
        addMessage("Path pattern sample");
        addInfoMessage("1) single directory or jar file pattern");
        addMessage("  D:\\src\\module1\\build\\classes");
        addMessage("  D:\\src\\module1\\lib\\antlr-3.3-complete.jar");

        addInfoMessage("2) recursive pattern");
        addMessage("  D:\\jboss\\server\\main\\**\\*.jar");
        addMessage("  D:\\jboss\\server\\main\\lib\\*.jar ");

        addInfoMessage("3) selective pattern");
        addMessage("  D:\\src\\{module1,module2}\\lib\\*.jar");

        addInfoMessage("4) excludable pattern");
        addMessage("  D:\\src\\!{module3,test}\\lib\\*.jar");
        addMessage("  D:\\src\\!test\\lib\\*.jar");
        addMessage("");
    }

    @Override
	public void addNotify(){
		super.addNotify();
		SwingUtilities.invokeLater(new Runnable()
			{
				public void run(){
					splitP.setDividerLocation(0.5d);
					rescan(setupUI.getClassPath());
				}
		});
	}

	protected void rescan(final Collection<String> paths){
	    if(paths.size() <= 0) return;

         extor.submit(new Runnable(){
                 public void run(){
                     try{
                        DirectoryScan2 scanner = new DirectoryScan2(paths,
                             new ArrayList<String>(), false);
                        scanner.enableDirectory(true);
                        StringBuilder buf = new StringBuilder();
                        ClasspathScan scanhandler = new ClasspathScan(buf);
                        scanner.scan(new File("."), scanhandler);
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run(){
                                addInfoMessage("Classpath scan done");
                            }
                        });

                        classSearchEngine.setClasspath(buf.toString());
                        mainTabPanel.setEnabledAt(1, true);
                     }catch(Exception ex){
                            log.log(Level.WARNING, "", ex);
                            addErrorMessage(ex.toString());
                     }
                 }
         });

	}

    class EventHandler implements ActionListener{
        public void actionPerformed(ActionEvent e){
            try{
                if("S".equals(e.getActionCommand())){
                    Collection<String> cps = setupUI.getClassPath();
                    persist.save(cps);
                    addInfoMessage("\n\n------ saved -----------------------");
                    rescan(cps);
                }
                else if("R".equals(e.getActionCommand())){
                    Collection<String> cps = setupUI.getClassPath();
                    rescan(cps);
                }
            }catch(Exception ex){
                log.log(Level.WARNING, "", ex);
                addErrorMessage(ex.toString());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                PrintStream p = new PrintStream(out);
                ex.printStackTrace(p);
                addErrorMessage(new String(out.toByteArray()));
            }
        }
    }


    private class ClasspathScan implements ScanHandler2{
		StringBuilder classpath;
		public HashSet pathSet = new HashSet();
		int count = 0;

		public ClasspathScan(){}

		public ClasspathScan(StringBuilder p){
			setPaths(p);
		}

		public void setPaths(StringBuilder p){
			classpath = p;
		}

		public void processFile(final File f, String relativePath){

			if(! pathSet.contains(f)){
			    if( count == 0){
			        addInfoMessage("Scanning classpath ...");
			    }
				classpath.append(f.getPath());
				classpath.append(File.pathSeparator);
				pathSet.add(f);
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run(){
					    addMessage(f.getPath());
					}
				});
				count++;
				//log.info("cp file "+ f.getPath());
			}else{
				log.fine("Duplicate Classpath " + f.getPath());

			}
		}
	}

    protected void addInfoMessage(String text){
        addMessage(text, infoAttr);
    }

    protected void addMessage(String text){
        addMessage(text, defaultAttr);
    }

    protected void addMessage(String text, SimpleAttributeSet attr){
        try{
        StyledDocument doc = tp_info.getStyledDocument();
        doc.insertString(doc.getLength(),text+"\n",attr);
        SwingUtilities.invokeLater(new Runnable()
				{
					public void run(){
					    JScrollBar bar = textScroll.getVerticalScrollBar();
						bar.setValue(bar.getMaximum());
					}
				});
		}catch(BadLocationException be){
		    log.log(Level.WARNING, "", be);
		}
    }

    protected void addErrorMessage(String text){
        addMessage(text, errorAttr);
    }

    public static void main(String[] args){
        ClassSearchFrame ctl = new ClassSearchFrame();
        JFrame window = new JFrame("My Class Path Viewer");

		ctl.mainTabPanel.add("Setup", ctl);


		ctl.mainTabPanel.add("Search", ctl.classSearchUI);
		ctl.mainTabPanel.setEnabledAt(1, false);
		window.getContentPane().add(ctl.mainTabPanel);
		window.setBounds(200,200,500,500);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE );
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		log.fine("window is loaded");
    }
}
