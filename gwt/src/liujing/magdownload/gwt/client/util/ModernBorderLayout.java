package liujing.magdownload.gwt.client.util;

import java.util.*;
import java.io.*;
import java.util.logging.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.dom.client.Style;
/**
 ModernBorderLayout
 @author Break(Jing) Liu
*/
public class ModernBorderLayout extends BorderLayout{
    /** log */
    private static Logger log = Logger.getLogger(ModernBorderLayout.class.getName());
    private Element container;
    private List<Layer> layers = new ArrayList();


    public ModernBorderLayout(){
    }
    
    public Element init(Element container, int insetLeft, int insetTop,
        int insetRight, int insetBottom)
    {
        Element layer = super.init(container, insetLeft,  insetTop,
         insetRight,  insetBottom);
        layer.getStyle().clearPosition();
        return layer;
    }

    @Override
    protected void presetLayerBounds(Layer layer){
        if(layer.bounds.left>= 0)
            layer.element.getStyle().setLeft(layer.bounds.left, Style.Unit.PX);
        if(layer.bounds.top>= 0)
            layer.element.getStyle().setTop(layer.bounds.top, Style.Unit.PX);
        if(layer.bounds.width>= 0)
            layer.element.getStyle().setWidth(layer.bounds.width, Style.Unit.PX);
        if(layer.bounds.height>= 0)
            layer.element.getStyle().setHeight(layer.bounds.height, Style.Unit.PX);
        if(layer.bounds.right >= 0)
            layer.element.getStyle().setRight(layer.bounds.right, Style.Unit.PX);
        if(layer.bounds.bottom >= 0)
            layer.element.getStyle().setBottom(layer.bounds.bottom, Style.Unit.PX);
    }
    
    @Override
    public void resize(int width, int height){
        
    }
}
