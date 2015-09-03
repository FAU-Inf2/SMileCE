package de.fau.cs.mad.smile.android.encryption.remote;

import android.os.AsyncTask;
import android.os.Environment;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import de.fau.cs.mad.smile.android.encryption.App;

public abstract class ContentLoaderTask<T> extends AsyncTask<Void, Void, T> {
    private final InputStream inputStream;

    protected ContentLoaderTask(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    protected InputStream getInputStream() {
        return inputStream;
    }

    protected File copyToFile(InputStream inputStream) throws IOException {
        final File externalStorage = Environment.getExternalStorageDirectory();
        final String targetDirName = FilenameUtils.concat(externalStorage.getAbsolutePath(), App.getContext().getPackageName());
        final File targetDir = new File(targetDirName);

        File targetFile = null;
        int fileNumber = 0;

        do {
            targetFile = new File(targetDir, String.format("source-%05d.eml", fileNumber++));
        } while (targetFile.exists());

        FileUtils.copyInputStreamToFile(inputStream, targetFile);
        return targetFile;
    }
}
