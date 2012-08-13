/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
function EnableDisable(userpermcode, permcode){
    if (userpermcode && permcode) {
        if ((userpermcode & permcode) == permcode)
            return false;
    }
    return true;
}
Wtf.onReady(function(){
    Wtf.QuickTips.init();
    if(columns == "null")
        columns = 510;
    else
        columns = parseInt(columns);
    colV = {
        "task_name": 1,
        "duration": 2,
        "start_date": 3,
        "end_date": 4,
        "predecessor": 5,
        "resources": 6,
        "notes": 7,
        "progress": 8
    };
    var pplan = new Wtf.Panel({
            region: 'center',
            id: id + "_projplanPane",
            layout: 'fit',
            autoDestroy: true,
            iconCls : "pwnd projplan",
            border: false
        })
        var viewport = new Wtf.Viewport({
            layout: 'border',
            items: [new Wtf.BoxComponent({
                region: 'north',
                el: "header"
            }), pplan]

        });
        var container = new Wtf.containerPanel({
            id: id
        });
        pplan.add(container);
        container.doLayout();
        pplan.doLayout();
        df = (df && df != 'null') ? df : -1;
});

/* Embed Plan Components*/

Wtf.containerPanel = function(config) {
    Wtf.apply(this,config);
    this.pid = this.id;
    Wtf.containerPanel.superclass.constructor.call(this, {
        layout: "border",
        id: this.id + 'projPlanCont',
        border: false,
        autoDestroy: true,
        items: [{
            region: 'center',
            width: '50%',
            id: this.pid + 'gridRegion',
            border: false,
            layout: 'fit'
        },{
            split: true,
            region: 'east',
            id: this.pid + 'chartRegion',
            width: '50%',
            border: false,
            layout: 'border',
            cls:'charteastPane',
            items:[{
                region: 'north',
                height: 33,
                id: this.pid + 'headerCont'
            },{
                region: 'center',
                layout: 'fit',
                border: false,
                id: this.pid+ 'chartCont'
            }]
        }],
        tbar: ['Quick Search: ', this.searchTask = new Wtf.form.TextField({
                id: 'searchTask',
                emptyText: "Search"
            })
        ]
    });
    this.searchTask.on('render', function() {
        this.taskDelay = new Wtf.util.DelayedTask();
        Wtf.EventManager.addListener("searchTask", 'keyup', this.searchTextOnKeyPress, this);
        this.searchTask.on('specialkey', this.searchTextOnEnter, this);
    }, this);
}

Wtf.extend(Wtf.containerPanel,Wtf.Panel,{
    initComponent: function(){
        Wtf.containerPanel.superclass.initComponent.call(this);
        this.scrollOffset = 9290;
        this.cControl = new ganttChart({
            id: this.pid + 'ganttChart',
            containerPanel: this
        });
        this.editGrid = new Wtf.GridComp({
            id: this.pid,
            containerPanel: this
        });
        this.headerPanel = new Wtf.Panel({
            id: this.pid + 'headerPanel',
            cls: 'headerPanel',
            border: false,
            autoDestroy: true,
            width: 10100
        });
        this.editGrid.on('setHeader', this.SetHeaderDate, this);
        this.editGrid.on("bodyscroll", function(scrollLeft, scrollTop) {
            if(scrollTop != 0){
                Wtf.getCmp("wraperpanel" + this.pid).body.dom.scrollTop = scrollTop;
            }
        },this);
        this.cControl.on('chartAfterRender',this.chartRender, this);                
    },    
    
    chartRender: function() {
        this.cControl.initGraphics();
        this.startDate = new Date();
        this.startDate = this.startDate.add(Date.DAY, -this.startDate.format('w'));
        this.endDate = this.startDate.add(Date.DAY, 50*7);
        this.chartRendered = true;
        this.editGrid.linkArrayObj = this.cControl.linkArrayObj;
        this.editGrid.getProjectMinMaxDate();
    },

    setHeader: function(noweeks){
        this.startDate = this.startDate.add(Date.DAY, -this.startDate.format('w'));
        this.cControl.StartDate = this.startDate;
        this.startDate = this.startDate.clearTime(false);
        this.endDate = this.endDate.clearTime(false);
        this.dt = this.startDate;
        var headerPanel = this.headerPanel;
        var d=0;
        for (var index = 0; d <= (noweeks + 1); d++, index++) {
            var header = new Wtf.Panel({
                id: 'header' + index,
                title: this.dt.format('d') + " " + this.dt.format('M') + " '" + this.dt.format('y'),
                cls: 'headerProject',
                baseCls: 'test'
            });
            headerPanel.add(header);
            this.dt = this.dt.add(Date.DAY, 7);
        }
        headerPanel.doLayout();
        this.index = index;
        this.drawToday();
    },

    drawToday: function(){
        var dateoffset = Math.abs(this.startDate.getTime() - (new Date()).getTime());
        dateoffset = Math.floor(dateoffset / 1000 / 60 / 60 / 24);
        obj = new jsGraphics(this.cControl.id);
        obj.clear();
        obj.setColor('gray');
        obj.drawLine(dateoffset*16,0,dateoffset*16,Wtf.get(this.cControl.id).getHeight());
        obj.paint();
    },

    onRender: function(config) {
        Wtf.containerPanel.superclass.onRender.call(this,config);
        this.lmask = new Wtf.LoadMask(document.body, Wtf.apply(this.loadingMask));
        this.lmask.msg = "Rendering...";
        this.lmask.show();
        var projId = this.pid;
        this.wraper = new Wtf.ScrollPanel({
            id: 'wraperpanel' + projId,
            border: false,
            autoScroll: true,
            cls: 'scrollPanel',
            items: [this.cControl]
        });
        var headerContainer = Wtf.getCmp(projId + 'headerCont');
        Wtf.getCmp(projId + 'gridRegion').add(this.editGrid);
        Wtf.getCmp(projId + 'chartCont').add(this.wraper);
        headerContainer.add(this.headerPanel);
        Wtf.getCmp(projId + 'chartRegion').doLayout();
        this.wraper.doLayout();
        this.wraper.on("bodyscroll", function(scrollLeft, scrollTop) {
//            if(scrollLeft >= this.scrollOffset){
//                this.scrollOffset += 112;
//                var header = new Wtf.Panel({
//                    id: 'header' + (++this.index),
//                    title: this.dt.format('d') + " " + this.dt.format('M') + " '" + this.dt.format('y'),
//                    cls: 'headerProject',
//                    baseCls: 'test'
//                });
//                var headerWidth = this.headerPanel.getSize().width + 112;
//                this.headerPanel.setSize(headerWidth, this.headerPanel.height);
//                this.headerPanel.add(header);
//                this.headerPanel.doLayout();
//                var chartWidth = this.cControl.getSize().width + 112;
//                this.cControl.setSize(chartWidth, this.cControl.height);
//                this.dt = this.dt.add(Date.DAY, 7);
//            }
            headerContainer.body.dom.scrollLeft = scrollLeft;
            this.editGrid.getView().scroller.dom.scrollTop = scrollTop;
        },this);
    },

    SetHeaderDate: function(beginDate, finishDate) {
        var noweeks = 89;
        this.startDate = new Date(beginDate);
        this.endDate = this.startDate.add(Date.DAY, 50*7);
        this.scopeendDate = new Date(finishDate);
        var timeDiff = finishDate.getTime() - beginDate.getTime();
        var days = Math.floor(timeDiff / (1000*60*60*24*7));
        if((days * 116) > 9976){
            this.cControl.setSize(days * 116, this.cControl.height);
            this.headerPanel.setSize(days * 116 + 14, this.headerPanel.height);
            noweeks = days;
        }
        if(this.chartRendered)
            this.setHeader(noweeks);
    },

    searchTextOnKeyPress: function(e) {
        this.taskDelay.cancel();
        if(this.searchTask.getValue() == "") {
            this.editGrid.selModel.clearSelections();
            this.wraper.scrollToTop();
            return;
        }
        this.taskDelay.delay(500, this.editGrid.searchText, this.editGrid);
    },
    
    searchTextOnEnter: function(f, e) {
        if(e.getKey() == 13) {
            if(this.searchTask.getValue() == "") {
                this.editGrid.selModel.clearSelections();
                this.wraper.scrollToTop();
                return;
            }
            this.editGrid.searchText;
        }
    }
});


function test(div, nid) {
    var pid = nid.substr(5);
    Wtf.getCmp(pid + 'projGrid').RowCollExp(div);
}

/*
===========================  GridComponent  ===========================
*/

Wtf.GridComp = function(config) {
    this.pid = config.id;
    Wtf.apply(this, config);
    this.ds_pred = null;
    this.styleClass = Wtf.data.Record.create([{
        name: 'textclass'
    },{
        name: 'imageclass'
    },{
        name: 'marginval'
    }]);

    this.Task = Wtf.data.Record.create([
        {name: 'taskid'},
        {name: 'taskname'},
        {name: 'duration',type:'string'},
        {name: 'startdate',type: 'date',dateFormat: 'Y-m-j'}, 
        {name: 'enddate',type: 'date',dateFormat: 'Y-m-j'},
        {name: 'predecessor'},
        {name: 'resourcename'},
        {name: 'parent'},
        {name: 'level',type:'int'},
        {name: 'percentcomplete'},
        {name: 'ismilestone',type: 'boolean'},
        {name: 'isparent',type: 'boolean'},
        {name: 'notes'}]);

    this.GridJsonReader = new Wtf.data.JsonReader({
        root: "data",
        id: 'task-reader'
    }, this.Task);
    
    this.dstore = new Wtf.data.Store({
        url: 'jspfiles/project/embedplan.jsp',
        baseParams: {
            projectid: this.pid,
            action: "1"
        },
        reader: this.GridJsonReader
    });
   
   this.cmodel = new Wtf.grid.ColumnModel([
        new Wtf.grid.RowNumberer(), {
            width: 25,
            dataIndex: 'notes',
            hidden: EnableDisable(columns, Math.pow(2, colV.notes)),
            renderer: ImageReturn
        },{
            dataIndex: 'taskid',
            hidden: true,
            header: 'taskid'
        },{
            header: "Task Name",
            id: 'tName' + this.pid,
            maxLength:200,
            dataIndex: 'taskname',
            width: 150,
            hidden: EnableDisable(columns, Math.pow(2, colV.task_name)),
            renderer: CustomCell
        },{
            header: "Duration",
            dataIndex: 'duration',
            align: 'right',
            hidden: EnableDisable(columns, Math.pow(2, colV.duration)),
            renderer: formatDuration
        },{
            header: "Start Date",
            dataIndex: 'startdate',
            width: 100,
            hidden: EnableDisable(columns, Math.pow(2, colV.start_date)),
            renderer: formatDate
        },{
            header: "End Date",
            dataIndex: 'enddate',
            width: 100,
            hidden: EnableDisable(columns, Math.pow(2, colV.end_date)),
            renderer: formatDate
        },{
            header: "Progress (%)",
            dataIndex: 'percentcomplete',
            hidden: EnableDisable(columns, Math.pow(2, colV.progress)),
            width: 70,
            renderer: function(val, a, rec, b){
                if(rec.data["duration"] != "" && rec.data["duration"] !== undefined){
                    if(val == "" || val === undefined)
                        val = 0;
                } else
                    val = "";
                return val;
            }
        },{
            header: "Predecessors",
            dataIndex: 'predecessor',
            hidden: EnableDisable(columns, Math.pow(2, colV.predecessor)),
            width: 70
        },{
            header: "Resource Names",
            dataIndex: 'resourcename',
            hidden: EnableDisable(columns, Math.pow(2, colV.resources)),
            width:150
    }]);
    
    function DisplayNotes() {
        return '<div height="10px" onclick="javascript:hello(this)">No..</div>';
    };
    
    function formatDate(value) {
        return value ? df && (df !== '' && isNaN(parseInt(df))) ? value.dateFormat(df) : value.dateFormat('M d, Y') : '';
    };

    function HTMLStripper(val){
        var stripTagsRE = /<\/?[^>]+>/gi;
        var str = !val ? val : String(val).replace(stripTagsRE, "");
        return str.replace(/"/g, '').trim();
    }
    
    function CustomCell(text) {
       text = HTMLStripper(text);
       return '<div id="img_div" onclick = test(this,"'+this.id+'") class="minus"> </div><div id="txtDiv" class="defaulttext" wtf:qtip="' + text + '" wtf:qtitle="Task">' + text + '</div>';
    };
    
    function ImageReturn(data) {
        if (data) {
            return "<img id='TaskNotes' style='height:12px; width:12px;'  wtf:qtitle='Notes' wtf:qtip=\""+ data +"\" src='../../images/Notes.png'></img>";
        }
    }
    
    function formatDuration(value) {
        var temp = new String(value);
        var index = temp.indexOf('h')
        if (index >= 0) {
            temp = temp.substr(0, index)
            if (temp != '') {
                if (temp == '1') 
                    return temp + ' hr';
                else 
                    return temp + ' hrs';
            }
        } else {
            temp = new String(value);
            index = temp.indexOf('d');
            if (index != -1) 
                temp = temp.substr(0, index);
            if (temp != '') {
                var idx = temp.indexOf('?');
                if (idx > 0) {
                    if (temp == '1?') 
                        return temp.substr(0, idx) + ' day?';
                    else 
                        return temp.substr(0, idx) + ' days?';
                } else {
                    if (temp == '1') 
                        return temp + ' day';
                    else 
                        return temp + ' days';
                }
            }
        }
    };
    Wtf.GridComp.superclass.constructor.call(this, {
        id: this.id + 'projGrid',
        ds: this.dstore,
        cm: this.cmodel,
        enableColumnMove: false,
        width: '100%',
        height: 7300,
        frame: true,
        scrollOffset: 0,
        selModel: new Wtf.grid.RowSelectionModel()
    });    
};

Wtf.extend(Wtf.GridComp, Wtf.grid.GridPanel, {
    
    initComponent: function() {
        Wtf.GridComp.superclass.initComponent.call(this);
        this.HrsPerDay = 8;
        this.rowexpander = false;
        this.startdate = new Date();
        this.linkArrayObj = [];
        this.NonworkWeekDays = [];
        this.HolidaysList = [];
        this.krow = 0;
        this.on('cellclick', this.Myhandler, this);
        this.dstore.on('load', this.onDataLoad, this);
        this.dstore.on('loadexception', this.onException, this);
    },
   
    onException: function(){
        msgBoxShow(55, 1);
    },
    
    afterRender: function(config) {
        Wtf.GridComp.superclass.afterRender.call(this,config);
        var scrollerDom = this.getView().scroller.dom;
        this.chartCtl = this.containerPanel.cControl;
        scrollerDom.firstChild.style.height = "7300px";
        scrollerDom.style.overflowY = "hidden";
        scrollerDom.style.overflowX = "auto";
    },
    
    getProjectMinMaxDate : function() {
        var chartControl = this.chartCtl;
        this.HolidaysList = [];
        chartControl.HolidaysList = [];
        this.NonworkWeekDays = [];
        chartControl.NonworkWeekDays = [];
        Wtf.Ajax.request({
            url: 'jspfiles/project/embedplan.jsp',
            params : {
	            projectid: this.pid,
	            action: "2",
                df: (df && df != 'null') ? df : -1
            },
            method: 'GET',
            scope :this,
            success :function(result, request) {
                var obj = null;
                var today =new Date();
                if(result.responseText.length > 0) {    
                   obj = eval('('+result.responseText+')');
                   var stdate =  new Date.parseDate(obj[0].data.mindate,"Y-m-j");
                   Wtf.get("projectname").dom.innerHTML = obj[3].name+" - Project Plan";
                   if(stdate > today)
                       stdate = today;
                   this.fireEvent('setHeader', stdate, new Date.parseDate(obj[0].data.maxdate,"Y-m-j"));
               } else {
                   this.fireEvent('setHeader',today, today);
               }  
               
               if(obj[1].companyholidays) { // company holidays
                   for(var cnt =0; cnt<obj[1].companyholidays.length;cnt++) {
                        var holiday = Date.parseDate(obj[1].companyholidays[cnt].holiday,"Y-m-d").format('d/m/Y');
                        this.HolidaysList[this.HolidaysList.length] = holiday;
                        chartControl.HolidaysList[chartControl.HolidaysList.length] = holiday;
                   }
               }
               if(obj[2].nonworkweekdays) { // project nonworking weekdays
                   for(var cnt =0; cnt<obj[2].nonworkweekdays.length;cnt++) {
                        var day = parseInt(obj[2].nonworkweekdays[cnt].day);
                        this.NonworkWeekDays[this.NonworkWeekDays.length] = day;
                        chartControl.NonworkWeekDays[chartControl.NonworkWeekDays.length] = day;
                   }
               }
               if(obj[4].df){
                   df = obj[4].df;
               }
               this.dstore.load({
                    params :{
                        start : 0,
                        limit : 51
                    },
                    add: true
                })
            }
        });
    },
    
    onDataLoad: function(store, record, option) {
        this.containerPanel.lmask.msg = "Loading Tasks...";
        this.containerPanel.lmask.show();
        var recordLength = record.length;
        var count = store.getCount();
        var chartControl = this.chartCtl;
        if(recordLength > 0) {
            chartControl.addPanelOnDataload(store, option.params.start);
            if(recordLength < option.params.limit)
                chartControl.insertPanelOnDataLoad(store.data.items[count - 1], count - 1);
            var gridData = this.dstore.data;
            var plannerView = this.getView();
            for (var cnt = option.params.start; cnt < count; cnt++) {
                var irec = gridData.items[cnt];
                if(irec.data['parent'] != '0') {
                    var parentRow = this.search(irec.data['parent']);
                    if(parentRow != null) {   
                        var parentRecord = gridData.items[parentRow];
                        if(parentRecord) {    
                            var level = parseInt(parentRecord.data['level']) + 1;
                            irec.data['level'] = level;
                            plannerView.getCell(cnt, 3).firstChild.style.marginLeft = ((level) * 20) + 'px';
                        }
                    }
                } else
                    irec.data['level'] = 0;
                if(irec.data.isparent) {
                    plannerView.getCell(cnt, 3).firstChild.firstChild.className = 'Dminus';
                    plannerView.getCell(cnt, 3).firstChild.lastChild.className = 'imgtext';
                    Wtf.get(plannerView.getRow(cnt)).addClass('parentRow');
                } else {
                    plannerView.getCell(cnt, 3).firstChild.firstChild.className = 'minus';
                    plannerView.getCell(cnt, 3).firstChild.lastChild.className = 'defaulttext';
                }
            }
            chartControl.attachlinkOnDataLoad(store, option.params.start);
            if(recordLength == option.params.limit)
                this.dstore.remove(gridData.items[count - 1]);
            var dStoreCount = this.dstore.getCount();
            for(var s = 0; s < dStoreCount; s++) {
                var tempRecord = gridData.items[s];
                var predecessor = tempRecord.data['predecessor'];
                if(predecessor.length > 0) {
                    var predArray = [];
                    var flag = false;
                    if (predecessor.indexOf(',') != -1)
                        predArray = predecessor.split(',');
                    else
                        predArray[0] = predecessor;
                    
                    var finalpredecessor ="";
                    var predArrayLength = predArray.length;
                    var tempPanel = 'Panel' + tempRecord.data['taskid'];
                    for (var k = 0; k < predArrayLength; k++) {
                        if(predArray[k].length > 5) {
                            var row = this.search(predArray[k]);
                            if(row!=null) {    
                                finalpredecessor += (row + 1) + ",";
                                flag = true;
                                var currentRecord = gridData.items[row];
                                    chartControl.checkForAttachedtaskPosition('Panel' + currentRecord.data['taskid'], tempPanel);
                                    chartControl.AttachNewlink('jg_Panel' + currentRecord.data['taskid'] + '_' + tempPanel, 'Panel' + currentRecord.data['taskid'], tempPanel, false);
                            }
                        } else
                            finalpredecessor += predArray[k] + ",";
                    }
                    if(flag) {
                        finalpredecessor = finalpredecessor.substr(0,(finalpredecessor.length-1));
                        var sc = this.backupclass(tempPanel, -1);
                        tempRecord.data.predecessor = finalpredecessor;
                        tempRecord.commit();
                        this.assignclass(tempPanel, sc);
                    }
                }
            }
        }
        if(recordLength == option.params.limit) {
            store.load({
                params :{
                    start : store.getCount(),
                    limit : 51
                },
                add: true
              }
            )
        } else {
            chartControl.reassignLinks();
            this.containerPanel.lmask.hide();
        }
        this.dragOffset = ((this.dstore.getCount() - 1) * 21) + 5;
    },
    
    
    Myhandler: function(gd, ri, ci, e) {
        this.krow = ri;
        if (this.rowexpander){
            this.rowexpander = false;
            this.CollapseExpand();
        }
    },
    
    CollapseExpand: function(){
        var maindiv = this.rowdiv.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode;
        var shiftY = 0;
        var gridData = this.dstore.data;
        var gridRow = this.krow;
        var curr = gridData.items[gridRow];
        var chartControl = this.chartCtl;
        if (this.rowdiv.className == "plus") {
            var YPos = (gridRow * 21) + 5;
            var nxt = gridData.items[gridRow + 1];
            var i = 2;
            nxt = gridData.items[gridRow + 1];
            var tempcnt = 0;
            var displayArray = [];
            var ResourceArray = [];
            ResourceArray[tempcnt] = curr.data['resourcename'];
            displayArray[tempcnt++] = curr.data['taskid'];
            while (curr.data['level'] < nxt.data['level']) {
                var nextdiv = maindiv.nextSibling;
                var nexttextdiv = nextdiv.firstChild.firstChild.firstChild.firstChild.nextSibling.nextSibling.nextSibling.firstChild.firstChild.nextSibling;
                nextdiv.style.display = "block";
                shiftY += 21;
                if (nexttextdiv.previousSibling.className == 'plus') {
                    var temp = gridData.items[gridRow + i];
                    while(nxt.data.level < temp.data.level) {
                        i++;
                        temp = gridData.items[gridRow + i];
                        nextdiv = nextdiv.nextSibling;
                        if(!temp) {
                            break;
                        }
                    }
                }
                ResourceArray[tempcnt] = nxt.data['resourcename'];
                displayArray[tempcnt++] = nxt.data['taskid'];
                maindiv = nextdiv;
                nxt = gridData.items[gridRow + i];
                if (!nxt) 
                    break;
                i++;
            }
            this.rowdiv.className = "Dminus";
            chartControl.showPanel(displayArray);
            chartControl.shiftdown(this.getSuccessorTaskIds(gridRow + i - 2), shiftY);
            this.dragOffset += shiftY;
        } else {
            var nxt = gridData.items[gridRow + 1];
            var i = 2;
            var hideArray = [];
            while (curr.data['level'] < nxt.data['level']) {
                var nextdiv = maindiv.nextSibling;
                var nexttextdiv = nextdiv.firstChild.firstChild.firstChild.firstChild.nextSibling.nextSibling.firstChild.lastChild;
                if (nextdiv.style.display == 'block' || nextdiv.style.display == '') 
                    shiftY += 21;
                nextdiv.style.display = "none";
                hideArray[i - 2] = nxt.data['taskid'];
                maindiv = nextdiv;
                nxt = gridData.items[gridRow + i];
                if (!nxt) 
                    break;
                i++;
            }
            this.rowdiv.className = "plus";
            chartControl.hidePanel(hideArray);
            chartControl.shiftup(this.getSuccessorTaskIds(gridRow + i - 2), shiftY);
            this.dragOffset -= shiftY;
        }
        chartControl.reassignLinks();
    },

    
    getSuccessorTaskIds: function(currentRow) {
        var taskIdArray = [];
        if(typeof currentRow == 'string') {
            currentRow = this.search(currentRow);
        }
        var gridData = this.dstore.data;
        var length = this.dstore.getCount();
        for (var i = currentRow + 1, j = 0; i < length; i++, j++) {
            taskIdArray[j] = gridData.items[i].data['taskid'];
        }
        return taskIdArray;
    },

    RowCollExp: function(div) {
        this.rowexpander = true;
        this.rowdiv = div;
    },

    search: function(ID) {
        var index =  this.dstore.findBy(function(record) {
            if(record.get("taskid")==ID) 
                return true;
            else 
                return false;
         });
        if(index == -1)
            return null;
        return index;
    },
    
    searchWithoutBlank: function(ID) {
        var gridStore = this.dstore;
        var index = gridStore.findBy(function(record) {
            if(record.get("taskid")==ID) 
                return true;
            else 
                return false;
        });
        if(index == -1)
            return null;
        if(index > 0) {
            var count = gridStore.getCount();
            var blank = 1;
            var plannerView = this.getView();
            for(var t = index - 1; t >= 0; t--) {
                if(gridStore.getAt(t).data.startdate == "") {
                    if(plannerView.getRow(this.search(gridStore.getAt(t).data.taskid)).style.display != "none") {
                        blank++;
                    }
                } else {
                    var tPanel = Wtf.getCmp('Panel' + gridStore.getAt(t).data.taskid);
                    if(tPanel && !tPanel.hidden) {
                        return ((tPanel.Ypos - 5) / 21) + blank;
                    }
                }
            }
        }
        return index;
    },

    backupclass: function(TaskId, rowval) {
        var plannerView = this.getView();
        if (rowval == -1) {
            rowval = this.search(TaskId.substr(5));
            var r = plannerView.getCell(rowval, 3);
            var img = r.firstChild.firstChild;
            var txt = img.nextSibling;
            var t = new this.styleClass({
                imageclass: img.className,
                marginval: txt.parentNode.style.marginLeft,
                textclass: plannerView.getRow(rowval).className
            });
            return t;
        }
        var bufferStyle = [];
        var cnt = 0;
        var nextrow = rowval;
        var gridData = this.dstore.data;
        do {
            var r = plannerView.getCell(nextrow, 3);
            var img = r.firstChild.firstChild;
            var txt = img.nextSibling;
            var t = new this.styleClass({
                imageclass: img.className,
                marginval: txt.parentNode.style.marginLeft,
                textclass: plannerView.getRow(rowval).className
            });
            bufferStyle[cnt] = t;
            cnt++;
            nextrow++;
            if (!gridData.items[nextrow])
                break;
            if (gridData.items[nextrow].data['level'] <= gridData.items[rowval].data['level'])
                break;
        }while (true);
        return bufferStyle;
    },

    assignclass: function(TaskId, style) {
        var plannerView = this.getView();
        if (typeof style == 'object') {
            var r = plannerView.getCell(this.search(TaskId.substr(5)), 3);
            var img = r.firstChild.firstChild;
            var txt = img.nextSibling;
            img.className = style.data['imageclass'];
            plannerView.getRow(this.search(TaskId.substr(5))).className = style.data['textclass'];
            txt.parentNode.style.marginLeft = style.data['marginval'];
            return;
        }

        var taskidLength = TaskId.length;
        for (var a = 0; a < taskidLength; a++) {
            var r = plannerView.getCell(style + a, 3);
            var img = r.firstChild.firstChild;
            var txt = img.nextSibling;
            img.className = TaskId[a].data['imageclass'];
            plannerView.getRow(this.search(TaskId.substr(5))).className = TaskId[a].data['textclass'];
            txt.parentNode.style.marginLeft = TaskId[a].data['marginval'];
        }
    },

    searchText: function() {
        var text = this.containerPanel.searchTask.getValue();
        var sm = this.selModel;
        sm.clearSelections();
        if(text.charAt(0) == "#" && text.length > 1) {
            var row = text.substr(1) - 1;
            if(row < this.dstore.getCount()) {
                sm.selectRow(row);
                this.containerPanel.wraper.scrollTo(Wtf.getCmp('Panel' + this.dstore.getAt(row).data.taskid).x - 16, row * 21);
            } else {
                this.containerPanel.wraper.scrollToTop();
            }
            return;
        }
        var records = this.dstore.query('taskname', text, true);
        var count = records.getCount();
        if(count < 1) {
            this.containerPanel.wraper.scrollToTop();
            return;
        }
        this.containerPanel.wraper.scrollTo(Wtf.getCmp('Panel' + records.items[0].data.taskid).x - 16, this.search(records.items[0].data.taskid) * 21);
        for(var i = 0; i < count; i++) {
            sm.selectRow(this.search(records.items[i].data.taskid), true);
        }
    },
    
    updateGridWithoutServerRequest : function(columnList, valueList, TaskId) {
        var sc = this.backupclass(TaskId, -1);
        var rowval = this.search(TaskId.substr(5));
        var record = this.dstore.data.items[rowval];
        var colListLength = columnList.length;
        for (var i = 0; i < colListLength; i++) {
            record.data[columnList[i]] = valueList[i];
        }
        record.commit();
        this.assignclass(TaskId, sc);
    }
    
});


/*
===========================   Gantt Chart  ===========================
*/
ganttChart = function(config) {
    Wtf.apply(this,config);
    this.tpl = new Wtf.XTemplate(
        '<tpl for=".">', 
        '<div class="thumb-wrap" id="{name}">', 
        '<div class="thumb"><img src="{url}" title="{name}"></div>', 
        '<span class="x-editable"></span></div>', 
        '</tpl>', 
        '<div class="x-clear"></div>'
    );
    ganttChart.superclass.constructor.call(this, {
        id: this.id,
        baseCls: 'chartPanel',
        height: 700,
        collapsible: true,
        margins: '35 5 5 0',
        cmargins: '35 5 5 0',
        cls: 'ganttChart',
        width: 10080,
        layout:'fit',
        items: [new Wtf.Panel({
            id  :"chartPanel1_"+this.id,
            cls :'DataViewPanel'
        })]
    });
};

Wtf.extend(ganttChart, Wtf.Panel, {
    initComponent: function(){
        ganttChart.superclass.initComponent.call(this);
        this.TaskminWidth = 16;
        this.NonworkWeekDays = [];
        this.HolidaysList = [];
        this.linkArrayObj = {};
        this.jg;
        this.rowCount;
        this.horiPos;
        this.StartDate = new Date();
        this.resourcePanelArray = {};
        this.addEvents({
            'chartAfterRender': true
        })
    },

    afterRender: function(config) {
        ganttChart.superclass.afterRender.call(this,config);
        this.fireEvent('chartAfterRender');
    },

    indentGrid: function(record) {
        var parentPanel = Wtf.getCmp('Panel' + record.data['taskid']);
        parentPanel.el.replaceClass('custom', 'ParentTask');
    },

    checkBlankForSubtask: function(rowval) {
        var gridStore = this.containerPanel.editGrid.dstore;
        var tempRec = gridStore.getAt(rowval + this.rowCount);
        this.rowCount++;
        if (tempRec && tempRec.data['duration'] == "") {
            do {
                tempRec = gridStore.getAt(rowval + this.rowCount);
                if(!tempRec || tempRec.data['duration'] != "")
                    break;
                this.rowCount++;
            }while(true);
        }
        return tempRec;
    },
    
    setSubtaskPosition: function(tempRec, tempPanel, parentPanel, side) {
        if(side == 'left') {
            if(document.getElementById(tempPanel.id).getAttribute('TaskType') != null)
                this.horiPos -= 5;
            tempPanel.Xpos = this.horiPos;
        } else if(side == 'right') {
            if(document.getElementById(tempPanel.id).getAttribute('TaskType') != null)
                this.horiPos += 5;
            tempPanel.Xpos = (this.horiPos + parentPanel.panelwidth) - tempPanel.panelwidth;
        }
        tempPanel.setPosition(tempPanel.Xpos, tempPanel.Ypos);
        tempPanel.updatePanel();
        this.mysubtask(tempPanel.id);
        this.RecursionFunToTraceLink(tempPanel.id);
        if(tempPanel.el.hasClass('ParentTask'))
            this.moveParentTask(tempRec);
        return tempPanel;
    },

    mysubtask: function(toTaskPanelId) {
        var toTaskRecord = this.containerPanel.editGrid.dstore.getAt(this.containerPanel.editGrid.search(toTaskPanelId.substr(5)));
        if (Wtf.get(toTaskPanelId).hasClass('ParentTask'))
            this.moveParentTask(toTaskRecord);
        else
            this.moveChildTask(toTaskRecord);
    },
    
    moveParentTask: function(toTaskParent) {
        var gridStore = this.containerPanel.editGrid.dstore;
        var parentRow = this.containerPanel.editGrid.search(toTaskParent.data['taskid']);
        var parentPanel = Wtf.getCmp('Panel' + toTaskParent.data['taskid']);
        this.horiPos = parentPanel.Xpos;
        this.rowCount = 1;
        var nextrec = this.checkBlankForSubtask(parentRow);
        if(!nextrec)
            return;
        var nextPanel = Wtf.getCmp('Panel' + nextrec.data['taskid']);
        if (nextPanel) {
            if (nextPanel.Xpos < this.horiPos && nextrec.data['startdate'] != "") {
                nextPanel = this.setSubtaskPosition(nextrec, nextPanel, parentPanel, 'left');
            } 
            if ((nextPanel.Xpos + nextPanel.panelwidth) > (this.horiPos + parentPanel.panelwidth) && nextrec.data['startdate'] != "") {
                nextPanel = this.setSubtaskPosition(nextrec, nextPanel, parentPanel, 'right');
            }
        }
        var lastnextrec = nextrec;
        nextrec = this.checkBlankForSubtask(parentRow);
        if(nextrec) {
            nextPanel = Wtf.getCmp('Panel' + nextrec.data['taskid']);
            if (nextPanel) {
                var compareLevel = parseInt(toTaskParent.data['level']) + 1;
                for (; nextrec.data['level'] >= compareLevel; this.rowCount++){
                    if (nextrec.data['level'] == compareLevel) {
                        if(document.getElementById(nextPanel.id).getAttribute('TaskType') == null) {
                            if (nextPanel.Xpos <  this.horiPos && nextrec.data['startdate'] != "") {
                            nextPanel = this.setSubtaskPosition(nextrec, nextPanel, parentPanel, 'left');
                            }
                            if ((nextPanel.Xpos + nextPanel.panelwidth) > (this.horiPos + parentPanel.panelwidth) && nextrec.data['startdate'] != "") {
                                nextPanel = this.setSubtaskPosition(nextrec, nextPanel, parentPanel, 'right');
                            }
                        } else {
                            if ((nextPanel.Xpos+5) <  this.horiPos && nextrec.data['startdate'] != "") {
                                nextPanel = this.setSubtaskPosition(nextrec, nextPanel, parentPanel, 'left');
                            }
                            if(((nextPanel.Xpos + nextPanel.panelwidth)-5) > (this.horiPos + parentPanel.panelwidth) && nextrec.data['startdate'] != "") {
                                nextPanel = this.setSubtaskPosition(nextrec, nextPanel, parentPanel, 'right');
                            }
                        }
                        
                        lastnextrec = nextrec;
                    }
                    nextrec = gridStore.getAt(parentRow + this.rowCount);
                    if (nextrec && nextrec.data['duration'] == "") {
                        do {
                            nextrec = gridStore.getAt(parentRow + this.rowCount + 1);
                            if(!nextrec || nextrec.data['duration'] != "")
                                break;
                            this.rowCount++;
                        }while(true);
                    }
                    if(!nextrec)
                        break;
                    nextPanel = Wtf.getCmp('Panel' + nextrec.data['taskid']);
                    if (!nextPanel)
                        break;
                }
            }
        }
        this.moveChildTask(lastnextrec);
    },
    
    moveChildTask: function(toTaskRecord) {
        var plannerGrid = this.containerPanel.editGrid;
        var gridStore = plannerGrid.dstore;
        var taskWidth = this.TaskminWidth;
        while (toTaskRecord.data['parent'] != 0) {
            var MinLeft = 0, MaxRight = 0;
            var recRow = plannerGrid.search(toTaskRecord.data['parent']);
            if(recRow == null) {
                plannerGrid.afterDelete(toTaskRecord.data['parent']);
                return;
            }
            var toTaskParent = gridStore.getAt(recRow);
            var parentPanel = Wtf.getCmp('Panel' + toTaskParent.data['taskid']);
            var parentRow = plannerGrid.search(toTaskParent.data['taskid']);
            var nextrec = gridStore.getAt(parentRow + 1);
            var i = 2;
            if (nextrec && nextrec.data['duration'] == "") {
                do {
                    nextrec = gridStore.getAt(parentRow + i);
                    if(!nextrec || nextrec.data['duration'] != "")
                        break;
                    i++;
                }while(true);
            }
            if(!nextrec)
                break;
            var nextPanel = Wtf.getCmp('Panel' + nextrec.data['taskid']);
            if(nextPanel) {
                if(document.getElementById(nextPanel.id).getAttribute('TaskType') == null) {
                    MinLeft = nextPanel.Xpos;
                    MaxRight = nextPanel.Xpos + nextPanel.panelwidth;
                } else
                    MinLeft = MaxRight = nextPanel.Xpos + 5;
            }    
            nextrec = gridStore.getAt(parentRow + i);
            i++;
            if (nextrec && nextrec.data['duration'] == "") {
                do {
                    nextrec = gridStore.getAt(parentRow + i);
                    if(!nextrec || nextrec.data['duration'] != "")
                        break;
                    i++;
                }while(true);
            }
            if(nextrec) {
                var compareLevel = parseInt(toTaskParent.data['level']) + 1;
                for (; nextrec.data['level'] >= compareLevel; i++) {
                    if (nextrec.data['level'] == compareLevel) {
                        var tempPanel = Wtf.getCmp('Panel' + nextrec.data['taskid']);
                        if(tempPanel) {
                            if (tempPanel.Xpos < MinLeft) {
                                MinLeft = tempPanel.Xpos;
                                if(document.getElementById(tempPanel.id).getAttribute('TaskType') != null)
                                    MinLeft += 5;
                            }                    
                            if ((tempPanel.Xpos + tempPanel.panelwidth) > MaxRight) {
                                MaxRight = tempPanel.Xpos + tempPanel.panelwidth;
                                if(document.getElementById(tempPanel.id).getAttribute('TaskType') != null)
                                    MaxRight -=5;
                            }
                        }
                    }
                    nextrec = gridStore.getAt(parentRow + i);
                    if (nextrec && nextrec.data['duration'] == "") {
                        do {
                            nextrec = gridStore.getAt(parentRow + i + 1);
                            if(!nextrec || nextrec.data['duration'] != "")
                                break;
                            i++;
                        }while(true);
                    }
                    if(!nextrec)
                        break;
                    tempPanel = Wtf.get('Panel' + nextrec.data['taskid']);
                    if (!tempPanel)
                        break;
                }
            }
            var panelWidth =  MaxRight - MinLeft;
            if(parentPanel.panelwidth != panelWidth || parentPanel.Xpos != MinLeft) {
                if(panelWidth == 0) {
                    parentPanel.el.replaceClass('custom', 'milestoneIcon');
                    parentPanel.el.dom.style.width = "10px";
                    parentPanel.panelwidth = 10;
                    parentPanel.Xpos = MinLeft - 5;
                    parentPanel.setPosition(parentPanel.Xpos, parentPanel.Ypos);
                    document.getElementById(parentPanel.id).setAttribute('TaskType', 'milestone');
                } else {
                    if(document.getElementById(parentPanel.id).getAttribute('TaskType') != null)
                        parentPanel.el.replaceClass('milestoneIcon', 'ParentTask');
                    parentPanel.setPosition(MinLeft, parentPanel.Ypos);
                    parentPanel.Xpos = MinLeft;
                    parentPanel.el.dom.style.width = panelWidth + "px";
                    parentPanel.panelwidth = panelWidth;
                }
                var stdate = new Date(this.StartDate);
                var enddate = new Date();
                stdate = stdate.add(Date.DAY, parentPanel.Xpos / taskWidth);
                var endPos = parentPanel.Xpos + parentPanel.panelwidth;
                enddate = (this.StartDate).add(Date.DAY, parseInt(endPos / taskWidth) - 1);
                if (parseFloat(endPos / taskWidth) > parseInt(endPos / taskWidth))
                    enddate = enddate.add(Date.DAY, 1);
                var duration = panelWidth / taskWidth + "";
                var nonWorkDays = parentPanel.calculatenonworkingDays(stdate, enddate);
                if (nonWorkDays != 0)
                    duration = duration - nonWorkDays + "";
                if(this.NonworkWeekDays.join().indexOf(stdate.format('w'))!=-1 || this.HolidaysList.join().indexOf(stdate.format('d/m/Y'))!=-1)
                    duration = parseFloat(duration, 10) + 1 + "";
                this.RecursionFunToTraceLink(parentPanel.id);
            }
            toTaskRecord = toTaskParent;
        }
    },

    hidePanel: function(panelArray) {
        for (var p in panelArray) {
            var tempP = Wtf.getCmp('Panel' + panelArray[p]);
            if(tempP)
                tempP.hide();
        }
    },
    
    showPanel: function(panelArray) {
        var Ypos = Wtf.getCmp('Panel' + panelArray[0]).Ypos;
        var arrLength = panelArray.length;
        for (var p = 1; p < arrLength; p++) {
            var tp = Wtf.getCmp('Panel' + panelArray[p]);
            if(tp) {
                tp.show();
                tp.Ypos = Ypos + (p * 21);
                tp.setPosition(tp.Xpos, tp.Ypos);
            }
        }
    },

    panelOver: function(taskID){
        var rowVal = this.containerPanel.editGrid.search(taskID);
        Wtf.get(this.containerPanel.editGrid.getView().getRow(rowVal)).addClass('x-grid3-row-over');
    },

    panelOut: function(taskID){
        var rowVal = this.containerPanel.editGrid.search(taskID);
        Wtf.get(this.containerPanel.editGrid.getView().getRow(rowVal)).removeClass('x-grid3-row-over');
    },

    shiftup: function(successorTasks, shiftVal) {
        var taskLength = successorTasks.length;
        for (var i = 0; i < taskLength; i++) {
            var gpanel = Wtf.getCmp('Panel' + successorTasks[i]);
            if (gpanel != null) {
                gpanel.Ypos = gpanel.Ypos - shiftVal;
                gpanel.setPosition(gpanel.Xpos, gpanel.Ypos);
            }
        }
    },

    reassignResourcePosition: function(taskId,XPos, YPos) {
        if (this.resourcePanelArray['PanelResource_' + taskId]) {
            var obj = this.resourcePanelArray['PanelResource_' + taskId];
            obj.clear();
            obj.setFont("tahoma,verdana,arial,geneva,helvetica,sans-serif", "10px", Font.BOLD);
            obj.drawString(this.containerPanel.editGrid.dstore.getAt(this.containerPanel.editGrid.search(taskId)).data["resourcename"], XPos + Wtf.getCmp('Panel' + taskId).panelwidth + 7, YPos - 5);
            obj.paint();
        }
    },
    
    shiftdown: function(successorTasks, shiftVal) {
        var taskLength = successorTasks.length;
        for (var i = 0; i < taskLength; i++) {
            var gpanel = Wtf.getCmp('Panel' + successorTasks[i]);
            if (gpanel != null) {
                gpanel.Ypos = gpanel.Ypos + shiftVal;
                gpanel.setPosition(gpanel.Xpos, gpanel.Ypos);
            }
        }
    },

    createTaskPanel: function(record) {
        var stdate = new Date(record.data['startdate']);
        var dur = record.data['duration'];
        var tPanel = new Wtf.taskPanel({
            id: 'Panel' + record.data['taskid'],
            sdate: new Date(stdate.getFullYear(), stdate.getMonth(), stdate.getDate()),
            Dur: dur,
            taskName: record.data['taskname'],
            autoDestroy: true
        }); 
        this.add(tPanel);
        this.doLayout();
        var ht = (this.containerPanel.editGrid.dstore.getCount() * 21) + 5;
        var curHt = Wtf.get("chartPanel1_"+this.id).getHeight();
        if(ht >= curHt) {
            var newHt = curHt + 210;
            this.setSize(this.width, newHt);
            Wtf.get("chartPanel1_"+this.id).setHeight(newHt);
            this.containerPanel.drawToday();
        }
        return tPanel;
    },

    addPanelOnDataload: function (Gridstore, startIndex) {
        var length = Gridstore.getCount();
        var plannerGrid = this.containerPanel.editGrid;
        for (var i = startIndex; i < length - 1; i++) {
            var record = Gridstore.getAt(i);
            if(record.data['startdate']!="" || record.data['duration']!="") {
                var tPanel = Wtf.getCmp('Panel' + record.data['taskid']);
                var row = plannerGrid.searchWithoutBlank(Gridstore.getAt(i).data.taskid);
                if (!tPanel) {
                    tPanel = this.createTaskPanel(record);
                    tPanel.setPanelPositionWidthOnDataLoad(record,(row * 21) + 5);
                }
            }
       }
    },

    insertPanelOnDataLoad : function(record,rowvalue) {
        if(record.data.startdate == "") {
            return;
        }
        var tPanel = Wtf.getCmp('Panel' + record.data['taskid']);
        var plannerGrid = this.containerPanel.editGrid;
        var row = plannerGrid.searchWithoutBlank(plannerGrid.dstore.getAt(rowvalue).data.taskid);
        if (!tPanel) {
            tPanel = this.createTaskPanel(record);
            tPanel.setPanelPositionWidthOnDataLoad(record,(row * 21) + 5);
        } else {
            tPanel.setPanelPositionWidthOnDataLoad(record,(row * 21) + 5);
            this.RecursionFunToTraceLink(tPanel.id);
        }
    },

    
    RecursionFunToTraceLink: function(ChangeTask){
        var changeTaskObj = Wtf.getCmp(ChangeTask);
        var changeTaskX = changeTaskObj.Xpos;
        var changeFlag = false;
        if(document.getElementById(ChangeTask).getAttribute('TaskType') == null) {
            changeFlag = true;
        }
        var succArray = changeTaskObj.successor;
        for (var succ in succArray) {
            var tempStr = 'jg_' + ChangeTask + '_' + succ;
            var succObj = Wtf.getCmp(succ);
            if (changeFlag) {
                if (succObj.Xpos < changeTaskX + changeTaskObj.panelwidth) {
                    if (document.getElementById(succ).getAttribute('TaskType') == null) {
                        succObj.Xpos = changeTaskX + changeTaskObj.panelwidth;
                        succObj.setPosition(succObj.Xpos);
                    } else {
                        succObj.Xpos = changeTaskX + changeTaskObj.panelwidth - 5 ;
                        succObj.setPosition(succObj.Xpos);
                    }    
                    succObj.updatePanel();
                    this.AttachNewlink(tempStr, ChangeTask, succ, false);
                    this.mysubtask(succ);
                    this.RecursionFunToTraceLink(succ);
                } else
                    this.AttachNewlink(tempStr, ChangeTask, succ, false);
            } else {
                if ((changeTaskX + changeTaskObj.panelwidth) > succObj.Xpos) {
                    if (document.getElementById(succ).getAttribute('TaskType') == null) {
                        succObj.Xpos = changeTaskX + changeTaskObj.panelwidth - 5 ;
                        succObj.setPosition(succObj.Xpos);
                    } else {
                        succObj.Xpos = changeTaskX;
                        succObj.setPosition(succObj.Xpos);
                    }
                    succObj.updatePanel();
                    this.AttachNewlink(tempStr, ChangeTask, succ, false);
                    this.mysubtask(succ);
                    this.RecursionFunToTraceLink(succ);
                } else
                    this.AttachNewlink(tempStr, ChangeTask, succ, false);
            }
        }
        var predArray = changeTaskObj.predecessor;
        for(var pred in predArray) {
            var tempStr = 'jg_' + pred + '_' + ChangeTask;
            if(changeFlag && document.getElementById(pred).getAttribute('TaskType') == null) {
                var predObj = Wtf.getCmp(pred);
                if (changeTaskX < predObj.Xpos + predObj.panelwidth) {
                    changeTaskObj.Xpos = predObj.Xpos + predObj.panelwidth;
                    changeTaskObj.setPosition(changeTaskObj.Xpos);
                    changeTaskObj.updatePanel();
                    this.mysubtask(ChangeTask);
                    this.RecursionFunToTraceLink(ChangeTask);
                } else
                    this.AttachNewlink(tempStr, pred, ChangeTask, false);
            } else
                this.AttachNewlink(tempStr, pred, ChangeTask, false);
        }
        return;
    },

    
    attachlinkOnDataLoad :function(Gridstore, startIndex) {
        var length = Gridstore.getCount();
        for (var i = startIndex; i < length; i++) {
            var irec = Gridstore.getAt(i);
            var nextrec = Gridstore.getAt(i + 1);
            if (nextrec && nextrec.data['parent'] != "0") 
                if ((irec.data['taskid']) == nextrec.data['parent']) 
                    this.indentGrid(irec);
       }
    },   

    checkForAttachedtaskPosition : function(from,to) {
        var fromObj = Wtf.getCmp(from);
        var toObj = Wtf.getCmp(to);
        if (document.getElementById(from).getAttribute('TaskType') == null) {
            if (toObj.Xpos < fromObj.Xpos + fromObj.panelwidth) {
                if (document.getElementById(to).getAttribute('TaskType') == null) {
                    toObj.Xpos = fromObj.Xpos + fromObj.panelwidth;
                    toObj.setPosition(toObj.Xpos);
                } else {
                    toObj.Xpos = fromObj.Xpos + fromObj.panelwidth - 5;
                    toObj.setPosition(toObj.Xpos);
                }    
                toObj.updatePanelOnDataLoad();
                this.mysubtask(to);
            }
        } else if ((fromObj.Xpos + fromObj.panelwidth) > toObj.Xpos) {
            if (document.getElementById(to).getAttribute('TaskType') == null) {
                toObj.Xpos = fromObj.Xpos + fromObj.panelwidth - 5;
                toObj.setPosition(toObj.Xpos);
            } else {
                toObj.Xpos = fromObj.Xpos;
                toObj.setPosition(toObj.Xpos);
            }    
            toObj.updatePanelOnDataLoad();
            this.mysubtask(to);
       }
    },
    AttachNewlink: function(LinkArraykey, FromTaskId, ToTaskId, flag) {
        var obj;
        var FromTaskObj = Wtf.getCmp(FromTaskId);
        var ToTaskObj = Wtf.getCmp(ToTaskId);
        if (this.linkArrayObj[LinkArraykey]) {
            obj = this.linkArrayObj[LinkArraykey];
            obj.clear();
        } else {
            var tempPanel = new Wtf.Panel({
                renderTo: this.id,
                baseCls: "linkPanel",
                id: LinkArraykey
            });
            obj = new jsGraphics(LinkArraykey);
            this.linkArrayObj[LinkArraykey] = obj;
            var taskLinkArray = FromTaskObj.successor;
            taskLinkArray[ToTaskId] = ToTaskId;
            taskLinkArray = ToTaskObj.predecessor;
            taskLinkArray[FromTaskId] = FromTaskId;
        }
        this.jg.clear();
        obj.setColor('gray');
        var fromTaskPageY = FromTaskObj.Ypos;
        var toTaskPageX = ToTaskObj.Xpos;
        var toTaskPageY = ToTaskObj.Ypos;
        var plannerGrid = this.containerPanel.editGrid;
        var FromTaskIndex = this.containerPanel.editGrid.search(FromTaskId.substr(5));
        var ToTaskIndex = this.containerPanel.editGrid.search(ToTaskId.substr(5));

        if (FromTaskIndex < ToTaskIndex) {
            if(ToTaskObj.el.dom.getAttribute('TaskType') == null) {
                var X1 = toTaskPageX + 3;
                var Y1 = fromTaskPageY +4;
                obj.drawLine(FromTaskObj.Xpos + FromTaskObj.panelwidth, Y1, X1, Y1 );
                obj.drawLine(X1, Y1, X1, toTaskPageY - 2);
                obj.fillPolygon(new Array(toTaskPageX-1, X1 , toTaskPageX + 7), new Array(toTaskPageY - 6, toTaskPageY - 2, toTaskPageY - 6));
            } else {
                obj.drawLine(FromTaskObj.Xpos + FromTaskObj.panelwidth, fromTaskPageY + 4, toTaskPageX + 8, fromTaskPageY + 4 );
                obj.drawLine(toTaskPageX + 8, fromTaskPageY + 4, toTaskPageX + 8, toTaskPageY - 2);
                obj.fillPolygon(new Array(toTaskPageX+4, toTaskPageX + 8, toTaskPageX + 12), new Array(toTaskPageY - 6, toTaskPageY - 2, toTaskPageY - 6));
            }
        } else {
            obj.drawLine(FromTaskObj.Xpos + FromTaskObj.panelwidth, fromTaskPageY + 4, toTaskPageX + 3, fromTaskPageY + 4);
            obj.drawLine(toTaskPageX + 3, fromTaskPageY + 4, toTaskPageX + 3, toTaskPageY + 10);
            obj.fillPolygon(new Array(toTaskPageX-1, toTaskPageX + 3, toTaskPageX + 7), new Array(toTaskPageY + 14, toTaskPageY + 10, toTaskPageY + 14));
        }
        obj.paint();
    },
    
    initGraphics: function() {
        this.jg = new jsGraphics(this.id);
    },

    reassignLinks: function() {
        var linkArray = this.linkArrayObj;
        for (var linkObj in linkArray) {
            var splitObj = linkObj.split('_');
            if(Wtf.getCmp(splitObj[1]).isVisible() && Wtf.getCmp(splitObj[2]).isVisible())
                this.AttachNewlink(linkObj, splitObj[1], splitObj[2],false);
        }
    }
});

/*
========================= TaskPanel =========================
*/

Wtf.taskPanel = function(config) {
    Wtf.apply(this,config);
    Wtf.taskPanel.superclass.constructor.call(this, {
        border: false,
        baseCls: "custom",
        id: this.id,
        autoDestroy: true
    });
};

Wtf.extend(Wtf.taskPanel, Wtf.Panel, {
    initComponent: function() {
        Wtf.taskPanel.superclass.initComponent.call(this);
        this.addEvents({
            'panelMouseUp': true,
            'panelMouseDown': true
        });
        this.startDate = this.sdate;
        this.duration = this.Dur;
        this.endDate;
        this.Xpos = 0;
        this.Ypos = 0;
        this.panelwidth = 0;
        this.TaskminWidth = 16;
        this.HrsPerDay = 8;
        this.successor = {};
        this.predecessor = {};
    },
    
    afterRender: function() {
        Wtf.taskPanel.superclass.afterRender.call(this);
        this.bwrap.dom.removeChild(this.bwrap.dom.firstChild);
        //this.el.dom.removeChild(this.el.dom.firstChild);
        var taskElement = this.el;
        this.ganttChartPanel = Wtf.getCmp(this.container.dom.offsetParent.id);
        var gridPanel = this.ganttChartPanel.containerPanel.editGrid;
        var recdata = gridPanel.dstore.getAt(gridPanel.search(this.id.substr(5))).data;
        this.data = {
            items:[{ h: 'Task', c: 'tName', d: recdata.taskname },
                { h: 'Start', c: 'tSDate', d: recdata.startdate.format('D j-m-Y') },
                { h: 'Duration', c: 'tDuration', d: recdata.duration },
                { h: 'Finish', c: 'tEDate', d: recdata.enddate.format('D j-m-Y') }]
        };
        this.tpl = new Wtf.XTemplate('<span class = "taskheader">Task</span><hr><tpl for="items"><span class="qTipHeader">{h} : </span><span class="{c}">{d}</span><br></tpl>');
        taskElement.addListener('mouseover', this.mymouseOver, this);
        taskElement.addListener('mouseout', this.mymouseOut, this);
        this.on('hide', this.hidePanel);
        this.el.dom.setAttribute("wtf:qtip", this.tpl.apply(this.data));
    },

    hidePanel: function() {
        var tempLinkArray = this.ganttChartPanel.linkArrayObj;
        var pred = this.predecessor;
        for(var predecessorId in pred) {
            var linkKey = 'jg_' + pred[predecessorId] + '_' + this.id;
            tempLinkArray[linkKey].clear();
        }
        var succ = this.successor;
        for(var successorId in succ) {
            var linkKey = 'jg_' + this.id + '_' + succ[successorId];
            tempLinkArray[linkKey].clear();
        }
    },

    setPanelWidthFromRecordDuration : function(record) {
        var localVal = record.data['duration']+'';
        var index = localVal.indexOf('h');
        var newWidth = this.panelwidth;
        if (index >= 0) {
            localVal = localVal.substr(0, index);
            if (localVal != '' && localVal != '0') {
                this.panelwidth = (parseFloat(localVal)/this.HrsPerDay) * this.TaskminWidth;
            }    
        } else {
            localVal = new String(record.data['duration']);
            index = localVal.indexOf('d');
            if (index != -1)
                localVal = localVal.substr(0, index);
            if (localVal != '' && parseFloat(localVal) != 0) {
                this.panelwidth = parseInt(parseFloat(localVal) * this.TaskminWidth);
            }    
        }
        if (parseFloat(localVal) == 0){
            this.el.replaceClass('custom', 'milestoneIcon');
            this.panelwidth = 10;
            this.Xpos -=  5;
            this.setPosition(this.Xpos, this.Ypos);
            document.getElementById('Panel' + record.data['taskid']).setAttribute('TaskType', 'milestone');
            var columnArray = [],valueArray = [];
            columnArray[0] = 'ismilestone';
            valueArray[0] = true;
            this.ganttChartPanel.containerPanel.editGrid.updateGridWithoutServerRequest(columnArray, valueArray, this.id);
        }
        this.el.dom.style.width = this.panelwidth + "px";
    },
    
    setPanelPositionWidthOnDataLoad : function(record, PosY) {
        this.startDate = new Date(record.data['startdate']);
        this.duration = record.data['duration'];
        var diff = this.timeDifference(this.startDate, this.ganttChartPanel.StartDate);    
        this.Xpos = diff * this.TaskminWidth;
        this.setPosition(this.Xpos, PosY);
        this.Ypos = PosY;
        if (this.el.hasClass('milestoneIcon')) {
            this.el.replaceClass('milestoneIcon', 'custom');
            document.getElementById(this.id).removeAttribute('TaskType');
            var columnArray = [], valueArray = [];
            columnArray[0] = 'ismilestone';
            valueArray[0] = false;
            this.ganttChartPanel.containerPanel.editGrid.updateGridWithoutServerRequest(columnArray, valueArray, this.id);
        }
        this.setPanelWidthFromRecordDuration(record);
        this.updatePanelOnDataLoad();
    },
    
    calculateEndDate : function (duration) {
        var enddate = new Date();
        var index = new String(this.duration).indexOf('h');
        if (index >= 0) {
            duration = parseFloat(duration.substr(0, index))/this.HrsPerDay;
            this.panelwidth = parseInt(duration * this.TaskminWidth);
            this.el.dom.style.width = this.panelwidth + "px";
            duration = parseFloat(duration);
            var endPos = this.Xpos + this.panelwidth;
            enddate = (this.ganttChartPanel.StartDate).add(Date.DAY, parseInt(endPos / this.TaskminWidth) - 1);
            if (parseFloat(endPos / this.TaskminWidth) > parseInt(endPos / this.TaskminWidth)) 
                enddate = enddate.add(Date.DAY, 1);
        } else {
            if ((index = (duration + '').indexOf('d')) >= 0) {
                duration = duration.substr(0, index);
            }
            duration = parseFloat(duration);
            this.panelwidth = parseInt(duration * this.TaskminWidth);
            this.el.dom.style.width = this.panelwidth + "px";
            var endPos = this.Xpos + this.panelwidth;
            enddate = (this.ganttChartPanel.StartDate).add(Date.DAY, parseInt(endPos / this.TaskminWidth) - 1);
            if (parseFloat(endPos / this.TaskminWidth) > parseInt(endPos / this.TaskminWidth)) 
                enddate = enddate.add(Date.DAY, 1);
        }
        this.duration = duration;
        return enddate;
    },
    
    forWorkingDates : function(stdate,enddate){
        var nonWorkDays = this.NonworkingDaysBetDates(stdate, enddate);
        if (nonWorkDays != 0) {
            this.panelwidth = this.panelwidth + (nonWorkDays * this.TaskminWidth);
            this.el.dom.style.width = this.panelwidth + "px";
            enddate = enddate.add(Date.DAY, nonWorkDays);
        }
        var dur = this.findWorkingStEndDate(enddate,0);
        if(dur!=0) {
            enddate = enddate.add(Date.DAY,dur);
            this.panelwidth = this.panelwidth + (dur * this.TaskminWidth);
            this.el.dom.style.width = this.panelwidth + "px";
        }
        this.endDate = enddate;
    },
    
    forNonWorkingdates : function (stdate, enddate){
        var ActualDuration = this.sdateForNonWorkCal(stdate,-1);
        if(ActualDuration !=-1)
            stdate = stdate.add(Date.DAY, ActualDuration + 1);
        else 
            ActualDuration = 0;
        if(this.panelwidth <= this.TaskminWidth)
            ActualDuration = 0;
        var nonworkingdays = this.NonworkingDaysBetDates(stdate, enddate);
        ActualDuration = ActualDuration + nonworkingdays;
        if(ActualDuration != 0) {
            this.panelwidth = this.panelwidth + (ActualDuration * this.TaskminWidth);
            this.el.dom.style.width = this.panelwidth + "px";
            enddate = enddate.add(Date.DAY, ActualDuration);
        }
        if(this.panelwidth < this.TaskminWidth) {
            enddate = this.startDate;
        }
        if(this.duration > 1) {
            var dur = this.findWorkingStEndDate(enddate,0);
            if(dur!=0) {
                enddate = enddate.add(Date.DAY,dur);
                this.panelwidth = this.panelwidth + (dur * this.TaskminWidth);
                this.el.dom.style.width = this.panelwidth + "px";
            }
        }
        this.endDate = enddate;
    },
    
    findWorkingStEndDate : function(dateVal,dur) { // initial values of dur = 0;
        var flag = true;
        while(flag) {
            if(this.ganttChartPanel.NonworkWeekDays.join().indexOf(dateVal.format('w'))!=-1 || (this.ganttChartPanel.HolidaysList.join().indexOf(dateVal.format('d/m/Y'))!=-1)) {
                dateVal = dateVal.add(Date.DAY, 1);
                dur +=1;
            }
            else 
                flag = false;
        }
        return dur;
    },
    
    sdateForNonWorkCal : function(stdate,nonworkcnt) {
        var flag = true;
        while(flag) {
            if(this.ganttChartPanel.NonworkWeekDays.join().indexOf(stdate.format('w'))!=-1 || (this.ganttChartPanel.HolidaysList.join().indexOf(stdate.format('d/m/Y'))!=-1)) {
                stdate= stdate.add(Date.DAY,1);
                nonworkcnt += 1;
            } else 
                flag = false;
        }
        return nonworkcnt;
    },
    
    updatePanelOnDataLoad :function() {
        var ganttPanel = this.ganttChartPanel;
        var stdate = new Date(ganttPanel.StartDate);
        var enddate = new Date();
        stdate = stdate.add(Date.DAY, this.Xpos / this.TaskminWidth);
        if (document.getElementById(this.id).getAttribute('TaskType') == null) {
            enddate = this.calculateEndDate(this.duration);
            if(this.ganttChartPanel.NonworkWeekDays.join().indexOf(stdate.format('w')) != -1 || (this.ganttChartPanel.HolidaysList.join().indexOf(stdate.format('d/m/Y'))!= -1)) {
                this.forNonWorkingdates(stdate,enddate);
                enddate = this.endDate;
            } else {
                this.forWorkingDates(stdate, enddate);
                enddate = this.endDate;
            }
        } else {    // for milestone 
            stdate = (ganttPanel.StartDate).add(Date.DAY, (this.Xpos + Wtf.getCmp(this.id).panelwidth) / this.TaskminWidth);
            this.endDate = stdate;
        }
        this.startDate = stdate;
        this.endDate = enddate;
    },

    mymouseOver: function(event) {
        var taskId = this.id.substr(5);
        var ganttPanel = this.ganttChartPanel;
        ganttPanel.panelOver(taskId);
        this.el.addClass('newTaskPanelStyle');
    },
    
    mymouseOut: function(event) {
        var taskId = this.id.substr(5);
        var ganttPanel = this.ganttChartPanel;
        ganttPanel.panelOut(taskId);
    },
    
    updatePanel: function() {
        var percentVal =0;
        var ganttPanel = this.ganttChartPanel;
        var stdate = new Date(ganttPanel.StartDate);
        var enddate = new Date();
        stdate = stdate.add(Date.DAY, this.Xpos / this.TaskminWidth);
        if (document.getElementById(this.id).getAttribute('TaskType') == null) {
            var dur = this.findWorkingStEndDate(stdate,0);
            if(dur!=0) {
                stdate = stdate.add(Date.DAY, dur);
                this.Xpos = this.Xpos + (dur * this.TaskminWidth);
                this.setPosition(this.Xpos, this.Ypos);
            }
            enddate = this.calculateEndDate(this.duration);
            this.forWorkingDates(stdate, enddate);
            enddate = this.endDate;
        } else {
            stdate = (ganttPanel.StartDate).add(Date.DAY, (this.Xpos + this.panelwidth) / this.TaskminWidth);
            var dur = this.findWorkingStEndDate(stdate,0);
            if(dur!=0) {
                stdate = stdate.add(Date.DAY, dur);
                this.Xpos = this.Xpos + (dur * this.TaskminWidth);
                this.setPosition(this.Xpos, this.Ypos);
            }
            enddate = stdate;
        }
        this.startDate = stdate;
        this.endDate = enddate;
    },
    
    timeDifference: function(laterdate, earlierdate) {
        var type = typeof laterdate;
        var daysDifference = 0;
        if (type == 'string') {
            var ld = new Date();
            ld = Date.parseDate(laterdate, 'd/m/Y');
            var ed = new Date();
            ed = Date.parseDate(earlierdate, 'd/m/Y');
            var difference = ld.getTime() - ed.getTime();
            daysDifference = Math.floor(difference / 1000 / 60 / 60 / 24);
        } else {
            laterdate = laterdate.clearTime(false);
            earlierdate = earlierdate.clearTime(false);
            var difference = laterdate.getTime() - earlierdate.getTime();
            daysDifference = Math.floor(difference / 1000 / 60 / 60 / 24);
        }
        return (daysDifference);
    },

    calculatenonworkingDays : function(stdate,enddate) {
        var StDate = new Date();
        var EndDate = new Date();
        if (typeof stdate == 'string') {
            var sd = Date.parseDate(stdate, 'd/m/Y');
            var ed = Date.parseDate(enddate, 'd/m/Y');
            StDate = sd.clearTime(false);
            EndDate = ed.clearTime(false);
        } else {
            StDate = stdate.clearTime(false);
            EndDate = enddate.clearTime(false);
        }
        var actualStDate = StDate;
        EndDate = EndDate.add(Date.DAY, 1);
        var NumWeeks = parseInt(this.timeDifference(EndDate, StDate) / 7, 10);
        var NonWorkingDaysBetween = NumWeeks * this.ganttChartPanel.NonworkWeekDays.length;
        StDate = StDate.add(Date.DAY, (NumWeeks * 7));
        var diff = this.timeDifference(EndDate, StDate);
        for (i = 0; i < diff; i++) {
            if(this.ganttChartPanel.NonworkWeekDays.join().indexOf(StDate.format('w'))>=0)
                NonWorkingDaysBetween += 1;
            StDate = StDate.add(Date.DAY, 1);
        }
        return NonWorkingDaysBetween + this.NumCompanyHoliday(actualStDate,EndDate.add(Date.DAY,-1));
    },

    NonworkingDaysBetDates: function(stdate, enddate) {
        var userDuration = this.timeDifference(enddate, stdate)+1;
        var NonWorkingdays = 0 ;
        var flag = true;
        while(flag) {
            var calnonwork = this.calculatenonworkingDays(stdate, enddate);
            if(NonWorkingdays != calnonwork) {
                var duration = parseInt(userDuration, 10);
                duration = (duration + calnonwork);
                enddate = stdate.add(Date.DAY, duration - 1);
                NonWorkingdays = calnonwork;
            } else
                flag = false;
        }
        return NonWorkingdays;
    },
    
    NumCompanyHoliday : function(stdate,enddate) {
        var NumHolidays = 0 ;
        for(var cnt =0; cnt< this.ganttChartPanel.HolidaysList.length;cnt++) {
            var currHoliday = Date.parseDate(this.ganttChartPanel.HolidaysList[cnt], "d/m/Y");
            if(this.ganttChartPanel.NonworkWeekDays.join().indexOf(currHoliday.format('w')) == -1) { // some Public Holidays fall on WeekEnd days, and NOT need to count.
                if(currHoliday.between(stdate,enddate)) { // Checks if this date falls on or between the given start and end dates
                    NumHolidays += 1;
                }
            }
        }
        return NumHolidays;
    }
});

Wtf.isIE8 = function(){
    if(Wtf.isIE && !Wtf.isIE6 && !Wtf.isIE7){
        return true;
    }
    return false;
}
