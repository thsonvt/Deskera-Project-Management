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
Wtf.PERTSheet = function(config){
    Wtf.PERTSheet.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.PERTSheet, Wtf.Panel, {
    
    onRender: function(config){
        Wtf.PERTSheet.superclass.onRender.call(this, config);
        var toolBar = [];
        var DecimalRenderer = function(val, metadata, record){
            if(typeof val == 'string')
                val = parseFloat(val);
            return parseFloat(val.toFixed(2));
        }
        if(this.mode == Wtf.proj.EDIT_PERT_SHEET) {
            toolBar.push(
                new Wtf.Toolbar.Button({
                    text: WtfGlobal.getLocaleText('pm.project.plan.cpa.updatepertsheet.save'),
                    id: 'saveSheet'+this.id,
                    scope: this,
                    disabled: true,
                    iconCls: 'dpwnd savePERTSheetIcon',
                    handler: this.savePertSheet
                })
            );
        }
        
        var pertReader = new Wtf.data.KwlJsonReader({
            totalProperty: 'count',
            root: 'data'
        }, Wtf.proj.common.pertRecord);
        
        this.pertStore = new Wtf.data.Store({
            url: '../../projectCPAController.jsp',
            reader: pertReader,
            method: 'GET'
        });
        
        if(this.mode == Wtf.proj.EDIT_PERT_SHEET){
            this.pertStore.load({
                params: {
                    cm: 'getPERTSheet',
                    projectid: this.projectid
                }
            });
        } else if(this.mode == Wtf.proj.SHOW_CPA_PERT_SHEET){
            this.pertStore.load({
                params: {
                    cm: 'getCPAWithPERT',
                    projectid: this.projectid
                }
            });
        } else {
            this.pertStore.load({
                params: {
                    cm: 'getCPAWithoutPERT',
                    projectid: this.projectid
                }
            });
        }
        this.pertStore.on('load', this.showCriticalTasks, this);
        
        var hidePERTColumns = (this.mode == Wtf.proj.EDIT_PERT_SHEET || this.mode == Wtf.proj.SHOW_CPA_PERT_SHEET) ? false : true;
        var hideCPAColumns = (this.mode == Wtf.proj.EDIT_PERT_SHEET) ? true : false;
        
        function fixSummaryValue(v){
            if(v){
                var val = v.toString();
                var indexOfDot = val.indexOf(".");
                if(indexOfDot != -1)
                    val = val.substr(0, indexOfDot + 3);
                v = parseFloat(val);
            }
            return v;
        }
        
        var cm = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header :WtfGlobal.getLocaleText('pm.project.plan.cpa.taskno'),
                dataIndex: 'taskindex',
                width: 30
            },{
                header :WtfGlobal.getLocaleText('pm.common.taskname'),
                dataIndex: 'taskname',
                autoSize : true
            },{
                header: WtfGlobal.getLocaleText('pm.project.plan.cpa.best'),
                dataIndex: 'optimistic',
                id: 'optimistic',
                hidden: hidePERTColumns,
                renderer: DecimalRenderer,
                summaryType: 'sum',
                summaryRenderer: function(v, params, data){
                    v = fixSummaryValue(v);
                    return (v > 1) ? v + ' days' : v + ' day';
                }
            },{
                header: WtfGlobal.getLocaleText('pm.project.plan.cpa.ld'),
                dataIndex: 'likely',
                hidden: hidePERTColumns,
                renderer: DecimalRenderer,
                summaryType: 'sum',
                summaryRenderer: function(v, params, data){
                    v = fixSummaryValue(v);
                    return (v > 1) ? v + ' days' : v + ' day';
                }
            },{
                header: WtfGlobal.getLocaleText('pm.project.plan.cpa.worst'),
                dataIndex: 'pessimistic',
                id: 'pessimistic',
                hidden: hidePERTColumns,
                renderer: DecimalRenderer,
                summaryType: 'sum',
                summaryRenderer: function(v, params, data){
                    v = fixSummaryValue(v);
                    return (v > 1) ? v + ' days' : v + ' day';
                }
            },{
                header: WtfGlobal.getLocaleText('pm.project.plan.cpa.expected'),
                dataIndex: 'expected',
                hidden: hidePERTColumns,
                renderer: DecimalRenderer,
                summaryType: 'sum',
                summaryRenderer: function(v, params, data){
                    v = fixSummaryValue(v);
                    return (v > 1) ? v + ' days' : v + ' day';
                }
            },{
                header: WtfGlobal.getLocaleText('pm.project.plan.cpa.sd'),
                dataIndex: 'sd',
                hidden: hidePERTColumns,
                renderer: DecimalRenderer,
                summaryType: 'sum',
                summaryRenderer: function(v, params, data){
                    v = fixSummaryValue(v);
                    return v;
                }
            },{
                header: WtfGlobal.getLocaleText('pm.project.plan.cpa.variance'),
                dataIndex: 'variance',
                hidden: hidePERTColumns,
                renderer: DecimalRenderer,
                summaryType: 'sum',
                summaryRenderer: function(v, params, data){
                    v = fixSummaryValue(v);
                    return v
                }
            },{
                header: WtfGlobal.getLocaleText('pm.project.plan.cpa.es'),
                dataIndex: 'es',
                hidden: hideCPAColumns,
                renderer: DecimalRenderer
            },{
                header: WtfGlobal.getLocaleText('pm.project.plan.cpa.ef'),
                dataIndex: 'ef',
                hidden: hideCPAColumns,
                renderer: DecimalRenderer
            },{
                header: WtfGlobal.getLocaleText('pm.project.plan.cpa.ls'),
                dataIndex: 'ls',
                hidden: hideCPAColumns,
                renderer: DecimalRenderer
            },{
                header: WtfGlobal.getLocaleText('pm.project.plan.cpa.lf'),
                dataIndex: 'lf',
                hidden: hideCPAColumns,
                renderer: DecimalRenderer
            },{
                header: WtfGlobal.getLocaleText('pm.project.plan.cpa.slack'),
                dataIndex: 'slack',
                hidden: hideCPAColumns,
                renderer: DecimalRenderer
            },{
                header: WtfGlobal.getLocaleText('pm.project.plan.cpa.critical'),
                dataIndex: 'iscritical',
                hidden: hideCPAColumns,
                width: 40,
                renderer: function(val){
                    if(val) return WtfGlobal.getLocaleText('lang.yes.text');
                    else return WtfGlobal.getLocaleText('lang.no.text');
                }
            }
        ]);
        
        var sm = new Wtf.grid.RowSelectionModel({
            singleSelect: true
        });
        
        this.summary = new Wtf.grid.GridSummary();
        
        if(this.mode == Wtf.proj.EDIT_PERT_SHEET){
            this.pertSheet = new Wtf.grid.EditorGridPanel({
                id: 'pertSheet'+this.id,
                store: this.pertStore,
                region: 'center',
                cm: cm,
                sm: sm,
                border: false,
                frame: true,
                loadMask: true,
                stripeRows: false,
                clicksToEdit: 1,
                autoExpandColumn: 2,
                autoScroll: true,
                tbar: toolBar,
                viewConfig: {
                    emptyText: WtfGlobal.getLocaleText('pm.common.nodatadisplay'),
                    forceFit: true
                }
            });
            cm.getColumnById('optimistic').editor = new Wtf.grid.GridEditor(new Wtf.form.NumberField({
               allowNegative: false,
               allowDecimals: true
            }));
            cm.getColumnById('pessimistic').editor = new Wtf.grid.GridEditor(new Wtf.form.NumberField({
               allowNegative: false,
               allowDecimals: true
            }));
            this.pertSheet.on('beforeedit', this.setCellsDisabled, this);
            this.pertSheet.on('afteredit', this.setPertSheetValues, this);
        } else {
            this.pertSheet = new Wtf.grid.GridPanel({
                id: 'pertSheet'+this.id,
                store: this.pertStore,
                width: '100%',
                region: 'center',
                cm: cm,
                sm: sm,
                border: false,
                frame: true,
                loadMask: true,
                stripeRows: false,
                autoScroll: true,
                viewConfig: {
                    emptyText: WtfGlobal.getLocaleText('pm.common.nodatadisplay'),
                    forceFit: true
                },
                plugins: this.summary
            });
        }
        
        var txt = this.getPanelInfo();
        
        var infoPanel = new Wtf.Panel({
            border: false,
            frame: true,
            title: WtfGlobal.getLocaleText("pm.project.plan.cpa.infopanel"),
            id: 'pertInfo'+this.id,
            height: 100,
            region: 'north',
            width: '100%',
            collapsible: true,
            bodyStyle: 'background-color: #F1F1F1; margin: 0px 5px;',
            html: txt
        });
        infoPanel.on('collapse', this.adjustSheetHeight, this);
        infoPanel.on('expand', this.adjustSheetHeight, this);
        
        var pertExtraCalc = new Wtf.Panel({
            id: 'pertExtraCalc'+this.id,
            border: false,
            frame: true,
            title: WtfGlobal.getLocaleText('pm.project.plan.cpa.pertanalysis'),
            height: 110,
            region: 'south',
            width: '100%',
            bodyStyle: 'background-color: #F1F1F1; margin: 0px 5px;'
        });
        
        this.add(infoPanel);
        this.add(this.pertSheet);
        if(this.mode == Wtf.proj.SHOW_CPA_PERT_SHEET)
            this.add(pertExtraCalc)
        
        if(this.mode != Wtf.proj.SHOW_CPA_PERT_SHEET)
            this.pertSheet.setHeight(this.ownerCt.getEl().getHeight() - 100);
        else
            this.pertSheet.setHeight(this.ownerCt.getEl().getHeight() - 100 - 110);
        
        this.doLayout();
       
        if(this.mode == Wtf.proj.SHOW_CPA_WITHOUT_PERT_SHEET)
            this.summary.toggleSummary(false);
        
    },
    
    adjustSheetHeight: function(panel){
        var comp = Wtf.getCmp('pertExtraCalc'+this.id);
        if(this.mode != Wtf.proj.SHOW_CPA_PERT_SHEET)
            this.pertSheet.setHeight(this.ownerCt.getEl().getHeight() - panel.getEl().getHeight());
        else {
            if(comp)
                this.pertSheet.setHeight(this.ownerCt.getEl().getHeight() - panel.getEl().getHeight() - comp.getEl().getHeight());
        }
    },
    
    getPERTExtraCalcForm: function(pertExtraCalc){
        var total = this.summary.getTotal();
        pertExtraCalc.add({
            xtype: 'panel',
            layout: 'column',
            height: 35,
            width: '60%',
            bodyStyle : 'font-size: 12px; padding:10px 10px 0px 10px;',
            defaults: {
                style: 'margin: 0px 10px 0px 10px;'
            },
            items: [{
                columnWidth: 0.45,
                html: WtfGlobal.getLocaleText('pm.project.plan.cpa.pertanalysis.info1'),
                border: false
            },{
                columnWidth: 0.1,
                xtype: 'numberfield',
                id: 'desireddur'+ this.id,
                allowBlank: false,
                allowDecimals: true,
                value: total.likely
            },{
                columnWidth: 0.1,
                xtype: 'button',
                setSize: Wtf.emptyFn,
                text: WtfGlobal.getLocaleText('lang.submit.text'),
                scope: this,
                handler: function(btn){
                    var f = Wtf.getCmp('desireddur'+this.id);
                    if(f && f.isValid()){
                        f = f.getValue();
                        Wtf.Ajax.requestEx({
                            url: '../../projectCPAController.jsp',
                            params: {
                                cm: 'getProbability',
                                projectid: this.projectid,
                                desiredduration: f,
                                sumofexpected: total.expected,
                                sumofvariance: total.variance
                            }
                        }, this, function(res, req){
                            var obj = eval('('+ res + ')');
                            if(obj && obj.success) {
                                var val = obj.probability * 100;
                                var ele = Wtf.fly('prob'+this.id);
                                if(ele)
                                    ele.dom.innerHTML = '<b>' + val.toFixed(2) + '%</b> (approx)';
                            } else 
                                msgBoxShow(20, 1);
                        }, function(res, req){
                            msgBoxShow(20, 1);
                        });
                    }
                }
            }]
        },{
            xtype: 'panel',
            layout: 'fit',
            height: 35,
            width: '100%',
            bodyStyle : 'font-size: 12px; padding:0px 10px 0px 10px;',
            html: '<br><div style="margin-left:10px;">'+WtfGlobal.getLocaleText("pm.project.plan.cpa.probability")+' \n\
            <span id="prob'+this.id+'"></span></div>'
        });
        pertExtraCalc.doLayout();
    },
    
    savePertSheet: function(btn){
        var count = this.pertStore.getCount();
        var jstr = "[";
        for(var i = 0; i < count; i++){
            var objStr = "{";
            var rec = this.pertStore.getAt(i);
            objStr += "\"task\":{\"taskID\":\""+rec.get('taskid')+"\", \"taskName\":\""+rec.get('taskname')+"\", \"taskIndex\":\""+rec.get('taskindex')+
                "\"}, \"optimistic\":\""+rec.get('optimistic')+"\", \"pessimistic\":\""+rec.get('pessimistic')+"\", \"likely\":\""+rec.get('likely')+
                "\", \"expected\":\""+rec.get('expected')+"\", \"sd\":\""+rec.get('sd')+"\", \"variance\":\""+rec.get('variance')+"\", \"es\":\""+rec.get('es')+
                "\", \"ef\":\""+rec.get('ef')+"\", \"ls\":\""+rec.get('ls')+"\", \"lf\":\""+rec.get('lf')+"\", \"slack\":\""+rec.get('slack')+
                "\", \"isCritical\":"+rec.get('iscritical');
            objStr += "}";
            jstr += objStr + ",";
        }
        jstr = jstr.substring(0, jstr.length - 1);
        jstr += "]";
        
        Wtf.Ajax.requestEx({
            url: '../../projectCPAController.jsp',
            params: {
                cm: 'savePERTSheet',
                projectid: this.projectid,
                data: jstr
            }
        }, this, function(res, req){
            var obj = eval('('+res+')');
            if(obj && obj.success){
                msgBoxShow(210, 0);
            } else {
                msgBoxShow(59, 1);
            }
        }, function(res, req){
            msgBoxShow(59, 1);
        });
        
    },
    
    setPertSheetValues: function(e){
        var nVal = WtfGlobal.HTMLStripper(e.value);
        var patt1 = new RegExp("^(\\d+\\.?\\d*|\\d*\\.?\\d+)$");
        var rec = e.record;
        if(patt1.test(nVal)){
             var duval = this.getValidDuration(nVal);
             var a = new RegExp(".0{2,3}$")
             var z = duval.replace(a, "");
             if(typeof z == 'string')
                z = parseFloat(z);
             rec.set(e.field, z);
        }
        var o = e.record.get('optimistic');
        var p = e.record.get('pessimistic');
        var l = e.record.get('likely');
        var exp = (o + (l * 4) + p) / 6;
        exp = (exp < 0) ? 0 : exp;
        e.record.set('expected', parseFloat(exp.toFixed(2)));
        exp = (p - o) / 6;
        exp = (exp < 0) ? 0 : exp;
        e.record.set('sd', parseFloat(exp.toFixed(2)));
        e.record.set('variance', parseFloat(Math.pow(exp, 2).toFixed(2)));
        e.record.commit();
        if(Wtf.getCmp('saveSheet'+this.id).disabled)
           Wtf.getCmp('saveSheet'+this.id).setDisabled(false); 
    },
    
    setCellsDisabled: function(e){
        if(e.record.get('task').isParent || e.record.get('task').isMilestone){
            e.cancel = true;
        }
        return;
    },
    
    getValidDuration: function(str){
        var slength = str.length;
        var dur;
        var patt1=new RegExp(".");
        if(str.search(patt1)!=-1){
            if((index=str.search(patt1.compile("d")))!=-1){
                if(str.substr(0, index)) // if-else check for chrome
                    dur = parseFloat(str.substr(0, index)).toFixed(2) + str.substr(index, slength);
                else
                    dur = str.substr(index, slength);
            } else {
                dur = parseFloat(str).toFixed(2);
            }
            return dur;
        } else {
            return str;
        }
    },
    
    showCriticalTasks: function(){
        var count = this.pertStore.getCount();
        var view = this.pertSheet.getView();
        for(var i = 0; i < count; i++){
            var rec = this.pertStore.getAt(i);
            if(this.mode != Wtf.proj.EDIT_PERT_SHEET){
                if(rec.get('iscritical')){
                    view.getRow(i).className += " conflicted";
                }
            } else {
                if(rec.get('task').isMilestone || rec.get('task').isParent){
                    view.getRow(i).className += " grayrow";
                }
            }
        }
        if(this.mode == Wtf.proj.SHOW_CPA_PERT_SHEET){
            view = Wtf.getCmp('pertExtraCalc'+this.id);
            if(view)
                this.getPERTExtraCalcForm(view);
        }
    },
    
    getPanelInfo: function(){
        var html = "<div style='font-size: 12px; padding: 10px; color: #15428B;'>";
        if(this.mode == Wtf.proj.SHOW_CPA_WITHOUT_PERT_SHEET){
            html += WtfGlobal.getLocaleText('pm.Help.cpaWithoutPert');
        } else if(this.mode == Wtf.proj.SHOW_CPA_PERT_SHEET){
            html += WtfGlobal.getLocaleText('pm.Help.cpaWithPert');
        } else {
            html += WtfGlobal.getLocaleText('pm.Help.cpaPertSheet');
        }
        html += "</div>";
        return html;
    }
    
});
