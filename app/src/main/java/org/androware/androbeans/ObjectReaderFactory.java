package org.androware.androbeans;

import android.app.Activity;
import android.content.ContextWrapper;

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
    public ContextWrapper contextWrapper;

    public static ObjectReaderFactory getInstance() {
        return getInstance(null);
    }

    public static ObjectReaderFactory getInstance(ContextWrapper contextWrapper) {
        if(ourInstance == null){
            if(contextWrapper == null) {
                throw new IllegalArgumentException("Must intialize ObjectReaderFactory with an contextWrapper reference. ");
            }
            ourInstance = new ObjectReaderFactory(contextWrapper);
        }
        return ourInstance;
    }

    private ObjectReaderFactory(ContextWrapper contextWrapper) {
        this.contextWrapper = contextWrapper;
    }

    public MapObjectReader makeMapReader(Map map, Class type, List<ObjectReadListener> objectReadListeners) throws ObjectReadException {
        MapObjectReader mapObjectReader = new MapObjectReader(map, type);
        if( objectReadListeners != null) {
            mapObjectReader.setObjectReadListeners(objectReadListeners);
        }

        return mapObjectReader;
    }

    public MapObjectReader makeMapReader(Map map, Class type) throws ObjectReadException {
        return makeMapReader(map, type, null);
    }

    public Object makeAndRunMapReader(Map map, Class type, List<ObjectReadListener> objectReadListeners) throws ObjectReadException {

        MapObjectReader mapObjectReader = makeMapReader(map, type, objectReadListeners);
        return mapObjectReader.read();
    }

    public Object makeAndRunMapReader(Map map, Class type) throws ObjectReadException {
        return makeAndRunMapReader(map, type, null);
    }

    public Object makeAndRunInitializingMapReader(Map map, Class type) throws ObjectReadException {
        List<ObjectReadListener> objectReadListeners = new ArrayList<>();
        objectReadListeners.add(new InitializingReadListener());
        return makeAndRunMapReader(map, type, objectReadListeners);
    }

    public JsonObjectReader makeJsonReader(String resourceName, String resourceGroup, Class type, List<ObjectReadListener> objectReadListeners) throws ObjectReadException {
        JsonObjectReader jsonObjectReader = null;
        try {
            jsonObjectReader = new JsonObjectReader(ResourceUtils.getResourceInputStream(contextWrapper, resourceName, resourceGroup), type, null, objectReadListeners);
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
            jsonObjectReader = new JsonObjectReader(inputStream, type, null, objectReadListeners);
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
