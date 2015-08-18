package de.fau.cs.mad.smile_crypto;

import android.content.Context;
import android.content.SharedPreferences;
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
import org.spongycastle.asn1.x509.X509Name;
import org.spongycastle.cert.jcajce.JcaX509CertificateHolder;
import org.spongycastle.jce.PrincipalUtil;
import org.spongycastle.jce.X509Principal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
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

    private static ArrayList<KeyInfo> knownOwnKeys = new ArrayList<>();
    private static ArrayList<KeyInfo> knownAllKeys = new ArrayList<>();

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
        ArrayList<KeyInfo> keylist = new ArrayList<>();
        try {
            Log.d(SMileCrypto.LOG_TAG, "Find all own certificates…");
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            Enumeration e = ks.aliases();
            while (e.hasMoreElements()) {
                String alias = (String) e.nextElement();
                Log.d(SMileCrypto.LOG_TAG, "Found certificate with alias: " + alias);

                if (alias.equals(App.getContext().getString(R.string.smile_save_passphrases_certificate_alias))) {
                    continue;
                }

                Certificate c = ks.getCertificate(alias);
                KeyStore.Entry entry = ks.getEntry(alias, null);
                if (entry instanceof KeyStore.PrivateKeyEntry) {
                    KeyInfo keyInfo = new KeyInfo();
                    keyInfo.alias = alias;
                    Log.d(SMileCrypto.LOG_TAG, "· Type: " + c.getType());
                    Log.d(SMileCrypto.LOG_TAG, "· HashCode: " + c.hashCode());
                    keyInfo.type = c.getType();
                    keyInfo.hash = Integer.toHexString(c.hashCode());

                    if (c.getType().equals("X.509")) {
                        X509Certificate cert = (X509Certificate) c;
                        X500Name x500name = new JcaX509CertificateHolder(cert).getSubject();
                        RDN[] rdn_email = x500name.getRDNs(BCStyle.E);
                        String email = "Certificate does not contain an email address.";
                        if (rdn_email.length > 0) {
                            email = IETFUtils.valueToString(rdn_email[0].getFirst().getValue());
                        }
                        Log.d(SMileCrypto.LOG_TAG, "· Email: " + email);
                        keyInfo.mail = email;
                        RDN[] cn = x500name.getRDNs(BCStyle.CN);
                        if (cn.length > 0) {
                            keyInfo.contact = IETFUtils.valueToString(cn[0].getFirst().getValue());
                        }
                        keyInfo.contact = IETFUtils.valueToString(cn[0].getFirst().getValue());
                        keyInfo.termination_date = new DateTime(cert.getNotAfter());
                        //keyInfo.trust; TODO
                        keyInfo.thumbprint = getThumbprint(cert);
                    }

                    if (!knownOwnKeys.contains(keyInfo)) {
                        keylist.add(keyInfo);
                    }
                } else {
                    //--> no private key available for this certificate
                    //currently there are no such entries because yet we cannot import the certs of
                    //others, e.g. by using their signature.
                    Log.d(SMileCrypto.LOG_TAG, "Not an instance of a PrivateKeyEntry");
                    Log.d(SMileCrypto.LOG_TAG, "· Type: " + c.getType());
                    Log.d(SMileCrypto.LOG_TAG, "· HashCode: " + c.hashCode());
                }
            }
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error while finding certificate: " + e.getMessage());
            e.printStackTrace();
        }

        knownOwnKeys.addAll(keylist);
        return keylist;
    }

    public ArrayList<KeyInfo> getAllCertificates() {
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

                if (androidKeyStore.isKeyEntry(alias) && !knownOwnKeys.contains(keyInfo)) {
                    knownOwnKeys.add(keyInfo);
                }

                if (!knownAllKeys.contains(keyInfo)) {
                    knownAllKeys.add(keyInfo);
                }
            }
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error while finding certificate: " + e.getMessage());
            e.printStackTrace();
        }

        return knownAllKeys;
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

            for (KeyInfo keyInfo : getAllCertificates()) {
                if (keyInfo.hasPrivateKey) {
                    for (String mail : keyInfo.mailAddresses) {
                        if (mailAddress.equals(mail)) {
                            return keyInfo.alias;
                        }
                    }
                }
            }
/*
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            Enumeration e = ks.aliases();
            while (e.hasMoreElements()) {
                String alias = (String) e.nextElement();
                Log.d(SMileCrypto.LOG_TAG, "checking alias: " + alias);
                if (alias.contains("_other_")) {//no private key available
                    continue;
                }

                X509Certificate c = (X509Certificate) ks.getCertificate(alias);
                //Log.d(SMileCrypto.LOG_TAG, c.toString());

                Collection<List<?>> alternateNames = c.getSubjectAlternativeNames();

                if (alternateNames != null) {
                    //seems to be always null...
                    for (List<?> names : alternateNames) {
                        for (Object name : names) {
                            if (name instanceof String) {
                                if (mailAddress.toString().equals(name.toString())) {
                                    Log.d(SMileCrypto.LOG_TAG, "matching mailaddresses");
                                    return alias;
                                }
                            }
                        }
                    }
                }

                //TODO: handle case if one mailaddress has more than one certificate
                if (c.getSubjectDN().getName().contains("E=" + mailAddress.toString())) {
                    Log.d(SMileCrypto.LOG_TAG, "alias found: " + alias);
                    return alias;
                }
            }
*/
            return null;
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error in getAliasByAddress:" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @NonNull
    private final KeyInfo getKeyInfo(final String alias) throws KeyStoreException,
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
            keyInfo.mailAddresses.addAll(getNamesFromCert(cert));
            keyInfo.hasPrivateKey = androidKeyStore.isKeyEntry(alias);

            X500Name x500name = new JcaX509CertificateHolder(cert).getSubject();
            RDN[] cn = x500name.getRDNs(BCStyle.CN);
            if (cn.length > 0) {
                keyInfo.contact = IETFUtils.valueToString(cn[0].getFirst().getValue());
            }

            keyInfo.termination_date = new DateTime(cert.getNotAfter());
            //keyInfo.trust; TODO
            keyInfo.thumbprint = getThumbprint(cert);
        }

        return keyInfo;
    }

    private final List<String> getNamesFromCert(final X509Certificate cert) throws CertificateParsingException, CertificateEncodingException {
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

    public Boolean deleteKey(String alias) {
        try {
            Log.d(SMileCrypto.LOG_TAG, "Delete key with alias: " + alias);

            if (androidKeyStore.containsAlias(alias)) {
                androidKeyStore.deleteEntry(alias);
            }

            if(alias.contains("_other_"))
                return androidKeyStore.containsAlias(alias);

            // just own keys have to be deleted from internal storage
            return deletePassphrase(alias) && deleteP12FromInternalDir(alias);
        } catch (Exception e) {
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
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error copying .p12 to internal storage: " + e.getMessage());
            return false;
        }
    }

    private static String addCertificateToKeyStore(PrivateKey key, X509Certificate c) {
        try {
            Log.d(SMileCrypto.LOG_TAG, "Import certificate to keyStore.");
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);

            String alias = "SMile_crypto_own_" + getThumbprint(c);
            //Check whether cert is already there
            if (ks.containsAlias(alias)) {
                Log.d(SMileCrypto.LOG_TAG, "Alias " + alias + " already exists.");
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_CERTIFICATE_ALREADY_IMPORTED;
                return alias;
            }

            ks.setKeyEntry(alias, key, null, new Certificate[]{c});

            Toast.makeText(App.getContext(), R.string.import_certificate_successful, Toast.LENGTH_SHORT).show();
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_SUCCESS;
            return alias;
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error while importing certificate: " + e.getMessage());
            Toast.makeText(App.getContext(), R.string.error + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private static Boolean savePassphrase(String alias, String passphrase) {
        try {
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
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error while saving passphrase: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static Boolean deleteP12FromInternalDir(String alias) {
        File certDirectory = App.getContext().getApplicationContext().getDir("smime-certificates", Context.MODE_PRIVATE);
        String filename = alias + ".p12";
        File toBeDeleted = new File(certDirectory, filename);
        return toBeDeleted.delete();
    }

    private static Boolean deletePassphrase(String alias) {
        SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(App.getContext().getApplicationContext()).edit();
        e.remove(alias + "-passphrase");
        e.commit();
        return true;
    }
}