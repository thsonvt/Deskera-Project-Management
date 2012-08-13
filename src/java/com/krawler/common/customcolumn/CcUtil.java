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
package com.krawler.common.customcolumn;

import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.AuthHandler;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Kamlesh kumar sah
 */
public class CcUtil {

    public static Column getObject(HttpServletRequest request) {
        Column column = CCManager.getColumnInstance();
        try {
            column.setCreator(AuthHandler.getUserid(request));
            column.setType(Integer.parseInt(request.getParameter("typeid")));
            column.setHeader(request.getParameter("header"));
            column.setModule(request.getParameter("module"));
            if (!StringUtil.isNullOrEmpty("masterdata")) {
                column.setMasterString(request.getParameter("masterdata"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enabled"))) {
                column.setEnabled(request.getParameter("enabled").equals("on") ? true : false);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("visible"))) {
                column.setVisible(request.getParameter("visible").equals("on") ? true : false);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isMandatory"))) {
                column.setMandatory(request.getParameter("isMandatory").equals("on") ? true : false);
            }
            if (!StringUtil.isNullOrEmpty("defaultValue")) {
                column.setDefaultValue(request.getParameter("defaultValue"));
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(CcUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return column;
    }

    public static Map<String, String> getAllfields(HttpServletRequest request) {
        Map<String, String> fileds = new HashMap<String, String>();
        Enumeration params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String _p = params.nextElement().toString();
            char _c = _p.charAt(1);
            if (isCustomFieldParam(_p)) {
                fileds.put(_p, request.getParameter(_p));
            }
        }
        return fileds;
    }

    public static Map<String, String> getAllfields(Map params) {
        Map<String, String> fileds = new HashMap<String, String>();
        Set<String> keys = params.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            String _p = it.next().toString();
            if (isCustomFieldParam(_p)) {
                fileds.put(_p, params.get(_p).toString());
            }
        }
        return fileds;
    }

    private static boolean isCustomFieldParam(String param) {
        char _c = param.charAt(2);
        return (param.charAt(0) == 'c' && param.length() <= 4 && (Character.isDigit(_c)));
    }
}
