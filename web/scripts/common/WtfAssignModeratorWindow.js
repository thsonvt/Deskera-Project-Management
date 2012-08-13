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

Wtf.AssignModeratorWindow = function(config){
    Wtf.apply(this, config);
    var btnArr = [];
    this.submitBtn = new Wtf.Button({
        text : WtfGlobal.getLocaleText('lang.done.text'),
        cls: 'adminButton',
        scope : this,
        handler: this.submitData
    });
    btnArr.push(this.submitBtn);

    this.cancelBtn = new Wtf.Button({
        text : WtfGlobal.getLocaleText('lang.cancel.text'),
        cls: 'adminButton',
        scope : this,
        handler: this.destroyComp
    });
    btnArr.push(this.cancelBtn);
    
    Wtf.AssignModeratorWindow.superclass.constructor.call(this, {
        title : WtfGlobal.getLocaleText('pm.admin.project.assignmoderator'),
        closable : true,
        modal : true,
        iconCls : 'iconwin',
        width: 450,
        height: 350,
        resizable :false,
        buttonAlign : 'right',
        buttons :btnArr,
        layout : 'border',
        id: 'assignModeratorWin',
        autoDestroy: true
    });
}

Wtf.extend(Wtf.AssignModeratorWindow, Wtf.Window,{

    initComponent: function(){
        Wtf.AssignModeratorWindow.superclass.initComponent.call(this);
        
        var topHTML = getTopHtml("Assign Moderators on projects which do not have any.", "Assign Moderators on projects which do not have any.", "../../images/Add-project-widget.jpg");

        this.usrRec = Wtf.data.Record.create([
           {name: 'userid', mapping: 'user.userID'},
           {name: 'name', mapping: 'user.fullName'},
           {name: 'username', mapping: 'user.userName'},
           {name: 'fname', mapping: 'user.firstName'},
           {name: 'lname', mapping: 'user.lastName'}
        ]);
        
        this.fPanel  = new Wtf.form.FormPanel({
            id: this.id + '_projectForm',
            border: false,
            autoScroll: true,
            loadMask: true,
            defaultType: 'combo',
            title: WtfGlobal.getLocaleText('pm.project.member.selectmoderator'),
            bodyStyle: 'padding: 20px;',
            url: '../../ProjectController.jsp?cm=setModerators'
        });

        this.add({
            region : 'north',
            height : 85,
            border : false,
            bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
            html: topHTML

        },{
            region : 'center',
            border : false,
            bodyStyle : 'background:#f1f1f1;font-size:10px;',
            layout : 'fit',
            items :this.fPanel
        });
        this.createCombos();
        this.doLayout();
    },
  
    createCombos: function(){
        this.memberCombos = [];
        var data = this.dataObj;
        for(var i = 0; i < data.length; i++){
            if(this.fPanel){
                var store = new Wtf.data.JsonStore({
                    root: data[i].projectID,
                    fields: [
                       {name: 'userid', mapping: 'user.userID'},
                       {name: 'name', mapping: 'user.fullName'},
                       {name: 'username', mapping: 'user.userName'},
                       {name: 'fname', mapping: 'user.firstName'},
                       {name: 'lname', mapping: 'user.lastName'}
                    ],
                    data: this.userData
                });
                this.memberCombos[i] = new Wtf.common.Select({
                    multiSelect: true,
                    forceSelection: true,
                    displayField: 'username',
                    valueField: 'userid' ,
                    triggerAction: 'all',
                    mode: 'local',
                    editable: true,
                    fieldLabel: data[i].projectName,
                    store: store
                });
                this.fPanel.add(this.memberCombos[i]);
            }
        }
    },

    destroyComp: function(btn, e){
        this.close();
        this.fPanel.destroy();
    },
    
    submitData: function(btn, e){
        var data = this.dataObj;
        var projectids = "", moderators = new Object();
        for(var i = 0; i < data.length; i++){
            projectids += data[i].projectID + ',';
            moderators[data[i].projectID] = this.memberCombos[i].getValue();
        }
        if(this.fPanel.form.isValid()){
            Wtf.Ajax.requestEx({
                url: '../../ProjectController.jsp',
                params: {
                    cm: 'setProjectModerators',
                    moderatorsJson: Wtf.encode(moderators)
                }
            }, this, function(res, req){
                msgBoxShow(13, 0);
            }, function(res, req){
                msgBoxShow(4, 1);
            });
        }
    }
})
