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
Wtf.proj.projCompare = function(conf){
    Wtf.apply(conf);
    Wtf.proj.projCompare.superclass.constructor.call(this, conf);
}

Wtf.extend(Wtf.proj.projCompare, Wtf.Panel, {
    initComponent: function(conf){
        Wtf.proj.projCompare.superclass.initComponent.call(this, conf);
        var baselineRec = Wtf.proj.common.taskRecord;
        this.baselineReader = new Wtf.data.KwlJsonReader({
        }, baselineRec);
        this.baselineStore = new Wtf.data.Store({
            reader : this.baselineReader
        });
        this.projectReader = new Wtf.data.KwlJsonReader({
        }, baselineRec);
        this.projectStore = new Wtf.data.Store({
            reader : this.projectReader
        });
        this.baselinePanel = new Wtf.proj.baselinePanel({
            compare: true,
            compareElement: "baseline",
            title: "[ "+ WtfGlobal.getLocaleText('pm.common.baseline.text') +"] " + this.baselineData.baselinename,
            id: "comparision_baseline_" + this.baselineData.baselineid,
            baselineData: this.baselineData,
            projectid: this.projectid,
            baselineStore: this.baselineStore,
            iconCls: "dpwnd baselineiconTab",
            region: "west",
            cls: "dportlet",
            split: true
        });
        this.projPanel = new Wtf.proj.baselinePanel({
            compare: true,
            compareElement: "project",
            title: "[ "+ WtfGlobal.getLocaleText('pm.project.text') +"] " + this.projectName,
            id: "comparision_project_" + this.baselineData.baselineid,
            baselineData: this.baselineData,
            baselineStore: this.projectStore,
            projectid: this.projectid,
            iconCls: "pwnd projectTabIcon",
            region: "center",
            split: true
        });
        this.colorPanel = new Wtf.Panel({
            region: "north",
            html: '<div class="Someclass" style = "background-color:#6187B9;height:25px;border:none;color:#FFFFFF;font-size:8pt;font-weight:bold;padding-bottom:2px;padding-top:2px;"><span style="margin-top:5px;margin-left:20px;display: inline-block; height:12px; width:12px; background-color:#FEFFB2;"></span> - '+WtfGlobal.getLocaleText('pm.project.plan.baseline.compare.updated')+'<span style="margin-top:5px;margin-left:20px;display: inline-block; height:12px; width:12px; background-color:#E0FFE0;"></span> - '+WtfGlobal.getLocaleText('pm.project.plan.baseline.compare.new')+'<span style="margin-top:5px;margin-left:20px;display: inline-block; height:12px; width:12px; background-color:#FFE3E3;"></span> - '+WtfGlobal.getLocaleText('pm.project.plan.baseline.compare.deleted')+'</div>'
        });
        this.projPanel.baselineGrid.getStore().on("load",function(){
            var temp1 = this.projectStore.data;
            var y = "#FEFFB2", r="#FFE3E3", g="#E0FFE0";
            var change = 0;
            var temp2 = this.baselineStore.data;
            for(var i=0; i< temp1.length; i++){
                var data1 = temp1.items[i].data;
                var data2 = temp2.items[i].data;
                if(data1.taskindex != "" && data2.taskindex != ""){
                    if(data1.startdate != ""){
                        if(data1.taskname!=data2.taskname || data1.duration!=data2.duration || data1.startdate.format('Y-m-d') != data2.startdate.format('Y-m-d') ||
                                data1.enddate.format('Y-m-d') != data2.enddate.format('Y-m-d') || data1.percentcomplete!=data2.percentcomplete){
                                change = 0;//updated in project
                                this.highlightRow(this.projPanel.baselineGrid,i, y);
                                this.highlightRow(this.baselinePanel.baselineGrid,i, y);
                        }
                    }
                }else if(data2.taskindex == ""){
                    change = 2;//new added in project
                    this.highlightRow(this.projPanel.baselineGrid,i, g);
                }else{
                    change = 1;//deleted in project
                    this.highlightRow(this.baselinePanel.baselineGrid,i, r);
                }
            }
            mainPanel.loadMask.hide();
        }, this);
        this.baselinePanel.baselineGrid.on("bodyscroll", function(scrollLeft, scrollTop){
            this.projPanel.baselineGrid.getView().scroller.dom.scrollLeft = scrollLeft;
            this.projPanel.baselineGrid.getView().scroller.dom.scrollTop = scrollTop;
        }, this);
        this.projPanel.baselineGrid.on("bodyscroll", function(scrollLeft, scrollTop){
            this.baselinePanel.baselineGrid.getView().scroller.dom.scrollLeft = scrollLeft;
            this.baselinePanel.baselineGrid.getView().scroller.dom.scrollTop = scrollTop;
        }, this);
        this.cPanel = new Wtf.Panel({
            layout: 'border',
            border:false,
            items: [this.colorPanel, this.baselinePanel, this.projPanel]
        });
        this.fetchData();
        this.projPanel.on('refresh', this.fetchData, this);
    },
    fetchData: function(){
        Wtf.Ajax.requestEx({
            url: Wtf.req.prj + "baseline.jsp",
            method: 'GET',
            params: {
                action: 5,
                baselineid: this.baselineData.baselineid,
                projectid: this.projectid
            }
        }, this,
        function(response, request){
            var obj = eval("(" + response + ")");
            if(obj.success){
                this.baselineStore.loadData(obj.data1);
                this.projectStore.loadData(obj.data2);
            } else {
                mainPanel.loadMask.hide();
                msgBoxShow(4, 1);
            }
        },function(response, request){
            msgBoxShow(4, 1);
        });
    },
    
    highlightRow: function(grid, row, color){
        var rowEl = grid.getView().getRow(row);
        var cell = grid.getView().getCell(row, 0);
        rowEl.style.backgroundColor = color;
        cell.style.backgroundColor = '#FFFFFF';
    }, 
    onRender: function(conf){
        Wtf.proj.projCompare.superclass.onRender.call(this, conf);
        this.add(this.cPanel);
        mainPanel.loadMask.msg = WtfGlobal.getLocaleText("pm.loading.text")+"...";
        mainPanel.loadMask.show();
    }
});
