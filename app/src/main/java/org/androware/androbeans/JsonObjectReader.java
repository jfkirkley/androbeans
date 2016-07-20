package org.androware.androbeans;

import android.util.JsonReader;
import android.util.JsonToken;

import org.androware.androbeans.utils.FilterLog;
import org.androware.androbeans.utils.ReflectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.R.attr.name;

/**
 * Created by jkirkley on 6/17/16.
 */
public class JsonObjectReader implements ObjectReader {
    JsonReader reader;
    JsonObjectReader parent;

    List<ObjectReadListener> objectReadListeners = new ArrayList<>();

    Class type;
    Object target;

    public static final String TAG = "jsonread";

    public void l(String s) {
        FilterLog.inst().log(TAG, s);
    }

    public JsonObjectReader(JsonReader reader, Class type) throws ObjectReadException {
        this(reader, type, null);
    }

    public JsonObjectReader(JsonReader reader, Class type, JsonObjectReader parent) throws ObjectReadException {
        this.reader = reader;
        this.type = type;
        this.parent = parent;

        if (parent != null) {
            this.setObjectReadListeners(parent.getObjectReadListeners());
        }

        target = invokeListenersOnCreate(type);
        if (target == null) {
            target = ReflectionUtils.newInstance(type);
        } else {
            this.type = target.getClass();
        }
        invokeListenersOnPostCreate();
    }

    public JsonObjectReader(InputStream in, Class type) throws IOException, ObjectReadException{
        this(new JsonReader(new InputStreamReader(in, "UTF-8")), type);
    }

    public void close() throws ObjectReadException {
        try {
            reader.close();
        } catch (IOException ex) {
            throw new ObjectReadException(ex);
        }
    }

    @Override
    public Object read() throws ObjectReadException {
        UUID targetUUID = UUID.randomUUID();

        try {
            reader.beginObject();
            while (reader.hasNext()) {

                String fieldName = reader.nextName();
                try {
                    Field field = type.getField(fieldName);
                    invokeListenersOnFieldName(fieldName, field, targetUUID);
                    field.set(target, readValueAndInvokeListeners(field));
                } catch (NoSuchFieldException e) {
                    invokeListenersOnFieldName(fieldName, null, targetUUID);
                }

            }
            reader.endObject();

        } catch (IOException e) {
            throw new ObjectReadException(e);
        } catch (IllegalAccessException e) {
            // TODO log exceptions
        }

        invokeListenersOnReadDone(target, targetUUID);

        return target;
    }

    @Override
    public String nextFieldName() throws ObjectReadException {
        try {
            return reader.nextName();
        } catch (IOException e) {
            throw new ObjectReadException(e);
        }
    }

    @Override
    public Object nextValue() throws ObjectReadException {
        return readAnyObject(false);
    }

    @Override
    public void addObjectReadListener(ObjectReadListener objectReadListener) {
        objectReadListeners.add(objectReadListener);
    }

    @Override
    public void removeOjectReadListener(ObjectReadListener objectReadListener) {
        objectReadListeners.remove(objectReadListener);
    }

    protected void invokeListenersOnReadDone(Object value, Object id) throws ObjectReadException {
        for (ObjectReadListener objectReadListener : objectReadListeners) {
            objectReadListener.onReadDone(value, id, this);
        }
    }

    protected void invokeListenersOnFieldName(String fieldName, Field field, Object id) throws ObjectReadException {
        invokeListenersOnFieldName(fieldName, field, target, id);
    }

    protected void invokeListenersOnFieldName(String fieldName, Field field, Object theTarget, Object id) throws ObjectReadException {
        for (ObjectReadListener objectReadListener : objectReadListeners) {
            objectReadListener.onFieldName(fieldName, field, theTarget, id, this);
        }
    }

    protected Object invokeListenersOnValue(Object value, Field field) throws ObjectReadException {
        for (ObjectReadListener objectReadListener : objectReadListeners) {
            // values is chained through the listeners - hence order is important
            value = objectReadListener.onValue(value, field, this);
        }
        return value;
    }

    protected Object invokeListenersOnCreate(Class type) throws ObjectReadException {
        Object object = null;
        for (ObjectReadListener objectReadListener : objectReadListeners) {
            // values is chained through the listeners - hence order is important
            object = objectReadListener.onCreate(type, this);
        }
        return object;
    }

    protected void invokeListenersOnPostCreate() throws ObjectReadException {
        for (ObjectReadListener objectReadListener : objectReadListeners) {
            objectReadListener.onPostCreate(target, this);
        }
    }

    public Object readValue(Class fieldType) throws ObjectReadException {
        return readValue(fieldType, null);
    }

    public Object readValue(Field field) throws ObjectReadException {
        return readValue(field.getType(), field);
    }

    @Override
    public Object getTarget() {
        return target;
    }


    public Object readValue(Class fieldType, Field field) throws ObjectReadException {
        try {
            Object value = null;
            if (int.class == fieldType) {
                value = reader.nextInt();
            } else if (long.class == fieldType) {
                value = reader.nextLong();
            } else if (double.class == fieldType) {
                value = reader.nextDouble();
            } else if (float.class == fieldType) {
                value = new Float(reader.nextDouble());
            } else if (boolean.class == fieldType) {
                value = reader.nextBoolean();
            } else if (String.class == fieldType) {
                value = reader.nextString();
            } else if (fieldType.isArray()) {
                value = readArray(fieldType);
            } else if (Map.class.isAssignableFrom(fieldType)) {
                value = readMap(field);
            } else if (List.class.isAssignableFrom(fieldType)) {
                value = readList(field);
            } else {
                value = readObject(fieldType);
            }
            return value;
        } catch (IOException e) {
            throw new ObjectReadException(e);
        }
    }

    public Object readValueAndInvokeListeners(Class fieldType, Field field) throws ObjectReadException {
        return invokeListenersOnValue(readValue(fieldType, field), field);
    }

    public Object readValueAndInvokeListeners(Field field) throws ObjectReadException {
        return readValueAndInvokeListeners(field.getType(), field);
    }

    public Object readArray(Class fieldType) throws ObjectReadException {

        try {
            List list = new ArrayList();
            Class componentType = fieldType == null ? Object.class : fieldType.getComponentType();
            reader.beginArray();
            while (reader.hasNext()) {
                if (componentType == Object.class) {
                    list.add(readAnyObject());
                } else {
                    list.add(readValueAndInvokeListeners(componentType, null));
                }
            }
            reader.endArray();
            return (componentType == Object.class) ? list.toArray() : ReflectionUtils.toTypedArray(list, fieldType);
        } catch (IOException e) {
            throw new ObjectReadException(e);
        }

    }

    public Map readMap(Field field) throws ObjectReadException {

        HashMap map = new HashMap();

        try {
            Class valueType = field != null ? ReflectionUtils.getGenericType(field, 1) : Object.class;
            reader.beginObject();
            while (reader.hasNext()) {
                String key = reader.nextName();

                if (valueType == Object.class) {
                    map.put(key, readAnyObject());
                } else {
                    map.put(key, readValueAndInvokeListeners(valueType, null));
                }
            }
            reader.endObject();

        } catch (IOException e) {
            throw new ObjectReadException(e);
        }

        return map;
    }

    public List readList(Field field) throws ObjectReadException {
        List list = new ArrayList();
        try {
            Class valueType = field != null ? ReflectionUtils.getGenericType(field, 0) : Object.class;
            reader.beginArray();

            while (reader.hasNext()) {

                if (valueType == Object.class) {
                    list.add(readAnyObject());
                } else {
                    list.add(readValueAndInvokeListeners(valueType, null));
                }

            }
            reader.endArray();

        } catch (IOException e) {
            throw new ObjectReadException(e);
        }
        return list;
    }

    public Object readAnyObject() throws ObjectReadException {
        return readAnyObject(true);
    }

    public Object readObject(Class fieldType)  throws ObjectReadException {
        if(fieldType == Object.class) {
            return readAnyObject();
        } else {
            return (new JsonObjectReader(reader, fieldType, this)).read();
        }
    }

    public Object readAnyObject(boolean invokeListeners) throws ObjectReadException {
        try {

            Object value = null;
            JsonToken jsonToken = reader.peek();
            if (jsonToken == JsonToken.BEGIN_ARRAY) {

                List list = new ArrayList<>();

                reader.beginArray();
                while (reader.hasNext()) {
                    list.add(readAnyObject(invokeListeners));
                }
                reader.endArray();

                value = list;

            } else if (jsonToken == JsonToken.BEGIN_OBJECT) {

                Map map = new HashMap();
                l("make map: " + map.toString());
                UUID mapUUID = UUID.randomUUID();
                reader.beginObject();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    l("read name: " + name);

                    if(invokeListeners) invokeListenersOnFieldName(name, null, map, mapUUID);

                    map.put(name, readAnyObject(invokeListeners));
                }
                reader.endObject();
                value = map;

                l("done map: " + map.toString());

                if(invokeListeners) invokeListenersOnReadDone(value, mapUUID);

            } else if (jsonToken == JsonToken.BOOLEAN) {
                value = reader.nextBoolean();
            } else if (jsonToken == JsonToken.NUMBER) {
                value = reader.nextInt();
            } else if (jsonToken == JsonToken.STRING) {
                value = reader.nextString();
            }

            if (invokeListeners) {
                return invokeListenersOnValue(value, null);
            } else {
                return value;
            }

        } catch (IOException e) {
            throw new ObjectReadException(e);
        }
    }

    public Map readRefMap() throws ObjectReadException {
        try {
            HashMap map = new HashMap();

            reader.beginObject();

            String name = reader.nextName();
            String typeClassName = reader.nextString();
            Class beanClass = ReflectionUtils.getClass(typeClassName);


            if (beanClass != null) {
                name = reader.nextName();

                reader.beginObject();
                while (reader.hasNext()) {
                    String key = reader.nextName();
                    map.put(key, readValue(beanClass));
                }
                reader.endObject();

            }
            reader.endObject();

            return map;

        } catch (IOException e) {
            throw new ObjectReadException(e);
        }
    }

    public List<ObjectReadListener> getObjectReadListeners() {
        return objectReadListeners;
    }

    public void setObjectReadListeners(List<ObjectReadListener> objectReadListeners) {
        this.objectReadListeners = objectReadListeners;
    }


}
