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

/**
 * Created by karoly.szanto on 28/06/15.
 */
public enum CallStatus {

    ONGOING("Ongoing"),
    MISSED("Missed"),
    HANDLED("Answered"),
    UNKNOWN("Unknown");

    private final String statusName;

    private CallStatus(final String statusName) {
        this.statusName = statusName;
    }

    public static CallStatus fromString(final String status) {

        for (final CallStatus value : values()) {
            if (value.statusName.equals(status)) {
                return value;
            }
        }

        return UNKNOWN;
    }

    public String getStatusName() {
        return statusName;
    }
}
