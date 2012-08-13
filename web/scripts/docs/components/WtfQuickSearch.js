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
Wtf.KWLQuickSearch = function(config){
    Wtf.KWLQuickSearch.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.KWLQuickSearch, Wtf.form.TextField, {
    Store: null,
    emptyText: WtfGlobal.getLocaleText('lang.search.text'),
    StorageArray: null,
    layout:'form',
    initComponent: function(){
        Wtf.KWLQuickSearch.superclass.initComponent.call(this);
        this.addEvents({
            'SearchComplete': true
        });
    },
    onRender: function(ct, position){
        Wtf.KWLQuickSearch.superclass.onRender.call(this, ct, position);
        this.el.dom.onkeyup = this.onKeyUp.createDelegate(this);
    },
    onKeyUp: function(e){
        if (this.getValue() != "") {
                this.Store.removeAll();
                var i = 0;
                while (i < this.StorageArray.length) {
                    var str=new RegExp(".*"+Wtf.escapeRe(this.getValue()),"gi");
                    if (str.test(this.StorageArray[i].get(this.field))) {
                        this.Store.add(this.StorageArray[i]);
                    }
                    i++;
                }
                //dsSearch.add(this.Storage.getAt(this.Storage.find('Name',this.quickSearchTF.getValue()))); 
        }
        else {
            this.Store.removeAll();
            for (i = 0; i < this.StorageArray.length; i++) {
                this.Store.insert(i, this.StorageArray[i]);
            }
        }
        this.fireEvent('SearchComplete', this.Store);
    },
    StorageChanged: function(store){
        this.Store = store;
        /*this.Store.on("load", function(a,b,opt){
            if(opt.params!=null && (opt.params.ss === null || opt.params.ss === undefined || (opt.params.ss==='' && (opt.params.ss1!=null || opt.params.ss1=="")))) {
                this.setValue("");
            }
        },this);*/
        this.StorageArray = this.Store.getRange();
    }
});
Wtf.reg('KWLQuickSearch', Wtf.KWLQuickSearch);

