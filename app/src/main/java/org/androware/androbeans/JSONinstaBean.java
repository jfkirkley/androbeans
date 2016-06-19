package org.androware.androbeans;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.androware.androbeans.utils.ConstructorSpec;
import org.androware.androbeans.utils.ReflectionUtils;

/**
 * Created by jkirkley on 2/13/16.
 */
public class JSONinstaBean extends InstaBean {
    public final static String TAG = "jsoninsta";

    public void l(String s) {
        FilterLog.inst().log(TAG, s);
    }

    public void l(String tag, String s) {
        FilterLog.inst().log(tag, s);
    }

    public JSONinstaBean() {
        super();
    }

    public JSONinstaBean(Map map, Class type, InstaBean parent) {
        super(map, type, parent);
    }

    public JSONinstaBean(JsonReader reader, Class type) throws IOException {
        this(reader, type, null);
    }

    public JSONinstaBean(JsonReader reader, Class type, InstaBean parent) throws IOException {
        super(type, parent);
        read(reader, type);
    }


    public static JSONinstaBean buildJsonInstaBean(InputStream is, Class topClass) {

        try {
            JsonReader reader = new JsonReader(new InputStreamReader(is, "UTF-8"));
            // this is a root object, so parent is always null
            return makeJSONinstaBean(reader, topClass, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    public void registerRefMap(JsonReader reader, String refmapKey) throws IOException {

        reader.beginObject();

        String name = reader.nextName();
        String typeClassName = reader.nextString();
        Class beanClass = ReflectionUtils.getClass(typeClassName);

        if (beanClass != null) {
            name = reader.nextName();

            HashMap map = new HashMap();

            reader.beginObject();
            while (reader.hasNext()) {
                String key = reader.nextName();
                if (JSONinstaBean.class.isAssignableFrom(beanClass)) {
                    map.put(key, makeJSONinstaBean(reader, beanClass, this));
                }
            }
            reader.endObject();

            setRefMap(refmapKey, map);
        }
        reader.endObject();

    }


    public void read(JsonReader reader, Class type) throws IOException {
        l(reader.peek() + "");
        BeanRef mergeBeanRef = null;
        reader.beginObject();
        while (reader.hasNext()) {
            String fieldName = reader.nextName();
            Class fieldType = this.getType(fieldName);

            if (fieldType == null) {

                if (fieldName.startsWith(REFMAP_PREFIX)) {
                    registerRefMap(reader, fieldName.substring(REFMAP_PREFIX.length()));
                } else if (fieldName.equals(BEAN_ID)) {
                    String id = reader.nextString();
                    setBeanById(id, this);
                } else if (fieldName.equals(MERGE_PREFIX)) {
                    mergeBeanRef = new BeanRef(reader.nextString());
                }
                l("NULL fieldName: " + fieldName);
                continue;
            }
            l("fieldName: " + fieldName);
            try {
                if (List.class.isAssignableFrom(fieldType)) {

                    this.setObject(fieldName, getArrayAsList(reader, fieldType, type.getField(fieldName)));

                } else if (Map.class.isAssignableFrom(fieldType)) {

                    this.setObject(fieldName, getMap(reader, fieldType, fieldName));

                } else if (fieldType.isArray()) {

                    this.setObject(fieldName, ReflectionUtils.toTypedArray(getArrayAsList(reader, fieldType, null), fieldType));

                } else if (JSONinstaBean.class.isAssignableFrom(fieldType)) {

                    this.setObject(fieldName, makeJSONinstaBean(reader, fieldType, this));

                } else if (int.class.isAssignableFrom(fieldType)) {
                    this.setInt(fieldName, reader.nextInt());
                } else if (long.class.isAssignableFrom(fieldType)) {
                    this.setLong(fieldName, reader.nextLong());
                } else if (double.class.isAssignableFrom(fieldType)) {
                    this.setDouble(fieldName, reader.nextDouble());
                } else if (boolean.class.isAssignableFrom(fieldType)) {
                    this.setBoolean(fieldName, reader.nextBoolean());
                } else if (String.class.isAssignableFrom(fieldType)) {
                    this.setObject(fieldName, reader.nextString());
                } else {
                    this.setObject(fieldName, readAnyObject(reader, 0, null));
                }

            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        reader.endObject();

        if(mergeBeanRef != null) {
            merge(mergeBeanRef.getBean());
        }
        //Log.d("lesson", "st: " + this.toString());
    }


    public Map getMap(JsonReader reader, Class mapClass, String fieldName) throws Exception {
        Class fieldType = getContainerGenericClass(mapClass, type.getField(fieldName), true);

        HashMap map = new HashMap();

        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            if (List.class.isAssignableFrom(fieldType)) {
                map.put(key, getArrayAsList(reader, fieldType, mapClass.getField(fieldName)));
            } else if (Map.class.isAssignableFrom(fieldType)) {
                map.put(key, getMap(reader, fieldType, fieldName));
            } else if (fieldType.isArray()) {
                // TODO this is fucking ugly,   maybe just cannot factor because of generic erasure
                map.put(key, ReflectionUtils.toTypedArray(getArrayAsList(reader, fieldType, null), fieldType));
            } else if (JSONinstaBean.class.isAssignableFrom(fieldType)) {
                map.put(key, makeJSONinstaBean(reader, fieldType, this));
            } else if (int.class.isAssignableFrom(fieldType)) {
                int i = reader.nextInt();
                 map.put(key, i);
            } else if (long.class.isAssignableFrom(fieldType)) {
                map.put(key, reader.nextLong());
            } else if (double.class.isAssignableFrom(fieldType)) {
                map.put(key, reader.nextDouble());
            } else if (boolean.class.isAssignableFrom(fieldType)) {
                map.put(key, reader.nextBoolean());
            } else if (String.class.isAssignableFrom(fieldType)) {
                map.put(key, reader.nextString());
            } else {
                map.put(key, readAnyObject(reader, 0, null));
            }
        }
        reader.endObject();

        return map;
    }

    public Class getContainerGenericClass(Class containerClass, Field containerField, boolean isMap) {
        Class ctype = containerClass.getComponentType();
        if (ctype != null) {
            // container is an array
            return ctype;
        }

        Class overrideTypeField = checkRegistry(this.getClass(), containerField.getName());
        if (overrideTypeField != null) {
            return overrideTypeField;
        }
        return ReflectionUtils.getGenericType(containerField, isMap? 1: 0);
    }

/*
    public  <T> T[] getArray(JsonReader reader, Class arrayClass, String fieldName) throws IOException, NoSuchFieldException {
        return ReflectionUtils.toTypedArray(getArrayAsList(reader, arrayClass, null), this.type.getField(fieldName));
    }
*/
    public List getArrayAsList(JsonReader reader, Class listClass, Field listField) throws IOException {

        List l = new ArrayList();
        Class ctype = getContainerGenericClass(listClass, listField, false);

        reader.beginArray();
        while (reader.hasNext()) {

            if (JSONinstaBean.class.isAssignableFrom(ctype)) {
                l.add(makeJSONinstaBean(reader, ctype, this));

            } else if (int.class.isAssignableFrom(ctype)) {

                l.add(reader.nextInt());

            } else if (long.class.isAssignableFrom(ctype)) {
                l.add(reader.nextLong());
            } else if (double.class.isAssignableFrom(ctype)) {
                l.add(reader.nextDouble());
            } else if (boolean.class.isAssignableFrom(ctype)) {
                l.add(reader.nextBoolean());
            } else if (String.class.isAssignableFrom(ctype)) {
                l.add(reader.nextString());
            } else {
                l.add(readAnyObject(reader, 0, null));
            }
        }
        reader.endArray();

        return l;
    }

    public String mkIndent(int indent) {
        String s = "";
        for (int i = 0; i < indent; ++i) {
            s += "    ";
        }
        return s;
    }

    public Object readAnyObject(JsonReader reader, int indent, Class containerItemType) throws IOException {

        JsonToken jsonToken = reader.peek();
        if (jsonToken == JsonToken.BEGIN_ARRAY) {

            List list = new ArrayList<>();

            reader.beginArray();
            l(mkIndent(indent) + "[");
            while (reader.hasNext()) {
                if (containerItemType != null) {
                    Class tempType = type;
                    type = containerItemType;
                    list.add(readAnyObject(reader, indent + 1, null));
                    type = tempType;
                } else {
                    list.add(readAnyObject(reader, indent + 1, null));
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
                    map.put(name, readAnyObject(reader, indent + 1, null));
                    type = tempType;
                } else {
                    map.put(name, readAnyObject(reader, indent + 1, null));
                }
            }
            l(mkIndent(indent) + "}");
            reader.endObject();

            return map;
        } else if (jsonToken == JsonToken.BOOLEAN) {
            return reader.nextBoolean();
        } else if (jsonToken == JsonToken.NUMBER) {
            return reader.nextInt();
        } else if (jsonToken == JsonToken.STRING) {
            String val = reader.nextString();
            if(val.startsWith(IDREF_PREFIX)) {
                // special case: this is a ref to a bean
                return handleBeanRef(val);
            }
            return val;
        }

        return null;
    }


    public class BeanRef {
        String prefix;
        String key;

        // TODO need path spec like xpath to ref any arbitrary bean in tree
        public static final String PARENT_KEY = "__p__";
        public static final String ROOT_KEY = "__root__";

        public BeanRef(String ref) {
            // prefix:key
            String tokens[] = ref.split(":");
            this.prefix = tokens[0];
            this.key = tokens[1];
        }

        public JSONinstaBean getBean() {

            if(prefix.equals(IDREF_PREFIX)) {

                return (JSONinstaBean) getBeanById(key);

            } else if(hasRefMap(prefix)) {
                // prefix is the refmap key
                Map m = getRefMap(prefix);
                if (m != null) {
                    return (JSONinstaBean) m.get(key);
                }
            }
            return null;
        }
    }

    public JSONinstaBean handleBeanRef(String refSpec) {
        return new BeanRef(refSpec).getBean();
    }

    public static JSONinstaBean makeJSONinstaBean(JsonReader reader, Class beanType, JSONinstaBean parent) {

        try {

            JsonToken jsonToken = reader.peek();

            if (jsonToken == JsonToken.BEGIN_OBJECT) {

                Class [] t = {JsonReader.class, Class.class, InstaBean.class};
                ConstructorSpec constructorSpec = new ConstructorSpec(beanType, t, reader, beanType, parent);

                return (JSONinstaBean) constructorSpec.build();

            } else if (jsonToken == JsonToken.STRING) {

                if(parent != null) {
                    return parent.handleBeanRef(reader.nextString());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public void writeJsonStream(OutputStream out) throws IOException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
        writer.setIndent("    ");
        write(writer);
        writer.close();
    }

    public void write(JsonWriter writer) {

        Field fields[] = type.getFields();
        try {

            writer.beginObject();
            for (Field f : fields) {

                if (Modifier.isFinal(f.getModifiers())) {
                    continue;
                }

                String fieldName = f.getName();
                Class fieldType = f.getType();

                writer.name(fieldName);

                if (int.class.isAssignableFrom(fieldType)) {
                    writer.value(getInt(fieldName));
                } else if (long.class.isAssignableFrom(fieldType)) {
                    writer.value(getLong(fieldName));
                } else if (double.class.isAssignableFrom(fieldType)) {
                    writer.value(getDouble(fieldName));
                } else if (boolean.class.isAssignableFrom(fieldType)) {
                    writer.value(getBoolean(fieldName));

                } else if (String.class.isAssignableFrom(fieldType)) {
                    writer.value((String) getObject(fieldName));

                } else if (Map.class.isAssignableFrom(fieldType)) {

                    writeMap(writer, fieldName);

                } else if (List.class.isAssignableFrom(fieldType)) {

                    writeList(writer, fieldName);

                } else if (fieldType.isArray()) {

                    writer.beginArray();
                    Object array = getObject(fieldName);

                    int length = Array.getLength(array);

                    for (int i = 0; i < length; i++) {
                        Object arrayElement = Array.get(array, i);
                        Class componentType = arrayElement.getClass();
                        if (JSONinstaBean.class.isAssignableFrom(componentType)) {
                            ((JSONinstaBean) arrayElement).write(writer);
                        } else if (int.class.isAssignableFrom(componentType)) {
                            writer.value((int) arrayElement);
                        } else if (long.class.isAssignableFrom(componentType)) {
                            writer.value((long) arrayElement);
                        } else if (double.class.isAssignableFrom(componentType)) {
                            writer.value((double) arrayElement);
                        } else if (boolean.class.isAssignableFrom(componentType)) {
                            writer.value((boolean) arrayElement);
                        } else if (String.class.isAssignableFrom(fieldType)) {
                            writer.value((String) arrayElement);
                        }
                    }
                    writer.endArray();

                } else if (JSONinstaBean.class.isAssignableFrom(fieldType)) {
                    ((JSONinstaBean) getObject(fieldName)).write(writer);
                }
            }
            writer.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }


    public void writeMap(JsonWriter writer, String fieldName) {

        try {
            Map m = (Map) getObject(fieldName);
            if (m == null) {
                writer.beginObject();
                writer.endObject();
                return;
            }

            Class mapClass = m.getClass();
            Class fieldType = getContainerGenericClass(mapClass, this.type.getField(fieldName), true);

            writer.beginObject();
            for (Object k : m.keySet()) {
                String key = (String) k;
                writer.name(key);

                Object value = m.get(key);

                if (int.class.isAssignableFrom(fieldType)) {
                    writer.value((int) value);
                } else if (long.class.isAssignableFrom(fieldType)) {
                    writer.value((long) value);
                } else if (double.class.isAssignableFrom(fieldType)) {
                    writer.value((double) value);
                } else if (boolean.class.isAssignableFrom(fieldType)) {
                    writer.value((boolean) value);
                } else if (String.class.isAssignableFrom(fieldType)) {
                    writer.value((String) value);
                } else if (fieldType.isArray()) {

                    writer.beginArray();
                    Object array = getObject(key);

                    int length = Array.getLength(array);

                    for (int i = 0; i < length; i++) {
                        Object arrayElement = Array.get(array, i);
                        Class componentType = arrayElement.getClass();
                        if (JSONinstaBean.class.isAssignableFrom(componentType)) {
                            ((JSONinstaBean) arrayElement).write(writer);
                        } else if (int.class.isAssignableFrom(componentType)) {
                            writer.value((int) arrayElement);
                        } else if (long.class.isAssignableFrom(componentType)) {
                            writer.value((long) arrayElement);
                        } else if (double.class.isAssignableFrom(componentType)) {
                            writer.value((double) arrayElement);
                        } else if (boolean.class.isAssignableFrom(componentType)) {
                            writer.value((boolean) arrayElement);
                        } else if (String.class.isAssignableFrom(fieldType)) {
                            writer.value((String) arrayElement);
                        }
                    }
                    writer.endArray();

                } else if (JSONinstaBean.class.isAssignableFrom(fieldType)) {
                    ((JSONinstaBean) value).write(writer);
                }
            }
            writer.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

    }

    public void writeList(JsonWriter writer, String fieldName) {

        try {
            List list = (List) getObject(fieldName);

            if (list == null) {
                // write empty array
                writer.beginArray();
                writer.endArray();
                return;
            }

            Class listClass = list.getClass();
            Class fieldType = getContainerGenericClass(listClass, this.type.getField(fieldName), false);

            writer.beginArray();
            for (Object value : list) {


                if (int.class.isAssignableFrom(fieldType)) {
                    writer.value((int) value);
                } else if (long.class.isAssignableFrom(fieldType)) {
                    writer.value((long) value);
                } else if (double.class.isAssignableFrom(fieldType)) {
                    writer.value((double) value);
                } else if (boolean.class.isAssignableFrom(fieldType)) {
                    writer.value((boolean) value);
                } else if (String.class.isAssignableFrom(fieldType)) {
                    writer.value((String) value);
                } else if (fieldType.isArray()) {

                    writer.beginArray();
                    Object array = value;

                    int length = Array.getLength(array);

                    for (int i = 0; i < length; i++) {
                        Object arrayElement = Array.get(array, i);
                        Class componentType = arrayElement.getClass();
                        if (JSONinstaBean.class.isAssignableFrom(componentType)) {
                            ((JSONinstaBean) arrayElement).write(writer);
                        } else if (int.class.isAssignableFrom(componentType)) {
                            writer.value((int) arrayElement);
                        } else if (long.class.isAssignableFrom(componentType)) {
                            writer.value((long) arrayElement);
                        } else if (double.class.isAssignableFrom(componentType)) {
                            writer.value((double) arrayElement);
                        } else if (boolean.class.isAssignableFrom(componentType)) {
                            writer.value((boolean) arrayElement);
                        } else if (String.class.isAssignableFrom(fieldType)) {
                            writer.value((String) arrayElement);
                        }
                    }
                    writer.endArray();

                } else if (JSONinstaBean.class.isAssignableFrom(fieldType)) {
                    ((JSONinstaBean) value).write(writer);
                }
            }
            writer.endArray();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

    }


}
