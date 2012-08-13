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
var PortalPersonalMessages = {};
PortalPersonalMessages.TopicStore = function(config){
    PortalPersonalMessages.TopicStore.superclass.constructor.call(this, {
        //remoteSort: true,
        sortInfo: {
            field: 'folder',
            direction: "DESC"
        },
        proxy: new Wtf.data.HttpProxy({
            url: Wtf.req.prt + 'getmail.jsp'
        }),
        
        reader: new Wtf.data.KwlJsonReader({
            idProperty: 'post_id',
            root: 'data',
            totalProperty: 'totalCount',
            remoteGroup: true,
            remoteSort: false
        
        }, Wtf.data.Record.create([{
            name: 'post_time',type: 'date',dateFormat: 'Y-m-j H:i:s'
        }, {
            name: 'flag'
        }, {
            name: 'post_id'
        }, {
            name: 'post_subject'
        }, {
            name: 'post_text'
        },{
            name: 'Attachment'
        },{
            name: 'deskSuperuser'
        }, {
            name: 'post_fullname'
        }, {
            name: 'poster_id'
        }, {
            name: 'readflag'
        }, {
            name: 'imgsrc'
        }, {
            name: 'senderid'
        }, {
            name: 'folder'
        }]))
    });
};

Wtf.extend(PortalPersonalMessages.TopicStore, Wtf.data.GroupingStore, {
    msgLmt: 15,
    loadForum: function(flag, mailflag, loginid){
        this.baseParams = {
            flag: flag,
            mailflag: mailflag,
            loginid: loginid
        };
        this.load({
            params: {
                start: 0,
                limit: (this.mailPageLimit && this.mailPageLimit.combo) ? (this.mailPageLimit.combo.getValue() || this.msgLmt) : (portalmail_mainPanel && portalmail_mainPanel.mailPageLimit) ? (portalmail_mainPanel.mailPageLimit.combo.getValue()) : this.msgLmt
            }
        });
    },
    
    loadSearch: function(searchtext, folder_id, mailflag, loginid, limit,currFolderId){
        this.baseParams = {
            searchtext: searchtext,
            folder_id: folder_id,
            mailflag: mailflag,
            loginid: loginid,
            flag:currFolderId
        };
        this.load({
            params: {
                start: 0,
                limit: limit || this.msgLmt
            }
        });
    },
    loadRefresh: function(flag, mailflag, loginid, cCursor,limit){
        this.baseParams = {
            flag: flag,
            mailflag: mailflag,
            loginid: loginid
        };
        this.load({
            params: {
                start: cCursor,
                limit: limit || this.msgLmt
            }
        });
    }
});
Wtf.PortalMailPanel= function(config){
    this.mailPageLimit=  new Wtf.common.pPageSize();
    this.dst = new PortalPersonalMessages.TopicStore({mailPageLimit:this.mailPageLimit});
    this.portalmail_sm1 = new Wtf.grid.CheckboxSelectionModel();
    this.portalmail_grid1 = new Wtf.grid.GridPanel({
        title: '',
        ds: this.dst,
        cm: new Wtf.grid.ColumnModel([this.portalmail_sm1, {
            header: WtfGlobal.getLocaleText('lang.subject.text'),
            width: 120,
            sortable: true,
            dataIndex: 'post_subject',
            renderer:this.subRenderer
        }, {
            header: WtfGlobal.getLocaleText('pm.personalmessages.from.text'),
            width: 120,
            sortable: true,
            dataIndex: 'post_fullname'
        }, {
            header: WtfGlobal.getLocaleText('pm.common.recieved'),
            renderer: WtfGlobal.userPrefDateRenderer,//Wtf.util.Format.dateRenderer('Y-m-d h:i a'),
            width: 115,
            sortable: true,
            dataIndex: 'post_time'
        } ,{
            header: WtfGlobal.getLocaleText('pm.common.flag'),
            sortable: true,
            width: 32,
            resizable:false,
            fixed:true,
            renderer: this.ImageReturn,
            dataIndex: 'flag'
        },{
            header: "folderid",
            width: 1,
            resizable:false,
            fixed:true,
            sortable: true,
            dataIndex: 'folder',
            groupable:true,
            hidden:true,
            groupRenderer:this.getfolderGroup

        }
        ]),
        loadMask: this.loadMask,
        sm: this.portalmail_sm1,
        id: 'grid123',
        border: false,
        view: new Wtf.grid.GroupingView({
            forceFit: true,
            showGroupName: false,
            enableGroupingMenu: false,
            hideGroupedColumn: false,
            emptyText: '<div class="emptyGridText">'+WtfGlobal.getLocaleText("pm.personalmessages.emptytext")+'</div>'
        }),
        bbar: new Wtf.PagingToolbar({
            id: 'draftspt',
            pageSize: 15,
            store: this.dst,
            displayInfo: true,
//            displayMsg: WtfGlobal.getLocaleText("pm.personalmessages.pagingtext"),
            emptyMsg: WtfGlobal.getLocaleText("pm.personalmessages.emptytext"),
            plugins: this.mailPageLimit
        })

    });
    this.MessagePanel1 = new Wtf.MessagePanel({
        id: "emails"
    });

    this.MessagePanel1.on("UpdateMailDstore", this.loadMailDstore,this);
    Wtf.PortalMailPanel.superclass.constructor.call(this, {activeTab: 0,
        id: 'tabmailtab_pmpanel',
        enableTabScroll: true,
        border: false,
        items: {
            id: 'tabmailtab_tab1',
            title: WtfGlobal.getLocaleText('pm.common.messages'),
            layout: 'border',
            items: [{
                region: 'center',
                collapsible: true,
                id: "tabmailtab_pmNorth",
                border: false,
                //bodyStyle:'border-bottom:1px solid #99BBE8 !important;',
                split: true,
                layout: 'fit',
                items:[this.portalmail_grid1],
                tbar: this.createMailFunctions()
            }, {
                region: 'south',
                id: "tabmailtab_pmCenter",
                //title:WtfGlobal.getLocaleText('pm.project.member.details'),
                collapsible: true,
                border: false,
                split: true,
                height: 250,
                layout: 'fit',
                items: this.MessagePanel1
            }]
        }
    });
    dojo.cometd.subscribe("/"+loginid+"/inbox", this, "inboxPublishHandler");
    this.on("tabchange", function(p, t){
        t.doLayout();
        this.dst.reload();
    },this)
  /*  Wtf.getCmp("tabmailtab_pmNorth").add(portalmail_grid1);
    Wtf.getCmp("tabmailtab_pmpanel").doLayout();
    Wtf.getCmp("tabmailtab").doLayout();
    Wtf.getCmp("folderview").displayMailWindow();*/
//    Wtf.EventManager.addListener("mailsearchtextbox", 'keyup', this.txtsearchKeyPress, this);
//    Wtf.EventManager.addListener("mailsearchtextbox", 'keyup', this.txtsearchKeyPress, this);
//      this.el.on("keyup", this.onKeyUp,  this, {buffer:50});
//    this.portalmail_grid1.on('rowcontextmenu', this.onMailGridContextmenu,this);
//    this.portalmail_grid1.on('rowdblclick', this.gridrowDoubleClick,this);
//    this.portalmail_grid1.on("cellclick", this.onClickHandle1,this);
//    this.portalmail_grid1.getSelectionModel().on("beforerowselect", this.beforeRowselect,this);
//    this.portalmail_grid1.getSelectionModel().on('rowselect',rowSelectionChange,this);
//    this.portalmail_grid1.getSelectionModel().on('rowdeselect',this.rowDeselect);
//    this.portalmail_grid1.on('sortchange',this.sortchange);
};

Wtf.extend(Wtf.PortalMailPanel, Wtf.TabPanel, {
    leftTree:null,
    highlightRow: function(){
        if(globalPostid){
          if(globalPostid !== ""){
                var row = this.portalmail_grid1.getStore().find('post_id', globalPostid);
                if(typeof row == 'number' && row != -1){
                    this.portalmail_grid1.getSelectionModel().selectRow(row);
                    this.portalmail_grid1.fireEvent('rowdblclick', this.portalmail_grid1, row);
                }
                globalPostid = "";
            }
        }
    },
    afterRender:function(){
        hideMainLoadMask();
        this.portalmail_grid1.loadMask = new Wtf.LoadMask(this.el.dom, Wtf.apply(this.id));
        this.portalmail_grid1.loadMask.msg = WtfGlobal.getLocaleText("pm.personalmessages.loadingtext")+'...'; 
        Wtf.PortalMailPanel.superclass.afterRender.call(this);
//        Wtf.EventManager.addListener("mailsearchtextbox", 'keyup', this.txtsearchKeyPress, this);
        Wtf.getCmp("mailsearchtextbox").on("render",function(textfield){
            textfield.el.on("keyup", this.txtsearchKeyPress,this);
        },this)//.el.on("keyup", this.txtsearchKeyPress,  this, {buffer:50});
        Wtf.getCmp("folderview").displayMailWindow();
        this.portalmail_grid1.on('rowcontextmenu', this.onMailGridContextmenu,this);
        this.portalmail_grid1.on('rowdblclick', this.gridrowDoubleClick,this);
        this.portalmail_grid1.on("cellclick", this.onClickHandle1,this);
        this.portalmail_grid1.getSelectionModel().on("beforerowselect", this.beforeRowselect,this);
        this.portalmail_grid1.getSelectionModel().on('rowselect',this.rowSelectionChange,this);
        this.portalmail_grid1.getSelectionModel().on('rowdeselect',this.rowDeselect,this);
        this.portalmail_grid1.on('sortchange',this.sortchange,this);
        Wtf.getCmp('tabpmessage').on('activate', this.highlightRow, this);
    },
    dtaskMail:new Wtf.util.DelayedTask(this.searchmails),
    txtsearchKeyPress:function(e){
        this.txt = e.getTarget().value;
        
        this.dtaskMail.cancel();
        this.dtaskMail.delay(500, this.searchmails,this);
    },
    subRenderer:function(value){
        return WtfGlobal.URLDecode(decodeURIComponent(value));
    },
    dateRenderer:function (value){
        return value.format(WtfGlobal.getDateFormat());
         //value;
    },
    ImageReturn:function (data){
        if (data) 
            return "<img class='starImgDiv' star=0 src='../../images/FlagRed.gif' wtf:qtip="+WtfGlobal.getLocaleText("pm.help.flagit")+"></img>";
        else 
            return "<img class='starImgDiv' star=1 src='../../images/FlagGrey.gif' wtf:qtip="+WtfGlobal.getLocaleText("pm.help.unflagit")+"></img>";
    },
    inboxPublishHandler:function(){
        if(this.portalmail_folderid==0){
            var temp=eval('('+msg.data.data+')');
            var temp1=Wtf.decode(temp.data[0]).data;
            var ds = Wtf.getCmp('grid123').getStore();
            ds.reload();
           /* var m=new mailMsg({
                post_time:temp1[0].post_time,
                flag:temp1[0].flag,
                post_id:temp1[0].post_id,
                post_subject:temp1[0].post_subject,
                post_text:temp1[0].post_text,
                poster_id:temp1[0].poster_id,
                readflag:temp1[0].readflag,
                imgsrc:temp1[0].imgsrc,
                senderid:temp1[0].senderid     

            });
            ds.insert(0,m);*/
        }
    },
    loadMailDstore:function(details,Attachment,id){
        for (var i = 0; i < this.dst.getCount(); i++) {
            var record = this.dst.getAt(i);
            var recId = record.data['post_id'];
            if (recId == id) {
                if(details==""&& Attachment==""){
    //                record.set('post_text', 'Wat');
                    details = " ";
                    Attachment = " ";
                }
                //data['readflag']
                if(record.data.readflag==false){
                    record.set('readflag', true);
                }
                record.set('post_text', details);
                record.set('Attachment', Attachment);
                break;
            }
        }
        if(typeof globalPostid !== 'undefined' && globalPostid !== '')
            this.highlightRow();
    },
    
   inboxPublishHandler:function(msg) {
        if(this.portalmail_folderid==0){
            var temp=eval('('+msg.data.data+')');
            var temp1=Wtf.decode(temp.data[0]).data;
            var ds = this.portalmail_grid1.getStore();
            var l=this.mailPageLimit.pagingToolbar.pageSize;
            ds.reload({params:
                    {start:0,
                    limit:l}});
            bHasChanged=true;
            if(refreshDash.join().indexOf("all") == -1 && refreshDash.join().indexOf("pmg") == -1)
                refreshDash[refreshDash.length] = 'pmg';
           /* var m=new mailMsg({
                post_time:temp1[0].post_time,
                flag:temp1[0].flag,
                post_id:temp1[0].post_id,
                post_subject:temp1[0].post_subject,
                post_text:temp1[0].post_text,
                poster_id:temp1[0].poster_id,
                readflag:temp1[0].readflag,
                imgsrc:temp1[0].imgsrc,
                senderid:temp1[0].senderid     

            });
            ds.insert(0,m);*/
        }

    },
    loadingDisplayNo:function(str){
        return '<div style="float: left; width:100%"><div style="float: left;">' + str + '</div><div style="float: right; color: rgb(0,0,0); margin-left: 20px; font-weight: normal;">'+WtfGlobal.getLocaleText("pm.personalmessages.emptytext")+'</div></div>';
    },
    searchmails:function(){
        this.enablemailtoolbarbtns();
        var searchstring = encodeURIComponent(document.getElementById('mailsearchtextbox').value.trim());
        if(searchstring.length > 0){
        //portalmail_grid1.setTitle(loadingDisplay(portalmail_titleflag));
            this.portalmail_grid1.setTitle("Search results");
            var view = '';
            var folder_id = this.portalmail_folderid;
            this.dst.loadSearch(searchstring, folder_id, "searchmails", loginid,this.mailPageLimit.combo.getValue(),this.portalmail_folderid);

            this.dst.on("loadexception", function exp(){
                //portalmail_grid1.setTitle(portalmail_titleflag);
                msgBoxShow(4, 1);
                //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occurred while connecting to the server');
            },this);

            this.portalmail_grid1.store = this.dst;
            view = this.portalmail_grid1.getView();
//            view.refresh();
            this.dst.on("load", function(a, b, c){
                if (b.length == 0) {
                    this.portalmail_grid1.setTitle(this.loadingDisplayNo(this.portalmail_titleflag));
                    this.MessagePanel1.setData1("", "", "", "", "../../images/blank.png");
                }
                else {
//                    view.refresh();
                    this.portalmail_grid1.getStore().groupBy("folder");
                    //portalmail_grid1.setTitle(portalmail_titleflag); 
                }
            },this)
            searchFlag=true;
        }
        else{
            if(searchFlag){
                this.dst.loadForum(this.portalmail_folderid, "fetch", loginid);
                searchFlag=false;
                this.dst.on("load", function(a, b, c){
                        var view = this.portalmail_grid1.getView();
//                        view.refresh();
                        this.portalmail_grid1.store.clearGrouping();
                        if (this.portalmail_folderid == '0') {   
                            for (var i = 0; i < a.getCount(); i++) {
                                if(b[i].data['readflag']==false){
                                    view.getCell(i, 1).firstChild.style.fontWeight = "bold";
                                    view.getCell(i,  2).firstChild.style.fontWeight = "bold";
                                    view.getCell(i,  3).firstChild.style.fontWeight = "bold";
                            }
                        }        
                    }
                },this)
            }
        }
    },
    createMailFunctions:function(){
        this.portalmail_actionMenu = new Wtf.menu.Menu({
            id: 'portalmail_actionMenu',
            items: [{
                text: WtfGlobal.getLocaleText('pm.personalmessages.inbox'),
                id: "0",
                icon: "../../images/inbox.png",
                tooltip: {
                    text : WtfGlobal.getLocaleText('pm.Help.pmextra') + '\'Inbox\''
                }
            }/*,{
                text: WtfGlobal.getLocaleText('pm.personalmessages.drafts'),
                id: "3",
                icon: "../../images/mail_generic.png"
            }*/,new Wtf.menu.Separator({})]
        });
        this.portalmail_actionMenu.on('itemclick', this.folderClick,this);
        return ([new Wtf.Action({
                //TODO: temp location for button .. suggest location for the same [charu]
                text: WtfGlobal.getLocaleText('pm.personalmessages.compose'),
                id: 'compMail',
                scope:this,
                handler: function(){
                    this.handleCompose()
                },
                tooltip: {
                    text: WtfGlobal.getLocaleText('pm.Help.pmcompose')
                },
                iconCls: 'pwnd compose'
            }), new Wtf.Action({
                text: WtfGlobal.getLocaleText('pm.personalmessages.reply'),
                id: 'btnreplyto',
                scope:this,
                handler: function(){
                    this.createReplyWindow()
                },
                tooltip: {
                    text: WtfGlobal.getLocaleText('pm.Help.pmreply')
                },
                iconCls: 'pwnd outbox'
            }), new Wtf.Action({
                text: WtfGlobal.getLocaleText('lang.delete.text'),
                handler: this.DeleteMails,
                scope:this,
                id: 'btndelete',
                tooltip: {
                    text: WtfGlobal.getLocaleText('pm.Help.pmdelete')
                },
                iconCls: 'pwnd deliconwt'
            }), moveto = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText('pm.personalmessages.moveto'),
                iconCls: 'pwnd sendmsg',
                scope:this,
                tooltip: {
                    text: WtfGlobal.getLocaleText('pm.Help.pmmoveto')
                },
                id: 'MoveFolders',
                menu: this.portalmail_actionMenu
            }), moreactions = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText('pm.personalmessages.moreactions'),
                iconCls: 'dpwnd settings',
                scope:this,
                tooltip: {
                    text: WtfGlobal.getLocaleText('pm.Help.pmmoreactions')
                },
                id: 'btnmoreactions',
                menu: {
                    items: [{
                        text: WtfGlobal.getLocaleText('pm.personalmessages.addflag'),
                        scope:this,
                        handler: this.addstarClick,
                        icon: "../../images/FlagRed16.png"
                    }, {
                        text: WtfGlobal.getLocaleText('pm.personalmessages.removeflag'),
                        scope:this,
                        handler: this.removestarClick,
                        icon: "../../images/FlagGrey16.png"
                    }]
                }
            }), /*/"Search: ",*/ new Wtf.form.TextField({
                id: "mailsearchtextbox",
                emptyText: WtfGlobal.getLocaleText('pm.common.quicksearch'),
                width: 150,
                height: 19
            })/*, new Wtf.Toolbar.Button({
                text: '',
                id: 'btnsearch',
                iconCls: 'pwnd btnMailSearch',
                handler: searchmails,
                ctCls: 'searchrightbutton',
                tooltip: {
                    title: WtfGlobal.getLocaleText('pm.search.messages'),
                    text: WtfGlobal.getLocaleText('pm.search.meassageall')
                }
            })*/]);
    },
    handleCompose:function(){
        var composePanel = Wtf.getCmp("composeMessagePanel");
        if(composePanel === undefined){
            composePanel = new Wtf.ReplyWindow({
                uLabelName: 'To',
                bLabelName: 'Subject',
                uLabel: WtfGlobal.getLocaleText('pm.common.TO'),
                bLabel: WtfGlobal.getLocaleText('lang.subject.text'),
                tdisabled: false,
                replytoId: '-1',
                userId: loginid,
                groupId: "",
                id: 'composeMessagePanel',
                title: WtfGlobal.getLocaleText('pm.personalmessages.compose'),
                closable: true,
                firstReply: "",
                uFieldValue: "",
                bFieldValue: "",
                type: "Mail",
                sendFlag: "newmsg",
                composeMail: 1,
                mailDS: this.portalmail_grid1.getStore()
            });
            composePanel.insertStore.on("load", this.handleInsertMail,this);
            this.add(composePanel);
        }
//        this.add(composePanel).show();
        this.setActiveTab(composePanel);
//        composePanel.doLayout();
    },
    getfolderGroup:function(value){
        var resultHead=WtfGlobal.getLocaleText('lang.none.text');
        switch(value){
             case "0":
                resultHead=WtfGlobal.getLocaleText('pm.personalmessages.inbox');
                break;
            case "1":
                resultHead=WtfGlobal.getLocaleText('pm.personalmessage.sentmail.text');
                break;
            case "2":
                resultHead = WtfGlobal.getLocaleText('pm.common.deleted');
                break;
            case "3":
                resultHead = WtfGlobal.getLocaleText('pm.personalmessages.drafts');
                break;
            default :
                var node = Wtf.getCmp("folderview").getNodeById(value);
                if(node){
                    resultHead= node.text;
                 }
                 break;
            }
        return resultHead;
    },
    handleInsertMail:function(obj, rec, opt){
        if(rec[0]){
            if (rec[0].data['Success'].match('Success')) {
                msgBoxShow(141, 0);
                //Wtf.Msg.alert('Message Sent', WtfGlobal.getLocaleText('pm.msg.141'));
                this.panelObj.handleClose();
            }
            else if (rec[0].data['Success'].match('Fail')) {
                    msgBoxShow(142, 1);
                    //Wtf.Msg.alert('Error', 'Error occurred while sending message.');
            }  
            else if (rec[0].data['Success'].match('Draft')) {
                    msgBoxShow(147, 0);
                    //Wtf.Msg.alert('Saved', 'Message saved to drafts successfully.');
                    this.panelObj.handleClose();
            }  
            else if (rec[0].data['Success'].match('userfail')) {
                    msgBoxShow([WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText({key: "pm.personalmessages.errorinvaliduser", params:[rec[0].data['Subject']]})], 1);
                    //Wtf.Msg.alert('Delivery Failure', 'Message to user '+rec[0].data['Subject'] +' is invalid.');
            }  
         }   
         this.dst.reload();
       // Wtf.getCmp('winMsgSend').close();
    },
    onMailGridContextmenu:function(grid, rowindex, e){
        this.portalmail_sm1.selectRow(rowindex);
        var menu = null;
        
        if (!menu) {
            menu = new Wtf.menu.Menu({
                id: 'context12',
                items: [{
                    text: WtfGlobal.getLocaleText('pm.personalmessages.reply'),
                    id: 'cntxbtnreplyto',
                    scope:this,
                    handler: this.createReplyWindow,
                    iconCls: 'pwnd outboxCx',
                    tooltip: {
                        text : WtfGlobal.getLocaleText('pm.Help.pmreply')
                    }
                }, {
                    text: WtfGlobal.getLocaleText('lang.delete.text'),
                    handler: this.DeleteMails,
                    id: 'cntxbtndelete',
                    scope:this,
                    iconCls: 'pwnd delicon',
                    tooltip: {
                        text : WtfGlobal.getLocaleText('pm.Help.pmdelete')
                    }
                }, {
                    text: WtfGlobal.getLocaleText('pm.personalmessage.restore.text'),
                    iconCls: 'pwnd sendmsgwt',
                    id: 'cntxbtnrestore',
                    scope:this,
                    handler: this.handleContextRestore,
                    tooltip: {
                        text : WtfGlobal.getLocaleText('pm.Help.pmrestore')
                    }
                }, {
                    text: WtfGlobal.getLocaleText('pm.personalmessages.moveto'),
                    iconCls: 'pwnd sendmsgwt',
                    id: 'cntxbtnmoveto',
                    scope:this,
                    // <-- submenu by nested config object
                    menu: this.portalmail_actionMenu,
                    tooltip: {
                        text : WtfGlobal.getLocaleText('pm.Help.pmmoveto')
                    }
                }, {
                    text: WtfGlobal.getLocaleText('pm.personalmessages.moreactions'),
                    iconCls: 'dpwnd settingswt',
                    id: 'cntxbtnmoreactions',
                    // <-- submenu by nested config object
                    tooltip: {
                        text : WtfGlobal.getLocaleText('pm.Help.pmmoreactions')
                    },
                    menu: {
                        items: [{
                            text: WtfGlobal.getLocaleText('pm.personalmessages.addflag'),
                            scope:this,
                            handler: this.addstarClick,
                            icon: "../../images/FlagRed16.png"
                        }, {
                            text: WtfGlobal.getLocaleText('pm.personalmessages.removeflag'),
                            scope:this,
                            handler: this.removestarClick,
                            icon: "../../images/FlagGrey16.png"
                        }]
                    }
                }]
            })
        }
        menu.showAt(e.getXY());
        e.preventDefault();

        var fid = this.portalmail_folderid;
        if(fid == 4)
            this.updateCntxButtonStatus(this.dst.getAt(rowindex).get("folder"));
        else
            this.updateCntxButtonStatus(fid);
    },
    updateCntxButtonStatus:function(folderid){
        var replyBtn = Wtf.getCmp('cntxbtnreplyto');
        var delBtn = Wtf.getCmp('cntxbtndelete');
        var moveBtn = Wtf.getCmp('cntxbtnmoveto');
        var moreAct = Wtf.getCmp('cntxbtnmoreactions');
        var restBtn = Wtf.getCmp('cntxbtnrestore');
        var zeroBtn =  Wtf.getCmp('0');
        replyBtn.disable();
        delBtn.disable();
        moveBtn.disable();
        moreAct.disable();
        restBtn.disable();
        zeroBtn.disable();
        if (folderid == '0') {
            replyBtn.enable();
            delBtn.enable();
            moveBtn.enable();
            moreAct.enable();
        }
        else 
            if (folderid == '2') {
                replyBtn.disable();
                delBtn.enable();
                restBtn.enable();
            }
            else 
                if (folderid == '4') {
                    moreAct.enable();
                }
                else 
                    if (folderid == '1') {
                        delBtn.enable();
                        moreAct.enable();
                    }else if(folderid == '3'){
                        delBtn.enable();
                        moreAct.enable();
                    }
                    else {
                        replyBtn.enable();
                        moreAct.enable();
                        delBtn.enable();
                        moveBtn.enable();
                        zeroBtn.enable();
                    }
    },
    folderClick:function(item, e){
        var folderid = item.id;
        //DOC: portalmail_folderid - is msg source folder id
        if(!(this.portalmail_folderid==folderid)){
            if(this.portalmail_sm1.getSelections().length>0){
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText("pm.msg.342"), function(btn){
                    if (btn == 'yes') {
                        this.UpdateFolderID(folderid);
                    }
                },this);
            } else {
                msgBoxShow(145, 1);
                 //Wtf.Msg.alert('Alert', 'Please select a message');
            }
        } else {
            msgBoxShow(146, 1);
             //Wtf.Msg.alert('Alert', 'Source and destination folders are same');
        }
    },
    createReplyWindow:function(){
        var selMail = this.portalmail_sm1.getSelections();
        if(selMail.length==1){
        //        dst.getAt(rowIndex).get('deskSuperuser')
            var record = selMail[0];
            if(record.data.deskSuperuser=='true'){
                msgBoxShow(173, 1);
                return;
            }
            var replyObj = Wtf.getCmp(record.data['post_id'] + "_replyPanel");
            var posttime =WtfGlobal.userPrefDateRenderer(record.data['post_time']);
            if(replyObj === undefined){
                replyObj= new Wtf.ReplyWindow({
                    uLabelName: 'Reply To',
                    bLabelName: 'Subject',
                    uLabel: WtfGlobal.getLocaleText('pm.personalmessages.replyto'),
                    bLabel: WtfGlobal.getLocaleText('lang.subject.text'),
                    tdisabled: true,
                    title:WtfGlobal.getLocaleText('pm.personalmessages.reply'),
    //                id:'replywin'+Math.random(),
                    id: record.data['post_id'] + "_replyPanel",
                    closable: true,
                    layout: 'fit',
                    replytoId: record.data['post_id'],
                    userId: loginid,
                    groupId: "",
                    firstReply: "",
                    uFieldValue: record.data['poster_id'],
                    details : "<br><br><br><br><br><br><br><br>"+WtfGlobal.getLocaleText("pm.mail.original.text")+"<br><br><br>"+WtfGlobal.getLocaleText("lang.on.text")+" "+posttime+", "+record.data['post_fullname']+" "+WtfGlobal.getLocaleText("lang.wrote.text") +" : <br><br>"+ WtfGlobal.URLDecode(decodeURIComponent(record.data['post_text'])),
                    bFieldValue: "Re: "+ WtfGlobal.URLDecode(decodeURIComponent(record.data['post_subject'])),
                    type: "Mail",
                    sendFlag:"reply",
                    fid:this.portalmail_folderid,
                    composeMail:5
                });
                replyObj.insertStore.on("load", this.handleInsertMail,this);
                this.add(replyObj);
            }
            this.setActiveTab(replyObj);
           // wind.show();
            
        } else {
            msgBoxShow(145, 1);
             //Wtf.Msg.alert('Alert', 'Please select a message');
        }
   
    },
    DeleteMails:function(){
        if(this.portalmail_sm1.getSelections().length>0){
            var delstr=WtfGlobal.getLocaleText('pm.msg.350');
            if(this.portalmail_folderid==2)
                delstr=WtfGlobal.getLocaleText('pm.msg.345');
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), delstr, function(btn){
                if (btn == 'yes') {
                    this.MessagePanel1.setData1("", "", "", "", "../../images/blank.png");
                    this.UpdateFolderID(2);
                }
            }, this);
        } else {         
             msgBoxShow(145, 1);
             //Wtf.Msg.alert('Alert', 'Please select a message');
        }
    },
    UpdateFolderID:function(folder_id){
           var last_folder_id = this.portalmail_folderid;

            if(this.portalmail_sm1.getSelections().length>0){
                var ds = this.portalmail_grid1.getStore();
                var selArray = Array();
                selArray = this.portalmail_sm1.getSelections();
                var jsonData = "{data:[";
                for (i = 0; i < selArray.length; i++) {
                    var rowobj = selArray[i];
                    if(last_folder_id == '4')
                        last_folder_id = rowobj.get('folder');
                    jsonData += "{'post_id':'" + encodeURIComponent(rowobj.get('post_id')) + "'},";
//                    if(folder_id=='2'){
//                        if(Wtf.getCmp("emailsTab" + rowobj.get('post_id'))!=null){
//    //                         Wtf.getCmp("emailsTab" + rowobj.get('post_id')).ownerCt.remove(Wtf.getCmp("emailsTab" + rowobj.get('post_id')));
//                           this.remove(Wtf.getCmp("emailsTab" + rowobj.get('post_id')));
//                            //Wtf.getCmp("emailsTab" + rowobj.get('post_id')).destroy();
//                        }
//                    }
                }
                jsonData = jsonData.substring(0, jsonData.length - 1) + "]}";
                Wtf.Ajax.requestEx({
                    method: 'POST',
                    url: Wtf.req.prt + 'getmail.jsp',
                    params: {
                        mailflag: 'movemails',
                        last_folder_id: last_folder_id,
                        dest_folder_id: folder_id,
                        post_id: jsonData
                    }
                }, this, function(result, req){
                    var nodeobj = eval("(" + result + ")").data;
                    var storeobj = this.portalmail_grid1.getStore();
                    var l=this.mailPageLimit.pagingToolbar.pageSize;
                    var start=storeobj.lastOptions.params.start - l;
                    start = start <= 0 ? 0 : start;
                    if(selArray.length == l || selArray.length == storeobj.getCount()){
                        storeobj.reload({
                            params:{
                                start:start,
                                limit:l
                            }
                        });
                    } else {
                        storeobj.reload();
                    }
                    if(folder_id == 2){
                        for(var cnt = 0; cnt < nodeobj.length; cnt++){
                            var pid = nodeobj[cnt].post_id;
                            if(Wtf.getCmp("emailsTab" + pid)!=null){
                                this.remove(Wtf.getCmp("emailsTab" + pid));
                            }if(Wtf.getCmp(pid + "_replyPanel") !== null){
                                this.remove(Wtf.getCmp(pid + "_replyPanel"));
                            }
                        }
                    }
                    // Wtf.Msg.alert('Moved', nodeobj.data.length + ' messages have been moved successfully.');
                    this.MessagePanel1.setData1("", "", "", "", "../../images/blank.png");
                    bHasChanged=true;
                    if(refreshDash.join().indexOf("all") == -1 && refreshDash.join().indexOf("pmg") == -1)
                        refreshDash[refreshDash.length] = 'pmg';
                }, function(result, req){
                });
    //                Wtf.Ajax.request({
    //                    method: 'POST',
    //                    url: Wtf.req.prt + 'getmail.jsp',
    //                    params: ({
    //                        mailflag: 'movemails',
    //                        last_folder_id: last_folder_id,
    //                        dest_folder_id: folder_id,
    //                        post_id: jsonData
    //                    }),
    //                    scope: this,
    //                    success: function(result, b){
    //                        var nodeobj = eval("(" + result.responseText + ")");
    //                        var storeobj = Wtf.getCmp('grid123').getStore();
    //                        for (var j = 0; j < nodeobj.data.length; j++) {
    //                            storeobj.remove(ds.getAt(ds.find('post_id', nodeobj.data[j].post_id)));
    //                        }
    //                       // Wtf.Msg.alert('Moved', nodeobj.data.length + ' messages have been moved successfully.');
    //                       MessagePanel1.setData1("", "", "", "", "../../images/blank.png");
    //                    },
    //                    failure: function(){
    //                    }
    //                });           
            } else {
                 msgBoxShow(145, 1);
                 //Wtf.Msg.alert('Alert', 'Please select a message');
            }            
    },
    handleContextRestore:function(){
        this.RestoreMsg(this.portalmail_sm1.getSelected().data['post_id']);
    },
    RestoreMsg:function(postid){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.325'), function(btn){
            if (btn == 'yes') {
                var ds = this.portalmail_grid1.getStore();
                Wtf.Ajax.requestEx({
                        method: 'POST',
                        url: Wtf.req.prt + 'getmail.jsp',
                        params: {
                            mailflag: 'restoremsg',
                            post_id: postid
                        }
                }, this, function(result, req){
                    if (postid == result) {
                        msgBoxShow(149, 0);
                        if(Wtf.getCmp("emailsTab" + postid)!=null){
                            this.remove(Wtf.getCmp("emailsTab" + postid));
                        } else {
                            var storeobj = this.portalmail_grid1.getStore();
                            var l=this.mailPageLimit.pagingToolbar.pageSize;
                            var start=storeobj.lastOptions.params.start - l;
                            start = start <= 0 ? 0 : start;
                            if(1 == storeobj.getCount()){
                                storeobj.reload({
                                    params:{
                                        start:start,
                                        limit:l
                                    }
                                });
                            } else {
                                storeobj.reload();
                            }
                        }
                        //Wtf.Msg.alert('Restored', WtfGlobal.getLocaleText('pm.msg.149'));
                    }
                    else {
                        msgBoxShow(4, 1);
                        //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occurred while connecting to the server');
                    }
                }, function(result, req){
                    msgBoxShow(4, 1);
                    //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occurred while connecting to the server');
                });
            }
        }, this);
    },

    deleteMsgForever: function(postid){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.345'), function(btn){
        if (btn == 'yes') {
            var ds = this.portalmail_grid1.getStore();
             Wtf.Ajax.requestEx({
                            method: 'POST',
                             url: Wtf.req.prt + 'getmail.jsp',
                            params: {
                                        mailflag: 'deleteforever',
                                        post_id: postid
                                    }
                        }, this, function(result, req){
                            if (postid == result) {
                                ds.remove(ds.getAt(ds.find('post_id', postid)));
                                msgBoxShow(148, 0);
                                this.remove(this.activeTab);
                                //Wtf.Msg.alert('Deleted', 'Message has been deleted successfully.');
                            }
                            else {
                                msgBoxShow(4, 1);
                                //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occurred while connecting to the server');
                            }
                        }, function(result, req){
                            msgBoxShow(4, 1);
                             //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occurred while connecting to the server');
                        });
        }
    }, this);

    },

    addstarClick:function(){
        this.handleStarChange(true);
    },
    removestarClick:function(){
        this.handleStarChange(false);
    },
    changeStar:function(flag,jsonData,row,col){
        Wtf.Ajax.requestEx({
                        method: 'GET',
                        url: Wtf.req.prt + 'getmail.jsp',
                        params: {
                        mailflag: 'starchange',
                        post_id: jsonData,
                        flag: flag
                    }
                    }, this, function(result, req){
                        var rowArr = row.toString().split(",");
                        for(var i=0;i<rowArr.length;i++){
                            targetImg = this.portalmail_grid1.getView().getCell(rowArr[i],col).firstChild.firstChild;
                            if(flag==true){
                                targetImg.src = "../../images/FlagRed.gif";
                                targetImg.setAttribute("star", 0);
                            }else{
                                targetImg.src = "../../images/FlagGrey.gif";
                                targetImg.setAttribute("star", 1);
                            }
                        }
                        bHasChanged=true;
                        if(refreshDash.join().indexOf("all") == -1 && refreshDash.join().indexOf("pmg") == -1)
                            refreshDash[refreshDash.length] = 'pmg';
                        
                    }, function(result, req){
                 });
    },
    handleStarChange:function(flag){
        if(this.portalmail_sm1.getSelections().length>0){   
            var grd = this.portalmail_grid1;
            var bt = grd.getBottomToolbar();
            var cCursor = bt.cursor;

            var flag1 = this.portalmail_folderid;
            var title = this.portalmail_titleflag;
            //portalmail_grid1.setTitle(loadingDisplay(portalmail_titleflag));

            var selArray = Array();
            selArray = this.portalmail_sm1.getSelections();
            var num="";
            var jsonData = "{\"data\":[";
            for (i = 0; i < selArray.length; i++) {
                var rowobj = selArray[i];
                jsonData += "{\"post_id\":\"" + encodeURIComponent(rowobj.get('post_id')) + "\"},";
                rowobj.set('flag',flag);
                num += this.dst.find('post_id', rowobj.get('post_id'))+",";
            }
            jsonData = jsonData.substring(0, jsonData.length - 1) + "]}";
            num = num.substring(0,num.length-1);
            this.changeStar(flag,jsonData,num,4);
//            Wtf.Ajax.requestEx({
//                method: 'POST',
//                url: Wtf.req.prt + 'getmail.jsp',
//                params: {
//                    mailflag: 'starchange',
//                    flag: flag,
//                    post_id: jsonData
//                }
//            }, this, function(result, req){
//                 this.dst.loadRefresh(flag1, "fetch", loginid, cCursor);
//                 this.dst.on("loadexception", function exp(){
//                        //portalmail_grid1.setTitle(portalmail_titleflag);
//                        msgBoxShow(4, 1);
//                        //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occurred while connecting to the server');
//                    });
//
//                    this.portalmail_grid1.store = this.dst;
//                    view = this.portalmail_grid1.getView();
//                    view.refresh();
//                   /* dst.on("load", function(a, b, c){
//                        if (b.length == 0) {
//                            this.portalmail_grid1.setTitle(loadingDisplayNo(portalmail_titleflag));
//                        }
//                        else {
//                            //this.portalmail_grid1.setTitle(portalmail_titleflag);
//                            view.refresh();
//                            this.portalmail_grid1.store.clearGrouping();
//                        }
//                    })*/                                
//                    this.MessagePanel1.setData1("", "", "", "", "../../images/blank.png");
//            }, function(result, req){
//                msgBoxShow(4, 1);
//                //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occurred while connecting to the server');
//            });
         } else {
             msgBoxShow(145, 1);
             //Wtf.Msg.alert('Alert', 'Please select a message');
         }
    },
    
    gridrowDoubleClick:function(obj, rowIndex, e){
        var dRec = obj.getStore().getAt(rowIndex);
        var ds = this.portalmail_grid1.getStore();
        var postid = ds.getAt(rowIndex).get('post_id');
        this.postid = postid;
        var postSub = WtfGlobal.URLDecode(decodeURIComponent(ds.getAt(rowIndex).get('post_subject')));
        this.portalmail_folderid = ds.getAt(rowIndex).get('folder');
        if(postSub=="")
            postSub="[No Subject]";
        var tabid = "emailsTab" + postid;
        var tab = this.getComponent(tabid);
        if (tab) {
            this.setActiveTab(tab);
        }
        else {
            var MessagePanel2 = new Wtf.MessagePanel({
                id: "emails" + postid
            });
            if(this.portalmail_folderid == "3"){
                var rObj = Wtf.getCmp(dRec.data['post_id'] + "_draftPanel");
                if(rObj === undefined){
                    rObj = new Wtf.ReplyWindow({
                        uLabelName: 'To',
                        bLabelName: 'Subject',
                        uLabel: WtfGlobal.getLocaleText('pm.common.TO'),
                        bLabel: WtfGlobal.getLocaleText('lang.subject.text'),
                        title:WtfGlobal.getLocaleText('pm.personalmessages.draft'),
                        tabWidth:150,
                        closable:true,
                        tdisabled: false,
                        //replytoId: portalmail_sm1.getSelected().data['poster_id'],
                        replytoId:'-1',
                        id: dRec.data['post_id'] + "_draftPanel",
                        userId: loginid,
                        groupId: "",
                        firstReply: "",
                        uFieldValue: (this.portalmail_sm1.getSelected().data['poster_id']==loginname) ? "" : this.portalmail_sm1.getSelected().data['poster_id'],
                        bFieldValue: WtfGlobal.URLDecode(decodeURIComponent(this.portalmail_sm1.getSelected().data['post_subject'])),
                        type: "Mail",
                        sendFlag: "newmsg",
                        composeMail:1,
                        fid:this.portalmail_folderid,
                        details:WtfGlobal.URLDecode(decodeURIComponent(this.portalmail_sm1.getSelected().data['post_text'])),
                        postid:this.postid
                    });
                    rObj.insertStore.on("loadsuccess", this.handleInsertMail,this);
                    this.add(rObj);
                }
               // wind.show();
                this.setActiveTab(rObj);
            }
            else{
                this.add({
                    id: "emailsTab" + postid,
                    title: Wtf.util.Format.ellipsis(postSub, 50),
                    closable: true,
                    layout: 'fit',
                    tbar: this.createMailToolbar(postid),
                    items: MessagePanel2
                }).show();
                var posttime = WtfGlobal.userPrefDateRenderer(ds.getAt(rowIndex).get('post_time'));
                MessagePanel2.setData1("", "", "", '<div class="loading-indicator">&#160;'+WtfGlobal.getLocaleText('pm.loading.text')+'...</div>', "");
                MessagePanel2.setData(ds.getAt(rowIndex).get('post_subject'), ds.getAt(rowIndex).get('post_fullname'), posttime, ds.getAt(rowIndex).get('imgsrc'),ds.getAt(rowIndex).get('senderid'),this.dst.getAt(rowIndex).get('deskSuperuser'));
                if (this.portalmail_folderid == '1') 
                    MessagePanel2.setFromText(WtfGlobal.getLocaleText('pm.common.TO')+':', WtfGlobal.getLocaleText('pm.personalmessages.senton')+':');
                else 
                    MessagePanel2.setFromText(WtfGlobal.getLocaleText('pm.personalmessages.from.text')+':', WtfGlobal.getLocaleText('pm.personalmessages.receivedon')+':');
                var detail = ds.getAt(rowIndex).get('post_text')+ds.getAt(rowIndex).get('Attachment');    
                if (detail == "") {
                    MessagePanel2.messageId = postid;
                    MessagePanel2.topicstore.loadForum(ds.getAt(rowIndex).get('post_id'), "-1", "mail","");
                }
                else 
                    MessagePanel2.loadCacheData(detail);
                if(this.portalmail_folderid == "0" || this.portalmail_folderid == "4") {
                    var flagFolderId = this.dst.getAt(this.dst.find('post_id', postid)).get("folder");
                    var draftsMenu = Wtf.menu.MenuMgr.get("portalmail_actionMenuForPMsg" + postid).items.items[0];
                    if(draftsMenu != undefined)
                        if(draftsMenu.text.trim() == WtfGlobal.getLocaleText('pm.personalmessages.inbox') && flagFolderId == '0')
                            draftsMenu.disable();
                }
                this.addExistingFoldersForMsgMenu(postid);
            }
        }
    },
    createMailToolbar1:function(folderid, postid){
        var actionarr = Array();
        var portalmail_actionMenuForPMsg = new Wtf.menu.Menu({
                id: 'portalmail_actionMenuForPMsg' + postid,
                scope:this
        });
        if (folderid != '2') {
                //******************** Action menu for perticular msg toolbar *********************
                if(folderid == '0' || (folderid != '1' && folderid != '2' && folderid != '3') ){
                portalmail_actionMenuForPMsg.add(
                    new Wtf.Action({
                    text: WtfGlobal.getLocaleText('pm.personalmessages.inbox'),
                    scope:this,
                    handler: function(){
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.342'), function(btn){
                            if (btn == 'yes') {
                                this.UpdateFolderIDForPerMsg(postid, 0);
                                //Wtf.Msg.alert('Message Move', 'Message has been moved successfully.');
                            }
                        },this);
                    },
                    iconCls: "pwnd inbox",
                    tooltip: {
                        text: WtfGlobal.getLocaleText('pm.Help.pminbox')
                    }
                })
                );

                actionarr.push(new Wtf.Action({
                    text: WtfGlobal.getLocaleText('pm.personalmessages.reply'),
                    id: 'btnreplyto1',
                    scope:this,
                    handler: function(){
                        this.createReplyWindowForPMsg(postid)
                    },
                    tooltip: {
                        text: WtfGlobal.getLocaleText('pm.Help.pmreply')
                    },
                    iconCls: 'pwnd outbox'
                }))
            }

            actionarr.push(new Wtf.Action({
                text: WtfGlobal.getLocaleText('lang.delete.text'),
                scope:this,
                handler: function(){
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.350'), function(btn){
                        if (btn == 'yes') {
                            this.UpdateFolderIDForPerMsg(postid, 2);
                            msgBoxShow(148, 0);
                            this.remove(this.activeTab);
                            //Wtf.Msg.alert('Message Delete', 'Message has been deleted successfully.');
                        }
                    }, this);
                },
                id: 'btndelete1',
                tooltip: {
                    text: WtfGlobal.getLocaleText('pm.Help.pmdelete')
                },
                iconCls: 'pwnd deliconwt'
            }))
            if(folderid!=1 && folderid!= 2 && folderid != 3 && folderid != 4){
                actionarr.push(moveto1 = new Wtf.Toolbar.MenuButton({
                    text: WtfGlobal.getLocaleText('pm.personalmessages.moveto'),
                    iconCls: 'pwnd sendmsg',
                    tooltip: {
                        text: WtfGlobal.getLocaleText('pm.Help.pmmoveto')
                    },
                    id: 'MoveFolders' + postid,
                    menu: portalmail_actionMenuForPMsg
                }))
            }
        } else {
            actionarr.push(new Wtf.Action({
                text: WtfGlobal.getLocaleText('lang.delete.forever'),
                id: 'btndelforever1',
                scope:this,
                handler: function(){
                    this.deleteMsgForever(postid)
                },
                tooltip: {
                    text: WtfGlobal.getLocaleText('pm.Help.pmdeleteforever')
                },
                iconCls: 'pwnd deliconwt'
            }))
            actionarr.push(new Wtf.Action({
                text: WtfGlobal.getLocaleText('pm.personalmessage.restore.text'),
                id: 'btnrestoremsg1',
                handler: function(){
                    this.RestoreMsg(postid);
                },
                scope: this,
                tooltip: {
                    text: WtfGlobal.getLocaleText('pm.Help.pmrestore')
                },
                iconCls: 'pwnd sendmsg'
            }))
        }
        return actionarr;
    }, 
    createMailToolbar:function(postid){
        var actionarr = Array();
        var folderid = this.portalmail_folderid;
        if(folderid == 4 || this.txt){
            var flagFolderId = this.dst.getAt(this.dst.find('post_id', postid)).get("folder");
            actionarr = this.createMailToolbar1(flagFolderId, postid);
        } else {
            actionarr = this.createMailToolbar1(folderid, postid);
        }
        return actionarr;
    },
    createReplyWindowForPMsg:function(postid){
//        var ds = this.portalmail_grid1.getStore();
        var record = this.dst.getAt(this.dst.find('post_id', postid));
        if(record.get('deskSuperuser')=='true'){
            msgBoxShow(173, 1);
            return;
        }
        var rObj = Wtf.getCmp(postid + "_replyPanel");
        var posttime = WtfGlobal.userPrefDateRenderer(record.get('post_time'));
        if(rObj === undefined){
            rObj = new Wtf.ReplyWindow({
                uLabelName: 'Reply To',
                bLabelName: 'Subject',
                uLabel: WtfGlobal.getLocaleText('pm.personalmessages.replyto'),
                bLabel: WtfGlobal.getLocaleText('lang.subject.text'),
                tdisabled: true,
                replytoId: postid,
                userId: loginid,
                title:WtfGlobal.getLocaleText('pm.personalmessages.reply'),
                id: postid + "_replyPanel",
//                id:'replywin'+Math.random(),
                closable: true,
                layout: 'fit',
                groupId: "",
                firstReply: "",
                uFieldValue: record.get('poster_id'),
                bFieldValue: "Re:"+ WtfGlobal.URLDecode(decodeURIComponent(record.get('post_subject'))),
                type: "Mail",
                details:"<br><br><br><br><br><br><br><br>"+WtfGlobal.getLocaleText("pm.mail.original.text")+"<br><br><br>"+WtfGlobal.getLocaleText("lang.on.text")+" "+posttime+", "+record.data['post_fullname']+" "+WtfGlobal.getLocaleText("lang.wrote.text") +" : <br><br>"+ WtfGlobal.URLDecode(decodeURIComponent(record.data['post_text'])),
                sendFlag: "reply",
                fid:this.portalmail_folderid,
                composeMail:5
            });
            rObj.insertStore.on("load", this.handleInsertMail);
            this.add(rObj);
        }
        this.setActiveTab(rObj);
    },

    UpdateFolderIDForPerMsg:function(postid, folder_id){
        var last_folder_id = this.portalmail_folderid;
        var ds = this.portalmail_grid1.getStore();
        var jsonData = "{data:[";
        jsonData += "{'post_id':'" + encodeURIComponent(postid) + "'}";
        jsonData += "]}";
        Wtf.Ajax.requestEx({
            method: 'POST',
            url: Wtf.req.prt + 'getmail.jsp',
            params: {
                mailflag: 'movemails',
                last_folder_id: last_folder_id,
                dest_folder_id: folder_id,
                post_id: jsonData
            }
        }, this, function(result, req){
               var nodeobj = eval("(" + result + ")");
                var storeobj = this.portalmail_grid1.getStore();
                storeobj.reload();
                /*for (var j = 0; j < nodeobj.data.length; j++) {
                    storeobj.remove(ds.getAt(ds.find('post_id', nodeobj.data[j].post_id)));
                    if(Wtf.getCmp("emailsTab" + postid).ownerCt)
                        Wtf.getCmp("emailsTab" + postid).ownerCt.remove(Wtf.getCmp("emailsTab" + postid));
                }*/
        }, function(result, req){
        });
    },
    addExistingFoldersForMsgMenu:function(postid){
        if (this.portalmail_folderid != '2') {
            //***************** Take user folders ******************************                
            Wtf.Ajax.requestEx({
                url: Wtf.req.prt + 'getmailfolders.jsp',
                params: {
                    loginid: loginid
                }},
                this,
                function(result, req){
                    var nodeobj = eval("(" + result + ")");
                    for (var j = 0; j < nodeobj.length; j++) {
                        var folderid = nodeobj[j].folderid;
                        var foldernametext = nodeobj[j].foldername;
                        var flagFolderId = this.dst.getAt(this.dst.find('post_id', postid)).get("folder");
                        if(folderid != flagFolderId){
                            Wtf.menu.MenuMgr.get("portalmail_actionMenuForPMsg" + postid).add({
                                text: foldernametext,
                                id: folderid,
                                scope: this,
                                handler: function(e){
                                    folderid = e.id;
                                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.342'), function(btn){
                                        if (btn == 'yes') {
                                            this.UpdateFolderIDForPerMsg(postid, folderid);
                                            this.remove(this.activeTab);
                                            //Wtf.Msg.alert('Message Move', 'Message has been moved successfully.');
                                        }
                                    }, this)
                                },
                                icon: "lib/resources/images/default/tree/folder.gif"
                            });
                        }
                    }
                });
        }
    },
    onClickHandle1:function (grid,row,col,e) {    
         if(e.getTarget("img[class='starImgDiv']")) {
            var flag = "";
            var targetImg  = grid.getView().getCell(row,col).firstChild.firstChild;
            var rowobj = this.dst.getAt(row);
            if (targetImg.src.match('../../images/FlagGrey.gif')) {
                flag = true;
            } else {
                flag = false;
            }
            var jsonData = "{data:[";
            jsonData += "{'post_id':'" + encodeURIComponent(rowobj.data.post_id) + "'}";
            jsonData += "]}";
            rowobj.set('flag',flag);
            this.changeStar(flag,jsonData,row,col);
//                Wtf.Ajax.requestEx({
//                        method: 'GET',
//                        url: Wtf.req.prt + 'getmail.jsp',
//                        params: {
//                        mailflag: 'starchange',
//                        post_id: jsonData,
//                        flag: flag
//                    }
//                    }, this, function(result, req){
//                        if(flag=='true'){
//                            targetImg.src = "../../images/FlagRed.gif";
//                            targetImg.setAttribute("star", 0);
//                        }else{
//                            targetImg.src = "../../images/FlagGrey.gif";
//                            targetImg.setAttribute("star", 1);
//                        }
//                        
//                    }, function(result, req){
//                 });
             this.portalmail_sm1.deselectRow(row);
             if(this.portalmail_folderid==4){
                 this.dst.reload();
             }
//        } else{
//            this.rowSelectionChange(this.portalmail_sm1,row);
        }
//            if (this.portalmail_sm1.getCount() == 1) {
//                var selArray = [];
//                selArray = this.portalmail_sm1.getSelections();
//                var rowobj = selArray[0];
//                this.updateButtonStatus(1,rowobj.get("folder"));
//                var ds = this.portalmail_grid1.getStore();
//                var rowIndex = ds.find('post_id', rowobj.get('post_id'));
//                var folderid = rowobj.get("folder");
//                if(folderid){
//        //                this.portalmail_folderid = parseInt(folderid);
//                }
//            } else if (this.portalmail_sm1.getCount() == 0) {
//                this.enablemailtoolbarbtns();
//                this.MessagePanel1.setData1("", "", "", "", "../../images/blank.png");
//            } else if (this.portalmail_sm1.getCount() > 1) {
//                this.updateButtonStatus(this.portalmail_sm1.getCount(),this.portalmail_folderid);
//                this.MessagePanel1.setData1("", "", "", "", "../../images/blank.png");
//            }
//         }
    },
    updateButtonStatus:function(count,folderid){
        if (folderid == '0') {
            if (count == 1) 
                Wtf.getCmp('btnreplyto').enable();
            else 
                Wtf.getCmp('btnreplyto').disable();
            Wtf.getCmp('btndelete').enable();
            Wtf.getCmp('MoveFolders').enable();
            Wtf.getCmp('btnmoreactions').enable();
            Wtf.getCmp('0').disable();
        } else if (folderid == '2') {
            this.enablemailtoolbarbtns();
            Wtf.getCmp('btndelete').enable();
        } else if (folderid== '4') {
            if(count > 1){
                var sel = this.portalmail_grid1.getSelectionModel().getSelections();
                var fid = sel[0].data.folder;
                for (var i = 1; i < sel.length; i++) {
                    var f = sel[i].data.folder;
                    if(f == fid)
                        var sameFolder = true;
                    else {
                        sameFolder = false;
                        break;
                    }
                }
                if(sameFolder){
                    Wtf.getCmp('btndelete').enable();
                } else {
                    Wtf.getCmp('btndelete').disable();
                }
            }
            Wtf.getCmp('btnreplyto').disable();
            Wtf.getCmp('MoveFolders').disable();
            Wtf.getCmp('btnmoreactions').enable();
        } else if (folderid == '1') {
            Wtf.getCmp('btnreplyto').disable();
            Wtf.getCmp('btndelete').enable();
            Wtf.getCmp('MoveFolders').disable();
            Wtf.getCmp('btnmoreactions').enable();
        } else if(folderid == '3') {
            Wtf.getCmp('btnreplyto').disable();
            Wtf.getCmp('btndelete').enable();
            Wtf.getCmp('MoveFolders').disable();
            Wtf.getCmp('btnmoreactions').enable();
            Wtf.getCmp('0').enable();
        } else {
            Wtf.getCmp('btnreplyto').enable();
            Wtf.getCmp('btndelete').enable();
            Wtf.getCmp('MoveFolders').enable();
            Wtf.getCmp('btnmoreactions').enable();
            Wtf.getCmp('0').enable();
        } 
    },
    enablemailtoolbarbtns:function(){
        Wtf.getCmp('btnreplyto').disable();
        //Wtf.getCmp('btnsendmsg').enable() ;
        Wtf.getCmp('btndelete').disable();
        Wtf.getCmp('MoveFolders').disable();
        Wtf.getCmp('btnmoreactions').disable();
    },
    beforeRowselect:function(sm,rowindex,kexisting,rec) {
        var folderid = rec.get("folder");
        if(folderid){
//             this.portalmail_folderid = parseInt(folderid);
        }
    },
    
    rowSelectionChange:function(sm,rowIndex,rec){
        /*if(sm.getSelections().length > 0 ){
            Wtf.getCmp("btndelete").enable();
            Wtf.getCmp("MoveFolders").enable();
            Wtf.getCmp("btnmoreactions").enable();
        }*/
        if(sm.getSelections().length > 1 ) {
            Wtf.getCmp("btnreplyto").disable();
            this.MessagePanel1.setData1("", "", "", "", "../../images/blank.png");
            this.updateButtonStatus(sm.getSelections().length,this.portalmail_folderid);
        }else{
            this.updateButtonStatus(sm.getSelections().length,this.dst.getAt(rowIndex).get('folder'));
            var details = this.dst.getAt(rowIndex).get('post_text')+this.dst.getAt(rowIndex).get('Attachment');
            var posttime = WtfGlobal.userPrefDateRenderer(this.dst.getAt(rowIndex).get('post_time'));
            this.MessagePanel1.setData1("","","",'<div class="loading-indicator">&#160;'+WtfGlobal.getLocaleText('pm.loading.text')+'...</div>',"");
            this.MessagePanel1.setData(this.dst.getAt(rowIndex).get('post_subject'), this.dst.getAt(rowIndex).get('post_fullname'), 
            posttime, this.dst.getAt(rowIndex).get('imgsrc'),this.dst.getAt(rowIndex).get('senderid'),this.dst.getAt(rowIndex).get('deskSuperuser'));
            this.MessagePanel1.messageId=this.dst.getAt(rowIndex).get('post_id');
            if (details == "") {
                this.MessagePanel1.topicstore.loadForum(this.dst.getAt(rowIndex).get('post_id'), "-1", "mail","");
            }
            else 
                this.MessagePanel1.loadCacheData(details);
        }
    },
    rowDeselect:function(sm, ri, rec) {
        this.MessagePanel1.setData1("", "", "", "", "../../images/blank.png");
        if(sm.getSelections().length ==  0){
            Wtf.getCmp('emails').clearContents();
            Wtf.getCmp("btndelete").disable();
            Wtf.getCmp("MoveFolders").disable();
            Wtf.getCmp("btnmoreactions").disable();
            Wtf.getCmp("btnreplyto").disable();
        }
        if(sm.getSelections().length ==  1){
            ri = this.dst.find("post_id", sm.getSelections()[0].data.post_id);
            this.rowSelectionChange(sm, ri, rec);
        }        
    },
    sortchange:function(grid,obj){
        //var store = grid.getStore();
        var count = this.dst.getCount();
        var recordArr = this.dst.getRange(0,count);
        var view = this.portalmail_grid1.getView();
//        view.refresh();
        if (this.portalmail_folderid == '0') {   
            for (i = 0; i < count; i++) {
                if(recordArr[i].data['readflag']==false){
                    view.getCell(i, 1).firstChild.style.fontWeight = "bold";
                    view.getCell(i,  2).firstChild.style.fontWeight = "bold";
                    view.getCell(i,  3).firstChild.style.fontWeight = "bold";
                }
            } 
        }
    },
    displayFoldersWindow:function(folderid, foldertext){
        this.enablemailtoolbarbtns();
        document.getElementById('mailsearchtextbox').value = "";
        this.portalmail_folderid = folderid;
        this.portalmail_titleflag = foldertext;
        //portalmail_grid1.setTitle(loadingDisplay(portalmail_titleflag));
        this.portalmail_grid1.setTitle(this.portalmail_titleflag);
        var view = '';

        this.dst.loadForum(folderid, "fetch", loginid);
        this.dst.on("loadexception", function exp(){
            //portalmail_grid1.setTitle(portalmail_titleflag);
            msgBoxShow(4, 1);
            //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occurred while connecting to the server');
        },this);

        this.portalmail_grid1.store = this.dst;
//        view = this.portalmail_grid1.getView();
//        view.refresh();
        this.dst.on("load", function(a, b, c){
            this.portalmail_folderid = a.baseParams.flag;
            var view = this.portalmail_grid1.getView();
            if (b.length == 0) {
                this.portalmail_grid1.setTitle(this.loadingDisplayNo(this.portalmail_titleflag));
            } else {
//                view.refresh();   
//                this.portalmail_grid1.store.clearGrouping();
                if(this.portalmail_folderid == '0') {
                   this.portalmail_grid1.store.clearGrouping();
                    for (var i = 0; i < a.getCount(); i++) {
                        if(b[i].data['readflag']==false){
                            view.getCell(i, 1).firstChild.style.fontWeight = "bold";
                            view.getCell(i, 2).firstChild.style.fontWeight = "bold";
                            view.getCell(i, 3).firstChild.style.fontWeight = "bold";
                        }
                    }
                }else if(this.portalmail_folderid =='4'){
//                    view.refresh();
                    this.portalmail_grid1.getStore().groupBy("folder");
                }else{
                     this.portalmail_grid1.store.clearGrouping();
                }
            }
            if(this.dst.getCount()>0){
                this.portalmail_sm1.selectFirstRow();
                this.rowSelectionChange(this.portalmail_sm1,0);
            }
        },this);
    }
});

var lefttree=null;
var portalmail_mainPanel = new Wtf.PortalMailPanel();
var pmPanel = new Wtf.Panel({
     layout:'border',
     border:false,
     items:[{
        region:'west',
        border:true,
        layout:'fit',
        frame:false,
        bodyStyle:'background:white;padding:10px;',
        width:210,
        split:false,
        items:[lefttree = new Wtf.MailLeftTree({border:false,tabless:true,portalmailPanel :portalmail_mainPanel})]
    }, {
        region:'center',
        layout:'fit',
        border:false,
        items:[new Wtf.Panel({
            border:false,
            layout:'fit',
            id:'tabmailtab',
            items:[portalmail_mainPanel]
            /*autoLoad: autoload = {
                url: "../../mail.html",
                scripts: true
            }*/
        })]
   }]
});
Wtf.getCmp("tabpmessage").add(pmPanel);
Wtf.getCmp("tabpmessage").doLayout();
