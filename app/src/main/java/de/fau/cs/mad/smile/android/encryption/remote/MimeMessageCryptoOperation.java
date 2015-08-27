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
import java.util.Properties;

import de.fau.cs.mad.smime_api.SMimeApi;
import korex.mail.MessagingException;
import korex.mail.Session;
import korex.mail.internet.MimeMessage;

abstract class MimeMessageCryptoOperation extends CryptoOperation<MimeMessage> {

    MimeMessageCryptoOperation(Intent data, ParcelFileDescriptor input, ParcelFileDescriptor output) throws IOException {
        super(data, input, output);
    }

    @Override
    MimeMessage preProcess() throws FileNotFoundException, MessagingException {
        Properties props = System.getProperties();
        Session session = Session.getDefaultInstance(props, null);
        return new MimeMessage(session, new FileInputStream(inputFile));
    }

    @Override
    public void execute() throws MessagingException, IOException, GeneralSecurityException, OperatorCreationException, SMIMEException, CMSException, CertPathReviewerException {
        final MimeMessage source = preProcess();
        final MimeMessage processed = process(source);
        copyHeaders(source, processed);
        processed.saveChanges();
        processed.writeTo(outputStream);
        result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_SUCCESS);
    }
}
