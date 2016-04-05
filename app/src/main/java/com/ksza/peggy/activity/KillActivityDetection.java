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

import android.content.Context;
import android.content.Intent;

import com.ksza.peggy.PeggyApplication;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by karoly.szanto on 03/07/15.
 */
public class KillActivityDetection implements Runnable {

    private final static int MAX_DETECTION_SECONDS = 60;

    @Override
    public void run() {

        final Context context = PeggyApplication.getInstance();

        final Intent callStartedIntent = new Intent(context, StateMachine.class);
        callStartedIntent.putExtra(StateMachine.KILLER_INTERVENTION_ACTION, true);
        context.startService(callStartedIntent);
    }

    public static void startKillerThread() {

        final ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        exec.schedule(new KillActivityDetection(), MAX_DETECTION_SECONDS, TimeUnit.SECONDS);
    }
}
