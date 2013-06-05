/* reference to config class in YUI api */

YUI_config = {
    debug: true,
    filter:"raw",
    groups:{
        WoodenAxe:{
            base:'js'+ urlToken + '/',
            modules: {
                
                "lj-basic": {
                    path: 'lj-basic/lj-basic.js'
                    ,lang:['zh']
                },
                "lj-init":{
                    path: 'lj-init/lj-init.js',
                    lang: ["zh"]
                },
                "woodenaxe-main":{
                    path: 'woodenaxe-main/woodenaxe-main.js',
                    lang: ["zh"]
                },
                "lj-home":{
                    path: 'lj-home/lj-home.js',
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
                },
                jquery:{
                    async:false,
                    path:"jquery-1.10.0.js"
                }
            }
        }
        //,WoodenAxeCss:{
        //    base:'css/',
        //    modules:{
        //        "lj-basic-css":{
        //            path:'lj-basic.css?'+urlToken,
        //            type:'css'/* ,
        //            async:false */
        //        },
        //        "lj-home-css":{
        //            path:'lj-home.css?'+urlToken,
        //            type:'css'
        //        }
        //    }
        //}
    }
    
};

var globalY;

YUI({lang:browser_locale}).use('lj-init','intl','transition', function(Y){
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
    
    if (Y.one(window).get('winWidth') < 500) {
        Y.SMALL_SCREEN = true;
    }
  }catch(e){
      Y.log(e.stack);
      throw e;
  }
  Y.Get.css(['/css/lj-home.css?'+urlToken, '/css/lj-basic.css?'+urlToken], function (err) {
    if (err) {
        Y.log('Error loading CSS: ' + err[0].error, 'error');
        return;
    }
    Y.use('lj-home', initHome);
  })
  
  var cfg = Y.merge({require:['lj-home']}, YUI_config);
  var s = new Y.Loader(cfg).resolve(true);
  Y.log(s);
    // lazy loading starts ...
    //setTimeout(function(){
    //
    //    Y.use('lj-home', initHome);
    //    //Y.use('woodenaxe-main',initWoo)
    //}, 10);
});

function initHome(){
    var Y = globalY, ua = Y.UA;
    //Y.lj.hideLoading();
    
    if((ua.ie>0 && ua.ie<10) /* ||
        (ua.webkit >0 && ua.webkit <= 534) */){
        /**@property lj.OLD_FASION_BROWSER */
        Y.lj.OLD_FASION_BROWSER = true;
        var b = Y.one('body').append('<div class="leftBackbg"><div class="rightBackbg"></div></div>');
        var container = b.one('.leftBackbg > .rightBackbg');
    }else{
        var container = Y.one('body');
    }
    var homeApp = new Y.lj.HomeApp({container:container});
    homeApp.once('loaded', function(){
            Y.lj.hideLoading();
    });;
    homeApp.render().navigate('/');
    
    
    //Y.log(Y.JSON.stringify(Y.UA));
}

function initWoo() {
try{
    var Y = globalY;
    var woo = new Y.lj.WoodenaxeView({container:'body'});
    woo.on('loaded', Y.lj.hideLoading);
    woo.render();
    
    
}catch(e){
    Y.log("erro: "+ e.stack);
    throw e;
}
}

