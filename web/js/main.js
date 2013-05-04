/* reference to config class in YUI api */
YUI_config = {
    debug: true,
    filter:"raw",
    groups:{
        WoodenAxe:{
            base:'js/',
            modules: {
                
                "lj-basic": {
                    path: 'lj-basic/lj-basic.js'
                    ,lang:[]
                },
                "lj-init":{
                    path: 'lj-init/lj-init.js',
                    lang: ["zh"]
                },
                
                'dwr-projects':{
                    async: false,
                    fullpath:'/dwr/interface/ProjectController.js'
                    //,requires:['dwr']
                },
                'dwr-filescan':{
                    async: false,
                    fullpath:'/dwr/interface/FileScanController.js'
                    //,requires:['dwr']
                }
            }
        },
        WoodenAxeCss:{
            base:'css/',
            modules:{
                "lj-basic-css":{
                    path:'lj-basic.css',
                    type:'css',
                    async:false
                }
            }
        }
    }
    
};

var globalY;

YUI({lang:'zh'}).use('lj-init','console', function(Y){
  try{
      //new Y.Console().render();
    Y.log("main start");
    Y.lj.statusBar.showLoading();
    Y.lj.statusBar.render();
    globalY = Y;
  }catch(e){
      Y.log(e.stack);
      throw e;
  }
    // lazy loading starts ...
    setTimeout(function(){
    Y.use('dwr-filescan','dwr-projects','lj-basic','json-stringify',initljBasic)}, 10);
});


function initljBasic() {
try{
    var Y = globalY, lj = Y.lj;
    lj.statusBar.render();
    
    //Y.Get.js(['/dwr/engine.js',
    //    '/dwr/interface/ProjectController.js',
    //'/dwr/interface/FileScanController.js'],function(err){
    //    
    //});

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
            columns:["Project Name", "Description"],
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
                 commonDialog.show("Are you sure to delete project?");
                 
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
            columns:["Folder","Includes","Excludes"],
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
                var options = new Y.MyButtonGroup({type: 'radio'});
                options.addButton("Matches","0");
                options.addButton("Starts with", "1");
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
    prjPortal.render("#project");
    

    
    /** @class DirectoryAddPortal
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
            leftApp.save("/add");
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
                        container:"#leftSection",
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
    var AddProjectView = Y.Base.create("addProjectView", Y.View, [],{
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
    /** @class leftApp */
    var leftApp = new Y.lj.App({
            viewContainer:"#leftSection",
            serverRouting:false,
            transitions:true,
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
                    document.title = "Wooden Axe Tool"
                    this.showView("listView", null, null, 
                        function(){
                            if(firstLoad){
                                firstLoad = false;
                                lj.deferredTasks.add(function(){
                                        lj.statusBar.hide();
                                }, 50).run();
                            }
                        });
                });
    
    leftApp.route('/add', function (){
                    document.title = "Wooden Axe Tool - Add Project"
                    this.showView("addProjectView");
                });
    
    var firstLoad = true, leftSectNode = Y.one('#leftSection');
    
    var splitBar0 = new Y.lj.SplitBar({
            node:'#splitbar0', 
            container:leftSectNode.ancestor(),
            direction:'h'
    });
    splitBar0.after('valueChange', function(e){
        this.reset();
        leftSectNode.setStyle('width', e.newVal+'px');
        leftApp.resize();
    }, splitBar0);
    
    
    leftApp.render().save("/");

    
    
}catch(e){
    Y.log("erro: "+ e.stack);
    throw e;
}
}

