package de.fau.cs.mad.smile_crypto;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class ActionBarDrawerToggle extends android.support.v7.app.ActionBarDrawerToggle {

    private final NavigationDrawerFragment fragment;

    public ActionBarDrawerToggle(NavigationDrawerFragment fragment) {
        super(fragment.getActivity(), fragment.mDrawerLayout, new Toolbar(fragment.getActivity()), R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        this.fragment = fragment;
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        super.onDrawerClosed(drawerView);
        if (!fragment.isAdded()) {
            return;
        }

        fragment.getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        super.onDrawerOpened(drawerView);
        if (!fragment.isAdded()) {
            return;
        }

        if (!fragment.mUserLearnedDrawer) {
            // The user manually opened the drawer; store this flag to prevent auto-showing
            // the navigation drawer automatically in the future.
            fragment.mUserLearnedDrawer = true;
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(fragment.getActivity());
            sp.edit().putBoolean(NavigationDrawerFragment.PREF_USER_LEARNED_DRAWER, true).apply();
        }

        fragment.getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
    }

}
