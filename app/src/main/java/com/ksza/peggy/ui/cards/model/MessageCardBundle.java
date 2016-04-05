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
package com.ksza.peggy.ui.cards.model;

import java.io.Serializable;

/**
 * Information to be displayed in a message card:
 * <ul>
 *     <li>title string</li>
 *     <li>description string</li>
 *     <li>welcome message</li>
 * </ul>
 *
 * Created by karoly.szanto on 05/07/15.
 */
public class MessageCardBundle implements Serializable {

    private final String title;
    private final String description;
    private final String message;
    private final boolean newVersionMessage;

    public MessageCardBundle(final String title, final String description, final String message) {
        this(title, description, message, false);
    }

    public MessageCardBundle(final String title, final String description, final String message, final boolean newVersionMessage) {
        this.title = title;
        this.description = description;
        this.message = message;
        this.newVersionMessage = newVersionMessage;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public boolean isNewVersionMessage() {
        return newVersionMessage;
    }
}
