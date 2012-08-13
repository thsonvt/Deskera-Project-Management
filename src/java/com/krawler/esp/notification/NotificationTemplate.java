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
package com.krawler.esp.notification;

/**
 *
 * @author Abhay
 */
public class NotificationTemplate {

    public static class PlainText {

        public static String Greet_Task(String username, String projectName) {
            return "Hi " + username + ",\n\nYou have following task(s) in project: " + projectName;
        }
        
        public static String Greet_Simple(String username) {
            return "Hi " + username + ",";
        }

        public static String TaskDetails_Task(int slno, String taskName, String percentComplete, String dueDate) {
            return String.format("\n %s) %s (%s Complete) - Due by %s", slno, taskName, percentComplete, dueDate);
        }
        
        public static String TaskDetails_ProjectName(String projectName) {
            return String.format("\nTasks in - %s", projectName);
        }

        public static String TaskDetails_ForManagers(int slno, String taskName, String priority, String percentComplete, String dueDate, String resourcesString) {
            return String.format("\n %s) %s (%s Complete)- Due by %s "
                    + "\n    Priority - %s"
                    + "\n    Assigned to - %s", slno, taskName, percentComplete, dueDate, priority, resourcesString);
        }
        
        public static String Greet_OverdueTask(String username, String projectName) {
            return "Hi " + username + ",\n\nFollowing is the list of your delayed task(s) in project: <b>" + projectName + "</b>";
        }

        public static String FirstLine_OverdueTask() {
            return "\n\nFollowing is the list of your delayed task(s)";
        }
        
        public static String FirstLine_ForManagers() {
            return "\n\nFollowing are the tasks in your projects today.";
        }

        public static String TaskDetails_OverdueTask(int slno, String taskName, String percentComplete, String dueDate) {
            return String.format("\n %s) %s (%s Complete) - was due on %s", slno, taskName, percentComplete, dueDate);
        }

        public static String Footer(String uri, String senderName) {
            return String.format("\n\nYou can log in at:\n%s\n\nSee you on Project Management!\n\n - %s ", uri, senderName);
        }
    }

    public static class HTMLText {

        public static String HeaderAndGreet_Task(String userName, String projectName) {
            return "<html>"
                    + "<head><title>Notification - Tasks for " + projectName + " </title></head>"
                    + "<style type='text/css'>"
                    + "a:link, a:visited, a:active {\n"
                    + "     color: #03C;"
                    + "}\n"
                    + "body {\n"
                    + "	font-family: tahoma, Helvetica, sans-serif;"
                    + "	color: #000;"
                    + "	font-size: 13px;"
                    + "}\n"
                    + "</style><body>"
                    + "	<div>"
                    + "		<p>Hi <strong>" + userName + "</strong>,</p>"
                    + "		<p>You have following task(s) in project: " + projectName + "</p>";
        }
        
        public static String HeaderAndGreet_Simple(String userName) {
            return "<html>"
                    + "<head><title>Notification - Tasks</title></head>"
                    + "<style type='text/css'>"
                    + "a:link, a:visited, a:active {\n"
                    + "     color: #03C;"
                    + "}\n"
                    + "body {\n"
                    + "	font-family: tahoma, Helvetica, sans-serif;"
                    + "	color: #000;"
                    + "	font-size: 13px;"
                    + "}\n"
                    + "</style><body>"
                    + "	<div>"
                    + "		<p>Hi <strong>" + userName + "</strong>,</p>";
        }

        public static String TaskDetails_Task(int slno, String taskName, String percentComplete, String dueDate) {
            return String.format("<p> %s) %s (%s Complete)- Due by %s </p>", slno, taskName, percentComplete, dueDate);
        }
        
        public static String TaskDetails_ProjectName(String projectName) {
            return String.format("<p> Tasks in - <b>%s </b></p>", projectName);
        }
        
        public static String TaskDetails_ForManagers(int slno, String taskName, String priority, String percentComplete, String dueDate, String resourcesString) {
            return String.format("<div style=\"clear:both;padding-left:15px;margin-top:15px;\"> %s) %s (%s Complete) - Due by %s </div>"
                    + "<div style=\"clear:both;padding-left:35px;\">Priority - %s</div>"
                    + "<div style=\"clear:both;padding-left:35px;\">Assigned to - %s</div></p>",
                    slno, taskName, percentComplete, dueDate, priority, resourcesString);
        }

        public static String HeaderAndGreet_OverdueTask(String userName, String projectName) {
            return "<html>"
                    + "<head><title>Notification - Overdue tasks for " + projectName + " </title></head>"
                    + "<style type='text/css'>"
                    + "a:link, a:visited, a:active {\n"
                    + "     color: #03C;"
                    + "}\n"
                    + "body {\n"
                    + "	font-family: tahoma, Helvetica, sans-serif;"
                    + "	color: #000;"
                    + "	font-size: 13px;"
                    + "}\n"
                    + "</style><body>"
                    + "	<div>"
                    + "		<p>Hi <strong>" + userName + "</strong>,</p>"
                    + "		<p>Following is the list of your delayed task(s) in project: <b>" + projectName + "</b></p>";
        }

        public static String FirstLine_OverdueTask() {
            return "<br><p>Following is the list of your <b>delayed task(s)</b></p>";
        }
        
        public static String FirstLine_ForManagers() {
            return "<p>Following are the tasks in your projects today.</p>";
        }

        public static String TaskDetails_OverdueTask(int slno, String taskName, String percentComplete, String dueDate) {
            return String.format("<p> %s) %s (%s Complete) - was due on %s </p>", slno, taskName, percentComplete, dueDate);
        }

        public static String Footer(String uri, String senderName) {
            return String.format("<p>You can log in at:</p><p>%s</p><br/><p>See you on Project Management!</p><p> - %s </p></div></body></html>", uri, senderName);
        }
        
        public static String Contact_Import_Message(String name,String fileName,int totalRecords,int importedRecords,int rejectedRecords) {
            return "Hi "+name+",<br><br>"
                        +"Report for your imported data.<br><br>"
                        +"<b>Module Name :</b> Contact <br>"
                        +"<b>File Name :</b> "+fileName+"<br>"
                        +"<b>Total Records :</b> "+totalRecords+"<br>"
                        +"<b>Imported Records :</b> "+importedRecords+"<br>"
                        +"<b>Rejected Records :</b> "+rejectedRecords+"<br><br>"
                        +"Please check the import log in the system for more details.<br>";
        }
    }
}
