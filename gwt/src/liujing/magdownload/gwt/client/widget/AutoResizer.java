package liujing.magdownload.gwt.client.widget;

import java.util.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.dom.client.Style;
import java.util.logging.*;
import liujing.magdownload.gwt.client.util.*;

public class AutoResizer{
    private boolean valid = false;
    private Widget container;
    
    public AutoResizer(Widget container){
        this.container = container;
    }
    
    public boolean isValid(){
        return valid;
    }

    /**  validate
     @return true if need validate and have been validated
    */
    public boolean validate(){
        if(!isValid()){
            ((AutoResizable)container).doLayout();
            if(container instanceof ComplexPanel){
                for(Widget w: (ComplexPanel)container){
                    if(w instanceof AutoResizable){
                        ((AutoResizable) w).getAutoResizer().validate();
                    }else if(w instanceof RequiresResize){
                        ((RequiresResize) w).onResize();
                    }
                }
            }
            valid = true;
            return true;
        }
        return false;
    }
    
    public void onResize(){
        //invalidate();
        valid = false;
        validate();
    }
    
    protected void invalidate(){
        valid = false;
        Widget p = container.getParent();
        while(!(p == null || p instanceof AutoResizable)){
            if(p instanceof ScrollPanel){
                return;
            }
            p = p.getParent();
        }
        if(p instanceof AutoResizable){
            AutoResizable a = (AutoResizable)p;
            a.getAutoResizer().invalidateIfValid();
        }
    }
    
    private void invalidateIfValid(){
        if (isValid()) {
            invalidate();
        }
    }
    
    public boolean revalidate() {
        if(container.getParent() == null)
            return false;
        invalidate();
        LayoutUtil.addDirtyWidget(container);
        return true;
    }
}
