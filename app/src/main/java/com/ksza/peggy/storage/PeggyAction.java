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
 * Created by karoly.szanto on 02/07/15.
 */
public enum PeggyAction {

    SENT_SMS("sent_sms"),
    ACTIVITY_NOT_OF_INTEREST("activity_not_of_interest"),
    ACTIVITY_NOT_FOR_PEGGY("activity_not_for_peggy"),
    BAD_ACCURACY("bad_accuracy"),
    RESTRICTED_INTERACTION("restricted"),
    NO_ACTION_HANDLED_BY_USER("action_handled_by_user"),
    CALL_WAS_STILL_GOING_ON("call_still_going_on"),
    DUPLICATE_REPLIES("duplicate_replies"),
    DISABLED("disabled"),
    UNKNOWN("unknown");

    private final String actionName;

    private PeggyAction(final String actionName) {
        this.actionName = actionName;
    }

    public static PeggyAction fromString(final String status) {

        for (final PeggyAction value : values()) {
            if (value.actionName.equals(status)) {
                return value;
            }
        }

        return UNKNOWN;
    }

    public String getActionName() {
        return actionName;
    }
}
