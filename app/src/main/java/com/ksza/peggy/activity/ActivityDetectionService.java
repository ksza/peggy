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
package com.ksza.peggy.activity;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.ksza.peggy.GoogleApiClientConnectionBridge;
import com.ksza.peggy.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by karoly.szanto on 27/06/15.
 */
public class ActivityDetectionService extends IntentService implements ResultCallback<Status> {

    private static final Logger logger = LoggerFactory.getLogger(ActivityDetectionService.class);

    private static final String SERVICE_NAME = "ACTIVITY_DETECTION_SERVICE";

    public static final String START_ACTIVITY_DETECTION_ACTION = "startDetection";
    public static final String STOP_ACTIVITY_DETECTION_ACTION = "stopDetection";

    public static final String ON_GOOGLE_API_CLIENT_READY_ACTION = "onGoogleClientReady";

    /**
     * Used when requesting or removing activity detection updates.
     */
    private PendingIntent mActivityDetectionPendingIntent;

    public ActivityDetectionService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {

        if (workIntent != null) {

            if (workIntent.hasExtra(START_ACTIVITY_DETECTION_ACTION)) {

                final Intent intent = new Intent(getApplicationContext(), ActivityDetectionService.class);
                intent.putExtra(ActivityDetectionService.ON_GOOGLE_API_CLIENT_READY_ACTION, true);
                GoogleApiClientConnectionBridge.getInstance().requestConnection(intent);

            } else if(workIntent.hasExtra(ON_GOOGLE_API_CLIENT_READY_ACTION)) {
                requestActivityUpdates(GoogleApiClientConnectionBridge.getInstance().getGoogleApiClient());
            } else if (workIntent.hasExtra(STOP_ACTIVITY_DETECTION_ACTION)) {
                removeActivityUpdates(GoogleApiClientConnectionBridge.getInstance().getGoogleApiClient());
            }
        }
    }

    /**
     * Runs when the result of calling requestActivityUpdates() and removeActivityUpdates() becomes
     * available. Either method can complete successfully or with an error.
     *
     * @param status The Status returned through a PendingIntent when requestActivityUpdates()
     *               or removeActivityUpdates() are called.
     */
    public void onResult(Status status) {
        if (status.isSuccess()) {
            // Toggle the status of activity updates requested, and save in shared preferences.
            boolean requestingUpdates = !getUpdatesRequestedState();
            setUpdatesRequestedState(requestingUpdates);

            if(requestingUpdates) {
                KillActivityDetection.startKillerThread();
            }

            updateOngoingActivityDetectionTimes(requestingUpdates);

            if(! requestingUpdates) {
                GoogleApiClientConnectionBridge.getInstance().requestDisconnect();
            }
        } else {
            logger.error("Error adding or removing activity detection: {}", status.getStatusMessage());
        }
    }

    private void updateOngoingActivityDetectionTimes(final boolean requestingUpdates) {

        if (requestingUpdates) {
            /* log activity request started time */
        } else {
            /* log activity request end time */
        }
    }

    /**
     * Retrieves a SharedPreference object used to store or read values in this app. If a
     * preferences file passed as the first argument to {@link #getSharedPreferences}
     * does not exist, it is created when {@link SharedPreferences.Editor} is used to commit
     * data.
     */
    private SharedPreferences getSharedPreferencesInstance() {
        return getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
    }

    /**
     * Retrieves the boolean from SharedPreferences that tracks whether we are requesting activity
     * updates.
     */
    private boolean getUpdatesRequestedState() {
        return getSharedPreferencesInstance()
                .getBoolean(Constants.ACTIVITY_UPDATES_REQUESTED_KEY, false);
    }

    /**
     * Sets the boolean in SharedPreferences that tracks whether we are requesting activity
     * updates.
     */
    private void setUpdatesRequestedState(boolean requestingUpdates) {
        getSharedPreferencesInstance()
                .edit()
                .putBoolean(Constants.ACTIVITY_UPDATES_REQUESTED_KEY, requestingUpdates)
                .commit();
    }

    /**
     * Registers for activity recognition updates using
     * {@link com.google.android.gms.location.ActivityRecognitionApi#requestActivityUpdates} which
     * returns a {@link com.google.android.gms.common.api.PendingResult}. Since this activity
     * implements the PendingResult interface, the activity itself receives the callback, and the
     * code within {@code onResult} executes. Note: once {@code requestActivityUpdates()} completes
     * successfully, the {@code DetectedActivitiesIntentService} starts receiving callbacks when
     * activities are detected.
     */
    public void requestActivityUpdates(final GoogleApiClient googleApiClient) {
        logger.info("Starting activity updates!");

        if (!googleApiClient.isConnected()) {
            logger.info(getString(R.string.not_connected));
            return;
        }
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                googleApiClient,
                Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
                getActivityDetectionPendingIntent()
        ).setResultCallback(this);
    }

    /**
     * Removes activity recognition updates using
     * {@link com.google.android.gms.location.ActivityRecognitionApi#removeActivityUpdates} which
     * returns a {@link com.google.android.gms.common.api.PendingResult}. Since this activity
     * implements the PendingResult interface, the activity itself receives the callback, and the
     * code within {@code onResult} executes. Note: once {@code removeActivityUpdates()} completes
     * successfully, the {@code DetectedActivitiesIntentService} stops receiving callbacks about
     * detected activities.
     */
    public void removeActivityUpdates(final GoogleApiClient googleApiClient) {
        logger.info("Stopping activity updates!");

        if (!googleApiClient.isConnected()) {
            logger.info(getString(R.string.not_connected));
            return;
        }
        // Remove all activity updates for the PendingIntent that was used to request activity
        // updates.
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                googleApiClient,
                getActivityDetectionPendingIntent()
        ).setResultCallback(this);
    }

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    private PendingIntent getActivityDetectionPendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mActivityDetectionPendingIntent != null) {
            return mActivityDetectionPendingIntent;
        }
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
