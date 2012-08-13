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
Wtf.common.WtfButton = function(config){
    Wtf.common.WtfButton.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.common.WtfButton, Wtf.Component, {
    width: 72,
    height: 91,
    imgWidth: 35,
    imgHeight: 35,
    initialized: false,
    imgObj: null,
    zoomLevel: 0.11,
    hoverWidth: null,
    hoverHeight: null,
    diffX: null,
    diffY: null,
    flag: 0,
    imgX: 0,
    imgY: 0,
    initComponent: function(){
        Wtf.common.WtfButton.superclass.initComponent.call(this);
        this.addEvents({
            "clicked": true
        });
    },
    onRender: function(){
        var chek = (this.enableHoverShadow == true) ? true : false;
        this.elDom = Wtf.get(this.renderTo).createChild({
            tag: 'div',
            cls: (chek) ? 'wtfbutton' : 'wtfbuttonhm'
        });
        this.image = Wtf.get(this.elDom.dom.appendChild(document.createElement('img')));
        this.label = Wtf.get(this.elDom.dom.appendChild(document.createElement('span')));

        this.image.dom.src = this.imgSrc;
        this.image.dom.alt = this.caption;

//        this.imgX = Math.ceil(this.width / 2 - this.imgWidth / 2);
        this.imgY = Math.ceil(this.width / 2 - this.imgHeight / 2 - 15);
        this.image.setXY([this.image.getX(), this.image.getY() + this.imgY + 15]);

//        if(chek) {
//            this.labelShadow = Wtf.get(this.elDom.dom.appendChild(document.createElement('span')));
//            this.shadow = new Wtf.common.Shadow({
//                offset: 5
//            });
//            this.shadow.show(this.image);
//        }
//        this.elDom.addListener('mouseover', this.onHover, this);
//        this.elDom.addListener('mouseout', this.onOut, this);
        this.label.dom.innerHTML = this.caption;
        this.label.dom.className = "label";
        this.elDom.addListener('click', this.onClick, this);
        
        this.elDom.addListener('click', this.onClick, this);
        
        this.imgObj = this.image;
//        if(chek){
//            this.shadow.realign(this.imgX, this.imgY + 15, this.imgWidth, this.imgHeight);
//        }
//        this.hoverWidth = Math.ceil(this.imgWidth + this.imgWidth * this.zoomLevel);
//        this.hoverHeight = Math.ceil(this.imgHeight + this.imgWidth * this.zoomLevel);
//        this.diffX = Math.ceil(this.hoverWidth - this.imgWidth);
//        this.diffY = Math.ceil(this.hoverHeight - this.imgHeight);
//        this.imgX = this.imgObj.getX();
//        this.imgY = this.imgObj.getY();
//        this.topMove = this.diffX / 2;
//        this.leftMove = this.diffY / 2;

        this.elDom.dom.style.display = "none";
    },
//    onHover: function(e, el, opt){
//        var idom = this.imgObj.dom;
//        idom.style.left = (parseInt(idom.style.left.split("px")[0]) - 2) + "px";
//        idom.style.top = (parseInt(idom.style.top.split("px")[0]) - 2) + "px";
//    },
//    onOut: function(e, el, opt){
//        var idom = this.imgObj.dom;
//        idom.style.left = (parseInt(idom.style.left.split("px")[0]) + 2) + "px";
//        idom.style.top = (parseInt(idom.style.top.split("px")[0]) + 2) + "px";
//    },
    onClick: function(e, el, opt){
        e.preventDefault();
        mainPanel.loadTab(this.href, "   " + this.id, this.caption, "navareadashboard", this.tabtype, true);
    },
    addData: function(imgSrc, caption, id, href, tabtype){
        this.elDom.dom.style.display = "block";
        this.elDom.dom.style.textAlign = "center";
        if(tabtype == 2)
            imgSrc = getSizedImagePath(imgSrc, 100);
        this.image.dom.src = imgSrc;
        this.image.dom.alt = caption;
        this.id = id;
        this.href = href;
        this.tabtype = tabtype;
        this.label.dom.innerHTML = Wtf.util.Format.ellipsis(caption, 20);
//        if(this.enableHoverShadow == true){
////            this.labelShadow.dom.innerHTML = Wtf.util.Format.ellipsis(caption, 20);
//        } else {
//            this.image.dom.style.left = 17 + "px";
//            this.image.dom.style.top = 16 + "px";
//        }
        this.caption = caption;
        
    },
    removeButton: function(){
        this.elDom.dom.parentNode.removeChild(this.elDom.dom);
    },
    hideButton: function(){
        this.elDom.dom.style.display = "none";
    }
})

Wtf.reg('wtfbutton', Wtf.common.WtfButton);
