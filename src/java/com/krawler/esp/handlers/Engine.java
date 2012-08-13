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

import com.octo.captcha.component.image.backgroundgenerator.BackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.*;
import com.octo.captcha.component.image.fontgenerator.FontGenerator;
import com.octo.captcha.component.image.fontgenerator.*;
import com.octo.captcha.component.image.textpaster.RandomTextPaster;
import com.octo.captcha.component.image.textpaster.TextPaster;
import java.awt.*;
 
public class Engine extends com.octo.captcha.engine.image.ListImageCaptchaEngine
{
    protected void buildInitialFactories()
    {
        com.octo.captcha.component.word.wordgenerator.WordGenerator wordGenerator = new com.octo.captcha.component.word.wordgenerator.RandomWordGenerator(
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");
        TextPaster textPaster = new RandomTextPaster(new Integer(5),
                new Integer(5), Color.black);
        BackgroundGenerator backgroundGenerator = new UniColorBackgroundGenerator(
                new Integer(200), new Integer(100),java.awt.Color.WHITE);
        FontGenerator fontGenerator = new DeformedRandomFontGenerator(
                new Integer(15), new Integer(20));
        com.octo.captcha.component.image.wordtoimage.WordToImage wordToImage = new com.octo.captcha.component.image.wordtoimage.ComposedWordToImage(
                fontGenerator, backgroundGenerator, textPaster);
        this.addFactory(
                new com.octo.captcha.image.gimpy.GimpyFactory(wordGenerator,
                        wordToImage));
    }
 
}
