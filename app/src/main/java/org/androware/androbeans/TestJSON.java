package org.androware.androbeans;

// TODO remove JSONInstabean debendency
import org.androware.androbeans.legacy.JSONinstaBean;
import org.androware.androbeans.utils.FilterLog;

import android.util.JsonReader;
import android.util.JsonToken;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


/**
 * Created by jkirkley on 2/13/16.
 */
public class TestJSON {
    public final static String TAG = "tstjson";

    public void l(String s) {
        FilterLog.inst().log(TAG, "| " + s);
    }

    Class type;

    Stack<Class> typeStack;

    public TestJSON() {
        super();
    }

    public TestJSON(JsonReader reader, Class type) throws IOException {
        this.type = type;
        Object x = readAnyObject(reader, 0, false, null);
        l(x.toString());
    }


    public static TestJSON buildTest(InputStream is, Class type) {

        try {
            FilterLog.inst().activateTag(TAG);
            JsonReader reader = new JsonReader(new InputStreamReader(is, "UTF-8"));
            return new TestJSON(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String mkIndent(int indent) {
        String s = "";
        for (int i = 0; i < indent; ++i) {
            s += "    ";
        }
        return s;
    }

    public void read(JsonReader reader, int indent, boolean isMap, Class mapType) throws IOException {

        JsonToken jsonToken = reader.peek();
        if (jsonToken == JsonToken.BEGIN_ARRAY) {

            reader.beginArray();
            l(mkIndent(indent) + "[");
            while (reader.hasNext()) {
                if(mapType != null) {
                    Class tempType = type;
                    type = mapType;
                    read(reader, indent + 1, false, null);
                    type = tempType;
                } else {
                    read(reader, indent + 1, false, null);
                }
            }
            l(mkIndent(indent) + "]");
            reader.endArray();

        } else if (isMap && jsonToken == JsonToken.BEGIN_OBJECT) {

            reader.beginObject();
            l(mkIndent(indent) + "{");
            while (reader.hasNext()) {
                String name = reader.nextName();
                l(mkIndent(indent) + name + ":");
                if (JSONinstaBean.class.isAssignableFrom(mapType)) {
                    Class tempType = type;
                    type = mapType;
                    read(reader, indent + 1, false, null);
                    type = tempType;
                } else {
                    read(reader, indent + 1, false, null);
                }
            }
            l(mkIndent(indent) + "}");
            reader.endObject();

        } else if (jsonToken == JsonToken.BEGIN_OBJECT) {

            reader.beginObject();
            l(mkIndent(indent) + "{");
            while (reader.hasNext()) {
                String fieldName = reader.nextName();
                try {

                    Field field = null;
                    field = type.getField(fieldName);
                    l(mkIndent(indent) + fieldName + ":");

                    // TODO remove JSONInstabean debendency
                    if (JSONinstaBean.class.isAssignableFrom(field.getType())) {
                        Class tempType = type;
                        type = field.getType();
                        read(reader, indent + 1, false, null);
                        type = tempType;
                    } else if (Map.class.isAssignableFrom(field.getType())) {

                        ParameterizedType pt = (ParameterizedType) field.getGenericType();
                        Type t[] = pt.getActualTypeArguments();
                        Class fieldType = (Class) t[1];
                        read(reader, indent + 1, true, fieldType);

                    } else if (List.class.isAssignableFrom(field.getType())) {

                        ParameterizedType pt = (ParameterizedType) field.getGenericType();
                        Type t[] = pt.getActualTypeArguments();
                        Class fieldType = (Class) t[0];
                        read(reader, indent + 1, false, fieldType);

                    } else {
                        read(reader, indent + 1, false, null);
                    }

                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
            l(mkIndent(indent) + "}");
            reader.endObject();

        } else if (jsonToken == JsonToken.BOOLEAN) {
            l(mkIndent(indent) + reader.nextBoolean() + "");
        } else if (jsonToken == JsonToken.NUMBER) {
            l(mkIndent(indent) + reader.nextInt() + "");
        } else if (jsonToken == JsonToken.STRING) {
            l(mkIndent(indent) + reader.nextString() + "");
//        } else if( jsonToken == JsonToken.) {
//            l(reader.nextBoolean()+ "");
        }
    }


    public Object readAnyObject(JsonReader reader, int indent, boolean isMap, Class containerItemType) throws IOException {

        JsonToken jsonToken = reader.peek();

        if (jsonToken == JsonToken.BEGIN_ARRAY) {

            List list = new ArrayList<>();

            reader.beginArray();
            l(mkIndent(indent) + "[");
            while (reader.hasNext()) {
                if(containerItemType != null) {
                    Class tempType = type;
                    type = containerItemType;
                    list.add(readAnyObject(reader, indent + 1, false, null));
                    type = tempType;
                } else {
                    list.add(readAnyObject(reader, indent + 1, false, null));
                }
            }
            l(mkIndent(indent) + "]");
            reader.endArray();

            return list;

        } else if (jsonToken == JsonToken.BEGIN_OBJECT) {

            Map map = new HashMap();

            reader.beginObject();
            l(mkIndent(indent) + "{");
            while (reader.hasNext()) {
                String name = reader.nextName();
                l(mkIndent(indent) + name + ":");

                if (containerItemType != null) {
                    Class tempType = type;
                    type = containerItemType;
                    map.put(name, readAnyObject(reader, indent + 1, false, null));
                    type = tempType;
                } else {
                    map.put(name, readAnyObject(reader, indent + 1, false, null));
                }
            }
            l(mkIndent(indent) + "}");
            reader.endObject();

            return map;

        } else if (false && jsonToken == JsonToken.BEGIN_OBJECT) {
            Map map = new HashMap();

            reader.beginObject();
            l(mkIndent(indent) + "{");
            while (reader.hasNext()) {
                String fieldName = reader.nextName();
                try {

                    Field field = null;
                    field = type.getField(fieldName);
                    l(mkIndent(indent) + fieldName + ":");

                    if (Map.class.isAssignableFrom(field.getType())) {

                        ParameterizedType pt = (ParameterizedType) field.getGenericType();
                        Type t[] = pt.getActualTypeArguments();
                        Class fieldType = (Class) t[1];
                        map.put(fieldName, readAnyObject(reader, indent + 1, true, fieldType));

                    } else if (List.class.isAssignableFrom(field.getType())) {

                        ParameterizedType pt = (ParameterizedType) field.getGenericType();
                        Type t[] = pt.getActualTypeArguments();
                        Class fieldType = (Class) t[0];
                        map.put(fieldName, readAnyObject(reader, indent + 1, false, fieldType));

                    } else {

                        Class tempType = type;
                        type = field.getType();
                        map.put(fieldName, readAnyObject(reader, indent + 1, false, null));
                        type = tempType;

                    }

                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
            l(mkIndent(indent) + "}");
            reader.endObject();

            return map;

        } else if (jsonToken == JsonToken.BOOLEAN) {
            return reader.nextBoolean();
        } else if (jsonToken == JsonToken.NUMBER) {
            return  reader.nextInt();
        } else if (jsonToken == JsonToken.STRING) {
            return  reader.nextString();
        }

        return null;
    }


}
