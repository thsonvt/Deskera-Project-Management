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
Wtf.common.customWindow = function(config){
    Wtf.apply(this.config);
    Wtf.common.customWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.common.customWindow, Wtf.Window, {
    onRender: function(config){
        Wtf.common.customWindow.superclass.onRender.call(this, config);
        this.radioButtonContainer = new Wtf.form.FieldSet({
            autoHeight: true,
            defaultType: 'radio',
            title: this.fieldLabel,
            labelWidth: 0,
            width: '96%',
            cls: 'radioButton',
            cls: 'windowFieldset',
            layoutConfig: {
                labelSeparator: ''
            }
        });
        if (this.bodytext != null) {
            this.BT = document.createElement('p');
            this.add(this.BT);
            this.BT.innerHTML = this.bodytext;
            this.BT.className = 'windowSpan';
        }
        
        if (this.imageURL != null) {
            this.titleimage = document.createElement('div');
            this.add(this.titleimage);
            this.titleimage.removeChild(this.titleimage.firstChild);
            this.titleimage.className = 'imageContainer';
            this.image = document.createElement('img');
            this.image.src = this.imageURL;
            this.image.className = 'windowimage';
            this.titleimage.appendChild(this.image);
        }
        this.add(this.radioButtonContainer);
        if (this.RadioButtons != null) {
            var cnt = 0;
            while (this.RadioButtons[cnt] != null) {
                var tempRB = new Wtf.form.Radio(this.RadioButtons[cnt]);
                this.radioButtonContainer.add(tempRB);
                cnt++;
            }
        }
    },
    
    getSelectedRadio: function(){
        var cnt = 0;
        while (this.RadioButtons[cnt] != null) {
            var tempid = this.RadioButtons[cnt].id;
            var tempRB = Wtf.getCmp(tempid);
            if (tempRB.getValue()) 
                return tempid;
            cnt++;
        }
        return null;
    }
})
