package org.androware.androbeans;


import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.androware.androbeans.beans.Flow;
import org.androware.androbeans.beans.Top;
import org.androware.androbeans.utils.FilterLog;
import org.androware.androbeans.utils.ResourceUtils;
import org.androware.androbeans.utils.Utils;

import java.io.FileOutputStream;
import java.io.IOException;

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

        MapObjectReader mapObjectReader = new MapObjectReader(Top.makeTextMap(), Top.class);

        try {
            Top top = (Top) mapObjectReader.read();
            l("tp" + top.toString());
        } catch (ObjectReadException e) {
            e.printStackTrace();
        }


        if (false) {
            try {
                JsonObjectReader jsonObjectReader = new JsonObjectReader(ResourceUtils.getResourceInputStream(this, "test_flow", "raw"), Flow.class);
                jsonObjectReader.addObjectReadListener(new LinkObjectReadListener());
                Flow flow = (Flow) jsonObjectReader.read();

                l(flow.toStringTest());

                FileOutputStream fos = Utils.getExternalFileOutputStream(this, null, "", "bout.txt");
                JsonObjectWriter jsonObjectWriter = new JsonObjectWriter(fos);
                jsonObjectWriter.write(flow);
                jsonObjectWriter.close();

            } catch (IOException e) {
            } catch (ObjectReadException e) {

            }
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
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
