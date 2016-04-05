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
package com.ksza.peggy.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by karoly.szanto on 18/07/15.
 */
public class PeggyRecycler extends RecyclerView {

    private View emptyView;

    public PeggyRecycler(Context context) {
        super(context);
    }

    public PeggyRecycler(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PeggyRecycler(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);

        if (adapter != null) {
            adapter.registerAdapterDataObserver(emptyObserver);
        }

        emptyObserver.onChanged();
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
    }

    private AdapterDataObserver emptyObserver = new AdapterDataObserver() {

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            handleChange();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            handleChange();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            handleChange();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            handleChange();
        }

        @Override
        public void onChanged() {
            handleChange();
        }

        private void handleChange() {
            Adapter<?> adapter = getAdapter();
            if (adapter != null && emptyView != null) {
                if (adapter.getItemCount() == 0) {
                    emptyView.setVisibility(View.VISIBLE);
                    PeggyRecycler.this.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    PeggyRecycler.this.setVisibility(View.VISIBLE);
                }
            }
        }
    };

}
