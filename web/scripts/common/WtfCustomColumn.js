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

// master value for Module
/*
 *'project','Project'
 *'contact','Contact'
 */

/**
 * window for  add, delete and edit master record for dropdown type for custom column
 **/

Wtf.CCMasterData = function(config){
    Wtf.apply(this, config);
    var masterRecord = new Wtf.data.Record.create([
    {
        name: 'columnId'
    },
    {
        name: 'masterdata'
    }
    ]);

    this.masterGrid = new Wtf.grid.EditorGridPanel({
        id: 'mastergrid' + this.id,
        store: this.masterds,
        sm:this.mastersm,
        cm: this.mastercm,
        border: false,
        clicksToEdit: 1,
        viewConfig: {
            forceFit: true
        }
    });
    Wtf.CCForm.superclass.constructor.call(this, {});
    this.masterWin = new Wtf.Window({
        id: 'master' + this.id,
        title:WtfGlobal.getLocaleText('pm.common.option.configuredd'),
        layout:'fit',
        iconCls: 'iconwin',
        modal: true,
        height:300,
        width:260,
        scope: this,
        closable:false, 
        tbar: [
        this.masterText = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText('pm.common.dd.new'),
            anchor: '95%',
            maxLength: 60,
            id: this.id + 'masterText',
            emptyText:WtfGlobal.getLocaleText('pm.common.provideoption'),
            maskRe: Wtf.validateCC,
            allowBlank: false
        }),
        '-',{
            text: WtfGlobal.getLocaleText('lang.add.text'),
            tooltip: {
                title: WtfGlobal.getLocaleText('lang.add.text'),
                text: WtfGlobal.getLocaleText('pm.common.option.addnew')
            },
            handler: function() {
                if(this.masterText.getValue().trim() != ""){
                    this.masterds.add( new masterRecord({
                        columnId:this.mode==1?this.data.columnId:'-1',
                        masterdata:this.masterText.getValue()
                    }))
                    this.masterText.setValue("");
                    this.masterGrid.getView().refresh();
                } else {
                    this.masterText.markInvalid("This field cannot be blank.");
                }
            },
            scope: this
        },'-',
        this.delmaster = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText('lang.delete.text'),
            disabled:true,
            tooltip: {
                title: WtfGlobal.getLocaleText('lang.delete.text'),
                text: WtfGlobal.getLocaleText('pm.common.delete.text')
            },
            handler: function() {
                this.masterds.remove(this.mastersm.getSelected());
            },
            scope:this
        })],
        items:[this.masterGrid],
        buttons:[
        {
            text:WtfGlobal.getLocaleText('lang.done.text'),
            handler:function() {
                Wtf.getCmp('master' + this.id).hide();
            },
            scope:this
        }/*,{
            text:WtfGlobal.getLocaleText('lang.cancel.text'),
            handler: function(){
                Wtf.getCmp('master' + this.id).close();
            },
            scope:this
        }*/]
    })
    this.mastersm.on("selectionchange",this.handleBttns,this);
}
Wtf.extend(Wtf.CCMasterData, Wtf.Panel,{
    handleBttns: function(obj){
        this.delmaster.disable();
        if(obj.getCount() > 0){
            this.delmaster.enable();
        }else{
            this.delmaster.disable();
        }
    }

});

/*-------------default value window *********************/

Wtf.cc.DefaultValue = function(config){
    Wtf.apply(this, config);
    var fieldConfig ={
        selectOnFocus:true,
        labelSeparator :'',
        hideLabel:true,
        maxLength: 90,
        width: 273
    };
    if(this.type==Wtf.cc.columntype.NUMBER_FIELD){
        this.defValuebox = new Wtf.form.NumberField(fieldConfig);
    }
    else if(this.type==Wtf.cc.columntype.CHECKBOX){
        this.defValuebox = new Wtf.form.Checkbox(fieldConfig);
    }else if(this.type==Wtf.cc.columntype.DATE_FIELD){
        this.defValuebox = new Wtf.form.DateField(fieldConfig);
    }else {
        this.defValuebox = new Wtf.form.TextField(fieldConfig);
    }
    this.save = document.createElement('img');
    this.save.src = '../../images/check16.png';
    this.save.title = WtfGlobal.getLocaleText('pm.customcolumn.savedefault.title');
    this.save.className = 'addtagbutton';
    this.cancel = document.createElement('img');
    this.cancel.src = '../../images/Stop.png';
    this.cancel.title = WtfGlobal.getLocaleText('lang.cancel.text');
    this.cancel.className = 'addtagbutton';
    this.modal=true;
    Wtf.cc.DefaultValue.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.cc.DefaultValue, Wtf.Window, {
    save2tag: function(){
        var value = WtfGlobal.HTMLStripper(this.defValuebox.getValue()) ;
        if(this.type==Wtf.cc.columntype.CHECKBOX){
            value = 'false'
        }else if(this.type==Wtf.cc.columntype.DATE_FIELD){
            if(value.trim()!="")
                value = Date.parse(value);
        }
        this.fireEvent('savetags', value);
    },
    cancel2tag: function(){
        this.fireEvent('canceltags');
        this.close();
    },
    setTagText: function(text){
        this.defValuebox.setValue(text);
    },
    onRender: function(config){
        Wtf.cc.DefaultValue.superclass.onRender.call(this, config);
        this.add({
            cls: "tagwindow",
            closable: false,
            resizable: false,
            autoHeight: true,
            width: 276,
            border: false,
            layout:'form',
            items: [this.defValuebox, {
                bodyStyle: 'margin:5px 0 5px 228px; border:none; float:right;',
                layout: "column",
                items: [{
                    baseCls: 'savebutton',
                    contentEl: this.save
                }, {
                    baseCls: 'savebutton',
                    contentEl: this.cancel
                }]
            }]
        });
        Wtf.get(this.save).addListener("click", this.save2tag, this);
        Wtf.get(this.cancel).addListener("click", this.cancel2tag, this);
    }  
});
/*************end of default value window***************/

/**
 * This component is used for add, edit custom column meta data
 *
 * */
Wtf.CCForm = function(config){
    Wtf.apply(this, config);
    var masterRecord = new Wtf.data.Record.create([
    {
        name: 'columnId'
    },
    {
        name: 'masterdata'
    }
    ]);
    this.masterReader = new Wtf.data.ArrayReader({
        id:'columnId'
    }, this.masterRecord);
    this.mastersm = new Wtf.grid.CheckboxSelectionModel();
    this.mastercm = new Wtf.grid.ColumnModel([
        new Wtf.grid.RowNumberer(),
        {
            header: WtfGlobal.getLocaleText('pm.common.masterrecord'),
            dataIndex: 'masterdata',
            editor: new Wtf.form.TextField({
                allowBlank: false,
                maxLength: 100
            })
        }]);

    this.masterds = new Wtf.data.Store({
        reader: this.masterReader
    });
    if(this.mode==1 && this.data.type=='3'){
        for(var i=0; i< this.data.masterdata.length; i++){
            this.masterds.add(new masterRecord({
                columnId:this.data.columnId,
                masterdata:this.data.masterdata[i]
            }))
        }
    }
    this.typeStore = new Wtf.data.SimpleStore({
        fields :['id', 'name'],
        data:[
        [Wtf.cc.columntype.TEXT_FIELD,'Text Field'],
        [Wtf.cc.columntype.NUMBER_FIELD,'Number Field'],
        [Wtf.cc.columntype.CHECKBOX,'Checkbox'],
        [Wtf.cc.columntype.DATE_FIELD,'Date Field'],
        [Wtf.cc.columntype.DROD_DOWN,'Dropdown'],
        //[Wtf.cc.columntype.RICH_TEXTBOX,'Rich TextBox'],
        [Wtf.cc.columntype.TEXT_AREA,'Text Area']//,
        //[Wtf.cc.columntype.MULTISELECT_COMBO,'MultiSelect Combobox'],
        //[Wtf.cc.columntype.FILE,'File Upload']
        ]
    });

    this.moduleStore = new Wtf.data.SimpleStore({
        fields :['id', 'name'],
        data:[['project','Project'],['contact','Contact']]
    });
    this.text = WtfGlobal.getLocaleText('lang.add.text');
    if(this.mode == 1) {
        this.text = WtfGlobal.getLocaleText('lang.edit.text');
    }
    var btnArr = [];
    this.OKBtn = new Wtf.Button({
        text : this.text,
        scope : this,
        id: 'createCCBtn',
        cls: 'adminButton',
        handler: this.OKHandler
    });
    btnArr.push(this.OKBtn);
    this.cancelBtn = new Wtf.Button({
        text : WtfGlobal.getLocaleText('lang.cancel.text'),
        cls: 'adminButton',
        id: "cancelCCBtn",
        scope : this
    });
    btnArr.push(this.cancelBtn);
    Wtf.CCForm.superclass.constructor.call(this, {
        title : (this.mode==1)?WtfGlobal.getLocaleText('pm.customcolumn.edit'):WtfGlobal.getLocaleText('pm.customcolumn.create'),
        closable : true,
        modal : true,
        iconCls : 'iconwin',
        width : 430,
        height: 380,
        resizable :true,
        buttonAlign : 'right',
        buttons :btnArr,
        layout : 'border',
        id: 'createCCWin',
        autoDestroy: true
    });
}

Wtf.extend(Wtf.CCForm, Wtf.Window,{

    initComponent: function(){
        Wtf.CCForm.superclass.initComponent.call(this);
        this.addForm = new Wtf.form.FormPanel({
            url: "../../customColumn.jsp?m="+(this.mode==0?"addColumn":"editColumn"),
            region: "center",
            bodyStyle: "padding: 10px;",
            border: false,
            labelWidth: 140,
            height: 100,
            buttonAlign: 'right',
            items:[this.colheader = new Wtf.form.TextField({
                fieldLabel: WtfGlobal.getLocaleText('pm.admin.company.customcol.columnheader')+'*',
                scope: this,
                anchor: '95%',
                allowBlank: false,
                name: 'header',
                id:'colheader'+this.id,
                value:this.mode==0 ? "":this.data.header,
                maxLength: 50,
                maskRe: Wtf.validateCC
            }),
            this.colType = new Wtf.form.ComboBox({
                valueField: 'id',
                displayField: 'name',
                store: this.typeStore,
                fieldLabel: WtfGlobal.getLocaleText('pm.admin.company.customcol.columntype')+'*',
                editable: true,
                typeAhead: true,
                forceSelection: true,
                value:this.mode==0 ? null:this.data.type,
                name: 'type',
                id:'type'+this.id,
                allowBlank: false,
                anchor: '95%',
                disabled:this.mode==1?true:false,
                mode: 'local',
                triggerAction: 'all',
                selectOnFocus: true,
                emptyText: WtfGlobal.getLocaleText('pm.admin.company.customcol.selectacolumntype')
            }),this.customField = new Wtf.Panel({
                bodyStyle : 'margin-bottom:4px;padding-right:20px;text-align:right',
                id:'customfieldpanel'+this.id,
                border:false,
                hidden:this.mode==1?(this.data.type==Wtf.cc.columntype.DROD_DOWN?false:true):true,
                html:"<a id = 'addColumnlink"+this.id+"'class='attachmentlink' href=\"#\" onclick=\"addMasterRecord(\'"+this.id+"\')\">"+(this.mode==1?WtfGlobal.getLocaleText('lang.edit.text'):WtfGlobal.getLocaleText('lang.add.text'))+" "+ WtfGlobal.getLocaleText('pm.common.options.dropdown') +"</a>"
            }),
            this.moduleType = new Wtf.form.ComboBox({
                valueField: 'id',
                displayField: 'name',
                store: this.moduleStore,
                fieldLabel: WtfGlobal.getLocaleText('pm.admin.company.customcol.modulename')+'*',
                editable: true,
                typeAhead: true,
                forceSelection: true,
                value:'Project',   // his.mode==0 ? "":this.data.module,  fixed for project module
                name: 'module',
                id:'module'+this.id,
                allowBlank: false,
                anchor: '95%',
                mode: 'local',
                triggerAction: 'all',
                selectOnFocus: true,
                emptyText: WtfGlobal.getLocaleText('pm.common.selectmodule'),
                disabled:true

            }),this.nullchk = new Wtf.form.Checkbox({
                fieldLabel: WtfGlobal.getLocaleText('pm.admin.company.customcol.ismandatory'),
                name:'isMandatory',
                id:'isMandatory'+this.id,
                checked: this.mode==0 ? false :this.data.mandatory,
                disabled:true
            }), /* this.numField = new Wtf.form.NumberField({
                fieldLabel: WtfGlobal.getLocaleText('pm.common.maxlength'),
                scope: this,
                allowBlank: false,
                name: 'question',
                maxLength: 256,
                allowNegative:false,
                allowDecimal:false
            }),*/ this.enable = new Wtf.form.Checkbox({
                fieldLabel: WtfGlobal.getLocaleText('pm.admin.company.customcol.enabled'),
                name:'enabled',
                checked: this.mode==0 ? true : this.data.enabled
            }),this.visible = new Wtf.form.Checkbox({
                fieldLabel: WtfGlobal.getLocaleText('pm.admin.company.customcol.visible'),
                name : 'visible',
                checked: this.mode==0 ? true : this.data.visible
            }),{
                xtype:'hidden',
                name:'columnId',
                value:this.mode==1?this.data.columnId:""
            }
            ]
        });

        Wtf.getCmp('colheader'+this.id).on("change", function(){
            Wtf.getCmp('colheader'+this.id).setValue(WtfGlobal.HTMLStripper(Wtf.getCmp('colheader'+this.id).getValue()));
        }, this);
        Wtf.getCmp('type'+this.id).on("select", function(cmp){
            var value = cmp.getValue();
            this.nullchk.setValue(false);
            if(value==Wtf.cc.columntype.DROD_DOWN)
                this.customField.show();
            else
                this.customField.hide();

            if(value==Wtf.cc.columntype.CHECKBOX){
                this.nullchk.disable();
            }else{
                this.nullchk.enable();
            }
            this.doLayout();
        }, this);
        this.colheader.on("change",function(){
            var _s = Wtf.cusColumnDS;
            var cccount = _s.getCount();
            var header = this.colheader.getValue();
            for(var i= 0;i<cccount;i++){
                if(_s.getAt(i).get('header').toLowerCase()==header.toLowerCase()){
                    if(this.mode==1){
                        if(_s.getAt(i).get('header').toLowerCase()!=this.data.header.toLowerCase()){
                            msgBoxShow([WtfGlobal.getLocaleText('lang.error.text'), WtfGlobal.getLocaleText({key:'pm.msg.duplicateheader',params:[header]})], 1);
                            this.colheader.markInvalid();
                            this.colheader.setValue("");
                            return;
                        }
                    }else{
                        msgBoxShow([WtfGlobal.getLocaleText('lang.error.text'), WtfGlobal.getLocaleText({key:'pm.msg.duplicateheader',params:[header]})], 1);
                        this.colheader.markInvalid();
                        this.colheader.setValue("");
                        return;
                    }
                }
            }
        },this);
        var icon = (this.mode == 1) ? "../../images/Edit-Custom-Columns.jpg" : "../../images/Add-Custom-Columns.jpg";
        var topHTML = getTopHtml(this.text + " "+WtfGlobal.getLocaleText('pm.admin.company.customcol.title1'), this.text + " "+WtfGlobal.getLocaleText('pm.admin.company.customcol.title1')+"<br><span style=\"float:right; margin-right:0px;\">"+WtfGlobal.getLocaleText('pm.common.requiredfields')+"</span>", icon);
        this.add({
            region : 'north',
            height : 80,
            border : false,
            bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
            html: topHTML
        },{
            region : 'center',
            border : false,
            bodyStyle : 'background:#f1f1f1;font-size:10px;',
            layout : 'fit',
            items :this.addForm
        });
        this.doLayout();
        focusOn('colheader'+this.id);
    },
    onRender: function(config){
        Wtf.CCForm.superclass.onRender.call(this,config);
        this.nullchk.on("check",function(cmp){
            if(cmp.getValue() && !cmp.disabled)
                this.showDefaultValueWindow();
        }, this);
    },
    showDefaultValueWindow: function(){
        if (!this.defValWin) {
            this.defValWin = new Wtf.cc.DefaultValue({
                cls: "tagwindow",
                border: false,
                resizable: false,
                closable: false,
                title:WtfGlobal.getLocaleText('pm.admin.company.customcol.defaultvalue'),
                type:this.colType.getValue()
            });
            this.defValWin.on('savetags', function(tagstr){
                if(!this.defValWin.defValuebox.isValid()){
                    return;
                }
                this.defaultValue=tagstr;
                this.defValWin.close();
            }, this);
            this.defValWin.on('close', function(){
                this.defValWin = null;
                
            }, this);
            this.defValWin.on('canceltags', function(tagstr){
                this.nullchk.setValue(false);
            }, this);
            var xy = Wtf.get('isMandatory'+this.id).getXY();
            this.defValWin.setPagePosition(xy);
            this.defValWin.show();
            focusOn(this.defValWin.defValuebox.id);
        }else {
            this.defValWin.close();
        }
    },

    OKHandler: function(btn){
        var count = this.masterds.getCount();
        var dv = this.nullchk.getValue()?this.defaultValue:"";
        if(this.colType.getValue()==Wtf.cc.columntype.DROD_DOWN && count==0 && dv==""){
            msgBoxShow(228, 2);
            this.colType.markInvalid();
            return;
        }
        if(this.nullchk.getValue() || this.nullchk.disabled){
            this.formsubmit(btn);
        }else{
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.321'),function(b){
                if(b=='yes'){
                    this.formsubmit(btn);
                }
            },this);
        }
    },
    formsubmit : function(btn){

        var masterdata="";
        var count = this.masterds.getCount();
        for(var i=0; i<count; i++){
            masterdata+=this.masterds.getAt(i).get('masterdata')+";";
        }
        if(this.colType.getValue()==Wtf.cc.columntype.DROD_DOWN){
            masterdata += this.nullchk.getValue()?this.defaultValue+";":" "; //keep one space to trim in next function
        }
        if(masterdata.length>0){
            masterdata = masterdata.substring(0,masterdata.length-1);
        }

        if(this.addForm.form.isValid()){
            btn.disable();
            this.cancelBtn.disable();
            this.addForm.form.submit({
                waitMsg: WtfGlobal.getLocaleText("pm.loading.text")+'...',
                params:{
                    masterdata:masterdata,
                    typeid:this.colType.getValue(),
                    defaultValue:this.nullchk.getValue()?this.defaultValue:"",
                    module: 'Project'
                },
                scope : this,
                useraction: this.mode,
                failure: function(frm, action){
                    this.failureHandler(btn, frm, action);
                },
                success: function(frm, action){
                    this.successHandler(btn, frm, action);
                    collectProjectData();
                }
            });
        }
    },
    createMasterRecord : function(){
        this.masterGrid = new Wtf.CCMasterData({
            columnid:this.data.columnId,
            data:this.data.masterdata,
            masterds:this.masterds,
            mastercm:this.mastercm,
            mastersm:this.mastersm
        })
        var xy = Wtf.get('customfieldpanel'+this.id).getXY();
        this.masterGrid.masterWin.setPagePosition(xy);
        this.masterGrid.masterWin.show();
    }
});
function addMasterRecord(id){
    Wtf.getCmp(id).createMasterRecord();
}


/** window for display details of custom column**/
Wtf.CCGrid = function(config){
    Wtf.apply(this, config);

    this.cusColumnCM= new Wtf.grid.ColumnModel([
        new Wtf.grid.RowNumberer(),
        {
            header:WtfGlobal.getLocaleText('pm.admin.company.customcol.columnheader'),
            dataIndex: 'header',
            autoWidth:true
        },{
            header: WtfGlobal.getLocaleText('pm.admin.company.customcol.columntype'),
            dataIndex: 'type',
            width : 90,
            renderer: function(val){
                return Wtf.cc.getcolumntype(val);
            }
        },{
            header: WtfGlobal.getLocaleText('pm.admin.company.customcol.modulename'),
            dataIndex: 'module',
            width : 90
        },{
            header : WtfGlobal.getLocaleText('lang.view.text'),
            dataIndex: 'columnId',
            width:30,
            renderer:function(value, css, record, row, column, store){
                return "<div class='viewcc' wtf:qtip="+WtfGlobal.getLocaleText('pm.customcolumn.viewdetails.tip')+">&nbsp;</div>";
            }
        },{
            header : WtfGlobal.getLocaleText('lang.edit.text'),
            dataIndex: 'colid',
            width:30,
            renderer:function(value, css, record, row, column, store){
                return "<div class='editcc' wtf:qtip="+WtfGlobal.getLocaleText('pm.customcolumn.edit.qtip')+">&nbsp;</div>";
            }
        },{
            header : WtfGlobal.getLocaleText('lang.delete.text'),
            dataIndex: 'colid',
            width:35,
            renderer:function(value, css, record, row, column, store){
                return "<div class='deletecc' wtf:qtip="+WtfGlobal.getLocaleText('pm.customcolumn.delete.tip')+">&nbsp;</div>";
            }
        }
        ]);
    Wtf.CCGrid.superclass.constructor.call(this,{
        ds: Wtf.cusColumnDS,
        cm: this.cusColumnCM,
        sm: this.cusColumnSM = new Wtf.grid.RowSelectionModel({
            singleSelect:true
        }),
        autoDestroy: true,
        border:false,
        bodyBorder:false,
        frame:false,
        hideBorders:true,
        viewConfig: {
            forceFit: true,
            autoFill: true,
            emptyText: '<div class="emptyGridText" style="margin: 0px;">'+WtfGlobal.getLocaleText('pm.customcolumn.emtytext')+'</div>'
        },
        title: WtfGlobal.getLocaleText('pm.admin.company.customcol.title'),
        tools:[{
            id:'add_column',
            qtip: WtfGlobal.getLocaleText('pm.customcolumn.add.qtip'),
            allowDomMove:false,
            scope : this,
            handler: this.handleAddColumn
        }]
    });
    this.on("cellclick", this.gridcellClickHandle, this);
    this.on("render",function(){
        if(Wtf.cusColumnDS.getCount()==0)
            this.getView().refresh();  // to show no records if there are no records.
    },this);
}

Wtf.extend(Wtf.CCGrid,Wtf.grid.GridPanel,{
    onRender: function(config){
        Wtf.CCGrid.superclass.onRender.call(this,config);
        //            this.on("show",this.setNewStyle,this);
        this.setNewStyle();
    },
    gridcellClickHandle :function(obj,row,col,e) {
        var event = e ;
        if(event.getTarget("div[class='viewcc']"))
            this.handleViewColumn(row);
        else if(event.getTarget("div[class='editcc']"))
            this.handleEditColumn();
        else if(event.getTarget("div[class='deletecc']"))
            this.handleDeleteColumn();

    },
    handleAddColumn : function(){
        this.mode=0;
        if(!Wtf.getCmp('createCCWin')){
            this.addCCForm = new Wtf.CCForm({
                mode: this.mode,
                successHandler: this.createCCSuccess.createDelegate(this),
                failureHandler: this.createCCFailure.createDelegate(this),
                data: {}
            });
            this.addCCForm.on("close", this.createCCFormClose, this);
            this.addCCForm.cancelBtn.on('click', this.createCCFormClose, this);
            this.addCCForm.show();
        }
    },
    handleViewColumn : function(row){
        var _p=["Number Field","Checkbox","Date Field","Dropdown","Text Field","Rich TextBox","Text Area","MultiSelect Combobox","File Upload"];
        var obj = this.cusColumnSM.getSelected().data;
        var md =[];
        for(var i=0; i<obj.masterdata.length; i++){
            var d = new Array(obj.masterdata[i]);
            if(d!="")
                md[i] = d;
        }
        var defvalue = obj.defaultValue.trim()!=""?obj.defaultValue:"-"
        var data = [
        [WtfGlobal.getLocaleText('pm.admin.company.customcol.columntype'), Wtf.cc.getcolumntype(obj.type)],
        [WtfGlobal.getLocaleText('pm.admin.company.customcol.defaultvalue'), defvalue],
        [WtfGlobal.getLocaleText('pm.admin.company.customcol.appliedto'), obj.module],
        [WtfGlobal.getLocaleText('pm.admin.company.customcol.mandatory'), (obj.mandatory?WtfGlobal.getLocaleText('lang.yes.text'):WtfGlobal.getLocaleText('lang.no.text'))],
        [WtfGlobal.getLocaleText('pm.admin.company.customcol.enabled'), (obj.enabled?WtfGlobal.getLocaleText('lang.yes.text'):WtfGlobal.getLocaleText('lang.no.text'))],
        [WtfGlobal.getLocaleText('pm.admin.company.customcol.visible'),(obj.visible?WtfGlobal.getLocaleText('lang.yes.text'):WtfGlobal.getLocaleText('lang.no.text'))],
        [WtfGlobal.getLocaleText('pm.common.createdon'),obj.createdDate]
        ];
        var grid = new Wtf.grid.GridPanel({
            id:'viewccgrid',
            width: '100%',
            autoHeight: true,
            autoScroll: true,
            border:false,
            ds: new Wtf.data.SimpleStore({
                fields:[{
                    name:'name'
                },{
                    name:'value'
                }],
                data:data
            }),
            columns:[{
                header:'<b>'+WtfGlobal.getLocaleText('lang.column.name')+'</b> :',
                dataIndex:'name',
                width:150,
                fixed: true
            },{
                header:"<b>"+obj.header+"<b/>",
                dataIndex:'value',
                width:215,
                fixed:true,
                renderer: function(value, css, record, row, column, store){
                    if(row=='1' && obj.renderer=='date' && value.trim()!=""){
                        if(!isNaN(value)){
                            value=eval(value);
                            value = new Date(eval(value));
                            return value.format(WtfGlobal.getOnlyDateFormat());
                        }else{
                            return Date.parseDate(value,WtfGlobal.getOnlyDateFormat())
                        }

                    }else if(row=='6'){
                        var d = Date.parseDate(value,'Y-m-d');
                        return d.format(WtfGlobal.getOnlyDateFormat());
                    }
                    else{
                        return value;
                    }
                }
            }],
            bodyBorder:false,
            frame:false,
            hideBorders:true,
            viewConfig: {
                forceFit: true,
                autoFill: true    
            }
        });
        var viewCC = new Wtf.Window({
            title : WtfGlobal.getLocaleText('pm.admin.company.customcol.viewcustomcol'), 
            closable : true,
            modal : true,
            iconCls : 'iconwin',
            width : 400,
            height: 500,
            items:[{
                region : 'north',
                height : 80,
                border : false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                html: getTopHtml(WtfGlobal.getLocaleText('pm.admin.company.customcol.viewcustomcol'), WtfGlobal.getLocaleText('pm.admin.company.customcol.viewcustomcolumn'), "../../images/Custom-Columns.jpg")
            },{
                region : 'center',
                border : false,
                bodyStyle : 'background:#ffffff;font-size:10px;',
                items :[grid]
            },
            {
                region : 'south',
                border : false,
                bodyStyle : 'background:#ffffff;font-size:10px;',
                height:170,
                items :[new Wtf.grid.GridPanel({
                    id:'viewccgridmasterdata',
                    ds:new Wtf.data.SimpleStore({
                        fields:[{
                            name:'masterdata'
                        }],
                        data:md
                    }),
                    border:false,
                    columns:[{
                        header:'<b>'+WtfGlobal.getLocaleText('pm.common.options.dropdown')+'</b>',
                        dataIndex:'masterdata'
                    }],
                    width: '100%',
                    height:130,
                    autoScroll:true,
                    viewConfig:{
                        forceFit: true,
                        autoFill: true,
                        emptyText: WtfGlobal.getLocaleText('pm.common.nooption')
                    }
                })]

            }
            ],
            layout : 'border',
            id: 'createCCWin',
            autoDestroy: true
        })
        viewCC.show();
    },
    handleEditColumn : function(){
        var buf = this.cusColumnSM.getSelected().data;
        var obj = {
            'columnId':buf["columnId"],
            'header':buf["header"],
            'type'  :buf["type"],
            'module' :buf["module"],
            'visible':buf["visible"],
            'enabled':buf["enabled"],
            'mandatory':buf["mandatory"],
            'masterdata':buf["masterdata"]
        }
        this.mode = 1;
        if(!Wtf.getCmp('createCCWin')){
            this.editCCForm = new Wtf.CCForm({
                mode: this.mode,
                successHandler: this.createCCSuccess.createDelegate(this),
                failureHandler: this.createCCFailure.createDelegate(this),
                data: obj
            });
            this.editCCForm.on("close", this.createCCFormClose, this);
            this.editCCForm.cancelBtn.on('click', this.createCCFormClose, this);
            this.editCCForm.show();
        }
    },
    handleDeleteColumn : function(){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.331'), function(btn){
            if (btn == "yes") {
                Wtf.Ajax.request({
                    url: "../../customColumn.jsp?m=deleteColumn",
                    method: 'POST',
                    params: {
                        companyid : companyid,
                        columnId:this.cusColumnSM.getSelected().data['columnId'],
                        module: this.cusColumnSM.getSelected().data['module'],
                        header: this.cusColumnSM.getSelected().data['header']
                    },
                    success: function(response, e){
                        var resobj = eval( "(" + response.responseText.trim() + ")" );
                        if(resobj.success){
                            msgBoxShow([WtfGlobal.getLocaleText('lang.success.text'), WtfGlobal.getLocaleText('pm.msg.cuscolumndeleted')], 0);
                            bHasChanged=true;
                            if(refreshDash.join().indexOf("all") == -1)
                                refreshDash[refreshDash.length] = 'all';
                            projAdminChange=true;
                            Wtf.cusColumnDS.load()
                        }
                        else{
                            msgBoxShow(292, 1);
                        }
                    },
                    failure: function(response, e){
                        msgBoxShow(292, 1);
                    },
                    scope: this
                })
            }
        }, this);
    },
    createCCFormClose: function(){
        if(this.mode == 1){
            this.editCCForm.close();
        } else if(this.mode==0){
            this.addCCForm.close();
        }
        else {
            this.viewCCForm.close();
        }
    },
    createCCSuccess : function(btn, frm, action){
        var text = WtfGlobal.getLocaleText("lang.created.text");
        var resobj = eval( "(" + action.response.responseText.trim() + ")" );
        if(action.options.useraction == 1){
            text = WtfGlobal.getLocaleText("lang.edited.text");
            if(action.response && action.response.responseText != "" && action.response.responseText !== undefined){
                var edited = eval("(" + action.response.responseText + ")").data;
                var rec = this.cusColumnSM.getSelected().data;
                rec.projectname = edited.projectname;
                rec.image = edited.image;
                rec.description = edited.description;
                rec.count = edited.members;
            }
            this.editCCForm.close();
        } else {
            this.addCCForm.close();
        }
        msgBoxShow([WtfGlobal.getLocaleText('lang.success.text'), WtfGlobal.getLocaleText({key:'pm.msg.cc.success', params: text})], 0);
        //    collectProjectData();
        (action.options.useraction == 1) ? Wtf.cusColumnDS.load() : Wtf.cusColumnDS.reload();
        this.getView().refresh();
        bHasChanged=true;
        projAdminChange=true;
    },
    createCCFailure: function(btn, frm, action){
        if(action.options.useraction == 1){
            msgBoxShow(260, 1);
            this.editCCForm.close();
        } else {
            var resobj = eval( "(" + action.response.responseText.trim() + ")" );
            var type = frm.items.items[1].value;
            if(resobj.data.indexOf("Max")>-1){  // if message contans 'Max'
                if(type<3)  // non string type  and count is 10
                    msgBoxShow(295, 1);
                else if(type>=3) // for string type max value os 20
                    msgBoxShow(290, 1);
                else{
                    msgBoxShow(303, 1);
                }
            }
            else{
                msgBoxShow(303, 1);
            }
            this.addCCForm.close();
        }
        
    },
    setNewStyle : function(){
        var c = Wtf.getCmp('adminccgrid').el.dom.children;
        var d = Wtf.getCmp('adminccgrid').el.dom.childNodes;
        if(c){
            c[0].className += " cc-grid-title";
        }else{
            d[0].className += "cc-grid-title";
        }
    }
});
