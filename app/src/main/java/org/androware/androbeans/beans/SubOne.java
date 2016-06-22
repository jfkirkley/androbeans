package org.androware.androbeans.beans;

import android.util.JsonReader;

import org.androware.androbeans.ObjectReader;
import org.androware.androbeans.legacy.InstaBean;
import org.androware.androbeans.legacy.JSONinstaBean;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jkirkley on 6/16/16.
 */
public class SubOne  {

    public String s1;
    public HashMap<String, Object> stringObjectHashMap;

    public SubOne() {

    }

    public SubOne(String s, HashMap<String, Object> m) {
        s1 = s;
        stringObjectHashMap = m;
    }


}
