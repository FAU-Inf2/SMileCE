package de.fau.cs.mad.smile.android.encryption;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import it.gmariotti.cardslib.library.internal.CardHeader;

public class KeyCardHeader extends CardHeader {
    private Boolean hasPrivateKey = false;

    public KeyCardHeader(Context context) {
        super(context, R.layout.keycard_header);
    }

    public KeyCardHeader(Context context, Boolean hasPrivateKey) {
        super(context, R.layout.keycard_header);
        this.hasPrivateKey = hasPrivateKey;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);
        if(this.hasPrivateKey) {
            if(view != null) {
                ImageView privateKey = (ImageView) view.findViewById(R.id.privateKey);
                privateKey.setVisibility(View.VISIBLE);
            }
        }
    }
}