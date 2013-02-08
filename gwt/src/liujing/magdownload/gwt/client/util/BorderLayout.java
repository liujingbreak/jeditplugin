package liujing.magdownload.gwt.client.util;

import java.util.*;
import java.io.*;
import java.util.logging.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.dom.client.Style;

/**
 BorderLayout
 @author Break(Jing) Liu
*/
public class BorderLayout{
    /** log */
    private static Logger log = Logger.getLogger(BorderLayout.class.getName());
    protected Element container;
    protected List<Layer> layers = new ArrayList();


    public BorderLayout(){
    }

    /** get container
     @return container
    */
    public Element getContainer(){
        return container;
    }

    /** set container
     @param container container
     @return content layer
    */
    public Element init(Element container, int insetLeft, int insetTop,
        int insetRight, int insetBottom)
    {
        this.container = container;
        if(!"absolute".equals(container.getStyle().getPosition())
            && !"relative".equals(container.getStyle().getPosition()))
        {
            container.getStyle().setPosition(Style.Position.RELATIVE);
        }
        Element contentLayer = DOM.createDiv();
        contentLayer.getStyle().setPosition(Style.Position.ABSOLUTE);
        if(insetTop > 0)
            contentLayer.getStyle().setPaddingTop(insetTop, Style.Unit.PX);
        if(insetLeft > 0)
            contentLayer.getStyle().setPaddingLeft(insetLeft, Style.Unit.PX);
        if(insetRight > 0)
            contentLayer.getStyle().setPaddingRight(insetRight, Style.Unit.PX);
        if(insetBottom > 0)
            contentLayer.getStyle().setPaddingBottom(insetBottom, Style.Unit.PX);
        container.appendChild(contentLayer);
        return contentLayer;
    }
    
    public void addElement(Element el, BoxBounds bounds){
        Layer layer = new Layer(el, bounds);
        layers.add(layer);
        el.getStyle().setPosition(Style.Position.ABSOLUTE);
        presetLayerBounds(layer);        
        container.appendChild(el);
    }

    protected class Layer{
        public Element element;
        public BoxBounds bounds;

        public Layer(Element el, BoxBounds bounds){
            element = el;
            this.bounds = bounds;
        }

    }

    protected void presetLayerBounds(Layer layer){
        if(layer.bounds.left>= 0)
            layer.element.getStyle().setLeft(layer.bounds.left, Style.Unit.PX);
        if(layer.bounds.top>= 0)
            layer.element.getStyle().setTop(layer.bounds.top, Style.Unit.PX);
        if(layer.bounds.width>= 0)
            layer.element.getStyle().setWidth(layer.bounds.width, Style.Unit.PX);
        if(layer.bounds.height>= 0)
            layer.element.getStyle().setHeight(layer.bounds.height, Style.Unit.PX);
    }

    public void resize(int width, int height){

        for(Layer layer : layers){
            if(layer.bounds.right >= 0){
                if(layer.bounds.width < 0 && layer.bounds.left >= 0)
                    layer.element.getStyle().setWidth(width - layer.bounds.left - layer.bounds.right, Style.Unit.PX);
                else if(layer.bounds.left < 0 && layer.bounds.width >=0)
                    layer.element.getStyle().setLeft(width - layer.bounds.width - layer.bounds.right, Style.Unit.PX);
            }
            if(layer.bounds.bottom >= 0){
                if(layer.bounds.height < 0 && layer.bounds.top >= 0)
                    layer.element.getStyle().setHeight(height - layer.bounds.bottom - layer.bounds.top, Style.Unit.PX);
                else if(layer.bounds.top < 0 && layer.bounds.height >=0)
                    layer.element.getStyle().setTop(height - layer.bounds.height - layer.bounds.bottom, Style.Unit.PX);
            }
        }
        //StringBuilder sb = new StringBuilder();
        //for(Layer layer: layers){
        //    
        //    sb.append("x: "+ layer.element.getOffsetLeft() + "-"+ layer.element.getStyle().getLeft());
        //    sb.append(", y: "+layer.element.getOffsetTop());
        //    sb.append(" | ");
        //}
        //log.info(sb.toString());
    }
}

