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
var ContactsTemplate = new Wtf.Template('{username}')
/*********************** Contact Tree ***********************/
Wtf.ContactsTree = function(config){
    this.nodeHash = {};
    //this.eventSetFlag = false;
    this.eventSetFlag = new Array();
    this.contactDetails = Wtf.data.Record.create([{
        name: 'userid'
    },{
        name: 'username'
    },{
        name: 'emailid'
    },{
        name: 'fullname'
    },{
        name: 'userstatus'
    },{
        name: 'messagetext'
    },{
        name: 'image'
    },{
        name: 'relationid'
    }]);
    this.contactReader = new Wtf.data.KwlJsonReader({
        root: "data"
    }, this.contactDetails);
    this.contactStore = new Wtf.data.Store({
        proxy: new Wtf.data.HttpProxy({
            url: Wtf.req.prt + "getFriendListDetails.jsp"
        }),
        baseParams :{
            mode : '1'
        },
        reader: this.contactReader,
        sortInfo: {field:'fullname', direction: 'ASC'}
    });

    Wtf.ContactsTree.superclass.constructor.call(this, config);

    this.contactStore.on("load", this.handleContacts, this);
};

Wtf.extend(Wtf.ContactsTree, Wtf.tree.TreePanel, {
    width: 250,
    height: '100%',
    rootVisible: false,
    id: 'contactsview',
    autoScroll: true,
    animate: Wtf.enableFx,
    enableDD: false,
    contacts: null,
    hlDrop: Wtf.enableFx,
    temptreenode: null,
    clientwinarr: new Wtf.util.MixedCollection(),

    setEvents: function(){
        this.getNodeById('contacts').getUI().getTextEl().setAttribute("pmnode", WtfGlobal.getLocaleText('pm.personalmessages.notclickable'));
    },

    handleContacts: function(obj, rec, opt){
        hideMainLoadMask();
        var myContacts = "";
        for (var j = 0; j < obj.getCount(); j++) {
            var recData =  rec[j].data;
            var userid = recData.userid;
            var usernametext = recData.username;
            var userstatus = recData.userstatus;
            var iconimg = '../../images/Offline.png';
            if (userstatus == "online") {
                iconimg = '../../images/Online.png';
                this.eventSetFlag[usernametext]= false;
            } else if (userstatus == "offline") {
                iconimg = '../../images/Offline.png';
            }
            this.createContactNode(userid,usernametext,userstatus);
            myContacts += userid +",";
            var _textEL = this.temptreenode.getUI().getTextEl();
            _textEL.setAttribute("status", "f");
            _textEL.setAttribute("ustat", userstatus);
            _textEL.setAttribute("mstat", "f");
            this.temptreenode = null;
        }
        if(myContacts.length>0) {
            this.publishAction(myContacts.substr(0,myContacts.length-1),'3');
        }
    },

    addExistingFolders: function(){
        this.contactStore.load({
            params: {
                login: loginid
            }
        });
    },

    initComponent: function(){
        Wtf.ContactsTree.superclass.initComponent.call(this);
        var root1 = new Wtf.tree.AsyncTreeNode({
            text: '',
            expanded: true
        });
        this.setRootNode(root1);
        this.contacts = new Wtf.tree.TreeNode({
            text: WtfGlobal.getLocaleText('pm.contacts.text'),
            allowDrag: false,
            id: 'contacts',
            iconCls: 'dpwnd chaticon',
            singleClickExpand: false,
            status:'userstatus'
        });
        root1.appendChild(this.contacts);
    },

    afterRender: function(){
        Wtf.ContactsTree.superclass.afterRender.call(this);
        this.setEvents();
        this.addExistingFolders();
    },

    handleClick: function(userid,username){
        var chatwin = Wtf.getCmp('chatWin'+userid);     //check if Chat-window is already present
        insertIntoChatwins(userid);
        var time = getUserTime();
        if(!chatwin){                                   //if not make an object of it,
            chatwin = new Wtf.common.WtfGChatWindow({
                renderTo : "chatFrame-zIndexed",        //I-frame used for rendering Chat Window
                id: "chatWin" + userid,                 //Id of called User given to Chat-window
                chatWith : username,                    //Name to called chat-buddy used for displaying as Chat-window Header and Status Message
                status : 1,                             //Called Chat-buddy's status set online
                userid : userid                         //Called Chat-buddy's userid used for giving id to various components of Chat-window
            });
            if(!this.eventSetFlag[username]){     //change made for the multiple user chat(kamlesh)
                chatwin.on('msgentered',function(window,msg,towhom){
                    Wtf.Ajax.requestEx({
                        url: "jspfiles/chatmessage.jsp",
                        params: {
                            type : 0,                       //
                            mode : 'msg',                   //
                            user: fullname,                 //to show sender's name in Chat-window of Remote user
                            rUserId: userid,                //to publish into channel with this id
                            rUserName: towhom,              //TBR
                            rstatus: 'online',              //TBR
                            chatMessage: msg                //Chat-Message to be sent
                        }
                    },this,
                    function(result, req){});
                },this);
                this.eventSetFlag[username] = true;
            }
            setStatus(1,userid);
            chatwin.on('closeclicked',function(userid){
                Wtf.getCmp('chatWin'+userid).hide();
                deleteFromChatwins(userid);
            },this);
        }
        chatwin.show();
        var frame = Wtf.get('chatFrame-zIndexed');
        if(Wtf.isWebKit && frame){
            frame.show();
            frame.hide();
        }
    },

    chatPublishHandler: function(msg){
        var temp = eval('(' + msg.data.data + ')');
        var temp1 = Wtf.decode(temp.data[0]).data;
        var time = getUserTime();
        if(temp1[0].timestamp)
                time = Date.parseDate(temp1[0].timestamp, "Y-m-d H:i:s").format('h:i A');
        if(temp1[0].mode != "msg") {
            var obj = Wtf.getCmp(this.id).getNodeById('kcont_' + temp1[1].userid);
            if(temp1[0].mode == "online") {
                if(obj.text.indexOf('[') < 0) {
                    obj.ui.iconNode.src="../../images/Online.png";
                    obj.getUI().getTextEl().setAttribute("ustat", "online");
                    obj.attributes.status = "online";
                    obj.attributes.icon = '../../images/Online.png';
                    if(Wtf.getCmp("chatWin" + temp1[1].userid)){
                        setStatus(1,temp1[1].userid);
                    }
                }
                if(temp1[1].status == "request") {
                    this.publishAction(temp1[1].userid,'2');
                }
            } else if(temp1[0].mode == "offline") {
                obj.ui.iconNode.src="../../images/Offline.png";
                obj.attributes.status = "offline";
                obj.attributes.icon = '../../images/Offline.png';
                if(Wtf.getCmp("chatWin" + temp1[1].userid)){
                    setStatus(0,temp1[1].userid);
                }
            } else if(temp1[0].mode == "delete") {
                obj = Wtf.getCmp(this.id).getNodeById('kcont_' + temp1[1].userid);
                obj.parentNode.removeChild(obj);
                if(Wtf.getCmp('_mycontactStore')) {
                    Wtf.getCmp('_mycontactStore').getStore().load({
                        params : {
                            start : 0,
                            limit:15
                        }
                    });
                    Wtf.getCmp('_newcontactStore').getStore().load({
                        params : {
                            start : 0,
                            limit:15
                        }
                    });
                }
                if(Wtf.getCmp("chatWin" + temp1[1].userid)){
                    Wtf.getCmp("chatWin" + temp1[1].userid).close();
                    deleteFromChatwins(userid);
                }
            } else if(temp1[0].mode == "add") {
                obj = Wtf.getCmp(this.id).getNodeById('kcont_' + temp1[1].userid);
                if(obj) {
                    obj.parentNode.removeChild(obj);
                }
                this.createContactNode(temp1[1].userid,temp1[1].username,'offline');
                this.temptreenode =null;
                this.publishAction(temp1[1].userid,'3'); // publish my active status
                if(Wtf.getCmp('_mycontactStore')) {
                    Wtf.getCmp('_mycontactStore').getStore().load({
                        params : {
                            start : 0,
                            limit:15
                        }
                    });
                    Wtf.getCmp('_newcontactStore').getStore().load({
                        params : {
                            start : 0,
                            limit:15
                        }
                    });
                }
                bHasChanged = true;
                var t = refreshDash.join();
                if(t.indexOf("all") == -1 && (t.indexOf("pmg") == -1 || t.indexOf("req") == -1)){
                    refreshDash[refreshDash.length] = 'pmg';
                    refreshDash[refreshDash.length] = 'req';
                }
            } else if(temp1[0].mode == "invite") {
                obj = Wtf.getCmp(this.id).getNodeById('kcont_' + temp1[1].userid);
                if(obj) {
                    obj.parentNode.removeChild(obj);
                }
                this.createContactNode(temp1[1].userid,temp1[1].username,'offline');
                this.temptreenode = null;
                if(Wtf.getCmp('_mycontactStore')) {
                    Wtf.getCmp('_mycontactStore').getStore().load({
                        params : {
                            start : 0,
                            limit:15
                        }
                    });
                    Wtf.getCmp('_newcontactStore').getStore().load({
                        params : {
                            start : 0,
                            limit:15
                        }
                    });
                }
            }
        } else {
            var chatpanel = Wtf.getCmp('chatWin'+temp1[0].id);
            insertIntoChatwins(temp1[0].id);
            if (!chatpanel) {
                chatpanel = new Wtf.common.WtfGChatWindow({
                    renderTo : "chatFrame-zIndexed",
                    id: "chatWin" + temp1[0].id,
                    chatWith : temp1[0].sname,
                    status : 1,
                    userid : temp1[0].id
                });
            }
            chatpanel.show();
            Wtf.get("chat_panel_header_tools_div" + temp1[0].id).addClass('newmsg');
            chatpanel.append(temp1[0].id,temp1[0].sname,temp1[0].message,time);
            //some code is deleted by kamlesh in order to solve the problem(multiple msg transmission)
            if(!this.eventSetFlag[temp1[0].sname]){
                chatpanel.on('msgentered',function(window,msg,towhom){
                    Wtf.Ajax.requestEx({
                        url: "jspfiles/chatmessage.jsp",
                        params: {
                            type : 0,
                            mode : 'msg',
                            user: fullname,
                            rUserId: temp1[0].id,
                            rUserName: towhom,
                            rstatus: 'online',
                            chatMessage: msg
                        }
                    },this,
                    function(result, req){});
                },this);
                this.eventSetFlag[temp1[0].sname] = true;
            }
            chatpanel.on('closeclicked',function(userid){
                Wtf.getCmp('chatWin'+userid).hide();
                deleteFromChatwins(userid);
            },this);
            var frame = Wtf.get('chatFrame-zIndexed');
            if(Wtf.isWebKit && frame){
                frame.show();
                frame.hide();
            }
        }
    },

    createContactNode : function(id,text,status) {
        this.temptreenode = new Wtf.tree.TreeNode({
            allowDrag: false,
            leaf: true,
            id: 'kcont_' + id,
            icon: '../../images/Offline.png',
            status:status
        });
        this.temptreenode.setText(ContactsTemplate.applyTemplate({
            username: text
        }));
        this.temptreenode.on('click', function(node, e){
         if(node.attributes.status == "online"){
             if(node.text.indexOf('[') < 0){
                var userid = String(node.id).replace("kcont_", "");
                var username = String(node.text);
                this.handleClick(userid, username);
             }
         }
        },this);
        var obj = this.getNodeById('kcont_' + id);
        if(obj) {
            obj.parentNode.removeChild(obj);
        }
        this.contacts.appendChild(this.temptreenode);
        this.contacts.expand();
    },

    publishAction : function(remoteuserid,mode) {
        Wtf.Ajax.requestEx({
            url: Wtf.req.prt + "getFriendListDetails.jsp",
            params: {
                userid: loginid,
                mode : mode,
                remoteUser: remoteuserid
            }
        }, this);
    }
});
/* End of contact tree  */

Wtf.myContactGrid = function(config){
    Wtf.apply(this,config);
    Wtf.myContactGrid.superclass.constructor.call(this,config);
    this.viewProfile = new Wtf.Toolbar.Button({
        text : WtfGlobal.getLocaleText('pm.contacts.viewprofile'),
        scope : this,
        id: "viewProfileButtoon"+this.id,
        disabled:true,
        tooltip: {
            text: WtfGlobal.getLocaleText('pm.Help.conviewprofileright')
        },
        iconCls: 'pwnd profile',
        handler : this.viewProfile
    });
    this.viewmyFriendProfile = new Wtf.Toolbar.Button({
        text : WtfGlobal.getLocaleText('pm.contacts.viewprofile'),
        scope : this,
        id: "viewOldProfileButtoon"+this.id,
        disabled:true,
        tooltip: {
            text: WtfGlobal.getLocaleText('pm.Help.conviewprofileleft')
        },
        iconCls: 'pwnd profile',
        handler : this.viewmyFriendProfile
    });
    this.Contact = new Wtf.Toolbar.Button({
        text : WtfGlobal.getLocaleText('pm.contacts.text'),
        scope : this,
        tooltip: {
            text: WtfGlobal.getLocaleText('pm.Help.contacts')
        },
        iconCls: 'dpwnd addcontact',
        menu: [
                this.addExtContact = new Wtf.Action({
                    text : WtfGlobal.getLocaleText('pm.contacts.addcontact'),
                    scope : this,
                    id: "addExtContact"+this.id,
                    tooltip: {
                        text: WtfGlobal.getLocaleText('pm.Help.conaddcontact')
                    },
                    iconCls: 'dpwnd addcontact',
                    handler : this.addExtContactHandler
                }),
                this.editExtContact = new Wtf.Action({
                    text : WtfGlobal.getLocaleText('pm.contacts.editcontact'),
                    scope : this,
                    disabled:true,
                    id: "EditExtContact"+this.id,
                    tooltip: {
                        text: WtfGlobal.getLocaleText('pm.Help.coneditcontact')
                    },
                    iconCls: 'pwnd editcontact',
                    handler : this.EditExtContactHandler
                })
            ]
    });
     this.ExpImp = new Wtf.Toolbar.Button({
        text : WtfGlobal.getLocaleText('pm.contacts.impexp'),
        scope : this,
        tooltip: {
            text: WtfGlobal.getLocaleText('pm.Help.conExpImp')
        },
        iconCls: 'dpwnd addcontact',
        menu: [
                this.ExportExtContact = new Wtf.Action({
                    text:WtfGlobal.getLocaleText('pm.common.export.csv'),
                    tooltip: {
                        text: WtfGlobal.getLocaleText('pm.Help.conexportcsv')
                    },
                    scope:this,
                    id: 'ExportContact'+this.id,
                    iconCls: 'pwnd contactexportcsv',
                    handler:function(){
                        this.ExportContacts('csv');
                    }
                }),
                this.ImportExtContact = new Wtf.Action({
                    text:WtfGlobal.getLocaleText('pm.common.import.csv'),
                    tooltip: {
                        text: WtfGlobal.getLocaleText('pm.Help.conimportcsv')
                    },
                    scope:this,
                    id: 'ImportContact'+this.id,
                    iconCls: 'pwnd contactimportcsv',
                    handler:function(){
                        this.ImportContacts('csv');
                    }
                })
            ]
     });
};

Wtf.extend(Wtf.myContactGrid, Wtf.Panel,{
    onRender : function(config){
        Wtf.myContactGrid.superclass.onRender.call(this,config);
        this.createContactStore();
        this.contactCM = new Wtf.grid.ColumnModel(this.createContactCM(true));
        this.createContactGrid();
        this.createNewContactStore();
        this.newContactCM = new Wtf.grid.ColumnModel(this.createNewContactCM(false));
        this.createNewContactGrid();
        this.assignmentUI();
        this.add(this.innerPanel);
        this.innerPanel.doLayout();
        this.mycontactStore.load({
            params : {
                start : 0,
                limit:15
            }
        });
        this.newContactStore.load({
            params : {
                start : 0,
                limit:15
            }
        });
        this.quickSearchNewContact.StorageChanged(this.newContactStore);
    },
    ExportContacts: function(type) {
        Wtf.get('downloadframe').dom.src = '../../exportimportcontacts.jsp?type='+type+'&do=export';
    },
    ImportContacts :function(type){
        
        this.ImportPanel1 = new Wtf.FormPanel({
            width:'100%',
            frame:true,
            method :'POST',
            scope: this,
            fileUpload : true,
            waitMsgTarget: true,
            labelWidth: 70,
            items:[{
                bodyStyle: 'padding: 5px',
                items: [{
                    layout: 'form',
                    items:[{
                        xtype : 'textfield',
                        id:'browseBttn',
                        inputType:'file',
                        fieldLabel:WtfGlobal.getLocaleText('pm.common.filename'),
                        allowBlank :false,
                        name: 'test'
                    }]
                }]
            }]
        },
        this);
        var impWin1 = new Wtf.Window({
            resizable: false,
            scope: this,
            layout: 'fit',
            modal:true,
            width: 380,
            height: 140,
            iconCls: 'iconwin',
            title: WtfGlobal.getLocaleText('pm.project.plan.import.text'),
            items: this.ImportPanel1,
            buttons: [{
                text: WtfGlobal.getLocaleText('pm.common.open'),
                type: 'submit',
                scope: this,
                handler: function(){
                    var parsedObject = document.getElementById('browseBttn').value;
                    var extension =parsedObject.substr(parsedObject.lastIndexOf(".")+1);
                    var fileName=parsedObject.substr(parsedObject.lastIndexOf("/")+1);
                    var patt1 = new RegExp("csv","i");
                    if(patt1.test(extension)) {
                        this.ImportPanel1.form.submit({
                            method:'GET',
                            url:'../../exportimportcontacts.jsp?type='+type+'&fileName='+fileName+'&do=getheaders',
                            waitMsg :WtfGlobal.getLocaleText("pm.contacts.import.csv.gettingheaders") + '...',
                            scope:this,
                            success: function (action,res, request) {
                                impWin1.close();
                                var resObj = eval( "(" + res.response.responseText.trim() + ")" );
                                if(resObj.FileName != "") {
                                    this.showHeaderMapWindow(resObj);                                
                                }else{
                                    msgBoxShow(215, 1);
                                }
                            },
                            failure: function ( result, request) {
                                msgBoxShow(152, 1);
                                impWin1.close();
                            }
                        },this);
                    } else
                        msgBoxShow(56, 1);
                }
            },{
                text:WtfGlobal.getLocaleText('lang.cancel.text'),
                scope:this,
                handler:function() {
                    impWin1.close();
                }
            }]
        },this);
        impWin1.show();
    },

    showHeaderMapWindow: function(resObj){
  
        var headerList=[
        ['Name*','name',''],
        ['E-mail ID*','emailid',''],
        ['Contact No.','contactno',''],
        ['Address','address','']
        ];
        
        var headerds = new Wtf.data.SimpleStore({
            fields: [{
                name:'title'
            },{
                name:'header'
            },{
                name:'fileField'
            }] 
        });
        headerds.loadData(headerList);
        
        var headerComboStore=new Wtf.data.JsonStore({
            fields:[
            {
                name:'header'
            },
            {
                name:'index'
            }
            ]
        });
        headerComboStore.loadData(resObj.Headers);
        
        var headerComboEditor = new Wtf.form.ComboBox({
            store:headerComboStore,
            displayField:'header',
            valueField:'header',
            mode:'local',
            triggerAction:'all',
            typeAhead:true
        });
        var listcm = new Wtf.grid.ColumnModel([{
            header: WtfGlobal.getLocaleText("pm.contacts.import.csv.available"),
            dataIndex: 'title'
        },{
            header: WtfGlobal.getLocaleText("pm.contacts.import.csv.mapwithimported"),
            dataIndex: 'fileField',
            allowBlank:false,
            editor: headerComboEditor
            
        }]);
        var grid= new Wtf.grid.EditorGridPanel({
            region:'north',
            clicksToEdit : 1,
            store: headerds,
            cm: listcm,
            sm : new Wtf.grid.RowSelectionModel(),
            border: false,
            height: 150,
            loadMask : true,
            listeners:{
                afteredit:function(){
                    headerds.commitChanges();
                }
            },
            viewConfig: {
                forceFit: true,
                autoFill: true
            }
        });
        var mapWin = new Wtf.Window({
            resizable: false,
            scope: this,
            layout: 'border',
            modal:true,
            width: 400,
            height: 400,
            iconCls: 'iconwin',
            title: WtfGlobal.getLocaleText("pm.contacts.import.csv.mappingwin.title"),
            items:[{
                region : 'north',
                height : 75,
                border : false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                html : getTopHtml(WtfGlobal.getLocaleText("pm.project.plan.import.csv.mapcsvheaders"), WtfGlobal.getLocaleText("pm.project.plan.import.csv.subheader"),"../../images/exportcsv40_52.gif")
            },{
                region : 'center',
                layout:'border',
                items:[grid,
                {
                    region:'center',
                    xtype:'fieldset',
                    title:WtfGlobal.getLocaleText("pm.contacts.import.csv.mappingwin.importoptions"),
                    layout:'auto',
                    width:350,
                    autoHeight:true,
                        
                    items:[{
                        xtype:'radio',
                        name:'impoption',
                        id:'unique',
                        boxLabel:WtfGlobal.getLocaleText("pm.contacts.import.csv.options.noduplicates"),
                        inputValue:'unique',
                        labelSeparator:'',
                        checked:true
                    },{
                        xtype:'radio',
                        name:'impoption',
                        id:'duplicate',
                        boxLabel:WtfGlobal.getLocaleText("pm.contacts.import.csv.options.allowduplicates"),
                        inputValue:'duplicate',
                        labelSeparator:''
                    },{
                        xtype:'radio',
                        name:'impoption',
                        id:'replace',
                        boxLabel:WtfGlobal.getLocaleText("pm.contacts.import.csv.options.replace"),
                        inputValue:'replace',
                        labelSeparator:''                                                        
                    }]
                }
                ]
            }],
            buttons: [{
                text: WtfGlobal.getLocaleText("lang.import.text"),
                type: 'submit',
                scope: this,
                handler: function(btn){
                        
                    var headerObj=new Object();
                    var headerArray=new Array();
                    var i=0;
                    for(i=0;i<headerComboStore.getCount();i++){
                        headerArray[resObj.Headers[i].header]=resObj.Headers[i].index;                        
                    }
                    headerArray[""]="";
                    for(i=0;i<headerds.getCount();i++){
                        headerObj[headerds.getAt(i).get('header')]=headerArray[headerds.getAt(i).get('fileField')];                       
                    }
                                    
                    var flag=true;
                    var headers=new Array();
                    for(i=0;i<headerds.getCount();i++){
                        headers[i]=headerds.getAt(i).get('fileField');                         
                    }
                    if(headers[0]==""){
                        msgBoxShow(211, 1);
                        return false;
                    }else if(headers[1]==""){
                        msgBoxShow(212, 1);
                        return false;
                    }else{
                        for(i=0;i<headers.length-1;i++){
                            var j=i+1;
                            if(headers[i]!=""){
                                while(j<headers.length){
                                    if(headers[i]==headers[j]){
                                        msgBoxShow(213, 1);
                                        i=j=headers.length;
                                        return false;
                                    }
                                    j++;
                                }
                            }
                        }
                    }
                                            
                    if(flag){
                        var value='unique'
                        if(Wtf.getCmp('unique').getValue()){
                            value='unique';
                        }else 
                        if(Wtf.getCmp('duplicate').getValue()){
                            value='duplicate';
                        }else 
                        if(Wtf.getCmp('replace').getValue()){
                            value='replace';
                        }
                        btn.disable();
                        Wtf.Ajax.request({
                            method:'POST',
                            url:'../../exportimportcontacts.jsp?do=import',
                            params:{
                                filename:resObj.FileName,
                                realFileName:resObj.RealFileName,
                                fileid:resObj.FileID,
                                headerMap:Wtf.encode(headerObj),
                                importType:value
                            },
                            waitMsg :WtfGlobal.getLocaleText("pm.importing.text")+'...',
                            scope:this,
                            success: function (response, request) {
                                mapWin.close();
                                var result = eval( "(" + response.responseText.trim() + ")" );
                                if(result.msg != "") {
                                    this.mycontactStore.reload();
                                    msgBoxShow(["Success",WtfGlobal.getLocaleText(result.msg)+'<br>' + WtfGlobal.getLocaleText("lang.pleasecheck.text")+' '+importLogLink()+'.'],0);
                                }else{
                                    msgBoxShow(214,0);  
                                }
                            },
                                    
                            failure: function ( result, request) {
                                mapWin.close();
                                this.mycontactStore.reload();
                                msgBoxShow(152, 1);
                            }
                        },this);
                    }
                }
            },{
                text:WtfGlobal.getLocaleText('lang.cancel.text'),
                scope:this,
                handler:function() {
                    mapWin.close();
                }
            }]
        });
        mapWin.show();
    },
    displayName:function(value,gridcell,record,d,e){
        var uname=(record.json.username).trim();
        return WtfGlobal.URLDecode(decodeURIComponent(uname));
    },
    comboBoxRenderer : function(combo) {
        return function(value) {
            var idx = combo.store.find(combo.valueField, value);
            if(idx == -1)
                return "<span style='color:gray;'>"+combo.emptyText+"</span>";
            var rec = combo.store.getAt(idx);
            return WtfGlobal.URLDecode(decodeURIComponent(rec.get(combo.displayField)));
        };
    },
    CreateNewContact : function() {
        var rec = this.grid.getSelectionModel().getSelected();
        this.addExtContactfunction(0,rec,1);
    },
    mapImportedRes : function(flag) {
        var nrecords=0;
        var temp_h="";
        var nrows=this.listds.getCount();
        var jsonData = "{userdata:[";
        for(var cnt =0 ;cnt< nrows;cnt++) {
            var urec = this.listds.getAt(cnt);
            var ind=this.userds.find("cusername",urec.data.cusername);
            if(ind!=-1){
                nrecords++;
                var crec=this.userds.getAt(ind);
                jsonData +=  temp_h+"{user:\""+crec.data.cusername+"\",email:\""+crec.json.cemailid+"\",userid:\""+urec.json.userid+"\",username:\""+urec.json.username+"\",emailid:\""+urec.json.emailid+"\",address:\""+urec.json.address+"\",contactno:\""+urec.json.contactno+"\"}";
                temp_h=",";
            }
        }
        //jsonData = jsonData.substr(0, jsonData.length - 1) + "]}";
        jsonData +="]}";
        if(nrecords>0){
            Wtf.Ajax.requestEx({
                url: '../../jspfiles/contact.jsp',
                params: ({
                    type:"repContact",
                    val: jsonData
                }),
                method: 'POST'
            },
            this,
            function(result, req){
                if(result!=null && result != "")
                    msgBoxShow(233, 0);
                this.mycontactStore.reload();
                this.conflictWin.close();
//                this.close();
            },function(){
                //this.close();
                });
        }
        else{
             Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText('lang.success.text'),
                        msg: WtfGlobal.getLocaleText('pm.msg.233'),
                        buttons: Wtf.MessageBox.OK,
                        animEl: 'mb9',
                        icon: Wtf.MessageBox.INFO
                    });
            this.conflictWin.close();
        }
    },
    createContactStore : function() {
        var assigncontactsArry = ["userid","username","emailid","address","contactno","role","status","statusid","image"];
        var Readerrecord =  Wtf.data.Record.create(assigncontactsArry);
        var reader = new Wtf.data.KwlJsonReader({
            root: 'data',
            totalProperty:'count',
            id: 'threadid'
        },Readerrecord);
        var url = null;
        this.mycontactStore = new Wtf.data.Store({
            url : "../../jspfiles/contact.jsp",
            root : "data",
            reader:reader,
            baseParams : {
                type: "mycontacts",
                userid: loginid
            },
            autoLoad : false,
            sortInfo: {field:'username', direction:'ASC'}
        });
        this.mycontactStore.on("load",function(){
            this.quickSearchContact.StorageChanged(this.mycontactStore);
            if(!this.searchPerformed){
                if(this.mycontactStore.getCount() == 0){
                    this.quickSearchContact.setDisabled(true);
                    this.searchPerformed = false;
                }
            }
            if(this.contactsTree==null){
                Wtf.getCmp('charttreepanel').add( this.contactsTree = new Wtf.ContactsTree({
                    border:false,
                    tabless:true
                }));
                this.innerPanel.doLayout();
                document.getElementById('contactsview').getElementsByTagName('div')[0].getElementsByTagName('div')[0].style.border = 'none';
                this.contacts = Wtf.getCmp('contactsview').contacts;
            }
        },this);
    },

    createNewContactStore : function() {
        var Readerrecord =  Wtf.data.Record.create(["id","username","emailid","address","contactno","image"]);
        var reader = new Wtf.data.KwlJsonReader({
            root: 'data',
            totalProperty:'count',
            id: 'threadid'
        },Readerrecord);
        this.newContactStore = new Wtf.data.Store({
            url : "../../jspfiles/contact.jsp",
            reader:reader,
            baseParams : {
                type : "newcontacts",
                userid :loginid
            },
            autoLoad : false,
            sortInfo: {field:'username', direction:'ASC'}
        });
        this.newContactStore.on("load",function(){
            this.quickSearchNewContact.StorageChanged(this.newContactStore);
            if(!this.newsearchPerformed){
                if(this.newContactStore.getCount() == 0){
                    this.quickSearchNewContact.setDisabled(true);
                    this.newsearchPerformed = false;
                } else
                    this.quickSearchNewContact.setDisabled(false);
			}
        },this);
    },

    createContactCM : function(flag) {
        var columnArry;
        columnArry  = [{
            header : WtfGlobal.getLocaleText('lang.username.text'),
            dataIndex: 'username',
            autoWidth: true,
            sortable: true
        },{
            dataIndex: 'status',
            width:12,
            header : WtfGlobal.getLocaleText('lang.action.text'),
            renderer:function(value){
                if(value == 'Invited')
                    return "<img id='AcceptImg' class='accept' style='cursor:pointer;' src='../../images/check16.png' wtf:qtip='"+WtfGlobal.getLocaleText('pm.help.conreqaccqtitle')+"'></img> <img id='AcceptImg' class='cancel' style='cursor:pointer;' src='../../images/Stop.png' wtf:qtip='"+WtfGlobal.getLocaleText('pm.help.conreqrejqtitle')+"'></img>";
                else
                    return "<img id='AcceptImg' class='cancel' style='cursor:pointer;' src='../../images/Delete.gif' wtf:qtip='"+WtfGlobal.getLocaleText('pm.Help.conquickdelete')+"'></img>";
            }
        }];
        return columnArry;
    },

    createNewContactCM : function(flag) {
        return [{
            header : WtfGlobal.getLocaleText('lang.user.text'),
            dataIndex: "username",
            autoWidth: true,
            sortable: true
        },{
            header : WtfGlobal.getLocaleText('pm.contacts.addascontact'),
            dataIndex: 'status',
            width:12,
            renderer:function(){
                return "<img id='AcceptImg' class='add' style='height:18px; width:18px; cursor:pointer;' src='../../images/btn_add_quick.gif' wtf:qtip='"+WtfGlobal.getLocaleText('pm.Help.conquickadd')+"'></img>";
            }
        }];
    },

    createContactGrid : function() {
        this.contactGrid = new Wtf.grid.GridPanel({
            store: this.mycontactStore,
            cm: this.contactCM,
            border: false,
            id : '_mycontactStore',
            enableColumnHide : false,
            loadMask: {
                msg: WtfGlobal.getLocaleText("pm.loading.text")+'...'
            },
            viewConfig: {
                forceFit: true,
                emptyText: '<div class="emptyGridText">'+WtfGlobal.getLocaleText("pm.contacts.emptytext1")+'<br><a href="#" onClick=\'createExtContact(\"'+this.id+'\")\'>'+WtfGlobal.getLocaleText('pm.contact.grid.emptytext')+'</a></div>'
            }
        });
        this.contactGrid.on("cellclick", this.gridcellClickHandle, this);
        this.contactGrid.getSelectionModel().addListener('rowselect', this.rowSelectionHandler, this);
    },
    rowSelectionHandler: function(sm,rowIndex,r) {
        var myContactsSelections = this.contactGrid.getSelectionModel().getSelections();
        var NewContactSelections = this.newContactGrid.getSelectionModel().getSelections();
        var selected = sm.getSelections();
        this.viewmyFriendProfile.disable();
        this.editExtContact.disable();
        if (selected.length == 1) {
            var pan=Wtf.getCmp('ContactDetails');
            var tpl = new Wtf.XTemplate(
                '<div class="contactInfo"><h1><p>{username}</p></h1>',
                '<p><br>'+WtfGlobal.getLocaleText('pm.common.email')+'     : {emailid}</p>',
                '<p>'+WtfGlobal.getLocaleText('lang.address.text')+'  : {address}</p>',
                '<p>'+WtfGlobal.getLocaleText('lang.contactno.text')+' : {contactno}</p></div>',
                '<img src="{image}" class="contactInfoImg">',
                '</tpl>'
            );
            tpl.overwrite(pan.body, selected[0].data);
            if(NewContactSelections.length == 1){
                if(NewContactSelections[0].data["statusid"]!=-999)
                    this.viewProfile.enable();
                else
                    this.editExtContact.enable();
            }
            if(myContactsSelections.length == 1){
                if(myContactsSelections[0].data["statusid"]!=-999)
                    this.viewmyFriendProfile.enable();
                else
                    this.editExtContact.enable();
            }
        } else {
            if (selected.length == 0)
                this.clearContactDetails(false);
            else
                this.clearContactDetails(true);
        }
    },
    createNewContactGrid : function() {
        this.newContactGrid = new Wtf.grid.GridPanel({
            store: this.newContactStore,
            cm: this.newContactCM,
            id : '_newcontactStore',
            enableColumnHide : false,
            border: false,
            region:'center',
            bbar: this.pagingNewToolbar = new Wtf.PagingSearchToolbar({
                pageSize : 15,
                id : "newpagingtoolbar" + this.id,
                store : this.newContactStore,
                searchField : this.quickSearchNewContact,
                displayInfo : true,
//                displayMsg : '{0} - {1} of {2}',
                emptyMsg : WtfGlobal.getLocaleText('pm.contacts.emptytext1'),
                plugins : this.pP_SearchNewContacts = new Wtf.common.pPageSize({
                    id : "pPageSizeAssign_"+this.id
                    })
            }),
            loadMask: {
                msg: WtfGlobal.getLocaleText('pm.loading.text')+'...'
            },
            viewConfig: {
                forceFit: true,
                emptyText: '<div class="emptyGridText">'+WtfGlobal.getLocaleText('pm.contacts.grid.emtytext')+'</div>'
            }
        });
        this.newContactGrid.on("cellclick", this.gridcellClickHandle, this);
        this.newContactGrid.getSelectionModel().addListener('rowselect', this.rowSelectionHandler, this)
	},

    viewProfile : function() {
        if(this.newContactGrid.getSelectionModel().getSelections().length > 0) {
            var rec = this.newContactGrid.getSelectionModel().getSelections()[0];
            var userid = rec.data['id'];
            var username = rec.data['username'];
            mainPanel.loadTab("../../user.html","   " +userid, username, "navareadashboard",Wtf.etype.user,true);
        } else
            msgBoxShow(88, 1);
    },

    viewmyFriendProfile :function() {
        if(this.contactGrid.getSelectionModel().getSelections().length > 0) {
            var rec = this.contactGrid.getSelectionModel().getSelections()[0];
            var userid = rec.data['userid'];
            var username = rec.data['username'];
            mainPanel.loadTab("../../user.html","   " +userid, username, "navareadashboard",Wtf.etype.user,true);
        } else
            msgBoxShow(88, 1);
    },

    assignmentUI : function() {
        var items = [
        this.chatTreePanel = {
            region:'west',
            border:true,
            layout:'fit',
            frame:false,
            autoScroll:true,
            id :'charttreepanel',
            bodyStyle:'background:transparent;padding:10px;',
            width : "39%",
            split:true
        },
        this.assignmentLP = new Wtf.common.KWLListPanel({
            region : "center",
            title : '<div wtf:qtip="'+WtfGlobal.getLocaleText('pm.Help.mycontacts')+'">'+WtfGlobal.getLocaleText('pm.contacts.mycontacts')+'</div>',
            split : true,
            autoLoad : false,
            layout : "fit",
            bodyStyle : "background:transparent;",
            paging : false,
            tbar : [/*'Quick Search: ',*/
            this.quickSearchContact = new Wtf.KWLTagSearch({
                width: 125,
                field : "username",
                emptyText: WtfGlobal.getLocaleText('pm.contacts.searchbyun')
            }),
            this.viewmyFriendProfile,"-",
            this.Contact,"-",
            this.ExpImp
            ],
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 15,
                id: "pagingtoolbar" + this.id,
                store: this.mycontactStore,
                searchField: this.quickSearchContact,
                displayInfo: true,
//                displayMsg: '{0} - {1} of {2}',
                emptyMsg : WtfGlobal.getLocaleText('pm.contacts.emptytext1'),
                plugins: this.pP_SearchContacts = new Wtf.common.pPageSize({
                    id : "pPageSizeAssign_"+this.id
                })
            }),
            items : this.contactGrid,
            autoScroll : true
        }), this.descriptionLP = new Wtf.common.KWLListPanel({
            title : '<div wtf:qtip="'+WtfGlobal.getLocaleText('pm.Help.newcontacts')+'">'+WtfGlobal.getLocaleText('pm.contacts.newcontacts')+'</div>',
            region : "east",
            split : true,
            width : "33%",
            autoLoad : false,
            layout : "border",
            tbar : [/*'Quick Search: ',*/
            this.quickSearchNewContact = new Wtf.KWLTagSearch({
                width: 100,
                field: "name",
                emptyText:  WtfGlobal.getLocaleText('pm.contacts.searchbyuser')
            }),
            this.viewProfile],
            bodyStyle : "background:transparent;",
            paging : false,
            items :[ this.newContactGrid,
            new Wtf.Panel({
                id:'ContactDetails',
                region:'south',
                border:false,
                height:150,
                html:"<p><h1>&nbsp;"+WtfGlobal.getLocaleText('pm.contacts.nocontactsselected')+"</h1></p>"
            })],
            autoScroll : true
        })];
        this.innerPanel = new Wtf.Panel({
            border : false,
            layout : "border",
            bodyStyle : "background:transparent;",
            items : items
        });
        this.innerPanel.addListener("resize",this.innerPanelResize,this);

        this.quickSearchContact.on('SearchComplete', this.QuickSearchContactComplete, this);

        this.mycontactStore.on('datachanged', function(){
            this.quickSearchContact.setPage(this.pP_SearchContacts.combo.value);
            this.emptyTextChange();
        }, this);

        this.quickSearchNewContact.on('SearchComplete', this.QuickNewSearchComplete, this);

        this.newContactStore.on('datachanged', function(){
            this.quickSearchNewContact.setPage(this.pP_SearchNewContacts.combo.value);
            this.newEmptyTextChange();
        }, this);
    },

    QuickSearchContactComplete: function(e){
        var view = this.contactGrid.getView();
        this.emptyTextChange();
    },

    emptyTextChange: function(){
        var view = this.contactGrid.getView();
        if(this.quickSearchContact.getValue() != "")
            this.searchPerformed = true;
        if(this.contactGrid.getStore().getCount() == 0 && !this.searchPerformed)
            view.emptyText = '<div class="emptyGridText">No Contacts to display. <br><a href="#" onClick=\'createExtContact(\"'+this.id+'\")\'>'+WtfGlobal.getLocaleText('pm.contact.grid.emptytext')+'</a></div>';
        else
            view.emptyText = '<div class="emptyGridText">'+WtfGlobal.getLocaleText('pm.grid.view.emptytext')+' </div>';
        this.clearContactDetails();
        this.rowSelectionHandler(this.newContactGrid.getSelectionModel())
        view.refresh();
    },

    QuickNewSearchComplete: function(e){
        var view = this.newContactGrid.getView();
        this.newEmptyTextChange();
    },

    newEmptyTextChange: function(){
        var view = this.newContactGrid.getView();
        if(this.quickSearchNewContact.getValue() != "")
            this.newsearchPerformed = true;
        this.clearContactDetails();
        this.rowSelectionHandler(this.contactGrid.getSelectionModel())
        view.refresh();
    },

    innerPanelResize : function(obj,w,h) {
        this.assignmentLP.setWidth(w/4);
        Wtf.getCmp('charttreepanel').setWidth(w/6.0);
        this.innerPanel.doLayout();
    },


    handleAddContact :function() {
        var selectedRecord = this.newContactGrid.getSelectionModel().getSelections();
        var idstr = "";
        for(var ctr=0;ctr<selectedRecord.length;ctr++) {
            idstr += selectedRecord[ctr].data['id'];
            if(ctr != selectedRecord.length-1)
                idstr += ",";
        }
        Wtf.Ajax.requestEx({
            method:'POST',
            url:'../../jspfiles/contact.jsp',
            params:{
                type:"addContact",
                userid :loginid,
                requestto:idstr
            }
        }, this,
        function(result, req){
            var retstatus = eval('('+result+')');
            if(retstatus.success == 'true') {
                this.mycontactStore.load({
                    params : {
                        start : 0,
                        limit:this.pagingToolbar.pageSize/*15*/
                    }
                });
                this.newContactStore.load({
                    params : {
                        start : 0,
                        limit:this.pagingNewToolbar.pageSize
                    }
                });
                this.createTreeNode(selectedRecord[0].data['id'],selectedRecord[0].data['username'] +' [Invited]','offline');
                this.publishAction(selectedRecord[0].data['id'],'7');
            /*Wtf.Ajax.requestEx({
                        method:'POST',
                        url: Wtf.req.prt + "getFriendListDetails.jsp",
                        params:{
                            userid: loginid,
                            mode : '7',
                            remoteUserId: selectedRecord[0].data['id'],
                            username: selectedRecord[0].data['name']
                        }},
                        this,
                        function(){
                });*/
            }
        }, function(){
            });
    },

    createTreeNode : function(id,nodetext,nodestatus) {
        Wtf.getCmp('contactsview').createContactNode(id,nodetext,nodestatus);
    },

    handleAcceptContact :function() {
        var selectedRecord = this.contactGrid.getSelectionModel().getSelections();
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), 'Are you sure you want to add \"'+selectedRecord[0].data['username']+'\" to your contact list?', this.confirmAccept, this);
    },

    confirmAccept : function(btn, text) {
        if(btn=="yes"){
            var selectedRecord = this.contactGrid.getSelectionModel().getSelections();
            var idstr = "";
            for(var ctr=0;ctr<selectedRecord.length;ctr++) {
                idstr += selectedRecord[ctr].data['userid'];
                if(ctr != selectedRecord.length-1)
                    idstr += ",";
            }
            Wtf.Ajax.requestEx({
                method:'POST',
                url:'../../jspfiles/contact.jsp',
                params:{
                    type:"acceptContact",
                    userid :loginid,
                    requestto:idstr
                }
            }, this, function(result, req){
                this.createTreeNode(selectedRecord[0].data['userid'],selectedRecord[0].data['username'],'offline');
                //this.publishMyStatus(selectedRecord);
                this.publishAction(selectedRecord[0].data['userid'],'6');
                this.mycontactStore.load({
                    params : {
                        start : 0,
                        limit:15
                    }
                });
                bHasChanged = true;
                if(refreshDash.join().indexOf("all") == -1 && refreshDash.join().indexOf("req") == -1){
                    refreshDash[refreshDash.length] = 'req';
                }
            });
        }
    },

    handleDeleteContact :function() {
        var selectedRecord = this.contactGrid.getSelectionModel().getSelections();
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'),  WtfGlobal.getLocaleText({key:'pm.msg.contact.conrfdelete',params:[selectedRecord[0].data['username']]}), this.confirmDelete, this);
    },

    confirmDelete :function(btn, text) {
        if(btn=="yes") {
            var selectedRecord = this.contactGrid.getSelectionModel().getSelections();
            var idstr = "";
            var extid = "";
            for(var ctr=0;ctr<selectedRecord.length;ctr++) {
                if(selectedRecord[ctr].data['statusid']!=-999)
                    idstr += selectedRecord[ctr].data['userid']+",";
                else
                    extid += selectedRecord[ctr].data['userid']+",";
            }
            if(idstr!=""){
                idstr = idstr.substr(0,idstr.lastIndexOf(","));
                Wtf.Ajax.requestEx({
                    method:'POST',
                    url:'../../jspfiles/contact.jsp',
                    params:{
                        type:"deleteContact",
                        userid :loginid,
                        requestto:idstr
                    }
                }, this, function(result, req){
                    var count = this.mycontactStore.getCount();
                    var ncount = this.newContactStore.getCount();
                    var start = this.mycontactStore.lastOptions.params.start - this.pP_SearchContacts.combo.value;
                    var nstart = this.newContactStore.lastOptions.params.start - this.pP_SearchContacts.combo.value;
                    start = (start < 0)? start= 0 : start;
                    nstart = (nstart < 0)? nstart= 0 : nstart;
                    if(count == 1){
                        this.mycontactStore.load({
                            params : {
                                start : start,
                                limit:this.pP_SearchContacts.combo.value
                            }
                        });
                    } else {
                        this.mycontactStore.load({
                            params : {
                                start : 0,
                                limit:this.pP_SearchContacts.combo.value
                            }
                        });
                    }
                    if(ncount == 1){
                        this.newContactStore.load({
                            params : {
                                start : start,
                                limit:this.pP_SearchContacts.combo.value
                            }
                        });
                    } else {
                        this.newContactStore.load({
                            params: {
                                start : 0,
                                limit:this.pP_SearchNewContacts.combo.value
                            }
                        })
                    }
                    var obj = Wtf.getCmp('contactsview').getNodeById('kcont_' + selectedRecord[0].data['userid']);
                    if(obj) {
                        obj.parentNode.removeChild(obj);
//                        if(Wtf.getCmp('chatWin'+obj.id.substr(6))!=null){
//                            Wtf.getCmp('chatWin'+obj.id.substr(6)).show();
//                            Wtf.getCmp('chatWin'+obj.id.substr(6)).close();
//                        }
                        this.publishAction(selectedRecord[0].data['userid'],'5');
                    /*Wtf.Ajax.requestEx({
                            url: Wtf.req.prt + "getFriendListDetails.jsp",
                            params: {
                                userid: loginid,
                                mode : '5',
                                remoteUser: selectedRecord[0].data['userid']
                            }
                        });*/
                    }
                    this.clearContactDetails();
                });
            }
            if(extid!=""){
                extid = extid.substr(0,extid.lastIndexOf(","));
                Wtf.Ajax.requestEx({
                    url: "../../AddressBook.jsp",
                    params:{
                        id:extid,
                        action:1
                    },
                    method: 'POST'
                },
                this,
                function(frm, act){
                    this.mycontactStore.load({
                        params : {
                            start : 0,
                            limit:this.pP_SearchContacts.combo.value
                        }
                    });
                    this.clearContactDetails();
                });
            }
        }
    },

    clearContactDetails : function(multipleSelections) {
        var contactDetails = Wtf.getCmp("ContactDetails");
        if(!multipleSelections){
            var tpl = new Wtf.XTemplate(
                "<p><h1>&nbsp;"+WtfGlobal.getLocaleText('pm.contacts.nocontactsselected')+"</h1></p>",
                "</tpl>"
            );
        } else {
            tpl = new Wtf.XTemplate(
                "<p><h1>&nbsp;</h1></p>",
                "</tpl>"
            );
        }
        tpl.overwrite(contactDetails.body, "");
    },

    addExtContactHandler : function(){
        this.addExtContactfunction(0);
    },
    addExtContactfunction:function(action,record,flag){
        var windowHeading = action==0?WtfGlobal.getLocaleText('pm.contacts.addcontact'):WtfGlobal.getLocaleText('pm.contacts.editcontact');
        var windowMsg = action==0?WtfGlobal.getLocaleText('pm.contacts.windows.addtitle'):WtfGlobal.getLocaleText('pm.contacts.windows.edittitle');
        this.addExtContactWindow = new Wtf.Window({
            title : action==0?WtfGlobal.getLocaleText('pm.contacts.addcontact'):WtfGlobal.getLocaleText('pm.contacts.editcontact'),
            closable : true,
            modal : true,
            iconCls : 'iconwin',
            width : 430,
            height: 370,
            resizable :false,
            buttons :[{
                text : action==0?WtfGlobal.getLocaleText('lang.add.text'):WtfGlobal.getLocaleText('lang.edit.text'),
                id: "createUserButton",
                scope : this,
                handler:function(){
                    if(this.createuserForm.form.isValid()){
                        if(flag==1){
                            Wtf.Ajax.requestEx({
                                url: '../../jspfiles/contact.jsp',
                                params: ({
                                    type:"newAddress",
                                    userid:Wtf.getCmp('tempContIdField').getValue(),
                                    username:Wtf.getCmp('tempNameField').getValue(),
                                    emailid:Wtf.getCmp('tempEmailField').getValue(),
                                    address: Wtf.getCmp('tempAddField').getValue(),
                                    contactno:Wtf.getCmp('tempPhoneField').getValue()
                                }),
                                method: 'POST'
                            },
                            this,
                            function(result, req){
                                if(result!=null && result != ""){
                                    msgBoxShow(150, 0);
                                }
                                this.listds.remove(record);
                                this.mycontactStore.reload();
                                this.addExtContactWindow.close();
                                this.close();
                            },function(){
                                this.close();
                            });
                        }
                        else{
                            this.createuserForm.form.submit({
                                waitMsg: WtfGlobal.getLocaleText("pm.loading.text")+'...',
                                scope : this,
                                failure: function(res, req){
                                    msgBoxShow(20, 1);
                                    this.addExtContactWindow.close();
                                },
                                success: function(res, req){
                                    var resObj = eval('('+req.response.responseText+')');
                                    if(resObj.data != ""){
                                        msgBoxShow([WtfGlobal.getLocaleText('lang.error.text'), resObj.data], 1);
                                        Wtf.getCmp("tempEmailField").setValue("");
                                    }
                                    else{
                                        if(action==0)
                                            msgBoxShow(150, 0);
                                        else
                                            msgBoxShow(151, 0);
                                        this.addExtContactWindow.close();
                                        this.mycontactStore.reload({
                                            params:{
                                                start:0,
                                                limit:this.pP_SearchContacts.combo.value
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                 }
            },{
                text : WtfGlobal.getLocaleText('lang.cancel.text'),
                id:'cancelCreateUserButton',
                scope : this,
                handler : function(){
                    this.addExtContactWindow.close();
                }
            }],
            layout : 'border',
            items :[{
                region : 'north',
                id: "userwinnorth",
                height : 75,
                border : false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                html :  getTopHtml(windowHeading,windowMsg+"<br><span style=\"float:right; \">"+WtfGlobal.getLocaleText('pm.common.requiredfields')+"</span>","../../images/createuser40_52.gif")
            },{
                region : 'center',
                border : false,
                id : 'userwincenter',
                bodyStyle : 'background:#f1f1f1;font-size:10px;',
                layout : 'fit',
                items :[this.createuserForm = new Wtf.form.FormPanel({
                    url: '../../AddressBook.jsp?action='+action,
                    waitMsgTarget: true,
                    method : 'POST',
                    border : false,
                    labelWidth: 120,
                    bodyStyle : 'margin-top:20px;margin-left:35px;font-size:10px;',
                    defaults: {
                        width: 200
                    },
                    defaultType: 'textfield',
                    items: [{
                        fieldLabel: WtfGlobal.getLocaleText('lang.name.text')+'*',
                        id:'tempNameField',
                        name:'name',
                        validator:WtfGlobal.validateUserName,
                        allowBlank:false,
                        maxLength:50,
                        maxLengthText:WtfGlobal.getLocaleText('pm.contactsname.maxlength.text')
                    },{
                        fieldLabel: WtfGlobal.getLocaleText('lang.email.text')+'*',
                        id:'tempEmailField',
                        name: 'emailid',
                        validator: WtfGlobal.validateEmail,
                        allowBlank:false,
                        renderer: WtfGlobal.renderEmailTo
                    },{
                        fieldLabel: WtfGlobal.getLocaleText('lang.contactno.text')+'*',
                        allowBlank:false,
                        validator: WtfGlobal.validatePhone,
                        id: "tempPhoneField",
                        name: 'phone'
                    },{
                        xtype:"textarea",
                        fieldLabel: WtfGlobal.getLocaleText('lang.address.text'),
                        id: "tempAddField",
                        name: 'address'
                    },{
                        xtype:"hidden",
                        id: "tempContIdField",
                        name: 'id'
                    }]
                })]
            }]
        });
        Wtf.getCmp('tempPhoneField').on("change", function(){
            Wtf.getCmp('tempPhoneField').setValue(WtfGlobal.HTMLStripper(Wtf.getCmp('tempPhoneField').getValue()));
        }, this);
        Wtf.getCmp('tempAddField').on("change", function(){
            Wtf.getCmp('tempAddField').setValue(WtfGlobal.HTMLStripper(Wtf.getCmp('tempAddField').getValue()));
        }, this);
        this.addExtContactWindow.show();
        if(record!=null){
            if(flag==1){
                Wtf.getCmp('tempNameField').setValue(record.json.username);
                Wtf.getCmp('tempEmailField').setValue(record.json.emailid);
                Wtf.getCmp('tempPhoneField').setValue(record.json.contactno);
                Wtf.getCmp('tempAddField').setValue(record.json.address);
                Wtf.getCmp('tempContIdField').setValue(record.json.userid);
            }else{
                var recData = record.data[0];
                Wtf.getCmp('tempNameField').setValue(recData.name);
                Wtf.getCmp('tempEmailField').setValue(recData.emailid);
                Wtf.getCmp('tempPhoneField').setValue(recData.phone);
                Wtf.getCmp('tempAddField').setValue(recData.address);
                Wtf.getCmp('tempContIdField').setValue(recData.id);
            }
        }
        focusOn("tempNameField");
    },
    EditExtContactHandler:function(){
        var selectedRecord = this.contactGrid.getSelectionModel().getSelections();
        var idstr = "";
        var extid = "";
        extid += selectedRecord[0].data['userid'];
        Wtf.Ajax.requestEx({
            method:'POST',
            url:'../../AddressBook.jsp',
            params:{
                contactid:extid,
                action:4
            }
        }, this,
        function(result){
            record = eval('('+result+')');
            this.addExtContactfunction(2,record);
        },
        function(){
        });
    },
    gridcellClickHandle :function(obj,row,col,e) {
        var event = e ;
        if(event.getTarget("img[class='accept']"))
            this.handleAcceptContact();
        else if(event.getTarget("img[class='cancel']"))
            this.handleDeleteContact();
        else if(event.getTarget("img[class='add']"))
            this.handleAddContact();
    },

    publishAction : function(remoteuserid,mode) {
        Wtf.Ajax.requestEx({
            url: Wtf.req.prt + "getFriendListDetails.jsp",
            params: {
                userid: loginid,
                mode : mode,
                remoteUser: remoteuserid
            }
        }, this);
    }
});

var contacts =  new Wtf.myContactGrid({
    id : "tabns_panel",
    border : false,
    layout : "fit"
});
Wtf.getCmp("tabcontactpanel").add(contacts);
Wtf.getCmp("tabcontactpanel").doLayout();
contacts.on('destroy', function(){
    Wtf.EventManager.removeResizeListener(this.resizehandler, this);
});

function createExtContact(id){
    Wtf.getCmp(id).addExtContactHandler(0);
}
