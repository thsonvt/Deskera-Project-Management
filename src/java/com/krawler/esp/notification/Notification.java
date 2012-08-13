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
import com.krawler.esp.user.User;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Abhay
 */
abstract public class Notification {

    protected String senderID;
    protected List<User> receiver = new ArrayList<User>();
    protected String message;
    protected String subject;

    public Notification() {
        this("","");
        this.subject = "";
    }

    public Notification(String sender, String message) {
        this.senderID = sender;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<User> getReciever() {
        return receiver;
    }

    public void setReciever(List<User> reciever) {
        this.receiver = reciever;
    }

    public String getSender() {
        return senderID;
    }

    public void setSender(String sender) {
        this.senderID = sender;
    }

    public void addReciever(User reciever) {
        this.receiver.add(reciever);
    }

    public void removeReciever(User reciever) {
        this.receiver.remove(reciever);
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    abstract public void notifyUser() throws ServiceException;
}
