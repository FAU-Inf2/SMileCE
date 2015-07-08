package de.fau.cs.mad.smile_crypto;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private static final String STATE_SECTION = "section";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private CharSequence mTitle;

    private int mSection = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (savedInstanceState != null) {
            mSection = savedInstanceState.getInt(STATE_SECTION, 0);
        }
        setContentView(R.layout.activity_main);
        onSectionAttached(mSection);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SECTION, mSection);
        // TODO
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // TODO
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag("tag_fragment_" + position);
        if (fragment == null) {
            switch (position) {
                case 0:
                    fragment = PlaceholderFragment.newInstance(position + 1);
                    break;
                case 1:
                    fragment = PlaceholderFragment.newInstance(position + 1);
                    break;
                case 2:
                    fragment = PlaceholderFragment.newInstance(position + 1);
                    break;
                default:
                    fragment = PlaceholderFragment.newInstance(position + 1);
                    break;
            }
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment, "tag_fragment_" + position)
                .commit();
    }

    public void onSectionAttached(int number) {
        mSection = number;
        switch (number) {
            case 1:
                mTitle = getString(R.string.key_management); // this is displayed as title in the ActionBar
                break;
            case 2:
                mTitle = getString(R.string.second_element);
                break;
            case 3:
                mTitle = getString(R.string.hello_world);
                break;
            default:
                mTitle = getTitle();
                break;
        }
    }

    void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }
}
