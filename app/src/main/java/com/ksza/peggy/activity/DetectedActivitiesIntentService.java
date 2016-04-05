/**
 * Copyright (C) 2015-2016 Karoly Szanto
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ksza.peggy.activity;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * IntentService for handling incoming intents that are generated as a result of requesting
 * activity updates using
 * {@link com.google.android.gms.location.ActivityRecognitionApi#requestActivityUpdates}.
 */
public class DetectedActivitiesIntentService extends IntentService {

    private static final Logger logger = LoggerFactory.getLogger(DetectedActivitiesIntentService.class);

    private static final String SERVICE_NAME = "DETECTED_ACTIVITIES_SERVICE";

    public static final int CONFIDENCE_THRESHOLD = 75;

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public DetectedActivitiesIntentService() {
        // Use the TAG to name the worker thread.
        super(SERVICE_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Handles incoming intents.
     *
     * @param intent The Intent is provided (inside a PendingIntent) when requestActivityUpdates()
     *               is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        if (result != null) {
            // Get the list of the probable activities associated with the current state of the
            // device. Each activity is associated with a confidence level, which is an int between
            // 0 and 100.
            ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

            // Log each activity.
            logger.info("activities detected");

            DetectedActivity highestConfidenceAccuracy = null;

            for (DetectedActivity da : detectedActivities) {
                logger.info(Constants.getActivityString(getApplicationContext(), da.getType()) + " " + da.getConfidence() + "%");

                if (highestConfidenceAccuracy == null || highestConfidenceAccuracy.getConfidence() < da.getConfidence()) {
                    highestConfidenceAccuracy = da;
                }
            }

            if (highestConfidenceAccuracy != null) {

                switch (highestConfidenceAccuracy.getType()) {

                    case DetectedActivity.ON_BICYCLE:
                    case DetectedActivity.IN_VEHICLE:
                    case DetectedActivity.RUNNING: {

                        if (highestConfidenceAccuracy.getConfidence() >= CONFIDENCE_THRESHOLD) {
                            final Intent callStartedIntent = new Intent(this, StateMachine.class);
                            callStartedIntent.putExtra(StateMachine.ACTIVITY_DETECTED_ACTION, true);
                            callStartedIntent.putExtra(StateMachine.ACTIVITY_TYPE_KEY, highestConfidenceAccuracy.getType());
                            callStartedIntent.putExtra(StateMachine.ACTIVITY_ACCURACY_KEY, highestConfidenceAccuracy.getConfidence());
                            this.startService(callStartedIntent);
                        }

                        break;
                    }
                    default: {

                        if (highestConfidenceAccuracy.getConfidence() >= CONFIDENCE_THRESHOLD) {

                            final Intent callStartedIntent = new Intent(this, StateMachine.class);
                            callStartedIntent.putExtra(StateMachine.ACTIVITY_DETECTED_ACTION, true);
                            callStartedIntent.putExtra(StateMachine.ACTIVITY_TYPE_KEY, highestConfidenceAccuracy.getType());
                            callStartedIntent.putExtra(StateMachine.ACTIVITY_ACCURACY_KEY, highestConfidenceAccuracy.getConfidence());
                            this.startService(callStartedIntent);
                        } else {

                            final Intent callStartedIntent = new Intent(this, StateMachine.class);
                            callStartedIntent.putExtra(StateMachine.LOG_ACTIVITY_ACTION, true);
                            callStartedIntent.putExtra(StateMachine.ACTIVITY_TYPE_KEY, highestConfidenceAccuracy.getType());
                            callStartedIntent.putExtra(StateMachine.ACTIVITY_ACCURACY_KEY, highestConfidenceAccuracy.getConfidence());
                            this.startService(callStartedIntent);
                        }

                        break;
                    }
                }
            }
        }
    }
}