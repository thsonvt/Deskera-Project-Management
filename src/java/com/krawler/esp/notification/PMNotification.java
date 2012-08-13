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
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.portalmsg.Mail;
import com.krawler.utils.json.base.JSONException;
import java.text.ParseException;

/**
 *
 * @author Abhay
 */
public class PMNotification extends Notification {

    private String companyID;

    public PMNotification() {
        super();
    }

    public void appendMessage(String htmlMsg) {
        message += htmlMsg;
    }

    public String getCompanyID() {
        return companyID;
    }

    public void setCompanyID(String companyID) {
        this.companyID = companyID;
    }

    @Override
    public void notifyUser() throws ServiceException {
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            for (int i = 0; i < receiver.size(); i++) {
                Mail.insertMailMsg(conn, receiver.get(i).getUserName(), senderID, subject, message, "1", false, "1", "", "newmsg", "", 3, "", companyID);
            }
            conn.commit();
        } catch (ParseException ex) {
            DbPool.quietRollback(conn);
            throw ServiceException.FAILURE("Exception while sending personal message :: " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            DbPool.quietRollback(conn);
            throw ServiceException.FAILURE("Exception while sending personal message :: " + ex.getMessage(), ex);
        } finally {
            DbPool.quietClose(conn);
        }

    }
}
