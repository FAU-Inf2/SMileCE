package de.fau.cs.mad.smile.android.encryption.ui.listener;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;

import com.daimajia.swipe.SwipeLayout;

import de.fau.cs.mad.smile.android.encryption.App;
import de.fau.cs.mad.smile.android.encryption.R;

/**
 * Detect share swipe.
 */
public class ShareRevealListener implements SwipeLayout.OnRevealListener {

    @Override
    public void onReveal(View view, SwipeLayout.DragEdge dragEdge, float v, int i) {
        SharedPreferences sharedPreferences = App.getPreferences();
        View share_icon = view.findViewById(R.id.share_icon);

        float shareDistance = sharedPreferences.getInt("share_distance", 20) / 100.0f;
        if (dragEdge != SwipeLayout.DragEdge.Left) {
            return;
        }

        if (v <= shareDistance && share_icon.isShown()) {
            share_icon.setVisibility(View.INVISIBLE);
        } else if (v > shareDistance && !share_icon.isShown()) {
            share_icon.setVisibility(View.VISIBLE);
        }
    }
}
