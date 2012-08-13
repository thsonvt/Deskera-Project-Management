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

public class Encoder {

	public static final String CHARSET_UTF8 = "UTF-8";

	private Encoder() {
		super();
	}

	/**
	 * Encodes the string as 7-bit ASCII string. Non-printing and non 7-bit
	 * characters are encoded in hexadecimal using '%' as encoding flag.
	 * 
	 * @param s
	 *            string to encode
	 */
	public static String encodeUTF8ASCII(String s) {
		try {
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < s.length(); i++) {
				char ch = s.charAt(i);
				if (ch > 31 && ch < 128) {
					buffer.append(ch);
				} else {
					byte[] bytes = new String(new char[] { ch })
							.getBytes(CHARSET_UTF8);
					for (int j = 0; j < bytes.length; j++) {
						buffer.append('%' + Integer
								.toHexString(bytes[j] & 0xff));
					}
				}
			}
			return buffer.toString();
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}
