/**
 * Copyright (C) 2015-2016 Karoly Szanto
 *
 * This file is part of Peggy.
 *
 * Peggy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Peggy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ksza.peggy.storage;

import com.orm.SugarRecord;

import java.util.Date;
import java.util.List;

/**
 * Created by karoly.szanto on 01/07/15.
 */
public class OngoingActivity extends SugarRecord {

    Date startTime = new Date();
    Date endTime = new Date();
    String phoneNo = "";
    String contactName = "";
    CallStatus callStatus = CallStatus.UNKNOWN;
    ActivityStatus activityStatus = ActivityStatus.UNKNOWN;
    int confidence = 0;
    PeggyAction peggyAction = PeggyAction.UNKNOWN;

    /**
     * We only have on possible instance in this DB
     * @return
     */
    public synchronized static OngoingActivity findInstance() {

        try {
            final List<OngoingActivity> activity = OngoingActivity.listAll(OngoingActivity.class);
            if(activity != null && activity.size() > 0) {
                return activity.get(0);
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    public Date getStartTime() {
        return startTime;
    }
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getPhoneNo() {
        return phoneNo;
    }
    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getContactName() {
        return contactName;
    }
    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public CallStatus getCallStatus() {
        return callStatus;
    }
    public void setCallStatus(CallStatus callStatus) {
        this.callStatus = callStatus;
    }

    public ActivityStatus getActivityStatus() {
        return activityStatus;
    }
    public void setActivityStatus(ActivityStatus activityStatus) {
        this.activityStatus = activityStatus;
    }

    public int getConfidence() {
        return confidence;
    }
    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }

    public void setPeggyAction(PeggyAction peggyAction) {
        this.peggyAction = peggyAction;
    }
    public PeggyAction getPeggyAction() {
        return peggyAction;
    }
}
