package org.androware.androbeans.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.R.attr.y;


/**
 * Created by jkirkley on 2/14/17.
 */
public class SwipeDetector implements View.OnTouchListener, GestureHandler.FlingListener {
    public static final String TAG = "swipe";

    public void l(String t) {
        FilterLog.inst().log(TAG, t);
    }

    private static SwipeDetector instance = null;
    public static float DX_THRESHOLD = 20;
    public static float DY_THRESHOLD = 20;
    private float swipeThreshold;

    private float touchDownX;
    private float touchDownY;

    private GestureHandler gestureHandler;

    public static boolean isPortraitMode = false;

    public static SwipeDetector inst() {
        return inst(null, null);
    }

    public static SwipeDetector inst(ContextWrapper contextWrapper, GestureHandler.GestureClient gestureClient) {
        if (instance == null) {
            instance = new SwipeDetector(contextWrapper, gestureClient);
        }
        return instance;
    }


    public SwipeDetector(ContextWrapper contextWrapper, GestureHandler.GestureClient gestureClient) {
        WindowManager wm = (WindowManager) contextWrapper.getSystemService(Context.WINDOW_SERVICE);

        if(gestureClient != null) {
            gestureHandler = new GestureHandler(contextWrapper, gestureClient);
            gestureHandler.addFlingListener(this);
        }

        float screenWidth = wm.getDefaultDisplay().getWidth();
        float screenHeight = wm.getDefaultDisplay().getHeight();

        isPortraitMode = screenWidth > screenHeight;

        swipeThreshold = (screenHeight > screenWidth) ? screenHeight / 10 : screenWidth / 10;

        DY_THRESHOLD = DX_THRESHOLD = swipeThreshold / 2;
    }

    public boolean isSwiping() {
        return isHorizontalSwipe() || isVerticalSwipe();
    }

    public boolean isHorizontalSwipe() {
        //float dx = gestureHandler == null? 0: gestureHandler.getdX();
        //float dy = gestureHandler == null? 0: gestureHandler.getdY();
        return Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > swipeThreshold;
    }

    public boolean isVerticalSwipe() {
        //float dx = gestureHandler == null? 0: gestureHandler.getdX();
        //float dy = gestureHandler == null? 0: gestureHandler.getdY();
        return Math.abs(dx) < Math.abs(dy) && Math.abs(dy) > swipeThreshold;
    }

    public static final int START_SWIPE_DENOMINATOR = 20;

    public boolean isStartSwiping() {
        return isStartHorizontalSwipe() || isStartVerticalSwipe();
    }

    public boolean isStartHorizontalSwipe() {
        //float dx = gestureHandler == null? 0: gestureHandler.getdX();
        //float dy = gestureHandler == null? 0: gestureHandler.getdY();
        return Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > swipeThreshold/START_SWIPE_DENOMINATOR;
    }

    public boolean isStartVerticalSwipe() {
        //float dx = gestureHandler == null? 0: gestureHandler.getdX();
        //float dy = gestureHandler == null? 0: gestureHandler.getdY();

        return Math.abs(dx) < Math.abs(dy) && Math.abs(dy) > swipeThreshold/START_SWIPE_DENOMINATOR;
    }

    public boolean isStartSwipeToRight() {
        //float dx = gestureHandler == null? 0: gestureHandler.getdX();
        //float dy = gestureHandler == null? 0: gestureHandler.getdY();

        return dx > 0 && Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > swipeThreshold/START_SWIPE_DENOMINATOR;
    }

    public boolean isStartSwipeToLeft() {
        //float dx = gestureHandler == null? 0: gestureHandler.getdX();
        //float dy = gestureHandler == null? 0: gestureHandler.getdY();
        return dx < 0 && Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > swipeThreshold/START_SWIPE_DENOMINATOR;
    }

    public boolean isStartSwipeToTop() {
        //float dx = gestureHandler == null? 0: gestureHandler.getdX();
        //float dy = gestureHandler == null? 0: gestureHandler.getdY();
        return dy < 0 && Math.abs(dx) < Math.abs(dy) && Math.abs(dy) > swipeThreshold/START_SWIPE_DENOMINATOR;
    }

    public boolean isStartSwipeToBottom() {
        //float dx = gestureHandler == null? 0: gestureHandler.getdX();
        //float dy = gestureHandler == null? 0: gestureHandler.getdY();
        return dy > 0 && Math.abs(dx) < Math.abs(dy) && Math.abs(dy) > swipeThreshold/START_SWIPE_DENOMINATOR;
    }


    public boolean isSwipeToRight() {
        //float dx = gestureHandler == null? 0: gestureHandler.getdX();
        //float dy = gestureHandler == null? 0: gestureHandler.getdY();
        return dx > 0 && Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > swipeThreshold;
    }

    public boolean isSwipeToLeft() {
        //float dx = gestureHandler == null? 0: gestureHandler.getdX();
        //float dy = gestureHandler == null? 0: gestureHandler.getdY();
        return dx < 0 && Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > swipeThreshold;
    }

    public boolean isSwipeToTop() {
        //float dx = gestureHandler == null? 0: gestureHandler.getdX();
        //float dy = gestureHandler == null? 0: gestureHandler.getdY();
        return dy < 0 && Math.abs(dx) < Math.abs(dy) && Math.abs(dy) > swipeThreshold;
    }

    public boolean isSwipeToBottom() {
        //float dx = gestureHandler == null? 0: gestureHandler.getdX();
        //float dy = gestureHandler == null? 0: gestureHandler.getdY();
        return dy > 0 && Math.abs(dx) < Math.abs(dy) && Math.abs(dy) > swipeThreshold;
    }

    public boolean isInRect(RectF rect) {
        //Log.d("uni", touchDownX + ", " + touchDownY + " rectF: " + rect);
        return rect.contains(touchDownX, touchDownY);
    }

    public boolean below(float y) {
        return lastY < y;
    }

    public boolean betweenX(float left, float right) {
        return lastX > left && lastX < right;
    }

    public boolean betweenY(float top, float bottom) {
        return lastY > top && lastY < bottom;
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

    public float getDiffXPastThreshold() {
        return Math.abs(dx) > DX_THRESHOLD? diffX: 0;
    }

    public float getDiffYPastThreshold() {
        return Math.abs(dy) > DY_THRESHOLD? diffY: 0;
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

    public int getFlingX() {
        return gestureHandler == null? 0: this.gestureHandler.getCurrX();
    }

    public int getFlingY() {
        return gestureHandler == null? 0: this.gestureHandler.getCurrY();
    }

    public int getFlingDX() {
        return gestureHandler == null? 0: this.gestureHandler.getdX();
    }

    public int getFlingDY() {
        return gestureHandler == null? 0: this.gestureHandler.getdY();
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
    boolean pastSwipeDetectDelay = false;
    public static final long LONG_PRESS_TIME = 500;
    static final long SWIPE_DETECT_DELAY = 20;

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

    public void clearTouchListeners() {
        touchListeners.clear();
    }

    public void removeTouchListener(TouchListener touchListener) {
        touchListeners.remove(touchListener);
    }

    public void flingReset(int x, int y) {
        gestureHandler.resetCurrXY(x, y);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean handledInput = false;
        final int action = MotionEventCompat.getActionMasked(event);

        gestureHandler.onTouchEvent(event);

        switch (action) {
            case MotionEvent.ACTION_MOVE: {

                float scale = .75f;  // TODO should be configurable (sensitivity setting)

                newX = event.getX();
                newY = event.getY();

                diffX = (newX - lastX) / scale;
                diffY = (newY - lastY) / scale;

                dx += diffX;
                dy += diffY;

                //l(newY + ", " + lastY + ", " + diffY);
                //Log.d("x", "dx, dy: " + dx + ", " + dy);

                lastX = newX;
                lastY = newY;

                if(!pastSwipeDetectDelay) {
                    long t = (new Date()).getTime();
                    pastSwipeDetectDelay = t - touchDownTime > SWIPE_DETECT_DELAY;
                    if (pastSwipeDetectDelay) {
                        for(TouchListener touchListener: touchListeners){
                            touchListener.onTouchDown(this);
                        }
                    }
                } else {

                    for (TouchListener touchListener : touchListeners) {
                        touchListener.onTouchMove(this);
                    }
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {

                touchDown = true;
                pastSwipeDetectDelay = false;
                startTouchDownTime = (new Date()).getTime();

                touchDownX = newX = lastX = event.getX();
                touchDownY = newY = lastY = event.getY();
                dx = dy = 0;


/*
                for(TouchListener touchListener: touchListeners){
                    l("down: " + touchDownTime + ", " + touchDownY);
                    touchListener.onTouchDown(this);
                }
*/
                break;
            }

            case MotionEvent.ACTION_UP: {

                if (!pastSwipeDetectDelay) {
                    // very fast touch or managed not to cause a move event -  touch down event has not yet fired, so fire it
                    for(TouchListener touchListener: touchListeners){
                        touchListener.onTouchDown(this);
                    }
                }

                if(!gestureHandler.isFlinging()) {
                    handleTouchUp();
                }

                break;
            }
        }

        return handledInput;
    }

    @Override
    public void onFlingEnd(GestureHandler gestureHandler) {
        handleTouchUp();
    }

    private void handleTouchUp() {
        touchDown = false;

        touchDownTime = (new Date()).getTime() - startTouchDownTime;

        for(TouchListener touchListener: touchListeners){
            l("up: " + lastX + ", " + lastY + ", " + touchDownTime);
            touchListener.onTouchUp(this);
        }

        diffX = 0;
        diffY = 0;
        dx = dy = 0;

        touchDownX = newX = lastX = 0;
        touchDownY = newY = lastY = 0;

    }


    public void computeScroll() {
        synchronized (this) {
            if (gestureHandler != null) {
                gestureHandler.computeScroll();
            }
        }
    }

    public void doDraw(Canvas canvas) {
        synchronized (this) {
            if (gestureHandler != null) {
                this.gestureHandler.onDraw(canvas);
            }
        }
    }

    private synchronized void destroyGestureHandlerInternal() {
        this.gestureHandler = null;
        instance = null;
        System.gc();
    }

    public static void destroyGestureHandler() {
        if(instance != null) {
            instance.destroyGestureHandlerInternal();
        }
    }

    public synchronized GestureHandler getGestureHandlerInternal() {
        return gestureHandler;
    }

    public static GestureHandler getGestureHandler() {
        if(instance != null) {
            return instance.getGestureHandlerInternal();
        }
        return null;
    }
}
