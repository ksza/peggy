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
package com.ksza.peggy.ui.cards.holders;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ksza.peggy.PeggyApplication;
import com.ksza.peggy.R;
import com.ksza.peggy.storage.ActivityLogEntry;
import com.ksza.peggy.storage.CallStatus;
import com.ksza.peggy.storage.PeggyAction;
import com.ksza.peggy.telephony.call.CallUtil;
import com.ksza.peggy.ui.cards.CardModel;
import com.ksza.peggy.ui.cards.details.CardDetailsClickedEvent;
import com.ksza.peggy.ui.cards.details.CardDetailsType;
import com.ksza.peggy.ui.widget.CardAction;
import com.ksza.peggy.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import butterknife.Bind;

/**
 * Created by karoly.szanto on 02/07/15.
 */
public class LogCardHolder extends BaseCardHolder {

    private static final Logger logger = LoggerFactory.getLogger(LogCardHolder.class);

    @Bind(R.id.action_icon)
    ImageView actionIcon;

    @Bind(R.id.action_title)
    TextView actionTitle;

    @Bind(R.id.contact_name)
    TextView actionContact;

    @Bind(R.id.action_description)
    TextView actionDescription;

    @Bind(R.id.card_top_toolbar)
    Toolbar cardTopToolbar;

    @Bind(R.id.call_action)
    TextView callAction;

    @Bind(R.id.activity_type_name)
    TextView activityTypeName;

    @Bind(R.id.activity_when)
    TextView activityWhen;

    @Bind(R.id.activity_image)
    ImageView activityImage;

    @Bind(R.id.cat_call)
    CardAction ctaCall;

    public LogCardHolder(final View itemView) {
        super(itemView);

        ctaCall.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final CardModel cardModel = (CardModel) v.getTag();
                final ActivityLogEntry entry = (ActivityLogEntry) cardModel.getDataBundle();
                CallUtil.startCall(entry.getPhoneNo());
            }
        });

        cardTopToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                logger.debug("Toolbar clicked - view SMS");

                final CardModel model = (CardModel) v.getTag();
                PeggyApplication
                        .getInstance()
                        .getBus()
                        .post(new CardDetailsClickedEvent(model, CardDetailsType.VIEW_SMS, cardTopToolbar));
            }
        });
    }

    public void onMap(final CardModel model, final int position, final Context context) {

        final ActivityLogEntry entry = (ActivityLogEntry) model.getDataBundle();

        setCardTitleBasedOnPeggyAction(context, entry);

        final Spanned whatToDo = Html.fromHtml(context.getString(R.string.cta_call_back, entry.getDisplayName()));
        ctaCall.setActionTitle(whatToDo);
        ctaCall.setActionIcon(R.drawable.ic_call_end_black);
        ctaCall.setTag(model);

        activityTypeName.setText(entry.getActivityStatus().getStatusName());

        final String relativeTime = Util.getCardFormattedTimeStamp(entry.getStartTime());
        activityWhen.setText(relativeTime);

        switch (entry.getActivityStatus()) {
            case BIKING: {
                activityImage.setImageResource(R.drawable.ic_directions_bike_black_24dp);
                break;
            }
            case DRIVING: {
                activityImage.setImageResource(R.drawable.ic_directions_car_black_24dp);
                break;
            }
            case RUNNING: {
                activityImage.setImageResource(R.drawable.ic_directions_run_black_24dp);
                break;
            }
            default: {
                activityImage.setImageResource(R.drawable.ic_not_interested_black_24dp);
                break;
            }
        }

        /* how was the call: missed, answered ... */
        callAction.setText(context.getString(R.string.call_action, entry.getCallStatus().getStatusName()));
        if(entry.getCallStatus() == CallStatus.MISSED) {
            callAction.setTextColor(context.getResources().getColor(R.color.call_missed));
        } else if(entry.getCallStatus() == CallStatus.HANDLED) {
            callAction.setTextColor(context.getResources().getColor(R.color.call_answered));
        }

        cardTopToolbar.setTag(model);
        if(entry.getPeggyAction() == PeggyAction.SENT_SMS && entry.isMessageSent()) {
            cardTopToolbar.setClickable(true);
        } else {
            cardTopToolbar.setClickable(false);
        }
    }

    private void setCardTitleBasedOnPeggyAction(final Context context, final ActivityLogEntry entry) {

        actionDescription.setVisibility(View.VISIBLE);
        actionIcon.setVisibility(View.VISIBLE);
        actionContact.setVisibility(View.VISIBLE);

        int titleResourceId = R.string.unknown_action_title;
        int titleContactId = R.string.no_action_title_contact;
        int descriptionResourceId = R.string.unknown_action_description;
        int titleIcon = R.drawable.ic_not_interested_black_24dp;

        if(entry.getPeggyAction() != null) {
            switch (entry.getPeggyAction()) {

                case SENT_SMS: {

                    if(entry.isMessageSent()) {
                        titleResourceId = R.string.message_sent_action_title;
                        titleContactId = R.string.message_sent_action_title_contact;
                        descriptionResourceId = R.string.message_sent_action_description;
                        titleIcon = R.drawable.ic_message_black_24dp;
                    } else {
                        titleResourceId = R.string.message_error_action_title;
                        titleContactId = R.string.message_error_action_title_contact;
                        descriptionResourceId = R.string.message_error_action_description;
                    }

                    break;
                }

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

                case DUPLICATE_REPLIES: {

                    titleResourceId = R.string.no_action_title;
                    titleContactId = R.string.no_action_title_contact;
                    descriptionResourceId = R.string.no_action_duplicate_reply_description;

                    break;
                }

                case DISABLED: {

                    titleResourceId = R.string.no_action_title;
                    titleContactId = R.string.no_action_title_contact;
                    descriptionResourceId = R.string.no_action_disabled_description;

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
        actionIcon.setImageResource(titleIcon);
    }
}
