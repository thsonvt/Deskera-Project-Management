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
Wtf.ScrollPanel = function(config){
    Wtf.apply(this, config);
    
    this.addEvents({
        bodyscroll: true
    });
    Wtf.ScrollPanel.superclass.constructor.call(this);
}

Wtf.ScrollPanel = Wtf.extend(Wtf.Panel, {
    getScrollState: function(){
        var sb = this.body.dom;
        return {
            left: sb.scrollLeft,
            top: sb.scrollTop
        };
    },
    
    restoreScroll: function(state){
        var sb = this.body.dom;
        sb.scrollLeft = state.left;
        sb.scrollTop = state.top;
    },
    
    
    scrollToTop: function(){
        this.body.dom.scrollTop = 0;
        this.body.dom.scrollLeft = 0;
    },
    
    scrollTo: function(v1, v2) {
        this.body.dom.scrollLeft = v1;
        this.body.dom.scrollTop = v2;
    },
    
    syncScroll: function(){
        var mb = this.body.dom;
        this.fireEvent("bodyscroll", mb.scrollLeft, mb.scrollTop);
    },
    onRender: function(ct, position){
        Wtf.ScrollPanel.superclass.onRender.call(this, ct, position);
        this.body.on('scroll', this.syncScroll, this);
    }
});
