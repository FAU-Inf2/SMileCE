package de.fau.cs.mad.smile_crypto;

import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
import org.spongycastle.mail.smime.SMIMEEnvelopedParser;
import org.spongycastle.mail.smime.SMIMEUtil;
import org.spongycastle.mail.smime.util.SharedFileInputStream;

import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

public class DecryptMail {
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    public DecryptMail() {
    }

    public MimeBodyPart decryptMail(String alias, MimeMessage msg) {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);

            Log.d(SMileCrypto.LOG_TAG, "Alias is: " + alias);

            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
            RecipientId recId = new JceKeyTransRecipientId(cert);
            Log.d(SMileCrypto.LOG_TAG, "Got recId");

            //
            // Get a Session object with the default properties.
            //
            Properties props = System.getProperties();
            Session session = Session.getDefaultInstance(props, null);
            if (session == null) {
                Log.d(SMileCrypto.LOG_TAG, "Session is null.");
                return null;
            }
            Log.d(SMileCrypto.LOG_TAG, "Got session.");

            //File file = new File("/storage/emulated/0/smime.p7m"); //hardcoded just for testing
            //MimeMessage msg = new MimeMessage(session, new FileInputStream(file));
            //MimeBodyPart msg = new MimeBodyPart(new SharedFileInputStream("/storage/emulated/0/smime.p7m"));
            SMIMEEnvelopedParser m = new SMIMEEnvelopedParser(msg); // <-- TODO: Fails here
            Log.d(SMileCrypto.LOG_TAG, "...never shown...");

            RecipientInformationStore recipients = m.getRecipientInfos();
            RecipientInformation recipient = recipients.get(recId);

            if(recipient == null) {
                Log.d(SMileCrypto.LOG_TAG, "Recipient is null");
                return null;
            }
            Log.d(SMileCrypto.LOG_TAG, "Recipient OKAY");

            byte[] content = recipient.getContent(new JceKeyTransEnvelopedRecipient(
                    ((KeyStore.PrivateKeyEntry) ks.getEntry(alias, null)).getPrivateKey()).setProvider("SC"));

            if(content == null) {
                Log.d(SMileCrypto.LOG_TAG, "Content is null");
                return null;
            }
            Log.d(SMileCrypto.LOG_TAG, "Content OKAY");

            MimeBodyPart res = SMIMEUtil.toMimeBodyPart(content);

            Log.d(SMileCrypto.LOG_TAG, "Decrypted Message:\n");
            Log.d(SMileCrypto.LOG_TAG, res.getContent().toString());

            return res;

        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error in DecryptMail: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
