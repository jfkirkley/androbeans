package org.androware.androbeans.beans;

import android.util.JsonReader;

import org.androware.androbeans.InstaBean;
import org.androware.androbeans.JSONinstaBean;

import java.io.IOException;
import java.util.List;

/**
 * Created by jkirkley on 6/16/16.
 */
public class Top extends JSONinstaBean {

    public String string;
    public int anInt;

    public List<SubOne> subOnes;

    public Top() {

    }

    public Top(JsonReader reader, Class type, InstaBean parent) throws IOException {
        super(reader, type, parent);
    }

}
