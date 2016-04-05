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

import com.ksza.peggy.util.Util;

/**
 * Created by karoly.szanto on 12/07/15.
 */
public class AppPrefsManager {

    private static final String APP_PREF_STORE = "app_prefs";
    private SharedPreferences appPrefsStore;

    private static AppPrefsManager instance;
    private AppPref appPrefs;

    /** if the current app run is an update */
    private boolean isUpdate;

    public static void initialize(final Context context) {

        if (instance != null) {
            throw new RuntimeException("This singleton has already be initialized!");
        }

        instance = new AppPrefsManager();
        instance.appPrefsStore = context.getSharedPreferences(APP_PREF_STORE, Context.MODE_PRIVATE);
        instance.appPrefs = AppPref.readFromPreference(instance.appPrefsStore, context);

        final boolean result = instance.appPrefs.setCurrentVersionKey(Util.getApplicationVersionNo());
        if (result) {
            instance.modifyOnUpdate();
            instance.appPrefs.writeToPreference(instance.appPrefsStore);
        }
    }

    private void modifyOnUpdate() {
        if(appPrefs.getOldAppVersion() > 0 && appPrefs.getOldAppVersion() <= 3 && appPrefs.getCurrentAppVersion() == 4) {
            isUpdate = true;

            /* Just added the new auto reject_call feature. We need to allow the user to update settings */
            final UserPrefs userPrefs = UserPrefsManager.getInstance().getUserPrefs();
            appPrefs.setHasFinishedWelcome(false);
            appPrefs.setHasFinishedSettings(false);
        }
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public static AppPrefsManager getInstance() {

        if (instance == null) {
            throw new RuntimeException("Call the initialize() method first!");
        }

        return instance;
    }

    public AppPref updateAppPrefs(final AppPref appPref) {

        this.appPrefs.setPeggyEnabled(appPref.isPeggyEnabled());
        this.appPrefs.writeToPreference(instance.appPrefsStore);

        return this.appPrefs;
    }

    public AppPref getAppPrefs() {
        return appPrefs;
    }
}
