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
import com.krawler.esp.handlers.AuditTrail;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.handlers.ProfileHandler;
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
import com.krawler.esp.handlers.projectReport;
import java.util.ArrayList;
import java.util.Iterator;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.handlers.projdb;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author krawler-user
 */
public class ExportProjectReportServlet extends HttpServlet {

    private static Font fontSmallRegular = FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.BLACK);
    private static Font SmallRegular = FontFactory.getFont("Helvetica", 8, Font.NORMAL, Color.BLACK);
    private static Font fontSmallBold = FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.BLACK);
    private static Font fontRegular = FontFactory.getFont("Helvetica", 12, Font.NORMAL, Color.BLACK);
    private static Font fontBold = FontFactory.getFont("Helvetica", 12, Font.BOLD, Color.BLACK);
    private static Font font12Regular = FontFactory.getFont("Helvetica", 12, Font.NORMAL, Color.BLACK);
    private static Font font12Bold = FontFactory.getFont("Helvetica", 12, Font.BOLD, Color.BLACK);
    private static Font fontBig = FontFactory.getFont("Helvetica", 24, Font.NORMAL, Color.BLACK);
    private static String imgPath = "";
    private static String companyName = "";
    private static String companySubDomain = "";
    private static String ProjectName = "";
    private static String ReportName = "";
    public static String currSymbol = "";
    private static com.krawler.utils.json.base.JSONObject config = null;
    private int showColumns = 6;
    private int stopCol = 0;
    private PdfPTable header = null;
    private PdfPTable footer = null;

    //inner class
    public class EndPage extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                Rectangle page = document.getPageSize();

                getHeaderFooter(document);
                // Add page header
                header.setTotalWidth(page.getWidth()-document.leftMargin()-document.rightMargin());
                header.writeSelectedRows(0, -1, document.leftMargin(), page.getHeight()-10 ,writer.getDirectContent());

                // Add page footer
                footer.setTotalWidth(page.getWidth()-document.leftMargin()-document.rightMargin());
                footer.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin()-5 ,writer.getDirectContent());

                // Add page border
                if (config.getBoolean("pageBorder")) {
                    int bmargin = 8;  //border margin
                    PdfContentByte cb = writer.getDirectContent();
                    cb.rectangle( bmargin, bmargin, page.getWidth() - bmargin*2, page.getHeight() - bmargin*2);
                    cb.setColorStroke(Color.LIGHT_GRAY);
                    cb.stroke();
                }

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

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, SQLException {
        JSONObject j = null;
        Connection conn = null;

            clearAll();
            ProjectName = request.getParameter("projname");
            ReportName = request.getParameter("reportname");
            String projectId = request.getParameter("projid") == null ? request.getParameter("projectid") : request.getParameter("projid");
            int auditMode = 0;
            String exportType = request.getParameter("exporttype");
            String filename = filename = ProjectName + "_" + ReportName + "." + exportType;
            ByteArrayOutputStream baos = null;
            try {
                int type = Integer.parseInt(request.getParameter("reporttypeformat"));
                String loginid = AuthHandler.getUserid(request);
                String userName = AuthHandler.getUserName(request);
                String companyid = AuthHandler.getCompanyid(request);
                String ipAddress = AuthHandler.getIPAddress(request);
            switch(type){
                case 1 :
                    j = new JSONObject(request.getParameter("colHeader"));
                    JSONArray colHeader = j.getJSONArray("data");

                    j = new JSONObject(request.getParameter("dataindex"));
                    JSONArray fieldList = j.getJSONArray("data");

                    String reportJson = projectReport.getProjectReportJson(request, true, true);
                    reportJson = giveSuffixces(reportJson, projectId);
                    j = new JSONObject(reportJson);
                    JSONArray store = j.getJSONArray("data");

                    if (StringUtil.equal(exportType, "pdf")) {

                        baos = getPdfData(colHeader, fieldList, store, request);
                    } else if (StringUtil.equal(exportType, "csv")) {
                        baos = getCsvData(colHeader, fieldList, store, request);
                    }
                    break;

                case 2:
                    conn = DbPool.getConnection();
                    String as = projectReport.getReportData(conn, request, loginid);

                    if (StringUtil.equal(exportType, "pdf")) {
                        baos = exportToPdfTimeline(request, as);
                    } else if (StringUtil.equal(exportType, "csv")) {
                        baos = exportToCsvTimeline(request, as);
                    }
                    break;
                    }
            String params = AuthHandler.getAuthor(conn, loginid) + " ("+ userName +"), " +
                   ReportName + ", " + exportType+ " , "+ ProjectName;
            AuditTrail.insertLog(conn, "1111", loginid, projectId, projectId,
                   companyid, params, ipAddress, auditMode);
        } catch (ServiceException ex) {
            filename = "Problem_in_exporting_file."+exportType;
            DbPool.quietRollback(conn);
        } catch (JSONException e) {
            filename = "Problem_in_exporting_file."+exportType;
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
            try{
                if(baos != null)
                    baos.close();
            } catch(IOException e){
            }
            writeDataToFile(filename, baos, response);
        }
    }

    private void writeDataToFile(String filename, ByteArrayOutputStream baos, HttpServletResponse response)
	{
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
        // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            try {
                processRequest(request, response);
            } catch (SQLException ex) {
                Logger.getLogger(ExportProjectReportServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(ExportProjectReportServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            try {
                processRequest(request, response);
            } catch (SQLException ex) {
                Logger.getLogger(ExportProjectReportServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(ExportProjectReportServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
    private static void addComponyLogo(Document d, HttpServletRequest request) throws ConfigurationException, DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setWidthPercentage(100);
        PdfPCell cell = null;
        try {
            imgPath = com.krawler.esp.utils.ConfigReader.getinstance().get("platformURL") + "b/" + companySubDomain + "/images/store/?company=true";
            // imgPath="http://192.168.0.141:8080/dp_dev/b/Demo/images/store/?company=true";
            Image img = Image.getInstance(imgPath);
            cell = new PdfPCell(img);
        } catch (Exception e) {
            cell = new PdfPCell(new Paragraph(companyName, fontBig));
        }
        cell.setBorder(0);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        PdfPCell cell2=new PdfPCell(new Paragraph(ProjectName.toUpperCase(),FontFactory.getFont("Helvetica", 15, Font.BOLD, Color.BLACK)));
        cell2.setBorderWidth(0);
        cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell2);
        d.add(table);
    }

    private static void addTitleSubtitle(Document d) throws DocumentException, JSONException {
        java.awt.Color tColor = new Color(Integer.parseInt(config.getString("textColor"), 16));
        fontBold.setColor(tColor);
        fontRegular.setColor(tColor);
        PdfPTable table = new PdfPTable(1);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);

        table.setWidthPercentage(100);
        table.setSpacingBefore(6);

        //Report Title
        PdfPCell cell = new PdfPCell(new Paragraph(config.getString("title"), fontBold));
        cell.setBorder(0);
        cell.setBorderWidth(0);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);

        //Report Subtitle(s)
        String[] SubTitles = config.getString("subtitles").split("~");// '~' as separator
        for(int i=0; i < SubTitles.length; i++){
            cell = new PdfPCell(new Paragraph(SubTitles[i], fontRegular));
            cell.setBorder(0);
            cell.setBorderWidth(0);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
        table.setSpacingAfter(6);
        d.add(table);

        //Separator line
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell cell1 = null;
        cell1 = new PdfPCell(new Paragraph(""));
        cell1.setBorder(PdfPCell.BOTTOM);
        line.addCell(cell1);
        d.add(line);
    }


    private int addTable(int stcol, int stpcol, int strow, int stprow, JSONArray store, String[] colwidth2, String[] colHeader, Document document) throws JSONException, DocumentException {

        java.awt.Color tColor = new Color(Integer.parseInt(config.getString("textColor"), 16));
        fontSmallBold.setColor(tColor);

        com.krawler.utils.json.base.JSONObject colWidth= config.getJSONObject("colWidth");
        JSONArray widths = colWidth.getJSONArray("data");
        ArrayList arr = new ArrayList();
        PdfPTable table;
        float[] f = new float[widths.length()+1];//[(stpcol - stcol) + 1];
        float[] tcol = new float[(stpcol - stcol) + 1];

        if(widths.length()==0){
            f[0]=10;
            for (int k = 1; k < f.length; k++) {
                f[k] = 20;
            }
            tcol[0]=10;
        }
        else{
            for (int i = 0; i < widths.length(); i++) {
                JSONObject temp = widths.getJSONObject(i);
                arr.add(temp.getInt("width"));
            }

            f[0]=30;
            for (int k = 1; k < f.length; k++) {
                if(!config.getBoolean("landscape") && (Integer)arr.get(k-1) > 550) {
                    f[k] = 550;
                }
                else {
                    f[k] = (Integer)arr.get(k-1);
                }
            }
//            table = new PdfPTable(f);
//            table.setTotalWidth(90);
           // table.setWidthPercentage(f,document.getPageSize());
            tcol[0]=30;
        }
        int i=1;
        for (int k = stcol; k < stpcol; k++) {
                tcol[i] = f[k+1];
                i++;
            }

        table = new PdfPTable(tcol);
        table.setTotalWidth(90);
         table.setWidthPercentage(tcol,document.getPageSize());

        table.setSpacingBefore(15);
        Font f1 = FontFactory.getFont("Helvetica", 8, Font.NORMAL, tColor);
        PdfPCell h2 = new PdfPCell(new Paragraph("No.", fontSmallBold));
        if (config.getBoolean("gridBorder")) {
                h2.setBorder(PdfPCell.BOX);
            } else {
                h2.setBorder(0);
            }
        h2.setPadding(4);
        h2.setBorderColor(Color.GRAY);
        h2.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(h2);
        int stpcol1 = 0;
        for (int hcol = stcol; hcol < stpcol; hcol++) {
            PdfPCell h1 = new PdfPCell(new Paragraph(colHeader[hcol], fontSmallBold));
            h1.setHorizontalAlignment(Element.ALIGN_CENTER);
            if (config.getBoolean("gridBorder")) {
                h1.setBorder(PdfPCell.BOX);
            } else {
                h1.setBorder(0);
            }
            h1.setBorderColor(Color.GRAY);
            h1.setPadding(4);
            table.addCell(h1);
        }
        table.setHeaderRows(1);

            for (int row = strow; row < stprow; row++) {
                h2 = new PdfPCell(new Paragraph(String.valueOf(row + 1), f1));
                if (config.getBoolean("gridBorder")) {
                    h2.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
                } else {
                    h2.setBorder(0);
                }
                h2.setPadding(4);
                h2.setBorderColor(Color.GRAY);
                h2.setHorizontalAlignment(Element.ALIGN_CENTER);
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
                    if (config.getBoolean("gridBorder")) {
                        h1.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
                    } else {
                        h1.setBorder(0);
                    }
                    h1.setPadding(4);
                    h1.setBorderColor(Color.GRAY);
                    h1.setHorizontalAlignment(Element.ALIGN_CENTER);
                    h1.setVerticalAlignment(Element.ALIGN_CENTER);
                    table.addCell(h1);
                }



            }
            document.add(table);
            document.newPage();

            /* add instance of OnStartPage*/
            if(widths.length()!=0){
                if (stpcol != colwidth2.length) {
                    float twidth=0;
                    stpcol1=stpcol;

                    int docwidth;
                    if (config.getBoolean("landscape"))
                        docwidth=800;
                    else
                        docwidth=600;


                    while(twidth<docwidth && stpcol1<f.length){
                        twidth+=f[stpcol1];
                        stpcol1++;
                    }
                    stpcol1--;

                    addTable(stpcol, stpcol1, strow, stprow, store, colwidth2, colHeader,
                            document);
                }

            }
            else{
                if (stpcol != colwidth2.length) {
                    if ((colwidth2.length - stpcol) > showColumns) {  // column limit
                        stpcol1 = stpcol + 5;
                    } else {
                        stpcol1 = (colwidth2.length - stpcol) + stpcol;
                    }
                    addTable(stpcol, stpcol1, strow, stprow, store, colwidth2, colHeader,
                            document);
                }
            }
        return stpcol;
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
            companySubDomain = res3;
            DbPool.quietClose(conn);
        }
    }

    public void getHeaderFooter(Document document) throws JSONException {
                java.awt.Color tColor = new Color(Integer.parseInt(config.getString("textColor"), 16));
                fontSmallRegular.setColor(tColor);

                java.util.Date dt = new java.util.Date();
                String dformat = "yyyy-MM-d";
                java.text.SimpleDateFormat dtf = new java.text.SimpleDateFormat(dformat);
                String DateStr = dtf.format(dt);

                // -------- header ----------------
                header = new PdfPTable(3);
                String HeadDate = "";
                if (config.getBoolean("headDate")) HeadDate = DateStr;
                PdfPCell headerDateCell = new PdfPCell(new Phrase( HeadDate, fontSmallRegular));
                headerDateCell.setBorder(0);
                headerDateCell.setPaddingBottom(4);
                header.addCell(headerDateCell);

                PdfPCell headerNotecell = new PdfPCell(new Phrase(config.getString("headNote"), fontSmallRegular));
                headerNotecell.setBorder(0);
                headerNotecell.setPaddingBottom(4);
                headerNotecell.setHorizontalAlignment(PdfCell.ALIGN_CENTER);
                header.addCell(headerNotecell);

                String HeadPager = "";
                if(config.getBoolean("headPager")) HeadPager = String.valueOf(document.getPageNumber());//current page no
                PdfPCell headerPageNocell = new PdfPCell(new Phrase(HeadPager, fontSmallRegular));
                headerPageNocell.setBorder(0);
                headerPageNocell.setPaddingBottom(4);
                headerPageNocell.setHorizontalAlignment(PdfCell.ALIGN_RIGHT);
                header.addCell(headerPageNocell);

                PdfPCell headerSeparator = new PdfPCell(new Phrase(""));
                headerSeparator.setBorder(PdfPCell.BOX);
                headerSeparator.setPadding(0);
                headerSeparator.setColspan(3);
                header.addCell(headerSeparator);
                // -------- header end ----------------

                // -------- footer  -------------------
                footer = new PdfPTable(3);
                PdfPCell footerSeparator = new PdfPCell(new Phrase(""));
                footerSeparator.setBorder(PdfPCell.BOX);
                footerSeparator.setPadding(0);
                footerSeparator.setColspan(3);
                footer.addCell(footerSeparator);

                String PageDate = "";
                if(config.getBoolean("footDate")) PageDate = DateStr;
                PdfPCell pagerDateCell = new PdfPCell(new Phrase( PageDate, fontSmallRegular));
                pagerDateCell.setBorder(0);
                footer.addCell(pagerDateCell);

                PdfPCell footerNotecell = new PdfPCell(new Phrase(config.getString("footNote"), fontSmallRegular));
                footerNotecell.setBorder(0);
                footerNotecell.setHorizontalAlignment(PdfCell.ALIGN_CENTER);
                footer.addCell(footerNotecell);

                String FootPager = "";
                if(config.getBoolean("footPager")) FootPager = String.valueOf(document.getPageNumber());//current page no
                PdfPCell footerPageNocell = new PdfPCell(new Phrase(FootPager, fontSmallRegular));
                footerPageNocell.setBorder(0);
                footerPageNocell.setHorizontalAlignment(PdfCell.ALIGN_RIGHT);
                footer.addCell(footerPageNocell);
                // -------- footer end   -----------
    }


    private ByteArrayOutputStream getPdfData(JSONArray colHeader, JSONArray fieldList, JSONArray store, HttpServletRequest request) throws ServiceException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try{
            config = new com.krawler.utils.json.base.JSONObject(request.getParameter("config"));
            String colHeaderArrStr[] = new String[colHeader.length()];
            for (int i = 0; i < colHeader.length(); i++)
                colHeaderArrStr[i] = colHeader.get(i).toString();
            String dataIndexArrStr[] = new String[fieldList.length()];
            for (int i = 0; i < fieldList.length(); i++)
                dataIndexArrStr[i] = fieldList.get(i).toString();



            Document document = null;
            if (config.getBoolean("landscape")){
                Rectangle recPage=new Rectangle(PageSize.A4.rotate());
                recPage.setBackgroundColor(new java.awt.Color(Integer.parseInt(config.getString("bgColor"), 16)));
                document = new Document(recPage, 15, 15, 30, 30);
            }
            else{
                Rectangle recPage=new Rectangle(PageSize.A4);
                recPage.setBackgroundColor(new java.awt.Color(Integer.parseInt(config.getString("bgColor"), 16)));
                document = new Document(recPage, 15, 15, 30, 30);
            }


            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new EndPage());
            document.open();
            if (config.getBoolean("showLogo")) {
                getCompanyDetails(request);
                addComponyLogo(document,request);
            }
//            int tC = Integer.parseInt(config.getString("textColor"), 16);
//            JSONArray myStore=(JSONArray)config.get("store");

            addTitleSubtitle(document);
            com.krawler.utils.json.base.JSONObject colWidth= config.getJSONObject("colWidth");
            JSONArray widths = colWidth.getJSONArray("data");
            ArrayList arr = new ArrayList();
            float[] f = new float[widths.length()];
            float totalwid=0;
            int counter=0;

            if(widths.length()!=0){
                for (int i = 0; i < widths.length(); i++) {
                    JSONObject temp = widths.getJSONObject(i);
                    arr.add(temp.getInt("width"));
                }

                f[0]=30;
                for (int k = 1; k < f.length; k++) {
                    if(!config.getBoolean("landscape") && (Integer)arr.get(k-1) > 550) {
                        f[k] = 550;
                    }
                    else {
                        f[k] = (Integer)arr.get(k-1);
                    }
                }

                int docwidth;
                if (config.getBoolean("landscape"))
                    docwidth=800;
                else
                    docwidth=600;

                while(totalwid<docwidth && counter<f.length){
                    totalwid += f[counter];
                    counter++;
                }
                if(totalwid>docwidth){
                    counter--;
                    counter--;
                }

                showColumns=counter;
            }


            addTable(0, showColumns, 0, store.length(), store, dataIndexArrStr, colHeaderArrStr, document);
            document.close();
            writer.close();
            baos.close();
        } catch (ConfigurationException ex) {
            throw ServiceException.FAILURE("ExportProjectReport.getPdfData", ex);
        } catch (DocumentException ex) {
            throw ServiceException.FAILURE("ExportProjectReport.getPdfData", ex);
        } catch (JSONException e){
            throw ServiceException.FAILURE("ExportProjectReport.getPdfData", e);
        } catch (IOException e){
            throw ServiceException.FAILURE("ExportProjectReport.getPdfData", e);
        }
        return baos;
    }
    private ByteArrayOutputStream getCsvData(JSONArray colHeader, JSONArray fieldList, JSONArray store, HttpServletRequest request) throws ServiceException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StringBuilder resSB = new StringBuilder();
        try{
            String header = "";
            for (int i = 0; i < colHeader.length(); i++)
                header += "\"" + colHeader.get(i).toString() + "\",";
            header = header.substring(0, (header.length() -1));
            header += "\n";
            resSB.append(header);
            String dataIndexArrStr[] = new String[fieldList.length()];
            for (int i = 0; i < fieldList.length(); i++)
                dataIndexArrStr[i] = fieldList.get(i).toString();
            for(int i = 0; i < store.length(); i++){
                JSONObject temp = store.getJSONObject(i);
                String dstr = "";
                for(int j = 0; j < dataIndexArrStr.length; j++)
                    dstr += "\"" + temp.getString(dataIndexArrStr[j]) + "\",";
                dstr = dstr.substring(0, (dstr.length() - 1));
                dstr += "\n";
                resSB.append(dstr);
            }
            baos.write(resSB.toString().getBytes());
            baos.close();
        } catch (IOException ex) {
            throw ServiceException.FAILURE("ExportProjectReport.getCsvData", ex);
        } catch (JSONException e){
            throw ServiceException.FAILURE("ExportProjectReport.getCsvData", e);
        }
        return baos;
    }

    private ByteArrayOutputStream exportToPdfTimeline(HttpServletRequest request, String as) throws ServiceException{
        JSONObject s = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try{
            config = new com.krawler.utils.json.base.JSONObject(request.getParameter("config"));
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

            Rectangle recPage=new Rectangle(PageSize.A4);
            recPage.setBackgroundColor(new java.awt.Color(Integer.parseInt(config.getString("bgColor"), 16)));

            Document document = null;
            if (config.getBoolean("landscape"))
                document = new Document(recPage.rotate(), 15, 15, 30, 30);
            else
                document = new Document(recPage, 15, 15, 30, 30);

            PdfWriter writer = PdfWriter.getInstance(document, os);
            writer.setPageEvent(new EndPage());
            document.open();
            if (config.getBoolean("showLogo")) {
                getCompanyDetails(request);
                addComponyLogo(document,request);
            }
            addTitleSubtitle(document);
            document.add(new Paragraph("\u00a0"));
            addTableTimeLine(3, p, 0, store.length(), store, colwidth2, maxlevel, document);
            document.close();
            writer.close();
            os.close();
        } catch (ConfigurationException ex) {
            throw ServiceException.FAILURE("ExportProjectReport.exportToPdfTimeline", ex);
        } catch (IOException ex) {
            throw ServiceException.FAILURE("ExportProjectReport.exportToPdfTimeline", ex);
        } catch (DocumentException ex) {
            throw ServiceException.FAILURE("ExportProjectReport.exportToPdfTimeline", ex);
        } catch(JSONException e){
            throw ServiceException.FAILURE("ExportProjectReport.exportToPdfTimeline", e);
        }
        return os;
    }
    private ByteArrayOutputStream exportToCsvTimeline(HttpServletRequest request, String as) throws ServiceException{
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
            throw ServiceException.FAILURE("ExportProjectReport.exportToCsvTimeline", ex);
        } catch(JSONException e){
            throw ServiceException.FAILURE("ExportProjectReport.exportToCsvTimeline", e);
        }
        return os;
    }


    private int addTableTimeLine(int stcol, int stpcol, int strow, int stprow,
            JSONArray store, String[] colwidth2, int maxlevel, Document document) throws JSONException, DocumentException {
        float[] f = new float[(stpcol - stcol) + 1];
        for (int k = 0; k < f.length; k++) {
            f[k] = 20;
        }
        f[0] = f[0] + 10 * maxlevel;
        PdfPTable table = new PdfPTable(f);

        java.awt.Color tColor = new Color(Integer.parseInt(config.getString("textColor"), 16));

        Font font = FontFactory.getFont("Helvetica", 8, Font.BOLD, tColor);
        Font font1 = FontFactory.getFont("Helvetica", 8, Font.NORMAL, tColor);
        Font f1;

        table.setWidthPercentage(90);
        PdfPCell h2 = new PdfPCell(new Paragraph("Name", font)); // new
        if (config.getBoolean("gridBorder"))
            h2.setBorder(PdfPCell.BOX);
        else
            h2.setBorder(0);
        h2.setPadding(2);
//                h2.setHorizontalAlignment(Element.ALIGN_UNDEFINED);
        // Paragraph(colwidth2[hcol],font);
        table.addCell(h2);
        int stpcol1 = 0;
        for (int hcol = stcol; hcol < stpcol; hcol++) {
            PdfPCell h1 = new PdfPCell(new Paragraph(colwidth2[hcol], font)); // new
            h1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            if (config.getBoolean("gridBorder"))
                h1.setBorder(PdfPCell.BOX);
            else
                h1.setBorder(0);
            h1.setPadding(2);
            // Paragraph(colwidth2[hcol],font);
            table.addCell(h1);
        }
        table.setHeaderRows(1);

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
//                    h2.setPaddingLeft((Integer.parseInt(store.getJSONObject(row).getString("level")) * 10) + 5);
                } else {
                    h2 = new PdfPCell(new Paragraph(store.getJSONObject(row).getString("info"), f1));
//                    h2.setPaddingTop(9);
//                    h2.setPaddingLeft((Integer.parseInt(store.getJSONObject(row).getString("level")) * 10) + 5);
                }

//                h2.setBorder(0);
//                h2.setPadding(1);
//                Color bColor = Color.decode("DDDDDD");

                if (config.getBoolean("gridBorder")){
                    if(store.getJSONObject(row).getBoolean("flag")) {
                        h2.setBackgroundColor(new Color(0xEEEEEE));
                        h2.setBorder(PdfPCell.LEFT | PdfPCell.BOTTOM );
                    } else
                        h2.setBorder(PdfPCell.BOX);
                } else {
                    h2.setBorder(0);
                }
                    h2.setPadding(2);
                    h2.setBorderColor(Color.GRAY);
                if (store.getJSONObject(row).getBoolean("flag"))
                    h2.setHorizontalAlignment(Element.ALIGN_LEFT);
                else
                    h2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                h2.setVerticalAlignment(Element.ALIGN_CENTER);
                table.addCell(h2);
                for (int col = stcol; col < stpcol; col++) {
                    Paragraph para = new Paragraph(store.getJSONObject(row).getString(colwidth2[col]), f1);
                    PdfPCell h1 = new PdfPCell(para);
//                    h1.setBorder(0);
//                    h1.setPadding(1);

                    h1.setMinimumHeight(15);
                    h1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    h1.setVerticalAlignment(Element.ALIGN_CENTER);
                    if (config.getBoolean("gridBorder")) {
                        if(store.getJSONObject(row).getBoolean("flag")) {
                            h1.setBorder(PdfPCell.BOTTOM);
                            h1.setBackgroundColor(new Color(0xEEEEEE));
                            if(col==stpcol-1) h1.setBorder(PdfPCell.BOTTOM | PdfPCell.RIGHT);
                        } else
                            h1.setBorder(PdfPCell.BOX);
                    } else {
                        h1.setBorder(0);
                    }
                    h1.setPadding(2);
                    h1.setBorderColor(Color.GRAY);
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
                addTableTimeLine(stpcol, stpcol1, strow, stprow, store, colwidth2,
                        maxlevel, document);
            }

        return stpcol;
    }

     public static String giveSuffixces(String result,String projectid) throws ServiceException {
         Connection conn = null;
         String ret = "";
         JSONObject json = null;
         try {
             conn = DbPool.getConnection();
             String cmpcurr = ProfileHandler.getCmpCurrFromProj(conn, projectid);
             try {
                 char a1 = (char) Integer.parseInt(cmpcurr, 16);
                 currSymbol = Character.toString(a1);
             } catch (Exception e) {
                 currSymbol = cmpcurr;
             }
             json = new JSONObject(result);
             if (result.compareTo("{data:{}}") != 0) {
                 for (int i = 0; i < json.getJSONArray("data").length(); i++) {
                     Iterator iterator = json.getJSONArray("data").getJSONObject(i).keys();
                     Map<String, Object> map = new HashMap<String, Object>();
                     while (iterator.hasNext()) {
                         String entry = (String) iterator.next();
                         String val = json.getJSONArray("data").getJSONObject(i).getString(entry);
                         if (entry.contains("dur") || entry.contains("delay") || entry.contains("startdiff") || entry.contains("enddiff") ) {
                             val = numberFormatterAppend(val, "duration");
                         } else if (entry.contains("cost") || entry.contains("expence")) {
                             val = numberFormatterPrepend(val, currSymbol);
                         }
                         else if(entry.contains("work") || entry.contains("hour")){
                             val = numberFormatterAppend(val, "work");
                         }
                         json.getJSONArray("data").getJSONObject(i).put(entry, val);
                         map.put(entry, val);
                     }
                     Iterator ite = map.entrySet().iterator();
                     Map.Entry entry = null;
                     while(ite.hasNext()){
                        entry = (Map.Entry) ite.next();
                        json.getJSONArray("data").getJSONObject(i).remove(entry.getKey().toString());
                        json.getJSONArray("data").getJSONObject(i).put(entry.getKey().toString(), entry.getValue());
                     }
                 }
                 ret = json.toString();
             }

         } catch (Exception e) {
             DbPool.quietRollback(conn);
             throw ServiceException.FAILURE("ExportProjectReportServlet.giveSuffixces", e);

         } finally {
             DbPool.quietClose(conn);
         }
         return ret;
     }
     public static String numberFormatterPrepend(String values, String compSymbol) {
        int notNA=values.compareTo("NA");
        if(notNA != 0){
        NumberFormat numberFormatter;
        java.util.Locale currentLocale = java.util.Locale.US;
        double d =Double.parseDouble(values);
        numberFormatter = NumberFormat.getNumberInstance(currentLocale);
        numberFormatter.setMinimumFractionDigits(0);
        numberFormatter.setMaximumFractionDigits(2);
        return (compSymbol  +" "+ numberFormatter.format(d));
        }else{
            return values;
        }
    }
    public static String numberFormatterAppend(String values, String dur) {
        String ret=values, toAppend = "";
        NumberFormat numberFormatter;
        java.util.Locale currentLocale = java.util.Locale.US;
        numberFormatter = NumberFormat.getNumberInstance(currentLocale);
        numberFormatter.setMinimumFractionDigits(0);
        numberFormatter.setMaximumFractionDigits(2);
        double d = 0;
        int notNA=values.compareTo("NA");
        if(notNA != 0){
        if(dur.equals("duration")){
            d = projdb.getDuration(values);
            toAppend = " day";
        }
        else if(dur.equals("work")){
            d = Double.parseDouble(values);
            toAppend = " hr";
        }

        if (d == 0.0 || d == 1)
            ret = String.valueOf(numberFormatter.format(d)).concat(toAppend);
        else
            ret = String.valueOf(numberFormatter.format(d)).concat(toAppend).concat("s");
        }
        return ret;
    }

}
