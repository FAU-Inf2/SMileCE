package de.fau.cs.mad.smile.android.encryption.remote;

import android.content.Intent;
import android.os.ParcelFileDescriptor;

import java.io.IOException;

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

    public CryptoOperation createDecryptAndVerifyOperation() throws IOException {
        return new DecryptAndVerifyOperation(data, input, output);
    }

    public CryptoOperation createSignAndEncryptOperation() throws IOException {
        return new SignAndEncryptOperation(data, input, output);
    }

    public CryptoOperation createSignOperation() throws IOException {
        return new SignOperation(data, input, output);
    }

    public CryptoOperation createEncryptOperation() throws IOException {
        return new EncryptOperation(data, input, output);
    }

    public CryptoOperation createVerifyOperation() throws IOException {
        return new EncryptOperation(data, input, output);
    }
}