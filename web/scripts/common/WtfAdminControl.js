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
Wtf.common.adminpageuser = function(config){
    Wtf.common.adminpageuser.superclass.constructor.call(this,config);
    this.addEvents({
        "announce": true,
        "dataRendered": true
    });
};
Wtf.extend(Wtf.common.adminpageuser,Wtf.Panel,{
    onRender : function(config){
        Wtf.common.adminpageuser.superclass.onRender.call(this,config);
        this.count = 0;
        this.usersRec = new Wtf.data.Record.create([
            {name: 'userid'},
            {name: 'username'},
            {name: 'fname'},
            {name: 'lname'},
            {name: "fullname"},
            {name: 'image'},
            {name: 'emailid'},
            {name: 'lastlogin',type: 'date',dateFormat: 'Y-m-j H:i:s'},
            {name: 'aboutuser'},
            {name: 'address'},
            {name:'contactno'},
            {name: 'permissions'}
        ]);
        this.dataReader = new Wtf.data.KwlJsonReader({
            totalProperty: 'count',
            root: "data"
        },this.usersRec);

        this.userds = new Wtf.data.Store({
            id : "userds"+this.id,
            reader: this.dataReader,
            url: '../../admin.jsp?action=0&mode=0&companyid='+this.companyid,//mode=0 is user
            method : 'GET'
        });

        this.selectionModel = new Wtf.grid.CheckboxSelectionModel();
        this.gridcm= new Wtf.grid.ColumnModel([this.selectionModel,{
            dataIndex: 'image',
            width : 30,
            fixed : true,
            renderer : function(value){
                if(value == ""){
                    value = Wtf.DEFAULT_USER_URL;
                }
                return String.format("<img src={0} style='height:18px;width:18px;vertical-align:text-top;'/>",value);
            }
        },{
            header: WtfGlobal.getLocaleText('pm.common.UserId'),
            dataIndex: 'username',
            autoWidth : true,
            sortable: true,
            groupable: true
        },{
            header: WtfGlobal.getLocaleText('lang.name.text'),
            dataIndex: 'fullname',
            autoWidth : true,
            sortable: true,
            groupable: true
        },{
            header :WtfGlobal.getLocaleText('pm.updateprofile.emailaddreass'),
            dataIndex: 'emailid',
            autoSize : true,
            sortable: true,
            renderer: WtfGlobal.renderEmailTo,
            groupable: true
        },{
            header :WtfGlobal.getLocaleText('pm.admin.user.lastlogin'),
            dataIndex: 'lastlogin',
            renderer:WtfGlobal.userPrefDateRenderer,
            autoSize : true,
            sortable: true,
            groupable: true
        },{
            header :WtfGlobal.getLocaleText('lang.address.text'),
            dataIndex: 'address',
            autoSize : true,
            sortable: true,
            groupable: true
        },{
            header :WtfGlobal.getLocaleText('lang.permissions.text'),
            dataIndex: 'permissions',
            width: 35,
            renderer: function(text){
                return "<div class = 'x-btn-text pwnd permicon' style = 'height: 16px; width: 16px; display: block;' wtf:qtitle="+WtfGlobal.getLocaleText('lang.permissions.text')+" wtf:qtip='"+text+"'></div>";
            }
        }]);

        this.usergrid = new Wtf.grid.GridPanel({
            id:'usergrid'+this.id,
            store: this.userds,
            cm: this.gridcm,
            sm : this.selectionModel,
            border : false,
            loadMask : true,
            enableColumnHide: false,
            viewConfig: {
                forceFit:true,
                emptyText:'<div class="emptyGridText">'+WtfGlobal.getLocaleText('pm.paging.noresult')+'<br></div>'
            }
        });

        this.usergrid.on("rowdblclick",this.GridRowdbClicked,this);
        this.usergrid.getSelectionModel().addListener('selectionchange', this.rowSelectionHandler, this);
        this.userds.load({
            params: {
                start:0,
                limit:15
            }
        });

        this.UsergridPanel  = new Wtf.common.KWLListPanel({
            id : "Usergridpanel"+this.id,
            title:WtfGlobal.getLocaleText('pm.project.member.profile.click'),
            autoLoad : false,
            paging : false,
            layout : 'fit',
            items:[this.usergrid]
        });
        this.innerpanel = new Wtf.Panel({
            id : 'innerpanel'+this.id,
            layout : 'fit',
            cls : 'backcolor',
            border : false,
            tbar:[/*'Quick Search: ',*/
            this.quickSearchTF = new Wtf.KWLTagSearch({
                id : 'user'+this.id,
                width: 200,
                field : "username",
                emptyText: WtfGlobal.getLocaleText('pm.search.byuserid')
            })],
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 15,
                id: "pagingtoolbar" + this.id,
                store: this.userds,
                searchField: this.quickSearchTF,
                displayInfo: false,
//                displayMsg: WtfGlobal.getLocaleText('pm.paging.displayrecord'),
                emptyMsg: WtfGlobal.getLocaleText('pm.paging.noresult'),
                plugins: this.pP = new Wtf.common.pPageSize({id : "pPageSize_"+this.id}),
                items :[
                '->',{
                    text : "User Management",
                    scope : this,
                    disabled: false,
                    hidden: (standAlone && creator)? false : true,
                    tooltip: {
                        title: 'Create, edit and delete user(s).',
                        text: 'Manage existing users in the application, or create new ones.'
                    },
                    menu: [{
                           text: 'Create User',
                           id: 'createuser_'+this.id,
                           scope: this,
                           handler: this.createEditUser,
                           btnPurpose: 'create',
                           tooltip: {
                               title: 'Create new user',
                               text: 'Create a new user in the application'
                           },
                           iconCls: 'dpwnd addresource'
                        },{
                           text: 'Edit User',
                           id: 'edituser_'+this.id,
                           scope: this,
                           btnPurpose: 'edit',
                           disabled: true,
                           handler: this.createEditUser,
                           iconCls : 'pwnd editresource',
                           tooltip: {
                               title: 'Edit user',
                               text: 'Select a user from the list and click here to edit his/her details.'
                           }
                        },{
                           text: 'Remove User',
                           id: 'removeuser_'+this.id,
                           scope: this,
                           handler: this.deleteUser,
                           tooltip: {
                               title: 'Remove user',
                               text: 'Remove the selected users from the system. Such users will no longer be able to access the application and will also be removed from all the projects they may be assigned o.'
                           },
                           iconCls: 'dpwnd deleteresource'
                        }
                    ],
                    iconCls : 'dpwnd addppl'
                },{
                    text : WtfGlobal.getLocaleText("pm.common.assigntoprojects"),
                    id : "addto"+this.id,
                    allowDomMove:false,
                    scope : this,
                    disabled: true,
                    tooltip: {title: WtfGlobal.getLocaleText('pm.common.assigntoprojects'),text: WtfGlobal.getLocaleText('pm.Help.addtoproject')},
                    handler : this.addHandler,
                    iconCls : 'dpwnd addppl'
                },Wtf.UPerm.ManagePermission ? '-':'',{
                    text : WtfGlobal.getLocaleText('pm.admin.user.assignpermissions'),
                    id : "permissions"+this.id,
                    allowDomMove:false,
                    scope : this,
                    hidden: Wtf.UPerm.ManagePermission ? false : true,
                    disabled: true,
                    tooltip: {title: WtfGlobal.getLocaleText('lang.permissions.text'),text: WtfGlobal.getLocaleText('pm.Help.projectpermission')},
                    handler : function(){
                        if(this.selectionModel.getCount() > 1){
                            var uids = "";
                            var buf = this.selectionModel.getSelections();
                            for(var i = 0 ;i<buf.length;i++){
                                if(uids == ""){
                                    uids +=buf[i].data['userid'];
                                }else{
                                    uids += ","+buf[i].data['userid'];
                                }
                            }
                            this.requestPermissions(uids);
                        }
                        else {
                            this.requestPermissions(this.selectionModel.getSelected().data['userid']);
                        }
                    },
                    iconCls : 'pwnd permicon'
                },'-',{
                    text : WtfGlobal.getLocaleText('pm.common.makeannouncements'),
                    id : "uannounce"+this.id,
                    allowDomMove:false,
                    scope : this,
                    tooltip: {title: WtfGlobal.getLocaleText('lang.announcement.text'),text: WtfGlobal.getLocaleText('pm.Help.projectannounce')},
                    handler : function(){
                        if(this.selectionModel.getSelections().length == 0){
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.349'), function(btn){
                                if(btn == "yes"){
                                    this.fireEvent("announce", "User", "userid");
                                }
                            },this);
                        } else {
                            this.fireEvent("announce", "User", "userid");
                        }
                    },
                    iconCls : 'dpwnd announceicon'
                }]
            }),
            items:[this.UsergridPanel]
        });
        this.add(this.innerpanel);
        this.userds.on("load",function(dsObj, rs, opt) {
            hideMainLoadMask();
            var jdata = this.dataReader.jsonData;
            this.count = jdata.count;
            this.quickSearchTF.StorageChanged(this.userds);
            this.quickSearchTF.on('SearchComplete', this.QuickSearchComplete, this);
            this.fireEvent("dataRendered");
        },this);

        this.userds.on('datachanged', function(){
            this.quickSearchTF.setPage(this.pP.combo.value);
        }, this);
    },
    rowSelectionHandler : function(sm) {
        var addBtn = Wtf.getCmp("addto"+this.id);
        var perBtn = Wtf.getCmp("permissions"+this.id);
        var editUserBtn = Wtf.getCmp('edituser_'+this.id);
        var deleteUserBtn = Wtf.getCmp('removeuser_'+this.id);

        var useridsbuf = sm.getSelections();
        var flag = true;
        for (var cnt=0;cnt<useridsbuf.length;cnt++){
            var users = useridsbuf[cnt].data['userid'] ;
            if(users == loginid){
                flag = false;
                break;
            }
        }
        if(editUserBtn && deleteUserBtn){
            if(Wtf.UPerm.ManagePermission) {
                if(sm.getSelections().length>0 & flag){
                    perBtn.setDisabled(false);
                    deleteUserBtn.setDisabled(false);
                } else {
                    perBtn.setDisabled(true);
                    deleteUserBtn.setDisabled(true);
                }
                if(sm.getSelections().length == 1) {
                    editUserBtn.setDisabled(false);
                } else {
                    editUserBtn.setDisabled(true);
                }
            }
        } else {
            if(Wtf.UPerm.ManagePermission) {
                if(sm.getSelections().length>0 && flag){
                    perBtn.setDisabled(false);
                }
                else {
                    perBtn.setDisabled(true);
                }
            }
        }
        if(Wtf.UPerm.AssignUserToProject) {
            if(sm.getSelections().length == 1) {
                addBtn.setDisabled(false);

            } else {
                addBtn.setDisabled(true);
            }
        }
    },
    addHandler: function() {
        var addToBtn = Wtf.getCmp("addto" + this.id);
        if(addToBtn)
            addToBtn.disable();
        var selected = this.selectionModel.getSelections();
        var selIds = "";
        for (var i = 0; i < selected.length; i++)
            selIds += (selected.length>1)?selected[i].data['userid'] + ",":selected[i].data['userid'];
        this.Addto = new Wtf.AddtoWindow({
            wizard:false,
            usergrid: this.usergrid,
            parentid:this.id,
            userid:selIds
        }).show();
    },
    requestPermissions: function(rec, flag){
        this.handlePermissionBtn(false);
        var uid = rec;

        Wtf.Ajax.requestEx({
            url: '../../admin.jsp',
            params: {
                action: 0,
                mode: 3,
                uid: uid
            },
            method: 'POST'
        },
        this,
        function(request,response) {
            var pObj = eval("("+request+")");
            if(pObj.error && pObj.error == "creator"){
                msgBoxShow(["Info",pObj.data],0);
            }else{
                this.showPermissionsForm(request, flag, uid);
            }
            bHasChanged=true;
        },
        function() {
            this.handlePermissionBtn(true);
            bHasChanged=false;
        }
        );
    },

    handlePermissionBtn : function(enable) {
        var permBtn = Wtf.getCmp("permissions"+this.id);
        if(permBtn) {
            if(enable)
                permBtn.enable();
            else
                permBtn.disable();
        }
    },

    QuickSearchComplete: function(e){
        this.usergrid.getView().refresh();
    },

    GridRowdbClicked : function(Grid,rowIndex,e){
        var r = this.userds.getAt(rowIndex).data;
        mainPanel.loadTab("../../user.html", "tab"+r.userid,r.fname + " " + r.lname, "navareadashboard",Wtf.etype.user,true);
    },
    createGP: function(){
        this.panel.add(new Wtf.grid.GridPanel({
            id:'list' + this.id,
            store: this.listds,
            cm: this.listcm,
            sm : this.listsm,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true
            }
        }));
        this.panel.doLayout();
        this.listds.removeListener("load",this.createGP,this);
    },

    AddTo : function(wizard, userid){
        var buttonText = (wizard)?"Skip":WtfGlobal.getLocaleText('lang.cancel.text');
        var subTitle = (wizard)?"Step 3 of 3":"";
        this.AddQuickSearch = new Wtf.KWLQuickSearch({
            id : 'AddToSearch'+this.id,
            width: 200,
            field : "name",
            emptyText: WtfGlobal.getLocaleText('pm.common.searchbyname')
        });
        this.listds = new Wtf.data.JsonStore({
            id : "listds"+this.id,
            url: '../../admin.jsp?action=0&companyid=' + this.companyid+'&userid='+userid,
            root: 'data',
            method : 'GET',
            baseParams :{mode: 5},
            fields: ['id', 'name']
        });
        this.listds.on("load",this.createGP,this);
        this.listsm = new Wtf.grid.CheckboxSelectionModel();
        this.listcm = new Wtf.grid.ColumnModel([this.listsm,{
            header: WtfGlobal.getLocaleText('lang.name.text'),
            dataIndex: 'name',
            autoWidth : true,
            sortable: true,
            groupable: true
        }]);

        this.AddToWindow = new Wtf.Window({
            title : WtfGlobal.getLocaleText('lang.user.text'),
            closable : true,
            modal : true,
            iconCls : 'iconwin',
            width : 450,
            height: 450,
            resizable :false,
            buttonAlign : 'right',
            buttons :[{
                text : WtfGlobal.getLocaleText('lang.add.text'),
                id:'addUserToProjButton',
                id:'addUserToProjButton',
                scope: this,
                handler: function(){
                    if(this.listsm.getSelections().length > 0) {
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.354'), function(btn){
                            if (btn == "yes") {
                                Wtf.getCmp("addUserToProjButton").disable();
                                Wtf.getCmp("cancelUserToProjButton").disable();
                                this.addUsersTo(userid, wizard);
                            }
                        }, this);
                    } else
                        msgBoxShow(6, 1);
                }
            },{
                text : buttonText,
                scope : this,
                id:"cancelUserToProjButton",
                handler : function(){
                    this.AddToWindow.close();
                    if(wizard)
                        this.finishWindow();
                }
            }],
            layout : 'border',
            items :[{
                region : 'north',
                height : 75,
                border : false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                html : getTopHtml("Add User To Project As Member", subTitle, "../../images/createuser40_52.gif")
            },{
                region : 'center',
                border : false,
                bodyStyle : 'background:#f1f1f1;font-size:10px;',
                layout : 'fit',
                items :[this.panel = new Wtf.common.KWLListPanel({
                    paging : false,
                    autoLoad : false,
                    border : false,
                    title : WtfGlobal.getLocaleText('pm.project.text'),
                    autoScroll:true,
                    tbar: [ this.AddQuickSearch ],
                    bbar: new Wtf.PagingToolbar({
                        pageSize: 20,
                        store : this.listds,
                        displayInfo: false,
                        emptyMsg: "No topics to display"
                    })
                })]
            }]
        });
        /*
        Wtf.getCmp("dropdown" + this.id).on("select",function(combo, record, index){
            this.listds.load({
                params: {
                    mode: 4 + index,
                    start: 0,
                    limit: 30
                }
            });
            this.listds.on('load', function(){
                this.AddQuickSearch.StorageChanged(this.listds);
            }, this);
        },this);
*/
        this.listds.load({
            params: {
                start: 0,
                limit: 20
            }
        });
        this.listds.on('load', function(){
            this.AddQuickSearch.StorageChanged(this.listds);
            this.AddToWindow.show();
        }, this);
    },

    addUsersTo : function(userid, wizard){
        var userslist = "", featureidslist = "";
        var cnt;

        //  Get Selected Feature(Project/Community)
        var featureidsbuf = this.listsm.getSelections();
        for(cnt=0;cnt<featureidsbuf.length;cnt++){
            featureidslist += featureidsbuf[cnt].data.id + ","
        }
        featureidslist = featureidslist.substring(0,featureidslist.length - 1);

        if(userid !== undefined) {
            userslist = userid;
        } else {
            var useridsbuf = this.selectionModel.getSelections();
            for (cnt=0;cnt<useridsbuf.length;cnt++)
                userslist += useridsbuf[cnt].data.userid + ",";
            userslist = userslist.substring(0,userslist.length - 1);
        }

        Wtf.Ajax.requestEx({
            url: "../../admin.jsp",
            params: ({
                action: 1,
                userslist: userslist,
                featureidslist: featureidslist,
                companyid: this.companyid,
                feature: 0,
                mode: 0,
                emode: 3
            }),
            method: 'POST'
        },
        this,
        function(result, req){
            this.AddToWindow.close();
            if(result == "success" && userid === undefined){
                this.usergrid.getStore().load();
                msgBoxShow(7, 0);
            } else if (wizard)
                this.finishWindow();
        },
        function(){
            msgBoxShow(8, 1);
            this.AddToWindow.close();
            if (userid !== undefined)
                this.finishWindow();
        });
    },
    //  TODO: ravi: Form to assign custom roletypes or permissions to the users

    showPermissionsForm: function(response, wizard, uid){
        this.assignPermObj = eval('(' + response + ')');
        var tempobj = this.assignPermObj.data;
        var buf = "",fieldsetArr = "",fields = ",";
        var buttonText = (wizard)?"Skip":WtfGlobal.getLocaleText('lang.cancel.text');
        var text = WtfGlobal.getLocaleText('lang.set.text');
        var subTitle = (wizard)?"Step 2 of 3":"Set permissions for the user";

        //do not change name of checkbox or radio buttons
        for(var cnt=0;cnt<tempobj.length;cnt++){
                if(tempobj[cnt].featureName != buf){
                    buf = tempobj[cnt].featureName;
                    fieldsetArr += fields.substring(0,fields.length-1);
                    fieldsetArr += "]},{xtype: 'fieldset',collapsible:true,id: 'PermissionForm_" + tempobj[cnt].featureName + "',title:'" + WtfGlobal.getLocaleText('pm.permission.'+tempobj[cnt].featureName) + "', collapsed :"+(tempobj[cnt].permission ? false:true)+", autoHeight: true,items:[";
                    fields = "";
                }
                var tempobj2 = tempobj[cnt].activities;
                for(var count = 0 ; count < tempobj2.length ;count++){
                    if(tempobj2[count].subActivity) {
                        if(tempobj2[count].activityName != buf){
                            buf = tempobj2[count].activityName;
                            fieldsetArr += fields.substring(0,fields.length-1);
                            fieldsetArr += ",{xtype: 'fieldset', collapsible:true, id: 'PermissionForm_" + tempobj2[count].activityName + "',title:'" + WtfGlobal.getLocaleText('pm.permission.'+tempobj2[count].activityName) + "', collapsed :"+(tempobj2[count].permission ? false:true) +",autoHeight: true, items:[";
                            fields = "";
                        }
                        var tempobj3 = tempobj2[count].subActivities;
                        for(var counter = 0 ; counter < tempobj3.length ;counter++){
                                fields += "{xtype:'checkbox', tooltip: {text: '" +WtfGlobal.getLocaleText('pm.Help.perm' + tempobj3[counter].subActivityName) + "'}, labelStyle:'width: 200px; ',id:'PermissionForm_" + tempobj3[counter].subActivityName + "',fieldLabel:'" + WtfGlobal.getLocaleText('pm.permission.'+tempobj3[counter].subActivityName) + "',title:'" + tempobj3[counter].subActivityName + "',name:'" + tempobj3[counter].subActivityName + "',checked:"+ tempobj3[counter].permission +"},";
                        }
                        fieldsetArr += fields.substring(0,fields.length-1)+"]}";
                        fields = "";
                    }else{
                        fields += "{xtype:'checkbox', tooltip:{text: '"+WtfGlobal.getLocaleText('pm.Help.perm' +tempobj2[count].activityName)+"'}, labelStyle:'width: 200px;',id:'PermissionForm_" + tempobj2[count].activityName + "',fieldLabel:'" + WtfGlobal.getLocaleText('pm.permission.'+tempobj2[count].activityName) + "',title:'" + tempobj2[count].activityName + "',name:'" + tempobj2[count].activityName + "',checked:"+ tempobj2[count].permission +"},";
                    }
                }


        }
        fieldsetArr += fields.substring(0,fields.length-1);
        fieldsetArr = "[" + fieldsetArr.substring(3) + "]}]";

        this.permFormWin = new Wtf.Window({
            title : WtfGlobal.getLocaleText('pm.user.permission.text'),
            closable : true,
            modal : true,
            id : "permFormWin_" + this.id,
            iconCls : 'iconwin',
            width : 450,
            height: 550,
            resizable :false,
            buttonAlign : 'right',
            buttons :[{
                text : WtfGlobal.getLocaleText('lang.apply.text'),
                id:'applyUserPermButton',
                id:'applyUserPermButton',
                scope : this,
                handler:function(){
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.334'), function(btn){
                        if (btn == "yes") {
                            Wtf.getCmp("applyUserPermButton").disable();
                            Wtf.getCmp("skipUserPermButton").disable();
                            this.assignUserPermissions(wizard, uid);
                        }
                    }, this);
                }
            },{
                text : buttonText,
                id:'skipUserPermButton',
                scope : this,
                handler : function(){
                    this.handlePermissionBtn(true);
                    this.permFormWin.close();
                    if(buttonText!=WtfGlobal.getLocaleText('lang.cancel.text')){
                        this.userds.reload();
                        if(wizard){
                            new Wtf.AddtoWindow({
                                wizard:true,
                                usergrid: this.usergrid,
                                parentid:this.id,
                                userid:uid
                            }).show();
                        }
                    }
                }
            }],
            layout : 'border',
            items :[{
                region : 'north',
                height : 75,
                border : false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                html : getTopHtml(WtfGlobal.getLocaleText('pm.admin.user.assignperm.header'), WtfGlobal.getLocaleText('pm.admin.user.assignperm.subheader'), "../../images/userpermission40_52.gif")
            },{
                region : 'center',
                border : false,
                id : 'aperm' + this.companyid,
                autoScroll: true,
                bodyStyle : 'background:#f1f1f1;font-size:10px;',
                layout : 'fit',
                items :[this.permForm = new Wtf.form.FormPanel({
                    url: '../../admin.jsp?action=1&mode=4&companyid=' + this.companyid,
                    id: "apermform" + this.companyid,
                    layout: "form",
                    waitMsgTarget: true,
                    method : 'POST',
                    border : false,
                    labelWidth: 250,
                    cls: "scrollform",
                    defaultType: 'textfield',
                    items: eval('(' + fieldsetArr + ')')
                })]
            }]
        });

        this.permFormWin.on("close" , function(panel) {
            var MainPanel = Wtf.getCmp(this.id.split("permFormWin_")[1]);
            MainPanel.handlePermissionBtn(true);
        });

        this.permFormWin.show();
        var allProj = this.permFormWin.findById('PermissionForm_AccessAllProjects');
        var assignProj = this.permFormWin.findById('PermissionForm_AccessOnlyAssigned');
        var eventName = "";
        if(Wtf.isWebKit){
            eventName = "check";
        }else{
            eventName = "focus";
        }
        allProj.on(eventName, function(){
            if(assignProj.getValue() == true){
                assignProj.setValue(false);
            }
        });
        assignProj.on(eventName, function(){
            if(allProj.getValue()==true){
                allProj.setValue(false);
            }
        });
    },

    assignUserPermissions: function(wizard, userid){
        var userslist = "";
        var cnt;
        var isitme = false;
        if(wizard){
            userslist = userid;
        } else {
            var useridsbuf = this.selectionModel.getSelections();
            for (cnt=0;cnt<useridsbuf.length;cnt++)
                userslist += useridsbuf[cnt].data['userid'] + ",";
            userslist = userslist.substring(0,userslist.length - 1);
        }

        isitme = (userslist.indexOf(loginid) !== -1) ? true : false;
        this.permForm.form.submit({
            url: "../../admin.jsp",
            params: {
                users: userslist,
                action: 1,
                mode: 0,
                emode: 4
            },
            waitMsg: WtfGlobal.getLocaleText("pm.loading.text")+'...',
            scope : this,
            failure: function(a,b){
                this.handlePermissionBtn(true);
                this.permFormWin.close();
                if(wizard){
                    new Wtf.AddtoWindow({
                        wizard:true,
                        usergrid: this.usergrid,
                        parentid:this.id,
                        userid:userid
                    }).show();
                //this.AddTo(true, userid);
                } else {
                    var obj = eval("(" + b.response.responseText + ")");
                    var msg = (obj.data == "") ? "Permissions Not Applied" : obj.data;
                    msgBoxShow([WtfGlobal.getLocaleText('pm.common.failure'), msg], 3);
                }
            },
            success: function(){
                this.handlePermissionBtn(true);
                this.permFormWin.close();
                if(wizard){
                    new Wtf.AddtoWindow({
                        wizard:true,
                        parentid:this.id,
                        userid:userid
                    }).show();
                //this.AddTo(true, userid);
                } else {
                    if(isitme){
                        msgBoxShow(259, 0);
                        var task = new Wtf.util.DelayedTask(function(){
                            location.reload(true);
                        }, this);
                        task.delay(3000);
                    } else {
                        msgBoxShow(9, 0);
                        this.userds.reload();
                    }
                }
            }
        });
    },

    createEditUser: function(btn, e){
        var grid = this.usergrid;
        var rec = grid.getSelectionModel().getSelected();
        var newResForm = null;
        var text = 'Edit', texted = 'edited', disabled = true, action = 10;
        if(btn.btnPurpose == 'create'){
            text = 'Create';
            texted = 'created'
            disabled = false;
            action = 2;
        }
        var getValueByID = function(id){
            return newResForm.findById(id).getValue();
        }
        var setValueByID = function(id, value){
            return newResForm.findById(id).setValue(value);
        }
        var getConfigByID = function(id){
            return newResForm.findById(id);
        }
        this.newUser = new Wtf.Window({
            title: text+" User",
            layout: "border",
            resizable: false,
            iconCls : 'iconwin',
            modal: true,
            autoScroll : true,
            height: 400,
            width: 440,
            items: [{
                region: "north",
                height : 75,
                border : false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                html: getTopHtml(text+' User',text+' User'+"<br><span style=\"float:right; \">( * indicates required fields )</span>","../../images/createuser40_52.gif")
            },{
                region: "center",
                border: false,
                bodyStyle : 'background:#f1f1f1;font-size:10px;',
                layout: "fit",
                items:[
                    newResForm = new Wtf.form.FormPanel({
                    labelWidth: 120,
                    autoScroll : true,
                    defaults: {width: 220},
                    method : 'POST',
                    fileUpload: true,
                    defaultType: "textfield",
                    bodyStyle: "padding:20px;",
                    items:[{
                        fieldLabel: 'User Name*',
                        name: 'username',
                        id: 'username',
                        allowBlank: false,
                        disabled: disabled,
                        validator:WtfGlobal.validateUserid,
                        maxLength : 36
                    },{
                        fieldLabel: 'Email Address*',
                        name: 'emailid',
                        id: 'emailid',
                        allowBlank: false,
                        maxLength: 100,
                        validator: WtfGlobal.validateEmail
                    },{
                        fieldLabel: 'First Name*',
                        name: 'fname',
                        id: 'fname',
                        allowBlank: false,
                        validator: WtfGlobal.validateUserName,
                        maxLength: 50
                    },{
                        fieldLabel: 'Last Name*',
                        name: 'lname',
                        id: 'lname',
                        allowBlank: false,
                        validator:WtfGlobal.validateUserName,
                        maxLength : 50
                    },{
                        fieldLabel: 'Contact Number',
                        id: 'contactno',
                        name: 'contactno',
                        validator: WtfGlobal.validatePhone,
                        maxLength : 15
                    },{
                        id: 'address',
                        fieldLabel: 'Address',
                        name : 'address',
                        maxLength : 100/*,
                            value: params.address*/
                    },{
                        xtype : 'hidden',
                        name: 'userid',
                        id: 'userid',
                        value: rec && btn.btnPurpose !== 'create' ? rec.get('userid') : ''
                    },{
                        xtype : 'hidden',
                        name: 'companyid',
                        id: 'companyid',
                        value: companyid
                    }]
                })]
            }],
            buttons:[{
                text: text,
                scope: this,
                handler: function(){
                    btn.disable();
                    if(!newResForm.form.isValid()){
                        btn.enable();
                        return ;
                    }
                    if(action == 2){
                        if(this.userds.find("username",newResForm.form.items.items[0].getValue())!=-1){
                            btn.enable();
                            msgBoxShow(171, 1);
                            return;
                        }
                        if(this.userds.find("emailid",newResForm.form.items.items[1].getValue())!=-1){
                            btn.enable();
                            msgBoxShow(172, 1);
                            return;
                        }
                    }
                    var data = '{"username":"'+getValueByID('username')+'", "fname":"'+getValueByID('fname')+'", "lname":"'
                     +getValueByID('lname')+'", "email":"'+getValueByID('emailid')+'", "companyid":"'+companyid+'", "contact":"'
                     +getValueByID('contactno')+'", "address":"'+getValueByID('address')+'", "remoteapikey":"krawler'
                     +'", "userid":"'+getValueByID('userid')+'"}';

                    Wtf.Ajax.request({
                        url: '../../remoteapi.jsp',
                        params: {
                            data: data,
                            action: action
                        },
                        scope: this,
                        success: function(res, req){
                            this.newUser.close();
                            btn.enable();
                            var resobj = eval( "(" + res.responseText.trim() + ")" );
                            if(resobj.success && (resobj.infocode == 'm05' || resobj.infocode == 'm11')) {
                                this.usergrid.getStore().reload();
                                bHasChanged = true;
                                var temp = refreshDash.join();
                                if(temp.indexOf("all") == -1)
                                    refreshDash[refreshDash.length] = 'all';
                                var succMess = "User "+texted+" successfully";
                                msgBoxShow(["Success",succMess], 0);
                            } else {
                                msgBoxShow(174, 1);
                            }
                        },
                        failure: function(action, res){
                            btn.enable();
                            this.newUser.close();
                            msgBoxShow(174, 1);
                        }
                    });
                }
            },{
                text: "Cancel",
                scope: this,
                handler: function(){
                    this.newUser.close();
                }
            }]
        });
        if(rec && btn.btnPurpose !== 'create'){
            setValueByID('username', rec.get('username'));
            setValueByID('fname', rec.get('fname'));
            setValueByID('lname', rec.get('lname'));
            setValueByID('contactno', rec.get('contactno'));
            setValueByID('address', rec.get('address'));
            setValueByID('emailid', rec.get('emailid'));
        }
        getConfigByID('contactno').on("change", function(){
            getConfigByID('contactno').setValue(WtfGlobal.HTMLStripper(getValueByID('contactno')));
        });
        getConfigByID('address').on("change", function(){
            getConfigByID('address').setValue(WtfGlobal.HTMLStripper(getValueByID('address')));
        });
        focusOn(!rec ? 'username' : 'emailid');
        this.newUser.show();
    },

    deleteUser: function(btn, e){
        var grid = this.usergrid;
        var recs = grid.getSelectionModel().getSelections();
        if(recs && recs.length > 0){
            Wtf.MessageBox.confirm("Confirm", "Are you sure you want to remove this user(s) from the system?", function(btn){
                if(btn == 'yes'){
                    var data = '{"userid":"', ids = "";
                    for(var i = 0; i < recs.length; i++){
                        var rec = recs[i];
                        ids += rec.get('userid') + '",';
                    }
                    ids += ' "remoteapikey":"krawler"';
                    data += ids + "}";
                    Wtf.Ajax.request({
                        url: '../../remoteapi.jsp',
                        params:{
                            action: 4,
                            data: data
                        },
                        scope: this,
                        success: function(resp, req){
                            var obj = eval( "(" + resp.responseText.trim() + ")" )
                            if(obj.success){
                                this.usergrid.getStore().reload();
                                bHasChanged = true;
                                var temp = refreshDash.join();
                                if(temp.indexOf("all") == -1)
                                    refreshDash[refreshDash.length] = 'all';
                                msgBoxShow(['Success', 'User removed from the application successfully.'], 0);
                            }
                        },
                        failure: function(resp, req){
                            msgBoxShow(4, 0);
                        }
                    });
                }
            }, this);
        }
    }
});
/*****  User Administration   *****/

/*****  Project Administration   *****/
Wtf.common.adminpageproject = function(config){
    Wtf.common.adminpageproject.superclass.constructor.call(this,config);

    this.addEvents({
        "announce": true
    });
};
Wtf.extend(Wtf.common.adminpageproject,Wtf.Panel,{
    onRender : function(config){
        Wtf.common.adminpageproject.superclass.onRender.call(this,config);
        this.projectManagementPerm = (Wtf.UPerm.CreateProject || Wtf.UPerm.EditProject || Wtf.UPerm.DeleteProject || Wtf.UPerm.ManageMembers)?true:false;
        this.projRecordConfig = [{name: 'image'},{name:'moderator'},{name: 'projectid'},
             {name: 'projectname'},{name: 'updatedon'},{name: 'count'},
             {name: 'status'},{name: 'createdon',type: 'date',dateFormat: 'Y-m-j H:i:s'},
             {name: 'description'},{name: 'archived'},
             {name: 'health'}
         ];
        this.selectionModel = new Wtf.grid.CheckboxSelectionModel();
        this.cmConfig =[this.selectionModel,{
            dataIndex: 'image',
            width : 30,
            fixed : true,
            renderer : function(value){
                if(value == "")
                    value = "../../images/projectimage.jpg";
                return String.format("<img src='{0}' style='height:18px;width:18px;vertical-align:text-top;'/>",value);
            }
        },{
            header: WtfGlobal.getLocaleText('pm.project.text'),
            dataIndex: 'projectname',
            autoWidth : true,
            sortable: true,
            groupable: true
        },{
            header :WtfGlobal.getLocaleText('lang.description.text'),
            dataIndex: 'description',
            autoSize : true,
            sortable: true,
            groupable: true
        },{
            header :WtfGlobal.getLocaleText('pm.admin.project.members'),
            dataIndex: 'count',
            autoSize : true,
            sortable: true,
            groupable: true,
            width:30
        },{
            header :WtfGlobal.getLocaleText('pm.common.createdon'),
            dataIndex: 'createdon',
            //           renderer:Wtf.util.Format.dateRenderer('Y-m-d h:i a'),
            renderer: WtfGlobal.userPrefDateRenderer,
            autoSize : true,
            sortable: true,
            groupable: true
        },{
            header: WtfGlobal.getLocaleText('pm.common.healthstatus'),
            dataIndex:"health",
            renderer:function(val){
                if(val==1) return "<div><img src='../../images/health_status/ontime.gif' style='vertical-align:text-bottom'/> "+WtfGlobal.getLocaleText('pm.project.home.health.ontime')+"</div>";
                else if(val==2) return "<div><img src='../../images/health_status/slightly.gif' style='vertical-align:text-bottom'/>"+WtfGlobal.getLocaleText('pm.project.home.health.slightly')+"</div>";
                else if(val==3) return "<div><img src='../../images/health_status/gravely.gif' style='vertical-align:text-bottom'/>"+WtfGlobal.getLocaleText('pm.project.home.health.gravely')+"<div>";
                else return "<div><img src='../../images/health_status/else_status.gif' style='vertical-align:text-bottom'/>Future Task<div>";
            }
        },{
            header: WtfGlobal.getLocaleText('lang.status.text'),
            dataIndex: 'archived',
            autoWidth : true,
            hidden:true,
            sortable: true,
            groupable: true,
            renderer: function(value) {
                if(value)
                    return WtfGlobal.getLocaleText('pm.cr.project.archived');
                else
                    return WtfGlobal.getLocaleText('lang.active.text');
            }
        }];
        this.ProjectgridPanel  = new Wtf.common.KWLListPanel({
            id : "projectgridpanel"+this.id,
            title:WtfGlobal.getLocaleText('pm.admin.project.info'),
            autoLoad : false,
            paging : false,
            layout : 'fit'
        });
         this.createGrid();
         projAdminChange=false;
         this.on("activate",this.changeonselect,this);
    },
    changeonselect : function(){
      if(projAdminChange){
          this.remove(this.projectsgrid);
          this.createGrid();
          this.doLayout();
      }
    },
    createGrid: function(){

        this.count = 0;
        this.projPermManage = Wtf.UPerm.Project;
        this.projPermManageMember = Wtf.UPerm.ManageMembers;
        var recConfig = getStoreFields(this.projRecordConfig) //add custom config
        this.projRec = new Wtf.data.Record.create(recConfig);

        var grpView = new Wtf.grid.GroupingView({
            forceFit: true,
            showGroupName: false,
            enableGroupingMenu: false,
            hideGroupedColumn: false,
            emptyText: '<div class="emptyGridText">'+WtfGlobal.getLocaleText('pm.project.grid.emptytext')+'<br><a href="#" onClick=\'createNewProject(\"'+this.id+'\")\'>'+WtfGlobal.getLocaleText('pm.project.grid.starttext')+'</a></div>'
        });

        this.dataReader = new Wtf.data.KwlJsonReader({
            totalProperty: 'count',
            root: "data"
        },this.projRec);

        this.projds = new Wtf.data.GroupingStore({
            id : "projds"+this.id,
            url: '../../admin.jsp?action=0&mode=2&userid=' + loginid,//mode 2 indicates project
            root: 'data',
            reader: this.dataReader,
            method : 'GET',
            sortInfo: {
                field: 'archived',
                direction: "DESC"
            }
        });
        //add additional config for custom column
        var cfg =getCcColModel(this.cmConfig,"Project")
        var viewCol = {
            header:WtfGlobal.getLocaleText('lang.view.text'),
            dataIndex:'projectid',
            width:50,
            renderer: function(val,meta,record,rowIndex){

                return "<a id = 'viewProject"+this.id+"' href=\"#\" wtf:qtip="+WtfGlobal.getLocaleText('pm.project.open.qtip')+" onclick=\"viewProject(\'"+val+"\',\'"+record.get('projectname')+"\')\"><img src='../../images/YellowOpenedFolder.gif'/></a>"
            }
        };
        cfg[cfg.length] = viewCol;
        this.cm= new Wtf.grid.ColumnModel(cfg);
        this.projectsgrid = new Wtf.grid.GridPanel({
            id:'projectsgrid'+this.id,
            store: this.projds,
            cm: this.cm,
            sm : this.selectionModel,
            border : false,
            loadMask : true,
            view: grpView,
            viewConfig: {
                forceFit:true
            }
        });
        this.projectsgrid.on("rowdblclick",this.editButtonClicked ,this);
        this.projectsgrid.getSelectionModel().addListener('selectionchange', this.rowSelectionHandler, this);
        this.projds.load({
            params: {
                start:0,
                limit:15
            },
            scope: this,
            callback: function(rec, opt, succ){
                if(succ)
                    this.projds.groupBy("archived");
            }
        });
      this.ProjectgridPanel.add(this.projectsgrid);
      this.innerpanel = new Wtf.Panel({
            id : 'innerpanel'+this.id,
            layout : 'fit',
            cls : 'backcolor',
            border : false,
            tbar:[/*'Quick Search: ',*/
            this.quickSearchTF = new Wtf.KWLTagSearch({
                id : 'project'+this.id,
                width: 200,
                field : "projectname",
                emptyText: WtfGlobal.getLocaleText('pm.admin.project.searchbyprojectname')
            })],
            bbar:new Wtf.PagingSearchToolbar({
                pageSize: 15,
                id : "paggintoolbar"+this.id,
                store: this.projds,
                searchField: this.quickSearchTF,
                displayInfo: false,
//                displayMsg: '',
                emptyMsg: '',
                plugins : this.pP = new Wtf.common.pPageSize({id : "pPageSize_"+this.id}),
                items :['->',{
                    id : 'importfrombasecamp',
                    text : WtfGlobal.getLocaleText('pm.admin.project.importfrombasecamp'),
                    hidden : (Wtf.UPerm.CreateProject)?false:true,
                    iconCls: 'dpwnd importbasecamp',
                    tooltip: {title:WtfGlobal.getLocaleText('pm.project.basecamp.import.tip'),text: WtfGlobal.getLocaleText('pm.Help.importfrombasecamp')},
                    handler: function() {
                        importFromBaseCamp(this.projds);
                    },
                    scope: this
                },(Wtf.UPerm.CreateProject)?'-':'',new Wtf.Toolbar.Button({
                    text:WtfGlobal.getLocaleText('pm.common.projectmanagement'),
                    id : 'manageuser',
                    tooltip: {title:WtfGlobal.getLocaleText('pm.common.projectmanagement'),text: WtfGlobal.getLocaleText('pm.Help.manageuser')},
                    scope:this,
                    hidden: (this.projectManagementPerm)?false:true,
                    menu:[{
                        text : WtfGlobal.getLocaleText('pm.admin.project.createproject'),
                        id : "createproj"+this.id,
                        disabled: (Wtf.UPerm.CreateProject)?false:true,
                        allowDomMove:false,
                        scope : this,
                        tooltip: {title: WtfGlobal.getLocaleText('pm.project.text'),text: WtfGlobal.getLocaleText('pm.Help.newproject')},
                        handler : this.createButtonClicked,
                        iconCls : 'dpwnd quickaddproject'
                    },{
                        text : WtfGlobal.getLocaleText('pm.admin.project.editproject'),
                        id : "editproj"+this.id,
                        allowDomMove:false,
                        scope : this,
                        disabled : true,
                        tooltip: {title: WtfGlobal.getLocaleText('pm.admin.project.editproject'),text: WtfGlobal.getLocaleText('pm.Help.editproject')},
                        handler : this.editButtonClicked,
                        iconCls : 'dpwnd editicon'
                    },{
                        text : WtfGlobal.getLocaleText('pm.admin.project.deleteproject'),
                        id : "delproj"+this.id,
                        allowDomMove:false,
                        scope : this,
                        disabled : true,
                        tooltip: {title: WtfGlobal.getLocaleText('pm.admin.project.deleteproject'),text: WtfGlobal.getLocaleText('pm.Help.deleteproject')},
                        handler : this.delButtonClicked,
                        iconCls : 'pwnd delicon'
                    },{
                        text: WtfGlobal.getLocaleText('pm.admin.project.managemembers'),
                        id: "manageMembers" + this.id,
                        disabled : true,
                        scope: this,
                        tooltip: {title: WtfGlobal.getLocaleText('pm.admin.project.managemembers'),text: WtfGlobal.getLocaleText('pm.Help.manageuser')},
                        handler: this.manageMembers,
                        iconCls: 'dpwnd addppl'
                    }]
                }),(this.projectManagementPerm)?'-':'',{
                    text: WtfGlobal.getLocaleText('lang.archive.text'),
                    archived: true,
                    id: "archive" + this.id,
                    allowDomMove: false,
                    scope: this,
                    iconCls: "pwnd archivedProjIcon",
                    disabled: true,
                    hidden: (Wtf.UPerm.ArchiveActive)?false:true,
                    archive: true,
                    tooltip: {title:WtfGlobal.getLocaleText('lang.archive.text'),text: WtfGlobal.getLocaleText('pm.Help.projectarchive')},
                    handler: this.setArchived
                },{
                    text: WtfGlobal.getLocaleText('pm.admin.project.setactive'),
                    archived: false,
                    id: "unarchive" + this.id,
                    iconCls: "dpwnd projectTabIcon",
                    allowDomMove: false,
                    scope: this,
                    hidden : (Wtf.UPerm.ArchiveActive)?false:true,
                    archive: false,
                    disabled: true,
                    tooltip: {title:WtfGlobal.getLocaleText('pm.admin.project.setactive'),text: WtfGlobal.getLocaleText('pm.Help.projectactive')},
                    handler: this.setArchived
                },(Wtf.UPerm.ArchiveActive)?'-':'',{
                    text : WtfGlobal.getLocaleText('pm.common.makeannouncements'),
                    id : "pannounce"+this.id,
                    allowDomMove:false,
                    scope : this,
                    tooltip: {title: WtfGlobal.getLocaleText('lang.announcement.text'),text: WtfGlobal.getLocaleText('pm.Help.projannounce')},
                    handler : function(){
                        if(this.selectionModel.getSelections().length == 0)
                            msgBoxShow(10, 1);
                        else
                            this.fireEvent("announce", WtfGlobal.getLocaleText('pm.project.text'), "projectid");
                    },
                    iconCls : 'dpwnd announceicon'
                }]
            }),
            items:[this.ProjectgridPanel]
        });
        this.add(this.innerpanel);
        this.projds.on("load",function(){
            hideMainLoadMask();
            var jdata = this.dataReader.jsonData;
            this.count = jdata.count;
            this.quickSearchTF.StorageChanged(this.projds);
            this.quickSearchTF.on('SearchComplete', this.QuickSearchComplete, this);
            if(!this.searchPerformed){
                if(this.projds.getCount() == 0){
                    this.quickSearchTF.setDisabled(true);
                    this.searchPerformed = false;
                }
            }
        },this);
        this.projds.on('datachanged', function(){
            this.quickSearchTF.setPage(this.pP.combo.value);
            this.emptyTextChange();
        }, this);
    },
//    addCustomReports: function(){
//
//        var reportBuilder = Wtf.getCmp("tabreportbuilder");
//        if(!reportBuilder){
//            reportBuilder = new Wtf.cc.CustomReportPanel({
//                title:"Custom Report Builder",
//                closable:true,
//                closeAction:'close',
//                layout: 'border',
//                autoDestroy:true,
//                id : "tabreportbuilder"
//            })
//            Wtf.getCmp("subtabpanelmainAdmin").add(reportBuilder);
//            Wtf.getCmp("subtabpanelmainAdmin").doLayout();
//        }
//        Wtf.getCmp("subtabpanelmainAdmin").setActiveTab(reportBuilder);
//    },
//    openReportsClick: function(){
//       var reportWin =new Wtf.ReportListWin({
//            width:450,
//            autoHeight:true
//        });
//        reportWin.on('onrunreport', function(win, rec){
//            var rep = getReportPanel(rec, {
//                id: 'admin',
//                searchable: true,
//                exportable: true,
//                showFilters: true,
//                customPageSize: 15,
//                showTitle: true
//            });
//            win.close();
//            Wtf.getCmp("subtabpanelmainAdmin").add(rep);
//            Wtf.getCmp("subtabpanelmainAdmin").doLayout();
//            Wtf.getCmp("subtabpanelmainAdmin").setActiveTab(rep);
//        }, this);
//        reportWin.show();
//    },
    manageMembers: function(btnObj){
        btnObj.disable();
        var flag = false;
        var prjrec = this.selectionModel.getSelected();
        var imgsrc = "../../images/managemembers40_52.gif";
        var availableds = new Wtf.data.Store({
            url:'../../admin.jsp',
            reader: new Wtf.data.KwlJsonReader({root:'data'},['username', 'userid', 'status'] ),
            autoLoad : false
        });
        var availablesm = new Wtf.grid.CheckboxSelectionModel();
        var availablecm = new Wtf.grid.ColumnModel([availablesm, {
            header: WtfGlobal.getLocaleText('pm.members.available'),
            dataIndex: 'username',
            autoWidth: true,
            sortable: true,
            groupable: true
        }
        //            ,{
        //                dataIndex: "userid",
        //                hidden: true,
        //                  resizable:false,
        //                  fixed:true,
        //                width:1
        //            },{
        //                dataIndex: "status",
        //                hidden: true,
        //                width:1
        //            }
        ]);
        var availablegrid = new Wtf.grid.GridPanel({
            store: availableds,
            cm: availablecm,
            sm : availablesm,
            border: false,
            loadMask: {
                msg: WtfGlobal.getLocaleText("pm.loading.text") + '...'
            },
            viewConfig: {
                forceFit: true
            }
        });
        availableds.on("loadexception", function(){
            if (!flag){
                msgBoxShow(11, 1);
                var _cmp = Wtf.getCmp("ManageProjectMembers");
                if(_cmp !== undefined)
                    _cmp.close();
                flag = true;
            }
        });
        availableds.load({
            params:{
                projid: prjrec.data["projectid"],
                action: 0,
                mode: 17
            }
        });
        var selectedds = new Wtf.data.Store({
            url:'../../admin.jsp',
            reader: new Wtf.data.KwlJsonReader({root:'data'},['username', 'userid', 'status']),
            autoLoad : false
        });
        var selectedsm = new Wtf.grid.CheckboxSelectionModel();
        var selectedcm = new Wtf.grid.ColumnModel([selectedsm, {
            header: WtfGlobal.getLocaleText('pm.members.allocated'),
            dataIndex: 'username',
            autoWidth: true,
            sortable: true,
            groupable: true
        }
        //            ,{
        //                dataIndex: "userid",
        //                hidden: true
        //            },{
        //                dataIndex: "status",
        //                hidden: true
        //            }
        ]);
        var selectedgrid = new Wtf.grid.GridPanel({
            store: selectedds,
            cm: selectedcm,
            sm : selectedsm,
            border: false,
            loadMask: {
                msg: WtfGlobal.getLocaleText("pm.loading.text") + '...'
            },
            viewConfig: {
                forceFit: true
            }
        });
        selectedds.on("load", function(obj, recs){
            var gview = selectedgrid.getView();
            for(var cnt=0; cnt < recs.length; cnt++){
                if(recs[cnt].data['status'] == 4)
                    Wtf.get(gview.getRow(cnt)).addClass("parentRow");
            }
        }, this);
        selectedds.on("loadexception", function(){
            if(!flag){
                msgBoxShow(11, 1);
                var _cmp = Wtf.getCmp("ManageProjectMembers");
                if(_cmp !== undefined)
                    _cmp.close();
                flag = true;
            }
        });
        selectedgrid.on("sortchange",function(th){
            var gview =th.getView();
            var sm=th.getStore();
            for(var cnt=0; cnt < sm.getCount(); cnt++){
                if(sm.getAt(cnt).get('status') == 4)
                    Wtf.get(gview.getRow(cnt)).addClass("parentRow");
            }
        });
        selectedds.load({
            params:{
                projid: prjrec.data["projectid"],
                action: 0,
                mode: 16
            }
        });
        var manageWin = new Wtf.manageWindow({
            headerCont:getTopHtml(WtfGlobal.getLocaleText('pm.admin.project.managemembers'),WtfGlobal.getLocaleText({key:'pm.project.membermanage.tophtml',params:[prjrec.data["projectname"]]}),imgsrc),
            selectedgrid: selectedgrid,
            title:WtfGlobal.getLocaleText('pm.common.projectmanagement'),
            id: "ManageProjectMembers",
            availablegrid: availablegrid
        });
        manageWin.on("okclicked", this.updateMembers, this);
        manageWin.on("beforeMoveRight", function(selRecs){
            for(var cnt=0; cnt < selRecs.length; cnt++){
                if(selRecs[cnt].data["status"] == 4){
                    msgBoxShow(12, 1);
                    return false;
                }
            }
            return true;
        }, this);
        manageWin.on("close", function(){
            btnObj.enable();
        }, this);
        manageWin.show();
    },

    updateMembers: function(window, availableStore, selectedStore){
        var jstr = "";
        var recs = selectedStore.data.items;
        for(var cnt=0; cnt<recs.length; cnt++){
            jstr += Wtf.encode(recs[cnt].data) + ","
        }
        jstr = "{\"data\":[" + jstr.substring(0,(jstr.length -1)) + "]}";
        Wtf.Ajax.requestEx({
            url: "../../admin.jsp",
            params: {
                action: 1,
                lid: loginid,
                mode: 2,
                emode: 4,
                data: jstr,
                projectid: this.selectionModel.getSelected().data["projectid"]
            }
        },
        this,
        function(response, requset){
            msgBoxShow(13, 0);
            window.close();
            this.projds.reload();
            this.projectsgrid.getView().refresh();
            bHasChanged = true;
            var temp = refreshDash.join();
            if(temp.indexOf("all") == -1 && temp.indexOf('qkl') == -1)
                refreshDash[refreshDash.length] = 'qkl';
        },
        function(response, requset){
            msgBoxShow(14, 1);
            window.close();
        });
    },

    rowSelectionHandler : function(sm) {
        if(this.projectManagementPerm || Wtf.UPerm.ArchiveActive) {
            var delBtn = Wtf.getCmp("delproj"+this.id);
            var annBtn = Wtf.getCmp("pannounce"+this.id);
            var archBtn = Wtf.getCmp("archive"+this.id);
            var unarchBtn = Wtf.getCmp("unarchive"+this.id);
            var eBtn = Wtf.getCmp("editproj"+this.id);
            //            var mmBtn = Wtf.getCmp("manageMembers"+this.id);
            if(sm.getSelections().length==1 && !sm.getSelected().data.archived && Wtf.UPerm.EditProject){
                eBtn.setDisabled(false);
            }
            else {
                eBtn.setDisabled(true);
            }

            var selected = sm.getSelections();
            if(selected.length>0){
                var temp = 0;
                for(var cnt = 0; cnt < selected.length; cnt++){
                    if(selected[cnt].data.archived)
                        temp++;
                }
                if(temp == selected.length){
                    unarchBtn.enable();
                    delBtn.disable();
                    annBtn.disable();
                    archBtn.disable();
                } else if(temp == 0){
                    unarchBtn.disable();
                    if(Wtf.UPerm.DeleteProject)
                        delBtn.enable();
                    annBtn.enable();
                    archBtn.enable();
                } else {
                    unarchBtn.disable();
                    delBtn.disable();
                    annBtn.disable();
                    archBtn.disable();
                }
            }
            else {
                delBtn.setDisabled(true);
                unarchBtn.disable();
                delBtn.disable();
                annBtn.disable();
                archBtn.disable();
            }

        if(Wtf.UPerm.ManageMembers) {
            var mmBtn = Wtf.getCmp("manageMembers"+this.id);

            if(sm.getSelections().length==1 && !sm.getSelected().data.archived  && ( (!Wtf.UPerm.AccessAllProjects && sm.getSelected().data.moderator) || (Wtf.UPerm.AccessAllProjects))) {
                mmBtn.setDisabled(false);
            } else {
                mmBtn.setDisabled(true);
            }
        }
        }
    },

    setArchived : function(obj) {
        var selected = this.selectionModel.getSelections();
        var pids = "";
        for(var i = 0; i < selected.length; i++){
            pids += selected[i].data.projectid + ",";
        }
        pids.substring(0, pids.length - 1)
        var action = "Active";
        var act = WtfGlobal.getLocaleText("lang.active.text");
        if(obj.archived){
            action = "Archive";
            act = WtfGlobal.getLocaleText('lang.archive.text');
        }
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText({key:'pm.msg.project.set',params:[act]}), function(btn){
            if (btn == "yes") {

                Wtf.Ajax.requestEx({
                    method: 'POST',
                    url: Wtf.req.prj + "archive.jsp",
                    params: {
                        action: action,
                        mode : '1',
                        projid: pids,
                        userid: loginid
                    }
                }, this,
                function(request, response){
                    var resobj = eval("(" +request.trim() + ")");
                    if(resobj.success) {
                        var msg = WtfGlobal.getLocaleText('pm.msg.project.archieved');
                        if(resobj.res == "0"){
                            msg = WtfGlobal.getLocaleText('pm.msg.project.activated');
                        }
                        msgBoxShow([WtfGlobal.getLocaleText('lang.success.text'), msg], 0);
                        this.projds.reload();
                        collectProjectData();
                        bHasChanged=true;
                        if(refreshDash.join().indexOf("all") == -1)
                            refreshDash[refreshDash.length] = 'all';
                    } else {
                        msgBoxShow([WtfGlobal.getLocaleText('pm.common.failure'), resobj.res], 1);
                    }
                },
                function(request, response){
                    msgBoxShow(20, 1);
                });
            }
        }, this);
    },

    createButtonClicked : function() {
        Wtf.getCmp("createproj" + this.id).disable();
        this.mode = 0;
        if(!Wtf.getCmp('createProjectWin')){
            this.createprojectWindow = new Wtf.createProject({
                mode: this.mode,
                successHandler: this.createProjectSuccess.createDelegate(this),
                failureHandler: this.createProjectFailure.createDelegate(this),
                data: {}
            });
            this.createprojectWindow.on("close", this.createProjectWindowClose, this);
            this.createprojectWindow.cancelBtn.on('click', this.createProjectWindowClose, this);
            this.createprojectWindow.show();
        }
        focusOn("projectname");
    },

    showWindow: function(healthdata){

        var buf = this.selectionModel.getSelected().data;
        Wtf.getCmp("editproj" + this.id).disable();
        buf['base'] = healthdata.base;
        buf['ontime'] = healthdata.ontime;
        buf['slightly'] = healthdata.slightly;
        buf['gravely'] = healthdata.gravely;
        this.mode = 1;
        if(!Wtf.getCmp('createProjectWin')){
            this.editprojectWindow = new Wtf.createProject({
                mode: this.mode,
                successHandler: this.createProjectSuccess.createDelegate(this),
                failureHandler: this.createProjectFailure.createDelegate(this),
                data: buf
            });
            this.editprojectWindow.on("close", this.createProjectWindowClose, this);
            this.editprojectWindow.cancelBtn.on('click', this.createProjectWindowClose, this);
            this.editprojectWindow.show();
        }
        focusOn("projectname");
    },

    editButtonClicked : function() {
        if(!Wtf.UPerm.EditProject){
            return;
        }else{
            Wtf.Ajax.requestEx({
                url: 'admin.jsp?action=0&mode=26&userid=',
                method:'post',
                params : {
                    pid:this.selectionModel.getSelected().data['projectid'],
                    action:0,
                    mode:26
                }
            },
            this,
            function(result, request) {
                var _r = eval( '('+result+')');
                this.showWindow(_r);
            }
            ,this);
        }
    },
delButtonClicked : function() {
    this.deleteProject();
},
QuickSearchComplete: function(e){
    var view = this.projectsgrid.getView();
    view.refresh();
},
emptyTextChange: function(){
    var view = this.projectsgrid.getView();
    if(this.projectsgrid.getStore().getCount() > 0)
        view.emptyText = '<div class="emptyGridText">'+WtfGlobal.getLocaleText('pm.project.grid.emptytext')+' <br><a href="#" onClick=\'getDocs(\"'+this.id+'\")\'>'+WtfGlobal.getLocaleText('pm.project.grid.starttext')+'</a></div>';
    else
        view.emptyText = '<div class="emptyGridText">'+WtfGlobal.getLocaleText('pm.grid.view.emptytext')+' </div>';
    if(this.quickSearchTF.getValue() != "")
        this.searchPerformed = true;
    view.refresh();
},

GridRowdbClicked : function(Grid,rowIndex,e){
    var rec = this.projds.getAt(rowIndex).data;
    mainPanel.loadTab("../../project.html", "   "+rec.projectid,rec.projectname, "navareadashboard",Wtf.etype.proj,true);
},
deleteProject : function(){
    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.352'), function(btn){
        if (btn == "yes") {
            this.DeleteProject();
        }
    }, this);
},
DeleteProject : function(){
    var selected = this.selectionModel.getSelections();
    var selIds = "";
    var tabs = [];
    for (var i = 0; i < selected.length; i++) {
        selIds += selected[i].data['projectid'] + ",";
        var cmp = Wtf.getCmp('tab'+selected[i].data['projectid']);
        if(cmp)
            tabs[i] = cmp;
    }
    tabs[i] = 'tabproject';
    selIds = selIds.substring(0,selIds.length - 1);
    Wtf.Ajax.requestEx({
        url: "../../admin.jsp?action=1&mode=2&emode=2",
        method: 'POST',
        params: {
            companyid: this.companyid,
            projId: selIds
        }},
    this,
    function(result, req){
        //            for (var i = 0; i < selected.length; i++) {
        //                this.projds.remove(selected[i]);
        //            }
        msgBoxShow(15, 0);
        this.count--;
        bHasChanged=true;
        if(refreshDash.join().indexOf("all") == -1)
            refreshDash[refreshDash.length] = 'all';
        var count = this.projds.getCount();
        var  start = this.projds.lastOptions.params.start - this.pP.combo.value;
        start = (start < 0)? start = 0 : start;
        if(selected.length == this.pP.combo.value || selected.length == count){
            this.projds.reload({
                params:{
                    start:start,
                    limit:this.pP.combo.value
                }
            })
        }else{
            this.projds.reload();
        }
        removeTabsAndRefreshDashoard(tabs, false);//do not redirect on dashboard after removing project
    },
    function(result, req){
        msgBoxShow(16, 1);
        bHasChanged=false;
    });
},

createProjectWindowClose: function(){
    if(this.mode == 1){
        Wtf.getCmp("editproj" + this.id).enable();
        this.editprojectWindow.close();
    } else {
        Wtf.getCmp("createproj" + this.id).enable();
        this.createprojectWindow.close();
    }
},

createProjectFailure: function(btn, frm, action){
    Wtf.getCmp("createproj" + this.id).enable();
    var text = WtfGlobal.getLocaleText('lang.create.text')
    if(action.options.useraction == 1){
        text = WtfGlobal.getLocaleText('lang.edit.text');
        Wtf.getCmp("editproj" + this.id).enable();
        this.editprojectWindow.close();
    } else {
        this.createprojectWindow.close();
    }
    msgBoxShow([WtfGlobal.getLocaleText('pm.common.failure'), "Operation Failed. Could not " + text + " project."], 1);
    bHasChanged=false;
},

createProjectSuccess: function(btn, frm, action){
    Wtf.getCmp("createproj" + this.id).enable();
    var text = WtfGlobal.getLocaleText('lang.created.text');
    var  start = this.projds.lastOptions.params.start - this.pP.combo.value;
    start = (start < 0)? start = 0 : start+this.pP.combo.value;
    var resobj = eval( "(" + action.response.responseText.trim() + ")" );
    if(action.options.useraction == 1){
        Wtf.getCmp("editproj" + this.id).enable();
        text = WtfGlobal.getLocaleText('lang.edited.text');
        if(action.response && action.response.responseText != "" && action.response.responseText !== undefined){
            var edited = eval("(" + action.response.responseText + ")").data;
            var rec = this.selectionModel.getSelected().data;
            rec.projectname = edited.projectname;
            rec.image = edited.image;
            rec.description = edited.description;
            rec.count = edited.members;
        }
        this.editprojectWindow.close();
    } else {
        this.createprojectWindow.close();
    }
    msgBoxShow([WtfGlobal.getLocaleText('lang.success.text'), WtfGlobal.getLocaleText({key:'pm.msg.project.success', params: text})], 0);
    collectProjectData();
    if(action.options.useraction != 1){
        var check = btn.id == 'createOpenProjBtn' ? true : false;
        var projname = frm.findField("projectname").getValue();
        var projid = resobj.data;
        if(check){
            navigate("q",projid,projid,projname,"home");
        }
    }
    this.count++;
    (action.options.useraction == 1) ? this.projds.load({
            params: {
                start:start,
                limit:this.pP.combo.value
            }
    }): this.projds.reload();
    this.projectsgrid.getView().refresh();
    bHasChanged=true;
    if(refreshDash.join().indexOf("all") == -1)
        refreshDash[refreshDash.length] = 'all';
}
});
/***** End Of Project Administration   *****/

/*****  Company Administration   *****/
Wtf.common.adminPageCompany = function(config){
    Wtf.apply(this, config);
    Wtf.common.adminpageproject.superclass.constructor.call(this,config);
};

Wtf.extend(Wtf.common.adminPageCompany,Wtf.Panel,{
    onRender: function(config) {
        Wtf.common.adminPageCompany.superclass.onRender.call(this, config);

        this.companyEdit = Wtf.UPerm.EditCompany;
        this.companyEditLogo = false;

        //        if(!Wtf.StoreMgr.containsKey("timezone")){
        //            Wtf.timezoneStore.load();
        //            Wtf.StoreMgr.add("timezone",Wtf.timezoneStore)
        //        }
        //        if(!Wtf.StoreMgr.containsKey("country")){
        //            Wtf.countryStore.load();
        //            Wtf.StoreMgr.add("country",Wtf.countryStore);
        //        }
        this.companyRecTmp = new Wtf.data.Record.create([
            {name: 'featureid'},
            {name: 'featurename'},
            {name: 'subscriptiondate'},
            {name: 'expdate'},
            {name: "subscribed"}
        ]);
        this.dataReader = new Wtf.data.KwlJsonReader({
            root: "data"
        },this.companyRecTmp);
        this.groupingView = new Wtf.grid.GroupingView({
            forceFit: true,
            showGroupName: false,
            enableGroupingMenu: false,
            hideGroupedColumn: false
        });
        this.subscriptionds = new Wtf.data.GroupingStore({
            reader: this.dataReader,
            url: '../../admin.jsp?action=0&mode=11&companyid='+ companyid,//mode=0 is user
            method : 'GET',
            sortInfo: {
                field: "subscribed",
                direction: "asc"
            }
        });
        this.subscriptionds.load();
        this.holidaysRec = Wtf.data.Record.create([
            {name: "day", mapping: 'holiday'},
            {name: 'description', mapping: 'description'}
        ]);
        var subscriptionRec = Wtf.data.Record.create([
            {name: "modulename", mapping: 'modulename'},
            {name: "status", mapping: "status"},
            {name: "modulenickname", mapping: "modulenickname"},
            {name: "parentmod", mapping: "parentmod"}
        ]);

        var featureViewRec = Wtf.data.Record.create([
            {name: "featurename", mapping: 'featurename'},
            {name: 'view', mapping: 'view'},
            {name:'featureshortname', mapping:'featureshortname'}
        ]);

        this.holidaysStore = new Wtf.data.Store({
            url: "../../admin.jsp",
            //            id: "companyHolidays",
            autoLoad: true,
            baseParams: {
                action: 0,
                mode: 15,
                companyid: companyid
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            }, this.holidaysRec)
        });
        this.subscriptionStore = new Wtf.data.Store({
            url: "../../admin.jsp",
            autoLoad: true,
            baseParams: {
                action: 0,
                mode: 23,
                companyid: companyid
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            }, subscriptionRec)
        });
        this.subscriptionStore.load();

        this.featureViewStore = new Wtf.data.Store({
            url: "../../admin.jsp",
            autoLoad: true,
            baseParams: {
                action: 0,
                mode: 24,
                companyid: companyid
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            }, featureViewRec)
        });
        this.featureViewStore.load();

        this.holidaysStore.on("load", function(){
            new Wtf.form.DateField({
                renderTo: 'holidayDatePick',
                allowBlank:false,
                format: WtfGlobal.getOnlyDateFormat(),
                readOnly:true,
                emptyText : WtfGlobal.getLocaleText('pm.admin.company.holidays.selectdate'),
                id: 'holidayDateField'
            });
        });
        //        this.notifystore = null;
        //        this.notificationReq();
        var seperator = {
            border: false,
            html: (Wtf.isIE) ? '<hr style="width:90%;margin-left:20px;text-align:left;">' : '<hr style="width:75%;margin-left:10px">'
        };
        var blankSeperator ={
            border: false,
            html: ''
        }

        var companyDetailsURL = "../../admin.jsp?mode=3&action=1&emode=0&editAll=1";  // Edit all details in company administration

        if(Wtf.UPerm.EditCompany && this.companyEditLogo) {
            companyDetailsURL = "../../admin.jsp?mode=3&action=1&emode=0&editAll=2";  // Edit all details except company logo

        } else if(!Wtf.UPerm.EditCompany && !this.companyEditLogo) {
            companyDetailsURL = "../../admin.jsp?mode=3&action=1&emode=0&editAll=0";  // Edit only company logo
        }

        var defConf = {ctCls: 'fieldContainerClass',labelStyle: 'font-size:11px; text-align:right;'};
        this.companyDetailsPanel = new Wtf.form.FormPanel({
            id: 'companyDetailsForm',
            url : companyDetailsURL,
            fileUpload: true,
            cls: 'adminFormPanel',
            autoScroll: true,
            border: false,
            items: [{
                layout: 'column',
                border: false,
                items:[{
                    columnWidth: 0.49,
                    border: false,
                    items: [{
                        xtype : 'fieldset',
                        title: WtfGlobal.getLocaleText('pm.admin.company.companydetails'),
                        autoHeight: true,
                        id:'compInfoFieldSet',
                        cls: 'companyFieldSet',
                        html: '<img src="../../images/loading.gif" style="margin-right:5px;"></img><span class="holidayspan">'+WtfGlobal.getLocaleText('pm.company.retrievedata')+'</span>'
                    }, seperator, {
                        xtype : 'fieldset',
                        title: WtfGlobal.getLocaleText('pm.contact.information'),
                        autoHeight: true,
                        id:'compContactInfo',
                        cls: 'companyFieldSet',
                        html: '<img src="../../images/loading.gif" style="margin-right:5px;"></img><span class="holidayspan">'+WtfGlobal.getLocaleText('pm.company.retrievedata')+'</span>'
                    }, (Wtf.UPerm.ModuleSubscription)?seperator:blankSeperator, {
                        xtype : 'fieldset',
                        cls: "companyFieldSet",
                        id: "companyModuleSubscription",
                        hidden: (Wtf.UPerm.ModuleSubscription)?false:true,
                        title: WtfGlobal.getLocaleText('pm.admin.company.modulesubscription'),
                        autoHeight: true,
                        items:[{
                            xtype: 'dataview',
                            itemSelector: "compnyModuleSubscription",
                            tpl: new Wtf.XTemplate('<div class="listpanelcontent"><tpl for=".">{[this.f(values)]}</tpl></div>', {
                                f: function(val){
                                    var id = "contDiv_" + val.modulename;
                                    var nameClass = (val.parentmod == "") ? "subscriptionDiv" : "subModSubscriptionDiv";
                                    var onClickFunction = "subscribeModule('";
                                    if(val.parentmod != "") {
                                        var parentData = this.scope.subscriptionStore.query("modulenickname", val.parentmod);
                                        if(parentData.items[0].data.status == "Unsubscribed") {
                                            onClickFunction = "subscribeModuleErrorMsg('";

                                        } else {
                                            onClickFunction = "subscribeSubModule('";
                                        }
                                    }

                                    var rethtml = "<div class = 'sibContDiv WAIT' id='" + id + "'><div class='" + nameClass + "'>"
                                    + WtfGlobal.getLocaleText('pm.modules.'+val.modulenickname) + "</div> <div class = 'statusDiv'>" + WtfGlobal.getLocaleText(val.status) + "</div>";

                                    if(val.status == "Subscribed"){
                                        rethtml = "<div class = 'sibContDiv SUB' id='" + id + "'><div class='" + nameClass + "'>" + ((val.parentmod == "") ? "" : " -- ") + WtfGlobal.getLocaleText('pm.modules.'+val.modulenickname) +
                                        "</div><div class = 'statusDiv'>" + WtfGlobal.getLocaleText(val.status) +
                                        "</div><a href='#' onclick=\"" + onClickFunction + id + "')\" class='sublink'>"+WtfGlobal.getLocaleText('Unsubscribe')+"</a>";

                                    } else if(val.status == "Unsubscribed"){
                                        rethtml = "<div class = 'sibContDiv UNS' id='" + id + "'><div class='" + nameClass + "'>" + ((val.parentmod == "") ? "" : " -- ") + WtfGlobal.getLocaleText('pm.modules.'+val.modulenickname) +
                                        "</div><div class = 'statusDiv'>" + WtfGlobal.getLocaleText(val.status) +
                                        "</div><a href='#' onclick=\"" + onClickFunction + id + "')\" class='sublink'>"+WtfGlobal.getLocaleText('lang.subscribe.text')+"</a>";
                                    }
                                    return rethtml + "</div>";
                                },
                                scope: this
                            }),
                            store: this.subscriptionStore,
                            emptyText: '<img src="../../images/loading.gif" style="margin-right:5px;"></img><span class="holidayspan">'+WtfGlobal.getLocaleText('pm.company.retrievedata.subscription')+'</span>'
                        }]
                    }, (Wtf.UPerm.ManageFeatures)?seperator:blankSeperator, {
                        xtype : 'fieldset',
                        cls: "companyFieldSet",
                        id: "companyFeatureView",
                        title: WtfGlobal.getLocaleText('pm.admin.company.featureview'),
                        hidden: (Wtf.UPerm.ManageFeatures)?false:true,
                        autoHeight: true,
                        items:[{
                            xtype: "dataview",
                            itemSelector: "companyFeatureView",
                            tpl: new Wtf.XTemplate('<div class="listpanelcontent"><tpl for=".">{[this.f(values)]}</tpl></div>', {
                                f: function(val){
                                    var id = "featureDiv_" + val.featurename;
                                    var rethtml = "<div class = 'sibContDiv WAIT' id='" + id + "'><div class='subscriptionDiv'>"
                                    + WtfGlobal.getLocaleText('pm.featureslist.'+val.featureshortname) + "</div> <div class = 'statusDiv'>" + WtfGlobal.getLocaleText(val.view) + "</div>";
                                    if(val.view == "Show") {
                                        rethtml = "<div class = 'sibContDiv SUB' id='" + id + "'><div class='subscriptionDiv'>" + WtfGlobal.getLocaleText('pm.featureslist.'+val.featureshortname) +
                                        "</div><div class = 'statusDiv'>" + WtfGlobal.getLocaleText(val.view) +
                                        "</div><a href='#' onclick=\"featureViewUpdate('" + id + "', '"+ val.featureshortname +"')\" class='sublink'>"+WtfGlobal.getLocaleText('Hide')+"</a>";
                                    } else if(val.view == "Hide") {
                                        rethtml = "<div class = 'sibContDiv UNS' id='" + id + "'><div class='subscriptionDiv'>" + WtfGlobal.getLocaleText('pm.featureslist.'+val.featureshortname) +
                                        "</div><div class = 'statusDiv'>" + WtfGlobal.getLocaleText(val.view) +
                                        "</div><a href='#' onclick=\"featureViewUpdate('" + id + "', '"+ val.featureshortname +"')\" class='sublink'>"+WtfGlobal.getLocaleText('Show')+"</a>";
                                    }
                                    return rethtml + "</div>";
                                },
                                scope: this
                            }),
                            store: this.featureViewStore,
                            emptyText: '<img src="../../images/loading.gif" style="margin-right:5px;"></img><span class="holidayspan">'+WtfGlobal.getLocaleText('pm.company.retrievedata.featureview')+'</span>'
                        }]
                    }, seperator, {
                        xtype : 'fieldset',
                        cls: "companyFieldSet",
                        id: "companyCheckListMaster",
                        title: WtfGlobal.getLocaleText('pm.admin.company.checklist.header'),
                        autoHeight: true,
                        items:[{
                            xtype: 'panel',
                            border: false,
                            layout: 'column',
                            header: false,
                            html: '<div style="color:#445566; font-size:11px; margin-left:7px;">'+WtfGlobal.getLocaleText('pm.admin.company.checklist.subheader')+'</div>'
                                    + '<div style="display:none;margin-left: 7px;" class="clInfo" id="clInfoMsg">'+WtfGlobal.getLocaleText('pm.holidays.update.tip')+'</div>',
                            items: [{
                                labelStyle: "width:115px",
                                xtype: 'checkbox',
                                name: 'checklist',
                                ctCls: 'clInfoMargin',
                                id: 'checklist',
                                listeners: {
                                    scope: this,
                                    'check': function(cb, checked){
                                        if(!this.fillDataFlag) {
                                            Wtf.get('clInfoMsg').dom.style.display = 'block';
                                            this.fillDataFlag = false;
                                        }
                                    }
                                },
                                boxLabel: WtfGlobal.getLocaleText('pm.admin.company.checklist.enable')
                            }]
                        }, {
                            xtype: 'panel',
                            border: false,
                            header: false,
                            hidden: true,
                            id: 'invokeCheckListMaster',
                            html: '<span style="color:#445566; font-size:12px; font-weight:bold;" class="mailTo">'+
                                '<ul><li class="clOpr"><a href="#" onclick="showCheckListMaster();" wtf:qtip="'+WtfGlobal.getLocaleText('pm.admin.company.checklist.master.subheader')+'">'+WtfGlobal.getLocaleText('pm.admin.company.checklist.link')+'</a><br></li>'+
                                '<li style="display:none;" class="clOpr"><a href="#" onclick="resetProgress();" wtf:qtip="'+'This option allows you to reset progres of the tasks with which checklists are associated. Use this setting to recalculate and set task progress after turning this feature on or off.'+'">'+'Re-calculate Task(s) Progress'+'</li></a></span>'+
                                ''
                        }]
                    }]
                },  //end of first column

                {
                    columnWidth: 0.49,
                    border: false,
                    items:[{
                        xtype : 'fieldset',
                        id: "notification"+this.id,
                        cls: "companyFieldSet",
                        title: WtfGlobal.getLocaleText('pm.admin.company.notification'),
                        defaults : defConf,
                        hidden : (Wtf.UPerm.ManageNotifications)?false:true,
                        autoHeight: true,
                        items:[{
                            xtype: 'panel',
                            html: '<span style="color:#445566; font-size:11px;">'+WtfGlobal.getLocaleText('pm.email.notification.msg')+'</span>',
                            border: false,
                            layout: 'column',
                            items: [{
                                xtype: 'panel',
                                columnWidth: 0.5,
                                layout: 'fit',
                                height : 22,
                                border: false,
                                items: [{
                                    labelStyle: "width:115px",
                                    xtype : 'checkbox',
                                    name: '1',
                                    id: '1',
                                    boxLabel: WtfGlobal.getLocaleText('pm.email.notification')
                                }]
                            },{
                                xtype:'panel',
                                columnWidth: 0.5,
                                layout: 'fit',
                                border: false,
                                hidden: true,
                                height : 2,
                                items: [{
                                    labelStyle: "width:115px",
                                    xtype: 'checkbox',
                                    name: '2',
                                    id: '2',
                                    boxLabel: WtfGlobal.getLocaleText('lang.SMS.text')
                                }]
                            }
                            ]
                        }]
                    }, (Wtf.UPerm.ManageNotifications)?seperator:blankSeperator, {
                        xtype : 'fieldset',
                        id: "customwidget"+this.id,
                        cls: "companyFieldSet",
                        title: WtfGlobal.getLocaleText('pm.admin.company.customwidget.title'),
                        defaults : defConf,
                        autoHeight: true,
                        items:[{
                            xtype: 'panel',
                            border: false,
                            layout: 'column',
                            header: false,
                            html: '<span style="color:#445566; font-size:11px;">'+WtfGlobal.getLocaleText('pm.admin.company.customwidget.description')+'</span>',
                            items: [{
                                labelStyle: "width:115px",
                                xtype: 'checkbox',
                                name: 'milestonewidget',
                                id: 'milestonewidget',
                                boxLabel: WtfGlobal.getLocaleText('pm.admin.company.customwidget.text')
                            }]
                        }]
                    }, seperator, {
                        xtype : 'fieldset',
                        id: "docaccess"+this.id,
                        cls: "companyFieldSet",
                        title: WtfGlobal.getLocaleText('pm.admin.company.docs.header'),
                        defaults : defConf,
                        autoHeight: true,
                        items:[{
                            xtype: 'panel',
                            border: false,
                            layout: 'column',
                            header: false,
                            html: '<span style="color:#445566; font-size:11px;">'+WtfGlobal.getLocaleText('pm.admin.company.docs.subheader')+'</span>'
                                    + '<div style="display:none;margin-left: 7px;" class="clInfo" id="docsInfoMsg">'+WtfGlobal.getLocaleText('pm.admin.company.docs.update')+'</div>',
                            items: [{
                                labelStyle: "width:115px",
                                xtype: 'checkbox',
                                name: 'docaccess',
                                id: 'docaccess',
                                listeners: {
                                    scope: this,
                                    'check': function(cb, checked){
                                        if(!this.fillDataFlag) {
                                            Wtf.get('docsInfoMsg').dom.style.display = 'block';
                                            this.fillDataFlag = false;
                                        }
                                    }
                                },
                                boxLabel: WtfGlobal.getLocaleText('pm.admin.company.docs.header')
                            }]
                        }]
                    }, seperator, {
                        xtype : 'fieldset',
                        cls: "companyFieldSet",
                        id: "CompanyHolidays",
                        title: WtfGlobal.getLocaleText('pm.admin.company.holidays.title'),
                        autoHeight: true,
                        items:[{
                            layout:'form',
                            border:false,
                            items:[{
                                border:false,
                                html:'<span style="display:none;font-size:10px;color:red;" id="addInfoMsg">'+WtfGlobal.getLocaleText('pm.holidays.update.tip')+'</span>'
                            }]
                        },{
                            xtype : 'hidden',
                            hidden: true,
                            id: "CompanyHolidaysHidden",
                            name: "holidays"
                        },{
                            xtype:"panel",
                            layout:'form',
                            autoDestroy: true,
                            //                            id: "CompanyAddHolidayPanel",
                            border: false,
                            id: 'newHolidayPanel',
                            hidden : (Wtf.UPerm.ManageHoliday)?false:true,
                            html: '<div style="display:block; padding-top:5px;" id="addHoliday">' +
                            '<input type="text" style="float:left;" id="holidayDesc" maxlength="512">' +
                            '<div id="holidayDatePick" style="float:left; width:110px; padding-left:5px;"></div>' +
                            '<a href="#" class="holidayDelete" onclick= \'addHoliday("' + this.id + '")\' wtf:qtitle="'+WtfGlobal.getLocaleText('pm.admin.company.holidays.addholiday')+'" wtf:qtip="'+WtfGlobal.getLocaleText('pm.holidays.add.tip')+'">'+WtfGlobal.getLocaleText('pm.admin.company.holidays.addholiday')+'</a></div>'
                        },{
                            xtype: 'dataview',
                            id: 'holidaysDataView',
                            itemSelector: "CompanyHolidays",
                            tpl: new Wtf.XTemplate('<div class="listpanelcontent"><tpl for=".">{[this.f(values)]}</tpl></div>', {
                                f: function(val){
                                    var editCreate = (Wtf.UPerm.ManageHoliday)?"<img src='../../images/cancel16.png' class='holidayDelete' onclick=\"deleteHoliday(this,'" + this.scope.id + "')\" id='del_"+val.day+"' wtf:qtip='"+WtfGlobal.getLocaleText('pm.holiday.delete.tip')+"'>"+
                                    "<div><span class='holidayDiv'></span></div></div>":"";
                                    return "<div id='div_"+val.day+"'><span class='holidaySpan holidayspanwidth'>" + val.description + "</span>" +
                                    "<span class='holidaySpan holidayspanwidth'>" + Date.parseDate(val.day,'Y-m-d').format(WtfGlobal.getOnlyDateFormat())+ "</span>" + editCreate;
                                },
                                scope: this
                            }),
                            store: this.holidaysStore,
                            emptyText: '<span class="holidayspan">'+WtfGlobal.getLocaleText('pm.holiday.nodata')+'</span>'
                        }]
                    }, (Wtf.UPerm.ManageCustomColumn)?seperator:blankSeperator ,{
                        xtype : 'fieldset',
                        cls: "ccFieldSet",
                        id: "companycustomcolumn",
                        hidden: (Wtf.UPerm.ManageCustomColumn)?false:true,
                        autoHeight: true,
                        items:[
                        this.cusColumnGrid = new Wtf.CCGrid({
                            id:'adminccgrid',
                            width:500,
                            autoHeight: true,
                            autoScroll: true,
                            border:false
                        })
                        ]
                    }, seperator ,{  //end of first column
                        xtype : 'fieldset',
                        id: "pertDifference"+this.id,
                        cls: "companyFieldSet",
                        title: WtfGlobal.getLocaleText('pm.admin.company.pert.title'),
                        defaults : defConf,
                        autoHeight: true,
                        items:[{
                            xtype: 'panel',
                            border: false,
                            html: '<span style="color: #445566;font-size: 11px;">'+WtfGlobal.getLocaleText('pm.Help.cpaCompanyDefault')+'</span>'
                        },{
                            layout: 'form',
                            border: false,
                            id: this.id+'PertDifferenceForm',
                            defaultType: 'numberfield',
                            defaults: {
                                maxValue: 99,
                                minValue: -99,
                                allowBlank: false,
                                allowDecimals: false
                            },
                            labelWidth: 260,
                            items: [{
                                fieldLabel: WtfGlobal.getLocaleText('pm.common.pert.diffopt.text'),
                                name: 'optimisticdiff',
                                id: 'optimisticdiff'
                            },{
                                fieldLabel: WtfGlobal.getLocaleText('pm.common.pert.diffpes.text'),
                                name: 'pessimisticdiff',
                                id: 'pessimisticdiff'
                            }]
                        }]
                    }]  //end of seond column
                }]
            }]
        });

        //        if(this.companyEdit) {
        //            Wtf.getCmp("nameField").disable();
        //            Wtf.getCmp("domainField").disable();
        //            Wtf.getCmp("addressField").disable();
        //            Wtf.getCmp("cityField").disable();
        //            Wtf.getCmp("stateField").disable();
        //            Wtf.getCmp("zipField").disable();
        //            Wtf.getCmp("countryField").disable();
        //            Wtf.getCmp("timezoneField").disable();
        //            Wtf.getCmp("currencyField").disable();
        //            Wtf.getCmp("phoneField").disable();
        //            Wtf.getCmp("faxField").disable();
        //            Wtf.getCmp("websiteField").disable();
        //            Wtf.getCmp("emailField").disable();
        //            Wtf.getCmp("CompanyHolidays").disable();
        //            Wtf.getCmp("compnyModuleSubscription").disable();
        //            Wtf.getCmp("companyFeatureView").disable();
        //        }

        //        if(this.companyEditLogo) {
        //            Wtf.getCmp("compfieldset").disable();
        //        }
        //
        //        Wtf.getCmp("nameField").on("change", function(){
        //            Wtf.getCmp("nameField").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("nameField").getValue()));
        //        }, this);
        //        Wtf.getCmp("addressField").on("change", function(){
        //            Wtf.getCmp("addressField").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("addressField").getValue()));
        //        }, this);
        //        Wtf.getCmp("cityField").on("change", function(){
        //            Wtf.getCmp("cityField").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("cityField").getValue()));
        //        }, this);
        //        Wtf.getCmp("stateField").on("change", function(){
        //            Wtf.getCmp("stateField").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("stateField").getValue()));
        //        }, this);
        //        Wtf.getCmp("websiteField").on("change", function(){
        //            var validateURL = WtfGlobal.HTMLStripper(Wtf.getCmp("websiteField").getValue());
        //            if(validateURL!="") {
        //                if(validateURL.indexOf("http://") != 0 && validateURL.indexOf("https://") != 0 && validateURL.indexOf("ftp://") != 0)
        //                    validateURL = "http://"+validateURL;
        //            }
        //            Wtf.getCmp("websiteField").setValue(validateURL);
        //        }, this);
        //        Wtf.getCmp("countryField").on("select",function(a,b,c){
        //            var rec = Wtf.countryStore.getAt(c);
        //            if(rec.data.timezone)
        //                Wtf.getCmp("timezoneField").setValue(rec.data.timezone + rec.data.name);
        //            else{
        //                Wtf.getCmp("timezoneField").setValue("(GMT-08:00)" + rec.data.name);
        //            }
        //        })
        var detailPanel = new Wtf.Panel({
            layout: "border",
            border: false,
            bodyStyle: "background-color:#ffffff;",
            bbar:[{
                id : 'updatecompanydetails',
                text: WtfGlobal.getLocaleText('lang.update.text'),
                scope: this,
                handler: this.updateCompany,
                iconCls: "dpwnd updatecompanydetails",
                tooltip: {text: WtfGlobal.getLocaleText('pm.Help.updatecompany')}
            }],
            items: [{
                border: false,
                region: 'center',
                autoScroll: true,
                items: [ this.companyDetailsPanel ]
            }/*,{
                border: false,
                height: 200,
                region: 'south',
                items: [new Wtf.grid.GridPanel({
                    id:'subscriptiongrid'+this.id,
                    store: this.subscriptionds,
                    cm: this.gridcm,
                    border : false,
                    loadMask : true,
                    view: this.groupingView,
                    enableColumnHide: false
               })]
            }*/]
        })
        this.add(detailPanel);
        Wtf.Ajax.requestEx({
            url: '../../admin.jsp',
            params:{
                action:0,
                mode:10,
                cid: companyid
            }},
        this,
        function(request, response){
            var res = eval('(' + request+ ')');
            if(res && res.data){
                this.doLayout();
                this.fillData(res.data[0], res.notification[0]);
                var hdate = Wtf.getCmp("holidayDateField");
                if(hdate)
                    hdate.destroy();
            } else {
                msgBoxShow(17, 1);
                this.companyDetailsPanel.disable();
            }
        //            bHasChanged=true;
        //            if(refreshDash.join().indexOf("all") == -1)
        //                refreshDash[refreshDash.length] = 'all';
        },
        function(){
            bHasChanged=false;
            msgBoxShow(17, 1);
            this.companyDetailsPanel.disable();
        }
        );
},
deleteHoliday: function(day){
    Wtf.get("addInfoMsg").dom.style.display = "block";
    var rec = this.holidaysStore.getAt(this.holidaysStore.find("day", day));
    this.holidaysStore.remove(rec);
    Wtf.getCmp('holidaysDataView').refresh();
},
addHoliday: function(){
    var desc = Wtf.get("holidayDesc").dom.value;
    desc = desc.substring(0,510);
    desc = WtfGlobal.HTMLStripper(desc);
    if(desc.trim().length>0) {
        var day = Wtf.getCmp("holidayDateField").getValue();
        if(day) {
            if(this.searchInHolilist(day.format('Y-m-d'))) {
                var rec = new this.holidaysRec({
                    "day": day.format('Y-m-d'),
                    "description": desc
                });
                this.holidaysStore.insert(this.holidaysStore.getCount(), rec);
                Wtf.getCmp('holidaysDataView').refresh();
                Wtf.get("addInfoMsg").dom.style.display = "block";
                Wtf.get("holidayDesc").dom.value = "";
                Wtf.getCmp("holidayDateField").reset();
            }
            else
                msgBoxShow(["Invalid Data", "Date '"+day.format(WtfGlobal.getOnlyDateFormat())+"' is already marked as a holiday." ], 2);
        } else
            msgBoxShow(26, 1);
    } else
        msgBoxShow(27, 1);
},

searchInHolilist : function(newDate) {
    for(var cnt =0 ; cnt<this.holidaysStore.getCount();cnt++) {
        if(this.holidaysStore.getAt(cnt).data.day == newDate)
            return false;
    }
    return true;
},
validateContact: function(value) {
    var regex = "^\\+?(\\d{1,4})(-| )?(\\d{1,4})(-| )?(\\d{4,10})$";
    if(value.match(regex) != null)
        return true;
    else {
        this.invalidText = 'Please enter a valid number';
        return false;
    }
},
updateCompany: function(){
    var holidayJson = "";
    for(var k = 0; k < this.holidaysStore.getCount(); k++)
        holidayJson += Wtf.encode(this.holidaysStore.getAt(k).data) + ",";
    holidayJson = "{\"data\":[" + holidayJson.substring(0, holidayJson.length - 1) + "]}";
    Wtf.getCmp("CompanyHolidaysHidden").setValue(holidayJson);
     this.companyDetailsPanel.url += "&optidiff"+Wtf.getCmp('optimisticdiff').getValue()+"&pessidiff"+Wtf.getCmp('optimisticdiff').getValue();
    //
    //        var notifyJson = "";
    //        for(var k = 0; k < this.notifystore.getCount(); k++)
    //            notifyJson += Wtf.encode(this.notifystore.getAt(k).data) + ",";
    //        notifyJson = "{\"data\":[" + notifyJson.substring(0, notifyJson.length - 1) + "]}";
    //        Wtf.getCmp("notifyconfHidden").setValue(notifyJson);

    //        Wtf.getCmp("nameField").validate();
    //        Wtf.getCmp("domainField").validate()
    //        if(Wtf.getCmp("nameField").isValid() && Wtf.getCmp("domainField").isValid()) {
    this.companyDetailsPanel.form.submit({
        scope: this,
        success: function(result,action){
            var resultObj = eval('('+action.response.responseText+')');
            Wtf.get("addInfoMsg").dom.style.display = "none";
            document.getElementById('companyLogo').src = "../../images/store/?company=true&"+Math.random();
            //                    document.getElementById('displaycompanylogo').src = "../../images/store/?company=true&"+Math.random();
            //                    Wtf.CurrencySymbol = this.currencystore.getAt(this.currencystore.find('currencyid',this.currencyfield.getValue())).data.htmlcode;
            Wtf.pref.CompanyNotification = Wtf.getCmp('notification'+this.id).findById('1').getValue();
            var cmp = Wtf.getCmp('invokeCheckListMaster');
            resultObj = Wtf.getCmp('checklist').getValue();
            var flag = false;
            if(WtfGlobal.getCheckListModuleStatus() != resultObj) {
                Wtf.pref.CheckListModule = resultObj
                if(resultObj)
                    cmp.show();
                else
                    cmp.hide();
                flag = true;
            }
            if(resultObj.data==null) {
                if(flag){
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText('lang.success.text'),
                        msg: WtfGlobal.getLocaleText('pm.msg.18') + '<br><b>' + WtfGlobal.getLocaleText('pm.msg.publish.refresh') + '</b>',
                        buttons: Wtf.MessageBox.OK,
                        icon: Wtf.MessageBox.INFO
                    });
                    var task = new Wtf.util.DelayedTask(function(){
                        location.reload(true);
                    }, this);
                    task.delay(4000);
                } else {
                    msgBoxShow(18, 0);
                }
            } else
                msgBoxShow([WtfGlobal.getLocaleText('pm.dashboard.widget.quicklinks.companyadministration'),resultObj.data],0);
            Wtf.get('clInfoMsg').dom.style.display = 'none';
            Wtf.get('docsInfoMsg').dom.style.display = 'none';
            resultObj = Wtf.getCmp('docaccess').getValue();
            Wtf.pref.DocAccess = resultObj
            bHasChanged = true;
            if(refreshDash.join().indexOf("all") == -1)
                refreshDash[refreshDash.length] = 'all';
        },
        failure: function(frm, action){
            if(action.failureType == "client")
                msgBoxShow(19, 1);
            else{
                var resObj = eval( "(" + action.response.responseText + ")" );
                msgBoxShow(20, 1);
            }
        }
    });
//        }
},

resetAll: function(){
    alert("reserAll");
},

fillData: function(resObj, notificationobj){
    // var imagePath = (resObj.image!="")?resObj.image:'../../images/deskeralogo.png';
    //  Wtf.get('domainField').dom.parentNode.innerHTML+="<span style='color:gray !important;font-size:11px !important;'>.deskera.com</span><br><span style='color:gray !important;font-size:10px !important;font-style:italic !important;'>Letters and numbers only — no spaces.<span>";
    resObj.address = resObj.address.replace(/ \n/g, ', ');
    this.compInfo = new Wtf.XTemplate(
        "<tpl><div class='compInfoWrapper'>"+
        "<div class='compInfoFields'>"+
        "<div  style='float:left;width:35%'>"+WtfGlobal.getLocaleText('lang.name.text')+" :  </div><div style='float:left;width:60%'>{companyname}</div><br clear='all'>"+
        "</div>"+
        "<div class='compInfoFields'>"+
        "<div  style='float:left;width:35%'>"+WtfGlobal.getLocaleText('pm.common.subdomain')+" :  </div><div style='float:left;width:60%'>{subdomain}</div><br clear='all'>"+
        "</div>"+
        "<div class='compInfoFields'>"+
        "<div  style='float:left;width:35%'>"+WtfGlobal.getLocaleText('lang.address.text')+" :  </div><div style='float:left;width:60%'>{address}</div><br clear='all'>"+
        "</div>"+
        "<div class='compInfoFields'>"+
        "<div  style='float:left;width:35%'>"+WtfGlobal.getLocaleText('lang.city.text')+" :  </div><div style='float:left;width:60%'>{city}</div><br clear='all'>"+
        "</div>"+
        "<div class='compInfoFields'>"+
        "<div  style='float:left;width:35%'>"+WtfGlobal.getLocaleText('lang.state.text')+" :  </div><div style='float:left;width:60%'>{state}</div><br clear='all'>"+
        "</div>"+
        "<div class='compInfoFields'>"+
        "<div  style='float:left;width:35%'>"+WtfGlobal.getLocaleText('lang.zipcode.text')+" :  </div><div style='float:left;width:60%'>{zip}</div><br clear='all'>"+
        "</div>"+
        "<div class='compInfoFields'>"+
        "<div  style='float:left;width:35%'>"+WtfGlobal.getLocaleText('lang.country.text')+" :  </div><div style='float:left;width:60%'>{country}</div><br clear='all'>"+
        "</div>"+
        "<div class='compInfoFields'>"+
        "<div  style='float:left;width:35%'>"+WtfGlobal.getLocaleText('lang.timezone.text')+" :  </div><div style='float:left;width:60%'>{timezone}</div><br clear='all'>"+
        "</div>"+
        "<div class='compInfoFields'>"+
        "<div  style='float:left;width:35%'>"+WtfGlobal.getLocaleText('lang.currency.text')+" :  </div><div style='float:left;width:60%'>{currency}</div><br clear='all'>"+
        "</div>"+
        "</div></tpl>");
    this.compContactInfo = new Wtf.XTemplate(
        "<tpl><div class='compInfoWrapper'>"+
        "<div class='compInfoFields'>"+
        "<div  style='float:left;width:35%'>"+WtfGlobal.getLocaleText('lang.phonenumber.text')+" :  </div><div style='float:left;width:60%'>{phone}</div><br clear='all'>"+
        "</div>"+
        "<div class='compInfoFields'>"+
        "<div  style='float:left;width:35%'>"+WtfGlobal.getLocaleText('pm.faxnumber')+" :  </div><div style='float:left;width:60%'>{fax}</div><br clear='all'>"+
        "</div>"+
        "<div class='compInfoFields'>"+
        "<div  style='float:left;width:35%'>"+WtfGlobal.getLocaleText('lang.website.text')+" :  </div><div style='float:left;width:60%'>{website}</div><br clear='all'>"+
        "</div>"+
        "<div class='compInfoFields'>"+
        "<div  style='float:left;width:35%'>"+WtfGlobal.getLocaleText('pm.admin.user.emailaddress')+" :  </div><div style='float:left;width:60%'>{emailid}</div><br clear='all'>"+
        "</div>"+
        "</div></tpl>");
    this.compInfo.overwrite(Wtf.getCmp('compInfoFieldSet').body, resObj);
    this.compContactInfo.overwrite(Wtf.getCmp('compContactInfo').body, resObj);
    Wtf.getCmp(this.id+'PertDifferenceForm').findById('optimisticdiff').setValue(resObj.optimisticdiff);
    Wtf.getCmp(this.id+'PertDifferenceForm').findById('pessimisticdiff').setValue(resObj.pessimisticdiff);
    this.fillDataFlag = true;
    Wtf.getCmp('docaccess').setValue(resObj.docaccess);
    //   Wtf.getCmp('durationRadioPanel').findByType('radio')[resObj.notificationduration].setValue(true);
    //        if(resObj.notificationtype == -1){
    //            Wtf.getCmp("notificationemail").setValue(false);
    //            Wtf.getCmp("notificationsms").setValue(false);
    //        } else if(resObj.notificationtype == 0){
    //             Wtf.getCmp("notificationsms").setValue(true);
    //        } else if(resObj.notificationtype == 1){
    //             Wtf.getCmp("notificationemail").setValue(true);
    //        } else{
    //            Wtf.getCmp("notificationemail").setValue(true);
    //            Wtf.getCmp("notificationsms").setValue(true);
    //        }
    for(var i = 0; i < notificationobj.data.length; i++){
        Wtf.getCmp(notificationobj.data[i].typeid.toString()).setValue(notificationobj.data[i].permission);
    }
    Wtf.getCmp('milestonewidget').setValue(resObj.milestonewidget);
    this.fillDataFlag = true;
    Wtf.getCmp('checklist').setValue(resObj.checklist);
    var cmp = Wtf.getCmp('invokeCheckListMaster');
    if(resObj.checklist)
        cmp.show();
    this.fillDataFlag = false;
    hideMainLoadMask();
//        document.getElementById('displaycompanylogo').src = "../../images/store/?company=true&"+Math.random();
}/*,

    notificationReq : function() {
        Wtf.Ajax.requestEx({
            url: '../../admin.jsp',
            method: 'POST',
            params: {
                action : 0,
                mode: 22
            }},
            this,
            function(response, e){
                var obj = eval('(' + response+ ')');
                this.notifystore = new Wtf.data.JsonStore({
                    fields: this.createNFields(obj.columnheader),
                    data: obj.data
                });
                this.notifytype = obj.columnheader;
                var header = "<div class='listpanelcontent' style ='font-weight:bold;'>";
                header +="<span class='holidaySpan' style = 'width:110px;'>&nbsp;</span>";
                for(var fieldcnt = 0; fieldcnt < this.notifytype.length; fieldcnt++) {
                    header +="<span class='holidaySpan' style = 'width:90px;'>"+this.notifytype[fieldcnt].typename+"</span>";
                }
                header +="</div>"
                Wtf.get('notifyheader'+this.id).dom.innerHTML = header;
                Wtf.getCmp('notificationDataView'+this.id).store = this.notifystore;
                Wtf.getCmp('notificationDataView'+this.id).refresh();
        });
    },
*/
/*    createNFields : function(columnheader) {
        var fields = [];
        var fObj1 = {};
        fObj1['name'] = 'nid';
        fObj1['type'] = 'string';
        fObj1['mapping'] = 'nid';
        fields[fields.length] = fObj1;
        var fObj2 = {};
        fObj2['name'] = 'name';
        fObj2['type'] = 'string';
        fObj2['mapping'] = 'name';
        fields[fields.length] = fObj2;
        for(var fieldcnt = 0; fieldcnt < columnheader.length; fieldcnt++) {
            var fObj = {};
            fObj['name'] = columnheader[fieldcnt].typeid;
            fObj['mapping'] = columnheader[fieldcnt].typeid;
            fObj['type'] = 'boolean';
            fields[fields.length] = fObj;
        }
        return fields;
    },

    onNotifyChange : function(id) {
        var idArray = id.split("_");
        var index = this.notifystore.find("nid",idArray[1]);
        this.notifystore.getAt(index).set(idArray[2],Wtf.get(id).dom.checked);
    }*/
});
/*****  Company Administration End  *****/

Wtf.common.MainAdmin = function(config){
    Wtf.common.MainAdmin.superclass.constructor.call(this,config);

    this.addEvents({
        'adminclicked':true,
        'projectclicked': true,
        //'communityclicked': true,
        'panelRendered': true,
        "companyclicked": true
    });
    this.on("adminclicked",this.handleadminClick,this);
    this.on("projectclicked",this.handleProjClick,this);
    this.on("companyclicked", this.handleCompanyClick, this);
    // this.on("communityclicked",this.handleCommClick,this);
    this.actTab=null;
}

Wtf.extend(Wtf.common.MainAdmin,Wtf.Panel,{
    handleadminClick:function(){
        this.actTab=0;
        if(this.tabpanel)
            this.tabpanel.setActiveTab(0);
    },

    handleProjClick:function(){
        this.actTab=1;
        if(this.tabpanel)
            this.tabpanel.setActiveTab(1);
    },

    handleCompanyClick:function(){
        if(this.tabpanel.items.length == 2)
            this.actTab=1;
        else
            this.actTab=2;
        if(this.tabpanel){
            if(this.tabpanel.items.length == 2)
                this.tabpanel.setActiveTab(1);
            else
                this.tabpanel.setActiveTab(2);
        }
    },

    //    handleCommClick:function(){
    //       this.actTab=1;
    //        if(this.tabpanel)
    //            this.tabpanel.setActiveTab(1);
    //    },

    onRender : function(config){
        Wtf.common.MainAdmin.superclass.onRender.call(this,config);
        this.adminuser = new Wtf.common.adminpageuser({
            id: "admin_" + this.id,
            companyid: this.companyid,
            border : false,
            title : WtfGlobal.getLocaleText('pm.dashboard.widget.quicklinks.useradministration'),
            layout : 'fit',
            iconCls:'pwnd userTabIcon'
        });
        this.adminuser.on("dataRendered", function(){
            this.fireEvent("panelRendered");
        },this);
        //        if(isRoleGroup(4)){
        //            this.admincommunity = new Wtf.common.adminpagecommunity({
        //                id: "admincommunity_" + this.id,
        //                companyid: this.companyid,
        //                border : false,
        //                title : WtfGlobal.getLocaleText('pm.admin.community.text'),
        //                layout : 'fit',
        //                iconCls:'dpwnd communityTabIcon'
        //            });
        //        }
        this.adminproject = new Wtf.common.adminpageproject({
            id: "adminproject_" + this.id,
            companyid: this.companyid,
            border : false,
            title : WtfGlobal.getLocaleText('pm.dashboard.widget.quicklinks.projectadministration'),
            layout : 'fit',
            iconCls:'dpwnd projectTabIcon'
        });
        this.tabpanel = this.add(new Wtf.TabPanel({
            id : 'subtabpanel'+this.id,
            border : false,
            activeItem : 0/*,
            items : [this.adminuser,this.adminproject]*/
        }));
        if(Wtf.UPerm.User) {
            this.tabpanel.add(this.adminuser);
        }
        if(Wtf.UPerm.Project) {
            this.tabpanel.add(this.adminproject);
        }
        //        if(isRoleGroup(4)){
        //            Wtf.getCmp('subtabpanel'+this.id).add(this.admincommunity);
        //            this.admincommunity.on("announce", this.showAnnouncementForm, this.admincommunity);
        //        }
        this.adminuser.on("announce", this.showAnnouncementForm, this.adminuser);
        this.adminproject.on("announce", this.showAnnouncementForm, this.adminproject);
        if(Wtf.UPerm.Company) {
            var currencystore = new Wtf.data.JsonStore({
                url:"../../admin.jsp?mode=18&action=0",
                root:'data',
                fields : ['currencyid','currencyname','symbol','htmlcode']
            });
            currencystore.load();
            this.adminCompany = new Wtf.common.adminPageCompany({
                id: 'adminCompany_' + this.id,
                companyid: this.companyid,
                border: false,
                currencystore:currencystore,
                title: WtfGlobal.getLocaleText('pm.dashboard.widget.quicklinks.companyadministration'),
                layout: 'fit',
                iconCls: 'dpwnd companyTabIcon'
            });
            this.tabpanel.add(this.adminCompany);
        }

        if(this.event){
            this.fireEvent(this.event);
        }
        this.adminuser.on("render",this.setActivefunc,this);
    },

    setActivefunc:function(obj){
        this.tabpanel.setActiveTab(this.actTab);
    },

    showAnnouncementForm: function(typetext, id){
        this.annwin = new Wtf.Window({
            title : typetext + " "+WtfGlobal.getLocaleText('lang.announcements.text'),
            closable : true,
            modal : true,
            iconCls : 'iconwin',
            width : 420,
            height: 320,
            resizable :false,
            buttonAlign : 'right',
            buttons :[{
                text : WtfGlobal.getLocaleText('lang.announcement.text'),
                scope : this,
                handler:function(){
                    if(!this.AnnForm.form.isValid())
                        return;
                    this.annwin.buttons[0].disable()
                    var list = "";
                    var cnt;
                    var idsbuf = this.selectionModel.getSelections();
                    for (cnt=0;cnt<idsbuf.length;cnt++){
                        list += idsbuf[cnt].data[id] + ",";
                    }
                    list = list.substring(0,list.length - 1);
                    var starttime = "";
                    var endtime = "";
                    if(!Wtf.getCmp('fromdate'+this.id).getValue() == "")
                        starttime = Wtf.getCmp('fromdate'+this.id).getValue().format('Y-m-d H:i:s');
                    else {
                        var a = Wtf.getCmp('fromdate'+this.id).df.getValue().format('Y-m-j') +" " + Wtf.getCmp('fromdate'+this.id).tf.getValue();
                        starttime = Date.parseDate(a, 'Y-m-j g:i A').format('Y-m-d H:i:s');
                    }
                    if(!Wtf.getCmp('todate'+this.id).getValue() == "")
                        endtime = Wtf.getCmp('todate'+this.id).getValue().format('Y-m-d H:i:s');
                    else {
                        a = Wtf.getCmp('todate'+this.id).df.getValue().format('Y-m-j') +" " + Wtf.getCmp('todate'+this.id).tf.getValue();
                        endtime = Date.parseDate(a, 'Y-m-j g:i A').format('Y-m-d H:i:s');
                    }
                    Wtf.Ajax.requestEx({
                        waitMsg: WtfGlobal.getLocaleText("pm.loading.text") + '...',
                        url: "../../admin.jsp",
                        params: {
                            featureid: list,
                            announcement: Wtf.getCmp("announcement" + this.id).getValue(),
                            fromdate: starttime,
                            todate: endtime,
                            action: 0,
                            mode: 8
                        }
                    },
                    this,
                    function(frm, action){
                        //var act = eval('(' + frm+ ')');
                        var act = frm;
                        if(act){
                            msgBoxShow(24, 0);
                            this.annwin.close();
                            bHasChanged=true;
                            if(refreshDash.join().indexOf("all") == -1 && refreshDash.join().indexOf("anc") == -1)
                                refreshDash[refreshDash.length] = 'anc';
                        }
                        else{
                            this.annwin.close();
                            bHasChanged=false;
                            msgBoxShow(25, 1);
                        }
                    },
                    function(frm, action){
                        msgBoxShow(25, 1);
                        this.annwin.close();
                        bHasChanged=false;
                    }
                    );
            }
        },{
            text : WtfGlobal.getLocaleText('lang.cancel.text'),
            scope : this,
            handler : function(){
                this.annwin.buttons[1].disable();
                this.annwin.close();
            }
        }],
        layout : 'border',
        items :[{
            region : 'north',
            height : 75,
            border : false,
            bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(WtfGlobal.getLocaleText('pm.announcements.create'), WtfGlobal.getLocaleText('pm.announcements.toppanel.text')+"<br><span style=\"float:right; \">"+WtfGlobal.getLocaleText('pm.common.requiredfields')+"</span>","../../images/announcement40_52.gif")
        },this.AnnForm = new Wtf.form.FormPanel({
            region: 'center',
            border: false,
            id: 'announce',
            bodyStyle: 'background:#f1f1f1; font-size:10px; padding:20px;',
            layout: 'form',
            items: [
            {
                xtype : 'textarea',
                id: "announcement" + this.id,
                name: "announcement" + this.id,
                fieldLabel: WtfGlobal.getLocaleText('lang.announcements.text')+'*',
                allowBlank:false,
                height : 80,
                width: 250
            },
            this.frmDate = new Wtf.form.DateTimeField({
                fieldLabel: WtfGlobal.getLocaleText('pm.admin.announcement.displyfrom')+'*',
                id: "fromdate" + this.id,
                name: "fromdate" + this.id,
                width: 250,
                dateFormat: WtfGlobal.getOnlyDateFormat(),
                empty: WtfGlobal.getLocaleText('lang.start.text'),
                dateConfig: {
                    allowBlank: false
                },
                timeConfig: {
                    altFormats: 'H:i:s',
                    allowBlank: false
                },
                scope: this
            }),
            this.uptoDate = new Wtf.form.DateTimeField({
                fieldLabel: WtfGlobal.getLocaleText('pm.admin.announcement.displyupto')+'*',
                id: "todate" + this.id,
                name: "todate" + this.id,
                width: 250,
                dateFormat: WtfGlobal.getOnlyDateFormat(),
                empty: WtfGlobal.getLocaleText('lang.End.text'),
                dateConfig: {
                    allowBlank: false
                },
                timeConfig: {
                    altFormats: 'H:i:s',
                    allowBlank: false
                },
                scope: this
            })
            //                this.frmDate = new Wtf.form.DateField({
            //                    id: "fromdate" + this.id,
            //                    name: "fromdate" + this.id,
            //                    format: 'Y-m-d H:i:s',
            //                    fieldLabel: 'Display From*',
            //                    allowBlank:false,
            //                    height : 80,
            //                    validateOnBlur:true,
            //                    vtypeText : "This field is of type Date",
            //                    validator:function(){
            //                        return true;
            //                    },
            //                    value:new Date().format('Y-m-d H:i:s'),
            //                    width: 250
            //                }),
            //                this.uptoDate = new Wtf.form.DateField({
            //                    id: "todate" + this.id,
            //                    name: "todate" + this.id,
            //                    fieldLabel: 'Display Upto*',
            //                    allowBlank:false,
            //                    format: 'Y-m-d H:i:s',
            //                    height : 80,
            //                    validateOnBlur:true,
            //                    vtypeText : "This field is of type Date",
            //                    validator:function(){
            //                        return true;
            //                    },
            //                    value:new Date().format('Y-m-d H:i:s'),
            //                    width: 250
            //                })
            ]
        })]
        });
    Wtf.getCmp("announcement" + this.id).on("change", function(){
        Wtf.getCmp("announcement" + this.id).setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("announcement" + this.id).getValue()));
    }, this);

    var isFuture = function (checkFor,date){
        if(date!==undefined && date != ""){
            //date = date.clearTime(false);
            var retValue = false;
            var todaysDate = new Date();
            todaysDate.format(WtfGlobal.getDateFormat());
            todaysDate.clearTime();
            if((checkFor=="From") && (todaysDate<=date))
                retValue = true;
            else if((checkFor=="To") && (todaysDate<=date))
                retValue = true;
            else
                retValue = false;
            if(!retValue)
                msgBoxShow(163, 1);
            return retValue;
        }
    };
    this.frmDate.on('blur',function(){
        if(!(isFuture("From",this.frmDate.getValue()))) {
            this.frmDate.setValue("");
            return;
        }
        if(!(this.uptoDate.getValue()==""&&this.uptoDate.getValue()!=null)) {
            if(this.frmDate.getValue()>this.uptoDate.getValue()){
                msgBoxShow(161, 1);
                this.frmDate.setValue("");
            }
        }
    },this);
    this.uptoDate.on('blur',function(){
        if(!(isFuture("To",this.uptoDate.getValue()))) {
                this.uptoDate.setValue("");return;
        }
        if(!(this.frmDate.getValue()==""&&this.frmDate.getValue()!=null)&&
            !(this.uptoDate.getValue()==""&&this.uptoDate.getValue()!=null)) {
            if(this.frmDate.getValue()>this.uptoDate.getValue()){
                msgBoxShow(162, 1);
                this.uptoDate.setValue("");
            }
        }
    },this);
    this.annwin.show();
    focusOn("announcement"+this.id);
}
});

function randomString(){
    var chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
    var string_length = 8;
    var randomstring = '';
    for (var i=0; i<string_length; i++) {
        var rnum = Math.floor(Math.random() * chars.length);
        randomstring += chars.substring(rnum,rnum+1);
    }
    return randomstring;
}

Wtf.MapBasecampdata = function(config){
    Wtf.apply(this, config);
    this.iconCls = 'iconwin';
    Wtf.MapBasecampdata.superclass.constructor.call(this, config);
    this.addEvents = {
        "onsuccess" : true
    };
};


Wtf.extend(Wtf.MapBasecampdata, Wtf.Window, {
    initComponent: function(){
        Wtf.MapBasecampdata.superclass.initComponent.call(this);
        this.title = WtfGlobal.getLocaleText('pm.admin.project.importfrombasecamp');
        this.layout = 'fit';
        this.width = 650;
        this.height = 450;
        this.modal = true;
    },

    onRender: function(config){
        Wtf.MapBasecampdata.superclass.onRender.call(this, config);
        this.userds = new Wtf.data.JsonStore({
            url: "../../admin.jsp?mode=21&action=0",
            root:'data',
            fields : ['userid','name','emailid', 'firstName', 'lastName', 'username']
        });
        this.userRec = Wtf.data.Record.create([
            {name: 'userid'},
            {name: 'name'},
            {name: 'emailid'}
        ]);
        //        this.userds = new Wtf.data.Store({
        //            url: "../../admin.jsp?mode=21&action=0",
        //            reader : new Wtf.data.KwlJsonReader({
        //                root: "data",
        //                totalProperty: 'count'
        //            },this.userRec)
        //        });
        this.userdsFlag = true;
        this.userds.on("load",function(){
            //            var newresentry = new this.userRec({
            //                userid: '-1',
            //                name: 'Create New',
            //                emailid:""
            //            });
            //            this.userds.insert(this.userds.getCount(), newresentry);
            this.userdsFlag = false;
        },this);
        this.userds.load();
        this.userCombo = new Wtf.form.ComboBox({
            store: this.userds,
            displayField: 'name',
            valueField: 'userid',
            typeAhead: true,
            mode: 'local',
            forceSelection: true,
            emptyText: WtfGlobal.getLocaleText('pm.common.clicktoselect'),
            editable: true,
            triggerAction: 'all',
            forceSelection: true,
            selectOnFocus: true
        });
        this.userCombo.on("select",function(combo,record,index){
            if(record.data.userid == '-1')
                this.CreateNewUser();
        },this);
        this.userCombo.on("blur",function(comboBox){
            comboBox.store.clearFilter(true);
        },this)

        this.listds = new Wtf.data.JsonStore({
            id : "listds"+this.id,
            root: 'data',
            data : eval('('+this.newresource+')'),
            fields: ['id', 'username','email','userid','lastName','firstName']
        });
        this.listcm = new Wtf.grid.ColumnModel([{
            header: WtfGlobal.getLocaleText('pm.project.basecamp.user'),
            dataIndex: 'username',
            renderer: this.displayName
        },{
            header: WtfGlobal.getLocaleText('pm.project.user.map'),
            dataIndex: 'userid',
            editor: this.userCombo,
            renderer : this.comboBoxRenderer(this.userCombo)
        }]);
        this.createGP();
        this.projlistds = new Wtf.data.JsonStore({
            id : "projlistds"+this.id,
            root: 'projdata',
            data : eval('('+this.newresource+')'),
            fields: ['projectid','projectname','milestone','todos','post', 'status']
        });
        this.smforproj = new Wtf.grid.RowSelectionModel({
            singleSelect:true
        });
        this.projlistcm = new Wtf.grid.ColumnModel([{
            header: WtfGlobal.getLocaleText('pm.createeditproject.projectname'),
            dataIndex: 'projectname'
        },{
            header: WtfGlobal.getLocaleText('lang.status.text'),
            dataIndex: 'status',
            renderer : function(val){
                if(val == 'active')
                    return WtfGlobal.getLocaleText('lang.active.text');
                else
                    return "Archived/Template";
            }
        },{
            header: WtfGlobal.getLocaleText('pm.project.molestone.import'),
            dataIndex: 'milesstone',
            renderer : this.checkBoxRen
        },{
            header: WtfGlobal.getLocaleText('pm.project.todos.import'),
            dataIndex: 'todo',
            renderer : this.checkBoxRen
        },{
            header: WtfGlobal.getLocaleText('pm.omport.post'),
            dataIndex: 'post',
            renderer : this.checkBoxRen
        }/*,{
            renderer: this.selLinkrend
        }*/]);

        this.projListGrid = new Wtf.grid.EditorGridPanel({
            id:'projlist' + this.id,
            clicksToEdit : 1,
            store: this.projlistds,
            cm: this.projlistcm,
            sm : this.smforproj,
            border : false,
            width: 434,
            loadMask : true,
            viewConfig: {
                forceFit:true
            }
        });
        //        this.smforproj.on('selectionchange',this.selectionchange,this);
        //        this.smforproj.on('rowdeselect ',this.rowdeselectHand,this);
        //        this.projListGrid.on("cellclick", this.gridcellClickHandle, this);
        this.innerpanel = this.add(new Wtf.Panel({
            layout: 'border',
            items: [{
                region : 'north',
                height : 75,
                border : false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                html : getTopHtml(WtfGlobal.getLocaleText('pm.admin.project.importfrombasecamp'), WtfGlobal.getLocaleText('pm.admin.project.basecamp.headersubtext')+"<br><span style=\"float:right; \">"+WtfGlobal.getLocaleText('pm.common.requiredfields')+"</span>","../../images/basecamp-50-52.png")
            },{
                region:'center',
                border:false,
                bodyStyle:'background:#f1f1f1;font-size:10px;',
                layout : 'fit',
                items:[this.projListGrid]
            },{
                region : 'south',
                border :false,
                bodyStyle:'background:#f1f1f1;font-size:10px;',
                layout : 'border',
                height : 160,
                items :[{
                    region:'north',
                    border:false,
                    layout : 'fit',
                    height:135,
                    items:this.grid
                },{
                    region:'center',
                    border: false,
                    html:"<span style='text-align:center !important; color:gray !important;font-size:10px !important;font-style:italic !important;width:100%; display:block;'>*Disclaimer: Basecamp® is a different service and has nothing to do with deskera.The names and logos for Basecamp and 37signals are registered trademarks of 37signals, LLC.</span>"
                }]
            }],
            buttons: [{
                text : WtfGlobal.getLocaleText('lang.import.text'),
                scope: this,
                handler: function(){
                    this.mapImportedRes();
                }
            },{
                text : WtfGlobal.getLocaleText('lang.cancel.text'),
                scope : this,
                id:"cancelUserToProjButton",
                handler : function(){
                    this.close();
                }
            }]
        }));
        this.grid.on("validateedit",function(e){
            if(e.field == 'userid' && e.value =='' ) {
                e.cancel = true;
                return;
            }
        },this);
    },
    /* selectionchange:function(sm){
        var records = sm.getSelections();
        for(var i=0;i<sm.getCount();i++){
            var possition = this.projlistds.find('projectid',records[i].data.projectid);
            this.projListGrid.getView().getCell(possition,1).firstChild.firstChild.checked = true;
            this.projListGrid.getView().getCell(possition,2).firstChild.firstChild.checked = true;
//            this.projListGrid.getView().getCell(possition,3).firstChild.firstChild.checked = true;
        }
    },
    rowdeselectHand:function(sm,rowno,record){
         this.projListGrid.getView().getCell(rowno,1).firstChild.firstChild.checked = false;
         this.projListGrid.getView().getCell(rowno,2).firstChild.firstChild.checked = false;
//         this.projListGrid.getView().getCell(rowno,3).firstChild.firstChild.checked = false;
    },*/
    displayName:function(value,gridcell,record,d,e){
        return (record.data.firstName+" "+record.data.lastName).trim();
    },
    checkBoxRen : function(){
        return '<input type="Checkbox" checked="true" class="checkboxclick"/>';
    },
    selLinkrend : function(a,b,c,d,e){
        //return "<a class='attachmentlink' href=\"#\" onclick=\"selLinkclick(\'"+this.id+"\')\">Deselect</a>";
        return "<a class='deselect' style = 'color:#083772;' href=\"#\">Deselect</a>";
    },
    /*selLinkclick:function(){
        var records = sm.getSelected();
        var possition = this.projlistds.find('projectid',records.data.projectid);
        this.projListGrid.getView().getCell(possition,1).firstChild.firstChild.checked = false;
        this.projListGrid.getView().getCell(possition,2).firstChild.firstChild.checked = false;
        this.projListGrid.getView().getCell(possition,3).firstChild.firstChild.checked = false;
    },*/
    gridcellClickHandle :function(obj,row,col,e) {
        var event = e ;
        if(event.getTarget("a[class='deselect']")){
            var link = this.projListGrid.getView().getCell(row,col).firstChild.firstChild;
            link.className = "select";
            link.innerHTML = WtfGlobal.getLocaleText('pm.project.calendar.import.select');
            this.projListGrid.getView().getCell(row,1).firstChild.firstChild.checked = false;
            this.projListGrid.getView().getCell(row,2).firstChild.firstChild.checked = false;
            this.projListGrid.getView().getCell(row,3).firstChild.firstChild.checked = false;
        }else if(event.getTarget("a[class='select']")){
            link = this.projListGrid.getView().getCell(row,col).firstChild.firstChild;
            link.className = "deselect";
            link.innerHTML = "Deselect";
            this.projListGrid.getView().getCell(row,1).firstChild.firstChild.checked = true;
            this.projListGrid.getView().getCell(row,2).firstChild.firstChild.checked = true;
            this.projListGrid.getView().getCell(row,3).firstChild.firstChild.checked = true;
        }else if(event.getTarget("input[class='checkboxclick']")){
            if(this.projListGrid.getView().getCell(row,1).firstChild.firstChild.checked == true ||
                this.projListGrid.getView().getCell(row,2).firstChild.firstChild.checked == true ||
                this.projListGrid.getView().getCell(row,3).firstChild.firstChild.checked == true){
                link = this.projListGrid.getView().getCell(row,4).firstChild.firstChild;
                link.className = "deselect";
                link.innerHTML = "Deselect";
            }else{
                link = this.projListGrid.getView().getCell(row,4).firstChild.firstChild;
                link.className = "select";
                link.innerHTML = WtfGlobal.getLocaleText('pm.project.calendar.import.select');
            }
        }
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
            loadMask : true,
            viewConfig: {
                forceFit:true
            }
        }));
    },

    searchUser: function(ID) {
        var index =  this.userds.findBy(function(record) {
            if(record.get("userid")==ID)
                return true;
            else
                return false;
         });
        if(index == -1)
            return null;
        return index;
    },

    mapImportedRes : function(flag) {
        this.innerpanel.buttons[0].disable()
        var jsonData = "[{userdata:[";
        for(var cnt =0 ;cnt<this.listds.getCount();cnt++) {
            var rec = this.listds.getAt(cnt);
            var idx = this.searchUser(rec.data.userid)
            if(idx != null){
                var userRec = this.userds.getAt(idx);
                jsonData +=  "{id:\""+rec.data.id+"\",userid:\""+rec.data.userid+"\",username:\""+userRec.data.username+"\",lastName:\""+userRec.data.lastName+"\",firstName:\""+userRec.data.firstName+"\"},";
            } else {
                jsonData +=  "{id:\""+rec.data.id+"\",userid:\""+rec.data.userid+"\",username:\""+rec.data.username+"\",lastName:\""+rec.data.lastName+"\",firstName:\""+rec.data.firstName+"\"},";
            }
        }
        jsonData = jsonData.substr(0, jsonData.length - 1) + "],";
        jsonData = jsonData+"projdata:[";
        for(var cnt =0 ;cnt<this.projlistds.getCount();cnt++) {
            var post = 0;
            var todos = 0;
            var milstones = 0;
            var flag = false;
            if(this.projListGrid.getView().getCell(cnt,1).firstChild.firstChild.checked == true){
                milstones ='1';
                flag = true;
            }else{
                milstones ='0';
            }
            if(this.projListGrid.getView().getCell(cnt,2).firstChild.firstChild.checked == true){
                todos ='1';
                flag = true;
            }else{
                todos ='0';
            }
            if(this.projListGrid.getView().getCell(cnt,3).firstChild.firstChild.checked == true){
                post ='1';
                flag = true;
            }else{
                post ='0';
            }
            if(flag){
                var rec = this.projlistds.getAt(cnt);
                jsonData +=  "{projectid:\""+rec.data.projectid+"\",projectname:\""+rec.data.projectname+"\",milestone:\""+milstones+"\",todos:\""+todos+"\",post:\""+post+"\"},";
            }
        }
        jsonData = jsonData.substr(0, jsonData.length - 1) + "]}]";
        Wtf.Ajax.requestEx({
            url: '../../jspfiles/importfrombaseCamp.jsp',
            params: ({
                action: 2,
                docid : this.docid,
                //                   userchoice : this.userchoice,
                //                   type : this.type,
                //                   projectid :this.projectid,
                //                   isres : this.isres,
                //                   isweek : this.isweek,
                //                   isholiday : this.isholiday,
                val: jsonData
            }),
            method: 'POST'
        },
        this,
        function(result, req){
            if(result!=null && result != "")
                msgBoxShow([WtfGlobal.getLocaleText('lang.success.text'),result], 0);
            collectProjectData();
            this.projds.reload();
            this.close();
        },function(){
            this.close();
        });
    },

    comboBoxRenderer : function(combo) {
        return function(value) {
            var idx = combo.store.find(combo.valueField, value);
            if(idx == -1)
                return "<span style='color:gray;'>"+combo.emptyText+"</span>";
            var rec = combo.store.getAt(idx);
            combo.store.reload();
            return rec.get(combo.displayField);
        };
    },

    CreateNewUser : function() {
        var grid = this.grid;
        var rec = grid.getSelectionModel().getSelected();
        this.newUser = new Wtf.Window({
            title: WtfGlobal.getLocaleText('pm.project.user.create'),
            layout: "border",
            resizable: false,
            iconCls : 'iconwin',
            modal: true,
            autoScroll : true,
            height: 435,
            width: 440,
            items: [{
                region: "north",
                height : 75,
                border : false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                html: getTopHtml(WtfGlobal.getLocaleText('pm.project.user.create'),WtfGlobal.getLocaleText('pm.project.user.create')+"<br><span style=\"float:right; \">"+WtfGlobal.getLocaleText('pm.common.requiredfields')+"</span>","../../images/createuser40_52.gif")
            },{
                region: "center",
                border: false,
                bodyStyle : 'background:#f1f1f1;font-size:10px;',
                layout: "fit",
                items:[ newResForm = new Wtf.form.FormPanel({
                    url: '../../admin.jsp?action=1&mode=0&emode=0',
                    labelWidth: 120,
                    autoScroll : true,
                    defaults: {width: 220},
                    method : 'POST',
                    fileUpload: true,
                    defaultType: "textfield",
                    bodyStyle: "padding:20px;",
                    items:[{
                        fieldLabel: 'User ID*',
                        name:'username',
                        allowBlank:false,
                        //                            disabled: params.mode==0?false:true,
                        value: rec.data.username,
                        validator:WtfGlobal.validateUserid,
                        maxLength : 36//,
                    //                            disabled: params.mode==1?true:false
                    },{
                        fieldLabel: 'Email Address*',
                        name: 'emailid',
                        allowBlank:false,
                        maxLength : 100,
                        validator:WtfGlobal.validateEmail,
                        //vtype: 'email',
                        value: rec.data.email
                    },{
                        fieldLabel: 'First Name*',
                        name: 'fname',
                        allowBlank:false,
                        validator:WtfGlobal.validateUserName,
                        maxLength : 50,
                        value: rec.data.firstName
                    },{
                        fieldLabel: 'Last Name*',
                        name: 'lname',
                        allowBlank:false,
                        validator:WtfGlobal.validateUserName,
                        maxLength : 50,
                        value: rec.data.lastName
                    },{
                        fieldLabel: WtfGlobal.getLocaleText('pm.user.picture'),
                        name : 'image',
                        height: 24,
                        inputType : 'file'
                    },{
                        fieldLabel: WtfGlobal.getLocaleText('pm.contact.number'),
                        id: 'contactnumNewUser',
                        name: 'contactno',
                        maxLength : 15/*,
                            value: params.contactno*/
                    },{
                        xtype : 'textarea',
                        id: 'addressNewUser',
                        fieldLabel: WtfGlobal.getLocaleText('lang.address.text'),
                        height : 80,
                        name : 'address',
                        maxLength : 100/*,
                            value: params.address*/
                    },{
                        xtype : 'hidden',
                        name: 'userid'/*,
                            value: params.userid*/
                    },{
                        xtype : 'hidden',
                        name: 'companyid'/*,
                            value: this.companyid*/
                    }]
                })]
            }],
            buttons:[{
                text:WtfGlobal.getLocaleText('lang.create.text'),
                scope: this,
                handler: function(){
                    this.newUser.buttons[0].disable();
                    if(!newResForm.form.isValid()){
                        this.newUser.buttons[0].enable();
                        return ;
                    }
                    if(this.userds.find("name",newResForm.form.items.items[0].getValue())!=-1){
                        this.newUser.buttons[0].enable();
                        msgBoxShow(171, 1);
                        return;
                    }
                    if(this.userds.find("emailid",newResForm.form.items.items[1].getValue())!=-1){
                        this.newUser.buttons[0].enable();
                        msgBoxShow(172, 1);
                        return;
                    }
                    newResForm.form.submit({
                        scope: this,
                        params: {
                            chkflag: true,
                            action: 11
                        },
                        success: function(action, res){
                            this.newUser.close();
                            var resobj = eval( "(" + res.response.responseText.trim() + ")" );
                            if(resobj.success) {
                                this.newUserid = resobj.userid;
                                var newresentry = new this.userRec({
                                    userid: resobj.userid,
                                    name: newResForm.form.items.items[0].getValue(),
                                    emailid:newResForm.form.items.items[1].getValue()
                                });
                                this.userds.insert(this.userds.getCount()-1, newresentry);
                                this.grid.getSelectionModel().getSelected().set('userid',this.newUserid);
                                var succMess = "User has been created successfully";
                                msgBoxShow([WtfGlobal.getLocaleText('lang.success.text'),succMess], 0);
                            } else {
                                msgBoxShow(174, 1);
                            }
                        },
                        failure: function(action, res){
                            this.newUser.buttons[0].enable();
                            //                             this.newUser.close();
                            msgBoxShow(174, 1);
                        }
                    });
                }
            },{
                text: WtfGlobal.getLocaleText('lang.cancel.text'),
                scope: this,
                handler: function(){
                    this.newUser.close();
                    var records = grid.getSelectionModel().getSelected();
                    var possition =  grid.getStore().find('id',records.data.id);
                    grid.getView().getCell(possition,1).firstChild.innerHTML = "<span style='color:gray;'>Click to select</span>";
                }
            }]
        });
        this.newUser.show();
    }
});

function importFromBaseCamp(projds){

    //        var userchoice = 0;
    var UploadPanel1 = new Wtf.FormPanel({
        width:'100%',
        frame:true,
        method :'POST',
        scope: this,
        fileUpload : true,
        waitMsgTarget: true,
        //            labelWidth: 100,
        items:[{
            bodyStyle: 'padding:5px; width:380px;',
            items: [{
                layout: 'form',
                items:[{
                    xtype : 'textfield',
                    id:'browseBttn',
                    inputType:'file',
                    allowBlank :false,
                    validator:WtfGlobal.validateBaseCampFile,
                    invalidText: WtfGlobal.getLocaleText("pm.common.validatebasecamp.invalidtext"),
                    fieldLabel:WtfGlobal.getLocaleText('pm.common.filename'),
                    name: 'test'
                }/*,{
                            xtype : 'panel',
                            border: false,
                            html:"<span style='color:gray !important;font-size:10px !important;font-style:italic !important;'>*Disclaimer: Basecamp® is a different service and has nothing to do with deskera.The names and logos for Basecamp and 37signals are registered trademarks of 37signals, LLC.</span>"
                        }*/]
            }]
        }
        ]});
    var upWin1 = new Wtf.Window({
        resizable: false,
        scope: this,
        layout: 'fit',
        modal:true,
        width: 420,
        height: 125,
        iconCls: 'iconwin',
        id: 'uploadwindow',
        title: WtfGlobal.getLocaleText('pm.project.plan.import.text'),
        items: UploadPanel1,
        buttons: [{
            text: WtfGlobal.getLocaleText('lang.import.text'),
            id: 'submitPicBttn',
            type: 'submit',
            scope: this,
            handler: function(){
                if(!UploadPanel1.form.isValid())
                    return;
                upWin1.buttons[0].disable();
                UploadPanel1.form.submit({
                    url:'../../jspfiles/importfrombaseCamp.jsp?action=1',
                    waitMsg :WtfGlobal.getLocaleText('pm.importing.text')+'...',
                    scope:this,
                    success: function (result, request) {
                        var obj = eval('('+request.response.responseText+')');
                        if(obj.success) {
                            var data = eval("("+obj.data+")");
                            if(data.error!=""){
                                msgBoxShow([WtfGlobal.getLocaleText('lang.error.text'),data.error], 1);
                                upWin1.buttons[0].enable();
                                return;
                            }
                            var importBaseCamp = new Wtf.MapBasecampdata({
                                newresource : obj.data,
                                projds:projds,
                                newprojData:data.projdata,
                                docid : data.docid
                            });
                            importBaseCamp.show();
                        }
                        upWin1.close();
                        bHasChanged = true;
                        var temp = refreshDash.join();
                        if(temp.indexOf("all") == -1)
                            refreshDash[refreshDash.length] = 'all';
                    },
                    failure: function ( result, request) {
                        this.upWin1.close();
                        msgBoxShow(20, 1);
                    }
                },this);
            }
        },{
            text:WtfGlobal.getLocaleText('lang.cancel.text'),
            id:'canbttn1',
            scope:this,
            handler:function() {
                upWin1.close();
            }
        }]
    },this);
    upWin1.show();
}


var profile = new Wtf.common.MainAdmin({
    id: 'mainAdmin',
    companyid: "",
    layout : 'fit',
    border: false,
    event:Wtf.getCmp("tabcompanyadminpanel").activesubtab
});
Wtf.getCmp("tabcompanyadminpanel").add(profile);
Wtf.getCmp("tabcompanyadminpanel").doLayout();
Wtf.getCmp("tabcompanyadminpanel").body.dom.style.overflow = Wtf.isIE7 ? 'hidden' : 'auto';
function createNewProject(id){
    Wtf.getCmp(id).createButtonClicked();
}

function viewProject(pid,pname){
    mainPanel.loadTab("../../project.html", "   "+pid,pname, "navareadashboard",Wtf.etype.proj,true);
}
