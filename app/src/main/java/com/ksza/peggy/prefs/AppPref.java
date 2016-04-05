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
 * Created by karoly.szanto on 12/07/15.
 */
public class AppPref {

    private static final String OLD_VERSION_KEY = "OLD_VERSION_KEY";
    private static final String CURRENT_VERSION_KEY = "CURRENT_VERSION_KEY";
    private static final String PEGGY_ENABLED_KEY = "PEGGY_ENABLED_KEY";

    private static final String WELCOME_DONE_KEY = "WELCOME_DONE_KEY";
    private static final String SETTINGS_DONE_KEY = "SETTINGS_DONE_KEY";

    private int oldAppVersion;
    private int currentAppVersion;
    private boolean peggyEnabled;

    private boolean welcomeDone;
    private boolean settingsDone;

    static AppPref readFromPreference(final SharedPreferences preferences, final Context context) {

        final AppPref appPref = new AppPref();

        appPref.oldAppVersion = preferences.getInt(OLD_VERSION_KEY, -1);
        appPref.currentAppVersion = preferences.getInt(CURRENT_VERSION_KEY, -1);
        appPref.peggyEnabled = preferences.getBoolean(PEGGY_ENABLED_KEY, false);

        appPref.welcomeDone = preferences.getBoolean(WELCOME_DONE_KEY, false);
        appPref.settingsDone = preferences.getBoolean(SETTINGS_DONE_KEY, false);

        return appPref;
    }

    void writeToPreference(final SharedPreferences preferences) {
        final SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(OLD_VERSION_KEY, oldAppVersion);
        editor.putInt(CURRENT_VERSION_KEY, currentAppVersion);
        editor.putBoolean(PEGGY_ENABLED_KEY, peggyEnabled);

        editor.putBoolean(WELCOME_DONE_KEY, welcomeDone);
        editor.putBoolean(SETTINGS_DONE_KEY, settingsDone);

        editor.apply();
    }

    public void setPeggyEnabled(final boolean enabled) {
        this.peggyEnabled = enabled;
    }

    public void togglePeggyEnabled() {
        setPeggyEnabled(! peggyEnabled);
    }

    public boolean isPeggyEnabled() {
        return peggyEnabled;
    }

    public void setHasFinishedWelcome() {
        this.welcomeDone = true;
    }

    public void setHasFinishedWelcome(final boolean hasFinishedWelcome) {
        this.welcomeDone = hasFinishedWelcome;
    }
    public boolean isWelcomeDone() {
        return welcomeDone;
    }

    public void setHasFinishedSettings() {
        this.settingsDone = true;
    }
    public void setHasFinishedSettings(final boolean hasFinishedSettings) {
        this.settingsDone = hasFinishedSettings;
    }
    public boolean isSettingsDone() {
        return settingsDone;
    }

    public int getCurrentAppVersion() {
        return currentAppVersion;
    }

    public int getOldAppVersion() {
        return oldAppVersion;
    }

    public boolean setCurrentVersionKey(final int currentAppVersion) {

        if(this.currentAppVersion != currentAppVersion) {
            this.oldAppVersion = this.currentAppVersion;
            this.currentAppVersion = currentAppVersion;
            return true;
        }

        return false;
    }
}
