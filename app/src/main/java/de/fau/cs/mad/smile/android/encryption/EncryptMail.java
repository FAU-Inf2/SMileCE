package de.fau.cs.mad.smile.android.encryption;


import android.os.AsyncTask;
import android.util.Log;

import org.spongycastle.cms.CMSAlgorithm;
import org.spongycastle.cms.RecipientInfoGenerator;
import org.spongycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.spongycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.spongycastle.jcajce.provider.asymmetric.X509;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.spongycastle.operator.OutputEncryptor;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

public class EncryptMail {
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    private final KeyManagement keyManagement;

    public EncryptMail() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        keyManagement = new KeyManagement();
    }

    /*
    public MimeMessage encryptMessage(MimeMessage message) {
        try {
            if(message == null) {
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
                return null;
            }
            return new encryptMessage(message);
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Exception in encryptMessage: " + e.getMessage());
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_ERROR_ASYNC_TASK;
            return null;
        }
    }*/

    public MimeMessage encryptMessage(MimeMessage message, Address recipient) throws KeyStoreException {
        String alias = keyManagement.getAliasByAddress(recipient);
        X509Certificate certificate = keyManagement.getCertificateForAlias(alias);
        return encryptMessage(message, certificate);
    }

    public MimeMessage encryptMessage(MimeMessage message, X509Certificate certificate) {
        try {
            if(message == null) {
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
                return null;
            }
            return new AsyncEncryptMessage(message, certificate).execute().get();
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Exception in encryptMessage: " + e.getMessage());
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_ERROR_ASYNC_TASK;
            return null;
        }
    }

    public MimeBodyPart encryptBodyPart(MimeBodyPart mimeBodyPart, Address recipient) throws KeyStoreException {
        String alias = keyManagement.getAliasByAddress(recipient);
        X509Certificate certificate = keyManagement.getCertificateForAlias(alias);
        return encryptBodyPart(mimeBodyPart, certificate);
    }

    public MimeBodyPart encryptBodyPart(MimeBodyPart mimePart, X509Certificate certificate) {
        try {
            if(mimePart == null || certificate == null) {
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
                return null;
            }

            return new AsyncEncryptPart(mimePart, certificate).execute().get();
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Exception in encryptMessage: " + e.getMessage());
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_ERROR_ASYNC_TASK;
            return null;
        }
    }

    public MimeBodyPart encryptBodyPartSynchronous(MimeBodyPart mimePart, X509Certificate certificate) {
        SMIMEEnvelopedGenerator envelopedGenerator = new SMIMEEnvelopedGenerator();

        try {
            JceKeyTransRecipientInfoGenerator recipientInfoGen =
                    new JceKeyTransRecipientInfoGenerator(certificate);
            recipientInfoGen.setProvider(BouncyCastleProvider.PROVIDER_NAME);
            envelopedGenerator.addRecipientInfoGenerator(recipientInfoGen);

            JceCMSContentEncryptorBuilder builder =
                    new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES256_CBC);
            builder.setProvider(BouncyCastleProvider.PROVIDER_NAME);
            OutputEncryptor encryptor = builder.build();

            return envelopedGenerator.generate(mimePart, encryptor);
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Exception while encrypting MimeBodyPart: " + e.getMessage());
            e.printStackTrace();
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_UNKNOWN_ERROR;
            return null;
        }
    }

    public MimeMessage encryptMessageSynchronous(MimeMessage message, X509Certificate certificate) {
        SMIMEEnvelopedGenerator envelopedGenerator = new SMIMEEnvelopedGenerator();
        try {
            RecipientInfoGenerator recipientInfoGen = new JceKeyTransRecipientInfoGenerator(certificate);
            envelopedGenerator.addRecipientInfoGenerator(recipientInfoGen);

            OutputEncryptor encryptor = new JceCMSContentEncryptorBuilder(
                    CMSAlgorithm.AES256_CBC).build();

            MimeBodyPart encryptedContent = envelopedGenerator.generate(message, encryptor);

            Properties props = System.getProperties();
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage result = new MimeMessage(session);
            //MimeMessage result = new MimeMessage(message);
            result.setContent(encryptedContent.getContent(), encryptedContent.getContentType());
            result.saveChanges();

            return result;
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error in encryptMessageSynchronous: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public MimeMessage encryptMessageSynchronous(MimeMessage message) {
        SMIMEEnvelopedGenerator envelopedGenerator = new SMIMEEnvelopedGenerator();

        try {
            for (Address recipient : message.getAllRecipients()) {
                ArrayList<X509Certificate> certificates = getCertificatesByAddress((InternetAddress) recipient);
                for (X509Certificate cert : certificates) {
                    RecipientInfoGenerator recipientInfoGen = new JceKeyTransRecipientInfoGenerator(cert);
                    envelopedGenerator.addRecipientInfoGenerator(recipientInfoGen);
                }
            }
            OutputEncryptor encryptor = new JceCMSContentEncryptorBuilder(
                    CMSAlgorithm.AES256_CBC).build();

            MimeBodyPart encryptedContent = envelopedGenerator.generate(message, encryptor);

            Properties props = System.getProperties();
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage result = new MimeMessage(session);
            //MimeMessage result = new MimeMessage(message);
            result.setContent(encryptedContent.getContent(), encryptedContent.getContentType());
            result.saveChanges();

            return result;
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error in encryptMessageSynchronous: " + e.getMessage());
            e.printStackTrace();
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_UNKNOWN_ERROR;
            return null;
        }
    }

    public ArrayList<X509Certificate> getCertificatesByAddress(InternetAddress address) {
        ArrayList<X509Certificate> certificates = new ArrayList<>();
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            Enumeration e = ks.aliases();
            while (e.hasMoreElements()) {
                String alias = (String) e.nextElement();
                X509Certificate c = (X509Certificate) ks.getCertificate(alias);
            /* TODO: Use selector?
                X509CertSelector selector = new X509CertSelector();
                selector.setMatchAllSubjectAltNames(false);
                selector.addSubjectAlternativeName(GeneralName.rfc822Name, address.getAddress()); */
                if(c.getSubjectDN().getName().contains("E="+address.toString()))
                    certificates.add(c);
            }
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error in getCertificates:" + e.getMessage());
            e.printStackTrace();
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_UNKNOWN_ERROR;
        }
        return certificates;
    }

    private class AsyncEncryptMessage extends AsyncTask<Void, Void, MimeMessage> {
        private final MimeMessage mimeMessage;
        private final X509Certificate certificate;

        public AsyncEncryptMessage(final MimeMessage mimeMessage, final X509Certificate certificate) {
            this.mimeMessage = mimeMessage;
            this.certificate = certificate;
        }

        @Override
        protected MimeMessage doInBackground(Void... params) {
            return encryptMessageSynchronous(mimeMessage, certificate);
        }
    }

    private class AsyncEncryptPart extends AsyncTask<Void, Void, MimeBodyPart> {
        private final MimeBodyPart bodyPart;
        private final X509Certificate certificate;

        public AsyncEncryptPart(MimeBodyPart bodyPart, X509Certificate certificate) {
            this.bodyPart = bodyPart;
            this.certificate = certificate;
        }

        @Override
        protected MimeBodyPart doInBackground(Void... params) {
            return encryptBodyPartSynchronous(bodyPart, certificate);
        }
    }
}