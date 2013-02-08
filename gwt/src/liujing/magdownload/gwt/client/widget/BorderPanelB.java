package liujing.magdownload.gwt.client.widget;

import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.dom.client.Style;
import java.util.logging.*;
import java.util.*;
import liujing.magdownload.gwt.client.util.*;

public class BorderPanelB extends CellPanel implements RequiresResize, ProvidesResize{
    private static Logger log = Logger.getLogger(BorderPanelB.class.getName());
    private Element tableRow;
    private Element containerTD;
    public BorderPanelB(){
        tableRow = DOM.createTR();
        DOM.appendChild(getBody(), tableRow);
        Element td = DOM.createTD();
        DOM.appendChild(tableRow, td);
        td.setClassName("borderB-corner");
        td.addClassName("borderB-lt");
        td = DOM.createTD();
        DOM.appendChild(tableRow, td);
        td.setClassName("borderB-tb");
        td.addClassName("borderB-t");
        td = DOM.createTD();
        DOM.appendChild(tableRow, td);
        td.setClassName("borderB-corner");
        td.addClassName("borderB-rt");

        tableRow = DOM.createTR();
        DOM.appendChild(getBody(), tableRow);
        td = DOM.createTD();
        DOM.appendChild(tableRow, td);
        td.setClassName("borderB-lr");
        td.addClassName("borderB-l");
        containerTD = DOM.createTD();
        DOM.appendChild(tableRow, containerTD);
        td = DOM.createTD();
        DOM.appendChild(tableRow, td);
        td.setClassName("borderB-lr");
        td.addClassName("borderB-r");

        tableRow = DOM.createTR();
        DOM.appendChild(getBody(), tableRow);
        td = DOM.createTD();
        DOM.appendChild(tableRow, td);
        td.setClassName("borderB-corner");
        td.addClassName("borderB-lb");
        td = DOM.createTD();
        DOM.appendChild(tableRow, td);
        td.setClassName("borderB-tb");
        td.addClassName("borderB-b");
        td = DOM.createTD();
        DOM.appendChild(tableRow, td);
        td.setClassName("borderB-corner");
        td.addClassName("borderB-rb");
    }

    public BorderPanelB(Widget w){
        this();
        add(w);
    }

    public void add(Widget w) {
        add(w, containerTD);
    }

    public void onResize(){
        for(Iterator<Widget> it = iterator(); it.hasNext();){
            Widget chr = it.next();
            if(chr instanceof RequiresResize){
                ((RequiresResize) chr).onResize();
            }
        }
    }
}
