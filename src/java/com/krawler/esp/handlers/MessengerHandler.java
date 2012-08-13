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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.utils.json.JSONArray;
import com.krawler.utils.json.JSONException;
import com.krawler.utils.json.JSONObject;
import com.krawler.utils.json.KWLJsonConverter;
import com.krawler.common.timezone.Timezone;

public class MessengerHandler {
	public static String insertChatmessages(Connection conn, String sendid,
			String receiveid, String message, int rsflag, int readflag)
			throws ServiceException {
		String query = null;
		query = "INSERT INTO chatmessages(sendid, receiveid, message, messagetimestamp, rsflag,readflag) VALUES (?, ?, ?, now(), ?,?)";
		int numRows = DbUtil.executeUpdate(conn, query, new Object[] { sendid,
				receiveid, message, rsflag, readflag });
		if (numRows == 0)
			return "Failure";
		else
			return "Success";

	}

	public static String minFromChatmessages(Connection conn, String recieveid)
			throws ServiceException {
		String tempcts = null;
		String query = null;
		Object[] params = { null };
                /*open connection*/
//		com.krawler.database.DbPool.Connection conn = null;
//		conn = DbPool.getConnection();
		query = "select min(messagetimestamp) from chatmessages where receiveid=? and readflag!=1";
		params[0] = recieveid;
		DbResults rs = DbUtil.executeQuery(conn, query, params);
		while (rs.next()) {
			tempcts = rs.getString(1);
            tempcts = Timezone.toUserTimezone(conn, tempcts, recieveid);
		}
		return tempcts;
	}
        public static Object[] getFriendListArray(Connection conn,String loginid){
            Object[] result = null;
            com.krawler.utils.json.base.JSONObject jobj = null;
            java.util.AbstractList<String> useridarr = new java.util.ArrayList<String>();            
            try {
            
                String query = null;
                
		jobj = new com.krawler.utils.json.base.JSONObject();
                /*query = "select users.userid,users.username,users.userstatus,users.emailid from users"
                +" inner join userrelations on "
                +" where luserrolemapping.roleid=6 and users.userid <> ? order by users.userstatus desc ";*/
                
                query ="select users.userid from userrelations inner join" 
                       +" users on users.userid = userrelations.userid1 or users.userid = userrelations.userid2 inner join userlogin on users.userid = userlogin.userid where users.userid != ? and (userid2 = ? or "
                       +" userid1 = ?) and userlogin.isactive = true";//and relationid =1";
                
		/*
		 * params[0]=loginid; params[1]=loginid;
		 */
		DbResults rs = DbUtil.executeQuery(conn, query,new Object[]{loginid,loginid,loginid});
                while(rs.next()){
                    useridarr.add(rs.getString("userid"));
                }
                result = useridarr.toArray();                
               } catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} 
            return result;
             
        }
        public static String getFriendlistDetail(Connection conn, String loginid)
			throws ServiceException, com.krawler.utils.json.base.JSONException {
            com.krawler.utils.json.base.JSONObject jobj = null;
            try {
            
                String query = null;
                
		jobj = new com.krawler.utils.json.base.JSONObject();
                /*query = "select users.userid,users.username,users.userstatus,users.emailid from users"
                +" inner join userrelations on "
                +" where luserrolemapping.roleid=6 and users.userid <> ? order by users.userstatus desc ";*/
                
                query ="select users.userid,concat(users.fname,' ',users.lname ) as username,users.userstatus,users.emailid,relationid from userrelations inner join" 
                       +" users on users.userid = userrelations.userid1 or users.userid = userrelations.userid2 inner join userlogin on users.userid = userlogin.userid where users.userid != ? and (userid2 = ? or "
                       +" userid1 = ?) and userlogin.isactive = true";//and relationid =1";
                
		/*
		 * params[0]=loginid; params[1]=loginid;
		 */
		DbResults rs = DbUtil.executeQuery(conn, query,new Object[]{loginid,loginid,loginid});
		String str = "";
		while (rs.next()) {
                        com.krawler.utils.json.base.JSONObject jtemp = new com.krawler.utils.json.base.JSONObject();
                            jtemp.put("userid", rs.getString(1));
                            jtemp.put("emailid", rs.getString("emailid"));
                            
                            jtemp.put("userstatus", "offline");
                            jtemp.put("relationid", rs.getInt("relationid"));
                            
                            if(rs.getInt("relationid")==1) 
                            {    
                                
                                String sql = "select count(*) as count from userrelations where userid1 = ? and userid2 = ?";
                                DbResults rs2 = DbUtil.executeQuery(conn, sql, new Object[] {loginid,rs.getObject("userid")});
                                if(rs2.next()){
                                    if(rs2.getInt("count")==0){
                                        jtemp.put("username", rs.getString(2)+" [Incoming Request]");
                                        jtemp.put("relationid", 2);
                                    }
                                    else {
                                        jtemp.put("username", rs.getString(2)+" [Invited]");
                                        //jtemp.put("relationid", 1);
                                    }    
                                }    
                            }    
                            else
                                jtemp.put("username", rs.getString(2));
                            jobj.append("data", jtemp);
					
		}
        if(jobj.length() <= 0)
            jobj.put("data", new JSONObject());
                } catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} 
                

		return jobj.toString();
        }
        public static String getUserName(String userid) throws ServiceException{
            Connection conn =null;
            String username = "";
            try{
                conn = DbPool.getConnection();
                username = AuthHandler.getAuthor(conn, userid);
            } catch(ServiceException e){
                DbPool.quietRollback(conn);
            }finally {
                DbPool.quietClose(conn);
            }
            return username;
        }
	public static String getFriendlistDetails(Connection conn, String loginid)
			throws ServiceException {
		String returnString = "{\"data\": []}";
		String query = null;
		Object[] params = { loginid, loginid };
		JSONArray opjson = new JSONArray();
		JSONObject tempjson = null;

		try {
			query = "select users.userid,userlogin.username,emailid,fname,lname,userstatus,userstatusmessages.messagetext,image from users "
					+ "left join userstatusmessages on users.userid=userstatusmessages.userid inner join userlogin on users.userid = userlogin.userid where users.userid IN (select userid1 from userrelations where userid2=? and "
					+ "relationid=3 union select userid2 from userrelations where userid1=? and relationid=3) "
					+ " and (selectedflag=1 or selectedflag is null) and userlogin.isactive = false order by userstatus DESC";
			DbResults rs = DbUtil.executeQuery(conn, query, params);
			while (rs.next()) {
				tempjson = new JSONObject();
				tempjson.put("userid", rs.getString("userid"));
				tempjson.put("username", rs.getString("username"));
				tempjson.put("emailid", rs.getString("emailid"));
				tempjson.put("fullname", rs.getString("fname") + ' '
						+ rs.getString("lname"));
				tempjson.put("userstatus", rs.getString("userstatus"));
				tempjson.put("messagetext", rs.getString("messagetext"));
				tempjson.put("image", rs.getString("image"));

				opjson.add(tempjson);
			}

			tempjson = new JSONObject();
			tempjson.put("data", opjson);
			returnString = tempjson.toString();
		} catch (JSONException ex) {
			throw ServiceException.FAILURE(
					"MessageHandler.getFriendlistDetails", ex);
		}
		return returnString;
	}

	public static String getMyMessage(Connection conn, String ulogin,
			String currentts) throws ServiceException {
		String result = "";
		PreparedStatement pstmt = null;
		PreparedStatement ustmt = null;
		ResultSet rsc = null;
		ResultSet rs = null;
		if (currentts.compareTo("999") == 0) {
			/*
			 * PreparedStatement pstmt =myConn.prepareStatement("select
			 * min(messagetimestamp) from chatmessages where receiveid=? and
			 * readflag!=1;"); pstmt.setString(1,ulogin);
			 * //pstmt.setString(1,"3"); //f6 rs=pstmt.executeQuery(); String
			 * tempcts=null; while(rs.next()) {tempcts=rs.getString(1);}
			 */
			String tempcts = minFromChatmessages(conn, ulogin);
			System.out.println("tempcts: " + tempcts);
			currentts = tempcts;
			// pstmt.close();
			// rs.close();
		}
		try {
			pstmt = conn
					.prepareStatement("select sendid,receiveid,message,messagetimestamp,rsflag,readflag from chatmessages where receiveid=? and readflag!=1 and messagetimestamp>? group by sendid,receiveid,message,messagetimestamp,rsflag,readflag order by messagetimestamp;");
			pstmt.setString(1, ulogin); // messages sent to ronin would be
			// retrived here
			pstmt.setString(2, currentts);
			int count = 0;
			rsc = pstmt.executeQuery();
			while (rsc.next()) {
				count++;
			}
			rsc.close();
			rs = pstmt.executeQuery();
			if (count == 0) {
				result = "zero";
			} else {
				KWLJsonConverter KWL = new KWLJsonConverter();
				String tp = KWL.getJsonString(rs).toString();
				result += tp; // json string
			}
			rs.close();

			ustmt = conn
					.prepareStatement("UPDATE chatmessages SET readflag=1 WHERE receiveid=? and readflag!=1 and messagetimestamp>?;");
			ustmt.setString(1, ulogin); // messages sent to ronin would be
			// retrived here
			ustmt.setString(2, currentts);
			ustmt.executeUpdate();

		} catch (SQLException ex) {
			throw ServiceException.FAILURE("FileHandler.getDocType", ex);
		} finally {
			DbPool.closeStatement(pstmt);
			DbPool.closeStatement(ustmt);
		}

		return result;
	}

}
