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
package com.krawler.esp.user;

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool.Connection;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Abhay
 */
public class UserDAOImpl implements UserDAO {

    private int count;

    @Override
    public String getUserID(Connection conn, String username, String companyID) throws ServiceException {
        DbResults rs = DbUtil.executeQuery(conn, "select users.userid from users inner join userlogin on users.userid=userlogin.userid where userlogin.username = ? and companyid = ?", new Object[]{username, companyID});
        if (rs.next()) {
            return rs.getString("userid");
        }
        return "";
    }

    @Override
    public int getTotalCount() {
        return count;
    }

    @Override
    public User getUser(Connection conn, String userID) throws ServiceException {
        User u = new User();
        String userquery = "select u.*, ul.* from users u inner join userlogin ul on u.userid = ul.userid where u.userid = ?";
        DbResults rs = DbUtil.executeQuery(conn, userquery, userID);
        if (rs.next()) {
            UserHelper.setObject(rs, u);
        }
        return u;
    }
}
