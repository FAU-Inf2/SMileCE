package de.fau.cs.mad.smile.android.encryption;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;
import java.io.Serializable;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import de.fau.cs.mad.smile.android.encryption.R;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.recyclerview.internal.CardArrayRecyclerViewAdapter;
import it.gmariotti.cardslib.library.recyclerview.view.CardRecyclerView;

public class SearchActivity extends ActionBarActivity {
    private CardArrayRecyclerViewAdapter mCardArrayAdapter;
    private KeyManagement keyManager;

    private CardListUpdater updater;
    private ArrayList<Card> cards;

    private Toolbar toolbar;
    private String searchQuery;
    private EditText searchEt;
    private ArrayList<Card> cardsFiltered;
    private String queryText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        toolbar.setTitle(R.string.title_activity_search);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        cards = new ArrayList<>();
        queryText = "";

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
        searchEt = (EditText) toolbar.findViewById(R.id.search_bar);
        searchEt.addTextChangedListener(new SearchWatcher());
        searchEt.setText(queryText);
        searchEt.requestFocus();

        ImageView image = (ImageView) toolbar.findViewById(R.id.remove_search);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEt.setText("");
                updater.updateCards();
            }
        });

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
            cardsFiltered = performSearch(cards, searchQuery);
            updater.updateCards(cardsFiltered);
        }

    }


    private ArrayList<Card> performSearch(ArrayList<Card> cardList, String query) {

        String[] queryByWords = query.toLowerCase().split("\\s+");

        ArrayList<Card> cardsFiltered = new ArrayList<Card>();

        for (Card card : cardList) {
            if (!(card instanceof KeyCard)) {
                continue;
            }
            KeyCard kc = (KeyCard) card;
            KeyInfo ki = kc.keyInfo;
            String content = (
                    ki.contact + " " +
                            ki.mail + " " +
                            String.valueOf(ki.termination_date) + " " +
                            String.valueOf(ki.valid_after)
            ).toLowerCase();

            for (String word : queryByWords) {
                Log.d(SMileCrypto.LOG_TAG, "Search for " + word + " in " + content);

                int numberOfMatches = queryByWords.length;

                if (content.contains(word)) {
                    numberOfMatches--;
                } else {
                    break;
                }

                if (numberOfMatches == 0) {
                    Log.d(SMileCrypto.LOG_TAG, "found");
                    cardsFiltered.add(card);
                }

            }

        }

        return cardsFiltered;
    }
}
