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

package com.krawler.esp.portalmsg;

import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool;
import com.krawler.utils.json.base.JSONException;
public class forummsgdbcon {
    public static String fillTree(String offset, String limit, String groupId,
			String searchText, int col, String sortFlag,String loginid) {
		com.krawler.database.DbPool.Connection conn = null;
		String fillString = null;
		try {
			conn = DbPool.getConnection();

			fillString = Forum.fillTree(conn, offset, limit, groupId,
					searchText, col, sortFlag,loginid);
			conn.commit();
		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} catch (JSONException ex){
                    DbPool.quietRollback(conn);
                } finally {
			DbPool.quietClose(conn);
		}
		return fillString;
	}
    public static String deleteMsg(String ids[]) {
		com.krawler.database.DbPool.Connection conn = null;
		String fillString = null;
		try {
			conn = DbPool.getConnection();

			fillString = Forum.deletePost(conn, ids);
			conn.commit();
		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} finally {
			DbPool.quietClose(conn);
		}
		return fillString;
	}
     public static String insertForumPost(HttpServletRequest request)
			throws ParseException,JSONException {
		com.krawler.database.DbPool.Connection conn = null;
		String insertForumString = null;
		try {
			conn = DbPool.getConnection();
			insertForumString = Forum.insertForumPost(conn,request);
			conn.commit();
		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} finally {
			DbPool.quietClose(conn);
		}
		return insertForumString;
	}
	public static String insertForumPost(String repto, String title,
			String ptext, String u_id, String group_id, int firstReply)
			throws ParseException,JSONException {
		com.krawler.database.DbPool.Connection conn = null;
		String insertForumString = null;
		try {
			conn = DbPool.getConnection();
			insertForumString = Forum.insertForumPost(conn, repto, title,
					ptext, u_id, group_id, firstReply);
			conn.commit();
		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} finally {
			DbPool.quietClose(conn);
		}
		return insertForumString;
	}
        public static String getDetails(String id, int type, String flag,
			String userId) {
		com.krawler.database.DbPool.Connection conn = null;
		String treeString = null;
		try {
			conn = DbPool.getConnection();
			// TagHandler.addButtonTag(conn, userid, docid, tagname, groupid,
			// pcname);
			treeString = forummsgcomm.getDetails(conn, id, type, flag, userId);
                        //is SELECT
			conn.commit();
		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} finally {
			DbPool.quietClose(conn);
		}
		return treeString;
	}

    public static String getPostTitle(String topicId, String flag) {
		com.krawler.database.DbPool.Connection conn = null;
		String postTitle = null;
		try {
			conn = DbPool.getConnection();
			postTitle = forummsgcomm.getPostTitle(conn, topicId, flag);

		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);

		} finally {
			DbPool.quietClose(conn);
		}
		return postTitle;
	}

//        public static String insertNewPost(String postid, String c_id, String u_id,
//			String topic, String title, String ptxt, String repto) {
//		String result = null;
//		com.krawler.database.DbPool.Connection conn = null;
//		try {
//			conn = DbPool.getConnection();
//			result = Forum.insertNewPost(conn, postid, c_id, u_id, topic,
//					title, ptxt, repto);
//		} catch (ServiceException ex) {
//			DbPool.quietRollback(conn);
//		} finally {
//			DbPool.quietClose(conn);
//		}
//		return result;
//	}
          public static String insertMailMsg(HttpServletRequest request,String companyid)
			throws ParseException,JSONException {
		com.krawler.database.DbPool.Connection conn = null;
		String insertMailString = null;
		try {
			conn = DbPool.getConnection();
			// folderString=Mail.getFolderidForMailuser(conn,userid,foldername);
			insertMailString = Mail.insertMailMsg(conn,request,companyid);
			conn.commit();
		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} finally {
			DbPool.quietClose(conn);
		}
		return insertMailString;
	}

	public static String insertMailMsg(String to_id, String poster_id,
			String post_subject, String post_text, String folder,
			Boolean readflag, String last_folder_id, String reply_to,
			String sendflag, String post_id1, int draft, String fid,String companyid)
			throws ParseException,JSONException {
		com.krawler.database.DbPool.Connection conn = null;
		String insertMailString = null;
		try {
			conn = DbPool.getConnection();
			// folderString=Mail.getFolderidForMailuser(conn,userid,foldername);
			insertMailString = Mail.insertMailMsg(conn, to_id, poster_id,
					post_subject, post_text, folder, readflag, last_folder_id,
					reply_to, sendflag, post_id1, draft, fid,companyid);
			conn.commit();
		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} finally {
			DbPool.quietClose(conn);
		}
		return insertMailString;
	}
public static String getUserFromPost(String post_id1) {
		com.krawler.database.DbPool.Connection conn = null;
		String insertMailString = null;
		try {
			conn = DbPool.getConnection();
			// folderString=Mail.getFolderidForMailuser(conn,userid,foldername);
			insertMailString = Mail.getUserFromPost(conn, post_id1);

		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} finally {
			DbPool.quietClose(conn);
		}
		return insertMailString;
	}     
        public static String getFolderidForMailuser(String userid, String foldername) {
		com.krawler.database.DbPool.Connection conn = null;
		String folderString = null;
		try {
			conn = DbPool.getConnection();
			folderString = Mail
					.getFolderidForMailuser(conn, userid, foldername);
			conn.commit();
		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} finally {
			DbPool.quietClose(conn);
		}
		return folderString;
	}

	public static String UpdateFoldernameForMailuser(String folderid,
			String foldername, String userid) {
		com.krawler.database.DbPool.Connection conn = null;
		String updateString = null;
		try {
			conn = DbPool.getConnection();
			// StarString=Mail.StarChangeForMail(conn,post_id,flag);
			// deleteString=Mail.DeleteFoldernameForMailuser(conn,folderid);
			updateString = Mail.UpdateFoldernameForMailuser(conn, folderid,
					foldername, userid);
			conn.commit();
		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} finally {
			DbPool.quietClose(conn);
		}
		return updateString;
	}

	public static String DeleteFoldernameForMailuser(String folderid) {
		com.krawler.database.DbPool.Connection conn = null;
		String deleteString = null;
		try {
			conn = DbPool.getConnection();
			// StarString=Mail.StarChangeForMail(conn,post_id,flag);
			deleteString = Mail.DeleteFoldernameForMailuser(conn, folderid);

			conn.commit();
		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} finally {
			DbPool.quietClose(conn);
		}
		return deleteString;
	}

	public static String StarChangeForMail(String post_id, boolean flag)
			throws ServiceException, JSONException {
		com.krawler.database.DbPool.Connection conn = null;
		String StarString = null;
		try {
			conn = DbPool.getConnection();
			StarString = Mail.StarChangeForMail(conn, post_id, flag);
			conn.commit();
		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} finally {
			DbPool.quietClose(conn);
		}
		return StarString;
	}

	public static int GetMailMessagesCount(String folderid, String loginid) {
		com.krawler.database.DbPool.Connection conn = null;
		int tCount = 0;
		try {
			conn = DbPool.getConnection();
			// tCount=Mail.GetMailMessages(conn,folderid,loginid,offset,limit);
			tCount = Mail.GetMailMessagesCount(conn, folderid, loginid);
			conn.commit();
		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} finally {
			DbPool.quietClose(conn);
		}
		return tCount;
	}

	public static int GetMailMessages(String folderid, String loginid,
			String offset, String limit) {
		com.krawler.database.DbPool.Connection conn = null;
		int tCount = 0;
		try {
			conn = DbPool.getConnection();
			tCount = Mail.GetMailMessages(conn, folderid, loginid, offset,
					limit);
			conn.commit();
		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} finally {
			DbPool.quietClose(conn);
		}
		return tCount;
	}

	public static String restoreMailMsg(String post_id, String last_folder_id) {
		com.krawler.database.DbPool.Connection conn = null;
		String restoreString = null;
		try {
			conn = DbPool.getConnection();
			restoreString = Mail.restoreMailMsg(conn, post_id, last_folder_id);
			conn.commit();
			// String sql = "Select last_folder_id from mailmessages where
			// post_id ='"+post_id+"' ";
			// Statement st = cn.createStatement();
			// ResultSet rs = st.executeQuery(sql);

		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} finally {
			DbPool.quietClose(conn);
		}
		return restoreString;
	}

	public static String deleteforeverMailMsg(String post_id) {
		com.krawler.database.DbPool.Connection conn = null;
		try {
			conn = DbPool.getConnection();
			String deleteString = Mail.deleteforeverMailMsg(conn, post_id);
			conn.commit();
		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} finally {
			DbPool.quietClose(conn);
		}
		return post_id;
	}

	public static String MoveMails(String post_id, String last_folder_id,
			String dest_folder_id,String loginid) throws JSONException {
		com.krawler.database.DbPool.Connection conn = null;
		String msgString = null;
		try {
			conn = DbPool.getConnection();
			msgString = Mail.MoveMails(conn, post_id, last_folder_id,
					dest_folder_id,loginid);
			conn.commit();
		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} finally {
			DbPool.quietClose(conn);
		}
		return msgString;
	}

	public static String UpdateMailReadflag(String post_id) {
		com.krawler.database.DbPool.Connection conn = null;
		String msgString = null;
		try {
			conn = DbPool.getConnection();
			msgString = Mail.UpdateMailReadflag(conn, post_id);
			conn.commit();
		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} finally {
			DbPool.quietClose(conn);
		}
		return msgString;
	}
        public static String getMailFolders(String userid) {
		String returnString = null;
		com.krawler.database.DbPool.Connection conn = null;
		try {
			conn = DbPool.getConnection();
			returnString = Mail.getMailFolders(conn, userid);
			conn.commit();

		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} catch (JSONException ex){
                    DbPool.quietRollback(conn);
                } finally {
			DbPool.quietClose(conn);
		}
		return returnString;
	}
        public static int fetchMailCount(String loginid, String i, int offset,
			int limit) {
		int result = 0;
		com.krawler.database.DbPool.Connection conn = null;
		try {
			conn = DbPool.getConnection();
			result = Mail.fetchMailCount(conn, loginid, i, offset, limit);
		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} finally {
			DbPool.quietClose(conn);
		}
		return result;
	}
        public static String fetchMail(String loginid, String i, int offset,
			int limit) {
		String result = null;
		com.krawler.database.DbPool.Connection conn = null;
		try {
			conn = DbPool.getConnection();
			result = Mail.fetchMail(conn, loginid, i, offset, limit);
			conn.close();
		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} finally {
			DbPool.quietClose(conn);
		}
		return result;
	}
        public static int searchMailCount(String searchtext1,String searchtext, String folder_id,
			String loginid, int offset, int limit) {
		int result = 0;
		com.krawler.database.DbPool.Connection conn = null;
		try {
			conn = DbPool.getConnection();
			result = Mail.searchMailCount(conn,searchtext1, searchtext, folder_id, loginid,
					offset, limit);
		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} finally {
			DbPool.quietClose(conn);
		}
		return result;
	}

	public static String searchMail(String searchtext1, String searchtext,
			String folder_id, String loginid, int offset, int limit) {
		String result = null;
		com.krawler.database.DbPool.Connection conn = null;
		try {
			conn = DbPool.getConnection();
			result = Mail.searchMail(conn, searchtext1, searchtext, folder_id,
					loginid, offset, limit);
		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		}  catch (JSONException ex){
                        DbPool.quietRollback(conn);
                } finally {
			DbPool.quietClose(conn);
		}
		return result;
	}

}
