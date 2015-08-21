package de.fau.cs.mad.smile.android.encryption;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        toolbar = (Toolbar) findViewById(R.id.search_toolbar);
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
}
