package de.fau.cs.mad.smile.android.encryption.ui.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import de.fau.cs.mad.smile.android.encryption.KeyInfo;
import de.fau.cs.mad.smile.android.encryption.PathConverter;
import de.fau.cs.mad.smile.android.encryption.R;
import de.fau.cs.mad.smile.android.encryption.SMileCrypto;
import de.fau.cs.mad.smile.android.encryption.crypto.DecryptMail;
import de.fau.cs.mad.smile.android.encryption.crypto.PasswordEncryption;
import korex.mail.internet.MimeMessage;


public class DecryptLocalMailActivity extends ActionBarActivity {
    private Toolbar toolbar;
    private TextView mTextView;
    protected final int DLMA_FILE_CHOOSER_REQUEST_CODE = 0;

    private String mimeBodyPartsString;
    private String textplain;
    private String texthtml;
    private MimeMessage decryptedMimeMessage;

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

        if(SMileCrypto.isDEBUG()) {
            Log.d(SMileCrypto.LOG_TAG, "Started DecryptLocalMailActivity.");
        }
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
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }
        if (id == R.id.action_settings) {
            Intent i = new Intent(DecryptLocalMailActivity.this, SettingsActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.action_save) {
            String pathToFile = saveMimeMessage();
            if (pathToFile == null) {
                Toast.makeText(this, R.string.no_mime_message_saved, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getString(R.string.saved_mime_message) + pathToFile, Toast.LENGTH_LONG).show();
            }
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
                    if(SMileCrypto.isDEBUG()) {
                        Log.d(SMileCrypto.LOG_TAG, "Path to selected file: " + path);
                    }

                    if (path.endsWith(".eml")) {
                        if(SMileCrypto.isDEBUG()) {
                            Log.d(SMileCrypto.LOG_TAG, " " + path);
                        }
                        mTextView.setText(getString(R.string.decrypt_file_show_path) + path);

                        SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                        e.putString("last-encrypted-file-path", path);
                        e.apply();
                        passphraseDecryptOrPrompt(path);
                    } else {
                        mTextView.setText(getString(R.string.not_eml));
                        AlertDialog.Builder builder = new AlertDialog.Builder(DecryptLocalMailActivity.this);
                        builder.setTitle(getResources().getString(R.string.error));
                        builder.setMessage(getResources().getString(R.string.not_eml));
                        builder.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                showFileChooser();
                            }
                        });
                        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                        builder.create().show();
                    }
                } else {
                    mTextView.setText(getString(R.string.import_certificate_no_file));
                    AlertDialog.Builder builder = new AlertDialog.Builder(DecryptLocalMailActivity.this);
                    builder.setTitle(getResources().getString(R.string.error));
                    builder.setMessage(getResources().getString(R.string.import_certificate_no_file));
                    builder.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            showFileChooser();
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
                    builder.create().show();
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
            if(SMileCrypto.isDEBUG()) {
                Log.d(SMileCrypto.LOG_TAG, "Call file manager to choose decrypted mail.");
            }
            startActivityForResult(Intent.createChooser(intent,
                            getResources().getString(R.string.import_decrypted_file)),
                    DLMA_FILE_CHOOSER_REQUEST_CODE);

        } catch (android.content.ActivityNotFoundException anfe) {
            if(SMileCrypto.isDEBUG()) {
                Log.e(SMileCrypto.LOG_TAG, "No file manager installed. " + anfe.getMessage());
            }
            Toast.makeText(this,
                    getResources().getString(R.string.no_file_manager),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void passphraseDecryptOrPrompt(String pathToFile) {
        DecryptMail decryptMail = getDecryptMail();
        if (decryptMail == null) {
            showErrorPrompt();
            return;
        }

        MimeMessage mimeMessage = decryptMail.getMimeMessageFromFile(pathToFile);

        if (mimeMessage == null) {
            showErrorPrompt();
            return;
        }

        ArrayList<KeyInfo> keyInfos = decryptMail.getKeyInfosByMimeMessage(mimeMessage);
        if (keyInfos == null || keyInfos.size() == 0) {
            showErrorPrompt();
        } else if (keyInfos.size() == 1) {
            String alias = keyInfos.get(0).getAlias();
            passphraseDecryptOrPromptAlias(alias, pathToFile);
        } else {
            //case more than one certificate --> show prompt to select correct certificate
            selectCertificate(pathToFile, keyInfos);
        }
    }


    public void passphraseDecryptOrPromptAlias(String alias, String pathToFile) {
        if (alias == null) {
            showErrorPrompt();
            return;
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (!preferences.contains(alias + "-passphrase")) {
            showPassphrasePrompt(pathToFile, alias);
            return;
        }

        String encryptedPassphrase = preferences.getString(alias + "-passphrase", null);
        if(SMileCrypto.isDEBUG()) {
            Log.d(SMileCrypto.LOG_TAG, "Passphrase: <sensitive>");
        }

        String passphrase;
        try {
            if(SMileCrypto.isDEBUG()) {
                Log.d(SMileCrypto.LOG_TAG, "Decrypt passphrase for alias: " + alias);
            }
            passphrase = PasswordEncryption.decryptString(encryptedPassphrase);

            if (passphrase == null) {
                if(SMileCrypto.isDEBUG()) {
                    Log.d(SMileCrypto.LOG_TAG, "Decrypted passphrase was null.");
                }
                showPassphrasePrompt(pathToFile, alias);
                return;
            }

            if(SMileCrypto.isDEBUG()) {
                Log.d(SMileCrypto.LOG_TAG, "Got decrypted passphrase.");
            }
        } catch (Exception e) {
            showPassphrasePrompt(pathToFile, alias);
            return;
        }

        Boolean success = decryptFile(pathToFile, alias, passphrase);
        if (!success) {
            showErrorPrompt();
        }

        options();
    }

    public void selectCertificate(final String pathToFile, final ArrayList<KeyInfo> keyInfos) {
        if(SMileCrypto.isDEBUG()) {
            Log.d(SMileCrypto.LOG_TAG, "More than one certificate found for this mail address, show dialog to select certificate.");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.title_select_certificate));
        //R.string.multiple_certificates?

        int size = keyInfos.size();
        ListView modeList = new ListView(this);
        String[] stringArray = new String[size];
        for (int i = 0; i < size; i++) {
            KeyInfo keyInfo = keyInfos.get(i);
            DateTime valid = keyInfo.getTerminationDate();
            DateTimeFormatter fmt = DateTimeFormat.forPattern("MMMM dd, yyyy");
            String displayDate = valid.toString(fmt);
            String info = "Name:\t\t\t\t\t" + keyInfo.getContact() +
                    "\nValid until:\t\t" + displayDate +
                    "\nThumbprint: " + keyInfo.getThumbprint();
            stringArray[i] = info;
        }
        if(SMileCrypto.isDEBUG()) {
            Log.d(SMileCrypto.LOG_TAG, size + " certificates to decide.");
        }
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2,
                android.R.id.text1, stringArray);
        modeList.setAdapter(modeAdapter);
        builder.setView(modeList);
        final Dialog dialog = builder.create();
        modeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(SMileCrypto.isDEBUG()) {
                    Log.d(SMileCrypto.LOG_TAG, "Selected certificate at position: " + position);
                }
                String alias = keyInfos.get(position).getAlias();
                if(SMileCrypto.isDEBUG()) {
                    Log.d(SMileCrypto.LOG_TAG, "Selected alias is: " + alias);
                }
                passphraseDecryptOrPromptAlias(alias, pathToFile);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Nullable
    private DecryptMail getDecryptMail() {
        DecryptMail decryptMail = null;

        try {
            decryptMail = new DecryptMail();
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | NoSuchProviderException e) {
            e.printStackTrace();
        } finally {
            if (decryptMail == null) {
                SMileCrypto.EXIT_STATUS = SMileCrypto.STATUS_UNKNOWN_ERROR;
                return null;
            }
        }

        return decryptMail;
    }

    public void showPassphrasePrompt(final String pathToFile, final String alias) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View passphrasePromptView = layoutInflater.inflate(R.layout.passphrase_prompt, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(passphrasePromptView);

        final EditText passphraseUserInput = (EditText) passphrasePromptView.
                findViewById(R.id.passphraseUserInput);
        passphraseUserInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passphraseUserInput.setTransformationMethod(new PasswordTransformationMethod());

        alertDialogBuilder.setCancelable(false).setPositiveButton(getResources().getString(R.string.go),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (!decryptFile(pathToFile, alias, passphraseUserInput.getText().toString())) {

                            if (!(SMileCrypto.EXIT_STATUS == SMileCrypto.STATUS_WRONG_PASSPHRASE)) {
                                showErrorPrompt();
                                return;
                            }
                            if (SMileCrypto.isDEBUG()) {
                                Log.d(SMileCrypto.LOG_TAG, "Maybe wrong passphrase. Show passphrase prompt again.");
                            }

                            AlertDialog.Builder builder = new AlertDialog.Builder(DecryptLocalMailActivity.this);
                            builder.setTitle(getResources().getString(R.string.error));
                            builder.setMessage(getResources().getString(R.string.something_went_wrong) +
                                    "\n" + getResources().getString(R.string.ask_try_again));

                            builder.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    showPassphrasePrompt(pathToFile, alias);
                                }
                            });
                            builder.setNegativeButton(R.string.cancel, null);
                            builder.create().show();
                        }
                        options();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }
        );
        alertDialogBuilder.create().show();
    }

    public void showErrorPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DecryptLocalMailActivity.this);
        builder.setTitle(getResources().getString(R.string.error));
        if (SMileCrypto.EXIT_STATUS == SMileCrypto.STATUS_NO_VALID_MIMEMESSAGE_IN_FILE) {
            builder.setMessage(getResources().getString(R.string.no_valid_mime_message));
        } else if (SMileCrypto.EXIT_STATUS == SMileCrypto.STATUS_NO_VALID_MIMEMESSAGE) {
            builder.setMessage(getResources().getString(R.string.no_valid_decrypted_mime_message));
        } else if (SMileCrypto.EXIT_STATUS == SMileCrypto.STATUS_NO_RECIPIENTS_FOUND) {
            builder.setMessage(getResources().getString(R.string.no_recipients));
        } else if (SMileCrypto.EXIT_STATUS == SMileCrypto.STATUS_NO_CERTIFICATE_FOUND) {
            builder.setMessage(getResources().getString(R.string.no_certificate_for_recipients));
        } else if (SMileCrypto.EXIT_STATUS == SMileCrypto.STATUS_DECRYPTION_FAILED) {
            builder.setMessage(getResources().getString(R.string.decryption_failed));
        } else {
            if(SMileCrypto.isDEBUG()) {
                Log.e(SMileCrypto.LOG_TAG, "EXIT_STATUS: " + SMileCrypto.EXIT_STATUS);
            }
            builder.setMessage(getResources().getString(R.string.internal_error));
        }
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }

    private Boolean decryptFile(String pathToFile, String alias, String passphrase) {
        DecryptMail decryptMail = getDecryptMail();
        if (decryptMail == null) {
            return false;
        }

        decryptedMimeMessage = decryptMail.decryptEncodeMail(pathToFile, alias, passphrase);
        if (decryptedMimeMessage == null) {
            return false;
        }

        mimeBodyPartsString = decryptMail.convertMimeMessageToString(decryptedMimeMessage);
        if (mimeBodyPartsString == null) {
            return false;
        }

        textplain = decryptMail.getTextPlainFromMimeMessage(decryptedMimeMessage);
        if (textplain == null) {
            textplain = getString(R.string.containsNoSuchPart);
        }

        texthtml = decryptMail.getTextHtmlFromMimeMessage(decryptedMimeMessage);
        if (texthtml == null) {
            texthtml = getString(R.string.containsNoSuchPart);
        }

        if(SMileCrypto.isDEBUG()) {
            Log.d(SMileCrypto.LOG_TAG, "Decrypted text: " + mimeBodyPartsString);
        }
        return true;
    }

    private void options() {
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Spinner spinner = (Spinner) findViewById(R.id.spinner_nav);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinnerTitleListDecryptMail,
                R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = mimeBodyPartsString;
                switch (position) {
                    case 0: {
                        text = textplain;
                        break;
                    }
                    case 1: {
                        text = texthtml;
                        break;
                    }
                }

                mTextView.setText(text);
                mTextView.scrollTo(0, 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if(SMileCrypto.isDEBUG()) {
                    Log.d(SMileCrypto.LOG_TAG, "nothing selected");
                }
            }

        });
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private String saveMimeMessage() {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if (preferences.contains("last-encrypted-file-path")) {
                String path = preferences.getString("last-encrypted-file-path", null);
                if(SMileCrypto.isDEBUG()) {
                    Log.d(SMileCrypto.LOG_TAG, "Path: " + path);
                }
                path = path.substring(0, path.length() - 4);
                path += "_decrypted.eml";
                if(SMileCrypto.isDEBUG()) {
                    Log.d(SMileCrypto.LOG_TAG, "New path to save MimeMessage: " + path);
                }

                if (!isExternalStorageWritable()) {
                    if(SMileCrypto.isDEBUG()) {
                        Log.e(SMileCrypto.LOG_TAG, "External storage is not writable!");
                    }
                    return null;
                }

                File newFile = new File(path);
                FileOutputStream out = new FileOutputStream(newFile);
                out.write(mimeBodyPartsString.getBytes(), 0, mimeBodyPartsString.length());
                out.flush();
                out.close();
                return path;
            }
        } catch (Exception e) {
            if(SMileCrypto.isDEBUG()) {
                Log.e(SMileCrypto.LOG_TAG, "Error saving file: " + e.getMessage());
            }
        }
        return null;
    }
}
