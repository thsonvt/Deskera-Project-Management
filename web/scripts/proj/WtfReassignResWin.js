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

Wtf.reassignWin = function(config){
    Wtf.apply(this, config);

    this.addEvents({
        "validateReassign": true,
        "deleteResource": true,
        "reassignCancelled": true
    });

    if(config.type == 'member'){
        this.winHeader = WtfGlobal.getLocaleText('pm.admin.project.members');
        this.winName = 'member';
        this.winType = 1;
    } else {
        this.winHeader = WtfGlobal.getLocaleText('pm.common.resource');
        this.winName = 'resource';
        this.winType = 2;
    }

    var btnArr = [];
    btnArr.push(new Wtf.Button({
        text: WtfGlobal.getLocaleText('lang.done.text'),
        cls: 'adminButton',
        scope: this,
        handler: function(btn){
            this.fireEvent("validateReassign", btn, this.tasksStore);
        }
    }));

    btnArr.push(new Wtf.Button({
        text: WtfGlobal.getLocaleText('lang.cancel.text'),
        cls: 'adminButton',
        scope: this,
        handler: function(btn){
            this.autoClose = false;
            this.fireEvent('reassignCancelled', this, btn);
            this.autoClose = true;
        }
    }));

    Wtf.reassignWin.superclass.constructor.call(this, {
        title: WtfGlobal.getLocaleText('lang.reassign.text')+" "+ this.winHeader,
        closable: true,
        modal: true,
        iconCls: 'iconwin',
        width: 600,
        height: 450,
        resizable: false,
        buttons: btnArr,
        buttonAlign: 'right',
        layout: 'border',
        id: config.id,
        autoDestroy: true
    });
}

Wtf.extend(Wtf.reassignWin, Wtf.Window, {

    initComponent: function(){
        Wtf.reassignWin.superclass.initComponent.call(this);
        var topHTML = getTopHtml(WtfGlobal.getLocaleText('lang.reassign.text')+" "+this.winHeader,
        WtfGlobal.getLocaleText({key:'pm.project.settings.reassign.subheader', params:this.winName}),
        "../../images/managemembers40_52.gif");
        var rec = new Wtf.data.Record.create([{
                name: 'taskid'
            },{
                name: 'taskindex'
            },{
                name: 'taskname'
            },{
                name: 'existingres'
            },{
                name: 'newres'
        }]);

        var reader = new Wtf.data.KwlJsonReader({
            root: "data"
        }, rec);

        this.tasksStore = new Wtf.data.Store({
            id : this.id + '_tasksStore',
            reader: reader,
            url: Wtf.req.prj + 'task.jsp',
            sortInfo: {
                field: 'taskindex',
                direction: "ASC"
            }
        });

        var resrec = Wtf.data.Record.create([{
                name: 'resourcename'
            },{
                name: 'resourceid'
            },{
                name: 'type'
            },{
                name: 'nickname'
        }]);

        this.resourcesStore = new Wtf.data.Store({
            url: Wtf.req.prj + 'resources.jsp?action=10&projid=' + this.projectid,
            reader: new Wtf.data.KwlJsonReader({
               root: "data"
            }, resrec),
            sortInfo: {field:'resourcename', direction:'ASC'}
        });
        var comboConfig = {
            store: this.resourcesStore,
            displayField: 'resourcename',
            valueField: 'resourceid' ,
            triggerAction: 'all',
            mode: 'local'
        };
        var multiCombo = new Wtf.common.Select(Wtf.applyIf({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText('lang.field.text'),
            forceSelection: true
        }, comboConfig));
        
        this.resourcesStore.load();

        var cm = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header: WtfGlobal.getLocaleText('pm.project.plan.cpa.taskno'),
                dataIndex: 'taskindex',
                sortable: false,
                fixed: true,
                width: 70
            },{
                header: WtfGlobal.getLocaleText('pm.common.task'),
                dataIndex: 'taskname',
                sortable: false,
                fixed: false
            },{
                header: WtfGlobal.getLocaleText('pm.project.plan.import.resourcemapping.existing'),
                dataIndex: 'existingres',
                sortable: false,
                fixed: false,
                renderer: this.comboBoxRenderer(multiCombo)
            },{
                header: WtfGlobal.getLocaleText('pm.project.resource.new'),
                dataIndex: 'newres',
                sortable: false,
                fixed: false,
                renderer: this.comboBoxRenderer(multiCombo),
                editor: multiCombo
            }
        ]);

        this.tasksGrid = new Wtf.grid.EditorGridPanel({
            id:this.id + '_tasksGrid',
            store: this.tasksStore,
            cm: cm,
            border: false,
            height: '100%',
            autoScroll: true,
            clicksToEdit: 1,
            loadMask: true,
            autoExpandColumn: 'taskname',
            viewConfig: {
                forceFit: true,
                emptyText: 'Selected '+this.winName+'(s) are not assigned on any tasks'
            }
        });

        this.add({
            region: 'north',
            height: 85,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: topHTML
        },{
            region: 'center',
            border: false,
            bodyStyle: 'background:#f1f1f1;font-size:10px;',
            layout: 'fit',
            items: this.tasksGrid
        });
        this.doLayout();

        this.tasksStore.on('loadexception', function(){
            msgBoxShow(4, 1);
        }, this);
        
        this.tasksStore.load({
            params: {
                seq: 12,
                projectid: this.projectid,
                resourceid: this.resourceid
            }
        });
        this.autoClose = false;
        this.tasksGrid.on('afteredit', this.updateResources, this);
        this.on('validateReassign', this.validateReassign, this);
        this.on('close', function(win){
            if(!this.autoClose)
                this.fireEvent('reassignCancelled', win);
        }, this);
    },

    comboBoxRenderer: function(combo) {
        return function(value) {
            if(value) {
                var resources = value.split(",");
                var nicknames = "";
                if(resources.length > 0) {
                    for(var cnt = 0; cnt < resources.length; cnt++) {
                        var idx = combo.store.find(combo.valueField, resources[cnt]);
                        if(idx == -1)
                            continue;
                        var rec = combo.store.getAt(idx).data;
                        if(cnt < (resources.length-1))
                            nicknames += rec.nickname +",";
                        else
                            nicknames += rec.nickname;
                    }
                    return nicknames;
                } else
                    return "";
            } else
                return "";
        };
    },

    updateResources: function(e){
        var jdata = "";
        if(e.record.data['newres'] != "") {
            var resArray = e.record.data['newres'].split(',');
            jdata = "[";
            var resArrayLength = resArray.length;
            for(var rescnt = 0; rescnt < resArrayLength; rescnt++) {
                var recIndex = this.resourcesStore.find('resourceid', resArray[rescnt]);
                var resRec = this.resourcesStore.data.items[recIndex];
                jdata += Wtf.encode(resRec.data) + ",";
            }
            jdata = jdata.substr(0, jdata.length - 1);
            jdata += "]";
        }
        this.storeResourcesInDB(e.record.data['taskid'], jdata, e.record.taskindex - 1);
    },

    storeResourcesInDB: function(taskid, jdata, row) {
         Wtf.Ajax.requestEx({
            url: Wtf.req.prj + 'getTaskResources.jsp',
            method: 'GET',
            params: {
                action: 'insert',
                taskid: taskid,
                row: row,
                data: jdata,
                projectid: this.projectid,
                userid: random_number
        }}, this,
        function(res, result){
        },
        function(res, result){
        });
    },

    validateReassign: function(btn, tasksStore){
        var cnt = tasksStore.getCount(), flag = false, i, type = this.winName;
        var assignedResIds = this.resourceid.split(",");
        for(i = 0; i < cnt; i++){
            var rec = tasksStore.getAt(i);
            for(var j = 0; j < assignedResIds.length; j++){
                if(rec.get('newres').indexOf(assignedResIds[j]) != -1){
                    flag = true;
                    break;
                }
            }
        }
        var str = flag ? WtfGlobal.getLocaleText({key:'pm.project.settings.reassign.assignedconfirm', params:[type, type]}):
            WtfGlobal.getLocaleText({key:'pm.project.settings.reassign.confirm', params:type});
         Wtf.Msg.show({
            title:WtfGlobal.getLocaleText('lang.confirm.text'),
            msg: str,
            cls: "msgboxClass",
            scope: this,
            buttons: Wtf.Msg.YESNO,
            icon: Wtf.MessageBox.QUESTION,
            fn: function(act){
                if(act == 'yes'){
                    this.fireEvent("deleteResource", this)
                    this.autoClose = true;
                    this.close();
                    this.autoClose = false;
                }
            }
         });
         return false;
    }
});
