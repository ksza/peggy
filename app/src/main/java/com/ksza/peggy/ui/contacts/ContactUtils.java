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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by karoly.szanto on 05/07/15.
 */
public class ContactUtils {

    public static ArrayList<Contact> getContacts(final Context context) {

        /* make sure we remove duplicate by using a set */
        final Set<Contact> contactsSet = new HashSet<>();

        final ContentResolver contentResolver = context.getContentResolver();
        final Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {

                /* we only care for contacts with phone numbers, for now */
                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

                    final String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    final String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    final Contact newContact = new Contact(name);
                    if(! contactsSet.contains(newContact)) {
                        contactsSet.add(new Contact(name));
                    }

//                    final Cursor phoneNoCursor = contentResolver.query(
//                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                            null,
//                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
//                            new String[]{id}, null
//                    );
//
//                    while (phoneNoCursor.moveToNext()) {
//                        final String phoneNo = phoneNoCursor.getString(phoneNoCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                    }
//                    phoneNoCursor.close();
                }
            }
        }

        final ArrayList<Contact> result = new ArrayList<>(contactsSet);
        Collections.sort(result, new Comparator<Contact>() {
            @Override
            public int compare(Contact lhs, Contact rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });

        return result;
    }
}
