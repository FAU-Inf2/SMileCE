package de.fau.cs.mad.smile.android.encryption.remote;

import java.io.InputStream;
import java.util.Properties;

import korex.mail.MessagingException;
import korex.mail.Session;
import korex.mail.internet.MimeMessage;

public class MimeMessageLoaderTask extends ContentLoaderTask<MimeMessage> {
    private final InputStream inputStream;

    public MimeMessageLoaderTask(final InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    protected MimeMessage doInBackground(Void... params) {
        Properties props = System.getProperties();
        Session session = Session.getDefaultInstance(props, null);
        try {
            return new MimeMessage(session, inputStream);
        } catch (MessagingException e) {
            return null;
        }
    }
}
