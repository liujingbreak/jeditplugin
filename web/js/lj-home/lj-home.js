YUI.add("lj-home", function(Y){
    var lj = Y.namespace("lj");
    var res = Y.Intl.get("lj-home"),
    ie = Y.UA.ie,
    HeaderView = Y.Base.create('headerView', Y.View, [], {

        render: function () {
            var container = this.get('container'), view = this;
            
            var logo = Y.Node.create('<div class="inline-block header-logo"></div>'),
                center = Y.Node.create('<div class="inline-block header-center"></div>'),
                right = Y.Node.create('<div class="inline-block header-right"></div>');
            this.renderLogo(logo);
            this.renderRight(right);
            //container.append('<div class="inline-block placeHolder">o</div>');
            right.append();
            
            container.append(logo);
            container.append(center);
            container.append(right);
            
            return this;
        },
        
        renderLogo:function(node){
            node.append('<div class="logotext inline-block">'+ res.LOGO );
        },
        
        renderRight:function(node){
            var view = this;
            node.append('<button>'+ res.SIGNIN +'</button>');
            //node.one('button').on('touchstart', function(){});
            this.signinBtn = new Y.Button({
                    srcNode:node.one('button'),
                    on:{
                        click: function(){
                            view.fire('login');
                        },
                        mousedown:function(){
                            this.get('contentBox').addClass('mousedown');
                        },
                        
                        mouseleave:function(){
                            this.get('contentBox').removeClass('mousedown').removeClass('hover-ie8');
                        },
                        mouseup:function(){
                            this.get('contentBox').removeClass('mousedown');
                        },
                        mouseenter:function(){
                            var cb = this.get('contentBox');
                            if(ie>0 && ie <9)
                                cb.addClass('hover-ie8');
                            
                        },
                        touchstart:function(e){
                            this.get('contentBox').addClass('mousedown');
                        },
                        touchcancel:function(){
                            this.get('contentBox').removeClass('mousedown');
                        },
                        touchmove:function(){
                            this.get('contentBox').removeClass('mousedown');
                        },
                        touchend:function(){
                            this.get('contentBox').removeClass('mousedown');
                        }
                    },
                    disabled: false
            });
            this.signinBtn.render(node);
            this.signinBtn.get("contentBox").addClass("button-hl");
        },
        onResize:function(){
            
        },
        destructor:function(){
            
        }
    });
    var HomeContentView = Y.Base.create('wooView', Y.View, [], {

        render: function () {
            
            var container = this.get('container'), view = this;
            container.append('.');
        },
        /** Invoked by lj.App */
        onResize:function(){
            
        },
        destructor:function(){
        }
    });
    /** @class LoginView*/
    var LoginView = Y.Base.create('loginView', Y.View, [], {
            render:function(){
                var c = Y.one(document.createDocumentFragment());
                
                var p = new Y.MyPortal({title:res.LOGIN, buttons:[
                    {   value:res.BT_LOGIN, 
                        action:function(){
                            this.getButton(0).set("disabled",true);
                        }
                    },
                    {   value:res.BT_CANCEL,
                        action:function(){
                            history.back();
                        }
                    }
                ]});
                p.render(c);
                var pn = p.getStdModNode(Y.WidgetStdMod.BODY, true);
                var username = new Y.MyTextField({label:res.USER_NAME, input:'',
                    labelWidth:'10em'});
                var password = new Y.MyTextField({label:res.USER_PASSWD, input:'', 
                    password:true, labelWidth:'10em'});
                username.render(pn);
                password.render(pn);
                //Y.log(lj.buttonBar);
                var bar = new lj.buttonBar(
                    {srcNode: p.get("contentBox").one(".yui3-widget-buttons")});
                bar.render(p.getStdModNode(Y.WidgetStdMod.FOOTER, false));
                
                var signUpNode = Y.Node.create('<div class="signupLayer">' + res.IS_NEW_USER + '  <button>'+res.SIGN_UP+'</button></div>');
                c.append(signUpNode);
                signupBtn = new Y.Button(
                    {srcNode: signUpNode.one("button")});
                signupBtn.render(signUpNode, false);
                
                this.get('container').append(c);
                Y.log('login rendered');
                return this;
            },
            
            renderSignup:function(){
                
            }
    });
    /** @class HomeApp */
    lj.HomeApp = Y.Base.create('homeApp', lj.App,[],{
        init:function(cfg){
            Y.log('HomeApp.init()');
            if(cfg == null)
                cfg = {};
            if(cfg.serverRouting === undefined)
                cfg.serverRouting = false;
            cfg.transitions = true;
            lj.HomeApp.superclass.init.call(this, cfg);
            
        },
        initializer:function(){
            Y.log('HomeApp.initializer()');
            this.route('/', function(){
                    Y.log('HomeApp.initializer() show home');
                    this.showView('home', null,null,function(){
                            Y.log('HomeApp.initializer() loaded');
                            this.fire('loaded');
                    });
            });
            this.route('/login', function(){
                    if(this.get('activeView') instanceof LoginView)
                        return;
                    Y.log('HomeApp.initializer() show login');
                    this.showView('login',  null,null,function(){
                            this.fire('loaded');
                    });
            });
        },
        views:{
            home:{type:HomeContentView, preserve: false},
            login:{type:LoginView, preserve: false, parent:'home'}
        },
        render:function(){
            var headerNode = Y.Node.create('<div></div>').addClass('home-header');
            
            var header = new HeaderView({container:headerNode});
            
            header.after('login', function(){
                this.navigate('/login');}, this);
            header.render();
            this.get('container').append(headerNode).append('<div class="header-seperate"></div>');
            lj.HomeApp.superclass.render.apply(this, arguments);
            this.get('viewContainer').addClass('home-view-c');
            return this;
        }
    });
    
    
}, "1.0.0",{
requires:['app','json','button', 'lj-basic']});
