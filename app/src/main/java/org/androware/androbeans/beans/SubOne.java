package org.androware.androbeans.beans;

import android.util.JsonReader;

import org.androware.androbeans.legacy.InstaBean;
import org.androware.androbeans.legacy.JSONinstaBean;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by jkirkley on 6/16/16.
 */
public class SubOne extends JSONinstaBean {

    public String s1;
    public HashMap<String, Object> stringObjectHashMap;

    public SubOne() {

    }

    public SubOne(JsonReader reader, Class type, InstaBean parent) throws IOException {
        super(reader, type, parent);
    }

}
