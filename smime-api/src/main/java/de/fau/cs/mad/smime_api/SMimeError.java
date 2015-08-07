package de.fau.cs.mad.smime_api;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class SMimeError implements Parcelable {

    /**
     * Since there might be a case where new versions of the client using the library getting
     * old versions of the protocol (and thus old versions of this class), we need a versioning
     * system for the parcels sent between the clients and the providers.
     */
    public static final int PARCELABLE_VERSION = 1;

    // possible values for errorId
    public static final int CLIENT_SIDE_ERROR = -1;
    public static final int GENERIC_ERROR = 0;
    public static final int INCOMPATIBLE_API_VERSIONS = 1;
    public static final int NO_OR_WRONG_PASSPHRASE = 2;
    public static final int NO_USER_IDS = 3;

    int errorId;
    String message;

    public SMimeError() {
    }

    public SMimeError(int errorId, String message) {
        this.errorId = errorId;
        this.message = message;
    }

    public SMimeError(SMimeError b) {
        this.errorId = b.errorId;
        this.message = b.message;
    }

    public int getErrorId() {
        return errorId;
    }

    public void setErrorId(int errorId) {
        this.errorId = errorId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        /**
         * NOTE: When adding fields in the process of updating this API, make sure to bump
         * {@link #PARCELABLE_VERSION}.
         */
        dest.writeInt(PARCELABLE_VERSION);
        // Inject a placeholder that will store the parcel size from this point on
        // (not including the size itself).
        int sizePosition = dest.dataPosition();
        dest.writeInt(0);
        int startPosition = dest.dataPosition();
        // version 1
        dest.writeInt(errorId);
        dest.writeString(message);
        // Go back and write the size
        int parcelableSize = dest.dataPosition() - startPosition;
        dest.setDataPosition(sizePosition);
        dest.writeInt(parcelableSize);
        dest.setDataPosition(startPosition + parcelableSize);
    }

    public static final Creator<SMimeError> CREATOR = new Creator<SMimeError>() {
        public SMimeError createFromParcel(final Parcel source) {
            int parcelableVersion = source.readInt();
            int parcelableSize = source.readInt();
            int startPosition = source.dataPosition();

            SMimeError error = new SMimeError();
            error.errorId = source.readInt();
            error.message = source.readString();

            // skip over all fields added in future versions of this parcel
            source.setDataPosition(startPosition + parcelableSize);

            return error;
        }

        public SMimeError[] newArray(final int size) {
            return new SMimeError[size];
        }
    };
}
