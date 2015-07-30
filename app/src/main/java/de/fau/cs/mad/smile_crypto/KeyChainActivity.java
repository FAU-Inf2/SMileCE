package de.fau.cs.mad.smile_crypto;

import android.security.KeyChainAliasCallback;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Enumeration;


public class KeyChainActivity extends ActionBarActivity implements
        KeyChainAliasCallback {
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_chain);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.navigation_drawer_key_chain);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().beginTransaction().
                replace(R.id.currentFragment, new DefaultFragment()).commit();

        Log.d(SMileCrypto.LOG_TAG, "Started KeyChainActivity.");
        listCertificates();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_key_chain, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void listCertificates() {
        try {
            Log.d(SMileCrypto.LOG_TAG, "Get all certificates from AndroidCAStore.");
            KeyStore ks = KeyStore.getInstance("AndroidCAStore");
            ks.load(null, null);
            Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                //Log.d(SMileCrypto.LOG_TAG, "Alias: " + alias);
                if(alias.contains("user")) {
                    Log.d(SMileCrypto.LOG_TAG, "Found user certificate.");
                    X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
                    Log.d(SMileCrypto.LOG_TAG, "Subject DN: " +
                            cert.getSubjectDN().getName());
                    Log.d(SMileCrypto.LOG_TAG, "Subject SN: " +
                            cert.getSerialNumber().toString());
                    Log.d(SMileCrypto.LOG_TAG, "Issuer DN: " +
                            cert.getIssuerDN().getName());
                    Log.d(SMileCrypto.LOG_TAG, "Public key: " +
                            cert.getPublicKey().toString());

                    //TODO: workaround to choose this cert
                    chooseCert(alias);
                }
            }
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, e.getMessage());
        }
    }

    private void chooseCert(String alias) {
        android.security.KeyChain.choosePrivateKeyAlias(this,
                this, // Callback
                new String[]{}, // Any key types.
                null, // Any issuers.
                "localhost", // Any host
                -1, // Any port
                alias);
    }

    @Override
    public void alias(String alias) {
        try {
            PrivateKey privateKey = android.security.KeyChain.getPrivateKey(this, alias);
            //TODO: remove this after testing, this leaks private key!
            Log.d(SMileCrypto.LOG_TAG, "PRIVATE-KEY: " + privateKey.toString());
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, e.getMessage());
        }
    }

}
