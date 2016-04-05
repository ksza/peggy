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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.ksza.peggy.PeggyApplication;
import com.ksza.peggy.R;
import com.ksza.peggy.prefs.UserPrefs;
import com.ksza.peggy.prefs.UserPrefsManager;
import com.ksza.peggy.ui.cards.details.CardDetailsFragment;
import com.ksza.peggy.ui.cards.holders.message.DetailsFragmentCloseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.Bind;

/**
 * Created by karoly.szanto on 05/07/15.
 */
public class SelectContactsFragment extends CardDetailsFragment<UserPrefs> {

    private static final String ALL_CONTACTS_KEY = "ALL_CONTACTS_KEY";
    private static final String SELECTED_CONTACTS_KEY = "SELECTED_CONTACTS_KEY";

    @Bind(R.id.contacts_list)
    RecyclerView contactsList;

    @Bind(R.id.no_of_contacts)
    TextView noOfSelectedContacts;

    @Bind(R.id.search_view)
    SearchView searchView;

    @Bind(R.id.select_all_button)
    Button selectAllButton;

    @Bind(R.id.select_none_button)
    Button unselectAllButton;

    @Bind(R.id.done_action)
    View doneAction;

    private ContactsAdapter contactsAdapter;

    private ArrayList<Contact> selectedContacts = new ArrayList<>();
    private ArrayList<Contact> allContacts = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            allContacts = savedInstanceState.getParcelableArrayList(ALL_CONTACTS_KEY);
            selectedContacts = savedInstanceState.getParcelableArrayList(SELECTED_CONTACTS_KEY);

            for (Contact contact : allContacts) {
                if (selectedContacts.contains(contact)) {
                    contact.setSelected(true);
                }
            }
        } else {
            allContacts = ContactUtils.getContacts(PeggyApplication.getInstance());

            final Set<String> userContacts = getModel().getSelectedContact();
            for(Contact contact: allContacts) {
                if(userContacts.contains(contact.getName())) {
                    contact.setSelected(true);
                    selectedContacts.add(contact);
                }
            }

            /* used to check if a certain contact is in the list */
            final Contact checkerContact = new Contact();
            for(String contact: userContacts) {
                checkerContact.setName(contact);
                checkerContact.setSelected(true);

                if(!allContacts.contains(checkerContact)) {
                    allContacts.add(0, checkerContact);
                    selectedContacts.add(checkerContact);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putParcelableArrayList(ALL_CONTACTS_KEY, allContacts);
        outState.putParcelableArrayList(SELECTED_CONTACTS_KEY, selectedContacts);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected int getResourceId() {
        return R.layout.fragment_select_contacts;
    }

    @Override
    protected void onUiSetup(View view) {
        initContactsList();
        selectionChanged();
    }

    @Override
    protected void configureMenuItem(MenuItem menuItem) {
        menuItem.setIcon(R.drawable.ic_check_white_48dp);
        menuItem.setTitle(R.string.setup_card_done);
    }

    @Override
    protected void onMenuItemClicked(MenuItem menuItem) {
        final UserPrefs userPrefs = getModel();
        final Set<String> userContacts = userPrefs.getSelectedContact();

        userContacts.clear();
        for(Contact contact: selectedContacts) {
            userContacts.add(contact.getName());
        }

        UserPrefsManager.getInstance().updateUserPrefs(userPrefs);

        PeggyApplication
                .getInstance()
                .getBus()
                .post(new DetailsFragmentCloseEvent(true));
    }

    private void initContactsList() {

        final Context context = PeggyApplication.getInstance();

        contactsList.addItemDecoration(new MarginDecoration(context));
        contactsList.setHasFixedSize(true);
        contactsList.setLayoutManager(new GridLayoutManager(context, 2));

        contactsAdapter = new ContactsAdapter(new ArrayList<>(allContacts));
        contactsList.setAdapter(contactsAdapter);

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                contactsAdapter.stopFiltering();
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                contactsAdapter.filter(newText);
                return true;
            }
        });

        selectAllButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                for (Contact contact : allContacts) {
                    contact.setSelected(true);
                }
                selectedContacts.clear();
                selectedContacts.addAll(allContacts);

                contactsAdapter.notifyDataSetChanged();
                selectionChanged();
            }
        });

        unselectAllButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                for (Contact contact : allContacts) {
                    contact.setSelected(false);
                }

                selectedContacts.clear();

                contactsAdapter.notifyDataSetChanged();
                selectionChanged();
            }
        });

        doneAction.setVisibility(View.GONE);
    }

    private void selectionChanged() {

        noOfSelectedContacts.setText(getString(R.string.no_of_selected_contacts, selectedContacts.size(), allContacts.size()));
    }

    private class ContactsAdapter extends RecyclerView.Adapter<ContactHolder> implements View.OnClickListener {

        private List<Contact> contacts;

        public ContactsAdapter(List<Contact> contacts) {
            this.contacts = contacts;
        }

        @Override
        public void onClick(View view) {

            final Contact model = (Contact) view.getTag();
            final CheckBox box = (CheckBox) view;
            model.setSelected(box.isChecked());

            if (model.isSelected()) {
                selectedContacts.add(model);
            } else {
                selectedContacts.remove(model);
            }

            selectionChanged();
        }

        @Override
        public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            final View userPrefsCardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_holder_layout, parent, false);
            final ContactHolder viewHolder = new ContactHolder(userPrefsCardView, this);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ContactHolder holder, int position) {
            holder.map(contacts.get(position));
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }


        public void filter(final String filterText) {

            final String ignoreCaseFilterText = filterText.toLowerCase();
            contacts = new ArrayList<>();

            for (Contact contact : allContacts) {
                if (contact.getName().toLowerCase().contains(ignoreCaseFilterText)) {
                    contacts.add(contact);
                }
            }

//            final Iterator<Contact> iterator = contacts.iterator();
//            while (iterator.hasNext()) {
//                final Contact contact = iterator.next();
//                if(! contact.getName().contains(ignoreCaseFilterText)) {
//                    iterator.remove();
//                }
//            }

            notifyDataSetChanged();
        }

        public void stopFiltering() {
            this.contacts = allContacts;
            notifyDataSetChanged();
        }
    }
}
