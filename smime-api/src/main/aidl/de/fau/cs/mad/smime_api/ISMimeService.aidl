// ISMimeService.aidl
package de.fau.cs.mad.smime_api;

// Declare any non-default types here with import statements

interface ISMimeService {
    Intent execute(in Intent data, in ParcelFileDescriptor input, in ParcelFileDescriptor output);
}
