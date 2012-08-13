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
Wtf.customReport=function(config){
    Wtf.apply(this,config);
    var defConf = {
        ctCls: 'reportfieldContainer',
        labelStyle: 'font-size:11px; text-align:right;'
    };
    this.attachheight=133;
    this.hfheight=150;
    this.subtitle = new Wtf.Panel({
        bodyStyle : 'margin-bottom:3px;padding-left:105px;',
        border:false,
        html:"<a id = 'subtitlelink"+this.id+"'class='attachmentlink' href=\"#\" onclick=\"Addsubtitle(\'"+this.id+"\')\">"+WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.addsub')+"</a>"
    });
    this.count=1;
    this.hfieldset=new Wtf.Panel({
        columnWidth: 0.59,
        border: false,
        height : this.attachheight,
        items:[{
            xtype:'fieldset',
            title: WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.headerfields'),
            cls: "customFieldSet",
            defaults : defConf,
            autoHeight : true,
            items:[
            {
                xtype : 'textfield',
                fieldLabel:WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.note'),
                labelSeparator:'',
                maxLength:40,
                validator:WtfGlobal.validateHTField,
                maxLengthText:WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.maxlengthtext'),
                emptyText:WtfGlobal.getLocaleText('pm.project.plan.note.insert')
            },{
                xtype : 'textfield',
                fieldLabel:WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.reporttitle'),
                labelSeparator:'',
                maxLength:40,
                validator:WtfGlobal.validateHTField,
                maxLengthText:WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.maxlengthtext'),
                emptyText:WtfGlobal.getLocaleText('pm.common.title.insert')
            }]
            },this.subtitle
        ]
    });
    var bgvalue="#FFFFFF";
    var tvalue="#000000";
//    var colorid='bgimg_div'+this.id;
    this.bclrPicker=new Wtf.Panel({
//        id:colorid,
        border:false,
        html:' <div id = "bimg_div'+this.id+'" style="cursor:pointer; height:12px; width:12px; margin:auto; padding:auto; border:thin solid; border-color:'+tvalue+'; background-color:'+bgvalue+';" onclick=\"showPaletteBg(\''+this.id+'\')\"></div>'
    });

//    var tcolorid='txtimg_div'+this.id;
    this.tclrPicker=new Wtf.Panel({
//        id:tcolorid,
        border:false,
        html:'<div id = "timg_div'+this.id+'" style="cursor:pointer; height:12px; width:12px; margin:auto; padding:auto; border:thin solid; border-color:'+tvalue+'; background-color:'+tvalue+';" onclick=\"showPaletteTxt(\''+this.id+'\')\"></div>'
    });
    this.tcc=tvalue;
    this.bcc=bgvalue;

    this.fpager= new Wtf.form.Checkbox({
                    name:'pager',
                    boxLabel:WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.paging'),
                    labelSeparator:'',
                    listeners:{check:this.checkfPager, scope:this}
    });
    this.hpager= new Wtf.form.Checkbox({
                    name:'pager',
                    boxLabel:WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.paging'),
                    labelSeparator:'',
                    listeners:{check:this.checkhPager, scope:this}
    });
    this.hdater= new Wtf.form.Checkbox({
                    name:'dater',
                    boxLabel:WtfGlobal.getLocaleText('lang.date.text'),
                    labelSeparator:'',
                    listeners:{check:this.checkhDater, scope:this}
    });
    this.fdater=new Wtf.form.Checkbox({
                    name:'dater',
                    boxLabel:WtfGlobal.getLocaleText('lang.date.text'),
                    labelSeparator:'',
                    listeners:{check:this.checkfDater, scope:this}
    });

    ////////////////////

//        var colSM = new Wtf.grid.CheckboxSelectionModel({
//            width: 25
//        });

        var repColModel = this.reportGrid.getColumnModel();
        var numCols = this.reportGrid.getColumnModel().getColumnCount();
        var sep="";
        var gridData="";
        for(var i = 1;i<numCols;i++){ // skip row numberer
            if(!(repColModel.isHidden(i))){
                gridData += sep + repColModel.config[i].colRealName;
                sep="~";
            }
        }
        var colHead=new Array();
        var headers=gridData.split("~");
        for(var i=0;i<headers.length;i++){
            if(i==0)
                var head=[headers[i],50];

            if(i == 1 || i == 2)
                head=[headers[i],150];

            if(i > 2)
                head=[headers[i],80];

           colHead.push(head);
        }

        var colCM = new Wtf.grid.ColumnModel([{
            header: WtfGlobal.getLocaleText('lang.column.text'),
            width: 160,
            dataIndex: "column"
        },{
            header: WtfGlobal.getLocaleText('pm.project.plan.export.width'),
            width: 160,
            dataIndex: 'width',
            editor: new Wtf.form.NumberField({
                allowBlank: false,
                maxValue: 750,
                minValue: 40
            })
        }]);
        this.colDS = new Wtf.data.SimpleStore({
            fields: ['column', 'width'],
            data: colHead
        });

        this.gPanel=new Wtf.grid.EditorGridPanel({
            store: this.colDS,
            border: false,
            cm: colCM,
            width: 340,
            clicksToEdit: 1
        });
    ////////////////////

    this.customForm=new Wtf.FormPanel({
        fileUpload: true,
        autoScroll: true,
        border: false,
        width:'100%',
        frame:false,
        method :'POST',
        scope: this,
        labelWidth: 40,
//        bodyStyle : 'border:none;',
        items:[{
            border: false,
            html: '<center><div style="padding-top:10px;color:#154288;font-weight:bold"> '+WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.title')+'</div><hr style = "width:95%;"></center>'
        },{
            layout:'column',
            border: false,
            items:[this.hfieldset,{
                columnWidth: 0.20,
                border: false,
                bodyStyle : 'margin-left:50%;margin-top:15%;',
                items:[this.hdater]
                },{
                columnWidth: 0.19,
                border: false,
                bodyStyle : 'margin-left:15%;margin-top:15%;',
                items:[this.hpager]
            }]
        },{ 
            border: false,
            html: '<center><hr style = "width:95%;"></center>'
        },{
            layout: 'column',
            border: false,
            items:[{
                columnWidth: 0.49,
                border: false,
                items:[{
                    xtype:'fieldset',
                    title: WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.pageborder'),
                    cls: "customFieldSet",
                    defaults : defConf,
                    autoHeight : true,
                    items:[{
                        xtype:'radio',
                        id:'pbordertrue'+this.id,
                        name:'pborder',
                        inputValue :'true',
                        boxLabel:WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.with'),
                        labelSeparator:'',
                        checked:true

                    },
                    {
                        xtype:'radio',
                        name:'pborder',
                        inputValue :'false',
                        labelSeparator:'',
                        boxLabel:WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.no')
                    }
                    ]
                },{
                    xtype:'fieldset',
                    title: WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.dataandgridborder'),
                    cls: "customFieldSet",
                    defaults : defConf,
                    autoHeight : true,
                    items:[{
                        xtype:'radio',
                        id:'gridbordertrue'+this.id,
                        name:'dborder',
                        inputValue :'true',
                         boxLabel:WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.with'),
                        labelSeparator:'',
                        checked:true
                    },
                    {
                        xtype:'radio',
                        name:'dborder',
                        inputValue :'false',
                        labelSeparator:'',
                        boxLabel:WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.no')
                    }]
                },
                {
                    xtype:'fieldset',
                    title: WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.selectbgcolor'),
                    cls: "customFieldSet",
                    id: this.id + 'bcolorPicker',
//                    defaults : defConf,
                    autoHeight : true,
                    items:[this.bclrPicker]
                }]
            },{
                columnWidth: 0.49,
                border: false,
                items:[{
                    xtype:'fieldset',
                    title: WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.pageview'),
                    cls: "customFieldSet",
                    defaults : defConf,
                    autoHeight : true,
                    items:[{
                        xtype:'radio',
                        name:'pview',
                        inputValue :'false',
                        boxLabel:WtfGlobal.getLocaleText('pm.common.portrait'),
                        labelSeparator:'',
                        checked:true
                    },
                    {
                        xtype:'radio',
                        name:'pview',
                        id:'pageviewtrue'+this.id,
                        inputValue :'true',
                        labelSeparator:'',
                         boxLabel:WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.landscape')
                    }]
                },{
                    xtype:'fieldset',
                    title: WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.companylogo'),
                    cls: "customFieldSet",
                    defaults : defConf,
                    autoHeight : true,
                    items:[{
                        xtype:'radio',
                        name:'complogo',
                        inputValue :'false',
                        boxLabel:WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.hide'),
                        labelSeparator:'',
                        checked:true
                    },
                    {
                        xtype:'radio',
                        name:'complogo',
                        id:'companylogo'+this.id,
                        inputValue :'true',
                        labelSeparator:'',
                        boxLabel:WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.show')
                    }]
                },
                {
                    xtype:'fieldset',
                    title: WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.selecttextcolor'),
                    cls: "customFieldSet",
                    id:this.id+'tcolorPicker',
//                    defaults : defConf,
                    autoHeight : true,
                    items:[this.tclrPicker]
                }              
                ]
            }]
        },{
                    xtype:'fieldset',
                    collapsible : true,
//                    collapsed:true,
                    width:'45%',
                    title: WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.colwidth'),
//                    bodyStyle: 'margin-left:30%',
                    cls: "adjustColWidth",
                    id: this.id + 'adjustColWidth',
//                    defaults : defConf,
                    autoHeight : true,
                    items:[this.gPanel]
         },
        {
            border: false,
            html: '<center><hr style = "width:95%;"></center>'
        },{
            layout:'column',
            border: false,
            items:[{
                columnWidth: 0.59,
                border: false,
                items:[{
                    xtype:'fieldset',
                    title: WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.footer'),
                    cls: "customFieldSet",
//                    id:'ffields',
                    defaults : defConf,
                    autoHeight : true,
                    items:[
                    {
                        xtype : 'textfield',
                        id:'footernote'+this.id,
                        fieldLabel:WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.footernote'),
                        maxLength:40,
                        validator:WtfGlobal.validateHTField,
                        maxLengthText:WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.maxlengthtext'),
                        labelSeparator:'',
                        emptyText:WtfGlobal.getLocaleText('pm.project.plan.note.insert')
                    }]
                }]
            },{
                columnWidth: 0.20,
                border: false,
                bodyStyle : 'margin-left:55%;margin-top:15%;',
                items:[this.fdater]
            },{
                columnWidth: 0.20,
                border: false,
                bodyStyle : 'margin-left:15%;margin-top:15%;',
                items:[this.fpager]
            }]
        },
        {
            border: false,
            html: '<center><hr style = "width:95%;"></center>'
        },
        {
            xtype:'button',
            text:'<b>'+WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.button')+'<b>',
            cls:'exportpdfbut',
            scope:this,
            handler:function(){
                if (this.customForm.getForm().isValid())
                 Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.338'),function(btn,text){
                            if(btn =='yes'){
                                this.saveTemplate();
                            }
                            else{
                                this.exportPdf();
                            }
                        },this);
                else
                    Wtf.MessageBox.alert("Alert!", "Please validate entries.");
            }
        }
        ]
    });
    
    Wtf.customReport.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.customReport,Wtf.Panel,{
    onRender: function(conf){
        Wtf.customReport.superclass.onRender.call(this, conf);
        if(this.reportType==2)
            Wtf.getCmp(this.id + 'adjustColWidth').hide();
        this.add(this.customForm);
    },
    removesubtitle:function(){
        this.attachheight -=37;
        this.hfieldset.setHeight(this.attachheight);
        if(this.count>5)
            document.getElementById('subtitlelink'+this.id).style.display='block';
        this.count--;
        if(this.count==1)
            document.getElementById('subtitlelink'+this.id).innerHTML = WtfGlobal.getLocaleText('pm.common.subtitle');
        this.doLayout();
    },
    Addsubtitle:function(){
        var textfield = new Wtf.form.TextField({
            fieldLabel: '',
            labelSeparator:'',
            emptyText:WtfGlobal.getLocaleText('pm.common.subtitle'),
            maxLength:40,
            name: 'subtitle'+(this.count++)
        });
        this.attachheight = this.attachheight+37;
        var pid = 'subtitle'+this.count+this.id;
        this.hfieldset.insert(this.count,new Wtf.Panel({
            id : pid,
            cls:'subtitleAddRemove',
            border: false,
            html:'<a href=\"#\" class ="attachmentlink" style ="margin-left:5px" onclick=\"removesubtitle(\''+pid+'\',\''+this.id+'\')\">'+WtfGlobal.getLocaleText('lang.remove.text')+'</a>',
            items:textfield
            })
        );
        this.hfieldset.setHeight(this.attachheight);
        document.getElementById('subtitlelink'+this.id).innerHTML = WtfGlobal.getLocaleText('pm.project.plan.reports.export.pdf.template.builder.addanother');
        if(this.count>5)
            document.getElementById('subtitlelink'+this.id).style.display='none';

        this.doLayout();
    },
    checkhDater:function(cbox,checked){
        if(checked)
        this.fdater.reset();
    },
    checkfDater:function(cbox,checked){
        if(checked)
        this.hdater.reset();
    },
    checkhPager:function(cbox,checked){
        if(checked)
        this.fpager.reset();
    },
    checkfPager:function(cbox,checked){
        if(checked)
        this.hpager.reset();
    },
    saveTemplate:function(){

        var nameField = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText('lang.name.text'),
            id:'repTemplateName',
            validator: WtfGlobal.validateUserName,
            allowBlank: false,
            width:255
        });
        var descField = new Wtf.form.TextArea({
            id:'repDescField',
            height: 187,
            hideLabel:true,
            cls:'descArea',
            fieldClass : 'descLabel',
            width:356
        });
        var reader = new Wtf.data.ArrayReader({}, [ {
            name: 'Field'
        } ]);
        
            var Template = new Wtf.Window({
                title: WtfGlobal.getLocaleText('pm.project.report.new'),
                width: 390,
                layout: 'border',
                iconCls : 'iconwin',
                modal: true,
                height: 330,
                frame: true,
                border:false,
                items:[{
                    region: 'north',
                    height: 45,
                    width: '95%',
                    id:'northRegion',
                    border:false,
                    items:[{
                        layout:'form',
                        border:false,
                        labelWidth:100,
                        frame:true,
                        items:[nameField]
                    }]
                },{
                    region: 'center',
                    width: '95%',
                    height:'100%',
                    id: 'centerRegion',
                    layout:'fit',
                    border:false,
                    items:[{
                        xtype:'fieldset',
                        title:WtfGlobal.getLocaleText('lang.description.text'),
                        cls: 'textAreaDiv',
                        labelWidth:0,
                        frame:false,
                        border:false,
                        items:[descField]
                    }]
                }],
                buttons:[{
                    text:WtfGlobal.getLocaleText('pm.common.save'),
                    handler: function() {
                        if(!nameField.isValid()) {
                            msgBoxShow(47, 1);
                            return;
                        }
                        this.saveReportTemplate(Template,nameField,descField);
//                        this.url = "";
                        this.exportPdf();
                    },
                    scope: this
                },{
                    text:WtfGlobal.getLocaleText('lang.cancel.text'),
                    handler:function() {
                        Template.close();
                    }
                }]
            });
            Template.show();
    },
    saveReportTemplate:function(win, nameField, descField){
        var tname = WtfGlobal.HTMLStripper(nameField.getValue());
        var description = WtfGlobal.HTMLStripper(descField.getValue());
        if(tname == null && tname == "") {
            Wtf.MessageBox.alert(273);
            return;
        }
        Wtf.Ajax.requestEx({
            url: Wtf.req.prj + 'template.jsp',
            params: {
                action: 'saveReportTemplate',
                projid: this.pid,
                name: tname,
                data: this.generateData(),
                userid: loginid,
                desc: description
            },
            method:'POST'
        },
        this,
        function() {
            Wtf.MessageBox.alert(WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.230'));
        },
        function() {
            msgBoxShow(50, 1);
        });
        win.close();
    },
    exportPdf:function(){
        var data=this.generateData();
        var sendUrl=this.url;
        sendUrl +="&config="+data;//+"&colWidth="+width;
        setDldUrl(sendUrl);
    },
    generateData:function(){
         if(this.reportType==1){
            var colJson = '{"data":['
             var temp="";

             for(var i = 0; i < this.colDS.getCount(); i++){
                 var rec=this.colDS.getAt(i);
                 colJson +=temp+ '{"column":"' + rec.data.column + '","width":"' + rec.data.width+'"}';
                 temp=',';
             }
             colJson +=']}';
         }
         else
             colJson='{"data":""}';
         var subtitles="";
         var tboxes=this.hfieldset.findByType('textfield');
         var headNote=WtfGlobal.ScriptStripper(WtfGlobal.HTMLStripper(tboxes[0].getValue()));
         var title=WtfGlobal.ScriptStripper(WtfGlobal.HTMLStripper(tboxes[1].getValue()));
         var sep="";
         for(i=2; i<tboxes.length; i++){
            subtitles += sep + WtfGlobal.ScriptStripper(WtfGlobal.HTMLStripper(tboxes[i].getValue()));
            sep="~";
         }
         var headDate=this.hdater.getValue();
         var headPager=this.hpager.getValue();
         var footDate=this.fdater.getValue();
         var footPager=this.fpager.getValue();
         var footNote=WtfGlobal.ScriptStripper(WtfGlobal.HTMLStripper(Wtf.getCmp('footernote'+this.id).getValue()));

         var pb=Wtf.getCmp('pbordertrue'+this.id). getGroupValue();
         var gb=(Wtf.getCmp('gridbordertrue'+this.id). getGroupValue());
         var pv=(Wtf.getCmp('pageviewtrue'+this.id). getGroupValue());
         var cl=(Wtf.getCmp('companylogo'+this.id). getGroupValue());
         var tColor = this.tcc.substring(1);
         var bColor = this.bcc.substring(1);
         var data = '{"landscape":"'+pv+'","pageBorder":"'+pb+'","gridBorder":"'+gb+'","title":"'+title +'","subtitles":"'+subtitles +'","headNote":"'+headNote+'","showLogo":"'+cl +'","headDate":"'+headDate+'","footDate":"'+footDate+'","footPager":"'+footPager+'","headPager":"'+headPager+'","footNote":"'+footNote+'","textColor":"'+tColor+'","bgColor":"'+bColor+'","colWidth":'+colJson+'}';
         return data;
    },

    showColorPanelBg: function(obj) {
        var eventargs = obj.target;

        var colorPicker = new Wtf.menu.ColorItem({
            id: 'coloritem'
        });

        var contextMenu = new Wtf.menu.Menu({
            id: 'contextMenu',
            items: [ colorPicker ]
        });
        contextMenu.showAt(Wtf.get(this.id + 'bcolorPicker').getXY());

        colorPicker.on('select', function(palette, selColor){
                this.bcc= '#' + selColor;
                Wtf.get("bimg_div"+this.id).dom.style.backgroundColor = this.bcc;
//                Wtf.get("img_div").dom.innerHTML = "<div class='colorCode' style=\"background-color:"+cc+";\" id='resColorDiv'></div>";
        },this);//{scope: this, eventargs: eventargs});
    },
    showColorPanelTxt: function(obj) {
        var eventargs = obj.target;

        var colorPicker = new Wtf.menu.ColorItem({
            id: 'coloritem'
        });

        var contextMenu = new Wtf.menu.Menu({
            id: 'contextMenu',
            items: [ colorPicker ]
        });
        contextMenu.showAt(Wtf.get(this.id + 'tcolorPicker').getXY());

        colorPicker.on('select', function(palette, selColor){
                this.tcc= '#' + selColor;
                Wtf.get("timg_div"+this.id).dom.style.backgroundColor = this.tcc;
//                Wtf.get("img_div").dom.innerHTML = "<div class='colorCode' style=\"background-color:"+cc+";\" id='resColorDiv'></div>";
        },this);//{scope: this, eventargs: eventargs});
    }
});



function Addsubtitle(objid){
    Wtf.getCmp(objid).Addsubtitle();
}


function removesubtitle(objid,thisid){
    Wtf.getCmp(objid).ownerCt.remove(Wtf.getCmp(objid),true);
    Wtf.getCmp(thisid).removesubtitle();
}
function showPaletteBg(cid){
        Wtf.getCmp(cid).showColorPanelBg(Wtf.get("bimg_div"+cid));
}
function showPaletteTxt(cid){
        Wtf.getCmp(cid).showColorPanelTxt(Wtf.get("timg_div"+cid));
}
