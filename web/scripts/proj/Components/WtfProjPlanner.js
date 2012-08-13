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
function collapseExpand(div, nid) {
    var pid = nid.substr(5);
    Wtf.getCmp(pid + 'projGrid').RowCollExp(div);
}

Wtf.EditorGridComp = function(config) {
    this.pid = config.id;
    Wtf.apply(this, config);
    this.ds_pred = null;
    this.styleClass = Wtf.data.Record.create([{
        name: 'textclass'
    },{
        name: 'imageclass'
    },{
        name: 'marginval'
    },{
        name: 'display'
    }]);

    this.Record_Resource = Wtf.data.Record.create([{
        name: 'resourcename',
        mapping: 'resourceName'
    },{
        name: 'resourceid',
        mapping: 'resourceID'
    },{
        name: 'resCost',
        mapping: 'stdRate',
        type: 'float'
    },{
        name: 'billing',
        mapping: 'billable'
    }]);

    this.ds_resource = null;
//    this.Task = Wtf.data.Record.create([
//        {name: 'taskid'},
//        {name: 'taskindex'},
//        {name: 'taskname'},
//        {name: 'duration',type:'string'},
//        {name: 'startdate',type: 'date',dateFormat: 'Y-m-j'},
//        {name: 'enddate',type: 'date',dateFormat: 'Y-m-j'},
//        {name: 'predecessor'},
//        {name: 'resourcename'},
//        {name: 'parent'},
//        {name: 'level',type:'int'},
//        {name: 'percentcomplete'},
//        {name: 'actstartdate',type: 'date',dateFormat: 'Y-m-j'},
//        {name: 'actualduration',type:'string'},
//        {name: 'ismilestone',type: 'boolean'},
//        {name: 'isparent',type: 'boolean'},
//        {name: 'priority'},
//        {name: 'notes'}]);

    this.Task = Wtf.proj.common.taskRecord;
    this.GridJsonReader = new Wtf.data.KwlJsonReader({
        root: "data",
        id: 'task-reader'
    }, this.Task);

    this.dstore = new Wtf.data.Store({
        url: Wtf.req.prj + 'task.jsp',
        baseParams: {
            projectid: this.pid,
            seq: "1"
        },
        reader: this.GridJsonReader
    });

   this.RES = Wtf.data.Record.create([
       {name: 'resourceid'},
       {name: 'nickname'},
       {name: 'resourcename'},
       {name: 'colorcode'},
       {name: 'inuseflag'}
    ]);
    this.allResources = new Wtf.data.Store({
        url: Wtf.req.prj + 'resources.jsp?action=10&projid=' + this.pid,
        reader: new Wtf.data.KwlJsonReader({
           root: "data",
           id: 'res-reader'
        },this.RES),
        sortInfo: {field:'resourcename', direction:'ASC'}
    });
    this.MSComboconfig = {
        store: this.allResources,
        displayField:'resourcename',
        valueField:'resourceid' ,
        triggerAction:'all',
        mode:'local'
    };
    this.multiCombo = new Wtf.common.Select(Wtf.applyIf({
        multiSelect:true,
        fieldLabel:WtfGlobal.getLocaleText('lang.field.text'),
        forceSelection:true
    },this.MSComboconfig));
    this.allResources.load();

    var prioData = [
        [ Wtf.proj.pr.HIGH , 'High' ],
        [ Wtf.proj.pr.MODERATE , 'Moderate' ], //change by kamlesh
        [ Wtf.proj.pr.LOW , 'Low' ]
    ];
    var prioStore = new Wtf.data.SimpleStore({
        fields: [
            {name:"priorityVal"},
            {name:"priorityName"}
        ]
    });
    this.priorityRenderer = function(combo) {
        return function(value,a,currRec) {
            var idx = combo.store.find(combo.valueField, value);
            if(idx == -1)
                return "";//false;//"";
            var rec = combo.store.getAt(idx);
            if(currRec.get('startdate'))
                return rec.get(combo.displayField);
            else
                return "";
        };
    };
    prioStore.loadData(prioData);

    this.priorityCombo = new Wtf.form.ComboBox({
        store: prioStore,
        displayField: 'priorityName',
        valueField: 'priorityVal',
        mode: 'local',
        forceSelection: true,
        editable: true,
        typeAhead:true,
        triggerAction: 'all'
    });

    this.checkListRecord = Wtf.data.Record.create([{
            name: 'checklistid',
            mapping: 'checkListID'
        },{
            name: 'checklistname',
            mapping: 'checkListName'
        },{
            name: 'checklistdesc',
            mapping: 'checkListDesc'
    }]);

    this.checkListStore = new Wtf.data.Store({
        url: '../../checklist.jsp',
        baseParams: {
            cm: 'getCheckLists'
        },
        reader: new Wtf.data.KwlJsonReader({
           root: "data"
        }, this.checkListRecord),
        sortInfo: {field:'checklistname', direction:'ASC'}
    });

    this.checkListRenderer = function(combo) {
        return function(value,a,currRec) {
            var idx = combo.store.find(combo.valueField, value);
            if(idx == -1)
                return "";//false;//"";
            var rec = combo.store.getAt(idx);
            if(currRec.get('startdate') && rec.get(combo.displayField) != '-')
                return rec.get(combo.displayField);
            else
                return "";
        };
    };

    if(WtfGlobal.getCheckListModuleStatus())
        this.checkListStore.load();
    this.checkListStore.on('load', function(store, recs){

        if(store.find('checklistname', '-') == -1){
            var r = new this.checkListRecord({
                checklistid: '-',
                checklistname: '-',
                checklistdesc: ''
            });
            store.add(r);
        }
    }, this);

    this.checkListCombo = new Wtf.form.ComboBox({
        store: this.checkListStore,
        displayField: 'checklistname',
        valueField: 'checklistid',
        mode: 'local',
        forceSelection: true,
        editable: true,
        typeAhead: true,
        lazyRender: true,
        triggerAction: 'all'
    });

    this.cmodel = new Wtf.grid.ColumnModel([
        new Wtf.grid.RowNumberer(), {
            width: 20,
            dataIndex: 'notes',
            fixed:true,
            renderer: ImageReturn
        },{
            id: 'taskid',
            dataIndex: 'taskid',
            hidden: true,
            header: 'taskid'
        },{
            header: WtfGlobal.getLocaleText('pm.common.taskname'),
            maxLength:512,
            id: 'tName' + this.pid,
            //id: 'taskname',
            dataIndex: 'taskname',
            width: 150,
            renderer: CustomCell,
            editor: new Wtf.form.TextField({
                validateOnBlur: false,
                validationDelay: 1000
            })
        },{
            header: WtfGlobal.getLocaleText('pm.common.duration'),
            dataIndex: 'duration',
            width: 50,
            id : 'duration',
            align: 'right',
            renderer: WtfGlobal.formatDuration,
            editor: new Wtf.form.TextField({
                allowNegative: false
            })
        },{
            header: WtfGlobal.getLocaleText('pm.project.calendar.newevent.startdate'),
            dataIndex: 'startdate',
            width: 70,
            id : 'startdate',
            renderer: formatDate/*,
            editor: new Wtf.form.DateField({
                format: WtfGlobal.getDateFormat()//'D j-m-Y'
            })*/
        },{
            header: WtfGlobal.getLocaleText('pm.common.enddate'),
            dataIndex: 'enddate',
            width: 70,
            id : 'enddate',
            renderer: formatDate/*,
            editor: new Wtf.form.DateField({
                format: WtfGlobal.getDateFormat(),//'D j-m-Y',
                disabledDays: [0, 6]
            })*/
        },{
            header: WtfGlobal.getLocaleText('pm.project.plan.task.attributes.progress'),
            id: 'tComplete' + this.pid,
            dataIndex: 'percentcomplete',
            width: 60,
            renderer: function(val, a, rec, b){
                if(rec.data["duration"] != "" && rec.data["duration"] !== undefined){
                    if(val == "" || val === undefined)
                        val = 0;
                } else
                    val = "";
                return val;
            },
            editor: new Wtf.form.NumberField({
                validateOnBlur: false,
                maxValue: 100,
                minValue: 0,
                nanText: WtfGlobal.getLocaleText('pm.msg.69')
//                validationDelay: 1000
            })
        },{
            header: WtfGlobal.getLocaleText('lang.priority.text'),
            dataIndex: 'priority',
            width: 70,
            id: 'priority',
            hidden: true,
            renderer: this.priorityRenderer(this.priorityCombo),
            editor: this.priorityCombo
         },{
            header: WtfGlobal.getLocaleText('pm.project.plan.info.predecessors'),
            dataIndex: 'predecessor',
            width: 70,
            id: 'predecessor',
            renderer: WtfGlobal.HTMLStripper,
            editor: new Wtf.form.TextField({
                allowNegative: false
            })
        },{
            header: WtfGlobal.getLocaleText('pm.project.plan.task.attributes.resourcenames'),
            dataIndex: 'resourcename',
            id: 'resourcename',
            width:85,
            //renderer: this.multiselectrender,
            renderer : this.comboBoxRenderer(this.multiCombo),
            editor: this.multiCombo
        },{
            header: WtfGlobal.getLocaleText('pm.admin.company.checklist.text'),
            dataIndex: 'checklist',
            id: 'checklist',
            hidden: !WtfGlobal.getCheckListModuleStatus(),
            width: 100,
            renderer: this.checkListRenderer(this.checkListCombo),
            editor: this.checkListCombo
        }]);

    function DisplayNotes() {
        return '<div height="10px" onclick="javascript:hello(this)">No..</div>';
    }

    function formatDate(value) {
         return WtfGlobal.onlyDateRenderer(value);
    }

    function CustomCell(text) {
        text = WtfGlobal.HTMLStripper(text);
        return '<div id="img_div" onclick = collapseExpand(this,"'+this.id+'") class="minus"> </div><div id="txtDiv" class="defaulttext" wtf:qtip="' + text + '" wtf:qtitle='+WtfGlobal.getLocaleText('pm.common.task')+'>' + text + '</div>';
    }

    function ImageReturn(data, md, rec, rowI, colI, store) {
        if(rec.data['taskid'] != '0'){
            if (data && data!= ""){
                    data = data.replace(/!NL!/g,"\n");
                    data = data.replace(/\n/g,"<br>");
                    return "<img src='../../images/Notes.png' id='TaskNotes_"+rec.data['taskid']+"_"+rowI+"' style='height:12px; width:12px;'></img>";
            } else if(rec.data['links'] != ""){
                return "<img src='../../images/Notes.png' id='TaskNotes_"+rec.data['taskid']+"_"+rowI+"' style='height:12px; width:12px;'></img>";
            }
        } else {
            return "";
        }
    }

    var selectionModel = new Wtf.grid.MultiSelectionModel();
    Wtf.EditorGridComp.superclass.constructor.call(this, {
        id: this.id + 'projGrid',
        ds: this.dstore,
        cm: this.cmodel,
        enableColumnHide: true,
        width: '100%',
        height: 7300,
        frame: true,
        scrollOffset: 0,
        selModel: selectionModel
    });
};

Wtf.extend(Wtf.EditorGridComp, Wtf.grid.EditorGridPanel, {

    initComponent: function() {
        Wtf.EditorGridComp.superclass.initComponent.call(this);
        this.addEvents({
            'onOutdent': true,
            'setHeader': true
        });
        this.dragFlag = false;
        this.dragOffset = 0;
        this.HrsPerDay = 8;
        this.krow = 0;
        this.edited = {};
        this.theid = 0;
        this.buffer = null;
        this.rowexpander = false;
        this.pasteFlag = false;
        this.styleArray = [];
        this.startdate = new Date();
        this.functionVisit = 0;//for ScrollToPanelOnGridRowSelect function
        this.gridRowSelect = true;// --,,--
        this.showResourceFlag =false;
        this.showTaskProgress = false;
        this.linkBufferArray = [];
        this.clientServerChange = 'server';
        this.linkArrayObj = [];
        this.parentIDPercentChange = [];
        this.circularCheckflag =false;
        this.templateBuffer = null,
        this.templateStore = null;
        this.NonworkWeekDays = [];
        this.HolidaysList = [];
        this.showPriorityFlag = false;
        this.dstore.on('load', this.onDataLoad, this);
        this.dstore.on('loadexception', this.onException, this);
        this.on('beforeedit', this.DisableCells, this);
        this.on('cellclick', this.Myhandler, this);
        if(!this.archived && (this.connstatus != 6 && this.connstatus != 7)){
            if(this.connstatus != 8)
                this.on('cellcontextmenu', this.onContextmenu, this);
            this.on('celldblclick', function(gd, ri, ci,e) {
                var taskid = gd.getStore().getAt(ri).data.taskid;
                if(ci == 0 && taskid!='0' && Wtf.getCmp('Panel'+taskid))
                    this.ShowModal();
            }, this);
            this.selModel.on('rowselect',this.handleMultiSelect,this);
            this.selModel.on('rowdeselect',this.handleMultiSelect,this);
            this.on('validateedit', this.Param, this);
        }
        if(!this.archived) {
            this.on('afteredit', this.GridAfterEdit, this);
        }
        this.on("mouseover", this.hideNotes, this);
    },

    scrollToPanelOnGridRowSelect: function(rec){
        this.functionVisit++;
        if((this.functionVisit % 2) != 0){
            var taskid = rec.data['taskid'];
            var tpanel = Wtf.getCmp('Panel' + taskid);
            if(tpanel != undefined){
                if(this.chartCtl.panelVisibleForScroll == false && this.gridRowSelect == true){
                    this.containerPanel.wraper.scrollTo(tpanel.Xpos-10, true);
                } else {
                    this.chartCtl.panelVisibleForScroll = false;
                    this.gridRowSelect = true;
                }
            }
        }
    },

    handleMultiSelect : function(sm,ri,rec){
        if(!this.viewMode){
            if(sm.getSelections().length>1) {
                if(this.connstatus != 8){
                    this.containerPanel.indentButton.disable();
                    this.containerPanel.outdentButton.disable();
                    this.containerPanel.pasteButton.disable();
                    this.containerPanel.insertTemplateButton.disable();
                    this.containerPanel.newTaskButton.disable();
                    this.containerPanel.moveUpButton.disable();
                    this.containerPanel.moveDownButton.disable();
                }
                this.containerPanel.showTaskPanel.disable();
            }else if(sm.getSelections().length==1){
                if(this.connstatus != 8){
                    this.containerPanel.indentButton.enable();
                    this.containerPanel.outdentButton.enable();
                    this.containerPanel.pasteButton.enable();
                    this.containerPanel.insertTemplateButton.enable();
                    this.containerPanel.newTaskButton.enable();
                    this.containerPanel.moveUpButton.enable();
                    this.containerPanel.moveDownButton.enable();
                }
                this.containerPanel.showTaskPanel.enable();
            }
        }
//        this.scrollToPanelOnGridRowSelect(rec);
    },
    onException: function(){
        msgBoxShow(55, 1);
        hideMainLoadMask();
    },

    afterRender: function(config) {
        Wtf.EditorGridComp.superclass.afterRender.call(this,config);
        var scrollerDom = this.getView().scroller.dom;
        this.chartCtl = this.containerPanel.cControl;
        this.containerPanel.scrollerDom = scrollerDom;
        scrollerDom.firstChild.style.height = "7300px";
        scrollerDom.style.overflowY = "hidden";
        scrollerDom.style.overflowX = "auto";
        if(!this.archived && !(this.connstatus >= 6 && this.connstatus <= 8)){
            this.keyMap = new Wtf.KeyMap(Wtf.get(this.id), [{
                key: "x",
                ctrl: true,
                scope: this,
                fn: function() {
                    this.loadbuffer("cutTask");
                    this.requestDeleteTask();
                }
            }, {
                key: "c",
                ctrl:true,
                scope: this,
                fn:function(){this.loadbuffer("copyTask");}
            }, {
                key: "v",
                ctrl:true,
                scope: this,
                fn: function(){
                    if(!this.pasteBuffer)
                        this.insertbuffer('paste');
                }
            },{
                key: Wtf.EventObject.DELETE,
                scope: this,
                ctrl:false,
                shift:false,
                handler: function() {
                    if(!Wtf.EventObject.hasModifier()) {
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.324'), function(btn){
                            if(btn == "no")
                                return;
                            else if(btn == "yes")
                                this.requestDeleteTask();
                        }, this);
                    }
                }
            }]);
        }
    },

    getProjectMinMaxDate : function() {
        var chartControl = this.chartCtl;
        this.HolidaysList = [];
        chartControl.HolidaysList = [];
        this.NonworkWeekDays = [];
        chartControl.NonworkWeekDays = [];
        Wtf.Ajax.requestEx({
            url: Wtf.req.prj + 'task.jsp',
            params : {
                projectid: this.pid,
                seq: "2"
            },
            method: 'GET'},
            this,
            function(result, request) {
               var res = result;
               if(res.length > 0) {
                   var obj = eval('(' + res + ')');
                   var stdate =  new Date.parseDate(obj[0].data.mindate,"Y-m-j H:i:s");
                   this.projStartDate = obj[0].data.startdate;
                   var today =new Date();
                   if(stdate > today)
                       stdate = today;
                   this.fireEvent('setHeader', stdate, new Date.parseDate(obj[0].data.maxdate,"Y-m-j H:i:s"));
               } else
                   this.fireEvent('setHeader', new Date(), new Date());
               if(obj[1].companyholidays) { // company holidays
                   for(var cnt =0; cnt<obj[1].companyholidays.length;cnt++) {
                        var holiday = Date.parseDate(obj[1].companyholidays[cnt].holiday,"Y-m-d").format('d/m/Y');
                        this.HolidaysList[this.HolidaysList.length] = holiday;
                        chartControl.HolidaysList[chartControl.HolidaysList.length] = holiday;
                   }
               }
               if(obj[2].nonworkweekdays) { // company holidays
                   for(cnt =0; cnt<obj[2].nonworkweekdays.length;cnt++) {
                        var day = parseInt(obj[2].nonworkweekdays[cnt].day);
                        this.NonworkWeekDays[this.NonworkWeekDays.length] = day;
                        chartControl.NonworkWeekDays[chartControl.NonworkWeekDays.length] = day;
                   }
               }
               this.setGridEditors();
               this.dstore.load({
                    params :{
                        start : 0,
                        limit : 51
                    },
                    add: true
                })
            },
            function(result, request) {
        });
    },

    setGridEditors: function(){
        var h = Wtf.util.clone(this.HolidaysList);
        for(var cnt =0; cnt<h.length;cnt++) {
            h[cnt] = Date.parseDate(h[cnt],"d/m/Y").format(WtfGlobal.getOnlyDateFormat());
        }
        h = (Wtf.isEmpty(h[0])) ? null : h;
        this.cmodel.config[6].editor = new Wtf.grid.GridEditor(new Wtf.form.DateField({
            format: WtfGlobal.getOnlyDateFormat(),
            disabledDates: h,
            readOnly:true,
            disabledDays: this.NonworkWeekDays
        }));
        this.cmodel.config[5].editor = new Wtf.grid.GridEditor(new Wtf.form.DateField({
            format: WtfGlobal.getOnlyDateFormat(),
            disabledDates: h,
            readOnly:true,
            disabledDays: this.NonworkWeekDays
        }));
        this.cmodel.config[4].editor = new Wtf.grid.GridEditor(new Wtf.form.TextField({
           allowNegative: false
        }));
        this.cmodel.config[3].editor = new Wtf.grid.GridEditor(new Wtf.form.TextField({
           validateOnBlur: false,
           validationDelay: 1000
        }));
        this.cmodel.config[7].editor = new Wtf.grid.GridEditor(new Wtf.form.NumberField({
                validateOnBlur: false,
                maxValue: 100,
                minValue: 0,
                nanText: WtfGlobal.getLocaleText('pm.msg.69')
//                validationDelay: 1000
            }));

        this.cmodel.config[8].editor = new Wtf.grid.GridEditor(this.priorityCombo);

        this.cmodel.config[9].editor = new Wtf.grid.GridEditor(new Wtf.form.TextField({
                allowNegative: false
            }));
        this.cmodel.config[10].editor = new Wtf.grid.GridEditor(this.multiCombo = new Wtf.common.Select(Wtf.applyIf({
            multiSelect:true,
            fieldLabel:WtfGlobal.getLocaleText('lang.field.text'),
            forceSelection:true
         },this.MSComboconfig)));
//        this.allResources.load();
        this.cmodel.config[10].renderer =this.comboBoxRenderer(this.multiCombo);

    },

comboBoxRenderer: function(combo) {
        return function(value,metadata,record,row,col,store) {
            if(value) {
                var resources = value.split(",");
                var nicknames = "";
                if(resources.length>0) {
                    for(var cnt=0;cnt<resources.length;cnt++) {
                        var idx = combo.store.find(combo.valueField, resources[cnt]);
                        if(idx == -1)
                            continue;
                        var rec = combo.store.getAt(idx).data;
                        if(cnt < (resources.length-1))
                            nicknames += rec.nickname +",";
                        else
                            nicknames += rec.nickname;
                    }
                    //record.set('resourcename',nicknames);
                    return nicknames;
                } else
                    return "";
            } else
                return "";
        };
    },
    onDataLoad: function(store, record, option) {
//        var lmask = this.containerPanel.loadingMask;
        showMainLoadMask(WtfGlobal.getLocaleText('pm.loadingtasks.text')+"...");
        var recordLength = record.length;
        var count = store.getCount();
        var chartControl = this.chartCtl;
        if(recordLength > 0) {
            chartControl.addPanelOnDataload(store, option.params.start);
            if(recordLength < option.params.limit)
                chartControl.insertPanelOnDataLoad(store.data.items[count - 1], count - 1);
            if(!(this.connstatus >= 6 && this.connstatus <= 7)){
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
                        } else {
                            irec.data['parent'] = "0";
                            irec.data['level'] = 0;
                            var recordTaskIdparentIdMap = {};
                            recordTaskIdparentIdMap[irec.data['taskid']] = "0";
                            this.ParentIdFieldOfRecord_onOutdent(recordTaskIdparentIdMap);
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
//            chartControl.reassignLinks();
            this.addBlankRow();
            dojo.cometd.subscribe("/projectplan/" + this.pid, this, this.updateGridFromOutside);
            this.clientServerChange = 'client';
            this.containerPanel.showState();
        }
        this.dragOffset = ((this.dstore.getCount() - 1) * 21) + 5;
        this.cmodel.config[5].editor.field.minValue = Date.parseDate(this.projStartDate, 'Y-m-j H:i:s');
        this.cmodel.config[6].editor.field.minValue = Date.parseDate(this.projStartDate, 'Y-m-j H:i:s');
    },

    importfile :function(obj){
        var win = Wtf.getCmp('uploadwindow');
        if(!win){
            var dateFormatStore = new Wtf.data.JsonStore({
                url:"../../admin.jsp?mode=25&action=0",
                root:'data',
                fields : ['id','name','dateformat', 'seppos']
            });
            dateFormatStore.load();
            var pnl = this.getImportOptionsUI(obj);
            this.UploadPanel1 = new Wtf.FormPanel({
                width:'100%',
                frame:true,
                method :'POST',
                scope: this,
                fileUpload : true,
                waitMsgTarget: true,
                items:[{
                    bodyStyle: 'padding:5px',
                    items: [{
                        layout: 'form',
                        items:[{
                            xtype : 'textfield',
                            id:'browseBttn',
                            inputType:'file',
                            labelStyle: "width:120px;",
                            fieldLabel:WtfGlobal.getLocaleText('pm.common.filename'),
                            allowBlank :false,
                            validator: (obj == "mpx") ? WtfGlobal.validateMSProjectFile : WtfGlobal.validateCSVFile,
                            invalidText: (obj == "mpx") ? WtfGlobal.getLocaleText("pm.common.validatemsp.invalidtext") : WtfGlobal.getLocaleText("pm.common.validatecsv.invalidtext"),
                            name: 'test'
                        }]
                    }]
                },{
                    border: true,
                    html: '&nbsp;',
                    cls: 'spacer'
                },{
                    layout: 'column',
                    id: 'dfComboPanel',
                    labelWidth: 5,
                    cls: 'radPanel',
                    items: [{
                        columnWidth: 0.55,
                        layout: 'fit',
                        bodyStyle: 'padding-top: 8px',
                        html: WtfGlobal.getLocaleText('pm.project.plan.import.csv.dateformat')+':'
                    },{
                        columnWidth: 0.45,
                        layout: 'fit',
                        bodyStyle: 'padding-top: 5px; height: 25px;',
                        items: [{
                            xtype: 'combo',
                            store: dateFormatStore,
                            displayField: 'name',
                            valueField: 'id',
                            id: 'dfcombo',
                            forceSelection: true,
                            typeAhead: false,
                            editable: false,
                            selectOnFocus: true,
                            hiddenName: 'dateformat',
                            mode: 'local',
                            width: 240,
                            triggerAction: 'all',
                            emptyText: WtfGlobal.getLocaleText('pm.common.dateformat.text'),
                            allowBlank: false
                        }]
                    }]
                },{
                    layout: 'column',
                    id: 'importRadio',
                    labelWidth: 5,
                    cls: 'radPanel',
                    items: [{
                        columnWidth: 0.55,
                        layout: 'fit',
                        bodyStyle: 'padding-top: 5px',
                        html: WtfGlobal.getLocaleText('pm.common.option.select')+':'
                    },{
                        columnWidth: 0.2,
                        layout: 'fit',
                        height: 25,
                        items: [{
                            xtype : 'radio',
                            boxLabel:WtfGlobal.getLocaleText('pm.project.plan.import.append'),
                            height: 18,
                            checked: false,
                            height: 18,
                            checked: false,
                            id:'ap',
                            name:'nam'
                        }]
                    },{
                        columnWidth: 0.2,
                        layout: 'fit',
                        height: 25,
                        items: [{
                            xtype : 'radio',
                            boxLabel:WtfGlobal.getLocaleText('pm.project.plan.import.overwrite'),
                            height: 18,
                            height: 18,
                            id:'ov',
                            name:'nam'
                        }]
                    }]
                }, pnl
            ]},
            this);
            this.upWin1 = new Wtf.Window({
                resizable: false,
                scope: this,
                layout: 'fit',
                modal:true,
                width: 440,
                height: Wtf.isWebKit ? 300 : 270,
                iconCls: 'iconwin',
                id: 'uploadwindow',
                title: WtfGlobal.getLocaleText('pm.project.plan.import.text'),
                items: this.UploadPanel1,
                autoDestroy: true,
                buttons: [{
                    text: WtfGlobal.getLocaleText('lang.import.text'),
                    id: 'submitPicBttn',
                    type: 'submit',
                    scope: this,
                    handler: function(){
    //                    if(obj == 'mpx')
    //                        Wtf.getCmp('dfcombo').setValue(dateFormatStore.getAt(0).get('id'));
                        if(this.UploadPanel1.form.isValid()){
                            if(obj == "mpx")
                                this.importMPXFile();
                            else
                                this.importCSVFile();
                        }
                    }
                },{
                    text:WtfGlobal.getLocaleText('lang.cancel.text'),
                    id:'canbttn1',
                    scope:this,
                    handler:function() {
                        this.upWin1.close();
                    }
                }]
            },this);
            this.upWin1.show();
            if(Wtf.subscription.proj.subModule.bsln){
                Wtf.get('ov').addListener('click', function(e, rad){
                    var cmp = Wtf.getCmp('sbpanel');
                    if(rad.checked && !cmp){
                        this.UploadPanel1.add({
                            xtype: 'panel',
                            id: 'sbpanel',
                            header: false,
                            border: false,
                            layout: 'form',
                            items:[{
                                labelStyle: "width:224px",
                                xtype : 'checkbox',
                                checked : true,
                                itemCls : obj == "mpx" ? 'importchkbox':'baselinechkbox',
                                fieldLabel :WtfGlobal.getLocaleText('pm.project.plan.import.savebaseline'),
                                id:'savebaseline'
                            }]
                        });
                    }
                    this.UploadPanel1.doLayout();
                }, this);
                Wtf.get('ap').addListener('click', function(e, rad){
                    var cmp = Wtf.getCmp('sbpanel');
                    if(rad.checked && cmp){
                        if(cmp)
                            this.UploadPanel1.remove(cmp, true);
                    }
                    this.UploadPanel1.doLayout();
                }, this);
            }
            if(obj == 'mpx'){
                this.UploadPanel1.remove(Wtf.getCmp('dfComboPanel'), true);
                this.UploadPanel1.form.remove(this.UploadPanel1.form.findField('dfcombo'), true);
            }
        }
    },

    getImportOptionsUI: function(obj) {
        var formpnl;
        if(obj == "mpx"){
            formpnl = new Wtf.Panel({
                layout: 'form',
                items:[{
                    labelStyle: "width:224px",
                    xtype: 'checkbox',
                    checked: obj == "mpx" ? true : false,
                    itemCls: 'importchkbox',
                    fieldLabel: WtfGlobal.getLocaleText('pm.project.plan.import.res'),
                    id: 'resimp' + this.id
                }]
            })
        } else {
           formpnl = new Wtf.Panel({
                layout: 'form',
                items:[{
                    layout: 'column',
                    id: 'headrMapRadio',
                    labelWidth: 5,
                    cls: 'radPanel',
                    items: [{
                        columnWidth: 0.55,
                        layout: 'fit',
                        bodyStyle: 'padding-top: 5px',
                        html: WtfGlobal.getLocaleText('pm.project.plan.import.headers')
                    },{
                        columnWidth: 0.2,
                        layout: 'fit',
                        height: 25,
                        items: [{
                            xtype: 'radio',
                            boxLabel:WtfGlobal.getLocaleText('lang.yes.text'),
                            fieldLabel: WtfGlobal.getLocaleText('pm.common.radio'),
                            height: 18,
                            checked: false,
                            id: 'yes',
                            name: 'head'
                        }]
                    },{
                        columnWidth: 0.1,
                        layout: 'fit',
                        height: 25,
                        items: [{
                            xtype: 'radio',
                            boxLabel: WtfGlobal.getLocaleText('lang.no.text'),
                            height: 18,
                            id: 'no',
                            name: 'head'
                        }]
                    }]
                }]
            })
        }
       var pnl = new Wtf.Panel({
            id: 'headerPanel',
            layout: 'form',
            items: [formpnl]
        });
        return pnl;
    },

    importMPXFile: function(){
        var parsedObject = document.getElementById('browseBttn').value;
        var extension =parsedObject.substr(parsedObject.lastIndexOf(".")+1);
        var patt1 = new RegExp("mpx","i");
        var patt2 = new RegExp("mpp","i");
        if(!Wtf.getCmp("ov").getValue() && !Wtf.getCmp("ap").getValue()){
            msgBoxShow(196, 0);
            return;
        }
        var userchoice = Wtf.getCmp("ov").getValue() == true ? 1 : 0;
        var isres = Wtf.getCmp('resimp' + this.id).getValue() == true ? 1 : 0;
        var isweek = 0;//Wtf.getCmp('weekimp' + this.id).getValue() == true ? 1 : 0;
        var isholiday = 0;//Wtf.getCmp('holimp' + this.id).getValue() == true ? 1 : 0;
        var bsln = Wtf.getCmp('savebaseline');
        if(bsln)
            bsln = bsln.getValue();
        var isbaseline = (userchoice == 1) ? bsln ? 1 : 0 : 0;
        if(patt2.test(extension)||patt1.test(extension)) {
            var type=1;
            if(patt2.test(extension))
               type=0;
            this.UploadPanel1.form.submit({
                url:'../../fileImporter.jsp?action=1&userchoice='+userchoice+'&projectid='+this.pid+'&type='+type+
                    '&isres='+isres+'&isweek='+isweek+'&isholiday='+isholiday+'&isbaseline='+isbaseline,
                waitMsg :WtfGlobal.getLocaleText("pm.importing.text")+'...',
                timeout: 120,
                scope:this,
                success: function (result, request) {
                    var obj = eval('('+request.response.responseText+')');
                    if(obj.success) {
                        this.upWin1.close();
                        var newresdata = eval('('+obj.newresource+')');
                        if(newresdata && newresdata.data.length>0) {
                            var importRes = new Wtf.MapImportResource({
                                mappingForType: 'mpp',
                                newresource : newresdata,
                                docid : obj.docid,
                                allResources :this.allResources,
                                connstatus : this.containerPanel.connstatus,
                                projectid : this.pid,
                                userchoice:userchoice,
                                type :type,
                                isbaseline :isbaseline,
                                isres : isres,
                                isweek : isweek,
                                isholiday : isholiday
                            });
                            importRes.show();
                            importRes.on("onsuccess",function(){
                                this.refreshPlan();
                            },this);
                        } else
                            this.refreshPlan();
                        if(obj.errormsg && obj.errormsg!="")
                            msgBoxShow([0,obj.errormsg], 1);
                    } else
                        msgBoxShow([0,obj.msg], 1);
                },
                failure: function ( result, request) {
                    this.upWin1.close();
                    var obj = eval('('+request.response.responseText+')');
                    msgBoxShow([WtfGlobal.getLocaleText('pm.msg.ERROR'),obj.msg], 1);
                }
            },this);
            this.upWin1.buttons[0].disable();
            this.upWin1.buttons[1].disable();
        } else
            msgBoxShow(56, 1);
    },

    importCSVFile :function(){
        var parsedObject = document.getElementById('browseBttn').value;
        if(!Wtf.getCmp("ov").getValue() && !Wtf.getCmp("ap").getValue()){
            msgBoxShow(196, 0);
            return;
        }
        if(!Wtf.getCmp("yes").getValue() && !Wtf.getCmp("no").getValue()){
            msgBoxShow(207, 0);
            return;
        }
        var userchoice = Wtf.getCmp("ov").getValue() == true ? 1 : 0;
        var includeheader = Wtf.getCmp("yes").getValue() == true ? 0 : 1;
        var bsln = Wtf.getCmp('savebaseline');
        if(bsln)
            bsln = bsln.getValue();
        var isbaseline = (userchoice == 1) ? bsln ? 1 : 0 : 0;
        var dfid = Wtf.getCmp('dfcombo').getValue();
        this.UploadPanel1.form.submit({
            timeout: 120,
            url:'../../importProjectPlanCSV.jsp?&includeheader='+includeheader+'&appendchoice='+userchoice+'&projectid='+this.pid+'&action=1&isbaseline='+isbaseline+'&realfilename='+parsedObject,
            waitMsg :'Importing...',
            scope:this,
            success: function (result, request) {
                this.upWin1.close();
                var obj = eval('('+request.response.responseText+')');
                this.showHeaderMappingWindow(obj, userchoice,includeheader, isbaseline, dfid);
            },
            failure: function ( result, request) {
                this.upWin1.close();
                msgBoxShow(268, 1);
            }
        },this);
        this.upWin1.buttons[0].disable();
        this.upWin1.buttons[1].disable();
    },


    showHeaderMappingWindow :function(res, userchoice,includeheader, isbaseline, dfid){
        var headerlist = [
            [ 0 , 'Task Name' ],
            [ 1 , 'Start Date' ],
            [ 2 , 'End Date' ],
            [ 3 , 'Percent Completed' ],
            [ 4 , 'Priority' ],
            [ 5 , 'Notes' ],
            [ 6 , 'Duration'],
            [ 7 , 'Resource Name'],
            [ 8 , 'Predecessor'],
            [ 9 , 'Parent'],
            [ 10 , '-' ]  //for extra any unmapped column
            ];
        var headerds = new Wtf.data.SimpleStore({
            fields: [
                {name:"headerindex"},
                {name:"headername"}
            ]
        });
        Wtf.ux.comboBoxRenderer = function(combo) {
            return function(value) {
                var idx = combo.store.find(combo.valueField, value);
                if(idx == -1)
                    return "-";//false;//"";
                var rec = combo.store.getAt(idx);
                return rec.get(combo.displayField);
            };
        };
        headerds.loadData(headerlist);
        var headerCombo = new Wtf.form.ComboBox({
            store: headerds,
            displayField: 'headername',
            emptyText: "<Select a column>",
            valueField: 'headerindex',
            mode: 'local',
            forceSelection: true,
            editable: true,
            typeAhead:true,
            triggerAction: 'all',
            selectOnFocus: true
        });
        var listds = new Wtf.data.JsonStore({
            fields: [{
                name:"header"
            },{
                name:"index"
            }]
        });
        listds.on('load', function(store, recs, options){
            for(var j=0;j < store.getCount();j++){
                var tempRec = store.getAt(j);
                var hcmbostore = headerCombo.store;
                var headerName = tempRec.get('header').toLowerCase();
                for(var i=0; i < hcmbostore.getCount();i++){
                    if(headerName.indexOf('resource') != -1 || headerName.indexOf('owner') != -1){
                        rec = hcmbostore.query(headerCombo.displayField, "Resource Name",true, true);
                        break;
                    } else if(headerName.indexOf('title') != -1 || headerName.indexOf('name') != -1){
                        var rec = hcmbostore.query(headerCombo.displayField, 'Task Name',true,true);
                        break;
                    }else if(headerName.indexOf('predecessor') != -1){
                        rec = hcmbostore.query(headerCombo.displayField, 'Predecessor',true,true);
                        break;
                    } else if(headerName.indexOf('parent') != -1){
                        rec = hcmbostore.query(headerCombo.displayField, 'Parent',true,true);
                        break;
                    } else if(headerName.indexOf('start') != -1 || headerName.indexOf('sdate') != -1){
                        rec = hcmbostore.query(headerCombo.displayField,'Start Date',true,true);
                        break;
                    } else if(headerName.indexOf('end') != -1 || headerName.indexOf('edate') != -1 || headerName.indexOf('due') != -1 || headerName.indexOf('finish') != -1 ){
                        rec = hcmbostore.query(headerCombo.displayField,'End Date',true,true);
                        break;
                    } else if(headerName.indexOf('dur') != -1 || headerName.indexOf('day') != -1){
                        rec = hcmbostore.query(headerCombo.displayField,'Duration',true,true);
                        break;
                    } else if(headerName.indexOf('complet') != -1 || headerName.indexOf('progress') != -1){
                        rec = hcmbostore.query(headerCombo.displayField,'Percent Complete',true,true);
                        break;
                    } else if(headerName.indexOf('priority') != -1 || headerName.indexOf('importance') != -1){
                        rec = hcmbostore.query(headerCombo.displayField,'Priority',true,true);
                        break;
                    } else if(headerName.indexOf('notes') != -1 || headerName.indexOf('summary') != -1 || headerName.indexOf('description') != -1){
                        rec = hcmbostore.query(headerCombo.displayField,'Notes',true,true);
                        break;
                    } else {
                        rec = hcmbostore.query(headerCombo.displayField,"-",true);
                    }
                }
                var indexfield = rec.items[0].data[headerCombo.valueField];
                tempRec.set('index', indexfield);
                tempRec.commit();
            }
        },
        this);
        listds.loadData(res.Headers);
        var listcm = new Wtf.grid.ColumnModel([{
            header: WtfGlobal.getLocaleText('pm.project.plan.import.csv.imported'),
            dataIndex: 'header'
        },{
            header: WtfGlobal.getLocaleText('pm.common.existingheader'),
            dataIndex: 'index',
            editor: headerCombo,
            renderer : Wtf.ux.comboBoxRenderer(headerCombo)
        }]);
        var haderMapgrid= new Wtf.grid.EditorGridPanel({
            region:'center',
            id:'headerlist' + this.id,
            clicksToEdit : 1,
            store: listds,
            cm: listcm,
            border : false,
            width: 434,
            loadMask : true,
            viewConfig: {
                forceFit:true
            }
        });
        this.headerMapWin = new Wtf.Window({
            resizable: false,
            scope: this,
            layout: 'border',
            modal:true,
            width: 400,
            height: 450,
            loadMask: true,
            iconCls: 'iconwin',
            id: 'importcsvwindow',
            title: WtfGlobal.getLocaleText('pm.project.plan.import.csv.mapcsvheaders'),
            items:[{
                region : 'north',
                height : 80,
                border : false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                html : getTopHtml(WtfGlobal.getLocaleText('pm.project.plan.import.csv.mapcsvheaders'),WtfGlobal.getLocaleText('pm.project.plan.import.csv.subheader'),"../../images/exportcsv40_52.gif")
            }, haderMapgrid],
            buttons: [{
                text: WtfGlobal.getLocaleText('lang.import.text'),
                type: 'submit',
                scope: this,
                handler: function(btn){
                    haderMapgrid.loadMask.show();
                    var mappedHeaders = '';
                    var headerArray = new Array();
                    var headerNamesArray = new Array();
                    var comboCount = headerds.getCount()-1; //mapping-combo records count escape last record for unmapped column
                    for(j=0;j<listds.getCount();j++){
                        headerArray[j] = 0;
                        headerNamesArray[j] = '';
                    }
                    for(var j=0;j<listds.getCount();j++){
                        var index = listds.getAt(j).get("index");
                        if(index < comboCount){ //consider only mapping-combo records, skip other
                            headerArray[index]=headerArray[index]+1;
                            var rec = headerCombo.store.getAt(index);
                            if(rec != undefined ){
                                mappedHeaders += "\""+rec.get(headerCombo.displayField)+"\":"+j+",";
                                headerNamesArray[index] = rec.get(headerCombo.displayField);
                            }
                        }
                    }
                    mappedHeaders = mappedHeaders.substr(0, mappedHeaders.length-1);
                    mappedHeaders = "{"+mappedHeaders+"}";
                    var mismatch = 0, headername = '';
                    for(j=0;j<comboCount;j++){  //mapping-combo record count
                        if(headerArray[j]>1){   //for one to one mapping use " != 1"
                            mismatch = 1;
                            headername = headerNamesArray[j];
                            break;
                        }
                    }
                    if(mismatch == 1){
                        haderMapgrid.loadMask.hide();
                        if(headername != '')
                            msgBoxShow([WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText({key:'pm.msg.duplicatemapping',params:headername})], 1);
                        else
                            msgBoxShow(284, 1);
                        return;
                    }
                    if(headerArray[1]==0){ //headerArray[1] -> start date mapping compulsory
                        haderMapgrid.loadMask.hide();
                        msgBoxShow(257, 1);
                        return;
                    }
                    if(headerArray[2]==0){ //headerArray[2] -> End date mapping compulsory
                        if(headerArray[6]==0){
                            haderMapgrid.loadMask.hide();
                            msgBoxShow(256,1);
                            return;
                        }
                    }

                    btn.setDisabled(true);
                    Wtf.getCmp('cancelbtn').setDisabled(true);
                    Wtf.Ajax.request({
                        method: 'POST',
                        url: '../../importProjectPlanCSV.jsp',
                        timeout: (2 * 60 * 1000),
                        params: ({
                            mappedheader : mappedHeaders,
                            append : userchoice,
                            includeheader : includeheader,
                            projectid : this.pid,
                            filename : res.FileName,
                            isbaseline : isbaseline,
                            dfid : dfid,
                            action : 2
                        }),
                        scope: this,
                        success: function(result, request){
                            haderMapgrid.loadMask.hide();
                            this.headerMapWin.close();
                            var obj = eval('('+result.responseText+')');
                            if(obj.success && obj.importResult == 'success' && obj.resources){
                                var newresdata = eval('('+obj.resources+')');
                                if(newresdata) {
                                    if(newresdata.data){
                                        newresdata.data = eval('('+newresdata.data+')');
                                        if(newresdata.data.length > 0){
                                            var importRes = new Wtf.MapImportResource({
                                                mappingForType: 'csv',
                                                newresource : newresdata,
                                                allResources :this.allResources,
                                                connstatus : this.containerPanel.connstatus,
                                                projectid : this.pid,
                                                userchoice:userchoice,
                                                isbaseline :isbaseline,
                                                docid : res.FileName
                                            });
                                            importRes.show();
                                            importRes.on("onsuccess",function(obj){
                                                obj = this.getImportStatus(obj);
                                                msgBoxShow([WtfGlobal.getLocaleText('pm.msg.SUCCESS'), obj], 0);
                                                this.refreshPlan();
                                            },this);
                                            importRes.on("onfailure",function(obj){
                                                obj = this.getImportStatus(obj);
                                                msgBoxShow(["Failed", obj], 0);
                                                this.refreshPlan();
                                            },this);
                                        } else {
                                            this.postProcessCSV(res.FileName, isbaseline);
                                        }
                                    } else {
                                        this.postProcessCSV(res.FileName, isbaseline);
                                    }
                                } else {
                                    this.postProcessCSV(res.FileName, isbaseline);
                                }
                            } else {
                                msgBoxShow(199, 1);
                                this.refreshPlan();
                            }
                        },
                        failure: function(result, req){
                            this.headerMapWin.close();
                            msgBoxShow(199, 1);
                        }
                    });
                }
            },{
                text:WtfGlobal.getLocaleText('lang.cancel.text'),
                scope:this,
                id: 'cancelbtn',
                handler:function() {
                    this.headerMapWin.close();
                    this.refreshPlan();
                }
            }]
        }),
        this.headerMapWin.show();
    },

    exportfile : function(filetype, opt, header) {
        var temp = "";
        var head = "";
        if(opt !== undefined){
            temp = "&options=" + opt;
            head = header;
        }
        Wtf.get('downloadframe').dom.src = '../../exportmpx.jsp?projectid=' + this.pid + '&filename=' + mainPanel.getActiveTab().title + "&filetype=" + filetype + temp + "&header=" + head;
    },

    postProcessCSV: function(filename, isbaseline){
        Wtf.Ajax.requestEx({
            method: 'POST',
            url: '../../importProjectPlanCSV.jsp',
            timeout: (2 * 60 * 1000),
            params: {
                projectid : this.pid,
                filename : filename,
                isbaseline : isbaseline,
                action : 4
            }
        },
        this,
        function(result, request){
            var obj = eval('('+result+')');
            obj = this.getImportStatus(obj);
            this.showImportLogMsg(WtfGlobal.getLocaleText('lang.success.text'),obj);
            this.refreshPlan();
        },
        function(result, request){
            var obj = eval('('+result+')');
            obj = this.getImportStatus(obj);
            this.showImportLogMsg(WtfGlobal.getLocaleText('lang.success.text'),obj);
//            this.refreshPlan();
        });
    },
    showImportLogMsg :  function(t,o){
       Wtf.MessageBox.show({
           title: t,
           msg: o,
           buttons: Wtf.MessageBox.OK,
           icon:Wtf.MessageBox.INFO
       });
    },
    getImportStatus: function(obj){
        var msg = '';
        if(obj.success){
            msg = obj.msg + WtfGlobal.getLocaleText({key:'pm.project.plan.import.csv.message',params:[obj.total, obj.rejected, obj.error]});
        } else {
            msg = WtfGlobal.getLocaleText('pm.project.plan.import.csv.processingproblem');
        }
        msg += WtfGlobal.getLocaleText('pm.project.plan.import.csv.importlog');
        return msg;
    },

    showNotesLinksContainPanel: function(data, e, taskid, ri){
        var tasktooltip="<div style='padding:1px 1px 1px 1px;overflow-x:auto;overflow-y:auto;border:1px solid #8DB2E3 !important;'>"+
                        "<div style='background:#E9F2FC;padding:0 5px;overflow-x:auto;overflow-y:auto;max-height:200px;font-size:11px;'>"+data+"</div>"+
                        "</div>";
        var tplRow = tasktooltip;
        if(tplRow) {
            var oldContainPane = Wtf.getCmp("containNotes");
            if(oldContainPane)      // if containpane already present then destroy it and create again as template could not be overwritten
                oldContainPane.destroy();

            var containNotes = new Wtf.Panel({
                id: "containNotes",
                frame: true,
                hideBorders: true,
                baseCls: "sddsf",
                header: false,
                headerastext: false
            });

            var oldWin1 = Wtf.getCmp("winNotes");
            if(oldWin1)             // if win1 already present then destroy it and create again
                oldWin1.destroy();

            var winWidth = 350;
            var winMaxHeight = 200;

            new Wtf.Window({
                id: "winNotes",
                width: winWidth,
                maxHeight: winMaxHeight,
                bodyStyle: 'padding-top: 5px; padding-bottom: 5px;',
                plain: true,
                shadow: true,
                header: false,
                closable: false,
                border: false,
                bodyBorder: true,
                frame: false,
                resizable : false,
                items: containNotes
            }).show();

            var tplNotes = new Wtf.Template(tplRow);
            tplNotes.compile();
            tplNotes.insertAfter("containNotes");
            if(Wtf.getCmp('winNotes')){
                if(!Wtf.get('TaskNotes_'+taskid+'_'+ri))
                    Wtf.getCmp('winNotes').destroy();
                else
                    Wtf.getCmp('winNotes').setPosition(e.getPageX(), e.getPageY());
            }

        }
    },

    hideNotes: function(event){
        var notesWin = Wtf.getCmp('winNotes');
        if(notesWin){
             notesWin.hide();
        }
    },

    Myhandler: function(gd, ri, ci, e) {
        this.krow = ri;
        if (this.rowexpander){
            this.rowexpander = false;
            this.CollapseExpand();
        }
        if (e.getTarget().className != "plus" && e.getTarget().className != "Dminus")
            this.startEditing(ri, ci);
        if(ci == 0 && gd.getSelectionModel().getSelections().length == 1) {
            var tid = gd.getSelectionModel().getSelected().data.taskid;
            if(this.containerPanel.showTaskPanel !== undefined){
                if(tid !='0' && Wtf.getCmp('Panel'+tid))
                    this.containerPanel.showTaskPanel.enable();
                else
                    this.containerPanel.showTaskPanel.disable();
            }
        }
        if(ci == 1){
            var newData = "";
            var rec = gd.getStore().getAt(ri);
            var data = rec.data['notes'];
            if(rec.data['taskid'] != '0'){
                var splitlinks = rec.data['links'].split(",");
                var link = "";
                for(var i = 0; i< splitlinks.length; i++){
                    link += "<br><a style=\"color:#083772;\" target= \"_blank\" href = \""+ splitlinks[i]+"\">"+splitlinks[i]+"</a>";
                }
                if (data){
                    if(rec.data['links'] != ""){
                        data = data.replace(/!NL!/g,"\n");
                        data = data.replace(/\n/g,"<br>");
                        newData = "<b>"+WtfGlobal.getLocaleText('pm.project.plan.task.attributes.notes')+" :</b><br>" + data + "<br><hr style=\"color:#083772;\"><b>"+WtfGlobal.getLocaleText('pm.common.links')+" :</b>" + link;

                    } else {
                        data = data.replace(/!NL!/g,"\n");
                        data = data.replace(/\n/g,"<br>");
                        newData = "<b>"+WtfGlobal.getLocaleText('pm.project.plan.task.attributes.notes')+" :</b><br>" + data;
                    }
                } else if(rec.data['links'] != ""){
                    newData = "<b>"+WtfGlobal.getLocaleText('pm.common.links')+" :</b>" + link;
                }
            }
            this.showNotesLinksContainPanel(newData, e, rec.data['taskid'], ri);
        }
    },

    onContextmenu: function(g, rindex, cindex, e) {
        this.Myhandler(g, rindex, cindex, e);
        this.getSelectionModel().selectRow(rindex);
        if(!this.viewMode){
            var menu = new Wtf.menu.Menu({
                id: 'context',
                items: [{
                    id: 'newTask',
                    icon: '../../images/New.gif',
                    text: WtfGlobal.getLocaleText('pm.project.plan.menu.newtask'),
                    scope: this,
                    handler: function insertTask(e) {
                        this.InsertTaskInGrid(rindex,'localchange');
                    }
                }, '-', {
                    id: 'cutTask',
                    icon: '../../images/Cut.gif',
                    text: WtfGlobal.getLocaleText('pm.project.plan.menu.cut'),
                    scope: this,
                    handler: function cutTask(e){
                        this.loadbuffer("cutTask");
                        this.requestDeleteTask();
                    }
                }, {
                    id: 'copyTask',
                    icon: '../../images/Copy.gif',
                    text: WtfGlobal.getLocaleText('pm.project.plan.menu.copy'),
                    scope: this,
                    handler: function copyTask(e){
                        this.loadbuffer("copyTask");
                    }
                }, {
                    id: 'pasteTask',
                    icon: '../../images/Paste.gif',
                    text: WtfGlobal.getLocaleText('pm.project.plan.menu.paste'),
                    scope: this,
                    disabled: true,
                    handler: function pasteTask(e){
                        if(!this.pasteBuffer)
                            this.insertbuffer('paste');
                    }
                }, {
                    id: 'deleteTask',
                    icon: '../../images/Delete.gif',
                    text: WtfGlobal.getLocaleText('pm.project.plan.menu.delete'),
                    scope: this,
                    handler: function deleteTask(e) {
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.324'), function(btn){
                            if(btn == "no") {
                                return;
                            } else if(btn == "yes") {
                                this.clientServerChange = 'client';
                                this.requestDeleteTask();
                            }
                        }, this);
                    }
                }, '-', {
                    id: 'indent',
                    icon: '../../images/Indent.gif',
                    text: WtfGlobal.getLocaleText('pm.project.plan.menu.indent'),
                    scope: this,
                    handler: function(){
                        this.indent();
                    }
                }, {
                    id: 'outdent',
                    icon: '../../images/Outdent.gif',
                    text: WtfGlobal.getLocaleText('pm.project.plan.menu.outdent'),
                    scope: this,
                    handler: function(){
                        this.outdent();
                    }
                }, '-', {
                    id: 'cntxtnotification',
                    iconCls: 'notification',
                    text: WtfGlobal.getLocaleText('pm.common.notifications.send'),
                    scope: this,
                    handler: function(){
                        this.containerPanel.notify();
                    }
                }, {
                    id: 'requestprogress',
                    iconCls: 'dpwnd progressicon',
                    text: WtfGlobal.getLocaleText('pm.project.plan.menu.requestprogress'),
                    scope: this,
                    handler: function(){
                        this.requestTaskProgress(g, rindex);
                    }
                }]
            });
            if (this.buffer != null && this.getSelectionModel().getSelections().length == 1)
                Wtf.getCmp('pasteTask').disabled = false;
            menu.showAt(e.getXY());
        }
        e.preventDefault();
    },

    addBlankRow: function(){
        if(!this.archived && !(this.connstatus >= 6 && this.connstatus <= 8)){
            var p = new this.Task({
                taskid: '0',
                taskname: '',
                duration: '',
                startdate: '',
                enddate: '',
                predecessor: '',
                resourcename: '',
                parent: '0',
                level: 0,
                percentcomplete: '',
                actstartdate: '',
                actualduration: '',
                ismilestone: false,
                priority: 1,
                isparent: false,
                links:'',
                notes: ''
            });
            this.dstore.insert(this.dstore.getCount(), p);
            this.edited[this.theid] = false;
        }
    },
    updateTask: function(R) {
        if(R.data['duration'] != "" || R.data['duration'] == "0")
            this.chartCtl.addPanel(R, this.search(R.data['taskid']));
    },
    checkCircularOnInOut: function(recordId, recParentId) {
        var linkDelete = false;
        var originalId = recordId;
        var rec = this.dstore.data.items[this.search(recordId)];
        var recLevel = rec.data['level'];
        var count = this.dstore.getCount();
        this.circularCheckflag = false;
        for(var t = this.krow + 1; t < count; t++) {
            if(this.linkArrayObj['jg_Panel' + recordId + '_Panel' + recParentId] || this.linkArrayObj['jg_Panel' + recParentId + '_Panel' + recordId]) {
                if(this.linkArrayObj['jg_Panel' + recordId + '_Panel' + recParentId]) {
                    recPanel = Wtf.getCmp('Panel' + recordId);
                    linkObj = 'Panel' + recParentId;
                } else {
                    recPanel = Wtf.getCmp('Panel' + recParentId);
                    linkObj = 'Panel' + recordId;
                }
                delete recPanel.successor[linkObj];
                linkDelete = true;
                break;
            }
            if(this.circularlink('Panel' + recParentId, 'Panel' + recordId, recParentId) || this.circularlink('Panel' + recordId, 'Panel' + recParentId, recParentId)) {
                msgBoxShow(66, 1);
                return true;
            }
            rec = this.dstore.data.items[t];
            if(!rec || rec.data['level'] <= recLevel) {
                recordId = originalId;
                rec = this.dstore.data.items[this.search(recordId)];
                break;
            }
            recordId = rec.data['taskid'];
        }
        for(t = this.krow + 1; t < count; t++) {
            if(this.circularlink('Panel' + recParentId, 'Panel' + recordId, recParentId) || this.circularlink('Panel' + recordId, 'Panel' + recParentId, recParentId)) {
                if(linkDelete) {
                    recPanel.successor[linkObj] = linkObj;
                    linkDelete = false;
                }
                msgBoxShow(66, 1);
                return true;
            }
            if(linkDelete) {
                recPanel.successor[linkObj] = linkObj;
                break;
            }
            rec = this.dstore.data.items[t];
            if(!rec || rec.data['level'] <= recLevel) {
                recordId = originalId;
                break;
            }
            recordId = rec.data['taskid'];
        }
        return false;
    },

    deleteLinkOnInOut: function(recordId) {
        var recRow = this.search(recordId);
        var level = this.dstore.data.items[recRow].data['level'];
        var count = this.dstore.getCount();
        var chartControl = this.chartCtl;
        for(var t = recRow + 1; t < count; t++) {
            var taskPanel = Wtf.getCmp('Panel' + recordId);
            if(taskPanel !== undefined){
                var predArray = taskPanel.predecessor;
                var succArray = taskPanel.successor;
                for(var pred in predArray) {
                    if(this.checkForParentToSubtaskLink(pred, 'Panel' + recordId)) {
                        chartControl.deleteLink('jg_' + pred + '_Panel' + recordId, false);
                        this.add_deleteLinkFromDB(pred.substr(5), recordId, "delete");
                    }
                }
                for(var succ in succArray) {
                    if(this.checkForParentToSubtaskLink(succ, 'Panel' + recordId)) {
                        chartControl.deleteLink('jg_Panel' + recordId + '_' + succ, false);
                        this.add_deleteLinkFromDB(recordId,succ.substr(5), "delete");
                    }
                }
                record = this.dstore.data.items[t];
                if(!record || record.data['level'] <= level)
                    break;
                else
                    recordId = record.data['taskid'];
            }
        }
    },

    checkIfLinkExists: function(task) {
        for(pred in Wtf.getCmp(task).predecessor)
            return true;
        return false;
    },

    outdent: function() {
        var gridData = this.dstore.data;
        var gridRow = this.krow;
        var record = gridData.items[gridRow];
        if(record.data['duration'] != "") {
            var oldParent = record.data["parent"];
            var chartControl = this.chartCtl;
            var recordTaskIdparentIdMap = {};
            if (record == null || parseInt(record.data['level']) == 0)
                return;
            var prevrec = gridData.items[gridRow - 1];
            var nextrec = gridData.items[gridRow + 1];
            var temprec = prevrec;
            for(var p = gridRow - 2; temprec.data['level'] != record.data['level'] - 1; p--) {
                temprec = gridData.items[p];
                if(!temprec)
                    break;
            }
            if(temprec.data['parent'] != "0") {
                if(this.checkCircularOnInOut(record.data['taskid'], temprec.data['parent']))
                    return;
            }
            var recParent;
            if (record.data['parent'] != '0') {
                var moreChildFlag = false;
                var parentrow = this.search(record.data['parent']);
                var parentrec = gridData.items[parentrow];
                temprec = gridData.items[parentrow + 1];
                if(parentrec.data['level'] <= temprec.data['level']) {
                    if (parentrec.data['taskid'] == temprec.data['parent'] && temprec != record && temprec.data['duration']!="")
                        moreChildFlag = true;
                }
                if (!moreChildFlag) {
                    parentrec.data.isparent = false;
                    if(parentrec.data["predecessor"] == ""){
                        var sc = this.backupclass('Panel' + parentrec.data['taskid'], -1);
                        parentrec.set('startdate', parentrec.data['actstartdate']);
                        if(parentrec.data['actualduration']!='')
                            parentrec.set('duration', parentrec.data['actualduration']);
                        chartControl.insertPanelOnDataLoad(parentrec,parentrow);
                        parentrec.set('enddate', Wtf.getCmp("Panel"+parentrec.data['taskid']).endDate);
                        this.assignclass('Panel' + parentrec.data['taskid'], sc);
                    }
                    if(this.clientServerChange=='client')
                        this.updateDB(parentrec);
                }
                if(!this.checkIfLinkExists('Panel' + record.data['taskid'])) {
                    var sc1 = this.backupclass('Panel' + record.data['taskid'], -1);
                    record.set('startdate', record.data['actstartdate']);
                    if(record.data['actualduration']!='')
                        record.set('duration', record.data['actualduration']);
                    chartControl.insertPanelOnDataLoad(record,this.search(record.data['taskid']));
                    record.set('enddate', Wtf.getCmp("Panel"+record.data['taskid']).endDate);
                    this.assignclass('Panel' + record.data['taskid'], sc1);
                    if(this.clientServerChange=='client')
                        this.updateDB(record);
                }
            }
            record.data['level'] = parseInt(record.data['level']) - 1;
            var plannerView = this.getView();
            var ttarget = plannerView.getCell(gridRow, 3).firstChild.firstChild;
            var ml = new String(ttarget.parentNode.style.marginLeft);
            var offset = ml.length - 2;
            ml = ml.substr(0, offset);
            var mlval = parseInt(ml) - 20;
            if (mlval < 0)
                mlval = 20;
            ml = new String(mlval) + 'px';
            ttarget.parentNode.style.marginLeft = ml;

            if (record.data['level'] == prevrec.data['level'] || prevrec.data['duration']=="") {
                var emptyFlag = true;
                for (var s = 2; prevrec.data['taskid'] != record.data['parent'] || prevrec.data['duration'] == ""; s++) {
                    if (prevrec.data['duration'] != "") {
                        emptyFlag = false;
                        break;
                    } else
                        prevrec.set('level', prevrec.data['level'] - 1);
                    prevrec = gridData.items[gridRow - s];
                    if (!prevrec)
                        break;
                }
                if (emptyFlag) {
                    plannerView.getCell(parentrow, 3).firstChild.firstChild.className = 'minus';
                    plannerView.getCell(parentrow, 3).firstChild.lastChild.className = 'defaulttext';
                    if(Wtf.get(plannerView.getRow(parentrow)).hasClass('parentRow'))
                        Wtf.get(plannerView.getRow(parentrow)).removeClass('parentRow');
                    ttarget.parentNode.style.marginLeft = parseInt(record.data['level']) * 20 + 'px';
                    recParent = gridData.items[parentrow];
                }
                for (var i = this.search(prevrec.data['taskid']) + 1;; i++) {
                    tempRec = gridData.items[i];
                    if (tempRec.data['duration'] != "")
                        break;
                    tempRec.set('parent', prevrec.data['parent']);
                    recordTaskIdparentIdMap[tempRec.data['taskid']] = prevrec.data['parent'];
                }
                record.data['parent'] = prevrec.data['parent'];
            } else {
                for (i = 1; record.data['level'] < prevrec.data['level']; i++)
                    prevrec = gridData.items[gridRow - i];
                record.data['parent'] = prevrec.data['parent'];
            }

            if (nextrec) {
                if (record.data['level'] >= nextrec.data['level']) {
                    ttarget.className = 'minus';
                    record.data.isparent = false;
                } else {
                    if (ttarget.className != "plus") {
                        ttarget.nextSibling.className = 'imgtext';
                        Wtf.get(plannerView.getRow(gridRow)).addClass('parentRow');
                        ttarget.className = 'Dminus';
                        record.data.isparent = true;
                    }
                    nextrec.data['parent'] = record.data['taskid'];
                    recordTaskIdparentIdMap[nextrec.data['taskid']] = record.data['taskid'];
                    for (i = 2; record.data['level'] != nextrec.data['level']; i++) {
                        morenextrec = gridData.items[gridRow + i];
                        if (!morenextrec)
                            break;
                        if (morenextrec.data['level'] == parseInt(record.data['level']) + 1) {
                            morenextrec.data['parent'] = nextrec.data['parent'];
                            recordTaskIdparentIdMap[morenextrec.data['taskid']] = nextrec.data['parent'];
                            nextrec = morenextrec;
                        } else if (morenextrec.data['level'] == parseInt(record.data['level']))
                            break;
                    }
                }
                nextrec = gridData.items[gridRow + 1];
                for (var j = 2; record.data['level'] < parseInt(nextrec.data['level']) - 1; j++) {
                    nextrec.data['level'] = parseInt(nextrec.data['level']) - 1;
                    var ttarget1 = plannerView.getCell(gridRow + j - 1, 3).firstChild.firstChild;
                    var ml1 = new String(ttarget1.parentNode.style.marginLeft);
                    var offset1 = ml1.length - 2;
                    ml1 = ml1.substr(0, offset1);
                    var mlval1 = parseInt(ml1) - 20;
                    if (mlval1 < 0)
                        mlval1 = 20;
                    ml1 = new String(mlval1) + 'px';
                    ttarget1.parentNode.style.marginLeft = ml1;
                    var morenextrec = gridData.items[gridRow + j];
                    if (!morenextrec)
                        break;
                    if (nextrec.data['level'] >= parseInt(morenextrec.data['level']) - 1) {
                        ttarget1.className = 'minus';
                        this.dstore.getAt(gridRow + j - 1).data.isparent = false;
                    } else {
                        Wtf.get(plannerView.getRow(gridRow + j - 1)).addClass('parentRow');
                        ttarget1.nextSibling.className = 'imgtext';
                        this.dstore.getAt(gridRow + j - 1).data.isparent = true;
                    }
                    nextrec = gridData.items[gridRow + j];
                    if (!nextrec)
                        break;
                }
            }
            this.deleteLinkOnInOut(record.data['taskid']);
            if (moreChildFlag)
                chartControl.mysubtask('Panel' + parentrec.data['taskid']);
            //chartControl.mysubtask('Panel' + record.data['taskid']);
            this.fireEvent('onOutdent', record, recParent);
            if(oldParent != 0 || oldParent != "0")
                this.setParentCompletion(oldParent);
            if(record.data["parent"] != 0 || record.data["parent"] != "0")
                this.setParentCompletion(record.data["parent"]);
            if(this.clientServerChange=='client') {
                this.parentIDPercentChange[this.parentIDPercentChange.length] = oldParent;
                this.parentIDPercentChange[this.parentIDPercentChange.length] = record.data["parent"];
                if(this.parentIDPercentChange.length > 0)
                    this.updatedParentTasks_PercentComp(this.parentIDPercentChange);
                this.onInOutPredupdateDB(record,'outdent');
                if(this.chartCtl.updatedTaskIds.length > 0)
                    this.updatedTasks_ServerRequest(this.chartCtl.updatedTaskIds)
                if(this.chartCtl.updatedParentTaskIds.length > 0)
                    this.updatedParentTasks_ServerRequest(this.chartCtl.updatedParentTaskIds)
                this.ParentIdFieldOfRecord_onOutdent(recordTaskIdparentIdMap);
            }
            chartControl.reassignLinks();
            if(this.showResourceFlag){
                this.chartCtl.showResources([], this.getSuccessorTaskIds(-1), false, this.allResources);
                this.chartCtl.showResources(this.getResources(), this.getSuccessorTaskIds(-1), true, this.allResources);
            }
            if(this.showTaskProgress)
                this.chartCtl.showTaskProgress(this.getTaskIdForTaskProgress(), true);
        }
    },

    ParentIdFieldOfRecord_onOutdent : function(recordTaskIdparentIdMap) {
        var jsonData = Wtf.encode(recordTaskIdparentIdMap);
        if(jsonData !='{}') {
            Wtf.Ajax.requestEx({
                url: Wtf.req.prj + 'projectGridData.jsp',
                params: {
                    action : "updateParentTaskId",
                    value: jsonData,
                    projectid: this.pid,
                    userid : random_number
            }}, this);
        }
    },

    indent: function() {
        var gridData = this.dstore.data;
        var gridRow = this.krow;
        var record = gridData.items[gridRow];
        if(record.data['duration']!= "") {
            if (gridRow == 0 || record.data['duration'] == "")
                return;
            var prevrec = gridData.items[gridRow - 1];
            var tempRec = prevrec;
            if(tempRec.data['duration'] == "") {
                var c = this.search(tempRec.data['taskid']) - 1;
                do {
                    tempRec = gridData.items[c];
                   if(!tempRec || tempRec.data['duration'] != "")
                       break;
                   c--;
                }while(true);
            }
            if(tempRec.data['level'] == record.data['level'] - 1)
                return;
            var nextrec = gridData.items[gridRow + 1];
            var emptyFlag = true;
            var recParentId = tempRec.data['taskid'];
            if(tempRec.data['level'] == parseInt(record.data['level']) - 1)
                recParentId = tempRec.data['parent'];
            else if(tempRec.data['level'] > record.data['level']) {
                if(tempRec.data['level'] != record.data['level']) {
                    for(c = gridRow - 2; tempRec.data['level'] != record.data['level']; c--) {
                        tempRec = gridData.items[c];
                        if(!tempRec)
                            break;
                    }
                    recParentId = tempRec.data['taskid'];
                }
            }
            if(this.checkCircularOnInOut(record.data['taskid'], recParentId))
                return;
            var plannerView = this.getView();
            if (prevrec.data['level'] >= record.data['level']) {
                for (var s = 2; prevrec.data['level'] >= record.data['level']; s++) {
                    if (prevrec.data['duration'] != "") {
                        record.data['level'] = parseInt(record.data['level']) + 1;
                        var ttarget = plannerView.getCell(gridRow, 3).firstChild.firstChild;
                        var ml = new String(ttarget.parentNode.style.marginLeft);
                        if (ml != '') {
                            var offset = ml.length - 2;
                            ml = ml.substr(0, offset);
                            var mlval = parseInt(ml) + 20;
                            ml = new String(mlval) + 'px';
                        } else
                            ml = '20px';
                        ttarget.parentNode.style.marginLeft = ml;
                        emptyFlag = false;
                        break;
                    }
                    prevrec = gridData.items[gridRow - s];
                    if (!prevrec)
                        break;
                }
            }
            if (emptyFlag)
                return;
            prevrec = gridData.items[gridRow - 1];
            if (prevrec) {
                for (s = 2; record.data['level'] == (parseInt(prevrec.data['level']) + 1); s++) {
                    if (prevrec.data['duration'] != "") {
                        var t = plannerView.getCell(gridRow - s + 1, 3).firstChild.firstChild;
                        t.className = 'Dminus';
                        t.nextSibling.className = 'imgtext';
                        this.dstore.getAt(gridRow - s + 1).data.isparent = true;
                        Wtf.get(plannerView.getRow(gridRow - s + 1)).addClass('parentRow');
                        break;
                    }
                    prevrec = gridData.items[gridRow - s];
                    if (!prevrec)
                        break;
                }
            }
            if (nextrec) {
                if (record.data['level'] == nextrec.data['level']) {
                    for (var j = 1; nextrec.data['level'] >= record.data['level']; j++) {
                        nextrec.data['level'] = parseInt(nextrec.data['level']) + 1;
//                        var ttarget = plannerView.getCell(gridRow + j - 1, 3).firstChild.firstChild;
                        var nexttarget = plannerView.getCell(gridRow + j, 3).firstChild.firstChild;
                        var ml1 = new String(nexttarget.parentNode.style.marginLeft);
                        if (ml1 != '') {
                            var offset1 = ml1.length - 2;
                            ml1 = ml1.substr(0, offset1);
                            var mlval1 = parseInt(ml1) + 20;
                            ml1 = new String(mlval1) + 'px';
                        }
                        nexttarget.parentNode.style.marginLeft = ml1;
                        nextrec = gridData.items[gridRow + j + 1];
                        if (!nextrec)
                            break;
                    }
                }
            }
            if (record.data['level'] >= prevrec.data['level']){
                if (record.data['level'] == prevrec.data['level'])
                    record.data['parent'] = prevrec.data['parent'];
                else
                    record.data['parent'] = prevrec.data['taskid'];
            } else {
                for (var i = 2; record.data['level'] != prevrec.data['level']; i++) {
                    prevrec = gridData.items[gridRow - i];
                    if (!prevrec) {
                        prevrec = record;
                        break;
                    }
                }
                record.data['parent'] = prevrec.data['parent'];
            }
            prevrec = gridData.items[gridRow - 1];
            if (prevrec) {
                for (s = 2; prevrec.data['taskid'] != record.data['parent']; s++) {
                    if (prevrec.data['duration'] == "") {
                        prevrec.set('parent', record.data['parent']);
                        prevrec.set('level', record.data['level']);
                        plannerView.getCell(gridRow - s + 1, 3).firstChild.style.marginLeft = (prevrec.data['level'] * 20) + 'px';
                        if(this.clientServerChange=='client')
                            this.updateDBForParentTask(record);
                    }
                    prevrec = gridData.items[gridRow - s];
                    if (!prevrec)
                        break;
                }
            }
            this.deleteLinkOnInOut(record.data['taskid']);
            var tr = gridData.items[this.search(record.data['parent'])];
            tr.data.checklist = "";
            Wtf.getCmp('todo_list' + this.pid).refreshToDos();
            var chartControl = this.chartCtl;
            chartControl.indentGrid(tr);
            if(this.checkIfLinkExists('Panel' + record.data['parent'])) {
                chartControl.mysubtask('Panel' + record.data['parent']);
            } //else {
                chartControl.mysubtask('Panel' + record.data['taskid']);
//            }
            this.setParentCompletion(record.data["parent"]);
            if(this.clientServerChange == 'client') {
                this.parentIDPercentChange[this.parentIDPercentChange.length] = record.data["parent"];
                if(this.parentIDPercentChange.length > 0)
                    this.updatedParentTasks_PercentComp(this.parentIDPercentChange);
                this.onInOutPredupdateDB(record,'indent');
                if(this.chartCtl.updatedTaskIds.length > 0)
                    this.updatedTasks_ServerRequest(this.chartCtl.updatedTaskIds)
                if(this.chartCtl.updatedParentTaskIds.length > 0)
                    this.updatedParentTasks_ServerRequest(this.chartCtl.updatedParentTaskIds)
            }
            if(this.showResourceFlag){
                this.chartCtl.showResources([], this.getSuccessorTaskIds(-1), false, this.allResources)
                this.chartCtl.showResources(this.getResources(), this.getSuccessorTaskIds(-1), true, this.allResources);
            }
            if(this.showTaskProgress)
                this.chartCtl.showTaskProgress(this.getTaskIdForTaskProgress(), true);
        }
    },

    setParentCompletion: function(parentid){
        var ganttChartPanel = this.chartCtl;
        var recindex = this.search(parentid);
        if(recindex != null){
            var rec = this.dstore.getAt(recindex);
            var totalduration = 0;
            var completion = 0;
            var flag = true;
            while(flag){
                var nextrec = this.dstore.getAt(recindex+1)
                if(rec.data["level"] < nextrec.data["level"]) {
                    if(rec.data["level"] == nextrec.data["level"]-1) {
                        var dur = parseInt(nextrec.data["duration"]);
                        if(typeof nextrec.data["duration"] == "string" && nextrec.data["duration"].indexOf("h") != -1)
                            dur = ( parseInt(nextrec.data["duration"]) / 8)
                        if(!isNaN(dur)){
                            completion += dur * (nextrec.data["percentcomplete"] / 100);
                            totalduration += dur;
                        }
                    }
                } else
                    flag = false;
                recindex++;
            }
            var percomp = 0;
            if(totalduration != 0)
                percomp = parseInt((completion / totalduration * 100));
//            var old_val = rec.data["percentcomplete"];
//            rec.data["percentcomplete"] = percomp;
            var pstyle = this.backupclass('Panel' + rec.data["taskid"], -1);
            rec.set("percentcomplete", percomp);
            this.assignclass('Panel' + rec.data["taskid"], pstyle);
            this.updateDB(rec);
            ganttChartPanel.setTaskProgress(rec);
            if(this.showTaskProgress) {
                var taskIdArray = [];
                taskIdArray[0] = rec.data['taskid'];
                var TaskPanel = 'Panel'+rec.data['taskid'];
                if(ganttChartPanel.taskProgressArray[TaskPanel]) {
                   var obj = ganttChartPanel.taskProgressArray[TaskPanel];
                   obj.clear();
                   delete ganttChartPanel.taskProgressArray[TaskPanel];
                }
                if(parseInt(rec.data['percentcomplete'])!='0')
                    ganttChartPanel.showTaskProgress(taskIdArray, true);
            }
            if(parseInt(rec.data["parent"]) != 0 && rec.data['parent']!='undefined'){
                this.setParentCompletion(rec.data["parent"]);
                if(this.clientServerChange=='client')
                    this.parentIDPercentChange[this.parentIDPercentChange.length] = rec.data["parent"];
            }
        }
        return;
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
                        if(!temp)
                            break;
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
            chartControl.shiftdown(this.getSuccessorTaskIds(gridRow + i - 2), shiftY, false);
            this.dragOffset += shiftY;
            if(this.showResourceFlag)
                chartControl.showResources(ResourceArray, displayArray, true, this.allResources);
        } else {
            var nxt1 = gridData.items[gridRow + 1];
            i = 2;
            var hideArray = [];
            while (curr.data['level'] < nxt1.data['level']) {
                var nextdiv1 = maindiv.nextSibling;
//                var nexttextdiv = nextdiv1.firstChild.firstChild.firstChild.firstChild.nextSibling.nextSibling.firstChild.lastChild;
                if (nextdiv1.style.display == 'block' || nextdiv1.style.display == '')
                    shiftY += 21;
                nextdiv1.style.display = "none";
                hideArray[i - 2] = nxt1.data['taskid'];
                maindiv = nextdiv1;
                nxt1 = gridData.items[gridRow + i];
                if (!nxt1)
                    break;
                i++;
            }
            this.rowdiv.className = "plus";
            chartControl.hidePanel(hideArray);
            chartControl.shiftup(this.getSuccessorTaskIds(gridRow + i - 2), shiftY, false);
            this.dragOffset -= shiftY;
            if(this.showResourceFlag)
                chartControl.showResources([], hideArray, false, this.allResources);
        }
        chartControl.reassignLinks();
    },
    requestDeleteTask: function() {
        if(this.krow != null){
            showMainLoadMask(WtfGlobal.getLocaleText('pm.loading.text')+'...');
            var count = this.dstore.getCount();
            var selArr = this.getSelectionModel().getSelections();
            this.getSelectionModel().clearSelections();
            var delData = "";
            var delObj = {};
            if(selArr.length == 0) {
                hideMainLoadMask();
                return;
            }
            for(var selCnt  = 0; selCnt < selArr.length; selCnt++) {
                var temp = selArr[selCnt];
                var curIndex = this.search(temp.data['taskid']);
                if(curIndex != (count - 1)){
                    if(delObj[temp.data['taskid']] === undefined){
                        if(temp.data["isparent"]){
                            var childRecords = [];
                            this.getChildRecs(temp, childRecords);
                            for(var cntr = 0; cntr < childRecords.length; cntr++){
                                if(delObj[childRecords[cntr].data['taskid']] === undefined){
                                    delData += "{taskid:" + childRecords[cntr].data['taskid'] + "},";
                                    delObj[childRecords[cntr].data['taskid']] = childRecords[cntr].data['taskid'];
                                }
                            }
                        } else {
                            delData += "{taskid:" + temp.data['taskid'] + "},";
                            delObj[temp.data['taskid']] = temp.data['taskid'];
                        }
                    }
                }
            }
            if(delData.length>0) {
                delData = "["+delData.substr(0, delData.length - 1) + "]";
                this.deleteTaskFromDB(delData, "deleteTask");
                Wtf.getCmp('todo_list' + this.pid).refreshToDos();
            }else
                hideMainLoadMask();
        }
    },
    afterDelete: function(prevArray) {
        var prevCnt = 0;
        var gridData = this.dstore.data;
        var plannerView = this.getView();
        var prevArrayLength = prevArray.length;
        while(prevCnt < prevArrayLength) {
            var prevIndex = this.search(prevArray[prevCnt]);
            if(prevIndex != null){
                var nxtRecord = gridData.items[prevIndex + 1];
                var prevRecord = gridData.items[prevIndex];
                if(nxtRecord && prevRecord.data['level'] >= nxtRecord.data['level']){
                    var t = plannerView.getCell(prevIndex, 3).firstChild.firstChild;
                    t.className = 'minus';
                    t.nextSibling.className = 'defaulttext';
                    this.dstore.getAt(prevIndex).data.isparent = false;
                    if(Wtf.get(plannerView.getRow(prevIndex)).hasClass('parentRow'))
                        Wtf.get(plannerView.getRow(prevIndex)).removeClass('parentRow');
                    this.fireEvent('onOutdent', null, prevRecord);
                }
            }
            prevCnt++;
        }
        var length = this.dstore.getCount();
        for(var sCnt = 0; sCnt < length; sCnt++) {
            var tempRec = gridData.items[sCnt];
            var tPanel = Wtf.getCmp('Panel' + tempRec.data['taskid']);
            var newPred = "";
            if(tPanel) {
                var predArray = tPanel.predecessor;
                for(var pId in predArray){
                    var newI = this.search(pId.substr(5));
                    if(newI != null)
                        newPred += (newI + 1) + ',';
                }
                var sc = this.backupclass('Panel' + tempRec.data['taskid'], -1);
                tempRec.set('predecessor', newPred.substr(0, newPred.length - 1));
                this.assignclass('Panel' + tempRec.data['taskid'], sc);
            }
        }
    },
    adjustParent: function(parentRecs){
        for(var parent in parentRecs){
            var index = this.search(parent);
            var parentR = this.dstore.data.items[index];
            if(parentR !== undefined){
                this.chartCtl.mysubtask('Panel' + parent);
            }
        }
    },
    getSuccessorTaskIds: function(currentRow) {
        var taskIdArray = [];
        if(typeof currentRow == 'string')
            currentRow = this.search(currentRow);
        var gridData = this.dstore.data;
        var length = this.dstore.getCount();
        for (var i = currentRow + 1, j = 0; i < length; i++, j++)
            taskIdArray[j] = gridData.items[i].data['taskid'];
        return taskIdArray;
    },

    InsertTaskInGrid: function(rindex,flag) {
        if (rindex >= 0) {
            var parent = 0;
            var level = 0;

            if (rindex > 0) {
                parent = this.dstore.data.items[rindex].data['parent'];
                level = this.dstore.data.items[rindex].data['level'];
            }
            var p = new this.Task({
                taskid: '0',
                taskname: '',
                taskindex: rindex+1,
                duration: '',
                startdate: '',
                enddate: '',
                predecessor: '',
                resourcename: '',
                parent: parent,
                level: level,
                percentcomplete: '0',
                actstartdate: '',
                actualduration: '',
                ismilestone: false,
                priority: 1,
                isparent: false,
                notes: ''
            });
            this.insertEmptyTask(rindex, p);
            this.getSelectionModel().clearSelections();
        } else
            msgBoxShow(58, 1);
    },

    ArrangeNumberer: function(currentRow) {
        var plannerView = this.getView();
        var length = this.dstore.getCount();
        for (var i = currentRow; i < length; i++)
            plannerView.getCell(i, 0).firstChild.innerHTML = i + 1;
    },

    bufferLinks: function(buttonObj, bufferObject) {
        var linksBuffer = {};
        if (buttonObj == 'cutTask') {
            for(var buff in bufferObject) {
                if(Wtf.getCmp(buff)) {
                    var succArray = Wtf.getCmp(buff).successor;
                    for(var succ in succArray)
                        linksBuffer['jg_' + buff + '_' + succ] = this.linkArrayObj['jg_' + buff + '_' + succ];
                    var predArray = Wtf.getCmp(buff).predecessor;
                    for(var pred in predArray)
                        linksBuffer['jg_' + pred + '_' + buff] = this.linkArrayObj['jg_' + pred + '_' + buff];
                }
            }
        } else {
            for(var buff1 in bufferObject) {
                if(Wtf.getCmp(buff1)) {
                    var succArray1 = Wtf.getCmp(buff1).successor;
                    for(var succ1 in succArray1) {
                        if(bufferObject[succ1])
                            linksBuffer['jg_' + buff1 + '_' + succ1] = this.linkArrayObj['jg_' + buff1 + '_' + succ1];
                    }
                }
            }
        }
        return linksBuffer;
    },

    getTaskObj : function(record) {
    	var temp = new this.Task({
            taskid: record.data['taskid'],
            taskname: record.data['taskname'],
            duration: record.data['duration'],
            startdate: record.data['startdate'],
            enddate: record.data['enddate'],
            predecessor: '',
            resourcename: record.data['resourcename'],
            parent: record.data['parent'],
            level: record.data['level'],
            percentcomplete: record.data['percentcomplete'],
            actstartdate: record.data['actstartdate'],
            actualduration: record.data['actualduration'],
            ismilestone: record.data['ismilestone'],
            priority: record.data['priority'],
            isparent: record.data['isparent'],
            notes: record.data['notes'],
            links: record.data['links']
        });
        return temp;
    },
    loadbuffer: function(object){
        if (this.krow != null) {
            showMainLoadMask(WtfGlobal.getLocaleText("pm.loading.text")+ '...');
            var buf = [];
            var selarr = this.getSelectionModel().getSelections();
            var gIds = {};
//            var ctr = 0;
            for(var i = 0; i < selarr.length; i++){
                var cont = true;
                for(var c = 0; c < selarr.length; c++){
                    if(selarr[i].data.parent == selarr[c].data.taskid){
                        cont = false;
                        break;
                    }
                }
                if(cont){
                    var idMap = {};
                    var childRecs = [];
                    this.getChildRecs(selarr[i], childRecs);
                    var lvl = childRecs[0].data["level"];
                    for(var j = 0; j < childRecs.length; j++){
                        var temp = buf.length;
                        buf[temp] = this.getTaskObj(childRecs[j]);
                        var lclID = ++this.theid;
                        idMap[buf[temp].data["taskid"]] = lclID;
                        gIds[buf[temp].data["taskid"]] = lclID;
                        buf[temp].data["taskid"] = lclID;
                        if(j == 0)
                            buf[temp].data["parent"] = "-1";
                        else
                            buf[temp].data["parent"] = idMap[buf[temp].data["parent"]];
                        buf[temp].data["level"] -= lvl;
                        if(childRecs[j].data["predecessor"] != "")
                            buf[temp].data["predecessor"] = this.getPreds(childRecs[j].data["predecessor"]);
                    }
                    for(var ctr = 0; ctr < buf.length; ctr++){
                        if(buf[ctr].data["predecessor"] != ""){
                            var pid = buf[ctr].data["predecessor"].split(",");
                            var newPredId = "";
                            for(var t = 0; t < pid.length; t++){
                                if(idMap[pid[t]] !== undefined)
                                    newPredId += idMap[pid[t]] + ",";
                                else if(gIds[pid[t]] !== undefined)
                                    newPredId += gIds[pid[t]] + ",";
                            }
                            if(newPredId != "")
                                buf[ctr].data["predecessor"] = newPredId.substring(0, (newPredId.length - 1));
                        }
                    }
                }
            }
            if(object=='template')
                this.templateBuffer = buf;
            else
                this.buffer = buf;
            hideMainLoadMask();
        } else
            msgBoxShow(46, 1);
    },
    getChildRecs: function(rec, cr){
        cr[cr.length] = rec
        if(rec !== undefined && rec.data["isparent"]){
            var childRecs = this.dstore.query("parent", rec.data["taskid"]).items;
            for(var cntr = 0; cntr < childRecs.length; cntr++){
                if(childRecs[cntr].data["isparent"])
                    this.getChildRecs(childRecs[cntr], cr);
                else
                    cr[cr.length] = childRecs[cntr];
            }
        }
    },
    getPreds: function(prds){
        var p = prds.split(",");
        var actid = "";
        for(var c = 0; c < p.length; c++)
            actid += this.dstore.getAt(p[c] - 1).data["taskid"] + ",";
        return actid.substring(0, (actid.length -1));
    },
    getResourceIdFromResourceNames : function(record) {
        var resources = [];
        var resourceIdStr = "";
        if(record.data['resourcename']!="") {
            if ((record.data['resourcename']).indexOf(',') != -1)
                resources = (record.data['resourcename']).split(',');
            else
                resources[0] = record.data['resourcename'];
            var resourcesLength = resources.length;
            for(var i = 0; i < resourcesLength; i++) {
                var index = this.allResources.find('resourcename',resources[i]);
                if(index != -1){
                    var record1 = this.allResources.data.items[index];
                    resourceIdStr += record1.data['resourceid'] + ",";
                }
            }
            var resIdLength = resourceIdStr.length;
            if(resIdLength > 0)
                resourceIdStr = resourceIdStr.substr(0, resIdLength - 1);
        }
        return resourceIdStr;
    },

    getResourceNameFromId: function(resId) {
        if(resId != "") {
            var resIdArray = [];
            if(resId.indexOf(',') != -1)
                resIdArray = resId.split(',');
            else
                resIdArray[0] = resId;
            var resName = "";
            var resIdLength = resIdArray.length;
            for(var t = 0; t < resIdLength; t++) {
                var index = this.allResources.find('resourceid', resIdArray[t]);
                if(index != -1)
                    resName += this.allResources.data.items[index].data['resourcename'] + ',';
            }
            if(resName != "")
                return resName.substr(0, resName.length - 1);
        }
        return "";
    },
    insertbuffer: function(object, refDate) {
        this.pasteFlag = true;
        this.getSelectionModel().clearSelections(false);
        var pasteBuffer = [];
        var selRec = this.dstore.data.items[this.krow].data;
        var lvl = parseInt(selRec.level);
        if(object=='paste'){
            pasteBuffer = this.buffer;
            refDate = Date.parseDate(this.projStartDate, 'Y-m-j H:i:s');
        } else
            pasteBuffer = this.templateStore.data.items;
        if (this.krow != null) {
            if (pasteBuffer != null) {
                showMainLoadMask(WtfGlobal.getLocaleText("pm.loading.text")+'...');
                var jsonData = "[";
                for(var k = 0; k < pasteBuffer.length; k++){
                    pasteBuffer[k].data["level"] = parseInt(pasteBuffer[k].data["level"]) + lvl;
                    if(pasteBuffer[k].data["parent"] == "-1")
                        pasteBuffer[k].data["parent"] = selRec.parent;
                    jsonData += this.getJsonFromRecord(pasteBuffer[k]) + ",";
                }
                jsonData = jsonData.substr(0, jsonData.length - 1) + "]";
                Wtf.Ajax.requestEx({
                    url: Wtf.req.prj + 'projectGridData.jsp',
                    params: {
                        action: object,
                        value: jsonData,
                        rowindex: this.krow,
                        projectid: this.pid,
                        userid: random_number,
                        refDate: refDate.format('Y-m-j')
                    }},
                    this,
                    function(result, request) {
                       this.pasteFlag = false;
                    },function(result, request){
                        msgBoxShow(179, 1);
                });
                hideMainLoadMask();
                this.pasteFlag = false;
             } else {
                msgBoxShow(60, 1);
                this.pasteFlag = false;
            }
        }
    },

    getJsonFromRecord : function(record) {
        record.data["notes"] = record.data["notes"].replace(/\n/g,"!NL!")
        var sdate = new Date(record.data["startdate"]);
        sdate = sdate.format("Y-m-j");
        var edate = new Date(record.data["enddate"]);
        edate = edate.format("Y-m-j");
        var actstartdate = new Date(record.data["actstartdate"]);
        actstartdate = actstartdate.format("Y-m-j");
        if(!record.data['links'])
            record.data.links = '';
        if(record.data["percentcomplete"] == "")
            record.data["percentcomplete"] = "0";
//        var taskindex = this.search(record.data['taskid']) + 1;
//        record.data['taskindex'] = taskindex;
        if(record.data['startdate'] != "")
            var jsonData = "{taskid:\""+record.data["taskid"]+"\",taskname:\""+record.data["taskname"]+"\",duration:\""+record.data["duration"]+"\",startdate:\""+sdate.toString()+"\",enddate:\""+edate.toString()+"\",predecessor:\""+record.data["predecessor"]+"\",resourcename:\""+record.data["resourcename"]+"\",parent:\""+record.data["parent"]+"\",level:"+record.data["level"]+",percentcomplete:\""+record.data["percentcomplete"]+"\",actstartdate:\""+actstartdate.toString()+"\",actualduration:\""+record.data["actualduration"]+"\",ismilestone:\""+record.data["ismilestone"]+"\",priority:\""+record.data["priority"]+"\",notes:\""+record.data["notes"]+"\",isparent:\""+record.data["isparent"]+"\",links:\""+record.data["links"]+"\",taskindex:\""+record.data["taskindex"]+"\",checklist:\""+record.data["checklist"]+"\"}";
        else
            jsonData = "{taskid:\""+record.data["taskid"]+"\",taskname:\"\",duration:\"\",startdate:\"\",enddate:\"\",predecessor:\"\",resourcename:\"\",parent:\""+record.data["parent"]+"\",level:"+record.data["level"]+",percentcomplete:\"\",actstartdate:\"\",actualduration:\"\",ismilestone:\"\",priority:\"\",notes:\"\",isparent:\"\",links:\""+record.data["links"]+"\",taskindex:\""+record.data["taskindex"]+"\",\"checklist\":\"\"}";
        return(jsonData);
    },

    isMyTask: function(record){
        var res = record.data.resourcename.split(",");
        var rE = false;
        for(var cnt = 0; cnt < res.length; cnt++){
            if(res[cnt] == loginid){
                rE = true;
                break;
            }
        }
        return rE;
    },

    DisableCells: function(e) {
        var chStatus = WtfGlobal.getCheckListModuleStatus();
        if(!this.archived && (this.connstatus != 7 && this.connstatus != 8) && !this.viewMode){
            if(this.connstatus == 6){
                if(e.field == "percentcomplete" && (e.record.data['checklist'] == '' && chStatus))
                    return true;
                else
                    return false;
            } else if((e.record.data.taskid =='0' || e.record.data.startdate == '') && (e.field == 'predecessor' || e.field == 'resourcename' || e.field == "percentcomplete" || e.field == "checklist")) {
                e.cancel = true;
                return;
            }
//            var nextrec = this.dstore.data.items[e.row + 1];
//            if (nextrec)
//            if (e.record.data["taskid"] == nextrec.data["parent"])
            if(e.record.data["isparent"]) {
                if (e.field == "duration" || e.field == "startdate" || e.field == "enddate" || e.field == "percentcomplete" || e.field == "checklist")
                    e.cancel = true;
            } else if(e.field == "percentcomplete" && (e.record.data['checklist'] != '' && chStatus)) {
                return false;
            }
            this.editStyleClass = this.backupclass('Panel' + this.dstore.data.items[e.row].data['taskid'], -1);
        } else if(this.connstatus == 8){
            if(this.isMyTask(e.record)){
                if(e.field == 'percentcomplete' && e.record.data["isparent"] == false && !(e.record.data['checklist'] != '' && chStatus))
                    return true;
                else
                    return false;
            } else
                return false;
        } else
            return false;
    },

    Param: function(e) {
        if ((e.field == 'duration' || e.field == 'startdate' || e.field == 'enddate') && e.value == "")
            e.cancel = true;
        if (e.field == "predecessor") {
            var reg = "^(\\d+(,\\d+)*)*$";
            if (!e.value.match(reg)) {
                e.cancel = true;
                return;
            }
        }
        if (e.record.data['taskname'] == "" && e.record.data['predecessor'] == "" && e.record.data['resourcename'] == ""){
            if (e.record.data['duration'] == "" && e.field != 'duration'){
                e.record.set('duration','1');
                e.record.data['actualduration'] = '1';
            }
            var dt = new Date(this.startdate);
            /*if (dt.format('w') == 6) {
                dt = dt.add(Date.DAY, 2);
            } else if (dt.format('w') == 0) {
                dt = dt.add(Date.DAY, 1);
            }*/
            var dur = this.findWorkingStEndDate(this.startdate,0);
            if(dur!=0)
                dt = dt.add(Date.DAY, dur);

            dt = this.checkStartDate(dt);
            if (e.record.data['enddate'] == ""){
                if (e.field != 'enddate')
                    e.record.set('enddate', dt);
                else {
                    e.record.set('enddate', e.value);
                    e.record.set('startdate', e.value);
                    e.record.data['actstartdate'] = e.value;
                }
            }
            if (e.record.data['startdate'] == "" && e.field != 'startdate') {
                e.record.set('startdate', dt);
                e.record.data['actstartdate'] = dt;
            }
        }
        if (e.field == 'duration')
            this.edited[e.record.data["taskid"]] = true;
    },

    GridAfterEdit: function(e) {
        if(this.connstatus == 6 || this.connstatus == 8){
            if(e.field == "percentcomplete"){
                Wtf.Ajax.requestEx({
                    url: Wtf.req.prj + 'projectGridData.jsp',
                    method: 'GET',
                    params: {
                        action:'updatepercentcomplete',
                        val: e.record.data["percentcomplete"],
                        taskid: e.record.data['taskid'],
                        projectid: this.pid,
                        userid : random_number,
                        uid: loginid
                    }
                }, this);
            }
        } else {
            Wtf.get(this.getView().getCell(e.row, e.column)).removeClass('x-grid3-dirty-cell');
            this.clientServerChange = 'client';
            var count = this.dstore.getCount();
            var flag = false;
            if (count == (e.row + 1))
                this.addBlankRow();
            if (e.field == 'duration'){
                var nVal = WtfGlobal.HTMLStripper(e.value);
                var patt1=new RegExp("^((\\d+\\.?\\d*|\\d*\\.?\\d+)+(day|days|hr|hrs|h|d)*)$");
                if(patt1.test(nVal)){
                     var duval = this.getvalidDuration(nVal);
                     var a = new RegExp(".0{2,3}$")
                     var z = duval.replace(a, "");
                     e.record.set('duration', z);
                     e.record.data['actualduration'] = z;
                } else {
                   var patt2 = new RegExp("^(day|days|hr|hrs|h|d)*$");
                    if(e.originalValue == "" || patt2.test(e.originalValue))
                        e.record.set('duration', "1");
                    else
                        e.record.data['duration'] = e.originalValue;
                    e.record.data['actualduration'] = e.originalValue;
                }

            } else if (e.field == 'taskname') {
                nVal = WtfGlobal.HTMLStripper(e.value);
                if(nVal.length>512){
                    msgBoxShow(61, 2);
                    nVal = nVal.substring(0,512);
                }
                e.record.set('taskname',nVal);
            } else if(e.field == 'startdate') {
                var curdate = e.value;
                var sdate = Date.parseDate(this.projStartDate, 'Y-m-d H:i:s');
                if(sdate){
                    if(sdate > curdate){
                        e.record.data['startdate'] = e.originalValue;
                        msgBoxShow(178,1);
                        return;
                    }
                }
              }
            else if (e.field == 'enddate' && e.record.get('duration') != 0 && e.record.data['enddate'] != "") {
                var stdate = new Date(e.record.get("startdate"));
                var enddate = new Date(e.value);
                if(enddate<stdate){
                  enddate = new Date(e.originalValue);
                  msgBoxShow(212,1);
              }
                stdate = new Date(stdate.getFullYear(), stdate.getMonth(), stdate.getDate());
                enddate = new Date(enddate.getFullYear(), enddate.getMonth(), enddate.getDate());
                var duration = this.timeDifference(enddate, stdate);
                if (duration < 0){
                    e.record.data['startdate'] = e.value;
                    e.record.data['actstartdate'] = e.value;
                    e.record.data['duration'] = 1;
                    e.record.data['actualduration'] = 1;
                } else {
                    duration = this.CalculateActualDuration(stdate, enddate);
                    e.record.data['duration'] = duration;
                    e.record.data['actualduration'] = duration;
                }
            } else if (e.field == 'predecessor') {
                if(e.record.data['taskid']!=0) {
                    nVal = WtfGlobal.HTMLStripper(e.value);
                    e.record.data['predecessor'] = nVal;
                    this.LinkChanges_AfterPredecessorEdit(e.originalValue,e.record);
                } else
                    e.record.set('predecessor', "");
    //            this.containerPanel.loadingMask.hide();
            } else if(e.field == 'resourcename') {
                if(e.record.data['taskid']!=0) {
                    var jdata = "";
                    if(e.record.data['resourcename']!="") {
                        var resArray = e.record.data['resourcename'].split(',');
                        jdata = "[";
                        var resArrayLength = resArray.length;
                        for(var rescnt = 0; rescnt < resArrayLength; rescnt++) {
                            var recIndex = this.allResources.find('resourceid', resArray[rescnt]);
                            var resRec = this.allResources.data.items[recIndex];
                            jdata += Wtf.encode(resRec.data) + ",";
                        }
                        jdata = jdata.substr(0, jdata.length - 1);
                        jdata += "]";
                    }
                    this.storeResourcesInDB(e.record.data['taskid'],jdata);
                } else
                    e.record.set('resourcename', "");
                this.checkGridRowStyle(e.record,e.row);
            } else if(e.field == 'checklist') {
                var old = e.originalValue;
                var val = e.value;
                if(e.record.data['taskid'] == 0 || val == "-")
                    e.record.set('checklist', "");
                e.record.commit();
                if(old != val){
                    Wtf.getCmp('todo_list' + this.pid).refreshToDos();
                }
                if(e.record.data['checklist'] != old)
                    e.record.set('percentcomplete', 0);
                e.record.commit();
            } else if(e.field == "percentcomplete"){
                if(e.record.data["parent"] != 0){
    //                var parentID = e.record.data["parent"];
                    this.setParentCompletion(e.record.data["parent"]);
    //                var pstyle = this.backupclass('Panel' + parentID, -1)
    //                var recindex = this.search(parentID);
    //                var pRec = this.dstore.getAt(recindex);
    //                var pcom = pRec.data["percentcomplete"];
    //                if(pcom <= 0){
    //                    pcom = 0;
    //                    pRec.data["percentcomplete"] = -1;
    //                } else
    //                    pRec.data["percentcomplete"] = 0;
    //                pRec.set("percentcomplete", pcom);
    //                this.assignclass('Panel' + parentID, pstyle);
    //                this.updateDB(pRec);
    //                this.chartCtl.setTaskProgress(pRec);
                }
                this.updateDB(e.record);
                this.chartCtl.setTaskProgress(e.record);
            }
            var imgdiv = 'imgciv' + e.record.data['taskid'];
            var obj = Wtf.get('img_div');
            if (obj)
                obj.id = imgdiv;
            if(e.record.data['taskid']!='0' && (e.field=="startdate" || e.field=="enddate" || e.field=="duration" || e.field == "percentcomplete")){
                this.updateTask(e.record);
                if(e.field == "duration") {
                    if(e.record.data['percentcomplete']!=0)
                        this.chartCtl.setTaskProgress(e.record);
                    this.checkGridRowStyle(e.record,e.row);
                }
                //this.chartCtl.mysubtask('Panel' + e.record.data['taskid']);
            }
            else if(e.record.data['taskname'] && e.record.data['taskid']!='0' && !Wtf.getCmp("Panel"+e.record.data['taskid'])) {
                    this.updateTask(e.record);
                //this.chartCtl.mysubtask('Panel' + e.record.data['taskid']);
            }
            else if(e.field != 'predecessor'&& e.field != 'resourcename') {
                if(e.record.data['taskid']!='0') {
                    this.updateDB(e.record);
                    this.checkGridRowStyle(e.record,e.row);
                }
                else {
                    e.record.set("checklist", "");
                    var dur = e.record.data.duration;
                    if(parseFloat(dur) > parseInt(dur))
                        dur = parseInt(dur) + 1;
                    else
                        dur = parseInt(dur);
                    sdate = e.record.data.startdate;
                    var edate = e.record.data.enddate;
                    if(e.field == "duration" && dur != 0) {
                        if(e.record.data.duration.indexOf('h')>-1){
                            dur=dur/8.0;
                            if(Math.ceil(dur)==parseInt(dur))
                                dur = dur -1;
                            var diff = this.calculatenonworkingDays(sdate, sdate.add(Date.DAY, dur));
                            enddate = sdate.add(Date.DAY, dur + diff);
                        }else{
                            diff = this.calculatenonworkingDays(sdate, sdate.add(Date.DAY, dur-1));
                            enddate = sdate.add(Date.DAY, dur + diff - 1);
                        }
                        e.record.set("enddate" , enddate);
                    } else  if(e.field == "startdate") {
                        e.record.set("enddate" , sdate);
                        e.record.data['actstartdate'] = sdate;
                    } else if(e.field == "enddate") {
                        e.record.set("startdate" , edate);
                        e.record.data['actstartdate'] = edate;
                    }
                    this.updateDB(e.record);
                }
            }
            this.assignclass('Panel' + this.dstore.data.items[e.row].data['taskid'], this.editStyleClass);
            var tp = Wtf.getCmp('Panel' + e.record.data['taskid']);
            if(tp)
                tp.updateQTip(e.record);
        }
        bHasChanged=true;
        if(refreshDash.join().indexOf("all") == -1 && refreshDash.join().indexOf("prj."+this.pid) == -1){
            refreshDash[refreshDash.length] = 'prj.'+this.pid;
            refreshDash[refreshDash.length] = 'pjh';
            refreshDash[refreshDash.length] = 'qkl';
        }

        if(this.showTaskProgress){
        	this.applyRowStyle("#9BDF7D");
        }
        if(this.showResourceFlag){
            this.highLightMyTask();
        }
    },

    storeResourcesInDB: function(taskid, jdata) {
         Wtf.Ajax.requestEx({
            url: Wtf.req.prj + 'getTaskResources.jsp',
            method: 'GET',
            params: {
                action:'insert',
                taskid:taskid,
                row : this.search(taskid),
                data: jdata,
                projectid: this.pid,
                userid : random_number
        }}, this);
    },

    LinkChanges_AfterPredecessorEdit: function(originalValue, record) {
        var value = record.data['predecessor'];
        if(!Wtf.getCmp('Panel' + record.data['taskid']))
            this.chartCtl.addPanel(record, this.search(record.data['taskid']));
        if (this.ForValidLinks(originalValue,record)) {
            value = record.data['predecessor'];
            if(originalValue != value) {
                if(this.clientServerChange =='client')
                    this.onInOutPredupdateDB(record,"predecessor") ;
                this.chartCtl.UpdatelinkArrayObj(originalValue, value, record.data['taskid'], true);
            }
        }
    },

    checkForValidLinkOnGanttChart: function(fromTaskPanelId, toTaskPanelId) {
        var toTaskRecord = this.dstore.data.items[this.search(toTaskPanelId.substr(5))];
        if(toTaskRecord) {
            var originalVal = toTaskRecord.data['predecessor'];
            var newVal = (this.search(fromTaskPanelId.substr(5)) + 1) +'';
            if(originalVal.length > 0)
                newVal = originalVal + "," + newVal;
            var sc = this.backupclass(toTaskPanelId, -1);
            toTaskRecord.set('predecessor', newVal);
            this.assignclass(toTaskPanelId, sc);
            this.LinkChanges_AfterPredecessorEdit(originalVal,toTaskRecord);
        }
    },

    ForValidLinks: function(originalValue, record) {
        var flag = true;
        var value = record.data['predecessor'];
        if(value!="") {
            var duplicateLinkFlag = false;
            var parentChildLinkFlag = false;
            var checkCircularflag = false;
            var emptyTask = false;
            var splitObj = value.split(',');
            var tempIndex = this.search(record.data['taskid']);
            var toTask = tempIndex + 1;
            var splitLength = splitObj.length;
            var gridStore = this.dstore;
            for (var i = 0; i < splitLength; i++) {
                var length = gridStore.getCount();
                if (splitObj[i] == '' || splitObj[i] > length - 1 || splitObj[i] < 1) {
                    msgBoxShow(62, 1);
                    flag = false;
                } else if (splitObj[i] == toTask) {
                    msgBoxShow(63, 1);
                    flag = false;
                }
            }
            if(flag) {
                var newSplitObj = [];
                if(originalValue != "") {
                    var oldSplitObj = [];
                    if(originalValue.indexOf(',') != -1)
                        oldSplitObj = originalValue.split(',')
                    else
                        oldSplitObj[0] = originalValue;
                    var oldSplitLength = oldSplitObj.length;
                    for(var a = 0; a < splitLength; a++) {
                        for(var b = 0; b < oldSplitLength; b++) {
                            if(oldSplitObj[b] != -1 && splitObj[a] == oldSplitObj[b]) {
                                newSplitObj[a] = -1;
                                oldSplitObj[b] = -1;
                                break;
                            }
                        }
                        if(newSplitObj[a] != -1)
                            newSplitObj[a] = splitObj[a];
                    }
                } else
                    newSplitObj = splitObj;
                var newSplitLength = newSplitObj.length;
                for(var d = 0; d < newSplitLength; d++) {
                    for (var j = d + 1; j < splitLength; j++) {
                        if (splitObj[d] == splitObj[j]) {
                            splitObj[j] = -1;
                            duplicateLinkFlag = true;
                            continue;
                        }
                    }
                }
                var to = 'Panel' + record.data['taskid'];
                for(d = 0; d < newSplitLength; d++) {
                    if(newSplitObj[d] != -1) {
                        this.circularCheckflag = false;
                        if(splitObj[d]!=-1 && this.circularlink(('Panel'+(gridStore.data.items[parseInt(splitObj[d])-1].data['taskid'])),to)) {
                            splitObj[d] = -1;
                            checkCircularflag = true;
                            //break;
                        }
                        if (splitObj[d] != -1  && this.checkForParentToSubtaskLink('Panel' + record.data['taskid'], 'Panel' + gridStore.data.items[splitObj[d] - 1].data['taskid'])) {
                            splitObj[d] = -1;
                            parentChildLinkFlag = true;
                            //break;
                        }
                        if(splitObj[d] != -1 && gridStore.data.items[parseInt(splitObj[d]) - 1].data['duration']=="") {
                            splitObj[d] = -1;
                            emptyTask = true;
                            //break;
                        }
                        if (!parentChildLinkFlag && !checkCircularflag && !emptyTask) { //if (!parentChildLinkFlag || !checkCircularflag || !emptyTask) {
                            var tempRec = gridStore.getAt(newSplitObj[d] - 1);
                            var nextRec = gridStore.getAt(tempIndex);
                            var tempLevel = tempRec.data['level'];
                            var nextLevel = record.data['level'];
                            var recLevel = nextLevel;
                            var curLevel = nextLevel;
//                            if(nextRec.data.parent != gridStore.getAt(tempIndex + 1).data.parent)
//                                curLevel++;
                            var curTempRec = tempRec;
                            var curNextRec = nextRec;
                            this.circularCheckflag = false;
                            if(tempLevel < nextLevel) {
                                while(tempLevel != nextLevel) {
                                    curNextRec = gridStore.getAt(this.search(curNextRec.data['parent']));
                                    if(this.circularlink('Panel' + tempRec.data['taskid'], 'Panel' + curNextRec.data['taskid'])) {
                                        splitObj[d] = -1;
                                        checkCircularflag = true;
                                        break;
                                    }
                                    nextLevel = curNextRec.data['level'];
                                }
                            } else if (tempLevel > nextLevel) {
                                while(tempLevel != nextLevel) {
                                    curTempRec = gridStore.getAt(this.search(curTempRec.data['parent']));
                                    if(this.circularlink('Panel' + curTempRec.data['taskid'], 'Panel' + nextRec.data['taskid'])) {
                                        splitObj[d] = -1;
                                        checkCircularflag = true;
                                        break;
                                    }
                                    tempLevel = curTempRec.data['level'];
                                }
                            }
                            if(this.circularCheckflag) {
                                break;
                            }
                            if(this.circularlink('Panel' + tempRec.data['taskid'], 'Panel' + curNextRec.data['taskid'])
                                || this.circularlink('Panel' + curTempRec.data['taskid'], 'Panel' + nextRec.data['taskid'])) {
                                splitObj[d] = -1;
                                checkCircularflag = true;
                                break;
                            }
                            for(var n = tempIndex + 1; n < length - 1 && recLevel < curLevel; n++) {
                                var curRec = gridStore.getAt(n);
                                if(this.circularlink('Panel' + tempRec.data['taskid'], 'Panel' + curRec.data['taskid'])) {
                                    splitObj[d] = -1;
                                    checkCircularflag = true;
                                    break;
                                }
                                curLevel = curRec.data.level;
                            }
                            if(this.circularCheckflag)
                                break;
                        }
                    }
                }
                if (duplicateLinkFlag || parentChildLinkFlag || checkCircularflag || emptyTask) {
                    var temp = "";
                    for (i = 0; i < splitLength; i++) {
                        if (splitObj[i] != -1) {
                            var tempLength = temp.length;
                            if (tempLength != 0)
                                temp += ',' + splitObj[i];
                            else
                                temp = splitObj[i];
                        }
                    }
                    if (parentChildLinkFlag)
                        msgBoxShow(64, 1);
                    else if(duplicateLinkFlag)
                        msgBoxShow(65, 1);
                    else if(checkCircularflag)
                        msgBoxShow(66, 1);
                    else if(emptyTask)
                        msgBoxShow(67, 1);
                    var sc = this.backupclass('Panel' + record.data['taskid'], -1);
                    record.set('predecessor', temp);
                    this.assignclass('Panel' + record.data['taskid'], sc);
                    return true;
                }
            } else {
                var sc1 = this.backupclass('Panel' + record.data['taskid'], -1);
                this.stopEditing();
                record.set('predecessor', originalValue);
                this.assignclass('Panel' + record.data['taskid'], sc1);
            }
        }
        return flag;
    },

    circularlink : function(from, to, parentId) {
        if(Wtf.getCmp(to) === undefined)
            return;
        var succArray = Wtf.getCmp(to).successor;
        var gridData = this.dstore.data;
        for (var succ in succArray) {
            var succRow = this.search(succ.substr(5));
            if (succArray[from] || (this.checkForParentToSubtaskLink(succ, from)
                && (!parentId || gridData.items[succRow].data['parent'] != parentId))) {
                this.circularCheckflag = true;
                break;
            } else {
                if(Wtf.getCmp(succ).el.hasClass('ParentTask')) {
                    var level = gridData.items[succRow].data['level'];
                    var nextRec = gridData.items[succRow];
                    for (var i = 1;; i++) {
                        if(nextRec.data['duration'] != "")
                            this.circularlink(from, 'Panel' + nextRec.data['taskid'], parentId);
                        nextRec = gridData.items[succRow + i];
                        if(!nextRec || nextRec.data['duration'] == "" || this.circularCheckflag || nextRec.data['level'] <= level)
                            break;
                    }
                    if(this.circularCheckflag)
                        break;
                } else
                    this.circularlink(from, succ, parentId);
                while(gridData.items[this.search(succ.substr(5))].data['parent'] != "0") {
                    succ = 'Panel' + gridData.items[this.search(gridData.items[this.search(succ.substr(5))].data['parent'])].data['taskid'];
                    this.circularlink(from, succ, parentId);
                    if(this.circularCheckflag)
                        break;
                }
            }
            if(this.circularCheckflag)
                break;
        }
        return (this.circularCheckflag);
    },

    calculatenonworkingDays : function(stdate,enddate) {
        var userDuration = this.timeDifference(enddate, stdate)+1;
        /*var NonWorkingdays = this.NonworkingDaysBetDates(stdate, enddate);
        if (NonWorkingdays != 0) {
            var duration = parseInt(userDuration, 10);
            duration = (duration + NonWorkingdays);
            enddate = stdate.add(Date.DAY, duration - 1);
            NonWorkingdays = this.NonworkingDaysBetDates(stdate, enddate);
            if (NonWorkingdays != 0) {
                if (NonWorkingdays % 2 == 1)
                    NonWorkingdays += 1;
            }
        }*/
        var NonWorkingdays = 0;
        var flag = true;
        while(flag) {
            var calnonwork = this.NonworkingDaysBetDates(stdate, enddate);
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

    NonworkingDaysBetDates: function(stdate, enddate) {
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
        var NonWorkingDaysBetween = NumWeeks * this.NonworkWeekDays.length;
        StDate = StDate.add(Date.DAY, (NumWeeks * 7));
        diff = this.timeDifference(EndDate, StDate);
        for (var i = 0; i < diff; i++) {
            if (this.NonworkWeekDays.join().indexOf(StDate.format('w'))>=0)
                NonWorkingDaysBetween += 1;
            StDate = StDate.add(Date.DAY, 1);
        }
        return NonWorkingDaysBetween + this.NumCompanyHoliday(actualStDate,EndDate.add(Date.DAY,-1));
    },

    CalculateActualDuration: function(stdate, enddate) {
        var nonWorkingDays = this.NonworkingDaysBetDates(stdate, enddate);
        var datediff = this.timeDifference(enddate, stdate);
        datediff = parseInt(datediff) + 1;
        datediff = datediff - nonWorkingDays;
        return datediff;
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
            var difference1 = laterdate.getTime() - earlierdate.getTime();
            daysDifference = Math.floor(difference1 / 1000 / 60 / 60 / 24);
        }
        return (daysDifference);
    },

    checkForParentToSubtaskLink: function(fromTask, toTask) {
        var fromRow = this.search(fromTask.substr(5));
        var gridData = this.dstore.data;
        var fromTaskRecord = gridData.items[fromRow];
        var toRow = this.search(toTask.substr(5));
        var toTaskRecord = gridData.items[toRow];
        if (toRow < fromRow) {
            var tempRecord = fromTaskRecord;
            fromTaskRecord = toTaskRecord;
            toTaskRecord = tempRecord;
            fromRow = fromRow + toRow;
            toRow = fromRow - toRow;
            fromRow = fromRow - toRow;
        }
        var fromLevel = fromTaskRecord.data['level'];
        var toLevel = toTaskRecord.data['level'];
        if (toLevel <= fromLevel)
            return false;
        while (toTaskRecord.data['parent'] != 0) {
            if (fromTaskRecord.data['taskid'] == toTaskRecord.data['parent'])
                return true;
            toTaskRecord = gridData.items[this.search(toTaskRecord.data['parent'])];
        }
        return false;
    },

    ShowModal: function(){
        var rec = this.dstore.getAt(this.krow);
        var taskModalWin = new Wtf.proj.taskModal({
            record: rec,
            pid: this.pid,
            plannerGrid: this,
            allResources: this.allResources,
            gridStore: this.dstore,
            connstatus: this.connstatus,
            mytask: this.isMyTask(rec)
        });
        taskModalWin.show();
    },


    UpdateGridRecord: function(columnList, valueList, TaskId){
        var sc = this.backupclass(TaskId, -1);
        var rowval = this.search(TaskId.substr(5));
        var record = this.dstore.data.items[rowval];
        var colListLength = columnList.length;
        for (var i = 0; i < colListLength; i++) {
            record.data[columnList[i]]  = valueList[i];
        }
        var tp = Wtf.getCmp('Panel' + record.data['taskid']);
        if(tp)
            tp.updateQTip(record);
        record.commit();
        this.assignclass(TaskId, sc);
        if(this.clientServerChange=='client')
            this.updateDB(record);
        this.checkGridRowStyle(record,rowval);
    },

    showResourcesOnUpdate : function(record, rowval) {
        var dur = record.data.duration+'';
        if ((this.dstore.data.items[rowval + 1] && record.data["taskid"] != this.dstore.data.items[rowval + 1].data["parent"])
            && parseFloat(dur) !=0 ) {
            var resArray = [],idArray = [];
            resArray[0]= record.data['resourcename'];
            idArray[0] = record.data['taskid'];
            if(record.data['resourcename']!='')
                this.chartCtl.showResources(resArray, idArray, true, this.allResources);
            else
                this.chartCtl.showResources(resArray, idArray, false, this.allResources);
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
        var tp = Wtf.getCmp('Panel' + record.data['taskid']);
        if(tp)
            tp.updateQTip(record);
        record.commit();
        this.assignclass(TaskId, sc);
        this.checkGridRowStyle(record,rowval);
        /*if(this.showTaskProgress && (record.data['percentcomplete']!='0')){
            var taskIdArray = [];
            taskIdArray[0] = record.data['taskid'];
            this.chartCtl.showTaskProgress(taskIdArray, true);
        } if(this.showResourceFlag){
            this.showResourcesOnUpdate(record, rowval);
        } if(this.showPriorityFlag){
            this.checkPriority(record, rowval);
        } */
    },

    updateGridForParentTask : function(stdate,enddate,duration,TaskId) {
        var sc = this.backupclass(TaskId, -1);
        var record = this.dstore.data.items[this.search(TaskId.substr(5))];
        record.data['startdate'] = stdate;
        record.data['enddate'] = enddate;
        record.data['duration'] = duration;
        var tp = Wtf.getCmp('Panel' + record.data['taskid']);
        if(tp)
            tp.updateQTip(record);
        record.commit();
        this.assignclass(TaskId, sc);
        if(this.clientServerChange=='client')
            this.updateDBForParentTask(record);
    },

    updateGridForParentWithoutServReq : function(stdate,enddate,duration,TaskId) {
        var sc = this.backupclass(TaskId, -1);
        var record = this.dstore.data.items[this.search(TaskId.substr(5))];
        record.data['startdate'] = stdate;
        record.data['enddate'] = enddate;
        record.data['duration'] = duration;
        var tp = Wtf.getCmp('Panel' + record.data['taskid']);
        if(tp)
            tp.updateQTip(record);
        record.commit();
        this.assignclass(TaskId, sc);
    },

    updatedTasks_ServerRequest : function(updatedTaskIds) {
        this.chartCtl.updatedTaskIds = [];
        var jsonData = "[";
        var taskidLength = updatedTaskIds.length;
        for(var i = 0; i < taskidLength; i++) {
            var record = this.dstore.data.items[this.search(updatedTaskIds[i])];
            jsonData += this.getJsonFromRecord(record) + ",";
        }
        jsonData = jsonData.substr(0, jsonData.length - 1) + "]";
        this.ajaxRequestForGrTasks(jsonData,'updateGroupTask');
    },

    updatedParentTasks_ServerRequest : function(updatedParentTaskIds) {
        this.chartCtl.updatedParentTaskIds = [];
        var jsonData = "[";
        var taskidLength = updatedParentTaskIds.length;
        for(var i = 0; i < taskidLength; i++) {
            if(updatedParentTaskIds[i] != "" || updatedParentTaskIds[i] != '0'){
                var record = this.dstore.data.items[this.search(updatedParentTaskIds[i])];
                jsonData += this.getJsonFromRecord(record) + ",";
            }
        }
        jsonData = jsonData.substr(0, jsonData.length - 1) + "]";
        this.ajaxRequestForGrTasks(jsonData,'updateParentGroupTask');
    },

    updatedParentTasks_PercentComp : function(updatedParentTaskIds) {
        this.parentIDPercentChange = [];
        var jsonData = "[";
        var taskidLength = updatedParentTaskIds.length;
        for(var i = 0; i < taskidLength; i++) {
            if(updatedParentTaskIds[i] != "" || updatedParentTaskIds[i] != '0'){
                var record = this.dstore.data.items[this.search(updatedParentTaskIds[i])];
                this.checkIsParent(updatedParentTaskIds[i]);
                jsonData += this.getJsonFromRecord(record) + ",";
            }
        }
        jsonData = jsonData.substr(0, jsonData.length - 1) + "]";
        this.ajaxRequestForGrTasks(jsonData,'updateParentGroupTask');
    },
    checkIsParent: function(taskid){
        var records = this.dstore.data.items;
        var rIndex = this.search(taskid);
        for(var i = rIndex; i < records.length; i++){
            if(records[i].data["parent"] == taskid)
                return false;
        }
        records[rIndex].data["isparent"] = false;
    },
    ajaxRequestForGrTasks : function(jsonData,action) {
        if(this.clientServerChange =='client'){
            Wtf.Ajax.requestEx({
            url: Wtf.req.prj + 'projectGridData.jsp',
            params: {
                action:action,
                val: jsonData,
                rowindex:"-1",
                taskid:"-1",
                projectid: this.pid,
                userid : random_number
            }},
            this);
        }
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
            var disp = this.getView().getRow(rowval).style.display;
            var r = plannerView.getCell(rowval, 3);
            var img = r.firstChild.firstChild;
            var txt = img.nextSibling;
            var cls = plannerView.getRow(rowval).className;
            if(cls.indexOf('x-grid3-row-selected') != -1)
                cls = cls.replace("x-grid3-row-selected", "", "gi");
            var t = new this.styleClass({
                imageclass: img.className,
                marginval: txt.parentNode.style.marginLeft,
                textclass: cls,
                display: disp
            });
            return t;
        }
        var bufferStyle = [];
        var cnt = 0;
        var nextrow = rowval;
        var gridData = this.dstore.data;
        do {
            var r1 = plannerView.getCell(nextrow, 3);
            var disp1 = this.getView().getRow(nextrow).style.display;
            var img1 = r1.firstChild.firstChild;
            var txt1 = img1.nextSibling;
            cls = plannerView.getRow(rowval).className;
            if(cls.indexOf('x-grid3-row-selected') != -1)
                cls = cls.replace("x-grid3-row-selected", "", "gi");
            var t1 = new this.styleClass({
                imageclass: img1.className,
                marginval: txt1.parentNode.style.marginLeft,
                textclass: cls,
                display: disp1
            });
            bufferStyle[cnt] = t1;
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
            var rv = this.search(TaskId.substr(5));
            var r = plannerView.getCell(rv, 3);
            this.getView().getRow(rv).style.display = style.data["display"];
            var img = r.firstChild.firstChild;
            var txt = img.nextSibling;
            img.className = style.data['imageclass'];
            plannerView.getRow(this.search(TaskId.substr(5))).className = style.data['textclass'];
            txt.parentNode.style.marginLeft = style.data['marginval'];
            return;
        }

        var taskidLength = TaskId.length;
        for (var a = 0; a < taskidLength; a++) {
            var r1 = plannerView.getCell(style + a, 3);
            var img1 = r1.firstChild.firstChild;
            var txt1 = img1.nextSibling;
            this.getView().getRow(style + a).style.display = style.data["display"];
            img1.className = TaskId[a].data['imageclass'];
            plannerView.getRow(this.search(TaskId.substr(5))).className = TaskId[a].data['textclass'];
            txt1.parentNode.style.marginLeft = TaskId[a].data['marginval'];
        }
    },

    shiftUpInGrid: function(taskid, shiftVal){
        var taskRecord = this.dstore.getAt(this.search(taskid));
        var sc = this.backupclass('Panel' + taskRecord.data['taskid'], -1);
        taskRecord.set("taskindex", taskRecord.data.taskindex - shiftVal);
        this.assignclass('Panel' + taskRecord.data['taskid'], sc);
    },

    shiftDownInGrid: function(taskid, shiftVal){
        var taskRecord = this.dstore.getAt(this.search(taskid));
        var sc = this.backupclass('Panel' + taskRecord.data['taskid'], -1);
        taskRecord.set("taskindex", taskRecord.data.taskindex + shiftVal);
        this.assignclass('Panel' + taskRecord.data['taskid'], sc);
    },

    showResources:function() {
        if(this.showResourceFlag==false){
            this.showResourceFlag=true;
            this.highLightMyTask();
            this.chartCtl.showResources(this.getResources(), this.getSuccessorTaskIds(-1), true, this.allResources);
            if(this.showTaskProgress)
                this.chartCtl.showTaskProgress(this.getTaskIdForTaskProgress(), true);
        } else {
            this.showResourceFlag=false;
            this.highLightMyTask();
            var resArray = [];
            resArray[0]= '';
            this.chartCtl.showResources(resArray, this.getSuccessorTaskIds(-1), false, this.allResources);
        }
    },

    showProgress : function() {
        if(this.showTaskProgress==false){
            this.showTaskProgress=true;
            this.chartCtl.showTaskProgress(this.getTaskIdForTaskProgress(), true);
            this.applyRowStyle("#9BDF7D");
        } else {
            this.showTaskProgress=false;
            this.chartCtl.showTaskProgress([], false);
            this.applyRowStyle("#ffffff");
        }
    },
    applyRowStyle : function(color){
        for(var s=0;s<this.dstore.getCount();s++){
            if(this.dstore.getAt(s).get("percentcomplete") == 100){
                this.view.getRow(s).style.backgroundColor = color;
            }else{
                this.view.getRow(s).style.backgroundColor = "#ffffff";
            }
        }
    },

    highLightMyTask : function(){
        if(!this.archived && (this.connstatus != 6 && this.connstatus != 7)){
            var color = "#9BDF7D";
            color=this.allResources.query('resourceid',loginid).items[0].data['colorcode'];
            for(var s=0;s<this.dstore.getCount();s++){
               if(this.showResourceFlag && this.isMyTask(this.dstore.getAt(s))){
                   this.view.getRow(s).style.backgroundColor = color;
               } else {
                   this.view.getRow(s).style.backgroundColor = "#FFFFFF";
               }
            }
        }
    },
    getTaskIdForTaskProgress: function() {
        var taskIDS = [];
        var cnt=0,i=0;
        var gridData = this.dstore.data;
        var count = this.dstore.getCount();
        for(; i < count; i++) {
            var percentComplete = gridData.items[i].data['percentcomplete'];
            if(percentComplete > 0) {
                taskIDS[cnt++] = gridData.items[i].data['taskid'];
            }
        }
        return taskIDS;
    },

    getResources : function(startIndex) {
        var resourceArray =[];
        var gridData = this.dstore.data;
        var count = this.dstore.getCount();
        var i;
        if(startIndex == undefined) {
            i = 0 ;startIndex = 0;
        }
        else
            i = startIndex;
        for(;i < count; i++) {
            var record = gridData.items[i];
            var dur = record.data.duration+'';
            if (gridData.items[i + 1] && parseFloat(dur) != 0)
                resourceArray[i-startIndex] = record.data['resourcename'];
            else
                resourceArray[i-startIndex] = "";
        }
        return resourceArray;
    },

    requestTaskProgress: function(g, rowindex){
        var cRec = this.dstore.getAt(rowindex);
        var resources = cRec.data['resourcename'];
        if(resources.length != 0){
            Wtf.Ajax.requestEx({
            url: Wtf.req.prj + 'projectGridData.jsp',
            params: {
                action:'requestprogress',
                requestto: resources,
                taskid: cRec.data['taskid'],
                userid: loginid,
                projectid: this.pid
            }},
            this,
            function(request, response){
                var obj = eval("(" + request + ")");
                if(obj.success) {
                    msgBoxShow(181, 0);
                }
            },
            function(){
                msgBoxShow(182, 1);
            }
        );
        } else {
            msgBoxShow(180,1);
        }
        bHasChanged = true;
        if(refreshDash.join().indexOf("all") == -1 && refreshDash.join().indexOf("req") == -1)
            refreshDash[refreshDash.length] = 'req';
    },

    selectGridRowOnPanelClick : function(taskId){
        this.krow = this.search(taskId);
        this.getSelectionModel().selectRow(this.krow);
    },

    updateDBForParentTask : function(record) {
        Wtf.Ajax.requestEx({
            url: Wtf.req.prj + 'projectGridData.jsp',
//            url: 'PorjectServlet.jsp',
            params: {
                action:'updateParentTask',
//                action:9,
//                mode:'updateParentTask',
                val: this.getJsonFromRecord(record),
                rowindex: this.search(record.data['taskid']),
                taskid: record.data['taskid'],
                projectid: this.pid
            }},
            this);
    },

    updateDB : function(record) {
        var tP = Wtf.getCmp('Panel' + record.data['taskid']);
        if(tP)
            tP.updateQTip(record);
        this.clientServerChange = 'client';
        var rowVal = this.search(record.data['taskid']);
        Wtf.Ajax.requestEx({
            url: Wtf.req.prj + 'projectGridData.jsp',
            params: {
                action:'update',
                val: this.getJsonFromRecord(record),
                rowindex:rowVal,
                taskid:record.data['taskid'],
                projectid: this.pid,
                userid : random_number
            }},
            this);
    },

    insertEmptyTask : function(rowVal,record) {
        this.clientServerChange = 'client';
        var _data = record.data;
        var jsonData = "{taskid:\""+_data.taskid+"\",taskname:\""+_data.taskname+"\",taskindex:\""+_data.taskindex+"\",duration:''"+",startdate:\""+_data.startdate+"\",enddate:\""+_data.enddate+"\",predecessor:\""+_data.predecessor+"\",resourcename:\""+_data.resourcename+"\",parent:"+
        _data.parent+",level:"+_data.level+",percentcomplete:\""+_data.percentcomplete+"\",actstartdate:\""+_data.actstartdate+"\",actualduration:\""+_data.actualduration+"\",ismilestone:\""+_data.ismilestone+"\",priority:\""+_data.priority+"\",notes:\""+_data.notes+"\",isparent:\""+_data.isparent+"\"}";
        Wtf.Ajax.requestEx({
            url: Wtf.req.prj + 'projectGridData.jsp',
            params: {
                action:'newtask',
                val: jsonData,
                rowindex : rowVal,
                projectid: this.pid,
                userid : random_number
            }},
        this);
    },

    deleteTaskFromDB: function(taskIds,userAction) {
        this.clientServerChange = 'client';
        Wtf.Ajax.requestEx({
            url: Wtf.req.prj + 'projectGridData.jsp',
            params: {
                action:userAction,
                val: taskIds,
                rowindex : "",
                projectid: this.pid,
                userid : random_number
            }},
            this,
            function(){
                bHasChanged=true;
                if(refreshDash.join().indexOf("all") == -1 && refreshDash.join().indexOf("prj."+this.pid) == -1){
                    refreshDash[refreshDash.length] = 'prj.'+this.pid;
                    refreshDash[refreshDash.length] = 'pjh';
                    refreshDash[refreshDash.length] = 'qkl';
                }
                hideMainLoadMask();
            },
            function(){
                bHasChanged=false;
                hideMainLoadMask();
        });
    },

    add_deleteLinkFromDB: function(fromId, toId, opertion) {
        var jsonData = "{fromtaskid:\"" + fromId + "\",totaskid:\"" + toId + "\"}";
        Wtf.Ajax.requestEx({
            url: Wtf.req.prj + 'projectGridData.jsp',
            params: {
                action:'deletelink',
                projectid: this.pid,
                value: jsonData,
                fromTaskId:fromId,
                toTaskId:toId,
                flag:opertion,
                userid : random_number
            }},
        this);
    },

    onInOutPredupdateDB:function (record,operation) {
        Wtf.Ajax.requestEx({
            url: Wtf.req.prj + 'projectGridData.jsp',
            params: {
                action : operation,
                value: this.getJsonFromRecord(record),
                rowindex : this.search(record.data['taskid']),
                projectid: this.pid,
                userid : random_number
            }},
        this);
    },

    importingFile : function() {
        Wtf.Ajax.requestEx({
            url: Wtf.req.prj + 'projectGridData.jsp',
            params: {
                action : "importFile",
                value: "",
                rowindex : "",
                projectid: this.pid,
                userid : random_number
            }},
        this);
    },

    convertDatesToUserTzOnPublish: function(rec){
        for(var i = 0; i<rec.data.length; i++){
            var sd = rec.data[i].startdate;
            var ed = rec.data[i].enddate;
            var asd = rec.data[i].actstartdate;

            if(sd && (ed || asd)){
                sd = new Date(WtfGlobal.convertDate(sd, Wtf.systemTzOffset))
                ed = new Date(WtfGlobal.convertDate(ed, Wtf.systemTzOffset))
                asd = new Date(WtfGlobal.convertDate(asd, Wtf.systemTzOffset))
                rec.data[i].startdate = new Date(WtfGlobal.convertDate(sd.format('Y-m-j H:i:s'), Wtf.usrTzOffset)).format('Y-m-j');
                rec.data[i].enddate = new Date(WtfGlobal.convertDate(ed.format('Y-m-j H:i:s'), Wtf.usrTzOffset)).format('Y-m-j');
                rec.data[i].actstartdate = new Date(WtfGlobal.convertDate(asd.format('Y-m-j H:i:s'), Wtf.usrTzOffset)).format('Y-m-j');
            }
        }
        return rec;
    },

    getDatesInRecords: function(rec){
        for(var i = 0; i<rec.data.length; i++){
            var sd = rec.data[i].startdate;
            var ed = rec.data[i].enddate;
            var asd = rec.data[i].actstartdate;

            if(sd && (ed || asd)){
                sd = new Date(WtfGlobal.getDateFromString(sd));
                ed = new Date(WtfGlobal.getDateFromString(ed));
                asd = new Date(WtfGlobal.getDateFromString(asd));
                rec.data[i].startdate = sd.format('Y-m-j');
                rec.data[i].enddate = ed.format('Y-m-j');
                rec.data[i].actstartdate = asd.format('Y-m-j');
            }
        }
        return rec;
    },

    updateGridFromOutside: function(msg){
        var JSONobj = msg.data;
        this.clientServerChange = 'server';
        var chartControl = this.chartCtl;
        var g = "E0FFE0", r="FFE3E3", sec = 10;
        if(this.getSelectionModel().getCount())
            this.tempSelections = this.getSelectionModel().getSelections();
//        this.getSelectionModel().clearSelections(false);
        if(JSONobj.userid && parseFloat(JSONobj.userid)!=random_number) {
            var obj1,temprecord;
            var action = JSONobj.action;
            var row = parseInt(JSONobj.rowindex);
            var taskid = JSONobj.taskid;
            if(action !="assignresource" && action!="deleteTask" && action!="importFile") {
                obj1 = eval('(' + JSONobj.data + ')');
                obj1 = this.getDatesInRecords(obj1);
                temprecord = this.GridJsonReader.readRecords(obj1);
            }
            if(this.connstatus != 6 && this.connstatus != 7)
                var uFlg = true;
            else {
                uFlg = false;
//                uFlg = this.isTaskExistInStore(taskid);
            }
            if(action=='insert' && uFlg) {
               if(this.search(temprecord.records[0].data['taskid']) == null ) {
                    this.dstore.insert(row, temprecord.records[0]);
                    var temprow = row;
                    if(this.dragFlag){
                        this.dragFlag = false;
                        temprow = ((this.dragOffset - 5) / 21) - 1;
                    }
                    chartControl.shiftdown(this.getSuccessorTaskIds(temprow), 21, true);
                    chartControl.insertPanelOnDataLoad(temprecord.records[0], temprow);
                    this.krow = row;
                    this.dragOffset = ((this.dstore.getCount() - 1) * 21) + 5;
                    this.ArrangeNumberer(this.krow);
                    this.highlightRow(g,sec,this.krow);
               }
            }
            if(action == "updatepercentcomplete"){
                var t = eval("(" + msg.data.data + ")");
                var o = this.dstore.query("taskid", t.data.taskid);
                if(o !== undefined){
                    o = o.items[0];
                    var sc = this.backupclass('Panel' + o.data['taskid'], -1);
                    o.set("percentcomplete", t.data.percentcomplete);
                    this.assignclass('Panel' + o.data['taskid'], sc);
                    var ri = this.search(o.data['taskid']);
                    this.highlightRow(g,sec,ri);
                    if(o.data["parent"]!= '0')
                        this.setParentCompletion(o.data["parent"])
                }
            }
            if(action=='newtask' && uFlg) {
                var oldrecord = this.dstore.data.items[row];
                this.dstore.insert(row, temprecord.records[0]);
                chartControl.shiftdown(this.getSuccessorTaskIds(row), 21, true);
                this.dragOffset += 21;
                chartControl.reassignLinks();
                this.krow = row;
                this.ArrangeNumberer(this.krow);
                if(this.showResourceFlag) {
                    this.chartCtl.showResources(this.getResources(row+1), this.getSuccessorTaskIds(row), true, this.allResources);
                }
                this.highlightRow(g,sec,this.krow);
            } else if(action=='update' && uFlg) {
                var delrecord = this.dstore.data.items[row];
                sc = this.backupclass('Panel' + delrecord.data['taskid'], -1);
//                var disp = this.getView().getRow(row).style.display;
                this.dstore.remove(delrecord);
                this.dstore.insert(row,temprecord.records[0]);
//                this.getView().getRow(row).style.display = disp;
                this.assignclass('Panel' + delrecord.data['taskid'], sc);
                chartControl.insertPanelOnDataLoad(temprecord.records[0],row);
                if(delrecord.data['percentcomplete']!=temprecord.records[0].data['percentcomplete'])
                    chartControl.setTaskProgress(temprecord.records[0]);
                if(delrecord.data['predecessor']!=temprecord.records[0].data['predecessor'])
                       this.LinkChanges_AfterPredecessorEdit(delrecord.data['predecessor'],temprecord.records[0]);
                chartControl.mysubtask('Panel' + temprecord.records[0].data['taskid']);
                this.checkGridRowStyle(temprecord.records[0],row);
                if(!this.deleteTaskFlag){
                    this.highlightRow(g,sec,row);
                }
                this.deleteTaskFlag = false;
            } else if(action=='deleteTask') {
//                this.containerPanel.loadingMask.show();
                obj1 = eval('(' + JSONobj.data + ')');
                for(var i=0; i<obj1.taskid.length; i++){
                    var temp = obj1.taskid[i].taskid;
                    var rowi = this.search(temp);
                    this.highlightRow(r, 5, rowi);
                }
                //this.deleteTaskOnServerResponse(obj1);
                this.timeout = new Wtf.util.DelayedTask(function(){
                    this.deleteTaskOnServerResponse(obj1);
                },this);
                this.timeout.delay(1000);
                this.deleteTaskFlag = true;
            } else if(action=='predecessor' && uFlg) {
                var delrecord1 = this.dstore.data.items[row];
                var oldPred = delrecord1.data['predecessor'];
                var sc1 = this.backupclass('Panel' + delrecord1.data['taskid'], -1);
                delrecord1.set('predecessor', temprecord.records[0].data['predecessor']);
                this.assignclass('Panel' + delrecord1.data['taskid'], sc1);
                this.LinkChanges_AfterPredecessorEdit(oldPred, temprecord.records[0]);
                this.highlightRow(g,sec,row);
            } else if(action=='indent' && uFlg) {
                this.krow = row;
                var record = this.dstore.data.items[row];
                if(temprecord.records[0].data['level']!=record.data['level'])
                    this.indent();
                this.highlightRow(g,sec,row);
            } else if(action=='outdent' && uFlg) {
                this.krow = row;
                var record1 = this.dstore.data.items[row];
                if(temprecord.records[0].data['level']!=record1.data['level'])
                    this.outdent();
                this.highlightRow(g,sec,row);
            } else if(action=='paste' || action == 'template' && uFlg) {
                if(action == 'paste'){
                    showMainLoadMask(WtfGlobal.getLocaleText('lang.pleaseeait.text')+'...');
                }
                this.pasteTaskOnServerRes(temprecord.records,row);
                this.ArrangeNumberer(row);
                var childrec = this.dstore.getAt((row+temprecord.records.length)-1);
                if(childrec.data["parent"]!= '0')
                   this.setParentCompletion(childrec.data["parent"])
            } else if(action=="assignresource") {
                obj1 = eval('(' + JSONobj.data + ')');
                obj1 = this.getDatesInRecords(obj1);
                var tempObj = eval("(" + obj1.data + ")");
                var record2 = this.dstore.data.items[row];
                if(this.connstatus >= 6 && this.connstatus <= 7){
                    this.checkResourceOfTask(obj1.resourcename, JSONobj.taskid, tempObj);
                } else {
                    sc = this.backupclass('Panel' + record2.data['taskid'], -1);
                    record2.set('resourcename', obj1.resourcename);
                    this.assignclass('Panel' + record2.data['taskid'], sc);
                    this.checkGridRowStyle(record2,row);
                    this.highlightRow(g,sec,row);
                }
            } else if(action=="importFile" && uFlg) {
                this.dstore.removeAll();
//                this.containerPanel.loadingMask.show();
                showMainLoadMask(WtfGlobal.getLocaleText('pm.loading.text')+'...');
                dojo.cometd.unsubscribe("/projectplan/"+this.pid);
                chartControl.clearChart();
                this.getProjectMinMaxDate();
            } else if (action == "updateGroupTask" && uFlg)
                this.UpdateTasksOnServerRes(temprecord.records);
            else if (action == "insertFromToDo" && uFlg){
                this.pasteTaskOnServerRes(temprecord.records, row);
                this.ArrangeNumberer(row);
            }
        } else if(parseFloat(JSONobj.userid)==random_number) {
            action = JSONobj.action;
            row = parseInt(JSONobj.rowindex);
            obj1 = eval('(' + JSONobj.data + ')');
            if(action != 'deleteTask')
                obj1 = this.getDatesInRecords(obj1);
            if(action=='insert') {
                var newRecord = this.GridJsonReader.readRecords(obj1).records[0];
                var curIndex = this.search('0');
                this.dragOffset += 21;
                if(curIndex != row) {
                    this.dstore.remove(this.dstore.data.items[curIndex]);
                    this.dstore.insert(row, newRecord);
                    chartControl.shiftdown(this.getSuccessorTaskIds(row), 21, true);
                    chartControl.reassignLinks();
                    this.clientServerChange = 'server';
                    chartControl.insertPanelOnDataLoad(newRecord, row);
                    this.krow = row;
                    this.ArrangeNumberer(this.krow);
                } else {
                    record = this.dstore.data.items[row];
                    this.dstore.remove(record);
                    this.dstore.insert(row, newRecord);
                    this.clientServerChange = 'server';
                    temprow = row;
                    if(this.dragFlag){
                        this.dragFlag = false;
                        temprow = ((this.dragOffset - 5) / 21) - 1;
                    }
                    chartControl.insertPanelOnDataLoad(newRecord, temprow);
                }
                var count = this.dstore.getCount();
                if (count == (row + 1))
                        this.addBlankRow();
            } else if(action=='newtask') {
                newRecord = this.GridJsonReader.readRecords(obj1).records[0];
                this.dstore.insert(row, newRecord);
                chartControl.shiftdown(this.getSuccessorTaskIds(row), 21, true);
                this.dragOffset += 21;
                chartControl.reassignLinks();
                this.getView().getCell(row, 3).firstChild.style.marginLeft = 20 * (newRecord.data['level']) + 'px';
                this.arrangePredecessor(row, 1);
                this.ArrangeNumberer(row);
                if(this.showResourceFlag) {
                    this.chartCtl.showResources(this.getResources(row+1), this.getSuccessorTaskIds(row), true, this.allResources);
                }
            } else if(action == 'paste' || action == 'template') {
                if(action == 'paste'){
                    showMainLoadMask(WtfGlobal.getLocaleText('lang.pleasewait.text')+'...');
                }
                temprecord = this.GridJsonReader.readRecords(obj1);
                this.pasteTaskOnServerRes(temprecord.records,row);
                this.ArrangeNumberer(row);
            } else if(action=='deleteTask') {
                this.clientServerChange='client';
                obj1 = eval('(' + JSONobj.data + ')');
                this.deleteTaskOnServerResponse(obj1);
                if(this.chartCtl.updatedTaskIds.length > 0)
                    this.updatedTasks_ServerRequest(this.chartCtl.updatedTaskIds);
                if(this.chartCtl.updatedParentTaskIds.length > 0)
                    this.updatedParentTasks_ServerRequest(this.chartCtl.updatedParentTaskIds);
            }
        }
        if(this.tempSelections){
            var sm = this.getSelectionModel();
            sm.clearSelections();
            sm.selectRecords(this.tempSelections, true);
        }
        this.clientServerChange = 'client';
        bHasChanged=true;
        if(refreshDash.join().indexOf("all") == -1 && refreshDash.join().indexOf("prj."+this.pid) == -1){
            refreshDash[refreshDash.length] = 'prj.'+this.pid;
            refreshDash[refreshDash.length] = 'pjh';
            refreshDash[refreshDash.length] = 'qkl';
        }
    },

    isTaskExistInStore: function(id){
        var r = this.dstore.query("taskid", id, false);
        return (r.length > 0);
    },
    checkResourceOfTask: function(resources, taskid, taskRec){
        var res = resources.split(",");
        var rE = false;
        var tE = false;
        for(var cnt = 0; cnt < res.length; cnt++){
            if(res[cnt] == loginid)
                rE = true;
        }
        var tR = this.dstore.query("taskid", taskid,false);
        if(tR.length > 0)
            tE = true;
        if(rE && !tE){//insert task
            var tTask = new this.Task({
                taskindex: taskRec["taskindex"],
                taskname: taskRec["taskname"],
                startdate: WtfGlobal.sqlToJsDate(taskRec["startdate"]),
                enddate: WtfGlobal.sqlToJsDate(taskRec["enddate"]),
                taskid: taskRec["taskid"],
                notes: taskRec["notes"],
                duration: taskRec["duration"],
                predecessor: "",
                resourcename: taskRec["resources"],
                percentcomplete: taskRec["percentcomplete"],
                parent: taskRec["parent"],
                level: taskRec["level"]
            });
            var nI = this.findGridIndex(taskRec["taskindex"]);
            this.dstore.insert(nI, tTask);
            this.getView().refresh();
            this.chartCtl.insertPanelOnDataLoad(tTask, nI);
            this.chartCtl.shiftdown(this.getSuccessorTaskIds(nI), 21, true);
            this.highlightRow("E0FFE0",10,nI);
        } else if(!rE && tE){//delete task
            var idx = this.dstore.query("taskid", taskRec["taskid"], false);
            var tI = this.search(taskRec["taskid"]);
            if(idx.length != 0){
                var tPanel = Wtf.getCmp("Panel" + taskRec["taskid"]);
                if(tPanel){
                    tPanel.destroyPanel();
                    this.chartCtl.remove(tPanel.id,true);
                    this.chartCtl.shiftup(this.getSuccessorTaskIds(tI), 21, true);
                }
                this.dstore.remove(idx.items[0]);
                this.getView().refresh();
            }
        }
    },
    findGridIndex: function(tI){
        for(var cnt = 0; cnt < this.dstore.getCount(); cnt++){
            if(this.dstore.getAt(cnt).data["taskindex"] > tI){
                break;
            }
        }
        return (cnt);
    },
    pasteTaskOnServerRes: function(temprecord, row) {
        var selectedRow = row;
        var tempRecLength = temprecord.length;
        var chartControl = this.chartCtl;
        var editorView = this.getView();
        for (var i = 0; i < tempRecLength; i++) {
            var irec = temprecord[i];
//            irec.data.resourcename = this.getResourceNameFromId(irec.data.resourcename);
            var nextrec = temprecord[i+1];
            var ti = temprecord[i].data['taskindex'];
            if(!ti || ti == '' || ti == 'undefined')
                temprecord[i].data['taskindex'] = row+1;
            this.dstore.insert(row, temprecord[i]);
            //this.clientServerChange = 'server';
            chartControl.shiftdown(this.getSuccessorTaskIds(row), 21, true);
            this.dragOffset += 21;
            if(irec.data['startdate'] != "") {
                chartControl.insertPanelOnDataLoad(irec, row);
                chartControl.mysubtask('Panel' + irec.data['taskid']);
            }
            if(irec.data['parent']!='0' && irec.data['parent']!='undefined') {
                var level = parseInt(this.dstore.data.items[this.search(irec.data['parent'])].data['level']) + 1;
                editorView.getCell(row, 3).firstChild.style.marginLeft = (level * 20) + 'px';
                irec.data['level'] = level;
            } else
                irec.data['level'] = 0;
            if (nextrec) {
                if ((irec.data['taskid'] || irec.data['parent']) == nextrec.data['parent']) {
                    editorView.getCell(row, 3).firstChild.firstChild.className = 'Dminus';
                    editorView.getCell(row, 3).firstChild.lastChild.className = 'imgtext';
                    Wtf.get(editorView.getRow(row)).addClass('parentRow');
                    //this.clientServerChange = 'server';
                    chartControl.indentGrid(irec);
                    chartControl.RecursionFunToTraceLink('Panel' + irec.data['taskid']);
                } else {
                    editorView.getCell(row, 3).firstChild.firstChild.className = 'minus';
                    editorView.getCell(row, 3).firstChild.lastChild.className = 'defaulttext';
                }
            }
            this.highlightRow("E0FFE0", 10,row);
            row++;
        }
        for (i = 0; i < tempRecLength; i++) {
            var irec = temprecord[i];
            if(irec.data["predecessor"] != ""){
                var np = "";
                var op = irec.data["predecessor"].split(",");
                for(var ctr = 0; ctr < op.length; ctr++){
                    np += (this.search(op[ctr]) + 1) + ",";
                    chartControl.checkForAttachedtaskPosition('Panel' + op[ctr], 'Panel' + irec.data["taskid"]);
                    chartControl.AttachNewlink('jg_Panel' + op[ctr] + '_Panel' + irec.data["taskid"], 'Panel' + op[ctr], "Panel" + irec.data["taskid"], false);
                }
                if(np != ""){
//                    irec.data["predecessor"] = np.substring(0, (np.length - 1));
                    var sc = this.backupclass('Panel' + irec.data['taskid'], -1);
                    irec.set('predecessor', np.substring(0, (np.length - 1)));
                    this.assignclass('Panel' + irec.data['taskid'], sc);
                }
            }
        }
        chartControl.reassignLinks();
//        this.arrangePredecessor(selectedRow, tempRecLength);
//        this.containerPanel.loadingMask.hide();
        if(this.showResourceFlag) {
            this.chartCtl.showResources(this.getResources(selectedRow), this.getSuccessorTaskIds(selectedRow-1), true, this.allResources);
        }
        hideMainLoadMask();
    },

    UpdateTasksOnServerRes : function(temprecord) {
        var tempRecLength = temprecord.length;
        var row = 0;
        for (var i = 0; i < tempRecLength; i++) {
            row = this.search(temprecord[i].data['taskid']);
            var record = this.dstore.data.items[row];
//            var disp = this.getView().getRow(row).style.display;
            var sc = this.backupclass('Panel' + record.data['taskid'], -1);
            this.dstore.remove(record);
            this.dstore.insert(row,temprecord[i]);
//            this.getView().getRow(row).style.display = disp;
            this.assignclass('Panel' + record.data['taskid'], sc);
            if(Wtf.getCmp('Panel' + record.data['taskid']) === undefined || i == 0)
                this.chartCtl.insertPanelOnDataLoad(temprecord[i],row);
            if(record.data['percentcomplete']!=temprecord[i].data['percentcomplete'])
                this.chartCtl.setTaskProgress(temprecord[i]);
            this.chartCtl.mysubtask('Panel' + temprecord[i].data['taskid']);
            this.checkGridRowStyle(temprecord[i],row);
        }
        this.highlightRow("E0FFE0",10,row);
    },

    highlightRow: function(color,duration,row){
        var rowEl = this.getView().getRow(row);
        Wtf.fly(rowEl).highlight(color
            ,{attr: "background-color",
                easing: 'easeIn',
                duration: duration,
                endColor: "ffffff"
        });
    },
    arrangePredecessor: function(rindex, count) {
        var length = this.dstore.getCount();
        if (length != 0) {
            var gridData = this.dstore.data;
            for (var a = 0; a < length; a++) {
                if (gridData.items[a].data['predecessor'] != "") {
                    var currentpred = gridData.items[a].data['predecessor'] + "";
                    var tempstr = currentpred.split(',');
                    var temppred = "";
                    var tempLength = tempstr.length;
                    for (var b = 0; b < tempLength; b++) {
                        if (tempstr[b] > rindex)
                            temppred += (parseInt(tempstr[b]) + count) + ",";
                        else
                            temppred += tempstr[b] + ",";
                    }
                    temppred = temppred.substr(0, temppred.length - 1);
                    var sc = this.backupclass('Panel' + gridData.items[a].data['taskid'], -1);
                    gridData.items[a].set('predecessor', temppred);
                    this.assignclass('Panel' + gridData.items[a].data['taskid'], sc);
                }
            }
         }
     },

    insertProxyPanel : function(stDate,endDate) {
        this.selModel.clearSelections();
        var record = this.dstore.data.items[this.dstore.getCount()-1];
        record.set('startdate',stDate);
        record.set('enddate',endDate);
        record.set('actstartdate',stDate);
        var duration = this.CalDur_ProxyPanel(record);
        record.set('duration', duration);
        record.set('actualduration', duration);
        record.set('percentcomplete', 0);
        this.updateDB(record);
//        this.dragOffset += 21;
        this.addBlankRow();
    },

    CalDur_ProxyPanel : function(record) {
        var stdate = record.data['startdate'];
        var enddate = record.data['enddate'];
        //var actualDuration = 0;
        /*if(stdate.format('w') == 6 || stdate.format('w') == 0) {
            if(stdate.format('w') == 6)
                stdate = stdate.add(Date.DAY,2);
            else if(stdate.format('w') == 0)
                stdate = stdate.add(Date.DAY,1);
            //actualDuration = 1;
            record.set('startdate',stdate);
        }*/
        var dur = this.findWorkingStEndDate(stdate,0);
        if(dur!=0) {
            stdate = stdate.add(Date.DAY, dur);
            record.set('startdate',stdate);
        }

        /*if(enddate.format('w') == 6 || enddate.format('w') == 0) {
            if(enddate.format('w') == 6)
                enddate = enddate.add(Date.DAY,-1);
            else if(enddate.format('w') == 0)
                enddate = enddate.add(Date.DAY,-2);
            if(this.timeDifference(enddate.clearTime(false), stdate.clearTime(false))>=0)
                record.set('enddate',enddate);
            else {
                record.set('enddate',record.data.startdate);
                enddate = record.data.startdate;
            }
        }*/
        var val = this.findEndDateOnResize(enddate,0);
        if(val != 0) {
            enddate = enddate.add(Date.DAY, val);
            if(this.timeDifference(enddate.clearTime(false), stdate.clearTime(false))>=0)
                record.set('enddate',enddate);
            else {
                record.set('enddate',record.data.startdate);
                enddate = record.data.startdate;
            }
        }
        var duration = 0;
        if(stdate <= enddate)
            duration = this.CalculateActualDuration(stdate,enddate);
        else
            record.set('enddate',record.data['startdate']);
        //return(actualDuration + duration);
        return(duration);
    },

    getvalidDuration: function (str) {
        var slength = str.length;
        var dur;
        var patt1=new RegExp(".");
        if(str.search(patt1)!=-1){
            if((index=str.search(patt1.compile("d")))!=-1){
                if(str.substr(0, index)) // if-else check for chrome
                    dur = parseFloat(str.substr(0, index)).toFixed(2) + str.substr(index, slength);
                else
                    dur = str.substr(index, slength);
            } else if((index=str.search(patt1.compile("h")))!=-1){
                if(str.substr(0, index))
                    dur = parseFloat(str.substr(0, index)).toFixed(2) + str.substr(index, slength);
                else
                    dur = str.substr(index, slength);
            } else {
                dur = parseFloat(str).toFixed(2);
            }
            return dur;
        } else {
            return str;
        }
    },

    deleteTaskOnServerResponse : function(obj1) {
//        this.containerPanel.loadingMask.show();
        showMainLoadMask(WtfGlobal.getLocaleText("pm.loading.text")+'...');
        var prevArray = [];
        var chartControl = this.chartCtl;
        chartControl.deleteTasks(obj1.taskid);
        var gridData = this.dstore.data;
        var taskidLength = obj1.taskid.length;
        var parentRecs = {};
        for(var cnt = 0; cnt < taskidLength; cnt++) {
            var delIndex = this.search(obj1.taskid[cnt].taskid);
            if(delIndex != null) {
                var prevRec = gridData.items[delIndex - 1];
                if(prevRec){
                    prevArray[prevArray.length] = prevRec.data['taskid'];
                }
                var record = gridData.items[delIndex];
                if(record.data["parent"] !== undefined && record.data["parent"] != "0")
                    parentRecs[record.data["parent"]] = record.data["parent"];
                this.dstore.remove(record);
                if(Wtf.get('Panel' + record.data['parent']) != null) {
                    chartControl.mysubtask('Panel' + record.data['parent']);
                    this.setParentCompletion(record.data["parent"]);
                    if(this.clientServerChange =='client') {
                        this.parentIDPercentChange[this.parentIDPercentChange.length] = record.data["parent"];
                        if(this.parentIDPercentChange.length > 0)
                            this.updatedParentTasks_PercentComp(this.parentIDPercentChange);
                    }
                }
            }
        }
        this.afterDelete(prevArray);
        this.adjustParent(parentRecs);
        this.ArrangeNumberer(0);
        chartControl.reassignLinks();
        if(this.showResourceFlag) {
            this.chartCtl.showResources(this.getResources(), this.getSuccessorTaskIds(-1), true, this.allResources);
        }
//        this.containerPanel.loadingMask.hide();
        hideMainLoadMask();
        if(delIndex * 21 < this.containerPanel.wraper.getScrollState().top) {
            this.containerPanel.wraper.scrollTo(0, delIndex * 21);
        }
    },

    searchText: function() {
        var text = this.containerPanel.searchTask.getValue();
        var sm = this.selModel;
        sm.clearSelections();
        if(text.charAt(0) == "#" && text.length > 1) {
            var row = text.substr(1) - 1;
            if(row < this.dstore.getCount() - 1) {
                sm.selectRow(row);
                this.containerPanel.wraper.scrollTo(Wtf.getCmp('Panel' + this.dstore.getAt(row).data.taskid).x - 16, row * 21);
            } else {
                this.containerPanel.wraper.scrollToTop();
            }
            return;
        }
        var records = this.dstore.query('taskname', text,true);
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

    showPriority: function(flag) {
        var gridStore = this.dstore;
        if(this.showPriorityFlag) {
            this.showPriorityFlag = false;
        } else if(!this.showPriorityFlag) {
            this.showPriorityFlag = true;
        }
        var count = gridStore.getCount();
        for(var i = 0; i < count; i++) {
            this.checkPriority(gridStore.getAt(i),i);
        }
    },

    checkPriority : function(record,rowIndex) {
        var plannerView = this.getView();
        if(this.showPriorityFlag) {
            if(record.data.priority == Wtf.proj.pr.LOW) {
                Wtf.get(plannerView.getRow(rowIndex)).dom.style.color = "#009933";
            } else if(record.data.priority == Wtf.proj.pr.HIGH) {
                Wtf.get(plannerView.getRow(rowIndex)).dom.style.color = "#CC0033";
            }
        } else {
            Wtf.get(plannerView.getRow(rowIndex)).dom.style.color = "black";
        }
    },

    checkGridRowStyle : function(record, rowIndex ) {
        if(this.showTaskProgress) {
            var taskIdArray = [];
            taskIdArray[0] = record.data['taskid'];
            this.chartCtl.showTaskProgress(taskIdArray, true);
        }if(this.showResourceFlag) {
            this.showResourcesOnUpdate(record, rowIndex);
        }if(this.showPriorityFlag) {
            this.checkPriority(record, rowIndex);
        }if(this.chartCtl.showOverDue) {
            Wtf.getCmp('Panel' + record.data.taskid).checkOverDue(true);
        }
    },

    refreshPlan : function() {
        showMainLoadMask(WtfGlobal.getLocaleText('pm.loading.text')+'...');
        if(this.showResourceFlag){
            this.chartCtl.showResources([], this.getSuccessorTaskIds(-1), false, this.allResources);
            this.containerPanel.showResources.toggle(false);
        }
        this.dstore.removeAll();
        dojo.cometd.unsubscribe("/projectplan/"+this.pid);
        this.chartCtl.clearChart();
        this.getProjectMinMaxDate();
        this.importingFile();
    },

    checkStartDate: function(date){
        if(typeof this.projStartDate == 'string')
            var dt = Date.parseDate(this.projStartDate, "Y-m-j H:i:s");
        if(!dt){
            if(typeof this.containerPanel.projstartdate == 'object')
                dt = this.containerPanel.projstartdate;
        }
        if(dt.format('Y-m-d') > new Date().format('Y-m-d')){
            date = dt;
        }
        return date;
    },

    findWorkingStEndDate : function(dateVal,dur) { // initial values of dur = 0;
        var flag = true;
        while(flag) {
            if(this.NonworkWeekDays.join().indexOf(dateVal.format('w'))!=-1 || (this.HolidaysList.join().indexOf(dateVal.format('d/m/Y'))!= -1)) {
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
            if(this.NonworkWeekDays.join().indexOf(stdate.format('w'))!=-1 || (this.HolidaysList.join().indexOf(stdate.format('d/m/Y')) != -1)) {
                stdate= stdate.add(Date.DAY,1);
                nonworkcnt += 1;
                //this.sdateForNonWorkCal(stdate,nonworkcnt);
            } else
                flag = false;
        }
        return nonworkcnt;
    },

    findEndDateOnResize : function(enddate,dur) { // initial values of dur = 0;
        var flag = true;
        while(flag) {
            if(this.NonworkWeekDays.join().indexOf(enddate.format('w'))!=-1 || (this.HolidaysList.join().indexOf(enddate.format('d/m/Y')) != -1)) {
                enddate = enddate.add(Date.DAY, -1);
                dur -=1;
                //this.findEndDateOnResize(enddate,dur);
            } else {
                //this.endDate = enddate;
                flag = false;
            }
        }
        return dur;
    },

    scheduleConfWin : function(resObj) {
        var recordData = Wtf.data.Record.create([
            {name: 'taskindex'},
            {name: 'taskname'},
            {name: 'startdate'},
            {name: 'enddate'},
            {name: 'level'}
        ]);
        var JsonReader1 = new Wtf.data.KwlJsonReader({
            root: "data"
        }, recordData);

        this.resConflicDS = new Wtf.data.Store({
            url :Wtf.req.prj + 'resources.jsp',
            baseParams : {
                pid: this.pid,
                action: 12
            },
            reader: JsonReader1
        });

        var listcm = new Wtf.grid.ColumnModel([{
            header: WtfGlobal.getLocaleText('lang.ID.text'),
            dataIndex: 'taskindex',
            width : 20
        },{
            header: WtfGlobal.getLocaleText('pm.common.tasks.text'),
            dataIndex: 'taskname'
        },{
            header: WtfGlobal.getLocaleText('pm.project.calendar.newevent.startdate'),
            dataIndex: 'startdate',
            renderer: function(value){
                if(value!='')
                   return Date.parseDate(value, "Y-m-d").format(WtfGlobal.getOnlyDateFormat());
            }
        },{
            header: WtfGlobal.getLocaleText('pm.common.enddate'),
            dataIndex: 'enddate',
            renderer: function(value){
                if(value!='')
                   return Date.parseDate(value, "Y-m-d").format(WtfGlobal.getOnlyDateFormat());
            }
        }]);

        var confResGrid = new Wtf.grid.GridPanel({
            store: this.resConflicDS,
            cm: listcm,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true
            }
        });

        new Wtf.Window({
            title: WtfGlobal.getLocaleText('pm.project.resource.conflict'),
            layout: "border",
            id : 'conflictres'+this.id,
            resizable: false,
            iconCls : 'iconwin',
            modal: true,
            autoScroll : true,
            height: 450,
            width: 540,
            items: [{
                region: "north",
                height : 75,
                border : false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                html: getTopHtml(WtfGlobal.getLocaleText('pm.project.resource.conflict'),WtfGlobal.getLocaleText('pm.project.resource.conflict'),"../../images/resourceconflict40_52.gif")
            },{
                region: "center",
                id : 'conflictRes'+this.id,
                border: false,
                bodyStyle : 'background:#f1f1f1;font-size:10px;',
                layout: "fit",
                items: confResGrid
            }],
            buttons:[{text: WtfGlobal.getLocaleText('lang.close.text'),
                scope: this,
                handler: function(){
                    Wtf.getCmp('conflictres'+this.id).close();
                }
            }]
        }).show();

        this.resConflicDS.on('load', function(store, record, option){
            var count = store.getCount();
            var reportView = confResGrid.getView();
            for(var cnt = 0; cnt < count; cnt++){
                if(store.getAt(cnt).data.level==0)
                    reportView.getRow(cnt).className = 'conflires';
                else if(store.getAt(cnt).data.level==1)
                    reportView.getRow(cnt).className = 'repoBold';
                var ML = parseInt(store.getAt(cnt).data['level'])*15;
                reportView.getCell(cnt,1).firstChild.style.marginLeft = ML + 'px';
            }
        }, this);
        this.resConflicDS.load();
    },

    NumCompanyHoliday : function(stdate,enddate) {
        var NumHolidays = 0 ;
        for(var cnt =0; cnt< this.HolidaysList.length;cnt++) {
            var currHoliday = Date.parseDate(this.HolidaysList[cnt], "d/m/Y");
            if(this.NonworkWeekDays.join().indexOf(currHoliday.format('w')) == -1) { // some Public Holidays fall on WeekEnd days, and NOT need to count.
                if(currHoliday.between(stdate,enddate)) { // Checks if this date falls on or between the given start and end dates
                    NumHolidays += 1;
                }
            }
        }
        return NumHolidays;
    },

   resourceConflictTab:function(tabTitle,tabId){
        this.tplRow = [];

        var TaskRec = Wtf.data.Record.create([
            {name: 'taskid'},
            {name: 'taskname'},
            {name: 'taskindex'},
            {name: 'startdate', type: 'date', dateFormat: 'Y-m-d'},
            {name: 'enddate', type: 'date', dateFormat: 'Y-m-d'},
            {name: 'resourcename'},
            {name: 'conflictedtasks'}
        ]);

        var taskReader = new Wtf.data.KwlJsonReader({
            root: "data",
            id: 'task-reader'
        }, TaskRec);

        var taskGridStore = new Wtf.data.Store({
            url: Wtf.req.prj + 'resources.jsp',
            baseParams: {
                projectid: this.pid,
                action: "14"
            },
            reader:taskReader
        });
        var taskGridCM = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),{
                dataIndex: 'taskid',
                hidden: true,
                header: 'taskid'
            },{
                header: WtfGlobal.getLocaleText('pm.project.plan.task.attributes.index'),
                dataIndex: 'taskindex',
                width: 50
            },{
                header: WtfGlobal.getLocaleText('pm.common.taskname'),
                dataIndex: 'taskname',
                width: 130
            },{
                header: WtfGlobal.getLocaleText('pm.project.calendar.newevent.startdate'),
                dataIndex: 'startdate',
                width: 80,
                renderer: function(v){
                    return (v && typeof v == 'object') ? v.format(WtfGlobal.getOnlyDateFormat()) : v;
                }
            },{
                header: WtfGlobal.getLocaleText('pm.common.enddate'),
                dataIndex: 'enddate',
                width: 80,
                renderer: function(v){
                    return (v && typeof v == 'object') ? v.format(WtfGlobal.getOnlyDateFormat()) : v;
                }
            },{
                header: WtfGlobal.getLocaleText('pm.project.plan.task.attributes.resourcenames'),
                dataIndex: 'resourcename',
                width:130,
                hidden :true
            }]);


        taskGridStore.load({
            params :{resourceid:"all"}
        });

        taskGridStore.on("load",function(Store,records,options){
            this.tplRow = [];
            var flag=true;
            var resourceid = options.params.resourceid;
            var resolve = options.params.resolve;
            var taskid = options.params.taskid;
            if(resourceid !="all"){
                var count = Store.getCount();
                var taskgridView = taskGrid.getView();
                for(var k=0;k<count;k++){
                    var task=eval('('+Store.getAt(k).get("conflictedtasks")+')');
                    if(task.data!=undefined){


                        var taskRow = taskgridView.getRow(k);
                        taskRow.className += " conflicted";
                        taskRow.id = resourceid+"conf"+k;
                        if(Wtf.getCmp("TT"+taskRow.id)!=undefined)
                            Wtf.getCmp("TT"+taskRow.id).destroy();

                        var ctasks ="";
                        for (var i=0;i<task.data.length;i++) {
                            if(taskid == task.data[i].taskid)
                                flag=false;

                            var taskname= task.data[i].taskname;
                            if(taskname.length > 16)
                                taskname = taskname.substring(0, 14)+"..";
                            var st = WtfGlobal.dateRendererForOnlyDate(task.data[i].startdate);
                            var et = WtfGlobal.dateRendererForOnlyDate(task.data[i].enddate);
                            ctasks += "<div style='padding:2px'>"+task.data[i].taskindex+". "+taskname+" ["+task.data[i].projectname+"] "+st+" to "+et+"</div>";
                        }
                        var ttaskmsg = task.data.length==1?" task.":" tasks.";
                        var tasktooltip="<div style='padding:2px 2px 5px 2px;overflow-x:auto;overflow-y:auto;'>"+
                        "<div style='border-bottom:thin dotted #CCC;font-size:12px;color:#15428B;padding:3px;'>"+
                        "<span >"+ WtfGlobal.getLocaleText({key:'pm.project.plan.rescon.panelinfo',params:task.data.length}) +"</span>"+
                        "</div>"+
                        "<div style='background:#FAFAFF;padding:0 5px;overflow-x:auto;overflow-y:auto;max-height:150px;'>"+ctasks+"</div>"+
                        "</div>";

                        this.tplRow[k] = tasktooltip;
                    }
                }
                if(resolve == "true"){
                    if(flag == false){
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.327'),function(btn,text){
                            if(btn=='yes'){
                                Wtf.getCmp('conflictres'+this.id).close();
                            }
                        },this);

                    }
                    else{
                        Wtf.MessageBox.alert(WtfGlobal.getLocaleText('lang.status.text'), WtfGlobal.getLocaleText('pm.msg.rescon.resolved.text'),function(){
                            Wtf.getCmp('conflictres'+this.id).close();
                        },this);

                    }
                }
            }
        },this);


        var taskGridSM = new Wtf.grid.RowSelectionModel({
            singleSelect:true
        });
        var taskGrid = new Wtf.grid.GridPanel({
            title: WtfGlobal.getLocaleText('pm.project.plan.rescon.tasklist'),
            ds: taskGridStore,
            cm: taskGridCM,
            sm: taskGridSM,
            enableColumnMove: false,
            loadMask : true,
            frame: true,
            view: new Wtf.grid.GridView({
                emptyText:WtfGlobal.getLocaleText('pm.project.task.notask'),
                forceFit:true
            }),
            tbar:[this.resolveBut = new Wtf.Button({
                text:WtfGlobal.getLocaleText('pm.project.plan.rescon.resolve'),
                id : 'resolveconflict',
                tooltip:WtfGlobal.getLocaleText('pm.common.resolveconflict'),
                disabled :true,
                handler: function(event, toolEl, panel){
                    var record = taskGridSM.getSelected();
                    var tid= record.get("taskid");
                    var resourecRecord = resrcGridSM.getSelected();
                    var json = "{'data':[";
                    json += "{'taskid':'"+record.get("taskid")+"','taskindex':'"+record.get("taskindex")+"','taskname':'"+record.get("taskname")+"','startdate':'"+record.get("startdate").format(WtfGlobal.getOnlyDateFormat())+"','enddate':'"+record.get("enddate").format(WtfGlobal.getOnlyDateFormat())+"','resourcename':'"+record.get("resourcename")+"'},";
                    record=eval('('+record.get("conflictedtasks")+')');
                    for (var i=0;i<record.data.length;i++) {
                        if(record.data[i].projectid == this.pid){
                            var st = WtfGlobal.dateRendererForOnlyDate(record.data[i].startdate);
                            var et = WtfGlobal.dateRendererForOnlyDate(record.data[i].enddate);
                            json += "{'taskid':'"+record.data[i].taskid+"','taskindex':'"+record.data[i].taskindex+"','taskname':'"+record.data[i].taskname+"','startdate':'"+st+"','enddate':'"+et+"','resourcename':'"+record.data[i].resourcename+"'},";
                        }
                    }
                    json = json.substr(0, json.length-1);
                    json += "]}";
                    var res = Wtf.data.Record.create([
                    {
                        name: 'resourceid'
                    },

                    {
                        name: 'nickname'
                    },

                    {
                        name: 'resourcename'
                    },

                    {
                        name: 'colorcode'
                    },

                    {
                        name: 'inuseflag'
                    }
                    ]);
                    var gridResources = new Wtf.data.Store({
                        url: Wtf.req.prj + 'resources.jsp?action=15&projectid=' + this.pid,
                        reader: new Wtf.data.KwlJsonReader({
                            root: "data"
                        },res)
                    });
                    var Comboconfig = {
                        store: resrcGridStore,
                        displayField:'resourcename',
                        valueField:'resourceid' ,
                        triggerAction:'all',
                        mode:'local'
                    };
                    var gridmultiCombo = new Wtf.common.Select(Wtf.applyIf({
                        multiSelect:true,
                        fieldLabel:WtfGlobal.getLocaleText('lang.field.text'),
                        forceSelection:true
//                        renderer:function(val,record){
//                            var retval=val;
//                            retval='<span style="color:green">'+val+'</span>';
//                            return retval;
//                        }

                    }, Comboconfig));
                    var conflictedGridCM = new Wtf.grid.ColumnModel([
                        new Wtf.grid.RowNumberer(),
                        {
                            dataIndex: 'taskid',
                            hidden: true,
                            header: 'taskid'
                        },{
                            header: WtfGlobal.getLocaleText('pm.project.plan.task.attributes.index'),
                            dataIndex: 'taskindex',
                            width: 70
                        },{
                            header: WtfGlobal.getLocaleText('pm.common.taskname'),
                            dataIndex: 'taskname',
                            width: 130
                        },{
                            header: WtfGlobal.getLocaleText('pm.project.calendar.newevent.startdate'),
                            dataIndex: 'startdate',
                            width: 80
                        },{
                            header: WtfGlobal.getLocaleText('pm.common.enddate'),
                            dataIndex: 'enddate',
                            width: 80
                        },{
                            header: WtfGlobal.getLocaleText('pm.project.plan.rescon.resolveconflict.allocated'),
                            dataIndex: 'resourcename',
                            width:130,
                            renderer : this.comboBoxRenderer(gridmultiCombo)
                        },{
                            header: WtfGlobal.getLocaleText('pm.project.plan.rescon.resolveconflict.available'),
                            dataIndex: 'resources',
                            width:130,
                            renderer : this.comboBoxRenderer(gridmultiCombo),
                            editor: gridmultiCombo
                        }]);

                    var listds = new Wtf.data.JsonStore({
                        fields: [{
                            name:"taskid"
                        },

                        {
                            name:"taskindex"
                        },

                        {
                            name:"taskname"
                        },

                        {
                            name: 'startdate'
                        },

                        {
                            name: 'enddate'
                        },

                        {
                            name:"resourcename"
                        },

                        {
                            name:"resources"
                        }]
                    });
                    var task=eval('('+json+')');
                    listds.loadData(task.data);
                    var conflictGridSm= new Wtf.grid.RowSelectionModel();
                    var conflictGrid = new Wtf.grid.EditorGridPanel({
                        title: WtfGlobal.getLocaleText('pm.project.plan.rescon.resolveconflict.gridtitle'),
                        clicksToEdit:1,
                        ds: listds,
                        sm: conflictGridSm,
                        cm: conflictedGridCM
                    });

                    conflictGrid.on("afteredit",function(e){
                        var crec=listds.getAt(e.row);
                        var temp_h="";
                        var resources = crec.data.resources.split(",");
                        var jsonData = "[";
                        for( var nm=0;nm<resources.length;nm++){
                            var idx = resrcGridStore.find("resourceid", resources[nm]);
                            var rcd = resrcGridStore.getAt(idx);
                            jsonData +=  temp_h+"{resourceid:\""+rcd.data.resourceid+"\",nickname:\""+rcd.data.nickname+"\",resourcename:\""+rcd.data.resourcename+"\",colorcode:\""+rcd.data.colorcode+"\",inuseflag:\""+rcd.data.inuseflag+"\"}";
                            temp_h=",";
                        }
                        jsonData +="]";
                        this.storeResourcesInDB(crec.data.taskid,jsonData);
                    },this);

                    new Wtf.Window({
                        title: WtfGlobal.getLocaleText('pm.common.resolveconflict'),
                        layout: "border",
                        id : 'conflictres'+this.id,
                        resizable: false,
                        iconCls : 'iconwin',
                        modal: true,
                        autoScroll : true,
                        height: 450,
                        width: 680,
                        items: [{
                            region: "north",
                            height : 75,
                            border : false,
                            bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                            html: getTopHtml(WtfGlobal.getLocaleText('pm.common.resolveconflict'),WtfGlobal.getLocaleText('pm.project.plan.rescon.resolveconflict.subheader'),"../../images/resourceconflict40_52.gif")
                        },{
                            region: "center",
                            id : 'conflictRes'+this.id,
                            border: false,
                            bodyStyle : 'background:#f1f1f1;font-size:10px;',
                            layout: "fit",
                            items: conflictGrid
                        }],
                        buttons:[
                        {
                            text: WtfGlobal.getLocaleText('pm.project.plan.rescon.resolve'),
                            scope: this,
                            handler: function(){
                                taskGridStore.load({
                                    params :{
                                        resourceid:resrcGridSM.getSelected().get("resourceid"),
                                        resolve:'true',
                                        taskid:tid
                                    }
                                });
                                this.resolveBut.disable();
                            }
                        },{
                            text: WtfGlobal.getLocaleText('lang.close.text'),
                            scope: this,
                            handler: function(){
                                Wtf.getCmp('conflictres'+this.id).close();
                                resrcGridSM.selectRow(resrcGridStore.indexOf(resourecRecord));
                                this.resolveBut.disable();
                            }
                        }]
                    }).show();
                },
                scope:this
            })],
            tools:[{
                id:'refresh',
                qtip:"Reset grid",
                handler: function(event, toolEl, panel){
                    this.resolveBut.disable();
                    this.removeRowClass(resrcGrid,"resourceBold",false);
                    taskGridStore.load({
                        params :{
                            resourceid:"all",
                            resolve:'false'
                        }
                    });
                },
                scope:this
            }]
        });

        taskGridSM.on("selectionchange",function(sm){
            if(sm.getCount() > 0){
                var rec = sm.getSelected() ;
                var task=eval('('+rec.get("conflictedtasks")+')');
                if(task.data!=undefined)
                    this.resolveBut.enable();
                else
                    this.resolveBut.disable();

                this.removeRowClass(resrcGrid,"resourceBold",true);
                var selectedTask = rec.get("resourcename");
                if(selectedTask!=""){
                    var selectedTasks = rec.get("resourcename").split(",");
                    var rsgridView = resrcGrid.getView();
                    for(var i=0 ; i<selectedTasks.length ; i++){
                        var idx = resrcGridStore.find("resourceid", selectedTasks[i]);
                        if(idx !== -1)
                            rsgridView.getRow(idx).className += ' resourceBold';
                    }
                }
            }
        },this);
        var resrcGridStore = new Wtf.data.Store({
            url: Wtf.req.prj + 'resources.jsp?action=10&projid=' + this.pid,
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.RES)
        });
        resrcGridStore.load();

        var resrcGridCM = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),{
                dataIndex: 'resourceid',
                hidden: true,
                header: 'resourceid'
            },{
                header: WtfGlobal.getLocaleText('pm.project.resource.name'),
                dataIndex: 'resourcename'
            }]);

        var resrcGridSM = new Wtf.grid.RowSelectionModel({
            singleSelect:true
        });
        var resrcGrid = new Wtf.grid.GridPanel({
            title: WtfGlobal.getLocaleText('pm.project.plan.rescon.resourcelist'),
            ds: resrcGridStore,
            cm: resrcGridCM,
            sm: resrcGridSM,
            loadMask : true,
            frame: true,
            viewConfig:{
                forceFit:true
            },
            tools:[{
                id:'refresh',
                qtip:"Reset grid",
                handler: function(event, toolEl, panel){
                    this.resolveBut.disable();
                    resrcGridStore.load();
                    taskGridStore.load({
                        params :{
                            resourceid:"all",
                            resolve:'false'
                        }
                    });
                },
                scope:this
            }]
        });

        resrcGridSM.on("selectionchange",function(sm){
            if(sm.getCount() > 0){
                this.resolveBut.disable();
                this.removeRowClass(resrcGrid,"resourceBold",true);
                //taskGridStore.clearFilter();
                //taskGridStore.filter("resourceid",sm.getSelected().get("resourceid"),false,true);
                taskGridStore.load({
                    params :{
                        resourceid:sm.getSelected().get("resourceid"),
                        resolve:'false'
                    }
                });
            }
        },this);

        var resConfTab = new Wtf.Panel({
            title: tabTitle,
            id: tabId,
            layout: 'border',
            closable: true,
            border: false,
            mask: null,
            iconCls: "dpwnd conflicttabicon",
            items:[{
                region: "west",
                width : '20%',
                border : false,
                layout: "fit",
                items: resrcGrid

            },{
                region: "center",
                border: false,
                layout: "fit",
                items: taskGrid
            }]
        });
        taskGrid.on("mouseover", this.hideTplResourceConf, this);
        taskGrid.on("rowclick", this.showTplResourceConf, this);
        return resConfTab;
    },

    hideTplResourceConf : function() {
        var winResourceConf = Wtf.getCmp("winResourceConf");
        if(winResourceConf)
            winResourceConf.hide();
    },

    showTplResourceConf : function(obj, rowIndex, eventobj) {

        if(this.tplRow[rowIndex]) {
            var oldContainPane = Wtf.getCmp("containResourceConf");
            if(oldContainPane)      // if containpane already present then destroy it and create again as template could not be overwritten
                oldContainPane.destroy();

            var containResourceConf = new Wtf.Panel({
                id: "containResourceConf",
                frame: true,
                hideBorders: true,
                baseCls: "sddsf",
                header: false,
                headerastext: false
            });

            var oldWin1 = Wtf.getCmp("winResourceConf");
            if(oldWin1)             // if win1 already present then destroy it and create again
                oldWin1.destroy();

            var winWidth = 350;
            var winMaxHeight = 200;

            new Wtf.Window({
                id: "winResourceConf",
                animateTarget: obj,
                width: winWidth,
                maxHeight: winMaxHeight,
                plain: true,
                shadow: true,
                header: false,
                closable: false,
                border: false,
                resizable : false,
                items: containResourceConf
            }).show();

            this.tplResourceConf = new Wtf.Template(this.tplRow[rowIndex]);
            this.tplResourceConf.compile();
            this.tplResourceConf.insertAfter("containResourceConf");

            var winX = eventobj.getPageX();
            var winY = eventobj.getPageY();

            var winResourceConf = Wtf.getCmp("winResourceConf").el.dom;
            if((winX + winWidth) > mainPanel.el.dom.offsetWidth)
                winX = mainPanel.el.dom.offsetWidth - winWidth;

            if((winY + winResourceConf.offsetHeight) > mainPanel.el.dom.offsetHeight)
                winY = mainPanel.el.dom.offsetHeight - winResourceConf.offsetHeight;

            Wtf.getCmp("winResourceConf").setPagePosition(winX, winY);
        }
    },

    removeRowClass:function(grid,classname,keepSel){
        if(!keepSel)
            grid.getSelectionModel().clearSelections();
        var gridView = grid.getView();
        for(var j=0;j<grid.getStore().getCount();j++){
            var curcls = gridView.getRow(j).className;
            var clsarr = curcls.split(classname);
            gridView.getRow(j).className = clsarr.toString().replace(',','');
        }
    },

    performMove: function(direction, selrec){
        selrec = selrec.data;
        showMainLoadMask(WtfGlobal.getLocaleText('pm.movingtasks.text')+'...');
        if(direction == 'up'){
            var ti = selrec.taskindex;
            ti = (ti <= 1) ? 2 : ti;
            var prevParent = null;
            for(var x = ti-2; x >= 0; x--){
                prevParent = this.dstore.getAt(x);
                if(prevParent){ // for empty task
                    if((selrec.level == prevParent.data.level) && (selrec.parent == prevParent.data.parent)){
                        var newrec = prevParent;
                        break;
                    }
                }
            }
            if(newrec){
                this.loadbuffer("cutTask");
                this.requestDeleteTask();
                if(!this.pasteFlag){
                    this.timeout = new Wtf.util.DelayedTask(function(){
                        this.krow = this.search(newrec.data.taskid);
                        this.insertbuffer('paste');
                    },this);
                    this.timeout.delay(100);
                }
            }
        } else {
            ti = selrec.taskindex;
            for(x = ti; x < this.dstore.getCount(); x++){
                prevParent = this.dstore.getAt(x);
                if(prevParent){
                    if((selrec.level == prevParent.data.level) && (selrec.parent == prevParent.data.parent)){
                        newrec = prevParent;
                        break;
                    }
                }
            }
            if(newrec && newrec.data.taskid != '0'){
                this.getSelectionModel().selectRow(newrec.data.taskindex-1, false);
                this.performMove('up', newrec);
            } else {
                msgBoxShow(195, 1);
            }
        }
        hideMainLoadMask();
    },

    moveUp: function(btn){
        var sel = this.getSelectionModel().getSelections();
        var tpanel = Wtf.getCmp('Panel' + sel[0].data['taskid']);
        var ti = sel[0].data['taskindex'] - 1;
        if(ti) // new tasks did not had taskindexes before it was introduced in the getJsonForGrid
            var prevrec = this.dstore.getAt(ti - 1);
        else {
            var index = this.search(sel[0].data.taskid);
            if(index !== -1){ // fail-safe. if index not found set it manually
                sel[0].data.taskindex = index + 1;
                prevrec = this.dstore.getAt(index-1);
            }
        }
        if(!prevrec){
            msgBoxShow(194, 1);
            return;
        }
        if(sel[0].data['parent'] !== prevrec.get('taskid')){ // above task not parent of selected task
            if(tpanel){
                if(Wtf.encode(tpanel.predecessor).length == 2 && Wtf.encode(tpanel.successor).length == 2){
                    this.performMove('up', sel[0]);
                } else {
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('pm.common.plzconfirm'), WtfGlobal.getLocaleText('pm.msg.332'), function(btn){
                        if (btn == "yes") {
                            this.performMove('up', sel[0]);
                        }
                    }, this);
                }
            } else
                this.performMove('up', sel[0]);
        } else
            msgBoxShow(190, 1);
    },

    moveDown: function(btn){
        var sel = this.getSelectionModel().getSelections();
        var tpanel = Wtf.getCmp('Panel' + sel[0].data['taskid']);
        var ti = sel[0].data['taskindex'] - 1;
        if(ti)
            var nextrec = this.dstore.getAt(ti + 1);
        else {
            var index = this.search(sel[0].data.taskid);
            if(index !== -1){
                sel[0].data.taskindex = index + 1;
                nextrec = this.dstore.getAt(index+1);
            }
        }
        if(!nextrec){
            msgBoxShow(195, 1);
            return;
        } else if(nextrec.data.taskid == '0'){ // for last empty row in grid
            msgBoxShow(195, 1);
            return;
        }

        if(tpanel){
            if(Wtf.encode(tpanel.successor).length == 2 && Wtf.encode(tpanel.predecessor).length == 2){ // get an empty json by encoding = {}
                this.performMove('down', sel[0]);
            } else {
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('pm.common.plzconfirm'), WtfGlobal.getLocaleText('pm.msg.332'), function(btn){
                    if (btn == "yes") {
                        this.performMove('down', sel[0]);
                    }
                }, this);
            }
        } else
            this.performMove('down', sel[0]);
    }

});

function displayImportLog(){
   Wtf.MessageBox.hide();
    showImportLog();
}
