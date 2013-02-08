package liujing.magdownload.gwt.client.widget;

import java.util.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.dom.client.Style;
import java.util.logging.*;

/**
 AutoResizable
 @author Break(Jing) Liu
*/
public interface AutoResizable{
    public AutoResizer getAutoResizer();
    
    /**
    implement your customized layout logic
    */
    public void doLayout();
}

