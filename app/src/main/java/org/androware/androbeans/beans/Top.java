package org.androware.androbeans.beans;

import android.util.JsonReader;

import org.androware.androbeans.legacy.InstaBean;
import org.androware.androbeans.legacy.JSONinstaBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jkirkley on 6/16/16.
 */
public class Top  {

    public String string;
    public int anInt;

    public List<SubOne> subOnes;

    public Top() {

    }



    public static Map makeTextMap(){
        Map map = new HashMap();

        map.put("string", "this is a string");
        map.put("anInt", 23);

        List list = new ArrayList();

        for(int i = 0; i < 3; ++i){
            Map m = new HashMap();
            Map subMap = new HashMap();
            subMap.put("a", "abc" + i);
            subMap.put("b", i%2 == 1);
            subMap.put("c", i*20);
            m.put("s1", "def" + i);
            m.put("stringObjectHashMap", subMap);
            list.add(m);
        }

        map.put("subOnes", list);

        return map;
    }

}
