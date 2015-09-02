package de.fau.cs.mad.smile.android.encryption.remote;

import android.content.Context;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import de.fau.cs.mad.smile.android.encryption.App;
import korex.mail.MessagingException;
import korex.mail.Session;
import korex.mail.internet.MimeMessage;

public class MimeMessageLoaderTask extends ContentLoaderTask<MimeMessage> {
    private final InputStream inputStream;
    private File inputFile;

    public MimeMessageLoaderTask(final InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    protected MimeMessage doInBackground(Void... params) {
        Properties props = System.getProperties();
        Session session = Session.getDefaultInstance(props, null);
        try {
            File msgDir = App.getContext().getApplicationContext().getDir("messages", Context.MODE_PRIVATE);
            inputFile = new File(msgDir, "mimemessage");
            FileUtils.copyInputStreamToFile(inputStream, inputFile);
            return new MimeMessage(session, new FileInputStream(inputFile));
        } catch (MessagingException | IOException e) {
            return null;
        }
    }
}
