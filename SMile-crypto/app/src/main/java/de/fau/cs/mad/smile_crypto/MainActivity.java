package de.fau.cs.mad.smile_crypto;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import java.io.File;

public class MainActivity extends ActionBarActivity {

    // Name and email in HeaderView -- TODO: for SMile-UI -> get from resources
    String mName;
    String mEmail;
    //titles and icons for ListView
    int mIcons[] = {R.drawable.ic_search_black_24dp, R.drawable.ic_info_black_24dp,
            R.drawable.ic_settings_black_24dp, R.drawable.ic_help_black_24dp};
    String mTitles[];

    private Toolbar toolbar;
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    DrawerLayout mDrawer;
    ActionBarDrawerToggle mDrawerToggle;
    protected final int FAB_FILE_CHOOSER_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.currentFragment, new DefaultFragment());
        ft.commit();

        ImageButton fab = (ImageButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this, R.string.import_certificate_todo, Toast.LENGTH_SHORT).show();
                showFileChooser();
            }

            private void showFileChooser() {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*"); //TODO: only relevant file types
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                try {
                    startActivityForResult(Intent.createChooser(intent,
                                    getResources().getString(R.string.fab_select_import_certificate)),
                            FAB_FILE_CHOOSER_REQUEST_CODE);

                } catch (android.content.ActivityNotFoundException anfe) {
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.no_file_manager),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources()));

        mTitles = new String[4];
        mTitles[0] = getResources().getString(R.string.navigation_drawer_search);
        mTitles[1] = getResources().getString(R.string.navigation_drawer_info);
        mTitles[2] = getResources().getString(R.string.navigation_drawer_settings);
        mTitles[3] = getResources().getString(R.string.navigation_drawer_help);
        mName = getResources().getString(R.string.navigation_drawer_header_name);
        mEmail = getResources().getString(R.string.navigation_drawer_header_email_address);

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

                    //switch not possible here :-(
                    if (title.equals(getResources().getString(R.string.toolbar_default_title))) {
                        FragmentManager fm = getSupportFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        DefaultFragment defaultFragment = new DefaultFragment();
                        ft.replace(R.id.currentFragment, defaultFragment);
                    } else if (title.equals(getResources().getString(R.string.navigation_drawer_settings))) {
                        Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(i);
                        return true;
                    } else if (title.equals(getResources().getString(R.string.navigation_drawer_help))) {
                        Intent i = new Intent(MainActivity.this, HelpActivity.class);
                        startActivity(i);
                        return true;
                    } else if (title.equals(getResources().getString(R.string.navigation_drawer_info))) {
                        Intent i = new Intent(MainActivity.this, InfoActivity.class);
                        startActivity(i);
                        return true;
                    } else if (title.equals(getResources().getString(R.string.navigation_drawer_search))) {
                        FragmentManager fm = getSupportFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        SearchFragment searchFragment = new SearchFragment();
                        ft.replace(R.id.currentFragment, searchFragment);
                        ft.commit();
                    }
                    toolbar.setTitle(title);

                    // TODO: remove after testing
                    Toast.makeText(MainActivity.this, "Item " + position + ": " + title,
                            Toast.LENGTH_SHORT).show();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FAB_FILE_CHOOSER_REQUEST_CODE:
                //receive result from file manager (--> uri of certificate)
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    File myFile = new File(uri.getPath());
                    //TODO: remove toast
                    Toast.makeText(this, myFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    //TODO: store/import...
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
            // TODO
            Toast.makeText(this, R.string.navigation_drawer_search,
                    Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}