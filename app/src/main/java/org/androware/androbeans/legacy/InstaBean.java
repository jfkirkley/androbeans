package org.androware.androbeans.legacy;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


import java.lang.reflect.Array;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import java.util.Map;



/**
 * Created by jkirkley on 2/13/16.
 */
public class InstaBean implements Parcelable {
    Class type;
    Object element;
    String className;

    InstaBean parent;

    final static String TAG = "instabean";
    static HashMap<String, Class> overrideRegistry = new HashMap<>();


    HashMap<String, Field> name2FieldMap;

    public InstaBean(Map map, Class type, InstaBean parent) {
        this(type, parent);
        read(map, type);
    }

    public static InstaBean makeInstaBean(Map map, Class beanType, InstaBean parent) {

        Class[] t = {Map.class, Class.class, InstaBean.class};
        org.androware.androbeans.utils.ConstructorSpec constructorSpec = new org.androware.androbeans.utils.ConstructorSpec(beanType, t, map, beanType, parent);

        return (InstaBean) constructorSpec.build();
    }


    public InstaBean() {
        this.type = this.getClass();
        this.element = this;
        buildName2FieldMap();
    }

    public InstaBean(Object element, InstaBean parent) {
        this();
        this.parent = parent;

        if (parent == null) {
            // we are root
            idMap = new HashMap<>();
            refMap = new HashMap<>();
        }

        // NOTE:  how the fuck did this ever work?
        //this.element = element;
        //this.type = element.getClass();

        buildName2FieldMap();
    }

    public InstaBean(Object element) {
        this(element, null);
    }

    public InstaBean(Parcel in) {
        readFromParcel(in);
    }


    public InstaBean getRoot() {
        if (parent != null) {
            return parent.getRoot();
        }
        return this;
    }

    // TODO this should not be static, but be set in the root bean - will need parent assoc with getRoot method
    private HashMap<String, Map> refMap;
    private HashMap<String, InstaBean> idMap;

    public static final String REFMAP_PREFIX = "__refmap__";
    public static final String IDREF_PREFIX = "__idref__";
    public static final String BEAN_ID = "__id__";
    public static final String MERGE_PREFIX = "__merge__";


    // refMap
    protected void setRefMapInternal(String key, Map map) {
        refMap.put(key, map);
    }

    public void setRefMap(String key, Map map) {
        InstaBean root = getRoot();
        root.setRefMapInternal(key, map);
    }

    protected Map getRefMapInternal(String key) {
        return refMap.get(key);
    }

    public Map getRefMap(String key) {
        InstaBean root = getRoot();
        return root.getRefMapInternal(key);
    }

    protected boolean hasRefMapInternal(String key) {
        return refMap.containsKey(key);
    }

    public boolean hasRefMap(String key) {
        InstaBean root = getRoot();
        return root.hasRefMapInternal(key);
    }


    // idMap
    protected InstaBean getBeanByIdInternal(String key) {
        return idMap.get(key);
    }

    public InstaBean getBeanById(String key) {
        InstaBean root = getRoot();
        return root.getBeanByIdInternal(key);
    }

    protected void setBeanByIdInternal(String key, InstaBean bean) {
        idMap.put(key, bean);
    }

    public void setBeanById(String key, InstaBean bean) {
        InstaBean root = getRoot();
        root.setBeanByIdInternal(key, bean);
    }

    public void read(Map map, Class type) {

        for (Object k : map.keySet()) {

            String fieldName = k.toString();
            Class fieldType = org.androware.androbeans.utils.ReflectionUtils.getFieldType(type, fieldName);
            Object value = map.get(k);

            if (fieldType == null) {

                if (fieldName.startsWith(REFMAP_PREFIX)) {
                    //registerRefMap(map, fieldName.substring(REFMAP_PREFIX.length()));
                } else if (fieldName.equals(BEAN_ID)) {
                    String id = (String) value;
                    setBeanById(id, this);
                }
                continue;
            }

            try {
                /*
                if (List.class.isAssignableFrom(fieldType)) {

                    this.setObject(fieldName, getArrayAsList(map, fieldType, type.getField(fieldName)));

                } else if (Map.class.isAssignableFrom(fieldType)) {

                    this.setObject(fieldName, getMap(map, fieldType, fieldName));

                } else if (fieldType.isArray()) {

                    this.setObject(fieldName, ReflectionUtils.toTypedArray(getArrayAsList(map, fieldType, null), fieldType));

                } else if (JSONinstaBean.class.isAssignableFrom(fieldType)) {

                    this.setObject(fieldName, makeJSONinstaBean(map, fieldType, this));
*/
                if (int.class.isAssignableFrom(fieldType)) {
                    this.setInt(fieldName, (Integer) value);
                } else if (long.class.isAssignableFrom(fieldType)) {
                    this.setLong(fieldName, (Long) value);
                } else if (double.class.isAssignableFrom(fieldType)) {
                    this.setDouble(fieldName, (Double) value);
                } else if (boolean.class.isAssignableFrom(fieldType)) {
                    this.setBoolean(fieldName, (Boolean) value);
                } else if (String.class.isAssignableFrom(fieldType)) {
                    this.setObject(fieldName, (String) value);
                }

            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

    }

    public void mergeMap(Map myMap, Map otherMap) {
        for (Object k : otherMap.keySet()) {
            Object otherMapVal = otherMap.get(k);
            Object myMapVal = myMap.get(k);

            if (myMapVal != null) {
                if (myMapVal instanceof InstaBean) {
                    ((InstaBean) myMapVal).merge((InstaBean) otherMapVal);
                } else if (myMapVal instanceof Map) {
                    mergeMap((Map) myMapVal, (Map) otherMapVal);
                }
            } else {
                myMap.put(k, otherMapVal);
            }
        }
    }

    public void merge(InstaBean otherBean) {

        for (String fieldName : name2FieldMap.keySet()) {
            Class fieldType = name2FieldMap.get(fieldName).getType();

            if (fieldType == null) {
                continue;
            }
            try {
                Object myVal = getObject(fieldName);
                Object otherVal = otherBean.getObject(fieldName);

                if (otherVal != null) {
                    if (myVal == null) {

                        setObject(fieldName, otherVal);

                    } else if (Map.class.isAssignableFrom(fieldType)) {

                        mergeMap( (Map)myVal, (Map)otherVal);

                    } else if (InstaBean.class.isAssignableFrom(fieldType)) {

                        ((InstaBean) myVal).merge((InstaBean)otherVal);
                    }
                }
            } catch (IllegalAccessException e) {
            }
        }
    }


    public void readFromParcel(Parcel in) {


        Field fields[] = type.getFields();

        for (Field f : fields) {
            if (Modifier.isFinal(f.getModifiers())) {
                continue;
            }

            String fieldName = f.getName();
            Class fieldType = f.getType();

            Log.d(TAG, "read fieldName: " + fieldName);

            try {
                if (int.class.isAssignableFrom(fieldType)) {
                    setInt(fieldName, in.readInt());
                } else if (long.class.isAssignableFrom(fieldType)) {
                    setLong(fieldName, in.readLong());
                } else if (double.class.isAssignableFrom(fieldType)) {
                    setDouble(fieldName, in.readDouble());
                } else if (boolean.class.isAssignableFrom(fieldType)) {
                    setBoolean(fieldName, (Boolean) in.readValue(InstaBean.class.getClassLoader()));

                } else if (String.class.isAssignableFrom(fieldType)) {
                    setObject(fieldName, in.readString());
                } else if (fieldType.isArray()) {

                    Class componentType = fieldType.getComponentType();
                    //nextClass = componentType;
                    Log.d(TAG, "componentType: " + componentType.getName());

                    if (InstaBean.class.isAssignableFrom(componentType)) {
                        Log.d(TAG, "set instabean: " + fieldName);
                        setObject(fieldName, in.createTypedArray(InstaBean.CREATOR));
                    } else if (int.class.isAssignableFrom(componentType)) {
                        setObject(fieldName, in.createIntArray());
                    } else if (long.class.isAssignableFrom(componentType)) {
                        setObject(fieldName, in.createLongArray());
                    } else if (double.class.isAssignableFrom(componentType)) {
                        setObject(fieldName, in.createDoubleArray());
                    } else if (boolean.class.isAssignableFrom(componentType)) {
                        setObject(fieldName, in.createBooleanArray());
                    } else if (String.class.isAssignableFrom(componentType)) {
                        setObject(fieldName, in.createStringArray());
                    }

                } else if (InstaBean.class.isAssignableFrom(fieldType)) {
                    setObject(fieldName, in.readParcelable(InstaBean.class.getClassLoader()));
                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public InstaBean(Class beanType) {
        this.type = beanType;
        this.element = this;
        buildName2FieldMap();

    }

    public final String TYPE_OVERRIDE_SUFFIX = "OverrideType";

    public void buildName2FieldMap() {

        Field fields[] = type.getFields();
        name2FieldMap = new HashMap<>();

        for (Field f : fields) {
            name2FieldMap.put(f.getName(), f);
        }
    }

    public String toStringTest() {
        return toStringTest(0);
    }

    public String toStringTest(int indent) {

        Field fields[] = type.getFields();
        StringBuffer buf = new StringBuffer();
        for (Field f : fields) {

            String fieldName = f.getName();
            Class fieldType = f.getType();

            if((f.getModifiers() & Modifier.STATIC)!= 0) {
                continue;
            }

            buf.append("| ");
            for (int i = 0; i < indent; ++i) buf.append(' ');

            try {
                if (int.class.isAssignableFrom(fieldType)) {
                    buf.append(f.getName() + ": " + getInt(fieldName));
                } else if (long.class.isAssignableFrom(fieldType)) {
                    buf.append(f.getName() + ": " + getLong(fieldName));
                } else if (double.class.isAssignableFrom(fieldType)) {
                    buf.append(f.getName() + ": " + getDouble(fieldName));
                } else if (boolean.class.isAssignableFrom(fieldType)) {
                    buf.append(f.getName() + ": " + getBoolean(fieldName));
                } else if (String.class.isAssignableFrom(fieldType)) {
                    buf.append(f.getName() + ": " + getObject(fieldName));
                } else if (fieldType.isArray()) {

                    Object array = getObject(fieldName);
                    buf.append(f.getName() + ": [");
                    int length = Array.getLength(array);
                    String comma = "";
                    for (int i = 0; i < length; i++) {
                        Object arrayElement = Array.get(array, i);
                        if (InstaBean.class.isAssignableFrom(arrayElement.getClass())) {
                            buf.append(comma + ((InstaBean) arrayElement).toStringTest(indent + 2));
                        } else {
                            buf.append(comma + arrayElement);
                        }

                        comma = ", ";
                    }
                    buf.append("]\n");

                } else if (Map.class.isAssignableFrom(fieldType)) {
                    buf.append(f.getName() + ": " );

                    Map map = (Map) getObject(fieldName);
                    if(map != null) {
                        buf.append("\n");
                        for (Object k : map.keySet()) {
                            Object v = map.get(k);
                            buf.append("| ");
                            for (int i = 0; i < indent + 2; ++i) buf.append(' ');
                            if (InstaBean.class.isAssignableFrom(v.getClass())) {
                                buf.append(k.toString() + ": " + ((InstaBean) v).toStringTest(indent + 4));
                            } else {
                                buf.append(k.toString() + ": " + v.toString());
                            }
                            buf.append("\n");
                        }
                    } else {
                        buf.append("null");
                    }
                } else if (InstaBean.class.isAssignableFrom(fieldType)) {
                    InstaBean instaBean = (InstaBean) getObject(fieldName);
                    if(instaBean != null) {
                        buf.append(f.getName() + ": " + instaBean.toStringTest(indent + 2));
                    }
                }
                buf.append("\n");

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        return buf.toString();
    }

    public Class getType(String fieldName) {
        if (name2FieldMap.containsKey(fieldName)) {
            return name2FieldMap.get(fieldName).getType();
        }
        return null;
    }

    public Object getAttribute(String attributeName) {
        java.lang.reflect.Method method;
        String methodName = "get" + attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1);

        try {

            method = element.getClass().getMethod(methodName);
            Object val = method.invoke(element);

            return val;
        } catch (SecurityException e) {
            // exception handling omitted for brevity
        } catch (NoSuchMethodException e) {
            // exception handling omitted for brevity

        } catch (IllegalArgumentException e) { // exception handling omitted for brevity
        } catch (IllegalAccessException e) { // exception handling omitted for brevity
        } catch (InvocationTargetException e) { // exception handling omitted for brevity
        }
        return null;
    }


    public void setInt(String attributeName, int value) throws IllegalAccessException {
        Field f = name2FieldMap.get(attributeName);
        f.setInt(element, value);
    }

    public void setLong(String attributeName, long value) throws IllegalAccessException {
        Field f = name2FieldMap.get(attributeName);
        f.setLong(element, value);
    }

    public void setShort(String attributeName, short value) throws IllegalAccessException {
        Field f = name2FieldMap.get(attributeName);
        f.setShort(element, value);
    }

    public void setBoolean(String attributeName, boolean value) throws IllegalAccessException {
        Field f = name2FieldMap.get(attributeName);
        f.setBoolean(element, value);
    }

    public void setObject(String attributeName, Object value) throws IllegalAccessException {
        Field f = name2FieldMap.get(attributeName);
        f.set(element, value);
    }

    public void setDouble(String attributeName, double value) throws IllegalAccessException {
        Field f = name2FieldMap.get(attributeName);
        f.setDouble(element, value);
    }


    public int getInt(String attributeName) throws IllegalAccessException {
        Field f = name2FieldMap.get(attributeName);
        return f.getInt(element);
    }

    public long getLong(String attributeName) throws IllegalAccessException {
        Field f = name2FieldMap.get(attributeName);
        return f.getLong(element);
    }

    public short getShort(String attributeName) throws IllegalAccessException {
        Field f = name2FieldMap.get(attributeName);
        return f.getShort(element);
    }

    public boolean getBoolean(String attributeName) throws IllegalAccessException {
        Field f = name2FieldMap.get(attributeName);
        return f.getBoolean(element);
    }

    public Object getObject(String attributeName) throws IllegalAccessException {
        Field f = name2FieldMap.get(attributeName);
        return f.get(element);
    }

    public double getDouble(String attributeName) throws IllegalAccessException {
        Field f = name2FieldMap.get(attributeName);
        return f.getDouble(element);
    }

//public static Class nextClass;
public static final Parcelable.Creator<InstaBean> CREATOR
        = new Parcelable.Creator<InstaBean>() {


    //HashMap<Thread, Class> thread2TopLevelClassMap
    public InstaBean createFromParcel(Parcel in) {
                        /*


            int p = in.dataPosition();
            //in.setDataPosition(0);
            while(in.dataAvail() > 0) {
                Object v = in.readValue(String.class.getClassLoader());
                Log.d(TAG, in.dataPosition() + ": parcel: " + v);
            }
            in.setDataPosition(p);
            */

        String className = in.readString();
        //if(className == null ) className = "org.androware.androbeans.lesson.Exercise";
        Log.d(TAG, "createFromParcel: " + className);

        try {
            //Class type = className==null? InstaBean.nextClass: Class.forName(className);
            Class type = Class.forName(className);
            InstaBean instaBean = (InstaBean) type.newInstance();
            instaBean.readFromParcel(in);
            return instaBean;
        } catch (ClassNotFoundException ce) {
            ce.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return new InstaBean(in);
    }

    public InstaBean[] newArray(int size) {
        return new InstaBean[size];
    }
};

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {


        Field fields[] = type.getFields();

        // write className as header
        dest.writeString(getClass().getName());


        Log.d(TAG, "writeToParcel: " + getClass().getName());

        for (Field f : fields) {

            if (Modifier.isFinal(f.getModifiers())) {
                continue;
            }
            String fieldName = f.getName();
            Class fieldType = f.getType();


            try {
                if (int.class.isAssignableFrom(fieldType)) {
                    dest.writeInt(getInt(fieldName));
                } else if (long.class.isAssignableFrom(fieldType)) {
                    dest.writeLong(getLong(fieldName));
                } else if (double.class.isAssignableFrom(fieldType)) {
                    dest.writeDouble(getDouble(fieldName));
                } else if (boolean.class.isAssignableFrom(fieldType)) {
                    dest.writeValue(getBoolean(fieldName));
                    //dest.writeBooleanArray( getBoolean(fieldName));

                } else if (String.class.isAssignableFrom(fieldType)) {
                    dest.writeString((String) getObject(fieldName));
                } else if (fieldType.isArray()) {

                    Object array = getObject(fieldName);
                    int length = Array.getLength(array);

                    if (length > 0) {
                        Object arrayElement = Array.get(array, 0);
                        Class componentType = arrayElement.getClass();

                        if (InstaBean.class.isAssignableFrom(componentType)) {
                            dest.writeParcelableArray((Parcelable[]) array, 0);
                        } else if (int.class.isAssignableFrom(componentType)) {
                            dest.writeIntArray((int[]) array);
                        } else if (long.class.isAssignableFrom(componentType)) {
                            dest.writeLongArray((long[]) array);
                        } else if (double.class.isAssignableFrom(componentType)) {
                            dest.writeDoubleArray((double[]) array);
                        } else if (boolean.class.isAssignableFrom(componentType)) {
                            dest.writeBooleanArray((boolean[]) array);
                        } else if (String.class.isAssignableFrom(componentType)) {
                            dest.writeStringArray((String[]) array);
                        }
                    }

                } else if (InstaBean.class.isAssignableFrom(fieldType)) {
                    dest.writeParcelable(((InstaBean) getObject(fieldName)), 0);
                }


            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

    }

    //////////    Override Registry static methods
    public static String makeOverrideClassKey(Class ownerClass, String fieldName) {
        return ownerClass.getName() + ":" + fieldName;
    }

    public static void registerOverrideClass(Class ownerClass, String fieldName, Class overrideClass) {
        overrideRegistry.put(makeOverrideClassKey(ownerClass, fieldName), overrideClass);
    }

    public static Class checkRegistry(Class ownerClass, String fieldName) {
        return overrideRegistry.get(makeOverrideClassKey(ownerClass, fieldName));
    }

}
