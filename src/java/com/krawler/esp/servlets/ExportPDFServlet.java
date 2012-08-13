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

import com.krawler.common.session.SessionExpiredException;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.krawler.esp.database.dbcon;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class ExportPDFServlet extends HttpServlet {
	private static final long serialVersionUID = 7177230356934462681L;

	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
	 * methods.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 */
	protected void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Document document = new Document(PageSize.A4, 15, 15, 15, 15);
		String stor = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			stor = getAgendaData(request);
			String[] colwidth1 = { "4", "16", "16", "16", "16", "16", "16" };
			float[] colwidth = new float[colwidth1.length];
			for (int i = 0; i < colwidth1.length; i++)
				colwidth[i] = 20;
			String[] colwidth2 = { "eid", "Event(s)", "Location", "Day",
					"Date", "Time", "Priority" };
			String[] colIndex1 = { "eid", "subject", "location", "eventday",
					"eventdate", "eventtime", "eventpri" };
			JSONArray store = null;

			JSONObject jbj = new JSONObject(stor);
			for (int i = 0; i < jbj.getJSONArray("data").length(); i++) {
				String startts = jbj.getJSONArray("data").getJSONObject(i)
						.getString("startts");
				String endts = jbj.getJSONArray("data").getJSONObject(i)
						.getString("endts");
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-d HH:mm:ss");
				SimpleDateFormat sdf2 = new SimpleDateFormat("h a");
				SimpleDateFormat sdf1 = new SimpleDateFormat("dd MMM");
				SimpleDateFormat sdf3 = new SimpleDateFormat("EEE");
				Date dt1 = sdf.parse(startts);
				Date dt2 = sdf.parse(endts);
				String datefield = sdf1.format(dt1) + " - " + sdf1.format(dt2);
				String timefield = sdf2.format(dt1) + " - " + sdf2.format(dt2);
				String priority = jbj.getJSONArray("data").getJSONObject(i)
						.getString("priority");
				if (priority.equals("h")) {
					priority = "High";
				} else if (priority.equals("m")) {
					priority = "Moderate";
				} else if (priority.equals("l")) {
					priority = "Low";
				}
				jbj.getJSONArray("data").getJSONObject(i).put("eventday",
						sdf3.format(dt1));
				jbj.getJSONArray("data").getJSONObject(i).put("eventdate",
						datefield);
				jbj.getJSONArray("data").getJSONObject(i).put("eventtime",
						timefield);
				jbj.getJSONArray("data").getJSONObject(i).put("eventpri",
						priority);
			}
			if (stor.compareTo("]") != 0) {
				store = jbj.getJSONArray("data");
				// tot = store.length();
			}
			PdfWriter.getInstance(document, baos);
			// PdfWriter.getInstance(document,fs);
			document.open();
			// Font font2 = FontFactory.getFont("Helvetica",
			// 12,Font.BOLD,Color.BLACK);
			int len = colwidth2.length - 3;
			int p = 0;
			if (len <= 5)
				p = colwidth2.length;
			else
				p = 8;

			// Paragraph prjnm = new Paragraph("Agenda Details");
			// prjnm.setIndentationLeft(135);
			// document.add(prjnm);
			// document.add(new Paragraph("time line"));

			int st = addTable(1, p, 0, store.length(), store, colwidth2,
					colIndex1, document);
		} catch (Exception de) {
			de.printStackTrace();
		}
		document.close();
		response.setHeader("Content-Disposition",
				"attachment; filename=Agenda Details");
		response.setContentType("application/pdf");
		response.setContentLength(baos.size());
		response.getOutputStream().write(baos.toByteArray());
		response.getOutputStream().flush();
	}

	private int addTable(int stcol, int stpcol, int strow, int stprow,
			JSONArray store, String[] colwidth2, String[] colIndex1,
			Document document) {
		float[] f = new float[(stpcol - stcol)];
		for (int k = 0; k < f.length; k++)
			f[k] = 20;
		PdfPTable table = new PdfPTable(f);
		PdfPCell cell = new PdfPCell(new Paragraph("Agenda Details"));
		cell.setColspan(6);
		table.addCell(cell);
		Font font = FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.BLACK);
		Font font1 = FontFactory.getFont("Helvetica", 8, Font.NORMAL,
				Color.BLACK);
		Font f1;
		int stpcol1 = 0;
		for (int hcol = stcol; hcol < stpcol; hcol++) {
			PdfPCell h1 = new PdfPCell(new Paragraph(colwidth2[hcol], font)); // new
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
				f1 = font1;
				for (int col = stcol; col < stpcol; col++) {
					table.addCell(new Paragraph(store.getJSONObject(row)
							.getString(colIndex1[col]), f1));
				}
			}
			document.newPage();
			document.add(table);

			if (stpcol != colIndex1.length) {
				if ((colIndex1.length - stpcol) > 5) // column limit
					stpcol1 = stpcol + 5;
				else
					stpcol1 = (colIndex1.length - stpcol) + stpcol;
				addTable(stpcol, stpcol1, strow, stprow, store, colwidth2,
						colIndex1, document);
			}
		} catch (Exception e) {
			e.toString();
		}
		return stpcol;
	}

	public static String getAgendaData(HttpServletRequest request)
			throws JSONException, SessionExpiredException {
		String[] cidList = request.getParameterValues("cidList");
		String viewdt1 = request.getParameter("viewdt1");
		String viewdt2 = request.getParameter("viewdt2");
		int limit = Integer.parseInt(request.getParameter("limit"));
		int start = Integer.parseInt(request.getParameter("start"));
        String loginid = AuthHandler.getUserid(request);
		Object[] returnStr = dbcon.fetchAgendaEvent(cidList, viewdt1, viewdt2,
				limit, start, loginid);
		com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(
				returnStr[1].toString());
		jobj.put("totalCount", returnStr[0].toString());
		return jobj.toString();
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
		processRequest(request, response);
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
		processRequest(request, response);
	}

	/**
	 * Returns a short description of the servlet.
	 */
	public String getServletInfo() {
		return "Short description";
	}
	// </editor-fold>
}
