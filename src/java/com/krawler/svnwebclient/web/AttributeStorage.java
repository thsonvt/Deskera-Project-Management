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
package com.krawler.svnwebclient.web;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

public class AttributeStorage {
	protected static AttributeStorage handler = new AttributeStorage();
	protected static String RESTRICTED_MAP = "restrictedMap";

	protected AttributeStorage() {
	}

	public void addParameter(HttpSession session, String key, Object value) {
		Map restricedMap = this.getRestrictedMap(session);
		restricedMap.put(key, value);
	}

	public Object getParameter(HttpSession session, String key) {
		Map restricedMap = this.getRestrictedMap(session);
		return restricedMap.get(key);
	}

	public static AttributeStorage getInstance() {
		return handler;
	}

	public void cleanSession(HttpSession session) {
		Map restricedMap = this.getRestrictedMap(session);
		restricedMap.clear();
		session.setAttribute(SystemInitializing.ORIGINAL_URL, null);
	}

	protected synchronized Map getRestrictedMap(HttpSession session) {
		Map restricedMap = (Map) session
				.getAttribute(AttributeStorage.RESTRICTED_MAP);
		if (restricedMap == null) {
			restricedMap = Collections.synchronizedMap(new HashMap());
			session.setAttribute(AttributeStorage.RESTRICTED_MAP, restricedMap);
		}
		return restricedMap;
	}
}
