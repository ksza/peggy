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

import com.ksza.peggy.ui.cards.CardModel;
import com.ksza.peggy.ui.cards.SentSmsDetailsFragment;
import com.ksza.peggy.ui.cards.holders.message.MessageDetailsFragment;
import com.ksza.peggy.ui.cards.holders.prefs.ConfigureSmsFragment;
import com.ksza.peggy.ui.contacts.SelectContactsFragment;

/**
 * Created by karoly.szanto on 06/07/15.
 */
public class DetailsFragmentFactory {

    public static CardDetailsFragment createFragment(final CardModel model, final CardDetailsType cardDetailsType) {

        CardDetailsFragment result = null;

        switch (cardDetailsType) {

            case READ_ON: {
                result = new MessageDetailsFragment();
                break;
            }
            case SELECT_CONTACTS: {
                result = new SelectContactsFragment();
                break;
            }
            case CONFIGURE_SMS: {
                result = new ConfigureSmsFragment();
                break;
            }
            case VIEW_SMS: {
                result = new SentSmsDetailsFragment();
                break;
            }

        }

        result.setModel(model.getDataBundle());

        return result;
    }
}
