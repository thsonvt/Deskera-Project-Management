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
function navigate(path, id, tabid, Cname, subtab){
    var eparam = {};
    eparam.id = id;
    Cname = WtfGlobal.URLDecode(decodeURIComponent(Cname));
    switch (path) {
        case 'u':
            mainPanel.loadTab('../../user.html', "tab" + id, Cname, "navareadashboard", Wtf.etype.user, true);
            break;
        case 'p':
            eparam.event = "projectplanclicked";
            mainPanel.loadTab('../../project.html', "tab" + id, Cname, "navareadashboard", Wtf.etype.proj, true, eparam);
            break;
        /*case 'c':
            mainPanel.loadTab('../../community.html', "tab" + id, Cname, "navareadashboard", Wtf.etype.comm, true);
            break;*/
        case 'x':
            eparam.event = "teamcalclicked";
            mainPanel.loadTab('../../project.html', "tab" + tabid, Cname, "navareadashboard", Wtf.etype.proj, true, eparam);
            break;
        /*case 'm':
            eparam.event = "shareddocclicked";
            mainPanel.loadTab('../../project.html', "tab" + id, Cname, "navareadashboard", Wtf.etype.proj, true, eparam);
            break;
        case 'n':
            eparam.event = "shareddocclicked";
            mainPanel.loadTab('../../community.html', "tab" + id, Cname, "navareadashboard", Wtf.etype.comm, true, eparam);
            break;
        case 'd':
            mainPanel.loadTab('../../documents.html', "tabdocs", (Cname ? Cname : "My Documents"), "navareadocs", Wtf.etype.docs, false);
            break;*/
        case 'q':
            if(Wtf.subscription.cal.subscription && !calLoad && Wtf.featuresView.cal) {
                WtfGlobal.loadScript("../../scripts/minified/calendar.js?v=3");
                calLoad = true;
            }
            mainPanel.loadTab('../../project.html', "tab" + id, Cname, "navareadashboard", Wtf.etype.proj, true, subtab);
            break;
        case 'a':
            eparam.event = "adminpageclicked";
            mainPanel.loadTab('../../project.html', "tab" + id, Cname, "navareadashboard", Wtf.etype.proj, true, eparam);
            break;
        /*case 'o':
            eparam.event = "adminpageclicked";
            mainPanel.loadTab('../../community.html', "tab" + id, Cname, "navareadashboard", Wtf.etype.comm, true, eparam);
            break;*/
        case 't':
            if(Wtf.featuresView.todo) {
                var todoContainer = Wtf.getCmp('list_conainer' + eparam.id + 'calctrl');
                if (!todoContainer) {
                    todoContainer = new Wtf.Panel({
                        title: Cname + ' To-Do',
                        layout: 'fit',
                        id: 'list_conainer' + eparam.id + 'calctrl',
                        closable: true,
                        tabType: Wtf.etype.todo,
                        iconCls: getTabIconCls(Wtf.etype.todo),
                        items: [new Wtf.TodoList({
                            title: WtfGlobal.getLocaleText('pm.common.todolist.text'),
                            id: 'todo_list' + eparam.id + 'calctrl',
                            layout: 'fit',
                            userid: eparam.id,
                            groupType: 2,
                            animate: true,
                            baseCls: 'todoPanel',
                            enableDD: true,
                            containerScroll: true,
                            border: false,
                            rootVisible: false
                        })]
                    });

                    Wtf.getCmp("as").add(todoContainer);
                    Wtf.getCmp("as").doLayout();
                }
                Wtf.getCmp("as").setActiveTab(todoContainer);
            }
            break;
        case 'y':
            var calTree = Wtf.getCmp(mainPanel.id + "Calendar");
            if (calTree) {
                calTree.selectChanged();
            }
            else {
                calLoadControl(mainPanel.id);
            }
            if (eparam.id) {
                var tnode = calTree.getNodeById(eparam.id);
                if (tnode) {
                    if (!tnode.ui.checkbox.checked) {
                        tnode.ui.checkbox.checked = true;
                        tnode.attributes.checked = true;
                        calTree.fireEvent("treecheckchange", calTree, tnode, true);
                    }
                }
                else {
                    msgBoxShow(138, 1);
                    //Wtf.Msg.alert('Invalid Calendar', 'Selected calendar doesn\'t exist or has been deleted!');
                }
            }
            break;
        case 'mp':
            mainPanel.loadTab('../../projlist.html', "tabproject", WtfGlobal.getLocaleText('pm.dashboard.widget.quicklinks.myprojects'), "navareaproject", Wtf.etype.proj, false);
            break;
        case 'armp':
            mainPanel.loadTab('../../archiveprojlist.html', "tabarchiveproject", WtfGlobal.getLocaleText('pm.dashboard.widget.quicklinks.archivedprojects'), "navareaproject", Wtf.etype.proj, false);
            break;
        /*case 'mc':
            mainPanel.loadTab('../../commlist.html', "tabcommunity", WtfGlobal.getLocaleText('pm.mycomminity'), "navareacommunity", Wtf.etype.comm, false);
            break;*/
        case 'mn':
            mainPanel.loadTab('../../connections.html', "tabfriends", WtfGlobal.getLocaleText('My.Network'), "navareafriends", Wtf.etype.user, false);
            break;
        case 'pm':
            mainPanel.loadTab('../../pmessage.html', 'navpmessage', WtfGlobal.getLocaleText('pm.personalmessages.text'),'navareamessages',7, false,false,true);
            break;
        case 'con':
            mainPanel.loadTab("../../contacts.html", "   contactpanel", WtfGlobal.getLocaleText('pm.contacts.text'), "navareadashboard", Wtf.etype.contacts, false);
            break;
        case 'docs':
            mainPanel.loadTab('../../documents.html', "tabdocument", WtfGlobal.getLocaleText('pm.dashboard.widget.mydocuments.text'), "navareadocs", Wtf.etype.docs, false);
            break;
        case 'audit':
            mainPanel.loadTab("../../audit.html", "tabaudittrail", WtfGlobal.getLocaleText('pm.audittrail.text'), "navareadashboard", Wtf.etype.log, false,false,true);
            break;
        case 'team':
            teamProjectWizard(id);
            break;
    }
}

/*
Wtf.common.ListPanel = function(config){
    Wtf.common.ListPanel.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.common.ListPanel, Wtf.Panel, {
    setContent: function(text){
        var ctdiv = Wtf.query("div[class=listpanelcontent]", this.contentEl);
        ctdiv[0].innerHTML = text;
    },

    load: function(page, limit){
        this.dataView.store.baseParams = {
            pageno: page,
            pageSize: limit
        };
        this.dataView.store.refresh();
    }
});

Wtf.reg('wtflistpanel', Wtf.common.ListPanel);
*/
/*  WtfTagEditor: Start*/

Wtf.TagWindow = function(config){
    Wtf.apply(this, config);

    this.tagtextbox = new Wtf.form.TextField({
        selectOnFocus:true,
        labelSeparator :'',
        hideLabel:true,
        maxLength: 90,
        width: 273
    });

    this.save = document.createElement('img');
    this.save.src = '../../images/check16.png';
    this.save.title = WtfGlobal.getLocaleText('pm.project.home.savetags');
    this.save.className = 'addtagbutton';
    this.cancel = document.createElement('img');
    this.cancel.src = '../../images/Stop.png';
    this.cancel.title = WtfGlobal.getLocaleText('lang.cancel.text');
    this.cancel.className = 'addtagbutton';
    this.modal=true;
    Wtf.TagWindow.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.TagWindow, Wtf.Window, {
    save2tag: function(){
        this.fireEvent('savetags', WtfGlobal.HTMLStripper(this.tagtextbox.getValue()));
        this.close();
    },

    cancel2tag: function(){
        this.close();
    },

    setTagText: function(text){
        this.tagtextbox.setValue(text);
    },

    onRender: function(config){
        Wtf.TagWindow.superclass.onRender.call(this, config);
        this.add({
            cls: "tagwindow",
            closable: false,
            resizable: false,
            autoHeight: true,
            width: 276,
            border: false,
            layout:'form',
            items: [this.tagtextbox, {
                bodyStyle: 'margin:5px 0 5px 228px; border:none; float:right;',
                layout: "column",
                items: [{
                    baseCls: 'savebutton',
                    contentEl: this.save
                }, {
                    baseCls: 'savebutton',
                    contentEl: this.cancel
                }]
            }, {
                bodyStyle: 'padding-bottom:5px; padding-left:5px;',
                border: false,
                items: this.view20
            }]
        });
        Wtf.get(this.save).addListener("click", this.save2tag, this);
        Wtf.get(this.cancel).addListener("click", this.cancel2tag, this);
    },

    setmytagstore: function(tagarr){
        var record =  Wtf.data.Record.create([{name:'tagname'}]);
        var reader = new Wtf.data.KwlJsonReader({
            root: 'data'
        },record)

        this.view20 = new Wtf.DataView({
            id: "view20" + this.id,
            itemSelector: 'span.mytagspan',
            style: 'overflow:auto',
            multiSelect: true,
            simpleSelect: true,
            selectedClass: 'tagselected',
            overClass: 'taghovered',
            store: new Wtf.data.Store({
                url: Wtf.req.prf + 'common/getmytags.jsp',
                autoLoad: true,
                reader: reader,
                payload: tagarr
            }),
            tpl: new Wtf.XTemplate('<span class="taghdr">'+WtfGlobal.getLocaleText('pm.common.mytags')+':</span>', '<tpl for=".">', '<span class="mytagspan">{tagname}</span>', '</tpl>')
        });
        this.view20.store.on('load', this.showtags, this);
    },

    showtags: function(store, records){
        var curtags = [];
        Wtf.each(store.payload, function(el){
            var i = store.find('tagname', new RegExp('^' + el + '$'));
            if (i > -1) {
                curtags.push(i);
            }
        }, this);
        this.view20.select(curtags);
        this.view20.on('containerclick', function(){
            return false;
        });
        this.view20.on('click', function(dv, i, node, e){
            var t = this.tagtextbox.getValue();
            var tg = node.innerHTML;
            var r = new RegExp('(^|\\s)' + tg + '(\\s|$)');
            if (r.test(t)) {
                t = t.replace(r, ' ').replace(/(\s+)/g, ' ');
            }
            else {
                t += (' ' + tg);
            }
            this.tagtextbox.setValue(t.trim());
        }, this);
    }
});
/*  WtfTagEditor: End*/

/*  WtfAboutView: Start */
Wtf.AboutView = function(config){
    Wtf.apply(this, config);
    Wtf.AboutView.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.AboutView, Wtf.Panel, {
    initComponent: function(){
        Wtf.AboutView.superclass.initComponent.call(this);

        this.imagediv = document.createElement('div');

        this.imagewrapper = document.createElement('div');
        this.image1 = document.createElement('img');
        this.imagewrapper.appendChild(this.image1);

        this.hmeter = document.createElement('div');
        this.hmeter.innerHTML="<div id='charttitle"+this.id+"' class='healthcharttitle'></div><div id='chart"+this.currId+"' class='chartdiv'></div>";


        this.imagediv.appendChild(this.imagewrapper);
        this.imagediv.appendChild(this.hmeter);
        this.imagewrapper.className="imagewrapper";
        if(Wtf.isGecko)
            this.image1.className = "tabimageG";
        else
            this.image1.className = "tabimage";
        this.UserTagurlpath = null;
        this.UserTagfields = null;
        this.view17 = new Wtf.DataView({
            itemSelector: 'div.thumb-wrap',
            style: 'overflow:auto;width:auto;height:auto;margin-left:8px;',
            multiSelect: true,
            loadingText: 'Collecting data...'
        });

        this.tagbutton2 = document.createElement('img');
        this.tagbutton2.src = '../../images/tag_green.gif';
        this.tagbutton2.title = WtfGlobal.getLocaleText('pm.common.edittags');
        this.tagbutton2.className = 'addtagbutton';

        this.view18 = new Wtf.DataView({
            itemSelector: 'a.taga',
            multiSelect: true,
            emptyText: '<span class="tagtitle">'+WtfGlobal.getLocaleText('lang.tags.text')+':</span>',
            loadingText: WtfGlobal.getLocaleText('pm.loading.text'),
            tpl: new Wtf.XTemplate('<span class="tagtitle">'+WtfGlobal.getLocaleText('lang.tags.text')+': </span>', '<tpl for=".">', '<a class="taga" href="" onClick="javascript: invokeTagSearch(this);">{tagname}</a>', '</tpl>')
        });
    },

    getHtml: function(){
        var str = '';
        if(this.WhichAbout == 'project'){
            var index = projId.indexOf(this.currId);
            var isWidget = widgetIdArray.indexOf(this.currId+'_drag');
            if(index !== -1){
                    str = '<div><span style="font-size:2.0em;font-weight:bold;float:left;">'+projects[index].name+'</span>'  //<br clear="all">'
                if(projects[index].isQuickLink)
                    str += '<span id="'+this.currId+'_addQlSpan" class="addToDashLink addDashLinkBg" style="display:none;"><a href="#" onClick=\'addProjectQlOnDash(\"addQlSpan\",\"'+this.currId+'\",\"true\")\' wtf:qtip="'+WtfGlobal.getLocaleText('pm.project.home.addql')+'"> &nbsp</a> </span>'+
                    '<span id="'+this.currId+'_removeQlSpan" class="addToDashLink remDashLinkBg" style="display:block;"><a href="#" onClick=\'addProjectQlOnDash(\"removeQlSpan\",\"'+this.currId+'\",\"false\")\' wtf:qtip="'+WtfGlobal.getLocaleText('pm.project.home.removeql')+'">&nbsp</a> </span>';
                else
                    str += '<span id="'+this.currId+'_addQlSpan" class="addToDashLink addDashLinkBg" style="display:block;"><a href="#" onClick=\'addProjectQlOnDash(\"addQlSpan\",\"'+this.currId+'\",\"true\")\'  wtf:qtip="'+WtfGlobal.getLocaleText('pm.project.home.addql')+'">&nbsp</a> </span>'+
                    '<span id="'+this.currId+'_removeQlSpan" class="addToDashLink remDashLinkBg" style="display:none;"><a href="#" onClick=\'addProjectQlOnDash(\"removeQlSpan\",\"'+this.currId+'\",\"false\")\' wtf:qtip="'+WtfGlobal.getLocaleText('pm.project.home.removeql')+'">&nbsp</a> </span>';
                if(isToAppendArray[index] == 0)
                    str += '<span id="'+this.currId+'_addSpan" class="addToDashLink addDashWidBg" style="display:block;"> <a href="#" onClick=\'addProjectWidgetOnDash(\"addSpan\",\"'+this.currId+'\",\"true\")\' wtf:qtip="'+WtfGlobal.getLocaleText('pm.project.home.addwidget')+'">&nbsp</a> </span>'+
                    '<span id="'+this.currId+'_removeSpan" class="addToDashLink remDashWidBg" style="display:none;"> <a href="#" onClick=\'addProjectWidgetOnDash(\"removeSpan\",\"'+this.currId+'\",\"false\")\' wtf:qtip="'+WtfGlobal.getLocaleText('pm.project.home.removewidget')+'">&nbsp</a> </span>';
                else if(isWidget == -1){
                    str += '<span id="'+this.currId+'_addSpan" class="addToDashLink addDashWidBg" style="display:none;"> <a href="#" onClick=\'addProjectWidgetOnDash(\"addSpan\",\"'+this.currId+'\",\"true\")\' wtf:qtip="'+WtfGlobal.getLocaleText('pm.project.home.addwidget')+'">&nbsp</a> </span>'+
                    '<span id="'+this.currId+'_removeSpan" class="addToDashLink remDashWidBg" style="display:none;"> <a href="#" onClick=\'addProjectWidgetOnDash(\"removeSpan\",\"'+this.currId+'\",\"false\")\' wtf:qtip="'+WtfGlobal.getLocaleText('pm.project.home.removewidget')+'">&nbsp</a> </span>';
                } else {
                    str += '<span id="'+this.currId+'_addSpan" class="addToDashLink addDashWidBg" style="display:none;"> <a href="#" onClick=\'addProjectWidgetOnDash(\"addSpan\",\"'+this.currId+'\",\"true\")\'  wtf:qtip="'+WtfGlobal.getLocaleText('pm.project.home.addwidget')+'">&nbsp</a> </span>'+
                    '<span id="'+this.currId+'_removeSpan" class="addToDashLink remDashWidBg" style="display:block;"> <a href="#" onClick=\'addProjectWidgetOnDash(\"removeSpan\",\"'+this.currId+'\",\"false\")\'  wtf:qtip="'+WtfGlobal.getLocaleText('pm.project.home.removewidget')+'">&nbsp</a> </span>';
                }
                if(projects[index].milestoneStack)
                    str += '<span id="'+this.currId+'_addMtSpan" class="addToDashLink addDashMilesBg" style="display:none;"><a href="#" onClick=\'addProjectMtOnDash(\"addMtSpan\",\"'+this.currId+'\",\"true\")\' wtf:qtip="'+WtfGlobal.getLocaleText('pm.project.home.addmt')+'">&nbsp</a> </span>'+
                    '<span id="'+this.currId+'_removeMtSpan" class="addToDashLink remDashMilesBg" style="display:block;"><a href="#" onClick=\'addProjectMtOnDash(\"removeMtSpan\",\"'+this.currId+'\",\"false\")\' wtf:qtip="'+WtfGlobal.getLocaleText('pm.project.home.removemt')+'">&nbsp</a></span>';
                else
                    str += '<span id="'+this.currId+'_addMtSpan" class="addToDashLink addDashMilesBg" style="display:block;"><a href="#" onClick=\'addProjectMtOnDash(\"addMtSpan\",\"'+this.currId+'\",\"true\")\'  wtf:qtip="'+WtfGlobal.getLocaleText('pm.project.home.addmt')+'">&nbsp</a> </span>'+
                    '<span id="'+this.currId+'_removeMtSpan" class="addToDashLink remDashMilesBg" style="display:none;"><a href="#" onClick=\'addProjectMtOnDash(\"removeMtSpan\",\"'+this.currId+'\",\"false\")\'  wtf:qtip="'+WtfGlobal.getLocaleText('pm.project.home.removemt')+'">&nbsp</a></span>';
                str += '</div>';
            }
        }else {
           str = '<div><span style="font-size:2.0em;font-weight:bold;float:left;">'+Wtf.util.Format.ellipsis(Wtf.getCmp('as').getActiveTab().title, 25)+'</span>'  //<br clear="all">'
        }
        return str;
    },

    onRender: function(config){
        Wtf.AboutView.superclass.onRender.call(this, config);
        Wtf.get(this.tagbutton2).addListener("click", this.showTagWindow, this);
        this.add({
            bodyStyle: "background-color:#FFFFFF; margin:0 0 0 0; padding:0 0 0 0; color:#000000; font-size:12px;",
            border: false,
            layoutConfig: {
                animate: true
            },
            layout: "column",
            items: [{
                columnWidth: 0.35,
                border: false,
                layout: "fit",
                contentEl: this.imagediv
            }, {
                columnWidth: 0.63,
                border: false,
                cls:'homeInfo',
                items: [{
                    bodyStyle: "height:auto; overflow:hidden; margin-left:5px; padding-bottom:5px;",
                    html: this.getHtml(),
                    border: false,
                    id: this.id.substr(0, this.id.indexOf('profileName'))+'projInfo'
                }, this.view17
                ]
            }]
        });
        this.add({bodyStyle: "background-color:#FFFFFF; margin:-8px 0 0 0; padding:0 0 0 0; color:#000000; font-size:12px;",
            border: false,
            layoutConfig: {
                animate: true
            },
            layout: "column",
            items: [{
                bodyStyle: "margin-top:5px;height:auto; overflow:auto;",
                border: false,
                width: '100%',
                items:[
                    {
                    bodyStyle: "height:20px; overflow:hidden; margin-top:5px; padding-top:10px; padding-bottom:6px;",
                    border: false,
                    layout: "column",
                    items: [{
                        width: 18,
                        border: false,
                        contentEl: this.tagbutton2
                    }, {
                        columnWidth: 0.5,
                        border: false,
                        items: this.view18
                    }]
                }]
            }]
        });
        if(this.WhichAbout == 'project'){
            var cnf = {
                config1:[{
                    url : Wtf.req.prj + 'getprojectdetails.jsp',
                    numRecs:4,
                    template:new Wtf.XTemplate(
                        "<tpl><div class='workspace listpanelcontent'>"+
                        "<div>{update}</div>" +
                        "</div></tpl>"),
                    isPaging: true,
                    pagingflag: true,
                    emptyText:WtfGlobal.getLocaleText('pm.common.nonewupdate'),
                    isSearch: false,
                    headerHtml :'',
                    paramsObj: {
                        login: this.currId,
                        updates: true,
                        loginid: loginid,
                        companyid: companyid
                    }
                }],
                draggable: false,
                border: false,
                isProject: true,
                title: WtfGlobal.getLocaleText('pm.project.home.updates.text'),
                id : this.id+'_updatesArea',
                tools: ''
            }
            this.updatesPortlet = createNewPanel(cnf);
            this.add(this.updatesPortlet);
        }
    },

    setImage: function(path){
        this.image1.src = path;
    },

    setHealthMeter: function(connstatus){
        Wtf.Ajax.requestEx({
            url: 'admin.jsp',
            method:'post',
            params : {
                action:0,
                mode:27,
                pid:this.currId
            }
        },
        this,
        function(result, request) {
            var respObj = eval( '('+result+')');
            var _x=respObj.value;
            this.healthValues =respObj;
            document.getElementById("charttitle"+this.id).innerHTML=WtfGlobal.getLocaleText('pm.project.home.health.text');
            var _pt = 0;
            if(respObj.total>0)
                _pt = (respObj.needattention+respObj.overdue)*100/respObj.total;

            var s1="'background:url("+getHelthStatusImage(_x)+") no-repeat scroll left bottom;padding-left:16px'";
            var clicklink = "";
            var clickmsg="";
            if(connstatus==4){
                clicklink= "href='#' onClick=showHealthEditWindow('"+this.id+"') wtf:qtip='"+WtfGlobal.getLocaleText('pm.project.home.health.details.tip')+"'";
                clickmsg =" <span style='font-size:11px;'>"+WtfGlobal.getLocaleText('pm.project.home.health.viewdetails')+"<span>";
            }
            document.getElementById("chart"+this.currId).innerHTML="<dl class='healthlist'><dt style="+s1+"><a "+clicklink+">"+getHealthStatus(_x)+clickmsg+"</a></dt><dd>"+_pt.toFixed()+WtfGlobal.getLocaleText('pm.project.home.health.infotext');
        },this);

    },
clickHealthEditWindow: function(){
   Wtf.Ajax.requestEx({
                url: 'admin.jsp',
                method:'post',
                params : {
                    pid:this.currId,
                    action:0,
                    mode:26
                }},this,
                function(result, request) {
                    var _r = eval( '('+result+')');
                    this.clickHealthEditWindow1(_r);
                },this);
},
clickHealthEditWindow1:function(healthdata){
    var tpl = new Wtf.Template(
        '<table class="healtheditWin">',
//        '<th colspan=3 class="healthcharttitle">Health status of Project</span></th>',
        '<tr>',
        '<td class="healthname">'+WtfGlobal.getLocaleText('pm.project.home.health.completed')+':<span class="healthValue">{completed}</span></td>',
        '<td class="healthname">'+WtfGlobal.getLocaleText('pm.project.home.health.inprogress')+':<span class="healthValue">{ontime}</span></td>',
        '<td class="healthname">'+WtfGlobal.getLocaleText('pm.project.home.health.needattention')+':<span class="healthValue">{needattention}</span></td>',
        '</tr> <tr>',
        '<td class="healthname">'+WtfGlobal.getLocaleText('pm.project.home.health.overdue')+':<span class="healthValue">{overdue}</span></td>',
        '<td class="healthname">'+WtfGlobal.getLocaleText('pm.project.home.health.future')+':<span class="healthValue">{future}</span></td>',
        '</tr>',
        '<table>'
        );
    tpl.compile();
    var tplbody = new Wtf.Panel({
        border:false,
        bodyStyle:'background:white;border-bottom:1px solid #bfbfbf; padding-bottom:10px;padding-top:5px;'
    });
    var statusForm = new Wtf.form.FormPanel({
        url:'../../admin.jsp?action=1&mode=2&emode=5&pid='+this.currId,
        bodyStyle:'background:#f1f1f1',
        items:[{
            xtype:'fieldset',
            title:WtfGlobal.getLocaleText('pm.project.health.parameter'),
            height:140,
            border:false,
            labelWidth:150,
            items:[{
                    border: false,
                    layout: 'fit',
                    autoHeight: true,
                    html:WtfGlobal.getLocaleText('pm.createeditproject.healthdetail.text'),
                    bodyStyle:'padding-bottom:10px;color:#15428B;font-size:11px'
                },{
                xtype:'numberfield',
                fieldLabel: WtfGlobal.getLocaleText('pm.project.health.otpercentage'),
                name: 'ontime',
                allowBlank:false,
                value : healthdata.ontime
            },{
                xtype:'numberfield',
                fieldLabel: WtfGlobal.getLocaleText('pm.project.health.sgbehind'),
                name: 'slightly',
                id: 'slightly'+this.id,
                allowBlank:false,
                value :  healthdata.slightly
            }/*,{
                xtype:'numberfield',
                fieldLabel: WtfGlobal.getLocaleText('pm.project.health.gbpercentage'),
                name: 'gravely',
                id:'gravely'+this.id,
                readOnly:true,
                hidden:true,
                value :healthdata.gravely
            }*/]
        }]

    });
    if(!healthWin){
    var healthWin = new Wtf.Window({
        cls: "tagwindow",
        border: false,
        resizable: false,
        closable: true,
        modal:true,
        width:400,
        title:WtfGlobal.getLocaleText('pm.project.health.information'),
        items:[ tplbody,statusForm],
        buttons:[{
            text:WtfGlobal.getLocaleText('pm.common.save'),
            scope:this,
            handler: function(btn){
                if(statusForm.form.isValid()){
                    btn.disable();
                    statusForm.form.submit({
                        waitMsg: WtfGlobal.getLocaleText('pm.loading.text')+'...',
                        scope : this,
                        useraction: this.mode,
                        failure: function(frm, action){
                                msgBoxShow(285,Wtf.MessageBox.ERROR);
                                healthWin.close();
                        },
                        success: function(frm, action){
                                msgBoxShow(254, 0);
                                healthWin.close();
                                this.refreshHealthStatus()
                        }
                    });
                }
            }
        },{
            text:WtfGlobal.getLocaleText('lang.close.text'),
            handler: function(){
                healthWin.close();
            }
        }
        ]
    });
    var xy = Wtf.get('chart'+this.currId).getXY();
    healthWin.setPagePosition(xy);
    healthWin.show();
    tpl.overwrite(tplbody.body,{
        completed:this.healthValues.completed,
        ontime:this.healthValues.ontime,
        needattention:this.healthValues.needattention,
        overdue:this.healthValues.overdue,
        future:this.healthValues.future
    });
    }
//    Wtf.getCmp('slightly'+this.id).on('blur', function(cmp){
//        Wtf.getCmp('gravely'+this.id).setValue(cmp.getValue());
//    },this)

},
showTagWindow: function(){
        if (!this.tagwin) {
            this.tagwin = new Wtf.TagWindow({
                cls: "tagwindow",
                border: false,
                resizable: false,
                closable: false
            });
            this.tagwin.on('savetags', function(tagstr){
                Wtf.Ajax.requestEx({
                    url: this.UserTagurlpath,
                    params: {
                        t: tagstr,
                        u: 1
                    }
                }, this, function(result, req){
                    this.view18.store.loadData(eval("(" + result + ")"));
                });
            }, this);
            this.tagwin.on('close', function(){
                this.tagwin = null;
            }, this);
            var tagarr = [];
            this.view18.store.each(function(el){
                tagarr.push(el.data.tagname);
            }, this);
            this.tagwin.setmytagstore(tagarr);
            var xy = Wtf.get(this.tagbutton2).getXY();
            this.tagwin.setPagePosition(xy);
            this.tagwin.setTagText(tagarr.join(' '));
            this.tagwin.show();
            focusOn(this.tagwin.tagtextbox.id);
        }
        else {
            this.tagwin.close();
        }
    },

    setAboutDetails: function(Abouturlpath, Abouturlfields, status){
        var str = this._getStore(Abouturlpath, 'data', Abouturlfields);
        str.removeAll();
        if(this.WhichAbout == 'user'){
            str.load();
            this.templ = new Wtf.XTemplate('<tpl for=".">',
                '<div class="tagsMainDiv"><div class="profileKeys"><span id="tagsspan1" class="tagsspan">'+WtfGlobal.getLocaleText('pm.updateprofile.designation')+':  </span></div><div id = "tagsdiv1" class="tagsdiv profileValues"><span id="ts1">{designation}</span></div><br clear="all"></div>',
                '<div class="tagsMainDiv"><div class="profileKeys"><span id="tagsspan2" class="tagsspan">'+WtfGlobal.getLocaleText('pm.updateprofile.aboutme')+':  </span></div><div id = "tagsdiv2" class="tagsdiv profileValues"><span id="ts2">{about}</span></div><br clear="all"></div>',
                '<div class="tagsMainDiv"><div class="profileKeys"><span id="tagsspan2" class="tagsspan">'+WtfGlobal.getLocaleText('lang.emailid.text')+':  </span></div><div id = "tagsdiv2" class="tagsdiv profileValues"><span id="ts2">{startdate}</span></div><br clear="all"></div>',
                '<div class="tagsMainDiv"><div class="profileKeys"><span id="tagsspan3" class="tagsspan">'+WtfGlobal.getLocaleText('lang.address.text')+':  </span></div><div id = "tagsdiv3" class="tagsdiv profileValues"><span id="ts3">{createdon}</span></div><br clear="all"></div>',
                '<div class="tagsMainDiv"><div class="profileKeys"><span id="tagsspan4" class="tagsspan">'+WtfGlobal.getLocaleText('lang.contactno.text')+':   </span></div><div id = "tagsdiv4" class="tagsdiv profileValues"><span id="ts4">{nickname}</span></div><br clear="all"></div>',
                '</tpl>');
        } else {
               var cctpl ="";
               var _s = Wtf.cusColumnDS;
                for(var i=0; i<_s.getCount(); i++){   // add here custom column (by kamlesh)
                    if(_s.getAt(i).get("visible") && _s.getAt(i).get("module")=="Project")
                        cctpl += '<div class="tagsMainDiv"><div class="profileKeys"><span id="tagsspan1" class="tagsspan">'+_s.getAt(i).get("header")+': </span></div><div id = "tagsdiv1" class="tagsdiv profileValues"><span id="ts1">{'+_s.getAt(i).get('dataIndex')+'}</span></div><br clear="all"></div>';
                }
              this.templ = new Wtf.XTemplate('<tpl for=".">',
                '<div class="tagsMainDiv"><div class="profileKeys"><span id="tagsspan3" class="tagsspan">'+WtfGlobal.getLocaleText('pm.common.createdon')+':</span></div><div id = "tagsdiv3" class="tagsdiv profileValues"><span id="ts3">{createdon}</span></div><br clear="all"></div>',
                '<div class="tagsMainDiv"><div class="profileKeys"><span id="tagsspan4" class="tagsspan">'+WtfGlobal.getLocaleText('pm.common.startdate')+':</span></div><div id = "tagsdiv4" class="tagsdiv profileValues"><span id="ts4">{startdate}</span></div><br clear="all"></div>',
                '<div class="tagsMainDiv"><div class="profileKeys"><span id="tagsspan1" class="tagsspan">'+WtfGlobal.getLocaleText('lang.description.text')+':</span></div><div id = "tagsdiv1" class="tagsdiv profileValues"><span id="ts1">{about}</span></div><br clear="all"></div>',
                cctpl,
            '</tpl>');
            if(typeof this.projectIndex == 'number'){
                var pdata = projects[this.projectIndex];
                var tplvalues={
                    nickname: pdata.nickname,
                    createdon: Date.parseDate(pdata.createdon, 'Y-m-d').format(WtfGlobal.getOnlyDateFormat()),
                    startdate: Date.parseDate(pdata.startDate, 'Y-m-d H:i:s').format(WtfGlobal.getOnlyDateFormat()),
                    about: pdata.about
                };
                for(i=0; i<_s.getCount(); i++){
                    if(_s.getAt(i).get("visible")){
                        var val = pdata.customColumn[_s.getAt(i).get("dataIndex")];
                        if(_s.getAt(i).get('type')==Wtf.cc.columntype.DATE_FIELD){
                            if(val=="0")
                                val = "";
                            else{
                                val = (new Date(eval(val)));
                                val =val.format(WtfGlobal.getOnlyDateFormat());
                            }
                        }else if(_s.getAt(i).get('type')==Wtf.cc.columntype.CHECKBOX){
                            val = eval(val)?WtfGlobal.getLocaleText('lang.yes.text'):WtfGlobal.getLocaleText('lang.no.text');
                        }
                        tplvalues[_s.getAt(i).get("dataIndex")]=val;
                    }
                }
                var rec = new this.tplRecord(tplvalues);

                str.insert(0, rec);
            } else {
                str.load();
            }
        }
        this.view17.tpl = this.templ;
        this.view17.setStore(str);
        if(this.WhichAbout == 'project' && ((status == 1 || status == 3) || (status == 0 && this.archived == true))){
            var temp = Wtf.get(this.currId + '_removeSpan');
            if(temp)
                temp.dom.style.display = 'none';
            this.remove(this.updatesPortlet);
        }
    },
   refreshHealthStatus : function(){
      this.setHealthMeter(4);
   },
    _getStore: function(u, r, f){
        var recordarr = [];
        for(var i =0;i<f.length;i++){
            recordarr.push(f[i]);
        }
        if(r=='data'){
            getStoreFields(recordarr);
        }
        var tplRecord = Wtf.data.Record.create(recordarr);
        if(r != 'tags')
            this.tplRecord = tplRecord;
        return new Wtf.data.Store({
            url: u,
            autoLoad: false,
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },tplRecord)
        });
    },

    setTagDetails: function(Tagurlpath, Tagurlfields){
        var str = this._getStore(Tagurlpath, 'tags', Tagurlfields);
        str.load();
        this.view18.setStore(str);
        this.UserTagurlpath = Tagurlpath;
        this.UserTagfields = Tagurlfields;
    }
});
function showHealthEditWindow(objid){
    Wtf.getCmp(objid).clickHealthEditWindow();
}
/*  WtfAboutView: End   */

/*  WtfProfileView: Start   */
Wtf.ProfileView = function(config){
    Wtf.apply(this, config);
    Wtf.enumStat = {
        AI: 0,
        AR: 1,
        JC: 2,
        RR: 3,
        RC: 4,
        RI: 5,
        LC: 6,
        WFA: 7,
        DFN: 8
    };
    Wtf.ProfileView.superclass.constructor.call(this, config);
    this.addEvents({
        "discussionclicked": true,
        "shareddocclicked": true,
        "projectplanclicked": true,
        "projdocclicked": true,
        "projnewdocclicked": true,
        "teamcalclicked": true,
        "adminpageclicked": true,
        "loadcomplete": true,
        "todoclicked": true
    });

    this.loading = 0;
};

Wtf.extend(Wtf.ProfileView, Wtf.Panel, {
    buttonPosition: 1,
    initComponent: function(){
        Wtf.ProfileView.superclass.initComponent.call(this);
//        mainPanel.loadMask.hide();
        this.view1 = new Wtf.common.KWLListPanel({
            id: this.id + "view1",
            title: WtfGlobal.getLocaleText('pm.project.member.network'),
            Href: "../../user.html",
            autoLoad: false,
            border: false,
            bodyStyle:'',
            ImgSrc: "user100.png",
            useFlag: 'home',
            TabType: Wtf.etype.user,
            layout:'fit'
        });

        this.view1.on("loadcomplete", this.checkLoading, this);
        this.view1.on("afterDataRender", this.dataRendered, this);
        this.view2 = new Wtf.common.KWLListPanel({
            id: this.id + "view2",
            title: WtfGlobal.getLocaleText('pm.project.communities.related'),
            Href: "../../community.html",
            autoLoad: false,
            border: false,
            ImgSrc: "projectimage200.png",
            useFlag: 'home',
            TabType: Wtf.etype.comm,
            layout:'fit'
        });

        this.view2.on("loadcomplete", this.checkLoading, this);

        this.about = new Wtf.AboutView({
            id: this.id + 'profileName',
            archived: false,
            currId: this.uid.userid,
            border: false,
            WhichAbout: (this.uid.type == Wtf.etype.proj) ? 'project' : 'user',
            projectIndex: this.projectIndex
        });

        switch (this.uid.type) {
            case Wtf.etype.user:
                this.setRelation(Wtf.req.prf + 'user/getUserrelation.jsp', "Related");
                break;

            case Wtf.etype.comm:
                this.setRelation(Wtf.req.prf + 'community/getMembershipStatus.jsp', WtfGlobal.getLocaleText('pm.project.communities.related'));
                break;

            case Wtf.etype.proj:
                this.setRelation(Wtf.req.prj + 'getProjectMembership.jsp', "Related");
                break;
        }
        this.dstorerecord =  Wtf.data.Record.create([
             {name: 'id1'},
             {name: 'connstatus'},
             {name: 'archived'},
             {name: 'notifSubVal'}
        ]);
        this.addEvents({
            'openreportfromwidget': true
        });
        this.dstore = new Wtf.data.Store({
            method: 'POST',
            url: this.Url,
            baseParams: ({
                userid1: loginid,
                userid2: this.param2
            }),
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.dstorerecord)
        });
        this.dstore.on('load', this.loaddata, this);
    },
    dataRendered: function(){
//        if(!mainPanel.loadMask.hidden)
//            mainPanel.loadMask.hide();
    },
    checkLoading: function(){
        this.loading++;
//        if (this.loading == 2) {
            this.fireEvent("loadcomplete");
//        }
    },

    setRelation: function(reljsp, heading){
        this.Url = reljsp;
        this.param2 = this.uid.userid;
        if (this.uid.type == Wtf.etype.proj)
            this.view1.title = WtfGlobal.getLocaleText('pm.project.members.text');
        else
            if (this.uid.type == Wtf.etype.comm)
                this.view1.title = this.profiletitle + "'s Members";
            else
                this.view1.title = this.profiletitle + " " + WtfGlobal.getLocaleText('pm.common.snetwork');
        this.view2.title = heading;
    },

    onRender: function(config){
        Wtf.ProfileView.superclass.onRender.call(this, config);
        var edt_tag=null;
        this.ComponentMainPanel = this.add({
            layout: "fit",
            items: [{
                border: false,
                layout: "column",
                bodyStyle: "background-color:#FFFFFF; height:auto;overflow-y:auto;",
                items: [{
                    columnWidth: .55,
                    border: false,
                    bodyStyle: 'background-color:#FFFFFF;',
                    items: [{
                        bodyStyle: 'padding:16px 16px 0 16px;border-right:1px solid #CEDFF5;',
                        border: false,
                        autoHeight: true,
                        items: this.about
                    }]
                }, {
                    columnWidth: .43,
                    id: "panels" + this.id,
                    border: false,
                    bodyStyle: 'background-color:#FFFFFF;height:630px;',
                    layout: "border",
                    items: [{
                            xtype: 'panel',
                            region: 'north',
                            bodyStyle: 'padding:10px 5px 10px 10px;background-color:#FFFFFF;border-bottom:1px solid #CEDFF5;',
                            height: 450,
                            id: this.id+'networkpanel',
                            layout: 'fit',
                            header: false,
                            border: false,
                            items: [this.view1]
                        },(this.uid.type == Wtf.etype.proj) ? {
                            xtype: 'fieldset',
                            id: "notsubpanel" + this.id,
                            region: 'center',
                            collapsible: false,
                            border: false,
                            hideBorders: true,
                            maskDisabled: (Wtf.isIE) ? false : true,
                            layout: 'form',
                            cls: 'notificationFieldset',
                            bodyStyle : "font-size:12px;",
                            autoHeight : true,
                            title: WtfGlobal.getLocaleText('pm.project.home.notifications'),
                            items:[{
                                border: false,
                                autoWidth: true,
                                style:'margin:5px;padding-left:15px;',
                                defaults:{
                                    hideLable: true
                                }
                            }]
                        } : {xtype: 'panel', border: false, html: '', region: 'center'}]
                }]
            }],
            bbar: [this.edt_tag = new Wtf.Button({
                text: WtfGlobal.getLocaleText('pm.common.edittags'),
                iconCls: "dpwnd edittags",
                tooltip:{
                    title: WtfGlobal.getLocaleText('pm.common.edittags'),
                    text: '<div class="dsf">'+WtfGlobal.getLocaleText('pm.Help.projhomeedittag')+'</div>',
                    closable:true
                },
                handler: this.about.showTagWindow,
                id: this.id+'editTag',
                scope: this.about
            })/*,'->',new Wtf.Button({
                text: WtfGlobal.getLocaleText('pm.common.refresh'),
                iconCls: "pwnd edittags",
                detailTip:'Refresh the content of the Project Members, if there are any changes made will be reflected.',
                tooltip:new Wtf.ToolTip({
                    title: WtfGlobal.getLocaleText('pm.project.member.refresh'),
                    text: '<div class="dsf">Click to Edit Tags</div>',
                    closable:true
                }),
                handler: this.about.showTagWindow,
                scope: this.about
            })*/]
        });


        Wtf.getCmp("panels" + this.id).on("resize", function(e, aw, ah, rw, rh){
//            e.getComponent("panel1" + this.id).setWidth(aw / 2);
            e.doLayout();
        }, this);
        this.loadStore();
        this.ComponentMainPanel.doLayout();
        Wtf.QuickHelp.register([this.edt_tag],this.about);
        if(this.uid.type == Wtf.etype.proj && typeof this.projectIndex == 'number'){
            var task = new Wtf.util.DelayedTask(function(){
                this.loadcustomdata();
            }, this);
            task.delay(20);
        }
    },

    loadStore: function(){
        if(this.uid.type == Wtf.etype.proj){
            if(!(typeof this.projectIndex == 'number')){
                this.dstore.load();
            } else {
                var pdata = projects[this.projectIndex];
                var perm = pdata.permissions.data[0];
                var rec = new this.dstorerecord({
                    id1: perm.id1,
                    connstatus: perm.connstatus,
                    archived: perm.archived,
                    notifSubVal: pdata.notifSubVal
                });
                this.dstore.removeAll();
                this.dstore.insert(0, rec);
            }
        } else
            this.dstore.load();
    },

    updateSubscription: function(check){
        var val = 0;
        for(var c = 1; c < 4; c++){
            var chk = Wtf.getCmp(this.id+'_'+Math.pow(2, c));
            if(chk){
                if(chk.getValue()){
                    val += Math.pow(2, c);
                }
            }
        }
        Wtf.Ajax.requestEx({
            url: Wtf.req.prj + 'projectGridData.jsp',
            params: {
                action : "updateNotificationStatus",
                value: val,
                projectid: this.param2
            }
        }, this,
        function(resp){
            var obj = eval('('+resp+')');
            if(obj){
                if(obj.success){
                    if(typeof this.projectIndex == 'number')
                        projects[this.projectIndex].notifSubVal = val;
                }
                else
                    msgBoxShow(4, 1);
            }
        },
        function(resp){
            msgBoxShow(182, 1);
        });
    },

    loadcustomdata: function(){
        this.setconnection(this.dstore);
        this.setIdentity(loginid, this.uid.userid);
        this.ComponentMainPanel.doLayout();
        this.doLayout();
        for(var c = 1; c < 4; c++){
            var check = Wtf.get(this.id+'_'+Math.pow(2, c));
            if(check){
                check.addListener('click', function(e, check){
                    this.updateSubscription(check);
                }, this);
            }
        }
        hideMainLoadMask();
    },

    loaddata: function(){
        this.loadcustomdata();
    },

    setLayoutDetails: function(jspuserdet, jsputags, jspmemlist, jspcomlist, arg1, arg2, whichabout){
        if(whichabout == 'project'){
            if(typeof this.projectIndex == 'number'){
                var path = projects[this.projectIndex].image;
                path = (path == '') ? '../../images/projectimage200.png' : path;
                this.about.setImage(path);
                this.about.setHealthMeter(this.dstore.getAt(0).data['connstatus']);
            } else {
                Wtf.iStore.getImg(this.uid.type, this.uid.userid, this.about.setImage.createDelegate(this.about), true, 200);
            }
            this.about.WhichAbout = 'project';
            if(this.dstore.getCount() > 0)
                this.about.archived = this.dstore.getAt(0).data['archived'];
                var args =[arg1, arg2, 'about', 'createdon', 'startdate', 'nickname'];
                args = getCCDataIndexes(args, "Project");
            this.about.setAboutDetails(jspuserdet + this.uid.userid,args , this.projStat);
        } else {
            Wtf.iStore.getImg(this.uid.type, this.uid.userid, this.about.setImage.createDelegate(this.about), true, 200);
            this.about.WhichAbout = 'user';
            this.about.setAboutDetails(jspuserdet + this.uid.userid, [arg1, arg2, 'about', 'createdon', 'startdate', 'nickname', 'designation'], this.connstatus);
        }
        this.about.setTagDetails(jsputags + this.uid.userid, ['tagname']);
        this.view1.url = jspmemlist + this.uid.userid;
//        this.view2.url = jspcomlist + this.uid.userid;
        this.view1.calculatePageSize();
        this.view1.checkForReq();
//        this.view2.calculatePageSize();
//        this.view2.checkForReq();
    },
    setIdentity: function(){
        var panelBbar = this.ComponentMainPanel.getBottomToolbar();
        switch (this.uid.type) {
            case 0:
                this.setLayoutDetails(Wtf.req.prf + 'user/getuserdetails.jsp?login=', Wtf.req.prf + 'optags.jsp?id=', Wtf.req.prf + 'common/getFriendList.jsp?login=', Wtf.req.prf + 'common/getUserCommunities.jsp?login=', 'userid', 'username', 'user');

                panelBbar.addButton({
                    text: WtfGlobal.getLocaleText('pm.userprofile.sendmessage'),
                    iconCls: "pwnd pmsgicon",
                    handler: this.message,
                    tooltip: {
                        title: WtfGlobal.getLocaleText('pm.userprofile.sendmessage'),
                        text: WtfGlobal.getLocaleText('pm.Help.sendmessage')
                    },
                    scope: this,
                    id: this.id+'sendPMsgBtn'
                });
                if(Wtf.UPerm.AssignUserToProject){
                    panelBbar.addButton({
                        text:  WtfGlobal.getLocaleText('pm.common.assigntoprojects'),
                        iconCls: 'dpwnd addppl',
                        handler: this.addHandler,
                        tooltip: {
                            title:  WtfGlobal.getLocaleText('pm.common.assigntoprojects'),
                            text: WtfGlobal.getLocaleText('pm.project.team.assignmore')
                        },
                        scope: this,
                        id: this.id+'addToProjBtn'
                    });
                }
                break;

            /*case 1:
                this.setLayoutDetails(Wtf.req.prf + 'community/getcommunitydetails.jsp?login=', Wtf.req.prf + 'optags.jsp?id=', Wtf.req.prf + 'community/getCommunityMembers.jsp?login=', Wtf.req.prf + 'community/getRelatedCommunities.jsp?login=', 'communityid', 'communityname');
                if (!this.dstore.getCount() == 0) {
                    if (this.dstore.getAt(0).data['connstatus'] == 3 || this.dstore.getAt(0).data['connstatus'] == 4 || this.dstore.getAt(0).data['connstatus'] == 5) {
                        this.ComponentMainPanel.getBottomToolbar().addButton({
                            text: WtfGlobal.getLocaleText('pm.module.discussion'),
                            iconCls: "pwnd communitydiscuss",
                            tooltip: {
                                title: WtfGlobal.getLocaleText('pm.module.discussion'),
                                text: WtfGlobal.getLocaleText('pm.project.discussion.text')
                            },
                            handler: this.opendiscussiontab,
                            scope: this
                        });
                        this.ComponentMainPanel.getBottomToolbar().addButton({
                            text: WtfGlobal.getLocaleText('pm.search.searchdoc'),
                            iconCls: "pwnd shared",
                            tooltip: {
                                title: WtfGlobal.getLocaleText('pm.search.searchdoc'),
                                text: WtfGlobal.getLocaleText('pm.project.document.openshared')
                            },
                            handler: this.opensharedtab,
                            scope: this
                        });
                        if(!WtfGlobal.EnableDisable(Wtf.UPerm.Community, Wtf.Perm.Community.ManageCommunity)) {
                        //if (this.dstore.getAt(0).data['connstatus'] == 5 || this.dstore.getAt(0).data['connstatus'] == 4) {
                            this.ComponentMainPanel.getBottomToolbar().addButton({
                                text: WtfGlobal.getLocaleText('pm.administration.text'),
                                iconCls: "pwnd admintab",
                                tooltip: {
                                    title: WtfGlobal.getLocaleText('pm.administration.text'),
                                    text: WtfGlobal.getLocaleText('pm.admin.opentext')
                                },
                                handler: this.openadmintab,
                                scope: this
                            });
                        //}
                      }
                    }
                }

                break;*/

            case 2:
                this.setLayoutDetails(Wtf.req.prj + 'getprojectdetails.jsp?&loginid='+loginid+'&login=', Wtf.req.prf + 'optags.jsp?id=', Wtf.req.prj + 'getProjectMembers.jsp?login=', Wtf.req.prj + 'getRelatedProjects.jsp?login=', 'projectid', 'projectname', 'project');
//                this.setLayoutDetails('../../ProjectServlet.jsp?action=4&login=', Wtf.req.prf + 'optags.jsp?id=', Wtf.req.prj + 'getProjectMembers.jsp?login=', Wtf.req.prj + 'getRelatedProjects.jsp?login=', 'projectid', 'projectname');
                if (!this.dstore.getCount() == 0) {
                    if (this.dstore.getAt(0).data['connstatus'] == 3 || this.dstore.getAt(0).data['connstatus'] == 4 || this.dstore.getAt(0).data['connstatus'] == 5) {
                        if(Wtf.featuresView.proj) {
                            panelBbar.addButton({
                                text: WtfGlobal.getLocaleText('pm.common.projectplan'),
                                iconCls: "pwnd projplan",
                                tooltip: {
                                    title: WtfGlobal.getLocaleText('pm.common.projectplan'),
                                    text: WtfGlobal.getLocaleText('pm.Help.qlplan')
                                },
                                handler: this.openprojectplan,
                                scope: this,
                                id: this.id+'PPlan'
                            });
                        }
//                        if(isRoleGroup(3)){
//                            panelBbar.addButton({
//                                text: WtfGlobal.getLocaleText('pm.project.document.title'),
//                                iconCls: "pwnd shared",
//                                tooltip: {
//                                    title: WtfGlobal.getLocaleText('lang.documents.text'),
//                                    text: WtfGlobal.getLocaleText('pm.Help.qldoc')
//                                },
//                                handler: this.openprojdoctab,
//                                scope: this
//                            });
//                        }
                        if(Wtf.featuresView.disc) {
                            panelBbar.addButton({
                                text: WtfGlobal.getLocaleText('pm.module.discussion'),
                                iconCls: "dpwnd communitydiscuss",
                                tooltip: {
                                    title: WtfGlobal.getLocaleText('pm.module.discussion'),
                                    text: WtfGlobal.getLocaleText('pm.Help.qldisc')
                                },
                                handler: this.opendiscussiontab,
                                scope: this,
                                id: this.id+'Disc'
                            });
                        }
                        if(Wtf.featuresView.todo) {
                            panelBbar.addButton({
                                text: WtfGlobal.getLocaleText('pm.module.todo'),
                                iconCls: "pwnd todolistpane",
                                tooltip: {
                                    title: WtfGlobal.getLocaleText('pm.module.todo'),
                                    text: WtfGlobal.getLocaleText('pm.Help.qltodo')
                                },
                                handler: this.opentodotab,
                                scope: this,
                                id: this.id+'todo'
                            });
                        }
                        if(Wtf.subscription.cal.subscription && Wtf.featuresView.cal) {
                            panelBbar.addButton({
                                text: WtfGlobal.getLocaleText('pm.module.teamcalendar'),
                                iconCls: "dpwnd teamcal",
                                tooltip: {
                                    title: WtfGlobal.getLocaleText('pm.module.teamcalendar'),
                                    text: WtfGlobal.getLocaleText('pm.Help.qlcal')
                                },
                                handler: this.openteamcaltab,
                                scope: this,
                                id: this.id+'teamCal'
                            });
                        }
                        if(Wtf.subscription.docs.subscription && Wtf.featuresView.docs){
                            panelBbar.addButton({
                                text: WtfGlobal.getLocaleText('lang.documents.text'),
                                iconCls: 'pwnd doctabicon',
                                tooltip: {
                                    title: WtfGlobal.getLocaleText('pm.project.document.title'),
                                    text: WtfGlobal.getLocaleText('pm.Help.qldoc')
                                },
                                handler: this.opennewprojdoctab,
                                scope: this,
                                id: this.id+'docs'
                            });
                        }
                        if(this.dstore.getAt(0).data['connstatus'] == 4) {
                            panelBbar.addButton({
                                text: WtfGlobal.getLocaleText('pm.module.projectsettings'),
                                iconCls: "pwnd admintab",
                                tooltip: {
                                    title: WtfGlobal.getLocaleText('pm.module.projectsettings'),
                                    text: WtfGlobal.getLocaleText('pm.Help.qladmin')
                                },
                                handler: this.openadmintab,
                                scope: this,
                                id: this.id+'admin'
                            });
                            if(Wtf.UPerm.ArchiveActive){
                                panelBbar.add('-');
                                var archiveButton = new Wtf.Toolbar.Button({
    //                                iconCls: "archivedProjIcon",
                                    handler: this.archiveProj,
                                    scope: this,
                                    id: this.id+'archive'
                                });
                                if(!this.dstore.getAt(0).data['archived']){
                                    archiveButton.text = WtfGlobal.getLocaleText('lang.archive.text');
                                    archiveButton.setIconClass("pwnd archivedProjIcon");
                                    archiveButton.tooltip = {
                                        title: WtfGlobal.getLocaleText('lang.archieve.project.text'),
                                        text: WtfGlobal.getLocaleText('pm.Help.projhomearchive')
                                    };
                                } else {
                                    archiveButton.text = WtfGlobal.getLocaleText('lang.active.text');
                                    archiveButton.setIconClass("dpwnd projectTabIcon");
                                    archiveButton.tooltip = {
                                        title: WtfGlobal.getLocaleText('pm.project.active'),
                                        text: WtfGlobal.getLocaleText('pm.Help.projhomeactive')
                                    };
                                }
                                panelBbar.addButton(archiveButton);
                            }
                        }
                        this.ComponentMainPanel.getBottomToolbar().add('-');
                        if(!this.dstore.getAt(0).data['archived']) {
                            this.ComponentMainPanel.getBottomToolbar().add(
                                "<a href=\""+Wtf.pagebaseURL+"feed.rss?m=project&u="+loginname+"&p="+this.uid.userid+"\" target='_blank'><img id=\""+this.id+"projRss\" class=\"rssimgMid\" alt=\"\" src=\"../../images/FeedIcon16.png\" wtf:qtip='"+WtfGlobal.getLocaleText('pm.Help.projhomerss')+"'/> "
                            );
                            this.ComponentMainPanel.getBottomToolbar().add('-');
                        }
                    }
                }
                break;
        }
        panelBbar.add('->');
        panelBbar.addButton({
            text: WtfGlobal.getLocaleText('pm.common.refresh'),
            id : 'refreshhome'+this.id,
            iconCls: "pwnd refresh",
            tooltip: {
                title: WtfGlobal.getLocaleText('pm.common.refresh'),
                text: this.uid.type == Wtf.etype.proj?WtfGlobal.getLocaleText('pm.Help.projhomerefresh'):WtfGlobal.getLocaleText('pm.Help.userProfileRefresh')
            },
            handler: this.refreshProfile,
            scope: this
        });
        if(this.uid.type == 2){
            if(Wtf.getCmp(this.id+'profileName_updatesArea')){
                var c = Wtf.getCmp(this.id+'profileName_updatesArea').el.dom.children;
                var d = Wtf.getCmp(this.id+'profileName_updatesArea').el.dom.childNodes;
                if(c){
                    c[0].className += " projHomeUpdateTitle";
                    c[1].className += " projHomeUpdate";
                } else {//for IE, Chrome, Firefox < 3
                    d[0].className += " projHomeUpdateTitle";
                    d[1].className += " projHomeUpdate";
                }
            }
        }
    //        this.ComponentMainPanel.getBottomToolbar().addItem('->');
    //        this.ComponentMainPanel.getBottomToolbar().addButton(new Wtf.Button({
    //            baseCls: 'tempRight',
    //            text: WtfGlobal.getLocaleText('pm.common.refresh'),
    //            iconCls: "pwnd edittags",
    //            detailTip:'Refresh the content of the Project Members, if there are any changes made will be reflected.',
    //            tooltip:new Wtf.ToolTip({
    //                title: WtfGlobal.getLocaleText('pm.project.member.refresh'),
    //                text: '<div class="dsf">Click to Edit Tags</div>',
    //                closable:true
    //            }),
    //            handler: this.about.showTagWindow,
    //            scope: this.about
    //        }));
    },

    archiveProj: function(obj){
        var act = 'Activate';
        if(obj.text == WtfGlobal.getLocaleText('lang.archive.text'))
            act = obj.text;
        Wtf.MessageBox.confirm(act + ' '+ WtfGlobal.getLocaleText('pm.project.text'), WtfGlobal.getLocaleText({key:'pm.msg.project.action', params: act}), function(btn){
            if(btn == 'yes'){
                Wtf.Ajax.requestEx({
                    method: 'POST',
                    url: Wtf.req.prj + "archive.jsp",
                    params: {
                        action: obj.text,
                        mode : '1',
                        projid: this.param2,
                        userid: loginid
                    }
                }, this,
                function(request, response){
                    var resobj = eval("(" +request.trim() + ")");
                    if(resobj.success) {
                        var msg = WtfGlobal.getLocaleText('pm.msg.project.archieved');
                        var buttonText = WtfGlobal.getLocaleText('lang.active.text');
                        var btTT = {
                            title: WtfGlobal.getLocaleText('pm.project.active'),
                            text: WtfGlobal.getLocaleText('pm.project.activate.text')
                        }
                        if(resobj.res == "0"){
                            msg = WtfGlobal.getLocaleText('pm.msg.project.activated');
                            buttonText = WtfGlobal.getLocaleText('lang.archive.text');
                            btTT = {
                                title: WtfGlobal.getLocaleText('lang.archieve.project.text'),
                                text: WtfGlobal.getLocaleText('pm.project.archieve.text')
                            }
                        }
                        msgBoxShow([WtfGlobal.getLocaleText('pm.msg.SUCCESS'), msg],0);
                        obj.setText(buttonText);
                        obj.setTooltip(btTT);
                        bHasChanged = true;
                        if(refreshDash.join().indexOf("all") == -1)
                            refreshDash[refreshDash.length] = 'all';
                        Wtf.getCmp('as').remove('tab'+this.param2);
                    } else {
                        msgBoxShow([WtfGlobal.getLocaleText('pm.msg.FAILURE'),resobj.res], 1);
                    }
                },
                function(request, response){
                    msgBoxShow(20);
                });
            }
        }, this);
    },

    opensharedtab: function(item, pressed){
        this.fireEvent("shareddocclicked");
    },

    opendiscussiontab: function(item, pressed){
        this.fireEvent("discussionclicked");
    },

    opentodotab: function(item, pressed){
        this.fireEvent("todoclicked");
    },
    openprojectplan: function(item, pressed){
        this.fireEvent("projectplanclicked");
    },

    openprojdoctab: function(item, pressed){
        this.fireEvent("shareddocclicked");
    },

    openteamcaltab: function(item, pressed){
        this.fireEvent("teamcalclicked");
    },

    opennewprojdoctab: function(item, pressed){
        this.fireEvent("projnewdocclicked");
    },

    openadmintab: function(item, pressed){
        this.fireEvent("adminpageclicked");
    },
    refreshProfile:function(){
        var lm = new Wtf.LoadMask(this.about.getEl(), {msg: WtfGlobal.getLocaleText('lang.pleasewait.text')+'...'});
        lm.show();
        if(this.uid.type == Wtf.etype.proj){
            if(typeof this.projectIndex == 'number'){
                this.loadStore();
                var path = projects[this.projectIndex].image;
                path = (path == '') ? '../../images/projectimage200.png' : path;
                this.about.setImage(path+'?v=1');
            } else {
                Wtf.iStore.getImg(this.uid.type, this.uid.userid, this.about.setImage.createDelegate(this.about), true, 200);
            }
            this.about.setAboutDetails(Wtf.req.prj + 'getprojectdetails.jsp?&loginid='+loginid+'&login='+this.uid.userid, ['projectid', 'projectname', 'about', 'createdon', 'startdate', 'nickname'], this.projStat);
        } else {
            this.about.setAboutDetails(Wtf.req.prf + 'user/getuserdetails.jsp?login=' + this.uid.userid, ['userid', 'username', 'about', 'createdon', 'startdate', 'nickname', 'designation'], this.connstatus);
            Wtf.iStore.getImg(this.uid.type, this.uid.userid, this.about.setImage.createDelegate(this.about), true, 200);
        }
//        Wtf.getCmp(this.id+'projInfo').html = this.about.getHtml();
        if(this.about.updatesPortlet)
            this.about.updatesPortlet.callRequest();
        if(this.uid.type==Wtf.etype.proj){
            this.addCheckBoxes();
        }
        this.setNotifSubValues(this.dstore.getAt(0));
        this.view1.calculatePageSize();
        this.view1.prevPageSize = 0;
        this.view1.checkForReq();
        lm.hide();
    },
    message: function(item, pressed){
        this.wind = new Wtf.ReplyWindow({
            uLabelName: 'To',
            bLabelName: 'Subject',
            uLabel: WtfGlobal.getLocaleText('pm.common.TO'),
            bLabel: WtfGlobal.getLocaleText('lang.subject.text'),
            tdisabled: true,
            replytoId: this.uid.userid,
            id:'replywin'+Math.random(),
            userId: loginid,
            groupId: "",
            firstReply: "",
            uFieldValue: this.profiletitle,
            bFieldValue: "",
            type: "Mail",
            sendFlag: "newmsg",
            composeMail: 1
        });
        this.wind.saveBtn.disable();
        this.sendMessagePanel = new Wtf.Panel({
            title: WtfGlobal.getLocaleText('pm.userprofile.sendmessage'),
            closable: true,
            layout: 'fit',
            // tbar: createMailToolbar(postid)
            items: this.wind
        });
        mainPanel.add(this.sendMessagePanel).show();
        this.wind.doLayout();
        this.wind.insertStore.on("load", this.handleInsertMail1, this);
//        this.wind.on("beforeClosewindow", function(){
////            alert("closewindow");
//            mainPanel.remove(this.sendMessagePanel);
//            this.sendMessagePanel.destroy();
//        }, this);
        this.wind.on("beforedestroy",function(){
            if(!this.destroymsgflag){
                mainPanel.remove(this.sendMessagePanel);
                this.sendMessagePanel.destroy();
            }
            this.destroymsgflag=false;
        },this);
        // this.wind.show();
        this.sendMessagePanel.on("beforedestroy",function(){
            this.destroymsgflag = true;
        },this);
    },
    addHandler:function(){
        this.Addto = new Wtf.AddtoWindow({
            wizard:false,
            userid:this.uid.userid
        }).show();
    },
    handleInsertMail1: function(obj, rec, opt){
        if (rec[0].data['Success'].match('Success')) {
            msgBoxShow(141, 0);
            //Wtf.Msg.alert('Message Sent', WtfGlobal.getLocaleText('pm.msg.141'));
        }
        else
            if (rec[0].data['Success'].match('Fail')) {
                msgBoxShow(142, 1);
                //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occured while sending message.');
            }
    },

    setConnectionButton: function(context, en, stat){
        this.connectionBttn.setText(context);
        this.ttipCB.setTitle(context);
        this.constatus = stat;
        if (en)
            this.connectionBttn.setDisabled(false);
        else
            this.connectionBttn.setDisabled(true);
    },

    confirmConnectionChange: function(item, pressed){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.330'), function(btn){
            if (btn == 'yes') {
                this.connectionchange(item, pressed);
            }
        },this);
    },

    connectionchange: function(item, pressed){
        var con = this.constatus;
        this.connectionBttn.setDisabled(true);
        var record =  Wtf.data.Record.create([{
            name: 'result',
            type: 'string'
        }]);
        this.dstore1 = new Wtf.data.Store({
            method: 'GET',
            url: Wtf.req.prf + 'user/setUserrelation.jsp',
            baseParams: ({
                login: loginid,
                userid1: loginid,
                userid2: this.uid.userid,
                relationid: con
            }),
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },record)
        });
        if (item.stateId == Wtf.enumStat.AR) {
            this.dstore1.baseParams.userid1 = this.uid.userid;
            this.dstore1.baseParams.userid2 = loginid;
            this.rejconBttn.setDisabled(true);
            this.dstore1.baseParams.relationid = 3;
            con = this.constatus = 3;
            this.connectionBttn.stateId = 8;
        } else if (item.stateId == Wtf.enumStat.RR || item.stateId == Wtf.enumStat.DFN) {
            this.dstore1.baseParams.userid1 = this.uid.userid;
            this.dstore1.baseParams.userid2 = loginid;
            this.dstore1.baseParams.relationid = 0;
            this.rejconBttn.setDisabled(true);
            con = this.constatus = 0;
            this.connectionBttn.stateId = 4;
        } else if (item.stateId == Wtf.enumStat.RC) {
            this.dstore1.baseParams.userid1 = loginid;
            this.dstore1.baseParams.userid2 = this.uid.userid;
            this.dstore1.baseParams.relationid = 1;
            this.rejconBttn.setDisabled(true);
            con = this.constatus = 1;
            this.connectionBttn.stateId = 7;
        }

        this.dstore1.on('load', function(){
            switch (con) {
                case 0:
                    this.setConnectionButton(WtfGlobal.getLocaleText('pm.userprofile.request'), true, 1);
                    this.loadContacttab(this.uid.userid,0);
                    this.loadContacttab(this.uid.userid,0);
                    this.rejconBttn.hide();
                    break;
                case 1:
                    this.setConnectionButton(WtfGlobal.getLocaleText('pm.member.request.approval'), false, 3);
                    this.loadContacttab(this.uid.userid,1);
                    this.loadContacttab(this.uid.userid,1);
                    this.rejconBttn.hide();
                    break;
                case 3:
                    this.setConnectionButton(WtfGlobal.getLocaleText('pm.userprofile.drop'), true, 0);
                    this.loadContacttab(this.uid.userid,3);
                    this.loadContacttab(this.uid.userid,3);
                    this.rejconBttn.hide();
                    break;
            }
        }, this);
        this.dstore1.on('loadexception', function(){
        });
        this.dstore1.load();
        bHasChanged = true;
        var temp = refreshDash.join();
        if(temp.indexOf("all") == -1 && temp.indexOf('req') == -1)
            refreshDash[refreshDash.length] = 'req';
    },
    loadContacttab:function(fuserid,type){
        var mode;
        var contactPanle = Wtf.getCmp('tabns_panel');
        var contactsview = Wtf.getCmp('contactsview');
        if(type==0){
            if(contactsview!=null){
                var obj = contactsview.getNodeById('kcont_' + fuserid);
                if(obj) {
                    obj.parentNode.removeChild(obj);
                    if(Wtf.get('chatWin'+obj.id.substr(6)))
                        Wtf.get('chatWin'+obj.id.substr(6)).destroy();
                }
            }
            mode = 5;
        }else if(type==3){
            if(contactsview!=null){
                var record = contactPanle.mycontactStore.getAt(contactPanle.mycontactStore.find("userid",fuserid));
                this.addInContactTree(fuserid,record.data.username,"<img id='AcceptImg' class='cancel' src='../../images/Delete.gif'></img>");
            }
            mode = 6;
        }else if(type==1){
            if(contactsview!=null){
                var record = contactPanle.newContactStore.getAt(contactPanle.newContactStore.find("id",fuserid));
                this.addInContactTree(fuserid,record.data.username +' [Invited]',"<img id='AcceptImg' class='cancel' src='../../images/Delete.gif' style=\"margin-left:5px;vertical-align:middle\" title='Delete contact'></img>")
            }
            mode = 7;
        }
        if(contactPanle!=null){
            contactPanle.mycontactStore.load({params : {start : 0,limit:15}});
            contactPanle.newContactStore.load({params : {start : 0,limit:contactPanle.pagingToolbar.pageSize}});
                }
        Wtf.Ajax.requestEx({
            method:'POST',
            url: Wtf.req.prt + "getFriendListDetails.jsp",
            params:{
                userid: loginid,
                mode : mode,
                remoteUser: fuserid
            }
        },
        this
        );
},

addInContactTree : function(id,text,innerHtml) {
    var contactsTree = Wtf.getCmp('contactsview');
    contactsTree.createContactNode(id,text);
},
setStatusButton: function(context, en, stat){
    this.commstatusBttn.setText(context);
    this.ttipCSB.setTitle(context);
    this.memstatus = stat;
    if (en)
        this.commstatusBttn.setDisabled(false);
    else
        this.commstatusBttn.setDisabled(true);
},

confirmStatusChange: function(item, pressed){
    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.330'), function(btn){
        if (btn == 'yes') {
            this.statuschange(item, pressed);
        }
    },this);
},

statuschange: function(item, pressed){
    var comm = this.memstatus;
    this.commstatusBttn.setDisabled(true);
    this.dstore2 = new Wtf.data.Store({
        method: 'GET',
        url: Wtf.req.prf + 'community/setStatuscommunity.jsp',
        baseParams: ({
            login: loginid,
            userid: loginid,
            comid: this.uid.userid,
            status: comm
        }),
        reader: new Wtf.data.JsonReader({
            root: 'data',
            fields: [{
                name: 'result',
                type: 'string'
            }]
        })
    });

    if (item.stateId == Wtf.enumStat.AI || item.stateId == Wtf.enumStat.AR) {
        this.dstore2.baseParams.userid = loginid;
        this.dstore2.baseParams.comid = this.uid.userid;
        this.rejreqBttn.setDisabled(true);
        this.dstore2.baseParams.status = 3;
        comm = this.memstatus = 3;
    }
    if (item.stateId == Wtf.enumStat.RI || item.stateId == Wtf.enumStat.RR) {
        this.dstore2.baseParams.userid = loginid;
        this.dstore2.baseParams.comid = this.uid.userid;
        this.dstore2.baseParams.status = 0;
        this.rejreqBttn.setDisabled(true);
        comm = this.memstatus = 0;
    }
    this.dstore2.on('load', function(){
        switch (comm) {
            case 0:
                this.setStatusButton("Join Community", true, 1);
                this.rejreqBttn.hide();
                break;
            case 1:
                this.setStatusButton(WtfGlobal.getLocaleText('pm.member.request.approval'), false, 3);
                this.rejreqBttn.hide();
                break;
            case 3:
                this.setStatusButton("Leave Community", true, 0);
                this.rejreqBttn.hide();
                break;
        }
    }, this);
    this.dstore2.on('loadexception', function(){
        }, this);
    this.dstore2.load();

},

setProjStatButton: function(context, en, stat){
    this.joinprojBttn.setText(context);
    this.ttipCSB.setTitle(context);
    this.projStat = stat;
    this.joinprojBttn.setDisabled(!en);
//        if (en)
//            this.joinprojBttn.setDisabled(false);
//        else
//            this.joinprojBttn.setDisabled(true);
},
//    projchange:function(item, pressed){
//        var confirmMess = "";
//        var comm = this.projStat;
//        switch (comm) {
//            case 0:
//                confirmMess = "Are you sure you want to leave this project ?";
//                break;
//            case 1:
//                confirmMess = "Are you sure you want to join this project ?";
//                break;
//            default:
//                confirmMess = WtfGlobal.getLocaleText('pm.msg.330');
//                //this.projchange1(item,pressed)
//        }
//         Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), confirmMess, function(btn){
//            if (btn == "yes") {
//                if(item.text == "Accept Invitation"){
//                    item.stateId = 6;
//                }
//                this.projchange1(item, pressed);
//
//            }
//        }, this);
//
//    },

confirmProjectStatusChange : function(item, pressed){
    var whatWouldYouLike = (this.projStat==0)?"leave":
    (this.projStat==1)?"join":"";
    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText({key:'pm.project.home.statuschange',params:whatWouldYouLike}),
        function(btn){
            if (btn == 'yes') {
                if(item.text == "Accept Invitation")
                    item.stateId = 6;
                this.projchange1(item, pressed);
            }
        }, this);
},

projchange1: function(item){
    var comm = this.projStat;
    this.joinprojBttn.setDisabled(true);
    var record =  Wtf.data.Record.create([{
        name: 'result',
        type: 'string'
    }]);
    this.dstore2 = new Wtf.data.Store({
        method: 'GET',
        url: Wtf.req.prf + 'project/setStatusproject.jsp',
        baseParams: ({
            userid: loginid,
            comid: this.uid.userid,
            flagLeave : 1,
            status: comm
        }),
        reader: new Wtf.data.KwlJsonReader({
            root: 'data'
        },record)
    });

    if (item.stateId == Wtf.enumStat.AI || item.stateId == Wtf.enumStat.AR) {
        this.dstore2.baseParams.userid = loginid;
        this.dstore2.baseParams.comid = this.uid.userid;
        this.rejprojBttn.setDisabled(true);
        this.dstore2.baseParams.status = 3;
        comm = this.projStat = 3;
    }
    if (item.stateId == Wtf.enumStat.RI || item.stateId == Wtf.enumStat.RR) {
        this.dstore2.baseParams.userid = loginid;
        this.dstore2.baseParams.comid = this.uid.userid;
        this.dstore2.baseParams.status = 0;
        this.rejprojBttn.setDisabled(true);
        comm = this.projStat = 0;
    }
    this.dstore2.on('load', function(){
        switch (comm) {
            case 0:
                this.setProjStatButton(WtfGlobal.getLocaleText('pm.project.member.join.text'), true, 1);
                this.joinprojBttn.stateId = 2;
                this.rejprojBttn.hide();
                break;
            case 1:
                this.setProjStatButton(WtfGlobal.getLocaleText('pm.member.request.approval'), false, 3);
                this.rejprojBttn.hide();
                break;
            case 3:
                this.setProjStatButton("Leave Project", true, 0);
                this.rejprojBttn.hide();
                break;
        }
    }, this);
    this.dstore2.on('loadexception', function(obj, a, b){
        var obj = eval("(" + b.responseText.trim() + ")");
        obj = eval("(" + obj.data.trim() + ")");
        msgBoxShow([WtfGlobal.getLocaleText('lang.error.text'), obj.error], 1);
    }, this);
    this.dstore2.load();
    bHasChanged = true;
    var temp = refreshDash.join();
    if(temp.indexOf("all") == -1 && temp.indexOf('all') == -1)
        refreshDash[refreshDash.length] = 'all';
},

setconnection: function(resultobj){
    switch (this.uid.type) {
        //case 0 = user, case 1 = community, case 2 = project
        case 0:
            this.connectionBttn = new Wtf.Toolbar.Button({
                id: this.id + "connection",
                stateId: Wtf.enumStat.RC,
                text: WtfGlobal.getLocaleText('pm.userprofile.request'),
                iconCls: "dpwnd addppl",
                handler: this.confirmConnectionChange,//this.connectionchange,
                region: 'south',
                scope: this
            });
            this.rejconBttn = new Wtf.Toolbar.Button({
                id: this.id + "rejcon",
                stateId: Wtf.enumStat.RR,
                text: WtfGlobal.getLocaleText('pm.userprofile.reject'),
                iconCls: "dpwnd addppl",
                handler: this.confirmConnectionChange,//this.connectionchange,
                region: 'south',
                scope: this
            });
            this.ComponentMainPanel.getBottomToolbar().insertButton(0, this.connectionBttn);
            this.ComponentMainPanel.getBottomToolbar().insertButton(1, this.rejconBttn);
            this.ttipCB = new Wtf.ToolTip({
                id: this.id + 'tooltipCB',
                title: WtfGlobal.getLocaleText('pm.userprofile.request'),
                text: WtfGlobal.getLocaleText('pm.member.request.rejectconn'),
                target: this.connectionBttn.id
            });
            this.ttipRB = new Wtf.ToolTip({
                id: this.id + 'tooltipRB',
                title: WtfGlobal.getLocaleText('pm.member.request.reject'),
                target: this.rejconBttn.id
            });
            if (!resultobj.getCount() == 0) {
                switch (resultobj.getAt(0).data['connstatus']) {
                    case "-1":
                        //users clicks on his own profile
                        this.rejconBttn.hide();
                        this.connectionBttn.hide();
                        break;
                    case 0:
                        //user not connected
                        this.constatus = 1;
                        this.rejconBttn.hide();
                        this.connectionBttn.stateId = Wtf.enumStat.RC;
                        this.connectionBttn.setText(WtfGlobal.getLocaleText('pm.userprofile.request'));
                        this.ttipCB.setTitle(WtfGlobal.getLocaleText('pm.member.request.rejectconn'));
                        break;

                    case 1:
                        //user has either sent a request for connection or has received request
                        if (resultobj.getAt(0).data['id1'] == loginid) {
                            this.constatus = 3;
                            this.rejconBttn.hide();
                            this.connectionBttn.stateId = Wtf.enumStat.WFA;
                            this.connectionBttn.setText(WtfGlobal.getLocaleText('pm.member.request.approval'));
                            this.ttipCB.setTitle(WtfGlobal.getLocaleText('pm.member.request.approval'));
                            this.connectionBttn.disable();

                        } else {
                            this.constatus = 3;
                            this.connectionBttn.stateId = Wtf.enumStat.AR;
                            this.connectionBttn.setText(WtfGlobal.getLocaleText('pm.userprofile.approve'));
                            this.ttipCB.setTitle(WtfGlobal.getLocaleText('pm.project.home.approvecon.tip'));
                            this.rejconBttn.stateId = Wtf.enumStat.RR;
                            this.rejconBttn.setText(WtfGlobal.getLocaleText('pm.userprofile.reject'));
                            this.ttipRB.setTitle(WtfGlobal.getLocaleText('pm.member.request.reject'));

                        }
                        break;

                    case 3:
                        //user is member
                        this.constatus = 0;
                        this.rejconBttn.hide();
                        this.connectionBttn.stateId = Wtf.enumStat.DFN;
                        this.connectionBttn.setText(WtfGlobal.getLocaleText('pm.userprofile.drop'));
                        this.ttipCB.setTitle(WtfGlobal.getLocaleText('pm.project.home.drop.tip'));
                        break;
                }
            } else {
                this.constatus = 1;
                this.rejconBttn.hide();
                this.connectionBttn.stateId = Wtf.enumStat.RC;
                this.connectionBttn.setText(WtfGlobal.getLocaleText('pm.userprofile.request'));
                this.ttipCB.setTitle(WtfGlobal.getLocaleText('pm.member.request.rejectconn'));
            }
            break;
        case 2:
            //For Projects
            this.joinprojBttn = new Wtf.Toolbar.Button({
                id: this.id + "joinproj",
                stateId: Wtf.enumStat.RR,
                text: WtfGlobal.getLocaleText('pm.project.member.join.text'),
                iconCls: "dpwnd addppl",
                handler: this.confirmProjectStatusChange,//this.projchange,
                region: 'south',
                scope: this
            });
            this.rejprojBttn = new Wtf.Toolbar.Button({
                id: this.id + "rejproj",
                stateId: Wtf.enumStat.RR,
                text: WtfGlobal.getLocaleText('pm.userprofile.reject'),
                iconCls: "dpwnd addppl",
                handler: this.confirmProjectStatusChange,//this.projchange,
                region: 'south',
                scope: this
            });
            this.ComponentMainPanel.getBottomToolbar().insertButton(0, this.joinprojBttn);
            this.ComponentMainPanel.getBottomToolbar().insertButton(1, this.rejprojBttn);
            this.ttipCSB = new Wtf.ToolTip({
                id: this.id + 'tooltipCSB',
                title: WtfGlobal.getLocaleText('pm.project.member.join'),
                target: this.joinprojBttn.id
            });
            this.ttipRRB = new Wtf.ToolTip({
                id: this.id + 'tooltipRRB',
                title: WtfGlobal.getLocaleText('pm.member.request.reject'),
                target: this.rejprojBttn.id
            });
            if (!resultobj.getCount() == 0) {
                switch (resultobj.getAt(0).data['connstatus']) {
                    case 0:
                        //Not member
                        this.projStat = 1;
                        this.rejprojBttn.hide();
                        this.joinprojBttn.stateId = Wtf.enumStat.JC;
                        this.joinprojBttn.setText(WtfGlobal.getLocaleText('pm.project.member.join.text'));
                        this.ttipCSB.setTitle(WtfGlobal.getLocaleText('pm.project.member.join'));
                        break;

                    case 1:
                        //Request either sent or received
                        if (resultobj.getAt(0).data['id1'] == loginid) {
                            this.projStat = 3;
                            this.rejprojBttn.hide();
                            this.joinprojBttn.stateId = Wtf.enumStat.WFA;
                            this.joinprojBttn.setText(WtfGlobal.getLocaleText('pm.userprofile.waiting'));
                            this.ttipCSB.setTitle(WtfGlobal.getLocaleText('pm.userprofile.waiting'));
                            this.joinprojBttn.setDisabled(true);
                            this.joinprojBttn.show();
                        } else {
                            this.projStat = 3;
                            this.rejprojBttn.show();
                            this.joinprojBttn.stateId = Wtf.enumStat.AR;
                            this.joinprojBttn.setText(WtfGlobal.getLocaleText('pm.userprofile.approve'));
                            this.ttipCSB.setTitle(WtfGlobal.getLocaleText('pm.project.home.approvecon.tip'));
                            this.rejprojBttn.stateId = Wtf.enumStat.JC;
                            this.rejprojBttn.setText(WtfGlobal.getLocaleText('pm.userprofile.reject'));
                            this.ttipRRB.setTitle(WtfGlobal.getLocaleText('pm.member.request.reject'));
                        }
                        break;

                    case 2:
                        //Invitation
                        if (resultobj.getAt(0).data['id1'] == loginid) {
                            this.projStat = 3;
                            this.rejprojBttn.show();
                            this.joinprojBttn.stateId = Wtf.enumStat.AI;
                            this.joinprojBttn.setText(WtfGlobal.getLocaleText('pm.project.home.acceptinvite'));
                            this.ttipCSB.setTitle(WtfGlobal.getLocaleText('pm.project.home.acceptinvite.tip'));
                            this.rejprojBttn.stateId = Wtf.enumStat.RI;
                            this.rejprojBttn.setText(WtfGlobal.getLocaleText('pm.project.home.rejectinvite'));
                            this.ttipRRB.setTitle(WtfGlobal.getLocaleText('pm.project.home.rejectinvite.tip'));
                        } else {
                            this.projStat = 1;
                            this.rejprojBttn.hide();
                            this.joinprojBttn.stateId = Wtf.enumStat.JC;
                            this.joinprojBttn.setText(WtfGlobal.getLocaleText('pm.project.member.join.text'));
                            this.ttipCSB.setTitle(WtfGlobal.getLocaleText('pm.project.member.join'));
                        }
                        break;

                    case 3:
                        //Member
                        this.projStat = 0;
                        this.rejprojBttn.hide();
                        this.joinprojBttn.stateId = Wtf.enumStat.LC;
                        this.joinprojBttn.setText(WtfGlobal.getLocaleText('pm.project.home.leave'));
                        this.ttipCSB.setTitle(WtfGlobal.getLocaleText('pm.project.home.leave.tip'));
                        break;

                    case 4:
                        //Owner
                        this.rejprojBttn.hide();
                        this.joinprojBttn.hide();

                        break;

                    case 5:
                        //Moderator
                        this.rejprojBttn.hide();
                        this.joinprojBttn.hide();
                        break;
                }
                if (resultobj.getAt(0).data.archived) {
                    this.rejprojBttn.hide();
                    this.joinprojBttn.hide();
                    Wtf.get(this.about.tagbutton2).remove();
                    this.edt_tag.hide();
                }
            } else {
                this.projStat = 1;
                this.rejprojBttn.hide();
                this.joinprojBttn.stateId = Wtf.enumStat.JC;
                this.joinprojBttn.setText(WtfGlobal.getLocaleText('pm.project.member.join.text'));
                this.ttipCSB.setTitle(WtfGlobal.getLocaleText('pm.project.member.join'));
            }
            break;
        }
        if(this.uid.type==Wtf.etype.proj){
            this.addCheckBoxes();
        }
        this.setNotifSubValues(resultobj.getAt(0));
    },

    setNotifSubValues: function(pdata){
        var pnl = Wtf.getCmp(this.id+'networkpanel').body;
        if(this.uid.type==Wtf.etype.proj){
            if(WtfGlobal.getCompanyNotificationSubscription()){
                if(WtfGlobal.getNotificationSubscription()){
                    if(pdata){
                        pdata = pdata.data;
                        if(pdata.connstatus !== 1 && pdata.connstatus !== 2){
                            var subVal = pdata.notifSubVal;
                            for(var c = 1; c < 4; c++){
                                var temp = Math.pow(2, c);
                                var check = Wtf.getCmp(this.id+'_'+temp);
                                if(check && !WtfGlobal.EnableDisable(subVal, temp)){
                                    check.setValue(true);
                                }
                            }
                        } else {
                            this.removeNotificationPanel(pnl);
                        }
                        temp = Wtf.getCmp("notsubpanel" + this.id);
                        if(temp){
                            var t = Wtf.getCmp(temp.id+'notifText');
                            if(t)
                                t.getEl().dom.innerHTML = '';
                            if(pdata.archived)
                                temp.setDisabled(true);
                            else
                                temp.setDisabled(false);
                        }
                    } else {
                        this.removeNotificationPanel(pnl);
                    }
                } else {
                    if(pdata){
                        temp = Wtf.getCmp("notsubpanel" + this.id);
                        t = Wtf.getCmp(temp.id+'notifText');
                        if(t)
                            t.getEl().dom.innerHTML = '<div class="notifCheckTextProfile">'+WtfGlobal.getLocaleText('pm.project.home.notifications.info2')+'</div>';
                        else
                            temp.add({
                                id: temp.id+'notifText',
                                html:'<div class="notifCheckTextProfile">'+WtfGlobal.getLocaleText('pm.project.home.notifications.info2')+'</div>'
                            });
                        temp.setDisabled(true);
                        temp.doLayout();
                    } else {
                        this.removeNotificationPanel(pnl);
                    }
                }
            } else {
                if(pdata){
                    temp = Wtf.getCmp("notsubpanel" + this.id);
                    t = Wtf.getCmp(temp.id+'notifText');
                    if(t)
                        t.getEl().dom.innerHTML = '<div class="notifCheckTextProfile">'+WtfGlobal.getLocaleText('pm.project.home.notifications.info1')+'</div>';
                    else
                        temp.add({
                            id: temp.id+'notifText',
                            html:'<div class="notifCheckTextProfile">'+WtfGlobal.getLocaleText('pm.project.home.notifications.info1')+'</div>'
                        });
                    temp.setDisabled(true);
                    temp.doLayout();
                } else {
                    this.removeNotificationPanel(pnl);
                }
            }
        } else {
            this.removeNotificationPanel(pnl);
        }
    },

    removeNotificationPanel: function(pnl){
        var cmp = Wtf.getCmp("notsubpanel" + this.id);
        if(cmp)
            cmp.destroy();
        if(pnl)
            pnl.dom.style.borderBottom = '0px solid red';
    },

    addCheckBoxes: function(){
        var fs = Wtf.getCmp("notsubpanel" + this.id);
        if(fs){
            fs = fs.items.items[0];
            if(fs){
                var temp = Math.pow(2, 1);
                var check = Wtf.getCmp(this.id+'_'+temp);
                var styl = 'margin-right:5px;';
                styl += (!Wtf.isIE) ? 'margin-bottom:2px;' : '';
                if(!check && Wtf.subscription.proj.subscription && Wtf.featuresView.proj){
                    fs.add({
                        xtype : "checkbox",
                        boxLabel : WtfGlobal.getLocaleText('pm.common.projectplan'),
                        id : this.id + '_2',
                        height: 18,
                        checked : false,
                        style: styl
                    });
                }
                temp = Math.pow(2, 2);
                check = Wtf.getCmp(this.id+'_'+temp);
                if(!check && Wtf.featuresView.todo){
                    fs.add({
                        xtype : "checkbox",
                        boxLabel : WtfGlobal.getLocaleText('pm.project.todo.text'),
                        id : this.id + '_4',
                        height: 18,
                        checked : false,
                        style: styl
                    });
                }
                temp = Math.pow(2, 3);
                check = Wtf.getCmp(this.id+'_'+temp);
                if(!check && Wtf.subscription.cal.subscription && Wtf.featuresView.cal){
                    fs.add({
                        xtype : "checkbox",
                        boxLabel : WtfGlobal.getLocaleText('pm.common.calendar'),
                        id : this.id + '_8',
                        height: 18,
                        checked : false,
                        style: styl
                    });
                }
                fs.doLayout();
            }
        }
    }
});
/*  WtfProfileView: End     */

/*  WtfPanelView: Start */
Wtf.PanelView = function(config){
    Wtf.apply(this, config);

    Wtf.PanelView.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.PanelView, Wtf.Panel, {
    flag1: 0,
    flag2: 0,
    store: null,

    initComponent: function(){
        Wtf.PanelView.superclass.initComponent.call(this);
        this.addEvents({
            'panelready': true
        });
        var reader = null;
        if (this.uid.type == Wtf.etype.user) {
            var RECORD = Wtf.data.Record.create([{
                name: 'name'
            }, {
                name: 'id'
            }, {
                name: 'img'
            }, {
                name: 'description'
            }, {
                name: 'status'
            }, {}, {
                name: 'o'
            }, {
                name: 'email'
            }, {
                name: 'address'
            }, {
                name: 'phone'
            }]);
        } else {
            var recConfig = [{
                name: 'name'
            }, {
                name: 'id'
            }, {
                name: 'img'
            }, {
                name: 'description'
            }, {
                name: 'status'
            }, {
                name: 'members'
            }, {
                name: 'createdon',
                type: 'date',
                dateFormat: 'Y-m-d H:i:s'
            },{
                name:'health'
            }];
            recConfig =getStoreFields(recConfig);
            RECORD = Wtf.data.Record.create(recConfig);
        }
        reader = new Wtf.data.KwlJsonReader({
            root: 'data'
        },RECORD);
        this.view30 = new Wtf.common.KWLListPanel({
            id: this.id + "view30",
            title: " ",
            style: "padding: 16px;",
            reader: reader,
            autoLoad: false,
            autoScroll: true,
            border: false,
            TabType: this.tabType,
            useFlag: 'network',
            layout:'fit'
        });
        this.view30.on('dataloaded', this.dataloaded, this);
        this.view30.on('afterDataRender', this.dataRendered, this);
        //        this.arrangeMenu = new Wtf.menu.Menu({
        //            id: 'arrangeMenu' + this.id,
        //            items: [{
        //                text: WtfGlobal.getLocaleText('lang.name.text'),
        //                handler: this.arrange
        //
        //            }, {
        //                text: WtfGlobal.getLocaleText('pm.project.plan.reports.activity.text'),
        //                handler: this.arrange
        //
        //            }, {
        //                text: WtfGlobal.getLocaleText('pm.common.preference'),
        //                handler: this.arrange
        //
        //            }]
        //        });
        var txt = WtfGlobal.getLocaleText('pm.common.searchbyname');
        if(this.uid.type== Wtf.etype.proj){
            txt = WtfGlobal.getLocaleText('pm.admin.project.searchbyprojectname')
        }
        this.quickPanelSearch = new Wtf.KWLQuickSearch({
            field: 'name',
            id : 'searchinnetwork'+this.id,
            width: 200,
            emptyText: txt
        })
        this.quickPanelSearch.on('panelready', this.check1);
        this.quickPanelSearch.on('SearchComplete', this.QuickSearchComplete, this);
        switch (this.uid.type) {
            case Wtf.etype.comm:
                this.setPanelDetails("My Communities", "projectimage.jpg", "../../community.html", Wtf.req.prf + 'common/getUserCommunities.jsp?login=');
                break;

            case Wtf.etype.user:
                this.setPanelDetails(WtfGlobal.getLocaleText('My.Network'), "user100.png", "../../user.html", Wtf.req.prf + 'common/getFriendList.jsp?login=');
                break;

            case Wtf.etype.proj:
                if(!this.archived)
                    this.setPanelDetails( WtfGlobal.getLocaleText('pm.dashboard.widget.quicklinks.myprojects'), "projectimage100.png", "../../project.html", Wtf.req.prf + 'dashboard/getProjList.jsp?login=');
                else
                    this.setPanelDetails(WtfGlobal.getLocaleText('pm.dashboard.widget.quicklinks.archivedprojects'), "projectimage100.png", "../../project.html", Wtf.req.prj + 'archive.jsp?mode=2&login=');
                break;
        }
    },

    check1: function(){
        if (this.flag1 == 1 && this.flag2 == 1) {
            this.quickPanelSearch.StorageChanged(this.store);
            this.view30.updateEmptyText(true);
        }
    },

    refresh: function(item, pressed){
        if(Wtf.getCmp('network_icon'+this.id).pressed) {
            if(this.tabType == Wtf.etype.proj || this.tabType == Wtf.etype.user){
                this.quickPanelSearch.setValue("");
                this.view30.calculatePageSize();
                this.view30.prevPageSize = 0;
                this.view30.checkForReq();
            }
        } else if(Wtf.getCmp('network_detail'+this.id).pressed){
            this.quickPanelSearch.setValue("");
            this.store.reload({params:{ss1:this.quickPanelSearch.getValue()}});
        }
    },

    /*arrange: function(item, pressed){
    },*/

    setPanelDetails: function(heading, defaultimg, filetype, jspfile){
        this.view30.title = heading;
        this.view30.ImgSrc = defaultimg;
        this.view30.Href = filetype;
        this.view30.url = jspfile + loginid;
    },

    onRender: function(config){
        Wtf.PanelView.superclass.onRender.call(this, config);
        var bbar =[];
        if(this.archived!=null &&  !this.archived) {
            bbar.push({
                text: WtfGlobal.getLocaleText('pm.dashboard.widget.quicklinks.archivedprojects'),
                iconCls: "pwnd arrange",
                id: 'archivedetail',
                handler : function() {navigate('armp')}
            });
            bbar.push('-');
        }
        bbar.push("->");
        bbar.push({
            text: WtfGlobal.getLocaleText('pm.common.refresh'),
            id : 'refreshnetwork'+this.id,
            iconCls: "pwnd refresh",
            scope: this,
            handler: this.refresh
        });
        this.innerPanel = this.add(new Wtf.Panel({
            id: this.id + "innerpanel",
            layout: "fit",
            border: false,
            items: this.view30,
            tbar: [/*'Quick Search: ',*/ this.quickPanelSearch, '->', this.iconBtn = new Wtf.Button({
                text: WtfGlobal.getLocaleText('pm.common.icons'),
                id : 'network_icon'+this.id,
                enableToggle: true,
                toggleGroup: this.id,
                pressed: true,
                scope: this,
                handler: this.toggleIconView,
                tooltip:{
                    title: WtfGlobal.getLocaleText('pm.project.plan.iconview'),
                    text: WtfGlobal.getLocaleText('pm.project.home.iconview')
                }
            }), this.detailBtn = new Wtf.Button({
                text: WtfGlobal.getLocaleText('lang.details.text'),
                id : 'network_detail'+this.id,
                enableToggle: true,
                toggleGroup: this.id,
                scope: this,
                handler: this.toggleDetailView,
                tooltip:{
                    title: WtfGlobal.getLocaleText('pm.common.details.view'),
                    text: WtfGlobal.getLocaleText('pm.project.plan.todetaol')
                }
            })],
            bbar:bbar

        }));
    },

    loadData: function(){
        hideMainLoadMask();
        this.view30.calculatePageSize();
        this.view30.checkForReq();
        this.flag1 = 1;
        this.fireEvent('panelready', this.flag1);
    },

    dataloaded: function(str){
        this.store = str;
        this.flag2 = 1;
        this.check1();
    },

    dataRendered: function(){
        this.fireEvent('panelready', this.flag1);
    },

    QuickSearchComplete: function(str){
        if(Wtf.getCmp('network_icon'+this.id).pressed) {
            if(this.tabType == Wtf.etype.proj || this.tabType == Wtf.etype.user ){
                this.view30.calculatePageSize();
                this.view30.prevPageSize = 0;
                this.view30.checkForReq(this.quickPanelSearch.getValue());
            }
        } else if(Wtf.getCmp('network_detail'+this.id).pressed){
            this.store.reload({params:{ss1:this.quickPanelSearch.getValue()}});
        }

        //this.store = str;
        //this.view30.fillwithstore(this.store);
    },

    toggleIconView: function(){
        Wtf.getCmp(this.view30.id).getEl().dom.style.display = 'block';
        this.innerPanel.remove(this.detailview);
//        this.innerPanel.add(this.view30);
        this.innerPanel.doLayout();
    },

    toggleDetailView: function(){
        var cmConfig=[];
        if (!Wtf.getCmp("detailview" + this.id)) {
            if (this.tabType == Wtf.etype.user) {
               cmConfig=[{
                        header: "",
                        width: 50,
                        dataIndex: 'img',
                        renderer: function(record){
                            if (record == "") {
                                record = Wtf.DEFAULT_USER_URL;
                            }
                            return String.format("<img height='18px' width='18px' src='{0}'/>", record);
                        }
                    }, {
                        header: WtfGlobal.getLocaleText('lang.name.text'),
                        width: 120,
                        sortable: true,
                        dataIndex: 'name'
                    }, {
                        header: WtfGlobal.getLocaleText('lang.email.text'),
                        width: 120,
                        sortable: true,
                        renderer: WtfGlobal.renderEmailTo,
                        dataIndex: 'email'
                    }, {
                        header: WtfGlobal.getLocaleText('lang.about.text'),
                        width: 400,
                        sortable: true,
                        dataIndex: 'description'
                    }, {
                        header: WtfGlobal.getLocaleText('lang.address.text'),
                        width: 120,
                        sortable: true,
                        dataIndex: 'address'
                    }, {
                        header: WtfGlobal.getLocaleText('pm.common.phone'),
                        width: 120,
                        sortable: true,
                        renderer: WtfGlobal.renderContactToSkype,
                        dataIndex: 'phone'
                    }, {
                        header: WtfGlobal.getLocaleText('lang.status.text'),
                        width: 120,
                        sortable: true,
                        dataIndex: 'status',
                        renderer: function(record, val, row){
                            if (record == "1") {
                                if (row.get("o") == "i") {
                                    return WtfGlobal.getLocaleText('pm.request.sent');
                                }
                                return WtfGlobal.getLocaleText('pm.invitation.recieve');
                            } else if (record == "2") {
                                return WtfGlobal.getLocaleText('pm.invitation.recieve');
                            }
                            return WtfGlobal.getLocaleText('Friend.text');
                        }
                    }];
               this.detailview = new Wtf.grid.GridPanel({
                    border: false,
                    loadMask: true,
                    id: "detailview" + this.id,
                    store: this.store,
                    viewConfig: {
                        forceFit: true,
                        emptyText:'<div class="emptyGridTextForPanelView">'+WtfGlobal.getLocaleText('pm.grid.view.emptytext')+' </div>'
                    },
                    autoHeight: true,
                    enableColumnHide: false,
                    columns: cmConfig
                });
            }
        else {
            cmConfig = [{
                    header: "",
                    width: 40,
                    dataIndex: 'img',
                    renderer: function(record){
                        if (record == "") {
                            record = "../../images/projectimage.jpg";
                        }
                        return String.format("<img height='18px' width='18px' src='{0}'/>", record);
                    }
                }, {
                    header: WtfGlobal.getLocaleText('pm.createeditproject.projectname'),
                    width: 110,
                    sortable: true,
                    dataIndex: 'name'
                }, {
                    header: WtfGlobal.getLocaleText('lang.about.text'),
                    width: 350,
                    sortable: true,
                    dataIndex: 'description'
                }, {
                    header: WtfGlobal.getLocaleText('pm.admin.project.members'),
                    width: 60,
                    sortable: true,
                    dataIndex: 'members'
                }, {
                    header: WtfGlobal.getLocaleText('lang.status.text'),
                    width: 80,
                    sortable: true,
                    dataIndex: 'status',
                    renderer: function(value){
                        if(value == 1) {
                            return WtfGlobal.getLocaleText('pm.member.requested');
                        } else if(value == 2) {
                            return WtfGlobal.getLocaleText('Invited.text');
                        } else if(value == 3) {
                            return WtfGlobal.getLocaleText('Member.text');
                        } else if(value == 4) {
                            return WtfGlobal.getLocaleText('Moderator.text');
                        }
                    }
                }, {
                    header: WtfGlobal.getLocaleText('pm.common.createdon'),
                    renderer:function(value,p,record) {
                        if(value!=null&&value!=""){
                            return value.format(WtfGlobal.getDateFormat());
                        }
                        else return "";
                    },
                    width: 120,
                    sortable: true,
                    dataIndex: 'createdon'
                }
//				  ,{
//                    header :WtfGlobal.getLocaleText('pm.common.healthstatus'),
//                    dataIndex: 'health',
//                    renderer:function(val){
//                        if(val==1) return "<div><img src='../../images/health_status/ontime.gif' style='vertical-align:text-bottom'/>"+WtfGlobal.getLocaleText('pm.project.home.health.ontime')+"</div>";
//                        else if(val==2) return "<div><img src='../../images/health_status/slightly.gif' style='vertical-align:text-bottom'/>"+WtfGlobal.getLocaleText('pm.project.home.health.slightly')+"</div>";
//                        else if(val==3) return "<div><img src='../../images/health_status/gravely.gif' style='vertical-align:text-bottom'/>"+WtfGlobal.getLocaleText('pm.project.home.health.gravely')+"<div>";
//                       else return "<div><img src='../../images/health_status/else_status.gif' style='vertical-align:text-bottom'/>"+WtfGlobal.getLocaleText('pm.project.home.health.future')+"<div>";
//                    }
//                }
				];
//            cmConfig = getCcColModel(cmConfig,"Project");
            this.detailview = new Wtf.grid.GridPanel({
                border: false,
                id: "detailview" + this.id,
                store: this.store,
                loadMask: true,
                viewConfig: {
                    forceFit: true,
                    emptyText:'<div class="emptyGridTextForPanelView">'+WtfGlobal.getLocaleText('pm.grid.view.emptytext')+'</div>'
                },
                enableColumnHide: false,
                autoHeight: true,
                columns: cmConfig
            });
        }
        this.detailview.on("rowdblclick", this.GridRowdbClicked, this);
    }

    Wtf.getCmp(this.view30.id).getEl().dom.style.display = 'none';
//    this.innerPanel.remove(this.view30);
    this.innerPanel.add(this.detailview);
    this.innerPanel.doLayout();
},

GridRowdbClicked: function(Grid, rowIndex, e){
    switch (this.tabType) {
        case Wtf.etype.user:
            mainPanel.loadTab("../../user.html", "tab" + this.store.getAt(rowIndex).get("id"), this.store.getAt(rowIndex).get("name"), "navareadashboard", Wtf.etype.user, true);
            break;

        case Wtf.etype.comm:
            mainPanel.loadTab("../../community.html", "tab" + this.store.getAt(rowIndex).get("id"), this.store.getAt(rowIndex).get("name"), "navareadashboard", Wtf.etype.comm, true);
            break;

        case Wtf.etype.proj:
            mainPanel.loadTab("../../project.html", "tab" + this.store.getAt(rowIndex).get("id"), this.store.getAt(rowIndex).get("name"), "navareadashboard", Wtf.etype.proj, true);
            break;
    }
}
});
/*  WtfPanelView: End   */


/*  Wtf.Profile: Start  */
Wtf.Profile = function(){
    config = {
        title: WtfGlobal.getLocaleText('pm.user.updateprofile'),
        id: "pprofwin",
        closable: true,
        modal: true,
        iconCls: 'iconwin',
        width: 460,
        height: standAlone ? 400 : 350,
        resizable: false,
        layout: 'border',
        buttonAlign: 'right',
        renderTo: document.body,
        buttons: [{
            text: WtfGlobal.getLocaleText('lang.update.text'),
            scope: this,
            handler: function(){
                if(!(this.Form.form.isValid()))
                    return;

                this.Form.form.submit({
                    waitMsg: WtfGlobal.getLocaleText("pm.loading.text") + '...',
                    scope: this,
                    failure: function(frm, action){
                        msgBoxShow(155, 1);
                        this.close();
                    },
                    success: function(frm, action){
                        var userRes = WtfGlobal.getLocaleText('pm.msg.profile.success');
                        var resObj = eval("(" + action.response.responseText + ")");
                        if(resObj.success == "true") {
                            if(resObj.data != "")
                                userRes += resObj.data;//message prepared on server side
                            msgBoxShow([WtfGlobal.getLocaleText('lang.success.text'), userRes], 3);
                            var dateFormat = Wtf.dateFormatStore.getAt(Wtf.dateFormatStore.find("id",this.dtCombo.getValue()));
                            Wtf.pref.DateFormatid = dateFormat.data.id;
                            Wtf.pref.DateFormat = dateFormat.data.dateformat;
                            Wtf.pref.DateFormatSeparatorPosition = dateFormat.data.seppos;
                            Wtf.pref.Notification = frm.findField('notificationcheck').el.dom.checked;
                        }
                        else
                        {
                            msgBoxShow(155, 1);
                        }
                        this.close();
                    }
                });
            }
        }, {
            text: WtfGlobal.getLocaleText('lang.cancel.text'),
            scope: this,
            handler: function(){
                this.close();
            }
        }]
    }
    Wtf.Profile.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.Profile, Wtf.Window, {
    initComponent: function(config){
        if(!Wtf.StoreMgr.containsKey("dateformat")){
            Wtf.dateFormatStore.load();
            Wtf.StoreMgr.add("dateformat", Wtf.dateFormatStore);
        }

        if(!Wtf.StoreMgr.containsKey("timezone")){
            Wtf.timezoneStore.load();
            Wtf.StoreMgr.add("timezone", Wtf.timezoneStore);
        }

        if(!Wtf.StoreMgr.containsKey("country")) {
            Wtf.countryStore.load();
            Wtf.StoreMgr.add("country", Wtf.countryStore);
        }

        Wtf.Profile.superclass.initComponent.call(this, config);
        Wtf.Ajax.requestEx({
            url: Wtf.req.prf + "user/getuserdetails.jsp",
            params : {
                mode: 1
            },
            method: 'POST'},
        this,
        function(result, request) {
            try {
                var data = result;
                if (data) {
                    this.createForm(Wtf.util.JSON.decode(data).data[0]);
                }
            }
            catch (e) {

            }
        },
        function(result, request) {
            msgBoxShow(156, 1);
        /*Wtf.MessageBox.Show({
                    title: WtfGlobal.getLocaleText('lang.error.text'),
                    msg: 'Error Retrieving Personal Information.',
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.INFO
                });*/
        });
    //
    //        Wtf.Ajax.request({
    //            method: "POST",
    //            url: Wtf.req.prf + "user/getuserdetails.jsp?",
    //            params: {
    //                mode: 1
    //            },
    //            success: function(result, request){
    //                try {
    //                    var data = result.responseText.trim();
    //                    if (data) {
    //                        this.createForm(Wtf.util.JSON.decode(data).data[0]);
    //                    }
    //                }
    //                catch (e) {
    //
    //                }
    //            },
    //            failure: function(){
    //                Wtf.MessageBox.Show({
    //                    title: WtfGlobal.getLocaleText('lang.error.text'),
    //                    msg: 'Error Retrieving Personal Information.',
    //                    buttons: Wtf.MessageBox.OK,
    //                    icon: Wtf.MessageBox.INFO
    //                });
    //            },
    //            scope: this
    //        });
    },
    createForm: function(params){
        this.Form = new Wtf.form.FormPanel({
            method: 'POST',
            url: Wtf.req.prf + 'user/updateProfile.jsp?action=1',
            waitMsgTarget: true,
            fileUpload: true,
            border: false,
            labelWidth: 120,
            cls: 'scrollform',
            defaults: {
                width: 240
            },
            defaultType: 'textfield',
            items: [{
                fieldLabel: 'User ID*',
                name: 'username',
                disabled :true,
                allowBlank: false,
                value: params.username,
                hidden: true,
                itemCls: 'hideFormField',
                hideLabel: true
            }, {
                fieldLabel: 'Email Address*',
                allowBlank: false,
                name: 'emailid',
                vtype: 'email',
                maxLength: 256,
                value: params.emailid,
                hidden: true,
                itemCls: 'hideFormField',
                hideLabel: true
            }, {
                fieldLabel: 'First Name*',
                name: 'fname',
                maxLength: 50,
                validator:WtfGlobal.validateUserName,
                allowBlank: false,
                value: params.fname,
                hidden: true,
                itemCls: 'hideFormField',
                hideLabel: true
            }, {
                fieldLabel: 'Last Name*',
                maxLength: 50,
                name: 'lname',
                allowBlank: false,
                validator:WtfGlobal.validateUserName,
                value: params.lname,
                hidden: true,
                itemCls: 'hideFormField',
                hideLabel: true
            }, {
                fieldLabel: WtfGlobal.getLocaleText('pm.contact.number'),
                name: 'contactno',
                maxLength: 15,
                id: 'updateContactNo',
                value: params.contactno,
                hidden: true,
                itemCls: 'hideFormField',
                hideLabel: true
            },{
                xtype:"textarea",
                fieldLabel: WtfGlobal.getLocaleText('lang.address.text'),
                height: 80,
                maxLength: 100,
                name: 'address',
                id: 'updateAddress',
                value: params.address,
                hidden: true,
                itemCls: 'hideFormField',
                hideLabel: true
            },{
                fieldLabel: WtfGlobal.getLocaleText('pm.updateprofile.designation'),
                maxLength: 15,
                name: 'designation',
                id: 'updateDesig',
                value: params.desig
            },{
                xtype:"textarea",
                fieldLabel: WtfGlobal.getLocaleText('pm.updateprofile.aboutme'),
                maxLength: 512,
                id: 'updateAboutMe',
                height: 80,
                name: 'about',
                value: params.aboutuser
            },this.dtCombo=new Wtf.form.ComboBox({
                xtype:'combo',
                fieldLabel : WtfGlobal.getLocaleText('pm.updateprofile.dateformat'),
                store : Wtf.dateFormatStore,
                displayField:'name',
                valueField:'id',
                value: Wtf.pref.DateFormatid,
                forceSelection:true,
                typeAhead: false,
                editable: false,
                selectOnFocus: true,
                hiddenName: 'dateformat',
                mode: 'remote',
                width: 240,
                triggerAction: 'all',
                emptyText : 'Select a type...',
                allowBlank:false
            }),
            this.locale = new Wtf.form.ComboBox({
                fieldLabel:WtfGlobal.getLocaleText('lang.timezone.tex'),
                mode:'local',
                scope: this,
                triggerAction:'all',
                typeAhead:true,
                value: params.timezone,
                selectOnFocus:true,
                forceSelection : true,
                hiddenName: 'timezone',
                blankText: WtfGlobal.getLocaleText('pm.common.timezone.select'),
                store: Wtf.timezoneStore,
                displayField:'name',
                valueField:'id',
                hidden: true,
                itemCls: 'hideFormField',
                hideLabel: true
            }),
            this.locale = new Wtf.form.ComboBox({
                fieldLabel : WtfGlobal.getLocaleText('lang.country.text'),
                mode : 'local',
                scope : this,
                triggerAction : 'all',
                typeAhead : true,
                value : params.country,
                forceSelection : true,
                hiddenName : 'country',
                blankText : WtfGlobal.getLocaleText('pm.common.country.emptytext'),
                store : Wtf.countryStore,
                displayField : 'name',
                selectOnFocus:true,
                valueField :'id',
                hidden: true,
                itemCls: 'hideFormField',
                hideLabel: true
            }),{
                fieldLabel: WtfGlobal.getLocaleText('pm.user.picture'),
                name: 'image',
                height: 24,
                inputType: 'file',
                hidden: !standAlone,
                hideLabel: !standAlone,
                itemCls: !standAlone ? 'hideFormField' : '',
                validator: WtfGlobal.validateImageFile,
                invalidText: WtfGlobal.getLocaleText("pm.common.validateimage.invalidtext")
            }, {
                inputType: 'checkbox',
                fieldLabel: WtfGlobal.getLocaleText('pm.project.notifiction.email'),
                name: 'notification',
                id: 'notificationcheck',
                width:'16px',
                checked: params.notification,
                style: (Wtf.isIE6 || Wtf.isIE7) ? '': 'margin-top:5px;'
            }]
        });
        Wtf.getCmp("updateContactNo").on("change", function(){
            Wtf.getCmp("updateContactNo").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("updateContactNo").getValue()));
        });
        Wtf.getCmp("updateAddress").on("change", function(){
            Wtf.getCmp("updateAddress").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("updateAddress").getValue()));
        });
        Wtf.getCmp("updateAboutMe").on("change", function(){
            Wtf.getCmp("updateAboutMe").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("updateAboutMe").getValue()));
        });
        var parentP = Wtf.getCmp("profilewin");
        if (parentP) {
            parentP.add(this.Form);
            this.doLayout();
        }
        focusOn('updateDesig');
        Wtf.get('notificationcheck').dom.checked = params.notification;
        var compSub = WtfGlobal.getCompanyNotificationSubscription();
        Wtf.getCmp('notificationcheck').setDisabled(!compSub);
        if(!compSub){
            var text = new Wtf.Element(document.createElement("div"));
            text.addClass("notifCheckText");
            text.dom.innerHTML = WtfGlobal.getLocaleText('pm.updateprofile.email.info');
            text.insertAfter(Wtf.get('notificationcheck'));
        }
    },
    onRender: function(config){
        Wtf.Profile.superclass.onRender.call(this, config);
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(WtfGlobal.getLocaleText('pm.user.updateprofile'),WtfGlobal.getLocaleText('pm.updateprofile.headersubtext'),"../../images/edituser40_52.gif")
        }, {
            region: 'center',
            border: false,
            id: 'profilewin',
            bodyStyle: 'background:#f1f1f1;font-size:10px;',
            layout: 'fit',
            autoScroll: true,
            items: this.Form
        });
    }
});/*  Wtf.Profile: End*/

/*  Wtf.ChangePassword: Start  */
Wtf.ChangePassword = function(){
    config = {
        title: WtfGlobal.getLocaleText('pm.changepassword.text'),
        id: "changePassword",
        closable: true,
        modal: true,
        iconCls: 'iconwin',
        width: 400,
        height: 260,
        resizable: false,
        layout: 'border',
        buttonAlign: 'right',
        renderTo: document.body,
        buttons: [{
            text: WtfGlobal.getLocaleText('lang.set.text'),
            scope: this,
            handler: function(btn){
                if(this.changePasswordForm.form.isValid()){
                    var opass = hex_sha1(Wtf.getCmp('opass').getValue());
                    var pass = hex_sha1(Wtf.getCmp('pass').getValue());
                    var rpass = hex_sha1(Wtf.getCmp('rpass').getValue());
                    if(pass != rpass){
                        msgBoxShow(192, 1);
                        return;
                    }
                    var param={
                        pass:pass,
                        opass:opass,
                        rpass:rpass
                    }
                    Wtf.Ajax.requestEx({
                        url:Wtf.req.prf + 'user/updateProfile.jsp?action=2',
                        params:param
                    },this,
                    function(resp, result){
                        var obj =  Wtf.decode(resp);
                        var code = obj.flag;
                        switch(code){
                            case 1:msgBoxShow(132, 0);
                                    this.close();
                                    break;
                            case 2:msgBoxShow(193, 1);
                                    break;
                            case 3:msgBoxShow(192, 1);
                                    break;
                            case 4:msgBoxShow(131, 1);
                                    break;
                        }

                    },
                    function(resp, result){
                        msgBoxShow(131, 1);
                    });

                } else {
                    msgBoxShow(133, 1);
                }
            }
        }, {
            text: WtfGlobal.getLocaleText('lang.cancel.text'),
            scope: this,
            handler: function() {
                this.close();
            }
        }]
    }
    Wtf.ChangePassword.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.ChangePassword, Wtf.Window, {
    initComponent: function(config){
        Wtf.ChangePassword.superclass.initComponent.call(this, config);
        this.changePasswordForm = new Wtf.form.FormPanel({
            method: 'POST',
            url: Wtf.req.prf + 'user/updateProfile.jsp?action=2',
            waitMsgTarget: true,
            border: false,
            labelWidth: 150,
            bodyStyle : 'font-size:10px;',
            defaults : {
                inputType : 'password',
                maxLength:32,
                minLength:4,
                width:150,
                allowBlank:false
            },
            defaultType : 'textfield',
            items :[{
                fieldLabel:WtfGlobal.getLocaleText('pm.changepassword.oldpass')+'*',
                name:'opass',
                id: 'opass'
            }, {
                fieldLabel:WtfGlobal.getLocaleText('pm.changepassword.newpassword')+'*',
                name:'pass',
                id: 'pass'
            }, {
                fieldLabel:WtfGlobal.getLocaleText('pm.changepassword.confirm')+'*',
                name:'rpass',
                id: 'rpass'
            }
            ]
        });
    },

    onRender: function(config) {
        Wtf.ChangePassword.superclass.onRender.call(this, config);
        var styl = "float:right;"
        styl += (Wtf.isIE6) ? "margin-right: 0px !important;" : "";
        this.add({
            region : 'north',
            height : 75,
            border : false,
            bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
            layout: "fit",
            html : getTopHtml(WtfGlobal.getLocaleText('pm.changepassword.text'),WtfGlobal.getLocaleText('pm.changepassword.hint')+"<br><span style=\""+styl+"\">"+WtfGlobal.getLocaleText("pm.common.allfieldsarerequired")+"</span>","../../images/changepassword40_52.gif")
        }, {
            region: 'center',
            border: false,
            id: 'changePasswordWin',
            bodyStyle : 'background:#f1f1f1;font-size:10px;padding:20px 20px 20px 20px;',
            layout: 'fit',
            items: this.changePasswordForm
        });
    }
});/*  Wtf.ChangePassword: End*/

Wtf.data.KwlDataReader = function(meta, recordType){

    this.meta = meta;
    this.recordType = recordType instanceof Array ?
    Wtf.data.Record.create(recordType) : recordType;
};
Wtf.data.KwlDataReader.prototype = {

    };
Wtf.extend(Wtf.data.KwlDataReader, Wtf.util.Observable);

Wtf.data.KwlJsonReader = function(meta, recordType){
    meta = meta || {};
    Wtf.data.KwlJsonReader.superclass.constructor.call(this, meta, recordType);
    this.events = {
        aftereval : true
    };
    this.on("aftereval", this.jsonErrorResponseHandler);

};
Wtf.extend(Wtf.data.KwlJsonReader, Wtf.data.KwlDataReader, {
    read : function(response){
        var json = response.responseText;
        var o = eval("("+json+")");
        if (o && o.valid==false) {
            signOut("timeout");
        }
        else
            o = eval('(' + o.data + ')');
        if(!o) {
                    throw {message: "JsonReader.read: Json object not found"};
        }
        if(o.metaData){
            delete this.ef;
            this.meta = o.metaData;
            this.recordType = Wtf.data.Record.create(o.metaData.fields);
        //this.onMetaChange(this.meta, this.recordType, o);
        }


        //this.fireEvent("aftereval", o, this, response);
        return this.readRecords(o);
    },

    /*onMetaChange : function(meta, recordType, o){

       },*/

    jsonErrorResponseHandler:function (json, reader, response) {
        if (json && !json.valid) {
            signOut("timeout");
        }
    },

    simpleAccess: function(obj, subsc) {
        return obj[subsc];
    },

    getJsonAccessor: function(){
        var re = /[\[\.]/;
        return function(expr) {
            try {
                return(re.test(expr))
                ? new Function("obj", "return obj." + expr)
                : function(obj){
                    return obj[expr];
                };
            } catch(e){}
            return Wtf.emptyFn;
        };
    }(),

    readRecords : function(o){
        this.jsonData = o;
        var s = this.meta, Record = this.recordType,
        f = Record.prototype.fields, fi = f.items, fl = f.length;
        if (!this.ef) {
            if(s.totalProperty) {
                this.getTotal = this.getJsonAccessor(s.totalProperty);
            }
            if(s.successProperty) {
                this.getSuccess = this.getJsonAccessor(s.successProperty);
            }
	        this.getRoot = s.root ? this.getJsonAccessor(s.root) : function(p){return p;};
            if (s.id) {
                var g = this.getJsonAccessor(s.id);
                this.getId = function(rec) {
                    var r = g(rec);
                    return (r === undefined || r === "") ? null : r;
                };
            } else {
	        	this.getId = function(){return null;};
            }
            this.ef = [];
            for(var i = 0; i < fl; i++){
                f = fi[i];
                var map = (f.mapping !== undefined && f.mapping !== null) ? f.mapping : f.name;
                this.ef[i] = this.getJsonAccessor(map);
            }
        }
        var root = this.getRoot(o), c = root.length, totalRecords = c, success = true;
        if(s.totalProperty){
            var v = parseInt(this.getTotal(o), 10);
            if(!isNaN(v)){
                totalRecords = v;
            }
        }
        if(s.successProperty){
            var v = this.getSuccess(o);
            if(v === false || v === 'false'){
                success = false;
            }
        }
        var records = [];
        for(var i = 0; i < c; i++){
            var n = root[i];
            var values = {};
            var id = this.getId(n);
            for(var j = 0; j < fl; j++){
                f = fi[j];
                var v = this.ef[j](n);
                values[f.name] = f.convert((v !== undefined) ? v : f.defaultValue);
            }
            var record = new Record(values, id);
            record.json = n;
            records[i] = record;
        }
        return {
            success : success,
            records : records,
            totalRecords : totalRecords
        };
    }
});

//--------Add to Window------------------
Wtf.AddtoWindow = function(config){

    Wtf.apply(this, config);
    this.iconCls = 'iconwin';
    Wtf.AddtoWindow.superclass.constructor.call(this, config);
};


Wtf.extend(Wtf.AddtoWindow, Wtf.Window, {
    initComponent: function(){
        Wtf.AddtoWindow.superclass.initComponent.call(this);
        this.title = WtfGlobal.getLocaleText("pm.admin.user.assigntoprojects.subheader");
        this.modal=true;
        this.layout = 'fit';
        this.width = 450;
        this.height = 450;
        this.resizable = false;
    },

    onRender: function(config){
        Wtf.AddtoWindow.superclass.onRender.call(this, config);
        var buttonText = (this.wizard)?"Skip":WtfGlobal.getLocaleText('lang.cancel.text');
        this.searchFlag = false;
        var subTitle = (this.wizard)?"Step 3 of 3":WtfGlobal.getLocaleText('pm.admin.user.assigntoprojects.subheader');
        this.AddQuickSearch = new Wtf.KWLQuickSearch({
            id : 'AddToSearch'+this.id,
            width: 200,
            field : "name",
            emptyText: WtfGlobal.getLocaleText('pm.common.searchbyname')
        });
        this.listds = new Wtf.data.JsonStore({
            id : "listds"+this.id,
            url: '../../admin.jsp?action=0&companyid=' + companyid+'&userid='+this.userid,
            root: 'data',
            method : 'GET',
            baseParams :{mode: 5},
            fields: ['id', 'name']
        });
        this.listsm = new Wtf.grid.CheckboxSelectionModel();
        this.listcm = new Wtf.grid.ColumnModel([this.listsm,{
            header: WtfGlobal.getLocaleText('lang.name.text'),
            dataIndex: 'name',
            autoWidth : true,
//            width: 434,
            sortable: true,
            groupable: true
        }]);
        this.listGrid = new Wtf.grid.GridPanel({
            id:'list' + this.id,
            store: this.listds,
            cm: this.listcm,
            sm : this.listsm,
            border : false,
            autoWidth: true,
            height: 225,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText: '<div class="emptyGridText">'+WtfGlobal.getLocaleText('pm.prpject.member.assignall')+'</div>'
            }
        });
        this.innerpanel = this.add(new Wtf.Panel({
            layout: 'border',
            items: [{
                region : 'north',
                height : 75,
                border : false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                //                html : getTopHtml("Add To Project", "Add user to project as members"),
                html : getTopHtml(WtfGlobal.getLocaleText('pm.admin.user.assigntoprojects.header'), subTitle,"../../images/createuser40_52.gif")
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
                    tbar: [
                    /*'Search: ',*/
                    ' ',
                    /*                        new Wtf.form.ComboBox({
                                  id: "dropdown" + this.id,
                                  store : dataStoreuser,
                                  readOnly : true,
                                  width:75,
                                  displayField:'To',
                                  mode: 'local',
                                  triggerAction: 'all',
                                  forceSelection:true
                              }),*/' ',this.AddQuickSearch
                    ],
                    bbar: new Wtf.PagingToolbar({
                        pageSize: 20,
                        store : this.listds,
                        displayInfo: false,
                        //                              displayMsg: 'Topics {0} - {1} of {2}',
                        emptyMsg: WtfGlobal.getLocaleText('pm.project.grid.emptytext')
                    })
                })]
            }],
            buttons: [{
                text : WtfGlobal.getLocaleText('lang.assign.text'),
                id:'addUserToProjButton',
                scope: this,
                disabled: (this.listds.getCount() > 0) ? false : true,
                handler: function(){
                    if(this.listsm.getSelections().length > 0) {
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.334'), function(btn){
                            if (btn == "yes") {
                                Wtf.getCmp("addUserToProjButton").disable();
                                Wtf.getCmp("cancelUserToProjButton").disable();
                                this.addUsersTo(this.userid, this.wizard);
                            }
                        }, this);
                    }/* else {

//                        msgBoxShow(84, 1);
                    }*/
                }
            },{
                text : buttonText,
                scope : this,
                id:"cancelUserToProjButton",
                handler : function(){
                    var addToBtn = Wtf.getCmp("addto" + this.parentid);
                    if(addToBtn)
                        addToBtn.enable();
                    this.close();
                    if(this.wizard){
                        Wtf.getCmp(this.parentid).finishWindow();
                    }
                }
            }]
        }));

        this.on("close", function() {
            var addToBtn = Wtf.getCmp("addto" + this.parentid);
            if(addToBtn)
                addToBtn.enable();
        });

        this.listds.on('load', function(){
            if(this.listds.getCount() > 0){
                Wtf.getCmp("addUserToProjButton").enable();
            }
            this.AddQuickSearch.StorageChanged(this.listds);
            Wtf.getCmp("AddToSearch" + this.id).setValue("");
            if(!this.searchPerformed){
                if(this.listds.getCount() == 0){
                    this.searchPerformed = false;
                }
            }
        // this.show();
        }, this);

        this.AddQuickSearch.on("SearchComplete", function(store){
            this.emptyTextChange(store);
       }, this);
        this.panel.add(this.listGrid);
        this.panel.doLayout();
        this.listds.load();
    },

    emptyTextChange: function(store){
        var view = this.listGrid.getView();
        if(this.listGrid.getStore().getCount() > 0)
            view.emptyText = '<div class="emptyGridText">'+WtfGlobal.getLocaleText('pm.prpject.member.assignall')+'</div>';
        else
            view.emptyText = '<div class="emptyGridText">'+WtfGlobal.getLocaleText('pm.grid.view.emptytext')+'</div>';
        if(this.AddQuickSearch.getValue() != "")
            this.searchPerformed = true;
        view.refresh();
    },

    addUsersTo : function(userid, wizard){
        var userslist = "", featureidslist = "";
        var cnt;

        //  Get Selected Feature(Project/Community)
        var featureidsbuf = this.listsm.getSelections();
        for(cnt=0;cnt<featureidsbuf.length;cnt++){
            featureidslist += featureidsbuf[cnt].data['id'] + ","
        }
        featureidslist = featureidslist.substring(0,featureidslist.length - 1);

        if(userid !== undefined) {
            userslist = userid;
        } else {
            var useridsbuf = this.selectionModel.getSelections();
            for (cnt=0;cnt<useridsbuf.length;cnt++){
                userslist += useridsbuf[cnt].data['userid'] + ",";
            }
            userslist = userslist.substring(0,userslist.length - 1);
        }
        Wtf.Ajax.requestEx({
            url: "../../admin.jsp?",
            params: {
                action: 1,
                userslist: userslist,
                featureidslist: featureidslist,
                companyid: this.companyid,
                lid: loginid,
                feature: 0,
                mode: 0,
                emode: 3
            },
            method: 'POST'
        },
        this,
        function(result, req){
            this.close();
            if(result == "success"){
                var addToBtn = Wtf.getCmp("addto" + this.parentid);
                if(addToBtn)
                    addToBtn.enable();
                if(wizard){
                    Wtf.getCmp(this.parentid).finishWindow();
                } else
                    msgBoxShow(7, 0);
                if(this.usergrid)
                    this.usergrid.getStore().load();
                bHasChanged = true;
                var temp = refreshDash.join();
                if(temp.indexOf("all") == -1 && temp.indexOf('qkl') == -1)
                    refreshDash[refreshDash.length] = 'qkl';
            }
        },
        function(){
            msgBoxShow(8, 1);
            var addToBtn = Wtf.getCmp("addto" + this.parentid);
            if(addToBtn)
                addToBtn.enable();
            this.close();
            if (userid !== undefined){
                Wtf.getCmp(this.parentid).finishWindow();
            }
        });
    }
});

//--------Add to Window------------------

Wtf.Button.override({
    setTooltip: function(tooltip) {
        var btnEl = this.getEl().child(this.buttonSelector)
        Wtf.QuickTips.register(Wtf.apply({
            target: btnEl.id
        },tooltip));
    }
});

function addProjectWidgetOnDash(clickedId, pid, toAdd){
    Wtf.get(pid+'_'+clickedId).dom.style.display = 'none';
    if(clickedId.toString() == 'addSpan')
        Wtf.get(pid+'_removeSpan').dom.style.display = 'block';
    else
        Wtf.get(pid+'_addSpan').dom.style.display = 'block';
    addProjectWidget(pid, toAdd);
    msgBoxShow(13, 0);
}

function addProjectQlOnDash(clickedId, pid, toAdd){
    Wtf.get(pid+'_'+clickedId).dom.style.display = 'none';
    var isQl = false;
    if(clickedId.toString() == 'addQlSpan'){
        Wtf.get(pid+'_removeQlSpan').dom.style.display = 'block';
        isQl = true;
    } else {
        Wtf.get(pid+'_addQlSpan').dom.style.display = 'block';
        isQl = false;
    }
    var qlobj = new Object();
    qlobj[pid] = isQl;
    addProjectQl(Wtf.encode(qlobj), this);
}

function addProjectMtOnDash(clickedId, pid, toAdd){
    Wtf.get(pid+'_'+clickedId).dom.style.display = 'none';
    var isMt = false;
    if(clickedId.toString() == 'addMtSpan'){
        Wtf.get(pid+'_removeMtSpan').dom.style.display = 'block';
        isMt = true;
    } else {
        Wtf.get(pid+'_addMtSpan').dom.style.display = 'block';
        isMt = false;
    }
    var mtobj = new Object();
    mtobj[pid] = isMt;
    addMilestoneTimeline(Wtf.encode(mtobj), new Object(), this);
}
