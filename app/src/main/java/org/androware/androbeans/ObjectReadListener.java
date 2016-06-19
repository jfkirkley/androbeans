package org.androware.androbeans;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Created by jkirkley on 6/18/16.
 */
public interface ObjectReadListener {

    public void onFieldName(String fieldName, Field field, ObjectReader objectReader) throws IOException;
    public Object onValue(Object value, Field field, ObjectReader objectReader) throws IOException;
    public Object onReadDone(Object value, ObjectReader objectReader);
}
