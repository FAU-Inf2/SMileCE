package de.fau.cs.mad.smile.android.encryption.remote;

import android.content.Intent;
import android.os.ParcelFileDescriptor;

import org.spongycastle.cms.CMSException;
import org.spongycastle.mail.smime.SMIMEException;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.x509.CertPathReviewerException;

import java.io.IOException;
import java.security.GeneralSecurityException;

import de.fau.cs.mad.smile.android.encryption.crypto.DecryptMail;
import de.fau.cs.mad.smile.android.encryption.crypto.VerifyMail;
import de.fau.cs.mad.smime_api.SMimeApi;
import korex.mail.MessagingException;
import korex.mail.internet.MimeBodyPart;

class DecryptAndVerifyOperation extends MimeBodyCryptoOperation {

    DecryptAndVerifyOperation(Intent data, ParcelFileDescriptor input, ParcelFileDescriptor output) throws IOException {
        super(data, input, output);
    }

    @Override
    MimeBodyPart process(MimeBodyPart message) throws MessagingException, IOException, GeneralSecurityException, OperatorCreationException, SMIMEException, CMSException, CertPathReviewerException {
        final DecryptMail decryptMail = new DecryptMail();
        final VerifyMail verifyMail = new VerifyMail();
        final MimeBodyPart decryptedPart = decryptMail.decryptMail(message, recipient);
        int resultType = SMimeApi.RESULT_TYPE_UNENCRYPTED_UNSIGNED;

        if (decryptedPart != null) {
            resultType = SMimeApi.RESULT_TYPE_ENCRYPTED;
            int signatureStatus = verifyMail.verifySignature(decryptedPart, sender);
            if (signatureStatus != SMimeApi.RESULT_SIGNATURE_UNSIGNED) {
                resultType |= SMimeApi.RESULT_TYPE_SIGNED;
            }

            result.putExtra(SMimeApi.RESULT_TYPE, resultType);
            result.putExtra(SMimeApi.RESULT_SIGNATURE, signatureStatus);
            result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_SUCCESS);
        } else {
            result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_ERROR);
        }

        return decryptedPart;
    }
}
