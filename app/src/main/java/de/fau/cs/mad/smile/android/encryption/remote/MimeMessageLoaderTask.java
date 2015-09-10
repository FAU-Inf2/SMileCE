package de.fau.cs.mad.smile.android.encryption.remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import de.fau.cs.mad.smile.android.encryption.SMileCrypto;
import korex.mail.MessagingException;
import korex.mail.Session;
import korex.mail.internet.MimeMessage;

public class MimeMessageLoaderTask extends ContentLoaderTask<MimeMessage> {
    private File inputFile;

    public MimeMessageLoaderTask(final InputStream inputStream) {
        super(inputStream);
    }

    @Override
    protected MimeMessage doInBackground(Void... params) {
        Properties props = System.getProperties();
        Session session = Session.getDefaultInstance(props, null);
        try {
            InputStream inputStream = getInputStream();
            if(SMileCrypto.isDEBUG()) {
                inputFile = copyToFile(inputStream);
                inputStream = new FileInputStream(inputFile);
            }

            return new MimeMessage(session, inputStream);
        } catch (MessagingException | IOException e) {
            return null;
        }
    }
}
