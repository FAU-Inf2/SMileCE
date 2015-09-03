package de.fau.cs.mad.smile.android.encryption.ui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.fau.cs.mad.smile.android.encryption.R;
import de.fau.cs.mad.smile.android.encryption.ui.adapter.ExpandableListAdapter;


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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

    private List<Pair<Integer, String[]>> setColours() {
        List<Pair<Integer, String[]>> colList = new ArrayList<>();
        String[] c0 = {"#0000ff", "#0000ff", "#0000ff", "#0000ff", "#0000ff", getString(R.string.faq_colour_00)};
        colList.add(Pair.create(1, c0));

        String[] c1 = {"#00ff00", "#00ff00", "#00ff00", "#00ff00", "#00ff00", getString(R.string.faq_colour_01)};
        colList.add(Pair.create(1, c1));

        String[] c2 = {"#00ff00", "#00ff00", "#00ff00", "#00ff00", "#d3d3d3", getString(R.string.faq_colour_02)};
        colList.add(Pair.create(1, c2));

        String[] c3 = {"#FFA500", "#FFA500", "#FFA500", "#d3d3d3", "#d3d3d3", getString(R.string.faq_colour_03)};
        colList.add(Pair.create(1, c3));

        String[] c4 = {"#FFA500", "#FFA500", "#d3d3d3", "#d3d3d3", "#d3d3d3", getString(R.string.faq_colour_04)};
        colList.add(Pair.create(1, c4));

        String[] c5 = {"#FFA500", "#d3d3d3", "#d3d3d3", "#d3d3d3", "#d3d3d3", getString(R.string.faq_colour_05)};
        colList.add(Pair.create(1, c5));

        String[] c6 = {"#ff0000", "#d3d3d3", "#d3d3d3", "#d3d3d3", "#d3d3d3", getString(R.string.faq_colour_06)};
        colList.add(Pair.create(1, c6));

        return colList;
    }

    private List<Pair<Integer, String[]>> getKeys() {
        List<Pair<Integer, String[]>> colList = new ArrayList<>();
        String[] c0 = {Integer.toString(R.drawable.ic_public_key_silver_48dp), getString(R.string.faq_public_key_saved)};
        colList.add(Pair.create(2, c0));

        String[] c1 = {Integer.toString(R.drawable.ic_private_key_black_48dp), getString(R.string.faq_private_key_saved)};
        colList.add(Pair.create(2, c1));
        return colList;
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<Pair<Integer, String[]>>>();

        // Adding child data
        listDataHeader.add(getString(R.string.faq_import_cert_q));
        listDataHeader.add(getString(R.string.faq_passphrase_saved_q));
        listDataHeader.add(getString(R.string.faq_import_other_cert_q));
        listDataHeader.add(getString(R.string.faq_meaning_colour_q));
        listDataHeader.add(getString(R.string.faq_meaning_coloured_keys_q));
        listDataHeader.add(getString(R.string.faq_additional_certificate_info_q));
        listDataHeader.add(getString(R.string.faq_delete_certificate_q));
        listDataHeader.add(getString(R.string.faq_expert_mode_q));
        listDataHeader.add(getString(R.string.faq_author));
        listDataHeader.add(getString(R.string.faq_license));
        listDataHeader.add(getString(R.string.faq_other_projects));

        String[] certText = {getString(R.string.faq_import_cert)};
        Pair<Integer, String[]> cert = Pair.create(0, certText);
        List<Pair<Integer, String[]>> importCert = new ArrayList<>();
        importCert.add(cert);

        String[] pwText = {getString(R.string.faq_passphrase_saved)};
        Pair<Integer, String[]> pw = Pair.create(0, pwText);
        List<Pair<Integer, String[]>> pwList = new ArrayList<>();
        pwList.add(pw);

        String[] otherCertText = {getString(R.string.faq_import_other_cert)};
        Pair<Integer, String[]> otherCert = Pair.create(0, otherCertText);
        List<Pair<Integer, String[]>> importOtherCert = new ArrayList<>();
        importOtherCert.add(otherCert);

        String[] colorText = {getString(R.string.faq_meaning_colour)};
        Pair<Integer, String[]> col = Pair.create(0, colorText);
        List<Pair<Integer, String[]>> colList = new ArrayList<>();
        colList.add(col);
        colList.addAll(setColours());

        String[] colorKey = {getString(R.string.faq_meaning_coloured_keys)};
        Pair<Integer, String[]> colK = Pair.create(0, colorKey);
        List<Pair<Integer, String[]>> colKList = new ArrayList<>();
        colKList.add(colK);
        colKList.addAll(getKeys());

        String[] certInfoText = {getString(R.string.faq_additional_certificate_info)};
        Pair<Integer, String[]> certInfo = Pair.create(0, certInfoText);
        List<Pair<Integer, String[]>> certInfoList = new ArrayList<>();
        certInfoList.add(certInfo);

        String[] deleteText = {getString(R.string.faq_delete_certificate)};
        Pair<Integer, String[]> del = Pair.create(0, deleteText);
        List<Pair<Integer, String[]>> delList = new ArrayList<>();
        delList.add(del);

        String[] expertText = {getString(R.string.faq_expert_mode)};
        Pair<Integer, String[]> exp = Pair.create(0, expertText);
        List<Pair<Integer, String[]>> expList = new ArrayList<>();
        expList.add(exp);

        String[] authorText = {getString(R.string.author_text)};
        Pair<Integer, String[]> author = Pair.create(0, authorText);
        List<Pair<Integer, String[]>> authorList = new ArrayList<>();
        authorList.add(author);

        String[] licenceText = {getString(R.string.apache)};
        Pair<Integer, String[]> licence = Pair.create(0, licenceText);
        List<Pair<Integer, String[]>> licenceList = new ArrayList<>();
        licenceList.add(licence);

        String[] othersText = {getString(R.string.other_projects_list)};
        Pair<Integer, String[]> others = Pair.create(0, othersText);
        List<Pair<Integer, String[]>> othersList = new ArrayList<>();
        othersList.add(others);

        listDataChild.put(listDataHeader.get(0), importCert);
        listDataChild.put(listDataHeader.get(1), pwList);
        listDataChild.put(listDataHeader.get(2), importOtherCert);
        listDataChild.put(listDataHeader.get(3), colList);
        listDataChild.put(listDataHeader.get(4), colKList);
        listDataChild.put(listDataHeader.get(5), certInfoList);
        listDataChild.put(listDataHeader.get(6), delList);
        listDataChild.put(listDataHeader.get(7), expList);
        listDataChild.put(listDataHeader.get(8), authorList);
        listDataChild.put(listDataHeader.get(9), licenceList);
        listDataChild.put(listDataHeader.get(10), othersList);

    }
}
