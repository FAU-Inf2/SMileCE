package de.fau.cs.mad.smile.android.encryption.ui;


import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

public abstract class AbstractCertificateInfoItem {
    public abstract void build(HashMap<String, String> data);
    public abstract View getView(ViewGroup parent);
}
