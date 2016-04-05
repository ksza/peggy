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

import com.ksza.peggy.PeggyApplication;
import com.ksza.peggy.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by karoly.szanto on 03/07/15.
 */
public class TestModelGenerator {

    public static List<ActivityLogEntry> generateTestModel() {

        final List<ActivityLogEntry> entries = new ArrayList<>();

        final ActivityLogEntry entry_mom_last = new ActivityLogEntry();
        entry_mom_last.setCallStatus(CallStatus.MISSED);
        entry_mom_last.setActivityStatus(ActivityStatus.BIKING);
        entry_mom_last.setConfidence(81);
        entry_mom_last.setPeggyAction(PeggyAction.DUPLICATE_REPLIES);
        entry_mom_last.setMessageSent(false);
        entry_mom_last.setContactName("Mom");
        final Calendar cal0 = Calendar.getInstance();
        cal0.add(Calendar.HOUR_OF_DAY, -3);
        cal0.add(Calendar.MINUTE, -5);
        entry_mom_last.setStartTime(cal0.getTime());
        entries.add(entry_mom_last);

        final ActivityLogEntry entry_mom = new ActivityLogEntry();
        entry_mom.setCallStatus(CallStatus.MISSED);
        entry_mom.setActivityStatus(ActivityStatus.BIKING);
        entry_mom.setConfidence(81);
        entry_mom.setPeggyAction(PeggyAction.SENT_SMS);
        entry_mom.setMessageSent(true);
        entry_mom.setSmsText(PeggyApplication.getInstance().getString(R.string.peggy_sms_activity, ActivityStatus.BIKING.getStatusName()));
        entry_mom.setContactName("Mom");
        final Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.HOUR_OF_DAY, -3);
        cal1.add(Calendar.MINUTE, -7);
        entry_mom.setStartTime(cal1.getTime());
        entries.add(entry_mom);

        final ActivityLogEntry entry_restricted = new ActivityLogEntry();
        entry_restricted.setCallStatus(CallStatus.MISSED);
        entry_restricted.setActivityStatus(ActivityStatus.DRIVING);
        entry_restricted.setConfidence(81);
        entry_restricted.setPeggyAction(PeggyAction.RESTRICTED_INTERACTION);
        entry_restricted.setMessageSent(false);
        entry_restricted.setContactName("Alice");
        final Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.HOUR_OF_DAY, -5);
        cal2.add(Calendar.MINUTE, -23);
        entry_restricted.setStartTime(cal2.getTime());
        entries.add(entry_restricted);

        final ActivityLogEntry entry_john = new ActivityLogEntry();
        entry_john.setCallStatus(CallStatus.MISSED);
        entry_john.setActivityStatus(ActivityStatus.DRIVING);
        entry_john.setConfidence(81);
        entry_john.setPeggyAction(PeggyAction.SENT_SMS);
        entry_john.setMessageSent(true);
        entry_john.setContactName("John");
        final Calendar cal3 = Calendar.getInstance();
        cal3.add(Calendar.HOUR_OF_DAY, -9);
        cal3.add(Calendar.MINUTE, -31);
        entry_john.setStartTime(cal3.getTime());
        entries.add(entry_john);

        return entries;
    }
}
