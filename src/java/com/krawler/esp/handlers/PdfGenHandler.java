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

import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.awt.Color;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Phrase;

public class PdfGenHandler {

    static public String invoiceheadings[] = {
        "Name :",
        "Address",
        "Invoice No :",
        "Date :"
    };
    static public String receiptheading[] = {
        "Received with thanks from :",
        "Being payment of :"
    };
    static public String Inst1[] = {
        "1. Payment can be made via :",
        "a) Online payment via our website: www.inceif.net",
        "\u00a0",
        "b) Direct bank-in or telegraphic ",
        " ",
        "   Account Name: INCEIF ",
        "   Bank:Bank Islam Malaysia Berhad Ground Floor & Plaza Menara Tun Razak,",
        "   Jalan Raja Laut,50350 Kuala Lumpur.",
        "   SWIFT code:BIMBMYKLXXX",
        "   Account Number:14-014-01013083-8 (Malaysian Ringgit)",
        "\u00a0",
        "c) Cheque , Money Order or bank draft and should be payable to “International ",
        "Centre  for Education in Islamic Finance” or “ INCEIF ”"
    };
    
    public static ByteArrayOutputStream getInvoiceForm1(String invoice) {
        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 15, 15, 15, 15);
            PdfWriter writer = PdfWriter.getInstance(document, os);
            Font font7 = FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.BLACK);
            Font font10 = FontFactory.getFont("Helvetica", 14, Font.BOLD, Color.BLACK);
            HeaderFooter temp = new HeaderFooter(new Phrase(String.format("Note: This is a computer generated document and does not require signature"), font7), false);
            temp.setAlignment(Element.ALIGN_CENTER);
            document.setFooter(temp);
            document.open();
            JSONObject jbj = new JSONObject(invoice);
            String currency = "";
            com.krawler.utils.json.base.JSONArray gridinfo = jbj.getJSONArray("items");
            String ids[] = {"name", "address", "invoiceno","paymentdate"};
            String uinfo[] = new String[ids.length];
            for (int i = 0; i < uinfo.length; i++) {
                uinfo[i] = jbj.getString(ids[i]);
            }
            PdfPTable tableinfo = new PdfPTable(1);
            PdfPTable table = new PdfPTable(2);
            Font font1 = FontFactory.getFont("Helvetica", 24, Font.BOLD, Color.BLACK);
            String cnxt = "";//StorageHandler.getProfileStorePath() + "/inceif-200.png";
            PdfPCell cell1 = null;
            try {
                Image img = Image.getInstance(cnxt);
                cell1 = new PdfPCell(img);
            } catch (Exception e) {
                cnxt=StorageHandler.GetProfileImgStorePath()+"/140-logo.png";
                 Image img = Image.getInstance(cnxt);
                cell1 = new PdfPCell(img);
            }
            cell1.setBorder(0);
            cell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableinfo.addCell(cell1);
            tableinfo = addspace(1, tableinfo);
            tableinfo = addspace(4, tableinfo);
            cell1 = new PdfPCell(new Paragraph("INVOICE",font10));
            cell1.setBorder(0);
            cell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableinfo.addCell(cell1);
            tableinfo = addspace(2, tableinfo);
            table = addspace(1, table);
//            cell1 = new PdfPCell(new Paragraph(uinfo[uinfo.length - 1],font9));
//            cell1.setBorder(0);
//            cell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
//            tableinfo.addCell(cell1);
//            tableinfo = addspace(1, tableinfo);
//            tableinfo = addspace(2, tableinfo);
            cell1 = new PdfPCell(tableinfo);
            cell1.setBorder(0);
            table.addCell(cell1);
            PdfPTable tb = new PdfPTable(1);
            document.add(new Paragraph("\u00a0"));
            tableinfo = addspace(1, tableinfo);
            
            Font font = FontFactory.getFont("Helvetica", 10, Font.BOLD, Color.BLACK);
            Font font2 = FontFactory.getFont("Helvetica", 10, Font.NORMAL, Color.BLACK);
            //Font font3 = FontFactory.getFont("Helvetica", 8, Font.ITALIC, Color.BLACK);
            Font font11 = FontFactory.getFont("Helvetica", 12, Font.BOLD, Color.BLACK);
            PdfPCell c1 =null;
            c1 = new PdfPCell(new Paragraph("Bill To :",font2));
            c1.setBorder(0);
            tb.addCell(c1);
            for (int i = 0; i < 2; i++) {
                PdfPCell c =null;
                if(i==0) 
                    c = new PdfPCell(new Paragraph(uinfo[i],font11));
                else 
                    c = new PdfPCell(new Paragraph(uinfo[i]));
                //c.(20);
                c.setBorder(0);
                tb.addCell(c);
            }
            PdfPCell c = new PdfPCell(tb);
            c.setBorder(0);
            table.addCell(c);
            PdfPTable tb1 = new PdfPTable(1);
            for (int i = 2; i < 4; i++) {
                PdfPTable nested1 = new PdfPTable(3);
                c = new PdfPCell();
                c.setBorder(0);
                c.setPaddingRight(5);
                nested1.addCell(c);
                c = new PdfPCell(new Paragraph(invoiceheadings[i], font));
                c.setBorder(0);
                
                nested1.addCell(c);
                c = new PdfPCell(new Paragraph(uinfo[i],font2));
                c.setBorder(0);
                c.setHorizontalAlignment(Element.ALIGN_RIGHT);
                //c.setPaddingLeft(5);
                nested1.addCell(c);
                
                c = new PdfPCell(nested1);
                c.setBorder(0);
                tb1.addCell(c);
            }
            PdfPCell c2 = new PdfPCell(tb1);
            c2.setBorder(0);
            table.addCell(c2);
            PdfPTable tb2 = new PdfPTable(new float[]{60, 40});

          /*  for (int i = 6; i < 7; i++) {
                PdfPTable tb3 = new PdfPTable(2);//new float[]{20, 40});
                tb3.setWidths(new int[]{1, 3});
                c2 = new PdfPCell(new Paragraph(invoiceheadings[i], font));
                c2.setBorder(0);
                tb3.addCell(c2);
                c2 = new PdfPCell(new Paragraph(uinfo[i]));
                c2.setBorder(0);
                tb3.addCell(c2);
                c2 = new PdfPCell(tb3);
                c2.setBorder(0);
                c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                tb2.addCell(c2);
                tb2 = addspace(1, tb2);
            }
*/
            PdfPTable maintable = new PdfPTable(1);
            c2 = new PdfPCell(table);
            c2.setBorder(0);
            maintable.addCell(c2);
            maintable = addspace(0, maintable);
            c2 = new PdfPCell(tb2);
            c2.setBorder(0);
            maintable.addCell(c2);
            document.add(maintable);
            //for (int i = 0; i < 2; i++) {
                document.add(new Paragraph("\u00a0"));
            //}
            String[] colwidth2 = {"Subscription date", "Description","Rate ($/Project)", "Amount ($)"};

            PdfPTable table1 = new PdfPTable(4);
            table1.setWidths(new float[]{2,3, 1,1});
            for (int i = 0; i < colwidth2.length; i++) {
                PdfPCell cell = new PdfPCell(new Paragraph(colwidth2[i], font));
                cell.setBorder(0);
                if(i!=1){
                    cell.setBorderWidthLeft(1);
                    cell.setBorderWidthRight(1);
                }
                if(i==3) {
                    cell.setBorderWidthLeft(0);
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                } else {
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                }        
                cell.setBorderWidthTop(1);
                cell.setBackgroundColor(Color.getHSBColor(0f, 0f, 0.9f));
                cell.setBorderWidthBottom(1);
                cell.setPaddingBottom(5);
                cell.setPaddingTop(5);
                table1.addCell(cell);
            }
            //table1.setHeaderRows(1);
            double d = 0;
            String[] di = {"billdate", "description","rate","amount"};
            java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
            for (int i = 0; i < gridinfo.length(); i++) {
                currency = "USD";//gridinfo.getJSONObject(i).getString("currency");
                PdfPCell cell = null;
                for (int j = 0; j < di.length; j++) {
                    if (j == 3) {
                        cell = new PdfPCell(new Paragraph(df.format(Double.parseDouble(gridinfo.getJSONObject(i).getString(di[j]))), font2));
                        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    } else {
                            cell = new PdfPCell(new Paragraph(gridinfo.getJSONObject(i).getString(di[j]), font2));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    }
                    cell.setBorder(0);
                    if(j!=1){
                        cell.setBorderWidthLeft(1);
                        cell.setBorderWidthRight(1);
                    }
                    if(j==3){
                        cell.setBorder(0);
                        cell.setBorderWidthRight(1);
                    }
                    cell.setPaddingTop(10);
                    cell.setFixedHeight(100);
                    table1.addCell(cell);
                }
                d += Double.parseDouble(gridinfo.getJSONObject(i).getString(di[3]));

            }
            PdfPCell cell = null;
            cell = new PdfPCell(new Paragraph(" ", font));
            cell.setBorder(0);
            cell.setBorderWidthTop(1);
            cell.setBorderWidthBottom(1);
            cell.setBorderWidthLeft(1);
            cell.setBorderWidthRight(0);
            table1.addCell(cell);
            cell = new PdfPCell(new Paragraph("    " , font));
            cell.setBorder(0);
            cell.setBorderWidthTop(1);
            cell.setBorderWidthBottom(1);
            cell.setBorderWidthLeft(0);
            cell.setBorderWidthRight(0);
            table1.addCell(cell);
            cell = new PdfPCell(new Paragraph("     Total", font));
            cell.setBorder(0);
            cell.setBorderWidthTop(1);
            cell.setBorderWidthBottom(1);
            cell.setBorderWidthLeft(0);
            cell.setBorderWidthRight(0);
            cell.setPaddingBottom(5);
            table1.addCell(cell);
            cell = new PdfPCell(new Paragraph(" " + df.format(d), font));
            cell.setBorder(0);
            cell.setBorderWidthTop(1);
            cell.setBorderWidthBottom(1);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setBorderWidthLeft(0);
            cell.setBorderWidthRight(1);
            cell.setPaddingBottom(5);
            table1.addCell(cell);
            
            EnglishDecimalFormat f1 = new EnglishDecimalFormat();
            if (currency.equals("USD")) {
                cell = new PdfPCell(new Paragraph("Amount in words : "+CurrencyConvert(currency, d), font));
            } else {
                cell = new PdfPCell(new Paragraph(CurrencyConvert(currency, d), font));
            }
            cell.setColspan(4);
            cell.setBorder(0);
            //cell.setBorderWidthTop(1);
            cell.setBorderWidthBottom(1);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorderWidthLeft(1);
            cell.setBorderWidthRight(1);
            cell.setPaddingBottom(5);
            table1.addCell(cell);
            document.add(table1);
            document.add(new Paragraph("\u00a0"));
            PdfPTable inst = new PdfPTable(1);
//            for (int i = 0; i < Inst1.length; i++) {
//                c2 = new PdfPCell(new Paragraph(Inst1[i]));
//                c2.setBorder(0);
//                inst.addCell(c2);
//            }
            document.add(inst);
            document.close();
            writer.close();
            os.close();

        } 
//        catch (ConfigurationException ex) {
//            Logger.getLogger(PdfGenHandler.class.getName()).log(Level.SEVERE, null, ex);
//        }
        catch (JSONException ex) {
            Logger.getLogger(PdfGenHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PdfGenHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DocumentException ex) {
            Logger.getLogger(PdfGenHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception exp) {
            Logger.getLogger(PdfGenHandler.class.getName()).log(Level.SEVERE, null, exp);
        }
        return os;
    }

    public static PdfPTable addspace(int space, PdfPTable tb) {
        for (int i = 0; i < space; i++) {
            PdfPCell cell = new PdfPCell(new Paragraph(""));
            cell.setBorder(0);
            tb.addCell(cell);
        }
        return tb;

    }

    public static ByteArrayOutputStream getReceiptAcc1(String ackrecp) {
        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream();
            JSONObject jbj = new JSONObject(ackrecp);
            com.krawler.utils.json.base.JSONArray gridinfo = jbj.getJSONArray("items");
            String ginfo[] = new String[gridinfo.length() * 2];
            String ids[] = {"name", "receiptno", "paymentdate", "address"};
            String currency = "USD";
//            String address = jbj.getString("address");
            String uinfo[] = new String[ids.length];
            for (int i = 0; i < uinfo.length; i++) {
                uinfo[i] = jbj.getString(ids[i]);
            }
            Font font = FontFactory.getFont("Helvetica", 10, Font.BOLD, Color.BLACK);
            Font font1 = FontFactory.getFont("Helvetica", 14, Font.BOLD, Color.BLACK);
            Font font3 = FontFactory.getFont("Helvetica", 12, Font.BOLD, Color.BLACK); 
            //Font font4 = FontFactory.getFont("Helvetica", 12, Font.NORMAL, Color.BLACK);
            Font font7 = FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.BLACK);
            Font font10 = FontFactory.getFont("Helvetica", 12, Font.BOLD, Color.BLACK);
            Font font11 = FontFactory.getFont("Helvetica", 12, Font.NORMAL, Color.BLACK);
            Font font12 = FontFactory.getFont("Helvetica", 10, Font.NORMAL, Color.BLACK); 
            Document document1 = new Document(PageSize.A4, 15, 15, 15, 15);
            PdfWriter writer = PdfWriter.getInstance(document1, os);
            HeaderFooter temp = new HeaderFooter(new Phrase(String.format("Note: This is a computer generated document and does not require signature"), font7), false);
            temp.setAlignment(Element.ALIGN_CENTER);
            document1.setFooter(temp);
            document1.open();
            PdfPTable tableHinfo = new PdfPTable(1);
            PdfPTable table = new PdfPTable(1);
            font3.setStyle(Font.UNDERLINE);
            String cnxt = "";//StorageHandler.getProfileStorePath() + "/inceif-200.png";
            PdfPCell cell1 = null;
            try {
                Image img = Image.getInstance(cnxt);
//                img.scalePercent(90);
                cell1 = new PdfPCell(img);
            } catch (Exception e) {
                 cnxt=StorageHandler.GetProfileImgStorePath()+"/140-logo.png";
                 Image img = Image.getInstance(cnxt);
                cell1 = new PdfPCell(img);
            }
            cell1.setBorder(0);
            cell1.setPaddingTop(18);
            cell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableHinfo.addCell(cell1);
            tableHinfo = addspace(1, tableHinfo);
            tableHinfo = addspace(4, tableHinfo);
            cell1 = new PdfPCell(new Paragraph("RECEIPT", font1));
            cell1.setBorder(0);
            cell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableHinfo.addCell(cell1);
            tableHinfo = addspace(0, tableHinfo);
//            cell1 = new PdfPCell(new Paragraph(uinfo[4], font11));
//            cell1.setBorder(0);
//            cell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
//            tableHinfo.addCell(cell1);
            document1.add(tableHinfo);
            PdfPTable tableUinfo = new PdfPTable(1);
                
            cell1 = new PdfPCell(new Paragraph(receiptheading[0], font12));
            cell1.setBorder(0);
            cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableUinfo.addCell(cell1);
            cell1 = new PdfPCell(new Paragraph(uinfo[0], font10));
            cell1.setBorder(0);
            cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableUinfo.addCell(cell1);
            cell1 = new PdfPCell(new Paragraph(uinfo[3], font11));
            cell1.setBorder(0);
            cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableUinfo.addCell(cell1);
            
            document1.add(new Paragraph("\u00a0"));
            tableUinfo = addspace(1, tableUinfo);
            String hed[] = {"", "Receipt No : ", "Date : ", ""};

            PdfPTable tableDinfo = new PdfPTable(1);
            //tableDinfo.setTotalWidth(100);
            for (int j = 1; j < 3; j++) {
                PdfPTable nested1 = new PdfPTable(3);
                cell1= new PdfPCell();
                cell1.setBorder(0);
                nested1.addCell(cell1);
                
                cell1 = new PdfPCell(new Paragraph(hed[j], font));
                cell1.setBorder(0);
                nested1.addCell(cell1);
                
                cell1 = new PdfPCell(new Paragraph(uinfo[j], font12));
                cell1.setBorder(0);
                cell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                nested1.addCell(cell1);
                
                cell1 = new PdfPCell(nested1);
                cell1.setBorder(0);
                tableDinfo.addCell(cell1);
            }
            PdfPTable container = new PdfPTable(2);
            cell1=new PdfPCell(tableUinfo);
            cell1.setBorder(0);
            cell1.setPaddingBottom(15);
            container.addCell(cell1);
            cell1 = new PdfPCell(tableDinfo);
            cell1.setBorder(0);
            container.addCell(cell1);
            document1.add(container);
            PdfPTable table1 = new PdfPTable(2);
            String[] colwidth2 = {"Invoice No.", "Amount Paid($)"};
            for (int i = 0; i < colwidth2.length; i++) {
                PdfPCell cell = new PdfPCell(new Paragraph(colwidth2[i], font));
                if(i==1){
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cell.setBorderWidthLeft(0);
                }else{
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setBorderWidthLeft(1);
                }
                cell.setBackgroundColor(Color.getHSBColor(0f, 0f, 0.9f));
                cell.setPaddingBottom(5);
                cell.setBorderWidthRight(1);
                cell.setBorderWidthTop(1);
                table1.addCell(cell);
            }
            table1.setHeaderRows(1);
            double d = 0;
            colwidth2 = new String[]{"invoicenum", "amount"};
            java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
            for (int i = 0; i < gridinfo.length(); i++) {
                for (int j = 0; j < colwidth2.length; j++) {
                    PdfPCell cell = null;
                    if (j == 1) {
                        cell = new PdfPCell(new Paragraph(df.format(Double.parseDouble(gridinfo.getJSONObject(i).getString(colwidth2[j]))), font12));
                        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        cell.setBorderWidthLeft(0);
                    } else {
                        cell = new PdfPCell(new Paragraph(gridinfo.getJSONObject(i).getString(colwidth2[j]), font12));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setBorderWidthLeft(1);
                    }
                    //cell.setMinimumHeight(25);
                    cell.setBorderWidthRight(1);
                    cell.setPaddingTop(5);
                    cell.setFixedHeight(100);
                    table1.addCell(cell);
                    if (j == colwidth2.length - 1) {
                        d += Double.parseDouble(gridinfo.getJSONObject(i).getString(colwidth2[j]));
                    }
                }
            }
//            EnglishDecimalFormat f1 = new EnglishDecimalFormat();
            PdfPCell cell = new PdfPCell(new Paragraph("", font));
            cell.setBorderWidthBottom(1);
            cell.setBorderWidthRight(0);
            cell.setBorderWidthLeft(1);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            table1.addCell(cell);
            cell = new PdfPCell(new Paragraph("Total              " + df.format(d), font));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            //cell.setPaddingBottom(15);
            cell.setPaddingLeft(15);
            cell.setPaddingBottom(5);
            cell.setBorderWidthBottom(1);
            cell.setBorderWidthLeft(0);
            cell.setBorderWidthRight(1);
            table1.addCell(cell);
            PdfPTable table3 = new PdfPTable(1);
            cell = new PdfPCell(new Paragraph("Amount in words : "+CurrencyConvert(currency,d), font));
            //cell.setPaddingBottom(15);
            cell.setBorder(0);
            cell.setColspan(4);
            cell.setBorderWidthBottom(1);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorderWidthLeft(1);
            cell.setBorderWidthRight(1);
            cell.setPaddingBottom(5);
            table3.addCell(cell);
            document1.add(table1);
            document1.add(table3);
            document1.add(new Paragraph("\u00a0")); 
             
            cell = new PdfPCell(new Paragraph(String.format("All payments are non-refundable and non-transferable. \nThis Receipt is valid subject to clearance of the payments."), font11));
            //cell.setPaddingLeft(5);
            cell.setBorder(0);
            table.addCell(cell);
            
//            String delFlag = jbj.getString("delflag");
//            if(delFlag.compareTo("2")==0){
//                BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
//                PdfContentByte cb = writer.getDirectContentUnder();
//                cb.saveState();
//                cb.setColorFill(Color.BLACK);
//                cb.beginText();
//                cb.setFontAndSize(bf, 48);
//                cb.showTextAligned(Element.ALIGN_CENTER, "Canceled Payment", document1.getPageSize().getWidth() / 2, document1.getPageSize().getHeight() / 2, 45);
//                cb.endText();
//                cb.restoreState();
//            }   
            
            document1.add(table);
            document1.close();
            writer.close();
            os.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return os;
    }

    public static void addCell(String text, Font f, PdfPTable table) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, f));
        cell.setBorder(0);
        table.addCell(cell);
    }
    
//    public static void addPara(String paraName, Font f, Document document) {
//        try {
//            Paragraph temp = new Paragraph(paraName, f);
//            temp.setSpacingBefore(10);
//            temp.setSpacingAfter(5);
//            document.add(temp);
//        } catch (DocumentException dex) {
//            Logger.getLogger(PdfGenHandler.class.getName()).log(Level.SEVERE, null, dex);
//        }
//    }
//
//    public static void addTable(JSONObject store, String[] names, String[] columns, Document document) {
//        Font font = FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.BLACK);
//        Font font1 = FontFactory.getFont("Helvetica", 8, Font.NORMAL, Color.BLACK);
//        try {
//            PdfPTable table = new PdfPTable(2);
//            table.setWidths(new int[] {1, 3});
//            for (int col = 0; col < columns.length; col++) {
//                addCell(names[col].equals("") ? "" : names[col] + ":", font, table);
//                addCell(store.getString(columns[col]), font1, table);
//                table.completeRow();
//            }
//            document.add(table);
//        } catch (JSONException jex) {
//            Logger.getLogger(PdfGenHandler.class.getName()).log(Level.SEVERE, null, jex);
//        } catch (com.lowagie.text.DocumentException dex) {
//            Logger.getLogger(PdfGenHandler.class.getName()).log(Level.SEVERE, null, dex);
//        }
//    }
//    
//    public static PdfPTable PrintData(PdfPTable table, String[] offdata, Document document1, Font font4, int spaces) throws DocumentException {
//        PdfPCell cell1 = null;
//        for (int i = 0; i < offdata.length; i++) {
//            if (offdata[i].contains("<pbr>")) {
//                document1.add(table);
//                document1.newPage();
//                table = new PdfPTable(1);
//                String a = offdata[i].replaceAll("<pbr>", " ");
//                table = addspace(Integer.parseInt(a.trim()), table);
//            } else {
//                if(offdata[i].contains("<b>")){
//                    font4 = FontFactory.getFont("Helvetica", 11, Font.BOLD, Color.BLACK);
//                    offdata[i] = offdata[i].replaceAll("<b>", " ");
//                } else {
//                    font4 = FontFactory.getFont("Helvetica", 11, Font.NORMAL, Color.BLACK);
//                }
//                float lineSpacing = 4f; // use a line spacing of 1 rather than the default 1.5
//                Paragraph tpar = new Paragraph(1.5f, offdata[i], font4);
//                cell1 = new PdfPCell(tpar);
//                cell1.setBorder(0);
//                //cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
//                table.addCell(cell1);
//                table = addspace(spaces, table);
//            }
//        }
//
//
//
//        return table;
//
//    }

//    public static PdfPTable getgrid(com.krawler.utils.json.base.JSONArray ar, PdfPTable tab, boolean flg, String[] di) throws JSONException {
//        PdfPCell cell = null;
//        Font font4 = FontFactory.getFont("Helvetica", 10, Font.NORMAL, Color.BLACK);
//        for (int i = 0; i < ar.length(); i++) {
//            if (flg) {
//                cell = new PdfPCell(new Paragraph(i + ".", font4));
//                cell.setBorder(0);
//                tab.addCell(cell);
//            }
//            for (int j = 0; j < di.length; j++) {
//                cell = new PdfPCell(new Paragraph(ar.getJSONObject(i).getString(di[j]), font4));
//                cell.setBorder(0);
//                tab.addCell(cell);
//            }
//        }
//        return tab;
//
//    }

//    public static PdfPTable getgrid(com.krawler.utils.json.base.JSONArray ar, PdfPTable tab, boolean flg, String[] di, int fontsize) throws JSONException {
//        PdfPCell cell = null;
//        Font font = FontFactory.getFont("Helvetica", fontsize, Font.NORMAL, Color.BLACK);
//        for (int i = 0; i < ar.length(); i++) {
//            if (flg) {
//                int cntr = i + 1;
//                cell = new PdfPCell(new Paragraph(cntr + ".", font));
//                cell.setBorder(0);
//                tab.addCell(cell);
//            }
//            for (int j = 0; j < di.length; j++) {
//                cell = new PdfPCell(new Paragraph(ar.getJSONObject(i).getString(di[j]), font));
//                cell.setBorder(0);
//                tab.addCell(cell);
//            }
//        }
//        return tab;
//
//    }
//
//    public static PdfPTable gettabline(PdfPCell cell, PdfPTable gtab, int col, Color c) {
//        for (int i = 0; i < col; i++) {
//            cell = new PdfPCell(new Paragraph(""));
//            cell.setBorderWidthLeft(0);
//            cell.setBorderWidthRight(0);
//            cell.setBorderWidthTop(0);
//            cell.setBorderColorBottom(c);
//            cell.setMinimumHeight(0);
//            gtab.addCell(cell);
//
//        }
//        return gtab;
//    }
    
    public static String CurrencyConvert(String type,double value){
        EnglishDecimalFormat edf = new EnglishDecimalFormat();
        String curr = "";
        if(type.equals("USD")){
            curr = " Cents";
        }else{
            curr = " Sen";
        }
        String str = Double.toString(value).replace(".","/");
        String[] arr = str.split("/");
        if(arr.length == 0){
            str = type+": "+edf.convert(Integer.parseInt(arr[0]))+" Only";
        }else{
            if(arr[1].length()==1){
                int val = Integer.parseInt(arr[1]);
                if(val>0){
                    val = val * 10;
                    arr[1] = Integer.toString(val);
                    str = type+" "+edf.convert(Integer.parseInt(arr[0]))+ " and "+edf.convert(Integer.parseInt(arr[1]))+curr+" Only";
                }else{
                    str = type+" "+edf.convert(Integer.parseInt(arr[0]))+" Only";
                }
            }else{
                str = type+" "+edf.convert(Integer.parseInt(arr[0]))+ " and "+edf.convert(Integer.parseInt(arr[1]))+curr+" Only";
            }
        }
        return str;
    }
}

class EnglishDecimalFormat {

    private static final String[] majorNames = {
        "",
        " Thousand",
        " Million",
        " Billion",
        " Trillion",
        " Quadrillion",
        " Quintillion"
    };
    private static final String[] tensNames = {
        "",
        " Ten",
        " Twenty",
        " Thirty",
        " Fourty",
        " Fifty",
        " Sixty",
        " Seventy",
        " Eighty",
        " Ninety"
    };
    private static final String[] numNames = {
        "",
        " One",
        " Two",
        " Three",
        " Four",
        " Five",
        " Six",
        " Seven",
        " Eight",
        " Nine",
        " Ten",
        " Eleven",
        " Twelve",
        " Thirteen",
        " Fourteen",
        " Fifteen",
        " Sixteen",
        " Seventeen",
        " Eighteen",
        " Nineteen"
    };

    private String convertLessThanOneThousand(int number) {
        String soFar;

        if (number % 100 < 20) {
            soFar = numNames[number % 100];
            number /= 100;
        } else {
            soFar = numNames[number % 10];
            number /= 10;

            soFar = tensNames[number % 10] + soFar;
            number /= 10;
        }
        if (number == 0) {
            return soFar;
        }
        return numNames[number] + " Hundred" + soFar;
    }

    public String convert(int number) {
        /* special case */
        if (number == 0) {
            return "zero";
        }

        String prefix = "";

        if (number < 0) {
            number = -number;
            prefix = "negative";
        }

        String soFar = "";
        int place = 0;

        do {
            int n = number % 1000;
            if (n != 0) {
                String s = convertLessThanOneThousand(n);
                soFar = s + majorNames[place] + soFar;
            }
            place++;
            number /= 1000;
        } while (number > 0);

        return (prefix + soFar).trim();
    }
}
