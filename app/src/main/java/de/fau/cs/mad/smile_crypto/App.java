package de.fau.cs.mad.smile_crypto;

import android.app.Application;
import android.content.Context;

import net.danlew.android.joda.JodaTimeAndroid;

public class App extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        JodaTimeAndroid.init(this);
    }

    public static Context getContext(){
        return mContext;
    }
}