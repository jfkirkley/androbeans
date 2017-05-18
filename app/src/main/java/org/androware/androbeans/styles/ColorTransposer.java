package org.androware.androbeans.styles;

import android.graphics.Paint;
import android.util.Log;

/**
 * Created by jkirkley on 5/12/17.
 */

public class ColorTransposer {

    private int startColor;
    private int endColor;

    private int numSteps;
    private int currStep = 0;

    public ColorTransposer(Paint startPaint, Paint endPaint, int numSteps) {
        this(startPaint.getColor(), endPaint.getColor(), numSteps);
    }

    public ColorTransposer(int startColor, int endColor, int numSteps) {
        this.startColor = startColor;
        this.endColor = endColor;
        this.numSteps = numSteps;


    }

    public String toString() { return "0x" + Integer.toHexString(startColor) + " - 0x" + Integer.toHexString(endColor) + " : " + currStep + " -> " + numSteps;}

    public int nextColorOrig() {

        int startAlpha = startColor & 0xff000000;
        int startRed = startColor & 0x00ff0000;
        int startGreen = startColor & 0x0000ff00;
        int startBlue = startColor & 0x000000ff;

        int endAlpha = endColor & 0xff000000;
        int endRed = endColor & 0x00ff0000;
        int endGreen = endColor & 0x0000ff00;
        int endBlue = endColor & 0x000000ff;

        ++currStep;

        int incAlpha = currStep * (endAlpha - startAlpha) / numSteps;
        int incRed = currStep * (endRed - startRed) / numSteps;
        int incGreen = currStep * (endGreen - startGreen) / numSteps;
        int incBlue = currStep * (endBlue - startBlue) / numSteps;

        return (startAlpha + incAlpha) | (startRed + incRed) | (startGreen + incGreen) | (startBlue + incBlue);
    }

    public int nextColor() {

        int startAlpha = startColor & 0xff;
        int startRGB = startColor & 0x00ffffff;

        ++currStep;
        int incAlpha = currStep * -(0xff / numSteps);

        incAlpha <<= 24;

        int nextColor = (startAlpha + incAlpha) | startRGB;
        //Log.d("wheel", currStep + " : " + Integer.toHexString(incAlpha) + ", " +Integer.toHexString(nextColor));

        return nextColor;
    }

    public int nextColorFull() {

        ++currStep;

        int inc = currStep * (endColor - startColor) / numSteps;

        return startColor + inc;
    }

    public boolean done() {
        return currStep >= numSteps;
    }

    public Paint getNextPaint() {
        Paint paint = new Paint();
        paint.setColor(nextColor());
        return paint;
    }

    public Paint getNextPaintFull() {
        Paint paint = new Paint();
        paint.setColor(nextColorFull());
        return paint;
    }

}


