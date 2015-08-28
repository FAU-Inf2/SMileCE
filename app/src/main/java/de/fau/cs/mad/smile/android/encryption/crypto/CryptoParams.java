package de.fau.cs.mad.smile.android.encryption.crypto;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

public class CryptoParams {
    private final KeyStore.PrivateKeyEntry identity;
    private final X509Certificate trustedParty;

    public CryptoParams(KeyStore.PrivateKeyEntry identity, X509Certificate trustedParty) {
        this.identity = identity;
        this.trustedParty = trustedParty;
    }

    public KeyStore.PrivateKeyEntry getIdentity() {
        return identity;
    }

    public X509Certificate getTrustedParty() {
        return trustedParty;
    }
}
