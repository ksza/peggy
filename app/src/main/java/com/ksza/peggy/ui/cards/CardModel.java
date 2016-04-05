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
package com.ksza.peggy.ui.cards;

import com.ksza.peggy.storage.ActivityLogEntry;

import java.io.Serializable;

/**
 * Works as the base model for the adapter.
 *
 * Created by karoly.szanto on 04/07/15.
 */
public class CardModel implements Serializable {

    private CardType cardType;
    private Serializable dataBundle;

    public CardModel(final CardType cardType, final Serializable dataBundle) {
        this.cardType = cardType;
        this.dataBundle = dataBundle;
    }

    public CardType getCardType() {
        return cardType;
    }

    public Serializable getDataBundle() {
        return dataBundle;
    }

    @Override
    public boolean equals(Object o) {

        if(! (o instanceof CardModel)) {
            return false;
        }

        final CardModel newModel = (CardModel) o;

        if(newModel.cardType == CardType.USER_PREFS_SETUP && cardType == CardType.USER_PREFS_SETUP) {
            return true;
        }

        if(newModel.cardType == CardType.WELCOME && cardType == CardType.WELCOME) {
            return true;
        }

        if((newModel.getDataBundle() instanceof ActivityLogEntry) && (getDataBundle() instanceof ActivityLogEntry)) {
            return ((ActivityLogEntry) newModel.getDataBundle()).getId() == ((ActivityLogEntry) getDataBundle()).getId();
        }

        return false;
    }
}
