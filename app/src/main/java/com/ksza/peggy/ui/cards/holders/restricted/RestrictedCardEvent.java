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
package com.ksza.peggy.ui.cards.holders.restricted;

import com.ksza.peggy.ui.cards.CardModel;

/**
 * Created by karoly.szanto on 10/07/15.
 */
public class RestrictedCardEvent {

    public static final int ADD_ACTION = 1;
    public static final int CONFIGURE_ACTION = 2;

    private final CardModel model;
    private final int action;

    public RestrictedCardEvent(CardModel model, int action) {
        this.model = model;
        this.action = action;
    }

    public CardModel getModel() {
        return model;
    }

    public boolean isAddAction() {
        return action == ADD_ACTION;
    }

    public boolean isConfigureAction() {
        return action == CONFIGURE_ACTION;
    }
}
