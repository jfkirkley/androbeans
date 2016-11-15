package org.androware.androbeans;


import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.androware.androbeans.beans.Flow;
import org.androware.androbeans.beans.Top;
import org.androware.androbeans.utils.FilterLog;
import org.androware.androbeans.utils.ResourceUtils;

public class MainActivity extends Activity {
    public final static String TAG = "main";

    public void l(String s) {
        FilterLog.inst().log(TAG, s);
    }

    public void l(String tag, String s) {
        FilterLog.inst().log(tag, s);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ResourceUtils.R = R.class;
        FilterLog.inst().activateTag(TAG);
        FilterLog.inst().activateTag(JsonObjectWriter.TAG);

        try {
            Top top = (Top) ObjectReaderFactory.getInstance(this).makeAndRunMapReader(Top.makeTextMap(), Top.class);

            l(top.toString());

            Flow flow = (Flow) ObjectReaderFactory.getInstance().makeAndRunLinkedJsonReader("test_merge", Flow.class);

            l(flow.toStringTest());

            ObjectWriterFactory.getInstance(this).writeJsonObjectToExternalFile("xyz.js", flow);


        } catch (ObjectReadException e) {
        } catch (ObjectWriteException e) {
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent contextWrapper in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
