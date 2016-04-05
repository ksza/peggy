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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ksza.peggy.PeggyApplication;
import com.ksza.peggy.R;
import com.ksza.peggy.prefs.UserPrefs;
import com.ksza.peggy.prefs.UserPrefsManager;
import com.ksza.peggy.storage.ActivityStatus;
import com.ksza.peggy.ui.cards.details.CardDetailsFragment;
import com.ksza.peggy.ui.cards.holders.message.DetailsFragmentCloseEvent;

import butterknife.Bind;

/**
 * Created by karoly.szanto on 10/07/15.
 */
public class ConfigureSmsFragment extends CardDetailsFragment<UserPrefs> {

    @Bind(R.id.driving_sms_label)
    TextView drivingSmsLabel;
    @Bind(R.id.driving_sms_edit)
    EditText drivingSmsEdit;
    @Bind(R.id.driving_default)
    Button drivingDefault;

    @Bind(R.id.biking_sms_label)
    TextView bikingSmsLabel;
    @Bind(R.id.biking_sms_edit)
    EditText bikingSmsEdit;
    @Bind(R.id.biking_default)
    Button bikingDefault;

    @Bind(R.id.running_sms_label)
    TextView runningSmsLabel;
    @Bind(R.id.running_sms_edit)
    EditText runningSmsEdit;
    @Bind(R.id.running_default)
    Button runningDefault;

    @Override
    protected int getResourceId() {
        return R.layout.fragment_configure_sms;
    }

    @Override
    protected void onUiSetup(View view) {

        title.setText(R.string.fragment_configure_sms_title);
        subtitle.setText(R.string.fragment_configure_sms_detail);

        drivingSmsEdit.addTextChangedListener(new SmsEditListener(drivingSmsLabel, ActivityStatus.DRIVING));
        drivingSmsEdit.setText(getModel().getDrivingSms(), TextView.BufferType.EDITABLE);
        drivingDefault.setOnClickListener(new DefaultClickListener(drivingSmsEdit, ActivityStatus.DRIVING));

        bikingSmsEdit.addTextChangedListener(new SmsEditListener(bikingSmsLabel, ActivityStatus.BIKING));
        bikingSmsEdit.setText(getModel().getBikingSms(), TextView.BufferType.EDITABLE);
        bikingDefault.setOnClickListener(new DefaultClickListener(bikingSmsEdit, ActivityStatus.BIKING));

        runningSmsEdit.addTextChangedListener(new SmsEditListener(runningSmsLabel, ActivityStatus.RUNNING));
        runningSmsEdit.setText(getModel().getRunningSms(), TextView.BufferType.EDITABLE);
        runningDefault.setOnClickListener(new DefaultClickListener(runningSmsEdit, ActivityStatus.RUNNING));
    }

    @Override
    protected void configureMenuItem(MenuItem menuItem) {
        menuItem.setIcon(R.drawable.ic_check_white_48dp);
        menuItem.setTitle(R.string.setup_card_done);
    }

    @Override
    protected void onMenuItemClicked(MenuItem menuItem) {
        final View currentlyFocusedView = getActivity().getCurrentFocus();
        if(currentlyFocusedView != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentlyFocusedView.getWindowToken(), 0);
        }

        final UserPrefs userPrefs = getModel();

        userPrefs.setBikingSms(bikingSmsEdit.getText().toString());
        userPrefs.setDrivingSms(drivingSmsEdit.getText().toString());
        userPrefs.setRunningSms(runningSmsEdit.getText().toString());

        UserPrefsManager.getInstance().updateUserPrefs(userPrefs);

        PeggyApplication
                .getInstance()
                .getBus()
                .post(new DetailsFragmentCloseEvent(true));
    }

    private class SmsEditListener implements TextWatcher {

        private final TextView label;
        private final ActivityStatus status;
        private final int maxSmsSize = getResources().getInteger(R.integer.max_sms_size);

        SmsEditListener(final TextView label, final ActivityStatus status) {

            this.label = label;
            this.status = status;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            label.setText(getString(R.string.fragment_configure_sms_label, status.getStatusName(), s.length(), maxSmsSize));
        }
    }

    private class DefaultClickListener implements View.OnClickListener {

        private final EditText editText;
        private final ActivityStatus status;

        DefaultClickListener(final EditText editText, final ActivityStatus status) {

            this.editText = editText;
            this.status = status;
        }

        @Override
        public void onClick(View v) {
            editText.setText(getString(R.string.peggy_sms_activity, status.getStatusName()), TextView.BufferType.EDITABLE);
        }
    }
}
