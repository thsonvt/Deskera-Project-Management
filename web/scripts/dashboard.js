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
var widgetcount = 0;

function pagingRedirect(panelid, pager, subPan, searchstr, panelcount){
    var myPanel = Wtf.getCmp(panelid);
    myPanel.doPaging(myPanel.config1[subPan].url, (panelcount * pager), searchstr, pager, subPan);
}

function quoteReplace(psString){
    var lsRegExp = /'|%/g;
    return String(psString).replace(lsRegExp, "");
}

function btnpressed(panelid){
    var searchid = "search" + panelid;
    var searchstr = document.getElementById(searchid).value;
    searchstr = quoteReplace(searchstr);
    var myPanel = Wtf.getCmp(panelid);
    myPanel.doSearch(myPanel.url, searchstr);
}

function createtooltip1(target, tpl_tool_tip, autoHide, closable, height){
    usertooltip = tpl_tool_tip, new Wtf.ToolTip({
        id: "1KChampsToolTip" + target,
        autoHide: autoHide,
        closable: closable,
        html: usertooltip,
        height: height,
        target: target
    });
}

function getQuickLinksHeaderHtml(){
    var s = "";
    if(!deskeraAdmin){
        if(welcome == false)
            s = "<a href=\""+Wtf.pagebaseURL+"feed.rss?m=global&u="+loginname+"\" target='_blank'><span class='dpwnd rssdash quickaddonly' wtf:qtip='"+WtfGlobal.getLocaleText('pm.Help.rssfeed')+"'></span></a>"+
            "<a href='#' onclick=\"getProjectWindow()\"><span id ='dashlink' class='dpwnd quickaddprowid quickaddwithrss' wtf:qtip='"+WtfGlobal.getLocaleText('pm.Help.addprojectwidget')+"' wtf:qtitle='"+WtfGlobal.getLocaleText('pm.Help.projwidget')+"'></span></a>";
        if(Wtf.UPerm.CreateProject)
            s += "<span class='createProjectcss quickaddwithrss'><a href='#' onclick=\"getCreateProjectWindow()\" wtf:qtitle='"+WtfGlobal.getLocaleText('pm.dashboard.widget.quicklinks.createnewproject')+"' wtf:qtip='"+WtfGlobal.getLocaleText('pm.Help.newprojectdash')+"'>"+WtfGlobal.getLocaleText('pm.dashboard.widget.quicklinks.createnewproject')+"</a></span>";
    }
    return s;
}

function getMyDocsToolsArrayForModules() {
    var ta = [];
    ta.push({
        id:'w_mydocs',
        qtip: WtfGlobal.getLocaleText('Upload.a.Document'),
        scope:this,
        handler: function(e, target, panel){
            panel.press = 3;
            quickAdd(panel.id);
            panel.tools.w_mydocs.dom.style.display = 'none';
        }
    });
    return ta;
}

function toolHandler(projectid, seq, tool){
    var panel = Wtf.getCmp(projectid+'_drag');
    var projname = projects[seq].name;
    var perm = projects[seq].permissions.data[0];
    var qa = "";
    switch(tool){
        case 'w_projectplan':
            panel.press = 0;
            panel.config1[0].paramsObj.mode = 0;
            if(!perm.archived && ((perm.planpermission == 4 && perm.connstatus == 3) || (perm.connstatus == 4 || perm.connstatus == 5)))
                panel.config1[0].headerHtml = "<span class ='dpwnd quickadd quickaddonly' onclick= \"quickAdd('"+ panel.id+"','"+ seq +"')\" wtf:qtip='"+WtfGlobal.getLocaleText({key:'pm.dashboard.widget.newtask.tip',params:projname})+"'></span>";
            else
                panel.config1[0].headerHtml = "";
            break;
        case 'w_todo':
            panel.press = 1;
            panel.config1[0].paramsObj.mode = 1;
            panel.config1[0].headerHtml = "";
            qa = (perm.archived || !(perm.planpermission == 2 || perm.planpermission == 8 || perm.planpermission == 10)) ? "<a href=\""+Wtf.pagebaseURL+"feed.rss?m=todos&p="+projectid+"\" target='_blank'><span class='dpwnd rssdash quickaddonly' wtf:qtip='"+WtfGlobal.getLocaleText({key:"pm.dashboard.widget.rss.todo.tip",params:projname})+"'></span></a>"+
            "<span class ='dpwnd quickadd quickaddwithrss' onclick= \"quickAdd('"+ panel.id+"','"+ seq +"')\" wtf:qtip='"+WtfGlobal.getLocaleText({key:'pm.dashboard.widget.newtodo.tip',params:projname})+"'></span>" : "";
            panel.config1[0].headerHtml += qa;
            break;
        case 'w_calendar':
            panel.press = 2;
            panel.config1[0].paramsObj.mode = 2;
            panel.config1[0].headerHtml = "";
            qa = (perm.archived || !(perm.planpermission == 2 || perm.planpermission == 8 || perm.planpermission == 10)) ? "<a href=\""+Wtf.pagebaseURL+"feed.rss?m=events&u="+loginname+"&p="+projectid+"\" target='_blank'><span class='dpwnd rssdash quickaddonly' wtf:qtip='"+WtfGlobal.getLocaleText({key:'pm.dashboard.widget.rss.calendar.tip',params:projname})+"'></span></a>"+
            "<span class ='dpwnd quickadd quickaddwithrss' onclick= \"quickAdd('"+ panel.id+"','"+ seq +"')\" wtf:qtip='"+WtfGlobal.getLocaleText({key:'pm.dashboard.widget.newevent.tip',params:projname})+"'></span>" : "";
            panel.config1[0].headerHtml += qa;
            break;
        case 'w_docs':
            panel.press = 3;
            panel.config1[0].paramsObj.mode = 3;
            panel.config1[0].headerHtml = (perm.archived || !(perm.planpermission == 2 || perm.planpermission == 8 || perm.planpermission == 10)) ? "<span class ='dpwnd quickadd quickaddonly' onclick= \"quickAdd('"+ panel.id+"','"+ seq +"')\" wtf:qtip='"+WtfGlobal.getLocaleText({key:'pm.dashboard.widget.newdoc.tip',params:projname})+"'></span>" : "";
            break;
        case 'w_admin':
            panel.press = 4;
            panel.config1[0].paramsObj.mode = 4;
            panel.config1[0].headerHtml = '';
            break;
        case 'w_reports':
            panel.press = 5;
            panel.config1[0].paramsObj.mode = 5;
            panel.config1[0].headerHtml = '';
            break;
        case 'w_discussion':
            panel.press = 6;
            panel.config1[0].paramsObj.mode = 6;
            panel.config1[0].headerHtml = '';
            break;
    }
    panel.callRequest();

}

function getToolsArrayForModules(code,managePerm, seq, projectid, mod) {
    var ta = [];
    var tip=listViewTips(code);
    managePerm = projects[seq].permissions.data[0];
    if(Wtf.featuresView.proj) {
        ta.push({
            id:'w_projectplan',
            qtip: tip.plan,
            handler: function(a,b,panel){
                panel.changeImg(0, panel.tools.w_projectplan, mod);
                toolHandler(projectid, seq, 'w_projectplan');
            }
        });
    }
    if(Wtf.featuresView.disc) {
        ta.push({
            id: (mod == 6) ? 'w_discussion':'w_discussion_dim',
            qtip : tip.disc,
            handler: function(a,b,panel){
                if(mod == 6)
                    panel.changeImg(6, panel.tools.w_discussion,mod);
                else
                    panel.changeImg(6, panel.tools.w_discussion_dim,mod);
                toolHandler(projectid, seq, 'w_discussion')
            }
        });
    }
    if(Wtf.featuresView.todo) {
        ta.push({
            id: (mod == 1) ? 'w_todo':'w_todo_dim',
            qtip : tip.todo,
            handler: function(a,b,panel){
                if(mod == 1)
                    panel.changeImg(1, panel.tools.w_todo,mod);
                else
                    panel.changeImg(1, panel.tools.w_todo_dim,mod);
                toolHandler(projectid, seq, 'w_todo')
            }
        });
    }
    if(Wtf.subscription.cal.subscription && Wtf.featuresView.cal){
        ta.push({
            id: (mod == 2) ? 'w_calendar':'w_calendar_dim',
            qtip : tip.cal,
            handler: function(a,b,panel){
                if(mod == 2)
                    panel.changeImg(2, panel.tools.w_calendar,mod);
                else
                    panel.changeImg(2, panel.tools.w_calendar_dim,mod);
                toolHandler(projectid, seq, 'w_calendar')
            }
        });
    }
    if(Wtf.subscription.docs.subscription && Wtf.featuresView.docs){
        ta.push({
            id: (mod == 3) ? 'w_docs':'w_docs_dim',
            qtip : tip.docs,
            handler: function(a,b,panel){
                if(mod == 3)
                    panel.changeImg(3, panel.tools.w_docs,mod);
                else
                    panel.changeImg(3, panel.tools.w_docs_dim,mod);
                toolHandler(projectid, seq, 'w_docs')
            }
        });
    }
    if(managePerm.connstatus == 4){
        ta.push({
            id: (mod == 4) ? 'w_admin':'w_admin_dim',
            qtip : tip.admin,
            handler: function(a,b,panel){
                if(mod == 4)
                    panel.changeImg(4, panel.tools.w_admin,mod);
                else
                    panel.changeImg(4, panel.tools.w_admin_dim,mod);
                toolHandler(projectid, seq, 'w_admin')
            }
        });
    }
    if(Wtf.featuresView.proj && ((managePerm.connstatus == 3 && managePerm.planpermission != 16) || (managePerm.connstatus == 4 || managePerm.connstatus == 5))){
        ta.push({
            id: (mod == 5) ? 'w_reports':'w_reports_dim',
            qtip : tip.reports,
            handler: function(a,b,panel){
                if(mod == 5)
                    panel.changeImg(5, panel.tools.w_reports,mod);
                else
                    panel.changeImg(5, panel.tools.w_reports_dim,mod);
                toolHandler(projectid, seq, 'w_reports')
            }
        });
    }
    return ta;
}

function getCloseTool(){
    var ta = [];
    ta.push({
        id: 'w_close',
        handler: function(e, target, panel){
            var tt = panel.title;
            panel.ownerCt.remove(panel, true);
            panel.destroy();
            removeWidget(tt);
        }
    });
    return ta;
}

function listViewTips(code) {
    var toolTip=new Object();
    toolTip.plan=WtfGlobal.getLocaleText('lang.view.text')+" ";
    toolTip.todo=WtfGlobal.getLocaleText('lang.view.text')+" ";
    toolTip.cal=WtfGlobal.getLocaleText('lang.view.text')+" ";
    toolTip.docs=WtfGlobal.getLocaleText('lang.view.text')+" ";
    toolTip.admin=WtfGlobal.getLocaleText('lang.view.text')+" ";
    toolTip.reports=WtfGlobal.getLocaleText('lang.generate.text')+" ";
    toolTip.disc=WtfGlobal.getLocaleText('lang.view.text')+" ";
    switch(code){
        case 0:
            toolTip.plan+=WtfGlobal.getLocaleText('pm.common.projectplan')+" ";
            toolTip.todo+=WtfGlobal.getLocaleText('pm.project.todo.text')+" ";
            toolTip.cal+=WtfGlobal.getLocaleText('pm.common.calendar')+" ";
            toolTip.docs+=WtfGlobal.getLocaleText('lang.documents.text')+" ";
            toolTip.admin+=WtfGlobal.getLocaleText('pm.module.projectsettings')+" ";
            toolTip.reports+=WtfGlobal.getLocaleText('pm.common.reports')+" ";
            toolTip.disc+=WtfGlobal.getLocaleText('pm.module.discussion')+" ";
            break;
    }
    toolTip.plan+=WtfGlobal.getLocaleText('pm.dashboard.widget.project.plan.tip');
    toolTip.todo+=WtfGlobal.getLocaleText('pm.dashboard.widget.project.todo.tip');
    toolTip.cal+=WtfGlobal.getLocaleText('pm.dashboard.widget.project.calendar.tip');
    toolTip.docs+=WtfGlobal.getLocaleText('pm.dashboard.widget.project.docs.tip');
    toolTip.admin+=WtfGlobal.getLocaleText('pm.dashboard.widget.project.settings.tip');
    toolTip.reports+=WtfGlobal.getLocaleText('pm.dashboard.widget.project.reports.tip');
    toolTip.disc+=WtfGlobal.getLocaleText('pm.dashboard.widget.project.discussion.tip');
    return toolTip;
}

function createNewPanel(setting, obj){
    if(setting !== undefined) {
        if (setting.config1 != null) {
            return (new Wtf.WtfCustomPanel(setting, obj));
        } else if (setting.config0 != null) {
            return (new Wtf.WtfCustomPanel(setting, obj));
        }
        else {
            if (setting.url != null) {
                return (new Wtf.WtfIframeWidgetComponent(setting));
            } else {
                return (new Wtf.WtfWidgetComponent(setting));
            }
        }
    }
}

function addProjectWidget(pid, toAdd){
    if(toAdd == 'true'){
        var ix = projId.indexOf(pid);
        var count = 0
        var lowCountCol = 1;
        var lowCount = 1;
        if (Wtf.getCmp("portal_container_box3").items != null) {
            lowCount = Wtf.getCmp("portal_container_box3").items.length;
        } else if (Wtf.getCmp("portal_container_box2").items != null) {
            lowCount = Wtf.getCmp("portal_container_box2").items.length;
        } else if (Wtf.getCmp("portal_container_box1").items != null) {
            lowCount = Wtf.getCmp("portal_container_box1").items.length;
        }

        for (var i = 3; i > 0; i--) {
            count=0;
            if (Wtf.getCmp("portal_container_box"+i).items != null){
                count = Wtf.getCmp("portal_container_box" + i).items.length;
            }
            if (count <= lowCount) {
                lowCount = count;
                lowCountCol = i;
            }
        }

        var pl = Wtf.getCmp("portal_container_box" + lowCountCol);
        if (pl != null) {
            var pn = createNewPanel(panelArr[ix]);
            pl.add(pn);
            pl.doLayout();
        }
        isToAppendArray[ix] = 1;
        insertIntoWidgetState(lowCountCol,widgetIdArray[ix]);
    } else {
        var panel = Wtf.getCmp(pid+'_drag');
        var tt = panel.id;
        panel.ownerCt.remove(panel, true);
        panel.destroy();
        removeWidget(tt);
    }
}

function insertIntoWidgetState(colno,wid){
    Wtf.Ajax.requestEx({
        url: Wtf.req.widget + 'widget.jsp',
        params:{
            flag:99,
            wid:wid,
            colno:colno,
            welcome: welcome
        }
    }, this, function(){
        }, function(){})

}
function removeWidget(tt){
    var ix = widgetIdArray.indexOf(WtfGlobal.HTMLStripper(tt));
    isToAppendArray[ix] = 0;
    requestForWidgetRemove(widgetIdArray[ix]);
}

function getProjectListByProgress(){
    var projRecordConfig = [
             {name: 'projectid'},
             {name: 'projectname'},
             {name: 'health'},
             {name: 'completed'},
             {name: 'inprogress'},
             {name: 'needattention'},
             {name: 'overdue'},
             {name: 'future'}
         ];
    var cmConfig =new Wtf.grid.ColumnModel([{
            header: WtfGlobal.getLocaleText('pm.project.health.text'),
            dataIndex:"health",
            renderer:function(val){
                if(val==1) return "<img src='../../images/health_status/ontime.gif' style='vertical-align:text-bottom'/><span style='color:#38A612'>"+WtfGlobal.getLocaleText('pm.project.home.health.ontime')+"</span>";
                else if(val==2) return "<img src='../../images/health_status/slightly.gif' style='vertical-align:text-bottom'/><span style='color:#DEB310'>"+WtfGlobal.getLocaleText('pm.project.home.health.slightly')+"</span>";
                else if(val==3) return "<img src='../../images/health_status/gravely.gif' style='vertical-align:text-bottom'/><span style='color:#C42737'>"+WtfGlobal.getLocaleText('pm.project.home.health.gravely')+"</span>";
                else return "<img src='../../images/health_status/else_status.gif' style='vertical-align:text-bottom'/>Future Task";
            },
            groupable: true
        },{
            header: WtfGlobal.getLocaleText('pm.cr.project.projectname'),
            dataIndex: 'projectname',
            autoWidth : true,
            width :350,
            sortable: true
        },{
            header : WtfGlobal.getLocaleText('pm.project.home.health.completed'),
            dataIndex: 'completed',
            autoSize : true,
            sortable: true
        },{
            header : WtfGlobal.getLocaleText('pm.project.home.health.inprogress'),
            dataIndex: 'inprogress',
            autoSize : true,
            sortable: true
        },{
            header : WtfGlobal.getLocaleText('pm.project.home.health.needattention'),
            dataIndex: 'needattention',
            autoSize : true,
            sortable: true
        },{
            header : WtfGlobal.getLocaleText('pm.project.home.health.overdue'),
            dataIndex: 'overdue',
            autoSize : true,
            sortable: true
        },{
            header : WtfGlobal.getLocaleText('pm.project.home.health.future'),
            dataIndex: 'future',
            autoSize : true,
            sortable: true
        }]);
    var selectionModel = new Wtf.grid.CheckboxSelectionModel();
    var projRec = new Wtf.data.Record.create(projRecordConfig);
    var dataReader = new Wtf.data.KwlJsonReader({
        totalProperty: 'count',
        root: "data"
    },projRec);
    var projds = new Wtf.data.GroupingStore({
        url : Wtf.req.widget + 'widget.jsp',
        baseParams: {
            flag:7
        },
        reader: dataReader,
        method : 'GET',
        sortInfo: {
            field: 'projectname',
            direction: "ASC"
        },
        groupField:'health'
    });
    var grpView = new Wtf.grid.GroupingView({
        forceFit: true,
        showGroupName: false,
        enableGroupingMenu: false,
        hideGroupedColumn: true,
        groupTextTpl: '{text}',
        emptyText: '<div class="emptyGridText">'+WtfGlobal.getLocaleText('pm.project.grid.emptytext')+'<br><a href="#" onClick=\'createNewProject(\"'+this.id+'\")\'>'+WtfGlobal.getLocaleText('pm.project.grid.starttext')+'</a></div>'
    });
    var projectsgrid = new Wtf.grid.GridPanel({
        id: 'projhealthcountgrid',
        store: projds,
        cm: cmConfig,
        sm: selectionModel,
        border: false,
        loadMask: true,
        enableHdMenu: false,
        view: grpView,
        viewConfig: {
            forceFit:true
        },
        autoHeight:true
    });
    return projectsgrid;
}
var panelArr = [];
if(welcome == false){
    for(var pc=0; pc<projects.totalCount; pc++){
        var mod = 0;
        var perm = getMyPlanPermission(projects[pc].id, pc).data[0];
        if(!Wtf.featuresView.proj){
            mod = 6;
            if(!Wtf.featuresView.disc){
                mod = 1
                if(!Wtf.featuresView.todo){
                    mod = 2;
                    if(!Wtf.featuresView.cal){
                        mod = 3;
                        if(!Wtf.featuresView.docs){
                            mod = 4;
                            if(!perm.connstatus == 4){
                                mod = 5;
                                if(!perm.connstatus != 6 && !perm.connstatus != 7 && !perm.connstatus != 8){
                                    mod = 0;
                                }
                            }
                        }
                    }
                }
            }
        }
   panelArr.push({
        config1:[{
            url : Wtf.req.widget + 'widget.jsp',
                numRecs:5,
                template:new Wtf.XTemplate(
                    "<tpl><div class='workspace listpanelcontent'>"+
                    "<div>{update}</div>" +
                    "</div></tpl>"),
                isPaging: true,
                pagingflag: true,
                emptyText:WtfGlobal.getLocaleText('pm.common.noupdate'),
                isSearch: false,
                headerHtml : (Wtf.featuresView.proj) ?
                    (!perm.archived && ((perm.planpermission == 4 && perm.connstatus == 3) || (perm.connstatus == 4 || perm.connstatus == 5))) ?
                    "<span class ='dpwnd quickadd quickaddonly' onclick= \"quickAdd('"+ projId[pc]+"_drag','" +pc+ "')\" wtf:qtip='"+WtfGlobal.getLocaleText({key:'pm.dashboard.widget.newtask.tip',params:projects[pc].name})+"'></span>" : "" : "",
                linkcode : 0,
                paramsObj: {
                    flag: 0,
                    mode: mod,
                    projectid: projId[pc]
                }
            }],
            draggable:true,
            press:0,
            border:true,
            isProject: true,
            title: "<div wtf:qtip='"+WtfGlobal.getLocaleText('pm.Help.widHeader')+"'>"+Wtf.util.Format.ellipsis(projects[pc].name, 35)+"</div>",
            id : projId[pc]+'_drag',
            tools: getToolsArrayForModules(0,true,pc,projId[pc],mod)//add moderators permission check and modify plan permission for reports
        });
    }

   panelArr.push({
        config1:[{
            url : Wtf.req.widget + 'widget.jsp',
            numRecs:1,
            template:new Wtf.XTemplate("<tpl><div>{update}</div></tpl>"),
            isPaging: (projects.totalCount <= 5) ? false : true,
            emptyText:'',
            isSearch: false,
            pagingflag: (projects.totalCount <= 5) ? false : true,
            headerHtml : '',
            paramsObj: {
                flag:4,
                projCount: projects.totalCount
            }
        }
        ],
        draggable:true,
        border:true,
        isProject:true,
        title: "<div wtf:qtip='"+WtfGlobal.getLocaleText('pm.Help.widHeader')+"'>"+WtfGlobal.getLocaleText('pm.dashboard.widget.projectcharts.text')+"</div>",
        id : "chart_drag",
        tools: ''//getCloseTool()
    });
} else {
    panelArr.push({
        config1:[{
            url : Wtf.req.widget + 'widget.jsp',
            numRecs:1,
            template:new Wtf.XTemplate("<tpl><div>{update}</div></tpl>"),
            isPaging: false,
            pagingflag: false,
            emptyText:'',
            isSearch: false,
            headerHtml : '',
            paramsObj: {
                flag:101
            }
        }],
        draggable:false,
        border:true,
        isProject: false,
        title: WtfGlobal.getLocaleText('pm.welcometext'),
        id : 'welcome_drag',
        tools: ''
    });
}
if(Wtf.subscription.docs.subscription && Wtf.featuresView.docs){
    panelArr.push({
        config1:[{
            url : Wtf.req.widget + 'widget.jsp',
            numRecs:5,
            template:new Wtf.XTemplate(
                "<tpl><div class='workspace listpanelcontent'>"+
                "<div>{update}</div>" +
                "</div></tpl>"),
            isPaging: true,
            pagingflag: true,
            emptyText:WtfGlobal.getLocaleText('pm.project.document.nodoc'),
            isSearch: false,
            headerHtml : '',
            paramsObj: {
                flag:6
            }
        }],
        id : "mydocs_drag",
        draggable:true,
        isProject: true,
        border:true,
        title: "<div wtf:qtip='"+WtfGlobal.getLocaleText('pm.Help.widHeader')+"'>"+WtfGlobal.getLocaleText('pm.dashboard.widget.mydocuments.text')+"</div>",
        tools: getMyDocsToolsArrayForModules()
    });
}
panelArr.push({
    config1:[{
        itemArr: [getProjectListByProgress()],
        isItems: true
    }],
    id:"taskwiseprojecthealth_drag",
    draggable:true,
    isProject: true,
    border:true,
    title: "<div wtf:qtip='"+WtfGlobal.getLocaleText('pm.Help.widHeader')+"'>"+WtfGlobal.getLocaleText('pm.project.health.taskwiseprojecthealth')+"</div>"
});
panelArr.push({
    config1:[{
        url : Wtf.req.widget + 'widget.jsp',
        numRecs:0,
        template:new Wtf.XTemplate("<tpl><div>{update}</div></tpl>"),
        isPaging: false,
        emptyText:WtfGlobal.getLocaleText('pm.common.noupdate'),
        isSearch: false,
        headerHtml : getQuickLinksHeaderHtml(),
        linkcode : 1,
        paramsObj: {
            flag:1,
            searchField:'name'
        }
    }],
    id : "quicklinks_drag",
    draggable:true,
    isProject: false,
    border:true,
    title: "<div wtf:qtip='"+WtfGlobal.getLocaleText('pm.Help.widHeader')+"' style='font-size:12px !important;'>"+WtfGlobal.getLocaleText('pm.quicklinks.text')+"</div>",
    tools: ''//getCloseTool()
});

panelArr.push({
    config1:[{
        url : Wtf.req.widget + 'widget.jsp',
        numRecs:5,
        template:new Wtf.XTemplate("<tpl><div style='margin-left:2%;margin-top:10px;'>{update}</div></tpl>"),
        isPaging: false,
        emptyText:WtfGlobal.getLocaleText('pm.common.noupdate'),
        isSearch: false,
        headerHtml : '',
        linkcode : 2,
        paramsObj: {
            flag:2,
            searchField:'name'
        }
    }],
    draggable:true,
    border:true,
    isProject: true,
    title: "<div wtf:qtip='"+WtfGlobal.getLocaleText('pm.Help.widHeader')+"'>"+WtfGlobal.getLocaleText('lang.requests.text')+"</div>",
    id : "requests_drag",
    tools: ''//getCloseTool()
});

panelArr.push({
    config1:[{
        url : Wtf.req.widget + 'widget.jsp',
        numRecs:5,
        template:new Wtf.XTemplate("<tpl><div style='margin-left:2%;margin-top:10px;'>{update}</div></tpl>"),
        isPaging: false,
        emptyText:WtfGlobal.getLocaleText('pm.common.noupdate'),
        isSearch: false,
        headerHtml : '',
        linkcode : 3,
        paramsObj: {
            flag:3,
            searchField:'name'
        }
    }
    ],
    draggable:true,
    border:true,
    isProject: true,
    title: "<div wtf:qtip='"+WtfGlobal.getLocaleText('pm.Help.widHeader')+"'>"+WtfGlobal.getLocaleText('pm.dashboard.widget.announcements.text')+"</div>",
    id : "announcements_drag",
    tools: ''//getCloseTool()
});
if(Wtf.subscription.pm.subscription && Wtf.featuresView.pm){
    panelArr.push({
        config1:[{
            url : Wtf.req.widget + 'widget.jsp',
            numRecs:5,
            template:new Wtf.XTemplate("<tpl><div class='workspace listpanelcontent'>"+
                "<div>{update}</div>" +
                "</div></tpl>"),
            isPaging: true,
            emptyText:WtfGlobal.getLocaleText('pm.personalmessage.nomessage'),
            isSearch: false,
            headerHtml : '',
            linkcode : 2,
            paramsObj: {
                flag:5,
                searchField:'name'
            }
        }],
        draggable:true,
        border:true,
        isProject: true,
        title: "<div wtf:qtip='"+WtfGlobal.getLocaleText('pm.Help.widHeader')+"'>"+WtfGlobal.getLocaleText('pm.personalmessages.text')+" - "+WtfGlobal.getLocaleText('pm.personalmessages.inbox')+"</div>",
        id : "pm_drag",
        tools: ''//getCloseTool()
    });
}
Wtf.Panel.prototype.afterRender = Wtf.Panel.prototype.afterRender.createInterceptor(function() {// Fix For IE
    if(this.autoScroll) {
        this.body.dom.style.position = 'relative';
    }
});

var widgetIdArray=[];
if(welcome == false){
    for(pc=0; pc<projects.totalCount; pc++){
        widgetIdArray.push(projId[pc]+'_drag');
    }
    widgetIdArray.push("chart_drag");
} else
    widgetIdArray.push("welcome_drag");
if(Wtf.subscription.docs.subscription && Wtf.featuresView.docs)
    widgetIdArray.push("mydocs_drag");
widgetIdArray.push("taskwiseprojecthealth_drag");
widgetIdArray.push("quicklinks_drag");
widgetIdArray.push("requests_drag");
widgetIdArray.push("announcements_drag");
if(Wtf.subscription.pm.subscription && Wtf.featuresView.pm)
    widgetIdArray.push("pm_drag");

var isToAppendArray=new Array();
for(var i=0;i<widgetIdArray.length;i++){
    isToAppendArray[i]=0;
}

/************initially all the widgets added to dashboard************/
showMainLoadMask(WtfGlobal.getLocaleText('pm.loading.text')+" "+WtfGlobal.getLocaleText('pm.dashboard.widgets.text')+"...");
if(welcome == false){
    Wtf.Ajax.requestEx({
        url:Wtf.req.widget + 'widget.jsp',
        params:{
            flag:100,
            welcome: welcome,
            projCount: projects.totalCount
        }
    }, this, function(res){
        var obj = eval('('+res+')');
        var index=0, tempid = "", tempObj;
        if(Wtf.subscription.proj.subscription && Wtf.featuresView.proj){
            if(obj.customwidget)
                this.getCustomReportWidget();
            else
                this.getMilestonePanel();
        }
        if(obj.helpflag == false)
            renderHelpDiv();
        for(var i=0;i<obj.col1.length;i++){
            index=widgetIdArray.indexOf(obj.col1[i].id);
            isToAppendArray[index]=1;
            if(panelArr[index]!==undefined){
                tempid = widgetIdArray[index];
                tempObj = eval('('+obj[tempid]+')')
                Wtf.getCmp('portal_container_box1').add(createNewPanel(panelArr[index], tempObj));
            }
            Wtf.getCmp('portal_container').doLayout();
        }
        for(i=0;i<obj.col2.length;i++){
            index=widgetIdArray.indexOf(obj.col2[i].id);
            isToAppendArray[index]=1;
            if(panelArr[index]!==undefined){
                tempid = widgetIdArray[index];
                tempObj = eval('('+obj[tempid]+')')
                Wtf.getCmp('portal_container_box2').add(createNewPanel(panelArr[index], tempObj));
            }
            Wtf.getCmp('portal_container').doLayout();
        }
        for(i=0;i<obj.col3.length;i++){
            index=widgetIdArray.indexOf(obj.col3[i].id);
            isToAppendArray[index]=1;
            if(panelArr[index]!==undefined){
                tempid = widgetIdArray[index];
                tempObj = eval('('+obj[tempid]+')')
                Wtf.getCmp('portal_container_box3').add(createNewPanel(panelArr[index], tempObj));
            }
            Wtf.getCmp('portal_container').doLayout();
        }
        setTimeout(function(){
            hideMainLoadMask();
        },300);
    }, function(){
        hideMainLoadMask();
    });

    var paneltop = new Wtf.Panel({
        border: false,
        layout: 'border',
        id: "portletContainer",
        frame: false,
        items: [{
            region: 'center',
            layout: 'column',
            id:'portal_container',
            xtype:'portal',
            bodyStyle: "background:#FFFFFF;",
            border: false,
            items: [{
                columnWidth: .328,
                cls: 'portletcls',
                id: 'portal_container_box1',
                border: false
            }, {
                columnWidth: .328,
                border: false,
                cls: 'portletcls',
                id: 'portal_container_box2'
            }, {
                columnWidth: .328,
                cls: 'portletcls',
                id: 'portal_container_box3',
                border: false
            }]
        }]
    });
} else {
    Wtf.Ajax.requestEx({
        url:Wtf.req.widget + 'widget.jsp',
        params:{
            flag:100,
            welcome: welcome,
            projCount: projects.totalCount
        }
    }, this, function(res){
        var obj = eval('('+res+')');
        var index=0, tempid = '', tempObj;
        if(obj.helpflag == false)
            renderHelpDiv();
        for(var i=0;i<obj.col1.length;i++){
            index=widgetIdArray.indexOf(obj.col1[i].id);
            isToAppendArray[index]=1;
            if(panelArr[index]!==undefined){
                tempid = widgetIdArray[index];
                tempObj = eval('('+obj[tempid]+')')
                Wtf.getCmp('portal_container_box1').add(createNewPanel(panelArr[index], tempObj));
            }
            Wtf.getCmp('portal_container').doLayout();
        }
        for(i=0;i<obj.col2.length;i++){
            index=widgetIdArray.indexOf(obj.col2[i].id);
            isToAppendArray[index]=1;
            if(panelArr[index]!==undefined){
                tempid = widgetIdArray[index];
                tempObj = eval('('+obj[tempid]+')')
                Wtf.getCmp('portal_container_box2').add(createNewPanel(panelArr[index], tempObj));
            }
            Wtf.getCmp('portal_container').doLayout();
        }
        setTimeout(function(){
            hideMainLoadMask()
        },300);
    }, function(){
        hideMainLoadMask();
    });

    var paneltop = new Wtf.Panel({
        border: false,
        layout: 'border',
        id: "portletContainer",
        frame: false,
        items: [{
            region: 'center',
            layout: 'column',
            id:'portal_container',
            xtype:'portal',
            bodyStyle: "background:#FFFFFF;",
            border: false,
            items: [{
                columnWidth: .65,
                cls: 'portletcls',
                id: 'portal_container_box1',
                border: false
            },{
                columnWidth: .33,
                border: false,
                cls: 'portletcls',
                id: 'portal_container_box2'
            }]
        }]
    });
}

isPermalink = checkPermalink();

Wtf.getCmp('portal_container').on('drop',function(e){
    Wtf.Ajax.requestEx({
        url : Wtf.req.widget + 'widget.jsp',
        params:{
            flag:-100,
            colno:e.columnIndex+1,
            position:e.position,
            wid:e.panel.id,
            welcome: welcome
        }
    }, this, function(){
        Wtf.getCmp('portal_container').doLayout();
    }, function(){});
},this);

Wtf.getCmp("tabdashboard").add(paneltop);
Wtf.getCmp("tabdashboard").doLayout();

if(!welcome){
    loadPermalink();
}

// Remove this code after 1/10/2012.  Application may not need this request.-----------------------------------------------

if(creator){
    Wtf.Ajax.requestEx({
        url: '../../ProjectController.jsp',
        params: {
            cm: 'checkForNonModeratorProjects'
        }
    }, this,
    function(resp, req){
        var obj = eval('(' + resp + ')');
        if(obj){
            if(obj.data){
                var userData = new Object();
                for(var i=0; i<obj.data.length; i++){
                    var temp = eval('(' + obj.data[i].memberdata + ')');
                    temp = temp.data;
                    obj.data[i].memberdata = null;
                    userData[obj.data[i].projectID] = temp;
                }
                if(obj.data.length != 0)
                    new Wtf.AssignModeratorWindow({
                        dataObj: obj.data,
                        userData: userData
                    }).show();
            }
        }
    },
    function(resp, req){
        msgBoxShow(4, 1);
    });
}

// -----------------------------------------------------------------------------------

function takeTour(){
    showHelp();
}

function loadPermalink(pos){
    if(pos == undefined){
        if(isPermalink >= 0){
            mainPanel.loadTab("../../project.html", "   "+projects[isPermalink].id,projects[isPermalink].name, "navareadashboard",Wtf.etype.proj,true);
            isPermalink = -1;
        }
    } else {
        isPermalink = pos;
        mainPanel.loadTab("../../project.html", "   "+projects[isPermalink].id,projects[isPermalink].name, "navareadashboard",Wtf.etype.proj,true);
        isPermalink = -1;
    }
}

function saveHelpState(){
    var chek = Wtf.get('showHelpCheck');
    if(chek && chek.dom.checked){
        Wtf.Ajax.requestEx({
            url : Wtf.req.widget + 'widget.jsp',
            params:{
                flag:-101
            }
        }, this, function(){
        }, function(){});
    }
    noThanks();
}

function noThanks(){
    if(Wtf.get('dashhelp')){
        Wtf.get('dashhelp').slideOut('t',{
            remove: true
        });
    }
}
function renderHelpDiv(){
    var txt = "<div class='outerHelp' id='dashhelp'>" +
        "<div style='float:left; padding-left:1%; margin-top:-1px;'><img src='../../images/alerticon.jpg'/></div>" +
        "<div class='helpHeader'>"+WtfGlobal.getLocaleText('pm.dashboard.help.needhelp')+"</div><div class='helpContent' id='wtf-gen285'>"+
        "<div style='padding-top: 5px; float: left;'><a href='#' class='helplinks guideme' onclick='loadHelp()'>"+WtfGlobal.getLocaleText('pm.dashboard.help.pleaseguideme')+"</a>"+
        "  <a class='helplinks nothanks' href='#' onclick='saveHelpState()'>"+WtfGlobal.getLocaleText('pm.dashboard.help.nothanks')+"</a></div>";
    txt += (!welcome) ? "<div style='float:right; margin-right:10px;'><div class='checkboxtext'>"+
        "<input type='checkbox' id='showHelpCheck' style='margin-right: 5px;'/><span style='margin-right:1%; color:#15428B;'>"+WtfGlobal.getLocaleText('pm.dashboard.help.donotshow')+"</span></div>"+
        "<span style='color:#333333; cursor:pointer; margin-top:1px; padding-top:5px; float:right;' id='closehelp' onclick='saveHelpState()'><img style='height:12px; width:12px;'src='../../images/cancel16.png' align='bottom'/></span></div></div></div>" : "";
    Wtf.DomHelper.insertFirst(Wtf.getCmp('portal_container').body, txt);
}
function requestForWidgetRemove(wid) {
    Wtf.Ajax.requestEx({
        url : Wtf.req.widget + 'widget.jsp',
        params:{
            flag:-99,
            wid:wid
        }
    }, this, function(){
        }, function(){});
}
function quickAdd(id, seq){
    Wtf.get('updatetable'+id).dom.style.display = 'none';
    Wtf.get('addrec'+id).dom.style.display = 'block';
    var wid = Wtf.getCmp(id);
    var flag = wid.press;
    wid.getWorkSpaceLinks(flag, seq);
    if(flag == 0 || flag == 1 || flag == 2)
        focusOn(Wtf.getCmp('mainForm'+id).form.items.items[0].id);
}

function handleFlagChange(postid, index, src){
    var flag = "";
    src = Wtf.get('pmFlag'+index).dom.src.toString();
    if (src.indexOf("FlagGrey.gif") != -1) {
        flag = true;
    } else {
        flag = false;
    }
    var jsonData = "{data:[";
    jsonData += "{'post_id':'" + encodeURIComponent(postid) + "'}";
    jsonData += "]}";
    Wtf.Ajax.requestEx({
        method: 'GET',
        url: Wtf.req.prt + 'getmail.jsp',
        params: {
            mailflag: 'starchange',
            post_id: jsonData,
            flag: flag
        }
    }, this,
    function(result, req){
        var obj = eval('('+result+')');
        if(obj.data[0].flag == true){
            Wtf.get('pmFlag'+index).dom.src = '../../images/FlagRed.gif';
        } else {
            Wtf.get('pmFlag'+index).dom.src = '../../images/FlagGrey.gif';
        }
    },
    function(result, req){
    });
}

function getProjectWindow(){
    var win = Wtf.getCmp('selectProjectWin');
    if(!win){
        win = new Wtf.selectProject();
        win.show();
    }

}

function getCreateProjectWindow(){
    if(!Wtf.getCmp('createProjectWin')){
        var createprojectWindow = new Wtf.createProject({
            mode: 0,
            successHandler: createProjectSuccess.createDelegate(this),
            failureHandler: createProjectFailure.createDelegate(this),
            data: {}
        });
        createprojectWindow.on("close", this.createProjectWindowClose, this);
        createprojectWindow.cancelBtn.on('click', this.createProjectWindowClose, this);
        createprojectWindow.show();
    }
    focusOn("projectname");
}

function createProjectWindowClose(){
    var win = Wtf.getCmp('createProjectWin');
    if(win)
        win.close();
}

function createProjectFailure(frm, action){
    createProjectWindowClose();
    msgBoxShow(266, 1);
}

function createProjectSuccess(action, frm, res){
    createProjectWindowClose();
    var resobj = eval( "(" + res.response.responseText.trim() + ")" );
    bHasChanged=true;
    if(refreshDash.join().indexOf("all") == -1){
        refreshDash[refreshDash.length] = 'all';
    }
    var open = action.id == 'createOpenProjBtn' ? true : false;
    var projname = frm.findField("projectname").getValue();
    var projid = resobj.data;
    if(open){
         navigate("q",projid,projid,projname,"home");
    }
    Wtf.MessageBox.show({
        title:WtfGlobal.getLocaleText('lang.success.text'),
        msg: WtfGlobal.getLocaleText('pm.msg.succCreateProject'),
        buttons: Wtf.MessageBox.OK,
        icon: Wtf.MessageBox.INFO,
        fn: function(id){
                var dash = Wtf.getCmp('tabdashboard');
                if(!open){
                    refreshDashboard(dash);
                }
            }
    });
}
function handleAcceptReject(id, name, call, type){
    var url, status, alertBox;
    if(call == 'accept'){
        status = 3;
        alertBox = true;
    } else {
        status = 0;
        alertBox = false;
    }
    url = (type == 'project') ? Wtf.req.prf + 'project/setStatusproject.jsp' : Wtf.req.prf + 'user/setUserrelation.jsp';
    changeStatus(url, id, status, name, alertBox, type);
}

function changeStatus(url, statusWith, newStatus, statusWithName, alertBox, type){
    if(type == 'project'){
        var p = {
            userid: loginid,
            comid: statusWith,
            flagLeave : 1,
            status: newStatus
        };
    } else {
        p = {
            userid1: statusWith,
            userid2: loginid,
            relationid: newStatus
        };
    }
    Wtf.Ajax.requestEx({
        url: url,
        params: p
    }, this,
    function(res){
        var obj = eval('('+res+')')
        if(obj.data){
            if(obj.data[0].result){
                bHasChanged=true;
                if(refreshDash.join().indexOf("req") == -1 || refreshDash.join().indexOf("qkl") == -1 || refreshDash.join().indexOf("pmg") == -1){
                    refreshDash[refreshDash.length] = 'req';
                    refreshDash[refreshDash.length] = 'qkl';
                    refreshDash[refreshDash.length] = 'pmg';
                }
                if(alertBox){
                    var msg = (type == 'project') ? [WtfGlobal.getLocaleText('lang.success.text'), WtfGlobal.getLocaleText({key:'pm.dashboard.widget.request.acceptproject',params:statusWithName})]
                    : [WtfGlobal.getLocaleText('lang.success.text'), WtfGlobal.getLocaleText({key:'pm.dashboard.widget.request.acceptconnection',params:statusWithName})];
                    msgBoxShow(msg, 0);

                }
                if(type == 'project')
                    var tabs = ['tab'+statusWith, "tabproject"];
                else
                    tabs = ['tab'+statusWith, 'tabfriends'];
                removeTabsAndRefreshDashoard(tabs, false);
            }
        }
        var dash = Wtf.getCmp('tabdashboard');
        refreshDashboard(dash);
    },
    function(res){
        msgBoxShow(182, 1);
    });
}

function getCustomReportWidget(){
    var html = "<div id='outerdiv' class='msOuter'></div>";

    if(!Wtf.get('milestone_outer')){
        var maindiv = document.createElement('div');
        maindiv.id ="milestone_outer";
        maindiv.style.display = 'block';
        Wtf.DomHelper.insertFirst(Wtf.getCmp('portal_container').body, html);
    } else {
        maindiv = document.getElementById('milestone_outer');
        maindiv.innerHTML = "";
    }

    var msbody = document.createElement('div');
    msbody.id = 'ms_body_outer';
    msbody.className = 'msBodyOuter';

    maindiv.appendChild(msbody);

    msbody = document.createElement('div');
    msbody.id = 'ms_body_inner';
    msbody.className = 'msBodyInner';
    msbody.style.height = '40px';

    maindiv.firstChild.appendChild(msbody);

    var mileStonePanel = Wtf.getCmp('milestoneTimelinePanel');
    if(mileStonePanel)
        mileStonePanel.destroy();
    mileStonePanel = new Wtf.Panel({
        border: true,
        layout: 'fit',
        id: 'milestoneTimelinePanel',
        style: 'width:100%; margin-bottom:-1px;',
        title: WtfGlobal.getLocaleText("pm.admin.project.customreport"),
        renderTo: 'outerdiv',
        tools: [{
            id: 'w_reports_dim',
            scope: this,
            qtip: WtfGlobal.getLocaleText("pm.dashboard.widget.custom.selectreport"),
            iconCls: 'dpwnd quickaddprowid',
            handler: showReportsWindow
        },{
            id: 'w_switchWidget',
            scope: this,
            qtip: WtfGlobal.getLocaleText("pm.dashboard.widget.custom.switch"),
            iconCls: 'dpwnd quickaddprowid',
            handler: function(b){
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("pm.msg.INFORMATION"),
                    msg: WtfGlobal.getLocaleText("pm.dashboard.widget.custom.prompt"),
                    buttons: Wtf.MessageBox.OKCANCEL,
                    icon: Wtf.MessageBox.INFO,
                    fn: function(btn){
                        if(btn == 'ok'){
                            Wtf.Ajax.requestEx({
                                url: Wtf.req.widget + 'widget.jsp',
                                params: {
                                    flag: 107,
                                    customwidget: false
                                }
                            }, this, function(){
                                bHasChanged=true;
                                if(refreshDash.join().indexOf("all") == -1){
                                    refreshDash[refreshDash.length] = 'all';
                                }
                                var dash = Wtf.getCmp('tabdashboard');
                                refreshDashboard(dash);
                            }, Wtf.emptyFn);
                        }
                    }
                });
                return;
            }
        }]
    });
    mileStonePanel.body.dom.appendChild(maindiv);
    getSelectedReport(msbody);
}

function showReportsWindow(){
    var reportWin =new Wtf.ReportListWin({
        width:450,
        autoHeight:true,
        fromDashboard: true
    });
    reportWin.on('onrunreport', runReport, this);
    reportWin.show();
}

function runReport(win, rec, btn){
    if(btn.id == 'set'){
        var rep = getReportPanel(rec, {
            id: 'dashboard',
            searchable: false,
            exportable: false,
            showFilters: true,
            customPageSize: 4,
            showTitle: false
        });
        if(rep){
            rep.advFilter.on('beforeshow', function(){
                mtp.setHeight(450);
                return true;
            }, this);
            rep.advFilter.on('beforehide', function(){
                mtp.setHeight(300);
                return true;
            }, this);
            if(win)
                win.close();
            var mtp = Wtf.getCmp("milestoneTimelinePanel");
            if(mtp.items.items[0])
                mtp.remove(mtp.items.items[0].id, true);
            else
                mtp.body.dom.innerHTML = "";
            mtp.setTitle(rec.data.reportName);
            mtp.add(rep);
            mtp.setHeight(300);
            mtp.doLayout();
            if(win){
                Wtf.Ajax.requestEx({
                    url: '../../reports.jsp',
                    params: {
                        m: 'setSelectedReport',
                        reportid: rec.data.reportID
                    }
                }, this, Wtf.emptyFn, Wtf.emptyFn);
            }
        } else {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("pm.msg.ERROR"),
                msg: WtfGlobal.getLocaleText("pm.dashboard.widget.custom.alreadyactive"),
                buttons: Wtf.MessageBox.OK,
                icon: Wtf.MessageBox.ERROR
            });
            return;
        }
    } else {
        rep = getReportPanel(rec, {
            id: 'admin',
            searchable: true,
            exportable: true,
            showFilters: true,
            customPageSize: 15,
            showTitle: true
        });
        win.close();
        mainPanel.add(rep);
        mainPanel.doLayout();
        mainPanel.setActiveTab(rep);
    }
}

function addCustomReports(){

    var reportBuilder = Wtf.getCmp("tabreportbuilder");
    if(!reportBuilder){
        reportBuilder = new Wtf.cc.CustomReportPanel({
            title: WtfGlobal.getLocaleText('pm.admin.project.customrep.title'),
            closable: true,
            closeAction:'close',
            layout: 'border',
            autoDestroy:true,
            id: "tabreportbuilder"
        })
        mainPanel.add(reportBuilder);
    }
    mainPanel.setActiveTab(reportBuilder);
}

function getSelectedReport(msbody){

    Wtf.Ajax.requestEx({
        url: '../../reports.jsp',
        params: {
            m: 'getSelectedReportConfig'
        }
    }, this, function(result, req){
        var obj = Wtf.decode(result);
        if(obj.success){
            obj.data = Wtf.decode(obj.data);
            var btn = {};
            btn.id = 'set';
            runReport(null, obj, btn);
        } else {
            msbody.innerHTML = '<div style="padding-top:10px;"><center><span style="color:gray;">'+WtfGlobal.getLocaleText("pm.dashboard.widget.custom.empty1")+' </span><span class="createProjectcss"><a href="#" onclick=\"showReportsWindow()\" class="mailTo">'+WtfGlobal.getLocaleText("pm.dashboard.widget.custom.empty2")+'</a></span></center></div>';
        }
    }, function(result, req){
        msbody.innerHTML = '<div style="padding-top:10px;"><center><span style="color:gray;">'+WtfGlobal.getLocaleText("pm.dashboard.widget.custom.empty3")+' </span><span class="createProjectcss"><a href="#" onclick=\"showReportsWindow()\" class="mailTo">'+WtfGlobal.getLocaleText("pm.dashboard.widget.custom.empty4")+'</a></span></center></div>';
    });
}


function getMilestonePanel(){
    var proj = [], projNames = [];
    for(var i = 0; i < projects.totalCount; i++){
        if(projects[i].milestoneStack){
            proj[proj.length] = projects[i].id;
            projNames[projNames.length] = projects[i].name;
        }
    }
    var html = "<div id='outerdiv' class='msOuter'></div>";

    if(!Wtf.get('milestone_outer')){
        var maindiv = document.createElement('div');
        maindiv.id ="milestone_outer";
        maindiv.style.display = 'block';
        Wtf.DomHelper.insertFirst(Wtf.getCmp('portal_container').body, html);
    } else {
        maindiv = document.getElementById('milestone_outer');
        maindiv.innerHTML = "";
    }

    var msbody = document.createElement('div');
    msbody.id = 'ms_body_outer';
    msbody.className = 'msBodyOuter';

    maindiv.appendChild(msbody);

    msbody = document.createElement('div');
    msbody.id = 'ms_body_inner';
    msbody.className = 'msBodyInner';
    msbody.style.height = (((proj.length != 0) ? proj.length : 0.5) * 80)+'px';

    maindiv.firstChild.appendChild(msbody);

    var mileStonePanel = Wtf.getCmp('milestoneTimelinePanel');
    if(mileStonePanel)
        mileStonePanel.destroy();
    mileStonePanel = new Wtf.Panel({
        border: true,
        layout: 'fit',
        id: 'milestoneTimelinePanel',
        style: 'width:100%; margin-bottom:-1px;',
        title: WtfGlobal.getLocaleText("pm.milestonetimeline.text"),
        renderTo: 'outerdiv',
        tools: [{
            id: 'w_switchWidget',
            scope: this,
            qtip: WtfGlobal.getLocaleText("pm.dashboard.widget.custom.switchtoreport"),
            iconCls: 'dpwnd quickaddprowid',
            handler: function(b){
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("pm.msg.INFORMATION"),
                    msg: WtfGlobal.getLocaleText("pm.dashboard.widget.custom.prompt"),
                    buttons: Wtf.MessageBox.OKCANCEL,
                    icon: Wtf.MessageBox.INFO,
                    fn: function(btn){
                        if(btn == 'ok'){
                            Wtf.Ajax.requestEx({
                                url: Wtf.req.widget + 'widget.jsp',
                                params: {
                                    flag: 107,
                                    customwidget: true
                                }
                            }, this, function(){
                                bHasChanged=true;
                                if(refreshDash.join().indexOf("all") == -1){
                                    refreshDash[refreshDash.length] = 'all';
                                }
                                var dash = Wtf.getCmp('tabdashboard');
                                refreshDashboard(dash);
                            }, Wtf.emptyFn);
                        }
                    }
                });
                return;
            }
        }]
    });
    mileStonePanel.body.dom.appendChild(maindiv);
    if(proj.length != 0)
        getMilestonesData(proj, projNames);
    else {
        msbody.innerHTML = '<div style="padding-top:10px;"><center><span style="color:gray;">'+WtfGlobal.getLocaleText('pm.project.milestone.emptytext')+' </span><span class="createProjectcss"><a href="#" onclick=\"getProjectWindow()\" class="mailTo">'+WtfGlobal.getLocaleText('pm.project.milestone.eptytext1')+'</a></span></center></div>';
    }
}

function getMilestonesData(proj, projNames) {
    Wtf.Ajax.requestEx({
        url: Wtf.req.widget + 'widget.jsp',
        params: {
            flag: 103,
            welcome: welcome,
            projList: proj.join()
        }
    }, this, function(res){
        var obj = eval('('+res+')')
        if(obj.milestonedata){
            obj = obj.milestonedata;
            var projMT = obj.data;
            for(var i = 0; i < proj.length; i++){
                showMilestoneTimeline(proj[i], projNames[i], projMT);
            }
        }
    }, function(){

    });
}

function showMilestoneTimeline(proj, projName, projMT) {

    var stackdetails = projMT[proj].stackdetails

    var actLength = 0, sm = 0, em = 0, leftDivLength = 0, rightDivLength = 0, ongoing = null;
    if(projMT[proj].startmarker)
        sm = projMT[proj].startmarker;
    if(projMT[proj].endmarker)
        em = projMT[proj].endmarker;

    if(sm == 0 && em == 0)
        ongoing = projMT[proj].ongoing;

    actLength = em - sm;
    if(em == 0 && sm != 0)
        actLength = stackdetails.length - sm;
    leftDivLength = sm;
    rightDivLength = stackdetails.length - (leftDivLength + actLength);

    var table = document.createElement("table");
    table.className = "msTable";
    var tbody = document.createElement("tbody");
    var tr = document.createElement("tr");
    tbody.appendChild(tr);
    table.appendChild(tbody);
    var td = document.createElement("td");

    var innerdiv = document.createElement('div');
    innerdiv.id = proj+'_milestone_bar';
    innerdiv.style.width = stackdetails.length +'px';
    innerdiv.style.height = '12px';

    var dt = WtfGlobal.dateRendererForOnlyDate(stackdetails.stackStartDate);

    var startdiv = document.createElement('div');
    startdiv.id = proj+'_milestone_start';
    startdiv.className = 'msDates';
    startdiv.innerHTML = '<b>'+WtfGlobal.getLocaleText('lang.start.text')+'</b><br>' + dt;

    td = document.createElement("td");
    if(Wtf.isIE){
        td.setAttribute("class", 'msTD');
        td.style.verticalAlign = 'top';
    }
    td.appendChild(startdiv);
    tr.appendChild(td);

    var maintd = document.createElement("td");
    maintd.setAttribute("width", stackdetails.length);
    maintd.appendChild(innerdiv);
    tr.appendChild(maintd);

    var top = 3, topValue = (Wtf.isIE) ? (Wtf.isIE8() ? -9 : 6) : -13;
    if(leftDivLength != 0){
        var leftdiv = document.createElement('div');
        leftdiv.style.width = leftDivLength +'px';
        leftdiv.id = proj+'_left';
        leftdiv.className = 'msBar msBarRight';
        innerdiv.appendChild(leftdiv);
        top = topValue;
    }
    if(false){
//    if(em != 0 || (leftDivLength != 0 || rightDivLength != 0)){
        leftdiv = document.createElement('div');
        leftdiv.style.width = actLength +'px';
        leftdiv.className = 'msBar';
        leftdiv.id = proj+'_center';
        innerdiv.appendChild(leftdiv);
    }
    if(em != 0 && em != stackdetails.length){
        leftdiv = document.createElement('div');
        leftdiv.style.width = rightDivLength +'px';
        leftdiv.className = 'msBar msBarRight';
        leftdiv.id = proj+'_right';
        innerdiv.appendChild(leftdiv);
        top = topValue;
    }
    else if(ongoing != null && !ongoing){
        innerdiv.className = 'msBar msBarRight';
    }
    else if(leftDivLength == 0 && rightDivLength == stackdetails.length){
        innerdiv.className = 'msBar';
    }

    dt = WtfGlobal.dateRendererForOnlyDate(stackdetails.stackFinishDate);

    startdiv = document.createElement('div');
    startdiv.id = proj+'_milestone_end';
    startdiv.className = 'msDates onright';
    startdiv.innerHTML = '<b>'+WtfGlobal.getLocaleText('lang.end.text')+'</b><br>' + dt;

    td = document.createElement("td");
    if(Wtf.isIE){
        td.setAttribute("class", 'msTD');
        td.style.verticalAlign = 'top';
    }
    td.appendChild(startdiv);
    tr.appendChild(td);

    var msDiv = document.createElement("div");
    msDiv.id = proj + '_mt';
    var pC = document.createElement("center");
    var projNameDiv = document.createElement("div");
    projNameDiv.style.paddingTop = '1%';
    var projLink = '<span class="createProjectcss"><a href="#" onclick="navigate(\'q\', \''+proj+'\', \''+proj+'\', \''+projName+'\', \'home\');">'+projName+'</a></span>';
    pC.innerHTML =WtfGlobal.getLocaleText('pm.project.milestone.for')+' '+ projLink;
    projNameDiv.appendChild(pC);

    msDiv.appendChild(projNameDiv);
    msDiv.appendChild(table);
    document.getElementById('ms_body_inner').appendChild(msDiv);

    var x, d;
    var tpl = new Wtf.XTemplate('<tpl for="items"><span class="qTipHeader">{h}: </span><span class="{c}">{d}</span><br></tpl>');
    var obj = projMT[proj].milestones;
    var len = obj.length;
    var cnt = 0, posArr = new Object(), posObj = [];
    for (var i = 0; i < len; i++){
        var milestone = obj[i];
        var position = milestone.position;
        var name = milestone.name;
        var index = milestone.index;
        var pc = milestone.progress;
        dt = milestone.startdate;
        dt = WtfGlobal.dateRendererForOnlyDate(dt);
        d = {
            items:[{
                h: WtfGlobal.getLocaleText('pm.project.plan.task.attributes.index'),
                c: 'tName',
                d: index
            },{
                h: WtfGlobal.getLocaleText('lang.date.text'),
                c: 'tSDate',
                d: dt
            },{
                h: WtfGlobal.getLocaleText('pm.project.plan.task.attributes.progress'),
                c: 'tDuration',
                d: pc + "%"
            }]};
        if(position){
            var tip = '<span class="taskheader msTipName">'+name+'</span><hr>'+tpl.apply(d);
            if(!Wtf.get(proj+'_ms_'+position)){
                posObj[0] = position;
                posObj[1] = 1;
                var milestoneImg = document.createElement("img");
                milestoneImg.className = 'msMilestoneImage' + milestone.image;
                milestoneImg.src = Wtf.BLANK_IMAGE_URL;
                milestoneImg.id = proj+'_ms_'+position;
                x = position - (cnt * 14);
                document.getElementById(proj+"_milestone_bar").appendChild(milestoneImg);
                x = x - 7;
//                if(em != 0 && ((WtfGlobal.dateRendererForOnlyDate(projMT[proj].end) === dt))){
//                    x = x - 4;
//                }
                milestoneImg.style.left = x+'px';
                milestoneImg.style.top = top+'px';
                milestoneImg.setAttribute("wtf:qtip", tip);
                var ms = Wtf.get(proj+'_ms_'+position);
                if(ms)
                    ms.addListener('click', showMilestonesDetailsPanel, this, {name: name});
                cnt++;
            } else {// milestone already exists
                posObj[1]++;
                milestoneImg = document.getElementById(proj+'_ms_'+position);
                var tipHTML = milestoneImg.getAttribute("wtf:qtip");
                tipHTML += '<hr>' + tip;
                milestoneImg.setAttribute("wtf:qtip", tipHTML);
            }
        }
        posArr[cnt] = Wtf.util.clone(posObj);
    }

//    var total = 0;
//    for(var c = 1; c < (cnt+1); c++){
//        var pos = posArr[c][0];
//        milestoneImg = Wtf.get(proj+'_ms_'+pos);
//        if(milestoneImg){
//            var oldLeft = parseInt(milestoneImg.dom.style.left);
//            milestoneImg.dom.style.left = (oldLeft - (14 * (c-1))) + 'px';
//        }
//    }

    var markers = stackdetails.monthmarkers;
    var markersdiv = document.createElement("div");
    markersdiv.className = 'msMarkersWrapper';
    for (i = 0; i < markers.months_between; i++){
        position = markers[i].position;
        name = markers[i].month;
        if(position <= stackdetails.length){
            var temp = document.createElement("div");
            temp.className = 'msMarkerNew';
            temp.style.left = position + 'px';
            var temp2 = document.createElement("div");
            temp2.innerHTML = name;
            temp2.className = 'msMonthName';
            temp.appendChild(temp2);
            markersdiv.appendChild(temp);
        }
    }
    maintd.appendChild(markersdiv);
}

function showMilestonesDetailsPanel(e, el, obj){
    var tipHTML = el.getAttribute("wtf:qtip");
    if(tipHTML){
        var tasktooltip = "<div style='padding:1px 1px 1px 1px;overflow-x:auto;overflow-y:auto;border:1px solid #8DB2E3 !important;'>"+
        "<div style='background:#E9F2FC;padding:0 5px;overflow-x:auto;overflow-y:auto;max-height:200px;font-size:11px;'>"+tipHTML+
        "</div></div>";
        var tplRow = tasktooltip;
        if(tplRow) {
            var oldContainPane = Wtf.getCmp("containMilestones");
            if(oldContainPane)      // if containpane already present then destroy it and create again as template could not be overwritten
                oldContainPane.destroy();

            var containNotes = new Wtf.Panel({
                id: "containMilestones",
                frame: true,
                hideBorders: true,
                baseCls: "sddsf",
                header: false,
                headerastext: false
            });

            var oldWin1 = Wtf.getCmp("winDetails");
            if(oldWin1)             // if win1 already present then destroy it and create again
                oldWin1.destroy();

            var winWidth = 230;
            var winMaxHeight = 200;

            new Wtf.Window({
                id: "winDetails",
                width: winWidth,
                maxHeight: winMaxHeight,
                plain: true,
                shadow: true,
                header: true,
                closable: true,
                border: false,
                bodyBorder: true,
                frame: true,
                resizable : false,
                title: WtfGlobal.getLocaleText("pm.project.milestone.details"),
                modal: true,
                items: containNotes
            }).show();

            var tplNotes = new Wtf.Template(tplRow);
            tplNotes.compile();
            tplNotes.insertAfter("containMilestones");
            if(Wtf.getCmp('winDetails')){
                Wtf.getCmp('winDetails').setPosition(e.getPageX(), e.getPageY());
                Wtf.getCmp('winDetails').el.dom.style.position = 'fixed';
            }
        }
    }
}
