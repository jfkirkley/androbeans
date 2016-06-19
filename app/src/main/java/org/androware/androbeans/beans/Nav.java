package org.androware.androbeans.beans;


import android.support.v4.app.FragmentTransaction;
import android.util.JsonReader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.androware.androbeans.FilterLog;
import org.androware.androbeans.InstaBean;
import org.androware.androbeans.JSONinstaBean;

/**
 * Created by jkirkley on 5/7/16.
 */
public class Nav extends JSONinstaBean {
    public final static String TAG = "nav";

    public String compName;
    public String event;
    public String anim_in;
    public String anim_out;

    public static final String ROOT_VIEW = "__ROOT_VIEW__";

    public static final String GEN_NEXT = "__NEXT__";
    public static final String GEN_PREV = "__PREV__";

    public static Set<String> SPECIAL_NAV_TARGETS = new HashSet<>();

    static {
        SPECIAL_NAV_TARGETS.add(GEN_NEXT);
        SPECIAL_NAV_TARGETS.add(GEN_PREV);
    }

    public String target;

    public boolean isPrev(){
        if(target != null) {
            return target.equals(Nav.GEN_PREV);
        }
        return false;
    }

    public boolean isNext(){
        if(target != null) {
            return target.equals(Nav.GEN_NEXT);
        }
        return false;
    }

    public List getItems() {
        return items;
    }

    public void setItems(List items) {
        this.items = items;
    }

    public boolean useStepGenerator;

    List items;

    public Nav() {

    }
    public Nav(String target, boolean useStepGenerator) {
        this.target = target;
        this.useStepGenerator = useStepGenerator;
    }

    // construct a nav for specific target
    public Nav(String target) {
        this(target, false);
    }

    // construct a nav to invoke step generator
    public Nav(boolean useStepGenerator) {
        this(null, useStepGenerator);
    }

    public Nav(JsonReader reader, Class type, InstaBean parent) throws IOException {
        super(reader, type, parent);
    }


    public Nav(JsonReader reader, Class type) throws IOException {
        super(reader, type);
    }

    public void setTarget(String target) {
        if(this.target == null || !SPECIAL_NAV_TARGETS.contains(this.target) ) {
            this.target = target;
        }
    }

}

