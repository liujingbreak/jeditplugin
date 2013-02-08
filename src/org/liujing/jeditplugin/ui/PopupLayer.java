package org.liujing.jeditplugin.ui;

import java.awt.*;
import javax.swing.*;

public class PopupLayer extends JPanel{
    private Popup popup;

    public PopupLayer(){
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS ));
    }

    public void show(JComponent invoker, int x, int y){
        //if(popup == null){
          Point invokerOrigin;
          if (invoker != null) {
              invokerOrigin = invoker.getLocationOnScreen();

              // To avoid integer overflow
              long lx, ly;
              lx = ((long) invokerOrigin.x) +
                   ((long) x);
              ly = ((long) invokerOrigin.y) +
                   ((long) y);
              if(lx > Integer.MAX_VALUE) lx = Integer.MAX_VALUE;
              if(lx < Integer.MIN_VALUE) lx = Integer.MIN_VALUE;
              if(ly > Integer.MAX_VALUE) ly = Integer.MAX_VALUE;
              if(ly < Integer.MIN_VALUE) ly = Integer.MIN_VALUE;
              popup = PopupFactory.getSharedInstance().getPopup(invoker, this, (int)lx, (int)ly);
          }else{
              popup = PopupFactory.getSharedInstance().getPopup(invoker, this, x, y);
          }
        //}
        popup.show();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void hide(){
      if(popup != null)
          popup.hide();
    }
}
