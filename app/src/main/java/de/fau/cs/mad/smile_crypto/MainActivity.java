package de.fau.cs.mad.smile_crypto;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
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
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.Toast;

import org.spongycastle.operator.OperatorCreationException;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.recyclerview.internal.CardArrayRecyclerViewAdapter;
import it.gmariotti.cardslib.library.recyclerview.view.CardRecyclerView;

public class MainActivity extends ActionBarActivity {

    private CardArrayRecyclerViewAdapter mCardArrayAdapter;
    private KeyManagement keyManager;

    private ImageButton fab;

    private boolean expanded = false;

    private View fabAction1;
    private View fabAction2;

    private float offset1;
    private float offset2;

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

        /*getSupportFragmentManager().beginTransaction().
                replace(R.id.currentFragment, new ListOwnCertificatesFragment()).commit();*/

        ArrayList<Card> cards = new ArrayList<Card>();

        keyManager = new KeyManagement();

        //Staggered grid view
        CardRecyclerView gRecyclerView = (CardRecyclerView) this.findViewById(R.id.carddemo_recyclerview);
        gRecyclerView.setHasFixedSize(false);
        gRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCardArrayAdapter = new CardArrayRecyclerViewAdapter(this, cards);
        gRecyclerView.setAdapter(mCardArrayAdapter);

        updateCards();

        final ViewGroup fabContainer = (ViewGroup) this.findViewById(R.id.fab_container);
        fab = (ImageButton) this.findViewById(R.id.fab);
        fabAction1 = this.findViewById(R.id.fab_action_1);
        fabAction2 = this.findViewById(R.id.fab_action_2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expanded = !expanded;
                if (expanded) {
                    expandFab();
                } else {
                    collapseFab();
                }
            }
        });
        fabAction1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                collapseFab();
                expanded = false;
                Intent i = new Intent(v.getContext(), ImportCertificateActivity.class);
                startActivity(i);
                updateCards();
            }
        });

        fabAction2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                collapseFab();
                expanded = false;
                try {
                    new SelfSignedCertificateCreator().create();
                    updateCards();
                } catch (OperatorCreationException | IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException | InvalidKeySpecException | NoSuchProviderException e) {
                    Log.e(SMileCrypto.LOG_TAG, "Error while importing certificate: " + e.getMessage());
                    Toast.makeText(v.getContext(), R.string.error + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        fabContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                fabContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                offset1 = fab.getY() - fabAction1.getY();
                fabAction1.setTranslationY(offset1);
                offset2 = fab.getY() - fabAction2.getY();
                fabAction2.setTranslationY(offset2);
                return true;
            }
        });

        /*final ImageButton fab = (ImageButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent i = new Intent(MainActivity.this, ImportCertificateActivity.class);
                startActivity(i);*/
                /*fab.setSelected(!fab.isSelected());
                fab.setImageResource(fab.isSelected() ? R.drawable.animated_plus : R.drawable.animated_minus);
                Drawable drawable = fab.getDrawable();
                if (drawable instanceof Animatable) {
                    ((Animatable) drawable).start();
                }
            }
        });*/



        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources()));

        mTitles = new String[5];
        mTitles[0] = getResources().getString(R.string.navigation_drawer_import_certificate);
        mTitles[1] = getResources().getString(R.string.navigation_drawer_search);
        mTitles[2] = getResources().getString(R.string.navigation_drawer_info);
        mTitles[3] = getResources().getString(R.string.navigation_drawer_settings);
        mTitles[4] = getResources().getString(R.string.navigation_drawer_help);
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

                    Log.d(SMileCrypto.LOG_TAG, "Clicked on NavigationDrawerItem " + position + ": "
                            + title);

                    //switch not possible here :-(
                    if (title.equals(getResources().getString(R.string.toolbar_default_title))) {
                        /*getSupportFragmentManager().beginTransaction().
                                replace(R.id.currentFragment, new ListOwnCertificatesFragment()).commit();*/
                    } else if(title.equals(getResources().getString(R.string.navigation_drawer_import_certificate))) {
                        Intent i = new Intent(MainActivity.this, ImportCertificateActivity.class);
                        startActivity(i);
                        return true;
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
                        /*getSupportFragmentManager().beginTransaction().
                                replace(R.id.currentFragment, new SearchFragment()).commit();*/
                    }
                    toolbar.setTitle(title);
                    updateCards();
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
        /*getSupportFragmentManager().beginTransaction().
                replace(R.id.currentFragment, new ListOwnCertificatesFragment()).commit();*/
        updateCards();
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
            // TODO
            Toast.makeText(this, R.string.navigation_drawer_search,
                    Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.decrypt_local_mail) {
            Intent i = new Intent(MainActivity.this, DecryptLocalMailActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void collapseFab() {
        fab.setImageResource(R.drawable.animated_minus);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(createCollapseAnimator(fabAction1, offset1),
                createCollapseAnimator(fabAction2, offset2));
        animatorSet.start();
        animateFab();
    }

    private void expandFab() {
        fab.setImageResource(R.drawable.animated_plus);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(createExpandAnimator(fabAction1, offset1),
                createExpandAnimator(fabAction2, offset2));
        animatorSet.start();
        animateFab();
    }

    private static final String TRANSLATION_Y = "translationY";

    private Animator createCollapseAnimator(View view, float offset) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_Y, 0, offset)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private Animator createExpandAnimator(View view, float offset) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_Y, offset, 0)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private void animateFab() {
        Drawable drawable = fab.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }
    }

    private void updateCards() {
        for(KeyInfo ki : keyManager.getAllCertificates()) {

            //Create a Card
            KeyCard card = new KeyCard(this, ki);

            //Create a CardHeader
            CardHeader header = new CardHeader(this);
            //Add Header to card
            card.addCardHeader(header);
            boolean contains = false;
            for(int i = 0; i < mCardArrayAdapter.getItemCount(); ++i) {
                KeyCard kc = (KeyCard) mCardArrayAdapter.getItem(i);
                Log.e(SMileCrypto.LOG_TAG, "Testing item.");
                if(kc.equals(card)) {
                    Log.e(SMileCrypto.LOG_TAG, "Items are equal");
                    contains = true;
                }
            }
            if(!contains) {
                Log.e(SMileCrypto.LOG_TAG, "Items added");
                mCardArrayAdapter.add(card);
            }
        }
    }
}