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
Wtf.selectProject = function(config){
    Wtf.apply(this, config);
    var btnArr = [];
    this.submitBtn = new Wtf.Button({
        text : WtfGlobal.getLocaleText('lang.done.text'),
        cls: 'adminButton',
        scope : this,
        handler: this.submitHandler
    });
    btnArr.push(this.submitBtn);

    this.cancelBtn = new Wtf.Button({
        text : WtfGlobal.getLocaleText('lang.cancel.text'),
        cls: 'adminButton',
        scope : this,
        handler: this.cancelHandler
    });
    btnArr.push(this.cancelBtn);
    
    Wtf.selectProject.superclass.constructor.call(this, {
        title : WtfGlobal.getLocaleText('pm.project.addwidget.title'),
        closable : true,
        modal : true,
        iconCls : 'iconwin',
        width: 550,
        height: 430,
        resizable :false,
        buttonAlign : 'right',
        buttons :btnArr,
        layout : 'border',
        id: 'selectProjectWin',
        autoDestroy: true
    });
}

Wtf.extend(Wtf.selectProject, Wtf.Window,{

    initComponent: function(){
        Wtf.selectProject.superclass.initComponent.call(this);
        var topHTML = getTopHtml(WtfGlobal.getLocaleText('pm.project.addwidget.top'),
        WtfGlobal.getLocaleText('pm.dashboard.settings.subheader'),
        "../../images/Add-project-widget.jpg");
        this.projRec = new Wtf.data.Record.create([
            {
                name: 'projectid'
            },{
                name: 'projectname'
            },{
                name: 'widgetselect'
            },{
                name: 'quicklinkselect'
            },{
                name: 'mtselect'
        }]);

        this.dataReader = new Wtf.data.KwlJsonReader({
            root: "data"
        },this.projRec);

        this.projds = new Wtf.data.Store({
            id : this.id + '_projds',
            reader: this.dataReader,
            sortInfo: {
                field: 'projectname',
                direction: "DESC"
            }
        });

        var isWidget = new Wtf.grid.CheckColumn({
           header: WtfGlobal.getLocaleText('pm.dashboard.settings.displaywidget'),
           dataIndex: 'widgetselect',
           width: 42,
           scope: this,
           sortable: false
        });

        var isQuickLink = new Wtf.grid.CheckColumn({
           header: WtfGlobal.getLocaleText('pm.dashboard.settings.displayql'),
           dataIndex: 'quicklinkselect',
           width: 50,
           scope: this,
           sortable: false
        });

        var isMilestoneTimeline = new Wtf.grid.CheckColumn({
           header: WtfGlobal.getLocaleText('pm.milestonetimeline.text'),
           dataIndex: 'mtselect',
           width: 50,
           scope: this,
           sortable: false
        });

        this.cm= new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header: WtfGlobal.getLocaleText('pm.project.text'),
                dataIndex: 'projectname',
                autoWidth : true,
                sortable: false
            },
            isWidget,
            isQuickLink,
            isMilestoneTimeline
        ]);

        this.selectionGrid = new Wtf.grid.GridPanel({
            id:this.id + '_selectionGrid',
            store: this.projds,
            cm: this.cm,
            border : false,
            height: 450,
            autoScroll: true,
            loadMask : true,
            autoExpandColumn: 'projectname',
            viewConfig: {
                forceFit:true
            }
        });

        this.selectionGrid.on('cellclick', this.handleCheckChange, this);
        this.initStore();

        this.add({
            region : 'north',
            height : 105,
            border : false,
            bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
            html: topHTML

        },{
            region : 'center',
            border : false,
            bodyStyle : 'background:#f1f1f1;font-size:10px;',
            layout : 'fit',
            items :this.selectionGrid
        });
        this.doLayout();
    },

    initStore: function(){
        if(projId.length != 0){
            this.qlStatusOld = [];
            this.mtStatusOld = [];
            for(var i = 0; i < projId.length; i++){
                if(projects[i].isQuickLink)
                    this.qlStatusOld[i] = true;
                else
                    this.qlStatusOld[i] = false;
                
                if(projects[i].milestoneStack)
                    this.mtStatusOld[i] = true;
                else
                    this.mtStatusOld[i] = false;

                var reco = new this.projRec({
                    projectid: projId[i],
                    projectname: projects[i].name,
                    widgetselect: (isToAppendArray[i] == 1) ? true : false,
                    quicklinkselect: this.qlStatusOld[i],
                    mtselect: this.mtStatusOld[i]
                });
                this.projds.insert(i, reco);
            }
        }
    },

    cancelHandler: function(){
        this.close();
    },
    
    submitHandler: function(btn){
        btn.disable();
        this.cancelBtn.disable();
        var store = this.projds;
        for(var i = 0; i < projId.length; i++){
            var newData = store.getAt(i);
            var projectid = projId[i];
            var isWid = newData.get('widgetselect');
            var index=widgetIdArray.indexOf(projectid+'_drag');
            if(isWid && isToAppendArray[index] !== 1){
                addProjectWidget(projectid, 'true');
                if(Wtf.getCmp('tab'+projectid)){
                    Wtf.get(projectid+'_addSpan').dom.style.display = 'none';
                    Wtf.get(projectid+'_removeSpan').dom.style.display = 'block';
                }
            } else if(!isWid && isToAppendArray[index] == 1){
                addProjectWidget(projectid, 'false');
                if(Wtf.getCmp('tab'+projectid)){
                    Wtf.get(projectid+'_removeSpan').dom.style.display = 'none';
                    Wtf.get(projectid+'_addSpan').dom.style.display = 'block';
                }
            }
        }
        var qlobj = new Object();
        var mtobj = new Object();
        for(i = 0; i < store.getCount(); i++){
            newData = store.getAt(i);
            var isQl = newData.get('quicklinkselect');
            var isMt = newData.get('mtselect');
            projectid = newData.get('projectid');
            if(this.qlStatusOld[i] !== isQl){
                qlobj[projectid] = isQl;
                if(Wtf.getCmp('tab'+projectid)){
                    if(isQl){
                        Wtf.get(projectid+'_addQlSpan').dom.style.display = 'none';
                        Wtf.get(projectid+'_removeQlSpan').dom.style.display = 'block';
                    } else {
                        Wtf.get(projectid+'_addQlSpan').dom.style.display = 'block';
                        Wtf.get(projectid+'_removeQlSpan').dom.style.display = 'none';
                    }
                }
            }
            if(this.mtStatusOld[i] !== isMt){
                mtobj[projectid] = isMt;
                if(Wtf.getCmp('tab'+projectid)){
                    if(isMt){
                        Wtf.get(projectid+'_addMtSpan').dom.style.display = 'none';
                        Wtf.get(projectid+'_removeMtSpan').dom.style.display = 'block';
                    } else {
                        Wtf.get(projectid+'_addMtSpan').dom.style.display = 'block';
                        Wtf.get(projectid+'_removeMtSpan').dom.style.display = 'none';
                    }
                }
            }
        }
        mtobj = Wtf.encode(mtobj);
        qlobj = Wtf.encode(qlobj);
        if(qlobj.length > 2)
            addProjectQl(qlobj, this);
        if(mtobj.length > 2)
            addMilestoneTimeline(mtobj, qlobj, this);
        btn.enable();
        this.cancelBtn.enable();
        this.close();
    },

    handleCheckChange: function(grid, r, c, e){
        if(e.target.tagName == 'IMG'){
            var rec = this.projds.getAt(r);
            var fieldName = grid.getColumnModel().getDataIndex(c);
            var classname = e.target.className;
            var ele = Wtf.get(e.target.id);
            if(classname.indexOf('x-grid3-check-col-on') !== -1){
                ele.replaceClass('x-grid3-check-col-on', 'x-grid3-check-col');
                rec.data[fieldName] = false;
            } else {
                ele.replaceClass('x-grid3-check-col', 'x-grid3-check-col-on');
                rec.data[fieldName] = true;
            }
        }
    }
    
});

function addProjectQl(qlobj, scope){
    Wtf.Ajax.requestEx({
        url: Wtf.req.widget + 'widget.jsp',
        method: 'POST',
        params:{
            flag: 102,
            quicklinksstate: qlobj
        }
    }, scope,
    function(resp){
        var obj = eval('('+resp+')');
        if(obj.success){
            msgBoxShow(13, 0);
            collectProjectData();
            Wtf.getCmp('quicklinks_drag').callRequest();
        }
    },
    function(resp){
        var obj = eval('('+resp+')');
        if(!obj.success){
            msgBoxShow(4, 1);
            if(scope.submitBtn)
                scope.submitBtn.enable();
            if(scope.cancelBtn)
                scope.cancelBtn.enable();
        }
    });
}

function addMilestoneTimeline(mtobj, qlobj, scope){
    Wtf.Ajax.requestEx({
        url: Wtf.req.widget + 'widget.jsp',
        method: 'POST',
        params:{
            flag: 104,
            mtstate: mtobj
        }
    }, scope,
    function(resp){
        var obj = eval('('+resp+')');
        if(obj.success){
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText('lang.success.text'),
                msg: WtfGlobal.getLocaleText('pm.msg.13'),
                buttons: Wtf.MessageBox.OK,
                icon: Wtf.MessageBox.INFO,
                fn: function(){
                    bHasChanged=true;
                    if(refreshDash.join().indexOf("all") == -1)
                        refreshDash[refreshDash.length] = 'all';
                    if(scope.baseCls == 'x-window'){
                        var dash = Wtf.getCmp('tabdashboard');
                        refreshDashboard(dash);
                    }
                }
            });
        } else {
            msgBoxShow(219, 1);
            if(scope.baseCls == 'x-window')
                scope.close();
        }
    },
    function(resp){
        msgBoxShow(219, 1);
    });
}

Wtf.grid.CheckColumn = function(config){
    Wtf.apply(this, config);
    if(!this.id){
        this.id = Wtf.id();
    }
    this.renderer = this.renderer.createDelegate(this);
};

Wtf.grid.CheckColumn.prototype ={
    init : function(grid){
        this.grid = grid;
    },
    renderer : function(v, p, record){
        p.css += ' x-grid3-check-col-td';
        return '<img src="../../images/s.gif" class="check_col_img_autowidth x-grid3-check-col'+(v?'-on':'')+' x-grid3-cc-'+this.id+'">&#160;</img>';
    }
};
