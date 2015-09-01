package de.fau.cs.mad.smile.android.encryption.ui.activity;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import de.fau.cs.mad.smile.android.encryption.R;
import de.fau.cs.mad.smile.android.encryption.SMileCrypto;

public class CertificateCreationActivity extends ActionBarActivity {
    private EditText name;
    private EditText email;
    private EditText passphrase;
    private EditText passphraseRepeat;
    private TextView date;
    private TextView wrongDate;
    private TextView wrongName;
    private TextView wrongEmail;
    private TextView wrongPassphrase;
    private ImageView clearName;
    private ImageView clearEmail;
    private ImageView clearPassphrase;
    private ImageView getClearPassphraseRepeat;
    private boolean nameOk;
    private boolean emailOk;
    private boolean passphraseOk;
    private boolean dateOk;
    private DateTime valid;
    DateTimeFormatter format = DateTimeFormat.forPattern("dd. MMMM yyyy");

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

        date = (TextView) findViewById(R.id.valid);
        wrongDate = (TextView) findViewById(R.id.valid_wrong);
        wrongName = (TextView) findViewById(R.id.name_wrong);
        wrongEmail = (TextView) findViewById(R.id.email_wrong);
        wrongPassphrase = (TextView) findViewById(R.id.passphrase_wrong);

        clearName = (ImageView) findViewById(R.id.clear_name);
        clearEmail = (ImageView) findViewById(R.id.clear_email);
        clearPassphrase = (ImageView) findViewById(R.id.clear_passphrase);
        getClearPassphraseRepeat = (ImageView) findViewById(R.id.clear_passphrase_repeat);

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
        getClearPassphraseRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passphraseRepeat.setText("");
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

}
