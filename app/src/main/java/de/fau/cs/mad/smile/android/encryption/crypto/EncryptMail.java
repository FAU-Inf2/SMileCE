package de.fau.cs.mad.smile.android.encryption.crypto;


import android.os.AsyncTask;
import android.util.Log;

import org.spongycastle.cms.CMSAlgorithm;
import org.spongycastle.cms.RecipientInfoGenerator;
import org.spongycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.spongycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.spongycastle.operator.OutputEncryptor;

import java.security.KeyStoreException;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.fau.cs.mad.smile.android.encryption.SMileCrypto;
import korex.mail.internet.MimeBodyPart;
import korex.mail.internet.MimeMessage;

public class EncryptMail {
    static {
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
    }

    public MimeMessage encryptMessage(MimeMessage message, CryptoParams cryptoParams) throws KeyStoreException {
        return encryptMessage(message, cryptoParams.getTrustedParty());
    }

    public MimeMessage encryptMessage(MimeMessage message, List<X509Certificate> certificate) {
        try {
            if (message == null) {
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
                return null;
            }

            return new AsyncEncryptMessage(message, certificate).execute().get();
        } catch (Exception e) {
            if(SMileCrypto.DEBUG) {
                Log.e(SMileCrypto.LOG_TAG, "Exception in encryptMessage: " + e.getMessage());
            }
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_ERROR_ASYNC_TASK;
            return null;
        }
    }

    public MimeBodyPart encryptBodyPart(MimeBodyPart mimeBodyPart, CryptoParams cryptoParams) throws KeyStoreException, ExecutionException, InterruptedException {
        List<X509Certificate> certificate = cryptoParams.getTrustedParty();
        return new AsyncEncryptPart(mimeBodyPart, certificate).execute().get();
    }

    private static class AsyncEncryptPart extends AsyncTask<Void, Void, MimeBodyPart> {
        private final MimeBodyPart bodyPart;
        private final List<X509Certificate> certificates;

        public AsyncEncryptPart(MimeBodyPart bodyPart, List<X509Certificate> certificates) {
            this.bodyPart = bodyPart;
            this.certificates = certificates;
        }

        @Override
        protected MimeBodyPart doInBackground(Void... params) {
            return encryptBodyPart();
        }

        private MimeBodyPart encryptBodyPart() {
            SMIMEEnvelopedGenerator envelopedGenerator = new SMIMEEnvelopedGenerator();

            try {
                for(X509Certificate certificate : certificates) {
                    RecipientInfoGenerator recipientInfoGen = new JceKeyTransRecipientInfoGenerator(certificate);
                    envelopedGenerator.addRecipientInfoGenerator(recipientInfoGen);
                }

                JceCMSContentEncryptorBuilder builder =
                        new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES256_CBC);
                builder.setProvider(BouncyCastleProvider.PROVIDER_NAME);
                OutputEncryptor encryptor = builder.build();

                return envelopedGenerator.generate(bodyPart, encryptor);
            } catch (Exception e) {
                if(SMileCrypto.DEBUG) {
                    Log.e(SMileCrypto.LOG_TAG, "Exception while encrypting MimeBodyPart: " + e.getMessage());
                }
                e.printStackTrace();
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_UNKNOWN_ERROR;
                return null;
            }
        }
    }
}