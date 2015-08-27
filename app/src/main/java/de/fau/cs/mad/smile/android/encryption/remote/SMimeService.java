package de.fau.cs.mad.smile.android.encryption.remote;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.Properties;

import de.fau.cs.mad.smile.android.encryption.crypto.EncryptMail;
import de.fau.cs.mad.smile.android.encryption.SMileCrypto;
import de.fau.cs.mad.smile.android.encryption.App;
import de.fau.cs.mad.smile.android.encryption.crypto.DecryptMail;
import de.fau.cs.mad.smile.android.encryption.crypto.SignMessage;
import de.fau.cs.mad.smile.android.encryption.crypto.SignatureCheck;
import de.fau.cs.mad.smime_api.ISMimeService;
import de.fau.cs.mad.smime_api.SMimeApi;
import korex.activation.CommandMap;
import korex.activation.MailcapCommandMap;
import korex.mail.MessagingException;
import korex.mail.Session;
import korex.mail.internet.AddressException;
import korex.mail.internet.InternetAddress;
import korex.mail.internet.MimeBodyPart;
import korex.mail.internet.MimeMessage;
import korex.mail.internet.MimeMultipart;

public class SMimeService extends Service {

    private final ISMimeService.Stub mBinder = new ISMimeService.Stub() {
        @Override
        public Intent execute(Intent data, ParcelFileDescriptor input, ParcelFileDescriptor output) throws RemoteException {
            String action = data.getAction();
            switch (action) {
                case SMimeApi.ACTION_SIGN:
                    return sign(data, input, output);
                case SMimeApi.ACTION_ENCRYPT:
                    return encrypt(data, input, output);
                case SMimeApi.ACTION_ENCRYPT_AND_SIGN:
                    return encryptAndSign(data, input, output);
                case SMimeApi.ACTION_DECRYPT_VERIFY:
                    return decryptAndVerify(data, input, output);
                default:
                    return null;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final Intent decryptAndVerify(final Intent data, final ParcelFileDescriptor input, final ParcelFileDescriptor output) {
        final InputStream inputStream = new ParcelFileDescriptor.AutoCloseInputStream(input);
        final OutputStream outputStream = new ParcelFileDescriptor.AutoCloseOutputStream(output);
        final String recipient = data.getStringExtra(SMimeApi.EXTRA_RECIPIENT);
        final String sender = data.getStringExtra(SMimeApi.EXTRA_SENDER);
        File encryptedFile = null;

        final Intent result = new Intent();

        try {
            //encryptedFile = copyToFile(inputStream);

            final DecryptMail decryptMail = new DecryptMail();
            final SignatureCheck verifyMail = new SignatureCheck();
            MimeBodyPart mimeBodyPart = new MimeBodyPart(inputStream);
            MimeBodyPart decryptedPart = decryptMail.decryptMail(mimeBodyPart, recipient);

            if (decryptedPart != null) {
                decryptedPart.writeTo(outputStream);
                int resultType = SMimeApi.RESULT_TYPE_ENCRYPTED;
                int signatureStatus = verifyMail.verifySignature(decryptedPart, sender);
                if (signatureStatus != SMimeApi.RESULT_SIGNATURE_UNSIGNED) {
                    resultType |= SMimeApi.RESULT_TYPE_SIGNED;
                }

                result.putExtra(SMimeApi.RESULT_TYPE, resultType);
                result.putExtra(SMimeApi.RESULT_SIGNATURE, signatureStatus);
                result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_SUCCESS);
            } else {
                // TODO: not encrypted/decrypt failed
                result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_ERROR);
            }
        } catch (Exception e) {
            result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_ERROR);
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (encryptedFile != null) {
                encryptedFile.delete();
            }
        }

        Log.d(SMileCrypto.LOG_TAG, "decryptAndVerify: returning intent: " + result);
        return result;
    }

    private Intent encrypt(Intent data, ParcelFileDescriptor input, ParcelFileDescriptor output) {
        Intent result = new Intent();
        EncryptOperation operation = null;

        try {
            operation = new EncryptOperation(data, input, output);
            result = operation.execute();
            operation.close();
        } catch (Exception e) {
            result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_ERROR);
        } finally {
            if (operation != null) {
                try {
                    operation.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    private final Intent sign(final Intent data, final ParcelFileDescriptor input, final ParcelFileDescriptor output) {
        Intent result = new Intent();
        SignOperation operation = null;
        try {
            operation = new SignOperation(data, input, output);
            result = operation.execute();
        } catch (Exception e) {
            result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_ERROR);
        } finally {
            if (operation != null) {
                try {
                    operation.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    private final Intent encryptAndSign(final Intent data, final ParcelFileDescriptor input, final ParcelFileDescriptor output) {
        Intent result = new Intent();
        SignAndEncryptOperation operation = null;
        try {
            operation = new SignAndEncryptOperation(data, input, output);
            result = operation.execute();
        } catch (Exception e) {
            result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_ERROR);
        } finally {
            if (operation != null) {
                try {
                    operation.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
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

    abstract class CryptoOperation implements Closeable {
        protected final String recipient;
        protected final String sender;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        private final File inputFile;

        CryptoOperation(final Intent data, final ParcelFileDescriptor input, final ParcelFileDescriptor output) throws IOException {
            sender = data.getStringExtra(SMimeApi.EXTRA_SENDER);
            recipient = data.getStringExtra(SMimeApi.EXTRA_RECIPIENT);
            inputStream = new ParcelFileDescriptor.AutoCloseInputStream(input);
            outputStream = new ParcelFileDescriptor.AutoCloseOutputStream(output);
            inputFile = copyToFile(inputStream);
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            close();
        }

        @Override
        public void close() throws IOException {
            inputStream.close();
            outputStream.close();
            inputFile.delete();
        }

        private MimeMessage preProcess() throws FileNotFoundException, MessagingException {
            Properties props = System.getProperties();
            Session session = Session.getDefaultInstance(props, null);
            return new MimeMessage(session, new FileInputStream(inputFile));
        }

        abstract MimeMessage process(MimeMessage message) throws MessagingException, KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException;

        public Intent execute() throws MessagingException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
            final MimeMessage source = preProcess();
            final MimeMessage result = process(source);
            copyHeaders(source, result);
            //addDataHandlers(result);
            result.saveChanges();
            result.writeTo(outputStream);
            Intent intent = new Intent();
            intent.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_SUCCESS);
            return intent;
        }

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
    }

    class EncryptOperation extends CryptoOperation {
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

    class SignOperation extends CryptoOperation {
        public SignOperation(Intent data, ParcelFileDescriptor input, ParcelFileDescriptor output) throws IOException {
            super(data, input, output);
        }

        @Override
        MimeMessage process(MimeMessage message) throws MessagingException, KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
            SignMessage signer = new SignMessage();
            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setContent(message.getContent(), message.getContentType());
            MimeMultipart signedPart = signer.sign(bodyPart, new InternetAddress(sender));
            Properties props = System.getProperties();
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage result = new MimeMessage(session);
            addDataHandlers(result);
            result.setContent(signedPart, signedPart.getContentType());
            result.saveChanges();
            return result;
        }
    }

    class SignAndEncryptOperation extends CryptoOperation {
        public SignAndEncryptOperation(Intent data, ParcelFileDescriptor input, ParcelFileDescriptor output) throws IOException {
            super(data, input, output);
        }

        @Override
        MimeMessage process(MimeMessage message) throws MessagingException, KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
            SignMessage signer = new SignMessage();
            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setContent(message.getContent(), message.getContentType());
            MimeMultipart signedPart = signer.sign(bodyPart, new InternetAddress(sender));
            Properties props = System.getProperties();
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage signedMessage = new MimeMessage(session);
            copyHeaders(message, signedMessage);
            signedMessage.setContent(signedPart);
            addDataHandlers(signedMessage);
            signedMessage.saveChanges();
            EncryptMail enigma = new EncryptMail();
            MimeMessage encryptedMessage = enigma.encryptMessage(signedMessage, new InternetAddress(recipient));

            return encryptedMessage;
        }
    }
}
