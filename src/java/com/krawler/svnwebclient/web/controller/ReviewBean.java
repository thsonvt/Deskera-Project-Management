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
package com.krawler.svnwebclient.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.krawler.svnwebclient.web.servlet.SendEmailServlet;

public class ReviewBean {
	protected String letterContent;

	public boolean execute(HttpServletRequest request,
			HttpServletResponse response) {
		String version = "3.0.0";
		this.letterContent = this.formReport(request
				.getParameter(SendEmailServlet.DESCRIPTION), request
				.getParameter(SendEmailServlet.EMAIL), request
				.getParameter(SendEmailServlet.NAME), request
				.getParameter(SendEmailServlet.STACKTRACE), version, request
				.getParameter("reportId"));
		return true;
	}

	public String getEncodedLetterContent() {
		String res = this.letterContent.replaceAll("&nbsp;&nbsp;&nbsp;&nbsp;",
				"\t");
		return res;
	}

	public String getLetterContent() {
		return this.letterContent;
	}

	protected String formReport(String userComment, String email, String name,
			String stackTrace, String version, String reportId) {
		String msgPlusTrace = "";
		if (stackTrace == null) {
			stackTrace = "<i>[empty]</i>";
		} else {
			stackTrace = stackTrace.replaceAll("\r", "<br>");
			String[] mas = stackTrace.split("\n");

			for (int i = 0; i < mas.length; i++) {
				msgPlusTrace += mas[i].replaceAll("\t",
						"&nbsp;&nbsp;&nbsp;&nbsp;");
			}
		}

		if (userComment != null) {
			userComment = userComment.replaceAll("\r", "<br>");
			userComment = userComment.replaceAll("\t",
					"&nbsp;&nbsp;&nbsp;&nbsp;");
		}

		msgPlusTrace += "<br><br><b>JVM Properties:</b><br>"
				+ System.getProperties().toString().replace('\n', ' ')
				+ "<br><br>";
		userComment = (userComment != null && userComment.trim().length() > 0) ? userComment
				: "<i>[empty]</i>";
		String author = (name != null ? name : "")
				+ (email != null && email.trim().length() > 0 ? " &lt;" + email
						+ "&gt;" : "");
		author = author.trim().length() > 0 ? author : "<i>[not specified]</i>";
		String messageBody = "<b>Report ID:" + reportId + "</b>"
				+ "<b><br><br>Version:</b> " + version
				+ "<br><br><b>From:</b> " + author + "<br><br>"
				+ "<b>User comment:</b><br>" + userComment
				+ "<br><br><b>Stack trace</b><br>" + msgPlusTrace;
		return messageBody;
	}
}
