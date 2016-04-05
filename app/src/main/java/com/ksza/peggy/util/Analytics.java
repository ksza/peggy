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
package com.ksza.peggy.util;

/**
 * Created by karoly.szanto on 24/07/15.
 */
public class Analytics {

    public final static String ACTIONS_CATEGORY = "Business Actions";
    public final static String SMS_ACTION = "sent SMS";
    public final static String KILL_CALL_ACTION = "attempt kill call";
    public final static String KILL_CALL_FAILED_ACTION = "kill call Failed";
    public final static String ACTIVITY_DETECTED_ACTION = "activity detected";
    public final static String PEGGY_ACTION = "peggy action";

    public final static String UI_CATEGORY = "UI actions";
    public final static String ADD_RESTRICTED_CONTACT_ACTION = "add restricted contact";
    public final static String CONFIGURE_FROM_RESTRICTED_CONTACT_ACTION = "configure from restricted contact";
    public final static String SWIPED_ACTION = "card swiped";
    public final static String UNDO_SWIPE_ACTION = "undo swipe";
}
