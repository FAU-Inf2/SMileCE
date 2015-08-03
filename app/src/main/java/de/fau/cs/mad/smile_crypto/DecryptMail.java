package de.fau.cs.mad.smile_crypto;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Properties;

import org.spongycastle.asn1.nist.NISTObjectIdentifiers;
import org.spongycastle.cms.RecipientId;
import org.spongycastle.cms.RecipientInformation;
import org.spongycastle.cms.RecipientInformationStore;
import org.spongycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.spongycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.spongycastle.cms.jcajce.JceKeyTransRecipientId;
import org.spongycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.spongycastle.mail.smime.SMIMEEnvelopedParser;
import org.spongycastle.mail.smime.SMIMEUtil;
import org.spongycastle.operator.bc.BcDigestCalculatorProvider;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class DecryptMail {
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }
    final String global_alias = "SMile_crypto_1438271102196"; //hardcoded just for testing

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

    public void startEncDecMail() {
        new AsyncEncDecMail().execute();
    }

    public void testEncDecMail(String alias, MimeBodyPart msg) {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
            PrivateKey privateKey = ((KeyStore.PrivateKeyEntry) ks.getEntry(alias, null)).getPrivateKey();

            SMIMEToolkit toolkit = new SMIMEToolkit(new BcDigestCalculatorProvider());

            MimeMessage message = makeMimeMessage(msg);
            MimeBodyPart res = toolkit.encrypt(message,
                    new JceCMSContentEncryptorBuilder(NISTObjectIdentifiers.id_aes128_CBC).setProvider("SC").build(),
                    new JceKeyTransRecipientInfoGenerator(cert).setProvider("SC"));

            //Assert.assertTrue(toolkit.isEncrypted(res));
            MimeMessage body = makeMimeMessage(res);
            MimeBodyPart dec = toolkit.decrypt(body, new JceKeyTransRecipientId(cert),
                    new JceKeyTransEnvelopedRecipient(privateKey).setProvider("SC"));

            Log.d(SMileCrypto.LOG_TAG, "MESSAGE: " +  message);
            Log.d(SMileCrypto.LOG_TAG, "DECRYPT: " + dec);
        } catch (Exception e) {
            Log.d(SMileCrypto.LOG_TAG, "Error in testEncDecMail: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private MimeMessage makeMimeMessage(MimeBodyPart res)
            throws MessagingException, IOException {
        Properties props = System.getProperties();
        Session session = Session.getDefaultInstance(props, null);

        Address fromUser = new InternetAddress("\"Eric H. Echidna\"<eric@bouncycastle.org>");
        Address toUser = new InternetAddress("example@bouncycastle.org");

        MimeMessage body = new MimeMessage(session);
        body.setFrom(fromUser);
        body.setRecipient(Message.RecipientType.TO, toUser);
        body.setSubject("example message");
        body.setContent(res.getContent(), res.getContentType());
        body.saveChanges();
        return body;
    }

    private class AsyncEncDecMail extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                MimeBodyPart messagePart = new MimeBodyPart();
                messagePart.setText("You message's string content goes here.", "utf-8");

                testEncDecMail(global_alias, messagePart);
            } catch (Exception e) {
                Log.e(SMileCrypto.LOG_TAG, "Error in doInBackground" + e.getMessage());
            }
            return null;
        }
    }
}
