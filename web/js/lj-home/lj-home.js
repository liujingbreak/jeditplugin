YUI.add("lj-home", function(Y){
    var lj = Y.namespace("lj");
    var res = Y.Intl.get("lj-home"),
    ie = Y.UA.ie,
    isTouchEnabled = Y.UA.touchEnabled;
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
            //this.signinBtn.get("contentBox").addClass("button-hl");
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
            lj.deferredTasks.add({fn:function(){
                    Y.use("woodenaxe-main", function(){
                        view.woo = new Y.lj.WoodenaxeView({container:container});
                        view.woo.render()
                    });
            }}).run();
            
        },
        /** Invoked by lj.App */
        onResize:function(){
            if(this.woo)
                this.woo.onResize();
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
                    labelWidth:'10em',nodeAttrs:{name:'email'}});
                var password = new Y.MyTextField({label:res.USER_PASSWD, input:'', 
                    password:true, labelWidth:'10em',nodeAttrs:{name:'email'}});
                username.render(pn);
                password.render(pn);
               
                var autoLogin = new lj.MyCheckBox2({label:res.AUTO_LOGIN, labelWidth:'10em', value:true});
                autoLogin.render(pn);
                
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
                
                //this._sv = new lj.MyScrollView({srcNode:layout});
                
                this.get('container').append(layout);
                this.niceScroll = new Y.lj.MyNiceScroll({container:layout});
                this.niceScroll.render();
                //this._scl = jQuery(layout.getDOMNode()).niceScroll(layout.one('.nicescl-content').getDOMNode(), {touchbehavior:isTouchEnabled, mousescrollstep:20});
                //this._sv.render(this.get('container'));
                Y.log('login rendered');
                return this;
            },
            onResize:function(){
                //this._sv.refresh();
                //this._scl.resize();
                //this.appHeaderLayout();
                this.niceScroll.resize();
                
            },
            /* appHeaderLayout:function(){
                if(this.app._headerVisible){
                if(this.get('container').get('offsetHeight')<700)
                   this.app.hideHeader();
                }else{
                    if(this.get('container').get('offsetHeight')>=700)
                       this.app.showHeader();
                }
            } */
    });
    
    /** @class SignUpView */
    var SignUpView = Y.Base.create('loginView', Y.View, [], {
            render:function(){
                
                var view = this;
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
                p.render(this.get('container'));
                var body = p.getStdModNode(Y.WidgetStdMod.BODY, true),
                    pn = Y.Node.create('<div></div>');
                
                new lj.ButtonBarView({container:p.getStdModNode("footer", false).one('.yui3-widget-buttons')}).render();
                //pn = body;
                
                var email = new Y.MyTextField({label:res.EMAIL, input:'',
                    labelWidth:'10em', required:true, nodeAttrs:{name:'email'}});
                var username = new Y.MyTextField({label:res.USERNAME, input:'',
                    labelWidth:'10em', required:true, nodeAttrs:{name:'username'}});
                var password = new Y.MyTextField({label:res.USER_PASSWD, input:'', 
                    password:true, required:true, labelWidth:'10em', alt:res.PASSWORD_ALT, nodeAttrs:{name:'password'}});
                var passwordRep = new Y.MyTextField({label:res.USER_PASSWD_CONFIRM, input:'', 
                    password:true, labelWidth:'10em'});
                var nickName = new Y.MyTextField({label:res.VIEW_NAME, input:'', 
                     labelWidth:'10em'});
                
                
                email.render(pn);
                username.render(pn);
                password.render(pn);
                passwordRep.render(pn);
                nickName.render(pn);
                
                
                pn.append('<div class="inline-block yui3-mytextfield-label"></div>');
                pn.append('<div class="ckeditor"></div>');
                this.intro = new lj.MyEditorView({
                        name:'intro',
                        label:res.SELF_INTRODUCT,
                    container:pn.one('.ckeditor'),
                    size:{height:'7em'}
                });
                
                this.intro.render();
                
                body.append(pn);
                //this.scrollView = new Y.lj.MyScrollView({srcNode:pn});
                this.niceScroll = new Y.lj.MyNiceScroll({container:pn});
                this.intro.on('signup|resize', this.onResize, this);
                
                p.get("boundingBox").addClass("expand-height");
                //this.get('container').append(c);
                this.niceScroll.render();
                
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
                //this.scrollView.set('height', bodyHeight);
                //this.scrollView.refresh();
                body.setStyles({"height":bodyHeight + "px", overflow:'hidden'});
                this.niceScroll.resize();
            },
            destructor:function(){
                this.intro.destroy();
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
            /**@property _headerVisible */
            this._headerVisible = true;
        },
        initializer:function(){
            Y.log('HomeApp.initializer()');
            var app = this;
            this.route('/', function(){
                    Y.log('HomeApp.initializer() show home');
                    this.showView('home', null,null,function(view){
                            Y.log('HomeApp.initializer() loaded');
                            this.fire('loaded');
                            if(!app._headerVisible){
                               app.showHeader();
                               view.onResize && view.onResize();
                            }
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
                            app.appHeaderLayout(view);
                    });
            });
            this.route('/signup', function(){
                    if(this.get('activeView') instanceof SignUpView)
                        return;
                    this.showView('signup',  null,null,function(view){
                           this.fire('loaded');
                           app.appHeaderLayout(view);
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
            this.headerNode = headerNode;
            var header = new HeaderView({container:headerNode});
            
            header.after('login', function(){
                this.navigate('/login');}, this);
            header.render();
            this.get('container').append(headerNode).append('<div class="header-seperate"></div>');
            
            lj.HomeApp.superclass.render.apply(this, arguments);
            this.get('viewContainer').addClass('home-view-c');
            return this;
        },
        /** predicate whether show or hide home header */
        appHeaderLayout:function(view){
            //Y.log('appHeaderLayout() ' + view.get('container').get('winHeight'));
            if(this._headerVisible){
                if(Y.one(window).get('winHeight')<650){
                   this.hideHeader();
                   
                }
            }else{
                if(Y.one(window).get('winHeight')>=650){
                   this.showHeader();
                   view.onResize();
                }
            }
        },
        hideHeader:function(needResize){
            
            var header = this.headerNode, h = header.get("offsetHeight"),
                app = this;
            if(h <= 0) return;
            header.transition({
                    easing:"ease-out",
                    duration: 0.3,
                    top:- h +'px'
            }, function(){
                header.setStyle('display','none');
                app.get('viewContainer').addClass("home-view-atop");
                needResize && needResize.onResize && needResize.onResize();
            });
            this._headerVisible = false;
        },
        showHeader:function(needResize){
            var header = this.headerNode, app = this;
            header.setStyle('display','block');
            if(header.get('offsetTop') <= 0)
                header.transition({
                    easing:"ease-out",
                    duration: 0.3,
                    top:'0px'
            }, function(){
                app.get('viewContainer').removeClass("home-view-atop");
                needResize && needResize.onResize && needResize.onResize();
            });
            this._headerVisible = true;
        }
    });
    
    
}, "1.0.0",{
requires:['app','json','button', 'lj-basic','ckeditor']});
