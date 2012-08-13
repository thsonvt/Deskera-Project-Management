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

import com.krawler.svnwebclient.configuration.ConfigurationException;
import java.util.UUID;

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.utils.json.util.JSONStringer;
import com.krawler.utils.json.base.JSONObject;
import com.krawler.utils.json.base.JSONException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.StorageHandler;
import java.io.*;
public class forummsgcomm {
    public static void doPost(Connection conn,java.util.List fileItems, String postid,java.sql.Timestamp docdatemod,String userid,String sendefolderpostid) throws ServiceException {
        try {
            String destinationDirectory = StorageHandler.GetDocStorePath()+ StorageHandler.GetFileSeparator()+ "attachment";
            java.io.File destDir = new java.io.File(destinationDirectory);
            String Ext = "";
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            String fileid ;
            long size;
            org.apache.commons.fileupload.DiskFileUpload fu = new org.apache.commons.fileupload.DiskFileUpload();
            fu.setSizeMax(-1);
            fu.setSizeThreshold(4096);
            fu.setRepositoryPath(destinationDirectory);
            for (java.util.Iterator i = fileItems.iterator(); i.hasNext();) {
                org.apache.commons.fileupload.FileItem fi = (org.apache.commons.fileupload.FileItem) i.next();
                if (!fi.isFormField()) {
                    String fileName = null;
                    fileid = UUID.randomUUID().toString();
                    try {
                        fileName = getFileName(fi);
//                        fileName = new String(fi.getName().getBytes(), "UTF8");
                        if (fileName.contains(".")) {
                            Ext = fileName.substring(fileName.lastIndexOf("."));
                        }
                        size = fi.getSize();
                        if (size != 0) {

                            File uploadFile = new File(destinationDirectory + "/" + fileid + Ext);
                            fi.write(uploadFile);
                            fildoc(conn,fileid,fileName,docdatemod,postid,fileid+Ext,userid,size,sendefolderpostid);
                        }
                        
                    } catch (Exception e) {
                        throw ServiceException.FAILURE("ProfileHandler.updateProfile", e);
                    }
                }
            }
        } catch (ConfigurationException ex) {
            Logger.getLogger(forummsgcomm.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
        
         public static void fildoc(Connection conn, String docid, String docname,
			 java.sql.Timestamp docdatemod,String postid,String svnName,String userid,long size,String sendefolderpostid) throws ServiceException {

		DbResults rs = null;
		// String pcid = "1";
		String sql = null;
		String currentStoreIndex = StorageHandler.GetCurrentStorageIndex();
		sql = "INSERT INTO docs(docid, docname, docdatemod, "
						+ "userid,svnname,storageindex,docsize) VALUES (?,?,?,?,?,?,?)";
		DbUtil.executeUpdate(conn, sql, new Object[] { docid, docname,
						 docdatemod, userid,svnName, currentStoreIndex,size});
                sql = "insert into docprerel (docid,userid,permission) values(?,?,?)";
                if(StringUtil.isNullOrEmpty(sendefolderpostid)){
                    DbUtil.executeUpdate(conn, sql, new Object[] { docid, postid,"7"});
                } else {
                    /*permission =7 ie document related to attachment.*/
                    DbUtil.executeUpdate(conn, sql, new Object[] { docid, sendefolderpostid,"7"});
                }
	}
         
        public static String getAttachment(Connection conn,String postid)throws ServiceException{
            String result = "";
            String sql = "select docprerel.docid,docsize,docname,svnname from docprerel inner join docs on docs.docid = docprerel.docid where docprerel.userid = ? and docprerel.permission = '7'";
            DbResults rs = DbUtil.executeQuery(conn, sql,new Object[]{postid});
            int count =1;
            while(rs.next()){
                result += "<span style=\"color:gray !important;\">"+(count++)+") "+rs.getString("docname")+" ("+getSizeKb(rs.getString("docsize"))+"K)  </span><a href='javascript:void(0)' title='Download' onclick='setDldUrl(\"fdownload.jsp?url=" + rs.getString("docid") + "&mailattch=true&dtype=attachment\")'>download</a><br><br>";
            }
            return !StringUtil.isNullOrEmpty(result)?"<br><br><br><span>Attachments</span><hr style=\"text-align:left !important; width: 40% !important; margin-left: 0px !important;\"/>"+result+"<br>":"";
        }
        
        public static String getDetails(Connection cn, String id, int type,
			String flag, String userId) throws ServiceException {

		String query = null;
		String query1 = null;
		String query2 = null;
		Object[] params = { null };
		String treeString = null;
		JSONStringer j = new JSONStringer();
		JSONObject jobj = new JSONObject();
		DbResults rs = null;
		DbResults rs1 = null;
                DbResults rs2 = null;
		try {
                    String ptext = "";
                    String attachnent = getAttachment(cn,id);
			if (flag.compareTo("forum") == 0) {
				if (type == 1) {

					query = "SELECT post_text FROM krawlerforum_topics where topic_id=?";

					
					rs = DbUtil.executeQuery(cn, query, id);

					//treeString = "{'data':[";
					while (rs.next()) {
                                                com.krawler.utils.json.base.JSONObject jtemp = new com.krawler.utils.json.base.JSONObject();
                                                ptext = java.net.URLEncoder.encode(rs.getString(1));
//                                                ptext += (!StringUtil.isNullOrEmpty(attachnent))?attachnent:"";
                                                jtemp.put("Details",ptext);
                                                jtemp.put("Attachment",java.net.URLEncoder.encode(attachnent));
                                                //jtemp.put("Details", java.net.URLEncoder.encode(rs.getString(1)));
                                                jtemp.put("ID", id);
                                                jtemp.put("flag", "Forum");
                                                jobj.append("data", jtemp);
						//treeString += j.object().key("Details").value(
								//rs.getString(1)).key("ID").value("topic" + id)
								//.key("flag").value("Forum").endObject()
								//.toString();

					}
					//treeString += "]}";

				} else if (type == 2) {

					query = "SELECT post_text FROM krawlerforum_posts where post_id=?";

					rs = DbUtil.executeQuery(cn, query, id);

					//treeString = "{'data':[";
					while (rs.next()) {
                                             com.krawler.utils.json.base.JSONObject jtemp = new com.krawler.utils.json.base.JSONObject();
                                                ptext = java.net.URLEncoder.encode(rs.getString(1));
                                                //ptext += (!StringUtil.isNullOrEmpty(attachnent))?attachnent:"";
                                                jtemp.put("Details", ptext);
                                                jtemp.put("Attachment",java.net.URLEncoder.encode(attachnent));
//                                                jtemp.put("Details", java.net.URLEncoder.encode(rs.getString(1)));
                                                jtemp.put("ID", id);
                                                jtemp.put("flag", "Forum");
                                                jobj.append("data", jtemp);
						//treeString += j.object().key("Details").value(
								//rs.getString(1)).key("ID").value(id)
								//.key("flag").value("Forum").endObject()
								//.toString();
					}

					//treeString += "]}";
				}
				query1 = "Select ifread from userForumPostRead where post_id =? and userid=? ";
				rs1 = DbUtil.executeQuery(cn, query1,
						new Object[] { id, userId });
				if (rs1.next()) {

				} else {
					query2 = "INSERT INTO userForumPostRead(post_id, userid, ifread) VALUES (?, ?, ?)";
					DbUtil.executeUpdate(cn, query2, new Object[] { id, userId,
							true });

				}

			} else if (flag.compareTo("mail") == 0) {

				query = "SELECT post_text,readflag FROM mailmessages where post_id=?";
                                rs2 = DbUtil.executeQuery(cn, query, id);
                                boolean readFlag =false;
				//treeString = "{'data':[";
				while (rs2.next()) {
                                     com.krawler.utils.json.base.JSONObject jtemp = new com.krawler.utils.json.base.JSONObject();
                                                jtemp.put("Details", java.net.URLEncoder.encode(rs2.getString(1)));
                                                jtemp.put("ID", id);
                                                jtemp.put("flag", "Forum");
                                                jtemp.put("Attachment",java.net.URLEncoder.encode(attachnent));
                                                jobj.append("data", jtemp);
                                      readFlag = rs2.getBoolean("readflag");
					//treeString += j.object().key("Details").value(
							//rs2.getString(1)).key("ID").value(id).key("flag")
							//.value("Mail").endObject().toString();
				}
                                if(!readFlag){
                                    query2 = "update mailmessages set readflag=true where post_id = ?";
                                    int rows= DbUtil.executeUpdate(cn, query2, new Object[] { id });
                                }
                                //treeString += "]}";
			}
		} catch (ServiceException ex) {
			DbPool.quietRollback(cn);
		} catch (JSONException e) {
                    throw ServiceException.FAILURE("course.gradeBook", e);
                }

		return jobj.toString();
	}
        public static String getFileName(org.apache.commons.fileupload.FileItem fileItem){
            if (fileItem.getName() == null) {
                return null;
             }
             // check for Unix-style path
            int pos = fileItem.getName().lastIndexOf("/");
            if (pos == -1) {
                 // check for Windows-style path
                 pos = fileItem.getName().lastIndexOf("\\");
             }
             if (pos != -1)  {
                 // any sort of path separator found
                 return fileItem.getName().substring(pos + 1);
             }
             else {
                 // plain name
                 return fileItem.getName();
             }
        }
        public static int getSizeKb(String size) {
		int no = ((Integer.parseInt(size)) / 1024);
		if (no >= 1)
			return no;
		else
			return 1;

	}
         public static int getmaxfilesize(Connection conn,String companyid)throws ServiceException{
            int size = 1;
            DbResults rs = null;
		// String pcid = "1";
		String sql = null;
		sql = "select maxfilesize from company where companyid = ?";
		rs = DbUtil.executeQuery(conn, sql, new Object[] { companyid});
                if(rs.next()){
                    size = rs.getInt("maxfilesize");
                }
                return size;
        }

    public static String getPostTitle(Connection conn, String topicId, String flag) {
        String postDetails = "";
        String ptext = "";
        String query = "";
        JSONObject jobj = new JSONObject();
        DbResults rs = null;
        try {
            if(flag.compareToIgnoreCase("forum") == 0) {
                query = "SELECT post_text FROM krawlerforum_topics where topic_id=?";
                rs = DbUtil.executeQuery(conn, query, topicId);

                if (rs.next()) {
                    com.krawler.utils.json.base.JSONObject jtemp = new com.krawler.utils.json.base.JSONObject();
                    ptext = java.net.URLEncoder.encode(rs.getString(1));
                    jtemp.put("Details",ptext);
                    jtemp.put("ID", topicId);
                    jtemp.put("flag", "Forum");
                    jobj.append("data", jtemp);

                } else {

                    query = "SELECT post_text FROM krawlerforum_posts where post_id=?";
                    rs = DbUtil.executeQuery(conn, query, topicId);

                    if (rs.next()) {
                        com.krawler.utils.json.base.JSONObject jtemp = new com.krawler.utils.json.base.JSONObject();
                        ptext = java.net.URLEncoder.encode(rs.getString(1));
                        jtemp.put("Details", ptext);
                        jtemp.put("ID", topicId);
                        jtemp.put("flag", "Forum");
                        jobj.append("data", jtemp);
                    }
                }
            }

        } catch (JSONException e) {
            DbPool.quietRollback(conn);

        } catch (ServiceException e) {
            DbPool.quietRollback(conn);

        } finally {
            DbPool.quietClose(conn);
        }
        return postDetails;
    }
}
