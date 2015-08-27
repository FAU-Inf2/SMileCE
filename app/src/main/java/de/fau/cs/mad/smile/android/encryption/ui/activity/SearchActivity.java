package de.fau.cs.mad.smile.android.encryption.ui.activity;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import de.fau.cs.mad.smile.android.encryption.ui.adapter.KeyAdapter;
import de.fau.cs.mad.smile.android.encryption.KeyInfo;
import de.fau.cs.mad.smile.android.encryption.KeyManagement;
import de.fau.cs.mad.smile.android.encryption.R;
import de.fau.cs.mad.smile.android.encryption.SMileCrypto;

public class SearchActivity extends ActionBarActivity {
    private KeyAdapter adapter;
    private KeyManagement keyManager;

    private ArrayList<KeyInfo> cards;

    private Toolbar toolbar;
    private String searchQuery;
    private EditText searchEt;
    private ArrayList<KeyInfo> cardsFiltered;
    private String edText;

    private static String STATE_SEARCH = "searchquery";
    private static String STATE_EDTEXT = "edtext";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        toolbar.setTitle(R.string.title_activity_search);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(STATE_SEARCH);
            edText = savedInstanceState.getString(STATE_EDTEXT);
        } else {
            searchQuery = "";
            edText = "";
        }

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

        cards = keyManager.getAllCertificates();

        //Staggered grid view
        RecyclerView gRecyclerView = (RecyclerView) this.findViewById(R.id.card_list);
        gRecyclerView.setHasFixedSize(false);
        gRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new KeyAdapter(this, cards);
        //adapter.addKey(cards);
        gRecyclerView.setAdapter(adapter);

        searchEt = (EditText) toolbar.findViewById(R.id.search_bar);
        searchEt.addTextChangedListener(new SearchWatcher());
        searchEt.setText(edText);
        searchEt.requestFocus();

        ImageView image = (ImageView) toolbar.findViewById(R.id.remove_search);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEt.setText("");
                adapter.addKey(keyManager.getAllCertificates());
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(STATE_SEARCH, searchQuery);
        savedInstanceState.putString(STATE_EDTEXT, edText);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        adapter.addKey(keyManager.getAllCertificates());
        super.onResume();
    }

    private class SearchWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence c, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence c, int i, int i2, int i3) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            searchQuery = searchEt.getText().toString();
            Log.d(SMileCrypto.LOG_TAG, "Search for: " + searchQuery);
            if (!searchQuery.equals("")) {
                cardsFiltered = performSearch(cards, searchQuery);
                adapter.switchCards(cardsFiltered);
            } else {
                adapter.switchCards(cards);
            }
        }

    }


    private ArrayList<KeyInfo> performSearch(ArrayList<KeyInfo> cardList, String query) {

        String[] queryByWords = query.toLowerCase().split("\\s+");

        ArrayList<KeyInfo> cardsFiltered = new ArrayList<KeyInfo>();

        for (KeyInfo ki : cardList) {
            DateTimeFormatter fmt = DateTimeFormat.forPattern("d MMMM yyyy");
            String content = (
                    ki.getContact() + " " +
                            ki.getMail() + " " +
                            ki.getTerminationDate().toString(fmt) + " " +
                            ki.getValidAfter().toString(fmt)
            ).toLowerCase();

            int numberOfMatches = queryByWords.length;

            for (String word : queryByWords) {
                Log.d(SMileCrypto.LOG_TAG, "Search for " + word + " in " + content);

                if (content.contains(word)) {
                    Log.d(SMileCrypto.LOG_TAG, "Found");
                    numberOfMatches--;
                } else {
                    Log.d(SMileCrypto.LOG_TAG, "Not found");
                    break;
                }

                if (numberOfMatches == 0) {
                    Log.d(SMileCrypto.LOG_TAG, "Found complete query");
                    cardsFiltered.add(ki);
                }

            }

        }

        return cardsFiltered;
    }
}
