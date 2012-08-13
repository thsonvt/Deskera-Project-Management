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
Wtf.AuditTrailComp = function(config){
    Wtf.AuditTrailComp.superclass.constructor.call(this,config);
//    this.addEvents({
//        "announce": true,
//        "dataRendered": true
//    });
};
Wtf.extend(Wtf.AuditTrailComp, Wtf.Panel,{

    onRender : function(config){
        this.limit = 200;
        Wtf.AuditTrailComp.superclass.onRender.call(this,config);
        this.loadMask = mainPanel.loadMask;
        var logRec = new Wtf.data.Record.create([
            {name: 'timestamp'},
            {name: 'actionby'},
            {name: 'description'},
            {name: 'projname'},
            {name: 'logid'}
        ]);
        
        var logReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: 'count'
        },logRec);

        this.logds = new Wtf.data.Store({
            id : this.id+'logds',
            reader: logReader,
            url: '../../jspfiles/getAuditLog.jsp',
            method : 'GET',
            baseParams:{
                action:5,
                admintype: "",
                projectname: ""
            },
            sortInfo: {field: 'timestamp', direction: 'DESC'}
        });

        var gridcm= new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header :WtfGlobal.getLocaleText('pm.audittrail.actionby'),
                dataIndex: 'actionby',
                autoSize : true,
                sortable: true
            },{
                header: WtfGlobal.getLocaleText('pm.audittrail.logdesc'),
                dataIndex: 'description',
                width: 550,
                sortable: true
            },{
                header: WtfGlobal.getLocaleText('pm.project.text'),
                dataIndex: 'projname',
                autoWidth : true,
                sortable: true
            },{
                header :WtfGlobal.getLocaleText('pm.common.timestamp'),
                dataIndex: 'timestamp',
                autoSize : true,
                sortable: true,
                align: 'center',
                renderer: WtfGlobal.dateRendererForServerDate
            }
            ]);

        this.quickSearchTF = new Wtf.KWLTagSearch({
            id : 'user'+this.id,
            width: 138,
            field : "username",
            emptyText: WtfGlobal.getLocaleText('pm.audittrail.search')
        });

        this.loggrid = new Wtf.grid.GridPanel({
            id: this.id+'_loggrid',
            store: this.logds,
            cm: gridcm,
            border : false,
            loadMask : true,
            emptyText: WtfGlobal.getLocaleText('pm.common.nodatadisplay'),
            stripeRows: true,
            viewConfig: { 
                forceFit: true
            },
            autoExpandColumn: 2,
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 100,
                id: this.id+"_pagingtoolbar",
                searchField: this.quickSearchTF,
                store: this.logds,
                displayInfo: true,
//                displayMsg: WtfGlobal.getLocaleText('pm.paging.displayrecord'),
                emptyMsg: WtfGlobal.getLocaleText('pm.paging.noresult'),
                plugins: this.pP = new Wtf.common.pPageSize({
                    id : this.id+"_pPageSize"
                })
            })
        });

        var mainStore = new Wtf.data.SimpleStore({
            fields: ['id', 'value'],
            data: [
                ['1', 'Project'],
                ['2', 'Administration'],
                ['3', 'Documents'],
                ['4', 'Notifications']
            ]
        });

        this.adminDataBlock = [
            ['User Administration'],
            ['Project Administration'],
            ['Company Administration']
        ];

        var comboRec = new Wtf.data.Record.create([
            {name: 'projectname'}
        ]);

        this.adminStore = new Wtf.data.SimpleStore({
            fields: ['projectname'],
            record: comboRec
        });

        var comboReader = new Wtf.data.KwlJsonReader({
            root: 'data',
            totalProperty: 'count'
        }, comboRec);

        this.dynamicStore = new Wtf.data.Store({
            id : "sCombods"+this.id,
            reader: comboReader,
            url: '../../jspfiles/getAuditLog.jsp',
            method : 'GET'
        });

       this.mainCombo = new Wtf.form.ComboBox({
            store: mainStore,
            mode: "local",
            width: 130,
            valueField: "id",
            typeAhead: false,
            editable: false,
            selectOnFocus: true,
            triggerAction: "all",
            displayField: "value",
            emptyText: WtfGlobal.getLocaleText('pm.audittrail.selectone'),
            id:this.id+'_mainCombo'
        });

        this.subCombo = new Wtf.form.ComboBox({
            store: this.adminStore,
            mode: "local",
            width: 150,
            typeAhead: true,
            editable: true,
            selectOnFocus: true,
            allowBlank: true,
            triggerAction: "all",
            displayField: "projectname",
            emptyText: WtfGlobal.getLocaleText('pm.audittrail.selectsubtype'),
            id:this.id+'_subCombo'
        });

        this.dynamicStore.load({
            params: {
                rtype: 'p',
                limit: 30,
                start: 0
            }
        });

        this.mainCombo.on('select', this.mainComboSelect, this);

        var topBar = [
        this.quickSearchTF,
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
        , '-' , '      '+ WtfGlobal.getLocaleText('pm.audittrail.otherfilters.text') +': ',

        Wtf.getCmp(this.id+'_mainCombo'), '      ',
        Wtf.getCmp(this.id+'_subCombo'), '->',
        new Wtf.Button({
            text: WtfGlobal.getLocaleText('pm.audittrail.clear'),
            id: this.id+'_clearbtn',
            iconCls: 'pwnd clearfiltericon'
        })
        ];

        this.innerpanel = new Wtf.Panel({
            id : this.id+'_innerpanel',
            layout : 'fit',
            border : false,
            items: [this.loggrid],
            tbar: topBar
        });
        this.add(this.innerpanel);
        Wtf.getCmp(this.id+'_subCombo').setDisabled(true);

       this.logds.baseParams = {type: ''};
        this.logds.load({
            params: {
                start: 0,
                limit: (this.pagingToolbar.pageSize > this.limit)? this.limit : this.pagingToolbar.pageSize
            }
        });

        this.searchPerformed = false;
        this.logds.on("load",function(dsObj, rs, opt) {
            hideMainLoadMask();
            Wtf.getCmp(this.id+"_pagingtoolbar").bind(this.logds);
            this.quickSearchTF.StorageChanged(this.logds);
            this.quickSearchTF.on('SearchComplete', this.QuickSearchComplete, this);
            this.loggrid.getView().refresh();
            if(dsObj.getCount() <= 0 && !this.searchPerformed)
                this.loggrid.getView().emptyText = '<div class="emptyGridText">'+WtfGlobal.getLocaleText('pm.audittrail.emptytext')+'</div>';
            else
                this.loggrid.getView().emptyText = '<div class="emptyGridText">'+WtfGlobal.getLocaleText('pm.audittrail.searchemptytext')+'</div>'
        },this);

        this.logds.on('datachanged', function(){
            this.quickSearchTF.setPage(this.pP.combo.value);
        }, this);
        
        this.logds.on('loadexception', function(){
            msgBoxShow(4, 1);
        }, this);

        Wtf.getCmp(this.id+'_filtersdate').on('change', function(df, nVal, oVal){
            this.fromDateChange(df, nVal, oVal);
        }, this);

        Wtf.getCmp(this.id+'_filteredate').on('change', function(df, nVal, oVal){
            this.toDateChange(df, nVal, oVal);
        }, this);

        Wtf.getCmp(this.id+'_clearbtn').on('click',
            function(btn, e){
                Wtf.getCmp(this.id+'_filtersdate').reset();
                Wtf.getCmp(this.id+'_filteredate').reset();
                this.mainCombo.reset();
                this.subCombo.reset();
                this.fromDateVal="";
                this.toDateVal="";
                this.quickSearchTF.reset();
                this.subCombo.setDisabled(true);
                this.loggrid.store.baseParams = {type: ''};
                this.loggrid.store.load({
                    params: {
                        start: 0,
                        limit: (this.pagingToolbar.pageSize > this.limit)? this.limit : this.pagingToolbar.pageSize
                    }
                });
            }, this);
    }, 
 
    QuickSearchComplete: function(e){
        this.loggrid.getView().refresh();
        this.searchPerformed = true;
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

    createSubCombo: function(val){
        var projNameDataBlock = new Array(this.dynamicStore.data.length);
        for(var i=0; i<this.dynamicStore.data.length; i++){
            projNameDataBlock[i] = new Array(1);
            projNameDataBlock[i][0] = this.dynamicStore.data.items[i].data['projectname'];
        }
        this.adminStore.removeAll();
        this.subCombo.setValue("");
        this.subCombo.setDisabled(false);

        if(val == '1'){
            this.adminStore.loadData(projNameDataBlock);
        } else if(val == '2'){
            this.adminStore.loadData(this.adminDataBlock);
        } else if(val == '3'){
            this.subCombo.setDisabled(true);
        }
        else if(val == '4'){
            this.subCombo.setDisabled(true);
        }
        this.subCombo.on('select', this.handleComboSelect, this);
    },

    mainComboSelect: function(combo, rec, index){
        var selection = rec.data['id'];
        switch(selection){
            case '1':
                this.createSubCombo('1');
                this.getLog(combo, rec, '1');
                break;
            case '2':
                this.createSubCombo('2');
                this.getLog(combo, rec, '2');
                break;
            case '3':
                this.createSubCombo('3');
                this.getLog(combo, rec, '3');
                break;
            case '4':
                this.createSubCombo('4');
                this.getLog(combo, rec, '4');
                break;
        }
    },

    handleComboSelect: function(combo, rec, index){
        this.fromDateVal = Wtf.getCmp(this.id+'_filtersdate').getValue();
        if(typeof this.fromDateVal == 'object')
            this.fromDateVal = this.fromDateVal.format('Y-m-j H:i:s');
        var td = Wtf.getCmp(this.id+'_filteredate').getValue();
        if(td && td !== ''){
            td = this.getTimeInDate(td);
            this.toDateVal = td.format('Y-m-j H:i:s');
        }
        this.getLog(combo, rec);
    },

    getLog: function(combo, rec, index){
        var select = '';
        var mode = 3;
        var action = '';
        if(combo !== undefined){
            if(combo.id == this.id+'_mainCombo'){
                select = rec.data['value'];
                if(index == '1'){
                    action = 3;
                } else if(index == '2'){
                    action = 4;
                } else if(index == '4'){
                    action = 5;
                }else {
                    action = 2;
                }
            } else {
                select = rec.data['projectname'];
                action = 0;
            }
        } else {
            var main = this.mainCombo.getValue();
            select = main;
            index = select;
            if(index == '1'){
                action = 3;
            } else if(index == '2'){
                action = 4;
            } else if(index == '4'){
                action = 5;
            } else if(index == '3'){
                action = 2;
            } else {
                 action = null;
            }
            if(this.subCombo.getValue() && this.subCombo.getValue() !== ''){
                select = this.subCombo.getValue();
                action = 0;
            }
        }
        if(select !== ''){
            if(select == 'User Administration'){
                mode = 0;
                action = 1;
            } else if(select == 'Project Administration'){
                mode = 1;
                action = 1;
            } else if(select == 'Company Administration'){
                mode = 2;
                action = 1;
            }
        }
        this.loggrid.getStore().removeAll();
        this.loggrid.store.baseParams = {
            rtype: '',
            type: action,
            admintype: mode,
            fromdate: this.fromDateVal,
            todate: this.toDateVal,
            pid:this.pid,
            projectname : select
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

var auditTrailComp = new Wtf.AuditTrailComp({
    layout: 'fit',
    id:'auditTrailComp'
});
Wtf.getCmp('tabaudittrail').add(auditTrailComp);
Wtf.getCmp('tabaudittrail').doLayout();
