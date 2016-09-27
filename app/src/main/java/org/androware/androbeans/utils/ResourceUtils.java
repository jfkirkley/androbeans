package org.androware.androbeans.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by jkirkley on 6/21/16.
 */
public class ResourceUtils {

    public static Class R = null;
    public static Context context = null;

    public static int getResId(String groupName, String resName, Class resourceClass) {
        Class group = getResourceGroup(groupName, resourceClass);
        Integer idValue = (Integer)ReflectionUtils.getStaticDeclaredFieldValue(group, resName);
        return idValue.intValue();
    }

    public static int getResId(String groupName, String resName) {
        return getResId(groupName, resName, R);
    }

    public static InputStream getResourceInputStream(Activity activity, String resourceName, String groupName) {
        return activity.getResources().openRawResource(getResId(groupName, resourceName));
    }

    public static int getLayoutResId(String layoutId) {
        return getResId("layout", layoutId);
    }

    public static int getViewResId(String viewId) {
        return getResId("id", viewId);
    }

    public static Class getResourceGroup(String name, Class resourceClass) {
        if(R == null) {
            throw new Error("You must initialize R with the correct android resouce class.");
        }
        return ReflectionUtils.getInnerClass(resourceClass, name);
    }

    public static Class getResourceGroup(String name) {
        return getResourceGroup(name, R);
    }

    public static int getLayoutId( String resName ) {
        return getResId("layout", resName);
    }

    public static int getRawId( String resName ) {
        return getResId("raw", resName);
    }

    public static int getDrawableId( String resName ) {
        return getResId("drawable", resName);
    }

    public static int getAnimId( String resName ) {
        return getResId("anim", resName);
    }

    public static int getValuesId( String resName ) {
        return getResId("values", resName);
    }

    public static int getMenuId( String resName ) {
        return getResId("menu", resName);
    }

    public static int getLayoutLandId( String resName ) {
        return getResId("layout-land", resName);
    }

    public static Class getLayout() {
        return getResourceGroup("layout");
    }

    public static Class getRaw() {
        return getResourceGroup("raw");
    }

    public static Class getDrawable() {
        return getResourceGroup("drawable");
    }

    public static Class getAnim() {
        return getResourceGroup("anim");
    }

    public static Class getValues() {
        return getResourceGroup("values");
    }

    public static Class getMenu() {
        return getResourceGroup("menu");
    }

    public static Class getLayoutLand() {
        return getResourceGroup("layout-land");
    }


    public static int getResourceIdFromDotName(String dotName) {
        String tks [] = dotName.split("\\.");

        if( tks.length == 3 && tks[0].equals("R") ) {

            return getResId(tks[1], tks[2]);

        } else if(tks.length == 4 && tks[0].equals("android") && tks[1].equals("R") ) {

            return getResId(tks[2], tks[3], android.R.class);

        }
        throw new IllegalArgumentException("Dotname '" + dotName + "' is not a valid resource path.");
    }


    public static Map<Integer, Object> resourceCache = new HashMap<>();

    public static Object getCachedResource(int id) {
        return resourceCache.get(id);
    }

    // BAD: not actually a good idea because these stay in ram and are not released
    public static Drawable getDrawableResource(String name) {
        int id = getResId("drawable", name);
        Object cachedObj = getCachedResource(id);
        if(cachedObj == null) {
            cachedObj = ContextCompat.getDrawable(context, id);
            resourceCache.put(id, cachedObj);
        }
        return (Drawable)cachedObj;
    }


}
