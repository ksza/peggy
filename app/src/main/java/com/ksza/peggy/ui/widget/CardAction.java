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
package com.ksza.peggy.ui.widget;

import android.content.Context;
import android.text.Spanned;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ksza.peggy.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by karoly.szanto on 04/07/15.
 */
public class CardAction extends LinearLayout {

    @Bind(R.id.cta_icon)
    ImageView actionIcon;

    @Bind(R.id.cta_description)
    TextView actionTitle;

    public CardAction(Context context) {
        super(context);
    }

    public CardAction(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CardAction(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setActionIcon(final int resourceId) {
        actionIcon.setImageResource(resourceId);
    }

    public void setActionTitle(final int resourceId, final Object... context) {
        actionTitle.setText(getContext().getString(resourceId, context));
    }

    public void setActionTitle(final String text) {
        actionTitle.setText(text);
    }

    public void setActionTitle(final Spanned text) {
        actionTitle.setText(text);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }
}
