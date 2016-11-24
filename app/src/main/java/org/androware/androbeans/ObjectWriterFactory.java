package org.androware.androbeans;

import android.app.Activity;
import android.content.ContextWrapper;

import org.androware.androbeans.utils.Utils;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by jkirkley on 6/24/16.
 */
public class ObjectWriterFactory {
    ContextWrapper contextWrapper;
    private static ObjectWriterFactory ourInstance = null;

    public static ObjectWriterFactory getInstance() {
        return getInstance(null);
    }

    public static ObjectWriterFactory getInstance(ContextWrapper contextWrapper) {
        if(ourInstance == null) {
            if(contextWrapper == null) {
                throw new IllegalArgumentException("Must intialize ObjectReaderFactory with an contextWrapper reference. ");
            }
            ourInstance = new ObjectWriterFactory(contextWrapper);
        }
        return ourInstance;
    }

    private ObjectWriterFactory(ContextWrapper contextWrapper) {
        this.contextWrapper = contextWrapper;
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
            FileOutputStream fos = Utils.getExternalFileOutputStream(contextWrapper, null, "", path);
            writeJsonObject(fos, value);
        } catch( IOException e) {
            throw new ObjectWriteException(e);
        }
    }
}
