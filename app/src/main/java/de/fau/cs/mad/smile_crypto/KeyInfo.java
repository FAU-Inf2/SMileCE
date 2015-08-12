package de.fau.cs.mad.smile_crypto;

import android.util.Log;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.Objects;

public class KeyInfo {
    protected String alias = "";
    protected String contact = "Max Mustermann";
    protected String mail = "Max@musterman.de";
    protected String type = "";
    protected String hash = "";
    protected String trust = "0";
    protected DateTime termination_date = new DateTime();

    @Override
    public boolean equals(Object o) {
        Log.e(SMileCrypto.LOG_TAG, "Equals");
        if(o instanceof KeyInfo) {
            KeyInfo other = (KeyInfo) o;
            Log.e(SMileCrypto.LOG_TAG, "This.hash = " + this.hash + " o.hash = " + other.hash);
            if (other.hash.equals(this.hash)) {
                Log.e(SMileCrypto.LOG_TAG, "This.hash == o.hash");
                return true;
            }
        }
        Log.e(SMileCrypto.LOG_TAG, "This.hash != o.hash");
        return false;
    }
}
