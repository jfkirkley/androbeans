package org.androware.androbeans;

import android.util.JsonWriter;


import org.androware.androbeans.utils.FilterLog;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import static android.R.attr.value;

/**
 * Created by jkirkley on 6/17/16.
 */
public class JsonObjectWriter implements ObjectWriter {
    JsonWriter writer;
    public final static String TAG = "jsonwrite";

    public void l(String s) {
        FilterLog.inst().log(TAG, s);
    }

    public void l(String tag, String s) {
        FilterLog.inst().log(tag, s);
    }

    public JsonObjectWriter(OutputStream out) throws IOException {
        writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
    }

    public void close() throws IOException {
        writer.close();
    }

    @Override
    public void write(Object object) throws IOException {
        Class type = object.getClass();

        Field fields[] = type.getFields();
        try {

            writer.beginObject();
            for (Field f : fields) {

                if (Modifier.isFinal(f.getModifiers())) {
                    continue;
                }

                String fieldName = f.getName();
                Class fieldType = f.getType();

                Object value = f.get(object);
                if(value != null) {
                    writer.name(fieldName);
                    writeValue(fieldType, value);
                } else {
                    l("field: " + fieldName + " is null.");
                }
            }
            writer.endObject();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public void writeArray(Object array) throws IOException {
        writer.beginArray();

        int length = Array.getLength(array);

        for (int i = 0; i < length; i++) {
            Object arrayElement = Array.get(array, i);
            Class componentType = arrayElement.getClass();

            writeValue(componentType, arrayElement);
        }
        writer.endArray();
    }

    public void writeValue(Class fieldType, Object value) throws IOException {
        if (int.class == fieldType || Integer.class == fieldType) {
            writer.value((int) value);
        } else if (long.class == fieldType || Long.class == fieldType) {
            writer.value((long) value);
        } else if (double.class == fieldType || Double.class == fieldType) {
            writer.value((double) value);
        } else if (float.class == fieldType || Float.class == fieldType) {
            writer.value((float) value);
        } else if (boolean.class == fieldType || Boolean.class == fieldType) {
            writer.value((boolean) value);
        } else if (String.class == fieldType) {
            writer.value((String) value);
        } else if (fieldType.isArray()) {
            writeArray(value);
        } else if (Map.class.isAssignableFrom(fieldType)) {
            writeMap((Map)value);
        } else if (List.class.isAssignableFrom(fieldType)) {
            writeList((List)value);
        } else {
            write(value);
        }
    }

    public void writeMap(Map map) throws IOException {

        try {

            if (map == null) {
                writer.beginObject();
                writer.endObject();
                return;
            }


            writer.beginObject();
            for (Object k : map.keySet()) {
                String key = (String) k;
                writer.name(key);

                Object value = map.get(key);
                Class fieldType = value.getClass();

                writeValue(fieldType, value);
            }
            writer.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void writeList(List list) {

        try {

            if (list == null) {
                // write empty array
                writer.beginArray();
                writer.endArray();
                return;
            }


            writer.beginArray();
            for (Object value : list) {
                Class fieldType = value.getClass();

                writeValue(fieldType, value);

            }
            writer.endArray();

        } catch (IOException e) {
        }

    }

}
