package net.dynart.neonsignal.core.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ControllerTypeMap {

    private static final Map<String, ControllerType> map = new HashMap<>();

    public static void add(ControllerType controllerType) {
        map.put(controllerType.getName(), controllerType);
    }

    public static ControllerType getByName(String name) {
        return map.get(name);
    }

    public static Collection<ControllerType> values() {
        return map.values();
    }

    public static ControllerType[] getArray() {
        Collection<ControllerType> controllerTypes = values();
        return controllerTypes.toArray(new ControllerType[controllerTypes.size()]);
    }
}
