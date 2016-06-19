package org.androware.androbeans;

import java.io.IOException;
import java.util.Map;

/**
 * Created by jkirkley on 6/17/16.
 */
public interface ObjectReader {

    public Object read() throws IOException;
    public String nextFieldName() throws IOException;
    public Object nextValue() throws IOException;
    public Object readValue(Class fieldType) throws IOException;
    public Object getTarget();
    public Map readRefMap() throws IOException;
    public void addObjectReadListener(ObjectReadListener objectReadListener);
    public void removeOjectReadListener(ObjectReadListener objectReadListener);
}
