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

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.KWLErrorMsgs;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

import com.krawler.common.util.KrawlerLog;
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.fileparser.mpp.mppparser;
import com.krawler.esp.fileparser.mpp.projectTask;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.handlers.SessionHandler;
import com.krawler.esp.handlers.AuditTrail;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.handlers.projdb;
import com.krawler.svnwebclient.configuration.ConfigurationException;
import com.krawler.utils.json.JSON;
import com.krawler.utils.json.JSONException;
import com.krawler.utils.json.JSONSerializer;
import com.krawler.utils.json.base.JSONObject;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import net.sf.mpxj.MPXJException;

public class FileImporterServlet extends HttpServlet {

    private static final long serialVersionUID = 6621979198129196515L;
    HashMap arrParam = new HashMap();

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     * 
     * @param request
     *            servlet request
     * @param response
     *            servlet response
     */
    @SuppressWarnings("static-access")
    protected void processRequest(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException, SessionExpiredException {
        String contentType = request.getContentType();
        response.setContentType("text/html;charset=UTF-8");
        boolean isFormSubmit = false;
        String emsg = KWLErrorMsgs.errMsgImportMPX;
        if (SessionHandler.isValidSession(request, response)) {
            String resString = null;
            File f1 = null;
            FileInputStream fstream = null;
            mppparser c = null;
            JSONObject jo = null;
            int action = Integer.parseInt(request.getParameter("action"));
            try {
                switch (action) {
                    case 1:
                        isFormSubmit = true;
                        jo = new JSONObject();
                        if ((contentType != null) && (contentType.indexOf("multipart/form-data") >= 0)) {
                            String docid = UUID.randomUUID().toString();
                            f1 = getfile(request, docid);
                            fstream = new FileInputStream(f1);
                            c = new mppparser();
                            String newres = c.getResourceInfo(fstream, Integer.parseInt(request.getParameter("type")));
                            JSONObject jarr = new JSONObject(newres);
                            jo.put("success", true);
                            if (jarr.getJSONArray("data").length() > 0 && request.getParameter("isres").equals("1")) // if imported file contains resources then send list
                            //for mapping with existing resources
                            {
                                jo.put("newresource", newres);
                                jo.put("docid", docid);
                                resString = jo.toString();
                            } else { // else import tasks
                                fstream = new FileInputStream(f1);// read uploded file
                                HashMap mp = importFile(fstream, docid, request); // store tasks
                                String errormsg = ((ArrayList) mp.get("errormsg")).get(0).toString();
                                jo.put("errormsg", errormsg);
                                resString = jo.toString();
                            }
                        } else {
                            jo.put("success", false);
                            jo.put("msg", "Error occured at server while importing file.");
                            resString = jo.toString();
                        }
                        break;
                    case 2:// after mapping with existing resources
                        jo = new JSONObject();
                        fstream = getStoredfile(request.getParameter("docid")); // read stored file
                        HashMap mp = importFile(fstream, request.getParameter("docid"), request); // store tasks
                        mapImportedRes(request.getParameter("val"), mp);// allocate tasks to mapped resources
                        if(request.getParameter("isbaseline").equals("1")){
                            saveBaseline(request);
                        }
                        String errormsg = ((ArrayList) mp.get("errormsg")).get(0).toString();
                        jo.put("success", true);
                        jo.put("errormsg", errormsg);
                        resString = jo.toString();
                        break;
                }
            } catch (com.krawler.utils.json.base.JSONException xex) {
                resString = "Problem FileImporterServlet while importing project[connection]:" + xex.toString();
                KrawlerLog.op.warn(resString);
                resString = "{\"success\":false, \"msg\":\"" +emsg+ "\", \"errormsg\":\"" +resString+ "\"}";
            } catch (ServiceException xex) {
                resString = "Problem FileImporterServlet while importing project[connection]:" + xex.toString();
                KrawlerLog.op.warn(resString);
                resString = "{\"success\":false, \"msg\":\"" +emsg+ "\", \"errormsg\":\"" +resString+ "\"}";
            } catch (MPXJException xex) {
                resString = "Problem FileImporterServlet while importing project[connection]:" + xex.toString();
                KrawlerLog.op.warn(resString);
                resString = "{\"success\":false, \"msg\":\"" +KWLErrorMsgs.errMsgImportMPXInvalidFileFormat+ "\", \"errormsg\":\"" +resString+ "\"}";
            } catch (Exception xex) {
                resString = "Problem FileImporterServlet while importing project[connection]:" + xex.toString();
                KrawlerLog.op.warn(resString);
                resString = "{\"success\":false, \"msg\":\"" +emsg+ "\", \"errormsg\":\"" +resString+ "\"}";
            } finally {
                if(fstream != null)
                    fstream.close();
                if (!isFormSubmit) {
                    try {
                        JSONObject jbj = new com.krawler.utils.json.base.JSONObject();
                        jbj.put("valid", "true");
                        jbj.put("data", resString);
                        response.getWriter().println(jbj.toString());
                    } catch (com.krawler.utils.json.base.JSONException ex) {
                        Logger.getLogger(FileImporterServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    response.getWriter().println(resString);
                }
                response.getWriter().close();
            }
        } else { //session valid if() ends here
            response.getWriter().println("{\"valid\": false}");
        }
    }

    protected HashMap importFile(FileInputStream fstream, String docid, HttpServletRequest request) throws ServiceException, MPXJException {
        Connection conn = null;
        HashMap mp = new HashMap();
        mppparser c = null;
        JSON ab = null;
        try {
            int type = Integer.parseInt(request.getParameter("type"));
            String projid = request.getParameter("projectid");
            c = new mppparser();
            List<projectTask> tasklist = c.getRecords(fstream, type);// get all task records
            String companyid = AuthHandler.getCompanyid(request, true);
            conn = DbPool.getConnection();
            JSONSerializer js = new JSONSerializer();
            ab = js.toJSON(tasklist);
            String userid = AuthHandler.getUserid(request);
            if (request.getParameter("mode") != null && request.getParameter("mode").equals("0")) {
                projid = AdminServlet.createSingleProject(conn, arrParam.get("projectname").toString(), companyid, userid);
            }
            mp = storeInDB(conn, ab, projid, request.getParameter("userchoice"), userid);
            if (request.getParameter("isweek").equals("1") || request.getParameter("isholiday").equals("1")) {
                fstream = getStoredfile(docid); // read stored file
                String returnStr = mppparser.importCalendar(fstream, request);
                if (returnStr.compareTo("{}") != 0) {
                    JSONObject jobj = new JSONObject(returnStr);
                    if (request.getParameter("isweek").equals("1")) {
                        com.krawler.utils.json.base.JSONArray workweek = jobj.getJSONArray("workweek");
                        for (int i = 0; i < workweek.length(); i++) {
                            JSONObject temp = workweek.getJSONObject(i);
//                            AdminServlet.updateWorkWeek(conn,temp.getInt("day"),temp.getBoolean("ish"),projid);
                        }
                    }
                    if (jobj.has("holiday")) {
                        com.krawler.utils.json.base.JSONArray holiday = jobj.getJSONArray("holiday");
                        for (int i = 0; i < holiday.length(); i++) {
                            JSONObject temp = holiday.getJSONObject(i);
//                            AdminServlet.newCompanyHoliday(conn,temp.getString("date"),temp.getString("date"),companyid);
                        }
                    }
                }
            }
            String userFullName = AuthHandler.getAuthor(conn, userid);
            String userName = AuthHandler.getUserName(request);
            String projName = projdb.getProjectName(conn, projid);
            String ipAddress = AuthHandler.getIPAddress(request);
            int auditMode = 0;
            String params = userFullName + " ("+ userName +"), " +
                                        "[ MPP/MPX ]" +
                                        ", " + projName;
            AuditTrail.insertLog(conn, "118", userid, projid, projid, companyid, params, ipAddress, auditMode);
            conn.commit();
            if(request.getParameter("isres").equals("0") && request.getParameter("isbaseline").equals("1")){
                saveBaseline(request);
            }
        } catch (ServiceException xex) {
            DbPool.quietRollback(conn);
            throw ServiceException.FAILURE("projdb.deleteAllRecords error", xex);
        } catch (SessionExpiredException xex) {
            DbPool.quietRollback(conn);
            throw ServiceException.FAILURE("projdb.deleteAllRecords error", xex);
        } catch (com.krawler.utils.json.base.JSONException e) {
            DbPool.quietRollback(conn);
            throw ServiceException.FAILURE("projdb.deleteAllRecords error", e);
        } catch (MPXJException e) {
            DbPool.quietRollback(conn);
            throw new MPXJException(e.getMessage(), e);
        } catch (Exception e) {
            String r = e.getMessage();
            DbPool.quietRollback(conn);
            throw ServiceException.FAILURE("projdb.deleteAllRecords error", e);
        } finally {
            DbPool.quietClose(conn);
        }
        return mp;
    }

    protected HashMap storeInDB(Connection conn, JSON data, String projectid, String userChoice, String userid) throws ServiceException {
        String errormsg = "";
        boolean flag = false;
        String data1 = "{data :" + data + " }";
        HashMap idMap = new HashMap();
        idMap.put(0, "0");
        HashMap<String, ArrayList> restaskmap = new HashMap<String, ArrayList>();
        try {
            int totalOldTasks = 0;
            com.krawler.utils.json.base.JSONObject jobj = null;
            if (userChoice.equals("0")) {
                totalOldTasks = projdb.getLastTaskIndex(conn, projectid);
            } else {
                projdb.deleteAllRecords(conn, projectid);
            }
            jobj = new com.krawler.utils.json.base.JSONObject(data1);
            com.krawler.utils.json.base.JSONArray taskArray = jobj.getJSONArray("data");
            for (int i = 0; i < taskArray.length(); i++) {
                JSONObject temp = taskArray.getJSONObject(i);
                String tid = UUID.randomUUID().toString();
                idMap.put(temp.getInt("taskid"), tid);
                Object np = idMap.get(temp.getInt("parent"));
                temp.remove("parent");
                temp.put("parent", np.toString());
                temp.put("loginid", userid);
                temp.put("links", "");
                projdb.InsertTask(conn, temp, tid, projectid, Integer.toString(totalOldTasks));
                totalOldTasks++;
                // insert resources
                String resnames = temp.getString("resourcename");
                if (resnames.length() > 0) {
                    insertimportedtaskResources(tid, resnames, restaskmap);
                }
            }
            for (int i = 0; i < taskArray.length(); i++) {
                JSONObject temp = taskArray.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(temp.getString("predecessor"))) {
                    String[] pred = temp.getString("predecessor").split(",");
                    for (int j = 0; j < pred.length; j++) {
                        if(idMap.get(Integer.parseInt(pred[j])) != null){
                            String fid = idMap.get(Integer.parseInt(pred[j])).toString();
                            String tid = idMap.get(Integer.parseInt(temp.getString("taskid"))).toString();
                            projdb.addLink(conn, fid, tid);
                        }
                    }
                }
            }
            ArrayList errArr = new ArrayList();
            errArr.add(errormsg);
            restaskmap.put("errormsg", errArr);
            projdb.chkProjDateOnImport(conn, projectid);
        } catch (com.krawler.utils.json.base.JSONException jE) {
            KrawlerLog.op.warn("Problem Storing Data In DB [FileImporterServlet.storeInDB()]:" + jE.toString());
            throw ServiceException.FAILURE("projdb.deleteAllRecords error", jE);
        } catch (ServiceException ex) {
            KrawlerLog.op.warn("Problem Storing Data In DB [FileImporterServlet.storeInDB()]:" + ex.toString());
            throw ServiceException.FAILURE("projdb.deleteAllRecords error", ex);
        }
        return restaskmap;
    }

    private void saveBaseline(HttpServletRequest request) throws ServiceException, SessionExpiredException{
        Connection conn = null;
        try{
            conn = DbPool.getConnection();
            String projid = request.getParameter("projectid");
            String projName = projdb.getProjectName(conn, projid);
            String userid = AuthHandler.getUserid(request);
            boolean isUnderLimit = projdb.checkBaseline(conn, projid);
            if(isUnderLimit) {
                projdb.saveBaseline(conn, projid, userid,
                        projName.concat(" Baseline - ").concat(new SimpleDateFormat("yyyy-MM-dd").format(new Date())),
                        "Baseline created on importing MPP/MPX file.");
            }
            conn.commit();
        } catch (Exception ex) {
            DbPool.quietRollback(conn);
            throw ServiceException.FAILURE("FileImporterServlet.saveBaseline", ex);
        } finally {
            DbPool.quietClose(conn);
        }
    }

    public File getfile(HttpServletRequest request, String fileid) throws ServiceException {
        File uploadFile = null;
        try {
            String destinationDirectory = StorageHandler.GetDocStorePath() + StorageHandler.GetFileSeparator() + "importplans";
            File destdir = new File(destinationDirectory);
            if (!destdir.exists()) {
                destdir.mkdir();
            }
            DiskFileUpload fu = new DiskFileUpload();
            String Ext = "";
            List fileItems = null;
            try {
                fileItems = fu.parseRequest(request);
            } catch (FileUploadException e) {
                KrawlerLog.op.warn("Problem While Uploading file :" + e.toString());
            }
            for (Iterator i = fileItems.iterator(); i.hasNext();) {
                FileItem fi = (FileItem) i.next();
                if (!fi.isFormField()) {
                    String fileName = null;
                    try {
                        fileName = new String(fi.getName().getBytes(), "UTF8");
                        if (fileName.contains(".")) {
                            Ext = fileName.substring(fileName.lastIndexOf("."));
                        }
                        long size = fi.getSize();
                        if (fi.getSize() != 0) {
                            String projid = request.getParameter("projectid");
                            uploadFile = new File(destinationDirectory + StorageHandler.GetFileSeparator() + fileid + Ext);
                            fi.write(uploadFile);
                            fildoc(fileid, fileName, projid, fileid + Ext, AuthHandler.getUserid(request), size);

                        }
                    } catch (IOException ex) {
                        KrawlerLog.op.warn("Problem While Reading file :" + ex.toString());
                        throw ServiceException.FAILURE("FileImporterServlet.getfile", ex);
                    } catch (ServiceException ex) {
                        KrawlerLog.op.warn("Problem While Reading file :" + ex);
                        throw ServiceException.FAILURE("FileImporterServlet.getfile", ex);
                    } catch (Exception ex) {
                        KrawlerLog.op.warn("Problem While Reading file :" + ex.toString());
                        throw ServiceException.FAILURE("FileImporterServlet.getfile", ex);
                    }
                } else {
                    arrParam.put(fi.getFieldName(), fi.getString());
                }
            }
        } catch (ConfigurationException ex) {
            KrawlerLog.op.warn("Problem Storing Data In DB [FileImporterServlet.storeInDB()]:" + ex);
            throw ServiceException.FAILURE("FileImporterServlet.getfile", ex);
        }
        return uploadFile;
    }

    public FileInputStream getStoredfile(String docid) throws ServiceException, ParseException {
        FileInputStream fis = null;
        try {
            java.util.Hashtable ht = com.krawler.esp.database.dbcon.getfileinfo(docid);
            String src = StorageHandler.GetDocStorePath(ht.get("storeindex").toString()) + StorageHandler.GetFileSeparator();
            src = src + "importplans" + StorageHandler.GetFileSeparator() + ht.get("svnname");
            File fp = new File(src);
            byte[] buff = new byte[(int) fp.length()];
            fis = new FileInputStream(fp);
//            fis.close();
        } catch (ConfigurationException ex) {
            KrawlerLog.op.warn("Problem Storing Data In DB [FileImporterServlet.storeInDB()]:" + ex);
        } catch (IOException ex) {
            KrawlerLog.op.warn("Problem Storing Data In DB [FileImporterServlet.storeInDB()]:" + ex);
        }
        return fis;
    }

    public static void fildoc(String docid, String docname,
            String projid, String svnName, String userid, long size) throws ServiceException {

        PreparedStatement pstmt = null;
        String sql = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            java.util.Date d = new java.util.Date();
            java.sql.Timestamp docdatemod = new Timestamp(d.getTime());
            String currentStoreIndex = StorageHandler.GetCurrentStorageIndex();
            sql = "INSERT INTO docs(docid, docname, docdatemod, " + "userid,svnname,storageindex,docsize) VALUES (?,?,?,?,?,?,?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, docid);
            pstmt.setString(2, docname);
            pstmt.setObject(3, docdatemod);
            pstmt.setObject(4, userid);
            pstmt.setString(5, svnName);
            pstmt.setString(6, currentStoreIndex);
            pstmt.setObject(7, size);
            pstmt.executeUpdate();
            sql = "insert into docprerel (docid,userid,permission) values(?,?,?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, docid);
            pstmt.setString(2, projid);
            pstmt.setObject(3, "8");
            pstmt.executeUpdate();

            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            throw ServiceException.FAILURE("FileImporterServlet.fildoc", ex);
        } catch (Exception ex) {
            DbPool.quietRollback(conn);
            throw ServiceException.FAILURE("FileImporterServlet.fildoc", ex);
        } finally {
            DbPool.quietClose(conn);
        }
    }

    public static void mapImportedRes(String resmapval, HashMap mp) throws ServiceException {
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            com.krawler.utils.json.base.JSONArray jsonArray = new com.krawler.utils.json.base.JSONArray(resmapval);
            com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject();
            for (int k = 0; k < jsonArray.length(); k++) {
                jobj = jsonArray.getJSONObject(k);
                if (jobj.getString("resourceid").length() > 0 && mp.containsKey(jobj.getString("tempid"))) {
                    ArrayList taskids = (ArrayList) mp.get(jobj.getString("tempid"));
                    String tasks = taskids.toString();
                    tasks = tasks.substring(1, tasks.lastIndexOf("]"));
                    projdb.insertimportedtaskResources(conn, tasks, jobj.getString("resourceid"));
                }
            }
            conn.commit();
        } catch (JSONException ex) {
            DbPool.quietRollback(conn);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } catch (Exception ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
    }

    public static void insertimportedtaskResources(String taskid, String resourcenames, HashMap restaskmap) {
        String[] resArray = resourcenames.split(",");
        ArrayList al = null;
        for (int i = 0; i < resArray.length; i++) {
            String rid = resArray[i];
            if (restaskmap.containsKey(rid)) {
                al = (ArrayList) restaskmap.get(rid);
            } else {
                al = new ArrayList();
            }
            al.add(taskid);
            restaskmap.put(rid, al);
        }
    }
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on
    // the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     * 
     * @param request
     *            servlet request
     * @param response
     *            servlet response
     */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(FileImporterServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * 
     * @param request
     *            servlet request
     * @param response
     *            servlet response
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(FileImporterServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}

