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
package com.ksza.peggy.ui.contacts;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by karoly.szanto on 06/07/15.
 */
public class Contact implements Parcelable {

    private String name;
    private boolean selected;

    public Contact(String name) {
        this(name, false);
    }

    public Contact(String name, boolean selected) {
        this.name = name;
        this.selected = selected;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean equals(Object o) {

        if(! (o instanceof Contact)) {
            return false;
        }

        return name.equals(((Contact) o).name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, false);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {

        out.writeString(name);
        out.writeValue(selected);
    }

    public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    public Contact(final Parcel in) {

        name = in.readString();
        selected = (Boolean) in.readValue(Boolean.class.getClassLoader());
    }

    public Contact() {
    }
}
