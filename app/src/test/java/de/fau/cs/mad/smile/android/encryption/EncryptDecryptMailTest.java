package de.fau.cs.mad.smile.android.encryption;

import android.os.AsyncTask;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Properties;

//import de.fau.cs.mad.javax.activation.CommandMap;
//import de.fau.cs.mad.javax.activation.MailcapCommandMap;
import de.fau.cs.mad.smile.android.encryption.crypto.EncryptMail;
import korex.mail.Address;
import korex.mail.Message;
import korex.mail.Session;
import korex.mail.internet.InternetAddress;
import korex.mail.internet.InternetHeaders;
import korex.mail.internet.MimeBodyPart;
import korex.mail.internet.MimeMessage;
import korex.mail.internet.MimeMultipart;

import static org.junit.Assert.*;

/*import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.cms.RecipientId;
import org.spongycastle.cms.RecipientInformation;
import org.spongycastle.cms.RecipientInformationStore;
import org.spongycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.spongycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.spongycastle.cms.jcajce.JceKeyTransRecipientId;
import org.spongycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.mail.smime.SMIMEEnveloped;
import org.spongycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.spongycastle.mail.smime.SMIMEUtil;
import org.spongycastle.util.encoders.Base64;

import java.io.IOException;

import korex.mail.internet.MimeBodyPart;

import java.security.KeyPair;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
*/

@RunWith(RobolectricTestRunner.class)
public class EncryptDecryptMailTest {

    final String content = "Content for MIME-Messages, üäÖß, México 42!";

    @Test
    public void testDecryptMail() throws Exception {
        /*Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
        MimeMessage originalMimeMessage = new AsyncCreateMimeMessage().execute().get();
        Pair<PrivateKey, X509Certificate> cert = new SelfSignedCertificateCreator().createForTest();
        MimeMessage encryptedMimeMessage = encrypt(originalMimeMessage, cert.first, cert.second);

        MimeBodyPart decrypted = decrypt(encryptedMimeMessage, cert.first, cert.second);

        if(decrypted == null) {
            fail("Decrypted is null.");
        }

        MimeMultipart multipart = (MimeMultipart) decrypted.getContent();
        BodyPart part = multipart.getBodyPart(0);

        String decryptedResult = (String) part.getContent();
        if(!decryptedResult.equals(content)){
            System.out.println("FAIL! Decrypted content is: " + decryptedResult);
            for(int i = 0; i < content.length(); i++) {
                if(content.charAt(i) == decryptedResult.charAt(i))
                    System.out.println(i + ". " + content.charAt(i) + "==" + decryptedResult.charAt(i));
                else
                    System.out.println(i + ". " + content.charAt(i) + "!=" + decryptedResult.charAt(i));
            }
            fail();
        }*/

    }

    /*private MimeMessage encrypt(MimeMessage originalMimeMessage, PrivateKey key, X509Certificate cert) throws Exception {
        System.out.println("Start encrypt.");

        EncryptMail encryptMail = new EncryptMail();
        return encryptMail.encryptMessage(originalMimeMessage, cert);
    }

    private class AsyncCreateMimeMessage extends AsyncTask<Void, Void, MimeMessage> {
        protected MimeMessage doInBackground(Void... params) {
            try {
                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
                mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
                mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
                mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
                mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
                mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");
                mc.addMailcap("application/pkcs7-signature;; x-java-content-handler=org.spongyCastle.mail.smime.handlers.pkcs7_signature");
                mc.addMailcap("application/pkcs7-mime;; x-java-content-handler=org.spongyCastle.mail.smime.handlers.pkcs7_mime");
                mc.addMailcap("application/x-pkcs7-signature;; x-java-content-handler=org.spongyCastle.mail.smime.handlers.x_pkcs7_signature");
                mc.addMailcap("application/x-pkcs7-mime;; x-java-content-handler=org.spongyCastle.mail.smime.handlers.x_pkcs7_mime");
                mc.addMailcap("multipart/signed;; x-java-content-handler=org.spongyCastle.mail.smime.handlers.multipart_signed");


                Properties props = System.getProperties();
                Session session = Session.getDefaultInstance(props, null);
                MimeMessage mimeMessage = new MimeMessage(session);

                MimeMultipart multipart = new MimeMultipart("alternative");
                InternetHeaders headers = new InternetHeaders();
                headers.addHeader("Content-Type", "text/plain; charset=utf-8");
                headers.addHeader("Content-Transfer-Encoding", "quoted-printable");
                MimeBodyPart bodyPart = new MimeBodyPart(headers, content.getBytes());
                multipart.addBodyPart(bodyPart);
                mimeMessage.setContent(multipart);
                mimeMessage.saveChanges();

                mimeMessage.addFrom(new Address[]{new InternetAddress("fixmymail@t-online.de")});
                mimeMessage.addRecipients(Message.RecipientType.TO, "SMile@MAD.de");
                mimeMessage.saveChanges();
                return mimeMessage;

            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }
            return null;
        }
    }*/
}