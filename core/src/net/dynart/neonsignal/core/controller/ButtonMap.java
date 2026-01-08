package net.dynart.neonsignal.core.controller;

import java.util.HashMap;
import java.util.Map;

public class ButtonMap {

    private static final Map<String, Button> map = new HashMap<String, Button>();

    public static void add(Button button) {
        map.put(button.getName(), button);
    }

    public static Button getByName(String name) {
        return map.get(name);
    }
}
