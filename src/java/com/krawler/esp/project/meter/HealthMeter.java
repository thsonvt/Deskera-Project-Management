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
package com.krawler.esp.project.meter;

/**
 *
 * @author krawler
 */
import com.krawler.esp.project.project.Project;
import flexjson.JSON;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HealthMeter {

    private Project project;
    private int ontime, completed, needAttentaion, future, overdue;
    public String IMG_ONTIME = "ontime.gif";
    public String IMG__SLIGHTLY_BEHIND_SCH = "slightly.gif";
    public String IMG_GRAVELY_BEHIND_SCH = "gravely.gif";

    public HealthMeter() {
    }

    public HealthMeter(Project project, int completed, int ontime, int needAttentaion, int overdue, int future) {
        this.project = project;
        this.ontime = ontime;
        this.completed = completed;
        this.needAttentaion = needAttentaion;
        this.future = future;
        this.overdue = overdue;
    }

    public HealthMeter(Project project) {
        this.project = project;
    }

    public HealthMeter(int completed, int ontime, int needAttentaion, int overdue, int future) {
        this.ontime = ontime;
        this.completed = completed;
        this.needAttentaion = needAttentaion;
        this.future = future;
        this.overdue = overdue;
    }

    @Override
    public String toString() {
        return new JSONSerializer().exclude("*.class").exclude("project").deepSerialize(this);
    }

    public HealthMeter JSONtoObject(String serializedJSONString) {
        return new JSONDeserializer<HealthMeter>().deserialize(serializedJSONString);
    }

    public int getCompleted() {
        return completed;
    }

    public void setCompleted(int completed) {
        this.completed = completed;
    }

    public int getFuture() {
        return future;
    }

    public void setFuture(int future) {
        this.future = future;
    }

    public int getNeedAttentaion() {
        return needAttentaion;
    }

    public void setNeedAttentaion(int needAttentaion) {
        this.needAttentaion = needAttentaion;
    }

    public int getOntime() {
        return ontime;
    }

    public void setOntime(int ontime) {
        this.ontime = ontime;
    }

    public int getOverdue() {
        return overdue;
    }

    public void setOverdue(int overdue) {
        this.overdue = overdue;
    }
    @JSON(include=false)
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @JSON(include=false)
    public int getStatus(Map<String, Object> baseValue) {
        int total = this.completed + this.ontime + this.needAttentaion + this.overdue + this.future;
        int percInc = 0;
        try {
            if (total > 0) {
                percInc = ((this.needAttentaion + this.overdue) * 100) / total;
                if (percInc <= (Integer) baseValue.get("ontime")) {
                    return HealthMeterDAO.ONTIME;
                } else if (percInc < (Integer) baseValue.get("slightly")) {
                    return HealthMeterDAO.SL_BH_SH;
                } else if (percInc >= (Integer) baseValue.get("gravely")) {
                    return HealthMeterDAO.GR_BH_SH;
                }
            }
        } catch (Exception ex) {

            Logger.getLogger(HealthMeter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return HealthMeterDAO.ONTIME;
    }
    @JSON(include=false)
    public String getImage(Map<String, Object> baseValue) {
        String img[] = {IMG_ONTIME, IMG__SLIGHTLY_BEHIND_SCH, IMG_GRAVELY_BEHIND_SCH};
        try {
            int status = getStatus(baseValue);
            return "../../images/health_status/" + img[status - 1];
        } catch (Exception ex) {
            Logger.getLogger(HealthMeter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "../../images/health_status/else_status.gif";

    }
    @JSON(include=false)
    public String getMeterData() {
        double percOnTime = 0, percNA = 0, perOD = 0, percCT = 0;
        double total = this.completed + this.ontime + this.needAttentaion + this.overdue + this.future;
        try {
            DecimalFormat df = new DecimalFormat("##.#");
            percCT = (this.completed * 100) / total;
            percOnTime = (this.ontime * 100) / total;
            percNA = (this.needAttentaion * 100) / total;
            perOD = (this.overdue * 100) / total;
            return ";" + df.format(percCT) + ";" + df.format(percOnTime) + ";" + df.format(percNA) + ";" + df.format(perOD);
//        return "{\"data\":{\"value\":1,\"data\":\"values\"}}";
            //return toString();
        } catch (Exception ex) {
            Logger.getLogger(HealthMeter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ";0.0;0.0;0.0;0.0";
    }
    @JSON(include=false)
    public int getTotal() {
        return this.completed + this.ontime + this.needAttentaion + this.overdue + this.future;
    }
}
