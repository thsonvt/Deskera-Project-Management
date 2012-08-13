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
Wtf.TodoList = function(config){
    Wtf.apply(this,config);
    this.nodeHash = {};
    this.url = config.url;
    if(!this.url)
        this.url = Wtf.req.prj + 'todolistmanager.jsp';
    this.groupForm = null;
    this.taskform=null;
    this.taskform1=null;
    this.newTaskWindow=null;
    this.priorityCombo=null;
    this.taskcomp_imgpath = '../../images/tick.png';
    this.taskincom_imgpath = '../../images/tickcdis.png';
    this.normal_task = '../../images/normal1.png';
    this.high_task = '../../images/todo_high.png';
    this.low_task = '../../images/todo_low.png';
    this.hightitle = "High Priority Task";
    this.lowtitle = "Low Priority Task";
    this.normaltitle = "Moderate Priority Task";
    this.assignedCombo=null;
    this.taskNameField = null;
    this.roleid = config.roleid;
    if(this.roleid == 3 && Wtf.featuresView.proj && Wtf.subscription.proj.subscription)
        var addToPlanShow= false;
    else
        addToPlanShow=true;
    if(!this.archived){
        this.sinNotification = new Wtf.menu.Item({
            iconCls:'pwnd outbox',
            text : WtfGlobal.getLocaleText('pm.common.notifyselected'),
            allowDomMove:false,
            disabled: true,
            scope : this,
            handler : this.sendNotification
        });
        this.allNotification = new Wtf.menu.Item({
            iconCls:'pwnd outbox',
            text : WtfGlobal.getLocaleText('pm.common.notifyall'),
            scope : this,
            disabled: true,
            handler : this.sendNotificationtoAll
        });
        this.tbar= [{
            iconCls:'pwnd todolistpane',
            text:WtfGlobal.getLocaleText('pm.project.todo.addtodogroup'),
            id: 'todoAddGroup',
            tooltip: {title: WtfGlobal.getLocaleText('pm.project.todo.addtodogroup'),text: WtfGlobal.getLocaleText('pm.Help.addtodogroup')},
            handler: this.addTaskGroup,
            scope:this
        },{
            iconCls:'addnew',
            text:WtfGlobal.getLocaleText('pm.project.todo.addtodo'),
            id: 'todoAddTodo',
            tooltip: {title: WtfGlobal.getLocaleText('pm.project.todo.addtodo'),text: WtfGlobal.getLocaleText('pm.Help.addtodo')},
            handler: this.addTask,
            scope:this
        },
        this.deletebutton = new Wtf.Toolbar.Button({
            iconCls:'pwnd deliconwt',
            tooltip: {title: WtfGlobal.getLocaleText('pm.project.todo.deletetodo'),text: WtfGlobal.getLocaleText('pm.Help.deletetodo')},
            text:WtfGlobal.getLocaleText('pm.project.todo.deletetodo'),
            disabled: true,
            scope:this,
            id: this.userid + 'todoDelete',
            hidden : false,
            handler: this.confirmTaskDelete//this.deleteSelected
        }),new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText('pm.common.notifications.send'),
            iconCls:'pwnd outbox',
            id: this.userid + 'todoNotification',
            tooltip: {title:WtfGlobal.getLocaleText('pm.common.notification'),text: WtfGlobal.getLocaleText('pm.Help.notifytodo')},
            scope:this,
            menu:[this.sinNotification,this.allNotification]
        }),new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText('pm.project.todo.imports'),
            iconCls:'pwnd importiconToDo',
            id : this.userid+'importtodo',
            tooltip: {title:WtfGlobal.getLocaleText('pm.project.todo.imports'),text: WtfGlobal.getLocaleText('pm.Help.importtodo')},
            scope:this,
            menu : {
                items: [{
                    text: WtfGlobal.getLocaleText('pm.project.plan.import.csv.text'),
                    tooltip: {
                        text:Wtf.Help.importtodo
                    },
                    iconCls: 'pwnd importicon',
                    scope: this,
                    handler: this.importFromCSV
                }]
            }
        /*menu: [
                    new Wtf.Action({
                        text: WtfGlobal.getLocaleText('pm.project.plan.import.csv.text'),
                        iconCls: 'pwnd importicon',
                        scope: this,
                        handler: this.importFromCSV
                    })
                ]*/
        //  handler: this.importFromCSV
        }),
        this.addtoprojectplanbutton = new Wtf.Toolbar.Button({
            iconCls:'dpwnd addToPlan',
            tooltip: {
                title: WtfGlobal.getLocaleText('pm.project.plan.move'),
                text: WtfGlobal.getLocaleText('pm.Help.movetodo')
            },
            text:WtfGlobal.getLocaleText('pm.project.plan.move'),
            scope:this,
            hidden:addToPlanShow,
            id: 'addtopp',
            disabled: true,
            handler:this.AddToProjectPlan
        }),
        "-",
        "<a href=\""+Wtf.pagebaseURL+"feed.rss?m=todos&p="+config.userid+"\" target='_blank'><img id=\"todoRss\" class=\"rssimgMid\" alt=\"\" src=\"../../images/FeedIcon16.png\" Wtf:qtip='"+WtfGlobal.getLocaleText("pm.Help.rsstodo")+"'></img></a>"
        ];
    }
    this.listRoot = new Wtf.tree.TreeNode({
        draggable:false,
        id:'root_todo'+config.id
    });
    this.setRootNode(this.listRoot);
    if(config.groupType == Wtf.etype.proj){
        this.memberstorerecord = Wtf.data.Record.create([
                {name: 'name'}, {name: 'id'}
        ]);
        this.memberstore = new Wtf.data.Store({
            method: 'GET',
            id: 'tempstore',
            url:'../../jspfiles/project/getProjectMembers.jsp?login='+config.userid+'&pageno=0&pageSize=10000',
            reader : new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.memberstorerecord)
        });
        this.groupstorerecord = Wtf.data.Record.create([
                {name:'id',mapping:'id'},
                {name:'name',mapping:'name'},
                {name:'assignedto',mapping:'assignedto'}
        ]);
        this.groupstore = new Wtf.data.SimpleStore({
            id    : 'temp1store',
            fields: [
                 {name: 'id'},
                 {name: 'name'},
                 {name:'assignedto'}
            ]
        });
        this.taskpri = [["High"],["Moderate"],["Low"]];
        this.prioritystore = new Wtf.data.SimpleStore({
            id    : 'pristore',
            fields: ['priority'],
            data: this.taskpri
        });
    }
    var jRecord = Wtf.data.Record.create([
        {name: 'taskname'},
        {name: 'taskid'},
        {name: 'parentId'},
        {name: 'status'},
        {name: 'taskorder'},
        {name: 'assignedto'},
        {name: 'leafflag'},
        {name: 'duedate'},
        {name: 'description'},
        {name: 'priority'},
        {name: 'ischecklisttask'}
    ]);
    this.reader = new Wtf.data.KwlJsonReader({
        root:"data"
    },jRecord);
    this.ds1 = new Wtf.data.Store({
        url:this.url,
        baseParams: {action:1,userid:config.userid,grouptype:config.groupType},
        reader:this.reader
    });
    this.selModel=new Wtf.tree.MultiSelectionModel({
        id: 'test' + this.id
    }),
    this.ds1.on("load",this.dataRefresh,this);
    this.loadflag = true;
    this.append = true;
    Wtf.TodoList.superclass.constructor.call(this);
    if(!this.archived){
        this.on("movenode",this.nodeMoved,this);
        this.on("beforemovenode",this.beforeNodeMove,this);
        this.getSelectionModel().on('selectionchange',this.selectionChange,this);
        this.on('contextmenu', this.contextMenu, this);
    }
}
Wtf.extend(Wtf.TodoList,Wtf.tree.TreePanel,{
    afterRender: function() {
        Wtf.TodoList.superclass.afterRender.call(this);
        this.memberstore.load();
    },
    dataRefresh:function(ds,record,obj){
        if(ds.getCount() == 0 && !this.archived){
            this.addEmptyText();
        } else {
            this.removeEmptyText()
        }
        if(this.loadflag ){
            if(this.getRootNode().childNodes != null){
                if(this.getRootNode().childNodes.length > 0){
                    var nodelen = this.getRootNode().childNodes.length;
                    for(var i = 0; i < nodelen; i++){
                        this.getRootNode().childNodes[0].remove();
                    }
                }
            }
            this.loadflag = false;
            if(this.memberstore.find("name", '-') == -1){
                var none = new this.memberstorerecord({
                    name:'-',
                    id:'0'
                });
                this.memberstore.add(none);
            }
            if(this.groupstore.find("name", '-') == -1){
                var none1 = new this.groupstorerecord({
                    id:'0',
                    name:'-'
                });
                this.groupstore.add(none1);
            }
            this.isDataLoad=true;
            var clStatus = WtfGlobal.getCheckListModuleStatus();
            for(var i=0;i<record.length;i++) {
                var _data = record[i].data;
                var nodetxt =  _data.taskname;
                var nodeid = _data.taskid;
                var nodestate = parseInt(_data.status);
                var Torder = parseInt(_data.taskorder);
                var parentn = _data.parentId;
                var assigned = record[i].get("assignedto");
                var leafflag = true;
                var task_priority = _data.priority;
                var dd = _data.duedate;
                if(typeof record[i].get("leafflag") == "boolean")
                    leafflag = record[i].get("leafflag");
                else
                    leafflag = record[i].get("leafflag") == '0' ? false : true;
                parentn = this.getNodeById(parentn);
                var nodeclass = _data.ischecklisttask ? "dpwnd checkListItemNode": "todoNode";
                if(!parentn)
                    parentn = this.listRoot;
                var create = true;
                if(!clStatus && _data.ischecklisttask)
                    create = false;
                if(!leafflag && create){
                    nodeclass = _data.ischecklisttask ? "dpwnd checkListNode" : "groupNode";
                    var newgroup=new this.groupstorerecord({
                        id   : nodeid,
                        name : nodetxt
                    });
                    this.groupstore.add(newgroup);
                }
                var src = nodestate==0?this.taskincom_imgpath:this.taskcomp_imgpath;
                var title = nodestate==1?WtfGlobal.getLocaleText('pm.project.todo.markasincomplete'):WtfGlobal.getLocaleText('pm.project.todo.markascomplete');
                var d = new Date();
                var today = new Date();
                d.setFullYear(parseInt(dd.substr(0,4)),(parseInt(dd.substr(5,2),10))-1,parseInt(dd.substr(8,2),10));
                var node_priority_part = this.setPriorityImg(task_priority);
                var node_part = "<img id = 'mark"+nodeid+"' class='cancel' src='"+src+"' onclick= \"markasComplete('" + nodeid + "','"+ this.id + "')\"style=\"margin-left:5px;vertical-align:middle;margin-right:10px;\" wtf:qtip='"+title+"'></img>"
                if(assigned!="")
                    var memberid = this.memberstore.getAt(this.memberstore.find("id",assigned));
                if(assigned!="" && memberid){
                    var assignedmember= memberid.data["name"];
                    if(d < today && dd.indexOf("1970")==-1){
                        nodetxt = node_priority_part + node_part + "<span id='span"+nodeid+"' style=\"color:red;\">"+nodetxt+"["+assignedmember+"]</span>";
                    }else{
                        nodetxt = node_priority_part + node_part + "<span id='span"+nodeid+"' style=\"color:black;\">"+nodetxt+"["+assignedmember+"]</span>";
                    }
                } else {
                    _data.assignedto ="";
                    if(d < today && dd.indexOf("1970")==-1){
                        nodetxt = node_priority_part + node_part + "<span id='span"+nodeid+"' style=\"color:red;\">"+nodetxt+"</span>";
                    }else{
                        nodetxt = node_priority_part + node_part + "<span id='span"+nodeid+"' style=\"color:black;\">"+nodetxt+"</span>";
                    }
                }
                if(create){
                    var tempnode = new Wtf.tree.TreeNode({
                        id:nodeid,
                        text:nodetxt,
                        tname:nodetxt,
                        draggable: !this.archived && !_data.ischecklisttask,
                        iconCls:nodeclass,
                        parentnode:_data.parentId,
                        leaf:leafflag,
                        nodestate:nodestate,
                        Torder:Torder,
                        assignedTo:assigned,
                        duedate:_data.duedate,
                        priority:task_priority,
                        desc:_data.description,
                        ischecklisttask:_data.ischecklisttask
                    });
                    parentn.insertBefore(tempnode,parentn.item(Torder));
                    parentn.expand();
                    if(nodestate==1)
                        tempnode.ui.getTextEl().lastChild.style.textDecoration = "line-through";
                    if(!this.archived && !_data.ischecklisttask)
                        tempnode.on("dblclick",this.editNode,this);
                }
            }
        }
        if(!this.archived){
            if(record.length>0)
                this.allNotification.enable();
            else
                this.allNotification.disable();
        }
    },

    addEmptyText: function(){
        //emptytodo
        Wtf.DomHelper.append(this.body.dom, '<div id="empty" class="emptyGridText">'+WtfGlobal.getLocaleText('pm.todo.emtytext')+' <br><a href="#" onClick=\'getTodo(\"'+this.id+'\")\'>'+WtfGlobal.getLocaleText('pm.todo.starttext')+'</a></div>');

    },
    removeEmptyText: function(){
        //emptytodo
        var el = Wtf.get('empty');
        if(el)
            el.dom.innerHTML = "";

    },
    contextMenu: function(node, e){
        node.select();
        if (!node.attributes.ischecklisttask) {
            menu = new Wtf.menu.Menu({
                id: 'todotreeMenu',
                items: [{
                    text: WtfGlobal.getLocaleText('lang.edit.text'),
                    id: 'name',
                    iconCls: 'pwnd renameicon',
                    scope:this,
                    handler: this.editNodeOncontextMenu
                }, new Wtf.Action({
                    text: WtfGlobal.getLocaleText('lang.delete.text'),
                    iconCls: 'pwnd delicon',
                    scope:this,
                    hidden : this.roleid == 3 ? false :true,
                    handler: this.confirmTaskDelete//this.deleteSelected
                })]
            });
            menu.showAt(e.getXY());
        }
    },
    editNodeOncontextMenu:function(){
        var node_select = this.getSelectionModel().getSelectedNodes()[0];
        if(node_select.isLeaf())
            this.editnode1(node_select);
        else
            this.editGroup(node_select);
    },
    beforeNodeMove: function(tree,tnode,oldp,newp,num){
        if(tnode.attributes.ischecklisttask || newp.attributes.ischecklisttask)
            return false;
        if(!tnode.isLeaf() && newp != this.listRoot)
            return false;
    },

    confirmTaskDelete: function(){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'),WtfGlobal.getLocaleText('pm.msg.330'), function(btn){
            if (btn == "yes") {this.deleteSelected();}
            else return ;
        }, this);
    },
    deleteSelected: function(){
        var nodes = this.getSelectionModel().getSelectedNodes();//this.getChecked();
        for(var i=0;i<nodes.length;i++){
            if(!nodes[i].attributes.leaf){
                var removegroup = this.groupstore.getAt(this.groupstore.find("id",nodes[i].id));
                this.groupstore.remove(removegroup);
            }
        }
        var nodeid = "";
        var leafflag="";
        for(var i=0;i<nodes.length;i++){
            nodeid += nodes[i].attributes.id+",";
            leafflag+= nodes[i].isLeaf()+","
        }
        if(nodeid!=""){
            nodeid = nodeid.substr(0,nodeid.lastIndexOf(","));
            leafflag = leafflag.substr(0,leafflag.lastIndexOf(","));
            this.deletebutton.disable();
            Wtf.Ajax.requestEx({
                method: 'POST',
                url:this.url,
                params: ({
                    action:4,
                    taskid:nodeid,
                    leafflag:leafflag
                })},
            this,
            function(result, req){
                this.ds1.load();
                while(nodes.length>0){
                    var tempNode = nodes.pop();
                    try{
                        tempNode.remove();
                    }catch(e){
                        clog(e);
                    }
                }
                this.deletebutton.disable();
            },
            function(result, req){

                });
        }
    },
    editNode:function(tnode,evt){
        if(tnode.isLeaf())
            this.editnode1(tnode);
        else
            this.editGroup(tnode);
    },
    editGroup : function(node){
        this.makeForm(1, node);
        var nodeText = (node.text).split("[");
        this.taskNameField.setValue(WtfGlobal.HTMLStripper(nodeText[0]));
        var taskid = this.ds1.getAt(this.ds1.find("taskid",node.attributes.id));
        var assign = taskid.data["assignedto"];
        if(assign){
            Wtf.getCmp('combo').setValue(assign);
        }
        this.newTaskWindow = new Wtf.Window({
            width:310,
            resizable : false,
            id : 'editToDoGr'+this.id,
            modal:true,
            iconCls : 'iconwin',
            title:WtfGlobal.getLocaleText('pm.project.todo.group.edit'),
            buttons: [{
                anchor : '90%',
                id : 'save',
                text: WtfGlobal.getLocaleText('lang.submit.text'),
                handler:this.editsinglegroup,
                scope:this
            },{
                anchor : '90%',
                id : 'editWinClose'+this.id,
                text: WtfGlobal.getLocaleText('lang.close.text'),
                handler:function() {
                    Wtf.getCmp('editToDoGr'+this.id).close();
                },
                scope:this
            }],
            items:[this.groupForm]
        }).show();
    },
    editnode1 : function(node){
        this.makeForm1(2,node.id);
        var nodetxt = WtfGlobal.HTMLStripper(node.text);
        var nid = node.id;
        if(!Wtf.get('newtodo'+nid)){
            var newtext = "<div style='margin: 10px; border-top: 1px solid black;border-bottom: 1px solid black;'><div id='newtodo"+nid+"' style=\"display:none;padding-top: 10px;padding-bottom:10px;margin-left:100px;\" onclick=\"return false;\"></div></div>";
            node.setText(node.text + newtext);
            Wtf.getCmp('tf'+ node.id).setValue(nodetxt.split("[")[0]);
            var dd = node.attributes.duedate;
            var tem = dd.split(" ")[0];
            var d1 = tem.replace(/\//g,"-");
            Wtf.getCmp('dd'+node.id).format = 'Y-m-d';
            if(dd.indexOf("1970") == -1 && dd.indexOf("1969") == -1){
                Wtf.getCmp('dd'+node.id).setValue(d1);
            }
            var taskid = this.ds1.getAt(this.ds1.find("taskid",node.attributes.id));
            var pid = taskid.data["parentId"];
            var pid1 = node.parentNode.id;
            if(pid){
                Wtf.getCmp('Groupcombo'+node.id).setValue(pid1);
            }
            var de = WtfGlobal.HTMLStripper(taskid.data["description"]);
            Wtf.getCmp('ta'+node.id).setValue(de);
            var priority = node.attributes.priority;
            Wtf.getCmp('pricombo'+node.id).setValue(priority);
            var assign = taskid.data["assignedto"];
            if(assign){
                Wtf.getCmp('combo'+node.id).setValue(assign);
            }
            this.newTaskWindow = new Wtf.Panel({
                width:900,
                resizable : false,
                modal:true,
                frame:true,
                iconCls : 'iconwin',
                baseCls :'todoouterpanel',
                id : 'editToDoTask'+nid,
                layout:'fit',
                renderTo:'newtodo'+nid,
                buttonAlign:'center',
                buttons: [{
                    anchor : '90%',
                    id : 'save'+nid,
                    text: WtfGlobal.getLocaleText('lang.submit.text'),
                    handler:this.editsinglenode1,
                    scope:this
                },{
                    anchor : '90%',
                    id : 'editWinClose'+nid,
                    text: WtfGlobal.getLocaleText('lang.close.text'),
                    handler:function() {
                        var node = this.getNodeById(nid);
                        var task_id = this.ds1.getAt(this.ds1.find("taskid",node.attributes.id));
                        var taskname = task_id.data["taskname"];
                        var assignedTo = task_id.data["assignedto"];
                        if(assignedTo!=""){
                            var assigned = this.memberstore.getAt(this.memberstore.find("id",assignedTo)).data["name"];
                            this.setNodeText(node,taskname+"["+assigned+"]");
                        }else{
                            this.setNodeText(node,taskname);
                        }
                        if(node.attributes.nodestate==1){
                            node.ui.getTextEl().lastChild.style.textDecoration="line-through";
                        }
                        var dd = task_id.data["duedate"];
                        var d = new Date();
                        var today = new Date();
                        d.setFullYear(parseInt(dd.substr(0,4)),(parseInt(dd.substr(5,2),10))-1,parseInt(dd.substr(8,2),10));
                        if(d < today && dd.indexOf("1970")==-1){
                            node.ui.getTextEl().lastChild.style.color="red";
                        }
                    },
                    scope:this
                }],
                items:[this.taskform1]
            });
            Wtf.get("newtodo"+nid).dom.style.display="block";
        }
    },
    editsinglegroup:function(a){
        var node = this.getSelectionModel().getSelectedNodes()[0];
        var taskname = WtfGlobal.HTMLStripper(this.taskNameField.getValue());
        var id=a.id.substr(4);
        if(taskname!=""){
            var assigned ="";
            var getMember = Wtf.getCmp('combo').getValue();
            if(getMember!="0" && getMember){
                var assignedval = getMember;
                assigned = this.memberstore.getAt(this.memberstore.find("id",assignedval)).data["name"];
                this.setNodeText(node,taskname+"["+assigned+"]");
            }else{
                this.setNodeText(node,taskname);
            }
            node.attributes.tname = taskname;
            node.attributes.assignedTo = assignedval;
            this.updatedb_NewNode(2,node);
            Wtf.getCmp('editToDoGr'+this.id).close();

        }
    },
    editsinglenode1:function(a){
        var pressnode=a.id.substr(4);
        var task_name = Wtf.getCmp('tf'+pressnode);
        var task_desc = Wtf.getCmp('ta'+pressnode);
        if(task_name.isValid() && task_desc.isValid()){
            var taskname = WtfGlobal.HTMLStripper(task_name.getValue());
            if(taskname!=""){
                var assigned ="";
                var due ="";
                var dued = Wtf.getCmp('dd'+pressnode).getValue();
                if(dued){
                    due =  dued.format('Y/m/d H:i:s');
                }else{
                    due = "1970/01/01 00:00:00";
                }
                var desc =  WtfGlobal.HTMLStripper(task_desc.getValue());
                var node = this.getNodeById(pressnode);
                var newParentId = Wtf.getCmp('Groupcombo'+pressnode).getValue();
                var newparent = this.getNodeById(newParentId);
                var priority = Wtf.getCmp('pricombo'+pressnode).getValue();
                node.attributes.priority = priority;
                var getMember = Wtf.getCmp('combo'+pressnode).getValue();
                var oldParentId = node.parentNode.attributes.id;
                if(oldParentId!=newParentId && newParentId){
                    if(newParentId=="0"){
                        this.listRoot.appendChild(node);
                    } else{
                        newparent.appendChild(node);
                        this.listRoot.removeChild(node);
                        newparent.appendChild(node);
                        node.parentNode.attributes.id = newparent.attributes.id;
                        newparent.expand();
                        if(newparent.attributes.nodestate == 1){
                            node.attributes.nodestate = 1;
                        }
                    }
                }
                if(getMember!="0" &&  getMember){
                    var assignedval = getMember;
                    assigned = this.memberstore.getAt(this.memberstore.find("id",assignedval)).data["name"];
                    this.setNodeText(node,taskname+"["+assigned+"]");
                }else{
                    this.setNodeText(node,taskname);
                }
                var today=new Date();
                if(dued && dued.getTime()<today.getTime()){
                    node.ui.getTextEl().lastChild.style.color="red";
                }else{
                    node.ui.getTextEl().lastChild.style.color="black";
                }
                if(node.attributes.nodestate==1){
                    node.ui.getTextEl().lastChild.style.textDecoration="line-through";
                }
                node.attributes.assignedTo = assignedval;
                node.attributes.tname = taskname;
                node.attributes.duedate = due;
                node.attributes.desc = desc;
                this.updatedb1(2,node);
            }
        }
    },
    updatedb_NewNode : function(actiontype,node){
        var tid = node.attributes.id;
        var parentid = node.parentNode.attributes.id;
        if(parentid == this.listRoot.id)
        {
            parentid = "";
        }
        var torder = node.parentNode.indexOf(node);
        Wtf.Ajax.requestEx({
            method: 'POST',
            url:this.url,
            params: ({
                taskname:node.attributes.tname,
                localid:tid,
                taskorder:torder,
                status:node.attributes.nodestate,
                parentId:parentid,
                taskid:tid,
                desc:node.attributes.desc,
                priority:node.attributes.priority,
                userid:this.userid,
                grouptype:this.groupType,
                leafflag:node.isLeaf(),
                action:actiontype,
                duedate:"",
                assignedto:node.attributes.assignedTo
            })
        },
        this,
        function(result, req){
            var data = eval("(" + result + ")");
            if(data.data[0].remoteid!=""){
                switch(actiontype){
                    case 3:
                        var tnode = this.getNodeById(data.data[0].localid);
                        var imgsrc = node.attributes.nodestate==0?this.taskincom_imgpath:this.taskcomp_imgpath;
                        tnode.attributes.id = data.data[0].remoteid;
                        var pri_node = this.setPriorityImg(node.attributes.priority);
                        var nodetext = pri_node + "<img id = 'mark"+data.data[0].localid+"' class='cancel' src='"+imgsrc+"' onclick= \"markasComplete1('" + data.data[0].localid + "','"+ this.id + "','"+ data.data[0].remoteid + "')\"style=\"margin-left:5px;vertical-align:middle;margin-right:10px;\" title='Click to mark as complete'></img><span>"+tnode.text+"</span>";
                        tnode.setText(nodetext)
                        if(node.attributes.nodestate == 1){
                            tnode.ui.getTextEl().lastChild.style.textDecoration="line-through";
                        }
                        break;
                }
            } else {
                for(var cnt = this.listRoot.childNodes.length; cnt>0; cnt--)
                {
                    this.listRoot.removeChild(this.listRoot.childNodes[cnt-1]);
                }
                msgBoxShow(59, 1);
            }
            this.ds1.load();
        },
        function(result, req){
            msgBoxShow(4, 1);
        }
        );
    },
    updatedb1 : function(actiontype,node){
    //TODO:update node status on db query success[shri]
    var tid = node.attributes.id;
    var parentid = node.parentNode.attributes.id;
    if(parentid == this.listRoot.id){
            parentid = "";
    }
    var torder = node.parentNode.indexOf(node);
    Wtf.Ajax.requestEx({
            method: 'POST',
            url:this.url,
            params: ({
                taskname:node.attributes.tname,
                localid:tid,
                taskorder:torder,
                status:node.attributes.nodestate,
                parentId:parentid,
                taskid:tid,
                desc:node.attributes.desc,
                userid:this.userid,
                duedate:node.attributes.duedate,
                grouptype:this.groupType,
                assignedto:node.attributes.assignedTo,
                leafflag:node.isLeaf(),
                priority:node.attributes.priority,
                action:actiontype
            })},
            this,
            function(result, req){
                var data = eval("(" + result + ")");
                if(data.data){
                    if(data.data[0].remoteid!=""){
                    switch(actiontype){
                        case 3:
                              var tnode = this.getNodeById(data.data[0].localid);
                              var imgsrc = node.attributes.nodestate==0?this.taskincom_imgpath:this.taskcomp_imgpath;
                              tnode.attributes.id = data.data[0].remoteid;
                              var pri_node = this.setPriorityImg(node.attributes.priority);
                              var nodetext = pri_node + "<img id = 'mark"+data.data[0].localid+"' class='cancel' src='"+imgsrc+"' onclick= \"markasComplete1('" + data.data[0].localid + "','"+ this.id + "','"+ data.data[0].remoteid + "')\"style=\"margin-left:5px;vertical-align:middle;margin-right:10px;\" title='Click to mark as complete'></img><span>"+tnode.text+"</span>";
                              tnode.setText(nodetext);
                              if(node.attributes.nodestate == 1){
                                    tnode.ui.getTextEl().lastChild.style.textDecoration="line-through";
                              }
                              break;
                        }
                    }
                    else{
                        for(var cnt = this.listRoot.childNodes.length; cnt>0; cnt--)
                        {
                            this.listRoot.removeChild(this.listRoot.childNodes[cnt-1]);
                        }
                        msgBoxShow(59, 1);
                    }
                }
            this.ds1.reload();
        },
        function(result, req){
            msgBoxShow(4, 1);
        }
        );
    },
    makeForm :function(flag, node){
        this.taskNameField = new Wtf.form.TextField({
            fieldLabel : WtfGlobal.getLocaleText('pm.project.todo.text')+'*',
            anchor : '99%',
            allowBlank:false,
            maxLength:100
        });
        if(flag==0)
        {
            this.taskform = new Wtf.FormPanel({
                labelWidth: 120,
                labelAlign : 'left',
                border:false,
                autoWidth:true,
                bodyStyle:'padding:5px 5px 0',
                layout : 'form',
                anchor : '90%',
                defaultType: 'textfield',
                buttonAlign :'right',
                items: [this.taskNameField]
            });
            if(this.groupType == Wtf.etype.proj){
                this.assignedGroupCombo1 = new Wtf.form.ComboBox({
                    fieldLabel :WtfGlobal.getLocaleText('pm.project.todo.group.text'),
                    store : this.groupstore,
                    anchor : '99%',
                    displayField : 'name',
                    id:'Groupcombo1',
                    valueField : 'id',
                    typeAhead:true,
                    mode: 'local',
                    emptyText: WtfGlobal.getLocaleText('pm.common.group.select'),
                    triggerAction: 'all',
                    forceSelection : true,
                    selectOnFocus:true,
                    allowBlank : true
                });
                this.taskform.add(this.assignedGroupCombo1);
            }
        }else{
            this.taskNameField.fieldLabel=WtfGlobal.getLocaleText("pm.project.todo.group.groupname")+"*";
            if(this.groupType == Wtf.etype.proj){
                this.assignedCombo = new Wtf.form.ComboBox({
                fieldLabel :WtfGlobal.getLocaleText('pm.dashboard.widget.project.assignedto'),
                store : this.memberstore,
                anchor : '90%',
                cls:'todoCombo',
                ctCls:'todoComboCont',
                displayField : 'name',
                id:'combo',
                valueField : 'id',
                mode: 'local',
                listWidth:175,
                typeAhead:true,
                triggerAction: 'all',
                emptyText: WtfGlobal.getLocaleText('pm.project.member.select'),
                forceSelection : true,
                selectOnFocus:true,
                allowBlank : true
             });
           }
                this.groupForm = new Wtf.FormPanel({
                labelWidth: 120,
                labelAlign : 'left',
                border:false,
                bodyStyle:'padding:5px 5px 0',
                layout : 'form',
                anchor : '100%',
                defaultType: 'textfield',
                buttonAlign :'right',
                items: [this.taskNameField]
            });
            if(node)
                this.groupForm.add(this.assignedCombo);
        }
        this.taskNameField.on("change", function(){
            this.taskNameField.setValue(WtfGlobal.HTMLStripper(this.taskNameField.getValue()));
        },this);
    },
    makeForm1 :function(flag,no){
        if(this.groupType == Wtf.etype.proj){
            this.taskNameField1 = new Wtf.form.TextField({
                fieldLabel : WtfGlobal.getLocaleText('pm.project.todo.text')+'*',
                anchor : '99%',
                id:'tf'+no,
                cls:'toDoTextarea',
                allowBlank:false,
                maxLength:100,
                autoWidth:true
            });
            this.dueDate= new Wtf.form.DateField({
                fieldLabel: WtfGlobal.getLocaleText('pm.project.todo.duedate'),
                name: 'due',
                cls: 'toDoDateField',
                id:'dd'+no,
                emptyText: WtfGlobal.getLocaleText('pm.admin.company.holidays.selectdate'),
                anchor : '99%',
                allowBlank:true,
                readOnly:true
            });
            this.desc = new Wtf.form.TextArea({
                fieldLabel:WtfGlobal.getLocaleText('lang.description.text'),
                cls:'toDoTextarea',
                anchor : '99%',
                id:'ta'+no,
                allowBlank:true,
                maxlength:200
            });
            this.subform1 = new Wtf.form.FormPanel({
                width:380,
                baseCls: 'toDoPanel',
                items:[this.taskNameField1,this.dueDate,this.desc]
            });
            this.assignedCombo = new Wtf.form.ComboBox({
                fieldLabel :WtfGlobal.getLocaleText('pm.dashboard.widget.project.assignedto'),
                store : this.memberstore,
                anchor : '90%',
                cls:'todoCombo',
                ctCls:'todoComboCont',
                displayField : 'name',
                id:'combo'+no,
                valueField : 'id',
                mode: 'local',
                listWidth:175,
                typeAhead:true,
                triggerAction: 'all',
                emptyText: WtfGlobal.getLocaleText('pm.project.member.select'),
                forceSelection : true,
                selectOnFocus:true,
                allowBlank : true
             });
            this.assignedGroupCombo = new Wtf.form.ComboBox({
                fieldLabel :WtfGlobal.getLocaleText('pm.project.todo.group.text'),
                store : this.groupstore,
                anchor : '90%',
                cls:'todoCombo',
                ctCls:'todoComboCont',
                displayField : 'name',
                id:'Groupcombo'+no,
                valueField : 'id',
                mode: 'local',
                listWidth:175,
                emptyText: WtfGlobal.getLocaleText('pm.common.selectgroup'),
                typeAhead:true,
                triggerAction: 'all',
                forceSelection : true,
                selectOnFocus:true,
                allowBlank : true
            });
            this.priorityCombo = new Wtf.form.ComboBox({
                fieldLabel :WtfGlobal.getLocaleText('lang.priority.text'),
                store : this.prioritystore,
                anchor : '90%',
                cls:'todoCombo',
                ctCls:'todoComboCont',
                displayField : 'priority',
                id:'pricombo'+no,
                listWidth:175,
                mode: 'local',
                typeAhead:true,
                emptyText: WtfGlobal.getLocaleText('pm.project.plan.setpriority'),
                triggerAction: 'all',
                forceSelection : true,
                selectOnFocus:true,
                allowBlank : true
            });
            this.subform2 = new Wtf.form.FormPanel({
                layout:'form',
                baseCls: 'toDoPanel1',
                labelWidth: 80,
                width:305,
                items:[this.assignedCombo,this.assignedGroupCombo,this.priorityCombo]
            })
            this.taskform1 = new Wtf.Panel({
                labelAlign : 'left',
                border:false,
                baseCls:'todoinnerpanel',
                frame:true,
                bodyStyle:'padding:5px 5px 0',
                layout : 'column',
                anchor : '100%',
                buttonAlign :'center',
                items: [this.subform1,this.subform2]
            });
        }
        Wtf.getCmp('tf'+no).on("change", function(){
            Wtf.getCmp('tf'+no).setValue(WtfGlobal.HTMLStripper(Wtf.getCmp('tf'+no).getValue()));
        },this);
        Wtf.getCmp('ta'+no).on("change", function(){
            Wtf.getCmp('ta'+no).setValue(WtfGlobal.HTMLStripper(Wtf.getCmp('ta'+no).getValue()));
        },this);
    },
    createNode:function(){
        if(this.taskNameField.isValid()){
            var taskname = this.taskNameField.getValue();
            var dued="1970/01/01 00:00:00";
            var tempnode = new Wtf.tree.TreeNode({
                text:""+taskname,
                tname:taskname,
                iconCls:'todoNode',
                leaf:true,
                nodestate:0,
                Torder:0,
                duedate:dued,
                assignedTo:"",
                parentnode:"",
                priority:"Moderate"
            });
            this.taskNameField.reset();
            this.taskNameField.focus();
            var GroupSelect = this.assignedGroupCombo1.getValue();
            if(GroupSelect && GroupSelect!="0"){
                var newparent = this.getNodeById(GroupSelect);
                newparent.appendChild(tempnode);
                if(newparent.attributes.nodestate == 1){
                    tempnode.attributes.nodestate = 1;
                }
                tempnode.parentNode.attributes.id = newparent.attributes.id;
                newparent.expand();
            }else{
                this.listRoot.appendChild(tempnode);
            }
            tempnode.attributes.tname = taskname;
            var Torder=this.listRoot.indexOf(tempnode);
            tempnode.on("checkchange",this.nodeCheckChange,this);
            tempnode.on("dblclick", this.editNode, this);
            this.updatedb_NewNode(3,tempnode);
        }
    },
    createGroup:function(){
        var groupname = this.taskNameField.getValue();
        if(this.taskNameField.isValid()){
            var tempnode = new Wtf.tree.TreeNode({
                text:groupname,
                iconCls:'groupNode',
                leaf:false,
                nodestate:0,
                Torder:0,
                priority:"Moderate"
            });
            this.taskNameField.reset();
            this.taskNameField.focus();
            this.listRoot.appendChild(tempnode);
            tempnode.attributes.tname = groupname;
            var newTaskGroup = new this.groupstorerecord({
                id:tempnode.attributes.id,
                name:groupname
            });
            this.groupstore.add(newTaskGroup);
            var Torder=this.listRoot.indexOf(tempnode);
            tempnode.on("checkchange",this.nodeCheckChange,this);
            tempnode.on("beforeinsert",this.groupbinsert,this);
            tempnode.on("beforeappend",this.groupbinsert,this);
            tempnode.on("dblclick", this.editNode, this);
            this.updatedb_NewNode(3,tempnode);
        }
    },
    groupbinsert:function(tree,tnode,oldn,refnode){
        if(!oldn.isLeaf()){
            return false;
        }
    },
    addTask : function(){
        if(Wtf.get('empty')){
            Wtf.get('empty').remove();
        }
        this.makeForm(0);
        if(this.getSelectionModel().getSelectedNodes().length > 0){
            var select = this.getSelectionModel().getSelectedNodes()[0];
            if(!select.attributes.leaf && select){
                var group_id = this.groupstore.getAt(this.groupstore.find("id",select.id));
                var sg = group_id.data["id"];
                this.assignedGroupCombo1.setValue(sg);
            }
        }
        this.newTaskWindow = new Wtf.Window({
            width:300,
            resizable : false,
            modal:true,
            id : 'addToDoTask'+this.id,
            iconCls : 'iconwin',
            title:WtfGlobal.getLocaleText('pm.project.todo.new'),
            keys:{
                key:[10,13],
                fn:this.createNode,
                scope:this
            },
            buttons: [{
                anchor : '90%',
                id : 'save',
                text: WtfGlobal.getLocaleText('lang.add.text'),
                handler:this.createNode,
                scope:this
            },{
                anchor : '90%',
                id : 'close'+this.id,
                text: WtfGlobal.getLocaleText('lang.close.text'),
                scope:this,
                handler: this.closeCreateToDoWin
            }],
            items:[this.taskform]
        }).show();
        focusOn(this.taskNameField.id);
        Wtf.getCmp('addToDoTask'+this.id).on('close', this.closeCreateToDoWin, this);
    },
    addTaskGroup : function(){
        if(Wtf.get('empty')){
            Wtf.get('empty').remove();
        }
        this.makeForm(1);
        this.newTaskWindow = new Wtf.Window({
            width:330,
            iconCls : 'iconwin',
            resizable : false,
            id : 'addToDoGr'+this.id,
            modal:true,
            title:WtfGlobal.getLocaleText('pm.project.todo.group.New'),
            keys:{
                key:[10,13],
                fn:this.createGroup,
                scope:this
            },
            buttons: [{
                anchor : '90%',
                id : 'save',
                text: WtfGlobal.getLocaleText('lang.add.text'),
                handler:this.createGroup,
                scope:this
            },{
                anchor : '90%',
                id : 'close'+this.id,
                text: WtfGlobal.getLocaleText('lang.close.text'),
                scope:this,
                handler: this.closeCreateToDoWin
            }],
            items:[this.groupForm]
        }).show();
        focusOn(this.taskNameField.id);
        Wtf.getCmp('addToDoGr'+this.id).on('close', this.closeCreateToDoWin, this);
    },
    closeCreateToDoWin: function(){
        var win = Wtf.getCmp('addToDoTask'+this.id);
        if(!win)
            win = Wtf.getCmp('addToDoGr'+this.id);
        if(win)
            win.close();
        this.ds1.reload();
    },
    nodeCheckChange:function(node,chkd){
        if(chkd){
            node.ui.getTextEl().lastChild.style.textDecoration="line-through";
            node.attributes.nodestate = 1;
            if(!node.isLeaf())
                this.updatedb1(7,node);
            else
                this.updatedb1(2,node);
            node.eachChild(function(cnode){
                cnode.ui.getTextEl().lastChild.style.textDecoration="line-through";
                this.setImagSource(cnode.id,false);
                cnode.attributes.nodestate = 1;
                cnode.ui.toggleCheck(true);
            },this);
        }
        if(!chkd){
            node.attributes.nodestate = 0;
            node.ui.getTextEl().lastChild.style.textDecoration="none";
            if(!node.isLeaf())
                this.updatedb1(7,node);
            else
                this.updatedb1(2,node);
            node.eachChild(function(cnode){
                cnode.ui.getTextEl().lastChild.style.textDecoration="none";
                this.setImagSource(cnode.id,true);
                cnode.attributes.nodestate = 0;
                cnode.ui.toggleCheck(false);
            },this);
        }
        if(node.attributes.ischecklisttask)
            this.refreshPlan();
        var nodes = this.getChecked();
    },

    refreshPlan: function(){
        var projPlanobj = Wtf.getCmp(this.userid + "_projplanPane");
        if(projPlanobj !== undefined && projPlanobj.items !== undefined && projPlanobj.items.items[0].editGrid !== undefined){
            projPlanobj = Wtf.getCmp(this.userid + "projPlanCont");
            projPlanobj.refreshOnActivate = true;
        }
    },

    refreshToDos: function(){
        var tC = Wtf.getCmp('list_conainer' + this.userid);
        if(tC !== undefined && tC.items !== undefined && tC.items.items[0] !== undefined){
            this.isDataLoad = false;
            this.loadflag = true;
        }
    },

    selectionChange: function(a,nodeArray){
        if(nodeArray.length <=0){
            this.deletebutton.disable();
            this.sinNotification.disable();
            this.addtoprojectplanbutton.disable();
        }else{
            this.deletebutton.enable();
            this.sinNotification.enable();
            this.addtoprojectplanbutton.enable();
            for (var i = 0; i < nodeArray.length; i++) {
                var n = nodeArray[i];
                var isCT = n.attributes.ischecklisttask;
                var found = false;
                if (isCT) {
                    found = true;
                    break;
                }
            }
            if (found) {
                this.deletebutton.disable();
                this.sinNotification.disable();
                this.addtoprojectplanbutton.disable();
            }
        }
    },
    nodeMoved : function(tree,tnode,oldp,newp,num){
        if(tnode.isLeaf()){
            var taskname = "",nodeid = "",duedate = "",priority = "",nodestate = "",parentnode = "",leaf = "",assignedTo = "",torder = "",description = "";
            var tname = this.ds1.getAt(this.ds1.find("taskid",tnode.attributes.id)).data["taskname"];
            tnode.attributes.tname = tname;
            tnode.attributes.duedate = tnode.attributes.duedate.split(".")[0];
            if(newp.attributes.nodestate){
                tnode.attributes.nodestate =1;
                tnode.ui.getTextEl().lastChild.style.textDecoration="line-through";
                this.setImagSource(tnode.id,0);
            }
            newp.eachChild(function(cnode){
                var child_id = this.ds1.getAt(this.ds1.find("taskid",cnode.attributes.id));
                var tname = child_id.data["taskname"];
                cnode.attributes.tname = tname;
                cnode.attributes.duedate = cnode.attributes.duedate.split(".")[0];
                taskname += cnode.attributes.tname + ",";
                nodeid += cnode.attributes.id + ",";
                duedate += cnode.attributes.duedate + ",";
                priority += priority = cnode.attributes.priority + ",";
                nodestate += cnode.attributes.nodestate + ",";
                var parentid = cnode.parentNode.attributes.id;
                parentid = (parentid == this.listRoot.id) ? "-" : parentid;
                parentnode += parentid + ",";
                leaf += cnode.isLeaf() +",";
                torder += cnode.parentNode.indexOf(cnode) + ",";
            },this)
            Wtf.Ajax.requestEx({
                method: 'POST',
                url:this.url,
                params: ({
                    taskname:taskname,
                    localid:nodeid,
                    task_order:torder,
                    tstatus:nodestate,
                    parentId:parentnode,
                    taskid:nodeid,
                    userid:this.userid,
                    duedate:duedate,
                    grouptype:this.groupType,
                    leafflag:leaf,
                    priority:priority,
                    action:6
            })},
            this,
            function(result, req){
                this.ds1.reload();
            },
            function(result, req){
                msgBoxShow(4, 1);
            }
            );
        }
    },
    sendNotificationtoAll:function(){
        var todorec = this.ds1.data.items;
        var idstr = "";
        var assignidstr = "";
        var notificationflag = true;
        if(todorec.length>0){
            for(var ctr=0;ctr<todorec.length;ctr++) {
                if(todorec[ctr].data.assignedto!="" ){
                    if(todorec[ctr].data.status==0){
                        idstr +="'"+ todorec[ctr].data.taskid+"',";
                        assignidstr += todorec[ctr].data.assignedto+",";
                    }else{
                        notificationflag = false;
                    }
                }
            }
        }
        if(idstr!=""){
            idstr = idstr.substr(0,idstr.lastIndexOf(","));
            assignidstr = assignidstr.substr(0,assignidstr.lastIndexOf(","));
            this.notify(idstr,assignidstr,notificationflag);
        }else{
            msgBoxShow(164,1);
        }
    },
    sendNotification:function(){
        var ab = this.getSelectionModel().selNodes;
        var idstr = "";
        var assignidstr = "";
        var rec;
        var notificationflag = true;
        for(var ctr=0;ctr<ab.length;ctr++) {
            rec = this.ds1.getAt(this.ds1.find("taskid",ab[ctr].attributes.id));
            if(rec.data.assignedto!="" ){
                if(rec.data.status == 0){
                    idstr += "'"+rec.data.taskid+"',";
                    assignidstr += rec.data.assignedto+",";
                }else{
                    notificationflag = false;
                }
            }else{
                var i = 0;
                while(ab[ctr].childNodes[i]!=null){
                    rec = this.ds1.getAt(this.ds1.find("taskid",ab[ctr].childNodes[i].attributes.id));
                    if(rec.data.status == 0){
                        idstr += "'"+rec.data.taskid+"',";
                        assignidstr += rec.data.assignedto+",";
                    }else{
                        notificationflag = false;
                    }
                    i++;
                }
            }
        }
        if(idstr!=""){
            idstr = idstr.substr(0,idstr.lastIndexOf(","));
            assignidstr = assignidstr.substr(0,assignidstr.lastIndexOf(","));
            this.notify(idstr,assignidstr,notificationflag);
        }else{
            msgBoxShow(164,1);
        }
    },
    notify:function(idstr,assignidstr,notificationflag){
        Wtf.Ajax.requestEx({
            method:'POST',
            url: Wtf.req.prj + 'todolistmanager.jsp',
            params:{
                action:5,
                userid:this.userid,
                grouptype:this.groupType,
                idstr:idstr,
                assignidstr:assignidstr
            }
        }, this, function(result, req){
            if(result == "typeError"){
                msgBoxShow(183,0);
            } else{
                if(notificationflag){
                    msgBoxShow(166,0);
                }else{
                    msgBoxShow(165,0);
                }
            }
        });
    },

    markComplete:function(nodeid){
        var node = this.getNodeById(nodeid);
        var taskid = this.ds1.getAt(this.ds1.find("taskid",node.attributes.id));
        var tname = taskid.data["taskname"];
        if(!this.archived){
            node.attributes.tname = tname;
            var check = false;
            node.attributes.duedate = node.attributes.duedate.split(".")[0]
            check = node.attributes.nodestate==1?false:true;
            this.setImagSource(nodeid,!(check));
            this.nodeCheckChange(node,check)
        }
    },

    markComplete1:function(nodeid,remoteid){
        var taskid = this.ds1.getAt(this.ds1.find("taskid",remoteid))
        var tname = taskid.data["taskname"];
        if(!this.archived){
            var node = this.getNodeById(nodeid);
            node.attributes.tname = tname;
            var check = false;
            if(node.attributes.leaf){
                node.attributes.duedate = node.attributes.duedate.split(".")[0]
            }
            check = node.attributes.nodestate==1?false:true;
            this.setImagSource(nodeid,!(check));
            this.nodeCheckChange(node,check)
        }
    },
    setPriorityImg:function(priority){
        var pri_src = "";
        var title1 = ""
        if(priority=="High"){
            pri_src = this.high_task;
            title1 = this.hightitle;
        }else if(priority=="Low"){
            pri_src = this.low_task;
            title1 = this.lowtitle;
        }else{
            pri_src = this.normal_task;
            title1 = this.normaltitle;
        }
        var pri_node = "<img class='priority' src='"+pri_src+"' style=\"margin-left:10px;vertical-align:middle;margin-right:5px;\" title='"+title1+"'></img>"
        return pri_node;
    },
    setImagSource:function(nodeid,completestatus){
        var src = (completestatus)?this.taskincom_imgpath:this.taskcomp_imgpath;
        var title = (completestatus)?WtfGlobal.getLocaleText('pm.project.todo.markascomplete'):WtfGlobal.getLocaleText('pm.project.todo.markasincomplete');
        var getMark = Wtf.get("mark"+nodeid);
        getMark.dom.src=src;
        getMark.dom.title=title;
    },
    setNodeText:function(node,textvalue){
        var imgsrc = node.attributes.nodestate==0?this.taskincom_imgpath:this.taskcomp_imgpath;
        var title = node.attributes.nodestate==0?WtfGlobal.getLocaleText('pm.project.todo.markascomplete'):WtfGlobal.getLocaleText('pm.project.todo.markasincomplete');
        var pri_node = this.setPriorityImg(node.attributes.priority);
        var nodetext = pri_node + "<img id = 'mark"+node.id+"' class='cancel' src='"+imgsrc+"' onclick= \"markasComplete('" + node.id + "','"+ this.id + "')\"style=\"margin-left:5px;vertical-align:middle;margin-right:10px;\" title='"+title+"'></img><span>"+textvalue+"</span>";
        node.setText(nodetext);
    },
    importFromCSV:function(){
        var dateFormatStore = new Wtf.data.JsonStore({
            url:"../../admin.jsp?mode=25&action=0",
            root:'data',
            fields : ['id','name','dateformat', 'seppos']
        });
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
                        labelStyle: "width:80px;",
                        fieldLabel:WtfGlobal.getLocaleText('pm.common.filename'),
                        validator: WtfGlobal.validateCSVFile,
                        invalidText: WtfGlobal.getLocaleText("pm.common.validatecsv.invalidtext"),
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
                        mode: 'remote',
                        width: 240,
                        triggerAction: 'all',
                        emptyText: WtfGlobal.getLocaleText('pm.common.dateformat.text'),
                        allowBlank: false
                    }]
                }]
            },{
                layout: 'column',
                id: 'importRadio',
                labelWidth: 110,
                cls: 'radPanel',
                items: [{
                    columnWidth: 0.55,
                    layout: 'fit',
                    bodyStyle: 'padding-top: 5px',
                    html: WtfGlobal.getLocaleText('pm.common.option.select')+':'
                },{
                    columnWidth: 0.2,
                    layout: 'fit',
                    height : 25,
                    items: [{
                        xtype : 'radio',
                        boxLabel:WtfGlobal.getLocaleText('pm.project.plan.import.append'),
                        height : 20,
                        checked: true,
                        id:'ap',
                        name:'nam'
                    }]
                },{
                    columnWidth: 0.2,
                    layout: 'fit',
                    height : 25,
                    items: [{
                        xtype : 'radio',
                        boxLabel:WtfGlobal.getLocaleText('pm.project.plan.import.overwrite'),
                        height : 25,
                        id:'ov',
                        name:'nam'
                    }]
                }]
            }
        ]},
        this);
        this.upWin1 = new Wtf.Window({
            resizable: false,
            scope: this,
            layout: 'fit',
            modal:true,
            width: 460,
            height: 300,
            iconCls: 'iconwin',
            id: 'uploadwindow',
            title: WtfGlobal.getLocaleText('pm.prioject.todo.import'),
            items: this.UploadPanel1,
            buttons: [{
                text: WtfGlobal.getLocaleText('lang.import.text'),
                id: 'submitPicBttn',
                type: 'submit',
                scope: this,
                handler: function(){
                    if(this.UploadPanel1.form.isValid()){
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
    },
    importCSVFile :function(){
        var parsedObject = document.getElementById('browseBttn').value;

        var extension =parsedObject.substr(parsedObject.lastIndexOf(".")+1);
        var patt = new RegExp("csv","i");
        var userchoice = Wtf.getCmp("ov").getValue() == true ? 1 : 0;
        if(userchoice == 1){
            this.append = false;
        }
        this.groupstore.removeAll();
        var dfid = Wtf.getCmp('dfcombo').getValue();
        if(patt.test(extension)) {
            this.UploadPanel1.form.submit({
                url:'../../importToDoTask?&appendchoice='+userchoice+'&projectid='+this.userid+'&action=1',
                waitMsg :'Importing...',
                scope:this,
                success: function (result, request) {
                    this.upWin1.close();
                    var obj = eval('('+request.response.responseText+')');
                    this.showHeaderMappingWindow(obj, userchoice, dfid);
                },
                failure: function ( result, request) {
                    this.upWin1.close();
                    var obj = eval('('+request.response.responseText+')');
                    msgBoxShow([0,"Error during uploading CSV file"], 1);
                }
            },this);
            this.upWin1.buttons[0].disable();
            this.upWin1.buttons[1].disable();
        } else
            msgBoxShow(56, 1);
    },

    showHeaderMappingWindow :function(res, userchoice, dfid){
        var headerlist = [
        [ 0 , 'Task Name' ],
        [ 1 , 'Due Date' ],
        [ 2 , 'Description' ],
        [ 3 , 'Priority' ],
        [ 4 , 'Status' ],
        [ 5 , '-' ]  //for extra any unmapped column
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
                    if(headerName.indexOf('title') != -1 || headerName.indexOf('task name') != -1|| headerName.indexOf('todoname') != -1){
                        var rec = hcmbostore.query(headerCombo.displayField, 'Task Name', true, true);
                        break;
                    } else if(headerName.indexOf('end') != -1 || headerName.indexOf('edate') != -1 || headerName.indexOf('due') != -1 || headerName.indexOf('finish') != -1 ){
                        rec = hcmbostore.query(headerCombo.displayField,'Due Date', true, true);
                        break;
                    } else if(headerName.indexOf('progress') != -1 || headerName.indexOf('status') != -1 || headerName.indexOf('mark') != -1){
                        rec = hcmbostore.query(headerCombo.displayField,'Status', true, true);
                        break;
                    } else if(headerName.indexOf('priority') != -1 || headerName.indexOf('importance') != -1){
                        rec = hcmbostore.query(headerCombo.displayField,'Priority', true, true);
                        break;
                    } else if(headerName.indexOf('notes') != -1 || headerName.indexOf('summary') != -1 || headerName.indexOf('description') != -1|| headerName.indexOf('detail') != -1){
                        rec = hcmbostore.query(headerCombo.displayField,'Description', true, true);
                        break;
                    } else {
                        rec = hcmbostore.query(headerCombo.displayField, "-", true);
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
            header: WtfGlobal.getLocaleText('pm.project.todo.import.attribute'),
            dataIndex: 'header'
        },{
            header: WtfGlobal.getLocaleText('pm.project.todo.import.headermapping.existing'),
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
            height: 415,
            iconCls: 'iconwin',
            id: 'importcsvwindow',
            title: WtfGlobal.getLocaleText('pm.project.task.map'),
            items:[{
                region : 'north',
                height : 75,
                border : false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                html : getTopHtml(WtfGlobal.getLocaleText('pm.project.todo.import.headermapping.header'),WtfGlobal.getLocaleText('pm.project.todo.import.headermapping.subheader'),"../../images/exportcsv40_52.gif")
            }, haderMapgrid],
            buttons: [{
                text: WtfGlobal.getLocaleText('lang.import.text'),
                type: 'submit',
                scope: this,
                handler: function(){
                    var mappedHeaders = '';
                    var headerArray = new Array();
                    var comboCount = headerds.getCount()-1; //mapping-combo records count escape last record for unmapped column
                    for(j=0;j<listds.getCount();j++)
                        headerArray[j] = 0;
                    for(var j=0;j<listds.getCount();j++){
                        var index = listds.getAt(j).get("index");
                        if(index < comboCount){ //consider only mapping-combo records, skip other
                            headerArray[index]=headerArray[index]+1;
                            var rec = headerCombo.store.getAt(index);
                            if(rec != undefined )
                                mappedHeaders += "\""+rec.get(headerCombo.displayField)+"\":"+j+",";
                        }
                    }
                    mappedHeaders = mappedHeaders.substr(0, mappedHeaders.length-1);
                    mappedHeaders = "{"+mappedHeaders+"}";
                    var mismatch = 0;
                    for(j=0;j<comboCount;j++){  //mapping-combo record count
                        if(headerArray[j]>1){   //for one to one mapping use " != 1"
                            mismatch = 1;
                            break;
                        }
                    }
                    if(mismatch == 1){
                        msgBoxShow(236, 1);
                        return;
                    }
                    if(headerArray[0]==0){ //headerArray[1] -> start date mapping compulsory
                        msgBoxShow(272, 1);
                        return;
                    }
                    Wtf.Ajax.request({
                        method: 'POST',
                        url: '../../importToDoTask',
                        params: ({
                            mappedheader : mappedHeaders,
                            append : userchoice,
                            dfid : dfid,
                            projectid : this.userid,
                            filename : res.FileName,
                            action : 2
                        }),
                        scope: this,
                        success: function(result, request){
                            this.headerMapWin.close();
                            this.loadflag = true;
                            if(Wtf.get('empty')){
                                Wtf.get('empty').remove();
                            }
                            this.ds1.load();
                            var obj = eval('('+result.responseText+')');
                            if(obj.success)
                                msgBoxShow([WtfGlobal.getLocaleText('pm.msg.SUCCESS'), obj.msg], 0);
                            else
                                msgBoxShow(234, 1);
                        },
                        failure: function(result, req){
                            this.headerMapWin.close();
                            msgBoxShow(234, 1);
                        }
                    });
                }
            },{
                text:WtfGlobal.getLocaleText('lang.cancel.text'),
                scope:this,
                handler:function() {
                    this.headerMapWin.close();
                }
            }]
        }),
        this.headerMapWin.show();
    },

    AddToProjectPlan: function(){
        var records=this.getSelectionModel().getSelectedNodes();
        var index = -1, req = true, dd, attrib;
        for(var cnt = 0; cnt < projects.length; cnt++){
            if(projects[cnt].id == this.userid){
                index = cnt;
                break;
            }
        }
        if(index !== -1){
            var projStartDate = projects[index].startDate;
            if(projStartDate){
                var temp = Date.parseDate(projStartDate, 'Y-m-d H:i:s').clearTime(true);
                projStartDate = temp.format('Y-m-d H:i:s');
            }
        }
        for(var i=0;i<records.length;i++){
            if(!records[i].attributes.leaf){
                for(var j=0;j<records[i].childNodes.length;j++){
                    var child = records[i].childNodes[j];
                    attrib = child.attributes;
                    if(attrib.duedate.indexOf("1970") == -1 && attrib.duedate.indexOf("1969") == -1){
                        if(projStartDate){
                            child.attributes.duedate = attrib.duedate.replace(/\//g,"-");
                            if(child.attributes.duedate < projStartDate){
                                req = false;
                                index = WtfGlobal.HTMLStripper(attrib.text);
                                dd = attrib.duedate;
                                break;
                            }
                            child.attributes.duedate = attrib.duedate.replace(/-/g, '/');
                        }
                    }
                }
            } else {
                attrib = records[i].attributes;
                if(attrib.duedate.indexOf("1970") == -1 && attrib.duedate.indexOf("1969") == -1){
                    if(projStartDate){
                        records[i].attributes.duedate = attrib.duedate.replace(/\//g,"-");
                        if(records[i].attributes.duedate < projStartDate){
                            req = false;
                            index = WtfGlobal.HTMLStripper(attrib.text);
                            dd = attrib.duedate;
                            break;
                        }
                        records[i].attributes.duedate = attrib.duedate.replace(/-/g,"/");
                    }
                }
            }
        }
        var recordid = "";
        for(var i=0;i<records.length;i++){
            var parentid = records[i].parentNode.attributes.id;
            if(parentid == this.listRoot.id){
                parentid = "";
            }
            if(parentid == ""){
                recordid += records[i].attributes.id+",";
            }else{
                for(var j=0; j<records.length;j++){
                    if(records[j].attributes.id == parentid){
                        break;
                    }
                }
                if(j==records.length){
                    recordid += records[i].attributes.id+",";
                }
            }
        }
        projStartDate = Date.parseDate(projStartDate, 'Y-m-d H:i:s').format(WtfGlobal.getOnlyDateFormat());
        if(dd)
            dd = Date.parseDate(dd, 'Y-m-d H:i:s').format(WtfGlobal.getOnlyDateFormat());
        if(recordid!=""){
            recordid = recordid.substr(0,recordid.lastIndexOf(",")); //checking
            var msg = (!req) ? WtfGlobal.getLocaleText({key:'pm.project.todo.movetoplan.prompt',params:[index, dd, projStartDate]}) : WtfGlobal.getLocaleText('pm.msg.330');
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), msg, function(btn){
                if (btn == "yes") {
                    var tp = Wtf.getCmp('as').getActiveTab();
                    if(Wtf.getCmp(this.userid+'_projplanPane'))
                        tp.findByType('tabpanel')[0].setActiveTab(this.userid+'_projplanPane');
                    Wtf.Ajax.requestEx({
                        method: 'POST',
                        url:this.url,
                        params: {
                            action:8,
                            taskid:recordid,
                            userid:this.userid
                        }
                    }, this,
                    function(result, request) {
                        for(i=0;i<records.length;i++){
                            if(!records[i].attributes.leaf){
                                var removegroup = this.groupstore.getAt(this.groupstore.find("id",records[i].id));
                                this.groupstore.remove(removegroup);
                            }
                        }
                        this.ds1.load();
                        while(records.length>0){
                            var tempNode = records.pop();
                            try{
                                tempNode.remove();
                            }catch(e){
                                clog(e);
                            }
                        }
                    },
                    function(result, req){});
                } else return;
            }, this);
        }
    }
});

function markasComplete(nodeid,objid){
    if(!Wtf.get('newtodo'+nodeid)){
        Wtf.getCmp(objid).markComplete(nodeid);
    }
}

function markasComplete1(nodeid,objid,remoteid){
    if(!Wtf.get('newtodo'+nodeid)){
        Wtf.getCmp(objid).markComplete1(nodeid,remoteid);
    }
}
function getTodo(id){
    //Wtf.getCmp(id).removeEmptyText();
    //Wtf.DomHelper.append(Wtf.getCmp(id).body.dom, '');

    Wtf.getCmp(id).addTask();
}
