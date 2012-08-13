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
ganttChart = function(config) {
    Wtf.apply(this,config);
    this.store = new Wtf.data.JsonStore({
        url: 'get-images.php',
        root: 'images',
        fields: ['name', 'url', {
                name: 'size',
                type: 'float'
            },{
                name: 'lastmod',
                type: 'date',
                dateFormat: 'timestamp'
        }]});

    this.tpl = new Wtf.XTemplate(
        '<tpl for=".">',
        '<div class="thumb-wrap" id="{name}">',
        '<div class="thumb"><img src="{url}" title="{name}"></div>',
        '<span class="x-editable"></span></div>',
        '</tpl>',
        '<div class="x-clear"></div>'
    );
    var plgin = [];
    if(!this.archived && !(this.connstatus >= 6 && this.connstatus <= 8) && !this.viewMode)
        plgin = [new Wtf.DataView.DragSelector({projDrag: true, contid: this.id})];
    this.dataViewObj = new Wtf.DataView({
        store: this.store,
        tpl: this.tpl,
        id: this.id + 'dview',
        layout:'fit',
        multiSelect: true,
        overClass: 'x-view-over',
        itemSelector: 'div.thumb-wrap',
        emptyText: "<div  id=chartPanel1_"+this.id+" class='DataViewPanel dailyView'></div>",
        plugins: plgin
    });
    ganttChart.superclass.constructor.call(this, {
        id: this.id,
        split: true,
//        autoHeight:true,
        baseCls: 'chartPanel',
        height: 700,
        collapsible: true,
        margins: '35 5 5 0',
        cmargins: '35 5 5 0',
        cls: 'ganttChart',
        width: 10080,
        layout:'fit',
        items: [this.dataViewObj]
    });
};

Wtf.extend(ganttChart, Wtf.Panel, {
    initComponent: function(){
        ganttChart.superclass.initComponent.call(this);
        this.x1 = 0;    this.x2 = 0;
        this.y1 = 0;    this.y2 = 0;
        this.CanvasX = 0;   this.CanvasY = 0;
        this.circularCheckflag = false;
        this.oldx = 0;
        this.TaskminWidth = 16;
        this.NonworkWeekDays = [];
        this.HolidaysList = [];
        this.linkArrayObj = {};
        this.parentTask = {};
        this.jg;
        this.rowCount;
        this.horiPos;
        this.showOverDue = false;
        this.StartDate = new Date();
        this.projScale = this.containerPanel.projScale;
        this.FromTaskId = null;
        this.panelVisibleForScroll = false;
        this.resourcePanelArray = {};
        this.resourceColorArray = {};
        this.taskProgressArray = {};
        this.panelStatusArr = {};
        this.updatedTaskIds = [];
        this.slackDurObj = [];
        this.updatedParentTaskIds = [];
        this.addEvents({
            'showWindow': true,
            'showWindowForUserChoice': true,
            'chartAfterRender': true
        })
    },

    afterRender: function(config) {
        ganttChart.superclass.afterRender.call(this,config);
        this.fireEvent('chartAfterRender');
    },

    panelMouseDown: function(event, panelObj){
        this.oldx = event.getPageX();
        this.x1 = event.getPageX() - this.CanvasX;
        this.y1 = event.getPageY() - this.CanvasY;
        this.FromTaskId = panelObj.id;
        this.panelVisibleForScroll = true;//for ScrollToPanelOnGridRowSelect function in projPlanner
        this.containerPanel.editGrid.gridRowSelect = false;
        this.containerPanel.editGrid.selectGridRowOnPanelClick((panelObj.id).substr(5));
    },

    SetPanel_ProjScale: function(){
        this.projScale = this.containerPanel.projScale;
        if(this.projScale == 'week'){
            this.TaskminWidth = 5;

        } else {
            this.TaskminWidth = 16;

        }
    },

    indentGrid: function(record) {
        var parentPanel = Wtf.getCmp('Panel' + record.data['taskid']);
        parentPanel.el.replaceClass('custom', 'ParentTask');
        parentPanel.removeMouseMoveListener();
    },

    outdentGrid: function(record) {
        var parentPanel = Wtf.getCmp('Panel' + record.data['taskid']);
        if(parentPanel) {
            parentPanel.el.replaceClass('ParentTask', 'custom');
            parentPanel.assignMouseMoveListener();
        }
    },

    checkBlankForSubtask: function(rowval) {
        var gridStore = this.containerPanel.editGrid.dstore;
        var tempRec = gridStore.getAt(rowval + this.rowCount);
        this.rowCount++;
        if (tempRec && tempRec.data['duration'] == "") {
            do {
                tempRec = gridStore.getAt(rowval + this.rowCount);
                if(!tempRec || tempRec.data['duration'] != "")
                    break;
                this.rowCount++;
            }while(true);
        }
        return tempRec;
    },

    setSubtaskPosition: function(tempRec, tempPanel, parentPanel, side) {
        if(side == 'left') {
            if(document.getElementById(tempPanel.id).getAttribute('TaskType') != null)
                this.horiPos -= 5;
            tempPanel.Xpos = this.horiPos;
        } else if(side == 'right') {
            if(document.getElementById(tempPanel.id).getAttribute('TaskType') != null)
                this.horiPos += 5;
            tempPanel.Xpos = (this.horiPos + parentPanel.panelwidth) - tempPanel.panelwidth;
        }
        tempPanel.setPosition(tempPanel.Xpos, tempPanel.Ypos);
        tempPanel.updatePanel();
        this.mysubtask(tempPanel.id);
        this.RecursionFunToTraceLink(tempPanel.id);
        if(tempPanel.el.hasClass('ParentTask'))
            this.moveParentTask(tempRec);
        return tempPanel;
    },

    mysubtask: function(toTaskPanelId) {
        this.SetPanel_ProjScale();
        var toTaskRecord = this.containerPanel.editGrid.dstore.getAt(this.containerPanel.editGrid.search(toTaskPanelId.substr(5)));
        if (Wtf.get(toTaskPanelId).hasClass('ParentTask'))
            this.moveParentTask(toTaskRecord);
        else
            this.moveChildTask(toTaskRecord);
    },

    moveParentTask: function(toTaskParent) {
        var gridStore = this.containerPanel.editGrid.dstore;
        var parentRow = this.containerPanel.editGrid.search(toTaskParent.data['taskid']);
        var parentPanel = Wtf.getCmp('Panel' + toTaskParent.data['taskid']);
        this.horiPos = parentPanel.Xpos;
        this.rowCount = 1;
        var nextrec = this.checkBlankForSubtask(parentRow);
        if(!nextrec)
            return;
        var nextPanel = Wtf.getCmp('Panel' + nextrec.data['taskid']);
        if (nextPanel) {
            if (nextPanel.Xpos < this.horiPos && nextrec.data['startdate'] != "") {
                nextPanel = this.setSubtaskPosition(nextrec, nextPanel, parentPanel, 'left');
            }
            if ((nextPanel.Xpos + nextPanel.panelwidth) > (this.horiPos + parentPanel.panelwidth) && nextrec.data['startdate'] != "") {
                nextPanel = this.setSubtaskPosition(nextrec, nextPanel, parentPanel, 'right');
            }
        }
        var lastnextrec = nextrec;
        nextrec = this.checkBlankForSubtask(parentRow);
        if(nextrec) {
            nextPanel = Wtf.getCmp('Panel' + nextrec.data['taskid']);
            if (nextPanel) {
                var compareLevel = parseInt(toTaskParent.data['level']) + 1;
                for (; nextrec.data['level'] >= compareLevel; this.rowCount++){
                    if (nextrec.data['level'] == compareLevel) {
                        if(document.getElementById(nextPanel.id).getAttribute('TaskType') == null) {
                            if (nextPanel.Xpos <  this.horiPos && nextrec.data['startdate'] != "") {
                            nextPanel = this.setSubtaskPosition(nextrec, nextPanel, parentPanel, 'left');
                            }
                            if ((nextPanel.Xpos + nextPanel.panelwidth) > (this.horiPos + parentPanel.panelwidth) && nextrec.data['startdate'] != "") {
                                nextPanel = this.setSubtaskPosition(nextrec, nextPanel, parentPanel, 'right');
                            }
                        } else {
                            if(this.projScale == 'day'){
                                if ((nextPanel.Xpos+5) <  this.horiPos && nextrec.data['startdate'] != "") {
                                    nextPanel = this.setSubtaskPosition(nextrec, nextPanel, parentPanel, 'left');
                                }
                                if(((nextPanel.Xpos + nextPanel.panelwidth)-5) > (this.horiPos + parentPanel.panelwidth) && nextrec.data['startdate'] != "") {
                                    nextPanel = this.setSubtaskPosition(nextrec, nextPanel, parentPanel, 'right');
                                }
                            } else {
                                if ((nextPanel.Xpos+5) <  this.horiPos && nextrec.data['startdate'] != "") {
                                    nextPanel = this.setSubtaskPosition(nextrec, nextPanel, parentPanel, 'left');
                                }
                                if(((nextPanel.Xpos + nextPanel.panelwidth)-5) > (this.horiPos + parentPanel.panelwidth) && nextrec.data['startdate'] != "") {
                                    nextPanel = this.setSubtaskPosition(nextrec, nextPanel, parentPanel, 'right');
                                }
                            }
                        }

                        lastnextrec = nextrec;
                    }
                    nextrec = gridStore.getAt(parentRow + this.rowCount);
                    if (nextrec && nextrec.data['duration'] == "") {
                        do {
                            nextrec = gridStore.getAt(parentRow + this.rowCount + 1);
                            if(!nextrec || nextrec.data['duration'] != "")
                                break;
                            this.rowCount++;
                        }while(true);
                    }
                    if(!nextrec)
                        break;
                    nextPanel = Wtf.getCmp('Panel' + nextrec.data['taskid']);
                    if (!nextPanel)
                        break;
                }
            }
        }
        this.moveChildTask(lastnextrec);
    },

    moveChildTask: function(toTaskRecord) {
        this.SetPanel_ProjScale();
        var plannerGrid = this.containerPanel.editGrid;
        var gridStore = plannerGrid.dstore;
        var taskWidth = this.TaskminWidth;
        while (toTaskRecord.data['parent'] != 0) {
            var MinLeft = 0, MaxRight = 0, actMaxRight = 0, actMinLeft = 0;
            var recRow = plannerGrid.search(toTaskRecord.data['parent']);
            if(recRow == null) {
                plannerGrid.afterDelete(toTaskRecord.data['parent']);
                return;
            }
            var toTaskParent = gridStore.getAt(recRow);
            var parentPanel = Wtf.getCmp('Panel' + toTaskParent.data['taskid']);
            var parentRow = plannerGrid.search(toTaskParent.data['taskid']);
            var nextrec = gridStore.getAt(parentRow + 1);
            var i = 2;
            if (nextrec && nextrec.data['duration'] == "") {
                do {
                    nextrec = gridStore.getAt(parentRow + i);
                    if(!nextrec || nextrec.data['duration'] != "")
                        break;
                    i++;
                }while(true);
            }
            if(!nextrec)
                break;
            var nextPanel = Wtf.getCmp('Panel' + nextrec.data['taskid']);
            if(nextPanel) {
                if(document.getElementById(nextPanel.id).getAttribute('TaskType') == null) {
                    MinLeft = nextPanel.Xpos;
                    MaxRight = nextPanel.Xpos + nextPanel.panelwidth;
                    actMinLeft = MinLeft;
                    actMaxRight = MaxRight;
                } else {
                    if(this.projScale == 'day'){
                        MinLeft = MaxRight = actMinLeft = nextPanel.Xpos + 5;
                        if(nextPanel.id == 'Panel'+toTaskRecord.get('taskid'))
                            actMaxRight = MaxRight + 5;
                        else
                            actMaxRight = MaxRight;
                    }
                    else
                        MinLeft = MaxRight = actMaxRight = actMinLeft = nextPanel.Xpos;
                }
            }
            nextrec = gridStore.getAt(parentRow + i);
            i++;
            if (nextrec && nextrec.data['duration'] == "") {
                do {
                    nextrec = gridStore.getAt(parentRow + i);
                    if(!nextrec || nextrec.data['duration'] != "")
                        break;
                    i++;
                }while(true);
            }
            if(nextrec) {
                var compareLevel = parseInt(toTaskParent.data['level']) + 1;
                for (; nextrec.data['level'] >= compareLevel; i++) {
                    if (nextrec.data['level'] == compareLevel) {
                        var tempPanel = Wtf.getCmp('Panel' + nextrec.data['taskid']);
                        if(tempPanel) {
                            var x = tempPanel.Xpos;
                            if (x < MinLeft) {
                                MinLeft = x;
                                actMinLeft = MinLeft;
                                if(document.getElementById(tempPanel.id).getAttribute('TaskType') != null)
                                    if(this.projScale == 'day')
                                        MinLeft += 5;
                                    else
                                        MinLeft += 0;
                                if(actMinLeft != MinLeft){
                                    actMinLeft = MinLeft - 5;
                                }
                            }
                            if ((x + tempPanel.panelwidth) > MaxRight) {
                                MaxRight = x + tempPanel.panelwidth;
                                actMaxRight = MaxRight;
                                if(document.getElementById(tempPanel.id).getAttribute('TaskType') != null)
                                    if(this.projScale == 'day')
                                        MaxRight -= 5;
                                    else
                                        MaxRight -= 0;
                                if(actMaxRight != MaxRight){
                                    actMaxRight = MaxRight + 5;
                                }
                            }
                        }
                    }
                    nextrec = gridStore.getAt(parentRow + i);
                    if (nextrec && nextrec.data['duration'] == "") {
                        do {
                            nextrec = gridStore.getAt(parentRow + i + 1);
                            if(!nextrec || nextrec.data['duration'] != "")
                                break;
                            i++;
                        }while(true);
                    }
                    if(!nextrec)
                        break;
                    tempPanel = Wtf.get('Panel' + nextrec.data['taskid']);
                    if (!tempPanel)
                        break;
                }
            }
            if(actMaxRight > MaxRight){
                var diff = actMaxRight - MaxRight;
                if((MaxRight - MinLeft) != 0){
                    if(diff == 5)
                        MaxRight += 16;
                    else
                        MaxRight += 11;
                }
            }
            var panelWidth =  MaxRight - MinLeft;
            var flag = false;
            if(parentPanel.panelwidth != panelWidth || parentPanel.Xpos != MinLeft) {
                if(panelWidth == 0) {
                    if(this.projScale == 'day'){
                        parentPanel.el.replaceClass('custom', 'milestoneIcon');
                        parentPanel.el.dom.style.width = "10px";
                        parentPanel.panelwidth = 10;
                        parentPanel.Xpos = MinLeft - 5;
                    } else {
                        parentPanel.el.replaceClass('custom', 'milestoneWeekIcon');
                        parentPanel.el.dom.style.width = "5px";
                        parentPanel.panelwidth = 5;
                        parentPanel.Xpos = MinLeft;
                    }
                    flag = true;
                    this.removeResizer(parentPanel.id);
                    parentPanel.setPosition(parentPanel.Xpos, parentPanel.Ypos);
                    document.getElementById(parentPanel.id).setAttribute('TaskType', 'milestone');
                } else {
                    if(document.getElementById(parentPanel.id).getAttribute('TaskType') != null)
                        if(this.projScale == 'day')
                            parentPanel.el.replaceClass('milestoneIcon', 'ParentTask');
                        else
                            parentPanel.el.replaceClass('milestoneWeekIcon', 'ParentTask');
                    parentPanel.setPosition(MinLeft, parentPanel.Ypos);
                    parentPanel.Xpos = MinLeft;
                    parentPanel.el.dom.style.width = panelWidth + "px";
                    parentPanel.panelwidth = panelWidth;
                }
                var stdate = new Date(this.StartDate);
                var enddate = new Date();
                var xpos = parentPanel.Xpos;
                if(flag)
                    if(this.projScale == 'day')
                        xpos += 5;
                    else
                        xpos += 0;
                stdate = stdate.add(Date.DAY, xpos / taskWidth);
                var endPos = xpos + parentPanel.panelwidth;
                enddate = (this.StartDate).add(Date.DAY, parseInt(endPos / taskWidth) - 1);
                if (parseFloat(endPos / taskWidth) > parseInt(endPos / taskWidth))
                    enddate = enddate.add(Date.DAY, 1);
                var duration = panelWidth / taskWidth + "";
                var nonWorkDays = parentPanel.calculatenonworkingDays(stdate, enddate);
                if (nonWorkDays != 0)
                    duration = duration - nonWorkDays + "";
                //if (stdate.format('w') == 6 || stdate.format('w') == 0)
                if(this.NonworkWeekDays.join().indexOf(stdate.format('w'))!=-1 || this.HolidaysList.join().indexOf(stdate.format('d/m/Y'))!=-1)
                    duration = parseFloat(duration, 10) + 1 + "";
                if(plannerGrid.clientServerChange == 'client') {
                    plannerGrid.updateGridForParentWithoutServReq(stdate, enddate, duration, parentPanel.id);
                    this.updatedParentTaskIds[this.updatedParentTaskIds.length] = parentPanel.id.substr(5);
                } else
                    plannerGrid.updateGridForParentTask(stdate, enddate, duration, parentPanel.id);

                //plannerGrid.updateGridForParentTask(stdate, enddate, duration, parentPanel.id);
                this.RecursionFunToTraceLink(parentPanel.id);
            }
            toTaskRecord = toTaskParent;
        }
    },

    mysubtaskoutdent: function(record, rowval) {
        var presentlevel = parseInt(record.data['level']);
        var nextrec = this.containerPanel.editGrid.dstore.getAt(rowval + 1);
        var nextlevel = parseInt(nextrec.data['level']);
        if (presentlevel==(nextlevel-1))
            this.indentGrid(record);
        this.mysubtask('Panel' + record.data['taskid']);
    },

    UpdateFromGrid: function(record) {
        var recordPanel = Wtf.getCmp('Panel' + record.data['taskid']);
        recordPanel.duration = record.data['duration'];
        if (recordPanel.timeDifference(recordPanel.startDate, new Date(record.data['startdate'])) != 0) {
            recordPanel.actualstartDate = record.data['actstartdate'];
            recordPanel.setPanelPosition(record.data['startdate'],recordPanel.Ypos);
        } else
            recordPanel.setPanelWidth(record);
        this.mysubtask(recordPanel.id);
        this.RecursionFunToTraceLink(recordPanel.id);
        if(this.updatedTaskIds.length > 0)
            this.containerPanel.editGrid.updatedTasks_ServerRequest(this.updatedTaskIds);
        if(this.updatedParentTaskIds.length > 0)
            this.containerPanel.editGrid.updatedParentTasks_ServerRequest(this.updatedParentTaskIds)
        //this.reassignLinks();
    },

    hidePanel: function(panelArray) {
        for (var p in panelArray) {
            var tempP = Wtf.getCmp('Panel' + panelArray[p]);
            if(tempP)
                tempP.hide();
            tempP = Wtf.getCmp('Slack' + panelArray[p]);
            if(tempP) {
                tempP.hide();
            }
        }
    },

    showPanel: function(panelArray) {
        var Ypos = Wtf.getCmp('Panel' + panelArray[0]).Ypos;
        var arrLength = panelArray.length;
        for (var p = 1; p < arrLength; p++) {
            var tp = Wtf.getCmp('Panel' + panelArray[p]);
            if(tp) {
                tp.show();
                tp.Ypos = Ypos + (p * 21);
                tp.setPosition(tp.Xpos, tp.Ypos);
            }
            tp = Wtf.getCmp('Slack' + panelArray[p]);
            if(tp) {
                tp.show();
                tp.Ypos = Ypos + (p * 21);
                tp.setPosition(tp.Xpos, tp.Ypos);
            }
        }
    },

    panelOver: function(taskID){
        var rowVal = this.containerPanel.editGrid.search(taskID);
        Wtf.get(this.containerPanel.editGrid.getView().getRow(rowVal)).addClass('x-grid3-row-over');
    },

    panelOut: function(taskID){
        var rowVal = this.containerPanel.editGrid.search(taskID);
        Wtf.get(this.containerPanel.editGrid.getView().getRow(rowVal)).removeClass('x-grid3-row-over');
    },

    shiftup: function(successorTasks, shiftVal, shiftIndexes) {
        var plannerGrid = this.containerPanel.editGrid;
        var showResourceFlag = plannerGrid.showResourceFlag;
        var taskLength = successorTasks.length;
        for (var i = 0; i < taskLength; i++) {
            var gpanel = Wtf.getCmp('Panel' + successorTasks[i]);
            if (gpanel != null) {
                gpanel.Ypos = gpanel.Ypos - shiftVal;
                gpanel.setPosition(gpanel.Xpos, gpanel.Ypos);
                if(showResourceFlag) {
                    this.reassignResourcePosition(successorTasks[i], gpanel.Xpos, gpanel.Ypos - shiftVal);
                }
                if(shiftIndexes)
                    plannerGrid.shiftUpInGrid(successorTasks[i], 1);
            }
        }
    },

    reassignResourcePosition: function(taskId,XPos, YPos) {
        if (this.resourcePanelArray['PanelResource_' + taskId]) {
            var obj = this.resourcePanelArray['PanelResource_' + taskId];
            obj.clear();
            obj.setFont("tahoma,verdana,arial,geneva,helvetica,sans-serif", "10px", Font.BOLD);
            obj.drawString(this.containerPanel.editGrid.dstore.getAt(this.containerPanel.editGrid.search(taskId)).data["resourcename"], XPos + Wtf.getCmp('Panel' + taskId).panelwidth + 7, YPos - 5);
            obj.paint();
        }
    },

    shiftdown: function(successorTasks, shiftVal, shiftIndexes) {
        var plannerGrid = this.containerPanel.editGrid;
        var showResourceFlag = plannerGrid.showResourceFlag;
        var taskLength = successorTasks.length;
        for (var i = 0; i < taskLength; i++) {
            var gpanel = Wtf.getCmp('Panel' + successorTasks[i]);
            if (gpanel != null) {
                gpanel.Ypos = gpanel.Ypos + shiftVal;
                gpanel.setPosition(gpanel.Xpos, gpanel.Ypos);
                if(showResourceFlag) {
                    this.reassignResourcePosition(successorTasks[i], gpanel.Xpos, gpanel.Ypos + shiftVal);
                }
                if(shiftIndexes)
                    plannerGrid.shiftDownInGrid(successorTasks[i], 1);
            }
        }
    },

    panelMouseUp: function(event, panelObj) {
        if (this.FromTaskId != null && this.FromTaskId != panelObj.id) {
            Wtf.get(this.FromTaskId).removeClass('newTaskPanelStyle');
            Wtf.get(panelObj.id).removeClass('newTaskPanelStyle');
            this.containerPanel.editGrid.checkForValidLinkOnGanttChart(this.FromTaskId, panelObj.id);
            if (Wtf.get('Panel_0') != null)
                this.remove(Wtf.getCmp('Panel_0'),true);
            this.x1 = this.x2 = this.y2 = this.y1 = 0;
            this.FromTaskId = null;
            this.oldx = 0;
            this.el.replaceClass('chartPanelNewCursor', 'chartPanelOldCursor');
        }
    },

/*    dialog: function() {
        msgBoxShow(66, 1);
    },
*/
    assignListener: function() {
        this.el.addListener('mousemove', this.ChartPanelmousemove, this);
        this.el.addListener('mouseup', this.ChartPanelmouseup, this);
    },

    createTaskPanel: function(record) {
        var stdate = new Date(record.data['startdate']);
        var dur = record.data['duration'];
        var tPanel = new Wtf.proj.taskPanel({
            id: 'Panel' + record.data['taskid'],
            archived: this.archived,
            connstatus: this.connstatus,
            viewMode: this.viewMode,
            sdate: new Date(stdate.getFullYear(), stdate.getMonth(), stdate.getDate()),
            Dur: dur,
            actualstartDate: record.data['actstartdate'],
            actualDuration: record.data['actduration'],
            taskName: record.data['taskname'],
            autoDestroy: true
        });
        this.add(tPanel);
        this.doLayout();
        var ht = (this.containerPanel.editGrid.dstore.getCount() * 21) + 5;
        var curHt = Wtf.get("chartPanel1_"+this.id).getHeight();
        if(ht >= curHt) {
            var newHt = curHt + 210;
            this.setSize(this.width, newHt);
            Wtf.get("chartPanel1_"+this.id).setHeight(newHt);
            this.containerPanel.drawToday();
        }
        tPanel.on('panelMouseUp', this.panelMouseUp);
        tPanel.on('panelMouseDown', this.panelMouseDown);
        return tPanel;
    },

    createSlackPanel: function(record, endDate) {
        var dur = record.data['expected'];
        var tPanel = new Wtf.proj.taskPanel({
            id: 'Slack' + record.data['taskid'],
            slackPanel: true,
            archived: this.archived,
            connstatus: this.connstatus,
            viewMode: this.viewMode,
            sdate: new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate()),
            Dur: dur,
            actualstartDate: endDate,
            actualDuration: dur,
            taskName: record.data['taskname'],
            autoDestroy: true
        });
        this.add(tPanel);
        this.doLayout();
        return tPanel;
    },

    deleteTasks: function(taskIdArray) {
        var tPanel = null;
        var arrLength = taskIdArray.length;
        var plannerGrid = this.containerPanel.editGrid;
        var gridStore = plannerGrid.dstore;
        var plannerView = plannerGrid.getView();
        for(var idCnt = 0; idCnt < arrLength; idCnt++){
            tPanel = Wtf.getCmp('Panel' + taskIdArray[idCnt].taskid);
            var rowIndex = plannerGrid.search(taskIdArray[idCnt].taskid);
            var tskRec = gridStore.getAt(rowIndex);
            if(tskRec !== undefined) {
                if((tPanel && tPanel.hidden != true) ||
                    (tskRec.data.duration == "" && plannerView.getRow(rowIndex).style.display != "none")) {
                    this.shiftup(plannerGrid.getSuccessorTaskIds(taskIdArray[idCnt].taskid), 21, true);
                    plannerGrid.dragOffset -= 21;
                }
                if(tPanel){
                    tPanel.destroyPanel();
                    this.remove(tPanel.id,true);
                }
            }
        }
        if(plannerGrid.showResourceFlag) {
            var taskIds = [];
            for(var i = 0; i < arrLength; i++)
                taskIds[i] = taskIdArray[i].taskid;
            this.showResources([], taskIds, false,null);
        }
    },

    addPanel: function(record, rowvalue) {
        if(record.data['startdate']!="" || record.data['duration']!="") {
            var tPanel = Wtf.getCmp('Panel' + record.data['taskid']);
            if (!tPanel) {
                tPanel = this.createTaskPanel(record);
                var row = this.containerPanel.editGrid.searchWithoutBlank(this.containerPanel.editGrid.dstore.getAt(rowvalue).data.taskid);
                tPanel.setPanelPosition(new Date(record.data['startdate']), (row) * 21 + 5);
                if(record.data['percentcomplete']!=0)
                    tPanel.setProgress(record);
            }
            this.UpdateFromGrid(record);
            if(this.showOverDue) {
                tPanel.checkOverDue(true);
            }
        }
    },

    addPanelOnDataload: function (Gridstore, startIndex) {
        var length = Gridstore.getCount();
        var plannerGrid = this.containerPanel.editGrid;
        for (var i = startIndex; i < length - 1; i++) {
            var record = Gridstore.getAt(i);
            if(record.data['startdate']!="" || record.data['duration']!="") {
                var tPanel = Wtf.getCmp('Panel' + record.data['taskid']);
                var row = plannerGrid.searchWithoutBlank(Gridstore.getAt(i).data.taskid);
                if (!tPanel) {
                    tPanel = this.createTaskPanel(record);
                    tPanel.setPanelPositionWidthOnDataLoad(record,(row * 21) + 5);
                }
                if(record.data['percentcomplete']!=0)
                    tPanel.setProgress(record);
                if(this.showOverDue) {
                    tPanel.checkOverDue(true);
                }
            }
       }
    },

    insertPanelOnDataLoad : function(record,rowvalue) {
        if(record.data.startdate == "") {
            return;
        }
        var tPanel = Wtf.getCmp('Panel' + record.data['taskid']);
        var plannerGrid = this.containerPanel.editGrid;
        var row = plannerGrid.searchWithoutBlank(plannerGrid.dstore.getAt(rowvalue).data.taskid);
        if (!tPanel) {
            tPanel = this.createTaskPanel(record);
            tPanel.setPanelPositionWidthOnDataLoad(record,(row * 21) + 5);
        } else {
            tPanel.setPanelPositionWidthOnDataLoad(record,(row * 21) + 5);
            this.RecursionFunToTraceLink(tPanel.id);
        }
        if(record.data['percentcomplete']!=0)
            tPanel.setProgress(record);
        if(this.showOverDue) {
            tPanel.checkOverDue(true);
        }
        tPanel.updateQTip(record);
    },


    ChartPanelmousemove: function(event){
        if (this.x1 > 0 && this.y1 > 0) {
            this.x2 = event.getPageX() - 2 - this.CanvasX;
            this.y2 = event.getPageY() - 2 - this.CanvasY;
            this.jg.clear();
            if (Wtf.get('Panel_0') == null) {
                this.jg.drawLine(this.x1, this.y1, this.x2, this.y2);
                this.jg.paint();
            } else {
                var moveX = event.getPageX() - this.oldx;
                this.el.replaceClass('chartPanelOldCursor', 'chartPanelNewCursor');
                var ElChartPos = [];
                ElChartPos = Wtf.getCmp('Panel_0').getPosition(true);
                var relMouseY_position = event.getPageY() - this.CanvasY;
                if (relMouseY_position > (ElChartPos[1] - 9) && relMouseY_position < (ElChartPos[1] + 23)){
                    this.x1 = ElChartPos[0] + moveX;
                    if (this.x1 > 0)
                        Wtf.getCmp('Panel_0').setPosition(this.x1, ElChartPos[1]);
                    else
                        Wtf.getCmp('Panel_0').setPosition(0, ElChartPos[1]);
                    this.oldx = event.getPageX();
                }
            }
        }
    },

    RecursionFunToTraceLink: function(ChangeTask){
        var changeTaskObj = Wtf.getCmp(ChangeTask);
        var changeTaskX = changeTaskObj.Xpos;
        var changeFlag = false;
        if(document.getElementById(ChangeTask).getAttribute('TaskType') == null) {
            changeFlag = true;
        }
        var succArray = changeTaskObj.successor;
        for (var succ in succArray) {
            var tempStr = 'jg_' + ChangeTask + '_' + succ;
            var succObj = Wtf.getCmp(succ);
            if (changeFlag) {
                if (succObj.Xpos < changeTaskX + changeTaskObj.panelwidth) {
                    if (document.getElementById(succ).getAttribute('TaskType') == null) {
                        succObj.Xpos = changeTaskX + changeTaskObj.panelwidth;
                        succObj.Xpos += this.TaskminWidth * (((changeTaskX + changeTaskObj.panelwidth) / this.TaskminWidth) % 1)
                        succObj.setPosition(succObj.Xpos);
                    } else {
                        succObj.Xpos = changeTaskX + changeTaskObj.panelwidth - 5 ;
                        succObj.setPosition(succObj.Xpos);
                    }
                    succObj.updatePanel();
                    this.AttachNewlink(tempStr, ChangeTask, succ, false);
                    this.mysubtask(succ);
                    this.RecursionFunToTraceLink(succ);
                } else
                    this.AttachNewlink(tempStr, ChangeTask, succ, false);
            } else {
                if ((changeTaskX + changeTaskObj.panelwidth) > succObj.Xpos) {
                    if (document.getElementById(succ).getAttribute('TaskType') == null) {
                        succObj.Xpos = changeTaskX + changeTaskObj.panelwidth - 5 ;
                        succObj.setPosition(succObj.Xpos);
                    } else {
                        succObj.Xpos = changeTaskX;
                        succObj.setPosition(succObj.Xpos);
                    }
                    succObj.updatePanel();
                    this.AttachNewlink(tempStr, ChangeTask, succ, false);
                    this.mysubtask(succ);
                    this.RecursionFunToTraceLink(succ);
                } else
                    this.AttachNewlink(tempStr, ChangeTask, succ, false);
            }
        }
        var predArray = changeTaskObj.predecessor;
        for(var pred in predArray) {
            var tempStr = 'jg_' + pred + '_' + ChangeTask;
            if(changeFlag && document.getElementById(pred).getAttribute('TaskType') == null) {
                var predObj = Wtf.getCmp(pred);
                if (changeTaskX < predObj.Xpos + predObj.panelwidth) {
                    changeTaskObj.Xpos = predObj.Xpos + predObj.panelwidth;
                    changeTaskObj.setPosition(changeTaskObj.Xpos);
                    changeTaskObj.updatePanel();
                    this.mysubtask(ChangeTask);
                    this.RecursionFunToTraceLink(ChangeTask);
                } else
                    this.AttachNewlink(tempStr, pred, ChangeTask, false);
            } else
                this.AttachNewlink(tempStr, pred, ChangeTask, false);
        }
        return;
    },

    UpdatelinkArrayObj: function(prevVal, curVal, toTask, flagToSave) {
        prevVal += "";
        var tempPrev = [];
        if (prevVal.indexOf(',') != -1)
            tempPrev = prevVal.split(',');
        else
            tempPrev[0] = prevVal;
        var tempCur = [];
        if (curVal.indexOf(',') != -1)
            tempCur = curVal.split(',');
        else
            tempCur[0] = curVal;
        var curLength = tempCur.length;
        var plannerGrid = this.containerPanel.editGrid;
        var getGridStore = plannerGrid.dstore;
        if (prevVal != "") {
            var prevLength = tempPrev.length;
            for (var i = 0; i < prevLength; i++) {
                for (var j = 0; j < curLength; j++) {
                    if (tempPrev[i] == tempCur[j]) {
                        tempCur[j] = -1;
                        break;
                    }
                }
                if (j == curLength) {
                    var tempCurRec = getGridStore.getAt(tempPrev[i] - 1);
                    var linkObj = 'jg_Panel' + tempCurRec.data['taskid'] + '_Panel' + toTask;
                    this.deleteLinkOnUpdatePred(linkObj);
                    if (curVal == "")
                        this.setOldPositionOf_ToTask('Panel' + toTask);
                    this.mysubtask('Panel' + toTask);
                    this.RecursionFunToTraceLink('Panel' + toTask);
                    if(this.updatedTaskIds.length > 0)
                        plannerGrid.updatedTasks_ServerRequest(this.updatedTaskIds);
                    if(this.updatedParentTaskIds.length > 0)
                        plannerGrid.updatedParentTasks_ServerRequest(this.updatedParentTaskIds);
                }
            }
        }
        if(!Wtf.getCmp('Panel' + toTask)) {
            var toTaskRow = plannerGrid.search(toTask);
            this.addPanel(getGridStore.getAt(toTaskRow), toTaskRow);
        }
        for (var s = 0; s < curLength; s++) {
            if (tempCur[s] != -1 && tempCur[s]!="") {
                var currentRecord = getGridStore.getAt(tempCur[s] - 1);
                var currentPanelId = 'Panel' + currentRecord.data['taskid'];
                this.AttachNewlink('jg_' + currentPanelId + '_Panel' + toTask, currentPanelId, 'Panel' + toTask,false);
                if(flagToSave)
                    plannerGrid.add_deleteLinkFromDB(currentRecord.data['taskid'], toTask, "add");
                this.mysubtask(currentPanelId);
                this.RecursionFunToTraceLink(currentPanelId);
                if(this.updatedTaskIds.length > 0)
                    plannerGrid.updatedTasks_ServerRequest(this.updatedTaskIds);
                if(this.updatedParentTaskIds.length > 0)
                    plannerGrid.updatedParentTasks_ServerRequest(this.updatedParentTaskIds);
            }
        }
    },

    deleteLinkOnUpdatePred :function(linkKey) {
        var obj = this.linkArrayObj[linkKey];
        obj.clear();
        var tobj;
        delete this.linkArrayObj[linkKey];
        if(tobj = Wtf.getCmp(linkKey))
            tobj.destroy();
        var idArray = linkKey.split("_");
        if(tobj = Wtf.getCmp(idArray[1]))
            delete tobj.successor[idArray[2]];
        if(tobj = Wtf.getCmp(idArray[2]))
            delete tobj.predecessor[idArray[1]];
        this.containerPanel.editGrid.add_deleteLinkFromDB((linkKey.split('_'))[1].substr(5), (linkKey.split('_'))[2].substr(5), "delete");
    },

    attachlinkOnDataLoad :function(Gridstore, startIndex) {
        var length = Gridstore.getCount();
        for (var i = startIndex; i < length; i++) {
            var irec = Gridstore.getAt(i);
            var nextrec = Gridstore.getAt(i + 1);
            if (nextrec && nextrec.data['parent'] != "0")
                if ((irec.data['taskid']) == nextrec.data['parent'])
                    this.indentGrid(irec);
       }
    },

    checkForAttachedtaskPosition : function(from,to) {
        var fromObj = Wtf.getCmp(from);
        var toObj = Wtf.getCmp(to);
        if (document.getElementById(from).getAttribute('TaskType') == null) {
            if (toObj.Xpos < fromObj.Xpos + fromObj.panelwidth) {
                if (document.getElementById(to).getAttribute('TaskType') == null) {
                    toObj.Xpos = fromObj.Xpos + fromObj.panelwidth;
                    toObj.setPosition(toObj.Xpos);
                } else {
                    toObj.Xpos = fromObj.Xpos + fromObj.panelwidth - 5;
                    toObj.setPosition(toObj.Xpos);
                }
                toObj.updatePanelOnDataLoad();
                this.mysubtask(to);
            }
        } else if ((fromObj.Xpos + fromObj.panelwidth) > toObj.Xpos) {
            if (document.getElementById(to).getAttribute('TaskType') == null) {
                toObj.Xpos = fromObj.Xpos + fromObj.panelwidth - 5;
                toObj.setPosition(toObj.Xpos);
            } else {
                toObj.Xpos = fromObj.Xpos;
                toObj.setPosition(toObj.Xpos);
            }
            toObj.updatePanelOnDataLoad();
            this.mysubtask(to);
       }
    },
    AttachNewlink: function(LinkArraykey, FromTaskId, ToTaskId, flag) {
        var obj;
        var FromTaskObj = Wtf.getCmp(FromTaskId);
        var ToTaskObj = Wtf.getCmp(ToTaskId);
        if (this.linkArrayObj[LinkArraykey]) {
            obj = this.linkArrayObj[LinkArraykey];
            obj.clear();
        } else {
            var tempPanel = new Wtf.Panel({
                renderTo: this.id,
                baseCls: "linkPanel",
                id: LinkArraykey
            });
            if(!this.archived && !(this.connstatus >= 6 && this.connstatus <= 8) && !this.viewMode)
                Wtf.get(LinkArraykey).addListener('dblclick', this.LinkPanelClick, {scope: this, key: LinkArraykey});
            obj = new jsGraphics(LinkArraykey);
            this.linkArrayObj[LinkArraykey] = obj;
            var taskLinkArray = FromTaskObj.successor;
            taskLinkArray[ToTaskId] = ToTaskId;
            taskLinkArray = ToTaskObj.predecessor;
            taskLinkArray[FromTaskId] = FromTaskId;
        }
        this.jg.clear();
        obj.setColor('gray');
        var fromTaskPageY = FromTaskObj.Ypos;
        var toTaskPageX = ToTaskObj.Xpos;
        var toTaskPageY = ToTaskObj.Ypos;
        var plannerGrid = this.containerPanel.editGrid;
        var FromTaskIndex = this.containerPanel.editGrid.search(FromTaskId.substr(5));
        var ToTaskIndex = this.containerPanel.editGrid.search(ToTaskId.substr(5));

        if (FromTaskIndex < ToTaskIndex) {
            if(ToTaskObj.el.dom.getAttribute('TaskType') == null) {
                var X1 = toTaskPageX + 3;
                var Y1 = fromTaskPageY +4;
                obj.drawLine(FromTaskObj.Xpos + FromTaskObj.panelwidth, Y1, X1, Y1 );
                obj.drawLine(X1, Y1, X1, toTaskPageY - 2);
                obj.fillPolygon(new Array(toTaskPageX-1, X1 , toTaskPageX + 7), new Array(toTaskPageY - 6, toTaskPageY - 2, toTaskPageY - 6));
            } else {
                if(this.projScale == 'day'){
                    obj.drawLine(FromTaskObj.Xpos + FromTaskObj.panelwidth, fromTaskPageY + 4, toTaskPageX + 8, fromTaskPageY + 4 );
                    obj.drawLine(toTaskPageX + 8, fromTaskPageY + 4, toTaskPageX + 8, toTaskPageY - 2);
                    obj.fillPolygon(new Array(toTaskPageX+4, toTaskPageX + 8, toTaskPageX + 12), new Array(toTaskPageY - 6, toTaskPageY - 2, toTaskPageY - 6));
                } else {
                    obj.drawLine(FromTaskObj.Xpos + FromTaskObj.panelwidth, fromTaskPageY + 4, toTaskPageX + 2, fromTaskPageY + 4 );
                    obj.drawLine(toTaskPageX + 2, fromTaskPageY + 4, toTaskPageX + 2, toTaskPageY - 3);
                    obj.fillPolygon(new Array(toTaskPageX+1, toTaskPageX + 2, toTaskPageX + 3), new Array(toTaskPageY - 3, toTaskPageY - 1, toTaskPageY - 3));
            }
            }
        } else {
            obj.drawLine(FromTaskObj.Xpos + FromTaskObj.panelwidth, fromTaskPageY + 4, toTaskPageX + 3, fromTaskPageY + 4);
            obj.drawLine(toTaskPageX + 3, fromTaskPageY + 4, toTaskPageX + 3, toTaskPageY + 10);
            obj.fillPolygon(new Array(toTaskPageX-1, toTaskPageX + 3, toTaskPageX + 7), new Array(toTaskPageY + 14, toTaskPageY + 10, toTaskPageY + 14));
        }
        if(!FromTaskObj.hidden && !ToTaskObj.hidden)
            obj.paint();
    },

    deleteLink: function(linkKey, flag) {
        var obj = this.linkArrayObj[linkKey];
        var tobj;
        obj.clear();
        delete this.linkArrayObj[linkKey];
        if(tobj = Wtf.getCmp(linkKey))
            tobj.destroy();
        var idArray = linkKey.split("_");
        var plannerGrid = this.containerPanel.editGrid;
        if(tobj = Wtf.getCmp(idArray[1]))
            delete tobj.successor[idArray[2]];
        if(tobj = Wtf.getCmp(idArray[2])) {
            delete tobj.predecessor[idArray[1]];
            var pRec = plannerGrid.dstore.getAt(plannerGrid.search(idArray[2].substr(5)));
            var recid = plannerGrid.search(idArray[1].substr(5)) + 1;
            var currentPred = pRec.data['predecessor'];
            var k = currentPred.indexOf(recid);
            if (k != -1) {
                if (k == 0) {
                    if (currentPred.indexOf(',') != -1)
                        currentPred = currentPred.replace(recid + ',', '');
                    else
                        currentPred = currentPred.replace(recid, '');
                } else
                    currentPred = currentPred.replace(',' + recid, '');
            }
            var sc = plannerGrid.backupclass('Panel' + pRec.data['taskid'], -1);
            pRec.set('predecessor', currentPred);
            if(flag)
                plannerGrid.onInOutPredupdateDB(pRec,"predecessor") ;
            plannerGrid.assignclass('Panel' + pRec.data['taskid'], sc);
        }
        if(flag)
            plannerGrid.add_deleteLinkFromDB((linkKey.split('_'))[1].substr(5), (linkKey.split('_'))[2].substr(5), "delete");
    },

    LinkPanelClick: function() {
        var linkArray = this.key.split('_');
        var plannerGrid = this.scope.containerPanel.editGrid;
        var from = Wtf.util.Format.ellipsis(plannerGrid.dstore.getAt(plannerGrid.search(linkArray[1].substr(5))).data['taskname'], 35);
        var to = Wtf.util.Format.ellipsis(plannerGrid.dstore.getAt(plannerGrid.search(linkArray[2].substr(5))).data['taskname'], 35);
        var configObj = {
            title: WtfGlobal.getLocaleText('pm.project.plan.taskdependency'),
            width: 280,
            height: 120,
            iconCls: 'iconwin',
            resizable: false,
            plain: true,
            modal: true,
            border: false,
            items: new Wtf.FormPanel({
                frame: true,
                height: 60,
                bodyStyle: 'padding:5px 5px 0',
                width: '100%',
                items: [{
                    height: 20,
                    html: WtfGlobal.getLocaleText('pm.personalmessages.from.text')+' :&nbsp;&nbsp;&nbsp;&nbsp;' + from
                },{
                    height: 20,
                    html: WtfGlobal.getLocaleText('pm.common.TO')+' &nbsp;&nbsp;&nbsp; :&nbsp;&nbsp;&nbsp;&nbsp;' + to
                }]
            })
        };
        this.scope.fireEvent('showWindow', configObj, this.key);
    },

    DeleteLinkOnMouseClick: function(LinkId) {
        var linkObj = new String(LinkId);
        var plannerGrid = this.containerPanel.editGrid;
        if (linkObj.indexOf('_') > 0) {
            var count = 0;
            this.deleteLink(linkObj, true);
            var linkPanel = Wtf.get(LinkId);
            if(linkPanel)
                linkPanel.removeListener('dblclick', this.LinkPanelClick);
            var pred = Wtf.getCmp(linkObj.split('_')[2]).predecessor;
            for(var temp in pred) {
                count++;
                break;
            }
            if (count == 0)
                this.setOldPositionOf_ToTask((linkObj.split('_'))[2]);
            this.mysubtask((linkObj.split('_'))[2]);
            this.RecursionFunToTraceLink((linkObj.split('_'))[2]);
            if(this.updatedTaskIds.length > 0)
                plannerGrid.updatedTasks_ServerRequest(this.updatedTaskIds);
            if(this.updatedParentTaskIds.length > 0)
                plannerGrid.updatedParentTasks_ServerRequest(this.updatedParentTaskIds);
        }
    },

    ChartPanelmouseup: function(event){
        this.jg.clear();
        if (Wtf.get('Panel_0') != null) {
            Wtf.getCmp(this.FromTaskId).setProxyPanelPosition('Panel_0');
            this.remove(Wtf.getCmp('Panel_0'),true);
        }
        this.el.replaceClass('chartPanelNewCursor', 'chartPanelOldCursor');
        if (this.FromTaskId != null)
            Wtf.get(this.FromTaskId).removeClass('newTaskPanelStyle');
        this.x1 = this.x2 = this.y1 = this.y2 = 0;
        this.oldx = 0;
        this.FromTaskId = null;
    },

    setPanelPositionForNonWorkingDate: function(XPosition, TaskId, userChoice){
        var panelObj = Wtf.getCmp(TaskId);
        panelObj.Xpos = XPosition;
        panelObj.setPosition(panelObj.Xpos, panelObj.Ypos);
        if (userChoice == 1)
            panelObj.updatePanel();
        else
            panelObj.UpdateStEndForNonWorking(TaskId);
        this.mysubtask(TaskId);
        this.RecursionFunToTraceLink(TaskId);
        if(this.updatedTaskIds.length > 0)
             this.containerPanel.editGrid.updatedTasks_ServerRequest(this.updatedTaskIds);
        if(this.updatedParentTaskIds.length > 0)
             this.containerPanel.editGrid.updatedParentTasks_ServerRequest(this.updatedParentTaskIds);
    },

    DeleteLinkOnPanelMove: function(XPosition, TaskId, userChoice) {
        this.SetPanel_ProjScale();
        var panelObj = Wtf.getCmp(TaskId);
        panelObj.Xpos = XPosition;
        panelObj.setPosition(XPosition, panelObj.Ypos);
        var taskWidth = this.TaskminWidth;
        var predecessor = panelObj.predecessor;
        for (var pred in predecessor) {
            var predPanel = Wtf.getCmp(pred);
            var tempx = predPanel.Xpos + predPanel.panelwidth;
            if (predPanel.panelwidth >= taskWidth || panelObj.el.dom.getAttribute('TaskType') == "milestone" || predPanel.el.dom.getAttribute('TaskType') == "milestone") {
                if (tempx >panelObj.Xpos) {
                    var stdate = new Date(this.StartDate);
                    stdate = stdate.add(Date.DAY, XPosition / taskWidth);
                    var dur = panelObj.findWorkingStEndDate(stdate,0);
                    panelObj.actualstartDate= stdate.add(Date.DAY,dur);
                    /*if (stdate.format('w') == 0)
                        stdate = stdate.add(Date.DAY,1);
                    else if (stdate.format('w') == 6)
                        stdate = stdate.add(Date.DAY,2);
                    panelObj.actualstartDate = stdate;*/
                    this.deleteLink('jg_' + pred + '_' + TaskId, true);
                    this.setOldPositionOf_ToTask(TaskId);
                }
            }
        }
        this.mysubtask(TaskId);
        this.RecursionFunToTraceLink(TaskId);
        if(this.updatedTaskIds.length > 0)
             this.containerPanel.editGrid.updatedTasks_ServerRequest(this.updatedTaskIds);
        if(this.updatedParentTaskIds.length > 0)
             this.containerPanel.editGrid.updatedParentTasks_ServerRequest(this.updatedParentTaskIds);
    },

    setOldPositionOf_ToTask: function(toTaskId) {
        this.SetPanel_ProjScale();
        var TaskPanel = Wtf.getCmp(toTaskId);
        var minWidth = TaskPanel.TaskminWidth;
        if(TaskPanel.timeDifference(TaskPanel.startDate,TaskPanel.actualstartDate)!=0) {
            var stDate = TaskPanel.actualstartDate;
            var duration = TaskPanel.duration;
            TaskPanel.startDate = stDate;
            TaskPanel.endDate = stDate.add(Date.DAY, (duration - 1));
            var diff = TaskPanel.timeDifference(new Date(stDate), this.StartDate);
            if (document.getElementById(TaskPanel.id).getAttribute('TaskType') == null) {
                TaskPanel.Xpos = diff * this.TaskminWidth;
                TaskPanel.setPosition(TaskPanel.Xpos, TaskPanel.Ypos);
                TaskPanel.panelwidth = duration * this.TaskminWidth;
                TaskPanel.el.dom.style.width = TaskPanel.panelwidth + "px";
            } else {
                TaskPanel.Xpos = (diff * this.TaskminWidth) - 5;
                TaskPanel.setPosition(TaskPanel.Xpos, TaskPanel.Ypos);
            }
            TaskPanel.updatePanelOnDataLoad();
            var columnArray = [], valueArray = [];
            columnArray[0] = 'startdate'; valueArray[0] = TaskPanel.startDate;
            columnArray[1] = 'enddate'; valueArray[1] = TaskPanel.endDate;
            this.containerPanel.editGrid.UpdateGridRecord(columnArray, valueArray, toTaskId);
        } else if(parseFloat(TaskPanel.Xpos / minWidth) > parseInt(TaskPanel.Xpos / minWidth)) {
            TaskPanel.Xpos = Math.floor(parseFloat(TaskPanel.Xpos / minWidth)) * minWidth;
            if (document.getElementById(TaskPanel.id).getAttribute('TaskType') != null) {
                TaskPanel.Xpos = Math.floor(parseFloat(TaskPanel.Xpos / minWidth) + 1) * minWidth;
                TaskPanel.Xpos = TaskPanel.Xpos - 5;
            }
            TaskPanel.setPosition(TaskPanel.Xpos, TaskPanel.Ypos);
        }
        if(this.showOverDue) {
            TaskPanel.checkOverDue(true);
        }
    },

    initGraphics: function() {
        this.jg = new jsGraphics(this.id);
        var posArr = this.getPosition();
        this.CanvasX = posArr[0];
        this.CanvasY = posArr[1];
    },

    reassignLinks: function(/*recId*/) {
        var linkArray = this.linkArrayObj;
        for (var linkObj in linkArray) {
            var splitObj = linkObj.split('_');
            if(Wtf.getCmp(splitObj[1]).isVisible() && Wtf.getCmp(splitObj[2]).isVisible())
                this.AttachNewlink(linkObj, splitObj[1], splitObj[2],false);
        }
    },

    insertNewLinks: function(linkObj, map) {
        var plannerGrid = this.containerPanel.editGrid;
        for (var newlink in linkObj) {
            var str = new String(newlink);
            var splitstr = str.split('_');
            var newFrom = splitstr[1];
            var newTo = splitstr[2];
            var panelId = splitstr[1].substr(5);
            if (map[panelId] != null)
                newFrom = 'Panel' + map[panelId];
            panelId = splitstr[2].substr(5);
            if (map[panelId] != null)
                newTo = 'Panel' + map[panelId];
            if(Wtf.getCmp(newFrom) && Wtf.getCmp(newTo)) {
                plannerGrid.checkForValidLinkOnGanttChart(newFrom, newTo);
            }
        }
    },

    removeResizer: function(TaskId) {
        Wtf.get(TaskId).dom.firstChild.style.display = 'none';
    },

    showResources: function(resourceArray, taskIdArray, flag, store_RESOURCE) {
        var obj =null;
        var arrLength = taskIdArray.length;
        if (flag){
            for (var i = 0; i < arrLength; i++){
                var tPanel = Wtf.getCmp('Panel' + taskIdArray[i]);
                if (tPanel && resourceArray[i].length > 0) {
                    if (this.resourcePanelArray['PanelResource_' + tPanel.id]) {
                        obj = this.resourcePanelArray['PanelResource_' + tPanel.id];
                        obj.clear();
                    } else {
                        var tempPanel = new Wtf.Panel({
                            renderTo: this.id,
                            baseCls: "linkPanel",
                            id: 'PanelResource_' + tPanel.id
                        });
                        obj = new jsGraphics('PanelResource_' + tPanel.id);
                        this.resourcePanelArray['PanelResource_' + tPanel.id] = obj;
                    }
                    var resStr = this.createResString(resourceArray[i],store_RESOURCE);
                    obj.setFont("tahoma,verdana,arial,geneva,helvetica,sans-serif", "10px", Font.BOLD);
                    obj.drawString(resStr, tPanel.Xpos + tPanel.panelwidth + 7, tPanel.Ypos - 5);
                    if(!(Wtf.getCmp('Panel' + taskIdArray[i]).hidden == true)){
                        obj.paint();
                    }/* else
                        obj.paint();*/
                    this.recourceColorCode(resStr,tPanel.id,store_RESOURCE);
                }
            }
        } else {
            for (var i = 0; i < arrLength; i++) {
                var resourcePanelId = 'PanelResource_Panel' + taskIdArray[i];
                if (this.resourcePanelArray[resourcePanelId]){
                    obj = this.resourcePanelArray[resourcePanelId];
                    obj.clear();
                    delete this.resourcePanelArray[resourcePanelId];
                    Wtf.getCmp(resourcePanelId).destroy();
                }
                var TaskPanel = 'Panel'+taskIdArray[i];
                if (this.resourceColorArray[TaskPanel]) {
                    obj = this.resourceColorArray[TaskPanel];
                    obj.clear();
                    delete this.resourceColorArray[TaskPanel];
                }
            }
        }
    },
    recourceColorCode : function(resourceArray,taskid,store_RESOURCE) {
        var width  = Wtf.getCmp(taskid).panelwidth;
        var obj = null;
        if (this.resourceColorArray[taskid]) {
             obj = this.resourceColorArray[taskid];
             obj.clear();
        } else {
            obj = new jsGraphics(taskid);
            this.resourceColorArray[taskid] = obj;
        }
        var resources = [];
        if (resourceArray.indexOf(',') != -1)
            resources = resourceArray.split(',');
        else
            resources[0] = resourceArray;
        var numOfRes = resources.length;
        var steps = 0;
        if(numOfRes < 3 || numOfRes>5)
            steps = 12/numOfRes;
        else if(numOfRes < 5)
            steps =parseInt((10+numOfRes)/numOfRes);
        else
            steps = 2;
        for(var i = 0; i < numOfRes; i++) {
            var index = this.searchInResource(resources[i],store_RESOURCE,"nickname")
            var record = store_RESOURCE.getAt(index);
            obj.setColor(record.data['colorcode']);
            obj.setStroke(steps);
            if(numOfRes == 1) {
                obj.setStroke(12);
                obj.drawLine(0,-1,width-7,-1);
            } else if(numOfRes < 5)
                obj.drawLine(0,i*steps-1,width-3,(i*steps)-1);
            else
                obj.drawLine(0,i*steps-1,width-2,(i*steps)-1);
            obj.paint();
        }
    },

    createResString : function(resIds,Store) {
        var resArr = resIds.split(",");
        var resStr = "";
        for(var cnt =0; cnt<resArr.length;cnt++) {
            if(Store.getAt(this.searchInResource(resArr[cnt],Store,"resourceid")))
                resStr += Store.getAt(this.searchInResource(resArr[cnt],Store,"resourceid")).data.nickname;
            else
                resStr += resArr[cnt];

            if(cnt < resArr.length-1)
                resStr +=",";
        }
        return resStr;
    },

    searchInResource : function(ID,store,searchFor) {
        var index =  store.findBy(function(record) {
            if(record.get(searchFor)==ID)
                return true;
            else
                return false;
         });
        if(index == -1)
            return null;
        return index;
    },
    setTaskProgress : function(record) {
        Wtf.getCmp('Panel'+record.data['taskid']).setProgress(record);
    },

    showTaskProgressStatus: function(taskPanel, taskStatus){
        if(taskStatus != ""){
            obj = new jsGraphics(taskPanel.id);
            var taskWidth = taskPanel.panelwidth;
            this.panelStatusArr[taskPanel.id] = obj;
            if(taskStatus == "completed")
                obj.drawImage("../../images/critical-completed.jpg",0,3,taskWidth,5);
            else if(taskStatus == "inprogress")
                obj.drawImage("../../images/critical-inprogress.jpg",0,3,taskWidth,5);
            else if(taskStatus == "overdue")
                obj.drawImage("../../images/critical-overdue.jpg",0,3,taskWidth,5);
            obj.paint();
        }
    },

    showTaskProgress : function(taskIDSArray,flag) {
        this.SetPanel_ProjScale();
        var grid = this.containerPanel.editGrid;
        if(flag) {
            var taskWidth = this.TaskminWidth;
            var arrLength = taskIDSArray.length;
            for(var i = 0; i < arrLength; i++) {
                var TaskPanel  = 'Panel'+taskIDSArray[i];
                if (this.taskProgressArray[TaskPanel]) {
                    obj = this.taskProgressArray[TaskPanel];
                    obj.clear();
                } else {
                   obj = new jsGraphics(TaskPanel);
                   this.taskProgressArray[TaskPanel] = obj;
                }
                var taskPanel = Wtf.getCmp(TaskPanel);
                if(taskPanel.progressInPixel!=0) {
                    var taskProgress =  Math.round(taskPanel.progressInPixel);
                    var stdate = new Date(this.StartDate);
                    var enddate = new Date();
                    stdate = stdate.add(Date.DAY, taskPanel.Xpos / taskWidth);
                    var endPos = taskPanel.Xpos + taskProgress;
                    if(parseFloat(endPos/taskWidth)>parseInt(endPos/taskWidth))
                          endPos = endPos + taskWidth;
                    enddate = (this.StartDate).add(Date.DAY, parseInt(endPos / taskWidth) - 1);
                    /*var nonworkingdays = 0;
                    if(stdate.format("w")==6) {
                       stdate = stdate.add(Date.DAY,2);
                       nonworkingdays = 1;
                    }
                    else if(stdate.format("w")==0)
                       stdate = stdate.add(Date.DAY,1);*/
                   var nonworkingdays = taskPanel.sdateForNonWorkCal(stdate,-1);
                   if(nonworkingdays !=-1)
                       stdate = stdate.add(Date.DAY, nonworkingdays + 1);
                   else
                       nonworkingdays = 0;

                    var calprogress = parseInt(taskPanel.progressInPixel);
                    if (taskPanel.timeDifference(enddate, stdate) < 0){
                        if(nonworkingdays > 0 && taskPanel.progressInPixel>taskWidth) {
                            calprogress += (nonworkingdays*taskWidth);
                        }
                    } else {
                        nonworkingdays = (taskPanel.NonworkingDaysBetDates(stdate,enddate) + nonworkingdays ) * taskWidth;
                        calprogress += nonworkingdays;
                    }
                    obj.drawImage("../../images/taskprogress.png",0,2,calprogress,5);
                    obj.paint();
                }
            }
        } else {
            for (var TaskPanel in this.taskProgressArray) {
               if (this.taskProgressArray[TaskPanel]) {
                   obj = this.taskProgressArray[TaskPanel];
                   var temp = this.taskProgressArray[TaskPanel].cont.id.toString();
                   var rowval = grid.search(temp.substr(5));
                   var record = grid.dstore.data.items[rowval];
                   if(record.data['ismilestone'] == false){
                        obj.clear();
                        delete this.taskProgressArray[TaskPanel];
                   } else {
                        delete this.taskProgressArray[TaskPanel];
                   }
               }
            }
        }
    },

    showOverdueTask: function() {
        if(this.showOverDue) {
            this.showOverDue = false;
        } else if(!this.showOverDue) {
            this.showOverDue = true;
        }
        var getGridStore = this.containerPanel.editGrid.dstore;
        var count = getGridStore.getCount();
        for(var i = 0; i < count - 1; i++) {
            var taskPanel = Wtf.getCmp('Panel' + getGridStore.getAt(i).data.taskid);
            if(taskPanel && !taskPanel.el.hasClass('ParentTask') && !taskPanel.el.hasClass('milestoneIcon') && !taskPanel.el.hasClass('milestoneWeekIcon')) {
                taskPanel.checkOverDue(this.showOverDue);
            }
        }
    },

    clearChart: function() {
        var removeArray = [];
        var itemLength = this.items.length;
        for(var iCnt = 1; iCnt < itemLength; iCnt++)
            removeArray[removeArray.length] = this.items.items[iCnt].id;
        var arrLength = removeArray.length;
        for(var iCnt = 0; iCnt < arrLength; iCnt++)
            this.remove(removeArray[iCnt], true);

        for(linkKey in this.linkArrayObj) {
            var obj = this.linkArrayObj[linkKey];
            obj.clear();
            delete this.linkArrayObj[linkKey];
        }
    },

    clearSlackPanels: function() {
        var removeArray = [];
        var itemLength = this.items.length;
        for(var iCnt = 1; iCnt < itemLength; iCnt++){
            var id = this.items.items[iCnt].id
            if(id.indexOf('Slack') != -1)
                removeArray[removeArray.length] = id;
            if(id.indexOf('Panel') != -1){
                var tpanel = Wtf.getCmp('Panel'+id.substr(5));
                if(tpanel)
                    tpanel.setViewMode(false);
            }
        }
        var arrLength = removeArray.length;
        for(iCnt = 0; iCnt < arrLength; iCnt++){
            this.remove(removeArray[iCnt], true);
        }
        for(linkKey in this.slackDurObj) {
            var obj = this.slackDurObj[linkKey];
            if(obj.clear){
                obj.clear();
                delete this.slackDurObj[linkKey];
            }
        }
        this.slackDurObj = [];
        for (var taskStatusPanel in this.panelStatusArr) {
            if (this.panelStatusArr[taskStatusPanel]) {
                this.panelStatusArr[taskStatusPanel].clear();
                delete this.panelStatusArr[taskStatusPanel];
            }
        }
        this.panelStatusArr ={};
    }
});
