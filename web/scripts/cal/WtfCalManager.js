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
Wtf.cal.oneDay=function(config){
    this.baseCls="oneDayPanelBase";
    Wtf.apply(this,config);
    Wtf.cal.oneDay.superclass.constructor.call(this);

};

Wtf.extend( Wtf.cal.oneDay,Wtf.Panel,{
//    initComponent : function(){
//        Wtf.cal.oneDay.superclass.initComponent.call(this);
//
//    },

    onRender : function(ct, position){
        Wtf.cal.oneDay.superclass.onRender.call(this,ct,position);
        this.createtableBody();
    },

    createtableBody:function(){
        var cellstr="<table width=100% height=100% cellspacing=0 cellpadding=0 style=position:relative;><tbody>";
        for(var j=0;j<48;j++){
            cellstr+='<tr><td id='+this.parent.id+'cell_'+j+'_'+this.initcolcount+' class="daycompCell">&nbsp;</td></tr>';
        }
        cellstr+='<tr><td id='+this.parent.id+'cell_'+49+'_'+this.initcolcount+' class="daycompCell" style="display: none">&nbsp;</td></tr>';
        cellstr +="</tbody></table>";
        Wtf.DomHelper.insertHtml("beforeEnd",this.body.dom,cellstr);
    }
});
/*======================================onedaycomp.js=========================*/

Wtf.cal.control = function(config) {
    Wtf.apply(this, config);
    this.id=config.id;
    this.currentview=null;
    this.formview=null;
    this.EventClick = "";
    this.rec = [];
    this.eStore=null;
    this.tmpStore=null;
    this.defaultTS="1970-01-01 00:00:00";
    this.dtcont.addListener("render",function(comp){
         comp.add(this.calendar = new Wtf.DatePicker({
              id: comp.id + 'calctrlcalpopup1',
              cls: 'datepicker',
              autoWidth: true,
              border: false,
              defaults: {
                  autoHeight: true,
                  autoScroll: true
              },
              renderTo: comp.id
        }));
        comp.doLayout();
        this.calendar.on('select',this.getCurrentDate,this);
       //this.onDayViewClick();
    },this);

    this.calcont.addListener("render",function(comp){
        comp.add(this.calTree = new Wtf.CalendarTree({
            id: this.id + "Calendar",
            url: Wtf.calReq.cal + "caltree.jsp",
            ownerid: this.ownerid,
            renderTo: comp.id,
            archived: this.archived,
            calControl: config.id,
            parentid: config.id,
            parentTabId: "subtabpanelcom" + comp.id
        }));
        comp.doLayout();
        this.initCalTreeEvents();
        this.created = true;
        this.startCalTreeBot();
        var tItems = this.getTopToolbar().items.items;
        tItems.remove(tItems[8]);
        tItems.remove(tItems[9]);
        tItems.remove(tItems[10]);
        Wtf.QuickHelp.register(tItems,this);
        if(this.calTree.defaultNode){
            this.startCalEventBot(this.calTree.defaultNode.id);
        }
    },this);

//    this.ts = this.defaultTs;
    this.created=false;
    Wtf.cal.control.superclass.constructor.call(this);
    this.createCalComp = new Wtf.cal.createCal(this);
    this.eventWin = new Wtf.cal.eventWindow(this);
    this.tempCalStore=null;
    this.Calendar_countReminder = 0;
    this.calPerm = "";
};

Wtf.extend(Wtf.cal.control,Wtf.Panel, {
    selectedDate: null,
    seldate:null,
    selecteddate: null,
    selectedday: null,
    EventInc:null,
    ResizeArray:null,
    day : null,
    delid : null,
    droi1:null,
    droi2:null,
    startdate:null,
    enddate:null,
    calList:[],
    chkCalList:[],
    hoverEvent: null,
    monthDayHeight: 0,  // [IE fix] Used for storing height of the month panel
    monthNoOfDays: 35,  // [IE fix] Used for storing the number of days in the month
    initComponent:function() {
        if(!this.height)
            this.height='100%';
        if(this.width==null)
            this.width='100%';

        this.on("activate",function(){
           // toggleMainCal(0);
           // this.calTree.show();
           // this.calendar.show();
            this.addListener("resize",this.CalculateHow,this);
            this.layout.activeItem = Wtf.getCmp(this.id+'dayPanel');
            if(!this.created){
                this.addListener("resize",this.CalculateHow,this);
                this.layout.activeItem = Wtf.getCmp(this.id+'dayPanel');
                if(!this.calTree.delayFlag){
                 // this.onDayViewClick();
                }

                this.CalculatingTotalCalendar();
                this.created = true;
                if(this.calTree.defaultNode){
                    this.startCalEventBot(this.calTree.defaultNode.id);
                }
            }

        },this);

        this.on('deactivate',function(){
          //  this.calTree.hide();
        ///    this.calendar.hide();
        //  toggleMainCal(1);
            var k = Wtf.getCmp(this.id.substring(0, this.id.indexOf('calctrl'))+'Expand');
            if(k!=undefined){
                k.close();
            }

        },this);
//datepicker created in global.js for main calendar and projects.js from projects
        var topBar = [];
        if(!this.archived){
            topBar.push([{
                text: WtfGlobal.getLocaleText('lang.new.text'),iconCls:'pwnd createnew caltb',tooltip:WtfGlobal.getLocaleText('pm.Help.createnew'),id:this.id+'new',
                scope:this, ctCls: 'fontstyle', menu:[{
                    text: WtfGlobal.getLocaleText('pm.common.calendar'),iconCls:'pwnd newcal caltb',tooltip:{text:WtfGlobal.getLocaleText('pm.Help.newcal')}, id:this.id+'newCal',
                    scope:this,handler:this.onButtonNewCalClick
                },{
                    text: WtfGlobal.getLocaleText('pm.project.calendar.new.icalendar'), iconCls:'pwnd newcal caltb', tooltip:{text:WtfGlobal.getLocaleText('pm.Help.newical')}, id:this.id+'newICal',
                    scope:this, handler:this.onButtonNewCalClick
                },{
                    text: WtfGlobal.getLocaleText('lang.event.text'),id:this.id+'createEvent',iconCls: 'pwnd newCalEvent caltb', scope:this,tooltip:{text:WtfGlobal.getLocaleText('pm.Help.newevent')},
                    handler:function(){
                        this.eventWin.showWindowWeek("",timeStr,5,5,true);
                    }
                }]
            }]);
        }
        topBar.push([{
                text: WtfGlobal.getLocaleText('lang.day.text'),id:this.id+'dayAction1',iconCls: 'dpwnd calday caltb',
                handler: this.onDayViewClick,tooltip:WtfGlobal.getLocaleText('pm.Help.dayview'),scope:this,
                toggleGroup: 'tabs',detailTip:WtfGlobal.getLocaleText('pm.cal.day.tip')
            },{
                text: WtfGlobal.getLocaleText('pm.cal.workweek.text'),id:this.id+'workweekAction1',iconCls: 'dpwnd calworkwk caltb',
                handler: this.onWorkWeekViewClick, tooltip:WtfGlobal.getLocaleText('pm.Help.workweekview'),scope:this,
                toggleGroup: 'tabs',detailTip:WtfGlobal.getLocaleText('pm.cal.workweek.tip')
            },{
                text: WtfGlobal.getLocaleText('lang.week.text'),id:this.id+'weekAction1',iconCls: 'dpwnd calweek caltb',
                handler: this.onWeekViewClick,tooltip:WtfGlobal.getLocaleText('pm.Help.weekview'),scope:this,
                toggleGroup: 'tabs',detailTip:WtfGlobal.getLocaleText('pm.cal.week.tip')
            },{
                text: WtfGlobal.getLocaleText('lang.month.text'),id:this.id+'monthAction1',iconCls: 'dpwnd calmonth caltb',
                handler: this.onMonthViewClick,tooltip:WtfGlobal.getLocaleText('pm.Help.monthview'),scope:this,
                toggleGroup: 'tabs',detailTip:WtfGlobal.getLocaleText('pm.cal.month.tip')
            },{
                text: WtfGlobal.getLocaleText('lang.agenda.text'),id:this.id+'agendaAction1',iconCls: 'dpwnd propicon caltb',
                handler: this.onAgendaClick,tooltip:WtfGlobal.getLocaleText('pm.Help.agenda'),scope:this,
                toggleGroup: 'tabs',detailTip:WtfGlobal.getLocaleText('pm.cal.agenda.tip')
            },{
                text: '',id:this.id+'prevAction1',iconCls: 'pwnd previcon caltb',scope:this,
                handler: this.onPrevClick,detailTip:'',tooltip:WtfGlobal.getLocaleText('pm.Help.prevview')
            },{
                text: 'selecteddaymon',id:this.id+'dateText1',ctCls:'fontstyle',scope:this,detailTip:''
            },{
                text: '',id:this.id+'nextAction1',iconCls: 'pwnd nexticon caltb',
                handler: this.onNextClick,scope:this,detailTip:'',tooltip:WtfGlobal.getLocaleText('pm.Help.nextview')
            }]);
        if(!this.archived){
            topBar.push([{
                    text: WtfGlobal.getLocaleText('pm.common.notifications.send'),id:this.id+'notify',iconCls: 'pwnd outbox caltb',
                    tooltip: {title:WtfGlobal.getLocaleText('pm.common.notification'),text: WtfGlobal.getLocaleText('pm.updateprofile.email')},menu:[],handler: this.attachMenu,
                    scope:this
                },{
                    text: WtfGlobal.getLocaleText('lang.import.text'),id:this.id+'importICSAction1',iconCls: 'dpwnd contactimporticon caltb',
                    handler: this.onImportICSClick, tooltip: WtfGlobal.getLocaleText('pm.Help.importcal'), scope: this,
                    toggleGroup: 'tabs',detailTip:WtfGlobal.getLocaleText('pm.cal.import.tip')
                },{
                    text: WtfGlobal.getLocaleText('pm.common.save'),id: this.id + 'SaveAction1',iconCls: 'dpwnd saveicon caltb',
                    handler: this.onSaveClick, scope: this, detailTip: '', hidden: true
                },{
                    text: WtfGlobal.getLocaleText('lang.close.text'),id: this.id + 'CloseAction1',iconCls: 'dpwnd closeicon caltb',
                    handler: this.onCloseClick, scope: this, detailTip: '', hidden: true
                },{
                    text: WtfGlobal.getLocaleText('lang.delete.text'),id: this.id + 'deleteEventClick',iconCls: 'pwnd deliconwt caltb',
                    handler: this.onDeleteClick, scope: this, detailTip: '', hidden: true
            }]);

        } else {
            topBar.push([{
                    text: WtfGlobal.getLocaleText('lang.close.text'),id: this.id + 'CloseAction1',iconCls: 'dpwnd closeicon caltb',
                    handler: this.onCloseClick, scope: this, detailTip: '', hidden: true
            }]);
        }
        topBar.push([{
            text: WtfGlobal.getLocaleText('lang.export.text'),id:this.id+'exportICSAction1',iconCls: 'dpwnd contactexporticon caltb',
            handler: this.onExportICSClick,tooltip:WtfGlobal.getLocaleText('pm.Help.exportcal'),scope:this,
            toggleGroup: 'tabs'
        }]);
        if(!this.archived && (this.connstatus != 6 && this.connstatus != 7)){
            topBar.push([{
                text: WtfGlobal.getLocaleText('pm.project.calendar.sync'), id:this.id+'syncAction', iconCls: 'pwnd calsyncincal caltb',
                handler: this.calSync, tooltip: WtfGlobal.getLocaleText('pm.Help.projplansync'), scope:this
            }])
        }
        if(!this.archived){
            topBar.push("<a id ='"+this.id+"globalrsscal' href=\""+Wtf.pagebaseURL+"feed.rss?m=events&u="+loginname+"&p="+this.ownerid.userid+"\" target='_blank'> <img class=\"rssimgMid\" alt=\"\" src=\"../../images/FeedIcon16.png\" Wtf:qtip ='"+WtfGlobal.getLocaleText('pm.Help.rsscal')+"'/></a>");

        }
        var toolbar = new Wtf.Toolbar({
            id:this.id+'calMainToolbar',
            cls:'toolbarcls',
            ctCls:'toolbarcls',
            items:topBar
        });

        if(this.myToolbar==true)
           this.tbar=toolbar;

        if(this.mView) {
            if(typeof this.mView == 'object')
                this.monthView = this.mView;
            delete this.mView;
        }

        if(this.dView) {
            if(typeof this.dView == 'object')
                this.dayView = this.dView;
            delete this.dView;
        }
        Wtf.cal.control.superclass.initComponent.call(this);




/*Calendar Tree*/

//cal tree created in global.js for main calendar and projects.js from projects

/*Calendar Tree*/
    },

    attachMenu: function(a){
         var month = ["January","February","March","April","May","June","July","August","September","October","November","December"];
         var menuList = new Wtf.menu.Menu({
            id: 'menu'+this.id,
            items:[
                        {
                            xtype: 'radio',
                            id: this.id+"day",
                            checked: true,
                            group: 'theme',
                            scope:this,
                            tooltip: {
                                 text: WtfGlobal.getLocaleText('pm.Help.senddayevent')
                            },
                            handler: function(){
                                var cur_date= new Date(this.calendar.value);
                                this.sendNotification(1, cur_date.format("Y-m-d H:i:s"));
                            }
                        },
                        {
                            xtype: 'radio',
                            id: this.id+"week",
                            checked: false,
                            group: 'theme',
                            scope:this,
                            tooltip: {
                                 text: WtfGlobal.getLocaleText('pm.Help.sendweekevent')
                            },
                            handler: function(){
                                this.sendNotification(2, this.weekday);
                            }
                        },
                        {
                            xtype: 'radio',
                            id: this.id+"month",
                            checked: false,
                            group: 'theme',
                            scope:this,
                            tooltip: {
                                 text: WtfGlobal.getLocaleText('pm.Help.sendmonthevent')
                            },
                            handler: function(){
                                this.sendNotification(3, this.monthday);
                            }
                        }
                    ]
        });
        a.menu = menuList;
        var dt = new Date(this.calendar.value);
        Wtf.getCmp(this.id + 'day').setText(dt.format(WtfGlobal.getOnlyDateFormat()));
        this.setWeekText();
        Wtf.getCmp(this.id + 'month').setText(month[dt.getMonth()]);
        this.monthday = dt.getFirstDateOfMonth().format("Y-m-d H:i:s")+","+dt.getLastDateOfMonth().format("Y-m-d H:i:s")
        a.showMenu();
    },
     
    setWeekText: function(){
          var lastdate = new Date(this.calendar.value);
          var firstdate = new Date(this.calendar.value);
          var currentDay = lastdate.getDay();
          firstdate.setDate(lastdate.getDate()-(currentDay - 1));
          lastdate.setDate(lastdate.getDate()+(6-(currentDay - 1)));
          this.weekday = firstdate.format("Y-m-d H:i:s")+","+lastdate.format("Y-m-d H:i:s");
          Wtf.getCmp(this.id + 'week').setText(firstdate.format(WtfGlobal.getOnlyDateFormat())+"  -  "+lastdate.format(WtfGlobal.getOnlyDateFormat()))
    },
    sendNotification: function(flag,date){
          var totalCal = "";
          if(this.chkCalList.length != 0){
              for(var i = 0; i < this.chkCalList.length ; i++){
                totalCal += this.chkCalList[i]+"," ;
              }
              Wtf.Ajax.requestEx({
                    method:'POST',
                    url: this.url,
                    params:{
                        action:7,
                        subaction:flag,
                        currentdate:date,
                        projectid:this.id,
                        caltype:this.calTree.ownerid.type,
                        calid:totalCal
                    }
              }, this, function(result, req){
                    if(result == "typeError"){
                        msgBoxShow(183,0);
                    } else if(result == "noEvent"){
                        msgBoxShow(185,0);
                    } else{
                        msgBoxShow(166,0);
                    }
              });
          } else {
              msgBoxShow(184,0);
          }
    },

    calSync: function(config) {
      var arr = [];
      arr = this.id.split("calctrl");
      var id = arr[0];
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.323'), function(btn) {
            if(btn == "no") {
                return;
            } else if(btn == "yes") {
                Wtf.Ajax.requestEx({
                    method:'POST',
                    url:Wtf.req.prj + 'task.jsp',
                    params:{
                        seq: "9",
                        pid: id
                    }
                },
                this,
                function(result, req) {
                    msgBoxShow(175, 0);
                    if(this.calTree.defaultNode){
                        var node = this.calTree.defaultNode;
                        if(node){
                            node.ui.toggleCheck();
                            if(!node.ui.isChecked()){
                                node.ui.toggleCheck();
                            }
                        }
                        
                    }
                },
                function() {
                    msgBoxShow(176, 1);
                });
            }
        }, this);
    },

    initCalTreeEvents:function(){
        this.calTree.on("changecolor",function(treeobj,node,color){
            if(this.calTree.agendaStore){
                var permissionString = "";
                var rec= new this.createCalComp.calRec();
                var t=this.calTree.agendaStore.find("cid",node.id);
                if(t!=-1){
                    var recData=this.calTree.agendaStore.getAt(t).data;
                    Wtf.Ajax.requestEx({
                        method: 'GET',
                        url: Wtf.calReq.cal + 'caltree.jsp',
                        params: ({
                            action: 4,
                            cid: recData.cid,
                            caltype:this.calTree.ownerid.type
                        })},
                    this,
                    function(result, req){
                        if(recData.caltype != 4){
                            var nodeobj = eval("(" + result+ ")");
                            for(var i=0;i<nodeobj.data.length;i++){
                                permissionString += nodeobj.data[i].userid;
                                permissionString += "_"+nodeobj.data[i].permissionlevel+",";
                            }
                        } else {
                            permissionString = "2";
                        }
                        var cRecord = [recData.cid, recData.cname, recData.description, recData.location, recData.timezone, color, recData.caltype, recData.isdefault, recData.userid, permissionString, recData.exportCal, recData.url, recData.interval];
                        this.updateCalendar(cRecord);
                    },
                    function(result, req){
                        calMsgBoxShow(4, 1);
                    });
//                    Wtf.Ajax.request({
//                    url: Wtf.calReq.cal + 'caltree.jsp',
//                    method: 'GET',
//                    params: ({
//                        action: 4,
//                        cid: rec.data["cid"],
//                        caltype:this.calTree.ownerid.type
//                    }),
//                    scope: this,
//                    success: function(result, req){
//                        var nodeobj = eval("(" + result.responseText.trim() + ")");
//                            for(var i=0;i<nodeobj.data.length;i++){
//                                permissionString += nodeobj.data[i].userid;
//                                permissionString += "_"+nodeobj.data[i].permissionlevel+",";
//                            }
//                        var cRecord = [rec.data["cid"], rec.data["cname"], rec.data["description"], rec.data["location"], rec.data["timezone"], color, rec.data["caltype"], rec.data["isdefault"], rec.data["userid"], permissionString];
//                        this.updateCalendar(cRecord);
//                        },
//                        failure: function(){
//                            Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occurred while connecting to the server');
//                        }
//                    });
                }
            }
            /*if (this.currentview == 'MonthView')
                this.ChangingMonthColor(color, node.id);
            else
                this.ChangingOtherColor(color, node.id);*/
        },this);

        this.calTree.on("treecheckchange",function(treeobj, treenode, event, request){
            if(this.currentview=="agendaView"){
                this.getStartEndDate();
                this.getCalIds();
                this.reloadAgenda();
            }
            else{
                var expandWin = Wtf.getCmp(this.id.substring(0, this.id.indexOf('calctrl'))+"Expand");
                if(expandWin)
                    expandWin.close();

                if(request){
                    Wtf.Ajax.requestEx({
                        url: Wtf.calReq.cal + 'caltree.jsp',
                        params:{
                            action: 8,
                            cid: treenode.id,
                            caltype: treenode.attributes.caltype,
                            selection: event
                        }
                    })
                }
                if (!event){
                    treenode.attributes.request = false;
                    this.stopCalEventBot(treenode.id);
                    this.removeICalEvents(this.eStore);
                    this.HidingOtherCalendarEvent(treenode.id);
                }
                else{
                    this.startCalEventBot(treenode.id);
                    this.ShowingOtherCalendarEvent(treenode.id);
                }
            }
            this.onCalViewChange();
        },this);

        this.calTree.on("calendarsettings",function(nodeId, caltype){
            this.RemoveMainPanelContent();
            var cc=Wtf.getCmp(this.id+'createCalForm');
            if(!cc){
                this.createCalComp.CreateCalendar(nodeId, caltype == 4 ? "ical" : "cal");
                cc=Wtf.getCmp(this.id+'createCalForm');
                this.showCalPanel(cc);
            }
            else{
                this.showCalPanel(cc);
                this.createCalComp.createCalendarForm(nodeId, caltype == 4 ? "ical" : "cal");
            }
//            CreateCalendar(this,nodeId);
        },this);

        this.calTree.on("deletecalendar",function(nodeId){
            if(this.calTree.agendaStore){
                var rec= new this.createCalComp.calRec();
                var t=this.calTree.agendaStore.find("cid",nodeId);
                if(t!=-1){
                    rec=this.calTree.agendaStore.getAt(t);
                    this.deleteCalendar(rec);
                }
            }
        },this);
    },

    CalculateHow : function(){
        this.CalculateHowMore();
    },

    hideFormtoolbar : function(){
        if(!this.archived){
            Wtf.getCmp(this.id + 'SaveAction1').hide();
            Wtf.getCmp(this.id + 'CloseAction1').hide();
            Wtf.getCmp(this.id + 'deleteEventClick').hide();

        } else {
            Wtf.getCmp(this.id + 'CloseAction1').hide();
        }
    },

     showFormtoolbar : function(){
        if(!this.archived){
            Wtf.getCmp(this.id + 'SaveAction1').show();
            Wtf.getCmp(this.id + 'CloseAction1').show();
            Wtf.getCmp(this.id + 'deleteEventClick').show();

        } else {
            Wtf.getCmp(this.id + 'CloseAction1').show();
        }
    },

    hideMaintoolbar : function(){
//        Wtf.getCmp(this.id+'refreshAction1').hide();
//        Wtf.getCmp(this.id+'pdfAction1').hide();
        Wtf.getCmp(this.id+'dayAction1').hide();
        Wtf.getCmp(this.id+'workweekAction1').hide();
        Wtf.getCmp(this.id+'weekAction1').hide();
        Wtf.getCmp(this.id+'monthAction1').hide();
        Wtf.getCmp(this.id+'agendaAction1').hide();
        //Wtf.getCmp(this.id+'todolist').hide();
        Wtf.getCmp(this.id+'prevAction1').hide();
        Wtf.getCmp(this.id+'dateText1').hide();
        Wtf.getCmp(this.id+'nextAction1').hide();
        Wtf.getCmp(this.id+'exportICSAction1').hide();

        if(!this.archived) {
            Wtf.getCmp(this.id+'newCal').hide();
            Wtf.getCmp(this.id+'createEvent').hide();
            Wtf.getCmp(this.id+'new').hide();
            Wtf.getCmp(this.id+'syncAction').hide();
            Wtf.getCmp(this.id+'notify').hide();
            Wtf.getCmp(this.id+'importICSAction1').hide();
            Wtf.get(this.id+'globalrsscal').dom.parentNode.style.display = "none";
        }
    },

    showMaintoolbar : function(){
//        Wtf.getCmp(this.id+'refreshAction1').show();
//        Wtf.getCmp(this.id+'pdfAction1').show();
        Wtf.getCmp(this.id+'dayAction1').show();
        Wtf.getCmp(this.id+'workweekAction1').show();
        Wtf.getCmp(this.id+'weekAction1').show();
        Wtf.getCmp(this.id+'monthAction1').show();
        Wtf.getCmp(this.id+'agendaAction1').show();
        //Wtf.getCmp(this.id+'todolist').show();
        Wtf.getCmp(this.id+'prevAction1').show();
        Wtf.getCmp(this.id+'dateText1').show();
        Wtf.getCmp(this.id+'nextAction1').show();
        Wtf.getCmp(this.id+'exportICSAction1').show();

        if(!this.archived) {
            Wtf.getCmp(this.id+'newCal').show();
            Wtf.getCmp(this.id+'createEvent').show();
            Wtf.getCmp(this.id+'new').show();
            Wtf.getCmp(this.id+'syncAction').show();
            Wtf.getCmp(this.id+'notify').show();
            Wtf.getCmp(this.id+'importICSAction1').show();
            Wtf.get(this.id+'globalrsscal').dom.parentNode.style.display = "block";
        }
    },

//    onPdfClick : function(){
//        var Grid = Wtf.getCmp(this.id + '_agendaGrid');
//        var GridStore = Grid.getStore();
//		Wtf.get('downloadframe').dom.src= "../../exportPdf.jsp?start="+GridStore.lastOptions.params["start"]+
//        "&limit="+GridStore.lastOptions.params["limit"]+"&cidList="+GridStore.lastOptions.params["cidList"]+
//        "&viewdt1="+GridStore.lastOptions.params["viewdt1"]+"&viewdt2="+GridStore.lastOptions.params["viewdt2"];
//    },

    onRender : function(ct, position){
        Wtf.cal.control.superclass.onRender.call(this, ct, position);
        this.loadMask = new Wtf.LoadMask(this.el.dom, Wtf.apply(this.id));
        this.loadMask.msg = WtfGlobal.getLocaleText('pm.loadingcalendar.text')+'...';
        Wtf.getCmp(this.id).addListener("resize", function() {
            // Calling calculateHowMore on resize and calculating new height of the month panel according to the number of days in the month
            if(this.currentview != null) {
                this.doLayout();
                this.monthDayHeight = ((Wtf.get(this.id).dom.offsetHeight - 50) * (this.monthNoOfDays == 35? 19.3 : 16.3))/100;
                this.CalculateHowMore();
            }
        },this);
        this.getWorkWeek(true);
    },

    getWorkWeek: function(change){
        this.actualworkingdays = "";
        this.NonworkWeekDays = [];
        this.workDayArr = [];
        if(change && change == true){
            Wtf.Ajax.requestEx({
                url: Wtf.calReq.cal + 'calEvent.jsp',
                params : {
                    projectid: this.id.substring(0, this.id.indexOf('calctrl')),
                    action: "5"
                },
                method: 'GET'},
                this,
                function(result, request) {
                    var res = result;
                    if(res.length > 0) {
                       var obj = eval('(' + res + ')');
                       for(var t=0; t<obj.nonworkweekdays.length; t++){
                           this.NonworkWeekDays[t] = obj.nonworkweekdays[t].day;
                       }
                       var cn = 0;
                       for(var t=0; t<7; t++){
                           if(this.NonworkWeekDays.indexOf(t.toString()) == -1){
                               this.workDayArr[cn] = t;
                               cn++;
                           }
                       }
                       this.actualworkingdays = 7 - obj.nonworkweekdays.length;
                    }
                },
                function(result, request) {
                    msgBoxShow(4, 1);
            });
        }
    },

    startCalTreeBot : function() {
      	dojo.cometd.subscribe("/calTree/"+this.ownerid.userid, this, "calTreeHandler");
    },

    stopCalTreeBot : function() {
        dojo.cometd.unsubscribe("/calTree/"+this.ownerid.userid);
    },

    calTreeHandler : function(msg) {
        this.calTree = Wtf.getCmp(this.calTree.id);     // [Temp Fix] for "this.calTree.nodeHash" null error
        if(msg.data.action=="1"){
            if(msg.data.success=="true"){
                var msgobj = eval("(" + msg.data.data+ ")");
                if(this.calTree.agendaStore){
                    var rec=this.calTree.agendaStore.reader.readRecords(msgobj).records;
                    this.calTree.agendaStore.add(rec);
                    var recData = rec[0].data;
                    var calname = Wtf.util.Format.ellipsis(recData.cname,15);
                    var node = this.calTree.treeRoot.appendChild(new Wtf.tree.TreeNode({
                        text: calname,
                        allowDrop: false,
                        allowDrag: false,
                        checked: false,
                        icon: "../../lib/resources/images/default/s.gif",
                        iconCls: 'imgchange',
                        cls: 'treenodeclass',
                        qtip : recData.description,
                        qtipTitle : recData.cname,
                        id: recData.cid,
                        uiProvider: Wtf.tree.TableTreeUI,
                        colorIndex: parseInt(recData.colorcode),
                        caltype: recData.caltype
                    }));
                    if(node){
                        node.attributes.request=false;
                        node.select();
                        node.ui.toggleCheck();
                    }
                }
                this.CalculatingTotalCalendar();
            }
        }
        if(msg.data.action=="2"){
            if(msg.data.success=="true"){
                var msgobj = eval("(" + msg.data.data+ ")");
                if(this.calTree.agendaStore){
                    var rec=this.calTree.agendaStore.reader.readRecords(msgobj).records;
                    Wtf.cal.utils.findAndReplace(this.calTree.agendaStore,"cid",rec);
                    var recData = rec[0].data;
                    var node=this.calTree.getNodeById(recData.cid);
                    if(node){
                        node.getUI().SetBackColor(recData.colorcode);
                        var calname = Wtf.util.Format.ellipsis(recData.cname,15);
                        node.ui.getTextEl().setAttribute("wtf:qtip", recData.description);
                        node.ui.getTextEl().setAttribute("wtf:qtitle",recData.cname);
                        node.setText(calname);
                        node.select();
                        this.ChangingOtherColor(recData.colorcode, node.id);
                    }
                }
                this.CalculatingTotalCalendar();
            }
        }
        if(msg.data.action=="3"){
            if(msg.data.success=="true"){
                if(this.calTree.agendaStore){
                    var cid=msg.data.cid;
                    var t=this.calTree.agendaStore.find("cid",cid);
                    if(t!=-1){
                        if(this.formview != null) {
                            if(this.formview == "CreateCal") {
                                // Checking if deleted calendar's settings form is opened
                                var openCal = this.calTree.getSelectionModel().getSelectedNode();
                                if(openCal != null) {
                                    var openCalId = openCal.id;
                                    var createCalForm = Wtf.getCmp(this.id+'inner1');
                                    if(createCalForm) {
                                        if(createCalForm.hidden == false && openCalId == cid) {
                                            calMsgBoxShow(158, 1);
                                            this.addCalendarTab1();
                                        }
                                    }
                                }
                            }
                            else if(this.formview == "Appointmentform") {
                                // Checking if appointment form of deleted calendar's event is opened
                                if(this.EventClick != "" && this.EventClick != null) {
                                    var eventDetails = this.eStore.query("peid", this.EventClick.split("e_")[1].split("CNT_")[0]);
                                    var appointmentForm = Wtf.getCmp(this.id+'Appointmentform');
                                    if(appointmentForm && eventDetails.length > 0) {
                                        if(appointmentForm.hidden == false && eventDetails.items[0].data["cid"] == cid) {
                                            calMsgBoxShow(159, 1);
                                            this.addCalendarTab1();
                                        }
                                    }
                                }
                            }
                        }

                        var rec=this.calTree.agendaStore.getAt(t);
                        if(rec.data["permissionlevel"]==""){
                            if(rec.data["isdefault"]!="1"){
                                var node=this.calTree.getNodeById(cid);
                                if(node){
                                    node.remove();
                                    this.calTree.agendaStore.remove(rec);
                                }
                            }
                        } else {
                            var node=this.calTree.getNodeById(cid);
                            if(node){
                                node.remove();
                                this.calTree.agendaStore.remove(rec);
                            }
                        }
                        var recs=this.eStore.query("cid",cid,true);
                        recs.each(function(r){
                            this.eStore.remove(r);
                            this.removeRenderedEvent(r.data["eid"], true);
                        },this);
                        if(this.currentview == "agendaView") {
                            this.getCalIds();
                            this.reloadAgenda();
                        }
                        else if(this.currentview != "MonthView")
                            this.CalculateWeekMore();
                    }
                }
                this.CalculatingTotalCalendar();
            }
        }
    },

    startCalEventBot : function(cid) {
      	dojo.cometd.subscribe("/calEvent/"+cid, this, "calEventHandler");
    },

    stopCalEventBot : function(cid) {
        dojo.cometd.unsubscribe("/calEvent/"+cid);
    },

    calEventHandler : function(msg) {
        this.calTree = Wtf.getCmp(this.calTree.id);     // [Temp Fix] for "this.calTree.nodeHash" null error
        this.currentview = this.calTree.calcontrol.currentview;
        var thisdroi1 = this.droi1.format("Y-m-d H:i:s");
        var thisdroi2 = this.droi2.add(Date.DAY, 1).format("Y-m-d 00:00:00");
        var thisstartdate = this.startdate.format("Y-m-d 00:00:00");
        var thisenddate = this.enddate.add(Date.DAY, 1).format("Y-m-d 00:00:00");
        if(msg.data.calView=="0"||msg.data.calView=="1") {
            if(msg.data.action=="1") {  //For insert event
                if(msg.data.success=="true") {
                    var msgobj = eval("(" + msg.data.data+ ")");
                    var jsonrec = this.eStore.reader.readRecords(msgobj);
                    var stdt = Wtf.cal.utils.sqlToJsDate(jsonrec.records[0].data["startts"]).format("Y-m-d H:i:s");
                    var endt = Wtf.cal.utils.sqlToJsDate(jsonrec.records[0].data["endts"]).format("Y-m-d H:i:s");
                    if((thisdroi1<=stdt && thisdroi2>=stdt) || (thisdroi1<=endt && thisdroi2>=endt)) {
                        // Update store if the event lies in the retrieved data's range view's range
                        for(var cnt = 0; cnt<jsonrec.records.length; cnt++)
                            this.eStore.add(jsonrec.records[cnt]);
                    }
                    if(this.currentview!="agendaView") {
                        if((thisstartdate <= stdt && thisenddate >= stdt) || (thisstartdate <= endt && thisenddate >= endt))
                            // Render the event if it lies in the current view's range
                            this.renderEventsOnPanel(jsonrec.records);
                    }
                    else
                        this.reloadAgenda();
                }
            }
            else if(msg.data.action=="2") {     //For update event
                if(msg.data.success=="true") {
                    msgobj = eval("(" + msg.data.data+ ")");
                    jsonrec = this.eStore.reader.readRecords(msgobj);
                    // Update store if the event lies in the retrieved data's range view's range
                        // [1] Remove the initial record of the event from the store
                        // [2] Add the new updated event records to the store
                    for(cnt = 0; cnt < jsonrec.totalRecords; cnt++) {
                        this.removeEventFromStore(jsonrec.records[0].data.peid);
                    }
                    for(cnt = 0; cnt<jsonrec.records.length; cnt++) {
                        stdt = Wtf.cal.utils.sqlToJsDate(jsonrec.records[cnt].data["startts"]).format("Y-m-d H:i:s");
                        endt = Wtf.cal.utils.sqlToJsDate(jsonrec.records[cnt].data["endts"]).format("Y-m-d H:i:s");
                        if((thisdroi1<=stdt && thisdroi2>=stdt) || (thisdroi1<=endt && thisdroi2>=endt)) {
                            for(cnt = 0; cnt<jsonrec.records.length; cnt++)
                                this.eStore.updateStore("eid", jsonrec.records[cnt], 0);
                        }
                    }
                    if(this.currentview!="agendaView") {
                        // [1] Remove the event from the DOM
                        // [2] Render the event if it lies in the current view's range
                        this.removeRenderedEvent(jsonrec.records[0].data.eid, this.currentview == "MonthView"? true: false);
                        if((thisstartdate<= stdt && thisenddate>stdt) || (thisstartdate<=endt && thisenddate>=endt) || (stdt<=thisstartdate && endt>=thisenddate))
                            this.renderEventsOnPanel(jsonrec.records);
                        else if(this.currentview != "MonthView")
                            this.CalculateWeekMore();
                    }
                    else
                        this.reloadAgenda();
                }
            }
            else if(msg.data.action=="3") {      //For event deletion
                if(msg.data.success=="true"){
                    var t = this.eStore.find("peid",msg.data.eid);
                    if(t!=-1){
                        var rec=this.eStore.getAt(t);
                        stdt=Wtf.cal.utils.sqlToJsDate(rec.data["startts"]);
                        endt = Wtf.cal.utils.sqlToJsDate(rec.data["endts"]);
                        stdt = stdt.format("Y-m-d H:i:s");
                        endt = endt.format("Y-m-d H:i:s");
                        this.removeEventFromStore(msg.data.eid);
                        if(this.formview == "Appointmentform") {
                            if(this.EventClick != "" && this.EventClick != null) {
                                var appointmentForm = Wtf.getCmp(this.id+'Appointmentform');
                                if(appointmentForm) {
                                    // Checking if appointment form of deleted event's is opened
                                    if(appointmentForm.hidden == false && this.EventClick.split("e_")[1].split("CNT_")[0] == msg.data.eid) {
                                        calMsgBoxShow(155, 1);
                                        this.addCalendarTab1();
                                    }
                                }
                            }
                        }
                    }
                    if(this.currentview!="agendaView") {
                        if((thisstartdate<= stdt && thisenddate>=stdt) || (thisstartdate<=endt && thisenddate>=endt) || (stdt<=thisstartdate && endt>=thisenddate)) {
                            // Remove the event from the DOM
                            this.removeRenderedEvent(msg.data.eid, this.currentview == "MonthView"? true: false);
                            if(this.currentview != "MonthView")
                                this.CalculateWeekMore();
                        }
                    }
                    else{
                        var Grid  = Wtf.getCmp(this.id+'_agendaGrid');
                        if(Grid && Grid!=undefined)
                            this.reloadAgenda();
                    }
                }
            }
        }
        else if(msg.data.action=="4") {      //Calendar import by merge
            if(this.CheckforChecked(msg.data.cid)) {
                this.calTree.treeRoot.childNodes[this.calTree.agendaStore.find("cid", msg.data.cid)].attributes.request = false;
                this.onCalViewChange();
            }
        }
    },

    /*afterRender:function(){
        Wtf.cal.control.superclass.afterRender.call(this);
        this.onDayViewClick();
    },

    setColor:function(cp, color) {
       Wtf.Msg.alert('Color Selected');
    },*/

    removeRenderedEvent: function(evtID, isMonthView) {
        // Used to remove the event from the DOM using the id of the event to query the DOM and find elements containg "peid"
        var eventDom = Wtf.query("div[peid="+ evtID.split("CNT_")[0] +"]");
        for(var i=0; i<eventDom.length; i++) {
            var parent = eventDom[i].parentNode;
            parent.removeChild(eventDom[i]);
            if(isMonthView)
                this.CalculateHowMore(null, null, parent);
        }
    },

    removeEventFromStore: function(evtPId) {
        // Used to remove the event from the store using the id of the event to query the store and find elements containg "peid"
        while(this.eStore.find("peid", evtPId) != -1) {
            var whereInStore = this.eStore.find("peid", evtPId);
            if(whereInStore != -1)
                this.eStore.remove(this.eStore.getAt(whereInStore));
            else
                break;
        }
    },

    onButtonNewCalClick :function(items){
        var caltype = 'ical';
        if(items.text == WtfGlobal.getLocaleText('pm.common.calendar')){
            caltype = 'cal';
        }
        var calCount=15;
        var currCalCount=0;//if(currCalCount!=0)
        for(var i=0;i<(this.calTree.agendaStore.getCount());i++){
            if((this.calTree.agendaStore.getAt(i).data["permissionlevel"])=="")
                currCalCount++;
        }

        if(currCalCount<calCount){
            this.RemoveMainPanelContent();
            var cc=Wtf.getCmp(this.id+'createCalForm');
            if(!cc){
                this.createCalComp.CreateCalendar(null, caltype);
                cc=Wtf.getCmp(this.id+'createCalForm');
            } else {
                this.createCalComp.createCalendarForm(null, caltype);
            }
            this.showCalPanel(cc);
            items.disable();
        }
        else{
           Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.calmsg.154'), WtfGlobal.getLocaleText({key:'pm.project.calendar.maxlimit', params:calCount}));
           return false;
        }
    },
    
    onDayViewClick:function(item){
        this.RemoveMainPanelContent();
        Wtf.getCmp(this.id+'dayAction1').disable();
        this.seldate = this.calendar.getValue();
        var obj1=Wtf.getCmp(this.id+'dateText1');
        this.getval();
        if((this.currentview=="WorkWeekView")||(this.currentview=="WeekView")){
            obj1.setText(this.selecteddate + ' ' + Date.monthNames[this.seldate.getMonth()] + " " + this.seldate.getFullYear());
            this.ModifyView(this.selectedday, 1, 48, false);
            this.currentview = 'DayView';
        }
        else{
            this.currentview ='DayView';
            obj1.setText(this.selecteddate + ' ' + Date.monthNames[this.seldate.getMonth()] + " " + this.seldate.getFullYear());
            this.AddDayView(this.selectedday, 1, 48);
       }
       this.onCalViewChange();
    },

    viewNextPrev:function(i){
        this.seldate = this.calendar.getValue();
        var obj1=Wtf.getCmp(this.id+'dateText1');
        if(this.currentview==('MonthView')){
            var d1 = this.seldate.add(Date.MONTH,i);
            this.calendar.setValue(d1);
            this.RemoveDiv();
            this.AddMonth();
            obj1.setText(Date.monthNames[d1.getMonth()] + " " + d1.getFullYear());
            this.seldate = this.calendar.getValue();
            var mp=Wtf.getCmp(this.id+'MonthPanel1');
            this.showCalPanel(mp);
        }else{
            var tt = new Date();
            tt = this.seldate.add(Date.DAY,i);
            this.calendar.setValue(tt);
            //this.seldate = this.calendar.getValue();
            if(this.currentview==("DayView")){
                this.removeFunction();
                obj1.setText(tt.format('d F Y'));
                //var newDay=tt.format('D');
                var day1=Wtf.get(this.id+'calDay1');
                day1.dom.innerHTML=tt.format('D')+"/"+tt.format('j');
                day1.dom.style.display="block";
            }else if(this.currentview==('WorkWeekView') || this.currentview==('WeekView')){
               this.removeFunction();
                if(this.currentview==('WorkWeekView')){
                    var selddate=this.selectDayDate(7);
                    obj1.setText(selddate[1]+' '+selddate[0]+', '+selddate[6]+' - '+selddate[5]+' '+selddate[4]+', '+selddate[7]);
                    this.changeDayHeaderText_workweek(7,selddate);
                }
                if(this.currentview==('WeekView')){
                    selddate=this.selectDayDate(7);
                    obj1.setText(selddate[1]+' '+selddate[0]+', '+selddate[6]+' - '+selddate[5]+' '+selddate[4]+', '+selddate[7]);
                    this.changeDayHeaderText(7,selddate);
                }
            }else if(this.currentview==('agendaView')){
                this.getval();
                obj1.setText(tt.format("d M") + " - " + tt.add(Date.DAY, 14).format("d M"));
//                obj1.setText(tt.format('d F Y'));
                this.reloadAgenda(false);
            }
        }
        /*var obj2=document.getElementById(this.id+this.currentview+'day1');
        obj2.innerHTML='<center>'+newDay+'/'+ this.selecteddate+'</ce>';*/
    },

    RemoveMainPanelContent:function(){
        this.hideFormtoolbar();
//        Wtf.getCmp(this.id+'pdfAction1').disable();
        if(Wtf.get(this.id+'dayPanel')!=null) {
            this.removeFunction("DayView");
            Wtf.getCmp(this.id+'dayPanel').hide();
            Wtf.getCmp(this.id+'dayAction1').enable();
        }
        if(Wtf.get(this.id+'weekPanel')!=null) {
            this.removeFunction("WeekView");
            Wtf.getCmp(this.id+'weekPanel').hide();
            Wtf.getCmp(this.id+'weekAction1').enable();
        }
        if(Wtf.get(this.id+'workWeekPanel')!=null) {
            this.removeFunction("WorkWeekView");
            Wtf.getCmp(this.id+'workWeekPanel').hide();
            Wtf.getCmp(this.id+'workweekAction1').enable();
        }
        if(Wtf.get(this.id+'MonthPanel1')!=null) {
            var k = Wtf.getCmp(this.id.substring(0, this.id.indexOf('calctrl'))+'Expand');
            if(k!=undefined){
                k.close();
            }
            Wtf.getCmp(this.id+'MonthPanel1').hide();
            Wtf.getCmp(this.id+'monthAction1').enable();
        }
        if(Wtf.get(this.id+'agendaPanel')!=null) {
            Wtf.getCmp(this.id+'agendaPanel').hide();
            Wtf.getCmp(this.id+'agendaAction1').enable();
        }
        if(Wtf.get(this.id+'createCalForm')!=null) {
            this.createCalComp.clearCalFormFields();
            this.createCalComp.selNodeId=null;
            Wtf.getCmp(this.id+'createCalForm').hide();
            Wtf.getCmp(this.id+'newCal').enable();
            Wtf.getCmp(this.id+'newICal').enable();
        }
        if(Wtf.get(this.id+'Appointmentform')!=null) {
            Wtf.getCmp(this.id+'Appointmentform').hide();
        }
    },

    getval:function(){
        this.seldate = this.calendar.getValue();
        var test = this.seldate.format('d,D,M,Y,y').toLocaleString();
        this.day  = [];
        this.day = test.split(',');
        this.selecteddate = this.day[0];
        this.selectedday =this.day[1];
        //this.selectedmonth = this.day[2];
        //this.selecteddaymon = this.selectedmonth + ' ' + this.selecteddate;
    },

    ModifyOldView:function(selectedday,nDays,nHours){
        var chkPanel=null;
        var currentView=null;
        if((chkPanel=Wtf.getCmp(this.id+'dayPanel'))){
            if(nDays==this.actualworkingdays||nDays==7){
                currentView="DayView";
                this.ModifyView(selectedday,nDays,nHours,true,currentView);
            }
            else{
                this.addDays(nDays);
                this.showCalPanel(chkPanel);
            }
        }
        else if((chkPanel=Wtf.getCmp(this.id+'workWeekPanel'))){
            if(nDays==1){
                currentView="WorkWeekView";
                this.ModifyView(selectedday,nDays,nHours,false,currentView);
            }
            else if(nDays==7){
                currentView="WorkWeekView";
                this.ModifyView(selectedday,nDays,nHours,true,currentView);
            }
            else{
                this.addDays_workweek(nDays);
                this.showCalPanel(chkPanel);
            }
        }
        else if((chkPanel=Wtf.getCmp(this.id+'weekPanel'))){
            if(nDays==this.actualworkingdays){
                currentView="WeekView";
                this.ModifyView(selectedday,nDays,nHours,false,currentView);
            }
            else{
                this.addDays(nDays);
                this.showCalPanel(chkPanel);
            }
        }
    },


    ModifyView:function(selectedday,nDays,nHours,stepFlag,currentView){
        var currPanel=null;
        var currDays=1;
        var currHours=nHours;
        var dayDiff=0;
        var nextPanelId="";
        var nextView="";

        if(!currentView||currentView==""||currentView==undefined)
            currentView=this.currentview;

        if((currentView=="DayView") || (currentView=="WeekView") || (currentView=="WorkWeekView")){
            if(stepFlag){
                if(currentView=="DayView"){
                    if(!Wtf.getCmp(this.id + "weekAction1").pressed){
                        nextPanelId="workWeekPanel";
                        nextView="WorkWeekView";
                    }
                    else{
                        nextPanelId="weekPanel";
                        nextView="WeekView";
                    }
                    currPanel=Wtf.getCmp(this.id+'dayPanel');
                }
                else if(currentView=="WorkWeekView"){
                    currDays=this.actualworkingdays;
                    nextPanelId="weekPanel";
                    nextView="WeekView";
                    currPanel=Wtf.getCmp(this.id+'workWeekPanel');
                }

                this.addDays(nDays);

                var wd=(99/nDays);
                for(var k=0;k<nDays;k++){
                    var p=Wtf.getCmp(this.id+"_"+k);
                    p.show();
//                    p.el.dom.style.width=Math.round(wd)+"%";
                    p.el.dom.style.width=wd+"%";

                }

                currPanel.hide();
                Wtf.ComponentMgr.unregister(currPanel);
                var cellPanel=Wtf.get(this.id+currentView+'eventPanel');
                cellPanel.dom.id=this.id+nextView+"eventPanel";

                var idd =this.id+nextPanelId;
                currPanel.id = idd;
                currPanel.el.id = idd;
                currPanel.el.dom.id = idd;
                Wtf.ComponentMgr.register(currPanel);

//                this.doLayout();
            /*    for(var a=0;a<nHours;a++){
                    for(var b=0;b<nDays;b++){
                        this.addDragCmp(this.id+'cell_'+a+'_'+b,false);
                        var eachDay=document.getElementById(this.id+'cell_'+a+'_'+b);
                        eachDay.ondblclick=this.onCellClick;
                    }
            }*/
            }
            else{
                if(currentView=="WeekView"){
                    currDays=7;
                    if(nDays==1){
                        nextPanelId="dayPanel";
                        nextView="DayView";
                    }
                    else{
                        nextPanelId="workWeekPanel";
                        nextView="WorkWeekView";
                    }
                    currPanel=Wtf.getCmp(this.id+'weekPanel');
                }
                else if(currentView=="WorkWeekView"){
                    currDays=this.actualworkingdays;
                    nextPanelId="dayPanel";
                    nextView="DayView";
                    currPanel=Wtf.getCmp(this.id+'workWeekPanel');
                }

                this.addDays(nDays);


                var wd=(99/nDays);
                for(var k=0;k<currDays;k++){
                    var p=Wtf.getCmp(this.id+"_"+k);
                    if(k>=nDays){
                        p.hide();
                    }
                    else{
//                        p.el.dom.style.width=Math.round(wd)+"%";
                        p.el.dom.style.width=wd+"%";
                    }

                }

                Wtf.ComponentMgr.unregister(currPanel);
                var cellPanel=Wtf.get(this.id+currentView+'eventPanel');
                cellPanel.dom.id=this.id+nextView+"eventPanel";

                var idd =this.id+nextPanelId;
                currPanel.id = idd;
                currPanel.el.id = idd;
                currPanel.el.dom.id = idd;
                Wtf.ComponentMgr.register(currPanel);

            }
            this.showCalPanel(currPanel);            //check 2nd line
        }
    },

    addDays:function(nDays){
        var weekdays=["Sun","Mon","Tue","Wed","Thu","Fri","Sat"];
        var selddate=new Array(6);
        var wd=93.5/nDays;
        this.getval();
        //selddate=this.selectDayDate(nDays,daycount, this.selecteddate, this.selectedday);
        selddate=this.selectDayDate(nDays);
        var d=selddate[0];
        var daycount=selddate[2];

        var timeDiv=Wtf.get(this.id+'calDay0');
        timeDiv.dom.style.display="block";
        timeDiv.dom.style.width="4.5%";

        if(nDays==1)
            weekdays[1] = this.selectedday;

        var allDayWd = 98/nDays;        // AlldayGrid columns and regular week cells (hour cells) are not properly aligned

        var maxViewDays=7;
        for(var i=1;i<=maxViewDays;i++){
            var tempDiv=Wtf.get(this.id+'calDay'+i);
            var tempAllDayDiv = Wtf.get(this.id+'_allDay_'+(i-1));
            if(i<=nDays){
                if(d>selddate[3])
                    d=1;
                tempAllDayDiv.dom.style.display = "block";
                tempAllDayDiv.dom.style.width = allDayWd+'%';

                tempDiv.dom.style.display="block";
                tempDiv.dom.style.width=wd+'%';
                tempDiv.dom.innerHTML=weekdays[daycount++]+'/'+d;
                if(nDays!=1)
                    d++;
            }
            else{
                tempAllDayDiv.dom.innerHTML = "";
                tempAllDayDiv.dom.style.display = "none";

                tempDiv.dom.innerHTML="";
                tempDiv.dom.style.display="none";
            }
        }
    },

    addCells:function(nextView,start,hrs,dys){
        var wd=(100/dys);
        var str=[];

        for(var i=0;i<hrs;i++){
            if(!str[i]||str[i]==undefined)
                str[i]="";
            for(var j=(start);j<dys;j++){
                str[i]+='<td id='+nextView+this.id+'cell_'+i+'_'+j+' style="z-index:-1000; border:1px solid #e8eef7; height:'+Wtf.fixHeight+'px;width='+wd+'%" >&nbsp;</td>';
            }
        }
		return ([wd, str]);
    },

    changeDayHeaderText:function(nDays,selddate){
        var weekdays=["Sun","Mon","Tue","Wed","Thu","Fri","Sat"];
        var wd=93.5/nDays;
        this.getval();

        var d=selddate[0];
        var daycount=selddate[2];

        if(nDays==1)
            weekdays[1] = this.selectedday;

        //optimize for day no. only & not for day string
        for(var i=1;i<=nDays;i++){
            var tempDiv=Wtf.get(this.id+'calDay'+i);
            if(d>selddate[3])
                d=1;
//            tempDiv.dom.style.display="block";
//            tempDiv.dom.style.width=wd+'%';
            tempDiv.dom.innerHTML=weekdays[daycount++]+'/'+d;
            if(nDays!=1)
                d++;
        }
    },
    changeDayHeaderText_workweek:function(nDays,selddate){
        var weekdays=["Sun","Mon","Tue","Wed","Thu","Fri","Sat"];
        var wd=93.5/nDays;
        this.getval();
        var d=selddate[0];
        var daycount=0;
        if(nDays==1)
            weekdays[1] = this.selectedday;
        for(var i=1;i<=nDays;i++){
            var tempDiv=Wtf.get(this.id+'calDay'+i);
            if(d>selddate[3])
                d=1;
            while(i<7){
                if(this.NonworkWeekDays.indexOf(daycount.toString()) != -1){
                    daycount++;
                    if(nDays!=1){
                        d++;
                        if(d>selddate[3])
                            d=1;
                    }
                } else {
                    break;
                }
            }
            tempDiv.dom.innerHTML=weekdays[daycount]+'/'+d;
            if(nDays!=1)
                d++;
        }
    },

    getCurrentPanel:function(){
        var viewPanel="";
        switch(this.currentview){
            case "WorkWeekView" :
                viewPanel="workWeekPanel";
                break;

            case "WeekView" :
                viewPanel="weekPanel";
                break;

            case "MonthView" :
                viewPanel="monthPanel";
                break;

            default :
                viewPanel="dayPanel";
                break;
        }
        return viewPanel;
    },

    AddDayView:function(selectedday,nDays,nHours){
        var idpanel=null;
        var ctr=0;
        var startDay=1;
        if(Wtf.getCmp(this.id+'dayPanel')||Wtf.getCmp(this.id+'workWeekPanel')||Wtf.getCmp(this.id+'weekPanel')){
            this.ModifyOldView(selectedday,nDays,nHours);
        } else{
            var calDragPlugin = [];
            var calAllDayDragPlugin = [];
            if(!this.archived) {
                calDragPlugin = [new Wtf.DataView.calDragSelector({calDrag: true, calContainer: this, calAllDayWeek: false, calMonthView: false})];
                calAllDayDragPlugin = [new Wtf.DataView.calDragSelector({calDrag: true, calContainer: this, calAllDayWeek: true, calMonthView: false})];
            }
            if(nDays==1)
                idpanel=this.id+'dayPanel';
            else if(nDays==7)
                idpanel=this.id+'weekPanel';
            else if(nDays==this.actualworkingdays)
                idpanel=this.id+'workWeekPanel';
            if(!(Wtf.getCmp(idpanel))){
                var htmlDays=this.createDays(nDays,startDay);
                var htmlAllDays = this.createAllDays(nDays);
                var htmlHours=this.createHours(nHours);
                this.add(new Wtf.Panel({
                    id:idpanel,
                    layout : 'border',
                    bodyStyle:'background-color:white',
                    border : false,
                    items :[{
                            region : 'center',
                            layout : 'fit',
                            bodyStyle:'background-color:white',
                            border:false,
                            items : [{
                                    bodyStyle:'background-color:white;overflow-y:auto;overflow-x:hidden;',
                                    layout : 'border',
                                    border:false,
                                    items : [{
                                            autoHeight:true,
                                            region : 'center',
                                            border:false,
                                            layout:'fit',
                                            items:new Wtf.DataView({
                                                cls:'centerCalDview',
                                                store:  new Wtf.data.JsonStore({
                                                    url: 'get-images.php',
                                                    root: 'images',
                                                    fields: ['name', 'url', {name:'size', type: 'float'}, {name:'lastmod', type:'date', dateFormat:'timestamp'}]
                                                }),
                                                id:this.id+'dview',
                                                multiSelect: true,
                                                overClass:'x-view-over',
                                                itemSelector:'div.thumb-wrap',
                                                emptyText:"<div id='"+this.id+this.currentview+"eventPanel' class=calevtPanel></div>",
                                                plugins: calDragPlugin
                                            })
                                        },{
                                            region : 'west',
                                            layout: 'fit',
                                            id:this.id+'fixHourPanel',
                                            autoHeight:true,
                                            border:false,
                                            width:42,
                                            cls:"fixedhourPanel"
                                        },{
                                            region: 'north',
                                            id: this.id+'AllDayContainer',
                                            border: false,
                                            layout: 'fit',
                                            height: 20,
                                            style: 'background-color: #BDD3EF',
                                            items: [
                                                    new Wtf.DataView({
                                                        cls: 'headerDays',
                                                        layout: 'fit',
                                                        store:  new Wtf.data.JsonStore({
                                                            url: 'get-images.php',
                                                            root: 'images',
                                                            fields: ['name', 'url', {name:'size', type: 'float'}, {name:'lastmod', type:'date', dateFormat:'timestamp'}]
                                                        }),
                                                        id: this.id+'fixAllDayPanel',
                                                        autoHeight: true,
                                                        multiSelect: true,
                                                        autoScroll: true,
                                                        overClass: 'x-view-over',
                                                        itemSelector: 'div.thumb-wrap',
                                                        emptyText: htmlAllDays,
                                                        plugins: calAllDayDragPlugin
                                                })]
                                    }]
                                }]
                          },{
                                region : 'north',
                                id:this.id+'fixDayPanel',
                                border : false,
                                height: 20,
                                cls:'headerDays'
                          }]
                }));
                var item=Wtf.getCmp(idpanel);
                this.showCalPanel(item);
                new Wtf.Panel({
                    id:this.id+"dayComp",
                    renderTo:this.id+this.currentview+"eventPanel",
                    layout:'column',
                    baseCls:'dayCompPanel',
                    items:[
                        new Wtf.cal.oneDay({
                            columnWidth:0.98,
                            parent:this,
                            id:this.id+"_0",
                            currview:this.currentview,
                            initcolcount:0
                        }),
                        new Wtf.cal.oneDay({
                            columnWidth:0.167,
                            parent:this,
                            id:this.id+"_1",
                            initcolcount:1
                        }).hide(),
                        new Wtf.cal.oneDay({
                            columnWidth:0.167,
                            parent:this,
                            id:this.id+"_2",
                            initcolcount:2
                        }).hide(),
                        new Wtf.cal.oneDay({
                            columnWidth:0.167,
                            id:this.id+"_3",
                            parent:this,
                            initcolcount:3
                        }).hide(),
                        new Wtf.cal.oneDay({
                            columnWidth:0.167,
                            parent:this,
                            id:this.id+"_4",
                            initcolcount:4
                        }).hide(),
                        new Wtf.cal.oneDay({
                            columnWidth:0.167,
                            parent:this,
                            id:this.id+"_5",
                            initcolcount:5
                        }).hide(),
                        new Wtf.cal.oneDay({
                            columnWidth:0.167,
                            parent:this,
                            id:this.id+"_6",
                            initcolcount:6
                        }).hide()
                    ]
                });
                Wtf.get(this.id+'fixDayPanel').dom.innerHTML = htmlDays;
                Wtf.get(this.id+'fixHourPanel').dom.innerHTML = htmlHours;
                if(!this.archived){
                    for(var j=0;j<7;j++){
                        var md = new Wtf.dd.DropZone(this.id+'_allDay_'+j, {ddGroup:'group'});
                        var eachAllDay = Wtf.get(this.id+'_allDay_'+j).dom;
                        eachAllDay.ondblclick = this.allDayDblClick;
                        for(var i=0;i<nHours;i++) {
                            this.addDragCmp(this.id+'cell_'+i+'_'+j,false);
                            var eachDay=Wtf.get(this.id+'cell_'+i+'_'+j).dom;
                            eachDay.ondblclick=this.onCellClick;
                        }
                    }
                }
            } else {
                var item=Wtf.getCmp(idpanel);
                this.showCalPanel(item);
            }
        }
    },

    createDays:function(dy,startdy){
        var weekdays = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
        var selddate=new Array(8);
        var wd=93.5/dy;
        var stdy=startdy;
        this.getval();

        var htmlStr="";
        htmlStr+='<div id='+this.id+'table2 style="background-color:#BDD3EF; color:#15428B; height:20px; width:98%;">';
        htmlStr+='<div id='+this.id+'calDay0 style="width:42px; display:block; float:left;vertical-align:middle; text-align:center; font-weight:bold; font-size:11px;">'+WtfGlobal.getLocaleText('lang.time.text')+'</div>';

        //selddate=this.selectDayDate(dy,daycount, this.selecteddate, this.selectedday);
        selddate=this.selectDayDate(dy);
        var d=selddate[0];
        var daycount=selddate[2];

        if(dy==1)
            weekdays[1] = this.selectedday;

        var maxViewDays=7;
        for(var i=1;i<=maxViewDays;i++){
            if(i<=dy){
                if(d>selddate[3])
                    d=1;

                htmlStr+='<div id='+this.id+'calDay'+i+' style="width:'+wd+'%; display:block; float:left;vertical-align:middle; text-align:center; font-weight:bold; font-size:11px;">'+weekdays[daycount++]+'/'+d+'</div>';

                if(dy!=1)
                    d++;
            }else
                htmlStr+='<div id='+this.id+'calDay'+i+' style="width:'+wd+'%; display:none; float:left;vertical-align:middle; text-align:center; font-weight:bold; font-size:11px;"></div>';
        }
        htmlStr+='</div>';
        return htmlStr;
    },

    createAllDays: function(dy) {
        var htmlStr = '<div id='+this.id+'allDayGrid style="margin-left: 40px; background-color:#BDD3EF; color:#15428B; height:15px; width:95%; border:2px solid #e8eef7; background-color:#D1DFF0;">';
        for(var i=0; i<7; i++)
            htmlStr += '<div id="'+this.id+'_allDay_'+i+'" style="border:1px solid #e8eef7; width:'+(98/dy)+'%; height:100%; float:left; display:'+(i<dy?"block;":"none;")+'"></div>';
        htmlStr += '</div>';

        htmlStr += '<div id='+this.id+'bottomline style="height:10px; width:100%; border-bottom:2px solid #e8eef7;"></div>';
        return htmlStr;
    },

    createHours:function(hrs){
//        var cellobj=Array();
        var hr;
        var str = '<div id=' + this.id + 'table1 style = "width:42px; background-color:#E8EEF7; color:#15428B;">';
        for(var i=0;i<hrs;i++){
            hr=Wtf.cal.utils.getHourStr(i);
            str+='<div id='+this.id+'hour_'+i+' style="display:block;width:42px;height:'+Wtf.fixHeight+'px;font-size:10px;text-align:center;vertical-align:top;" >'+hr[0]+hr[1]+'</div>';
//            str+='<div id='+this.id+'hour_'+i+' style="display:block;border:1px solid #e8eef7;width:42px;height:'+Wtf.fixHeight+'px;font-size:11px;text-align:center;vertical-align:top;" >'+hr[0]+hr[1]+'</div>';
        }
        str+="</div>";
        return str;
    },

    selectDayDate:function(dy){
        var selddate=new Array(8);
        var dayNo = this.seldate.format('N');
        if(dayNo == 7){
            dayNo = 0;
        }
        var dt = new Date(this.seldate);
        if(dy==7){
            dt = new Date(dt).add(Date.DAY, -dayNo);
        }else if(dy ==this.actualworkingdays){
            dt = new Date(dt).add(Date.DAY, -dayNo);
        }
        var dt1 = new Date(dt).add(Date.DAY,(dy-1));
        selddate[0]=dt.format('j');
        selddate[1]=dt.format('M');
        selddate[2]=((dy==7)?0:1);
        selddate[3]=dt.getLastDateOfMonth().format('j');
        selddate[4]=dt1.format('j');
        selddate[5]=dt1.format('M');
        selddate[6]=dt.format('Y');
        selddate[7]=dt1.format('Y');
        return selddate;
    },

    getdroi:function(){
        var dtr1=new Date(this.seldate);
        var dtr2=new Date(this.seldate);
        dtr1.setDate(1);

        var mnt1=dtr1.getMonth();
        if(mnt1!=11){
            dtr2.setMonth(mnt1+2);
            dtr2.setDate(1);
            dtr2.setDate(dtr1.getDate()-1);
        }
        else{
            dtr2.setMonth(0);
            dtr2.setDate(31);
            dtr2.setYear(dtr1.getFullYear()+1);
        }

        if((!this.droi1) || (!this.droi2)){
            this.droi1=dtr1;
            this.droi2=dtr2;
            if(this.eStore && this.eStore.getCount())
                this.eStore.removeAll();
        }
        else{
            var ost=this.droi1.format("Y-m-d H:i:s");
            var oed=this.droi2.format("Y-m-d H:i:s");
            var nst=dtr1.format("Y-m-d H:i:s");
            var ned=dtr2.format("Y-m-d H:i:s");

            if(ost!=nst && oed!=ned){
/*                if((nst>ost && nst<oed && ned>oed) || (ned>ost && ned<oed && nst<ost)){
                    this.droi1=dtr1;
                    this.droi2=dtr2;
                }
                else{*/
                    if(this.eStore && this.eStore.getCount())
                        this.eStore.removeAll();
                    var cids=[];
                    var selNode=this.calTree.getChecked();
                    for(var i=0;i<selNode.length;i++){
                        selNode[i].attributes.request=false;
                        cids[i]=selNode[i].id;
                    }
                    this.droi1=dtr1;
                    this.droi2=dtr2;
                    this.chkCalList=cids;
//                }
            }
        }
    },

    getStartEndDate:function(){
        var dt=[];
        dt[0]=new Date(this.seldate);
        dt[1]=new Date(this.seldate);
        if(this.currentview!="MonthView"){
            var index=this.seldate.format("w");
            var s=0,e=0;
            if(this.currentview=='DayView'){
                dt[1].setDate(dt[0].getDate());
                this.startdate=dt[0];
                dt[1].setHours(23,59,59);
                this.enddate=dt[1];
                this.getdroi();
                return dt;
            }
            else if(this.currentview=='WeekView'){
                s=-index;
                e=6-index;
            }
            else if(this.currentview=='WorkWeekView'){
                s=-index;
                e=6-index;
            }
            dt[0].setDate(this.seldate.getDate()+s);
            dt[1].setDate(this.seldate.getDate()+e);//+1); //1 day more
            dt[1].setHours(23,59,59);
       }
       else{
            var monthDate=new Date(this.seldate);
            monthDate.setDate(1);
            dt[0]=new Date(monthDate);

            monthDate.setMonth(monthDate.getMonth()+1);
            monthDate.setDate(monthDate.getDate()-1);
            dt[1]=monthDate;
            dt[1].setHours(23,59,59);
       }
       this.startdate=dt[0];
       this.enddate=dt[1];
       this.getdroi();
       return dt;
    },

    onCalViewChange:function(e){

        if(this.currentview!="agendaView"){
            this.getStartEndDate();
            this.getCalIds();
            this.triggerEvent();
            this.getCurrentDate();
        }

    },

    onCellClick:function (e){
//        counter++;
        var a = this.id.split('cell_');
        var aa=a[1].split('_');
        //a[1]=aa[0];
        //var t = Wtf.cal.utils.getHourStr(a[1]);
        a[1] = parseInt(aa[0]);
        //a[2] = a[1]+1;
        //var t1 = Wtf.cal.utils.getHourStr(a[1]);
        //var timeStr = t[0]+t[1]+"-"+t1[0]+t1[1];
        var timeStr = a[1]+ "-"+ (a[1]+1);
        var w1 = Wtf.get(this.id).dom.offsetWidth;
        var MainPanel=Wtf.getCmp(a[0]);
        var panewidth = Wtf.get(MainPanel.id+MainPanel.currentview+'eventPanel').dom.offsetWidth;
        w1 = (w1/panewidth)*100-1;
//        eventWin = new Wtf.cal.eventWindow(MainPanel);
        if(!Wtf.getCmp(MainPanel.id + "weekAction1").pressed){
            var m = this.id.split('_');
            var curr_Day = MainPanel.workDayArr[parseInt(m[2],10)]
            var x = this.id.substring(0, this.id.length - 1);
            x = x +curr_Day;
            MainPanel.eventWin.showWindowWeek(x,timeStr,Wtf.fixHeight,w1);
        } else {
            MainPanel.eventWin.showWindowWeek(this.id,timeStr,Wtf.fixHeight,w1);
        }
    },

    allDayDblClick:function(e) {
        // Used to call ShowWindowWeek() for all day event when doubled clicked on the "All Day Grid""
        var a = this.id.split('_allDay_');
        var MainPanel = Wtf.getCmp(a[0]);

        MainPanel.getStartEndDate();
        var stdt = MainPanel.startdate;

        var allDayStr = stdt.add(Date.DAY, parseInt(a[1])).format("Y-m-d");

        MainPanel.eventWin.showWindowWeek(a[0], allDayStr + "/" + allDayStr, 0, 0, false, false, true);
    },

    removeFunction:function(currentView){
        if(!currentView||currentView==""||currentView==undefined){
            currentView=this.currentview;
        }
        this.getval();
        if((currentView==('DayView'))||(currentView==('WeekView'))||(currentView==('WorkWeekView'))){
            var panelelement = Wtf.get(this.id+currentView+'eventPanel');
            for(var ii=panelelement.dom.childNodes.length-1;ii>0;ii--){
                var childObj=Wtf.get(panelelement.dom.childNodes[ii].id);
                childObj.remove();
            }
            var nDays = 1;
            if(currentView == "WorkWeekView")
                nDays = this.actualworkingdays;
            else if(currentView == "WeekView")
                nDays = 7;
            for(var i=0; i<nDays; i++) {
                // For removing the multi day or all day events from the "all day grid"
                var allDayPanel = Wtf.get(this.id + "_allDay_" + i);
                for(ii = allDayPanel.dom.childNodes.length-1; ii>=0; ii--)
                    Wtf.get(allDayPanel.dom.childNodes[ii].id).remove();
            }
        }
    },

    HidingOtherCalendarEvent : function(CalID){
        // Used to hide the events of the unchecked calendar
        var calculateWhom = [];
        var cntCalWhom = 0;
        var a = Wtf.query("div[name="+CalID+"]");
        for(var i=0;i<a.length;i++){
            this.HidingOtherSingleEvent(a[i].id);
            // Storing the days for which the calculateHowMore function has to be called for
            if(this.currentview == "MonthView" && calculateWhom.indexOf(a[i].parentNode.id) == -1)
                calculateWhom[cntCalWhom++] = a[i].parentNode.id;
        }
        if(this.currentview == "MonthView") {
            if(cntCalWhom >= this.seldate.getDaysInMonth())
                // if calculateHowMore() has to be called for all the days
                this.CalculateHowMore();
            else
                for(var cnt = 0; cnt<cntCalWhom; cnt++)
                    // calling calculateHowMore() for particular days
                    this.CalculateHowMore(null, null, Wtf.get(calculateWhom[cnt]).dom);
        }
    },

    ShowingOtherCalendarEvent : function(CalID){
        // Used to show the events of the checked calendar
        var calculateWhom = [];
        var cntCalWhom = 0;
        var a = Wtf.query("div[name="+CalID+"]");
        for(var i=0;i<a.length;i++){
            this.ShowingOtherSingleEvent(a[i].id);
            // Storing the days for which the calculateHowMore function has to be called for
            if(this.currentview == "MonthView" && calculateWhom.indexOf(a[i].parentNode.id) == -1)
                calculateWhom[cntCalWhom++] = a[i].parentNode.id;
        }
        if(this.currentview == "MonthView") {
            if(cntCalWhom >= this.seldate.getDaysInMonth())
                // if calculateHowMore() has to be called for all the days
                this.CalculateHowMore();
            else
                for(var cnt = 0; cnt<cntCalWhom; cnt++)
                    // calling calculateHowMore() for particular days
                    this.CalculateHowMore(null, null, Wtf.get(calculateWhom[cnt]).dom);
        }
    },

    HidingOtherSingleEvent : function(EventID){
        EventID = Wtf.get(EventID).dom;
        EventID.className += ' HidingEvent';
    },

    ShowingOtherSingleEvent : function(EventID){
        EventID = Wtf.get(EventID).dom;
        var a = EventID.className.split('HidingEvent');
        if(a[0].trim() != "")
            EventID.className = a[0]+" " + a[2];
        else
            EventID.className = "";
    },

    ChangingOtherColor:function(selectedColor,CalendarID){
        var eventColor = this.calTree.getNodeById(CalendarID).getUI().GetBackColor();
        var titleColor = this.ApplyColorTransform(eventColor, 0.68);
        if(this.currentview == "agendaView")
            this.reloadAgenda();
        else {
            var a  = Wtf.query("div[name="+CalendarID+"]");
            for(var i=0;i<a.length;i++){
                if(a[i].className !="moreDiv"){
                    this._changeEventItemColor(CalendarID, a[i].id, eventColor, titleColor);
                }
            }
        }
    },

    ChangingOtherSingleEventColor:function(CalendarID,EventID){
        var eventColor = this.calTree.getNodeById(CalendarID).getUI().GetBackColor();
        var titleColor = this.ApplyColorTransform(eventColor, 0.68);
        this._changeEventItemColor(CalendarID, EventID, eventColor, titleColor);
    },

    _changeEventItemColor:function(CalendarID, EventID, eventColor, titleColor){
        if(this.currentview == "MonthView"){
            this.ChangingMonthSingleEventColor(CalendarID,EventID);
        }else{
            var a = Wtf.get(this.id + "wrapper_" + EventID.split("_")[1]);
            if(a == null)
                // if multi day event then only color the background
                this.ChangingMonthSingleEventColor(CalendarID,EventID);
            else {
                // if appoinment lasting less than one day then color the tiltle, text, body/wrapper
                a.dom.style.backgroundColor = titleColor;
                var a1 = Wtf.get(EventID).dom;
                a1.style.backgroundColor = eventColor;
                a1.style.borderColor = titleColor;
            }
        }
    },

    onWeekViewClick:function(){
        var obj1=Wtf.getCmp(this.id+'dateText1');
        this.RemoveMainPanelContent();
        var selddate=new Array(8);
        this.getval();
        this.seldate = this.calendar.getValue();
        selddate=this.selectDayDate(7);
        Wtf.getCmp(this.id+'weekAction1').disable();
        if((this.currentview=="DayView")||(this.currentview=="WorkWeekView")){
            obj1.setText(selddate[1]+' '+selddate[0]+', '+selddate[6]+' - '+selddate[5]+' '+selddate[4]+', '+selddate[7]);
            this.ModifyView(this.selectedday, 7, 48, true);
            this.currentview = 'WeekView';
        }
        else{
            this.currentview ='WeekView';
            obj1.setText(selddate[1]+' '+selddate[0]+', '+selddate[6]+' - '+selddate[5]+' '+selddate[4]+', '+selddate[7]);
            this.AddDayView(this.selectedday, 7, 48);
       }
       this.onCalViewChange();
       this.highlightDay(7);
    },

    onWorkWeekViewClick:function(){
        var obj1=Wtf.getCmp(this.id+'dateText1');
        this.RemoveMainPanelContent();
        Wtf.getCmp(this.id+'workweekAction1').disable();
        this.seldate = this.calendar.getValue();
        this.getval();
        var selddate = this.selectDayDate(7);
        if((this.currentview=="DayView")||(this.currentview=="WeekView")){
           obj1.setText(selddate[1]+' '+selddate[0]+', '+selddate[6]+' - '+selddate[5]+' '+selddate[4]+', '+selddate[7]);
            if(this.currentview=="DayView")
                this.ModifyView_workweek(this.selectedday, this.actualworkingdays, 48, true);
            else
                this.ModifyView_workweek(this.selectedday, this.actualworkingdays, 48, false);
            this.currentview = 'WorkWeekView';
        }
        else{
            this.currentview = 'WorkWeekView';
            obj1.setText(selddate[1]+' '+selddate[0]+', '+selddate[6]+' - '+selddate[5]+' '+selddate[4]+', '+selddate[7]);
            this.AddDayView(this.selectedday,this.actualworkingdays,48);
        }
        this.onCalViewChange();
        this.highlightDay(this.actualworkingdays);
    },

    ModifyView_workweek:function(selectedday,nDays,nHours,stepFlag,currentView){
        var currPanel=null;
        var currDays=1;
        var currHours=nHours;
        var dayDiff=0;
        var nextPanelId="";
        var nextView="";

        if(!currentView||currentView==""||currentView==undefined)
            currentView=this.currentview;

        if((currentView=="DayView") || (currentView=="WeekView")){
            if(stepFlag){
                if(currentView=="DayView"){
                    nextPanelId="workWeekPanel";
                    nextView="WorkWeekView";
                    currPanel=Wtf.getCmp(this.id+'dayPanel');
                }
                else if(currentView=="WorkWeekView"){
                    currDays=nDays;
                    nextPanelId="weekPanel";
                    nextView="WeekView";
                    currPanel=Wtf.getCmp(this.id+'workWeekPanel');
                }
                this.addDays_workweek(nDays);

                var wd=(99/nDays);
                for(var k=0;k<nDays;k++){
                    var p=Wtf.getCmp(this.id+"_"+k);
                    p.show();
                    p.el.dom.style.width=wd+"%";
                }

                currPanel.hide();
                Wtf.ComponentMgr.unregister(currPanel);
                var cellPanel=Wtf.get(this.id+currentView+'eventPanel');
                cellPanel.dom.id=this.id+nextView+"eventPanel";

                var idd =this.id+nextPanelId;
                currPanel.id = idd;
                currPanel.el.id = idd;
                currPanel.el.dom.id = idd;
                Wtf.ComponentMgr.register(currPanel);

            } else {
                if(currentView=="WeekView"){
                    currDays=7;
                    if(nDays==1){
                        nextPanelId="dayPanel";
                        nextView="DayView";
                    }
                    else{
                        nextPanelId="workWeekPanel";
                        nextView="WorkWeekView";
                    }
                    currPanel=Wtf.getCmp(this.id+'weekPanel');
                }
                else if(currentView=="WorkWeekView"){
                    currDays=this.actualworkingdays;
                    nextPanelId="dayPanel";
                    nextView="DayView";
                    currPanel=Wtf.getCmp(this.id+'workWeekPanel');
                }
                this.addDays_workweek(nDays);

                var wd=(99/nDays);
                for(var k=0;k<currDays;k++){
                    var p=Wtf.getCmp(this.id+"_"+k);
                    if(k>=nDays)
                        p.hide();
                    else
                        p.el.dom.style.width=wd+"%";
                }
                Wtf.ComponentMgr.unregister(currPanel);
                var cellPanel=Wtf.get(this.id+currentView+'eventPanel');
                cellPanel.dom.id=this.id+nextView+"eventPanel";

                var idd =this.id+nextPanelId;
                currPanel.id = idd;
                currPanel.el.id = idd;
                currPanel.el.dom.id = idd;
                Wtf.ComponentMgr.register(currPanel);
            }
            this.showCalPanel(currPanel);
        }
    },  
    addDays_workweek:function(nDays){
        var weekdays=["Sun","Mon","Tue","Wed","Thu","Fri","Sat"];
        var selddate=new Array(6);
        var wd=93.5/nDays;
        this.getval();
        selddate=this.selectDayDate(7);
        var d=selddate[0];
        var daycount = 0;

        var timeDiv=Wtf.get(this.id+'calDay0');
        timeDiv.dom.style.display="block";
        timeDiv.dom.style.width="4.5%";

        var allDayWd = 98/nDays;

        var maxViewDays=7;
        for(var i=1;i<=maxViewDays;i++){
            var tempDiv=Wtf.get(this.id+'calDay'+i);
            var tempAllDayDiv = Wtf.get(this.id+'_allDay_'+(i-1));
            if(i<=nDays){
                if(d>selddate[3])
                    d=1;
                tempAllDayDiv.dom.style.display = "block";
                tempAllDayDiv.dom.style.width = allDayWd+'%';

                tempDiv.dom.style.display="block";
                tempDiv.dom.style.width=wd+'%';
                while(i<7){
                    if(this.NonworkWeekDays.indexOf(daycount.toString()) != -1){
                        daycount++;
                        if(nDays!=1){
                             d++;
                             if(d>selddate[3])
                                 d=1;
                        }
                    } else {
                        break;
                    }
                }
                tempDiv.dom.innerHTML=weekdays[daycount]+'/'+d;
                daycount++;
                if(nDays!=1)
                    d++;
            }
            else{
                tempAllDayDiv.dom.innerHTML = "";
                tempAllDayDiv.dom.style.display = "none";

                tempDiv.dom.innerHTML="";
                tempDiv.dom.style.display="none";
            }
        }
    },

    onMonthViewClick:function(item){
        this.seldate = this.calendar.getValue();
        this.RemoveMainPanelContent();
        this.RemoveDiv();
        this.AddMonth();
        var mp=Wtf.getCmp(this.id+'MonthPanel1');
        this.showCalPanel(mp);
        Wtf.getCmp(this.id+'monthAction1').disable();
        //var dd = this.calendar.getValue();
        //var obj1=Wtf.getCmp(this.id+'dateText1');
        Wtf.getCmp(this.id+'dateText1').setText(Date.monthNames[this.seldate.getMonth()] + " " + this.seldate.getFullYear());
        this.currentview = 'MonthView';
        this.onCalViewChange();
    },

    RemoveDiv:function(){
        /*i=1;
        var k = document.getElementById(this.id+'MonthPanel');
        Wtf.get(this.id+'MonthPanel').remove(this.id+'MonthPanel',true);*/
        var mp=Wtf.getCmp(this.id+'MonthPanel1');
        if(mp)
            mp.destroy();

    },

    AddMonth:function (){
        //If expand/more window present then remove it
        var expandWin = Wtf.getCmp(this.id.substring(0, this.id.indexOf('calctrl'))+"Expand");
        if(expandWin)
            expandWin.destroy();

        this.ResizeArray = [];
        var i = this.calendar.getValue();
        TodayMonth = i.getMonth()+1;
        TodayYear = i.getFullYear();

        var calMonthDragPlugin = [];
        if(!this.archived)
            calMonthDragPlugin = [new Wtf.DataView.calDragSelector({calDrag: true, calContainer: this, calAllDayWeek: false, calMonthView: true})]

        var monthDV = new Wtf.DataView({
            cls: 'centerCalDview',
            store:  new Wtf.data.JsonStore({
                url: 'get-images.php',
                root: 'images',
                fields: ['name', 'url', {name:'size', type: 'float'}, {name:'lastmod', type:'date', dateFormat:'timestamp'}]
            }),
            id: this.id + 'dMonthView',
            multiSelect: true,
            overClass: 'x-view-over',
            itemSelector: 'div.thumb-wrap',
            border: false,
            plugins: calMonthDragPlugin,
            emptyText: "<div style = 'background:#fff; width : 100%; height: 100%' id='" + this.id + "MonthPanel'></div>"
        });

        var mainMonthPanel = new Wtf.Panel({
            id: this.id + 'MonthPanel1',
            border: false,
            layout: 'fit',
            items: monthDV
        });

        this.add(mainMonthPanel);
        this.doLayout();

        var a = Wtf.get(this.id+'MonthPanel').dom;
        a.innerHTML +=	"<div id='"+this.id+"MonthDayTitle' style = 'background-color:#BDD3EF;height:19px;padding-top:3px;font-weight:bold;color:#15428B;font-size:11px; width:100%' >"+
                        "<div class='innerDiv ctext'>Sun</div>"+
                        "<div class='innerDiv ctext'>Mon</div>"+
                        "<div class='innerDiv ctext'>Tue</div>"+
                        "<div class='innerDiv ctext'>Wed</div>"+
                        "<div class='innerDiv ctext'>Thu</div>"+
                        "<div class='innerDiv ctext'>Fri</div>"+
                        "<div class='innerDiv ctext'>Sat</div>"+
                        "</div>"+
                        "<div id = '"+this.id+"MonthDayPanel' class = 'TotalDayPanel'></div>";
        this.AddDays();
        this.showCalPanel(Wtf.getCmp(this.id+'MonthPanel1'));
    },

    AddDays:function(){
        var k = this.CalculateDate();
        var p=k[2],flag=1,m,cls='BlurtaskPanel';
        var mainDayClass = 'OneDayPanel';
        this.monthNoOfDays = k[4];      // Setting the number of days in the month and calculating the height of the month panel accordingly
        this.monthDayHeight = ((Wtf.get(this.id).dom.offsetHeight - 50) * (this.monthNoOfDays == 35? 19.3: 16.3)/100);
        if(k[4]==42)
        {
            mainDayClass = 'OneDayPanel SmallDayPanel';
        }
        var a = Wtf.get(this.id+'MonthDayPanel').dom;
        for(var i=0;i<k[4];i++){
            if(i==k[0]){
                flag = 0;
                p =0;
                cls = 'taskPanel';
            }
            else if(p==k[1]&&flag==0){
                p=0;
                flag = 1;
                k[3] = k[3]+2;
                if(k[3]==13)
                    k[3]=1;
                cls='BlurtaskPanel';
            }
            p++;
            m=p;
            if(flag ==1){
                if(k[3]==14)
                    k[3]=2;
                m = p + "/"+ k[3];
            }
            else if(m<10)
                m = "0"+m;

            var id1 = this.id+"Day2"+m;
            var id2 = this.id+"Day1"+m;

            Wtf.DomHelper.insertHtml('beforeEnd',a, "<div class = '" + mainDayClass + "' id ='" + id1 + "'>"+
            "<div class = '" + cls + "' id='" + id2 + "'><div class = 'taskPanel1'>" + m + "</div></div>"+
            "</div>");

            Wtf.get(id2).addListener('click',this.DateSglClick,this);
            if(!this.archived)
                Wtf.get(id2).addListener('dblclick',this.DateDblClick,this);

     //       var z = TodayMonth + "/" + m + "/" + TodayYear;
           /* var zz = hash.containsKey(z);
            if(zz){
                var zzz=document.getElementById(this.id+'Day1'+m);
                var vall = hash.get(z);
                this.AddItem(vall,zzz,DefaultEventName);
            }*/

        }
        this.MakeDraggable(k[1]);
        this.doLayout();
    },

    DateDblClick:function(e){
        e.preventDefault();
        var dbclkDay = e.getTarget();
        var dayStr = "";

        if(dbclkDay && dbclkDay.id.indexOf("Day1") == -1)
            // Acquiring the appropriate double clicked day as target could be the "task panel"
            if(dbclkDay.parentNode.id.indexOf("Day1") != -1)
                dbclkDay = dbclkDay.parentNode;

        if(dbclkDay && dbclkDay.id.indexOf("Day1") != -1 && dbclkDay.id.indexOf("more") == -1) {
            var st = dbclkDay.id.split('Day1')[1];
            if(st.indexOf("/") == -1) {
                dayStr = this.seldate.format("Y-m-") + st;
                dayStr = dayStr + "/" + dayStr;
                this.eventWin.showWindowWeek("", dayStr, 0, 0, false, true);
            }
        }
    },

    AddItem: function(obj, ClickedDay, CalendarID, totalday, cnt, eid, startts, flagWhere, allday, deleteflag, recData) {
        var createEvent = true;
        var tempw = Wtf.get(ClickedDay.id).dom.offsetWidth;

        // Calculating the number of chars of the event's subject that can be shown
        obj = Wtf.util.Format.ellipsis((cnt<0? startts + " " + obj : obj), tempw/ (this.currentview == "WeekView" ? 9 : 8));
        var recStart = Wtf.cal.utils.sqlToJsDate(recData.startts);
        var recEnd = Wtf.cal.utils.sqlToJsDate(recData.endts);
        var tStr = "";
        if(recData.allday) {
            if(recStart.format("Y-m-d H:i:s") == recEnd.format("Y-m-d H:i:s"))
                tStr = recStart.format("M j, Y");
            else
                tStr = "From " + recStart.format("M j, Y") +" to "+ recEnd.format("M j, Y");
        } else
            tStr = "From " + recStart.format("M j, Y g:i A") +" to "+ recEnd.format("M j, Y g:i A");

        var EventID = this.id+"e_"+eid;
        var currEvent = Wtf.get(EventID);
        if(currEvent) {
            if(currEvent.dom.parentNode.id != ClickedDay.id)
                currEvent.dom.parentNode.removeChild(currEvent.dom);
            else {
                currEvent.dom.firstChild.innerHTML=obj;
                createEvent = false;
            }
        }
        if(createEvent) {
            var eventClass = "";
            if(flagWhere == 1)                          // For start day of the events show "more right""
                eventClass = "moreright";
            else if(flagWhere == 2 || flagWhere == 3)   // For mid-days and last day of the events show "more left"
                eventClass = "moreleft";
            if(allday)                                  // Dummy 'allDay' class used for indentifying the all day events
                eventClass += " allDay";

            var Peid = eid.split("CNT_")[0];
            var perm = Wtf.cal.utils.getPermissionLevel(this, CalendarID);
            var nodecolor= this.calTree.getNodeById(CalendarID).getUI().GetBackColor();

            var contHtml = "<span style='font-size:12px; color:" + (cnt < 0? nodecolor : "white") + "; height:17px; float:left; margin-left:1px;' wtf:qtitle='"+ recData.subject +"' wtf:qtip='"+ tStr +"'>"+ obj +"</span>";
            if(!this.archived && perm != 3 && deleteflag == '0')
                // Checking if the user has permissions and whether project is archieved, if not then render the close image
                contHtml += "<div class='closeDiv'> <img alt='X' src= " + "'../../images/deleteLink.gif'" +
                            "style='height:11px;' id='delLink_"+ eid +"' wtf:qtip='Delete this event'/></div> ";

            var newEventNode = Wtf.DomHelper.append(ClickedDay, {
                tag: 'div',
                cls: eventClass,
                id: EventID,
                name: CalendarID,
                peid: Peid,
                style: "height:17px; margin-bottom:1px; " + (cnt >= 0? "background-color:" + nodecolor : ""),
                html: contHtml
            });
            currEvent = Wtf.get(EventID);
            if(!this.archived && (perm != 3) && Wtf.get('delLink_' + eid)) {
                Wtf.get('delLink_' + eid).addListener("click", function(obj) {
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.336'), function(btn) {
                        if(btn == 'yes') {
                            var rec = [Peid, CalendarID];
                            this.deleteEvent(rec, "0");
                        }
                    }, this);
                }, this);
            }
            currEvent.on("dblclick", this.eventWin.showEventDetails);
            if((perm=="" || perm==1) && !this.archived)
                this.MakeDropSource(currEvent.dom);
        }
        if(!this.CheckforChecked(CalendarID))
            this.HidingMonthCalendarSingleEvent(EventID);
        return newEventNode;
    },

    /*onEventClick:function(e){
        eventWin = new Wtf.cal.eventWindow(this);
        var t  = this.eventWin.getDateAndValue(e,TodayMonth,TodayYear);
        this.RemoveMainPanelContent();
        var eventfrm = new Wtf.cal.eventForms(this);
        eventfrm.AddingTabPanelEvent();
        var a = [];
        a[0] = e.innerHTML;
        a[1] = t[0];
        a[2] = t[1];
        a[3] = t[2];
        a[4] = t[3];
        this.AddingValue(a);
    },*/

    addCalendarTab1:function(){
        this.hideFormtoolbar();
        this.showMaintoolbar();
        this.RemoveMainPanelContent();
        this.formview = null;
        if(this.currentview!="agendaView"){
            if(this.currentview=='DayView'){
    //            Wtf.get(this.id+'dayPanel').dom.style.display="block";
                this.AddDayView(this.selectedday,1,48);
                Wtf.getCmp(this.id+'dayAction1').disable();
            }
            else if(this.currentview=='WorkWeekView'){
    //            Wtf.get(this.id+'workWeekPanel').dom.style.display="block";
                this.AddDayView(this.selectedday,this.actualworkingdays,48);
                Wtf.getCmp(this.id+'workweekAction1').disable();
            }
            else if(this.currentview=='WeekView'){
    //            Wtf.get(this.id+'weekPanel').dom.style.display="block";
                this.AddDayView(this.selectedday,7,48);
                Wtf.getCmp(this.id+'weekAction1').disable();
            }
            else if(this.currentview=='MonthView'){
                this.RemoveDiv();
                this.AddMonth();
                Wtf.getCmp(this.id+'monthAction1').disable();
                var mp=Wtf.getCmp(this.id+'MonthPanel1');
                this.showCalPanel(mp);
            }
            this.onCalViewChange();
        }
        else{
//            this.Addagenda(this);
            this.RemoveMainPanelContent();
            var ap=Wtf.getCmp(this.id+'agendaPanel');
            if(!ap){
                //var obj1 = Wtf.getCmp(this.id + 'dateText1');
                Wtf.getCmp(this.id + 'dateText1').setText(this.selecteddate + ' ' + Date.monthNames[this.seldate.getMonth()] + " " + this.seldate.getFullYear());
                this.Addagenda();
                ap=Wtf.getCmp(this.id+'agendaPanel');
                this.showCalPanel(ap);
            }
            else{
                this.showCalPanel(ap);
//                this.reloadAgenda();
            }
            this.currentview='agendaView';
            Wtf.getCmp(this.id+'agendaAction1').disable();
//            Wtf.getCmp(this.id+'pdfAction1').enable();
        }
    },

    DateSglClick:function(e){
//        var k = e.target.id;
//        eventWin = new Wtf.cal.eventWindow(this);
        var t = this.eventWin.getDateAndValue(e,TodayMonth,TodayYear);
        if(t)
            this.DifferBackGround(t.format("d"));
    },

    DifferBackGround:function(m){
        if(this.selectedDate!=null){
            this.selectedDate.dom.style.backgroundColor = "";
            var daaa = this.selectedDate.dom.childNodes[0];
            daaa.style.backgroundColor = "";
        }
        this.selectedDate =  Wtf.get(this.id+'Day1'+m);
        if (this.selectedDate) {
            daaa = this.selectedDate.dom.childNodes[0];
            daaa.style.backgroundColor = '#BBCCDD';
            this.selectedDate.dom.style.backgroundColor = '#FFFFCC';
        }
    },

    changeCalDate:function(d){
        if (d){
            this.calendar.setValue(d);
        }
    },

    /*MakeDropSource2:function(ClickArray){
        for(var i=0;i<ClickArray.length;i=i+2){
            this.MakeDropSource(ClickArray[i]+"Showing"+ClickArray[i+1]);
        }
    },*/

   /* getElementLeft:function(Elem){
        var elem;
        if(document.getElementById)
            var elem = document.getElementById(Elem);
        else if (document.all)
            var elem = document.all[Elem];
        xPos = elem.offsetLeft;
        tempEl = elem.offsetParent;
        while (tempEl != null) {
            xPos += tempEl.offsetLeft;
            tempEl = tempEl.offsetParent;
        }
        return xPos;
    },*/

   /* getElementTop:function(Elem){
        if(document.getElementById) {
            var elem = document.getElementById(Elem);
        } else if (document.all){
            var elem = document.all[Elem];
        }
        yPos = elem.offsetTop;
        tempEl = elem.offsetParent;
        while (tempEl != null) {
            yPos += tempEl.offsetTop;
            tempEl = tempEl.offsetParent;
        }
        return yPos;
    },*/

    ifArrayContains:function(elem){
        for(var i =0;i<this.ResizeArray.length;i++){
            if(this.ResizeArray[i]==elem)
                return true;
        }
        return false;
    },

    AddToArray:function(elem){
        this.ResizeArray.push(elem);
    },

    RemoveExpandWin:function(){
        var k = Wtf.getCmp(this.id.substring(0, this.id.indexOf('calctrl'))+'Expand');
        if(k!=undefined){
            var innerchild = Wtf.get("tempexpandDiv");
            if(innerchild){
                var temparray = [];
                for(var i=0;i<innerchild.dom.childNodes.length;i++)
                    temparray[i] = innerchild.dom.childNodes[i];
                var limit = temparray.length;
                for(var j=1;j<limit;j++){
                    temparray[j].style.display="none";
                    k.eventContainer.appendChild(temparray[j]);
                }
            }
        }
    },

    MakeDraggable:function(Days){
        for(var i=1;i<=Days;i++){
            var id2 =this.id+"Day1"+i;
            if(i<10)
                id2 =this.id+"Day10"+i;
            //this.MakeDropZone(id2);
            new Wtf.dd.DropZone(id2, {ddGroup:'group'});
        }
    },

//    MakeDropSource1:function(CalID){
//        var a = document.getElementsByName(CalID);
//        for(i=0;i<a.length;i++)
//        this.MakeDropSource(CalID);
//    },

    ChangingMonthColor:function(CalendarID){
        var a = Wtf.query("div[name="+CalendarID+"]");
        for(var i=0;i<a.length;i++)
            this.ChangingMonthSingleEventColor(CalendarID,a[i].id);
    },

    ChangingMonthSingleEventColor:function(calId,eventid){
        var nodecolor= this.calTree.getNodeById(calId).getUI().GetBackColor();
        var a = Wtf.get(eventid).dom;
        if(eventid.indexOf("CNT") != -1 || a.className.indexOf("allDay") != -1 || a.className.indexOf("moreright") != -1 || a.className.indexOf("moreleft") != -1) {
            // if multi days or all day event then show colored background
            a.style.backgroundColor = nodecolor;
            a.childNodes[0].style.color = "white";
        }
        else
            a.childNodes[0].style.color = nodecolor;
    },

    HidingMonthCalendarSingleEvent:function(FullID){
        var a = Wtf.get(FullID);
        if(a)
            a.dom.className = "eventPanel HidingEvent";
    },

    ShowingMonthCalendarSingleEvent:function(FullID){
        var a = Wtf.get(FullID);
        if(a)
            a.dom.className = "eventPanel";
    },

    CheckforChecked:function(CalID){
        var t = this.calTree.getChecked();
        for(var i=0;i<t.length;i++){
            if(CalID==t[i].id)
                return true;
        }
        return false;
    },
    checkMoreEvents: function(currentDom){
        var limit = Math.round((currentDom.offsetHeight - 18.5)/ 18.5);    //dfactor = 20
        if(!Wtf.isIE6 && currentDom.offsetHeight == 0 && limit == -1)
            limit = Math.round((this.monthDayHeight -18.5)/ 18.5);
        if(Wtf.isIE6)
            limit = Math.round((this.monthDayHeight -18.5)/ 18.5);
        var moreobj = Wtf.get(currentDom.id+"more");
        var hiddenEvents = 0;
        var calid = null;
        var j = 0;
        var morePos = 0;    // for finding the postion of the "more obj" in the particular day
        for(var i=0;i<currentDom.childNodes.length;i++){
            var curEvent = currentDom.childNodes[i];
            if(curEvent.className != "moreDiv" && curEvent.getAttribute("name") != null) {
                if(!curEvent.className.match("HidingEvent"))
                    j++;
                if(j<limit){
                    curEvent.style.display = "block";
                    if(!calid)
                        calid = curEvent.getAttribute("name");
                }else{
                    curEvent.style.display = "none";
                    if(this.chkCalList.indexOf(curEvent.getAttribute("name")) != -1)
                        hiddenEvents++;
                }
            } else if(curEvent.className == "moreDiv")
                morePos = i;
        }
        if(hiddenEvents > 0 ){
            if(!moreobj || morePos < limit) {
                // if "more obj" not present then create it
                // or if it exists in between events that are to be shown then remove and create again
                if(moreobj) {
                    var moreParent = moreobj.dom.parentNode;
                    moreParent.removeChild(moreobj.dom);
                }
                var tempobj = Wtf.get(currentDom.id).createChild({tag:'div'});
                tempobj.dom.setAttribute("name", calid);
                tempobj.dom.className = "moreDiv";
                tempobj.dom.style.textAlign = "right";
                tempobj.dom.style.fontSize = "10px";
                tempobj.dom.style.width = "100%";
                tempobj.dom.style.display = "block";
                tempobj.dom.style.cursor = "pointer";
                tempobj.dom.id = currentDom.id + "more";
                tempobj.dom.innerHTML = "+" + hiddenEvents + " more";
                moreobj = tempobj;
            }else{
                moreobj.dom.style.display="block";
                moreobj.dom.innerHTML = "+"+hiddenEvents+" more"
                Wtf.get(moreobj.id).removeListener("click",this.ShowRemaining,this);
            }
            moreobj.addListener("click", this.ShowRemaining, this);
        }else{
            if(moreobj)
                moreobj.dom.style.display="none";
        }
    },
    CalculateHowMore: function(eventel, flag, currentDom){
        if(this.formview == null || this.formview == "CreateCal") {
            if(this.currentview == "MonthView"){
                var k = Wtf.getCmp(this.id.substring(0, this.id.indexOf('calctrl'))+'Expand');
                if(k!=undefined)
                    k.close();
                if(currentDom)
                    this.checkMoreEvents(currentDom);
                else{
                    var totaldays = this.seldate.getDaysInMonth();
                    for(var i1=1;i1<=totaldays;i1++){
                        var compid = this.id+"Day1"+i1;
                        if(i1 < 10)
                            compid = this.id+"Day10"+i1;
                        var tempcomp = Wtf.get(compid);
                        if(tempcomp)
                            this.checkMoreEvents(tempcomp.dom);
                    }
                }
            }
            else if(this.currentview !="agendaView")
                this.onCalViewChange();
        }
    },

    ShowRemaining: function(e){
        e.preventDefault();
        e.stopPropagation();
        var targetdiv = e.target;
            if(targetdiv.className == "moreDiv"){
            if(targetdiv.parentNode){
                var t1 = targetdiv.parentNode;
                var panel1 = Wtf.getCmp(this.id.substring(0, this.id.indexOf('calctrl'))+"Expand");
                var tempobj = Wtf.get(t1.id);
                if(panel1)
                    panel1.close();
                else if(tempobj) {
                    panel1 = new Wtf.Window({
                        id:this.id.substring(0, this.id.indexOf('calctrl'))+"Expand",
                        header:false,
                        footer:false,
                        autoHeight:true,
                        width:'14%',
                        resizable:false,
                        cls:'remainingevents',
                        closable: true,
                        renderTo:document.body,
                        eventContainer:null,
                        shim:false,
                        draggable:false,
                        shadow:false
                    });
                    var container = Wtf.get(this.id +"MonthPanel");
                    var containerHt = container.dom.offsetHeight;
                    var tempdiv = document.createElement("div");
                    tempdiv.id="tempexpandDiv";
                    tempdiv.style.overflowY = "auto";                       // for providing scroll to the expand/more window
                    tempdiv.style.maxHeight = (containerHt - 35) + "px";    // height of buttons in the tbar = 35
                    tempdiv.style.position = "relative";
                    panel1.add(tempdiv);
                    panel1.doLayout();

                    var totalevents = t1.childNodes.length;
                    var temparray = [];

                    for(var i1=0;i1<totalevents;i1=i1+1)
                        temparray[i1] = t1.childNodes[i1];

                    for(var j=1;j<temparray.length;j++){
                        var moreEvent = temparray[j];
                        if(moreEvent.className != "moreDiv"){
                            moreEvent.style.display="block";
                            tempdiv.appendChild(moreEvent);
                            var perm = Wtf.cal.utils.getPermissionLevel(this, moreEvent.getAttribute("name"));
                            if((perm=="" || perm==1) && !this.archived)
                                this.MakeDropSource(moreEvent);
                        }else
                            t1.removeChild(moreEvent);
                    }

                    var winSz = {width: panel1.el.dom.offsetWidth, height: panel1.el.dom.offsetHeight};
                    var containerXY = container.getXY();
                    var containerWd = container.dom.offsetWidth;
                    var tmpXY = tempobj.getXY();
                    var posX = 0;
                    var posY = 0;
                    panel1.alignTo(tempobj, "tl");

                    if(tmpXY[0] >= (containerWd - (tempobj.dom.offsetWidth/5)))         // if the window extends beyond the right boundary
                        posX = containerWd + containerXY[0] - panel1.x - winSz.width;

                    if((containerXY[1] + containerHt) < (panel1.y + winSz.height)) {    // if the window extends over bottom boundary
                        posY = containerXY[1] + containerHt - panel1.y - winSz.height;
                        if(Wtf.isIE6)
                            posY -= 17; //height of each event
                    }

                    if(winSz.height > containerHt)
                        // if the window's height is greater than the container's height then set top of window as container's top
                        // (the events in the remaining part of the window will be shown via the vertical scroll)
                        posY = containerXY[1] - tmpXY[1];

                    panel1.eventContainer=t1;
                    panel1.setTitle(t1.childNodes[0].innerHTML +" "+Date.getShortMonthName(this.seldate.getMonth()));
                    panel1.alignTo(tempobj, "tl", [posX, posY]);
                    panel1.doLayout();
                    panel1.show();
                    panel1.on("beforeclose",function(panel){
                        this.RemoveExpandWin();
                    },this);
                    panel1.on("close",function(panel){
                        this.CalculateHowMore(null, null, tempobj.dom);
                    },this);
                }
            }
        }
    },

    CalculateWeekMore: function() {
        var totaldays = 1;
        if(this.currentview == "WorkWeekView")
            totaldays = this.actualworkingdays;
        else if(this.currentview == "WeekView")
            totaldays = 7;
        var adg = Wtf.getCmp(this.id+"AllDayContainer");
        var allDayGrid = Wtf.get(this.id+'allDayGrid');
        var minHt = 0;
        for(var i1=0; i1<totaldays; i1++) {
            var compid = this.id+"_allDay_"+i1;
            var tempcomp = Wtf.get(compid);
            var numEvents = 0;
            for(var i=0; i<tempcomp.dom.childNodes.length; i++) {
                // calculating the number of events in each day of the "all day" containers
                if(tempcomp.dom.childNodes[i].className.indexOf("HidingEvent") == -1)
                    numEvents++;
            }
            if(minHt < numEvents)
                // maximum number of events in a particular "all day" container
                minHt = numEvents;
        }
        adg.setHeight((minHt*18) + 28);     // (number of events * height of each event) + extra open space for user, to be able to drag
        allDayGrid.dom.style.height = (minHt*18) + 18 + "px";
        Wtf.getCmp(this.id+this.getCurrentPanel()).doLayout();
    },

    /*getBrowserWidth:function(){
        return document.body.clientWidth || innerWidth;
    },*/

    /*getBrowserHeight:function(){
        return document.body.clientHeight || innerHeight;
    },*/

    onAgendaClick:function(){
        this.getval();
        Wtf.getCmp(this.id + "dateText1").setText(this.seldate.format("d M") + " - " + this.seldate.add(Date.DAY, 14).format("d M"));
//        Wtf.getCmp(this.id + 'dateText1').setText(this.selecteddate + ' ' + Date.monthNames[this.seldate.getMonth()] + " " + this.seldate.getFullYear());
        this.RemoveMainPanelContent();
        var ap=Wtf.getCmp(this.id+'agendaPanel');
        if(!ap){
            this.Addagenda();
            ap=Wtf.getCmp(this.id+'agendaPanel');
            this.showCalPanel(ap);
        }
        else{
            this.showCalPanel(ap);
            this.reloadAgenda();
        }
        this.currentview='agendaView';
        Wtf.getCmp(this.id+'agendaAction1').disable();
//        Wtf.getCmp(this.id+'pdfAction1').enable();
    },

//    ontodoClick : function(){
//		var todoContainer = Wtf.getCmp('list_conainer'+this.id);
//		if (!todoContainer) {
//			todoContainer = new Wtf.Panel({
//				title: WtfGlobal.getLocaleText('pm.project.todo.mytodo'),
//				layout: 'fit',
//				id: 'list_conainer' + this.id,
//				closable: true,
//				autoScroll: true,
//				tabType: Wtf.etype.todo,
//				iconCls: getTabIconCls(Wtf.etype.todo),
//				items: [new Wtf.TodoList({
//					autoScroll: true,
//					title: 'To-Do list',
//					id: 'todo_list' + this.id,
//					layout: 'fit',
//					userid: this.ownerid.userid,
//					groupType: this.ownerid.type,
//					animate: true,
//					baseCls: 'todoPanel',
//					enableDD: true,
//					containerScroll: true,
//					border: false,
//					rootVisible: false
//				})]
//			});
//
//			Wtf.getCmp("as").add(todoContainer);
//			Wtf.getCmp("as").doLayout();
//		}
//		Wtf.getCmp("as").setActiveTab(todoContainer);
//    },

    Addagenda : function(){
        dt1= new Date(this.calendar.getValue());
        dt2= dt1;
        dt2.setMonth(dt1.getMonth()+2);
        var record = Wtf.data.Record.create(['eid','cid', 'startts', 'endts','subject','descr','location','showas','priority','recpattern','recend','resources','timestamp', 'allday', 'deleteFlag']);
        var agendastore = new Wtf.data.Store({
            url: Wtf.calReq.cal + 'calEvent.jsp',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty: 'totalCount'
            },record)
        });
        var sm = new Wtf.grid.CheckboxSelectionModel();

        var cm = new Wtf.grid.ColumnModel([sm,{
            header: "",
            dataIndex: 'cid',
            width: 30,
            autoHeight: true,
            fixed: true,
            renderer: function(value, p, record) {
                // to display the color of the calendar in this column
                var nodecolor = Wtf.getCmp(grid.id.split("_agendaGrid")[0]).calTree.getNodeById(record.data.cid).getUI().GetBackColor();
                return String.format("<div style='height:12px; width:30px; margin: auto; background-color: "+ nodecolor +";'></div>");
            }
        },{
            header: WtfGlobal.getLocaleText('lang.events.text'),
            width: 100,
            dataIndex: 'subject'
        }, {
            header:WtfGlobal.getLocaleText('lang.day.text'),
            width: 45,
            dataIndex: 'eventday'
        }, {
            header: WtfGlobal.getLocaleText('lang.date.text'),
            width: 60,
            dataIndex: 'eventdate'
        }, {
            header:WtfGlobal.getLocaleText('lang.time.text'),
            width: 80,
            dataIndex: 'eventtime'
        },
        {
            header:WtfGlobal.getLocaleText('lang.description.text'),
            width: 80,
            dataIndex: 'descr',   
            renderer: WtfGlobal.validateURL
        },{
            header: WtfGlobal.getLocaleText('lang.location.text'),
            width: 100,
            dataIndex: 'location'
        }, {
            header: WtfGlobal.getLocaleText('pm.project.plan.task.attributes.resources'),
            width: 100,
            dataIndex: 'resources'
        }, {
            header:WtfGlobal.getLocaleText('lang.priority.text'),
            width: 40,
            dataIndex: 'eventpri'
        }]);
        cm.defaultSortable = true;

        var grid = new Wtf.grid.GridPanel({
            ds: agendastore,
            cm: cm,
            loadMask : true,
            id:this.id + '_agendaGrid',
            sm: sm,
            layout:'fit',
            width: '100%',
            height: '100%',
            headerStyle: 'background-color: rgb(231,240,250) ',
            bbar: [WtfGlobal.getLocaleText('pm.project.calendar.agenda.info')],
                /*new Wtf.PagingSearchToolbar({
                id: this.id+'agendapt',
                pageSize: 15,
                store: agendastore,
                displayInfo: true,
                displayMsg: 'Displaying events {0} - {1} of {2}',
                emptyMsg: "No events to display",
//                plugins: new Wtf.common.pPageSize()
                plugins: this.pP = new Wtf.common.pPageSize({id : "pPageSize_Agenda"+this.id})
            }),*/
            viewConfig: {
                forceFit: true
            }
        });
        var nowDate = new Date(this.calendar.getValue());
        agendastore.baseParams={
            calView: 1,
            action: 0,
            cidList:this.chkCalList,
            viewdt1:nowDate.format("Y-m-d 00:00:00"),
            viewdt2:nowDate.add(Date.DAY,14).format("Y-m-d 24:00:00")
        }
        agendastore.load({
            scope: this,
            params: {
                start: 0,
                limit: 50
            }
        });

        agendastore.on("Load",function(){
            for(var i = 0; i < agendastore.getCount(); i++) {
                var agendaStoreAt = agendastore.getAt(i);
                var startts=Wtf.cal.utils.sqlToJsDate(agendaStoreAt.data['startts']);
                var endts=Wtf.cal.utils.sqlToJsDate(agendaStoreAt.data['endts']);
                var allDay = agendaStoreAt.data["allday"];
                var dt1=new Date(startts);
                var dt2=new Date(endts);
                var rec = agendastore.getAt(i);
                if((agendaStoreAt.data['startts'] != agendaStoreAt.data['endts']))
                    var datefield = dt1.format("d M")+' - '+dt2.format("d M");
                else if(allDay)
                    datefield = dt1.format("d M");

                //temp fix done for half an hour timestring display[Gopi]
                if(!allDay)
                    var timefield = dt1.format("g:i A")+' - '+dt2.format("g:i A");
                else
                    timefield = "All Day";

                var priority = agendastore.getAt(i).data['priority'];
                if(priority=='h')
                    priority="High";
                else if(priority=='m')
                    priority="Moderate";
                else if(priority=='l')
                    priority="Low";
                rec.set('eventdate',datefield);
                rec.set('eventtime',timefield);
                rec.set('eventday',dt1.format("l"));
                rec.set('eventpri',priority);
            }
        });
        agendastore.on("loadexception", this.showErrorBox, this);
        var tbar = [];
        tbar.push({
            text: this.archived ? "View Details" : WtfGlobal.getLocaleText('lang.edit.text'),
            id: this.id + '_editAgenda',
            disabled : true,
            iconCls: 'dpwnd editicon caltb',
            scope:this

        });
        if(!this.archived) {
            tbar.push({
                text: WtfGlobal.getLocaleText('lang.delete.text'),
                id: this.id + '_deleteAgenda',
                disabled : true,
                iconCls: 'pwnd deliconwt caltb',
                scope:this
            });
        }
        this.add(new Wtf.Panel({
            id: this.id + 'agendaPanel',
            frame: false,
            layout:'fit',
            width: '100%',
            border: false,
            //renderTo: this.body.id,
            items: [{
                border: false,
                id: this.id + 'agendaPanel1',
                width:'100%',
                layout:'fit',
                items:grid,
                tbar: tbar
            }]
        }));
        this.doLayout();
        if(!this.archived) {
            Wtf.getCmp(this.id+'_deleteAgenda').on('click',this.deleteAgendaEvent,this);
        }

        Wtf.getCmp(this.id+'_editAgenda').on('click',this.editEvent,this);
        Wtf.getCmp(this.id+'_agendaGrid').on('celldblclick',this.editEvent,this);
        sm.on('selectionchange',this.enableDisable,this);
    },

    enableDisable : function(sm) {
        var edbtn = new Wtf.getCmp(this.id + '_editAgenda');
        var delbtn = new Wtf.getCmp(this.id + '_deleteAgenda');
        if(sm.getCount() == 1) {
            if(sm.getSelected().data.deleteFlag=="0"){
                edbtn.setText(this.archived ? "View Details" : WtfGlobal.getLocaleText('lang.edit.text'));
                edbtn.enable();
                if(!this.archived) {
                    delbtn.enable();
                }
            } else {
                edbtn.setText(sm.getSelected().data.deleteFlag == "1" ? "View Details" : "Edit");
                edbtn.enable();
            }
        } else {
            edbtn.disable();
            if(!this.archived)
                delbtn.disable();
            edbtn.setText(WtfGlobal.getLocaleText('lang.edit.text'));
        }
    },
    
    editEvent: function(){
        var Grid  = Wtf.getCmp(this.id+'_agendaGrid');
        var eventSelect = Grid.getSelectionModel();
        var selectedRow = eventSelect.getSelections();
        var ds = Grid.getStore();
        var count = eventSelect.getCount();
        if(count==1){
            var eventid = selectedRow[0].get('eid');
            var rec=ds.find("eid",eventid);
            var edetailsData=ds.getAt(rec).data;
            var sdate=new Date(Wtf.cal.utils.sqlToJsDate(edetailsData.startts));
            var edate=new Date(Wtf.cal.utils.sqlToJsDate(edetailsData.endts));
            var eventData=[];
            eventData[0]=edetailsData.subject;
            eventData[1]=edetailsData.location;
            //eventData[2]=sdate.format("g A");
            //eventData[3]=edate.format("g A");

            //temp fix done for half an hour timestring display[Gopi]
            var sd=sdate.format("i");
            if(sd && sd!="00"){
                eventData[2]=sdate.format("g:i A");
                eventData[3]=edate.format("g:i A");
            }
            else{
                eventData[2]=sdate.format("G");
                eventData[3]=edate.format("G");
            }
            ///////////
            eventData[4]=sdate;
            eventData[5]=edate;
            eventData[6]=edetailsData.descr;
            eventData[7]=edetailsData.showas;
            eventData[8]=edetailsData.priority;
            eventData[9]=edetailsData.recpattern;
            if(edetailsData.recend!=""){
                eventData[10] =(Wtf.cal.utils.sqlToJsDate(edetailsData.recend)).format("Y-m-d 00:00:00");
                var recenddate = new Date(Wtf.cal.utils.sqlToJsDate(edetailsData.recend));
                eventData[12]=recenddate;
            }else{
                eventData[10]=this.defaultTS;
            }
            eventData[11]=edetailsData.resources;
            eventData[13]=edetailsData.cid;
            eventData[14] = edetailsData.allday;
            eventData[15] = edetailsData.deleteFlag;
            this.RemoveMainPanelContent();
            eventfrm = new Wtf.cal.eventForms(this);
            eventfrm.AddingTabPanelEvent();
            eventfrm.AddingEventValues(eventData,eventid);
        }
        else{
            if(count==0){
                calMsgBoxShow(118, 0);
            	//Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Please select a row to edit');
            }
            else{
                calMsgBoxShow(119, 0);
                //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Select only one event at a time');
            }
        }
    },

    deleteAgendaEvent: function() {
        var Grid  = Wtf.getCmp(this.id+'_agendaGrid');
        var eventSelect = Grid.getSelectionModel();
        var selectedRow = eventSelect.getSelections();
        var ds = Grid.getStore();
        var recdata = eventSelect.getSelected().data;
        var count = eventSelect.getCount();

        if(count==0){
            calMsgBoxShow(120, 1);
            //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Please select a row to delete');
        }
        else{
            if(recdata.deleteFlag == '1') {
                calMsgBoxShow(160, 0);
            } else {
                var delEventids ="";
                var calIds="";
                for(var i=0;i<(count);i++){
                    var eid=selectedRow[i].get('eid');
                    var perm=Wtf.cal.utils.getPermLevelByEid(this,eid);
                    if(perm == 3) {
                        calMsgBoxShow(117, 1);
                        return false;
                    }
                    else if(perm != "" && perm > 1) {
                        calMsgBoxShow(121, 1);
                        //Wtf.Msg.alert('Invalid Operation', 'You dont have sufficient privileges to delete the event(s)!');
                        return false;
                    }

                    var t=ds.find("eid",eid);
                    if(t!=-1){
                        var cid=ds.getAt(t).data["cid"];
                        delEventids += eid + ",";
                        calIds += cid + ",";
                    }
                }
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.336'), function(btn){
                    if (btn == 'yes'){
                        var eRecord= [delEventids,calIds];
                        this.deleteEvent(eRecord,"1");
                    }
                },this);
            }
        }
    },

    /*gotodayview: function(){
        this.RemoveMainPanelContent();
        this.AddDay(this, this.selectedday);
    },*/

    reloadAgenda:function(flag){
        var dt1 = new Date(this.seldate);
        var dt2 = dt1;
        if(flag) {
            dt2.setDate(dt1.getDate());
            Wtf.getCmp(this.id + "dateText1").setText(dt1.format("d M Y"));

        } else {
            dt2 = dt1.add(Date.DAY,14);
            Wtf.getCmp(this.id + "dateText1").setText(dt1.format("d M") + " - " + dt2.format("d M"));
            //dt2.setDate(dt1.getDate()+15);
        }

        this.startdate = dt1;
        this.enddate = dt2;
        var Grid = Wtf.getCmp(this.id + "_agendaGrid");
        var ds = Grid.getStore();
        ds.baseParams={
            calView: 1,
            action: 0,
            cidList:this.chkCalList,
            viewdt1:dt1.format("Y-m-d 00:00:00"),
            viewdt2:dt2.format("Y-m-d 24:00:00")
        }
        ds.load({
            params: {
                start: 0,
                limit: 50
            }
        });
    },

    CalculateDate:function(){
        var z = new Array(10);
        var k = new Date();
        var da = this.calendar.getValue();
        var d1 = da.getMonth()+1;
        TadayMonth = da.getMonth();
        TadayYear = da.getFullYear();
        var d2 = da.getFullYear();
        k = Date.parseDate(d2+'-'+d1+'-01','Y-n-d');

        if(d1==2)
            k = Date.parseDate(d2+'-Feb-01','Y-M-d');

        z[0] = k.getFirstDayOfMonth();
        z[1] = k.getDaysInMonth();
        var m = new Date();
        var j = k.getMonth();
        var Str = d2 + '-' + j + '-01';
        m = Date.parseDate(Str, 'Y-n-d');
        z[2] = m.getDaysInMonth()-z[0];
        z[3] = j;

        if(z[3]==0)
            z[3]=12;

        z[4]=35;
        switch(z[0]){
            case 5:
                    if(z[1]==31)
                        z[4]=42;
                    break;
            case 6:
                    if(z[1]==31||z[1]==30)
                        z[4]=42;
                    break;
        }
        return z;
    },

    onPrevClick:function(item) {
        if(this.currentview != ('agendaView')) {
            if(this.currentview==('WorkWeekView') || this.currentview==('WeekView'))
                this.viewNextPrev(-7);
            else if(this.currentview==('DayView'))
                this.viewNextPrev(-1);
            else if(this.currentview==('MonthView'))
                this.viewNextPrev(-1);
            this.onCalViewChange();
        }
        else
            this.viewNextPrev(-15);
    },

    onNextClick:function(item) {
        if(this.currentview != ('agendaView')) {
            if(this.currentview==('WorkWeekView') || this.currentview==('WeekView'))
                this.viewNextPrev(7);
            else if(this.currentview==('DayView'))
                this.viewNextPrev(1);
            else if(this.currentview==('MonthView'))
                this.viewNextPrev(1);
            this.onCalViewChange();
        }
        else
            this.viewNextPrev(15);
    },

    DblClick : function(e){
        showWindow(e);
    },

    highlightDay:function(dy){
        for(var k=0;k<dy;k++){
            var p=Wtf.getCmp(this.id+"_"+k);
            p.el.dom.style.backgroundColor="white";
        }
        var selday=this.calendar.getValue().format("N");
        if((dy==5 && selday >0 && selday <6) || dy==7){
            selday=selday % (dy==5?6:7);
            var p=Wtf.getCmp(this.id+"_"+(dy==5?selday-1:selday));
            p.el.dom.style.backgroundColor="#FFFFCC";
        }
    },

    getCurrentDate:function(){
        var dy=7;
        var obj1 = Wtf.getCmp(this.id+'dateText1');
        if(this.currentview!="agendaView"){
            var calViewChangeFlag = true;   // used for knowing whether or not to render the events again on every date-picker click
            if(this.currentview==('WorkWeekView')||this.currentview==('WeekView')){
                if(this.currentview==('WorkWeekView'))
                    dy=this.actualworkingdays;
                if(this.calendar!=null){
                    var dateval=this.calendar.getValue();
                    if(dateval.format("Y-m-d H:i:s") == this.seldate.format("Y-m-d H:i:s"))
                        // if the initial date and the clicked date are the same then do not render events again
                        calViewChangeFlag = false;
                    else {
                        this.removeFunction();
                        //selddate=this.selectDayDate(dy,0, this.selecteddate, this.selectedday);
                        var selddate = this.selectDayDate(dy);
                        //var obj1=Wtf.getCmp(this.id+'dateText1');
                        obj1.setText(selddate[1]+' '+selddate[0]+', '+selddate[6]+' - '+selddate[5]+' '+selddate[4]+', '+selddate[7]);
                        if(this.currentview=='WeekView')
                            this.changeDayHeaderText(dy,selddate);
                        else
                            this.changeDayHeaderText_workweek(7,selddate);
                    }
                }
                this.highlightDay(dy);
            }
            else if(this.currentview==('DayView')){
                if(this.calendar!=null){
                    var dateval=this.calendar.getValue();
                    if(dateval.format("Y-m-d H:i:s") == this.seldate.format("Y-m-d H:i:s"))
                        // if the initial date and the clicked date are the same then do not render events again
                        calViewChangeFlag = false;
                    else {
                        this.removeFunction();
                        //var obj1=Wtf.getCmp(this.id+'dateText1');
                        obj1.setText(this.selecteddate + ' ' + Date.monthNames[dateval.getMonth()] + " " + dateval.getFullYear());
                        var day1=Wtf.get(this.id+'calDay1');
                        var newDay= this.selectedday.substring(0,3);
                        day1.dom.innerHTML= newDay + "/" + this.selecteddate;
                        day1.dom.style.display="block";
                    }
                }
            }
            else if(this.currentview==('MonthView')){
                var dateval=this.calendar.getValue();
                if(dateval.format("Y-m") == this.seldate.format("Y-m"))
                    // if month not changed then not to render the events again
                    calViewChangeFlag = false;
                else {
                    var m = new Date();
                    var m1 = m;
                    m = dateval.getMonth();
                    m1 = dateval.getFullYear();

                    if(m!=TadayMonth || m1!=TadayYear){
                        TadayMonth=m;
                        TadayYear = m1;
                        this.RemoveDiv();
                        this.AddMonth();
                        var dd = this.calendar.getValue();
                        //var obj1=Wtf.getCmp(this.id+'dateText1');
                        obj1.setText(Date.monthNames[dd.getMonth()] + " " + dd.getFullYear());
                    }
                }
                m = dateval.getDate();
                if(m>=1 && m <=9)
                    m = "0"+m;
                this.DifferBackGround(m);
                this.seldate=this.calendar.getValue();
            }
            if(calViewChangeFlag)
                this.onCalViewChange();
        }
        else{
            var dateval1=this.calendar.getValue();
            var dateval2=dateval1.add(Date.DAY, 14);
            this.removeFunction();
            //var obj1=Wtf.getCmp(this.id+'dateText1');
            obj1.setText(dateval1.format17() + " - " + dateval2.format17());
            this.reloadAgenda(false);
        }
    },

    MakeDropSource : function(ElemID){
        var dd12 = Wtf.get(ElemID);
        dd12.dd = new Wtf.cal.DDProxy(ElemID, 'group');
    },

    // Used to find color in the hex ratio of the earlier color (find a darker or lighter shade)
    ApplyColorTransform: function(nodecolor, cfactor){
        var cc = [];
        if (!nodecolor.match("#"))
            cc = nodecolor.replace(/rgb\(|\)/g, "").split(",");
        else {
            var h = nodecolor.substr(1);
            cc = [Wtf.cal.utils.HexToDec(h.substring(0, 2)), Wtf.cal.utils.HexToDec(h.substring(2, 4)), Wtf.cal.utils.HexToDec(h.substring(4, 6))];
        }
        return String.format("rgb({0}, {1}, {2})", Wtf.cal.utils.MultiplyInt(cc[0], cfactor), Wtf.cal.utils.MultiplyInt(cc[1], cfactor), Wtf.cal.utils.MultiplyInt(cc[2], cfactor));
    },

    addDragCmp : function(objId,dragflag){
        if(dragflag){
            var newObj = new Wtf.cal.EventDZ(objId,{
                ddGroup: 'group',
                scroll: false
            });
        } else {
            var newObj = new Wtf.cal.EventDT(objId,{
                ddGroup: 'group'/*,
                overClass: 'dd-over'*/
            });
        }
    },

    getCalIds : function(){
        var ctr=0;
        var calIds=[];
        var cids=[];
        var selNode=this.calTree.getChecked();
        for(var i=0;i<selNode.length;i++){
            if(!selNode[i].attributes.request){
                calIds[ctr++]=selNode[i].id;
            }
            cids[i]=selNode[i].id;
        }
        this.calList=calIds;
        this.chkCalList=cids;
    },

    /*MakingMoreDaysEventRemove:function (innertext,totaldays,startDate) {
        var date = new Date(TodayMonth + "/" + startDate + "/" + TodayYear);
        var index = totaldays;
        var t = ""//eventHash.keys();
        var a;
        var b;
        for(var i=0;i<(t.length-1);i++) {
            var id = t[i];
            a = "";//eventHash.get(t[i]);
            if(a==totaldays) {
                b= Wtf.get(t[i]).dom;
                if(TodayMonth==date.format('n')) {
                    var k = Wtf.get(this.id+"Day1"+date.format('d')).dom;
                    k.appendChild(b);
                } else
                    b.parentNode.removeChild(a);
                date = date.add(Date.DAY,1);
            }
        }
        a.className += " moreleft";
    },*/

    //Database functions
    insertCalendar:function (rec, type){
         Wtf.Ajax.requestEx({
            method: 'GET',
            url: type == 'ical' ? '../../importICS.jsp?projid='+rec[7]+'&mode=url' : this.url,
            params: ({
                action: 1,
                cname:rec[0],
                description: rec[1],
                location: rec[2],
                timezone: rec[3],
                colorcode: rec[4],
                caltype: rec[5],
                isdefault:rec[6],
                userid:rec[7],
                permission:rec[8],
                exportCal: rec[9],
                loginid: loginid,
                url: rec[10],
                interval: rec[11]
            })},
            this,
            function(result, req){
                try{
                    var nodeobj = eval("(" + result+ ")");
                    if(nodeobj.success=="false"){
                        if(nodeobj.data)
                            calMsgBoxShow([WtfGlobal.getLocaleText('pm.msg.ERROR'), nodeobj.data], 1);
                        else
                            calMsgBoxShow(122, 1);
                    }
                } catch(e) {
                    calMsgBoxShow([WtfGlobal.getLocaleText('pm.msg.ERROR'), result], 1);
                }
                
            },
            function(result, req){
                calMsgBoxShow(4, 1);
            });
    },

   updateCalendar:function(rec, type){
      Wtf.Ajax.requestEx({
        method: 'GET',
        url: this.url,
        params: ({
            action: 2,
            cid:rec[0],
            cname:rec[1],
            description: rec[2],
            location: rec[3],
            timezone: rec[4],
            colorcode: rec[5],
            caltype: rec[6],
            isdefault:rec[7],
            userid:rec[8],
            permission:rec[9],
            exportCal: rec[10],
            loginid: loginid,
            url: rec[11],
            interval: rec[12]
        })},
        this,
        function(result, req){
            var nodeobj = eval("(" + result + ")");
            if(nodeobj.success=="false")
                calMsgBoxShow(123, 1);
        },
        function(result, req){
           calMsgBoxShow(4, 1);
        });
    },

   deleteCalendar:function(rec){
        Wtf.Ajax.requestEx({
            method: 'GET',
            url: this.url,
            params: ({
                action: 3,
                cid:rec.data["cid"],
                isdefault:rec.data["isdefault"],
                caltype:rec.data["caltype"],//this.calTree.ownerid.type,
                userid:this.calTree.ownerid.userid
            })},
            this,
            function(result, req){
                  var nodeobj = eval("(" + result + ")");
                if(nodeobj.success=="false")
                    calMsgBoxShow(124, 1);
                else
                    if(rec.data["isdefault"] == 1){
                        calMsgBoxShow(136, 0);
                        var node = this.calTree.getNodeById(rec.data['cid']);
                        if(node){
                            node.ui.toggleCheck();
                            if(!node.ui.isChecked()){
                                node.ui.toggleCheck();
                            }
                        }
                    } else
                        calMsgBoxShow(137, 0);
            },
            function(result, req){
                calMsgBoxShow(4, 1);
            });
    },

    setRequestAttribute:function(){
        for(var k=0;k<this.calList.length;k++){
            var tnode=this.calTree.getNodeById(this.calList[k]);
            if(tnode)
                tnode.attributes.request=true;
        }
    },

    showErrorBox: function(){
        calMsgBoxShow(153, 1);
        this.loadMask.hide();
    },

    removeICalEvents: function(Store){
        var recs = Store.queryBy(function(rec, id){
            var calId = rec.get('cid');
            var tnode=this.calTree.getNodeById(calId);
            if(tnode && !tnode.ui.isChecked() && tnode.attributes.caltype == 4){
                if(rec.get('deleteFlag') == '1')
                    return true;
                else
                    return false;
            } else {
                return false;
            }
        }, this);

        var cnt = recs.length;
        for(var i=0; i<cnt; i++){
            var eid = recs.items[i].get('eid');
            Store.remove(recs.items[i]);
            this.removeRenderedEvent(eid, this.currentview == "MonthView"? true: false);
        }
    },

    getEvents : function(cal,st,ed){
        var droi1format = (this.droi1.add(Date.DAY, -this.droi1.getDay())).format("Y-m-d 00:00:00");
        var droi2format = (this.droi2.add(Date.DAY, (6 - this.droi2.getDay()))).format("Y-m-d 24:00:00");
        if(!this.eStore){
            this.eStore=new Wtf.calStore();
            this.eStore.on("loadexception", this.showErrorBox, this);
            this.eStore.on("load", this.renderLoadedEvent, this);
            this.eStore.load({params:{calView:0,action:0,cid:cal,startts:droi1format,endts:droi2format}});
        }
        else if(!this.eStore.getCount()){
            this.removeICalEvents(this.eStore);
            this.eStore.load({params:{calView:0,action:0,cid:cal,startts:droi1format,endts:droi2format}});
        } else if(!this.tmpStore){
            this.removeICalEvents(this.eStore);
            this.createTempStore();
            this.tmpStore.load({params:{calView:0,action:0,cid:cal,startts:droi1format,endts:droi2format}});
        } else {
            this.removeICalEvents(this.eStore);
            this.tmpStore.load({params:{calView:0,action:0,cid:cal,startts:droi1format,endts:droi2format}});
        }
        this.loadMask.show();
    },

    createTempStore : function(){
        this.tmpStore=new Wtf.calStore();
        this.tmpStore.on("load", this.addToMainStore, this);
        this.tmpStore.on("loadexception",this.showErrorBox,this);
    },

    renderLoadedEvent : function(storeObj, rec, opt){
        if (opt) {
            this.findAndRender();
            this.setRequestAttribute();
        }
        this.loadMask.hide();
    },

    addToMainStore : function(obj,rec,opt){
        this.eStore.checkAndAdd(obj,"eid");
        obj.removeAll();
        this.findAndRender();
        this.setRequestAttribute();
        this.loadMask.hide();
    },

    findAndRender : function(){
        var val = [this.chkCalList, this.startdate.format("Y-m-d 00:00:00"), this.enddate.add(Date.DAY, 1).format("Y-m-d 00:00:00")];
        var records=this.eStore.findRec(["cid", "startts", "endts"],val,["0", "3", "4"]);
        if(records)
            this.renderEventsOnPanel(records);//.items);
    },

    renderEventsOnPanel : function(rec) {
        var apptRec = [];
        var recData = null;
        var zzz = null;
        var newEventNode = null;
        var tStr = null;
        var calculateWhom = [];
        var cntCalWhom = 0;
        var IsMonth = this.currentview == "MonthView" ? true : false;

        for(var cntRec= 0; cntRec < rec.length; cntRec++) {
            recData = rec[cntRec].data;
            var recStart = Wtf.cal.utils.sqlToJsDate(recData.startts);
            var recEnd = Wtf.cal.utils.sqlToJsDate(recData.endts);

            var dayDiff = Wtf.cal.utils.DateDiff(recEnd.format("d/m/Y"), recStart.format("d/m/Y"));
            var addCnt = (recData.eid.indexOf("CNT_") != -1 ? parseInt(recData.eid.split("CNT_")[1]) : 0);

            if(recData.allday || ((recStart.format("Y-m-d") != recEnd.format("Y-m-d")) && !(recEnd.getHours() == 0 && dayDiff <= 1))) {
                //Multiple days or All day events

                var checkDateStart = recStart.add(Date.DAY, addCnt);
                var finalStartAdd = 0;

                if(!IsMonth) {
                    // Finding the day dom "zzz" for day, work-week, week views
                    var dtt = 0;
                    if(this.currentview == "WorkWeekView")
                        dtt = -1;
                    if(this.currentview != "DayView")
                        finalStartAdd = parseFloat(checkDateStart.getDay());
                    if(checkDateStart >= this.startdate && checkDateStart <= this.enddate && (dtt + finalStartAdd) < 7 && (dtt + finalStartAdd) >= 0)
                        zzz = Wtf.get(this.id+'_allDay_'+(dtt + finalStartAdd)).dom;
                    else
                        continue;
                }
                else {
                    // Finding the day dom "zzz" for month view
                    finalStartAdd = checkDateStart.format('d').toString();
                    if(checkDateStart >= this.startdate && checkDateStart <= this.enddate)
                        zzz = Wtf.get(this.id+'Day1'+ finalStartAdd).dom;
                    else
                        continue;
                }
                newEventNode = this.AddItem(recData.subject, zzz, recData.cid, addCnt, 0, recData.eid, -1, recData.flagEvent, recData.allday, recData.deleteFlag, recData);

                if(newEventNode !== undefined) {
                    if(recData.allday) {
                        if(recStart.format("Y-m-d H:i:s") == recEnd.format("Y-m-d H:i:s"))
                            tStr = recStart.format("M j, Y");
                        else
                            tStr = "From " + recStart.format("M j, Y") +" to "+ recEnd.format("M j, Y");
                    }
                    else
                        tStr = "From " + recStart.format("M j, Y g:i A") +" to "+ recEnd.format("M j, Y g:i A");

                    newEventNode.setAttribute("wtf:qtip", tStr);
                    newEventNode.setAttribute("wtf:qtitle", recData.subject);

                    Wtf.get(this.id+"e_"+ recData.eid).hover(this.onMultiEventHoverIn, this.onMultiEventHoverOut);
                    if(calculateWhom.indexOf(finalStartAdd) == -1 && IsMonth)
                        // getting the days for which the calculateHowMore() function is to be called for
                        // (for limiting the no. of events to be shown in a day)
                        calculateWhom[cntCalWhom++] = finalStartAdd;
                }
            }
            else if(!IsMonth) {
                // if not multi day or all day event and not month view then call CreateChipBody()
                var chipBody = Wtf.getCmp(this.id + "e_" + recData.eid);
                if(chipBody && chipBody != undefined)
                    chipBody.destroy();
                this.createChipBody(rec[cntRec]);
            }
            else
                // if month view and not multi day or all day event then add to appoinment record
                    // for rendering to be only after multi day or all day events are rendered
                apptRec.push(rec[cntRec]);
        }
        if(IsMonth) {
            for(cntRec = 0; cntRec<apptRec.length; cntRec++) {
                // rendering the event for month view in the appoinment records
                recData = apptRec[cntRec].data;
                var start = Wtf.cal.utils.sqlToJsDate(recData.startts);
                var startgiA = start.format("g:i A");
                var startD = start.format('d').toString();
                zzz = Wtf.get(this.id + 'Day1' + startD).dom;
                newEventNode = this.AddItem(recData.subject, zzz, recData.cid, -1, -1, recData.eid, startgiA, 0, false, recData.deleteFlag, recData);
                if(newEventNode !== undefined) {
                    tStr = recStart.format("M j, Y") + " from " + startgiA +" to "+ Wtf.cal.utils.sqlToJsDate(recData.endts).format("g:i A");
                    newEventNode.setAttribute("wtf:qtip", tStr);
                    newEventNode.setAttribute("wtf:qtitle", recData.subject);
                }
                if(calculateWhom.indexOf(startD) == -1)
                    // getting the days for which the calculateHowMore() function is to be called for
                    calculateWhom[cntCalWhom++] = startD;
            }
            // if the no. of days for whom the calculateHowMore() is to be called then call it for all days, by not passing any argument
            if(cntCalWhom >= this.seldate.getDaysInMonth())
                this.CalculateHowMore();
            else
                for(var cnt = 0; cnt<cntCalWhom; cnt++)
                    this.CalculateHowMore(null, null, Wtf.get(this.id + "Day1" + calculateWhom[cnt]).dom);
        }
        else
            this.CalculateWeekMore();
    },

    onMultiEventHoverIn: function(e) {
        // Applying color transform to the event's DOM elements for highlighting the event on hover in
        var eTar = e.getTarget();
        if(eTar.id.indexOf("delLink_") == -1) {
            var eventContd = eTar.id;
            var EventId = eventContd.split("e_");
            var MainPanel = Wtf.getCmp(EventId[0]);
            if((eTar.id == "" || eTar.tagName == "SPAN") || MainPanel == undefined) {
                // if the target is span containing the event's subject' then get it's parent
                eventContd = eTar.parentNode.id;
                EventId = eventContd.split("e_");
                MainPanel = Wtf.getCmp(EventId[0]);
            }
            if(MainPanel.hoverEvent != null && MainPanel.hoverEvent != "") {
                var eventDom = Wtf.query("div[peid="+ MainPanel.hoverEvent +"]");
                // apply the original color to all the DOM elements of the previous hover event
                if(eventDom.length > 0) {
                    var nodecolor = MainPanel.calTree.getNodeById(eventDom[0].getAttribute('name')).getUI().GetBackColor();
                    if(eventDom[0].style.backgourndColor != nodecolor) {
                        for(var i=0; i<eventDom.length; i++) {
                            eventDom[i].style.backgroundColor = nodecolor;
                            eventDom[i].childNodes[0].style.color = "white";
                        }
                    }
                }
            }

            nodecolor = MainPanel.calTree.getNodeById(Wtf.get(eventContd).dom.getAttribute('name')).getUI().GetBackColor();
            var nodeColorBG = MainPanel.ApplyColorTransform(nodecolor, 1.75);
            var nodecolorFont = MainPanel.ApplyColorTransform(nodecolor, 0.68);
            eventDom = Wtf.query("div[peid="+ EventId[1].split("CNT_")[0] +"]");
            // apply the transformed color to all the DOM elements of the event
            for(i=0; i<eventDom.length; i++) {
                eventDom[i].style.backgroundColor = nodeColorBG;
                eventDom[i].childNodes[0].style.color = nodecolorFont;
            }
            MainPanel.hoverEvent = EventId[1].split("CNT_")[0];
        }
    },

    onMultiEventHoverOut: function(e) {
        // For restoring the event back to it's original color on hover out
        var eTar = e.getTarget();
        if(eTar.id.indexOf("delLink_") == -1) {
            var eventContd = eTar.id;
            var MainPanel = Wtf.getCmp(eTar.id.split("e_")[0]);
            var EventId = eventContd.split("e_");
            if(eTar.id == "" || eTar.tagName != "DIV" || MainPanel == undefined) {
                eventContd = eTar.parentNode.id;
                EventId = eventContd.split("e_");
                MainPanel = Wtf.getCmp(EventId[0]);
            }
            var nodecolor = MainPanel.calTree.getNodeById(Wtf.get(eventContd).dom.getAttribute('name')).getUI().GetBackColor();
            var eventDom = Wtf.query("div[peid="+ EventId[1].split("CNT_")[0] +"]");
            // apply the original color to all the DOM elements of the event
            for(var i=0; i<eventDom.length; i++) {
                eventDom[i].style.backgroundColor = nodecolor;
                eventDom[i].childNodes[0].style.color = "white";
            }
            MainPanel.hoverEvent = "";
        }
    },

    createChipBody : function(rec){
        var recData = rec.data;
        var flag = 0;
        var eventid=recData.eid;
        var startts=Wtf.cal.utils.sqlToJsDate(recData.startts);
        var endts=Wtf.cal.utils.sqlToJsDate(recData.endts);
        var obj = recData.subject;
        var obj4 = recData.cid;
//        var a=startts.format("G");
//        var a1=endts.format("G");
        var a=startts.getHours()*2;
        var a1=endts.getHours()*2;
        if(startts.getMinutes()==30){
            a+=1;
        }if(endts.getMinutes()==30){
            a1+=1;
        }
        if(a1=="0")
            a1="48";
        var eh = (a1-a) * Wtf.fixHeight;
        var dtt=0;
        if(this.currentview=="WorkWeekView"){
            dtt=startts.getDay();
            if(this.workDayArr.indexOf(dtt) != -1){
                dtt = this.workDayArr.indexOf(dtt);
            } else {
                flag = 1;
            }
        }
        else if(this.currentview!="DayView")
            dtt=startts.getDay();
        if(flag == 0){
            var tStr=startts.format("g:i A")+" - "+endts.format("g:i A");
            var qTipStr = startts.format("M j, Y") + " from " + startts.format("g:i A") +" to "+ endts.format("g:i A");
    //        if(a1=="24")
    //            a1="0";//try a1=23;
            var cellId= this.id + 'cell_' + a + '_' + dtt;
            var cell = Wtf.get(cellId);
            var w1 = cell.dom.offsetWidth;
            var panelWidth = Wtf.get(this.id + this.currentview + 'eventPanel').dom.offsetWidth;
            var ew = (w1 / panelWidth) * 98-1;
        //    eventWin = new Wtf.cal.eventWindow(this);
            this.eventWin.createEventCell(eventid, cell, tStr, obj, qTipStr, eh, ew, obj4, recData.timestamp, this.archived, recData.deleteFlag);
            this.updatecellvalue(obj4,a,a1,startts,dtt,ew);
        }
    },

    triggerEvent : function(){
        if(this.calList.length)
            this.getEvents(this.calList,this.startdate.format("Y-m-d H:00:00"), this.enddate.add(Date.DAY, 1).format("Y-m-d 00:00:00"));
        else if(this.eStore)
            this.findAndRender();
    },

    insertEvent : function(rec){
        Wtf.Ajax.requestEx({
            method: 'GET',
            url: Wtf.calReq.cal + 'calEvent.jsp',
            params: ({
                calView: 0,
                action: 1,
                eid: rec[0],
                cid: rec[1],
                startts: rec[2],
                endts: rec[3],
                subject: rec[4],
                descr:rec[5],
                location: rec[6],
                showas:rec[7],
                priority:rec[8],
                recpattern:rec[9],
                recend:rec[10],
                resources:rec[11],
    //            userid:rec[12],
                reminders:rec[12],
                allDay:rec[13]
            })},
            this,
            function(result, req){
                var nodeobj = eval("(" + result + ")");
                if(nodeobj.success=="false")
                    calMsgBoxShow(125, 1);
            },
            function(result, req){
                calMsgBoxShow(4, 1);
        });
    },

    updateEvent : function(rec,view){
        Wtf.Ajax.requestEx({
            method: 'GET',
            url: Wtf.calReq.cal + 'calEvent.jsp',
            params: ({
                calView: view,
                action: 2,
                eid: rec[0].split("CNT_")[0],
                cid: rec[1],
                startts: rec[2],
                endts: rec[3],
                subject: rec[4],
                descr:rec[5],
                location: rec[6],
                showas:rec[7],
                priority:rec[8],
                recpattern:rec[9],
                recend:rec[10],
                resources:rec[11],
    //            userid:rec[12],
                reminders:rec[12],
                allDay:rec[13],
                fullupdate:rec[14]
            })},
            this,
            function(result, req){
                var nodeobj = eval("(" + result+ ")");
                if(nodeobj.success=="false")
                    calMsgBoxShow(125, 1);
            },
            function(result, req){
                calMsgBoxShow(4, 1);
        });
    },

    deleteEvent : function(rec,view){
        Wtf.Ajax.requestEx({
            method: 'GET',
            url: Wtf.calReq.cal + 'calEvent.jsp',
            params: ({
                calView: view,
                action: 3,
                eid: rec[0],
                cid:rec[1]
            })},
            this,
            function(result, req){
                var nodeobj = eval("(" + result + ")");
                if(nodeobj.success=="false")
                    calMsgBoxShow(126, 1);
                else if(this.currentview == "AgendaView")
                    this.reloadAgenda();
            },
            function(result, req){
                calMsgBoxShow(4, 1);
        });
    },

    updatecellvalue : function(calid,a,a1,startts,ddt,ew){
        //TODO:needs to be optimized [shri]
        var prop = ["cid", "startts", "startts"];
        var starttsYMD = startts.format("Y-m-d");
        var val = [this.chkCalList, starttsYMD, startts.add(Date.DAY, 1).format("Y-m-d 00:00:00")];
        var op = ["0", "3", "4"];
        var rec = this.eStore.findRec(prop, val, op, false, false);
        var clash=[];
        var flagfound =false;
        var edown=0;
        var evtPanel;
        for(var i=0;i<rec.length;i++){
            var recStart = Wtf.cal.utils.sqlToJsDate(rec[i].data.startts);
            var recStartYMD = recStart.format("Y-m-d");
            var recEnd = Wtf.cal.utils.sqlToJsDate(rec[i].data.endts);
            var dayDiff = Wtf.cal.utils.DateDiff(recEnd.format("d/m/Y"), recStart.format("d/m/Y"));
            if(rec[i].data.allday || ((recStartYMD != recEnd.format("Y-m-d")) && !(recEnd.getHours() == 0 && dayDiff <= 1)))
                //Check for multi-day events, if multi day then do not add them in clash[]
                continue;
            else if(recStartYMD == starttsYMD) {
                var eid=rec[i].data["eid"];
                evtPanel=Wtf.getCmp(this.id+"e_"+eid);
                if(evtPanel){
                    flagfound = true;
                    var currCell=evtPanel.cellId;
                    var starthr=parseInt(currCell.split('_')[1]);
                    var endhr = parseInt(evtPanel.el.dom.offsetHeight/30) + starthr;
                    if((a >= starthr && a <=endhr) || (a1 > starthr && a1<=endhr) || (a <= starthr && a1 >=endhr))
                        // accumulating the events that are clashing
                        clash[edown] = [eid,"d",++edown];
                }
            }
        }
        if(flagfound){
            for(i=0;i<edown;i++){
                var clashEvtPanel=Wtf.getCmp(this.id+"e_"+clash[i][0]);
                var evtWidth = Wtf.get(this.id+"_"+ddt).dom.offsetWidth;
                val = 0;
                if(edown > 1)
                    // (event width) / (number of events clashing)
                    val = evtWidth/edown;
                var parentCell = Wtf.get(clashEvtPanel.cellId);
                var cell = parentCell.getXY();
                var wd = ew;
                if(edown)
                    wd = wd/edown;
                var x = cell[0]+ 2 + (val *i);
                clashEvtPanel.el.dom.style.width=wd+"%";
                var y =cell[1];
                clashEvtPanel.el.dom.style.left = x-(Wtf.get(this.dtcont.id).dom.offsetWidth + 46)+"px";
            }
        }
        /*var max=0;
        for(var i=a;i<=a1;i++){
       var cell = Wtf.get(this.id + 'cell_' + i + '_' + ddt).dom;
       var evtCount =  parseInt(cell.getAttribute("eventCount"));
       evtCount++;
       if(evtCount > max){
           max = evtCount;
       }
       cell.setAttribute("eventCount",evtCount);
       }
        return max;*/


    },

CalculatingTotalCalendar : function(){
    if(!this.tempCalStore){
        var calData = [], i = 0;
        var recresult = this.calTree.agendaStore.queryBy(function(record){
            var perm= record.get("permissionlevel");
            if(perm == "2" || perm == "3")
                return false;
            if(record.get("caltype") != 4){
                calData[i++] = [record.get("cid"),record.get("cname")];
            }
            return true;
        },this);
        this.tempCalStore = new Wtf.data.SimpleStore({
            fields: ['id', 'state1'],
            data: calData
        });
    }
    else{
        if(this.tempCalStore.getCount())
            this.tempCalStore.removeAll();
        var i=0;
        this.calTree.agendaStore.each(function(record){
            var rec=new Wtf.cal.utils.tempCalRec({
                id:record.get("cid"),
                state1:record.get("cname")
            });
            if(record.data.caltype != 3 && record.data.caltype != 4)
                this.tempCalStore.insert(i++,rec);
        },this);
//        this.tempCalStore.commitChanges();
    }
},

showCalPanel:function(p){
    p.show();
    this.layout.activeItem=p;
    this.doLayout();
},

saveEvent:function(){
    var subject = Wtf.getCmp(this.id + 'Subject').getValue();
//    subject = Wtf.cal.utils.HTMLScriptStripper(subject);
    subject = WtfGlobal.HTMLStripper(subject);
    if (subject == "") {
//        Wtf.cal.utils.ShowErrorMsgBox("Please enter the required field(s)!");
        calMsgBoxShow(['Invalid Input', "Please enter the required fields!"], 1);
        return;
    }
    var allday = Wtf.getCmp(this.id + 'CheckAllDay1').getValue();
    var stdt= (Wtf.getCmp(this.id + 'sdate1').getValue()).format("Y-m-d");
    var endt = (Wtf.getCmp(this.id + 'edate1').getValue()).format("Y-m-d");
    //var endt= new Date(Wtf.get(this.id + 'edate1').getValue()).format("Y-m-d");
    if (endt < stdt) {
        calMsgBoxShow(['Invalid Input', "Please enter a valid start date"], 1);
        return;
    }

    if(allday)
        endt = (Wtf.getCmp(this.id + 'edate1').getValue()).add(Date.DAY, 1).format("Y-m-d");

    var descr = Wtf.getCmp(this.id + 'Description').getValue();
//    descr = Wtf.cal.utils.HTMLScriptStripper(descr);
    descr = WtfGlobal.HTMLStripper(descr);
    if(!descr||descr=="undefined")
        descr="";
    var resource = Wtf.getCmp(this.id + 'Resources').getValue();
//    resource = Wtf.cal.utils.HTMLScriptStripper(resource);
    resource = WtfGlobal.HTMLStripper(resource);
    if(!resource||resource=="undefined")
        resource="";
    var showas = Wtf.getCmp(this.id + 'showas').getValue();
    var priority = Wtf.getCmp(this.id + 'comboPriority').getValue();
    //var sttm= Wtf.cal.utils.getTimeIndNo(Wtf.get(this.id + 'stime1').getValue(), false);
    //var endtm= Wtf.cal.utils.getTimeIndNo(Wtf.get(this.id + 'etime1').getValue(), true);
    var sttm = Wtf.getCmp(this.id + 'stime1').getValue();
    var endtm = Wtf.getCmp(this.id + 'etime1').getValue();
    var sttm1,endtm1;
    if(!allday) {
        if((sttm+'').indexOf("AM")>0 || (sttm+'').indexOf("PM")>0)
        {
            var ld = Date.parseDate(stdt.trim() + " " + sttm.trim() , 'Y-m-d g:i A');
            sttm1 = ld.format("Y-m-d H:i:s.00");
        } else {
            ld = Date.parseDate(stdt + " " + sttm , 'Y-m-d G');
            sttm1 = ld.format("Y-m-d H:i:s.00");
            //sttm1 = parseInt(sttm);
        }
        if((endtm+'').indexOf("AM")>0 || (endtm+'').indexOf("PM")>0)
        {
            ld = new Date();
            ld = Date.parseDate(endt.trim() + " " + endtm.trim() , 'Y-m-d g:i A');
            endtm1 = ld.format("Y-m-d H:i:s.00");
        } else {
            ld = Date.parseDate(endt + " " + endtm , 'Y-m-d G');
            endtm1 = ld.format("Y-m-d H:i:s.00");
            //endtm1 = parseInt(endtm);
        }
    }
    else {
        sttm1 = stdt.trim() + " 00:00:00.00";
        endtm1 = endt.trim() + " 00:00:00.00";
    }
    //g:i A
    //stdt + " " + sttm + ":00:00.00"
    //var startdate = new Date()
    if(((sttm1 >= endtm1 && stdt >= endt) || (Wtf.getCmp(this.id + 'etime1').getValue() == 0 && endt == stdt)) && !allday) {
        calMsgBoxShow(['Invalid Input', "Please enter valid start time and end time"], 1);
        return;
    }
    var loc=Wtf.get(this.id + 'Location').getValue();
//    loc = Wtf.cal.utils.HTMLScriptStripper(loc);
    loc = WtfGlobal.HTMLStripper(loc);
        if(!loc||loc=="undefined")
            loc="";
    var checkfield = Wtf.getCmp(this.id + 'RecurringPattern');
    var reminderString="";
    for(var i=0;i<5;i++){
        if(Wtf.getCmp(this.id+'MainRemindPanel'+i)!=undefined){
            reminderString+=Wtf.getCmp(this.id+'PopupCombo'+ i).getValue();
            reminderString+="_"+Wtf.getCmp(this.id+'ReminderEndTime'+i).getValue()+",";
            Wtf.getCmp(this.id+"MainRemindPanel"+i).destroy();
        }
    }
//    var Grid  = Wtf.getCmp(this.id+'guestGrid');
//    var storeGuest = Grid.getStore();
//    var guestUids="";
//    var statusfield;
//    for(var i=0;i<storeGuest.getCount();i++){
//        statusfield = this.setResponse(storeGuest.getAt(i).get("response"));
//        guestUids+=storeGuest.getAt(i).get("userid");
//        guestUids+="/"+storeGuest.getAt(i).get("username");
//        guestUids+="_"+ statusfield+",";
//    }
    var recfield;
   // if(checkfield.checked==true){
   //     var recfield = this.rec[0].substring(0,1);
    //    if(recfield=='W')
    //        recfield=recfield+this.rec[1];
    //    if(this.rec[3]=="" || this.rec[3]==undefined){
    //        this.rec[3]=this.defaultTS;
    //    }else{
    //        this.rec[3]=this.rec[3].format("Y-m-d 00:00:00");
    //    }
    //}
   // else{
        recfield="";
        this.rec[3]=this.defaultTS;
    //}
    if (this.EventClick == "") {
        if (this.currentview != 'agendaView') {
           // if (this.currentview != 'MonthView') {
                //var index= this.eventWin.CalendarStore.find('state1', Wtf.get(this.id + 'calendarweek').getValue());
                //var cid = this.eventWin.CalendarStore.collect('id');
                //cid = cid[index];
            cid = Wtf.getCmp(this.id + 'calendarweek').getValue();
            var eRecord = ["0", cid, sttm1, endtm1, subject, descr, loc, showas, priority, recfield, this.rec[3], resource, /*guestUids,*/ reminderString, allday];
            this.insertEvent(eRecord);
            var node = this.calTree.getNodeById(cid);
            if(node && !node.ui.isChecked()){
                node.ui.toggleCheck();
            }
          //   }
        }
    }
    else {
        if(this.currentview == 'agendaView'){
            var eid=this.EventClick;
            /*var startDateTime = stdt+' '+sttm+':00:00';
            var endDateTime = stdt+' '+endtm+':00:00';

            if(sttm.length==1)
                sttm="0"+sttm;

            if(endtm.length==1)
                endtm="0"+endtm;*/

            var Grid  = Wtf.getCmp(this.id+'_agendaGrid');
            if(Grid && Grid!=undefined){
                var calId =null;
                var ds = Grid.getStore();
                var k=ds.find("eid",eid);
                if(k!=-1){
                    calId=ds.getAt(k).data["cid"];
                    eRecord = [this.EventClick, calId, sttm1, endtm1, subject, descr, loc, showas, priority, recfield, this.rec[3], resource, /*guestUids,*/ reminderString, allday];
                    this.updateEvent(eRecord,"1");
                }
            }
        }
        else{
            //hash.put(stdt, subject);
            eid = this.EventClick.split("e_")[1].split('CNT_')[0];
            k = this.eStore.find("peid", eid);
            var cid = null;
            if(k!=-1)
                cid = this.eStore.getAt(k).data["cid"];
            eRecord = [eid, cid, sttm1, endtm1, subject, descr, loc, showas, priority, recfield, this.rec[3], resource, /*guestUids,*/ reminderString, allday];
            this.updateEvent(eRecord,"0");
        }
    }
    this.addCalendarTab1();
},

onSaveClick:function(){
    this.saveEvent();
    for(var i=0;i<4;i++){
        this.rec[i]="";
    }
    this.Calendar_countReminder = 0;
    //this.EventClick = "";
},

 onDeleteClick:function(){
     if(!this.archived){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.336'), function(btn){
            if (btn == 'yes'){
                if(this.EventClick==""){
                    calMsgBoxShow(127, 1);
                    return;
                } else{
                    if(this.currentview == 'agendaView'){
                        var Grid  = Wtf.getCmp(this.id+'_agendaGrid');
                        if(Grid && Grid!=undefined){
                            var calId =null;
                            var ds = Grid.getStore();
                            var k=ds.find("eid",this.EventClick);
                            if(k!=-1){
                                calId=ds.getAt(k).data["cid"];
                                var eRecord = [this.EventClick, calId];
                                this.deleteEvent(eRecord,"1");
                            }
                        }
                    } else{
                        var eid = this.EventClick.split("e_")[1].split("CNT_")[0];
                        k = this.eStore.find("peid", eid);
                        var cid =null;
                        if(k!=-1)
                            cid=this.eStore.getAt(k).data["cid"];
                        eRecord= [eid, cid];
                        this.deleteEvent(eRecord,"0");
                    }
                }
                this.addCalendarTab1();
            }
        },this);
    }
},

show1:function(btn){
    if (btn == 'yes')
       this.saveEvent();
    if (btn == 'no')
        this.addCalendarTab1();
    for(var i=0;i<4;i++)
        this.rec[i]="";
    this.Calendar_countReminder = 0;
    //this.EventClick = "";
},

onCloseClick:function (){
    if((this.calPerm=="" || this.calPerm==1) && !this.archived) {
        Wtf.Msg.show({
            title: WtfGlobal.getLocaleText('pm.project.calendar.event.savechanges'),
            msg: WtfGlobal.getLocaleText('pm.project.calendar.event.prompt.text'),
            buttons: Wtf.Msg.YESNOCANCEL,
            fn: this.show1,
            scope: this,
            animEl: 'elId',
            icon: Wtf.MessageBox.QUESTION
        });
    }
    else
        this.addCalendarTab1();
},

setResponse:function(response){
    if(response=='Accepted')
        return('a');
    else if(response=='Rejected')
        return('r');
    else
        return('p');
},

    onExportICSClick : function(){
        Wtf.getCmp(this.id+'exportICSAction1').toggle();
        var exportURL = Wtf.pagebaseURL + "exportICS.ics?uid=" + loginid;
        var SelectLink = "<a href='javascript:document.exportLinkForm.exportLinkField.select()'>"+WtfGlobal.getLocaleText('pm.project.calendar.export.selecturl')+"</a>";
        var iCalLink= "<form name='exportLinkForm'><textarea readonly='' name='exportLinkField' style='width:390px; background:white' onclick='javascript:document.exportLinkForm.exportLinkField.select()'>"+exportURL+"</textarea>";
        var msgExport = "<br/><div style='margin-left:4px;'><b>"+WtfGlobal.getLocaleText('pm.project.calendar.export.info')+"<br/><br/>"+SelectLink+"</b></div>"+iCalLink+"<br/><br/>";

        var exportRecord = new Wtf.data.Record.create([
            {
                name: "projectname"
            },{
                name: "cname"
            },{
                name: "cid"
            }
        ]);
        var exportRecordReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: 'count'
        }, exportRecord);

        this.groupingView = new Wtf.grid.GroupingView({
            forceFit: true,
            showGroupName: false,
            enableGroupingMenu: false,
            hideGroupedColumn: false
        });

        this.exportDS = new Wtf.data.GroupingStore({
            id: "exportDS",
            url: this.url,
            reader: exportRecordReader,
            method: 'GET',
            baseParams: {
                action: 5,
                caltype: 0,
                loginid: loginid
            },
            sortInfo: {
                field: 'projectname',
                direction: "DESC"
            }
        });

        this.exportGrid = new Wtf.grid.GridPanel({
            store: this.exportDS,
            columns: [
                {id:'exportProjectCal', header: WtfGlobal.getLocaleText('pm.createeditproject.projectname'), hidden: true, dataIndex: 'projectname'},
                {header: WtfGlobal.getLocaleText('pm.project.calendar.newcal.calname'), width: 200, sortable: true, dataIndex: 'cname',groupable:true}
            ],
            sm: new Wtf.grid.RowSelectionModel({singleSelect:true}),
            width:400,
            height:200,
            frame:true,
            layout: 'fit',
            view: this.groupingView,
            iconCls:'icon-grid'
        });
        this.ExportGridPanel = new Wtf.common.KWLListPanel({
            title: WtfGlobal.getLocaleText('pm.project.calendar.export.title'),
            autoLoad: false,
            autoScroll:true,
            paging: false,
            layout: 'fit',
            items: [this.exportGrid]
        });
        this.exportWindow = new Wtf.Window({
            width:410,
            height: 440,
            resizable : false,
            id : 'exportWindow',
            modal:true,
            title:WtfGlobal.getLocaleText('pm.project.calender.export.text'),
            buttons: [{
                anchor : '90%',
                text: WtfGlobal.getLocaleText('lang.OK.text'),
                handler:function() {
                    Wtf.getCmp('exportWindow').close();
                },
                scope:this
            }],
            items:[this.ExportGridPanel,new Wtf.Panel({
                    html: msgExport,
                    autoHeight : true
            })]
        }).show();
        this.exportDS.load();
        this.exportDS.on("load", function(){
            this.exportDS.groupBy("projectname");
        }, this);
    },

    onImportICSClick: function(){
        Wtf.getCmp(this.id + 'importICSAction1').toggle();
        var projid = this.calTree.ownerid.userid;

        this.BrowseCalStore = new Wtf.data.JsonStore({
            url: Wtf.calReq.cal + "caltree.jsp?userid=" + projid + "&action=6&caltype=3",
            root: "data",
            fields: ['cid', 'cname']
        });

        this.BrowseCalRec = Wtf.data.Record.create([
            {name: 'cid'},
            {name: 'cname'}
        ]);

        var choiceAddCaltext = new Wtf.form.TextField({
            allowBlank: false,
            maxLength : 100,
            width: 210,
            labelStyle: 'width:110px;',
            fieldLabel: WtfGlobal.getLocaleText('pm.project.calendar.newcal.calname')
        });

        choiceAddCaltext.on("change", function() {
            choiceAddCaltext.setValue(WtfGlobal.HTMLStripper(choiceAddCaltext.getValue()));
        });

        var choiceMergeCaltext = new Wtf.form.ComboBox({
            emptyText: WtfGlobal.getLocaleText('pm.project.calendar.import.choose.select'),
            fieldLabel: WtfGlobal.getLocaleText('pm.project.calendar.import.choose'),
            selectOnFocus: true,
            store : this.tempCalStore,
            displayField: 'state1',
            triggerAction: 'all',
            editable:true,
            forceSelection:true,
            typeAhead:true,
            mode: 'local',
            readOnly: true,
            disabled: true,
            maxHeight: 200,
            listWidth: 210,
            labelStyle: 'width:110px;',
            width: 210,
            valueField: 'id',
            allowBlank: false,
            name: "combovalue"
        });

        this.BrowseCalStore.load();

        var choiceBrowseCaltext = new Wtf.form.ComboBox({
            emptyText: WtfGlobal.getLocaleText('pm.project.calendar.import.holiday.browse'),
            fieldLabel: WtfGlobal.getLocaleText('pm.project.calendar.import.holiday'),
            selectOnFocus: true,
            store : this.BrowseCalStore,
            displayField: 'cname',
            triggerAction: 'all',
            editable:true,
            forceSelection:true,
            typeAhead:true,
            mode: 'local',
            readOnly: true,
            disabled: true,
            maxHeight: 200,
            listWidth: 210,
            labelStyle: 'width:110px;',
            width: 210,
            valueField: 'cid',
            allowBlank: false,
            name: "combovalue"
        });

        var enterNewCalFieldset = new Wtf.form.FieldSet ({
            title:WtfGlobal.getLocaleText('pm.project.calendar.import.calname'),
            autoHeight:true,
            items:[choiceAddCaltext]
        });

        var enterMergeCalFieldset = new Wtf.form.FieldSet ({
            title:WtfGlobal.getLocaleText('pm.project.calendar.import.choose'),
            hidden: true,
            autoHeight:true,
            items:[choiceMergeCaltext]
        });

        var enterBrowseCalFieldset = new Wtf.form.FieldSet ({
            title: WtfGlobal.getLocaleText('pm.project.calendar.import.holiday.choose'),
            hidden: true,
            autoHeight: true,
            items: [choiceBrowseCaltext]
        });

        this.ImportICSPanel = new Wtf.FormPanel({
            frame:true,
            method : 'POST',
            fileUpload : true,
            waitMsgTarget: true,
            scope:this,
            title:WtfGlobal.getLocaleText('pm.project.calendar.import.information'),
            width:450,
            bodyStyle:"padding:10px 10px 10px 10px",
            items:[{
                xtype: "fieldset",
                title: WtfGlobal.getLocaleText('pm.project.calendar.import.openfile'),
                autoHeight:true,
                items:[{
                    xtype: "textfield",
                    fieldLabel: WtfGlobal.getLocaleText('pm.common.selectfile'),
                    name: "filevalue",
                    id: this.id + "browseBttnImportICS",
                    inputType: "file",
                    allowBlank: false,
                    validator: WtfGlobal.validateICSFile,
                    invalidText: WtfGlobal.getLocaleText("pm.common.validateics.invalidtext")
                }]
            }, {
                xtype:"fieldset",
                title:WtfGlobal.getLocaleText('pm.project.calendar.import.select'),
                autoHeight:true,
                items:[{
                    layout:"column",
                    border:false,
                    items:[{
                        columnWidth: 0.33,
                        border:false,
                        items:[{
                            xtype:"radio",
                            boxLabel:WtfGlobal.getLocaleText('pm.project.calendar.import.newcalendar'),
                            id: this.id + 'choiceRadioAddCal',
                            name:"choiceradio",
                            checked: true
                          }]
                      }, {
                        columnWidth: 0.33,
                        border:false,
                        items:[{
                            xtype:"radio",
                            id: this.id + 'choiceRadioMergeCal',
                            boxLabel:WtfGlobal.getLocaleText('pm.project.calendar.import.merge'),
                            name:"choiceradio"
                          }]
                      }, {
                        columnWidth: 0.33,
                        border: false,
                        items: [{
                            xtype: "radio",
                            id: this.id + 'choiceBrowseAddCal',
                            boxLabel: WtfGlobal.getLocaleText('pm.project.calendar.import.holiday'),
                            name: "choiceradio"
                          }]
                      }]
                  }]
            }, enterNewCalFieldset, enterMergeCalFieldset, enterBrowseCalFieldset]
        }, this);
        var ht = 330;
        if(Wtf.isWebKit)
            ht = 350;
        else if(Wtf.isIE)
            ht = 300;
        this.impICSWin = new Wtf.Window({
            resizable: false,
            scope: this,
            layout: 'fit',
            modal:true,
            width: 450,
            height: ht,
            iconCls: 'iconwin',
            id: this.id + 'importwindowImportICS',
            title: WtfGlobal.getLocaleText('pm.project.plan.import.text'),
            items: this.ImportICSPanel,
            buttons: [{
                anchor : '90%',
                text: WtfGlobal.getLocaleText('pm.project.calendar.import.text'),
                type: 'submit',
                width: 350,
                scope: this,
                handler:function() {
                    if(Wtf.getCmp(this.id + "choiceRadioAddCal").getValue()) {
                        var mode = "new";
                        var calName = WtfGlobal.HTMLStripper(choiceAddCaltext.value);
                        var calId = "";
                        choiceMergeCaltext.setValue("-1");
                        choiceBrowseCaltext.setValue("-1");
                    }
                    else if(Wtf.getCmp(this.id + "choiceRadioMergeCal").getValue()) {
                        mode = "merge";
                        calName = "";
                        calId = choiceMergeCaltext.getValue();
                        choiceAddCaltext.setValue("-1");
                        choiceBrowseCaltext.setValue("-1");
                    }
                    else if(Wtf.getCmp(this.id + "choiceBrowseAddCal").getValue()) {
                        mode = "browse";
                        calName = "";
                        calId = choiceBrowseCaltext.getValue();
                        choiceAddCaltext.setValue("-1");
                        choiceMergeCaltext.setValue("-1");
                    }
                    if(this.ImportICSPanel.form.isValid()){
                        this.ImportICSPanel.form.submit({
                            url:'../../importICS.jsp?userid='+loginid+'&projid='+projid+'&mode='+mode+'&calName='+calName+'&calId='+calId,
                            waitMsg :'Importing...',
                            scope:this,
                            success: function (result, request) {
                                calMsgBoxShow(129, 0);
                                this.impICSWin.close();
                            },
                            failure: function ( result, request) {
                                calMsgBoxShow(128, 1);
                                this.impICSWin.close();
                            }
                        },this);
                    } else {
//                        choiceAddCaltext.setValue("");
//                        choiceMergeCaltext.setValue("");
//                        choiceBrowseCaltext.setValue("");
                    }
                }
            }, {
                anchor : '90%',
                text: WtfGlobal.getLocaleText('lang.cancel.text'),
                width: 350,
                handler:function() {
                   this.impICSWin.close();
                },
                scope:this
            }]
        });
        this.impICSWin.show();
        var evtName = Wtf.isWebKit ? 'check' : 'focus';
        Wtf.getCmp(this.id + "choiceRadioAddCal").on(evtName, function(check, newval, oldval) {
            enterMergeCalFieldset.hide();
            enterBrowseCalFieldset.hide();
            Wtf.getCmp(this.id.split("choice")[0] + "browseBttnImportICS").enable();
            enterNewCalFieldset.show();
            choiceAddCaltext.enable();
            choiceMergeCaltext.disable();
            choiceBrowseCaltext.disable();
        });
        Wtf.getCmp(this.id + "choiceRadioMergeCal").on(evtName, function(check, newval, oldval) {
            enterNewCalFieldset.hide();
            enterBrowseCalFieldset.hide();
            Wtf.getCmp(this.id.split("choice")[0] + "browseBttnImportICS").enable();
            enterMergeCalFieldset.show();
            choiceAddCaltext.disable();
            choiceMergeCaltext.enable();
            choiceBrowseCaltext.disable();
        });
        Wtf.getCmp(this.id + "choiceBrowseAddCal").on(evtName, function(check, newval, oldval) {
            enterNewCalFieldset.hide();
            enterMergeCalFieldset.hide();
            Wtf.getCmp(this.id.split("choice")[0] + "browseBttnImportICS").disable();
            enterBrowseCalFieldset.show();
            choiceAddCaltext.disable();
            choiceMergeCaltext.disable();
            choiceBrowseCaltext.enable();
        });
    }
});
