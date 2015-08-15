package de.fau.cs.mad.smile_crypto;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

public class KeyManagement {

    private static ArrayList<KeyInfo> knownOwnKeys = new ArrayList<>();
    private static ArrayList<KeyInfo> knownAllKeys = new ArrayList<>();

    public KeyManagement() {}

    public static Boolean addPrivateKeyFromP12ToKeyStore(String pathToFile, String passphrase) {
        try {
            KeyStore p12 = KeyStore.getInstance("pkcs12");
            p12.load(new FileInputStream(pathToFile), passphrase.toCharArray());
            Enumeration e = p12.aliases();
            while (e.hasMoreElements()) {
                String alias = (String) e.nextElement();
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

            String alias = "SMile_crypto_other_" + getThumbprint(certificate);
            Log.d(SMileCrypto.LOG_TAG, "Check whether certificate is stored for alias: " + alias);

            //Check whether cert is already there
            if(ks.containsAlias(alias)) {
                return true;
            }

            Log.d(SMileCrypto.LOG_TAG, "Alias is not there, import new certificate without private key.");
            ks.setCertificateEntry(alias, certificate);
            return ks.containsAlias(alias);
        }catch (Exception e) {
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
                if(alias.equals(App.getContext().getString(R.string.smile_save_passphrases_certificate_alias)))
                    continue;
                Certificate c = ks.getCertificate(alias);
                KeyStore.Entry entry = ks.getEntry(alias, null);
                if (entry instanceof KeyStore.PrivateKeyEntry) {
                    KeyInfo keyInfo = new KeyInfo();
                    keyInfo.alias = alias;
                    Log.d(SMileCrypto.LOG_TAG, "· Type: " + c.getType());
                    Log.d(SMileCrypto.LOG_TAG, "· HashCode: " + c.hashCode());
                    keyInfo.type = c.getType();
                    keyInfo.hash = Integer.toHexString(c.hashCode());

                    if(c.getType().equals("X.509")) {
                        X509Certificate cert = (X509Certificate) c;
                        String issuerDN = ((X509Certificate) c).getIssuerDN().getName();
                        String email = issuerDN.substring(issuerDN.lastIndexOf("E=") + 2).split(",")[0];
                        Log.d(SMileCrypto.LOG_TAG, "· Email: " + email);
                        keyInfo.mail = email;
                        keyInfo.contact = cert.getSubjectX500Principal().getName();
                        keyInfo.termination_date = new DateTime(cert.getNotAfter());
                        //keyInfo.trust; TODO
                        keyInfo.thumbprint = getThumbprint(cert);
                    }

                    if(!knownOwnKeys.contains(keyInfo)) {
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
        ArrayList<KeyInfo> keylist = new ArrayList<>();
        try {
            Log.d(SMileCrypto.LOG_TAG, "Find all own certificates…");
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            Enumeration e = ks.aliases();
            while (e.hasMoreElements()) {
                String alias = (String) e.nextElement();
                Log.d(SMileCrypto.LOG_TAG, "Found certificate with alias: " + alias);
                if(alias.equals(App.getContext().getString(R.string.smile_save_passphrases_certificate_alias))) {
                    continue;
                }

                Certificate c = ks.getCertificate(alias);
                KeyStore.Entry entry = ks.getEntry(alias, null);
                KeyInfo keyInfo = new KeyInfo();
                keyInfo.alias = alias;
                Log.d(SMileCrypto.LOG_TAG, "· Type: " + c.getType());
                Log.d(SMileCrypto.LOG_TAG, "· HashCode: " + c.hashCode());
                keyInfo.type = c.getType();
                keyInfo.hash = Integer.toHexString(c.hashCode());
                if(c.getType().equals("X.509")) {
                    X509Certificate cert = (X509Certificate) c;
                    Collection<List<?>> alternateNames = cert.getSubjectAlternativeNames();

                    if  (alternateNames != null) {
                        //seems to be always null...
                        for (List<?> names : alternateNames) {
                            for (Object name : names) {
                                if (name instanceof String) {
                                    keyInfo.mailAddresses.add(name.toString());
                                }
                            }
                        }
                    } else {
                        // workaround...
                        String issuerDN = ((X509Certificate) c).getIssuerDN().getName();
                        String email = issuerDN.substring(issuerDN.lastIndexOf("E=") + 2).split(",")[0];
                        Log.d(SMileCrypto.LOG_TAG, "· Email: " + email);
                        keyInfo.mail = email;
                    }

                    keyInfo.contact = cert.getSubjectDN().getName();
                    keyInfo.termination_date = new DateTime(cert.getNotAfter());
                    //keyInfo.trust; TODO
                    keyInfo.thumbprint = getThumbprint(cert);
                }

                if(!knownAllKeys.contains(keyInfo)) {
                    keylist.add(keyInfo);
                }
            }
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error while finding certificate: " + e.getMessage());
            e.printStackTrace();
        }
        knownAllKeys.addAll(keylist);
        return keylist;
    }

    public static Boolean deleteKey(String alias) {
        try {
            Log.d(SMileCrypto.LOG_TAG, "Delete key with alias: " + alias);
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            if(keyStore.containsAlias(alias))
                keyStore.deleteEntry(alias);

            return deletePassphrase(alias) && deleteP12FromInternalDir(alias);
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error while deleting key: " + e.getMessage());
            return false;
        }
    }

    public static String getThumbprint(X509Certificate certificate) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] der = certificate.getEncoded();
        md.update(der);
        byte[] digest = md.digest();
        return bytesToHex(digest);
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
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
            /*FileChannel inChannel = new FileInputStream(src).getChannel();
            FileChannel outChannel = new FileOutputStream(dst).getChannel();

            inChannel.transferTo(0, inChannel.size(), outChannel);
            inChannel.close();
            outChannel.close();

            Log.d(SMileCrypto.LOG_TAG, "Copied p12 to interal storage, filename: " + filename);*/
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
            if(ks.containsAlias(alias)) {
                Log.d(SMileCrypto.LOG_TAG, "Alias " + alias + " already exists.");
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_CERTIFICATE_ALREADY_IMPORTED;
                return alias;
            }

            ks.setKeyEntry(alias, key, null, new Certificate[]{c});

            Toast.makeText(App.getContext(), R.string.import_certificate_successful, Toast.LENGTH_SHORT).show();
            SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_SUCCESS;
            return alias;
        } catch (Exception e){
            Log.e(SMileCrypto.LOG_TAG, "Error while importing certificate: " + e.getMessage());
            Toast.makeText(App.getContext(), R.string.error + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private static Boolean savePassphrase(String alias, String passphrase) {
        try {
            Log.d(SMileCrypto.LOG_TAG, "Encrypt passphrase for alias: " + alias);
            String encryptedPassphrase = PasswordEncryption.encryptString(passphrase);

            if(encryptedPassphrase == null)
                return false;

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