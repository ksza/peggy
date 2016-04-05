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
import com.ksza.peggy.storage.PeggyAction;

/**
 * Create log card model instances
 *
 * Created by karoly.szanto on 07/07/15.
 */
public class LogCardModelFactory {

    public static CardModel create(final ActivityLogEntry entry) {

        if(entry.getPeggyAction() == PeggyAction.RESTRICTED_INTERACTION) {
            return new CardModel(CardType.RESTRICTED_LOG_ENTRY, entry);
        } else {
            return new CardModel(CardType.LOG_ENTRY, entry);
        }
    }
}
