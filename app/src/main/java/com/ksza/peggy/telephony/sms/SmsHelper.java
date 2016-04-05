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
package com.ksza.peggy.telephony.sms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;

import com.ksza.peggy.PeggyApplication;
import com.ksza.peggy.storage.ActivityLogAddedEvent;
import com.ksza.peggy.storage.ActivityLogEntry;
import com.ksza.peggy.telephony.call.CallReceiver;
import com.ksza.peggy.util.Analytics;
import com.ksza.peggy.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by karoly.szanto on 27/06/15.
 */
public class SmsHelper {

    private static final Logger logger = LoggerFactory.getLogger(SmsHelper.class);

    private static final String SENT = "SMS_SENT";
    private static final String DELIVERED = "SMS_DELIVERED";

    /**
     * Send the provided message to the specified phone number
     *
     * @param context
     * @param phoneNumber
     * @param message
     */
    public static void sendSMS(final Context context, final String phoneNumber, final String message, final ActivityLogEntry entry, final boolean hangUp) {

        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, new Intent(DELIVERED), 0);

        /* get notified when the sms has been sent */
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK: {
                        logger.error("SMS sent");

                        if(hangUp) {
                            CallReceiver.attemptKillCall(context);
                        }

                        /* long once sent */
                        entry.setMessageSent(true);
                        entry.setSmsText(message);

                        saveActivityLogEntry(entry);

                        PeggyApplication
                                .getInstance()
                                .postSentSmsNotification(entry.getDisplayName(), entry.getActivityStatus().getStatusName());

                        Util.createEasyTrackerEvent(Analytics.ACTIONS_CATEGORY, Analytics.SMS_ACTION, "sms has been sent");

                        break;
                    }
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                    case SmsManager.RESULT_ERROR_RADIO_OFF: {

                        /* long if error appeared */
                        entry.setMessageSent(false);
                        saveActivityLogEntry(entry);

                        break;
                    }
                }
            }
        }, new IntentFilter(SENT));

        /* get notified when the sms has been delivered */
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK: {
                        logger.error("SMS delivered");
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        logger.error("SMS not delivered");
                        break;
                    }
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }

    private static void saveActivityLogEntry(final ActivityLogEntry entry) {

        Util.createEasyTrackerEvent(Analytics.ACTIONS_CATEGORY, Analytics.PEGGY_ACTION, entry.getPeggyAction().getActionName());

        entry.save();

        PeggyApplication
                .getInstance()
                .getBus()
                .post(new ActivityLogAddedEvent(entry));
    }
}
