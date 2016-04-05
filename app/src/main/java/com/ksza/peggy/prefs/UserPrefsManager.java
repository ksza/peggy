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

/**
 * Created by karoly.szanto on 04/07/15.
 */
public class UserPrefsManager {

    private static final String USER_PREF_STORE = "user_prefs";

    private static UserPrefsManager instance;

    private SharedPreferences userPrefsStore;
    private UserPrefs userPrefs;

    public UserPrefs getUserPrefs() {
        return userPrefs.clone();
    }

    public UserPrefs updateUserPrefs(final UserPrefs userPrefs) {

        if(userPrefs.getActivityStatuses() != null) {
            this.userPrefs.setActivityStatuses(userPrefs.getActivityStatuses());
        }

        if(userPrefs.getInteractionType() != null) {
            this.userPrefs.setInteractionType(userPrefs.getInteractionType());
        }

        if(userPrefs.getSelectedContact() != null) {
            this.userPrefs.setSelectedContact(userPrefs.getSelectedContact());
        }

        this.userPrefs.setHangUpAfterSms(userPrefs.shouldHangUpAfterSms());

        this.userPrefs.setBikingSms(userPrefs.getBikingSms());
        this.userPrefs.setDrivingSms(userPrefs.getDrivingSms());
        this.userPrefs.setRunningSms(userPrefs.getRunningSms());

        this.userPrefs.writeToPreference(userPrefsStore);

        return userPrefs.clone();
    }

    public static void initialize(final Context context) {

        if (instance != null) {
            throw new RuntimeException("This singleton has already be initialized!");
        }

        instance = new UserPrefsManager();
        instance.userPrefsStore = context.getSharedPreferences(USER_PREF_STORE, Context.MODE_PRIVATE);
        instance.userPrefs = UserPrefs.readFromPreference(instance.userPrefsStore, context);
    }

    public static UserPrefsManager getInstance() {

        if (instance == null) {
            throw new RuntimeException("Call the initialize() method first!");
        }

        return instance;
    }
}
