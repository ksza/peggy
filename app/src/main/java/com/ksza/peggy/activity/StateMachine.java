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
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.ksza.peggy.PeggyApplication;
import com.ksza.peggy.prefs.AppPrefsManager;
import com.ksza.peggy.prefs.UserPrefs;
import com.ksza.peggy.prefs.UserPrefsManager;
import com.ksza.peggy.storage.ActivityLogAddedEvent;
import com.ksza.peggy.storage.ActivityLogEntry;
import com.ksza.peggy.storage.ActivityStatus;
import com.ksza.peggy.storage.CallStatus;
import com.ksza.peggy.storage.OngoingActivity;
import com.ksza.peggy.storage.PeggyAction;
import com.ksza.peggy.telephony.sms.SmsHelper;
import com.ksza.peggy.util.Analytics;
import com.ksza.peggy.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by karoly.szanto on 03/07/15.
 */
public class StateMachine extends IntentService {

    private static final Logger logger = LoggerFactory.getLogger(StateMachine.class);

    private static final String SERVICE_NAME = "STATE_MACHINE_SERVICE";

    public static final String CALL_STARTED_ACTION = "callStarted";
    public static final String PHONE_NO_KEY = "phoneNoKey";
    public static final String START_DATE_KEY = "startDateKey";

    public static final String CALL_MISSED_ACTION = "callMissed";
    public static final String END_DATE_KEY = "endDateKey";

    public static final String CALL_HANDLED_ACTION = "callHandled";

    public static final String LOG_ACTIVITY_ACTION = "logActivity";
    public static final String ACTIVITY_DETECTED_ACTION = "activityDetected";
    public static final String ACTIVITY_TYPE_KEY = "activityTypeKey";
    public static final String ACTIVITY_ACCURACY_KEY = "activityAccuracyKey";

    public static final String KILLER_INTERVENTION_ACTION = "killerIntervention";

    private static final int DO_NOT_REPLY_TWICE_MINUTES = 5;

    private final AppPrefsManager appPrefsManager = AppPrefsManager.getInstance();

    public StateMachine() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {

            if(appPrefsManager.getAppPrefs().isPeggyEnabled()) {
                if (intent.hasExtra(CALL_STARTED_ACTION)) {
                    handleCallStarted(intent);
                } else if (intent.hasExtra(CALL_MISSED_ACTION)) {
                    handleCallMissed(intent);
                } else if (intent.hasExtra(CALL_HANDLED_ACTION)) {
                    handleCallHandled(intent);
                } else if (intent.hasExtra(ACTIVITY_DETECTED_ACTION)) {
                    handleActivityDetected(intent);
                } else if (intent.hasExtra(LOG_ACTIVITY_ACTION)) {
                    handleLogActivity(intent);
                } else if (intent.hasExtra(KILLER_INTERVENTION_ACTION)) {
                    handleKillerInterventionDetected(intent);
                }
            } else {

                final OngoingActivity ongoingActivity = OngoingActivity.findInstance();
                if(ongoingActivity != null) {
                    stopActivityDetection();
                    saveToLogDb(ongoingActivity, PeggyAction.DISABLED);
                }
            }
        }
    }

    private void handleCallStarted(final Intent intent) {

        final String phoneNo = intent.getStringExtra(PHONE_NO_KEY);
        final Date startDate = new Date(intent.getLongExtra(START_DATE_KEY, 0));

        logger.debug("[State] [CallStarted] " + phoneNo);

        /* at the moment Peggy will not know how to handle two simultaneous incoming calls */
        if (OngoingActivity.findInstance() == null) {

            logger.debug("[State] [CallStarted] creating ongoing entry!");

            String contactName = "";
            try {
                Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNo));
                final Cursor c = getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

                if (c.moveToNext()) {
                    contactName = c.getString(0);
                }

                c.close();
            } catch (Exception e) {
            }

            final OngoingActivity ongoingActivity = new OngoingActivity();
            ongoingActivity.setStartTime(startDate);
            ongoingActivity.setPhoneNo(phoneNo);
            ongoingActivity.setContactName(contactName);
            ongoingActivity.setCallStatus(CallStatus.ONGOING);
            ongoingActivity.setActivityStatus(ActivityStatus.UNDETECTED);

            logger.debug("[State] [CallStarted] - contact detected " + ongoingActivity.getContactName());

            final UserPrefs userPrefs = UserPrefsManager.getInstance().getUserPrefs();
            if(userPrefs.shouldInteractWithContact(ongoingActivity.getPhoneNo(), ongoingActivity.getContactName(), "")) {

                /* after init, start the detection process */
                startActivityDetection();
            } else {

                logger.debug("[State] [CallStarted] - RESTRICTED INTERACTION!");
                ongoingActivity.setPeggyAction(PeggyAction.RESTRICTED_INTERACTION);
            }

            ongoingActivity.save();
        }
    }

    private void handleCallMissed(final Intent intent) {

        final String phoneNo = intent.getStringExtra(PHONE_NO_KEY);
        final Date endDate = new Date(intent.getLongExtra(END_DATE_KEY, 0));

        logger.debug("[State] [CallMissed] " + phoneNo);

        final OngoingActivity ongoingActivity = OngoingActivity.findInstance();

        if (ongoingActivity != null && phoneNo.equals(ongoingActivity.getPhoneNo())) {

            logger.debug("[State] [CallMissed] - updating entry!");

            ongoingActivity.setEndTime(endDate);
            ongoingActivity.setCallStatus(CallStatus.MISSED);

            ongoingActivity.save();

            attemptTakingDecision(ongoingActivity);
        }
    }

    private void handleCallHandled(final Intent intent) {

        final String phoneNo = intent.getStringExtra(PHONE_NO_KEY);
        final Date endDate = new Date(intent.getLongExtra(END_DATE_KEY, 0));

        logger.debug("[State] [CallHandled] " + phoneNo);

        final OngoingActivity ongoingActivity = OngoingActivity.findInstance();
        if (ongoingActivity != null && phoneNo.equals(ongoingActivity.getPhoneNo())) {

            logger.debug("[State] [CallHandled] - updating entry!");

            ongoingActivity.setEndTime(endDate);
            ongoingActivity.setCallStatus(CallStatus.HANDLED);
            ongoingActivity.save();

            attemptTakingDecision(ongoingActivity);
        }
    }

    private void handleLogActivity(final Intent intent) {

        final ActivityStatus activityType = ActivityStatus.fromConstant(intent.getIntExtra(ACTIVITY_TYPE_KEY, -1));
        final int confidence = intent.getIntExtra(ACTIVITY_ACCURACY_KEY, 0);

        logger.debug("[State] [ActivityLog] " + activityType + ", " + confidence);

        final OngoingActivity ongoingActivity = OngoingActivity.findInstance();
        if (ongoingActivity != null) {

            logger.debug("[State] [ActivityLog] - updating entry!");

            ongoingActivity.setConfidence(confidence);
            ongoingActivity.setActivityStatus(activityType);

            ongoingActivity.save();

            attemptTakingDecision(ongoingActivity);
        }
    }

    private void handleActivityDetected(final Intent intent) {

        final ActivityStatus activityType = ActivityStatus.fromConstant(intent.getIntExtra(ACTIVITY_TYPE_KEY, -1));
        final int confidence = intent.getIntExtra(ACTIVITY_ACCURACY_KEY, 0);

        logger.debug("[State] [ActivityDetected] " + activityType + ", " + confidence);

        final OngoingActivity ongoingActivity = OngoingActivity.findInstance();
        if (ongoingActivity != null) {

            if(confidence >= DetectedActivitiesIntentService.CONFIDENCE_THRESHOLD) {

                Util.createEasyTrackerEvent(Analytics.ACTIONS_CATEGORY, Analytics.ACTIVITY_DETECTED_ACTION, activityType.getStatusName());

                ongoingActivity.setConfidence(confidence);
                ongoingActivity.setActivityStatus(activityType);
                ongoingActivity.save();

                if(activityType.isForPeggy()) {

                    /* if one of BIKING, RUNNING, DRIVING */
                    final UserPrefs userPrefs = UserPrefsManager.getInstance().getUserPrefs();
                    if(userPrefs.isInterestedInActivity(activityType)) {

                        /* detected activity of interest */
                        attemptTakingDecision(ongoingActivity);
                        stopActivityDetection();
                    } else {

                        /* detected peggy activity, but user is not interested */
                        saveToLogDb(ongoingActivity, PeggyAction.ACTIVITY_NOT_OF_INTEREST);
                        stopActivityDetection();
                    }
                } else {

                    if(ongoingActivity.getActivityStatus() != ActivityStatus.TILTING) {

                        /* detected an activity which is not of interest for peggy */
                        saveToLogDb(ongoingActivity, PeggyAction.ACTIVITY_NOT_FOR_PEGGY);
                        stopActivityDetection();
                    } else {
                        /* don't stop the detection if activity is TILTING. I've experience false negatives with it */
                        logActivityUpdate(ongoingActivity, activityType, confidence);
                    }
                }
            } else {
                /* low confidence */
                logActivityUpdate(ongoingActivity, activityType, confidence);
            }
        }
    }

    private void logActivityUpdate(final OngoingActivity ongoingActivity, final ActivityStatus activityType, final int confidence) {
        logger.debug("[State] [ActivityDetected] - updating entry!");

        ongoingActivity.setConfidence(confidence);
        ongoingActivity.setActivityStatus(activityType);

        ongoingActivity.save();

        attemptTakingDecision(ongoingActivity);
    }

    private void handleKillerInterventionDetected(final Intent intent) {

        logger.debug("[State] [KillerThread] - came in strong!");

        OngoingActivity ongoingActivity = OngoingActivity.findInstance();
        if(ongoingActivity != null) {
            if (! attemptTakingDecision(ongoingActivity, true)) {

                /* refresh in case it was overridden in the decision taking process */
                ongoingActivity = OngoingActivity.findInstance();
                if (ongoingActivity != null) {

                    if(ongoingActivity.getCallStatus() == CallStatus.ONGOING) {
                        saveToLogDb(ongoingActivity, PeggyAction.CALL_WAS_STILL_GOING_ON);
                    } else if(ongoingActivity.getCallStatus() == CallStatus.MISSED) {

                        if(! ongoingActivity.getActivityStatus().isForPeggy()) {
                            saveToLogDb(ongoingActivity, PeggyAction.ACTIVITY_NOT_FOR_PEGGY);
                        } else {

                            /* the rest should have been already handled while attempting to take a decision */
                            saveToLogDb(ongoingActivity, PeggyAction.UNKNOWN);
                        }

                    } else if(ongoingActivity.getCallStatus() == CallStatus.HANDLED) {
                        saveToLogDb(ongoingActivity, PeggyAction.NO_ACTION_HANDLED_BY_USER);
                    } else {
                        saveToLogDb(ongoingActivity, PeggyAction.UNKNOWN);
                    }
                }
            }
        }

        stopActivityDetection();
    }

    private void startActivityDetection() {
        final Intent startActivityDetection = new Intent(this, ActivityDetectionService.class);
        startActivityDetection.putExtra(ActivityDetectionService.START_ACTIVITY_DETECTION_ACTION, true);
        startService(startActivityDetection);
    }

    private void stopActivityDetection() {
        final Intent stopActivityDetection = new Intent(this, ActivityDetectionService.class);
        stopActivityDetection.putExtra(ActivityDetectionService.STOP_ACTIVITY_DETECTION_ACTION, true);
        startService(stopActivityDetection);
    }

    /* **************************** */
    private boolean attemptTakingDecision(final OngoingActivity ongoingActivity) {
        return attemptTakingDecision(ongoingActivity, false);
    }

    private boolean attemptTakingDecision(final OngoingActivity ongoingActivity, final boolean isFromKillerThread) {

        if (ongoingActivity != null) {

            final CallStatus callStatus = ongoingActivity.getCallStatus();
            final ActivityStatus activityStatus = ongoingActivity.getActivityStatus();

            final PeggyAction earlyPeggyAction = ongoingActivity.getPeggyAction();
            if(earlyPeggyAction != PeggyAction.UNKNOWN) {
                /*
                 * In some cases, like restricted_interaction, we don't even need to start activityDetection. Just log
                 * what went on
                 */
                saveToLogDb(ongoingActivity, earlyPeggyAction);

                return true;
            } else {

                switch (callStatus) {
                    case ONGOING: {

                        final UserPrefs userPrefs = UserPrefsManager.getInstance().getUserPrefs();

                        if (activityStatus.isForPeggy() && ongoingActivity.getConfidence() >= DetectedActivitiesIntentService.CONFIDENCE_THRESHOLD) {
                            if (isFromKillerThread) {
                                /** If the call is not done after the max number of seconds, send a message */
                                if(userPrefs.shouldHangUpAfterSms()) {
                                    callOngoingHangUp(ongoingActivity);
                                } else {
                                    callMissed(ongoingActivity);
                                }
                                stopActivityDetection();

                                return true;
                            } else if(userPrefs.shouldHangUpAfterSms()) {
                                /** if activity was detected early, hangUp if the user configured Peggy as such */
                                callOngoingHangUp(ongoingActivity);
                                stopActivityDetection();
                            }
                        }

                        break;
                    }
                    case MISSED: {

                        if (activityStatus.isForPeggy() && ongoingActivity.getConfidence() >= DetectedActivitiesIntentService.CONFIDENCE_THRESHOLD) {
                            callMissed(ongoingActivity);
                            stopActivityDetection();

                            return true;
                        }

                        break;
                    }
                    case HANDLED: {
                        callHandled(ongoingActivity);
                        stopActivityDetection();

                        return true;
                    }
                    case UNKNOWN: {
                        break;
                    }
                }
            }
        }

        return false;
    }

    private void callOngoingHangUp(final OngoingActivity ongoingActivity) {

        final Context context = PeggyApplication.getInstance();
        final UserPrefs userPrefs = UserPrefsManager.getInstance().getUserPrefs();

        if (userPrefs.isInterestedInActivity(ongoingActivity.getActivityStatus())) {

            if (userPrefs.shouldInteractWithContact(ongoingActivity.getPhoneNo(), ongoingActivity.getContactName(), "")) {

                final long recentReplies = ActivityLogEntry.countEntriesInTimeframeWithSentSms(ongoingActivity.getPhoneNo(), DO_NOT_REPLY_TWICE_MINUTES);

                if (recentReplies <= 0) {
                    final ActivityLogEntry entry = moveOngoingActivityToLogEntry(ongoingActivity, PeggyAction.SENT_SMS);
                    /* we've decided to send an SMS, so we will consider this call as being missed */
                    entry.setCallStatus(CallStatus.MISSED);
                    String smsText = getSmsText(entry.getActivityStatus(), context);

                    if (!TextUtils.isEmpty(smsText)) {
                        SmsHelper.sendSMS(
                                context,
                                ongoingActivity.getPhoneNo(),
                                smsText,
                                entry,
                                true);
                    }
                } else {
                    /** this will be picked up once the call has been missed */
                }
            }
        }
    }

    private void callMissed(final OngoingActivity ongoingActivity) {

        final Context context = PeggyApplication.getInstance();
        final UserPrefs userPrefs = UserPrefsManager.getInstance().getUserPrefs();

        if(userPrefs.isInterestedInActivity(ongoingActivity.getActivityStatus())) {

            if(userPrefs.shouldInteractWithContact(ongoingActivity.getPhoneNo(), ongoingActivity.getContactName(), "")) {

                final long recentReplies = ActivityLogEntry.countEntriesInTimeframeWithSentSms(ongoingActivity.getPhoneNo(), DO_NOT_REPLY_TWICE_MINUTES);

                if(recentReplies > 0) {
                    /* discard */
                    saveToLogDb(ongoingActivity, PeggyAction.DUPLICATE_REPLIES);
                } else {
                    final ActivityLogEntry entry = moveOngoingActivityToLogEntry(ongoingActivity, PeggyAction.SENT_SMS);
                    String smsText = getSmsText(entry.getActivityStatus(), context);

                    if (!TextUtils.isEmpty(smsText)) {
                        SmsHelper.sendSMS(
                                context,
                                ongoingActivity.getPhoneNo(),
                                smsText,
                                entry,
                                false);
                    }
                }
            } else {
                saveToLogDb(ongoingActivity, PeggyAction.RESTRICTED_INTERACTION);
            }
        } else {
            saveToLogDb(ongoingActivity, PeggyAction.ACTIVITY_NOT_OF_INTEREST);
        }
    }

    private void callHandled(final OngoingActivity ongoingActivity) {

        /* do nothing, maybe log to analytics. Clear ongoing! */
        saveToLogDb(ongoingActivity, PeggyAction.NO_ACTION_HANDLED_BY_USER);
    }

    private ActivityLogEntry saveToLogDb(final OngoingActivity ongoingActivity, PeggyAction peggyAction) {

        Util.createEasyTrackerEvent(Analytics.ACTIONS_CATEGORY, Analytics.PEGGY_ACTION, peggyAction.getActionName());

        final ActivityLogEntry entry = moveOngoingActivityToLogEntry(ongoingActivity, peggyAction);
        entry.save();

        PeggyApplication
                .getInstance()
                .getBus()
                .post(new ActivityLogAddedEvent(entry));

        return entry;
    }

    private ActivityLogEntry moveOngoingActivityToLogEntry(final OngoingActivity ongoingActivity, PeggyAction peggyAction) {

        final ActivityLogEntry entry = new ActivityLogEntry();
        entry.setStartTime(ongoingActivity.getStartTime());
        entry.setEndTime(ongoingActivity.getEndTime());
        entry.setCallStatus(ongoingActivity.getCallStatus());
        entry.setPhoneNo(ongoingActivity.getPhoneNo());
        entry.setContactName(ongoingActivity.getContactName());
        entry.setActivityStatus(ongoingActivity.getActivityStatus());
        entry.setConfidence(ongoingActivity.getConfidence());
        entry.setPeggyAction(peggyAction);

        OngoingActivity.deleteAll(OngoingActivity.class);

        return entry;
    }

    private String getSmsText(final ActivityStatus status, final Context context) {

        final UserPrefs userPrefs = UserPrefsManager.getInstance().getUserPrefs();

        switch (status) {
            case BIKING: return userPrefs.getBikingSms();
            case DRIVING: return userPrefs.getDrivingSms();
            case RUNNING: return userPrefs.getRunningSms();
        }

        return null;
    }
}
