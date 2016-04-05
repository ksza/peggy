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
package com.ksza.peggy.storage;

import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karoly.szanto on 28/06/15.
 */
public enum ActivityStatus {

    UNDETECTED("Undetected", false),
    BIKING("Biking", true),
    RUNNING("Running", true),
    DRIVING("Driving", true),
    ON_FOOT("On foot", false),
    STILL("Still", false),
    TILTING("Tilting", false),
    WALKING("Walking", false),
    UNKNOWN("Unknown", false);

    private final String statusName;
    private final boolean forPeggy;

    private ActivityStatus(final String statusName, final boolean forPeggy) {
        this.statusName = statusName;
        this.forPeggy = forPeggy;
    }

    public static ActivityStatus fromString(final String status) {

        for (final ActivityStatus value : values()) {
            if (value.statusName.equals(status)) {
                return value;
            }
        }

        return UNKNOWN;
    }

    public static ActivityStatus fromConstant(final int activity) {

        if(activity == DetectedActivity.ON_BICYCLE) {
            return BIKING;
        } else if(activity == DetectedActivity.IN_VEHICLE) {
            return DRIVING;
        } else if(activity == DetectedActivity.RUNNING) {
            return RUNNING;
        } else if(activity == DetectedActivity.ON_FOOT) {
            return ON_FOOT;
        } else if(activity == DetectedActivity.STILL) {
            return STILL;
        } else if(activity == DetectedActivity.TILTING) {
            return TILTING;
        } else if(activity == DetectedActivity.WALKING) {
            return WALKING;
        } else if(activity == DetectedActivity.WALKING) {
            return WALKING;
        }

        return UNKNOWN;
    }

    public String getStatusName() {
        return statusName;
    }

    public boolean isForPeggy() {
        return forPeggy;
    }

    /* a list of activities which are possibly of interest for Peggy */
    public static List<ActivityStatus> getPossibleActivities() {

        final List<ActivityStatus> result = new ArrayList<>();

        for(ActivityStatus status: values()) {
            if(status.isForPeggy()) {
                result.add(status);
            }
        }

        return result;
    }
}
