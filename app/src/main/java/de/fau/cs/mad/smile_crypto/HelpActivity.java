package de.fau.cs.mad.smile_crypto;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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


        final TextView faq_meaning_colour_q = (TextView) findViewById(R.id.buttonlike_faq_meaning_colour_q);
        faq_meaning_colour_q.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TextView textView = (TextView) findViewById(R.id.text_faq_meaning_colour);
                RelativeLayout rL0 = (RelativeLayout) findViewById(R.id.valid_00);
                RelativeLayout rL1 = (RelativeLayout) findViewById(R.id.valid_01);
                RelativeLayout rL2 = (RelativeLayout) findViewById(R.id.valid_02);
                RelativeLayout rL3 = (RelativeLayout) findViewById(R.id.valid_03);
                RelativeLayout rL4 = (RelativeLayout) findViewById(R.id.valid_04);
                RelativeLayout rL5 = (RelativeLayout) findViewById(R.id.valid_05);
                RelativeLayout rL6 = (RelativeLayout) findViewById(R.id.valid_06);

                if (textView.getVisibility() == View.GONE) {
                    textView.setVisibility(View.VISIBLE);
                    rL0.setVisibility(View.VISIBLE);
                    rL1.setVisibility(View.VISIBLE);
                    rL2.setVisibility(View.VISIBLE);
                    rL3.setVisibility(View.VISIBLE);
                    rL4.setVisibility(View.VISIBLE);
                    rL5.setVisibility(View.VISIBLE);
                    rL6.setVisibility(View.VISIBLE);
                }
                else {
                    textView.setVisibility(View.GONE);
                    rL0.setVisibility(View.GONE);
                    rL1.setVisibility(View.GONE);
                    rL2.setVisibility(View.GONE);
                    rL3.setVisibility(View.GONE);
                    rL4.setVisibility(View.GONE);
                    rL5.setVisibility(View.GONE);
                    rL6.setVisibility(View.GONE);
                }
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

        setColours();
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

    private void setColours() {
        // TODO: not implemented
        RelativeLayout rL0 = (RelativeLayout) findViewById(R.id.valid_00);
        ImageView validCircle0 = (ImageView) rL0.findViewById(R.id.valid_circle_0);
        ImageView validCircle1 = (ImageView) rL0.findViewById(R.id.valid_circle_1);
        ImageView validCircle2 = (ImageView) rL0.findViewById(R.id.valid_circle_2);
        ImageView validCircle3 = (ImageView) rL0.findViewById(R.id.valid_circle_3);
        ImageView validCircle4 = (ImageView) rL0.findViewById(R.id.valid_circle_4);
        TextView text = (TextView) rL0.findViewById(R.id.valid_text);
        validCircle0.getBackground().setColorFilter(getColorFilter("blue"));
        validCircle1.getBackground().setColorFilter(getColorFilter("blue"));
        validCircle2.getBackground().setColorFilter(getColorFilter("blue"));
        validCircle3.getBackground().setColorFilter(getColorFilter("blue"));
        validCircle4.getBackground().setColorFilter(getColorFilter("blue"));
        text.setText(R.string.faq_colour_00);

        //} else if(years.getYears() > 1 || (years.getYears() == 1 && months.getMonths() > 0)) {
        RelativeLayout rL1 = (RelativeLayout) findViewById(R.id.valid_01);
        validCircle0 = (ImageView) rL1.findViewById(R.id.valid_circle_0);
        validCircle1 = (ImageView) rL1.findViewById(R.id.valid_circle_1);
        validCircle2 = (ImageView) rL1.findViewById(R.id.valid_circle_2);
        validCircle3 = (ImageView) rL1.findViewById(R.id.valid_circle_3);
        validCircle4 = (ImageView) rL1.findViewById(R.id.valid_circle_4);
        text = (TextView) rL1.findViewById(R.id.valid_text);
        validCircle0.getBackground().setColorFilter(getColorFilter("#00ff00"));
        validCircle1.getBackground().setColorFilter(getColorFilter("#00ff00"));
        validCircle2.getBackground().setColorFilter(getColorFilter("#00ff00"));
        validCircle3.getBackground().setColorFilter(getColorFilter("#00ff00"));
        validCircle4.getBackground().setColorFilter(getColorFilter("#00ff00"));
        text.setText(R.string.faq_colour_01);

        //} else if(months.getMonths() > 5) {
        RelativeLayout rL2 = (RelativeLayout) findViewById(R.id.valid_02);
        validCircle0 = (ImageView) rL2.findViewById(R.id.valid_circle_0);
        validCircle1 = (ImageView) rL2.findViewById(R.id.valid_circle_1);
        validCircle2 = (ImageView) rL2.findViewById(R.id.valid_circle_2);
        validCircle3 = (ImageView) rL2.findViewById(R.id.valid_circle_3);
        validCircle4 = (ImageView) rL2.findViewById(R.id.valid_circle_4);
        text = (TextView) rL2.findViewById(R.id.valid_text);
        validCircle0.getBackground().setColorFilter(getColorFilter("#00ff00"));
        validCircle1.getBackground().setColorFilter(getColorFilter("#00ff00"));
        validCircle2.getBackground().setColorFilter(getColorFilter("#00ff00"));
        validCircle3.getBackground().setColorFilter(getColorFilter("#00ff00"));
        validCircle4.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
        text.setText(R.string.faq_colour_02);

        //else if (months.getMonths() > 2) {
        RelativeLayout rL3 = (RelativeLayout) findViewById(R.id.valid_03);
        validCircle0 = (ImageView) rL3.findViewById(R.id.valid_circle_0);
        validCircle1 = (ImageView) rL3.findViewById(R.id.valid_circle_1);
        validCircle2 = (ImageView) rL3.findViewById(R.id.valid_circle_2);
        validCircle3 = (ImageView) rL3.findViewById(R.id.valid_circle_3);
        validCircle4 = (ImageView) rL3.findViewById(R.id.valid_circle_4);
        text = (TextView) rL3.findViewById(R.id.valid_text);
        validCircle0.getBackground().setColorFilter(getColorFilter("#FFA500"));
        validCircle1.getBackground().setColorFilter(getColorFilter("#FFA500"));
        validCircle2.getBackground().setColorFilter(getColorFilter("#FFA500"));
        validCircle3.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
        validCircle4.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
        text.setText(R.string.faq_colour_03);

        //else if(months.getMonths() > 0) {
        RelativeLayout rL4 = (RelativeLayout) findViewById(R.id.valid_04);
        validCircle0 = (ImageView) rL4.findViewById(R.id.valid_circle_0);
        validCircle1 = (ImageView) rL4.findViewById(R.id.valid_circle_1);
        validCircle2 = (ImageView) rL4.findViewById(R.id.valid_circle_2);
        validCircle3 = (ImageView) rL4.findViewById(R.id.valid_circle_3);
        validCircle4 = (ImageView) rL4.findViewById(R.id.valid_circle_4);
        text = (TextView) rL4.findViewById(R.id.valid_text);
        validCircle0.getBackground().setColorFilter(getColorFilter("#FFA500"));
        validCircle1.getBackground().setColorFilter(getColorFilter("#FFA500"));
        validCircle2.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
        validCircle3.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
        validCircle4.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
        text.setText(R.string.faq_colour_04);

        //else if(valid.getMillis() > today.getMillis()) {
        RelativeLayout rL5 = (RelativeLayout) findViewById(R.id.valid_05);
        validCircle0 = (ImageView) rL5.findViewById(R.id.valid_circle_0);
        validCircle1 = (ImageView) rL5.findViewById(R.id.valid_circle_1);
        validCircle2 = (ImageView) rL5.findViewById(R.id.valid_circle_2);
        validCircle3 = (ImageView) rL5.findViewById(R.id.valid_circle_3);
        validCircle4 = (ImageView) rL5.findViewById(R.id.valid_circle_4);
        text = (TextView) rL5.findViewById(R.id.valid_text);
        validCircle0.getBackground().setColorFilter(getColorFilter("#FFA500"));
        validCircle1.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
        validCircle2.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
        validCircle3.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
        validCircle4.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
        text.setText(R.string.faq_colour_05);

        // if(valid.getMillis() <= today.getMillis())
        RelativeLayout rL6 = (RelativeLayout) findViewById(R.id.valid_06);
        validCircle0 = (ImageView) rL6.findViewById(R.id.valid_circle_0);
        validCircle1 = (ImageView) rL6.findViewById(R.id.valid_circle_1);
        validCircle2 = (ImageView) rL6.findViewById(R.id.valid_circle_2);
        validCircle3 = (ImageView) rL6.findViewById(R.id.valid_circle_3);
        validCircle4 = (ImageView) rL6.findViewById(R.id.valid_circle_4);
        text = (TextView) rL6.findViewById(R.id.valid_text);
        validCircle0.getBackground().setColorFilter(getColorFilter("red"));
        validCircle1.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
        validCircle2.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
        validCircle3.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
        validCircle4.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
                text.setText(R.string.faq_colour_06);

    }

    private ColorFilter getColorFilter(String color) {
        int iColor = Color.parseColor(color);

        int red = (iColor & 0xFF0000) / 0xFFFF;
        int green = (iColor & 0xFF00) / 0xFF;
        int blue = iColor & 0xFF;

        float[] matrix = { 0, 0, 0, 0, red
                , 0, 0, 0, 0, green
                , 0, 0, 0, 0, blue
                , 0, 0, 0, 1, 0 };

        return new ColorMatrixColorFilter(matrix);
    }
}
