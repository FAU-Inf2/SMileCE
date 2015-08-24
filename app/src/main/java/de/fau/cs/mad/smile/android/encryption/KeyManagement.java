package de.fau.cs.mad.smile.android.encryption;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;
import org.spongycastle.asn1.x500.RDN;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x500.style.BCStyle;
import org.spongycastle.asn1.x500.style.IETFUtils;
import org.spongycastle.cert.jcajce.JcaX509CertificateHolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

public class KeyManagement {

    private final String certificateDirectory;
    private final KeyStore androidKeyStore;

    public KeyManagement() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        final Context context = App.getContext();
        this.certificateDirectory = context.getDir(
                context.getString(R.string.smime_certificates_folder), Context.MODE_PRIVATE).
                getAbsolutePath();
        this.androidKeyStore = KeyStore.getInstance("AndroidKeyStore");
        androidKeyStore.load(null);
    }

    public static Boolean addPrivateKeyFromP12ToKeyStore(String pathToFile, String passphrase) {
        try {
            KeyStore p12 = KeyStore.getInstance("pkcs12");
            p12.load(new FileInputStream(pathToFile), passphrase.toCharArray());
            Enumeration e = p12.aliases();
            while (e.hasMoreElements()) {
                String alias = (String) e.nextElement();
                if (!p12.isKeyEntry(alias)) {
                    continue;
                }

                X509Certificate c = (X509Certificate) p12.getCertificate(alias);
                Log.d(SMileCrypto.LOG_TAG, "Found certificate with alias: " + alias);
                Log.d(SMileCrypto.LOG_TAG, "· SubjectDN: " + c.getSubjectDN().getName());
                Log.d(SMileCrypto.LOG_TAG, "· IssuerDN: " + c.getIssuerDN().getName());

                PrivateKey key = (PrivateKey) p12.getKey(alias, passphrase.toCharArray());
                String new_alias = addCertificateToKeyStore(key, c);

                copyP12ToInternalDir(pathToFile, new_alias);
                return savePassphrase(new_alias, passphrase);
            }
            return true;
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error while loading keyStore: " + e.getMessage());
            return false;
        }
    }

    /*TODO: add certificate from someone else (without private key) */
    public static Boolean addFriendsCertificate(X509Certificate certificate) {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);

            String thumbprint = getThumbprint(certificate);
            String alias = "SMile_crypto_other_" + thumbprint;
            Log.d(SMileCrypto.LOG_TAG, "Check whether certificate is stored for alias: " + alias);

            //Check whether cert is already there
            if (ks.containsAlias(alias)) {
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_CERTIFICATE_ALREADY_IMPORTED;
                return true;
            }

            //Check whether cert is already there because it's our own (= have private key)
            if (ks.containsAlias("SMile_crypto_own_" + thumbprint)) {
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_CERTIFICATE_ALREADY_IMPORTED;
                return true;
            }

            Log.d(SMileCrypto.LOG_TAG, "Alias is not there, import new certificate without private key.");
            ks.setCertificateEntry(alias, certificate);
            return ks.containsAlias(alias);
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error in x: " + e.getMessage());
            return false;
        }
    }

    public ArrayList<KeyInfo> getOwnCertificates() {
        ArrayList<KeyInfo> keyList = new ArrayList<>();
        for (KeyInfo keyInfo : getAllCertificates()) {
            if(keyInfo.hasPrivateKey) {
                keyList.add(keyInfo);
            }
        }

        return keyList;
    }

    public ArrayList<KeyInfo> getAllCertificates() {
        ArrayList<KeyInfo> erg = new ArrayList<>();
        try {
            Log.d(SMileCrypto.LOG_TAG, "Find all own certificates…");

            Enumeration e = androidKeyStore.aliases();
            while (e.hasMoreElements()) {
                String alias = (String) e.nextElement();
                Log.d(SMileCrypto.LOG_TAG, "Found certificate with alias: " + alias);
                if (alias.equals(App.getContext().getString(R.string.smile_save_passphrases_certificate_alias))) {
                    continue;
                }

                KeyInfo keyInfo = getKeyInfo(alias);

                erg.add(keyInfo);
            }
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error while finding certificate: " + e.getMessage());
            e.printStackTrace();
        }

        return erg;
    }

    public final X509Certificate getCertificateForAlias(final String alias) throws KeyStoreException {
        return (X509Certificate) androidKeyStore.getCertificate(alias);
    }

    public PrivateKey getPrivateKeyForAlias(final String alias, final String passphrase) throws KeyStoreException {
        KeyStore p12 = KeyStore.getInstance("pkcs12");
        String pathTop12File = FilenameUtils.concat(certificateDirectory, alias + ".p12");
        Log.d(SMileCrypto.LOG_TAG, "certificate file path: " + pathTop12File);
        File p12File = new File(pathTop12File);

        if (!p12File.exists()) {
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_CERTIFICATE_STORED;
            return null;
        }

        PrivateKey privateKey = null;
        try {
            p12.load(new FileInputStream(p12File), passphrase.toCharArray());
            Enumeration e = p12.aliases();

            while (e.hasMoreElements()) {
                String aliasp12 = (String) e.nextElement();
                if (p12.isKeyEntry(aliasp12)) {
                    privateKey = (PrivateKey) p12.getKey(aliasp12, passphrase.toCharArray());
                }
            }
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error, probably wrong passphrase: " + e.getMessage());
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_WRONG_PASSPHRASE;
            return null;
        }

        return privateKey;
    }

    public final String getPassphraseForAlias(final String alias) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext().
                getApplicationContext());

        if (preferences.contains(alias + "-passphrase")) {
            String encryptedPassphrase = preferences.getString(alias + "-passphrase", null);
            //Log.d(SMileCrypto.LOG_TAG, "Passphrase: " + encryptedPassphrase);
            Log.d(SMileCrypto.LOG_TAG, "Encrypted passphrase found.");

            try {
                Log.d(SMileCrypto.LOG_TAG, "Decrypt passphrase for alias: " + alias);
                return PasswordEncryption.decryptString(encryptedPassphrase);
            } catch (Exception e) {
                Log.e(SMileCrypto.LOG_TAG, "Error while decrypting passphrase: " + e.getMessage());
                return null;
            }
        }

        return null;
    }

    public final String getAliasByAddress(final Address emailAddress) {
        try {
            String mailAddress = emailAddress.toString();
            if (emailAddress instanceof InternetAddress) {
                mailAddress = ((InternetAddress) emailAddress).getAddress();
            }

            Log.d(SMileCrypto.LOG_TAG, "looking up alias for: " + mailAddress);

            for (KeyInfo keyInfo : getOwnCertificates()) {
                for (String mail : keyInfo.mailAddresses) {
                    if (mailAddress.equalsIgnoreCase(mail)) {
                        return keyInfo.alias;
                    }
                }
            }

            return null;
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error in getAliasByAddress:" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<KeyInfo> getKeyInfoByOwnAddress(final Address emailAddress) {
        ArrayList<KeyInfo> keyInfos = new ArrayList<>();
        try {
            String mailAddress = emailAddress.toString();
            if (emailAddress instanceof InternetAddress) {
                mailAddress = ((InternetAddress) emailAddress).getAddress();
            }

            Log.d(SMileCrypto.LOG_TAG, "Looking up alias for: " + mailAddress);

            for (KeyInfo keyInfo : getOwnCertificates()) {
                if (mailAddress.equals(keyInfo.mail)) {
                    keyInfos.add(keyInfo);
                }
            }
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error in getAliasByAddress:" + e.getMessage());
            e.printStackTrace();
        }
        return keyInfos;
    }

    public ArrayList<String> getAliasesByOwnAddress(final Address emailAddress) {
        ArrayList<KeyInfo> keyInfos = getKeyInfoByOwnAddress(emailAddress);
        ArrayList<String> aliases = new ArrayList<>();
        for(KeyInfo keyInfo : keyInfos) {
            String alias = keyInfo.alias;
            if(alias != null) {
                aliases.add(alias);
            }
        }
        return aliases;
    }

    @NonNull
    public final KeyInfo getKeyInfo(final String alias) throws KeyStoreException,
            CertificateParsingException, CertificateEncodingException, NoSuchAlgorithmException {
        Certificate c = androidKeyStore.getCertificate(alias); // maybe hand in certificate?
        KeyInfo keyInfo = new KeyInfo();
        keyInfo.alias = alias;
        Log.d(SMileCrypto.LOG_TAG, "· Type: " + c.getType());
        Log.d(SMileCrypto.LOG_TAG, "· HashCode: " + c.hashCode());
        keyInfo.type = c.getType();
        keyInfo.hash = Integer.toHexString(c.hashCode());

        if (c.getType().equals("X.509")) {
            X509Certificate cert = (X509Certificate) c;
            keyInfo.certificate = cert;
            keyInfo.mailAddresses.addAll(getAlternateNamesFromCert(cert));
            keyInfo.hasPrivateKey = androidKeyStore.isKeyEntry(alias);

            X500Name x500name = new JcaX509CertificateHolder(cert).getSubject();
            RDN[] cn = x500name.getRDNs(BCStyle.CN);
            if (cn.length > 0) {
                keyInfo.contact = IETFUtils.valueToString(cn[0].getFirst().getValue());
            }

            RDN[] rdn_email = x500name.getRDNs(BCStyle.E);
            String email = "";
            if (rdn_email.length > 0) {
                email = IETFUtils.valueToString(rdn_email[0].getFirst().getValue());
            }
            Log.d(SMileCrypto.LOG_TAG, "· Email: " + email);
            keyInfo.mail = email;

            keyInfo.termination_date = new DateTime(cert.getNotAfter());
            keyInfo.valid_after = new DateTime((cert.getNotBefore()));
            //keyInfo.trust; TODO
            keyInfo.thumbprint = getThumbprint(cert);
        }

        return keyInfo;
    }

    public List<String> getAlternateNamesFromCert(final X509Certificate cert) throws CertificateParsingException, CertificateEncodingException {
        ArrayList<String> altNames = new ArrayList<>();
        Collection<List<?>> alternateNames = cert.getSubjectAlternativeNames();

        if (alternateNames != null) {
            //seems to be always null...
            for (List<?> names : alternateNames) {
                for (Object name : names) {
                    if (name instanceof String) {
                        altNames.add(name.toString());
                    }
                }
            }
        } else {
            // workaround...
            X500Name x500name = new JcaX509CertificateHolder(cert).getSubject();
            RDN[] rdn_email = x500name.getRDNs(BCStyle.E);
            for (int i = 0; i < rdn_email.length; i++) {
                altNames.add(IETFUtils.valueToString(rdn_email[i].getFirst().getValue()));
            }
        }

        return altNames;
    }

    public boolean deleteKey(final String alias) {
        try {
            Log.d(SMileCrypto.LOG_TAG, "Delete key with alias: " + alias);

            if (androidKeyStore.containsAlias(alias)) {
                androidKeyStore.deleteEntry(alias);
            }

            if(alias.contains("_other_")) {
                return !androidKeyStore.containsAlias(alias);
            }

            // just own keys have to be deleted from internal storage
            return deletePassphrase(alias) && deleteP12FromInternalDir(alias);
        } catch (KeyStoreException e) {
            Log.e(SMileCrypto.LOG_TAG, "Error while deleting key: " + e.getMessage());
            return false;
        }
    }

    public static String getThumbprint(final X509Certificate certificate)
            throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] der = certificate.getEncoded();
        md.update(der);
        byte[] digest = md.digest();
        return bytesToHex(digest);
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }

    private static Boolean copyP12ToInternalDir(String pathToFile, String alias) {
        File src = new File(pathToFile);
        File certDirectory = App.getContext().getApplicationContext().getDir("smime-certificates", Context.MODE_PRIVATE);
        String filename = alias + ".p12";
        File dst = new File(certDirectory, filename);

        try {
            org.apache.commons.io.FileUtils.copyFile(src, dst);
            Log.d(SMileCrypto.LOG_TAG, "Copied p12 to interal storage, filename: " + filename);
            return true;
        } catch (IOException e) {
            Log.e(SMileCrypto.LOG_TAG, "Error copying .p12 to internal storage: " + e.getMessage());
            return false;
        }
    }

    public static String copyP12ToSDCard(String alias) {
        File certDirectory = App.getContext().getApplicationContext().getDir("smime-certificates", Context.MODE_PRIVATE);
        String filename = alias + ".p12";
        File src = new File(certDirectory, filename);

        File dstDirectory =  new File(Environment.getExternalStorageDirectory(), "SMile-crypto/export/");
        dstDirectory.mkdirs(); // create folder if it does not exist yet

        File dst = new File(dstDirectory, filename);
        try {
            org.apache.commons.io.FileUtils.copyFile(src, dst);
            Log.d(SMileCrypto.LOG_TAG, "Copied p12 from interal storage to SD-card.");
            return dst.getAbsolutePath();
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error copying .p12 to external storage: " + e.getMessage());
            return null;
        }
    }

    public static String copyCertificateToSDCard(X509Certificate certificate, String alias) {
        if(certificate == null || alias == null) {
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_INVALID_PARAMETER;
            Log.e(SMileCrypto.LOG_TAG, "Called copyCertificateToSDCard with invalid parameters.");
            return null;
        }

        String filename = alias + ".crt";
        File dstDirectory =  new File(Environment.getExternalStorageDirectory(), "SMile-crypto/export/");
        dstDirectory.mkdirs(); // create folder if it does not exist yet

        File dst = new File(dstDirectory, filename);
        try {
            Log.d(SMileCrypto.LOG_TAG, "Export certificate with alias: " + alias);
            FileOutputStream fos = new FileOutputStream(dst);
            fos.write(certificate.getEncoded());
            fos.flush();
            fos.close();
            Log.d(SMileCrypto.LOG_TAG, "Exported certificate to SD-card.");
            return dst.getAbsolutePath();
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error exporting certificate to external storage: " + e.getMessage());
            return null;
        }
    }

    private static String addCertificateToKeyStore(final PrivateKey key, final X509Certificate certificate) {
        try {
            Log.d(SMileCrypto.LOG_TAG, "Import certificate to keyStore.");
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);

            String alias = "SMile_crypto_own_" + getThumbprint(certificate);
            //Check whether cert is already there
            if (ks.containsAlias(alias)) {
                Log.d(SMileCrypto.LOG_TAG, "Alias " + alias + " already exists.");
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_CERTIFICATE_ALREADY_IMPORTED;
                return alias;
            }

            ks.setKeyEntry(alias, key, null, new Certificate[]{certificate});

            Toast.makeText(App.getContext(), R.string.import_certificate_successful, Toast.LENGTH_SHORT).show();
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_SUCCESS;
            return alias;
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error while importing certificate: " + e.getMessage());
            Toast.makeText(App.getContext(), R.string.error + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private static Boolean savePassphrase(final String alias, final String passphrase) {
        Log.d(SMileCrypto.LOG_TAG, "Encrypt passphrase for alias: " + alias);
        String encryptedPassphrase = PasswordEncryption.encryptString(passphrase);

        if (encryptedPassphrase == null) {
            return false;
        }

        Log.d(SMileCrypto.LOG_TAG, "Encrypted passphrase will be saved in preferences:  <sensitive>");

        SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(App.getContext().getApplicationContext()).edit();
        e.putString(alias + "-passphrase", encryptedPassphrase);
        e.commit();

        return true;
    }

    private static Boolean deleteP12FromInternalDir(final String alias) {
        File certDirectory = App.getContext().getApplicationContext().getDir("smime-certificates", Context.MODE_PRIVATE);
        String filename = alias + ".p12";
        File toBeDeleted = new File(certDirectory, filename);
        return toBeDeleted.delete();
    }

    private static Boolean deletePassphrase(final String alias) {
        SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(App.getContext().getApplicationContext()).edit();
        e.remove(alias + "-passphrase");
        e.commit();
        return true;
    }
}