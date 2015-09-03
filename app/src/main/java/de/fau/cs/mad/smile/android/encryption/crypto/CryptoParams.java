package de.fau.cs.mad.smile.android.encryption.crypto;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class CryptoParams {
    private final KeyStore.PrivateKeyEntry identity;
    private final List<X509Certificate> trustedParty;

    public CryptoParams(KeyStore.PrivateKeyEntry identity) {
        this.identity = identity;
        this.trustedParty = new ArrayList<>();
    }

    public KeyStore.PrivateKeyEntry getIdentity() {
        return identity;
    }

    public List<X509Certificate> getTrustedParty() {
        return trustedParty;
    }

    public void addTrustedParty(X509Certificate certificate) {
        trustedParty.add(certificate);
    }
}
