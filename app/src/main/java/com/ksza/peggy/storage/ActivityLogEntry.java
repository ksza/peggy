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

import android.text.TextUtils;

import com.orm.SugarRecord;
import com.orm.util.NamingHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by karoly.szanto on 28/06/15.
 */
public class ActivityLogEntry extends SugarRecord implements Serializable {

    Date startTime;
    Date endTime;
    String phoneNo;
    String contactName;
    CallStatus callStatus;
    ActivityStatus activityStatus;
    int confidence;
    String smsText;
    boolean messageSent;
    PeggyAction peggyAction;

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

    public String getSmsText() {
        return smsText;
    }

    public void setSmsText(String smsText) {
        this.smsText = smsText;
    }

    public boolean isMessageSent() {
        return messageSent;
    }

    public void setMessageSent(boolean messageSent) {
        this.messageSent = messageSent;
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

    public PeggyAction getPeggyAction() {
        return peggyAction;
    }

    public void setPeggyAction(PeggyAction peggyAction) {
        this.peggyAction = peggyAction;
    }

    public String getDisplayName() {
        if(! TextUtils.isEmpty(contactName)) {
            return contactName;
        }

        if(! TextUtils.isEmpty(phoneNo)) {
            return phoneNo;
        }

        return "";
    }

    public static List<ActivityLogEntry> listAllInOrder() {

        List<ActivityLogEntry> entries = listAllRelevant();
        if(entries == null) {
            entries = new ArrayList<>();
        } else {
            Collections.reverse(entries);
        }

        return entries;
    }

    /**
     * Only query for the entries Peggy needs to show, for now:
     * <ul>
     *     <li>no older than 2 days</li>
     *     <li>only missed calls OR ongoing calls</li>
     *     <li>only with activities of interest detected</li>
     * </ul>
     * @return
     */
    private static List<ActivityLogEntry> listAllRelevant() {

        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -2);

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                /* no older than 2 days */
                .append(NamingHelper.toSQLNameDefault("startTime")).append(" > ?")
                .append(" and ")

                .append(" ( ")
                /* SENT_SMS */
                .append(NamingHelper.toSQLNameDefault("peggyAction")).append(" = ?")
                .append(" or ")

                /* RESTRICTED_INTERACTION */
                .append(NamingHelper.toSQLNameDefault("peggyAction")).append(" = ?")
                .append(" or ")

                /* DUPLICATE_REPLIES */
                .append(NamingHelper.toSQLNameDefault("peggyAction")).append(" = ?")
                .append(" or ")

                /* ACTIVITY_NOT_OF_INTEREST */
                .append(NamingHelper.toSQLNameDefault("peggyAction")).append(" = ?")
                .append(" ) ")

                /* CALL_MISSED */
                .append(" and ")
                .append(NamingHelper.toSQLNameDefault("callStatus")).append(" = ?");
        return find(
                ActivityLogEntry.class,
                stringBuilder.toString(),

                "" + calendar.getTime().getTime(),
                PeggyAction.SENT_SMS.toString(),
                PeggyAction.RESTRICTED_INTERACTION.toString(),
                PeggyAction.DUPLICATE_REPLIES.toString(),
                PeggyAction.ACTIVITY_NOT_OF_INTEREST.toString(),
                CallStatus.MISSED.toString()
        );
    }

    /**
     * Get all entries for the phoneNo in the last specified minutes, which I've sent an SMS for
     *
     * @param phoneNo The phone number we are interested in
     * @param minutes The last X minutes we are interested in
     * @return
     */
    public static long countEntriesInTimeframeWithSentSms(final String phoneNo, final int minutes) {

        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -minutes);

        return count(
                ActivityLogEntry.class,
                NamingHelper.toSQLNameDefault("phoneNo") + " = ? and " + NamingHelper.toSQLNameDefault("startTime") + " > ? and " + NamingHelper.toSQLNameDefault("messageSent") + " = ?",
                new String[] { phoneNo, "" + calendar.getTime().getTime(), "1" }
        );
    }

    public static long countNoOfSmsToday() {

        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return count(
                ActivityLogEntry.class,
                NamingHelper.toSQLNameDefault("startTime") + " > ? and " + NamingHelper.toSQLNameDefault("messageSent") + " = ?",
                new String[] { "" + calendar.getTime().getTime(), "1" }
        );
    }
}