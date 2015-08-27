package de.fau.cs.mad.smile.android.encryption.remote;

import android.content.Intent;
import android.os.ParcelFileDescriptor;

import org.spongycastle.cms.CMSException;
import org.spongycastle.mail.smime.SMIMEException;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.x509.CertPathReviewerException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;

import korex.mail.MessagingException;
import korex.mail.internet.MimeBodyPart;

abstract class MimeBodyCryptoOperation extends CryptoOperation<MimeBodyPart> {
    MimeBodyCryptoOperation(Intent data, ParcelFileDescriptor input, ParcelFileDescriptor output) throws IOException {
        super(data, input, output);
    }

    @Override
    MimeBodyPart preProcess() throws FileNotFoundException, MessagingException {
        MimeBodyPart mimeBodyPart = new MimeBodyPart(new FileInputStream(inputFile));
        return mimeBodyPart;
    }

    @Override
    public void execute() throws MessagingException, IOException, OperatorCreationException, GeneralSecurityException, SMIMEException, CMSException, CertPathReviewerException {
        final MimeBodyPart source = preProcess();
        final MimeBodyPart processed = process(source);
        if (processed != null && outputStream != null) {
            processed.writeTo(outputStream);
        }
    }
}
