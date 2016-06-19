package org.androware.androbeans.beans;

import android.util.JsonReader;

import java.io.IOException;
import java.util.List;

import org.androware.androbeans.InstaBean;
import org.androware.androbeans.JSONinstaBean;

/**
 * Created by jkirkley on 5/7/16.
 */
public class ViewSpec extends JSONinstaBean {

    public String viewId;
    public String itemLayoutId;
    public boolean useDefault;

    public List<ItemSpec> items;

    public ViewSpec() {
    }

    public ViewSpec(JsonReader reader, Class type) throws IOException {
        super(reader,type);
    }

    public ViewSpec(JsonReader reader, Class type, InstaBean parent) throws IOException {
        super(reader,type, parent);
    }

    public ItemSpec getDefaultItemSpec(){
        return getItemSpec(0);
    }

    public ItemSpec getItemSpec(int position){
        if(useDefault) {
            return items.get(0);
        }
        return items.get(position);
    }


}

