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

/**
 * js for custom column
 @author kamlesh
*/

//global record function for custom column meta data
Wtf.cusColRecord = Wtf.data.Record.create([
{
    name: "columnId",
    type:"string"
},{
    name: "header",
    type:"string"
},{
    name: "type",
    type:"string"
},{
    name:"module",
    type:"string"
},{
    name:"name",
    type:"string"
},{
    name:"visible"
},{
    name:"enabled"
},{
    name:"mandatory",
    type:"string"
},{
    name:'masterdata'
},{
    name:'dataIndex',
    type:"string"
},{
    name:'no',
    type:'string'
},{
    name:'mandatory'
},{
    name:'defaultValue'
},{
    name:"renderer",
    type:"string"
},{
    name:"editor",
    type:"string"
},{
    name:"creator",
    type:"string"
},{
    name:"createdDate" //type:"date"//, dateFormat:'Y-m-d H:i:s'
}
]);

/** renderer for custom column */
var milestoneQtipTpl = new Wtf.XTemplate('<tpl for="items"><span class="qTipHeader">{h}: </span><span class="{c}">{d}</span><br></tpl>');

Wtf.cc.renderer ={
    date: function(value){
        var temp="";
        if(value!=undefined  && value!=null && value!=""){
            if(!isNaN(value))
                value = new Date(value);
            else if(typeof StringValue)
                value = Date.parseDate(value,"Y-m-d");
            
            temp = value.format(WtfGlobal.getOnlyDateFormat());
        }
        return temp;
    },
    integer: function(val){
        return Math.floor(val);
    },
    decimal: function(val){
        return val //.toFixed(2);
    },
    textfield: function(val){
        return val;
    },
    booleanfield: function(val){
        if(val) return WtfGlobal.getLocaleText('lang.yes.text');
        else return WtfGlobal.getLocaleText('lang.no.text');
    },
    milestone: function(val, meta, rec){
        if(val && val != ""){
            var obj = Wtf.decode(val);
            var milestone = obj;
            var name = milestone.taskname;
            if(name == '')
                name = '<no name>';
            var index = milestone.taskindex;
            var pc = milestone.percentcomplete;
            var dt = milestone.startdate;
            dt = WtfGlobal.dateRendererForOnlyDate(dt);
            var d = {
                items:[{
                    h: WtfGlobal.getLocaleText('pm.project.plan.task.attributes.index'),
                    c: 'tName',
                    d: index
                },{
                    h: WtfGlobal.getLocaleText('lang.date.text'),
                    c: 'tSDate',
                    d: dt
                },{
                    h: WtfGlobal.getLocaleText('pm.project.plan.task.attributes.progress'),
                    c: 'tDuration',
                    d: pc + "%"
                }]
            };
            var tip = '<span class="taskheader msTipName">'+name+'</span><hr>'+milestoneQtipTpl.apply(d);
            meta.attr += "wtf:qtip='"+tip+"'";
            if(milestone.inprogressml){
                meta.css += "inprogressml";
            } else if(milestone.overdueml){
                meta.css += "overdueml";
            } else if(milestone.completedml){
                meta.css += "completedml";
            }
            return name + "<br> <span style='color:#777777;'>" + dt + '</span>';
        } else 
            return "--";
    },
    projectHealth: function(val){
        if(val==1) return "<div><img src='../../images/health_status/ontime.gif' style='vertical-align:text-bottom'/> "+WtfGlobal.getLocaleText('pm.project.home.health.ontime')+"</div>";
        else if(val==2) return "<div><img src='../../images/health_status/slightly.gif' style='vertical-align:text-bottom'/>"+WtfGlobal.getLocaleText('pm.project.home.health.slightly')+"</div>";
        else if(val==3) return "<div><img src='../../images/health_status/gravely.gif' style='vertical-align:text-bottom'/>"+WtfGlobal.getLocaleText('pm.project.home.health.gravely')+"<div>";
        else return "<div><img src='../../images/health_status/else_status.gif' style='vertical-align:text-bottom'/>Future Task</div>";
    }
};


/* constants for Custom Column */

Wtf.cc.columntype = {
    TEXT_FIELD :'4',
    NUMBER_FIELD:'0',
    CHECKBOX :'1',
    DATE_FIELD:'2',
    DROD_DOWN:'3',
    RICH_TEXTBOX:'5',
    TEXT_AREA:'6',
    MULTISELECT_COMBO:'7',
    FILE:'8'
}

/*------------ type of report -----------------*/
Wtf.cc.reprttypes = [[WtfGlobal.getLocaleText('lang.cost.text')],["Progress"],["Custom"]];

/** get the column type name
 * parameter type id */
Wtf.cc.getcolumntype= function(type){
    var _x = ['Number Field','Checkbox','Date Field','Dropdown','Text Field','Rich TextBox','Text Area','MultiSelect Combobox','File Upload'];
    return _x[type];
}
Wtf.cc.getColumnXtype = function(type){
    var _x =["numberfield","checkbox","datefield","combo","textfield","htmleditor","textarea","mscombo","file"];
    return _x[type];
}

// global Wtf store for custom column
Wtf.CusColumnDS = function(config){
    Wtf.apply(this,config);

    Wtf.CusColumnDS.superclass.constructor.call(this,{
        url: "../../customColumn.jsp",
        autoLoad: true,
        baseParams: {
            m:this.mname
        },
        reader: new Wtf.data.KwlJsonReader({
            root: 'data'
        }, Wtf.cusColRecord)
    })
};
Wtf.extend(Wtf.CusColumnDS,Wtf.data.Store);

/**
 *retunrs data indexes in array
 */
function getCCDataIndexes(obj,module){
    var _s = Wtf.cusColumnDS;
    var c = _s.getCount();
    for(var i=0;i<c; i++){
       obj[obj.length]  = _s.getAt(i).get("dataIndex");
    }
    return obj;
}

/**
 * create record at runtime
 * config : config is existing config for record
 *  */
function getStoreFields(config) {
    var store = Wtf.cusColumnDS;
    var _c = store.getCount();
    for(var i = 0; i <_c ; i++) {
        var fObj = {};
        fObj['name'] = store.getAt(i).get("dataIndex");
        config[config.length] = fObj;
    }
    return config;
}
/**
 *create two dimension array for combo data from one dimension array
 *obj ; one dimensional array
 **/

function getCcComboData(obj){
    var _t = [];
    for(var i=0; i<obj.length; i++){
        var _t1 = [obj[i]];  // array of one element requeired for combo
        _t.push(_t1);
    }
    return _t;
}

/**
 *get the column model config with the adding custom column cm config.
 *colconfig : existing congig for column model.
 * module name of the custom column
 * */
function getCcColModel(colConfig,module) {
    var store = Wtf.cusColumnDS;
    var len = colConfig.length;
    var cfg = new Array();
    for(var i=0;i<len; i++){
        cfg.push(colConfig[i]);
    }
    len = store.getCount();
    for(var columncnt =0; columncnt<len; columncnt++) {
        var rec = store.getAt(columncnt);
        if(rec.get("module")==module){
            var colObj = {};
            colObj['header'] = rec.get("header");
            colObj['dataIndex'] = rec.get("dataIndex");
            colObj['sortable'] = false;
            colObj['renderer'] = Wtf.cc.renderer[rec.get("renderer")]
            colObj['hidden'] = !rec.get('visible');
            cfg[cfg.length] = colObj;
        }
    }
    return cfg;
}
/**
 * this function return the values for item
 * mode : edit mode/ create mode
 * record : meta data of custom column
 * type : xtype
 * data ; data for this item in case of edit mode
 **/
function addFormsCCValue(fld,record,mode,type,data){
    var v=(mode==1?data[record.get("dataIndex")]:record.get("defaultValue"));
    if(v!=""){
        if(type==Wtf.cc.columntype.CHECKBOX){
            fld["checked"] = v;
        }else if(type==Wtf.cc.columntype.DATE_FIELD){
            fld['value'] = new Date(parseInt(v)).format(WtfGlobal.getOnlyDateFormat());
        }else if(type==Wtf.cc.columntype.FILE){

        }else{
            fld['value'] =v;
        }
    }
}

/**
 *creates all items using csutom column metra data.
 *module : module name of the custom column
 *mode : create/edit
 *d ; data if edit mode.
 **/
function getFormsCCFields(module,mode,d){
    var config =[];
    var store = Wtf.cusColumnDS;
    var len = store.getCount();
    for(var i=0; i<len; i++){
        var _r = store.getAt(i);
        var fld ={};
        if(_r.get("module")==module){
            var xtype = _r.get("type")
            fld['xtype'] =Wtf.cc.getColumnXtype(xtype);
            addFormsCCValue(fld, _r, mode, xtype, d);   //set the value to config
            switch(xtype){
                case Wtf.cc.columntype.CHECKBOX :
                    fld["width"]=30;
                    break;
                case Wtf.cc.columntype.DROD_DOWN:
                    fld['valueField']='name';
                    fld['displayField']='name';
                    fld['store'] =  new Wtf.data.SimpleStore({
                        fields :['name'],
                        data:getCcComboData(_r.get("masterdata"))
                    });
                    fld['editable']= false;
                    fld['mode'] = 'local';
                    fld['triggerAction'] ='all';
                    fld['selectOnFocus']= true;
                    //fld['emptyText']='Select value...';
                    break;
                case Wtf.cc.columntype.FILE:
                    fld['inputType'] = 'file';
                    fld['xtype']='textfield';
                    break;
                case Wtf.cc.columntype.DATE_FIELD:
                    fld['format'] = WtfGlobal.getOnlyDateFormat();
                    break;
            }
            var mand = _r.get('mandatory');
            fld['fieldLabel']= _r.get("header")+(mand?"*":"");
            fld['disabled'] =!(_r.get("enabled"));
            fld['allowBlank']= !mand;
            fld['name']=_r.get("no");
            if(!(_r.get("visible"))){
               fld["hidden"] = true;
               fld["itemCls"]="hideFormField";
               fld["hideLabel"] = true;
               fld['allowBlank']= true;
            }
            config[config.length]=fld;
        }
    }
    if(len==0){          // if no item for custom cilumn then add one hidden item to adjust form and avoid error
        var f = {};
        f["hidden"]=true;
        config[0] = f;
    }
    return config;
}
/*-----------end of custom column changes---------------------------*/

function getCustomColumnCount(module){
    var _s = Wtf.cusColumnDS;
    var _c = _s.getCount();
    var count = 0;
    for(var i=0;i<_c; i++)
        if(_s.getAt(i).get("module")==module)
            count++;

    return count;
}

Wtf.CustomFieldSet = function(module,mode,config,data){
    Wtf.apply(this,config);
    this.columnCount = getCustomColumnCount(module);
    if(this.columnCount>0){
        this.items = getFormsCCFields(module,mode,data);
    }
    Wtf.CustomFieldSet.superclass.constructor.call(this,{
        hidden:this.columnCount==0?true:false,
        items:this.columnCount==0?{}:this.items
    });
   
}
Wtf.extend(Wtf.CustomFieldSet,Wtf.form.FieldSet,{
      onRender: function(config){
        Wtf.CustomFieldSet.superclass.onRender.call(this,config);

    }


});
