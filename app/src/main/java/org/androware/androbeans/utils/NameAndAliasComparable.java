package org.androware.androbeans.utils;

import android.widget.AdapterView;

/**
 * Created by jkirkley on 8/13/16.
 */

public class NameAndAliasComparable<T> implements Comparable {


    private T name;
    private T alias;

    public NameAndAliasComparable(T n, T a) {
        name = n;
        alias = a;
    }

    public NameAndAliasComparable(T n) {
        name = n;
        alias = null;
    }


    @Override
    public int compareTo(Object another) {
        if(another instanceof NameAndAliasComparable) {
            NameAndAliasComparable namePairComparable = (NameAndAliasComparable) another;
            T otherName = (T) namePairComparable.getName();
            if( otherName.equals(name) ) {
                return 0;
            }
            T otherAlias = (T) namePairComparable.getAlias();
            if(otherAlias != null && otherAlias.equals(alias)) {
                return 0;
            }
            return (name.toString()).compareTo( otherName.toString());
        }
        throw new IllegalArgumentException("Cannot compare NameAndAliasComparable to " + another.getClass().getName());
    }

    public T getName() {
        return name;
    }

    public T getAlias() {
        return alias;
    }
}
