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

/*  Left Tree Menu: Start   */
Wtf.MailLeftTree = function(config){
    this.nodeHash = {};
    var tree;
    var inbox;
    var outbox;
    var drafts;
    var deleteditems;
    var starreditems;
    var temptreenode;
    var folders;
    var nodeid;
    var treeObj;
    var composeMail;
    Wtf.MailLeftTree.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.MailLeftTree, Wtf.tree.TreePanel, {
    autoWidth: true,
    autoHeight: true,
    rootVisible: false,
    id: 'folderview',
    autoScroll: true,
    animate: Wtf.enableFx,
    enableDD: false,
    hlDrop: Wtf.enableFx,
    setEvents: function(){
        Wtf.each(['0', '1', '2', '3', '4'], function(i){
            this.getNodeById(i).getUI().getTextEl().setAttribute("pmnode", WtfGlobal.getLocaleText('pm.personalmessages.text'));
        }, this);
        Wtf.each(['PM', 'folders'], function(i){
            this.getNodeById(i).getUI().getTextEl().setAttribute("pmnode", WtfGlobal.getLocaleText('pm.personalmessages.notclickable'));
        }, this);
    },
    
    displayMailWindow: function(){
        if (Wtf.get('tabmailtab') != null) {
            var tab = this.portalmailPanel.getComponent('tabmailtab_tab1');
            if (tab) {
                this.portalmailPanel.setActiveTab(tab);
            }
            var _cm = this.portalmailPanel.portalmail_grid1.getColumnModel();
            var ftext = "";
            switch (treeObj.nodeid) {
                case '3':
                    /*_cm.setColumnHeader(2, WtfGlobal.getLocaleText('pm.common.TO'));
                    _cm.setColumnHeader(3, "Created on");
                     Wtf.getCmp('emails').setFromText('From:', 'Created on:');
                    var ftext = Wtf.getCmp('folderview').getNodeById(treeObj.nodeid).getUI().getTextEl().innerHTML;
                    displayFoldersWindow(treeObj.nodeid, ftext);
                    break;*/
                    Wtf.getCmp('emails').setFromText(WtfGlobal.getLocaleText('pm.common.TO')+':', WtfGlobal.getLocaleText('pm.personalmessages.savedon')+':');
                    _cm.setColumnHeader(2, WtfGlobal.getLocaleText('pm.common.TO'));
                    _cm.setColumnHeader(3, WtfGlobal.getLocaleText('pm.personalmessages.savedon'));
                    ftext = Wtf.getCmp('folderview').getNodeById(treeObj.nodeid).getUI().getTextEl().innerHTML;
                    this.portalmailPanel.displayFoldersWindow(treeObj.nodeid, ftext);
                    break;
                case '4':
                    Wtf.getCmp('emails').setFromText(WtfGlobal.getLocaleText('pm.personalmessages.fromto')+':', WtfGlobal.getLocaleText('pm.personalmessages.receivedsent')+':');
                    _cm.setColumnHeader(2, WtfGlobal.getLocaleText('pm.personalmessages.fromto'));
                    _cm.setColumnHeader(3, WtfGlobal.getLocaleText('pm.personalmessages.receivedsent'));
                    ftext = Wtf.getCmp('folderview').getNodeById(treeObj.nodeid).getUI().getTextEl().innerHTML;
                    this.portalmailPanel.displayFoldersWindow(treeObj.nodeid, ftext);
                    break;
                case '2':
                    Wtf.getCmp('emails').setFromText(WtfGlobal.getLocaleText('pm.personalmessages.fromto')+':', WtfGlobal.getLocaleText('pm.personalmessages.receivedsent')+':');
                    _cm.setColumnHeader(2, WtfGlobal.getLocaleText('pm.personalmessages.fromto'));
                    _cm.setColumnHeader(3, WtfGlobal.getLocaleText('pm.personalmessages.receivedsent'));
                    ftext = Wtf.getCmp('folderview').getNodeById(treeObj.nodeid).getUI().getTextEl().innerHTML;
                    this.portalmailPanel.displayFoldersWindow(treeObj.nodeid, ftext);
                    break;
                case '1':
                    Wtf.getCmp('emails').setFromText(WtfGlobal.getLocaleText('pm.common.TO')+':', WtfGlobal.getLocaleText('pm.personalmessages.savedon')+':');
                    _cm.setColumnHeader(2, WtfGlobal.getLocaleText('pm.common.TO'));
                    _cm.setColumnHeader(3, WtfGlobal.getLocaleText('pm.personalmessages.senton'));
                    ftext = Wtf.getCmp('folderview').getNodeById(treeObj.nodeid).getUI().getTextEl().innerHTML;
                    this.portalmailPanel.displayFoldersWindow(treeObj.nodeid, ftext);
                    break;
                default:
                    Wtf.getCmp('emails').setFromText(WtfGlobal.getLocaleText('pm.personalmessages.from.text')+':', WtfGlobal.getLocaleText('pm.personalmessages.receivedon')+':');
                    _cm.setColumnHeader(2, WtfGlobal.getLocaleText('pm.personalmessages.from.text'));
                    _cm.setColumnHeader(3, WtfGlobal.getLocaleText('pm.personalmessages.receivedon'));
                    ftext = Wtf.getCmp('folderview').getNodeById(treeObj.nodeid).getUI().getTextEl().innerHTML.split("<");
                    this.portalmailPanel.displayFoldersWindow(treeObj.nodeid, ftext[0]);
                    break;
            }
            this.portalmailPanel.MessagePanel1.setData1("", "", "", "", "../../images/blank.png");
            treeObj.getNodeById(treeObj.nodeid).select();
        }
    },
    
    EditClick: function(obj){
        var nodeid = obj;
        var _folderView = Wtf.getCmp('folderview').getNodeById(nodeid);
        _folderView.select();
        var ftext = _folderView.getUI().getTextEl().innerHTML.split("<");
        
        _folderView.getUI().getTextEl().setAttribute("pmnode", WtfGlobal.getLocaleText('pm.personalmessages.notclickable'));
        _folderView.setText('<input id="temp2" type="textbox" style="width: 80px;"/>');
        var _temp2 =document.getElementById('temp2');
        _temp2.value = ftext[0];
        _temp2.focus();
        _temp2.onkeyup = EditKeyCheck;
        _temp2.onblur = callEditSaveFolder;
        
        function EditKeyCheck(e){
            var keyID = (window.event) ? event.keyCode : e.keyCode;
            switch (keyID) {
                case 13:
                    callEditSaveFolder();
                    break;
                case 27:
                    callEditEscapeFunction();
                    break;
            }
        }
        
        function callEditEscapeFunction(){
            var txtObject = document.getElementById('temp2');
            
            txtObject.parentNode.innerHTML = ftext[0];
        }
        
        function callEditSaveFolder(){
            var txtObj = document.getElementById('temp2');
            var foldernametext = txtObj.value.trim();
            if (foldernametext == '') {
                txtObj.parentNode.innerHTML = ftext[0];
            }
            else {
                if (ftext[0] == foldernametext) {
                    txtObj.parentNode.innerHTML = ftext[0];
                }
                else {
                    //Save in db
                    Wtf.Ajax.requestEx({
                        url: Wtf.req.prt + "getPageCount.jsp",
                        params: {
                            flag: "editfolder",
                            folderid: nodeid,
                            foldername: foldernametext,
                            loginid: loginid
                        }
                    }, this, function(result, req){
                        if (result == "-1") {
                            txtObj.parentNode.innerHTML = ftext[0];
                            msgBoxShow(143, 1);
                            //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Folder with this name already exists');
                        }
                        else 
                            if (result == "-2") {
                            
                                txtObj.parentNode.innerHTML = ftext[0];
                                msgBoxShow(4, 1);
                                //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occurred while connecting to the server');
                            }
                            else {
                            
                                txtObj.parentNode.innerHTML = foldernametext;
// Import				//Wtf.getCmp(nodeid).setText(foldernametext);
                            }
                    });
                }
            }
            Wtf.getCmp('folderview').getNodeById(nodeid).getUI().getTextEl().setAttribute("pmnode", WtfGlobal.getLocaleText('pm.personalmessages.text'));
        };
    },
    
    DeleteClick: function(obj){
        var nodeid = obj;
        Wtf.getCmp('folderview').getNodeById(nodeid).getUI().getTextEl().setAttribute("pmnode", WtfGlobal.getLocaleText('pm.personalmessages.notclickable'));
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.348'), function(btn){
            if (btn == 'yes') {
                //Save in db
                Wtf.Ajax.requestEx({
                    url: Wtf.req.prt + "getPageCount.jsp",
                    params: {
                        flag: "deletefolder",
                        folderid: nodeid
                    }
                }, this, function(result, req){
                    if (result == obj) {
                        Wtf.menu.MenuMgr.get("portalmail_actionMenu").remove(Wtf.getCmp(nodeid));
                        Wtf.getCmp('folderview').getNodeById(nodeid).remove();
                        this.portalmailPanel.dst.loadForum("0", "fetch", loginid);
                        Wtf.getCmp("folderview").getNodeById("0").select();
                    }
                    else {
                        msgBoxShow(4, 1);
                        //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occurred while connecting to the server');
                    }
                });
            }
        }, this);
    },

    attachpanel: function(obj){
        treeObj.nodeid = obj.id;
        if (obj.getUI().getTextEl().getAttribute("pmnode") != WtfGlobal.getLocaleText('pm.personalmessages.notclickable')) {
            var loadMsgs = (Wtf.get('tabmailtab') != null);
            if(!treeObj.tabless){
                mainPanel.loadTab("../../mail.html", "   mailtab", WtfGlobal.getLocaleText('pm.personalmessages.text'), "navareadashboard", Wtf.etype.pmessage, false);
            }
            if (loadMsgs) {
                treeObj.displayMailWindow();
            }
        }
    },

    addExistingFolders: function(){
        //***************** Take user folders ******************************
        Wtf.Ajax.requestEx({
            url: Wtf.req.prt + "getmailfolders.jsp",
            params: {
                loginid: loginid
            }
        }, this, function(result, req){
            var nodeobj = eval("(" + result + ")");
            for (var j = 0; j < nodeobj.length; j++) {
                var folderid = nodeobj[j].folderid;
                var foldernametext = nodeobj[j].foldername;
                Wtf.menu.MenuMgr.get("portalmail_actionMenu").add({
                    text: foldernametext,
                    id: folderid,
                    tooltip: {
                        text: WtfGlobal.getLocaleText('pm.Help.pmextra') + '\'' + foldernametext + '\''
                    },
                    icon: "lib/resources/images/default/tree/folder.gif"
                });

                treeObj.temptreenode = new Wtf.tree.TreeNode({
                    text: foldernametext,
                    allowDrag: false,
                    leaf: true,
                    id: folderid,
                    icon: 'lib/resources/images/default/tree/folder.gif',
                    uiProvider: Wtf.tree.NewFolderUI
                });

                Wtf.getCmp('folderview').getNodeById('folders').appendChild(treeObj.temptreenode);
                Wtf.getCmp('folderview').getNodeById('folders').expand();
                treeObj.temptreenode.getUI().getTextEl().setAttribute("pmnode", WtfGlobal.getLocaleText('pm.personalmessages.text'));
                treeObj.temptreenode.on('click', treeObj.attachpanel,treeObj);
                treeObj.temptreenode.on('edclick', this.EditClick,treeObj);
                treeObj.temptreenode.on('delclick', this.DeleteClick,treeObj);
                treeObj.temptreenode = null;
            }
        });
    },

    initComponent: function(){
        Wtf.MailLeftTree.superclass.initComponent.call(this);
        treeObj = this;
        function _createNode(nodeText, nodeID, canDrag, isLeaf, nodeIcon, qtip){
            return new Wtf.tree.TreeNode({
                text: nodeText,
                id: nodeID,
                allowDrag: canDrag,
                leaf: isLeaf,
                iconCls: nodeIcon,
                qtip: qtip
            });
        };
        
        var root1 = new Wtf.tree.AsyncTreeNode({
            text: '',
            expanded: true
        });
        var rootNode = new Wtf.tree.TreeNode({
            text: WtfGlobal.getLocaleText('pm.personalmessages.text'),
            id: "PM",
            allowDrag: false,
            singleClickExpand: true,
            expanded: true
        });
        inbox = new Wtf.tree.TreeNode({
            text: WtfGlobal.getLocaleText('pm.personalmessages.inbox'),
            id: '0',
            allowDrag: false,
            iconCls: 'pwnd inbox',
            qtip: WtfGlobal.getLocaleText('pm.Help.pminbox')
        });
        outbox = _createNode(WtfGlobal.getLocaleText('pm.personalmessages.sent'), '1', false, true, 'pwnd send_item', WtfGlobal.getLocaleText('pm.Help.pmsent'));
        deleteditems = _createNode(WtfGlobal.getLocaleText('pm.personalmessages.deleted'), '2', false, true, 'pwnd delicon',  WtfGlobal.getLocaleText('pm.Help.pmdeleted'));
        drafts = _createNode(WtfGlobal.getLocaleText('pm.personalmessages.drafts'), '3', false, true, 'pwnd mail_generic',  WtfGlobal.getLocaleText('pm.Help.pmdrafts'));
        starreditems = _createNode(WtfGlobal.getLocaleText('pm.personalmessages.flagged'), '4', false, true, 'dpwnd flag_item',  WtfGlobal.getLocaleText('pm.Help.pmflagged'))
        folders = new Wtf.tree.TreeNode({
            text: WtfGlobal.getLocaleText("pm.personalmessages.folders.text") + ' ' + '<a id="leftTreeAnchor" href="#"><span  pmnod='+WtfGlobal.getLocaleText('pm.personalmessages.notclickable')+'><img style="vertical-align:text-top;" src="../../images/btn_add_quick.gif" style="height:12px;" wtf:qtip="'+WtfGlobal.getLocaleText("pm.personalmessages.addfolder")+'" /></span></a>',
            allowDrag: false,
            id: 'folders',
            iconCls: 'dpwnd add_folder',
            singleClickExpand: false,
            qtip :  WtfGlobal.getLocaleText('pm.Help.pmfolders')
        });
        
        this.setRootNode(root1);
        root1.appendChild(rootNode);
        rootNode.appendChild([inbox, outbox, deleteditems, drafts, starreditems, folders]);
        treeObj.nodeid=0; //default node id set to inbox
        folders.addListener('click', function(){
            return false;
        });
    },

    afterRender: function(){
        Wtf.MailLeftTree.superclass.afterRender.call(this);
        document.getElementById('leftTreeAnchor').onclick = this.addFolder;
        this.setEvents();
        this.addExistingFolders();
        Wtf.each(['0', '1', '2', '3', '4'], function(i){
            this.getNodeById(i).on('click', this.attachpanel,this);
        }, this);
        this.body.dom.style.overflow = 'hidden';
    },

    addFolder: function(){

        if (treeObj.temptreenode == null) {
            treeObj.temptreenode = new Wtf.tree.TreeNode({
                allowDrag: false,
                leaf: true,
                id: 'TreeNode',
                icon: 'lib/resources/images/default/tree/folder.gif',
                uiProvider: Wtf.tree.NewFolderUI
            });

            treeObj.temptreenode.setText('<input id="temp1" type="textbox" style="width:80px;"/>');

            treeObj.temptreenode.on('click', function(){
                return false;
            });
            folders.appendChild(treeObj.temptreenode);
            folders.expand();
            treeObj.temptreenode.getUI().getTextEl().setAttribute("pmnode", WtfGlobal.getLocaleText('pm.personalmessages.notclickable'));
        }
        if(folders.childNodes.length>1)
            document.getElementById('temp1').focus();
        document.getElementById('temp1').onblur = callSaveFolder;
        document.getElementById('temp1').onkeyup = KeyCheck;

        function handleExpand(nd){
            //alert('df');
            if(document.getElementById('temp1')!=null){
                document.getElementById('temp1').focus();
                document.getElementById('temp1').onblur = callSaveFolder;
            }
            //folders.removeListener('expand',handleExpand,folders);
        }

        function KeyCheck(e){
            var keyID = (window.event) ? event.keyCode : e.keyCode;
            switch (keyID) {
                case 13:
                    callSaveFolder();
                    break;
                case 27:
                    callEscapeFunction();
                    break;
            }
        }

        function callEscapeFunction(){
            Wtf.getCmp('folderview').getNodeById('TreeNode').remove();
            treeObj.temptreenode = null;
        }


        function callSaveFolder(){
            var foldernametext = document.getElementById('temp1').value.trim();
            foldernametext = WtfGlobal.HTMLStripper(foldernametext);
            if (foldernametext == '') {
                Wtf.getCmp('folderview').getNodeById('TreeNode').remove();
                treeObj.temptreenode = null;
            }
            else {
                //Save in db
                Wtf.Ajax.requestEx({
                    url: Wtf.req.prt + "getPageCount.jsp",
                    params: {
                        flag: "savefolder",
                        loginid: loginid,
                        foldername: foldernametext
                    }
                }, this, function(result, req){
                    if (result == "-1") {
                        Wtf.getCmp('folderview').getNodeById('TreeNode').remove();
                        msgBoxShow(143, 1);
                        //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Folder with this name already exist');
                    }
                    else 
                        if (result == "-2") {
                            Wtf.getCmp('folderview').getNodeById('TreeNode').remove();
                            msgBoxShow(4, 1);
                            //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occurred while connecting to the server');
                        }
                        else {
                            Wtf.menu.MenuMgr.get("portalmail_actionMenu").add({
                                text: foldernametext,
                                id: result,
                                icon: "lib/resources/images/default/tree/folder.gif"
                            });
                            Wtf.getCmp('folderview').getNodeById('TreeNode').remove();
                            treeObj.temptreenode = new Wtf.tree.TreeNode({
                                allowDrag: false,
                                leaf: true,
                                id: result,
                                icon: 'lib/resources/images/default/tree/folder.gif',
                                text: foldernametext,
                                uiProvider: Wtf.tree.NewFolderUI
                            });
                            
                            
                            Wtf.getCmp('folderview').getNodeById('folders').appendChild(treeObj.temptreenode);
                            Wtf.getCmp('folderview').getNodeById('folders').expand();
                            treeObj.temptreenode.getUI().getTextEl().setAttribute("pmnode", WtfGlobal.getLocaleText('pm.personalmessages.text'));
                            treeObj.temptreenode.on('click', treeObj.attachpanel, treeObj);
                            treeObj.temptreenode.on('edclick', treeObj.EditClick, treeObj);
                            treeObj.temptreenode.on('delclick', treeObj.DeleteClick, treeObj);
                        }
                    treeObj.temptreenode = null;
                }, function(result, req){
                    treeObj.temptreenode = null;
                });
            }
            treeObj.temptreenode = null;
        };
            }
});
/*  Left Tree Menu: End   */

/*  WtfPMsgTreeUI: Start    */
Wtf.tree.NewFolderUI = function(node){
    this.node = node;
    this.rendered = false;
    this.animating = false;
    this.wasLeaf = true;
    this.addEvents = {
        'edclick': true,
        'delclick': true
    }
    this.ecc = 'x-tree-ec-icon x-tree-elbow';
};

Wtf.extend(Wtf.tree.NewFolderUI, Wtf.tree.TreeNodeUI, {
    renderElements: function(n, a, targetNode, bulkRender){
    
        this.indentMarkup = n.parentNode ? n.parentNode.ui.getChildIndent() : '';
        var cb = typeof a.checked == 'boolean';
        var href = a.href ? a.href : Wtf.isGecko ? "" : "#";
        var buf = ['<li class="x-tree-node"><table wtf:tree-node-id="', n.id, '" class="x-tree-node-el x-tree-node-leaf ', a.cls, '" cellspacing="0" cellpadding="0" ><tbody><tr><td>', this.indentMarkup, '</td><td ><img src="', Wtf.BLANK_IMAGE_URL, '" class="x-tree-ec-icon x-tree-elbow" /><img src="', a.icon || this.emptyIcon, '" class="x-tree-node-icon', (a.icon ? " x-tree-node-inline-icon" : ""), (a.iconCls ? " " + a.iconCls : ""), '" unselectable="on" /></td><td class="chip"><a hidefocus="on"  class="x-tree-node-anchor" tabIndex="1" ', a.hrefTarget ? ' target="' + a.hrefTarget + '"' : "", '>', '<span  unselectable="on">', n.text, '</span></td><td onmousedown="" ><img wtf:qtip="'+WtfGlobal.getLocaleText("pm.personalmessages.editfolder")+'" id="edit', n.id, '" style="cursor: pointer;" do="edit" src="../../images/edit12.gif"/><img wtf:qtip="'+WtfGlobal.getLocaleText("pm.personalmessages.deletefolder")+'" id="del', n.id, '" style="cursor: pointer;" do="delete" src="../../images/stop12.gif"/></td></tr></tbody></table><ul class="x-tree-node-ct" style="display:none;"></ul></li>'].join('');
       
        var index = 2;
        this.wrap = Wtf.DomHelper.insertHtml("beforeEnd", n.parentNode.ui.ctNode, buf);
        
        
        this.elNode = this.wrap.childNodes[0].childNodes[0].rows[0];
        this.ctNode = this.wrap.parentNode;
        var cs = this.elNode.childNodes;
        
        this.indentNode = cs[0].firstChild;
        this.ecNode = cs[1].firstChild;
        this.iconNode = cs[1].lastChild;
        
        this.anchor = cs[index].firstChild;
        
        this.textNode = cs[index].firstChild.firstChild;
        Wtf.get("edit" + n.id).on("click", function(){
            this.fireEvent("edclick", this.node.id);
        }, this);
        Wtf.get("del" + n.id).on("click", function(){
            this.fireEvent("delclick", this.node.id);
        }, this);
    }
    
    
});
/*  WtfPMsgTreeUI: End  */
