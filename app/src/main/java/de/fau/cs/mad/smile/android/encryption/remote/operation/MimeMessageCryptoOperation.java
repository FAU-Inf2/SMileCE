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
import java.util.concurrent.ExecutionException;

import de.fau.cs.mad.smile.android.encryption.crypto.CryptoParams;
import de.fau.cs.mad.smile.android.encryption.remote.MimeMessageLoaderTaskBuilder;
import de.fau.cs.mad.smime_api.SMimeApi;
import korex.mail.MessagingException;
import korex.mail.internet.AddressException;
import korex.mail.internet.MimeMessage;

public abstract class MimeMessageCryptoOperation extends CryptoOperation<MimeMessage> {

    MimeMessageCryptoOperation(Intent data, ParcelFileDescriptor input, ParcelFileDescriptor output) throws IOException, AddressException, CertificateException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException {
        super(data, input, output, new MimeMessageLoaderTaskBuilder());
    }

    @Override
    public void execute() throws MessagingException, IOException, GeneralSecurityException, OperatorCreationException, SMIMEException, CMSException, CertPathReviewerException, ExecutionException, InterruptedException {
        final MimeMessage source = preProcess();
        final CryptoParams cryptoParams = cryptoParamsLoaderTask.get();
        final MimeMessage processed = process(source, cryptoParams);
        copyHeaders(source, processed);
        processed.saveChanges();
        processed.writeTo(outputStream);
        result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_SUCCESS);
    }
}
