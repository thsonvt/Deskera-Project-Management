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
function folderClick(item, e){
    var folderid = item.id;
    //DOC: portalmail_folderid - is msg source folder id
    if(!(portalmail_folderid==folderid)){
        if(portalmail_sm1.getSelections().length>0){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.342'), function(btn){
                if (btn == 'yes') {
                    UpdateFolderID(folderid);
                }
            });
        } else {
            msgBoxShow(145, 1);
             //Wtf.Msg.alert('Alert', 'Please select a message');
        }
    } else {
        msgBoxShow(146, 1);
         //Wtf.Msg.alert('Alert', 'Source and destination folders are same');
    }
}

//******************** Action menu for main toolbar *********************
portalmail_actionMenu = new Wtf.menu.Menu({
    id: 'portalmail_actionMenu',
    items: [{
        text: WtfGlobal.getLocaleText('pm.personalmessages.inbox'),
        id: "0",
        icon: "../../images/inbox.png"
    }/*,{
        text: WtfGlobal.getLocaleText('pm.personalmessages.drafts'),
        id: "3",
        icon: "../../images/mail_generic.png"
    }*/,new Wtf.menu.Separator({})]
});
portalmail_actionMenu.on('itemclick', folderClick);

//*************************************************************
portalmail_sm1 = new Wtf.grid.CheckboxSelectionModel();
//dojo.cometd.subscribe("/"+loginname+"/inbox", this, "inboxPublishHandler");
//***************************************************************
function loadMailDstore(details,Attachment,id){
    for (var i = 0; i < dst.getCount(); i++) {
        var record = dst.getAt(i);
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
}
function rowDeselect(sm, ri, rec) {
    MessagePanel1.setData1("", "", "", "", "../../images/blank.png");
    if(sm.getSelections().length ==  0){
        Wtf.getCmp('emails').clearContents();
        Wtf.getCmp("btndelete").disable();
        Wtf.getCmp("MoveFolders").disable();
        Wtf.getCmp("btnmoreactions").disable();
        Wtf.getCmp("btnreplyto").disable();
    }
    if(sm.getSelections().length ==  1){
        rowSelectionChange(sm, ri, rec);
    }        
}
function rowSelectionChange(sm,rowIndex,rec){
    /*if(sm.getSelections().length > 0 ){
        Wtf.getCmp("btndelete").enable();
        Wtf.getCmp("MoveFolders").enable();
        Wtf.getCmp("btnmoreactions").enable();
    }*/
    updateButtonStatus(sm.getSelections().length);
    if(sm.getSelections().length > 1 ) {
            Wtf.getCmp("btnreplyto").disable();
            MessagePanel1.setData1("", "", "", "", "../../images/blank.png");
    }else{
        var details = dst.getAt(rowIndex).get('post_text')+dst.getAt(rowIndex).get('Attachment');
        MessagePanel1.setData1("","","",'<div class="loading-indicator">&#160;'+WtfGlobal.getLocaleText("pm.loading.text")+'...</div>',"");
        MessagePanel1.setData(dst.getAt(rowIndex).get('post_subject'), dst.getAt(rowIndex).get('post_fullname'), 
                              /*dst.getAt(rowIndex).get('post_time').format('Y-m-d h:i a'),*/dst.getAt(rowIndex).get('post_time').format(WtfGlobal.getDateFormat()),
                              dst.getAt(rowIndex).get('imgsrc'),dst.getAt(rowIndex).get('senderid'),dst.getAt(rowIndex).get('deskSuperuser'));
        MessagePanel1.messageId=dst.getAt(rowIndex).get('post_id');
        if (details == "") {
            MessagePanel1.topicstore.loadForum(dst.getAt(rowIndex).get('post_id'), "-1", "mail","");
        }
        else 
            MessagePanel1.loadCacheData(details);
    }
}

function inboxPublishHandler(msg) {
    if(portalmail_folderid==0){
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
    
}
var snippet = '<div><img src="{imgico}"/><div class="snippet">{title}</div><br/><span  class="txt">{descp}</span></div>';
var tpl;
tpl = new Wtf.Template(snippet);
tpl.compile();

//************************* Reply Window ************************************
function createReplyWindow(){
    if(portalmail_sm1.getSelections().length==1){
//        dst.getAt(rowIndex).get('deskSuperuser')
        var record = portalmail_sm1.getSelected();
        if(record.data.deskSuperuser=='true'){
            msgBoxShow(173, 1);
            return;
        }
        wind = new Wtf.ReplyWindow({
            uLabel: 'Reply To',
            bLabel: WtfGlobal.getLocaleText('lang.subject.text'),
            tdisabled: true,
            title:WtfGlobal.getLocaleText('pm.personalmessages.reply'),
            id:'replywin'+Math.random(),
            closable: true,
            layout: 'fit',
            replytoId: record.data['post_id'],
            userId: loginid,
            groupId: "",
            firstReply: "",
            uFieldValue: record.data['poster_id'],
            details : "<br><br><br><br><br><br><br><br><-----------------Original Message-----------------><br><br><br>On "+record.data['post_time']+", "+record.data['post_fullname']+" wrote: <br><br>"+ WtfGlobal.URLDecode(record.data['post_text']),
            bFieldValue: "Re: "+ WtfGlobal.URLDecode(record.data['post_subject']),
            type: "Mail",
            sendFlag:"reply",
            fid:portalmail_folderid,
            composeMail:5
        });
        wind.insertStore.on("load", handleInsertMail);
       // wind.show();
        portalmail_mainPanel.add(wind).show();
    } else {
        msgBoxShow(145, 1);
         //Wtf.Msg.alert('Alert', 'Please select a message');
    }
   
}

function createReplyWindowForPMsg(postid){
    var ds = Wtf.getCmp('grid123').getStore();
    var record = ds.getAt(ds.find('post_id', postid));
    if(record.get('deskSuperuser')=='true'){
            msgBoxShow(173, 1);
            return;
        }
    wind = new Wtf.ReplyWindow({
        uLabel: 'Reply To',
        bLabel: WtfGlobal.getLocaleText('lang.subject.text'),
        tdisabled: true,
        replytoId: postid,
        userId: loginid,
        title:WtfGlobal.getLocaleText('pm.personalmessages.reply'),
        id:'replywin'+Math.random(),
        id:'replywin'+Math.random(),
        closable: true,
        layout: 'fit',
        groupId: "",
        firstReply: "",
        uFieldValue: record.get('poster_id'),
        bFieldValue: "Re:"+ WtfGlobal.URLDecode(record.get('post_subject')),
        type: "Mail",
        details:"<br><br><br><br><br><br><br><br><-----------------Original Message-----------------><br><br><br>On "+record.get('post_time')+", "+record.get('post_fullname')+" wrote: <br><br>"+ WtfGlobal.URLDecode(record.get('post_text')),
        sendFlag: "reply",
        fid:portalmail_folderid,
        composeMail:5
    });
    wind.insertStore.on("load", handleInsertMail);
    portalmail_mainPanel.add(wind).show();
}

function handleInsertMail(obj, rec, opt){
    if(rec[0]){
        if (rec[0].data['Success'].match('Success')) {
            msgBoxShow(141, 0);
            //Wtf.Msg.alert('Message Sent', WtfGlobal.getLocaleText('pm.msg.141'));
            this.panelObj.handleClose();
        }
        else if (rec[0].data['Success'].match('Fail')) {
                msgBoxShow(142, 1);
                //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.142'));
        }  
        else if (rec[0].data['Success'].match('Draft')) {
                msgBoxShow(147, 0);
                //Wtf.Msg.alert('Saved', WtfGlobal.getLocaleText('pm.msg.147'));
                this.panelObj.handleClose();
        }  
        else if (rec[0].data['Success'].match('userfail')) {
                msgBoxShow(['Delivery Failure', 'Message to user '+rec[0].data[WtfGlobal.getLocaleText('lang.subject.text')] +' is invalid.'], 1);
                //Wtf.Msg.alert('Delivery Failure', 'Message to user '+rec[0].data[WtfGlobal.getLocaleText('lang.subject.text')] +' is invalid.');
        }  
     }   
   // Wtf.getCmp('winMsgSend').close();
}

//*************************Mail Toolbar**************************************

    function handleCompose(){
       var  composeWind=new Wtf.ReplyWindow({
            uLabel:WtfGlobal.getLocaleText('pm.common.TO'),
            bLabel:WtfGlobal.getLocaleText('lang.subject.text'),
            tdisabled:false,
            replytoId:'-1',
            userId:loginid,
            groupId:"",
            id:'replywin'+Math.random(),
            title:WtfGlobal.getLocaleText('pm.personalmessages.compose'),
            closable: true,
            firstReply:"",
            uFieldValue:"",
            bFieldValue:"",
            type:"Mail",
            sendFlag:"newmsg",            
            composeMail:1,
            mailDS:Wtf.getCmp('grid123').getStore()
        });
        composeWind.insertStore.on("load", handleInsertMail);
        portalmail_mainPanel.add(composeWind).show();
        composeWind.doLayout();
}
function createMailFunctions(){
    return ([new Wtf.Action({
        //TODO: temp location for button .. suggest location for the same [charu]
        text: WtfGlobal.getLocaleText('pm.personalmessages.compose'),
        id: 'compMail',
        handler: function(){
            handleCompose()
        },
        tooltip: {
            title: WtfGlobal.getLocaleText('lang.compose.text'),
            text: WtfGlobal.getLocaleText('pm.personalmessages.compose.new')
        },
        iconCls: 'pwnd compose'
    }), new Wtf.Action({
        text: WtfGlobal.getLocaleText('pm.personalmessages.reply'),
        id: 'btnreplyto',
        handler: function(){
            createReplyWindow()
        },
        tooltip: {
            title: WtfGlobal.getLocaleText('pm.personalmessages.reply'),
            text: WtfGlobal.getLocaleText('pm.personalmessage.reply.selected')
        },
        iconCls: 'pwnd outbox'
    }), new Wtf.Action({
        text: WtfGlobal.getLocaleText('lang.delete.text'),
        handler: DeleteMails,
        id: 'btndelete',
        tooltip: {
            title: WtfGlobal.getLocaleText('pm.personalmessage.delete.text'),
            text: WtfGlobal.getLocaleText('pm.personalmessage.delete')            
        },
        iconCls: 'pwnd deliconwt'
    }), moveto = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText('pm.personalmessages.moveto'),
        iconCls: 'pwnd sendmsg',
        tooltip: {
            title: WtfGlobal.getLocaleText('pm.personalmessage.move'),
            text: WtfGlobal.getLocaleText('pm.personalmessage.moveselected')
        },
        id: 'MoveFolders',
        menu: portalmail_actionMenu
    }), moreactions = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText('pm.personalmessages.moreactions'),
        iconCls: 'dpwnd settings',
        tooltip: {
            title: WtfGlobal.getLocaleText('pm.personalmessages.moreactions'),
            text: WtfGlobal.getLocaleText('pm.common.moreaction')
        },
        id: 'btnmoreactions',
        menu: {
            items: [{
                text: 'Add flag',
                handler: addstarClick,
                icon: "../../images/FlagRed16.png"
            }, {
                text: 'Remove flag',
                handler: removestarClick,
                icon: "../../images/FlagGrey16.png"
            }]
        }
    }), /*/"Search: ",*/ new Wtf.form.TextField({
        id: "mailsearchtextbox",
        emptyText: WtfGlobal.getLocaleText('lang.search.text'),
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
}

//Mail Toolbar For Particular Message
function createMailToolbar(postid){
    var actionarr = Array();
    
    if (portalmail_folderid != '2') {
        //******************** Action menu for perticular msg toolbar *********************
        var portalmail_actionMenuForPMsg = new Wtf.menu.Menu({
            id: 'portalmail_actionMenuForPMsg' + postid,
            items: [{
                text: 'Drafts    ',
                handler: function(){
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.342'), function(btn){
                        if (btn == 'yes') {
                            UpdateFolderIDForPerMsg(postid, 3);
                            //Wtf.Msg.alert('Message Move', 'Message has been moved successfully.');
                        }
                    })
                },
                icon: "../../images/mail_generic.png"
            }]
        });
        
        if (portalmail_folderid == '0' || (portalmail_folderid != '1' && portalmail_folderid != '4')) {
            actionarr.push(new Wtf.Action({
                text: WtfGlobal.getLocaleText('pm.personalmessages.reply'),
                id: 'btnreplyto1',
                handler: function(){
                    createReplyWindowForPMsg(postid)
                },
                tooltip: {
                    title: WtfGlobal.getLocaleText('pm.personalmessages.reply'),
                    text: WtfGlobal.getLocaleText('pm.personalmessage.reply.selected')
                },
                iconCls: 'pwnd outbox'
            }))
        }
        
        actionarr.push(new Wtf.Action({
            text: WtfGlobal.getLocaleText('lang.delete.text'),
            handler: function(){
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.350'), function(btn){
                    if (btn == 'yes') {
                        UpdateFolderIDForPerMsg(postid, 2);
                        msgBoxShow(148, 0);
                        
                    }
                }, this);
            },
            id: 'btndelete1',
            tooltip: {
                title: WtfGlobal.getLocaleText('pm.personalmessage.delete.text'),
                text: WtfGlobal.getLocaleText('pm.personalmessage.delete')
            },
            iconCls: 'pwnd delicon'
        }))
        
        actionarr.push(moveto1 = new Wtf.Toolbar.MenuButton({
            text: WtfGlobal.getLocaleText('pm.personalmessages.moveto'),
            iconCls: 'pwnd sendmsg',
            tooltip: {
                title: WtfGlobal.getLocaleText('pm.personalmessage.move'),
                text: WtfGlobal.getLocaleText('pm.personalmessage.moveselected')
            },
            id: 'MoveFolders' + postid,
            menu: portalmail_actionMenuForPMsg
        }))
    }
    else {
        actionarr.push(new Wtf.Action({
            text: WtfGlobal.getLocaleText('lang.delete.forever'),
            id: 'btndelforever1',
            handler: function(){
                deleteMsgForever(postid)
            },
            tooltip: {
                title: WtfGlobal.getLocaleText('pm.personalmessage.delete.forever'),
                text: WtfGlobal.getLocaleText('pm.project.resources.delete.forever')
            },
            iconCls: 'pwnd delicon'
        }))
        
        actionarr.push(new Wtf.Action({
            text: WtfGlobal.getLocaleText('pm.personalmessage.restore.text'),
            id: 'btnrestoremsg1',
            handler: function(){
                RestoreMsg(postid)
            },
            tooltip: {
                title: WtfGlobal.getLocaleText('pm.personalmessage.restore.text'),
                text: WtfGlobal.getLocaleText('pm.personalmessages.restore')
            },
            iconCls: 'pwnd sendmsg'
        }))
    }
    return actionarr;
}

function addExistingFoldersForMsgMenu(postid){
    if (portalmail_folderid != '2') {
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
                    Wtf.menu.MenuMgr.get("portalmail_actionMenuForPMsg" + postid).add({
                        text: foldernametext,
                        id: folderid,
                        handler: function(e){
                            folderid = e.id;
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.342'), function(btn){
                                if (btn == 'yes') {
                                    UpdateFolderIDForPerMsg(postid, folderid);
                                    //Wtf.Msg.alert('Message Move', 'Message has been moved successfully.');
                                }
                            })
                        },
                        icon: "../../lib/resources/images/default/tree/folder.gif"
                    });
                }
            });
    }
}

function deleteMsgForever(postid){
    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.345'), function(btn){
        if (btn == 'yes') {
            var ds = Wtf.getCmp('grid123').getStore();
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
}

function RestoreMsg(postid){
    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.325'), function(btn){
        if (btn == 'yes') {
            var ds = Wtf.getCmp('grid123').getStore();
             Wtf.Ajax.requestEx({
                            method: 'POST',
                            url: Wtf.req.prt + 'getmail.jsp',
                            params: {
                                mailflag: 'restoremsg',
                                post_id: postid
                            }
                            }, this, function(result, req){
                                 if (postid == result) {
                                        ds.remove(ds.getAt(ds.find('post_id', postid)));
                                        msgBoxShow(149, 0);
                                    }
                                    else {
                                        msgBoxShow(4, 1);
                                    }
                            }, function(result, req){
                                    msgBoxShow(4, 1);
                                 //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occurred while connecting to the server');
                            });
        }
    }, this);
}

function UpdateFolderIDForPerMsg(postid, folder_id){
    var last_folder_id = portalmail_folderid;
    var ds = Wtf.getCmp('grid123').getStore();
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
                                var storeobj = Wtf.getCmp('grid123').getStore();
                                storeobj.reload();
                                /*for (var j = 0; j < nodeobj.data.length; j++) {
                                    storeobj.remove(ds.getAt(ds.find('post_id', nodeobj.data[j].post_id)));
                                    if(Wtf.getCmp("emailsTab" + postid).ownerCt)
                                        Wtf.getCmp("emailsTab" + postid).ownerCt.remove(Wtf.getCmp("emailsTab" + postid));
                                }*/
                        }, function(result, req){
                        });
}

/***********************/

function ImageReturn(data){
    if (data) 
        return "<img id='flagImage' class='starImgDiv' onclick='changeStarImage(this)' star=0 src='../../images/FlagRed.gif'></img>";
    else 
        return "<img id='flagImage' class='starImgDiv' onclick='changeStarImage(this)' star=1 src='../../images/FlagGrey.gif'></img>";
}

/***********************/


//************************************************************
function MsgRead(data, md, rec, rowindex, colindex, store){
    alert(portalmail_grid1.getView().getRow(rowindex).getElementsByTagName('td')[colindex]);
   
}
function beforeRowselect(sm,rowindex,kexisting,rec) {
    var folderid = rec.get("folder");
    if(folderid){
         portalmail_folderid = parseInt(folderid);
    }
}

function onClickHandle1(grid,row,col,e) {    
    if(e.target.id == "flagImage"){
        inboxFlag = true;
    }
     if (portalmail_sm1.getCount() == 1) {
        updateButtonStatus(1);        
        var ds = Wtf.getCmp('grid123').getStore();
        var selArray = [];
        selArray = portalmail_sm1.getSelections();
        var rowobj = selArray[0];
        var rowIndex = ds.find('post_id', rowobj.get('post_id'));
        var folderid = rowobj.get("folder");
        if(folderid){
            portalmail_folderid = parseInt(folderid);
        }
    }
    else 
        if (portalmail_sm1.getCount() == 0) {
            enablemailtoolbarbtns();
            MessagePanel1.setData1("", "", "", "", "../../images/blank.png");
        }
        else 
            if (portalmail_sm1.getCount() > 1) {
                updateButtonStatus(portalmail_sm1.getCount());
                MessagePanel1.setData1("", "", "", "", "../../images/blank.png");
            }
    if(col == 4){
        if(e.target && e.target.tagName!="IMG") {
            var target = e.target;
            var flag = "";
            var targetImg = target.getElementsByTagName('img')[0];
            if (targetImg.src.match('../../images/FlagGrey.gif')) {
                targetImg.src = "../../images/FlagRed.gif";
                targetImg.setAttribute("star", 0);
                flag = 'true';
            }
            else {
                targetImg.src = "../../images/FlagGrey.gif";
                targetImg.setAttribute("star", 1);
                flag = 'false';
            }
            var jsonData = "{data:[";
            jsonData += "{'post_id':'" + encodeURIComponent(rowobj.get('post_id')) + "'}";
            jsonData += "]}";
            Wtf.Ajax.requestEx({
                    method: 'GET',
                    url: Wtf.req.prt + 'getmail.jsp',
                    params: {
                    mailflag: 'starchange',
                    post_id: jsonData,
                    flag: flag
                }
                }, this, function(result, req){
                }, function(result, req){
                });
            } 
    }
}

//***********Temp functions*******************************

function loadingDisplay(str){
    //return '<div style="float: left; width:100%"><div style="float: left;">'+str+'</div><div style="float: right; color: rgb(0,0,0); margin-left: 20px; font-weight: normal;"><img src="../../images/loading.gif" style="width:16px;height:16px; vertical-align:middle" alt="Loading" />&#160;Loading...</div></div>';
    return '<div style="float: left; width:100%"><div style="float: left;">' + str + '</div><div style="float: right; color: rgb(0,0,0); margin-left: 20px; font-weight: normal;">&#160;'+ WtfGlobal.getLocaleText("pm.loading.text") +'...</div></div>';
}

function loadingDisplayNo(str){
    return '<div style="float: left; width:100%"><div style="float: left;">' + str + '</div><div style="float: right; color: rgb(0,0,0); margin-left: 20px; font-weight: normal;">No messages to display</div></div>';
}

//*********************************
function enablemailtoolbarbtns(){
    Wtf.getCmp('btnreplyto').disable();
    //Wtf.getCmp('btnsendmsg').enable() ;
    Wtf.getCmp('btndelete').disable();
    Wtf.getCmp('MoveFolders').disable();
    Wtf.getCmp('btnmoreactions').disable();
}

//***********************************
function displayFoldersWindow(folderid, foldertext){
    enablemailtoolbarbtns();
    document.getElementById('mailsearchtextbox').value = "";
    portalmail_folderid = folderid;
    portalmail_titleflag = foldertext;
    //portalmail_grid1.setTitle(loadingDisplay(portalmail_titleflag));
    portalmail_grid1.setTitle(portalmail_titleflag);
    var view = '';
    
    dst.loadForum(folderid, "fetch", loginid);
    dst.on("loadexception", function exp(){
        //portalmail_grid1.setTitle(portalmail_titleflag);
        msgBoxShow(4, 1);
        //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occurred while connecting to the server');
    });
    
    portalmail_grid1.store = dst;
    view = portalmail_grid1.getView();
    view.refresh();
    dst.on("load", function(a, b, c){
        portalmail_folderid = a.baseParams.flag;
        var view = portalmail_grid1.getView();
        if (b.length == 0) {
            portalmail_grid1.setTitle(loadingDisplayNo(portalmail_titleflag));
        }
        else {
            view.refresh();   
            portalmail_grid1.store.clearGrouping();
        }
       if (portalmail_folderid == '0') {
           portalmail_grid1.store.clearGrouping();
            for (i = 0; i < a.getCount(); i++) {
                if(b[i].data['readflag']==false){
                    view.getCell(i, 1).firstChild.style.fontWeight = "bold";
                    view.getCell(i,  2).firstChild.style.fontWeight = "bold";
                    view.getCell(i,  3).firstChild.style.fontWeight = "bold";
                }
            }
            
        }else if(portalmail_folderid =='4'){
            view.refresh();
            portalmail_grid1.getStore().groupBy("folder");

        }else{
             portalmail_grid1.store.clearGrouping();
        }
    })
}
function sortchange(grid,obj){
    //var store = grid.getStore();
    var count = dst.getCount();
    var recordArr = dst.getRange(0,count);
    var view = portalmail_grid1.getView();
    view.refresh();
    if (portalmail_folderid == '0') {   
        for (i = 0; i < count; i++) {
            if(recordArr[i].data['readflag']==false){
                view.getCell(i, 1).firstChild.style.fontWeight = "bold";
                view.getCell(i,  2).firstChild.style.fontWeight = "bold";
                view.getCell(i,  3).firstChild.style.fontWeight = "bold";
            }
        } 
    }
}
function gridrowDoubleClick(obj, rowIndex, e){
    var ds = Wtf.getCmp('grid123').getStore();
    var postid = ds.getAt(rowIndex).get('post_id');
    var postSub = WtfGlobal.URLDecode(ds.getAt(rowIndex).get('post_subject'));
    if(postSub=="")
        postSub="[No Subject]";
    var tabid = "emailsTab" + postid;
    var tab = portalmail_mainPanel.getComponent(tabid);
    if (tab) {
        portalmail_mainPanel.setActiveTab(tab);
    }
    else {
        var MessagePanel2 = new Wtf.MessagePanel({
            id: "emails" + postid
        });
        if(portalmail_folderid == "3"){
            wind = new Wtf.ReplyWindow({
            uLabel: WtfGlobal.getLocaleText('pm.common.TO'),
            bLabel: WtfGlobal.getLocaleText('lang.subject.text'),
            title:WtfGlobal.getLocaleText('pm.personalmessages.draft'),
            tabWidth:150,
            closable:true,
            tdisabled: false,
            //replytoId: portalmail_sm1.getSelected().data['poster_id'],
            replytoId:'-1',
            id:'replywin'+Math.random(),
            userId: loginid,
            groupId: "",
            firstReply: "",
            uFieldValue: portalmail_sm1.getSelected().data['poster_id'],
            bFieldValue: WtfGlobal.URLDecode(portalmail_sm1.getSelected().data['post_subject']),
            type: "Mail",
            sendFlag: "newmsg",
            composeMail:1,
            fid:portalmail_folderid,
            details:portalmail_sm1.getSelected().data['post_text']
        });
            wind.insertStore.on("loadsuccess", handleInsertMail);
           // wind.show();
            portalmail_mainPanel.add(
                wind
                ).show();
        }
        else{
            portalmail_mainPanel.add({
                id: "emailsTab" + postid,
                title: postSub,
                closable: true,
                layout: 'fit',
                tbar: createMailToolbar(postid),
                items: MessagePanel2
            }).show();
            MessagePanel2.setData1("", "", "", '<div class="loading-indicator">&#160;'+WtfGlobal.getLocaleText("pm.loading.text")+'...</div>', "");
            MessagePanel2.setData(ds.getAt(rowIndex).get('post_subject'), ds.getAt(rowIndex).get('post_fullname'), ds.getAt(rowIndex).get('post_time').format('Y-m-d h:i a'), ds.getAt(rowIndex).get('imgsrc'),ds.getAt(rowIndex).get('senderid'),dst.getAt(rowIndex).get('deskSuperuser'));
            if (portalmail_folderid == '1') 
                MessagePanel2.setFromText('To:', 'Sent on:');
            else 
                MessagePanel2.setFromText('From:', WtfGlobal.getLocaleText('pm.personalmessages.receivedon')+':');
            var detail = ds.getAt(rowIndex).get('post_text')+ds.getAt(rowIndex).get('Attachment');    
            if (detail == "") {
                MessagePanel2.messageId = postid;
                MessagePanel2.topicstore.loadForum(ds.getAt(rowIndex).get('post_id'), "-1", "mail","");
            }
            else 
                MessagePanel2.loadCacheData(detail);
            addExistingFoldersForMsgMenu(postid);
        }
    }
}

function DeleteMails(){
    if(portalmail_sm1.getSelections().length>0){
        var delstr=WtfGlobal.getLocaleText('pm.msg.350');
        if(portalmail_folderid==2)
            delstr=WtfGlobal.getLocaleText('pm.msg.345');
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), delstr, function(btn){
            if (btn == 'yes') {
                UpdateFolderID(2);
            }
        }, this);
    } else {         
         msgBoxShow(145, 1);
         //Wtf.Msg.alert('Alert', 'Please select a message');
    }
    MessagePanel1.setData1("", "", "", "", "../../images/blank.png");
}

function UpdateFolderID(folder_id){
    var last_folder_id = portalmail_folderid;
    
        if(portalmail_sm1.getSelections().length>0){
            var ds = Wtf.getCmp('grid123').getStore();
            var selArray = Array();
            selArray = portalmail_sm1.getSelections();
            var jsonData = "{data:[";
            for (i = 0; i < selArray.length; i++) {
                var rowobj = selArray[i];
                jsonData += "{'post_id':'" + encodeURIComponent(rowobj.get('post_id')) + "'},";
                if(folder_id=='2'){
                    if(Wtf.getCmp("emailsTab" + rowobj.get('post_id'))!=null){
//                         Wtf.getCmp("emailsTab" + rowobj.get('post_id')).ownerCt.remove(Wtf.getCmp("emailsTab" + rowobj.get('post_id')));
                       portalmail_mainPanel.remove(Wtf.getCmp("emailsTab" + rowobj.get('post_id')));
                        //Wtf.getCmp("emailsTab" + rowobj.get('post_id')).destroy();
                    }
                }
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
                        var nodeobj = eval("(" + result + ")");
                        var storeobj = Wtf.getCmp('grid123').getStore();
                        storeobj.reload();
                        /*for (var j = 0; j < nodeobj.data.length; j++) {
                            storeobj.remove(ds.getAt(ds.find('post_id', nodeobj.data[j].post_id)));
                        }*/
                       // Wtf.Msg.alert('Moved', nodeobj.data.length + ' messages have been moved successfully.');
                       MessagePanel1.setData1("", "", "", "", "../../images/blank.png");
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
}



//************************Search Functions********************************

function searchmails(){
    enablemailtoolbarbtns();
    var searchstring = encodeURIComponent(document.getElementById('mailsearchtextbox').value.trim());
    if(searchstring.length > 0){
    //portalmail_grid1.setTitle(loadingDisplay(portalmail_titleflag));
        portalmail_grid1.setTitle("Search results");
        var view = '';
        var folder_id = 0;
        dst.loadSearch(searchstring, folder_id, "searchmails", loginid,mailPageLimit.combo.getValue());

        dst.on("loadexception", function exp(){
            //portalmail_grid1.setTitle(portalmail_titleflag);
            msgBoxShow(4, 1);
            //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occurred while connecting to the server');
        });

        portalmail_grid1.store = dst;
        view = portalmail_grid1.getView();
        view.refresh();
        dst.on("load", function(a, b, c){
            if (b.length == 0) {
                portalmail_grid1.setTitle(loadingDisplayNo(portalmail_titleflag));
            }
            else {
                view.refresh();
                portalmail_grid1.getStore().groupBy("folder");
                //portalmail_grid1.setTitle(portalmail_titleflag); 
            }
        })
        searchFlag=true;
    }
    else{
        if(searchFlag){
            dst.loadForum(portalmail_folderid, "fetch", loginid);
            searchFlag=false;
            dst.on("load", function(a, b, c){
                    var view = portalmail_grid1.getView();
                    view.refresh();
                    portalmail_grid1.store.clearGrouping();
                    if (portalmail_folderid == '0') {   
                        for (i = 0; i < a.getCount(); i++) {
                            if(b[i].data['readflag']==false){
                                view.getCell(i, 1).firstChild.style.fontWeight = "bold";
                                view.getCell(i,  2).firstChild.style.fontWeight = "bold";
                                view.getCell(i,  3).firstChild.style.fontWeight = "bold";
                        }
                    }        
                }
            })
        }
    }
}

//*****************More action functions**************************
function changeStarImage(obj1){
    var grid = Wtf.getCmp('grid123');
//    grid.loadMask.show();
    var rowobj = portalmail_sm1.getSelected();
    var rowindex = grid.store.find('post_id', rowobj.get('post_id'));
    var star = obj1.getAttribute("star");
    if (star == 1)
        flag = 'true';
    else
        flag = 'false';
    
    var jsonData = "{data:[";
    jsonData += "{'post_id':'" + encodeURIComponent(rowobj.get('post_id')) + "'}";
    jsonData += "]}";
    Wtf.Ajax.requestEx({
        method: 'POST',
        url:  '../../jspfiles/portal/getmail.jsp',
        params: {
            mailflag: 'starchange',
            post_id: jsonData,
            flag: flag
        }
    }, this, function(action, req){
        var res = eval("(" + action + ")").data;
            for(var cnt = 0; cnt < res.length; cnt++){
                var s = Wtf.getCmp("grid123").getStore();
                var ri = s.find("post_id", res[cnt].postid);
                var gV = Wtf.getCmp("grid123").getView();
                var img = gV.getCell(ri, 4).firstChild.firstChild;
                if(res[cnt].flag){
                    img.src = '../../images/FlagRed.gif';
                    img.setAttribute("star", 0);
                } else {
                    img.src = '../../images/FlagGrey.gif';
                    img.setAttribute("star", 1);
                }
            }
//            Wtf.getCmp("grid123").getStore().on("load",function(){//                
//            },this);
//            Wtf.getCmp("grid123").getStore().reload();
//            MessagePanel1.setData1("", "", "", "", "../../images/blank.png");                
//            grid.loadMask.hide();
              grid.store.reload();  
//            grid.store.loadRefresh(portalmail_folderid, "fetch", loginid, 0,15);            
    }, function(result, req){
        grid.loadMask.hide();
        msgBoxShow(4, 1);
        //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occurred while connecting to the server');         
    });
    
}

function addstarClick(){
    handleStarChange('true');
}

function removestarClick(){
    handleStarChange('false');
}

function handleContextRestore(){
    RestoreMsg(portalmail_sm1.getSelected().data['post_id']);
}

function handleStarChange(flag){
    if(portalmail_sm1.getSelections().length>0){   
        var grd = Wtf.getCmp('grid123');
        var bt = grd.getBottomToolbar();
        var cCursor = bt.cursor;

        var flag1 = portalmail_folderid;
        var title = portalmail_titleflag;
        //portalmail_grid1.setTitle(loadingDisplay(portalmail_titleflag));

        var selArray = Array();
        selArray = portalmail_sm1.getSelections();
        var jsonData = "{\"data\":[";
        for (i = 0; i < selArray.length; i++) {
            var rowobj = selArray[i];
            jsonData += "{\"post_id\":\"" + encodeURIComponent(rowobj.get('post_id')) + "\"},";
        }
        jsonData = jsonData.substring(0, jsonData.length - 1) + "]}";
        Wtf.Ajax.requestEx({
            method: 'POST',
            url: Wtf.req.prt + 'getmail.jsp',
            params: {
                mailflag: 'starchange',
                flag: flag,
                post_id: jsonData
            }
        }, this, function(result, req){
             dst.loadRefresh(flag1, "fetch", loginid, cCursor);
                dst.on("loadexception", function exp(){
                    //portalmail_grid1.setTitle(portalmail_titleflag);
                    msgBoxShow(4, 1);
                    //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occurred while connecting to the server');
                });

                portalmail_grid1.store = dst;
                view = portalmail_grid1.getView();
                view.refresh();
               /* dst.on("load", function(a, b, c){
                    if (b.length == 0) {
                        portalmail_grid1.setTitle(loadingDisplayNo(portalmail_titleflag));
                    }
                    else {
                        //portalmail_grid1.setTitle(portalmail_titleflag);
                        view.refresh();
                        portalmail_grid1.store.clearGrouping();
                    }
                })*/                                
                MessagePanel1.setData1("", "", "", "", "../../images/blank.png");
        }, function(result, req){
            msgBoxShow(4, 1);
            //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occurred while connecting to the server');
        });
     } else {
         msgBoxShow(145, 1);
         //Wtf.Msg.alert('Alert', 'Please select a message');
     }
}

function updateButtonStatus(count){
    if (portalmail_folderid == '0') {
        if (count == 1) 
            Wtf.getCmp('btnreplyto').enable();
        else 
            Wtf.getCmp('btnreplyto').disable();
        Wtf.getCmp('btndelete').enable();
        Wtf.getCmp('MoveFolders').enable();
        Wtf.getCmp('btnmoreactions').enable();
         Wtf.getCmp('0').disable();
    }
    else if (portalmail_folderid == '2') {
            enablemailtoolbarbtns();
            Wtf.getCmp('btndelete').enable();
    }
    else if (portalmail_folderid == '4') {
            Wtf.getCmp('btnreplyto').disable();
            Wtf.getCmp('btndelete').disable();
            Wtf.getCmp('MoveFolders').disable();
            Wtf.getCmp('btnmoreactions').enable();
    }
    else if (portalmail_folderid == '1') {
            Wtf.getCmp('btnreplyto').disable();
            Wtf.getCmp('btndelete').enable();
            Wtf.getCmp('MoveFolders').disable();
            Wtf.getCmp('btnmoreactions').enable();
    } else if(portalmail_folderid == '3') {
            Wtf.getCmp('btnreplyto').disable();
            Wtf.getCmp('btndelete').enable();
            Wtf.getCmp('MoveFolders').disable();
            //Wtf.getCmp('btnmoreactions').disable();
            Wtf.getCmp('0').enable();
    } else {
            Wtf.getCmp('btnreplyto').enable();
            Wtf.getCmp('btndelete').enable();
            Wtf.getCmp('MoveFolders').enable();
            //Wtf.getCmp('btnmoreactions').disable();
            Wtf.getCmp('0').enable();
    }               
}


function onMailGridContextmenu(grid, rowindex, e){
    portalmail_sm1.selectRow(rowindex);
    var menu = null;
    if (!menu) {
        menu = new Wtf.menu.Menu({
            id: 'context12',
            items: [{
                text: WtfGlobal.getLocaleText('pm.personalmessages.reply'),
                id: 'cntxbtnreplyto',
                handler: createReplyWindow,
                iconCls: 'pwnd outboxCx'
            }, {
                text: WtfGlobal.getLocaleText('lang.delete.text'),
                handler: DeleteMails,
                id: 'cntxbtndelete',
                iconCls: 'pwnd delicon'
            }, {
                text: WtfGlobal.getLocaleText('pm.personalmessage.restore.text'),
                iconCls: 'msgRestore',
                id: 'cntxbtnrestore',
                handler: handleContextRestore
            }, {
                text: WtfGlobal.getLocaleText('pm.personalmessages.moveto'),
                iconCls: 'pwnd sendmsgwt',
                id: 'cntxbtnmoveto',
                // <-- submenu by nested config object
                menu: portalmail_actionMenu
            }, {
                text: WtfGlobal.getLocaleText('pm.personalmessages.moreactions'),
                iconCls: 'dpwnd settingswt',
                id: 'cntxbtnmoreactions',
                // <-- submenu by nested config object
                menu: {
                    items: [{
                        text: 'Add flag',
                        handler: addstarClick,
                        icon: "../../images/FlagRed16.png"
                    }, {
                        text: 'Remove flag',
                        handler: removestarClick,
                        icon: "../../images/FlagGrey16.png"
                    }]
                }
            }]
        })
    }
    menu.showAt(e.getXY());
    e.preventDefault();
    
    updateCntxButtonStatus();
}

function updateCntxButtonStatus(){
    var replyBtn = Wtf.getCmp('cntxbtnreplyto');
    var delBtn = Wtf.getCmp('cntxbtndelete');
    var moveBtn = Wtf.getCmp('cntxbtnmoveto');
    var moreAct = Wtf.getCmp('cntxbtnmoreactions');
    var restBtn = Wtf.getCmp('cntxbtnrestore');
    var zeroBtn =  Wtf.getCmp('0');
    if (portalmail_folderid == '0') {
        replyBtn.enable();
        delBtn.enable();
        moveBtn.enable();
        moreAct.enable();
        restBtn.disable();
        zeroBtn.disable();
    }
    else 
        if (portalmail_folderid == '2') {
            replyBtn.disable();
            delBtn.enable();
            restBtn.enable();
            moveBtn.disable();
            moreAct.disable();
            zeroBtn.disable();
        }
        else 
            if (portalmail_folderid == '4') {
                replyBtn.disable();
                delBtn.disable();
                moveBtn.disable();
                moreAct.enable();
                restBtn.disable();
                zeroBtn.disable();
            }
            else 
                if (portalmail_folderid == '1') {
                    replyBtn.disable();
                    delBtn.enable();
                    moveBtn.disable();
                    moreAct.enable();
                    restBtn.disable();
                    zeroBtn.disable();
                }else if(portalmail_folderid == '3'){
                    replyBtn.disable();
                    delBtn.enable();
                    moveBtn.disable();
                    moreAct.disable();
                    restBtn.disable();
                    zeroBtn.enable();
                }
                else {
                    replyBtn.disable();
                    delBtn.enable();
                    moveBtn.enable();
                    moreAct.disable();
                    restBtn.disable();
                    zeroBtn.enable();
                }
}

/*  Messenger: Start    */

/*************************************************************************
 minimize individual chat win: collapse-anchor-hide-call proxy element
 *************************************************************************/
function minchatwin(objid){
    var tid = String(objid.id).replace("chatWin", "kcont_");
    
    Wtf.getCmp('contactsview').getNodeById(tid).getUI().getTextEl().setAttribute("mstat", "t");
    Wtf.getCmp(objid.id).hide();
}


function handleClick(obj){
    var chatuserstatus = obj.getUI().getTextEl().getAttribute("ustat");
    var un = obj.getUI().getTextEl().innerHTML;
    
    if (obj.getUI().getTextEl().getAttribute("mstat") == "t") {
        var tid = String(obj.id).replace("kcont_", "");
        var showWin = Wtf.getCmp('chatWin' + tid);
        showWin.show();
        obj.getUI().getTextEl().setAttribute("mstat", "f");
    }
    
    if (obj.getUI().getTextEl().getAttribute("status") == "f") {
        obj.getUI().getTextEl().setAttribute("status", "t");
        win1(obj)
    }
}

//function win1(idd, chatuserstatus,un){
function win1(obj){
    var profileId = String(obj.id).replace("kcont_", "");
    var iclass;
    if (obj.getUI().getTextEl().getAttribute("ustat") == "online") {
        iclass = "K-icon";
    }
    else 
        if (obj.getUI().getTextEl().getAttribute("ustat") == "offline") {
            iclass = "K-iconOffline";
        }
    
    // creating the chat window here
    new Wtf.Window({
        layout: 'border',
        width: 500,
        height: 300,
        //id: "win_iddd" + iddd,
        id: "chatWin" + profileId,
        maximizable: true,
        minimizable: true,
        plain: true,
        closable: true,
        closeAction: 'hide',
        iconCls: iclass,
        title: "K-Chat with " + obj.getUI().getTextEl().innerHTML,
        shadow: true,
        items: [{
            region: 'center',
            id: 'northRegion' + profileId,
            layout: 'fit',
            deferHeight: true,
            height: '70%',
            
            items: [new Wtf.Panel({
                id: 'Npane_' + profileId,
                deferHeight: true,
                border: true,
                width: '100%',
                tbar: [{
                    text: WtfGlobal.getLocaleText('pm.contacts.viewprofile'),
                    handler: function(){
                        mainPanel.loadTab("../../user.html", "   " + profileId, obj.getUI().getTextEl().innerHTML, 'navareadashboard', Wtf.etype.user, true);
                    }
                }, '-'],
                
                html: '<div id="readArea_' + profileId + '" class=readAreaClass ></div>'
            })]
        }, {
            region: 'south',
            height: 100,
            layout: 'fit',
            id: 'southRegion' + profileId,
            items: [hEdit = new Wtf.newHTMLEditor({
                id: 'writeArea_' + profileId,
                enableLists: false,
                enableSourceEdit: false,
                enableAlignments: false,
                hideLabel: true,
                deferHeight: true
            })]
        }],
        buttons: [{
            text: WtfGlobal.getLocaleText('lang.Send.text'),
            handler: function(){
                var wA = document.getElementById("writeArea_" + profileId);
                var str = wA.value;
                if (str != "") {
                    insertmsg(str, profileId, 1);
                    Wtf.getCmp(wA.id).setValue(null);
                    
                    var msgstr = encodeURIComponent(str);
                    Wtf.Ajax.requestEx({
                        url: Wtf.req.prt + "messenger/msg.jsp",
                        params: {
                            s: portalMessenger_usr,
                            r: profileId,
                            chat: msgstr,
                            rstatus: obj.getUI().getTextEl().getAttribute("ustat")
                        }},
                        this);
                }
            }
        }]
    }).on({
        //'hide':closescw,
        //'close': myclosefunction,
        'hide': minchatwin,
        'minimize': minchatwin,
        scope: this
    });
    var mywin = Wtf.getCmp("chatWin" + profileId);
    mywin.show();
}

function myclosefunction(obj){
    document.getElementById(iddd).setAttribute("status", "f");
    obj.remove();
}

/*****************************************************************
 function to replace the smiley definition by smiley image
 defined smileys are-
 :) , :P ,:D ,:X,;) , :(
 ******************************************************************/
function smiley(tdiv, emoticon){
    tdiv.innerHTML = tdiv.innerHTML.replace(emoticon, '<img src=../../images/smiley' + (smileyStore.indexOf(emoticon) +1) + '.gif style=display:inline;vertical-align:text-top;></img>');
}


/********************this is nop******/
function thisfunct(){

}

function displayMsg(){
    var i;
    // gcount=gcount+1;
    Wtf.Ajax.request({
        method: 'GET',
        url: Wtf.req.prt + "messenger/getmymessage.jsp",
        params: ({
            login: portalMessenger_usr,
            cts: portalMessenger_maxts
        }),
        scope: this,
        success: function(result, req){
            if (result.responseText.match("zero") == null) {
                var mymsg = eval(('(' + result.responseText + ')'));
                var l = mymsg.message.length;
                portalMessenger_maxts = mymsg.messagetimestamp[l - 1];
                for (i = 0; i < l; i++) {
                    winchecker(mymsg.sendid[i], mymsg.message[i]);
                }
            }
        },
        failure: function(result, req){
        
        }
        });
    portalMessenger_pollTimer = setTimeout("displayMsg()", 2000);
}

function winchecker(wcidd, msg){
    alert();
    handleClick(wcidd);
    insertmsg(msg, wcidd, 2);
}

/******************************************************************************
 function to insert messages in the read area used with two diff flags
 1 for default user
 2 for the friend
 *******************************************************************************/
function insertmsg(msg, imiddd, mflag){

    var name;
    var rA = document.getElementById("readArea_" + imiddd);
    var imtdiv = document.createElement('div');
    var str = msg;
    var arr = [];
    //Wtf.getCmp('contactsview').getNodeById('kcont_'+imiddd).getUI().getTextEl().innerHTML;
    
    if (mflag == 1) {
        name = portalMessenger_defusr;
    }
    else 
        if (mflag == 2) {
            name = Wtf.getCmp('contactsview').getNodeById('kcont_' + imiddd).getUI().getTextEl().innerHTML;
        }
    
    arr = str.match(/(:\))|(:X)|(:\()|(:P)|(:D)|(;\))/g);
    if (arr == null) {
        imtdiv.innerHTML = '<p><strong>' + name + ':</strong> ' + str;
    }
    else {
        var i;
        var smileyStr;
        imtdiv.innerHTML = '<p><strong>' + name + ':</strong> ' + str;
        for (i = 0; i < arr.length; i++) {
            smiley(imtdiv, arr[i]);
        }
    }
    rA.innerHTML += imtdiv.innerHTML + '<br/>';
    
}

//********************function to change the pointer style on hover*************************
//someTimer();
/*  Messenger: End  */

/*  MsgHandler: Start   */
function openprofile(theId, from, imgsrc){
    mainPanel.loadTab("../../user.html", "   " + theId, from, "navareadashboard", Wtf.etype.user, true);
}

//*********Check Smileys******************
function parseSmiley(str){
    str = unescape(str);
    var tdiv = document.createElement('div');
    var arr = [];
    arr = str.match(/(:\(\()|(:\)\))|(:\))|(:x)|(:\()|(:P)|(:D)|(;\))|(;;\))|(&gt;:D&lt;)|(:-\/)|(:&gt;&gt;)|(:-\*)|(=\(\()|(:-O)|(X\()|(:&gt;)|(B-\))|(:-S)|(#:-S)|(&gt;:\))|(:\|)|(\/:\))|(=\)\))|(O:-\))|(:-B)|(=;)|(:-c)/g);
    
    if (arr == null) {
        tdiv.innerHTML = str;
    }
    else {
        var i;
        var smileyStr;
        tdiv.innerHTML = str;
        for (i = 0; i < arr.length; i++) {
            smiley(tdiv, arr[i]);
        }
    }
    return tdiv.innerHTML;
}
/*  MsgHandler: End */
