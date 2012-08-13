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

import javax.mail.*;
import javax.mail.internet.*;

import com.krawler.svnwebclient.configuration.ConfigurationException;
import com.krawler.svnwebclient.configuration.ConfigurationProvider;

import java.util.*;

public class SendMailHandler {
	
	private static String getSMTPPath() throws ConfigurationException {
		return ConfigurationProvider.getInstance().getSMTPPath();
	}
	
	private static String getSMTPPort() throws ConfigurationException {
		return ConfigurationProvider.getInstance().getSMTPPort();
	}

	public static void postMail(String recipients[], String subject,
			String htmlMsg, String plainMsg, String from) throws MessagingException,
			ConfigurationException {
		boolean debug = false;

		// Set the host smtp address
		Properties props = new Properties();
		props.put("mail.smtp.host", getSMTPPath());
		props.put("mail.smtp.port", getSMTPPort());

		// create some properties and get the default Session
		Session session = Session.getDefaultInstance(props, null);
		session.setDebug(debug);

		// create a message
		MimeMessage msg = new MimeMessage(session);

		// set the from and to address
		InternetAddress addressFrom = new InternetAddress(from);
		msg.setFrom(addressFrom);

		InternetAddress[] addressTo = new InternetAddress[recipients.length];
		for (int i = 0; i < recipients.length; i++) {
			addressTo[i] = new InternetAddress(recipients[i].trim().replace(" ", "+"));
		}
		msg.setRecipients(Message.RecipientType.TO, addressTo);

		// Setting the Subject and Content Type
		msg.setSubject(subject);

		Multipart multipart = new MimeMultipart("alternative");

		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setContent(plainMsg, "text/plain");
		multipart.addBodyPart(messageBodyPart);

		messageBodyPart = new MimeBodyPart();
		messageBodyPart.setContent(htmlMsg, "text/html");
		multipart.addBodyPart(messageBodyPart);

		msg.setContent(multipart);
		Transport.send(msg);
	}
}
