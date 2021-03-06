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

import java.util.List;
import java.util.UUID;

/**
 * Created by karoly.szanto on 04/07/15.
 */
public interface CardContainer {

    void cardsFetched(final String fetchId, final List<CardModel> cardModels, boolean partialRefresh);

    void smsCountFetched(String fetchId, final long smsCountFetched);
}
