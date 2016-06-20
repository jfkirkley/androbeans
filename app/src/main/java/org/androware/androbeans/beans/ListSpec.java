package org.androware.androbeans.beans;

import java.util.List;
import java.util.Map;

/**
 * Created by jkirkley on 6/20/16.
 */
public class ListSpec extends  ViewSpec {

    public void __get_type_overrides__(Map map) {
        map.put(ItemSpec.class, ListItemSpec.class);
    }

}
