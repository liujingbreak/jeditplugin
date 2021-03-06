YUI.add("woodenaxe-main", function(Y){
  try{
      var lj = Y.lj, fullWindow = false;
      
      function initWoo(wooContainer){
        
        var res = Y.Intl.get("woodenaxe-main");
        Y.log("lang:"+ Y.Intl.lookupBestLang(browser_locale, Y.Intl.getAvailableLangs("woodenaxe-main")));
        
        var firstLoad = true, leftSectNode = Y.Node.create('<div class="leftSection inline-block"></div>'),
        splitBarNode = Y.Node.create('<div></div>'),
        centerSection = Y.Node.create('<div class="centerSection"></div>');
        var frag = Y.one(document.createDocumentFragment());
        //frag = Y.one(document.body);
        frag.append(leftSectNode);
        frag.append(splitBarNode);
        frag.append(centerSection);
        
        //return frag;
    
        dwr.engine.setErrorHandler(function(errorString, exception){
                Y.log("DWR engine error: "+ errorString);
                Y.log("DWR exception: "+ JSON.stringify(exception));
        });
        dwr.engine.setWarningHandler(function(errorString, exception){
                Y.log("DWR engine warning: "+ errorString);
                Y.log("DWR exception: "+ JSON.stringify(exception));
        });
        // IE9 has a problem with pressing enter key leads to first button on the page 
        // get pressed issue
        Y.one(document.body).on('keypress',
            function(e){
               if(e.charCode == 13){
                   e.preventDefault();
                   e.stopPropagation();
               }
            });
        
        function routeCallBack(req){
            Y.log("req: "+ Y.JSON.stringify(req));
        };
        
        
        var commonDialog = new Y.lj.Dialog({});
        commonDialog.render();
        
        var projectsModel = new Y.PagedGridModel({
                columns:[res.PROJECT_NAME, res.PROJECT_DESC],
                columnKeys:["name", "desc"],
                keyColumn:"id",
                rows:[],
                 loadHandle:function(offset, size){
                     var model = this;
                     ProjectController.list({
                        offset:offset,
                        limit:size
                        //,$dwrClassName:"PagingRequest"
                        }, {callback:function(res){
                                try{
                                    var data = res.data;
                                    model.loadRows(data, res.more, res.total);
                                    
                                }catch(e){
                                    Y.log(e.stack);
                                    throw e;
                                }
                            }
                        }); 
                 },
                 deleteHandle:function(keyset){
                     var ids = [];
                     for(id in keyset)
                         ids.push(id);
                     var model = this;
                     commonDialog.show("Are you sure to delete project?",
                         function(){
                             ProjectController.deleteProjects(ids,
                                 {
                                     callback:function(res){
                                         projects.refresh();
                                     }
                                 });
                             this.hide();
                         });
                     
                 }
        });
        var projects = new Y.MyEditableGrid({width:'100%'});
        projects.setModel(projectsModel);
        projects.on("itemSelected", 
            function(e){
                var rowModel = projectsModel.getRowByKey(e.data);
                
            });
        //projects.render("#leftSection");
        
        
        var dirModel = new Y.PagedGridModel({
                columns:[res.FOLER, res.INCLUDES, res.EXCLUDES],
                keyColumn:"id",
                rows:[],
                loadHandle:function(offset, size){
                    var model = this;
                    FileScanController.dirRootFolders({
                    offset:offset,
                    limit:size
                    }, {callback:function(res){
                            try{
                                var data = res.data;
                                //if(data.length > 0){
                                    //Y.log(Y.JSON.stringify(res));
                                model.loadRows(data, res.more, res.total);
                                //}
                            }catch(e){
                                Y.log(e.stack);
                                throw e;
                            }
                        }
                    }); 
                }
        });
        var directories = new Y.MyEditableGrid({
                cellView:function(rowModel, colIdx){
                    if(colIdx == 0)
                        return rowModel.path;
                    else if(colIdx == 1)
                        return rowModel.includes.join(", ");
                    else if(colIdx == 2)
                        return rowModel.excludes.join(", ");
                },
                
                width:'100%'
        });
        directories.setModel(dirModel);
        //directories.render("#leftSection");
        
        
        
        var ProjectModel = function (){
            
        }
        ProjectModel.prototype = {
            fireChange:function(){
                this.fire('change');
            },
            setData:function(prjDAO){
                this.name = prjDAO.name;
                this.desc = prjDAO.desc;
                this.id = prjDAO.id;
                this.fireChange();
            },
            load:function(id){
                
            },
            save:function(){
                var model = this;
                ProjectController.updateProject(
                    {
                        id:model.id,
                        name: model.name, 
                        desc: model.desc
                    },
                    {   callback:function(res){
                            model.fire('saved');
                        }
                    }
                );
            }
        }
        Y.augment(ProjectModel, Y.EventTarget);
        
        /**@class ProjectPortal */
        var _ProjectPortal = Y.Base.create("projportal",Y.MyPortal, [Y.WidgetParent], {
                
                initializer:function(){
                    this.model = new ProjectModel();
                    
                    
                    this.after('render', function(){ 
                        this.renderLayout();
                        this.modelHandle = this.model.on('change', this._syncModelUI, this);
                        this.savedHandle = this.model.on('saved', function(e){
                            //projects.refresh();
                            this.getButton(0).set('disabled', false);
                        }, this);
                        this._syncModelUI();
                        this.visibleHandle = this.after('visibleChange', function(e){
                                this.vBox.reLayout();
                        }, this);
                    }, this);
                    
                    var portal = this;
                    this.foundFilesModel = new Y.PagedGridModel({
                        columns:['File Name'],
                        columnKeys: [0],
                        keyColumn:5,
                        groupBy: [4, 3],
                        groupCont: [2, 1],
                        loadHandle: function(offset, limit){
                            var findOpt = parseInt(portal.optionsBtn.getSelectedButtons()[0].getAttribute("my-data"), 10);
                            if(portal.findText == null){
                                this.loadRows([], false);
                                return;
                            }
                            var model = this;
                            ProjectController.findSrcFilePlain(portal.model.id,
                                portal.findText, portal.findSuffix, findOpt, {offset:offset, limit:limit},
                                {callback:function(res){
                                    try{
                                        var data = res.listData;
                                        model.loadRows(res.listData, res.more, res.total);
                                        portal.loadingInd.setStyle("display","none");
                                        //Y.log(Y.JSON.stringify(res, null,'  '));
                                    }catch(e){
                                        Y.log(e.stack);
                                        throw e;
                                    }
                                    }
                                }); 
                        }
                    });
                    
                    this.foundFilesGrid = new Y.MyEditableGrid({
                            height: 400,
                            popmenu:[
                                {text:"Open in JEdit", disabled:true, action: function(e){portal._openJEdit();} }
                            ],
                    buttonVisible:false});
                    this.foundFilesGrid.setModel(this.foundFilesModel);
                    this.foundFilesSelHandle = this.foundFilesGrid.after("itemSelected",
                        function(e){
                            
                            this.menuItems[0].set("disabled", e.data == null || e.data == "");
                        }, this.foundFilesGrid);
                },
                _openJEdit:function(){
                    for(var key in this.foundFilesGrid.selectionModel.keyset){
                        Y.log('open '+ this.foundFilesGrid.model.getRowByKey(key)[0]);
                        FileScanController.openFileInJEdit(parseInt(key, 10), 
                            {callback:function(){
                                Y.log('opened ');
                            }});
                    }
                },
                renderLayout:function(){
                    this.get("boundingBox").setStyle("position", "static");
                    var ct = this.getStdModNode(Y.WidgetStdMod.BODY, true);
                    this.setChildrenContainer(ct);
                    this.vBox = new Y.VerBox();
                    this.add(this.vBox);
                    this.namefield = new Y.MyTextField({label:'Project Name', required:true});
                    this.vBox.add(this.namefield);
                    
                    this.descfield = new Y.MyTextField({label:'Description'});
                    this.vBox.add(this.descfield);
                    
                    
                 
                    var searchGroup = new Y.VerBox({enableBorder: true});
                    var options = new Y.MyButtonGroup({type: 'radio', 
                    buttons:[
                        {name:'Matches', actionCmd:'0'},
                        {name:'Starts with', actionCmd:'1'}
                    ]});
                    options.selectButton("1");
                    this.optionsBtn = options; 
                    
                    this.findField = new Y.MySearchField({label:'File Name'});
                    searchGroup.add(this.findField);
                    this._enterKeyHandle = this.findField.on("search", function(e){
                            this.suffixField.syncInput();
                            this._search(e.text, this.suffixField.get("input"));
                            this.foundFilesGrid.refresh();
                        }, this);
                    
                    var n = Y.Node.create('<div><img src="../img/nycli1.gif" style="display:none"></div>');
                    ct.append(n);
                    this.loadingInd = n.one('img');
                    searchGroup.add(this.optionsBtn);
                    
                    this.suffixField = new Y.MySearchField({label:'File Suffix'});
                    searchGroup.add(this.suffixField);
                    this._enterKeyHandle2 = this.suffixField.on("search", function(e){
                            this.findField.syncInput();
                            this._search(this.findField.get("input"), e.text);
                            this.foundFilesGrid.refresh();
                        }, this);
                    
                    this.vBox.add(searchGroup);
                    this.vBox.add(this.foundFilesGrid);
                    
                },
                _syncModelUI:function(){
                    this.namefield.set('input', this.model.name);
                    this.descfield.set('input', this.model.desc);
                },
                syncModel:function(){
                    this.namefield.syncInput();      
                    this.descfield.syncInput();
                    this.model.name = this.namefield.get('input');
                    this.model.desc = this.descfield.get('input');
                },
                _search:function(text, suffix){
                    Y.log("_search() "+ text + ", " + suffix);
                    if(this.model.id == null){
                        alert("Please select a project first.");
                        return;
                    }
                    //this.loadingInd.setStyle("display","inline");
                    this.findText = text;
                    this.findSuffix = suffix;
                },
                destructor:function(){
                    this.modelHandle.detach();
                    this.savedHandle.detach();
                    this._enterKeyHandle.detach();
                    this.visibleHandle.detech();
                    this.foundFilesSelHandle.detach();
                    this._enterKeyHandle2.detach();
                    this.changeHandler1.detach();
                }
        });
        _ProjectPortal.CSS_PREFIX = _ProjectPortal.superclass.constructor.CSS_PREFIX;
        var ProjectPortal = function(){
            var portal = this;
            config = { 
            buttons: [
                'close',
                {
                value:'Update',
                    action:function(e){
                        this.set('disabled', true);
                        portal.syncModel();
                        var button = this;
                        portal.model.save();
                    },
                    section: Y.WidgetStdMod.FOOTER
                }
            ]
            };
            _ProjectPortal.call(this, config);
        }
        ProjectPortal.prototype = _ProjectPortal.prototype;
        
        
        var prjPortal = new ProjectPortal();
        //var rightSection = Y.one("#rightSection");
        prjPortal.set("visible", false);
        //prjPortal.render(rightSection);
        //prjPortal.render("#project");
        
    
        
        /** @class Directory'deleteAddPortal
        */
        var DirectoryAddPortal = Y.Base.create("diraddportal",Y.MyPortal, [Y.WidgetParent], {
             initializer:function(){
                 //DirectoryAddPortal.superclass.initializer.apply(this, arguments);
                 this.after('render', this.renderLayout, this);
                 this.setTitle("Add Directory");
             },
             
             renderLayout:function(){
                 this.get("boundingBox").setStyle("position", "static");
                 var ct = this.getStdModNode(Y.WidgetStdMod.BODY, true);
                 this.setChildrenContainer(ct);
                 ct.append('<div>Path:</div>');
                 var nameInput = Y.Node.create('<input type="text" size="30">');
                 var descInput = Y.Node.create('<input type="text" size="30">');
                 ct.append(nameInput);
                 ct.append('Descrption:');
                 ct.append(descInput);
             }
        });
        DirectoryAddPortal.CSS_PREFIX = DirectoryAddPortal.superclass.constructor.CSS_PREFIX;
        var dirAdd = new DirectoryAddPortal({buttons: ['close']});
        
        //dirAdd.render("#folder");
        /** @class ProjectAaddPortal
        */
        var ProjectAddPortal = Y.Base.create("prjaddportal",Y.MyPortal, [Y.WidgetParent], {
             initializer:function(){
                 this.after('render', this.renderLayout, this);
                 this.setTitle("Add Project");
             },
             
             renderLayout:function(){
                 this.get("boundingBox").setStyle("position", "static");
                 var ct = this.getStdModNode(Y.WidgetStdMod.BODY, true);
                 this.setChildrenContainer(ct);
                 this.vBox = new Y.VerBox();
                 this.add(this.vBox);
                 this.namefield = new Y.MyTextField({label:'Project Name', required:true});
                 this.vBox.add(this.namefield);
                 
                 this.descfield = new Y.MyTextField({label:'Description'});
                 this.vBox.add(this.descfield);
             },
             destructor:function(){
                 Y.log("ProjectAddPortal destroy");
             }
        });
        ProjectAddPortal.CSS_PREFIX = ProjectAddPortal.superclass.constructor.CSS_PREFIX;
        var prjAdd = null;
        
        projects.on('add', function(){
                leftApp.navigate("/add");
        });
        
        /**@class ListView */
        var ListView = Y.Base.create("listView", Y.View, [],{
                initializer:function(){
                    Y.log("init listview");
                },
                render:function(){
                    var container = this.get('container');
                    var frag = Y.one(document.createDocumentFragment());
                    projects.render(frag);
                    var dragbarNode = Y.Node.create('<div></div>');
                    frag.append(dragbarNode);
                    directories.render(frag);
                    container.append(frag);
                    /**@property dragbar */
                    this.dragbar = new Y.lj.SplitBar({
                            node:dragbarNode, 
                            container:leftSectNode,
                            direction:'v'
                    });
                    var bar = this.dragbar, view = this;
                    bar.after('listView|valueChange', function(e){
                            bar.reset();
                            view.app.resize();
                    });
                    Y.log("listview rendered");
                },
                onResize:function(p){
                    var h = Y.lj.parseStyleLen(p.getComputedStyle('height')),
                    w = Y.lj.parseStyleLen(p.getComputedStyle('width')),
                    splitH = this.dragbar.node.get('offsetHeight');
                    
                    var splitValue = this.dragbar.get('value');
                    if(splitValue < 0)
                        splitValue = (h>>1) - Math.ceil(7/2);
                        
                    var hDown = h - splitValue-splitH;
                    //Y.log(" split :"+ splitValue + "/"+ hDown+ "/"+ Y.lj.parseStyleLen(h));
                    projects.set('height', splitValue);
                    directories.set('height', hDown);
                                    
                    projects.set('width', w - 1);
                    directories.set("width", w - 1);
                }
        });
        /**@class AddProjectView */
        var AddProjectView = Y.Base.create("addprjview", Y.View, [],{
                initializer:function(){
                    Y.log("init AddProjectView");
                },
                render:function(){
                    var container = this.get('container');
                    prjAdd = new ProjectAddPortal({ buttons: [
                        {  
                            value:'Save',
                            action:function(e){
                                this.set('disabled', true);
                                prjAdd.namefield.syncInput();
                                prjAdd.descfield.syncInput();
                                var button = this;
                                ProjectController.addProject(prjAdd.namefield.get('input'), 
                                            prjAdd.descfield.get('input'),
                                            {   callback:function(res){
                                                    if(fullWindow)
                                                        history.back();
                                                    projects.refresh();
                                                    button.set('disabled', false);
                                                },
                                                errorHandler:function(m){ }
                                            }
                                            );
                           },
                           section: Y.WidgetStdMod.FOOTER
                        },
                        {
                            value:'Cancel',
                            action:function(e){
                                history.back();
                                fullWindow || prjAdd.hide('fadeOut');
                            },
                            section: Y.WidgetStdMod.FOOTER
                        }
                    ]});
                    prjAdd.render(container);
                },
                destructor:function(){
                    Y.log("destroy add project view");
                    if(prjAdd != null)
                        prjAdd.destroy();
                    prjAdd = null;
                }
                 
        });
        /**@class AboutView */
        var AboutView = Y.Base.create("AboutView", Y.View, [],{
                initializer:function(){},
                render:function(){
                    var container = this.get('container');
                    container.setHTML("By    Liu Jing");
                }
        });
        /**@class EmptyView */
        var EmptyView = Y.Base.create("emptyView", Y.View, [],{
        });
        
        /**@class CenterView */
        var CenterView = Y.Base.create("centerView", Y.View, [],{
        });
                
        /** @class leftApp */
        var leftApp = new Y.lj.App({
                viewContainer:leftSectNode,
                container:leftSectNode,
                serverRouting:false,
                transitions:true,
                root:'/woo',
                views:{
                    listView:{
                        preserve:true,
                        type:ListView
                        //,parent:"addProjectView"
                    },
                    addProjectView:{
                        type:AddProjectView,
                        preserve:true
                        ,parent:"listView"
                    },
                    aboutView:{
                        type:AboutView,
                        preserve:false,
                        parent:"listView"
                    }
                }
            });
        leftApp.route("/", function (){
                        document.title = "Wooden Axe Tool";
                        var self = this;
                        this.showView("listView", null, null, 
                            function(){
                                if(firstLoad){
                                    firstLoad = false;
                                    Y.lj.deferredTasks.add(function(){
                                            leftApp.fire('loaded');
                                    }, 50).run();
                                }
                            });
                    });
        leftApp.route('/add', function (){
            //if(centerApp.get('activeView') === centerApp._addProjView)
            //    return;
            document.title = "Wooden Axe TmainSectionool - Add Project";
            if(fullWindow)
                centerApp.showView("addProjectView", null, null, function(view){
                        centerApp._addProjView = view;
                });
            else
                centerApp.addContent("addProjectView", AddProjectView);
        });
        
        /**@class CenterApp */
        var CenterApp = Y.Base.create('centerapp', Y.lj.App,[],
            {
                initializer:function(){
                    this.contentViews = {};
                },
                addContent:function(viewName, viewContructor){
                    var container = this.get('activeView').get('container'), 
                        node = Y.Node.create('<div></div>');
                    node.hide('fadeOut',{duration:0});
                    if(!this.contentViews[viewName]){
                        var childView = new viewContructor({
                                container:node
                        }),
                            it = this.contentViews[viewName] = {
                            view: childView,
                            node: node
                        };
                        childView.render();
                        container.append(node);
                        node.show('fadeIn');
                    }
                }
            });
        
        var centerApp;
        if(fullWindow)
            centerApp = leftApp;
        else
            centerApp = new CenterApp({
                viewContainer:centerSection,
                container:centerSection,
                serverRouting:false,
                transitions:true,
                views:{
                    emptyView:{
                        type:EmptyView
                    },
                    centerView:{
                        type:CenterView,
                        parent: 'emptyView',
                        preserve:false
                    }
                }
            });
            
        leftApp.render();
        var splitBar0 = new Y.lj.SplitBar({
            node: splitBarNode, 
            container:wooContainer,
            direction:'h'
        });
        splitBar0.after('valueChange', function(e){
            this.reset();
            leftSectNode.setStyle('width', e.newVal+'px');
            leftApp.resize();
        }, splitBar0);
        
        if (Y.one(window).get('winWidth') < 500) {
            var left = leftSectNode;
            left.removeClass('leftSection').addClass('fullWindow');
            fullWindow = true;
            splitBarNode.setStyle('display','none');
            centerSection.setStyle('display','none');
        }
        
        
        
        
        if(!fullWindow)
            centerApp.render();
        
        this.centerApp = centerApp;
        this.leftApp = leftApp;
        this.splitBar0 = splitBar0;
        return frag;
    }
    
    /** @class WoodenaxeView 
        @event loaded
    */
    Y.lj.WoodenaxeView = Y.Base.create('wooView', Y.View, [], {

        render: function () {
            
            var container = this.get('container'), view = this;
            //initWoo.apply(this);
            container.append(initWoo.call(this, container));
            this.leftApp.on('loaded', this.onLoaded, this);
            lj.deferredTasks.add(function(){
                    view.leftApp.navigate("/");
                    view.centerApp.showView('emptyView');
            }).run();
        },
        onLoaded:function(e){
            this.fire('loaded');
        },
        /** Invoked by lj.App */
        onResize:function(){
            this.leftApp.resize();
        },
        destructor:function(){
            this.leftApp.destory();
            if(this.centerApp != this.leftApp)
                this.centerApp.destroy();
            this.splitBar0.destroy();
            Y.log('woodenaxe destoryed');
        }
    });
    
      }catch(e){
        Y.log(e.stack);
        throw e;
    }

    
    //loadI18n('lj-basic',['zh'], run);
}, "1.0.0",
{
requires:['intl','dwr-filescan','dwr-projects','lj-basic','json-stringify','transition']
});
