package de.fau.cs.mad.smile.android.encryption.remote;

import android.content.Intent;
import android.os.ParcelFileDescriptor;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import de.fau.cs.mad.smile.android.encryption.crypto.EncryptMail;
import korex.mail.internet.AddressException;
import korex.mail.internet.InternetAddress;
import korex.mail.internet.MimeMessage;

class EncryptOperation extends MimeMessageCryptoOperation {
    public EncryptOperation(Intent data, ParcelFileDescriptor input, ParcelFileDescriptor output) throws IOException {
        super(data, input, output);
    }

    @Override
    MimeMessage process(MimeMessage message) throws AddressException, KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        EncryptMail enigma = new EncryptMail();
        MimeMessage encryptedMessage = enigma.encryptMessage(message, new InternetAddress(recipient));
        return encryptedMessage;
    }
}
