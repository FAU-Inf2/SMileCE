package de.fau.cs.mad.smile.android.encryption.crypto;

import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyStore;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import de.fau.cs.mad.smile.android.encryption.App;
import de.fau.cs.mad.smile.android.encryption.R;
import de.fau.cs.mad.smile.android.encryption.SMileCrypto;

public class PasswordEncryption {
    public PasswordEncryption() {
    }

    public static String encryptString(String passphrase) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            String passwordEncryptionCertificateAlias = App.getContext().getResources().getString(R.string.smile_save_passphrases_certificate_alias);

            if (!keyStore.containsAlias(passwordEncryptionCertificateAlias)) {
                return null;
            }

            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)
                    keyStore.getEntry(passwordEncryptionCertificateAlias, null);
            RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();

            Cipher input = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
            input.init(Cipher.ENCRYPT_MODE, publicKey);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(
                    outputStream, input);
            cipherOutputStream.write(passphrase.getBytes("UTF-8"));
            cipherOutputStream.close();

            return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            if(SMileCrypto.isDEBUG()) {
                Log.e(SMileCrypto.LOG_TAG, Log.getStackTraceString(e));
            }
            return null;
        }
    }

    public static String decryptString(String encryptedPassphrase) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            ;
            String passwordEncryptionCertificateAlias = App.getContext().getResources().getString(R.string.smile_save_passphrases_certificate_alias);

            if (!keyStore.containsAlias(passwordEncryptionCertificateAlias)) {
                return null;
            }

            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)
                    keyStore.getEntry(passwordEncryptionCertificateAlias, null);
            RSAPrivateKey privateKey = (RSAPrivateKey) privateKeyEntry.getPrivateKey();

            Cipher output = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
            output.init(Cipher.DECRYPT_MODE, privateKey);

            CipherInputStream cipherInputStream = new CipherInputStream(
                    new ByteArrayInputStream(Base64.decode(encryptedPassphrase, Base64.DEFAULT)), output);

            ArrayList<Byte> values = new ArrayList<>();
            int nextByte;

            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte) nextByte);
            }

            byte[] bytes = new byte[values.size()];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = values.get(i).byteValue();
            }

            return new String(bytes, 0, bytes.length, "UTF-8");
        } catch (Exception e) {
            if(SMileCrypto.isDEBUG()) {
                Log.e(SMileCrypto.LOG_TAG, Log.getStackTraceString(e));
            }
            return null;
        }
    }
}
