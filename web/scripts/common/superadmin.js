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
Wtf.SuperAdmin = function(config){
    Wtf.SuperAdmin.superclass.constructor.call(this,config);
};
Wtf.extend(Wtf.SuperAdmin,Wtf.Panel,{
    createCompanyWindow:null,
    emode : 0,
    bttnText : WtfGlobal.getLocaleText('lang.create.text'),
    divText : "Create a new",
    headertext : WtfGlobal.getLocaleText('pm.company.create.text'),
    onRender : function(config){
        Wtf.SuperAdmin.superclass.onRender.call(this,config);
        
        this.dataReader = new Wtf.data.JsonReader({
            totalProperty: 'count',
            root: "data",
            fields: [
                    {name: 'image'},
                    {name: 'companyid'},
                    {name: 'companyname'},
                    {name: 'members'},
                    {name: 'createdon'},
                    {name: 'website'},
                    {name: 'activated'},
                    {name: 'aboutcompany'},
                    {name: 'address'},
                    {name: 'city'},
                    {name: 'state'},
                    {name: 'country'},
                    {name: 'phone'},
                    {name: 'fax'},
                    {name: 'zip'},
                    {name: 'timezone'}
                ]
        });
        
        this.companyds = new Wtf.data.Store({
            id : "datastore"+this.id,
            url: '../../sa.jsp',
            root: 'data',
            reader: this.dataReader,
            method : 'POST'
        });

        this.selectionModel = new Wtf.grid.CheckboxSelectionModel();

        this.cm= new Wtf.grid.ColumnModel([this.selectionModel,{
            dataIndex: 'image',
            width : 30,
            renderer : function(value){
               return String.format("<img src={0} height='18' width='18' />",value);
            }
        },{
           header: WtfGlobal.getLocaleText('pm.company.text'),
           dataIndex: 'companyname',
           autoWidth : true,
           sortable: true,
           groupable: true
        },{
           header :WtfGlobal.getLocaleText('pm.common.createdon'),
           dataIndex: 'createdon',
           autoSize : true,
           sortable: true,
           groupable: true
        },{
           header :WtfGlobal.getLocaleText('lang.website.text'),
           dataIndex: 'website',
           autoSize : true,
           sortable: true,
           groupable: true 
        },{
           header :WtfGlobal.getLocaleText('pm.admin.project.members'),
           dataIndex: 'members',
	   autoSize : true,
           sortable: true,
           groupable: true
        },{
           header :WtfGlobal.getLocaleText('lang.actevated.text'),
           dataIndex: 'activated',
           autoSize : true,
           sortable: true,
           groupable: true
        }]);

       this.companygrid = new Wtf.grid.GridPanel({
            id:'companygrid'+this.id,
            store:  this.companyds,
            cm: this.cm,                           
            sm : this.selectionModel,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true
            }
       });

       this.companygrid.on("rowdblclick",this.GridRowdbClicked,this);
 
       this.companyds.load({
            params: {
                start:0,
                limit:20,
                mode: 0
            }
       });
        
       this.companygrid.getSelectionModel().addListener('selectionchange', this.rowSelectionHandler, this);
       this.companygridPanel  = new Wtf.common.KWLListPanel({
            id : "companygridpanel"+this.id,
            title:WtfGlobal.getLocaleText('pm.companies.text'),
            autoLoad : false,
            paging : false,
            layout : 'fit',
            items:[this.companygrid]
        });

        this.innerpanel = new Wtf.Panel({
            id : 'companyinnerpanel'+this.id,
            layout : 'fit',
            cls : 'backcolor',
            border : false,
            tbar:[WtfGlobal.getLocaleText('pm.common.quicksearch')+': ',
                    this.quickSearchTF = new Wtf.KWLQuickSearch({
                        id : 'company'+this.id,
                        width: 200,
                        field : "Company",
                        emptyText: WtfGlobal.getLocaleText('pm.search.company.text')
                })],
            bbar:new Wtf.PagingToolbar({
                pageSize: 20,
                id : "paggintoolbarcompany"+this.id,
                store: this.companyds,
                plugins : new Wtf.common.pPageSize({id : "pPageSize_"+this.id}),
                items :['-','->',{
                    text : WtfGlobal.getLocaleText('pm.company.create.text'),
                    id : "createcompany"+this.id,
                    allowDomMove:false,
                    scope : this,
                    tooltip:'Create a new company',
                    handler : this.createClick,
                    iconCls : 'add'
                },'-',{
                    text : WtfGlobal.getLocaleText('pm.comapny.edit'),
                    id : "editcompany"+this.id,
                    allowDomMove:false,
                    scope : this,
                    disabled : true,
                    tooltip:'Edit Company information',
                    handler : this.editClick,
                    iconCls : 'add'
               },'-',{
                    text : WtfGlobal.getLocaleText('pm.company.delete.text'),
                    id : "deletecompany"+this.id,
                    allowDomMove:false,
                    scope : this,
                    disabled: true,
                    tooltip:'Delete Comapany',
                    handler : this.delClick,
                    iconCls : 'add'
               }]
          }),
          items:[this.companygridPanel]
      });
      this.add(this.innerpanel);
      this.companyds.on("load",function(){
        this.quickSearchTF.StorageChanged(this.companyds);
        this.quickSearchTF.on('SearchComplete', this.QuickSearchComplete, this);
      },this);
      
    },
    
    rowSelectionHandler: function(sm) {
        if(sm.getSelections().length>1){
                Wtf.getCmp("deletecompany"+this.id).setDisabled(false);
                Wtf.getCmp("editcompany"+this.id).setDisabled(true);
                Wtf.getCmp("createcompany"+this.id).setDisabled(false);
            }
            else if(sm.getSelections().length==1){
                Wtf.getCmp("deletecompany"+this.id).setDisabled(false);
                Wtf.getCmp("editcompany"+this.id).setDisabled(false);
                Wtf.getCmp("createcompany"+this.id).setDisabled(false);
            }
            else {
                Wtf.getCmp("deletecompany"+this.id).setDisabled(true);
                Wtf.getCmp("editcompany"+this.id).setDisabled(true);
                Wtf.getCmp("createcompany"+this.id).setDisabled(false);
            }
    },

    createClick: function() {
        this.CreateWindow({
            mode: 1
        });
    },

    editClick: function() {
        var buf = this.selectionModel.getSelected();
        this.CreateWindow({
            mode: 2,
            companyname: buf.data["companyname"],
            address: buf.data["address"],
            city: buf.data["city"],
            state: buf.data["state"],
            country: buf.data["country"],
            phone: buf.data["phone"],
            fax: buf.data["fax"],
            zip: buf.data["zip"],
            timezone: buf.data["timezone"],
            website: buf.data["website"],
            aboutcompany: buf.data["aboutcompany"],
            companyid: buf.data["companyid"]
        });
    },

    delClick: function() {
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.335'), function(btn){
            if (btn == "yes") {
                this.deleteCompany();
            }
        }, this);
    },

    featureClick: function() {
        
    },

    QuickSearchComplete: function(e){
        view = this.companygrid.getView();
        view.refresh();
    },

    GridRowdbClicked : function(Grid,rowIndex,e){
        var p = Wtf.getCmp(this.companyds.getAt(rowIndex).get("companyid")+'adminpanel');
        if(!p){
            mainPanel.setActiveTab(mainPanel.add(new Wtf.common.MainAdmin({
                title: this.companyds.getAt(rowIndex).get("companyname"),
                id: this.companyds.getAt(rowIndex).get("companyid")+'adminpanel',
                companyid: this.companyds.getAt(rowIndex).get("companyid"),
                navarea: "navareadashboard",
                layout : "fit",
                border: false,
                closable: true,
                tabType: Wtf.etype.adminpanel,
                imgsource: this.companyds.getAt(rowIndex).get("image"),
                iconCls: getTabIconCls(Wtf.etype.adminpanel)
            })));
            mainPanel.doLayout();
        }
        else{
            mainPanel.setActiveTab(p);
        }
    },
    
    deleteCompany: function(){
       var selected = this.selectionModel.getSelections();
       var selIds = "";
       for (var i = 0; i < selected.length; i++) {
            selIds += "'" + selected[i].data['companyid'] + "',";
       }
       selIds = selIds.substring(0,selIds.length - 1);
       Wtf.Ajax.request({
            method: 'POST',
            url: "../../sa.jsp?mode=3",
            params: ({
                   companyid: selIds
            }),
            scope: this,
            success: function(result, req){
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText('lang.success.text'),
                    msg: 'Company(s) Deleted Successfully',
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.INFO
                });
            },
            failure: function(result, req){
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText('lang.error.text'),
                    msg: 'Operation Failed. Company(s) Not Deleted',
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.INFO
                });
            }
        });       
    },
    
    CreateWindow : function(params){

         if(params.mode == 1) {
            this.bttnText = WtfGlobal.getLocaleText('lang.create.text');
            this.divText = "Create a new";
            this.headertext = WtfGlobal.getLocaleText('pm.company.create.text');
        }
        else {
            this.bttnText = WtfGlobal.getLocaleText('lang.update.text');
            this.divText = "Edit an existing";
            this.headertext = WtfGlobal.getLocaleText('pm.comapny.edit');
        }
        this.createCompanyWindow = new Wtf.Window({
            title : WtfGlobal.getLocaleText('pm.company.text'),
            closable : true,
            modal : true,
            iconCls : 'iconwin',
            width : 450,
            height: 550,
            resizable :false,
            buttonAlign : 'right',
            buttons :[{
                text : this.bttnText,
                scope : this,
                handler:function(){
                    this.createCompanyForm.form.submit({
                        waitMsg: 'Loading...',
                        scope : this,
                        useraction: params.mode,
                        failure: function(action){
                            var text = "Created";
                            if(action.options.useraction == 1){
                                text = "Edited";
                            }
                             Wtf.MessageBox.show({
                                title: WtfGlobal.getLocaleText('pm.common.failure'),
                                msg: 'Operation Failed. Company Not ' + text,
                                buttons: Wtf.MessageBox.OK,
                                icon: Wtf.MessageBox.INFO
                            });
                            this.createCompanyWindow.close();
                        },
                        success: function(frm, action){
                            var text = "Created";
                            if(action.options.useraction == 1){
                                text = "Edited";
                            }
                             Wtf.MessageBox.show({
                                title: WtfGlobal.getLocaleText('lang.success.text'),
                                msg: 'Company ' + text + ' Successfully',
                                buttons: Wtf.MessageBox.OK,
                                icon: Wtf.MessageBox.INFO
                            });
                            this.createCompanyWindow.close();
                        }
                    }); 
                }
            },{
                text : WtfGlobal.getLocaleText('lang.cancel.text'),
                scope : this,
                handler : function(){
                    this.createCompanyWindow.close();
                }
            }],
            layout : 'border',
            items :[{
                region : 'north',
                height : 75,
                border : false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                html : "<div style = 'width:100%;height:100%;position:relative;float:left;'>"
                         +"<div style='float:left;height:100%;width:auto;position:relative;'>"
                            +"<img src = '../../images/createcompany.png' style = 'width:40px;height:52px;margin:5px 5px 5px 5px;'></img>"
                         +"</div>"
                         +"<div style='float:left;height:100%;width:80%;position:relative;'>"
                             +"<div style='font-size:12px;font-style:bold;float:left;margin:20px 0px 0px 10px;width:100%;position:relative;'><strong>"+this.headertext+"</strong></div>"
                             +"<div style='font-size:10px;float:left;margin:15px 0px 0px 10px;width:100%;position:relative;'>"+this.divText+" Company Domain</div>"
                         +"</div>"
                        +"</div>"
            },{
                region : 'center',
                border : false,
                id : 'companywincenter',
                bodyStyle : 'background:#f1f1f1;font-size:10px;',
                layout : 'fit',
                items :[this.createCompanyForm = new Wtf.form.FormPanel({
                    url: '../../sa.jsp?mode='+params.mode,
                    waitMsgTarget: true,
                    fileUpload: true,
                    method : 'POST',
                    border : false,
                    labelWidth: 120,
                    bodyStyle : 'margin-top:20px;margin-left:35px;font-size:10px;',
                    defaults: {width: 240},
                    defaultType: 'textfield',
                    items: [{
                        fieldLabel: WtfGlobal.getLocaleText('pm.admin.company.name'),
                        name:'companyname',
                        value : params.companyname
                    },{
                        fieldLabel: WtfGlobal.getLocaleText('lang.address.text'),
                        name : 'address',
                        value : params.address
                    },{
                        fieldLabel: WtfGlobal.getLocaleText('lang.city.text'),
                        name : 'city',
                        value : params.city
                    },{
                        fieldLabel: WtfGlobal.getLocaleText('lang.state.text'),
                        name : 'state',
                        value : params.state
                    },{
                        fieldLabel: WtfGlobal.getLocaleText('lang.country.text'),
                        name : 'country',
                        value : params.country
                    },{
                        fieldLabel: WtfGlobal.getLocaleText('pm.common.phone'),
                        name : 'phone',
                        value : params.phone
                    },{
                        fieldLabel: WtfGlobal.getLocaleText('lang.fax.text'),
                        name : 'fax',
                        value : params.fax
                    },{
                        fieldLabel: WtfGlobal.getLocaleText('pm.common.postalzip'),
                        name : 'zip',
                        value : params.zip
                    },{
                        fieldLabel: WtfGlobal.getLocaleText('lang.timezone.tex'),
                        name : 'timezone',
                        value : params.timezone
                    },{
                        fieldLabel: WtfGlobal.getLocaleText('lang.emailid.text'),
                        name : 'website',
                        value : params.website
                    },new Wtf.form.TextArea({
                        fieldLabel: WtfGlobal.getLocaleText('lang.comapany.description.text'),
                        height : 60,
                        name : 'aboutcompany',
                        value : params.aboutcompany
                    }),{
                        fieldLabel: WtfGlobal.getLocaleText('pm.admin.company.pic'),
                        name : 'image',
                        inputType : 'file',
                        height: 24
                    },new Wtf.form.Hidden({
                        name: "companyid",
                        value: params.companyid
                    })]
                })]
            }]
        });
        this.createCompanyWindow.show();
    }
});
