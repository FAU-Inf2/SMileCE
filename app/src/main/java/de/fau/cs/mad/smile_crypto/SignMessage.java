package de.fau.cs.mad.smile_crypto;

import android.util.Log;

import org.spongycastle.cms.SignerInfoGenerator;
import org.spongycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.spongycastle.operator.bc.BcDigestCalculatorProvider;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

public class SignMessage {
    public SignMessage() {}

    public MimeMultipart sign(MimeBodyPart mimeBodyPart, PrivateKey privateKey, X509Certificate certificate) {
        SMIMEToolkit smimeToolkit = new SMIMEToolkit(new BcDigestCalculatorProvider());
        MimeMultipart signedMimeMultipart = null;
        try {
            Log.d(SMileCrypto.LOG_TAG, "Sign mimeBodyPart.");

            SignerInfoGenerator signerInfoGenerator = new
                    JcaSimpleSignerInfoGeneratorBuilder().setProvider("SC").build("SHA1WITHRSA", privateKey, certificate);

            signedMimeMultipart = smimeToolkit.sign(mimeBodyPart, signerInfoGenerator);
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error signing mimeBodyPart: " + e.getMessage());
        }

        return signedMimeMultipart;
    }

    public MimeBodyPart signEncapsulated(MimeBodyPart mimeBodyPart, PrivateKey privateKey, X509Certificate certificate) {
        SMIMEToolkit smimeToolkit = new SMIMEToolkit(new BcDigestCalculatorProvider());
        MimeBodyPart signedMimeBodyPart = null;
        try {
            Log.d(SMileCrypto.LOG_TAG, "Sign mimeBodyPart.");

            SignerInfoGenerator signerInfoGenerator = new
                    JcaSimpleSignerInfoGeneratorBuilder().setProvider("SC").build("SHA1WITHRSA", privateKey, certificate);

            signedMimeBodyPart = smimeToolkit.signEncapsulated(mimeBodyPart, signerInfoGenerator);
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error signing mimeBodyPart: " + e.getMessage());
        }

        return signedMimeBodyPart;
    }

}
