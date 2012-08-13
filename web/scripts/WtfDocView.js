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
docScriptLoaded = true;
var tabMyDocs = Wtf.getCmp("tabdocument");
if(tabMyDocs){
    Wtf.destroy(Wtf.get('document-tag-tree'));

    var mainGrid = new Wtf.docs.com.Grid({
        id: 'doc-mydocs',
        groupid: 1,
        border: false,
        treeroot: WtfGlobal.getLocaleText('pm.dashboard.widget.mydocuments.text'),
        autoWidth: true,
        userid: loginid,
        treeid: 'doctree-mydocs',
        treeRenderto: 'navareadocs',
        pcid:1
    });
    tabMyDocs.add(mainGrid);
    tabMyDocs.doLayout();
    //var tabpannelforDocTree =  Wtf.getCmp(mainPanel.getActiveTab().id)
    tabMyDocs.on('deactivate', function(tabpanel){
        if (Wtf.getCmp('doctree-mydocs'))
            Wtf.getCmp('doctree-mydocs').hide();
    });

    tabMyDocs.on('activate', function(tabpanel){
        if (Wtf.getCmp('doctree-mydocs'))
            Wtf.getCmp('doctree-mydocs').show();
    });

//    Wtf.getCmp("docpanel").expand(false);
}
