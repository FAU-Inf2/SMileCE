package de.fau.cs.mad.smile.android.encryption;


import android.view.View;

import java.util.HashMap;

public abstract class AbstractCertificateInfoItem {
    public abstract void build(HashMap<String, String> data);
    public abstract View getView();
}
