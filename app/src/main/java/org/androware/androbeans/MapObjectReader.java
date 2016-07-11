package org.androware.androbeans;

import org.androware.androbeans.utils.ReflectionUtils;
import org.androware.androbeans.utils.Utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jkirkley on 6/22/16.
 */
public class MapObjectReader implements ObjectReader {
    List<ObjectReadListener> objectReadListeners = new ArrayList<>();
    Map map;
    Object target;
    Class type;

    public MapObjectReader(Map map, Class type) {
        this.map = map;
        this.type = type;
        this.target = ReflectionUtils.newInstance(type);
    }

    @Override
    public Object read() throws ObjectReadException {
        try {
            for (Object k : map.keySet()) {

                String fieldName = k.toString();
                Object value = map.get(k);

                Field field = type.getField(fieldName);
                Class fieldType = field.getType();

                if (Utils.isPrimitiveOrString(value)) {
                    field.set(target, value);
                } else if (List.class.isAssignableFrom(fieldType)) {
                    field.set(target, readList(field, value));
                } else if (Map.class.isAssignableFrom(fieldType)) {
                    field.set(target, readMap(field, value));
                } else if (fieldType.isArray()) {
                    field.set(target, readArray(field, value));
                }

            }
        } catch (NoSuchFieldException e) {
            throw new ObjectReadException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        invokeListenersOnReadDone(target);

        return target;
    }

    public Object readArray(Field field, Object array) throws ObjectReadException {
        return readArray(field, array, false);
    }

    public Object readArray(Field field, Object array, boolean returnList) throws ObjectReadException {

        int length = Array.getLength(array);
        List list = new ArrayList();
        Class componentType = ReflectionUtils.getGenericType(field);

        for (int i = 0; i < length; i++) {
            Object arrayElement = Array.get(array, i);

            list.add(makeValue(componentType, arrayElement));
        }
        if (returnList) {
            return list;
        }
        return (componentType == Object.class) ? list.toArray() : ReflectionUtils.toTypedArray(list, componentType);
    }

    public Map readMap(Field field, Object object) throws ObjectReadException {

        if (object instanceof Map) {
            Map inputMap = (Map) object;

            HashMap map = new HashMap();

            Class componentType = field != null ? ReflectionUtils.getGenericType(field, 1) : Object.class;

            for (Object key : inputMap.keySet()) {

                Object value = inputMap.get(key);
                map.put(key, makeValue(componentType,value));

            }

            return map;
        }
        throw new ObjectReadException("Container field '" + field.getName() + "' with elements of type Map must get input in map format.");
    }

    public List readList(Field field, Object value) throws ObjectReadException {
        if (value.getClass().isArray()) {
            return (List) readArray(field, value, true);
        } else if (value instanceof List) {
            return readList(field, (List) value);
        }
        throw new ObjectReadException("Container field '" + field.getName() + " must get input of type list or array, but got: " + value.getClass());
    }

    public List readList(Field field, List inputList) throws ObjectReadException {
        List list = new ArrayList();

        Class componentType = field != null ? ReflectionUtils.getGenericType(field, 0) : Object.class;
        for (Object object : inputList) {

            list.add(makeValue(componentType, object));

        }

        return list;
    }

    private Object makeValue(Class componentType, Object object) throws ObjectReadException {
        if (componentType == Object.class) {
            return object;
        } else if (object instanceof Map) {
            return (new MapObjectReader((Map) object, componentType)).read();
        } else {
            throw new ObjectReadException("Non-primititive value of type '" + componentType.getName() + "' must get input in map format.");
        }
    }

    protected void invokeListenersOnReadDone(Object value) throws ObjectReadException {
        for (ObjectReadListener objectReadListener : objectReadListeners) {
            objectReadListener.onReadDone(value, null, this);  // TODO support needed for linking
        }
    }

    @Override
    public String nextFieldName() throws ObjectReadException {
        return null;
    }

    @Override
    public Object nextValue() throws ObjectReadException {
        return null;
    }

    @Override
    public Object readValue(Class fieldType) throws ObjectReadException {
        return null;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public Map readRefMap() throws ObjectReadException {
        return null;
    }

    @Override
    public void addObjectReadListener(ObjectReadListener objectReadListener) {

    }

    @Override
    public void removeOjectReadListener(ObjectReadListener objectReadListener) {

    }

    @Override
    public List<ObjectReadListener> getObjectReadListeners() {
        return null;
    }

    @Override
    public void setObjectReadListeners(List<ObjectReadListener> objectReadListeners) {
        this.objectReadListeners = objectReadListeners;
    }
}
