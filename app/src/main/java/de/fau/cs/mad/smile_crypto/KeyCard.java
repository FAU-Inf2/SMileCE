package de.fau.cs.mad.smile_crypto;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.mail.Address;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

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
    KeyInfo keyInfo;
    Context context;


    public KeyCard(Context context) {
        this(context, R.layout.key);
        this.context = context;
    }

    public KeyCard(Context context, int innerLayout) {
        super(context, innerLayout);
        this.context = context;
    }

    public KeyCard(Context context, KeyInfo keyInfo) {
        this(context, R.layout.key);
        this.keyInfo = keyInfo;
        this.context = context;
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

        if(keyInfo != null) {
            setData(keyInfo);
        }
    }

    private long getContactIDByEmail(String email) {
        Uri uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI, Uri.encode(email));
        String name = "?";
        long contactId =0;

        ContentResolver contentResolver = context.getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[] {ContactsContract.Data.CONTACT_ID, ContactsContract.Data.DISPLAY_NAME }, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {

                contactLookup.moveToNext();
                contactId = contactLookup.getLong(contactLookup.getColumnIndex(ContactsContract.Data.CONTACT_ID));

            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }

        return contactId;
    }

    private void setContactBadge(String email) {
        long contactID = getContactIDByEmail(email);
        Log.e(SMileCrypto.LOG_TAG, "ContactID = " + contactID);
        contact.assignContactFromEmail(email, false);
        Bitmap photo = null;

        try {
            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactID);
            Log.e(SMileCrypto.LOG_TAG, "ContactUri = " + contactUri);
            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), contactUri, false);

            if (inputStream != null) {
                Log.e(SMileCrypto.LOG_TAG, "Image found");
                photo = BitmapFactory.decodeStream(inputStream);
                contact.setImageBitmap(photo);
                inputStream.close();
            } else {
                Log.e(SMileCrypto.LOG_TAG, "Image not found");
                contact.setImageToDefault();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setData(KeyInfo keyInfo) {
        if(keyInfo.mailAddresses.size() > 0) {
            email.setText(keyInfo.mailAddresses.get(0));
            setContactBadge(keyInfo.mailAddresses.get(0));
        } else {
            email.setText(keyInfo.mail);
            setContactBadge(keyInfo.mail);
        }

        if(keyInfo.termination_date != null) {
            DateTime valid = keyInfo.termination_date;
            DateTimeFormatter fmt = DateTimeFormat.forPattern("d MMMM, yyyy");
            String displayDate= valid.toString(fmt);
            valid_until.setText(displayDate);
            DateTime today = new DateTime();
            Years years = Years.yearsBetween(today, valid);
            Months months = Months.monthsBetween(today, valid);

            if(valid.getMillis() <= today.getMillis()) {
                validCircle0.getBackground().setColorFilter(getColorFilter("#ff0000"));
                validCircle1.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
                validCircle2.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
                validCircle3.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
                validCircle4.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
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
        if(keyInfo == null) {
            return false;
        }
        if(o instanceof KeyCard) {
            KeyCard kc = (KeyCard) o;
            if(keyInfo.equals(kc.keyInfo)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return keyInfo != null ? keyInfo.hashCode() : 0;
    }
}
