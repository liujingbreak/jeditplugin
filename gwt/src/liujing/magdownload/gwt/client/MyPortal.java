package liujing.magdownload.gwt.client;

import liujing.magdownload.gwt.shared.FieldVerifier;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.*;
import liujing.magdownload.gwt.client.message.*;
import java.util.logging.*;
import com.google.gwt.user.client.*;
import com.google.gwt.dom.client.Style;
import liujing.magdownload.gwt.client.widget.*;
import liujing.magdownload.gwt.client.util.*;
import com.google.gwt.event.logical.shared.*;
import com.google.gwt.dom.client.Document;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class MyPortal implements EntryPoint {
  /**
   * Create a remote service proxy to talk to the server-side Greeting service.
   */
  private final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);
  PortalMessage messages;

  private static Logger log = Logger.getLogger("liujing.Test");
  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {

      adjustBodyStyle();
    messages = GWT.create(PortalMessage.class);
    HorizontalPanel mainPanel = new HorizontalPanel();
    mainPanel.setStyleName("main");
    mainPanel.getElement().setPropertyString("align","center");
    //mainPanel.getElement().getStyle().setHeight(100, Style.Unit.PCT);
    final VBox fp = new VBox();
    mainPanel.add(fp);

    HorizontalPanel headerPanel = new HorizontalPanel();
    headerPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    //Image titleLogo = new Image(GWT.getModuleBaseURL()+ "title.png");
    Image titleLogo = new Image(GWT.getModuleBaseURL() + "title.png");
    LayoutUtil.validateOnload(titleLogo);
    titleLogo.setStyleName("titleLogo");
    headerPanel.add(titleLogo);

    HTMLPanel introduction = new HTMLPanel(messages.introduction());
    introduction.setStyleName("colorfulFont");
    headerPanel.add(introduction);

    fp.add(headerPanel);


    HorizontalPanel demoImagePanel = new HorizontalPanel();
    PanelB imagePanel = new PanelB(new ScrollPanel(demoImagePanel));
    imagePanel.setMaxSize(900, -1);
    fp.add(imagePanel);
    fp.addExpand(new VBox());
    HTMLPanel footerPanel = new HTMLPanel(messages.address());
    //BorderPanelB borderPanel = new BorderPanelB(footerPanel);
    fp.add(footerPanel);
    fp.setHeight(Window.getClientHeight()+"px");

    Window.addResizeHandler(new ResizeHandler(){
            public void onResize(ResizeEvent event){
                int winH = Window.getClientHeight();
                winH = winH < 400? 400:winH;
                fp.setHeight(winH + "px");
                log.fine("resize "+ winH);
                fp.getAutoResizer().revalidate();
                //LayoutUtil.addDirtyWidget(fp);
            }
    });
    Image demoImg = new Image(GWT.getModuleBaseURL() + "demo/cover01.jpg");
    LayoutUtil.validateOnload(demoImg);
    demoImagePanel.add(demoImg);
    demoImg = new Image(GWT.getModuleBaseURL() + "img?path="+ GWT.getModuleName() + "/demo/cover02.jpg");LayoutUtil.validateOnload(demoImg);demoImagePanel.add(demoImg);
    demoImg = new Image(GWT.getModuleBaseURL() + "demo/cover03.jpg");LayoutUtil.validateOnload(demoImg);demoImagePanel.add(demoImg);
    demoImg = new Image(GWT.getModuleBaseURL() + "demo/cover04.jpg");LayoutUtil.validateOnload(demoImg);demoImagePanel.add(demoImg);
    demoImg = new Image(GWT.getModuleBaseURL() + "demo/cover05.jpg");LayoutUtil.validateOnload(demoImg);demoImagePanel.add(demoImg);
    demoImg = new Image(GWT.getModuleBaseURL() + "demo/cover06.jpg");LayoutUtil.validateOnload(demoImg);demoImagePanel.add(demoImg);

    RootPanel.get().add(mainPanel);

  }

  private void adjustBodyStyle(){
      Document.get().getBody().getStyle().setMargin(0, Style.Unit.PX);
  }
    //var st = $doc.body.style;
    //st.margin = "0px 0px";

}
