YUI.add("lj-init", function(Y){
        
    function loadI18n(module, availableLangs, callback){
      var locale = Y.Intl.lookupBestLang(browser_locale, availableLangs),
          localeSuffix = locale ? '_' + locale : '',
          url = "/js/i18n/"+(locale ? locale + '/':'') + module + localeSuffix + '.js?'+ urlToken;
      Y.log("load res "+ url);
      Y.Get.js(url, function(err){
          if(err){
              Y.log(err);
              Y.log('failed to load '+ url);
          }
          Y.log("llll");
          callback(YUI);
      });
          
    }
    function load(){
        var res = LANG;
            
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
                  var bar = this;
                  this.get('boundingBox').hide({duration: 0.75});
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
        lj.loadI18n = loadI18n;
        var statusBar = new StatusBar({srcNode:"#status-bar"});
        lj.statusBar = statusBar;
    }
    loadI18n('lj-init', ['zh'], load);
    //load();
    
}, "1.0.0",{
requires:['base','overlay','transition','get']});
