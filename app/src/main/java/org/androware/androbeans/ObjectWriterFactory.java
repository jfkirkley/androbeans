package org.androware.androbeans;

import android.app.Activity;

import org.androware.androbeans.utils.Utils;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by jkirkley on 6/24/16.
 */
public class ObjectWriterFactory {
    Activity activity;
    private static ObjectWriterFactory ourInstance = null;

    public static ObjectWriterFactory getInstance() {
        return getInstance(null);
    }

    public static ObjectWriterFactory getInstance(Activity activity) {
        if(ourInstance == null) {
            if(activity == null) {
                throw new IllegalArgumentException("Must intialize ObjectReaderFactory with an activity reference. ");
            }
            ourInstance = new ObjectWriterFactory(activity);
        }
        return ourInstance;
    }

    private ObjectWriterFactory(Activity activity) {
        this.activity = activity;
    }

    public JsonObjectWriter makeJsonObjectWriter(FileOutputStream fos)  throws ObjectWriteException {
        try {
            return new JsonObjectWriter(fos);
        } catch( IOException e) {
            throw new ObjectWriteException(e);
        }
    }

    public void writeJsonObject(FileOutputStream fos, Object value)  throws ObjectWriteException {
        try {
            JsonObjectWriter jsonObjectWriter = makeJsonObjectWriter(fos);
            jsonObjectWriter.write(value);
            jsonObjectWriter.close();

        } catch( IOException e) {
            throw new ObjectWriteException(e);
        }
    }

    public void writeJsonObjectToExternalFile(String path, Object value)  throws ObjectWriteException {
        try {
            FileOutputStream fos = Utils.getExternalFileOutputStream(activity, null, "", path);
            writeJsonObject(fos, value);
        } catch( IOException e) {
            throw new ObjectWriteException(e);
        }
    }
}
