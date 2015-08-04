package de.fau.cs.mad.smile_crypto;


import android.util.Log;

import org.spongycastle.cms.RecipientId;
import org.spongycastle.cms.RecipientInformation;
import org.spongycastle.cms.RecipientInformationStore;
import org.spongycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.spongycastle.cms.jcajce.JceKeyTransRecipientId;
import org.spongycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.spongycastle.mail.smime.SMIMEEnvelopedParser;
import org.spongycastle.mail.smime.SMIMEUtil;

import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Properties;

import de.fau.cs.mad.javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

public class EncryptMail {
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    public MimeMessage encrypt(String alias, MimeMessage msg) {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);

            Log.d(SMileCrypto.LOG_TAG, "Alias is: " + alias);

            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
            RecipientId recId = new JceKeyTransRecipientId(cert);
            Log.d(SMileCrypto.LOG_TAG, "Got recId");

            SMIMEEnvelopedGenerator gen = new SMIMEEnvelopedGenerator();
            gen.addKeyTransRecipient(cert); // TODO: <- Deprecated search alternative
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setDataHandler(msg.getDataHandler());
            mbp.setHeader("Content-Type", "application/octet-stream");
            mbp.setHeader("Content-Transfer-Encoding", "binary");
            MimeBodyPart mp = gen.generate(mbp, SMIMEEnvelopedGenerator.RC2_CBC, "SC");

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

            MimeMessage body = new MimeMessage(msg);
            body.setContent(mp.getContent(), mp.getContentType());
            return body;


        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error in EncryptMail: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
