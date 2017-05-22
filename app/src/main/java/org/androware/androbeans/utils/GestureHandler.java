package org.androware.androbeans.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.EdgeEffectCompat;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.OverScroller;

import java.util.ArrayList;
import java.util.List;

import static android.util.Log.d;


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

    private static final float TOLERANCE = 0.05f;

    private boolean isFlinging = false;

    private int currX = 0;
    private int currY = 0;
    private int startX = 0;
    private int startY = 0;

    private int dX = 0;
    private int dY = 0;


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

    private Handler uiThreadHandler;

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



        uiThreadHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                callFlingListeners();
                resetDxDy();
            }
        };
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
        final int action = MotionEventCompat.getActionMasked(event);

        if( action == MotionEvent.ACTION_DOWN ) {
            startX = currX;
            startY = currY;

            resetDxDy();
        }
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

    public final static int VELOCITY_THRESHOLD = 2000;
    public final static int DIFFXY_THRESHOLD = 100;

    public void setIgnoreVelocityThreshold(boolean ignoreVelocityThreshold) {
        this.ignoreVelocityThreshold = ignoreVelocityThreshold;
    }

    public boolean ignoreVelocityThreshold = false;


    private void fling(int velocityX, int velocityY) {

        float diffy = SwipeDetector.inst().getDiffY();
        float diffx = SwipeDetector.inst().getDiffX();

/*
        if(Math.abs(diffx) < DIFFXY_THRESHOLD && Math.abs(diffy)  < DIFFXY_THRESHOLD) {
            return;   // this stops weak touch movements from causing flings.
        }
*/

        if(!ignoreVelocityThreshold && Math.abs(velocityX) < VELOCITY_THRESHOLD && Math.abs(velocityY)  < VELOCITY_THRESHOLD) {
            return;   // this stops weak touch movements from causing flings.
        }


        releaseEdgeEffects();


        diffX = diffY = 0;

        mScroller.forceFinished(true);

        mScroller.fling(
                currX,
                currY,
                velocityX,
                velocityY,
                0, gestureClient.getScrollXRange(),
                0, gestureClient.getScrollYRange());

/*
        Log.d("g", "fling: " + mScroller.getFinalX() + " , " + mScroller.getFinalY() + " :: " +
                currX + ", " +
                currY + ", " +
                velocityX + ", " +
                velocityY + ", " +
                0 + ", " + gestureClient.getScrollXRange() + ", " +
                0 + ", " + gestureClient.getScrollYRange());
*/

        //SwipeDetector.l( diffx + " <> flingX: " + mScroller.getFinalX() + " , " + currX + ", " + velocityX + ", " + 0 + ", " + gestureClient.getScrollXRange() + " >> " + ignoreVelocityThreshold);
        //SwipeDetector.l( diffy + " <> flingY: " + mScroller.getFinalY() + " , " + currY + ", " + velocityY + ", " + 0 + ", " + gestureClient.getScrollYRange() );

//*/
        xTolerance = Math.abs(mScroller.getFinalX() - currX)*TOLERANCE;
        yTolerance = Math.abs(mScroller.getFinalY() - currY)*TOLERANCE;


        ViewCompat.postInvalidateOnAnimation(gestureClient.getView());
    }

    private float xTolerance = 0;
    private float yTolerance = 0;


    public void resetDxDy() {
        diffX = diffY = dX = dY = 0;
    }

    public void resetCurrXY() {
        resetCurrXY(0, 0);
    }

    public void resetCurrXY(int x, int y) {
        currX = x;
        currY = y;
    }

    public int getDiffX() {
        return -diffX;
    }

    public int getDiffY() {
        return -diffY;
    }

    int diffX;
    int diffY;

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void computeScroll() {


        if (mScroller.computeScrollOffset()) {

            // The scroller isn't finished, meaning a fling or programmatic pan operation is
            // currently active.

            int cx = mScroller.getCurrX();
            int cy = mScroller.getCurrY();

            int finalX = mScroller.getFinalX();
            int finalY = mScroller.getFinalY();

            float fx = Math.abs(finalX - cx);
            float fy = Math.abs(finalY - cy);

            //Log.d("g", xTolerance + ", " + yTolerance + " :<>;; " + fx + ", " + fy  + " :: " + currX + ", " + currY);//+ " : " + (Math.abs(fx) - Math.abs(currX)) + ", " + (Math.abs(fy) - Math.abs(currY)));

            boolean logIt = false;
            String log = "";


            if(fx <= xTolerance && fy <= yTolerance) {
                diffX = finalX - currX;
                diffY = finalY - currY;

                currX = finalX;
                currY = finalY;

                Message completeMessage = uiThreadHandler.obtainMessage(0, null);
                completeMessage.sendToTarget();

                mScroller.forceFinished(true);

                if(logIt) {
                    log = ("last cxcy: " + cx + " , " + cy + " :: " + currX + ", " + currY + " >> " + diffX + ", " + diffY + " || " + mScroller.isFinished() + " , " + isFlinging);
                }

                ignoreVelocityThreshold = false;
            } else {

                diffX = cx - currX;
                diffY = cy - currY;

                currX = cx;
                currY = cy;

                if(logIt) {
                    log = ("cxcy: " + cx + " , " + cy + " :: " + currX + ", " + currY + " >> " + diffX + ", " + diffY + " || " + mScroller.isFinished() + " , " + isFlinging);
                }

            }

            dX = currX - startX;
            dY = currY - startY;

            if(logIt) {
                SwipeDetector.l(dX + ", " + dY + " " + log);
            }

            doScroll();
        }

    }

    public void doScroll(){


        if (currX < 0 && mEdgeEffectLeft.isFinished() && !mEdgeEffectLeftActive) {

            mEdgeEffectLeft.onAbsorb((int) getCurrVelocity(mScroller));
            mEdgeEffectLeftActive = true;

        } else if (currX > gestureClient.getScrollXRange() && mEdgeEffectRight.isFinished() && !mEdgeEffectRightActive) {

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
        boolean isFinished = mScroller.isFinished();
        //SwipeDetector.l(mScroller.isFinished() + " , " + isFlinging);
        boolean retval = isFlinging || !isFinished;
        isFlinging = !isFinished;
        return retval;
    }

    public void addFlingListener(FlingListener flingListener) {
        flingListeners.add(flingListener);
    }

    private void callFlingListeners() {
        for (FlingListener flingListener : flingListeners) {
            flingListener.onFlingEnd(this);
        }
    }

    public int getdX() {
        return -dX;
    }

    public int getdY() {
        return -dY;
    }

    public void setCurrXandStartX(int currX) {
        setCurrX(currX);
        startX = currX;
    }

    public void setCurrYandStartY(int currY) {
        setCurrY(currY);
        startY = currY;
    }

    public void setCurrX(int currX) {
        if(currX < 0) {
            this.currX = 0;
        } else if(currX > gestureClient.getScrollXRange()) {
            this.currX = gestureClient.getScrollXRange();
        } else {
            this.currX = currX;
        }
    }

    public void setCurrY(int currY) {
        if(currY < 0) {
            this.currY = 0;
        } else if(currY > gestureClient.getScrollYRange()) {
            this.currY = gestureClient.getScrollYRange();
        } else {
            this.currY = currY;
        }
    }

    public void setdX(int dX) {
        this.dX = dX;
    }

    public void setdY(int dY) {
        this.dY = dY;
    }



}
