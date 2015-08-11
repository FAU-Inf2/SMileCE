package de.fau.cs.mad.smile_crypto;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.security.KeyChain;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;

public class ImportCertificateActivity extends ActionBarActivity {

    private Toolbar toolbar;
    protected final int FAB_FILE_CHOOSER_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_certificate);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.navigation_drawer_import_certificate);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().beginTransaction().
                replace(R.id.currentFragment, new ImportCertificateFragment()).commit();

        Log.d(SMileCrypto.LOG_TAG, "Started ImportCertificateActivity.");
        showFileChooser();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info, menu);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FAB_FILE_CHOOSER_REQUEST_CODE:
                //receive result from file manager (--> uri of certificate)
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    String path = PathConverter.getPath(this, uri);
                    Log.d(SMileCrypto.LOG_TAG, "Path to selected certificate: " + path);
                    getSupportFragmentManager().beginTransaction().replace(R.id.currentFragment,
                            ImportCertificateFragment.newInstance(path)).commitAllowingStateLoss();
                    //addCertificateToKeyChain(path);
                    showPassphrasePrompt(path);
                } else {
                    getSupportFragmentManager().beginTransaction().replace(R.id.currentFragment,
                            ImportCertificateFragment.newInstance(getResources().
                                    getString(R.string.import_certificate_no_file))).commitAllowingStateLoss();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/x-pkcs12"); //TODO: more file types?
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            Log.d(SMileCrypto.LOG_TAG, "Call file manager to choose certificate file.");
            startActivityForResult(Intent.createChooser(intent,
                            getResources().getString(R.string.fab_select_import_certificate)),
                    FAB_FILE_CHOOSER_REQUEST_CODE);

        } catch (android.content.ActivityNotFoundException anfe) {
            Log.e(SMileCrypto.LOG_TAG, "No file manager installed. " + anfe.getMessage());
            Toast.makeText(this,
                    getResources().getString(R.string.no_file_manager),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void showPassphrasePrompt(final String pathToFile) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View passphrasePromptView = layoutInflater.inflate(R.layout.passphrase_prompt, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(passphrasePromptView);

        final EditText passphraseUserInput = (EditText) passphrasePromptView.
                findViewById(R.id.passphraseUserInput);
        passphraseUserInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passphraseUserInput.setTransformationMethod(new PasswordTransformationMethod());

        alertDialogBuilder.setCancelable(false).setNegativeButton(getResources().getString(R.string.go),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String passphrase = passphraseUserInput.getText().toString();
                        KeyManagement keyManagement = new KeyManagement();
                        if (!keyManagement.addPrivateKeyFromP12ToKeyStore(pathToFile, passphrase)) {
                            Log.d(SMileCrypto.LOG_TAG, "Wrong passphrase. Show passphrase prompt again.");

                            AlertDialog.Builder builder = new AlertDialog.Builder(ImportCertificateActivity.this);
                            builder.setTitle(getResources().getString(R.string.error));
                            builder.setMessage(getResources().getString(R.string.enter_passphrase_wrong) +
                                    "\n" + getResources().getString(R.string.try_again));

                            builder.setPositiveButton(R.string.cancel, null);
                            builder.setNegativeButton(R.string.retry, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    showPassphrasePrompt(pathToFile);
                                }
                            });
                            builder.create().show();
                        }
                    }
                })
                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }
                );
        alertDialogBuilder.create().show();
    }

    /*depreciated -- can be deleted later (stays here to see how KeyChain works*/
    @Deprecated
    private void addCertificateToKeyChain(String pathToFile) {
        try {
            File file = new File(pathToFile);
            byte[] p12 = new byte[(int) file.length()];
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(p12);
            fileInputStream.close();

            Log.d(SMileCrypto.LOG_TAG, "Import certificate to keychain.");
            Intent importKey = KeyChain.createInstallIntent();
            importKey.putExtra(KeyChain.EXTRA_PKCS12, p12);
            startActivity(importKey);

        } catch (Exception e){
            Log.e(SMileCrypto.LOG_TAG, "Error while importing certificate: " + e.getMessage());
            Toast.makeText(this, R.string.error + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}