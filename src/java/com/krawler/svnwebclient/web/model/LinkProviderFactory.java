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
package com.krawler.svnwebclient.web.model;

public class LinkProviderFactory {
	public final static String PICKER_CONTENT_MODE_VALUE = "pickerContentMode";

	public static ILinkProvider getLinkProvider(String linkType) {
		ILinkProvider provider = null;

		if (LinkProviderFactory.PICKER_CONTENT_MODE_VALUE.equals(linkType)) {
			provider = new PickerLinkProvider();
		} else {
			provider = new GeneralLinkProvider();
		}

		return provider;
	}
}
