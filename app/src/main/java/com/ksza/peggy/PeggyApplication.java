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
package com.ksza.peggy;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.ksza.peggy.activity.ActivityDetectionService;
import com.ksza.peggy.prefs.AppPrefsManager;
import com.ksza.peggy.prefs.UserPrefsManager;
import com.ksza.peggy.storage.OngoingActivity;
import com.ksza.peggy.ui.MainActivity;
import com.orm.SugarApp;
import com.orm.SugarContext;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import io.fabric.sdk.android.Fabric;
import util.Config;

/**
 * Created by karoly.szanto on 27/06/15.
 */
public class PeggyApplication extends SugarApp {

    private static PeggyApplication instance;

    private final Bus bus = new Bus(ThreadEnforcer.ANY);

    public GoogleAnalytics analytics;
    public Tracker tracker;

    public Bus getBus() {
        return bus;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        initFabric();
        initAnalytics();

        SugarContext.init(this);

        cleanupOngoingActivityInfo();
        stopActivityDetection();

        UserPrefsManager.initialize(this);
        AppPrefsManager.initialize(this);

        GoogleApiClientConnectionBridge.initialise(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SugarContext.terminate();
    }

    public static PeggyApplication getInstance() {
        return instance;
    }

    private void initFabric() {
        if (!Config.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
    }

    private void initAnalytics() {
        if(!Config.DEBUG) {
            analytics = GoogleAnalytics.getInstance(this);
            analytics.setLocalDispatchPeriod(300);

            tracker = analytics.newTracker("UA-64584312-1");
            tracker.enableExceptionReporting(true);
            tracker.enableAdvertisingIdCollection(true);
            tracker.enableAutoActivityTracking(true);
        }
    }

    public Tracker getTracker() {

        if(Config.DEBUG) {
            throw new RuntimeException("Analytics available in Release builds only");
        }

        return tracker;
    }

    /**
     * Make sure we are not relying on any leftovers from a previous session
     */
    private void cleanupOngoingActivityInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OngoingActivity.deleteAll(OngoingActivity.class);
            }
        }).start();
    }

    private void stopActivityDetection() {
        final Intent stopActivityDetection = new Intent(this, ActivityDetectionService.class);
        stopActivityDetection.putExtra(ActivityDetectionService.STOP_ACTIVITY_DETECTION_ACTION, true);
        startService(stopActivityDetection);
    }

    private static final String NOTIFICATIONS_GROUP_KEY = "notification_group_key";
    private static final int NOTIFICATION_ID = 1;

    public void postSentSmsNotification(final String contactName, final String activityName) {

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Creates an Intent for the Activity
        Intent notifyIntent = new Intent(this, MainActivity.class);

        // Sets the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Creates the PendingIntent
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // Puts the PendingIntent into the notification builder
        builder.setContentIntent(notifyPendingIntent);

        builder.setContentTitle(getString(R.string.sent_sms_notification_title, contactName))
                .setContentText(getString(R.string.sent_sms_notification_detail, activityName))
                .setSmallIcon(R.drawable.peggy_launcher)
                .setGroup(NOTIFICATIONS_GROUP_KEY)
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
