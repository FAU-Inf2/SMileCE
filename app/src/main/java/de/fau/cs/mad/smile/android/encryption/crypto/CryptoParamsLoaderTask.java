package de.fau.cs.mad.smile.android.encryption.crypto;


import android.os.AsyncTask;

import org.joda.time.DateTime;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import de.fau.cs.mad.smile.android.encryption.KeyInfo;
import korex.mail.Address;
import korex.mail.internet.InternetAddress;

public class CryptoParamsLoaderTask extends AsyncTask<Void, Void, CryptoParams> {
    private final KeyManagement keyManagement;
    private final InternetAddress identity;
    private final InternetAddress otherParty;

    public CryptoParamsLoaderTask(KeyManagement keyManagement, InternetAddress identity, InternetAddress otherParty) {
        this.keyManagement = keyManagement;
        this.identity = identity;
        this.otherParty = otherParty;
    }

    @Override
    protected CryptoParams doInBackground(Void... params) {
        CryptoParams cryptoParams = null;

        final String identityAlias;
        final String passphrase;
        if (identity != null) {
            identityAlias = getNewestAlias(identity, true);
            passphrase = keyManagement.getPassphraseForAlias(identityAlias);
        } else {
            identityAlias = null;
            passphrase = null;
        }

        final String otherPartyAlias = getNewestAlias(otherParty, false);

        try {
            final KeyStore.PrivateKeyEntry sender = keyManagement.getPrivateKeyEntry(identityAlias, passphrase);
            final X509Certificate trustedParty = keyManagement.getCertificateForAlias(otherPartyAlias);
            cryptoParams = new CryptoParams(sender, trustedParty);
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e) {
            return null;
        }

        return cryptoParams;
    }

    private String getNewestAlias(Address address, boolean ownAddress) {
        if (address == null) {
            return null;
        }

        ArrayList<KeyInfo> keyInfos;
        if(ownAddress) {
            keyInfos = keyManagement.getKeyInfoByOwnAddress(address);
        } else {
            keyInfos = keyManagement.getKeyInfoByAddress(address, false);
        }

        if (keyInfos.size() == 0) {
            return null;
        }

        String alias = null;
        DateTime termination = new DateTime(0);
        Collections.sort(keyInfos, new Comparator<KeyInfo>() {
            @Override
            public int compare(KeyInfo lhs, KeyInfo rhs) {
                return lhs.getTerminationDate().compareTo(rhs.getTerminationDate());
            }
        });

        for (KeyInfo keyInfo : keyInfos) { // use cert with longest validity
            DateTime terminationDate = keyInfo.getTerminationDate();
            if (terminationDate.isAfter(termination)) {
                alias = keyInfo.getAlias();
                termination = terminationDate;
            }
        }

        return alias;
    }
}
