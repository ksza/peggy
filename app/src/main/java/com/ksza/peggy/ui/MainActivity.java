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
package com.ksza.peggy.ui;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ksza.peggy.PeggyApplication;
import com.ksza.peggy.R;
import com.ksza.peggy.prefs.AppPref;
import com.ksza.peggy.prefs.AppPrefsManager;
import com.ksza.peggy.prefs.UserPrefs;
import com.ksza.peggy.prefs.UserPrefsManager;
import com.ksza.peggy.storage.ActivityLogAddedEvent;
import com.ksza.peggy.storage.ActivityLogEntry;
import com.ksza.peggy.storage.CallStatus;
import com.ksza.peggy.storage.PeggyAction;
import com.ksza.peggy.ui.cards.CardContainer;
import com.ksza.peggy.ui.cards.CardModel;
import com.ksza.peggy.ui.cards.CardType;
import com.ksza.peggy.ui.cards.CardsFetcher;
import com.ksza.peggy.ui.cards.LogCardModelFactory;
import com.ksza.peggy.ui.cards.RemoveCardEvent;
import com.ksza.peggy.ui.cards.StatsSmsCountFetcher;
import com.ksza.peggy.ui.cards.details.CardDetailsClickedEvent;
import com.ksza.peggy.ui.cards.details.CardDetailsFragment;
import com.ksza.peggy.ui.cards.details.DetailsFragmentFactory;
import com.ksza.peggy.ui.cards.holders.message.DetailsFragmentCloseEvent;
import com.ksza.peggy.ui.cards.holders.message.MessageBundleFactory;
import com.ksza.peggy.ui.cards.holders.restricted.RestrictedCardEvent;
import com.ksza.peggy.ui.cards.model.MessageCardBundle;
import com.ksza.peggy.util.Analytics;
import com.ksza.peggy.util.UiUtil;
import com.ksza.peggy.util.Util;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.squareup.otto.Subscribe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.OvershootInRightAnimator;

/**
 * Created by karoly.szanto on 27/06/15.
 */
public class MainActivity extends AppCompatActivity implements CardContainer {

    private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);
    private static final String DETAILS_FRAGMENT_TAG = "DETAILS_FRAGMENT_TAG";

    private static final String CARDS_LIST_KEY = "CARDS_LIST_KEY";

    private String fetchId;

    @Bind(R.id.activity_log_list)
    PeggyRecycler cardList;

    @Bind(R.id.main_toolbar)
    Toolbar toolbar;

    @Bind(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;

    @Bind(R.id.start_stop_peggy)
    ImageButton startStopPeggyFab;

    @Bind(R.id.title)
    TextView title;

    @Bind(R.id.subtitle)
    TextView subtitle;

    @Bind(R.id.empty_recycler)
    ImageView emptyRecycler;

    private AppPrefsManager appPrefsManager = AppPrefsManager.getInstance();

    private LinearLayoutManager cardListLayoutManager;

    private PeggyAdapter peggyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity_layout);

        ButterKnife.bind(this);

        fetchId = UUID.randomUUID().toString();

        toolbar.setLogo(R.drawable.peggy_logo);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);

        updateTitleMessages();
        updateStartStopButton();

        ArrayList<CardModel> savedModels = new ArrayList<>();
        if(savedInstanceState != null && savedInstanceState.containsKey(CARDS_LIST_KEY)) {
            savedModels = (ArrayList<CardModel>) savedInstanceState.get(CARDS_LIST_KEY);
        }

        setUpLogList(savedModels);

        startStopPeggyFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final AppPref appPref = AppPrefsManager.getInstance().getAppPrefs();
                appPref.togglePeggyEnabled();
                AppPrefsManager.getInstance().updateAppPrefs(appPref);

                updateStartStopButton();
                updateTitleMessages();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(CARDS_LIST_KEY, peggyAdapter.getCardModels());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.credits: {
                new LibsBuilder()
                        .withActivityTheme(R.style.AppTheme)
                        .start(this);

                return true;
            }
            case R.id.settings: {
                int settingsCardPosition = peggyAdapter.positionByCardType(CardType.USER_PREFS_SETUP);
                if (settingsCardPosition == -1) {
                    settingsCardPosition = 0;
                    peggyAdapter.add(settingsCardPosition, new CardModel(CardType.USER_PREFS_SETUP, UserPrefsManager.getInstance().getUserPrefs()));
                }
                cardList.scrollToPosition(settingsCardPosition);

                return true;
            }
            case R.id.welcome: {
                int settingsCardPosition = peggyAdapter.positionByCardType(CardType.WELCOME);
                if (settingsCardPosition == -1) {
                    settingsCardPosition = 0;
                    final MessageCardBundle welcomeMessageBundle = MessageBundleFactory.createMessageBundle(this, false);
                    peggyAdapter.add(settingsCardPosition, new CardModel(CardType.WELCOME, welcomeMessageBundle));
                }
                cardList.scrollToPosition(settingsCardPosition);

                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateStartStopButton() {

        final AppPref appPref = AppPrefsManager.getInstance().getAppPrefs();

        int drawable;
        int titleId;
        int colorId;

        if (!appPref.isPeggyEnabled()) {
            drawable = R.drawable.ic_play_arrow_white_36dp;
            titleId = R.string.enable_peggy;
            colorId = R.color.primary;
        } else {
            drawable = R.drawable.ic_stop_white_36dp;
            titleId = R.string.disable_peggy;
            colorId = R.color.red;
        }

        startStopPeggyFab.setImageDrawable(UiUtil.setIconTint(drawable, colorId, this));
    }

    private void updateTitleMessages() {

        final AppPref appPref = appPrefsManager.getAppPrefs();
        if (appPref.isPeggyEnabled()) {
            title.setText(R.string.peggy_enabled_title);
            subtitle.setText(" ");

            new StatsSmsCountFetcher(fetchId, this).execute();
        } else {
            title.setText(R.string.peggy_disabled_title);
            subtitle.setText(R.string.peggy_disabled_subtitle);
        }
    }

    private void setUpLogList(final ArrayList<CardModel> savedModels) {
        /* true would make for a better performance */
        cardList.setHasFixedSize(false);

        // use a linear layout manager
        cardListLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        final ItemTouchHelper swipeToDismissTouchHelper = new ItemTouchHelper(new SwipeToDismissTouchListener());
        swipeToDismissTouchHelper.attachToRecyclerView(cardList);
        cardList.setLayoutManager(cardListLayoutManager);
        cardList.setEmptyView(emptyRecycler);

        cardList.setItemAnimator(new OvershootInRightAnimator());

        peggyAdapter = new PeggyAdapter();
        cardList.setAdapter(peggyAdapter);

        if(! savedModels.isEmpty()) {
            peggyAdapter.addAll(savedModels, false);
        }

        emptyRecycler.setImageDrawable(UiUtil.setIconTint(R.drawable.peggy_empty, R.color.secondary_text, this));
    }

    @Override
    protected void onResume() {
        super.onResume();

        /** if the adapter is not empty, just check if there are any new events */
        final boolean partialRefresh = ! peggyAdapter.getCardModels().isEmpty();
        new CardsFetcher(fetchId, this, partialRefresh).execute();
    }

    @Subscribe
    public void onActivityLogged(final ActivityLogAddedEvent event) {

        final ActivityLogEntry entry = event.getEntry();

        if(entry.getCallStatus() == CallStatus.MISSED && (entry.getPeggyAction() == PeggyAction.SENT_SMS || entry.getPeggyAction() == PeggyAction.RESTRICTED_INTERACTION ||
                entry.getPeggyAction() == PeggyAction.ACTIVITY_NOT_OF_INTEREST || entry.getPeggyAction() == PeggyAction.DUPLICATE_REPLIES)) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    logger.debug("New activity logged!");

                    if (peggyAdapter != null) {

                        final CardModel model = LogCardModelFactory.create(event.getEntry());
                        final int insertedAt = peggyAdapter.add(model);
                        if(insertedAt >= 0) {
                            cardList.scrollToPosition(insertedAt);
                        }
                    }

                    new StatsSmsCountFetcher(fetchId, MainActivity.this).execute();
                }
            });
        }
    }

    @Subscribe
    public void onRemoveCard(final RemoveCardEvent event) {

        peggyAdapter.remove(event.getCardModel());
        if (event.getCardModel().getCardType() == CardType.WELCOME) {

            updateStartStopButton();
            updateTitleMessages();

            if (!appPrefsManager.getAppPrefs().isSettingsDone()) {
                final CardModel settingsModel = new CardModel(CardType.USER_PREFS_SETUP, UserPrefsManager.getInstance().getUserPrefs());
                peggyAdapter.add(0, settingsModel);
            }
        } else if (event.getCardModel().getCardType() == CardType.USER_PREFS_SETUP) {
            Snackbar
                    .make(coordinatorLayout, R.string.setup_card_snack_text, Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onBackPressed() {

        if (!removeDetailsFragment()) {
            super.onBackPressed();
        }
    }

    @Subscribe
    public void onRestrictedCardEvent(final RestrictedCardEvent event) {

        if (event.isAddAction()) {

            final ActivityLogEntry entry = (ActivityLogEntry) event.getModel().getDataBundle();

            final int settingsItemPosition = peggyAdapter.positionByCardType(CardType.USER_PREFS_SETUP);
            if (settingsItemPosition >= 0) {
                peggyAdapter.notifyItemChanged(settingsItemPosition);
            }
            peggyAdapter.notifyModelChanged(event.getModel());

            Snackbar
                    .make(coordinatorLayout, getString(R.string.card_entry_action_contact_added, entry.getDisplayName()), Snackbar.LENGTH_SHORT)
                    .setAction(R.string.entry_undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            final UserPrefs userPrefs = UserPrefsManager.getInstance().getUserPrefs();
                            userPrefs.getSelectedContact().remove(entry.getDisplayName());

                            UserPrefsManager.getInstance().updateUserPrefs(userPrefs);

                            final int settingsItemPosition = peggyAdapter.positionByCardType(CardType.USER_PREFS_SETUP);
                            if (settingsItemPosition >= 0) {
                                peggyAdapter.notifyItemChanged(settingsItemPosition);
                            }
                            peggyAdapter.notifyModelChanged(event.getModel());
                        }
                    }).show();
        } else if (event.isConfigureAction()) {

            int settingsCardPosition = peggyAdapter.positionByCardType(CardType.USER_PREFS_SETUP);
            if (settingsCardPosition == -1) {
                settingsCardPosition = 0;
                peggyAdapter.add(settingsCardPosition, new CardModel(CardType.USER_PREFS_SETUP, UserPrefsManager.getInstance().getUserPrefs()));
            }
            cardList.scrollToPosition(settingsCardPosition);
        }
    }

    @Subscribe
    public void onDetailsFragmentClose(final DetailsFragmentCloseEvent event) {

        if (removeDetailsFragment() && event.isModelChanged()) {
            peggyAdapter.notifyDataSetChanged();
        }
    }

    private boolean removeDetailsFragment() {

        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(DETAILS_FRAGMENT_TAG);

        if (fragment != null && fragment.isVisible()) {

            final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(0, android.R.anim.fade_out).remove(fragment).commit();

            return true;
        }

        return false;
    }

    @Subscribe
    public void onCardDetailsClicked(final CardDetailsClickedEvent event) {

        final CardModel cardModel = event.getCardModel();
        final CardDetailsFragment cardDetailsFragment = DetailsFragmentFactory.createFragment(cardModel, event.getCardDetailsType());

        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, 0).add(R.id.details_container, cardDetailsFragment, DETAILS_FRAGMENT_TAG).commit();
    }

    @Override
    protected void onStart() {
        super.onStart();

        PeggyApplication.getInstance().getBus().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        PeggyApplication.getInstance().getBus().unregister(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        deleteMarkedEntries();
    }

    private void deleteMarkedEntries() {
        for(ActivityLogEntry entry: markedForDelete) {
            entry.delete();
        }
    }

    @Override
    public void cardsFetched(String fetchId, List<CardModel> cardModels, boolean partialRefresh) {

        if (this.fetchId != null && this.fetchId.equals(fetchId)) {
            logger.debug("Entries fetched!");
            peggyAdapter.addAll(cardModels, partialRefresh);
        }
    }

    @Override
    public void smsCountFetched(String fetchId, long smsCountFetched) {

        if (this.fetchId != null && this.fetchId.equals(fetchId)) {

            if (appPrefsManager.getAppPrefs().isPeggyEnabled()) {
                final String noSmsSent = getString(R.string.peggy_enabled_subtitle, smsCountFetched);
                subtitle.setText(noSmsSent);
            }
        }
    }

    private final Set<ActivityLogEntry> markedForDelete = new HashSet<>();
    private class SwipeToDismissTouchListener extends ItemTouchHelper.SimpleCallback {

        public SwipeToDismissTouchListener() {
            super(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {

            final int itemPosition = viewHolder.getAdapterPosition();
            final CardModel model = peggyAdapter.getModelAt(itemPosition);
            if(model.getDataBundle() instanceof ActivityLogEntry) {
                markedForDelete.add((ActivityLogEntry) model.getDataBundle());
            }

            Util.createEasyTrackerEvent(Analytics.UI_CATEGORY, Analytics.SWIPED_ACTION, "swiped card");

            final Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, R.string.entry_deleted, Snackbar.LENGTH_LONG)
                    .setAction(R.string.entry_undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Util.createEasyTrackerEvent(Analytics.UI_CATEGORY, Analytics.UNDO_SWIPE_ACTION, "undo swipe card");

                            logger.debug("Undo swipe {}", itemPosition);
                            markedForDelete.remove(model.getDataBundle());

                            peggyAdapter.add(itemPosition, model);
                            cardList.scrollToPosition(itemPosition);
                        }
                    });

            peggyAdapter.remove(viewHolder.getAdapterPosition());
            snackbar.show();
        }
    }
}
