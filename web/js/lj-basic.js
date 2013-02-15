
YUI.add("lj-basic", function(Y){
        try{
    function parseStyleLen(styleLength){
        //Y.log(styleLength);
        if(Y.Lang.isString(styleLength)){
            var m = parseStyleLen.LENTH_PAT.exec(styleLength);
            //Y.log(m[1]);
            return parseInt(m[1]);
        }else
            return styleLength;
    }
    parseStyleLen.LENTH_PAT = /([0-9.]*)\w*/;

    function hasVerticalScrolled(node) {
        return node.get("scrollHeight") - node.get("clientHeight") > 1;
    }
    
    function hasHorizontalScrolled(node) {
        return node.get("scrollWidth") - node.get("clientWidth") > 1;
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
    function createIntervalEventChecker(interval, handleFunc, stopHandleFunc, thisObj){
        var DONE = 'nil', timeoutId = null, lastEvent = null;
        var newEvent = DONE;
        
        function heartBeat(){
            if(newEvent === DONE){
                //Y.log("stop event");
                timeoutId = null;
                if(stopHandleFunc)
                    stopHandleFunc.call(thisObj, lastEvent);
            }else{
                lastEvent = newEvent;
                newEvent = DONE;
                //Y.log("check event");
                if(handleFunc)
                    handleFunc.call(thisObj, lastEvent);
                timeoutId = setTimeout(heartBeat, interval);
            }
        }
        
        return function handleEvent(e){
            newEvent = e;
            if(timeoutId === null)
                timeoutId = setTimeout(heartBeat, interval);
        }
    }
    var _toInitialCap = Y.cached(function(str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    })
    /** @class WidgetRenderTaskQ */
    var WidgetRenderTaskQ = {
       
        
        invokeRendered:function(taskFun, context){
            if(this.get("rendered")){
                if(context != null)
                    taskFun.apply(context);
                else
                    taskFun.apply(this);
            }else{
                this.after("render", taskFun, context);
            }
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
            setUIFunc.call(this, this.get(attrName ), null);
        }
    };
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
        this.rows = config.rows?config.rows:[];
        this.loadHandle = config.loadHandle;
        this.deleteHandle = config.deleteHandle;
        this._keyColumn = config.keyColumn;
        this.groupBy = config.groupBy;
        this.groupCont = config.groupCont? config.groupCont : this.groupBy;
        this._hasMore = false;
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
        CONTENT_TEMPLATE: "<table></table>",
        
        /**
        config.cellView - function(thisGridObj, columnIndex)
        */
        initializer:function(config){
            //this.bindRenderTaskQ();
            this.cellView = config.cellView;
            this._horizontalScrollOn = false;
            this._verticalScrollOn = false;
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
                //this.model.fireRowLoaded(0, this.model.getRowCount());
                this.model.requestMore();
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
            this.headerscroll = Y.Node.create("<div><table><tbody></tbody></table></div>");
            var tbtbody = this.headerscroll.one("tbody");
            this.headerscroll.addClass(this.getClassName("header-scroll"));
            this._colheaders = Y.Node.create("<tr><th>header</th></tr>");
            tbtbody.append(this._colheaders);
            
            var bodyscroll = Y.Node.create("<div><table><tbody></tbody></table></div>")
            
            this._bodyscroll = bodyscroll;
            this._body = bodyscroll.one(">table");
            
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
            this._widthHandle = this.on("widthChange", this.onWidthChange, this);
            this._tryBindScroll();
            this._tapHandle = this._body.one(">tbody").delegate("tap", this._onItemTap, "tr", this);
        },
        _unbind:function(){
            this._heightHandle.detach();
            this._widthHandle.detach();
            this._tapHandle.detach();
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
        
        onWidthChange:function(e){
            this.userSetWidth = e.newVal;
            this.syncWidth();
            e.preventDefault();
        },
        syncWidth:function(){
            if(this.userSetWidth){
                this._bodyscroll.setStyle("width", this.userSetWidth + this.DEF_UNIT);
                Y.log("syncWidth");
            }
        },
        syncHeight:function(){
            var h = this.get("height");
            var padding = 2;
            if(Y.Lang.isNumber(h)){
                var headerH = this._colheaders.get("offsetHeight");
                Y.log("header Hight="+ headerH);
                this._bodyscroll.setStyle("height", (h - headerH) - padding + this.DEF_UNIT);
            }
            this._syncColumnsWidth();
            
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
            this._bodyscroll.setStyle("width", 'auto');
            this.headerscroll.setStyle("width", 'auto');
        },
        
        _syncColumnsWidth:function(){
            this._clearColumnsWidth();
            var row1 = this._firstRowTrNode();
            if(row1 != null){
                var tds = row1.all("> td");
                if(tds == null)
                    return;
                var headerArray = this._colheaders.all("> th");
                var last = tds.size() -1;
                var calTotalWidth = 0;
                tds.each(function(td, idx, tds){
                        // skip the last one, let it adjust its width
                        //if(last == idx)
                        //    return;
                        var w = td.get("offsetWidth");
                        var colHeader = headerArray.item(idx);
                        if(colHeader == null)
                            return; 
                        var w1 = colHeader.get("offsetWidth");
                        //Y.log("w, w1="+ w + "," + w1);
                        if(Y.Lang.isNumber(w)){
                            if( w > w1){
                                //colHeader.setAttribute("width", w );
                                var padding = parseStyleLen(colHeader.getComputedStyle("paddingLeft"));
                                padding += parseStyleLen(colHeader.getComputedStyle("paddingRight"));
                                var perWidth = w - padding + "px";
                                colHeader.setStyle("width", perWidth);
                                td.setStyle("width", perWidth);//force its width
                                //Y.log("set column header "+ idx + " width "+ perWidth);
                                calTotalWidth += td.get("offsetWidth");
                            }else if(w < w1){
                                //td.setAttribute("width", w1);
                                var padding = parseStyleLen(td.getComputedStyle("paddingLeft"));
                                padding += parseStyleLen(td.getComputedStyle("paddingRight"));
                                var perWidth = w1 - padding+"px";
                                td.setStyle("width", perWidth);
                                
                                colHeader.setStyle("width", perWidth);//force its width
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
            this.syncWidth();
            if(calTotalWidth){
                Y.log("# calTotalWidth="+ calTotalWidth);
                if(calTotalWidth > this._body.get('clientWidth')) 
                    //for firefox, if set each column's width does not actually expand the table's width
                {
                    this._body.setStyle("width", calTotalWidth+4  + "px");
                    //Y.log("synce header scroll, calTotalWidth="+ calTotalWidth+ " _body="+ this._body.get("tagName") );
                    this.headerscroll.one(">table").setStyle("width", calTotalWidth +"px");
                    //Y.log("this._bodyscroll's scrollWidth="+ this._bodyscroll.get("scrollWidth"));
                }
                if(Y.UA.ie > 0){
                    this._bodyscroll.setStyle("width", "");//fix IE's issue, force it recaculate the size of that DIV
                    this.syncWidth();
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
             var tbody = this._body.one("tbody");
             for(var i=e.index, l=e.index+e.count;i < l;i++){
                 var rowModel = this.model.getRow(i);
                 this._insertGroupLabel(tbody, i, rowModel);
                 var tr = Y.Node.create("<tr></tr>");
                 this._insertTr(tbody, i, tr);
                 tr.setAttribute("data-key", this.model.getRowId(i));
                 tr.append("<td>"+ (i+1) +"</td>");
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
                createIntervalEventChecker(50, this._syncScroll, null, this)
                
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
    */
    //var ScrollView = Y.Base.mix(Y.ScrollView, [Y.WidgetChild]);
    var ScrollableGrid = Y.Base.create("scrollgrid", PagedGrid, [Y.WidgetChild, Y.WidgetParent],
    {
        /** @attribute CONTENT_TEMPLATE */
        CONTENT_TEMPLATE: "<div></div>",
        renderUI:function(){
            var node = this.get("contentBox");
            node.set('tabIndex',0);
            this.bodyHeight = null;
            this.headerscroll = Y.Node.create("<div><table><tbody></tbody></table></div>");
            var tbtbody = this.headerscroll.one("tbody");
            this.headerscroll.addClass(this.getClassName("header-scroll"));
            this._colheaders = Y.Node.create("<tr><th>header</th></tr>");
            tbtbody.append(this._colheaders);
            
            var bodyscroll = Y.Node.create("<div><table><tbody></tbody></table></div>")
            
            this._bodyscroll = bodyscroll;
            this._body = bodyscroll.one('table');
            
            this._bodyscroll.addClass(this.getClassName("scroll"));
            this._body.addClass(this.getClassName("body"));
            
            node.append(this.headerscroll);
            node.append(this._bodyscroll);
            this._moreRowNode = Y.Node.create('<tr><td><img src="../img/nycli1.gif"></td></tr>');
            
            this._renderScrollView();
            this.rendedHasMore = false;
        },
        
        bindUI:function(){
            this.heightHandle = this.after("heightChange", this.syncHeight, this);
            this.widthHandle = this.on("widthChange", this.onWidthChange, this);
            //this._tryBindScroll();
            var tbody = this._body.one(">tbody");
            this.mouseEnterHandle = tbody.delegate('mouseenter', this._onMouseEnter, 'tr', this);
            this.mouseLeaveHandle = tbody.delegate('mouseleave', this._onMouseLeave, 'tr', this);
            this.tapHandle = tbody.delegate("tap", this._onItemTap, "tr", this);
            this.keydownHandle = this.get('contentBox').on("keydown", this._syncKeydown, this);
        },
        _unbind:function(){
            this.heightHandle.detach();
            this.widthHandle.detach();
            this.mouseEnterHandle.detach();
            this.mouseLeaveHandle.detach();
            this.tapHandle.detach();
            if(this._scrollViewHandle)
                this._scrollViewHandle.detach();
            this.keydownHandle.detach();
        },
        syncWidth:function(){
            if(this.userSetWidth && this.scrollView != null){
                this.scrollView.set('width', this.userSetWidth + this.DEF_UNIT);
            }
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
            
        },
        _renderScrollView:function(){
            var grid = this;
            this.scrollView = new MyScrollView({
                //bounceRange: 50,
                snapDuration:100, 
                //axis:'xy',
                srcNode: this._bodyscroll
                });
            this.scrollView.render();
            
            this._scrollViewHandle = this.scrollView.after('scrolling', 
                createIntervalEventChecker(200, this._syncScrolling, null, this),
                this);
        },
        _syncScrolling:function(e){
            if(e.x >=0)
                this.headerscroll.set("scrollLeft", e.x);
            
            this._syncLoadMore(e.y);
        },
        _syncLoadMore:function(scrollTop){
            if(this.rendedHasMore && 
                this.scrollContHeight - scrollTop - this.viewportHeight
            < this._moreRowNode.get("offsetHeight"))
            {
                Y.log("loading more...");
                this.fire("loadmore");
                if(this.model && !this.model.isLoading())
                    this.model.requestMore();
            }
        },
        _onItemTap: function (e){
            this._selectItemNode(e.currentTarget);
        },
        
        _selectItemNode:function(node){
            this._scrollToVisible(node);
            this.fire("itemSelected",{data:node.getAttribute("data-key")});
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
            this.scrollContHeight = this.scrollView.get('boundingBox').get('scrollHeight');
            /** @property viewportHeight*/
            this.viewportHeight = this.scrollView.get('boundingBox').get('clientHeight');
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
            Y.log(config);
            this.selectionModel = new MyGridSelModel();
            this.selChangeHandle = this.selectionModel.after("selectChange",
                function(e){this._syncButton();}, this);
        },
        renderUI:function(){
            MyEditableGrid.superclass.renderUI.apply(this, arguments);
            this.bottom = Y.Node.create('<div>&nbsp;</div>');
            this.bottom.addClass(this.getClassName('bottom'));
            if(this.get('buttonVisible')){
                this._renderDefButtons(this.bottom);
            }
            this._renderMenu();
            this._totalNode = document.createTextNode("Total:  0");
            this.bottom.append(this._totalNode);
            this.get('contentBox').append(this.bottom);
        },
        syncUI:function(){
            MyEditableGrid.superclass.syncUI.apply(this, arguments);
            this.bindAndSyncAttr('maxHeight', this._syncMaxHeightUI);
        },
        _renderMenu:function(){
            var menu = this.get("popmenu");
            if(menu == null) return;
            if(menu.length == 1){
                this._renderPopBtn(menu[0].text);
            }else if(menu.length > 1){
                this._renderPopBtn("more");
            }
        },
        _renderPopBtn:function(text){
            var node = Y.Node.create('<div>'+ text + '</div>');
            node.addClass('yui3-button');
            this.bottom.append(node);
            this.popButton = new Y.Button({
                    srcNode:node,
                    on:{
                        click: function(){
                            if(grid.popButton.get('disabled') === true)
                                return;
                            grid.popButton.set('disabled', true);
                            grid.model.deleteRows(grid.selectionModel.keyset);
                            grid.fire('menu', {src:'ui'});
                        }
                    },
                    disabled: true
            });
            this.popButton.render(this.bottom);
        },
        _renderDefButtons:function(container){
            
            var delNode = Y.Node.create('<div>Delete</div>');
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
                            grid.model.deleteRows(grid.selectionModel.keyset);
                            grid.fire('delete', {src:'ui'});
                        }
                    },
                    disabled: true
            });
            this.delBut.render(container);
            
            var addNode = Y.Node.create('<button>Add</button>');
            
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
        },
        syncHeight:function(){
            var h = this.get("height");
            //var padding = 2;
            
            
            if(Y.Lang.isNumber(h)){
                var contentBox = this.get('contentBox');
                var headerH = this._colheaders.get("offsetHeight");
                var bottomH = this.bottom.get('offsetHeight');
                var padding = parseStyleLen(contentBox.getComputedStyle("paddingTop"));
                padding += parseStyleLen(contentBox.getComputedStyle("paddingBottom"));
                Y.log("---height: "+ h+ ", header Hight="+ headerH + ", bottomH="+ bottomH);
                this._bodyscroll.setStyle("height", (h - headerH) - padding - bottomH + this.DEF_UNIT);
            }
            
            this._syncColumnsWidth();
            
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
        
        _selectItemNode:function(node){
            this._syncSelectItemUI(node);
            MyEditableGrid.superclass._selectItemNode.apply(this, arguments);
        },
        /** @param node tr
        */
        _syncSelectItemUI:function(node){

            if(this.get('singleSelection')){
                var classname = this.getClassName('s','selItem');
                this.selectionModel.selectSingle(node.getAttribute("data-key"), node);
                if(!node.hasClass(classname))
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
        _unbind:function(){
            MyEditableGrid.superclass._unbind.apply(this, arguments);
            if(this.delBut)
                this.delBut.destroy();
            if(this.selChangeHandle)
                this.selChangeHandle.detach();
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
            /** @attribute popmenu */
            popmenu:{value: []}
        }
    });
    MyEditableGrid.CSS_PREFIX = "yui3-scrollgrid";
    
    /** @class MyGridSelModel selction model for Grid
    event: selectChange
    */
    Y.MyGridSelModel = MyGridSelModel = function(){
        this.keyset = {};
        this.selectCount = 0;
    }
    MyGridSelModel.prototype = {
        selectSingle:function(key, node){
            this.clear();
            this.keyset[key] = node;
            this.selectCount = 1;
            this._fireSelectChange({src:'ui', key:key});
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
    var MyScrollView = Y.Base.create("myscrollview",Y.ScrollView, [Y.WidgetChild],{
            initializer:function(){
                //this._boundingNode = this.get("boundingBox");
                //this._contentNode = this.get("contentBox");
                this.MyScrollView_top = 0;
                this.MyScrollView_left = 0;
                this.m_bb = this.get('boundingBox');
            },
            scrollTo: function (x, y, duration, easing, node){
                var ret = MyScrollView.superclass.scrollTo.apply(this, arguments);
                this.MyScrollView_top = y;
                this.MyScrollView_left = x;
                //Y.log('scroll to '+ this.getScrollLeft()+ ','+ this.getScrollTop());
                
                this.fire('scrolling', {x:x, y:y, node:node});
                return ret;
            },
            getScrollTop:function(){
                return this.MyScrollView_top;
            },
            getScrollLeft:function(){
                return this.MyScrollView_left;
            },
            getScrollHeight:function(){
                return this.m_bb.get("scrollHeight");
            },
            refresh: function(){
                delete this._cAxis;
                this.syncUI();
            },
            getViewportHeight:function(){
                return this.m_bb.get("clientHeight");
            },
            
    });
    MyScrollView.CSS_PREFIX = 'yui3-scrollview';
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
            this.visibleHandle.detech();
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
            if(this.focusHandle == null)
                this.focusHandle = this.get('contentBox').on('tap',
                function(){
                    this.focus();
                }, this);
        },
        _unbindFocus:function(){
            if(this.focusHandle){
                this.focusHandle.detach();
                delete this.focusHandle;
            }
        },
        _bindBlur:function(){
            if(this.blurHandle == null)
                this.blurHandle = this.get('contentBox').on('tapoutside', 
                    function(){
                        this.blur();
                    }, this);
        },
        _unbindBlur:function(){
            if(this.blurHandle){
                this.blurHandle.detach();
                delete this.blurHandle;
            }
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
            this._focusedAttrHandle.detach();
        },
        },{ATTRS:{
            /**@attribute title*/
            title:{value:""}
        }});
    
    Y.mix(MyPortal.prototype, WidgetRenderTaskQ);
    Y.MyPortal = MyPortal;
    MyPortal.CSS_PREFIX = "yui3-panel";
    
    /** @class MyButtonGroup 
    */
    MyButtonGroup = Y.Base.create("mybtngroup", Y.ButtonGroup, [Y.WidgetChild], {
            initializer:function(config){
                //MyButtonGroup.superclass.initializer.apply(this,arguments);
                this._buttonMap = {};
                this._buttonView = this._defButtonView;
                this._groupId = Y.guid();
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
    
    /**@class MyTextField */
    MyTextField = Y.Base.create("mytextfield", Y.Widget, [Y.WidgetChild],
        {
            initializer:function(config){
                MyTextField.superclass.initializer.apply(this, arguments);
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
            },
            /**
            event:
                src - 'ui'
                text - text value
            */
            onEnterKey:function(func, thisObj){
                if(!this._onEnterKeySet){
                    this.invokeRendered(function(){
                        var field = this.get('contentBox').one('input[type="text"]');
                        this._keydownHandle = field.on('keypress', function(e){
                                if(e.charCode == 13){
                                    e.preventDefault();
                                    e.stopPropagation();
                                    this.fire('enterKey', {src: 'ui', text:field.get("value") });
                                }
                        }, this);
                    }, this);
                    this._onEnterKeySet = true;
                }
                this.on('enterKey', func, thisObj);
            },
            renderUI:function(){
                this.lbDom = document.createTextNode("");
                this.get('contentBox').append(this.lbDom);
                var textbox = Y.Node.create('<input type="text" value="">');
                this.get('contentBox').append(textbox);
            },
            syncInput:function(){
                var val = this.get('contentBox').one('input[type="text"]').get('value');
                Y.log(val);
                this.set('input', val,
                    {src:'ui'});
                return val;
            },
            destructor:function(){
                if(this._keydownHandle)
                    this._keydownHandle.detach();
            }
            }, {ATTRS:{
                /** @attribute required */
                'required': {value: 'false'}
            }});
    Y.mix(MyTextField.prototype, WidgetRenderTaskQ);
    Y.MyTextField = MyTextField;
        }catch(e){
            Y.log(e.stack);
        }
}, "1.0.0",{
requires:['base','overlay','node','event','panel','widget','widget-parent','widget-child',
'button','button-group','scrollview']});
