package net.dynart.neonsignal;


import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class DesktopArguments {

    private int width = 1280;
    private int height = 720;
    private boolean sizeWasSet;
    private boolean fullscreen = true;
    private boolean vsync = true;
    private boolean debug;
    private boolean gaDebug;
    private String level;
    private int fps;
    private int display = -1;
    private boolean borderless = false;

    private boolean nextIsWidth;
    private boolean nextIsHeight;
    private boolean nextIsLevel;
    private boolean nextIsFps;
    private boolean nextIsDisplay;

    void process(String arg) {
        if (nextIsWidth) {
            nextIsWidth = false;
            width = Integer.parseInt(arg);
            sizeWasSet = true;
        }
        if (nextIsHeight) {
            nextIsHeight = false;
            height = Integer.parseInt(arg);
            sizeWasSet = true;
        }
        if (nextIsFps) {
            nextIsFps = false;
            fps = Integer.parseInt(arg);
        }
        if (nextIsLevel) {
            nextIsLevel = false;
            level = arg;
        }
        if (nextIsDisplay) {
            nextIsDisplay = false;
            display = Integer.parseInt(arg);
        }
        switch (arg) {
            case "-window":
                fullscreen = false;
                break;
            case "-w":
            case "-width":
                nextIsWidth = true;
                break;
            case "-h":
            case "-height":
                nextIsHeight = true;
                break;
            case "-l":
            case "-level":
                nextIsLevel = true;
                break;
            case "-display":
                nextIsDisplay = true;
                break;
            case "-debug":
                debug = true;
                break;
            case "-ga_debug":
                gaDebug = true;
                break;
            case "-fps":
                nextIsFps = true;
                break;
            case "-novsync":
                vsync = false;
                break;
            case "-borderless":
                borderless = true;
                break;
        }
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isSizeWasSet() {
        return sizeWasSet;
    }

    public String getLevel() {
        return level;
    }

    public boolean getDebug() {
        return debug;
    }

    public boolean isGaDebug() {
        return gaDebug;
    }

    public int getFps() {
        return fps;
    }

    public boolean isVsync() {
        return vsync;
    }

    public boolean isBorderless() {
        return borderless;
    }

    public int getDisplay() {
        return display;
    }
}
