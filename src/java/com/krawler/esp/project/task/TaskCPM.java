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
package com.krawler.esp.project.task;

import com.krawler.common.util.Utilities;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

/**
 *
 * @author Abhay
 */
public class TaskCPM implements Comparable<TaskCPM> {

    private Task task;
    private double es;
    private double ef;
    private double ls;
    private double lf;
    private double slack;
    private double expected;
    private double sd;
    private double variance;
    private double optimistic;
    private double pessimistic;
    private double likely;
    private boolean isCritical;
    private String taskStatus;

    @Override
    public String toString() {
        return new JSONSerializer().exclude("*.class").deepSerialize(this);
    }

    public static TaskCPM JSONtoObject(String serializedJSONString) {
        return new JSONDeserializer<TaskCPM>().use(null, TaskCPM.class).deserialize(serializedJSONString);
    }

    public TaskCPM() {
        es = ef = ls = lf = slack = expected = sd = variance = 0;
        isCritical = false;
        taskStatus = "";
    }

    public TaskCPM(Task task) {
        this.task = task;
        es = ef = ls = lf = slack = expected = sd = variance = 0;
        isCritical = false;
        taskStatus = "";
    }

    public double getEf() {
        return Utilities.roundDoubleTo(ef, 2);
    }

    public void setEf(double ef) {
        this.ef = Utilities.roundDoubleTo(ef, 2);
    }

    public double getEs() {
        return Utilities.roundDoubleTo(es, 2);
    }

    public void setEs(double es) {
        this.es = Utilities.roundDoubleTo(es, 2);
    }

    public double getExpected() {
        return Utilities.roundDoubleTo(expected, 2);
    }

    public void setExpected(double expected) {
        this.expected = Utilities.roundDoubleTo(expected, 2);
    }

    public double getLf() {
        return Utilities.roundDoubleTo(lf, 2);
    }

    public void setLf(double lf) {
        this.lf = Utilities.roundDoubleTo(lf, 2);
    }

    public double getLikely() {
        return Utilities.roundDoubleTo(likely, 2);
    }

    public void setLikely(double likely) {
        this.likely = Utilities.roundDoubleTo(likely, 2);
    }

    public double getLs() {
        return Utilities.roundDoubleTo(ls, 2);
    }

    public void setLs(double ls) {
        this.ls = Utilities.roundDoubleTo(ls, 2);
    }

    public double getOptimistic() {
        return Utilities.roundDoubleTo(optimistic, 2);
    }

    public void setOptimistic(double optimistic) {
        this.optimistic = Utilities.roundDoubleTo(optimistic, 2);
    }

    public double getPessimistic() {
        return Utilities.roundDoubleTo(pessimistic, 2);
    }

    public void setPessimistic(double pessimistic) {
        this.pessimistic = Utilities.roundDoubleTo(pessimistic, 2);
    }

    public double getSd() {
        return Utilities.roundDoubleTo(sd, 2);
    }

    public void setSd(double sd) {
        this.sd = Utilities.roundDoubleTo(sd, 2);
    }

    public double getSlack() {
        return Utilities.roundDoubleTo(slack, 2);
    }

    public void setSlack(double slack) {
        this.slack = Utilities.roundDoubleTo(slack, 2);
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public double getVariance() {
        return Utilities.roundDoubleTo(variance, 2);
    }

    public void setVariance(double variance) {
        this.variance = Utilities.roundDoubleTo(variance, 2);
    }

    public boolean isIsCritical() {
        return isCritical;
    }

    public void setIsCritical(boolean isCritical) {
        this.isCritical = isCritical;
    }
    
    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }
    
    @Override
    public int compareTo(TaskCPM o) {
        int compareResult = 0;
        if (o != null) {
            if (o.getTask().getTaskIndex() - this.getTask().getTaskIndex() < 0) {
                compareResult = 1;
            }
        }
        return compareResult;
    }
}
