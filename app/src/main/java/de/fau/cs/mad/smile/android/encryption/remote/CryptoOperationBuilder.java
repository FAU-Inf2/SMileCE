package de.fau.cs.mad.smile.android.encryption.remote;

import android.content.Intent;
import android.os.ParcelFileDescriptor;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import de.fau.cs.mad.smile.android.encryption.remote.operation.CryptoOperation;
import de.fau.cs.mad.smile.android.encryption.remote.operation.DecryptAndVerifyOperation;
import de.fau.cs.mad.smile.android.encryption.remote.operation.EncryptOperation;
import de.fau.cs.mad.smile.android.encryption.remote.operation.SignAndEncryptOperation;
import de.fau.cs.mad.smile.android.encryption.remote.operation.SignOperation;
import de.fau.cs.mad.smile.android.encryption.remote.operation.VerifyOperation;
import korex.mail.internet.AddressException;

public class CryptoOperationBuilder {
    private Intent data;
    private ParcelFileDescriptor input;
    private ParcelFileDescriptor output;

    public CryptoOperationBuilder setData(Intent data) {
        this.data = data;
        return this;
    }

    public CryptoOperationBuilder setInput(ParcelFileDescriptor input) {
        this.input = input;
        return this;
    }

    public CryptoOperationBuilder setOutput(ParcelFileDescriptor output) {
        this.output = output;
        return this;
    }

    public CryptoOperation createDecryptAndVerifyOperation() throws IOException, AddressException, CertificateException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException {
        return new DecryptAndVerifyOperation(data, input, output);
    }

    public CryptoOperation createSignAndEncryptOperation() throws IOException, AddressException, CertificateException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException {
        return new SignAndEncryptOperation(data, input, output);
    }

    public CryptoOperation createSignOperation() throws IOException, AddressException, CertificateException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException {
        return new SignOperation(data, input, output);
    }

    public CryptoOperation createEncryptOperation() throws IOException, AddressException, CertificateException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException {
        return new EncryptOperation(data, input, output);
    }

    public CryptoOperation createVerifyOperation() throws IOException, AddressException, CertificateException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException {
        return new VerifyOperation(data, input, output);
    }
}