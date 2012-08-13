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
var timeStr = null;
var activecell = '`';
var x = null;
var xy = null;
var y = null;
var counter = 0;

Wtf.DataView.DragSelector = function(cfg){
    cfg = cfg ||
    {};
    var view, regions, proxy, tracker;
    var rs, bodyRegion, dragRegion = new Wtf.lib.Region(0, 0, 0, 0);
    var dragSafe = cfg.dragSafe === true;

    this.init = function(dataView){
        view = dataView;
        view.on('render', onRender);
    };

    this.disableDrag = function(flag){
        if(flag)
            tracker.destroy();
        else
            tracker.initEl(view.el);
    }

    function fillRegions(){
        rs = [];
        view.all.each(function(el){
            rs[rs.length] = el.getRegion();
        });
        bodyRegion = view.el.getRegion();
    }

    function cancelClick(){
        return false;
    }

    function onBeforeStart(e){
        return !dragSafe || e.target == view.el.dom;
    }

    function onStart(e){
        view.on('containerclick', cancelClick, view, {
            single: true
        });
        if (!proxy)
            proxy = view.el.createChild({
                cls: 'x-view-selector'
            });
        else
            proxy.setDisplayed('block');
        fillRegions();
        view.clearSelections();
    }

    function onRender(view){
        if(cfg.projDrag) {
            tracker = new Wtf.dd.DragTracker({
                onBeforeStart: onBeforeStart,
                onStart: onStart,
                onDrag: onDrag,
                onEnd: onEnd
            });
        }
        tracker.initEl(view.el);
    }
    if(cfg.projDrag) {
        var dragContainer = cfg.contid;
        function onDrag(e){
            var gridObj = Wtf.getCmp(dragContainer).containerPanel.editGrid;
            gridObj.dragFlag = true;
            var xy = tracker.getXY();
            var startXY = tracker.startXY;
            var Ypos = Wtf.get(dragContainer).getY();
            Ypos += gridObj.dragOffset;
            startXY[1] = Ypos;
            var y = Ypos;
            var x = Math.min(startXY[0], xy[0]);
            var w = Math.abs(startXY[0] - xy[0]);
            var h = 10;
            dragRegion.left = x;
            dragRegion.top = y;
            dragRegion.right = x + w;
            dragRegion.bottom = y + h;
            dragRegion.constrainTo(bodyRegion);
            proxy.setRegion(dragRegion);
            for (var i = 0, len = rs.length; i < len; i++) {
                var r = rs[i], sel = dragRegion.intersect(r);
                if (sel && !r.selected) {
                    r.selected = true;
                    view.select(i, true);
                }
                else 
                    if (!sel && r.selected) {
                        r.selected = false;
                        view.deselect(i);
                    }
            }
        }

        function onEnd(e){
            if (proxy) {
                var gridObj = Wtf.getCmp(dragContainer).containerPanel.editGrid;
                var startXY = tracker.startXY;
                var container = Wtf.getCmp(dragContainer);
                if(container.containerPanel.projScale == 'day')
                    var TaskminWidth = container.TaskminWidth;
                else
                    var TaskminWidth = 5;
                var rightPos = Wtf.get(proxy.id).getRight(true);
                var leftPos = Wtf.get(proxy.id).getLeft(true);
                if ((rightPos - leftPos) > 5) {
                    leftPos = parseInt(leftPos / TaskminWidth) * TaskminWidth;
                    if ((rightPos / TaskminWidth) > parseInt(rightPos / TaskminWidth)) {
                        rightPos = parseInt(rightPos / TaskminWidth) * TaskminWidth + TaskminWidth
                    }
                    else 
                        rightPos = parseInt(rightPos / TaskminWidth) * TaskminWidth;
                    var width = rightPos - leftPos;
                    var diff = parseInt(leftPos / TaskminWidth);
                    var stdate = new Date(container.StartDate)
                    stdate = stdate.add(Date.DAY, (diff))
                    var enddate = stdate.add(Date.DAY, parseInt(width / TaskminWidth) - 1)
                    if(stdate > Date.parseDate(gridObj.projStartDate, 'Y-m-j H:i:s'))
                        container.fireEvent('insertProxyPanel',stdate,enddate);
                }
                proxy.setDisplayed(false);
            }
        }
    }
};
