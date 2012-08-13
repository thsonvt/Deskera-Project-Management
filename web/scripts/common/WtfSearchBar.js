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
Wtf.KWLSearchBar = function(config){
    Wtf.KWLSearchBar.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.KWLSearchBar, Wtf.Panel, {
    searchid: "all",
    SearchOn: false,
//    timer: null,
    txt: null,
    searchds: null,
    searchds1: null,
    textSearchFlag: false,
    Noflag: 0,
    tabpanel: null,
    searchgrid: null,
    searchgrid1: null,
    peoplepanel: null,
    peoplepanel1: null,
    communitypanel: null,
    communitypanel1: null,
    projectpanel: null,
    projectpanel1: null,
    KGridAll: null,
    KGridOnlyDoc: null,
    div1: null,
    AllSearchPanel: null,
    snippet: '<div><img src="{imgico}"/><div class="snippet">{title}</div><br><br/><div class = "fileinfo", style = "top : 27px;">{type}</div><div class = "fileinfo", style= "top : 38px;">{size}Kb</div><span  class="txtSearchSnippet">{descp}</span></div>',
    tpl: null,
    mainpaneltab: null,
    searchcolModel: null,
    SearchAllDocGrid: null,
    ds: null,
    contentEl:'serchForIco',
    layout:'form',
    timer:new Wtf.util.DelayedTask(this.getSearchResult),
    onRender: function(config){
        Wtf.KWLSearchBar.superclass.onRender.call(this, config);
        this.add(new Wtf.form.TextField({
            id: "textSearch",
            width: 200,
            height: 21,
            colspan: 3,
            emptyText: WtfGlobal.getLocaleText('lang.search.text'),
            cls:'searchInput',
            bodyStyle:'padding-left:24px !important;'
            
        }));
        this.add(new Wtf.Toolbar.MenuButton({
            text: '',
            renderTo:'serchForIco',
            id: 'searchBtn',
            iconCls:'dpwnd searchall',
            ctCls:'searchleftbutton',
            colspan: 1,
            menu: {
                items: [{
                    text: WtfGlobal.getLocaleText('lang.people.text')+'   ',
                    handler: this.onItemClick,
                    scope: this,
                    iconCls: 'pwnd searchpeople',
                    id: 'pep'
                }, {
                    text: WtfGlobal.getLocaleText('pm.projects.text'),
                    handler: this.onItemClick,
                    scope: this,
                    iconCls: 'dpwnd searchproject',
                    id: 'pro'
                }, /*{
                    text: 'Communities ',
                    handler: this.onItemClick,
                    scope: this,
                    iconCls: 'pwnd searchcommunity',
                    id: 'com'
                }, */{
                    text: WtfGlobal.getLocaleText('Document.text'),
                    handler: this.onItemClick,
                    scope: this,
                    iconCls: 'pwnd searchdoc',
                    id: 'doc'
                }, '-', {
                    text: WtfGlobal.getLocaleText('lang.all.text')+'       ',
                    cls: "selected",
                    handler: this.onItemClick,
                    scope: this,
                    iconCls: 'dpwnd searchall',
                    id: 'all'
                }/*, '-', {
                    //TODO:feature to be implemented
                    text: 'Advanced Search...   ',
                    handler: this.onItemClick,
                    scope: this
                }*/]
            }
        }));
         this.add(new Wtf.Toolbar.Button({
            text: '',
            id: 'searchBtn1',
            iconCls:'btnsearch',
            ctCls:'searchrightbutton',
            scope: this,
            handler: this.onButtonClick
          
        }));
            Wtf.getCmp("textSearch").on('render', function(e){
            Wtf.EventManager.addListener("textSearch", 'keyup', this.txtsearchKeyPress, this);
            Wtf.EventManager.addListener("textSearch", 'keyup', this.txtsearchTitleRefresh, this);
            Wtf.getCmp('textSearch').on('specialkey', this.specialKeyPressed, this);
            Wtf.getCmp('textSearch').on('focus', function(){this.setValue("");});
            Wtf.getCmp("searchBtn").on("mouseover",this.btnHover);
            Wtf.getCmp("searchBtn1").on("mouseover",this.btnHover);
            Wtf.getCmp("searchBtn1").on("mouseout",this.btnOut);
            Wtf.getCmp("searchBtn").on("mouseout",this.btnOut);
        }, this);
        var dsReader = new Wtf.data.JsonReader({
                root: "data",
                fields: [{
                    name: 'FileName'
                
                }, {
                    name: 'Size'
                
                }, {
                    name: 'Type'
                
                }, {
                    name: 'DateModified'
                }, {
                    name: 'RevisionNumber'
                }, {
                    name: 'Author'
                }, {
                    name: 'Summary'
                },{
                    name: 'DocumentId'
                }]
            })
        this.searchds = new Wtf.data.Store({
            reader: dsReader
        });
        this.searchds1 = new Wtf.data.Store({
            reader : dsReader
        });
        this.tpl = new Wtf.Template(this.snippet);
        this.tpl.compile();
        this.searchcolModel = new Wtf.grid.ColumnModel([{
            header: "Name     ",
            sortable: true,
            dataIndex: 'FileName',
            width: 100,
            scope: this,
            renderer: function(value, p, record){
                var img = MimeBasedValue(record.data.Type,true);
                return String.format("<img src={0} style='height:16px;width:16px;margin:0px 4px 0 0;vertical-align:text-top;'/>{1}", img, value);
            }
        }, {
            header: "Size     ",
            sortable: true,
            dataIndex: 'Size',
            width: 100,
            renderer: function(value, p, record){
                //return String.format("<span>{0}Kb</span>", Math.round(value / 1024));
                return value+ " KB";
            }
        }, {
            header: "Type    ",
            sortable: true,
            dataIndex: 'Type',
            width: 100,
            renderer: function(value, p, record){
                return String.format("<span>{0}</span>", MimeBasedValue(value,false));
            }
        }, {
            header: "Modified Date     ",
            sortable: true,
            renderer: WtfGlobal.dateRendererForServerDate,
            dataIndex: 'DateModified',
            width: 100
        }, {
            header: "Author	",
            sortable: true,
            dataIndex: 'Author',
            width: 100
        }]);
    },

    onButtonClick: function(e){
        this.SearchOn = true;
        var tab = Wtf.getCmp("tabsearchBtn");
        if (!tab) {
            Wtf.getCmp("as").add(this.mainpaneltab = new Wtf.ux.ContentPanel({
                id: "tabsearchBtn",
                layout: 'fit',
                title: WtfGlobal.getLocaleText('pm.search.deskera.text'),
                iconCls:getTabIconCls(Wtf.etype.search)
            }));
            this.mainpaneltab.on('render', this.mainpaneltabrendered, this);
            Wtf.getCmp("as").setActiveTab(this.mainpaneltab);
            this.mainpaneltab.on("destroy",function(){
                Wtf.getCmp("textSearch").reset();
            });
            
//            if (Wtf.getCmp('textSearch').getValue() != "") {
//                this.txt = Wtf.getCmp('textSearch').getValue();
//                this.getSearchResult();
//            }
        }
        else {
            Wtf.getCmp("as").activate("tabsearchBtn");
            if (Wtf.getCmp('textSearch').getValue() != "") {
                this.txt = Wtf.getCmp('textSearch').getValue();
                this.switchdisplay(this.searchid);
                this.getSearchResult();
            }
            //Wtf.getCmp("as").activate("tabsearchBtn");
            this.switchdisplay(this.searchid);
        }
        this.txtsearchTitleRefresh();
    },
    
    onItemClick: function(e){
        var sbttn = Wtf.getCmp('searchBtn');
        var txts = Wtf.getCmp("textSearch");
        this.searchid = e.id;
        var menuItem = e.parentMenu.items.items;
        for(var i = 0; i <menuItem.length; i++) {
            if(menuItem[i].el.dom.firstChild.style !== undefined) {
                menuItem[i].el.dom.firstChild.style.border = "0";
            }
        }
        switch (e.id) {
            case "pep":
                sbttn.setIconClass("pwnd searchpeople");
                txts.emptyText = WtfGlobal.getLocaleText("pm.search.people");
//                e.parentMenu.items.items[0].el.dom.firstChild.style.border = "1px outset #222";
                break;
            case "pro":
                sbttn.setIconClass("dpwnd searchproject");
                txts.emptyText = WtfGlobal.getLocaleText("pm.search.project");
//                e.parentMenu.items.items[1].el.dom.firstChild.style.border = "1px outset #222";
                break;
            case "com":
                sbttn.setIconClass("pwnd searchcommunity");
                txts.emptyText='Search for Communities';
//                e.parentMenu.items.items[2].el.dom.firstChild.style.border = "1px outset #222";
                break;
            case "doc":
                sbttn.setIconClass("pwnd searchdoc");
                txts.emptyText = WtfGlobal.getLocaleText("pm.search.docs");
//                e.parentMenu.items.items[3].el.dom.firstChild.style.border = "1px outset #222";
                break;
            case "all":
                sbttn.setIconClass("dpwnd searchall");
                txts.emptyText = WtfGlobal.getLocaleText({key:"pm.search.people", params:companyName});
//                e.parentMenu.items.items[3].el.dom.firstChild.style.border = "1px outset #222";
                break;
        }
        e.el.dom.firstChild.style.border = "1px outset #222";
         txts.reset();
    },
    
    txtsearchKeyPress: function(e){
        var tab = Wtf.getCmp("tabsearchBtn");
        if (tab) {
            if (this.SearchOn) {
                this.txt = e.getTarget().value;
                this.timer.cancel();
                this.timer.delay(1000,this.getSearchResult,this);
                
            }
            Wtf.getCmp("as").activate("tabsearchBtn");
            if (Wtf.getCmp("SearchTabPanel")) 
                this.switchdisplay(this.searchid);
        }
        else {
            this.SearchOn = false;
            Wtf.getCmp("as").add(this.mainpaneltab = new Wtf.ux.ContentPanel({
                id: "tabsearchBtn",
                layout: 'fit',
                title: WtfGlobal.getLocaleText('pm.search.deskera.text'),
                iconCls:getTabIconCls(Wtf.etype.search)
            }));
            this.mainpaneltab.on('render', this.mainpaneltabrendered, this);
            Wtf.getCmp("as").setActiveTab(this.mainpaneltab);
            this.mainpaneltab.on("destroy",function(){
                Wtf.getCmp("textSearch").reset();
                Wtf.getCmp("searchBtn").focus();
            });
        }
    },
    
    txtsearchTitleRefresh: function(e){
        if (Wtf.getCmp("SearchTabPanel")) {
            var projecttitle = WtfGlobal.getLocaleText("pm.search.people.results");
            var peopletitle = WtfGlobal.getLocaleText("pm.search.project.results");
            var doctitle = WtfGlobal.getLocaleText("pm.search.docs.results");
            var stripvalue = WtfGlobal.HTMLStripper(Wtf.getCmp('textSearch').getValue());
            stripvalue="'"+stripvalue+"'";
            switch (this.searchid) {
                case 'all':
                    if (this.projectpanel) {
                        this.peoplepanel.ResetTitle(peopletitle+stripvalue);
//                        this.communitypanel.ResetTitle(Wtf.getCmp('textSearch').getValue());
                        this.projectpanel.ResetTitle(projecttitle +stripvalue);
                        this.KGridAll.ResetTitle(doctitle + stripvalue);
                    }
                    break;
                case 'pep':
                    if (this.peoplepanel1) {
                        this.peoplepanel1.ResetTitle(peopletitle + stripvalue);
                    }
                    break;
                case 'com':
                    if (this.communitypanel1) {
                        this.communitypanel1.ResetTitle(stripvalue);
                    }
                    break;
                case 'pro':
                    if (this.projectpanel1) {
                        this.projectpanel1.ResetTitle(projecttitle + stripvalue);
                    }
                    break;
                case 'doc':
                    if (this.KGridOnlyDoc) {
                        this.KGridOnlyDoc.ResetTitle(doctitle + stripvalue);
                    }
                    break;
            }
        }
    },

    specialKeyPressed: function(f, e){
        if (e.getKey() == 13) {
            this.SearchOn = true;
        }
    },
    
    getSearchResult: function(){
        if (this.txt != "") {
            switch (this.searchid) {
                case "all":
                    this.Noflag = 4;
                    this.getData("docs", true, "grid1");
                    this.getData("user", false, "peoplepanel");
                    //this.getData("com", false, "communitypanel");
                    this.getData("pro", false, "projectpanel");
                    break;
                case "pep":
                    this.Noflag = 1;
                    this.getData("user", false, "peoplepanel1");
                    break;
                case "com":
                    this.Noflag = 1;
                    this.getData("com", false, "communitypanel1");
                    break;
                case "pro":
                    this.Noflag = 1;
                    this.getData("pro", false, "projectpanel1");
                    break;
                case "doc":
                    this.Noflag = 1;
                    this.getData("docs", true, "grid2");
                    break;
            }
        }
        else {
            switch (this.searchid) {
                case "all":
                    this.searchds.removeAll();
                    Wtf.getCmp('peoplepanel').Refresh("{}");
//                    Wtf.getCmp('communitypanel').Refresh("{}");
                    Wtf.getCmp('projectpanel').Refresh("{}");
                    break;
                case "pep":
                    Wtf.getCmp('peoplepanel1').Refresh("{}");
                    break;
                case "com":
                    Wtf.getCmp('communitypanel1').Refresh("{}");
                    break;
                case "pro":
                    Wtf.getCmp('projectpanel1').Refresh("{}");
                    break;
                case "doc":
                    this.searchds1.removeAll();
                    break;
            }
        }
    },
    
    getData: function(type, urlflag, component){
        if (component != "grid1" && component != "grid2") {
            var comp = Wtf.getCmp(component);
            if(comp){
                //comp.ResetTitle(Wtf.getCmp('textSearch').getValue());
                var stripval = WtfGlobal.HTMLStripper(this.txt).trim();
                //if(stripval.length>=1){
                Wtf.getCmp("textSearch").setValue(stripval);
                    comp.setUrl(Wtf.req.doc + 'search/QuickSearchData.jsp?type=' + type + '&keyword=' + stripval);
                /*}else{
                    Wtf.getCmp("textSearch").setValue("");
                }*/
            }
        }
//        if(this.document === undefined)
//            this.document = Wtf.subscription.docs;
        else if(this.document && this.documentView) {
            if (this.searchid == "all" && type == "docs") {
                //this.KGridAll.loadMask.show();
            }
            else {
               // this.KGridOnlyDoc.loadMask.show();
            }
            Wtf.Ajax.request({
                method: 'GET',
                url: Wtf.req.doc + 'search/searchIndex.jsp',
                params: ({
                    type: type,
                    keyword: WtfGlobal.HTMLStripper(this.txt)
                }),
                scope: this,
                success: function(result, req){
                    if (this.searchid == "all" && type == "docs") {
                        this.searchds.loadData(eval('(' + result.responseText + ')'), "data");
                        this.ds = this.searchds;
                        /*this.KGridAll.loadMask.hide();
                        if (this.KGridAll.labelext.innerHTML == "") {
                            this.KGridAll.ResetTitle(Wtf.getCmp('textSearch').getValue());
                        }*/
                    }
                    else {
                        this.searchds1.loadData(eval('(' + result.responseText + ')'), "data");
                        this.ds = this.searchds1;
                        /*this.KGridOnlyDoc.loadMask.hide();
                        if (this.KGridOnlyDoc.labelext.innerHTML == "") {
                            this.KGridOnlyDoc.ResetTitle(Wtf.getCmp('textSearch').getValue());
                        }*/
                    }
                },
                failure: function(result, req){
                    if (this.searchid == "all" && type == "docs") {
                      //  this.KGridAll.loadMask.hide();
                    }
                    else {
                      //  this.KGridOnlyDoc.loadMask.hide();
                    }
                }
            });
        }
    },
    
    mainpaneltabrendered: function(r){
        this.mainpaneltab.add(this.tabpanel = new Wtf.TabPanel({
            id: 'SearchTabPanel',
            tabWidth: 'auto',
            enableTabScroll: true,
            border: false
        }));
        this.switchdisplay(this.searchid);
        this.SearchOn = true;
        this.txt = Wtf.getCmp("textSearch").getValue();
        this.timer.cancel();
        this.timer.delay(1000,this.getSearchResult,this);
    },
    
    switchdisplay: function(searchid){
        switch (searchid) {
            case 'all':
                if (document.getElementById('SearchTabPanel__SearchAll') == null) {
                    this.KGridAll = new Wtf.common.KWLListPanel({
                        title: WtfGlobal.getLocaleText("pm.search.docs.results"),
                        id: 'gridpaneldocsearch',
                        paging: false,
                        autoLoad: false,
                        border: true,
                        layout: "fit"
                    });
                    this.SearchAllDocGrid = new Wtf.grid.GridPanel({
                        id: 'DocSearchGrid',
                        store: this.searchds,
                        cm: this.searchcolModel,
                        autoScroll: true,
                        viewConfig: {
                            forceFit: true,
                            emptyText: '<div class="emptyGridText">No documents found that match your search criteria</div>'
                        },
                        bodyStyle: 'background-color:transparent;'
                    });
                    this.KGridAll.add(this.SearchAllDocGrid);
                    this.KGridAll.doLayout();
                    this.peoplepanel = new Wtf.common.KWLListPanel({
                        title: WtfGlobal.getLocaleText("pm.search.people.results"),
                        id: 'peoplepanel',
                        autoScroll: true,
                        Href: "../../user.html",
                        autoLoad: false,
                        anchor: '100% 33.3%',
                        ImgSrc: "user100.png",
                        TabType:Wtf.etype.user,
                        layout:'fit'                        
                    });
                    this.peoplepanel.doLayout();
                   /* this.communitypanel = new Wtf.common.KWLListPanel({
                        title: 'Community Results for ',
                        id: 'communitypanel',
                        autoScroll: true,
                        anchor: '100% 33.3%',
                        Href: "../../community.html",
                        autoLoad: false,
                        ImgSrc: "defaultcourse.png",
                        TabType:Wtf.etype.comm,
                        layout:'fit'
                    
                    });
                    this.communitypanel.doLayout();*/ 
                    this.projectpanel = new Wtf.common.KWLListPanel({
                        title: WtfGlobal.getLocaleText("pm.search.project.results"),
                        id: 'projectpanel',
                        autoScroll: true,
                        Href: "../../project.html",
                        anchor: '100% 33.3%',
                        autoLoad: false,
                        ImgSrc: "projectimage100.png",
                        TabType:Wtf.etype.proj,
                        layout:'fit'
                    });
                    this.projectpanel.doLayout();
                    this.AllSearchPanel = new Wtf.Panel({
                        layout: 'border',
                        id: 'AllSearchPanel',
                        cls: 'backcolor',
                        border: false,
                        items: [/*{
                            region: 'west',
                            id: 'westAllSearchPanel',
                            layout: 'fit',
                            split: true,
                            height:100,
                            bodyStyle: 'background-color:transparent;padding:0 8px 0 0;',
                            cls: 'backcolor',
                            border: false,
                            items: [this.KGridAll]
                        },*/{
                            region: 'center',
                            id: 'eastAllSearchPanel',
                            cls: 'backcolor',
                            border: false,
                            split: true,
                            layout: 'anchor',
                            bodyStyle: 'background-color:transparent;padding:0 0 0 8px;',
                            anchorSize: {
                                width: 800,
                                height: 600
                            },
                            items: [this.peoplepanel,this.projectpanel]// this.communitypanel, this.projectpanel]
                        }]
                    });
//                    if(this.document === undefined)
//                        this.document = Wtf.subscription.docs;
                    if(this.document && this.documentView){
                        this.AllSearchPanel.add(new Wtf.Panel({
                            region: 'west',
                            id: 'westAllSearchPanel',
                            layout: 'fit',
                            split: true,
                            height:100,
                            bodyStyle: 'background-color:transparent;padding:0 8px 0 0;',
                            cls: 'backcolor',
                            border: false,
                            items: [this.KGridAll]
                        }));
                    }
                    this.AllSearchPanel.doLayout();
                    this.AllSearchPanel.on('render', function(e){
                        this.AllSearchPanel.el.dom.firstChild.className += " backcolor";
                        this.AllSearchPanel.el.dom.firstChild.firstChild.className += " backcolor";
                    }, this);
                    this.AllSearchPanel.on('resize', function(e, aw, ah, rw, rh){
                        var temp = Wtf.getCmp("westAllSearchPanel");
                        if(temp !== undefined)
                            Wtf.getCmp("westAllSearchPanel").setWidth(aw / 2);
                        this.AllSearchPanel.doLayout();
                    }, this);
                    this.SearchAllDocGrid.on("mouseover", this.hidewindow, this);
                    this.SearchAllDocGrid.on("rowclick", this.searchpop, this);
                    this.SearchAllDocGrid.on("rowdblclick", this.rowdblclick, this);
                    this.SearchAllDocGrid.on('rowcontextmenu', this.onDocGridRowContextMenu, this);
                    this.SearchAllDocGrid.addListener('rowdblclick',this.rowdblclick, this);
                }
                this.DisplayTab(WtfGlobal.getLocaleText("Search"), "dpwnd searchalltab", "SearchAll", this.AllSearchPanel);
                break;
            case 'pep':
                if (document.getElementById('SearchTabPanel__SearchPeople') == null) {
                    this.peoplepanel1 = new Wtf.common.KWLListPanel({
                        title: WtfGlobal.getLocaleText("pm.search.people.results"),
                        id: 'peoplepanel1',
                        autoScroll: true,
                        autoLoad: false,
                        Href: "../../user.html",
                        ImgSrc: "user100.png",
                        TabType:Wtf.etype.user,
                        layout:'fit',
                        useFlag: 'network'
                    });
                }
                this.DisplayTab("People Search", "pwnd userTabIcon", "SearchPeople", this.peoplepanel1);
                break;
            case 'com':
                if (document.getElementById('SearchTabPanel__SearchCommunity') == null) {
                    this.communitypanel1 = new Wtf.common.KWLListPanel({
                        title: 'Community Results for ',
                        id: 'communitypanel1',
                        autoScroll: true,
                        autoLoad: false,
                        Href: "../../community.html",
                        ImgSrc: "defaultcourse.png",
                        TabType:Wtf.etype.comm,
                        layout:'fit'
                    });
                }
                this.DisplayTab("Community Search", "dpwnd communityTabIcon", "SearchCommunity", this.communitypanel1);
                break;
            case 'pro':
                if (document.getElementById('SearchTabPanel__SearchProject') == null) {
                    this.projectpanel1 = new Wtf.common.KWLListPanel({
                        title: WtfGlobal.getLocaleText("pm.search.project.results"),
                        id: 'projectpanel1',
                        autoScroll: true,
                        autoLoad: false,
                        Href: "../../project.html",
                        ImgSrc: "projectimage100.png",
                        TabType:Wtf.etype.proj,
                        layout:'fit',
                        useFlag: 'network'
                    });
                }
                this.DisplayTab("Project Search", "dpwnd projectTabIcon", "SearchProject", this.projectpanel1);
                break;
            case 'doc':
                if (document.getElementById('SearchTabPanel__Searchdocument') == null) {
                    this.KGridOnlyDoc = new Wtf.common.KWLListPanel({
                        title: WtfGlobal.getLocaleText("pm.search.docs.results"),
                        id: 'gridpaneldocsearch1',
                        paging: false,
                        autoLoad: false,
                        layout: "fit"
                    });
                    this.SearchOnlyDocGrid = new Wtf.grid.GridPanel({
                        id: 'DocSearchGrid1',
                        store: this.searchds1,
                        cm: this.searchcolModel,
                        autoScroll: true,
                        viewConfig: {
                            forceFit: true,
                            emptyText: '<div class="emptyGridText">'+ WtfGlobal.getLocaleText("pm.document.search.emptytext") +'</div>'
                        },
                        bodyStyle: 'background-color:transparent;'
                    });
                    this.KGridOnlyDoc.add(this.SearchOnlyDocGrid);
                    this.KGridOnlyDoc.doLayout();
                    this.SearchOnlyDocGrid.on("mouseover", this.hidewindow, this);
                    this.SearchOnlyDocGrid.on("rowclick", this.searchpop, this);
                    this.SearchOnlyDocGrid.addListener('rowdblclick',this.rowdblclick, this);
                    this.SearchOnlyDocGrid.on('rowcontextmenu', this.onDocGridRowContextMenu, this);
                }
                this.DisplayTab("Document Search", "pwnd doctabicon", "Searchdocument", this.KGridOnlyDoc);
                break;
        }
    },
    
    DisplayTab: function(title, iconCls, id, items){
        if (document.getElementById('SearchTabPanel__' + id) == null) {
            var tab = this.tabpanel.add({
                title: title,
                iconCls: iconCls,
                id: id,
                layout: 'fit',
                cls: 'appbg subtab',
                border: false,
                frame: false,
                items: [items]
            
            });
            this.tabpanel.setActiveTab(id);
            this.tabpanel.doLayout();
        }
        else {
            Wtf.getCmp("SearchTabPanel").activate(id);
        }
    },
    
    hidewindow: function(){
        if (document.getElementById("win1") != null) {
            Wtf.getCmp("win1").hide();
        }
    },
    
    searchpop: function(obj, rindex, eventobj){
        var record = this.ds.getAt(rindex);
        var a = record.get("Summary");
        var b = record.get("FileName");
        var c = record.get("Type");
        var size = record.get("Size");
        eventobj.preventDefault();
        var target = eventobj.getTarget();

        var oldContainPane = Wtf.getCmp("containpane");
        if(oldContainPane)      // if containpane already present then destroy it and create again as template could not be overwritten
            oldContainPane.destroy();

        new Wtf.Panel({
            id: "containpane",
            frame: true,
            hideBorders: true,
            baseCls: "sddsf",
            header: false,
            headerastext: false
        });

        var oldWin1 = Wtf.getCmp("win1");
        if(oldWin1)             // if win1 already present then destroy it and create again
            oldWin1.destroy();

        new Wtf.Window({
            id: "win1",
            animateTarget: obj,
            width: 350,
            height: 200,
            plain: true,
            shadow: true,
            header: false,
            closable: false,
            border: false,
            items: Wtf.getCmp("containpane")
        }).show();

        this.tpl.insertAfter('containpane', {
            imgico: MimeBasedValue(c,true),
            type: MimeBasedValue(unescape(c),false),
            title: unescape(b),
            //size: Math.round(size / 1024),
            size: size,
            descp: unescape(a)
        });

        Wtf.getCmp("win1").setPagePosition(eventobj.getPageX(), eventobj.getPageY());
    },
     onDocGridRowContextMenu: function(grid, num, e){

        e.preventDefault();
        
        var open = new Wtf.Action({
            text: WtfGlobal.getLocaleText('pm.common.open'),
            iconCls: 'dpwnd Openfile',
            scope:this,
            handler: function(){
//                grid.fireEvent('rowdblclick', grid, grid.getSelectionModel().getSelected());
                this.displayContent(grid)
            }
        });
        var gridMenu = new Wtf.menu.Menu({
                //id: 'gridMenu',
                items: [open]
        });
        grid.getSelectionModel().selectRow(num);
     //   grid.fireEvent('rowclick', grid, num, e);
        rownum = num;
        var posnX = e.getPageX();
        var posnY = e.getPageY();
        gridMenu.showAt([posnX, posnY]);
        return false;
    },
   // rowdblclick:function(a,b){
        displayContent:function(a){
        var selectedRow = a.getSelectionModel().getSelected();
        if(!docScriptLoaded){
            WtfGlobal.loadScript("../../scripts/minified/document.js?v=19");
            docScriptLoaded = true;
        }
        Wtf.Ajax.requestEx({
                    url: Wtf.req.doc + "file-releated/filecontent/filedownloadchk.jsp",
                    params:{
                        docid:selectedRow.get('DocumentId')
                    }},
                    this,
                    function(resp,option){
                        var respText = eval('('+resp+')');
                        if(respText.download=="no"){
                            //Wtf.getCmp("as").loadTab(Wtf.req.doc + 'file-releated/filecontent/fileContent.jsp?url=' + selectedRow.get('DocumentId'), 'tabfcontent'+selectedRow.get('DocumentId'),selectedRow.get('FileName'), '',Wtf.etype.docs);
                             var fileContent = Wtf.getCmp('tabfcontent'+selectedRow.get('DocumentId')+selectedRow.get('RevisionNumber')); 
                            if(fileContent==null){
                                fileContent= new Wtf.FilecontentTab({
                                    url: selectedRow.get('DocumentId'),
                                    id: 'tabfcontent'+selectedRow.get('DocumentId')+selectedRow.get('RevisionNumber'),
                                    parentid:"as",
                                    title:selectedRow.get('FileName'),
                                    fileType:selectedRow.get('Type'),
                                    RevisionNumber:selectedRow.get('RevisionNumber')
                                });
                                Wtf.getCmp("as").add(fileContent);
                                Wtf.getCmp("as").doLayout();
                            }
                            Wtf.getCmp("as").activate(fileContent);
//                            filecontentTab(selectedRow.get('DocumentId'),'tabfcontent'+selectedRow.get('DocumentId')+selectedRow.get('RevisionNumber'),selectedRow.get('FileName'),selectedRow.get('Type'),selectedRow.get('RevisionNumber'));
                        }
                        else{
                            setDownloadUrl(selectedRow.get('DocumentId'));
                        }
                    });
    },
    btnHover : function(){
        Wtf.getCmp("textSearch").addClass("searchInput-over");
        if(this.id=="searchBtn"){
            Wtf.getCmp("searchBtn1").addClass("x-btn-over");
        }else{
            Wtf.getCmp("searchBtn").addClass("x-btn-over");
        }
            
    },
    btnOut : function(){
        Wtf.getCmp("textSearch").removeClass("searchInput-over");
        if(this.id=="searchBtn"){
            Wtf.getCmp("searchBtn1").removeClass("x-btn-over");
        }else{
            Wtf.getCmp("searchBtn").removeClass("x-btn-over");
        }
            
    },

    setDocumentValue: function(value){
        this.document = value;
        var obj = Wtf.getCmp("doc");
        if(value)
            obj.show();
        else
            obj.hide();
    },

    setDocumentViewValue: function(value) {
        this.documentView = value;
        var obj = Wtf.getCmp("doc");
        if(value)
            obj.show();
        else
            obj.hide();
    }
});

function MimeBasedValue(value,imgFlag){
    var type;
    switch (value.toLowerCase()) {
        case "microsoft excel document":
            if(imgFlag){
                type = "../../images/XLS.png";
            }else{
                type = "Microsoft Excel Document";
            }
            break;
        case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
            if(imgFlag){
                type = "../../images/XLS.png";
            }else{
                type = "Microsoft Excel Document";
            }
            break;
        case "application/msword":
            if(imgFlag){
                type = "../../images/word.gif";
            }else{
                type = "Microsoft Word Document";
            }
            break;
        case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
            if(imgFlag){
                type = "../../images/word.gif";
            }else{
                type = "Microsoft Word Document";
            }
            break;
        case "pdf Document":
            if(imgFlag){
                type = "../../images/PDF.gif";
            }else{
                type = "PDF Document";
            }
            break;
        case "text/plain":
            if(imgFlag){
                type = "../../images/TXT52.png";
            }else{
                type = "Plain Text File";
            }
            break;
        case "text/xml":
            if(imgFlag){
                type = "../../images/XML52.png";
            }else{
                type = "XML File";
            }
            break;
        case "text/css":
            if(imgFlag){
                type = "../../images/CSS52.png";
            }else{
                type = "CSS File";
            }
            break;
        case "text/html":
            if(imgFlag){
                type = "../../images/HTML52.png";
            }else{
                type = "HTML File";
            }
            break;
        case "text/cs":
            if(imgFlag){
                type = "../../images/TXT52.png";
            }else{
                type = "C# Source File";
            }
            break;
        case "text/x-javascript":
            if(imgFlag){
                type = "../../images/TXT52.png";
            }else{
                type = "JavaScript Source";
            }
            break;
       default:
            if(imgFlag){
                type="../../images/documents.png"
            }else{
                type="File"
            }
    }
    return type;
}
