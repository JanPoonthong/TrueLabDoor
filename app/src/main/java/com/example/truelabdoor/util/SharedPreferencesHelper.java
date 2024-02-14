package com.example.truelabdoor.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * Save the information configuration.
 */
public class SharedPreferencesHelper {

    private final SharedPreferences sharedPreferences;
    /*
     * Save your phone's name
     */private final SharedPreferences.Editor editor;

    public SharedPreferencesHelper(Context context, String FILE_NAME) {
        sharedPreferences = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * put the saved data
     *
     * @param key    The name of the preference to modify.
     * @param object
     */
    public void put(String key, Object object) {
        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }
        editor.commit();
    }

    /**
     * Get the saved data
     *
     * @param key
     * @param defaultObject
     * @return
     */
    public Object getSharedPreference(String key, Object defaultObject) {
        if (defaultObject instanceof String) {
            return sharedPreferences.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return sharedPreferences.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return sharedPreferences.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return sharedPreferences.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return sharedPreferences.getLong(key, (Long) defaultObject);
        } else {
            return sharedPreferences.getString(key, null);
        }
    }

    /**
     * Removes a value that already corresponds to a key value
     */
    public void remove(String key) {
        editor.remove(key);
        editor.commit();
    }

    /**
     * Clear all data
     */
    public void clear() {
        editor.clear();
        editor.commit();
    }

    /**
     * Query if a key exists
     */
    public Boolean contain(String key) {
        return sharedPreferences.contains(key);
    }

    /**
     * Returns all key pairs
     */
    public Map<String, ?> getAll() {
        return sharedPreferences.getAll();
    }
}