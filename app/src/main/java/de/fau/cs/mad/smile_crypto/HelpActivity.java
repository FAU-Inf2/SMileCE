package de.fau.cs.mad.smile_crypto;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;


public class HelpActivity extends ActionBarActivity {
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.navigation_drawer_help);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final TextView faq_import_cert = (TextView) findViewById(R.id.buttonlike_faq_import_cert);
        faq_import_cert.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TextView textView = (TextView) findViewById(R.id.text_faq_import_cert);
                if (textView.getVisibility() == View.GONE)
                    textView.setVisibility(View.VISIBLE);
                else
                    textView.setVisibility(View.GONE);
            }
        });

        final TextView faq_passphrase_saved = (TextView) findViewById(R.id.buttonlike_faq_passphrase_saved);
        faq_passphrase_saved.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TextView textView = (TextView) findViewById(R.id.text_faq_passphrase_saved);
                if (textView.getVisibility() == View.GONE)
                    textView.setVisibility(View.VISIBLE);
                else
                    textView.setVisibility(View.GONE);
            }
        });


        final TextView author = (TextView) findViewById(R.id.buttonlike_author);
        author.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TextView textView = (TextView) findViewById(R.id.text_author);
                if (textView.getVisibility() == View.GONE)
                    textView.setVisibility(View.VISIBLE);
                else
                    textView.setVisibility(View.GONE);
            }
        });

        final TextView license  = (TextView) findViewById(R.id.buttonlike_license);
        license.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TextView textView = (TextView) findViewById(R.id.text_license);
                if(textView.getVisibility() == View.GONE)
                    textView.setVisibility(View.VISIBLE);
                else
                    textView.setVisibility(View.GONE);
            }
        });

        final TextView otherProjects = (TextView) findViewById(R.id.buttonlike_other_projects);
        otherProjects.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TextView textView = (TextView) findViewById(R.id.text_projects);
                if(textView.getVisibility() == View.GONE)
                    textView.setVisibility(View.VISIBLE);
                else
                    textView.setVisibility(View.GONE);
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
