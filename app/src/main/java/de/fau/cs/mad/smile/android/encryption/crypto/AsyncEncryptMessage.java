package de.fau.cs.mad.smile.android.encryption.crypto;

import android.os.AsyncTask;
import android.util.Log;

import org.spongycastle.cms.CMSAlgorithm;
import org.spongycastle.cms.RecipientInfoGenerator;
import org.spongycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.spongycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.spongycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.spongycastle.operator.OutputEncryptor;

import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;

import de.fau.cs.mad.smile.android.encryption.SMileCrypto;
import korex.mail.Session;
import korex.mail.internet.MimeBodyPart;
import korex.mail.internet.MimeMessage;

public class AsyncEncryptMessage extends AsyncTask<Void, Void, MimeMessage> {
    static {
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
    }

    private final MimeMessage mimeMessage;
    private final List<X509Certificate> certificates;

    public AsyncEncryptMessage(final MimeMessage mimeMessage, final List<X509Certificate> certificates) {
        this.mimeMessage = mimeMessage;
        this.certificates = certificates;
    }

    @Override
    protected MimeMessage doInBackground(Void... params) {
        return encryptMessage();
    }

    private MimeMessage encryptMessage() {
        SMIMEEnvelopedGenerator envelopedGenerator = new SMIMEEnvelopedGenerator();
        try {
            for(X509Certificate certificate : certificates) {
                RecipientInfoGenerator recipientInfoGen = new JceKeyTransRecipientInfoGenerator(certificate);
                envelopedGenerator.addRecipientInfoGenerator(recipientInfoGen);
            }

            OutputEncryptor encryptor = new JceCMSContentEncryptorBuilder(
                    CMSAlgorithm.AES256_CBC).build();

            MimeBodyPart encryptedContent = envelopedGenerator.generate(mimeMessage, encryptor);

            Properties props = System.getProperties();
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage result = new MimeMessage(session);
            //MimeMessage result = new MimeMessage(message);
            result.setContent(encryptedContent.getContent(), encryptedContent.getContentType());
            result.saveChanges();

            return result;
        } catch (Exception e) {
            if(SMileCrypto.isDEBUG()) {
                Log.e(SMileCrypto.LOG_TAG, "Error in encryptMessage: " + e.getMessage());
            }
            //e.printStackTrace();
            return null;
        }
    }
}
