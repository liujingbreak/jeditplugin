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
    /**@class HomeContentView*/
    var HomeContentView = Y.Base.create('wooView', Y.View, [], {

        render: function () {
            
            var container = this.get('container'), view = this;
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
                var c = Y.one(document.createDocumentFragment()),
                    view = this;
                
                var p = new Y.MyPortal({title:res.LOGIN, buttons:[
                    {   value:res.BT_LOGIN, 
                        action:function(){
                            this.getButton(0).set("disabled",true);
                        }
                    },
                    {   value:res.BT_CANCEL,
                        action:function(){
                            view.app.navigate("/");
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
                    {
                        srcNode: signUpNode.one("button"),
                        on:{
                            click: function(){
                                view.fire("goSignUp", {src:"ui"});
                                }
                        }
                    });
                //signupBtn.on('click', function(){
                //        this.fire("goSignUp", {src:"ui"});
                //});
                signupBtn.render(signUpNode, false);
                var layout = Y.Node.create('<div class="home-view-layout"></div>');
                layout.append(c);
                this.get('container').append(layout);
                Y.log('login rendered');
                return this;
            }
    });
    
    /** @class SignUpView */
    var SignUpView = Y.Base.create('loginView', Y.View, [], {
            render:function(){
                var c = Y.Node.create('<div class="home-view-layout"></div>'),
                    view = this;
                var p = this.portal = new Y.MyPortal({title:res.SIGNUP, buttons:[
                    {   value:res.BT_SIGNUP, 
                        //section:"header",
                        action:function(){
                            this.getButton(0).set("disabled",true);
                        }
                    },
                    {   value:res.BT_CANCEL,
                        //section:"header",
                        action:function(){
                            view.app.navigate("/");
                        }
                    }
                ]});
                p.render(c);
                var body = p.getStdModNode(Y.WidgetStdMod.BODY, true),
                    pn = Y.Node.create('<div></div>');
                //pn = body;
                
                var email = new Y.MyTextField({label:res.EMAIL, input:'',
                    labelWidth:'10em'});
                var username = new Y.MyTextField({label:res.USERNAME, input:'',
                    labelWidth:'10em'});
                var password = new Y.MyTextField({label:res.USER_PASSWD, input:'', 
                    password:true, labelWidth:'10em', alt:res.PASSWORD_ALT});
                var passwordRep = new Y.MyTextField({label:res.USER_PASSWD_CONFIRM, input:'', 
                    password:true, labelWidth:'10em'});
                var nickName = new Y.MyTextField({label:res.VIEW_NAME, input:'', 
                     labelWidth:'10em'});
                var intro = new Y.MyTextField({label:res.SELF_INTRODUCT, input:'', 
                    password:true, labelWidth:'10em'});
                email.render(pn);
                username.render(pn);
                password.render(pn);
                passwordRep.render(pn);
                nickName.render(pn);
                this.scrollView = new Y.lj.MyScrollView({srcNode:pn});
                //this.scrollView._prevent={
                //    start: false,
                //    move: false,
                //    end: false
                //};
                this.scrollView.render(body);
                
                p.get("boundingBox").addClass("expand-height");
                this.get('container').append(c);
            },
            onResize:function(){
               // if(!this._layouted){
                var p = this.portal;
                this._layouted = true;
                var hf = p.getStdModNode("footer", false).get("offsetHeight"),
                    hh = p.getStdModNode("header", false).get("offsetHeight"),
                    body = p.getStdModNode("body", false),
                    content = body.ancestor(),
                    ch = lj.parseStyleLen(content.getComputedStyle("height")),
                    
                    padding = lj.parseStyleLen(body.getComputedStyle("paddingTop")) + 
                        lj.parseStyleLen(body.getComputedStyle("paddingBottom"));
                var bodyHeight = ch - padding - hf - hh;
                this.scrollView.set('height', bodyHeight);
                this.scrollView.refresh();
                body.setStyle("height",bodyHeight + "px");
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
            var app = this;
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
                    this.showView('login',  null,null,function(view){
                             view.on('goSignUp',
                                 function(){
                                     app.navigate("/signup");
                                 });
                            this.fire('loaded');
                    });
            });
            this.route('/signup', function(){
                    if(this.get('activeView') instanceof SignUpView)
                        return;
                    this.showView('signup',  null,null,function(){
                           
                            this.fire('loaded');
                    });
                    
            });
        },
        views:{
            home:{type:HomeContentView, preserve: false},
            login:{type:LoginView, preserve: false, parent:'home'},
            signup:{type:SignUpView, preserve:false, parent:'login'}
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
