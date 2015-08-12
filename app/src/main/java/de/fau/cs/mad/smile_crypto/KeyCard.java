package de.fau.cs.mad.smile_crypto;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.Years;


import it.gmariotti.cardslib.library.internal.Card;

public class KeyCard extends Card {
    TextView email;
    TextView valid_until;
    QuickContactBadge contact;
    String name;
    ImageView validCircle0;
    ImageView validCircle1;
    ImageView validCircle2;
    ImageView validCircle3;
    ImageView validCircle4;
    KeyInfo ki;

    public KeyCard(Context context) {
        this(context, R.layout.key);
    }

    public KeyCard(Context context, int innerLayout) {
        super(context, innerLayout);
    }

    public KeyCard(Context context, KeyInfo keyInfo) {
        this(context, R.layout.key);
        ki = keyInfo;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        email = (TextView) parent.findViewById(R.id.email);
        valid_until = (TextView) parent.findViewById(R.id.valid_until);
        contact = (QuickContactBadge) parent.findViewById(R.id.badge);
        name = "";
        validCircle0 = (ImageView) parent.findViewById(R.id.valid_circle_0);
        validCircle1 = (ImageView) parent.findViewById(R.id.valid_circle_1);
        validCircle2 = (ImageView) parent.findViewById(R.id.valid_circle_2);
        validCircle3 = (ImageView) parent.findViewById(R.id.valid_circle_3);
        validCircle4 = (ImageView) parent.findViewById(R.id.valid_circle_4);
        if(ki != null)
            setData(ki);
    }

    private void setData(KeyInfo ki) {
        if(ki.mail != null)
            email.setText(ki.mail);
        if(ki.termination_date != null) {
            DateTime valid = ki.termination_date;
            valid_until.setText(valid.toString());
            DateTime today = new DateTime();
            Years years = Years.yearsBetween(today, valid);
            Months months = Months.monthsBetween(today, valid);
            if(valid.getMillis() <= today.getMillis()) {
                validCircle0.getBackground().setColorFilter(getColorFilter("red"));
                validCircle1.getBackground().setColorFilter(getColorFilter("white"));
                validCircle2.getBackground().setColorFilter(getColorFilter("white"));
                validCircle3.getBackground().setColorFilter(getColorFilter("white"));
                validCircle4.getBackground().setColorFilter(getColorFilter("white"));
            } else if(years.getYears() > 1 || (years.getYears() == 1 && months.getMonths() > 0)) {
                validCircle0.getBackground().setColorFilter(getColorFilter("#00ff00"));
                validCircle1.getBackground().setColorFilter(getColorFilter("#00ff00"));
                validCircle2.getBackground().setColorFilter(getColorFilter("#00ff00"));
                validCircle3.getBackground().setColorFilter(getColorFilter("#00ff00"));
                validCircle4.getBackground().setColorFilter(getColorFilter("#00ff00"));
            } else if(months.getMonths() > 5) {
                validCircle0.getBackground().setColorFilter(getColorFilter("#00ff00"));
                validCircle1.getBackground().setColorFilter(getColorFilter("#00ff00"));
                validCircle2.getBackground().setColorFilter(getColorFilter("#00ff00"));
                validCircle3.getBackground().setColorFilter(getColorFilter("#00ff00"));
                validCircle4.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
            } else if (months.getMonths() > 2) {
                validCircle0.getBackground().setColorFilter(getColorFilter("#FFA500"));
                validCircle1.getBackground().setColorFilter(getColorFilter("#FFA500"));
                validCircle2.getBackground().setColorFilter(getColorFilter("#FFA500"));
                validCircle3.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
                validCircle4.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
            } else if(months.getMonths() > 0) {
                validCircle0.getBackground().setColorFilter(getColorFilter("#FFA500"));
                validCircle1.getBackground().setColorFilter(getColorFilter("#FFA500"));
                validCircle2.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
                validCircle3.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
                validCircle4.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
            } else if(valid.getMillis() > today.getMillis()) {
                validCircle0.getBackground().setColorFilter(getColorFilter("#FFA500"));
                validCircle1.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
                validCircle2.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
                validCircle3.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
                validCircle4.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
            }
        }
        if(ki.contact != null)
            setTitle(ki.contact);
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

    @Override
    public boolean equals(Object o) {
        Log.e(SMileCrypto.LOG_TAG, "KeyCard equals called");
        if(ki == null) {
            Log.e(SMileCrypto.LOG_TAG, "KeyCard != o");
            return false;
        }
        if(o instanceof KeyCard) {
            KeyCard kc = (KeyCard) o;
            if(ki.equals(kc.ki)) {
                Log.e(SMileCrypto.LOG_TAG, "KeyCard == o");
                return true;
            }
        }
        Log.e(SMileCrypto.LOG_TAG, "KeyCard != o");
        return false;
    }

    @Override
    public int hashCode() {
        return ki != null ? ki.hashCode() : 0;
    }
}
