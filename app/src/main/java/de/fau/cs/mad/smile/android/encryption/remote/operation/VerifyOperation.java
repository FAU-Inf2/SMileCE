package de.fau.cs.mad.smile.android.encryption.remote.operation;

import android.content.Intent;
import android.os.ParcelFileDescriptor;

import org.spongycastle.cms.CMSException;
import org.spongycastle.mail.smime.SMIMEException;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.x509.CertPathReviewerException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import de.fau.cs.mad.smile.android.encryption.crypto.CryptoParams;
import de.fau.cs.mad.smile.android.encryption.crypto.VerifyMail;
import de.fau.cs.mad.smime_api.SMimeApi;
import korex.mail.MessagingException;
import korex.mail.internet.AddressException;
import korex.mail.internet.MimeBodyPart;

public class VerifyOperation extends MimeBodyCryptoOperation {
    public VerifyOperation(Intent data, ParcelFileDescriptor input, ParcelFileDescriptor output) throws IOException, AddressException, CertificateException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException {
        super(data, input, output);
    }

    @Override
    MimeBodyPart process(MimeBodyPart message, CryptoParams cryptoParams) throws MessagingException, IOException, GeneralSecurityException, OperatorCreationException, SMIMEException, CMSException, CertPathReviewerException {
        int resultType = SMimeApi.RESULT_TYPE_UNENCRYPTED_UNSIGNED;
        final VerifyMail verifyMail = new VerifyMail();
        int signatureStatus = verifyMail.verifySignature(message, otherParty);
        if (signatureStatus != SMimeApi.RESULT_SIGNATURE_UNSIGNED) {
            resultType |= SMimeApi.RESULT_TYPE_SIGNED;
        }

        result.putExtra(SMimeApi.RESULT_TYPE, resultType);
        result.putExtra(SMimeApi.RESULT_SIGNATURE, signatureStatus);
        result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_SUCCESS);
        return null;
    }
}
