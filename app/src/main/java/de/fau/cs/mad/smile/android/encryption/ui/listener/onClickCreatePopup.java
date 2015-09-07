package de.fau.cs.mad.smile.android.encryption.ui.listener;

import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import de.fau.cs.mad.smile.android.encryption.App;
import de.fau.cs.mad.smile.android.encryption.KeyInfo;
import de.fau.cs.mad.smile.android.encryption.R;
import de.fau.cs.mad.smile.android.encryption.ui.adapter.KeyAdapter;

/**
 * Click listener for popup menu.
 */
public class onClickCreatePopup implements PopupMenu.OnMenuItemClickListener, View.OnClickListener {
    private KeyAdapter keyAdapter;
    private KeyInfo keyInfo;

    public onClickCreatePopup(KeyAdapter keyAdapter, KeyInfo keyInfo) {
        this.keyAdapter = keyAdapter;
        this.keyInfo = keyInfo;
    }

    @Override
    public void onClick(View v) {
        PopupMenu popup = new PopupMenu(App.getContext(), v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.card_context);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        boolean own = keyInfo.getAlias().startsWith("SMile_crypto_own");
        if (id == R.id.delete) {
            keyAdapter.deleteCertificate(keyInfo);
        } else if (id == R.id.export) {
            keyAdapter.exportCertificate(keyInfo);
        }
        return true;
    }
}
