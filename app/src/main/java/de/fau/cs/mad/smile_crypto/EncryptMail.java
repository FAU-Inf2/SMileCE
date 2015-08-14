package de.fau.cs.mad.smile_crypto;


import android.os.AsyncTask;
import android.util.Log;

import org.spongycastle.asn1.x509.GeneralName;
import org.spongycastle.cms.CMSAlgorithm;
import org.spongycastle.cms.RecipientInfoGenerator;
import org.spongycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.spongycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.spongycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.spongycastle.operator.OutputEncryptor;

import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

public class EncryptMail {
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    public MimeMessage encryptMessage(MimeMessage message) {
        try {
            if(message == null) {
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
                return null;
            }
            return new AsyncEncryptMessage().execute(message).get();
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Exception in encryptMessage: " + e.getMessage());
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_ERROR_ASYNC_TASK;
            return null;
        }
    }

    public MimeMessage encryptMessage(MimeMessage message, X509Certificate certificate) {
        try {
            if(message == null) {
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
                return null;
            }
            return new AsyncEncryptMessage().execute(message, certificate).get();
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Exception in encryptMessage: " + e.getMessage());
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_ERROR_ASYNC_TASK;
            return null;
        }
    }

    public MimeBodyPart encryptBodyPart(MimeBodyPart mimePart, X509Certificate certificate) {
        try {
            if(mimePart == null || certificate == null) {
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
                return null;
            }

            return new AsyncEncryptPart().execute(mimePart, certificate).get();
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Exception in encryptMessage: " + e.getMessage());
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_ERROR_ASYNC_TASK;
            return null;
        }
    }

    public MimeBodyPart encryptBodyPartSynchronous(MimeBodyPart mimePart, X509Certificate certificate) {
        SMIMEEnvelopedGenerator envelopedGenerator = new SMIMEEnvelopedGenerator();
        try {
            RecipientInfoGenerator recipientInfoGen = new JceKeyTransRecipientInfoGenerator(certificate);
            envelopedGenerator.addRecipientInfoGenerator(recipientInfoGen);

            OutputEncryptor encryptor = new JceCMSContentEncryptorBuilder(
                    CMSAlgorithm.AES256_CBC).build();

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

            MimeMessage result = new MimeMessage(message);
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

            MimeMessage result = new MimeMessage(message);
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

    public MimeBodyPart encryptAndSign(MimeMessage mimeMessage) {
        SignMessage signMessage = new SignMessage();
        MimeMessage encryptedMimeMessage = encryptMessage(mimeMessage);

        try {
            MimeBodyPart mimeBodyPart = (MimeBodyPart) encryptedMimeMessage.getContent();
            //TODO: correct cast??

            return signMessage.signEncapsulated(mimeBodyPart, null, null);
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error in encryptAndSign: " + e.getMessage());
            return null;
        }
    }

    private class AsyncEncryptMessage extends AsyncTask<Object, Void, MimeMessage> {
        @Override
        protected MimeMessage doInBackground(Object... params) {
            if(params.length == 1)
                return encryptMessageSynchronous((MimeMessage) params[0]);
            else if(params.length == 2)
                return encryptMessageSynchronous((MimeMessage) params[0], (X509Certificate) params[1]);
            else
                return null;
        }
    }

    private class AsyncEncryptPart extends AsyncTask<Object, Void, MimeBodyPart> {
        @Override
        protected MimeBodyPart doInBackground(Object... params) {
            if(params.length == 2)
                return encryptBodyPartSynchronous((MimeBodyPart) params[0], (X509Certificate) params[1]);
            else
                return null;
        }
    }
}