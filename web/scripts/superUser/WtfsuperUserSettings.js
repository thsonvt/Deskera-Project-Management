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
function superuser(mode, cid){
    switch(mode){
        case "status":
            mainPanel.loadTab("../../companystats.jsp", "CompanyStatistics", "List of companies", "navareadashboard");
            break;
        case "subdomain":
            loadSubdomainList("offSubdomainList", "List of subdomain");
            break;
        case "subdetails":
            loadSubscriptionDetails("subscriptionDetails", "Subscription details");
            break;
        case "managesub":
            loadManageSubscription("manageSubscriptionPanel", "Manage subscription", cid);
            break;
        case "managepay":
            openSubCsvWindow();
            break;
    }
}

function openSubCsvWindow(){
    Wtf.SubCsvWindow = function(){
         config = {
            title: WtfGlobal.getLocaleText('pm.common.uploadpaycsv'),
            closable: true,
            modal: true,
            iconCls: 'iconwin',
            width: 460,
            height: 300,
            resizable: false,
            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body,
            buttons: [{
                text: WtfGlobal.getLocaleText('pm.common.upload'),
                scope: this,
                handler: function(){
                    if(!(this.center.form.isValid()))
                        return;
                    this.center.form.submit({
                        waitMsg: WtfGlobal.getLocaleText("pm.loading.text")+'...',
                        scope: this,
                        failure: function(frm, action){
                            this.close();
                        },
                        success: function(frm, action){
                            var res = eval('('+action.response.responseText+')')
                            var resText = res.msg;
                            if(res.success) {
                                msgBoxShow([0,resText], 0);
                            }
                            this.close();
                        }
                    });
                }
            }, {
                text: WtfGlobal.getLocaleText('lang.cancel.text'),
                scope: this,
                handler: function(){
                    this.close();
                }
            }]
        }
        Wtf.SubCsvWindow.superclass.constructor.call(this, config);
    }

    Wtf.extend(Wtf.SubCsvWindow, Wtf.Window, {
        initComponent: function(config){

            Wtf.SubCsvWindow.superclass.initComponent.call(this, config);
            this.createForm();

        },
        createForm: function(params){
            this.attachheight = 75;
            this.count = 1;
            this.attachPanel = {
                xtype : 'panel',
                border: false,
                html:"<a id = 'attachmentlink"+this.id+"' class='subattachremove' href=\"#\" onclick=\"Attachfile(\'"+this.id+"\')\">Upload another file</a>"
            };
            this.center = new Wtf.FormPanel({
                frame: false,
                layout:'fit',
                bodyStyle : 'margin-top:20px;margin-left:15px;font-size:12px;background:#f1f1f1;',
                url: 'jspfiles/authorizeddotnet.jsp?action=4',
                border: false,
                labelWidth:0,
                fileUpload: true,
                layout: 'form',
                items: [ new Wtf.form.TextField({
                        fieldLabel: '',
                        labelSeparator:'',
                        name: 'attach0',
                        inputType: 'file'
                    }), this.attachPanel
                ]
            });
        },
        removeFile:function(){
            this.attachheight -=25;
            this.count--;
            this.doLayout();
        },
        Attachfile:function(){
            var textfield = new Wtf.form.TextField({
                    fieldLabel: '',
                    labelSeparator:'',
                    name: 'attach'+(this.count),
                    inputType: 'file'
                });
                var pid = 'fileattach'+this.count+this.id;
                this.center.insert(this.count++,new Wtf.Panel({id : pid,cls:'subattachremove',border:false,html:'<a href=\"#\" class ="attachmentlink" style ="margin-left:5px" onclick=\"removefile(\''+pid+'\',\''+this.id+'\')\">Remove</a>',
                    items:textfield})
                );
                document.getElementById('attachmentlink'+this.id).innerHTML = "Upload another file";
            this.doLayout();
        },
        onRender: function(config){
            Wtf.SubCsvWindow.superclass.onRender.call(this, config);
            this.add({
                region: 'north',
                height: 75,
                border: false,
                bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                html: getTopHtml('Upload Paymnet Csv','Upload paymnet csv',"../../images/exportcsv40_52.gif")
            },{
                region: 'center',
                border: false,
                id: 'profilewin',
                bodyStyle: 'background:#f1f1f1;font-size:10px;',
                autoScroll:true,
                items: this.center
            });
        }
    });
    new Wtf.SubCsvWindow().show();
}

function loadSubscriptionDetails(id, title){
    var temp = Wtf.getCmp(id);
    if(temp === undefined){
        var compRec = Wtf.data.Record.create([{
            name: 'name'
        },{
            name: 'companyid'
        }]);
        var compStore = new Wtf.data.Store({
            url: "../../subscriptionStatus.jsp",
            baseParams: {
                mode: 1,
                loginid: loginid
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            }, compRec)
        });
        compStore.load();
        temp = new Wtf.Panel({
            id: id,
            title: title,
            layout: 'fit',
            autoScroll: true,
            tbar: [new Wtf.form.ComboBox({
                id: "companyListCombo",
                allowBlank: false,
                emptyText:'< Select company >',
                typeAhead: true,
                forceSelection: true,
                triggerAction: 'all',
                store:compStore,
                displayField: "name",
                valueField:'companyid',
                mode: 'local',
                height: 80,
                width: 200
            }), {
                text: WtfGlobal.getLocaleText('lang.submit.text'),
                handler: function(){
                    var cid = Wtf.getCmp("companyListCombo").getValue();
                    if(cid != null && cid != ""){
                        temp.load({
                            url: '../../subscriptionStatus.jsp',
                            params: {
                                mode: 2,
                                companyid: cid,
                                loginid: loginid
                            }
                        });
                    }
                }
            }],
            closable: true
        });
        mainPanel.add(temp);
    }
    mainPanel.setActiveTab(temp);
}

function loadSubdomainList(id, title){
    var temp = Wtf.getCmp(id);
    if(temp === undefined){
        temp = new Wtf.Panel({
            id: id,
            title: title,
            layout: 'fit',
            autoScroll: true,
            autoLoad: {
                url: '../../offdomain.jsp'
            },
            tbar: [new Wtf.form.TextField({
                id: 'subdomainField'
            }), {
                text: WtfGlobal.getLocaleText('lang.submit.text'),
                handler: function(){
                    var domain = Wtf.getCmp("subdomainField").getValue();
                    if(domain != null && domain != ""){
                        temp.load({
                            url: '../../offdomain.jsp',
                            params:{
                                subdomain: domain
                            }
                        });
                    }
                }
            }],
            closable: true
        });
        mainPanel.add(temp);
    }
    mainPanel.setActiveTab(temp);
}

function loadManageSubscription(id, title, cid){
    var temp = Wtf.getCmp(id);
    if(temp === undefined){
        var compRec = Wtf.data.Record.create([{
            name: 'name'
        },{
            name: 'companyid'
        }]);
        var compStore = new Wtf.data.Store({
            url: "../../subscriptionStatus.jsp",
            baseParams: {
                mode: 1,
                loginid: loginid
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            }, compRec)
        });
        compStore.on("load", function(obj){
            loadSubTab(cid, id);
        });
        compStore.load();
        temp = new Wtf.Panel({
            id: id,
            title: title,
            layout: 'fit',
            autoScroll: true,
            tbar: [new Wtf.form.ComboBox({
                id: "companyListComboBox",
                allowBlank: false,
                emptyText:'< Select company >',
                typeAhead: true,
                forceSelection: true,
                triggerAction: 'all',
                store:compStore,
                displayField: "name",
                valueField:'companyid',
                mode: 'local',
                height: 80,
                width: 200
            }), {
                text: WtfGlobal.getLocaleText('lang.submit.text'),
                handler: function(){
                    var cid = Wtf.getCmp("companyListComboBox").getValue();
                    if(cid != null && cid != ""){
                        temp.load({
                            url: '../../manageSubscription.jsp',
                            params: {
                                mode: 1,
                                companyid: cid,
                                loginid: loginid
                            }
                        });
                    }
                }
            }],
            closable: true
        });
        mainPanel.add(temp);
    }
    mainPanel.setActiveTab(temp);
//    Wtf.getCmp("companyListComboBox").store.load();
    if(cid !== undefined)
        loadSubTab(cid, id);
}
function loadSubTab(cid, panelid){
    var t = Wtf.getCmp("companyListComboBox");
    if(cid !== undefined && t !== undefined){
        t.setValue(cid);
        Wtf.getCmp(panelid).load({
            url: '../../manageSubscription.jsp',
            params: {
                mode: 1,
                companyid: cid,
                loginid: loginid
            }
        });
    }
}
function subscribemodeule(){
    var cid = Wtf.getCmp("companyListComboBox").getValue();
    if(cid != null && cid != ""){
        var tab = document.getElementById("companySubscriptionStatusTable");
        var ip = tab.getElementsByTagName("input");
        var pr =  {};
        pr["mode"] = 2;
        pr["companyid"] = cid;
        pr["loginid"] = loginid;
        for(var i = 0; i < ip.length; i++){
            pr[ip[i].name] = ip[i].checked;
        }
        Wtf.getCmp("manageSubscriptionPanel").load({
            url: '../../manageSubscription.jsp',
            params: pr
        });
    }
}
