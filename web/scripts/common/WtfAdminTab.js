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
Wtf.common.adminpage = function(config){
    Wtf.common.adminpage.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.common.adminpage, Wtf.Panel, {
    onRender: function(config){
        Wtf.common.adminpage.superclass.onRender.call(this, config);
        this.loadingMask = mainPanel.loadMask;//new Wtf.LoadMask(this.el.dom, Wtf.apply(this.loadingMask));
        this.loadingMask.show();
        //        this.memds = new Wtf.data.JsonStore({
        //            id: "memds" + this.id,
        //            url: '../../admin.jsp',
        //            root: 'data',
        //            method: 'GET',
        //            fields: ['img', 'id', 'username', 'Name', 'email', 'status', 'color']
        //        });
        var grpView = new Wtf.grid.GroupingView({
            forceFit: true,
            showGroupName: false,
            enableGroupingMenu: false,
            hideGroupedColumn: false
        });
        var jRecTmp = new Wtf.data.Record.create([
        {
            name: "img"
        },{
            name: "id"
        },{
            name: "Name"
        },{
            name: "email"
        },{
            name: "status",
            type: "int"
        },{
            name: "planpermission",
            type: "int"
        },{
            name: "inuse"
        }
        ]);
        var jReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: 'count'
        },jRecTmp);

        this.memds = new Wtf.data.GroupingStore({
            id: "memds" + this.id,
            url: '../../admin.jsp',
            reader: jReader,
            baseParams: {
                action: 0,
                mode: this.mode,
                featureid: this.featureid,
                status: 3
            },
            sortInfo: {
                field: 'inuse',
                direction: "DESC"
            }
        });
        this.memds.on("load",function(){
            this.requestgrid.getView().refresh();
        },this);

        this.invitesds = new Wtf.data.GroupingStore({
            id: "invitesds" + this.id,
            url: '../../admin.jsp',
            baseParams: {
                action: 0,
                mode: this.mode,
                featureid: this.featureid,
                status: 2
            },
            reader: jReader
        });
        this.invitesds.on("load",function(){
            this.requestgrid.getView().refresh();
        },this);
        this.requestsds = new Wtf.data.GroupingStore({
            id: "requestsds" + this.id,
            baseParams: {
                action: 0,
                mode: this.mode,
                featureid: this.featureid,
                status: 1
            },
            url: '../../admin.jsp',
            reader: jReader
        });
        this.requestsds.on("load", function(){
            this.requestgrid.store = this.requestsds;
            Wtf.getCmp("paggintoolbar" + this.id).bind(this.requestsds);
            this.quickSearchTF.StorageChanged(this.requestsds);
            this.requestgrid.getView().refresh();
            this.loadingMask.hide();
        }, this);
        this.selectionModel = new Wtf.grid.CheckboxSelectionModel();
        if(!this.archived){
            this.selectionModel.on('rowselect', this.enableButtons, this);
            this.selectionModel.on('rowdeselect', this.enableButtons, this);
        }
        this.cm = new Wtf.grid.ColumnModel([this.selectionModel, {
            dataIndex: 'img',
            width: 30,
            fixed : true,
            renderer: function(value, p, record){
                if(value == "")
                    value = Wtf.DEFAULT_USER_URL;
                return String.format("<img src={0} style='height:18px;width:18px;vertical-align:text-top;'/>", value);
            }
        }, {
            header: WtfGlobal.getLocaleText('lang.name.text'),
            dataIndex: 'Name',
            autoWidth: true,
            sortable: true,
            groupable: true
        }, {
            header: WtfGlobal.getLocaleText('pm.updateprofile.emailaddreass'),
            dataIndex: 'email',
            autoSize: true,
            sortable: true,
            renderer: WtfGlobal.renderEmailTo,
            groupable: true
        }, {
            header: WtfGlobal.getLocaleText('lang.status.text'),
            //dataIndex: 'joindate',
            autoSize: true,
            //                sortable: true,
            groupable: true,
            renderer: function(value) {
                if(value == 1) {
                    return WtfGlobal.getLocaleText('pm.member.requested');
                } else if(value == 2) {
                    return WtfGlobal.getLocaleText('Invited.text');
                } else if(value == 3) {
                    return WtfGlobal.getLocaleText('Member.text');
                } else if(value == 4) {
                    return WtfGlobal.getLocaleText('Moderator.text');
                } else if(value == 3) {
                    return "Member";
                } else if(value == 4) {
                    return "Moderator";
                }
            }
        },{
            header: WtfGlobal.getLocaleText('pm.project.permission.plan'),
            autoSize: true,
            dataIndex: 'planpermission',
            groupable: true,
            renderer: function(value) {
                var ret = "";
                switch(value){
                    case 2:ret = "<div wtf:qtip="+WtfGlobal.getLocaleText('pm.project.plan.permission.tip')+" wtf:qtitle="+WtfGlobal.getLocaleText('pm.project.permission.plan')+">"+WtfGlobal.getLocaleText('pm.permission.viewall')+"</div>";
                        break;
                    case 10:ret = "<div wtf:qtip="+WtfGlobal.getLocaleText('pm.project.plan.permission.tip')+" wtf:qtitle="+WtfGlobal.getLocaleText('pm.project.permission.plan')+">"+WtfGlobal.getLocaleText('pm.permission.viewall')+"</div>";
                        break;
                    case 4:ret = "<div wtf:qtip="+WtfGlobal.getLocaleText('pm.project.plan.permission.tip1')+" wtf:qtitle="+WtfGlobal.getLocaleText('pm.project.permission.plan')+">"+WtfGlobal.getLocaleText('pm.permission.modiall')+"</div>";
                        break;
                    case 18:ret = "<div wtf:qtip="+WtfGlobal.getLocaleText('pm.project.plan.permission.tip2')+" wtf:qtitle="+WtfGlobal.getLocaleText('pm.project.permission.plan')+">"+WtfGlobal.getLocaleText('pm.permission.viewallmodass')+"</div>";
                        break;
                    case 8:ret = "<div wtf:qtip="+WtfGlobal.getLocaleText('pm.project.plan.permission.tip3')+" wtf:qtitle="+WtfGlobal.getLocaleText('pm.project.permission.plan')+">"+WtfGlobal.getLocaleText('pm.permission.viewass')+"</div>";
                        break;
                    case 16:ret = "<div wtf:qtip="+WtfGlobal.getLocaleText('pm.project.plan.permission.tip4')+" wtf:qtitle="+WtfGlobal.getLocaleText('pm.project.permission.plan')+">"+WtfGlobal.getLocaleText('pm.permission.modassonly')+"</div>";
                        break;
                }
                return ret;
            }
        },{
            hidden: true,
            fixed : true,
            groupable: true,
            dataIndex: 'inuse'
        },{
            header: WtfGlobal.getLocaleText('lang.remove.text'),
            width: 60,
            hidden:(this.archived) ? true : false,
            fixed : true,
            groupable: false,
            dataIndex: '',
            id: 'deleteCol',
            renderer: function(){
                return "<img src = '../../images/Delete.gif'wtf:qtip="+WtfGlobal.getLocaleText('pm.project.settings.member.drop.tip')+" style='cursor: pointer;'>";
            }
        }
        ]);

        this.requestgrid = new Wtf.grid.GridPanel({
            id: 'requestgrid' + this.id,
            store: this.memds,
            cm: this.cm,
            layout:'fit',
            loadMask: this.loadingMask,
            sm: this.selectionModel,
            border: false,
            autoScroll:true,
            view: grpView,
            viewConfig: {
                forceFit: true
            }
        });
        this.requestgrid.on('cellclick', this.cellClickHandler, this);
        this.requestgrid.on("rowdblclick",function(Grid,rowIndex,e){
            var data = Grid.getStore().getAt(rowIndex).data;
            mainPanel.loadTab("../../user.html", "tab"+data.id, data.Name, "navareadashboard",Wtf.etype.user,true);
        },this);
        this.memds.on("load", function(){
            this.loadingMask.hide();
        }, this);
        this.memds.load({
            params: {
                start: 0,
                limit: 10
            },
            scope: this,
            callback: function(rec, opt, succ){
                if(succ){
                    this.memds.groupBy("inuse");
                }
            }
        });

        this.MembergridPanel = new Wtf.common.KWLListPanel({
            id: "membergridpanel" + this.id,
            title: WtfGlobal.getLocaleText('pm.project.settings.members.helptext'),
            autoLoad: false,
            autoScroll:true,
            paging: false,
            layout: 'fit',
            items: [this.requestgrid]
        });
        var bottomBar = [];
        if(!this.archived){
            bottomBar = ['-', {
                text: WtfGlobal.getLocaleText('lang.accept.text'),
                id: "accept" + this.id,
                allowDomMove: false,
                hidden: true,
                scope: this,
                disabled: true,
                tooltip: {
                    title:WtfGlobal.getLocaleText('pm.dashboard.widget.requests.text'),
                    text:WtfGlobal.getLocaleText('pm.member.request.accept')
                },
                handler: this.confirmAcceptRejectRequest, //this.bottombarButtonClicked,
                iconCls: 'dpwnd accepticon'
            }, {
                text: WtfGlobal.getLocaleText('lang.reject.text'),
                id: "reject" + this.id,
                allowDomMove: false,
                hidden: true,
                scope: this,
                disabled: true,
                tooltip: {
                    title:WtfGlobal.getLocaleText('pm.dashboard.widget.requests.text'),
                    text:WtfGlobal.getLocaleText('pm.userprofile.reject')
                },
                handler: this.confirmAcceptRejectRequest, //this.bottombarButtonClicked,
                iconCls: 'pwnd rejecticon'
            }, {
                text: WtfGlobal.getLocaleText('lang.invite.text'),
                id: "invite" + this.id,
                allowDomMove: false,
                hidden: true,
                scope: this,
                tooltip: {
                    title:WtfGlobal.getLocaleText('pm.common.invitations'),
                    text:''
                },
                handler: this.bottombarButtonClicked,
                iconCls: 'pwnd invite'
            }, {
                text: WtfGlobal.getLocaleText('pm.project.member.dropmember'),
                id: "dropmem" + this.id,
                allowDomMove: false,
                scope: this,
                disabled: true,
                tooltip: {
                    title:WtfGlobal.getLocaleText('pm.project.members.drops'),
                    text:WtfGlobal.getLocaleText('pm.Help.dropmember')
                },
                handler: this.bottombarButtonClicked,
                iconCls: 'dpwnd dropMember'
            },{
                text: WtfGlobal.getLocaleText('pm.admin.project.setactive'),
                id: "activateMember" + this.id,
                scope: this,
                disabled: true,
                tooltip: {
                    title:WtfGlobal.getLocaleText('pm.admin.project.setactive'),
                    text:WtfGlobal.getLocaleText('pm.Help.activemember')
                },
                handler: this.bottombarButtonClicked,
                iconCls: "dpwnd setActiveMember"
            }, {
                text: WtfGlobal.getLocaleText('pm.project.member.setmoderator'),
                id: "setmod" + this.id,
                allowDomMove: false,
                scope: this,
                disabled: true,
                tooltip: {
                    title:WtfGlobal.getLocaleText('pm.project.member.setmoderator'),
                    text:WtfGlobal.getLocaleText('pm.Help.setmod')
                },
                handler: this.bottombarButtonClicked,
                iconCls: 'dpwnd setModerator'
            }, {
                text: WtfGlobal.getLocaleText('pm.project.member.removemoderator'),
                id: "remmod" + this.id,
                allowDomMove: false,
                scope: this,
                disabled: true,
                tooltip: {
                    title:WtfGlobal.getLocaleText('pm.project.member.removemoderator'),
                    text:WtfGlobal.getLocaleText('pm.Help.remmod')
                },
                handler: this.bottombarButtonClicked,
                iconCls: 'dpwnd remModerator'
            }, {
                text:WtfGlobal.getLocaleText('pm.project.permission.text'),
                id: "chgperm" + this.id,
                allowDomMove: false,
                scope: this,
                disabled: true,
                tooltip: {
                    title:WtfGlobal.getLocaleText('pm.project.permission.text'),
                    text:WtfGlobal.getLocaleText('pm.Help.changeperm')
                },
                handler : this.changePermission,
                iconCls: 'pwnd permicon'
            },
            new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText('pm.project.resources.manageresources'),
                hidden: true,
                id: "manageResMenu" + this.id,
                tooltip: {
                    title:WtfGlobal.getLocaleText('pm.project.resource.management'),
                    text: WtfGlobal.getLocaleText('pm.Help.manageres')
                },
                menu: {
                    items: [{
                        text: WtfGlobal.getLocaleText('pm.project.resource.create'),
                        id: "newRes" + this.id,
                        allowDomMove: false,
                        scope: this,
                        hidden: true,
                        tooltip: {
                            title:WtfGlobal.getLocaleText('pm.project.resource.create'),
                            text:WtfGlobal.getLocaleText('pm.Help.createres')
                        },
                        handler: this.createNewRes,
                        iconCls: 'dpwnd addresource'
                    },{
                        text: WtfGlobal.getLocaleText('pm.project.resource.edit'),
                        id: "editRes" + this.id,
                        allowDomMove: false,
                        scope: this,
                        hidden: true,
                        disabled: true,
                        tooltip: {
                            title:WtfGlobal.getLocaleText('pm.project.resource.edit'),
                            text:WtfGlobal.getLocaleText('pm.Help.editres')
                        },
                        handler: this.createNewRes,
                        iconCls: 'pwnd editresource'
                    },{
                        text: WtfGlobal.getLocaleText('pm.project.resource.delete.text'),
                        id: "delRes" + this.id,
                        allowDomMove: false,
                        scope: this,
                        hidden: true,
                        disabled: true,
                        tooltip: {
                            title:WtfGlobal.getLocaleText('pm.project.resources.delete.text'),
                            text:WtfGlobal.getLocaleText('pm.Help.deleteres')
                        },
                        handler: this.preDeleteResource,
                        iconCls: 'dpwnd deleteresource'
                    },{
                        text: WtfGlobal.getLocaleText('pm.resource.active.text'),
                        id: "actRes" + this.id,
                        allowDomMove: false,
                        scope: this,
                        hidden: true,
                        disabled: true,
                        billable: true,
                        tooltip: {
                            title:WtfGlobal.getLocaleText('pm.resources.active.text'),
                            text: WtfGlobal.getLocaleText('pm.Help.activateres')
                        },
                        handler: this.activateResource,
                        iconCls: 'dpwnd setActiveMember'
                    }]
                }
            }),{
                text: WtfGlobal.getLocaleText('pm.project.resources.setbillable'),
                id: "billable" + this.id,
                allowDomMove: false,
                scope: this,
                hidden: true,
                disabled: true,
                billable: true,
                tooltip: {
                    title:WtfGlobal.getLocaleText('pm.project.resources.setbillable'),
                    text: WtfGlobal.getLocaleText('pm.Help.setbill')
                },
                handler: this.setBillable
            },{
                text: WtfGlobal.getLocaleText('pm.project.resources.unbillable'),
                id: "unbillable" + this.id,
                allowDomMove: false,
                scope: this,
                hidden: true,
                billable: false,
                disabled: true,
                tooltip: {
                    title:WtfGlobal.getLocaleText('pm.project.resources.unbillable'),
                    text: WtfGlobal.getLocaleText('pm.Help.setunbill')
                },
                handler: this.setBillable
            }];
        }
        this.innerpanel = new Wtf.Panel({
            id: 'innerpanel' + this.id,
            layout: 'fit',
            cls: 'backcolor',
            border: false,
            tbar: [/*'Quick Search: ', */this.quickSearchTF = new Wtf.KWLTagSearch({
                width: 200,
                field: WtfGlobal.getLocaleText('lang.name.text'),
                emptyText: WtfGlobal.getLocaleText('pm.common.searchbyname')
            }), '-', {
                text: WtfGlobal.getLocaleText('pm.admin.project.members'),
                id: "memtogglebtn" + this.id,
                tooltip: WtfGlobal.getLocaleText('pm.Help.projectadminmember'),
                enableToggle: true,
                toggleGroup: 'toggle' + this.id,
                pressed: true,
                scope: this,
                handler: function(el, e){
                    if(el.pressed){
                        this.quickSearchTF.show();
                        this.pagingToolbar.bind(this.memds);
                        if(this.MembergridPanel.hidden)
                            this.swapPanel();
                        if(this.requestgrid.hidden)
                            this.swapGrid();
                        var colIndex = this.cm.getIndexById('deleteCol');
                        if(!this.archived)
                            this.cm.setHidden(colIndex, false);
                        this.MembergridPanel.setTitle(WtfGlobal.getLocaleText('pm.project.settings.members.helptext'));
                        this.requestgrid.reconfigure(this.memds, this.cm);
                        //                        this.requestgrid.store = this.memds;
                        this.requestgrid.getView().refresh();
                        this.requestgrid.getView().emptyText = '<div class="emptyGridText">No Members to display</div>' ;
                        Wtf.getCmp("paggintoolbar" + this.id).bind(this.memds);
                        this.quickSearchTF.setValue("");
                        this.quickSearchTF.StorageChanged(this.memds);
                        if(!this.archived){
                            Wtf.getCmp("accept" + this.id).hide();
                            Wtf.getCmp("reject" + this.id).hide();
                            Wtf.getCmp("newRes" + this.id).hide();
                            Wtf.getCmp("delRes" + this.id).hide();
                            Wtf.getCmp("actRes" + this.id).hide();
                            Wtf.getCmp("editRes" + this.id).hide();
                            Wtf.getCmp("manageResMenu" + this.id).hide();
                            Wtf.getCmp("unbillable" + this.id).hide();
                            Wtf.getCmp("billable" + this.id).hide();
                            Wtf.getCmp("invite" + this.id).hide();
                            Wtf.getCmp("dropmem" + this.id).show();
                            Wtf.getCmp("activateMember" + this.id).show();
                            Wtf.getCmp("setmod" + this.id).show();
                            Wtf.getCmp("remmod" + this.id).show();
                            Wtf.getCmp("chgperm" + this.id).show();
                            this.disableButtons();
                        }
                        this.memds.load({
                            params: {
                                start: 0,
                                limit: this.pagingToolbar.pageSize
                            },
                            scope: this,
                            callback: function(rec, opt, succ){
                                if(succ){
                                    this.memds.groupBy("inuse");
                                }
                            }
                        });
                    } else
                        el.toggle();
                },
                iconCls: 'dpwnd communityTabIcon'
            }, '-', {
                text: WtfGlobal.getLocaleText('pm.common.invitations'),
                id: "invitetogglebtn" + this.id,
                tooltip: WtfGlobal.getLocaleText('pm.Help.projectadmininvitation'),
                enableToggle: true,
                toggleGroup: 'toggle' + this.id,
                scope: this,
                handler: function(el, e){
                    if(el.pressed){
                        this.quickSearchTF.show();
                        this.loadingMask.show();
                        //                        this.pagingToolbar.bind(this.invitesds);
                        this.invitesds.load({
                            params: {
                                start: 0,
                                limit: this.pagingToolbar.pageSize
                            }
                        });
                        if(this.MembergridPanel.hidden)
                            this.swapPanel();
                        if(this.requestgrid.hidden)
                            this.swapGrid();
                        var colIndex = this.cm.getIndexById('deleteCol');
                        this.cm.setHidden(colIndex, true);
                        this.MembergridPanel.setTitle(WtfGlobal.getLocaleText('pm.project.settings.invitation.helptext'));
                        this.requestgrid.reconfigure(this.invitesds, this.cm);
                        //                        this.requestgrid.store = this.invitesds;
                        this.requestgrid.getView().refresh();
                        if(!this.archived)
                            this.requestgrid.getView().emptyText = '<div class="emptyGridText">'+WtfGlobal.getLocaleText('pm.project.settings.invitation.emptytext')+' <br><a href="#" onClick=\'getInvitations(\"'+this.id+'\")\'>'+WtfGlobal.getLocaleText('pm.project.settings.invitation.start')+'</a></div>';
                        Wtf.getCmp("paggintoolbar" + this.id).bind(this.invitesds);
                        this.quickSearchTF.setValue("");
                        this.quickSearchTF.StorageChanged(this.invitesds);
                        if(!this.archived){
                            Wtf.getCmp("accept" + this.id).hide();
                            Wtf.getCmp("reject" + this.id).hide();
                            Wtf.getCmp("invite" + this.id).show();
                            Wtf.getCmp("newRes" + this.id).hide();
                            Wtf.getCmp("delRes" + this.id).hide();
                            Wtf.getCmp("actRes" + this.id).hide();
                            Wtf.getCmp("editRes" + this.id).hide();
                            Wtf.getCmp("manageResMenu" + this.id).hide();
                            Wtf.getCmp("unbillable" + this.id).hide();
                            Wtf.getCmp("billable" + this.id).hide();
                            Wtf.getCmp("dropmem" + this.id).hide();
                            Wtf.getCmp("activateMember" + this.id).hide();
                            Wtf.getCmp("setmod" + this.id).hide();
                            Wtf.getCmp("remmod" + this.id).hide();
                            Wtf.getCmp("chgperm" + this.id).hide();
                        }
                    } else
                        el.toggle();
                },
                iconCls: 'dpwnd inviteUsericon'
            }, '-', {
                text: WtfGlobal.getLocaleText('pm.dashboard.widget.requests.text'),
                id: "reqtogglebtn" + this.id,
                tooltip: WtfGlobal.getLocaleText('pm.Help.projectadminrequest'),
                enableToggle: true,
                toggleGroup: 'toggle' + this.id,
                scope: this,
                handler: function(el, e){
                    if(el.pressed){
                        this.quickSearchTF.show();
                        this.loadingMask.show();
                        //                        this.pagingToolbar.bind(this.requestsds);
                        this.requestsds.load({
                            params: {
                                start: 0,
                                limit: this.pagingToolbar.pageSize
                            }
                        });
                        if(this.MembergridPanel.hidden)
                            this.swapPanel();
                        if(this.requestgrid.hidden)
                            this.swapGrid();
                        var colIndex = this.cm.getIndexById('deleteCol');
                        this.cm.setHidden(colIndex, true);
                        this.MembergridPanel.setTitle(WtfGlobal.getLocaleText('pm.project.settings.request.helptext'));
                        this.requestgrid.reconfigure(this.requestsds, this.cm);
                        //                        this.requestgrid.store = this.requestsds;
                        this.requestgrid.getView().refresh();
                        this.requestgrid.getView().emptyText = '<div class="emptyGridText">'+WtfGlobal.getLocaleText('pm.project.settings.request.emptytext')+'</div>' ;
                        Wtf.getCmp("paggintoolbar" + this.id).bind(this.requestsds);
                        this.quickSearchTF.setValue("");
                        this.quickSearchTF.StorageChanged(this.requestsds);
                        if(!this.archived){
                            Wtf.getCmp("accept" + this.id).show();
                            Wtf.getCmp("reject" + this.id).show();
                            Wtf.getCmp("newRes" + this.id).hide();
                            Wtf.getCmp("delRes" + this.id).hide();
                            Wtf.getCmp("actRes" + this.id).hide();
                            Wtf.getCmp("editRes" + this.id).hide();
                            Wtf.getCmp("manageResMenu" + this.id).hide();
                            Wtf.getCmp("unbillable" + this.id).hide();
                            Wtf.getCmp("billable" + this.id).hide();
                            Wtf.getCmp("invite" + this.id).hide();
                            Wtf.getCmp("dropmem" + this.id).hide();
                            Wtf.getCmp("activateMember" + this.id).hide();
                            Wtf.getCmp("setmod" + this.id).hide();
                            Wtf.getCmp("remmod" + this.id).hide();
                            Wtf.getCmp("chgperm" + this.id).hide();
                            this.disableButtons();
                        }
                    } else
                        el.toggle();
                },
                iconCls: 'dpwnd requests'
            }, '-', {
                text: WtfGlobal.getLocaleText('pm.common.resources'),
                id: "restogglebtn" + this.id,
                tooltip: WtfGlobal.getLocaleText('pm.Help.projectadminresource'),
                enableToggle: true,
                toggleGroup: 'toggle' + this.id,
                scope: this,
                handler: function(obj){
                    if(obj.pressed){
                        this.quickSearchTF.show();
                        if(this.MembergridPanel.hidden)
                            this.swapPanel();
                        if(this.resGrid === undefined)
                            this.showProjectResources();
                        else
                            this.swapGrid();
                    }else
                        obj.toggle();
                },
                iconCls: "dpwnd resourcesicon"
            }, '-', {
                text: WtfGlobal.getLocaleText('Project.Calendar'),
                id: "projAdmin" + this.id,
                title:WtfGlobal.getLocaleText('pm.calender.currentworkweek'),
                tooltip: WtfGlobal.getLocaleText('pm.Help.projectcal'),
                enableToggle: true,
                toggleGroup: 'toggle' + this.id,
                scope: this,
                handler: function(obj){
                    if(obj.pressed) {
                        this.quickSearchTF.hide();
                        if(this.calendarPanel === undefined)
                            this.createCalendarPanel();
                        else
                            this.swapPanel();
                    } else
                        obj.toggle();
                    this.MembergridPanel.setTitle(WtfGlobal.getLocaleText('pm.calender.currentworkweek'));
                },
                iconCls: "dpwnd projcalicon"
            }],
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 10,
                id: "paggintoolbar" + this.id,
                store: this.memds,
                searchField: this.quickSearchTF,
                displayInfo: true,
//                displayMsg: WtfGlobal.getLocaleText('pm.paging.displayrecord'),
                emptyMsg: WtfGlobal.getLocaleText('pm.paging.noresult'),
                plugins: this.pP = new Wtf.common.pPageSize({id : "pPageSize_"+this.id}),
                items: bottomBar
            }),
            items: [this.MembergridPanel]
        });

        var jRecPop = new Wtf.data.Record.create([
        {
            name: "img"
        },{
            name: "id"
        }/*,{
                name: "username",
                type: "string"
            }*/,{
            name: "Name"
        },{
            name: "email"
        },{
            name: "status",
            type: "int"
        }
        ]);
        var jReaderPop = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: 'count'
        },jRecPop);

        this.popds = new Wtf.data.Store({
            id: "popds" + this.id,
            url: '../../admin.jsp',
            reader: jReaderPop,
            method: 'GET',
            baseParams: {
                action: 0,
                mode: this.mode,
                featureid: this.featureid,
                status: 4
            }
        });
        this.popds.on("load",function(){
            this.popqs.StorageChanged(this.popds);
            this.popqs.on('SearchComplete', function() {
                this.popgrid.getView().refresh();
            }, this);
            this.popInvite.show();
            this.popgrid.getView().refresh();
        },this);

        this.popsm = new Wtf.grid.CheckboxSelectionModel();
        this.popcm = new Wtf.grid.ColumnModel([this.popsm, {
            dataIndex: 'img',
            width: 30,
            fixed : true,
            renderer: function(value, p, record){
                if(value == "")
                    value = Wtf.DEFAULT_USER_URL;
                return String.format("<img src={0} style='height:18px;width:18px;vertical-align:text-top;'/>", value);
            }
        }/*, {
                header: WtfGlobal.getLocaleText('pm.common.UserId'),
                dataIndex: 'username',
                autoWidth: true,
                sortable: true,
                groupable: true
            }*/, {
            header: WtfGlobal.getLocaleText('lang.name.text'),
            dataIndex: 'Name',
            autoWidth: true,
            sortable: true,
            groupable: true
        }, {
            header: WtfGlobal.getLocaleText('pm.updateprofile.emailaddreass'),
            dataIndex: 'email',
            autoSize: true,
            sortable: true,
            groupable: true
        }]);

        this.memds.on('load', this.myStoreChange, this);
        this.invitesds.on('load', this.myStoreChange, this);
        this.requestsds.on('load', this.myStoreChange, this);
        //this.quickSearchTF.StorageChanged(this.memds);
        this.quickSearchTF.on('SearchComplete', this.QuickSearchComplete, this);
        this.memds.on('datachanged', this.onStoreDataChanged,this);
        this.invitesds.on('datachanged', this.onStoreDataChanged,this);
        this.requestsds.on('datachanged', this.onStoreDataChanged,this);
        this.add(this.innerpanel);
    },

    onStoreDataChanged : function() {
        this.quickSearchTF.setPage(this.pP.combo.value);
    },
    myStoreChange : function(store, records, obj){
        this.loadingMask.hide();
        this.quickSearchTF.StorageChanged(store);
    },

    setBillable: function(obj){
        var act = WtfGlobal.getLocaleText('pm.resource.billable');
        if(!obj.billable){
          act = WtfGlobal.getLocaleText('pm.project.settings.resource.unbillable');
        }
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText({key:'pm.msg.resource.select',params:act}), function(btn){
            if (btn == "yes") {
                var selected = this.resGrid.getSelectionModel().getSelections();
                var uids = "";
                for(var i = 0; i < selected.length; i++)
                    uids += selected[i].data.resourceid + ",";
                Wtf.Ajax.requestEx({
                    url: "../../jspfiles/project/resources.jsp",
                    params: {
                        uids: uids.substring(0, uids.length - 1),
                        billable: obj.billable,
                        projid: this.featureid,
                        action: 9
                    },
                    method:'POST'},
                this,
                function() {
                    this.resGrid.getStore().reload();
                    Wtf.getCmp("editRes" + this.id).disable();
                    Wtf.getCmp("unbillable" + this.id).disable();
                    Wtf.getCmp("billable" + this.id).disable();
                    Wtf.getCmp("delRes" + this.id).disable();
                },
                function() {
                    Wtf.getCmp("editRes" + this.id).disable();
                    Wtf.getCmp("unbillable" + this.id).disable();
                    Wtf.getCmp("billable" + this.id).disable();
                    Wtf.getCmp("delRes" + this.id).disable();
                });
            }
        }, this);
    },

    showProjectResources: function(){
        this.loadingMask.show();
        var grpView = new Wtf.grid.GroupingView({
            forceFit: true,
            showGroupName: false,
            enableGroupingMenu: false,
            hideGroupedColumn: false
        });
        var jrecord = Wtf.data.Record.create([
        {
            name: "resourceid",
            mapping: 'resourceID'
        },{
            name: "resourcename",
            mapping: 'resourceName'
        },{
            name: "colorcode",
            mapping: 'colorCode'
        },{
            name: "stdrate",
            type: "float",
            mapping: 'stdRate'
        },{
            name: "typename",
            mapping: 'type.typeName'
        },{
            name: "typeid",
            type: "int",
            mapping: 'type.typeID'
        },{
            name: "categoryname",
            mapping: 'category.categoryName'
        },{
            name: "categoryid",
            type: "int",
            mapping: 'category.categoryID'
        },{
            name: "wuvalue",
            type: "int",
            mapping: 'wuvalue'
        },{
            name: "billable",
            type: "boolean",
            mapping: 'billable'
        },{
            name: "teamname"
        },{
            name: "Name"
        },{
            name: 'inuseflag',
            type: 'boolean',
            mapping: 'inUseFlag'
        }
        ]);
        var jReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: 'count'
        },jrecord);

        this.grpStore = new Wtf.data.GroupingStore({
            url: '../../jspfiles/project/resources.jsp',
            reader: jReader,
            baseParams: {
                action: 2,
                projid: this.featureid
            },
            sortInfo: {
                field: 'typename',
                direction: "DESC"
            }
        });
        this.pagingToolbar.bind(this.grpStore);

        this.grpStore.load({
            params: {
                start: 0,
                limit: this.pagingToolbar.pageSize
            },
            scope: this,
            callback: function(rec, opt, succ){
                if(succ){
                    if(!this.archived)
                        this.MembergridPanel.setTitle(WtfGlobal.getLocaleText('pm.project.settings.resources.helptext')+'<a href="#" onclick=\'createNewRes(\"'+this.id+'\")\'><img src="../../images/user_add.gif" style="margin-bottom:-4px; margin-left:5px;" wtf:qtitle="'+WtfGlobal.getLocaleText('pm.project.resources.create')+'" wtf:qtip="'+WtfGlobal.getLocaleText('pm.Help.createres')+'"/></a>');
                    else
                        this.MembergridPanel.setTitle(WtfGlobal.getLocaleText('pm.common.resources'));
                    this.quickSearchTF.setValue("");
                    this.quickSearchTF.StorageChanged(this.grpStore);
                    this.requestgrid.hide();
                    this.MembergridPanel.add(this.resGrid);
                    this.resGrid.setHeight(this.MembergridPanel.getSize().height);
                    this.resGrid.setWidth(this.MembergridPanel.getSize().width);
                    this.MembergridPanel.doLayout();
                    this.grpStore.groupBy("typename");
                    if(!this.archived){
                        Wtf.getCmp("accept" + this.id).hide();
                        Wtf.getCmp("reject" + this.id).hide();
                        Wtf.getCmp("newRes" + this.id).show();
                        Wtf.getCmp("delRes" + this.id).show();
                        Wtf.getCmp("actRes" + this.id).show();
                        Wtf.getCmp("editRes" + this.id).show();
                        Wtf.getCmp("manageResMenu" + this.id).show();
                        Wtf.getCmp("unbillable" + this.id).show();
                        Wtf.getCmp("billable" + this.id).show();
                        Wtf.getCmp("invite" + this.id).hide();
                        Wtf.getCmp("dropmem" + this.id).hide();
                        Wtf.getCmp("activateMember" + this.id).hide();
                        Wtf.getCmp("setmod" + this.id).hide();
                        Wtf.getCmp("remmod" + this.id).hide();
                        Wtf.getCmp("chgperm" + this.id).hide();
                    }
                    this.loadingMask.hide();
                }
            }
        });
        var resSM = new Wtf.grid.CheckboxSelectionModel();
        var resCM = new Wtf.grid.ColumnModel([
            resSM,
            {
                header: WtfGlobal.getLocaleText('pm.project.resource.name'),
                autoWidth: true,
                dataIndex: "resourcename"
            },{
                hidden: true,
                autoWidth: true,
                dataIndex: "resourceid"
            },{
                hidden: true,
                autoWidth: true,
                dataIndex: "typename"
            },{
                header: WtfGlobal.getLocaleText('lang.category.text'),
                autoWidth: true,
                dataIndex: "categoryname"
            },{
                header: WtfGlobal.getLocaleText('lang.cost.text'),
                autoWidth: true,
                dataIndex: "stdrate",
                renderer: function(val, metadata, rec, ri, ci, store){
                    if(rec.data['typeid'] == Wtf.proj.resources.type.WORK)
                        val = val + ' '+Wtf.CurrencySymbol+'/hr';
                    else if(rec.data['typeid'] == Wtf.proj.resources.type.MATERIAL)
                        val = val + ' '+Wtf.CurrencySymbol+'/Unit';
                    else
                        val = Wtf.CurrencySymbol + ' ' + val;
                    return val;
                }
            },{
                header: WtfGlobal.getLocaleText('pm.common.units.text'),
                autoWidth: true,
                dataIndex: "wuvalue",
                renderer: function(val, metadata, rec, ri, ci, store){
                    if(rec.data['typeid'] == Wtf.proj.resources.type.WORK)
                        val = val + '%';
                    else if(rec.data['typeid'] == Wtf.proj.resources.type.MATERIAL)
                        val = val + ' Units';
                    else 
                        val = val + '%';
                    return val;
                }
            },{
                header: WtfGlobal.getLocaleText('pm.resource.billable'),
                autoWidth: true,
                renderer: this.renderBillableValue,
                dataIndex: "billable"
            },{
                header: WtfGlobal.getLocaleText('lang.colorcode.text'),
                width: 120,
                dataIndex: "colorcode",
                renderer:(!this.archived) ? this.selectColor : this.archiveProjSelectColor 
            },{
                header: WtfGlobal.getLocaleText('pm.project.teamnames'),
                autoWidth: true,
                dataIndex: 'teamname',
                hidden: false
            },{
                header: WtfGlobal.getLocaleText('lang.remove.text'),
                width: 60,
                hidden: (this.archived) ? true : false,
                fixed : true,
                groupable: false,
                dataIndex: '',
                id: 'deleteColRes',
                renderer: function(){
                    return "<img src = '../../images/Delete.gif' Wtf:qtip='"+WtfGlobal.getLocaleText('pm.project.settings.resource.drop.tip')+"' style='cursor: pointer;'>";
                }                
            }
            ]);
        this.resGrid = new Wtf.grid.GridPanel({
            store: this.grpStore,
            autoScroll:true,
            loadMask: this.loadingMask,
            autoHeight:true,
            layout:'fit',
            border:false,
            view: grpView,
            viewConfig:{
                forcefit:true
            },
            sm: resSM,
            cm: resCM
        });
        this.resGrid.on('cellclick', this.cellClickHandler, this);
        this.resGrid.on('rowdblclick', this.editResourceOnDoubleClick, this);
        if(!this.archived){
            resSM.on("rowselect", function(sm, row, rec){
                var selected = sm.getSelections();
                var temp = 0;
                var t2 = 0;
                for(var cnt = 0; cnt < selected.length; cnt++){
                    if(selected[cnt].data.billable)
                        temp++;
                    if(!selected[cnt].data.inuseflag)
                        t2++;
                }
                var ubBtn = Wtf.getCmp("unbillable" + this.id);
                var bBtn = Wtf.getCmp("billable" + this.id);
                var eBtn = Wtf.getCmp("editRes" + this.id);
                if(temp == selected.length){
                    ubBtn.enable();
                    bBtn.disable();
                } else if(temp == 0){
                    ubBtn.disable();
                    bBtn.enable();
                } else {
                    ubBtn.disable();
                    bBtn.disable();
                }
                if(t2 == selected.length){
                    Wtf.getCmp("actRes" + this.id).enable();
                    Wtf.getCmp("delRes" + this.id).disable();
                } else
                    Wtf.getCmp("actRes" + this.id).disable();
                if(t2 == 0)
                    Wtf.getCmp("delRes" + this.id).enable();
                else
                    Wtf.getCmp("delRes" + this.id).disable();
                if(selected.length == 1)
                    eBtn.enable();
                else
                    eBtn.disable();
            }, this);
            resSM.on("rowdeselect", function(sm, row, rec){
                var selRec = sm.getSelections();
                var temp = 0;
                var t2 = 0;
                for(var cnt = 0; cnt < selRec.length; cnt++){
                    if(selRec[cnt].data.billable)
                        temp++;
                    if(!selRec[cnt].data.inuseflag)
                        t2++;
                }
                var edBtn = Wtf.getCmp("editRes" + this.id);
                if(selRec.length == 0){
                    Wtf.getCmp("unbillable" + this.id).disable();
                    Wtf.getCmp("billable" + this.id).disable();
                    Wtf.getCmp("delRes" + this.id).disable();
                    Wtf.getCmp("actRes" + this.id).disable();
                } else {
                    if(t2 == selRec.length){
                        Wtf.getCmp("actRes" + this.id).enable();
                        Wtf.getCmp("delRes" + this.id).disable();
                    } else
                        Wtf.getCmp("actRes" + this.id).disable();
                    if(t2 == 0)
                        Wtf.getCmp("delRes" + this.id).enable();
                }
                if(selRec.length == 1)
                    edBtn.enable();
                else
                    edBtn.disable();
            }, this);
        }
        this.grpStore.on('load', this.myStoreChange, this);
        this.grpStore.on('datachanged',this.onStoreDataChanged,this);
    },
    renderBillableValue: function(value){
        if(value)
            return WtfGlobal.getLocaleText('lang.yes.text');
        else return WtfGlobal.getLocaleText('lang.no.text');
    },

    editResourceOnDoubleClick: function(grid, ri, e){
        if(!this.archived){
          var eBtn = Wtf.getCmp("editRes" + this.id);
          this.createNewRes(eBtn);
        }
    },

    createWeekGrid: function(){
        this.weekStore = new Wtf.data.Store({
            url: "../../admin.jsp",
            baseParams: {
                action: 0,
                mode: 14,
                projid: this.featureid
            },
            reader: new Wtf.data.JsonReader({
                root: 'data',
                fields:[{
                    name: 'day',
                    type: 'int'
                },{
                    name: 'intime'
                },{
                    name: 'outtime'
                },{
                    name: 'isholiday',
                    type: 'boolean'
                }]
            })
        });
        var colArr = [];
        colArr.push({
            header: WtfGlobal.getLocaleText('lang.day.text'),
            dataIndex: 'day',
            renderer: function(val){
                return Wtf.Week[val];
            }
        });
        var intime = new Wtf.form.TimeField({
            name:'intime',
            format: "H:i:s",
            editable: false,
            value: new Date().format('g:i A'),
            emptyText: WtfGlobal.getLocaleText('pm.project.health.intime'),
            width: 25
        });

        colArr.push({
            header : 'Intime',
            dataIndex : 'intime',
            editor : intime,
            width: 25
        });
        colArr.push({
            header: WtfGlobal.getLocaleText('pm.holiday.outtime'),
            dataIndex: 'outtime',
            width: 25
        });
        if(!this.archived) {
            var checkdays = new Wtf.grid.CheckColumn({
                header: WtfGlobal.getLocaleText('pm.calender.holiday.mark'),
                dataIndex: 'isholiday',
                width: 27,
                scope: this,
                sortable: false
            });
            colArr.push(checkdays);
        }
        var colM = new Wtf.grid.ColumnModel(colArr);
        var projWorkGrid = new Wtf.grid.EditorGridPanel({
            cm: colM,
            layout: 'fit',
            ds: this.weekStore,
            border: false,
            autoScroll:false,
            autoHeight:true,
            clicksToEdit : 1,
            viewConfig: {
                forceFit: true
            }
        });
        this.weekStore.on("load", function(obj, records){
            this.innerpanel.doLayout();
            for(var cnt=0; cnt<records.length; cnt++) {
                if(records[cnt].data["isholiday"]) {
                    projWorkGrid.getView().getRow(cnt).style.background = '#FF9F8F';
                }
            }
        }, this);
        this.weekStore.load();
        projWorkGrid.on("beforeedit", function(e){
            if(this.archived){
                e.cancel = true;
            }
        },this)
        projWorkGrid.on("afteredit", function(e){
            this.updateDay(e.record);
        },this)
        projWorkGrid.on("cellclick", function(grid, ri, ci, e){
            if(e.target.tagName == 'IMG'){
                var rec = this.weekStore.getAt(ri);
                var hdata = this.weekStore.data.items;
                var cnt = 0;
                var classname = e.target.className;
                if(classname.indexOf('x-grid3-check-col-on') == -1){
                    for(var i = 0; i < hdata.length; i++){
                        if(hdata[i].get('isholiday') == false)
                            cnt++;
                    }
                }
                if(cnt != 1){
                    var fieldName = grid.getColumnModel().getDataIndex(ci);
                    var ele = Wtf.get(e.target.id);
                    if(classname.indexOf('x-grid3-check-col-on') !== -1){
                        ele.replaceClass('x-grid3-check-col-on', 'x-grid3-check-col');
                        rec.data[fieldName] = false;
                    } else {
                        ele.replaceClass('x-grid3-check-col', 'x-grid3-check-col-on');
                        rec.data[fieldName] = true;
                    }
                    this.holidayChange = true;
                    this.updateDay(rec);
                    this.holidayChange = false;
                } else {
                    msgBoxShow(205, 0);
                    return;
                }
            }
        }, this)
        return projWorkGrid;
    },

    createHolidaysGrid: function(){
        var holidayRec = new Wtf.data.Record.create([
        {
            name: 'holiday',
            type: 'date',
            dateFormat: 'Y-m-d'
        },{
            name: 'description'
        }]);
        var holidayReader = new Wtf.data.KwlJsonReader({
            totalProperty: 'count',
            root: "data"
        },holidayRec);

        var holidayStore = new Wtf.data.Store({
            url: "../../admin.jsp",
            baseParams: {
                action: 0,
                mode: 15,
                projid: this.featureid
            },
            reader: holidayReader
        });
        var colM = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header: WtfGlobal.getLocaleText('lang.date.text'),
                dataIndex: 'holiday',
                renderer: function (value) {
                    return value ? value.dateFormat(WtfGlobal.getOnlyDateFormat()) : '';
                }
            },{
                header: WtfGlobal.getLocaleText('lang.description.text'),
                dataIndex: 'description'
        }]);
        var holidaysGrid = new Wtf.grid.GridPanel({
            cm: colM,
            layout: 'fit',
            ds: holidayStore,
            height: 172,
            border : false,
            autoScroll:true,
            viewConfig: {
                forceFit: true,
                emptyText: '<div class="emptyGridText">'+WtfGlobal.getLocaleText('pm.project.settings.calendar.holidays.emptytext')+'</div>'
            }
        });
        return holidaysGrid;
    },

//    setEditValue: function(obj, ri, r){
//        var rdata = r.data;
//        Wtf.getCmp(this.id + 'editWorkWeekPanel').disable();
//        Wtf.getCmp(this.id + 'intimeField').setValue(rdata.intime);
//        Wtf.getCmp(this.id + 'outtimeField').setValue(rdata.outtime);
//        Wtf.getCmp(this.id + 'holidayChkbox').setValue(rdata.isholiday);
//        Wtf.get(this.id + 'dayLabel').dom.innerHTML = Wtf.Week[rdata.day];
//        Wtf.get(this.id + 'dayid').dom.innerHTML = rdata.day;
//    },

    createCalendarPanel: function(){
        this.holidayChange = false;
        var weekGrid = this.createWeekGrid();
        var holidaysGrid = this.createHolidaysGrid();
        this.calendarPanel = new Wtf.Panel({
            layout: 'column',
            height: 220,
            items: [{
                columnWidth: 0.5,
                id: this.id + 'workweekPanel',
                title: WtfGlobal.getLocaleText('pm.calender.currentworkweek'),
                border : true,
                height: 220,
                items: [ weekGrid ]
            },{
                columnWidth: 0.5,
                title: WtfGlobal.getLocaleText('pm.calender.holidays'),
                height: 220,
                border: true,
                id: this.id + 'holidayPanel',
                items: [ holidaysGrid ]
            }]
        });
        /*        Wtf.getCmp(this.id + 'holidayChkbox').on("check", function(obj, val){
            if(val) {
                Wtf.getCmp(this.id + 'intimeField').disable();
                Wtf.getCmp(this.id + 'outtimeField').disable();
            } else {
                Wtf.getCmp(this.id + 'intimeField').enable();
                Wtf.getCmp(this.id + 'outtimeField').enable();
            }
        }, this);*/
        this.innerpanel.add(this.calendarPanel);
        this.MembergridPanel.hide();
        holidaysGrid.getStore().load();
        holidaysGrid.getStore().on("loadexception", function(){
            alert("loadexception");
        }, this);        
        this.disableBbar(false);
    },

    updateDay: function(rec){
        var value = rec.get('isholiday');
        var tempday = rec.get('day');
        var outTime = new Date(new Date().toDateString() + ' ' + rec.data.intime).add(Date.HOUR, 8).format('H:i:s');
        Wtf.Ajax.requestEx({
            url: '../../admin.jsp',
            params: {
                action: 1,
                mode: 2,
                emode: 3,
                day: tempday,
                dayLabel:  Wtf.Week[tempday],
                intime : rec.data.intime,
                outtime : outTime,
                isholiday : value ? "on" : "off",
                holidayChange: this.holidayChange,
                projid: this.featureid
            },
            method:'POST'
        },
        this,
        function(resp, req) {
            msgBoxShow(28, 0);
            this.weekStore.load();
            var obj = Wtf.urlDecode(req.params);
            if(obj.holidayChange == 'true' || obj.holidayChange)
                this.refreshPlan();
            bHasChanged=true;
            if(refreshDash.join().indexOf("all") == -1)
                refreshDash[refreshDash.length] = 'all';
        },
        function() {
            msgBoxShow(4, 1);
        });
    },

    swapPanel: function(){
        if(this.MembergridPanel.hidden) {
            this.MembergridPanel.show();
            if(this.resGrid !== undefined){
                this.swapGrid();
            }
            this.calendarPanel.hide();
            this.disableBbar(true);
        } else {
            this.MembergridPanel.hide();
            this.calendarPanel.show();
            this.disableBbar(false);
        }
    },

    disableBbar:function(flg){
        if(flg)
            this.innerpanel.bottomToolbar.show();
        else
            this.innerpanel.bottomToolbar.hide();
    },
    swapGrid: function() {
        //if(this.resGrid != undefined && this.resGrid.hidden){
        if( Wtf.getCmp("restogglebtn" + this.id).pressed){
            if(!this.archived)
               this.MembergridPanel.setTitle(WtfGlobal.getLocaleText('pm.project.settings.resources.helptext')+'<a href="#" onclick=\'createNewRes(\"'+this.id+'\")\'><img src="../../images/user_add.gif" style="margin-bottom:-4px; margin-left:5px;" wtf:qtitle="'+WtfGlobal.getLocaleText('pm.project.resources.create')+'" wtf:qtip="'+WtfGlobal.getLocaleText('pm.Help.createres')+'"/></a>');
            else
                this.MembergridPanel.setTitle(WtfGlobal.getLocaleText('pm.common.resources'));
            this.requestgrid.hide();
            this.quickSearchTF.setValue("");
            this.quickSearchTF.StorageChanged(this.grpStore);
            this.resGrid.show();
            this.pagingToolbar.bind(this.grpStore);
            this.grpStore.load({
                params: {
                    start: 0,
                    limit: this.pagingToolbar.pageSize
                }
            });
            if(!this.archived){
                Wtf.getCmp("accept" + this.id).hide();
                Wtf.getCmp("reject" + this.id).hide();
                Wtf.getCmp("newRes" + this.id).show();
                Wtf.getCmp("delRes" + this.id).show();
                Wtf.getCmp("actRes" + this.id).show();
                Wtf.getCmp("editRes" + this.id).show();
                Wtf.getCmp("manageResMenu" + this.id).show();
                Wtf.getCmp("unbillable" + this.id).show();
                Wtf.getCmp("billable" + this.id).show();
                Wtf.getCmp("invite" + this.id).hide();
                Wtf.getCmp("dropmem" + this.id).hide();
                Wtf.getCmp("activateMember" + this.id).hide();
                Wtf.getCmp("setmod" + this.id).hide();
                Wtf.getCmp("remmod" + this.id).hide();
                Wtf.getCmp("chgperm" + this.id).hide();
                Wtf.getCmp("editRes" + this.id).disable();
                Wtf.getCmp("unbillable" + this.id).disable();
                Wtf.getCmp("billable" + this.id).disable();
            }
        } else {
            this.resGrid.hide();
            this.requestgrid.show();
        }
    },

    activateResource: function(){
        var selected = this.resGrid.getSelectionModel().getSelections();
        var rid = "";
        for(var i = 0; i < selected.length; i++)
            rid += selected[i].data["resourceid"] + ",";
        rid = rid.substring(0, (rid.length - 1));
        Wtf.Ajax.requestEx({
            url: '../../jspfiles/project/resources.jsp',
            params: {
                resid: rid,
                action: 8,
                projid: this.featureid
            }
        }, this, function(response){
            if(response == "success") {
                msgBoxShow(294, 3);
                this.resGrid.getStore().reload();
                this.refreshPlan();
                bHasChanged=true;
                if(refreshDash.join().indexOf("all") == -1 && refreshDash.join().indexOf("qkl") == -1)
                    refreshDash[refreshDash.length] = 'qkl';
            }
        },
        function(response){
            msgBoxShow(4, 1);
        });

        Wtf.getCmp("editRes" + this.id).disable();
        Wtf.getCmp("delRes" + this.id).disable();
        Wtf.getCmp("actRes" + this.id).disable();
        Wtf.getCmp("unbillable" + this.id).disable();
        Wtf.getCmp("billable" + this.id).disable();
    },

    preDeleteResource: function(){
        var selected = this.resGrid.getSelectionModel().getSelections();
        var rid = "";
        for(var i = 0; i < selected.length; i++)
            rid += selected[i].data["resourceid"] + ",";
        rid = rid.substring(0, (rid.length - 1));
        Wtf.Ajax.requestEx({
            url: "../../jspfiles/project/resources.jsp",
            params: {
                projid: this.featureid,
                action: 13,
                resid: rid
            }
        }, this, function(response){
            var obj = eval("(" + response + ")");
            var str = "";
            var cnt = 0;
            var assignedResIds = [];
            for(var i = 0; i < obj.data.length; i++){
                str += obj.data[i].name + obj.data[i].msg + "<br>";
                if(obj.data[i].block)
                    cnt++;
                if(obj.data[i].assigned)
                    assignedResIds[assignedResIds.length] = obj.data[i].id;
            }
            var cButtons = Wtf.Msg.OK;
            if(obj.data.length != cnt){
                cButtons = Wtf.Msg.YESNO;
                if(assignedResIds.length == 0)
                    str += "<b>"+WtfGlobal.getLocaleText('pm.msg.355')+"</b>";
                else
                    str += "<b>"+WtfGlobal.getLocaleText('pm.msg.356')+"</b>";
            }
            Wtf.Msg.show({
                title:WtfGlobal.getLocaleText('pm.project.settings.resource.drop'),
                msg: str,
                cls: "msgboxClass",
                scope: this,
                buttons: cButtons,
                fn: function(act, b,c){
                    if(act == "yes"){
                        var rid = "";
                        for(var i = 0; i < obj.data.length; i++)
                            rid += obj.data[i].id + ",";
                        rid = rid.substring(0, (rid.length - 1));
                        if(assignedResIds.length == 0){
                            this.deleteResources(rid);
                        } else {
                            this.showReassignWindow('resource', assignedResIds.join());
                        }
                    }
                },
                icon: Wtf.MessageBox.QUESTION
            });
        },
        function(response) {
            msgBoxShow(4);
        });
    },

    deleteResources: function(rid){
        Wtf.Ajax.requestEx({
            url: "../../jspfiles/project/resources.jsp",
            params: {
                projid: this.featureid,
                action: 6,
                resid: rid
            }
        }, this, function(response){
            msgBoxShow(237, 3);
            this.resGrid.getStore().reload();
            this.refreshPlan();
            Wtf.getCmp("editRes" + this.id).disable();
            Wtf.getCmp("delRes" + this.id).disable();
            Wtf.getCmp("unbillable" + this.id).disable();
            Wtf.getCmp("billable" + this.id).disable();
            bHasChanged=true;
            if(refreshDash.join().indexOf("all") == -1 && refreshDash.join().indexOf("qkl") == -1)
                refreshDash[refreshDash.length] = 'qkl';
        },
        function(response) {
            msgBoxShow(4, 1);
            Wtf.getCmp("editRes" + this.id).disable();
            Wtf.getCmp("delRes" + this.id).disable();
            Wtf.getCmp("unbillable" + this.id).disable();
            Wtf.getCmp("billable" + this.id).disable();
        });
    },

    getFieldConfig: function(name){
        return newResForm.findById(name);
    },

    createNewRes: function(obj, event) {
        obj.disable();
        var newtyperecord = Wtf.data.Record.create([{
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
            },newtyperecord)
        });

        this.typeStore.load();
        var typeCombo = new Wtf.form.ComboBox({
            store: this.typeStore,
            mode: 'local',
            editable: true,
            forceSelection:true,
            typeAhead: true,
            triggerAction: 'all',
            fieldLabel: WtfGlobal.getLocaleText('pm.project.settings.resource.type')+"*",
            displayField: "typename",
            name: "typecombo",
            allowBlank: false,
            selectOnFocus: true,
            valueField: "typeid",
            validateOnBlur: false,
            validator: function(text){
                var store = typeCombo.store;
                var cnt = store.getCount();
                if(cnt == 0){
                    typeCombo.markInvalid();
                    return false;
                } else {
                    return true;
                }
            }

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

        this.categoryStore.load();
        var categoryCombo = new Wtf.form.ComboBox({
            store: this.categoryStore,
            mode: 'local',
            editable: true,
            forceSelection:true,
            typeAhead: true,
            triggerAction: 'all',
            fieldLabel: WtfGlobal.getLocaleText('pm.project.settings.resource.category')+"*",
            displayField: "categoryname",
            name: "categorycombo",
            allowBlank: false,
            selectOnFocus: true,
            valueField: "categoryid",
            validateOnBlur: false,
            validator: function(text){
                var store = categoryCombo.store;
                var cnt = store.getCount();
                if(cnt == 0){
                    categoryCombo.markInvalid();
                    return false;
                } else {
                    return true;
                }
            }
        });
        var str = WtfGlobal.getLocaleText('lang.create.text');
        var imgPath = "../../images/createuser40_52.gif";
        if(obj.text != WtfGlobal.getLocaleText('pm.project.resource.create')){
            str = WtfGlobal.getLocaleText('lang.edit.text');
            imgPath = "../../images/edituser40_52.gif";
            var rec = this.resGrid.getSelectionModel().getSelected();
            this.categoryStore.on('load', function(store) {
                categoryCombo.setValue(rec.data["categoryid"]);
                if(rec.data['categoryid'] == 1){
                    categoryCombo.disable();
                } else {
                    var idx = store.find('categoryname', 'Member', 0, false, true);
                    if(idx && idx != -1)
                        store.remove(store.getAt(idx));
                }
            }, this);
            this.typeStore.on('load', function(store) {
                typeCombo.setValue(rec.data["typeid"]);
                typeCombo.disable();
                if(rec.data['typeid'] != 1)
                    var idx = store.find('typename', 'Work', 0, false, true);
                    if(idx && idx != -1)
                        store.remove(store.getAt(idx));
            }, this);
        } else {
            this.categoryStore.on('load', function(store, recs, opts){
                var idx = store.find('categoryname', 'Member', 0, false, true);
                if(idx && idx != -1)
                    store.remove(store.getAt(idx));
            }, this);
            this.typeStore.on('load', function(store, recs, opts){
                var idx = store.find('typename', 'Work', 0, false, true);
                if(idx && idx != -1)
                    store.remove(store.getAt(idx));
            }, this);
        }
        var recColor = rec !== undefined ? rec.data.colorcode : "#666";
        typeCombo.on('select', function(combo, rec, index){
            if(newResForm){
                var costField = this.getFieldConfig('stdrate');
                var wuField = this.getFieldConfig('wuvalue');
                switch(rec.data['typeid']){
                    case Wtf.proj.resources.type.MATERIAL:
                        costField.setFieldLabel(WtfGlobal.getLocaleText('pm.project.settings.resource.costperunit')+'*');
                        wuField.setFieldLabel(WtfGlobal.getLocaleText('pm.project.settings.resource.noofunits')+'*');
                        break;
                    case Wtf.proj.resources.type.COST:
                        costField.setFieldLabel(WtfGlobal.getLocaleText('pm.project.settings.resource.cost')+'*');
                        wuField.setFieldLabel(WtfGlobal.getLocaleText('pm.project.settings.resource.weightage')+'*');
                        wuField.maxValue = 100;
                        break;
                }
            }
        }, this);
        var newRes = new Wtf.Window({
            title: obj.text,
            layout: "border",
            resizable: false,
            iconCls : 'iconwin',
            modal: true,
            height: 380,
            width: 480,
            items: [{
                region: "north",
                height : 75,
                border : false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                html: getTopHtml(obj.text, obj.text+"<br><span style=\"float:right; \">"+WtfGlobal.getLocaleText('pm.common.requiredfields')+"</span>", imgPath)
            },{
                region: "center",
                border: false,
                bodyStyle : 'background:#f1f1f1;font-size:10px;',
                layout: "fit",
                items:[ newResForm = new Wtf.form.FormPanel({
                    url: "../../jspfiles/project/resources.jsp",
                    labelWidth: 180,
                    defaults: {width: 225},
                    defaultType: "textfield",
                    bodyStyle: "padding:20px;",
                    items:[{
                        fieldLabel: WtfGlobal.getLocaleText('pm.project.resource.name')+"*",
                        disabled: rec ? true : false,
                        id: 'newResourceNameCreate',
                        value: rec !== undefined ? rec.data["resourcename"] : "",
                        name: "resourcename",
                        maxLength: 255,
                        allowBlank: false
                    }, 
                    typeCombo,
                    categoryCombo, 
                    {
                        xtype : 'panel',
                        id: "newCategoryLink",
                        border: false,
                        bodyStyle: "margin-left:125px;",
                        html: "<a href = '#' id = 'createCategoryLink'>"+WtfGlobal.getLocaleText('pm.project.resource.category')+"</a>"
                    },{
                        fieldLabel: WtfGlobal.getLocaleText('lang.cost.text'),
                        xtype:"numberfield",
                        allowBlank: false,
                        allowNegative: false,
                        minValue:0,
                        value: rec !== undefined ? rec.data["stdrate"] : 0,
                        selectOnFocus: true,
                        name: "stdrate",
                        id: "stdrate"
                    },{
                        fieldLabel: (rec) ? ((rec.data['typeid'] == Wtf.proj.resources.type.MATERIAL) ? WtfGlobal.getLocaleText('pm.project.settings.resource.noofunits') : WtfGlobal.getLocaleText('pm.project.settings.resource.weightage')) : WtfGlobal.getLocaleText('pm.project.settings.resource.weightageorunits'),
                        xtype:"numberfield",
                        allowBlank: false,
                        allowNegative: false,
                        allowDecimals: false,
                        minValue:0,
                        maxValue: (rec && (rec.data['typeid'] == Wtf.proj.resources.type.WORK || rec.data['typeid'] == Wtf.proj.resources.type.COST)) ? 100 : Number.MAX_VALUE,
                        value: rec !== undefined ? rec.data["wuvalue"] : 0,
                        selectOnFocus: true,
                        nanText: WtfGlobal.getLocaleText('pm.msg.69'),
                        name: "wuvalue",
                        id: "wuvalue"
                    },{
                        xtype : 'panel',
                        id: "colorCodeField",
                        border: false,
                        html: "<div style='width:200px; height:20px;'><div style='width:185px; height:16px; float:left'><span style='font-size:12px;'>"+WtfGlobal.getLocaleText('lang.colorcode.text')+"*</span></div><div id='colorCodeTemp'><div class='colorCode' style = 'background-color:" + recColor + ";' id='resColorDiv'></div></div></div>"
                    },{
                        xtype : 'hidden',
                        name: "resourceid",
                        value: rec !== undefined ? rec.data["resourceid"] : ""
                    },{
                        xtype : 'hidden',
                        name: "projectid",
                        value: this.featureid
                    },{
                        xtype : 'hidden',
                        id: "colorCodeHidden",
                        name: "colorcode",
                        value: recColor
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
                text: str,
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
                            action: str == WtfGlobal.getLocaleText('lang.create.text') ? 4 : 5,
                            projid: this.featureid
                        },
                        success: function(){
                            newRes.close();
                            var succMess = (str == WtfGlobal.getLocaleText('lang.create.text')) ? WtfGlobal.getLocaleText('pm.msg.357') : WtfGlobal.getLocaleText('pm.msg.358');
                            msgBoxShow([WtfGlobal.getLocaleText('lang.success.text'),succMess], 0);
                            this.resGrid.getStore().reload();
                            this.reloadPlanStore();
                            //                            if(this.resGrid.getSelectionModel().getSelections().length > 0)
                            if(obj.text == WtfGlobal.getLocaleText('pm.project.resource.create'))
                                obj.enable();
                            else
                                obj.disable();
                        },
                        failure: function(action, res){
                            if(obj.text == WtfGlobal.getLocaleText('pm.project.resource.create'))
                                obj.enable();
                            if(res.response.responseText){
                                var resobj = eval( "(" + res.response.responseText.trim() + ")" );
                                if(resobj.errcode == 1) {
                                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('pm.common.failure'), WtfGlobal.getLocaleText({key:'pm.msg.resource.active',params:resobj.error}), function(obj){
                                        if(obj == "yes"){
                                            Wtf.Ajax.requestEx({
                                                url: "../../jspfiles/project/resources.jsp",
                                                params: {
                                                    action: 8,
                                                    projid: this.featureid,
                                                    resid: resobj.resourceid
                                                },
                                                method : 'POST'
                                            },
                                            this,
                                            function(){
                                                this.resGrid.getStore().reload();
                                                this.reloadPlanStore();
                                            },
                                            function(){
                                                msgBoxShow(20, 1);
                                            });
                                        }
                                    }, this);
                                } else if(resobj.errcode == 2){
                                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('pm.common.failure'), WtfGlobal.getLocaleText({key:'pm.msg.resource.createanother',params:resobj.error}), function(obj){
                                        if(obj == "yes"){
                                            Wtf.Ajax.request({
                                                url: "../../jspfiles/project/resources.jsp",
                                                params: {
                                                    chkflag: false,
                                                    action: 4,
                                                    colorcode: resobj.colorcode,
                                                    overtimerate: resobj.overtimerate,
                                                    projectid: this.featureid,
                                                    resourcename: resobj.resourcename,
                                                    typeid: resobj.typeid,
                                                    categoryid: resobj.categoryid,
                                                    stdrate: resobj.stdrate
                                                },
                                                method : 'POST',
                                                scope : this,
                                                success:function(){
                                                    msgBoxShow(33, 0);
                                                    this.resGrid.getStore().reload();
                                                    this.reloadPlanStore();
                                                },
                                                failure :function(){
                                                    msgBoxShow(20, 1);
                                                }
                                            });
                                        }
                                    }, this);
                                }
                                newRes.close();
                            }
                        }
                    });
                    Wtf.getCmp("editRes" + this.id).disable();
                    Wtf.getCmp("delRes" + this.id).disable();
                    //                    Wtf.getCmp("resGrp" + this.id).disable();
                    Wtf.getCmp("unbillable" + this.id).disable();
                    Wtf.getCmp("billable" + this.id).disable();
                }
            },{
                text: WtfGlobal.getLocaleText('lang.cancel.text'),
                scope: this,
                handler: function(){
                    obj.enable();
                    newRes.close();
                }
            }]
        });
        Wtf.getCmp("newResourceNameCreate").on("change", function(){
            Wtf.getCmp("newResourceNameCreate").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("newResourceNameCreate").getValue()));
        }, this);

        newRes.on("close", function() {
            obj.enable();
        });

        newRes.show();
        //        Wtf.getCmp("groupBillable" + this.id).setValue( rec !== undefined ? rec.data["billable"] : false);
        //        if(! Wtf.getCmp("groupBillable" + this.id).getValue() )
        //            Wtf.getCmp("groupUnbillable" + this.id).setValue( rec !== undefined ? rec.data["billable"] : true);
        Wtf.get("colorCodeField").addListener("click", function(e){
            if(e.target.id == "resColorDiv"){
                this.showPalette(e);
            }
        }, this);
        Wtf.get("newCategoryLink").addListener("click", function(e){
            if(e.target.id == "createCategoryLink"){
                this.createCategory();
            }
        }, this);
        var newLink = Wtf.getCmp('newCategoryLink');
        newLink.setWidth(300);
        newLink.body.dom.style.marginLeft = '185px';
        newLink = Wtf.getCmp('colorCodeField');
        newLink.setWidth(300);
        focusOn("newResourceNameCreate");
    },

    resDel: function(){
        var selRes = this.resGrid.getSelectionModel().getSelections();
        if(selRes.length==0){
            return;
        }
        if(selRes[0].data["categoryname"]=="Member"){
            msgBoxShow(91, 1);
            return;
        }
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.320'), function(obj){
            if(obj == "yes") {
                var selResId = "";
                for(var cnt=0; cnt<selRes.length; cnt++){
                    selResId += selRes[cnt].data["resourceid"] + ",";
                }
                selResId = selResId.substring(0, (selResId.length - 1));
                Wtf.Ajax.requestEx({
                    url: "../../jspfiles/project/resources.jsp",
                    params: {
                        action: 6,
                        projid: this.featureid,
                        resid: selResId
                    },
                    method :'POST'
                },
                this,
                function(){
                    this.resGrid.getStore().reload();
                    this.reloadPlanStore();
                    bHasChanged=true;
                    if(refreshDash.join().indexOf("all") == -1 && refreshDash.join().indexOf("qkl") == -1)
                        refreshDash[refreshDash.length] = 'qkl';
                },
                function(){
                    msgBoxShow(34, 1);
                });
            }
        }, this);
    },
    reloadPlanStore: function(){
        var projPlanobj = Wtf.getCmp(this.featureid + "_projplanPane");
        if(projPlanobj !== undefined && projPlanobj.items !== undefined && projPlanobj.items.items[0].editGrid !== undefined){
            projPlanobj.items.items[0].editGrid.allResources.reload();
        }
    },
    refreshPlan: function(){
        var projPlanobj = Wtf.getCmp(this.featureid + "_projplanPane");
        if(projPlanobj !== undefined && projPlanobj.items !== undefined && projPlanobj.items.items[0].editGrid !== undefined){
            projPlanobj = Wtf.getCmp(this.featureid + "projPlanCont");
            projPlanobj.refreshOnActivate = true;
            projPlanobj.editGrid.allResources.reload();
        }
    },
    createCategory: function(){
        var typeField;
        var typeForm = new Wtf.form.FormPanel({
            url: "../../jspfiles/project/resources.jsp",
            id: 'newTypeResource',
            bodyStyle: "margin:10px 15px 0 15px;",
            border: false,
            items: [typeField = new Wtf.form.TextField({
                fieldLabel: WtfGlobal.getLocaleText('lang.category.name'),
                name: "categoryname"
            })]
        });
        var newTypeWin = new Wtf.Window({
            title: WtfGlobal.getLocaleText('pm.project.resource.category'),
            height: 120,
            modal: true,
            resizable: false,
            iconCls:'iconwin',
            bodyStyle: "background-color:#f1f1f1;",
            width: 350,
            buttons:[{
                text: WtfGlobal.getLocaleText('lang.create.text'),
                scope: this,
                handler: function(){
                    typeField.setValue(WtfGlobal.HTMLStripper(typeField.getValue()));
                    if(typeField.getValue() == ""){
                        msgBoxShow(54, 1);
                        return false;
                    }
                    this.createNewResCategory(typeForm, newTypeWin);
                }
            },{
                text: WtfGlobal.getLocaleText('lang.cancel.text'),
                handler: function(){
                    newTypeWin.close();
                }
            }],
            items: [typeForm]
        });
        newTypeWin.show();
    },
    createNewResCategory: function(formPanel, winObj){
        formPanel.form.submit({
            params: {
                action: 7
            },
            scope: this,
            success: function(action, response){
                winObj.close();
                this.categoryStore.reload();
            },
            failure: function(){
                winObj.close();
            }
        });
    },
    confirmAcceptRejectRequest : function(el, e){
        if(!(el.text == WtfGlobal.getLocaleText('lang.invite.text'))) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.330'), function(obj){
                if(obj == "yes"){
                    this.bottombarButtonClicked(el, e);
                }
            }, this);
        } else this.bottombarButtonClicked(el, e);

    },

    createPopInviteWindow: function(){
        this.popds.load({
            params: {
                start: 0,
                limit: 100
            }
        });
        this.popInvite = new Wtf.Window({
            id: 'PopInvite' + this.id,
            modal:true,
            iconCls:'iconwin',
            title:WtfGlobal.getLocaleText('pm.common.invitemember'),
            layout:'fit',
            height:500,
            width:500,
            resizable:false,
            closeAction: 'close',
            items:[this.poppanel = new Wtf.Panel({
                id: 'poppanel' + this.id,
                layout: 'fit',
                cls: 'backcolor',
                border: false,
                tbar: [/*'Quick Search: ', */this.popqs = new Wtf.KWLQuickSearch({
                    width: 200,
                    field: "Name",
                    emptyText: WtfGlobal.getLocaleText('pm.common.searchbyname')
                })],
                bbar: new Wtf.PagingToolbar({
                    pageSize: 100,
                    id: "poppagetbar" + this.id,
                    store: this.popds,
                    /*plugins: new Wtf.common.pPageSize({
                                id: "popPageSize_" + this.id
                            }),*/
                    items: ['->',
                    {
                        text: WtfGlobal.getLocaleText('pm.project.member.sendinvite'),
                        id: "sendinv" + this.id,
                        allowDomMove: false,
                        scope: this,
                        tooltip: WtfGlobal.getLocaleText('pm.common.invitemember'),
                        handler: function(el, e){
                            if(!(this.popsm.hasSelection())){
                                msgBoxShow(135, 1);
                            /*Wtf.MessageBox.show({
                                                title: WtfGlobal.getLocaleText('pm.common.warning.text'),
                                                msg: "Please select the user(s) you wish to invite?",
                                                buttons: Wtf.Msg.OK,
                                                scope: this,
                                                icon: Wtf.MessageBox.QUESTION
                                            });*/
                            } else
                                this.bottombarButtonClicked(el, e);
                        }
                    },{
                        text: WtfGlobal.getLocaleText('lang.close.text'),
                        allowDomMove: false,
                        scope: this,
                        handler: function(){
                            this.popInvite.close();
                        }
                    }
                    ]
                }),
                items: [this.popgrid = new Wtf.grid.GridPanel({
                    id: 'invitegrid' + this.id,
                    store: this.popds,
                    cm: this.popcm,
                    sm: this.popsm,
                    border: false,
                    viewConfig: {
                        forceFit: true
                    }
                })]
            })]
        });
    },

    bottombarButtonClicked: function(el, e){
        var acceptrecords = this.selectionModel.getSelections();
        var action=0;
        var mod=0, emode;
        var commid=this.id.substr(6);
        var chkDependency = 0;
        if(el.text == WtfGlobal.getLocaleText('lang.invite.text')) {
            this.createPopInviteWindow();
        //            this.popqs.StorageChanged(this.popds);
        //            this.popqs.on('SearchComplete', function() { this.popgrid.getView().refresh();}, this);
        //            this.popInvite.show();
        //            this.popgrid.getView().refresh();
        } else {
            var planperm = 0;
            if(this.mode == 6) {
                action = 0;
            }else if(this.mode == 7) {
                action = 1;
            }
            switch(el.text) {
                case WtfGlobal.getLocaleText('lang.accept.text'):
                    emode = 0;
                    break;
                case WtfGlobal.getLocaleText('lang.reject.text'):
                    emode = 1;
                    break;
                case WtfGlobal.getLocaleText('pm.project.member.sendinvite'):
                    emode = 4;
                    this.popInvite.close();
                    break;
                case WtfGlobal.getLocaleText('pm.project.member.dropmember'):
                    /*                    var index = this.memds.query('status', 4);
                    var indexLength = index.length;
                    var length = acceptrecords.length;
                    if(indexLength <= length) {
                        var cnt = 0;
                        for(var i = 0; i < indexLength; i++) {
                            for(var j = 0; j < length; j++) {
                                if(index.items[i].data.id == acceptrecords[j].data.id) {
                                    cnt++;
                                }
                            }
                        }
                        if(cnt == indexLength) {
                            msgBoxShow(35, 1);
                            return;
                        }
                    }*/
                    emode = 2;
                    var flag = 1;
                    chkDependency = 1;
                    break;
                case WtfGlobal.getLocaleText('pm.admin.project.setactive'):
                    var index = this.memds.query('status', 4);
                    var indexLength = index.length;
                    var length = acceptrecords.length;
                    if(indexLength <= length) {
                        var cnt = 0;
                        for(var i = 0; i < indexLength; i++) {
                            for(var j = 0; j < length; j++) {
                                if(index.items[i].data.id == acceptrecords[j].data.id) {
                                    cnt++;
                                }
                            }
                        }
                    }
                    emode = 6;
                    break;
                case WtfGlobal.getLocaleText('pm.project.member.setmoderator'):
                    emode = 3;
                    break;
                case WtfGlobal.getLocaleText('pm.project.member.removemoderator'):
                    var index = this.memds.query('status', 4);
                    //                    if(index.length == 1) {
                    //                        msgBoxShow(35, 1);
                    //                        return;
                    //                    }
                    emode = 5;
                    break;
            }
            var rec = "";
            if(emode == 4) {
                acceptrecords = this.popsm.getSelections();
            }
            var isitme = false;
            for(var i = 0; i < acceptrecords.length; i++) {
                var _id  = acceptrecords[i].get("id");
                if(_id == loginid)
                    isitme = true;
                if(i == acceptrecords.length - 1) {
                    rec += _id;
                } else {
                    rec += _id + ",";
                }
            }
            Wtf.Ajax.requestEx({
                url: "../../AdminHandler.jsp",
                method: 'POST',
                params: {
                    action: action,
                    commid: commid,
                    emode: emode,
                    flagDrop : flag,
                    chkFlag: chkDependency,
                    uids: rec,
                    projid:this.featureid
                }
            },
            this,
            function(frm, response) {
                var retstatus = eval('('+frm+')');
                if(retstatus.error !== undefined){
                    Wtf.Msg.show({
                        title:WtfGlobal.getLocaleText('pm.project.member.dropmember'),
                        msg: retstatus.error,
                        scope: this,
                        buttons: retstatus.errorcode == 1 ? Wtf.Msg.OK : Wtf.Msg.YESNO,
                        fn: function(obj){
                            if(obj == 'yes'){
                                this.showReassignWindow('member', rec, action, emode, commid, flag);
                            } else if(obj == 'no'){
                                this.dropMember(action, emode, commid, flag, rec);
                            }
                        },
                        icon: Wtf.MessageBox.QUESTION
                    });
                    this.requestgrid.getView().refresh();
                } else if(retstatus.data[0].value==null){
                    if(isitme !== true){
                        var _store = this.requestgrid.getStore();
                        _store.reload({
                            params: {
                                start: 0,
                                limit: this.pagingToolbar.pageSize
                            }
                        });
                        _store.on('load',function() {
                            this.requestgrid.getView().refresh();
                        }, this);
                   }else{
                        this.dropMember(action, emode, commid, flag, rec);
                   }
                 } else {
                    msgBoxShow(35, 1);
                    isitme = false;
                    this.requestgrid.getView().refresh();
                }
                if(emode == 0 || emode == 1 || emode == 2 || emode == 6){
                    bHasChanged=true;
                    if(refreshDash.join().indexOf("all") == -1 && refreshDash.join().indexOf("qkl") == -1)
                        refreshDash[refreshDash.length] = 'qkl';
                } else if(emode == 5 && isitme == true){
                    bHasChanged=true;
                    if(refreshDash.join().indexOf("all") == -1)
                        refreshDash[refreshDash.length] = 'all';
                    var tab = Wtf.getCmp('as');
                    var tabs = [tab.getActiveTab(), "tabproject"];
                    removeTabsAndRefreshDashoard(tabs, true);
                }
            },
            function(){
                bHasChanged=false;
            });
        }
        this.disableButtons();
    },
    dropMember: function(action, emode, commid, flag, rec){
        Wtf.Ajax.requestEx({
            url: "../../AdminHandler.jsp",
            method: 'POST',
            params: {
                action: action,
                commid: commid,
                emode: emode,
                flagDrop : flag,
                chkFlag: 0,
                uids: rec,
                projid: commid
            }},this,
        function(frm, response) {
            var retstatus = eval('('+frm+')');
            if(retstatus.data[0].value==null){
                var _store = this.requestgrid.getStore();
                _store.reload();
                _store.on('load',function() {
                    this.requestgrid.getView().refresh();
                }, this);
                this.refreshPlan();
                bHasChanged=true;
                if(refreshDash.join().indexOf("all") == -1 && refreshDash.join().indexOf("qkl") == -1)
                    refreshDash[refreshDash.length] = 'qkl';
            } else {
                msgBoxShow(35, 1);
                this.requestgrid.getView().refresh();
            } 
        },
        function(){
            msgBoxShow(35, 1);
            this.requestgrid.getView().refresh();
        });
    },

    showReassignWindow: function(type, resourceString, action, emode, commid, flag){
        var reassignWin = new Wtf.reassignWin({
            type: type,
            projectid: this.featureid,
            resourceid: resourceString,
            id: 'reassignWin'
        }).show();
        reassignWin = Wtf.getCmp('reassignWin');
        reassignWin.on('deleteResource', function(){
            if(type == 'member')
                this.dropMember(action, emode, commid, flag, resourceString);
            else
                this.deleteResources(resourceString);
            return true;
        }, this);
        reassignWin.on('reassignCancelled', function(win, btn){
            win.close();
            this.refreshPlan();
        }, this);
    },

    enableButtons: function(sm, ri, rec) {
        var selrec = sm.getSelections();
        if(selrec.length == 0){
            this.disableButtons();
        } else {
            var setmod = 0;
            var remmod = 0;
            var drop = 0;
            var act = 0;
            var viewperm = editperm = 0;
            for(var cnt = 0; cnt < selrec.length; cnt++) {
                var _r = selrec[cnt].data;
                if(_r.status == 3)
                    setmod++;
                else if(_r.status == 4)
                    remmod++;
                if(_r.inuse == "Active")
                    drop++;
                else
                    act++;
                if(_r.planpermission == 1)
                    viewperm++;
                else
                    editperm++;
            }
            var smBtn = Wtf.getCmp("setmod" + this.id);
            var rmBtn = Wtf.getCmp("remmod" + this.id);
            if(setmod == selrec.length){
                smBtn.enable();
                rmBtn.disable();
            } else if(remmod == selrec.length) {
                smBtn.disable();
                rmBtn.enable();
            } else {
                smBtn.disable();
                rmBtn.disable();
            }
            if(drop == selrec.length){
                Wtf.getCmp("dropmem" + this.id).enable();
                Wtf.getCmp("activateMember" + this.id).disable();
            } else if(act == selrec.length){
                Wtf.getCmp("activateMember" + this.id).enable();
                Wtf.getCmp("setmod" + this.id).disable();
                Wtf.getCmp("remmod" + this.id).disable();
                Wtf.getCmp("dropmem" + this.id).disable();
            } else {
                Wtf.getCmp("activateMember" + this.id).disable();
                Wtf.getCmp("dropmem" + this.id).disable();
            }
            if(viewperm==selrec.length || editperm==selrec.length)
                Wtf.getCmp("chgperm" + this.id).enable();
            else
                Wtf.getCmp("chgperm" + this.id).disable();
            Wtf.getCmp("accept" + this.id).enable();
            Wtf.getCmp("reject" + this.id).enable();
        }
    },

    disableButtons: function(sm, ri, rec) {
        Wtf.getCmp("accept" + this.id).disable();
        Wtf.getCmp("reject" + this.id).disable();
        Wtf.getCmp("dropmem" + this.id).disable();
        Wtf.getCmp("activateMember" + this.id).disable();
        Wtf.getCmp("setmod" + this.id).disable();
        Wtf.getCmp("remmod" + this.id).disable();
        Wtf.getCmp("chgperm" + this.id).disable();
    },

    cellClickHandler: function(gd, ri, ci, obj) {
        if(!this.archived){
            if(obj.target.id == "img_div") {
                this.showPalette(obj, ri, obj.target.parentNode, gd.getStore());
            }
            if(obj.target.tagName == "img" || obj.target.tagName == "IMG") {
                if( Wtf.getCmp("restogglebtn" + this.id).pressed){
                    this.preDeleteResource();
                }
                if( Wtf.getCmp("memtogglebtn" + this.id).pressed){
                    var el = Wtf.getCmp("dropmem" + this.id);
                    this.bottombarButtonClicked(el);
                }
            }
        }
    },

    showPalette: function(e, ri, obj, ds) {
        var eventargs = e.target;
        var colorPicker = new Wtf.menu.ColorItem({
            id: this.id + 'coloritem'
        });

        var contextMenu = new Wtf.menu.Menu({
            id: this.id + 'contextMenu',
            items: [ colorPicker ]
        });
        contextMenu.showAt(e.getXY());
        if(ds !== undefined) {
            colorPicker.on('select', function(palette, selColor){
                var colorCode = '#' + selColor;
                //var index = ds.find('color', colorCode);
                //if(index == -1 && selColor != 'FF0000' && selColor != '000000' && selColor != '0000FF') {
                obj.innerHTML = this.getcolor(colorCode);
                var _rec = ds.getAt(ri).data;
                _rec.colorcode = colorCode;
                Wtf.Ajax.requestEx({
                    url: Wtf.req.prj + 'getTaskResources.jsp',
                    params: {
                        action : "putcolor",
                        data: colorCode,
                        projectid: this.featureid,
                        userid : _rec.resourceid
                    }
                }, this, function(){ this.reloadPlanStore(); }, function(){});
            /*} else {
                        msgBoxShow(36, 1);
                    }*/
            },this);
        } else {
            colorPicker.on('select', function(palette, selColor){
                var cc= '#' + selColor;
                Wtf.getCmp("colorCodeHidden").setValue(cc);
                this.eventargs.parentNode.innerHTML = "<div class='colorCode' style=\"background-color:"+cc+";\" id='resColorDiv'></div>";
            },{
                scope: this,
                eventargs: eventargs
            });
        }
    },

    getcolor:function(value){
        return '<div id = "img_div" style="cursor: pointer; height: 12px; width: 12px; margin: auto; padding: auto; border-color: '+value+'; background-color: '+value+';"></div>';
    },

    selectColor: function(value) {
          return '<div id = "img_div" style="cursor: pointer; height: 12px; width: 12px; padding: auto; border-color: '+value+'; background-color: '+value+';"></div>';
    },

    archiveProjSelectColor: function(value) {
        return '<div id = "img_div" style="height: 12px; width: 12px; padding: auto; border-color: '+value+'; background-color: '+value+';"></div>';
    },

    QuickSearchComplete: function(e){
        view = this.requestgrid.getView();
        view.refresh();
    },

    updateRadios: function(no, subscr){
        var r3 = Wtf.getCmp(this.id+'_3');
        var r4 = Wtf.getCmp(this.id+'_4');
        var f = Wtf.getCmp('fieldset_assgn').el.dom.firstChild.firstChild;
        if(no == 2){
            r3.setValue(0);r3.setDisabled(true);
            r4.setValue(0);r4.setDisabled(true);
            if(subscr){
                f.checked = false;
                f.disabled = true;
            }
        } else {
            r3.setDisabled(false);
            r4.setDisabled(false);
            if(subscr){
                f.checked = true;
                f.disabled = false;
            }
        }
    },

    setPermDetails: function(no){
        var txt = "", r3 = Wtf.getCmp(this.id+'_3'), r4 = Wtf.getCmp(this.id+'_4');
        var subscr = Wtf.subscription.proj.subModule.usrtk;
        switch(no){
            case 1:
                if(r3 && r3.getValue())
                    txt = WtfGlobal.getLocaleText('pm.Help.viewallviewassgn');
                else if(r4 && r4.getValue())
                    txt = WtfGlobal.getLocaleText('pm.Help.viewallmodifyassgn');
                else
                    txt = WtfGlobal.getLocaleText('pm.Help.viewall');
                if(subscr)
                    this.updateRadios(no, subscr);
                Wtf.get('permDesc').dom.innerHTML = txt;
                break;
            case 2:
                txt = WtfGlobal.getLocaleText('pm.Help.modifyall');
                Wtf.get('permDesc').dom.innerHTML = txt;
                if(subscr)
                    this.updateRadios(no, subscr);
                break;
            case 3:
                if(Wtf.getCmp(this.id+'_1').getValue())
                    txt = WtfGlobal.getLocaleText('pm.Help.viewall');
                else
                    txt = WtfGlobal.getLocaleText('pm.Help.viewassgn');
                Wtf.get('permDesc').dom.innerHTML = txt;
                break;
            case 4:
                if(Wtf.getCmp(this.id+'_1').getValue())
                    txt = WtfGlobal.getLocaleText('pm.Help.viewallmodifyassgn');
                else
                    txt = WtfGlobal.getLocaleText('pm.Help.modifyassgn');
                Wtf.get('permDesc').dom.innerHTML = txt;
                break;
        }
    },

    changePermission : function() {
        var emode = 3;
        var action = 2;
        var acceptrecords = this.selectionModel.getSelections();
        var planperm = acceptrecords[0].data.planpermission;
        var rec = "";
        var commid = this.id.substr(6);
        var isitme = false;
        var subscr = Wtf.subscription.proj.subModule.usrtk;

        for(var i = 0; i < acceptrecords.length; i++) {
            var _id  = acceptrecords[i].get("id");
            if(_id == loginid)
                isitme = true;
            if(acceptrecords[i].get("status") != 4) {
                if(i == acceptrecords.length - 1) {
                    rec += _id;
                } else {
                    rec += _id + ",";
                }
            } else {
                msgBoxShow(301, 1);
                return;
            }
        }

        this.chPermWin = Wtf.getCmp(this.id + "changePermWin");
        if(!this.chPermWin) {
            var text1 = '<div style="color:#15428B;font-size:12px;margin-bottom:20px;">'+WtfGlobal.getLocaleText('pm.project.permission.info1')+'</div>';
            var text2 = '<div style="color:#15428B;font-size:12px;margin-bottom:20px;">'+WtfGlobal.getLocaleText('pm.project.permission.info2')+'</div>'
            var text3 = '<div style="color:#15428B;font-size:12px;margin-bottom:20px;">'+WtfGlobal.getLocaleText('pm.project.permission.info3')+'</div>'
            this.chPermWin = new Wtf.Window({
                iconCls : "iconwin",
                closable : true,
                modal : true,
                title : WtfGlobal.getLocaleText('pm.project.permission.text'),
                id : this.id + "changePermWin",
                autoHeight : true,
                width : 520,
                buttonAlign : "right",
                resizable: false,
                items : [{
                    region : "north",
                    height : 75,
                    bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                    html : getTopHtml(WtfGlobal.getLocaleText('pm.project.permission.text'), WtfGlobal.getLocaleText('pm.project.permission.topbartext'),"../../images/userpermission40_52.gif")
                }, {
                    region : "center",
                    border : false,
                    id : 'changePerm',
                    bodyStyle : 'background:#f1f1f1; font-size:10px; padding:15px;',
                    layout: 'fit',
                    items : [{
                        border: false,
                        layout: 'fit',
                        autoHeight: true,
                        html: (subscr) ? text1 : (creator) ? text3 : text2
                    }, {
                        layout : "column",
                        border : false,
                        autoHeight: true,
                        id:'fieldsetArea',
                        items : [{
                            xtype : "fieldset",
                            columnWidth : (subscr) ? 0.48 : 0.97,
                            title : WtfGlobal.getLocaleText('pm.common.allTask'),
                            checkboxToggle: true,
                            checkboxName: 'perm',
                            cls: 'permWinFieldset',
                            id: 'fieldset_all',
                            bodyStyle : "font-size:12px;",
                            autoHeight : true,
                            collapsed: false,
                            layout: 'form',
                            items : [{
                                border: false,
                                autoWidth: true,
                                style:'margin:5px;padding-left:15px;',
                                defaults:{
                                    hideLable: true
                                },
                                items : [{
                                    xtype : "radio",
                                    boxLabel : WtfGlobal.getLocaleText('lang.view.text'),
                                    id : this.id + '_1',
                                    name : "1radio",
                                    height: 18,
                                    checked : (planperm == 2 || planperm == 10 || planperm == 18) ? true : false
                                },{
                                    xtype : "radio",
                                    boxLabel : WtfGlobal.getLocaleText('lang.modify.text'),
                                    id : this.id + '_2',
                                    name : "1radio",
                                    checked : (planperm == 4) ? true : false
                                }]
                            }]
                        }]
                    }]
                },{
                    border: false,
                    height: 85,
                    id: 'permDetails',
                    bodyStyle: 'background:white;font-size:12px;border:2px solid #bfbfbf;',
                    html: '<div style="clear:both;margin:2px 5px;">'+WtfGlobal.getLocaleText('lang.description.text')+' - </div><div id="permDesc" class="permDesc"></div>'
                }],
                buttons : [{
                    text : WtfGlobal.getLocaleText('pm.project.permission.text'),
                    scope : this,
                    handler : function() {
                        var id = this.id+'_'
                        var c1, c2, c3, c4, val = 0;
                        c1 = Wtf.getCmp(id+'1').getValue();
                        c2 = Wtf.getCmp(id+'2').getValue();
                        if(subscr){
                            c3 = Wtf.getCmp(id+'3').getValue();
                            c4 = Wtf.getCmp(id+'4').getValue();
                        }
                        if(c1)
                            val = Math.pow(2, 1);
                        if(c3)
                            val += Math.pow(2, 3);
                        if(c4)
                            val += Math.pow(2, 4);
                        if(c2)
                            val = Math.pow(2, 2);
                        planperm = val;
                        if(planperm !== 0){
                            Wtf.Ajax.requestEx({
                                url : "../../AdminHandler.jsp",
                                params : {
                                    action : action,
                                    emode : emode,
                                    uids : rec,
                                    commid : commid,
                                    planpermission : planperm,
                                    projid : this.featureid
                                },
                                method : "POST"
                            },
                            this,
                            function(result, req) {
                                var resobj = eval("(" + result.trim() + ")");
                                if(resobj.success) {
                                    bHasChanged=true;
                                    if(refreshDash.join().indexOf("all") == -1)
                                        refreshDash[refreshDash.length] = 'all';
                                    if(isitme){
                                        Wtf.getCmp('as').remove(Wtf.getCmp('as').getActiveTab());
                                    } else {
                                        var _store = this.requestgrid.getStore();
                                        _store.reload({
                                            params: {
                                                start: 0,
                                                limit: this.pagingToolbar.pageSize
                                            }
                                        });

                                        _store.on('load', function() {
                                            this.requestgrid.getView().refresh();
                                        }, this);
                                    }
                                    this.chPermWin.close();
                                    this.disableButtons();
                                }
                            },
                            function(result, req) {
                                this.chPermWin.hide();
                                msgBoxShow(4, 1);
                            });
                        } else {
                            msgBoxShow(270, 1);
                        }
                    }
                }, {
                    text : WtfGlobal.getLocaleText('lang.cancel.text'),
                    scope : this,
                    handler : function() {
                        this.chPermWin.close();
                    }
                }]
            });
            if(subscr){
                Wtf.getCmp('fieldsetArea').add({
                    xtype : "fieldset",
                    cls: 'permWinFieldset',
                    columnWidth : 0.48,
                    title : WtfGlobal.getLocaleText('pm.common.assignedtasks'),
                    id: 'fieldset_assgn',
                    checkboxToggle: true,
                    checkboxName: 'perm',
                    bodyStyle : "font-size:12px;",
                    autoHeight : true,
                    layout: 'form',
                    items : [{
                        border: false,
                        autoWidth: true,
                        style:'margin:5px;padding-left:15px;',
                        defaults:{
                            hideLable: true
                        },
                        items : [{
                            xtype : "radio",
                            boxLabel : WtfGlobal.getLocaleText('lang.view.text'),
                            id : this.id + '_3',
                            name : "2radio",
                            height: 18,
                            checked : (planperm == 8 || planperm == 10) ? true : false
                        },{
                            xtype : "radio",
                            boxLabel : WtfGlobal.getLocaleText('lang.modify.progressonly'),
                            id : this.id + '_4',
                            name : "2radio",
                            checked : (planperm == 18 || planperm == 16) ? true : false
                        }]
                    }]
                });
            }
        }
        this.chPermWin.show();
        for(var c = 1; c < 5; c++){
            var rad = Wtf.getCmp(this.id+'_'+c);
            if(rad){
                if(Wtf.isWebKit)
                    rad.on("check", this.showDesc, this);
                else
                    rad.on("focus", this.showDesc, this);
                if(rad.getValue()){
                    if(Wtf.isWebKit)
                        rad.fireEvent("check", rad);
                    else
                        rad.fireEvent("focus", rad);
                }
            }
        }
        Wtf.get(Wtf.getCmp('fieldset_all').el.dom.firstChild.firstChild.id).addListener('click', function(){
            var r1 = Wtf.getCmp(this.id+'_1');
            var r2 = Wtf.getCmp(this.id+'_2');
            if(r1.disabled){
                r1.setDisabled(false);
                r2.setDisabled(false);
            } else {
                r1.setDisabled(true);
                r2.setDisabled(true);
            }
            r1.setValue(0);
            r2.setValue(0);
            if(subscr){
                var f = Wtf.getCmp('fieldset_assgn').el.dom.firstChild.firstChild;
                f.disabled = false;
                if(!f.checked)
                    Wtf.get('permDesc').dom.innerHTML = '';
            }
        }, this);
        if(subscr){
            Wtf.get(Wtf.getCmp('fieldset_assgn').el.dom.firstChild.firstChild.id).addListener('click', function(){
                var r1 = Wtf.getCmp(this.id+'_3');
                var r2 = Wtf.getCmp(this.id+'_4');
                if(r1.disabled){
                    if(!Wtf.getCmp(this.id+'_2').getValue()){
                        r1.setDisabled(false);
                        r2.setDisabled(false);
                    }
                } else {
                    r1.setDisabled(true);
                    r2.setDisabled(true);
                }
                r1.setValue(0);
                r2.setValue(0);
                var f = Wtf.getCmp('fieldset_assgn').el.dom.firstChild.firstChild;
                if(!f.checked)
                    Wtf.get('permDesc').dom.innerHTML = '';
            }, this);
            Wtf.getCmp('fieldset_assgn').on('beforecollapse', function(p){
                return false;
            }, this);
        }

        Wtf.getCmp('fieldset_all').on('beforecollapse', function(p){
            return false;
        }, this);

    },

    showDesc: function(radio){
        var id = radio.id;
        var no = parseInt(id.split('_')[2]);
        no = Wtf.num(no, -1);
        if(no)
            this.setPermDetails(no);
    }
});

function getInvitations(id){
    Wtf.getCmp(id).createPopInviteWindow();
}

function createNewRes(id){
   if(!this.archived){
     var btn = Wtf.getCmp("newRes" + id);
     Wtf.getCmp(id).createNewRes(btn);
   }
}
