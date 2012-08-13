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
package com.krawler.esp.handlers;

import com.krawler.common.locale.LocaleResolver;
import com.krawler.common.locale.LocaleUtils;
import com.krawler.common.util.Log;
import com.krawler.common.util.LogFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SessionHandler {     

    private static Log log = LogFactory.getLog(SessionHandler.class);
    
    public SessionHandler() {
	}
    
	public static boolean isValidSession(HttpServletRequest request,
			HttpServletResponse response) {
		boolean bSuccess = false;
		try {
			if (request.getSession().getAttribute("initialized") != null) {
				bSuccess = true;
			}
		} catch (Exception ex) {
		}
		return bSuccess;
	}
        
	public boolean validateSession(HttpServletRequest request,
			HttpServletResponse response) {
		return SessionHandler.isValidSession(request, response);
	}

	public void createUserSession(HttpServletRequest request,
			HttpServletResponse response, String uname, String userid, 
            String companyid, String company) {
		HttpSession session = request.getSession(true); 
		session.setAttribute("username", uname);
		session.setAttribute("userid", userid);
		session.setAttribute("companyid", companyid);
		session.setAttribute("company", company);
		session.setAttribute("initialized", "true");
	}

	public void destroyUserSession(HttpServletRequest request,
			HttpServletResponse response) {
		request.getSession().invalidate();
	}

    public void setLocale(HttpServletRequest request, HttpServletResponse response, String newLocale) {
        if (newLocale != null) {
            LocaleResolver localeResolver = LocaleUtils.getLocaleResolver(request);
            if (localeResolver == null) {
                log.debug("No LocaleResolver found: not in a DispatcherServlet request?");
                return;
}
            localeResolver.setLocale(request, response, LocaleUtils.parseLocaleString(newLocale));
        }
    }
}
