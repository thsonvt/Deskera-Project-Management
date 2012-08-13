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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.krawler.common.util.KrawlerLog;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.krawler.common.util.CsvReader;
import com.krawler.common.util.Constants;
import com.krawler.common.util.KWLErrorMsgs;
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.esp.company.CompanyDAO;
import com.krawler.esp.company.CompanyDAOImpl;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.handlers.ImportLogHandler;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.notification.MailNotification;
import com.krawler.esp.notification.NotificationTemplate;
import com.krawler.esp.user.User;
import com.krawler.esp.user.UserDAO;
import com.krawler.esp.user.UserDAOImpl;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.svnwebclient.configuration.ConfigurationException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;

public class ExportImportContactsServlet extends HttpServlet {

    HashMap arrParam = new HashMap();
    private static final String EMAIL_PATTERN = "^([a-zA-Z0-9_\\-\\.+]+)@(([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})$";
    private static final String NAME_PATTERN = "^[_\\.A-Za-z0-9- ]*$";
    private static final String PHONE_PATTERN = "^[0-9\\(\\)\\/\\+ \\-]*$";
    Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
    Pattern namePattern = Pattern.compile(NAME_PATTERN);
    Pattern phonePattern = Pattern.compile(PHONE_PATTERN);
    Matcher matcher;

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        String doAction = request.getParameter("do");
        Connection conn = null;
        String result = KWLErrorMsgs.rsSuccessFalse;
        String res = "";
        try {
            conn = DbPool.getConnection();
            if (doAction.compareToIgnoreCase("export") == 0) {
                doExport(conn, request, response);
            } else if (doAction.compareToIgnoreCase("import") == 0) {
                res = doImport(request, conn);
            } else if (doAction.compareToIgnoreCase("getheaders") == 0) {
                res = getFileHeaders(request);
            }

            if (doAction.compareToIgnoreCase("export") == 0) {
                result = KWLErrorMsgs.rsSuccessTrue;
            } else if (doAction.compareToIgnoreCase("import") == 0 || doAction.compareToIgnoreCase("getheaders") == 0) {
                result = res;
            }
            conn.commit();
        } catch (IOException ex) {
            DbPool.quietRollback(conn);
            Logger.getLogger(ExportImportContactsServlet.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            Logger.getLogger(ExportImportContactsServlet.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            DbPool.quietRollback(conn);
            Logger.getLogger(ExportImportContactsServlet.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            DbPool.quietRollback(conn);
            Logger.getLogger(ExportImportContactsServlet.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            DbPool.quietClose(conn);
            response.getWriter().write(result);
            response.getWriter().close();
        }
    }

    private void doExport(Connection conn, HttpServletRequest request, HttpServletResponse response)
            throws SessionExpiredException, ServiceException, IOException {
        StringBuilder resultStr = new StringBuilder();
        String endOfRec = "\n";
        String endOfField = ",";
        String tmp = "";
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=MyContacts.csv");
        DbResults rs = null;
        OutputStream opStream = null;
        try {
            opStream = response.getOutputStream();
            for (int i = 0; i < Constants.CSV_HEADER_MSOUTLOOK.length; i++) {
                tmp += Constants.CSV_HEADER_MSOUTLOOK[i] + endOfField;
            }
            resultStr.append(tmp.substring(0, tmp.length() - 1) + endOfRec);
            String userid = AuthHandler.getUserid(request);
            String FETCH_CONTACTS_INFO = " select concat(users.fname,' ',users.lname) as name,users.emailid, users.contactno as phone, users.address"
                    + " from userrelations inner join users on (users.userid = userrelations.userid2 or users.userid = userrelations.userid1)"
                    + " inner join userlogin on users.userid = userlogin.userid where userlogin.isactive = true and users.userid != ? and (userid1 = ? or userid2 = ?)"
                    + " union all select addressbook.name as name,addressbook.emailid,addressbook.phone,addressbook.address"
                    + " from addressbook where userid = ?"
                    + " order by name";
            rs = DbUtil.executeQuery(conn, FETCH_CONTACTS_INFO, new Object[]{userid, userid, userid, userid,});
            while (rs.next()) {
                resultStr.append('"' + rs.getObject("name").toString() + '"' + endOfField + '"' + rs.getObject("emailid").toString() + '"' + endOfField + '"' + rs.getObject("phone").toString() + '"' + endOfField + '"' + rs.getObject("address").toString() + '"' + endOfRec);
            }
            opStream.write(resultStr.toString().getBytes());
            opStream.flush();
        } catch (Exception ex) {
            Logger.getLogger(ExportImportContactsServlet.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            opStream.close();
        }
    }

    private String doImport(HttpServletRequest request, Connection conn)
            throws SessionExpiredException, ServiceException, IOException, JSONException, ConfigurationException {
        String contentType = request.getContentType();
        CsvReader csvReader = null;
        FileInputStream fstream = null;
        JSONObject mapping = new JSONObject();
        if (request.getParameter("headerMap") != null) {
            mapping = new JSONObject(request.getParameter("headerMap"));
        }
        String importType = request.getParameter("importType");
        String csvFile = request.getParameter("filename");
        String fileID = request.getParameter("fileid");
        String realFileName = request.getParameter("realFileName");
        JSONObject jsnobj = new JSONObject();
        String userid = AuthHandler.getUserid(request);
        String nameIndex = mapping.getString("name");
        String emailIndex = mapping.getString("emailid");
        String phoneIndex = mapping.getString("contactno");
        String addressIndex = mapping.getString("address");
        String companyID = AuthHandler.getCompanyid(request);
        String destinationDirectory = StorageHandler.GetDocStorePath() + StorageHandler.GetFileSeparator() + "importcontacts" +StorageHandler.GetFileSeparator()+ userid;
        File csv = new File(destinationDirectory + StorageHandler.GetFileSeparator() + csvFile);

        fstream = new FileInputStream(csv);
        csvReader = new CsvReader(new InputStreamReader(fstream));
        int noOfRecords = 0;
        csvReader.readRecord();
        int threadStartLimit = Integer.parseInt(ConfigReader.getinstance().get("contact_thread_start_limit"));
        while (csvReader.readRecord()) {
            noOfRecords++;
            if (noOfRecords > threadStartLimit) {
                break;
            }
        }
        fstream.close();
        csvReader.close();
        fstream = new FileInputStream(csv);
        csvReader = new CsvReader(new InputStreamReader(fstream));
        String msg = "";
        ThreadImpl r = new ThreadImpl(csvReader, userid, importType, realFileName, fileID, destinationDirectory, nameIndex, phoneIndex, addressIndex, emailIndex, companyID);
        if (noOfRecords > threadStartLimit) {
            Thread t = new Thread(r);
            t.start();
        } else {
            msg = r.importContacts(csvReader, userid, importType, realFileName, fileID, destinationDirectory, nameIndex, phoneIndex, addressIndex, emailIndex, companyID);
        }
        jsnobj.put("success", "true");
        jsnobj.put("msg", msg);
        String str = jsnobj.toString();
        return str;
    }

    private String getFileHeaders(HttpServletRequest request)
            throws SessionExpiredException, ServiceException, IOException, com.krawler.utils.json.JSONException {
        String contentType = request.getContentType();
        CsvReader csvReader = null;
        String header = "";
        String realFileName = request.getParameter("fileName");
        {
            FileInputStream fstream = null;

            try {
                if ((contentType != null) && (contentType.indexOf("multipart/form-data") >= 0)) {
                    String fileid = UUID.randomUUID().toString();
                    String userid = AuthHandler.getUserid(request);
                    String f1 = uploadDocument(request, fileid, userid);
                    if (f1.length() != 0) {
                        String destinationDirectory = StorageHandler.GetDocStorePath() + StorageHandler.GetFileSeparator() + "importcontacts" +StorageHandler.GetFileSeparator()+ userid;
                        File csv = new File(destinationDirectory + StorageHandler.GetFileSeparator() + f1);
                        String mimetype = SVNFileUtil.detectMimeType(csv);
                        boolean flag = false;
                        if (StringUtil.isNullOrEmpty(mimetype)) {
                            flag = true;
                        }
                        if (flag) {
                            fstream = new FileInputStream(csv);
                            csvReader = new CsvReader(new InputStreamReader(fstream));
                            csvReader.readRecord();
                            int i = 0;
                            if (!StringUtil.isNullOrEmpty(csvReader.get(i))) {
                                while (!(StringUtil.isNullOrEmpty(csvReader.get(i)))) {
                                    header += "{\"header\":\"" + csvReader.get(i) + "\",\"index\":" + i + "},";
                                    i++;
                                }
                                header = header.substring(0, header.length() - 1);
                                header = "{\"success\": true,\"FileName\":\"" + f1 + "\",\"RealFileName\":\"" + realFileName + "\",\"FileID\":\"" + fileid + "\",\"Headers\":[" + header + "]}";
                                // e.g. Header= "{'Task Name':0,'Start Date':1,'End Date':2,'Proirity':3,'Percent Completed':4,'Notes':5}";
                            } else {
                                header = "{\"success\": false}";
                            }
                        }else{
                            header = "{\"success\": false}";
                        }
                    } else {
                        header = "{\"success\": true,\"FileName\":\"\"}";
                    }

                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ExportImportContactsServlet.class.getName()).log(Level.SEVERE, "File not Found", ex);
            } catch (ConfigurationException ex) {
                KrawlerLog.op.warn(ex.getMessage());
                throw ServiceException.FAILURE("ExportImportContactsServlet", ex);
            } finally {
                if (csvReader != null) {
                    csvReader.close();
                }
                if (fstream != null) {
                    fstream.close();
                }
            }
        }
        return header;
    }

    public static String uploadDocument(HttpServletRequest request, String fileid, String userId) throws ServiceException {
        String result = "";
        try {
            String destinationDirectory = StorageHandler.GetDocStorePath() + StorageHandler.GetFileSeparator() + "importcontacts" +StorageHandler.GetFileSeparator()+ userId;
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
            throw ServiceException.FAILURE("ExportImportContactServlet.uploadDocument", ex);
        } catch (Exception ex) {
            Logger.getLogger(importToDoTask.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("ExportImportContactServlet.uploadDocument", ex);
        }
        return result;
    }

    private String cleanHTML(String strText) throws IOException {
        return StringUtil.serverHTMLStripper(strText);
    }

    private String getName(CsvReader csvrdr) throws IOException {
        /*
         * "Title","First Name","Middle Name","Last Name","Suffix"
         */
        String result = null;
        if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("First Name")))))) {
            result = cleanHTML(csvrdr.get(csvrdr.getIndex("First Name")));
            if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Middle Name")))))) {
                result += " " + cleanHTML(csvrdr.get(csvrdr.getIndex("Middle Name")));
            }
            if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Middle Name")))))) {
                result += " " + cleanHTML(csvrdr.get(csvrdr.getIndex("Last Name")));
            }
        } else {
            result = "";
        }
        return result;
    }

    private String getAddress(CsvReader csvrdr) throws IOException {
        /*
         * "Business Street","Business Street 2","Business Street 3","Business
         * City","Business Postal Code","Business State","Business
         * Country/Region", "Home Street","Home Street 2","Home Street 3","Home
         * City","Home State","Home Postal Code","Home Country/Region", "Other
         * Street","Other Street 2","Other Street 3","Other City","Other
         * State","Other Postal Code","Other Country/Region"
         */
        String result = null;
        if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Business Street")))))) {
            result = cleanHTML(csvrdr.get(csvrdr.getIndex("Business Street")));
            if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Business Street 2")))))) {
                result += ",\n" + cleanHTML(csvrdr.get(csvrdr.getIndex("Business Street 2")));
            }
            if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Business Street 3")))))) {
                result += ",\n" + cleanHTML(csvrdr.get(csvrdr.getIndex("Business Street 3")));
            }
            if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Business City")))))) {
                result += ",\n" + cleanHTML(csvrdr.get(csvrdr.getIndex("Business City"))) + "-" + cleanHTML(csvrdr.get(csvrdr.getIndex("Business Postal Code")));
            }
            if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Business State")))))) {
                result += ",\n" + cleanHTML(csvrdr.get(csvrdr.getIndex("Business State")));
            }
            if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Business Country/Region")))))) {
                result += ",\n" + cleanHTML(csvrdr.get(csvrdr.getIndex("Business Country/Region")));
            }
        } else if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Home Street")))))) {
            result = cleanHTML(csvrdr.get(csvrdr.getIndex("Home Street")));
            if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Home Street 2")))))) {
                result += ",\n" + cleanHTML(csvrdr.get(csvrdr.getIndex("Home Street 2")));
            }
            if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Home Street 3")))))) {
                result += ",\n" + cleanHTML(csvrdr.get(csvrdr.getIndex("Home Street 3")));
            }
            if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Home City")))))) {
                result += ",\n" + cleanHTML(csvrdr.get(csvrdr.getIndex("Home City"))) + "-" + cleanHTML(csvrdr.get(csvrdr.getIndex("Home Postal Code")));
            }
            if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Home State")))))) {
                result += ",\n" + cleanHTML(csvrdr.get(csvrdr.getIndex("Home State")));
            }
            if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Home Country/Region")))))) {
                result += ",\n" + cleanHTML(csvrdr.get(csvrdr.getIndex("Home Country/Region")));
            }
        } else if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Other Street")))))) {
            result = cleanHTML(csvrdr.get(csvrdr.getIndex("Other Street")));
            if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Other Street 2")))))) {
                result += ",\n" + cleanHTML(csvrdr.get(csvrdr.getIndex("Other Street 2")));
            }
            if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Other Street 3")))))) {
                result += ",\n" + cleanHTML(csvrdr.get(csvrdr.getIndex("Other Street 3")));
            }
            if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Other City")))))) {
                result += ",\n" + cleanHTML(csvrdr.get(csvrdr.getIndex("Other City"))) + "-" + cleanHTML(csvrdr.get(csvrdr.getIndex("Other Postal Code")));
            }
            if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Other State")))))) {
                result += ",\n" + cleanHTML(csvrdr.get(csvrdr.getIndex("Other State")));
            }
            if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Other Country/Region")))))) {
                result += ",\n" + cleanHTML(csvrdr.get(csvrdr.getIndex("Other Country/Region")));
            }
        } else {
            result = "";
        }
        return result;
    }

    private String getEmail(CsvReader csvrdr) throws IOException {
        /*
         * "E-mail Address","E-mail 2 Address","E-mail 3 Address"
         */
        String result = null;
        if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("E-mail Address")))))) {
            result = cleanHTML(csvrdr.get(csvrdr.getIndex("E-mail Address")));
        } else if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("E-mail 2 Address")))))) {
            result = cleanHTML(csvrdr.get(csvrdr.getIndex("E-mail 2 Address")));
        } else if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("E-mail 3 Address")))))) {
            result = cleanHTML(csvrdr.get(csvrdr.getIndex("E-mail 3 Address")));
        } else {
            result = "";
        }

        if (!StringUtil.isNullOrEmpty(result)) {
            if (!StringUtil.serverValidateEmail(result)) // Email format validation
            {
                result = "";
            }
        }
        return result;
    }

    private String getPhone(CsvReader csvrdr) throws IOException {
        /*
         * "Primary Phone","Mobile Phone","Business Phone","Home Phone","Other
         * Phone","Assistant's Phone", "Company Main Phone","Car
         * Phone","Business Phone 2","Home Phone 2"
         */
        String result = null;
        if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Primary Phone")))))) {
            result = cleanHTML(csvrdr.get(csvrdr.getIndex("Primary Phone")));
        } else if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Mobile Phone")))))) {
            result = cleanHTML(csvrdr.get(csvrdr.getIndex("Mobile Phone")));
        } else if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Business Phone")))))) {
            result = cleanHTML(csvrdr.get(csvrdr.getIndex("Business Phone")));
        } else if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Home Phone")))))) {
            result = cleanHTML(csvrdr.get(csvrdr.getIndex("Home Phone")));
        } else if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Other Phone")))))) {
            result = cleanHTML(csvrdr.get(csvrdr.getIndex("Other Phone")));
        } else if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Assistant's Phone")))))) {
            result = cleanHTML(csvrdr.get(csvrdr.getIndex("Assistant's Phone")));
        } else if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Company Main Phone")))))) {
            result = cleanHTML(csvrdr.get(csvrdr.getIndex("Company Main Phone")));
        } else if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Car Phone")))))) {
            result = cleanHTML(csvrdr.get(csvrdr.getIndex("Car Phone")));
        } else if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Business Phone 2")))))) {
            result = cleanHTML(csvrdr.get(csvrdr.getIndex("Business Phone 2")));
        } else if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(csvrdr.getIndex("Home Phone 2")))))) {
            result = cleanHTML(csvrdr.get(csvrdr.getIndex("Home Phone 2")));
        } else {
            result = "";
        }
        return result;
    }

    private File getfile(HttpServletRequest request) {

        DiskFileUpload fu = new DiskFileUpload();
        String Ext = null;
        File uploadFile = null;
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
                    if (fi.getSize() != 0) {
                        uploadFile = File.createTempFile("contacts", ".csv");
                        fi.write(uploadFile);
                    }
                } catch (Exception e) {
                    KrawlerLog.op.warn("Problem While Reading file :" + e.toString());
                }
            } else {
                arrParam.put(fi.getFieldName(), fi.getString());
            }
        }

        return uploadFile;
    }

    private boolean validateEmail(String email) {

        matcher = emailPattern.matcher(email);
        return matcher.matches();
    }

    private boolean validatePhone(String phone) {
        if (StringUtil.isNullOrEmpty(phone)) {
            return false;
        }
        matcher = phonePattern.matcher(phone);
        return matcher.matches();
    }

    private boolean validateName(String name) {
        if (StringUtil.isNullOrEmpty(name)) {
            return false;
        }
        matcher = namePattern.matcher(name);
        return matcher.matches();
    }

    private String validContactNo(String phone) {
        String s = "";
        if (validatePhone(phone)) {
            s = phone;
        } else {
            for (int i = 0; i < phone.length(); i++) {
                matcher = phonePattern.matcher("" + phone.charAt(i));
                if (matcher.matches()) {
                    s += phone.charAt(i);
                }

            }
        }
        return s;
    }

    private void createFailureFile(String failedRecords, String fileName, String destinationDirectory) {
        FileOutputStream fo = null;
        try {
            if (failedRecords.split("\n").length >= 2) {
                fo = new FileOutputStream(destinationDirectory + StorageHandler.GetFileSeparator() + "reject_" + fileName);
                fo.write(failedRecords.getBytes());
                fo.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (fo != null) {
                    fo.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>   

    public class ThreadImpl implements Runnable {

        public ThreadImpl(CsvReader csvReader, String userid, String importType, String fileName, String fileID, String destinationDirectory, String nameIndex, String phoneIndex, String addressIndex, String emailIndex, String companyID) {
            this.csvReader = csvReader;
            this.nameIndex = nameIndex;
            this.phoneIndex = phoneIndex;
            this.addressIndex = addressIndex;
            this.emailIndex = emailIndex;
            this.importType = importType;
            this.userID = userid;
            this.fileName = fileName;
            this.fileID = fileID;
            this.destinationDirectory = destinationDirectory;
            this.companyID = companyID;
        }
        int totalRecords = 0;
        int skipped = 0;
        int count = 0;
        int existsContact = 0;
        CsvReader csvReader;
        String nameIndex;
        String phoneIndex;
        String addressIndex;
        String emailIndex;
        String importType;
        String userID;
        String fileName;
        String fileID;
        String destinationDirectory;
        String companyID;

        @Override
        public void run() {
            importContacts(csvReader, userID, importType, fileName, fileID, destinationDirectory, nameIndex, phoneIndex, addressIndex, emailIndex, companyID);
            sendMail();
        }

        public String importContacts(CsvReader csvReader, String userid, String importType, String realFileName, String fileID, String destinationDirectory, String nameIndex, String phoneIndex, String addressIndex, String emailIndex, String companyID) {
            String skipMsg = "";
            String failedRecords = "";
            String msg = "", logkey = "";
            String uploadedFileName = fileID + ".csv";
            try {
                Connection con = DbPool.getConnection();
                try {
                    csvReader.readRecord();
                    String header = csvReader.getRawRecord();
                    failedRecords = header + ",\"Log description\"";
                    while (csvReader.readRecord()) {

                        totalRecords++;
                        String namevalue = csvReader.get(Integer.parseInt(nameIndex));
                        String emailvalue = csvReader.get(Integer.parseInt(emailIndex));
                        String phonevalue = "";
                        if (namevalue.length() > 50) {
                            namevalue = namevalue.substring(0, 50);
                        }
                        if (!StringUtil.isNullOrEmpty(phoneIndex)) {
                            phonevalue = csvReader.get(Integer.parseInt(phoneIndex));
                            phonevalue = validContactNo(phonevalue);
                            if (phonevalue.length() > 15) {
                                phonevalue = phonevalue.substring(0, 15);
                            }
                        }
                        String addressvalue = "";
                        if (!StringUtil.isNullOrEmpty(addressIndex)) {
                            addressvalue = csvReader.get(Integer.parseInt(addressIndex));
                            if (addressvalue.length() > 100) {
                                addressvalue = addressvalue.substring(0, 100);
                            }
                        }
                        if (!validateEmail(emailvalue) || emailvalue.length() > 100) {
                            skipMsg = KWLErrorMsgs.iLConInvalidEmail;
                            failedRecords += "\n" + csvReader.getRawRecord() + ",\"" + skipMsg + "\"";
                            skipped++;
                            continue;
                        }
                        int type = 0;
                        if (importType.equalsIgnoreCase("unique")) {
                            type = 1;
                        } else if (importType.equalsIgnoreCase("duplicate")) {
                            type = 2;
                        } else if (importType.equalsIgnoreCase("replace")) {
                            type = 3;
                        }

                        int row = AddressBookServlet.saveContactByOperationType(con, type, userid, namevalue, emailvalue, phonevalue, addressvalue);
                        count += row;
                        if (row == 0) {
                            skipMsg = KWLErrorMsgs.iLConExists;
                            failedRecords += "\n" + csvReader.getRawRecord() + ",\"" + skipMsg + "\"";
                            existsContact++;
                        }
                    }
                    if (skipped == totalRecords) {
                        msg = KWLErrorMsgs.iLConInvalidMapping;
                        logkey = "pm.contacts.import.csv.log.incorrect";
                    } else if (existsContact == totalRecords) {
                        msg = KWLErrorMsgs.iLConNoNewCont;
                        logkey = "pm.contacts.import.csv.log.nonewcontacts";
                    } else if (count == totalRecords) {
                        msg = KWLErrorMsgs.iLConImportSuccess;
                        logkey = "pm.contacts.import.csv.log.success";
                    } else {
                        msg = KWLErrorMsgs.iLConDiscarded;
                        logkey = "pm.contacts.import.csv.log.discarded";
                    }
                    ImportLogHandler.insertImportLog(con, fileID, realFileName, uploadedFileName, "CSV", msg, "Contacts", totalRecords, skipped + existsContact, 0, userid, companyID, "", logkey, "pm.contacts.text");
                    con.commit();
                } catch (ServiceException ex) {
                    DbPool.quietRollback(con);
                    Logger.getLogger(ExportImportContactsServlet.class.getName()).log(Level.SEVERE, "DataBase Connection Error", ex);
                } catch (IOException ex) {
                    DbPool.quietRollback(con);
                    Logger.getLogger(ExportImportContactsServlet.class.getName()).log(Level.SEVERE, "File Operation Error", ex);
                } catch (SQLException ex) {
                    DbPool.quietRollback(con);
                    Logger.getLogger(ExportImportContactsServlet.class.getName()).log(Level.SEVERE, "SQL Error", ex);
                } catch (JSONException ex) {
                    DbPool.quietRollback(con);
                    Logger.getLogger(ExportImportContactsServlet.class.getName()).log(Level.SEVERE, "JSON Error", ex);
                } catch (Exception ex) {
                    DbPool.quietRollback(con);
                    Logger.getLogger(ExportImportContactsServlet.class.getName()).log(Level.SEVERE, "Some Error", ex);
                } finally {
                    createFailureFile(failedRecords, uploadedFileName, destinationDirectory);
                    DbPool.quietClose(con);
                }
            } catch (ServiceException ex) {
                Logger.getLogger(ExportImportContactsServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
            return logkey;
        }

        private void sendMail() {
            Connection con=null;
            try {
                con = DbPool.getConnection();
                UserDAO udao = new UserDAOImpl();
                User user = udao.getUser(con, userID);
                List receiver = new ArrayList();
                receiver.add(user);

                CompanyDAO cdao = new CompanyDAOImpl();
                String sender = cdao.getSysEmailIdByCompanyID(con, companyID);

                String msg = NotificationTemplate.HTMLText.Contact_Import_Message(user.getFirstName(), fileName, totalRecords, count, (skipped + existsContact));

                MailNotification mail = new MailNotification();
                mail.setReciever(receiver);
                mail.setSender(sender);
                mail.setHtmlMsg(msg);
                mail.setSubject("Deskera Project Management - Report for data imported");
                mail.setMessage("");

                mail.notifyUser();
                DbPool.quietClose(con);
            } catch (ServiceException ex) {
                DbPool.quietRollback(con);
                Logger.getLogger(ExportImportContactsServlet.class.getName()).log(Level.SEVERE, "Database Connection Error.", ex);
            }finally{
                DbPool.quietClose(con);
            }
        }
    }
}
