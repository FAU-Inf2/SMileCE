package de.fau.cs.mad.smile.android.encryption.crypto;

import android.os.AsyncTask;
import android.util.Log;

import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.jcajce.JcaX509CertificateHolder;
import org.spongycastle.cms.SignerInfoGenerator;
import org.spongycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.mail.smime.SMIMESignedGenerator;
import org.spongycastle.util.CollectionStore;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import de.fau.cs.mad.smile.android.encryption.SMileCrypto;
import korex.mail.internet.MimeBodyPart;
import korex.mail.internet.MimeMultipart;

public class SignMessage {
    static {
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
    }

    public MimeMultipart sign(MimeBodyPart mimeBodyPart, KeyStore.PrivateKeyEntry privateKey) {
        if (mimeBodyPart == null) {
            if(SMileCrypto.DEBUG) {
                Log.e(SMileCrypto.LOG_TAG, "Could not sign, mimeBodyPart was null.");
            }
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
            return null;
        }

        if (privateKey == null) {
            if(SMileCrypto.DEBUG) {
                Log.e(SMileCrypto.LOG_TAG, "Could not sign, privateKeyEntry was null.");
            }
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
            return null;
        }

        try {
            return new AsyncSign(mimeBodyPart, privateKey).execute().get();
        } catch (Exception e) {
            if(SMileCrypto.DEBUG) {
                Log.e(SMileCrypto.LOG_TAG, "Exception in sign: " + e.getMessage());
            }
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_ERROR_ASYNC_TASK;
            return null;
        }
    }

    private static class AsyncSign extends AsyncTask<Void, Void, MimeMultipart> {
        private final MimeBodyPart mimeBodyPart;
        private final KeyStore.PrivateKeyEntry privateKeyEntry;

        public AsyncSign(final MimeBodyPart mimeBodyPart, final KeyStore.PrivateKeyEntry privateKeyEntry) {
            this.mimeBodyPart = mimeBodyPart;
            this.privateKeyEntry = privateKeyEntry;
        }

        @Override
        protected MimeMultipart doInBackground(Void... params) {
            return sign();
        }

        private MimeMultipart sign() {
            MimeMultipart signedMimeMultipart = null;

            try {
                if(SMileCrypto.DEBUG) {
                    Log.d(SMileCrypto.LOG_TAG, "Sign mimeBodyPart.");
                }

                JcaSimpleSignerInfoGeneratorBuilder builder = new JcaSimpleSignerInfoGeneratorBuilder();
                builder.setProvider(BouncyCastleProvider.PROVIDER_NAME);
                X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
                PrivateKey privateKey = privateKeyEntry.getPrivateKey();
                SignerInfoGenerator signerInfoGenerator = builder.build("SHA256WITHRSA", privateKey, certificate);

                SMIMESignedGenerator gen = new SMIMESignedGenerator();
                List<X509CertificateHolder> certList = new ArrayList<>();

                for (Certificate cert : privateKeyEntry.getCertificateChain()) {
                    certList.add(new JcaX509CertificateHolder((X509Certificate) cert));
                }

                gen.addCertificates(new CollectionStore(certList));
                gen.addSignerInfoGenerator(signerInfoGenerator);

                signedMimeMultipart = gen.generate(mimeBodyPart);
            } catch (Exception e) {
                if(SMileCrypto.DEBUG) {
                    Log.e(SMileCrypto.LOG_TAG, "Error signing mimeBodyPart: " + e.getMessage());
                }
                e.printStackTrace();
            }

            return signedMimeMultipart;
        }
    }
}
