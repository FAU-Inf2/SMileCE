package de.fau.cs.mad.smile.android.encryption.remote;


import korex.mail.internet.MimeMessage;

public class MimeMessageLoaderTaskBuilder extends AbstractContentLoaderTaskBuilder<MimeMessage> {
    @Override
    public ContentLoaderTask<MimeMessage> build() {
        return new MimeMessageLoaderTask(getInputStream());
    }
}
