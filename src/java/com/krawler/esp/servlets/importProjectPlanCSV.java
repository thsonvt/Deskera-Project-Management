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
import com.krawler.common.util.SchedulingUtilities;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.handlers.projdb;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.esp.company.CompanyDAO;
import com.krawler.esp.company.CompanyDAOImpl;
import com.krawler.esp.handlers.AuditTrail;
import com.krawler.esp.handlers.ImportLogHandler;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.handlers.dateFormatHandlers;
import com.krawler.esp.project.task.DurationType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class importProjectPlanCSV extends HttpServlet {

    private Set<String> resNames;
//    String[] datePatterns = {"yyyy-MM-d", "yyyy-MM-dd", "dd/MM/yy", "MM/dd/yy", "dd/MM/yyyy", "MM/dd/yyyy",
//                             "M/d/yy", "E M/d/yy", "EEEE, MMMM dd, yyyy", "dd-MM-yy", "MM-dd-yy", "d-MM-yyyy", "dd-MM-yyyy",
//                             "E d-MM-yy", "MMMM dd, yyyy"};
    private String dateFormat = "";
    private Locale locale = Locale.ENGLISH;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, com.krawler.utils.json.base.JSONException {
        response.setContentType("text/html;charset=UTF-8");
        String doAction = request.getParameter("action");
        Connection conn = null;
        String result = KWLErrorMsgs.rsSuccessFalse;
        JSONObject j = new JSONObject();
        JSONObject temp = new JSONObject();
        try {
            conn = DbPool.getConnection();
            String companyID = AuthHandler.getCompanyid(request);
            CompanyDAO cd = new CompanyDAOImpl();
            locale = cd.getCompanyLocale(conn, companyID);
            int errorCount = 0;
            // This action will import and store the original file on the svn. Also initializes importLog and returns the header JSON
            if (doAction.equals("1")) {

                clearTempData(conn);
                result = doImport(request, conn);

            // This action will insert tasks in proj_task and temp table, try to populate skipped records and respective error msgs.
            // will also collect resources and update counts in importLog.
            } else if (doAction.equals("2")) {
                result = UpdateTaskList(request, conn);
                JSONObject resJSON = new JSONObject(result);
                if(StringUtil.equal(resJSON.getString("importResult"), "success")){
                    Object[] resArr = resNames.toArray();
                    JSONArray ja = new JSONArray();
                    for (int c = 0; c < resArr.length; c++) {
                        JSONObject jo = new JSONObject();
                        String str = (String)resArr[c];
                        jo.put("name", str);
                        ja.put(jo);
                    }
                    resJSON.put("resources", new JSONObject().put("data", ja.toString()).toString());
                    insertAuditLog(conn, request); // Audit Log is inserted here as it successfully inserted tasks in DB
                }
                result = resJSON.toString();

            } else if(doAction.equals("3") || doAction.equals("4")){

                // for action 3, there is resource mapping involved. Not for 4
                if(doAction.equals("3")){
                    String val = request.getParameter("val");
                    errorCount = saveResources(conn, val, errorCount);
                }
                errorCount = savePredecessors(conn, errorCount);
                errorCount = saveParent(conn, request.getParameter("projectid"), errorCount);
                result = KWLErrorMsgs.rsValidTrueSuccessTrue;
            }

            conn.commit(); //  connection committed before post processing to atleast show user imported data.

            if(doAction.equals("3") || doAction.equals("4")){

                String filename = request.getParameter("filename");
                filename = filename.substring(0, filename.lastIndexOf("."));
                Object[] data = ImportLogHandler.getRecordCount(conn, filename);
                if(errorCount > 0) {
                    createPostProcessErrorFile(conn, request, errorCount);
                }
                clearTempData(conn);
                projdb.chkProjDateOnImport(conn, request.getParameter("projectid")); // changing project start date if needed
                saveBaseline(conn, request);
                conn.commit();
                temp.put("success", true);
                temp.put("msg", MessageSourceProxy.getMessage(data[2].toString(), null, request));
                temp.put("total", data[0]);
                temp.put("rejected", data[1]);
                temp.put("error", errorCount);
                temp.put("importResult", "success");
            }
        } catch (IOException ex) {
            DbPool.quietRollback(conn);
            temp.put("success", false); temp.put("msg", ex.getMessage()); temp.put("importResult", "failure");
            Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            temp.put("success", false); temp.put("msg", ex.getMessage()); temp.put("importResult", "failure");
            Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            DbPool.quietRollback(conn);
            temp.put("success", false); temp.put("msg", ex.getMessage()); temp.put("importResult", "failure");
            Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            DbPool.quietRollback(conn);
            temp.put("success", false); temp.put("msg", ex.getMessage()); temp.put("importResult", "failure");
            Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            j.put("valid", true);
            j.put("data", temp.toString());
            DbPool.quietClose(conn);
            if(doAction.equals("3") || doAction.equals("4"))
                response.getWriter().write(j.toString());
            else
                response.getWriter().write(result);
            response.getWriter().close();
        }
    }

    /**
     * Prepare task for inserting in Db by reading it from CSV. Looks for exceptions while storing and skips such tasks,
     * which are then inserted into the failure file
     */
    public String UpdateTaskList(HttpServletRequest request, Connection conn) throws ServiceException {
        String result = " ";
        CsvReader csvReader = null;
        FileInputStream fstream = null;
        String failedRecords = "", csvFile = "";
        try {
            String mappedHeaders = request.getParameter("mappedheader");
            csvFile = request.getParameter("filename");
            String appendchoice = request.getParameter("append");
            String includeheader = request.getParameter("includeheader");
            String projectid = request.getParameter("projectid");
            int dfid = 2;
            if(!StringUtil.isNullOrEmpty(request.getParameter("dfid")))
                dfid = Integer.parseInt(request.getParameter("dfid"));
            dateFormat = dateFormatHandlers.getDateFormat(conn, dfid);
            com.krawler.utils.json.base.JSONObject headers = new com.krawler.utils.json.base.JSONObject(mappedHeaders);
            com.krawler.utils.json.base.JSONObject jtemp;
            String userid = AuthHandler.getUserid(request);
            String destinationDirectory = StorageHandler.GetDocStorePath() + StorageHandler.GetFileSeparator() + "importplans";
            File csv = new File(destinationDirectory + StorageHandler.GetFileSeparator() + csvFile);
            fstream = new FileInputStream(csv);
            csvReader = new CsvReader(new InputStreamReader(fstream));
            int totalrecords = 0;
            String s = "";
            if (includeheader.equals("0")) {
                if(csvReader.readHeaders()){
                    String[] heads = csvReader.getHeaders();
                    for (int i = 0; i < heads.length; i++) {
                        s += "\""+heads[i]+"\",";
                    }
                    s = s.concat("\"log\"");
                }
            }
            failedRecords += s;
            int totalOldTasks = 0;
            if (appendchoice.equals("0")) {
                totalOldTasks = projdb.getLastTaskIndex(conn, projectid);
            } else {
                projdb.deleteAllRecords(conn, projectid);
            }
            resNames = new HashSet<String>();
            int skipped = 0;

            while (csvReader.readRecord()) {
                totalrecords++;
                HashMap<Integer, String> skip = new HashMap<Integer, String>();
                jtemp = new com.krawler.utils.json.base.JSONObject();
                jtemp.put("taskname", getFieldFromCSV(csvReader, headers, "Task Name"));
                jtemp.put("startdate", getFieldFromCSV(csvReader, headers, "Start Date"));
                jtemp.put("enddate", getFieldFromCSV(csvReader, headers, "End Date"));
                String pc = getFieldFromCSV(csvReader, headers, "Percent Completed");
                if(StringUtil.isNullOrEmpty(pc))
                    pc = "0";
                else if(pc.contains("%")){
                    pc = pc.substring(0, pc.indexOf("%"));
                }
                jtemp.put("percentcomplete", pc);
                String priority = getFieldFromCSV(csvReader, headers, "Priority");
                String prt = "1";
                if (priority != null) {
                    if (priority.equalsIgnoreCase("Low")) {
                        prt = "2";
                    } else if (priority.equalsIgnoreCase("High")) {
                        prt = "0";
                    }
                }
                jtemp.put("priority", prt);
                jtemp.put("notes", getFieldFromCSV(csvReader, headers, "Notes"));
                jtemp.put("duration", getFieldFromCSV(csvReader, headers, "Duration"));
                jtemp.put("resourcename", getFieldFromCSV(csvReader, headers, "Resource Name"));
                jtemp.put("predecessor", getFieldFromCSV(csvReader, headers, "Predecessor"));
                String parent = getFieldFromCSV(csvReader, headers, "Parent");
                String p = "";
                if(!StringUtil.equal(parent, "") && !parent.equalsIgnoreCase("Parent"))
                    p = parent;
                jtemp.put("parent", p);

                skip = storeInDB(conn, jtemp, projectid, appendchoice, userid, totalOldTasks, totalrecords);
                if (!skip.isEmpty()) {
                    if(includeheader.equals("0"))
                        failedRecords += "\n"+csvReader.getRawRecord()+",\""+skip.get(1)+"\"";
                    else
                        failedRecords += "\n"+csvReader.getRawRecord()+"\n\"Header row was included while importing. Cannot create task for it.\"";
                    skipped++;
                } else {
                    totalOldTasks++;
                }
            }
            String msg_client = "", msg = "", logkey = "";
            if(skipped == totalrecords) {
                if(appendchoice.equals("1")){
                    DbPool.quietRollback(conn); // Restore deleted records in case of overwrite & when nothing added from CSV
                }
                msg = "CSV discarded due to incorrect data/mapping.";
                msg_client = MessageSourceProxy.getMessage("pm.importlog.csvdiscarded", null, locale);
                logkey = "pm.importlog.csvdiscarded";
                result = "{\"success\":true,\"msg\":\""+msg+"\", \"importResult\":\"failure\"}";
            } else if(skipped == 0){
                msg = "Project plan imported successfully.";
                msg_client = MessageSourceProxy.getMessage("pm.importlog.succPlan", null, locale);
                logkey = "pm.importlog.succPlan";
                result = "{\"success\":true,\"msg\":\""+msg+"\", \"importResult\":\"success\"}";
            } else {
                msg = "Some rows have been discarded due to incorrect data.";
                msg_client = MessageSourceProxy.getMessage("pm.importlog.discardrow", null, locale);
                logkey = "pm.importlog.discardrow";
                result = "{\"success\":true,\"msg\":\""+msg+"\", \"importResult\":\"success\"}";
            }
            String fid = csvFile.substring(0, csvFile.lastIndexOf("."));
            ImportLogHandler.updateRecordCount(conn, fid, msg, logkey, totalrecords, skipped, -1);
            fstream.close();
            csvReader.close();
        } catch (SessionExpiredException ex) {
            Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConfigurationException ex) {
            Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
        } catch (com.krawler.utils.json.base.JSONException ex) {
            Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            createFailureFile(conn, failedRecords, csvFile);
        }
        return result;
    }

    /**
     * Adds the task into proj_task and temp table. any exception will return a HashMap contining error description
     */
     protected HashMap<Integer, String> storeInDB(Connection conn, JSONObject temp, String projectid, 
             String appendchoice, String userid, int totalOldTasks, int recordIndex) throws ServiceException {
        int skipped = 0;
        HashMap<Integer, String> skip = new HashMap<Integer, String>();
        try {
            int nonworkweekArr[] = projdb.getNonWorkWeekDays(conn, projectid);
            String holidayArr[] = projdb.getCompHolidays(conn, projectid, "");
            try {
                String tid = UUID.randomUUID().toString();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                String startdate = StringUtil.serverHTMLStripper(temp.getString("startdate"));
                java.util.Date SDateVal = sdf.parse(startdate);
                startdate = sdf.format(SDateVal);
                Calendar cal = Calendar.getInstance();
                cal.setTime(SDateVal);
                java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd");
                boolean flag = true;
                while(flag){
                      if((Arrays.binarySearch(nonworkweekArr, SDateVal.getDay()) >= 0 || Arrays.binarySearch(holidayArr, sdf1.format(SDateVal)) >= 0)) {
                             cal.setTime(SDateVal);
                             cal.add(Calendar.DATE, 1);
                             SDateVal = cal.getTime();
                             flag = true;
                      } else {
                             flag = false;
                      }
                }
                temp.remove("startdate");
                temp.put("startdate", sdf1.format(SDateVal));
                java.util.Date EDateVal= new Date();
                String acduration ="";
                String enddate = "";
                String dura=temp.getString("duration");
                if(!temp.getString("enddate").equals("")){
                   enddate = StringUtil.serverHTMLStripper(temp.getString("enddate"));
                   EDateVal = sdf.parse(enddate);
                   acduration = getActualDuration_importCSV(SDateVal, EDateVal, nonworkweekArr, holidayArr,dura);
                } else {
                    if(!StringUtil.isNullOrEmpty(dura)){
                        EDateVal = projdb.calculateEndDate(conn, SDateVal, dura, nonworkweekArr, holidayArr, "");
                        enddate = sdf.format(EDateVal);
                        acduration = dura;
                    } else {
                        throw new ParseException("Insufficient Data", 0);
                    }
                }

                temp.put("duration", acduration);
                temp.put("actstartdate", startdate);
                temp.put("enddate",enddate);
                temp.put("level", "0");
                temp.put("isparent", "false");
                temp.put("loginid", userid);
                temp.put("links", "");
                projdb.InsertTask(conn, temp, tid, projectid, Integer.toString(totalOldTasks));
                totalOldTasks++;
                DbUtil.executeUpdate(conn, "INSERT INTO tempImportData VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        new Object[]{recordIndex, tid, temp.getString("taskname"), temp.getString("duration"),
                        temp.getString("startdate"), temp.getString("enddate"), temp.getInt("percentcomplete"),
                        temp.getString("priority"), temp.getString("notes"), temp.getString("resourcename"),
                        temp.getString("predecessor"), temp.getString("parent"), ""});
                String resnames = temp.getString("resourcename");
                String[] res = resnames.split(",");
                for(int c = 0; c < res.length; c++){
                    if(!StringUtil.equal(res[c], ""))
                        resNames.add(res[c]);
                }
            } catch (com.krawler.utils.json.base.JSONException ex) {
//                skip.put(++skipped, MessageSourceProxy.getMessage(KWLErrorMsgs.importCSVInvFormat, null, locale));
                skip.put(++skipped, KWLErrorMsgs.importCSVInvFormat);
            } catch (ParseException ex) {
//                skip.put(++skipped, MessageSourceProxy.getMessage(KWLErrorMsgs.importCSVInvDateFormat, null, locale));
                skip.put(++skipped, KWLErrorMsgs.importCSVInvDateFormat);
            } catch (ServiceException ex) {
//                skip.put(++skipped, MessageSourceProxy.getMessage(KWLErrorMsgs.importCSVTaskFailure, null, locale));
                skip.put(++skipped, KWLErrorMsgs.importCSVTaskFailure);
            }
        } catch (ServiceException ex) {
            KrawlerLog.op.warn("Problem Storing Data In DB [importProjectPlanCSV.storeInDB()]:" + ex.toString());
            throw ServiceException.FAILURE("importProjectPlanCSV.storeInDB error", ex);
        }
        return skip;
    }

     public String getActualDuration(Date stdate, Date enddate, int[] NonWorkDays, String[] holidays) {
        int noofdays = 0;
        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd");
        try {
            Calendar c1 = Calendar.getInstance();
            while (stdate.compareTo(enddate) < 0) {
                if (Arrays.binarySearch(NonWorkDays, stdate.getDay()) < 0 && Arrays.binarySearch(holidays, sdf1.format(stdate)) < 0) {
                    noofdays++;
                }
                c1.set(stdate.getYear() + 1900, stdate.getMonth(), stdate.getDate());
                c1.add(Calendar.DATE, 1);
                stdate = sdf1.parse(sdf1.format(c1.getTime()));
            }
            if (stdate.compareTo(enddate) == 0) {
                if (Arrays.binarySearch(NonWorkDays, stdate.getDay()) < 0 && Arrays.binarySearch(holidays, sdf1.format(stdate)) < 0) {
                         noofdays++;
                   }
                }
        } catch (ParseException ex) {
            Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
        }
        return noofdays + "d";
    }

    public static String getActualDuration_importCSV(Date stdate, Date enddate, int[] NonWorkDays, String[] holidays, String duration) {
        Double noofdays = 0.0;
        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd");
        try {
            Calendar c1 = Calendar.getInstance();
            while (stdate.compareTo(enddate) < 0) {
                if (Arrays.binarySearch(NonWorkDays, stdate.getDay()) < 0 && Arrays.binarySearch(holidays, sdf1.format(stdate)) < 0) {
                    noofdays++;
                }
                c1.setTime(stdate);
                c1.add(Calendar.DATE, 1);
                stdate = sdf1.parse(sdf1.format(c1.getTime()));
            }
            if (stdate.compareTo(enddate) == 0) {
                if (Arrays.binarySearch(NonWorkDays, stdate.getDay()) < 0 && Arrays.binarySearch(holidays, sdf1.format(stdate)) < 0) {
                    if (duration.equals("")) {
                        noofdays++;
                    } else {
                        int dur1 = Integer.parseInt(duration.substring(0, 1));
                        if (dur1 == 0) {
                            noofdays = 0.0;
                        } else {
                            noofdays++;
                        }
                    }
                }
            }
        } catch (ParseException ex) {
            Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
        }
        return String.valueOf(noofdays);
    }

    private String doImport(HttpServletRequest request, Connection conn)
            throws SessionExpiredException, ServiceException, IOException, JSONException {
        String contentType = request.getContentType();
        CsvReader csvReader = null;
        String header = "";
        FileInputStream fstream = null;
        try {
            if ((contentType != null) && (contentType.indexOf("multipart/form-data") >= 0)) {
                String fileid = UUID.randomUUID().toString();
                String f1 = uploadDocument(request, fileid);
                if (f1.length() != 0) {
                    String destinationDirectory = StorageHandler.GetDocStorePath() + StorageHandler.GetFileSeparator() +"importplans";
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

                    String fileName = request.getParameter("realfilename");
                    String userid = AuthHandler.getUserid(request);
                    String companyid = AuthHandler.getCompanyid(request);
                    String projectid = request.getParameter("projectid");
                    int c = ImportLogHandler.insertImportLog(conn, fileid, fileName, f1, "CSV", "", "Project plan", userid, companyid, projectid, "", "pm.common.projectplan");
                    if(c != 1)
                        throw ServiceException.FAILURE("Failed to insert log", new Throwable());
                    // e.g. Header= "{'Task Name':0,'Start Date':1,'End Date':2,'Proirity':3,'Percent Completed':4,'Notes':5}";
                }

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConfigurationException ex) {
            KrawlerLog.op.warn("Problem Storing Data In DB [importProjectPlanCSV.storeInDB()]:" + ex);
            throw ServiceException.FAILURE("importProjectPlanCSV.getfile", ex);
        } finally {
            if(csvReader != null)
                csvReader.close();
            if(fstream != null)
                fstream.close();
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
            Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("importProjectPlanCSV.uploadDocument", ex);
        } catch (Exception ex) {
            Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
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
                if(FieldName.compareTo("Start Date") == 0 || FieldName.compareTo("End Date") == 0) {
                    // Checking date string by parsing with standard date format
                    result = cleanHTML(csvrdr.get(index));
                    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
                    try {
                        Date checkDate = sdf.parse(result);
                        result = new SimpleDateFormat("yyyy-MM-dd").format(checkDate);
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

    /**
     * Adding resources on the respective task. Checks for
     * 1. mapping of the resource
     */
    private int saveResources(Connection conn, String val, int errorCount) throws ServiceException{
        String taskid = "";
        try{
            JSONArray resData = new JSONArray(val);
            DbResults rs = DbUtil.executeQuery(conn, "SELECT taskid, resourcename FROM tempImportData");
            while (rs.next()) {
                String resOnTask = rs.getString("resourcename");
                taskid = rs.getString("taskid");
                if(!StringUtil.isNullOrEmpty(resOnTask)){
                    String[] ress = resOnTask.split(",");
                    for (int r = 0; r < ress.length; r++) {
                        String res = ress[r];
                        for (int i = 0; i < resData.length(); i++) {
                            JSONObject jtemp = resData.getJSONObject(i);
                            String resname = jtemp.getString("tempid");
                            if(resname.equalsIgnoreCase(res)){
                                String resourceid = jtemp.getString("resourceid");
                                if(!StringUtil.equal(resourceid, ""))
                                    DbUtil.executeUpdate(conn, "INSERT INTO proj_taskresourcemapping (taskid,resourceid,resduration) values(?,?,?)",
                                        new Object[]{taskid, resourceid, 0});
                            }
                        }
                    }
                }
            }
        } catch (com.krawler.utils.json.base.JSONException ex) {
            ImportLogHandler.updateTaskLog(conn, taskid, KWLErrorMsgs.iLExRes);
            errorCount++;
        } catch (ServiceException ex) {
            ImportLogHandler.updateTaskLog(conn, taskid, KWLErrorMsgs.iLExRes);
            errorCount++;
        } finally{
            return errorCount;
        }
    }

    /**
     * Establish predecessor and successors. checks for
     * 1. inconsistent start and finish dates for pred-succ
     * 2. finding the specified task in imported data
     */
    private int savePredecessors(Connection conn, int errorCount) throws ServiceException{
        String fromtaskid = "", totaskid = "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d");
        DbResults rs = DbUtil.executeQuery(conn, "SELECT taskid, predecessor FROM tempImportData");
        while (rs.next()) {
            try{
                totaskid = rs.getString("taskid");
                String pred = rs.getString("predecessor");
                if (!StringUtil.equal(pred, "")) {
                    String predsearr[] = pred.split(",");
                    for (int i = 0; i < predsearr.length; i++) {
                        DbResults rs1 = DbUtil.executeQuery(conn, "SELECT taskid, duration FROM tempImportData WHERE taskindex = ?",
                                new Object[]{predsearr[i]});
                        if (rs1.next()) {
                            fromtaskid = rs1.getString("taskid");
                            double duration = projdb.getDuration(rs1.getString("duration"));
                            Date sd = null, ed = null;
                            DbResults to = DbUtil.executeQuery(conn, "SELECT startdate FROM proj_task WHERE taskid = ?", totaskid);
                            if(to.next())
                                sd = sdf.parse(to.getObject("startdate").toString());
                            DbResults from = DbUtil.executeQuery(conn, "SELECT enddate FROM proj_task WHERE taskid = ?", fromtaskid);
                            if(from.next())
                                ed = sdf.parse(from.getObject("enddate").toString());
                            if(sd != null && ed != null){
                                if((sd.after(ed) && duration != 0) || ((sd.after(ed) || sd.equals(ed)) && duration == 0)){
                                    projdb.addLink(conn, fromtaskid, totaskid);
                                } else {
                                    ImportLogHandler.updateTaskLog(conn, totaskid, KWLErrorMsgs.iLPredInvalidDate);
                                    errorCount++;
                                }
                            } else{
                                ImportLogHandler.updateTaskLog(conn, totaskid, KWLErrorMsgs.iLPredInvalidDate);
                                errorCount++;
                            }
                        } else { // taskindex doesnt exist in tempImportData
                            ImportLogHandler.updateTaskLog(conn, totaskid, KWLErrorMsgs.iLPredNotFound);
                            errorCount++;
                        }
                    }
                }
            } catch (ParseException ex) {
                ImportLogHandler.updateTaskLog(conn, totaskid, KWLErrorMsgs.iLExPred);
                errorCount++;
            } catch (ServiceException ex) {
                ImportLogHandler.updateTaskLog(conn, totaskid, KWLErrorMsgs.iLExPred);
                errorCount++;
        }
        }
        return errorCount;
    }

    /**
     * Establish parent-child relationship in imported tasks. checks for
     * 1. immediate above parent task
     * 2. finding the specified task in imported data
     * 3. WBS or Outline type of task heirarchy (for Non-Deskera CSVs)
     */
    private int saveParent(Connection conn, String projectid, int errorCount) throws ServiceException{
        String taskid = "";
        DbResults rs = DbUtil.executeQuery(conn, "SELECT * FROM tempImportData");
        String parentid = "";
        boolean flag = false, nonDeskeraTypeParentField = false;
            while (rs.next()) {
            try{
                flag = false;
                String parentString = rs.getString("parent");
                int parent = 0;
                int index = rs.getInt("taskindex");
                taskid = rs.getString("taskid");
                if (parentString.contains(".")) {
                    String par = parentString.substring(0, parentString.lastIndexOf("."));
                    DbResults rsTemp = null;
                    if(par.contains(".")) {
                        rsTemp = DbUtil.executeQuery(conn, "SELECT * FROM tempImportData WHERE parent = ? ", new Object[]{par});
                    } else {
                        parent = Integer.parseInt(par);
                        rsTemp = DbUtil.executeQuery(conn, "SELECT * FROM tempImportData WHERE parent = ? ", new Object[]{parent});
                    }
                    if (rsTemp.next()) {
                        parent = rsTemp.getInt("taskindex");
                    } else {
                        ImportLogHandler.updateTaskLog(conn, taskid, KWLErrorMsgs.iLParentNotFound);
                        errorCount++;
                    }
                    nonDeskeraTypeParentField = true;
                } else {
                    if(!nonDeskeraTypeParentField){ // deskera CSV having direct parent index
                        if(!StringUtil.isNullOrEmpty(parentString))
                            parent = Integer.parseInt(parentString);
                        else 
                            parent = 0;
                    }
                }

                if (parent == 0) {
                    parentid = "0";
                    flag = false;
                } else {
                    Object[] temp = validateParentTask(conn, taskid, index, parent, errorCount);
                    flag = (Boolean) temp[0];
                    parentid = (String) temp[1];
                }

                if(flag){
                    projdb.updateParentFieldOfRecord(conn, taskid, parentid);
                    DbUtil.executeUpdate(conn, "UPDATE proj_task SET isparent = true WHERE taskid = ?", parentid);
                    DbUtil.executeUpdate(conn, "UPDATE proj_task SET level=level+1 WHERE taskid = ?", taskid);
                } else {
                    projdb.updateParentFieldOfRecord(conn, taskid, parentid);
                }
            } catch(ServiceException ex){
            ImportLogHandler.updateTaskLog(conn, taskid, KWLErrorMsgs.iLExParent);
            errorCount++;
        }
        }
        errorCount = updateAllParents(conn, projectid, errorCount);
        return errorCount;
    }

    private Object[] validateParentTask(Connection conn, String taskid, int index, int parent, int errorCount) throws ServiceException{
        String parentid = "0";
        Object[] o = new Object[2];
        boolean flag = false;
        if (parent < index) {
            if(parent != index-1){
                DbResults rsTemp = getTempTask(conn, index-1, taskid, errorCount);
                if (rsTemp.next()) {
                    String parentString = rsTemp.getString("parent");
                    int tempParent = -1;
                    DbResults rs2 = null;
                    if (parentString.contains(".")){
                        String par = parentString.substring(0, parentString.lastIndexOf("."));
                        if(par.contains(".")) {
                            rs2 = DbUtil.executeQuery(conn, "SELECT * FROM tempImportData WHERE parent = ? ", new Object[]{par});
                        } else {
                            tempParent = Integer.parseInt(par);
                            rs2 = DbUtil.executeQuery(conn, "SELECT * FROM tempImportData WHERE parent = ? ", new Object[]{tempParent});
                        }
                    } else {
                        tempParent = Integer.parseInt(parentString);
                        rs2 = DbUtil.executeQuery(conn, "SELECT * FROM tempImportData WHERE taskindex = ? ", new Object[]{tempParent});
                    }
                    if (rs2.next()) {
                        tempParent = rs2.getInt("taskindex");
                        if((parentString.contains(".") && tempParent <= parent) || (!parentString.contains(".") && tempParent >= parent)){
                            DbResults rs1 = getTempTask(conn, parent, taskid, errorCount);
                            if (rs1.next()) {
                                parentid = rs1.getString("taskid");
                                flag = true;
                            }
                        } else {
                            ImportLogHandler.updateTaskLog(conn, taskid, KWLErrorMsgs.iLParentInvalidIndex);
                            errorCount++;
                        }
                    } else {
                        ImportLogHandler.updateTaskLog(conn, taskid, KWLErrorMsgs.iLParentNotFound);
                        errorCount++;
                    }
                }
            } else {
                DbResults rs1 = getTempTask(conn, parent, taskid, errorCount);
                if (rs1.next()) {
                    parentid = rs1.getString("taskid");
                    flag = true;
                }
            }
        } else {
            ImportLogHandler.updateTaskLog(conn, taskid, KWLErrorMsgs.iLParentInvalidIndex);
            errorCount++;
        }
        o[0] = flag;
        o[1] = parentid;
        return o;
    }

    private DbResults getTempTask(Connection conn, int taskindex, String taskid, int errorCount) throws ServiceException{
        DbResults rs1 = DbUtil.executeQuery(conn, "SELECT * FROM tempImportData WHERE taskindex = ? ", new Object[]{taskindex});
        if (rs1.size() != 0) {
            return rs1;
        } else {
            ImportLogHandler.updateTaskLog(conn, taskid, KWLErrorMsgs.iLParentNotFound);
            errorCount++;
        }
        return rs1;
    }

    /**
     * Update dates, percent complete of newly imported parent tasks
     */
    private int updateAllParents(Connection conn, String projectid, int errorCount) throws ServiceException{
        String sdt = "";
        String edt = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        double percentcomplete = 0, totaldur = 0, duration = 0;
        String taskid = "";
        DbResults rsAll = DbUtil.executeQuery(conn, "SELECT * FROM tempImportData");
        while (rsAll.next()) {
            try{
                percentcomplete = 0;
                duration = 0;
                totaldur = 0;
                taskid = rsAll.getString("taskid");
                DbResults dirChilds = DbUtil.executeQuery(conn, "SELECT taskid, duration, percentcomplete " +
                        "FROM proj_task WHERE parent = ? AND projectid = ?",
                        new Object[]{taskid, projectid});
                while (dirChilds.next()) {
                    String strdur = dirChilds.getString("duration");
                    duration = projdb.getDuration(strdur);
                    percentcomplete += (int) (duration * ((double) dirChilds.getInt("percentcomplete") / 100));
                    totaldur += duration;
                }
                DbResults rs = DbUtil.executeQuery(conn, "SELECT projectid, MIN(startdate) AS stdate, " +
                        "MAX(enddate) AS edate FROM proj_task WHERE parent=? AND projectid = ? GROUP BY projectid",
                    new Object[]{taskid, projectid});
                if (rs.next()) {
                    double percent = 0;
                    if (totaldur != 0) {
                        percent = ((percentcomplete / totaldur) * 100);
                    }
                    if (!rs.isNull("stdate")) {
                        sdt = rs.getObject("stdate").toString();
                        edt = rs.getObject("edate").toString();
                        Date stdate = sdf.parse(sdt);
                        Date endate = sdf.parse(edt);

                        int[] nonworkweekArr = projdb.getNonWorkWeekDays(conn, projectid);
                        String holidayArr[] = projdb.getCompHolidays(conn, projectid, "");
                        String dur1 = getActualDuration_importCSV(stdate, endate, nonworkweekArr, holidayArr, "");
                        duration = SchedulingUtilities.parseDuration(dur1);
                        percentcomplete = (int) percent;

                        DbUtil.executeUpdate(conn, "UPDATE proj_task SET startdate=?, enddate=?, duration =?,percentcomplete=? WHERE taskid=?",
                                new Object[]{stdate, endate, duration, percentcomplete, taskid});
                    }
                }
            } catch (ParseException ex) {
                ImportLogHandler.updateTaskLog(conn, taskid, KWLErrorMsgs.iLExParent);
                errorCount++;
            } catch (ServiceException ex) {
                ImportLogHandler.updateTaskLog(conn, taskid, KWLErrorMsgs.iLExParent);
                errorCount++;
            }
        }
        return errorCount;
    }

    /**
     * Deleting data in the temp table after all processing is complete irrespective of operation being success or failure
     */
    private void clearTempData(Connection conn) throws ServiceException {
        DbUtil.executeUpdate(conn, "DELETE FROM tempImportData");
    }

    /**
     * save baseline if user has asked for it
     */
    private void saveBaseline(Connection conn, HttpServletRequest request) throws ServiceException {
        try{
            String projectid = request.getParameter("projectid");
            String userid = AuthHandler.getUserid(request);
            String projName = projdb.getProjectName(conn, projectid);
            int bsln = Integer.parseInt(request.getParameter("isbaseline"));
            boolean isBaseline = (bsln == 1) ? true : false;
            if(isBaseline){
                boolean isUnderLimit = projdb.checkBaseline(conn, projectid);
                if(isUnderLimit) {
                    projdb.saveBaseline(conn, projectid, userid,
                            projName.concat(" Baseline - ").concat(new SimpleDateFormat("yyyy-MM-dd").format(new Date())),
                            "Baseline created on importing CSV file.");
                }
            }
        } catch (SessionExpiredException ex){
            throw ServiceException.FAILURE("importProjectPlanCSV.saveBaseline", ex);
        }
    }

    /**
     * Creating CSV having skipped records for which tasks were not created
     */
    private void createFailureFile(Connection conn, String failedRecords, String filename) {
        FileOutputStream fo = null;
        try {
            if(failedRecords.split("\n").length >= 2){
                String destinationDirectory = StorageHandler.GetDocStorePath() + StorageHandler.GetFileSeparator() + "importplans";
                fo = new FileOutputStream(destinationDirectory + StorageHandler.GetFileSeparator() +"reject_" + filename);
                fo.write(failedRecords.getBytes());
                fo.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConfigurationException ex) {
            Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(fo != null)
                    fo.close();
            } catch (IOException ex) {
                Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Creating CSV having errors found during post processing
     */
    private void createPostProcessErrorFile(Connection conn, HttpServletRequest request, int errorCount) {
        FileOutputStream fo = null;
        try {
            String destinationDirectory = StorageHandler.GetDocStorePath() + StorageHandler.GetFileSeparator() + "importplans";
            String filename = request.getParameter("filename");
            fo = new FileOutputStream(destinationDirectory + StorageHandler.GetFileSeparator() + "error_" + filename);
            DbResults dr = DbUtil.executeQuery(conn, "SELECT * FROM tempImportData");
            String recs = "";
            int cnt = 0;
            while(dr.next()){
                cnt++;
                try{
                    JSONObject temp = dr.toJSONObject(cnt);
                    if(cnt == 1){
                        Iterator i = temp.keys();
                        while(i.hasNext()){
                            String entry = (String) i.next();
                            if(!StringUtil.equal("taskid", entry))
                                recs += "\""+ entry + "\",";
                        }
                        recs = recs.substring(0, recs.length()-1);
                    } else {
                        if(dr.getString("log").compareTo("") != 0){
                            recs += "\n";
                            Iterator i = temp.keys();
                            while(i.hasNext()){
                                String entry = (String) i.next();
                                if(!StringUtil.equal("taskid", entry) && !entry.contains("date"))
                                    recs += "\"" + temp.getString(entry) + "\",";
                                if(entry.contains("date"))
                                    recs += "\"" + temp.getString(entry).split(" ")[0] + "\",";
                            }
                            recs = recs.substring(0, recs.length()-1);
                        }
                    }
                } catch (com.krawler.utils.json.base.JSONException ex) {
                    Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            fo.write(recs.getBytes());
            fo.flush();
            ImportLogHandler.updateErrorsCount(conn, filename.substring(0, filename.lastIndexOf(".")), errorCount);
        } catch (ServiceException ex) {
            Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConfigurationException ex) {
            Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(fo != null)
                    fo.close();
            } catch (IOException ex) {
                Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Make an audit log entry of Project plan being imported in CSV format.
     * This is done before the post-processing of the data.
     */
    private void insertAuditLog(Connection conn, HttpServletRequest request) {
        try {
            int auditMode = 1;
            String loginid = AuthHandler.getUserid(request);
            String userFullName = AuthHandler.getAuthor(conn, loginid);
            String userName = AuthHandler.getUserName(request);
            String projId = request.getParameter("projectid");
            String projName = projdb.getProjectName(conn, projId);
            String companyid = AuthHandler.getCompanyid(request);
            String ipAddress = AuthHandler.getIPAddress(request);

            String params = userFullName + " ("+ userName +"), " + "[ CSV ]" + ", " + projName;
            AuditTrail.insertLog(conn, "118", loginid, projId, projId, companyid, params, ipAddress, auditMode);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String cleanHTML(String strText) throws IOException {
        return StringUtil.serverHTMLStripper(strText);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (com.krawler.utils.json.base.JSONException ex) {
            Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (com.krawler.utils.json.base.JSONException ex) {
            Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "Imports project plan from csv document";
    }// </editor-fold>
}
