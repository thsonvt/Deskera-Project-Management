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

Wtf.ForumTree = function(config){
    Wtf.apply(this, config);
    this.row;
    this.col = 2;
    this.id = 100;
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.flagArray = ["DESC", "DESC", "DESC", "DESC"];
    this.forumPost = Wtf.data.Record.create([{
        name: 'Subject'
    }, {
        name: 'From'
    }, {
        name: 'Received',type: 'date',dateFormat: 'Y-m-j H:i:s'
    }, {
        name: 'Flag',
        type: 'boolean'
    }, {
        name: 'ifread',
        type: 'boolean'
    }, {
        name: 'ID'
    }, {
        name: 'Details'
    }, {
        name: 'Attachment'
    }, {
        name: 'Parent'
    }, {
        name: 'Level'
    }, {
        name: 'Image'
    }, {
        name: 'User_Id'
    }]);
    if (config.roleId == 3) {
        this.cmodel = new Wtf.grid.ColumnModel([this.sm, {
            header: WtfGlobal.getLocaleText('pm.project.discussion.topics'),
            width: 300,
            dataIndex: 'Subject',
            //sortable: true,
            renderer: this.CustomCell
        }, {
            header: WtfGlobal.getLocaleText('pm.personalmessages.from.text'),
            width: 170,
           // sortable: true,
            dataIndex: 'From'
        }, {
            header: WtfGlobal.getLocaleText('pm.project.discussion.createddate'),
            width: 180,
            //sortable: true,
            renderer: WtfGlobal.userPrefDateRenderer,
            dataIndex: 'Received'
        },{
            header: WtfGlobal.getLocaleText('pm.common.flag'),
            width: 70,
            resizable:false,
            fixed:true,
            dataIndex: 'Flag',
            renderer: this.FlagCell
        }
        ]);
    }
    else 
        if (config.roleId == 1) {
            this.cmodel = new Wtf.grid.ColumnModel([{
                header: WtfGlobal.getLocaleText('pm.project.discussion.topics'),
                width: 300,
               // sortable: true,
                dataIndex: 'Subject',
                renderer: this.CustomCell
            }, {
                header: WtfGlobal.getLocaleText('pm.personalmessages.from.text'),
                width: 170,
                //sortable: true,
                dataIndex: 'From'
            }, {
                header: "Created date",
                width: 180,
                renderer: WtfGlobal.userPrefDateRenderer,
                dataIndex: 'Received'//,
               // sortable: true
            } ,{
                header: WtfGlobal.getLocaleText('pm.common.flag'),
                width: 70,
                //sortable: true,
                resizable:false,
                fixed:true,
                dataIndex: 'Flag',
                renderer: this.FlagCell
            }]);
        }
    this.jReader = new Wtf.data.KwlJsonReader({
        totalProperty: 'forumCount',
        root: "data"
    }, this.forumPost);
    this.dstore = new Wtf.data.Store({
        proxy: new Wtf.data.HttpProxy({
            url: Wtf.req.prt + "loadForumTree.jsp"
        }),
        reader: this.jReader
    });
    
    Wtf.ForumTree.superclass.constructor.call(this, {
        ds: this.dstore,
        cm: this.cmodel,
        border: false,
        loadMask: true,
        enableHdMenu : false,
        viewConfig: {
            forceFit: true,
            emptyText: '<div class="emptyGridText">'+WtfGlobal.getLocaleText('pm.project.discussion.emptytext')+'<br>'
        }
    });
    
    this.addEvents = {
        "cellclick1": true,
        "mgContext": true
    };
}

Wtf.extend(Wtf.ForumTree, Wtf.grid.GridPanel, {

    ReplyWindow: function(obj, rowval){
        var m = null;
        m = new obj.forumPost({
            Subject: 'New Reply',
            Received: new Date('26/10/2007 05:05:05 PM'),
            From: 'Unknown User',
            Flag: 'False',
            ID: obj.id++,
            Details: 'No Details',
            Level: parseInt(obj.dstore.getAt(rowval).data['Level']) + 1,
            Parent: obj.dstore.getAt(rowval).data['ID']
        });
        var parentRow = obj.search(obj.dstore.getAt(rowval).data['ID'], obj);
        var parentRec = obj.dstore.getAt(rowval);
        var i = 2;
        var tempRec = obj.dstore.getAt(parentRow + 1);
        if (tempRec) {
            for (; parentRec.data['Level'] < tempRec.data['Level']; i++) {
                tempRec = obj.dstore.getAt(parentRow + i);
                if (!tempRec) 
                    break;
            }
        }
        obj.dstore.insert(rowval + i - 1, m);
        var _v = obj.getView();
        if (_v.getCell(parentRow, 0).firstChild.firstChild.className == "forum_plus") 
            _v.getCell(rowval + i - 1, 0).parentNode.parentNode.parentNode.parentNode.style.display = "none";
        else 
            if (_v.getCell(parentRow, 0).firstChild.firstChild.className != "forum_minus") 
                _v.getCell(parentRow, 0).firstChild.firstChild.className = "forum_minus";
        _v.getCell(rowval + i - 1, 0).firstChild.style.marginLeft = parseInt(18 * obj.dstore.getAt(rowval + i - 1).data['Level']) + 'px';
    },
    
    CollExp: function(obj, ri, styleHelper){
        var readFlag = false;
        var _v = this.getView();
        if (obj.className == "forum_minus") {
            var parentLevel = this.dstore.getAt(this.row).data['Level'];
            for (var i = this.row + 1; i < this.dstore.getCount(); i++) {
                var nextrec = this.dstore.getAt(i);
                if (nextrec.data['Level'] <= parentLevel) 
                    break;
                _v.getCell(i, 0).parentNode.parentNode.parentNode.parentNode.style.display = "none";
                if (_v.getCell(i, styleHelper).firstChild.style.fontWeight == "bold") 
                    readFlag = true;
            }
            if (readFlag) {
                this.makeUnread(_v, ri, styleHelper);
            }
            obj.className = "forum_plus";
        }
        else 
            if (obj.className == "forum_plus") {
                var parentLevel = this.dstore.getAt(this.row).data['Level'];
                for (var j = this.row + 1; j < this.dstore.getCount(); j++) {
                    var nextrec = this.dstore.getAt(j);
                    if (nextrec.data['Level'] <= parentLevel) 
                        break;
                    _v.getCell(j, 0).parentNode.parentNode.parentNode.parentNode.style.display = "block";
                    if (_v.getCell(j, styleHelper).firstChild.firstChild.className == "forum_plus") {
                        var tempLevel = this.dstore.getAt(j).data['Level'];
                        for (var k = j + 1; k < this.dstore.getCount(); k++, j++) {
                            nextrec = this.dstore.getAt(k);
                            if (nextrec.data['Level'] <= tempLevel) 
                                break;
                        }
                    }
                }
                obj.className = "forum_minus";
                this.makeRead(_v, ri, styleHelper);
            }
    },
    
    search: function(ID, obj){
        for (var z = 0; z < obj.dstore.getCount(); z++) {
            if (obj.dstore.getAt(z).data['ID'] == ID) 
                return z;
        }
        return null;
    },
    
    CustomCell: function(text){
        return '<div id="forum_imgDiv" class="forum_blank"></div><div id="forum_postDiv" class="forum_postImage" height="13px" width="15px" ><img id="forum_img" height="12px" width="15px" src="../../images/read.gif"/></div><div id="forum_textDiv" class="forum_defaultText">' + WtfGlobal.URLDecode(decodeURIComponent(text)) + '</div>';
    },
    
    FlagCell: function(text){
        if (text) {
            return '<div id="img_div" class="redflag" height="10px"> </div>';
        } else {
            return '<div id="img_div" class="greyflag" height="10px"> </div>';
        }
    },
    dateRenderer: function(value){
        if(value!=null)    
            return value.format(WtfGlobal.getDateFormat());
    }
});
/*  WtfForumGrid: End   */

/*  WtfForum: Start */
Wtf.forumpPageSize = function(config){
    Wtf.apply(this, config)
    this.totalSize = null;
}
Wtf.extend(Wtf.forumpPageSize, Wtf.common.pPageSize, {
    changePageSize: function(value){
        var topicCount = 0;
        var subCount = 0;
        var pt = this.pagingToolbar;

        //this.combo.collapse();
        value = parseInt(value) || parseInt(this.combo.getValue());
        value = (value > 0) ? value : 1;
         
        if (value < pt.pageSize) {
            if(this.ftree!=null){
                this.ftree.getSelectionModel().selections.clear();
            }
            var store = pt.store;
            store.suspendEvents();
            for (var j = 0; j < store.getCount(); j++) {

                if (store.getAt(j).data['ID'].match('topic')) {
                    topicCount++;
                    if ((topicCount) == (value + 1))
                        break;
                }
                subCount++;
            }
            if(topicCount!=this.totalSize)
                topicCount--;
            pt.pageSize = value;
            var ap = Math.round(pt.cursor / subCount) + 1;
            var cursor = (ap - 1) * subCount;
            store.suspendEvents();
            for (var i = 0, len = cursor - pt.cursor; i < len; i++) {
                store.remove(store.getAt(0));
            }
            while (store.getCount() > subCount) {
                store.remove(store.getAt(store.getCount() - 1));
            }
            store.resumeEvents();
            store.fireEvent('datachanged', store);
            pt.cursor = cursor;
            var d = pt.getPageData();
            pt.afterTextEl.el.innerHTML = String.format(pt.afterPageText, d.pages);
            if(store.data.length <= 0)
                ap = 1;
            pt.field.dom.value = ap;
            pt.first.setDisabled(ap == 1);
            pt.prev.setDisabled(ap == 1);
            pt.next.setDisabled(ap == d.pages);
            pt.last.setDisabled(ap == d.pages);
            pt.cursor = (ap - 1) * value;
//            pt.updateInfo();
            if(store.data.length > 0)
                pt.displayEl.update(WtfGlobal.getLocaleText({key:'pm.project.discussion.pagingtext', params:[parseInt(pt.cursor+1), parseInt(pt.cursor + parseInt(topicCount)), this.totalSize]}));
            else
                pt.displayEl.update(WtfGlobal.getLocaleText('pm.project.discussion.emptytext'));
        }
        else {
            this.pagingToolbar.pageSize = value;
            this.pagingToolbar.doLoad(Math.floor(this.pagingToolbar.cursor / this.pagingToolbar.pageSize) * this.pagingToolbar.pageSize);
        }
        this.updateStore();
        this.combo.collapse();
    }

});
/*  WtfForum: End*/

/*  WtfDiscussion: Start */
Wtf.DiscussionForum = function(config){
    Wtf.apply(this, config);
    this.msgReadImg = '../../images/read.gif';
    this.msgUnreadImg = '../../images/unread.gif';
    this.roleId = config.roleId;
    this.previewPanel = new Wtf.MessagePanel({
        id: 'ppanel' + this.id
    });
    this.searchFlag = true;
    this.curRecCount = 1;
    this.searchText = "";
    this.curComboIndex = 0;
    this.curComboFlag = false;
    this.styleHelper = 0;
    this.msgLmt = 15;
    this.dtask = new Wtf.util.DelayedTask(this.searchForum);
    this.mailSearchFlag = false;
    this.isClearTopic = false;
    this.delStr = "";
    if (this.roleId == 3) 
        this.styleHelper = 1;
    if (config.projectFlag)
        this.projectFlag = true;
    else
        this.projectFlag = false;
    var deleteReaderrecord =  Wtf.data.Record.create([{name:'tid'}]);
    this.deleteReader = new Wtf.data.KwlJsonReader({
        root: "data"
    }, deleteReaderrecord);
    this.deleteStore = new Wtf.data.Store({
        proxy: new Wtf.data.HttpProxy({
            url: Wtf.req.prt + "deleteMessage.jsp"
        }),
        reader: this.deleteReader
    });
    this.newMsgButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText('pm.project.discussion.newtopic'),
        tooltip: {
            title: WtfGlobal.getLocaleText('pm.project.discussion.newtopic'),
            text: WtfGlobal.getLocaleText('pm.Help.discnewmsg')
        },
        iconCls: 'pwnd compose',
        id: 'newMessage' + this.id
    });
    this.replyMsgButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText('pm.personalmessages.reply'),
        tooltip: {
            title: WtfGlobal.getLocaleText('pm.personalmessages.reply'),
            text: WtfGlobal.getLocaleText('pm.Help.discreply')
        },
        iconCls: 'pwnd outbox',
        id: 'Reply' + this.id
    });
    this.DeleteButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText('lang.delete.text'),
//        hidden:false,
        tooltip: {
            title: WtfGlobal.getLocaleText('lang.delete.text'),
            text: WtfGlobal.getLocaleText('pm.Help.discdelete')
        },
        iconCls: 'pwnd deliconwt',
        id: 'Delete' + this.id
    });
    
    this.searchField = new Wtf.form.TextField({
        id: "searchtextbox" + this.id,
        emptyText: WtfGlobal.getLocaleText('pm.search.bytopic'),
        width: 175,
        height: 19
    });
    if(!this.archived){
        this.tButnArr = [];

        this.tButnArr.push(this.newMsgButton);
        this.newMsgButton.on("click", this.createNewTopicWindow, this);

        this.tButnArr.push(this.replyMsgButton);
        this.replyMsgButton.on("click", this.ReplyWindow, this);

        this.tButnArr.push(this.searchField);

        this.tButnArr.push(this.DeleteButton);
        this.DeleteButton.on("click", this.deleteMessage, this);

    } else
        this.tButnArr = [this.searchField]
    this.selectedRow = null;
    this.wind = null;
    this.ftree = new Wtf.ForumTree({
        border: false,
        region: 'center',
        layout: 'fit',
        id: 'forum-grid' + config.id,
        roleId: this.roleId,
        msgReadImg: this.msgReadImg,
        msgUnreadImg: this.msgUnreadImg,
        makeUnread: this.makeUnread,
        makeRead: this.makeRead
    });
    this.pageLimit = new Wtf.forumpPageSize({
        ftree:this.ftree
    });
    this.pToolBar = new Wtf.PagingToolbar({
        id: 'pgTbar' + this.id,
        pageSize: this.msgLmt,
        store: this.ftree.dstore,
        displayInfo: true,
//        displayMsg: WtfGlobal.getLocaleText('pm.project.discussion.pagingtext'),
        emptyMsg: WtfGlobal.getLocaleText('pm.project.discussion.emptytext'),
        plugins: this.pageLimit
    });
    Wtf.DiscussionForum.superclass.constructor.call(this, {
        title: WtfGlobal.getLocaleText('pm.module.discussion'),
        iconCls: config.iconCls,
        layout: 'fit',
        border: false,
        items: [{
            layout: 'border',
            border: false,
            autoWidth: true,
            items: [this.ftree , {
                region: 'south',
                height: 225,
                border: false,
                layout: 'fit',
                items: this.previewPanel,
                split: true
            }]
        }],
        tbar: this.tButnArr,
        bbar: this.pToolBar
    });
    
    this.on("render", this.renderDiscussion, this);
    this.ftree.on("cellclick1", this.ssetData, this);
    this.ftree.on("headerclick", this.handleSorting, this);
    this.previewPanel.on("UpdateDstore", this.syncDstore, this);
//    if(!this.archived)
    this.ftree.on("cellcontextmenu", this.handleContext, this);
    this.ftree.getSelectionModel().on("rowselect", this.setSelectedRow, this);
    this.ftree.dstore.on("load", this.setStyle, this);
    this.ftree.on("mgContext", this.mContextParent, this);
    this.ftree.dstore.on("loadexception", this.ldException, this);
    this.ftree.dstore.on("beforeload", this.changeBasePara, this);
    this.ftree.on("cellclick", this.onClickHandle, this);
    this.ftree.on("rowdblclick", this.ondblClickHandle, this);
    this.deleteStore.on("load", this.deletePost, this);
    this.ftree.getSelectionModel().on('rowselect', this.rowSelHandle, this);    
    this.ftree.getSelectionModel().on("rowdeselect", this.rowDeSelectHandle, this);
    dojo.cometd.subscribe("/" + this.id.substring(4) + "/forum", this, "forumPublishHandler");
}

Wtf.extend(Wtf.DiscussionForum, Wtf.Panel, {

    forumPublishHandler: function(msg){
        var temp = eval('(' + msg.data.data + ')');
        var temp1 = Wtf.decode(temp.data[0]).data;
        var m = null;
        this.ftree.dstore.reload();
      /*  if (temp1[0].ID.match('topic')) {
            m = new this.ftree.forumPost({
                Subject: WtfGlobal.URLDecode(temp1[0].Subject),
                Received: temp1[0].Received,
                From: temp1[0].From,
                Flag: temp1[0].Flag,
                ID: temp1[0].ID,
                Details: temp1[0].Details,
                Level: '0',
                Parent: '0',
                Image: temp1[0].Image
            });
            this.ftree.dstore.insert(0, m);
            this.makeUnread(this.ftree.getView(), 0, this.styleHelper);
        }
        else {
            var rIndex = this.ftree.search(temp1[0].Parent, this.ftree);
            if (rIndex != null) {
                m = new this.ftree.forumPost({
                    Subject: WtfGlobal.URLDecode(temp1[0].Subject),
                    Received: temp1[0].Received,
                    From: temp1[0].From,
                    Flag: temp1[0].Flag,
                    ID: temp1[0].ID,
                    Details: temp1[0].Details,
                    Level: parseInt(this.ftree.dstore.getAt(rIndex).data['Level']) + 1,
                    Parent: this.ftree.dstore.getAt(rIndex).data['ID'],
                    Image: temp1[0].Image
                });
                var parentRow = this.ftree.search(this.ftree.dstore.getAt(rIndex).data['ID'], this.ftree);
                var parentRec = this.ftree.dstore.getAt(rIndex);
                var c = 2;
                var tempRec = new Wtf.data.Record;
                tempRec = this.ftree.dstore.getAt(parentRow + 1);
                if (tempRec) {
                    for (; parentRec.data['Level'] < tempRec.data['Level']; c++) {
                        tempRec = new Wtf.data.Record;
                        tempRec = this.ftree.dstore.getAt(parentRow + c);
                        if (!tempRec) 
                            break;
                    }
                }
                this.ftree.dstore.insert(rIndex + c - 1, m);
                var _v = this.ftree.getView();
                this.makeUnread(_v, (rIndex + c - 1), this.styleHelper);
                if (_v.getCell(parentRow, this.styleHelper).firstChild.firstChild.className == "forum_plus") 
                    _v.getCell(rIndex + c - 1, 0).parentNode.parentNode.parentNode.parentNode.style.display = "none";
                else 
                    if (_v.getCell(parentRow, this.styleHelper).firstChild.firstChild.className != "forum_minus") 
                        _v.getCell(parentRow, this.styleHelper).firstChild.firstChild.className = "forum_minus";
                _v.getCell(rIndex + c - 1, this.styleHelper).firstChild.style.marginLeft = parseInt(18 * this.ftree.dstore.getAt(rIndex + c - 1).data['Level']) + 'px';
            }
        }*/
    },
    
    deletePost: function(obj, rec, opt){
        if(rec.length > 0){
            var dcount =this.deleteStore.getCount();
            var count = this.ftree.dstore.getCount();
            var pagesize = this.getBottomToolbar().pageSize;
            var pstart = this.ftree.getStore().lastOptions.params.start - pagesize;
            pstart = (pstart < 0) ? pstart = 0 : pstart;
            if(count == dcount){
                this.getBottomToolbar().doLoad(pstart);
            } else {
                this.getBottomToolbar().doLoad(Math.floor(this.getBottomToolbar().cursor / this.getBottomToolbar().pageSize) * this.getBottomToolbar().pageSize);
            }
            for(var cnt = 0; cnt < rec.length; cnt++){
                var temp = Wtf.getCmp("replywin" + rec[cnt].data["tid"]);
                if(temp !== undefined){
                    temp.ownerCt.remove(temp, true);
                }
                temp = Wtf.getCmp("pp" + rec[cnt].data["tid"]);
                if(temp !== undefined){
                    temp.ownerCt.remove(temp, true);
                }
            }
    }
//        if (rec[0].data['status'] == "success") {
//            this.getBottomToolbar().doLoad(Math.floor(this.getBottomToolbar().cursor / this.getBottomToolbar().pageSize) * this.getBottomToolbar().pageSize);
//        }
    },
    
    confirmDelete: function(btn, text){
        if (btn == 'yes') {
            var delObj = this.ftree.getSelectionModel().getSelections();
            this.delStr = "";
            for (var i = 0; i < delObj.length; i++) {
                var delObjCheck = this.delStr.split(",");
                var mainRecurrenceFlag = false;
                
                for (var b = 0; b < delObjCheck.length; b++) {
                    if (delObjCheck[b] == delObj[i].data['ID'])
                        mainRecurrenceFlag = true;
                }
                if (!mainRecurrenceFlag)
                    this.delStr += delObj[i].data['ID'] + ",";
                
                var rowIndex = this.ftree.search(delObj[i].data['ID'], this.ftree);
                var parentLevel = this.ftree.dstore.getAt(rowIndex).data['Level'];
                for (var k = rowIndex + 1; k < this.ftree.dstore.getCount(); k++) {
                    var nextrec = this.ftree.dstore.getAt(k);
                    if (nextrec.data['Level'] <= parentLevel)
                        break;
                    this.ftree.getSelectionModel().selectRow(k, true);
                    var delObjCheck = this.delStr.split(",");
                    var recurrenceFlag = false;
                    for (var a = 0; a < delObjCheck.length - 1; a++) {
                        if (delObjCheck[a] == nextrec.data['ID'])
                            recurrenceFlag = true;
                    }
                    if (!recurrenceFlag)
                        this.delStr += nextrec.data['ID'] + ",";
                }
            }
            this.delStr = this.delStr.substring(0, (this.delStr.length - 1));
            this.deleteStore.load({
                params: {
                    deleteId: this.delStr,
                    groupId: this.id.substring(4),
                    flag:'delmsg'
                }
            });
        }
    },
    
    deleteMessage: function(obj, e){
        if(this.ftree.getSelectionModel().getCount()>0)
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.328'), this.confirmDelete, this);
        else
            msgBoxShow(139, 0);
//        else if(this.ftree.getSelectionModel().getCount()>1)
//            msgBoxShow(158, 3);
        
    },
    
    ondblClickHandle: function(obj, rowIndex, e){
//        this.previewPanel = new Wtf.MessagePanel({
//        id: 'ppanel' + this.id
//    });
        var id12 = this.ftree.dstore.getAt(rowIndex).data['ID'];
        var details = this.ftree.dstore.getAt(rowIndex).data['Details']+this.ftree.dstore.getAt(rowIndex).data['Attachment'];
        var newMessagePanel = Wtf.getCmp( 'pp'+id12);
        if(newMessagePanel==null){
            var sub = this.ftree.dstore.getAt(rowIndex).get('Subject')==""?"[No Subject]": WtfGlobal.URLDecode(decodeURIComponent(this.ftree.dstore.getAt(rowIndex).get('Subject')));
            sub = Wtf.util.Format.ellipsis(sub, 50);
            var newMessagePanel = new Wtf.MessagePanel({
                title: sub,
                id: 'pp'+id12
                // this.ftree.dstore.getAt(ri).data['ID']
            });
            var tabid = this.id.substring(4);
            if (this.projectFlag) {
                Wtf.getCmp('subtabpanelcomprojectTabs_' + tabid).add(newMessagePanel).show();
            } else {
                Wtf.getCmp('sub' + tabid).add(newMessagePanel).show();
            }
            if(!Wtf.isIE7) // TEMP FIX: Causing layout problems in other tabs. Only in IE7.
                newMessagePanel.ownerCt.doLayout();
            newMessagePanel.setData1("", "", "", '<div class="loading-indicator">&#160;'+WtfGlobal.getLocaleText('pm.loading.text')+'...</div>', "");
            newMessagePanel.messageId = id12;
            var recived = WtfGlobal.userPrefDateRenderer(this.ftree.dstore.getAt(rowIndex).get('Received'));
            newMessagePanel.setData(this.ftree.dstore.getAt(rowIndex).get('Subject'), this.ftree.dstore.getAt(rowIndex).get('From'), recived/*.format('Y-m-d h:i a')*/ , this.ftree.dstore.getAt(rowIndex).get('Image'), this.ftree.dstore.getAt(rowIndex).get('User_Id'));
            if (id12.match('topic')) {
                var tempid = id12.substring(5);
                if (details == "")
                    newMessagePanel.topicstore.loadForum(tempid, 1, "forum", this.ftree.dstore.getAt(rowIndex).data['User_Id']);
                else
                    newMessagePanel.loadCacheData(details);
                newMessagePanel.setFromText(WtfGlobal.getLocaleText('pm.personalmessages.from.text')+':', WtfGlobal.getLocaleText('pm.common.createdon')+':');
            } else {
                if (details == "")
                    newMessagePanel.topicstore.loadForum(id12, 2, "forum",this.ftree.dstore.getAt(rowIndex).data['User_Id']);
                else
                    newMessagePanel.loadCacheData(details);
                newMessagePanel.setFromText(WtfGlobal.getLocaleText('pm.personalmessages.from.text')+':', WtfGlobal.getLocaleText('pm.project.discussion.postedon')+':');
            }
        } else {
            newMessagePanel.show();
            newMessagePanel.loadCacheData(details);
        }
//        if (this.ftree.dstore.getAt(rowIndex).data['Details'] == "") 
//            newMessagePanel.topicstore.loadForum(this.ftree.dstore.getAt(rowIndex).data['ID'], 2, "forum", this.ftree.dstore.getAt(rowIndex).data['User_Id']);
//        else 
//            newMessagePanel.loadCacheData(this.ftree.dstore.getAt(rowIndex).data['Details']);
    },
    
    rowSelHandle: function(sm, ri, rec){
        if(sm.getSelections().length>1){
          this.previewPanel.clearContents();
        } else {
          var selRec = this.ftree.dstore.getAt(ri);
          if(!selRec.data.ifread){
            selRec.data.ifread = true;
          }
          var recived = WtfGlobal.userPrefDateRenderer(selRec.data['Received']);
          this.ftree.fireEvent("cellclick1", selRec.data['Subject'], selRec.data['From'], recived, selRec.data['Image'], selRec.data['ID'], selRec.data['User_Id'], ri);
        }
    },
    rowDeSelectHandle: function(sm, ri, rec){     
        if(sm.getSelections().length>1 || !(sm.hasSelection())){
          this.previewPanel.clearContents();
        } else if(sm.getSelections().length==1){
            var selRec = this.ftree.getSelectionModel().getSelected();  
            var index = this.ftree.dstore.indexOf(selRec);
            this.rowSelHandle(sm,index,rec);
        }
    },
    onClickHandle: function(grid, ri, ci, e){
        grid.row = ri;
//        grid.col = ci;
        var flag=0;
        var target = e.target;
        var postid=this.ftree.dstore.getAt(ri).data['ID'];
        if (ci == (this.styleHelper + 3)) {
            if (target.className === "greyflag") {
                target.className = "redflag";
                flag = 1;
            } else if(target.className === "redflag"){
                target.className = "greyflag";
            }
            Wtf.Ajax.requestEx({
                url:"../../jspfiles/portal/deleteMessage.jsp",
                params: {
                   flag: "forumflag",
                postid: postid,
                value: flag
                }
            });
//             Wtf.Ajax.request({
//                        url:"../../jspfiles/portal/deleteMessage.jsp",
//                        params: {
//                            flag: "forumflag",
//                            postid: postid,
//                            value: flag
//                        },
//                        scope:this
//                        });
        }
        else if ((target.className == "forum_plus" || target.className == "forum_minus") && ci == this.styleHelper)
            grid.CollExp(e.target, ri, this.styleHelper);
        else{
            var _v = this.ftree.getView();
            this.makeRead(_v, grid.row, this.styleHelper);
        }
            
    },
    
    changeBasePara: function(obj, opt){
        obj.baseParams = {
            searchText: this.searchText,
            sortFlag: this.ftree.flagArray[this.ftree.col],
            col: this.ftree.col,
            limit: this.pToolBar.pageSize,
            groupId: this.id.substring(4)
        }
    },

    ldException: function(){
        this.previewPanel.ownerCt.doLayout();
    },
    
    mContextParent: function(row){
        this.checkReplyWindow(this);
    },
    
    setStyle: function(store, rec, opt){
        var parentId = [];
        var count = 0;
        for (var i = 0; i < store.getCount(); i++) {
            if (rec[i].data['Level'] == 0 && rec[i].data['Parent'] == 0) 
                count++;
            
            if (parentId[parseInt(rec[i].data['Level']) - 1] == rec[i].data['Parent']) 
                this.ftree.getView().getCell(i, 0).parentNode.parentNode.parentNode.parentNode.style.display = "none";
            if (rec[i + 1]) {
                if (rec[i].data['ID'] == rec[i + 1].data['Parent']) {
                    parentId[rec[i].data['Level']] = rec[i].data['ID'];
                    this.ftree.getView().getCell(i, this.styleHelper).firstChild.firstChild.className = "forum_plus";
                    
                }
            }
            var _v = this.ftree.getView();
            _v.getCell(i, this.styleHelper).firstChild.style.marginLeft = 18 * parseInt(rec[i].data['Level']) + 'px';
            if (!rec[i].data['ifread']) {
                var level = rec[i].data['Level'];
                var parentid = rec[i].data['Parent'];
                this.makeUnread(_v, i, this.styleHelper);
                while(level>0){
                    for (var j = i; j >= 0; j--) {
                        if (parentid == rec[j].data['ID']) {
                            this.makeUnread(_v, j, this.styleHelper);
                            parentid = rec[j].data['Parent'];
                            level--;
                            break;
                        }
                    }
                }    
            }
        }
        //[sy][Bug:Threads 1-0-0]
        if(this.ftree.jReader.jsonData['forumCount'])
            this.getBottomToolbar().displayEl.update(WtfGlobal.getLocaleText({key:'pm.project.discussion.pagingtext',params:[parseInt(opt.params["start"] + 1), parseInt(count + opt.params["start"]), this.ftree.jReader.jsonData['forumCount']]}));
        this.pageLimit.totalSize = this.ftree.jReader.jsonData['forumCount'];
        this.previewPanel.ownerCt.doLayout();
        this.ftree.ownerCt.doLayout();
        this.previewPanel.clearContents();

        this.previewPanel.addListener("click", function(a,e){
                var tar = a.target;
                if(tar.tagName == 'A' && tar.href.match('http://'))
                {
                  window.open(tar.href);
                  a.preventDefault();
                }
        }, this);
        this.ftree.getSelectionModel().selectFirstRow();
    },
    
    makeUnread: function(view, i, s, NoChgImg){
        view.getCell(i, s).firstChild.style.fontWeight = "bold";
        view.getCell(i, s + 1).firstChild.style.fontWeight = "bold";
        view.getCell(i, s + 2).firstChild.style.fontWeight = "bold";
//        if (!NoChgImg) 
            view.getCell(i, s).firstChild.childNodes[1].firstChild.src = this.msgUnreadImg;
    },
    
    makeRead: function(view, i, s){
        view.getCell(i, s).firstChild.style.fontWeight = "normal";
        view.getCell(i, s + 1).firstChild.style.fontWeight = "normal";
        view.getCell(i, s + 2).firstChild.style.fontWeight = "normal";
        view.getCell(i, s).firstChild.childNodes[1].firstChild.src = this.msgReadImg;
    },
    
    handleActivate: function(obj){
        obj.pageLimit.combo.on("select", obj.setIndex, obj);
    },
    
    setSelectedRow: function(obj, rowIndex, rec){
        this.selectedRow = rowIndex;
    },
    
    setIndex: function(obj, rec, index){
        if ((this.curComboIndex >= index) || (this.curComboIndex == 0)) {
            var parentId = [];
            var count = 0;
            var _trview = this.ftree.getView();
            for (i = 0; i < this.ftree.dstore.getCount(); i++) {
                var _rec = this.ftree.dstore.getAt(i);
                if (!_rec.data['ifread']) {
                    this.makeUnread(_trview, i, this.styleHelper, true);
                    for (j = i; j >= 0; j--) {
                        if (_rec.data['Parent'] == this.ftree.dstore.getAt(j).data['ID']) {
                            this.makeUnread(_trview, j, this.styleHelper, true);
                        }
                    }
                }
                if (parentId[parseInt(_rec.data['Level']) - 1] == _rec.data['Parent']) 
                    _trview.getCell(i, 0).parentNode.parentNode.parentNode.parentNode.style.display = "none";
                if (this.ftree.dstore.getAt(i + 1)) {
                    if (_rec.data['ID'] == this.ftree.dstore.getAt(i + 1).data['Parent']) {
                        parentId[_rec.data['Level']] = _rec.data['ID'];
                        _trview.getCell(i, this.styleHelper).firstChild.firstChild.className = "forum_plus";
                    }
                }
                _trview.getCell(i, this.styleHelper).firstChild.style.marginLeft = 18 * parseInt(_rec.data['Level']) + 'px';
                if (_rec.data['Level'] == 0 && _rec.data['Parent'] == 0) 
                    count++;
            }
        }
    },
    
    handleContext: function(Grid, row, coll, e){
        var menu = null;
        var contButnArr = Array();

        if(!this.archived) {
            contButnArr.push(new Wtf.menu.Item({
                id: 'newReply',
                iconCls: 'pwnd outboxCx',
                text: WtfGlobal.getLocaleText('pm.personalmessages.reply'),
                handler: function reply(){
                    Grid.fireEvent("mgContext", row);
                }
            }));
        }

        if(!this.archived ) {
            contButnArr.push(new Wtf.menu.Item({
                id: 'conDelete',
                iconCls: 'pwnd delicon',
                text: WtfGlobal.getLocaleText('lang.delete.text'),
                scope:this,
                handler: function del(){
                    this.deleteMessage(null,null);
                }
            }));
        }
        this.ftree.getSelectionModel().selectRow(row);

        if(contButnArr.length > 0 ) {
            if (!menu) {
                menu = new Wtf.menu.Menu({
                    id: 'context',
                    height: 18,
                    items: contButnArr
                });
            }
            menu.showAt(e.getXY());
        }

        e.preventDefault();
    },
    
    handleSorting: function(obj, col, e){
        e.preventDefault();
        e.stopPropagation();
        this.isClearTopic  = (col==0) ? true : false;
        var loadSortingStore = function(obj, c, mainObj){
            obj.col = c;
            obj.dstore.load({
                params: {
                    start: 0,
                    limit: (mainObj.pageLimit && mainObj.pageLimit.combo) ? (mainObj.pageLimit.combo.getValue() || mainObj.msgLmt) : mainObj.msgLmt
                }
            });
            if (obj.flagArray[c] == "ASC")
                obj.flagArray[c] = "DESC";
            else
                obj.flagArray[c] = "ASC";
        }
        if (this.styleHelper == 1) {
            if (col != 0) {
                loadSortingStore(obj, col - 1, this);
            }
        }
        else {
            loadSortingStore(obj, col, this);
        }
    },
    
    syncDstore: function(details, Attachment,dataid){
        for (var i = 0; i < this.ftree.dstore.getCount(); i++) {
            var rec = this.ftree.dstore.getAt(i);
            if (rec.data['ID'] == dataid) {
                rec.data['Details'] = details;
                rec.data['Attachment'] = Attachment;
				this.makeRead(this.ftree.getView(), i, this.styleHelper);
                break;
            }
            if (rec.data['ID'] == "topic"+dataid) {
                rec.data['Details'] = details;
                rec.data['Attachment'] = Attachment;
				this.makeRead(this.ftree.getView(), i, this.styleHelper);
                break;
            }
        }
    },
    
    renderDiscussion: function(obj){
        obj.ftree.dstore.load({
            params: {
                start: 0,
                limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt
            }
        });
        obj.pageLimit.combo.on("select", obj.setIndex, obj);
        Wtf.EventManager.addListener("searchtextbox" + this.id, 'keyup', this.txtsearchKeyPress, this);
    },
    
    ssetData: function(a, s, d, f, id, uid, row){
        var flag = "forum";
        this.previewPanel.setData1("", "", "", '<div class="loading-indicator">&#160;'+WtfGlobal.getLocaleText('pm.loading.text')+'...</div>', "");
        this.previewPanel.messageId = id;
        var _rec = this.ftree.dstore.getAt(row);
         var details = _rec.data['Details']+_rec.data['Attachment'];
        if (id.match('topic')) {
            var tempid = id.substring(5);
            if (details == "") 
                this.previewPanel.topicstore.loadForum(tempid, 1, flag, _rec.data['User_Id']);
            else 
                this.previewPanel.loadCacheData(details);
            this.previewPanel.setFromText(WtfGlobal.getLocaleText('pm.personalmessages.from.text')+':', WtfGlobal.getLocaleText('pm.common.createdon')+':');
        }
        else {
            if (details == "") 
                this.previewPanel.topicstore.loadForum(id, 2, flag, _rec.data['User_Id']);
            else 
                this.previewPanel.loadCacheData(details);
            this.previewPanel.setFromText(WtfGlobal.getLocaleText('pm.personalmessages.from.text')+':', WtfGlobal.getLocaleText('pm.project.discussion.postedon')+':');
        }
        this.previewPanel.setData(a, s, d, f, uid);
    },
    
    searchForum: function(){
        this.searchText = encodeURIComponent(this.searchField.getValue().trim());
        var lm = (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt;
        var _f = function(store){
            store.load({
                params: {
                    start: 0,
                    limit: lm
                }
            });
        }
        var _s = this.ftree.dstore;
        if (this.searchText.length > 0) {
            _f(_s);
            this.mailSearchFlag = false;
        }
        else {
            if (!this.mailSearchFlag) {
                _f(_s);
                this.mailSearchFlag = true;
            }
        }
    },
    
    checkReplyWindow: function(obj){
        var repId = null;
        var _rec = obj.ftree.getSelectionModel().getSelected();
        var repFlag = "2";
        if (_rec.data['Parent'] == 0 && _rec.data['Level'] == 0) 
            repFlag = "1";
        var tabid = this.id.substring(4);
        var tempCont = Wtf.getCmp('subtabpanelcomprojectTabs_' + tabid);
        if (!this.projectFlag) {
            tempCont = Wtf.getCmp('sub' + tabid).add(this.wind);
        }
        var temp = Wtf.getCmp('replywin'+_rec.data["ID"]);
        var recived = WtfGlobal.userPrefDateRenderer(_rec.data['Received']);
        if(temp === undefined){
            this.wind = new Wtf.ReplyWindow({
                uLabelName: 'Reply To',
                bLabelName: 'Title',
                uLabel: WtfGlobal.getLocaleText('pm.personalmessages.replyto'),
                bLabel: WtfGlobal.getLocaleText('lang.title.text'),
                tdisabled: true,
                id:'replywin'+_rec.data["ID"],
                title: WtfGlobal.getLocaleText('pm.personalmessages.reply'),
                replytoId: _rec.data['ID'],
                userId: loginid,
                closable: true,
                tabWidth: 150,
                sendFlag:'reply',
                groupId: obj.id.substring(4),
                firstReply: repFlag,
                uFieldValue: _rec.data['From'],
                bFieldValue: WtfGlobal.URLDecode(decodeURIComponent(_rec.data['Subject'])),
                type: "Forum",
                //id:"wind"+this.id.substring(4),
                projectFlag: this.projectFlag,
                details: "<br><br><br><br><br>"+WtfGlobal.getLocaleText("pm.mail.original.text")+"<br><br><br>"+WtfGlobal.getLocaleText("lang.on.text")+" "+ recived +",  "+_rec.data['From']+" "+WtfGlobal.getLocaleText("lang.wrote.text")+": <br><br>"+ WtfGlobal.URLDecode(decodeURIComponent(_rec.data['Details'])),
                replyds:this.ftree.dstore
            });
            tempCont.add(this.wind);
        } else {
            this.wind = temp;
        }
        tempCont.setActiveTab(this.wind);
        //this.wind.show();
    },

    ReplyWindow: function(obj, e){
        if(this.ftree.getSelectionModel().getCount()==1)
            this.checkReplyWindow(this, null);
        else if(this.ftree.getSelectionModel().getCount()>1) 
            msgBoxShow(158, 0);
        else 
            msgBoxShow(139, 0);
    },

    createNewTopicWindow: function(obj, e){
      var uLbl = "Community", uLblName = 'Community';
        if (this.projectFlag){
            uLbl = WtfGlobal.getLocaleText('pm.project.text');
            uLblName = 'Project';
        }
        var grId = this.id.substring(4);
        var tempCont = this.ownerCt;
        if (!this.projectFlag) {
            tempCont = Wtf.getCmp('sub' + grId);
        }
        var temp = Wtf.getCmp("replywin_newTopic");
        if(temp === undefined){
            if(this.wind)
                this.wind.destroy();
            this.wind = new Wtf.ReplyWindow({
                uLabel: uLbl,
                uLabelName: uLblName,
                bLabelName: 'Title',
                bLabel: WtfGlobal.getLocaleText('lang.title.text'),
                tdisabled: true,
                //isBlankable:false,
                closable: true,
                id:'replywin_newTopic',
                replytoId: "-999",
                userId: loginid,
                title: WtfGlobal.getLocaleText('pm.project.discussion.thread'),
                tabWidth: 150,
                sendFlag:'newmsg',
                groupId: grId,
                uFieldValue: "NewTopic",
                bFieldValue: "",
                firstReply: "0",
                rowIndex: "0",
                type: "Forum",
                id1: "wind" + grId,
                projectFlag: this.projectFlag,
                replyds:this.ftree.dstore
            });
            tempCont.add(this.wind);
        } else {
            this.wind = temp;
        }
        tempCont.setActiveTab(this.wind);
//        this.wind.doLayout();
    },

    txtsearchKeyPress: function(e){
        this.txt = e.getTarget().value;
        this.dtask.cancel();
        this.dtask.delay(500, this.searchForum, this);
    }
});
/*  WtfForum: End   */

