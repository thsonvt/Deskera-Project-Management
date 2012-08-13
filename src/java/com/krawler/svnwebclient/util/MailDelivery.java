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

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.krawler.svnwebclient.web.support.MailSettingsProvider;

public class MailDelivery {
	protected static String TO = "receiverMail";
	protected static String FROM = "reporterMail";
	protected static String HOST = "reporterHost";
	protected static String PORT = "reporterPort";
	protected static String USER_NAME = "reporterName";
	protected static String PASSWORD = "reporterPass";

	public static boolean sendEmail(String body, String reportId) {
		boolean isSent = false;
		MailSettingsProvider mailProvider = MailSettingsProvider.getInstance();
		if (mailProvider.isCorrectlyInitialized()) {
			String to = mailProvider.getEmailTo();
			String from = mailProvider.getEmailFrom();
			String subject = "[" + mailProvider.getProjectName()
					+ "] Report ID:" + reportId + "- Operation Failure Report";
			String host = mailProvider.getHost();
			String port = mailProvider.getPort();
			isSent = MailDelivery.sendMail(to, from, subject, body, host, port);
		} else {
			isSent = false;
		}
		return isSent;
	}

	protected static boolean sendMail(String to, String from, String subject,
			String body, String host, String port) {

		if (host.length() == 0) {
			return true;
		}
		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);
		props.put("mail.smtp.auth", "false");

		Session session = Session.getInstance(props, null);
		session.setDebug(false);
		try {
			String message = "<html>" + "<head>" + "</head>" + "<body>"
					+ "<table cellpadding=\"0\" cellspacing=\"0\">"
					+ "<tr><td>" + body + "</td></tr>" + "</table>" + "</body>"
					+ "</html>";

			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from));
			msg.setRecipients(Message.RecipientType.TO,
					new InternetAddress[] { new InternetAddress(to) });
			msg.setSubject(subject);
			msg.setSentDate(new java.util.Date());

			message = new String(message.getBytes("UTF-8"));
			msg.setText(message);
			msg.setHeader("content-type", "text/html; charset=UTF-8");
			Transport.send(msg);
		} catch (Exception e) {
			Logger.getLogger(MailDelivery.class).error(e, e);
			return false;
		}
		return true;
	}
}
