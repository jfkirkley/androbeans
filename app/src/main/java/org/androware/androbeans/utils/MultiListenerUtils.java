package org.androware.androbeans.utils;

import android.view.View;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jkirkley on 8/7/16.
 *
 * Unfortunately android widgets only support one listener per event.   A design flaw that this code seeks to remedy.
 */


public class MultiListenerUtils {

    public static class MultiOnClickListener implements View.OnClickListener {
        private List<View.OnClickListener> listeners;

        public MultiOnClickListener(View.OnClickListener ... listeners) {
            this.listeners = new ArrayList<>();

            for(View.OnClickListener listener: listeners) {
                this.add(listener);
            }
        }

        @Override
        public void onClick(View v) {
            for( View.OnClickListener listener: listeners) {
                listener.onClick(v);
            }
        }

        public void addAt(View.OnClickListener listener, int position) {
            listeners.add(position, listener);
        }

        public void add(View.OnClickListener listener) {
            listeners.add(listener);
        }

        public boolean remove(View.OnClickListener listener) {
            return listeners.remove(listener);
        }

        // allow access to listeners in order to facilitate ordering etc.
        public List<View.OnClickListener> getListeners() {
            return listeners;
        }

        public void setListeners(List<View.OnClickListener> listeners) {
            this.listeners = listeners;
        }

        public static void setListener(View view, View.OnClickListener listener) {
            setListener(view, listener, -1);
        }

        public static void setListener(View view, View.OnClickListener listener, int position) {
            View.OnClickListener onClickListener = (View.OnClickListener) getListener(view, "mOnClickListener");
            if(onClickListener == null) {
                view.setOnClickListener(new MultiOnClickListener(listener));
            } else if( onClickListener instanceof MultiOnClickListener) {
                if(position >= 0) {
                    ((MultiOnClickListener) onClickListener).addAt(listener, position);
                } else {
                    ((MultiOnClickListener) onClickListener).add(listener);
                }
            } else {
                throw new IllegalArgumentException("View already has listener that is not a multi listener.");
            }
        }
    }


    public static Object getListener(View v, String listenerType) {

        try {
            Field listenerInfoField = null;
            listenerInfoField = Class.forName("android.view.View").getDeclaredField("mListenerInfo");

            if (listenerInfoField != null) {
                listenerInfoField.setAccessible(true);
            }
            Object myLiObject = listenerInfoField.get(v);

            Field listenerField = null;
            listenerField = Class.forName("android.view.View$ListenerInfo").getDeclaredField(listenerType);//"mOnClickListener");
            if (listenerField != null && myLiObject != null) {
                listenerField.setAccessible(true);
                return listenerField.get(myLiObject);
            }

        } catch (NoSuchFieldException e) {
        } catch (ClassNotFoundException e) {
        } catch (IllegalAccessException e) {
        }
        return null;
    }
}
