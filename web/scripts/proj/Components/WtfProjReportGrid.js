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
Wtf.proj.reportGrid = function(conf){
    Wtf.apply(this, conf);
    var ieflag = (Wtf.isIE) ? true : false;
    this.chartDOM = document.createElement("div");
    this.chartDOM.className = "projectReportChartDiv";
    this.chartDOM.id = this.id + "_chartDiv";
    this.chartDiv = document.createElement("div");
    this.chartDiv.className = "reportGridShow";
    this.chartDiv.id = this.id + "_chartContainer";
//    var closeChart = document.createElement("div");
//    closeChart.id = this.id + "_closeChart";
//    closeChart.innerHTML = "[ X ]";
//    closeChart.className = "closeChartButton";
//    closeChart.onclick = this.closeChartClicked.createDelegate(this, []);
//    this.chartDiv.appendChild(closeChart);
    var note = document.createElement("div");
    note.id = this.id+"_currency-note";
    note.className = "reportCurrencyNote";
    note.innerHTML = WtfGlobal.getLocaleText({key:'pm.project.plan.reports.graph.coststext',params:Wtf.CurrencySymbol})
    this.chartDiv.appendChild(note);
    this.chartDiv.appendChild(this.chartDOM);
    conf.contentEl = this.chartDiv;
    this.baseCls = "reportPanel";
    var startdate = new Date();
    var enddate = new Date();
    if(this.columnScroll){
        var len = this.headerObj.length -1;
        startdate = (this.headerObj[0]['1'] == undefined) ? this.headerObj[0]['0'] : this.headerObj[0]['1'];
        enddate = (this.headerObj[len-1]['1'] == undefined) ? this.headerObj[len-1]['0'] : this.headerObj[len-1]['1'];
    }
    this.tbar = [{
         text: WtfGlobal.getLocaleText('pm.project.plan.reports.colorcoding'),
         iconCls: 'dpwnd colorcoder',
         scope: this,
         hidden: !this.colorCodeMenu,
         enableToggle: true,
         tooltip: {
            title: WtfGlobal.getLocaleText('pm.common.color.coding.enabled'),
            text: WtfGlobal.getLocaleText('pm.Help.reportcolorcode')
        },
         pressed: true,
         id: this.id + '_colorCoding',
         toggleHandler: function(btn, pressed){
             if(pressed == true){
                 if(this.colorCodeMenu == true)
                    this.enableColorCoding(true);
             } else {
                 this.enableColorCoding(false);
             }
         } 
        },'-',{
        text:WtfGlobal.getLocaleText('lang.export.text'),
        iconCls: 'pwnd exporticontext',
        scope:this,
        tooltip: {
            title: WtfGlobal.getLocaleText('pm.report.export.text'),
            text: WtfGlobal.getLocaleText('pm.Help.reportexport')
        },
        id: this.id + '_export',
        menu: [ new Wtf.Action({
            text: WtfGlobal.getLocaleText('pm.project.plan.export.pdf'),
            iconCls: 'pwnd pdfexporticon',
            scope: this,
            handler:function(){
                this.fireEvent("exportPDF", this);
            }
        }),new Wtf.Action({
            text: WtfGlobal.getLocaleText('pm.project.plan.export.csv'),
            iconCls: 'pwnd csvexporticon',
            scope: this,
            handler:function(){
                this.fireEvent("exportCSV", this);
            }
        }),new Wtf.Action({
            text: WtfGlobal.getLocaleText('pm.project.plan.reports.export.embed'),
            iconCls: 'pwnd exporticon',
            scope: this,
            hidden: !this.chartflag,
            handler:function(){
                this.fireEvent("embedReportClicked", this);
            }
        })]
      },{
        text: WtfGlobal.getLocaleText('pm.project.plan.reports.graph'),
        id: this.id + '_graphBtn',
        scope: this,
        hidden: !this.chartflag,
        enableToggle: true,
        iconCls: 'dpwnd graph',
        pressed: false,
        tooltip: {
            title: WtfGlobal.getLocaleText('pm.common.view.graph'),
            text: WtfGlobal.getLocaleText('pm.Help.reportgraph')
        },
        handler: function(btn){
            var cbtn = Wtf.getCmp(this.id + '_colorCoding');
            var ccdmenu = Wtf.getCmp(this.id+'_selectrange');
            var datefilter = Wtf.getCmp(this.id+'_filteredate');
            var gobtn = Wtf.getCmp(this.id+'_gotodate');
            var prevbtn = Wtf.getCmp(this.id + '_prevCol');
            var nextbtn = Wtf.getCmp(this.id + '_nextCol');
            if(btn.pressed){
                if(this.colorCodeMenu){
                    cbtn.hide();
                    ccdmenu.hide();
                    for(var i = 0; i< this.colHeader.length; i++){
                        if(Wtf.getCmp(this.id + '_colormenu_' + i))
                            Wtf.getCmp(this.id + '_colormenu_' + i).hide();
                    }
                } else {
                    datefilter.hide();
                    gobtn.hide();
                    prevbtn.hide();
                    nextbtn.hide();
                }
                this.chartDOM.innerHTML = "";
                var t = Wtf.get(this.chartDiv.id);
                t.removeClass("reportGridShow");
                t.addClass("reportGridHide");
                var ht = t.getHeight();
                if(this.chart[0] === undefined)
                    this.chart = [this.chart];
                for(var c = 0 ; c < this.chart.length; c++){
                    var tempDiv = document.createElement("div");
                    var f = this.chart[c].chartFlash;
                    var s = this.chart[c].chartSetting;
                    var d = this.chart[c].dataURL;
                    if(f === undefined || s === undefined){
                        f = "scripts/bar chart/krwcolumn.swf";
                        s = "scripts/bar chart/krwcolumn_settings.xml";
                    }
                    if(!ieflag)
                        tempDiv.style.height = ht + "px";
                    tempDiv.id = this.id + "_chart-gen-" + c;
                    this.chartDOM.appendChild(tempDiv);
                    //                if(d !== undefined)
                    createNewChart(f, tempDiv.id + "_chart" + c, '96%', '100%', '8', '#ffffff', s, d, this.id + "_chart-gen-" + c);
                //                else
                //                    tempDiv.innerHTML = "Oops! Could not load the graph...";
                }
//                btn.setDisabled(true);
//                if(this.colorCodeMenu == true){
//                    var cbtn = Wtf.getCmp(this.id + '_colorCoding');
//                    cbtn.setDisabled(true);
//                    this.isColorCodingOn = cbtn.pressed;
//                    if(cbtn.pressed)
//                        cbtn.toggle();
//                }
            } else {
                this.closeChartClicked();
                if(this.colorCodeMenu){
                    cbtn.show();
                    ccdmenu.show();
                    for(i = 0; i< this.colHeader.length; i++){
                        if(Wtf.getCmp(this.id + '_colormenu_' + i))
                            Wtf.getCmp(this.id + '_colormenu_' + i).show();
                            
                    }
                } else {
                    datefilter.show();
                    gobtn.show();
                    prevbtn.show();
                    nextbtn.show();
                }
            }
        }
        }, '->' , new Wtf.Action({ 
        text: WtfGlobal.getLocaleText('pm.project.plan.reports.setcolorcodingrange'),
        scope: this,
        id: this.id+'_selectrange',
        hidden: !this.colorCodeMenu,
        tooltip: {
            title: WtfGlobal.getLocaleText('pm.project.plan.reports.setcolorcodingrange'),
            text: WtfGlobal.getLocaleText('pm.Help.reportrange')
        },
        handler: function(btn, e){
            this.selectCustomRanges(btn, e);
        }
        }),
        this.scrollToDate = new Wtf.form.DateField({
            emptyText: WtfGlobal.getLocaleText('lang.date.goto'),
            width:120,
            readOnly: true,
            hidden: !this.columnScroll,
            format: 'Y-m-d',
            id:this.id+'_filteredate',
            minValue:startdate,
            minValueText: WtfGlobal.getLocaleText({key:'pm.project.plan.reports.minvaltext',params:startdate}),
            maxValue: enddate,
            maxValueText: WtfGlobal.getLocaleText({key:'pm.project.plan.reports.maxvaltext',params:startdate})
        }),'->',
        {
            text: WtfGlobal.getLocaleText('lang.go.text'),
            cls :'reportToolbarBtn',
            hidden: !this.columnScroll,
            scope: this,
            id: this.id+'_gotodate',
            handler: function(btn, e){
                var tempdate = Wtf.getCmp(this.id+'_filteredate').getValue();
                if(tempdate == ""){
                    msgBoxShow(315, 0);
                } else {
                    this.goToDate(tempdate);
                }
            }
        },(this.columnScroll) ? '-' : '',
        this.prevColumn = new Wtf.Button({
        text: "",
        iconCls: 'pwnd previcon',
        style: 'margin-left:7px;',
        hidden: !this.columnScroll,
        scope: this,
        id: this.id + '_prevCol',
        disabled: true,
        tooltip: {
            title: WtfGlobal.getLocaleText('pm.common.previous'),
            text: WtfGlobal.getLocaleText('pm.Help.reportprev')
        },
        handler: function(obj){
            this.fireEvent("scrollPrev", this);
        }
    }),this.nextColumn = new Wtf.Button({
        text: "",
        iconCls: 'pwnd nexticon',
        style: 'margin-left:7px;',
        hidden: !this.columnScroll,
        scope: this,
        id: this.id + '_nextCol',
        tooltip: {
            title: WtfGlobal.getLocaleText('lang.Next.text'),
            text: WtfGlobal.getLocaleText('pm.Help.reportnext')
        },
        handler: function(obj){
            this.fireEvent("scrollNext", this);
        }
    })];
    scp=this;
    Wtf.proj.reportGrid.superclass.constructor.call(this, conf);
}
 
Wtf.extend(Wtf.proj.reportGrid, Wtf.grid.GridPanel, {
    columnScroll: false,
    colorCodeMenu: true,
    initComponent: function(conf){
        Wtf.proj.reportGrid.superclass.initComponent.call(this, conf);
        this.defaultChrtFlv = "scripts/bar chart/krwcolumn.swf";
        this.defaultChrtSetting = "scripts/bar chart/krwcolumn_settings.xml";
        this.addEvents({
            'exportPDF': true,
            "exportCSV": true,
            "scrollNext": true,
            "scrollPrev": true
        });
    }, 
    onRender: function(conf){
        Wtf.proj.reportGrid.superclass.onRender.call(this, conf);
        if(this.colorCodeMenu == true){
            this.initRange();
            this.initColorsForRendering();
            this.enableColorCoding(true);
        }
    },
    closeChartClicked: function(){
        Wtf.get(this.chartDiv.id).removeClass("reportGridHide");
        Wtf.get(this.chartDiv.id).addClass("reportGridShow");
        Wtf.getCmp(this.id + '_graphBtn').setDisabled(false);
        if(this.colorCodeMenu == true){
            var cbtn = Wtf.getCmp(this.id + '_colorCoding');
            cbtn.setDisabled(false);
            if(this.isColorCodingOn)
                cbtn.toggle();
        }
    },
    enableNextButton: function(){
        this.nextColumn.enable();
    },
    disableNextButton: function(){
        this.nextColumn.disable();
    },
    enablePrevButton: function(){
        this.prevColumn.enable();
    },
    disablePrevButton: function(){
        this.prevColumn.disable();
    },
    adjustNavigationButtons: function(){
        var columnHeader = this.headerObj;
        var columncnt = (this.pageNo + 1) * this.columnPerPage;
        if(columncnt < columnHeader.length) {
            this.enableNextButton();
            this.lastPage = false;
        } else {
            this.disableNextButton();
            this.lastPage = true;
        }
        if(columncnt == this.columnPerPage)
            this.disablePrevButton();
        else
            this.enablePrevButton();
    },

    setColorsForRendering: function(rangeNo, ci, clr){
        var clrs = this.colors;
        var menu = ci.id.split(this.id+'_coloritem_')[1];
        var menuNo = menu.split('_')[0];
        if(ci.palette)
            clrs[menuNo][rangeNo-1] = '#' + ci.palette.value;
        else
            clrs[menuNo][rangeNo-1] = '#' + clr;
        this.colors = clrs;
        document.getElementById(this.id+'_colorspan_'+menuNo).style.background = this.colors[menuNo][1]; 
        Wtf.getCmp(this.id+'_colorrangemenu_'+menuNo+'_'+rangeNo).el.dom.firstChild.style.backgroundColor = this.colors[menuNo][rangeNo-1]; 
        this.renderColumn(true);
    },

    selectCustomRanges: function(){
        var noH = this.colHeader.length;
        this.rangeWin = new Wtf.Window({
            id: this.id+'_rangewindow',
            title: WtfGlobal.getLocaleText('pm.common.selectrange'),
            autoHeight: true,
            iconCls: '',
            modal: true,
            autoScroll: true,
            bodyStyle: 'padding:10px;',
            buttonAlign: 'center',
            resizable: false
        });
        for(var i = 0; i<this.colHeader.length; i++){
            var type = this.colHeader[i].toString();
            var typekey = this.colKeys[i].toString();
            this.rangeWin.add({
                xtype: 'fieldset',
                id: this.id+'_fieldset_'+i,
                collapsible: false,
                style: 'border:0px solid red !important',
                title: WtfGlobal.getLocaleText(typekey),
                height: 100,
                border: true,
                bodyBorder: false,
                items:[{
                    layout: 'form',
                    autoWidth: true,
                    labelAlign: 'left',
                    border: false,
                    items: [
                    {
                        xtype: 'numberfield',
                        fieldLabel: WtfGlobal.getLocaleText('pm.project.plan.reports.lower'),
                        id: this.id+'_rangemin_'+i,
                        value: this.range[i][0],
                        width: 50,
                        allowBlank:false,
                        maxLength: 5,
                        maxLengthText: WtfGlobal.getLocaleText('pm.project.plan.reports.minlengthtext'),
                        allowDecimals: false,
                        minValue: (type.indexOf('Percent') != -1) ? 0: '',
                        minValueText: WtfGlobal.getLocaleText('pm.project.plan.reports.maxlengthtext')
                    },{
                        xtype: 'numberfield',
                        fieldLabel: WtfGlobal.getLocaleText('pm.project.plan.reports.upper'),
                        id: this.id+'_rangemax_'+i,
                        value: this.range[i][1],
                        width: 50,
                        allowBlank:false,
                        maxLength: 5,
                        maxLengthText: WtfGlobal.getLocaleText('pm.project.plan.reports.minlengthtext'),
                        minValue: (type.indexOf('Percent') != -1) ? 0: '',
                        minValueText: WtfGlobal.getLocaleText('pm.project.plan.reports.maxlengthtext'),
                        allowDecimals: false
                    }
                    ]
                }]
            });
        }

        this.rangeWin.addButton({
            text: WtfGlobal.getLocaleText('lang.done.text'),
            id: this.id+'_rangesubmitbtn'
        }, function(){
            var flag = true;
            for(var i = 0; i<this.colHeader.length; i++){
                if(!Wtf.getCmp(this.id+'_rangemin_'+i).isValid(false) || !Wtf.getCmp(this.id+'_rangemax_'+i).isValid(false)){
                    flag = false;
                    break;
                }
            }
            if(flag){
                var ranges = new Array(this.colHeader.length);
                for(i = 0; i<this.colHeader.length; i++){
                    ranges[i] = new Array(2);
                    ranges[i][0] = Wtf.getCmp(this.id+'_rangemin_'+i).getValue();
                    ranges[i][1] = Wtf.getCmp(this.id+'_rangemax_'+i).getValue();
                    Wtf.getCmp(this.id+'_colorrangemenu_'+i+'_1').setText(this.getMenuItemText(this.colHeader[i]) + '<  '+ranges[i][0]);
                    Wtf.getCmp(this.id+'_colorrangemenu_'+i+'_2').setText(ranges[i][0]+'   <' + this.getMenuItemText(this.colHeader[i]) + '<  '+ranges[i][1]);
                    Wtf.getCmp(this.id+'_colorrangemenu_'+i+'_3').setText(this.getMenuItemText(this.colHeader[i]) + '>  '+ranges[i][1]);
                }
                this.range = ranges;
                this.rangeWin.close();
                this.enableColorCoding(true);
            }
        }, this);

        this.rangeWin.addButton({
            text: WtfGlobal.getLocaleText('lang.cancel.text'),
            id: this.id+'_rangecancelbtn'
        }, function(){
            this.rangeWin.close();
        }, this);

        this.rangeWin.show();
    },

    enableColorCoding: function(flag){
        if(flag){
            var colp = [];
            for(var i = 0; i< this.colHeader.length; i++){
                if(!Wtf.getCmp(this.id + '_colormenu_' + i)){
                    colp[i] = new Wtf.Toolbar.Button({
                        id: this.id + '_colormenu_' + i,
                        cls: 'colorCodeMenuBtn',
                        scope: this,
                        minWidth: '150px',
                        text: '<div id="'+ this.id + '_colorspan_'+i+'" style="width:12px; height:12px; background:'+this.colors[i][1]+'; float:left; margin-right: 5px;"></div>' + WtfGlobal.getLocaleText(this.colKeys[i]),
                        menu:[
                            new Wtf.Action({
                                text: this.getMenuItemText(this.colHeader[i]) + '<=  '+this.range[i][0],
                                autoWidth: true,
                                id: this.id+'_colorrangemenu_'+i+'_1',
                                menu:[
                                    new Wtf.menu.ColorItem({
                                         id: this.id + '_coloritem_'+i+'_1',
                                         scope: this,
                                         handler: function(ci, color){
                                             this.setColorsForRendering(1, ci, color);
                                         }
                                })]
                            }),
                            new Wtf.Action({
                                autoWidth: true,
                                id: this.id+'_colorrangemenu_'+i+'_2',
                                text: this.range[i][0]+'   <' + this.getMenuItemText(this.colHeader[i]) + '<=  '+this.range[i][1],
                                menu:[
                                    new Wtf.menu.ColorItem({
                                         id: this.id + '_coloritem_'+i+'_2',
                                         scope: this,
                                         handler: function(ci, color){
                                             this.setColorsForRendering(2, ci, color);
                                         }
                                })]
                            }),
                            new Wtf.Action({
                                autoWidth: true,
                                id: this.id+'_colorrangemenu_'+i+'_3',
                                text: this.getMenuItemText(this.colHeader[i]) + '>  '+this.range[i][1],
                                menu:[
                                    new Wtf.menu.ColorItem({
                                         id: this.id + '_coloritem_'+i+'_3',
                                         scope: this,
                                         handler: function(ci, color){
                                             this.setColorsForRendering(3, ci, color);
                                         }
                                })]
                            })
                        ]
                    });
                    this.getTopToolbar().add('-');
                    this.getTopToolbar().add(colp[i]);
                } else{
                    for(i = 0; i< this.colHeader.length; i++){
                    if(Wtf.getCmp(this.id + '_colormenu_' + i))
                        Wtf.getCmp(this.id + '_colormenu_' + i).setDisabled(false);
                    }
                    Wtf.getCmp(this.id + '_selectrange').setDisabled(false);
                }
            }
        } else{
            for(i = 0; i< this.colHeader.length; i++){
                if(Wtf.getCmp(this.id + '_colormenu_' + i))
                    Wtf.getCmp(this.id + '_colormenu_' + i).setDisabled(true);
            }
            Wtf.getCmp(this.id + '_selectrange').setDisabled(true);
            this.renderColumn(false);
        }
        if(flag)
            this.renderColumn(true);
    },

    initRange: function(){
        var ranges = new Array(this.colHeader.length);
        for(var i = 0; i<this.colHeader.length; i++){
            var type = this.colHeader[i].toString();
            ranges[i] = new Array(2);
            ranges[i][0] = 0;
            ranges[i][1] = (type.indexOf('Duration') != -1 || type.indexOf('Percent') != -1) ? 100: 1000
        }
        this.range = ranges;
    },

    initColorsForRendering: function(){
        var clrs = new Array(this.colHeader.length);
        for(var i = 0; i<this.colHeader.length; i++){
            clrs[i] = new Array(3);
            clrs[i][0] = '#439941';
            clrs[i][1] = '#15428B';
            clrs[i][2] = '#E73030';
        }
        this.colors = clrs;
    },

    getMenuItemText: function(header){
        var text = "";
        header = header.toString();
        if(header.indexOf('Cost') != -1 || header.indexOf('Expense') != -1){
            text = '  '+WtfGlobal.getLocaleText("lang.cost.text")+'  ';
        } else if(header.indexOf('Work') != -1){
            text = '  '+WtfGlobal.getLocaleText("lang.hours.text")+'  ';
        } else if(header.indexOf('Duration') != -1 || header.indexOf('Date') != -1 || header.indexOf('Overdue By') != -1){
            text = '  '+WtfGlobal.getLocaleText("lang.days.text")+'  ';
        } else if(header.indexOf('Percent') != -1){
            text = '  % '+WtfGlobal.getLocaleText("pm.dashboard.widget.project.complete")+'  ';
        }
        return text;
    },

    getRendererType: function(header, cm, index, menuNo, flag){
        var type = header.toString();
        if(type.indexOf('Cost') != -1 || type.indexOf('Expense') != -1){
            this.setRendererForCost(cm, index, menuNo, flag);
        } else if(type.indexOf('Work') != -1){
            this.setRendererForWork(cm, index, menuNo, flag);
        } else if(type.indexOf('Duration') != -1 || type.indexOf('Date') != -1 || type.indexOf('Overdue By') != -1){
            this.setRendererForDuration(cm, index, menuNo, flag);
        } else if(type.indexOf('Percent') != -1 ){
            this.setRendererForPercentComplete(cm, index, menuNo, flag);
        }
        return type;
    },

    setRendererForCost: function(cm, colNo, menuNo, flag){
        if(flag){
            cm.setRenderer(colNo, function(v,a,b,c,d,e){
                var clr = "";
                var ret = "";
                var temp = v.toString();
                if(temp.indexOf(Wtf.CurrencySymbol) != -1)
                    v = v.substr(0,2);
                if(v != 'NA'){
                    if(v <= scp.range[menuNo][0])
                        clr = scp.colors[menuNo][0];
                    else if(v > scp.range[menuNo][0] && v <= scp.range[menuNo][1])
                        clr = scp.colors[menuNo][1];
                    else
                        clr = scp.colors[menuNo][2];
                    ret = '<span style="color:'+clr+';">' + Wtf.CurrencySymbol + " " + v + '</span>';
                } else {
                    clr = '#000000';
                    ret = '<span style="color:'+clr+';">' + v + '</span>';
                }
                return ret;
            });
        } else {
            cm.setRenderer(colNo, function(v,a,b,c,d,e){
                var clr = '#000000';
                var ret = "";
                var temp = v.toString();
                if(temp.indexOf(Wtf.CurrencySymbol) != -1)
                    v = v.substr(0,2);
                if(v != 'NA'){
                    ret = '<span style="color:'+clr+';">' + Wtf.CurrencySymbol + " " + v + '</span>';
                } else {
                    ret = '<span style="color:'+clr+';">' + v + '</span>';
                }
                return ret;
            });
        }
    }, 

    setRendererForPercentComplete: function(cm, colNo, menuNo, flag){
        if(flag){
            cm.setRenderer(colNo, function(v,a,b,c,d,e){
                var clr = "";
                var ret = "";
                v = v.substr(0,v.indexOf('%'));
                if(v != 'NA'){
                    if(v <= scp.range[menuNo][0])
                        clr = scp.colors[menuNo][0];
                    else if(v > scp.range[menuNo][0] && v <= scp.range[menuNo][1])
                        clr = scp.colors[menuNo][1];
                    else
                        clr = scp.colors[menuNo][2];
                    ret = '<span style="color:'+clr+';">' + v + '%</span>';
                } else {
                    clr = '#000000';
                    ret = '<span style="color:'+clr+';">' + v + '</span>';
                }
                return ret;
            });
        } else {
            cm.setRenderer(colNo, function(v,a,b,c,d,e){
                var clr = '#000000';
                var ret = "";
                v = v.substr(0,v.indexOf('%'));
                if(v != 'NA'){
                    ret = '<span style="color:'+clr+';">' + v + '%</span>';
                } else {
                    ret = '<span style="color:'+clr+';">' + v + '</span>';
                }
                return ret;
            });
        }
    },
    
    setRendererForWork: function(cm, colNo, menuNo, flag){
        if(flag){
            cm.setRenderer(colNo, function(v,a,b,c,d,e){
                var clr = "";
                var ret = "";
                var temp = v.toString();
                if(temp.indexOf(' hrs') != -1)
                    v = v.substr(0,v.indexOf(' hrs'));
                if(v != 'NA'){
                    if(v <= scp.range[menuNo][0])
                        clr = scp.colors[menuNo][0];
                    else if(v > scp.range[menuNo][0] && v <= scp.range[menuNo][1])
                        clr = scp.colors[menuNo][1];
                    else
                        clr = scp.colors[menuNo][2];
                    ret = '<span style="color:'+clr+';">' + v + ' hrs' + '</span>';
                } else {
                    clr = '#000000';
                    ret = '<span style="color:'+clr+';">' + v + '</span>';
                }
                return ret;
            });
        } else {
            cm.setRenderer(colNo, function(v,a,b,c,d,e){
                var clr = '#000000';
                var ret = "";
                var temp = v.toString();
                if(temp.indexOf(' hrs') != -1)
                    v = v.substr(0,v.indexOf(' hrs')); 
                if(v != 'NA'){
                    ret = '<span style="color:'+clr+';">' + v + ' hrs' + '</span>';
                } else {
                    ret = '<span style="color:'+clr+';">' + v + '</span>';
                }
                return ret;
            });
        }
    },
    
    setRendererForDuration: function(cm, colNo, menuNo, flag){
        if(flag){
            cm.setRenderer(colNo, function(v,a,b,c,d,e){
                var clr = "";
                var ret = "";
                var temp = v.toString();
                if(temp.indexOf('d') != -1)
                    v = v.substr(0,v.indexOf('d'));
                if(v != 'NA'){
                    if(temp.indexOf('h') != -1)
                        v = v.substr(0,v.indexOf('h'));
                    if(v <= scp.range[menuNo][0])
                        clr = scp.colors[menuNo][0];
                    else if(v > scp.range[menuNo][0] && v <= scp.range[menuNo][1])
                        clr = scp.colors[menuNo][1];
                    else
                        clr = scp.colors[menuNo][2];
                    if(temp.indexOf('h') != -1)
                        ret = '<span style="color:'+clr+';">' + v + ' hours</span>';
                    else
                        ret = '<span style="color:'+clr+';">' + v + ' days' + '</span>';
                } else {
                    clr = '#000000';
                    ret = '<span style="color:'+clr+';">' + v + '</span>';
                }
                return ret;
            });
        } else {
            cm.setRenderer(colNo, function(v,a,b,c,d,e){
                var clr = '#000000';
                var ret = "";
                var temp = v.toString();
                if(temp.indexOf(' days') != -1)
                    v = v.substr(0,v.indexOf(' days'));
                if(temp.indexOf('h') != -1)
                    v = v.substr(0,v.indexOf('h'));
                if(v != 'NA'){
                    if(temp.indexOf('h') != -1)
                        ret = '<span style="color:'+clr+';">' + v + ' hours</span>';
                    else
                        ret = '<span style="color:'+clr+';">' + v + ' days' + '</span>';
                } else {
                    ret = '<span style="color:'+clr+';">' + v + '</span>';
                }
                return ret;
            });
        }
    },

   goToDate: function(tempdate){
        var count;
        var pageNo;
        var headerdate;
        var left = 0;
        var right = this.headerObj.length - 1;
        var seldate = tempdate.format('Y-m-d');
        while (left <= right){
            var mid = parseInt((left + right)/2);
            headerdate = this.headerObj[mid]["0"];
            if(headerdate == undefined) {
                if (this.headerObj[mid]["1"] == seldate){
                    count =  mid;
                    break;
                } else if (this.headerObj[mid]['1'] < seldate){
                    left = mid + 1;                 
                } else {
                    right = mid - 1;
                }
            } else {
                if(this.headerObj[mid]["0"] == seldate){
                    count =  mid;
                    break;
                } else if (this.headerObj[mid]['0'] < seldate){
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }
        }
        pageNo = (count / this.columnPerPage);
        if(pageNo == parseInt(pageNo)){
             pageNo = pageNo - 1;
             this.pageNo = pageNo;
             this.fireEvent("scrollNext", this);
        } else {
             this.pageNo = Math.floor(pageNo) -1;
             this.fireEvent("scrollNext", this);
        }
   }, 

    renderColumn: function(flag){
        var cm = this.colModel;
        for(var i = 0; i< this.colHeader.length; i++){
            for(var c = 0; c<cm.config.length; c++){
                if(cm.config[c].header == WtfGlobal.getLocaleText(this.colKeys[i])){
                    this.getRendererType(this.colHeader[i], cm, c, i, flag);
                }
            }
        }
        this.getView().refresh(); 
    }
}); 
