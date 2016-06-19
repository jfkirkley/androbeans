package org.androware.androbeans.beans;

import android.util.JsonReader;
import android.view.View;

import java.io.IOException;
import java.util.HashMap;

import org.androware.androbeans.InstaBean;
import org.androware.androbeans.JSONinstaBean;
import org.androware.androbeans.R;


/**
 * Created by jkirkley on 5/7/16.
 */
public class UI extends JSONinstaBean {


    public UI() {
    }

    public UI(JsonReader reader, Class type, InstaBean parent) throws IOException {
        super(reader,type, parent);
    }

    public UI(JsonReader reader, Class type) throws IOException {
        super(reader,type);
    }


}

