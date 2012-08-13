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
WtfReport = {
     getColumnModel : function(action, percent){
        var gCol = new Wtf.grid.ColumnModel([
                new Wtf.grid.RowNumberer,
                {
                    header: WtfGlobal.getLocaleText('pm.project.plan.task.attributes.index'),
                    dataIndex: 'taskindex',
                    hidden: false,
                    align: 'center',
                    colRealName: 'Task Index'
                },{
                    id: 'taskname',
                    hidden: false,
                    header: WtfGlobal.getLocaleText('pm.common.taskname'),
                    dataIndex: 'taskname',
                    align: 'left',
                    colRealName: 'Task Name'
                },{
                    header: WtfGlobal.getLocaleText('pm.project.resource.name'),
                    hidden: false,
                    dataIndex: 'resourcename',
                    align: 'left',
                    colRealName: 'Resource Names'
                },{
                    header: WtfGlobal.getLocaleText('pm.common.duration'),
                    hidden: false,
                    dataIndex: 'duration',
                    align: 'right',
                    renderer:this.rendererForDuration,
                    colRealName: 'Duration'
                },{
                    header: WtfGlobal.getLocaleText('pm.project.calendar.newevent.startdate'),
                    hidden: false,
                    dataIndex: 'startdate',
                    align: 'right',
                    renderer: function(v){
                        return (v && typeof v == 'object') ? v.format(WtfGlobal.getOnlyDateFormat()) : v;
                    },
                    colRealName: 'Start Date'
                },{
                    header: WtfGlobal.getLocaleText('pm.common.enddate'),
                    hidden: false,
                    dataIndex: 'enddate',
                    align: 'right',
                    renderer: function(v){
                        return (v && typeof v == 'object') ? v.format(WtfGlobal.getOnlyDateFormat()) : v;
                    },
                    colRealName: 'End Date'
                },{
                    header: WtfGlobal.getLocaleText('pm.project.plan.task.attributes.pc'),
                    hidden: false,
                    dataIndex: 'percentcomplete',
                    align: 'right',
                    colRealName: 'Percent Complete'
                },{
                    header: WtfGlobal.getLocaleText('pm.project.plan.predecessor'),
                    hidden: false,
                    dataIndex: 'predecessor',
                    align: 'right',
                    colRealName: 'Predecessor'
                },{
                    header: WtfGlobal.getLocaleText('pm.project.resource.cost'),
                    dataIndex: 'cost',
                    hidden: true,
                    align: 'right',
                    colRealName: 'Total Cost'
                },{
                    header: WtfGlobal.getLocaleText('pm.project.plan.totalwork'),
                    dataIndex: 'work',
                    hidden: true,
                    align: 'right',
                    renderer:this.rendererForWork,
                    colRealName: 'Total Work'
                },{
                    header: WtfGlobal.getLocaleText('pm.dashboard.widget.project.overdueby'),
                    dataIndex: 'delay',
                    hidden: true,
                    align: 'right',
                    colRealName: 'Overdue By'
                },{
                    header: WtfGlobal.getLocaleText('pm.dashboard.widget.project.overdueexp'),
                    dataIndex: 'expence',
                    hidden: true,
                    align: 'right',
                    colRealName: 'Overdue Expense'
                },{
                    header: WtfGlobal.getLocaleText('lang.project.plan.cpa.duratiionvariance'),
                    dataIndex: 'durvary',
                    hidden: true,
                    align: 'right',
                    colRealName: 'Duration Variance'
                },{
                    header: WtfGlobal.getLocaleText('pm.project.plan.cpa.workvariance'),
                    dataIndex: 'hoursvary',
                    hidden: true,
                    align: 'right',
                    colRealName: 'Work Variance'
                },{
                    header: WtfGlobal.getLocaleText('pm.project.cost.actual'),
                    dataIndex: 'actualcost',
                    hidden: true,
                    align: 'right',
                    colRealName: 'Actual Cost'
                },{
                    header: WtfGlobal.getLocaleText('pm.project.cost.variance'),
                    dataIndex: 'costvary',
                    hidden: true,
                    align: 'right',
                    colRealName: 'Cost Variance'
                },{
                    header: WtfGlobal.getLocaleText('pm.project.plan.cpa.sdv'),
                    dataIndex: 'startdiff',
                    hidden: true,
                    align: 'right',
                    colRealName: 'Start Date Variance'
                },{
                    header: WtfGlobal.getLocaleText('pm.project.plan.cpa.edvariance'),
                    dataIndex: 'enddiff',
                    hidden: true,
                    align: 'right',
                    colRealName: 'End Date Variance'
                },{
                    header: WtfGlobal.getLocaleText('pm.project.resource.baseline'),
                    dataIndex: 'baseresources',
                    hidden: true,
                    align: 'left',
                    colRealName: 'Baseline Resources'
                }]);
        switch(action){
            case 'progressreport':
                    if(percent == -99){
                        gCol.setHidden(9, false);
                        gCol.setHidden(10, false);
                        gCol.setColumnHeader(9, WtfGlobal.getLocaleText('pm.project.cost.actual'));
                        gCol.setColumnHeader(10, WtfGlobal.getLocaleText('pm.project.task.actual'));
                        this.setColumnRealName(gCol, 9, WtfGlobal.getLocaleText('pm.project.cost.actual'));
                        this.setColumnRealName(gCol, 10, WtfGlobal.getLocaleText('pm.project.task.actual'));
                    } else if(percent == 0){
                        gCol.setHidden(6, true);
                        gCol.setHidden(7, true);
                    } else if(percent == 100){
                        gCol.setHidden(6, true);
                        gCol.setHidden(7, true);
                        gCol.setHidden(9, false);
                        gCol.setHidden(10, false);
                        gCol.setColumnHeader(9, WtfGlobal.getLocaleText('pm.project.cost.actual'));
                        gCol.setColumnHeader(10, WtfGlobal.getLocaleText('pm.project.task.actual'));
                        this.setColumnRealName(gCol, 9, WtfGlobal.getLocaleText('pm.project.cost.actual'));
                        this.setColumnRealName(gCol, 10, WtfGlobal.getLocaleText('pm.project.task.actual'));
                    }
                    break;
            case 'enddatetasks':
                    gCol.setHidden(9, false);
                    gCol.setHidden(10, false);
                    gCol.setColumnHeader(9, WtfGlobal.getLocaleText('pm.project.cost.actual'));
                    gCol.setColumnHeader(10, WtfGlobal.getLocaleText('pm.project.task.actual'));
                    break;
            case 'startdatetasks':
                    gCol.setHidden(9, false);
                    gCol.setHidden(10, false);
                    gCol.setColumnHeader(9, WtfGlobal.getLocaleText('pm.project.cost.actual'));
                    gCol.setColumnHeader(10, WtfGlobal.getLocaleText('pm.project.task.actual'));
                    break;
            case 'toplevel':
                    gCol.setHidden(9, false);
                    gCol.setHidden(10, false);
                    gCol.setHidden(4, true);
                    gCol.setColumnHeader(9, WtfGlobal.getLocaleText('pm.project.cost.actual'));
                    gCol.setColumnHeader(10, WtfGlobal.getLocaleText('pm.project.task.actual'));
                    break;
            case 'datecompare':
                    gCol.setHidden(3, true);
                    gCol.setHidden(4, true);
                    gCol.setHidden(5, false);
                    gCol.setHidden(6, false);
                    gCol.setHidden(7, true);
                    gCol.setHidden(8, true);
                    gCol.setHidden(17, false);
                    gCol.setHidden(18, false);
                    break;
            case 'costcompare':
                    gCol.setHidden(3, true);
                    gCol.setHidden(4, true);
                    gCol.setHidden(5, true);
                    gCol.setHidden(6, true);
                    gCol.setHidden(7, true);
                    gCol.setHidden(8, true);
                    gCol.setHidden(9, false);
                    gCol.setHidden(15, false);
                    gCol.setHidden(16, false);
                    break;
            case 'durationcompare':
                    gCol.setHidden(3, true);
                    gCol.setHidden(4, false);
                    gCol.setHidden(5, true);
                    gCol.setHidden(6, true);
                    gCol.setHidden(7, true);
                    gCol.setHidden(8, true);
                    gCol.setHidden(10, false);
                    gCol.setHidden(14, false);
                    gCol.setHidden(13, false);
                    break;
            case 'overdue':
                    gCol.setHidden(4, true);
                    gCol.setHidden(6, false);
                    gCol.setHidden(3, false);
                    gCol.setHidden(11, false);
                    gCol.setHidden(12, false);
                    break;
            case 'resourcecompare':
                    gCol.setHidden(4, true);
                    gCol.setHidden(5, true);
                    gCol.setHidden(6, true);
                    gCol.setHidden(7, true);
                    gCol.setHidden(8, true);
                    gCol.setHidden(19, false);
                    break;
            case 'milestones':
                    gCol.setHidden(4, true);
                    gCol.setHidden(6, true);
                    break;
        }
        return gCol;
    },

    setColumnRealName: function(colModel, colNo, newRealName){
        colModel.config[colNo].colRealName = newRealName;
    },

    getRecordCreate : function(){
        var reportRecord  = Wtf.data.Record.create([
            {
                name :'taskid'
            },{
                name :'taskindex'
            },{
                name :'taskname'
            },{
                name :'duration'
            },{
                name :'startdate',
                type: 'date',
                dateFormat: 'Y-m-d'
            },{
                name :'enddate',
                type: 'date',
                dateFormat: 'Y-m-d'
            },{
                name :'predecessor'
            },{
                name :'resourcename'
            },{
                name :'percentcomplete'
            },{
                name :'cost'
            },{
                name :'work'
            },{
                name :'delay'
            },{
                name :'expence'
            },{
                name :'hours'
            },{
                name :'durvary'
            },{
                name :'hoursvary'
            },{
                name :'actualcost'
            },{
                name :'costvary'
            },{
                name :'startdiff'
            },{
                name :'enddiff'
            },{
                name :'baseresources'
            },{
                name :'isparent'
            }]);
            return reportRecord;
    },

    getDataForChart : function(action, percent, pid, baselineid, date_1, date_2, base){
        var columnHeader = [];
        var columnKeys = [];
        switch(action){
            case 'progressreport':
                    if(percent == -99){
                        var cdata=[{
                            dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&percent="+percent+"&projid="+pid +"&type=taskcompletion",
                            chartSetting: 'scripts/bar chart/task_settings.xml',
                            chartFlash:  'scripts/bar chart/krwcolumn.swf'
                        },{
                            dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&percent="+percent+"&projid="+pid +"&type=taskcost",
                            chartSetting: 'scripts/bar chart/taskcost_settings.xml',
                            chartFlash:  'scripts/bar chart/krwcolumn.swf'
                        },{
                            dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&percent="+percent+"&projid="+pid +"&type=duration",
                            chartSetting: 'scripts/bar chart/taskduration_settings.xml',
                            chartFlash:  'scripts/bar chart/krwcolumn.swf'
                        }];
                        columnHeader = ['Duration','Actual Cost','Actual Work'];
                        columnKeys = ['pm.common.duration','pm.project.cost.actual','pm.project.task.actual'];
                    } else if(percent == 0){
                        cdata=[{
                            dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&percent="+percent+"&projid="+pid +"&type=duration",
                            chartSetting: 'scripts/bar chart/taskduration_settings.xml',
                            chartFlash:  'scripts/bar chart/krwcolumn.swf'
                        }];
                        columnHeader = ['Duration'];
                        columnKeys = ['pm.common.duration'];
                    } else if(percent == 100){
                        cdata=[{
                            dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&percent="+percent+"&projid="+pid +"&type=taskcost",
                            chartSetting: 'scripts/bar chart/taskcost_settings.xml',
                            chartFlash:  'scripts/bar chart/krwcolumn.swf'
                        },
                        {
                            dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&percent="+percent+"&projid="+pid +"&type=duration",
                            chartSetting: 'scripts/bar chart/taskduration_settings.xml',
                            chartFlash:  'scripts/bar chart/krwcolumn.swf'
                        }];
                        columnKeys = ['pm.common.duration','pm.project.cost.actual','pm.project.task.actual'];
                        columnHeader = ['Duration','Actual Cost','Actual Work'];
                    }
                    break;
            case 'enddatetasks':
                    cdata=[{
                        dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&date1="+date_1+"&date2="+date_2+"&projid="+pid +"&type=taskcompletion",
                        chartSetting: 'scripts/bar chart/task_settings.xml',
                        chartFlash:  'scripts/bar chart/krwcolumn.swf'
                    },{
                        dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&date1="+date_1+"&date2="+date_2+"&projid="+pid +"&type=taskcost",
                        chartSetting: 'scripts/bar chart/taskcost_settings.xml',
                        chartFlash:  'scripts/bar chart/krwcolumn.swf'
                    },{
                        dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&date1="+date_1+"&date2="+date_2+"&projid="+pid +"&type=duration",
                        chartSetting: 'scripts/bar chart/taskduration_settings.xml',
                        chartFlash:  'scripts/bar chart/krwcolumn.swf'
                    }];
                    columnKeys = ['pm.common.duration','pm.project.plan.task.attributes.pc','pm.project.cost.actual','pm.project.task.actual'];
                    columnHeader = ['Duration','Percent Complete','Actual Cost','Actual Work'];
                    break;
            case 'startdatetasks':
                     cdata=[{
                        dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&date1="+date_1+"&date2="+date_2+"&projid="+pid +"&type=taskcompletion",
                        chartSetting: 'scripts/bar chart/task_settings.xml',
                        chartFlash:  'scripts/bar chart/krwcolumn.swf'
                    },
                    {
                        dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&date1="+date_1+"&date2="+date_2+"&projid="+pid +"&type=duration",
                        chartSetting: 'scripts/bar chart/taskduration_settings.xml',
                        chartFlash:  'scripts/bar chart/krwcolumn.swf'
                    }];
                    columnKeys = ['pm.common.duration','pm.project.plan.task.attributes.pc','pm.project.cost.actual','pm.project.task.actual'];
                    columnHeader = ['Duration','Percent Complete','Actual Cost','Actual Work'];
                    break;
            case 'toplevel':
                    cdata=[{
                        dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&percent="+percent+"&projid="+pid +"&type=taskcompletion",
                        chartSetting: 'scripts/bar chart/task_settings.xml',
                        chartFlash:  'scripts/bar chart/krwcolumn.swf'
                    },{
                        dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&percent="+percent+"&projid="+pid +"&type=taskcost",
                        chartSetting: 'scripts/bar chart/taskcost_settings.xml',
                        chartFlash:  'scripts/bar chart/krwcolumn.swf'
                    },{
                        dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&percent="+percent+"&projid="+pid +"&type=duration",
                        chartSetting: 'scripts/bar chart/taskduration_settings.xml',
                        chartFlash:  'scripts/bar chart/krwcolumn.swf'
                    }];
                    columnKeys = ['pm.project.cost.actual','pm.project.task.actual'];
                    columnHeader = ['Actual Cost','Actual Work'];
                    break;
            case 'datecompare':
                    cdata=[{
                        dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&percent="+percent+"&projid="+pid +"&baselineid="+baselineid +"&type=startdatecompare",
                        chartSetting: 'scripts/bar chart/task_variance.xml',
                        chartFlash:  'scripts/bar chart/krwcolumn.swf'
                    },{
                        dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&percent="+percent+"&projid="+pid +"&baselineid="+baselineid +"&type=enddatecompare",
                        chartSetting: 'scripts/bar chart/task_variance.xml',
                        chartFlash:  'scripts/bar chart/krwcolumn.swf'
                    }];
                    columnKeys = ['pm.project.plan.cpa.sdv','pm.project.plan.cpa.edvariance'];
                    columnHeader = ['Start Date Variance','End Date Variance'];
                    break;
            case 'costcompare':
                    cdata=[{
                        dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&percent="+percent+"&projid="+pid +"&baselineid="+baselineid +"&type=costcompare",
                        chartSetting: 'scripts/bar chart/taskcost_settings.xml',
                        chartFlash:  'scripts/bar chart/krwcolumn.swf'
                    }];
                    columnKeys = ['pm.project.resource.cost','pm.project.cost.actual','pm.project.cost.variance'];
                    columnHeader = ['Total Cost','Actual Cost','Cost Variance'];
                    break;
            case 'durationcompare':
                     cdata=[{
                        dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&percent="+percent+"&projid="+pid +"&baselineid="+baselineid+"&type=durationcompare",
                        chartSetting: 'scripts/bar chart/task_variance.xml',
                        chartFlash:  'scripts/bar chart/krwcolumn.swf'
                    },{
                        dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&percent="+percent+"&projid="+pid +"&baselineid="+baselineid+"&type=workcompare",
                        chartSetting: 'scripts/bar chart/taskwork_variance.xml',
                        chartFlash:  'scripts/bar chart/krwcolumn.swf'
                    }];
                    columnKeys = ['lang.project.plan.cpa.duratiionvariance','pm.project.plan.cpa.workvariance'];
                    columnHeader = ['Duration Variance','Work Variance'];
                    break;
            case 'overdue':
                    cdata=[{
                        dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&percent="+percent+"&projid="+pid +"&type=overdue",
                        chartSetting: 'scripts/bar chart/taskoverdue_settings.xml',
                        chartFlash:  'scripts/bar chart/krwcolumn.swf'
                    },{
                        dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&percent="+percent+"&projid="+pid +"&type=overdueexpence",
                        chartSetting: 'scripts/bar chart/overdue_expence.xml',
                        chartFlash:  'scripts/bar chart/krwcolumn.swf'
                    }];
                    columnKeys = ['pm.project.plan.task.attributes.pc','pm.dashboard.widget.project.overdueby','pm.dashboard.widget.project.overdueexp'];
                    columnHeader = ['Percent Complete','Overdue By','Overdue Expense'];
                    break;
            case 'milestones':
                    cdata = [{
                        dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&percent="+percent+"&projid="+pid +"&type=taskcompletion",
                        chartSetting: 'scripts/bar chart/task_settings.xml',
                        chartFlash:  'scripts/bar chart/krwcolumn.swf'
                    }];
                    columnKeys= ['pm.project.plan.task.attributes.pc'];
                    columnHeader = ['Percent Complete'];
                    break;
            case 'projectcharts':
                    cdata = [{
                        dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&projid="+pid +"&type=projectcharts&subtype=progress",
                        chartSetting: 'scripts/bar chart/progresschart_settings.xml',
                        chartFlash:  'scripts/bar chart/krwcolumn.swf'
                    },{
                        dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&projid="+pid +"&type=projectcharts&subtype=time",
                        chartSetting: 'scripts/bar chart/timechart_settings.xml',
                        chartFlash:  'scripts/bar chart/krwcolumn.swf'
                    }];
                    break;
            case 'resanalysis':
                    cdata=[{
                        dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&date1="+date_1+"&date2="+date_2+"&projid="+pid +"&type=taskcost&curr=",
                        chartSetting: 'scripts/bar chart/taskcost_settings.xml',
                        chartFlash:  'scripts/bar chart/krwcolumn.swf'
                    },{
                        dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&date1="+date_1+"&date2="+date_2+"&projid="+pid +"&type=duration",
                        chartSetting: 'scripts/bar chart/resanalysis_settings.xml',
                        chartFlash:  'scripts/bar chart/krwcolumn.swf'
                    }];
                    columnKeys = ['pm.common.schedulework','pm.project.task.actual','pm.project.resource.schcost','pm.project.cost.actual'];
                    columnHeader = ['Scheduled Work','Actual Work','Scheduled Cost','Actual Cost'];
                    break;
            case 'resourcewisecompare':
                    cdata=[{
                        dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&projid="+pid+"&type=resourcewisecost&subtype=costvary&baselineid="+baselineid+"&curr=",
                        chartSetting: 'scripts/bar chart/reswise_cost.xml',
                        chartFlash:  'scripts/bar chart/krwcolumn.swf'
                    },{
                        dataURL: "../../jspfiles/profile/project/taskCompletionData.jsp?action="+action+"&projid="+pid +"&type=resourcewisecost&subtype=workvary&baselineid="+baselineid,
                        chartSetting: 'scripts/bar chart/reswise_work.xml',
                        chartFlash:  'scripts/bar chart/krwcolumn.swf'
                    }];
                    columnKeys = ['pm.project.plan.cpa.workvariance','pm.project.cost.variance'];
                    columnHeader = ['Work Variance','Cost Variance'];
                    break;
            case 'line':
                    var ch = '';
                    switch(date_2){
                        case 'timeline':
                            ch = "scripts/bar chart/restime_settings.xml";
                            break;
                        case 'costline':
                            ch = "scripts/bar chart/rescost_settings.xml";
                            break;
                    }
                    cdata=[{
                        dataURL: "../../jspfiles/profile/project/getResCostData.jsp?groupby="+date_1+"&reporttype="+date_2+"&projectid="+pid+"&period="+percent,
                        chartSetting: ch,
                        chartFlash: 'scripts/bar chart/krwcolumn.swf'
                    }];
                    break;
        }
        if(base == 'chart')
            return cdata;
        else if(base == 'keys')
            return columnKeys;
        else
            return columnHeader;
    },

    rendererForCost: function(v,a,b,c,d,e){
        var ret = "";
        var temp = v.toString();
        if(temp.indexOf(Wtf.CurrencySymbol) != -1)
            v = v.substr(0,2);
        if(v != 'NA')
            ret = Wtf.CurrencySymbol + " " + v;
        else
            ret = v;
        return ret;
    },

    rendererForWork: function(v,a,b,c,d,e){
        var ret = "";
        var temp = v.toString();
        if(temp.indexOf(' hrs') != -1)
            v = v.substr(0,v.indexOf(' hrs'));
        if(v != 'NA')
            ret = v + ' hr';
        else
            ret = v;
        return ret;
    },

    rendererForDuration: function(v,a,b,c,d,e){
        var ret = "";
        var temp = v.toString();
        if(temp.indexOf('d') != -1)
            v = v.substr(0,v.indexOf('d'));
        if(v != 'NA'){
            if(temp.indexOf('h') != -1)
                v = v.substr(0,v.indexOf('h'));
            if(temp.indexOf('h') != -1)
                ret = v + ' hours';
            else
                ret = v + ' days';
        } else
            ret = v;
        return ret;
    }
};
