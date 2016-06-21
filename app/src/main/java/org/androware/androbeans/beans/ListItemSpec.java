package org.androware.androbeans.beans;

import android.util.JsonReader;

import java.io.IOException;

import org.androware.androbeans.legacy.InstaBean;

/**
 * Created by jkirkley on 5/7/16.
 */
public class ListItemSpec extends ItemSpec {

    public String label;
    public String labelFieldId;
    public String target;

    public ListItemSpec() {
    }

    public ListItemSpec(JsonReader reader, Class type) throws IOException {
        this(reader,type, null);
    }

    public ListItemSpec(JsonReader reader, Class type, InstaBean parent) throws IOException {
        super(reader,type, parent);
    }

}

