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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.krawler.common.util.URLUtil"%>
<%@ page import="com.krawler.common.util.StringUtil"%>
<%@ page import="java.awt.image.BufferedImage"%>
<%@ page import="java.io.ByteArrayOutputStream"%>
<%
//    int imgquality = 100;
    int width = Integer.parseInt(request.getParameter("width"));
    int height = Integer.parseInt(request.getParameter("height"));
    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
    for(int y = 0; y < height; y++){
        int x = 0;
        String nm = "r" + Integer.toString(y);
        String[] row = request.getParameter(nm).split(",");
        for(int r = 0; r < row.length; r++){
            String[] pixel = row[r].split(":");
            pixel[0] = StringUtil.padString(pixel[0], 6, "0", 2);
            long crgb = StringUtil.hexadecimalToDecimal(pixel[0]);
            int repeat = 1;
            if(pixel.length > 1){
                repeat = StringUtil.isNullOrEmpty(pixel[1]) ? 1 : Integer.parseInt(pixel[1]);
            }
            for(int c = 0; c < repeat; c++) {
                img.setRGB(x, y, (int)crgb);
                x++;
            }
        }
    }
    response.setHeader("Content-Disposition", "attachment; filename=\"projectCompletionChart.jpeg\"");
    response.setContentType("image/png");
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    //com.sun.image.codec.jpeg.JPEGImageEncoder encoder = com.sun.image.codec.jpeg.JPEGCodec.createJPEGEncoder(os);
    //encoder.encode(img);
    response.setContentLength(os.size());
    response.getOutputStream().write(os.toByteArray());
    response.getOutputStream().flush();
%>
