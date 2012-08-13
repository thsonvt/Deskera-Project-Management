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

import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashSet;

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.database.DbPool.Connection;

public class TagUtils {
	private static String ValidTagRegx = "(([\'\"])\\s*([\\w][\\s\\w]*)\\s*\\2)|(\\w+)";
	private static String ValidTagWithSlashRegx = "(([\'|\"])\\s*\\w+([\\s\\\\\\/]*\\w+)*\\s*\\2)|(\\w+([\\\\\\/]*\\w+)*)";

	/*
	 * 1)select from 2 altrenative 1st is
	 * (([\'|\"])\\s*\\w+([\\s\\\\\\/]*\\w+)*\\s*\\2) and 2nd is
	 * (\\w+([\\\\\\/]*\\w+)*)
	 * 
	 * 1st part of regx contains string capture by [\'|\"] this group followed
	 * by any number of whitespaces and more than one number of alphanumeric cha
	 * then string capture by ([\\s\\\\\\/]*\\w+) this group followed by any
	 * number of whitespaces then backrefreance to capture groupnumber 2 which
	 * is ([\'|\"])
	 * 
	 * 2nd part of regx capture the string which start with alphanumeric
	 * charector and it contains zero or more occurance of string captute by
	 * (\\w+([\\\\\\/]*\\w+)* this group
	 * 
	 */
	private static String CleanTag(String tagStrIn) {
		String tagStrOut = tagStrIn.trim().replaceAll("(\\s*\\/\\s*)", "/")
				.replaceAll("(^[\'\"]\\s*)|(\\s*[\'\"]$)", "'").replaceAll(
						"\\s+", " ");
		tagStrOut = (tagStrOut.contains(" ") ? tagStrOut : tagStrOut
				.replaceAll("'", ""));

		return tagStrOut.trim();
	}

	public static ArrayList<String> getTagArray(String tagIn) {
		ArrayList<String> tagArr = new ArrayList<String>();
		Pattern myPattern = Pattern.compile(ValidTagRegx);
		Matcher myMatcher = myPattern.matcher(tagIn);
		while (myMatcher.find()) {
			tagArr.add(myMatcher.group());
		}
		return tagArr;
	}
        public static ArrayList<String> getUniqTagArray(String tagIn){
            	ArrayList<String> tagArr = getTagArray(tagIn);
                HashSet<String> hs = new HashSet<String>();
                hs.addAll(tagArr);
                tagArr.clear();
                tagArr.addAll(hs);
                hs.clear();
                return tagArr;
        }

	public static String getTagId(Connection conn, String tagStr)
			throws ServiceException {
		String tagid;
		String str = CleanTag(tagStr);
		DbResults rs = DbUtil.executeQuery(conn,
				"select count(*) as asd from unitags where tagname = ?",
				new Object[] { str });
		rs.next();

		if (rs.getInt(1) == 0) {
			tagid = UUID.randomUUID().toString();
			DbUtil.executeUpdate(conn,
					"insert into unitags (tagid,tagname) values(?,?)",
					new Object[] { tagid, str });

		} else {
			rs = DbUtil.executeQuery(conn,
					"select tagid from unitags where tagname = ?",
					new Object[] { str });
			rs.next();
			tagid = rs.getString(1);
		}
		return tagid;
	}

	public static ArrayList<String> getTagWithSlashArray(String tagIn) {
		ArrayList<String> tagArr = new ArrayList<String>();
		Pattern myPattern = Pattern.compile(ValidTagWithSlashRegx);
		Matcher myMatcher = myPattern.matcher(tagIn);
		while (myMatcher.find()) {
			tagArr.add(myMatcher.group());
		}
		return tagArr;
	}

	public static void main(String[] args) {
		/*
		 * System.out.println(CleanTag("' df / ff/ f '"));
		 * System.out.println(CleanTag("' df ff fdsd '")); ArrayList<String>
		 * tagArr = getTagArray("abc ?dc #cc @ 'ff h h' ggfg$g hhh");
		 * System.out.println(tagArr.toString());
		 */
		ArrayList<String> tagArr2 = getTagWithSlashArray("'   a /b/c dc   ' 'fgdf fgdfg' cc 'ff# h h' ffaf 'fdgd' gfg 'gg/'f g' hh//h");
		System.out.println(tagArr2.toString());
		System.out.println(CleanTag("sdfsd dfddf"));
	}

	public static String checkTag(String tagName) {
		String tag = tagName.trim();
		if (tag.contains(" ") && !tag.startsWith("'"))
			tag = "'" + tag + "'";
		return tag;
	}
}
