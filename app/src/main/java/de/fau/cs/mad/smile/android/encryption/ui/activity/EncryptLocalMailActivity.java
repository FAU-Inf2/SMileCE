package de.fau.cs.mad.smile.android.encryption.ui.activity;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import org.spongycastle.mail.smime.SMIMEException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Properties;

import de.fau.cs.mad.smile.android.encryption.DecryptMail;
import de.fau.cs.mad.smile.android.encryption.EncryptMail;
import de.fau.cs.mad.smile.android.encryption.R;
import korex.mail.Address;
import korex.mail.MessagingException;
import korex.mail.Session;
import korex.mail.internet.MimeBodyPart;
import korex.mail.internet.MimeMessage;

public class EncryptLocalMailActivity extends ActionBarActivity {

    private Toolbar toolbar;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decrypt_local_mail);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.navigation_decrypt_local_mail);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTextView = (TextView) findViewById(R.id.decrypt_local_mail_text_view);
        mTextView.setTextSize(12);
        mTextView.setMovementMethod(new ScrollingMovementMethod());
        doStuff();
    }

    private void doStuff() {
        new EncryptDecryptTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class EncryptDecryptTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                final EncryptMail encryptMail = new EncryptMail();
                final DecryptMail decryptMail = new DecryptMail();
                File downloadDir = new File("/sdcard/Download/");
                File sourceFile = new File(downloadDir, "Test-unencrypted.eml");

                Properties props = System.getProperties();
                Session session = Session.getDefaultInstance(props, null);
                MimeMessage mimeMessage = new MimeMessage(session, new FileInputStream(sourceFile));
                Address recipient = mimeMessage.getAllRecipients()[0];
                MimeMessage encryptMessage = encryptMail.encryptMessage(mimeMessage, recipient);

                if (encryptMessage != null) {
                    File encryptedFile = new File(downloadDir, "encrypted.eml");
                    encryptMessage.writeTo(new FileOutputStream(encryptedFile));

                    // decrypt again
                    mimeMessage = new MimeMessage(session, new FileInputStream(encryptedFile));
                    MimeBodyPart encryptedPart = new MimeBodyPart();
                    encryptedPart.setContent(mimeMessage.getContent(), mimeMessage.getContentType());
                    MimeBodyPart decryptedPart = decryptMail.decryptMail(encryptedPart, recipient.toString());
                    //mimeMessage.setContent(decryptedPart.getContent(), decryptedPart.getContentType());
                    //mimeMessage.saveChanges();
                    File decryptedFile = new File(downloadDir, "decrypted.eml");
                    if (decryptedPart != null) {
                        decryptedPart.writeTo(new FileOutputStream(decryptedFile));
                    }
                }
            } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException | MessagingException | SMIMEException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
