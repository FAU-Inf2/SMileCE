package de.fau.cs.mad.smile_crypto;

import android.widget.AdapterView;
import android.view.View;


public class NavigationDrawerListener implements AdapterView.OnItemClickListener {

    private final NavigationDrawerFragment fragment;

    public NavigationDrawerListener(NavigationDrawerFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        fragment.selectItem(position);
    }
}
