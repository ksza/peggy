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
package com.ksza.peggy.ui.cards.holders.prefs;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ksza.peggy.PeggyApplication;
import com.ksza.peggy.R;
import com.ksza.peggy.prefs.AppPref;
import com.ksza.peggy.prefs.AppPrefsManager;
import com.ksza.peggy.prefs.InteractionType;
import com.ksza.peggy.prefs.UserPrefs;
import com.ksza.peggy.prefs.UserPrefsManager;
import com.ksza.peggy.storage.ActivityStatus;
import com.ksza.peggy.ui.cards.CardModel;
import com.ksza.peggy.ui.cards.RemoveCardEvent;
import com.ksza.peggy.ui.cards.details.CardDetailsClickedEvent;
import com.ksza.peggy.ui.cards.details.CardDetailsType;
import com.ksza.peggy.ui.cards.holders.BaseCardHolder;
import com.ksza.peggy.ui.widget.CardAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import butterknife.Bind;

/**
 * Created by karoly.szanto on 04/07/15.
 */
public class UserPrefHolder extends BaseCardHolder {

    private static final Logger logger = LoggerFactory.getLogger(UserPrefHolder.class);

    @Bind(R.id.action_icon)
    ImageView actionIcon;

    @Bind(R.id.action_title)
    TextView actionTitle;

    @Bind(R.id.action_description)
    TextView actionDescription;

    @Bind(R.id.biking_checkbox)
    CheckBox bikingCheckbox;

    @Bind(R.id.driving_checkbox)
    CheckBox drivingCheckbox;

    @Bind(R.id.running_checkbox)
    CheckBox runningCheckbox;

    @Bind(R.id.everybody_button)
    RadioButton everybodyButton;

    @Bind(R.id.preferred_contacts_button)
    RadioButton preferredContactsButton;

    @Bind(R.id.selected_contacts_button)
    Button selectedContactsButton;

    @Bind(R.id.hang_up_checkbox)
    CheckBox hangUpAfterSms;

    @Bind(R.id.cta_done)
    CardAction ctaDone;

    @Bind(R.id.cta_configure_sms)
    CardAction ctaConfigureSms;

    public UserPrefHolder(View itemView) {

        super(itemView);

        selectedContactsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final CardModel model = (CardModel) v.getTag();

                logger.debug("Selected custom users group");
                PeggyApplication
                        .getInstance()
                        .getBus()
                        .post(new CardDetailsClickedEvent(model, CardDetailsType.SELECT_CONTACTS, selectedContactsButton));
            }
        });
    }

    @Override
    protected void onMap(final CardModel model, final int position, Context context) {

        actionIcon.setImageResource(R.drawable.ic_settings_white_24dp);
        actionIcon.setVisibility(View.VISIBLE);

        actionTitle.setText(context.getString(R.string.setup_card_title));

        actionDescription.setText(context.getString(R.string.setup_card_description));
        actionDescription.setVisibility(View.VISIBLE);

        final UserPrefs userPrefs = (UserPrefs) model.getDataBundle();

        ctaConfigureSms.setActionIcon(R.drawable.ic_message_white_24dp);
        ctaConfigureSms.setActionTitle(R.string.setup_card_configure_sms);

        ctaDone.setActionIcon(R.drawable.ic_done_white_24dp);
        ctaDone.setActionTitle(R.string.setup_card_done);

        ctaConfigureSms.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                logger.debug("Configure SMS");

                PeggyApplication
                        .getInstance()
                        .getBus()
                        .post(new CardDetailsClickedEvent(model, CardDetailsType.CONFIGURE_SMS, ctaConfigureSms));

            }
        });

        ctaDone.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                logger.debug("User pref setup done!");

                UserPrefsManager.getInstance().updateUserPrefs(userPrefs);

                final AppPref appPref = AppPrefsManager.getInstance().getAppPrefs();
                appPref.setHasFinishedSettings();
                AppPrefsManager.getInstance().updateAppPrefs(appPref);

                PeggyApplication
                        .getInstance()
                        .getBus()
                        .post(new RemoveCardEvent(model));
            }
        });

        bikingCheckbox.setChecked(userPrefs.getActivityStatuses().contains(ActivityStatus.BIKING));
        drivingCheckbox.setChecked(userPrefs.getActivityStatuses().contains(ActivityStatus.DRIVING));
        runningCheckbox.setChecked(userPrefs.getActivityStatuses().contains(ActivityStatus.RUNNING));

        bikingCheckbox.setOnCheckedChangeListener(new ActivityCheckboxListener(ActivityStatus.BIKING, userPrefs));
        drivingCheckbox.setOnCheckedChangeListener(new ActivityCheckboxListener(ActivityStatus.DRIVING, userPrefs));
        runningCheckbox.setOnCheckedChangeListener(new ActivityCheckboxListener(ActivityStatus.RUNNING, userPrefs));

        hangUpAfterSms.setChecked(userPrefs.shouldHangUpAfterSms());
        hangUpAfterSms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userPrefs.setHangUpAfterSms(isChecked);
            }
        });

        final InteractionTypeRadioListener interactionTypeRadioListener = new InteractionTypeRadioListener(userPrefs);
        everybodyButton.setOnClickListener(interactionTypeRadioListener);
        preferredContactsButton.setOnClickListener(interactionTypeRadioListener);

        preferredContactsButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(preferredContactsButton.isChecked()) {
                    selectedContactsButton.setVisibility(View.VISIBLE);
                } else {
                    selectedContactsButton.setVisibility(View.GONE);
                }
            }
        });

        switch (userPrefs.getInteractionType()) {
            case EVERYONE: {
                everybodyButton.setChecked(true);
                preferredContactsButton.setChecked(false);
                break;
            }
            case SELECTED_CONTACTS_ONLY: {
                preferredContactsButton.setChecked(true);
                everybodyButton.setChecked(false);
                break;
            }
        }

        if(preferredContactsButton.isChecked()) {
            selectedContactsButton.setVisibility(View.VISIBLE);
        } else {
            selectedContactsButton.setVisibility(View.GONE);
        }
        selectedContactsButton.setText(context.getString(R.string.setup_card_selected_contacts, userPrefs.getSelectedContact().size()));
        selectedContactsButton.setTag(model);
    }

    private class ActivityCheckboxListener implements CompoundButton.OnCheckedChangeListener {

        private final ActivityStatus status;
        private final UserPrefs userPrefs;

        ActivityCheckboxListener(final ActivityStatus status, final UserPrefs userPrefs) {
            this.status = status;
            this.userPrefs = userPrefs;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            if (isChecked) {
                this.userPrefs.getActivityStatuses().add(status);
                logger.debug("[Setup card] added " + status.getStatusName());
            } else {
                this.userPrefs.getActivityStatuses().remove(status);
                logger.debug("[Setup card] removed " + status.getStatusName());
            }
        }
    }

    private class InteractionTypeRadioListener implements View.OnClickListener {

        private final UserPrefs userPrefs;

        public InteractionTypeRadioListener(final UserPrefs userPrefs) {
            this.userPrefs = userPrefs;
        }

        @Override
        public void onClick(View v) {

            final int id = v.getId();

            if (id != everybodyButton.getId()) {
                everybodyButton.setChecked(false);
            }

            if (id != preferredContactsButton.getId()) {
                preferredContactsButton.setChecked(false);
            }

            switch (id) {
                case R.id.everybody_button: {
                    userPrefs.setInteractionType(InteractionType.EVERYONE);
                    break;
                }
                case R.id.preferred_contacts_button: {
                    userPrefs.setInteractionType(InteractionType.SELECTED_CONTACTS_ONLY);
                    break;
                }
            }

            ((RadioButton) v).setChecked(true);
        }
    }
}
