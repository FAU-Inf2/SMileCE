package de.fau.cs.mad.smile_crypto.remote;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import de.fau.cs.mad.javax.activation.DataSource;
import de.fau.cs.mad.smile_crypto.DecryptMail;
import de.fau.cs.mad.smime_api.ISMimeService;
import de.fau.cs.mad.smime_api.SMimeApi;

public class SMimeService extends Service {

    private final ISMimeService.Stub mBinder = new ISMimeService.Stub() {
        @Override
        public Intent execute(Intent data, ParcelFileDescriptor input, ParcelFileDescriptor output) throws RemoteException {
            String action = data.getAction();
            switch (action) {
                case SMimeApi.ACTION_SIGN:
                    sign(data, input, output);
                    break;
                case SMimeApi.ACTION_ENCRYPT_AND_SIGN:
                    encryptAndSign(data, input, output);
                    break;
                case SMimeApi.ACTION_DECRYPT_VERIFY:
                    decryptAndVerify(data, input, output);
                    break;
                default:
                    return null;
            }
            return null;
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

        try {
            MimeBodyPart mimeBodyPart = new MimeBodyPart(inputStream);
            final String certDir = getApplicationContext().getDir("smime-certificates", Context.MODE_PRIVATE).getAbsolutePath();
            final DecryptMail decryptMail = new DecryptMail(certDir);
            MimeMessage mimeMessage = decryptMail.decodeMimeBodyParts(mimeBodyPart);
            mimeMessage.writeTo(outputStream);
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO:
        }

        Intent result = new Intent();
        result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_SUCCESS);
        return result;
    }

    private final Intent sign(final Intent data, final ParcelFileDescriptor input, final ParcelFileDescriptor output) {
        return null;
    }

    private final Intent encryptAndSign(final Intent data, final ParcelFileDescriptor input, final ParcelFileDescriptor output) {
        return null;
    }
}
