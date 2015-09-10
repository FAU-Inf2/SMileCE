package de.fau.cs.mad.smile.android.encryption.ui.activity;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.fau.cs.mad.smile.android.encryption.R;
import de.fau.cs.mad.smile.android.encryption.SMileCrypto;
import de.fau.cs.mad.smile.android.encryption.ui.adapter.ExpandableListAdapter;
import de.fau.cs.mad.smile.android.encryption.utilities.Utils;

/**
 * Display FAQ
 */
public class HelpActivity extends ActionBarActivity {
    private Toolbar toolbar;
    ExpandableListAdapter listAdapter;
    private ExpandableListView expListView;
    private List<String> listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<Pair<Integer, String[]>>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.navigation_drawer_help);
        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar supportActionBar = getSupportActionBar();
        if(supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.lvExp);

        // preparing list data
        prepareListData();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info, menu);
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


    /**
     * Get data from xml files and display it.
     */
    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<Pair<Integer, String[]>>>();
        if(SMileCrypto.isDEBUG()) {
            Log.d(SMileCrypto.LOG_TAG, "Help:");
        }
        Resources resources = getResources();
        Collections.addAll(listDataHeader, resources.getStringArray(R.array.faq_questions));
        int headerIndex = 0;
        for (String key : resources.getStringArray(R.array.faq_key_array)) {
            if(SMileCrypto.isDEBUG()) {
                Log.d(SMileCrypto.LOG_TAG, "\tProcessing key: " + key);
            }
            List<TypedArray> helpItems = Utils.getMultiTypedArray(this, key);
            List<Pair<Integer, String[]>> result = new ArrayList<>();
            for (TypedArray item : helpItems) {
                int size = item.length() - 1;
                if(SMileCrypto.isDEBUG()) {
                    Log.d(SMileCrypto.LOG_TAG, "\t\tLength of data: " + size);
                }
                int type = item.getInt(0, 0);
                String[] helpText = new String[size];
                for (int i = 0; i < size; ++i) {
                    String description;
                    if(type == 2 && i == 0) { // Get resource id, from first value if type is 2
                        int res = item.getResourceId(i + 1, 0);
                        description = Integer.toString(res);
                    } else {
                        description = item.getString(i + 1);
                    }
                    if(SMileCrypto.isDEBUG()) {
                        Log.d(SMileCrypto.LOG_TAG, "\t\tDescription: " + description);
                    }
                    helpText[i] = description;
                }
                Pair<Integer, String[]> value = Pair.create(type, helpText);
                result.add(value);
            }
            listDataChild.put(listDataHeader.get(headerIndex), result);
            ++headerIndex;
        }
    }
}
