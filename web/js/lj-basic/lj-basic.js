
YUI.add("lj-basic", function(Y){
       
  try{
     var res = Y.Intl.get("lj-basic");
      
    function loadHandler(err){
        if (err) {
            Y.Array.each(err, function (error) {
                Y.log('Error loading file: ' + error.error, 'error');
            });
            return;
        }
        Y.log('All files loaded successfully!');
    }
    //Y.Get.css('css/lj-basic.css', {async:false},loadHandler);
    //Y.log("lj-basic css loaded");
    function parseStyleLen(styleLength){
        //Y.log("parseSytleLen() "+ styleLength);
        if(Y.Lang.isString(styleLength)){
            var m = parseStyleLen.LENTH_PAT.exec(styleLength);
            
            var n = parseInt(m[1], 10);
            //Y.log("parseSytleLen() match "+m[1]);
            n = isNaN(n) ? 0:n;
            return n;
        }else if(Y.Lang.isNumber(styleLength))
            return styleLength;
    }
    parseStyleLen.LENTH_PAT = /([0-9.]*)\w*/;

    function hasVerticalScrolled(node) {
        return node.get("scrollHeight") - node.get("clientHeight") > 1;
    }
    
    function hasHorizontalScrolled(node) {
        return node.get("scrollWidth") - node.get("clientWidth") > 1;
    }
    
    var logVarPat = /[a-zA-Z0-9_$.]+/g;
    /** 
        usage:
            logVars('info()','abc,efg',abc,efg)
    */
    function logVars(name, sVarListStr, var1, var2){
        var msg =name + ': ', res = null, i=2;
        while((res = logVarPat.exec(sVarListStr)) != null && i< arguments.length){
            msg += res[0];
            msg += '=';
            msg += arguments[i++];
            msg += ' ';
        }
        Y.log(msg);
        logVarPat.lastIndex = 0;
    }
    
    /**
    Utilize setTimeout to asynchronously execute some event listeners, yield control 
    back to other UI operations during hanlding some continous event, like onScroll, onResize, onMousemove.
    
    2 functions,
    
    1) Tell a handle function a certain continous event stops, and send that last event to the handler
    (e.g. the mouse stops moving for 1 seconds)
    
    2) force event hanlder function check event intermittent
    it promises every last/stop event would must be sent to callback Function, so
    I may choose to only handle the last event result.
    
    Usage:
            node.on("scroll", createIntervalEventChecker(1000, handleFunction, null, this));
    @param interval the interval time
    @param handleFunc (could be null) the interval event checker callback function, with 1 parameter "event"
    @param stopHandleFunc (could be null) the stop|last event handle function, if you only want to handle stop event
    @return the delegate event handler function
    */
    function createIntervalEventChecker(interval, handleFunc, stopHandleFunc, startHandleFunc, thisObj){
        var timeoutId = null, STAT_STOPED = 0, STAT_STARTED =1, STAT_WAIT =2;
        var newEvent = null, stat = STAT_STOPED;
        
        function heartBeat(){
            if(stat === STAT_WAIT){
                //Y.log("createIntervalEventChecker()- stop event");
                timeoutId = null;
                if(stopHandleFunc)
                    stopHandleFunc.call(thisObj, newEvent);
                stat = STAT_STOPED;
            }else if(stat == STAT_STARTED){
                stat = STAT_WAIT;
                //Y.log("createIntervalEventChecker() check event");
                timeoutId = setTimeout(heartBeat, interval);
                if(handleFunc)
                    handleFunc.call(thisObj, newEvent);
            }else{
                //Y.log("!! createIntervalEventChecker() unknown state: "+ stat);
            }
        }
        
        return function handleEvent(e){
            newEvent = e;
            if(stat == STAT_STOPED){
                if(startHandleFunc)
                    startHandleFunc.call(thisObj, newEvent);
                //Y.log(">> createIntervalEventChecker() start event");
            }
            stat = STAT_STARTED;
            if(timeoutId === null)
                timeoutId = setTimeout(heartBeat, interval);
        }
    }
    var _toInitialCap = Y.cached(function(str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    })
    /**@class deferredTasks */
    var deferredTasks = new Y.AsyncQueue();
    /** @class WidgetRenderTaskQ */
    var WidgetRenderTaskQ = {
        
        invokeRendered:function(taskFun, widget){
            if(widget == null)
                widget = this;
            widget.after("render", taskFun, widget);
            //if(! widget instanceof Y.Widget){
            //    Y.log("invokeRendered() incorrect widget instance: "+ widget);
            //}
            //if(widget.get("rendered")){
            //    taskFun.apply(widget);
            //}else{
            //    widget.after("render", taskFun, widget);
            //}
        },
        /**
        call it in initializer()
        @param setUIFunc function(newValue, prevValue)
        */
        setupUIAttr:function(attrName, setUIFunc){
            this.invokeRendered(function(){
                this.bindAndSyncAttr(attrName, setUIFunc);
            }, this);
        },
        addUIAttr:function(name, config, syncUIFunc){
            this.addAttr(name, config, false);
            this.setupUIAttr(name, syncUIFunc);
        },
        WidgetRenderTaskQ_init:function(){
            this.addUIAttr("maxWidth",{value:null}, function(maxWidth){
                    if(maxWidth != null)
                        this.get("contentBox").setStyle("maxWidth", maxWidth);
            });
        },
        
        /**
        call it in syncUI
        @param setUIFunc function(newValue, prevValue) prevValue is null at first call
        */
        bindAndSyncAttr:function(attrName, setUIFunc){
            function _syncUIAttr(e){
                if(e.src !== 'ui')
                    setUIFunc.call(this, e.newVal, e.prevVal);
            }
            this.after(attrName + "Change", _syncUIAttr, this);
            var value = this.get(attrName );
            if(value != null)
                setUIFunc.call(this, value, null);
        }
    };
    Y.WidgetRenderTaskQ = WidgetRenderTaskQ;
    /**@property globalEventMgr */
    function _GlobalEventMgr(){
        var context = this;
        Y.on("windowresize", function(e){
        //Y.Event.attach("resize", function(e){
                Y.log("window resized");
                context.fire("resize", e);
        });
        //var t = this;
        //window.onresize = function(e){
        //        Y.log("window on resized");
        //        t.fire("resize", e);
        //}
    };
    _GlobalEventMgr.prototype = {
        onWindowResize:function(fn, context){
                return this.on("resize", fn, context);
        }
    };
    //Y.extend(_GlobalEventMgr, Y.EventTarget, {
    //        onresize:function(fn, context){
    //            return this.on("resize", fn, context);
    //        },
    //});
    Y.augment(_GlobalEventMgr, Y.EventTarget);
    var globalEventMgr = new _GlobalEventMgr();
    /**
    @param config <ul>
        <li> {array} columns column names
        <li> {array} columnKeys
        <li> {string} keyColumn the ID column's key
        <li> {array} rows
        <li> {function} loadHandle
        <li> {function} deleteHandle
        <li> {array} groupBy array of group id column's key/index
        <li> {array} groupCont array of group name column's key/index
            default is same as groupBy setting's value
        </ul>
    */
    function PagedGridModel(config){
        this.columns = ["default"];
        this.offset = 0;
        this.pageLimit = 10;
        //config:
        this.columns = config.columns;
        this.columnKeys = config.columnKeys;
        /** @property rows */
        this.rows = config.rows?config.rows:[];
        this.loadHandle = config.loadHandle;
        this.deleteHandle = config.deleteHandle;
        this._keyColumn = config.keyColumn;
        this.groupBy = config.groupBy;
        this.groupCont = config.groupCont? config.groupCont : this.groupBy;
        this._hasMore = false;
        /** @property _rowMap */
        this._rowMap = {};
        this.lastFetchedCount = 0;
        this.total = 0;
        this._loading = false;
        this._gpStartInfo = {};
    }
    PagedGridModel.prototype={
        setColumnAndKeys:function(cols, keys){
            this.columns = cols;
            this.columnKeys = keys;
            this.fireColumnChange();
        },
        /** call this method after data is transfered back from server side
        */
        loadRows:function(rows, hasMore, total){
            this.rows = this.rows.concat(rows);
            for(var i=0,l=rows.length; i<l; i++){
                var row = rows[i];
                this._rowMap[row[this._keyColumn]] = row;
            }
            this._hasMore = hasMore;
            this.lastFetchedCount = rows.length;
            //Y.log("new offset " + this.offset);
            if(total != null && total >= 0)
                this.total = total;
            this._loading = false;
            this.fireRowLoaded(this.offset, rows.length);
            
        },
        deleteRows:function(keyset){
            this.deleteHandle.call(this, keyset);
        },
        reset:function(){
            this.offset = 0;
            this._hasMore = true;
            this.lastFetchedCount = 0;
            var rows = this.rows;
            while(rows.length > 0)
                rows.shift();
        },
        isLoading:function(){
            return this._loading;
        },
        requestMore:function(){
            if(this._loading)
                return;
            var newOffset = this.lastFetchedCount + this.offset;
            this.offset = newOffset;
            this._loading = true;
            this.fireLoading();
            this.loadHandle.call(this, newOffset, this.pageLimit);
        },
        getRowByKey:function(key){
            return this._rowMap[key];
        },
        getColumns:function(){
            return this.columns;
        },
        getColumnKeys:function(){
            return this.columnKeys;
        },
        getRow:function(index){
            return this.rows[index];
        },
        /** 
        @return object with 2 properties
            {
                <ul><li> ids {array} group id
                <li> contents {array} group content
                </ul>
            }
         if there is any new group starts for this row,
         else return array of 2 empty arrays
        */
        groupStartFor:function(rowIndex){
            var lastRowIdx = rowIndex -1;
            var rowModel = this.getRow(rowIndex);
            var ids = this._gpStartInfo.ids = [],
            contents = this._gpStartInfo.contents = [];
            
            var  g = this.groupBy, gt = this.groupCont;
            for(var i=0,l= g.length; i<l; i++){
                var gpIdKey = g[i], gpContKey = gt[i];
                if(lastRowIdx < 0 || rowModel[gpIdKey] != this.getRow(lastRowIdx)[gpIdKey]){
                    ids.push(rowModel[gpIdKey]);
                    contents.push(rowModel[gpContKey]);
                }
            }
            return this._gpStartInfo;
        },
        
        getRowId:function(index){
            return this.getRow(index)[this._keyColumn];
        },
        getRows:function(){
            return this.rows;
        },
        getRowCount:function(){
            return this.rows.length;
        },
        getCell:function(rowIdx, columnKey){
            return this.getRow(rowIdx)[columnKey];
        },
        fireDeleted:function(count){
            this.fire("deleted", {count:count});
        },
        fireRowLoaded:function(index, count){
            this.fire("rowLoaded", {index: index, count:count});
        },
        fireColumnChange:function(){
            this.fire("columnChange", {});
        },
        fireLoading:function(){
            this.fire('loading', {});
        },
        hasMore:function(){
            return this._hasMore;
        }
    };
    Y.augment(PagedGridModel, Y.EventTarget);
    
    /** @class PagedGrid */
    var PagedGrid = Y.Base.create("pagedGrid",Y.Widget, [Y.WidgetChild], {
        MAX_WIDTH:"2900px",
        CONTENT_TEMPLATE: "<table></table>",
        
        /**
        config.cellView - function(thisGridObj, columnIndex)
        */
        initializer:function(config){
            //this.bindRenderTaskQ();
            this.cellView = config.cellView;
            this._horizontalScrollOn = false;
            this._verticalScrollOn = false;
            this.WidgetRenderTaskQ_init();
        },
        setCellView:function(cellViewFunc){
            this.cellView = cellViewFunc;
        },
        setModel:function(model){
            this.invokeRendered(function(){
                if(this.model != null)
                    this._unbindModel();
                this.model = model;
                this._bindModel();
                this.model.fireColumnChange();
                //model.requestMore();
                deferredTasks.add(
                    {fn:function(){
                            model.requestMore();
                        },
                        //context:this.model,
                        timeout:50
                    }).run();
            }, this);
            
        },
        _bindModel:function(){
            this.columnChangeHandle = this.model.after("columnChange", this.syncColumns, this);
            this.rowLoadedHandle = this.model.after("rowLoaded", this.syncLoadedUI, this);
            this.loadingHandle = this.model.on("loading", function(e){ 
                    this._syncHasMoreIncidater(true);
            }, this);
            this.deletedHandle = this.model.after('deleted', 
                function(e){
                    this.refresh();
                }, this);
        },
        _unbindModel:function(){
            this.columnChangeHandle.detach();
            this.rowLoadedHandle.detach();
            this.loadingHandle.detach();
            this.deletedHandle.detach();
        },
        testdata:function(){
            for(var i =0;i<50;i++)
                this._body.append(Y.Node.create("<tr><td>td</td></tr>"));
        },
        renderUI:function(){
            var node = this.get("contentBox");
            
            this.bodyHeight = null;
            this.headerscroll = Y.Node.create("<div><div><table><tbody></tbody></table></div></div>");
            var tbtbody = this.headerscroll.one("tbody");
            this.headerscroll.addClass(this.getClassName("header-scroll"));
            this._colheaders = Y.Node.create("<tr><th>header</th></tr>");
            tbtbody.append(this._colheaders);
            
            var bodyscroll = Y.Node.create("<div><div><table><tbody></tbody></table></div></div>")
            
            this._bodyscroll = bodyscroll;
            this._body = bodyscroll.one("table");
            
            this._bodyscroll.addClass(this.getClassName("scroll"));
            this._body.addClass(this.getClassName("body"));
            
            //this.testdata();
            var contentTbody = Y.Node.create("<tbody><tr><td></td></tr></tbody>");
            node.append(contentTbody);
            var contentTbodyTd = contentTbody.one("tr td");
            contentTbodyTd.append(this.headerscroll);
            contentTbodyTd.append(this._bodyscroll);
            this.contentTd = contentTbodyTd;
            this._moreRowNode = Y.Node.create('<tr><td><img src="../img/nycli1.gif"></td></tr>');
            this.rendedHasMore = false;
        },
        bindUI:function(){
            this._heightHandle = this.after("heightChange", this.syncHeight, this);
            this._widthHandle = this.after("widthChange", this.syncWidth, this);
            this._maxWidthHandle = this.on("maxWidthChange", this.onMaxWidthChange, this);
            this._tryBindScroll();
            this._tapHandle = this._body.one(">tbody").delegate("tap", this._onItemTap, "tr", this);
        },
        syncUI:function(){
            this.syncHeight();
            this.syncWidth();
        },
        _unbind:function(){
            this._heightHandle.detach();
            this._widthHandle.detach();
            this._tapHandle.detach();
            this._maxWidthHandle.detach();
        },
        destructor:function(){
            this._unbind();
        },
        _onItemTap: function (e){
                //e.currentTarget.addClass(this.getClassName("item","tapped"));
                this.fire("itemSelected",{data:e.currentTarget.getAttribute("data-key")});
        },
        syncUI:function(){
            this.syncHeight();
            this.syncWidth();
            this.syncColumns();
            
            //Y.log("sync UI "+ this.get("height"));
        },
        
        onMaxWidthChange:function(e){
            this._bodyscroll.setStyle("maxWidth", e.newVal + this.DEF_UNIT);
            e.preventDefault();
        },
        syncWidth:function(){
            var w = this.get("width");
            if(w != null ){
                //this._syncColumnsWidth();
                this._bodyscroll.setStyle("width", w + this.DEF_UNIT);
            }
        },
        syncHeight:function(){
            var h = this.get("height");
            var padding = 2;
            if(Y.Lang.isNumber(h)){
                var headerH = this._colheaders.get("offsetHeight");
                Y.log("header Hight="+ headerH);
                this._bodyscroll.setStyle("height", (h - headerH) - padding + this.DEF_UNIT);
                this._syncColumnsWidth();
            }
            
            
        },
        
        reLayout:function(){
            this.syncHeight();
        },
        syncColumns:function(){
            if(this.model == null)
                return;
            
            var hs = this._colheaders;
            /*var hsHidden = this._body.one(">thead");
            if(hsHidden == null){
                hsHidden = Y.Node.create("<thead></thead>");
                this._body.insert(hsHidden, 0);
            }else
                hsHidden.empty();
            hsHidden.append("<th></th>");
            */
            hs.empty();
            
            hs.append("<th></th>");
            
            Y.Array.each(this.model.getColumns(), 
                function(col){
                    hs.append("<th>"+ col +"</th>");
                    /*hsHidden.append("<th>"+ col +"</th>");*/
            }, this);
            
            this._syncColumnsWidth();
            
            this._moreRowNode.one(">td").setAttribute("colspan", this._colheaders.all(">th").size());
        },
        
        refresh:function(){
            this._body.one('tbody').setHTML('');
            delete this._lastTr;
            this.rendedHasMore = false;
            if(this.model){
                this.model.reset();
                this.model.requestMore();
            }
        },
        
        _firstRowTrNode:function(){
            var row1 = this._body.one("tr");
            while(row1 != null){
                var groupLabel = row1.getAttribute("grid-group");
                if(groupLabel != null && groupLabel != "" )
                    row1 = row1.next();
                else
                    break;
            }
            return row1;
        },
        _clearColumnsWidth:function(){
            this.headerscroll.one("table").setStyle("width", "auto");
            var row1 = this._firstRowTrNode();
            if(row1 == null) return;
            var tds = row1.all("> td");
            if(tds == null)
                return;
            var headerArray = this._colheaders.all("> th");
            //var last = tds.size() -1;
            tds.each(function(td, idx, tds){
                    var colHeader = headerArray.item(idx);
                    td.setStyle("width","auto");
                    colHeader.setStyle("width","auto");
            });
            this._bodyscroll.one("> div").setStyle("width", this.MAX_WIDTH);
            this.headerscroll.one("> div").setStyle("width", this.MAX_WIDTH);
        },
        
        _syncColumnsWidth:function(){
            this._clearColumnsWidth();
            var row1 = this._firstRowTrNode();
            if(row1 != null){
                var tds = row1.all("> td");
                if(tds == null)
                    return;
                var headerArray = this._colheaders.all("> th"),
                last = tds.size() -1, calTotalWidth = 0;
                tds.each(function(td, idx, tds){
                        // skip the last one, let it adjust its width
                        //if(last == idx)
                        //    return;
                        var w = td.get("clientWidth");
                        var colHeader = headerArray.item(idx);
                        if(colHeader == null)
                            return; 
                        var w1 = colHeader.get("clientWidth");
                        //Y.log("w, w1="+ w + "," + w1);
                        var headerPadding = parseStyleLen(colHeader.getComputedStyle("paddingLeft"));
                        headerPadding += parseStyleLen(colHeader.getComputedStyle("paddingRight"));
                        var tdPadding = parseStyleLen(td.getComputedStyle("paddingLeft"));
                        tdPadding += parseStyleLen(td.getComputedStyle("paddingRight"));
                        
                        
                        //Y.log("headerPadding:"+ headerPadding +
                        //    ", tdPadding:"+ tdPadding);
                        if(Y.Lang.isNumber(w)){
                            if( w > w1){
                                colHeader.setStyle("width", w - headerPadding + "px");
                                td.setStyle("width", w - tdPadding + "px");//force its width
                                //Y.log("set column header "+ idx + " width "+ perWidth);
                                calTotalWidth += td.get("offsetWidth");
                            }else if(w < w1){
                                td.setStyle("width", w1 - tdPadding + "px");
                                colHeader.setStyle("width", w1 - headerPadding + "px");//force its width
                                calTotalWidth += colHeader.get("offsetWidth");
                                //Y.log("set column "+ idx + " width "+ perWidth);
                                
                            }
                        }
                });
            }
            this._syncWidthForScroll(calTotalWidth);
        },
        _syncWidthForScroll:function(calTotalWidth){
            //Y.log('_syncColumnsWidth() here');
            
            this._bodyscroll.setStyle("width", "auto");
            this.headerscroll.one("> div").setStyle("width", 'auto');
            this.syncWidth();
            if(calTotalWidth){
                Y.log("# calTotalWidth="+ calTotalWidth);
                //if(calTotalWidth > this._body.get('clientWidth')) 
                //    //for firefox, if set each column's width does not actually expand the table's width
                //{
                    this._bodyscroll.one("> div").setStyle("width", calTotalWidth  + "px");
                    //Y.log("synce header scroll, calTotalWidth="+ calTotalWidth+ " _body="+ this._body.get("tagName") );
                    this.headerscroll.one("> div").setStyle("width", calTotalWidth + 1 +"px");
                    //Y.log("this._bodyscroll's scrollWidth="+ this._bodyscroll.get("scrollWidth"));
                //}
                
                
                if(Y.UA.ie > 0){
                    this.syncWidth();//fix IE's issue, force it recaculate the size of that DIV
                }
                this._eliminateHozScrollBar();
            }
        },
        _insertGroupLabel:function(tbody, rowIndex, rowModel){
            var model = this.model;
            if(model.groupBy == null)
                return;
            var groupInfo = model.groupStartFor(rowIndex);
            //Y.log("_insertGroupLabel() "+ Y.JSON.stringify(group));
            var grid = this;
            var deep = this.model.groupBy.length - groupInfo.ids.length;
            Y.Array.each(groupInfo.ids, function(gId, i, ids){
                    var tr = Y.Node.create("<tr><td><div><div></td></tr>");
                    tr.addClass(grid.getClassName('gp-'+ deep));
                    var td = tr.one('>td');
                    td.append(document.createTextNode(groupInfo.contents[i]));
                    td.setAttribute("colspan", model.columns.length + 1);
                    tr.setAttribute("grid-group", gId);
                    td.one('div').addClass('flag');
                    grid._insertTr(tbody, rowIndex, tr);
                    deep++;
            });
        },
        _insertTr:function(tbody, rowIdx, tr){
            if(this._lastTr == null){
                if(this.rendedHasMore)
                    this._moreRowNode.insert(tr, "before");
                else
                    tbody.append(tr);
            }else{
                try{
                this._lastTr.insert(tr, "after");
                }catch(e){
                    Y.log("insertTr() "+ tr.get("tagName") + this._lastTr);
                }
            }
            this._lastTr = tr;
        },
        syncLoadedUI:function(e){
             
             tbody = Y.one(document.createDocumentFragment());
             for(var i=e.index, l=e.index+e.count;i < l;i++){
                 var rowModel = this.model.getRow(i);
                 this._insertGroupLabel(tbody, i, rowModel);
                 var tr = Y.Node.create("<tr></tr>");
                 this._insertTr(tbody, i, tr);
                 tr.setAttribute("data-key", this.model.getRowId(i));
                 var idxTD = tr.append("<td class=\""+this.getClassName('no')+"\">"+ (i+1) +"</td>");
                 //idxTD.addClass(this.getClassName("idx"));
                 if(Y.Lang.isFunction(this.cellView)){
                     Y.Array.each(this.model.getColumns(), 
                         function(colName, colIndex){
                            var td = Y.Node.create("<td></td>");;
                            tr.append(td);
                            td.append(this.cellView.call(this, rowModel, colIndex));
                         }, this);
                 }else{
                     Y.Array.each(this.model.getColumnKeys(), function(key, index){
                         tr.append(this._renderDefCellNode(index, rowModel[key]));
                     }, this);
                 }
             }
             this._body.one("tbody").append(tbody);
             this._syncHasMoreIncidater(this.model.hasMore());
             Y.log('syncLoadedUI() done');
             this._syncColumnsWidth();
             this.fire("rowLoaded");
        },
        _syncHasMoreIncidater:function(hasMore){
            var tbody = this._body.one("tbody");
            if(hasMore){
                if(!this.rendedHasMore){
                   Y.log("loading...");
                   this.rendedHasMore = true;
                   tbody.append(this._moreRowNode);
               }
            }else if(this.rendedHasMore){
                 tbody.removeChild(this._moreRowNode);
                 this.rendedHasMore = false;
             }
        },
        _tryBindScroll:function(){
            if(this._bodyScrollHandle)
                return;
            this._bodyScrollHandle = this._bodyscroll.after("scroll", 
                createIntervalEventChecker(50, this._syncScroll, null,null, this)
                
                , this);
        },
        _unbindScroll:function(){
            if(this._bodyScrollHandle){
                this._bodyScrollHandle.detach();
                delete this._bodyScrollHandle;
            }
        },
        
        _syncScroll:function(e){
            var t = this._bodyscroll;
            var left = this._bodyscroll.get("scrollLeft");
            if(left !== this.headerscroll.get("scrollLeft")){
                this.headerscroll.set("scrollLeft", left);
            }
            if(this.rendedHasMore && 
                t.get("scrollHeight") - t.get("scrollTop") - t.get("clientHeight") < this._moreRowNode.get("offsetHeight"))
            {
                Y.log("loading more...");
                this.fire("loadmore");
                if(this.model)
                    this.model.requestMore();
            }
        },
        
        
        _renderDefCellNode:function(index, data){
             
             return ["<td>",data, "</td>"].join("");
             
        },
        //eliminate unnessary horizontal scroll bar
        _eliminateHozScrollBar:function(){
            var w = this.get("width");
            if(Y.Lang.isNumber(w) && w >= 0 || (Y.Lang.isString(w) && w.length > 0))
                return;
            var scroll = this._bodyscroll;
            if(scroll.get("scrollHeight") > scroll.get("clientHeight")){
                var offsetWidth = scroll.get("offsetWidth");
                var clientWidth = scroll.get("clientWidth");
                var scrollBarWidth = offsetWidth - clientWidth;
                if(clientWidth> 0 && scrollBarWidth > 0){
                    Y.log("h scrollbar width "+ scrollBarWidth);
                    if(scroll.get("scrollWidth") <= offsetWidth){
                        Y.log("horizontal scroll bar is shown only because of the vertical scroll bar");
                        scroll.setStyle("width", offsetWidth + scrollBarWidth+ "px");
                    }
                }
            }
        }
        });
    Y.mix(PagedGrid.prototype, WidgetRenderTaskQ);
    
    Y.PagedGridModel = PagedGridModel;
    Y.PagedGrid = PagedGrid;
    
    /**
    @class ScrollableGrid a new version of PagedGrid with Y.ScrollView feature
    @event itemSelected: data - id in string, node - selected TR node, src - "ui"
    */
    //var ScrollView = Y.Base.mix(Y.ScrollView, [Y.WidgetChild]);
    var ScrollableGrid = Y.Base.create("scrollgrid", PagedGrid, [Y.WidgetChild, Y.WidgetParent],
    {
        /** @attribute CONTENT_TEMPLATE */
        CONTENT_TEMPLATE: "<div></div>",
        renderUI:function(){
            var node = this.get("contentBox");
            node.set('tabIndex',0);
            var frag = Y.one(document.createDocumentFragment());
            this.bodyHeight = null;
            this.headerscroll = Y.Node.create("<div><div><table><tbody></tbody></table></div></div>");
            this._headerContent = this.headerscroll.one("*");
            var tbtbody = this.headerscroll.one("tbody");
            this.headerscroll.addClass(this.getClassName("header-scroll"));
            this._colheaders = Y.Node.create("<tr><th>header</th></tr>");
            tbtbody.append(this._colheaders);
            
            var bodyscroll = Y.Node.create("<div><div><table><tbody></tbody></table></div></div>")
            
            this._bodyscroll = bodyscroll;
            this._body = bodyscroll.one('table');
            
            this._bodyscroll.addClass(this.getClassName("scroll"));
            this._body.addClass(this.getClassName("body"));
            
            frag.append(this.headerscroll);
            frag.append(this._bodyscroll);
            this._moreRowNode = Y.Node.create('<tr><td><img src="../img/nycli1.gif"></td></tr>');
            
            this._renderScrollView(frag);
            this.rendedHasMore = false;
            node.append(frag);
        },
        
        bindUI:function(){
            this.heightHandle = this.after("heightChange", this.syncHeight, this);
            this.widthHandle = this.on("widthChange", this.syncWidth, this);
            //this._tryBindScroll();
            var tbody = this._body.one(">tbody");
            this.mouseEnterHandle = tbody.delegate('mouseenter', this._onMouseEnter, 'tr', this);
            this.mouseLeaveHandle = tbody.delegate('mouseleave', this._onMouseLeave, 'tr', this);
            tbody.delegate("grid|click", this._onItemClick, "tr", this);
            tbody.delegate("grid|touchstart", this._onTouchStart, "tr", this);
            tbody.delegate("grid|touchend", this._onTouchEnd, "tr", this);
            tbody.delegate("grid|touchmove", this._onTouchMove, "tr", this);
            tbody.delegate("grid|touchcancel", this._onTouchCancel, "tr", this);
            this._maxWidthHandle = this.on("maxWidthChange", this.onMaxWidthChange, this);
        },
        _unbind:function(){
            this.heightHandle.detach();
            this.widthHandle.detach();
            this.mouseEnterHandle.detach();
            this.mouseLeaveHandle.detach();
            this.tapHandle.detach();
            if(this._scrollViewHandle)
                this._scrollViewHandle.detach();
            
            this._maxWidthHandle.detach();
            this._body.one(">tbody").detach("grid|*");
            this.scrollView.detach('grid|*');
        },
        syncWidth:function(e){
            //var w = e? e.newVal: this.get("width"), pw;
            //if(w == null)
            //    return;
            //if(typeof(w) == 'number' && this.scrollView != null){
            //    var cb = this.get("contentBox");
            //    w = w - parseStyleLen(cb.getComputedStyle("paddingLeft"))
            //    - parseStyleLen(cb.getComputedStyle("paddingRight"));
            //    this.scrollView.set('width', w);
            //}else if(w.charAt(w.length-1) == '%'){
            //    var percent = parseInt(w.substring(0, w.length -1), 10),
            //    aa = this.get("boundingBox").ancestor();
            //    if(aa == null)
            //        return;// in case it is rendered to a document fragment
            //    pw = aa.get("clientWidth")*percent/100 
            //    - parseStyleLen(aa.getComputedStyle("paddingLeft"))
            //    - parseStyleLen(aa.getComputedStyle("paddingRight"));
            //    
            //    this.scrollView.set('width', pw);
            //}
            this.scrollView.refresh();
        },
        
        onMaxWidthChange:function(e){
            this.scrollView.set("width", e.newVal);
            this.scrollView.refresh();
            e.preventDefault();
        },
        syncHeight:function(){
            var h = this.get("height");
            var padding = 2;
            if(Y.Lang.isNumber(h)){
                var headerH = this._colheaders.get("offsetHeight");
                Y.log("header Hight="+ headerH);
                this.scrollView.set("height", (h - headerH) - padding + this.DEF_UNIT);
            }
            this._syncColumnsWidth();
            this.scrollView.refresh();
        },
        refresh:function(){
            ScrollableGrid.superclass.refresh.apply(this, arguments);
            this.scrollView.scrollTo(0, 0, 1);
        },
        _renderScrollView:function(frag){
            var grid = this;
            this.scrollView = new MyScrollView({
                //bounceRange: 50,
                snapDuration:100, 
                //axis:'xy',
                srcNode: this._bodyscroll
                });
            this.scrollView.render(frag);
            this.scrollView.on('grid|scrollXChange', 
                this._syncScrolling, this);
            this.scrollView.on('grid|scrollYChange', 
                createIntervalEventChecker(700, null, function(e){
                    this._syncLoadMore(e.newVal);
                }, null, this));
            //this._scrollViewHandle = this.scrollView.after('scrolling', 
            //    createIntervalEventChecker(200, this._syncScrolling, null,null, this),
            //    this);
        },
        _syncScrolling:function(e){
            if(e.newVal >=0){
                //this.headerscroll.set("scrollLeft", e.x);
                this._headerContent.setStyle("left", -e.newVal + "px");
                
                /* this._headerContent.transition(
                    {
                        duration: 0.2,
                        left: -e.newVal + "px"
                    }); */
            }
        },
        _syncLoadMore:function(scrollTop){
            if(this.rendedHasMore && 
                this.scrollContHeight - scrollTop - this.viewportHeight
            < this._moreRowNode.get("offsetHeight"))
            {
                Y.log("loading more...");
                logVars('_syncLoadMore()', 'scrollContHeight,scrollTop, viewportHeight moreNodeHeight',
                    this.scrollContHeight, scrollTop, this.viewportHeight, this._moreRowNode.get("offsetHeight"));
                this.fire("loadmore");
                if(this.model && !this.model.isLoading())
                    this.model.requestMore();
            }
        },
        _onItemClick: function (e){
            this.onTouchCancel(e);
            this._selectItemNode(e.currentTarget);
        },
        
        _onTouchStart:function(e){
            var node = e.currentTarget;
            var classname = this.getClassName('s','tch','start');
            this.touchLatency = setTimeout(function(){
                    node.addClass(classname);
            }, 200);
            
            this._touchStartNode = node;
            
        },
        _onTouchEnd:function(e){
            this.onTouchCancel(e);
        },
        _onTouchMove:function(e){
            this.onTouchCancel(e);
        },
        onTouchCancel:function(e){
            if( this._touchStartNode){
                var classname = this.getClassName('s','tch','start');
                if(this.touchLatency){
                    clearTimeout(this.touchLatency);
                    delete this.touchLatency;
                }
                this._touchStartNode.removeClass(classname);
                delete this._touchStartNode;
            }
        },
        _selectItemNode:function(node){
            var classname = this.getClassName('s','selItem');
            Y.log("_selectItemNode()");
            if(this.get('singleSelection')){
                
                this.selectionModel.selectSingle(node.getAttribute("data-key"), node, true);
                //if(!node.hasClass(classname))
                    node.addClass(classname);
                if(this._lastSingleSelNode && this._lastSingleSelNode != node)
                    this._lastSingleSelNode.removeClass(classname);
                this._lastSingleSelNode = node;
            }else{
                if(node.hasClass(this.getClassName('selItem'))){
                    node.removeClass(this.getClassName('selItem'));
                    this.selectionModel._removeSelection(node.getAttribute("data-key"));
                }else{
                    node.addClass(this.getClassName('selItem'));
                    this.selectionModel._addSelection(node.getAttribute("data-key"), node);
                }
            }
            this._scrollToVisible(node);
            var key = node.getAttribute("data-key");
            this.fire("itemSelected",{src:'ui', node:node, data:key});
        },
        
        _scrollToVisible:function(tr){
            if( (tr.get("offsetTop") + tr.get("offsetHeight"))  >
                (this.scrollView.getScrollTop() + this.viewportHeight))
            {
                //Y.log("test: "+ (tr.get("offsetTop")+ tr.get("offsetHeight") - this.viewportHeight + 4));
                this.scrollView.scrollTo(this.scrollView.getScrollLeft(), 
                    tr.get("offsetTop") + tr.get("offsetHeight") - this.viewportHeight, 200);
            }else if(tr.get("offsetTop") < this.scrollView.getScrollTop()){
                this.scrollView.scrollTo(this.scrollView.getScrollLeft(), 
                    tr.get("offsetTop"), 200);
            }
        },
        
        _onMouseEnter:function(e){
            var cln = this.getClassName('hover');
            e.currentTarget.addClass(cln);
        },
        _onMouseLeave:function(e){
            var cln = this.getClassName('hover');
            e.currentTarget.removeClass(cln);
            
        },
        syncLoadedUI:function(){
            ScrollableGrid.superclass.syncLoadedUI.apply(this, arguments);
            this.scrollView.refresh();
            /** @property scrollContHeight*/
            this.scrollContHeight = this.scrollView.get('contentBox').get('scrollHeight');
            /** @property viewportHeight*/
            this.viewportHeight = this.scrollView.get('contentBox').get('clientHeight');
            this._syncLoadMore(0);
        }
    });
    
    Y.ScrollableGrid = ScrollableGrid;
    
    /** @class MyEditableGrid
    @event: 'delete','add'
    */
    var MyEditableGrid = Y.MyEditableGrid = Y.Base.create("myeditgrid", ScrollableGrid,[],
    {
        initializer:function(config){
            //Y.log(config);
            /**@property selectionModel */
            this.selectionModel = new MyGridSelModel();
            this.selChangeHandle = this.selectionModel.after("selectChange",
                function(e){this._syncButton();}, this);
        },
        renderUI:function(){
            MyEditableGrid.superclass.renderUI.apply(this, arguments);
            var bottom = this.bottom = Y.Node.create('<div></div>');
            
            bottom.addClass(this.getClassName('bottom')).addClass('inline-parent');
            if(this.get('buttonVisible')){
                this._renderDefButtons(this.bottom);
            }
            this._renderMenu();
            
            this._totalNode = document.createTextNode("Total:  0");
            var _totalDIV = Y.Node.create("<div></div>");
            _totalDIV.addClass(this.getClassName("total"));
            _totalDIV.append(this._totalNode);
            bottom.append(_totalDIV);
            //bottom.append("<div class=\"clear\"></div>");
            
            this.get('contentBox').append(bottom);
        },
        bindUI:function(){
            MyEditableGrid.superclass.bindUI.apply(this,arguments);
            this.keydownHandle = this.get('contentBox').on("keydown", this._syncKeydown, this);
            //this.resizeHandle = globalEventMgr.onWindowResize(this.resize, this);
        },
        syncUI:function(){
            MyEditableGrid.superclass.syncUI.apply(this, arguments);
            this.bindAndSyncAttr('maxHeight', this._syncMaxHeightUI);
        },
        _renderMenu:function(){
            var menu = this.get("popmenu");
            if(menu == null) return;
            /**@property menuItems*/
            this.menuItems = [];
            if(menu.length == 1){
                this.menuItems.push(this._renderPopBtn(menu[0].text,
                    (menu[0].disabled==null? false: menu[0].disabled), menu[0].action ));
            }else if(menu.length > 1){
                this._renderPopBtn("more", false, function(e){
                        //todo popup menu
                });
            }
        },
        _renderPopBtn:function(text, disabled, action){
            var node = Y.Node.create('<div>'+ text + '</div>');
            node.addClass('yui3-button');
            this.bottom.append(node);
            var grid = this;
            this.popButton = new Y.Button({
                    srcNode:node,
                    on:{
                        click: function(e){
                            action.call(this, e);
                        }
                    },
                    disabled: disabled
            });
            this.popButton.render(this.bottom);
            return this.popButton;
        },
        _renderDefButtons:function(bottom){
            var container = Y.Node.create('<div></div>'),
            delNode = Y.Node.create('<div>'+res.DELETE+'</div>');
            container.addClass(this.getClassName("buttons"));
            
            delNode.addClass('yui3-button');
            container.append(delNode);
            
            var grid = this;
            this.delBut = new Y.Button({
                    srcNode:delNode,
                    on:{
                        click: function(){
                            if(grid.delBut.get('disabled') === true)
                                return;
                            grid.delBut.set('disabled', true);
                            deferredTasks.add({
                                    fn:function(){
                                        var g = grid;
                                        g.model.deleteRows(grid.selectionModel.keyset);
                                        g.fire('delete', {src:'ui'});
                                    }
                            }).run();
                            
                        }
                    },
                    disabled: true
            });
            this.delBut.render(container);
            
            var addNode = Y.Node.create('<button>'+res.ADD+'</button>');
            
            container.append(addNode);
            this.addBut = new Y.Button({
                    srcNode:addNode,
                    on:{
                        click: function(){
                            if(grid.addBut.get('disabled') === true)
                                return;
                            grid.fire('add', {src:'ui'});
                        }
                    },
                    disabled: false
            });
            this.addBut.render(container);
            this.butbar = new MyButtonBar({srcNode:container});
            bottom.append(container);
            this.butbar.render(bottom);
        },
        resize:function(){
            
            this.syncHeight();
            this.syncWidth();
        },
        syncHeight:function(){
            var h = this.get("height");
            if(h == null)
                return;
            var contentBox = this.get('contentBox');
            var headerH = this._colheaders.get("offsetHeight");
            var bottomH = this.bottom.get('offsetHeight');
            var padding = parseStyleLen(contentBox.getComputedStyle("paddingTop"));
            padding += parseStyleLen(contentBox.getComputedStyle("paddingBottom"));
            if(Y.Lang.isNumber(h)){
                Y.log("---height: "+ h+ ", header Hight="+ headerH + ", bottomH="+ bottomH
                    + " padding="+ padding + ", computed header height:"+ this._colheaders.getComputedStyle('height'));
                this.scrollView.set('height', (h - headerH) - padding - bottomH + this.DEF_UNIT);
            }else if(h.charAt(h.length-1) == '%'){
                
                var precent = parseInt(h.substring(0, h.length -1), 10);
                var parent = this.get("boundingBox").ancestor();
                if(parent !=null){
                    var ph = parent.get("clientHeight");
                    if(ph > 0 ){
                        var cal = Math.floor(ph * precent/100) - headerH - padding - bottomH;
                        //this._bodyscroll.setStyle("height", cal + "px");
                        this.scrollView.set('height', cal + "px");
                    }
                }
                Y.log("---% height: "+ h+ ", header Hight="+ headerH + ", bottomH="+ bottomH+
                    ", cal="+ cal);
            }
            this.scrollView.refresh();
            //this._syncColumnsWidth();
            
        },
        
        _syncMaxHeightUI:function(newVal, prevVal){
            if(Y.Lang.isNumber(newVal) && newVal != prevVal){
                var contentBox = this.get('contentBox');
                //this.get('boundingBox').setStyle('maxHeight', newVal);
                var headerH = this._colheaders.get("offsetHeight");
                var bottomH = this.bottom.get('offsetHeight');
                var padding = parseStyleLen(contentBox.getComputedStyle("paddingTop"));
                padding += parseStyleLen(contentBox.getComputedStyle("paddingBottom"));
                this._bodyscroll.setStyle("maxHeight", (newVal - headerH) - padding - bottomH + this.DEF_UNIT);
            }
        },
        
        _syncKeydown:function(e){
            var code = e.keyCode;
            //Y.log(code);
            switch(code){
            case 38:
                this._keySelNextItem(false, e);
                break;
            case 40:
                this._keySelNextItem(true, e);
                break;
            case 13:
                this._keySelNextItem(true, e);
                break;
            }
        },
        
        _keySelNextItem:function(isNext, e){
            if(this.model.getRowCount() == 0) return;
            if(this.selectionModel.isEmpty()){
                var firstTR = this._body.one('tbody tr');
                this._skip2SelNextItemTR(firstTR, true, e);
            }else{
                var node = this.selectionModel.getFirstNode();
                var next = isNext? node.next() : node.previous();
                this._skip2SelNextItemTR(next, isNext, e);
            }
        },
        
        _skip2SelNextItemTR:function(tr, isNext, e){
            while(tr != null){
                var attr = tr.getAttribute("data-key");
                if(attr != null & attr.length != 0){
                    this._selectItemNode(tr);
                    this.get("contentBox").focus();
                    // have to use this.focus(), IE9 fails to obtain focus in this way
                    e.preventDefault();
                    e.stopPropagation();
                    break;
                }else{
                    e.preventDefault();
                    e.stopPropagation();
                    this._scrollToVisible(tr);
                }
                
                tr = isNext? tr.next() : tr.previous();
            }
        },
        
        _syncPageInfoUI:function(){
            this._totalNode.data = "      total: " + this.model.total;
        },
        syncLoadedUI:function(){
            MyEditableGrid.superclass.syncLoadedUI.apply(this, arguments);
            this._syncButton();
            this._syncPageInfoUI();
            //this.get('contentBox').focusManager.refresh();
        },
        refresh:function(){
            MyEditableGrid.superclass.refresh.apply(this, arguments);
            this.selectionModel.clear();
            this._syncButton();
            this._syncPageInfoUI();
        },
        _syncButton:function(){
            if(!this.get('buttonVisible'))
                return;
            if(this.selectionModel.selectCount > 0){
                if(this.delBut.get('disabled') === true)
                    this.delBut.set('disabled', false);
            }else{
                if(this.delBut.get('disabled') === false)
                    this.delBut.set('disabled', true);
            }
        },
        //_syncMenuUI:function(){
        //    var menu = this.get("popmenu");
        //    if(menu == null || menu.length ==0) return;
        //    if(this.selectionModel.selectCount > 0)
        //        this.popButton.set("disabled", false);
        //},
        _unbind:function(){
            MyEditableGrid.superclass._unbind.apply(this, arguments);
            if(this.delBut)
                this.delBut.destroy();
            if(this.selChangeHandle)
                this.selChangeHandle.detach();
            this.keydownHandle.detach();
            //this.resizeHandle.detach();
        },
        destructor:function(){
            Y.Array.each(this.menuItems, function(menuItem){
                menuItem.destroy();
            });
            delete this.menuItems;
        }
    },
    {
        ATTRS:{
            /** @attribute singleSelection */
            singleSelection:{value: true},
            /** @attribute buttonVisible */
            buttonVisible:{value: true},
            /** @attribute maxHeight */
            maxHeight:{value: null},
            /** @attribute popmenu
                <pre>e.g. [{
                    text: 'open',
                    disabled: false,
                    action:function(){...}
                }]
                </pre>
            */
            popmenu:{value: null}
        }
    });
    MyEditableGrid.CSS_PREFIX = "yui3-scrollgrid";
    
    /** @class MyGridSelModel selction model for Grid
    event: selectChange
    */
    Y.MyGridSelModel = MyGridSelModel = function(){
        /** @property keyset*/
        this.keyset = {};
        /** @property selectCount*/
        this.selectCount = 0;
    }
    MyGridSelModel.prototype = {
        selectSingle:function(key, node, fromUI){
            this.clear();
            this.keyset[key] = node;
            this.selectCount = 1;
            if(fromUI)
                this._fireSelectChange({src:'ui', key:key});
            else
                this._fireSelectChange({key:key});
        },
        clear:function(){
            for(var k in this.keyset){
                delete this.keyset[k];
            }
            this.selectCount = 0;
        },
        _addSelection:function(key, node){
            this.keyset[key] = node;
            this.selectCount++;
            this._fireSelectChange({src:'ui'});
        },
        _removeSelection:function(key){
            delete this.keyset[key];
            this.selectCount--
            this._fireSelectChange({src:'ui'});
        },
        _fireSelectChange:function(evtFacade){
            this.fire('selectChange', (evtFacade ==null ?{}:evtFacade) );
        },
        getSelectedKeyset:function(){
            return this.keyset;
        },
        getFirstNode:function(){
            for(k in this.keyset){
                return this.keyset[k];
            }
            return null;
        },
        isEmpty:function(){
            for(k in this.keyset){
                return false;
            }
            return true;
        }
    }
    Y.augment(MyGridSelModel, Y.EventTarget);
    
    
    /** @class MyScrollView 
        @event scrolling
    */
    var MyScrollView = (function(){
        var getClassName = Y.ClassNameManager.getClassName,
        DOCUMENT = Y.config.doc,
        IE = Y.UA.ie,
        NATIVE_TRANSITIONS = Y.Transition.useNative,
        vendorPrefix = Y.Transition._VENDOR_PREFIX, // Todo: This is a private property, and alternative approaches should be investigated
        SCROLLVIEW = 'scrollview',
        CLASS_NAMES = {
            vertical: getClassName(SCROLLVIEW, 'vert'),
            horizontal: getClassName(SCROLLVIEW, 'horiz')
        },
        EV_SCROLL_END = 'scrollEnd',
        FLICK = 'flick',
        DRAG = 'drag',
        MOUSEWHEEL = 'mousewheel',
        UI = 'ui',
        TOP = 'top',
        LEFT = 'left',
        PX = 'px',
        AXIS = 'axis',
        SCROLL_Y = 'scrollY',
        SCROLL_X = 'scrollX',
        BOUNCE = 'bounce',
        DISABLED = 'disabled',
        DECELERATION = 'deceleration',
        DIM_X = 'x',
        DIM_Y = 'y',
        BOUNDING_BOX = 'boundingBox',
        CONTENT_BOX = 'contentBox',
        GESTURE_MOVE = 'gesturemove',
        START = 'start',
        END = 'end',
        EMPTY = '',
        ZERO = '0s',
        SNAP_DURATION = 'snapDuration',
        SNAP_EASING = 'snapEasing',
        EASING = 'easing',
        FRAME_DURATION = 'frameDuration',
        BOUNCE_RANGE = 'bounceRange',
        _constrain = function (val, min, max) {
            return Math.min(Math.max(val, min), max);
        };
        var ScrollView = Y.ScrollView;
        return Y.Base.create("myscrollview",Y.ScrollView, [Y.WidgetChild],{
            init:function(cfg){
                //cfg = cfg?cfg:{};
                //if(cfg.bounce == null)
                //    cfg.bounce = 0;//not working, dont know why
                MyScrollView.superclass.init.call(this, cfg);
            },
            initializer:function(cfg){
                //this._boundingNode = this.get("boundingBox");
                //this._contentNode = this.get("contentBox");
                this.MyScrollView_top = 0;
                this.MyScrollView_left = 0;
            },
            bindUI:function(){
                MyScrollView.superclass.bindUI.apply(this, arguments);
                this.bindAndSyncAttr("maxWidth", function(w){
                        this.m_bb.setStyle("maxWidth", w + "px");
                });
                this.after('myScrollView|axisChange', function(e){
                        //Y.log('axis change '+ Y.JSON.stringify(e.newVal));
                        var newVal = e.newVal, prevVal = e.prevVal;
                        if(prevVal == null)
                            return;
                        if(prevVal.x && !newVal.x)
                            this.scrollTo(0, this.get('scrollY'));
                        if(prevVal.y && !newVal.y)
                            this.scrollTo(this.get('scrollX'), 0);
                },this);
                this.handleRefresh = createIntervalEventChecker(300, null, function(){
                    delete this._cAxis;
                    var sv = this,
                    scrollDims = sv._getScrollDims(),
                    width = scrollDims.offsetWidth,
                    scrollWidth = scrollDims.scrollWidth,
                    scrollHeight = scrollDims.scrollHeight;
                    //logVars('myscrollView.refresh bf', 'width, scrollWidth', width, scrollWidth);
                    //logVars('myscrollView.refresh bf bb', 'sw',sv.get('boundingBox').get('scrollWidth'));
                    this.syncUI();
                    
                }, null, this);
            },
            destructor:function(){
                this.detach('myScrollView|*');
            },
            scrollTo: function (x, y, duration, easing, node){
                //arguments[2]=300;
                var ret = MyScrollView.superclass.scrollTo.apply(this, arguments);
                this.MyScrollView_top = y;
                this.MyScrollView_left = x;
                //Y.log('scroll to '+ this.getScrollLeft()+ ','+ this.getScrollTop()
                //    +" duration="+ duration
                //    +" node=" + node);
                
                this.fire('scrolling', {x:x, y:y, node:node});
                return ret;
            },
            getScrollTop:function(){
                //return this.MyScrollView_top;
                return this.get('scrollY');
            },
            getScrollLeft:function(){
                //return this.MyScrollView_left;
                return this.get('scrollX');
            },
            getScrollHeight:function(){
                return this.m_bb.get("scrollHeight");
            },
            /** current scrolled position is not reset to 0,0 yet, this has to be manually set if needed
            */
            refresh: function(){
                this.handleRefresh();
            },
            getViewportHeight:function(){
                return this.m_bb.get("clientHeight");
            },
            _getScrollDims: function () {
                var sv = this,
                    cb = sv._cb,
                    bb = sv._bb,
                    TRANS = ScrollView._TRANSITION,
                    // Ideally using CSSMatrix - don't think we have it normalized yet though.
                    // origX = (new WebKitCSSMatrix(cb.getComputedStyle("transform"))).e,
                    // origY = (new WebKitCSSMatrix(cb.getComputedStyle("transform"))).f,
                    origX = sv.get(SCROLL_X),
                    origY = sv.get(SCROLL_Y),
                    origHWTransform,
                    dims;
        
                // TODO: Is this OK? Just in case it's called 'during' a transition.
                if (NATIVE_TRANSITIONS) {
                    cb.setStyle(TRANS.DURATION, ZERO);
                    cb.setStyle(TRANS.PROPERTY, EMPTY);
                }
        
                origHWTransform = sv._forceHWTransforms;
                sv._forceHWTransforms = false; // the z translation was causing issues with picking up accurate scrollWidths in Chrome/Mac.
                
                /**
                Fix by Liujing, change to use cb instead of bb
                */
                //sv._moveTo(cb, 0, 0);
                dims = {
                    'offsetWidth': cb.get('offsetWidth'),
                    'offsetHeight': cb.get('offsetHeight'),
                    'scrollWidth': cb.get('scrollWidth'),
                    'scrollHeight': cb.get('scrollHeight')
                };
                //sv._moveTo(cb, -(origX), -(origY));
        
                sv._forceHWTransforms = origHWTransform;
        
                return dims;
            },
            
            /**
            change to YUI code,<ul>
                <li> scrollOffset = 2
                <li> scrollToY = scrollY - ((e.wheelDelta) * scrollOffset);
                </ul>
            */
            _mousewheel:function(e){
                //MyScrollView.superclass._mousewheel.apply(this, arguments);
                //Y.log("wheelDelta: " + e.wheelDelta);
                var sv = this,
                    scrollY = sv.get("scrollY"),
                    bb = sv._bb,
                    scrollOffset = 10,
                    isForward = (e.wheelDelta > 0),
                    // change from YUI code
                    scrollToY = scrollY - ((e.wheelDelta) * scrollOffset);
                    //scrollToY = scrollY - ((isForward ? 1 : -1) * scrollOffset);
                if(e._event && e._event.wheelDeltaX !== undefined)
                    var scrollToX = sv.get('scrollX') - (e._event.wheelDeltaX);
                //logVars('_mousewheel()', 'scrollToX,e.wheelDeltaX', scrollToX,e._event.wheelDeltaX);
                scrollToY = _constrain(scrollToY, sv._minScrollY, sv._maxScrollY);
                scrollToX = _constrain(scrollToX, sv._minScrollX, sv._maxScrollX);
                
                if (bb.contains(e.target) && (sv._cAxis["y"]||sv._cAxis['x'])) {
                    sv.lastScrolledAmt = 0;
                    sv.set("scrollY", scrollToY);
                    sv.set('scrollX', scrollToX);
                    if (sv.scrollbars) {
                        // @TODO: The scrollbars should handle this themselves
                        sv.scrollbars._update();
                        sv.scrollbars.flash();
                        // or just this
                        // sv.scrollbars._hostDimensionsChange();
                    }
        
                    // Fire the 'scrollEnd' event
                    sv._onTransEnd();
        
                    // prevent browser default behavior on mouse scroll
                    e.preventDefault();
                }
            },
            modernEvent:function(orgEvent){
                var delta = 0, returnValue = true, deltaX = 0, deltaY = 0;
                // Old school scrollwheel delta
                if ( orgEvent.wheelDelta ) { delta = orgEvent.wheelDelta/120; }
                if ( orgEvent.detail     ) { delta = -orgEvent.detail/3; }
                
                // New school multidimensional scroll (touchpads) deltas
                deltaY = delta;
                
                // Gecko
                if ( orgEvent.axis !== undefined && orgEvent.axis === orgEvent.HORIZONTAL_AXIS ) {
                    deltaY = 0;
                    deltaX = -1*delta;
                }
                
                // Webkit
                if ( orgEvent.wheelDeltaY !== undefined ) { deltaY = orgEvent.wheelDeltaY/120; }
                if ( orgEvent.wheelDeltaX !== undefined ) { deltaX = -1*orgEvent.wheelDeltaX/120; }
                
                return [deltaX, deltaY];
            }
        }, {ATTRS:{
            maxWidth:{value:null}
        }});
    })();
    Y.mix(MyScrollView.prototype, WidgetRenderTaskQ);
    MyScrollView.CSS_PREFIX = 'yui3-scrollview';
    Y.MyScrollView = MyScrollView;
    
    
    /**@class VerBox
    */
    var VerBox = Y.Base.create("verbox",Y.Widget, [Y.WidgetChild, Y.WidgetParent], {
        _uiAddChild:function(child){
            child.get("boundingBox").addClass(this.getClassName("child"));
            var width = this.get("width");
            if(Y.Lang.isNumber(width)){
                this._setChildWidth(child, this.get('width'));
            }
            Y.WidgetParent.prototype._uiAddChild.apply(this, arguments);
        },
        reLayout:function(){
            Y.Array.each(this._items, function(item, i, items){
                    if(item.reLayout)
                        item.reLayout();
            });
        },
        syncUI:function(){
            this.bindAndSyncAttr('enableBorder', function(enabled){
                    if(enabled){
                        this.get('contentBox').addClass(this.getClassName("border"));
                    }
            });
            this._syncWidth();
        },
        bindUI:function(){
            this.widthHandle = this.after("widthChange", this._syncWidth, this);
            this.visibleHandle = this.after('visibleChange',
                function(e){
                            this.vBox.reLayout();
                }, this);
        },
        destructor:function(){
            this.widthHandle.detach();
            this.visibleHandle.detach();
        },
        _syncWidth:function(){
            var width = this.get("width");
            if(Y.Lang.isNumber(width)){
                Y.Array.each(this._items, function(child){
                        this._setChildWidth(child, width);
                }, this)
            }
        },
        _setChildWidth:function(child, width){
            child.set("width", width);
        }
        },{ATTRS:{
            enableBorder:{value: false}
        }});
    
    Y.VerBox = VerBox;
    Y.mix(VerBox.prototype, WidgetRenderTaskQ);
    
    /** @class HorBox*/
    var HorBox = Y.Base.create("horbox",Y.Widget, [Y.WidgetChild, Y.WidgetParent], {
        _uiAddChild:function(child){
            child.get("boundingBox").addClass(this.getClassName("child"));
            var height = this.get("height");
            if(Y.Lang.isNumber(width)){
                this._setChildHeight(child, this.get('height'));
            }
            Y.WidgetParent.prototype._uiAddChild.apply(this, arguments);
        },
        reLayout:function(){
            Y.Array.each(_items, function(item, i, items){
                    if(item.reLayout)
                        item.reLayout();
            });
        },
        syncUI:function(){
            this.bindAndSyncAttr('enableBorder', function(enabled){
                    if(enabled){
                        this.get('contentBox').addClass(this.getClassName("border"));
                    }
            });
            this._syncWidth();
        },
        bindUI:function(){
            this.heightHandle = this.after("heightChange", this._syncWidth, this);
            this.visibleHandle = this.after('visibleChange',
                function(e){
                            this.vBox.reLayout();
                }, this);
        },
        destructor:function(){
            this.heightHandle.detach();
            this.visibleHandle.detech();
        },
        _syncHeight:function(){
            var width = this.get("height");
            if(Y.Lang.isNumber(width)){
                Y.Array.each(this._items, function(child){
                        this._setChildWidth(child, width);
                }, this)
            }
        },
        _setChildHeight:function(child, width){
            child.set("height", width);
        }
        },{ATTRS:{
            enableBorder:{value: false}
        }});
    
    Y.HorBox = HorBox;
    Y.mix(HorBox.prototype, WidgetRenderTaskQ);
    
    /**@class MyPortal */
    Y.Event.defineOutside( "tap");
    
    var MyPortal = Y.Base.create("myportal",Y.Panel, [], {
        initializer:function(config){
            //MyPortal.superclass.initializer.apply(this, arguments);
            this.after("render", function(e){
                var node = this.getStdModNode(Y.WidgetStdMod.HEADER, true);
                this._titleNode = Y.Node.create("<div>UUUUU</div>");
                node.insert(this._titleNode, 0);
                Y.log("title: "+ this.get('title'));
                this.bindAndSyncAttr('title', this._setTitleUI);
                this.bindAndSyncAttr('focused', 
                    function(focused){
                        if(focused){
                            this._bindBlur();
                            this._unbindFocus();
                        }else{
                            this._bindFocus();
                            this._unbindBlur();
                        }
                    });
            }, this);
        },
        _bindFocus:function(){
            //if(this.focusHandle == null)
            //    this.focusHandle = this.get('contentBox').on('focus',
            //    function(){
            //        this.focus();
            //    }, this);
        },
        _unbindFocus:function(){
            //if(this.focusHandle){
            //    this.focusHandle.detach();
            //    delete this.focusHandle;
            //}
        },
        _bindBlur:function(){
            //if(this.blurHandle == null)
            //    this.blurHandle = this.get('boundingBox').on('blur', 
            //        function(){
            //            this.blur();
            //        }, this);
        },
        _unbindBlur:function(){
            //if(this.blurHandle){
            //    this.blurHandle.detach();
            //    delete this.blurHandle;
            //}
        },
        _setTitleUI:function(title){
            this._titleNode.setHTML(title);
        },
        setTitle:function(title){
            this.set('title', title);
        },
        setChildrenContainer:function(node){
            this._childrenContainer = node;
        },
        destructor:function(){
            this._unbindFocus();
            this._unbindBlur();
            
        }
        },{ATTRS:{
            /**@attribute title*/
            title:{value:""}
        }});
    
    Y.mix(MyPortal.prototype, WidgetRenderTaskQ);
    Y.MyPortal = MyPortal;
    MyPortal.CSS_PREFIX = "yui3-panel";
    
    /**@class Dialog 
    useful attribute:
    bodyContent - text content
    */
    var MyDialog = Y.Base.create("mydialog", Y.Panel, [], {
        init:function(cfg){
            var defCfg = {
                 //centered:true,
                 modal:true,
                 y:-300,
                 zIndex:90,
                 visible:false,
                 plugins:[Y.Plugin.Drag],
                 buttons: {
                    footer: [
                        {
                            name  : 'cancel',
                            label : 'Cancel',
                            action: 'onCancel',
                            context:this
                        },
        
                        {
                            name     : 'proceed',
                            label    : 'OK',
                            action   : 'onOK',
                            context:this
                        }
                    ]
                }
            };
            Y.mix(defCfg, cfg);
            MyDialog.superclass.init.call(this, defCfg);
        },
        /**
        @param callback on OK clicked
        */
        show:function(content, callback){
            if(content)
                this.set('bodyContent', content);
            this.okCallback = callback;
            this.set('visible', true);
            var bb = this.get("boundingBox"),
             parent = Y.one(window);
            var viewport = {h:parent.get('winHeight'), w: parent.get('winWidth')};
            var size = this._viewSize ={h:bb.get('offsetHeight'), w:bb.get('offsetWidth')};
            var x = (viewport.w - size.w)>>1;
            var y = (viewport.h - size.h)>>1;
            this.set("xy",[x, -size.h-10]);
            
            deferredTasks.add(function(){
                bb.transition({
                        top:y+"px",
                        left:x+"px",
                        easing:'ease-out',
                        duration:0.15
                }); 
                bb.setStyles({top:y+"px",
                left:x+"px"});
            }).run();
            
        },
        hide:function(){
            var bb = this.get("boundingBox"),
            self = this;
            //bb.setStyle("top", -this._viewSize.h-20+"px");
            self.set('visible', false);
            
        },
        onOK:function(){
            if(this.okCallback);
                this.okCallback();
        },
        onCancel:function(){
            this.hide();
        }
    },{ATTRS:{
            /**@attribute title*/
            title:{value:""}
    }});
    MyDialog.CSS_PREFIX = "lj-dialog";
    
    /**@class ButtonBar
    */
    var MyButtonBar = Y.Base.create('mybtnbar', Y.Widget, [Y.WidgetChild], {
            renderUI:function(){
                var cb =this.get('contentBox'), buttonNodes = cb.get('children');
                cb.one(':first-child').addClass('barButton-first');
                cb.one(':last-child').addClass('barButton-last');
            }
    });
    
    /** @class MyButtonGroup 
    */
    var MyButtonGroup = Y.Base.create("mybtngroup", Y.ButtonGroup, [Y.WidgetChild], {
            initializer:function(config){
                this._buttonMap = {};
                this._buttonView = this._defButtonView;
                this._groupId = Y.guid();
                this.after('render',function(){
                        var cb = this.get("contentBox");
                        if(config.buttons)
                            for(var i=0,l=config.buttons.length;i<l;i++){
                                var b = config.buttons[i];
                                var node = this._buttonView.call(this, b.name, cb);
                                node.setAttribute("my-data", b.actionCmd);
                                this._buttonMap[b.actionCmd] = node;
                                node.plug(Y.Plugin.Button);
                            }
                }, this);
            },
            renderUI:function(){
                MyButtonGroup.superclass.renderUI.apply(this, arguments);
                //var cb =this.get('contentBox'), buttonNodes = cb.get('children');
                
            },
            addButton:function(name, actionCmd){
                this.invokeRendered(function(){
                        var node = this._buttonView.call(this, name, this.get("contentBox"));
                        node.setAttribute("my-data", actionCmd);
                        this._buttonMap[actionCmd] = node;
                        node.plug(Y.Plugin.Button);
                }, this);
            },
            selectButton:function(actionCommand){
                this.invokeRendered(function(){
                        this._buttonMap[actionCommand].simulate('click');
                });
            },
            _defButtonView:function(name, containerNode){
                var node = Y.Node.create('<button>'+ name +'</button>');
                containerNode.append(node);
                return node;
            }
    });
    Y.mix(MyButtonGroup.prototype, WidgetRenderTaskQ);
    Y.MyButtonGroup = MyButtonGroup;
    MyButtonGroup.CSS_PREFIX="yui3-buttongroup";
    
    /**@class MyTextField 
        @event enter
        @event valueChange
    */
    var MyTextField = Y.Base.create("mytextfield", Y.Widget, [Y.WidgetChild],
        {
            initializer:function(config){
                this._textLabel = config.label;
                /**@attribute input */
                this.addUIAttr('input', {value: ''}, 
                    function(value){
                        this.get('contentBox').one('input[type="text"]').set('value', value);
                    });
                /**@attribute label */
                this.addUIAttr('label', {value: 'Label'}, 
                    function(label){
                        this.lbDom.data = label;
                        //this.get('contentBox').one('> div').setHTML(label);
                    });
                this.setupUIAttr('required',
                    function(newVal, preVal){
                        if(newVal === true){
                            if(preVal != true)
                                this.get('contentBox').insert(Y.Node.create('<font color="red">*</font>'), 1);
                        }else if(newVal === false){
                            if(preVal === true)
                                this.get('contentBox').one('>font').remove(true);
                        }
                    });
                if(config.value != null)
                    this.set('input', config.value);
                if(config.label != null)
                    this.set('label', config.label);
                this._onEnterKeySet = false;
                this._onChangeSet = false;
            },
            /**
            event object:
                src - 'ui'
                text - text value
            */
            on:function(type, func, thisobj){
                if(type == 'valueChange' && this._initValueChange == null ){
                    this._initValueChangeEvt();
                }else if(type == "enter" && (this._onEnterKeySet == null || this._onEnterKeySet === false)){
                    this._initEnterEvt();
                }
                return MyTextField.superclass.on.apply(this, arguments);
            },
            _initEnterEvt:function(){
                this.after("render", function(){
                    var field = this.inputField;
                    this._keydownHandle = field.on('keypress', function(e){
                            if(e.charCode == 13){
                                e.preventDefault();
                                //e.stopPropagation();
                                this.fire('enter', {src: 'ui', text:field.get("value") });
                            }
                    }, this);
                }, this);
                this._onEnterKeySet = true;
            },
            _initValueChangeEvt:function(){
                this.after("render", function(){
                    this._valueChangeHandle = this.inputField.on('valueChange',
                    function(e){
                        e.src = 'ui';
                        e.text = this.inputField.get('value');
                        this.fire('valueChange', e);
                    }, this);
                });
                this._initValueChange = true;
            },
            renderUI:function(){
                this.lbDom = document.createTextNode("");
                this.get('contentBox').append(this.lbDom);
                var textbox = Y.Node.create('<input type="text" value="">');
                this.get('contentBox').append(textbox);
                /** @property inputField */
                this.inputField = this.get('contentBox').one('input[type="text"]');
            },
            syncInput:function(){
                var val = this.get('contentBox').one('input[type="text"]').get('value');
                //Y.log(val);
                this.set('input', val,
                    {src:'ui'});
                return val;
            },
            destructor:function(){
                if(this._keydownHandle)
                    this._keydownHandle.detach();
                if(this._valueChangeHandle)
                    this._valueChangeHandle.detach();
            }
        }, {ATTRS:{
            /** @attribute required */
            'required': {value: 'false'}
        }});
    
    Y.mix(MyTextField.prototype, WidgetRenderTaskQ);
    Y.MyTextField = MyTextField;
    
    /** @class MySearchField */
    var MySearchField = Y.Base.create("mytextfield", MyTextField, [Y.WidgetChild],
        {
            bindUI:function(config){
                MySearchField.superclass.bindUI.apply(this, arguments);
                this.on("valueChange", this._syncValueChangeUI, this);
                
            },
            _syncValueChangeUI:function(e){
                var field = this;
                if(this.searchTimer != null)
                    clearTimeout(this.searchTimer);
                this.searchTimer = setTimeout(
                    function(){
                        field.fire("search", e);
                    }, this.get("delay"));
            }
            
        }, {ATTRS:{
                /**@attribute delay time in millisecond*/
                delay:{value:700}
            }
        });
    Y.mix(MyTextField.prototype, WidgetRenderTaskQ);
    Y.MySearchField = MySearchField;
        MySearchField.CSS_PREFIX = "yui3-mytextfield";
    
    /**@class SplitBar 
    config: 
        - direction 'v'/'h'
        - node bar node
        - container  container node
      @event change
        - value top distance in pixel
    */
    function SplitBar(config){
        SplitBar.superclass.constructor.apply(this,arguments);
    }
    SplitBar.ATTRS = {
            maxValue:{value:-1},
            minValue:{value:1},
            value:{value:-1}
    };
    Y.extend(SplitBar, Y.Base, {
            initializer:function(cfg){
                /**@property direction */
                this.direction = cfg.direction ? cfg.direction:'v';
                /**@property node */
                this.node = Y.one(cfg.node);
                /**@property contain */
                this.container = Y.one(cfg.container);
                this.drag = new Y.DD.Drag({node:this.node}).
                //plug(Y.Plugin.DDProxy).
                plug(Y.Plugin.DDConstrained, {
                    constrain2node: this.container
                });
                this.node.addClass(this.direction == 'v'? 'ver-dragBar':'hor-dragBar');
                if(this.direction == 'v'){
                    this._containerClass = 'ver-split-panel';
                    this._draggingClass = 'ver-splitDragging';
                }else{
                    this._containerClass = 'hor-split-panel';
                    this._draggingClass = 'hor-splitDragging';
                }
                
                this.drag.on('drag:end', this._dragEnd, this);
                this.drag.on('drag:start', function(){
                        this.container.addClass(this._containerClass);
                        this.node.addClass(this._draggingClass);
                }, this);
            },
            
            _dragEnd:function(e){
                var bar = this;
                deferredTasks.add(function(){
                        bar.container.removeClass(bar._containerClass);
                        bar.node.removeClass(bar._draggingClass);
                        bar.set('value',
                            bar.node.get(bar.direction == 'v'?'offsetTop': 'offsetLeft'));
                }).run();
                
            },
            reset:function(){
                if(this.direction == 'v')
                    this.node.setStyle('top','');
                else
                    this.node.setStyle('left','');
            },
            destructor:function(){
                this.drag.detachAll();
            }
    });
    
    /**@class App 
    view may implement onResize(viewContainer)
    and App will set itself to view.app when a view becomes activeView
    */
    var MyApp = Y.Base.create('myApp', Y.App, [],{
            initializer:function(){
                this.resizeHandler = globalEventMgr.onWindowResize(this.resize, this);
                this.after('activeViewChange', function(e){
                        e.newVal.app = this;
                        Y.log("active view changed");
                        this.resize();
                }, this);
            },
            destructor:function(){
                this.resizeHandler.detach();
                this.detachAll();
            },
            resize:function(){
                var v = this.get('activeView');
                if(v && v.onResize)
                    v.onResize(this.get('viewContainer'));
            }
    });
    
    
    /**@class AppManager */
    function AppManager(viewContainer, views){
        /**@property apps */
        this.apps = [];
        this.setupApp(viewContainer, views);
        this._create1stApp();
    }
    AppManager.prototype = {
        setupApp:function(viewContainer, views){
            this.appConfig = {
                    serverRouting:false,
                    transitions:true,
                    viewContainer:viewContainer,
                    views:views
            };
        },
        routes:function(routes){
            if(routes != null){
                this.routes = routes;
                var self = this;
                Y.Array.each(this.apps, function(app){
                      Y.Array.each(self.routes, function(r){
                              app.route(r.path, r.callbacks);
                      });
                });
            }else
                return this.routes;
            return this;
        },
        _create1stApp:function(){
            
            var app = new Y.App(this.appConfig);
            
            this.apps.push(app);
            return app;
        },
        showView:function(name){
            Y.log("showView "+ name);
            this.apps[0].showView.apply(this.apps[0], arguments);
        },
        save:function(url){
            this.apps[0].save(url);
        }
    };
    Y.augment(AppManager, Y.EventTarget);
    
    var lj = Y.namespace('lj');
    Y.mix(lj,{
        AppManager:AppManager,
        Dialog:MyDialog,
        SplitBar:SplitBar,
        App:MyApp,
        buttonBar:MyButtonBar,
        parseStyleLen:parseStyleLen,
        globalEventMgr:globalEventMgr,
        deferredTasks:deferredTasks,
        loadHandler:loadHandler
    });
    
    }catch(e){
        Y.log(e.stack);
        throw e;
    }
    
    
}, "1.0.0",{
requires:['lj-basic-css','get','base','overlay','node','event','panel','widget','widget-parent','widget-child',
'button','button-group','scrollview','node-focusmanager','app',"async-queue",'dd-drag','dd-proxy','dd-constrain']});
