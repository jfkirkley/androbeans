package org.androware.androbeans;

import android.util.JsonReader;

import org.androware.androbeans.utils.ReflectionUtils;
import org.androware.androbeans.utils.Utils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by jkirkley on 6/18/16.
 */
public class LinkObjectReadListener implements ObjectReadListener {

    public static final String REFMAP_PREFIX = "__refmap__";
    public static final String IDREF_PREFIX = "__idref__";
    public static final String BEAN_ID = "__id__";
    public static final String MERGE_PREFIX = "__merge__";

    private HashMap<String, Map> refMap = new HashMap<>();
    private HashMap<String, Object> idMap = new HashMap<>();
    private HashMap<ObjectReader, BeanRef> pendingMergeMap = new HashMap<>();


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
    public void onFieldName(String fieldName, Field field, ObjectReader objectReader) throws IOException {
        if (fieldName.startsWith(REFMAP_PREFIX)) {

            try {
                refMap.put(fieldName.substring(REFMAP_PREFIX.length()), objectReader.readRefMap());
            } catch (IOException e) {
                // TODO log exception
            }

        } else if (fieldName.equals(BEAN_ID)) {
            String id = (String) objectReader.nextValue();
            idMap.put(id, objectReader.getTarget());

        } else if (fieldName.equals(MERGE_PREFIX)) {
            pendingMergeMap.put(objectReader, new BeanRef((String) objectReader.nextValue()));
        }
    }

    @Override
    public Object onValue(Object value, Field field, ObjectReader objectReader) throws IOException {
        return value;
    }

    @Override
    public Object onReadDone(Object value, ObjectReader objectReader) {
        if (pendingMergeMap.containsKey(objectReader)) {
            BeanRef beanRef = pendingMergeMap.get(objectReader);
            merge(value, beanRef.getBean());
        }
        return null;
    }

    public void merge(Object thisBean, Object thatBean) {
        Class type = thisBean.getClass();

        Field fields[] = type.getFields();
        try {

            for (Field field : fields) {

                if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                Class fieldType = field.getType();

                Object myVal = field.get(thisBean);
                Object otherVal = field.get(thatBean);

                if (otherVal != null) {

                    if (myVal == null) {

                        field.set(thisBean, otherVal);

                    } else if (Map.class.isAssignableFrom(fieldType)) {

                        mergeMap((Map) myVal, (Map) otherVal);

                    } else if (!Utils.isPrimitiveOrString(myVal)) {
                        merge(myVal, otherVal);
                    }
                }

            }
        } catch (IllegalAccessException e) {
        }
    }

    public void mergeMap(Map myMap, Map otherMap) {
        for (Object k : otherMap.keySet()) {
            Object otherMapVal = otherMap.get(k);
            Object myMapVal = myMap.get(k);

            if (myMapVal != null) {
                if (!Utils.isPrimitiveOrString(myMapVal)) {
                    merge(myMapVal, otherMapVal);
                } else if (myMapVal instanceof Map) {
                    mergeMap((Map) myMapVal, (Map) otherMapVal);
                }
            } else {
                myMap.put(k, otherMapVal);
            }
        }
    }


}
