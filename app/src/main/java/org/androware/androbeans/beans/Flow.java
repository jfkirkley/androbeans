package org.androware.androbeans.beans;

import android.util.JsonReader;

import java.io.IOException;
import java.util.HashMap;

import org.androware.androbeans.InstaBean;
import org.androware.androbeans.JSONinstaBean;
import org.androware.androbeans.utils.ConstructorSpec;

/**
 * Created by jkirkley on 5/7/16.
 */
public class Flow extends JSONinstaBean {

    public String fragmentContainer;
    public HashMap<String, Step> steps;
    public String layout;
    public String processor;

    public ConstructorSpec stepGeneratorSpec;

    //public StepGenerator stepGenerator;

    //public String firstStep;
    public Nav startNav;    // navigates to the first step


    public Flow() {
    }


    public Flow(JsonReader reader, Class type, InstaBean parent) throws IOException {
        super(reader, type, parent);
    }

    public Flow(JsonReader reader, Class type) throws IOException {
        super(reader,type, null);

    }



}
