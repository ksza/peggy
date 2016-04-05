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
package com.ksza.peggy.telephony.call;

import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.ksza.peggy.activity.StateMachine;
import com.ksza.peggy.util.Analytics;
import com.ksza.peggy.util.Util;

import java.lang.reflect.Method;
import java.util.Date;

public class CallReceiver extends PhoneCallReceiver {

    @Override
    protected void onIncomingCallStarted(final Context ctx, String number, Date start) {
        final Intent callStartedIntent = new Intent(ctx, StateMachine.class);
        callStartedIntent.putExtra(StateMachine.CALL_STARTED_ACTION, true);
        callStartedIntent.putExtra(StateMachine.START_DATE_KEY, start.getTime());
        callStartedIntent.putExtra(StateMachine.PHONE_NO_KEY, number);
        ctx.startService(callStartedIntent);
    }

    @Override
    protected void onIncomingCallPickedUp(Context ctx, String number, Date end) {
        /* once picked up, we don't care about the call anymore */
        final Intent callStartedIntent = new Intent(ctx, StateMachine.class);
        callStartedIntent.putExtra(StateMachine.CALL_HANDLED_ACTION, true);
        callStartedIntent.putExtra(StateMachine.END_DATE_KEY, end.getTime());
        callStartedIntent.putExtra(StateMachine.PHONE_NO_KEY, number);
        ctx.startService(callStartedIntent);
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        final Intent callStartedIntent = new Intent(ctx, StateMachine.class);
        callStartedIntent.putExtra(StateMachine.CALL_MISSED_ACTION, true);
        callStartedIntent.putExtra(StateMachine.END_DATE_KEY, new Date().getTime());

        /** we've noticed, number might sometimes be null */
        if(number == null) {
            number = "";
        }
        callStartedIntent.putExtra(StateMachine.PHONE_NO_KEY, number);
        ctx.startService(callStartedIntent);
    }

    public static boolean attemptKillCall(Context context) {
        Util.createEasyTrackerEvent(Analytics.ACTIONS_CATEGORY, Analytics.KILL_CALL_ACTION, "attempt kill call");
        try {
            // Get the boring old TelephonyManager
            TelephonyManager telephonyManager =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            // Get the getITelephony() method
            Class classTelephony = Class.forName(telephonyManager.getClass().getName());
            Method methodGetITelephony = classTelephony.getDeclaredMethod("getITelephony");

            // Ignore that the method is supposed to be private
            methodGetITelephony.setAccessible(true);

            // Invoke getITelephony() to get the ITelephony interface
            Object telephonyInterface = methodGetITelephony.invoke(telephonyManager);

            // Get the endCall method from ITelephony
            Class telephonyInterfaceClass =
                    Class.forName(telephonyInterface.getClass().getName());
            Method methodEndCall = telephonyInterfaceClass.getDeclaredMethod("endCall");

            // Invoke endCall()
            methodEndCall.invoke(telephonyInterface);

        } catch (Throwable throwable) { // Many things can go wrong with reflection calls
            Util.createEasyTrackerEvent(Analytics.ACTIONS_CATEGORY, Analytics.KILL_CALL_FAILED_ACTION, "kill call failed");
            return false;
        }

        return true;
    }
}