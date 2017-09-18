package com.oose2017.kwang57.hareandhounds;

import java.util.Arrays;
public class MapKey {

    private final int[] values;

    public MapKey(int[] values) {
        this.values = values;
    }

    @Override
    public boolean equals(Object another) {
        if (another == this) {
            return true;
        }
        if (another == null) {
            return false;
        }
        if (another.getClass() != this.getClass()) {
            return false;
        }
        MapKey key = (MapKey) another;
        return Arrays.equals(this.values, key.values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.values);
    }

}
