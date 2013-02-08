package org.liujing.ironsword.cmd;

import java.util.*;
import java.io.*;
/**
    use getContentPanel().add() to add widget to this dialog
*/
public class OKCancelDialog extends ConsoleAlert implements ConsoleEventHandler{
        ConsoleLabel title;
        
        ConsoleChoiceList actions;
        public static final int OK_EVENT = 0;
        public static final int CANCEL_EVENT = 1;
        ConsoleContainerWidget contentPanel;
        
        public OKCancelDialog(String titleStr){
            title = new ConsoleLabel(titleStr);
            add(title);
            contentPanel = new ConsoleContainerWidget();
            add(contentPanel);
            ConsoleChoiceList actions = new ConsoleChoiceList();
            actions.addItem("OK");
            actions.addItem("<< Cancel");
            add(actions);
            actions.addSelEvtHandler(this);
        }
        
        public ConsoleContainerWidget getContentPanel(){
            return contentPanel;
        }
        
        public void handleEvent(ConsoleEvent evt){
            int selNo = ((Number)evt.getData()).intValue();
            if(selNo == 0){
                hide();
                fireEvent(OK_EVENT, evt.getInput());
            }else if(selNo == 1){
                hide();
                fireEvent(CANCEL_EVENT, evt.getInput());
            }
        }
    }
