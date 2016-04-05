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
package com.ksza.peggy.ui.cards.details;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ksza.peggy.R;

import java.io.Serializable;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Represent the details of a card in case the card is too small to show all data. It defines
 * a standard animation setup.
 *
 * Created by karoly.szanto on 06/07/15.
 */
public abstract class CardDetailsFragment<T extends Serializable> extends Fragment {

    private static final String MODEL_KEY = "MODEL_KEY";

    private T model;

    @Bind(R.id.fragment_top_toolbar)
    Toolbar toolbar;

    @Nullable
    @Bind(R.id.title)
    protected TextView title;

    @Nullable
    @Bind(R.id.subtitle)
    protected TextView subtitle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            model = (T) savedInstanceState.getSerializable(MODEL_KEY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putSerializable(MODEL_KEY, model);
        super.onSaveInstanceState(outState);
    }

    public void setModel(T model) {
        this.model = model;
    }

    protected T getModel() {
        return model;
    }

    protected abstract int getResourceId();
    protected abstract void onUiSetup(final View view);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(getResourceId(), container, false);

        ButterKnife.bind(this, view);

        onUiSetup(view);

        toolbar.inflateMenu(R.menu.details_fragment_menu);
        configureMenuItem(toolbar.getMenu().findItem(R.id.fragment_action));
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if(item.getItemId() == R.id.fragment_action) {
                    onMenuItemClicked(item);
                    return true;
                }

                return false;
            }
        });

        return view;
    }

    protected abstract void configureMenuItem(final MenuItem menuItem);
    protected abstract void onMenuItemClicked(final MenuItem menuItem);

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}