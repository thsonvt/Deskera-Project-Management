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
package com.krawler.esp.notification;

import com.krawler.common.service.ServiceException;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.svnwebclient.configuration.ConfigurationException;
import javax.mail.MessagingException;

/**
 *
 * @author Abhay
 */
public class MailNotification extends Notification {

    private String htmlMsg = "";

    public MailNotification() {
        super();
    }

    public String getHtmlMsg() {
        return htmlMsg;
    }

    public void setHtmlMsg(String htmlMsg) {
        this.htmlMsg = htmlMsg;
    }

    public void appendMessage(String pmsg, String htmlMsg) {
        message += pmsg;
        this.htmlMsg += htmlMsg;
    }

    @Override
    public void notifyUser() throws ServiceException {
        int count = receiver.size();
        String rec[] = new String[receiver.size()];
        for (int i = 0; i < count; i++) {
            rec[i] = receiver.get(i).getEmailID();
        }
        try {
            if (rec.length > 0) {
                SendMailHandler.postMail(rec, subject, htmlMsg, message, senderID);
            }
        } catch (MessagingException ex) {
            throw ServiceException.FAILURE("Messaging Exception :: " + ex.getMessage(), ex);
        } catch (ConfigurationException ex) {
            throw ServiceException.FAILURE("Configuration Exception :: " + ex.getMessage(), ex);
        }
    }
}
