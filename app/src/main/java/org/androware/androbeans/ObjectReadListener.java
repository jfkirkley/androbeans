package org.androware.androbeans;

import java.lang.reflect.Field;

/**
 * Created by jkirkley on 6/18/16.
 */
public interface ObjectReadListener {

    public void onFieldName(String fieldName, Field field, ObjectReader objectReader) throws ObjectReadException;
    public Object onValue(Object value, Field field, ObjectReader objectReader) throws ObjectReadException;
    public Object onReadDone(Object value, ObjectReader objectReader);
    public Object onCreate(Class type, ObjectReader objectReader);
    public void onPostCreate(Object newObject, ObjectReader objectReader);
}
