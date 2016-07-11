package org.androware.androbeans;

import java.lang.reflect.Field;

import static android.R.attr.id;

/**
 * Created by jkirkley on 6/18/16.
 */
public interface ObjectReadListener {

    public void onFieldName(String fieldName, Field field, Object target, Object id, ObjectReader objectReader) throws ObjectReadException;
    public Object onValue(Object value, Field field, ObjectReader objectReader) throws ObjectReadException;
    public Object onReadDone(Object value, Object id, ObjectReader objectReader);
    public Object onCreate(Class type, ObjectReader objectReader);
    public void onPostCreate(Object newObject, ObjectReader objectReader);
}
