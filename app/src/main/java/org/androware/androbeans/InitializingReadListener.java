package org.androware.androbeans;

import org.androware.androbeans.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by jkirkley on 6/26/16.
 */
public class InitializingReadListener implements ObjectReadListener {

    public static final String DEFAULT_POST_INIT_METHOD = "__init__";

    @Override
    public void onFieldName(String fieldName, Field field, ObjectReader objectReader) throws ObjectReadException {

    }

    @Override
    public Object onValue(Object value, Field field, ObjectReader objectReader) throws ObjectReadException {
        return null;
    }

    @Override
    public Object onReadDone(Object value, ObjectReader objectReader) {
        if(ReflectionUtils.hasMethod(value.getClass(), DEFAULT_POST_INIT_METHOD)){
            Method initMethod = ReflectionUtils.getMethod(value.getClass(), DEFAULT_POST_INIT_METHOD);
            ReflectionUtils.callMethod(value, initMethod);
        }
        return null;
    }

    @Override
    public Object onCreate(Class type, ObjectReader objectReader) {
        return null;
    }

    @Override
    public void onPostCreate(Object newObject, ObjectReader objectReader) {

    }
}
