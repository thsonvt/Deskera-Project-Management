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
function baselineCollapseExpand(div, nid) {
    var baselineId = nid.substr(5);
    Wtf.getCmp(baselineId).baselineRowCollExp(div);
}

Wtf.proj.baselinePanel = function(config) {
    Wtf.apply(this,config);
    this.addEvents({
        'refresh': true
    });
    var baselineCM = new Wtf.grid.ColumnModel([{
            width: 20,
            dataIndex: 'diff',
            hidden: (this.compare) ? false : true,
            renderer: this.diffImg
        },new Wtf.grid.RowNumberer(),{
            width: 20,
            dataIndex: 'notes',
            hidden: (this.compare) ? true : false,
            renderer: this.ImageReturn
        },{
            dataIndex: 'taskid',
            hidden: true,
            header: 'taskid'
        },{
            header: WtfGlobal.getLocaleText('pm.common.taskname'),
            maxLength: 512,
            dataIndex: 'taskname',
            width: 130,
            renderer: this.CustomCell
        },{
            header: WtfGlobal.getLocaleText('pm.common.duration'),
            dataIndex: 'duration',
            width: 50,
            align: 'right',
            renderer: WtfGlobal.formatDuration
        },{
            header: WtfGlobal.getLocaleText('pm.project.calendar.newevent.startdate'),
            dataIndex: 'startdate',
            width: 80,
            renderer: this.formatDate
        },{
            header: WtfGlobal.getLocaleText('pm.common.enddate'),
            dataIndex: 'enddate',
            width: 80,
            renderer: this.formatDate
        },{
            header: WtfGlobal.getLocaleText('pm.project.plan.task.attributes.progress'),
            dataIndex: 'percentcomplete',
            width: 85,
            renderer: function(val, a, rec, b){
                if(rec.data["duration"] != "" && rec.data["duration"] !== undefined) {
                    if(val == "" || val === undefined)
                        val = 0;
                } else
                    val = "";
                return val;
            }
        },{
            header: WtfGlobal.getLocaleText('pm.project.plan.info.predecessors'),
            dataIndex: 'predecessor',
            width: 70,
            hidden: (this.compare) ? true : false,
            renderer: WtfGlobal.HTMLStripper
        },{
            header: WtfGlobal.getLocaleText('pm.project.plan.task.attributes.resourcenames'),
            dataIndex: 'resourcename',
            hidden: (this.compare) ? true : false,
            width: 130
    }]);

    if (!this.compare) {
        var grpView = new Wtf.grid.GroupingView({
            forceFit: true,
            showGroupName: false,
            enableGroupingMenu: false,
            hideGroupedColumn: true,
            emptyText: WtfGlobal.getLocaleText('pm.common.nodatapresent')
        });
        var jrecord = Wtf.data.Record.create([
            {
                name: "resourceid",
                mapping: 'resourceID'
            },{
                name: "resourcename",
                mapping: 'resourceName'
            },{
                name: "colorcode",
                mapping: 'colorCode'
            },{
                name: "stdrate",
                type: "float",
                mapping: 'stdRate'
            },{
                name: "typename",
                mapping: 'type.typeName'
            },{
                name: "typeid",
                type: "int",
                mapping: 'type.typeID'
            },{
                name: "categoryname",
                mapping: 'category.categoryName'
            },{
                name: "categoryid",
                type: "int",
                mapping: 'category.categoryID'
            },{
                name: "wuvalue",
                type: "int",
                mapping: 'wuvalue'
            },{
                name: "billable",
                type: "boolean",
                mapping: 'billable'
            },{
                name: 'inuseflag',
                type: 'boolean',
                mapping: 'inUseFlag'
            }
        ]);
        var jReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: 'count'
        }, jrecord);

        var grpStore = new Wtf.data.GroupingStore({
            url: '../../jspfiles/project/resources.jsp',
            reader: jReader,
            baseParams: {
                action: 18,
                baselineid: this.baselineData.baselineid
            },
            sortInfo: {
                field: 'typename',
                direction: "DESC"
            }
        });
        grpStore.on('load', function(store){
            store.groupBy("typename");
        }, this);
        grpStore.load();
        
        var resCM = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer()
            ,{
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
                dataIndex: "stdrate",
                renderer: function(val, metadata, rec, ri, ci, store){
                    if(rec.data['typeid'] == Wtf.proj.resources.type.WORK)
                        val = val + ' '+Wtf.CurrencySymbol+WtfGlobal.getLocaleText('lang.perhour.text');
                    else if(rec.data['typeid'] == Wtf.proj.resources.type.MATERIAL)
                        val = val + ' '+Wtf.CurrencySymbol+WtfGlobal.getLocaleText('lang.perunit.text');
                    else
                        val = Wtf.CurrencySymbol + ' ' + val;
                    return val;
                }
            },{
                header: WtfGlobal.getLocaleText('pm.common.units.text'),
                autoWidth: true,
                dataIndex: "wuvalue",
                renderer: function(val, metadata, rec, ri, ci, store){
                    if(rec.data['typeid'] == Wtf.proj.resources.type.WORK)
                        val = val + '%';
                    else if(rec.data['typeid'] == Wtf.proj.resources.type.MATERIAL)
                        val = val + ' '+WtfGlobal.getLocaleText('lang.units.text');
                    else 
                        val = val + '%';
                    return val;
                }
            },{
                header: WtfGlobal.getLocaleText('pm.resource.billable'),
                autoWidth: true,
                renderer: this.renderBillableValue,
                dataIndex: "billable"
            }
        ]);
        
        var baselineResourcesGrid = new Wtf.grid.GridPanel({
            region: 'south',
            cm : resCM,
            ds : grpStore,
            id : this.id + "baselineResourcesGrid",
            layout : "fit",
            trackMouseOver : false,
            disableSelection : true,
            autoScroll: true,
            title : WtfGlobal.getLocaleText('pm.project.resources.baseline'),
            height: 300,
            view: grpView
        });
    }

    this.baselineStore.on("load", this.onBaselineGridDataLoad, this);
    if(!this.compare)
        this.baselineStore.load();



    this.baselineGrid = new Wtf.grid.GridPanel({
        region: 'center',
        cm : baselineCM,
        ds : this.baselineStore,
        id : this.id + "baselineGrid",
        layout : "fit",
        autoScroll: true,
        header: !this.compare ? true : false,
        trackMouseOver : false,
        disableSelection : true,
        viewConfig : {
            forceFit : true,
            emptyText : WtfGlobal.getLocaleText('pm.common.nodata')
        }
    });
    Wtf.proj.baselinePanel.superclass.constructor.call(this, {
        layout: "fit",
        title: this.title,
        closable: this.closable,
        tabType: this.tabType,
        iconCls: this.iconCls,
        id: this.id,
        border: false,
        autoDestroy: true,
        items: [{
            id: this.id + 'gridRegion',
            border: false,
            autoScroll: true,
            layout: 'border',
            items : [this.baselineGrid]
        }]
    });

    if(this.compareElement == 'project'){
        this.tools = [{
            id: 'refresh',
            scope: this,
            qtip: WtfGlobal.getLocaleText('pm.project.plan.baseline.compare.refresh'),
            handler: function(e, toolEl, panel){
                this.fireEvent('refresh');
            }
        }]
    }
    
    var comp = Wtf.getCmp(this.id + 'gridRegion');
    if(comp && !this.compare){
        comp.add(baselineResourcesGrid);
        this.baselineGrid.setTitle(WtfGlobal.getLocaleText('pm.project.plan.baseline.tasks'));
        this.doLayout();
    }
}   

Wtf.extend(Wtf.proj.baselinePanel, Wtf.Panel, {

    formatDate : function(value) {
        return value ? value.dateFormat(WtfGlobal.getOnlyDateFormat()) : '';
    },

    diffImg: function(data){
        var img = "";
        if(data == "add")
            img = 'compare_add.png';
        else if(data == "remove")
            img = "compare_delete.png";
        else if(data == "update")
            img = "compare_update.png";
        return "<img height=13px width=14px src = '../../images/" + img + "'>";
    },
 
    CustomCell : function(text) {
        text = WtfGlobal.HTMLStripper(text);
        return '<div id="img_div" class="minus"> </div><div id="txtDiv" class="defaulttext" wtf:qtip="' + text + '" wtf:qtitle='+WtfGlobal.getLocaleText('pm.common.task')+'>' + text + '</div>';
    },

    ImageReturn : function(data) {
        if (data) {
            data = data.replace(/!NL!/g,"\n");
            data = data.replace(/\n/g,"<br>");
                return "<img id='TaskNotes' style='height:12px; width:12px;' wtf:qtitle='"+WtfGlobal.getLocaleText('pm.project.plan.task.attributes.notes')+"' wtf:qtip=\""+ data +"\" src='../../images/Notes.png'></img>";
            }
    },

    renderBillableValue: function(value){
        if(value)
            return WtfGlobal.getLocaleText('lang.yes.text');
        else return WtfGlobal.getLocaleText('lang.no.text');
    },

    onBaselineGridDataLoad : function(baselineStore, baselineRecord, option) {
        var count = baselineStore.getCount();
        var baselineGrid = Wtf.getCmp(this.id + "baselineGrid");
        for (var cnt = 0; cnt < count; cnt++) {
            var irec = baselineStore.data.items[cnt];
            var baselineGridView = baselineGrid.getView();
            if(baselineStore.data.items[cnt].data['parent'] != '0') {
                var parentRow = baselineStore.find("taskid", irec.data['parent']);
                if(parentRow != -1) {
                    var parentRecord = baselineStore.data.items[parentRow];
                    if(parentRecord) {
                        var level = parseInt(parentRecord.data['level']) + 1;
                        irec.data['level'] = level;
                        baselineGridView.getCell(cnt, 4).firstChild.style.marginLeft = ((level) * 20) + 'px';
                    }

                } else {
                    irec.data['parent'] = "0";
                    irec.data['level'] = 0;
                }

            } else
                irec.data['level'] = 0;

            if(irec.data.isparent) {
                baselineGridView.getCell(cnt, 4).firstChild.firstChild.className = 'Dminus';
                baselineGridView.getCell(cnt, 4).firstChild.lastChild.className = 'imgtext';
                Wtf.get(baselineGridView.getRow(cnt)).addClass('parentRow');

            } else {
                baselineGridView.getCell(cnt, 4).firstChild.firstChild.className = 'minus';
                baselineGridView.getCell(cnt, 4).firstChild.lastChild.className = 'defaulttext';
            }
        }
    }
});
