package de.fau.cs.mad.smile_crypto;

import android.app.Application;

public class SMileCrypto extends Application {

    public static interface ApplicationAware {
        void initializeComponent(Application application);
    }

    public static Application app = null;
    public static final String LOG_TAG = "SMile-crypto";

    /**
     * If this is enabled there will be additional logging information sent to
     * Log.d, including protocol dumps.
     * TODO: Controlled by Preferences at run-time
     */
    public static boolean DEBUG = true;

}
