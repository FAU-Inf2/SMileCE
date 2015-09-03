package de.fau.cs.mad.smile.android.encryption.ui.activity;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import de.fau.cs.mad.smile.android.encryption.App;
import de.fau.cs.mad.smile.android.encryption.R;
import de.fau.cs.mad.smile.android.encryption.SMileCrypto;
import de.fau.cs.mad.smile.android.encryption.crypto.SelfSignedCertificateCreator;


/**
 * User interface to create self signed v3 X500 Certificates.
 */
public class CertificateCreationActivity extends ActionBarActivity {
    private EditText name;
    private EditText email;
    private EditText passphrase;
    private EditText passphraseRepeat;
    private EditText expert;
    private TextView nameLabel;
    private TextView emailLabel;
    private TextView expertLabel;
    private TextView date;
    private TextView wrongDate;
    private TextView wrongName;
    private TextView wrongEmail;
    private TextView wrongPassphrase;
    private ImageView clearName;
    private ImageView clearEmail;
    private ImageView clearPassphrase;
    private ImageView clearPassphraseRepeat;
    private ImageView clearExpert;
    private ImageButton create;
    private boolean nameOk;
    private boolean emailOk;
    private boolean passphraseOk;
    private boolean dateOk;
    private boolean expertMode;
    private DateTime valid;
    DateTimeFormatter format = DateTimeFormat.forPattern("dd. MMMM yyyy");


    /**
     * Check if the user chooses a date in the past. If date is correct, notify activity, that a new value is set.
     */
    private DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            DateTime tmp = DateTime.now();
            DateTime now = DateTime.now();
            ++monthOfYear; // DatePicker starts with 0
            Log.d(SMileCrypto.LOG_TAG, "Today: " + now.getDayOfMonth() + "." + now.getMonthOfYear() + "." + now.getYear());
            Log.d(SMileCrypto.LOG_TAG, "Got date: " + dayOfMonth + "." + monthOfYear + "." + year);
            valid = tmp.withDate(year, monthOfYear, dayOfMonth);
            date.setText(format.print(valid));
            if(valid.getMillis() <= now.getMillis()) {
                Log.d(SMileCrypto.LOG_TAG, "Date invalid");
                dateOk = false;
                date.setBackgroundColor(Color.RED);
                wrongDate.setVisibility(View.VISIBLE);
            } else {
                Log.d(SMileCrypto.LOG_TAG, "Date valid");
                wrongDate.setVisibility(View.GONE);
                dateOk = true;
                date.setBackgroundColor(Color.GREEN);
            }
            booleanSet();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate_creation);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.navigation_drawer_info);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.create_certificate);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        } else {
            Log.d(SMileCrypto.LOG_TAG, "Failed to set home as up in CertificateCreationActivity");
        }

        name = (EditText) findViewById(R.id.name);
        email = (EditText) findViewById(R.id.email);
        passphrase = (EditText) findViewById(R.id.passphrase);
        passphraseRepeat = (EditText) findViewById(R.id.passphrase_repeat);
        expert = (EditText) findViewById(R.id.expert);

        nameLabel = (TextView) findViewById(R.id.name_label);
        emailLabel = (TextView) findViewById(R.id.email_label);
        expertLabel = (TextView) findViewById(R.id.expert_label);
        date = (TextView) findViewById(R.id.valid);
        wrongDate = (TextView) findViewById(R.id.valid_wrong);
        wrongName = (TextView) findViewById(R.id.name_wrong);
        wrongEmail = (TextView) findViewById(R.id.email_wrong);
        wrongPassphrase = (TextView) findViewById(R.id.passphrase_wrong);

        clearName = (ImageView) findViewById(R.id.clear_name);
        clearEmail = (ImageView) findViewById(R.id.clear_email);
        clearPassphrase = (ImageView) findViewById(R.id.clear_passphrase);
        clearPassphraseRepeat = (ImageView) findViewById(R.id.clear_passphrase_repeat);
        clearExpert = (ImageView) findViewById(R.id.clear_expert);

        create = (ImageButton) findViewById(R.id.create);

        nameOk = false;
        emailOk = false;
        passphraseOk = false;
        dateOk = false;

        // Clear listener
        clearName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name.setText("");
            }
        });
        clearEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email.setText("");
            }
        });
        clearPassphrase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passphrase.setText("");
            }
        });
        clearPassphraseRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passphraseRepeat.setText("");
            }
        });
        clearExpert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expert.setText("");
            }
        });

        // Date listener
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTime now = DateTime.now();
                // DatePicker starts months with 0
                new DatePickerDialog(v.getContext(), onDateSetListener, now.getYear(), now.getMonthOfYear() - 1, now.getDayOfMonth()).show();
            }
        });

        // TextWatcher
        name.addTextChangedListener(new NameWatcher());
        email.addTextChangedListener(new EmailWatcher());
        PassphraseWatcher pw = new PassphraseWatcher();
        passphrase.addTextChangedListener(pw);
        passphraseRepeat.addTextChangedListener(pw);

        // OnClickListener
        create.setOnClickListener(new AsyncClickListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_certificate_ceration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Items in Toolbar/ActionBar
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_expert) {
            toggleExpert();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * If all fields are set, make create button accessible.
     */
    private void booleanSet() {
        if(!expertMode) {
            if (nameOk && emailOk && dateOk && passphraseOk) {
                create.setVisibility(View.VISIBLE);
            } else {
                create.setVisibility(View.GONE);
            }
        } else {
            if(dateOk && passphraseOk) {
                create.setVisibility(View.VISIBLE);
            } else {
                create.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Check if the user choose a valid name.
     */
    private class NameWatcher implements TextWatcher {
        private void error() {
            wrongName.setVisibility(View.VISIBLE);
            name.setBackgroundColor(Color.RED);
            nameOk = false;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Do nothing
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Do nothing
        }

        @Override
        public void afterTextChanged(Editable s) {
            int status = SelfSignedCertificateCreator.validateName(s.toString());
            switch (status) {
                case SMileCrypto.STATUS_NAME_OK :
                    nameOk = true;
                    name.setBackgroundColor(Color.GREEN);
                    wrongName.setVisibility(View.GONE);
                    break;
                case  SMileCrypto.STATUS_NAME_EMPTY:
                    wrongName.setText(getString(R.string.empty_name));
                    error();
                    break;
                case SMileCrypto.STATUS_NO_NAME:
                    wrongName.setText(getString(R.string.no_name));
                    error();
                    break;
                case SMileCrypto.STATUS_NAME_INVALID_CHARACTER:
                    wrongName.setText(getString(R.string.invalid_name));
                    error();
                    break;
            }
            booleanSet();
        }
    }

    /**
     * Check if the email address is formatted correctly.
     */
    private class EmailWatcher implements TextWatcher {
        private void error() {
            wrongEmail.setVisibility(View.VISIBLE);
            email.setBackgroundColor(Color.RED);
            emailOk = false;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Do nothing
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Do nothing
        }

        @Override
        public void afterTextChanged(Editable s) {
            String emailAddress = s.toString();
            int status = SelfSignedCertificateCreator.validateEmail(emailAddress);
            switch (status) {
                case SMileCrypto.STATUS_EMAIL_OK :
                    emailOk = true;
                    email.setBackgroundColor(Color.GREEN);
                    wrongEmail.setVisibility(View.GONE);
                    break;
                case  SMileCrypto.STATUS_EMAIL_EMPTY:
                    wrongEmail.setText(getString(R.string.empty_email));
                    error();
                    break;
                case SMileCrypto.STATUS_NO_EMAIL:
                    wrongEmail.setText(getString(R.string.no_email));
                    error();
                    break;
                case SMileCrypto.STATUS_EMAIL_INVALID_CHARACTER:
                    wrongEmail.setText(getString(R.string.invalid_email_character));
                    error();
                    break;
                case SMileCrypto.STATUS_EMAIL_INVALID:
                    wrongEmail.setText(getString(R.string.invalid_email));
                    error();
                    break;
            }
            booleanSet();
        }
    }

    /**
     * Check if passphrase and its repetition match.
     */
    private class PassphraseWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            Editable t1 = passphrase.getText();
            Editable t2 = passphraseRepeat.getText();

            String passphraseText = t1.toString();
            String passphraseRepeatText = t2.toString();
            if(passphraseText.length() > 0 && passphraseRepeatText.length() > 0) {
                if(passphraseText.length() == passphraseRepeatText.length() && passphraseText.equals(passphraseRepeatText)) {
                    passphraseOk = true;
                    passphrase.setBackgroundColor(Color.GREEN);
                    passphraseRepeat.setBackgroundColor(Color.GREEN);
                    wrongPassphrase.setVisibility(View.GONE);
                } else {
                    passphraseOk = false;
                    passphrase.setBackgroundColor(Color.RED);
                    passphraseRepeat.setBackgroundColor(Color.RED);
                    wrongPassphrase.setText(getString(R.string.passphrases_dont_match));
                    wrongPassphrase.setVisibility(View.VISIBLE);
                }
            } else {
                passphraseOk = false;
                passphrase.setBackgroundColor(Color.RED);
                passphraseRepeat.setBackgroundColor(Color.RED);
                wrongPassphrase.setText(getString(R.string.passphrases_empty));
                wrongPassphrase.setVisibility(View.VISIBLE);
            }
            booleanSet();
        }
    }

    /**
     * Create certificate in background task.
     */
    private class AsyncClickListener implements View.OnClickListener {

        private class CreateCertTask extends AsyncTask<Object, Object, Object> {

            private String name;
            private String email;
            private DateTime valid;
            private String passphrase;
            private String expert;
            private int status;

            public CreateCertTask(String name, String email, DateTime valid, String passphrase) {
                this.name = name;
                this.email = email;
                this.valid = valid;
                this.passphrase = passphrase;
            }

            public CreateCertTask(String expert, DateTime valid, String passphrase) {
                this.expert = expert;
                this.valid = valid;
                this.passphrase = passphrase;
            }

            @Override
            protected Object doInBackground(Object[] params) {
                if(expert != null) {
                    status = SelfSignedCertificateCreator.createCert(null, null, expert, valid, passphrase);
                } else {
                    status = SelfSignedCertificateCreator.createCert(name, email, null, valid, passphrase);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                // Output result.
                switch (status) {
                    case SMileCrypto.STATUS_SAVED_CERT:
                        Toast.makeText(App.getContext(), getString(R.string.creation_success), Toast.LENGTH_SHORT).show();
                        break;
                    case SMileCrypto.STATUS_EXPERT_WRONG_STRING:
                        Toast.makeText(App.getContext(), getString(R.string.wrong_rdn_format), Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(App.getContext(), getString(R.string.creation_failed), Toast.LENGTH_SHORT).show();
                        break;
                }
                super.onPostExecute(o);
            }
        }

        @Override
        public void onClick(View v) {
            v.setVisibility(View.GONE);
            if(!expertMode) {
                String myName = name.getText().toString();
                String myEmail = email.getText().toString();
                String pass = passphrase.getText().toString();
                CreateCertTask cct = new CreateCertTask(myName, myEmail, valid, pass);
                cct.execute();
            } else {
                String myExpert = expert.getText().toString();
                String pass = passphrase.getText().toString();
                CreateCertTask cct = new CreateCertTask(myExpert, valid, pass);
                cct.execute();
            }
        }
    }

    /**
     * Toggle expert mode.
     */
    private void toggleExpert() {
        if(expertMode) {
            expertMode = false;
            expert.setVisibility(View.GONE);
            clearExpert.setVisibility(View.GONE);
            expertLabel.setVisibility(View.GONE);
            nameLabel.setVisibility(View.VISIBLE);
            emailLabel.setVisibility(View.VISIBLE);
            email.setVisibility(View.VISIBLE);
            name.setVisibility(View.VISIBLE);
            clearEmail.setVisibility(View.VISIBLE);
            clearName.setVisibility(View.VISIBLE);
            wrongEmail.setVisibility(View.VISIBLE);
            wrongName.setVisibility(View.VISIBLE);
            booleanSet();
        } else {
            expertMode = true;
            expert.setVisibility(View.VISIBLE);
            clearExpert.setVisibility(View.VISIBLE);
            expertLabel.setVisibility(View.VISIBLE);
            nameLabel.setVisibility(View.GONE);
            emailLabel.setVisibility(View.GONE);
            email.setVisibility(View.GONE);
            name.setVisibility(View.GONE);
            clearEmail.setVisibility(View.GONE);
            clearName.setVisibility(View.GONE);
            wrongEmail.setVisibility(View.GONE);
            wrongName.setVisibility(View.GONE);
            booleanSet();
        }
    }

}
