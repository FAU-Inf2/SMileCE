package de.fau.cs.mad.smile.android.encryption;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.recyclerview.internal.CardArrayRecyclerViewAdapter;
import it.gmariotti.cardslib.library.recyclerview.view.CardRecyclerView;

public class MainActivity extends ActionBarActivity {

    private CardArrayRecyclerViewAdapter mCardArrayAdapter;
    private KeyManagement keyManager;
    private ArrayList<Card> cards;

    private CardListUpdater updater;

    private ImageButton fab;

    // Name and email in HeaderView -- TODO: for SMile-UI -> get from resources
    String mName;
    String mEmail;
    //titles and icons for ListView
    int mIcons[] = {R.drawable.ic_add_black_24dp, R.drawable.ic_search_black_24dp,
            R.drawable.ic_info_black_24dp, R.drawable.ic_settings_black_24dp,
            R.drawable.ic_help_black_24dp};
    String mTitles[];

    private Toolbar toolbar;
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    DrawerLayout mDrawer;
    ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.toolbar_default_title);
        setSupportActionBar(toolbar);

        cards = new ArrayList<>();

        try {
            keyManager = new KeyManagement();
        } catch (KeyStoreException e) { // TODO: display error message and die
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }

        //Staggered grid view
        CardRecyclerView gRecyclerView = (CardRecyclerView) this.findViewById(R.id.carddemo_recyclerview);
        gRecyclerView.setHasFixedSize(false);
        gRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCardArrayAdapter = new CardArrayRecyclerViewAdapter(this, cards);
        gRecyclerView.setAdapter(mCardArrayAdapter);
        updater = new CardListUpdater(keyManager, this, mCardArrayAdapter, cards);

        updater.updateCards();
        registerForContextMenu(gRecyclerView);

        final ViewGroup fabContainer = (ViewGroup) this.findViewById(R.id.fab_container);
        fab = (ImageButton) this.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), ImportCertificateActivity.class);
                startActivity(i);
                updater.updateCards();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources()));

        mTitles = new String[5];
        mTitles[0] = getResources().getString(R.string.navigation_drawer_import_certificate);
        mTitles[1] = getResources().getString(R.string.navigation_drawer_search);
        mTitles[2] = getResources().getString(R.string.navigation_drawer_info);
        mTitles[3] = getResources().getString(R.string.navigation_drawer_settings);
        mTitles[4] = getResources().getString(R.string.navigation_drawer_help);

        ArrayList<KeyInfo> ownCertificates = new ArrayList<>();

        try {
            ownCertificates = new KeyManagement().getOwnCertificates();
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error: " + e.getMessage());
        }

        int i = 0;
        while(mName == null && i < ownCertificates.size()) { // use first certificate with name set
            KeyInfo keyInfo = ownCertificates.get(i);
            mName = keyInfo.contact;
            Log.d(SMileCrypto.LOG_TAG, "mName: " + mName);
            mEmail = keyInfo.mail;
            Log.d(SMileCrypto.LOG_TAG, "mEmail: " + mEmail);
            i++;
        }

        if(mName == null && mEmail == null) {
            mName = getResources().getString(R.string.navigation_drawer_header_name);
            mEmail = getResources().getString(R.string.navigation_drawer_header_email_address);
        } else if(mName == null) {
            mName = "";
        } else if(mEmail == null) {
            mEmail = null;
        }

        mAdapter = new RecyclerViewAdapter(mTitles, mIcons, mName, mEmail);
        mRecyclerView.setAdapter(mAdapter);

        final GestureDetector mGestureDetector = new GestureDetector(MainActivity.this,
                new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());

                if (child != null && mGestureDetector.onTouchEvent(motionEvent)) {
                    mDrawer.closeDrawers();

                    int position = recyclerView.getChildPosition(child);
                    String title;
                    if (position == 0)
                        title = getResources().getString(R.string.toolbar_default_title);
                    else
                        title = mTitles[position - 1];

                    Log.d(SMileCrypto.LOG_TAG, "Clicked on NavigationDrawerItem " + position + ": "
                            + title);

                    //switch not possible here :-(
                    if (title.equals(getResources().getString(R.string.toolbar_default_title))) {
                    } else if(title.equals(getResources().getString(R.string.navigation_drawer_import_certificate))) {
                        Intent i = new Intent(MainActivity.this, ImportCertificateActivity.class);
                        startActivity(i);
                    } else if (title.equals(getResources().getString(R.string.navigation_drawer_settings))) {
                        Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(i);
                    } else if (title.equals(getResources().getString(R.string.navigation_drawer_help))) {
                        Intent i = new Intent(MainActivity.this, HelpActivity.class);
                        startActivity(i);
                    } else if (title.equals(getResources().getString(R.string.navigation_drawer_info))) {
                        Intent i = new Intent(MainActivity.this, InfoActivity.class);
                        startActivity(i);
                    } else if (title.equals(getResources().getString(R.string.navigation_drawer_search))) {
                        Intent i = new Intent(MainActivity.this, SearchActivity.class);
                        startActivity(i);
                        updater.updateCards();
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
            }
        });

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mDrawer = (DrawerLayout) findViewById(R.id.DrawerLayout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.hello_world,
                R.string.hello_world){ //TODO: set correct strings
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mDrawer.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onResume() {
        updater.updateCards();
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Items in Toolbar/ActionBar
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.action_search) {
            Intent i = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.decrypt_local_mail) {
            Intent i = new Intent(MainActivity.this, DecryptLocalMailActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}