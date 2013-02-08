package liujing.magdownload.gwt.client.widget;

import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.dom.client.Style;
import com.google.gwt.core.client.GWT;
import java.util.logging.*;
import java.util.*;
import liujing.magdownload.gwt.client.util.*;

public class PanelB extends ComplexPanel implements AutoResizable,RequiresResize, ProvidesResize
{
    private static Logger log = Logger.getLogger(PanelB.class.getName());
    private boolean rendered = false;
    private boolean valid = false;
    protected int insetTop;
    protected int insetLeft;
    protected int insetBottom;
    protected int insetRight;
    protected Element container;
    protected BorderLayout layout;
    protected AutoResizer autoResizer;
    protected Element wrapper;

    public PanelB(Widget w){
        autoResizer = new AutoResizer(this);
        Element tableEl = DOM.createTable();
        Element tbodyEl = DOM.createTBody();
        tableEl.appendChild(tbodyEl);
        Element td = DOM.createTD();
        Element tr = DOM.createTR();
        tr.appendChild(td);
        tbodyEl.appendChild(tr);
        wrapper = DOM.createDiv();
        td.appendChild(wrapper);
        setElement(tableEl);
        

        //container = DOM.createDiv();
        //getElement().appendChild(container);

        createBorder();
        add(w);
        //setContainerMargin();
    }

    public AutoResizer getAutoResizer(){
        return autoResizer;
    }

    protected void createBorder(){
        insetTop = 8;
        insetLeft = 7;
        insetRight = 11;
        insetBottom = 6;
        layout = GWT.create(BorderLayout.class);
        container = layout.init(wrapper, insetLeft, insetTop, insetRight, insetBottom);
        //left top
        BoxBounds bounds = new BoxBounds();
        bounds.top = 0;
        bounds.width = 9;
        bounds.height = 10;
        Element el = DOM.createDiv();
        el.setClassName("borderB-corner");
        el.addClassName("borderB-lt");
        layout.addElement(el, bounds);
        //top
        bounds = new BoxBounds();
        bounds.top = 0;
        bounds.left = 9;
        bounds.right = 13;
        el = DOM.createDiv();
        el.setClassName("borderB-tb");
        el.addClassName("borderB-t");
        layout.addElement(el, bounds);
        //right top
        bounds = new BoxBounds();
        bounds.top = 0;
        bounds.right = 0;
        bounds.width = 13;
        el = DOM.createDiv();
        el.setClassName("borderB-corner");
        el.addClassName("borderB-rt");
        layout.addElement(el, bounds);

        //left
        bounds = new BoxBounds();
        bounds.top = 10;
        bounds.left = 0;
        bounds.bottom = 12;
        el = DOM.createDiv();
        el.setClassName("borderB-lr");
        el.addClassName("borderB-l");
        layout.addElement(el, bounds);

        //right
        bounds = new BoxBounds();
        bounds.top = 10;
        bounds.right = 0;
        bounds.bottom = 12;
        bounds.width = 13;
        el = DOM.createDiv();
        el.setClassName("borderB-lr");
        el.addClassName("borderB-r");
        layout.addElement(el, bounds);
        //left bottom
        bounds = new BoxBounds();
        bounds.left = 0;
        bounds.bottom = 0;
        bounds.height = 12;
        el = DOM.createDiv();
        el.setClassName("borderB-corner");
        el.addClassName("borderB-lb");
        layout.addElement(el, bounds);
        //bottom
        bounds = new BoxBounds();
        bounds.left = 9;
        bounds.right = 13;
        bounds.bottom = 0;
        bounds.height = 12;
        el = DOM.createDiv();
        el.setClassName("borderB-tb");
        el.addClassName("borderB-b");
        layout.addElement(el, bounds);
        //right bottom
        bounds = new BoxBounds();
        bounds.right = 0;
        bounds.bottom = 0;
        bounds.height = 12;
        bounds.width = 13;
        el = DOM.createDiv();
        el.setClassName("borderB-corner");
        el.addClassName("borderB-rb");
        layout.addElement(el, bounds);
    }

    //protected void setContainerMargin(){
    //    //log.fine("user.agent="+ Window.Navigator.getUserAgent());
    //    container.getStyle().setPosition(Style.Position.ABSOLUTE);
    //    if(insetTop > 0)
    //        container.getStyle().setPaddingTop(insetTop, Style.Unit.PX);
    //    if(insetLeft > 0)
    //        container.getStyle().setPaddingLeft(insetLeft, Style.Unit.PX);
    //    if(insetRight > 0)
    //        container.getStyle().setPaddingRight(insetRight, Style.Unit.PX);
    //    if(insetBottom > 0)
    //        container.getStyle().setPaddingBottom(insetBottom, Style.Unit.PX);
    //}

    @Override
    public void add(Widget w) {
        add(w, container);
    }

    public void onResize(){
        autoResizer.onResize();
        //int width = container.getClientWidth();
        //int height = container.getClientHeight();
        //layout.resize(width, height);
    }
    
    protected int maxWidth = -1;
    protected int maxHeight = -1;
    
    public void setMaxSize(int width, int height){
        maxWidth = width;
        maxHeight = height;
    }
    

    @Override
    protected void onLoad() {
        super.onLoad();
        autoResizer.validate();
    }

    @Override
    protected void onUnload() {

    }

    public void doLayout(){

        //int width = 700;
        //int height = 300;
        int width = container.getOffsetWidth();
        int height = container.getOffsetHeight();
        if(maxWidth >= 0 && width > maxWidth){
            width = maxWidth;
            container.getStyle().setWidth( width, Style.Unit.PX);
        }
        if(maxHeight >= 0 && height > maxHeight){
            height = maxHeight;
            container.getStyle().setHeight( height, Style.Unit.PX);
        }
        //log.fine("client width="+ container.getClientWidth() + " height="+ container.getClientHeight());
        if(width > (insetLeft + insetRight) && height > (insetTop + insetBottom)){
            layout.resize(width, height);
        }
        
    }
}
