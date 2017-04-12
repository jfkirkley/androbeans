package org.androware.androbeans.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.EdgeEffectCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.OverScroller;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jkirkley on 4/12/17.
 */

public class GestureHandler {

    public interface FlingListener {
        public void onFlingEnd(GestureHandler gestureHandler);

    }

    public interface GestureClient {
        public int getScrollXRange();

        public int getScrollYRange();

        public Rect getContentRect();

        public View getView();

    }

    private List<FlingListener> flingListeners = new ArrayList<>();


    private int currX = 0;
    private int currY = 0;


    // Current attribute values and Paints.
    private Paint mDataPaint;
    // State objects and values related to gesture tracking.

    private GestureDetectorCompat mGestureDetector;
    private OverScroller mScroller;

    // Edge effect / overscroll tracking objects.
    private EdgeEffectCompat mEdgeEffectTop;
    private EdgeEffectCompat mEdgeEffectBottom;
    private EdgeEffectCompat mEdgeEffectLeft;
    private EdgeEffectCompat mEdgeEffectRight;

    private boolean mEdgeEffectTopActive;
    private boolean mEdgeEffectBottomActive;
    private boolean mEdgeEffectLeftActive;
    private boolean mEdgeEffectRightActive;

    float screenWidth;
    float screenHeight;

    private GestureClient gestureClient;

    // Buffers for storing current X and Y stops. See the computeAxisStops method for more details.

    /**
     * The simple math function Y = fun(X) to draw on the chart.
     *
     * @param x The X value
     * @return The Y value
     */
    protected static float fun(float x) {
        return (float) Math.pow(x, 3) - x / 4;
    }

    public GestureHandler(Context context, GestureClient gestureClient) {

        this.gestureClient = gestureClient;
        // Sets up interactions
        mGestureDetector = new GestureDetectorCompat(context, mGestureListener);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight();

        mScroller = new OverScroller(context);

        currX = currY = 0;

        mEdgeEffectLeft = new EdgeEffectCompat(context);
        mEdgeEffectTop = new EdgeEffectCompat(context);
        mEdgeEffectRight = new EdgeEffectCompat(context);
        mEdgeEffectBottom = new EdgeEffectCompat(context);

        new Thread(new FlingEndChecker()).start();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Methods and objects related to drawing
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void drawEdgeEffectsUnclipped(Canvas canvas) {
        // The methods below rotate and translate the canvas as needed before drawing the glow,
        // since EdgeEffectCompat always draws a top-glow at 0,0.

        boolean needsInvalidate = false;

        Rect contentRect = gestureClient.getContentRect();

        if (!mEdgeEffectTop.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(contentRect.left, contentRect.top);
            mEdgeEffectTop.setSize(contentRect.width(), contentRect.height());
            if (mEdgeEffectTop.draw(canvas)) {
                needsInvalidate = true;
            }
            canvas.restoreToCount(restoreCount);
        }

        if (!mEdgeEffectBottom.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(2 * contentRect.left - contentRect.right, contentRect.bottom);
            canvas.rotate(180, contentRect.width(), 0);
            mEdgeEffectBottom.setSize(contentRect.width(), contentRect.height());
            if (mEdgeEffectBottom.draw(canvas)) {
                needsInvalidate = true;
            }
            canvas.restoreToCount(restoreCount);
        }


        if (!mEdgeEffectLeft.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(contentRect.left, contentRect.bottom);
            canvas.rotate(-90, 0, 0);
            mEdgeEffectLeft.setSize(contentRect.height(), contentRect.height());
            if (mEdgeEffectLeft.draw(canvas)) {
                needsInvalidate = true;
            }
            canvas.restoreToCount(restoreCount);
        }

        if (!mEdgeEffectRight.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(contentRect.right, contentRect.top);
            canvas.rotate(90, 0, 0);
            mEdgeEffectRight.setSize(contentRect.height(), contentRect.height());
            if (mEdgeEffectRight.draw(canvas)) {
                needsInvalidate = true;
            }
            canvas.restoreToCount(restoreCount);
        }

        if (needsInvalidate) {
            ViewCompat.postInvalidateOnAnimation(gestureClient.getView());
        }
    }


    protected void onDraw(Canvas canvas) {


        int clipRestoreCount = canvas.save();
        canvas.clipRect(gestureClient.getContentRect());

        drawEdgeEffectsUnclipped(canvas);

        // Removes clipping rectangle
        canvas.restoreToCount(clipRestoreCount);

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Methods and objects related to gesture handling
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    public boolean onTouchEvent(MotionEvent event) {
        //boolean retVal = mScaleGestureDetector.onTouchEvent(event);
        return mGestureDetector.onTouchEvent(event);
    }


    /**
     * The gesture listener, used for handling simple gestures such as double touches, scrolls,
     * and flings.
     */
    private final android.view.GestureDetector.SimpleOnGestureListener mGestureListener
            = new android.view.GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            releaseEdgeEffects();
            mScroller.forceFinished(true);
            ViewCompat.postInvalidateOnAnimation(gestureClient.getView());
            return true;
        }

        public static final float DIST_THRESHOLD = 50;

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Scrolling uses math based on the viewport (as opposed to math using pixels).
            /**
             * Pixel offset is the offset in screen pixels, while viewport offset is the
             * offset within the current viewport. For additional information on surface sizes
             * and pixel offsets, see the docs for {@link computeScrollSurfaceSize()}. For
             * additional information about the viewport, see the comments for
             * {@link mCurrentViewport}.
             */

            distanceX = Math.abs(distanceX) > DIST_THRESHOLD ? distanceX : 0;
            distanceY = Math.abs(distanceY) > DIST_THRESHOLD ? distanceY : 0;

            if (distanceX + currX < 0) {
                mEdgeEffectLeft.onPull(distanceX / (float) gestureClient.getScrollXRange());
                mEdgeEffectLeftActive = true;
            }

            if (distanceY + currY < 0) {
                mEdgeEffectTop.onPull(distanceY / (float) gestureClient.getScrollYRange());
                mEdgeEffectTopActive = true;
            }

            if (currX + distanceX > gestureClient.getScrollXRange()) {
                mEdgeEffectRight.onPull(distanceX / gestureClient.getScrollXRange());
                mEdgeEffectRightActive = true;
            }

            if (distanceY + currY > gestureClient.getScrollYRange()) { //textBounds.height() * cStr.length()) {
                mEdgeEffectBottom.onPull(distanceY / gestureClient.getScrollYRange());
                mEdgeEffectBottomActive = true;
            }


            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            fling((int) -velocityX, (int) -velocityY);
            return true;
        }
    };

    private void releaseEdgeEffects() {
        mEdgeEffectLeftActive
                = mEdgeEffectTopActive
                = mEdgeEffectRightActive
                = mEdgeEffectBottomActive
                = false;
        mEdgeEffectLeft.onRelease();
        mEdgeEffectTop.onRelease();
        mEdgeEffectRight.onRelease();
        mEdgeEffectBottom.onRelease();
    }


    private void fling(int velocityX, int velocityY) {

        releaseEdgeEffects();


        mScroller.forceFinished(true);
        mScroller.fling(
                currX,
                currY,
                velocityX,
                velocityY,
                0, gestureClient.getScrollXRange(),
                0, gestureClient.getScrollYRange());

        ViewCompat.postInvalidateOnAnimation(gestureClient.getView());
    }


    public void computeScroll() {


        if (mScroller.computeScrollOffset()) {
            // The scroller isn't finished, meaning a fling or programmatic pan operation is
            // currently active.

            currX = mScroller.getCurrX();
            currY = mScroller.getCurrY();

            if (currX < 0 && mEdgeEffectLeft.isFinished() && !mEdgeEffectLeftActive) {

                mEdgeEffectLeft.onAbsorb((int) getCurrVelocity(mScroller));
                mEdgeEffectLeftActive = true;

            } else if ( currX > gestureClient.getScrollXRange() && mEdgeEffectRight.isFinished() && !mEdgeEffectRightActive) {

                mEdgeEffectRight.onAbsorb((int) getCurrVelocity(mScroller));
                mEdgeEffectRightActive = true;
            }

            if (currY < 0 && mEdgeEffectTop.isFinished() && !mEdgeEffectTopActive) {

                mEdgeEffectTop.onAbsorb((int) getCurrVelocity(mScroller));
                mEdgeEffectTopActive = true;

            } else if (currY >= gestureClient.getScrollYRange() && mEdgeEffectBottom.isFinished() && !mEdgeEffectBottomActive) {

                mEdgeEffectBottom.onAbsorb((int) getCurrVelocity(mScroller));
                mEdgeEffectBottomActive = true;
            }

            ViewCompat.postInvalidateOnAnimation(gestureClient.getView());
        }


    }

    public int getCurrX() {
        return -currX;
    }

    public int getCurrY() {
        return -currY;
    }

    /**
     * @see android.view.ScaleGestureDetector#getCurrentSpanY()
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static float getCurrVelocity(OverScroller overScroller) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return overScroller.getCurrVelocity();
        } else {
            return 0;
        }
    }


    public boolean isFlinging() {
        return !mScroller.isFinished();
    }

    public class FlingEndChecker implements Runnable {

        public FlingEndChecker() {

        }


        @Override
        public void run() {

            long sleepTime = 100;

            boolean wasFlinging = false;

            while (true) {
                try {

                    Thread.sleep(sleepTime);

                    boolean nowFlinging = isFlinging();

                    if(wasFlinging && !nowFlinging) {
                        callFlingListeners();
                    }

                    wasFlinging = nowFlinging;

                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    public void addFlingListener(FlingListener flingListener) {
        flingListeners.add(flingListener);
    }

    private void callFlingListeners() {
        for(FlingListener flingListener: flingListeners) {
            flingListener.onFlingEnd(this);
        }
    }

}
