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
Wtf.WtfCustomPanel = function(config, obj){
    Wtf.WtfCustomPanel.superclass.constructor.call(this, config);
    this.oldinnerHtml  ="";
    this.o1  ="";
    this.o2  ="";
    this.o3  ="";
    this.cmbcnt=1;
    this.config1.emptyText=WtfGlobal.getLocaleText('pm.paging.noresult');
    this.config1.tableClassName='datagridonDB';
    this.config1.tableHeader='';
    this.dataObj = obj;
}
Wtf.extend(Wtf.WtfCustomPanel, Wtf.Panel,{
    onRender: function(config){
        Wtf.WtfCustomPanel.superclass.onRender.call(this, config);
        this.header.replaceClass('x-panel-header','portlet-panel-header');
        for(var count = 0;count<this.config1.length;count++){
            this.count = count;
            this.newObj = this.config1[count];
            if(this.dataObj)
                this.initWidget(this.dataObj);
            else
                this.callRequest();
        }
    },

    getWorkSpaceLinks : function(flag, seq) {
        var items = [];
        var fileUpload = false;
        var h = (Wtf.isEmpty(projects.companyHolidays[0])) ? null : Wtf.util.clone(projects.companyHolidays);
        if(WtfGlobal.isDefined(seq)){
            var minVal = Date.parseDate(projects[seq].startDate, 'Y-m-j H:i:s');
            minVal = WtfGlobal.convertToOnlyDate(minVal);
        }
        switch(flag) {
            case 0 ://PROJECT PLAN
                items = [{
                    xtype : 'textfield',
                    fieldLabel:WtfGlobal.getLocaleText('pm.common.taskname'),
                    id : 'taskName'+this.id,
                    cls:'WidgetTextArea',
                    maxLength:512
                },
                this.startDate = new Wtf.form.DateField({
                    fieldLabel:WtfGlobal.getLocaleText('pm.common.startdate')+'*',
                    anchor:'98%',
                    cls: 'widgetDateField',
                    readOnly:true,
                    id:'taskstartdate'+this.id,
                    emptyText : WtfGlobal.getLocaleText('pm.admin.company.holidays.selectdate'),
                    allowBlank : false,
                    format: WtfGlobal.getOnlyDateFormat(),
                    renderer: formatDateWidget,
                    value: new Date(),
                    disabledDates: h,
                    disabledDays: projects[seq].nonWorkWeekDays,
                    minValue: minVal
                }),
                this.endDate = new Wtf.form.DateField({
                    fieldLabel:WtfGlobal.getLocaleText('pm.common.enddate')+'*',
                    anchor:'98%',
                    cls: 'widgetDateField',
                    readOnly:true,
                    allowBlank : false,
                    id:'taskenddate'+this.id,
                    emptyText : WtfGlobal.getLocaleText('pm.admin.company.holidays.selectdate'),
                    format: WtfGlobal.getOnlyDateFormat(),
                    value: new Date(),
                    renderer: formatDateWidget,
                    disabledDates: h,
                    disabledDays: projects[seq].nonWorkWeekDays,
                    minValue: minVal
                }),
                new Wtf.Button({
                    cls: 'button11',
                    text: WtfGlobal.getLocaleText('lang.cancel.text'),
                    scope : this,
                    minWidth: 70,
                    handler: function(){
                        this.form1.destroy();
                        Wtf.get('addrec'+this.id).dom.style.display = 'none';
                        Wtf.get('updatetable'+this.id).dom.style.display = 'block';
                    }
                }),
                new Wtf.Button({
                    id: 'upbtn',
                    cls: 'button1',
                    text: WtfGlobal.getLocaleText('lang.add.text'),
                    minWidth: 70,
                    scope : this,
                    handler: this.addtask
                })];
                break;
            case 1  ://TO-DO TASK
                items=[{
                    xtype : 'textfield',
                    fieldLabel:WtfGlobal.getLocaleText('pm.project.todo.text')+'*',
                    id:'todoname'+this.id,
                    cls:'WidgetTextArea',
                    allowBlank:false,
                    maxLength:100
                },
                this.desc = new Wtf.form.TextArea({
                    fieldLabel:WtfGlobal.getLocaleText('lang.description.text'),
                    cls:'WidgetTextArea',
                    anchor : '99%',
                    id:'tododesc'+this.id,
                    allowBlank:true,
                    maxlength:200
                }),
                this.dueDate = new Wtf.form.DateField({
                    fieldLabel:WtfGlobal.getLocaleText('lang.date.due.text'),
                    anchor:'98%',
                    cls: 'widgetDateField',
                    readOnly:true,
                    format: WtfGlobal.getOnlyDateFormat(),
                    id:'duedate'+this.id,
                    emptyText : WtfGlobal.getLocaleText('pm.admin.company.holidays.selectdate')
                }),new Wtf.Button({
                    cls: 'button11',
                    text: WtfGlobal.getLocaleText('lang.cancel.text'),
                    scope : this,
                    minWidth: 70,
                    handler : function(){
                        this.form1.destroy();
                        Wtf.get('addrec'+this.id).dom.style.display = 'none';
                        Wtf.get('updatetable'+this.id).dom.style.display = 'block';
                    }
                }), new Wtf.Button({
                    id: 'upbtn',
                    cls: 'button1',
                    text: WtfGlobal.getLocaleText('lang.add.text'),
                    scope : this,
                    minWidth: 70,
                    handler: this.addtodo
                })];
                break;
            case 2 : //CALENDAR
                this.getCalendarCombo();
                items = [{
                    xtype : 'textfield',
                    fieldLabel:WtfGlobal.getLocaleText('lang.subject.text')+'*',
                    id:'subject'+this.id,
                    cls:'WidgetTextArea',
                    maxLength: 100,
                    allowBlank: false
                },
                {
                    xtype :'combo',
                    id: 'calcombo' + this.id,
                    allowBlank :false,
                    fieldLabel: WtfGlobal.getLocaleText('pm.common.calendar')+'*',
                    store: this.calStore,
                    cls: 'listComboCss calComboCss',
                    valueField:'cid',
                    displayField: 'cname',
                    emptyText: WtfGlobal.getLocaleText('pm.project.calendar.import.choose.select'),
                    typeAhead: true,
                    mode: 'remote',
                    editable: true,
                    triggerAction: 'all',
                    forceSelection: true,
                    selectOnFocus: true
                },
                new Wtf.form.DateTimeField({
                    fieldLabel: WtfGlobal.getLocaleText('pm.project.calendar.newevent.startdate'),
                    id: "fromdate" + this.id,
                    name: "fromdate" + this.id,
                    width: 210,
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
                new Wtf.form.DateTimeField({
                    fieldLabel: WtfGlobal.getLocaleText('pm.common.enddate'),
                    id: "todate" + this.id,
                    name: "todate" + this.id,
                    width: 210,
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
                }),
                new Wtf.Button({
                    cls: 'button11',
                    text: WtfGlobal.getLocaleText('lang.cancel.text'),
                    scope : this,
                    minWidth: 70,
                    handler : function() {
                        this.form1.destroy();
                        Wtf.get('addrec'+this.id).dom.style.display = 'none';
                        Wtf.get('updatetable'+this.id).dom.style.display = 'block';
                    }
                }),
                new Wtf.Button({
                    id: 'upbtn',
                    cls: 'button1',
                    text: WtfGlobal.getLocaleText('lang.add.text'),
                    scope : this,
                    minWidth: 70,
                    handler: this.addevent
                })];
                //    Wtf.getCmp('calcombo'+this.id).setValue(this.calStore.getAt(0).data["cid"]);
                break;
            case 3 ://DOCUMENT
                fileUpload = true;
                items=[ new Wtf.form.TextField({
                    fieldLabel: WtfGlobal.getLocaleText('pm.common.filepath'),
                    id: 'filepath'+this.id,
                    inputType: 'file',
                    cls: 'WidgetTextArea',
                    allowBlank:false
                }), new Wtf.form.TextArea({
                    fieldLabel: WtfGlobal.getLocaleText('lang.comments.text'),
                    name: 'comment',
                    cls: 'WidgetTextArea',
                    maxLength: 80,
                    id: 'comment'+this.id
                }), new Wtf.Button({
                    cls: 'button11',
                    text: WtfGlobal.getLocaleText('lang.cancel.text'),
                    scope : this,
                    minWidth: 70,
                    handler: function() {
                        this.form1.destroy();
                        Wtf.get('addrec'+this.id).dom.style.display = 'none';
                        Wtf.get('updatetable'+this.id).dom.style.display = 'block';
                        if(this.id == 'mydocs_drag')
                            Wtf.getCmp('mydocs_drag').tools.w_mydocs.dom.style.display = 'block';
                    }
                }), new Wtf.Button({
                    id: 'upbtn',
                    cls: 'button1',
                    text: WtfGlobal.getLocaleText('pm.common.upload'),
                    minWidth: 70,
                    scope : this,
                    handler: this.adddocs
                }), new Wtf.Panel({
                    height: 0,
                    id : 'hidden'+this.id,
                    items: [new Wtf.form.TextField({
                        id: 'pcid',
                        value: 1,
                        height: 0
                    }), new Wtf.form.TextField({
                        id: 'userid',
                        height: 0,
                        value: loginid
                    }), new Wtf.form.TextField({
                        id: 'groupid',
                        height: 0,
                        value: 1
                    }), new Wtf.form.TextField({
                        fieldLabel: 'docid',
                        id: 'docid',
                        height: 0
                    }), new Wtf.form.TextField({
                        fieldLabel: 'docownerid',
                        id: 'docownerid',
                        height: 0
                    }), new Wtf.form.TextArea({
                        fieldLabel: WtfGlobal.getLocaleText('lang.Type.tezxt'),
                        id: 'type',
                        height: 0
                    })]
                })
                ];
                break;
        }
        this.form1 = new Wtf.form.FormPanel({
            border:false,
            id :'mainForm'+this.id,
            url: (this.id != "mydocs_drag") ? 'fileAddAction.jsp?a=a&fileadd=false&projectid='+this.id.substring(0,(this.id.length-5)):'fileAddAction.jsp?a=a&fileadd=false&projectid=',
            fileUpload: true,
            items:items,
            renderTo: 'addrec'+this.id,
            bodyStyle : (flag == 0 || flag == 1) ? 'font-size:10px;padding:10px 4% 0px 4%':'font-size:10px;padding:8px 10px 0px 9px',
            labelWidth: 70
        });
        if(flag == 3)
            Wtf.getCmp('hidden'+this.id).setVisible(false);
        if(flag == 2){
            this.calStore.on("load", function(store, recs, options){
                for(var i = 0; i<recs.length; i++){
                    var rec = this.calStore.getAt(i);
                    if(rec.get('caltype') == 4)
                        this.calStore.remove(rec);
                }
                Wtf.getCmp('calcombo'+this.id).setValue(this.calStore.getAt(0).data["cid"]);
            }, this);
            Wtf.getCmp('subject'+this.id).on('change', function(tf, nv, ov){
                tf.setValue(WtfGlobal.HTMLStripper(nv.trim()));
            }, this);
        }
        if(flag == 1){
            Wtf.getCmp('todoname'+this.id).on('change', function(tf, nv, ov){
                tf.setValue(WtfGlobal.HTMLStripper(nv.trim()));
            }, this);
        }
    }, 

 addtask : function() {
        if(Wtf.getCmp('taskName'+this.id).isValid() && Wtf.getCmp('taskstartdate'+this.id).isValid() && Wtf.getCmp('taskenddate'+this.id).isValid()){
            var pid = this.id.substring(0,(this.id.length-5));
            var taskname = WtfGlobal.HTMLStripper(Wtf.getCmp('taskName' + this.id).getValue());
            var startdate = Wtf.getCmp('taskstartdate'+this.id).getValue();
            var stdate = new Date(startdate);
            startdate =  startdate.format('Y-m-d 00:00:00');
            var enddate = Wtf.getCmp('taskenddate'+this.id).getValue();
            var edate = new Date(enddate);
            enddate =  enddate.format('Y-m-d 00:00:00');
            
            stdate = new Date(stdate.getFullYear(), stdate.getMonth(), stdate.getDate());
            edate = new Date(edate.getFullYear(), edate.getMonth(), edate.getDate());
            if (stdate <= edate){
                Wtf.Ajax.requestEx({
                    url: Wtf.req.prj + 'projectGridData.jsp',
                    params: {
                        action:'insertFromWidget',
                        rowindex:-1,
                        taskname: taskname,
                        startdate : startdate,
                        enddate : enddate,
                        projectid: pid,
                        userid : random_number
                    }
                },this,function(result, req) {
                    this.newObj.paramsObj.mode = 0;
                        this.callRequest();
                });
                this.form1.destroy();
                Wtf.get('addrec'+this.id).dom.style.display = 'none';
                Wtf.get('updatetable'+this.id).dom.style.display = 'block';
            } else {
                Wtf.getCmp('taskstartdate'+this.id).markInvalid(WtfGlobal.getLocaleText('pm.project.validatedates'));
                Wtf.getCmp('taskenddate'+this.id).markInvalid(WtfGlobal.getLocaleText('pm.project.validatedates'));
            }
        }
    },

    addtodo : function(){
        if(Wtf.getCmp('todoname' + this.id).isValid() && Wtf.getCmp('tododesc'+ this.id).isValid()){
            var pid = this.id.substring(0,(this.id.length-5));
            var taskname = WtfGlobal.HTMLStripper(Wtf.getCmp('todoname' + this.id).getValue());
            var duedate = Wtf.getCmp('duedate'+this.id).getValue();
            if(duedate){
                duedate =  duedate.format('Y/m/d H:i:s');
            }else{
                duedate = "1970/01/01 00:00:00";
            }
            var desc =  WtfGlobal.HTMLStripper(Wtf.getCmp('tododesc' + this.id).getValue());
            Wtf.Ajax.requestEx({
                url: Wtf.req.prj + 'todolistmanager.jsp',
                params: {
                    taskname:taskname,
                    localid:"",
                    taskorder:0,
                    status:0,
                    parentId:"",
                    taskid:"",
                    desc:desc,
                    priority:"Normal",
                    userid:pid,
                    grouptype:2,
                    leafflag: true,
                    action:3,
                    duedate:duedate,
                    assignedto:""
                }
            },this,
            function(result, req) {
                this.newObj.paramsObj.mode = 1;
                this.callRequest();
            });
            this.form1.destroy();
            Wtf.get('addrec'+this.id).dom.style.display = 'none';
            Wtf.get('updatetable'+this.id).dom.style.display = 'block';
        }
    },

    addevent : function(){
        if(Wtf.getCmp('subject'+this.id).isValid() && Wtf.getCmp('calcombo'+this.id).isValid() && Wtf.getCmp('fromdate'+this.id).isValid() && Wtf.getCmp('todate'+this.id).isValid()){
            var subject = WtfGlobal.HTMLStripper(Wtf.getCmp('subject'+this.id).getValue());
            var starttime = "";
            var endtime = "";
            if(!Wtf.getCmp('fromdate'+this.id).getValue() == "")
                starttime = Wtf.getCmp('fromdate'+this.id).getValue().format('Y-m-d H:i:s.00');
            else {
                var a = Wtf.getCmp('fromdate'+this.id).df.getValue().format('Y-m-j') +" " + Wtf.getCmp('fromdate'+this.id).tf.getValue();
                starttime = Date.parseDate(a, 'Y-m-j g:i A').format('Y-m-d H:i:s.00');
            }
            if(!Wtf.getCmp('todate'+this.id).getValue() == "")
                endtime = Wtf.getCmp('todate'+this.id).getValue().format('Y-m-d H:i:s.00');
            else {
                a = Wtf.getCmp('todate'+this.id).df.getValue().format('Y-m-j') +" " + Wtf.getCmp('todate'+this.id).tf.getValue();
                endtime = Date.parseDate(a, 'Y-m-j g:i A').format('Y-m-d H:i:s.00');
            }
            var cal = Wtf.getCmp('calcombo'+this.id).getValue();
            if(starttime<endtime){
                Wtf.Ajax.requestEx({
                    method: 'GET',
                    url: Wtf.req.cal + 'calEvent.jsp',
                    params: ({
                        calView: 0,
                        action: 1,
                        eid: "0",
                        cid: cal,
                        startts: starttime,
                        endts: endtime,
                        subject: subject,
                        descr:"",
                        location: "",
                        showas:"b",
                        priority:"m",
                        recpattern:"",
                        recend:"1970-01-01 00:00:00",
                        resources:"",
                        reminders:"",
                        allDay:false
                    })
                },
                this,
                function(result, req){
                    var nodeobj = eval("(" + result + ")");
                    if(nodeobj.success=="false")
                        calMsgBoxShow(125, 1);
                    else
                        this.newObj.paramsObj.mode = 2;
                    this.callRequest();
                },
                function(result, req){
                    calMsgBoxShow(4, 1);
                });
                this.form1.destroy();
                Wtf.get('addrec'+this.id).dom.style.display = 'none';
                Wtf.get('updatetable'+this.id).dom.style.display = 'block';
            }else{
                Wtf.getCmp('fromdate'+this.id).markInvalid(WtfGlobal.getLocaleText('pm.project.validatedates'));
                 Wtf.getCmp('todate'+this.id).markInvalid(WtfGlobal.getLocaleText('pm.project.validatedates'));
            }
        } else {
            Wtf.getCmp('fromdate'+this.id).markInvalid("End date must be greater than start date.");
            Wtf.getCmp('todate'+this.id).markInvalid("End date must be greater than start date.");
        }
    },

    adddocs : function(){
        if (document.getElementById('filepath'+this.id).value <= 0) {
            msgBoxShow(206,1);
        } else {
            var textArea = Wtf.get('filepath'+this.id);
            Wtf.Ajax.requestEx({
                url: Wtf.req.doc + 'file-releated/filecontent/chkfile.jsp',
                params: {
                    docname: textArea.getValue(),
                    groupid: 1,
                    pcid: 1
                }
            }, this, function(res, req){
                ResultFunWidget(res, this.id);
                Wtf.get('addrec'+this.id).dom.style.display = 'none';
                Wtf.get('updatetable'+this.id).dom.style.display = 'block';
            }, function(){
                msgBoxShow(304, 1);
                this.form1.destroy();
                Wtf.get('addrec'+this.id).dom.style.display = 'none';
                Wtf.get('updatetable'+this.id).dom.style.display = 'block';
            });
            if(this.id == 'mydocs_drag')
                Wtf.getCmp('mydocs_drag').tools.w_mydocs.dom.style.display = 'block';
        }
    }, 

    getCalendarCombo : function() {
        var pid = this.id.substring(0,(this.id.length-5));
        this.calStorereder = Wtf.data.Record.create(["cname","cid", "caltype"])
        this.calStore = new Wtf.data.Store({
            url: Wtf.req.cal + "caltree.jsp",
            id: 'calStore'+this.id,
            baseParams:{
                action:0,
                userid:pid,
                caltype:2,
                loginid: loginid,
                latestts:"1970-01-01 00:00:00"
            },
            reader:new Wtf.data.KwlJsonReader({
                root:'data'
            },this.calStorereder),
            autoLoad:false
        });
//        this.calStore.load();
        
    }, 

    writeTemplateToBody:function(innerHTML,lib,ss,pgsize,pager){
        var temp1 = innerHTML ;
        var temp3 ='';
        if(this.config1.length >1){
            var temp2 = this.oldinnerHtml;
            temp3 = '<div style="background-color:#DFE8F6;padding:7px;float:left;width:97%">'
            +((this.newObj.isSearch)?this.addSearchBar1():"")
            +'<div style="background-color:#ffffff;padding:1%;float:left;width:95.5%">'
            + temp2 + lib
            +((this.newObj.isPaging)?this.changePagingBar(pager):"")
            +'</div>'
            +'</div>'
            +'</div>';
        } else {
            temp3 = temp1;
        }
        var temp=new Wtf.Template(temp3);
        this.oldinnerHtml += lib;
        temp.overwrite(this.body);
    },

    addSearchBar1:function(searchss){
        var valueStr="";
        if(searchss){
            valueStr = "value = "+searchss;
        }
        var a1 =this.id;
        return('<div style="height:24px;width:97.5%;background-image:url(../../images/search-field-bg.gif);margin-bottom:6px;">'
            +'<div id="searchdiv\"'+a1+'\" class="search_div" onclick="btnpressed(\''+a1+'\')">Search</div>'
            +'<div class="searchspacer">&nbsp;</div>'
            +'<div style="width: 85%;overflow:hidden;">'
            +'<input '+valueStr+' onkeypress=\"javascript:if(event.keyCode==13)btnpressed(\''+a1+'\');\" style="background-color: transparent;float:left;border:none; padding-top:3px; height:21px; border-left:solid 1px #a0bcda;width:100%;" type="text" id="search'+a1+'" />'
            +'</div>'
            +'</div>');
    },

    paging:function(numpages,searchss){
        var a =this.id;
        if(!searchss) {
            searchss="";
        }
        var to = this.panelcount;
        if(this.panelcount>this.totalCount)
            to = this.totalCount;
        var pagininfo = "<span id='"+a+"pagetext0' style='float:left;padding-top:3px;'> 1 - "+to+" "+ WtfGlobal.getLocaleText("pm.dashboard.widget.paging.of") +" "+this.totalCount+"</span>";
        var pager = '<div id="pageinfobar'+a+'" class="portlet-paging">'+pagininfo;
        if(numpages>1) {
            pager +='<span wtf:qtip="'+WtfGlobal.getLocaleText('lang.older.text')+'" id="'+a+'nextpage0" class="pagination-div next-pagination" onclick="pagingRedirect(\''+a+'\',1,'+this.count+',\''+searchss+'\','+this.panelcount+');">1</span>';
            pager +='<span wtf:qtip="'+WtfGlobal.getLocaleText('lang.oldest.text')+'" id="'+a+'lastpage0" class="pagination-div last-pagination" onclick="pagingRedirect(\''+a+'\','+(numpages-1)+','+this.count+',\''+searchss+'\','+this.panelcount+');">1</span>';
            }
        pager=pager+'</div>';
        return(pager);
    },

    changePagingBar : function(currentPage){
        var a = this.id;
        var numpages = Math.ceil(this.totalCount/this.panelcount)
        var from = (currentPage * this.panelcount)+1;
        var to = from + this.panelcount-1;
        if(to>this.totalCount)
            to = this.totalCount;
        var pagininfo = "<span id='"+a+"pagetext"+currentPage+"' style='float:left;padding:3px 0px 0px 6px;'> "+from+" - "+to+" "+ WtfGlobal.getLocaleText("pm.dashboard.widget.paging.of") +" "+this.totalCount+"</span>";
        var pager = '<div id="pageinfobar'+a+'" style="float:left;padding-right:7px;background-color:#f1f1f1;width:98%;">';
        if(numpages>1 && currentPage==0) {//first page
            pager +=pagininfo;
            pager +='<span wtf:qtip="'+WtfGlobal.getLocaleText('lang.older.text')+'" id="'+a+'nextpage'+currentPage+'" class="pagination-div next-pagination" onclick="pagingRedirect(\''+a+'\','+(currentPage+1)+','+this.count+',\'\','+this.panelcount+');">1</span>';
            pager +='<span wtf:qtip="'+WtfGlobal.getLocaleText('lang.oldest.text')+'" id="'+a+'lastpage'+currentPage+'" class="pagination-div last-pagination" onclick="pagingRedirect(\''+a+'\','+(numpages-1)+','+this.count+',\'\','+this.panelcount+');">1</span>';
        } else if(currentPage==(numpages-1)){//last page
            pager += '<span wtf:qtip="'+WtfGlobal.getLocaleText('lang.newest.text')+'" id="'+a+'firstpage'+currentPage+'" class="pagination-div first-pagination" onclick="pagingRedirect(\''+a+'\','+(0)+','+this.count+',\'\','+this.panelcount+');">1</span>';
            pager += '<span wtf:qtip="'+WtfGlobal.getLocaleText('lang.newer.text')+'" id="'+a+'prevpage'+currentPage+'" class="pagination-div prev-pagination" onclick="pagingRedirect(\''+a+'\','+(currentPage-1)+','+this.count+',\'\','+this.panelcount+');">1</span>';
            pager += pagininfo;
        } else {
            pager += '<span wtf:qtip="'+WtfGlobal.getLocaleText('lang.newest.text')+'" id="'+a+'firstpage'+currentPage+'" class="pagination-div first-pagination" onclick="pagingRedirect(\''+a+'\','+(0)+','+this.count+',\'\','+this.panelcount+');">1</span>';
            pager += '<span wtf:qtip="'+WtfGlobal.getLocaleText('lang.newer.text')+'" id="'+a+'prevpage'+currentPage+'" class="pagination-div prev-pagination" onclick="pagingRedirect(\''+a+'\','+(currentPage-1)+','+this.count+',\'\','+this.panelcount+');">1</span>';
            pager += pagininfo;
            pager += '<span wtf:qtip="'+WtfGlobal.getLocaleText('lang.older.text')+'" id="'+a+'nextpage'+currentPage+'" class="pagination-div next-pagination" onclick="pagingRedirect(\''+a+'\','+(currentPage+1)+','+this.count+',\'\','+this.panelcount+');">1</span>';
            pager += '<span wtf:qtip="'+WtfGlobal.getLocaleText('lang.oldest.text')+'" id="'+a+'lastpage'+currentPage+'" class="pagination-div last-pagination" onclick="pagingRedirect(\''+a+'\','+(numpages-1)+','+this.count+',\'\','+this.panelcount+');">1</span>';
        }
        pager=pager+'</div>';
        return(pager);
    }, 

    callRequest:function(url,searchss,pager){
        if(this.newObj.isItems){
            var its = this.newObj.itemArr;
            for(var i = 0; i < its.length; i++){
                if(its[i].getXType() == 'grid'){
                    its[i].getStore().reload();
                }
            }
        } else {
            this.panelcount = (this.newObj.numRecs)?this.newObj.numRecs:this.panelcount;
            var headerHtml = this.newObj.headerHtml;
            var mytestpl=this.newObj.template;
            var xtooltip = this.newObj.xtooltip;
            var formatField = this.newObj.formatField;
            var prefixImage = this.newObj.prefixImage;
            var imageField = this.newObj.imageField;
            var formatFileSize = this.newObj.formatFileSize;
            var quoteFormatField = this.newObj.quoteFormatField;
            var tpl_tool_tip = "";
            var autoHide = "";
            var closable = "";
            var height = "";
            var emptyText = "";
            var shwCombo=this.newObj.isCombo;
            if(this.newObj.emptyText != null){
                emptyText = this.emptyText;
            }
        
            if(this.newObj.tool_tip != null){
                tpl_tool_tip = this.newObj.tool_tip.tpl_tool_tip;
                autoHide = this.newObj.tool_tip.autoHide;
                closable = this.newObj.tool_tip.closable;
                height = this.newObj.tool_tip.height;
            }

            Wtf.Ajax.requestEx({
                url: (url)?url:this.newObj.url+"?limit="+this.panelcount+"&start=0&searchString=",
                params: this.newObj.paramsObj
            },
            this,
            function(result, req) {
                var innerHTML="";
                var obj = eval('('+result+')');
                if(xtooltip && formatField){
                    this.newObj.formatField = formatField;
                    this.formatSpecifiedField(obj);
                }
                
                if(xtooltip && quoteFormatField){
                    this.newObj.quoteFormatField = quoteFormatField;
                    this.formatquoteField(obj);
                }

                if(prefixImage && imageField){
                    this.newObj.imageField = imageField;
                    this.formatFileName(obj);
                }

                if(formatFileSize){
                    this.formatFileSize(obj);
                }
                if(this.id != 'crmmodule_drag') {
                    var lib=this.getDataString(obj,mytestpl,headerHtml,tpl_tool_tip,autoHide,closable,height,emptyText,shwCombo);
                    this.totalCount = obj.count;
                    innerHTML = this.getPagingString(lib,obj,searchss,pager);
                    var pgsize=Math.ceil(obj.count/this.panelcount);
                    var re = /<script\b[\s\S]*?>([\s\S]*?)<\//ig;
                    var match;
                    if(match = re.exec(innerHTML)) {
                        this.chartScriptText1 = match[1];
                    }
                    if(match = re.exec(innerHTML)) {
                        this.chartScriptText2 = match[1];
                    }
                    this.writeTemplateToBody(innerHTML,lib,searchss,pgsize,pager);
                    mytestpl = "";
                    this.togglePageCss(this.id,pager);
                    if(this.cmb3)
                        document.getElementById("cmb3").selectedIndex=this.cmb3;
                    if(url && this.isSearch){
                        document.getElementById("search"+this.id).value=searchss;
                    }
                    if(this.storeflag){
                        this.storefunction.call();
                    }
                } else {
                    this.body.dom.innerHTML = '<div class="portlet-body">'
                    +'<div style="background-color:#ffffff;padding:1%;float:left;width:95.5%" id="crmDashboardThumbnailPortlet">'
                    +'</div>'
                    +'</div>';
                    for(var cnt=0;cnt<obj.data.length;cnt++) {
                        new Wtf.emailTemplateThumbnail({
                            id: "thumbnail_" + cnt,
                            tName: obj.data[cnt].name,
                            thumbnail: obj.data[cnt].img,
                            tqtip: obj.data[cnt].qtip,
                            tempRec:cnt,
                            scope: this,
                            listeners: {
                                "templateSelected": this.selectTemplate
                            },
                            renderTo: "crmDashboardThumbnailPortlet"
                        });
                    }
                }
                if(this.id == 'chart_drag')
                    this.runChartScript();
            },
            function(result, req){
                mytestpl = "";
            }
            );
        }
    },

    initWidget:function(obj,url,searchss,pager){
        if(this.newObj.isItems){
            var its = this.newObj.itemArr;
            for(var i = 0; i < its.length; i++){
                this.add(its[i]);
                if(its[i].getXType() == 'grid'){
                      its[i].getStore().loadData(obj);
                      if(its[i].getStore().getCount() > 15){
                          its[i].autoHeight = false;
                          its[i].setHeight(400);
                      }
                }
            }
        } else {
            this.panelcount = (this.newObj.numRecs)?this.newObj.numRecs:this.panelcount;
            var headerHtml = this.newObj.headerHtml;
            var mytestpl=this.newObj.template;
            var xtooltip = this.newObj.xtooltip;
            var formatField = this.newObj.formatField;
            var prefixImage = this.newObj.prefixImage;
            var imageField = this.newObj.imageField;
            var formatFileSize = this.newObj.formatFileSize;
            var quoteFormatField = this.newObj.quoteFormatField;
            var tpl_tool_tip = "";
            var autoHide = "";
            var closable = "";
            var height = "";
            var emptyText = "";
            var shwCombo=this.newObj.isCombo;
            if(this.newObj.emptyText != null){
                emptyText = this.emptyText;
            }

            if(this.newObj.tool_tip != null){
                tpl_tool_tip = this.newObj.tool_tip.tpl_tool_tip;
                autoHide = this.newObj.tool_tip.autoHide;
                closable = this.newObj.tool_tip.closable;
                height = this.newObj.tool_tip.height;
            }

            var innerHTML="";
            if(xtooltip && formatField){
                this.newObj.formatField = formatField;
                this.formatSpecifiedField(obj);
            }

            if(xtooltip && quoteFormatField){
                this.newObj.quoteFormatField = quoteFormatField;
                this.formatquoteField(obj);
            }

            if(prefixImage && imageField){
                this.newObj.imageField = imageField;
                this.formatFileName(obj);
            }

            if(formatFileSize){
                this.formatFileSize(obj);
            }
            var lib=this.getDataString(obj,mytestpl,headerHtml,tpl_tool_tip,autoHide,closable,height,emptyText,shwCombo);
            this.totalCount = obj.count;
            innerHTML = this.getPagingString(lib,obj,searchss,pager);
            var pgsize=Math.ceil(obj.count/this.panelcount);
            var re = /<script\b[\s\S]*?>([\s\S]*?)<\//ig;
            var match;
            if(match = re.exec(innerHTML)) {
                this.chartScriptText1 = match[1];
            }
            if(match = re.exec(innerHTML)) {
                this.chartScriptText2 = match[1];
            }
            this.writeTemplateToBody(innerHTML,lib,searchss,pgsize,pager);
            mytestpl = "";
            this.togglePageCss(this.id,pager);
            if(this.cmb3)
                document.getElementById("cmb3").selectedIndex=this.cmb3;
            if(url && this.isSearch)
                document.getElementById("search"+this.id).value=searchss;
            if(this.storeflag)
                this.storefunction.call();
            if(this.id == 'chart_drag')
                this.runChartScript();
        }
    },

    getDataString:function(obj, tpl, headerHtml,tpl_tool_tip,autoHide,closable,height,emptyText,shwCombo) {
        var lib= "";
        if(this.newObj.isTable == true){
            lib = lib +"<table class='"+this.newObj.tableClassName+"' border='0' cellspacing=0 width='100%' style='float:left;margin:0px;'>";
            if(this.newObj.tableHeader != null){
                lib = lib +this.newObj.tableHeader;
            }
        } else {
            lib = lib +"<div class='content-wrapper'>";
        }
        lib = lib +headerHtml;
        lib = lib +"<div id='portalContent"+this.id+"' style='padding-top:5px;'>";

        if(shwCombo) {
            lib = lib  +'<select id="cmb'+this.cmbcnt+'" style="width:200px;margin-right:3px;margin-left:5px;" onchange="filterBrands('+this.cmbcnt+')"><option  value="">Select Value --<option>';
            this.cmbcnt++;
        }
        if(this.isProject){
            for(var i=0;i<obj.data.length;i++){
                if(this.newObj.isToolTip == true){
                    var target = "KCUser"+obj[i].userid;
                    createtooltip1(target,tpl_tool_tip,autoHide,closable,height);
                }
                if(obj.length==0){
                    lib = emptyText;
                }
                if(this.pagingflag){
                    if(obj.count == -1){
                        this.config0[0].isPaging = false;
                        this.config0[0].WorkspaceLinks = '';
                        if(this.timeid){
                            clearTimeout(this.timeid);
                        }
                    } else{
                        this.config0[0].isPaging = true;
                        this.config0[0].WorkspaceLinks = signoutLinks;
                    }
                }
                lib = lib  + tpl.applyTemplate(obj.data[i]);
            }
        } else {
            lib = lib  + tpl.applyTemplate(obj[0]);
        }
        if(shwCombo)
            lib = lib  +"</select></div>";
        else {
            if(this.newObj.isTable == true){
                lib = lib +"</table>";
            } else {
                lib = lib +"</div>";
            }
            lib += (Wtf.isIE6 || Wtf.isIE7) ? (!this.config1[0].isPaging) ?  "<br style='clear:both'/>" : "" : "<br style='clear:both'/>";
        }
        return lib;
    },
 
    getPagingString:function(lib,obj,searchss,pager) {
        this.panelcount = (this.newObj.numRecs)?this.newObj.numRecs:this.panelcount;
        var quickAddDiv = "";
        if(this.isProject)
            quickAddDiv = '<div id="addrec'+this.id+'" style="display:none;background-color:#ffffff;padding:1%;float:left;width:95.5%"></div>';
        var tmpHTML = "";
        if(obj.count!=0){
            //             var links = " ";
            //            if(this.newObj.WorkspaceLinks != null){
            //                links = this.newObj.WorkspaceLinks;
            //            }
            var pgsize=Math.ceil(obj.count/this.panelcount);
            tmpHTML = '<div class="portlet-body">'
            +((this.newObj.isSearch)?this.addSearchBar1(searchss):"")
            +'<div id="updatetable'+this.id+'" style="background-color:#ffffff;padding:1%;float:left;width:95.5%">'
            +lib
            +((this.newObj.isPaging)? (pager===undefined ? this.paging(pgsize,searchss) : this.changePagingBar(pager)):"")
            +'</div>'
            +'</div>'+ quickAddDiv;
        } else {
            if(this.newObj.emptyText!=null){
                links = " ";
                if(this.newObj.WorkspaceLinks != null){
                    links = this.newObj.WorkspaceLinks;
                }
                tmpHTML = '<div class="portlet-body">'
                +((this.newObj.isSearch)?this.addSearchBar1():"")
                +'<div id="updatetable'+this.id+'" style="background-color:#ffffff;padding:1%;float:left;width:95.5%">'
                + lib + '<div class="widgetEmptyText">' + this.getClickedTool() + '</div>'
                +'</div></div>'+ quickAddDiv;
            }
        }
        return tmpHTML;
    },  

    getClickedTool: function(){
        var toolText = '';
        switch(this.press){
            case 0:
                if(Wtf.featuresView.proj)
                    toolText = WtfGlobal.getLocaleText('pm.project.plan.notip');
                else
                    toolText = WtfGlobal.getLocaleText('pm.project.tip.hidden');
                break;
            case 1:toolText = WtfGlobal.getLocaleText({key:'pm.common.tip.noupdate',params:[WtfGlobal.getLocaleText('Todo')]});break;
            case 2:toolText = WtfGlobal.getLocaleText({key:'pm.common.tip.noupdate',params:[WtfGlobal.getLocaleText('pm.modules.cal')]});break;
            case 3:toolText = WtfGlobal.getLocaleText({key:'pm.common.tip.noupdate',params:[WtfGlobal.getLocaleText('pm.module.documents')]});break;
            case 4:toolText =  WtfGlobal.getLocaleText({key:'pm.common.tip.noupdate',params:[WtfGlobal.getLocaleText('pm.module.projectsettings')]});break; // 5 is for reports which cannot be empty
            case 6:toolText =  WtfGlobal.getLocaleText({key:'pm.common.tip.noupdate',params:[WtfGlobal.getLocaleText('Discussion.Forums')]});break;
            default:
                switch(this.id.split('_')[0]){
                    case 'pm':
                        toolText = WtfGlobal.getLocaleText('pm.personalmessage.inbox.notip');
                        break;
                    case 'mydocs':
                        toolText = WtfGlobal.getLocaleText('pm.project.document.emptytext')+'<br>'+WtfGlobal.getLocaleText('pm.dashboard.widget.mydocuments.empty2');
                        break;
                    case WtfGlobal.getLocaleText('pm.dashboard.widget.announcements.text'):
                        toolText = WtfGlobal.getLocaleText('pm.dashboard.widget.announcement.emptytext');
                        break;
                    case WtfGlobal.getLocaleText('pm.dashboard.widget.requests.text'):
                        toolText = WtfGlobal.getLocaleText('pm.qtip.request.pending');
                        break;
                    default:toolText = WtfGlobal.getLocaleText('pm.qtip.noupdates');
                        break;
                }
                break;
        }
        return toolText;
    },

    doPaging:function(url,offset,searchstr,pager,subPan){
        url+="?limit="+this.panelcount+"&start="+offset+"&searchString="+searchstr;
        this.callRequest(url,searchstr,pager);
    },

    togglePageCss: function(panelid,pager){
        var clickedDiv = document.getElementById(panelid+pager);
        var prevDiv;
        for(var x=0;x<pager;x++){
            prevDiv = document.getElementById(panelid+(x));
            if(prevDiv)
                prevDiv.className="pagination-div deactive-pagination";
        }
        if(clickedDiv)
            clickedDiv.className="pagination-div active-pagination";
        if(this.id == 'welcome_drag')
            Wtf.get('updatetable'+this.id).dom.parentNode.style.width = '100.5%';
    },

    doSearch: function(url,searchstr){
        var str = this.newObj.url;
        var myArr = str.split('?');
        var newUrl=myArr[0]+"?limit="+this.panelcount+"&start=0&searchString="+searchstr;
        this.callRequest(newUrl,searchstr);
    },

    formatSpecifiedField : function(obj){
        for(i=0;i<obj.data.length;i++){
            for(j=0;j<this.newObj.formatField.length;j++){
                obj.data[i][this.newObj.formatField[j]] = getFormattedDate(obj.data[i][this.newObj.formatField[j]]);
            }
        }
    },

    formatquoteField : function(obj){
        for(i=0;i<obj.data.length;i++){
            for(j=0;j<this.newObj.quoteFormatField.length;j++){
                obj.data[i][this.newObj.quoteFormatField[j]] = obj.data[i][this.newObj.quoteFormatField[j]].adjustQuotes();
            }
        }
    },
    
    formatFileName : function(obj){
        for(var i = 0 ; i < obj.data.length; i++){
            if(obj.data[i].docName != undefined && obj.data[i].docName != null && obj.data[i].docName != ""){
                var imageClass = getimage(obj.data[i].docName);
                obj.data[i].imageClass = imageClass;
            }
        }
    },

    formatFileSize : function(obj){
        obj.data[0].totalSize = getFileSize(obj.data[0].totalSize);
    },
    
    runChartScript : function() {
        var html = Wtf.get('projChart1').dom.innerHTML;
        if(html !== ""){
            var re = /<script\b[\s\S]*?>([\s\S]*?)<\//ig;
            var match;
            if((match = re.exec(html))) {
                eval(match[1]);
                var done = 1;
            }
            if(!done){
                if((match = re.exec(html))) {
                    eval(match[1]);
                    done = 1;
                }
            }
        } else if(this.chartScriptText1){
            eval(this.chartScriptText1);
        }
        done = 0;
        html = Wtf.get('projChart2').dom.innerHTML;
        if(html !== ""){
            re = /<script\b[\s\S]*?>([\s\S]*?)<\//ig;
            if((match = re.exec(html))) {
                eval(match[1]);
                done = 1;
            }
            if(!done){
                if((match = re.exec(html))) {
                    eval(match[1]);
                    done = 1;
                }
            }
        } else if(this.chartScriptText2){
            eval(this.chartScriptText2);
        }
    },

    changeImg : function(ind, tool, mod) {
        var normalClass = ['x-tool x-tool-w_projectplan', 'x-tool x-tool-w_todo', 'x-tool x-tool-w_calendar', 'x-tool x-tool-w_docs', 'x-tool x-tool-w_admin','x-tool x-tool-w_reports', 'x-tool x-tool-w_discussion'];
        var dimClass = ['x-tool x-tool-w_projectplan_dim', 'x-tool x-tool-w_todo_dim', 'x-tool x-tool-w_calendar_dim', 'x-tool x-tool-w_docs_dim','x-tool x-tool-w_admin_dim', 'x-tool x-tool-w_reports_dim', 'x-tool x-tool-w_discussion_dim'];
        var toolName = [this.tools.w_projectplan, this.tools.w_todo_dim, this.tools.w_calendar_dim, this.tools.w_docs_dim, this.tools.w_admin_dim, this.tools.w_reports_dim, this.tools.w_discussion_dim];
        var toolNameMod = [this.tools.w_projectplan, this.tools.w_todo, this.tools.w_calendar, this.tools.w_docs, this.tools.w_admin, this.tools.w_reports, this.tools.w_discussion];
        //if(ind != 5)
            tool.dom.className = normalClass[ind];
        for(var i=0; i<dimClass.length; i++){
            if(i != ind){
                if(toolName[i])
                    tool = toolName[i];
                else{
                    if(mod == i)
                        tool = toolNameMod[mod];
                    else
                        tool = toolName[i];
                }
                if(tool)
                    tool.dom.className = dimClass[i];
            }
        }
    }
});
function formatDateWidget(value) {
    return WtfGlobal.onlyDateRenderer(value);
}
function ResultFunWidget(response, id){
    var value = eval('(' + response + ')');
    var rs = value.type;
    Wtf.getCmp('type').setValue(rs);
    var val = value.data;
    var userdocid = value.userdocid;
    if (rs == 1 || rs == 2) {
        if (rs == 1) {
        }
        if (rs == 2) {
            UploadFileWidget(id);
        }
    } else if (rs == 3) {
    } else if (rs == 4) {
        UploadFileWidget(id);
    }
}

function downloadDocWidget(docid){
    setDownloadUrl(docid);
}

function UploadFileWidget(id){
    Wtf.getCmp('mainForm'+id).form.submit({
        waitMsg: WtfGlobal.getLocaleText('pm.common.uploading')+'...',
        success: function(frm, action){
            if (action.response.responseText != "") {
                var uploadstr = eval('(' + action.response.responseText + ')');
                if (uploadstr.msg != null && uploadstr.msg != "1") {
                    msgBoxShow([WtfGlobal.getLocaleText('pm.msg.ERROR'), uploadstr.msg], 1);
                }
            } else
                frm.reset();
            Wtf.getCmp('mainForm'+id).destroy();
            if(id == 'mydocs_drag')
                Wtf.getCmp('mydocs_drag').callRequest();
            else
                Wtf.getCmp(id).newObj.paramsObj.mode = 3;
                Wtf.getCmp(id).callRequest();
        }, 
        failure: function(){
            Wtf.getCmp('mainForm'+id).destroy();
        }
    });
}

function showResults(widgetid) {
    
    var cmbstr="";
    var widget = Wtf.getCmp(widgetid);
    widget.cmb3 = document.getElementById('cmb3').selectedIndex;
    if((document.getElementById('cmb1')) && (document.getElementById('cmb1').value !='')) {
        cmbstr=cmbstr+document.getElementById('cmb1').value+",";
    }
    if((document.getElementById('cmb2')) && (document.getElementById('cmb2').value !='')) {
        cmbstr=cmbstr+document.getElementById('cmb2').value+",";
    }
    if((document.getElementById('cmb3')) && (document.getElementById('cmb3').value !='')) {
        cmbstr=cmbstr+document.getElementById('cmb3').value+",";
    }
    
    Wtf.getCmp('DSBKnowledgeCampus').doSearch("jspfiles/knowledgeUni/workspace.jsp",cmbstr);
}

function filterBrands(val) {

    if( (val==1) &&  (document.getElementById('cmb1')) ) {
                 
        Wtf.Ajax.requestEx({
            url:'jspfiles/knowledgeUni/CenterManagement.jsp',
            params:{
                flag:119,
                segmentname:document.getElementById('cmb1').value
            }
        },
        this,
        function(result,resp){
            if(result.success){
                var cmb2Select = document.getElementById('cmb2');
                cmb2Select.innerHTML = "";
                var optSelectEl = getOptionElement("Select Value --" , "");
                Wtf.isIE ? cmb2Select.add(optSelectEl) : cmb2Select.appendChild(optSelectEl);

                optSelectEl =getOptionElement("" , "");
                Wtf.isIE ? cmb2Select.add(optSelectEl) : cmb2Select.appendChild(optSelectEl);

                for(var i=0;i<result.data.length;i++){
                    var text = result.data[i].name;
                    var opt = getOptionElement(text , text);
                    Wtf.isIE ? cmb2Select.add(opt) : cmb2Select.appendChild(opt);
                }
            }else{
                msgBoxShow(6,1);
            }

        }, function(){
            msgBoxShow(6,1);
        });

    }

}

function getOptionElement(text,val){
    var opt = document.createElement("option");
    opt.value = val;
    opt.text = text;
    return opt;
}

function getReportName(reportname, projname, projid){
    reportActionValue = reportname;
    if(Wtf.getCmp('tab'+projid)){
        mainPanel.loadTab('../../project.html', "tab" + projid, projname, "navareadashboard", Wtf.etype.proj, true, "projectplanclicked");
        Wtf.getCmp("subtabpanelcomprojectTabs_"+projid).setActiveTab(Wtf.getCmp(projid+"projPlanCont").rPanel);
    } else {
        mainPanel.loadTab('../../project.html', "tab" + projid, projname, "navareadashboard", Wtf.etype.proj, true, "projectplanclicked");
    //mainPanel.loadTab('../../project.html', "tab" + projid, projname, "navareadashboard", Wtf.etype.proj, true, "home");
    }
}
function myDocClicked(docid){
    globalDocId = docid;
    mainPanel.loadTab('../../documents.html', "tabdocument", WtfGlobal.getLocaleText('pm.dashboard.widget.mydocuments.text'), "navareadocs", Wtf.etype.docs, false);
}

function gotoPM(postid){
    globalPostid = postid;
    navigate('pm');
}

Wtf.emailTemplateThumbnail = function(conf){
    Wtf.apply(this, conf);
    this.addEvents({
        "templateSelected": true
    });
    Wtf.emailTemplateThumbnail.superclass.constructor.call(this, conf);
}

Wtf.extend(Wtf.emailTemplateThumbnail, Wtf.Component, {
    onRender: function(conf){
        Wtf.emailTemplateThumbnail.superclass.onRender.call(this, conf);
        this.elDom = Wtf.get(this.renderTo).createChild({
            tag: "div",
            cls: "templateThumbCont templateThumbContainer"
        });
        this.templateImg = document.createElement("img");
        this.templateImg.width = "70";
        this.templateImg.height = "63";
        this.templateImg.alt = "No Image";
        this.templateImg.src = this.thumbnail;
        this.templateImg.setAttribute("wtf:qtip",this.tqtip);
        var nameDiv = document.createElement("div");
        var centerTag = document.createElement("center");
        this.nameSpan = document.createElement("span");
        nameDiv.appendChild(this.nameSpan);
        this.nameSpan.className = "templateThumbSpan";
        nameDiv.className = "templateNameDiv";
        this.templateImg.className = "templateThumbImg";
        this.nameSpan.innerHTML = this.tName;
        this.elDom.addListener("click", this.fireSelect, this);
        centerTag.appendChild(this.templateImg);
        this.elDom.appendChild(centerTag);
        this.elDom.appendChild(nameDiv);
    },
    setName: function(templatename){
        this.nameSpan.innerHTML = templatename;
    },
    setImage: function(src) {
        this.templateImg.src = src;
    },
    fireSelect: function(){
        this.fireEvent("templateSelected", this);
    },
    selectTemplate: function(){
        this.elDom.addClass("selectedTemplate");
        this.elDom.removeClass("templateThumbContainer");
    },
    deselectTemplate: function(){
        this.elDom.removeClass("selectedTemplate");
        this.elDom.addClass("templateThumbContainer");
    }
});
