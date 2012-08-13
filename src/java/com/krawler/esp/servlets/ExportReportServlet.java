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

import java.awt.Color;
import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.database.DbPool;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.krawler.common.util.KrawlerLog;
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.handlers.projectReport;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import com.lowagie.text.Document;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.handlers.StorageHandler; //used when deployed
import com.krawler.utils.json.base.JSONException;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import java.util.Iterator;

public class ExportReportServlet extends HttpServlet {

    private static final long serialVersionUID = -7052763457578466081L;
    private static Font fontSmallRegular = FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.BLACK);
    private static Font fontSmallBold = FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.BLACK);
    private static Font fontRegular = FontFactory.getFont("Helvetica", 12, Font.NORMAL, Color.BLACK);
    private static Font fontBold = FontFactory.getFont("Helvetica", 12, Font.BOLD, Color.BLACK);
    private static Font font12Regular = FontFactory.getFont("Helvetica", 12, Font.NORMAL, Color.BLACK);
    private static Font font12Bold = FontFactory.getFont("Helvetica", 12, Font.BOLD, Color.BLACK);
    private static Font fontBig = FontFactory.getFont("Helvetica", 24, Font.NORMAL, Color.BLACK);
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    public static final int FILTER_LEFT = 1;
    public static final int FILTER_CENTER = 0;
    private static int pages = 0;
    private static String imgPath = "";
    private static String companyName = "";
    private static String ProjectName = "";
    private static String ReportName = "";
    private static String RepStartDate = "";
    private static String RepEndDate = "";

    //inner class
    public class EndPage extends PdfPageEventHelper {

        /**
         * Demonstrates the use of PageEvents.
         * @param args no arguments needed
         */
        /**
         * @see com.lowagie.text.pdf.PdfPageEventHelper#onEndPage(com.lowagie.text.pdf.PdfWriter, com.lowagie.text.Document)
         */
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                Rectangle page = document.getPageSize();
                java.util.Date dt = new java.util.Date();
                PdfPTable foot = new PdfPTable(1);
                PdfPCell cell = new PdfPCell(new Phrase(sdf.format(dt), fontSmallRegular));
                cell.setBorder(0);
                foot.addCell(cell);
                foot.setTotalWidth(page.getWidth() / 4);
                foot.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin() + 10,
                        writer.getDirectContent());
            } catch (Exception e) {
                Logger.getLogger(ExportProjectReportServlet.class.getName()).log(Level.SEVERE, null, e);
                throw new ExceptionConverter(e);
            }
        }
    }

    public static void clearAll() {
        imgPath = "";
        companyName = "";
        pages = 0;
        ProjectName = "";
        ReportName = "";
        RepStartDate = "";
        RepEndDate = "";
    }
    //inner classends
    protected void processRequest(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException, ServiceException {
        clearAll();
        com.krawler.database.DbPool.Connection conn = null;
        try {
            conn = DbPool.getConnection();
            String userid = AuthHandler.getUserid(request);
            ProjectName = request.getParameter("projname");
            ReportName = request.getParameter("reportname");
            String as = projectReport.getReportData(conn, request, userid);
            String fileType = request.getParameter("exporttype");
            ByteArrayOutputStream os = null;
            if(StringUtil.equal(fileType, "pdf"))
                os = exportToPdf(conn, request, as);
            else if(StringUtil.equal(fileType, "csv")){
                os = exportToCsv(conn, request, as);
            }
            if(os != null){
                String filename = ProjectName + "_" + ReportName + "." + fileType;
                response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
                final ServletOutputStream out = response.getOutputStream();
                response.setContentType("application/octet-stream");
                response.setContentLength(os.size());
                response.getOutputStream().write(os.toByteArray());
                response.getOutputStream().flush();
            }
            os.close();
        } catch (ServiceException xex) {
            KrawlerLog.op.warn("Service Exception ExportProjectReport.java" + xex.toString());
            DbPool.quietRollback(conn);
        } catch (SessionExpiredException ex) {
                DbPool.quietRollback(conn);
                Logger.getLogger(ExportMPXServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (com.krawler.utils.json.base.JSONException j) {
            KrawlerLog.op.warn("Problem During Creating JSON Object :" + j.toString());
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }

    }
    private ByteArrayOutputStream exportToPdf(Connection conn, HttpServletRequest request, String as){
        JSONObject s = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try{
            s = new JSONObject(as);
            JSONArray head = s.getJSONArray("columnheader");
            JSONArray store = s.getJSONArray("data");
            String[] colwidth2 = new String[head.length() + 3];
            String[] colnm = {"info", "level", "flag"};
            for (int i = 0; i < colwidth2.length; i++) {
                if (i < 3)
                    colwidth2[i] = colnm[i];
                else {
                    if (head.getJSONObject(i - 3).has("0"))
                        colwidth2[i] = head.getJSONObject(i - 3).getString("0");
                    else
                        colwidth2[i] = head.getJSONObject(i - 3).getString("1");
                }
            }
            int maxlevel = 0;
            for (int k = 0; k < store.length(); k++) {
                for (int j = 0; j < colwidth2.length; j++) {
                    if (!store.getJSONObject(k).has(colwidth2[j]))
                        store.getJSONObject(k).put(colwidth2[j], "");
                }
                if (store.getJSONObject(k).getInt("level") > maxlevel)
                    maxlevel = store.getJSONObject(k).getInt("level");
            }
            int len = colwidth2.length - 3;
            int p = 0;
            if (len <= 5)
                p = colwidth2.length;
            else
                p = 8;
            Document document = new Document(PageSize.A4, 15, 15, 15, 15);
            PdfWriter writer = PdfWriter.getInstance(document, os);
            writer.setPageEvent(new EndPage());
            getCompanyDetails(conn, request);
            getPageCount(3, p, 0, store.length(), store, colwidth2,
                    maxlevel, document);
            prepare(document, FILTER_CENTER);
            document.add(new Paragraph("\u00a0"));
            addTable(3, p, 0, store.length(), store, colwidth2, maxlevel, document);
            document.close();
            writer.close();
            os.close();
        } catch (IOException ex) {
            Logger.getLogger(ExportReportServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DocumentException ex) {
            Logger.getLogger(ExportReportServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch(JSONException e){
            
        }
        return os;
    }
    private ByteArrayOutputStream exportToCsv(Connection conn, HttpServletRequest request, String as){
        JSONObject s = null;
        ByteArrayOutputStream os = null;
        try{
            s = new JSONObject(as);
            JSONArray head = s.getJSONArray("columnheader");
            JSONArray store = s.getJSONArray("data");
            StringBuilder csvBuild = new StringBuilder();
            String headerCsv = "\"Resource Name\",\"Task Name\",";
            if(StringUtil.equal(request.getParameter("reportname"), "Project Cost"))
                headerCsv = "\"Task Name\",\"Resource Name\",";
            String headerCsvArr = "";
            for(int c = 0; c < head.length(); c++){
                JSONObject temp = head.getJSONObject(c);
                String header = temp.getString(temp.names().getString(0));
                headerCsv += "\"" + header + "\",";
                headerCsvArr += header + ",";
            }
            headerCsvArr = headerCsvArr.substring(0, (headerCsvArr.length() - 1));
            headerCsv = headerCsv.substring(0, (headerCsv.length() - 1));
            String[] headers = headerCsvArr.split(",");
            headerCsv += "\n";
            csvBuild.append(headerCsv);
            for(int c = 0; c < store.length(); c++){
                String data = "";
                JSONObject temp = store.getJSONObject(c);
//                int lvl = Integer.parseInt(temp.getString("level"));
                if(temp.getBoolean("flag"))
                    data += "\"" + temp.getString("info") + "\",\" \",";
                else
                    data += "\" \",\"" + temp.getString("info") + "\",";
//                temp.remove("info");
//                temp.remove("level");
//                temp.remove("flag");
                for(int i = 0; i < headers.length; i++){
                    Iterator<String> keys = temp.keys();
                    boolean flg = false;
                    while(keys.hasNext()){
                        String k = keys.next();
                        if(StringUtil.equal(k, headers[i])){
                            flg = true;
                            break;
                        }
                    }
                    if(flg)
                        data += "\"" + temp.getString(headers[i]) + "\",";
                    else
                        data += "\" \",";
                }
                data = data.substring(0, (data.length() - 1));
                data += "\n";
                csvBuild.append(data);
            }
            os = new ByteArrayOutputStream();
            os.write(csvBuild.toString().getBytes());
            os.close();
        } catch (IOException ex) {
            Logger.getLogger(ExportReportServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch(JSONException e){
            
        }
        return os;
    }
    private static void prepare(Document d, HeaderFooter hfFooter) {
        PdfPTable table = new PdfPTable(1);
        try {
            d.setFooter(hfFooter);
            d.open();
            imgPath = StorageHandler.GetProfileImgStorePath() + StorageHandler.GetFileSeparator() + imgPath;
//                  imgPath = "/home/krawler-user/logo.jpg";                
            table.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.setWidthPercentage(50);
            PdfPCell cell = null;
            try {
                Image img = Image.getInstance(imgPath);
                cell = new PdfPCell(img);
            } catch (Exception e) {
                cell = new PdfPCell(new Paragraph(companyName, fontBig));
            }
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(cell);

            d.add(table);
        } catch (Exception e) {
            Logger.getLogger(ExportProjectReportServlet.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public static HeaderFooter addFooter() {
        HeaderFooter hf = new HeaderFooter(new Phrase("Page ", fontSmallRegular), new Phrase(" of " + pages, fontSmallRegular));
        hf.setBorderWidth(0);
        hf.setBorderWidthTop(1);
        hf.setAlignment(Element.ALIGN_RIGHT);
        return hf;
    }

    private static void prepare(Document d, int flag) {
        HeaderFooter hfFooter = null;
        try {
            switch (flag) {
                case FILTER_CENTER:
                    hfFooter = addFooter();
                    prepare(d, hfFooter);
                    addCenter(d);
                    break;
                case FILTER_LEFT:
                    break;
                default:

                    break;
                }
        } catch (Exception e) {
            Logger.getLogger(ExportProjectReportServlet.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private static void addCenter(Document d) throws DocumentException {

        PdfPTable table = new PdfPTable(1);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);

        table.setWidthPercentage(50);
        table.setSpacingBefore(10);
        PdfPCell cell = null;
        cell = new PdfPCell(new Paragraph(ProjectName, fontBold));
        cell.setBorder(0);
        cell.setBorderWidth(0);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);

        cell = new PdfPCell(new Paragraph(ReportName, fontRegular));
        cell.setBorder(0);
        cell.setBorderWidth(0);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        // For tasks which prompt user for a start and end date
        if (!(StringUtil.isNullOrEmpty(RepStartDate) && StringUtil.isNullOrEmpty(RepEndDate))) {
            cell = new PdfPCell(new Paragraph("From:" + RepStartDate + " To:" + RepEndDate, fontSmallRegular));
            cell.setBorder(0);
            cell.setBorderWidth(0);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
        table.setSpacingAfter(10);
        d.add(table);
    }


    private int addTable(int stcol, int stpcol, int strow, int stprow,
            JSONArray store, String[] colwidth2, int maxlevel, Document document) {
        float[] f = new float[(stpcol - stcol) + 1];
        for (int k = 0; k < f.length; k++) {
            f[k] = 20;
        }
        f[0] = f[0] + 10 * maxlevel;
        PdfPTable table = new PdfPTable(f);
        Font font = FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.BLACK);
        Font font1 = FontFactory.getFont("Helvetica", 8, Font.NORMAL,
                Color.BLACK);
        Font f1;
        PdfPCell h2 = new PdfPCell(new Paragraph("", font)); // new
        h2.setBorder(0);
        h2.setPadding(1);
//                h2.setHorizontalAlignment(Element.ALIGN_UNDEFINED);													
        // Paragraph(colwidth2[hcol],font);
        table.addCell(h2);
        int stpcol1 = 0;
        for (int hcol = stcol; hcol < stpcol; hcol++) {
            PdfPCell h1 = new PdfPCell(new Paragraph(colwidth2[hcol], font)); // new
            h1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            h1.setBorder(0);
            h1.setPadding(1);
            // Paragraph(colwidth2[hcol],font);
            table.addCell(h1);
        }
        table.setHeaderRows(1);
        try {
            for (int row = strow; row < stprow; row++) {
                if (row % 62 == 61) {
                    document.add(table);
                    table.deleteBodyRows();
                    table.setSkipFirstHeader(true);
                }
                if (store.getJSONObject(row).getBoolean("flag"))
                    f1 = font;
                else
                    f1 = font1;
               if (store.getJSONObject(row).getString("info").compareTo("") != 0) {
                    h2 = new PdfPCell(new Paragraph(store.getJSONObject(row).getString("info"), f1));
                    h2.setPaddingLeft((Integer.parseInt(store.getJSONObject(row).getString("level")) * 10) + 5);
                } else {
                    h2 = new PdfPCell(new Paragraph(store.getJSONObject(row).getString("info"), f1));
                    h2.setPaddingTop(9);
                    h2.setPaddingLeft((Integer.parseInt(store.getJSONObject(row).getString("level")) * 10) + 5);
                }
                h2.setBorder(0);
                h2.setPadding(1);
                if (store.getJSONObject(row).getBoolean("flag"))
                    h2.setHorizontalAlignment(Element.ALIGN_LEFT);
                else
                    h2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                h2.setVerticalAlignment(Element.ALIGN_CENTER);
                table.addCell(h2);
                for (int col = stcol; col < stpcol; col++) {
                    Paragraph para = null;
                    if(store.getJSONObject(row).has(colwidth2[col]))
                        para = new Paragraph(store.getJSONObject(row).getString(colwidth2[col]), f1);
                    else
                        para = new Paragraph("", f1);
//                    Paragraph para = new Paragraph(store.getJSONObject(row).getString(colwidth2[col]), f1);
                    PdfPCell h1 = new PdfPCell(para);
                    h1.setBorder(0);
                    h1.setPadding(1);
                    h1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    h1.setVerticalAlignment(Element.ALIGN_CENTER);
                    table.addCell(h1);
//					table.addCell(new Paragraph(store.getJSONObject(row)
//							.getString(colwidth2[col]), f1));
                }

            }
            document.add(table);
            document.newPage();
            if (stpcol != colwidth2.length) {
                if ((colwidth2.length - stpcol) > 5) // column limit
                    stpcol1 = stpcol + 5;
                else
                    stpcol1 = (colwidth2.length - stpcol) + stpcol;
                addTable(stpcol, stpcol1, strow, stprow, store, colwidth2,
                        maxlevel, document);
            }

        } catch (com.krawler.utils.json.base.JSONException j) {
            KrawlerLog.op.warn("Problem During Creating JSON Object :" + j.toString());
        } catch (com.lowagie.text.DocumentException de) {
            KrawlerLog.op.warn("Problem While Creating PDF :" + de.toString());
        }
        return stpcol;
    }

    private void getPageCount(int stcol, int stpcol, int strow, int stprow,
            JSONArray store, String[] colwidth2, int maxlevel, Document document) {
        float[] f = new float[(stpcol - stcol) + 1];
        for (int k = 0; k < f.length; k++)
            f[k] = 20;
        f[0] = f[0] + 10 * maxlevel;
        int stpcol1 = 0;
        for (int hcol = stcol; hcol < stpcol; hcol++) {
        }
        for (int row = strow; row < stprow; row++) {
        }
        pages++;
        if (stpcol != colwidth2.length) {
            if ((colwidth2.length - stpcol) > 5) // column limit
                stpcol1 = stpcol + 5;
            else
                stpcol1 = (colwidth2.length - stpcol) + stpcol;
            getPageCount(stpcol, stpcol1, strow, stprow, store, colwidth2,
                    maxlevel, document);
        }
    }

    public static void getCompanyDetails(Connection conn, HttpServletRequest request) {


        String res1 = null;
        String res2 = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
//            com.krawler.database.DbPool.Connection conn = null;
        String GET_COMPNY_IMGPATH = "SELECT companyname,image FROM company WHERE companyid = ?";
        try {
//                    conn = DbPool.getConnection();                        
            pstmt = conn.prepareStatement(GET_COMPNY_IMGPATH);
            pstmt.setString(1, AuthHandler.getCompanyid(request));
            rs = pstmt.executeQuery();
            if (rs.next()) {
                res1 = rs.getString("image").trim();
                String tmp[] = res1.split("/");
                res1 = tmp[tmp.length - 1].toString().trim();
                res2 = rs.getString("companyname");
            } else {
                res1 = "";
                res2 = "";
            }
//                conn.close();
        } catch (SessionExpiredException ex) {
            Logger.getLogger(ExportProjectReportServlet.class.getName()).log(Level.SEVERE, null, ex);
            res1 = "";
            res2 = "";
        } catch (SQLException ex) {
            Logger.getLogger(ExportProjectReportServlet.class.getName()).log(Level.SEVERE, null, ex);
            res1 = "";
            res2 = "";
        } catch (ServiceException ex) {
            Logger.getLogger(ExportProjectReportServlet.class.getName()).log(Level.SEVERE, null, ex);
            res1 = "";
            res2 = "";
        } finally {
            imgPath = res1;
            companyName = res2;
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
        } catch (ServiceException ex) {

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
        } catch (ServiceException ex) {

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
