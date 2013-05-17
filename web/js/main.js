/* reference to config class in YUI api */

YUI_config = {
    debug: true,
    filter:"raw",
    groups:{
        WoodenAxe:{
            base:'js'+ urlToken + '/',
            modules: {
                
                "lj-basic": {
                    path: 'lj-basic/lj-basic.js?'+urlToken
                    ,lang:['zh']
                },
                "lj-init":{
                    path: 'lj-init/lj-init.js?'+urlToken,
                    lang: ["zh"]
                },
                "woodenaxe-main":{
                    path: 'woodenaxe-main/woodenaxe-main.js?'+urlToken,
                    lang: ["zh"]
                },
                
                'dwr-projects':{
                    async: false,
                    fullpath:'/dwr/interface/ProjectController.js?'+urlToken
                    //,requires:['dwr']
                },
                'dwr-filescan':{
                    async: false,
                    fullpath:'/dwr/interface/FileScanController.js?'+urlToken
                    //,requires:['dwr']
                }
            }
        },
        WoodenAxeCss:{
            base:'css/',
            modules:{
                "lj-basic-css":{
                    path:'lj-basic.css?'+urlToken,
                    type:'css',
                    async:false
                }
            }
        }
    }
    
};

var globalY;

YUI({lang:browser_locale}).use('lj-init','intl','transition','console', function(Y){
  try{
      var lj = Y.namespace("lj");
      var res = Y.Intl.get("lj-init"),
      loading = Y.one('#loading-mask');
      loading.one('div').setHTML(res.LOADING);
      Y.log("main start "+ browser_locale+ ", "+res.LOADING);
      Y.lj.hideLoading = function(){
        loading.hide({duration: 0.75});
    }
    globalY = Y;
    
    
  }catch(e){
      Y.log(e.stack);
      throw e;
  }
  
    // lazy loading starts ...
    setTimeout(function(){
    //Y.use('woodenaxe-main',initWoo)
    Y.use('app','json', initHome);
    }, 10);
});

function initHome(){
    var Y = globalY;
    Y.lj.hideLoading();
    
    if(Y.UA.webkit >0 && Y.UA.webkit <= 534){
        
        Y.one('body').append('<div class="leftBackbg"></div>');
        
    }
    var MainApp = new Y.App();
    Y.log(Y.JSON.stringify(Y.UA.webkit));
}

function initWoo() {
try{
    var Y = globalY;
    var woo = new Y.lj.WoodenaxeView({container:'body'});
    woo.render();
    
    
}catch(e){
    Y.log("erro: "+ e.stack);
    throw e;
}
}

