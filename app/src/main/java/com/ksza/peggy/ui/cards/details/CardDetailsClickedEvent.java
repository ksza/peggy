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

import android.view.View;

import com.ksza.peggy.ui.cards.CardModel;

/**
 * Used by view holders to notify the activity that an action button
 * has been clicked.
 *
 * Created by karoly.szanto on 05/07/15.
 */
public class CardDetailsClickedEvent {

    private final CardModel cardModel;
    private final View actionTriggerView;
    private final CardDetailsType cardDetailsType;

    public CardDetailsClickedEvent(final CardModel cardModel, final CardDetailsType cardDetailsType) {
        this(cardModel, cardDetailsType, null);
    }

    public CardDetailsClickedEvent(final CardModel cardModel, final CardDetailsType cardDetailsType, final View actionTriggerView) {
        this.cardModel = cardModel;
        this.cardDetailsType = cardDetailsType;
        this.actionTriggerView = actionTriggerView;
    }

    public CardModel getCardModel() {
        return cardModel;
    }

    public CardDetailsType getCardDetailsType() {
        return cardDetailsType;
    }

    public View getActionTriggerView() {
        return actionTriggerView;
    }
}
