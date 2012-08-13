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
Wtf.selectTempWin=function(config){
    Wtf.apply(this,config);

    var templateRec = Wtf.data.Record.create([
                {
                    name: 'tempid',
                    mapping: 'tempid'
                },
                {
                    name: 'tempname',
                    mapping: 'tempname'
                },
                {
                    name: 'description',
                    mapping: 'description'
                },
                {
                    name: 'configstr',
                    mapping: 'configstr'
                }
                ]);
            var template_ds = new Wtf.data.Store({
                url: Wtf.req.prj + 'template.jsp?projid='+this.pid+'&action=getallReportTemp',
                method: 'GET',
                reader: new Wtf.data.KwlJsonReader({
                    root: 'data'
                },templateRec)
            });
        var namePanel = new Wtf.grid.GridPanel({
            id:'templateName',
            autoScroll: true,
            enableColumnResize:false,
            border:false,
            viewConfig:{
                forceFit:true
            },
            cm: new Wtf.grid.ColumnModel([
                new Wtf.grid.RowNumberer, {
                    header:WtfGlobal.getLocaleText('lang.name.text'),
                    dataIndex: 'tempname'
                }]),
            ds: template_ds,
            height:180
        });
        var defaultPreview="";
        namePanel.on('cellclick',function(gridObj, ri, ci, e){
            var config = gridObj.getStore().getAt(ri).data['configstr'];

            var configstr = eval('('+config+')');
            var title = configstr["title"];
            var subtitle =configstr["subtitles"];
            var starr = subtitle.split("~");
            var subtitles = "";
            for(var i=0;i< starr.length;i++)
               subtitles += "<div>"+starr[i]+"</div>";

            var textColor = "#"+configstr["textColor"];
            var bgColor ="#"+configstr["bgColor"];

            var headdate = configstr["headDate"]=="true"?"<small>2009/01/01</small>":"";
            var footdate = configstr["footDate"]=="true"?"<small>2009/01/01</small>":"";

            var headnote = configstr["headNote"];
            var footnote = configstr["footNote"];

            var headpager = configstr["headPager"]=="true"?"1":"";
            var footpager = configstr["footPager"]=="true"?"1":"";

            var pageborder = configstr["pageBorder"]=="true"?"border:thin solid #666;":"";
            var gridborder = configstr["gridBorder"]=="true"?"1":"0";
            var displaylogo = configstr["showLogo"]=="true"?"block":"none";

            var pagelayoutPR = "height:300px;width:240px;margin:auto;";
            var pagelayoutLS = "height:240px;width:380px;margin:0px auto;";
            var pagelayout = configstr["landscape"]=="true"?pagelayoutLS:pagelayoutPR;

            var reportPreview = "<div style=\""+pagelayout+"align:center;color:"+textColor+";font-family:arial;padding:5px;font-size:12px;background:"+bgColor+";border-right:4px solid #DDD;border-bottom:4px solid #888\">" +
                                "<div style=\""+pageborder+"height:99%;width:99%;\">" +
                                    "<div style=\"border-bottom:thin solid #666;margin:0 2px;height:6%;width:98%;\">" +
                                        "<table border=0 width=100% style=\"font-size:12px\">" +
                                        "<tr><td align=\"left\" width=25%>"+headdate+"</td><td align=\"center\" >"+headnote+"</td><td align=\"right\" width=25%>"+headpager+"</td></tr>" +
                                        "</table>" +
                                    "</div>" +
                                    "<div style=\"margin:0 2px;height:86%;width:98%;text-align:center;overflow:hidden;\">" +
                                    "<div style=\"border-bottom:thin solid #666;\">" +
                                        "<div style=\"display:"+displaylogo+";position:absolute;font-size:16px;margin:1px 0 0 1px\"></div>" +
                                        "<div style=\"display:"+displaylogo+";position:absolute;color:#8080FF;font-size:16px\"></div>" +
                                        "<br/><div style=\"font-size:13px\"><b>"+title+"</b></div>" +
                                        subtitles + "<br/>"+
                                    "</div>" +
                                    "<table border="+gridborder+" width=90% cellspacing=0 style=\"font-size:12px;margin:5px auto;\">" +
                                    "<tr><td align=\"center\" width=10%><b>No.</b></td><td align=\"center\" width=20%><b>Index</b></td><td align=\"center\" width=45%><b>Task Name</b></td><td align=\"right\" width=25%><b>Resources</b></td></tr>" +
                                    "<tr><td align=\"center\">1.</td><td align=\"center\">31</td><td align=\"center\">Gather info.</td><td align=\"right\" >Thomas</td></tr>" +
                                    "<tr><td align=\"center\">2.</td><td align=\"center\">56</td><td align=\"center\">Documentation</td><td align=\"right\" >Jane,Alice</td></tr>" +
                                    "<tr><td align=\"center\">3.</td><td align=\"center\">78</td><td align=\"center\">Planning</td><td align=\"right\" >Darin</td></tr>" +
                                    "<tr><td align=\"center\">4.</td><td align=\"center\">90</td><td align=\"center\">Coding</td><td align=\"right\" >John</td></tr>" +
                                    "<tr><td align=\"center\">5.</td><td align=\"center\">111</td><td align=\"center\">Implemention</td><td align=\"right\">John</td></tr>" +
                                    "<tr><td align=\"center\">6.</td><td align=\"center\">112</td><td align=\"center\">Submission</td><td align=\"right\">John</td></tr>" +
                                    "</table>" +
                                    "</div>" +
                                    "<div style=\"border-top:thin solid #666;margin:0 2px;height:6%;width:98%;\">" +
                                        "<table border=0 width=100% style=\"font-size:12px\">" +
                                        "<tr><td align=\"left\" width=25%>"+footdate+"</td><td align=\"center\" >"+footnote+"</td><td align=\"right\" width=25%>"+footpager+"</td></tr>" +
                                        "</table>" +
                                    "</div>" +
                                "</div>" +
                                "</div>";


            var reportTmp = new Wtf.Template(reportPreview);
            reportTmp.overwrite(Wtf.getCmp("layoutpreview").body);
            Wtf.get('description').dom.innerHTML = gridObj.getStore().getAt(ri).data['description'];
        },this);
       
        template_ds.load();


        var templatePanel = new Wtf.Panel({
            id:'templatePanel',
            layout:'border',
            border:false,
            width:500,
            items:[{
                region:'center',
                width:'50%',
                border:false,
                layout:'fit',
                height:'100%',
                items:[namePanel]
            },{
                region:'east',
                width:410,
                border:false,
                layout: 'border',
                height:'100%',
                bodyStyle:"background:#EEEEEE",
                items:[{
                    layout:'fit',
                    xtype:'fieldset',
                    cls: 'textAreaDiv',
                    region: 'center',
                    preventScrollbars:false,
                    frame:true,
                    border:false,
                    id:'layoutpreview',
                    html:"<div style='font-size:14px;margin-top:175px;text-align:center;'>"+WtfGlobal.getLocaleText("pm.project.plan.reports.export.pdf.template.emptytext")+"</div>"
                }, {
                    region: 'south',
                    border: false,
                    height: 85,
                    bodyStyle: 'background:white;font-size:12px;border:2px solid #bfbfbf;',
                    html: '<div style="clear:both;margin:2px 5px;">Description - </div><div id="description" class="permDesc"></div>'
                }]
            }]
        });
        var configstr="";
        this.templateWindow = new Wtf.Window({
                title:WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.existing'),
                modal:true,
                iconCls : 'iconwin',
                layout:'fit',
                items:[templatePanel],
                resizable:false,
                autoDestroy:true,
                height:this.height,
                width:this.width,
                buttons:[{
                    text:WtfGlobal.getLocaleText('lang.export.text'),
                    handler:function() {
                        var smTmp = namePanel.getSelectionModel();
                        
                        if(smTmp.getCount()<1){
                            msgBoxShow(239, 1);
                            return;
                        } else {
                            configstr=smTmp.getSelected().data['configstr'];
                            smTmp.clearSelections();
                            var configObj = eval('('+configstr+')');
                            var repColModel = this.reportGrid.getColumnModel();
                            var numCols = this.reportGrid.getColumnModel().getColumnCount();
                            var sep="";
                            var gridData="";
                            for(var i = 1;i<numCols;i++){ // skip row numberer
                                if(!(repColModel.isHidden(i))){
                                    gridData += sep + repColModel.config[i].colRealName;
                                    sep="~";
                                }
                            }
                            var colHead=new Array();
                            var headers=gridData.split("~");
                            if(configObj.colWidth.data.length == headers.length || this.reportType == 2){
                                var newurl = this.url+"&config="+configstr;
                                setDldUrl(newurl);
                                this.templateWindow.close();
                           }
                           else
                               msgBoxShow(317, 1);
                        }
                    },
                    scope: this
                },{
                    text:WtfGlobal.getLocaleText('lang.new.create'),
                    handler:function() {
                       var custForm=new Wtf.customReport({
                                autoScroll: true,
                                border: false,
                                width:'99%',
                                bodyStyle : 'background:white;',
                                id:'custForm'+this.id + this.tabtitle,
                                reportGrid:this.reportGrid,
                                reportType:this.reportType,
                                url:this.url
                            });
                            var eobj = Wtf.getCmp(this.id + "_buildReport"+ this.tabtitle);
                            var cont = Wtf.getCmp("subtabpanelcomprojectTabs_"+this.pid);//this.ownerCt.ownerCt;
                            if(eobj === undefined){
                                eobj = new Wtf.reportBuilder.builderPanel({
                                    title: WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.text'),
                                    id: this.id + "_buildReport" + this.tabtitle,
                                    closable: true,
                                    autoScroll: true,
                                    formCont: custForm
                                });
                                cont.add(eobj);
                            }
                            this.templateWindow.close();
                            cont.setActiveTab(eobj);
                            cont.doLayout();
                    },
                    scope: this
                },
                {
                    text:WtfGlobal.getLocaleText('lang.cancel.text'),
                    handler:function() {
                        this.templateWindow.close();
                    },
                    scope: this
                }]
            });
            this.templateWindow.show();

    Wtf.selectTempWin.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.selectTempWin,Wtf.Window,{
    onRender: function(conf){
        Wtf.selectTempWin.superclass.onRender.call(this, conf);
        this.add(this.templateWindow);
    }
});
