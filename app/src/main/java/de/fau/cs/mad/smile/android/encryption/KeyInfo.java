package de.fau.cs.mad.smile.android.encryption;

import org.joda.time.DateTime;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class KeyInfo {
    protected String alias = "";
    protected String contact = "";
    protected final List<String> mailAddresses;
    protected String mail = "";
    protected String type = "";
    protected String hash = "";
    protected String trust = "";
    protected String thumbprint = "";
    protected DateTime termination_date;
    protected Boolean hasPrivateKey = false;
    protected DateTime valid_after;
    protected X509Certificate certificate = null;


    public KeyInfo() {
        mailAddresses = new ArrayList<String>();
        termination_date = new DateTime();
        valid_after = new DateTime();
    }

    @Override
    public boolean equals(Object o) {
        //Log.e(SMileCrypto.LOG_TAG, "Equals");
        if(!(o instanceof KeyInfo)) {
            return false;
        }

        KeyInfo other = (KeyInfo) o;
        if (other.thumbprint.equals(this.thumbprint)) {
            return true;
        }

        return false;
    }
}
