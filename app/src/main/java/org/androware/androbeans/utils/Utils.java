package org.androware.androbeans.utils;

import android.content.ContextWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Debug;
import android.os.Environment;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    public static void l(String t) {
        FilterLog.inst().log("utils", t);
    }


    public static void startMethodTracing(ContextWrapper contextWrapper, String logName) {
        String fname = Utils.getExternalFile(contextWrapper, null, null, logName).getAbsolutePath();
        Debug.startMethodTracing(fname);
    }


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

    public static int getInverseColor(int color) {
        return (0x00FFFFFF - (color | 0xFF000000)) | (color & 0xFF000000);
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

    public static FileOutputStream getExternalFileOutputStream(ContextWrapper contextWrapper, String type, String path, String fileName) throws IOException {
        File file = getExternalFile(contextWrapper, type, path, fileName);
        return new FileOutputStream(file);
    }

    public static FileOutputStream getInternalFileOutputStream(ContextWrapper contextWrapper, String fileName) throws IOException {
        File file = getInternalFile(contextWrapper, fileName);
        return new FileOutputStream(file);
    }

    public static File getExternalFile(ContextWrapper contextWrapper, String type, String path, String fileName) {
        String fullPath = path != null ? path + fileName : fileName;
        return new File(contextWrapper.getExternalFilesDir(type), fullPath);
    }

    /*
        public static void copyAssetsToExternal(AssetManager assetManager, String subDir) {

            String[] files = null;
            subDir = subDir == null ? "" : subDir;

            try {
                files = assetManager.list(subDir);
            } catch (IOException e) {
                l("tag", "Failed to get asset file list.", e);
            }

            String extDir = Environment.getExternalStorageDirectory().getAbsolutePath();

            for (String filename : files) {
                InputStream in = null;
                OutputStream out = null;
                try {
                    String extPath = (subDir.length() > 0) ? extDir + "/" + subDir : extDir;
                    File outFile = new File(extPath, filename);

                    String assetPath = (subDir.length() > 0) ? "/" + subDir + "/" + filename : filename;

                    l( "copy " + assetPath + " to " + outFile.getAbsolutePath());

                    in = assetManager.open(assetPath);
                    out = new FileOutputStream(outFile);

                    copyFile(in, out);

                    in.close();
                    out.close();

                } catch (IOException e) {
                    l("tag", "Failed to copy asset file: " + filename, e);
                }
            }
        }
    */

    public static void copyAssetsToExternal(AssetManager assetManager, String extDir, Set<String> excludeFiles) {

        String[] files = null;

        try {
            files = assetManager.list("");
        } catch (IOException e) {
            l("Failed to get asset file list.");
        }

        //String extDir = Environment.getExternalStorageDirectory().getAbsolutePath();

        for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            if (!filename.startsWith("images") && !filename.startsWith("sounds") && !filename.startsWith("webkit") && !excludeFiles.contains(filename)) {
                try {
                    String extPath = extDir;
                    File outFile = new File(extPath, filename);

                    String assetPath = filename;

                    l("copy " + assetPath + " to " + outFile.getAbsolutePath());

                    in = assetManager.open(assetPath);
                    out = new FileOutputStream(outFile);

                    copyFile(in, out);

                    in.close();
                    out.flush();
                    out.close();

                } catch (IOException e) {
                    l("Failed to copy asset file: " + filename);
                }
            }
        }
    }

    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public static String getExternalDirPath(ContextWrapper contextWrapper, String type) {
        String state = Environment.getExternalStorageState();
        String extPath = "";
        if (false && Environment.MEDIA_MOUNTED.equals(state)) {
            File baseDirFile = contextWrapper.getExternalFilesDir(type);
            if (baseDirFile == null) {
                extPath = contextWrapper.getFilesDir().getAbsolutePath();
            } else {
                extPath = baseDirFile.getAbsolutePath();
            }
        } else {
            extPath = contextWrapper.getFilesDir().getAbsolutePath();
        }
        return extPath;
    }


    public static StringBuffer externalFile2stringBuffer(ContextWrapper contextWrapper, String type, String path, String fileName) {
        return file2stringBuffer(getExternalFile(contextWrapper, type, path, fileName));
    }

    public static StringBuffer file2stringBuffer(String path) {
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(path));
            return file2stringBuffer(fileInputStream);
        } catch (IOException e) {
        }
        return null;
    }

    public static StringBuffer file2stringBuffer(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            return file2stringBuffer(fileInputStream);
        } catch (IOException e) {
        }
        return null;
    }

    public static StringBuffer file2stringBuffer(InputStream in) throws IOException {
        byte[] buffer = new byte[1024];
        StringBuffer stringBuffer = new StringBuffer(1024);
        int read;
        while ((read = in.read(buffer)) != -1) {
            stringBuffer.append(new String(buffer, 0, read));
        }
        in.close();
        return stringBuffer;
    }

    public static void string2externalFile(ContextWrapper contextWrapper, String type, String path, String fileName, String string) {
        string2file(getExternalFile(contextWrapper, type, path, fileName), string);
    }

    public static void string2file(String path, String string) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(path));
            string2file(fileOutputStream, string);
        } catch (IOException e) {
        }
    }

    public static void string2file(File file, String string) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            string2file(fileOutputStream, string);
        } catch (IOException e) {
        }
    }

    public static void string2file(OutputStream out, String string) throws IOException {
        out.write(string.getBytes());
        out.close();
    }

    public static boolean fileExists(String absolutePath) {
        return new File(absolutePath).exists();
    }

    public static long fileSize(String absolutePath) {
        return new File(absolutePath).length();
    }

    public static boolean externalFileExists(ContextWrapper contextWrapper, String type, String path, String fileName) {
        return getExternalFile(contextWrapper, type, path, fileName).exists();
    }

    public static boolean internalFileExists(ContextWrapper contextWrapper, String path) {
        return fileExists(getInternalFilePath(contextWrapper, path));
    }

    public static File getInternalFile(ContextWrapper contextWrapper, String path) {
        return new File(getInternalFilePath(contextWrapper, path));
    }

    public static String getInternalFilePath(ContextWrapper contextWrapper, String path) {
        String intPath = contextWrapper.getFilesDir().getAbsolutePath();
        if (!intPath.endsWith("/") && !path.startsWith("/")) {
            intPath += "/";
        }
        if (intPath.endsWith("/") && path.startsWith("/")) {
            path = path.substring(1);
        }
        return intPath + path;
    }

    public static void deleteFiles(File dir, final String ext) {

        FilenameFilter ff;
        ff = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(ext);
            }
        };
        for (File f : dir.listFiles(ff)) {
            l("delete: " + f.getAbsolutePath());
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

    public static int[] makeRandArray(int size) {

        int randIndexMap[] = new int[size];

        for (int c = 0; c < size; ++c) {

            while (true) {
                int rand = (int) Math.round(Math.random() * size);
                int i = 0;
                for (; i < randIndexMap.length; ++i) {
                    if (rand == randIndexMap[i]) break;
                }
                if (i == randIndexMap.length) {
                    randIndexMap[c] = rand;
                    break;
                }
            }
        }

        return randIndexMap;
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
            //l( k + " -> " + v);
            Class type = v.getClass();
            if (Parcelable.class.isAssignableFrom(type)) {
                intent.putExtra(k, (Parcelable) v);
            } else if (Serializable.class.isAssignableFrom(type)) {
                intent.putExtra(k, (Serializable) v);
            } else if (Integer.class.isAssignableFrom(type)) {
                //l( k + " --------------------- -> " + v);
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
        public static void startActivity(HashMap extras, Class activityClass, AppCompatActivity contextWrapper) {

            Intent intent = new Intent(contextWrapper, activityClass);
            for(String k: extras.keySet()) {
                intent.putExtra(k, extras.get(k));
            }
            contextWrapper.startActivity(intent);
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


                        //l(TAG, "diffy: " + (y2 - lasty2));
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
                l(fname + " is being used");
                return true;
            }
        }
        l("No files used !!!!!!!!!!!!!!!!!!!!!!!!!!");
        return false;
    }

    public static boolean filesIsBeingUsed(String filename) {

        boolean isLocked = false;
        RandomAccessFile fos = null;
        try {
            File file = new File(filename);
            if (file.exists()) {
                l(filename + " exists");
                fos = new RandomAccessFile(file, "rw");
            } else {
                l(filename + " does not exist");
            }
        } catch (FileNotFoundException e) {
            l(filename + " not found");
            isLocked = true;
        } catch (SecurityException e) {
            l(filename + " got security exception");
            isLocked = true;
        } catch (Exception e) {
            l(filename + " got exception: " + e.getMessage());
            isLocked = true;
            // handle exception
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                l(filename + " now closed: " + isLocked);

            } catch (Exception e) {
                //handle exception
                l(filename + " got exception: " + e.getMessage());
                isLocked = true;
            }
        }
        l(filename + " locked: " + isLocked);
        return isLocked;
    }

    public static boolean isAllAlphaWSorPunctuation(CharSequence charSequence) {
        for (int i = 0; i < charSequence.length(); ++i) {
            char ch = charSequence.charAt(i);
            if (!Character.isWhitespace(ch) && !Character.isLetter(ch) && ch != '.' && ch != '?' && ch != '!' && ch != ',' && ch != ';' && ch != ':' && ch != '\b') {
                return false;
            }
        }
        return true;
    }

    public static boolean isAllWhiteSpaceOrEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0 || isAllWhiteSpace(charSequence);

    }

    public static boolean isAllWhiteSpace(CharSequence charSequence) {

        for (int i = 0; i < charSequence.length(); ++i) {
            char ch = charSequence.charAt(i);
            if (!Character.isWhitespace(ch)) {
                return false;
            }
        }
        return true;
    }
}