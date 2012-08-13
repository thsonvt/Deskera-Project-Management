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
Wtf.KWLCustomSearch = function(config){
    Wtf.KWLCustomSearch.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.KWLCustomSearch, Wtf.form.TextField, {
    store: null,
    emptyText  : WtfGlobal.getLocaleText('lang.search.text'),
    StorageArray: null,
    limit: this.limit,
    initComponent: function(){
        Wtf.KWLCustomSearch.superclass.initComponent.call(this);
        this.addEvents({
            'SearchComplete': true
        });
    },
    timer:new Wtf.util.DelayedTask(this.callKeyUp),
    setPage: function(val) {
        this.limit = val;
    },
    onRender: function(ct, position){
        Wtf.KWLCustomSearch.superclass.onRender.call(this, ct, position);
        this.el.dom.onkeyup = this.onKeyUp.createDelegate(this);
    },
    onKeyUp: function(e){
        if(this.store) {
            if (this.getValue() != "") {
                this.timer.cancel();
                this.timer.delay(1000,this.callKeyUp,this);
            }
            else {
                this.store.reload({
                    params: {
                        start: 0,
                        limit: this.limit,
                        ss: ""
                    }
                });
            }
            this.fireEvent('SearchComplete', this.store);
        }
    },
    callKeyUp: function() {
      this.store.reload({
          params: {
              start: 0,
              limit: this.limit,
              ss: this.getValue()
          }
      });
    },
    storageChanged: function(_s){
        this.store = _s;
        this.StorageArray = this.store.getRange();
    }
});
Wtf.reg('KWLCustomSearch', Wtf.KWLCustomSearch);
