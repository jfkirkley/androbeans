package org.androware.androbeans;


import org.androware.androbeans.utils.FilterLog;
import org.androware.androbeans.utils.ReflectionUtils;
import org.androware.androbeans.utils.Utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by jkirkley on 6/18/16.
 */
public class LinkObjectReadListener extends InitializingReadListener {

    public static final String REFMAP_PREFIX = "__refmap__";
    public static final String IDREF_PREFIX = "__idref__";
    public static final String BEAN_ID = "__id__";
    public static final String MERGE_PREFIX = "__merge__";
    public static final String DEFAULT_POST_INIT_METHOD = "__init__";
    public static final String DEFAULT_TYPE_OVERRIDE_METHOD = "__get_type_overrides__";


    private HashMap<String, Map> refMap = new HashMap<>();
    private HashMap<String, Object> idMap = new HashMap<>();
    //private HashMap<ObjectReader, Stack<BeanRef>> pendingMergeMap = new HashMap<>();
    private HashMap<Object, BeanRef> pendingMergeMap = new HashMap<>();
    private HashMap<Class, String> typeOverrides = new HashMap<>();


    public class BeanRef {
        String prefix;
        String key;


        public BeanRef(String ref) {
            // prefix:key
            String tokens[] = ref.split(":");
            this.prefix = tokens[0];
            this.key = tokens[1];
        }

        public Object getBean() {

            if (prefix.equals(IDREF_PREFIX)) {

                return idMap.get(key);

            } else if (refMap.containsKey(prefix)) {
                // prefix is the refmap key
                Map m = refMap.get(prefix);
                if (m != null) {
                    return m.get(key);
                }
            }
            return null;
        }
    }


    @Override
    public void onFieldName(String fieldName, Field field, Object target, Object objId, ObjectReader objectReader) throws ObjectReadException {
        if (fieldName.startsWith(REFMAP_PREFIX)) {

            refMap.put(fieldName.substring(REFMAP_PREFIX.length()), objectReader.readRefMap());

        } else if (fieldName.equals(BEAN_ID)) {

            String id = (String) objectReader.nextValue();
            idMap.put(id, target);

        } else if (fieldName.equals(MERGE_PREFIX)) {

            pendingMergeMap.put(objId, new BeanRef((String) objectReader.nextValue()));

            /*
            try {
                Utils.addValueToMappedContainer(this.getClass().getField("pendingMergeMap"), pendingMergeMap, objectReader, new BeanRef((String) objectReader.nextValue()));
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            */
        }
    }

    @Override
    public Object onValue(Object value, Field field, ObjectReader objectReader) throws ObjectReadException {
        if (value instanceof String && ((String) value).startsWith(IDREF_PREFIX)) {
            return (new BeanRef((String) value)).getBean();
        }
        return value;
    }

    @Override
    public Object onReadDone(Object value, Object id, ObjectReader objectReader) {
        super.onReadDone(value, id, objectReader);

        if (pendingMergeMap.containsKey(id)) {

            BeanRef beanRef = pendingMergeMap.get(id);
            merge(value, beanRef.getBean());

            /*
            Stack<BeanRef> beanRefStack = pendingMergeMap.get(objectReader);
            while(!beanRefStack.empty()) {
                BeanRef beanRef = beanRefStack.pop();
                mergeObject(value, beanRef.getBean());
            }
            */
        }

        return null;
    }

    @Override
    public Object onCreate(Class type, ObjectReader objectReader) {
        if (typeOverrides.containsKey(type)) {
            String overrideType = typeOverrides.get(type);
            return ReflectionUtils.newInstance(overrideType);
        }
        return null;
    }

    @Override
    public void onPostCreate(Object newObject, ObjectReader objectReader) {
        Method method = ReflectionUtils.getMethod(newObject.getClass(), DEFAULT_TYPE_OVERRIDE_METHOD, Map.class);
        if (method != null) {
            ReflectionUtils.callMethod(newObject, method, typeOverrides);
        }

    }

    public void merge(Object thisBean, Object thatBean) {
        if (Map.class.isAssignableFrom(thisBean.getClass())) {

            mergeMap((Map) thisBean, (Map) thatBean);

        } else {

            mergeObject(thisBean, thatBean);
        }

    }

    public void mergeObject(Object thisBean, Object thatBean) {
        Class type = thisBean.getClass();

        Field fields[] = type.getFields();
        try {

            for (Field field : fields) {

                if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                Class fieldType = field.getType();

                Object thisVal = field.get(thisBean);
                Object thatVal = field.get(thatBean);

                if (thatVal != null) {

                    if (thisVal == null) {

                        if (Map.class.isAssignableFrom(fieldType)) {

                            field.set(thisBean, copyMap((Map)thatVal));

                        } else if (List.class.isAssignableFrom(fieldType)) {

                            field.set(thisBean, copyList((List)thatVal));

                        } else if(fieldType.isArray()) {

                            field.set(thisBean, Arrays.copyOf((Object[])thatVal, Array.getLength(thatVal)));

                        } else {

                            field.set(thisBean, ReflectionUtils.tryCopy(thatVal));
                        }

                    } else if (Map.class.isAssignableFrom(fieldType)) {

                        mergeMap((Map) thisVal, (Map) thatVal);

                    } else if (List.class.isAssignableFrom(fieldType)) {

                        field.set(thisBean, copyList((List)thatVal));

                    } else if(fieldType.isArray()) {

                        field.set(thisBean, Arrays.copyOf((Object[])thatVal, Array.getLength(thatVal)));

                    } else if (!Utils.isPrimitiveOrString(thisVal)) {

                        mergeObject(thisVal, thatVal);
                    }
                }
            }
        } catch (IllegalAccessException e) {
        }
    }

    public Map copyMap(Map map){
        HashMap newMap = new HashMap();
        for(Object k: map.keySet()) {
            newMap.put(k, map.get(k));
        }
        return map;
    }

    public List copyList(List list){
        ArrayList newList = new ArrayList();
        for(Object o: list) {
            newList.add(o);
        }
        return list;
    }

    public void mergeMap(Map myMap, Map otherMap) {
        for (Object k : otherMap.keySet()) {
            Object otherMapVal = otherMap.get(k);
            Object myMapVal = myMap.get(k);

            if (myMapVal != null) {
                if (myMapVal instanceof Map) {
                    mergeMap((Map) myMapVal, (Map) otherMapVal);
                } else if (!Utils.isPrimitiveOrString(myMapVal)) {
                    mergeObject(myMapVal, otherMapVal);
                }
            } else {
                myMap.put(k, otherMapVal);
            }
        }
    }


}
