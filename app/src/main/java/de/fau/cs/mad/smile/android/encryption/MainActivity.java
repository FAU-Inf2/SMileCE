package de.fau.cs.mad.smile.android.encryption;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
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
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import de.fau.cs.mad.smile.android.encryption.R;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.recyclerview.internal.CardArrayRecyclerViewAdapter;
import it.gmariotti.cardslib.library.recyclerview.view.CardRecyclerView;

public class MainActivity extends ActionBarActivity {

    private CardArrayRecyclerViewAdapter mCardArrayAdapter;
    private KeyManagement keyManager;
    private ArrayList<Card> cards;

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

        cards = new ArrayList<Card>();

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

        updateCards();

        final ViewGroup fabContainer = (ViewGroup) this.findViewById(R.id.fab_container);
        fab = (ImageButton) this.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), ImportCertificateActivity.class);
                startActivity(i);
                updateCards();
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

    private void updateCards() {
        List<KeyInfo> kis =  keyManager.getAllCertificates();
        for(KeyInfo keyInfo : kis) {

            //Create a Card
            KeyCard card = new KeyCard(this, keyInfo);

            //Create a CardHeader
            CardHeader header = new CardHeader(this);
            //Add Header to card
            if(keyInfo.contact != null) {
                header.setTitle(keyInfo.contact);
            } else {
                header.setTitle("No contact information available.");
            }

            card.addCardHeader(header);
            boolean contains = false;

            for(int i = 0; i < mCardArrayAdapter.getItemCount(); ++i) {
                KeyCard kc = (KeyCard) mCardArrayAdapter.getItem(i);
                if(kc.equals(card)) {
                    contains = true;
                }
            }

            if(!contains) {
                Log.e(SMileCrypto.LOG_TAG, "Items added");
                card.setOnLongClickListener(new Card.OnLongCardClickListener() {

                    @Override
                    public boolean onLongClick(Card card, View view) {
                        if (!(card instanceof KeyCard)) {
                            return false;
                        }
                        final KeyCard kc = (KeyCard) card;
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                card.getContext());
                        if (kc.keyInfo.alias.startsWith("SMile_crypto_own")) {

                            // set title
                            alertDialogBuilder.setTitle(getString(R.string.alert_header_start) + kc.keyInfo.contact + getString(R.string.alert_header_end));

                            // set dialog message
                            alertDialogBuilder
                                    .setMessage(getString(R.string.alert_content))
                                    .setCancelable(false)
                                    .setPositiveButton(getString(R.string.erase), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // if this button is clicked, close
                                            // current activity
                                            keyManager.deleteKey(kc.keyInfo.alias);
                                            mCardArrayAdapter.remove(kc);
                                            mCardArrayAdapter.notifyDataSetChanged();
                                        }
                                    })
                                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // if this button is clicked, just close
                                            // the dialog box and do nothing
                                            dialog.cancel();
                                        }
                                    });

                            // create alert dialog
                            AlertDialog alertDialog = alertDialogBuilder.create();

                            // show it
                            alertDialog.show();
                        } else {
                            // set dialog message
                            alertDialogBuilder
                                    .setMessage(getString(R.string.alert_header_start) + kc.keyInfo.contact + getString(R.string.alert_header_end))
                                    .setCancelable(false)
                                    .setPositiveButton(getString(R.string.erase), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // if this button is clicked, close
                                            // current activity
                                            keyManager.deleteKey(kc.keyInfo.alias);
                                            mCardArrayAdapter.remove(kc);
                                            assert !mCardArrayAdapter.contains(kc);
                                            mCardArrayAdapter.notifyDataSetChanged();
                                        }
                                    })
                                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // if this button is clicked, just close
                                            // the dialog box and do nothing
                                            dialog.cancel();
                                        }
                                    });

                            // create alert dialog
                            AlertDialog alertDialog = alertDialogBuilder.create();

                            // show it
                            alertDialog.show();
                        }
                        return true;
                    }
                });

                card.setOnClickListener(new Card.OnCardClickListener() {
                    @Override
                    public void onClick(Card card, View view) {
                        if (!(card instanceof KeyCard)) {
                            return;
                        }
                        final KeyCard kc = (KeyCard) card;
                        Intent i = new Intent(MainActivity.this, DisplayCertificateInformationActivity.class);
                        i.putExtra("Alias", kc.keyInfo.alias);
                        startActivity(i);
                    }
                });

                mCardArrayAdapter.add(card);
            }
        }
        ArrayList<Card> toDelete = new ArrayList<>();
        for(Card c : cards) {
            if (!(c instanceof KeyCard)) {
                continue;
            }
            final KeyCard kc = (KeyCard) c;
            if(!kis.contains(kc.keyInfo)) {
                toDelete.add(c);
            }
        }
        cards.removeAll(toDelete);
        mCardArrayAdapter.notifyDataSetChanged();
    }
}