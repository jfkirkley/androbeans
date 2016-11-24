package org.androware.androbeans.styles;

import android.graphics.Paint;

import java.util.Map;

/**
 * Created by jkirkley on 11/15/16.
 */

public class StylesBean {
    public Map<String, PaintBean> paintMap;

    public StylesBean() {}

    public PaintBean getPaintBean(String name) {
        return paintMap.get(name);
    }

    public void setPaintBean(String name, PaintBean paintBean) {
        paintMap.put(name, paintBean);
    }

    public void addPaintBean(String name, int color) {
        setPaintBean(name, new PaintBean(color));
    }

    public void addPaintBean(String name, int color, int fontSize ) {
        setPaintBean(name, new PaintBean(color, fontSize));
    }

    public void addPaintBean(String name, int color, int highLightColor, int fontSize ) {
        setPaintBean(name, new PaintBean(color, highLightColor, fontSize));
    }

    public void addPaintBean(String name, int color, int fontSize, String style ) {
        setPaintBean(name, new PaintBean(color, fontSize, style));
    }

    public void addPaintBean(String name, int color, int fontSize, String style, int strokeWidth) {
        setPaintBean(name, new PaintBean(color, -1, fontSize, style, strokeWidth));
    }

    public void addPaintBean(String name, int color, int highLightColor, int fontSize, String style, int strokeWidth) {
        setPaintBean(name, new PaintBean(color, highLightColor, fontSize, style, strokeWidth));
    }

    public Paint getPaint(String name) {
        PaintBean paintBean = paintMap.get(name);
        if(paintBean != null) {
            return paintBean.buildPaint();
        }
        throw new IllegalArgumentException("No style paint with name: " + name);
    }
}
