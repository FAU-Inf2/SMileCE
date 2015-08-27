package de.fau.cs.mad.smile.android.encryption;

import org.joda.time.DateTime;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class KeyInfo {
    private String alias;
    private String contact;
    private final List<String> mailAddresses;
    private String mail;
    private String type;
    private String hash;
    private String trust;
    private String thumbprint;
    private DateTime termination_date;
    private Boolean hasPrivateKey = false;
    private DateTime valid_after;
    private X509Certificate certificate = null;

    public KeyInfo() {
        mailAddresses = new ArrayList<>();
        setTerminationDate(new DateTime());
        setValidAfter(new DateTime());
    }

    @Override
    public boolean equals(Object o) {
        //Log.e(SMileCrypto.LOG_TAG, "Equals");
        if(!(o instanceof KeyInfo)) {
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
}
