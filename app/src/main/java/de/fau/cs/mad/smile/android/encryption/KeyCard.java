package de.fau.cs.mad.smile.android.encryption;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import de.fau.cs.mad.smile_crypto.R;
import it.gmariotti.cardslib.library.internal.Card;

public class KeyCard extends Card {
    TextView email;
    TextView valid_until;
    Button contact;
    String name;
    ImageView validCircle0;
    ImageView validCircle1;
    ImageView validCircle2;
    ImageView validCircle3;
    ImageView validCircle4;
    KeyInfo keyInfo;
    Context context;

    private static List<Integer> materialColors = Arrays.asList(
            0xffe57373,
            0xfff06292,
            0xffba68c8,
            0xff9575cd,
            0xff7986cb,
            0xff64b5f6,
            0xff4fc3f7,
            0xff4dd0e1,
            0xff4db6ac,
            0xff81c784,
            0xffaed581,
            0xffff8a65,
            0xffd4e157,
            0xffffd54f,
            0xffffb74d,
            0xffa1887f,
            0xff90a4ae
    );


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
        contact = (Button) parent.findViewById(R.id.badge);
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

    private Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }


    private Bitmap generateCircleBitmap(int circleColor, float diameterDP, String text){
        /**
         *
         * http://stackoverflow.com/questions/31168636/rounded-quickcontactbadge-with-text
         */
        final int textColor = 0xffffffff;

        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float diameterPixels = diameterDP * (metrics.densityDpi / 160f);
        float radiusPixels = diameterPixels/2;

        // Create the bitmap
        Bitmap output = Bitmap.createBitmap((int) diameterPixels, (int) diameterPixels, Bitmap.Config.ARGB_8888);

        // Create the canvas to draw on
        Canvas canvas = new Canvas(output);
        canvas.drawARGB(0, 0, 0, 0);

        // Draw the circle
        final Paint paintC = new Paint();
        paintC.setAntiAlias(true);
        paintC.setColor(circleColor);
        canvas.drawCircle(radiusPixels, radiusPixels, radiusPixels, paintC);

        // Draw the text
        if (text != null && text.length() > 0) {
            final Paint paintT = new Paint();
            paintT.setColor(textColor);
            paintT.setAntiAlias(true);
            paintT.setTextSize(radiusPixels * 2);
            paintT.setTypeface(Typeface.SANS_SERIF);
            final Rect textBounds = new Rect();
            paintT.getTextBounds(text, 0, text.length(), textBounds);
            canvas.drawText(text, radiusPixels - textBounds.exactCenterX(), radiusPixels - textBounds.exactCenterY(), paintT);
        }

        return output;
    }

    private int getMaterialColor(Object key) {
        return materialColors.get(Math.abs(key.hashCode()) % materialColors.size());
    }

    private void setContactBadge(final String email) {
        Uri uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI, Uri.encode(email));
        String name = "";
        String lookUpKey = "";
        String thumb = "";

        ContentResolver contentResolver = context.getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[]{ContactsContract.Data.LOOKUP_KEY, ContactsContract.Data.DISPLAY_NAME, ContactsContract.Data.PHOTO_THUMBNAIL_URI}, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {

                contactLookup.moveToNext();
                lookUpKey = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.LOOKUP_KEY));
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                thumb = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.PHOTO_THUMBNAIL_URI));

            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }
        Log.d(SMileCrypto.LOG_TAG, "mail: " + email);
        Log.d(SMileCrypto.LOG_TAG, "thumb: " + thumb);
        Log.d(SMileCrypto.LOG_TAG, "name: " + name);
        Log.d(SMileCrypto.LOG_TAG, "key: " + lookUpKey);

        final Uri lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookUpKey);

        if(thumb != null && thumb.length() > 0) {
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), lookupUri, false);
            Bitmap bmp = getCroppedBitmap(BitmapFactory.decodeStream(input));
            BitmapDrawable bdrawable = new BitmapDrawable(context.getResources(), bmp);
            contact.setBackground(bdrawable);
            Log.d(SMileCrypto.LOG_TAG, "Thumbnail found.");
        } else {
            Log.d(SMileCrypto.LOG_TAG, "Thumbnail not found.");
            String initial = (name.length() > 0) ? name.substring(0, 1) : keyInfo.contact.substring(0, 1);
            BitmapDrawable bdrawable = new BitmapDrawable(context.getResources(), generateCircleBitmap(getMaterialColor(initial), 42, initial));
            contact.setBackground(bdrawable);
        }

        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT, Uri.parse("mailto:" + email));
                context.startActivity(intent);
            }
        });
    }

    private void setData(KeyInfo keyInfo) {
        if(keyInfo.mailAddresses.size() > 0) {
            email.setText(keyInfo.mailAddresses.get(0));
            setContactBadge(keyInfo.mailAddresses.get(0));
        } else {
            email.setText(keyInfo.mail);
            setContactBadge(keyInfo.mail);
        }

        if(keyInfo.termination_date != null && keyInfo.valid_after != null) {
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
            } else if (keyInfo.valid_after.getMillis() > today.getMillis()) {
                validCircle0.getBackground().setColorFilter(getColorFilter("#0000ff"));
                validCircle1.getBackground().setColorFilter(getColorFilter("#0000ff"));
                validCircle2.getBackground().setColorFilter(getColorFilter("#0000ff"));
                validCircle3.getBackground().setColorFilter(getColorFilter("#0000ff"));
                validCircle4.getBackground().setColorFilter(getColorFilter("#0000ff"));
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
