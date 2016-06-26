package org.androware.androbeans;

import java.util.List;
import java.util.Map;

/**
 * Created by jkirkley on 6/17/16.
 */
public interface ObjectReader {

    public Object read() throws ObjectReadException;

    public String nextFieldName() throws ObjectReadException;

    public Object nextValue() throws ObjectReadException;

    public Object readValue(Class fieldType) throws ObjectReadException;

    public Object getTarget();

    public Map readRefMap() throws ObjectReadException;

    public void addObjectReadListener(ObjectReadListener objectReadListener);

    public void removeOjectReadListener(ObjectReadListener objectReadListener);

    public List<ObjectReadListener> getObjectReadListeners();

    public void setObjectReadListeners(List<ObjectReadListener> objectReadListeners);

}