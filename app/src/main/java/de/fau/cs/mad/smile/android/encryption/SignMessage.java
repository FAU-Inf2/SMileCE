package de.fau.cs.mad.smile.android.encryption;

import android.os.AsyncTask;
import android.util.Log;

import org.joda.time.DateTime;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.jcajce.JcaX509CertificateHolder;
import org.spongycastle.cms.SignerInfoGenerator;
import org.spongycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.mail.smime.SMIMESignedGenerator;
import org.spongycastle.util.CollectionStore;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import korex.mail.Address;
import korex.mail.internet.MimeBodyPart;
import korex.mail.internet.MimeMultipart;

public class SignMessage {
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    private final KeyManagement keyManagement;

    public SignMessage() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        keyManagement = new KeyManagement();
    }

    public MimeMultipart sign(MimeBodyPart mimeBodyPart, Address signerAddress) {
        if(mimeBodyPart == null) {
            Log.e(SMileCrypto.LOG_TAG, "Could not sign, mimeBodyPart was null.");
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
            return null;
        }

        if(signerAddress == null) {
            Log.e(SMileCrypto.LOG_TAG, "Could not sign, signerAddress was null.");
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
            return null;
        }

        PrivateKey privateKey;
        X509Certificate certificate;

        try {
            ArrayList<KeyInfo> keyInfos = keyManagement.getKeyInfoByOwnAddress(signerAddress);
            if(keyInfos.size() == 0) {
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_NO_CERTIFICATE_FOUND;
                Log.e(SMileCrypto.LOG_TAG, "No certificate found for signer address: " + signerAddress);
                return null;
            }

            String alias = "";
            DateTime termination = new DateTime(0);

            for(KeyInfo keyInfo : keyInfos) { // use cert with longest validity
                DateTime terminationDate = keyInfo.getTerminationDate();
                if(terminationDate.isAfter(termination)) {
                    alias = keyInfo.getAlias();
                    termination = terminationDate;
                }
            }

            privateKey = keyManagement.getPrivateKeyForAlias(alias, keyManagement.getPassphraseForAlias(alias));
            certificate = keyManagement.getCertificateForAlias(alias);
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error getting info from KeyManagement: " + e.getMessage());
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_UNKNOWN_ERROR;
            return null;
        }

        return sign(mimeBodyPart, privateKey, certificate);
    }

    private MimeMultipart sign(MimeBodyPart mimeBodyPart, PrivateKey privateKey, X509Certificate certificate) {
        if(mimeBodyPart == null) {
            Log.e(SMileCrypto.LOG_TAG, "Could not sign, mimeBodyPart was null.");
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
            return null;
        }

        if(privateKey == null) {
            Log.e(SMileCrypto.LOG_TAG, "Could not sign, privateKey was null.");
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
            return null;
        }

        if(certificate == null) {
            Log.e(SMileCrypto.LOG_TAG, "Could not sign, certificate was null.");
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
            return null;
        }

        try {
            return new AsyncSign(mimeBodyPart, privateKey, certificate).execute().get();
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Exception in sign: " + e.getMessage());
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_ERROR_ASYNC_TASK;
            return null;
        }
    }

    private class AsyncSign extends AsyncTask<Void, Void, MimeMultipart> {
        private final MimeBodyPart mimeBodyPart;
        private final PrivateKey privateKey;
        private final X509Certificate certificate;

        public AsyncSign(MimeBodyPart mimeBodyPart, PrivateKey privateKey, X509Certificate certificate) {
            this.mimeBodyPart = mimeBodyPart;
            this.privateKey = privateKey;
            this.certificate = certificate;
        }

        @Override
        protected MimeMultipart doInBackground(Void... params) {
            return sign();
        }

        private MimeMultipart sign() {
            MimeMultipart signedMimeMultipart = null;

            try {
                Log.d(SMileCrypto.LOG_TAG, "Sign mimeBodyPart.");

                JcaSimpleSignerInfoGeneratorBuilder builder = new JcaSimpleSignerInfoGeneratorBuilder();
                builder.setProvider(BouncyCastleProvider.PROVIDER_NAME);
                SignerInfoGenerator signerInfoGenerator = builder.build("SHA256WITHRSA", privateKey, certificate);

                SMIMESignedGenerator gen = new SMIMESignedGenerator();
                List<X509CertificateHolder> certList = new ArrayList<>();
                certList.add(new JcaX509CertificateHolder(certificate));

                Certificate[] chain = keyManagement.getCertificateChain(certificate);
                for (Certificate cert : chain) {
                    certList.add(new JcaX509CertificateHolder((X509Certificate) cert));
                }

                gen.addCertificates(new CollectionStore(certList));
                gen.addSignerInfoGenerator(signerInfoGenerator);

                signedMimeMultipart = gen.generate(mimeBodyPart);
            } catch (Exception e) {
                Log.e(SMileCrypto.LOG_TAG, "Error signing mimeBodyPart: " + e.getMessage());
                e.printStackTrace();
            }

            return signedMimeMultipart;
        }
    }
}
