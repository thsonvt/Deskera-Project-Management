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

Wtf.TeamProjectView = function(config){
    Wtf.TeamProjectView.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.TeamProjectView, Wtf.Panel,{
    onRender : function(config){
        Wtf.TeamProjectView.superclass.onRender.call(this,config);
        this.jrecord = Wtf.data.Record.create([ 
        {
            name: "resourceid"
        },{
            name: "resourcename"
        },{
            name: "stdrate",
            type: "float"
        },{
            name: "typename"
        },{
            name: "typeid"
        },{
            name: "categoryname"
        },{
            name: "categoryid"
        },{
            name: "wuvalue"
        },{
            name: "billable",
            type: "boolean"
        },{
            name: 'inuseflag',
            type: 'boolean'
        },{
            name: 'teamname'
        },{
            name: 'projectname'
        }
        ]);
        var jReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: 'count'
        },this.jrecord); 

        this.grpStore = new Wtf.data.GroupingStore({
            url: '../../jspfiles/project/resources.jsp',
            reader: jReader,
            baseParams: {
                action: 16,
                companyid: companyid
            },
            sortInfo: {
                field: 'typename',
                direction: "DESC"
            }
        });
        if(this.loadStore){
            this.grpStore.load({
                scope: this,
                callback: function(rec, opt, succ){
                    if(succ){
                        this.grpStore.groupBy("typename");
                    }
                }
            });
        }
        var resSM = new Wtf.grid.CheckboxSelectionModel();
        var resCM = new Wtf.grid.ColumnModel([
            resSM,
            {
                header: WtfGlobal.getLocaleText('pm.project.resource.name'),
                autoWidth: true,
                dataIndex: "resourcename"
            },{
                hidden: true,
                autoWidth: true,
                dataIndex: "resourceid"
            },{
                hidden: true,
                autoWidth: true,
                dataIndex: "typename"
            },{
                header: WtfGlobal.getLocaleText('lang.category.text'),
                autoWidth: true,
                dataIndex: "categoryname"
            },{
                header: WtfGlobal.getLocaleText('lang.cost.text'),
                autoWidth: true,
                dataIndex: "stdrate"
            },{
                header: WtfGlobal.getLocaleText('pm.common.units.text'),
                autoWidth: true,
                dataIndex: "wuvalue"
            },{
                header: WtfGlobal.getLocaleText('pm.resource.billable'),
                autoWidth: true,
                renderer: this.renderBillableValue,
                dataIndex: "billable"
            },{
                header: WtfGlobal.getLocaleText('pm.project.teamname'),
                autoWidth: true,
                dataIndex: 'teamname'
            }]);

        var grpView = new Wtf.grid.GroupingView({
            forceFit: true,
            showGroupName: false,
            enableGroupingMenu: false,
            hideGroupedColumn: false    
        });

        this.resGrid = new Wtf.grid.GridPanel({
            store: this.grpStore,
            id: this.id+'_grid',
            title: (this.gridTitle) ? this.gridTitle : '',
            collapsible: (this.collapseIt) ? true : false,
            autoScroll:true,
            loadMask: true,
            height: (this.height == null) ? 430 : this.height,
            border:false,
            view: grpView,
            emptyText: '<div class="emptyGridText">'+WtfGlobal.getLocaleText('pm.common.nodatadisplay')+'</div>',
            sm: resSM,
            cm: resCM
        });

        this.add(this.resGrid);

        resSM.on("rowselect", function(sm, row, rec){
            var selected = sm.getSelections();
            this.selected = selected;
        }, this);
        
    }, 
    renderBillableValue: function(value){
        if(value)
            return WtfGlobal.getLocaleText('lang.yes.text');
        else return WtfGlobal.getLocaleText('lang.no.text');
    },

    getCurrStore: function(){
        return this.grpStore;
    }

});

//------------------ Step 1 grid Cmp Complete -----------------------------------
//------------------ Step 2 form cmp for create team -----------------------------------
Wtf.createTeam = function(config){
    Wtf.createTeam.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.createTeam, Wtf.Panel,{
    initComponent: function(config){
        Wtf.createTeam.superclass.initComponent.call(this,config);
        this.teamForm = new Wtf.Panel({
            title: WtfGlobal.getLocaleText('pm.project.team.create'),
            layout: 'form',
            id: this.id + '_createTeamForm',
            border: true,
            layoutConfig:{
                labelAlign: 'left',
                labelWidth: 150
            },
            bodyStyle: 'padding:10px;background-color:white;',
            autoHeight: true,
            width: 400, 
            items:[{
                xtype: 'textfield',
                fieldLabel: WtfGlobal.getLocaleText("pm.project.teamname")+'*',
                width: 150,
                emptyText: WtfGlobal.getLocaleText('lang.name.providetext'),
                maxLength: 150,
                allowBlank: false
            },{
                xtype: 'textarea',
                fieldLabel: WtfGlobal.getLocaleText('lang.description.text'),
                width: 150,
                height: 50,
                emptyText: WtfGlobal.getLocaleText('pm.common.providedesc'),
                maxLength: 500,
                allowBlank: true
            }]
        });

        this.selectionGrid = new Wtf.TeamProjectView({
            layout: 'fit',
            id: this.id+'_selectionGrid',
            projid: companyid,
            height: 300,
            gridTitle: 'Selected Team Members',
            collapseIt: true,
            topbar: false,
            loadStore: false
        });

        this.innerPanel = new Wtf.Panel({
            layout: 'fit',
            id: this.id + '_innerPanel',
            border: false,
            frame: false,
            plain: true,
            autoHeight: true
        });

        this.innerPanel.add(this.teamForm);
        this.innerPanel.add(this.selectionGrid);
        this.add(this.innerPanel);
    }

});

//--------------------------- Step 2 Complete -------------------------------
//--------------------------- Step 3 assign project to this team -----------------

Wtf.assignTeam = function(config){
    Wtf.assignTeam.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.assignTeam, Wtf.Panel,{
    initComponent: function(config){
        Wtf.assignTeam.superclass.initComponent.call(this,config);
        this.projRec = new Wtf.data.Record.create([
            {
                name: 'projectid'
            },{
                name: 'projectname'
            },{
                name: 'description'
        }]);

        this.dataReader = new Wtf.data.KwlJsonReader({
            root: "data"
        },this.projRec);

        this.projds = new Wtf.data.Store({
            id : this.id + '_projds',
            url: '../../jspfiles/teamControl.jsp?action=3&projectid='+this.projectid+'&companyid='+companyid,
            reader: this.dataReader,
            sortInfo: {
                field: 'projectname',
                direction: "DESC"
            }
        });

        this.selectionModel = new Wtf.grid.CheckboxSelectionModel();
        this.cm= new Wtf.grid.ColumnModel([this.selectionModel,{
            header: WtfGlobal.getLocaleText('pm.project.text'),
            dataIndex: 'projectname',
            autoWidth : true,
            sortable: true
        },{
            header :WtfGlobal.getLocaleText('lang.description.text'),
            dataIndex: 'description',
//            hidden: (!this.requestfromdashboard) ? false : true,
            autoSize : true,
            sortable: true
        }]);

        this.projectsgrid = new Wtf.grid.GridPanel({
            id:this.id + '_projectGrid',
            store: this.projds,
            cm: this.cm,
            sm : this.selectionModel,
            border : false,
            height: 375,
            autoScroll: true,
            loadMask : true,
//            title: (this.requestfromdashboard) ? 'Select Projects' : '',
            viewConfig: {
                forceFit:true
            }
        });

        this.innerPanel  = new Wtf.common.KWLListPanel({
            id : this.id + '_innerPanel',
            title: WtfGlobal.getLocaleText("pm.project.team.step3.subheader"),
            autoLoad : false,
            paging : false,
            height: '100%',
            layout : 'fit',
            items: [ this.projectsgrid ]
        });
        if(!this.requestfromdashboard)
            this.projds.load();
        this.add(this.innerPanel);
    }
});


function teamProjectWizard(id){
    var teamid = "", showStep = "";
    var teamname = "";
    var resids = "";
    var teamProjectView = new Wtf.TeamProjectView({
        layout: 'fit',
        id:'teamProjectView',
        border: false,
        projid: companyid,
        topbar: true,
        loadStore: true,
        selModel: true,
        gridTitle: WtfGlobal.getLocaleText({key:"pm.project.team.allresources", params:companyName})
    });

    var step1 = new Wtf.common.Wizard.Step({
        title: WtfGlobal.getLocaleText('pm.project.resource.selectteam'),
        defaults: {
            labelStyle: 'font-size:11px'
        },
        items: [ teamProjectView ]
    });

    var tPanel = new Wtf.Panel({
        border: false,
        width: '100%',
        autoScroll: true,
        height: 430,
        layout: 'fit',
        trackMouseOver: true
    });

    var step2 = new Wtf.common.Wizard.Step({
        title: WtfGlobal.getLocaleText('pm.project.team.createselected'),
        enableFinalButton: true,
        defaults: {
            labelStyle: 'font-size:11px'
        },
        items: [ tPanel ]
    });
    var assignPanel = new Wtf.Panel({
        border: false,
        width: '100%',
        autoScroll: (Wtf.isIE7) ? false : true,
        height: 430,
        layout: 'fit',
        trackMouseOver: true
    });
    var step3 = new Wtf.common.Wizard.Step({
        title: WtfGlobal.getLocaleText('pm.projects.team.assign'),
        finalCard: true,
        items: [ assignPanel ]
    });
	
    step2.on("show", function(){
        if(showStep == true){
            if(tPanel.items) {
                for(var cnt = 0; cnt < tPanel.items.items.length; cnt++){
                    tPanel.remove(tPanel.items.items[cnt], true);
                }
            }
            var createteam = new Wtf.createTeam({
                border: true,
                width: '100%',
                trackMouseOver: true,
                id: 'createTeamComp',
                height: 430
            });
            tPanel.add(createteam);
            tPanel.doLayout();
            var orig = Wtf.getCmp('teamProjectView');
            var t = Wtf.getCmp('createTeamComp_selectionGrid_grid');
            var select = orig.resGrid.getSelectionModel().getSelections();
            t.getStore().removeAll();
            if(select.length != 0){
                for(var i = 0; i < select.length; i++){
                    var reco = new orig.jrecord({
                        resourceid: select[i].get('resourceid'),
                        resourcename: select[i].get('resourcename'),
                        billable: select[i].get('billable'),
                        stdrate: select[i].get('stdrate'),
                        typename: select[i].get('typename'),
                        typeid: select[i].get('typeid'),
                        categoryname: select[i].get('categoryname'),
                        categoryid: select[i].get('categoryid'),
                        inuseflag: select[i].get('inuseflag'),
                        wuvalue: select[i].get('wuvalue'),
                        teamname: select[i].get('teamname'),
                        projectname: select[i].get('projectname')
                    });
                    t.getStore().insert(i, reco);
                }
            }
        }
    }, this);

    step3.on("show", function(){
        if(showStep == true){
            var formcomp = Wtf.getCmp('createTeamComp_createTeamForm');
            var t = Wtf.getCmp('createTeamComp_selectionGrid_grid');

            for(var i = 0; i < t.getStore().data.items.length; i++){
                resids += t.getStore().data.items[i].data['resourceid'] + ",";
            }
            resids = resids.substr(0,resids.length-1);
            teamname = formcomp.items.items[0].getValue().trim();
            var teamdesc = formcomp.items.items[1].getValue().trim();
            Wtf.Ajax.requestEx({
                url : "../../jspfiles/teamControl.jsp",
                params : {
                    action : 1,
                    teamname: teamname,
                    desc : teamdesc,
                    resids: resids,
                    projectid: id
                },
                method : 'POST'
            },
            this,
            function(request, response) {
                var obj = eval("(" + request + ")");
                if(obj.success) {
                    teamid = obj.data;
                    msgBoxShow(251);
                }
            },
            function() {
                msgBoxShow(4, 1);
                wizard.disableFinalButton();
                wizard.disableNextButton();
            });
            if(assignPanel.items) {
                for(var cnt = 0; cnt < assignPanel.items.items.length; cnt++){
                    assignPanel.remove(assignPanel.items.items[cnt], true);
                }
            }
            var assignteam = new Wtf.assignTeam({
                border: true,
                width: '100%',
                trackMouseOver: true,
                id: 'assignTeamComp',
                height: 430,
                title: WtfGlobal.getLocaleText('pm.project.team.assign'),
                projectid: id,
                requestfromdashboard: false
            });
            assignPanel.add(assignteam);
            assignPanel.doLayout();
        } else {

        }
    }, this);
    
    var wizard = new Wtf.common.Wizard({
        title: WtfGlobal.getLocaleText('pm.dashboard.widget.quicklinks.teamprojectrelationship'),
        id:'teamprojectwizard',
        headerConfig: {
            title: WtfGlobal.getLocaleText('pm.dashboard.widget.quicklinks.teamprojectrelationship')
        },
        stepPanelConfig: {
            defaults: {
                border: false
            }
        },
        steps: [step1, step2, step3]
    });
    wizard.show();
    wizard.on('nextButtonClicked', function(currStep){
        showStep = false;
        if(currStep == 1){
            var formcomp = Wtf.getCmp('createTeamComp_createTeamForm');
            teamname = formcomp.items.items[0].getValue().trim();
            if(teamname == ""){
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.351'), function(btn){
                    if (btn == "yes") {
                        wizard.close();
                    } else {
                        showStep = true;
                    }
                });
            } else {
                showStep = true;
            }
        } else {
            var orig = Wtf.getCmp('teamProjectView');
            var select = orig.resGrid.getSelectionModel().getSelections();
            if(select.length < 1){
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.344'), function(btn){
                    if (btn == "yes") {
                        wizard.close();
                    } else {
                        showStep = true;
                    }
                });
            } else {
                showStep = true;
            }
        }
        if(showStep == true)
            return true;
        else
            return false;
    }, this);

    wizard.on("finish", function(){
        var ret = false;
        if(wizard.currentStep != 1){
            var grid = Wtf.getCmp('assignTeamComp_projectGrid');
            var select = grid.getSelectionModel().getSelections();
            resids = "";
            for(var i = 0; i < select.length; i++){
                resids += select[i].data['projectid'] + ",";
            }
            resids = resids.substr(0,resids.length-1);
            if(select.length > 0){
                Wtf.Ajax.requestEx({
                    url : "../../jspfiles/teamControl.jsp",
                    params : {
                        action : 2,
                        teamid: teamid,
                        projectid: id,
                        projectids: resids
                    },
                    method : 'POST'
                },
                this,
                function(request, response) {
                    var obj = eval("(" + request + ")");
                    if(obj.success) {
                        mainPanel.loadMask.hide();
                        msgBoxShow(318);
                        bHasChanged = true;
                        wizard.close();
                        ret = true;
                    }
                },
                function() {
                    msgBoxShow(4, 1);
                    ret = false;
                });
            } else {
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText({key:"pm.project.team.step3.prompt", params:teamname}), function(btn){
                    if (btn == "yes") {
                        ret = true;
                        wizard.close();
                    } else {
                        ret = false;
                    }
                });
            }
        } else {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.329'), function(btn){
                if (btn == "yes") {
                    ret = true;
                    wizard.close();
                } else {
                    ret = false;
                }
            });
        }
        if(ret == true)
            wizard.close();
        return ret;
    }, this);
    wizard.on("afterStepShow",  function(card){
        if(card.enableFinalButton) {
            wizard.enableFinalButton();
            wizard.setNextButtonText(WtfGlobal.getLocaleText("lang.Next.text")+" &gt;");
        }
        else {
            wizard.disableFinalButton();
            wizard.setNextButtonText(WtfGlobal.getLocaleText("lang.Next.text")+ " &gt;");
        }
        if(card.finalCard) {
            wizard.disableNextButton();
            wizard.enableFinalButton();
            wizard.disablePreviousButton();
        }
    }, this);
//	wizard.disableNextButton();
}
