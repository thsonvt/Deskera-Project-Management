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
Wtf.proj.taskModal = function(config){
    Wtf.apply(this, config);
    Wtf.proj.taskModal.superclass.constructor.call(this,{
        title: WtfGlobal.getLocaleText('pm.project.plan.menu.taskinfo'),
        modal: true,
        width: 615,
        height: 350,
        resizable: false,
        autoDestroy: true,
        layout:'fit',
        border: false ,
        iconCls: 'iconwin',
        buttons: [{
            text: WtfGlobal.getLocaleText('lang.OK.text'),
            id: 'ModalOkBttn',
            scope: this,
            handler: this.reflectModal
        },{
            text: WtfGlobal.getLocaleText('lang.cancel.text'),
            cls: 'bt1',
            id: 'ModalCancelBttn',
            scope: this,
            handler: function() {
                this.close();
            }
        }]
    });
};

Wtf.extend(Wtf.proj.taskModal, Wtf.Window, {
    initComponent: function(config){
        Wtf.proj.taskModal.superclass.initComponent.call(this, config);
        var resnames = this.record.data['resourcename'];
        this.tabs = new Wtf.TabPanel({
            activeTab:0,
            border: false,
            items :[{
                id:'GenTab',
                title:WtfGlobal.getLocaleText('long.general.text'),
                scope: this,
                border: false,
                items: [{
                    layout: 'fit',
                    region: 'north',
                    height: 50,
                    border: false,
                    items: [this.getTopPanel('GenTab')]
                },{
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items:[this.getGeneralTab()]
                }]
            },{
                title:WtfGlobal.getLocaleText('pm.project.plan.info.predecessors'),
                id:'PredTab',
                scope: this,
                border: false,
                items: [{
                    region: 'north',
                    height: 50,
                    layout: 'fit',
                    border: false,
                    items:[this.getTopPanel('PredTab')]
                },{
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.getGridpred()]
                }]
            },{
                id :'resr',
                title:WtfGlobal.getLocaleText('pm.common.resources'),
                scope: this,
                border: false,
                items: [{
                    region: 'north',
                    height: 50,
                    layout: 'fit',
                    border: false,
                    items: [this.getTopPanel('resr')]
                },{
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.getGrid()]
                }]
            },{
                id:'NoteTab',
                title: WtfGlobal.getLocaleText('pm.project.plan.task.attributes.notes'),
                scope: this,
                border: false,
                items: [{
                    region: 'north',
                    height: 50,
                    layout: 'fit',
                    border: false,
                    items: [this.getTopPanel('NoteTab')]
                },{
                    region: 'center',
                    layout:'fit',
                    border: false,
                    items: [this.getNotesTab()]
                }]
            },{
                id:'LinksTab',
                title: WtfGlobal.getLocaleText('pm.common.references'),
                scope: this,
                border: false,
                items: [{
                    region: 'north',
                    height: 50,
                    layout: 'fit',
                    border: false,
                    items: [this.getTopPanel('LinksTab')]
                },{
                    region: 'center',
                    layout:'fit',
                    border: false,
                    items: [this.getLinksTab()]
                }]
            },{
                id:'checkListTab',
                title: WtfGlobal.getLocaleText('pm.project.plan.info.checklisttab.text'),
                scope: this,
                border: false,
                items: [{
                    region: 'north',
                    height: 50,
                    layout: 'fit',
                    border: false,
                    items: [this.getTopPanel('checkListTab')]
                },{
                    region: 'center',
                    layout:'fit',
                    border: false,
                    items: [this.getCheckListTab()]
                }]
            }]
        });
        this.tabs.on('beforetabchange', this.backupNameDur, this);
        Wtf.getCmp('GenTab').on("activate", this.handleActivate, this);
        Wtf.getCmp('PredTab').on("activate", this.handleActivate, this);
        Wtf.getCmp('resr').on("activate", this.handleActivate, this);
        Wtf.getCmp('NoteTab').on("activate", this.handleActivate, this);
        Wtf.getCmp('LinksTab').on("activate", this.handleActivate, this);
        var comp = Wtf.getCmp('checkListTab');
        if(comp)
            comp.on("activate", this.handleActivate, this);
        this.add(this.tabs);
    },

    onRender: function(config){
        Wtf.proj.taskModal.superclass.onRender.call(this, config);
        var k1 = document.getElementById('resok') ;
        if(this.record.data.isparent || this.connstatus == 8) {
            Wtf.getCmp("start").disable();
            Wtf.getCmp("finish").disable();
        }
        if(this.connstatus == 8){
            this.tabs.remove(Wtf.getCmp('PredTab'));
            this.tabs.remove(Wtf.getCmp('resr'));
        }
        var tab = Wtf.getCmp('checkListTab');
        if(this.record.get('checklist') == "" || !WtfGlobal.getCheckListModuleStatus()){
            this.tabs.remove(tab);
        } else {
            Wtf.getCmp("percom").disable();
        }
    },

     getTopPanel: function(parentTab) {
        var durationTextBox = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText('pm.common.duration'),
            value:this.record.data['duration'],
            selectOnFocus: true,
            id: parentTab + 'duration',
            width: 147,
            disabled: (this.connstatus == 8 || this.record.data['isparent']) ? true : false
        });
        durationTextBox.on('change',function(obj, newVal, oldVal){
            var nVal = WtfGlobal.HTMLStripper(obj.getValue());
            var patt1=new RegExp("^((\\d+\\.?\\d*|\\d*\\.?\\d+)+(day|days|hr|hrs|h|d)*)$");
            if(patt1.test(nVal)){
                 var duval=this.plannerGrid.getvalidDuration(nVal);
                 var a = new RegExp(".0{2,3}$");
                 obj.setValue(duval.replace(a,""));
                 var duration = obj.getValue();
                 if(duration.indexOf('h')>=0) {
                     duration = parseFloat(duration)/this.plannerGrid.HrsPerDay;
                     if(parseFloat(duration)>parseInt(duration)) {
                         duration = parseInt(duration) + 1;
                     }
                 } else {
                    duration = parseFloat(duration);
                    if(duration > parseInt(duration)) {
                         duration = parseInt(duration) + 1;
                    }
                 }
                 var stdate = Wtf.getCmp('start').getValue();
                 if(stdate == '') {
                     stdate = new Date();
                     Wtf.getCmp('start').setValue(stdate);
                 }
                 var nonworkingdays = this.plannerGrid.sdateForNonWorkCal(stdate,-1);
                 if(nonworkingdays !=-1)
                    stdate = stdate.add(Date.DAY, nonworkingdays + 1);
                 else
                    nonworkingdays = 0;
                 var diff = this.plannerGrid.calculatenonworkingDays(stdate, Wtf.getCmp('start').getValue().add(Date.DAY,duration-1)) + nonworkingdays;
                 var enddate = Wtf.getCmp('start').getValue().add(Date.DAY, duration + diff - 1);
                 Wtf.getCmp('finish').setValue(enddate);
            } else {
               msgBoxShow(68, 1);
               obj.setValue(oldVal);
            }
        },this);
        var topPanel = new Wtf.Panel({
            frame: true,
            bodyStyle: 'padding:5px 5px 0',
            width: 600,
            height: 50,
            border: false,
            items: [{
                layout: 'column',
                items: [{
                    columnWidth: 0.60,
                    labelWidth: 60,
                    layout: 'form',
                    border: false,
                    items: [new Wtf.form.TextField({
                        fieldLabel: WtfGlobal.getLocaleText('lang.name.text'),
                        value: this.record.data['taskname'],
                        id: parentTab + 'name',
                        width: 250,
                        nameFld: true,
                        disabled: (this.connstatus == 8) ? true : false
                    })]
                },{
                    columnWidth: 0.40,
                    labelWidth: 60,
                    layout: 'form',
                    border: false,
                    items: [ durationTextBox ]
                },{
                   html:"<span style=\"float:left;padding:5px 0 0; \">"+WtfGlobal.getLocaleText('lang.days.text')+"</span>"
                 }]
            }]
        });
        return topPanel;
    },

    getGeneralTab: function() {
//        var temp;
//        var chb2 = null;
//        var dur;
        this.origStartDate = this.record.data['startdate'];
        this.origEndDate = this.record.data['enddate'];
        var storeprior = new Wtf.data.SimpleStore({
            fields: ['index', 'state'],
            data: [[Wtf.proj.pr.HIGH, 'High'], [Wtf.proj.pr.MODERATE, 'Moderate'], [Wtf.proj.pr.LOW, 'Low']]
        });
        var disablePC = true;
        if(!this.record.data.isparent){
            if(!(this.connstatus == 4 || this.connstatus == 5)){
                if(this.connstatus == 6 || (this.connstatus == 8 && this.mytask))
                    disablePC = false;
                else
                    disablePC = false;
            } else {
                disablePC = false;
            }
        }
        var combo1 = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText('lang.priority.text'),
            name: 'Priority',
            store: storeprior,
            displayField: 'state',
            valueField: 'index',
            typeAhead: true,
            mode: 'local',
            width: 120,
            forceSelection: true,
            editable: true,
            triggerAction: 'all',
            selectOnFocus: true,
            disabled: (this.connstatus == 4 || this.connstatus == 5 || this.connstatus == 3) ? false : true,
            value: this.record.data['priority'],
            id: 'priority'
        });
        var percom = new Wtf.form.NumberField({
            minValue: 0,
            maxvalue: 100,
            emptyText:0,
            blankText:0,
            allowDecimals: false,
            fieldLabel: WtfGlobal.getLocaleText('pm.project.plan.task.attributes.pc'),
            validateOnBlur: true,
            id: 'percom',
            width: 40,
            disabled: disablePC,
            value: this.record.data['percentcomplete'] == "" || this.record.data['percentcomplete'] === undefined ? 0 : this.record.data['percentcomplete'],
            tooltip: WtfGlobal.getLocaleText('pm.msg.69')
        });
        percom.on('blur',function(){
            if( this.getValue()=="")
                this.setValue(0);
            if(this.getValue()<0 || this.getValue()>100){
                msgBoxShow(69, 1);
                this.focus();
            }
        });
        var sDatePick = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText('lang.start.text'),
            readOnly:true,
            validateOnBlur: true,
            format: WtfGlobal.getOnlyDateFormat(),
            id: 'start',
            value: this.record.data['startdate'],
            width: 200,
            disabledDates: this.plannerGrid.cmodel.config[5].editor.field.disabledDates,
            disabledDays: this.plannerGrid.cmodel.config[5].editor.field.disabledDays,
            minValue: Date.parseDate(this.plannerGrid.projStartDate, 'Y-m-j H:i:s')
        });
        var eDatePick = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText('lang.end.text'),
            readOnly:true,
            validateOnBlur: true,
            format: WtfGlobal.getOnlyDateFormat(),
            id: 'finish',
            value: this.record.data['enddate'],
            width: 200,
            disabledDates: this.plannerGrid.cmodel.config[6].editor.field.disabledDates,
            disabledDays: this.plannerGrid.cmodel.config[6].editor.field.disabledDays,
            minValue: Date.parseDate(this.plannerGrid.projStartDate, 'Y-m-j H:i:s')
        });
        sDatePick.on('change',this.validateDate, this);
        eDatePick.on('change',this.changeDuration, this);
        var general = new Wtf.FormPanel({
            frame: true,
            bodyStyle: 'padding:15px 5px 0;',
            height: 210,
            border: false,
            items: [{
                labelWidth: 110,
                bodyStyle: 'margin-left:10px;',
                layout: 'column',
                items: [{
                    columnWidth: 0.50,
                    layout: 'form',
                    items: [percom]
                },{
                    columnWidth: 0.50,
                    labelWidth: 50,
                    layout: 'form',
                    items: [combo1]
                }]
            },{
                bodyStyle: 'padding-top:15px;',
                layout: 'form',
                items: [new Wtf.form.FieldSet({
                    height: 70,
                    title: WtfGlobal.getLocaleText('lang.date.text'),
                    layout: 'column',
                    items: [{
                        columnWidth: 0.50,
                        labelWidth: 40,
                        layout: 'form',
                        items: [ sDatePick ]
                    },{
                        columnWidth: 0.50,
                        labelWidth: 40,
                        layout: 'form',
                        items: [ eDatePick ]
                    }]
                })]
            },{
                html: "<br/><br/><br/><br/><br/>"
            }]
        });
        return general;
    },

    changeDuration: function(obj, nVal, oVal){
        if(!(obj.validate())){
            msgBoxShow(70, 0);
            obj.focus();
            return false;
        }
        var actDur = this.plannerGrid.CalculateActualDuration(Wtf.getCmp('start').getValue(), nVal);
        if(actDur < 0) {
            actDur = 1;
            obj.setValue(sDatePick.getValue());
        }
        Wtf.getCmp('GenTab' + 'duration').setValue(actDur);
    },

    validateDate: function(obj){
        if(!(obj.validate())){
            msgBoxShow(70, 0);
            this.focus();
            return false;
        }
    },

    getGridpred: function() {
        var totask =  this.record; //this.gridStore.data.items[this.krow];
        this.predRecord = Wtf.data.Record.create([{
            name: 'index'
        },{
            name: 'taskname',
            type: 'string'
        },{
            name: 'type',
            type: 'string'
        },{
            name: 'taskid',
            type: 'string'
        }]);
        this.predecessorStore = new Wtf.data.Store();
        Wtf.Ajax.requestEx({
            url: Wtf.req.prj + 'predecessor.jsp?',
            params:{
                taskid: totask.data['taskid']
            }
        },
        this,
        function (res, req) {
            if(res != "") {
                var lnkArray = res.split(',');
                var arrayLength = lnkArray.length;
                for(var i = 0; i < arrayLength; i++) {
                    var f = this.plannerGrid.search(lnkArray[i]);
                    var prec = new this.predRecord({
                        taskname: this.gridStore.data.items[f].data['taskname'],
                        index: f+1,
                        taskid: this.gridStore.data.items[f].data['taskid']
                    });
                    this.predecessorStore.insert(i,prec);
                }
            }
        });

        this.predStore = new Wtf.data.Store();
        this.predCombo = new Wtf.form.ComboBox({
            displayField: 'taskname',
            valueField: 'taskid',
            mode:'local',
            typeAhead:true,
            forceSelection:true,
            triggerAction: 'all',
            width: 575,
            emptyText:WtfGlobal.getLocaleText('pm.project.plan.predeccesor.select'),
            store: this.predStore
        });
        //change by kamlesh             resolve problem of using same store for combo and plan grid
        for(var i=0;i<this.gridStore.getCount();i++){
            var rec = new this.predRecord({
                taskname : this.gridStore.data.items[i].data['taskname'],
                index:i,
                taskid : this.gridStore.data.items[i].data['taskid']
            });
            this.predStore.add(rec);
        }
        var cm_pred = new Wtf.grid.ColumnModel([{
            width: 20,
            dataIndex: 'index',
            renderer: this.removeButton
        },{
            value: "",
            header: WtfGlobal.getLocaleText('lang.ID.text'),
            dataIndex: 'index',
            width: 80,
            align: 'left'
        },{
            hidden: true,
            dataIndex: 'taskid'
        },{
            header: WtfGlobal.getLocaleText('pm.common.taskname'),
            width: 310,
            align: 'left',
            dataIndex: 'taskname'
        }]);
        var grid_pred = this.getPred_ResGridPanel(this.predecessorStore, cm_pred, this.predCombo);
        grid_pred.on('cellclick',this.deletePredecessor, this);
        this.predCombo.on('select', this.addPredecessor, this);
        return grid_pred;
    },

    deletePredecessor: function(obj, rowI, colI, e){
        if(colI == 0 && e.target.tagName == 'IMG'){
            this.predecessorStore.remove(this.predecessorStore.data.items[rowI]);
        }
    },

    addPredecessor: function(combo, rec, index){
        var newid = combo.getValue();
        if(newid != ""){
            combo.setValue("");
            var newIndex = this.predecessorStore.find('taskid', newid);
            if(this.record.data['taskid'] == newid)
                msgBoxShow(255, 0);
            else if(newIndex != -1)
                msgBoxShow(75, 0);
            else {
                var lclId = this.plannerGrid.search(newid);
                var newRecord = this.gridStore.data.items[lclId];
                var newRec = new this.predRecord({
                    taskid: newRecord.data['taskid'],
                    taskname: newRecord.data['taskname'],
                    type: '',
                    index: lclId + 1
                });
                this.predecessorStore.insert(this.predecessorStore.getCount(),newRec);
            }
        } else {
            msgBoxShow(242, 0);
        }
    },

    getPred_ResGridPanel : function (store,cmodel,comboObj, type, handlerFun){
        var toolbar;
        if(type == 'link'){
            toolbar = [/*{
                    icon: '../../images/cancel16.png',
                    iconCls: 'iconclass',
                    id: 'resno',
                    tooltip: {
                        text:WtfGlobal.getLocaleText('pm.common.text.clear')
                    },
                    scope: this,
                    handler: function(){
                        comboObj.setValue("");
                    }
                },*/
				{
                    icon: '../../images/check16.png',
                    iconCls: 'iconclass',
                    id: 'resyes',
                    tooltip: {
                        text:WtfGlobal.getLocaleText('pm.project.task.addreference')
                    },
                    scope: this,
                    handler: handlerFun
                }, comboObj];
        } else {
            toolbar = [comboObj];
        }
        var tempPanel = new Wtf.grid.GridPanel({
            ds: store,
            cm: cmodel,
            width: 600,
            height: 210,
            autoScroll: true,
            frame: true,
            autoDestroy: true,
            viewConfig: {
                forceFit: true,
                autoFill: true
            },
            tbar: toolbar
        });
        return tempPanel;
    },

    getGrid: function() {
        var taskid = this.record.data["taskid"];
        this.ds_resource = new Wtf.data.Store({
            url: Wtf.req.prj + 'getTaskResources.jsp?taskid=' + taskid + '&projectid=' + this.pid,
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.plannerGrid.Record_Resource)
        });
        this.ds_resource.load();
        this.resCombo = new Wtf.form.ComboBox({
            displayField: 'resourcename',
            valueField: 'resourceid',
            mode:'local',
            editable: false,
            triggerAction: 'all',
            forceSelection:true,
            width: 575,
            emptyText:WtfGlobal.getLocaleText('pm.project.plan.info.resources.combotext'),
            typeAhead: true,
            store: this.allResources
        });
        var cm_res = new Wtf.grid.ColumnModel([{
            width: 20,
            renderer: this.removeButton
        },{
            hidden: true,
            dataIndex: 'resourceid'
        },{
            header: WtfGlobal.getLocaleText('pm.project.resource.name'),
            width: 310,
            align: 'left',
            dataIndex: 'resourcename'
        }]);
        var resourceGrid = this.getPred_ResGridPanel(this.ds_resource, cm_res, this.resCombo);
        resourceGrid.on('cellclick',this.deleteResource,this);
        this.resCombo.on('select', this.addResource, this);
        return resourceGrid;
    },

    deleteResource: function(obj, rowI, colI, e){
        if(colI == 0){
            this.ds_resource.remove(this.ds_resource.data.items[rowI]);
        }
    },

    addResource: function(combo, rec, index){
        var resId = combo.getValue();
        if(resId != ""){
            combo.setValue("");
            var newIndex = this.allResources.find('resourceid', resId);
            if(this.ds_resource.find('resourceid', resId) != -1){
                msgBoxShow(77, 0);
            } else {
                var newResource = this.allResources.data.items[newIndex];
                var newRec = new this.plannerGrid.Record_Resource({
                    resourceid: newResource.data['resourceid'],
                    resourcename: newResource.data['resourcename']
                });
                this.ds_resource.insert(this.ds_resource.getCount(),newRec);
            }
        } else {
            msgBoxShow(277, 0);
        }
    },

    deleteLink: function(obj, rowI, colI, e){
        if(colI == 0){
            this.linkStore.remove(this.linkStore.data.items[rowI]);
        }
    },

    addLink: function(){
        var newLink = this.linktf.getValue();
        var temp = newLink.toString();
        temp = temp.trim();
        if(newLink != "" && temp != "" && this.linktf.isValid()){
            if(temp.indexOf("://") == -1){
                temp = 'http://'+temp;
                newLink = temp;
            }
            this.linktf.setValue("");
            var pos = this.linkStore.getCount();
            var linkrec = new this.linkRecord({
                link: newLink
            });
            this.linkStore.insert(pos, linkrec);
        } else {
            msgBoxShow(262, 0);
        }
    },

    getLinksTab: function(){
        var task =  this.record; //this.gridStore.data.items[this.krow];
        this.linkRecord = Wtf.data.Record.create([
        {
            name: 'link',
            type: 'string',
            mapping: 'link'
        }]);
        this.linkStore = new Wtf.data.SimpleStore({
            fields: [{name: 'link'}]
        });
        var linkStr = task.get('links');
        var links = linkStr.split(',');
        var pos = 0;
        for(var i = 0; i<links.length; i++){
            if(links != ""){
                var linkrec = new this.linkRecord({
                    link: links[i]
                });
                this.linkStore.insert(i, linkrec);
            }
        }

        this.linktf = new Wtf.form.TextField({
            width: 535,
            emptyText: WtfGlobal.getLocaleText('pm.project.plan.info.references'),
            hideLabel: true,
            vtype: 'url'
        });
        var cm_link = new Wtf.grid.ColumnModel([{
            width: 20,
            renderer: this.removeButton
        },{
            header: WtfGlobal.getLocaleText('pm.common.links'),
            dataIndex: 'link',
            align: 'left',
            width: 380,
            renderer: function(data){
                return '<a href="'+data+'" style="color:#083772;" target= "_blank">'+data+'</a>';
            }
        }]);
        var grid_link = this.getPred_ResGridPanel(this.linkStore, cm_link, this.linktf, 'link', this.addLink);
        grid_link.on('cellclick',this.deleteLink, this);
        return grid_link;
    },

    getCheckListTab: function(){
        return new Wtf.tree.TaskCheckList({
            id: 'cltree',
            layout: 'fit',
            animate: true,
            enableDD: false,
            autoScroll: true,
            taskid: this.record.data['taskid'],
            checklistid: this.record.data['checklist'],
            containerScroll: true,
            border: false,
            rootVisible: false
        });
    },

    updateProgress: function(progress){
        var p = Wtf.getCmp('percom');
        if(p){
            p.setValue(progress);
        }
        Wtf.getCmp('todo_list' + this.pid).refreshToDos();
    },

    getNotesTab: function() {
        this.record.data['notes'] = this.record.data['notes'].replace(/!NL!/g,"\n");
        var p = new Wtf.Panel({
            width: 600,
            hideBorders: true,
            height: 210,
            layout: 'form',
            items: [ new Wtf.form.TextArea({
                id: 'bio',
                hideLabel: true,
                width: 600,
                height: 210,
                value: this.record.data['notes'],
                fieldLabel: WtfGlobal.getLocaleText('pm.project.plan.task.attributes.notes'),
                enableColors: false,
                enableLinks: false
            })]
        });
        return p;
    },

    holidayList: function(record) {
        var holidayPanel = new Wtf.Panel({
            layout: 'column',
            items: [{
                columnWidth: 0.5,
                title: WtfGlobal.getLocaleText('lang.sunday.text'),
                layout: "fit"
            },{
                columnWidth: 0.5,
                title: WtfGlobal.getLocaleText('lang.saturdays.text'),
                layout: "fit"
            }]
        });
        return holidayPanel;
    },
    handleActivate:function(obj) {
        obj.doLayout();
        obj.ownerCt.doLayout();
        if(obj.id == 'checkListTab'){
            obj.findById('cltree').loadCheckLists();
            obj.findById('cltree').on('nodeupdated', this.updateProgress, this)
        }
    },

    removeButton: function(){
        return "<img src = '../../images/Delete.gif' wtf:qtip='Delete this entry' style='cursor: pointer;'>";
    },

    reflectModal: function() {
        if(!(Wtf.getCmp('percom').isValid())) return false;
        var rec = this.record;
        var oldPercentVal = rec.data['percentcomplete'];
        var temp = this.reflect_gen();
        if(rec.data["parent"] != 0)
            this.plannerGrid.setParentCompletion(rec.data["parent"]);
        if(!temp)
            return false;
        var sc = this.plannerGrid.backupclass('Panel' + temp.data['taskid'], -1);
        var activeTabId = this.tabs.getActiveTab().id;
        temp.set('taskname', WtfGlobal.HTMLStripper(Wtf.getCmp(activeTabId + 'name').getValue()));
        if(Wtf.getCmp('bio')){
            var bio = Wtf.getCmp('bio').getValue();
            bio = WtfGlobal.HTMLStripper(bio);
            temp.set('notes', WtfGlobal.ScriptStripper(bio));
        }
        var pred = temp.data['predecessor'];
        var predCount = this.predecessorStore.getCount();
        if(predCount > 0) {
            var append_pred = '';
            var predData = this.predecessorStore.data;
            for (var i = 0; i < predCount; i++) {
                var tmp = predData.items[i].data['index'];
                if (tmp != "")
                    append_pred += tmp + ',';
            }
            var set_val = append_pred.substr(0, append_pred.length - 1);
            temp.set('predecessor', set_val);
            if(pred != set_val)
                this.plannerGrid.LinkChanges_AfterPredecessorEdit(pred,temp);
        } else if(pred != "") {
            temp.set('predecessor', "");
            this.plannerGrid.LinkChanges_AfterPredecessorEdit(pred,temp);
        }
        var res = "";
        var resData = this.ds_resource.data;
        var resCount = this.ds_resource.getCount();
        for(var i = 0; i < resCount; i++)
            res += resData.items[i].data['resourceid'] + ',';
        res = res.substr(0, res.length - 1);
        if(temp.data['resourcename'] != res) {
            temp.set('resourcename', res);
            var jdata = "";
            if(resCount > 0) {
                jdata = "[";
                for(var j = 0; j < resCount; j++)
                    jdata += Wtf.encode(resData.items[j].data) + ",";
                jdata = jdata.substr(0, jdata.length - 1);
                jdata += "]";
            }
            this.plannerGrid.storeResourcesInDB(temp.data['taskid'],jdata);
        }

        var link = temp.data['links'];
        var linkCount = this.linkStore.getCount();
        if(linkCount > 0) {
            var append_link = '';
            var linkData = this.linkStore.data;
            for (var i = 0; i < linkCount; i++) {
                var tmpl = linkData.items[i].data['link'];
                if (tmpl != "")
                    append_link += tmpl + ',';
            }
            var set_link = append_link.substr(0, append_link.length - 1);
            temp.set('links', set_link);
//            if(link != set_link)
//                this.plannerGrid.LinkChanges_AfterPredecessorEdit(pred,temp);
        } else if(link != "") {
            temp.set('links', "");
        }

        if(oldPercentVal != temp.data['percentcomplete'] && Wtf.getCmp('Panel' + temp.data.taskid)) {
            var ganttPanel = this.plannerGrid.containerPanel.cControl;
            ganttPanel.setTaskProgress(temp);
            if(ganttPanel.showOverDue == true)
                Wtf.getCmp('Panel' + temp.data.taskid).checkOverDue(true);
        }
        this.clientServerChange = 'client';
        if(temp.data.taskid != '0')
            this.plannerGrid.updateTask(temp);
        else
            this.plannerGrid.updateDB(temp);
        //this.containerPanel.cControl.mysubtask('Panel' + temp.data['taskid']);
        this.plannerGrid.assignclass('Panel' + temp.data['taskid'], sc);
        this.close();
    },

    reflect_gen: function() {
        var temp = this.record;
        var flag_com = 0;
        var flag_start = 0;
        var flag_end = 0;
        var temp3 = Wtf.getCmp('start');
        var temp4 = Wtf.getCmp('finish');
        var temp5 = Wtf.getCmp('percom').getValue();
        var Stdt = new Date();
        var Enddt = new Date();
        if(temp3.getValue()!="") {
            Stdt = new Date(temp3.getValue());
            temp.set('startdate', Stdt);
        }
        if(temp4.getValue()!="") {
            Enddt = new Date(temp4.getValue());
            temp.set('enddate', Enddt);
        }
        if(Stdt != this.origStartDate){
            if (temp3.validate())
                flag_start = 1;
        } else {
            flag_start = 1;
        }
        if(Enddt != this.origEndDate){
            if (temp4.validate())
                flag_end = 1;
        } else {
            flag_end = 1;
        }
        if (flag_end == 1) {
            if ((temp5 >= 0 && temp5 <= 100))
                flag_com = 1;
            else {
                msgBoxShow(73, 0);
                return false;
            }
        }
        var duration = 1;
        var sc = this.plannerGrid.backupclass('Panel' + temp.data['taskid'], -1);
        if (flag_start == 1 && flag_end == 1 && flag_com == 1) {
            if(temp.get('percentcomplete')!=temp5)
                temp.set('percentcomplete', temp5);
            if(temp.get('priority') != Wtf.getCmp('priority').getValue())
                temp.set('priority', Wtf.getCmp('priority').getValue());
            var activeTabId = this.tabs.getActiveTab().id;
            var duration = Wtf.getCmp(activeTabId + 'duration').getValue();
            if((duration+'').indexOf("h")>=0) {
                var val = duration + '';
                duration = parseInt(val.substr(0,val.indexOf('h')-1)) / 8;
            } else if((duration+'').indexOf("d")>=0) {
                var val = duration + '';
                duration = parseFloat(val.substr(0,val.indexOf('d')-1));
            } else
                duration = parseFloat(duration);
            if((Wtf.getCmp(activeTabId + 'duration').value+'')=="") {
                temp.set("duration",1);
                duration = 1;
            } else
                temp.set("duration",duration);
            temp.data['actualduration'] = duration;
            if(parseFloat(duration)>parseInt(duration))
                duration += 1;
            if(temp.data.taskid!='0'){
                if (!temp.get('startdate'))
                    temp.set('startdate', Stdt);
                else if (temp.get('startdate').format('D j-m-Y') != Stdt.format('D j-m-Y'))
                    temp.set('startdate', Stdt);
                if (!temp.get('enddate'))
                    temp.set('enddate', Enddt);
                else if(temp.get('enddate').format('D j-m-Y') != Enddt.format('D j-m-Y'))
                    temp.set('enddate', Enddt);
            } else {
                if(parseInt(temp.data.taskid)==0) {
                    if(Stdt.format("w") == 6)
                        Stdt = Stdt.add(Date.DAY,2);
                    else if(Stdt.format("w")==0)
                        Stdt = Stdt.add(Date.DAY,1);
                }
                if (temp.get('startdate') =='') {
                    if (temp.get('enddate')=='') {
                        temp.set('startdate', Stdt);
                        var diff = this.calculatenonworkingDays(Stdt, Stdt.add(Date.DAY, duration - 1));
                        var enddate = Stdt.add(Date.DAY, duration + diff - 1);
                        temp.set('enddate', enddate);
                    } else {
                        var diff = this.calculatenonworkingDays(Enddt.add(Date.DAY, -(duration - 1)),Enddt);
                        var stdate = Enddt.add(Date.DAY, -(duration + diff - 1));
                        temp.set('startdate',stdate);
                    }
                    temp.data['actstartdate'] = temp.data['startdate'];
                } else {
                    temp.set('startdate', Stdt);
                    temp.data['actstartdate'] = Stdt;
                    var diff = this.calculatenonworkingDays(Stdt, Stdt.add(Date.DAY, duration - 1));
                    var enddate = Stdt.add(Date.DAY, duration + diff - 1);
                    temp.set('enddate', enddate);
                }
            }
        }
        this.plannerGrid.assignclass('Panel' + temp.data['taskid'], sc);
        return temp;
    },

    backupNameDur: function(obj, newTab, currentTab){
        if(currentTab) {
            Wtf.getCmp(newTab.id + 'name').setValue(Wtf.getCmp(currentTab.id + 'name').getValue());
            Wtf.getCmp(newTab.id + 'duration').setValue(Wtf.getCmp(currentTab.id + 'duration').getValue());
        }
    }
});


Wtf.MapImportResource = function(config){
    Wtf.apply(this, config);
    this.iconCls = 'iconwin';
    Wtf.MapImportResource.superclass.constructor.call(this, config);
    this.addEvents = {
        "onsuccess" : true,
        "onfailure" : true
    };
};


Wtf.extend(Wtf.MapImportResource, Wtf.Window, {
    initComponent: function(){
        Wtf.MapImportResource.superclass.initComponent.call(this);
        this.title = WtfGlobal.getLocaleText('pm.project.plan.import.resourcemapping.importedres');
        this.layout = 'fit';
        this.width = 650;
        this.height = 450;
        this.modal = true;
    },

    onRender: function(config){
        this.csvFlag = this.mappingForType == 'csv' ? true : false;
        Wtf.MapImportResource.superclass.onRender.call(this, config);
        this.projresRec = Wtf.data.Record.create([{
            name: 'resourcename'
        },{
            name: 'resourceid'
        },{
            name: 'nickname'
        },{
            name: "category"
        },{
            name: "categoryid"
        },{
            name: "typename"
        },{
            name: "type"
        }]);
        this.projresds = new Wtf.data.Store({
            url: Wtf.req.prj + 'resources.jsp',
            baseParams: {
                action: 10,
                projid: this.projectid
            },
            reader : new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'count'
            },this.projresRec)
        });
        this.projresds.on("load",function(store){
            if(this.connstatus == 4) {
                this.projresds.insert(this.projresds.getCount(), this.newResRec('-1','Create New','Create New'));
            }
            if(this.csvFlag){
                var coll = store.query('type', 3);
                var i = 0;
                if(coll){
                    for(;i < coll.length; i++){
                        store.remove(coll[i]);
                    }
                }
            }
        },this);
        this.projresds.load();
        this.projresCombo = new Wtf.form.ComboBox({
            store: this.projresds,
            displayField: (this.csvFlag) ? 'nickname' : 'resourcename',
            valueField: 'resourceid',
            typeAhead: true,
            mode: 'local',
            forceSelection: true,
            emptyText: "",
            lastQuery: '',
            //editable: false,
            triggerAction: 'all',
            selectOnFocus: true
        });
        this.projresCombo.on("select",function(combo,record,index){
            if(record.data.resourceid == '-1')
                this.CreateNewRes();
        },this);
        if(!this.csvFlag){
            var rec = Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'},
                {name: "rate"},
                {name: "category"},
                {name: "categoryid"},
                {name: "typename"},
                {name: "type"},
                {name: "email"},
                {name: "resourceid"}
            ]);

            var reader = new Wtf.data.KwlJsonReader({
                root: "data",
                id: 'task-reader'
            }, rec);

            this.listds = new Wtf.data.GroupingStore({
                id : "listds"+this.id,
                sortInfo: {
                    field: 'typename',
                    direction: "ASC"
                },
                reader: reader
            });

            var grpView = new Wtf.grid.GroupingView({
                forceFit: true,
                showGroupName: false,
                enableGroupingMenu: false,
                hideGroupedColumn: true,
                emptyText: WtfGlobal.getLocaleText('pm.common.nodatapresent')
            });

            this.listds.on('load', function(store){
                store.groupBy("typename");
            }, this);
            this.listds.loadData(this.newresource);
        } else {
            this.listds = new Wtf.data.JsonStore({
                id : "listds"+this.id,
                root: 'data',
                data : this.newresource,
                fields: ['id', 'name',"rate","overtime","email","resourceid"]
            });
        }
        this.listcm = new Wtf.grid.ColumnModel([{
            header: WtfGlobal.getLocaleText('pm.project.resource.omport'),
            dataIndex: 'name'
        },{
            header: WtfGlobal.getLocaleText('pm.project.user.deskaramap'),
            dataIndex: 'resourceid',
            editor: this.projresCombo,
            renderer : this.comboBoxRenderer(this.projresCombo)
        },{
            header: WtfGlobal.getLocaleText('lang.type.text'),
            dataIndex: 'typename',
            hidden: true
        },{
            header: WtfGlobal.getLocaleText('typeid.text'),
            dataIndex: 'type',
            hidden: true
        }]);
        this.createGP();
        if(!this.csvFlag){
            this.grid.view = grpView;
            this.grid.on('cellclick', this.filterComboByType, this);
        }
        else
            this.grid.getView().forceFit = true;

        this.innerpanel = this.add(new Wtf.Panel({
            layout: 'border',
            items: [{
                region : 'north',
                height : 75,
                border : false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                html : getTopHtml(WtfGlobal.getLocaleText('pm.project.plan.import.resourcemapping.header'), WtfGlobal.getLocaleText('pm.project.plan.import.resourcemapping.subheader'),"../../images/createuser40_52.gif")
            },{
                region : 'center',
                border : false,
                bodyStyle : 'background:#f1f1f1;font-size:10px;',
                layout : 'fit',
                items :[this.grid]
            }],
            buttons: [{
                text : WtfGlobal.getLocaleText('lang.import.text'),
                id : 'importplanbtn'+this.id,
                scope: this,
                handler: function(){
                    Wtf.getCmp('skipresbtn'+this.id).disable();
                    Wtf.getCmp('importplanbtn'+this.id).disable();
                    this.allResources.load();
                    this.mapImportedRes();
                }
            },{
                text : WtfGlobal.getLocaleText('lang.cancel.text'),
                scope : this,
                id : 'skipresbtn'+this.id,
                disabled: this.csvFlag,
                handler : function(){
                    this.close();
                }
            }]
        }));
        this.grid.on("validateedit",function(e){
            if(e.field == 'resourceid' && e.value =='-1' ) {
                e.cancel = true;
                return;
            }
        },this);
    },

    createGP: function(){
        return  (this.grid = new Wtf.grid.EditorGridPanel({
            id:'list' + this.id,
            clicksToEdit : 1,
            store: this.listds,
            cm: this.listcm,
            sm : new Wtf.grid.RowSelectionModel(),
            border : false,
            width: 434,
            loadMask : true
        }));
    },

    filterComboByType: function(grid, ri, ci, e){
        var store = this.projresds;
        if(ci == 1){
            if(store.isFiltered())
                store.clearFilter(false);
            var val = grid.getStore().getAt(ri).get('type');
            store.filter('type', val, true, false);
            if(this.connstatus == 4 && val != Wtf.proj.resources.type.WORK)
                store.insert(this.projresds.getCount(), this.newResRec('-1','Create New','Create New'));
        }
    },

    mapImportedRes : function(flag) {
        var jsonData = "[";
        for(var cnt =0 ;cnt<this.listds.getCount();cnt++) {
            var rec = this.listds.getAt(cnt);
            jsonData +=  "{tempid:\""+rec.data.name+"\",resourceid:\""+rec.data.resourceid+"\"},";
        }
        jsonData = jsonData.substr(0, jsonData.length - 1) + "]";
        var reqObj;
        if(this.csvFlag){
            reqObj = {
                url: '../../importProjectPlanCSV.jsp',
                params: ({
                    action: 3,
                    userchoice: this.userchoice,
                    projectid: this.projectid,
                    isbaseline: this.isbaseline,
                    val: jsonData,
                    filename: this.docid
                }),
                method: 'POST'
            }
        } else {
            reqObj = {
                url: '../../fileImporter.jsp',
                params: ({
                    action: 2,
                    docid : this.docid,
                    userchoice : this.userchoice,
                    type : this.type,
                    projectid :this.projectid,
                    isres : this.isres,
                    isweek : this.isweek,
                    isholiday : this.isholiday,
                    isbaseline : this.isbaseline,
                    val: jsonData
                }),
                method: 'POST'
            }
        }
        Wtf.Ajax.requestEx(reqObj, this,
        function(result, req){
            if(this.csvFlag)
                var obj = eval('('+result+')');
            this.fireEvent("onsuccess", obj);
            this.close();
        },function(result){
            if(this.csvFlag){
                var obj = eval('('+result+')');
                this.fireEvent("onfailure", obj);
            }
            this.close();
        });
    },

    comboBoxRenderer : function(combo) {
        return function(value) {
            var idx = combo.store.find(combo.valueField, value);
            if(idx == -1)
                return "";
            var rec = combo.store.getAt(idx);
            return rec.get(combo.displayField);
        };
    },

    newResRec : function(resid,resname, resnickname, typeid, typename, category) {
        return (new this.projresRec({
            resourceid: resid,
            resourcename: resname,
            nickname: resnickname,
            type: typeid,
            typename: typename,
            category: category
        }));
    },
    CreateNewRes : function() {
        var rec = this.grid.getSelectionModel().getSelected();
        //var creatememflag = false;
        var newresrecord = Wtf.data.Record.create([{
            name: "typeid",
            type: 'int',
            mapping: 'typeID'
        },{
            name: 'typename',
            type: 'string',
            mapping: 'typeName'
        }]);
        this.typeStore = new Wtf.data.Store({
            url: "../../jspfiles/project/resources.jsp?action=17",
            sortInfo: {field: 'typename', direction:'ASC'},
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },newresrecord)
        });
        this.typeStore.on('load', function(store, recs, opts){
            var idx = store.find('typename', 'Work', 0, false, true);
            if(idx && idx != -1)
                store.remove(store.getAt(idx));
            var rec = this.grid.getSelectionModel().getSelected();
            if(rec){
                typeCombo.setValue(rec.data['type']);
            }
        }, this);
        this.typeStore.load();
        var typeCombo = new Wtf.form.ComboBox({
            store: this.typeStore,
            mode: 'local',
            typeAhead: true,
            forceSelection:true,
            triggerAction: 'all',
            fieldLabel: "Resource Type*",
            displayField: "typename",
            name: "typecombo",
            allowBlank: false,
            valueField: "typeid"
        });
        var newcatrecord = Wtf.data.Record.create([{
            name: "categoryid",
            type: 'int',
            mapping: 'categoryID'
        },{
            name: 'categoryname',
            type: 'string',
            mapping: 'categoryName'
        }]);
        this.categoryStore = new Wtf.data.Store({
            url: "../../jspfiles/project/resources.jsp?action=3",
            sortInfo: {field: 'categoryname', direction:'ASC'},
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },newcatrecord)
        });
        this.categoryStore.on('load', function(store, recs, opts){
            var idx = store.find('categoryname', 'Member', 0, false, true);
            if(idx && idx != -1)
                store.remove(store.getAt(idx));
            var rec = this.grid.getSelectionModel().getSelected();
            if(rec){
                typeCombo.setValue(rec.data['type']);
            }
        }, this);
        var categoryCombo = new Wtf.form.ComboBox({
            store: this.categoryStore,
            mode: 'local',
            editable: true,
            forceSelection:true,
            typeAhead: true,
            triggerAction: 'all',
            fieldLabel: "Resource Category*",
            displayField: "categoryname",
            name: "categorycombo",
            allowBlank: false,
            selectOnFocus: true,
            valueField: "categoryid"
        });
        this.categoryStore.load();
        typeCombo.on('select', function(combo, rec, index){
            if(newResForm){
                var costField = this.getFieldConfig('stdrate');
                var wuField = this.getFieldConfig('wuvalue');
                switch(rec.data['typeid']){
                    case Wtf.proj.resources.type.MATERIAL:
                        costField.setFieldLabel('Cost/Unit*');
                        wuField.setFieldLabel('No. of Units*');
                        break;
                    case Wtf.proj.resources.type.COST:
                        costField.setFieldLabel('Cost*');
                        wuField.setFieldLabel('Weightage(%)*');
                        wuField.maxLength = 100;
                        break;
                }
            }
        }, this);
        var newRes = new Wtf.Window({
            title: WtfGlobal.getLocaleText('pm.project.resource.create'),
            layout: "border",
            resizable: false,
            iconCls : 'iconwin',
            modal: true,
            autoScroll : true,
            height: 380,
            width: 480,
            items: [{
                region: "north",
                height : 75,
                border : false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                html: getTopHtml(WtfGlobal.getLocaleText('pm.project.resource.create'),WtfGlobal.getLocaleText('pm.project.resource.create')+"<br><span style=\"float:right; \">"+WtfGlobal.getLocaleText('pm.common.requiredfields')+"</span>","../../images/createuser40_52.gif")
            },{
                region: "center",
                border: false,
                bodyStyle : 'background:#f1f1f1;font-size:10px;',
                layout: "fit",
                items:[ newResForm = new Wtf.form.FormPanel({
                    url: "../../jspfiles/project/resources.jsp",
                    labelWidth: 180,
                    defaults: {
                        width: 225
                    },
                    defaultType: "textfield",
                    bodyStyle: "padding:20px;",
                    items:[{
                        fieldLabel: "Resource Name*",
                        id: 'newResourceNameCreate',
                        value: rec.data["name"],
                        name: "resourcename",
                        maxLength: 255,
                        allowBlank: false
                    },
                    typeCombo,
                    categoryCombo,
                    {
                        fieldLabel: "Cost*",
                        xtype:"numberfield",
                        allowBlank: false,
                        minValue:0,
                        value: (rec.data["rate"]) ? rec.data["rate"] : 0 ,
                        name: "stdrate",
                        id: "stdrate"
                    },{
                        fieldLabel: (rec) ? ((rec.data[WtfGlobal.getLocaleText('lang.Type.tezxt')] == Wtf.proj.resources.type.MATERIAL) ? 'No. of Units' : 'Weightage(%)') : "Weightage(%) or Units",
                        xtype:"numberfield",
                        allowBlank: false,
                        minValue:0,
                        maxValue: (rec && rec.data['type'] == Wtf.proj.resources.type.WORK) ? 100 : Number.MAX_VALUE,
                        value: rec ? rec.data["wuvalue"] : 0,
                        selectOnFocus: true,
                        nanText: "Please enter the valid number between (0 - 100)",
                        name: "wuvalue",
                        id: "wuvalue"
                    },{
                        xtype : 'hidden',
                        name: "resourceid",
                        value: rec.data["id"]
                    },{
                        xtype : 'hidden',
                        name: "username",
                        id : this.id + "username"
                    },{
                        xtype : 'hidden',
                        name: "projectid",
                        value: this.projectid
                    },{
                        xtype : 'hidden',
                        id: "colorCodeHidden",
                        name: "colorcode",
                        value: "#666"
                    },{
                        xtype : 'hidden',
                        id: "typeIdHidden",
                        name: "typeid",
                        value: "1"
                    },{
                        xtype : 'hidden',
                        id: "categoryIdHidden",
                        name: "categoryid",
                        value: "1"
                    }]
                })]
            }],
            buttons:[{
                text:WtfGlobal.getLocaleText('lang.create.text'),
                scope: this,
                handler: function(){
                    if(!newResForm.form.isValid())
                        return ;
                    Wtf.getCmp("categoryIdHidden").setValue(categoryCombo.getValue());
                    Wtf.getCmp("typeIdHidden").setValue(typeCombo.getValue());
                    newResForm.form.submit({
                        scope: this,
                        params: {
                            chkflag: true,
                            action: 11
                        },
                        success: function(action, res){
                            var resname = Wtf.getCmp("newResourceNameCreate").getValue();
                            resname += "[" + categoryCombo.el.dom.value + "]";
                            newRes.close();
                            var resobj = eval( "(" + res.response.responseText.trim() + ")" );
                            if(resobj.success) {
                                var val = typeCombo.getValue();
                                this.projresds.clearFilter();
                                this.projresds.insert(this.projresds.getCount()-1, this.newResRec(resobj.resid,resname, resname, val,
                                    this.getDisplayValue(typeCombo, this.typeStore, val), this.getDisplayValue(categoryCombo, this.categoryStore, categoryCombo.getValue())));
                                this.grid.getSelectionModel().getSelected().set("resourceid",resobj.resid);
                                msgBoxShow(33,0);
                            } else {
                                msgBoxShow(37, 1);
                            }
                        },
                        failure: function(action, res){
                            if(res.response.responseText){
                                var resobj = eval( "(" + res.response.responseText.trim() + ")" );
                                if(resobj.errcode == 1) {
                                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('pm.msg.FAILURE'), resobj.error + "<br>Do you want to activate this resource?", function(obj){
                                        if(obj == "yes"){
                                            Wtf.Ajax.requestEx({
                                                url: "../../jspfiles/project/resources.jsp",
                                                params: {
                                                    action: 8,
                                                    projid: this.projectid,
                                                    resid: resobj.resourceid
                                                },
                                                method : 'POST'
                                            },
                                            this,
                                            function(){
                                                this.projresds.load();
                                            },
                                            function(){
                                                msgBoxShow(20, 1);
                                            });
                                        }
                                    }, this);
                                } else if(resobj.errcode == 2){
                                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('pm.msg.FAILURE'), resobj.error + '<br>Do you want to create another resource with the same name?', function(obj){
                                        if(obj == "yes"){
                                            Wtf.Ajax.request({
                                                url: "../../jspfiles/project/resources.jsp",
                                                params: {
                                                    chkflag: false,
                                                    action: 4,
                                                    colorcode: resobj.colorcode,
                                                    overtimerate: resobj.overtimerate,
                                                    projectid: this.projectid,
                                                    resourcename: resobj.resourcename,
                                                    typeid: resobj.typeid,
                                                    categoryid: resobj.categoryid,
                                                    stdrate: resobj.stdrate
                                                },
                                                method : 'POST',
                                                scope : this,
                                                success:function(){
                                                    msgBoxShow(33, 0);
                                                    this.projresds.load();
                                                },
                                                failure :function(){
                                                    msgBoxShow(20, 1);
                                                }
                                            });
                                        }
                                    }, this);
                                } else
                                    msgBoxShow(4, 1);
                                newRes.close();
                            }
                        }
                    });
                }
            },{
                text: WtfGlobal.getLocaleText('lang.cancel.text'),
                scope: this,
                handler: function(){
                    newRes.close();
                }
            }]
        });
        Wtf.getCmp("newResourceNameCreate").on("change", function(){
            Wtf.getCmp("newResourceNameCreate").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("newResourceNameCreate").getValue()));
        }, this);
        if(!this.csvFlag)
            typeCombo.setDisabled(true);
        newRes.show();
    },

    getFieldConfig: function(name){
        return newResForm.findById(name);
    },

    getDisplayValue: function(combo, store, val){
        var idx = store.find(combo.valueField, val, 0);
        if(idx && idx != -1){
            val = store.getAt(idx).get(combo.displayField);
            return val;
        }
        return null;
    },

    getValueFromDisplayField: function(combo, store, val){
        var idx = store.find(combo.displayField, val, 0);
        if(idx && idx != -1){
            val = store.getAt(idx).get(combo.valueField);
            return val;
        }
        return null;
    }
});
