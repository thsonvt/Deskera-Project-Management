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
package com.krawler.svnwebclient.util.contentencoding;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.krawler.svnwebclient.configuration.ConfigurationProvider;
import com.krawler.svnwebclient.web.support.RequestParameters;
import com.krawler.svnwebclient.web.support.State;

public class ContentEncodingHelper {

	public static String encodeBytes(byte[] bytes, String encoding)
			throws UnsupportedEncodingException, IOException {
		ByteArrayInputStream fis = new ByteArrayInputStream(bytes);
		UnicodeInputStream uin = new UnicodeInputStream(fis, encoding);
		uin.getEncoding();// skip bom in UTF
		InputStreamReader in = null;

		try {
			StringBuffer res = new StringBuffer();
			if (encoding == null) {
				in = new InputStreamReader(uin);
			} else {
				in = new InputStreamReader(uin, encoding);
			}
			int f;
			while ((f = in.read()) != -1) {
				res.append((char) f);
			}

			// System.out.println(res.toString());
			return res.toString();
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	public static String getEncoding(State state) {
		String encoding = state.getRequest().getParameter(
				RequestParameters.CHARACTER_ENCODING);
		if (encoding == null) {
			encoding = (String) state.getSession().getAttribute(
					RequestParameters.DEFAULT_CHARACTER_ENCODING);
			if (encoding == null) {
				encoding = ConfigurationProvider.getInstance()
						.getDefaultEncoding();
			}
		} else {
			state.getSession().setAttribute(
					RequestParameters.DEFAULT_CHARACTER_ENCODING, encoding);
		}
		return encoding;
	}

	public static Collection getCharacterEncodings() {
		Set encodings = ConfigurationProvider.getInstance()
				.getCharacterEncodings();
		encodings.add(ConfigurationProvider.getInstance().getDefaultEncoding());

		List res = new ArrayList();
		res.addAll(encodings);
		Collections.sort(res, new Comparator() {
			public int compare(Object o1, Object o2) {
				String s1 = (String) o1;
				String s2 = (String) o2;
				return s1.compareTo(s2);
			}
		});
		return res;
	}

	public static boolean isSelectedCharacterEncoding(State state,
			String encoding) {
		boolean isSelectedCharacterEncoding = false;

		String defaultEncoding = null;
		defaultEncoding = (String) state.getSession().getAttribute(
				RequestParameters.DEFAULT_CHARACTER_ENCODING);
		if (defaultEncoding == null) {
			defaultEncoding = ConfigurationProvider.getInstance()
					.getDefaultEncoding();
		}
		if (encoding.equals(defaultEncoding)) {
			isSelectedCharacterEncoding = true;
		}
		return isSelectedCharacterEncoding;
	}
}
