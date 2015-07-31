package de.fau.cs.mad.smile_crypto;

import android.util.Log;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Properties;

import org.spongycastle.cms.RecipientId;
import org.spongycastle.cms.RecipientInformation;
import org.spongycastle.cms.RecipientInformationStore;
import org.spongycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.spongycastle.cms.jcajce.JceKeyTransRecipientId;
import org.spongycastle.mail.smime.SMIMEEnveloped;
import org.spongycastle.mail.smime.SMIMEUtil;

import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

public class DecryptMail {
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    public DecryptMail() {
    }

    public void decryptMail(String alias) {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);

            Log.d(SMileCrypto.LOG_TAG, "Alias is: " + alias);

            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
            RecipientId recId = new JceKeyTransRecipientId(cert);

            //
            // Get a Session object with the default properties.
            //
            Properties props = System.getProperties();
            Session session = Session.getDefaultInstance(props, null);

            MimeMessage msg = new MimeMessage(session, new FileInputStream("/storage/emulated/0/smime.p7m")); //just for testing
            SMIMEEnveloped m = new SMIMEEnveloped(msg);
            RecipientInformationStore recipients = m.getRecipientInfos();
            RecipientInformation recipient = recipients.get(recId);

            byte[] content = recipient.getContent(new JceKeyTransEnvelopedRecipient(
                    ((KeyStore.PrivateKeyEntry) ks.getEntry(alias, null)).getPrivateKey()).setProvider("SC"));

            MimeBodyPart res = SMIMEUtil.toMimeBodyPart(content);

            Log.d(SMileCrypto.LOG_TAG, "Decrypted Message:\n");
            Log.d(SMileCrypto.LOG_TAG, res.getContent().toString());

        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error in DecryptMail: " + e.getMessage());
        }
    }
}
