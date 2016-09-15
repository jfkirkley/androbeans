package org.androware.androbeans.utils;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;

import android.widget.ArrayAdapter;

import android.widget.ListView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.R.attr.name;
import static android.R.attr.path;
import static android.R.attr.type;
import static android.R.attr.value;

/**
 * Created by jkirkley on 1/19/16.
 */
public class Utils {

    public static Object R;

    public static void showAlert(String msg, String title, Context context) {
        AlertDialog a = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

    /*
        public static String toString(<T> T[] array) {
            StringBuffer buf = new StringBuffer();
            String comma = "";
            for(T bean: a) {
                buf.append(comma + ": " + bean);
                comma = ", ";
            }
            buf.append("]\n");
            return buf.toString();
        }
    */


    public static boolean deviceIsOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        }
        return ni.isConnected();
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static void clearAndReloadAdaptor(ArrayAdapter adapter, String[] newData) {
        adapter.clear();
        adapter.addAll(new ArrayList<String>(Arrays.asList(newData)));
        adapter.notifyDataSetChanged();
    }

    public static void clearAndReloadAdaptor(ArrayAdapter adapter, Collection newData) {
        adapter.clear();
        adapter.addAll(new ArrayList<String>(newData));
        adapter.notifyDataSetChanged();
    }

    public static void setLayoutHeight(ViewGroup viewGroup, int height) {
        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) viewGroup.getLayoutParams();
        params.height = height;
        viewGroup.setLayoutParams(params);
    }

    public static View findView(Activity activity, String viewId) {
        return activity.findViewById(ResourceUtils.getViewResId(viewId));
    }

    public static void setLayout(Activity activity, String layoutId) {
        activity.setContentView(ResourceUtils.getLayoutResId(layoutId));
    }

    public static View inflateView(String layoutId, LayoutInflater inflater, ViewGroup container) {
        // Inflate the layout for this fragment
        return inflater.inflate(ResourceUtils.getLayoutResId(layoutId), container, false);
    }

    public static ListView getAndSetListView(String[] listItems, String name, Activity activity) {
        return getAndSetListView(listItems, name, activity, android.R.layout.simple_list_item_1);
    }

    public static ListView getAndSetListView(String[] listItems, String name, Activity activity, int listItemLayoutId) {
        ListView listView = (ListView) activity.findViewById(ResourceUtils.getResId("id", name));

        listView.setAdapter(new ArrayAdapter<String>(activity, listItemLayoutId, listItems));

        return listView;
    }

    public static File getInternalFile(Activity activity, String path, String fileName) {
        return new File(activity.getFilesDir(), path + fileName);
    }

    public static FileOutputStream getExternalFileOutputStream(Activity activity, String type, String path, String fileName) throws IOException {
        File file = getExternalFile(activity, type, path, fileName);
        return new FileOutputStream(file);
    }

    public static File getExternalFile(Activity activity, String type, String path, String fileName) {
        return new File(activity.getExternalFilesDir(type), path + fileName);
    }

    public static boolean fileExists(String absolutePath) {
        return new File(absolutePath).exists();
    }

    public static boolean externalFileExists(Activity activity, String type, String path, String fileName) {
        return getExternalFile(activity, type, path, fileName).exists();
    }

    public static void deleteFiles(File dir, final String ext) {

        FilenameFilter ff;
        ff = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(ext);
            }
        };
        for (File f : dir.listFiles(ff)) {
            Log.d("util", "delete: " + f.getAbsolutePath());
            f.delete();
        }
    }

    public static List<File> getFiles(Activity activity, String dirName, String suffix) {
        return getFiles(activity.getExternalFilesDir(dirName), suffix);
    }

    public static List<File> getFiles(File dir, final String suffix) {

        FilenameFilter ff;
        ff = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(suffix);
            }
        };
        List<File> files = new ArrayList<>();
        for (File f : dir.listFiles(ff)) {
            files.add(f);
        }
        return files;
    }

    public static String normalizeStringToFilePath(String s) {
        // test code:
        //         String s = Utils.normalizeStringToFilePath("s{}!@#$abc%^&*()xyz+");

        return s.replaceAll("[\\s{}\\?\\.\\,\\:\\;!@#\\$%\\^&*\\-\\(\\)\\+\\]\\[]+", "_");
    }

    public static String removePunctuation(String s, String replacement) {
        return s.replaceAll("[<>{}\\?\\.\\,\\:\\;!@#\\$%\\^&*\\(\\)\\+\\]\\[]+", replacement);
    }

    public static String removePunctuation(String s) {
        // test code:
        //         String s = Utils.normalizeStringToFilePath("s{}!@#$abc%^&*()xyz+");
        return removePunctuation(s, " ");
    }

    public static void displayFiles(AssetManager mgr, String path, int level, ArrayList<String> paths) {

        paths.add(path);
        try {
            String list[] = mgr.list(path);

            if (list != null)
                for (int i = 0; i < list.length; ++i) {
                    if (level >= 1) {
                        displayFiles(mgr, path + "/" + list[i], level + 1, paths);
                    } else {
                        displayFiles(mgr, list[i], level + 1, paths);
                    }
                }
        } catch (IOException e) {

        }
    }


    public static void startActivity(Class activityClass, HashMap<String, String> extras, HashMap<String, Parcelable> parcelableHashMap, Activity activity) {
        Intent intent = new Intent(activity, activityClass);
        for (String k : extras.keySet()) {
            intent.putExtra(k, extras.get(k));
        }
        for (String k : parcelableHashMap.keySet()) {
            intent.putExtra(k, parcelableHashMap.get(k));
        }
        activity.startActivity(intent);
    }

    public static void raiseToForegroundActivity(Class activityClass, Activity activity) {
        Intent openMainActivity = new Intent(activity, activityClass);
        openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(openMainActivity);
    }

    public static void startActivity(Class activityClass, HashMap extras, Activity activity) {
        Intent intent = new Intent(activity, activityClass);
        for (Object key : extras.keySet()) {
            Object v = extras.get(key);
            String k = (String) key;
            //Log.d("util", k + " -> " + v);
            Class type = v.getClass();
            if (Parcelable.class.isAssignableFrom(type)) {
                intent.putExtra(k, (Parcelable) v);
            } else if (Serializable.class.isAssignableFrom(type)) {
                intent.putExtra(k, (Serializable) v);
            } else if (Integer.class.isAssignableFrom(type)) {
                //Log.d("util", k + " --------------------- -> " + v);
                intent.putExtra(k, (int) v);
            } else if (Long.class.isAssignableFrom(type)) {
                intent.putExtra(k, (long) v);
            } else if (Double.class.isAssignableFrom(type)) {
                intent.putExtra(k, (double) v);
            } else if (Boolean.class.isAssignableFrom(type)) {
                intent.putExtra(k, (boolean) v);
            } else if (String.class.isAssignableFrom(type)) {
                intent.putExtra(k, (String) v);
            }

        }
        activity.startActivity(intent);
    }

    public static void startActivity(HashMap<String, String> extras, Class activityClass, Activity activity) {
        Intent intent = new Intent(activity, activityClass);
        for (String k : extras.keySet()) {
            String v = extras.get(k);
            intent.putExtra(k, v);
        }
        activity.startActivity(intent);
    }

    /*
        public static void startActivity(HashMap extras, Class activityClass, AppCompatActivity activity) {

            Intent intent = new Intent(activity, activityClass);
            for(String k: extras.keySet()) {
                intent.putExtra(k, extras.get(k));
            }
            activity.startActivity(intent);
        }
    */


    public static Set makeSet(Object... members) {
        HashSet hashSet = new HashSet();
        for (Object obect : members) {
            hashSet.add(obect);
        }
        return hashSet;
    }

    public static boolean isPrimitiveOrString(Object object) {
        if (object != null) {
            Class cls = object.getClass();
            return cls.isPrimitive() || cls == String.class || cls == Integer.class ||
                    cls == Boolean.class || cls == Long.class || cls == Character.class ||
                    cls == Float.class || cls == Double.class;
        }
        return false;
    }


    public static String getExtraString(Bundle extras, String name) {

        if (extras != null) {
            return extras.getString(name);
        }
        return null;
    }


    public static int getResIdFromExtra(Intent intent, String name, String groupName) {
        Bundle extras = intent.getExtras();
        return ResourceUtils.getResId(groupName, getExtraString(extras, name));
    }

    public static String getStringFromExtra(Intent intent, String name) {
        Bundle extras = intent.getExtras();
        return getExtraString(extras, name);
    }

    public static void addSwipeListener(final View view) {

        view.setOnTouchListener(new View.OnTouchListener() {
            float y1 = 0;
            float y2 = 0;
            float lasty2 = 0;
            final float MIN_DISTANCE = 40;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        y1 = event.getY();
                        lasty2 = y1;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        y2 = event.getY();

                            /*
                            TranslateAnimation anim = new TranslateAnimation(0, 0, 0, y2-lasty2); //first 0 is start point, 150 is end point horizontal
                            anim.setDuration(100); // 1000 ms = 1second
                            anim.setFillAfter(true);
                            testSwipeButton.startAnimation(anim); // your imageview that you want to give the animation. call this when you want it to take effect
                            lasty2 = y2;
                            */


                        //Log.d(TAG, "diffy: " + (y2 - lasty2));
                        lasty2 = y2;
                        break;
                    case MotionEvent.ACTION_UP:
                        y2 = event.getY();
                        float deltay = y1 - y2;
                        if (Math.abs(deltay) > MIN_DISTANCE) {
                            TranslateAnimation anim = new TranslateAnimation(0, 0, 0, -deltay); //first 0 is start point, 150 is end point horizontal
                            anim.setDuration(100); // 1000 ms = 1second
                            anim.setFillAfter(true);
                            view.startAnimation(anim); // your imageview that you want to give the animation. call this when you want it to take effect
                            lasty2 = y2;
                            Toast.makeText(v.getContext(), "up swipe swipe", Toast.LENGTH_SHORT).show();
                        } else {
                            // consider as something else - a screen tap for example
                        }

                        break;
                }
                return true;
            }
        });

    }

    public static String upCaseFirstLetter(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static <K, C> void addToContainerValue(Map<K, C> map, K key, Object value) {
        if (!map.containsKey(key)) {
            //  TODO can this be done??? C container = ReflectionUtils.newInstance(C);
        }
    }

    public static void addValueToMappedContainer(Field mapField, Map map, Object key, Object value) {
        if (!map.containsKey(key)) {
            Class containerType = ReflectionUtils.getGenericType(mapField, 1);
            if (Collection.class.isAssignableFrom(containerType)) {
                Collection collection = (Collection) ReflectionUtils.newInstance(containerType);
                collection.add(value);
                map.put(key, collection);
            }
        }
    }

    public static boolean oneOfTheseFilesIsBeingUsed(List<String> fileNames) {
        for (String fname : fileNames) {
            if (Utils.filesIsBeingUsed(fname)) {
                Log.d("fileUtil", fname + " is being used");
                return true;
            }
        }
        Log.d("fileUtil", "No files used !!!!!!!!!!!!!!!!!!!!!!!!!!");
        return false;
    }

    public static boolean filesIsBeingUsed(String filename) {

        boolean isLocked = false;
        RandomAccessFile fos = null;
        try {
            File file = new File(filename);
            if (file.exists()) {
                Log.d("fileUtil", filename + " exists");
                fos = new RandomAccessFile(file, "rw");
            } else {
                Log.d("fileUtil", filename + " does not exist");
            }
        } catch (FileNotFoundException e) {
            Log.d("fileUtil", filename + " not found");
            isLocked = true;
        } catch (SecurityException e) {
            Log.d("fileUtil", filename + " got security exception");
            isLocked = true;
        } catch (Exception e) {
            Log.d("fileUtil", filename + " got exception: " + e.getMessage());
            isLocked = true;
            // handle exception
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                Log.d("fileUtil", filename + " now closed: " + isLocked);

            } catch (Exception e) {
                //handle exception
                Log.d("fileUtil", filename + " got exception: " + e.getMessage());
                isLocked = true;
            }
        }
        Log.d("fileUtil", filename + " locked: " + isLocked);
        return isLocked;
    }
}