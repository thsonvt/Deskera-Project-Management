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
Wtf.proj.common = {
    taskRecord: Wtf.data.Record.create([
        {name: 'taskid'},
        {name: 'diff'},
        {name: 'taskindex'},
        {name: 'taskname'},
        {name: 'duration',type:'string'},
        {name: 'startdate',type: 'date',dateFormat: 'Y-m-j'},
        {name: 'enddate',type: 'date',dateFormat: 'Y-m-j'},
        {name: 'predecessor'},
        {name: 'resourcename'},
        {name: 'parent'},
        {name: 'links'},
        {name: 'level',type:'int'},
        {name: 'percentcomplete'},
        {name: 'actstartdate',type: 'date',dateFormat: 'Y-m-j'},
        {name: 'actualduration',type:'string'},
        {name: 'ismilestone',type: 'boolean'},
        {name: 'isparent',type: 'boolean'},
        {name: 'priority'},
        {name: 'checklist'},
        {name: 'notes'}])

    ,pertRecord: Wtf.data.Record.create([
        {
            name: 'task',
            mapping: 'task'
        },{
            name: 'taskid',
            mapping: 'task.taskID'
        },{
            name: 'taskname',
            mapping: 'task.taskName'
        },{
            name: 'taskindex',
            mapping: 'task.taskIndex'
        },{
            name: 'optimistic'
        },{
            name: 'likely'
        },{
            name: 'pessimistic'
        },{
            name: 'expected'
        },{
            name: 'sd'
        },{
            name: 'variance'
        },{
            name: 'es'
        },{
            name: 'ef'
        },{
            name: 'ls'
        },{
            name: 'lf'
        },{
            name: 'slack'
        },{
            name: 'iscritical',
            mapping: 'isCritical'
        },{
            name:'taskStatus'
        }
    ])

    ,reportsMapping: {
        pm_report_resourcecost_title:"Cash Flow",
        pm_report_projectcost_title:"Project Cost",
        pm_project_plan_reports_overview_milestones:"Milestones",
        pm_project_plan_reports_overview_topleveltasks:"Top Level Tasks",
        pm_project_report_overduetask:"Overdue Tasks",
        pm_dashboard_widget_projectcharts_text:"Project Charts",
        pm_report_resourcetimesheet_title:"Resource Time-Sheet",
        pm_report_project_timesheet_title:"Task Usage",
        pm_project_plan_reports_workload_resourceanalysis:"Resource Analysis",
        pm_project_home_health_completed:"Completed Tasks",
        pm_project_plan_reports_activities_inprogress:"In-progress Tasks",
        pm_project_plan_reports_activities_ending:"Tasks Ending Between",
        pm_project_plan_reports_activities_starting:"Tasks Starting Between",
        pm_project_plan_reports_activities_unstarted:"Un-started Tasks",
        pm_project_plan_reports_baseline_projectsummary:"Project Summary",
        pm_project_plan_reports_baseline_date:"Date Comparison",
        pm_project_plan_reports_baseline_duration:"Duration Comparison",
        pm_project_plan_reports_baseline_cost:"Cost Comparison",
        pm_project_plan_reports_baseline_resource:"Resource Comparison",
        pm_project_plan_reports_baseline_resourcewise:"Resource-wise Task Comparison"
    },

    getReportTitleForEmbed: function(key){
        return Wtf.proj.common.reportsMapping[key.replace(/\./g, '_')];
    }
};

Wtf.ProjectTabs = function(config){
    Wtf.ProjectTabs.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.ProjectTabs, Wtf.Panel, {
    onRender: function(config){
        Wtf.ProjectTabs.superclass.onRender.call(this, config);
        this.modStatus = false;
        if(isPermalink >= 0){
            var projectIndex = projects.totalCount;
            this.projectIndex = projectIndex;
        } else {
            for(var cnt = 0; cnt < projects.length; cnt++){
                if(projects[cnt].id == this.parentId)
                    projectIndex = cnt;
                    this.projectIndex = projectIndex;
            }
        }
        this.pro = new Wtf.ProfileView({
            id: "subtabproj" + this.guid,
            uid: this.uid,
            layout: 'fit',
            profiletitle: this.comtitle,
            isource: this.tabprojimage,
            border: false,
            member: this.member,
            tabType: Wtf.etype.home,
            iconCls: getTabIconCls(Wtf.etype.home),
            projectIndex: projectIndex
        });

        this.pro.on("loadcomplete", function(){
            var p = Wtf.getCmp("tab" + this.parentId);
            if (p.activesubtab) {
                if(typeof p.activesubtab == "string"){
                    this.pro.fireEvent(p.activesubtab);
                    p.activesubtab = "";
                }
                else
                    this.pro.fireEvent(p.activesubtab.event);
            }
        }, this);

        if(WtfGlobal.isDefined(projectIndex)){
            var task = new Wtf.util.DelayedTask(function(){
                this.conveyRelation(projects[projectIndex].permissions);
            }, this);
            task.delay(20);
        } else {
            Wtf.Ajax.requestEx({
                url: Wtf.req.prj + 'getProjectMembership.jsp',
                params : {
                    userid1:loginid,
                    userid2:this.guid
                },
                method: 'GET'
            },
            this,
            function(result, request) {
                this.conveyRelation(result, request);
            });
        }
    },

    doctab: function(){
//        if(isRoleGroup(3)){
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
                pcid: this.parentId
            });
//        }
    },
    todo:function(fstatus){
        var tdo=null;
        if(fstatus === undefined)
            fstatus = this.connstatus;
        var roleId = 1;
        if (fstatus == 4 || fstatus == 5)
            roleId = 3
        var panelTitle = WtfGlobal.getLocaleText('pm.project.todo.info');
        if(this.archived)
            panelTitle = WtfGlobal.getLocaleText('pm.project.todo.readonly');
        this.todolist = new Wtf.Panel({
            title: WtfGlobal.getLocaleText('pm.module.todo'),
            layout: 'fit',
            id: 'list_conainer' + this.parentId,
            closable: true,
            //            autoScroll: true,
            tabType: Wtf.etype.todo,
            iconCls: getTabIconCls(Wtf.etype.todo),
            items: [tdo=new Wtf.TodoList({
                title: WtfGlobal.getInstrMsg(panelTitle),
                id: 'todo_list' + this.parentId,
                layout: 'fit',
                roleid: roleId,
                archived: this.archived,
                userid: this.parentId,
                groupType: 2,
                animate: true,
                baseCls: 'todoPanel',
                enableDD: true,
                containerScroll: true,
                border: false,
                rootVisible: false
            })]
        });

        this.todolist.on("activate",function(){
            if(!tdo.isDataLoad){
                tdo.ds1.load();
            }
        },tdo)
    },

    forumtab: function(fstatus){
        if(fstatus === undefined)
            fstatus = this.connstatus;
        var roleId = 1;
        if (fstatus == 4 || fstatus == 5)
            roleId = 3
        this.discussion = new Wtf.DiscussionForum({
            id: "for_" + this.parentId,
            closable: true,
            border: false,
            archived: this.archived,
            tabType: Wtf.etype.forum,
            iconCls: getTabIconCls(Wtf.etype.forum),
            roleId: roleId,
            projectFlag: true
        });
    },

    caltab: function(delayflag){
        this.calcont=new Wtf.Panel({
            id:"cal-tree-container"+this.parentId,
            region:'center',
            autoScroll:'true',
            cls:'calTreeContainer',
            border:false
        });

        this.dtcont=new Wtf.Panel({
            id:"dt-picker-container"+this.parentId,
            region:'north',
            height:200,
            border:false
        });
        this.helpcont = new Wtf.Panel({
            width:'100%',
            id: "help-container"+this.parentId,
            title:WtfGlobal.getLocaleText('pm.project.calendar.helptitle'),
            height: 100,
            region: 'south',
            closable: false,
            style: 'background-color: #E8EEF7;',
            frame: false,
            border: false,
            html: '<div class="calQuickTip">'+WtfGlobal.getLocaleText('pm.project.calendar.helptext')+'</div>'
        });
        this.calendar=new Wtf.Panel({
            layout:'border',
            title: WtfGlobal.getLocaleText('pm.module.teamcalendar'),
            closable:true,
            tabType: Wtf.etype.cal,
            autoDestroy: true,
            iconCls: getTabIconCls(Wtf.etype.cal),
            items:[
                this.calendarCtrl = new Wtf.cal.control({
                    id: this.parentId + 'calctrl',
                    region:'center',
                    border: false,
                    myToolbar: true,
                    closable: true,
                    archived: this.archived,
                    calTabId: this.parentId + 'tabmycal',
                    layout: "fit",
                    ownerid: this.uid,
                    url: Wtf.req.cal + "caltree.jsp",
                    calcont:this.calcont,
                    dtcont:this.dtcont
                }),{
                    region:'west',
                    width:190,
                    cls:'westcalContainer',
                    id: this.id + 'westcalContainer',
                    bodyStyle:'background:#fff;',
                    layout:'border',
                    border:'false',
                    items:[this.calcont,this.dtcont]
                }]
        });
        if(!this.archived)
            Wtf.getCmp(this.id+'westcalContainer').add(this.helpcont);
        this.calendar.on("hide", function() {
            var expandWin = Wtf.getCmp(this.parentId+'Expand');
            if(expandWin !== undefined)
                expandWin.close();
        }, this);
        this.calendar.on("destroy", this.destroyCal, this);
    },

    projdocstab: function(){
        this.projdoc = new Wtf.Panel({
             id:'proj-doc'+ this.parentId,
             title:WtfGlobal.getLocaleText('lang.documents.text'),
             layout:'fit',
             closable:true,
             autoDestroy: true,
             iconCls: getTabIconCls(Wtf.etype.docs),
             items:[new Wtf.docs.com.Grid({
                            id: 'doc-mydocs1'+ this.parentId,
                            archived: this.archived,
                            groupid: 1,
                            border: false,
                            treeroot: WtfGlobal.getLocaleText('pm.dashboard.widget.mydocuments.text'),
                            autoWidth: true,
                            userid: loginid,
                            treeid: 'doctree-mydocs',
                            treeRenderto: 'navareadocs',
                            pcid:1
                    })]
        });
        Wtf.getCmp('proj-doc'+ this.parentId).doLayout();
    },

    destroyCal: function(obj) {
        var expandWin = Wtf.getCmp(this.parentId+'Expand');
        if(expandWin !== undefined)
            expandWin.destroy();

        this.calendarCtrl.currentview = "DayView";
        this.calendarCtrl.stopCalEventBot();
        this.calendarCtrl.stopCalTreeBot();
        if(Wtf.get(this.calendar.id) !== null)
            this.tabpanel.remove(this.calendar, true);
        this.calTree=null;
        this.calendar=null;

        /*toggleMainCal(1);
        var chkNode = this.calTree.getChecked();
        for (var i = 0; i < chkNode.length; i++)
            chkNode[i].attributes.request = false;
        if (this.calTree)
            this.calTree.destroy();
        if (this.calendar)
            this.datePicker.destroy();*/
    },

    projecttab: function(connstatus){
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
        this.pplan.on('activate',function() {
            this.loadPlan(connstatus);
            var cont = Wtf.getCmp(this.parentId + 'projPlanCont');
            if(cont && cont.refreshOnActivate){
                cont.refreshContainer();
            }
        }, this);
        this.pplan.on("destroy", this.projDestroy);
    },
    projDestroy: function(){
        var tabId = (this.id).split('_');
        dojo.cometd.unsubscribe("/projectplan/" + tabId[0]);
    },

    loadPlan: function(connstatus){
        var planview = 'day';
        if(WtfGlobal.isDefined(this.projectIndex)){
            var perms = getMyPlanPermission(this.parentId, this.projectIndex);
            connstatus = perms.data[0].connstatus;
            this.archived = perms.data[0].archived;
            planview = projects[this.projectIndex].planview;
        }
        if (!Wtf.getCmp(this.parentId + 'projPlanCont')) {
            var container = new Wtf.proj.containerPanel({
                id: this.parentId,
                archived: this.archived,
                connstatus : connstatus,
                pname: this.comtitle,
                projScale : planview,
                projectIndex: this.projectIndex
            });
            this.pplan.add(container);
            container.doLayout();
            this.pplan.doLayout();
        }
    },
    adminpagetab: function(){
        this.adminpage = new Wtf.common.adminpage({
            id: "admin_" + this.guid,
            featureid: this.guid,
            archived: this.archived,
            mode: 7,
            title: WtfGlobal.getLocaleText('pm.module.projectsettings'),
            closable: true,
            layout: 'fit',
            iconCls: 'pwnd admintab'
        });
    },

    conveyRelation: function(request, response){
        if(typeof request == 'string')
            var res = eval('(' + request + ')').data[0];
        else {
            res = getMyPlanPermission(this.parentId, this.projectIndex);
            res = res.data[0];
        }
        if (res && (res.connstatus >= 3 && res.connstatus <= 8)) {
            this.archived = res.archived;
            this.planpermission = res.planpermission;
            this.pro.setTitle(WtfGlobal.getLocaleText('pm.project.Home.title'));
//            this.projecttab(res.connstatus);
//            this.doctab();
//            this.todo(res.connstatus);
            this.connstatus = res.connstatus;
//            this.forumtab(res.connstatus);
            this.tabpanel = this.add(new Wtf.TabPanel({
                id: "subtabpanelcom" + this.id,
                border: false,
//                connstatus: res.connstatus,
                enableTabScroll: true,
                items: [this.pro/*, this.pplan, this.discussion,this.todolist,this.calendar*/]
            }));
            if(Wtf.featuresView.proj) {
                this.projecttab(res.connstatus);
                this.tabpanel.add(this.pplan);
            }
            if(Wtf.featuresView.disc) {
                this.forumtab(res.connstatus);
                this.tabpanel.add(this.discussion);
            }
            if(Wtf.featuresView.todo) {
                this.todo(res.connstatus);
                this.tabpanel.add(this.todolist);
            }
            if(Wtf.subscription.cal.subscription && Wtf.featuresView.cal) {
                this.caltab(false);
                this.tabpanel.add(this.calendar);
            }
            if(Wtf.subscription.docs.subscription && Wtf.featuresView.docs) {
                this.projdocstab();
                this.tabpanel.add(this.projdoc);
            }
            tabRegister["tab" + this.guid] = this.tabpanel.id;

            if ( res.connstatus == 4) {
                this.adminpagetab();
                this.tabpanel.add(this.adminpage);
            }

            /*this.tabpanel.on("tabchange", function(panel, tab){
                toggleAccordion(tab.tabType);
            });*/

            this.tabpanel.setActiveTab(0);
            this.pro.on("discussionclicked", function(e){
                if (!Wtf.getCmp("for_" + this.parentId)) {
                    this.forumtab(res.connstatus);
                    this.tabpanel.add(this.discussion);
                }
                this.tabpanel.setActiveTab(this.discussion);
            }, this);
//            if (isRoleGroup(3)) {
//                this.doctab();
//                this.tabpanel.add(this.doc);

            /*this.pro.on("shareddocclicked", function(e){
                if (!Wtf.getCmp('community-tab-grid' + this.parentId)) {
                    this.doctab();
                    this.tabpanel.add(this.doc);
                    toggleAccordion(this.doc.tabType);
                }
                this.tabpanel.setActiveTab(this.doc);
            }, this);*/
//            }
            this.pro.on("projectplanclicked", function(){
                if (!Wtf.getCmp(this.parentId + "_projplanPane")) {
                    this.projecttab(res.connstatus);
                    this.tabpanel.add(this.pplan);
                }
                this.pplan.doLayout();
                this.tabpanel.setActiveTab(this.pplan);
            }, this);
            this.pro.on("todoclicked", function(e){
                if (!Wtf.getCmp('list_conainer' + this.parentId)) {
                    this.todo();
                    this.tabpanel.add(this.todolist);
                //toggleAccordion(this.todolist.tabType);
                }
                this.tabpanel.setActiveTab(this.todolist);
            }, this);
            this.pro.on("teamcalclicked", function(){
                if (!Wtf.getCmp(this.parentId + 'calctrl')) {
                    this.caltab(true);
                    this.tabpanel.add(this.calendar);
                //toggleAccordion(this.calendar.tabType);
                }
                this.tabpanel.setActiveTab(this.calendar);
            //                var p = Wtf.getCmp("tab" + this.parentId);
            /* var tnode = this.calendar.calTree.getNodeById(p.activesubtab.id);
                if (tnode) {
                    if (!tnode.ui.checkbox.checked) {
                        tnode.ui.checkbox.checked = true;
                        tnode.attributes.checked = true;
                        this.calendar.calTree.fireEvent("treecheckchange", this.calendar.calTree, tnode, true);
                    }
                }
                else {
                    Wtf.Msg.alert('Invalid Calendar', 'Selected calendar doesn\'t exist or has been deleted!');
                }*/
            }, this);
            this.pro.on("projnewdocclicked", function(){
                if(!Wtf.getCmp('proj-doc' + this.parentId)) {
                        this.projdocstab();
                        this.tabpanel.add(this.projdoc);
                    }
                   this.tabpanel.setActiveTab(this.projdoc);
            }, this);
            if (res.connstatus == 4) {
                this.pro.on("adminpageclicked", function(e){
                    if(!Wtf.getCmp('admin_' + this.parentId)) {
                        this.adminpagetab();
                        this.tabpanel.add(this.adminpage);
                    }
                    this.tabpanel.setActiveTab(this.adminpage);
                }, this);
            }
            this.doLayout();
        }
        else {
            this.add(this.pro);
            this.doLayout();
        }
    }
});
/*  WtfProjView: End    */
//if(Wtf.subscription.cal)
//    WtfGlobal.loadScript("../../scripts/minified/calendar.js");
//
//WtfGlobal.loadScript("../../scripts/cal/WtfCalManager.js");
//WtfGlobal.loadScript("../../scripts/cal/WtfCalSettings.js");
//WtfGlobal.loadScript("../../scripts/cal/WtfCalDragPlugin.js");
//WtfGlobal.loadScript("../../scripts/cal/AddingEvent/WtfEventDetails.js");
//WtfGlobal.loadScript("../../scripts/cal/CalendarTree/WtfCalTree.js");

var projobtid = projectTabs.shift();
var projuid = new Wtf.common.Uid(projobtid.id.substr(3), Wtf.etype.proj);
var project = new Wtf.ProjectTabs({
    id: 'projectTabs_' + projobtid.id.substr(3),
    guid: projobtid.id.substr(3),
    uid: projuid,
    parentId: projobtid.id.substr(3),
    tabid : projobtid.id,
    layout: 'fit',
    comtitle: projobtid.title,
    border: false
});
mainPanel.loadMask.show();
Wtf.getCmp(projobtid.id).add(project);
Wtf.getCmp(projobtid.id).doLayout();
