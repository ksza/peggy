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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ksza.peggy.PeggyApplication;
import com.ksza.peggy.R;
import com.ksza.peggy.ui.cards.CardModel;
import com.ksza.peggy.ui.cards.CardType;
import com.ksza.peggy.ui.cards.holders.BaseCardHolder;
import com.ksza.peggy.ui.cards.holders.LogCardHolder;
import com.ksza.peggy.ui.cards.holders.message.MessageCardHolder;
import com.ksza.peggy.ui.cards.holders.prefs.UserPrefHolder;
import com.ksza.peggy.ui.cards.holders.restricted.RestrictedEntryHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karoly.szanto on 02/07/15.
 */
public class PeggyAdapter extends RecyclerView.Adapter<BaseCardHolder> {

    private static final Logger logger = LoggerFactory.getLogger(PeggyAdapter.class);

    private final ArrayList<CardModel> cardModels;

    public PeggyAdapter() {
        this.cardModels = new ArrayList<>();
    }

    public PeggyAdapter(final ArrayList<CardModel> cardModels) {
        this.cardModels = cardModels;
    }

    @Override
    public BaseCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        final CardType cardType = CardType.values()[viewType];

        switch (cardType) {

            case USER_PREFS_SETUP: {

                final View userPrefsCardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_prefs_card_layout, parent, false);
                final UserPrefHolder viewHolder = new UserPrefHolder(userPrefsCardView);

                return viewHolder;
            }

            case LOG_ENTRY: {
                final View logCardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.log_card_layout, parent, false);
                final LogCardHolder viewHolder = new LogCardHolder(logCardView);

                return viewHolder;
            }

            case WELCOME: {
                final View messageCardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_card_layout, parent, false);
                final MessageCardHolder viewHolder = new MessageCardHolder(messageCardView);

                return viewHolder;
            }

            case RESTRICTED_LOG_ENTRY: {
                final View restrictedCardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.restricted_card_layout, parent, false);
                final RestrictedEntryHolder viewHolder = new RestrictedEntryHolder(restrictedCardView);

                return viewHolder;
            }
        }

        return null;
    }

    @Override
    public void onBindViewHolder(final BaseCardHolder holder, final int position) {
        holder.map(cardModels.get(position), position, PeggyApplication.getInstance());
    }

    @Override
    public int getItemCount() {
        return cardModels.size();
    }

    @Override
    public int getItemViewType(int position) {

        return cardModels.get(position).getCardType().ordinal();
    }

    public int add(final CardModel entry) {

        if (cardModels.size() > 0) {

            if(! cardModels.contains(entry)) {
                int index = cardModels.size();

                for (CardModel cardModel : cardModels) {

                    if (cardModel.getCardType() == CardType.LOG_ENTRY || cardModel.getCardType() == CardType.RESTRICTED_LOG_ENTRY) {
                    /* make sure we add the item after the priority ones */
                        index = cardModels.indexOf(cardModel);
                        break;
                    }
                }

                cardModels.add(index, entry);
                notifyItemInserted(index);

                return index;
            } else {
                return -1;
            }
        } else {
            cardModels.add(entry);
            notifyItemInserted(cardModels.size());

            return 0;
        }
    }

    public void addAll(final List<CardModel> entries, final boolean partialRefresh) {

        if (!partialRefresh) {
            cardModels.addAll(entries);
            notifyDataSetChanged();
        } else {

            final List<CardModel> delta = new ArrayList<>();

            /** compute the delta */
            for (CardModel model : entries) {
                if (!cardModels.contains(model)) {
                    delta.add(model);
                }
            }

            if (delta.size() > 0) {
                cardModels.addAll(0, delta);
                notifyDataSetChanged();
            } else {
                logger.debug("No new events!");
            }
        }
    }

    public void add(final int position, final CardModel model) {
        cardModels.add(position, model);
        notifyItemInserted(position);
    }

    public void remove(final CardModel model) {

        final int position = cardModels.indexOf(model);

        cardModels.remove(model);
        notifyItemRemoved(position);
    }

    public void remove(final int position) {

        cardModels.remove(position);
        notifyItemRemoved(position);
    }

    public CardModel getModelAt(final int position) {
        return cardModels.get(position);
    }

    public int positionByCardType(final CardType type) {

        int position = -1;

        for (CardModel model : cardModels) {
            if (model.getCardType() == type) {
                position = cardModels.indexOf(model);
                break;
            }
        }

        return position;
    }

    public void notifyModelChanged(final CardModel model) {

        final int indexOfModel = cardModels.indexOf(model);
        if (indexOfModel >= 0) {
            notifyItemChanged(indexOfModel);
        }
    }

    public ArrayList<CardModel> getCardModels() {
        return cardModels;
    }
}
