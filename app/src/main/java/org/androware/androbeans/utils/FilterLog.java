package org.androware.androbeans.utils;

import java.util.Set;



import android.util.Log;

import java.util.HashSet;


/**
 * Created by jkirkley on 3/21/16.
 */
public class FilterLog {
    public static boolean loggingEnabled = true;

    Set<String> activeTags;

    static FilterLog filterLog = null;
    public static FilterLog inst() {
        if(filterLog == null) {
            filterLog = new FilterLog();
        }
        return filterLog;
    }


    public FilterLog(Set<String> activeTags) {
        this.activeTags = activeTags;
    }

    public FilterLog() {
        this(new HashSet<String>());
    }

    public void activateTag(String tag) {
        activeTags.add(tag);
    }

    public void deactivateTag(String tag) {
        activeTags.remove(tag);
    }

    public void log(String tag, String s) {
        if(loggingEnabled && activeTags.contains(tag) && s != null && tag != null) {
            Log.d(tag, s);
        }
    }


    public static void l(String tag, String s) {
        inst().log(tag, s);
    }
}
