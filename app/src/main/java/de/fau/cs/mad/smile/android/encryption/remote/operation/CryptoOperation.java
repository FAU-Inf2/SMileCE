package de.fau.cs.mad.smile.android.encryption.remote.operation;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;

import org.spongycastle.cms.CMSException;
import org.spongycastle.mail.smime.SMIMEException;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.x509.CertPathReviewerException;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;

import de.fau.cs.mad.smile.android.encryption.crypto.CryptoParams;
import de.fau.cs.mad.smile.android.encryption.crypto.CryptoParamsLoaderTask;
import de.fau.cs.mad.smile.android.encryption.crypto.KeyManagement;
import de.fau.cs.mad.smile.android.encryption.remote.AbstractContentLoaderTaskBuilder;
import de.fau.cs.mad.smile.android.encryption.remote.ContentLoaderTask;
import de.fau.cs.mad.smime_api.SMimeApi;
import korex.mail.MessagingException;
import korex.mail.internet.AddressException;
import korex.mail.internet.InternetAddress;
import korex.mail.internet.MimeMessage;

public abstract class CryptoOperation<T> implements Closeable {
    protected final String otherParty;
    protected final String identity;
    private final InputStream inputStream;
    protected final OutputStream outputStream;
    protected final Intent result;
    protected final CryptoParamsLoaderTask cryptoParamsLoaderTask;
    protected final ContentLoaderTask<T> contentLoaderTask;

    CryptoOperation(final Intent data, final ParcelFileDescriptor input,
                    final ParcelFileDescriptor output,
                    final AbstractContentLoaderTaskBuilder<T> contentLoaderTaskBuilder)
            throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException,
            NoSuchProviderException, AddressException {
        identity = data.getStringExtra(SMimeApi.EXTRA_IDENTITY);
        otherParty = data.getStringExtra(SMimeApi.EXTRA_OTHERPARTY);
        inputStream = new ParcelFileDescriptor.AutoCloseInputStream(input);

        if (output != null) {
            outputStream = new ParcelFileDescriptor.AutoCloseOutputStream(output);
        } else {
            outputStream = null;
        }

        result = new Intent();

        if (contentLoaderTaskBuilder == null) {
            contentLoaderTask = null;
        } else {
            this.contentLoaderTask = contentLoaderTaskBuilder.setInputStream(getInputStream()).build();
        }

        if (contentLoaderTask != null) {
            contentLoaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        KeyManagement keyManagement = new KeyManagement();
        InternetAddress identityAddress = null;

        if (identity != null) {
            identityAddress = new InternetAddress(identity);
        }

        InternetAddress otherPartyAddress = null;
        if (otherParty != null) {
            otherPartyAddress = new InternetAddress(otherParty);
        }

        cryptoParamsLoaderTask = new CryptoParamsLoaderTask(keyManagement, identityAddress, otherPartyAddress);
        cryptoParamsLoaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public Intent getResult() {
        return result;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    @Override
    public void close() throws IOException {
        getInputStream().close();
        if (outputStream != null) {
            outputStream.close();
        }
    }

    protected T preProcess() throws FileNotFoundException, MessagingException, ExecutionException, InterruptedException {
        return contentLoaderTask.get();
    }

    abstract T process(T message, CryptoParams cryptoParams) throws MessagingException, IOException, GeneralSecurityException, OperatorCreationException, SMIMEException, CMSException, CertPathReviewerException;

    public abstract void execute() throws MessagingException, IOException, GeneralSecurityException, OperatorCreationException, SMIMEException, CMSException, CertPathReviewerException, ExecutionException, InterruptedException;

    protected void copyHeaders(MimeMessage source, MimeMessage target) throws MessagingException {
        Enumeration enumeration = source.getAllHeaderLines();
        while (enumeration.hasMoreElements()) {
            String headerLine = (String) enumeration.nextElement();
            //if (!headerLine.toLowerCase().startsWith("content-")) {
            target.addHeaderLine(headerLine);
            //}
        }
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}


