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
package com.krawler.svnwebclient.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class UrlUtil {

	public static String append(String parent, String child) {
		StringBuffer ret = new StringBuffer();
		ret.append(UrlUtil.trim(parent));
		String data = UrlUtil.trim(child);
		if (data.startsWith("/") && (data.length() > 1)) {
			data = data.substring(1);
		}
		if (data.length() > 0) {
			ret.append("/");
			ret.append(data);
		}
		return ret.toString();
	}

	public static String trim(String url) {
		String ret = url.trim();
		if (ret.endsWith("/")) {
			ret = ret.substring(0, url.length() - 1);
		}
		return ret;
	}

	public static String encode(String url) {
		StringBuffer res = new StringBuffer();
		try {
			if (url.indexOf(" ") != -1) {
				String[] strs = url.split(" ");
				for (int i = 0; i < strs.length; i++) {
					String str = strs[i];
					res.append(URLEncoder.encode(str, "UTF-8"));
					if (i < strs.length - 1) {
						res.append("%20");
					}
				}
			} else {
				res.append(URLEncoder.encode(url, "UTF-8"));
			}
		} catch (UnsupportedEncodingException e) {
			Logger.getLogger(UrlUtil.class).error(e, e);
		}
		return res.toString();
	}

	public static String decode(String url) {
		String ret = null;
		try {
			ret = URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Logger.getLogger(UrlUtil.class).error(e, e);
		}
		return ret;
	}

	public static List getPathChain(String path) {
		List ret = new ArrayList();
		if ((path != null) && (path.length() > 0)) {
			String pathUrl = "";
			String[] pathElements = path.split("/");
			for (int i = 0; i < pathElements.length; i++) {
				if (i == 0) {
					pathUrl = pathElements[i];
				} else {
					pathUrl += "/" + pathElements[i];
				}
				ret.add(pathUrl);
			}
		}
		return ret;
	}

	public static String getLastPathElement(String path) {
		int index = path.lastIndexOf("/");
		if (index == -1) {
			return path;
		} else {
			if (index < (path.length() - 1)) {
				return path.substring(index + 1);
			} else {
				return "";
			}
		}
	}

	public static String getPreviousFullPath(String path) {
		int index = path.lastIndexOf("/");
		if (index == -1) {
			return "";
		} else {
			return path.substring(0, index);
		}
	}

	public static String getNextLevelFullPath(String path) {
		int index = path.indexOf("/");
		if (index == -1) {
			return "";
		} else {
			if (index == (path.length() - 1)) {
				return "";
			} else {
				return path.substring(index + 1);
			}
		}
	}
}
