package de.fau.cs.mad.smile.android.encryption;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.joda.time.DateTime;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class KeyInfo implements Comparable<KeyInfo> {

    private static SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());

    private String alias;
    private String contact;
    private final List<String> mailAddresses;
    private String mail;
    private String type;
    private String hash;
    private String trust;
    private String thumbprint;
    private DateTime termination_date;
    private Boolean hasPrivateKey;
    private DateTime valid_after;
    private X509Certificate certificate;

    public KeyInfo() {
        mailAddresses = new ArrayList<>();
        setTerminationDate(new DateTime());
        setValidAfter(new DateTime());
        setHasPrivateKey(false);
        setCertificate(null);
    }

    @Override
    public boolean equals(Object o) {
        //if(SMileCrypto.DEBUG) {
            //Log.e(SMileCrypto.LOG_TAG, "Equals");
        //}
        if (!(o instanceof KeyInfo)) {
            return false;
        }

        KeyInfo other = (KeyInfo) o;
        if (other.getThumbprint().equals(this.getThumbprint())) {
            return true;
        }

        return false;
    }

    public String getAlias() {
        if (alias == null) {
            return "";
        }
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getContact() {
        if (contact == null) {
            return "";
        }
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public List<String> getMailAddresses() {
        return mailAddresses;
    }

    public String getMail() {
        if (mail == null) {
            return "";
        }
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getType() {
        if (type == null) {
            return "";
        }
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHash() {
        if (hash == null) {
            return "";
        }
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getTrust() {
        if (trust == null) {
            return "";
        }
        return trust;
    }

    public void setTrust(String trust) {
        this.trust = trust;
    }

    public String getThumbprint() {
        if (thumbprint == null) {
            return "";
        }
        return thumbprint;
    }

    public void setThumbprint(String thumbprint) {
        this.thumbprint = thumbprint;
    }

    public DateTime getTerminationDate() {
        return termination_date;
    }

    public void setTerminationDate(DateTime termination_date) {
        this.termination_date = termination_date;
    }

    public Boolean getHasPrivateKey() {
        return hasPrivateKey;
    }

    public void setHasPrivateKey(Boolean hasPrivateKey) {
        this.hasPrivateKey = hasPrivateKey;
    }

    public DateTime getValidAfter() {
        return valid_after;
    }

    public void setValidAfter(DateTime valid_after) {
        this.valid_after = valid_after;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }


    @Override
    public int compareTo(KeyInfo another) {
        Context context = App.getContext();
        switch (sharedPreferences.getString("pref_key_type", "0")) {
            case "0":
                return compareName(another);
            case "1":
                return compareMail(another);
            case "2":
                return compareTermination(another);
            case "3":
                return comparePrivateKey(another);
            default:
                if(SMileCrypto.isDEBUG()) {
                    Log.d(SMileCrypto.LOG_TAG, "Sort by default not possible.");
                }
                throw new IllegalArgumentException("Sort by " + sharedPreferences.getString("pref_key_type", "0") + " not implemented.");
        }
    }

    /**
     * Compares contacts to this KeyInfo and another.
     *
     * @param another Compare to this KeyInfo.
     * @return Comparison between contacts. If one or both do not have a contact set, it uses mail.
     */

    public int compareName(KeyInfo another) {
        int erg = 0;
        if (this.getContact().equals("") && another.getContact().equals("")) {
            erg = this.getMail().compareTo(another.getMail());
        } else if (this.getContact().equals("")) {
            erg = this.getMail().compareTo(another.getContact());
        } else if (another.getContact().equals("")) {
            erg = this.getContact().compareTo(another.getMail());
        } else {
            erg = this.getContact().compareTo(another.getContact());
        }
        if(SMileCrypto.isDEBUG()) {
            Log.d(SMileCrypto.LOG_TAG, "Sorted by: Name; Order: " + Integer.valueOf(sharedPreferences.getString("pref_key_direction", "1")));
        }
        erg *= Integer.valueOf(sharedPreferences.getString("pref_key_direction", "1"));
        return erg;
    }

    /**
     * Compares mail address to this KeyInfo and another.
     *
     * @param another Compare to this KeyInfo.
     * @return Comparison between mail address.
     */

    public int compareMail(KeyInfo another) {
        int erg = 0;
        erg = this.getMail().compareTo(another.getMail());
        if(SMileCrypto.isDEBUG()) {
            if(SMileCrypto.isDEBUG()) {
                Log.d(SMileCrypto.LOG_TAG, "Sorted by: Mail address; Order: " + Integer.valueOf(sharedPreferences.getString("pref_key_direction", "1")));
            }
        }
        erg *= Integer.valueOf(sharedPreferences.getString("pref_key_direction", "1"));
        return erg;
    }

    /**
     * Compares expiration dates to this KeyInfo and another.
     *
     * @param another Compare to this KeyInfo.
     * @return Comparison between expiration dates.
     */

    public int compareTermination(KeyInfo another) {
        int erg = 0;
        erg = this.getTerminationDate().compareTo(another.getTerminationDate());
        if(SMileCrypto.isDEBUG()) {
            Log.d(SMileCrypto.LOG_TAG, "Sorted by: Expiration date; Order: " + Integer.valueOf(sharedPreferences.getString("pref_key_direction", "1")));
        }
        erg *= Integer.valueOf(sharedPreferences.getString("pref_key_direction", "1"));
        return erg;
    }

    /**
     * Compares if KeyInfo has a private key.
     *
     * @param another Compare to this KeyInfo.
     * @return 1/-1 If one has a private key. Else they are compared by name.
     */

    public int comparePrivateKey(KeyInfo another) {
        int erg = 0;
        boolean otherPriv = another.getHasPrivateKey();
        if (this.getHasPrivateKey()) {
            if (otherPriv) {
                erg = compareName(another);
            } else {
                erg = 1;
            }
        } else {
            if (otherPriv) {
                erg = -1;
            } else {
                erg = compareName(another);
            }
        }
        erg = this.getTerminationDate().compareTo(another.getTerminationDate());
        if(SMileCrypto.isDEBUG()) {
            Log.d(SMileCrypto.LOG_TAG, "Sorted by: Expiration date; Order: " + Integer.valueOf(sharedPreferences.getString("pref_key_direction", "1")));
        }
        erg *= Integer.valueOf(sharedPreferences.getString("pref_key_direction", "1"));
        return erg;
    }


}
