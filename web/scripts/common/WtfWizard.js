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
﻿// JScript File
Wtf.common.Wizard = function(config){
    Wtf.common.Wizard.constructor.superclass.call(this, config);
}

Wtf.common.Wizard = Wtf.extend(Wtf.Window, {
	loadMaskConfig: {
		'default': 'Saving...'
	},
	height: 550,
	width: 680,
    iconCls: 'iconwin',
	closable: true,
	resizable: false,
	modal: true,
	steps: null,
	previousButtonText: '&lt; '+WtfGlobal.getLocaleText('pm.common.previous'),
	nextButtonText: WtfGlobal.getLocaleText('lang.Next.text')+' &gt;',
	cancelButtonText: WtfGlobal.getLocaleText('lang.cancel.text'),
	finishButtonText: WtfGlobal.getLocaleText('lang.end.text'),
	headerConfig: {},
	stepPanelConfig: {},
	previousButton: null,
	nextButton: null,
	cancelButton: null,
	stepPanel: null,
	currentStep: -1,
	headPanel: null,
	stepCount: 0,
	initComponent: function(){
		this.initButtons();
		this.initPanels();

		var title = this.title || this.headerConfig.title;
		title = title || "";

		Wtf.apply(this, {
			title: title,
			layout: 'border',
			stepCount: this.steps.length,
			buttons: [this.previousButton, this.nextButton, this.finalButton, this.cancelButton],
			items: [this.headPanel, this.stepPanel]
		});

		this.addEvents({
            'cancel': true,
            'finish': true,
            'afterStepShow': true,
            'nextButtonClicked': true,
            'previousButtonClicked': true
        });

		Wtf.common.Wizard.superclass.initComponent.call(this);
	},
	getWizardData: function(){
		var formValues = {};
		var steps = this.steps;
		for (var i = 0, len = steps.length; i < len; i++) {
			if (steps[i].form) {
				formValues[steps[i].id] = steps[i].form.getValues(false);
			}
			else {
				formValues[steps[i].id] = {};
			}
		}
		return formValues;
	},
	switchDialogState: function(enabled, type){
		this.showLoadMask(!enabled, type);

		this.previousButton.setDisabled(!enabled);
		this.nextButton.setDisabled(!enabled);
		this.cancelButton.setDisabled(true);

		if (this.closable) {
			var ct = this.tools['close'];
			switch (enabled) {
				case true:
					this.tools['close'].unmask();
					break;

				default:
					this.tools['close'].mask();
					break;
			}
		}
	},
	showLoadMask: function(show, type){
		if (!type) {
			type = 'default';
		}

		if (show) {
			if (this.loadMask == null) {
				this.loadMask = new Wtf.LoadMask(this.body);
			}
			this.loadMask.msg = this.loadMaskConfig['type'];
			this.loadMask.show();
		}
		else {
			if (this.loadMask) {
				this.loadMask.hide();
			}
		}
	},
	initEvents: function(){
		Wtf.common.Wizard.superclass.initEvents.call(this);

		var steps = this.steps;

		for (var i = 0, len = steps.length; i < len; i++) {
			steps[i].on('show', this.onStepShow, this);
			steps[i].on('hide', this.onStepHide, this);
			steps[i].on('clientvalidation', this.onClientValidation, this);
		}
	},
	initPanels: function(){
		var steps = this.steps;
		var stepPanelConfig = this.stepPanelConfig;

		Wtf.apply(this.headerConfig, {
			steps: steps.length
		});

		this.headPanel = new Wtf.common.Wizard.Header(this.headerConfig);

		Wtf.apply(stepPanelConfig, {
			layout: new Wtf.common.Wizard.CardLayout(),
			items: steps
		});

		Wtf.applyIf(stepPanelConfig, {
			region: 'center',
			border: false,
			activeItem: 0
		});

		this.stepPanel = new Wtf.Panel(stepPanelConfig);
	},
	initButtons: function(){
		this.previousButton = new Wtf.Button({
			text: this.previousButtonText,
			disabled: true,
			minWidth: 75,
			handler: this.onPreviousClick,
			scope: this
		});

		this.nextButton = new Wtf.Button({
			text: this.nextButtonText,
			minWidth: 75,
			handler: this.onNextClick,
			scope: this
		});

		this.finalButton = new Wtf.Button({
			text: this.finishButtonText,
			minWidth: 75,
			disabled: true,
			handler: this.onFinish,
			scope: this
		});

		this.cancelButton = new Wtf.Button({
			text: this.cancelButtonText,
			handler: this.onCancelClick,
			scope: this,
			minWidth: 75
		});
	},
	onClientValidation: function(step, isValid){
		if (!isValid) {
			this.nextButton.setDisabled(true);
		} else {
			this.nextButton.setDisabled(false);
		}
	},
	onStepHide: function(step){
		if (this.stepPanel.layout.activeItem.id === step.id) {
			this.nextButton.setDisabled(true);
		}
	},
	onStepShow: function(step){
		var parent = step.ownerCt;

		var items = parent.items;

		for (var i = 0, len = items.length; i < len; i++) {
			if (items.get(i).id == step.id) {
				break;
			}
		}

		this.currentStep = i;
		this.headPanel.updateStep(i, step.title);

		if (i == len - 1) {
			this.nextButton.setDisabled(true);
		}
		else {
		    this.nextButton.setDisabled(false);
		}

		if(step.isValid()) {
			this.nextButton.setDisabled(false);
		}

		if (i == 0) {
			this.previousButton.setDisabled(true);
		}
		else {
			this.previousButton.setDisabled(false);
		}
		this.fireEvent("afterStepShow", step);
	},
	setNextButtonText: function(text){
	    this.nextButton.setText((text === undefined) ? this.nextButtonText : text);
	},
	disableNextButton: function(){
	    this.nextButton.setDisabled(true);
	},
	enableNextButton: function(){
	    this.nextButton.setDisabled(false);
	},
	disableFinalButton: function(){
	    this.finalButton.setDisabled(true);
	},
	disablePreviousButton: function(){
	    this.previousButton.setDisabled(true);
	},
	enableFinalButton: function(){
	    this.finalButton.setDisabled(false);
	},
	onCancelClick: function(){
		if (this.fireEvent('cancel', this)) {
			this.close();
		}
	},
	onFinish: function(){
		if (this.fireEvent('finish', this)) {
			this.close();
		}
	},
	onPreviousClick: function(){
        if(this.fireEvent("previousButtonClicked", this.currentStep)) {
            if (this.currentStep > 0) {
                this.stepPanel.getLayout().setActiveItem(this.currentStep - 1);
            }
        }
	},
	onNextClick: function(){
        if(this.fireEvent("nextButtonClicked", this.currentStep)) {
            if (this.currentStep == this.stepCount - 1) {
                this.onFinish();
            }
            else {
                this.stepPanel.getLayout().setActiveItem(this.currentStep + 1);
            }
        }
	}
});
Wtf.common.Wizard.Header = Wtf.extend(Wtf.BoxComponent, {
	height: 55,
	region: 'north',
	cls: "headerBackground",
	title: WtfGlobal.getLocaleText('pm.common.wizard'),
	steps: 0,
	stepText: "Step {0} of {1}: {2}",
	autoEl: {
		tag: 'div',
		cls: 'wtf-common-wizard-Header',
		children: [{
			tag: 'div',
			cls: 'wtf-common-wizard-Header-title'
		}, {
			tag: 'div',
			style: "margin-top: 18px;",
			children: [{
				tag: 'div',
				cls: 'wtf-common-wizard-Header-step'
			}/*, {
				tag: 'div',
				cls: 'wtf-ux-wiz-Header-stepIndicator-container'
			}*/]
		}]
	},
	titleEl: null,
	stepEl: null,
	imageContainer: null,
	indicators: null,
	stepTemplate: null,
	lastActiveStep: -1,
	updateStep: function(currentStep, title){
		var html = this.stepTemplate.apply({
			0: currentStep + 1,
			1: this.steps,
			2: title
		});

		this.stepEl.update(html);
		/*
		if (this.lastActiveStep != -1) {
			this.indicators[this.lastActiveStep].removeClass('wtf-ux-wiz-Header-stepIndicator-active');
		}

		this.indicators[currentStep].addClass('wtf-ux-wiz-Header-stepIndicator-active');
		*/
		this.lastActiveStep = currentStep;
	},
	onRender: function(ct, position){
		Wtf.common.Wizard.Header.superclass.onRender.call(this, ct, position); 

		this.indicators = [];
		this.stepTemplate = new Wtf.Template(this.stepText), this.stepTemplate.compile();

		var el = this.el.dom.firstChild;
		var ns = el.nextSibling;

		this.titleEl = new Wtf.Element(el);
		this.stepEl = new Wtf.Element(ns.firstChild);
		this.titleEl.update(this.title);
/*		this.imageContainer = new Wtf.Element(ns.lastChild);
		var image = null;
		for (var i = 0, len = this.steps; i < len; i++) {
			image = document.createElement('div');
			image.innerHTML = "&#160;";
			image.className = 'wtf-ux-wiz-Header-stepIndicator';
			this.indicators[i] = new Wtf.Element(image);
			this.imageContainer.appendChild(image);
		}*/
	}
});


Wtf.common.Wizard.Step = Wtf.extend(Wtf.FormPanel, {
	header: false,
	hideMode: 'display',
	initComponent: function(){
		this.addEvents('beforestephide');
		Wtf.common.Wizard.Step.superclass.initComponent.call(this);
	},
	isValid: function(){
		if (this.monitorValid) {
			return this.bindHandler();
		}

		return true;
	},
	bindHandler: function(){
		this.form.items.each(function(f){
			if (!f.isValid) {
				f.isValid = Wtf.emptyFn;
			}
		});
		Wtf.common.Wizard.Step.superclass.bindHandler.call(this);
	},
	initEvents: function(){
		var old = this.monitorValid;
		this.monitorValid = false;
		Wtf.common.Wizard.Step.superclass.initEvents.call(this);
		this.monitorValid = old;
		this.on('beforehide', this.bubbleBeforeHideEvent, this);
		this.on('beforestephide', this.isValid, this);
		this.on('show', this.onStepShow, this);
		this.on('hide', this.onStepHide, this);
	},
	bubbleBeforeHideEvent: function(){
		var ly = this.ownerCt.layout;
		var activeItem = ly.activeItem;

		if (activeItem && activeItem.id === this.id) {
			return this.fireEvent('beforestephide', this);
		}
		return true;
	},
	onStepHide: function(){
		if (this.monitorValid) {
			this.stopMonitoring();
		}
	},
	onStepShow: function(){
		if (this.monitorValid) {
			this.startMonitoring();
		}
	}
});

Wtf.common.Wizard.CardLayout = Wtf.extend(Wtf.layout.CardLayout, {
	setActiveItem: function(item){
		item = this.container.getComponent(item); 
		if (this.activeItem != item) {
			if (this.activeItem) {
				this.activeItem.hide();
			}
			// check if the beforehide method allowed to
			// hide the current item
			if (this.activeItem && !this.activeItem.hidden) {
				return;
			}

			this.activeItem = item;
			item.show();
			this.layout();
		}
	}
});
