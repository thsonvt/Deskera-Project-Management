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
Wtf.docs.com.Grid = function(config){

    Wtf.apply(this, config);

    this.groupingView = new Wtf.grid.GroupingView({
        forceFit: true,
        showGroupName: false,
        enableGroupingMenu: false,
        hideGroupedColumn: false
    });

    this.reader = new Wtf.data.JsonReader({

        root: 'data',

        fields: [{
            name: 'Id',
            type: 'string'
        }, {
            name: 'Name',
            type: 'string'
        }, {
            name: 'Size',
            type: 'float'
        }, {
            name: 'Type',
            type: 'string'
        }, {
            name: 'DateModified',type: 'date',dateFormat: 'Y-m-j H:i:s'
        }, {
            name: 'Permission',
            type: 'string'
        }, {
            name: 'docprojper',
            type: 'string'
        }, {
            name: 'Status',
            type: 'string'
        }, {
            name: 'Author',
            type: 'string'
        },{
            name: 'Owner',
            type: 'string'
        }, {
            name: 'version',
            type: 'string'
        },{
            name: 'Tags',
            type: 'string'
        },{
            name: 'storeindex',
            type: 'string'
        },{
            name: 'readwrite',
            type: 'string'
        }]
    });
    //------------------------------------------------------------

    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.sm2 = new Wtf.grid.CheckboxSelectionModel();
    this.sm2.id = "chk";
    this.sm2.addListener('selectionchange', this.rowSelectionHandler, this);
    this.txtareaid = config.id + 'edittag';
    //componentid=config.id;
    //-------------------------------------------------------------
    this.panelid = this.id.substr(11);
    this.divele = document.createElement("div");
    this.divele.id = 'divTag' + config.id;
    this.divele.className = 'docTagDiv';
    this.myTextArea = document.createElement("textarea");
    this.myTextArea.id = this.txtareaid;
    this.myTextArea.style.height = '100%';
    this.myTextArea.style.width = '80%';
    this.myTextArea.style.display = 'none';
    this.myTextArea.style.fontSize = '20px';
    //myTextArea.style.display = 'block';
    this.txtboxid = config.id + 'tagtextbox';
    // this.txtboxid = 'tagtextbox';
    this.bttnid = config.id + 'tagbttn';
    this.delbttid = config.id + 'del_butt';
    this.downbttid = config.id + 'down_butt';
    this.sharebttid = config.id + 'sharedoc';
    this.permbttid = config.id + 'perm';
    this.alldownbttid = config.id + 'alldown_butt';
    this.showbttid = config.id + 'show_butt';
    this.ver = config.id + 'ver_butt';
    this.addFilesid = config.id + 'addFile';
    this.tabtabpanelid = config.id + 'tabtabpanel';
    // this.bttnid = 'tagbttn';
    this.revlist = config.id+'revlist_butt';
    this.textbox = new Wtf.form.TextField({
        id: this.txtboxid,
        cls: 'docTagTextBox',
        width: 100,
        maxLength:90
    });
    this.textbox.setVisible(false);
    this.bttn = new Wtf.Button({
        id: this.bttnid,
        text: WtfGlobal.getLocaleText('pm.common.tag.add'),
        cls: 'bttn',
        disabled: true
    });
    this.bttn.setVisible(false);

    this.ds = new Wtf.data.GroupingStore({
        url: Wtf.req.doc + 'grid/fillGrid.jsp?groupid=' + this.groupid + '&pcid=' + this.pcid + '&tab=*flag*&projid='+this.panelid,
        reader: this.reader,
        sortInfo: {
            field: 'Type',
            direction: "DESC"
        }

    });

    this.cm = new Wtf.grid.ColumnModel([this.sm, {
        id: 'Name',
         header: WtfGlobal.getLocaleText('lang.name.text'),
        dataIndex: 'Name',
        tableColumn: 'docname',
        xtype: 'textfield',
        sortable: true,
        groupable: true,
        groupRenderer: WtfGlobal.nameRenderer
    }, {
        header: WtfGlobal.getLocaleText('lang.size.text'),
        dataIndex: 'Size',
        sortable: true,
        align: 'right',
        groupable: true,
        renderer: function(val){
            return val + " KB"
        },
        groupRenderer: WtfGlobal.sizeRenderer
    }, {
        header: WtfGlobal.getLocaleText('lang.type.text'),
        dataIndex: 'Type',
        tableColumn: 'doctype',
        xtype: 'textfield',
        sortable: true,
        groupable: true
    }, {
        id: 'Date_Modified',
        header: WtfGlobal.getLocaleText('pm.project.document.datemodified'),
        dataIndex: 'DateModified',
        sortable: true,
        align: 'center',
        tableColumn: 'docdatemod',
        xtype: 'datefield',
        renderer: WtfGlobal.userPrefDateRenderer,
        groupable: true,
        groupRenderer: WtfGlobal.dateFieldRenderer
    }, {
        id: 'author',
       header: WtfGlobal.getLocaleText('lang.author.text'),
        dataIndex: 'Author',
        tableColumn:"concat(fname,' ',lname)",
        sortable: true,
        align: 'center',
        xtype: 'textfield',
        groupable: true
    }, {
        header: WtfGlobal.getLocaleText('pm.contacts.permission'),
        dataIndex: 'Permission',
        tableColumn: 'docper',
        xtype: 'combo',
        xtypeStore: this.getPermStore(),
        sortable: true,
        groupable: true,
        renderer: this.permRenderer,
        groupRenderer: WtfGlobal.permissionConRenderer
    }, {
        header: WtfGlobal.getLocaleText('pm.project.permission'),
        dataIndex: 'docprojper',
        tableColumn: 'docprojper',
        xtype: 'combo',
        xtypeStore: this.getProjPermStore(),
        sortable: true,
        groupable: true,
        renderer: this.permRenderer,
        groupRenderer: WtfGlobal.permissionProjRenderer
    }, {
        header: WtfGlobal.getLocaleText('pm.common.versioning.text'),
        dataIndex: 'version',
        tableColumn: 'version',
        xtype: 'combo',
        xtypeStore: this.getVersionStore(),
        sortable: true,
        groupable: true
    }/*, {
                        header: WtfGlobal.getLocaleText('lang.status.text'),
                        dataIndex: 'Status',
                        sortable: true,
                        groupable: true
                    }*/
    ]);


    this.cm.defaultSortable = true;
    this.sharedoc = [this.sm,{
        id: 'Name',
        header: WtfGlobal.getLocaleText('lang.name.text'),
        dataIndex: 'Name',
        tableColumn: 'docname',
        xtype: 'textfield',
        sortable: true,
        groupable: true,
        groupRenderer: WtfGlobal.nameRenderer
    }, {
       header: WtfGlobal.getLocaleText('lang.size.text'),
        dataIndex: 'Size',
        sortable: true,
        align: 'right',
        groupable: true,
        renderer: function(val){
            return val + " KB"
        },
        groupRenderer: WtfGlobal.sizeRenderer
    }, {
         header: WtfGlobal.getLocaleText('lang.type.text'),
        dataIndex: 'Type',
        tableColumn: 'doctype',
        xtype: 'textfield',
        sortable: true,
        groupable: true
    }, {
        id: 'Date_Modified',
        header: WtfGlobal.getLocaleText('pm.project.document.datemodified'),
        dataIndex: 'DateModified',
        tableColumn: 'docdatemod',
        xtype: 'datefield',
        sortable: true,
        align: 'center',
        renderer: WtfGlobal.userPrefDateRenderer,
        groupable: true,
        groupRenderer: WtfGlobal.dateFieldRenderer
    }, {
        header: WtfGlobal.getLocaleText('lang.author.text'),
        dataIndex: 'Author',
        sortable: true,
        groupable: true
    }];
    //    var setCheckedIcon = function(eChk, sNone, index, grid){
    //        eChk.setIconClass(eChk.getIconClass() == 'none' ? 'pwnd checked' : 'pwnd checked');
    //        Wtf.each(sNone, function(sb){
    //            sb.setIconClass('none');
    //        });
    //        grid.fireEvent('headerclick', grid, index);
    //    }
    this.getAdvanceSearchComponent();

    Wtf.docs.com.Grid.superclass.constructor.call(this, {
        layout: 'fit',
        items: [{
            layout: 'border',
            border: false,
            autoWidth: true,
            items:[this.tagTreePanel = {
                region:'west',
                border:false,
                layout:'fit',
                frame:false,
                autoScroll: Wtf.isWebKit ? false : true,
                id :'tagtreepanel'+this.id,
                bodyStyle:'background:White;padding:10px;',
                width : "15%",
                split:true
            },this.docTabPanel = new Wtf.TabPanel({
                border:false,
                id:'doctabpanel'+this.id,
                enableTabScroll: true,
                region:'center',
                items:[
                this.doccontainerPanel = new Wtf.Panel({
                    layout:'border',
                    title:WtfGlobal.getLocaleText('pm.project.document.doclist'),
                    id: 'container'+this.id,
                    bbar: this.bBar(config),
                    items: [
                        this.advSearchComp,
                        this.grid1 = new Wtf.grid.GridPanel({
                            border: false,
                            region: 'center',
                            id: 'topic-grid' + config.id,
                            store: this.ds,
                            height:50,
                            //                        enableColumnHide: false,
                            view: this.groupingView,
                            cm: this.cm,
                            sm: this.sm2,
                            trackMouseOver: true,
                            viewConfig: {
                                forceFit: true
                            },
                            loadMask: {
                                msg: WtfGlobal.getLocaleText('pm.loading.text')+''+ WtfGlobal.getLocaleText('lang.documents.text') +'...'
                            },
                            tbar: [WtfGlobal.getLocaleText('pm.common.quicksearch')+': ', this.quickSearchTF = new Wtf.KWLQuickSearch({
                                field: 'Name',
                                id : 'searchdoc',
                                width: 200,
                                emptyText: WtfGlobal.getLocaleText('pm.project.document.search.empty')
                            }),'-', this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
                                text : WtfGlobal.getLocaleText('pm.common.search.advance'),
                                id:'advSearch'+this.id,
                                scope : this,
                                tooltip:WtfGlobal.getLocaleText('pm.advancedsearch.tip'),
                                handler : this.configureAdvancedSearch,
                                iconCls : 'pwnd searchtabpane'
                            })]
                        }), {
                            region: 'south',
                            minHeight: 75,
                            height: 100,
                            id: this.tabtabpanelid,
                            title: WtfGlobal.getLocaleText('pm.common.tags'),
                            frame: true,
                            border: false,
                            header: true,
                            items: [this.divele, this.myTextArea, this.textbox, this.bttn],
                            split: true
                        }]
                })
                //                    })
                ]
            })]
        }]

    });
    if(this.groupid=="1")
    {
        dojo.cometd.subscribe("/"+this.userid+"/"+this.groupid+"/docs", this, "publishHandler");
    }
    else
    {
        dojo.cometd.subscribe("/"+this.pcid+"/"+this.groupid+"/docs", this, "publishHandler");
    }
    dojo.cometd.subscribe("/"+companyid+"/"+this.groupid+"/docs", this, "publishHandler");
    this.advSearchComp.on("filterStore",this.filterStore, this);
    this.advSearchComp.on("clearStoreFilter",this.clearStoreFilter, this);

//EventManagers
};


Wtf.extend(Wtf.docs.com.Grid, Wtf.Panel, {

    loadMask: null,
    txtboxid: '',
    editable: 0,
    gridrowindex: '',
    root: null,
    defaultTag: null,
    tagsArray: null,
    tempSpans: null,
    spanlength: null,
    mainTree: null,
    flagForTreeClick: 0,
    flagForReloadTree: 0,
    regx : '^([\'"]?)\\s*([\\w]+[(/|\\\{1})]?)*[\\w]\\1$',
    tagregx: "\\w+|(([\'\"])\\s*([\\w][\\s[\\\\|\\/]\\w]*)\\s*\\2)|([\\w][\\s[\\\\|\\/]\\w]*)",
    patt1 : /(['"])\s*([\w]+[\s\\|\/\w]*)\s*\1/g,
    /*
        *  regx contains string capture by [\'|\"] this group followed by any number of whitespaces and more than one number of alphanumeric cha
         * then string capture by ([\\s\\\\\\/]*\\w+) this group followed by any number of whitespaces then
         * backrefreance to capture groupnumber 1 which is ([\'|\"])
    */
    patt2 : /([\w][\\|\/\w]*)/g,
    /*
    * capture the string which start with alphanumeric charector and it contains zero or more occurance of string captute by
    * (\\w+([\\\\\\/]*\\w+)*
    */
    timer:new Wtf.util.DelayedTask(this.callrefreshGrid),
    //     Wtf.getCmp(this.delbttid).disable();
    //        Wtf.getCmp(this.downbttid).disable();
    //        Wtf.getCmp(this.showbttid).disable();
    //        Wtf.getCmp(this.ver).disable();
    //        Wtf.getCmp(this.revlist).disable();
    bBar:function(config){
        return [{
            text: WtfGlobal.getLocaleText('om.project.document.add'),
            id: this.addFilesid,
            iconCls: 'pwnd doctabicon',
            hidden : (!this.archived)? false :true,
            scope: this,
            tooltip: {
                text: WtfGlobal.getLocaleText('pm.Help.addprojectdoc')
            },
            handler: this.AddFiles
        }, '-', {
            iconCls: 'pwnd delicon',
            id: this.delbttid,
            scope: this,
            hidden : (!this.archived)? false :true,
            disabled:true,
            tooltip: {
                text: WtfGlobal.getLocaleText('pm.Help.deletedoc')
            },
            handler: this.DeleteFiles

        }, '-', toggleBttn = new Wtf.Button({
            id: 'toggleBttn' + config.id,
            text: WtfGlobal.getLocaleText('pm.project.document.showgroup'),
            iconCls: 'pwnd showgrp',
            tooltip: {
                text: WtfGlobal.getLocaleText('pm.Help.showingroup')
            },
            enableToggle: true
        /*,
             pressed: true*/
        }), {
            text: WtfGlobal.getLocaleText('lang.sort.text'),
            id : 'sortdocs' + config.id,
            iconCls: 'pwnd arrange',
            tooltip: {
                text: WtfGlobal.getLocaleText('lang.sort.click')
            },
            scope:this,
            handler:this.attachMenu
        }, '-', {
            text: WtfGlobal.getLocaleText('pm.project.document.download'),
            iconCls: 'dpwnd dldicon',
            id: this.downbttid,
            disabled:false,
            scope: this,
            tooltip: {
                text: WtfGlobal.getLocaleText('pm.Help.download')
            },
            handler: this.DownloadAllFiles
        }, '-', {
            text: WtfGlobal.getLocaleText('pm.project.document.viewfile'),
            id: this.showbttid,
            disabled:true,
            iconCls: 'dpwnd Openfile',
            scope: this,
            tooltip: {
                text: WtfGlobal.getLocaleText('pm.Help.showdoc')
            },
            handler: this.fileShow
        },'-', {
            text: WtfGlobal.getLocaleText('pm.document.share'),
            iconCls: 'dpwnd shareicon',
            id: this.sharebttid,
            disabled: true,
            hidden: !WtfGlobal.getDocAccessStatus(),
            scope: this,
            tooltip: {
                text: WtfGlobal.getLocaleText('pm.document.share.tooltip')
            },
            handler: this.shareDocument
        }, '-', {
            text: WtfGlobal.getLocaleText('pm.document.permission.text'),
            iconCls: 'pwnd permicon',
            scope: this,
            disabled: true,
            id: this.permbttid,
            tooltip: {
                text: WtfGlobal.getLocaleText('pm.document.perm.tooltip')
            },
            handler: this.setFilePermissions
        }, '-', {
            text: WtfGlobal.getLocaleText('pm.document.version.activate'),
            id: this.ver,
            disabled:true,
            scope: this,
            tooltip: {
                text: WtfGlobal.getLocaleText('pm.Help.version')
            },
            handler: this.ActivateVer
        },'-', {
            text: WtfGlobal.getLocaleText('pm.common.revisionlist'),
            iconCls: 'pwnd RevisionList',
            id: this.revlist,
            disabled:true,
            scope: this,
            tooltip: {
                text: WtfGlobal.getLocaleText('pm.Help.revlist')
            },
            handler: this.ViewRevlist
        }/*        ,'-',{
         text:WtfGlobal.getLocaleText('pm.common.content.text'),
         handler:function(){
            Wtf.getCmp("as").loadTab("lms.html","tabcourse",WtfGlobal.getLocaleText('pm.common.content.text'),'navareadashboard',Wtf.etype.lms,false);
         }
         }*/
        ]
    },

    getPermStore: function(){
        var permData = [
            [ 0 , 'All My Connections' ],
            [ 1 , 'Selected Connections' ],
            [ 2 , 'Everyone' ]
        ];
        var permDs = new Wtf.data.SimpleStore({
            fields: [
                {name:"id"},
                {name:"name"}
            ]
        });
        permDs.loadData(permData);
        return permDs;
    },
    getProjPermStore: function(){
        var projPermData = [
            [ 0 , 'All My Projects' ],
            [ 1 , 'Selected Projects' ]
        ];
        var projPermDs = new Wtf.data.SimpleStore({
            fields: [
                {name:"id"},
                {name:"name"}
            ]
        });
        projPermDs.loadData(projPermData);
        return projPermDs;
    },
    getVersionStore: function(){
        var verData = [
            [ 0 , 'Active' ],
            [ 1 , 'Inactive' ]
        ];
        var verDs = new Wtf.data.SimpleStore({
            fields: [
                {name:"id"},
                {name:"name"}
            ]
        });
        verDs.loadData(verData);
        return verDs;
    },
    configureAdvancedSearch:function(){
        if(this.quickSearchTF.getValue() != ''){
            this.quickSearchTF.reset();
            this.ds.load();
        }
        this.advSearchComp.show();
        this.advSearchComp.advSearch = true;
        this.advSearchComp.cm = this.cm;
        this.advSearchComp.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
    },
    getAdvanceSearchComponent:function(){
        this.advSearchComp=new Wtf.advancedSearchComponent({
            cm: this.cm,
            module: 'documents',
            advSearch:false
        });
    },
    clearStoreFilter:function(){
        if(this.quickSearchTF.getValue() != ''){
            this.quickSearchTF.reset();
        }
        this.ds.load();
        this.searchJson="";
        this.AdvanceSearchBtn.enable();
        this.advSearchComp.hide();
        this.doLayout();
    },
    filterStore:function(json){
        this.ds.load({params: {searchJson: json}});
    },

    setCheckedIcon: function(eChk, sNone, index, grid){
        eChk.setIconClass('dpwnd checked');
        this.sortParam = eChk;
        Wtf.each(sNone, function(sb){
            sb.setIconClass('none');
        });
        grid.fireEvent('headerclick', grid, index);
    },
    publishHandler: function(msg) {
        if(msg.data.action=="add") {
            var recarr = this.reader.readRecords(eval('('+msg.data.data+')'));
            if(this.grid1.getStore().find('Id',recarr.records[0].data["Id"])==-1){
                this.grid1.getStore().add(recarr.records);
                this.grid1.getView().refresh();
                this.mainTree.makeTree(recarr.records[0].data["Tags"], this.mainTree.root);
                this.tagsArray[this.grid1.getStore().getCount()] = []
            }

        } else if(msg.data.action=="svnadd") {
            var index = this.grid1.getStore().find('Id',msg.data.data);
            if(index>-1) {
                this.grid1.getStore().getAt(index).set("version","Active");
                Wtf.getCmp(this.ver).disable();
                Wtf.getCmp(this.revlist).enable();
            }
        }/* else if(msg.data.action=="status") {
            var respobj = eval('('+msg.data.data+')');
            var index = this.grid1.getStore().find('Id',respobj[0].id);
            if(index>-1) {
                this.grid1.getStore().getAt(index).set("Status",respobj[0].status);
            }
        }*/ else if(msg.data.action=="delete") {
            var rec =this.grid1.store.find("Id",msg.data.data);
            if(rec>-1){
                if(msg.data.permDelete==null || this.grid1.store.getAt(rec).data['Owner']==0){
                    for(var j = 0;j<this.grid1.store.getAt(rec).data['Tags'].split(',').length;j++) {
                        this.mainTree.breakTree(this.grid1.store.getAt(rec).data['Tags'].split(',')[j],this.mainTree.root);
                    }
                    this.grid1.store.remove( this.grid1.store.getAt(rec));
                    this.grid1.getView().refresh();
                }
            }
        }
        else if(msg.data.action=="commit") {
            var com =  eval('('+msg.data.data+')')
            var index = this.grid1.getStore().find('Id',com.Id);
            if(index>-1) {
                this.grid1.getStore().getAt(index).set("Size",com.Size);
                this.grid1.getStore().getAt(index).set("DateModified",Date.parseDate(com.DateModified,'Y-m-d H:i:s'));
            }
        }
        bHasChanged=true;
        if(refreshDash.join().indexOf("all") == -1 && refreshDash.join().indexOf("doc") == -1)
            refreshDash[refreshDash.length] = 'doc';
    },

    afterRender: function(config){
        Wtf.docs.com.Grid.superclass.afterRender.call(this, config);
        hideMainLoadMask();
        if(this.id != 'doc-mydocs'){
            this.cm.setConfig(this.sharedoc);
        }
        if(!this.archived)
            this.grid1.getView().emptyText = '<div class="emptyGridText">'+WtfGlobal.getLocaleText('pm.project.document.emptytext')+' <br><a href="#" onClick=\'getDocs(\"'+this.id+'\")\'>'+WtfGlobal.getLocaleText('pm.document.starttext')+'</a></div>';
        this.initPage();
        this.loadMask = new Wtf.LoadMask(this.el.dom, Wtf.apply(Wtf.get('topic-grid' + config.id)));
        this.defaultTag = new Array();
        this.defaultTag = ['shared','uncategorized','shared/'];
        this.tagsArray = [];
        this.tempSpans = [];
        this.spanlength = [];
        this.loadMask.show();
        if(this.groupid!=1)
            this.grid1.getColumnModel().setHidden(5,true);
        this.ds.on("loadexception",function(){
            hideMainLoadMask();
            msgBoxShow(269, 1);
            this.loadMask.hide();
        },this);
        this.ds.on('load', function(){
            this.loadMask.hide();
            Wtf.getCmp(this.tabtabpanelid).doLayout();
            this.mainTree.clearTree(this.mainTree.root);
            for (var i = 0; i < this.ds.getCount(); i++) {
                var a = this.ds.getAt(i).data['Tags'].split(',');
                for (var j = 0; j < a.length; j++) {
                    this.mainTree.makeTree(a[j], this.mainTree.root);
                }
            }
            this.mainTree.root.expand();
            var rowcount = this.ds.getCount();
            for (var x = 0; x <= rowcount; x++) {
                this.tagsArray[x] = [];
                this.tempSpans[x] = [];
                this.tagsArray[x][0] = 'Uncategorized';
                this.tempSpans[x][0] = 'Uncategorized';
            }
            this.quickSearchTF.StorageChanged(this.ds);
            this.mainTree.on('click', function(node, e){
                this.timer.cancel();
                this.timer.delay(1000,this.callrefreshGrid,this);
                this.flagForTreeClick = 1;
            }, this);
            if(globalDocId != "")
                this.abc();
            this.searchPerformed = false;
            if(this.quickSearchTF.getValue() != "" || this.advSearchComp.advSearch)
                this.searchPerformed = true;
            if(this.ds.getCount() == 0){
                this.quickSearchTF.setDisabled(true);
                this.AdvanceSearchBtn.setDisabled(true);
                Wtf.getCmp(this.downbttid).setDisabled(true);
            } else {
                this.quickSearchTF.setDisabled(false);
                this.AdvanceSearchBtn.setDisabled(false);
                Wtf.getCmp(this.downbttid).setDisabled(false);
            }
            this.emptyTextChange();
        }, this);
        this.ds.load();
        this.mainTree = new Wtf.docs.com.Tree({
            rootText:this.treeroot,
            grid: this.grid1,
            userid: this.userid,
            id: 'tree'+this.id,
            groupid: this.groupid,
            defaultTag: this.defaultTag,
            pcid:this.pcid
        });
        // this.mainTree.setRootNode(this.mainTree.setRootNode1(this.treeroot));
        // this.mainTree.render(this.treeRenderto);
        Wtf.getCmp('tagtreepanel'+this.id).add(Wtf.getCmp('tree'+this.id));
        //       this.mainTree.render('tagtreepanel'+this.id);
//        this.mainTree.root.select();
        this.mainTree.on('onDeleteComplete', this.onDeleteComplete, this);

        //  this.mainTree.loadTree(this.mainTree.root, this.groupid);
        // this.setGroupid(this.groupid);
        this.docTabPanel.activate(Wtf.getCmp('container'+this.id));
        this.docTabPanel.on('afterlayout',function(){
            if(this.id != 'doc-mydocs'){
                Wtf.getCmp(this.revlist).hide();
                Wtf.getCmp(this.ver).hide();
                Wtf.getCmp(this.sharebttid).hide();
                Wtf.getCmp(this.permbttid).hide();
            }
        },this);
        this.grid1.on("sortchange", function(b, bd){
            if (toggleBttn.pressed)
                this.grid1.getStore().groupBy(bd.field);
        }, this);
        this.grid1.on("headerclick", function(obj, ci, e){
            this.setCheckedIconForActoin(ci, obj);
        }, this);
    },
    setCheckedIconForActoin: function(ci, obj){
        if(ci > 0) {
            var eChk = Wtf.getCmp("sortBy" + ci);
            if(eChk) {
                eChk.setIconClass('dpwnd checked');
            }
            this.sortParam = eChk;
            if(eChk){
                for(var cnt = 1; cnt <= 7; cnt++){
                    if(cnt != ci)
                        Wtf.getCmp("sortBy" + cnt).setIconClass('none');
                }
            }
        }
    },
    callrefreshGrid:function(){
        this.refreshGrid(this.mainTree.getSelectionModel().getSelectedNode());
    },
    onDestroy: function(config){
        Wtf.destroy(this.mainTree);
    },

    onDeleteComplete: function(){
        this.refreshGrid(this.mainTree.root);
        this.flagForReloadTree = 1;
    },
    abc: function(a, b){
        var row = -1;
        if(globalDocId != ""){
            docid = globalDocId;
            row = this.ds.indexOf(this.ds.getAt(this.ds.find("Id",globalDocId)));
            globalDocId = "";
            if(row != -1)
                this.grid1.getSelectionModel().selectRow(row);
        }
        this.fileShow();
    },
    //    permissionRenderer: function(val, rec){
    //        var text = val.toLowerCase();
    //        switch (text) {
    //            case "everyone":
    //                text = "Everyone on deskEra";
    //                break;
    //            case "connections":
    //                text = "All Connections";
    //                break;
    //            case "none":
    //                text = "Private";
    //                break;
    //            default:
    //                text = WtfGlobal.getLocaleText('pm.common.selectedconnection');
    //                break;
    //        }
    //        return text;
    //    },
    permRenderer: function(val, a, rec){
        if(rec.data["Owner"] != "1"){
            val = "--";
        }
        return val;
    },
    attachMenu: function(a){
        var menuList = new Wtf.menu.Menu({
            id: 'menu'+this.id,
            items:[
            sortBy1 = new Wtf.Action({
                text: WtfGlobal.getLocaleText('lang.name.text'),
                id: "sortBy1",
                scope: this,
                handler: function(){
                    this.setCheckedIcon(sortBy1, [sortBy2, sortBy3, sortBy4, sortBy5, sortBy6, sortBy7], 1, this.grid1);
                }
            }, this), sortBy2 = new Wtf.Action({
                text: WtfGlobal.getLocaleText('lang.size.text'),
                id: "sortBy2",
                iconCls: 'none',
                scope: this,
                handler: function(){
                    this.setCheckedIcon(sortBy2, [sortBy1, sortBy3, sortBy4, sortBy5, sortBy6, sortBy7], 2, this.grid1);
                }
            }, this), sortBy3 = new Wtf.Action({
                text: WtfGlobal.getLocaleText('lang.type.text'),
                id: "sortBy3",
                iconCls: 'none',
                scope: this,
                handler: function(){
                    this.setCheckedIcon(sortBy3, [sortBy1, sortBy2, sortBy4, sortBy5, sortBy6, sortBy7], 3, this.grid1);
                }
            }, this), sortBy4 = new Wtf.Action({
                text: WtfGlobal.getLocaleText('pm.project.document.datemodified'),
                id: "sortBy4",
                iconCls: 'none',
                scope: this,
                handler: function(){
                    this.setCheckedIcon(sortBy4, [sortBy1, sortBy2, sortBy3, sortBy5, sortBy6, sortBy7], 4, this.grid1);
                }
            }, this), sortBy5 = new Wtf.Action({
                text: WtfGlobal.getLocaleText('lang.author.text'),
                id: "sortBy5",
                iconCls: 'none',
                scope: this,
                handler: function(){
                    this.setCheckedIcon(sortBy5, [sortBy1, sortBy2, sortBy3, sortBy4, sortBy6, sortBy7], 5, this.grid1);
                }
            }, this), sortBy6 = new Wtf.Action({
                text: WtfGlobal.getLocaleText('pm.contacts.permission'),
                id: "sortBy6",
                iconCls: 'none',
                scope: this,
                handler: function(){
                    this.setCheckedIcon(sortBy6, [sortBy1, sortBy2, sortBy3, sortBy4, sortBy5, sortBy7], 6, this.grid1);
                }
            }, this), sortBy7 = new Wtf.Action({
                text: WtfGlobal.getLocaleText('pm.project.permission'),
                id: "sortBy7",
                iconCls: 'none',
                scope: this,
                handler: function(){
                    this.setCheckedIcon(sortBy7, [sortBy1, sortBy2, sortBy3, sortBy4, sortBy5, sortBy6], 7, this.grid1);
                }
            }, this)]
        });
        a.menu = menuList;
        if(this.id != "doc-mydocs"){
            sortBy7.hide();
            sortBy6.hide();
        }
        if(this.sortParam)
            Wtf.getCmp(this.sortParam.initialConfig.id).setIconClass('dpwnd checked');
        else
            sortBy3.setIconClass('dpwnd checked');
        a.showMenu();

    },
    fileShow: function(){
        var selectedRow = this.grid1.getSelectionModel().getSelected();
        var docid = selectedRow.get('Id');
        if(WtfGlobal.getDocAccessStatus()){

            Wtf.Ajax.requestEx({
                url: Wtf.req.doc + "file-releated/filecontent/fileViewCheck.jsp",
                params:{
                    docid:docid
                }
            },
            this,
            function(resp){
                var respText = eval('('+resp+')');
                if(respText.exists){
                    if(this.panelid != ""){
                        var flag = 1;
                    }
                    this.openDocument(selectedRow, flag, true);
                } else {
                    if(respText.converted){
                        if(respText.download=="no"){
                            this.openDocument(selectedRow, flag);
                        } else {
                            msgBoxShow([WtfGlobal.getLocaleText("lang.document.text")+" "+WtfGlobal.getLocaleText("lang.view.text"), WtfGlobal.getLocaleText('pm.document.view.filenotloaded')], 0);
                        }
                    } else {
                        msgBoxShow([WtfGlobal.getLocaleText("lang.document.text")+" "+WtfGlobal.getLocaleText("lang.view.text"), WtfGlobal.getLocaleText('pm.document.view.process')],0);
                    }
                }
            });
        } else {
            Wtf.Ajax.requestEx({
                url: Wtf.req.doc + "file-releated/filecontent/filedownloadchk.jsp",
                params:{
                    docid:docid
                }
            }, this,
            function(resp,option){
                var respText = eval('('+resp+')');
                if(this.panelid != ""){
                    var flag = 1;
                }
                if(respText.download=="no"){
                    this.openDocument(selectedRow, flag, false);
                } else {
                    msgBoxShow([WtfGlobal.getLocaleText("lang.document.text")+" "+WtfGlobal.getLocaleText("lang.view.text"), WtfGlobal.getLocaleText('pm.document.view.filenotloaded')], 0);
                }
            });
        }
    },

    openDocument: function(selectedRow, flag, viewable){
        var docid = selectedRow.get('Id');
        var fileContent = Wtf.getCmp('tabfcontent'+docid);
        if(fileContent==null){
            fileContent= new Wtf.FilecontentTab({
                url: docid,
                id: 'tabfcontent'+docid,
                parentid: this.docTabPanel.id,
                flag: flag,
                title: selectedRow.get('Name'),
                fileType: selectedRow.get('Type'),
                viewable: viewable
            });
            this.docTabPanel.add(fileContent);
            this.docTabPanel.doLayout();
            this.docTabPanel.activate(fileContent);
            mainPanel.loadMask.msg = WtfGlobal.getLocaleText('pm.loadingfilecontent.text')+' '+WtfGlobal.getLocaleText('lang.pleasewait.text')+'...'
            mainPanel.loadMask.show();
        } else {
            this.docTabPanel.setActiveTab(fileContent);
        }
    },

    DeleteFiles: function(){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.322'), function(btn){
            if (btn == "yes") {
                this.DeleteFile();
            }
        }, this);
    },
    DeleteFile: function(){
        var selected = this.sm2.getSelections();
        if (selected.length != 0) {
            var selIds = "";
            for (var i = 0; i < selected.length; i++) {
                if(selected[i].data["Owner"] != "1") {
                    msgBoxShow(177, 1);
                    continue;
                }
                if (i == (selected.length - 1)) {
                    if (selected[i].data['Name'] != "" && selected[i].data['Cost'] != "") {
                        selIds += selected[i].data['Id'];
                    }
                }
                else {
                    if (selected[i].data['Name'] != "" && selected[i].data['Cost'] != "") {
                        selIds += selected[i].data['Id'] + ",";
                    }
                }
            }
            if(selIds != "")
                this.deleteButttonClick(this.groupid, selIds, this.userid, selected.length, this.mainTree,this.pcid,this.grid1);
        }
        else {
            msgBoxShow(308, 0);
        }
    },
    ViewRevlist:function()
    {
        var selected = this.sm2.getSelections();
        var selNames = "";
        var selIds = "";
        var fileExt = "";
        if(selected[0].data['Name'].indexOf(".")>-1)
            fileExt = selected[0].data['Name'].substr(selected[0].data['Name'].lastIndexOf("."));
        selNames += selected[0].data['Id']+fileExt;
        selIds += selected[0].data['Id'];
        //Wtf.getCmp("as").loadTab(Wtf.req.doc + 'file-releated/revision/revisionListMain.jsp?url=' + selNames, selIds+"revisionlist",selected[0].data['Name'], '',Wtf.etype.docs);
        var revisionlistTab = Wtf.getCmp(selNames);
        if(revisionlistTab==null){
            revisionlistTab= new Wtf.revisionlistTab({
                url: selNames,
                id: 'tabrevlist'+selIds,
                parentid: this.docTabPanel.id,
                //                                                        layout:'fit',
                tabname: selected[0].data['Name'],
                fileType: selected[0].data["Type"],
                docid: selected[0].data["Id"]
            });
            this.docTabPanel.add(revisionlistTab);
            this.docTabPanel.doLayout();
        }
        this.docTabPanel.activate(revisionlistTab);

    //        function revisionlistFun(url, tabid, tabname, docid, fileType){
    //        revisionlistFun(selNames,selIds+'revisionlist',selected[0].data['Name'],selected[0].data["Id"],selected[0].data["Type"]);
    },

    ActivateVer:function()
    {
        var selected = this.sm2.getSelections();
        var selNames = "";
        var selIds = "";
        var fileExt = "";
        function addfile(val)
        {
            if(val=="yes")
            {
                Wtf.Ajax.request({

                    url: "fileAddAction.jsp",
                    params:{
                        fileadd: 'true',
                        filename: selNames,
                        fileid: selIds,
                        groupid: this.groupid,
                        pcid: this.pcid
                    },
                    scope:this,
                    method: 'POST',
                    success: function(frm, action){
                        if (frm.responseText != "") {
                            var uploadstr = eval('(' + frm.responseText + ')');
                            if (uploadstr.msg != null && uploadstr.msg != "1") {
                                msgBoxShow([WtfGlobal.getLocaleText('pm.msg.ERROR'), uploadstr.msg], 1);
                            }else if(uploadstr.action=="svnadd"){
                                var index = this.grid1.getStore().find('Id',uploadstr.data);
                                if(index>-1){
                                    this.grid1.getStore().getAt(index).set("version","Active");
                                    Wtf.getCmp(this.ver).disable();
                                    Wtf.getCmp(this.revlist).enable();
                                }
                            }
                        }
                    }
                })
            }
        }


        if(selected[0].data['Name'].indexOf(".")>-1)
            fileExt = selected[0].data['Name'].substr(selected[0].data['Name'].lastIndexOf("."));
        selNames += selected[0].data['Id']+fileExt;
        selIds += selected[0].data['Id'];
        Wtf.Msg.confirm(WtfGlobal.getLocaleText('pm.document.version.activate'), WtfGlobal.getLocaleText('pm.document.versioning.activate.prompt'), addfile, this);

    },
    DownloadFiles: function(){
        var selected = this.sm2.getSelections();
        var selNames = "";
        var selIds = "";
        var fileExt = "";
        if (selected[0].data['Name'].indexOf(".") > -1)
            fileExt = selected[0].data['Name'].substr(selected[0].data['Name'].lastIndexOf("."));
        selNames += selected[0].data['Id'] + fileExt;
        selIds += selected[0].data['Id'];
        selIds += "&pid"+this.panelid;
        setDownloadUrl(selIds);
    },
    DownloadAllFiles: function(){
        var selected = this.sm2.getSelections();
        var selIds = [];
        var length = selected.length;

        if(length > 0){

            if(length > 1){
                for(var i =0 ;i< length;i++ ){
                    selIds.push(selected[i].get('Id'));
                }
                selIds.push( "&pid"+this.panelid);
                setDownloadUrlZip(selIds);

            } else if(length == 1) {
                selIds = "";
                selIds += selected[0].data['Id'];
                selIds += "&pid"+this.panelid;
                setDownloadUrl(selIds);
            }

        } else if(length == 0){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.337'), function(btn){
                if (btn == "yes") {
                    length = this.ds.getCount();
                    if(length > 0){
                        for(i =0 ;i<length ;i++ ){
                            selected = this.ds.getAt(i);
                            selIds.push(selected.get('Id'));
                        }
                        selIds.push( "&pid"+this.panelid);
                    }
                    setDownloadUrlZip(selIds);
                }
            }, this);
        }
    },
    shareDocument: function(btn){
        var selected = this.sm2.getSelected();
        if(selected){
        Wtf.Ajax.requestEx({
            url: Wtf.req.doc + "file-releated/filecontent/fileViewCheck.jsp",
            params:{
                docid:selected.get('Id')
            }
        },
        this,
        function(resp){
            var respText = eval('('+resp+')');
            var fileflag = false;
            if(respText.exists){
                fileflag = true;
            } else {
                 if(respText.converted){
                     msgBoxShow([WtfGlobal.getLocaleText("lang.document.text")+" "+WtfGlobal.getLocaleText("pm.document.share"), WtfGlobal.getLocaleText('pm.document.share.cannotshare')], 0);
                 }else{
                     fileflag = true;
                 }
            }
            if(fileflag){
//               var url = location.href.replace("#", "", "g") + "showDoc.jsp?d=" + selected.get('Id') + "&u=" + loginid;
                var url = Wtf.pagebaseURL + "showDoc.jsp?d=" + selected.get('Id');
                url =  url.replace("/b/", "/a/", "i");
                var selURL = "<a href='javascript:document.exportLinkForm1.exportLinkField1.select()'>"+WtfGlobal.getLocaleText('pm.document.share.selecturl')+"</a>";
                selURL = selURL+"<form name='exportLinkForm1'><textarea readonly='' name='exportLinkField1' style='width:500px;background:white' onclick='javascript:document.exportLinkForm1.exportLinkField1.select()'>"+url+"</textarea>";
                var msgExport1 = WtfGlobal.getLocaleText("pm.document.share.sharedescription")+"<br/>"+selURL;
                msgBoxShow([WtfGlobal.getLocaleText('lang.document.text')+" "+WtfGlobal.getLocaleText('pm.document.share'), msgExport1], 0);
            }
        })
        }
    },
    AddFiles: function(){
        // var flagforgrid =  uploadButttonClick(this.grid1, this.groupid, this.mainTree.root.text, this.userid,this);
        if(Wtf.get('upfilewin')==null)
            var flagforgrid = this.uploadButttonClick(this);
    //1 is  for userid;
    },
    rowSelectionHandler: function(){
        var selected = this.sm2.getSelections();

        if (selected.length == 0) {
            Wtf.getCmp(this.delbttid).disable();
//            Wtf.getCmp(this.downbttid).disable();
            Wtf.getCmp(this.ver).disable();
            Wtf.getCmp(this.revlist).disable();
            Wtf.getCmp(this.txtboxid).setVisible(false);
            Wtf.getCmp(this.bttnid).setVisible(false);
        }
        else {
            Wtf.getCmp(this.delbttid).enable();
            Wtf.getCmp(this.txtboxid).setVisible(true);
            Wtf.getCmp(this.bttnid).setVisible(true);
        }
        if (selected.length == 1) {
//            Wtf.getCmp(this.downbttid).enable();
            var data = selected[0].data;
            if(data.version!="Active" && (data.readwrite=='1' || data.Owner=='1'||this.groupid!=1))
                Wtf.getCmp(this.ver).enable();
            if(data.version=="Active")
                Wtf.getCmp(this.revlist).enable();
            Wtf.getCmp(this.showbttid).enable();
            if (data.Owner == '1' && this.groupid == 1) {
                Wtf.getCmp(this.sharebttid).enable();
                Wtf.getCmp(this.permbttid).enable();
            }
        }
        else {
//            Wtf.getCmp(this.downbttid).disable();
            Wtf.getCmp(this.showbttid).disable();
            Wtf.getCmp(this.ver).disable();
            Wtf.getCmp(this.revlist).disable();
            Wtf.getCmp(this.sharebttid).disable();
            Wtf.getCmp(this.showbttid).disable();
            Wtf.getCmp(this.permbttid).disable();
        }
        this.onDocGridRowClick(this.grid1,0);
    },

    onDocGridRowContextMenu: function(grid, num, e){
        e.preventDefault();
        var data = grid.store.getAt(num).data;
        var open = new Wtf.Action({
            text: WtfGlobal.getLocaleText('pm.project.document.viewfile'),
            iconCls: 'dpwnd OpenfileCx',
            scope: this,
            tooltip: {
                text: WtfGlobal.getLocaleText('pm.Help.showdoc')
            },
            disabled: true,
            handler: function(){
                this.fileShow();
            }
        });
        var dwnld = new Wtf.Action({
            text: WtfGlobal.getLocaleText('pm.project.document.download'),
            iconCls: 'dpwnd dldicon',
            handler: function(){
                grid.fireEvent('rowdblclick', grid, data);
            }
        });
        var share = new Wtf.Action({
            text: WtfGlobal.getLocaleText('pm.document.share'),
            iconCls: 'dpwnd shareicon',
            disabled: true,
            hidden: !WtfGlobal.getDocAccessStatus(),
            scope: this,
            tooltip: {
                text: WtfGlobal.getLocaleText('pm.document.share.tooltip')
            },
            handler: function(){
                this.shareDocument();
            }
        });
        var permission = new Wtf.Action({
            text: WtfGlobal.getLocaleText('pm.project.member.permission.set'),
            iconCls: 'pwnd permicon',
            scope: this,
            handler: this.setFilePermissions
        },this);
        //        var status = new Wtf.Action({
        //            text: WtfGlobal.getLocaleText('pm.common.setstatus'),
        //            iconCls: 'pwnd statusicon',
        //            scope : this,
        //            handler: function(){
        //                this.statusButttonClick(grid, grid.getSelectionModel().getSelected().get('Id'), grid.getSelectionModel().getSelected().get('Status'), this.userid,this.groupid,this.pcid);
        //            }
        //        },this);
        if (this.groupid == 1 && data.Owner == 1 && this.panelid == "") {
            var gridMenu = new Wtf.menu.Menu({
                items: [open, dwnld, share, permission/*, status*/]
            });
        } else {
            gridMenu = new Wtf.menu.Menu({
                items: [open, dwnld/*, status*/]
            });
        }
        grid.getSelectionModel().selectRow(num);
        grid.fireEvent('rowclick', grid, num, e);
        //        rownum = num;
        open.enable();
        share.enable();
        var posnX = e.getPageX();
        var posnY = e.getPageY();
        gridMenu.showAt([posnX, posnY]);
        return false;
    },

    setFilePermissions: function(btn){
        var selected = this.sm2.getSelected();
        var data = selected.data;
        if(selected){
            new Wtf.docs.com.permissionwin({
                userid :this.userid,
                grid1 :this.grid1,
                title : WtfGlobal.getLocaleText('pm.document.permission.text'),
                height : 400,
                docid :data.Id,
                uPer :data.Permission,
                pPer: data.docprojper
            }).show();
        }
    },

    initPage: function(){
        Wtf.EventManager.addListener(this.myTextArea, 'blur', this.handleLost, this);
        Wtf.EventManager.addListener(this.divele, 'dblclick', this.handleClickDiv, this);
        Wtf.EventManager.addListener(this.myTextArea, 'keydown', this.handleTextSubmit, this);
        this.quickSearchTF.on('SearchComplete', this.QuickSearchComplete, this);
        this.grid1.on('rowclick', this.onDocGridRowClick, this);
        this.grid1.on('rowcontextmenu', this.onDocGridRowContextMenu, this);
        toggleBttn.on('toggle', this.onGroupBttnClick, this);
        this.grid1.addListener('rowdblclick', this.abc, this);
        this.textbox.on('specialkey', this.specialKey, this);
    },

    /* setGroupid: function(id){
        groupid = id;
    },*/


    refreshGrid: function(node){
        this.loadMask.show();
        var node1 = node;
        this.textbox.setVisible(false);
        this.bttn.setVisible(false);
        this.divele.style.display = 'none';

        var depth = node.getDepth();

        var tabpath = [];
        for (var i = 0; i < depth; i++) {

            tabpath[i] = node.text.split(' (')[0];
            node = node.parentNode;
        }
        var string = '';
        for (var j = tabpath.length - 1; j >= 0; j--) {
            string += tabpath[j] + '/';
        }
        if (depth == 0) {
            string = "*flag*/";
        }

        var reader = new Wtf.data.JsonReader({
            root: 'data',
            fields: [{
                name: 'Id',
                type: 'string'
            },{
                name: 'Name',
                type: 'string'
            },{
                name: 'Size',
                type: 'float'
            },{
                name: 'Type',
                type: 'string'
            },{
                name: 'DateModified',
                type: 'date',
                dateFormat: 'Y-m-j H:i:s'
            },{
                name: 'Permission',
                type: 'string'
            },{
                name: 'docprojper',
                type: 'string'
            },{
                name: 'Status',
                type: 'string'
            },{
                name: 'Author',
                type: 'string'
            },{
                name: 'Owner',
                type: 'string'
            },{
                name: 'version',
                type: 'string'
            },{
                name: 'Tags',
                type: 'string'
            },{
                name: 'storeindex',
                type: 'string'
            },{
                name: 'readwrite',
                type: 'string'
            }]
        });

        string = string.substr(0, string.length - 1);
        var url2 = Wtf.req.doc + "grid/fillGrid.jsp?";
        url2 += "groupid=" + this.groupid;
        url2 +="&pcid=" + this.pcid;
        url2 +="&projid=" + this.panelid;
        url2 = url2 + "&tab=" + string;

        var ds1 = new Wtf.data.GroupingStore({
            url: url2,
            reader: reader,
            sortInfo: {
                field: 'Type',
                direction: "DESC"
            }
        });

        ds1.load();
        ds1.on("loadexception",function(){
            msgBoxShow(269, 1);
            this.loadMask.hide();
        },this);
        ds1.on("load", function(){
            this.flagForTreeClick = 0;
            this.grid1.getStore().removeAll();
            var view = this.grid1.getView();
            view.refresh();
            var records = ds1.getRange(0, (ds1.getTotalCount() - 1));
            this.grid1.getStore().add(records);
            view.refresh();
            this.quickSearchTF.StorageChanged(this.grid1.getStore());
            if(this.flagForReloadTree ==1){
                this.flagForReloadTree =0;
                var nodelength = this.mainTree.root.childNodes.length;
                var nodearr = this.mainTree.root.childNodes;
                for(var q = 0; q<nodelength; q++){
                    this.mainTree.root.removeChild(nodearr.shift());
                }
                for (var i = 0; i < ds1.getCount(); i++) {
                    var a = ds1.getAt(i).data['Tags'].split(',');
                    for (var j = 0; j < a.length; j++) {
                        this.mainTree.makeTree(a[j], this.mainTree.root);
                    }
                }
                this.mainTree.root.expand(false,false);
            }
            this.loadMask.hide();
            this.docTabPanel.setActiveTab(0);
            this.doLayout();
        }, this);
    //toggleBttn.toggle(true);
    },
    stringToArray:function(arrayString,delimetor){
        return arrayString.split(delimetor);
    },

    onDocGridRowClick: function(obj, index, e){
        if (this.flagForTreeClick == 0) {
            var tagname = [];
            /* Wtf.getCmp(this.textbox.id).setVisible(true);
            Wtf.getCmp(this.bttn.id).setVisible(true);*/
            Wtf.EventManager.addListener(this.textbox.id, 'keyup', this.handleKeyPress, this);
            Wtf.EventManager.addListener(this.bttn.id, 'click', this.handleBttnClick, this);
            var tagname1="";
            for(var a = 0; a < this.grid1.getSelectionModel().getCount();a++){
                var rec = this.grid1.getSelectionModel().getSelections()[a];
                var temp = rec.data['Id'];
                tagname =  tagname.concat(this.stringToArray(rec.data['Tags'],','));
            }
            tagname = tagname.sort();
            var tagArray1 = new Array();
            vardocid = temp;
            tagArray1 = arrayUniq(tagname);
            var spanele;
            var div = this.divele;//document.getElementById('divTag');
            div.innerHTML = "";
            div.style.display = 'block';
            this.editable = 1;
            for (var i = 0; i < tagArray1.length; i++) {
                spanele = document.createElement("span");
                spanele.className = 'spanelement';
                spanele.innerHTML = tagArray1[i];
                spanele.id = 'span' + i;
                spanele.style.color = "#15428b";
                div.appendChild(spanele);
                Wtf.EventManager.addListener("span" + i, 'mouseover', this.handleMouseOver, this);
                Wtf.EventManager.addListener("span" + i, 'mouseout', this.handleMouseOut, this);
                Wtf.EventManager.addListener("span" + i, 'click', this.handleMouseClick, this);
            }

        }
    },

    onGroupBttnClick: function(bttnobj, isPressed){
        if (isPressed)
            this.grid1.getStore().groupBy(this.grid1.getStore().getSortState().field);
        else
            this.grid1.store.clearGrouping();
    },
    handleTextSubmit: function(e){
        var cal = e.getKey();

        if (cal == 13) {
            var t = Wtf.getCmp(this.txtboxid);
            var te = this.myTextArea;
            //document.getElementById('edittag');
            te.style.display = 'none';
            var div = this.divele;
            //document.getElementById("divTag");
            div.style.display = 'block';
            t.focus();
        }
    },

    handleKeyPress: function(obj){
        var textval = this.textbox.getValue();
        if (textval == '' || (this.editable == 0))
            this.bttn.disable();
        else
            this.bttn.enable();
    },
    checkSystemTag:function(tag){

        if(this.defaultTag.indexOf(tag.toLowerCase())!=-1){
            return false;
        }else if(this.defaultTag.indexOf(tag.toLowerCase().substr(0,tag.indexOf('/')+1))!=-1){
            return false;
        }
        return true;
    },
    _fillTmpArray: function(tagstr){
        var tagarr = tagstr.split(',');
        var arr = [];
        for (var q = 0; q < tagarr.length; q++) {
            if (!this.checkSystemTag(tagarr[q])) {
                arr.push(tagarr[q]);
                break;
            }
        }
        return arr;
    },
    checkForDuplicateEntry:function(arr){
        var temparr = arr.join(',').toLowerCase().split(',');
        for(var i=0;i<arr.length;i++){
            temparr.shift();
            if(temparr.indexOf(arr[i].toLowerCase())>-1 || !this.checkSystemTag(arr[i])){
                arr[i] = '-';
            }
        }
        while(arr.length!=arr.remove('-').length)
            arr.remove('-');
        return arr;
    },

    handleLost: function(e){
        var flagtemp = 0;
        var spanele;
        this.textbox.setVisible(true);
        this.bttn.setVisible(true);
        var div = this.divele;
        div.innerHTML = "";
        var shared = "shared";
        var Uncategorized = "uncategorized";
        var tarea = this.myTextArea;
        var text = tarea.value;
        var tagMatches;
        //ext = text.replace(/\s+uncategorized\s+/gi," ")
        tagMatches = text.match(this.patt1);
        text = text.replace(this.patt1,"");
        if(tagMatches!=null){
            tagMatches = tagMatches.concat(text.match(this.patt2));
        }else{
            tagMatches = text.match(this.patt2);
        }
        var arr = [];
        var tagArray = new Array();
        var selectedRow = this.grid1.getSelectionModel().getSelected();
        var recno = this.grid1.store.find("Id", selectedRow.data['Id'], 0, false, true);
        if(tagMatches && tagMatches.length>0){
            tagMatches = this.checkForDuplicateEntry(tagMatches);
        }
        if(tagMatches && tagMatches.length > 0 ){
            if (selectedRow.data['Owner'] == 0 && this.groupid == 1)
                arr = this._fillTmpArray(selectedRow.data['Tags']);
            if (arr.length > 0)
                arr = arr.concat(tagMatches);
            else
                arr = tagMatches;
        }else{
            if(selectedRow.data['Owner']== 0 && this.groupid == 1)
                arr = this._fillTmpArray(selectedRow.data['Tags']);
            else{
                arr[0] = "Uncategorized";
                this.mainTree.makeTree(arr[0], this.mainTree.root)
            }
        }
        var allTag = '';
        for (var i = 0; i < arr.length; i++) {
            spanele = document.createElement("span");
            spanele.className = 'spanelement';
            spanele.innerHTML = arr[i];
            spanele.id = 'span' + i;
            spanele.style.color = "#15428b";
            div.appendChild(spanele);
            Wtf.EventManager.addListener("span" + i, 'mouseover', this.handleMouseOver, this);
            Wtf.EventManager.addListener("span" + i, 'mouseout', this.handleMouseOut, this);
            Wtf.EventManager.addListener("span" + i, 'click', this.handleMouseClick, this);
        }
        tarea.style.display = 'none';
        div.style.display = 'block';
        allTag = arr.join(",");
        var selectedtagArr = selectedRow.data['Tags'].split(',');
        for (var l = 0; l < arr.length; l++) {
            if(arr[l]!=null){
                if (this.tempSpans[recno].indexOf(arr[l].toLowerCase())>-1) {
                    flagtemp = 1;
                    this.tempSpans[recno][this.tempSpans[recno].indexOf(arr[l].toLowerCase())]="-";
                }
                if (flagtemp == 0 || this.tempSpans[recno][0] == '') {
                    //if(arr[l].toLowerCase()!=shared &&  arr[l].toLowerCase().substr(0,7)!=shared+"/"){
                    if(this.checkSystemTag(arr[l])){
                        this.mainTree.makeTree(arr[l], this.mainTree.root);
                    }
                }
                flagtemp = 0;
            }
        }
        var asd = 0;
        for (m = 0; m < this.tempSpans[recno].length; m++) {
            if ( this.tempSpans[recno][m] != '-') {
                if(this.tempSpans[recno][m].toLowerCase()!=shared &&  this.tempSpans[recno][m].toLowerCase().substr(0,7)!=shared+"/"){
                    this.mainTree.breakTree(this.tempSpans[recno][m], this.mainTree.root);
                }
            }
        }
        var alltag1 = selectedRow.data['Tags'];
        selectedRow.data['Tags']= allTag;
        Wtf.Ajax.requestEx({
            url: Wtf.req.doc + "tree/tab_tag.jsp",
            params: {
                docid: vardocid,
                tags: allTag,
                groupid: this.groupid,
                pcid:this.pcid
            }
        },
        this,
        function(result, req){
            if(eval('('+result+')')["res"]==1){
                selectedRow.data['Tags']= alltag1;
                msgBoxShow(244, 1);
            }
        });
    },

    handleBttnClick: function(obj, e){
        if(this.textbox.isValid()){
            var flagbtclick = 0;
            var flagbtclick1 = 1;
            var shared = "shared";
            var Uncategorized = "uncategorized";
            var flagShared=0;
            var flag = 0;
            var uncatflag =0;
            if (this.bttn.disabled == false) {
                var spanele;
                var div = this.divele;
                //document.getElementById('divTag');
                div.style.display = 'block';
                var text = WtfGlobal.HTMLStripper(this.textbox.getValue());
                text = text.trim();
                /*for (l = 0; l < text.length; l++) {
                if (text.charAt(l) == ' '&& text[0]!='"' || text[0]!="'" ) {
                    text = '"' + text + '"';
                    break;
                }
            }*/
                var check = text.match(this.patt1);
                if(check==null){
                    check = text.match(/^([\w][\s]*[\\|\/\w\s]*)$/g);
                }else{
                    text = text.replace(/'|"/g,"");
                }
                var docidarr = "";
                if (check != null && this.checkSystemTag(text.toLowerCase())) {
                    text = text.replace(/\/+/g, "/");
                    if(text.trim().match(/\s+/g)!=null){
                        text = "'"+text+"'";
                    }
                    var spans = div.getElementsByTagName('span');
                    for (var i = 0; i < spans.length; i++) {
                        if(spans[i].innerHTML.toLowerCase() == Uncategorized){
                            spans[i].innerHTML = '';
                        }
                    }
                    var selectedRow = this.grid1.getSelectionModel().getSelections();
                    for(var a = 0; a < this.grid1.getSelectionModel().getCount();a++){
                        var rec = selectedRow[a];
                        var temp = rec.data['Id'];
                        var tagname1 = rec.data['Tags'].split(',');
                        for (i = 0; i < tagname1.length; i++) {
                            if(tagname1[i].toLowerCase() == text.toLowerCase()) {
                                flagbtclick = 1;
                                flagbtclick1 = 0;
                            }
                            if (tagname1[i].toLowerCase() == 'uncategorized') {
                                this.mainTree.breakTree(tagname1[i], this.mainTree.root);
                                rec.data['Tags'] = '';
                            }
                        }
                        if(flagbtclick==0){
                            docidarr += temp+",";
                        }
                        flagbtclick=0;
                    }
                    docidarr = docidarr.substr(0,docidarr.length-1);

                    if(this.checkSystemTag(text)){
                        if(flagbtclick1 == 1){
                            spanele = document.createElement("span");
                            spanele.className = 'spanelement';
                            var child = div.getElementsByTagName('span');
                            spanele.id = 'span' + child.length;
                            spanele.innerHTML = text;
                            spanele.style.color = "#15428b";
                            div.appendChild(spanele);
                            Wtf.EventManager.addListener(spanele.id, 'mouseover', this.handleMouseOver, this);
                            Wtf.EventManager.addListener(spanele.id, 'mouseout', this.handleMouseOut, this);
                            Wtf.EventManager.addListener(spanele.id, 'click', this.handleMouseClick, this);
                        }
                        this.textbox.setValue("");
                        var docidarr1 = docidarr.split(',');
                        for(var l = 0; l<docidarr1.length; l++){
                            if(docidarr1[l]!=''){
                                this.mainTree.makeTree(text, this.mainTree.root);
                                var rec = this.grid1.store.find("Id", docidarr1[l], 0, false, true);
                                var alltag1 = this.grid1.store.getAt(rec).data['Tags'];
                                var rec1 = this.grid1.store.getAt(rec);
                                if(rec1.data['Tags']!='')
                                    rec1.data['Tags'] += ',' + text;
                                else
                                    rec1.data['Tags'] = text;
                            }
                        }
                        if(docidarr1[0]!=""){
                            Wtf.Ajax.requestEx({
                                url: Wtf.req.doc + "grid/addButtonTag.jsp",
                                params:{
                                    res: text,
                                    docid: docidarr,
                                    groupid: this.groupid,
                                    pcid:this.pcid
                                }
                            },
                            this,
                            function(result, req){
                                if(eval('('+result+')')["res"]==1)
                                    msgBoxShow(244, 1);
                            });
                        }
                    }
                } else {
                    msgBoxShow(204, 1);
                    this.textbox.setValue("");
                }
                this.textbox.setValue("");
            }
            this.bttn.setDisabled(true);
        //this.textbox.setValue("");
        }
    },

    specialKey: function(obj, e){
        var cal = e.getKey();
        if (cal == 13) {
            var textval = this.textbox.getValue();
            if (textval != '') {
                this.handleBttnClick();
            }
        }
    },
    handleClickDiv: function(e){
        if( this.grid1.getSelectionModel().getCount()==1){
            var selectedRow = this.grid1.getSelectionModel().getSelected();
            var recno = this.grid1.store.find("Id", selectedRow.data['Id'], 0, false, true);
            this.textbox.setVisible(false);
            this.bttn.setVisible(false);
            var div = this.divele;
            var spans = div.getElementsByTagName('span');
            var tarea = this.myTextArea;
            var shared = "shared";
            tarea.style.display = "block";
            tarea.value = "";
            var flag = 0;
            this.spanlength[recno] = spans.length;
            for (var i = 0; i < spans.length; i++) {
                if (i < (spans.length - 1)) {
                    if(!this.checkSystemTag(spans[i].innerHTML))
                        tarea.value += " ";
                    else
                        tarea.value += spans[i].innerHTML + " ";
                }
                else {
                    if(!this.checkSystemTag(spans[i].innerHTML))
                        tarea.value += " ";
                    else
                        tarea.value += spans[i].innerHTML;
                }
                var q = spans[i].innerHTML;
                this.tempSpans[recno][i] = q.toLowerCase();
                spans[i].style.display = 'none';
            }
            div.style.display = 'none'
            tarea.style.display = 'block';
            tarea.focus();
        }
    },

    handleMouseOver: function(e){
        var span = e.getTarget();
        span.style.backgroundColor = 'White';
        span.style.cursor = 'pointer';
    },

    handleMouseOut: function(e){
        var span = e.getTarget();
        span.style.backgroundColor = '';
    },

    handleMouseClick: function(obj, e){
        var nodeText = e.innerHTML;
        this.grid1.getSelectionModel().clearSelections();
        nodeText = nodeText.replace(/'/g,"");
        var arrnodeText = nodeText.split('/');
        var node12 = this.mainTree.root;
        for (var i = 0; i < arrnodeText.length; i++) {
            for (var j = 0; j < node12.childNodes.length; j++) {
                // var string = node12.childNodes[j].text.split("-");
                var string = node12.childNodes[j].text.split(" (");
                if (arrnodeText[i].toLowerCase()== string[0].toLowerCase()) {
                    node12 = node12.childNodes[j];
                    if (node12.childNodes.length > 0 && i + 1 != arrnodeText.length) {
                        node12.expand();
                    }
                }
            }
        }
        node12.fireEvent("click", node12);
        this.divele.innerHTML = '';
    },
    QuickSearchComplete: function(e){
        this.searchPerformed = true;
        this.emptyTextChange();
    },

    emptyTextChange: function(){
        var view = this.grid1.getView();
        if(this.grid1.getStore().getCount() > 0)
            view.emptyText = '<div class="emptyGridText">'+WtfGlobal.getLocaleText('pm.project.document.emptytext')+' <br><a href="#" onClick=\'getDocs(\"'+this.id+'\")\'>'+WtfGlobal.getLocaleText('pm.document.starttext')+'</a></div>';
        else if(this.searchPerformed)
            view.emptyText = '<div class="emptyGridText">'+WtfGlobal.getLocaleText('pm.document.search.emptytext')+' </div>';
        view.refresh();
    },

    uploadButttonClick: function(doccmp){
        var grid1 = doccmp.grid1;
        var groupid = doccmp.groupid;
        var pcid = doccmp.pcid;
        var userid = doccmp.userid;
        var ChkValue;
        var flagforgrid = 0;
        var filename;
        var flagforconfirm = 0;
        var ChkOwner;
        var fs = new Wtf.FormPanel({
            id: 'uploadfrm',
            frame: true,
            labelWidth: 55,
            width: 340,
            method: 'POST',
            fileUpload: true,
            waitMsgTarget: true,
            url: 'fileAddAction.jsp?fileadd=false&projectid='+this.panelid,
            onSubmit: FileUpload,
            layoutConfig: {
                labelSeparator: ''
            },
            items: [new Wtf.form.TextField({
                fieldLabel: WtfGlobal.getLocaleText('pm.common.filepath')+':',
                id: 'filepath',
                inputType: 'file',
                allowBlank:false
            }), new Wtf.form.TextArea({
                fieldLabel: WtfGlobal.getLocaleText('lang.comment.text')+':',
                width: 265,
                height: 60,
                maxLength: 80,
                id: 'comment'
            }), new Wtf.Button({
                cls: 'button11',
                text: WtfGlobal.getLocaleText('lang.cancel.text'),
                handler: close

            }), new Wtf.Button({
                id: 'upbtn',
                cls: 'button1',
                text: WtfGlobal.getLocaleText('pm.common.upload'),
                handler: FileUpload

            }), new Wtf.Panel({
                height: 0,
                items: [new Wtf.form.TextField({
                    id: 'pcid',
                    value: pcid,
                    height: 0
                }), new Wtf.form.TextField({
                    id: 'userid',
                    height: 0,
                    value: userid
                }), new Wtf.form.TextField({
                    id: 'groupid',
                    height: 0,
                    value: groupid
                }), new Wtf.form.TextField({
                    fieldLabel: 'docid',
                    id: 'docid',
                    height: 0
                }), new Wtf.form.TextField({
                    fieldLabel: 'docownerid',
                    id: 'docownerid',
                    height: 0
                }), new Wtf.form.TextArea({
                    fieldLabel: WtfGlobal.getLocaleText('lang.Type.tezxt'),
                    id: 'type',
                    height: 0
                })]
            })]
        });
        Wtf.getCmp('groupid').hide();
        Wtf.getCmp('userid').hide();
        Wtf.getCmp('pcid').hide();
        var div = document.createElement('div');
        div.id = 'formct';
        var win = new Wtf.Window({
            id: 'upfilewin',
            title: WtfGlobal.getLocaleText('pm.common.uploadfile'),
            closable: true,
            width: 357,
            plain: true,
            iconCls: 'iconwin',
            resizable: false,
            layout: 'fit',
            contentEl: div
        }).show();

        fs.render('formct');
        Wtf.getCmp('upfilewin').on("Hide",distroyfunction);
        function distroyfunction(){
            Wtf.get('upfilewin').destroy();
        }
        function UploadFile(){
            Wtf.getCmp('docid').setValue(ChkValue);
            Wtf.getCmp('docownerid').setValue(ChkOwner);
            //  Wtf.get('confirm').hide();
            filename = Wtf.get('filepath').dom.value;
            fs.form.submit({
                waitMsg: WtfGlobal.getLocaleText('pm.common.uploading')+'...',
                //                success: function(result, req){
                //                    msgBoxShow([WtfGlobal.getLocaleText('pm.msg.ERROR'), "A problem occurred while uploading document"], 1);
                //                    close();
                //                },
                success: function(frm, action){
                    close();
                    if (action.response.responseText != "") {
                        var uploadstr = eval('(' + action.response.responseText + ')');
                        if (uploadstr.msg != null && uploadstr.msg != "1") {
                            msgBoxShow([WtfGlobal.getLocaleText('pm.msg.ERROR'), uploadstr.msg], 1);
                        }else{
                            doccmp.quickSearchTF.setDisabled(false);
                            if(uploadstr.action=="add"){
                                var recarr = doccmp.reader.readRecords(eval('('+uploadstr.data+')'));
                                if(doccmp.grid1.getStore().find('Id',recarr.records[0].data["Id"])==-1){
                                    //recarr.records[0].data["docprojper"]="None";
                                    //  recarr.records[0].set("docprojper","None");
                                    doccmp.grid1.getStore().add(recarr.records);
                                    doccmp.grid1.getStore().reload();       // Provided as in grouping view duplicate file type was being added
                                    //                                    doccmp.grid1.getView().refresh(true);
                                    doccmp.mainTree.makeTree(recarr.records[0].data["Tags"], doccmp.mainTree.root);
                                }
                            }else if(uploadstr.action=="commit"){
                                var com =  eval('('+uploadstr.data+')')
                                var index = doccmp.grid1.getStore().find('Id',com.Id);
                                if(index>-1) {
                                    doccmp.grid1.getStore().getAt(index).set("Size",com.Size);
                                    doccmp.grid1.getStore().getAt(index).set("DateModified",Date.parseDate(com.DateModified,'Y-m-d H:i:s'));
                                }
                            }
                        }
                    } else
                        frm.reset();
                    if (flagforconfirm == 1) {
                        close1();
                        bHasChanged=true;
                        if(refreshDash.join().indexOf("all") == -1 && refreshDash.join().indexOf("doc") == -1)
                            refreshDash[refreshDash.length] = 'doc';
                    }
                },
                failure: function(frm, action){
                   msgBoxShow(188, 1);
                   frm.reset();
                   Wtf.getCmp('upbtn').enable();
                }
            });
        }

        function CheckChanged(radio, value){
            if (value) {
                ChkValue = radio.value;
                ChkOwner = radio.id;
            }
        }

        function result1(val, userdocid, flagtype){
            var panelContainer = new Wtf.Panel({
                id: 'panelContainer',
                border: false,
                frame: false,
                autoWidth: true,
                items: [{
                    xtype: 'fieldset',
                    labelWidth: 140,
                    id: 'uploadconfirm',
                    height: 100,
                    autoWidth: true,
                    defaultType: 'radio',
                    autoScroll:true,
                    border: false
                }, new Wtf.Button({
                    cls: 'button11',
                    minWidth: 60,
                    text: WtfGlobal.getLocaleText('lang.cancel.text'),
                    handler: close1

                }), new Wtf.Button({
                    id: 'OK',
                    cls: 'button1',
                    minWidth: 60,
                    text: WtfGlobal.getLocaleText('lang.OK.text'),
                    handler: UploadFile

                })]
            });
            var div1 = document.createElement('div');
            div1.id = 'formct1';
            win = new Wtf.Window({
                id: 'confirm',
                title: WtfGlobal.getLocaleText('lang.confirmation.text'),
                closable: true,
                width: 357,
                plain: true,
                iconCls: 'iconwin',
                resizable: false,
                layout: 'fit',
                contentEl: div1
            }).show();
            panelContainer.render('formct1');
            flagforconfirm = 1;
            for (var i = 0; i < val.length; i++) {
                if (i == 0) {
                    ChkValue = val[i].docid;
                    ChkOwner = val[i].userid;
                }
                var str = "Update " + val[i].username+"'s version";
                Wtf.getCmp('uploadconfirm').add({
                    id: val[i].userid,
                    name: 'a',
                    value: val[i].docid,
                    fieldLabel: str
                });
                Wtf.getCmp('uploadconfirm').doLayout();
                Wtf.getCmp(val[i].userid).on('change', CheckChanged);
            };
            if (flagtype == 1) {
                var str = "Update to My version"
                Wtf.getCmp('uploadconfirm').add({
                    id: 'my',
                    name: 'a',
                    fieldLabel: str,
                    value: userdocid
                });
                Wtf.getCmp('uploadconfirm').doLayout();
                Wtf.getCmp('my').on('change', CheckChanged);
            }
            else {
                var str = "Upload new file "
                Wtf.getCmp('uploadconfirm').add({
                    id: 'my',
                    name: 'a',
                    fieldLabel: str
                // value:'new'
                });
                Wtf.getCmp('uploadconfirm').doLayout();
                Wtf.getCmp('my').on('change', CheckChanged);
            }
        }

        function ResultFun(response, option){
            var value = eval('(' + response + ')');
            var rs = value.type;
            Wtf.getCmp('type').setValue(rs);
            var val = value.data;
            var userdocid = value.userdocid;
            if (rs == 1 || rs == 2) {
                if (rs == 1) {
                    result1(val, userdocid, 1);
                }
                if (rs == 2) {
                    ChkValue = userdocid;
                    ChkOwner = value.userid;
                    UploadFile();
                }
            } else if (rs == 3) {
                result1(val, userdocid, 0);
            } else if (rs == 4) {
                UploadFile();
            }
           bHasChanged=true;
           if(refreshDash.join().indexOf("all") == -1 && refreshDash.join().indexOf('doc') == -1)
            refreshDash[refreshDash.length] = 'doc';
        }

        function FileUpload(){
            if(fs.form.isValid()){
                Wtf.getCmp('upbtn').disable();
                if (document.getElementById('filepath').value <= 0) {
                    msgBoxShow(206,1);
                    Wtf.getCmp('upbtn').enable();
                } else {
                    var textArea = Wtf.get('filepath');
                    Wtf.Ajax.requestEx({
                        url: Wtf.req.doc + 'file-releated/filecontent/chkfile.jsp',
                        params: {
                            docname: textArea.getValue(),
                            groupid: groupid,
                            pcid: pcid

                        }
                    }, this, ResultFun, function(){
                        msgBoxShow(304, 1);
                        close();
                    });
                }
            }
        }
        function close1(){
            flagforconfirm = 0;
            Wtf.getCmp('upbtn').enable();
            Wtf.get('confirm').destroy();
        }
        function close(){
            /*var store=grid1.getStore();
         store.load();
         store.on("load",function(){
         grid1.getView().refresh();
         }
         );
         grid1.getView().refresh();*/
            Wtf.get('upfilewin').hide();
            Wtf.get('upfilewin').destroy();
        }
        return flagforgrid;
    },

    deleteButttonClick: function(groupid, docid, userid, count, mainTree, pcid,grid){
        Wtf.Ajax.request({
            url: "deleteAction.jsp",
            params: {
                DOCID: docid,
                GROUPID: groupid,
                COMMENT: "f",
                COUNT: count,
                PCID: pcid,
                PROJECTID: this.panelid
            },
            scope: this,
            success: function(result, req){
                if (eval('(' + result.responseText + ')')["res"] == 1) {
                    msgBoxShow(271, 1);
                } else {
                    var docids = eval('('+eval('(' + result.responseText + ')')["docids"]+')');
                    for(var i=0;i<docids.docid.length;i++){
                        var rec =grid.store.find("Id",docids.docid[i]);
                        if(rec>-1){
                            for(var j = 0;j<grid.store.getAt(rec).data['Tags'].split(',').length;j++)
                                mainTree.breakTree(grid.store.getAt(rec).data['Tags'].split(',')[j],mainTree.root);
                            grid.store.remove(grid.store.getAt(rec));
                            var openDoc = Wtf.getCmp("tabfcontent" + docids.docid[i]);
                            if(openDoc !== undefined)
                                openDoc.ownerCt.remove(openDoc, true);
                        }
                    }
                    grid.getView().refresh();
                    if(grid.store.getCount() == 0){
                        this.AdvanceSearchBtn.setDisabled(true);
                        this.quickSearchTF.setDisabled(true);
                    }
                    bHasChanged=true;
                    if(refreshDash.join().indexOf("all") == -1 && refreshDash.join().indexOf("doc") == -1)
                        refreshDash[refreshDash.length] = 'doc';
                }
            }
        });
    }//,

//    statusButttonClick: function(grid1, MyDocId, per, userid,groupid,pcid){
//        var form;
//        var win;
//        form = new Wtf.FormPanel({
//            id: 'perfrm',
//            frame: true,
//            minWidth: 385,
//            url: 'http://www.google.com',
//            method: 'POST',
//            onSubmit: MyokClicked,
//            items: [{
//                xtype: 'fieldset',
//                title: WtfGlobal.getLocaleText('lang.status.text'),
//                id: 'statusRadio',
//                autoHeight: true,
//                defaultType: 'radio',
//                labelWidth: 0,
//                layoutConfig: {
//                    labelSeparator: ''
//                },
//                items: [/*{
//                id: 'pending',
//                boxLabel: WtfGlobal.getLocaleText('pm.common.pending'),
//                name: 'statusgrp'
//            }, */{
//                    id: 'waiting',
//                    boxLabel: WtfGlobal.getLocaleText('pm.member.request.approval'),
//                    name: 'statusgrp'
//                }, {
//                    id: 'completed',
//                    boxLabel: WtfGlobal.getLocaleText('lang.completed.text'),
//                    name: 'statusgrp'
//                // checked: true
//                }, {
//                    id: 'draft',
//                    boxLabel: WtfGlobal.getLocaleText('pm.personalmessages.draft'),
//                    name: 'statusgrp'
//                }, {
//                    id: 'Nonestatus',
//                    boxLabel: WtfGlobal.getLocaleText('lang.none.text'),
//                    name: 'statusgrp'
//                }]
//            }]
//        });
//
//        var divstatus = document.createElement('div');
//        divstatus.id = 'formst';
//        statusDefault(per);
//        win = new Wtf.Window({
//            id: 'statusupwin',
//            title: WtfGlobal.getLocaleText('pm.project.document.filestatus'),
//            closable: true,
//            modal: true,
//            width: 400,
//            autoHeight: true,
//            plain: true,
//            iconCls: 'iconwin',
//            resizable: false,
//            layout: 'fit',
//            contentEl: divstatus,
//            buttons: [{
//                text: WtfGlobal.getLocaleText('lang.cancel.text'),
//                id: 'doccancel',
//                handler: MycancelClicked,
//                minWidth: 60,
//                cls: 'docbutton'
//            },{
//                text: WtfGlobal.getLocaleText('lang.OK.text'),
//                type: 'submit',
//                id: 'docok',
//                handler: MyokClicked,
//                minWidth: 60,
//                cls: 'docbutton'
//            }]
//        }).show();
//
//        form.render('formst');
//
//        function MyokClicked(){
//            var string = '';
//            if (Wtf.getCmp('waiting').getValue())
//                string = "Waiting";
//            else if (Wtf.getCmp('completed').getValue())
//                string = WtfGlobal.getLocaleText('lang.completed.text');
//            else if (Wtf.getCmp('draft').getValue())
//                string = WtfGlobal.getLocaleText('pm.personalmessages.draft');
//            else
//                string = WtfGlobal.getLocaleText('lang.none.text');
//            var xmlHttp, i = 0;
//            var response;
//            Wtf.Ajax.requestEx({
//                url: Wtf.req.doc + "perm/setStatus.jsp",
//                params: ({
//                    docid: MyDocId,
//                    status: string,
//                    groupid:groupid,
//                    pcid:pcid
//                //  url: url
//                })
//            }, this, function(result, req){
//                if (eval('(' + result + ')')["res"] == 0) {
//                    var rec = grid1.store.find("Id", MyDocId, 0, false, true);
//                    grid1.store.getAt(rec).set('Status', string);
//                    grid1.getView().refresh();
//                    Wtf.getCmp('statusupwin').close();
//                }
//                else {
//                    msgBoxShow([WtfGlobal.getLocaleText('pm.msg.ERROR'), "Please change the status again."], 1);
//                }
//            });
//        }
//
//        function MycancelClicked(){
//            Wtf.getCmp('statusupwin').close();
//        }
//
//        function statusDefault(per){
//            if (per == "Waiting")
//                Wtf.getCmp('waiting').checked = true;
//            else if (per == "Completed")
//                Wtf.getCmp('completed').checked = true;
//            else if (per == "Draft")
//                Wtf.getCmp('draft').checked = true;
//            else
//                Wtf.getCmp('Nonestatus').checked = true;
//        }
//    }
});
function getDocs(id){
    Wtf.getCmp(id).AddFiles();
}
