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
package com.ksza.peggy.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.ksza.peggy.R;
import com.ksza.peggy.storage.ActivityStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by karoly.szanto on 04/07/15.
 */
public class UserPrefs implements Serializable, Cloneable {

    private static final Logger logger = LoggerFactory.getLogger(UserPrefs.class);

    private static final String ACTIVITIES_OF_INTEREST_KEY = "ACTIVITIES_OF_INTEREST_KEY";
    private static final String INTERACTION_TYPE_KEY = "INTERACTION_TYPE_KEY";
    private static final String SELECTED_INTERACTION_CONTACTS_KEY = "SELECTED_INTERACTION_CONTACTS_KEY";

    private static final String HANG_UP_AFTER_SMS_KEY = "HANG_UP_AFTER_SMS_KEY";

    private static final String BIKING_SMS_KEY = "BIKING_SMS_KEY";
    private static final String DRIVING_SMS_KEY = "DRIVING_SMS_KEY";
    private static final String RUNNING_SMS_KEY = "RUNNING_SMS_KEY";

    private List<ActivityStatus> activityStatuses = ActivityStatus.getPossibleActivities();
    private InteractionType interactionType = InteractionType.EVERYONE;
    private Set<String> selectedContact = new HashSet<>();

    private boolean hangUpAfterSms;

    private String bikingSms;
    private String drivingSms;
    private String runningSms;

    /**
     * @return deep clone
     */
    @Override
    protected UserPrefs clone() {

        final UserPrefs clonedPrefs = new UserPrefs();

        clonedPrefs.bikingSms = new String(bikingSms);
        clonedPrefs.drivingSms = new String(drivingSms);
        clonedPrefs.runningSms = new String(runningSms);

        clonedPrefs.activityStatuses = new ArrayList<>(activityStatuses);
        clonedPrefs.interactionType = interactionType;
        clonedPrefs.selectedContact = new HashSet<>(selectedContact);

        clonedPrefs.hangUpAfterSms = hangUpAfterSms;

        return clonedPrefs;
    }

    public boolean shouldHangUpAfterSms() {
        return hangUpAfterSms;
    }

    public boolean isInterestedInActivity(final ActivityStatus activity) {
        return activityStatuses.contains(activity);
    }

    public boolean shouldInteractWithContact(final String phoneNumber, final String contactName, final String contactGroup) {

        if (interactionType == InteractionType.EVERYONE) {
            return true;
        } else if (interactionType == InteractionType.SELECTED_CONTACTS_ONLY) {

            return selectedContact.contains(contactName) || selectedContact.contains(phoneNumber);
        }

        return false;
    }

    public void setActivityStatuses(final List<ActivityStatus> statuses) {
        this.activityStatuses = statuses;
    }

    public void setInteractionType(InteractionType interactionType) {
        this.interactionType = interactionType;
    }

    public void setSelectedContact(Set<String> selectedContact) {
        this.selectedContact = selectedContact;
    }

    public void setHangUpAfterSms(boolean hangUpAfterSms) {
        this.hangUpAfterSms = hangUpAfterSms;
    }

    public List<ActivityStatus> getActivityStatuses() {
        return activityStatuses;
    }

    public InteractionType getInteractionType() {
        return interactionType;
    }

    public Set<String> getSelectedContact() {
        return selectedContact;
    }

    public void setBikingSms(String bikingSms) {
        this.bikingSms = bikingSms;
    }

    public String getBikingSms() {
        return bikingSms;
    }

    public void setDrivingSms(String drivingSms) {
        this.drivingSms = drivingSms;
    }

    public String getDrivingSms() {
        return drivingSms;
    }

    public void setRunningSms(String runningSms) {
        this.runningSms = runningSms;
    }

    public String getRunningSms() {
        return runningSms;
    }

    static UserPrefs readFromPreference(final SharedPreferences preferences, final Context context) {

        final UserPrefs userPres = new UserPrefs();

        /* read activities */
        final Set<String> rawActivities = preferences.getStringSet(ACTIVITIES_OF_INTEREST_KEY, null);
        if(rawActivities != null) {

            userPres.activityStatuses = new ArrayList<>();
            for(String rawActivity: rawActivities) {
                final ActivityStatus status = ActivityStatus.fromString(rawActivity);
                userPres.activityStatuses.add(status);
                logger.debug("Read activity_of_interest: {}", status);
            }
        }

        /* read interaction type */
        final int interactionTypeOrdinal = preferences.getInt(INTERACTION_TYPE_KEY, -1);
        if(interactionTypeOrdinal > 0 && interactionTypeOrdinal < InteractionType.values().length) {
            userPres.interactionType = InteractionType.values()[interactionTypeOrdinal];
            logger.debug("Read interaction_type: {}", userPres.interactionType);
        }

        /* read persisted groups */
        userPres.selectedContact = preferences.getStringSet(SELECTED_INTERACTION_CONTACTS_KEY, new HashSet<String>());
        for(String s: userPres.selectedContact) {
            logger.debug("Read selected_contact: {}", s);
        }

        /* read should hang up */
        userPres.hangUpAfterSms = preferences.getBoolean(HANG_UP_AFTER_SMS_KEY, false);

        userPres.runningSms = preferences.getString(RUNNING_SMS_KEY, getDefaultStringForActivity(context, ActivityStatus.RUNNING));
        userPres.bikingSms = preferences.getString(BIKING_SMS_KEY, getDefaultStringForActivity(context, ActivityStatus.BIKING));
        userPres.drivingSms = preferences.getString(DRIVING_SMS_KEY, getDefaultStringForActivity(context, ActivityStatus.DRIVING));

        return userPres;
    }

    private static String getDefaultStringForActivity(final Context context, final ActivityStatus activityStatus) {
        return context.getString(R.string.peggy_sms_activity, activityStatus.getStatusName());
    }

    void writeToPreference(final SharedPreferences preferences) {

        final SharedPreferences.Editor editor = preferences.edit();

        /* write activity types */
        final Set<String> rawActivities = new HashSet<>();
        for(ActivityStatus activity: activityStatuses) {
            logger.debug("Adding activity_of_interest: {}", activity.getStatusName());
            rawActivities.add(activity.getStatusName());
        }
        editor.putStringSet(ACTIVITIES_OF_INTEREST_KEY, rawActivities);

        /* write interaction type */
        logger.debug("Updating interaction_type: {}", interactionType);
        editor.putInt(INTERACTION_TYPE_KEY, interactionType.ordinal());

        /* write persistent groups */
        for(String s: selectedContact) {
            logger.debug("Adding selected_contact_group: {}", s);
        }
        editor.putStringSet(SELECTED_INTERACTION_CONTACTS_KEY, selectedContact);

        /* write should hang up */
        editor.putBoolean(HANG_UP_AFTER_SMS_KEY, hangUpAfterSms);

        editor.putString(BIKING_SMS_KEY, bikingSms);
        editor.putString(DRIVING_SMS_KEY, drivingSms);
        editor.putString(RUNNING_SMS_KEY, runningSms);

        editor.apply();
    }
}
