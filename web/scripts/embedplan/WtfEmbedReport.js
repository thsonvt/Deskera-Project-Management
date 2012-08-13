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
Wtf.onReady(function(){
    var reportPanel = new Wtf.Panel({
        region: 'center',
        id: id + '_' +rid + '_reportPanel',
        layout: 'fit',
        autoDestroy: true,
        autoScroll: true,
        iconCls : "pwnd projplan",
        cls: 'graphbg',
        border: false,
        frame: false
    })
    var viewport = new Wtf.Viewport({
        layout: 'border',
        items: [
            new Wtf.BoxComponent({
                region: 'north',
                el: 'header'
            }),
            reportPanel
        ]
    });
    var container = new Wtf.EmbedReport({
        id: id + "_" +rid,
        autoHeight: true,
        autoWidth: true,
        frame: false,
        border: true
    });
    reportPanel.add(container);
    container.doLayout();
    reportPanel.doLayout();
});

Wtf.EmbedReport = function(config) {
    Wtf.apply(this,config);
    this.pid = id;
    Wtf.EmbedReport.superclass.constructor.call(this);
}

Wtf.extend(Wtf.EmbedReport, Wtf.Panel, {

    initComponent: function(){
        Wtf.EmbedReport.superclass.initComponent.call(this);
    },

    afterRender: function(){
        Wtf.EmbedReport.superclass.afterRender.call(this);
        this.createCharts();
    },

    createCharts: function(){
        var cdata = WtfReport.getDataForChart(rid, pc, this.pid, bid, d1, d2, 'chart');
        Wtf.get("projectname").dom.innerHTML = projname+" - "+rptname;
        for(var c = 0; c < cdata.length; c++){
            var tempDiv = document.createElement("div");
            var f = cdata[c].chartFlash;
            var s = cdata[c].chartSetting;
            var d = cdata[c].dataURL;
            if(f === undefined || s === undefined){
                f = "scripts/bar chart/krwcolumn.swf";
                s = "scripts/bar chart/krwcolumn_settings.xml";
            }
            d += '&userid='+u;
            tempDiv.style.height = this.getChartDivHeight(cdata.length);
            tempDiv.id = this.id + "_chart-gen-" + c;
            this.body.dom.appendChild(tempDiv);
            createNewChart(f, tempDiv.id + "_chart" + c, '98%', '98%', '8', '#ffffff', s, d, this.id + "_chart-gen-" + c);
        }
    },

    getChartDivHeight: function(noOfCharts){
        var ht = (noOfCharts == 1) ? "450px" : "470px";
        if(rid == 'projectcharts')
            ht = '250px';
        return ht;
    }
});
