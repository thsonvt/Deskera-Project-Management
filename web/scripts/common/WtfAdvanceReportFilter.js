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
comboConfig = {
    mode:'local',
    editable:false,
    allowBlank:false,
    typeAhead : true,
    triggerAction: 'all'
}
Wtf.advanceReportFilter = function(config){
    Wtf.apply(this, config);
    this.op = new Wtf.form.ComboBox({    //  combo for operator AND/OR
        store : this.opStore = new Wtf.data.SimpleStore({
            fields:['name'],
            data:[["AND"],["OR"]],
            autoLoad:true
        }),
        displayField:'name',
        valueField : 'name',
        disabled:true,
        width:80,
        value:'AND'
    });
    Wtf.apply(this.op,comboConfig);
    this.columnStore = new Wtf.data.Store({
        url:"../../reports.jsp",
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, Wtf.cc.reportColumnRec)
    });
    this.columnStore.load({
        params:{
            type :'Custom',
            m:"getReportsColumnAll"
        }
    });
    this.columnCombo = new Wtf.form.ComboBox({          //editor for column headers
        store : this.columnStore,
        displayField:'header',
        valueField : 'header',
        triggerAction: 'all',
        emptyText : WtfGlobal.getLocaleText('pm.customreport.filter.column.emptytext')
    });
    Wtf.apply(this.columnCombo,comboConfig);
    this.columnCombo.on("select",this.changeEditor,this);  // change editor of value column
    this.crtComboData = [
        ['1',WtfGlobal.getLocaleText('lang.text.equals'),'13'],
        ['2',WtfGlobal.getLocaleText('lang.lessthan.text'),'5'],
        ['3',WtfGlobal.getLocaleText('lang.lessthanequals.text'),'5'],   // 1 for numberfield 4 for date field 8 for text foeld (grpid)
        ['4',WtfGlobal.getLocaleText('lang.greaterthan.text'),'5'],
        ['5',WtfGlobal.getLocaleText('lang.greaterthaneq.text'),'5'],
        ['6',WtfGlobal.getLocaleText('lang.notequal.text'),'13'],
        ['7',WtfGlobal.getLocaleText('lang.contains.text'),'8'],
        ['8',WtfGlobal.getLocaleText('lang.notcontain.text'),'8']
    ];
    this.criteriaStore = new Wtf.data.SimpleStore({ // 1 - number 2-checkbox 4-date 8-string
        fields:['id','name','grpid'],
        data: this.crtComboData,
        autoLoad:true
    });
    
    this.criteriaCombo = new Wtf.form.ComboBox({    // editor combo for criteria
        store : this.criteriaStore,
        displayField:'name',
        valueField : 'id',
        emptyText : WtfGlobal.getLocaleText('pm.customreport.filter.condition.text'),
        firstExpand:false
    });
    Wtf.apply(this.criteriaCombo, comboConfig);
    
    this.cm = new Wtf.grid.ColumnModel([{
        dataIndex:'op',
        width:50
      
    },{
        header: WtfGlobal.getLocaleText('lang.header.text'),
        dataIndex:'header'
    },{
        header : WtfGlobal.getLocaleText('pm.admin.company.customcol.columntype'),
        dataIndex:'type',
        renderer : function(val){
            return Wtf.cc.getcolumntype(val);
        }
    },{
        header: WtfGlobal.getLocaleText('lang.condtion.text'),
        dataIndex:'criteria',
        renderer : function(val){
            var s = [WtfGlobal.getLocaleText('lang.text.equals'),WtfGlobal.getLocaleText('lang.lessthan.text'),WtfGlobal.getLocaleText('lang.lessthanequals.text'),WtfGlobal.getLocaleText('lang.greaterthan.text'),WtfGlobal.getLocaleText('lang.greaterthaneq.text'),'not equal','contain','not contain'];
            return s[val-1];
        }     
    },{
        header: WtfGlobal.getLocaleText('lang.value.text'),
        dataIndex:'value',
        renderer: function(val, cell, row, rowIndex, colIndex, ds){
            if(row.data.type==Wtf.cc.columntype.CHECKBOX){
                return val==1?"YES":"NO";
            }else if(row.data.type==Wtf.cc.columntype.DATE_FIELD){
                return val.format('Y-m-d');
            }else{
                return val;
            }
        }
    },{
        header: WtfGlobal.getLocaleText('lang.delete.text'),
        dataIndex:'header',
        renderer : function(val, cell, row, rowIndex, colIndex, ds) {
            return "<img src = '../../images/Delete.gif' wtf:qtip='Delete this filter' style='cursor: pointer;'>";
        }
    }
    ]);

    this.searchRecord = Wtf.data.Record.create([{
        name: 'header'
    },{
        name: 'criteria'
    },{
        name: 'type'
    },{
        name: 'value'
    },{
        name: 'op'
    }]);

    this.GridJsonReader = new Wtf.data.ArrayReader({
        id:0
    }, this.searchRecord);

    this.searchStore = new Wtf.data.Store({
        reader: this.GridJsonReader
    });
   
    this.toolbar =[WtfGlobal.getLocaleText('pm.customreport.filter.createfilter'),this.op,'-',this.columnCombo,'-',this.criteriaCombo,'-'];

    Wtf.advanceReportFilter.superclass.constructor.call(this, {
        hidden:true,
        store: this.searchStore,
        cm:this.cm,
        stripeRows: true,
        autoScroll : true,
        border:false,
        viewConfig: {
            forceFit:true
        },
        tbar: this.toolbar
    });
}

Wtf.extend(Wtf.advanceReportFilter, Wtf.grid.GridPanel, {
    initComponent: function(config){
        Wtf.advanceReportFilter.superclass.initComponent.call(this);
        this.crValue=8;  //group value for criteria
        this.on("cellclick", this.deleteFilter, this);
    },
    onRender:function(config){
        Wtf.advanceReportFilter.superclass.onRender.call(this,config);
        this.addToolbarItem(null);
        this.criteriaCombo.on("expand", function(cmb){
            if(!cmb.firstExpand){
                this.criteriaStore.filterBy(function(r){   // filter accordeing to group value for criteria
                    return ((r.data.grpid & this.crValue) == this.crValue);
                },this);
                cmb.firstExpand=true;
            }
        },this)
    },
    addNewRecord:function(){
        if(this.columnCombo.isValid() && this.criteriaCombo.isValid() && this.value.isValid()){
            var p = new this.store.recordType({
                header:this.columnCombo.getValue(),
                criteria:this.criteriaCombo.disabled?"":this.criteriaCombo.getValue(),
                value:this.value.getValue(),
                op:this.op.disabled?"":this.op.getValue(),
                type: this.columnStore.getAt(this.columnStore.find('header',this.columnCombo.getValue())).get("type")
            });
            this.searchStore.add(p);
            this.setInitState();
        }
    },
    getFilterData: function(){
        var jsonData = "[";
        if(this.searchStore.getCount()==0){
            return "";
        }
        this.searchStore.each(function(filterRecord){
            var crec = this.columnStore.getAt(this.columnStore.find('header',filterRecord.data.header));
            var filterVal = filterRecord.data.value;
            filterVal = this.getFilterValue(filterVal,crec.data.type);
            jsonData += "{ op :\" "+filterRecord.data.op+"\",tableName:\""+crec.data.tableName+"\",fieldName:\""+crec.data.fieldName+"\",condition:\""+this.getOperator(filterRecord.data.criteria)+"\",value:\""+filterVal+"\",type:\""+crec.data.type+"\"},";
        },this);
        jsonData = jsonData.substr(0, jsonData.length - 1) + "]";
        return jsonData;
    },
    getFilterValue: function(val,type){
        if(type == Wtf.cc.columntype.DATE_FIELD){
            return val.format('Y-m-d');
        }
        return val;
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
    getOperator:  function(v){
        var _s = ["=","<","<=",">",">=","<>","LIKE","NOT LIKE"];
        if(v=="")
            return "=";
        else
            return _s[v-1];    
    },
    deleteFilter:function(gd, ri, ci, e) {
        if(e.target.tagName == "img" || e.target.tagName == "IMG") {
            this.searchStore.remove(this.searchStore.getAt(ri));
            if(ri==0 && this.searchStore.getCount()>0){
                this.searchStore.getAt(0).set("op","");
            }
            this.op.setDisabled(this.searchStore.getCount()==0);
        }
    },
    comboStore: function(id){
        var ds = Wtf.cusColumnDS;
        var count = ds.getCount();
        for(var i=0;i<count;i++){
           if(id==ds.getAt(i).get('columnId')){
               break;
           } 
        }
        return getCcComboData(ds.getAt(i).get('masterdata'));
        
    },
    changeEditor :  function(combo,record,index){
        var change = false;
        var v 
        this.criteriaCombo.setDisabled(false);
        var cfg ={
            allowBlank: false,
            width:150
        }
        if (record.data.type == Wtf.cc.columntype.NUMBER_FIELD && this.value.getXType() != 'numberfield' ) {
            // Create and replace with a new editor in the target column
            change = true;
            cfg["allowDecimals"] = true;
            v = new Wtf.form.NumberField(cfg);
            this.crValue = 1;
        } else if (record.data.type == Wtf.cc.columntype.TEXT_FIELD &&  this.value.getXType() != 'textfield') {
            change = true;
            cfg["allowNegative"]=false;
            cfg["allowDecimals"]=true;
            v = new Wtf.form.TextField(cfg);
            this.crValue = 8;
        } else if (record.data.type == Wtf.cc.columntype.TEXT_AREA &&  this.value.getXType() != 'textarea') {
            change = true;
            cfg["allowNegative"]=false;
            cfg["allowDecimals"]=true;
            v = new Wtf.form.TextField(cfg);
            this.crValue = 8;
        } else if (record.data.type == Wtf.cc.columntype.DATE_FIELD && this.value.getXType() != 'datefield') {
            change = true;
            v = new Wtf.form.DateField(cfg);
            this.crValue = 4;
        } else if(record.data.type == Wtf.cc.columntype.CHECKBOX && this.value.getXType() != 'checkbox'){
            change = true;
            this.criteriaCombo.setDisabled(true);
            v = new Wtf.form.ComboBox({
                store : new Wtf.data.SimpleStore({
                    fields : ["id","val"],
                    data:[[1,WtfGlobal.getLocaleText("lang.yes.text")],[0,WtfGlobal.getLocaleText("lang.no.text")]],
                    autoLoad:true
                }),
                displayField:'val',
                valueField : 'id',
                allowBlank:false
            });
            Wtf.apply(v, comboConfig);
            this.crValue = 4;
        }else if(record.data.type == Wtf.cc.columntype.DROD_DOWN && this.value.getXType() != 'combobox'){
            change = true;
            this.criteriaCombo.setDisabled(true);
            v = new Wtf.form.ComboBox({
                store : new Wtf.data.SimpleStore({
                    fields : ["val"],
                    data:this.comboStore(record.data.columnID),
                    autoLoad:true
                }),
                displayField:'val',
                valueField : 'val',
                allowBlank:false
            });
            Wtf.apply(v, comboConfig);
            this.crValue = 4;
        }
        this.criteriaStore.filterBy(function(r){   // filter accordeing to group value for criteria
            return ((r.data.grpid & this.crValue) == this.crValue);
        },this);

        if(change){
            this.addToolbarItem(v);
            this.doLayout();
        }
    },
    addToolbarItem : function(v){ // value fields item (textitem/datefield/numberfield)
        if(v==null){
            this.value = new Wtf.form.TextField({
                allowBlank:false,
                maskRe:Wtf.validateCC
            })
        }else{
            this.add.destroy();
            this.cancel.destroy();
            this.close.destroy();
            this.value.destroy();
            this.value=v;
            this.value.setDisabled(false);
        }
        this.add = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText('lang.add.text'),
            scope:this,
            border:true,
            iconCls:'pwnd addfiltericon',
            handler: this.addNewRecord,
            tooltip: {
                text:WtfGlobal.getLocaleText('pm.project.filter.add')
            }
        });
        this.cancel = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText('lang.clear.text'),
            tooltip: {
                text: WtfGlobal.getLocaleText('pm.filter.clear.text')
            },
            handler: this.clearFilters,//this.cancelSearch,
            scope:this,
            iconCls:'pwnd clearfiltericon'
        });
        this.close = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText('lang.resetclose.text'),
            tooltip: {
                text: WtfGlobal.getLocaleText('pm.filter.grid.close')
            },
            handler: this.closeFilter,//this.cancelSearch,
            scope:this,
            iconCls:'dpwnd closefiltericon'
        });
        this.getTopToolbar().add(this.value);
        this.getTopToolbar().add(this.add);
        this.getTopToolbar().add(this.cancel);
        this.getTopToolbar().add(this.close);
    },
    clearFilters: function(){
        this.searchStore. removeAll();
        this.setInitState();
    },
    closeFilter: function(){
        this.showFilter();
    },
    setInitState : function(){ // v = 0 for init component ant 1 for later stage to set defaults
        this.columnCombo.clearValue();
        this.criteriaCombo.clearValue();
        //this.value.setDisabled(true);
        this.op.setValue("AND");
        this.op.setDisabled(this.searchStore.getCount()==0);
        this.criteriaCombo.setDisabled(true);     
    }
});
