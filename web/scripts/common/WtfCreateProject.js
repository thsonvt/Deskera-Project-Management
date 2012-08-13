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

Wtf.createProject = function(config){
    Wtf.apply(this, config);
    this.ccCount = getCustomColumnCount("Project");
    var ccheigth = this.ccCount==0?0:(40+(this.ccCount<4?this.ccCount*40:160));
    this.text = WtfGlobal.getLocaleText('lang.create.text');
    if(this.mode == 1) {
        this.text = WtfGlobal.getLocaleText('lang.edit.text');
    }
    var btnArr = [];
    this.OKBtn = new Wtf.Button({
        text : this.text,
        scope : this,
        id: 'createProjtBtn',
        cls: 'adminButton',
        handler: this.OKHandler
    });
    btnArr.push(this.OKBtn);
    this.CreateOpenBtn = new Wtf.Button({
        text: WtfGlobal.getLocaleText('pm.project.creatopen'),
        scope: this,
        id: 'createOpenProjBtn',
        cls: 'adminButton',
        hidden: (this.mode) ? true : false,
        handler: this.OKHandler
    });
    btnArr.push(this.CreateOpenBtn);
    this.cancelBtn = new Wtf.Button({
        text :WtfGlobal.getLocaleText('lang.cancel.text'),
        cls: 'adminButton',
        id: "cancelProjBtn",
        scope : this
    });
    btnArr.push(this.cancelBtn);
    Wtf.createProject.superclass.constructor.call(this, {
        title : (this.mode==1)?WtfGlobal.getLocaleText('pm.admin.project.editproject'):WtfGlobal.getLocaleText('pm.dashboard.widget.quicklinks.createnewproject'),
        closable : true,
        modal : true,
        iconCls : 'iconwin',
        width : 500,
        height: (this.mode==1?530:350+ccheigth),        //autoHeight:true,
        buttonAlign : 'right',
        buttons :btnArr,
        layout : 'border',
        id: 'createProjectWin',
        autoDestroy: true
    });
}

Wtf.extend(Wtf.createProject, Wtf.Window,{

    initComponent: function(){
        Wtf.createProject.superclass.initComponent.call(this);
        var customcolumn = [];
        customcolumn = getFormsCCFields("Project",this.mode,this.data);
        this.Form = new Wtf.form.FormPanel({
            url: '../../admin.jsp?action=1&mode=2&emode='+this.mode,
            autoScroll:true,
            waitMsgTarget: true,
            method : 'POST',
            fileUpload: true,
            border : false,
            labelWidth: 125,
            bodyStyle : 'margin-top:10px;padding-left:20px;padding-bottom:10px;font-size:10px;',
            defaults: {
                width: 230
            },
            defaultType: 'textfield',
            items: [{
                xtype:'fieldset',
                title:WtfGlobal.getLocaleText('pm.createeditproject.title1'),
                autoHeight:true,
                width:425,
                defaults: {
                    width: 230
                },
                defaultType: 'textfield',
                items:[
                {
                    fieldLabel: WtfGlobal.getLocaleText('pm.createeditproject.projectname')+'*',
                    allowBlank:false,
                    name : 'projectname',
                    scope: this,
                    id : 'projectname',
                    value : this.data.projectname
                },{
                    fieldLabel: WtfGlobal.getLocaleText('pm.createeditproject.projectimage'),
                    height: 24,
                    inputType : 'file',
                    validator: WtfGlobal.validateImageFile,
                    invalidText: WtfGlobal.getLocaleText("pm.common.validateimage.invalidtext"),
                    name : 'image'
                },{
                    xtype : 'textarea',
                    fieldLabel: WtfGlobal.getLocaleText('pm.createeditproject.projectdescription'),
                    height : 80,
                    name : 'aboutproject',
                    maxLength : 512,
                    id : 'aboutproject',
                    value : this.data.description
                },{
                    xtype : 'hidden',
                    name: 'projectid',
                    value: this.data.projectid
                },{
                    xtype : 'hidden',
                    name: 'companyid',
                    value: companyid
                }]
            },{
                xtype:'fieldset',
                title:WtfGlobal.getLocaleText('pm.admin.customcolumn.information'),
                checkboxToggle:false,
                defaults:{
                    width:230
                },
                hidden:Wtf.cusColumnDS.getCount()>0?false:true,
                autoHeight:true,
                width:425,
                items:customcolumn         //items array for custom column
            },{
                xtype:'fieldset',
                title: WtfGlobal.getLocaleText('pm.createeditproject.title3'),
                autoHeight:true,
                width:425,
                toggle:true,
                labelWidth:195,
                hidden:this.mode==1?false:true,
                items :[
                {
                    border: false,
                    layout: 'fit',
                    autoHeight: true,
                    html:WtfGlobal.getLocaleText('pm.createeditproject.healthdetail.text'),
                    bodyStyle:'padding-bottom:10px;color:#15428B;font-size:11px'
                },
                /*this.baseCombo = new Wtf.form.ComboBox({
                        fieldLabel:WtfGlobal.getLocaleText('lang.base.text'),
                        emptyText: WtfGlobal.getLocaleText('pm.project.health.selectbase'),
                        store:new Wtf.data.SimpleStore({
                          fields:['id','base'],
                          data:[['1','TASK'],['2','DAYS']]
                        }),
                        displayField:'base',
                        valueField:'base',
                        id:'base',
                        typeAhead: false,
                        mode: 'local',
                        editable: false,
                        triggerAction: "all"

                     }),*/
                {
                    layout:'column',
                    border:false,
                    items:[
                    {
                        columnWidth:.9,
                        layout:'form',
                        border:false,
                        items:[{
                            xtype:'numberfield',
                            fieldLabel: WtfGlobal.getLocaleText('pm.createeditproject.ontime'),
                            name: 'ontime',
                            allowBlank:this.mode==1?false:true,
                            emptyText:WtfGlobal.getLocaleText('pm.common.lessthanpercent'),
                            value : this.data.ontime
                        }]
                    },{
                        border:false,
                        html:"<img  src='../../images/help.png' wtf:qtip='"+WtfGlobal.getLocaleText('pm.createeditproject.healthdetail.qtip')+"'/>"
                    }]
                },
                {
                    layout:'column',
                    border:false,
                    items:[
                    {
                        columnWidth:.9,
                        layout:'form',
                        border:false,
                        items:[{
                            xtype:'numberfield',
                            fieldLabel: WtfGlobal.getLocaleText('pm.projhealth.sl_gb'),
                            name: 'slightly',
                            id: 'slightly',
                            allowBlank:this.mode==1?false:true,
                            emptyText:WtfGlobal.getLocaleText('pm.common.lessthanpercent'),
                            value : this.data.slightly
                        }]
                    },{
                        border:false,
                        html:"<img src='../../images/help.png' style='cursor:pointer' wtf:qtip='"+WtfGlobal.getLocaleText('pm.createeditproject.healthdetail.qtip')+"'/>"
                    }]
                }/*,{
                        xtype:'numberfield',
                        fieldLabel: WtfGlobal.getLocaleText('pm.project.health.gb'),
                        name: 'gravely',
                        id:'gravely',
                        readOnly:true,
                        emptyText:WtfGlobal.getLocaleText('lang.greaterthaneq.perc.text'),
                        hidden:true,
                        value :this.data.gravely
                        
                   }*/
                ]
            }]
        });

        Wtf.getCmp("projectname").on("change", function(){
            Wtf.getCmp("projectname").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("projectname").getValue()));
        }, this);
        Wtf.getCmp("aboutproject").on("change", function(){
            Wtf.getCmp("aboutproject").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("aboutproject").getValue()));
        }, this);
        // Wtf.getCmp('base').setValue(this.data.base);
        //        Wtf.getCmp('slightly').on('blur', function(cmp){
        //            Wtf.getCmp('gravely').setValue(cmp.getValue())
        //        });
        var topHTML = getTopHtml(this.text + " "+WtfGlobal.getLocaleText('pm.project.text'), this.text +  + " "+WtfGlobal.getLocaleText('pm.project.text')+"<br><span style=\"float:right; \">"+WtfGlobal.getLocaleText('pm.common.requiredfields')+"</span>", "../../images/createprojectplan40_52.gif");
        this.add({
            region : 'north',
            height : 75,
            border : false,
            bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
            html: topHTML

        },{
            region : 'center',
            border : false,
            bodyStyle : 'background:#f1f1f1;font-size:10px;',
            layout : 'fit',
            items :this.Form
        });
        this.doLayout();
    },

    OKHandler: function(btn){
        var df = this.Form.findByType('datefield');
        for(var i=0; i<df.length; i++){
            df[i].format='Y-m-d';
            df[i].setValue(Date.parseDate(df[i].getRawValue(),WtfGlobal.getOnlyDateFormat()));
        }
        if(this.Form.form.isValid()){
            btn.disable();
            this.cancelBtn.disable();
            this.Form.form.submit({
                waitMsg: WtfGlobal.getLocaleText("pm.loading.text") + '...',
                scope : this,
                useraction: this.mode,
                failure: function(frm, action){
                    this.failureHandler(btn, frm, action);
                },
                success: function(frm, action){
                    this.successHandler(btn, frm, action);
                }
            });
        }else{
            for( i=0; i<df.length; i++){
            df[i].format=WtfGlobal.getOnlyDateFormat();
            df[i].setValue(Date.parseDate(df[i].getRawValue(),'Y-m-d'));
        }
        }
    }
});
