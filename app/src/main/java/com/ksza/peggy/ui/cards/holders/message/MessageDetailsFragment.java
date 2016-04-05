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

import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ksza.peggy.PeggyApplication;
import com.ksza.peggy.R;
import com.ksza.peggy.ui.cards.details.CardDetailsFragment;
import com.ksza.peggy.ui.cards.model.MessageCardBundle;

import butterknife.Bind;

/**
 * Detailed view of the message card model
 * <p/>
 * Created by karoly.szanto on 05/07/15.
 */
public class MessageDetailsFragment extends CardDetailsFragment<MessageCardBundle> {

    @Bind(R.id.message_container)
    TextView messageContainer;

    @Override
    protected int getResourceId() {
        return R.layout.fragment_message_details;
    }

    @Override
    protected void onUiSetup(View view) {

        final MessageCardBundle bundle = getModel();

        title.setText(bundle.getTitle());
        subtitle.setText(bundle.getDescription());

        messageContainer.setText(Html.fromHtml(bundle.getMessage(), null, new WelcomeMessageTagHandler()));
    }

    @Override
    protected void configureMenuItem(MenuItem menuItem) {
        menuItem.setIcon(R.drawable.ic_close_white_48dp);
        menuItem.setTitle(R.string.close_menu_entry);
    }

    @Override
    protected void onMenuItemClicked(MenuItem menuItem) {
        PeggyApplication
                .getInstance()
                .getBus()
                .post(new DetailsFragmentCloseEvent());
    }
}
