package org.androware.androbeans.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.R.attr.data;

/**
 * Created by jkirkley on 4/25/17.
 */

public class EventSimulator {

    public static final int SWIPE_TYPE = 0;
    public static final int FLING_TYPE = 1;

    public static class EventInfo {
        public int type;

        public float xt;
        public float yt;
        public float fx;
        public float fy;

        // consists of tuples of [currX, currY, (t - startTime)]
        public float[] data;
    }


    public List<EventInfo> eventInfoList;


    private EventInfo currEventInfo;
    private long startTime;
    private List<Float> currentData = new ArrayList<>();

    public void recordStartSwipeEvent(float x, float y) {

        currEventInfo = new EventInfo();

        currEventInfo.type = SWIPE_TYPE;

        startTime = (new Date()).getTime();

        currentData.clear();

    }

    public void addEvent(float x, float y) {

        long t = (new Date()).getTime();




    }
}
