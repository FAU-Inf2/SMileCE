package de.fau.cs.mad.smile.android.encryption.remote;

import android.content.Context;
import android.content.Intent;
import android.os.ParcelFileDescriptor;

import org.apache.commons.io.FileUtils;
import org.spongycastle.cms.CMSException;
import org.spongycastle.mail.smime.SMIMEException;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.x509.CertPathReviewerException;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Enumeration;

import de.fau.cs.mad.smile.android.encryption.App;
import de.fau.cs.mad.smime_api.SMimeApi;
import korex.activation.CommandMap;
import korex.activation.MailcapCommandMap;
import korex.mail.MessagingException;
import korex.mail.internet.MimeMessage;

abstract class CryptoOperation<T> implements Closeable {
    protected final String recipient;
    protected final String sender;
    private final InputStream inputStream;
    protected final OutputStream outputStream;
    protected final File inputFile;
    protected final Intent result;

    CryptoOperation(final Intent data, final ParcelFileDescriptor input, final ParcelFileDescriptor output) throws IOException {
        sender = data.getStringExtra(SMimeApi.EXTRA_SENDER);
        recipient = data.getStringExtra(SMimeApi.EXTRA_RECIPIENT);
        inputStream = new ParcelFileDescriptor.AutoCloseInputStream(input);

        if (output != null) {
            outputStream = new ParcelFileDescriptor.AutoCloseOutputStream(output);
        } else {
            outputStream = null;
        }

        inputFile = copyToFile(inputStream);
        result = new Intent();
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
        inputStream.close();
        if (outputStream != null) {
            outputStream.close();
        }
        inputFile.delete();
    }

    abstract T preProcess() throws FileNotFoundException, MessagingException;

    abstract T process(T message) throws MessagingException, IOException, GeneralSecurityException, OperatorCreationException, SMIMEException, CMSException, CertPathReviewerException;

    public abstract void execute() throws MessagingException, IOException, GeneralSecurityException, OperatorCreationException, SMIMEException, CMSException, CertPathReviewerException;

    protected void copyHeaders(MimeMessage source, MimeMessage target) throws MessagingException {
        Enumeration enumeration = source.getAllHeaderLines();
        while (enumeration.hasMoreElements()) {
            String headerLine = (String) enumeration.nextElement();
            //if (!headerLine.toLowerCase().startsWith("content-")) {
            target.addHeaderLine(headerLine);
            //}
        }
    }

    protected void addDataHandlers(MimeMessage mimeMessage) throws MessagingException {
        MailcapCommandMap commandMap = (MailcapCommandMap) CommandMap.getDefaultCommandMap();

        commandMap.addMailcap("application/pkcs7-signature;; x-java-content-handler=org.spongycastle.mail.smime.handlers.pkcs7_signature");
        commandMap.addMailcap("application/pkcs7-mime;; x-java-content-handler=org.spongycastle.mail.smime.handlers.pkcs7_mime");
        commandMap.addMailcap("application/x-pkcs7-signature;; x-java-content-handler=org.spongycastle.mail.smime.handlers.x_pkcs7_signature");
        commandMap.addMailcap("application/x-pkcs7-mime;; x-java-content-handler=org.spongycastle.mail.smime.handlers.x_pkcs7_mime");
        commandMap.addMailcap("multipart/signed;; x-java-content-handler=org.spongycastle.mail.smime.handlers.multipart_signed");
        commandMap.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        commandMap.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        commandMap.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        commandMap.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        commandMap.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        mimeMessage.getDataHandler().setCommandMap(commandMap);
    }

    private File copyToFile(InputStream inputStream) throws IOException {
        File targetDir = App.getContext().getApplicationContext().getDir("service-messages", Context.MODE_PRIVATE);
        File targetFile = null;
        int fileNumber = 1;

        do {
            targetFile = new File(targetDir, String.format("%05d", fileNumber++));
        } while (targetFile.exists());

        FileUtils.copyInputStreamToFile(inputStream, targetFile);
        return targetFile;
    }
}
