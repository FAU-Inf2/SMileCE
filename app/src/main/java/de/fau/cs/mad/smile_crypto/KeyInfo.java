package de.fau.cs.mad.smile_crypto;

import android.util.Log;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class KeyInfo {
    protected String alias = "";
    protected String contact = "Max Mustermann";
    protected final List<String> mailAddresses;
    protected String mail = "Max@musterman.de";
    protected String type = "";
    protected String hash = "";
    protected String trust = "0";
    protected String thumbprint = "0";
    protected DateTime termination_date = new DateTime();
    protected Boolean hasPrivateKey = false;

    public KeyInfo() {
        mailAddresses = new ArrayList<String>();
        termination_date = new DateTime();
    }

    @Override
    public boolean equals(Object o) {
        Log.e(SMileCrypto.LOG_TAG, "Equals");
        if(!(o instanceof KeyInfo)) {
            return false;
        }

        KeyInfo other = (KeyInfo) o;
        Log.e(SMileCrypto.LOG_TAG, "This.thumbprint = " + this.thumbprint + " o.thumbprint = " + other.thumbprint);
        if (other.thumbprint.equals(this.thumbprint)) {
            Log.e(SMileCrypto.LOG_TAG, "This.thumbprint == o.thumbprint");
            return true;
        }

        Log.e(SMileCrypto.LOG_TAG, "This.thumbprint != o.thumbprint");
        return false;
    }
}
