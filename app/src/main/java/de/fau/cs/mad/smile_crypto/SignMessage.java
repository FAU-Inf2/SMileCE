package de.fau.cs.mad.smile_crypto;

import android.os.AsyncTask;
import android.util.Log;

import org.spongycastle.cms.SignerInfoGenerator;
import org.spongycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.spongycastle.operator.bc.BcDigestCalculatorProvider;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.mail.Address;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SignMessage {
    public SignMessage() {}

    public MimeMultipart signSynchronous(MimeBodyPart mimeBodyPart, PrivateKey privateKey, X509Certificate certificate) {
        SMIMEToolkit smimeToolkit = new SMIMEToolkit(new BcDigestCalculatorProvider());
        MimeMultipart signedMimeMultipart = null;
        try {
            Log.d(SMileCrypto.LOG_TAG, "Sign mimeBodyPart.");

            SignerInfoGenerator signerInfoGenerator = new
                    JcaSimpleSignerInfoGeneratorBuilder().setProvider("SC").build("SHA256WITHRSA", privateKey, certificate);

            signedMimeMultipart = smimeToolkit.sign(mimeBodyPart, signerInfoGenerator);
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error signing mimeBodyPart: " + e.getMessage());
            e.printStackTrace();
        }

        return signedMimeMultipart;
    }

    public MimeMultipart sign(MimeBodyPart mimeBodyPart, Address signerAddress) {
        if(mimeBodyPart == null) {
            Log.e(SMileCrypto.LOG_TAG, "Could not sign, mimeBodyPart was null.");
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
            return null;
        }
        if(signerAddress == null) {
            Log.e(SMileCrypto.LOG_TAG, "Could not sign, signerAddress was null.");
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
            return null;
        }
        KeyManagement keyManagement;
        PrivateKey privateKey;
        X509Certificate certificate;
        try {
            keyManagement = new KeyManagement();
            ArrayList<String> aliases = keyManagement.getAliasesByOwnAddress(signerAddress);
            if(aliases.size() == 0) {
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_NO_CERTIFICATE_FOUND;
                Log.e(SMileCrypto.LOG_TAG, "No certificate found for signer address: " + signerAddress);
                return null;
            }
            String alias = aliases.get(0); //TODO: best fit --> check validity
            privateKey = keyManagement.getPrivateKeyForAlias(alias, keyManagement.getPassphraseForAlias(alias));
            certificate = keyManagement.getCertificateForAlias(alias);
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error getting info from KeyManagement: " + e.getMessage());
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_UNKNOWN_ERROR;
            return null;
        }
        return sign(mimeBodyPart, privateKey, certificate);
    }

    public MimeMultipart sign(MimeBodyPart mimeBodyPart, PrivateKey privateKey, X509Certificate certificate) {
        if(mimeBodyPart == null) {
            Log.e(SMileCrypto.LOG_TAG, "Could not sign, mimeBodyPart was null.");
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
            return null;
        }
        if(privateKey == null) {
            Log.e(SMileCrypto.LOG_TAG, "Could not sign, privateKey was null.");
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
            return null;
        }
        if(certificate == null) {
            Log.e(SMileCrypto.LOG_TAG, "Could not sign, certificate was null.");
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
            return null;
        }

        Object[] params = new Object[] {mimeBodyPart, privateKey, certificate};

        try {
            return new AsyncSign().execute(params).get();
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Exception in sign: " + e.getMessage());
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_ERROR_ASYNC_TASK;
            return null;
        }
    }

    /* Note: doing this is strongly not recommended as it means a recipient of the message will have to be able to read the signature to read the message.*/
    @Deprecated
    public MimeBodyPart signEncapsulated(MimeBodyPart mimeBodyPart, PrivateKey privateKey, X509Certificate certificate) {
        SMIMEToolkit smimeToolkit = new SMIMEToolkit(new BcDigestCalculatorProvider());
        MimeBodyPart signedMimeBodyPart = null;
        try {
            Log.d(SMileCrypto.LOG_TAG, "Sign mimeBodyPart.");

            SignerInfoGenerator signerInfoGenerator = new
                    JcaSimpleSignerInfoGeneratorBuilder().setProvider("SC").build("SHA256WITHRSA", privateKey, certificate);

            signedMimeBodyPart = smimeToolkit.signEncapsulated(mimeBodyPart, signerInfoGenerator);
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error signing mimeBodyPart: " + e.getMessage());
        }

        return signedMimeBodyPart;
    }

    private class AsyncSign extends AsyncTask<Object, Void, MimeMultipart> {

        @Override
        protected MimeMultipart doInBackground(Object... params) {
            return signSynchronous((MimeBodyPart) params[0], (PrivateKey) params[1], (X509Certificate) params[2]);
        }
    }
}
