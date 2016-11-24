package org.androware.androbeans.styles;

import android.graphics.Paint;
import android.graphics.Rect;

import static android.R.attr.label;


/**
 * Created by jkirkley on 11/15/16.
 */

public class PaintBean {


    public final static String FILL = "FILL";
    public final static String FILL_AND_STROKE = "FILL_AND_STROKE";
    public final static String STROKE = "STROKE";

    public long color = Long.MAX_VALUE;   // N0TE: any int is a color, so we use longs so we can have Long.MAX_VALUE as our null value
    public long hightLightColor = Long.MAX_VALUE;
    public int fontSize = -1;
    public String style = null;
    public int strokeWidth = -1;

    public Rect getFontBounds(String text) {
        return getFontBounds(text, -1);
    }

    public Rect getFontBounds(String text, int textSize) {
        buildPaint();
        Rect textBounds = new Rect();
        if(textSize != -1) {
            paint.setTextSize(textSize);
        }
        paint.getTextBounds(text, 0, text.length(), textBounds);
        if(fontSize != -1) {
            paint.setTextSize(fontSize);
        }
        return textBounds;
    }

    public Paint getPaint() {
        buildPaint();
        return paint;
    }

    Paint paint = null;

    public PaintBean() {
    }

    public PaintBean(int color) {
        this(color, -1, -1, null, -1);
    }

    public PaintBean(int color,
                     int fontSize) {
        this(color, -1, fontSize, null, -1);
    }

    public PaintBean(int color,
                     int hightLightColor,
                     int fontSize) {
        this(color, hightLightColor, fontSize, null, -1);
    }

    public PaintBean(int color,
                     int fontSize,
                         String style) {
        this(color, -1, fontSize, style, -1);
    }

    public PaintBean(int color,
                     int hightLightColor,
                     int fontSize,
                     String style,
                     int strokeWidth) {
        this.color = color;
        this.hightLightColor = hightLightColor;
        this.style = style;
        this.strokeWidth = strokeWidth;
        this.fontSize = fontSize;

        buildPaint();
    }

    public Paint buildPaint() {
        if (paint == null) {
            paint = new Paint();
            if (color != Long.MAX_VALUE) {
                paint.setColor((int)color);
            }
            if (fontSize != -1) {
                paint.setTextSize(fontSize);
            }
            if (style != null) {
                paint.setStyle(Paint.Style.valueOf(style));
            }
            if (strokeWidth != -1) {
                paint.setStrokeWidth(strokeWidth);
            }
        }
        return paint;
    }

    public void setHightLightColor() {
        if(hightLightColor == Long.MAX_VALUE) {
            throw new IllegalArgumentException("no highlight color set!");
        }
        paint.setColor((int)hightLightColor);
    }

    public void setNormalColor() {
        buildPaint();
        paint.setColor((int)color);
    }

    public Paint clonePaint() {
        buildPaint();
        return new Paint(paint);
    }

    public String toString() {
        return "color: " + color + ", hlc: " + hightLightColor + ", style: " + style + ", sw: " + strokeWidth + ", fs: " + fontSize;
    }

}
