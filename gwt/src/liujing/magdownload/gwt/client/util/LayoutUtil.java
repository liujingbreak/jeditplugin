package liujing.magdownload.gwt.client.util;

import java.util.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.dom.client.Style;
import java.util.logging.*;
import liujing.magdownload.gwt.client.widget.*;

/**
 LayoutUtil
 @author Break(Jing) Liu
*/
public class LayoutUtil{
    /** log */
    private static Logger log = Logger.getLogger(LayoutUtil.class.getName());
    private static com.google.gwt.user.client.Timer layoutTimer;


    public LayoutUtil(){
    }

    public static void validateOnload(HasLoadHandlers w){
        HandlerRegistration loadReg = w.addLoadHandler(new LoadHandlerImpl(w));
        //((Widget)w).addAttachHandler();
    }

    private static class LoadHandlerImpl implements LoadHandler{
        private Widget widget;
        public LoadHandlerImpl(HasLoadHandlers w){
            widget = (Widget)w;
        }
        public void onLoad(LoadEvent event){
            //log.fine("onload");
            Widget p = widget.getParent();
            while(p != null && !(p instanceof AutoResizable)){
                p = p.getParent();
                //log.fine("p"+ p.getElement().getTagName());
            }

            if(p != null && (p instanceof AutoResizable)){
                ((AutoResizable)p).getAutoResizer().revalidate();
            }
        }
    }

    static LinkedList<AutoResizable> layoutRoots = new LinkedList<AutoResizable>();
    
    public static void addDirtyWidget(Widget w){
        AutoResizable root = null;
        while(w != null && !(w instanceof ScrollPanel)){
            if(w instanceof AutoResizable){
                root = (AutoResizable)w;
            }
            w = w.getParent();
        }
        log.fine("layoutRoots size "+ layoutRoots.size());

        if(root != null && !(layoutRoots.size() >0 && layoutRoots.getLast() == root)){
            layoutRoots.add(root);
            startValidate();
        }
    }

    protected static void startValidate(){
        if(layoutTimer != null){
            return;
        }
        layoutTimer = new com.google.gwt.user.client.Timer(){
            @Override
              public void run() {
                  for(Iterator<AutoResizable> it = layoutRoots.iterator(); it.hasNext();){
                      AutoResizable root = it.next();
                      it.remove();
                      log.fine("validate "+ root.getClass().getName());
                      ((RequiresResize)root).onResize();
                  }
                  log.fine("validate loop over");
                  layoutTimer = null;
              }
        };
        layoutTimer.schedule(700);
    }

}


