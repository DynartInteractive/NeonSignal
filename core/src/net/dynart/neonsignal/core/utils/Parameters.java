package net.dynart.neonsignal.core.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Parameters {

    private final Map<String, String> data = new HashMap<>();

    public void set(String name, String value) {
        data.put(name, value);
    }

    public void set(String name, int value) {
        data.put(name, String.valueOf(value));
    }

    public void set(String name, float value) {
        data.put(name, String.valueOf(value));
    }

    public void set(String name, boolean value) {
        data.put(name, String.valueOf(value));
    }

    public String get(String name) {
        return data.get(name);
    }

    public String get(String name, String defaultValue) {
        return data.containsKey(name) ? get(name) : defaultValue;
    }

    public boolean has(String name) {
        return data.containsKey(name);
    }

    public int getInteger(String name) {
        return Integer.parseInt(data.get(name));
    }

    public int getInteger(String name, int defaultValue) {
        return data.containsKey(name) ? getInteger(name) : defaultValue;
    }

    public float getFloat(String name) {
        return Float.parseFloat(data.get(name));
    }

    public float getFloat(String name, float defaultValue) {
        return data.containsKey(name) ? getFloat(name) : defaultValue;
    }

    public boolean getBoolean(String name) {
        return Boolean.parseBoolean(data.get(name));
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        return data.containsKey(name) ? getBoolean(name) : defaultValue;
    }

    public void clear() {
        data.clear();
    }

    public void copy(Parameters parameters) {
        for (String name : parameters.data.keySet()) {
            data.put(name, parameters.get(name));
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (String name : data.keySet()) {
            result.append(name).append(": ").append(data.get(name)).append("\r\n");
        }
        return result.toString();
    }

    public Set<String> getKeySet() {
        return data.keySet();
    }
}
