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
Wtf.FilecontentTab= function(config){
        Wtf.apply(this, config);
        if(config.flag == 1){
            this.flag = 1;
        }
        Wtf.FilecontentTab.superclass.constructor.call(this, {
        frame: false,
        title:this.title,
        closable:true,
        layout: "border",
        items: [this.infoPanel = new Wtf.Panel({
            region: "north",
            frame: false,
            height: 75,
            border:false,
            tbar : this.GetToolbar()

        }), this.contentPanel = new Wtf.Panel({
            region: "center",
//            layout: "fit",
            border: false,
            frame: false,
            autoScroll: true,
            bodyStyle: "background-color:#FFFFFF;"
        })]
    });
    Wtf.Ajax.requestEx({
        url: Wtf.req.doc + "file-releated/filecontent/fileContent.jsp",
        params: {
            url: this.url
        }
    }, this, this.SuccessFun)
}

Wtf.extend(Wtf.FilecontentTab, Wtf.Panel, {
    SuccessFun:function(resp){
        this.respobj = eval('('+resp+')');
        this.containerdiv = document.createElement('div');
        this.containerdiv.className = "docHdr";
        this.containerdiv.style.height = "50px";
        this.containerdiv.innerHTML = '<div class="docHdrTxt" style="width:240px"><span class="sb">'+WtfGlobal.getLocaleText("pm.document.content.uploadedon")+':</span>&nbsp;' + this.respobj.date +
            '</div><div class="docHdrTxt" style="width:240px"><span class="sb">'+WtfGlobal.getLocaleText("lang.author.text")+':</span>&nbsp;' + this.respobj.author +
            '</div><div class="docHdrTxt"><span class="sb">'+WtfGlobal.getLocaleText("lang.size.text")+':</span>&nbsp;' + this.respobj.size +
            '</div><div style="clear:both;"></div><div class="docHdrTxt" style="width:100%"><span class="sb">'+WtfGlobal.getLocaleText("lang.comment.text")+':</span>&nbsp;' + WtfGlobal.URLDecode(decodeURIComponent(this.respobj.comment)) +
            '</div>';
        this.containerdiv.style.overflow = 'hidden';
        this.infoPanel.add(this.containerdiv);
        var obj1 = this.viewable  ? document.createElement('embed') : document.createElement('iframe')
        this.containerdiv1 = document.createElement('div');
//        thsi.containerdiv1.id = 'file1div' + tabid
        this.containerdiv1.style.height = '100%';
        this.containerdiv1.style.overflow = 'hidden';
//        obj1.id = "iframe1" + Wtf.getCmp(tabid);
//        obj1.name = "iframe1" + tabid;
        obj1.cls = "ascls";
        obj1.style.width = '100%';
        obj1.style.height = '100%';
        obj1.style.border = '0';
        obj1.style.autoScroll = false;
        if (this.RevisionNumber == '-' || this.RevisionNumber == null) {
            if(this.viewable){
                obj1.width = '100%';
                obj1.height = '100%';
                obj1.border = '0';
                obj1.quality = "high";
                obj1.bgcolor = "#FFFFFF";
                obj1.type = "application/x-shockwave-flash";
                obj1.wmode='transparent';
                obj1.src ='content.stream?path='+ this.url +'&type=swf';
            } else {
                obj1.src = 'fileData.jsp?url=' + this.url;
            }
        } else {
            Wtf.Ajax.requestEx({
                url: Wtf.req.doc + "file-releated/filecompare/fileCompare.jsp",
                params: {
                    url: this.respobj.svnname,
                    startrev: this.RevisionNumber,
                    endrev: -1,
                    fileType:this.fileType,
                    flag: '1'
                }
            }, this, function(){
                var tabname = this.respobj.docname;
                obj1.src = Wtf.req.doc + 'file-releated/filecompare/fileCompareData.jsp?startrev=' + this.RevisionNumber + '&extension=' + tabname.substr(tabname.lastIndexOf('.') + 1)+'&flag=1';
            });
        }
        this.containerdiv1.appendChild(obj1);
        this.contentPanel.add(this.containerdiv1);
        if(this.flag != 1){
            (this.respobj.version== "Inactive")?this.revBut.disable():this.revBut.enable();
        }
        this.doLayout();
        hideMainLoadMask();
    },
    GetToolbar:function(version){
        if(this.flag == 1){
            return ([this.dwdbut = new Wtf.Action({
//                  id: 'Download' + tabid,
                    text: WtfGlobal.getLocaleText('pm.project.document.download'),
                    iconCls: 'dpwnd dldicon',
                    scope:this,
                    handler: this.Downlist
            })]);
        }else{
            return ([this.revBut = new Wtf.Action({
        //                id: 'rev' + thtabid,
                    text: WtfGlobal.getLocaleText('pm.common.revisionlist'),
                    iconCls: 'pwnd RevisionList',
                    scope:this,
        //                disable: (this.respobj.version== "Inactive")?true:false,
                    handler: this.CallRevisionList
            }), this.dwdbut = new Wtf.Action({
        //                id: 'Download' + tabid,
                      text: WtfGlobal.getLocaleText('pm.project.document.download'),
                    iconCls: 'dpwnd dldicon',
                    scope:this,
                    handler: this.Downlist
            })]);
        }
    },
    CallRevisionList:function(){
        var revisionlistTab = Wtf.getCmp('tabrevlist'+this.url);
        if(revisionlistTab==null){
            revisionlistTab= new Wtf.revisionlistTab({
                url: this.respobj.svnname,
                id: 'tabrevlist'+this.url,
                parentid:this.parentid,
//                layout:'fit',
                tabname:this.respobj.docname,
                fileType:this.fileType,
                docid:this.url
            });
            Wtf.getCmp(this.parentid).add(revisionlistTab);
            Wtf.getCmp(this.parentid).doLayout();
        }
        Wtf.getCmp(this.parentid).activate(revisionlistTab);
    //        function revisionlistFun(url, tabid, tabname, docid, fileType){
    //        revisionlistFun(this.respobj.svnname, this.url + 'revisionlist', this.respobj.docname, this.url, this.fileType);
    },
    Downlist:function(){
        setDownloadUrl(this.url);
    }
});


Wtf.revisionlistTab= function(config){
    Wtf.apply(this, config);
    this.reader = new Wtf.data.JsonReader({
        idProperty: 'taskId',
        root: 'data',
        remoteGroup: true,
        remoteSort: true,
        fields: [{
            name: 'Revision',
            type: 'string'
        }, {
            name: 'Version',
            type: 'string'
        }, {
            name: 'Age',
            type: 'string'
        }, {
            name: 'Author',
            type: 'string'
        }, {
            name: 'Comment',
            type: 'string'
        }, {
            name: 'Download',
            type: 'string'
        }]
    });
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.ds = new Wtf.data.Store({
        url: Wtf.req.doc + 'file-releated/revision/revisionlistin.jsp?url=' + this.url,
        reader: this.reader
    });
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.cm = new Wtf.grid.ColumnModel([this.sm, {
         header: WtfGlobal.getLocaleText('pm.common.version'),
        dataIndex: 'Version'
    }, {
         header: WtfGlobal.getLocaleText('lang.age.text'),
        dataIndex: 'Age'
    }, {
        header: WtfGlobal.getLocaleText('lang.author.text'),
        dataIndex: 'Author'
    }, {
       header: WtfGlobal.getLocaleText('lang.comments.text'),
        dataIndex: 'Comment'
    }, {
        header: WtfGlobal.getLocaleText('pm.project.document.download'),
        dataIndex: 'Download',
        renderer: this.DownloadLink
    }]);
    this.cm.defaultSortable = true;
    this.grid = new Wtf.grid.GridPanel({
        //layout:'fit',
        ds: this.ds,
        cm: this.cm,
        sm: this.sm,
        trackMouseOver: true,
        enableColumnHide: false,
        headerStyle: 'background-color: rgb(231,240,250)',
        viewConfig: {
            forceFit: true,
            autoFill: true
        }
    });
    this.ds.load();
    this.ds.on("loadexception", function(){
        msgBoxShow(240, 1);
//        Wtf.MessageBox.show({
//            title: WtfGlobal.getLocaleText('lang.error.text'),
//            msg: 'A problem comparing docs',
//            buttons: Wtf.MessageBox.OK,
//            animEl: 'ok',
//            icon: Wtf.MessageBox.ERROR
//        });
    },this);
    Wtf.revisionlistTab.superclass.constructor.call(this, {
        frame: false,
        border: false,
        layout: "border",
        closable:true,
        title: "["+WtfGlobal.getLocaleText('pm.common.version')+"] " + this.tabname,
        items: [ this.dispPanel = new Wtf.Panel({
            region: "north",
            border: false,
            frame: false,
            height: 55,
            layout: "fit",
            tbar: this.GetToolbar(),
            html: '<div class="docHdr"><div class="docHdrTxt"><span class="sb">'+WtfGlobal.getLocaleText("pm.document.versioning.displayed")+':</span>&nbsp;<span id="'+this.id+'version"></span></div></div>'
        }), {
            region: "center",
            border: false,
            frame: false,
            layout: "fit",
            items: new Wtf.Panel({
                border: false,
                frame: false,
                items: [this.grid],
                autoScroll: true,
                layout: "fit",
                bodyStyle: "background-color:#FFFFFF;"
            })
        }]
    });
    Wtf.Ajax.requestEx({
        url: Wtf.req.doc + "file-releated/filecontent/filedownloadchk.jsp",
        params: {
            docid: this.docid
        }
    }, this,
    function(resp, option){
        var respText = eval('(' + resp + ')');
        if (respText.download == "yes" || this.fileType == "Image") {
            displayButflag = 1;
        }else{
            this.compButt.enable();
        }
    })
    Wtf.Ajax.requestEx({
        url: Wtf.req.doc + "file-releated/revision/revisionListMain.jsp",
        params: {
            url: this.url
        }
    }, this, this.SuccessFun)
     
}

Wtf.extend(Wtf.revisionlistTab, Wtf.Panel, {
    DownloadLink:function(a, b, c, d, e, f){
        return "<a href='#' title=WtfGlobal.getLocaleText('pm.project.document.download') onclick='setDldUrl(\"" + unescape(a) + "\")'><div class='dpwnd dldiconwt' style='height:16px; width:16px;'></div></a>";
    },
    GetToolbar:function(){
        return ([this.compButt = new Wtf.Action({
            text: WtfGlobal.getLocaleText('lang.comapre.text'),
            tooltip: {
                text: WtfGlobal.getLocaleText('pm.project.document.comapare.text')
            },
            iconCls: 'pwnd RevisionList',
            disabled: ((this.displayButflag == 0) ? false : true),
            scope:this,
            handler: this.Compare_Click
        })]);
    },
    SuccessFun:function(resp, option){
        var respobj = eval('(' + resp + ')');
        var version = respobj.version;
        Wtf.get(this.id+"version");
        
    },
    Compare_Click:function(){
        var val = this.sm.getSelections();
        if (val.length == 2) {
            var rev1 = parseInt(val[1].get('Revision'));
            var rev2 = parseInt(val[0].get('Revision'));
            //for title
            var v1 = parseInt(val[1].get('Version'));
            var v2 = parseInt(val[0].get('Version'));
            //to check if rev1 > rev2 which depends on selection and swap if case is true
            if (rev1 > rev2) {
                rev1 = rev1 ^ rev2;
                rev2 = rev1 ^ rev2;
                rev1 = rev1 ^ rev2;
                v1 = v1 ^ v2;
                v2 = v1 ^ v2;
                v1 = v1 ^ v2;
            }
            //             var filecompareTab = Wtf.getCmp('tabrevlist'+this.url);
            var filecompareTab = Wtf.getCmp('compare' + this.url + rev1 + rev2);
            if(filecompareTab==null){
                filecompareTab= new Wtf.filecompareTab({
                    parentid:this.parentid,
                    url:this.url,
                    rev1:rev1,
                    closable: true,
                    rev2:rev2,
                    id:'compare' + this.url + rev1 + rev2,
                    v1:v1,
                    v2:v2,
                    tabname:'[Comparison] '+this.tabname,
                    fileType:this.fileType
                });
                Wtf.getCmp(this.parentid).add(filecompareTab);
                Wtf.getCmp(this.parentid).doLayout();
            }
            Wtf.getCmp(this.parentid).activate(filecompareTab);
        }
        else {
            msgBoxShow(263,1);
           // Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), "Please select two versions to compare",1);
        }
    }
});

Wtf.filecompareTab= function(config){
    Wtf.apply(this, config);
    this.currentPosition = -1;
    this.array="";
    this.frameNames="";
    this.arraySize="";
    this.pageNo=0;
    this.loadedFrameNum= 0;
    
    this.tpl = new Wtf.XTemplate('<div class="docHdr"><tpl for="items"><div class="docHdrTxt"><img src="{i}"/><span class="sb">{h}:</span><span class="st">&nbsp;{t}</span></div></tpl></div>');
    Wtf.FilecontentTab.superclass.constructor.call(this,{
        baseCls: 'ascls1',
        layout: 'border',
        title:this.tabname,
        items: [this.rightPanel = new Wtf.Panel({
            region: 'east',
            id: 'east' + this.id,
            border: false,
            title: WtfGlobal.getLocaleText('pm.common.version')+" " + this.v2,
            width: '50%',
            layout: 'fit'/*,
                items: obj2*/

        }),this.topPanel = new Wtf.Panel({
            region: 'north',
            border: false,
            height: 32
        //html: this.tpl,//.apply(this.data),
        /*,
                    items:[
                        new Wtf.Button({
                            text:WtfGlobal.getLocaleText('pm.common.prev'),
                            scope:this,
                            handler: this.prev
                        }),
                        new Wtf.Button({
                            text:WtfGlobal.getLocaleText('lang.next.text'),
                            scope:this,
                            handler:this.next 
                       })
                    ]*/

        })
        , this.leftPanel = new Wtf.Panel({
            region: 'center',
            id: 'center' + this.id,
            border: false,
            title: WtfGlobal.getLocaleText('pm.common.version')+" " + this.v1,
            width: '50%',
            layout: 'fit'/*,
                items: obj1*/
        })]
    });
    Wtf.Ajax.requestEx({
        url: Wtf.req.doc + "file-releated/filecompare/fileCompare.jsp",
        params: {
            url: this.url,
            startrev: this.rev1,
            endrev: this.rev2,
            flag: '0',
            fileType: this.fileType
        }
    }, this, this.SuccessPanel)

}

Wtf.extend(Wtf.filecompareTab, Wtf.Panel, {
    set:function(numbers, names){
        this.array = numbers;
        this.frameNames = names;
        this.arraySize = this.array.length;
    },
    next:function(){
        var dest = this.array[this.arraySize - 1];
        if (this.arraySize > 0) {
            if (this.currentPosition != this.arraySize - 1) {
                dest = this.array[++this.currentPosition];
            }
        }
        thsi.jump(dest);
    },
    previous:function(){
        var dest = this.array[0];
        if (this.arraySize > 0) {
            if (this.currentPosition != -1 && this.currentPosition != 0) {
                dest = this.array[--this.currentPosition];
            }
        }
        this.jump(dest);
    },
    jump:function(dest){
        if (self.frames[leftFrameName] == null || self.frames[rightFrameName] == null) {
            return;
        }
        else {
            var path = this.frameNames[this.currentPosition];
            if (path.indexOf("startrev") != -1) {
                self.frames[leftFrameName].location = path + "#" + dest;
            }
            else {
                self.frames[rightFrameName].location = path + "#" + dest;
            }
        }
    },

    SuccessPanel:function(resp, option){
        var respobj = eval('(' + resp + ')');
        var endurl = respobj.endurl;
        var starturl = respobj.starturl;
        var itemsadded = respobj.itemsadded;
        var itemsmodified = respobj.itemsmodified;
        var itemsdeleted = respobj.itemsdeleted;
        this.data = {
            items: [{
                h: WtfGlobal.getLocaleText('pm.dashboard.widget.mydocuments.added'),
                i: '../../images/added_ico.gif',
                t: itemsadded
            }, {
                h: WtfGlobal.getLocaleText("pm.document.versioning.changed"),
                i: '../../images/changed_ico.gif',
                t: itemsmodified
            }, {
                h: WtfGlobal.getLocaleText('pm.common.deleted'),
                i: '../../images/removed_ico.gif',
                t: itemsdeleted
            }]
        };
        this.tpl.overwrite(this.topPanel.el.dom.firstChild.firstChild, this.data);
        //        this.topPanel.innerHTML = this.tpl.apply(this.data);
        this.obj1 = new Wtf.ScrollPanel({
            id: "iframe1" + this.id,
            width: '100%',
            border: false,
            autoLoad: starturl+"&page=0",
            autoScroll: true,
            style: 'background-color: rgb(255,255,255)'
        });
        this.obj2 = new Wtf.ScrollPanel({
            id: "iframe2" + this.id,
            width: '100%',
            border: false,
            autoLoad: endurl+"&page=0",
            autoScroll: true,
            style: 'background-color: rgb(255,255,255)'
        });
        this.leftPanel.add(this.obj1);
        this.rightPanel.add(this.obj2);
        /*  var updater1 = obj1.getUpdater();//.on('update',function(){alert("frame1 is loaded");});
        var updater2 = obj2.getUpdater();//.on('update',function(){alert("frame2 is loaded");});
        alert(updater1 + updater2);*/
        this.obj1.on("bodyscroll", function(scrollLeft, scrollTop){
            this.obj2.body.dom.scrollTop = scrollTop;
            this.obj2.body.dom.scrollLeft = scrollLeft;

        },this);
        this.obj2.on("bodyscroll", function(scrollLeft, scrollTop){
            this.obj1.body.dom.scrollTop = scrollTop;
            this.obj1.body.dom.scrollLeft = scrollLeft;
        },this);
        this.doLayout();
    }
});
