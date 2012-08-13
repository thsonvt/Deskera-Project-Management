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
Wtf.tree.checkList = function(config){
    Wtf.apply(this, config);
    this.nodeHash = {};
    this.tbar = [{
        xtype: 'tbbutton',
        id: 'add-cl-btn',
        text: WtfGlobal.getLocaleText('pm.admin.company.checklist.master.addchecklist'),
        iconCls: 'dpwnd addCL',
        scope: this,
        handler: this.onAddClick
    }, {
        xtype: 'tbbutton',
        id: 'add-clitem-btn',
        text: WtfGlobal.getLocaleText('pm.admin.company.checklist.master.addchecklistitem'),
        iconCls: 'dpwnd addCLItem',
        disabled: true,
        scope: this,
        handler: this.onAddClick
    }, {
        xtype: 'tbbutton',
        id: 'delete-cl-btn',
        text: WtfGlobal.getLocaleText('pm.admin.company.checklist.master.remove'),
        iconCls: 'dpwnd removeCL',
        disabled: true,
        scope: this,
        handler: this.beforeRemove
    }];
    var node = new Wtf.tree.TreeNode({
        draggable: false
    });
    this.setRootNode(node);
    Wtf.tree.checkList.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.tree.checkList, Wtf.tree.TreePanel, {

    completeImg: '../../images/tick.png',
    incompleteImg: '../../images/tickcdis.png',

    initComponent: function(){
        Wtf.tree.checkList.superclass.initComponent.call(this);

        var rec = Wtf.data.Record.create([{
                name: 'checklistid',
                mapping: 'checkListID'
            },{
                name: 'checklistname',
                mapping: 'checkListName'
            },{
                name: 'checklistdesc',
                mapping: 'checkListDesc'
            }, {
                name: 'tasks'
        }]);
        var read = new Wtf.data.KwlJsonReader({
            root: 'data',
            totalProperty: 'count'
        }, rec)
        this.clstore = new Wtf.data.Store({
            reader: read,
            url: '../../checklist.jsp',
            method: 'GET',
            baseParams: {
                cm: 'getCheckLists'
            },
            listeners: {
                 scope: this,
                'load': this.refreshTree
            }
        });
        this.loadCheckLists(true);
        this.getSelectionModel().on('selectionchange',this.selectionChange,this);
    },

    refreshTree: function(store, recs, params){

        var childs = this.getRootNode().childNodes;
        if (childs != null) {
            var nodelen = childs.length;
            if (nodelen > 0) {
                for (var i = 0; i < nodelen; i++) {
                    childs[0].remove();
                }
            }
        }

        for (i = 0; i < recs.length; i++) {
            var rec = recs[i];
            var n = new Wtf.tree.TreeNode({
                id: rec.get('checklistid'),
                text: '<span style="margin-left:10px;">' + rec.get('checklistname') + '</span>',
                draggable: false,
                iconCls: 'dpwnd checkListNode',
                leaf: false
            });
            this.getRootNode().appendChild(n);
            var t = rec.get('tasks');
            for (var j = 0; j < t.length; j++) {
                var ln = new Wtf.tree.TreeNode({
                    id: t[j].cTaskID,
                    text: '<span style="margin-left:10px;">' + t[j].cTaskName + '</span>',
                    draggable: false,
                    iconCls: 'dpwnd checkListItemNode',
                    leaf: true
                });
                n.appendChild(ln);
            }
            n.expand();
        }
        Wtf.getCmp('delete-cl-btn').disable();
        Wtf.getCmp('add-clitem-btn').disable();
    },

    selectionChange: function(model, node){
        if(node && node.isLeaf()){
            Wtf.getCmp('delete-cl-btn').disable();
            Wtf.getCmp('add-clitem-btn').disable();
        } else {
            Wtf.getCmp('delete-cl-btn').enable();
            Wtf.getCmp('add-clitem-btn').enable();
        }
    },

    onAddClick: function(btn){
        var f = true;
        if(btn.id == 'add-cl-btn'){
            f = false;
        }
        this.getWindow(f);
    },

    addCheckList: function(win){
        var form = win.findByType('form')[0];
        var val = form.findByType('textfield')[0].getValue();
        Wtf.Ajax.requestEx({
            url: '../../checklist.jsp',
            params: {
                cm: 'addCheckListCommon',
                checklist: true,
                checklistname: val
            }
            }, this,
            function(result, action){
                this.loadCheckLists();
            },
            function(result, action){
                msgBoxShow(4, 1);
            }
        );

    },

    addCheckListItem: function(win){
        var form = win.findByType('form')[0];
        var val = form.findByType('combo')[0].getValue();
        var tname = form.findByType('textfield')[0].getValue();
        Wtf.Ajax.requestEx({
            url: '../../checklist.jsp',
            params: {
                cm: 'addCheckListCommon',
                checklist: false,
                checklistid: val,
                taskname: tname
            }
            }, this,
            function(result, action){
                this.loadCheckLists();
            },
            function(result, action){
                msgBoxShow(4, 1);
            }
        );
    },

    beforeRemove: function(btn, e){
        var selNode = this.getSelectionModel().getSelectedNode();
        Wtf.Ajax.requestEx({
            url: '../../checklist.jsp',
            method: 'POST',
            params: {
                cm: 'isAssociatedWithAnyTask',
                checklistid: selNode.attributes.id
            }
        }, this, function(resp, req){
            var obj = Wtf.decode(resp);
            if(obj.data){
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText('lang.confirm.text'),
                    msg: WtfGlobal.getLocaleText('pm.admin.company.checklist.master.remove.check'),
                    buttons: Wtf.Msg.YESNOCANCEL,
                    icon: Wtf.MessageBox.QUESTION,
                    scope: this,
                    fn: function(id){
                        if(id == 'yes'){
                            this.removeCheckLists();
                        } else return;
                    }
                });
                return;
            } else {
                this.removeCheckLists();
            }
        }, function(resp, req){
            msgBoxShow(4, 1);
        });
    },

    removeCheckLists: function(){
        var selNode = this.getSelectionModel().getSelectedNode();
        Wtf.Ajax.requestEx({
            url: '../../checklist.jsp',
            method: 'POST',
            params: {
                cm: 'removeCheckList',
                checklistid: selNode.attributes.id
            }
        }, this, function(resp, req){
            this.loadCheckLists();

        }, function(resp, req){
            msgBoxShow(4, 1);
        });
    },

    loadCheckLists: function(FirstLoad){
        if(!FirstLoad)
            this.changeTracker = true;
        if(this.clstore.getCount() > 0)
            this.getSelectionModel().clearSelections();
        this.clstore.load();
    },

    getWindow: function(taskFlag){
        var win = Wtf.getCmp('addchecklistcommon');
        if(!win){
            var sel = this.getSelectionModel().getSelectedNode();
            var t1 = {
                xtype: 'textfield',
                name: taskFlag ? 'taskname' : 'checklistname',
                id: 'name',
                fieldLabel: taskFlag ? WtfGlobal.getLocaleText('pm.admin.company.checklist.master.checklistitemname') + '*' : WtfGlobal.getLocaleText('pm.admin.company.checklist.master.checklistname') + '*',
                anchor: '99%',
                allowBlank: false,
                maxLength: 256
            };

            var combo = {
                xtype: 'combo',
                name: 'checklistid',
                fieldLabel: WtfGlobal.getLocaleText('pm.admin.company.checklist.master.select') + '*',
                store: this.clstore,
                anchor: '99%',
                displayField: 'checklistname',
                valueField: 'checklistid',
                value: sel ? sel.attributes.id : '',
                typeAhead: true,
                mode: 'local',
                emptyText: WtfGlobal.getLocaleText('pm.common.group.select'),
                triggerAction: 'all',
                forceSelection: true,
                selectOnFocus: true,
                allowBlank: true
            };

            var f = {
                xtype: 'form',
                labelWidth: 120,
                labelAlign: 'left',
                border: false,
                bodyStyle: 'padding:5px 5px 0',
                anchor: '100%',
                defaultType: 'textfield',
                buttonAlign: 'right',
                items: [t1]
            };

            win = new Wtf.Window({
                title: taskFlag ? WtfGlobal.getLocaleText('pm.admin.company.checklist.master.addchecklistitem') : WtfGlobal.getLocaleText('pm.admin.company.checklist.master.addchecklist'),
                id: 'addchecklistcommon',
                closable: true,
                modal: true,
                resizable: false,
                height: taskFlag ? 160 : 100,
                width: 320,
                layout: 'fit',
                buttonAlign: 'right',
                iconCls: 'iconwin',
                items: [f],
                buttons: [{
                    text: WtfGlobal.getLocaleText('lang.add.text'),
                    scope: this,
                    clItemFlag: taskFlag,
                    handler: function(btn, e){
                        var w = Wtf.getCmp('addchecklistcommon');
                        var v = Wtf.getCmp("name");
                        v.setValue(WtfGlobal.HTMLStripper(v.getValue()));
                        if(v.isValid()){
                            if(btn.clItemFlag)
                                this.addCheckListItem(w);
                            else
                                this.addCheckList(w);
                            w.findByType('textfield')[0].reset();
                        }
                    }
                },{
                    text: WtfGlobal.getLocaleText('lang.close.text'),
                    scope: this,
                    handler: function(btn){
                        Wtf.getCmp('addchecklistcommon').close();
                    }
                }]
            });

            if(taskFlag)
                win.findByType('form')[0].add(combo);
            win.doLayout();
        }
        win.show();
    }
});

Wtf.tree.TaskCheckList = function(config){
    Wtf.apply(this, config);
    this.nodeHash = {};
    var node = new Wtf.tree.TreeNode({
        id: this.checklistid,
        draggable: false
    });
    this.setRootNode(node);
    Wtf.tree.TaskCheckList.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.tree.TaskCheckList, Wtf.tree.TreePanel, {

    completeImg: '../../images/tick.png',
    incompleteImg: '../../images/tickcdis.png',
    moderatePriorityImg: '../../images/normal1.png',
    highPriorityImg: '../../images/todo_high.png',
    lowPriorityImg: '../../images/todo_low.png',
    highPriorityTitle: "High Priority Task",
    lowPriorityTitle: "Low Priority Task",
    moderatePriorityTitle: "Moderate Priority Task",

    initComponent: function(){
        Wtf.tree.checkList.superclass.initComponent.call(this);

        this.addEvents({
            'nodeupdated': true
        })

        var rec = Wtf.data.Record.create([{
                name: 'ctaskid'
            },{
                name: 'ctaskname'
            },{
                name: 'parentid'
            },{
                name: 'status'
            },{
                name: 'priority'
            },{
                name: 'leaf'
        }]);
        var read = new Wtf.data.KwlJsonReader({
            root: 'data'
        }, rec)
        this.clstore = new Wtf.data.Store({
            reader: read,
            url: '../../checklist.jsp',
            method: 'GET',
            baseParams: {
                cm: 'getCheckListToDos'
            },
            listeners: {
                 scope: this,
                'load': this.refreshTree
            }
        });
//        this.getSelectionModel().on('selectionchange',this.selectionChange,this);
    },

    refreshTree: function(store, recs, params){

        var childs = this.getRootNode().childNodes;
        if (childs != null) {
            var nodelen = childs.length;
            if (nodelen > 0) {
                for (var i = 0; i < nodelen; i++) {
                    childs[0].remove(true);
                }
            }
        }
        for (i = 0; i < recs.length; i++) {
            var rec = recs[i];
            var ID = rec.get('ctaskid');
            var st = rec.get('status');
            var prio = this.getPriorityImage(rec.get('priority'));
            var src = st == 0 ? this.incompleteImg : this.completeImg;
            var title = st ? WtfGlobal.getLocaleText('pm.project.todo.markasincomplete') : WtfGlobal.getLocaleText('pm.project.todo.markascomplete');
            var text = prio + "<img id='markclt"+ID+"' class='cancel' src='"+src+"'" +
                "onclick=\"markComplete('"+ID+"','"+this.id+"');\" style=\"margin-left:5px;vertical-align:middle;margin-right:10px;\" wtf:qtip='"+title+"'></img>" +
                "<span id='spanclt"+ID+"' style=\"color:black;\">"+ rec.get('ctaskname') + "</span>";
            var ln = new Wtf.tree.TreeNode({
                id: ID,
                text: text,
                tname: rec.get('ctaskname'),
                complete: st,
                draggable: false,
                iconCls: 'dpwnd checkListItemNode',
                leaf: true
            });
            this.getRootNode().appendChild(ln);
            if(st==1)
                ln.ui.getTextEl().lastChild.style.textDecoration = "line-through";
        }
        this.getRootNode().expand();
    },


    loadCheckLists: function(){
//        if(this.clstore.getCount() > 0)
//            this.getSelectionModel().clearSelections();
        this.clstore.load({
            params: {
                checklistid: this.checklistid,
                taskid: this.taskid
            }
        });
    },

    markComplete:function(nodeid){
        var node = this.getNodeById(nodeid);
        var task = this.clstore.getAt(this.clstore.find("ctaskid", node.attributes.id));
        var tname = task.data["ctaskname"];
        if (!this.archived) {
            node.attributes.tname = tname;
            var check = false;
            check = node.attributes.complete == 1 ? false : true;
            this.setImageSource(nodeid, !(check));
            this.nodeCheckChange(node, check);
        }
    },

    nodeCheckChange:function(node, chkd){
        if(chkd){
            node.ui.getTextEl().lastChild.style.textDecoration="line-through";
            node.attributes.complete = 1;

            this.updateTaskStatus(node);
        }
        if(!chkd){
            node.attributes.complete = 0;
            node.ui.getTextEl().lastChild.style.textDecoration="none";

            this.updateTaskStatus(node);
        }
    },

    updateTaskStatus : function(node){
        var id = node.attributes.id;
        Wtf.Ajax.requestEx({
            method: 'POST',
            url: '../../checklist.jsp',
            params: {
                cm: 'updateCheckListToDoStatus',
                taskid: id,
                status: node.attributes.complete
            }
        }, this,
        function(result, req){
            var obj = Wtf.decode(result);
            if(obj){
                var prog = obj.data;
                this.fireEvent('nodeupdated', prog);
            }
            this.loadCheckLists();
        },
        function(result, req){
            msgBoxShow(4, 1);
        });
    },

    getPriorityImage: function(priority){
        var pri_src = this.moderatePriorityImg;
        var title1 = this.moderatePriorityTitle;
        if (priority == "High") {
            pri_src = this.highPriorityImg;
            title1 = this.highPriorityTitle;
        } else if (priority == "Low") {
            pri_src = this.lowPriorityImg;
            title1 = this.moderatePriorityTitle;
        }
        var pri_node = "<img class='priority' src='"+pri_src+"' style=\"margin-left:10px;vertical-align:middle;margin-right:5px;\" title='"+title1+"'></img>"
        return pri_node;
    },

    setImageSource:function(nodeid, completestatus){
        var src = (completestatus) ? this.incompleteImg : this.completeImg;
        var title = (completestatus) ? WtfGlobal.getLocaleText('pm.project.todo.markascomplete') : WtfGlobal.getLocaleText('pm.project.todo.markasincomplete');
        var getMark = Wtf.get("markclt"+nodeid);
        getMark.dom.src = src;
        getMark.dom.title = title;
    }

});

function showCheckListMaster(){
    var w = new Wtf.Window({
        title: WtfGlobal.getLocaleText('pm.admin.company.checklist.master.text'),
        id: 'checklistmaster',
        closable: true,
        modal: true,
        resizable: false,
        height: 400,
        width: 600,
        layout: 'border',
        buttonAlign: 'right',
        iconCls: 'iconwin',
        items: [{
                region: 'north',
                height: 75,
                border: false,
                bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                html: getTopHtml(WtfGlobal.getLocaleText('pm.admin.company.checklist.header'),
                WtfGlobal.getLocaleText('pm.admin.company.checklist.master.subheader'), "../../images/edituser40_52.gif")
            }, {
                region: 'center',
                border: false,
                layout: 'fit',
                items: [
                    new Wtf.tree.checkList({
                        layout: 'fit',
                        id: 'checklisttree',
                        animate: true,
                        enableDD: false,
                        containerScroll: true,
                        border: false,
                        autoScroll: true,
                        rootVisible: false
                })]
        }],
        buttons: [{
                id: 'clwinclose',
                text: WtfGlobal.getLocaleText('lang.close.text')
        }]
    });

    w.on('beforeclose', function(win){
        if(win.findById('checklisttree').changeTracker)
            win.showRefreshPrompt = true;
    })

    w.on('close', function(win){
        if(win.showRefreshPrompt){
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText('pm.msg.INFORMATION'),
                msg: WtfGlobal.getLocaleText('pm.admin.company.checklist.master.success') + '<br><b>' + WtfGlobal.getLocaleText('pm.msg.publish.refresh') + '</b>',
                buttons: Wtf.MessageBox.OK,
                icon: Wtf.MessageBox.INFO
            });
            var task = new Wtf.util.DelayedTask(function(){
                location.reload(true);
            }, this);
            task.delay(4000);
        } else return;
    }, this);
    w.show();
    Wtf.getCmp('clwinclose').addListener('click', w.close, w);
}

function resetProgress(){
    Wtf.MessageBox.show({
        title: 'Confirm',
        msg: 'This will recalculate and reset task progress - according to check list status - for all such tasks where check lists are associated, in all the projects.<br><b> Changes made will be permanent.</b><br> Do you want to continue?',
        buttons: Wtf.MessageBox.YESNO,
        icon: Wtf.MessageBox.QUESTION,
        fn: function(btn){
            if(btn == 'yes'){
                Wtf.Ajax.requestEx({
                    url: '../../checklist.jsp',
                    params: {
                        cm: 'calculateAllTasks',
                        companyid: companyid
                    },
                    method: 'POST'
                }, this,
                function(resp, req){
                    var obj = Wtf.decode(resp);
                    if(obj.success){
                        msgBoxShow(['Success', 'Task progress updated successfully. Please close and reopen all project plans.'], 0);
                    }
                },
                function(resp, req){
                    msgBoxShow(4, 1);
                });
            }
        }
    });
}

function markComplete(id, cmpID){
    Wtf.getCmp(cmpID).markComplete(id);
}
