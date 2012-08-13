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
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import= "java.io.IOException"%>
<%@ page import= "java.sql.PreparedStatement"%>
<%@ page import= "java.sql.ResultSet"%>
<%@ page import= "java.sql.SQLException"%>
<%@ page import= "com.krawler.common.service.ServiceException"%>
<%@ page import= "com.krawler.database.DbPool"%>
<%@ page import= "com.krawler.database.DbPool.Connection"%>
<%@ page import= "com.krawler.common.util.StringUtil"%>
<%
        String msg = "";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try{
            conn = DbPool.getConnection();
            String subdomain = request.getParameter("subdomain");
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                String query = "SELECT count(*)  as count from defaultdomains where name = ?";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, subdomain);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    if (rs.getInt("count") == 0) {
                        query = "insert into defaultdomains(name) values(?)";
                        pstmt = conn.prepareStatement(query);
                        pstmt.setString(1, subdomain);
                        int t = pstmt.executeUpdate();
                        if(t != 0)
                            conn.commit();
                    }
                }
            }
            String query = "SELECT name from defaultdomains order by name";
            String data = "";
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();
            int i = 0;
            while (rs.next()) {
                if (i % 10 == 0) {
                    if (i == 0) {
                        data += "<TR>";
                    } else {
                        data += "</TR><TR>";
                    }
                }
                data += "<TD>" + rs.getString("name") + "</TD>";
                i++;
            }
            msg = "<table style='margin: auto; font-size: 0.9em' class='kwlAdminTab'>" +
                    "<tbody>" + data + "</tbody></table>";
        } catch (SQLException e) {
            System.out.println(e);
            throw ServiceException.FAILURE("SuperUser: ", e);
        } finally {
            DbPool.closeStatement(pstmt);
            DbPool.quietClose(conn);
        }
%>
<%=msg%>
