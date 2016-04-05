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
package com.ksza.peggy.ui.cards.holders.message;

import android.content.Context;
import android.text.TextUtils;

import com.ksza.peggy.R;
import com.ksza.peggy.ui.cards.model.MessageCardBundle;
import com.ksza.peggy.util.Util;

/**
 * Created by karoly.szanto on 28/07/15.
 */
public class MessageBundleFactory {

    public static MessageCardBundle createMessageBundle(final Context context, final boolean useVersionFeatures) {

        if (useVersionFeatures) {

            final String versionFeaturesMessage = Util.readFileFromAssets(getVersionFeaturesFileName(), context);
            if (!TextUtils.isEmpty(versionFeaturesMessage)) {
                final String title = context.getString(R.string.welcome_message_version_features_title);
                final String description = context.getString(R.string.welcome_message_version_features_details, Util.getApplicationVersionName());
                return new MessageCardBundle(title, description, versionFeaturesMessage, true);
            }
        }

        return createWelcomeMessageBundle(context);
    }

    private static MessageCardBundle createWelcomeMessageBundle(final Context context) {
        final String title = context.getString(R.string.welcome_message_title);
        final String description = context.getString(R.string.welcome_message_details);
        final String welcomeMessage = Util.readFileFromAssets("welcome.html", context);
        return new MessageCardBundle(title, description, welcomeMessage);
    }

    private static String getVersionFeaturesFileName() {
        return String.format("welcome_%d.html", Util.getApplicationVersionNo());
    }
}
