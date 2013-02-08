package liujing.magdownload.gwt.client.widget;

import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.dom.client.Style;
import java.util.logging.*;
import java.util.*;
import liujing.magdownload.gwt.client.util.*;

public class VBox extends FlowPanel implements RequiresResize, ProvidesResize, AutoResizable{
    private static Logger log = Logger.getLogger(VBox.class.getName());
    protected Set<Widget> expandWidgets;
    private boolean rendered = false;
    private int prefHeight = -1;
    private int assiHeight = -1;
    private int assiWidth = -1;
    protected AutoResizer autoResizer;


    public VBox(){
        autoResizer = new AutoResizer(this);
    }

    public AutoResizer getAutoResizer(){
        return autoResizer;
    }
    /**
     * Adds a new child widget to the panel.
     *
     * @param w the widget to be added
     */
    @Override
    public void add(Widget w) {
        setTempPos(w);
        String tagName = w.getElement().getTagName().toLowerCase();
        if(tagName.equals("div") || tagName.equals("table")){
            add(w, getElement());
        }else{
            Element container = DOM.createDiv();
            getElement().appendChild(container);
            add(w, container);
        }
        if(rendered && expandWidgets.size() > 0){
            autoResizer.revalidate();
        }
        clearTempPos(w);
    }

    public void addExpand(Widget w) {
        setTempPos(w);
        if(expandWidgets == null)
            expandWidgets = new HashSet();
        expandWidgets.add(w);
        log.fine("expandWidgets:"+ expandWidgets.size());
        add(w);
        if(rendered && expandWidgets.size() > 0){
            autoResizer.revalidate();
        }
        clearTempPos(w);
    }

    private void setTempPos(Widget w){
        //w.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
    }

    private void clearTempPos(Widget w){
        //w.getElement().getStyle().clearPosition();
    }



    public void doLayout(){
        if(expandWidgets == null)
            return;
        int height = getElement().getClientHeight();
        //log.fine("height="+ height);
        Iterator<Widget> it = iterator();
        while(it.hasNext()){
            Widget chr = it.next();
            if(expandWidgets.contains(chr))
                continue;
            //log.fine("off height="+ chr.getElement().getOffsetHeight());
            height -= chr.getElement().getOffsetHeight();
        }
        //log.fine("remain height="+height);

        int eachSize = height/expandWidgets.size();
        eachSize = eachSize>0?eachSize:0;
        Iterator<Widget>  itex = expandWidgets.iterator();
        while(itex.hasNext()){
            Widget chr = itex.next();
            chr.setHeight(eachSize + "px");
        }

    }



    public int prefHeight()
    {
        if(prefHeight < 0){
            prefHeight = getElement().getOffsetHeight();
        }
        return prefHeight;
    }

    @Override
    protected void onLoad(){
        log.fine("onload");
        autoResizer.validate();
        rendered = true;
    }

    public void onResize(){
        autoResizer.onResize();
    }

    @Override
    public void insert(Widget w, int beforeIndex) {
        setTempPos(w);
        String tagName = w.getElement().getTagName().toLowerCase();
        if(tagName.equals("div") || tagName.equals("table")){
            insert(w, getElement(),  beforeIndex, true);
            return;
        }
        Element container = DOM.createDiv();
        getElement().appendChild(container);
        insert(w, container, beforeIndex, true);
        if(rendered && expandWidgets.size() > 0){
            autoResizer.revalidate();
        }
        clearTempPos(w);
    }

    @Override
    public boolean remove(Widget w) {
        Element container = DOM.getParent(w.getElement());
        boolean res = super.remove(w);
        if( !res)
            return res;
        String tagName = w.getElement().getTagName().toLowerCase();
        if(!(tagName.equals("div") || tagName.equals("table"))){
            DOM.removeChild(DOM.getParent(container), container);
        }
        expandWidgets.remove(w);
        //for(int i = 0, l = expandWidgets.length; i< l; i++){
        //    if(expandWidgets.get(i) == w){
        //        expandWidgets.remove(i);
        //        break;
        //    }
        //}
        return res;
    }

}
