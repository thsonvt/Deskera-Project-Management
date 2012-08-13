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
Wtf.proj.containerPanel = function(config) {
    Wtf.apply(this,config);
    this.pid = this.id;
    this.curr_baseline = null;
    this.baselineflag = 0;
    var topBar = [];
    var bottomBar = [];
    topBar.push(this.searchTask = new Wtf.form.TextField({
        id: this.pid+'searchTask',
        emptyText: WtfGlobal.getLocaleText('pm.project.plan.search')
    }));
    topBar.push('-');
    if(!this.archived && !(this.connstatus >= 6 && this.connstatus <= 8)){
        topBar.push(this.templateMenu = new Wtf.Toolbar.Button({
            iconCls: 'dpwnd inserttpl',
            tooltip: {
                title:WtfGlobal.getLocaleText('pm.common.template.text'),
                text: WtfGlobal.getLocaleText('pm.Help.projplannewtemp')
            },
            scope: this,
            menu: [
            this.sabase = new Wtf.Action({
                text: WtfGlobal.getLocaleText('pm.project.plan.template.create.text'),
                iconCls: 'dpwnd createtpl',
                handler: this.CreateTemplateOfTask,
                id: 'projPlanCreateTpl',
                scope: this,
                tooltip: {
                    title:WtfGlobal.getLocaleText('pm.project.plan.template.create.text'),
                    text: WtfGlobal.getLocaleText('pm.Help.projplancreatetemp')
                }
            }),
            this.insertTemplateButton = new Wtf.Action({
                text: WtfGlobal.getLocaleText('pm.project.plan.template.insert'),
                iconCls: 'dpwnd inserttpl',
                id: 'projPlanInsertTpl',
                tooltip: {
                    title:WtfGlobal.getLocaleText('pm.project.plan.template.insert'),
                    text: WtfGlobal.getLocaleText('pm.Help.projplaninserttemp')
                },
                handler: function(){
                    var smTmp = this.editGrid.getSelectionModel();
                    if(smTmp.getCount()<1){
                        msgBoxShow(159, 0);
                        return ;
                    } else {
                        this.InsertTemplate(0);
                        smTmp.clearSelections();
                    }
                },
                scope: this
            }),
            this.cobase = new Wtf.Action({
                text: WtfGlobal.getLocaleText('pm.common.template.delete.text'),
                id: 'projPlanDeleteTpl',
                tooltip: {
                    title:WtfGlobal.getLocaleText('pm.common.template.delete.text'),
                    text: WtfGlobal.getLocaleText('pm.Help.projplandeltemp')
                },
                iconCls : "dpwnd deletetpl",
                handler: function(){
                    this.InsertTemplate(1);
                },
                scope: this
            })
            ]
        }));
        topBar.push('-');
        topBar.push(this.newTaskButton = new Wtf.Toolbar.Button({
            icon: '../../images/New.gif',
            tooltip: {
                title:WtfGlobal.getLocaleText('pm.project.plan.menu.newtask'),
                text: WtfGlobal.getLocaleText('pm.Help.projplannew')
            },
            iconCls: 'iconclass',
            id: 'projPlanNewTask',
            handler: function() {
                var smTmp = this.editGrid.getSelectionModel();
                if(smTmp.getCount()>0){
                    this.editGrid.InsertTaskInGrid(this.editGrid.krow);
                    smTmp.clearSelections();
                } else
                    msgBoxShow(160, 0);
            },
            scope: this
        }));
        topBar.push(this.deleteTaskButton = new Wtf.Toolbar.Button({
            tooltip: {
                title:WtfGlobal.getLocaleText('pm.project.plan.menu.delete'),
                text: WtfGlobal.getLocaleText('pm.Help.projplandelete')
            },
            iconCls: 'pwnd delicon',
            id: 'delete',
            handler: function() {
                if(!(this.editGrid.getSelectionModel().getSelections().length>0)){
                    msgBoxShow(39, 1);
                    return;
                }
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.324'), function(btn){
                    if(btn == "no")
                        return;
                    else if(btn == "yes") {
                        this.clientServerChange = 'client';
                        this.editGrid.requestDeleteTask();
                    }
                }, this);
            },
            scope: this
        }));
        topBar.push('-');
        topBar.push(this.cutTaskButton = new Wtf.Toolbar.Button({
            iconCls: 'dpwnd cuticon',
            tooltip: {
                title:WtfGlobal.getLocaleText('pm.project.plan.menu.cut'),
                text: WtfGlobal.getLocaleText('pm.Help.projplancut')
            },
            id: 'cutTask',
            handler: function() {
                if(!(this.editGrid.getSelectionModel().getSelections().length>0)){
                    msgBoxShow(40, 1);
                    return;
                }
                this.editGrid.loadbuffer("cutTask");
                this.editGrid.requestDeleteTask();
            },
            scope: this
        }));
        topBar.push(this.copyTaskButton = new Wtf.Toolbar.Button({
            iconCls: 'dpwnd cpyicon',
            tooltip: {
                title:WtfGlobal.getLocaleText('pm.project.plan.menu.copy'),
                text: WtfGlobal.getLocaleText('pm.Help.projplancopy')
            },
            id: 'copyTask',
            handler: function() {
                if(!(this.editGrid.getSelectionModel().getSelections().length>0)){
                    msgBoxShow(41, 1);
                    return;
                }
                this.editGrid.loadbuffer("copyTask");
            },
            scope: this
        }));
        topBar.push(this.pasteButton = new Wtf.Toolbar.Button({
            iconCls: 'dpwnd pasteicon',
            tooltip: {
                title:WtfGlobal.getLocaleText('pm.project.plan.menu.paste'),
                text: WtfGlobal.getLocaleText('pm.Help.projplanpaste')
            },
            id: 'pasteButton',
            handler: function() {
                if(!this.editGrid.pasteFlag)
                    this.editGrid.insertbuffer('paste');
            },
            scope: this
        }));
        topBar.push('-');
        topBar.push(this.outdentButton = new Wtf.Toolbar.Button({
            iconCls: 'dpwnd leftarw',
            tooltip: {
                title:WtfGlobal.getLocaleText('pm.project.tak.outdent'),
                text: WtfGlobal.getLocaleText('pm.Help.projplanoutdent')
            },
            id: 'projPlanOutdent',
            detailTip:"Outdenting a task moves the task out one level.",
            handler: function() {
                if(!(this.editGrid.getSelectionModel().getSelections().length>0)){
                    msgBoxShow(42, 1);
                    return;
                }
                this.editGrid.outdent();
            },
            scope: this
        }));
        topBar.push(this.indentButton = new Wtf.Toolbar.Button({
            iconCls: 'dpwnd rightarw',
            tooltip: {
                title:WtfGlobal.getLocaleText('pm.project.task.indent.text'),
                text: WtfGlobal.getLocaleText('pm.Help.projplanindent')
            },
            id: 'projPlanIndent',
            handler: function() {
                if(!(this.editGrid.getSelectionModel().getSelections().length>0)){
                    msgBoxShow(43, 1);
                    return;
                }
                this.editGrid.indent();
            },
            scope: this
        }));
        topBar.push(this.moveUpButton = new Wtf.Toolbar.Button({
            iconCls: 'dpwnd moveup',
            tooltip: {
                title:WtfGlobal.getLocaleText('pm.project.plan.moveup'),
                text: WtfGlobal.getLocaleText('pm.Help.projplanmoveup')
            },
            id: 'projPlanMoveUp',
            handler: function(btn) {
                var len = this.editGrid.getSelectionModel().getSelections().length;
                if(len !== 1){
                    msgBoxShow(189, 1);
                    return;
                } else {
                    this.editGrid.moveUp(btn);
                }
            },
            scope: this
        }));
        topBar.push(this.moveDownButton = new Wtf.Toolbar.Button({
            iconCls: 'dpwnd movedown',
            tooltip: {
                title:WtfGlobal.getLocaleText('pm.project.task.movedown'),
                text: WtfGlobal.getLocaleText('pm.Help.projplanmovedown')
            },
            id: 'projPlanMoveDown',
            handler: function(btn) {
                if(this.editGrid.getSelectionModel().getSelections().length !== 1){
                    msgBoxShow(189, 1);
                    return;
                } else
                    this.editGrid.moveDown(btn);
            },
            scope: this
        }));
        topBar.push('-');
    }
    var sStore = new Wtf.data.SimpleStore({
        fields: ['id', 'value'],
        data: [
        ['1', WtfGlobal.getLocaleText('pm.project.plan.dayview')],
        ['2', WtfGlobal.getLocaleText('pm.project.plan.view.week')]
        ]
    });
    this.sCombo = new Wtf.form.ComboBox({
        store: sStore,
        mode: "local",
        width: 130,
        valueField: "id",
        typeAhead: false,
        editable: false,
        selectOnFocus: true,
        triggerAction: "all",
        displayField: "value",
        emptyText: WtfGlobal.getLocaleText('lang.view.select'),
        id: this.id+'projPlanView'
    });
    this.sCombo.on("select", this.projViewSelect, this);
    bottomBar.push(this.sCombo);
    bottomBar.push('-');
    bottomBar.push(this.showTaskProgress = new Wtf.Toolbar.Button({
        iconCls: 'dpwnd progressicon',
        tooltip: {
            title:WtfGlobal.getLocaleText('pm.project.plan.task.attributes.taskprogress'),
            text: WtfGlobal.getLocaleText('pm.Help.projplanprogress')
        },
        enableToggle: true,
        id: 'projPlanProgress'+this.id,
        toggleGroup: 'showStatus'+this.id,
        handler: function() {
            this.checkToggledBtn("taskprogress");
            this.editGrid.showProgress();
        },
        scope: this
    }));
    bottomBar.push(this.showOverdue = new Wtf.Toolbar.Button({
        iconCls: 'dpwnd overdueicon',
        tooltip: {
            title:WtfGlobal.getLocaleText('pm.project.task.overdue'),
            text: WtfGlobal.getLocaleText('pm.Help.projplanoverdue')
        },
        toggleGroup: 'showStatus'+this.id,
        id: 'projPlanOverdue'+this.id,
        enableToggle: true,
        handler: function() {
            this.checkToggledBtn("taskoverdue");
            this.cControl.showOverdueTask();
        },
        scope: this
    }));
    bottomBar.push(this.showPriority = new Wtf.Toolbar.Button({
        iconCls: 'dpwnd priorityicon',
        tooltip: {
            title:WtfGlobal.getLocaleText('pm.project.tesk.priority'),
            text: WtfGlobal.getLocaleText('pm.Help.projplanpriority')
        },
        toggleGroup: 'showStatus'+this.id,
        id: 'projPlanPriority'+this.id,
        enableToggle: true,
        handler: function() {
            this.checkToggledBtn("taskpriority");
            this.editGrid.showPriority();
        },
        scope: this
    }));
    bottomBar.push(this.showResources = new Wtf.Toolbar.Button({
        iconCls: 'dpwnd resourceicon',
        tooltip: {
            title:WtfGlobal.getLocaleText('pm.project.task.resources'),
            text: WtfGlobal.getLocaleText('pm.Help.projplanresources')
        },
        toggleGroup: 'showStatus'+this.id,
        id: 'projPlanResource'+this.id,
        enableToggle: true,
        handler: function() {
            this.checkToggledBtn("taskresources");
            this.editGrid.showResources();
        },
        scope: this
    }));
    if(!this.archived && (this.connstatus != 6 && this.connstatus != 7)){
        bottomBar.push(this.showTaskPanel = new Wtf.Toolbar.Button({
            id:'taskPanelIcon'+this.pid,
            iconCls: 'dpwnd taskpanelenabledicon',
            tooltip: {
                title:WtfGlobal.getLocaleText('pm.project.plan.menu.taskinfo'),
                text: WtfGlobal.getLocaleText('pm.Help.projplaninfo')
            },
            disabled: false,
            handler: function() {
                var len = this.editGrid.getSelectionModel().getSelected();
                if(!len){
                    msgBoxShow(189, 1);
                    return;
                } else {
                    this.editGrid.ShowModal();
                }
            },
            scope: this
        }));
        if(this.connstatus != 8){
            bottomBar.push('-');
            bottomBar.push(this.syncProject = new Wtf.Toolbar.Button({
                iconCls: 'pwnd calsync',
                tooltip: {
                    title: WtfGlobal.getLocaleText('pm.project.sync'),
                    text: WtfGlobal.getLocaleText('pm.Help.projplansync')
                },
                scope: this,
                id: 'projPlanCalSync',
                handler: this.calSync
            }));
            topBar.push(new Wtf.Toolbar.Button({
                iconCls: 'pwnd importicon',
                tooltip: {
                    title:WtfGlobal.getLocaleText('pm.project.plan.menu.import'),
                    text: WtfGlobal.getLocaleText('pm.project.plan.importtext')
                },
                scope: this,
                id: this.id +'projPlanImport',
                menu: [
                this.importMS = new Wtf.Action({
                    text: WtfGlobal.getLocaleText('pm.project.plan.import.msproject.text'),
                    iconCls: 'pwnd importicon',
                    handler: function() {
                        this.editGrid.importfile("mpx");
                    },
                    tooltip: {
                        text: WtfGlobal.getLocaleText('pm.Help.projplanimportMS')
                    },
                    scope: this
                }),
                this.importCSV = new Wtf.Action({
                    text: WtfGlobal.getLocaleText('pm.project.plan.import.csv.text'),
                    iconCls: 'pwnd importicon',
                    handler: function() {
                        this.editGrid.importfile("csv");
                    },
                    tooltip: {
                        text: WtfGlobal.getLocaleText('pm.Help.projplanimportCSV')
                    },
                    scope: this
                })
                ]
            }));
        }
    }
    if(this.connstatus != 6) {
        topBar.push(this.exportPlan = new Wtf.Toolbar.Button({
            iconCls: 'pwnd exporticon',
            tooltip: {
                title:WtfGlobal.getLocaleText('pm.project.plan.export.text'),
                text: WtfGlobal.getLocaleText('pm.Help.projplanexport')
            },
            scope: this,
            id: this.id + 'projPlanExport',
            menu: [
            this.exportMS = new Wtf.Action({
                text: WtfGlobal.getLocaleText('pm.common.export.msproject'),
                iconCls: 'pwnd exporticon',
                handler: function() {
                    if(this.chkExport())
                        this.editGrid.exportfile("mpx");
                },
                tooltip: {
                    text: WtfGlobal.getLocaleText('pm.Help.projplanexportMS')
                },
                scope: this
            }),
            this.exportCSV = new Wtf.Action({
                text: WtfGlobal.getLocaleText('pm.project.plan.export.csv'),
                iconCls: 'pwnd csvexporticon',
                handler: function() {
                    if(this.chkExport())
                        this.editGrid.exportfile("csv");
                },
                tooltip: {
                    text: WtfGlobal.getLocaleText('pm.Help.projplanexportCSV')
                },
                scope: this
            }),
            this.exportPDF = new Wtf.Action({
                text: WtfGlobal.getLocaleText('pm.project.plan.export.pdf'),
                iconCls: 'pwnd pdfexporticon',
                handler: function() {
                    if(this.chkExport())
                        this.exportOptions("pdf");
                },
                tooltip: {
                    text: WtfGlobal.getLocaleText('pm.Help.projplanexportPDF')
                },
                scope: this
            }),
            this.EMBED = new Wtf.Action({
                text: WtfGlobal.getLocaleText('pm.project.plan.export.embed'),
                iconCls: 'pwnd exporticon',
                handler: function() {
                    this.exportOptions("embed");
                },
                tooltip: {
                    text: WtfGlobal.getLocaleText('pm.Help.projplanexportEMBED')
                },
                scope: this
            })]
        }));
        topBar.push('-');
        if(!this.archived && !(this.connstatus >= 6 && this.connstatus <= 8)){
            topBar.push(new Wtf.Toolbar.Button({
                iconCls: 'dpwnd runCPAIcon',
                tooltip: {
                    title:WtfGlobal.getLocaleText('pm.project.plan.menu.cpa'),
                    text: WtfGlobal.getLocaleText('pm.Help.cpatip')
                },
                id: 'projPlanCPA'+this.id,
                scope: this,
                menu: [
                new Wtf.Action({
                    text: WtfGlobal.getLocaleText('pm.project.plan.cpa.setdefault'),
                    iconCls: 'dpwnd durDiffIcon',
                    id: 'setPertDifference'+this.id,
                    handler: this.setPERTDifferences,
                    mode: Wtf.proj.EDIT_PERT_SHEET,
                    hidden: '',
                    tooltip: {
                        text: WtfGlobal.getLocaleText('pm.Help.cpadefaultdifftip')
                    },
                    scope: this
                }),
                new Wtf.Action({
                    text: WtfGlobal.getLocaleText('pm.project.plan.cpa.updatepertsheet'),
                    iconCls: 'dpwnd updatePERTSheetIcon',
                    id: 'updatePertSheet'+this.id,
                    handler: this.getPERTSheet,
                    mode: Wtf.proj.EDIT_PERT_SHEET,
                    hidden: '',
                    tooltip: {
                        text: WtfGlobal.getLocaleText('pm.Help.cpaupdatepertsheettip')
                    },
                    scope: this
                }),
                new Wtf.Action({
                    text: WtfGlobal.getLocaleText('pm.project.plan.cpa.run'),
                    iconCls : "dpwnd runCPAIcon",
                    scope: this,
                    menu: [
                    new Wtf.Action({
                        text: WtfGlobal.getLocaleText('pm.project.plan.cpa.run.using'),
                        iconCls: 'dpwnd runCPAIcon',
                        id: 'cpaWithPert'+this.id,
                        mode: Wtf.proj.SHOW_CPA_PERT_SHEET,
                        handler: this.getPERTSheet,
                        tooltip: {
                            text: WtfGlobal.getLocaleText('pm.Help.cparuncpaperttip')
                        },
                        scope: this
                    }),
                    new Wtf.Action({
                        text: WtfGlobal.getLocaleText('pm.project.plan.cpa.run.withoutusing'),
                        iconCls : "dpwnd runCPAIcon",
                        id: 'cpaWithoutPert'+this.id,
                        mode: Wtf.proj.SHOW_CPA_WITHOUT_PERT_SHEET,
                        handler: this.getPERTSheet,
                        tooltip: {
                            text: WtfGlobal.getLocaleText('pm.Help.cparuncpatip')
                        },
                        scope: this
                    })
                    ]
                })
                ]
            }),
            new Wtf.Toolbar.Button({
                iconCls: 'dpwnd runCPAOnGCIcon',
                tooltip: {
                    title:WtfGlobal.getLocaleText('pm.project.plan.menu.viewcpa'),
                    text: WtfGlobal.getLocaleText('pm.Help.cpashowcpatip')
                },
                enableToggle: true,
                id: 'projPlanViewCPA'+this.id,
                handler: this.showCPOnGC,
                scope: this
            }));
            topBar.push('-');
        }

        if(!this.archived && !(this.connstatus >= 6 && this.connstatus <= 8)){
            if(Wtf.subscription.proj.subModule.bsln) {
                topBar.push(new Wtf.Toolbar.Button({
                    iconCls: 'dpwnd baselineicon',
                    tooltip: {
                        title:WtfGlobal.getLocaleText('pm.common.baseline.text'),
                        text: WtfGlobal.getLocaleText('pm.Help.projplanbaseline')
                    },
                    id: this.id +'projPlanBaseline',
                    scope: this,
                    menu: [
                    this.savebase = new Wtf.Action({
                        text: WtfGlobal.getLocaleText('pm.project.plan.baseline.save'),
                        iconCls: 'dpwnd savebaselineicon',
                        handler: this.showBaselineWindow,
                        tooltip: {
                            text: WtfGlobal.getLocaleText('pm.Help.savebaseline')
                        },
                        scope: this
                    })
                    ,
                    this.viewbase = new Wtf.Action({
                        text: WtfGlobal.getLocaleText('pm.project.plan.baseline.view'),
                        iconCls : "dpwnd baselineicon",
                        mode: 1,
                        handler: this.viewBaseline,
                        tooltip: {
                            text: WtfGlobal.getLocaleText('pm.Help.viewbaseline')
                        },
                        scope: this
                    }),
                    this.compbase = new Wtf.Action({
                        text: WtfGlobal.getLocaleText('pm.project.baseline.compare'),
                        iconCls : "dpwnd comparebaselineicon",
                        handler: this.viewBaseline,
                        tooltip: {
                            text: WtfGlobal.getLocaleText('pm.Help.comparebaseline')
                        },
                        mode: 0,
                        scope: this
                    })
                    ]
                }));
                topBar.push('-');
            }
            bottomBar.push(new Wtf.Toolbar.Button({
                iconCls: 'dpwnd projstartdate',
                tooltip: {
                    title:WtfGlobal.getLocaleText('pm.project.plan.menu.startdate'),
                    text: WtfGlobal.getLocaleText('pm.Help.projplanstartdate')
                },
                scope: this,
                id: 'projPlanStartDate',
                handler: this.changeStartDate
            }));
            bottomBar.push(this.sendProjectNotify = new Wtf.Toolbar.Button({
                iconCls: 'notification',
                tooltip: {
                    title:WtfGlobal.getLocaleText('pm.common.notifications'),
                    text: WtfGlobal.getLocaleText('pm.Help.projplannotify')
                },
                scope: this,
                id: this.id +'projPlanNotification',
                menu: [
                this.sendselect = new Wtf.Action({
                    iconCls: 'notification',
                    text: WtfGlobal.getLocaleText('pm.common.notifyselected'),
                    name: 'cost',
                    handler: this.notify,
                    tooltip: {
                        text: WtfGlobal.getLocaleText('pm.Help.projplansendselect')
                    },
                    scope: this
                }),
                this.sendall = new Wtf.Action({
                    iconCls: 'notification',
                    text: WtfGlobal.getLocaleText('pm.common.notifyall'),
                    name: 'time',
                    handler: this.notifyAll,
                    tooltip: {
                        text: WtfGlobal.getLocaleText('pm.Help.projplansendall')
                    },
                    scope: this
                }),
                this.sendoverdue = new Wtf.Action({
                    iconCls: 'notification',
                    text: WtfGlobal.getLocaleText('pm.common.notifyoverdue'),
                    handler: this.notifyOverdue,
                    tooltip: {
                        text: WtfGlobal.getLocaleText('pm.Help.projplansendoverdue')
                    },
                    scope: this
                })]
            }));
            topBar.push(this.resavailable = new Wtf.Toolbar.Button({
                id:'resourceConflict'+this.pid,
                iconCls: 'dpwnd conflicticon',
                tooltip: {
                    title:WtfGlobal.getLocaleText('pm.project.plan.rescon.text'),
                    text: WtfGlobal.getLocaleText('pm.Help.projplanrescon')
                },
                handler: function() {
                    if(this.editGrid.dstore.getCount() > 0) {
                        var tabId = "resourceConflictTab" + this.pid;
                        if(!Wtf.getCmp(tabId)) {
                            var tpanel = Wtf.getCmp('subtabpanelcomprojectTabs_' + this.pid);
                            var newPanel = this.editGrid.resourceConflictTab(WtfGlobal.getLocaleText('pm.project.plan.rescon.text'),tabId);
                            tpanel.add(newPanel);
                            if(!Wtf.isIE7) // TEMP FIX: Causing layout problems in other tabs. Only in IE7.
                                tpanel.doLayout();
                            tpanel.setActiveTab(newPanel);
                            newPanel.mask = new Wtf.LoadMask(document.body);
                        } else {
                            Wtf.getCmp('subtabpanelcomprojectTabs_' + this.pid).setActiveTab(Wtf.getCmp(tabId));
                        }
                    }
                },
                scope: this
            }));
            topBar.push('-');
        }
        //    topBar.push('-');
        //    if(!this.archived && !(this.connstatus >= 6 && this.connstatus <= 8)){
        //        topBar.push(this.viewModeBtn = new Wtf.Toolbar.Button({
        //            id:'viewMode'+this.pid,
        //            iconCls: 'dpwnd planmodeicon',
        //            tooltip: {
        //                title:WtfGlobal.getLocaleText('pm.project.plan.switchmode'),
        //                text: WtfGlobal.getLocaleText('pm.Help.projplanmode')
        //            },
        //            enableToggle: true,
        //            pressed: false,
        //            handler: function(btn) {
        //                this.togglePlanViewMode(btn, btn.pressed);
        //            },
        //            scope: this
        //        }));
        //    }


        if(this.connstatus != 6) {
            var btnArrObj = [
            [WtfGlobal.getLocaleText('pm.project.plan.reports.overview.projectcharts'), WtfGlobal.getLocaleText('pm.Help.projectcharts')],
            [WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.projectsummary'), WtfGlobal.getLocaleText('pm.Help.projSummary')],
            [WtfGlobal.getLocaleText('pm.project.report.resourceusage'), WtfGlobal.getLocaleText('pm.Help.resourceusage')],
            [WtfGlobal.getLocaleText('pm.project.plan.reports.workload.taskusage'), WtfGlobal.getLocaleText('pm.Help.taskusage')],
            [WtfGlobal.getLocaleText('pm.project.report.unstartedtask'), WtfGlobal.getLocaleText('pm.Help.unstartedtask')],
            [WtfGlobal.getLocaleText('pm.project.report.tsakinprogress'), WtfGlobal.getLocaleText('pm.Help.inprogress')],
            [WtfGlobal.getLocaleText('pm.project.plan.reports.overview.milestones'), WtfGlobal.getLocaleText('pm.Help.milestones')],
            [WtfGlobal.getLocaleText('pm.project.home.health.overdue'), WtfGlobal.getLocaleText('pm.Help.overduetask')],
            [WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.date'), WtfGlobal.getLocaleText('pm.Help.dateCompare')],
            [WtfGlobal.getLocaleText('pm.project.plan.reports.workload.resourceanalysis'), WtfGlobal.getLocaleText('pm.Help.resanalysis')]
            ], menuObj = [];
            for(var i=0; i<btnArrObj.length; i++){
                menuObj.push(
                    new Wtf.Action({
                        text: btnArrObj[i][0],
                        iconCls: 'dpwnd reporticon',
                        tooltip: {
                            text: btnArrObj[i][1]
                        },
                        handler: this.reportMenuHandler,
                        scope: this
                    })
                    );
            }
            topBar.push(new Wtf.Toolbar.Button({
                iconCls:'dpwnd reportmenuicon',
                text:WtfGlobal.getLocaleText('pm.project.plan.reports.quick.text'),
                tooltip: {
                    title:WtfGlobal.getLocaleText('pm.project.plan.reports.quick.text'),
                    text: WtfGlobal.getLocaleText('pm.Help.projplanQuickreport')
                },
                scope:this,
                id:this.id +'reportsMenu',
                menu: menuObj
            }));
        }
        topBar.push(new Wtf.Toolbar.Button({
            iconCls: 'dpwnd reportmenuicon',
            text:WtfGlobal.getLocaleText('pm.project.plan.reports.advanced'),
            tooltip: {
                title:WtfGlobal.getLocaleText('pm.project.plan.reports.advanced'),
                text: WtfGlobal.getLocaleText('pm.Help.projplanreport')
            },
            scope: this,
            id: 'projPlanReport',
            handler: function(){
                if(!Wtf.subscription.proj.subModule.bsln) {
                    this.newReportWindow(3,
                    [/*assignments","cost",*/"Overview", "Workload", "Activities"/*,"Custom"*/],
                    [/*"Assignments","cost",*/WtfGlobal.getLocaleText('pm.project.plan.reports.overview.text'),WtfGlobal.getLocaleText('pm.project.plan.reports.workload.text'),WtfGlobal.getLocaleText('pm.project.plan.reports.activities.text')/*,"Custom"*/],false);
                } else{
                    this.newReportWindow(4,
                    [/*assignments","cost",*/"Overview", "Workload", "Activities", "Baseline"/*,"Custom"*/],
                    [/*"Assignments","cost",*/WtfGlobal.getLocaleText('pm.project.plan.reports.overview.text'),WtfGlobal.getLocaleText('pm.project.plan.reports.workload.text'),WtfGlobal.getLocaleText('pm.project.plan.reports.activities.text'),WtfGlobal.getLocaleText('pm.common.baseline.text')/*,"Custom"*/],false);
                }
            }
        }));
        topBar.push('-');
    }
    bottomBar.push('->');
    bottomBar.push(this.refreshButton = new Wtf.Toolbar.Button({
        iconCls: 'pwnd refresh',
        align:'right',
        text: WtfGlobal.getLocaleText('pm.common.refresh'),
        tooltip: {
            title:WtfGlobal.getLocaleText('pm.project.plan.menu.refresh'),
            text: WtfGlobal.getLocaleText('pm.Help.projplanrefresh')
        },
        scope: this,
        id: this.id+'refresh',
        handler: function(){
            this.refreshContainer();
        }
    }));

    Wtf.proj.containerPanel.superclass.constructor.call(this, {
        layout: "border",
        title: this.title,
        closable: this.closable,
        tabType: this.tabType,
        iconCls: this.iconCls,
        id: this.id + 'projPlanCont',
        border: false,
        autoDestroy: true,
        items: [{
            region: 'center',
            width: WtfGlobal.getCheckListModuleStatus() ? '58%' : '50%',
            id: this.pid + 'gridRegion',
            border: false,
            layout: 'fit'
        },{
            split: true,
            region: 'east',
            id: this.pid + 'chartRegion',
            width: WtfGlobal.getCheckListModuleStatus() ? '42%' : '50%',
            border: false,
            layout: 'border',
            cls:'charteastPane',
            hideMode: 'offsets',
            items:[{
                region: 'north',
                height: 33,
                id: this.pid + 'headerCont'
            },{
                region: 'center',
                layout: 'fit',
                border: false,
                id: this.pid+ 'chartCont'
            }]
        }],
        tbar: topBar,
        bbar:bottomBar
    });
    this.searchTask.on('render', function() {
        this.taskDelay = new Wtf.util.DelayedTask();
        Wtf.EventManager.addListener(this.pid+"searchTask", 'keyup', this.searchTextOnKeyPress, this);
        this.searchTask.on('specialkey', this.searchTextOnEnter, this);
    }, this);
    Wtf.QuickHelp.register(this.getTopToolbar(),this);
    this.showPriority.on('toggle', function() {
        if(this.showOverdue.pressed && this.showPriority.pressed)
            this.cControl.showOverdueTask();
    }, this);
    this.showOverdue.on('toggle', function() {
        if(this.showPriority.pressed && this.showOverdue.pressed)
            this.editGrid.showPriority(false);
    }, this);
}

Wtf.extend(Wtf.proj.containerPanel,Wtf.Panel,{
    initComponent: function(){
        Wtf.proj.containerPanel.superclass.initComponent.call(this);
        this.scrollOffset = 9290;
        this.todaylineobj = null;
        this.projstartdate = new Date();
        this.reportPageSize = 12;
        this.isViewMode = false;
        this.refreshOnActivate = false;
        this.cControl = new ganttChart({
            id: this.pid + 'ganttChart',
            containerPanel: this,
            connstatus: this.connstatus,
            archived: this.archived
        });
        this.editGrid = new Wtf.EditorGridComp({
            id: this.pid,
            containerPanel: this,
            connstatus: this.connstatus,
            archived: this.archived
        });
        this.headerPanel = new Wtf.Panel({
            id: this.pid + 'headerPanel',
            cls: 'headerPanel',
            border: false,
            autoDestroy: true,
            width: 10080
        });
        this.gridFields = [
        ['Task ID'],
        ['Task Name'],
        ['Duration'],
        ['Start Date'],
        ['End Date'],
        ['Predecessor'],
        ['Resources'],
        ['Completion'],
        ['Notes']
        ];
        this.periodData = [
        ['Daily'],
        ['Weekly'],
        ['Monthly'],
        ['Yearly']
        ];
        this.addEvents({
            'openreportfromwidget': true
        });
        this.editGrid.on('onOutdent', this.DoOutdent, this);
        this.editGrid.on('setHeader', this.SetHeaderDate, this);
        this.editGrid.on('insertPanelOnDataLoad',this.cControl.insertPanelOnDataLoad,this.cControl);
        this.cControl.on('insertProxyPanel', this.editGrid.insertProxyPanel, this.editGrid);
        this.editGrid.on("bodyscroll", function(scrollLeft, scrollTop) {
            if(scrollTop != 0)
                Wtf.getCmp("wraperpanel" + this.pid).body.dom.scrollTop = scrollTop;
        },this);
        this.cControl.on('showWindow', this.DisplayWindow, this);
        this.cControl.on('showWindowForUserChoice', this.ShowWindowForUserChoice, this);
        this.cControl.on('chartAfterRender',this.chartRender, this);
        this.on("beforedestroy", this.saveState, this);
        this.on("openreportfromwidget", this.openReport, this);
        mainPanel.getActiveTab().on('activate',function(){
            this.fireEvent('openreportfromwidget');
        }, this);
    },

    reportMenuHandler: function(object) {
        object.parentMenu.hide();
        reportActionValue = object.text;
        this.openReport();
    },

    openReport: function(){
        if(reportActionValue != ""){
            switch(reportActionValue){
                case WtfGlobal.getLocaleText('pm.project.plan.reports.overview.cashflow'):
                    this.reportTypeSelect("Daily","resource","cost","all");
                    break;
                case WtfGlobal.getLocaleText('pm.project.plan.reports.overview.topleveltasks'):
                    this.toplevelTaskReport();
                    break;
                case WtfGlobal.getLocaleText('pm.project.plan.reports.overview.milestones'):
                    this.milestonesReport();
                    break;
                case WtfGlobal.getLocaleText('pm.project.home.health.overdue'):
                    this.overdueTaskReport();
                    break;
                case WtfGlobal.getLocaleText('pm.project.plan.reports.overview.projectcharts'):
                    this.showProjectCharts();
                    break;
                case WtfGlobal.getLocaleText('pm.project.home.health.completed'):
                    this.completeTaskReport();
                    break;
                case WtfGlobal.getLocaleText('pm.project.plan.reports.activities.ending'):
                    this.acceptDate('enddatetasks');
                    break;
                case WtfGlobal.getLocaleText('pm.project.report.unstartedtask'):
                    this.unstartedTaskReport();
                    break;
                case WtfGlobal.getLocaleText('pm.project.plan.reports.activities.starting'):
                    this.acceptDate('startdatetasks');
                    break;
                case WtfGlobal.getLocaleText('pm.project.report.tsakinprogress'):
                    this.inprogressTaskReport();
                    break;
                case WtfGlobal.getLocaleText('pm.project.report.resourceusage'):
                    this.reportTypeSelect("Daily","resource","time","all");
                    break;
                case WtfGlobal.getLocaleText('pm.project.plan.reports.workload.taskusage'):
                    this.reportTypeSelect("Daily","task","cost","all");
                    break;
                case WtfGlobal.getLocaleText('pm.project.plan.reports.workload.resourceanalysis'):
                    this.acceptDateForResAnalysisRpt('resanalysis');
                    break;
                case WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.projectsummary'):
                    this.viewBaselineForReport();
                    this.baselineflag = 1;
                    break;
                case WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.duration'):
                    this.viewBaselineForReport();
                    this.baselineflag = 3;
                    break;
                case WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.cost'):
                    this.viewBaselineForReport();
                    this.baselineflag = 4;
                    break;
                case WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.date'):
                    this.viewBaselineForReport();
                    this.baselineflag = 2;
                    break;
                case WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.resource'):
                    this.viewBaselineForReport();
                    this.baselineflag = 5;
                    break;
                case WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.resourcewise'):
                    this.viewBaselineForReport();
                    this.baselineflag = 6;
                    break;
            }
            reportActionValue = "";
        }
    },

    saveState: function(obj){
        //        if(this.isViewMode)
        //            this.renderEditMode();
        var stateVar = this.getStateVariables();
        var planview ='';
        planview = (this.projScale == 'day') ? 'day' : 'week';
        Wtf.Ajax.requestEx({
            url: "../../jspfiles/project/projectState.jsp",
            params: {
                mode: 1,
                userid: loginid,
                featureid: 2,
                projectid: this.pid,
                statevar: stateVar,
                planview: planview
            }
        }, this);
    },
    getStateVariables: function(){
        var _o = "";
        if(this.showTaskProgress.pressed){
            _o += "progress";
        } else if(this.showOverdue.pressed){
            _o += "overdue";
        } else if(this.showPriority.pressed){
            _o += "priority";
        } else if(this.showResources.pressed){
            _o += "resources";
        }
        var stateVar = "{feature: \"" + _o + "\"}";
        return stateVar;
    },
    showState: function(){
        showMainLoadMask(WtfGlobal.getLocaleText('pm.loadingpreferences.text')+"...");
        Wtf.Ajax.requestEx({
            url: "../../jspfiles/project/projectState.jsp",
            params: {
                mode: 2,
                userid: loginid,
                featureid: 2,
                projectid: this.pid
            }
        }, this, function(request, response){
            var obj = eval("(" + request + ")");
            this.loadPreferences(obj);
            this.fireEvent('openreportfromwidget');
            hideMainLoadMask();
        }, function(request, response){
            hideMainLoadMask();
        });
    },
    loadPreferences: function(obj) {
        this.checkToggledBtn("task" + obj.feature);
        switch(obj.feature){
            case "progress":
                this.showTaskProgress.toggle(true);
                this.editGrid.showProgress();
                break;
            case "overdue":
                this.showOverdue.toggle(true);
                this.cControl.showOverdueTask();
                break;
            case "priority":
                this.showPriority.toggle(true);
                this.editGrid.showPriority();
                break;
            case "resources":
                this.showResources.toggle(true);
                this.editGrid.showResources();
                break;
        }
    },
    checkToggledBtn :function(ButType) {
        if (ButType) {
            switch (ButType) {
                case "taskoverdue":
                    if(this.editGrid.showTaskProgress)
                        this.editGrid.showProgress();
                    else if(this.editGrid.showResourceFlag)
                        this.editGrid.showResources();
                    else if(this.editGrid.showPriorityFlag)
                        this.editGrid.showPriority();
                    break;
                case "taskpriority":
                    if(this.editGrid.showTaskProgress)
                        this.editGrid.showProgress();
                    else if(this.editGrid.showResourceFlag)
                        this.editGrid.showResources();
                    else if(this.cControl.showOverDue)
                        this.cControl.showOverdueTask();
                    break;
                case "taskprogress":
                    if(this.editGrid.showResourceFlag)
                        this.editGrid.showResources();
                    else if(this.editGrid.showPriorityFlag)
                        this.editGrid.showPriority();
                    else if(this.cControl.showOverDue)
                        this.cControl.showOverdueTask();
                    break;
                case "taskresources":
                    if(this.editGrid.showTaskProgress)
                        this.editGrid.showProgress();
                    else if(this.editGrid.showPriorityFlag)
                        this.editGrid.showPriority();
                    else if(this.cControl.showOverDue)
                        this.cControl.showOverdueTask();
                    break;
            }
        }
        var featureOn = false;
        var btn = Wtf.getCmp('projPlanViewCPA'+this.pid);
        if(this.showTaskProgress.pressed || this.showOverdue.pressed || this.showPriority.pressed || this.showResources.pressed)
            featureOn = true;
        if(featureOn && btn){
            if(btn)
                btn.setDisabled(true);
        } else {
            if(btn)
                btn.setDisabled(false);
        }
    },

    toggleFeatureBtns: function(){
        var plan = this.editGrid;
        if(this.showTaskProgress.pressed){
            plan.showTaskProgress = true;
            this.showTaskProgress.toggle(false);
            plan.showProgress();

        } else if(this.showOverdue.pressed){
            this.cControl.showOverDue = true;
            this.showOverdue.toggle(false);

        } else if(this.showPriority.pressed){
            plan.showPriorityFlag = true;
            this.showPriority.toggle(false);

        } else if(this.showResources.pressed){
            plan.showResourceFlag = true;
            this.showResources.toggle(false);
            plan.showResources();
        }
    },

    showBaselineWindow : function() {
        var nameField = new Wtf.form.TextField({
            fieldLabel : WtfGlobal.getLocaleText('lang.name.text')+"*",
            id : "baselineName",
            allowBlank : false,
            maxLength : 50,
            width : 255
        });

        var descField = new Wtf.form.TextArea({
            id : "baselineDesc",
            height : 187,
            hideLabel : true,
            cls : "descArea",
            fieldClass : "descLabel",
            maxLength : 200,
            width : 356
        });

        var baselineWindow = new Wtf.Window({
            title : WtfGlobal.getLocaleText('pm.project.baseline.text'),
            width : 390,
            layout : "border",
            iconCls : "iconwin",
            modal : true,
            height : 330,
            frame : true,
            border :false,
            items : [{
                region : "north",
                height : 41,
                width : "95%",
                id : "northReg",
                border : false,
                items : [{
                    layout : "form",
                    border : false,
                    labelWidth : 100,
                    frame : true,
                    items : [nameField]
                }]
            }, {
                region : "center",
                width : "95%",
                height : "100%",
                id : "centerReg",
                layout : "fit",
                border : false,
                items : [{
                    xtype : "fieldset",
                    title : WtfGlobal.getLocaleText('lang.description.text'),
                    cls : "textAreaDiv",
                    labelWidth : 0,
                    frame : false,
                    border : false,
                    items : [descField]
                }]
            }],
            buttons:[{
                text : WtfGlobal.getLocaleText('lang.create.text'),
                handler : function() {
                    if(!nameField.isValid()) {
                        msgBoxShow(226, 1);
                        return;
                    }
                    if(!descField.isValid()) {
                        msgBoxShow(319, 1);
                        return;
                    }
                    this.saveBaseline(nameField, descField);
                    baselineWindow.close();
                },
                scope: this
            }, {
                text:WtfGlobal.getLocaleText('lang.cancel.text'),
                handler:function() {
                    baselineWindow.close();
                }
            }]
        });

        baselineWindow.show();

        Wtf.get("baselineDesc").dom.parentNode.style.paddingLeft = "0";

        Wtf.getCmp("baselineName").on("change", function() {
            Wtf.getCmp("baselineName").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("baselineName").getValue()));
        });

        Wtf.getCmp("baselineDesc").on("change", function() {
            Wtf.getCmp("baselineDesc").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("baselineDesc").getValue()));
        });
        focusOn(nameField.id);
    },

    saveBaseline: function(nameField, descField) {

        var baselineName = WtfGlobal.HTMLStripper(nameField.getValue());
        var baselineDesc = WtfGlobal.HTMLStripper(descField.getValue());

        if(baselineName == null || baselineName == "") {
            msgBoxShow(48, 1);
            return;
        }

        Wtf.Ajax.requestEx({
            url : "../../jspfiles/project/baseline.jsp",
            params : {
                action : 4,
                projectid : this.pid,
                userid : loginid,
                baselineid : 0,
                baselinename : baselineName,
                baselinedescription : baselineDesc
            },
            method : 'POST'
        },
        this,
        function(result, request) {
            var obj = eval("(" + result + ")");
            if(obj.success) {
                msgBoxShow(44, 0);

            } else if(obj.data == "delete") {
                Wtf.Msg.show({
                    buttons : Wtf.Msg.YESNO,
                    title : WtfGlobal.getLocaleText('pm.project.baseline.overwrite'),
                    icon : Wtf.MessageBox.QUESTION,
                    scope : this,
                    msg : WtfGlobal.getLocaleText('pm.msg.maxbaseline.text'),
                    fn : function(obj) {
                        if(obj == "yes") {
                            Wtf.Ajax.requestEx({
                                url : "../../jspfiles/project/baseline.jsp",
                                params : {
                                    action : 4,
                                    projectid : this.pid,
                                    userid : loginid,
                                    baselineid : -1,
                                    baselinename : baselineName,
                                    baselinedescription : baselineDesc
                                },
                                method : 'POST'
                            },
                            this,
                            function(request, response) {
                                var obj = eval("(" + request + ")");
                                if(obj.success) {
                                    if(obj.data[0].baselineid) {
                                        var baselinePanel = Wtf.getCmp("baseline_" + obj.data[0].baselineid);
                                        if(baselinePanel) {
                                            var projTabs = Wtf.getCmp("projectTabs_" + this.pid).tabpanel;
                                            projTabs.remove(baselinePanel);
                                        }
                                    }
                                    msgBoxShow(44, 0);
                                }
                            },
                            function() {
                                msgBoxShow(4, 1);
                            });
                        }
                    }
                });

            } else if(obj.data == "noTask") {
                msgBoxShow(224, 1);

            } else {
                msgBoxShow(45, 1);
            }
        },
        function() {
            msgBoxShow(4, 1);
        });
    },

    viewBaseline : function(object) {
        var text = (object.mode == 1) ? WtfGlobal.getLocaleText('lang.view.text') : WtfGlobal.getLocaleText('lang.comapre.text');
        var baselineRec = new Wtf.data.Record.create([
        {
            name : "baselineid"
        },

        {
            name : "userid"
        },

        {
            name : "createdby"
        },

        {
            name : "createdon"
        },

        {
            name : "baselinename"
        },

        {
            name : "description"
        }
        ]);

        var baselineReader = new Wtf.data.KwlJsonReader({
            root : "data"
        }, baselineRec);

        var baselineStore = new Wtf.data.Store({
            url : Wtf.req.prj + "baseline.jsp",
            baseParams : {
                action : 1,
                projectid : this.pid
            },
            reader : baselineReader
        });

        var baselineCM = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                dataIndex : "baselineid",
                hidden : true
            }, {
                dataIndex : "userid",
                hidden : true
            }, {
                dataIndex : "baselinename",
                header : WtfGlobal.getLocaleText('lang.name.text'),
                width : 130
            }, {
                dataIndex : "description",
                header : WtfGlobal.getLocaleText('lang.description.text'),
                width : 200
            }, {
                dataIndex : "createdon",
                header : WtfGlobal.getLocaleText('pm.common.createdon'),
                width : 120,
                renderer : function(value) {
                    value = value.split(".0")[0];
                    var valDate = new Date();
                    valDate = Date.parseDate(value, "Y-m-d H:i:s");
                    value = value ? valDate.dateFormat(WtfGlobal.getDateFormat()) : '';
                    return value;
                }
            }, {
                dataIndex : "createdby",
                header : WtfGlobal.getLocaleText('pm.project.createdby'),
                width : 120
            }
            ]);

        baselineStore.load();
        baselineStore.on('load',function(){
            if(baselineStore.getCount() == 0){
                Wtf.getCmp('showoredit').disable();
                Wtf.getCmp('delbase').disable();
            }
        });

        var baselineGrid = new Wtf.grid.GridPanel({
            cm : baselineCM,
            ds : baselineStore,
            mode: object.mode,
            id : this.id + "baselineGrid",
            viewConfig : {
                forceFit : true,
                emptyText : WtfGlobal.getLocaleText('pm.common.nodata')
            }
        });

        //        baselineGrid.on("rowdblclick", this.showBaseline, this);
        var baselineViewWindow = new Wtf.Window({
            border : false,
            frame : true,
            width : 650,
            height : 250,
            modal : true,
            iconCls : "iconwin",
            layout : "border",
            title : text +" Baselines",
            id : this.id + "baselineViewWindow",
            items : [{
                region : "center",
                border : false,
                layout : "fit",
                items : [baselineGrid]
            }],
            buttons : [{
                text : text,
                mode: object.mode,
                handler : this.showBaseline,
                scope : this,
                id: 'showoredit'
            }, {
                text : WtfGlobal.getLocaleText('pm.project.baseline.delete'),
                id: 'delbase',
                hidden: (object.mode == 1) ? false : true,
                handler : function() {
                    var baselineGrid = Wtf.getCmp(this.id + "baselineGrid");
                    var baselineSelections = baselineGrid.getSelectionModel().getSelections();
                    if(baselineSelections.length > 1) {
                        msgBoxShow(280, 1);
                        return;

                    } else if(baselineSelections.length < 1) {
                        msgBoxShow(253, 1);
                        return;
                    }

                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.339'), function(btn) {
                        if(btn == "no") {
                            return;

                        } else if(btn == "yes") {
                            Wtf.Ajax.requestEx({
                                url : "../../jspfiles/project/baseline.jsp",
                                params : {
                                    action : 3,
                                    projectid : this.pid,
                                    userid : loginid,
                                    baselineid : baselineSelections[0].data.baselineid
                                },
                                METHOD : "POST"
                            },
                            this,
                            function(result, response) {
                                var res = eval("(" + result + ")");
                                if(res.success) {
                                    msgBoxShow(276, 0);
                                    baselineGrid.getStore().reload();
                                    baselineGrid.getView().refresh();

                                    var baselinePanel = Wtf.getCmp("baseline_" + baselineSelections[0].data.baselineid);
                                    if(baselinePanel) {
                                        var projTabs = Wtf.getCmp("projectTabs_" + this.pid).tabpanel;
                                        projTabs.remove(baselinePanel);
                                    }

                                } else {
                                    msgBoxShow(291, 1);
                                }
                            },
                            function() {
                                msgBoxShow(4, 1);
                            });
                        }
                    }, this);
                },
                scope : this
            }, {
                text : WtfGlobal.getLocaleText('lang.cancel.text'),
                handler : function() {
                    baselineViewWindow.close();
                },
                scope : this
            }]
        });

        baselineViewWindow.show();
    },

    showBaseline: function(obj) {
        var baselineViewWindow = Wtf.getCmp(this.id + "baselineViewWindow");
        var baselineGrid = Wtf.getCmp(this.id + "baselineGrid");
        var baselineSelections = baselineGrid.getSelectionModel().getSelections();
        if(baselineSelections.length > 1) {
            msgBoxShow(229, 1);
            return;
        } else if(baselineSelections.length < 1) {
            msgBoxShow([WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText({key:'pm.msg.253',params:obj.initialConfig.text})], 1);
            return;
        }
        if(obj.mode == 1){
            this.displayBaselineData(baselineViewWindow, baselineSelections[0].data);
        } else {
            if(this.editGrid.getStore().getCount() > 1)
                this.compareBaseline(baselineSelections[0].data);
            else
                msgBoxShow(197, 0);
        }
        baselineViewWindow.close();
    },

    compareBaseline: function(blRec){
        var comparePanel = Wtf.getCmp(blRec.baselineid + "_comparePanel");
        var pPanel = Wtf.getCmp('subtabpanelcomprojectTabs_' + this.pid);
        if(comparePanel === undefined){
            comparePanel = new Wtf.proj.projCompare({
                id: blRec.baselineid + "_comparePanel",
                layout: "fit",
                projectid: this.pid,
                baselineData: blRec,
                projectName: this.pname,
                closable: true,
                title: WtfGlobal.getLocaleText('pm.project.baseline.compare'),
                iconCls: 'dpwnd comparebaselineiconTab'
            });
            pPanel.add(comparePanel);
        }
        pPanel.setActiveTab(comparePanel);
    //        comparePanel.on("activate",function(obj){
    //
    ////             var baselineGrid = Wtf.getCmp(this.id + "baselineGrid");
    ////              baselineGrid.getStore().reload();
    //              baselineGrid.getView().refresh();
    //
    //        })
    },

    deleteBaseline: function(object) {
        var baselineGrid = Wtf.getCmp(this.id + "baselineGrid");
        var baselineSelections = baselineGrid.getSelectionModel().getSelections();
        if(baselineSelections.length > 1) {
            msgBoxShow(280, 1);
            return;
        } else if(baselineSelections.length < 1) {
            msgBoxShow(253, 1);
            return;
        }
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.339'), function(btn) {
            if(btn == "no") {
                return;
            } else if(btn == "yes") {
                Wtf.Ajax.requestEx({
                    url : "jspfiles/project/baseline.jsp",
                    params : {
                        action : 3,
                        projectid : this.pid,
                        userid : loginid,
                        baselineid : baselineSelections[0].data.baselineid
                    },
                    METHOD : "POST"
                },
                this,
                function(result, response) {
                    var res = eval("(" + result + ")");
                    if(res.success) {
                        msgBoxShow(276, 0);
                        baselineGrid.getStore().reload();
                        baselineGrid.getView().refresh();

                        var baselinePanel = Wtf.getCmp("baseline_" + baselineSelections[0].data.baselineid);
                        if(baselinePanel) {
                            var projTabs = Wtf.getCmp("projectTabs_" + this.pid).tabpanel;
                            projTabs.remove(baselinePanel);
                        }

                    } else {
                        msgBoxShow(291, 1);
                    }
                },
                function() {
                    msgBoxShow(4, 1);
                });
            }
        }, this);
    },
    displayBaselineData : function(obj, baselineData) {
        var baseLinePanel = Wtf.getCmp("baseline_" + baselineData.baselineid);
        var projTabs = Wtf.getCmp("projectTabs_" + this.pid).tabpanel;

        if(baseLinePanel === undefined) {

            var baselineRec = Wtf.proj.common.taskRecord;
            var baselineReader = new Wtf.data.KwlJsonReader({
                root : "data"
            }, baselineRec);
            var bStore = new Wtf.data.Store({
                url: Wtf.req.prj + "baseline.jsp",
                baseParams : {
                    action : 2,
                    projectid : this.pid,
                    baselineid : baselineData.baselineid
                },
                reader : baselineReader
            });
            baseLinePanel = new Wtf.proj.baselinePanel({
                title: "[ "+ WtfGlobal.getLocaleText('pm.common.baseline.text') +" ] " + baselineData.baselinename,
                id: "baseline_" + baselineData.baselineid,
                baselineData: baselineData,
                baselineStore: bStore,
                projectid: this.pid,
                iconCls: "dpwnd baselineiconTab",
                closable: true
            });

            projTabs.add(baseLinePanel);
        }
        projTabs.setActiveTab(baseLinePanel);
        baseLinePanel.doLayout();
    },

    chartRender: function() {
        showMainLoadMask(WtfGlobal.getLocaleText('pm.renderingcomponents.text')+"...");
        this.cControl.initGraphics();
        this.cControl.assignListener();
        this.startDate = new Date();
        var dtOffset = this.startDate.format('w');
        this.startDate = this.startDate.add(Date.DAY, -dtOffset);
        this.endDate = this.startDate.add(Date.DAY, 50*7);
        this.chartRendered = true;
        this.editGrid.linkArrayObj = this.cControl.linkArrayObj;
        this.sCombo.setValue((this.projScale == 'day') ? WtfGlobal.getLocaleText('pm.project.plan.dayview'): WtfGlobal.getLocaleText('pm.project.plan.weekview'));
        this.editGrid.getProjectMinMaxDate();
    },

    setHeader: function(noweeks){
        var dateoffset = this.startDate.format('w');
        this.startDate = this.startDate.add(Date.DAY, -dateoffset);
        this.cControl.StartDate = this.startDate;
        this.startDate = this.startDate.clearTime(false);
        this.endDate = this.endDate.clearTime(false);
        this.dt = this.startDate;
        var headerPanel = this.headerPanel;
        var index = 0;
        var d=0;
        if(this.index)
            noweeks = (this.index > noweeks)? noweeks : this.index;
        if(this.projScale == 'day'){
            for (index = 0; d < (noweeks + 1); d++, index++) {
                var header = new Wtf.Panel({
                    id: 'header' + index,
                    title: this.dt.format('d') + " " + this.dt.format('M') + " '" + this.dt.format('y'),
                    cls: 'headerProject',
                    baseCls: 'test'
                });
                headerPanel.add(header);
                this.dt = this.dt.add(Date.DAY, 7);
            }
            this.cControl.doLayout();
            Wtf.get("chartPanel1_" + this.cControl.id).removeClass("weeklyView");
            Wtf.get("chartPanel1_" + this.cControl.id).addClass("dailyView");
        } else {
            for (index = 0; d < ((noweeks * 3)+parseInt(noweeks / 4)); d++, ++index) {
                var header = new Wtf.Panel({
                    id: 'header' + index,
                    title: this.dt.format('d') + "-" +this.dt.add(Date.DAY, 6).format('d') + " " + this.dt.format('M') + "'" + this.dt.format('y'),
                    cls: 'headerProjectWeek',
                    baseCls: 'test'
                });
                headerPanel.add(header);
                this.dt = this.dt.add(Date.DAY, 7);
            }
            this.cControl.doLayout();
            Wtf.get("chartPanel1_" + this.cControl.id).removeClass("dailyView");
            Wtf.get("chartPanel1_" + this.cControl.id).addClass("weeklyView");
        }
        headerPanel.doLayout();
        this.index = index;
        this.drawToday();
    },

    drawToday: function(){
        var dateoffset = Math.abs(this.startDate.getTime() - (new Date()).getTime());
        dateoffset = Math.floor(dateoffset / 1000 / 60 / 60 / 24);
        if(this.todaylineobj)
            this.todaylineobj.clear();
        this.todaylineobj = new jsGraphics(this.cControl.id);
        this.todaylineobj.clear();
        this.todaylineobj.setColor('gray');
        if(this.projScale == 'day')
            this.todaylineobj.drawLine(dateoffset*16,0,dateoffset*16,Wtf.get(this.cControl.id).getHeight());
        else
            this.todaylineobj.drawLine(dateoffset*5,0,dateoffset*5,Wtf.get(this.cControl.id).getHeight());
        this.todaylineobj.paint();
    },

    onRender: function(config) {
        Wtf.proj.containerPanel.superclass.onRender.call(this,config);
        var projId = this.pid;
        this.wraper = new Wtf.ScrollPanel({
            id: 'wraperpanel' + projId,
            border: false,
            autoScroll: true,
            cls: 'scrollPanel',
            items: [this.cControl]
        });
        var headerContainer = Wtf.getCmp(projId + 'headerCont');
        this.headerContainer = headerContainer;
        //        this.projViewFlag = 'day';
        Wtf.getCmp(projId + 'gridRegion').add(this.editGrid);
        Wtf.getCmp(projId + 'chartCont').add(this.wraper);
        headerContainer.add(this.headerPanel);
        Wtf.getCmp(projId + 'chartRegion').doLayout();
        this.wraper.doLayout();
        this.wraper.on("bodyscroll", function(scrollLeft, scrollTop) {
            if(scrollLeft >= this.scrollOffset){
                this.scrollOffset += 112;
                if(this.projScale == 'day'){
                    var header = new Wtf.Panel({
                        id: 'header' + (++this.index),
                        title: this.dt.format('d') + " " + this.dt.format('M') + " '" + this.dt.format('y'),
                        cls: 'headerProject',
                        baseCls: 'test'
                    });
                    this.dt = this.dt.add(Date.DAY, 7);
                } else {
                    var header = new Wtf.Panel({
                        id: 'header' + (++this.index),
                        title: this.dt.format('d') + "-" +this.dt.add(Date.DAY, 6).format('d') + " " + this.dt.format('M') + "'" + this.dt.format('y'),
                        cls: 'headerProjectWeek',
                        baseCls: 'test'
                    });
                    this.dt = this.dt.add(Date.DAY, 6);
                }
                var headerWidth = this.headerPanel.getSize().width + 112;
                this.headerPanel.setSize(headerWidth, this.headerPanel.height);
                this.headerPanel.add(header);
                this.headerPanel.doLayout();
                var chartWidth = this.cControl.getSize().width + 112;
                this.cControl.setSize(chartWidth, this.cControl.height);

            }
            headerContainer.body.dom.scrollLeft = scrollLeft;
            this.editGrid.getView().scroller.dom.scrollTop = scrollTop;
            if(!this.archived){
                var chartPanelXY = Wtf.get("chartPanel1_" + projId + 'ganttChart').getXY();
                this.cControl.CanvasX = chartPanelXY[0];
                this.cControl.CanvasY = chartPanelXY[1];
            }
        },this);
    },

    checkForProjStartDate: function(sdate){
        if(typeof this.editGrid.projStartDate == 'string')
            var dt = Date.parseDate(this.editGrid.projStartDate, "Y-m-j H:i:s");
        if(!dt){
            if(typeof this.projstartdate == 'object')
                dt = this.projstartdate;
        }
        if(sdate.format("Y-m-d") < dt.format("Y-m-d"))
            return true;
        else
            return false;
    },


    DoOutdent: function(record, parentRec) {
        if (parentRec)
            this.cControl.outdentGrid(parentRec);
        if (record)
            this.cControl.mysubtaskoutdent(record, this.editGrid.search(record.data['taskid']));
    },

    WindowForNonWorkingUserChoice: function(win, operationType, Xposition, TaskId) {
        var rOption = 2;
        if(win.getSelectedRadio() == 'rd1')
            rOption = 1;
        if (operationType == 'Ok')
            this.cControl.setPanelPositionForNonWorkingDate(Xposition, TaskId, rOption);
        win.close();
    },

    CloseDeleteLinkWindow: function(win, operationType, Xposition, TaskId) {
        var rOption = 2;
        if(win.getSelectedRadio() == 'rd1')
            rOption = 1;
        if (operationType == 'Ok' && rOption == 2)
            this.cControl.DeleteLinkOnPanelMove(Xposition, TaskId, rOption);
        win.close();
    },

    ShowWindowForUserChoice: function(stringArray, tempXposition, TaskId, type) {
        var iurl = '../../images/deletelink.png';
        if(type == 'NonWorkingDay')
            iurl = '../../images/nonWorking.png';
        var win = new Wtf.common.customWindow({
            title: WtfGlobal.getLocaleText('pm.calender.nonworkngday'),
            id : 'OtherWinShow',
            bodytext: stringArray[0],
            closable: true,
            iconCls: 'iconwin',
            imageURL: iurl,
            fieldLabel: stringArray[1],
            OKCANCEL: true,
            RadioButtons: [{
                id: 'rd1',
                boxLabel: stringArray[2],
                checked: true,
                name: 'grp1'
            },{
                id: 'rd2',
                boxLabel: stringArray[3],
                name: 'grp1'
            }],
            cls: 'windowComp',
            resizable: false,
            border: false,
            modal: true,
            width: 335,
            autoHeight: true,
            buttons:[{
                text: WtfGlobal.getLocaleText('lang.OK.text'),
                width: 40,
                scope: this,
                handler: function() {
                    if(type == 'NonWorkingDay')
                        this.WindowForNonWorkingUserChoice(win, 'Ok', tempXposition, TaskId);
                    else
                        this.CloseDeleteLinkWindow(win, 'Ok', tempXposition, TaskId);
                }
            },{
                text: WtfGlobal.getLocaleText('lang.cancel.text'),
                width: 40,
                scope: this,
                handler: function() {
                    if(type== 'NonWorkingDay')
                        this.WindowForNonWorkingUserChoice(win, 'Cancel', tempXposition, TaskId);
                    else
                        this.CloseDeleteLinkWindow(win, 'Cancel', tempXposition, TaskId);
                }
            }]
        });
        win.show();
    },

    WinButtonClick: function(win, LinkId) {
        this.cControl.DeleteLinkOnMouseClick(LinkId);
        win.close();
    },

    DisplayWindow: function(configObj, LinkId) {
        var win = new Wtf.Window(configObj);
        var OkButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText('lang.cancel.text'),
            width: 40,
            handler: function() {
                this.WinButtonClick(win, 'OK');
            },
            scope: this
        });
        var DeleteButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText('lang.delete.text'),
            width: 40,
            id: LinkId,
            handler: function() {
                this.WinButtonClick(win, LinkId);
            },
            scope: this
        });
        win.addButton(DeleteButton);
        win.addButton(OkButton);
        win.show();
    },

    CreateTemplateOfTask: function() {
        var buff = this.editGrid.getSelectionModel().getSelections();
        var buffLength = buff.length;
        for(var t = 0; t < buffLength; t++) {
            if(buff[t].data['duration'] != "")
                break;
        }
        if(t == buffLength) {
            msgBoxShow(46, 1);
            return;
        }
        var nameField = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText('lang.name.text')+'*',
            id:'templateName',
            allowBlank: false,
            maxLength : 50,
            width:255
        });
        var descField = new Wtf.form.TextArea({
            id:'DescField',
            height: 187,
            hideLabel:true,
            cls:'descArea',
            fieldClass : 'descLabel',
            maxLength : 200,
            width:356
        });
        var reader = new Wtf.data.ArrayReader({}, [ {
            name: 'Field'
        } ]);
        if(buffLength != 0) {
            this.sabase.disable();
            var Template = new Wtf.Window({
                title: WtfGlobal.getLocaleText('pm.project.plan.template.new'),
                width: 390,
                layout: 'border',
                iconCls : 'iconwin',
                modal: true,
                height: 330,
                frame: true,
                border:false,
                items:[{
                    region: 'north',
                    height: 41,
                    width: '95%',
                    id:'northReg',
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
                    id: 'centerReg',
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
                    text:WtfGlobal.getLocaleText('lang.create.text'),
                    handler: function() {
                        if(!nameField.isValid()) {
                            msgBoxShow(47, 1);
                            return;
                        }
                        this.createTemplate(Template,nameField,descField);
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
            Template.on('close',function(){
                Wtf.getCmp('projPlanCreateTpl').enable();
            });
            Wtf.get('DescField').dom.parentNode.style.paddingLeft = '0';
            Wtf.getCmp("templateName").on("change", function(){
                Wtf.getCmp("templateName").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("templateName").getValue()));
            });
            Wtf.getCmp("DescField").on("change", function(){
                Wtf.getCmp("DescField").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("DescField").getValue()));
            });
            focusOn(nameField.id);
        }
    },

    createTemplate: function(win, nameField, descField) {
        var tname = WtfGlobal.HTMLStripper(nameField.getValue());
        var description = WtfGlobal.HTMLStripper(descField.getValue());
        if(tname == null && tname == "") {
            msgBoxShow(48, 1);
            return;
        }
        var plannerGrid = this.editGrid;
        plannerGrid.loadbuffer('template');
        var gc = 0;
        var jdata = "[";
        //        var buffLength = plannerGrid.templateBuffer.length;
        //        for(var cnt = 0; cnt < buffLength; cnt++) {
        //            var tempStore = new Wtf.data.Store();
        //            tempStore = plannerGrid.templateBuffer[cnt];
        //            var tempcnt = 0;
        //            while(tempStore.getAt(tempcnt)) {
        var tempStore = plannerGrid.templateBuffer;
        for(var t = 0; t < tempStore.length; t++){
            var temrec = plannerGrid.getTaskObj(tempStore[t]);
            if(temrec.data['startdate'] != "") {
                temrec.data["startdate"] = temrec.data['startdate'].format('Y-m-d');
                temrec.data["enddate"] = temrec.data['enddate'].format('Y-m-d');
                temrec.data["actstartdate"] = temrec.data['actstartdate'].format('Y-m-d');
            //                    temrec.set('startdate', temrec.data['startdate'].format('Y-m-d'));
            //                    temrec.set('enddate', temrec.data['enddate'].format('Y-m-d'));
            //                    temrec.set('actstartdate', temrec.data['actstartdate'].format('Y-m-d'));
            } else {
                temrec.data["startdate"] = "";
                temrec.data["enddate"] = "";
                temrec.data["actstartdate"] = "";
            //                    temrec.set('startdate', '');
            //                    temrec.set('enddate', '');
            //                    temrec.set('actstartdate', '');
            }
            temrec.data["resourcename"] = "";
            temrec.data["percentcomplete"] = "";
            temrec.data["predecessor"] = tempStore[t].data["predecessor"];
            //                temrec.set('resourcename','');
            //                temrec.set('percentcomplete','0');
            jdata += Wtf.encode(temrec.data) + ",";
        //                tempcnt++;
        }
        //            }
        //        }
        jdata = jdata.substr(0, jdata.length - 1) + "]";
        Wtf.Ajax.requestEx({
            url: Wtf.req.prj + 'template.jsp',
            params: {
                action: 'insert',
                projid: this.pid,
                name: tname,
                data: jdata,
                userid: loginid,
                desc: description
            },
            method:'POST'
        },
        this,
        function() {
            msgBoxShow(49, 0);
        },
        function() {
            msgBoxShow(50, 1);
        });
        win.close();
    },

    chkExport: function(){
        var flg = true;
        if(this.editGrid.dstore.getCount() == 1){
            msgBoxShow(["Empty plan"," There is no data present to export."]);
            flg = false;
        }
        return flg;
    },
    InsertTemplate: function(action) {
        var templateRec = Wtf.data.Record.create([{
            name: 'tempid',
            mapping: 'tempid'
        },{
            name: 'tempname',
            mapping: 'tempname'
        },{
            name: 'description',
            mapping: 'description'
        }]);
        var template_ds = new Wtf.data.Store({
            url: Wtf.req.prj + 'template.jsp?projid='+this.pid+'&action=getall',
            method: 'GET',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },templateRec)
        });
        template_ds.on('load',function(){
            if(template_ds.getCount() == 0){
                detailsField.setValue("There are no records to display");
                Wtf.getCmp('ok').disable();
                Wtf.getCmp('refDate').disable();
            }
        });
        var namePanel = new Wtf.grid.GridPanel({
            id:'templateName',
            autoScroll: true,
            enableColumnResize:false,
            border:false,
            viewConfig:{
                forceFit:true,
                emptyText : WtfGlobal.getLocaleText('pm.common.nodata')
            },
            cm: new Wtf.grid.ColumnModel([
                new Wtf.grid.RowNumberer, {
                    header:WtfGlobal.getLocaleText('lang.name.text'),
                    dataIndex: 'tempname'
                }]),
            ds: template_ds,
            height:180
        });
        namePanel.on('cellclick',function(gridObj, ri, ci, e){
            detailsField.setValue(gridObj.getStore().getAt(ri).data['description']);
        },this);
        var detailsField = new Wtf.form.TextArea({
            id: 'detailsPanel',
            readOnly: true,
            disabled: true,
            height: 100,
            border:false,
            cls:'descArea',
            fieldClass : 'descLabel',
            width: 175
        })
        var h = Wtf.util.clone(this.editGrid.HolidaysList);
        for(var cnt =0; cnt<h.length;cnt++) {
            h[cnt] = Date.parseDate(h[cnt],"d/m/Y").format(WtfGlobal.getOnlyDateFormat());
        }
        h = (Wtf.isEmpty(h[0])) ? null : h;
        var refDate = new Wtf.form.DateField({
            id:'refDate',
            width:175,
            readOnly: true,
            format: WtfGlobal.getOnlyDateFormat(),
            disabledDates: h,
            disabledDays: this.editGrid.NonworkWeekDays,
            emptyText: WtfGlobal.getLocaleText('pm.common.date.referencedate'),
            minValue: Date.parseDate(this.editGrid.projStartDate, 'Y-m-j H:i:s'),
            minValueText: WtfGlobal.getLocaleText('pm.msg.referencedate.text')
        });
        template_ds.load();
        var templatePanel = new Wtf.Panel({
            id:'templatePanel',
            layout:'border',
            border:false,
            width:385,
            items:[{
                region:'center',
                width:'50%',
                border:false,
                layout:'fit',
                height:'100%',
                items:[namePanel]
            },{
                region:'east',
                width:'50%',
                border:false,
                layout: 'fit',
                height:'100%',
                items:[{
                    layout: 'border',
                    border: false,
                    items: (action == 1) ? [{
                        region: 'center',
                        layout:'fit',
                        xtype:'fieldset',
                        title:WtfGlobal.getLocaleText('lang.description.text'),
                        cls: 'templateFieldset',
                        preventScrollbars:false,
                        frame:true,
                        border:false,
                        items:[detailsField]
                    }]:[{
                        region: 'north',
                        layout:'fit',
                        xtype:'fieldset',
                        title:WtfGlobal.getLocaleText('lang.description.text'),
                        cls: 'templateFieldset',
                        height: 110,
                        preventScrollbars:false,
                        frame:true,
                        border:false,
                        items:[detailsField]
                    },{
                        region: 'center',
                        layout:'fit',
                        xtype:'fieldset',
                        cls: 'templateFieldset',
                        title:WtfGlobal.getLocaleText('pm.common.datereferencedate'),
                        preventScrollbars:false,
                        frame:true,
                        border:false,
                        items:[refDate]
                    }]
                }]
            }]
        });
        var templateWindow = new Wtf.Window({
            title:(action == 0) ? WtfGlobal.getLocaleText('pm.project.plan.template.insert'):WtfGlobal.getLocaleText('pm.common.template.delete.text'),
            modal:true,
            iconCls : 'iconwin',
            layout:'fit',
            items:[templatePanel],
            resizable:false,
            autoDestroy:true,
            height:270,
            width:385,
            buttons:[{
                text: (action == 0) ? WtfGlobal.getLocaleText('lang.OK.text'):WtfGlobal.getLocaleText('lang.delete.text'),
                disabled: (template_ds.data.length < 1) ? false : true,
                enableToggle:true,
                id:'ok',
                handler:function() {
                    var templateSelections = namePanel.getSelectionModel().getSelections();
                    if(templateSelections.length > 0) {
                        if(action == 0){
                            if(templateSelections.length == 1) {
                                var refDate = "";
                                if(Wtf.getCmp('refDate'))
                                    refDate = Wtf.getCmp('refDate').getValue();
                                if(refDate != ""){
                                    this.getTemplate(namePanel.getSelectionModel().getSelected().data['tempid'], refDate);
                                    templateWindow.close();
                                    this.editGrid.getSelectionModel().clearSelections();
                                } else {
                                    msgBoxShow(186, 1);
                                }
                            } else {
                                msgBoxShow(247, 1);
                                return;
                            }
                        } else {
                            if(templateSelections.length == 1) {
                                this.DelTemplate(namePanel.getSelectionModel().getSelected().data['tempid']);
                            } else {
                                msgBoxShow(264, 1);
                                return;
                            }
                        }
                    } else {
                        msgBoxShow(187, 1);
                    }
                },
                scope: this
            },{
                text: (action == 0) ? WtfGlobal.getLocaleText('lang.cancel.text'):WtfGlobal.getLocaleText('lang.close.text'),
                id:'cancel',
                handler:function() {
                    templateWindow.close();
                }
            }]
        });
        templateWindow.show();
    },
    selectTemplate: function(tabtitle,url,reportGrid,repType) {
        this.twin=new Wtf.selectTempWin({
            tabtitle:tabtitle,
            url:url,
            pid:this.pid,
            reportGrid:reportGrid,
            reportType:repType,
            height:490,
            width:580
        });
    },

    getTemplate: function(tempid, refDate) {
        var plannerGrid = this.editGrid;
        this.refDateforTemplate = refDate;
        plannerGrid.templateStore = new Wtf.data.Store({
            url: Wtf.req.prj + 'template.jsp',
            baseParams:{
                tempid: tempid
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },plannerGrid.Task)
        })
        plannerGrid.templateStore.on('load',function(){
            if(!plannerGrid.pasteFlag)
                plannerGrid.insertbuffer('template', this.refDateforTemplate);
        },this)
        plannerGrid.templateStore.on("loadexeption",function(){},this);
        plannerGrid.templateStore.load();
    },

    DelTemplate: function(tempid) {
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.333'), function(btn) {
            if(btn == "no") {
                return;
            } else if(btn == "yes") {
                Wtf.Ajax.requestEx({
                    method: 'POST',
                    url: Wtf.req.prj + 'template.jsp',
                    params: ({
                        action: 'DeleteTemp',
                        tempid: tempid
                    })
                    },
                this,
                function(result, req){
                    msgBoxShow(275, 0);
                    Wtf.getCmp('detailsPanel').setValue("");
                    Wtf.getCmp('templateName').store.load();
                },
                function(result, req){
                    msgBoxShow(310, 1);
                });
            }
        }, this);
    },
    //    SetHeaderDate: function(beginDate, finishDate, mode) {
    //        if(mode === undefined)
    //            mode = 1;
    SetHeaderDate: function(beginDate, finishDate) {
        var noweeks = 89;
        this.startDate = new Date(beginDate);
        this.projstartdate = this.startDate;
        this.endDate = this.startDate.add(Date.DAY, 50*7);
        this.scopeendDate = new Date(finishDate);
        var timeDiff = finishDate.getTime() - beginDate.getTime();
        var days = Math.floor(timeDiff / (1000*60*60*24*7));
        if((days * 116) > 9976){
            this.cControl.setSize(days * 116, this.cControl.height);
            this.headerPanel.setSize(days * 116 + 14, this.headerPanel.height);
            noweeks = days;
            this.noweeks = noweeks;
        }
        if(this.chartRendered)
            this.setHeader(noweeks);
    },

    projViewSelect: function(cb, rec, index){
        if(parseInt(rec.data['id'], 10) == 2 && this.projScale == 'day'){//week selected
            var i;
            this.projScale = 'week';
            if(WtfGlobal.isDefined(this.projectIndex)){
                projects[this.projectIndex].planview = this.projScale;
            }
            for(i = this.index; i>=0; i--){
                this.headerPanel.remove(this.headerPanel.findById('header'+i));
            }
            this.refreshContainer();
        } else if(parseInt(rec.data['id'], 10) == 1 && this.projScale == 'week'){//day
            this.projScale = 'day';
            if(WtfGlobal.isDefined(this.projectIndex)){
                projects[this.projectIndex].planview = this.projScale;
            }
            for(i = this.index; i>=0; i--){
                this.headerPanel.remove(this.headerPanel.findById('header'+i));
            }
            this.refreshContainer();
        }
    },

    highlightRow: function(grid, row, color){
        var rowEl = grid.getView().getRow(row);
        var cell = grid.getView().getCell(row, 0);
        rowEl.style.backgroundColor = color;
        cell.style.backgroundColor = '#FFFFFF';
    },

    showPreviousReportWindow: function(){
        if(!Wtf.subscription.proj.subModule.bsln) {
            this.newReportWindow(3,
            [/*assignments","Cost",*/"Overview","Workload","Activities"/*,"Custom"*/],
            [/*"Assignments","cost",*/WtfGlobal.getLocaleText('pm.project.plan.reports.overview.text'),WtfGlobal.getLocaleText('pm.project.plan.reports.workload.text'),WtfGlobal.getLocaleText('pm.project.plan.reports.activities.text')/*,"Custom"*/],false);
        } else{
            this.newReportWindow(4,
            [/*assignments","Cost",*/"Overview","Workload","Activities","Baseline"/*,"Custom"*/],
            [/*"Assignments","cost",*/WtfGlobal.getLocaleText('pm.project.plan.reports.overview.text'),WtfGlobal.getLocaleText('pm.project.plan.reports.workload.text'),WtfGlobal.getLocaleText('pm.project.plan.reports.activities.text'),WtfGlobal.getLocaleText('pm.common.baseline.text')/*,"Custom"*/],false);
        }
    },

    newReportWindow: function(num, idobj, labelobj,isSubReportWin){
        var noColumn = Math.floor(num/2);
        if(num%2)
            noColumn++;
        var columnSize = 1/noColumn;
        var itemsArray = [];
        for(var cnt = 0; cnt < noColumn; cnt++){
            itemsArray[itemsArray.length] = {
                columnWidth: columnSize,
                height: 200,
                layout: 'border',
                border: false,
                bodyStyle: 'background-color:#f1f1f1; padding-left:15px',
                items: [{
                    region: 'center',
                    border: false,
                    bodyStyle: "margin:auto",
                    height: 100,
                    id: "northReport" + cnt,
                    html: "<div class='reportimg' id='"+ idobj[2*cnt] +"' wtf:qtip=\""+WtfGlobal.getLocaleText("pm.Help."+[idobj[2*cnt]])+"\"></div><div class = 'imgLabel'>"+ labelobj[2*cnt] +"</div>"
                },{
                    region: 'south',
                    id: 'southReport' + cnt,
                    border: false,
                    bodyStyle: "margin:auto",
                    height: 100,
                    html: "<div class='reportimg' id='" + idobj[(2*cnt) + 1] + "' wtf:qtip=\""+WtfGlobal.getLocaleText("pm.Help."+[idobj[(2*cnt) + 1]])+"\"></div><div class = 'imgLabel'>"+ labelobj[(2*cnt) + 1] +"</div>"
                }]
            }
        }
        this.closeRptModal();
        var reportModal = new Wtf.Window({
            title: WtfGlobal.getLocaleText('pm.common.report'),
            width: (noColumn * 90 + 84),
            height: 295,
            resizable: false,
            layout: 'column',
            modal: true,
            iconCls: 'iconwin',
            bodyStyle: 'padding:20px;background-color:#f1f1f1;',
            items: itemsArray,
            buttons:[{
                width: 35,
                id:'PreviousBtn',
                text: WtfGlobal.getLocaleText('pm.common.previous'),
                hidden:true,
                scope:this,
                handler: this.showPreviousReportWindow
            },{
                width: 35,
                text: WtfGlobal.getLocaleText('lang.cancel.text'),
                handler:function(){
                    reportModal.close();
                }
            }]
        });
        reportModal.show();
        if(isSubReportWin) Wtf.getCmp("PreviousBtn").show();
        this.reportModal = reportModal;
        if(num%2){
            cnt--;
            Wtf.getCmp("southReport" + (noColumn-1)).hide();
        }
        for(var cnt = 0; cnt < noColumn; cnt++){
            Wtf.get("northReport" + cnt).addListener("click", this.subReportWindow, this);
            Wtf.get("southReport" + cnt).addListener("click", this.subReportWindow, this);
        }
    },

    showBaselineProjSummary: function(a){
        var baselineViewWindow = Wtf.getCmp(this.id + "baselineViewWindowForReport");
        var baselineGrid = Wtf.getCmp(this.id + "baselineGridForReport");
        var baselineSelections = baselineGrid.getSelectionModel().getSelections();
        var tabTitle = WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.projectsummary');
        this.reportTitleForEmbed = Wtf.proj.common.getReportTitleForEmbed('pm.project.plan.reports.baseline.projectsummary');
        if(this.manageReportTabForProjSummary('projectSummary', tabTitle, baselineSelections[0].data.baselineid)) {
            var seperator = {
                border: false,
                html: '<hr style = "width:75%;margin-left:10px">'
            };

            if(baselineSelections.length > 1) {
                msgBoxShow(229, 1);
                return;
            } else if(baselineSelections.length < 1) {
                msgBoxShow(306, 1);
                return;
            }
            showMainLoadMask(WtfGlobal.getLocaleText('pm.loading.text')+'...');
            Wtf.Ajax.requestEx({
                url: Wtf.req.prj + 'projectReport.jsp',
                method: 'POST',
                params: {
                    action:'projsummary',
                    baselineid: baselineSelections[0].data.baselineid,
                    projectid: this.pid
                }
            }, this,
            function(response, request){
                if(response != ""){
                    Wtf.getCmp('projectSummary' + this.pid).body.dom.style.overflow = 'auto';
                    Wtf.DomHelper.append(Wtf.getCmp('projectSummary' + this.pid).body.dom, response);
                    hideMainLoadMask();
                }
            },function(response, request){
                Wtf.DomHelper.append(Wtf.getCmp('projectSummary' + this.pid).body.dom, response);
                hideMainLoadMask();
            });

        }
        this.baselinemodel.close();
    },

    viewBaselineForReport : function(object) {
        var text = WtfGlobal.getLocaleText('lang.comapre.text');
        var baselineRec = new Wtf.data.Record.create([
        {
            name : "baselineid"
        },

        {
            name : "userid"
        },

        {
            name : "createdby"
        },

        {
            name : "createdon"
        },

        {
            name : "baselinename"
        },

        {
            name : "description"
        }
        ]);

        var baselineReader = new Wtf.data.KwlJsonReader({
            root : "data"
        }, baselineRec);

        var baselineStore = new Wtf.data.Store({
            url : Wtf.req.prj + "baseline.jsp",
            baseParams : {
                action : 1,
                projectid : this.pid
            },
            reader : baselineReader
        });

        var baselineCM = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                dataIndex : "baselineid",
                hidden : true
            }, {
                dataIndex : "userid",
                hidden : true
            }, {
                dataIndex : "baselinename",
                header : WtfGlobal.getLocaleText('lang.name.text'),
                width : 130
            }, {
                dataIndex : "description",
                header : WtfGlobal.getLocaleText('lang.description.text'),
                width : 200
            }, {
                dataIndex : "createdon",
                header : WtfGlobal.getLocaleText('pm.common.createdon'),
                width : 120,
                renderer : function(value) {
                    value = value.split(".0")[0];
                    var valDate = new Date();
                    valDate = Date.parseDate(value, "Y-m-d H:i:s");
                    value = value ? valDate.dateFormat(WtfGlobal.getDateFormat()) : '';
                    return value;
                }
            }, {
                dataIndex : "createdby",
                header : WtfGlobal.getLocaleText('pm.project.createdby'),
                width : 120
            }
            ]);

        baselineStore.load();
        baselineStore.on('load', function(){
            if(baselineStore.getCount() == 0)
                Wtf.getCmp('view').disable();
        }, this);

        var baselineGrid = new Wtf.grid.GridPanel({
            cm : baselineCM,
            ds : baselineStore,
            mode: 0,
            id : this.id + "baselineGridForReport",
            viewConfig : {
                forceFit : true,
                emptyText : WtfGlobal.getLocaleText('pm.common.nodata')
            }
        });

        //baselineGrid.on("rowdblclick", this.showBaseline, this);
        var baselineViewWindow = new Wtf.Window({
            border : false,
            frame : true,
            width : 650,
            height : 250,
            modal : true,
            iconCls : "iconwin",
            layout : "border",
            title : WtfGlobal.getLocaleText('pm.project.baselines'),
            id : this.id + "baselineViewWindowForReport",
            items : [{
                region : "center",
                border : false,
                layout : "fit",
                items : [baselineGrid]
            }],
            buttons : [{
                text : WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.viewreport'),
                mode: 0,
                scope : this,
                id: 'view',
                handler : function(){
                    var baselineGrid = Wtf.getCmp(this.id + "baselineGridForReport");
                    var baselineSelections = baselineGrid.getSelectionModel().getSelections();
                    if(baselineSelections.length <= 0){
                        msgBoxShow(302, 1);
                    } else if(baselineSelections.length > 1){
                        msgBoxShow(279, 1);
                    } else {
                        this.curr_baseline = baselineSelections;
                        if(this.baselineflag == 1){
                            this.showBaselineProjSummary();
                        }else if(this.baselineflag == 2){
                            this.dateCompareReport();
                        }else if(this.baselineflag == 3){
                            this.durationCompareReport();
                        }else if(this.baselineflag == 4){
                            this.costCompareReport();
                        }else if(this.baselineflag == 5){
                            this.resourceCompareReport();
                        }else if(this.baselineflag == 6){
                            this.resourcewiseCompareReport('resourcewisecompare');
                        }
                    }
                }
            }, {
                text : WtfGlobal.getLocaleText('pm.common.previous'),
                scope : this,
                handler : function(){
                    baselineViewWindow.close();
                    this.newReportWindow(6,
                    ["projSummary","dateCompare","durationCompare"/*,"shouldhavestarted"*/,"costCompare","resourceCompare","resourcewiseCompare"],
                    [
                        WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.projectsummary'),
                        WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.date'),
                        WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.duration')/*,"shouldhavestarted"*/,
                        WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.cost'),
                        WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.resource'),
                        WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.resourcewise')
                    ],true);
                }
            }, {
                text : WtfGlobal.getLocaleText('lang.cancel.text'),
                handler : function() {
                    baselineViewWindow.close();
                },
                scope : this
            }]
        });

        this.baselinemodel = baselineViewWindow;
        baselineViewWindow.show();
    },


    subReportWindow: function(e){
        switch(e.target.id){
            case "Custom":
                break;
            case "Overview":
                this.newReportWindow(5,["cashflow","milestones"/*,"projsummary"*/,"topleveltask","overduetask", "projectcharts"/*,"workingdays",""*/],[WtfGlobal.getLocaleText('pm.project.plan.reports.overview.cashflow'),WtfGlobal.getLocaleText('pm.project.plan.reports.overview.milestones')/*,WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.projectsummary')*/,WtfGlobal.getLocaleText('pm.project.plan.reports.overview.topleveltasks'),WtfGlobal.getLocaleText('pm.project.report.overduetask'), WtfGlobal.getLocaleText('pm.project.plan.reports.overview.projectcharts')/*,"Working Days",""*/],true);
                break;
            case "Workload":
                this.newReportWindow(3,["resourceusage","taskusage","resanalysis"],[WtfGlobal.getLocaleText('pm.project.report.resourceusage'),WtfGlobal.getLocaleText('pm.project.plan.reports.workload.taskusage'),WtfGlobal.getLocaleText('pm.project.plan.reports.workload.resourceanalysis')],true);
                break;
            case "Cost":
                //                this.newReportWindow(5,["budget","cashflow","earnedvalue","overbudget","overbudgetres",""],["Budget",WtfGlobal.getLocaleText('pm.project.plan.reports.overview.cashflow'),"Earned Value","Over Budget","Over Budget Resources",""],true);
                break;
            case "Activities":
                this.newReportWindow(5,["completetask","inprogress"/*,"shouldhavestarted"*/,"slippingtask","startingsoon","unstartedtask"],[WtfGlobal.getLocaleText('pm.project.home.health.completed'),WtfGlobal.getLocaleText('pm.project.plan.reports.activities.inprogress')/*,"Should Have Started"*/,WtfGlobal.getLocaleText('pm.project.plan.reports.activities.ending'),WtfGlobal.getLocaleText('pm.project.plan.reports.activities.starting'),WtfGlobal.getLocaleText('pm.project.plan.reports.activities.unstarted')],true);
                break;
            case "Baseline":
                this.newReportWindow(6,["projSummary","dateCompare","durationCompare"/*,"shouldhavestarted"*/,"costCompare","resourceCompare","resourcewiseCompare"],[WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.projectsummary'),WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.date'),WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.duration')/*,"shouldhavestarted"*/,WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.cost'),WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.resource'),WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.resourcewise')],true);
                break;
            //            case "cashflow":
            ////                this.createReportWindow("cost");
            //                this.reportTypeSelect("Daily","resource","cost","all");
            //                this.closeRptModal();
            //                break;
            case "overduetask":
                this.overdueTaskReport();
                this.closeRptModal();
                break;
            case "resanalysis":
                this.acceptDateForResAnalysisRpt('resanalysis');
                this.closeRptModal();
                break;
            case "projSummary":
                this.viewBaselineForReport();
                this.baselineflag = 1;
                this.closeRptModal();
                break;
            case "dateCompare":
                this.viewBaselineForReport();
                this.baselineflag = 2;
                this.closeRptModal();
                break;
            case "durationCompare":
                this.viewBaselineForReport();
                this.baselineflag = 3;
                this.closeRptModal();
                break;
            case "costCompare":
                this.viewBaselineForReport();
                this.baselineflag = 4;
                this.closeRptModal();
                break;
            case "resourceCompare":
                this.viewBaselineForReport();
                this.baselineflag = 5;
                this.closeRptModal();
                break;
            case "resourcewiseCompare":
                this.viewBaselineForReport();
                this.baselineflag = 6;
                this.closeRptModal();
                break;
            case "completetask":
                this.completeTaskReport();
                this.closeRptModal();
                break;
            case "startingsoon":
                this.acceptDate('startdatetasks');
                this.closeRptModal();
                break;
            case "slippingtask":
                this.acceptDate('enddatetasks');
                this.closeRptModal();
                break;
            case "inprogress":
                this.inprogressTaskReport();
                this.closeRptModal();
                break;
            case "unstartedtask":
                this.unstartedTaskReport();
                this.closeRptModal();
                break;
            case "milestones":
                this.milestonesReport();
                this.closeRptModal();
                break;
            case "whowhatwhen":
                this.reportTypeSelect("Daily","resource","time","all");
                this.closeRptModal();
                break;
            case "resourceusage":
                this.reportTypeSelect("Daily","resource","time","all");
                this.closeRptModal();
                break;
            case "taskusage":
                this.reportTypeSelect("Daily","task","cost","all");
                this.closeRptModal();
                break;
            case "cashflow":
                //                this.createReportWindow("cost");
                this.reportTypeSelect("Daily","resource","cost","all");
                this.closeRptModal();
                break;
            case "topleveltask":
                this.toplevelTaskReport();
                this.closeRptModal();
                break;
            case "projectcharts":
                this.showProjectCharts();
                this.closeRptModal();
                break;
        }
    },

    closeRptModal: function(){
        if(this.reportModal !== undefined){
            this.reportModal.close();
            this.reportModal.destroy();
        }
    },

    milestonesReport: function(){
        var tabTitle = WtfGlobal.getLocaleText('pm.project.plan.reports.overview.milestones');
        this.reportTitleForEmbed = Wtf.proj.common.getReportTitleForEmbed('pm.project.plan.reports.overview.milestones');
        var grid = this.createReportGrid(tabTitle,'milestones');
        if(this.manageReportTab('milestones', tabTitle, grid)) {
            grid.store.load({
                params: {
                    action: 'milestones',
                    projid: this.pid
                }
            });
        }
    },

    resourceCompareReport: function(){
        var tabTitle = WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.resource');
        this.reportTitleForEmbed = Wtf.proj.common.getReportTitleForEmbed('pm.project.plan.reports.baseline.resource');
        var grid = this.createReportGrid(tabTitle,'resourcecompare');
        if(this.manageReportTab('resourcecompare', tabTitle, grid)) {
            grid.store.load({
                params: {
                    action: 'resourcecompare',
                    projid: this.pid,
                    baselineid: this.curr_baseline[0].data.baselineid
                }
            });
            //  Wtf.getCmp(this.id+"_resourcecompare_undefined"+"_graphBtn").setDisabled(true);
            this.baselinemodel.close();
            grid.store.on("load", function(store){
                var count = store.getCount();
                for (var cnt = 0; cnt < count; cnt++) {
                    var irec = store.data.items[cnt];
                    if(!this.isResourcesSame(irec.data.resourcename,irec.data.baseresources)) {
                        this.highlightRow(grid, cnt, '#FFE3E3');
                    }
                }
            }, this);
        }
    },
    isResourcesSame:function(res1,res2){// ex: (res1="ravi,vimal,prakash" and res2="vimal,ravi,prakash")  return => true
        if(res1.length== res2.length ){
            var resArray = new Array();
            resArray=res1.split(',');
            for(var i=0;i<resArray.length;i++){
                if(res2.indexOf(resArray[i].trim()) < 0){
                    return false;
                }
            }
        }else{
            return false;
        }
        return true;
    },
    overdueTaskReport: function(){
        var tabTitle = WtfGlobal.getLocaleText('pm.project.report.overduetask');
        this.reportTitleForEmbed = Wtf.proj.common.getReportTitleForEmbed('pm.project.report.overduetask');
        var grid = this.createReportGrid(tabTitle,'overdue');
        if(this.manageReportTab('overdue', tabTitle, grid)) {
            grid.store.load({
                params: {
                    action: 'overdue',
                    projid: this.pid
                }
            });
        }
    },

    durationCompareReport: function(){
        var tabTitle = WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.duration');
        this.reportTitleForEmbed = Wtf.proj.common.getReportTitleForEmbed('pm.project.plan.reports.baseline.duration');
        var grid = this.createReportGrid(tabTitle,'durationcompare');
        if(this.manageReportTab('duration', tabTitle, grid)) {
            grid.store.load({
                params: {
                    action: 'durationcompare',
                    projid: this.pid,
                    baselineid: this.curr_baseline[0].data.baselineid
                }
            });
            this.baselinemodel.close();
        }
    },

    costCompareReport: function(){
        var tabTitle = WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.cost');
        this.reportTitleForEmbed = Wtf.proj.common.getReportTitleForEmbed('pm.project.plan.reports.baseline.cost');
        var grid = this.createReportGrid(tabTitle,'costcompare');
        if(this.manageReportTab('costcompare', tabTitle, grid)) {
            grid.store.load({
                params: {
                    action: 'costcompare',
                    projid: this.pid,
                    baselineid: this.curr_baseline[0].data.baselineid
                }
            });
            this.baselinemodel.close();
        }
    },

    dateCompareReport: function(){
        var tabTitle = WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.date');
        this.reportTitleForEmbed = Wtf.proj.common.getReportTitleForEmbed('pm.project.plan.reports.baseline.date');
        var grid = this.createReportGrid(tabTitle,'datecompare');
        if(this.manageReportTab('datecompare', tabTitle, grid)) {
            grid.store.load({
                params: {
                    action: 'datecompare',
                    projid: this.pid,
                    baselineid: this.curr_baseline[0].data.baselineid
                }
            });
            this.baselinemodel.close();
        }
    },
    resourcewiseCompareReport: function(tabId){
        var tabTitle = WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.resourcewise');
        this.reportTitleForEmbed = Wtf.proj.common.getReportTitleForEmbed('pm.project.plan.reports.baseline.resourcewise');
        var grid = this.createReportGridForResAnalysisRpt(tabId, tabTitle);
        if(Wtf.getCmp(tabId + this.pid))
            Wtf.getCmp(tabId + this.pid).destroy();
        if(this.manageReportTab(tabId, tabTitle, grid)) {
            var gCol = grid.getColumnModel();
            gCol.setColumnHeader(6, WtfGlobal.getLocaleText('lang.work.text'));
            gCol.setColumnHeader(8, WtfGlobal.getLocaleText('lang.cost.text'));
            gCol.setHidden(4, true);
            gCol.setHidden(5, true);
            gCol.setHidden(7, true);
            gCol.setHidden(9, true);
            gCol.setHidden(10, false);
            gCol.setHidden(11, false);
            grid.store.removeAll();
            grid.store.load({
                params: {
                    action: tabId,
                    projid: this.pid,
                    baselineid: this.curr_baseline[0].data.baselineid
                }
            });
        }
        this.baselinemodel.close();
    },

    toplevelTaskReport: function(){
        var tabTitle = WtfGlobal.getLocaleText('pm.project.plan.reports.overview.topleveltasks')
        this.reportTitleForEmbed = Wtf.proj.common.getReportTitleForEmbed('pm.project.plan.reports.overview.topleveltasks');
        var grid = this.createReportGrid(tabTitle,'toplevel',100);
        if(this.manageReportTab('topleveltasks', tabTitle, grid)) {
            grid.store.load({
                params: {
                    action: 'toplevel',
                    percent: 100,
                    projid: this.pid
                }
            });
            grid.store.on("load", function(store){
                var count = store.getCount();
                for (var cnt = 0; cnt < count; cnt++) {
                    var irec = store.data.items[cnt];
                    if(irec.data.isparent) {
                        Wtf.get(grid.getView().getRow(cnt)).addClass('parentRow');
                    }
                }
            }, this);

        }
    },

    inprogressTaskReport: function(){
        var tabTitle = WtfGlobal.getLocaleText('pm.project.plan.reports.activities.inprogress');
        this.reportTitleForEmbed = Wtf.proj.common.getReportTitleForEmbed('pm.project.plan.reports.activities.inprogress');
        var grid = this.createReportGrid(tabTitle,'progressreport',-99);
        if(this.manageReportTab('inprogresstasks',tabTitle , grid)) {
            grid.store.load({
                params: {
                    action: 'progressreport',
                    percent: -99,
                    projid: this.pid
                }
            });
        }
    },

    unstartedTaskReport: function(){
        var tabTitle = WtfGlobal.getLocaleText('pm.project.plan.reports.activities.unstarted');
        this.reportTitleForEmbed = Wtf.proj.common.getReportTitleForEmbed('pm.project.plan.reports.activities.unstarted');
        var grid = this.createReportGrid(tabTitle,'progressreport', 0);
        if(this.manageReportTab('unstartedtasks', tabTitle, grid)) {
            grid.store.load({
                params: {
                    action: 'progressreport',
                    percent: 0,
                    projid: this.pid
                }
            });
        }
    },

    showProjectCharts: function(){
        var store = this.editGrid.getStore();
        if(!((store.getCount() == 1 && store.getAt(0).get('taskid') == '0') || store.getCount() == 0)){
            var tabTitle = WtfGlobal.getLocaleText('pm.dashboard.widget.projectcharts.text');
            this.reportTitleForEmbed = Wtf.proj.common.getReportTitleForEmbed('pm.dashboard.widget.projectcharts.text');
            var tabid = 'projectcharts';
            if(this.manageReportTabForProjSummary(tabid, tabTitle)) {

                var chart = WtfReport.getDataForChart(tabid, 0, this.pid, "", "", "", 'chart');
                for(var c = 0; c < 2; c++){
                    var tempDiv = document.createElement("div");
                    var f = chart[c].chartFlash;
                    var s = chart[c].chartSetting;
                    var d = chart[c].dataURL;
                    if(f === undefined || s === undefined){
                        f = "scripts/bar chart/krwcolumn.swf";
                        s = "scripts/bar chart/krwcolumn_settings.xml";
                    }
                    Wtf.getCmp(tabid + this.pid).autoScroll = true;
                    tempDiv.className = 'projectchartdiv';
                    tempDiv.id = tabid + this.pid + "_chart-gen-" + c;
                    Wtf.getCmp(tabid + this.pid).body.dom.appendChild(tempDiv);
                    createNewChart(f, tempDiv.id + "_chart" + c, '96%', '100%', '8', '#ffffff', s, d, tabid + this.pid + "_chart-gen-" + c);
                }
                hideMainLoadMask();
            }
        } else {
            msgBoxShow(200, 1);
        }
    },

    setPERTDifferences: function(btn){
        Wtf.Ajax.requestEx({
            url: '../../projectCPAController.jsp',
            params: {
                cm: 'getPERTDiff',
                projectid: this.pid
            }
        },
        this,
        function(res){
            res = eval('(' + res + ')');
            if(res && res.data){
                res = eval('(' + res.data + ')');
                if(res.pertStatus < 2){
                    new Wtf.Window({
                        id: 'pertDiffWin',
                        title: WtfGlobal.getLocaleText('pm.project.plan.cpa.durdiff.title'),
                        iconCls: 'iconwin',
                        resizable: false,
                        modal: true,
                        layout: 'border',
                        height: 320,
                        width: 500,
                        items: [{
                            region : 'north',
                            height : 125,
                            border : false,
                            bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                            layout: "fit",
                            html : getTopHtml(WtfGlobal.getLocaleText('pm.project.plan.cpa.durdiff.title'), WtfGlobal.getLocaleText('pm.Help.cpaProjectDefault'), "../../images/Add-project-widget.jpg")
                        },{
                            region: 'center',
                            border: false,
                            bodyStyle : 'background:#f1f1f1;font-size:10px;padding:20px 20px 20px 20px;',
                            layout: 'form',
                            id: 'pertDiffForm',
                            defaultType: 'numberfield',
                            defaults: {
                                maxValue: 99,
                                minValue: -99,
                                allowBlank: false,
                                allowDecimals: false
                            },
                            labelWidth: 260,
                            items: [{
                                fieldLabel: WtfGlobal.getLocaleText('pm.common.pert.diffopt.text'),
                                name: 'optimisticdiff',
                                id: 'optimisticdiff'+this.id,
                                value: res.optimisticDiff
                            },{
                                fieldLabel: WtfGlobal.getLocaleText('pm.common.pert.diffpes.text'),
                                name: 'pessimisticdiff',
                                id: 'pessimisticdiff'+this.id,
                                value: res.pessimisticDiff
                            }]
                        }],
                        buttons: [{
                            text: WtfGlobal.getLocaleText('lang.submit.text'),
                            scope: this,
                            handler: function(btn){
                                var f = Wtf.getCmp('pertDiffForm');
                                if(f){
                                    var val1 = f.findById('optimisticdiff'+this.id).getValue();
                                    var val2 = f.findById('pessimisticdiff'+this.id).getValue();
                                    Wtf.Ajax.requestEx({
                                        url: '../../projectCPAController.jsp',
                                        params: {
                                            cm: 'setDefaultPERTDiff',
                                            projectid: this.pid,
                                            optimisticdiff: val1,
                                            pessimisticdiff: val2
                                        }
                                    }, this, function(res, req){
                                        var obj = eval('('+ res + ')');
                                        if(obj && obj.success)
                                            msgBoxShow(208, 0);
                                        else
                                            msgBoxShow(20, 1);
                                        var comp = Wtf.getCmp('pertDiffWin');
                                        if(comp)
                                            comp.close();
                                    }, function(res, req){
                                        msgBoxShow(20, 1);
                                        var comp = Wtf.getCmp('pertDiffWin');
                                        if(comp)
                                            comp.close();
                                    });
                                }
                            }
                        },{
                            text: WtfGlobal.getLocaleText('lang.cancel.text'),
                            handler: function(btn){
                                var comp = Wtf.getCmp('pertDiffWin');
                                if(comp)
                                    comp.close();
                            }
                        }]
                    }).show();
                } else {
                    msgBoxShow(209, 1);
                }
            }
        }, function(){
            msgBoxShow(4, 1);
        });
    },

    getPERTSheet: function(btn){
        if(!((this.editGrid.getStore().getCount() == 1 && this.editGrid.getStore().getAt(0).get('taskid') == '0') || this.editGrid.getStore().getCount() == 0)){
            var comp = Wtf.getCmp('pertSheet_'+btn.mode+'_'+this.pid);
            var tpanel = Wtf.getCmp('subtabpanelcomprojectTabs_' + this.pid);
            var title = "", icon = "", mode = 0;
            var tp = comp;
            if(!comp){
                if(btn.mode == Wtf.proj.EDIT_PERT_SHEET){
                    title = WtfGlobal.getLocaleText('pm.project.plan.cpa.updatepertsheet');
                    icon = "dpwnd updatePERTSheetTabIcon";
                    mode = Wtf.proj.EDIT_PERT_SHEET;
                }
                if(btn.mode == Wtf.proj.SHOW_CPA_PERT_SHEET){
                    title = WtfGlobal.getLocaleText("pm.project.plan.cpa.cpawithpert");
                    icon = "dpwnd runCPATabIcon";
                    mode = Wtf.proj.SHOW_CPA_PERT_SHEET
                }
                if(btn.mode == Wtf.proj.SHOW_CPA_WITHOUT_PERT_SHEET){
                    title = WtfGlobal.getLocaleText("pm.project.plan.cpa.cpawithoutpert");
                    icon = "dpwnd runCPATabIcon";
                    mode = Wtf.proj.SHOW_CPA_WITHOUT_PERT_SHEET;
                }
                tp = new Wtf.Panel({
                    title: title,
                    id: 'pertSheet_'+mode+'_'+this.pid,
                    layout: 'fit',
                    closable: true,
                    border: false,
                    tabType: Wtf.etype.pplan,
                    mask: null,
                    iconCls: icon,
                    autoScroll: Wtf.isWebKit ? false : true,
                    items: [
                    new Wtf.PERTSheet({
                        id: 'pertSheetPanel_'+mode+'_'+this.pid,
                        border: false,
                        projectid: this.pid,
                        autoScroll: true,
                        mode: mode
                    })
                    ]
                });
                tpanel.add(tp);
                if(!Wtf.isIE7) // TEMP FIX: Causing layout problems in other tabs. Only in IE7.
                    tpanel.doLayout();
            }
            tpanel.setActiveTab(tp);
        } else {
            msgBoxShow(200, 1);
        }
    },

    acceptDate: function(tabId) {
        var popupTitle = (tabId == 'startdatetasks') ? WtfGlobal.getLocaleText('pm.project.plan.reports.activities.starting') : (tabId == 'enddatetasks') ? WtfGlobal.getLocaleText('pm.project.plan.reports.activities.ending'):"";
        var dateFieldText = (tabId == 'startdatetasks') ? WtfGlobal.getLocaleText('lang.starting.text') : (tabId == 'enddatetasks') ? WtfGlobal.getLocaleText('lang.finishing.text'):"";
        var dateWin = new Wtf.Window({
            border: false,
            iconCls: 'iconwin',
            height: '125px',
            title:popupTitle,
            resizable:false,
            width: '300px',
            bodyStyle: 'padding:10px 0px 0px 10px',
            modal: true,
            layout: 'fit',
            items: [ new Wtf.Panel({
                layout:'form',
                border:false,
                height:80,
                labelWidth: 135,
                items:[this.dateBox1 = new Wtf.form.DateField({
                    fieldLabel: WtfGlobal.getLocaleText({key:'pm.project.plan.reports.activities.from',params:dateFieldText}),
                    width:'125px',
                    allowBlank: false,
                    readOnly:true,
                    msgTarget: 'side',
                    format: WtfGlobal.getOnlyDateFormat()
                }), this.dateBox2 = new Wtf.form.DateField({
                    fieldLabel: WtfGlobal.getLocaleText({key:'pm.project.plan.reports.activities.to',params:dateFieldText}),
                    width:'125px',
                    allowBlank: false,
                    readOnly:true,
                    msgTarget: 'side',
                    format: WtfGlobal.getOnlyDateFormat(),
                    value: new Date()
                })]
            })],
            buttons:[{
                text:WtfGlobal.getLocaleText('lang.OK.text'),
                handler:function() {
                    var tabTitle = WtfGlobal.getLocaleText('pm.project.plan.reports.activities.ending');
                    this.reportTitleForEmbed = Wtf.proj.common.getReportTitleForEmbed('pm.project.plan.reports.activities.ending');
                    var actVal = "enddatetasks";
                    if(tabId == 'startdatetasks'){
                        tabTitle = WtfGlobal.getLocaleText('pm.project.plan.reports.activities.starting');
                        this.reportTitleForEmbed = Wtf.proj.common.getReportTitleForEmbed('pm.project.plan.reports.activities.starting');
                        actVal = "startdatetasks";
                    }
                    this.date1 = this.dateBox1.getValue().format('Y-m-d H:i:s.0');
                    this.date2 = this.dateBox2.getValue().format('Y-m-d H:i:s.0');
                    if(this.date1 == '' || this.date2 == ''){
                        msgBoxShow(3, 0);
                        return;
                    } else if(this.date1 > this.date2){
                        msgBoxShow(198, 0);
                        return;
                    }
                    var grid = this.createReportGrid(tabTitle,actVal);
                    if(!this.dateBox1.isValid() || !this.dateBox2.isValid())
                        return;
                    if(Wtf.getCmp(tabId + this.pid))
                        Wtf.getCmp(tabId + this.pid).destroy();
                    if(this.manageReportTab(tabId, tabTitle, grid)) {
                        grid.store.removeAll();
                        grid.store.load({
                            params: {
                                action: tabId,
                                date1: this.date1,
                                date2: this.date2,
                                projid: this.pid
                            }
                        });
                    }
                    dateWin.close();
                },
                scope: this
            },{
                text:WtfGlobal.getLocaleText('lang.cancel.text'),
                handler:function() {
                    dateWin.close();
                },
                scope: this
            }]
        });
        dateWin.show();
    },

    acceptDateForResAnalysisRpt: function(tabId) {
        var popupTitle = WtfGlobal.getLocaleText('pm.project.plan.reports.workload.resourceanalysis');
        var dateWin = new Wtf.Window({
            border: false,
            iconCls: 'iconwin',
            autoHeight: true,
            title:popupTitle,
            resizable:false,
            width: '300px',
            bodyStyle: 'padding:5px 5px 0px 5px',
            modal: true,
            layout: 'fit',
            items: [ new Wtf.Panel({
                layout:'border',
                border:false,
                height:130,
                items:[
                {
                    layout: 'fit',
                    region: 'north',
                    border: false,
                    frame: true,
                    height: 50,
                    html: '<div class="reportAcceptDateWinText">'+WtfGlobal.getLocaleText('pm.project.plan.reports.workload.resourceanalysis.info')+'</div>'
                },{
                    layout: 'form',
                    border: false,
                    region: 'center',
                    frame: true,
                    labelWidth: 135,
                    height: 75,
                    items: [
                    this.dateBox1 = new Wtf.form.DateField({
                        fieldLabel: WtfGlobal.getLocaleText('pm.personalmessages.from.text'),
                        width:'125px',
                        allowBlank: false,
                        readOnly:true,
                        format: WtfGlobal.getOnlyDateFormat(),
                        msgTarget: 'side'
                    }),
                    this.dateBox2 = new Wtf.form.DateField({
                        fieldLabel: WtfGlobal.getLocaleText('pm.common.TO'),
                        width:'125px',
                        allowBlank: false,
                        readOnly:true,
                        msgTarget: 'side',
                        format: WtfGlobal.getOnlyDateFormat(),
                        value: new Date()
                    })]
                }]
            })],
            buttons:[{
                text:WtfGlobal.getLocaleText('lang.OK.text'),
                handler:function() {
                    var tabTitle = WtfGlobal.getLocaleText('pm.project.plan.reports.workload.resourceanalysis');
                    this.reportTitleForEmbed = Wtf.proj.common.getReportTitleForEmbed('pm.project.plan.reports.workload.resourceanalysis');
                    var actVal = 'resanalysis';
                    var grid = this.createReportGridForResAnalysisRpt(actVal, tabTitle);
                    if(!this.dateBox1.isValid() || !this.dateBox2.isValid())
                        return;
                    this.date1 = this.dateBox1.getValue().format('Y-m-d H:i:s.0');
                    this.date2 = this.dateBox2.getValue().format('Y-m-d H:i:s.0');
                    if(this.date1 > this.date2){
                        msgBoxShow(198, 0);
                        return;
                    }
                    if(Wtf.getCmp(tabId + this.pid))
                        Wtf.getCmp(tabId + this.pid).destroy();
                    if(this.manageReportTab(tabId, tabTitle, grid)) {
                        grid.store.removeAll();
                        grid.store.load({
                            params: {
                                action: tabId,
                                date1: this.date1,
                                date2: this.date2,
                                projid: this.pid
                            }
                        });
                    }
                    dateWin.close();
                },
                scope: this
            },{
                text:WtfGlobal.getLocaleText('lang.cancel.text'),
                handler:function() {
                    dateWin.close();
                },
                scope: this
            }]
        });
        dateWin.show();
    },

    createReportGridForResAnalysisRpt: function(tabid, tabtitle){
        var summary = new Wtf.grid.GroupSummary();
        var chart = true;
        var reportcm = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer()
            ,{
                id: 'taskname',
                hidden: false,
                header: WtfGlobal.getLocaleText('pm.common.taskname'),
                dataIndex: 'taskname',
                align: 'left',
                summaryType: 'count',
                summaryRenderer: function(v){
                    return ((v != 0 || v !="") ? '(' + v +' Tasks)' : '(1 Task)');
                }
            },{
                header: WtfGlobal.getLocaleText('pm.project.resource.name'),
                hidden: false,
                dataIndex: 'resourcename',
                align: 'left',
                fixed: true
            },{
                header: WtfGlobal.getLocaleText('pm.project.plan.task.attributes.index'),
                dataIndex: 'taskindex',
                hidden: true,
                align: 'center',
                width: '30px',
                fixed: true
            },{
                header: WtfGlobal.getLocaleText('pm.project.calendar.newevent.startdate'),
                hidden: false,
                dataIndex: 'startdate',
                align: 'right',
                fixed: true,
                renderer: function(v){
                    return (v && typeof v == 'object') ? v.format(WtfGlobal.getOnlyDateFormat()) : v;
                }
            },{
                header: WtfGlobal.getLocaleText('pm.common.enddate'),
                hidden: false,
                dataIndex: 'enddate',
                align: 'right',
                fixed: true,
                renderer: function(v){
                    return (v && typeof v == 'object') ? v.format(WtfGlobal.getOnlyDateFormat()) : v;
                }
            },{
                header: WtfGlobal.getLocaleText('pm.common.schedulework'),
                hidden: false,
                dataIndex: 'duration',
                id:'duration',
                align: 'right',
                fixed: true,
                summaryType: 'sum',
                summaryRenderer: function(v){
                    return v + ' hrs';
                },
                renderer: function(v){
                    return v + ' hrs';
                }
            },{
                header: WtfGlobal.getLocaleText('pm.project.task.actual'),
                dataIndex: 'curduration',
                hidden: false,
                align: 'right',
                summaryType:'sum',
                renderer: function(v){
                    return v + ' hrs';
                },
                summaryRenderer: function(v){
                    return v + ' hrs';
                },
                fixed: true
            },{
                header: WtfGlobal.getLocaleText('pm.project.resource.schcost'),
                dataIndex: 'actcost',
                id: 'cost',
                hidden: false,
                align: 'right',
                summaryType: 'sum',
                renderer: function(v){
                    return Wtf.CurrencySymbol + " " + v;
                },
                summaryRenderer: function(v){
                    return Wtf.CurrencySymbol + " " + v;
                },
                fixed: true
            },{
                header: WtfGlobal.getLocaleText('pm.project.cost.actual'),
                dataIndex: 'curcost',
                hidden: false,
                align: 'right',
                summaryType: 'sum',
                renderer: function(v){
                    return Wtf.CurrencySymbol + " " + v;
                },
                summaryRenderer: function(v){
                    return Wtf.CurrencySymbol + " " + v;
                },
                fixed: true
            },{
                header: WtfGlobal.getLocaleText('pm.project.plan.cpa.workvariance'),
                dataIndex: 'workvary',
                hidden: true,
                align: 'right',
                summaryType: 'sum',
                renderer: function(v){
                    return v + ' hrs';
                },
                summaryRenderer: function(v){
                    return v + ' hrs';
                },
                fixed: true
            },{
                header: WtfGlobal.getLocaleText('pm.project.cost.variance'),
                dataIndex: 'costvary',
                hidden: true,
                align: 'right',
                summaryType: 'sum',
                renderer: function(v){
                    return Wtf.CurrencySymbol + " " + v;
                },
                summaryRenderer: function(v){
                    return Wtf.CurrencySymbol + " " + v;
                },
                fixed: true
            }]);
        var reportRecord  = Wtf.data.Record.create([
        {
            name :'taskid'
        },{
            name :'taskindex'
        },{
            name :'taskname'
        },{
            name :'duration'
        },{
            name :'startdate',
            type: 'date',
            dateFormat: 'Y-m-d'
        },{
            name :'enddate',
            type: 'date',
            dateFormat: 'Y-m-d'
        },{
            name :'resourcename'
        },{
            name :'actcost'
        },{
            name :'curcost'
        },{
            name :'curduration'
        },{
            name :'costvary'
        },{
            name :'workvary'
        }
        ]);

        var reportds = new Wtf.data.GroupingStore({
            url: Wtf.req.prj + 'projectReport.jsp',
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },reportRecord),
            sortInfo:{
                field: 'startdate',
                direction: "ASC"
            },
            groupField:'resourcename'
        });
        var colHeader;
        var baselineid = "", date_1 = "", date_2 = "";
        if(tabtitle == WtfGlobal.getLocaleText('pm.project.plan.reports.workload.resourceanalysis')){
            date_1 = this.dateBox1.getValue().format('Y-m-d');
            date_2 = this.dateBox2.getValue().format('Y-m-d');
        } else {
            baselineid = this.curr_baseline[0].data.baselineid;
        }
        colHeader = WtfReport.getDataForChart(tabid, -99, this.pid, baselineid, date_1, date_2, '');
        var cdata = WtfReport.getDataForChart(tabid, -99, this.pid, baselineid, date_1, date_2, 'chart');
        var kdata = WtfReport.getDataForChart(tabid, -99, this.pid, baselineid, date_1, date_2, 'keys');
        var reportGrid = new Wtf.proj.reportGrid({
            cm: reportcm,
            baseCls: 'reportPanel',
            id: this.id + "_" + tabid,
            ds: reportds,
            border: false,
            chartflag: true,
            autoExpandColumn: 'taskname',
            bodyStyle: 'margin:auto;',
            chart: cdata,
            colorCodeMenu: true,
            colHeader: colHeader,
            colKeys: kdata,
            view: new Wtf.grid.GroupingView({
                forceFit:true,
                showGroupName: false,
                enableNoGroups:false,
                hideGroupedColumn: false,
                emptyText:'<div class="emptyGridText">'+WtfGlobal.getLocaleText('pm.common.nodatadisplay')+' <br></div>'
            }),
            plugins: summary
        });

        reportGrid.on("exportPDF", function(){
            if(reportGrid.getStore().getCount()==0)
                msgBoxShow(154, 1);
            else
                this.ExportReportPDF(tabtitle,reportGrid,tabid,-99, "pdf");
        }, this);
        reportGrid.on("exportCSV", function(){
            if(reportGrid.getStore().getCount()==0)
                msgBoxShow(154, 1);
            else
                this.ExportReportPDF(tabtitle,reportGrid,tabid,-99, "csv");
        }, this);

        reportGrid.on("embedReportClicked", function(){
            if(reportGrid.getStore().getCount()==0)
                msgBoxShow(154, 1);
            else
                this.embedReport(tabtitle, tabid, -99, baselineid, date_1, date_2, cdata.length);
        }, this);
        reportGrid.on('rowdblclick', this.selectRow, this);
        return reportGrid;
    },

    manageReportTab: function(tabId, title, gridObj) {
        if(!Wtf.getCmp(tabId + this.pid)) {
            this.inserttab(tabId + this.pid, title);
            this.rPanel.add(gridObj);
            this.rPanel.doLayout();
            return true;
        } else {
            Wtf.getCmp('subtabpanelcomprojectTabs_' + this.pid).setActiveTab(Wtf.getCmp(tabId + this.pid));
            return false;
        }
    },

    manageReportTabForProjSummary: function(tabId, title, baselineid) {
        if(!Wtf.getCmp(tabId + this.pid)) {
            this.inserttab(tabId + this.pid, title, baselineid);
            return true;
        } else {
            Wtf.getCmp('subtabpanelcomprojectTabs_' + this.pid).setActiveTab(Wtf.getCmp(tabId + this.pid));
            return false;
        }
    },

    completeTaskReport: function(){
        var tabTitle = WtfGlobal.getLocaleText('pm.project.home.health.completed');
        this.reportTitleForEmbed = Wtf.proj.common.getReportTitleForEmbed('pm.project.home.health.completed');
        var grid = this.createReportGrid(tabTitle,'progressreport',100);
        if(this.manageReportTab('completedtasks',tabTitle , grid)) {
            var gCol = grid.getColumnModel();
            gCol.setHidden(6, true);
            gCol.setHidden(7, true);
            gCol.setHidden(9, false);
            gCol.setHidden(10, false);
            grid.store.load({
                params: {
                    action: 'progressreport',
                    percent: 100,
                    projid: this.pid
                }
            });
        }
    },

    exportOptions: function(type){
        var winHeight = 424;
        if(Wtf.isIE6)
            winHeight = 513;
        var imgPath = "../../images/exportpdf40_52.gif";
        var topTitle = WtfGlobal.getLocaleText('pm.project.plan.export.header');
        var opt = WtfGlobal.getLocaleText('pm.project.plan.export.subheader');
        var header = false;
        if(type == "embed"){
            winHeight -= 10;
            header = true;
            imgPath = "../../images/embedexport40_52.gif"
            topTitle = WtfGlobal.getLocaleText('pm.project.plan.export.embed.header');
            opt = WtfGlobal.getLocaleText('pm.project.plan.export.embed.subheader');
        }
        var colSM = new Wtf.grid.CheckboxSelectionModel({
            width: 25
        });
        var colCM = new Wtf.grid.ColumnModel([ colSM,{
            header: WtfGlobal.getLocaleText('lang.column.text'),
            dataIndex: "column"
        },{
            header: WtfGlobal.getLocaleText('pm.project.plan.export.width'),
            hidden: header,
            dataIndex: 'width',
            editor: new Wtf.form.NumberField({
                allowBlank: false,
                maxValue: 850,
                minValue: 50
            })
        }]);
        var pdfDs = new Wtf.data.SimpleStore({
            fields: ['column', 'width'],
            data: [['Task id', 50], ['Task name', 150], ['Duration', 60], ['Start date', 80], ['End date', 80],
            ['Predecessor', 80], ['Resources', 120], ['Percent complete', 60], ['Priority', 70], ['Notes', 100]]
        });
        var embedCol = new Wtf.data.SimpleStore({
            fields: ['column', 'value'],
            data: [['Task name', 1], ['Duration', 2], ['Start date', 3], ['End date', 4],
            ['Predecessor', 5], ['Resources', 6], ['Notes', 7], ['Progress (%)', 8]]
        });
        if(!Wtf.StoreMgr.containsKey("dateformat")){
            Wtf.dateFormatStore.load();
            Wtf.StoreMgr.add("dateformat", Wtf.dateFormatStore);
        }
        var dtCombo = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText('pm.updateprofile.dateformat'),
            store: Wtf.dateFormatStore,
            displayField: 'name',
            valueField: 'id',
            forceSelection: true,
            typeAhead: false,
            editable: false,
            selectOnFocus: true,
            hiddenName: 'dateformat',
            mode: 'local',
            width: 240,
            value: Wtf.pref.DateFormatid,
            triggerAction: 'all',
            emptyText : WtfGlobal.getLocaleText('pm.select.type')+'...',
            allowBlank: false
        });
        var colDS = (type == "pdf") ? pdfDs : embedCol;
        var headerField = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText('lang.header.text'),
            width: 180,
            maxLength:90,
            emptyText: mainPanel.getActiveTab().title + " - Project Plan"
        });
        var colG = new Wtf.grid.EditorGridPanel({
            store: colDS,
            border: false,
            layout: "fit",
            width : 328,
            viewConfig: {
                forceFit: true
            },
            cm: colCM,
            clicksToEdit: 1,
            sm: colSM
        });
        var optionsWin = new Wtf.Window({
            title: (type == "pdf") ? WtfGlobal.getLocaleText('pm.project.plan.export.text') : WtfGlobal.getLocaleText('pm.project.plan.export.embed'),
            iconCls: 'iconwin',
            height: winHeight,
            width: 350,
            modal: true,
            layout: "table",
            layoutConfig: {
                columns: 1
            },
            resizable: false,
            items: [{
                height: 75,
                border : false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                html : getTopHtml(topTitle ,opt,imgPath)
            },{
                height: 40,
                hidden: header,
                border: false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;padding:10px 10px 10px 25px;',
                layout: 'form',
                items:[headerField]
            },{
                bodyStyle : 'background:#f1f1f1;font-size:10px;padding:5px;',
                layout: 'fit',
                width : 338,
                items: [colG]
            },{
                layout: 'fit',
                xtype: 'fieldset',
                cls: 'embedFieldset',
                title: WtfGlobal.getLocaleText('pm.common.dateformat.select'),
                preventScrollbars: false,
                frame: false,
                border: true,
                items: [dtCombo]
            }],
            buttons: [{
                text: WtfGlobal.getLocaleText('lang.proceed.text'),
                scope: this,
                handler: function(){
                    if(headerField.isValid()){
                        var selCol = colSM.getSelections();
                        if(selCol.length > 0){
                            if(type == "pdf"){
                                var colJson = "{\"data\":[";
                                for(var i = 0; i < selCol.length; i++)
                                    colJson += "{\"column\":\"" + selCol[i].data["column"] + "\",\"width\":" + selCol[i].data["width"] + ",\"columnname\":" + selCol[i].data["columnname"] + "},"
                                colJson = colJson.substring(0,(colJson.length - 1)) + "]}";
                                var hText = headerField.getValue()=="" ? headerField.emptyText : WtfGlobal.HTMLStripper(headerField.getValue());
                                this.editGrid.exportfile("pdf", colJson, hText);
                            } else {
                                var colVal = 0;
                                for(var c = 0; c < selCol.length; c++){
                                    colVal += Math.pow(2, selCol[c].data["value"]);
                                }
                                this.embedCodeWin(Wtf.pagebaseURL+"projview.jsp?id="+this.pid+"&col="+colVal+"&df="+dtCombo.getValue(), WtfGlobal.getLocaleText('pm.common.projectplan'));
                            }
                            optionsWin.close();
                        } else {
                            msgBoxShow(211, 1);
                        }
                    }
                }
            },{
                text: WtfGlobal.getLocaleText('lang.cancel.text'),
                handler: function(){
                    optionsWin.close();
                }
            }]
        });
        colG.on("render", function(obj){
            obj.getSelectionModel().selectAll();
        }, this);
        optionsWin.show();
        function onDateFormatStoreLoad(store) {
            dtCombo.setValue(Wtf.pref.DateFormatid);
            store.un('datachanged', onDateFormatStoreLoad, this);
        }
        Wtf.dateFormatStore.on('datachanged', onDateFormatStoreLoad, this);
    },

    createReportGrid: function(tabtitle,actionValue,percent){
        var a = "";
        var columnHeader = [];
        var reportcm = WtfReport.getColumnModel(actionValue, percent);
        var reportRecord  = WtfReport.getRecordCreate();
        var reportds = new Wtf.data.Store({
            url: Wtf.req.prj + 'projectReport.jsp',
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },reportRecord)
        });
        var date_1 = "";
        var date_2 = "";
        if(actionValue == "enddatetasks" || actionValue == "startdatetasks"){
            date_1 = this.dateBox1.getValue().format('Y-m-d');
            date_2 = this.dateBox2.getValue().format('Y-m-d');
        }
        var action=actionValue;
        var baselineid = (this.curr_baseline == null) ? "" : this.curr_baseline[0].data.baselineid;
        var cdata = WtfReport.getDataForChart(action, percent, this.pid, baselineid, date_1, date_2, 'chart');
        var kdata = WtfReport.getDataForChart(action, percent, this.pid, baselineid, date_1, date_2, 'keys');
        columnHeader = WtfReport.getDataForChart(action, percent, this.pid, baselineid, date_1, date_2, 'header');

        var reportGrid = new Wtf.proj.reportGrid({
            cm: reportcm,
            baseCls: 'reportPanel',
            id: this.id + "_" + action+"_"+percent,
            ds: reportds,
            border: false,
            chartflag: (action == 'resourcecompare') ? false: true,
            autoExpandColumn: 'taskname',
            colHeader: columnHeader,
            colKeys: kdata,
            colorCodeMenu: (action == 'resourcecompare') ? false: true,
            bodyStyle: 'margin:auto;',
            chart: cdata,
            viewConfig: {
                emptyText:'<div class="emptyGridText">'+WtfGlobal.getLocaleText('pm.common.nodatadisplay')+' <br></div>'
            }
        });
        reportGrid.on("exportPDF", function(){
            if(reportGrid.getStore().getCount()==0)
                msgBoxShow(154, 1);
            else
                this.ExportReportPDF(tabtitle,reportGrid,actionValue,percent, "pdf");
        }, this);
        reportGrid.on("exportCSV", function(){
            if(reportGrid.getStore().getCount()==0)
                msgBoxShow(154, 1);
            else
                this.ExportReportPDF(tabtitle,reportGrid,actionValue,percent, "csv");
        }, this);
        reportGrid.on("embedReportClicked", function(){
            if(reportGrid.getStore().getCount()==0)
                msgBoxShow(154, 1);
            else
                this.embedReport(tabtitle, action, percent, baselineid, date_1, date_2, cdata.length);
        }, this);
        reportGrid.on('rowdblclick', this.selectRow, this);
        return reportGrid;
    },
    ExportReportPDF: function(tabtitle,reportGrid,actionValue,per,exportType){
        var repColModel = reportGrid.getColumnModel();
        var numCols = reportGrid.getColumnModel().getColumnCount();
        var colHeader = "{\"data\":[";
        var dataindex = "{\"data\":[";
        for(var i = 1;i<numCols;i++){ // skip row numberer
            if(!(repColModel.isHidden(i))){
                colHeader += "\""+repColModel.config[i].colRealName+"\",";
                dataindex += "\""+repColModel.getDataIndex(i)+"\",";
            }
        }
        colHeader = colHeader.substr(0,colHeader.length-1)+"]}"
        dataindex = dataindex.substr(0,dataindex.length-1)+"]}"
        var url =  "../../exportProjectReport.jsp?" +"date1="+ ((this.date1!=null)?this.date1:"")+
        "&date2="+ ((this.date2!=null)?this.date2:"")+
        "&action=" + actionValue +
        "&percent=" + ((per!=null)?per:"") +
        "&colHeader=" + colHeader+
        "&dataindex=" + dataindex+
        "&projname=" + mainPanel.getActiveTab().title +
        "&reportname=" + this.reportTitleForEmbed +
        "&baselineid=" + ((this.curr_baseline == null) ? "": this.curr_baseline[0].data.baselineid) +
        "&projid=" + this.pid +
        "&reporttypeformat=1"+
        "&exporttype=" + exportType;
        //        setDldUrl(url);

        if(exportType=="pdf") {
            var repType=1;
            this.selectTemplate(tabtitle,url,reportGrid,repType);
        } else {
            setDldUrl(url);
        }
    },

    exportSummary: function(tabTitle, type, baselineid){
        var url =  "../../ExportProjectSummary.jsp?" +
        "&action=projsummary" +
        "&projname=" + mainPanel.getActiveTab().title +
        "&reportname=" + this.reportTitleForEmbed +
        "&projectid=" + this.pid +
        "&baselineid="+ this.curr_baseline[0].data.baselineid +
        "&reporttypeformat=3"+
        "&exporttype=" + type;
        setDldUrl(url);
    },

    embedReport: function(reportName, reportID, percent, baselineid, date_1, date_2, graphCount){
        var url = Wtf.pagebaseURL+"projview.jsp?id="+this.pid+"&rid="+reportID+"&rnm="+this.reportTitleForEmbed+"&pc="+percent+"&u="+loginid;
        if(baselineid && baselineid != "")
            url += "&bid="+baselineid;
        if(date_1 && date_1 != ""){
            url += "&d1="+date_1; // encrypt date format here
        }
        if(date_2 && date_2 != ""){
            url += "&d2="+date_2;
        }
        this.embedCodeWin(url, WtfGlobal.getLocaleText('pm.common.report'), Math.floor(500*graphCount));
    },

    selectRow: function(gd, ri, e) {
        var recid = gd.selModel.getSelected().data.taskid;
        Wtf.getCmp('subtabpanelcomprojectTabs_' + this.pid).setActiveTab(Wtf.getCmp(this.pid + "_projplanPane"));
        this.editGrid.selectGridRowOnPanelClick(recid);
        this.wraper.scrollTo(Wtf.getCmp('Panel' + recid).x - 16, this.editGrid.krow * 21);
    },

    createReportWindow: function(object, Xline) {
        if(this.editGrid.dstore.getCount() <= 1) {
            msgBoxShow(51, 1);
            return;
        }
        var optionPanel = new Wtf.form.FieldSet({
            autoHeight: true,
            width: '96%',
            cls: 'optionFieldSet',
            defaultType: 'datefield',
            layoutConfig:{
                labelSeparator : ''
            },
            title: WtfGlobal.getLocaleText('pm.common.options'),
            items:[{
                id: 'fromDate',
                fieldLabel: WtfGlobal.getLocaleText('pm.personalmessages.from.text'),
                labelStyle: 'margin-left:30px; width:50px',
                width: 100
            },

            {
                id: 'toDate',
                fieldLabel: WtfGlobal.getLocaleText('pm.common.TO'),
                labelStyle: 'margin-left:30px; width:50px',
                width: 100
            }]
        });
        var periodCombo = new Wtf.form.ComboBox({
            fieldLabel: 'Choose period ',
            bodyStyle: 'margin:auto; padding-top:5px;',
            store: new Wtf.data.SimpleStore({
                fields: ['period'],
                data: this.periodData
            }),
            displayField: 'period',
            allowBlank: false,
            typeAhead: true,
            mode: 'local',
            forceSelection: true,
            editable: false,
            triggerAction: 'all',
            value: WtfGlobal.getLocaleText('pm.project.plan.weekly'),
            selectOnFocus: true
        });
        var reportModal = new Wtf.Window({
            title: WtfGlobal.getLocaleText('pm.common.report'),
            width: 311,
            resizable: false,
            layout: 'form',
            iconCls: 'iconwin',
            modal: true,
            bodyStyle: 'padding:7px',
            items:[
            periodCombo,
            new Wtf.form.FieldSet({
                width: '100%',
                autoHeight: true,
                id: 'groupSet',
                title: WtfGlobal.getLocaleText('pm.common.groupby'),
                layout: 'column',
                layoutConfig:{
                    labelSeparator: ''
                },
                cls: 'windowFieldset',
                bodyStyle: 'margin:auto;padding-left:10px',
                items:[{
                    columnWidth: 0.5,
                    border: false,
                    defaultType: 'radio',
                    items:[{
                        boxLabel: WtfGlobal.getLocaleText('pm.common.task'),
                        hideLabel: true,
                        id: 'groupTask',
                        checked: true,
                        name: 'group'
                    }]
                },{
                    columnWidth: 0.5,
                    defaultType: 'radio',
                    border: false,
                    items:[{
                        boxLabel: WtfGlobal.getLocaleText('pm.common.resource'),
                        hideLabel: true,
                        id: 'groupResource',
                        name: 'group'
                    }]
                }]
            }),new Wtf.form.FieldSet({
                width: '100%',
                title: WtfGlobal.getLocaleText('lang.display.text'),
                layout: 'column',
                autoHeight: true,
                layoutConfig:{
                    labelSeparator : ''
                },
                cls: 'windowFieldset',
                bodyStyle: 'margin:auto;padding-left:10px',
                items:[{
                    columnWidth: 0.5,
                    border: false,
                    items:[{
                        xtype : 'radio',
                        id: 'reportAll',
                        hideLabel: true,
                        boxLabel: WtfGlobal.getLocaleText('lang.all.text'),
                        checked: true,
                        name: 'displaygrp'
                    }]
                },{
                    columnWidth: 0.5,
                    border: false,
                    items: [{
                        xtype : 'radio',
                        id: 'reportSelected',
                        hideLabel: true,
                        boxLabel: WtfGlobal.getLocaleText('pm.common.selected'),
                        name: 'displaygrp'
                    }]
                }
                ,optionPanel]
            })],
            buttons:[{
                width: 35,
                text: WtfGlobal.getLocaleText('lang.OK.text'),
                handler: function(){
                    var period = periodCombo.getValue();
                    var groupBy = "resource";
                    if(Wtf.getCmp('groupTask').getValue())
                        groupBy = "task";
                    //                    var selRad = "all";
                    //                    var from = null;
                    //                    var to = null;
                    //                    if(Wtf.getCmp('reportSelected').getValue()){
                    //                        from = Wtf.getCmp('fromDate').getValue();
                    //                        to = Wtf.getCmp('toDate').getValue();
                    //                        selRad = "selected";
                    //                    }
                    this.reportTypeSelect(period,groupBy,object);
                    reportModal.close();
                },
                scope: this
            },{
                width: 35,
                text: WtfGlobal.getLocaleText('lang.cancel.text'),
                handler:function(){
                    reportModal.close();
                }
            }]
        })
        optionPanel.hide();
        Wtf.getCmp('reportSelected').on('focus',function(){
            optionPanel.show();
        });
        Wtf.getCmp('reportAll').on('focus',function(){
            Wtf.getCmp('reportSelected').checked = false;
            optionPanel.hide();
        });
        reportModal.show();
    },

    inserttab: function(tabId, tabTitle, baselineid){
        var tpanel = null;
        if(tabTitle != WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.projectsummary') && tabTitle != WtfGlobal.getLocaleText('pm.project.plan.reports.overview.projectcharts')){
            tpanel = Wtf.getCmp('subtabpanelcomprojectTabs_' + this.pid);
            this.rPanel = new Wtf.Panel({
                title: tabTitle,
                id: tabId,
                layout: 'fit',
                closable: true,
                border: false,
                tabType: Wtf.etype.pplan,
                mask: null,
                iconCls: "pwnd reporttab",
                autoScroll: Wtf.isWebKit ? false : true
            });
        } else if(tabTitle == WtfGlobal.getLocaleText('pm.project.plan.reports.baseline.projectsummary')) {
            tpanel = Wtf.getCmp('subtabpanelcomprojectTabs_' + this.pid);
            this.rPanel = new Wtf.Panel({
                title: tabTitle,
                id: tabId,
                layout: 'fit',
                closable: true,
                border: false,
                tabType: Wtf.etype.pplan,
                mask: null,
                iconCls: "pwnd reporttab",
                tbar: [{
                    text:WtfGlobal.getLocaleText('lang.export.text'),
                    iconCls: 'pwnd exporticontext',
                    scope:this,
                    menu: [
                        new Wtf.Action({
                            text: WtfGlobal.getLocaleText('pm.project.plan.export.pdf'),
                            iconCls: 'pwnd pdfexporticon',
                            scope: this,
                            handler:function(){
                                this.exportSummary(tabTitle,"pdf", baselineid);
                            }
                        }),
                        new Wtf.Action({
                            text: WtfGlobal.getLocaleText('pm.project.plan.export.csv'),
                            iconCls: 'pwnd csvexporticon',
                            scope: this,
                            hidden: true,
                            handler:function(){
                                this.exportSummary(tabTitle,"csv", baselineid);
                            }
                        })
                    ]
            }]
        });
} else {
    tpanel = Wtf.getCmp('subtabpanelcomprojectTabs_' + this.pid);
    this.rPanel = new Wtf.Panel({
        title: tabTitle,
        id: tabId,
        layout: 'fit',
        closable: true,
        border: false,
        tabType: Wtf.etype.pplan,
        mask: null,
        iconCls: "pwnd reporttab",
        tbar: [{
            text:WtfGlobal.getLocaleText('lang.export.text'),
            iconCls: 'pwnd exporticontext',
            scope:this,
            menu: [
            new Wtf.Action({
                text: WtfGlobal.getLocaleText('pm.project.plan.reports.export.embed'),
                iconCls: 'pwnd exporticon',
                scope: this,
                handler: function(){
                    this.embedReport(tabTitle, 'projectcharts', -99, "", "", "", 1.3);
                }
            })
        ]
    }]
    });
}
tpanel.add(this.rPanel);
    if(!Wtf.isIE7) // TEMP FIX: Causing layout problems in other tabs. Only in IE7.
        tpanel.doLayout();
    tpanel.setActiveTab(this.rPanel);
    this.rPanel.mask = new Wtf.LoadMask(document.body);
},

createColModel: function(object, action){
    var gridId;
    var colConfig = [];
    var colObj1 = {};
    colObj1['header'] = WtfGlobal.getLocaleText('lang.name.text');
    colObj1['dataIndex'] = 'info';
    colObj1['width'] = 70;
    colObj1['id'] = 'nameCol';
    colObj1['sortable'] = false;
    colConfig[colConfig.length] = colObj1;
    var colObj2 = {};
    colObj2['header'] = 'level';
    colObj2['dataIndex'] = 'level';
    colObj2['hidden'] = true;
    colObj2['sortable'] = false;
    colConfig[colConfig.length] = colObj2;
    var colObj3 = {};
    colObj3['header'] = 'flag';
    colObj3['dataIndex'] = 'flag';
    colObj3['hidden'] = true;
    colObj3['sortable'] = false;
    colConfig[colConfig.length] = colObj3;
    var newPage = this.reportPageSize;
    var columncnt = 0
    var columnHeader;
    if(typeof object == "object"){
        columnHeader = object;
        gridId = action;
    } else {
        gridId = object;
        if(action == ">>"){
            var pageNo = Wtf.getCmp(gridId).pageNo++;
            columncnt = (pageNo + 1) * this.reportPageSize;
            newPage = columncnt + this.reportPageSize;
            columnHeader = Wtf.getCmp(gridId).headerObj;
        } else {
            pageNo = Wtf.getCmp(gridId).pageNo--;
            columncnt = (pageNo - 1) * this.reportPageSize;
            newPage = columncnt + this.reportPageSize;
            columnHeader = Wtf.getCmp(gridId).headerObj;
        }
    }
    for(; columncnt < columnHeader.length && columncnt < newPage; columncnt++) {
        var colObj = {};
        if(colObj['header'] = columnHeader[columncnt][0]){
            colObj['header'] = '<div wtf:qtip="'+columnHeader[columncnt][0]+'">'+columnHeader[columncnt][0]+'</div>';
            colObj['dataIndex'] = columnHeader[columncnt][0];
        } else {
            colObj['header'] = '<div wtf:qtip="'+columnHeader[columncnt][1]+'">'+columnHeader[columncnt][1]+'</div>';
            colObj['dataIndex'] = columnHeader[columncnt][1];
        }
        colObj['width'] = 80;
        colObj['fixed'] = true;
        colObj['align'] = "right";
        colObj['sortable'] = false;
        colObj['renderer'] = function(val) {
            return unescape(val)
        };
        colConfig[colConfig.length] = colObj;
    }
    //        if(columncnt < columnHeader.length && gridObj !== undefined){
    //            gridObj.enableNextButton();
    ////            nxtBtn.enable();
    //        } else if(gridObj !== undefined){
    //            gridObj.disableNextButton();
    ////            nxtBtn.disable();
    //        }
    //        if(columncnt == this.reportPageSize && gridObj !== undefined) {
    //            gridObj.disablePrevButton();
    ////            prevBtn.disable();
    //        } else if(gridObj !== undefined) {
    //            gridObj.enablePrevButton();
    ////            prevBtn.enable();
    //        }
    return colConfig;
},

reportTypeSelect: function(period,groupBy,reportType) {
    if(!((this.editGrid.getStore().getCount() == 1 && this.editGrid.getStore().getAt(0).get('taskid') == '0') || this.editGrid.getStore().getCount() == 0)){
        var repoTabId = this.pid + '_'+reportType+'_' + groupBy;
        if(Wtf.getCmp(repoTabId)) {
            Wtf.getCmp('subtabpanelcomprojectTabs_' + this.pid).setActiveTab(Wtf.getCmp(repoTabId));
            return false;
        } else {
            this.rPanel = Wtf.getCmp(repoTabId);
            var reportPanel = this.rPanel;
            var tabTitle = "";
            if(reportType == "cost") {
                tabTitle = WtfGlobal.getLocaleText('pm.report.resourcecost.title');
                this.reportTitleForEmbed = Wtf.proj.common.getReportTitleForEmbed('pm.report.resourcecost.title');
                if(groupBy == "task"){
                    tabTitle =WtfGlobal.getLocaleText('pm.report.projectcost.title');
                    this.reportTitleForEmbed = Wtf.proj.common.getReportTitleForEmbed('pm.report.projectcost.title');
                }
            } else {
                tabTitle =WtfGlobal.getLocaleText('pm.report.resourecetimesheet.title');
                this.reportTitleForEmbed = Wtf.proj.common.getReportTitleForEmbed('pm.report.resourcetimesheet.title');
                if(groupBy == "task"){
                    tabTitle = WtfGlobal.getLocaleText('pm.report.project.timesheet.title');
                    this.reportTitleForEmbed = Wtf.proj.common.getReportTitleForEmbed('pm.report.project.timesheet.title');
                }
            }
            if(reportPanel === undefined)
                this.inserttab(repoTabId, tabTitle);
            else if(Wtf.getCmp(reportPanel.id + '_reportGrid'))
                Wtf.getCmp(reportPanel.id + '_reportGrid').destroy();
            this.rPanel.mask.show();
            Wtf.Ajax.requestEx({
                url: Wtf.req.prj + 'projectReport.jsp',
                method: 'GET',
                params: {
                    groupby: groupBy,
                    reporttype: reportType + 'line',
                    projectid: this.pid,
                    period: period
                }
            },
            this,
            function(response, e){
                this.makeReport(response, e, groupBy, reportType + 'line', period);
            });
        }
    } else {
        msgBoxShow(200, 1);
    }
},

createFields: function(columnheader){
    var fields = [];
    var fObj1 = {};
    fObj1['name'] = 'level';
    fObj1['type'] = 'string';
    fObj1['mapping'] = 'level';
    fields[fields.length] = fObj1;
    var fObj2 = {};
    fObj2['name'] = 'info';
    fObj2['type'] = 'string';
    fObj2['mapping'] = 'info';
    fields[fields.length] = fObj2;
    var fObj3 = {};
    fObj3['name'] = 'flag';
    fObj3['type'] = 'boolean';
    fObj3['mapping'] = 'flag';
    fields[fields.length] = fObj3;
    var fObj4 = {};
    fObj4['name'] = 'totalrow';
    fObj4['type'] = 'boolean';
    fObj4['mapping'] = 'totalRow';
    fields[fields.length] = fObj4;
    for(var fieldcnt = 0; fieldcnt < columnheader.length; fieldcnt++) {
        var fObj = {};
        if(columnheader[fieldcnt][0]) {
            fObj['name'] = columnheader[fieldcnt][0];
            fObj['mapping'] = columnheader[fieldcnt][0];
        } else {
            fObj['name'] = columnheader[fieldcnt][1];
            fObj['mapping'] = columnheader[fieldcnt][1];
        }
        fObj['type'] = 'string';
        fields[fields.length] = fObj;
    }
    return fields;
},

makeReport: function(response,e, groupBy, reportType, period) {
    var chart = true;
    var obj = eval('(' + response+ ')');
    if(obj != null){
        var simstore = new Wtf.data.JsonStore({
            fields: this.createFields(obj.columnheader),
            data: obj.data
        });
        var reportPanel = this.rPanel;
        var cdata = WtfReport.getDataForChart('line', period, this.pid, '', groupBy, reportType, 'chart');
        var reportGrid = new Wtf.proj.reportGrid({
            cm: new Wtf.grid.ColumnModel(this.createColModel(obj.columnheader, reportPanel.id + "_reportGrid")),
            headerObj: obj.columnheader,
            pageNo: 0,
            columnPerPage: this.reportPageSize,
            enableColumnMove: false,
            id: reportPanel.id + '_reportGrid',
            baseCls: 'reportPanel',
            layout:'fit',
            ds: simstore,
            chartflag: chart,
            colorCodeMenu: false,
            autoScroll: true,
            columnScroll: true,
            autoExpandColumn: 'nameCol',
            bodyStyle: 'margin:auto;',
            chart: cdata
        });
        reportGrid.on("exportPDF", function(){
            this.ExportReports(reportGrid,reportPanel.title,groupBy,reportType,period, "pdf");
        }, this);
        reportGrid.on("exportCSV", function(){
            this.ExportReports(reportGrid,reportPanel.title,groupBy,reportType,period, "csv");
        }, this);
        reportGrid.on("scrollPrev", function(){
            reportPanel.mask.show();
            this.reportNext(reportPanel.id + "_reportGrid", "<<", reportGrid);
            reportPanel.mask.hide();
        }, this);
        reportGrid.on("scrollNext", function(){
            reportPanel.mask.show();
            this.reportNext(reportPanel.id + "_reportGrid", ">>", reportGrid);
            reportPanel.mask.hide();
        }, this);
        reportGrid.on("embedReportClicked", function(){
            if(reportGrid.getStore().getCount()==0)
                msgBoxShow(154, 1);
            else
                this.embedReport(reportPanel.title, 'line', period, '', groupBy, reportType, cdata.length);
        }, this);
        reportPanel.add(reportGrid);
        reportGrid.adjustNavigationButtons();
        reportPanel.doLayout();
        this.setReportStyle(reportPanel.id + '_reportGrid');
    //            if(obj.columnheader.length <= this.reportPageSize)
    //                Wtf.getCmp(reportPanel.id + "_reportGrid_nextButton").disable();
    }
    this.rPanel.mask.hide();
},

setReportStyle: function(gridId){
    var repGrid = Wtf.getCmp(gridId);
    var simStore = repGrid.getStore();
    var count = simStore.getCount();
    var reportView = repGrid.getView();
    for(var cnt = 0; cnt < count; cnt++){
        if(simStore.getAt(cnt).data['flag'])
            reportView.getRow(cnt).className = 'repoBold';
        if(simStore.getAt(cnt).data['totalrow'])
            reportView.getRow(cnt).className += ' x-grid3-summary-row';
        var ML = parseInt(simStore.getAt(cnt).data['level'])*20;
        reportView.getCell(cnt,0).firstChild.style.marginLeft = ML + 'px';
    }
    if(repGrid.lastPage){
        var colNo = repGrid.getColumnModel().config.length;
        for(cnt = 0; cnt < count; cnt++){
            reportView.getCell(cnt, colNo-1).className += ' x-grid3-summary-row';
        }
    }
},

ExportReports: function(rGrid, reportname, groupBy, reportType, period, exportType) {
    var url = '../../exportProjectReport.jsp?blank=0' +
    '&projname=' + mainPanel.getActiveTab().title +
    '&reportname=' + this.reportTitleForEmbed + '&groupby=' + groupBy +
    '&reporttype=' + reportType + '&period=' + period +
    '&projectid=' + this.pid +
    '&reporttypeformat=2'+
    '&exporttype=' + exportType;

    if(exportType=="pdf") {
        var repType=2;
        this.selectTemplate(reportname,url,rGrid,repType);
    } else {
        Wtf.get('downloadframe').dom.src = url;
    }
},

reportNext: function(gridId, action, gridObj){
    var colModel = Wtf.getCmp(gridId).getColumnModel();
    colModel.setConfig(this.createColModel(gridId, action));
    gridObj.adjustNavigationButtons();
    this.setReportStyle(gridId);
},

searchTextOnKeyPress: function(e) {
    this.taskDelay.cancel();
    if(this.searchTask.getValue() == "") {
        this.editGrid.selModel.clearSelections();
        this.wraper.scrollToTop();
        return;
    }
    this.taskDelay.delay(500, this.editGrid.searchText, this.editGrid);
},

searchTextOnEnter: function(f, e) {
    if(e.getKey() == 13) {
        if(this.searchTask.getValue() == "") {
            this.editGrid.selModel.clearSelections();
            this.wraper.scrollToTop();
            return;
        }
//        this.editGrid.searchText;
    }
},

changeStartDate:function(){
    var store = this.editGrid.getStore();
    if(!((store.getCount() == 1 && store.getAt(0).get('taskid') == '0') || store.getCount() == 0)){
        var td = new Date(WtfGlobal.getOnlyDateFormat());
        var prvStartDate = this.startDate;
        var startDatePanel = new Wtf.Panel({
            id:'startDatePanel',
            layout:'border',
            width:275,
            items:[{
                region:'center',
                bodyStyle:'padding:8px 8px 8px 8px',
                layout:'form',
                items:[this.newstartDate = new Wtf.form.DateField({
                    fieldLabel: WtfGlobal.getLocaleText('pm.common.newstartdate'),
                    width:130,
                    value:Date.parseDate(this.editGrid.projStartDate, 'Y-m-j H:i:s').format(WtfGlobal.getOnlyDateFormat()),
                    readOnly:true,
                    format: WtfGlobal.getOnlyDateFormat(),//'D j-m-Y',
                    allowBlank:false
                })]
            }]
        });
        this.startDateWindow = new Wtf.Window({
            title:WtfGlobal.getLocaleText('pm.common.date.setstartdate'),
            modal:true,
            layout:'fit',
            iconCls : 'iconwin',
            items:[startDatePanel],
            resizable:false,
            autoDestroy:true,
            height:120,
            width:275,
            buttons:[{
                text:WtfGlobal.getLocaleText('lang.OK.text'),
                enableToggle:true,
                id:'ok',
                handler:this.getUserOptionForStartDate,
                scope: this
            },{
                text:WtfGlobal.getLocaleText('lang.cancel.text'),
                id:'cancel',
                scope:this,
                handler:function() {
                    this.startDateWindow.close();
                }
            }]
        });
        this.startDateWindow.show();
    } else {
        msgBoxShow(200, 1);
    }
},

getUserOptionForStartDate: function(btn, e){
    if(this.newstartDate.isValid())
        var val = this.newstartDate.getValue();
    this.startDateWindow.close();
    if(val < this.projstartdate){
        this.projstartdate = val;
        Wtf.Msg.show({
            title: WtfGlobal.getLocaleText('pm.common.plzconfirm'),
            msg: WtfGlobal.getLocaleText('pm.msg.shifttasks.text'),
            buttons: Wtf.Msg.YESNOCANCEL,
            fn: this.setStartDate,
            scope: this,
            icon: Wtf.MessageBox.QUESTION
        });
    } else {
        this.projstartdate = val;
        this.setStartDate('yes');
    }
},

setStartDate:function(btn){
    if(btn != 'cancel'){
        Wtf.Ajax.requestEx({
            url: Wtf.req.prj + 'task.jsp',
            params : {
                projectid: this.pid,
                startDate: this.projstartdate.format('Y-m-j H:i:s'),//yyyy-MM-d HH:mm:ss
                seq: "3",
                option: btn
            },
            method: 'POST'
        },
        this,
        function(result, request) {
            var nodeobj = eval("(" + result + ")");
            if(nodeobj.success) {
                var dt = this.projstartdate;
                this.projstartdate = dt.format('Y-m-j');
                this.editGrid.projStartDate = dt.format('Y-m-j H:i:s');
                this.editGrid.refreshPlan();
                collectProjectData();
                bHasChanged=true;
                if(refreshDash.join().indexOf("all") == -1 || refreshDash.join().indexOf("mtl") == -1){
                    refreshDash[refreshDash.length] = 'mtl';
                }
                msgBoxShow(52, 0);
            } else if(nodeobj.msg != "")
                msgBoxShow(["Invalid Input",nodeobj.msg]);
        },
        function() {
            msgBoxShow(53, 1);
        });
    }
},

notify:function(){
    var store = this.editGrid.getStore();
    if(!((store.getCount() == 1 && store.getAt(0).get('taskid') == '0') || store.getCount() == 0)){
            if(this.editGrid.getSelectionModel().getSelections().length==0){
                msgBoxShow(167, 0);
                return;
            }
            var idstr = "";
            var assignidstr = "";
            var rec;
            var notificationflag = true;
            var records = this.editGrid.getSelectionModel().getSelections();
            for(var i=0;i<records.length;i++){
                if(records[i].data.resourcename!="" && records[i].data.percentcomplete<100){
                    idstr += "'"+records[i].data.taskid+"',";
                    assignidstr += records[i].data.resourcename+",";
                }else
                    notificationflag = false;
            }
            if(idstr!=""){
                idstr = idstr.substr(0,idstr.lastIndexOf(","));
                assignidstr = assignidstr.substr(0,assignidstr.lastIndexOf(","));
                Wtf.Ajax.requestEx({
                    method:'POST',
                    url:Wtf.req.prj + 'task.jsp',
                    params:{
                        seq: "4",
                        pname:this.pname,
                        pid:this.pid,
                        grouptype:this.groupType,
                        idstr:idstr,
                        assignidstr:assignidstr
                    }
                }, this,
                function(result, req){
                    if(result == "typeError"){
                        msgBoxShow(183,2);
                    }else{
                        if(notificationflag)
                            msgBoxShow([WtfGlobal.getLocaleText('pm.msg.SUCCESS'),WtfGlobal.getLocaleText({key:'pm.msg.notification.sent',params:result})],0);
                        else
                            msgBoxShow(169,0);
                    }
                });
            } else
                msgBoxShow(168,1);
        } else {
            msgBoxShow(200, 1);
        }
},

notifyOverdue:function(){
    var store = this.editGrid.getStore();
    if(!((store.getCount() == 1 && store.getAt(0).get('taskid') == '0') || store.getCount() == 0)){
        Wtf.Ajax.requestEx({
            method:'POST',
            url:Wtf.req.base + 'OverdueNotifications.jsp',
            params:{
                pid:this.pid
            }
        }, this,
        function(result, req){
            if(result == "typeError"){
                msgBoxShow(183, 2);
            } else {
                if(result !== 'error'){
                    if(result == 'overdueError')
                        msgBoxShow([WtfGlobal.getLocaleText('pm.msg.INFORMATION'),WtfGlobal.getLocaleText('pm.notification.overdue.notask')],0);
                    else if(result == 'resourceError')
                        msgBoxShow([WtfGlobal.getLocaleText('pm.msg.INFORMATION'),WtfGlobal.getLocaleText('pm.notification.overdue.noresorce')],0);
                    else
                        msgBoxShow([WtfGlobal.getLocaleText('pm.msg.SUCCESS'),WtfGlobal.getLocaleText({key:'pm.msg.notification.sent',params:result})],0);
                } else
                    msgBoxShow(20, 0);
            }
        });
    } else {
        msgBoxShow(200, 1);
    }
},

notifyAll:function(){
    var store = this.editGrid.getStore();
    if(!((store.getCount() == 1 && store.getAt(0).get('taskid') == '0') || store.getCount() == 0)){
        Wtf.Ajax.requestEx({
            method:'POST',
            url:Wtf.req.prj + 'task.jsp',
            params:{
                seq: "5",
                pname:this.pname,
                pid:this.pid
            }
        }, this, function(result, req){
            if(result == "typeError"){
                msgBoxShow(183,0);
            } else{
                msgBoxShow(169,0);
            }
        });
    } else {
        msgBoxShow(200, 1);
    }
},

embedCodeWin :function(url, type, ht) {
    var topHeader = WtfGlobal.getLocaleText('pm.common.embed')+' '+type;
    var topDesc = WtfGlobal.getLocaleText({key:'pm.project.plan.reports.export.embed.subheader',params:type});
    if(!ht)
        ht = 600;
    new Wtf.Window({
        title : WtfGlobal.getLocaleText('pm.project.plan.embed'),
        id : "embedprojplan",
        modal : true,
        iconCls : 'iconwin',
        width : 450,
        height: 300,
        resizable :false,
        buttonAlign : 'right',
        buttons :[{
            text: WtfGlobal.getLocaleText('lang.close.text'),
            scope: this,
            handler: function(){
                Wtf.getCmp("embedprojplan").close();
            }
        }],
        layout : 'border',
        items :[{
            region : 'north',
            height : 75,
            border : false,
            bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(topHeader, topDesc, "../../images/embedexport40_52.gif")
        },{
            region : 'center',
            border : false,
            bodyStyle : 'background:#f1f1f1;font-size:10px;',
            layout : 'fit',
            items :[new Wtf.form.FormPanel({
                border : false,
                labelWidth: 70,
                bodyStyle : 'margin-top:20px;margin-left:20px;font-size:10px;',
                defaults: {
                    width: 320,
                    readOnly:'true',
                    selectOnFocus : true
                },
                defaultType: 'textfield',
                items: [{
                    fieldLabel: WtfGlobal.getLocaleText('pm.common.url'),
                    value : url
                },{
                    fieldLabel: WtfGlobal.getLocaleText('pm.common.embed'),
                    xtype :'textarea',
                    height : 90,
                    value : '<iframe src=\"'+url+'\" style="border: 0" width="800" height="'+ht+'" frameborder="0" scrolling="no"></iframe>'
                }
                ]
            })]
        }]
    }).show();
},

calSync: function() {
    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.347'), function(btn) {
        if(btn == "no") {
            return;

        } else if(btn == "yes") {
            Wtf.Ajax.requestEx({
                method:'POST',
                url:Wtf.req.prj + 'task.jsp',
                params:{
                    seq: "9",
                    pid: this.pid
                }
            },
            this,
            function(result, req) {
                msgBoxShow(175, 0);
            },
            function() {
                msgBoxShow(176, 1);
            });
        }
    }, this);
},

refreshContainer: function(){
    this.toggleFeatureBtns(); //for reapplying features after refresh
    var plan = this.editGrid;
    this.refreshOnActivate = false;
    showMainLoadMask(WtfGlobal.getLocaleText('pm.refreshingdata.text'));
    plan.getView().emptyText = '';
    plan.dstore.removeAll();
    this.cControl.clearChart();
    this.searchTask.setValue("");
    plan.getProjectMinMaxDate();
    if(this.isViewMode){
        plan.getView().refresh(true);
    }
},

togglePlanViewMode: function(btn, isViewMode){
    this.isViewMode = isViewMode;
    var plan = this.editGrid;
    var planStore = plan.getStore();
    var cm = plan.getColumnModel();
    var colNo = cm.getIndexById('priority');
    if(isViewMode){
        Wtf.getCmp(this.pid + 'chartRegion').hide();
        Wtf.getCmp(this.pid + 'gridRegion').setWidth(this.getEl().getWidth());
        cm.defaultSortable = true;
        cm.setHidden(colNo, false); // priority column shown
        plan.autoScroll = true;
        plan.enableHdMenu = true;
        plan.enableColumnHide = false;
        plan.getView().forceFit = true;
        planStore.remove(planStore.getAt(planStore.getCount()-1));
        plan.getView().renderUI();
        plan.getView().refresh(true);
        plan.getView().emptyText = '<div class="emptyGridText">'+WtfGlobal.getLocaleText('pm.project.task.emtytext')+'</div>';
        this.doLayout();
        plan.addListener('headerclick', this.sortDuration, this);
        this.manageToolbarButtons(isViewMode);
        this.toggleFeatureBtns();
    } else {
        var chartComp = Wtf.getCmp(this.pid + 'chartRegion');
        Wtf.getCmp(this.pid + 'gridRegion').setWidth((this.getEl().getWidth() - chartComp.lastSize.width) - 5);
        chartComp.setWidth(chartComp.lastSize.width - 5);
        chartComp.show();
        this.renderEditMode();
        plan.getView().renderUI();
        plan.getView().refresh(true);
        var scrollerDom = plan.getView().scroller.dom;
        this.scrollerDom = scrollerDom;
        scrollerDom.style.overflowX = "auto";
        scrollerDom.style.overflowY = "hidden";
        this.refreshContainer();
        this.templateMenu.setDisabled(isViewMode);
        this.sCombo.setDisabled(isViewMode);
        Wtf.getCmp('projPlanStartDate').setDisabled(isViewMode);
        Wtf.getCmp(this.pid +'projPlanImport').setDisabled(isViewMode);
        Wtf.getCmp(this.pid+'refresh').setDisabled(isViewMode);
    }
},

sortDuration: function(grid, ci, e){
    var planStore = grid.getStore();
    if(ci == 4){
        tempGrid = grid;
        var itemArr = planStore.data.items;
        if(planStore.getSortState().direction == 'DESC'){
            itemArr.sort(function(a,b){
                var dur1 = tempGrid.getvalidDuration(a.data.duration);
                var dur2 = tempGrid.getvalidDuration(b.data.duration);
                dur1 = parseInt(dur1);
                dur2 = parseInt(dur2);
                return dur1 - dur2;
            });
        } else {
            itemArr.sort(function(a,b){
                var dur1 = tempGrid.getvalidDuration(a.data.duration);
                var dur2 = tempGrid.getvalidDuration(b.data.duration);
                dur1 = parseInt(dur1);
                dur2 = parseInt(dur2);
                return dur2 - dur1;
            });
        }
        planStore.removeAll();
        planStore.insert(0, itemArr);
    }
},

renderEditMode: function(){
    var plan = this.editGrid;
    var cm = plan.getColumnModel();
    var colNo = cm.getIndexById('priority');
    cm.setHidden(colNo, true);
    cm.defaultSortable = false;
    plan.getView().forceFit = false;
    plan.enableHdMenu = false;
    plan.enableColumnHide = true;
    plan.autoScroll = false;
    var scrollerDom = plan.getView().scroller.dom;
    this.scrollerDom = scrollerDom;
    scrollerDom.style.overflowX = "auto";
    scrollerDom.style.overflowY = "hidden";
    plan.removeListener('headerclick', this.sortDuration, this);
},

manageToolbarButtons: function(isViewMode){
    this.templateMenu.setDisabled(isViewMode);
    this.newTaskButton.setDisabled(isViewMode);
    this.deleteTaskButton.setDisabled(isViewMode);
    this.cutTaskButton.setDisabled(isViewMode);
    this.copyTaskButton.setDisabled(isViewMode);
    this.pasteButton.setDisabled(isViewMode);
    this.outdentButton.setDisabled(isViewMode);
    this.indentButton.setDisabled(isViewMode);
    this.moveUpButton.setDisabled(isViewMode);
    this.moveDownButton.setDisabled(isViewMode);
    this.sCombo.setDisabled(isViewMode);
    this.showTaskProgress.setDisabled(isViewMode);
    this.showOverdue.setDisabled(isViewMode);
    this.showResources.setDisabled(isViewMode);
    this.showPriority.setDisabled(isViewMode);
    this.showTaskPanel.setDisabled(isViewMode);
    Wtf.getCmp('projPlanStartDate').setDisabled(isViewMode);
    Wtf.getCmp(this.pid +'projPlanImport').setDisabled(isViewMode);
    Wtf.getCmp(this.pid+'refresh').setDisabled(isViewMode);
}

,showCPOnGC: function(btn){
    var planStore = this.editGrid.getStore();
    var gc = this.cControl;
    this.editGrid.viewMode = true;
    if(btn.pressed){
        showMainLoadMask(WtfGlobal.getLocaleText('pm.calculating.text')+" "+WtfGlobal.getLocaleText("lang.pleasewait.text")+"...");
        var pertReader = new Wtf.data.KwlJsonReader({
            totalProperty: 'count',
            root: 'data'
        }, Wtf.proj.common.pertRecord);

        this.pertStore = new Wtf.data.Store({
            url: '../../projectCPAController.jsp',
            reader: pertReader,
            method: 'GET'
        });
        this.pertStore.load({
            params: {
                cm: 'getFreeSlackWithoutPERT',
                projectid: this.pid
            }
        });
        gc.dataViewObj.plugins[0].disableDrag(true);
        this.manageToolbarButtons(true);
        this.toggleFeatureBtns();
        planStore.remove(planStore.getAt(planStore.getCount()-1));
        this.pertStore.on('load', this.displaySlack, this);
    } else {
        this.editGrid.viewMode = false;
        this.manageToolbarButtons(false);
        gc.clearSlackPanels();
        gc.dataViewObj.plugins[0].disableDrag(false);
        this.editGrid.addBlankRow();
        this.pertStore.destroy();
    }
},

displaySlack: function(store, recs, params){
    var count = store.getCount();
    for(var i = 0; i < count; i++){
        var rec = store.getAt(i);
        var taskid = rec.get('taskid');
        var tpanel = Wtf.getCmp('Panel'+taskid);
        if(tpanel){
            tpanel.critical = rec.get('iscritical');
            if(rec.get('expected') > 1){
                var taskRecord = this.editGrid.dstore.getAt(this.editGrid.search(taskid));
                var spanel = Wtf.getCmp('Slack'+taskid);
                if(!spanel){
                    spanel = this.cControl.createSlackPanel(rec, tpanel.endDate);
                    spanel.setSlackPanelPositionWidth(taskRecord, tpanel, rec);
                }
            }
            tpanel.setViewMode(true);
            if(tpanel.critical)
                this.cControl.showTaskProgressStatus(tpanel, rec.get('taskStatus'));
        }
    }
    hideMainLoadMask();
}

});
