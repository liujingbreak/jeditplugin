YUI.add("lj-init", function(Y){
        
    var res = Y.Intl.get("lj-init");
        
    /**@class StatusBar */
    var StatusBar = Y.Base.create("statusBar", Y.Widget, 
        [Y.WidgetPosition, Y.WidgetStack, Y.WidgetPositionAlign, Y.WidgetPositionConstrain],
        {
          initializer:function(cfg){
              
          },
          bindUI:function(){
              StatusBar.superclass.bindUI.call(this);
              this.after("attr|statusChange", this.syncStatusUI, this);
          },
          syncUI:function(){
              StatusBar.superclass.syncUI.call(this);
              this.syncStatusUI();
          },
          syncStatusUI:function(){
              var status = this.get('status');
              status != null && this.get("contentBox").setHTML(status);
          },
          showLoading:function(){
              this.set('status', res.LOADING);
          },
          hide:function(){
              this.set('visible', false);
          }
        },{
            ATTRS:{
                status:{
                    value:null
                }
            }
        });
    StatusBar.CSS_PREFIX = "lj-statusbar";
    StatusBar.HTML_PARSER = {
        
    };
    
    var lj = Y.namespace("lj");
    
    var statusBar = new StatusBar({srcNode:"#status-bar"});
    lj.statusBar = statusBar;
}, "1.0.0",{
requires:['base','overlay','intl']});
