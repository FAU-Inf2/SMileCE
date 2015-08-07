package de.fau.cs.mad.smile_crypto;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Properties;

import org.spongycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.spongycastle.cms.jcajce.JceKeyTransRecipientId;
import org.spongycastle.operator.bc.BcDigestCalculatorProvider;

import javax.mail.Address;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class DecryptMail {
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    String certificateDirectory;
    Boolean RETURN_DECODED = true;

    //TODO: parameter for AsyncTask
    private String alias;
    private MimeMessage encryptedMimeMessage;
    private String pathToFile;
    private String passphrase;

    //MimeBodyPart messagePart;

    public DecryptMail(String certificateDirectory) {
        this.certificateDirectory = certificateDirectory;
    }

    private MimeBodyPart decryptMailSynchronous(String pathToFile, String passphrase) {
        try {
            MimeMessage mimeMessage = getMimeMessageFromFile(pathToFile);
            if(mimeMessage == null)
                return null;

            Address[] recipients = getMailAddressFromMimeMessage(mimeMessage);
            String alias = null;
            for(Address r : recipients) {
                if ((alias = getAliasByAddress(r)) != null)
                    break;
            }
            if(alias == null)
                return null;

            return decryptMailSynchronous(alias, mimeMessage, passphrase);
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error in DecryptMail: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private MimeBodyPart decryptMailSynchronous(String alias, MimeMessage mimeMessage, String passphrase) {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);

            KeyStore p12 = KeyStore.getInstance("pkcs12");
            String pathTop12File = certificateDirectory + "/" + alias + ".p12";
            p12.load(new FileInputStream(pathTop12File), passphrase.toCharArray());

            PrivateKey privateKey = null;
            Enumeration e = p12.aliases();
            while (e.hasMoreElements()) {
                String aliasp12 = (String) e.nextElement();
                privateKey = (PrivateKey) p12.getKey(aliasp12, passphrase.toCharArray());
            }

            if(privateKey == null) {
                Log.e(SMileCrypto.LOG_TAG, "Could not find private key!");
                return null;
            }

            SMIMEToolkit toolkit = new SMIMEToolkit(new BcDigestCalculatorProvider());

            MimeBodyPart dec = toolkit.decrypt(mimeMessage, new JceKeyTransRecipientId(cert),
                    new JceKeyTransEnvelopedRecipient(privateKey).setProvider("SC"));

            Log.d(SMileCrypto.LOG_TAG, "MESSAGE: " + mimeMessage.getContent());
            Log.d(SMileCrypto.LOG_TAG, "DECRYPT (base64-encoded): " + convertMimeBodyPartToString(dec));
            return dec;
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error in DecryptMail: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public MimeBodyPart decryptMail(String pathToFile, String passphrase) {
        this.alias = null;
        this.encryptedMimeMessage = null;
        this.pathToFile = pathToFile;
        this.passphrase = passphrase;
        try {
            return new AsyncDecryptMail().execute().get();
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error while waiting for AsyncTask: " + e.getMessage());
            return null;
        }
    }

    public MimeBodyPart decryptMail(String alias, MimeMessage mimeMessage, String passphrase) {
        if(alias == null) {
            Log.e(SMileCrypto.LOG_TAG, "Called decryptMail with empty alias.");
            return null;
        }
        if(mimeMessage == null) {
            Log.e(SMileCrypto.LOG_TAG, "Called decryptMail with empty mimeMessage.");
            return null;
        }
        if(passphrase == null) {
            Log.e(SMileCrypto.LOG_TAG, "Called decryptMail without passphrase.");
            return null;
        }
        this.alias = alias;
        this.encryptedMimeMessage = mimeMessage;
        this.passphrase = passphrase;
        try {
            return new AsyncDecryptMail().execute().get();
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error while waiting for AsyncTask: " + e.getMessage());
            return null;
        }
    }

    public MimeBodyPart decryptMail(Address emailAddress, MimeMessage mimeMessage, String passphrase) {
        String alias = getAliasByAddress(emailAddress);
        if(alias == null) {
            Log.e(SMileCrypto.LOG_TAG, "Could not find certificate for given email address in decryptMail.");
            return null;
        }
        return decryptMail(alias, mimeMessage, passphrase);
    }

    public String decryptEncodeMail(String pathToFile, String passphrase) {
        this.alias = null;
        this.encryptedMimeMessage = null;
        this.pathToFile = pathToFile;
        this.passphrase = passphrase;
        try {
            return new AsyncDecryptEncodeMail().execute().get();
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error while waiting for AsyncTask: " + e.getMessage());
            return null;
        }
    }
    private MimeMessage getMimeMessageFromFile(String pathToFile) {
        try {
            Properties props = System.getProperties();
            Session session = Session.getDefaultInstance(props, null);
            File file = new File(pathToFile);
            return new MimeMessage(session, new FileInputStream(file));
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Exception while reading encrypted mail: " + e.getMessage());
            return null;
        }
    }

    private Address[] getMailAddressFromMimeMessage(MimeMessage mimeMessage) {
        try {
            return mimeMessage.getAllRecipients();
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error while getting all recipients: " + e.getMessage());
            return null;
        }
    }

    private String getAliasByAddress(Address emailAddress) {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            Enumeration e = ks.aliases();
            while (e.hasMoreElements()) {
                String alias = (String) e.nextElement();
                X509Certificate c = (X509Certificate) ks.getCertificate(alias);
                //TODO: handle case if one mailaddress has more than one certificate
                if(c.getSubjectDN().getName().contains("E="+emailAddress.toString()))
                    return alias;
            }
            return null;
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error in getAliasByAddress:" + e.getMessage());
            return null;
        }
    }

    public MimeMessage decodeMimeBodyParts(MimeBodyPart mimeBodyPart) {
        try {
            Properties props = System.getProperties();
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage newMimeMessage = new MimeMessage(session);

            Multipart multipart = new MimeMultipart("alternative");

            String decryptedPart = convertMimeBodyPartToString(mimeBodyPart);
            String[] lines = decryptedPart.split("\n");
            Boolean possibleConvert = false;
            Boolean convert = false;
            InternetHeaders headers = null;
            String newContent = null;
            //int i = -1;
            for (String line : lines) {
                line = line.replace("\r", "");
                //i++;
                //Log.d(SMileCrypto.LOG_TAG, i + "LINE is: " + line + "\n\n");
                if (line.startsWith("------")) {
                    //Log.d(SMileCrypto.LOG_TAG, i + "First case!" + "\n\n");
                    if (headers != null && newContent != null) {
                        MimeBodyPart bodyPart;
                        //Log.d(SMileCrypto.LOG_TAG, i + "headers: " + headers.getAllHeaderLines().nextElement());
                        if (convert) {
                            //Log.d(SMileCrypto.LOG_TAG, i + "Convert: " + newContent);
                            byte[] decoded = Base64.decode(newContent, 0);
                            //Log.d(SMileCrypto.LOG_TAG, i + "DECODED: " + new String(decoded));

                            bodyPart = new MimeBodyPart(headers, decoded);
                        } else {
                            //Log.d(SMileCrypto.LOG_TAG, i + "not convert: " + newContent);
                            if(newContent.equals(""))
                                newContent = "\n";
                            bodyPart = new MimeBodyPart(headers, newContent.getBytes());
                        }
                        //Log.d(SMileCrypto.LOG_TAG, i + "clear all" + "\n\n");
                        multipart.addBodyPart(bodyPart);
                        headers = null;
                        convert = false;
                        possibleConvert = false;
                        newContent = null;
                    } else {
                        //Log.d(SMileCrypto.LOG_TAG, i + "header null - continue!" + "\n\n");
                    }
                } else if (line.contains("boundary=\"---")) {
                    continue;
                } else if (line.startsWith("Content-Type")) {
                    if ((line.contains("text/plain") || line.contains("text/html")) && line.contains("utf-8")) {
                        possibleConvert = true;
                    } else if(line.contains("multipart/alternative"))
                        continue; //ignore

                    headers = new InternetHeaders();
                    //Log.d(SMileCrypto.LOG_TAG, i + "add header line: " + line + "\n\n");
                    headers.addHeaderLine(line);
                } else if (line.startsWith("Content-Transfer-Encoding")) {
                    if (line.contains("base64")) {
                        if (possibleConvert) {
                            convert = true;
                            headers.addHeaderLine("Content-Transfer-Encoding: quoted-printable");
                            //Log.d(SMileCrypto.LOG_TAG, i + "add header line: " + "Content-Transfer-Encoding: quoted-printable" + "\n\n");
                            continue;
                        }
                    }
                    //Log.d(SMileCrypto.LOG_TAG, i + "add header line: " + line + "\n\n");
                    if(headers == null)
                        headers = new InternetHeaders();
                    headers.addHeaderLine(line);
                } else if (line.startsWith("Content-Disposition")) {
                    //Log.d(SMileCrypto.LOG_TAG, i + "add header line: " + line + "\n\n");
                    if(headers == null)
                        headers = new InternetHeaders();
                    headers.addHeaderLine(line);
                } else if (line.equals("\n") || line.equals("\r")) {
                    //Log.d(SMileCrypto.LOG_TAG, i + "empty line" + "\n\n");
                    continue;
                } else {
                    //line = line.replaceAll("[\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", "");
                    //Log.d(SMileCrypto.LOG_TAG, i + "old content " + newContent + "\n\n");
                    if (newContent == null)
                        newContent = "";
                    newContent += line + "\n";
                    //Log.d(SMileCrypto.LOG_TAG, i + "new content " + newContent + "\n\n");
                }
            }
            newMimeMessage.setContent(multipart);
            newMimeMessage.saveChanges();

            return newMimeMessage;
        } catch(Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Exception decrypting parts: " + e.getMessage());
            return null;
        }
    }

    public String convertMimeBodyPartToString(MimeBodyPart mimeBodyPart) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            mimeBodyPart.getDataHandler().writeTo(baos);

            return baos.toString();
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error converting MimeBodyPart to String: " + e.getMessage());
            return null;
        }
    }

    public String convertMimeMessageToString(MimeMessage mimeMessage) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ((Multipart) mimeMessage.getContent()).writeTo(bytes);
            return bytes.toString();
        }catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error converting MimeMessage to String: " + e.getMessage());
            return  null;
        }
    }

    private class AsyncDecryptMail extends AsyncTask<Void, Void, MimeBodyPart> {

        protected MimeBodyPart doInBackground(Void... params) {
            try {
                if(alias == null || encryptedMimeMessage == null)
                    return decryptMailSynchronous(pathToFile, passphrase);
                else
                    return decryptMailSynchronous(alias, encryptedMimeMessage, passphrase);
            } catch (Exception e) {
                Log.e(SMileCrypto.LOG_TAG, "Error in doInBackground" + e.getMessage());
                return null;
            }
        }
    }

    private class AsyncDecryptEncodeMail extends AsyncTask<Void, Void, String> {

        protected String doInBackground(Void... params) {
            try {
                MimeBodyPart mimeBodyPart;
                if(alias == null || encryptedMimeMessage == null)
                    mimeBodyPart = decryptMailSynchronous(pathToFile, passphrase);
                else
                    mimeBodyPart = decryptMailSynchronous(alias, encryptedMimeMessage, passphrase);

                MimeMessage decodedMimeMessage = decodeMimeBodyParts(mimeBodyPart);
                return convertMimeMessageToString(decodedMimeMessage);
            } catch (Exception e) {
                Log.e(SMileCrypto.LOG_TAG, "Error in doInBackground" + e.getMessage());
                return null;
            }
        }
    }


    /*
    public void startEncDecMail(String alias) {
        this.alias = alias;
        new AsyncEncDecMail().execute();
    }
    public void testEncDecMail(String alias, MimeBodyPart msg) {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
            Log.d(SMileCrypto.LOG_TAG, "· SubjectDN: " + cert.getSubjectDN().getName());
            Log.d(SMileCrypto.LOG_TAG, "· IssuerDN: " + cert.getIssuerDN().getName());

            KeyStore p12 = KeyStore.getInstance("pkcs12");
            String pathTop12File = certificateDirectory + "/" + alias + ".p12";
            p12.load(new FileInputStream(pathTop12File), "password".toCharArray()); //TODO!!!!

            PrivateKey privateKey = null;
            Enumeration e = p12.aliases();
            while (e.hasMoreElements()) {
                String aliasp12 = (String) e.nextElement();
                //X509Certificate c = (X509Certificate) p12.getCertificate(alias);
                //Log.d(SMileCrypto.LOG_TAG, "Found certificate with alias: " + alias);
                //Log.d(SMileCrypto.LOG_TAG, "· SubjectDN: " + c.getSubjectDN().getName());
                //Log.d(SMileCrypto.LOG_TAG, "· IssuerDN: " + c.getIssuerDN().getName());
                privateKey = (PrivateKey) p12.getKey(aliasp12, "password".toCharArray());
            }

            if(privateKey == null) {
                Log.e(SMileCrypto.LOG_TAG, "Could not find private key!");
                return;
            }

            SMIMEToolkit toolkit = new SMIMEToolkit(new BcDigestCalculatorProvider());

            MimeMessage message = makeMimeMessage(msg);
            MimeBodyPart res = toolkit.encrypt(message,
                    new JceCMSContentEncryptorBuilder(NISTObjectIdentifiers.id_aes128_CBC).setProvider("SC").build(),
                    new JceKeyTransRecipientInfoGenerator(cert).setProvider("SC"));

            //Assert.assertTrue(toolkit.isEncrypted(res));
            MimeMessage body = makeMimeMessage(res);
            MimeBodyPart dec = toolkit.decrypt(body, new JceKeyTransRecipientId(cert),
                    new JceKeyTransEnvelopedRecipient(privateKey).setProvider("SC"));

            Log.d(SMileCrypto.LOG_TAG, "MESSAGE: " +  message.getContent());
            String content = convertStreamToString((java.io.InputStream) dec.getContent());
            Log.d(SMileCrypto.LOG_TAG, "DECRYPT: " + content);
        } catch (Exception e) {
            Log.d(SMileCrypto.LOG_TAG, "Error in testEncDecMail: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public MimeMessage enc(String alias, MimeBodyPart part) {
        this.alias = alias;
        this.messagePart = part;
        new AsyncEncMail().execute();
        try {
            //This is evil! Freezes main thread
            Thread.sleep(3000);
        }catch (Exception e)  {
            e.printStackTrace();
        }
        return this.encryptedMimeMessage;
    }
    public MimeMessage makeMimeMessage(MimeBodyPart res)
            throws MessagingException, IOException {
        Properties props = System.getProperties();
        Session session = Session.getDefaultInstance(props, null);

        Address fromUser = new InternetAddress("\"Eric H. Echidna\"<eric@bouncycastle.org>");
        Address toUser = new InternetAddress("example@bouncycastle.org");

        MimeMessage body = new MimeMessage(session);
        body.setFrom(fromUser);
        body.setRecipient(Message.RecipientType.TO, toUser);
        body.setSubject("example message");
        body.setContent(res.getContent(), res.getContentType());

        body.saveChanges();

        return body;
    }
    public MimeMessage encrypt(String alias, MimeBodyPart part) {
        SMIMEToolkit toolkit = new SMIMEToolkit(new BcDigestCalculatorProvider());
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);

            MimeMessage message = makeMimeMessage(part);
            MimeBodyPart res = toolkit.encrypt(message,
                    new JceCMSContentEncryptorBuilder(NISTObjectIdentifiers.id_aes128_CBC).setProvider("SC").build(),
                    new JceKeyTransRecipientInfoGenerator(cert).setProvider("SC"));

            this.encryptedMimeMessage = makeMimeMessage(res);
            return this.encryptedMimeMessage;
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error encrypting mail" + e.getMessage());
            e.printStackTrace();
            return null;
        }

    }
    private class AsyncEncDecMail extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                MimeBodyPart messagePart = new MimeBodyPart();
                messagePart.setText("You message's string content goes here.", "utf-8");

                testEncDecMail(alias, messagePart);
            } catch (Exception e) {
                Log.e(SMileCrypto.LOG_TAG, "Error in doInBackground" + e.getMessage());
            }
            return null;
        }
    }

    private class AsyncEncMail extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                encrypt(alias, messagePart);
            } catch (Exception e) {
                Log.e(SMileCrypto.LOG_TAG, "Error in doInBackground" + e.getMessage());
            }
            return null;
        }
    } */
}
