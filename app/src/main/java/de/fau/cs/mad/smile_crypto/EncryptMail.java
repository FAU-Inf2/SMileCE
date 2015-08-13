package de.fau.cs.mad.smile_crypto;


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
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

public class EncryptMail {
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    public MimeBodyPart encryptBodyPart(MimeBodyPart mimePart, X509Certificate certificate) {
        SMIMEEnvelopedGenerator envelopedGenerator = new SMIMEEnvelopedGenerator();
        try {
            RecipientInfoGenerator recipientInfoGen = new JceKeyTransRecipientInfoGenerator(certificate);
            envelopedGenerator.addRecipientInfoGenerator(recipientInfoGen);

            OutputEncryptor encryptor = new JceCMSContentEncryptorBuilder(
                    CMSAlgorithm.AES256_CBC).build();

            return envelopedGenerator.generate(mimePart, encryptor);
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Exception while encrypting MimeBodyPart: " + e.getMessage());
            return null;
        }
    }

    public MimeMessage encryptMessage(MimeMessage message, X509Certificate certificate) {
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
            Log.e(SMileCrypto.LOG_TAG, "Error in encryptMessage: " + e.getMessage());
            return null;
        }
    }

    public MimeMessage encryptMessage(MimeMessage message) {
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
            Log.e(SMileCrypto.LOG_TAG, "Error in encryptMessage: " + e.getMessage());
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
}
