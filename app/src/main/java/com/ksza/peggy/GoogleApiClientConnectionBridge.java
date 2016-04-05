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
package com.ksza.peggy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helps ou get connected to the Google Client Api and executes an action once
 * connected. Whomever requests the connection, will need to disconnect once
 * the done.
 * <p/>
 * Created by karoly.szanto on 09/07/15.
 */
public class GoogleApiClientConnectionBridge implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final Logger logger = LoggerFactory.getLogger(GoogleApiClientConnectionBridge.class);

    private static GoogleApiClientConnectionBridge instance;
    private Context context;

    public static void initialise(final Context context) {

        if(instance != null) {
            throw new RuntimeException("This singleton has already be initialized!");
        }
        instance = new GoogleApiClientConnectionBridge();
        instance.buildGoogleApiClient(context);
        instance.context = context;
    }

    public static GoogleApiClientConnectionBridge getInstance() {

        if (instance == null) {
            throw new RuntimeException("Call the initialize() method first!");
        }

        return instance;
    }

    private Intent clientNotificationIntent;
    public synchronized void requestConnection(final Intent clientNotificationIntent) {
        this.clientNotificationIntent = clientNotificationIntent;
        googleApiClient.connect();
    }

    public synchronized void requestDisconnect() {
        logger.debug("Disconnecting GoogleApiClient!");
        googleApiClient.disconnect();
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient googleApiClient;

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * ActivityRecognition API.
     */
    protected synchronized void buildGoogleApiClient(final Context context) {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        logger.info("Connected to GoogleApiClient");

        if(clientNotificationIntent != null) {
            context.startService(clientNotificationIntent);
            clientNotificationIntent = null;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        logger.error("Connection failed: ConnectionResult.getErrorCode() = {}", result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        logger.info("Connection suspended");
        googleApiClient.connect();
    }
}
