package net.dynart.neonsignal.core.controller;

public enum Button {

    LEFT("left"),
    RIGHT("right"),
    UP("up"),
    DOWN("down"),
    A("a"),
    B("b"),
    C("c"),
    MENU("menu");

    private final String name;

    Button(String name) {
        this.name = name;
        ButtonMap.add(this);
    }

    public String getName() {
        return name;
    }

}
