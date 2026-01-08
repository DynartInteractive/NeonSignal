package net.dynart.neonsignal.core.controller;

public enum ControllerType {

    KEYBOARD("keyboard"), GAMEPAD("gamepad"), TOUCH("touch");

    private final String name;

    ControllerType(String name) {
        this.name = name;
        ControllerTypeMap.add(this);
    }

    public String getName() {
        return name;
    }

}
