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
package com.krawler.common.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpUtils;

public class URLUtil {

    public static String baseForumURL = com.krawler.esp.utils.ConfigReader.getinstance().get("forum_base_url");
    public static String baseUrlFormat = com.krawler.esp.utils.ConfigReader.getinstance().get("base_urlformat");
    private static String urlFormatProtected = com.krawler.esp.utils.ConfigReader.getinstance().get("base_urlformat_protected");
    private static String urlFormatUnprotected = com.krawler.esp.utils.ConfigReader.getinstance().get("base_urlformat_unprotected");

    public static String getDomainURL(String subdomain, boolean isProtected) {
        String baseURLFormat = urlFormatUnprotected;
        if (isProtected) {
            baseURLFormat = urlFormatProtected;
        }
        String uri = String.format(baseURLFormat, subdomain);
        return uri;
    }

    public static String getPageURL(HttpServletRequest request, String pagePathFormat) {
        String subdomain = request.getParameter("cdomain");
        return getPageURL(request, pagePathFormat, subdomain);
    }

    public static String getPageURL(HttpServletRequest request, String pagePathFormat, String subdomain) {
        String path = HttpUtils.getRequestURL(request).toString();
        String servPath = request.getServletPath();
        String uri = path.replace(servPath, "/" + String.format(pagePathFormat, subdomain));
        return uri;
    }

    public static String getDomainName2(HttpServletRequest request) {
        String companyName = request.getParameter("cdomain");
        return companyName;
    }
}
