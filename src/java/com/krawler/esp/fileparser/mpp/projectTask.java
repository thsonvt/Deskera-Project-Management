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
package com.krawler.esp.fileparser.mpp;

import java.text.SimpleDateFormat;
import java.util.Date;

public class projectTask {

	private int taskid;
	private String taskname;
	private String duration;
	private Date startdate;
	private Date enddate;
	private String resourcename;
	private String predecessor;
	private Number percentcomplete;
	private String notes;
	private Date actstartdate;
	private Date actenddate;
	private boolean ismilestone;
	private int parent;
	private int level;
	private int flag;
        private int priority;
        private boolean isparent;

	/**
	 * Creates a new instance of projectTask
	 */
	public projectTask(int tkid, String tknm, String durn, Date stdate,
			Date eddate, String resnm, String pred, Number percom, int nor,
			String Notes, Date acsdt, Date acedt, boolean milstone, int flg, int priority, boolean isparent) {

		/*
		 * this.TaskId=tkid; this.TaskName=tknm; this.Durn=durn;
		 * this.StDate=stdate; this.EndDate=eddate; this.ResourceName=resnm;
		 * this.Predecessor=pred; this.PerCom=percom; this.NoOfRes=nor;
		 * this.Notes=Notes; this.AcStDate=acsdt; this.ActEdDate=acedt;
		 * this.IsMilestone=milstone; this.flag=flg;
		 */
		this.taskid = tkid;
		this.taskname = tknm;
		this.duration = durn;
		this.startdate = stdate;
		this.enddate = eddate;
		this.resourcename = resnm;
		this.predecessor = pred;
		this.percentcomplete = percom;
		this.notes = Notes;
		this.actstartdate = acsdt;
		this.actenddate = acedt;
		this.ismilestone = milstone;
		this.flag = flg;
                this.priority = priority;
                this.isparent = isparent;
	}

        public boolean getisparent(){
            return this.isparent;
        }

        public void setisparent(boolean value){
            this.isparent = value;
        }
        public int gettaskid() {
		return taskid;
	}

	public void settaskid(int TaskId) {
		this.taskid = TaskId;
	}

	public String gettaskname() {
		return taskname;
	}

	public void settaskname(String TaskName) {
		this.taskname = TaskName;
	}

	public String getduration() {
		return duration;
	}

	public void setduration(String Durn) {
		this.duration = Durn;
	}

	public int getflag() {
		return this.flag;
	}

	public String getstartdate() {
		// return StDate.toString();
        if(startdate != null){
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            return df.format(startdate).toString();
        } else
            return null;
	}

	public void setstartdate(Date StDate) {
		this.startdate = StDate;
	}

	public String getenddate() {
        if(enddate != null){
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            return df.format(enddate).toString();
        } else
            return null;
		// return EndDate.toString();
	}

	public void setenddate(Date EndDate) {
		this.enddate = EndDate;
	}

	public String getresourcename() {
		return resourcename;
	}

	public void setresourcename(String ResourceName) {
		this.resourcename = ResourceName;
	}

	public String getpredecessor() {
		return predecessor;
	}

	public void setpredecessor(String Predecessor) {
		this.predecessor = Predecessor;
	}

	public Number getpercentcomplete() {
		return percentcomplete;
	}

	public void setpercentcomplete(Number PerCom) {
		this.percentcomplete = PerCom;
	}

	public String getnotes() {
		return notes;
	}

	public void setnotes(String Notes) {
		this.notes = Notes;
	}

	public String getactstartdate() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String abc = null;
		if (actstartdate != null) {
			abc = df.format(actstartdate).toString();
		}
		return abc;
	}

	public void setactstartdate(Date AcStDate) {
		this.actstartdate = AcStDate;
	}

	public String getactenddate() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String abc = null;
		if (actenddate != null) {
			abc = df.format(actenddate).toString();
		}
		return abc;
		// return actenddate;
	}

	public void setactenddate(Date ActEdDate) {
		this.actenddate = ActEdDate;
	}

	public boolean getismilestone() {
		return ismilestone;
	}

	public void setismilestone(boolean IsMilestone) {
		this.ismilestone = IsMilestone;
	}

	public int getparent() {
		return parent;
	}

	public void setparent(int Parent) {
		this.parent = Parent;
	}

	public int getlevel() {
		return level;
	}

	public void setlevel(int Level) {
		this.level = Level;
	}
        
        public void setPriority(int priority){
            this.priority = priority;
        }
        
        public int getPriority(){
            return this.priority;
        }
}
