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
package com.ksza.peggy.ui.contacts;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;

import com.ksza.peggy.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by karoly.szanto on 05/07/15.
 */
public class ContactHolder extends RecyclerView.ViewHolder {

    @Bind((R.id.contact_checkbox))
    CheckBox contactCheckBox;

    public ContactHolder(View itemView, View.OnClickListener chkClickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        contactCheckBox.setOnClickListener(chkClickListener);
    }

    public void map(final Contact model) {

        contactCheckBox.setText(model.getName());
        contactCheckBox.setChecked(model.isSelected());
        contactCheckBox.setTag(model);
    }
}