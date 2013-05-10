/* reference to config class in YUI api */

YUI_config = {
    debug: true,
    filter:"raw",
    groups:{
        WoodenAxe:{
            base:'js/',
            modules: {
                
                "lj-basic": {
                    path: 'lj-basic/lj-basic.js?'+urlToken
                    //,lang:[]
                },
                "lj-init":{
                    path: 'lj-init/lj-init.js?'+urlToken
                    //lang: ["zh"]
                },
                "woodenaxe-main":{
                    path: 'woodenaxe-main/woodenaxe-main.js?'+urlToken
                    //lang: ["zh"]
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

var globalY, lang = {}, LANG = lang;

YUI().use('lj-init','console', function(Y){
  try{
      //new Y.Console().render();
    Y.log("main start "+ browser_locale);
    Y.lj.statusBar.showLoading();
    Y.lj.statusBar.render();
    globalY = Y;
    
    
  }catch(e){
      Y.log(e.stack);
      throw e;
  }
  
    // lazy loading starts ...
    setTimeout(function(){
    Y.use('woodenaxe-main',initljBasic)}, 10);
});


function initljBasic() {
try{
    
    
    
}catch(e){
    Y.log("erro: "+ e.stack);
    throw e;
}
}

