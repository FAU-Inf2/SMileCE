package de.fau.cs.mad.smile_crypto.remote;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import javax.mail.internet.MimeBodyPart;
import javax.mail.util.SharedFileInputStream;

import de.fau.cs.mad.smile_crypto.App;
import de.fau.cs.mad.smile_crypto.DecryptMail;
import de.fau.cs.mad.smile_crypto.SMileCrypto;
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
                case SMimeApi.ACTION_ENCRYPT_AND_SIGN:
                    return encryptAndSign(data, input, output);
                case SMimeApi.ACTION_DECRYPT_VERIFY:
                    return decryptAndVerify(data, input, output);
                default:
                    return null;
            }
        }
    };

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

        try {
            encryptedFile = copyToFile(inputStream);

            final DecryptMail decryptMail = new DecryptMail();
            MimeBodyPart mimeBodyPart = new MimeBodyPart(new SharedFileInputStream(encryptedFile));
            MimeBodyPart decryptedPart = decryptMail.decryptMail(recipient, mimeBodyPart);
            decryptedPart.writeTo(outputStream);
        } catch (Exception e) {
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

        Intent result = new Intent();
        result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_SUCCESS);

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
        return null;
    }

    private final Intent encryptAndSign(final Intent data, final ParcelFileDescriptor input, final ParcelFileDescriptor output) {
        return null;
    }
}
