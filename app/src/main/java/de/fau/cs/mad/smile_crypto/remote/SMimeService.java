package de.fau.cs.mad.smile_crypto.remote;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import org.spongycastle.asn1.cms.RecipientInfo;
import org.spongycastle.cms.CMSException;
import org.spongycastle.cms.Recipient;
import org.spongycastle.cms.RecipientId;
import org.spongycastle.cms.RecipientInformation;
import org.spongycastle.mail.smime.SMIMEEnveloped;
import org.spongycastle.mail.smime.SMIMESigned;
import org.spongycastle.mail.smime.SMIMEUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import de.fau.cs.mad.javax.activation.DataSource;
import de.fau.cs.mad.smile_crypto.DecryptMail;
import de.fau.cs.mad.smile_crypto.SMIMEToolkit;
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

        try {
            /*MimeBodyPart mimeBodyPart = new MimeBodyPart(inputStream);
            SMIMEEnveloped enveloped = new SMIMEEnveloped(mimeBodyPart);
            Collection<RecipientInformation> recipients = enveloped.getRecipientInfos().getRecipients();

            for(RecipientInformation recipient : recipients) {
                RecipientId id = recipient.getRID();
                //MimeBodyPart part = SMIMEUtil.toMimeBodyPart(recipient.getContent(null, "BC"));
            }

            */

            final DecryptMail decryptMail = new DecryptMail();
            MimeBodyPart mimeBodyPart = new MimeBodyPart(inputStream);
            MimeBodyPart decryptedPart = decryptMail.decryptMail(recipient, mimeBodyPart);
            decryptedPart.writeTo(outputStream);
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
        }

        Intent result = new Intent();
        result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_SUCCESS);

        Log.d(SMileCrypto.LOG_TAG, "decryptAndVerify: returning intent: " + result);
        return result;
    }

    private final Intent sign(final Intent data, final ParcelFileDescriptor input, final ParcelFileDescriptor output) {
        return null;
    }

    private final Intent encryptAndSign(final Intent data, final ParcelFileDescriptor input, final ParcelFileDescriptor output) {
        return null;
    }
}
