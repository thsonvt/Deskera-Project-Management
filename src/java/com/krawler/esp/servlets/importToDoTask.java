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
package com.krawler.esp.servlets;

import com.krawler.common.locale.MessageSourceProxy;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import java.text.ParseException;
import java.util.UUID;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileUploadException;
import com.krawler.common.util.KrawlerLog;
import com.krawler.common.util.StringUtil;
import com.krawler.svnwebclient.configuration.ConfigurationException;
import com.krawler.utils.json.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.List;
import java.util.logging.Level;
import java.io.*;
import java.io.IOException;
import com.krawler.common.util.CsvReader;
import com.krawler.common.util.KWLErrorMsgs;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.handlers.AuditTrail;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.handlers.dateFormatHandlers;
import com.krawler.esp.handlers.projdb;
import com.krawler.esp.handlers.todolist;
import java.util.Date;

/**
 *
 * @author krawler
 */
public class importToDoTask extends HttpServlet {

    private String dateFormat = "";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        String doAction = request.getParameter("action");
        Connection conn = null;

        String result = KWLErrorMsgs.rsSuccessFalse;
        try {
            conn = DbPool.getConnection();
            if (doAction.equals("1")) {
                result = doImport(request, conn);
            } else if (doAction.equals("2")) {
                result = UpdateTaskList(request, conn);
            }
            conn.commit();
        } catch (IOException ex) {
            DbPool.quietRollback(conn);
            Logger.getLogger(importToDoTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            Logger.getLogger(importToDoTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            DbPool.quietRollback(conn);
            Logger.getLogger(importToDoTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            DbPool.quietRollback(conn);
            Logger.getLogger(importToDoTask.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DbPool.quietClose(conn);
            response.getWriter().write(result);
            response.getWriter().close();
        }
    }

    public String UpdateTaskList(HttpServletRequest request, Connection conn) throws ServiceException, SessionExpiredException {
        String result = " ";
        CsvReader csvReader = null;
        FileInputStream fstream = null;
        try {
            String loginid = AuthHandler.getUserid(request);
            String userFullName = AuthHandler.getAuthor(conn, loginid);
            String userName = AuthHandler.getUserName(request);
            String projId = request.getParameter("projectid");
            String projName = projdb.getProjectName(conn, projId);
            String companyid = AuthHandler.getCompanyid(request);
            String ipAddress = AuthHandler.getIPAddress(request);
            int auditMode = 0;
            String mappedHeaders = request.getParameter("mappedheader");
            String csvFile = request.getParameter("filename");
            String appendchoice = request.getParameter("append");
            String projectid = request.getParameter("projectid");
            int dfid = 2;
            if (!StringUtil.isNullOrEmpty(request.getParameter("dfid"))) {
                dfid = Integer.parseInt(request.getParameter("dfid"));
            }
            dateFormat = dateFormatHandlers.getDateFormat(conn, dfid);
            com.krawler.utils.json.base.JSONObject headers = new com.krawler.utils.json.base.JSONObject(mappedHeaders);
            com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject();
            com.krawler.utils.json.base.JSONObject jtemp;
            String destinationDirectory = StorageHandler.GetDocStorePath() + StorageHandler.GetFileSeparator() + "importplans";
            File csv = new File(destinationDirectory + StorageHandler.GetFileSeparator() + csvFile);
            fstream = new FileInputStream(csv);
            csvReader = new CsvReader(new InputStreamReader(fstream));
            int totalrecords = 0;
            csvReader.readRecord();
            while (csvReader.readRecord()) {
                totalrecords++;
                jtemp = new com.krawler.utils.json.base.JSONObject();
                jtemp.put("taskname", getFieldFromCSV(csvReader, headers, "Task Name"));
                jtemp.put("duedate", getFieldFromCSV(csvReader, headers, "Due Date"));
                jtemp.put("description", getFieldFromCSV(csvReader, headers, "Description"));
                jtemp.put("priority", getFieldFromCSV(csvReader, headers, "Priority"));
                String status = getFieldFromCSV(csvReader, headers, "Status");
                int st = 0;
                if (status != null) {
                    if (status.equalsIgnoreCase("complete")) {
                        st = 1;
                    } else if (status.equalsIgnoreCase("Incomplete")) {
                        st = 0;
                    }
                }
                jtemp.put("status", st);
                jobj.append("data", jtemp);
            }

            int skipped = storeInDB(conn, jobj, projectid, appendchoice);
            if (skipped > 0) {
                result = "{\"success\":true,\"msg\":\""+ MessageSourceProxy.getMessage("pm.importlog.discardrow", null, request) +"\"}";
            } else {
                result = "{\"success\":true,\"msg\":\""+ MessageSourceProxy.getMessage("pm.project.todo.import.csv.success", null, request) +"\"}";
            }
            if (appendchoice.equals("1") && skipped == totalrecords) {
                DbPool.quietRollback(conn); // Restore deleted records in case of overwrite & when nothing added from CSV
                result = "{\"success\":true,\"msg\":\""+MessageSourceProxy.getMessage("pm.importlog.csvdiscarded", null, request)+"\"}";
            }
            fstream.close();
            String params = userFullName + " (" + userName + "), "
                    + "[ CSV ]"
                    + ", " + projName;
            AuditTrail.insertLog(conn, "200", loginid, projId, projId, companyid, params, ipAddress, auditMode);
        } catch (IOException ex) {
            Logger.getLogger(importToDoTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConfigurationException ex) {
            Logger.getLogger(importToDoTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (com.krawler.utils.json.base.JSONException ex) {
            Logger.getLogger(importToDoTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    protected int storeInDB(Connection conn, JSONObject jobjT, String projectid, String appendchoice) throws ServiceException {
        int skipped = 0;
        int taskorder = 1;
        int skipRecord = 0;
        java.util.Date defaultdd = new Date();
        try {
            if (appendchoice.equals("1")) {
                todolist.deleteAllToDoTask(conn, projectid);
            } else {
                taskorder = todolist.getLastTodoTaskIndex(conn, projectid);
                taskorder++;
            }
            com.krawler.utils.json.base.JSONArray taskArray = jobjT.getJSONArray("data");
            for (int i = 0; i < taskArray.length(); i++) {
                try {
                    com.krawler.utils.json.base.JSONObject temp = taskArray.getJSONObject(i);
                    String tid = UUID.randomUUID().toString();
                    java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    String duedate = StringUtil.serverHTMLStripper(temp.getString("duedate"));
                    if (!duedate.equals("")) {
                        defaultdd = sdf1.parse(duedate);
                    } else {
                        defaultdd = sdf1.parse("1970-01-01");
                    }
                    String pri = temp.getString("priority");
                    if (pri.equals("")) {
                        pri = "Moderate";
                    }
                    skipRecord = todolist.insertToDoTaskFromCSV(conn, temp.getString("taskname"), taskorder, temp.getInt("status"), "", tid, projectid, 2, defaultdd, true, tid, pri, temp.getString("description"));
                    if (skipRecord == 1) {
                        skipped += skipRecord;
                    } else {
                        taskorder++;
                    }
                } catch (ParseException ex) {
                    skipped++;
                } catch (ServiceException ex) {
                    skipped++;
                }
            }
        } catch (com.krawler.utils.json.base.JSONException jE) {
            KrawlerLog.op.warn("Problem Storing Data In DB [importProjectPlanCSV.storeInDB()]:" + jE.toString());
            throw ServiceException.FAILURE("importProjectPlanCSV.storeInDB error", jE);
        } catch (ServiceException ex) {
            KrawlerLog.op.warn("Problem Storing Data In DB [importProjectPlanCSV.storeInDB()]:" + ex.toString());
            throw ServiceException.FAILURE("importProjectPlanCSV.storeInDB error", ex);
        }
        return skipped;
    }

    private String doImport(HttpServletRequest request, Connection conn)
            throws SessionExpiredException, ServiceException, IOException, JSONException {
        String contentType = request.getContentType();
        CsvReader csvReader = null;
        String header = "";
        {
            FileInputStream fstream = null;
            try {
                if ((contentType != null) && (contentType.indexOf("multipart/form-data") >= 0)) {
                    String fileid = UUID.randomUUID().toString();
                    String f1 = uploadDocument(request, fileid);
                    if (f1.length() != 0) {
                        String destinationDirectory = StorageHandler.GetDocStorePath() + StorageHandler.GetFileSeparator() + "importplans";
                        File csv = new File(destinationDirectory + StorageHandler.GetFileSeparator() + f1);
                        fstream = new FileInputStream(csv);
                        csvReader = new CsvReader(new InputStreamReader(fstream));
                        csvReader.readRecord();
                        int i = 0;
                        while (!(StringUtil.isNullOrEmpty(csvReader.get(i)))) {
                            header += "{\"header\":\"" + csvReader.get(i) + "\",\"index\":" + i + "},";
                            i++;
                        }
                        header = header.substring(0, header.length() - 1);
                        header = "{\"success\": true,\"FileName\":\"" + f1 + "\",\"Headers\":[" + header + "]}";
                        // e.g. Header= "{'Task Name':0,'Start Date':1,'End Date':2,'Proirity':3,'Percent Completed':4,'Notes':5}";

                    }

                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ConfigurationException ex) {
                KrawlerLog.op.warn("Problem Storing Data In DB [importProjectPlanCSV.storeInDB()]:" + ex);
                throw ServiceException.FAILURE("importProjectPlanCSV.getfile", ex);
            } finally {
                csvReader.close();
                fstream.close();
            }
        }
        return header;
    }

    public static String uploadDocument(HttpServletRequest request, String fileid) throws ServiceException {
        String result = "";
        try {
            String destinationDirectory = StorageHandler.GetDocStorePath() + StorageHandler.GetFileSeparator() + "importplans";
            org.apache.commons.fileupload.DiskFileUpload fu = new org.apache.commons.fileupload.DiskFileUpload();
            org.apache.commons.fileupload.FileItem fi = null;
            org.apache.commons.fileupload.FileItem docTmpFI = null;

            List fileItems = null;
            try {
                fileItems = fu.parseRequest(request);
            } catch (FileUploadException e) {
                KrawlerLog.op.warn("Problem While Uploading file :" + e.toString());
            }

            long size = 0;
            String Ext = "";
            String fileName = null;
            boolean fileupload = false;
            java.io.File destDir = new java.io.File(destinationDirectory);
            fu.setSizeMax(-1);
            fu.setSizeThreshold(4096);
            fu.setRepositoryPath(destinationDirectory);
            java.util.HashMap arrParam = new java.util.HashMap();
            for (java.util.Iterator k = fileItems.iterator(); k.hasNext();) {
                fi = (org.apache.commons.fileupload.FileItem) k.next();
                arrParam.put(fi.getFieldName(), fi.getString());
                if (!fi.isFormField()) {
                    size = fi.getSize();
                    fileName = new String(fi.getName().getBytes(), "UTF8");

                    docTmpFI = fi;
                    fileupload = true;
                }
            }

            if (fileupload) {

                if (!destDir.exists()) {
                    destDir.mkdirs();
                }
                if (fileName.contains(".")) {
                    Ext = fileName.substring(fileName.lastIndexOf("."));
                }
                if (size != 0) {
                    File uploadFile = new File(destinationDirectory + "/" + fileid + Ext);
                    docTmpFI.write(uploadFile);
//                    fildoc(fileid, fileName, fileid + Ext, AuthHandler.getUserid(request), size);
                    result = fileid + Ext;
                }
            }
        } catch (ConfigurationException ex) {
            Logger.getLogger(importToDoTask.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("importProjectPlanCSV.uploadDocument", ex);
        } catch (Exception ex) {
            Logger.getLogger(importToDoTask.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("importProjectPlanCSV.uploadDocument", ex);
        }
        return result;
    }

    private String getFieldFromCSV(CsvReader csvrdr, JSONObject header, String FieldName) throws IOException {
        String result = "";
        try {
            int index = Integer.parseInt(header.get(FieldName).toString());
            if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(index))))) {
                result = cleanHTML(csvrdr.get(index));
                if (FieldName.compareTo("Due Date") == 0) {
                    // Checking date string by parsing with standard date format
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(dateFormat);
                    result = cleanHTML(csvrdr.get(index));
                    try {
                        Date checkDate = sdf.parse(result);
                        result = new java.text.SimpleDateFormat("yyyy-MM-dd").format(checkDate);
                    } catch (ParseException ex) {
                        result = "ex";
                    }
                }

            } else {
                result = "";
            }
        } catch (com.krawler.utils.json.base.JSONException ex) {
            result = "";

        } finally {
            return result;
        }
    }

    private String cleanHTML(String strText) throws IOException {
        return StringUtil.serverHTMLStripper(strText);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Imports project plan from csv document";
    }// </editor-fold>
}
