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

/*----------Record for custom report----------------*/

Wtf.cc.reportColumnRec = Wtf.data.Record.create([{
    name:'columnID'
},{
    name:'reportID'
},{
    name :'displayHeader'
},{
    name: "name"
},{
    name: "type"
},{
    name: "header"
},{
    name: "module"
},{
    name:'tableName'
},{
    name:'fieldName'
},{
    name : "dataIndex"
},{
    name : "iscustomcolumn"
},{
    name : "renderer"
},{
    name :"summary"
},{
    name :"quickSearch"
},{
    name :'isMandatory'
},{
    name :'headerkey'
}
]);

/* componenet for export button */
Wtf.ExportButton = function(config){
    Wtf.apply(this,config);
    Wtf.ExportButton.superclass.constructor.call(this,{
        iconCls: 'pwnd exporticontext1',
        menu: [
        /* new Wtf.Action({
            text: WtfGlobal.getLocaleText('pm.project.plan.export.pdf'),
            iconCls: 'pwnd pdfexporticon',
            scope: this,
            exporttype:"pdf",
            handler:function(btn){
                this.exportReport(this.reportid,btn.exporttype);
            }
        }),*/
        new Wtf.Action({
            text: WtfGlobal.getLocaleText('pm.project.plan.export.csv'),
            iconCls: 'pwnd csvexporticon',
            scope: this,
            exporttype:"csv",
            handler:function(btn){
                this.exportReport(this.reportID,btn.exporttype);
            }
        })
        ]
    });
}
Wtf.extend(Wtf.ExportButton,Wtf.Toolbar.Button,{
    exportReport  :function(rid,exptype){
        this.exportHandler(rid, exptype); //create delegate for this method
    }
})

/* Grid component for report where report will dispaly */
Wtf.CustomReportGrid = function(config){
    Wtf.apply(this, config);

    this.btnExport = new Wtf.ExportButton({
        text: WtfGlobal.getLocaleText('lang.export.text'),
        reportID:this.reportID,
        hidden: !this.exportable,
        exportHandler:this.exportReport.createDelegate(this)

    });
    this.filter=new Wtf.Button({
        text:WtfGlobal.getLocaleText('lang.filter.text'),
        handler:this.showAdvanceFilter,
        scope:this,
        hidden: !this.showFilters,
        iconCls:"pwnd applyfilter"
    });
    Wtf.CustomReportGrid.superclass.constructor.call(this,{
        title:this.reportname,
        header: this.showTitle,
        border: false,
        iconCls:'pwnd c_reporttabicon'
    });
};

Wtf.extend(Wtf.CustomReportGrid, Wtf.Panel, {
    initComponent: function(config){
        Wtf.CustomReportGrid.superclass.initComponent.call(this);
        this.getAdvanceSearchComponent();
    },
    submitHandler : function() {
        var ldmask = new Wtf.LoadMask(Wtf.get('centerGridRegion'+this.id));
        ldmask.show();
        this.store.load({
            params:{
                start: 0,
                limit: this.pagingToolbar ? this.pagingToolbar.pageSize : 10000,
                reportid:this.reportID,
                filter:this.advFilter.getFilterData(),
                ss:this.quickSearch.getValue()
            },
            callback: function(){
                ldmask.hide();
            },
            scope:this
        });
        this.doLayout();
    },

    createGrid : function(){
        this.grid = new Wtf.grid.GridPanel({
            cm: this.cm,
            sm: new Wtf.grid.CellSelectionModel(),
            id: this.id + '_reportGrid',
            ds: this.store,
            border : false,
            stripeRows : true,
            layout:'fit',
            cls: this.reportConfig.milestone ? 'rowheight' : '',
            view :this.groupingView,
            loadMask: {
                msg: WtfGlobal.getLocaleText("pm.loading.text")+'...'
            },
            tbar: this.toolbar,
            plugins: this.summary,
            bbar: this.reportConfig.paging ? this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: this.customPageSize,
                id: this.id + '_pt',
                store: this.store,
                searchField: this.quickSearch,
                displayInfo: false,
//                displayMsg: 'Displaying records {0} - {1} of {2}',
                emptyMsg: "No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                    })
            }) : []
        });
        // Only this part is hardcoded in this component for milestone widget. Other changes are reusable.
        if(this.reportConfig.milestone){
            this.grid.on('cellclick', function(grid, rowI, colI, e){
                var cm = grid.getColumnModel();
                var colId = cm.getColumnById(cm.getColumnId(colI)).id;
                if(colId.indexOf('defaultmilestone') != -1){
                    var cell = e.getTarget().parentElement;
                    if(cell.tagName == 'TD')
                        cell = cell.children[0];
                    showMilestonesDetailsPanel(e, cell);
                }
            }, this);
        }
    },
    createGridHeader: function(response){
        var obj = eval('(' + response.trim() + ')');
        this.createRecord(obj.data);
        this.createToolBar(obj.data);
        this.cm = new Wtf.grid.ColumnModel(this.createColModel(obj.data));
        this.createStore(obj.data);
    },
    createRecord: function(obj){
        var cfg = [];
        var _c = obj.length;
        for(var i = 0; i <_c ; i++) {
            var fObj = {};
            fObj['name'] = obj[i].dataIndex;
            cfg[cfg.length] = fObj;
        }
        this.record = Wtf.data.Record.create(cfg);
    },
    createColModel: function(obj){
        var cfg =[];
        this.summary = new Wtf.grid.GridSummary();
        //        this.summary = new Wtf.grid.GroupSummary();
        var len = obj.length, mlCnt = 1;
        for(var cnt =0; cnt<len; cnt++) {
            var colObj = {};
            colObj['header'] = obj[cnt].headerkey ? WtfGlobal.getLocaleText(obj[cnt].headerkey) + ((obj[cnt].renderer == 'milestone') ? ' ' +  mlCnt++: '') : obj[cnt].displayHeader;
            colObj['dataIndex'] = obj[cnt].dataIndex;
            colObj['id'] = obj[cnt].columnID;
            //colObj['sortable'] = false;
            colObj['renderer'] =Wtf.cc.renderer[obj[cnt].renderer]
            //colObj['hidden'] = !rec.get('visible');
            if(obj[cnt].summary!="null"){
                colObj["summaryType"] = obj[cnt].summary;
                colObj["id"] = obj[cnt].summary+"_"+obj[cnt].dataIndex+i;
                colObj["summaryRenderer"] = function(v, params, data){
                    v = v ? v : 0;
                    return params.id.substr(0,params.id.indexOf("_"))+":"+v;
                }
            }
            cfg[cfg.length] = colObj;
        }
        return cfg;
    },
    createToolBar: function(obj){
        var legend="";
        if(this.reportConfig.milestone){
            legend = "<div style='float:right;border:none;font-size:8pt;margin-right:10px;' class='Someclass'>"+
            "<span class='milestoneLegendBkg inprogressml'></span> - "+WtfGlobal.getLocaleText('pm.reports.custom.milestone.legend.inprogress')+
            "<span class='milestoneLegendBkg completedml'></span> - "+WtfGlobal.getLocaleText('pm.reports.custom.milestone.legend.completed')+
            "<span class='milestoneLegendBkg overdueml'></span> - "+WtfGlobal.getLocaleText('pm.reports.custom.milestone.legend.overdue')+
            "</div>";
        }
        var emptyText ='Search by ';
        var len = obj.length;
        for(var cnt =0; cnt<len; cnt++) {
            if(obj[cnt].quickSearch){
                emptyText+=obj[cnt].displayHeader+",";
            }
        }
        this.toolbar = [this.searchable ? WtfGlobal.getLocaleText('pm.common.quicksearch')+':' : '',
        this.quickSearch = new Wtf.KWLCustomSearch({
            id : 'search'+this.id,
            width: 200,
            emptyText: emptyText.substr(0,emptyText.length-1)
        }),this.filter,'-',this.btnExport, '->', legend];
        if(!this.searchable)
            this.quickSearch.hide();
    },
    createStore: function(obj){
        this.groupingView = new Wtf.grid.GroupingView({
            forceFit: false,
            showGroupName: false,
            enableGroupingMenu: true,
            //groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Columns" : "Column"]})'//,
            hideGroupedColumn: true
        });
        this.store = new Wtf.data.GroupingStore({
            url:"../../reports.jsp",
            //groupField:'projectname',
            baseParams : {
                m:'getReport'
            },
            sortInfo: {
                field: 'projectname',
                direction: "ASC"
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'count'
            }, this.record)
        });

        //         this.store = new Wtf.data.Store({
        //            url:"../../reports.jsp",
        //            baseParams : {
        //                m:'getReport'
        //            },
        //            sortInfo: {
        //                field: 'projectname',
        //                direction: "ASC"
        //            },
        //            reader: new Wtf.data.KwlJsonReader({
        //                root: "data"
        //            }, this.record)
        //        });
        this.milestoneFirstIndex = -1;
        this.milestoneLastIndex = -2;
        this.quickSearch.storageChanged(this.store);
        this.store.on('beforeload',function(){
            var fields = this.store.fields;
            var keysize = fields.keys.length;
            for(var i = 0 ; i<keysize ; i++){
                if(fields.keys[i]=="milestone1"){
                    this.milestoneFirstIndex = i;
                    this.milestoneLastIndex = keysize - 1;
                    break;
                }
            }
            this.store.baseParams = {
                reportid:this.reportID,
                filter:this.advFilter.getFilterData(),
                m:'getReport'
            }
        },this);

        this.store.on('load',function(){

          var isSubString = function(substring, string){
               var flag = false;
               var arr = string.split(",");
               for(var i =0 ; i<arr.length ; i++){
                    if(substring == arr[i]){
                        flag = true;
                        break;
                    }
               }
               return flag;
           }
           var rowHeaders = new Array((this.milestoneLastIndex - this.milestoneFirstIndex +1));
           for( i=0;i<rowHeaders.length;i++){
               rowHeaders[i]="";
           }
           var data = this.store.data;
           if(this.mileStoneStartIndex != -1){
               for(var i=0 ; i < rowHeaders.length ; i++){
                   var milestonekey = "milestone"+(i+1);
                   for(var j=0; j<data.length; j++){
                       var ms = data.items[j].data[milestonekey];
                       if(ms && ms != ""){
                           var substr = eval("("+ms+")").taskname;
                           if(!isSubString(substr, rowHeaders[i])){
                                if(rowHeaders[i] == ""){
                                    rowHeaders[i] += substr;
                                }else{
                                    rowHeaders[i] += ","+ substr;
                                }
                            }
                       }
                   }
               }
           }
           for(var i=0;i<rowHeaders.length;i++){
               var tip = "No milestones in this column.";
               if(rowHeaders[i] != ""){
                    tip = "";
                    var h = rowHeaders[i].split(",");
                    for(var j=0 ; j<h.length;j++){
                        tip += h[j]+"<br>";
                    }
               }

               var displayHeader = (rowHeaders[i] == "")?"--":rowHeaders[i];
               var header = "<span  wtf:qtip='"+tip+"'>"+displayHeader+"</span>"
               this.cm.setColumnHeader(i+this.milestoneFirstIndex,header);
           }

        },this);
    },
    onRender : function(config) {
        Wtf.CustomReportGrid.superclass.onRender.call(this,config);
        this.loadMask1 = new Wtf.LoadMask(Wtf.getCmp(this.id).el.dom, Wtf.apply(this.loadMask1));
        this.loadMask1.show();
        Wtf.Ajax.requestEx({
            url: '../../reports.jsp',
            method: 'POST',
            params: {
                m : 'getReportsColumn',
                reportid:this.reportID
            }
        },this,
        function(response, e){
            this.createGridHeader(response);
            this.createGrid();
            this.DisplayReport();
            this.submitHandler();
            this.loadMask1.hide();
        },
        function(resp,req){
            this.loadMask1.hide();
        });
    },
    DisplayReport : function()  {
        this.panel = {
            border: false,
            layout: "border",
            items :[this.advFilter,{
                region: "center",
                //                autoScroll: true,
                id : 'centerGridRegion'+this.id,
                bodyStyle : 'background:#f1f1f1;font-size:10px;',
                border:false,
                layout:'fit',
                items: [this.grid]
            }]
        }
        this.add(this.panel);
        this.doLayout();
    },
    showAdvanceFilter : function(){
        if(this.advFilter.hidden){
            this.advFilter.show();
            this.filter.setText(WtfGlobal.getLocaleText('lang.submit.text'));
            this.filter.setIconClass("dpwnd submiticon1");
        }
        else{
            this.submitHandler();
        }
        this.doLayout();
    },
    closeFilter : function(){
        this.advFilter.hide();
        this.doLayout();
        this.filter.setText(WtfGlobal.getLocaleText('lang.filter.text'));
        this.filter.setIconClass("pwnd applyfilter");
        this.advFilter.clearFilters();
        this.submitHandler();
    },
    getAdvanceSearchComponent:function(){
        this.advFilter=new Wtf.advanceReportFilter({
            region: "north",
            height:150,
            split:true,
            showFilter:this.closeFilter.createDelegate(this)
        });
    },
    exportReport : function(rid,exptype){
        var url = "../../exportcustomreport.jsp?m=exportReport&reportid="+rid+"&type="+exptype+"&reportname="+this.reportname+"&filter="+this.advFilter.getFilterData();
        setDldUrl(url);
    }
});

/*---component for display list of reports to open */
Wtf.ReportListWin = function(config){
    Wtf.apply(this,config);

    this.addEvents({
        storeLoad : true
    });
    this.reportRec = Wtf.data.Record.create([
    {
        name :'reportID'
    },{
        name :'reportName'
    },{
        name :'type'
    },{
        name :'description'
    },{
        name :'createdBy'
    },{
        name :'createdON'
    },{
        name :'modifiedON'
    },{
        name :'milestone'
    },{
        name :'paging'
    },{
        name:'category'
    }
    ]);
    this.store = new Wtf.data.GroupingStore({
        url:"../../reports.jsp",
        groupField:'category',
        baseParams : {
            m:'getAllReports'
        },
        sortInfo: {
            field: 'type',
            direction: "ASC"
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.reportRec)
    });
    this.store.load();
    this.sm =new Wtf.grid.CheckboxSelectionModel({
        singleSelect : true
    });
    this.cm = new Wtf.grid.ColumnModel([
        this.sm,
        {
            header : WtfGlobal.getLocaleText('pm.report.reporttype'),
            dataIndex:'category'
        },{
            header : WtfGlobal.getLocaleText('pm.admin.project.customrep.reportname'),
            dataIndex:'reportName'
        },{
            header :WtfGlobal.getLocaleText('lang.description.text'),
            dataIndex:'description'
        },{
            header:WtfGlobal.getLocaleText('lang.action.text'),
            dataIndex:'reportID',
            hidden: Wtf.UPerm.CreateDeleteReports ? false : true,
            width:30,
            renderer : function(value, css, record, row, column, store){
                return "<div class='deletecrc' wtf:qtip='"+WtfGlobal.getLocaleText('pm.customreport.delete.tip')+"'>&nbsp;</div>";
            }
        }
        ]);

    this.groupingView = new Wtf.grid.GroupingView({
        forceFit: true,
        showGroupName: false,
        enableGroupingMenu: true,
        groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Reports" : "Report"]})',
        hideGroupedColumn: true,
        emptyText: '<div class="emptyGridText" style="margin: 0px;">'+WtfGlobal.getLocaleText('pm.customreport.grid.emptytext')+'</div>'
    });

    this.grid = new Wtf.grid.GridPanel({
        store: this.store,
        cm:this.cm,
        sm:this.sm,
        view:this.groupingView,
        height:250,
        autoScroll:true,
        layout:'fit',
        border:false
    });
    Wtf.ReportListWin.superclass.constructor.call(this, {
        title:WtfGlobal.getLocaleText('pm.admin.project.customrep.reportlist'),
        modal:true,
        items:[this.grid],
        bodyStyle:"background:#ffffff",
        buttons:[{
            text:WtfGlobal.getLocaleText('pm.admin.project.customrep.openintab'),
            disabled:true,
            scope: this,
            id: 'open',
            handler: function(btn){
                var rec = this.sm.getSelected();
                this.fireEvent('onrunreport', this, rec, btn);
                return;
            }
        },{
            text:WtfGlobal.getLocaleText('pm.admin.project.customrep.run'),
            disabled : true,
            scope:this,
            id: 'set',
            hidden : welcome ,
            handler: function(btn){
                var rec = this.sm.getSelected();
                this.fireEvent('onrunreport', this, rec, btn);
                return;
            }
        },{
            text:WtfGlobal.getLocaleText('lang.cancel.text'),
            scope:this,
            handler: function(){
                this.close();
            }
        }]
    });
}
Wtf.extend(Wtf.ReportListWin, Wtf.Window, {
    initComponent: function(config){
        Wtf.ReportListWin.superclass.initComponent.call(this, config);
        this.addEvents({
            'onrunreport': true
        })
        this.grid.on("cellclick",this.deletehandler,this);
        this.grid.getSelectionModel().addListener('selectionchange', this.rowSelectionHandler, this);
        this.store.on("load",function(str){
            this.grid.getView().refresh();
        },this)
    },
    rowSelectionHandler: function(sm){
        var setButton = Wtf.getCmp("open");
        var openButton = Wtf.getCmp("set");
        if(sm.getSelections().length > 0){
            setButton.setDisabled(false);
            openButton.setDisabled(false);
        }else{
            setButton.setDisabled(true);
            openButton.setDisabled(true);
        }
    },
    deletehandler : function(obj,row,col,e){
        if(col == 4){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.331'), function(btn){
                if (btn == "yes") {
                    Wtf.Ajax.request({
                        url: "../../reports.jsp?m=deleteReport",
                        method: 'POST',
                        params: {
                            reportId:this.sm.getSelected().data['reportID'],
                            reportname:this.sm.getSelected().data['reportName']
                        },
                        success: function(response, e){
                            var resobj = eval( "(" + response.responseText.trim() + ")" );
                            if(resobj.success){
                                bHasChanged=true;
                                if(refreshDash.join().indexOf("all") == -1)
                                    refreshDash[refreshDash.length] = 'all';
                                if(this.fromDashboard){
                                    Wtf.MessageBox.show({
                                        title: WtfGlobal.getLocaleText('lang.success.text'),
                                        msg: WtfGlobal.getLocaleText('pm.Help.delReport'),
                                        buttons: Wtf.MessageBox.OK,
                                        icon: Wtf.MessageBox.INFO,
                                        fn: function(id){
                                            var dash = Wtf.getCmp('tabdashboard');
                                            refreshDashboard(dash);
                                        }
                                    });
                                    this.close();
                                } else {
                                    msgBoxShow([WtfGlobal.getLocaleText('lang.success.text'),WtfGlobal.getLocaleText('pm.Help.delReport')], 0);
                                    this.store.load();
                                }
                            }
                            else{
                                msgBoxShow([WtfGlobal.getLocaleText('pm.common.failure'),WtfGlobal.getLocaleText('pm.Help.failDelReport')], 1);
                            }
                        },
                        failure: function(response, e){
                            msgBoxShow([WtfGlobal.getLocaleText('pm.common.failure'), WtfGlobal.getLocaleText('pm.Help.connFail')], 1);
                        },
                        scope: this
                    })
                }
            }, this);
        }
    }
})

function getReportPanel(rec, options){
    var custreport = Wtf.getCmp('tabcustomreport'+rec.data.reportID+options.id);
    if(!custreport){
        custreport = new Wtf.CustomReportGrid({
            reportname:rec.data.reportName,
            reportConfig: rec.data,
            closable:true,
            layout:'fit',
            reportID:rec.data.reportID,
            autoDestroy:true,
            searchable: options.searchable,
            exportable: options.exportable,
            showFilters: options.showFilters,
            customPageSize: options.customPageSize,
            showTitle: options.showTitle,
            id:'tabcustomreport'+rec.data.reportID+options.id
        });
    } else {
        return null;
    }
    return custreport;
}




/*------------------ For custom Report Builder-------------------------
 *  gives option to create and define new reports                     */


/** component for Report Builder **/

Wtf.cc.CustomReportPanel = function(config){
    Wtf.apply(this, config);

    /*-------------create left panel for field names----------------*/
    this.reportTypeStore = new Wtf.data.SimpleStore({
        fields:['name'],
        data: Wtf.cc.reprttypes,
        autoLoad: true
    });
    this.reportTypeCombo = new Wtf.form.ComboBox({
        store:this.reportTypeStore,
        displayField: 'name',
        valueField: 'name',
        forceSelection: true,
        selectOnFocus: true,
        value:"Custom",
        disabled:true
    });
    Wtf.apply(this.reportTypeCombo, comboConfig);
    this.columnSM =new Wtf.grid.CheckboxSelectionModel({
        singleSelect : true
    })
    this.ccm = new Wtf.grid.ColumnModel([
        this.columnSM,
        {
            header: WtfGlobal.getLocaleText('pm.admin.company.customcol.columntype'),
            dataIndex: "tableName",
            renderer:function(val){
                if(val=="project")
                    return WtfGlobal.getLocaleText('pm.project.text');
                else if(val=="ccdata")
                    return WtfGlobal.getLocaleText('pm.admin.company.customcol.title');
                else if(val=="users" || val=="userlogin")
                    return WtfGlobal.getLocaleText('pm.project.home.members.text');
                else
                    return val;
            }
        },{
            header: WtfGlobal.getLocaleText('pm.admin.company.customcol.columnheader'),
            dataIndex: "headerkey",
            renderer:function(val,css, record, row, column, store){
                if(record.data.tableName!='ccdata')
                    return WtfGlobal.getLocaleText(val);
                return val;
            }
        },{
            header: WtfGlobal.getLocaleText('lang.type.text'),
            dataIndex: "type",
            dbname:"",
            renderer: function(val) {
                return Wtf.cc.getcolumntype(val);
            }
        }
        ]);

    this.columnsView = new Wtf.grid.GroupingView({
        forceFit: true,
        showGroupName: false,
        enableGroupingMenu: true,
        //groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Columns" : "Column"]})',
        hideGroupedColumn: true
    });
    this.columnStore = new Wtf.data.GroupingStore({
        url:"../../reports.jsp",
        groupField:"tableName",
        sortInfo: {
            field: 'header',
            direction: "ASC"
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, Wtf.cc.reportColumnRec)
    });

    this.columnGrid = new Wtf.grid.GridPanel({
        autoScroll:true,
        layout: 'fit',
        region:"west",
        split:'true',
        width:"25%",
        sm: this.columnSM,
        cm: this.ccm,
        store: this.columnStore,
        view:this.columnsView,
        tbar:['Report Type:',this.reportTypeCombo],
        ddGroup: 'ColGridDDGroup',
        enableDragDrop : true

    });
    /*---------------end of left panel ---------------------------*/


    /*----------------------------------------------------------
 * Buttons for report builder grid */

    this.btnSave = new Wtf.Toolbar.Button({
        text : WtfGlobal.getLocaleText('pm.common.save'),
        iconCls : 'dpwnd saveicon1',
        scope : this,
        handler : this.saveReport
    });
    this.btnMoveUp = new Wtf.Toolbar.Button({
        text : WtfGlobal.getLocaleText('pm.common.moveup'),
        iconCls : 'dpwnd cr_moveup',
        scope : this,
        disabled:true,
        handler : function(){
            this.changeOrder(-1);
        }
    });
    this.btnMoveDown = new Wtf.Toolbar.Button({
        text : WtfGlobal.getLocaleText('pm.common.movedown'),
        iconCls : 'dpwnd cr_movedown',
        scope : this,
        disabled:true,
        handler : function(){
            this.changeOrder(1);
        }
    });


    /*---------------Right panel ---------------------------------*/

    var quickSearchCol = new Wtf.grid.CheckColumn({
        header: WtfGlobal.getLocaleText('pm.common.quicksearch'),
        dataIndex: 'quickSearch',
        width:40
    });
    this.summaryTypeStore = new Wtf.data.SimpleStore({
        fields:['id','name'],
        data: [['sum','sum'],['count','count'],['max','max'],['min','min'],['average','average']],
        autoLoad: true
    });
    this.summaryCombo = new Wtf.form.ComboBox({
        store:this.summaryTypeStore,
        displayField: 'name',
        valueField: 'id',
        forceSelection: false,
        selectOnFocus: true,
        mode:'local',
        editable:false,
        typeAhead : true,
        triggerAction: 'all'
    });
//    Wtf.apply(this.summaryCombo, comboConfig);
    this.reportSM =new Wtf.grid.CheckboxSelectionModel({
        singleSelect : true
    });
    var headerEditor = new Wtf.form.TextField({
        allowBlank:false
    })

    this.reportCM = new Wtf.grid.ColumnModel([
        this.reportSM,
        {
            header:WtfGlobal.getLocaleText('pm.admin.company.customcol.columnheader'),
            dataIndex: "headerkey",
            renderer:function(val,css, record, row, column, store){
                if(record.data.tableName!='ccdata')
                    return WtfGlobal.getLocaleText(val);
                return val;
            }

        },{
            header:WtfGlobal.getLocaleText('pm.admin.project.customrep.displayheader'),
            dataIndex:"displayHeader",
            renderer : function(text){
                return WtfGlobal.HTMLStripper(text);
            },
            editor: headerEditor
        },{
            header:WtfGlobal.getLocaleText('pm.admin.company.customcol.columntype'),
            dataIndex:'type',
            renderer: function(val){
                return Wtf.cc.getcolumntype(val);
            }
        },{
            header:WtfGlobal.getLocaleText('pm.admin.project.customrep.summarytype'),
            dataIndex:'summary',
            renderer:this.comboBoxRenderer(this.summaryCombo),
            editor:this.summaryCombo
        },quickSearchCol,
        {
            header : WtfGlobal.getLocaleText('lang.action.text'),
            dataIndex: 'colid',
            width:35,
            renderer:function(value, css, record, row, column, store){
                if(!record.data.isMandatory)
                    return "<div class='deletecrc' wtf:qtip='"+WtfGlobal.getLocaleText('pm.customcolumn.delete.tip')+"'>&nbsp;</div>";
                else
                    return ' ';
            }
        }
        ]);
    this.reportStore = new Wtf.data.Store({
        reader: new Wtf.data.ArrayReader({
            id:0
        }, Wtf.cc.reportColumnRec)
    });

    this.reportBuilderGrid = new Wtf.grid.EditorGridPanel({
        cm: this.reportCM,
        ddGroup :'GridDDGroup',
        id : 'customReportConfigGrid',
        store: this.reportStore,
        clicksToEdit :1,
        sm: this.reportSM,
        layout:'fit',
        viewConfig: {
            forceFit: true
        },
        plugins:[quickSearchCol]
    });

    Wtf.cc.CustomReportPanel.superclass.constructor.call(this,{
        layout: 'border',
        bodyStyle:"background-color:#DFE8F6",
        items:[new Wtf.Panel({
                region:'north',
                collapsible:true,
                bodyStyle:"padding:2px 12px 2px 2px; background:#6187B9; color: white;",
                html:WtfGlobal.getLocaleText('pm.admin.project.customrep.helpmsg')
            }),
        this.columnGrid,
        new Wtf.Panel({
            border: false,
            layout:'fit',
            region: "center",
            tbar: [this.btnSave,'-',this.btnMoveUp,'-',this.btnMoveDown],
            items:this.reportBuilderGrid
        })]
    });
}
Wtf.extend(Wtf.cc.CustomReportPanel, Wtf.Panel, {
    initComponent: function(config){
        Wtf.cc.CustomReportPanel.superclass.initComponent.call(this, config);

        this.loadColumnName();
        this.reportBuilderGrid.on("render",function(){
            var gridDropTargetEl = this.reportBuilderGrid.getView().el.dom.childNodes[0].childNodes[1]
            var destGridDropTarget = new Wtf.dd.DropTarget(gridDropTargetEl, {
                ddGroup: 'ColGridDDGroup',
                notifyDrop : function(ddSource, e, data){
                    // Generic function to add records.
                    function addRow(record, index, allItems) {
                        this.reportStore.add(record);
                        this.columnStore.remove(record);
                    }
                    // Loop through the selections
                    Wtf.each(ddSource.dragData.selections ,addRow,this);
                    return(true);
                }.createDelegate(this)
            },this);
        },this);

        this.reportTypeCombo.on("select", function(){
            this.loadColumnName();
            this.reportStore.removeAll();
        }, this);
        this.summaryCombo.on("beforeselect",this.isSummaryType,this);
        this.columnStore.on("load",this.loadFixedColumns,this);
        this.reportBuilderGrid.on("cellclick", this.gridcellClickHandle, this);
        this.reportSM.on("rowselect",this.enabledArrow, this);
        this.reportSM.on("rowdeselect",this.disabledArrow,this);
        this.reportBuilderGrid.on('afteredit', this.GridAfterEdit, this);
    },
    onRender: function(config){
        Wtf.cc.CustomReportPanel.superclass.onRender.call(this, config);

    },
    loadColumnName:function(){
        this.columnStore.load({
            params:{
                type : this.reportTypeCombo.getValue(),
                m:"getReportsColumnAll"
            }
        })
    },
    loadFixedColumns :function(){
        this.reportStore.removeAll();
        this.columnStore.filter("isMandatory","true");
        for(var i =0; i<this.columnStore.getCount(); i++){
            var rec = this.columnStore.getAt(i);
            rec.set("quickSearch",true);
            this.reportStore.add(rec);
            this.columnStore.remove(rec);
        }
        this.columnStore.clearFilter();
    },
    changeOrder : function(move){
        var rec = this.reportSM.getSelected();
        var idx = this.reportStore.indexOf(rec);
        move = move+idx;
        if(move>-1 && move<this.reportStore.getCount()){
            this.reportStore.remove(rec);
            this.reportStore.insert(move,rec);
            this.reportSM.selectRow(move);
        }
    },
    saveReport: function(){
        var saveWin = new Wtf.saveReportWin({
            width:470,
            height:370,
            store:this.reportStore,
            type:this.reportTypeCombo.getValue(),
            successHandler:this.saveReportHandler.createDelegate(this)
        });
        saveWin.show();
    },
    isSummaryType: function(combo,record,index){
        var a = this.reportSM.getSelected().data.type;
        if(a == Wtf.cc.columntype.NUMBER_FIELD || ((a==Wtf.cc.columntype.TEXT_FIELD ||a==Wtf.cc.columntype.CHECKBOX) && record.data.id=="count"))
            return combo.getValue();
        else{
            msgBoxShow(320, 1);
            return false;
        }
    },
    GridAfterEdit:function(e){
        if (e.field == 'displayHeader') {
            var nVal = WtfGlobal.HTMLStripper(e.value);
            if(nVal.length==0 || nVal>50)
                e.record.set('displayHeader',e.originalValue);
            else
                e.record.set('displayHeader',nVal);

        }
    },
    gridcellClickHandle: function(obj,row,col,e){
        if(col==5) {   // if column is quick search column
            if( this.reportSM.getSelected().data.type==Wtf.cc.columntype.TEXT_FIELD){
                var rec = this.reportSM.getSelected();
                if(!rec.data.isMandatory)
                    rec.set("quickSearch",!rec.data["quickSearch"]);
            }else{
                msgBoxShow(321, 1);
            }
        }
        if(col==6){
            rec =this.reportSM.getSelected();
            if(!rec.data.isMandatory){
                this.reportStore.remove(rec);
                this.columnStore.add(rec);
                this.columnStore.groupBy('tableName',true);
                this.reportBuilderGrid.getView().refresh();
            }
        }
    },
    comboBoxRenderer : function(combo) {
        return function(value) {
            var idx = combo.store.find(combo.valueField, value);
            if(idx == -1)
                return "";
            var rec = combo.store.getAt(idx);
            return rec.get(combo.displayField);
        };
    },
    saveReportHandler : function(){
        this.loadColumnName();
    },
    enabledArrow: function(e,row,rec){
        if(this.reportStore.getCount()>1){
            if(e.hasPrevious())
                this.btnMoveUp.setDisabled(false);
            else
                this.btnMoveUp.setDisabled(true);
            if(e.hasNext())
                this.btnMoveDown.setDisabled(false);
            else
                this.btnMoveDown.setDisabled(true);
        }
    },
    disabledArrow: function(e,row,rec){
        this.btnMoveDown.setDisabled(true);
        this.btnMoveUp.setDisabled(true);
    }

});



/*---------------- Form for Save Report --------------------*/

Wtf.saveReportWin = function(config){
    Wtf.apply(this,{
        title:WtfGlobal.getLocaleText('pm.report.save.text'),
        id:'savereportWin',
        closable: true,
        modal: true,
        iconCls: 'dpwnd saveicon1',
        width: 470,
        height:370,
        resizable: false,
        layout: 'border',
        buttonAlign: 'right',
        buttons: [{
            text: WtfGlobal.getLocaleText('pm.common.save'),
            scope: this,
            handler:this.saveForm.createDelegate(this)
        }, {
            text: WtfGlobal.getLocaleText('lang.cancel.text'),
            scope: this,
            handler: function(){
                this.close();
            }
        }]
    },config);
    Wtf.saveReportWin.superclass.constructor.call(this, config);
}

Wtf.extend( Wtf.saveReportWin, Wtf.Window, {
    onRender: function(config){
        Wtf.saveReportWin.superclass.onRender.call(this, config);
        this.createForm();
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(WtfGlobal.getLocaleText('pm.customreport.create.title'),WtfGlobal.getLocaleText('pm.customreport.create.title')+"<br><span style=\"float:right; \">"+WtfGlobal.getLocaleText('pm.common.requiredfields')+"</span>", "../../images/create-report.jpg")
        },{
            region: 'center',
            border: false,
            bodyStyle: 'background:#f1f1f1;font-size:10px;',
            autoScroll:true,
            items:this.reportConfigInfo
        });
    },
    createForm:function(){

        this.reportConfigInfo= new Wtf.form.FormPanel({
            baseParams:{
                m:"insertReport"
            },
            url:"../../reports.jsp",
            region:'center',
            bodyStyle: "background: transparent;",
            border:false,
            style: "background: transparent;padding:20px;",
            defaultType:'textfield',
            labelWidth:135,
            items:[ {
                name:'reportno',
                xtype:'hidden',
                value : 0
            },{
                fieldLabel:WtfGlobal.getLocaleText('pm.admin.project.customrep.reportname')+'* ',
                name:'reportname',
                id:'reportname',
                width:220,
                maxLength:50,
                allowBlank:false,
                regex: /[a-zA-Z0-9]+/
            },{
                fieldLabel: WtfGlobal.getLocaleText('pm.report.description'),
                name: 'description',
                id:'rdescription',
                width:220,
                maxLength:250,
                xtype:'textarea'
            },{
                fieldLabel: WtfGlobal.getLocaleText("pm.report.includemilestones"),
                name: 'includemilestones',
                id:'includemilestones',
                xtype:'checkbox',
                width:'16px',
                style: (Wtf.isIE6 || Wtf.isIE7) ? '': 'margin-top:5px;'
            },{
                fieldLabel: WtfGlobal.getLocaleText("pm.report.enablepaging"),
                name: 'enablepaging',
                id:'enablepaging',
                xtype:'checkbox',
                width:'16px',
                style: (Wtf.isIE6 || Wtf.isIE7) ? '': 'margin-top:5px;'
            }
            ]
        });
    },
    saveForm:function(){
        if(this.reportConfigInfo.getForm().isValid()){
            var jsonData = "[";
            var recCnt = this.store.getCount();
            for(var cnt = 0; cnt < recCnt; cnt++) {
                var record = this.store.getAt(cnt);
                jsonData += this.getJsonFromRecord(record,cnt) + ",";
            }
            jsonData = jsonData.substr(0, jsonData.length - 1) + "]";
            this.reportConfigInfo.getForm().submit({
                params : {
                    columns : jsonData,
                    type: this.type,
                    reportid : this.reportID
                },
                waitMsg:WtfGlobal.getLocaleText('pm.customreport.save.waitmsg'),
                success:function(f,a){
                    var res = eval('('+a.response.responseText+')');
                    this.genSaveSuccessResponse(res)
                },
                failure:function(f,a){
                    this.genSaveFailureResponse(eval('('+a.response.responseText+')'))
                },
                scope:this
            });
        }
    },

    genSaveSuccessResponse:function(response){
        msgBoxShow([WtfGlobal.getLocaleText('pm.report.save.text'),WtfGlobal.getLocaleText('pm.Help.createreport')], 3);
        Wtf.getCmp('savereportWin').close();
        bHasChanged = true;
        this.successHandler();
    },
    genSaveFailureResponse:function(response){
        if(response.data.indexOf("Duplicate")){
            msgBoxShow(278,1);
        }else{
            var msg=WtfGlobal.getLocaleText('pm.Help.connFail');
            if(response.msg)
                msg=response.msg;
            Wtf.getCmp('savereportWin').close();
            msgBoxShow([WtfGlobal.getLocaleText('pm.report.save.text'),msg],1);
        }
    },
    getJsonFromRecord : function(record, cnt) {
        var jsonData = "{";
        var dataObj = record.data
        for (var dataIndex in dataObj) {
            jsonData += dataIndex+":\""+record.data[dataIndex]+"\",";
        }
        jsonData += 'displayOrder'+":\""+cnt+"\"}";
        return(jsonData);
    }
});
