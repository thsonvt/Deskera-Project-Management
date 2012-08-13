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

Wtf.CalendarTree  = function(config){
    Wtf.apply(this,config);
    this.nodeHash = {};
    this.containerScroll=true;
    this.border=false;
    this.bodyStyle = "padding:10px;";
    this.rootVisible= true;
    this.lines= false;
    this.agendaStorereder = Wtf.data.Record.create(["cid","cname","description","location","timezone","colorcode","caltype","isdefault","userid","timestamp","permissionlevel","exportCal","url",{name: "interval", mapping: "syncinterval"}, "lastselected"]);
    this.treeRoot = new Wtf.tree.TreeNode({
        id: this.parentid + 'rn',
        cls: 'calroot',
        expanded: true,
        text: WtfGlobal.getLocaleText('pm.project.calendar.calendars.text'),
        cls: 'takeleft',
        singleClickExpand: false
    });
    this.setRootNode(this.treeRoot);
    this.agendaStore = new Wtf.data.Store({
        url:this.url,
        baseParams:{
            action:0,
            userid:this.ownerid.userid,
            caltype:this.ownerid.type,
            loginid: loginid,
            latestts:"1970-01-01 00:00:00"
        },
        reader:new Wtf.data.KwlJsonReader({
            root:'data'
        },this.agendaStorereder)
    });


    this.agendaStore.on("load",this.loadTree,this);
    this.agendaStore.load();
//  this.datePicker.on("select",this.DateSelected,this);
    this.addEvents({
        "treecheckchange": true,
        "changecolor": true,
        "deletecalendar": true,
        "calendarsettings": true
    });

    Wtf.CalendarTree.superclass.constructor.call(this);
}

Wtf.extend(Wtf.CalendarTree,Wtf.tree.TreePanel,{
    onRender: function(config){
        Wtf.CalendarTree.superclass.onRender.call(this,config);
        this.getSelectionModel().on("beforeselect",this.selectChanged,this);
    },

    loadTree: function (obj,rec,opt){
        var nodeCreated=false;
        for (var cnt=0;cnt<this.agendaStore.getTotalCount();cnt++) {
            var agenData = this.agendaStore.getAt(cnt).data;
            var calname = name = Wtf.util.Format.ellipsis(agenData.cname,15);
            var tnode=this.treeRoot.appendChild(new Wtf.tree.TreeNode({
                text: calname,
                allowDrop: false,
                allowDrag: false,
                checked: (!nodeCreated),
                icon: "../../lib/resources/images/default/s.gif",
                iconCls: 'imgchange',
                cls: 'treenodeclass',
                qtip : agenData.description,
                qtipTitle : agenData.cname,
                id: agenData.cid,
                uiProvider: Wtf.tree.TableTreeUI,
                colorIndex: parseInt(agenData.colorcode),
				caltype : agenData.caltype,
                lastselected: agenData.lastselected
            }));
            if(tnode)
                tnode.attributes.request=false;
            nodeCreated=true;
        }
        if(nodeCreated){
            this.defaultNode = this.getNodeById(this.agendaStore.getAt(0).data["cid"]);
            this.defaultNode.attributes.request = false;
            this.calcontrol = Wtf.getCmp(this.calControl);
            this.calcontrol.startCalEventBot(this.defaultNode.id);
//            this.calcontrol.onWorkWeekViewClick();
            this.calcontrol.onDayViewClick();
            this.calcontrol.CalculatingTotalCalendar();
        }
        this.attachListeners();
        var sm = this.treeRoot.childNodes;
        for(var i=0; i<sm.length; i++){
            if(sm[i].attributes.lastselected){
                sm[i].ui.checkbox.checked = true;
                sm[i].attributes.checked = true;
                this.fireEvent("treecheckchange", this, sm[i], true, false);
            }
        }
    },

    makeContextMenu: function(nodeobj){
        this.contextMenu = null ;
        var colorPicker = new Wtf.menu.ColorItem({
            id: this.parentid + 'coloritem',
            colors: ["CC3333", "DD4477", "994499", "6633CC", "336699", "3366CC", "22AA99", "329262", "109618", "66AA00", "AAAA11", "D6AE00", "EE8800", "DD5511", "A87070", "8C6D8C", "627487", "7083A8", "5C8D87", "898951", "B08B59"]
        });

        this.contextMenu = new Wtf.menu.Menu({
            id: this.parentid + 'contextMenu',
            items: [{
                id: this.parentid + 'Delete',
                iconCls: 'pwnd delicon',
                text: WtfGlobal.getLocaleText('lang.delete.text'),
                scope: this,
                handler: function(){
                    if(this.treeRoot.firstChild == this.getSelectionModel().getSelectedNode()){
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.343'), function(btn){
                            if(btn == "yes"){
                                var msg = {};
                                msg.events = true;
                                msg.id = this.treeRoot.firstChild.id;
                                this.fireEvent("deletecalendar",msg.id);
                            }
                        },this);
                    }
                    else{
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.341'), function(btn){
                            if (btn == "yes") {
                                var msg = {};
                                msg.events = false;
                                msg.id = this.getSelectionModel().getSelectedNode().id;
//                                this.getSelectionModel().getSelectedNode().remove();
//                                this.fireEvent("deletecalendar",this,msg);
                                this.fireEvent("deletecalendar",msg.id);
                            }
                        },this);
                    }
                }
            },
            /*{
                id: this.parentid + 'Sync',
                iconCls: 'pwnd refresh',
                hidden : (this.getSelectionModel().getSelectedNode().attributes.caltype == 4) ? false : true,
                text: WtfGlobal.getLocaleText('pm.events.sync'),
                scope: this,
                handler: function(){
                    
                }
            },*/
             {
                id: this.parentid + 'Edit',
                iconCls: 'dpwnd editiconwt',
                text: WtfGlobal.getLocaleText('pm.common.settings'),
                scope: this,
                handler: function(){
                    this.fireEvent("calendarsettings",this.getSelectionModel().getSelectedNode().id, this.getSelectionModel().getSelectedNode().attributes.caltype);
                }
            }, {
                id: this.parentid + 'thisonly',
                text: WtfGlobal.getLocaleText('pm.project.calendar.displayonlythis'),
                iconCls: 'dpwnd checked',
                scope: this,
                handler: function(){
                    var s = this.getSelectionModel().getSelectedNode();
                    if(nodeobj.getOwnerTree() != null)
                        var sm = nodeobj.getOwnerTree().treeRoot.childNodes;
                    else
                        sm = this.treeRoot.childNodes;
                    for(var i=0;i<sm.length;i++){
                        sm[i].ui.checkbox.checked = false;
                        sm[i].attributes.checked = false;
                        this.fireEvent("treecheckchange",this, sm[i], false, true);
                    }
                    s.ui.checkbox.checked = true;
                    s.attributes.checked=true;
                    this.fireEvent("treecheckchange",this, s, true, true);
                 }
            }, {
                id: this.parentid + 'exportthisonly',
                text: WtfGlobal.getLocaleText('pm.project.calendar.export'),
                iconCls: 'dpwnd calexporticon',
                scope: this,
                handler: function(){
                    var s = this.getSelectionModel().getSelectedNode();
                        Wtf.Ajax.requestEx({
                            method: 'GET',
                            url: Wtf.calReq.cal + 'calEvent.jsp',
                            params: {
                                action: 6,
                                cid: s.id,
                                caltype: s.attributes.caltype
                            }
                        }, this,
                        function(result){
                            var nodeobj = eval("(" + result + ")");
                            if(nodeobj.exportCheck == 'true'){
                                var exportURL1 = Wtf.pagebaseURL + "exportICS.ics?cid=" + s.id;
                                var iCalLink1 = "<a href='javascript:document.exportLinkForm1.exportLinkField1.select()'>"+WtfGlobal.getLocaleText('pm.project.calendar.export.selecturl')+"</a>";
                                iCalLink1= iCalLink1+"<form name='exportLinkForm1'><textarea readonly='' name='exportLinkField1' style='width:500px; background:white' onclick='javascript:document.exportLinkForm1.exportLinkField1.select()'>"+exportURL1+"</textarea>";
                                var msgExport1 = WtfGlobal.getLocaleText('pm.project.calendar.export.info')+"<br/>"+iCalLink1;
                                msgBoxShow([WtfGlobal.getLocaleText('pm.project.calendar.newcal.export'), msgExport1], 0);
                            } else {
                                msgBoxShow(154, 1);
                            }
                        },
                        function(result){
                            msgBoxShow(154, 1);
                        });
                    }
            },{
                id: this.parentid + 'rssthiscalonly',
                text: WtfGlobal.getLocaleText('pm.project.calendar.rss'),
                iconCls: 'pwnd rssfeedicon',
                scope: this,
                handler: function(){
                    var s = this.getSelectionModel().getSelectedNode();
                    window.open(Wtf.pagebaseURL+"feed.rss?m=events&c="+s.id,'_blank');
                 }
            },
            '-',
            colorPicker
            ]
        });
        colorPicker.on('select', function(palette, selColor){
            var colorIndex = palette.colors.indexOf(selColor);
            var node = this.getSelectionModel().getSelectedNode();
            /*node.getUI().SetBackColor(colorIndex);*/
            this.fireEvent("changecolor",this,node,colorIndex);
        },this);
    },

    attachListeners: function(){
        this.on('checkchange', function(node, e){
            node.select();
            this.fireEvent("treecheckchange",this, node, e, true);
        },this);
        this.on('contextmenu', function(nodeobj,x,y){
            if (nodeobj != this.treeRoot) {
                this.getNodeById(nodeobj.id).select();
                if(this.contextMenu)
                    this.contextMenu.removeAll();
                this.makeContextMenu(nodeobj);

                this.contextMenu.showAt([x + 11, y]);
                var t=this.agendaStore.find("cid",nodeobj.id);
                var perm=1;
                if(t!=-1){
                    perm=this.agendaStore.getAt(t).data["permissionlevel"];
                }
                var calType = this.agendaStore.getAt(t).data["caltype"];
                var exportCal = this.agendaStore.getAt(t).data["exportCal"];
                var delBtn = Wtf.getCmp(this.parentid+'Delete');
                var edBtn = Wtf.getCmp(this.parentid+'Edit');
                var clBtn = Wtf.getCmp(this.parentid+'coloritem');
                var rssBtn = Wtf.getCmp(this.parentid+'rssthiscalonly');
                var expBtn = Wtf.getCmp(this.parentid+'exportthisonly');
                if(calType == 3 && !this.archived) {
                    // For holiday calendar
                    delBtn.enable();
                    edBtn.disable();
                    clBtn.enable();
                }
                else if((perm!="" && calType != 4) || this.archived) { // for archived projects
                    delBtn.disable();
                    edBtn.disable();
                    clBtn.disable();
                }
                else if(calType == 4 && perm!="") { //
                    delBtn.enable();
                    edBtn.enable();
                    clBtn.enable();
                    rssBtn.disable();
                    expBtn.disable();
                }
                else if(exportCal == 0) { 
                    expBtn.disable();
                }
                else{
                    delBtn.enable();
                    edBtn.enable();
                    clBtn.enable();
                    expBtn.enable();
                    rssBtn.enable();
                }
            }
        },this);

        this.on('click', function(node, e){
            var calback = this.getNodeById(node.id).ui;
            calback.getTextEl().style.background = "transparent none repeat scroll 0%";
            calback.getTextEl().style.color = "#000000";
        },this);
    },
    selectChanged : function() {
  //          this.calcontrol = Wtf.getCmp(this.parentid +'calctrl');
//            this.calcontrol.onDayViewClick();
            //var parentTab = Wtf.getCmp(this.parentTabId);
            //if(!this.calcontrol){
               /* this.calcontrol = new Wtf.cal.control({
                        id: this.parentid +'calctrl',
                        title:WtfGlobal.getLocaleText('pm.calender.mycalender'),
                        tabType:Wtf.etype.cal,
                        iconCls:getTabIconCls(Wtf.etype.cal),
                        closable: true,
                        border: false,
                        ownerid: {type:0,userid:loginid},
                        myToolbar: true,
                        calTabId: 'tabmycal',
                        layout: "fit",
                        url: Wtf.calReq.cal + "caltree.jsp",
                        calTree:this,
                        calendar:this.datePicker,
                        mainCal:true

                    });
                    parentTab.add(this.calcontrol);
                    parentTab.doLayout();
                    parentTab.setActiveTab(this.calcontrol);
                    this.calcontrol.on("destroy",function(obj){
                        var chkNode = obj.calTree.getChecked();
                        for(var i=0;i<chkNode.length;i++){
                            chkNode[i].attributes.request = false;
                        }
                        obj.calTree.getSelectionModel().clearSelections();
                        obj.calTree.calcontrol = null;

                    });*/
          //  }
            //else{
                //parentTab.setActiveTab(this.calcontrol);
            //}

    },
    DateSelected : function() {
        if(!this.calcontrol)
            this.selectChanged();
        else {
            var parentTab = Wtf.getCmp(this.parentTabId);
            parentTab.setActiveTab(this.calcontrol);
        }
    }
 });
 /*//FILE:=============================createcal.js==========================*/

 Wtf.cal.createCal=function(MainPanel){
    this.parent = MainPanel;
    this.selNodeId=null;
    this.colorInd="";
    this.calRec = Wtf.data.Record.create([
    {name: 'cid'},
    {name: 'cname'},
    {name: 'description'},
    {name: 'location'},
    {name: 'timezone'},
    {name: 'colorcode'},
    {name: 'caltype'},
    {name: 'isdefault'},
    {name: 'userid'},
    {name: 'timestamp'},
    {name: 'url'},
    {name: 'interval'}
]);

 Wtf.cal.createCal.superclass.constructor.call(this);
};
Wtf.extend(Wtf.cal.createCal,Wtf.Component,{

createcal : function(e, calType){
    var parentId = this.parent.id;
    if(!(Wtf.getCmp(parentId+'inner1').form.isValid())){
       calMsgBoxShow([WtfGlobal.getLocaleText('pm.common.invalidinput'), WtfGlobal.getLocaleText('pm.msg.80')], 1);
       return;
    }
    var Grid  = Wtf.getCmp(parentId+'gridpanel');
    var ds = Grid.getStore();
    var permissionString="";

    for(var i=0;i<ds.getCount();i++){
        var recdata = ds.getAt(i).data;
        permissionString += recdata.userid;
        permissionString += "_"+recdata.resourcename+",";
    }
    var name = Wtf.get(parentId+'calname').getValue();
//    name = Wtf.cal.utils.HTMLScriptStripper(name);
    name = WtfGlobal.HTMLStripper(name);
    var description = Wtf.get(parentId+'des').getValue();
//    description = Wtf.cal.utils.HTMLScriptStripper(description);
    description = WtfGlobal.HTMLStripper(description);
    var type = this.parent.calTree.ownerid.type;
    var TZ = Wtf.getCmp(parentId+'timezone').value;
    var timezone = (TZ== WtfGlobal.getLocaleText('pm.common.timezone.select'))
                  ?""
                  :TZ;
    var CNT = Wtf.getCmp(parentId+'country').value;
    var country = (CNT== WtfGlobal.getLocaleText('pm.common.country.emptytext'))
                  ?""
                  :CNT;
    var interval = '1h';
    var url = "";
    if(calType == 'ical'){
        url = Wtf.getCmp(parentId+'calurl').getValue();
        interval = Wtf.getCmp(parentId+'icaltime').getValue();
        type = 4;
    }
    if(this.colorInd==""){
        this.colorInd=0;
    }

    if(name == "" || timezone == "" || country == "" || (calType == 'ical' && interval == ""))
        Wtf.MessageBox.alert(WtfGlobal.getLocaleText('pm.common.entrymissing'), WtfGlobal.getLocaleText('pm.msg.mandatory.text'));
//    if(name == "")
//        Wtf.MessageBox.alert('Invalid Entry','Please enter the calendar name!');
    else {
        var exportCal = true;
        if(calType == 'cal'){
            exportCal = Wtf.getCmp(this.parent.id+'exportcal').getValue();
        } else {
            exportCal = false;
        }
        if(!this.selNodeId){
            var cRecord = [name, description, country, timezone, this.colorInd, type, "0", this.parent.calTree.ownerid.userid, permissionString, exportCal, url, interval];
            this.parent.insertCalendar(cRecord, calType);
        }
        else{
            var isdefault="0";
            var t=this.parent.calTree.agendaStore.find("cid",this.selNodeId);
            if(t!=-1)
                isdefault=this.parent.calTree.agendaStore.getAt(t).data["isdefault"];

            cRecord = [this.selNodeId, name, description, country, timezone, this.colorInd, type, isdefault, this.parent.calTree.ownerid.userid, permissionString, exportCal, url, interval];
            this.parent.updateCalendar(cRecord);
        }
        this.parent.addCalendarTab1();
        this.selNodeId=null;
    }
},
CreateCalendar : function(nodeId, type){
    this.parent.add(new Wtf.Panel({
        frame:true,
        layout: "fit",
        deferredRender:true ,
        id: this.parent.id+'createCalForm',
        border:false,
        items: [{
            bodyStyle: "position: relative;",
            autoScroll:true,
            id: this.parent.id+'innerpanel'
        }]
    }));

    this.Addfield(type);
    this.Addbuttons(type);
    this.clearCalFormFields();
    if(nodeId)
        this.showSettingsForm(nodeId, type);
    Wtf.getCmp(this.parent.id+'calname').clearInvalid();
},
clearCalFormFields : function(){
    this.parent.formview ='CreateCal';
    var parentId = this.parent.id;
    Wtf.getCmp(parentId+'calname').setValue("");
    Wtf.getCmp(parentId+'des').setValue("");
//    Wtf.getCmp(parentId+'selectColor').select("CC3333");
//    Wtf.getCmp(parentId+'person').setValue("< Select the user name >");
//    Wtf.getCmp(parentId+'timezone').setValue(Wtf.timezoneName);
//    Wtf.getCmp(parentId+'country').setValue(Wtf.countryName);
    var Grid =  Wtf.getCmp(parentId+'gridpanel');
    Grid.getStore().removeAll();
    Wtf.getCmp(parentId+'create').setText(WtfGlobal.getLocaleText('pm.project.calendar.newcal.create'));
    Wtf.getCmp(parentId+'exportcal').setValue('true');
},


showSettingsForm : function(nodeId, type){
    this.parent.formview ='CreateCal';
    if(nodeId){
        this.selNodeId=nodeId;
        if(this.parent.calTree.agendaStore){
            var rec= new this.calRec();
            var t=this.parent.calTree.agendaStore.find("cid",this.selNodeId);
            if(t!=-1){
                var recData=this.parent.calTree.agendaStore.getAt(t).data;
                var cRecord = [recData.cid, recData.cname, recData.description, recData.location, recData.timezone, recData.colorcode, recData.caltype, recData.isdefault, recData.userid, recData.timestamp, recData.exportCal, recData.url, recData.interval];
                this.displaySettingValues(cRecord, type);

/*                var calPerm=rec.data["permissionlevel"];
                if(calPerm!=""||calPerm>1){
                    Wtf.getCmp(MainPanel.id+'sharingfield').hide();
                }
                else{
                    Wtf.getCmp(MainPanel.id+'sharingfield').show();
                }                */
            }
        }
    }
},

createCalendarForm: function(nodeId, type){
    var x = Wtf.getCmp(this.parent.id+'inner1');
    if(x){
        Wtf.getCmp(this.parent.id+'innerpanel').remove(x, true);
        this.Addfield(type);
        this.Addbuttons(type);
        this.clearCalFormFields();
        if(nodeId)
            this.showSettingsForm(nodeId, type);
        Wtf.getCmp(this.parent.id+'calname').clearInvalid();
    }
},

Addbuttons : function(type){
    Wtf.getCmp(this.parent.id+"inner1").add(new Wtf.Panel({
        id:this.parent.id+'addbuttons',
        items:[
            {
                xtype : "button",
                cls:'button1',
                text:WtfGlobal.getLocaleText('lang.cancel.text'),
                id:this.parent.id+'cancel',
                scope:this.parent,
                handler:function(){
//                    clearCalFormFields(this);
                    this.addCalendarTab1();
                    this.selNodeId=null;
                }
            },
            {
                xtype : "button",
                cls:'button11',
                text:WtfGlobal.getLocaleText('pm.project.calendar.newcal.create'),
                id:this.parent.id+'create',
                handler: function(btn){
                    this.createcal(btn, type);
                },
                scope:this
            }
        ]
    }));
    this.parent.doLayout();
},


Addfield : function(type){
   /* var storet = new Wtf.data.SimpleStore({
        fields: ['id', 'name'],
        data : Wtf.form.ComboBox.timezone
    });	*/
    var stores = new Wtf.data.SimpleStore({
        fields: ['abbr', 'state'],
        data : Wtf.form.ComboBox.sharing
    });
    var syncFreq = new Wtf.data.SimpleStore({
        fields: ['value', 'displayValue'],
        data : [
            [ 5, '5 Minutes'],
            [ 15, '15 Minutes'],
            [ 60, '1 Hour'],
            [ 240, '4 Hours'],
            [ 720, '12 Hours'],
            [ 1440, '1 Day']
        ]
    });
    if(!Wtf.StoreMgr.containsKey("timezone")){
        Wtf.timezoneStore.load();
        Wtf.timezoneStore.on("load", function() {
            Wtf.StoreMgr.add("timezone", Wtf.timezoneStore);
        });
    }

    if(!Wtf.StoreMgr.containsKey("country")){
        Wtf.countryStore.load();
        Wtf.countryStore.on("load", function() {
            Wtf.StoreMgr.add("country", Wtf.countryStore);
        });
    }

//    Wtf.Ajax.request({
//        url: '../../jspfiles/cal/caltree.jsp',
//        params:{
//            action:7
//        },
//        method:'POST',
//        scope:this,
//        success: function(result, req){
//            var obj = Wtf.decode(result.responseText);
//            Wtf.getCmp(this.parent.id+'timezone').setValue(obj.timezone);
//            Wtf.getCmp(this.parent.id+'country').setValue(obj.country);//
//        },
//        failure:function(result, req){
//
//        }
//   });

    var authorityStore = new Wtf.data.SimpleStore({
        fields:['abbr','resourcename'],
        data:Wtf.form.ComboBox.resourcename
    });

    Wtf.ux.comboBoxRenderer = function(combo) {
        return function(value) {
            var idx = combo.store.find(combo.valueField, value);
            var rec = combo.store.getAt(idx);
            return rec.get(combo.displayField);
        };
    }

    var combo = new Wtf.form.ComboBox({
        selectOnFocus:true,
//        typeAhead: true,
        triggerAction: 'all',
        editable: false,
        mode: 'local',
        store: authorityStore,
        displayField: 'resourcename',
        valueField:'abbr'
    });

    var id = this.parent.id;
    var mailgrid = new Wtf.grid.EditorGridPanel({
        id:id+'gridpanel',
        clicksToEdit:1,
        selModel: new Wtf.grid.RowSelectionModel(),
        ds: new Wtf.data.Store({
            id: id + "datastore",
            reader: new Wtf.data.ArrayReader({},[{
                name: 'Email'
            },{
                name:'userid'
            },{
                name:'resourcename'
            },{
                name:'setbutton'
            }])
        }),
        cm:new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header: WtfGlobal.getLocaleText('lang.name.text'),
                dataIndex:'fullname'
            },{
                header: WtfGlobal.getLocaleText('pm.updateprofile.emailaddreass'),
                dataIndex:'Email'
            },{
                header: WtfGlobal.getLocaleText('lang.permission.text'),
                dataIndex: 'resourcename',
                editor: combo,
                renderer:Wtf.ux.comboBoxRenderer(combo)
            },{
                header: WtfGlobal.getLocaleText('pm.common.userId'),
                dataIndex:'userid',
                hidden:true
            },{
                header: WtfGlobal.getLocaleText('lang.delete.text'),
                renderer:this.setbutton,
                dataIndex:'setbutton',
                width:20
            }
        ]),
        viewConfig:{
            forceFit:true,
            autoFill:true
        },
        height: 95
    });
    //var contacts=Wtf.getCmp('contactsview');

    Wtf.getCmp(id+'innerpanel').add(new Wtf.FormPanel({
        id: id+'inner1',
        labelWidth: 130,
        layout:'form',
        border: false,
        items:[{
                html: "<span style=\"float:right; margin-right:30px;\">"+WtfGlobal.getLocaleText('pm.common.requiredfields')+"</span>"
           },{
            xtype: 'fieldset',
            title:WtfGlobal.getLocaleText('lang.details.text'),
            border: false,
            border: false,
            autoHeight: true,
            items:[{
                        fieldLabel: WtfGlobal.getLocaleText('pm.project.calendar.newical.url')+'*',
                        allowBlank: true,
                        xtype: 'textfield',
                        id : id+'calurl',
                        width:'98%',
                        tabIndex:1,
                        anchor:'71%',
                        hidden: type == 'ical' ? false : true,
                        itemCls: type == 'ical' ? '' : 'hideFormField',
                        hideLabel: type == 'ical' ? false : true
                   },{
                        fieldLabel: WtfGlobal.getLocaleText('pm.project.calendar.newcal.calname')+'*',
                        allowBlank: false,
                        maxLength:50,
                        xtype: 'textfield',
                        id : id+'calname',
                        width:'98%',
                        tabIndex:1,
                        anchor:'71%'
                   },{
                        xtype: 'textarea',
                        fieldLabel: WtfGlobal.getLocaleText('lang.description.text'),
                        id:id+'des',
                        width:'98%',
                        tabIndex:1,
                        anchor:'71%'
                   },{
                        xtype : 'combo',
                        tabIndex:1,
                        fieldLabel: WtfGlobal.getLocaleText('lang.country.text')+'*',
                        allowBlank: false,
                        id:id+'country',
                        store:Wtf.countryStore,
                        displayField:'name',
                        emptyText:WtfGlobal.getLocaleText('pm.common.country.emptytext'),
                        valueField:'id',
                        forceSelection : true,
                        typeAhead: true,
                        mode: 'local',
                        triggerAction: 'all',
                        selectOnFocus:true,
                        anchor:'71%',
                        hidden: type == 'ical' ? true : false,
                        itemCls: type == 'ical' ? 'hideFormField' : '',
                        hideLabel: type == 'ical' ? true : false
                   },{
                        xtype : 'combo',
                        tabIndex:1,
                        allowBlank: false,
                        fieldLabel: WtfGlobal.getLocaleText('lang.timezone.tex')+'*',
                        id:id+'timezone',
                        emptyText:WtfGlobal.getLocaleText('pm.common.timezone.select'),
                        store: Wtf.timezoneStore,
                        displayField:'name',
                        maxHeight:200,
                        valueField:'id',
                        forceSelection : true,
                        typeAhead : true,
                        mode: 'local',
                        triggerAction: 'all',
                        selectOnFocus:true,
                        anchor:'71%',
                        hidden: type == 'ical' ? true : false,
                        itemCls: type == 'ical' ? 'hideFormField' : '',
                        hideLabel: type == 'ical' ? true : false
                   },{
                        xtype : 'panel',
                        layout:'column',
                        border: false,
                        items:[{
                                layout:'form',
                                width:135,
                                border: false,
                                items:{
                                    labelWidth: 130,
                                    fieldLabel:WtfGlobal.getLocaleText('lang.color.text'),
                                    id:"textField",
                                    hideField:true,
                                    xtype: 'textfield'
                                }
                               },{
                                border: false,
                                width:150,
                                items:[
                                        new Wtf.ColorPalette({
                                            cls:'palette',
                                            id : id+'selectColor',
                                            colors: ["CC3333", "DD4477", "994499", "6633CC", "336699", "3366CC", "22AA99", "329262", "109618", "66AA00", "AAAA11", "D6AE00", "EE8800", "DD5511", "A87070", "8C6D8C", "627487", "7083A8", "5C8D87", "898951", "B08B59"]
                                    })]
                            }]
                   },{
                        fieldLabel: WtfGlobal.getLocaleText('pm.project.calendar.newcal.export'),
                        allowBlank: false,
                        maxLength:50,
                        xtype: 'checkbox',
                        checked: true,
                        fieldClass: 'calChkbox',
                        id : id+'exportcal',
                        tabIndex:1,
                        hidden: type == 'ical' ? true : false,
                        itemCls: type == 'ical' ? 'hideFormField' : '',
                        hideLabel: type == 'ical' ? true : false
                   },{
                        xtype: 'combo',
                        tabIndex: 1,
                        fieldLabel: WtfGlobal.getLocaleText('pm.project.calendar.newical.syncdur')+'*',
                        allowBlank: false,
                        id: id+'icaltime',
                        store: syncFreq,
                        displayField: 'displayValue',
                        emptyText: WtfGlobal.getLocaleText('pm.common.syncfrequently'),
                        valueField: 'value',
                        forceSelection: true,
                        typeAhead: true,
                        mode: 'local',
                        triggerAction: 'all',
                        selectOnFocus: true,
                        anchor: '71%',
                        hidden: type == 'ical' ? false : true,
                        itemCls: type == 'ical' ? '' : 'hideFormField',
                        hideLabel: type == 'ical' ? false : true
                   }
                ]}
       /* ,{
            xtype: 'fieldset',
            title:WtfGlobal.getLocaleText('pm.common.sharing'),
            height:170,
            id:this.parent.id+'sharingfield',
            items:[{
                id:this.parent.id+'pqrs',
                layout: 'column',
                items: [{
                    columnWidth:0.715,
                    labelWidth:130,
                    layout:'form',
                    items:[{
                            xtype : 'combo',
                            editable:false,
                            fieldLabel: WtfGlobal.getLocaleText('lang.username.text'),
                            emptyText:'< Select the user name >',
                            id:this.parent.id+'person',
                            store: calContacts_Store,
                            displayField:'fullname',
                            valueField:'userid',
                            mode: 'local',
                            triggerAction: 'all',
                            selectOnFocus:true,
                            anchor:'100%'
                      }]
                },{
                    columnWidth:0.1,
                    layout:'form',
                    items:[new Wtf.Template(
                        '<table id='+this.parent.id+'addedperson><tr>',
                        '<td><img  id='+this.parent.id+'addperson src="../../images/tabicons_02.gif" title="ADD PERSON" class = "addpersonbutton""></img></td></tr></table>'
                    )]
                }]
            },{
                border: false,
                id: this.parent.id + 'mailGridPanel',
                height:110,
                layout:'border',
                items:[{
                    height:100,
                    region:'center',
                    layout:'fit',
                    items:mailgrid}]
            }]
        }*/
        ]
    }));
//    if(this.parent.calTree.ownerid.type==2){
//        Wtf.getCmp(this.parent.id+'sharingfield').hide();
//    }
    this.parent.doLayout();
    Wtf.getCmp(id+'gridpanel').on('afteredit', this.remGridClass, this);
//    Wtf.get(id+'addperson').on("click",this.addshareperson,this);
    Wtf.get('textField').hide();
    var colors = Wtf.getCmp(id+'selectColor');
    colors.on('select', function(palette, selColor){
        this.colorInd = palette.colors.indexOf(selColor);
    },this);
    var calName = Wtf.getCmp(id+'calname');
    calName.on("change", function(){
        calName.setValue(WtfGlobal.HTMLStripper(calName.getValue()));
    },this);
    var urlField = Wtf.getCmp(id+'calurl');
    Wtf.getCmp(id+'icaltime').setValue(60);
    colors.select(colors.colors[Math.floor(Math.random() * colors.colors.length)]);
    if(type == 'ical'){
        Wtf.timezoneStore.on('load', function(store, records){
            Wtf.getCmp(id+'timezone').setValue(records[0].get('id'));
        }, this, {single: true});
        Wtf.countryStore.on('load', function(store, records){
            Wtf.getCmp(id+'country').setValue(records[0].get('id'));
        }, this, {single: true});
        urlField.allowBlank = false;
        urlField.validator = Wtf.form.VTypes.url;
        urlField.invalidText = WtfGlobal.getLocaleText('pm.project.calendar.newical.url.invalidtext');
        focusOn(id+'calurl');
    } else {
        urlField.setValue("");
        focusOn(id+'calname');
    }
//    Wtf.getCmp(this.parent.id+'country').on("select",function(a,b,c){
//        this.countryRec = Wtf.countryStore.getAt(c);
//        var index = Wtf.timezoneStore.findBy(function(record,id){
//            var cntri = record.get('name').split(' ')[1];
//            if(cntri && this.countryRec.data.name.indexOf(cntri) !== -1){
//                return true;
//            }
//        }, this);
//        if(index !== -1)
//            Wtf.getCmp(this.parent.id+'timezone').setValue(Wtf.timezoneStore.getAt(index).get('id'));
//    }, this)
},

remGridClass:function(e){
    Wtf.get(Wtf.getCmp(this.parent.id+'gridpanel').getView().getCell(e.row, e.column)).removeClass('x-grid3-dirty-cell');
},
setbutton : function(){
    return("<img src='../../images/Delete.gif'  id=_delbutton  title='Delete Person' class='xbtn'></img>");
},


/*
addshareperson : function(){
    var mailGridData = Wtf.getCmp(this.parent.id+"gridpanel").store;
    var validRegExp = /^[^@]+@[^@]+.[a-z]{2,}$/i;
    var strid = Wtf.getCmp(this.parent.id+'person').getValue();
    var fullname = Wtf.get(this.parent.id+'person').getValue();
        if(fullname == '< Select the user name >'){
            msgBoxShow(89, 1);
        return 0;
    }
    //var contacts=Wtf.getCmp('contactsview');
    var t=calContacts_Store.find("userid",strid);
    var rec=calContacts_Store.getAt(t);
    if(rec){
        var strEmail = rec.data["emailid"];
       // search email text for regular exp matches
        if (strEmail.search(validRegExp) == -1) {
            msgBoxShow(90, 1);
            return 0;
        }
        var TopicRecord = Wtf.data.Record.create(
            {name: 'fullname'},
            {name: 'Email'},
            {name: 'userid'},
            {name: 'resourcename'},
            {name: 'setbutton'}
        );
        var tr = new TopicRecord({
            fullname:fullname,
            Email:strEmail,
            userid:strid,
            resourcename:1,
            setbutton:''
        });

        strEmail = this.checkRepeatMail(strEmail);
        if(strEmail==null){
            mailGridData.add(tr);
         }
         Wtf.get('_delbutton').on("click",this.deleteClick,this);
     }
},*/

checkRepeatMail : function(strEmail){
    var mailGridData = Wtf.getCmp(this.parent.id+"gridpanel").store;
    for(var i=0;i<mailGridData.getCount();i++){
        if(strEmail == mailGridData.getAt(i).data['Email']){
            msgBoxShow(117, 1);
            //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Email Address is already present');
            return(strEmail);
        }
    }
    strEmail = null;
    return(strEmail);
},

deleteClick : function(e) {

    var Grid = Wtf.getCmp(this.parent.id+"gridpanel");
    var rowselectmodel = Grid.getSelectionModel();
    var rowselect = rowselectmodel.getSelections();
    var mailGridData = Grid.getStore();
    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.341'),function(btn){
        if(btn == 'yes'){
            mailGridData.remove(rowselect[0]);
        }
    });
},

displaySettingValues : function(rec, type){
    var parentId = this.parent.id;
    Wtf.getCmp(parentId+'create').setText(WtfGlobal.getLocaleText('pm.project.calendar.newcal.edit'));
    Wtf.getCmp(parentId+'calname').setValue(rec[1]);
    Wtf.getCmp(parentId+'des').setValue(rec[2]);

    if(type != 'ical'){
        if(!Wtf.StoreMgr.containsKey("country")) {
            Wtf.countryStore.on("load", function() {
                Wtf.getCmp(parentId+'country').setValue(rec[3]);
            });

        } else {
            Wtf.getCmp(parentId + "country").setValue(rec[3]);
        }

        if(!Wtf.StoreMgr.containsKey("timezone")) {
            Wtf.timezoneStore.on("load", function() {
                Wtf.getCmp(parentId+'timezone').setValue(rec[4]);
            });

        } else {
            Wtf.getCmp(parentId + "timezone").setValue(rec[4]);
        }
    }

    var colorfield = ["CC3333", "DD4477", "994499", "6633CC", "336699", "3366CC", "22AA99", "329262", "109618", "66AA00", "AAAA11", "D6AE00", "EE8800", "DD5511", "A87070", "8C6D8C", "627487", "7083A8", "5C8D87", "898951", "B08B59"];
    Wtf.getCmp(parentId+'selectColor').select(colorfield[rec[5]]);
    if(rec[10])
        Wtf.getCmp(parentId+'exportcal').setValue('true');
    else
        Wtf.getCmp(parentId+'exportcal').setValue('false');
    if(type == 'ical'){
        if(rec[11])
            Wtf.getCmp(parentId+'calurl').setValue(rec[11]);
        Wtf.getCmp(parentId+'calurl').disable();
        if(rec[12])
            Wtf.getCmp(parentId+'icaltime').setValue(rec[12]);
    }
    /*   Wtf.Ajax.requestEx({
            method: 'GET',
            url:  Wtf.calReq.cal + 'caltree.jsp',
            params: ({
            action: 4,
            cid: this.selNodeId,
            caltype:this.parent.calTree.ownerid.type
        })},
            this,
            function(result, req){
            var nodeobj = eval("(" + result + ")");
            var Grid =  Wtf.getCmp(this.parent.id+'gridpanel');
            var mailStore = Grid.getStore();
            var mailRecord = Wtf.data.Record.create([{name: 'fullname'},{name: 'Email'},{name: 'userid'},{name: 'resourcename'}, {name: 'setbutton'}]);
           // var contacts=Wtf.getCmp('contactsview');
            for(var i=0;i<nodeobj.data.length;i++){
                var contactCheck = calContacts_Store.find("userid",nodeobj.data[i].userid);
                if(contactCheck!=-1){
                    var _data = calContacts_Store.getAt(contactCheck).data;
                    var p = new mailRecord({
                        fullname: _data.fullname,
                        Email : _data.emailid,
                        userid: _data.userid,
                        resourcename:nodeobj.data[i].permissionlevel,
                        setbutton:''
                    })
                    mailStore.add(p);
                }
                if(Wtf.get('_delbutton')) {
                     Wtf.get('_delbutton').on("click",this.deleteClick,this);
                }

            }
            },
            function(result, req){
                msgBoxShow(4, 1);
               //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occurred while connecting to the server');

        });*/
//    Wtf.Ajax.request({
//        url: Wtf.calReq.cal + 'caltree.jsp',
//        method: 'GET',
//        params: ({
//            action: 4,
//            cid: this.selNodeId,
//            caltype:this.parent.calTree.ownerid.type
//        }),
//        scope: this,
//        success: function(result, req){
//            var nodeobj = eval("(" + result.responseText.trim() + ")");
//            var Grid =  Wtf.getCmp(this.parent.id+'gridpanel');
//            var mailStore = Grid.getStore();
//            var mailRecord = Wtf.data.Record.create([{name: 'fullname'},{name: 'Email'},{name: 'userid'},{name: 'resourcename'}, {name: 'setbutton'}]);
//           // var contacts=Wtf.getCmp('contactsview');
//            for(var i=0;i<nodeobj.data.length;i++){
//                var contactCheck = calContacts_Store.find("userid",nodeobj.data[i].userid);
//                if(contactCheck!=-1){
//                    var p = new mailRecord({
//                        fullname: calContacts_Store.getAt(contactCheck).get("fullname"),
//                        Email : calContacts_Store.getAt(contactCheck).get("emailid"),
//                        userid: calContacts_Store.getAt(contactCheck).get("userid"),
//                        resourcename:nodeobj.data[i].permissionlevel,
//                        setbutton:''
//                    })
//                    mailStore.add(p);
//                }
//                if(Wtf.get('_delbutton')) {
//                     Wtf.get('_delbutton').on("click",this.deleteClick,this);
//                }
//
//            }
//        },
//        failure: function(){
//            Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occurred while connecting to the server');
//        }
//    });
}
})/*================================createcal.js================================*/
