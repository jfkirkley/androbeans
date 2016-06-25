package org.androware.androbeans;

import android.app.Activity;

import org.androware.androbeans.utils.ResourceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jkirkley on 6/24/16.
 */
public class ObjectReaderFactory {
    private static ObjectReaderFactory ourInstance = null;
    Activity activity;

    public static ObjectReaderFactory getInstance() {
        return getInstance(null);
    }

    public static ObjectReaderFactory getInstance(Activity activity) {
        if(ourInstance == null){
            ourInstance = new ObjectReaderFactory(activity);
        }
        return ourInstance;
    }

    private ObjectReaderFactory(Activity activity) {
        this.activity = activity;
    }

    public MapObjectReader makeMapReader(Map map, Class type) throws ObjectReadException {
        return new MapObjectReader(map, type);
    }
    public Object makeAndRunMapReader(Map map, Class type) throws ObjectReadException {

        MapObjectReader mapObjectReader = makeMapReader(map, type);
        return mapObjectReader.read();
    }

    public JsonObjectReader makeJsonReader(String resourceName, String resourceGroup, Class type, List<ObjectReadListener> objectReadListeners) throws ObjectReadException {
        JsonObjectReader jsonObjectReader = null;
        try {
            jsonObjectReader = new JsonObjectReader(ResourceUtils.getResourceInputStream(activity, resourceName, resourceGroup), type);
            if(objectReadListeners != null) {
                jsonObjectReader.setObjectReadListeners(objectReadListeners);
            }
            return jsonObjectReader;
        } catch (IOException e) {
            throw new ObjectReadException(e);
        }
    }

    public JsonObjectReader makeJsonReader(String resourceName, Class type, List<ObjectReadListener> objectReadListeners) throws ObjectReadException {
        return makeJsonReader(resourceName, "raw", type, objectReadListeners);
    }

    public Object makeAndRunJsonReader(String resourceName, Class type, List<ObjectReadListener> objectReadListeners) throws ObjectReadException {

        JsonObjectReader JsonObjectReader = makeJsonReader(resourceName, type, objectReadListeners);
        return JsonObjectReader.read();

    }
    public Object makeAndRunLinkedJsonReader(String resourceName, Class type) throws ObjectReadException {
        List<ObjectReadListener> readListeners = new ArrayList<>();
        readListeners.add(new LinkObjectReadListener());
        return makeAndRunJsonReader(resourceName, type, readListeners);
    }

    public JsonObjectReader makeJsonReader(InputStream inputStream, Class type, List<ObjectReadListener> objectReadListeners) throws ObjectReadException {
        JsonObjectReader jsonObjectReader = null;
        try {
            jsonObjectReader = new JsonObjectReader(inputStream, type);
            if(objectReadListeners != null) {
                jsonObjectReader.setObjectReadListeners(objectReadListeners);
            }
            return jsonObjectReader;
        } catch (IOException e) {
            throw new ObjectReadException(e);
        }
    }


    public Object makeAndRunJsonReader(InputStream inputStream, Class type, List<ObjectReadListener> objectReadListeners) throws ObjectReadException {

        JsonObjectReader JsonObjectReader = makeJsonReader(inputStream, type, objectReadListeners);
        return JsonObjectReader.read();

    }
    public Object makeAndRunLinkedJsonReader(InputStream inputStream, Class type) throws ObjectReadException {
        List<ObjectReadListener> readListeners = new ArrayList<>();
        readListeners.add(new LinkObjectReadListener());
        return makeAndRunJsonReader(inputStream, type, readListeners);
    }

}
