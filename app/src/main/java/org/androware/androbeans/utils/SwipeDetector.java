package org.androware.androbeans.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.R.attr.x;
import static android.R.attr.y;

/**
 * Created by jkirkley on 2/14/17.
 */
public class SwipeDetector implements View.OnTouchListener {
    public static final String TAG = "swipe";

    public void l(String t) {
        FilterLog.inst().log(TAG, t);
    }

    private static SwipeDetector instance = null;
    public static float DX_THRESHOLD = 20;
    private float swipeThreshold;

    private float touchDownX;
    private float touchDownY;

    public static SwipeDetector inst() {
        return inst(null);
    }

    public static SwipeDetector inst(ContextWrapper contextWrapper) {
        if (instance == null) {
            instance = new SwipeDetector(contextWrapper);
        }
        return instance;
    }

    public SwipeDetector(ContextWrapper contextWrapper) {
        WindowManager wm = (WindowManager) contextWrapper.getSystemService(Context.WINDOW_SERVICE);

        float screenWidth = wm.getDefaultDisplay().getWidth();
        float screenHeight = wm.getDefaultDisplay().getHeight();

        swipeThreshold = (screenHeight > screenWidth) ? screenHeight / 10 : screenWidth / 10;
        DX_THRESHOLD = swipeThreshold / 2;
    }

    public void setTouchDown(float x, float y) {
        touchDownX = x;
        touchDownY = y;
    }

    public boolean isSwiping() {
        return isHorizontalSwipe() || isVerticalSwipe();
    }

    public boolean isHorizontalSwipe() {
        return isHorizontalSwipe(lastX, lastY);
    }

    public boolean isHorizontalSwipe(float x, float y) {
        float xdiff = touchDownX - x;
        float ydiff = touchDownY - y;
        return Math.abs(xdiff) > Math.abs(ydiff) && Math.abs(xdiff) > swipeThreshold;
    }

    public boolean isVerticalSwipe() {
        return isVerticalSwipe(lastX, lastY);
    }

    public boolean isVerticalSwipe(float x, float y) {
        float xdiff = touchDownX - x;
        float ydiff = touchDownY - y;
        return Math.abs(xdiff) < Math.abs(ydiff) && Math.abs(ydiff) > swipeThreshold;
    }

    public boolean isSwipeToRight() {
        return isSwipeToRight(lastX, lastY);
    }
    public boolean isSwipeToRight(float x, float y) {
        float xdiff = touchDownX - x;
        float ydiff = touchDownY - y;
        return xdiff < 0 && Math.abs(xdiff) > Math.abs(ydiff) && Math.abs(xdiff) > swipeThreshold;
    }

    public boolean isSwipeToLeft() {
        return isSwipeToLeft(lastX, lastY);
    }

    public boolean isSwipeToLeft(float x, float y) {
        float xdiff = touchDownX - x;
        float ydiff = touchDownY - y;
        return xdiff > 0 && Math.abs(xdiff) > Math.abs(ydiff) && Math.abs(xdiff) > swipeThreshold;
    }

    public boolean isSwipeToTop() {
        return isSwipeToTop(lastX, lastY);
    }

    public boolean isSwipeToTop(float x, float y) {
        float xdiff = touchDownX - x;
        float ydiff = touchDownY - y;
        return ydiff > 0 && Math.abs(xdiff) < Math.abs(ydiff) && Math.abs(ydiff) > swipeThreshold;
    }

    public boolean isSwipeToBottom() {
        return isSwipeToBottom(lastX, lastY);
    }

    public boolean isSwipeToBottom(float x, float y) {
        float xdiff = touchDownX - x;
        float ydiff = touchDownY - y;
        return ydiff < 0 && Math.abs(xdiff) < Math.abs(ydiff) && Math.abs(ydiff) > swipeThreshold;
    }

    public boolean isInRect(RectF rect) {
        return rect.contains(touchDownX, touchDownY);
    }

    public boolean below(float y) {
        return lastY < y;
    }

    float diffX = 0;
    float diffY = 0;

    float dx = 0;
    float dy = 0;

    public float getLastX() {
        return lastX;
    }

    public float getLastY() {
        return lastY;
    }

    public float getDiffX() {
        return diffX;
    }

    public float getDiffY() {
        return diffY;
    }

    public float getDx() {
        return dx;
    }

    public float getDy() {
        return dy;
    }

    public float getTouchDownX() {
        return touchDownX;
    }

    public float getTouchDownY() {
        return touchDownY;
    }


    public boolean isTouchDown() {
        return touchDown;
    }

    float lastX = -1f;
    float lastY = -1f;

    float newX;
    float newY;

    long touchDownTime;
    long startTouchDownTime = 0;
    boolean touchDown = false;
    public static final long LONG_PRESS_TIME = 500;

    public boolean isLongPress() {
        return touchDownTime > LONG_PRESS_TIME;
    }

    public interface TouchListener {
        public void onTouchDown(SwipeDetector swipeDetector);
        public void onTouchMove(SwipeDetector swipeDetector);
        public void onTouchUp(SwipeDetector swipeDetector);
    }

    List<TouchListener> touchListeners = new ArrayList<>();

    public void addTouchListener(TouchListener touchListener) {
        touchListeners.add(touchListener);
    }

    public void removeTouchListener(TouchListener touchListener) {
        touchListeners.remove(touchListener);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean handledInput = false;
        final int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case MotionEvent.ACTION_MOVE: {

                float scale = .75f;  // TODO should be configurable (sensitivity setting)

                newX = event.getX();
                newY = event.getY();

                diffX = (lastX - newX) / scale;
                diffY = (lastY - newY) / scale;

                dx += diffX;
                dy += diffY;

                lastX = newX;
                lastY = newY;

                for(TouchListener touchListener: touchListeners){
                    touchListener.onTouchMove(this);
                }

                break;
            }

            case MotionEvent.ACTION_DOWN: {

                touchDown = true;

                startTouchDownTime = (new Date()).getTime();

                touchDownX = newX = lastX = event.getX();
                touchDownY = newY = lastY = event.getY();


                for(TouchListener touchListener: touchListeners){
                    l("down: " + touchDownTime + ", " + touchDownY);
                    touchListener.onTouchDown(this);
                }

                break;
            }

            case MotionEvent.ACTION_UP: {

                diffX = 0;
                diffY = 0;

                touchDown = false;

                touchDownTime = (new Date()).getTime() - startTouchDownTime;



                for(TouchListener touchListener: touchListeners){
                    l("up: " + lastX + ", " + lastY + ", " + touchDownTime);
                    touchListener.onTouchUp(this);
                }


                //touchDownX = lastX;
                //touchDownY = lastY;

                break;
            }
        }

        return handledInput;
    }

}
