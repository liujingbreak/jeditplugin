/* reference to config class in YUI api */
YUI_config = {
    debug: true
    //filter:"raw"
};
YUI().use('lj-basic','panel','json-stringify','tabview','button-group', function (Y) {
try{
    
    dwr.engine.setErrorHandler(function(errorString, exception){
            Y.log(errorString);
            Y.log(JSON.stringify(exception));
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
                                //if(data.length > 0){
                                model.loadRows(data, res.more, res.total);
                                //}
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
                 ProjectController.deleteProjects(ids, {
                         callback:function(res){
                            try{
                                model.fireDeleted();
                            }catch(e){
                                Y.log(e.stack);
                                throw e;
                            }
                        }
                 });
             }
    });
    var projects = new Y.MyEditableGrid({height:250});
    projects.setModel(projectsModel);
    projects.on("itemSelected", 
        function(e){
            var rowModel = projectsModel.getRowByKey(e.data);
            prjPortal.setTitle("Project " + rowModel.name);
            prjPortal.model.setData(rowModel);
            prjPortal.set("visible",true);
            prjPortal.focus();
            var savedHandle = 
            prjPortal.model.on('saved', function(){
                    this.refresh();
                    savedHandle.detach();
            }, projects);
        });
    
    
    
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
                    },
                    errorHandler:function(message){
                        alert(message);
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
            }
    });
    directories.setModel(dirModel);
    
    var main = new Y.VerBox({
            //children:[ directories]
            children:[projects, directories]
    });
    
    main.render(Y.one("#leftSection"));
    main.set("width", 350);
    
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
                this.get("boundingBox").setStyle("position", "static");
                
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
                            portal.findText, null, findOpt, {offset:offset, limit:limit},
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
                        maxHeight: 200,
                        popmenu:[
                            {text:"Open in JEdit", disabled:true }
                        ],
                buttonVisible:false});
                this.foundFilesGrid.setModel(this.foundFilesModel);
                this.foundFilesSelHandle = this.foundFilesGrid.after("selectChange",
                    function(){
                        this.menuItems[0].set("disabled", false);
                    }, this.foundFilesGrid);
            },
            
            renderLayout:function(){
                
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
                
                this.findField = new Y.MyTextField({label:'Find'});
                searchGroup.add(this.findField);
                this._enterKeyHandle = this.findField.onEnterKey(function(e){
                        this._search(e.text);
                        this.foundFilesGrid.refresh();
                    }, this);
                
                var n = Y.Node.create('<div><img src="../img/nycli1.gif" style="display:none"></div>');
                ct.append(n);
                this.loadingInd = n.one('img');
                searchGroup.add(this.optionsBtn);
                
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
            _search:function(text){
                if(this.model.id == null){
                    alert("Please select a project first.");
                    return;
                }
                //this.loadingInd.setStyle("display","inline");
                this.findText = text;
                
            },
            destructor:function(){
                this.modelHandle.detach();
                this.savedHandle.detach();
                this._enterKeyHandle.detach();
                this.visibleHandle.detech();
                this.foundFilesSelHandle.detach();
            }
    });
    _ProjectPortal.CSS_PREFIX = _ProjectPortal.superclass.constructor.CSS_PREFIX;
    var ProjectPortal = function(){
        var portal = this;
        config = {
        width   : 400,
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
    var rightSection = Y.one("#rightSection");
    prjPortal.set("visible", false);
    prjPortal.render(rightSection);
    
    

    
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
    var dirAdd = new DirectoryAddPortal({width:400, buttons: ['close']});
    dirAdd.render(rightSection);
    /** @class ProjectAaddPortal
    */
    var ProjectAddPortal = Y.Base.create("prjaddportal",Y.MyPortal, [Y.WidgetParent], {
         initializer:function(){
             //ProjectAddPortal.superclass.initializer.apply(this, arguments);
             this.after('render', this.renderLayout, this);
             this.setTitle("Add Project");
         },
         
         renderLayout:function(){
             this.get("boundingBox").setStyle("position", "relative");
             var ct = this.getStdModNode(Y.WidgetStdMod.BODY, true);
             this.setChildrenContainer(ct);
             this.vBox = new Y.VerBox();
             this.add(this.vBox);
             this.namefield = new Y.MyTextField({label:'Project Name', required:true});
             this.vBox.add(this.namefield);
             
             this.descfield = new Y.MyTextField({label:'Description'});
             this.vBox.add(this.descfield);
         }
    });
    ProjectAddPortal.CSS_PREFIX = ProjectAddPortal.superclass.constructor.CSS_PREFIX;
    var prjAdd = new ProjectAddPortal({width:400, visible: false, buttons: ['close',
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
                    errorHandler:function(m){ alert(m);}
                }
                );
        },
        section: Y.WidgetStdMod.FOOTER
    }]});
    prjAdd.render(rightSection);
    
    projects.on('add', function(){
        prjAdd.set('visible', true);
        prjAdd.focus();
    });
    
    
}catch(e){
    Y.log(e.stack);
    throw e;
}
});