package com.dank.util;

import java.util.HashMap;

/**
 * Project: DankWise
 * Date: 01-03-2015
 * Time: 19:43
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 * Use to lock a hashmap from receiving more keys
 */
public class LockableHashMap<K, V> extends HashMap<K, V> {

    public LockableHashMap(final K... initialKeys) {
        for (final K key : initialKeys) {
            super.put(key, null);
        }
    }

    public final V put(K key, V value) {
        if (super.containsKey(key)) {
            return super.put(key, value);
        }
        //throw new Error("Locked KeySet: " + key + "." + value);
        return null;
    }
}
