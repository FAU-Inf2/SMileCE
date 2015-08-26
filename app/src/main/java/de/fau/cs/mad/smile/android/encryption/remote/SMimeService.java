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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import de.fau.cs.mad.javax.activation.DataHandler;
import de.fau.cs.mad.javax.activation.FileDataSource;
import de.fau.cs.mad.smile.android.encryption.EncryptMail;
import de.fau.cs.mad.smile.android.encryption.SMileCrypto;
import de.fau.cs.mad.smile.android.encryption.App;
import de.fau.cs.mad.smile.android.encryption.DecryptMail;
import de.fau.cs.mad.smile.android.encryption.SignMessage;
import de.fau.cs.mad.smile.android.encryption.SignatureCheck;
import de.fau.cs.mad.smime_api.ISMimeService;
import de.fau.cs.mad.smime_api.SMimeApi;

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

            if(decryptedPart != null) {
                decryptedPart.writeTo(outputStream);
                int resultType = SMimeApi.RESULT_TYPE_ENCRYPTED;

                if(verifyMail.verifySignature(decryptedPart, sender)) {
                    resultType |= SMimeApi.RESULT_TYPE_SIGNED;
                }

                result.putExtra(SMimeApi.RESULT_TYPE, resultType);
                result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_SUCCESS);
            } else {
                // TODO: not encrypted/decrypt failed
                result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_ERROR);
            }
        } catch (Exception e) {
            result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_ERROR);
            e.printStackTrace();
        } finally {
            if(outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(encryptedFile != null) {
                encryptedFile.delete();
            }
        }

        Log.d(SMileCrypto.LOG_TAG, "decryptAndVerify: returning intent: " + result);
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

    private final Intent sign(final Intent data, final ParcelFileDescriptor input, final ParcelFileDescriptor output) {
        final InputStream inputStream = new ParcelFileDescriptor.AutoCloseInputStream(input);
        final OutputStream outputStream = new ParcelFileDescriptor.AutoCloseOutputStream(output);
        final String sender = data.getStringExtra(SMimeApi.EXTRA_SENDER);
        File inputFile = null;

        final Intent result = new Intent();

        try {
            inputFile = copyToFile(inputStream);
            SignMessage signer = new SignMessage();

            Properties props = System.getProperties();
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage mimeMessage = new MimeMessage(session, new FileInputStream(inputFile));
            mimeMessage.setDataHandler(new DataHandler(new FileDataSource(inputFile)));
            Object content = mimeMessage.getContent();
            MimeBodyPart bodyPart = new MimeBodyPart((InputStream) content);

            MimeMultipart signedPart = signer.sign(bodyPart, new InternetAddress(sender));
            if (signedPart != null) {
                mimeMessage.setContent(signedPart, signedPart.getContentType());
                mimeMessage.saveChanges();
                File outputFile = new File(App.getContext().getApplicationContext().getDir("service-messages", Context.MODE_PRIVATE), "signed.tmp");
                mimeMessage.writeTo(new FileOutputStream(outputFile));
                mimeMessage.writeTo(outputStream);
                result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_SUCCESS);
            }
        }catch (Exception e) {
            result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_ERROR);
            e.printStackTrace();
        } finally {
            if(outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(inputFile != null) {
                inputFile.delete();
            }
        }

        return result;
    }

    private Intent encrypt(Intent data, ParcelFileDescriptor input, ParcelFileDescriptor output) {
        final InputStream inputStream = new ParcelFileDescriptor.AutoCloseInputStream(input);
        final OutputStream outputStream = new ParcelFileDescriptor.AutoCloseOutputStream(output);
        final String recipient = data.getStringExtra(SMimeApi.EXTRA_RECIPIENT);
        File inputFile = null;

        final Intent result = new Intent();

        try {
            inputFile = copyToFile(inputStream);
            EncryptMail enigma = new EncryptMail();
            Properties props = System.getProperties();
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage mimeMessage = new MimeMessage(session, new FileInputStream(inputFile));
            MimeMessage encryptedMessage = enigma.encryptMessage(mimeMessage, new InternetAddress(recipient));
            if (encryptedMessage != null) {
                File outputFile = new File(App.getContext().getApplicationContext().getDir("service-messages", Context.MODE_PRIVATE), "encrypted.tmp");
                Enumeration enumeration = mimeMessage.getAllHeaderLines();
                while (enumeration.hasMoreElements()) {
                    String headerLine = (String) enumeration.nextElement();
                    encryptedMessage.addHeaderLine(headerLine);
                }
                encryptedMessage.writeTo(new FileOutputStream(outputFile));
                encryptedMessage.writeTo(outputStream);
                result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_SUCCESS);
            }
        }catch (Exception e) {
            result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_ERROR);
            e.printStackTrace();
        } finally {
            if(outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(inputFile != null) {
                inputFile.delete();
            }
        }

        return result;
    }

    private final Intent encryptAndSign(final Intent data, final ParcelFileDescriptor input, final ParcelFileDescriptor output) {
        return null;
    }
}
