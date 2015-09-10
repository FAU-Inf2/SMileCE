package de.fau.cs.mad.smile.android.encryption.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;

import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.x500.style.BCStyle;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import de.fau.cs.mad.smile.android.encryption.App;
import de.fau.cs.mad.smile.android.encryption.R;
import de.fau.cs.mad.smile.android.encryption.SMileCrypto;

/**
 * Collection of usefull static methods.
 */
public class Utils {

    /**
     * All possible identifiers used in X500 certificates.
     */
    public static final ASN1ObjectIdentifier[] asn1ObjectIdentifiers = {
            BCStyle.CN,
            BCStyle.E,
            BCStyle.T,
            BCStyle.INITIALS,
            BCStyle.GENDER,
            BCStyle.GIVENNAME,
            BCStyle.NAME_AT_BIRTH,
            BCStyle.PSEUDONYM,
            BCStyle.SURNAME,
            BCStyle.UID,
            BCStyle.DATE_OF_BIRTH,
            BCStyle.TELEPHONE_NUMBER,
            BCStyle.GENERATION,
            BCStyle.BUSINESS_CATEGORY,
            BCStyle.O,
            BCStyle.OU,
            BCStyle.COUNTRY_OF_CITIZENSHIP,
            BCStyle.COUNTRY_OF_RESIDENCE,
            BCStyle.PLACE_OF_BIRTH,
            BCStyle.C,
            BCStyle.ST,
            BCStyle.L,
            BCStyle.STREET,
            BCStyle.POSTAL_ADDRESS,
            BCStyle.POSTAL_CODE,
            BCStyle.DC,
            BCStyle.DMD_NAME,
            BCStyle.DN_QUALIFIER,
            BCStyle.SN,
            BCStyle.UNIQUE_IDENTIFIER
    };

    /**
     * Generate a color filter with a given color.
     * @param color The color the filter represents.
     * @return A color filter that can be used to set background colors of image views.
     */
    public static ColorFilter getColorFilter(String color) {
        int iColor = Color.parseColor(color);

        int red = (iColor & 0xFF0000) / 0xFFFF;
        int green = (iColor & 0xFF00) / 0xFF;
        int blue = iColor & 0xFF;

        float[] matrix = {0, 0, 0, 0, red
                , 0, 0, 0, 0, green
                , 0, 0, 0, 0, blue
                , 0, 0, 0, 1, 0};

        return new ColorMatrixColorFilter(matrix);
    }

    /**
     * Returns a material color depending on key.
     * @param key Used to get random color.
     * @return An integer representing an RGB color
     */
    public static int getMaterialColor(Object key) {
        Resources res = App.getContext().getResources();
        int[] colours = res.getIntArray(R.array.materialColors);
        return colours[Math.abs(key.hashCode()) % colours.length];
    }

    /**
     * Returns bitmap as circle
     * @param bitmap The bitmap to cut
     * @return A circle shaped bitmap
     */
    public static Bitmap getCroppedBitmap(Bitmap bitmap) {
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

    /**
     * Create round, coloured bitmap with text embedded.
     * @param circleColor The color to use.
     * @param diameterDP The diameter of the circle.
     * @param text The text to embed.
     * @return Bitmap showing a text.
     */
    public static Bitmap generateCircleBitmap(int circleColor, float diameterDP, String text) {
        /**
         *
         * http://stackoverflow.com/questions/31168636/rounded-quickcontactbadge-with-text
         */
        final int textColor = 0xffffffff;

        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float diameterPixels = diameterDP * (metrics.densityDpi / 160f);
        float radiusPixels = diameterPixels / 2;

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

    /**
     * Access multi typed arrays from xml.
     * Code from: http://basememara.com/storing-multidimensional-resource-arrays-in-android/
     * @param context The context used to access the resources.
     * @param key The key of the array
     * @return A list with the collected data.
     */
    public static List<TypedArray> getMultiTypedArray(Context context, String key) {
        List<TypedArray> array = new ArrayList<>();

        try {
            Class<R.array> res = R.array.class;
            Field field;
            int counter = 0;

            do {
                field = res.getField(key + "_" + counter);
                array.add(context.getResources().obtainTypedArray(field.getInt(null)));
                counter++;
            } while (field != null);
        } catch (Exception e) {
            if(SMileCrypto.isDEBUG()) {
                Log.e(SMileCrypto.LOG_TAG, "Error accessing data: " + e.getMessage());
            }
        } finally {
            return array;
        }
    }
}
