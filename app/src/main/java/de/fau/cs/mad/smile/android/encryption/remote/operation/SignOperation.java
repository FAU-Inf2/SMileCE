package de.fau.cs.mad.smile.android.encryption.remote.operation;

import android.content.Intent;
import android.os.ParcelFileDescriptor;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.Properties;

import de.fau.cs.mad.smile.android.encryption.crypto.CryptoParams;
import de.fau.cs.mad.smile.android.encryption.crypto.SignMessage;
import korex.mail.MessagingException;
import korex.mail.Session;
import korex.mail.internet.AddressException;
import korex.mail.internet.MimeBodyPart;
import korex.mail.internet.MimeMessage;
import korex.mail.internet.MimeMultipart;

public class SignOperation extends MimeMessageCryptoOperation {
    public SignOperation(Intent data, ParcelFileDescriptor input, ParcelFileDescriptor output) throws IOException, AddressException, CertificateException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException {
        super(data, input, output);
    }

    @Override
    MimeMessage process(MimeMessage message, CryptoParams cryptoParams) throws MessagingException, KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, NoSuchProviderException {
        SignMessage signer = new SignMessage();
        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setContent(message.getContent(), message.getContentType());
        MimeMultipart signedPart = signer.sign(bodyPart, cryptoParams.getIdentity());
        Properties props = System.getProperties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage result = new MimeMessage(session);
        //addDataHandlers(result);
        result.setContent(signedPart, signedPart.getContentType());
        result.saveChanges();
        return result;
    }
}
