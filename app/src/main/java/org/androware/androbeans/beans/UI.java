package org.androware.androbeans.beans;

import android.util.JsonReader;

import java.io.IOException;

import org.androware.androbeans.legacy.InstaBean;
import org.androware.androbeans.legacy.JSONinstaBean;


/**
 * Created by jkirkley on 5/7/16.
 */
public class UI extends JSONinstaBean {

    public ListSpec listSpec;

    public UI() {
    }

    public UI(JsonReader reader, Class type, InstaBean parent) throws IOException {
        super(reader,type, parent);
    }

    public UI(JsonReader reader, Class type) throws IOException {
        super(reader,type);
    }


}

