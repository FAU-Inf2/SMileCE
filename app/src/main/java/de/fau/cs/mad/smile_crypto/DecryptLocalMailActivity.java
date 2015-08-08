package de.fau.cs.mad.smile_crypto;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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

import javax.mail.internet.MimeMessage;

public class DecryptLocalMailActivity extends ActionBarActivity {
    private Toolbar toolbar;
    protected final int DLMA_FILE_CHOOSER_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decrypt_local_mail);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.navigation_decrypt_local_mail);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().beginTransaction().
                replace(R.id.currentFragment, new DecryptLocalMailFragment()).commit();

        Log.d(SMileCrypto.LOG_TAG, "Started DecryptLocalMailActivity.");
        showFileChooser();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_decrypt_local_mail, menu);
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
            case DLMA_FILE_CHOOSER_REQUEST_CODE:
                //receive result from file manager (--> uri of certificate)
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    String path = PathConverter.getPath(this, uri);
                    Log.d(SMileCrypto.LOG_TAG, "Path to selected file: " + path);

                    if(path.endsWith(".eml")) {
                        Log.d(SMileCrypto.LOG_TAG, " " + path);
                        getSupportFragmentManager().beginTransaction().replace(R.id.currentFragment,
                                DecryptLocalMailFragment.newInstance(path)).commitAllowingStateLoss();
                        passphraseDecryptOrPrompt(path);
                    } else {
                        Toast.makeText(this, R.string.not_eml, Toast.LENGTH_LONG).show();
                        getSupportFragmentManager().beginTransaction().replace(R.id.currentFragment,
                                DecryptLocalMailFragment.newInstance(getResources().getString(R.string.not_eml))).
                                commitAllowingStateLoss();
                    }
                } else {
                    getSupportFragmentManager().beginTransaction().replace(R.id.currentFragment,
                            DecryptLocalMailFragment.newInstance(getResources().
                                    getString(R.string.import_certificate_no_file))).commitAllowingStateLoss();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        //intent.setType("message/rfc822"); //TODO: does not exist in Android :-(
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            Log.d(SMileCrypto.LOG_TAG, "Call file manager to choose decrypted mail.");
            startActivityForResult(Intent.createChooser(intent,
                            getResources().getString(R.string.import_decrypted_file)),
                    DLMA_FILE_CHOOSER_REQUEST_CODE);

        } catch (android.content.ActivityNotFoundException anfe) {
            Log.e(SMileCrypto.LOG_TAG, "No file manager installed. " + anfe.getMessage());
            Toast.makeText(this,
                    getResources().getString(R.string.no_file_manager),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void passphraseDecryptOrPrompt(String pathToFile) {
        DecryptMail decryptMail = new DecryptMail(this.getApplicationContext().getDir(
                getString(R.string.smime_certificates_folder), Context.MODE_PRIVATE).getAbsolutePath());
        String passphrase;
        MimeMessage m = decryptMail.getMimeMessageFromFile(pathToFile);
        if(m == null) {
            showPassphrasePrompt(pathToFile);
            return;
        }
        String alias = decryptMail.getAliasByMimeMessage(m);
        if(alias == null) {
            showPassphrasePrompt(pathToFile);
            return;
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(preferences.contains(alias+"-passphrase")) {
            String encryptedPassphrase = preferences.getString(alias + "-passphrase", null);
            Log.d(SMileCrypto.LOG_TAG, "Passphrase: " + encryptedPassphrase);
            try {
                PasswordEncryption passwordEncryption = new PasswordEncryption(this,
                        getResources().getString(R.string.smile_save_passphrases_certificate_alias));

                Log.d(SMileCrypto.LOG_TAG, "Decrypt passphrase for alias: " + alias);
                passphrase = passwordEncryption.decryptString(encryptedPassphrase);

                if (passphrase == null) {
                    Log.d(SMileCrypto.LOG_TAG, "Decrypted passphrase was null.");
                    showPassphrasePrompt(pathToFile);
                    return;
                }
                Log.d(SMileCrypto.LOG_TAG, "Got decrypted passphrase.");
            } catch (Exception e) {
                showPassphrasePrompt(pathToFile);
                return;
            }
        } else {
            showPassphrasePrompt(pathToFile);
            return;
        }
        decryptFile(pathToFile, passphrase);
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
                    if (!decryptFile(pathToFile, passphraseUserInput.getText().toString())) {
                        Log.d(SMileCrypto.LOG_TAG, "Maybe wrong passphrase. Show passphrase prompt again.");

                        AlertDialog.Builder builder = new AlertDialog.Builder(DecryptLocalMailActivity.this);
                        builder.setTitle(getResources().getString(R.string.error));
                        builder.setMessage(getResources().getString(R.string.something_went_wrong) +
                                "\n" + getResources().getString(R.string.ask_try_again));

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
            }).setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }
        );
        alertDialogBuilder.create().show();
    }

    private Boolean decryptFile(String pathToFile, String passphrase) {
        DecryptMail decryptMail = new DecryptMail(this.getApplicationContext().getDir(
                getString(R.string.smime_certificates_folder), Context.MODE_PRIVATE).getAbsolutePath());
        //MimeBodyPart bodyPart = decryptMail.decryptMail(pathToFile, passphrase);
        //if(bodyPart == null)
        //    return false;
        //String result = decryptMail.convertMimeBodyPartToString(bodyPart);

        String result = decryptMail.decryptEncodeMail(pathToFile, passphrase);
        if (result == null)
            return false;

        getSupportFragmentManager().beginTransaction().replace(R.id.currentFragment,
                DecryptLocalMailFragment.newInstanceDecryptedContent(result)).commitAllowingStateLoss();
        return true;
    }
}
