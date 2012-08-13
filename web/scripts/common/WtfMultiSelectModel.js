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
Wtf.grid.MultiSelectionModel = function(config){
    Wtf.apply(this, config);
    this.selections = new Wtf.util.MixedCollection(false, function(o){
        return o.id;
    });
    this.last = false;
    this.lastActive = false;
    this.selection = null;

    this.addEvents({
    
        "beforecellselect": true,
        
        "cellselect": true,
        
        "selectionchange": true,
        
        "beforerowselect": true,
        
        "rowselect": true,
        
        "rowdeselect": true,
        
        "multiplerowSelect": true
    });
    
    Wtf.grid.MultiSelectionModel.superclass.constructor.call(this);
};

Wtf.extend(Wtf.grid.MultiSelectionModel, Wtf.grid.AbstractSelectionModel, {

    singleSelect: false,
    initEvents: function(){
    
        if (!this.grid.enableDragDrop && !this.grid.enableDrag) {
            //  this.grid.on("rowmousedown", this.handleMouseDown, 0,this);
        } else {
            this.grid.on("rowclick", function(grid, rowIndex, e){
                if (e.button === 0 && !e.shiftKey && !e.ctrlKey) {
                    this.selectRow(rowIndex, false);
                    grid.view.focusRow(rowIndex);
                }
            }, this);
        }
        
        this.rowNav = new Wtf.KeyNav(this.grid.getGridEl(), {
            "up": function(e){
                if (!e.shiftKey)
                    this.selectPrevious(e.shiftKey);
                else if (this.last !== false && this.lastActive !== false) {
                    var last = this.last;
                    this.selectRange(this.last, this.lastActive - 1);
                    this.grid.getView().focusRow(this.lastActive);
                    if (last !== false)
                        this.last = last;
                } else
                    this.selectFirstRow();
            },
            "down": function(e){
                if (!e.shiftKey)
                    this.selectNext(e.shiftKey);
                else 
                    if (this.last !== false && this.lastActive !== false) {
                        var last = this.last;
                        this.selectRange(this.last, this.lastActive + 1);
                        this.grid.getView().focusRow(this.lastActive);
                        if (last !== false)
                            this.last = last;
                    } else
                        this.selectFirstRow();
            },
            scope: this
        });
        this.grid.on("cellmousedown", this.handleMouseDown, this);
        this.grid.getGridEl().on(Wtf.isIE ? "keydown" : "keypress", this.handleKeyDown, this);
        var view = this.grid.view;
        view.on("refresh", this.onViewChange, this);
        view.on("rowupdated", this.onRowUpdated, this);
        view.on("beforerowremoved", this.clearSelections, this);
        view.on("beforerowsinserted", this.clearSelections, this);
        if (this.grid.isEditor)
            this.grid.on("beforeedit", this.beforeEdit, this);
    },

    beforeEdit: function(e){
        this.select(e.row, e.column, false, true, e.record);
    },

    onRowUpdated: function(v, index, r){
        if (this.selection && this.selection.record == r)
            v.onCellSelect(index, this.selection.cell[1]);
    },

    onViewChange: function(){
        this.clearSelections(true);
    },

    getCount: function(){
        return this.selections.length;
    },

    selectFirstRow: function(){
        this.selectRow(0);
    },

    selectLastRow: function(keepExisting){
        this.selectRow(this.grid.store.getCount() - 1, keepExisting);
    },

    selectNext: function(keepExisting){
        if (this.hasNext()) {
            this.selectRow(this.last + 1, keepExisting);
            this.grid.getView().focusRow(this.last);
        }
    },

    selectPrevious: function(keepExisting){
        if (this.hasPrevious()) {
            this.selectRow(this.last - 1, keepExisting);
            this.grid.getView().focusRow(this.last);
        }
    },

    hasNext: function(){
        return this.last !== false && (this.last + 1) < this.grid.store.getCount();
    },

    hasPrevious: function(){
        return !!this.last;
    },

    getSelections: function(){
        return [].concat(this.selections.items);
    },

    getSelected: function(){
        return this.selections.itemAt(0);
    },

    each: function(fn, scope){
        var s = this.getSelections();
        for (var i = 0, len = s.length; i < len; i++) {
            if (fn.call(scope || this, s[i], i) === false)
                return false;
        }
        return true;
    },

    getSelectedCell: function(){
        return this.selection ? this.selection.cell : null;
    },

    deselectRow: function(index, preventViewNotify){
        if (this.locked) 
            return;
        if (this.last == index)
            this.last = false;
        if (this.lastActive == index)
            this.lastActive = false;
        var r = this.grid.store.getAt(index);
        this.selections.remove(r);
        if (!preventViewNotify)
            this.grid.getView().onRowDeselect(index);
        this.fireEvent("rowdeselect", this, index, r);
        this.fireEvent("selectionchange", this);
    },

    clearSelections: function(preventNotify){
        fast = false;
        if (this.locked) 
            return;
        if (fast !== true) {
            var ds = this.grid.store;
            var s = this.selections;
            s.each(function(r){
                this.deselectRow(ds.indexOfId(r.id), preventNotify);
            }, this);
            s.clear();
        } else
            this.selections.clear();
        this.last = false;
        var s = this.selection;
        if (s) {
            if (preventNotify !== true)
                this.grid.view.onCellDeselect(s.cell[0], s.cell[1]);
            this.selection = null;
            this.fireEvent("selectionchange", this, null);
        }
    },

    hasSelection: function(){
        return this.selection ? true : false;
    },

    isSelected: function(index){
        var r = typeof index == "number" ? this.grid.store.getAt(index) : index;
        return (r && this.selections.key(r.id) ? true : false);
    },

    isIdSelected: function(id){
        return (this.selections.key(id) ? true : false);
    },

    handleMouseDown: function(g, row, cell, e){
        if (e.button !== 0 || this.isLocked()) {
            return;
        };
        if (cell == 0) {
            var view = this.grid.getView();
            if (e.shiftKey && this.last !== false) {
                var last = this.last;
                this.selectRange(last, row, e.ctrlKey);
                this.fireEvent("multiplerowSelect", last, row);
                this.last = last;
                view.focusRow(row);
            }
            else {
                var isSelected = this.isSelected(row);
                if (e.ctrlKey && isSelected)
                    this.deselectRow(row);
                else if (!isSelected || this.getCount() > 1) {
                    this.selectRow(row, e.ctrlKey || e.shiftKey);
                    this.fireEvent("multiplerowSelect", last, row);
                    view.focusRow(row);
                }
            }
        }
        else
            this.select(row, cell);
    },

    selectRange: function(startRow, endRow, keepExisting){
        if (this.locked) 
            return;
        if (!keepExisting)
            this.clearSelections();
        if (startRow <= endRow) {
            for (var i = startRow; i <= endRow; i++)
                this.selectRow(i, true);
        } else {
            for (var i = startRow; i >= endRow; i--)
                this.selectRow(i, true);
        }
    },

    select: function(rowIndex, colIndex, preventViewNotify, preventFocus, r){
        if (this.fireEvent("beforecellselect", this, rowIndex, colIndex) !== false) {
            this.clearSelections();
            r = r || this.grid.store.getAt(rowIndex);
            this.selection = {
                record: r,
                cell: [rowIndex, colIndex]
            };
            if (!preventViewNotify) {
                var v = this.grid.getView();
                v.onCellSelect(rowIndex, colIndex);
                if (preventFocus !== true) {
                    v.focusCell(rowIndex, colIndex);
                }
            }
            this.fireEvent("cellselect", this, rowIndex, colIndex);
            this.fireEvent("selectionchange", this, this.selection);
        }
    },

    isSelectable: function(rowIndex, colIndex, cm){
        return !cm.isHidden(colIndex);
    },

    handleKeyDown: function(e){
        if (!e.isNavKeyPress())
            return;
        var g = this.grid, s = this.selection;
        var sm = this;
        if (!s) {
            e.stopEvent();
            if(!sm.getSelections()[0]){
                var cell = g.walkCells(0, 0, 1, this.isSelectable, this);
                if (cell)
                    this.select(cell[0], cell[1]);
                return;
            } else {
                return;
            }
        }
        var walk = function(row, col, step){
            return g.walkCells(row, col, step, sm.isSelectable, sm);
        };
        var k = e.getKey(), r = s.cell[0], c = s.cell[1];
        var newCell;
        switch (k) {
            case e.TAB:
                if (e.shiftKey) {
                    newCell = walk(r, c - 1, -1);
                    g.startEditing(r, c - 1);
                } else {
                    newCell = walk(r, c + 1, 1);
                    g.startEditing(r, c + 1);
                }
                break;
            case e.DOWN:
                if(!e.shiftKey){
                    newCell = walk(r + 1, c, 1);
                    if ((r + 1) != this.grid.store.getCount())
                        g.startEditing(r + 1, c);
                    else
                        e.stopEvent();
                    this.selectFirstRow();
                }
                break;
            case e.UP:
                if(!e.shiftKey){
                    newCell = walk(r - 1, c, -1);
                    if ((r) == 0)
                        e.stopEvent();
                    else
                        g.startEditing(r - 1, c);
                }
                break;
            case e.RIGHT:
//                if((c + 1) < g.colModel.config.length){
//                    newCell = walk(r, c + 1, 1);
//                    g.startEditing(r, c + 1);
//                }
                break;
            case e.LEFT:
//                if(c > 0){
//                    newCell = walk(r, c - 1, -1);
//                    g.startEditing(r, c - 1);
//                }
                break;
            case e.ENTER:
                newCell = walk(r + 1, c, 1);
                g.startEditing(r + 1, c);
                if (g.isEditor && !g.editing) {
                    e.stopEvent();
                    return;
                }
                break;
            default:
        };
        if (newCell) {
            this.select(newCell[0], newCell[1]);
            e.stopEvent();
        }
    },

    onRowDeselect: function(row){
        this.removeRowClass(row, "x-grid3-row-selected");
    },

    acceptsNav: function(row, col, cm){
        return !cm.isHidden(col) && cm.isCellEditable(col, row);
    },

    onEditorKey: function(field, e){

        var s = this.selection;
        var sm = this;
        var k = e.getKey(), newCell, g = this.grid, ed = g.activeEditor, r = ed.row, c = ed.col, cm = g.getColumnModel();
        var walk = function(row, col, step){
            return g.walkCells(row, col, step, sm.isSelectable, sm);
        };
        switch (k) {
            case e.TAB:
                if (e.shiftKey) {
                    newCell = walk(r, c - 1, -1);
                    g.startEditing(r, c - 1);
                } else {
                    if(cm.getColumnCount() > c+1){
                        newCell = walk(r, c + 1, 1);
                        g.startEditing(r, c + 1);
                    } else {
                        newCell = walk(r, c + 1, 1);
                        g.startEditing(r + 1, 3);
                    }
                }
                break;
            case e.DOWN:
                if(!e.shiftKey){
                    newCell = walk(r + 1, c, 1);
                    if ((r + 1) != this.grid.store.getCount())
                        g.startEditing(r + 1, c);
                    else
                        e.stopEvent();
                }
                break;
            case e.UP:
                if(!e.shiftKey){
                    newCell = walk(r - 1, c, -1);
                    if ((r) == 0)
                        e.stopEvent();
                    else
                        g.startEditing(r - 1, c);
                }
                break;
            case e.RIGHT:
//                if((c + 1) < g.colModel.config.length){
//                    newCell = walk(r, c + 1, 1);
//                    g.startEditing(r, c + 1);
//                }
                break;
            case e.LEFT:
//                if(c > 0){
//                    newCell = walk(r, c - 1, -1);
//                    g.startEditing(r, c - 1);
//                }
                break;
            case e.ESC:
                ed.cancelEdit();
//                g.startEditing(r, c);
                break;
            case e.ENTER:
                if(g.store.getAt(r+1) !== undefined) {
                    newCell = walk(r + 1, c, 1);
                    g.startEditing(r + 1, c);
                } else
                    g.stopEditing();
                break;
        }
        
    },
    selectRow: function(index, keepExisting, preventViewNotify){
        if (this.locked || (index < 0 || index >= this.grid.store.getCount())) 
            return;
        var r = this.grid.store.getAt(index);
        if (this.fireEvent("beforerowselect", this, index, keepExisting, r) !== false) {
            if (!keepExisting || this.singleSelect)
                this.clearSelections();
            this.selections.add(r);
            this.last = this.lastActive = index;
            if (!preventViewNotify)
                this.grid.getView().onRowSelect(index);
            this.fireEvent("rowselect", this, index, r);
            this.fireEvent("selectionchange", this);
        }
    },

    selectRecords : function(records, keepExisting){
        if(!keepExisting){
            this.clearSelections();
        }
        var ds = this.grid.store;
        for(var i = 0, len = records.length; i < len; i++){
            this.selectRow(ds.indexOf(records[i]), true);
        }
    }
});
