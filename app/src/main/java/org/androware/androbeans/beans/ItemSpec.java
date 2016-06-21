package org.androware.androbeans.beans;

import android.util.JsonReader;

import java.io.IOException;
import java.util.HashMap;

import org.androware.androbeans.legacy.InstaBean;
import org.androware.androbeans.legacy.JSONinstaBean;

/**
 * Created by jkirkley on 5/7/16.
 */
public class ItemSpec extends JSONinstaBean {

    public HashMap<String,Object> props;

    public ItemSpec() {
    }

    public ItemSpec(JsonReader reader, Class type, InstaBean parent) throws IOException {
        super(reader, type, parent);
    }

    public ItemSpec(JsonReader reader, Class type) throws IOException {
        super(reader,type);
    }

    public String getProp(String key) {
        return (String)props.get(key);
    }

    public boolean hasProp(String key) {
        return props.containsKey(key);
    }
}

