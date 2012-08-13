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

import java.util.Random;

public class StringId {

	public static final char ID_SEPARATOR = '-';

	private final static char[] FIRST_CHAR = { 'A', 'B', 'C', 'D', 'E', 'F',
			'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
			'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

	private final static char[] LETTERS_DIGITS = { 'A', 'B', 'C', 'D', 'E',
			'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
			'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4',
			'5', '6', '7', '8', '9' };

	public static String generateRandom(String prefix, int length) {
		if (prefix == null) {
			return generateRandom(length);
		} else {
			return prefix + ID_SEPARATOR + generateRandom(length);
		}
	}

	public static String generateRandom(int length) {
		if (length == 0) {
			return new String();
		}

		Random random = new Random();
		StringBuffer str = new StringBuffer();

		str.append(FIRST_CHAR[random.nextInt(FIRST_CHAR.length)]);

		for (int i = 1; i < length; i++) {
			str.append(LETTERS_DIGITS[random.nextInt(LETTERS_DIGITS.length)]);
		}
		return str.toString();
	}

	public static boolean isStringId(String strId) {
		int pos = strId.lastIndexOf(ID_SEPARATOR);
		if (pos != -1) {
			strId = strId.substring(pos + 1);
		}
		for (int i = 0; i < strId.length(); i++) {
			char ch = strId.charAt(i);
			if (!Character.isDigit(ch) && !Character.isUpperCase(ch)) {
				return false;
			}
		}
		return true;
	}

}
