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
package com.ksza.peggy.ui.cards;

import android.content.Context;
import android.os.AsyncTask;

import com.ksza.peggy.PeggyApplication;
import com.ksza.peggy.prefs.AppPref;
import com.ksza.peggy.prefs.AppPrefsManager;
import com.ksza.peggy.prefs.UserPrefsManager;
import com.ksza.peggy.storage.ActivityLogEntry;
import com.ksza.peggy.ui.cards.holders.message.MessageBundleFactory;
import com.ksza.peggy.ui.cards.model.MessageCardBundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by karoly.szanto on 04/07/15.
 */
public class CardsFetcher extends AsyncTask<Void, CardModel, List<CardModel>> {

    private final String uuid;
    private final CardContainer container;
    private final boolean partialRefresh;

    public CardsFetcher(final String uuid, final CardContainer container, final boolean partialRefresh) {
        this.uuid = uuid;
        this.container = container;
        this.partialRefresh = partialRefresh;
    }

    @Override
    protected List<CardModel> doInBackground(Void... params) {
        final Context context = PeggyApplication.getInstance();
        final AppPrefsManager appPrefsManager = AppPrefsManager.getInstance();
        final AppPref appPrefs = appPrefsManager.getAppPrefs();

        if(!partialRefresh) {
            /* handle system messages first */
            if (!appPrefs.isWelcomeDone()) {
                final MessageCardBundle welcomeMessageBundle = MessageBundleFactory.createMessageBundle(context, appPrefsManager.isUpdate());
                publishProgress(new CardModel(CardType.WELCOME, welcomeMessageBundle));
            } else if (!appPrefs.isSettingsDone()) {
                publishProgress(new CardModel(CardType.USER_PREFS_SETUP, UserPrefsManager.getInstance().getUserPrefs()));
            }
        }

        final List<CardModel> result = new ArrayList<>();

        final List<ActivityLogEntry> logEntries = ActivityLogEntry.listAllInOrder();

        for(ActivityLogEntry entry: logEntries) {
            result.add(LogCardModelFactory.create(entry));
        }

        return result;
    }

    @Override
    protected void onProgressUpdate(CardModel... values) {

        final List<CardModel> models = new ArrayList<>();
        Collections.addAll(models, values);

        if(container != null) {
            container.cardsFetched(uuid, models, partialRefresh);
        }
    }

    @Override
    protected void onPostExecute(List<CardModel> cardModels) {
        if(container != null) {
            container.cardsFetched(uuid, cardModels, partialRefresh);
        }
    }
}
