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
Wtf.tree.NewFolderUI = function(node){
    this.node = node;
    this.rendered = false;
    this.animating = false;
    this.wasLeaf = true;
    this.ecc = 'x-tree-ec-icon x-tree-elbow';
    
};

Wtf.tree.NewFolderUI.prototype = {
    removeChild: function(node){
        if (this.rendered) {
            this.ctNode.removeChild(node.ui.getEl());
        }
    },
    onOver: function(e){
        this.addClass('x-tree-node-over');
    },
    
    onOut: function(e){
        this.removeClass('x-tree-node-over');
    },
    beforeLoad: function(){
        this.addClass("x-tree-node-loading");
    },
    
    afterLoad: function(){
        this.removeClass("x-tree-node-loading");
    },
    
    onTextChange: function(node, text, oldText){
        if (this.rendered) {
            this.textNode.innerHTML = text;
        }
    },
    
    onDisableChange: function(node, state){
        this.disabled = state;
        if (state) {
            this.addClass("x-tree-node-disabled");
        }
        else {
            this.removeClass("x-tree-node-disabled");
        }
    },
    
    onSelectedChange: function(state){
        if (state) {
            this.focus();
            this.addClass("x-tree-selected");
        }
        else {
        
            this.removeClass("x-tree-selected");
        }
    },
    
    onMove: function(tree, node, oldParent, newParent, index, refNode){
        this.childIndent = null;
        if (this.rendered) {
            var targetNode = newParent.ui.getContainer();
            if (!targetNode) {
                this.holder = document.createElement("div");
                this.holder.appendChild(this.wrap);
                return;
            }
            var insertBefore = refNode ? refNode.ui.getEl() : null;
            if (insertBefore) {
                targetNode.insertBefore(this.wrap, insertBefore);
            }
            else {
                targetNode.appendChild(this.wrap);
            }
            this.node.renderIndent(true);
        }
    },
    
    
    addClass: function(cls){
        if (this.elNode) {
            Wtf.fly(this.elNode).addClass(cls);
        }
    },
    
    
    removeClass: function(cls){
        if (this.elNode) {
            Wtf.fly(this.elNode).removeClass(cls);
        }
    },
    
    remove: function(){
        if (this.rendered) {
            this.holder = document.createElement("div");
            this.holder.appendChild(this.wrap);
        }
    },
    
    fireEvent: function(){
        return this.node.fireEvent.apply(this.node, arguments);
    },
    
    initEvents: function(){
        this.node.on("move", this.onMove, this);
        
        if (this.node.disabled) {
            this.addClass("x-tree-node-disabled");
        }
        if (this.node.hidden) {
            this.hide();
        }
        var ot = this.node.getOwnerTree();
        var dd = ot.enableDD || ot.enableDrag || ot.enableDrop;
        if (dd && (!this.node.isRoot || ot.rootVisible)) {
            Wtf.dd.Registry.register(this.elNode, {
                node: this.node,
                handles: this.getDDHandles(),
                isHandle: false
            });
        }
    },
    
    getDDHandles: function(){
        return [this.iconNode, this.textNode];
    },
    
    hide: function(){
        this.node.hidden = true;
        if (this.wrap) {
            this.wrap.style.display = "none";
        }
    },
    
    show: function(){
        this.node.hidden = false;
        if (this.wrap) {
            this.wrap.style.display = "";
        }
    },
    
    onContextMenu: function(e){
        if (this.node.hasListener("contextmenu") || this.node.getOwnerTree().hasListener("contextmenu")) {
            e.preventDefault();
            this.focus();
            this.fireEvent("contextmenu", this.node, e);
        }
    },
    
    onClick: function(e){
        if (this.dropping) {
            e.stopEvent();
            return;
        }
        if (this.fireEvent("beforeclick", this.node, e) !== false) {
            if (!this.disabled && this.node.attributes.href) {
                this.fireEvent("click", this.node, e);
                return;
            }
            e.preventDefault();
            if (this.disabled) {
                return;
            }
            
            if (this.node.attributes.singleClickExpand && !this.animating && this.node.hasChildNodes()) {
                this.node.toggle();
            }
            
            this.fireEvent("click", this.node, e);
        }
        else {
            e.stopEvent();
        }
    },
    
    onDblClick: function(e){
        if (this.disabled) {
            return;
        }
        if (this.checkbox) {
            this.toggleCheck();
        }
        if (!this.animating && this.node.hasChildNodes()) {
            this.node.toggle();
        }
        this.fireEvent("dblclick", this.node, e);
    },
    
    onCheckChange: function(){
        var checked = this.checkbox.checked;
        this.node.attributes.checked = checked;
        this.fireEvent('checkchange', this.node, checked);
    },
    
    ecClick: function(e){
        if (!this.animating && (this.node.hasChildNodes() || this.node.attributes.expandable)) {
            this.node.toggle();
        }
    },
    
    startDrop: function(){
        this.dropping = true;
    },
    
    
    endDrop: function(){
        setTimeout(function(){
            this.dropping = false;
        }
.createDelegate(this), 50);
    },
    
    expand: function(){
        this.updateExpandIcon();
        this.ctNode.style.display = "";
    },
    
    focus: function(){
        if (!this.node.preventHScroll) {
            try {
                this.anchor.focus();
            } 
            catch (e) {
            }
        }
        else 
            if (!Wtf.isIE) {
                try {
                    var noscroll = this.node.getOwnerTree().getTreeEl().dom;
                    var l = noscroll.scrollLeft;
                    this.anchor.focus();
                    noscroll.scrollLeft = l;
                } 
                catch (e) {
                }
            }
    },
    
    
    toggleCheck: function(value){
        var cb = this.checkbox;
        if (cb) {
            cb.checked = (value === undefined ? !cb.checked : value);
        }
    },
    
    blur: function(){
        try {
            this.anchor.blur();
        } 
        catch (e) {
        }
    },
    
    animExpand: function(callback){
        var ct = Wtf.get(this.ctNode);
        ct.stopFx();
        if (!this.node.hasChildNodes()) {
            this.updateExpandIcon();
            this.ctNode.style.display = "";
            Wtf.callback(callback);
            return;
        }
        this.animating = true;
        this.updateExpandIcon();
        
        ct.slideIn('t', {
            callback: function(){
                this.animating = false;
                Wtf.callback(callback);
            },
            scope: this,
            duration: this.node.ownerTree.duration || .25
        });
    },
    
    highlight: function(){
        var tree = this.node.getOwnerTree();
        Wtf.fly(this.wrap).highlight(tree.hlColor || "C3DAF9", {
            endColor: tree.hlBaseColor
        });
    },
    
    collapse: function(){
        this.updateExpandIcon();
        this.ctNode.style.display = "none";
    },
    
    animCollapse: function(callback){
        var ct = Wtf.get(this.ctNode);
        ct.enableDisplayMode('block');
        ct.stopFx();
        
        this.animating = true;
        this.updateExpandIcon();
        
        ct.slideOut('t', {
            callback: function(){
                this.animating = false;
                Wtf.callback(callback);
            },
            scope: this,
            duration: this.node.ownerTree.duration || .25
        });
    },
    
    getContainer: function(){
        return this.ctNode;
    },
    
    getEl: function(){
        return this.wrap;
    },
    
    appendDDGhost: function(ghostNode){
        ghostNode.appendChild(this.elNode.cloneNode(true));
    },
    
    getDDRepairXY: function(){
        return Wtf.lib.Dom.getXY(this.iconNode);
    },
    
    onRender: function(){
        this.render();
    },
    
    render: function(bulkRender){
        var n = this.node, a = n.attributes;
        var targetNode = n.parentNode ? n.parentNode.ui.getContainer() : n.ownerTree.innerCt.dom;
        
        if (!this.rendered) {
            this.rendered = true;
            
            this.renderElements(n, a, targetNode, bulkRender);
            
            if (a.qtip) {
                if (this.textNode.setAttributeNS) {
                    this.textNode.setAttributeNS("wtf", "qtip", a.qtip);
                    if (a.qtipTitle) {
                        this.textNode.setAttributeNS("wtf", "qtitle", a.qtipTitle);
                    }
                }
                else {
                    this.textNode.setAttribute("wtf:qtip", a.qtip);
                    if (a.qtipTitle) {
                        this.textNode.setAttribute("wtf:qtitle", a.qtipTitle);
                    }
                }
            }
            else 
                if (a.qtipCfg) {
                    a.qtipCfg.target = Wtf.id(this.textNode);
                    Wtf.QuickTips.register(a.qtipCfg);
                }
            this.initEvents();
            if (!this.node.expanded) {
                this.updateExpandIcon(true);
            }
        }
        else {
            if (bulkRender === true) {
                targetNode.appendChild(this.wrap);
            }
        }
    },
    deleteClick: function(node){
        alert(node);
        
    },
    renderElements: function(n, a, targetNode, bulkRender){
    
        this.indentMarkup = n.parentNode ? n.parentNode.ui.getChildIndent() : '';
        var cb = typeof a.checked == 'boolean';
        var href = a.href ? a.href : Wtf.isGecko ? "" : "#";
        var buf = ['<li class="x-tree-node"><table wtf:tree-node-id="', n.id, '" class="x-tree-node-el x-tree-node-leaf ', a.cls, '" cellspacing="0" cellpadding="0" ><tbody><tr><td>', this.indentMarkup, '</td><td ><img src="', Wtf.BLANK_IMAGE_URL, '" class="x-tree-ec-icon x-tree-elbow" /><img src="', a.icon || this.emptyIcon, '" class="x-tree-node-icon', (a.icon ? " x-tree-node-inline-icon" : ""), (a.iconCls ? " " + a.iconCls : ""), '" unselectable="on" /></td><td class="chip"><a hidefocus="on"  class="x-tree-node-anchor" tabIndex="1" ', a.hrefTarget ? ' target="' + a.hrefTarget + '"' : "", '>', '<span  unselectable="on">', n.text, '</span></td><td onmousedown="" ><img title="Edit Folder" onclick=EditClick("', n.id, '") style="cursor: pointer;" do="edit" src="../../images/edit12.gif"/><img title="Delete Folder" onclick=DeleteClick("', n.id, '") style="cursor: pointer;" do="delete" src="../../images/stop12.gif"/></td></tr></tbody></table><ul class="x-tree-node-ct" style="display:none;"></ul></li>'].join('');
        
        var index = 2;
        this.wrap = Wtf.DomHelper.insertHtml("beforeEnd", n.parentNode.ui.ctNode, buf);
        
        
        this.elNode = this.wrap.childNodes[0].childNodes[0].rows[0];
        this.ctNode = this.wrap.parentNode;
        var cs = this.elNode.childNodes;
        
        this.indentNode = cs[0].firstChild;
        this.ecNode = cs[1].firstChild;
        this.iconNode = cs[1].lastChild;
        
        this.anchor = cs[index].firstChild;
        
        this.textNode = cs[index].firstChild.firstChild;
    },
    
    
    getAnchor: function(){
        return this.anchor;
    },
    
    
    getTextEl: function(){
        return this.textNode;
    },
    
    
    getIconEl: function(){
        return this.iconNode;
    },
    
    
    isChecked: function(){
        return this.checkbox ? this.checkbox.checked : false;
    },
    
    updateExpandIcon: function(){
        if (this.rendered) {
            var n = this.node, c1, c2;
            var cls = n.isLast() ? "x-tree-elbow-end" : "x-tree-elbow";
            var hasChild = n.hasChildNodes();
            if (hasChild || n.attributes.expandable) {
                if (n.expanded) {
                    cls += "-minus";
                    c1 = "x-tree-node-collapsed";
                    c2 = "x-tree-node-expanded";
                }
                else {
                    cls += "-plus";
                    c1 = "x-tree-node-expanded";
                    c2 = "x-tree-node-collapsed";
                }
                if (this.wasLeaf) {
                    this.removeClass("x-tree-node-leaf");
                    this.wasLeaf = false;
                }
                if (this.c1 != c1 || this.c2 != c2) {
                    Wtf.fly(this.elNode).replaceClass(c1, c2);
                    this.c1 = c1;
                    this.c2 = c2;
                }
            }
            else {
                if (!this.wasLeaf) {
                    Wtf.fly(this.elNode).replaceClass("x-tree-node-expanded", "x-tree-node-leaf");
                    delete this.c1;
                    delete this.c2;
                    this.wasLeaf = true;
                }
            }
            var ecc = "x-tree-ec-icon " + cls;
            if (this.ecc != ecc) {
                this.ecNode.className = ecc;
                this.ecc = ecc;
            }
        }
    },
    
    getChildIndent: function(){
        if (!this.childIndent) {
            var buf = [];
            var p = this.node;
            while (p) {
                if (!p.isRoot || (p.isRoot && p.ownerTree.rootVisible)) {
                    if (!p.isLast()) {
                        buf.unshift('<img src="' + this.emptyIcon + '" class="x-tree-elbow-line" />');
                    }
                    else {
                        buf.unshift('<img src="' + this.emptyIcon + '" class="x-tree-icon" />');
                    }
                }
                p = p.parentNode;
            }
            this.childIndent = buf.join("");
        }
        return this.childIndent;
    },
    
    renderIndent: function(){
        if (this.rendered) {
            var indent = "";
            var p = this.node.parentNode;
            if (p) {
                indent = p.ui.getChildIndent();
            }
            if (this.indentMarkup != indent) {
                this.indentNode.innerHTML = indent;
                this.indentMarkup = indent;
            }
            this.updateExpandIcon();
        }
    }
};
