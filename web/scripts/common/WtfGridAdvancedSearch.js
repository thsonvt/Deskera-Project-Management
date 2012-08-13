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

Wtf.advancedSearchComponent = function(config){
    Wtf.apply(this, config);

    this.events = {
        "filterStore": true,
        "clearStoreFilter": true
    };

    this.combovalArr=[];
    this.xtypeArr=[];

    this.combostore = new Wtf.data.SimpleStore({
        fields: [{
            name: 'header'
        },{
            name: 'name'
        },{
            name: 'xtype'
        },{
            name: 'xtypestore'
        }]
    });

    this.columnCombo = new Wtf.form.ComboBox({
        store : this.combostore,
        typeAhead : true,
        displayField:'header',
        valueField : 'name',
        triggerAction: 'all',
        emptyText : WtfGlobal.getLocaleText('pm.search.selectfield'),
        mode:'local',
        editable:false
    })

    this.columnCombo.on("select", this.displayField, this);

    this.cm = new Wtf.grid.ColumnModel([{
        header: WtfGlobal.getLocaleText('pm.search.field.text'),
        dataIndex:'column'
    },{
        header: "Search1 Text",
        dataIndex:'searchText',
        hidden:true
    },{
        header: WtfGlobal.getLocaleText('pm.search.searchtext'),
        dataIndex:'id'
    },{
        header: WtfGlobal.getLocaleText('lang.delete.text'),
        dataIndex:'delField',
        renderer : function(val, cell, row, rowIndex, colIndex, ds) {
            return "<img src = '../../images/Delete.gif' wtf:qtip="+WtfGlobal.getLocaleText('pm.advancedsearch.delete')+" style='cursor: pointer;'>";
        }
    }
    ]);

    this.searchRecord = Wtf.data.Record.create([{
        name: 'column'
    },{
        name: 'searchText'
    },{
        name: 'tableColumn'
    },{
        name: 'id'
    },{
        name: 'xtype'
    }]);

    this.GridJsonReader = new Wtf.data.JsonReader({
        root: "data",
        totalProperty: 'count'
    }, this.searchRecord);

    this.searchStore = new Wtf.data.Store({
        reader: this.GridJsonReader
    });

    this.on("cellclick", this.deleteFilter, this);

    Wtf.advancedSearchComponent.superclass.constructor.call(this, {
        region :'north',
        height:150,
        hidden:true,
        store: this.searchStore,
        cm:this.cm,
        stripeRows: true,
        autoScroll : true,
        border:false,
        clicksToEdit:1,
        viewConfig: {
            forceFit:true
        },
        tbar: [
        this.text1=new Wtf.Toolbar.TextItem(WtfGlobal.getLocaleText('pm.search.field.text')+':'),
        this.columnCombo,
        '-',
        this.text=new Wtf.Toolbar.TextItem(WtfGlobal.getLocaleText('pm.search.searchtext')+':'),
        this.searchText = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText('pm.common.masterecord.new'),
            anchor: '95%',
            maxLength: 100,
            width:125
        }),this.add = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText('lang.add.text'),
            tooltip: {
                text: WtfGlobal.getLocaleText('pm.search.addterm')
            },
            handler: this.addSearchFilter,
            scope: this,
            iconCls : 'pwnd addfiltericon'
        }),
        this.search = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText('lang.search.text'),
            tooltip: {
                text: WtfGlobal.getLocaleText('pm.search.preform')
            },
            handler: this.doSearch,
            scope:this,
            disabled:true,
            iconCls : 'pwnd searchtabpane'
        }),
        this.cancel = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText('pm.search.clear.text'),
            tooltip: {
                text: WtfGlobal.getLocaleText('pm.search.advance,text')
            },
            handler: this.cancelSearch,
            scope:this,
            iconCls:'pwnd clearfiltericon'
        })]
    });

}

Wtf.extend(Wtf.advancedSearchComponent, Wtf.grid.EditorGridPanel, {
    addSearchFilter:function(){
        if(this.columnCombo.getRawValue().trim()==""){
            msgBoxShow(201, 0);
            return;
        }
        var column =this.columnCombo.getValue();
        var searchText="";
        if(this.searchText.getXType()=="numberfield" || this.searchText.getXType()=="datefield" || this.searchText.getXType()=="combo"){
            searchText = this.searchText.getValue();
            searchText = (typeof searchText == 'number') ? searchText+'' : searchText;
        } else {
            searchText = this.searchText.getValue().trim();
        }
        var do1=0;
        if (column != "" && searchText != ""){
            this.searchText1 = this.searchText.getRawValue();
            this.combovalArr.push(this.searchText1);
            if(this.searchText.getXType()=="datefield"){
                this.xtypeArr.push("datefield");
            } else{
                this.xtypeArr.push(this.searchText.store);
            }
            this.columnText="";
            if(searchText != "") {
                for(var i=0;i<this.combostore.getCount();i++) {
                    if(this.combostore.getAt(i).get("name") == column) {
                        this.columnText = this.combostore.getAt(i).get("header");
                        do1=1;
                    }
                }
                if(do1==1) {
                    this.search.enable();
                    this.search.tooltip.text = 'Search on multiple terms';
                    var searchRecord = new this.searchRecord({
                        column: this.columnText,
                        searchText: searchText,
                        tableColumn: column,
                        id: this.searchText1,
                        xtype: this.searchText.getXType()
                    });

                    var index=this.searchStore.find('column',this.columnText);
                    if (index == -1 ) {
                        this.searchStore.add(searchRecord);
                    } else {
                        this.searchStore.remove(this.searchStore.getAt(index ) );
                        this.searchStore.insert(index,searchRecord);
                    }
                }
            }
        } else {
            if(column == "") {
                msgBoxShow(201, 0);
            } else if(searchText =="") {
                msgBoxShow(202, 0);
            }
        }
        if(this.searchText.getXType()!='combo')
                this.searchText.reset();
    },

    doSearch:function(){
        if (this.searchStore.getCount() > 0){
            var filterJson='{"root":[';
            var i=0;

             this.searchStore.each(function(filterRecord){
                var combosearch ='';
                //  for combo case also searchText is combodataid
                var searchText=filterRecord.data.searchText+"";
                var value="";
                var xType = "";
                value=filterRecord.data.searchText+"";
                var xtype=filterRecord.data.xtype;
                if (xtype == 'datefield' || xtype =='Date'){
                    searchText = WtfGlobal.convertToOnlyDate(filterRecord.data.searchText);
                }
                searchText = searchText.replace(/"/g,"");
                //this.combovalArr[i] = this.combovalArr[i].replace(/"/g,"");
                combosearch = filterRecord.data.id.replace(/"/g,"");
                value =  value.replace(/"/g,"");
                if(xtype=="textfield")
                     xtype="";
                // object is push in xtypeArr for Combo field else it is datefield. No value is push for numberfield and textfield.
                //if(this.xtypeArr[i]!=undefined){
                    //if(this.xtypeArr[i]=="datefield"){
                        //xType ='datefield'
                    //}else if(typeof(this.xtypeArr[i])=="object"){
                        //xType ='combo'
                //}
                //}else {
                    //xType ="";
                //}
                filterJson+='{"column":"'+filterRecord.data.tableColumn+'","searchText":"'+searchText+'","columnheader":"'+encodeURIComponent(filterRecord.data.column)+'","search":"'+value+'","xtype":"'+xtype+'","combosearch":"'+combosearch+'"},';
                i++;
            },this);

            filterJson=filterJson.substring(0,filterJson.length-1);
            filterJson+="]}";
            this.fireEvent("filterStore",filterJson);

        } else {
            msgBoxShow(203, 0);
            this.fireEvent("filterStore","");
        }
    },
    cancelSearch:function(){
        this.columnCombo.setValue("");
        var searchXtype = this.searchText.getXType();
        if(searchXtype=='combo')
            this.columnCombo.fireEvent("select", undefined, '');
        this.searchStore.removeAll();
        this.combovalArr=[];
        this.xtypeArr=[];
        this.fireEvent("clearStoreFilter");
    },

    deleteFilter:function(gd, ri, ci, e) {
        var event = e;
        if(event.target.tagName == "img" || event.target.tagName == "IMG") {
            this.searchStore.remove(this.searchStore.getAt(ri));
            if(this.searchStore.getCount()==0) {
                this.search.disable();
                this.search.tooltip.text = 'Add terms to search.';
            }
            this.combovalArr.splice(ri,1);
            this.xtypeArr.splice(ri,1);
        }
    },

    displayField:function(combo,record){
        if(record == '')
            var recXtype = "textfield";
        else
            recXtype=record.get('xtype');
        if (recXtype == "None"){
            record.set('xtype','textfield');
        }
        if (this.text){
            this.text.destroy();
        }
        this.searchText.destroy();
        this.add.destroy();
        this.search.destroy();
        this.cancel.destroy();
        this.doLayout();
        if (recXtype == "textfield" || recXtype == 'Text' || recXtype =='textarea'){
            this.searchText = new Wtf.form.TextField({
                anchor: '95%',
                maxLength: 100,
                width:125
            });
        }

        if (recXtype == "numberfield" || recXtype == 'Number(Integer)' || recXtype == 'Number(Float)'){
            this.searchText = new Wtf.form.NumberField({
                anchor: '95%',
                maxLength: 100,
                width:125
            });
        }
        if (recXtype == "combo" || recXtype == "Combobox" || recXtype == "select" ){
            this.comboStore = record.get('xtypestore')

            this.displayField = combo.getValue();

            this.searchText = new Wtf.form.ComboBox({
                valueField: 'id',
                displayField: 'name',
                store: this.comboStore,
                typeAhead:true,
                forceSelection :true,
                anchor: '95%',
                mode: 'local',
                triggerAction: 'all',
                selectOnFocus: true,
                emptyText: WtfGlobal.getLocaleText('pm.common.option.select'),
                width:125,
                editable:false
            });
        }

        if (recXtype == "datefield" || recXtype == 'Date' ){
            this.searchText=new Wtf.form.DateField({
                width:125,
                format:WtfGlobal.getOnlyDateFormat(),
                readOnly: true
            });
        }

        this.text = new Wtf.Toolbar.TextItem(WtfGlobal.getLocaleText('pm.search.searchtext')+':');
        this.getTopToolbar().add(this.text);
        this.getTopToolbar().add(this.searchText);
        this.getTopToolbar().addButton([this.add = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText('lang.add.text'),
            tooltip: {
                text: WtfGlobal.getLocaleText('pm.search.addterm')
            },
            handler: this.addSearchFilter,
            scope: this,
            iconCls : 'pwnd addfiltericon'
        }),
        this.search = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText('lang.search.text'),
            tooltip: {
                text: WtfGlobal.getLocaleText('pm.search.preform')
            },
            handler: this.doSearch,
            disabled:true,
            scope:this,
            iconCls : 'pwnd searchtabpane'
        }),
        this.cancel = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText('pm.search.clear.text'),
            tooltip: {
                text: WtfGlobal.getLocaleText('pm.search.advance,text')
            },
            handler: this.cancelSearch,
            scope:this,
            iconCls:'pwnd clearfiltericon'
        })]);
        this.add.getEl().dom.style.paddingLeft="4px";
        this.doLayout();
    },

    getComboData: function(){
        if(!this.myData){
            var mainArray = [];
            this.comboStoreArray = [];
            var cmArray = this.cm.config;
            for (var i = 0; i < cmArray.length; i++) {
                var tmpArray = [];
                if(cmArray[i].tableColumn && (cmArray[i].hidden == undefined || cmArray[i].hidden == false)) {
                    var header = cmArray[i].header;
                    tmpArray.push(header);
                    tmpArray.push(cmArray[i].tableColumn);
                    tmpArray.push(cmArray[i].xtype);
                    tmpArray.push(cmArray[i].xtypeStore);
                    mainArray.push(tmpArray)
                }
            }
            this.myData = mainArray;
            if(this.advSearch)
                this.combostore.loadData(this.myData);
        }
    }

});


