package de.fau.cs.mad.smile_crypto;

import android.app.Application;
import android.content.Context;

public class App extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext(){
        return mContext;
    }
}