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
function uploadButttonClick(doccmp){
    var grid1 = doccmp.grid1;
    var groupid = doccmp.groupid;
    var pcid = doccmp.pcid;
    var userid = doccmp.userid;
    var ChkValue;
    var flagforgrid = 0;
    var filename;
    var flagforconfirm = 0;
    var ChkOwner;
    fs = new Wtf.FormPanel({
        id: 'uploadfrm',
        frame: true,
        labelWidth: 55,
        width: 340,
        method: 'POST',
        fileUpload: true,
        waitMsgTarget: true,
        url: 'fileAddAction.jsp?fileadd=false',
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
                fieldLabel: WtfGlobal.getLocaleText('lang.Type.text'),
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
    
    win = new Wtf.Window({
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
            waitMsg: WtfGlobal.getLocaleText('pm.uploading.text')+'...',
            failure: function(result, req){
                msgBoxShow(265, 1);
//                Wtf.MessageBox.show({
//                    title: WtfGlobal.getLocaleText('lang.error.text'),
//                    msg: 'A problem occurred while uploading',
//                    buttons: Wtf.MessageBox.OK,
//                    animEl: 'upfilewin',
//                    icon: Wtf.MessageBox.ERROR
//                });
                close();
                
            },
            success: function(frm, action){
                close();
                if (action.response.responseText != "") {
                    var uploadstr = eval('(' + action.response.responseText + ')');
                    if (uploadstr.msg != null && uploadstr.msg != "1") {
                        msgBoxShow([WtfGlobal.getLocaleText('pm.msg.ERROR'), uploadstr.msg], 1);
//                        Wtf.Msg.show({
//                            title: WtfGlobal.getLocaleText('lang.error.text'),
//                            msg: uploadstr.msg,
//                            buttons: Wtf.Msg.OK,
//                            animEl: 'elId',
//                            icon: Wtf.MessageBox.ERROR
//                        });
                    }else{
                        if(uploadstr.action=="add"){
                            var recarr = doccmp.reader.readRecords(eval('('+uploadstr.data+')'));
                            if(doccmp.grid1.getStore().find('Id',recarr.records[0].data["Id"])==-1){
                                doccmp.grid1.getStore().add(recarr.records);
                                doccmp.grid1.getView().refresh();
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
                }
                else 
                    frm.reset();
                if (flagforconfirm == 1) {
                    close1();
                }
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
                id: WtfGlobal.getLocaleText('lang.OK.text'),
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
        } else if (rs == 3)
            result1(val, userdocid, 0);
        else if (rs == 4)
            UploadFile();
    }
    
    function FileUpload(){
        Wtf.getCmp('upbtn').disable();
        if (document.getElementById('filepath').value <= 0) {
            MshBoxShow(206, 0);
//            Wtf.MessageBox.show({
//                title: 'File Path...',
//                msg: 'Please Enter File path',
//                buttons: Wtf.MessageBox.OK,
//                animEl: 'upbtn',
//                icon: Wtf.MessageBox.INFO
//            });
             Wtf.getCmp('upbtn').enable();
        }
        else {
            var textArea = Wtf.get('filepath');
            Wtf.Ajax.requestEx({
                url: Wtf.req.doc + 'file-releated/filecontent/chkfile.jsp',
                params: {
                    docname: textArea.getValue(),
                    groupid: groupid,
                    pcid: pcid
                }
            }, this, ResultFun, function(){
                msgBoxShow([WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.245')], 1);
//                Wtf.MessageBox.show({
//                    title: 'Error...',
//                    msg: 'Error Occurred While Uploading File',
//                    buttons: Wtf.MessageBox.OK,
//                    animEl: 'upbtn',
//                    icon: Wtf.MessageBox.INFO
//                });
                close();
            });
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
}

function deleteButttonClick(groupid, docid, userid, count, mainTree, pcid,grid){

    Wtf.Ajax.request({
    url: "deleteAction.jsp",
    params: {
        DOCID: docid,
        GROUPID: groupid,
        COMMENT: "f",
        COUNT: count,
        PCID: pcid
    }, 
    scope: this,
    success: function(result, req){
        if (eval('(' + result.responseText + ')')["res"] == 1) {
            MshBoxShow([WtfGlobal.getLocaleText('pm.msg.ERROR'), "Error in deleting file", 1]);
//            Wtf.MessageBox.show({
//                title: WtfGlobal.getLocaleText('pm.commom.errorincon'),
//                msg: 'Error In Deleting File',
//                buttons: Wtf.MessageBox.OK,
//                animEl: 'upbtn',
//                icon: Wtf.MessageBox.INFO
//            });
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
        }
    }
    });
}

function statusButttonClick(grid1, MyDocId, per, userid,groupid,pcid){
    var form;
    var win;
    form = new Wtf.FormPanel({
        id: 'perfrm',
        frame: true,
        minWidth: 385,
        url: 'http://www.google.com',
        method: 'POST',
        onSubmit: MyokClicked,
        items: [{
            xtype: 'fieldset',
            title: WtfGlobal.getLocaleText('lang.status.text'),
            id: 'statusRadio',
            autoHeight: true,
            defaultType: 'radio',
            labelWidth: 0,
            layoutConfig: {
                labelSeparator: ''
            },
            items: [/*{
                id: 'pending',
                boxLabel: WtfGlobal.getLocaleText('pm.common.pending'),
                name: 'statusgrp'
            }, */{
                id: 'waiting',
                boxLabel: WtfGlobal.getLocaleText('pm.member.request.approval'),
                name: 'statusgrp'
            }, {
                id: 'completed',
                boxLabel: WtfGlobal.getLocaleText('lang.completed.text'),
                name: 'statusgrp'
                // checked: true
            }, {
                id: 'draft',
                boxLabel: WtfGlobal.getLocaleText('pm.personalmessages.draft'),
                name: 'statusgrp'
            }, {
                id: 'Nonestatus',
                boxLabel: WtfGlobal.getLocaleText('lang.none.text'),
                name: 'statusgrp'
            }]
        }/*, new Wtf.Button({
            text: WtfGlobal.getLocaleText('lang.cancel.text'),
            id: 'doccancel',
            handler: MycancelClicked,
            minWidth: 60,
            cls: 'docbutton'
        }), new Wtf.Button({
            text: WtfGlobal.getLocaleText('lang.OK.text'),
            type: 'submit',
            id: 'docok',
            handler: MyokClicked,
            minWidth: 60,
            cls: 'docbutton'
        })*/]
    });
    
    var divstatus = document.createElement('div');
    divstatus.id = 'formst';
    statusDefault(per);
    win = new Wtf.Window({
        id: 'statusupwin',
        title: WtfGlobal.getLocaleText('pm.project.document.filestatus'),
        closable: true,
        modal: true,
        width: 400,
        autoHeight: true,
        plain: true,
        iconCls: 'iconwin',
        resizable: false,
        layout: 'fit',
        contentEl: divstatus,
        buttons: [{
            text: WtfGlobal.getLocaleText('lang.cancel.text'),
            id: 'doccancel',
            handler: MycancelClicked,
            minWidth: 60,
            cls: 'docbutton'
        },{
            text: WtfGlobal.getLocaleText('lang.OK.text'),
            type: 'submit',
            id: 'docok',
            handler: MyokClicked,
            minWidth: 60,
            cls: 'docbutton'
        }]
    }).show();
    
    form.render('formst');
    
    function MyokClicked(){
        //alert(MyDocId);
        var string = '';
      /*  if (Wtf.getCmp('pending').getValue()) {
            string = WtfGlobal.getLocaleText('pm.common.pending');
        }
        else */
            if (Wtf.getCmp('waiting').getValue()) {
                string = "Waiting";
            }
            else 
                if (Wtf.getCmp('completed').getValue()) {
                    string = WtfGlobal.getLocaleText('lang.completed.text');
                }
                else 
                    if (Wtf.getCmp('draft').getValue()) {
                        string = WtfGlobal.getLocaleText('pm.personalmessages.draft');
                    }
                    else {
                        string = WtfGlobal.getLocaleText('lang.none.text');
                    }
        
        var xmlHttp, i = 0;
        var response;
        Wtf.Ajax.requestEx({
            url: Wtf.req.doc + "perm/setStatus.jsp",
            params: ({
                docid: MyDocId,
                status: string,
                groupid:groupid,
                pcid:pcid
                //  url: url
            })
        }, this, function(result, req){
            if (eval('(' + result + ')')["res"] == 0) {
                var rec = grid1.store.find("Id", MyDocId, 0, false, true);
                grid1.store.getAt(rec).set('Status', string);
                grid1.getView().refresh();
                Wtf.getCmp('statusupwin').close();
            }
            else {
                msgBoxShow(232, 1);
//                Wtf.MessageBox.show({
//                    title: WtfGlobal.getLocaleText('pm.commom.errorincon'),
//                    msg: 'Please Change Status Again',
//                    buttons: Wtf.MessageBox.OK,
//                    animEl: 'upbtn',
//                    icon: Wtf.MessageBox.INFO
//                });
            }
        });
        
    }
    
    function MycancelClicked(){
        Wtf.getCmp('statusupwin').close();
    }
    
    function statusDefault(per){
       /* if (per == "Pending") {
            Wtf.getCmp('pending').checked = true;
        }
        else */
            if (per == "Waiting") {
                Wtf.getCmp('waiting').checked = true;
            }
            else 
                if (per == WtfGlobal.getLocaleText('lang.completed.text')) {
                    Wtf.getCmp('completed').checked = true;
                }
                else 
                    if (per == WtfGlobal.getLocaleText('pm.personalmessages.draft')) {
                        Wtf.getCmp('draft').checked = true;
                    }
                    else {
                        Wtf.getCmp('Nonestatus').checked = true;
                    }
    }
}
