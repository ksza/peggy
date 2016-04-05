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
package com.ksza.peggy.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.format.DateUtils;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.ksza.peggy.PeggyApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import util.Config;

/**
 * Created by karoly.szanto on 28/06/15.
 */
public class Util {

    private static final String ACTIVITY_LOG_DATE_FORMAT_PATTERN = "dd MMMM yyyy";
    private static final DateFormat activityLogDateFormat = new SimpleDateFormat(ACTIVITY_LOG_DATE_FORMAT_PATTERN, Locale.UK);

    public static String getActivityLogFormattedDate(final long date) {
        final String formattedDate = activityLogDateFormat.format(date);
        return formattedDate;
    }

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    private static final DateFormat cardDateFormat = new SimpleDateFormat("HH:mm, dd MMM");

    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static String getCardFormattedTimeStamp(final Date date) {
        return cardDateFormat.format(date);
    }

    public static String getRelativeTimeSpan(final String date) {

        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return getRelativeTimeSpan(convertedDate);
    }

    public static String getRelativeTimeSpan(final Date date) {

        Calendar calendar = Calendar.getInstance();
        long timeInMillis = (calendar.getTimeInMillis() - date.getTime());
        int timeDifference = (int) TimeUnit.MILLISECONDS.toDays(timeInMillis);
        if (timeDifference >= 1) {
            return Util.getActivityLogFormattedDate(date.getTime());
        } else {
            CharSequence relativeTimeSpan = DateUtils.getRelativeTimeSpanString(date.getTime(),
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);

            return relativeTimeSpan.toString();
        }
    }

    public static void createEasyTrackerEvent(final String category, final String action, final String label) {
        if (!Config.DEBUG) {
            Tracker tracker = PeggyApplication.getInstance().getTracker();

            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(action)
                    .setLabel(label)
                    .build());
        }
    }

    public static String readFileFromAssets(final String fileName, final Context context) {
        InputStream in = null;
        try {
            in = context.getAssets().open(fileName);
            final StringBuilder sb = new StringBuilder();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String str;

            while ((str = reader.readLine()) != null) {
                sb.append(str);
            }

            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return "";
    }

    public static boolean isPreLolipop() {

        return android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    }

    public static int getApplicationVersionNo() {
        int result;

        try {
            Context context = PeggyApplication.getInstance();
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            result = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            result = -1;
        }

        return result;
    }

    public static String getApplicationVersionName() {
        String result;

        try {
            Context context = PeggyApplication.getInstance();
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            result = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            result = "";
        }

        return result;
    }
}
