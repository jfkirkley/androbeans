package org.androware.androbeans.beans;

import android.util.JsonReader;



import java.io.IOException;
import java.util.HashMap;

import java.util.Stack;



import org.androware.androbeans.InstaBean;
import org.androware.androbeans.JSONinstaBean;

import org.androware.androbeans.utils.ConstructorSpec;


/**
 * Created by jkirkley on 5/7/16.
 */
public class Step extends JSONinstaBean {

    public static final int MAX_PARAMS = 1024;

    public String layout;
    public String processor;
    public String parentContainer;
    public String transitionClassName;

    public String targetFlow;
    public UI ui;

    public ConstructorSpec viewCustomizerSpec;


    private Nav currNav;

    public HashMap<String, String> meta;
    public HashMap<String, String> data;

    public HashMap<String, Nav> navMap;

    private Stack<Object> paramStack = new Stack<>();

    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void __init__() {
        l("__init__: has been called !!!!!!!!!!!!!!! --------------------------------");
    }

    public Step() {
    }

    public Step(Nav nav) {
        this.currNav = nav;
        this.name = nav.target;
    }


    public Step(JsonReader reader, Class type, InstaBean parent) throws IOException {
        super(reader, type, parent);
    }


    public Step(JsonReader reader, Class type) throws IOException {
        super(reader, type);
    }

}

