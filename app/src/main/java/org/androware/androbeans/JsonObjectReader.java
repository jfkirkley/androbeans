package org.androware.androbeans;

import android.util.JsonReader;
import android.util.JsonToken;

import org.androware.androbeans.utils.ReflectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jkirkley on 6/17/16.
 */
public class JsonObjectReader implements ObjectReader {
    JsonReader reader;
    JsonObjectReader parent;

    List<ObjectReadListener> objectReadListeners = new ArrayList<>();

    Class type;
    Object target;

    public JsonObjectReader(JsonReader reader, Class type) throws IOException {
        this(reader, type, null);
    }

    public JsonObjectReader(JsonReader reader, Class type, JsonObjectReader parent) throws IOException {
        this.reader = reader;
        this.type = type;
        this.parent = parent;

        if(parent != null) {
            this.setObjectReadListeners(parent.getObjectReadListeners());
        }

        target = invokeListenersOnCreate(type);
        if(target == null) {
            target = ReflectionUtils.newInstance(type);
        } else {
            this.type = target.getClass();
        }
        invokeListenersOnPostCreate();
    }

    public JsonObjectReader(InputStream in, Class type) throws IOException {
        this(new JsonReader(new InputStreamReader(in, "UTF-8")), type);
    }

    public void close() throws IOException {
        reader.close();
    }

    @Override
    public Object read() throws IOException {

        try {

            reader.beginObject();
            while (reader.hasNext()) {

                String fieldName = reader.nextName();
                try {
                    Field field = type.getField(fieldName);
                    invokeListenersOnFieldName(fieldName, field);
                    field.set(target, readValueAndInvokeListeners(field));
                } catch (NoSuchFieldException e) {
                    invokeListenersOnFieldName(fieldName, null);
                }

            }
            reader.endObject();

        } catch (IOException e) {
            // TODO log exceptions
        } catch (IllegalAccessException e) {

        }

        invokeListenersOnReadDone(target);

        return target;
    }

    @Override
    public String nextFieldName() throws IOException {
        return reader.nextName();
    }

    @Override
    public Object nextValue() throws IOException {
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

    protected void invokeListenersOnReadDone(Object value) throws IOException {
        for(ObjectReadListener objectReadListener: objectReadListeners) {
            objectReadListener.onReadDone(value, this);
        }
    }

    protected void invokeListenersOnFieldName(String fieldName, Field field) throws IOException {
        for(ObjectReadListener objectReadListener: objectReadListeners) {
            objectReadListener.onFieldName(fieldName, field, this);
        }
    }

    protected Object invokeListenersOnValue(Object value, Field field) throws IOException {
        for(ObjectReadListener objectReadListener: objectReadListeners) {
            // values is chained through the listeners - hence order is important
            value = objectReadListener.onValue(value, field, this);
        }
        return value;
    }

    protected Object invokeListenersOnCreate(Class type) throws IOException {
        Object object = null;
        for(ObjectReadListener objectReadListener: objectReadListeners) {
            // values is chained through the listeners - hence order is important
             object = objectReadListener.onCreate(type, this);
        }
        return object;
    }

    protected void invokeListenersOnPostCreate() throws IOException {
        for(ObjectReadListener objectReadListener: objectReadListeners) {
            objectReadListener.onPostCreate(target, this);
        }
    }

    public Object readValue(Class fieldType) throws IOException {
        return readValue(fieldType, null);
    }

    public Object readValue(Field field) throws IOException {
        return readValue(field.getType(), field);
    }

    @Override
    public Object getTarget() {
        return target;
    }


    public Object readValue(Class fieldType, Field field) throws IOException {

        Object value = null;
        if (int.class == fieldType) {
            value = reader.nextInt();
        } else if (long.class == fieldType) {
            value = reader.nextLong();
        } else if (double.class == fieldType) {
            value = reader.nextDouble();
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
            value = (new JsonObjectReader(reader, fieldType, this)).read();
        }
        return value;
    }

    public Object readValueAndInvokeListeners(Class fieldType, Field field) throws IOException {
        return invokeListenersOnValue(readValue(fieldType, field), field);
    }

    public Object readValueAndInvokeListeners(Field field) throws IOException {
        return readValueAndInvokeListeners(field.getType(), field);
    }

    public Object readArray(Class fieldType) throws IOException {
        List list = new ArrayList();
        Class componentType = fieldType == null? Object.class: fieldType.getComponentType();
        reader.beginArray();
        while (reader.hasNext()) {
            if(componentType == Object.class) {
                list.add(readAnyObject());
            } else {
                list.add(readValueAndInvokeListeners(componentType,null));
            }
        }
        reader.endArray();
        return (componentType == Object.class)? list.toArray(): ReflectionUtils.toTypedArray(list, fieldType);
    }

    public Map readMap(Field field) throws IOException {
        HashMap map = new HashMap();

        try {
            Class valueType = field != null? ReflectionUtils.getGenericType(field, 1): Object.class;
            reader.beginObject();
            while (reader.hasNext()) {
                String key = reader.nextName();

                if(valueType == Object.class) {
                    map.put(key, readAnyObject());
                } else {
                    map.put(key, readValueAndInvokeListeners(valueType, null));
                }
            }
            reader.endObject();

        } catch (IOException e) {
        }

        return map;
    }

    public List readList(Field field) {
        List list = new ArrayList();
        try {
            Class valueType = field != null? ReflectionUtils.getGenericType(field, 0): Object.class;
            reader.beginArray();

            while (reader.hasNext()) {

                if(valueType == Object.class) {
                    list.add(readAnyObject());
                } else {
                    list.add(readValueAndInvokeListeners(valueType, null));
                }

            }
            reader.endArray();

        } catch (IOException e) {
        }
        return list;
    }

    public Object readAnyObject() throws IOException {
        return readAnyObject(true);
    }

    public Object readAnyObject(boolean invokeListeners) throws IOException {
        Object value = null;
        JsonToken jsonToken = reader.peek();
        if (jsonToken == JsonToken.BEGIN_ARRAY) {

            List list = new ArrayList<>();

            reader.beginArray();
            while (reader.hasNext()) {
                list.add(readAnyObject());
            }
            reader.endArray();

            value = list;

        } else if (jsonToken == JsonToken.BEGIN_OBJECT) {

            Map map = new HashMap();

            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();

                map.put(name, readAnyObject());
            }
            reader.endObject();

            value = map;
        } else if (jsonToken == JsonToken.BOOLEAN) {
            value = reader.nextBoolean();
        } else if (jsonToken == JsonToken.NUMBER) {
            value = reader.nextInt();
        } else if (jsonToken == JsonToken.STRING) {
            value = reader.nextString();
        }

        if(invokeListeners) {
            return invokeListenersOnValue(value, null);
        } else {
            return value;
        }
    }

    public Map readRefMap() throws IOException {
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

    }

    public List<ObjectReadListener> getObjectReadListeners() {
        return objectReadListeners;
    }

    public void setObjectReadListeners(List<ObjectReadListener> objectReadListeners) {
        this.objectReadListeners = objectReadListeners;
    }


}
