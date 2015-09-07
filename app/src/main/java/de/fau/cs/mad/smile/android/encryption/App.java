package de.fau.cs.mad.smile.android.encryption;

import android.app.Application;
import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.util.Log;

import net.danlew.android.joda.JodaTimeAndroid;

import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.security.auth.x500.X500Principal;

public class App extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        JodaTimeAndroid.init(this);
        generateKeyPair();
    }

    public static Context getContext() {
        return mContext;
    }

    private void generateKeyPair() {
        /* When app starts first, create certificate for PasswordEncryption. */
        String passwordEncryptionCertificateAlias = getResources().getString(R.string.smile_save_passphrases_certificate_alias);

        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            if (keyStore.containsAlias(passwordEncryptionCertificateAlias)) {
                return;
            }

            Calendar start = new GregorianCalendar();
            Calendar end = new GregorianCalendar();
            end.add(Calendar.YEAR, 5);

            final KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(mContext)
                    .setAlias(passwordEncryptionCertificateAlias)
                    .setSubject(new X500Principal("CN=SMile-crypto-Password-Encrypt"))
                    .setSerialNumber(BigInteger.ONE)
                    .setStartDate(start.getTime())
                    .setEndDate(end.getTime())
                    .build();

            final KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
            gen.initialize(spec);
            gen.generateKeyPair();
        } catch (Exception e) {
            if(SMileCrypto.DEBUG) {
                Log.e(SMileCrypto.LOG_TAG, "ERROR: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }
}