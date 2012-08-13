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

import com.krawler.svnwebclient.configuration.ConfigurationException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.KrawlerLog;
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.handlers.AuditTrail;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.handlers.ProfileHandler;
import com.krawler.esp.handlers.projdb;
import javax.servlet.*;
import com.krawler.utils.json.base.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author trainee
 */
public class ExportProjectSummaryServlet extends HttpServlet {

    private static Font fontSmallRegular = FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.BLACK);
    private static Font fontSmallBold = FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.BLACK);
    private static Font fontRegular = FontFactory.getFont("Helvetica", 12, Font.NORMAL, Color.BLACK);
    private static Font fontBold = FontFactory.getFont("Helvetica", 12, Font.BOLD, Color.BLACK);
    private static Font fontBig = FontFactory.getFont("Helvetica", 24, Font.NORMAL, Color.BLACK);
    private static String imgPath = "";
    private static String companyName = "";
    private static String ProjectName = "";
    private static String ReportName = "";
    private static String baseName = "";
     private static String companySubDomain = "";
    private static String currSymbol = "";
    private Boolean landscape = false,  showLogo = true,  gridBorder = true;
    private int padding = 2;
    private PdfPTable header = null;
    private PdfPTable footer = null;

    public class EndPage extends PdfPageEventHelper {

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                Rectangle page = document.getPageSize();

                getHeaderFooter(document);
                // Add page header
                header.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
                header.writeSelectedRows(0, -1, document.leftMargin(), page.getHeight() - 10, writer.getDirectContent());

                // Add page footer
                footer.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
                footer.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin() - 5, writer.getDirectContent());

                // Add page border

                int bmargin = 8;  //border margin
                PdfContentByte cb = writer.getDirectContent();
                cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
                cb.setColorStroke(Color.LIGHT_GRAY);
                cb.stroke();


            } catch (JSONException e) {
                Logger.getLogger(ExportProjectReportServlet.class.getName()).log(Level.SEVERE, null, e);
                throw new ExceptionConverter(e);
            }
        }
    }
    //inner classends

    public static void clearAll() {
        imgPath = "";
        companyName = "";
        ProjectName = "";
        ReportName = "";
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SessionExpiredException {
        JSONObject j = null;
        Connection conn = null;

        clearAll();
        String projectId = request.getParameter("projectid");
        ProjectName = request.getParameter("projname");
        ReportName = request.getParameter("reportname");
        String loginid = AuthHandler.getUserid(request);
        String userName = AuthHandler.getUserName(request);
        String companyid = AuthHandler.getCompanyid(request);
        String ipAddress = AuthHandler.getIPAddress(request);
        String exportType = request.getParameter("exporttype");
        String filename = filename = ProjectName + "_" + ReportName + "." + exportType;
        ByteArrayOutputStream baos = null;
        try {
            conn = DbPool.getConnection();
            String cmpcurr = ProfileHandler.getCmpCurrFromProj(conn, projectId);
            try{
                char a1= (char) Integer.parseInt(cmpcurr,16);
                currSymbol = Character.toString(a1);
            } catch(Exception e){
                currSymbol = cmpcurr;
            }
            String baseid = request.getParameter("baselineid");
            String jbase = projdb.getBaselineDetails(conn, baseid);
            JSONObject temp = new JSONObject(jbase);
            baseName = temp.getJSONArray("data").getJSONObject(0).getString("baselinename");
            String reportJson = projdb.getProjectSummaryData(conn, projectId, baseid, loginid);
            j = projdb.getAllProjectSummaryData(reportJson);
            JSONArray store = j.getJSONArray("data");
            JSONArray rstore = j.getJSONArray("resources");
            if (StringUtil.equal(exportType, "pdf")) {
                baos = getPdfData(store, rstore, request);
            } else if (StringUtil.equal(exportType, "csv")) {
                JSONObject jtemp = new JSONObject(reportJson);
                rstore = jtemp.getJSONArray("resources");
                baos = getCsvData(rstore, store, request);
            }
            String params = AuthHandler.getAuthor(conn, loginid) + " (" + userName + "), " +
                    ReportName + ", " + exportType + " , " + ProjectName;
            AuditTrail.insertLog(conn, "1111", loginid, projectId, projectId,
                    companyid, params, ipAddress, 0);
        } catch (ServiceException ex) {
            filename = "Problem_in_exporting_file." + exportType;
            DbPool.quietRollback(conn);
        } catch (JSONException e) {
            filename = "Problem_in_exporting_file." + exportType;
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
            }
            writeDataToFile(filename, baos, response);
        }
    }

    private void writeDataToFile(String filename, ByteArrayOutputStream baos, HttpServletResponse response) {
        try {
            if (baos == null) {
//                baos.write("Problem in exporting file".getBytes());
            }
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            response.setContentType("application/octet-stream");
            response.setContentLength(baos.size());
            response.getOutputStream().write(baos.toByteArray());
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (Exception ex) {
            Logger.getLogger(ExportProjectReportServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void addComponyLogo(Document d,HttpServletRequest request ) throws ConfigurationException, DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setWidthPercentage(50);
        PdfPCell cell = null;
        try {
            imgPath= com.krawler.esp.utils.ConfigReader.getinstance().get("platformURL")+"b/"+companySubDomain+"/images/store/?company=true";
            Image img = Image.getInstance(imgPath);
            cell = new PdfPCell(img);
        } catch (Exception e) {
            cell = new PdfPCell(new Paragraph(companyName, fontBig));
        }
        cell.setBorder(0);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        d.add(table);
    }

    private static void addTitleSubtitle(Document d) throws DocumentException, JSONException {
        java.awt.Color tColor = new Color(0, 0, 0);
        fontBold.setColor(tColor);
        fontRegular.setColor(tColor);
        PdfPTable table = new PdfPTable(1);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
        java.util.Date today = new java.util.Date();

        table.setWidthPercentage(100);
        table.setSpacingBefore(6);

        //Report Title
        PdfPCell cell = new PdfPCell(new Paragraph("Project Summary", fontBold));
        cell.setBorder(0);
        cell.setBorderWidth(0);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);

        //Sub-title(s)
        cell = new PdfPCell(new Paragraph("(on comparison with "+baseName + ")", fontRegular));
        cell.setBorder(0);
        cell.setBorderWidth(0);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);

        //Separator line
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell cell1 = null;
        cell1 = new PdfPCell(new Paragraph(""));
        cell1.setBorder(PdfPCell.BOTTOM);
        line.addCell(cell1);
        d.add(table);
        d.add(line);
    }

    public static void getCompanyDetails(HttpServletRequest request) {
        String res1 = null;
        String res2 = null;
        String res3 = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        com.krawler.database.DbPool.Connection conn = null;
        String GET_COMPNY_IMGPATH = "SELECT companyname,subdomain,image FROM company WHERE companyid = ?";
        try {
            conn = DbPool.getConnection();
            pstmt = conn.prepareStatement(GET_COMPNY_IMGPATH);
            pstmt.setString(1, AuthHandler.getCompanyid(request));
            rs = pstmt.executeQuery();
            if (rs.next()) {

                res1 = rs.getString("image").trim();
                String tmp[] = res1.split("/");
                res1 = tmp[tmp.length - 1].toString().trim();
                res3 = rs.getString("subdomain");
                res2 = rs.getString("companyname");
            } else {
                res1 = "";
                res2 = "";
                res3 = "";
            }
//                conn.close();
        } catch (SessionExpiredException ex) {
            KrawlerLog.op.warn("Problem While Creating PDF :" + ex.toString());
            DbPool.quietRollback(conn);
            res1 = "";
            res2 = "";
            res3 = "";
        } catch (SQLException ex) {
            KrawlerLog.op.warn("Problem While Creating PDF :" + ex.toString());
            DbPool.quietRollback(conn);
            res1 = "";
            res2 = "";
            res3 = "";
        } catch (ServiceException ex) {
            KrawlerLog.op.warn("Problem While Creating PDF :" + ex.toString());
            DbPool.quietRollback(conn);
            res1 = "";
            res2 = "";
            res3 = "";
        } finally {
            imgPath = res1;
            companyName = res2;
            companySubDomain=res3;
            DbPool.quietClose(conn);
        }
    }

    public void getHeaderFooter(Document document) throws JSONException {
        java.awt.Color tColor = new Color(0, 0, 0);
        fontSmallRegular.setColor(tColor);

        java.util.Date dt = new java.util.Date();
        String dformat = "yyyy-MM-d";
        java.text.SimpleDateFormat dtf = new java.text.SimpleDateFormat(dformat);
        String DateStr = dtf.format(dt);

        // -------- header ----------------

        header = new PdfPTable(1);
        PdfPCell headerNotecell = new PdfPCell(new Phrase("Project Summary Report", fontSmallRegular));
        headerNotecell.setBorder(0);
        headerNotecell.setPaddingBottom(4);
        headerNotecell.setHorizontalAlignment(PdfCell.ALIGN_CENTER);
        header.addCell(headerNotecell);


        PdfPCell headerSeparator = new PdfPCell(new Phrase(""));
        headerSeparator.setBorder(PdfPCell.BOX);
        headerSeparator.setPadding(0);
        headerSeparator.setColspan(1);
        header.addCell(headerSeparator);
        // -------- header end ----------------

        // -------- footer  -------------------
        footer = new PdfPTable(3);
        PdfPCell footerSeparator = new PdfPCell(new Phrase(""));
        footerSeparator.setBorder(PdfPCell.BOX);
        footerSeparator.setPadding(0);
        footerSeparator.setColspan(3);
        footer.addCell(footerSeparator);

        PdfPCell emptyCell = new PdfPCell(new Phrase("", fontSmallRegular));
        emptyCell.setBorder(0);
        emptyCell.setColspan(1);
        emptyCell.setHorizontalAlignment(PdfCell.ALIGN_LEFT);
        footer.addCell(emptyCell);

        PdfPCell footerNotecell = new PdfPCell(new Phrase("Project Summary Report", fontSmallRegular));
        footerNotecell.setBorder(0);
        footerNotecell.setColspan(1);
        footerNotecell.setHorizontalAlignment(PdfCell.ALIGN_CENTER);
        footer.addCell(footerNotecell);

        PdfPCell pagerDateCell = new PdfPCell(new Phrase(DateStr, fontSmallRegular));
        pagerDateCell.setBorder(0);
        pagerDateCell.setColspan(1);
        pagerDateCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        footer.addCell(pagerDateCell);

    // -------- footer end   -----------
    }

    private String[] getColHeader() {
        String[] colHeader = {"Project Start Date", "Project End Date", "Baseline Start Date", "Baseline End Date",
            "Project Actual Start Date", "Project End Date", "Start Variance", "End Variance",
            "Scheduled Duration", "Remaining Duration", "Baseline Duration", "Actual Duration", "Duration Variance", "    ",
            "Scheduled Work", "Remaining Work", "Baseline Work", "Actual Work", "Work Variance", "Percent Complete",
            "Scheduled Cost", "Remaining Cost", "Baseline Cost", "Actual Cost", "Cost Variance", "    ",
            "Unstarted Tasks", "Inprogress Tasks", "Completed Tasks", "Parent Tasks", "Total Tasks"};
        return colHeader;
    }

    private String[] getColIndexes() {
        String[] colHeader = {"Project Start Date", "Project End Date", "Baseline Start Date", "Baseline End Date",
            "Project Actual Start Date", "Project End Date", "Start Variance", "End Variance",
            "Scheduled Duration", "Remaining Duration", "Baseline Duration", "Actual Duration", "Variance",
            "Scheduled Work", "Remaining Work", "Baseline Work", "Actual Work", "Variance", "Percent Complete",
            "Scheduled Cost", "Remaining Cost", "Baseline Cost", "Actual Cost", "Variance",
            "Unstarted Tasks", "Inprogress Tasks", "Completed Tasks", "Parent Tasks"};
        return colHeader;
    }

    private String[] getColValues(String[] colIndex, JSONArray j) throws JSONException {
        String[] val = new String[colIndex.length];
        try {
            for (int i = 0; i < val.length; i++) {
                if (colIndex[i].contains("    ")) {
                    val[i] = " ";
                } else {
                    val[i] = (String) j.getJSONObject(0).getString(colIndex[i]);
                }
            }
        } catch (JSONException e) {
        } catch (Exception e) {
        }
        return val;
    }

    private String[] getResourcesColHeader(JSONArray j, JSONArray data) throws JSONException {
        String[] res = new String[(j.length() * 2) + 2];
        int i = 0, k = 0;
        try {
            for (i = 0  , k=0; i < res.length || k < j.length(); i += 2, k++) {
                res[i] = (String) j.getJSONObject(k).getString("type");
                res[i + 1] = (String) j.getJSONObject(k).getString("count");
            }
        } catch (JSONException e) {
        } catch (Exception e) {
        } finally {
            res[i] = "Total Resources";
            res[i + 1] = (String) data.getJSONObject(0).getString("Total Resources");
        }
        return res;
    }

    private ByteArrayOutputStream getCsvData(JSONArray src, JSONArray store, HttpServletRequest request) throws ServiceException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StringBuilder resSB = new StringBuilder();
        JSONObject temp = null;
        int fieldListLength = 0;
        try{
                String header = "";
                temp = new JSONObject("{data:[Project Start,Baseline Start,Actual Start," +
                        "Start Variance,Project Finish,Baseline Finish,Actual Finish,Finish Variance," +
                        "Scheduled,Baseline,Variance,Remaining,Actual,Scheduled,Baseline,Variance," +
                        "Remaining,Actual,Scheduled,Baseline,Variance,Remaining,Actual,Percent Complete," +
                        "Task not yet Started,Task in progress,Task Completed,Parent Tasks, Total Tasks]}");
                JSONArray colHeader = temp.getJSONArray("data");
                temp = new JSONObject("{data:[Project Start Date,Baseline Start Date,Project Actual Start Date," +
                        "Start Variance,Project End Date,Baseline End Date,Project End Date,End Variance," +
                        "Scheduled Duration,Baseline Duration,Duration Variance,Remaining Duration," +
                        "Actual Duration,Scheduled Cost,Baseline Cost,Cost Variance,Remaining Cost,Actual Cost," +
                        "Scheduled Work,Baseline Work,Work Variance,Remaining Work,Actual Work,Percent Complete," +
                        "Unstarted Tasks,Inprogress Tasks,Completed Tasks,Parent Tasks,Total Tasks]}");
                JSONArray fieldList = temp.getJSONArray("data");
                fieldListLength = fieldList.length();
                for(int i = 0; i < src.length(); i++){
                    colHeader.put(src.getJSONObject(i).getString("type"));
                    fieldList.put("count");
                }
                fieldList.put("Total Resources");
                colHeader.put("Total Resources");
                for (int i = 0; i < colHeader.length(); i++){
                    header += "\"" + colHeader.get(i).toString() + "\",";
                }
                header = header.substring(0, (header.length() -1));
                header += "\n";
                resSB.append(header);
                String dataIndexArrStr[] = new String[fieldList.length()];
                for (int i = 0; i < fieldList.length(); i++)
                    dataIndexArrStr[i] = fieldList.get(i).toString();
                for(int i = 0; i < store.length(); i++){
                    temp = store.getJSONObject(i);
                    String dstr = "";
                    for(int j = 0; j < fieldListLength; j++)
                        dstr += "\"" + temp.getString(dataIndexArrStr[j]) + "\",";
                    dstr = dstr.substring(0, (dstr.length() - 1));
                    dstr += "\n";
                    resSB.append(dstr);
                }
                for(int i = 0; i < src.length(); i++){
                    temp = src.getJSONObject(i);
                    String dstr = "";
                    dstr += "\"" + temp.getString("count") + "\",";
                    dstr = dstr.substring(0, (dstr.length() - 1));
                    resSB.append(dstr);
                }
                String dstr = "";
                dstr += "\"" + store.getJSONObject(0).getString("Total Resources") + "\",";
                resSB.append(dstr);
                baos.write(resSB.toString().getBytes());
                baos.close();
        } catch (IOException ex) {
            throw ServiceException.FAILURE("ExportProjectReport.getCsvData", ex);
        } catch (JSONException e){
            throw ServiceException.FAILURE("ExportProjectReport.getCsvData", e);
        } catch (Exception e){
            throw ServiceException.FAILURE("ExportProjectReport.getCsvData", e);
        }
        return baos;
    }

    private ByteArrayOutputStream getPdfData(JSONArray data, JSONArray res, HttpServletRequest request) throws ServiceException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            String[] colHeader = getColHeader();
            String[] colIndex = getColIndexes();
            String[] val = getColValues(colHeader, data);
            String[] resources = getResourcesColHeader(res, data);
            String[] mainHeader = {"Dates", "Duration", "Work", "Cost", "Tasks", "Resources"};
            Document document = null;
            if (landscape) {
                Rectangle recPage = new Rectangle(PageSize.A4.rotate());
                recPage.setBackgroundColor(new java.awt.Color(255, 255, 255));
                document = new Document(recPage, 15, 15, 30, 30);
            } else {
                Rectangle recPage = new Rectangle(PageSize.A4);
                recPage.setBackgroundColor(new java.awt.Color(255, 255, 255));
                document = new Document(recPage, 15, 15, 30, 30);
            }
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new EndPage());
            document.open();
            if (showLogo) {
                getCompanyDetails(request);
                addComponyLogo(document,request);
            }
            addTitleSubtitle(document);
            addTable(data, resources, colIndex, colHeader, mainHeader, val, document);
            document.close();
            writer.close();
            baos.close();
        } catch (ConfigurationException ex) {
            throw ServiceException.FAILURE("ExportProjectReport.getPdfData", ex);
        } catch (DocumentException ex) {
            throw ServiceException.FAILURE("ExportProjectReport.getPdfData", ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("ExportProjectReport.getPdfData", e);
        } catch (IOException e) {
            throw ServiceException.FAILURE("ExportProjectReport.getPdfData", e);
        } catch (Exception e) {
            throw ServiceException.FAILURE("ExportProjectReport.getPdfData", e);
        }
        return baos;
    }

    private void addTable(JSONArray store, String[] res, String[] colIndex, String[] colHeader, String[] mainHeader, String[] val, Document document) throws JSONException, DocumentException {

        java.awt.Color tColor = new Color(0, 0, 0);
        fontSmallBold.setColor(tColor);
        Font f1 = FontFactory.getFont("Helvetica", 8, Font.NORMAL, tColor);

        float[] colw = new float[4];
        for (int x = 0; x < 4; x++) {
            colw[x] = 150;
        }
        int col = 0;

        for (int x = 0; x < mainHeader.length; x++) {
            //table start
            PdfPTable table = new PdfPTable(colw);
            table.setTotalWidth(88);
            table.setWidthPercentage(colw, document.getPageSize());
            //table.setSpacingBefore(10);

            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setTotalWidth(90);
            mainTable.setWidthPercentage(100);
            mainTable.setSpacingBefore(20);

            //header cell for mainTable
            PdfPCell headcell = null;
            headcell = new PdfPCell(new Paragraph(mainHeader[x], fontSmallBold));
            headcell.setBackgroundColor(new Color(0xEEEEEE));
            headcell.setPadding(padding);
            mainTable.addCell(headcell);
            document.add(mainTable);

            //header cell added to mainTable
            int row = 3;
            if (x == 0 || x == 4) {
                row = 4;
            } else if (x == 5) {
                row = 0;
            }
            for (; row > 0; row--) {
                for (int y = 1; y < colw.length + 1; y++) {// for each column add the colHeader and value cell
                    if (col != colHeader.length) {
                        if (y % 2 != 0) {
                            Paragraph p = new Paragraph(colHeader[col], f1);
                            if(colHeader[col].contains("Variance")){
                                p = new Paragraph(colHeader[col], fontSmallBold);
                            }
                            PdfPCell pcell = new PdfPCell(p);
                            if (gridBorder) {
                                pcell.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT | PdfPCell.TOP);
                            } else {
                                pcell.setBorder(0);
                            }
                            pcell.setPadding(padding);
                            pcell.setBorderColor(new Color(0xF2F2F2));
                            pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            pcell.setVerticalAlignment(Element.ALIGN_CENTER);
                            table.addCell(pcell);
                        } else {
                            Paragraph p;
                            p = new Paragraph(val[col], f1);
                            if(colHeader[col].contains("Start Variance") || colHeader[col].contains("End Variance")){
                                p = new Paragraph(val[col] + " days", fontSmallBold);
                            } else if(colHeader[col].contains("Duration")){
                                if(colHeader[col].contains("Duration Variance"))
                                    p = new Paragraph(val[col] + " days", fontSmallBold);
                                else
                                    p = new Paragraph(val[col] + " days", f1);
                            } else if(colHeader[col].contains("Work")){
                                if(colHeader[col].contains("Work Variance"))
                                    p = new Paragraph(val[col] + " hrs", fontSmallBold);
                                else
                                    p = new Paragraph(val[col] + " hrs", f1);
                            } else if(colHeader[col].contains("Cost")){
                                if(colHeader[col].contains("Cost Variance"))
                                    p = new Paragraph(currSymbol + " " + val[col], fontSmallBold);
                                else
                                    p = new Paragraph(currSymbol + " " + val[col], f1);
                            } else if(colHeader[col].contains("Percent Complete")){
                                p = new Paragraph(val[col] + " %", f1);
                            }
                            PdfPCell pcell = new PdfPCell(p);
                            if (gridBorder) {
                                pcell.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT | PdfPCell.TOP);
                            } else {
                                pcell.setBorder(0);
                            }
                            pcell.setPadding(padding);
                            pcell.setBorderColor(new Color(0xF2F2F2));
                            pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            pcell.setVerticalAlignment(Element.ALIGN_CENTER);
                            table.addCell(pcell);
                            col++;
                        }
                    }
                }
            }
            if (x == 5) {
                int y = 0;
                row = res.length / 4;
                for (; row > 0; row--) {
                    for (int c = 0; c < 2; c++) {
                        Paragraph p = new Paragraph(res[y], f1);
                        PdfPCell pcell = new PdfPCell(p);
                        if (gridBorder) {
                            pcell.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT | PdfPCell.TOP);
                        } else {
                            pcell.setBorder(0);
                        }
                        pcell.setPadding(padding);
                        pcell.setBorderColor(new Color(0xF2F2F2));
                        pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        pcell.setVerticalAlignment(Element.ALIGN_CENTER);
                        table.addCell(pcell);
                        p = new Paragraph(res[y + 1], f1);
                        pcell = new PdfPCell(p);
                        if (gridBorder) {
                            pcell.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT | PdfPCell.TOP);
                        } else {
                            pcell.setBorder(0);
                        }
                        pcell.setPadding(padding);
                        pcell.setBorderColor(new Color(0xF2F2F2));
                        pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        pcell.setVerticalAlignment(Element.ALIGN_CENTER);
                        table.addCell(pcell);
                        y += 2;
                    }
                }
            }
            document.add(table);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(ExportProjectSummaryServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(ExportProjectSummaryServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
