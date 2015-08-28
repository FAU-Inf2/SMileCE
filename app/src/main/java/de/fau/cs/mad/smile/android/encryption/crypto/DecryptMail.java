package de.fau.cs.mad.smile.android.encryption.crypto;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.sun.mail.util.QPDecoderStream;

import org.spongycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.spongycastle.cms.jcajce.JceKeyTransRecipientId;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.mail.smime.SMIMEException;
import org.spongycastle.operator.bc.BcDigestCalculatorProvider;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import de.fau.cs.mad.smile.android.encryption.KeyInfo;
import de.fau.cs.mad.smile.android.encryption.SMileCrypto;
import korex.mail.Address;
import korex.mail.BodyPart;
import korex.mail.Header;
import korex.mail.MessagingException;
import korex.mail.Multipart;
import korex.mail.Session;
import korex.mail.internet.InternetHeaders;
import korex.mail.internet.MimeBodyPart;
import korex.mail.internet.MimeMessage;
import korex.mail.internet.MimeMultipart;

public class DecryptMail {
    static {
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
    }

    private final KeyManagement keyManagement;
    private MimeMessage encryptedMimeMessage = null;

    public DecryptMail() throws KeyStoreException, IOException, NoSuchAlgorithmException,
            CertificateException, NoSuchProviderException {
        keyManagement = new KeyManagement();
    }

    public final MimeBodyPart decryptMail(final MimeBodyPart mimeBodyPart, final CryptoParams cryptoParams)
            throws KeyStoreException, MessagingException, NoSuchAlgorithmException,
            CertificateException, IOException, SMIMEException, UnrecoverableEntryException {

        if (mimeBodyPart == null) {
            Log.e(SMileCrypto.LOG_TAG, "Called decryptMail with empty mimeMessage.");
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
            return null;
        }

        X509Certificate certificate = (X509Certificate) cryptoParams.getIdentity().getCertificate();
        PrivateKey privateKey = cryptoParams.getIdentity().getPrivateKey();

        SMIMEToolkit toolkit = new SMIMEToolkit(new BcDigestCalculatorProvider());
        JceKeyTransEnvelopedRecipient envelopedRecipient = new JceKeyTransEnvelopedRecipient(privateKey);
        envelopedRecipient.setProvider(BouncyCastleProvider.PROVIDER_NAME);
        JceKeyTransRecipientId recipientId = new JceKeyTransRecipientId(certificate);
        MimeBodyPart decryptedBodyPart = toolkit.decrypt(mimeBodyPart, recipientId, envelopedRecipient);
        return decryptedBodyPart;
    }

    private MimeBodyPart decryptMailSynchronous(String pathToFile, String alias, String passphrase) {
        try {
            MimeMessage mimeMessage = getMimeMessageFromFile(pathToFile);

            if(alias != null) {
                return decryptMailSynchronous(mimeMessage, alias, passphrase);
            }

            Address[] recipients = getMailAddressFromMimeMessage(mimeMessage);
            if (recipients == null) {
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_NO_RECIPIENTS_FOUND;
                return null;
            }

            ArrayList<String> aliases;
            for (Address r : recipients) { // try all recipients until one has fitting certificate
                aliases = keyManagement.getAliasesByOwnAddress(r);
                if(aliases.size() == 0) {
                    continue;
                }
                for(String foundAlias : aliases) { //try all aliases until one fits
                    MimeBodyPart result = decryptMailSynchronous(mimeMessage, foundAlias, passphrase);
                    if (result != null) // message was decrypted
                        return result;
                }
            }

            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_NO_CERTIFICATE_FOUND;
            return null;
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error in DecryptMail: " + e.getMessage());
            e.printStackTrace();
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_UNKNOWN_ERROR;
            return null;
        }
    }

    private MimeBodyPart decryptMailSynchronous(MimeMessage mimeMessage, String alias, String passphrase) {
        try {
            final KeyStore.PrivateKeyEntry privateKey = keyManagement.getPrivateKeyEntry(alias, passphrase);
            if (privateKey == null) {
                Log.e(SMileCrypto.LOG_TAG, "Could not find private key!");
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_CERTIFICATE_STORED;
                return null;
            }

            X509Certificate cert = (X509Certificate) privateKey.getCertificate();

            return decryptMailSynchronous(mimeMessage, privateKey.getPrivateKey(), cert);
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error in DecryptMail: " + e.getMessage());
            e.printStackTrace();
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_UNKNOWN_ERROR;
            return null;
        }
    }

    private MimeBodyPart decryptMailSynchronous(MimeMessage mimeMessage, PrivateKey privateKey, X509Certificate certificate) {
        try {
            encryptedMimeMessage = mimeMessage;
            SMIMEToolkit toolkit = new SMIMEToolkit(new BcDigestCalculatorProvider());

            MimeBodyPart dec = toolkit.decrypt(mimeMessage, new JceKeyTransRecipientId(certificate),
                    new JceKeyTransEnvelopedRecipient(privateKey).setProvider("SC"));

            if(dec == null) {
                Log.d(SMileCrypto.LOG_TAG, "Decrypted MimeBodyPart is null.");
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_DECRYPTION_FAILED;
            } else {
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_SUCCESS;
            }
            return dec;
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error in DecryptMail: " + e.getMessage());
            e.printStackTrace();
            if(e.getMessage().contains("CMS processing failure"))
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_NO_VALID_MIMEMESSAGE;
            else
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_UNKNOWN_ERROR;
            return null;
        }
    }

    public MimeMessage decryptEncodeMail(String pathToFile, String alias, String passphrase) {
        try {
            return new AsyncDecryptEncodeMail(pathToFile, alias, passphrase).execute().get();
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error while waiting for AsyncTask: " + e.getMessage());
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_ERROR_ASYNC_TASK;
            return null;
        }
    }

    public MimeMessage getMimeMessageFromFile(String pathToFile) {
        try {
            Properties props = System.getProperties();
            Session session = Session.getDefaultInstance(props, null);
            File file = new File(pathToFile);
            return new MimeMessage(session, new FileInputStream(file));
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Exception while reading encrypted mail: " + e.getMessage());
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_NO_VALID_MIMEMESSAGE_IN_FILE;
            return null;
        }
    }

    public ArrayList<KeyInfo> getKeyInfosByMimeMessage(MimeMessage mimeMessage) {
        Address[] addresses = getMailAddressFromMimeMessage(mimeMessage);
        if (addresses == null) {
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_NO_RECIPIENTS_FOUND;
            return null;
        }

        ArrayList<KeyInfo> result = new ArrayList<>();

        for (Address a : addresses) {
            ArrayList<KeyInfo> keyInfos = keyManagement.getKeyInfoByOwnAddress(a);
            for(KeyInfo keyInfo : keyInfos) {
                result.add(keyInfo);
            }
        }

        if(result.size() == 0)
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_NO_CERTIFICATE_FOUND;

        return result;
    }

    private Address[] getMailAddressFromMimeMessage(MimeMessage mimeMessage) {
        try {
            return mimeMessage.getAllRecipients();
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error while getting all recipients: " + e.getMessage());
            return null;
        }
    }

    public MimeMessage decodeMimeBodyParts(MimeBodyPart mimeBodyPart) {
        return decodeMimeBodyParts(mimeBodyPart, true);
    }

    public MimeMessage decodeMimeBodyParts(String decryptedPart, Boolean decodeBase64Parts, String multipartContentType) {
        try {
            Log.d(SMileCrypto.LOG_TAG, "Try to decode MimeBodyPart…");
            if(decodeBase64Parts)
                Log.d(SMileCrypto.LOG_TAG, "Will decode base64-text-parts if such parts exist.");
            else
                Log.d(SMileCrypto.LOG_TAG, "Will not decode base64-text-parts.");

            Properties props = System.getProperties();
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage newMimeMessage = new MimeMessage(session);

            Multipart multipart;
            if(multipartContentType == null)
                multipart = new MimeMultipart("alternative");
            else
                multipart = new MimeMultipart(multipartContentType);

            String[] lines = decryptedPart.split("\n");
            Boolean possibleConvert = false;
            Boolean convert = false;
            InternetHeaders headers = null;
            String newContent = null;
            Boolean isAttachment = false;
            Boolean headerPossible = true;
            //int i = -1;
            for (String line : lines) {
                line = line.replace("\r", "");
                //i++;
                //Log.d(SMileCrypto.LOG_TAG, i + ". LINE is: " + line + "\n\n");
                if (line.startsWith("------") || line.startsWith("--Apple-Mail=")) {
                    //Log.d(SMileCrypto.LOG_TAG, i + ". First case!" + "\n\n");
                    if (headers != null && newContent != null) {
                        MimeBodyPart bodyPart;
                        //Log.d(SMileCrypto.LOG_TAG, i + ". headers: " + headers.getAllHeaderLines().nextElement());
                        if (convert && decodeBase64Parts) {
                            //Log.d(SMileCrypto.LOG_TAG, i + ". Convert: " + newContent);
                            byte[] decoded = Base64.decode(newContent, 0);
                            //Log.d(SMileCrypto.LOG_TAG, i + ". DECODED: " + new String(decoded));

                            bodyPart = new MimeBodyPart(headers, decoded);
                        } else {
                            //Log.d(SMileCrypto.LOG_TAG, i + ". not convert: " + newContent);
                            if (newContent.equals(""))
                                newContent = "\n";
                            bodyPart = new MimeBodyPart(headers, newContent.getBytes());
                        }
                        //Log.d(SMileCrypto.LOG_TAG, i + ". clear all" + "\n\n");
                        multipart.addBodyPart(bodyPart);
                        headers = null;
                        convert = false;
                        possibleConvert = false;
                        newContent = null;
                        isAttachment = false;
                        headerPossible = true;
                    } else {
                        //Log.d(SMileCrypto.LOG_TAG, i + ". header null -- continue!" + "\n\n");
                        headerPossible = true;
                    }
                } else if (line.contains("boundary=\"---")) {
                    continue;
                } else if (headerPossible && line.startsWith("Content-Type")) {
                    if ((line.contains("text/plain") || line.contains("text/html")) && line.contains("utf-8")) {
                        possibleConvert = true;
                    } else if (line.contains("multipart/alternative"))
                        continue; //ignore

                    headers = new InternetHeaders();
                    //Log.d(SMileCrypto.LOG_TAG, i + ". add header line: " + line + "\n\n");
                    headers.addHeaderLine(line);
                } else if (headerPossible && line.startsWith("Content-Transfer-Encoding")) {
                    if (line.contains("base64")) {
                        if (possibleConvert && decodeBase64Parts) {
                            convert = true;
                            headers.addHeaderLine("Content-Transfer-Encoding: quoted-printable");
                            //Log.d(SMileCrypto.LOG_TAG, i + ". add header line: " + "Content-Transfer-Encoding: quoted-printable" + "\n\n");
                            continue;
                        }
                    }
                    //Log.d(SMileCrypto.LOG_TAG, i + ". add header line: " + line + "\n\n");
                    if (headers == null)
                        headers = new InternetHeaders();
                    headers.addHeaderLine(line);
                } else if (headerPossible && line.startsWith("Content-Disposition")) {
                    //Log.d(SMileCrypto.LOG_TAG, i + ". add header line: " + line + "\n\n");
                    if (headers == null)
                        headers = new InternetHeaders();
                    headers.addHeaderLine(line);
                    if(line.contains("attachment"))
                        isAttachment = true;
                } else if (headerPossible && line.startsWith("Content-Description")) {
                    //Log.d(SMileCrypto.LOG_TAG, i + ". add header line: " + line + "\n\n");
                    if (headers == null)
                        headers = new InternetHeaders();
                    headers.addHeaderLine(line);
                } else if (headerPossible && line.contains("charset")) {
                    //Log.d(SMileCrypto.LOG_TAG, i + ". add header line: " + line + "\n\n");
                    if (headers == null)
                        headers = new InternetHeaders();
                    headers.addHeaderLine(line);
                } else if (headerPossible && isAttachment && (line.contains("filename=") || line.contains("name="))){
                    //Log.d(SMileCrypto.LOG_TAG, i + ". add header line: " + line + "\n\n");
                    if (headers == null)
                        headers = new InternetHeaders();
                    headers.addHeaderLine(line);
                } else if (line.equals("\n") || line.equals("\r")) {
                    //Log.d(SMileCrypto.LOG_TAG, i + ". empty line" + "\n\n");
                    continue;
                } else {
                    headerPossible = false;
                    line = line.replaceAll("[\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", "");
                    //Log.d(SMileCrypto.LOG_TAG, i + ". old content " + newContent + "\n\n");
                    if (newContent == null)
                        newContent = "";
                    newContent += line + "\n";
                    //Log.d(SMileCrypto.LOG_TAG, i + ". new content " + newContent + "\n\n");
                }
            }
            newMimeMessage.setContent(multipart);
            newMimeMessage.saveChanges();
            /*
            MimeMultipart mpart = (MimeMultipart) newMimeMessage.getContent();
            int cnt = mpart.getCount();
            for (int i = 0; i < cnt; i++) {
                BodyPart b = mpart.getBodyPart(i);
                BufferedInputStream bis = new BufferedInputStream((QPDecoderStream) b.getContent());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    while (true) {
                        int c = bis.read();
                        if (c == -1) {
                            break;
                        }
                        baos.write(c);
                    }

                Log.e(SMileCrypto.LOG_TAG, "Decrypt Content: " + new String(baos.toByteArray()));
                Log.e(SMileCrypto.LOG_TAG, "Decrypt toString: " + b.toString());
                Log.e(SMileCrypto.LOG_TAG, "Decrypt getDescription: " + b.getDescription());

            }*/
            Log.d(SMileCrypto.LOG_TAG, "… finished decoding MimeBodyPart.");
            return newMimeMessage;
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Exception decoding parts: " + e.getMessage());
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_UNKNOWN_ERROR;
            e.printStackTrace();
            return null;
        }
    }

    public MimeMessage decodeMimeBodyParts(MimeBodyPart mimeBodyPart, Boolean decodeBase64Parts) {
        try {
            return decodeMimeBodyParts(convertMimeBodyPartToString(mimeBodyPart), decodeBase64Parts, null);
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Exception decoding parts: " + e.getMessage());
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_UNKNOWN_ERROR;
            return null;
        }
    }

    public String convertMimeBodyPartToString(MimeBodyPart mimeBodyPart) {
        try {
            if (mimeBodyPart == null) {
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
                return null;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            mimeBodyPart.getDataHandler().writeTo(baos);

            return baos.toString();
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error converting MimeBodyPart to String: " + e.getMessage());
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_UNKNOWN_ERROR;
            return null;
        }
    }

    public String convertMimeMessageToString(MimeMessage mimeMessage) {
        try {
            if (mimeMessage == null) {
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
                return null;
            }

            String result = "";
            Enumeration allHeaders = mimeMessage.getAllHeaders();
            while (allHeaders.hasMoreElements()) {
                Header header = (Header) allHeaders.nextElement();
                result += header.getName() + ": " + header.getValue() + "\n";
            }

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ((Multipart) mimeMessage.getContent()).writeTo(bytes);
            return result + bytes.toString();
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error converting MimeMessage to String: " + e.getMessage());
            e.printStackTrace();
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_UNKNOWN_ERROR;
            return null;
        }
    }

    public String getTextPlainFromMimeMessage(MimeMessage mimeMessage) {
        try {
            if (mimeMessage == null) {
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
                return null;
            }

            MimeMultipart mimeMultipart = (MimeMultipart) mimeMessage.getContent();
            int cnt = mimeMultipart.getCount();
            for (int i = 0; i < cnt; i++) {
                BodyPart b = mimeMultipart.getBodyPart(i);
                if (b.isMimeType("text/plain")) {
                    if (b.getContent() instanceof String) {
                        return (String) b.getContent();
                    } else if (b.getContent() instanceof QPDecoderStream) {
                        BufferedInputStream bis = new BufferedInputStream((QPDecoderStream) b.getContent());
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        while (true) {
                            int c = bis.read();
                            if (c == -1) {
                                break;
                            }
                            baos.write(c);
                        }
                        return new String(baos.toByteArray());
                    } else {
                        Log.d(SMileCrypto.LOG_TAG, "b.getContent was instance of: " + b.getContentType());
                        SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_UNKNOWN_ERROR;
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error extracting text/plain from MimeMessage: " + e.getMessage());
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_UNKNOWN_ERROR;
        }
        return null;
    }

    public String getTextHtmlFromMimeMessage(MimeMessage mimeMessage) {
        try {
            if (mimeMessage == null) {
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
                return null;
            }

            MimeMultipart mimeMultipart = (MimeMultipart) mimeMessage.getContent();
            int cnt = mimeMultipart.getCount();
            for (int i = 0; i < cnt; i++) {
                BodyPart b = mimeMultipart.getBodyPart(i);
                if (b.isMimeType("text/html")) {
                    if (b.getContent() instanceof String) {
                        return (String) b.getContent();
                    } else if (b.getContent() instanceof QPDecoderStream) {
                        BufferedInputStream bis = new BufferedInputStream((QPDecoderStream) b.getContent());
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        while (true) {
                            int c = bis.read();
                            if (c == -1) {
                                break;
                            }
                            baos.write(c);
                        }
                        return new String(baos.toByteArray());
                    } else {
                        Log.d(SMileCrypto.LOG_TAG, "b.getContent was instance of: " + b.getContentType());
                        SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_UNKNOWN_ERROR;
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error extracting text/plain from MimeMessage: " + e.getMessage());
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_UNKNOWN_ERROR;
        }
        return null;
    }

    public MimeMessage addOldHeaders(MimeMessage newMimeMessage) {
        if (encryptedMimeMessage == null) {
            return newMimeMessage;
        }

        try {
            int i = 0;
            Log.d(SMileCrypto.LOG_TAG, "Add headers from encrypted MimeMessage.");
            Enumeration allHeaders = encryptedMimeMessage.getAllHeaders();
            while (allHeaders.hasMoreElements()) {
                Header header = (Header) allHeaders.nextElement();
                //Log.d(SMileCrypto.LOG_TAG, "Headername + value: " + header.getName() + " "+ header.getValue());
                newMimeMessage.addHeader(header.getName(), header.getValue());
                i++;
            }
            Log.d(SMileCrypto.LOG_TAG, "Added " + i + " headers from encrypted MimeMessage.");
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Exception in addOldHeaders:" + e.getMessage());
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_UNKNOWN_ERROR;
        }
        return newMimeMessage;
    }

    private class AsyncDecryptEncodeMail extends AsyncTask<Void, Void, MimeMessage> {
        private final String pathToFile;
        private final String alias;
        private final String passphrase;

        public AsyncDecryptEncodeMail(final String pathToFile, final String alias, final String passphrase) {
            this.pathToFile = pathToFile;
            this.alias = alias;
            this.passphrase = passphrase;
        }

        protected MimeMessage doInBackground(Void... params) {
            MimeBodyPart mimeBodyPart = null;
            mimeBodyPart = decryptMailSynchronous(pathToFile, alias, passphrase);

            if (mimeBodyPart == null) {
                return null;
            }

            return addOldHeaders(decodeMimeBodyParts(mimeBodyPart));
        }
    }
}
