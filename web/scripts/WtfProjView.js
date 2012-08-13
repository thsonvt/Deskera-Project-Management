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
Wtf.ProjectTabs = function(config){
    Wtf.ProjectTabs.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.ProjectTabs, Wtf.Panel, {
    onRender: function(config){
        Wtf.ProjectTabs.superclass.onRender.call(this, config);
        this.pro = new Wtf.ProfileView({
            id: "subtabproj" + this.guid,
            uid: this.uid,
            layout: 'fit',
            profiletitle: this.comtitle,
            isource: this.tabprojimage,
            border: false,
            member: this.member,
            tabType: Wtf.etype.home,
            iconCls: getTabIconCls(Wtf.etype.home)
        });
         Wtf.Ajax.requestEx({
            url: Wtf.req.prj + 'getProjectMembership.jsp?userid1=' + loginid + '&userid2=' + this.guid,
            params : {
            },
            method: 'GET'},
            this,
            function(result, request) {
               this.conveyRelation(result, request);
            },
            function(result, request) {
        });
//        Wtf.Ajax.request({
//            method: "GET",
//            url: Wtf.req.prj + 'getProjectMembership.jsp?userid1=' + loginid + '&userid2=' + this.guid,
//            scope: this,
//            success: this.conveyRelation
//        })
    },
    
    doctab: function(){
        this.doc = new Wtf.docs.com.Grid({
            id: 'community-tab-grid' + this.parentId,
            groupid: 2,
            closable: true,
            iconCls: getTabIconCls(Wtf.etype.docs),
            treeroot: this.comtitle,
            title: WtfGlobal.getLocaleText('lang.documents.text'),
            border: false,
            treeid: 'Community-Tree' + this.parentId,
            userid: loginid,
            treeRenderto: 'navareadocs',
            tabType: Wtf.etype.docs,
            pcid:this.parentId
        });
    },
    
    forumtab: function(fstatus){
         var roleId = 1;
        if (fstatus == 4 || fstatus == 5) 
            roleId = 3
        this.discussion = new Wtf.DiscussionForum({
            id: "for_" + this.parentId,
            closable: true,
            border: false,
            tabType: Wtf.etype.forum,
            iconCls: getTabIconCls(Wtf.etype.forum),
            roleId:roleId,
            projectFlag:true
        });
    },

	caltab: function(delayflag){
         toggleMainCal(0);
         var datePicker = new Wtf.DatePicker({
            id: this.parentId + 'calctrlcalpopup1',
            cls: 'datepicker',
            autoWidth: true,
            border: false,
            defaults: {
                autoHeight: true,
                autoScroll: true
            },
            renderTo: 'calendarcontainer'
        });
             var calTree = new Wtf.CalendarTree({
                 id: this.parentId + "Calendar",
                 url: Wtf.req.cal + "caltree.jsp",
                 ownerid:this.uid,
                 renderTo: "calendartree-container",
                 calControl:this.parentId + 'calctrl',
                 parentid:this.parentId,
                 parentTabId: "subtabpanelcom" + this.id,
                 datePicker:datePicker,
                 delayFlag:delayflag
                });
                
		this.calendar = new Wtf.cal.control({
			id: this.parentId + 'calctrl',
                        title: WtfGlobal.getLocaleText('pm.common.calendar'),
			border: false,
			myToolbar: true,
			closable: true,
			calTabId: this.parentId + 'tabmycal',
			layout: "fit",
                        ownerid:this.uid,
                        tabType:Wtf.etype.cal,
			iconCls:getTabIconCls(Wtf.etype.cal),
                        url: Wtf.req.cal + "caltree.jsp",
                        calTree:calTree,
                        calendar:datePicker
		});
           
		
	},
    
    projecttab: function(){
        this.pplan = new Wtf.Panel({
            title: WtfGlobal.getLocaleText('pm.common.projectplan'),
            id: this.parentId + "_projplanPane",
            layout: 'fit',
            autoDestroy: true,
            closable: true,
            border: false,
            tabType: Wtf.etype.pplan,
            iconCls: getTabIconCls(Wtf.etype.pplan)
        });
        this.pplan.on('activate',this.loadPlan,this);
        this.pplan.on("destroy",this.projDestroy);
    },
    projDestroy:function(){
        var tabId = (this.id).split('_');
        dojo.cometd.unsubscribe("/projectplan/"+tabId[0]);
    },
    
    loadPlan: function(){
          if(!Wtf.getCmp(this.parentId + 'projPlanCont')){
             var container = new Wtf.proj.containerPanel({
                id: this.parentId
            })
            this.pplan.add(container);
            container.doLayout();
            this.pplan.doLayout();
        }
    },

    conveyRelation: function(request, response){
        var res = eval('(' + request + ')').data[0];
        if (res.connstatus && ((res.connstatus == 3) || (res.connstatus == 4) || (res.connstatus == 5))) {
            this.pro.setTitle(WtfGlobal.getLocaleText('pm.project.Home.title'));
            
            this.projecttab();
            this.doctab();
            this.forumtab(res.connstatus);
            this.caltab(false);
            
            this.tabpanel = this.add(new Wtf.TabPanel({
                id: "subtabpanelcom" + this.id,
                border: false,
                items: [this.pro, this.pplan, this.doc, this.discussion, this.calendar]
            }));
            tabRegister["tab"+this.guid] = this.tabpanel.id;

            this.tabpanel.on("tabchange", function(panel, tab){
                toggleAccordion(tab.tabType);
            });
            
            this.tabpanel.setActiveTab(0);
            this.pro.on("discussionclicked", function(e){
                if (!Wtf.getCmp("forum_" + this.parentId)) {
                    this.forumtab();
                    this.tabpanel.add(this.discussion);
                }
                this.tabpanel.setActiveTab(this.discussion);
            }, this);
            
            this.pro.on("shareddocclicked", function(e){
                if (!Wtf.getCmp('community-tab-grid' + this.parentId)) {
                    this.doctab();
                    this.tabpanel.add(this.doc);
                    toggleAccordion(this.doc.tabType);
                }
                this.tabpanel.setActiveTab(this.doc);
            }, this);
            
            this.pro.on("projectplanclicked", function(){
                if (!Wtf.getCmp(this.parentId + "_projplanPane")) {
                    this.projecttab();
                    this.tabpanel.add(this.pplan);
                }
                this.pplan.doLayout();
                this.tabpanel.setActiveTab(this.pplan);
            }, this);
            
            this.pro.on("teamcalclicked", function(){
                if (!Wtf.getCmp(this.parentId + 'calCtrl')) {
                    this.caltab(true);
                    this.tabpanel.add(this.calendar);
                    toggleAccordion(this.calendar.tabType);
                }
                this.tabpanel.setActiveTab(this.calendar);
            }, this);
            
            this.doLayout();
        }
        else {
            this.add(this.pro);
            this.doLayout();
        }
    }
});
