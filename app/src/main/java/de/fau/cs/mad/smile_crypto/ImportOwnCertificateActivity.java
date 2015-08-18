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
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;

public class ImportOwnCertificateActivity extends ActionBarActivity {

    private Toolbar toolbar;
    protected final int FAB_FILE_CHOOSER_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_own_certificate);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.navigation_drawer_import_certificate);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Log.d(SMileCrypto.LOG_TAG, "Started ImportOwnCertificateActivity.");
        final Intent intent = getIntent();
        final String action = intent.getAction();

        if(Intent.ACTION_VIEW.equals(action)){
            Log.d(SMileCrypto.LOG_TAG, "Intent contains path to file.");
            Uri uri = intent.getData();
            String path = PathConverter.getPath(this, uri);
            TextView textView = (TextView) findViewById(R.id.import_text_view);
            textView.setText(getString(R.string.import_certificate_show_path) + path);
            Log.d(SMileCrypto.LOG_TAG, "Path to file is " + path);
            if(path == null)
                showFileChooser();

            if(path.endsWith(".p12")) {
                showPassphrasePrompt(path);
            } else {
                Log.d(SMileCrypto.LOG_TAG, "Not a p12-file -- show file chooser.");
                showFileChooser();
            }
        } else {
            Log.d(SMileCrypto.LOG_TAG, "Intent was something else: " + action);
            Log.d(SMileCrypto.LOG_TAG, "Show file chooser.");
            showFileChooser();
        }
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
                    TextView textView = (TextView) this.findViewById(R.id.import_text_view);
                    textView.setText(getString(R.string.import_certificate_show_path) + path);
                    showPassphrasePrompt(path);
                } else {
                    noFileSelected();
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
            AlertDialog.Builder builder = new AlertDialog.Builder(ImportOwnCertificateActivity.this);
            builder.setTitle(getResources().getString(R.string.error));
            builder.setMessage(getResources().getString(R.string.no_file_manager));

            builder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            builder.create().show();
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
                        if (!KeyManagement.addPrivateKeyFromP12ToKeyStore(pathToFile, passphrase)) {
                            wrongPassphrase(pathToFile);
                        } else {
                            importSuccessful();
                        }
                    }
                })
                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //dialog.dismiss();
                                finish();
                            }
                        }
                );
        alertDialogBuilder.create().show();
    }

    private void wrongPassphrase(final String pathToFile) {
        Log.d(SMileCrypto.LOG_TAG, "Wrong passphrase. Show passphrase prompt again.");

        AlertDialog.Builder builder = new AlertDialog.Builder(ImportOwnCertificateActivity.this);
        builder.setTitle(getResources().getString(R.string.error));
        builder.setMessage(getResources().getString(R.string.enter_passphrase_wrong) +
                "\n" + getResources().getString(R.string.try_again));

        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.setNegativeButton(R.string.retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                showPassphrasePrompt(pathToFile);
            }
        });
        builder.create().show();
    }

    private void noFileSelected() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ImportOwnCertificateActivity.this);
        builder.setTitle(getResources().getString(R.string.error));
        builder.setMessage(getResources().getString(R.string.import_certificate_no_file));

        builder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.setNegativeButton(R.string.retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                showFileChooser();
            }
        });
        builder.create().show();
    }

    private void importSuccessful() {
        Log.d(SMileCrypto.LOG_TAG, "Exit-Status: " + SMileCrypto.EXIT_STATUS);
        if (SMileCrypto.EXIT_STATUS == SMileCrypto.STATUS_CERTIFICATE_ALREADY_IMPORTED) {
            AlertDialog.Builder builderImported = new AlertDialog.Builder(ImportOwnCertificateActivity.this);
            builderImported.setTitle(getResources().getString(R.string.info));
            builderImported.setMessage(getResources().getString(R.string.certificate_already_imported));

            builderImported.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            builderImported.create().show();
        } else {
            AlertDialog.Builder builderImported = new AlertDialog.Builder(ImportOwnCertificateActivity.this);
            builderImported.setTitle(getResources().getString(R.string.info));
            builderImported.setMessage(getResources().getString(R.string.import_certificate_successful));

            builderImported.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            builderImported.create().show();
        }
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