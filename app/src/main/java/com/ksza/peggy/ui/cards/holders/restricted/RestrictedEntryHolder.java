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
package com.ksza.peggy.ui.cards.holders.restricted;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ksza.peggy.PeggyApplication;
import com.ksza.peggy.R;
import com.ksza.peggy.prefs.UserPrefs;
import com.ksza.peggy.prefs.UserPrefsManager;
import com.ksza.peggy.storage.ActivityLogEntry;
import com.ksza.peggy.storage.CallStatus;
import com.ksza.peggy.telephony.call.CallUtil;
import com.ksza.peggy.ui.cards.CardModel;
import com.ksza.peggy.ui.cards.holders.BaseCardHolder;
import com.ksza.peggy.ui.widget.CardAction;
import com.ksza.peggy.util.Analytics;
import com.ksza.peggy.util.Util;

import java.util.Set;

import butterknife.Bind;

/**
 * Both contact and activity
 *
 * Created by karoly.szanto on 07/07/15.
 */
public class RestrictedEntryHolder extends BaseCardHolder {

    @Bind(R.id.action_icon)
    ImageView actionIcon;

    @Bind(R.id.action_title)
    TextView actionTitle;

    @Bind(R.id.contact_name)
    TextView actionContact;

    @Bind(R.id.action_description)
    TextView actionDescription;

    @Bind(R.id.activity_call_status)
    TextView activityCallStatus;

    @Bind(R.id.activity_when)
    TextView activityWhen;

    @Bind(R.id.card_top_toolbar)
    Toolbar cardTopToolbar;

    @Bind(R.id.cat_add_contact)
    CardAction ctaAddContact;

    @Bind(R.id.cat_configure)
    CardAction ctaConfigureSettings;

    @Bind(R.id.cat_call)
    CardAction ctaCall;

    public RestrictedEntryHolder(View itemView) {
        super(itemView);

        ctaAddContact.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Util.createEasyTrackerEvent(Analytics.UI_CATEGORY, Analytics.ADD_RESTRICTED_CONTACT_ACTION, "adding to enabled contacts");

                final CardModel cardModel = (CardModel) v.getTag();
                final ActivityLogEntry entry = (ActivityLogEntry) cardModel.getDataBundle();
                final UserPrefs userPrefs = UserPrefsManager.getInstance().getUserPrefs();

                final Set<String> selectedContacts = userPrefs.getSelectedContact();
                selectedContacts.add(entry.getDisplayName());

                UserPrefsManager.getInstance().updateUserPrefs(userPrefs);

                PeggyApplication
                        .getInstance()
                        .getBus()
                        .post(new RestrictedCardEvent(cardModel, RestrictedCardEvent.ADD_ACTION));
            }
        });

        ctaConfigureSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Util.createEasyTrackerEvent(Analytics.UI_CATEGORY, Analytics.CONFIGURE_FROM_RESTRICTED_CONTACT_ACTION, "configure settings");

                final CardModel cardModel = (CardModel) v.getTag();

                PeggyApplication
                        .getInstance()
                        .getBus()
                        .post(new RestrictedCardEvent(cardModel, RestrictedCardEvent.CONFIGURE_ACTION));
            }
        });

        ctaCall.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final CardModel cardModel = (CardModel) v.getTag();
                final ActivityLogEntry entry = (ActivityLogEntry) cardModel.getDataBundle();
                CallUtil.startCall(entry.getPhoneNo());
            }
        });
    }

    @Override
    protected void onMap(CardModel model, int position, Context context) {

        final UserPrefs userPrefs = UserPrefsManager.getInstance().getUserPrefs();

        final ActivityLogEntry entry = (ActivityLogEntry) model.getDataBundle();
        setCardTitleBasedOnPeggyAction(context, entry);

        actionIcon.setVisibility(View.VISIBLE);
        actionIcon.setImageResource(R.drawable.ic_not_interested_black_24dp);

        final String relativeTime = Util.getCardFormattedTimeStamp(entry.getStartTime());
        activityCallStatus.setText(context.getString(
                R.string.restricted_card_context_info,
                entry.getCallStatus().getStatusName()
        ));
        activityWhen.setText(relativeTime);
        if(entry.getCallStatus() == CallStatus.MISSED) {
            activityCallStatus.setTextColor(context.getResources().getColor(R.color.call_missed));
        } else if(entry.getCallStatus() == CallStatus.HANDLED) {
            activityCallStatus.setTextColor(context.getResources().getColor(R.color.call_answered));
        }

        ctaAddContact.setActionIcon(R.drawable.ic_add_circle_outline_black_24dp);
        ctaAddContact.setActionTitle(R.string.card_entry_action_add_contact);
        ctaAddContact.setTag(model);
        if(userPrefs.shouldInteractWithContact(entry.getPhoneNo(), entry.getContactName(), "")) {
            ctaAddContact.setVisibility(View.GONE);
        } else {
            ctaAddContact.setVisibility(View.VISIBLE);
        }

        ctaConfigureSettings.setActionIcon(R.drawable.ic_settings_black_24dp);
        ctaConfigureSettings.setActionTitle(R.string.card_entry_action_configure);
        ctaConfigureSettings.setTag(model);

        final Spanned whatToDo = Html.fromHtml(context.getString(R.string.cta_call_back, entry.getDisplayName()));
        ctaCall.setActionTitle(whatToDo);
        ctaCall.setActionIcon(R.drawable.ic_call_end_black);
        ctaCall.setTag(model);
    }

    private void setCardTitleBasedOnPeggyAction(final Context context, final ActivityLogEntry entry) {

        actionDescription.setVisibility(View.VISIBLE);
        actionContact.setVisibility(View.VISIBLE);

        int titleResourceId = R.string.unknown_action_title;
        int titleContactId = R.string.no_action_title_contact;
        int descriptionResourceId = R.string.unknown_action_description;

        if(entry.getPeggyAction() != null) {
            switch (entry.getPeggyAction()) {

                case ACTIVITY_NOT_OF_INTEREST: {

                    titleResourceId = R.string.no_action_title;
                    titleContactId = R.string.no_action_title_contact;
                    descriptionResourceId = R.string.no_action_activity_not_of_interest_description;

                    break;
                }

                case ACTIVITY_NOT_FOR_PEGGY: {

                    titleResourceId = R.string.no_action_title;
                    titleContactId = R.string.no_action_title_contact;
                    descriptionResourceId = R.string.no_action_activity_not_for_peggy_description;

                    break;
                }

                case BAD_ACCURACY: {

                    titleResourceId = R.string.no_action_title;
                    titleContactId = R.string.no_action_title_contact;
                    descriptionResourceId = R.string.no_action_bad_accuracy_description;

                    break;
                }

                case RESTRICTED_INTERACTION: {

                    titleResourceId = R.string.no_action_title;
                    titleContactId = R.string.no_action_title_contact;
                    descriptionResourceId = R.string.no_action_restricted_description;

                    break;
                }

                case NO_ACTION_HANDLED_BY_USER: {

                    titleResourceId = R.string.no_action_title;
                    titleContactId = R.string.no_action_title_contact;
                    descriptionResourceId = R.string.no_action_handled_description;

                    break;
                }

                case CALL_WAS_STILL_GOING_ON: {

                    titleResourceId = R.string.no_action_title;
                    titleContactId = R.string.no_action_title_contact;
                    descriptionResourceId = R.string.no_action_ongoing_description;

                    break;
                }

                case UNKNOWN: {

                    titleResourceId = R.string.unknown_action_title;
                    titleContactId = R.string.no_action_title_contact;
                    descriptionResourceId = R.string.unknown_action_description;

                    break;
                }
            }
        }

        final String whatHappened = context.getString(titleResourceId);
        actionTitle.setText(whatHappened);

        final String forWhom = context.getString(titleContactId, entry.getDisplayName());
        actionContact.setText(forWhom);

        actionDescription.setText(context.getString(descriptionResourceId));
    }
}
