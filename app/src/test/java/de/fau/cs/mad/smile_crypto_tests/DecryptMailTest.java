package de.fau.cs.mad.smile_crypto_tests;

import org.junit.Before;
import org.junit.Test;
import org.spongycastle.asn1.ASN1ObjectIdentifier;
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

import javax.mail.internet.MimeBodyPart;

import java.security.KeyPair;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import static org.junit.Assert.*;

public class DecryptMailTest {

    /*private static final String BC = BouncyCastleProvider.PROVIDER_NAME;

    private static String          _signDN;
    private static KeyPair _signKP;

    private static String          _reciDN;
    private static KeyPair         _reciKP;
    private static X509Certificate _reciCert;

    private static String          _reciDN2;
    private static KeyPair         _reciKP2;
    private static X509Certificate _reciCert2;

    private static final byte[] testMessage = Base64.decode(
            "TUlNRS1WZXJzaW9uOiAxLjANCkNvbnRlbnQtVHlwZTogbXVsdGlwYXJ0L21peGVkOyANCglib3VuZGFye" +
                    "T0iLS0tLT1fUGFydF8wXzI2MDM5NjM4Ni4xMzUyOTA0NzUwMTMyIg0KQ29udGVudC1MYW5ndWFnZTogZW" +
                    "4NCkNvbnRlbnQtRGVzY3JpcHRpb246IEEgbWFpbCBmb2xsb3dpbmcgdGhlIERJUkVDVCBwcm9qZWN0IHN" +
                    "wZWNpZmljYXRpb25zDQoNCi0tLS0tLT1fUGFydF8wXzI2MDM5NjM4Ni4xMzUyOTA0NzUwMTMyDQpDb250" +
                    "ZW50LVR5cGU6IHRleHQvcGxhaW47IG5hbWU9bnVsbDsgY2hhcnNldD11cy1hc2NpaQ0KQ29udGVudC1Uc" +
                    "mFuc2Zlci1FbmNvZGluZzogN2JpdA0KQ29udGVudC1EaXNwb3NpdGlvbjogaW5saW5lOyBmaWxlbmFtZT" +
                    "1udWxsDQoNCkNpYW8gZnJvbSB2aWVubmENCi0tLS0tLT1fUGFydF8wXzI2MDM5NjM4Ni4xMzUyOTA0NzU" +
                    "wMTMyLS0NCg==");


    @Before
    public void setUp() throws Exception {
        _signDN   = "O=Bouncy Castle, C=AU";
        _signKP   = CMSTestUtil.makeKeyPair();

        _reciDN   = "CN=Doug, OU=Sales, O=Bouncy Castle, C=AU";
        _reciKP   = CMSTestUtil.makeKeyPair();
        _reciCert = CMSTestUtil.makeCertificate(_reciKP, _reciDN, _signKP, _signDN);

        _reciDN2   = "CN=Fred, OU=Sales, O=Bouncy Castle, C=AU";
        _reciKP2   = CMSTestUtil.makeKeyPair();
        _reciCert2 = CMSTestUtil.makeCertificate(_reciKP2, _reciDN2, _signKP, _signDN);
    }

    private RecipientId getRecipientId(
            X509Certificate cert)
            throws IOException, CertificateEncodingException
    {
        RecipientId recId = new JceKeyTransRecipientId(cert);

        return recId;
    }*/

    @Test
    public void testDecryptMail() throws Exception {
        /*SMIMEEnvelopedGenerator gen = new SMIMEEnvelopedGenerator();

        gen.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator(_reciCert).setProvider(BC));

        //
        // generate a MimeBodyPart object which encapsulates the content
        // we want encrypted.
        //

        MimeBodyPart mp = gen.generate(msg, new JceCMSContentEncryptorBuilder(new ASN1ObjectIdentifier(algorithmOid)).setProvider(BC).build());
        SMIMEEnveloped m = new SMIMEEnveloped(mp);
        RecipientId recId = getRecipientId(_reciCert);

        RecipientInformationStore recipients = m.getRecipientInfos();
        RecipientInformation recipient = recipients.get(recId);

        MimeBodyPart    res = SMIMEUtil.toMimeBodyPart(recipient.getContent(new JceKeyTransEnvelopedRecipient(_reciKP.getPrivate()).setProvider(BC)));*/


        assertFalse(false);
    }
}