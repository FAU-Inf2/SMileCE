package de.fau.cs.mad.smile.android.encryption.remote.operation;

import android.content.Intent;
import android.os.ParcelFileDescriptor;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import de.fau.cs.mad.smile.android.encryption.crypto.CryptoParams;
import de.fau.cs.mad.smile.android.encryption.crypto.EncryptMail;
import korex.mail.MessagingException;
import korex.mail.internet.AddressException;
import korex.mail.internet.MimeMessage;

public class SignAndEncryptOperation extends SignOperation {
    public SignAndEncryptOperation(Intent data, ParcelFileDescriptor input, ParcelFileDescriptor output) throws IOException, AddressException, CertificateException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException {
        super(data, input, output);
    }

    @Override
    MimeMessage process(MimeMessage message, CryptoParams cryptoParams) throws MessagingException, KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, NoSuchProviderException {
        MimeMessage signedMessage = super.process(message, cryptoParams);
        EncryptMail enigma = new EncryptMail();
        MimeMessage encryptedMessage = enigma.encryptMessage(signedMessage, cryptoParams);

        return encryptedMessage;
    }
}
