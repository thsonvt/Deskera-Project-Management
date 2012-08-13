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
Wtf.ImportLog = function(config){
    Wtf.ImportLog.superclass.constructor.call(this,config);
};

Wtf.extend(Wtf.ImportLog, Wtf.Panel, {

    initComponent : function(config){
        this.limit = 20;
        Wtf.ImportLog.superclass.initComponent.call(this, config);
        this.loadMask = mainPanel.loadMask;
        var logRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'filename'},
            {name: 'svnname'},
            {name: 'type'},
            {name: 'log'},
            {name: 'totalrecs'},
            {name: 'rejected'},
            {name: 'timestamp',type: 'date',dateFormat: 'Y-m-j H:i:s'},
            {name: 'importedrec'},
            {name: 'errorcount'},
            {name: 'modulename'},
            {name: 'username'},
            {name: 'projectname'},
            {name: 'logkey'}
        ]);

        var logReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: 'count'
        },logRec);

        var logds = new Wtf.data.Store({
            id : this.id+'logds',
            reader: logReader,
            url: '../../jspfiles/getAuditLog.jsp',
            method : 'GET'
        });

        var gridcm = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header: WtfGlobal.getLocaleText('pm.common.module.text'),
                dataIndex: 'modulename',
                width: 80,
                renderer:function(val){
                    return WtfGlobal.getLocaleText(val);
                }
            },{
                header: WtfGlobal.getLocaleText('pm.common.filename'),
                sortable: true,
                dataIndex: 'filename',
                width: 140
            },{
                header: WtfGlobal.getLocaleText('pm.importlog.filetype'),
                dataIndex: 'type',
                width: 60,
                fixed: true,
                renderer: function(val){
                    return typeof val == 'string' ? val.toUpperCase() : val;
                }
            },{
                header: WtfGlobal.getLocaleText('pm.importlog.importedby'),
                sortable: true,
                dataIndex: 'username',
                width: 110
            },{
                header: WtfGlobal.getLocaleText('pm.createeditproject.projectname'),
                sortable: true,
                dataIndex: 'projectname',
                width: 170
            },{
                header: WtfGlobal.getLocaleText('pm.importlog.importedon'),
                sortable: true,
                dataIndex: 'timestamp',
                renderer : WtfGlobal.userPrefDateRenderer,
                width: 160
            },{
                header: WtfGlobal.getLocaleText('pm.importlog.total'),
                align: "left",
                dataIndex: 'totalrecs',
                fixed: true,
                width: 80,
                renderer: function(val){
                    return val < 0 ? 0 : val;
                }
            },{
                header: WtfGlobal.getLocaleText('pm.importlog.imported'),
                align: "left",
                dataIndex:'importedrec',
                fixed: true,
                width: 105

            },{
                header: WtfGlobal.getLocaleText('pm.importlog.rejected'),
                align: "left",
                dataIndex: 'rejected',
                width: 100,
                fixed: true,
                renderer: function(val){
                    return val < 0 ? 0 : val;
                }
            },{
                header: WtfGlobal.getLocaleText('pm.importlog.errors'),
                align: "left",
                dataIndex: 'errorcount',
                width: 120,
                fixed: true,
                renderer: function(val){
                    return val < 0 ? 0 : val;
                }
            },{
                header: WtfGlobal.getLocaleText('pm.audittrail.logdesc'),
                sortable: true,
                dataIndex: 'logkey',
                width: 250,
                renderer: function(val, rec, obj, ri, ci, store){
                    if(val == ''){
                       var total = obj.data.totalrecs;
                       if(total <= 0){
                           var txt = WtfGlobal.getLocaleText('pm.importlog.canceled');
                           return "<div wtf:qtip=\""+txt+"\">"+txt+"</div>";
                       }
                    }
                    return "<div wtf:qtip=\""+WtfGlobal.getLocaleText(val)+"\">"+WtfGlobal.getLocaleText(val)+"</div>";
                }
            },{
                header: WtfGlobal.getLocaleText('pm.importlog.original'),
                sortable: true,
                fixed: true,
                dataIndex: 'totalrecs',
                align: "center",
                width: 80,
                renderer : function(val, rec, obj, ri, ci, store){
                    if(val > 0)
                        return "<div class=\"pwnd downloadIcon\" id=\"original"+ri+"_"+ci+"\" wtf:qtip=\""+WtfGlobal.getLocaleText('pm.Help.ilorigfile')+"\" style=\"height:16px;width:16px;\">&nbsp;</div>";
                    else return "--";
                }
           },{
                header: WtfGlobal.getLocaleText('pm.importlog.rejectedfile'),
                sortable: true,
                fixed: true,
                align: "center",
                dataIndex: 'rejected',
                width: 80,
                renderer : function(val, rec, obj, ri, ci, store){
                   var totalrec = obj.data.totalrecs;
                   if(val > 0 && totalrec > 0)
                       return "<div class=\"pwnd downloadIcon\" id=\"rejected"+ri+"_"+ci+"\" wtf:qtip=\""+WtfGlobal.getLocaleText('pm.Help.ilrejfile')+"\" style=\"height:16px;width:16px;\">&nbsp;&nbsp;</div>";
                   else return "--";
                }
           },{
                header: WtfGlobal.getLocaleText('pm.importlog.errorsfile'),
                sortable: true,
                fixed: true,
                align: "center",
                dataIndex: 'errorcount',
                width: 80,
                renderer : function(val, rec, obj, ri, ci, store){
                   var totalrec = obj.data.totalrecs;
                   if(val > 0 && totalrec > 0)
                       return "<div class=\"pwnd downloadIcon\" id=\"error"+ri+"_"+ci+"\" wtf:qtip=\""+WtfGlobal.getLocaleText('pm.Help.ilerrorfile')+"\" style=\"height:16px;width:16px;\">&nbsp;&nbsp;</div>";
                   else return "--";
                }
             }
        ]);

        var topBar = [
            this.quickSearchTF = new Wtf.KWLTagSearch({
                id : 'user'+this.id,
                width: 200,
                field : "username",
                emptyText: WtfGlobal.getLocaleText('pm.search.module.emptytext')
            }),
            WtfGlobal.getLocaleText('pm.audittrail.datefilters.text')+': ',
            new Wtf.form.DateField({
                emptyText: WtfGlobal.getLocaleText('pm.common.fromdate'),
                readOnly: true,
                format: 'Y-m-d',
                id: this.id+'_filtersdate',
                maxValue: new Date().format('Y-m-d'),
                maxValueText: WtfGlobal.getLocaleText('pm.audittrail.maxvaltext')
            })
            ,'    ',
            new Wtf.form.DateField({
                emptyText: WtfGlobal.getLocaleText('pm.common.todate'),
                readOnly: true,
                format: 'Y-m-d',
                id: this.id+'_filteredate',
                maxValue: new Date().format('Y-m-d'),
                maxValueText: WtfGlobal.getLocaleText('pm.audittrail.maxvaltext')
            })
            , '->' ,
            new Wtf.Button({
                text: WtfGlobal.getLocaleText('pm.audittrail.clear'),
                id: this.id+'_clearbtn',
                iconCls: 'pwnd clearfiltericon'
            })
        ];

        this.loggrid = new Wtf.grid.GridPanel({
             id: this.id+'_loggrid',
             store: logds,
             cm: gridcm,
             border : false,
             loadMask : true,
             viewConfig: {
                emptyText: '<div class="emptyGridText">'+WtfGlobal.getLocaleText('pm.importlog.emtytext')+'</div>'
             },
             stripeRows: true,
             bbar:
                 this.pagingToolbar = new Wtf.PagingSearchToolbar({
                     pageSize: 20,
                     id: this.id+"_pagingtoolbar",
                     searchField: this.quickSearchTF,
                     store: logds,
                     displayInfo: true,
//                     displayMsg: WtfGlobal.getLocaleText('pm.paging.displayrecord'),
                     emptyMsg: WtfGlobal.getLocaleText('pm.common.nodatadisplay'),
                     plugins: this.pP = new Wtf.common.pPageSize({
                        id : this.id+"_pPageSize"
                     })
                })
        });
        
        this.innerpanel = new Wtf.Panel({
            id : this.id+'_innerpanel',
            layout : 'fit',
            border : false,
            items: [this.loggrid],
            tbar: topBar
        });
        this.add(this.innerpanel);

        logds.baseParams = {rtype: 'importlog'};
        logds.load({
            params: {
                start: 0,
                limit: (this.pagingToolbar.pageSize > this.limit)? this.limit : this.pagingToolbar.pageSize
            }
        });

        logds.on("load", function(dsObj, rs, opt) {
            var i = 0;
            for (i = 0; i < rs.length; i++) {
                var obj = rs[i];
                if(obj.totalrecs <= 0){
                    if(obj.rejected <= 0 && obj.importedrecs <= 0 && obj.errorcount <= 0){
                        obj.log = WtfGlobal.getLocaleText("pm.importlog.cancelled");
                    }
                }
            }
            hideMainLoadMask();
            Wtf.getCmp(this.id+"_pagingtoolbar").bind(logds);
            this.quickSearchTF.StorageChanged(logds);
            this.quickSearchTF.on('SearchComplete', this.QuickSearchComplete, this);
            this.loggrid.getView().refresh();
        },this);

        logds.on('datachanged', function(){
            this.quickSearchTF.setPage(this.pP.combo.value);
        }, this);

        logds.on('loadexception', function(){
            msgBoxShow(4, 1);
        }, this);

        this.loggrid.on('cellclick', function(grid, ri, ci, e){
            if(ci == 12 || ci == 13 || ci == 14){
                if(e.target.tagName == 'DIV' && e.target.id.indexOf(ri+'_'+ci) > -1){
                    var rec = grid.getStore().getAt(ri);
                    var format = 'error';
                    if(ci == 12)
                        format = 'original';
                    else if(ci == 13)
                        format = 'rejected';
                    Wtf.get('downloadframe').dom.src = '../../fdownload.jsp?type=importedfiles&fileid='
                        + rec.get('svnname') + '&format=' + format + '&module='+ rec.get('modulename') + '&filename='+ rec.get('filename');
                }
            }
        }, this);

        this.loggrid.myScrollLeft = 25;
        this.loggrid.getSelectionModel().on('selectionchange', function(sm){
            var gv = this.loggrid.getView();
            if(gv.scroller.dom.scrollLeft > 0){
                this.loggrid.myScrollLeft = gv.scroller.dom.scrollLeft;
            }
        }, this);

        Wtf.getCmp(this.id+'_filtersdate').on('change', function(df, nVal, oVal){
            this.fromDateChange(df, nVal, oVal);
        }, this);

        Wtf.getCmp(this.id+'_filteredate').on('change', function(df, nVal, oVal){
            this.toDateChange(df, nVal, oVal);
        }, this);

        Wtf.getCmp(this.id+'_clearbtn').on('click', function(btn, e){
            Wtf.getCmp(this.id+'_filtersdate').reset();
            Wtf.getCmp(this.id+'_filteredate').reset();
            this.quickSearchTF.reset();
            this.loggrid.store.baseParams = {rtype: 'importlog'};
            this.loggrid.store.load({
                params: {
                    start: 0,
                    limit: (this.pagingToolbar.pageSize > this.limit)? this.limit : this.pagingToolbar.pageSize
                }
            });
        }, this);

        this.loggrid.getView().override({
            focusRow: function(row){
                this.focusCell(row, parseInt(this.grid.myScrollLeft/25), false);
            }
        });
     },

    QuickSearchComplete: function(e){
        this.loggrid.getView().refresh();
    },

    fromDateChange: function(df, nVal, oVal){
        this.toDateVal = Wtf.getCmp(this.id+'_filteredate').getValue();
        this.fromDateVal = nVal.format('Y-m-j H:i:s');
        if(this.toDateVal){
            this.toDateVal = this.getTimeInDate(this.toDateVal);
            if(!(this.toDateVal > nVal)){
                msgBoxShow(286, 1);
                Wtf.getCmp(this.id+'_filtersdate').reset();
                this.fromDateVal = '';
                return;
            } else {
                this.toDateVal = this.toDateVal.format('Y-m-j H:i:s');
            }
        }
        this.getLog();
    },

    toDateChange: function(df, nVal, oVal){
        this.fromDateVal = Wtf.getCmp(this.id+'_filtersdate').getValue();
        nVal = this.getTimeInDate(nVal);
        this.toDateVal = nVal.format('Y-m-j H:i:s');
        if(this.fromDateVal){
            if(!(this.fromDateVal < nVal)){
                msgBoxShow(198, 1);
                Wtf.getCmp(this.id+'_filteredate').reset();
                this.toDateVal = '';
                return;
            } else {
                this.fromDateVal = this.fromDateVal.format('Y-m-j H:i:s');
            }
        }
        this.getLog();
    },

    getTimeInDate: function(nVal){
        if(typeof nVal == 'object'){
            nVal = Date.parseDate(nVal.format('Y-m-j') + ' 23:59:59', 'Y-m-j H:i:s');
        }
        return nVal;
    },

    getLog: function(combo, rec, index){
        this.loggrid.getStore().removeAll();
        this.loggrid.store.baseParams = {
            rtype: 'importlog',
            fromdate: this.fromDateVal,
            todate: this.toDateVal
        }
        this.loggrid.getStore().load({
            params: {
                start: 0,
                limit: (this.pagingToolbar.pageSize > this.limit)? this.limit : this.pagingToolbar.pageSize,
                ss: this.quickSearchTF.getValue()
            }
        });
    }
});
