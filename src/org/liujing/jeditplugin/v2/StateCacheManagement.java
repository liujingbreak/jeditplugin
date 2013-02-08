package org.liujing.jeditplugin.v2;

import java.io.*;
import java.util.zip.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import org.liujing.filesync.*;
import org.liujing.filesync.StateReadController.IndexEntity;
import liujing.persist.*;
/**
A helper for ProjectModule to maintain its state cache
*/
public class StateCacheManagement{
    private static Logger log = Logger.getLogger(StateCacheManagement.class.getName());
    private static Map<String, Object> EMPTY_OPTIONS = new HashMap();
    //private static StateCacheManagement instance = new StateCacheManagement();
    private JDialog patchDialog;
    private StateFileViewPanel stateView;

    public StateCacheManagement(Frame owner){
        patchDialog = new JDialog(owner, "File Changes", true);
        Container rootPanel = patchDialog.getContentPane();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS ));
        stateView = new StateFileViewPanel();
        rootPanel.add(stateView);

        //JPanel buttons = new JPanel();
        //buttons.setLayout(new BoxLayout(rootPanel, BoxLayout.X_AXIS ));
        //JButton
        //buttons.add();
        patchDialog.addWindowListener(new DialogWindowListener());
        patchDialog.setLocationRelativeTo(owner);
        //patchDialog.setDefaultCloseOperation();
        patchDialog.setSize(400, 400);
    }

    private class DialogWindowListener extends WindowAdapter{
        public void windowClosing(WindowEvent e) {
            try{
                log.fine("delete patch state files");
                stateView.clearAndDelete();
            }catch(IOException ioe){
                log.log(Level.WARNING, "Unable to delete file ", ioe);
            }
        }
    }

    //public static StateCacheManagement getInstance(){
    //    return instance;
    //}

    public void createState(ProjectModule module)
    throws IOException, javax.xml.stream.XMLStreamException
    {
        //Map<String, String> options = new HashMap();
        createState(module, EMPTY_OPTIONS);
    }

    protected void createState(ProjectModule module, Map<String, Object> options)
    throws IOException, javax.xml.stream.XMLStreamException{
        module.releaseStateCache();
        FileSyncController sync = new FileSyncController();
        sync.addHandler(new StateCreationHandler());
        SourceDir src = new SourceDir(module.getDirectory());
        for(String include : module.getIncludes())
            src.addInclude(include);
        for(String exclude : module.getExcludes())
            src.addExclude(exclude);
        sync.config(src, module.stateFile().getName());
        sync.createUpdates(module.stateFile().getParentFile(), options);
    }

    public void updateState(ProjectModule module, String scanPath)throws IOException, ClassNotFoundException{
        module.releaseStateCache();
        FileSyncController sync = new FileSyncController();
        SourceDir src = new SourceDir(module.getDirectory());
        for(String include : module.getIncludes())
            src.addInclude(include);
        for(String exclude : module.getExcludes())
            src.addExclude(exclude);
        sync.config(src, module.stateFile().getName());
        File patchFile = sync.scanForUpdates(module.stateFile(), scanPath, true);
        stateView.setStateFile(patchFile);
        patchDialog.setVisible(true);
    }

    public void addFile(ProjectModule module){
    }

    public void removeFile(ProjectModule module){
    }


}
