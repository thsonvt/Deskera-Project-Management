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
Wtf.proj.taskPanel = function(config) {
    Wtf.apply(this,config);
    var cls = "custom";
    if(this.slackPanel)
        cls = "slacktask";
    if(this.archived || this.connstatus == 6 || this.connstatus == 8)
        cls += " archivedProjTask";
    Wtf.proj.taskPanel.superclass.constructor.call(this, {
        minWidth: this.TaskminWidth / 2,
        border: false,
        baseCls: cls,
        id: this.id,
        autoDestroy: true
    });
};

Wtf.extend(Wtf.proj.taskPanel, Wtf.Panel, {
    initComponent: function() {
        Wtf.proj.taskPanel.superclass.initComponent.call(this);
        this.addEvents({
            'panelMouseUp': true,
            'panelMouseDown': true
        });
        this.startDate = this.sdate;
        this.duration = this.Dur;
        this.endDate;
        this.actualDuartion = 0;
        this.actualstartDate;
        this.Xpos = 0;
        this.Ypos = 0;
        this.panelwidth = 0;
        this.TaskminWidth = 16;
        this.HrsPerDay = 8;
        this.progressInPixel = 0;
        this.successor = {};
        this.predecessor = {};
    },
    
    afterRender: function() {
        Wtf.proj.taskPanel.superclass.afterRender.call(this);
        this.bwrap.dom.removeChild(this.bwrap.dom.firstChild);
        var taskWidth = this.TaskminWidth;
        if(!this.archived && !(this.connstatus >= 6 && this.connstatus <= 8) && !this.slackPanel){
            var resizer = new Wtf.Resizable(this.id, {
                minWidth: taskWidth / 2,
                width: taskWidth,
                handles: 'e',
                autoDestroy: true,
                widthIncrement: taskWidth / 2
            });
            resizer.on("resize", this.ResizePanel, this);
            var taskElement = this.el;
            taskElement.addListener('mousedown', this.mymouseDown, this);
            taskElement.addListener('mouseup', this.mymouseUp, this);
            taskElement.addListener('mousemove', this.mymouseMove, this);
            taskElement.addListener('mouseover', this.mymouseOver, this);
            taskElement.addListener('mouseout', this.mymouseOut, this);
        }
        if(this.container.dom.offsetParent)
            this.ganttChartPanel = Wtf.getCmp(this.container.dom.offsetParent.id);
        else
            this.ganttChartPanel = Wtf.getCmp(this.ownerCt.id);
        var recdata = this.ganttChartPanel.containerPanel.editGrid.dstore.getAt(this.ganttChartPanel.containerPanel.editGrid.search(this.id.substr(5))).data;
        this.data = {
            items:[{h: WtfGlobal.getLocaleText('pm.common.task'), c: 'tName', d: recdata.taskname},
                {h: WtfGlobal.getLocaleText('lang.start.text'), c: 'tSDate', d: recdata.startdate.format(WtfGlobal.getOnlyDateFormat())},
                {h: WtfGlobal.getLocaleText('pm.common.duration'), c: 'tDuration', d: recdata.duration},
                {h: WtfGlobal.getLocaleText('lang.end.text'), c: 'tEDate', d: recdata.enddate.format(WtfGlobal.getOnlyDateFormat())}]
        };
        this.tpl = new Wtf.XTemplate('<span class = "taskheader">Task</span><hr><tpl for="items"><span class="qTipHeader">{h} : </span><span class="{c}">{d}</span><br></tpl>');
        this.on('hide', this.hidePanel);
        this.el.dom.setAttribute("wtf:qtip", this.tpl.apply(this.data));
    //this.el.dom.setAttribute("wtf:qwidth","230");
		this.projScale = this.ganttChartPanel.containerPanel.projScale;
    },

    updateQTip: function(recData){
        var _data = recData.data;
        this.data.items[0].d = _data.taskname;
        this.data.items[1].d = _data.startdate.format(WtfGlobal.getOnlyDateFormat());
        this.data.items[2].d = _data.duration;
        this.data.items[3].d = _data.enddate.format(WtfGlobal.getOnlyDateFormat());
        this.el.dom.removeAttribute("wtf:qtip");
        //        this.el.dom.removeAttribute("wtf:qwidth");
        this.el.dom.setAttribute("wtf:qtip", this.tpl.apply(this.data));
    //        this.el.dom.setAttribute("wtf:qwidth","230");
    },
 
    destroyPanel: function() {
        if(this.el)
            this.el.remove(true);
        var ganttPanel = this.ganttChartPanel;
        var tempLinkArray = ganttPanel.linkArrayObj;
        var pred = this.predecessor;
        for(var predecessorId in pred) {
            var linkKey = 'jg_' + pred[predecessorId] + '_' + this.id;
            ganttPanel.deleteLink(linkKey);
        }
        var succ = this.successor;
        for(var successorId in succ) {
            var linkKey = 'jg_' + this.id + '_' + succ[successorId];
            ganttPanel.deleteLink(linkKey);
        }
    },

    hidePanel: function() {
        var tempLinkArray = this.ganttChartPanel.linkArrayObj;
        var pred = this.predecessor;
        for(var predecessorId in pred) {
            var linkKey = 'jg_' + pred[predecessorId] + '_' + this.id;
            tempLinkArray[linkKey].clear();
        }
        var succ = this.successor;
        for(var successorId in succ) {
            var linkKey = 'jg_' + this.id + '_' + succ[successorId];
            tempLinkArray[linkKey].clear();
        }
    },

    removeResizer: function() {
        this.el.dom.lastChild.style.display = 'none';
        this.el.removeListener('resize',this.ResizePanel,this);
    },

    assignResizer: function() {
        this.el.dom.lastChild.style.display = 'block';
        this.el.addListener('resize',this.ResizePanel, this);
    },

    removeMouseMoveListener : function() {
        this.el.dom.lastChild.style.display = 'none';
        this.el.removeListener('mousemove',(this.mymouseMove));
        if(this.ganttChartPanel.showOverDue) {
            this.checkOverDue(true);
        }
    },

    assignMouseMoveListener: function() {
        this.el.dom.lastChild.style.display = 'block';
        this.el.addListener('mousemove',(this.mymouseMove),this);
    //        this.checkOverDue();
    },

    mymouseOver: function(event) {
        var taskId = this.id.substr(5);
        var ganttPanel = this.ganttChartPanel;
        ganttPanel.panelOver(taskId);
        if (ganttPanel.y2 != 0)
            this.el.addClass('newTaskPanelStyle');
    },

    mymouseOut: function(event) {
        var taskId = this.id.substr(5);
        var ganttPanel = this.ganttChartPanel;
        ganttPanel.panelOut(taskId);
        if (ganttPanel.y2 != 0 && ganttPanel.FromTaskId != this.id)
            this.el.removeClass('newTaskPanelStyle');
    },
    SetPanel_ProjScale: function(){
        if(this.projScale == 'week'){
            this.TaskminWidth = 5;
            this.HrsPerDay = 8;
        } else {
            this.TaskminWidth = 16;
            this.HrsPerDay = 8;
        }
    },

    setPanelWidth: function(record) {
        var localVal = record.data['duration']+'';
        if (record.data['ismilestone']) {
            if(this.projScale == 'day'){
                this.el.replaceClass('milestoneIcon', 'custom');
                this.Xpos += 5;
            } else {
                this.el.replaceClass('milestoneWeekIcon', 'custom');
            }
            this.assignResizer();
            document.getElementById(this.id).removeAttribute('TaskType');
            this.setPosition(this.Xpos, this.Ypos);
            var columnArray = [], valueArray = [];
            columnArray[0] = 'ismilestone';
            valueArray[0] = false;
            this.ganttChartPanel.containerPanel.editGrid.updateGridWithoutServerRequest(columnArray, valueArray, this.id);
        }
        this.setPanelWidthFromRecordDuration(record);
        //       this.updatePanel();
        this.updatePanelOnDataLoad();            //this.updatePanel();
        var ganttPanel = this.ganttChartPanel;
        var plannerGrid = this.ganttChartPanel.containerPanel.editGrid;
        var columnArray = [], valueArray = [];
        columnArray[0] = 'startdate';valueArray[0] = this.startDate;
        columnArray[1] = 'enddate';valueArray[1] = this.endDate;
        if(plannerGrid.clientServerChange == 'client') {
            plannerGrid.updateGridWithoutServerRequest(columnArray, valueArray, this.id);
            ganttPanel.updatedTaskIds[ganttPanel.updatedTaskIds.length] = (this.id).substr(5);
        } else 
            plannerGrid.UpdateGridRecord(columnArray, valueArray, this.id);
    },

    setPanelWidthFromRecordDuration : function(record) {
        if(this.projScale == 'day'){
            var localVal = record.data['duration']+'';
            var index = localVal.indexOf('h');
            var newWidth = this.panelwidth;
            if (index >= 0) {
                localVal = localVal.substr(0, index);
                if (localVal != '' && localVal != '0') {
                    this.panelwidth = (parseFloat(localVal)/this.HrsPerDay) * this.TaskminWidth;
                }
            } else {
                localVal = new String(record.data['duration']);
                index = localVal.indexOf('d');
                if (index != -1)
                    localVal = localVal.substr(0, index);
                if (localVal != '' && parseFloat(localVal) != 0) {
                    this.panelwidth = parseInt(parseFloat(localVal) * this.TaskminWidth);
                }
            }
            if (parseFloat(localVal) == 0){
                this.el.replaceClass('custom', 'milestoneIcon');
                this.removeResizer();
                this.panelwidth = 10;
                this.Xpos -=  5;
                this.setPosition(this.Xpos, this.Ypos);
                document.getElementById('Panel' + record.data['taskid']).setAttribute('TaskType', 'milestone');
                var columnArray = [],valueArray = [];
                columnArray[0] = 'ismilestone';
                valueArray[0] = true;
                this.ganttChartPanel.containerPanel.editGrid.updateGridWithoutServerRequest(columnArray, valueArray, this.id);
            }
            this.el.dom.style.width = this.panelwidth + "px";
        } else {
            this.SetPanel_ProjScale();
            var newWidth = this.panelwidth;
            localVal = new String(record.data['duration']);
            index = localVal.indexOf('d');
            if (index != -1)
                localVal = localVal.substr(0, index);
            index = localVal.indexOf('h');
            if (index != -1){
                localVal = localVal.substr(0, index);
                if (localVal != '' && localVal != '0') {
                    this.panelwidth = (parseFloat(localVal)/this.HrsPerDay) * this.TaskminWidth;
                }
            } else if (localVal != '' && parseFloat(localVal) != 0) {
                this.panelwidth = parseInt(parseFloat(localVal) * this.TaskminWidth);
            }
            if (parseFloat(localVal) == 0){
                this.el.replaceClass('custom', 'milestoneWeekIcon');
                this.removeResizer();
                this.panelwidth = 5;
                this.Xpos =  this.Xpos;
                this.setPosition(this.Xpos, this.Ypos);
                document.getElementById('Panel' + record.data['taskid']).setAttribute('TaskType', 'milestone');
                var columnArray = [],valueArray = [];
                columnArray[0] = 'ismilestone';
                valueArray[0] = true;
                this.ganttChartPanel.containerPanel.editGrid.updateGridWithoutServerRequest(columnArray, valueArray, this.id);
            }
            this.el.dom.style.width = this.panelwidth + "px";
        }
    },
    setPanelPosition : function(sdate, PosY){
        this.SetPanel_ProjScale();
        var diff = this.timeDifference(new Date(sdate),this.ganttChartPanel.StartDate);
        this.Xpos = diff*this.TaskminWidth; 
        this.setPosition(this.Xpos, PosY);
        this.Ypos = PosY;
        this.startDate = sdate;
        this.actualstartDate = sdate;
        this.setProxyPanelPosition(this.id);
    },
    
    setPanelPositionWidthOnDataLoad : function(record, PosY) {
        this.SetPanel_ProjScale();
        this.startDate = new Date(record.data['startdate']);
        this.actualstartDate = record.data['actstartdate'];
        this.duration = record.data['duration'];
        var diff = this.timeDifference(this.startDate, this.ganttChartPanel.StartDate);    
        this.Xpos = diff * this.TaskminWidth;
        this.setPosition(this.Xpos, PosY);
        this.Ypos = PosY;
        if (this.el.hasClass('milestoneIcon') || this.el.hasClass('milestoneWeekIcon')) {
            if(this.el.hasClass('milestoneIcon'))
                this.el.replaceClass('milestoneIcon', 'custom');
            else
                this.el.replaceClass('milestoneWeekIcon', 'custom');
            this.assignResizer();
            document.getElementById(this.id).removeAttribute('TaskType');
            var columnArray = [], valueArray = [];
            columnArray[0] = 'ismilestone';
            valueArray[0] = false;
            this.ganttChartPanel.containerPanel.editGrid.updateGridWithoutServerRequest(columnArray, valueArray, this.id);
        }
        this.setPanelWidthFromRecordDuration(record);
        this.updatePanelOnDataLoad();
    },
    
    setSlackPanelPositionWidth: function(record, tpanel, slack) {
        this.SetPanel_ProjScale();
        this.startDate = new Date(record.data['enddate']);
        this.actualstartDate = record.data['actstartdate'];
        this.duration = slack.get('expected');
        var diff = this.timeDifference(this.startDate, this.ganttChartPanel.StartDate);
        this.Xpos = (diff * this.TaskminWidth);
        this.setPosition(this.Xpos, tpanel.Ypos);
        this.Ypos = tpanel.Ypos;
        if (this.el.hasClass('milestoneIcon') || this.el.hasClass('milestoneWeekIcon')) {
            if(this.el.hasClass('milestoneIcon'))
                this.el.replaceClass('milestoneIcon', 'custom');
            else
                this.el.replaceClass('milestoneWeekIcon', 'custom');
            this.assignResizer();
            document.getElementById(this.id).removeAttribute('TaskType');
        }
        var localVal = slack.get('expected')+'';
        var index = localVal.indexOf('h');
        var newWidth = this.panelwidth;
        if (index >= 0) {
            localVal = localVal.substr(0, index);
            if (localVal != '' && localVal != '0') {
                this.panelwidth = (parseFloat(localVal)/this.HrsPerDay) * this.TaskminWidth;
            }
        } else {
            localVal = new String(slack.get('expected'));
            index = localVal.indexOf('d');
            if (index != -1)
                localVal = localVal.substr(0, index);
            if (localVal != '' && parseFloat(localVal) != 0) {
                this.panelwidth = parseInt(parseFloat(localVal) * this.TaskminWidth);
            }
        }
        this.el.dom.style.width = this.panelwidth + "px";
        new Wtf.Panel({
            renderTo: this.id,
            id: 'SlackDur_' + tpanel.id
        });
        var obj = new jsGraphics('SlackDur_' + tpanel.id);
        this.ganttChartPanel.slackDurObj['SlackDur_' + tpanel.id] = obj;
        var resStr = slack.get('slack') + ' days';
        obj.setFont("tahoma,verdana,arial,geneva,helvetica,sans-serif", "10px", Font.BOLD);
        obj.drawString(resStr, this.panelwidth + 7, 0);
        if(!this.hidden){
            obj.paint();
        }
    },

    UpdateEndDateGrid: function(duration) {
        this.SetPanel_ProjScale();
        var stdate = new Date(this.startDate);
        var enddate = new Date(this.startDate);
        var endPos = this.el.getRight(true);
        var taskWidth = this.TaskminWidth;
        this.panelwidth = parseInt(duration * taskWidth);
        enddate = this.ganttChartPanel.StartDate.add(Date.DAY, parseInt(endPos / taskWidth) - 1);
        if(parseFloat(endPos / taskWidth) > parseInt(endPos / taskWidth))
            enddate = enddate.add(Date.DAY, 1);
        
        var val = this.findEndDateOnResize(enddate,0);
        if(val != 0) {
            enddate = this.endDate;
            duration += val;
            if(duration<=0) {
                enddate = stdate;
                duration = 1;
            } else if(parseFloat(duration)>parseInt(duration)){
                duration += (parseFloat(duration)-parseInt(duration));
            }
            this.el.dom.style.width = (duration * taskWidth) + "px";
            this.panelwidth = duration * taskWidth;
        }
        /*if(enddate.format('w') == 6 || enddate.format('w') == 0) {
            if(enddate.format('w') == 6) {
                enddate = enddate.add(Date.DAY, -1);
                duration -= 1;
                if(duration<=0) {
                  enddate = stdate;  
                  duration = 1;
                }
                this.el.dom.style.width = (duration * taskWidth) + "px";
                this.panelwidth = duration * taskWidth;
            } else if(enddate.format('w') == 0) {
                enddate = enddate.add(Date.DAY, -2);
                duration -= 2;
                if(duration<=0) {
                  enddate = stdate;  
                  duration = 1;
                }
                this.el.dom.style.width = (duration * taskWidth) + "px";
                this.panelwidth = duration * taskWidth;
            }
        }*/
        var nonWorkDays = this.calculatenonworkingDays(stdate, enddate);
        if(nonWorkDays != 0)
            duration = duration - nonWorkDays;
        if(this.ganttChartPanel.NonworkWeekDays.join().indexOf(stdate.format('w'))!=-1 || this.ganttChartPanel.HolidaysList.join().indexOf(stdate.format('d/m/Y'))!=-1)
            duration = parseFloat(duration, 10) + 1;
        this.endDate = enddate;
        this.duration = duration;
        var percentVal =0 ;
//        if(this.progressInPixel!=0) {
//            percentVal = Math.round(this.progressInPixel/(taskWidth * duration)*100);
//            if(percentVal>100) {
//                this.progressInPixel = this.panelwidth;
//                percentVal = 100;
//            }
//        }
        var columnArray = [], valueArray = [];
        columnArray[0] = 'enddate'; valueArray[0] = enddate;
        columnArray[1] = 'duration'; valueArray[1] = duration;
        columnArray[2] = 'actualduration'; valueArray[2] = duration;
//        if(percentVal!=0)
//            columnArray[3] = 'percentcomplete'; valueArray[3] = percentVal;
        this.ganttChartPanel.containerPanel.editGrid.UpdateGridRecord(columnArray, valueArray, this.id);
        this.ganttChartPanel.mysubtask(this.id);
    },
    
    findEndDateOnResize : function(enddate,dur) { // initial values of dur = 0;
        var flag = true;
        while(flag) {
            if(this.ganttChartPanel.NonworkWeekDays.join().indexOf(enddate.format('w'))!=-1 || this.ganttChartPanel.HolidaysList.join().indexOf(enddate.format('d/m/Y'))!=-1) {
                enddate = enddate.add(Date.DAY, -1);
                dur -=1;
            //this.findEndDateOnResize(enddate,dur);
            } else { 
                this.endDate = enddate;
                flag = false;
            }
        }
        return dur;
    },

    ResizePanel: function(event) {
        this.SetPanel_ProjScale();
        this.duration = this.getSize().width / this.TaskminWidth;
        if(this.panelwidth != parseInt(this.duration * this.TaskminWidth)) {
            this.UpdateEndDateGrid(this.duration);
            var ganttPanel = this.ganttChartPanel;
            ganttPanel.RecursionFunToTraceLink(this.id);
            if(ganttPanel.updatedTaskIds.length > 0)
                ganttPanel.containerPanel.editGrid.updatedTasks_ServerRequest(ganttPanel.updatedTaskIds);
            if(ganttPanel.updatedParentTaskIds.length > 0)
                ganttPanel.containerPanel.editGrid.updatedParentTasks_ServerRequest(ganttPanel.updatedParentTaskIds)
            if(ganttPanel.showOverDue) {
                this.checkOverDue(true);
            }
        }
    },

    mymouseDown: function(event) {
        this.ganttChartPanel.panelMouseDown(event, this);
        this.addClass('newTaskPanelStyle');
    },

    mymouseUp: function(event) {
        this.removeClass('newTaskPanelStyle');
        var ganttPanel = this.ganttChartPanel;
        if(Wtf.get('Panel_0') != null) {
            if(ganttPanel.FromTaskId) {
                Wtf.getCmp(ganttPanel.FromTaskId).setProxyPanelPosition('Panel_0');
                this.UpdateStEndForNonWorking(ganttPanel.FromTaskId); // function called to set actualstartdate in db
            }    
            ganttPanel.remove(Wtf.getCmp('Panel_0'), true);
        }
        ganttPanel.el.replaceClass('chartPanelNewCursor', 'chartPanelOldCursor');
        ganttPanel.panelMouseUp(event, this);
    },

    mymouseMove: function(event) {
        this.SetPanel_ProjScale();
        var xVal = event.getPageX();
        var ganttPanel = this.ganttChartPanel;
        if (ganttPanel.oldx != xVal && ganttPanel.oldx > 0) {
            var moveX = xVal - ganttPanel.oldx;
            var ElChartPos = this.getPosition(true);
            if (/*(ElChartPos[0] + moveX) > 5 && */this.id == ganttPanel.FromTaskId && ganttPanel.x2 == 0 && ganttPanel.y2 == 0) {
                if (Wtf.get('Panel_0') == null) {
                    var TaskPanel = new Wtf.Panel({
                        minWidth: this.panelwidth,
                        width: this.panelwidth,
                        border: true,
                        baseCls: 'TempTask',
                        id: 'Panel_0'
                    });
                    ganttPanel.add(TaskPanel);
                    ganttPanel.doLayout();
                    var taskPanel = Wtf.get(TaskPanel.getId());
                    taskPanel.addListener('mousemove', this.TempmouseMove, this);
                    taskPanel.addListener('mouseup', this.TempmouseUp, this);
                    TaskPanel.setPosition((ElChartPos[0] + moveX), ElChartPos[1]);
                }
                ganttPanel.oldx = xVal;
            } else if (this.id != ganttPanel.FromTaskId && Wtf.get('Panel_0') != null) {
                var fromTaskPanel = Wtf.getCmp(ganttPanel.FromTaskId);
                ganttPanel.x1 = (fromTaskPanel.getPosition(true))[0] + (fromTaskPanel.panelwidth / 2);
                ganttPanel.y1 = (fromTaskPanel.getPosition(true))[1];
                Wtf.get(ganttPanel.id).replaceClass('chartPanelNewCursor', 'chartPanelOldCursor');
                ganttPanel.remove(Wtf.getCmp('Panel_0'),true);
            }
        }
    },

    TempmouseMove: function(event) {
        var ganttPanel = this.ganttChartPanel;
        if (ganttPanel.oldx != event.getPageX() && ganttPanel.oldx > 0){
            moveX = event.getPageX() - ganttPanel.oldx;
            var proxyPanel = Wtf.getCmp('Panel_0');
            var ElChartPos = proxyPanel.getPosition(true);
            x1 = ElChartPos[0] + moveX;
            if (x1 > 0)
                proxyPanel.setPosition(x1, ElChartPos[1]);
            else
                proxyPanel.setPosition(0, ElChartPos[1]);
            ganttPanel.oldx = event.getPageX();
        }
    },

    TempmouseUp: function(event) {
        this.removeClass('newTaskPanelStyle');
        this.setProxyPanelPosition('Panel_0');
        var ganttPanel = this.ganttChartPanel;
        if (Wtf.get('Panel_0') != null)
            ganttPanel.remove(Wtf.getCmp('Panel_0'),true);
        ganttPanel.oldx = ganttPanel.x1 = ganttPanel.y1 = ganttPanel.x2 = ganttPanel.y2 = 0;
        Wtf.get(ganttPanel.id).replaceClass('chartPanelNewCursor', 'chartPanelOldCursor');
        this.UpdateStEndForNonWorking(this.id); // function called to set actualstartdate in db
    },

    setProxyPanelPosition: function(TaskId) {
        var arr = Wtf.getCmp(TaskId).getPosition(true);
        var Xposition = arr[0];
        var PanelXY = this.getPosition(true);
        var ganttPanel = this.ganttChartPanel;
        var stdate = new Date(ganttPanel.StartDate);
        var count = 0;
        var TotalLinks = 0;
        if(TaskId.indexOf('_') < 0 || (PanelXY[0] != (parseInt((Xposition / this.TaskminWidth), 10) * this.TaskminWidth))) {
            if(this.el.dom.getAttribute('TaskType') == null || this.el.dom.getAttribute('TaskType') == "milestone")
                Xposition = parseInt((Xposition / this.TaskminWidth), 10) * this.TaskminWidth;
            else
                Xposition = parseInt(((Xposition + 5) / this.TaskminWidth), 10) * this.TaskminWidth - 5;
            stdate = stdate.add(Date.DAY, Xposition / this.TaskminWidth);
            if(ganttPanel.containerPanel.checkForProjStartDate(stdate)){
                msgBoxShow(178, 1);
                Xposition = PanelXY[0];
                return;
            } else {
                var predecessor = this.predecessor;
                for(var pred in predecessor) {
                    var predObj = Wtf.getCmp(pred);
                    var tempx = 0;
                    if((this.el.dom.getAttribute('TaskType') == null || this.el.dom.getAttribute('TaskType') == "milestone") && (predObj.el.dom.getAttribute('TaskType') != "milestone"))
                        tempx = predObj.Xpos + predObj.panelwidth;
                    else
                        tempx = predObj.el.dom.offsetLeft + predObj.panelwidth - 5;
                    if((predObj.panelwidth >= this.TaskminWidth) || (this.el.dom.getAttribute('TaskType') == "milestone" || predObj.el.dom.getAttribute('TaskType') == "milestone")) {
                        if (tempx > Xposition)
                            count++;
                    }
                    TotalLinks++;
                }
            }
            if(count == 1 && TotalLinks == 1)
                this.DialogBoxForNonWorking(this.id, stdate, Xposition, 'DeletedLink');
            else if(count >= 1 && TotalLinks > 1) { // if totallinks are more than one and current position less than it's predecessors position then cancel operation
                this.setPosition(PanelXY[0], PanelXY[1]);
                this.Xpos = PanelXY[0];
                this.updatePanel();
                ganttPanel.mysubtask(this.id);
                ganttPanel.updatedTaskIds = []; // initialize array;
                ganttPanel.updatedParentTaskIds = []; // initialize array;
            } else {
                if((this.el.dom.getAttribute('TaskType') == null || this.el.dom.getAttribute('TaskType') == "milestone") && ((this.ganttChartPanel.NonworkWeekDays.join().indexOf(stdate.format('w'))!=-1) || (this.ganttChartPanel.HolidaysList.join().indexOf(stdate.format('d/m/Y'))!=-1)))
                    this.DialogBoxForNonWorking(this.id, stdate, Xposition, 'NonWorkingDay');
                else if((this.el.dom.getAttribute('TaskType') != null && this.el.dom.getAttribute('TaskType') != "milestone") && (this.ganttChartPanel.NonworkWeekDays.join().indexOf(stdate.add(Date.DAY, 1).format('w'))!=-1 || this.ganttChartPanel.HolidaysList.join().indexOf(stdate.add(Date.DAY,1).format('d/m/Y'))!=-1))
                    this.DialogBoxForNonWorking(this.id, stdate.add(Date.DAY, 1), Xposition, 'NonWorkingDay');
                else if((this.el.dom.getAttribute('TaskType') == "milestone") && (this.ganttChartPanel.NonworkWeekDays.join().indexOf(stdate.format('w'))!=-1 || this.ganttChartPanel.HolidaysList.join().indexOf(stdate.format('d/m/Y'))!=-1))
                    this.DialogBoxForNonWorking(this.id, stdate.add(Date.DAY, 1), Xposition, 'NonWorkingDay');
                else {
                    if(this.el.dom.getAttribute('TaskType') == "milestone" && this.projScale == 'day'){
                        this.setPosition(Xposition - 5, arr[1]);
                        this.Xpos = Xposition - 5;
                    } else {
                        this.setPosition(Xposition, arr[1]);
                        this.Xpos = Xposition;
                    }
                    var columnArray = [];
                    var valueArray = [];
                    columnArray[0] = 'actstartdate';
                    valueArray[0] = this.startDate;
                    var plannerGrid = ganttPanel.containerPanel.editGrid;
                    plannerGrid.updateGridWithoutServerRequest(columnArray, valueArray, this.id);
                    this.actualstartDate = this.startDate;
                    this.updatePanel();
                    ganttPanel.mysubtask(this.id);
                    ganttPanel.RecursionFunToTraceLink(this.id);
                    if(ganttPanel.updatedTaskIds.length > 0)
                        plannerGrid.updatedTasks_ServerRequest(ganttPanel.updatedTaskIds);
                    if(ganttPanel.updatedParentTaskIds.length > 0)
                        plannerGrid.updatedParentTasks_ServerRequest(ganttPanel.updatedParentTaskIds)
                    }
                }
            }
    },

    DialogBoxForNonWorking: function(TaskId, userDate, tempXPostion, type) {
        var stringArray = [];
        stringArray[1] = WtfGlobal.getLocaleText("lang.youcan.text");
        var dt = new Date(userDate);
        var dur = this.findWorkingStEndDate(dt,0);
        if(dur!=0) {
            dt = dt.add(Date.DAY,dur);
        }    
        /*if (dt.format('w') == 6)
        	dt = dt.add(Date.DAY, 2);
        else if (dt.format('w') == 0)
        	dt = dt.add(Date.DAY, 1);*/
        if (type == 'NonWorkingDay') {
            stringArray[0] = WtfGlobal.getLocaleText({key:"pm.project.plan.nonworking.movedto",params:userDate.format(WtfGlobal.getOnlyDateFormat())});
            stringArray[2] = WtfGlobal.getLocaleText({key:"pm.project.plan.nonworking.movedto.option1",params:dt.format(WtfGlobal.getOnlyDateFormat())});
            stringArray[3] = WtfGlobal.getLocaleText({key:"pm.project.plan.nonworking.movedto.option2",params:userDate.format(WtfGlobal.getOnlyDateFormat())});
        } else if (userDate.format('w') == 6 || userDate.format('w') == 0) {
            stringArray[0] = WtfGlobal.getLocaleText({key:"pm.project.plan.nonworking.linkedmovedto",params:userDate.format('D M j, Y')});
            stringArray[2] = WtfGlobal.getLocaleText("pm.project.plan.nonworking.linkedmovedo.option1");
            stringArray[3] = WtfGlobal.getLocaleText({key:"pm.project.plan.nonworking.linkedmovedto.option2",params:[dt.format(WtfGlobal.getOnlyDateFormat()), userDate.format(WtfGlobal.getOnlyDateFormat())]});
        } else {
            stringArray[0] = WtfGlobal.getLocaleText("pm.project.plan.nonworking.linked");
            stringArray[2] = WtfGlobal.getLocaleText("pm.project.plan.nonworking.linkedmovedto.option1");
            stringArray[3] = WtfGlobal.getLocaleText({key:"pm.project.plan.nonworking.linked.option2",params:dt.format(WtfGlobal.getOnlyDateFormat())});
        }
        this.ganttChartPanel.fireEvent('showWindowForUserChoice', stringArray, tempXPostion, TaskId, type);
    },

    calculateEndDate : function (duration) {
        this.SetPanel_ProjScale();
        var enddate = new Date();
        var index = new String(this.duration).indexOf('h');
        if(this.projScale == 'day' && index >= 0){
            duration = parseFloat(duration.substr(0, index))/this.HrsPerDay;
            this.panelwidth = parseInt(duration * this.TaskminWidth);
            this.el.dom.style.width = this.panelwidth + "px";
            duration = parseFloat(duration);
            var endPos = this.Xpos + this.panelwidth;
            enddate = (this.ganttChartPanel.StartDate).add(Date.DAY, parseInt(endPos / this.TaskminWidth) - 1);
            if (parseFloat(endPos / this.TaskminWidth) > parseInt(endPos / this.TaskminWidth))
                enddate = enddate.add(Date.DAY, 1);
        } else {//if scale week or (scale day and index !>= 0)
            if ((index = (duration + '').indexOf('d')) >= 0) {
                duration = duration.substr(0, index);
            } else if((index = (duration + '').indexOf('h')) >= 0){
                duration = parseFloat(duration.substr(0, index))/this.HrsPerDay;
            }
            duration = parseFloat(duration);
            this.panelwidth = parseInt(duration * this.TaskminWidth);
            this.el.dom.style.width = this.panelwidth + "px";
            var endPos = this.Xpos + this.panelwidth;
            enddate = (this.ganttChartPanel.StartDate).add(Date.DAY, parseInt(endPos / this.TaskminWidth) - 1);
            if (parseFloat(endPos / this.TaskminWidth) > parseInt(endPos / this.TaskminWidth))
                enddate = enddate.add(Date.DAY, 1);
        }
        this.duration = duration;
        return enddate;
    },
    
    forWorkingDates : function(stdate,enddate){
        this.SetPanel_ProjScale();
        var nonWorkDays = this.NonworkingDaysBetDates(stdate, enddate);
        if (nonWorkDays != 0) {
            /*if (nonWorkDays % 2 == 1) 
                nonWorkDays = nonWorkDays + 1;*/
            this.panelwidth = this.panelwidth + (nonWorkDays * this.TaskminWidth);
            this.el.dom.style.width = this.panelwidth + "px";
            enddate = enddate.add(Date.DAY, nonWorkDays);
        }
        var dur = this.findWorkingStEndDate(enddate,0);
        if(dur!=0) {
            enddate = enddate.add(Date.DAY,dur);
            this.panelwidth = this.panelwidth + (dur * this.TaskminWidth);
            this.el.dom.style.width = this.panelwidth + "px";
        }
        this.endDate = enddate;
    },
    
    forNonWorkingdates : function (stdate, enddate){
        this.SetPanel_ProjScale();
        /*if (stdate.format('w') == 6) {
            stdate= stdate.add(Date.DAY,2);
            ActualDuration = 1;
        } else 
            stdate = stdate.add(Date.DAY, 1);*/
        var ActualDuration = this.sdateForNonWorkCal(stdate,-1);
        if(ActualDuration !=-1)
            stdate = stdate.add(Date.DAY, ActualDuration + 1);
        else 
            ActualDuration = 0;
        if(this.panelwidth <= this.TaskminWidth)
            ActualDuration = 0;
        var nonworkingdays = this.NonworkingDaysBetDates(stdate, enddate);
        ActualDuration = ActualDuration + nonworkingdays;
        if(ActualDuration != 0) {
            this.panelwidth = this.panelwidth + (ActualDuration * this.TaskminWidth);
            this.el.dom.style.width = this.panelwidth + "px";
            enddate = enddate.add(Date.DAY, ActualDuration);
        }
        if(this.panelwidth < this.TaskminWidth) {
            enddate = this.startDate;
        }
        if(this.duration > 1) {
            var dur = this.findWorkingStEndDate(enddate,0);
            if(dur!=0) {
                enddate = enddate.add(Date.DAY,dur);
                this.panelwidth = this.panelwidth + (dur * this.TaskminWidth);
                this.el.dom.style.width = this.panelwidth + "px";
            }
        }
        /*if (enddate.format('w') == 6 && this.duration > 1) {
            enddate = enddate.add(Date.DAY, 2);
            this.panelwidth = this.panelwidth + (2 * this.TaskminWidth);
            this.el.dom.style.width = this.panelwidth + "px";
        } else if (enddate.format('w') == 0 && this.duration > 1)  {
            enddate = enddate.add(Date.DAY, 1);
            this.panelwidth = this.panelwidth + this.TaskminWidth;
            this.el.dom.style.width = this.panelwidth + "px";
        }
        if(this.panelwidth < this.TaskminWidth) {
            enddate = this.startDate;
        }*/
        this.endDate = enddate;
    },
    
    findWorkingStEndDate : function(dateVal,dur) { // initial values of dur = 0;
        /*if(this.NonworkWeekDays.join().indexOf(dateVal.format('w'))!=-1) {
            dateVal = dateVal.add(Date.DAY, 1);
            dur +=1;
            dur +=this.findWorkingStEndDate(dateVal,dur);
        } */
        var flag = true;
        while(flag) {
            if(this.ganttChartPanel.NonworkWeekDays.join().indexOf(dateVal.format('w'))!=-1 || (this.ganttChartPanel.HolidaysList.join().indexOf(dateVal.format('d/m/Y'))!=-1)) {
                dateVal = dateVal.add(Date.DAY, 1);
                dur +=1;
            //this.findWorkingStEndDate(dateVal,dur);
            }
            else 
                flag = false;
        }
        return dur;
    },
    
    sdateForNonWorkCal : function(stdate,nonworkcnt) {
        var flag = true;
        while(flag) {
            if(this.ganttChartPanel.NonworkWeekDays.join().indexOf(stdate.format('w'))!=-1 || (this.ganttChartPanel.HolidaysList.join().indexOf(stdate.format('d/m/Y'))!=-1)) {
                stdate= stdate.add(Date.DAY,1);
                nonworkcnt += 1;
            //this.sdateForNonWorkCal(stdate,nonworkcnt);
            } else 
                flag = false;
        }
        return nonworkcnt;
    },
    
    UpdateStEndForNonWorking: function(TaskId){
        this.SetPanel_ProjScale();
        var TaskXY = Wtf.getCmp(TaskId).getPosition(true);
        var ganttPanel = this.ganttChartPanel;
        var stdate = new Date(ganttPanel.StartDate);
        var enddate = new Date();
        var plannerGrid = this.ganttChartPanel.containerPanel.editGrid;
        if (document.getElementById(TaskId).getAttribute('TaskType') == null) {
            stdate = stdate.add(Date.DAY, TaskXY[0] / this.TaskminWidth);
            if(ganttPanel.containerPanel.checkForProjStartDate(stdate)){
                msgBoxShow(178, 1);
                return
            } else {
                this.startDate = stdate;
                enddate = this.calculateEndDate(this.duration);
                this.forNonWorkingdates(stdate,enddate);
            }
        } else {
            if(this.projScale == 'day')
                stdate = ganttPanel.StartDate.add(Date.DAY, (TaskXY[0] + Wtf.get(TaskId).getWidth()) / this.TaskminWidth);
            else
                stdate = ganttPanel.StartDate.add(Date.DAY, TaskXY[0] / this.TaskminWidth);
            if(ganttPanel.containerPanel.checkForProjStartDate(stdate)){
                msgBoxShow(178, 1);
                return;
            } else {
                enddate = stdate;
                this.startDate = stdate;
                this.endDate = enddate;
            }
        }
        this.actualstartDate = this.startDate;
        var columnArray = [], valueArray = [];
        columnArray[0] = 'startdate';valueArray[0] = this.startDate;
        columnArray[1] = 'enddate';valueArray[1] = this.endDate;
        columnArray[2] = 'actstartdate';valueArray[2] = this.startDate;
        columnArray[3] = 'actduration';valueArray[3] = this.duration;
        ganttPanel.containerPanel.editGrid.UpdateGridRecord(columnArray, valueArray, this.id);
    },
    
    updatePanel: function() {
        var percentVal =0;
        var ganttPanel = this.ganttChartPanel;
        var stdate = new Date(ganttPanel.StartDate);
        var enddate = new Date();
        stdate = stdate.add(Date.DAY, this.Xpos / this.TaskminWidth);
        if (document.getElementById(this.id).getAttribute('TaskType') == null) {
            /*if (stdate.format('w') == 6) {
                stdate = stdate.add(Date.DAY, 2);
                this.Xpos = (this.Xpos + this.TaskminWidth * 2); 
                this.setPosition(this.Xpos, this.Ypos);
            } else if (stdate.format('w') == 0) {
                stdate = stdate.add(Date.DAY, 1);
                this.Xpos = this.Xpos + this.TaskminWidth; 
                this.setPosition(this.Xpos, this.Ypos);
            }*/
            var dur = this.findWorkingStEndDate(stdate,0);
            if(dur!=0) {
                stdate = stdate.add(Date.DAY, dur);
                this.Xpos = this.Xpos + (dur * this.TaskminWidth);
                this.setPosition(this.Xpos, this.Ypos);
            }
            enddate = this.calculateEndDate(this.duration);

            if(this.progressInPixel!=0) {
                percentVal = Math.round(this.progressInPixel / this.panelwidth * 100);
                if(percentVal>100) {
                    this.progressInPixel = this.panelwidth;
                    percentVal = 100;
                }    
            }
            this.forWorkingDates(stdate, enddate);
            enddate = this.endDate;
        } else {
            if(this.projScale == 'day')
                stdate = (ganttPanel.StartDate).add(Date.DAY, (this.Xpos + this.panelwidth) / this.TaskminWidth);
            else
                stdate = (ganttPanel.StartDate).add(Date.DAY, (this.Xpos / this.TaskminWidth));
            /*if (stdate.format('w') == 6) {
                stdate = stdate.add(Date.DAY, 2);
                this.Xpos += (this.TaskminWidth * 2); 
                this.setPosition(this.Xpos, this.Ypos);
            } else if (stdate.format('w') == 0) {
                stdate = stdate.add(Date.DAY, 1);
                this.Xpos += this.TaskminWidth; 
                this.setPosition(this.Xpos, this.Ypos);
            }*/
//            var dur = this.findWorkingStEndDate(stdate,0);
//            if(dur!=0) {
//                stdate = stdate.add(Date.DAY, dur);
//                this.Xpos = this.Xpos + (dur * this.TaskminWidth);
//                this.setPosition(this.Xpos, this.Ypos);
//            }
            if(this.progressInPixel!=0) {
                this.progressInPixel = 0;
                percentVal = 100;
            }
            enddate = stdate;
        }
        this.startDate = stdate;
        this.endDate = enddate;
        var columnArray = [], valueArray = [];
        columnArray[0] = 'startdate';valueArray[0] = stdate;
        columnArray[1] = 'enddate';valueArray[1] = enddate;
        if(percentVal!=0)
            columnArray[2] = 'percentcomplete';valueArray[2] = percentVal;
        var plannerGrid = ganttPanel.containerPanel.editGrid;
        /*if(!flag)
            plannerGrid.UpdateGridRecord(columnArray, valueArray, this.id);*/
        if(plannerGrid.clientServerChange == 'client') {
            plannerGrid.updateGridWithoutServerRequest(columnArray, valueArray, this.id);
            ganttPanel.updatedTaskIds[ganttPanel.updatedTaskIds.length] = (this.id).substr(5);
        } else 
            plannerGrid.UpdateGridRecord(columnArray, valueArray, this.id);
        if(ganttPanel.showOverDue) {
            this.checkOverDue(true);
        }
    },

    updatePanelOnDataLoad :function() {
        var ganttPanel = this.ganttChartPanel;
        var stdate = new Date(ganttPanel.StartDate);
        var enddate = new Date();
        stdate = stdate.add(Date.DAY, this.Xpos / this.TaskminWidth);
        if (document.getElementById(this.id).getAttribute('TaskType') == null) {
            enddate = this.calculateEndDate(this.duration);
            if(this.ganttChartPanel.NonworkWeekDays.join().indexOf(stdate.format('w')) != -1 || (this.ganttChartPanel.HolidaysList.join().indexOf(stdate.format('d/m/Y'))!= -1)) {
                this.forNonWorkingdates(stdate,enddate);
                enddate = this.endDate;
            } else {
                this.forWorkingDates(stdate, enddate);
                enddate = this.endDate;
            }
        } else {    // for milestone
            if(this.projScale == 'day')
                stdate = (ganttPanel.StartDate).add(Date.DAY, (this.Xpos + Wtf.getCmp(this.id).panelwidth) / this.TaskminWidth);
            else
                stdate = (ganttPanel.StartDate).add(Date.DAY, (this.Xpos / this.TaskminWidth));
            /*if (stdate.format('w') == 6) {
                stdate = stdate.add(Date.DAY, 2);
                this.Xpos += (this.TaskminWidth * 2);
                this.setPosition(this.Xpos, this.Ypos);
            } else if (stdate.format('w') == 0) {
                stdate = stdate.add(Date.DAY, 1);
                this.Xpos += this.TaskminWidth;
                this.setPosition(this.Xpos, this.Ypos);
            }*/
            enddate = stdate;
        }
        this.startDate = stdate;
        this.endDate = enddate;
        if(ganttPanel.showOverDue) {
            this.checkOverDue(true);
        }
    },

    calculatenonworkingDays : function(stdate,enddate) {
        var StDate = new Date();
        var EndDate = new Date();
        if (typeof stdate == 'string') {
            var sd = Date.parseDate(stdate, 'd/m/Y');
            var ed = Date.parseDate(enddate, 'd/m/Y');
            StDate = sd.clearTime(false);
            EndDate = ed.clearTime(false);
        } else {
            StDate = stdate.clearTime(false);
            EndDate = enddate.clearTime(false);
        }
        var actualStDate = StDate;
        EndDate = EndDate.add(Date.DAY, 1);
        var NumWeeks = parseInt(this.timeDifference(EndDate, StDate) / 7, 10);
        var NonWorkingDaysBetween = NumWeeks * this.ganttChartPanel.NonworkWeekDays.length;
        StDate = StDate.add(Date.DAY, (NumWeeks * 7));
        var diff = this.timeDifference(EndDate, StDate);
        for (i = 0; i < diff; i++) {
            //if (StDate.format('w') == 0 || StDate.format('w') == 6)
            if(this.ganttChartPanel.NonworkWeekDays.join().indexOf(StDate.format('w'))>=0)
                NonWorkingDaysBetween += 1;
            StDate = StDate.add(Date.DAY, 1);
        }
        return NonWorkingDaysBetween + this.NumCompanyHoliday(actualStDate,EndDate.add(Date.DAY,-1));
    },

    NonworkingDaysBetDates: function(stdate, enddate) {
        var userDuration = this.timeDifference(enddate, stdate)+1;
        var NonWorkingdays = 0 ;
        /*NonWorkingdays = this.calculatenonworkingDays(stdate, enddate);
        if (NonWorkingdays != 0) {
            var duration = parseInt(userDuration, 10);
            duration = (duration + NonWorkingdays);
            enddate = stdate.add(Date.DAY, duration - 1);
            NonWorkingdays = this.calculatenonworkingDays(stdate, enddate);
            if (NonWorkingdays != 0) {
                if (NonWorkingdays % this.NonworkWeekDays.length == 1) 
                    NonWorkingdays += 1;
            }
        }*/
        var flag = true;
        while(flag) {
            var calnonwork = this.calculatenonworkingDays(stdate, enddate);
            if(NonWorkingdays != calnonwork) {
                var duration = parseInt(userDuration, 10);
                duration = (duration + calnonwork);
                enddate = stdate.add(Date.DAY, duration - 1);
                NonWorkingdays = calnonwork;
            } else
                flag = false;
        }
        return NonWorkingdays;
    },

    timeDifference: function(laterdate, earlierdate){
        var daysDifference = 0;
        if (typeof laterdate == 'string') {
            var ld = Date.parseDate(laterdate, 'd/m/Y');
            var ed = Date.parseDate(earlierdate, 'd/m/Y');
            var difference = ld.getTime() - ed.getTime();
            daysDifference = Math.floor(difference / 1000 / 60 / 60 / 24);
        } else {
            laterdate = laterdate.clearTime(false);
            earlierdate = earlierdate.clearTime(false);
            var difference = laterdate.getTime() - earlierdate.getTime();
            daysDifference = Math.floor(difference / 1000 / 60 / 60 / 24);
        }
        return (daysDifference);
    },

    checkOverDue: function(flag) {
        this.SetPanel_ProjScale();
        var percentVal = 0;
        if(this.progressInPixel != 0) {
            percentVal = Math.round(this.progressInPixel / (this.TaskminWidth * this.duration) * 100);
            if(percentVal > 100) {
                this.progressInPixel = this.panelwidth;
                percentVal = 100;
            }
        }
        var dt = new Date();
        dt.clearTime();
        var enddate = new Date();
        var plannerGrid = this.ganttChartPanel.containerPanel.editGrid;
        var isReset = false;
        if(this.duration > 0) {
            enddate = this.calculateEndDate(this.duration);
            if(this.ganttChartPanel.NonworkWeekDays.join().indexOf(this.startDate.format('w'))!=-1 || (this.ganttChartPanel.HolidaysList.join().indexOf(this.startDate.format('d/m/Y'))!=-1))
                this.forNonWorkingdates(this.startDate, enddate);
            else    
                this.forWorkingDates(this.startDate, enddate);
            if(flag && this.endDate < dt && percentVal < 100 && this.duration > 0) {
                Wtf.get(plannerGrid.getView().getRow(plannerGrid.dstore.find('taskid', this.id.substr(5)))).dom.style.color = "#ff0000";
                this.addClass('taskOverDue');
                isReset = true;
            }
        }
        if(!isReset && this.el.hasClass('taskOverDue')) {
            Wtf.get(plannerGrid.getView().getRow(plannerGrid.dstore.find('taskid', this.id.substr(5)))).dom.style.color = "#000000";
            this.removeClass('taskOverDue');
        }
    },

    setProgress : function(record) {
        this.SetPanel_ProjScale();
        var val = 0;
        var recData = record.data;
        if((recData.duration+'').indexOf("h")>0)
            val = parseFloat(recData.duration)/this.HrsPerDay;
        else
            val = parseFloat(recData.duration); 
        this.progressInPixel = parseFloat((val*this.TaskminWidth)*(parseFloat(recData.percentcomplete)/100));
    },
    
    NumCompanyHoliday : function(stdate,enddate) {
        var NumHolidays = 0 ;
        for(var cnt =0; cnt< this.ganttChartPanel.HolidaysList.length;cnt++) {
            var currHoliday = Date.parseDate(this.ganttChartPanel.HolidaysList[cnt], "d/m/Y");
            if(this.ganttChartPanel.NonworkWeekDays.join().indexOf(currHoliday.format('w')) == -1) { // some Public Holidays fall on WeekEnd days, and NOT need to count.
                if(currHoliday.between(stdate,enddate)) { // Checks if this date falls on or between the given start and end dates
                    NumHolidays += 1;
                }
            }
        }
        return NumHolidays;
    }
    
    ,setViewMode: function(val){
        var taskElement = this.el;
        if(val){
            this.removeResizer();
            taskElement.removeListener('mousedown', this.mymouseDown, this);
            taskElement.removeListener('mouseup', this.mymouseUp, this);
            taskElement.removeListener('mousemove', this.mymouseMove, this);
            taskElement.removeListener('mouseover', this.mymouseOver, this);
            taskElement.removeListener('mouseout', this.mymouseOut, this);
            taskElement.addClass('defaultCursor');
            this.removeMouseMoveListener();
            if(this.critical && !(this.el.hasClass('milestoneIcon') || this.el.hasClass('milestoneWeekIcon')))
                taskElement.addClass('taskOverDue');
        } else {
            this.assignResizer();
            taskElement.addListener('mousedown', this.mymouseDown, this);
            taskElement.addListener('mouseup', this.mymouseUp, this);
            taskElement.addListener('mousemove', this.mymouseMove, this);
            taskElement.addListener('mouseover', this.mymouseOver, this);
            taskElement.addListener('mouseout', this.mymouseOut, this);
            taskElement.removeClass('defaultCursor');
            this.assignMouseMoveListener();
            taskElement.removeClass('taskOverDue');
        }
    }
});
