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
Wtf.docs.com.permissionwin = function(config){
    Wtf.apply(this, config);
    this.Docid = config.docid;
    this.Grid = config.grid1;
    this.userid = config.userid;
    this.per = config.per;
    Wtf.docs.com.permissionwin.superclass.constructor.call(this, {
        id: 'perwin',
        title: WtfGlobal.getLocaleText('pm.document.permission.text'),
        closable: true,
        plain: true,
        layout: 'fit',
        width: 517,
        resizable: false,
        modal: true,
        autoHeight: true,
        shadow: false,
        iconCls: 'iconwin',
        border: false,
        animEl: 'setperid',
        items: [{
            layout : 'border',
            height : 160,
            id:'perwinRegion',
            items:[{
                region: 'north',
                height:160,
                layout:'column',
                border: false,
                items:[{
                    xtype: 'fieldset',
                    baseCls: 'permFieldSet',
                    columnWidth: 0.48,
                    border: false,
                    title: WtfGlobal.getLocaleText('pm.common.connection.text'),
                    autoHeight: true,
                    defaultType: 'radio',
                    labelWidth: 0,
                    layoutConfig: {
                        labelSeparator: ''
                    },
                    items: [{
                        id: 'nosel',
                        boxLabel: WtfGlobal.getLocaleText('pm.member.communities.connects'),
                        name: 'permissiongrp'
                    }, {
                        id: 'sel',
                        boxLabel: WtfGlobal.getLocaleText('pm.common.selectedconnection'),
                        name: 'permissiongrp'
                    }, {
                        id: 'nosel1',
                        boxLabel: WtfGlobal.getLocaleText('pm.common.foreveryone'),
                        name: 'permissiongrp'
                    }, {
                        id: 'nosel2',
                        boxLabel: WtfGlobal.getLocaleText('lang.none.text'),
                        name: 'permissiongrp'
                    }]
                },{
                    xtype: 'fieldset',
                    columnWidth: 0.48,
                    baseCls: 'permFieldSetNoBorder',
                    title: WtfGlobal.getLocaleText('pm.projects.text'),
                    autoHeight: true,
                    defaultType: 'radio',
                    labelWidth: 0,
                    layout: 'form',
                    layoutConfig: {
                        labelSeparator: ''
                    },
                    items: [{
                        id: 'allProj',
                        boxLabel: WtfGlobal.getLocaleText('pm.dashboard.widget.quicklinks.allmyprojects'),
                        name: 'permissiongrpproj'
                    }, {
                        id: 'selectedProjectRadioButton',
                        boxLabel: WtfGlobal.getLocaleText('pm.project.selecte.text'),
                        name: 'permissiongrpproj'
                    }, {
                        id: 'noneProj',
                        boxLabel: WtfGlobal.getLocaleText('lang.none.text'),
                        name: 'permissiongrpproj'
                    }]
                }]
            },{
                region:'center',
                id:'perwinCenter',
                layout: 'fit',
                hidden: true,
                border: false,
                items: this.addContactPanel()
            },{
                region: 'south',
                id:'perwinSouth',
                layout: 'fit',
                hidden: true,
                border: false,
                items: this.addProjectPanel()
            }]
        }],
        buttons:[{
            text: WtfGlobal.getLocaleText('lang.OK.text'),
            id: 'docok',
            scope: this,
            handler: this.okClicked,
            cls: 'docbutton'
        },{
            text: WtfGlobal.getLocaleText('lang.cancel.text'),
            id: 'doccancel',
            scope: this,
            handler: this.cancelClicked,
            cls: 'docbutton'
        }]
    });
    var evtName = Wtf.isWebKit ? 'check' : 'focus';
    Wtf.getCmp('sel').addListener(evtName, this.RadioCheck, this);
    Wtf.getCmp('nosel').addListener(evtName, this.RadioCheckblur, this);
    Wtf.getCmp('nosel1').addListener(evtName, this.RadioCheckblur, this);
    Wtf.getCmp('nosel2').addListener(evtName, this.RadioCheckblur, this);
    Wtf.getCmp('selectedProjectRadioButton').on(evtName, this.projectSelect, this);
    Wtf.getCmp('allProj').addListener(evtName, this.projRadioCheckblur, this);
    Wtf.getCmp('noneProj').addListener(evtName, this.projRadioCheckblur, this);
};

Wtf.extend(Wtf.docs.com.permissionwin, Wtf.Window, {
    loadMask: null,
    panel: null,
    colModel1 : null,
    colModel : null,
    ds : null,
    leftGrid : null,
    ds1 : null,
    myData1 : null,
    rightGrid : null,
    middiv : null,
    leftrecord : null,
    leftflag : null,
    rightflag : null,
    rightrecord : null,
    onRender: function(config){
        Wtf.docs.com.permissionwin.superclass.onRender.call(this, config);
        this.loadMask = new Wtf.LoadMask(this.el.dom, Wtf.apply(Wtf.get('perwin')));
        this.on("show", this.perDefault, this);
    },
    perDefault:function() {
        if(this.uPer==WtfGlobal.getLocaleText('pm.member.communities.connects'))
            Wtf.getCmp('nosel').setValue(true);
        else if(this.uPer==WtfGlobal.getLocaleText('pm.common.selectedconnection')){
            Wtf.getCmp('sel').setValue(true);
            this.RadioCheck();
        } else if(this.uPer=="Everyone")
            Wtf.getCmp('nosel1').setValue(true);
        else if(this.uPer==WtfGlobal.getLocaleText('lang.none.text'))
            Wtf.getCmp('nosel2').setValue(true);
        f = false;
        if(this.pPer==WtfGlobal.getLocaleText('pm.dashboard.widget.quicklinks.allmyprojects'))
            Wtf.getCmp('allProj').setValue(true);
        else if(this.pPer==WtfGlobal.getLocaleText('pm.project.selecte.text')){
            Wtf.getCmp('selectedProjectRadioButton').setValue(true);
            this.projectSelect();
        } else if(this.pPer==WtfGlobal.getLocaleText('lang.none.text'))
            Wtf.getCmp('noneProj').setValue(true);
    },

    cancelClicked : function(){
        Wtf.getCmp('perwin').close();
    },

    FillLeftGrid : function(){
//        var view  = this.leftGrid.getView();
//        view.refresh()
    },

//    FillRightGrid : function(){
//        var view  = this.rightContactGrid.getView();
//        view.refresh();
//        for(var no =0;no<this.rightContactStore.getCount();no++){
//            if(this.rightContactStore.getAt(no).data['perm']==1)
//                view.getCell(no,1).firstChild.firstChild.checked = true;
//        }
//    },

    LeftGridRowClicked : function(Lgrid, rowindex, e){
        this.leftflag = 1;
        this.leftrecord = this.ds.getAt(rowindex);
    },

    RightGridRowClicked : function(Rgrid, rowindex, e){
        this.rightflag = 1;
        this.rightrecord = this.rightContactStore.getAt(rowindex);
    },

    okClicked : function(){
        var param = {};
        param.pmode = 11;
        param.cmode = 4;
        param.docid = this.Docid;
        if(Wtf.getCmp('sel').getValue()){
            param.cmode = 2;
            param.cdata = "";
            var t = "";
            for(var i = 0; i < this.rightContactStore.getCount(); i++){
                var f = "false";
//                if(this.rightContactGrid.getView().getCell(i,1).firstChild.firstChild.checked == true)
//                    f = "true";
                t += this.rightContactStore.getAt(i).data["id"] + "_" + f +",";
            }
            param.cdata = t.substring(0, (t.length - 1));
        } else if(Wtf.getCmp("nosel").getValue()) {
            param.cmode = 1;
        } else if(Wtf.getCmp("nosel1").getValue()) {
            param.cmode = 3;
        }
        if(Wtf.getCmp('selectedProjectRadioButton').getValue()){
            param.pmode = 9;
            param.pdata = "";
            var t = "";
            for(var i = 0; i < this.rightProjStore.getCount(); i++){
                var f = "false";
//                if(this.rightProjGrid.getView().getCell(i,1).firstChild.firstChild.checked == true)
//                    f = "true";
                t += this.rightProjStore.getAt(i).data["id"] + "_" + f + ",";
            }
            param.pdata = t.substring(0, (t.length - 1));
        } else if(Wtf.getCmp("allProj").getValue()) {
            param.pmode = 10;
        }
        Wtf.Ajax.requestEx({
            url: Wtf.req.doc + "perm/setpermission.jsp",
            params: param
        }, this, function(response){
            msgBoxShow(217, 3);
            this.f1();
            this.grid1.getStore().reload();
            Wtf.getCmp('searchdoc').reset();
        }, function(req, response) {
            msgBoxShow(274, 1);
            this.f1();
        });
    },


    RadioCheck : function(radiobtn, state){
        var cObj = Wtf.getCmp("perwinCenter");
        if(cObj.hidden){
            var t = Wtf.getCmp("perwinRegion");
            t.setHeight(t.getSize().height + 170);
            cObj.show();
            t.doLayout();
        }
    },
    RadioCheckblur : function(radiobtn, state){
        var cObj = Wtf.getCmp("perwinCenter");
        if(!cObj.hidden){
            var t = Wtf.getCmp("perwinRegion");
            t.setHeight(t.getSize().height - 170);
            cObj.hide();
            t.doLayout();
        }
    },

    projectSelect: function(radiobtn, state){
        var cObj = Wtf.getCmp("perwinSouth");
        if(cObj.hidden){
            var t = Wtf.getCmp("perwinRegion");
            t.setHeight(t.getSize().height + 170);
            cObj.show();
            t.doLayout();
        }
    },
    projRadioCheckblur: function(radiobtn, state){
        var cObj = Wtf.getCmp("perwinSouth");
        if(!cObj.hidden){
            var t = Wtf.getCmp("perwinRegion");
            t.setHeight(t.getSize().height - 170);
            cObj.hide();
            t.doLayout();
        }
    },

    addContactPanel : function(){
        var colModel1 = new Wtf.grid.ColumnModel([{
            header: WtfGlobal.getLocaleText('pm.common.selectedconnection'),
            dataIndex : 'name',
            width: 180,
            renderer: this.imgrenderer
        }/*,{
            header: WtfGlobal.getLocaleText('lang.write.text'),
            width: 35,
            renderer:this.rwcheckBox
        }*/]);
        var colModel=new Wtf.grid.ColumnModel([{
            header: WtfGlobal.getLocaleText('pm.member.communities.connects'),
            width: 180,
            renderer: this.imgrenderer,
            dataIndex: 'name'
        }]);
        this.leftContactStore = new Wtf.data.Store({
            url: Wtf.req.doc + 'grid/getConnections.jsp?',
            baseParams: {
                mode: 'connection',
                docid: this.Docid,
                flag: 1
            },
            reader: new Wtf.data.JsonReader({
                root: "data",
                fields: [{
                    name: 'id'
                }, {
                    name: 'name'
                }]
            })
        });
        this.leftContactStore.on("load", this.FillLeftGrid,this);
        var leftGrid =  new Wtf.grid.GridPanel({
            cm: colModel,
            border: false,
            store: this.leftContactStore,
            width: 180,
            height: 180,
            autoScroll: true,
            bodyBorder: false
        });
        this.rightContactStore = new Wtf.data.Store({
            url: Wtf.req.doc + 'grid/getConnections.jsp',
            baseParams: {
                mode: 'connection',
                docid: this.Docid,
                flag: 2
            },
            reader: new Wtf.data.JsonReader({
                root: "data",
                fields: [{
                    name: 'id'
                }, {
                    name: 'name'
                },{
                    name: 'perm'
                }]
            })
        });
//        this.rightContactStore.on("load", this.FillRightGrid,this);
        this.rightContactGrid=new Wtf.grid.GridPanel({
            cm: colModel1,
            border: false,
            ds: this.rightContactStore,
            width: 180,
            height: 180,
            autoWidth:true,
            autoScroll: true,
            bodyBorder: false
        });
        var contactPanel = new Wtf.docs.com.managePanel({
            border: false,
            height: 140,
            title: WtfGlobal.getLocaleText('pm.common.selectedconnection'),
            autoDestroy: true,
            availableGrid: leftGrid,
            selectedGrid: this.rightContactGrid
        });
        return contactPanel;
    },

    addProjectPanel : function(){
        var colModel1 = new Wtf.grid.ColumnModel([{
            header: WtfGlobal.getLocaleText('pm.project.selecte.text'),
            dataIndex : 'name',
            width: 180,
            renderer: this.imgrenderer
        }/*,{
            header: WtfGlobal.getLocaleText('lang.write.text'),
            width: 35,
            renderer:this.rwcheckBox
        }*/]);
        var colModel=new Wtf.grid.ColumnModel([{
            header: WtfGlobal.getLocaleText('pm.dashboard.widget.quicklinks.allmyprojects'),
            width: 180,
            renderer: this.imgrenderer,
            dataIndex: 'name'
        }]);
        this.leftProjStore  = new Wtf.data.Store({
            url: Wtf.req.doc + 'grid/getConnections.jsp',
            baseParams: {
                mode: 'project',
                docid: this.Docid,
                flag: 1
            },
            reader: new Wtf.data.JsonReader({
                root: "data",
                fields: [{
                    name: 'id'
                }, {
                    name: 'name'
                }]
            })
        });
//        this.projds.on("load", this.FillLeftGrid,this);
        var leftProjGrid =  new Wtf.grid.GridPanel({
            cm: colModel,
            border: false,
            store: this.leftProjStore,
            width: 180,
            height: 150,
            autoScroll: true,
            bodyBorder: false
        });
        this.rightProjStore = new Wtf.data.Store({
            url: Wtf.req.doc + 'grid/getConnections.jsp',
            baseParams: {
                mode: 'project',
                docid: this.Docid,
                flag: 2
            },
            reader: new Wtf.data.JsonReader({
                root: "data",
                fields: [{
                    name: 'id'
                }, {
                    name: 'name'
                },{
                    name: 'perm'
                }]
            })
        });
//        this.projds1.on("load", this.FillRightGrid,this);
        this.rightProjGrid=new Wtf.grid.GridPanel({
            cm: colModel1,
            border: false,
            ds: this.rightProjStore,
            width: 180,
            height: 150,
            autoWidth:true,
            autoScroll: true,
            bodyBorder: false
        });
        var contactPanel = new Wtf.docs.com.managePanel({
            border: false,
            height: 140,
            title: WtfGlobal.getLocaleText('pm.project.selecte.text'),
            autoDestroy: true,
            availableGrid: leftProjGrid,
            selectedGrid: this.rightProjGrid
        });
        return contactPanel;
    },

    rwcheckBox : function() {
        return '<input type="Checkbox" />';
    },
    
    imgrenderer : function(a){
        return "<img src = '../../images/user16.png'/> " + a
    },

    f1 : function(){
        Wtf.getCmp('perwin').close();
    }
});


Wtf.docs.com.managePanel = function(conf){
    Wtf.apply(this, conf);
    this.movetoright = document.createElement('div');
    this.movetoright.className = "navigateButton navButtons moveToRight";
    this.movetoright.onclick = this.moveToRightBtnClicked.createDelegate(this,[]);
    this.movetoleft = document.createElement('div');
    this.movetoleft.className = "navigateButton navButtons moveToLeft";
    this.movetoleft.onclick = this.moveToLeftBtnClicked.createDelegate(this,[]);
    this.centerdiv = document.createElement("div");
    this.centerdiv.style.marginTop = '65px';
    this.centerdiv.appendChild(this.movetoright);
    this.centerdiv.appendChild(this.movetoleft);
    this.addEvents({
        "rightBtnClicked": true,
        "leftBtnClicked": true
    })
    Wtf.docs.com.managePanel.superclass.constructor.call(this, conf);
}

Wtf.extend(Wtf.docs.com.managePanel, Wtf.Panel, {
    layout: 'column',
    onRender: function(conf){
        Wtf.docs.com.managePanel.superclass.onRender.call(this, conf);
        this.add(new Wtf.Panel({
            columnWidth: 0.44,
            layout : 'fit',
            height: this.height,
            border: false,
            bodyStyle: 'padding:5px',
            items: [this.availableGrid]
        }));
        this.add(new Wtf.Panel({
            columnWidth: 0.1,
            layout : 'fit',
            height: this.height,
            border: false,
            bodyStyle: 'padding:5px',
            contentEl: this.centerdiv
        }));
        this.add(new Wtf.Panel({
            columnWidth: 0.44,
            layout : 'fit',
            height: this.height,
            border: false,
            bodyStyle: 'padding:5px',
            items: [this.selectedGrid]
        }));
        this.selectedGrid.getStore().load();
        this.availableGrid.getStore().load();
    },

    moveToLeftBtnClicked: function(){
        var rec = this.selectedGrid.getSelectionModel().getSelections();
        var selStore = this.selectedGrid.getStore();
        var availableStore = this.availableGrid.getStore();
        for(var i = 0; i < rec.length; i++){
            availableStore.add(rec[i]);
            selStore.remove(rec[i]);
        }
        this.fireEvent("leftBtnClicked", availableStore, selStore);
    },

    moveToRightBtnClicked: function(){
        var rec = this.availableGrid.getSelectionModel().getSelections();
        var selStore = this.selectedGrid.getStore();
        var availableStore = this.availableGrid.getStore();
        for(var i = 0; i < rec.length; i++){
            availableStore.remove(rec[i]);
            selStore.add(rec[i]);
        }
        this.fireEvent("rightBtnClicked", selStore, availableStore);
    }
});
