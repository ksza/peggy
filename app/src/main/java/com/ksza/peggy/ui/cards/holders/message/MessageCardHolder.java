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
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ksza.peggy.PeggyApplication;
import com.ksza.peggy.R;
import com.ksza.peggy.prefs.AppPref;
import com.ksza.peggy.prefs.AppPrefsManager;
import com.ksza.peggy.ui.cards.CardModel;
import com.ksza.peggy.ui.cards.RemoveCardEvent;
import com.ksza.peggy.ui.cards.details.CardDetailsClickedEvent;
import com.ksza.peggy.ui.cards.details.CardDetailsType;
import com.ksza.peggy.ui.cards.holders.BaseCardHolder;
import com.ksza.peggy.ui.cards.model.MessageCardBundle;
import com.ksza.peggy.ui.widget.CardAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import butterknife.Bind;

/**
 * Created by karoly.szanto on 05/07/15.
 */
public class MessageCardHolder extends BaseCardHolder {

    private static final Logger logger = LoggerFactory.getLogger(MessageCardHolder.class);

    @Bind(R.id.cta_done)
    CardAction doneAction;

    @Bind(R.id.message_container)
    TextView messageContainer;

    @Bind(R.id.action_icon)
    ImageView actionIcon;

    @Bind(R.id.action_title)
    TextView actionTitle;

    @Bind(R.id.action_description)
    TextView actionDescription;

    @Bind(R.id.cta_read_on)
    CardAction readOnAction;

    public MessageCardHolder(View itemView) {

        super(itemView);

        readOnAction.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final CardModel model = (CardModel) v.getTag();
                logger.debug("Clicked on card: {}", model.getCardType());

                PeggyApplication
                        .getInstance()
                        .getBus()
                        .post(generateClickedEvent(model));
            }
        });

        doneAction.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final CardModel model = (CardModel) v.getTag();
                final MessageCardBundle bundle = (MessageCardBundle) model.getDataBundle();

                final AppPref appPref = AppPrefsManager.getInstance().getAppPrefs();
                if(! bundle.isNewVersionMessage()) {
                    /** enable Peggy only for the welcome message */
                    appPref.setPeggyEnabled(true);
                }
                appPref.setHasFinishedWelcome();
                AppPrefsManager.getInstance().updateAppPrefs(appPref);

                logger.debug("Clicked on done: {}", model.getCardType());

                PeggyApplication
                        .getInstance()
                        .getBus()
                        .post(new RemoveCardEvent(model));
            }
        });
    }

    @Override
    protected void onMap(CardModel model, int position, Context context) {

        final MessageCardBundle bundle = (MessageCardBundle) model.getDataBundle();

        actionTitle.setText(bundle.getTitle());

        actionDescription.setText(bundle.getDescription());
        actionDescription.setVisibility(View.VISIBLE);

        actionIcon.setImageResource(R.drawable.peggy_logo);
        actionIcon.setVisibility(View.VISIBLE);

        messageContainer.setText(Html.fromHtml(bundle.getMessage(), null, new WelcomeMessageTagHandler()));

        readOnAction.setActionTitle(R.string.welcome_message_card_read_on_button);
        readOnAction.setActionIcon(R.drawable.ic_speaker_notes_white_24dp);
        readOnAction.setTag(model);

        if(bundle.isNewVersionMessage()) {
            doneAction.setActionTitle(R.string.new_version_done_action);
        } else {
            doneAction.setActionTitle(R.string.enable_peggy);
        }
        doneAction.setActionIcon(R.drawable.ic_done_white_24dp);
        doneAction.setTag(model);
    }

    private CardDetailsClickedEvent generateClickedEvent(final CardModel model) {

        return new CardDetailsClickedEvent(model, CardDetailsType.READ_ON, readOnAction);
    }
}
