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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import java.awt.*;
import java.awt.image.*;
import sun.awt.image.BufferedImageGraphicsConfig;

public class genericFileUpload {

    protected Map parameters = new HashMap();
    protected boolean isUploaded;
    protected String Ext;
    public String ErrorMsg = "";
    private final String sep = StorageHandler.GetFileSeparator();

    public void doPost(List fileItems, String filename,
            String destinationDirectory) {
        File destDir = new File(destinationDirectory);
        Ext = "";
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        DiskFileUpload fu = new DiskFileUpload();
        fu.setSizeMax(-1);
        fu.setSizeThreshold(4096);
        fu.setRepositoryPath(destinationDirectory);
        for (Iterator i = fileItems.iterator(); i.hasNext();) {
            FileItem fi = (FileItem) i.next();
            if (!fi.isFormField()) {
                String fileName = null;
                try {
                    fileName = new String(fi.getName().getBytes(), "UTF8");
                    if (fileName.contains(".")) {
                        Ext = ".png";
                    }
                    if (fi.getSize() != 0) {
                        this.isUploaded = true;
                        File uploadFile = new File(destinationDirectory + sep
                                + filename + Ext);
                        fi.write(uploadFile);
                        String contentType = fi.getContentType();
                        if (contentType.contains("image"))
                            contentType = contentType.substring(contentType.lastIndexOf("/") + 1);
                        imgResizeInRatio(destinationDirectory + sep + filename + Ext,
                                100, 100, destinationDirectory + sep + filename
                                + "_100", contentType);
                        imgResizeInRatio(destinationDirectory + sep + filename + Ext,
                                200, 200, destinationDirectory + sep + filename
                                + "_200", contentType);
                        imgResizeInRatio(destinationDirectory + sep + filename + Ext,
                                35, 35, destinationDirectory + sep + filename + "_35", contentType);

                    } else {
                        this.isUploaded = false;
                    }
                } catch (Exception e) {
                    Logger.getInstance(genericFileUpload.class).error(e, e);
                }
            }
        }
    }
    public void doPostCompay(List fileItems, String filename,
            String destinationDirectory) {
        File destDir = new File(destinationDirectory);
        Ext = "";
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        DiskFileUpload fu = new DiskFileUpload();
        fu.setSizeMax(-1);
        fu.setSizeThreshold(4096);
        fu.setRepositoryPath(destinationDirectory);
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
                        this.isUploaded = true;
                        File uploadFile = new File(destinationDirectory + sep
                                + "temp_" + filename + Ext);
                        fi.write(uploadFile);
                        imgResizeCompany(destinationDirectory + sep + "temp_" + filename + Ext,
                                0, 0, destinationDirectory + sep + "original_" + filename, true);
                        imgResizeCompany(destinationDirectory + sep + "temp_" + filename + Ext,
                                130, 25, destinationDirectory + sep + filename, false);
//						imgResize(destinationDirectory + sep + filename + Ext,
//								0, 0, destinationDirectory + "/original_" + filename);
                        uploadFile.delete();
                    } else {
                        this.isUploaded = false;
                    }
                } catch (Exception e) {
                    this.ErrorMsg = "Problem occured while uploading logo";
                    Logger.getInstance(genericFileUpload.class).error(e, e);
                }
            }
        }
    }

    public boolean isUploaded() {
        return isUploaded;
    }

    public String getExt() {
        return Ext;
    }
    public String getCompanyImageExt() {
        return Ext;
    }
    public final void imgResizeCompany(String imagePath, int Width, int Height,
            String fName, boolean ori) throws IOException {
        try {
            // Get a path to the image to resize.
            // ImageIcon is a kluge to make sure the image is fully
            // loaded before we proceed.
            Image sourceImage = new ImageIcon(Toolkit.getDefaultToolkit().getImage(imagePath)).getImage();
            int imageWidth = sourceImage.getWidth(null);
            int imageHeight = sourceImage.getHeight(null);
            if (ori) {
                Width = imageWidth;
                Height = imageHeight;
            } else {
                Width = imageWidth < Width ? imageWidth : Width;
                Height = imageHeight < Height ? imageHeight : Height;
                float imageRatio = ((float) imageWidth / (float) imageHeight);
                float framemageratio = ((float) Width / (float) Height);
                if (imageRatio > framemageratio) {
                    float value = Width / imageRatio;
                    Height = (int) value;

                } else {
                    float value = Height * imageRatio;
                    Width = (int) value;
                }
            }
            BufferedImage resizedImage = this.scaleCompanyImage(sourceImage, Width,
                    Height);
            ImageIO.write(resizedImage, "PNG", new File(fName + ".png"));
            sourceImage.flush();
        } catch (Exception e) {
            this.ErrorMsg = "Problem occured while uploading logo";
            Logger.getInstance(genericFileUpload.class).error(e, e);
        }
    }

    private static BufferedImage createImage(String path, int width, int height) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        image = createCompatibleImage(image);
        image = createBlurImage(image);
        image = resizeImage(image, width, height);
        return image;
    }

    private static BufferedImage createCompatibleImage(BufferedImage image) {
        GraphicsConfiguration gc = BufferedImageGraphicsConfig.getConfig(image);
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage result = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
        Graphics2D g2 = result.createGraphics();
        g2.drawRenderedImage(image, null);
        g2.dispose();
        return result;
    }

    public static BufferedImage createBlurImage(BufferedImage image) {
        float ninth = 1.0f / 9.0f;
        float[] blurKernel = {
            ninth, ninth, ninth,
            ninth, ninth, ninth,
            ninth, ninth, ninth
        };

        Map map = new HashMap();
        map.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        map.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        map.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        RenderingHints hints = new RenderingHints(map);
        BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, blurKernel), ConvolveOp.EDGE_NO_OP, hints);
        return op.filter(image, null);
    }

    private static BufferedImage resizeImage(BufferedImage image, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return resizedImage;
    }

    public final void imgResizeInRatio(String imagePath, int Width, int Height,
            String fName, String contentType) throws IOException {
        try {
            Image sourceImage = new ImageIcon(Toolkit.getDefaultToolkit().getImage(imagePath)).getImage();
            int imageWidth = sourceImage.getWidth(null);
            int imageHeight = sourceImage.getHeight(null);
            Width = imageWidth < Width ? imageWidth : Width;
            Height = imageHeight < Height ? imageHeight : Height;
            float imageRatio = ((float) imageWidth / (float) imageHeight);
            float framemageratio = ((float) Width / (float) Height);
            if (imageRatio > framemageratio) {
                float value = Width / imageRatio;
                Height = (int) value;

            } else {
                float value = Height * imageRatio;
                Width = (int) value;
            }
            /*  BufferedImage resizedImage = this.scaleImage(sourceImage, Width,
            Height);
            ImageIO.write(resizedImage, contentType, new File(fName + Ext));*/
            BufferedImage resizedImage = createImage(imagePath, Width, Height);
            ImageIO.write(resizedImage, "PNG", new File(fName + Ext));
            sourceImage.flush();
        } catch (Exception e) {
            Logger.getInstance(genericFileUpload.class).error(e, e);
        }
    }

    public final void imgResize(String imagePath, int Width, int Height,
            String fName) throws IOException {
        try {
            // Get a path to the image to resize.
            // ImageIcon is a kluge to make sure the image is fully
            // loaded before we proceed.
            Image sourceImage = new ImageIcon(Toolkit.getDefaultToolkit().getImage(imagePath)).getImage();

            BufferedImage resizedImage = this.scaleImage(sourceImage, Width,
                    Height);
            ImageIO.write(resizedImage, "jpeg", new File(fName + ".jpg"));
        } catch (Exception e) {
            Logger.getInstance(genericFileUpload.class).error(e, e);
        }
    }

    private BufferedImage scaleImage(Image sourceImage, int width, int height) {
        ImageFilter filter = new ReplicateScaleFilter(width, height);
        ImageProducer producer = new FilteredImageSource(sourceImage.getSource(), filter);
        Image resizedImage = Toolkit.getDefaultToolkit().createImage(producer);

        return this.toBufferedImage(resizedImage);
    }

    private BufferedImage toBufferedImage(Image image) {
        image = new ImageIcon(image).getImage();
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null),
                image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, image.getWidth(null), image.getHeight(null));
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bufferedImage;
    }
    private BufferedImage scaleCompanyImage(Image sourceImage, int width, int height) {
        ImageFilter filter = new ReplicateScaleFilter(width, height);
        ImageProducer producer = new FilteredImageSource(sourceImage.getSource(), filter);
        Image resizedImage = Toolkit.getDefaultToolkit().createImage(producer);

        return this.toBufferedCompanyImage(resizedImage);
    }

    private BufferedImage toBufferedCompanyImage(Image image) {
        image = new ImageIcon(image).getImage();
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null),
                image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();
//		 java.awt.Color transparent = new java.awt.Color(255, 255, 255, 1);
//                g.setColor(transparent);
//                int rule = java.awt.AlphaComposite.SRC_OVER;
//
//                java.awt.AlphaComposite ac = java.awt.AlphaComposite.getInstance(rule,1);
//                g.setComposite(ac);
//		g.fillRect(0, 0, image.getWidth(null), image.getHeight(null));
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bufferedImage;
    }

    public static void main(String args[]) {
        try {
            genericFileUpload a = new genericFileUpload();
            a.imgResizeCompany("/home/mosin/images/pics/aragorn.jpg", 1300, 1000, "/home/mosin/images/pics/test", true);
        } catch (Exception e) {
        }
    }
}
