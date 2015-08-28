package de.fau.cs.mad.smile.android.encryption.remote;

import java.io.InputStream;

import korex.mail.MessagingException;
import korex.mail.internet.MimeBodyPart;

public class MimeBodyLoaderTask extends ContentLoaderTask<MimeBodyPart> {
    private final InputStream inputStream;

    public MimeBodyLoaderTask(final InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    protected MimeBodyPart doInBackground(Void... params) {
        try {
            return new MimeBodyPart(inputStream);
        } catch (MessagingException e) {
            return null;
        }
    }
}
