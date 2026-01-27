package net.dynart.neonsignal.core.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.EngineConfig;
import net.dynart.neonsignal.core.Fade;
import net.dynart.neonsignal.core.Settings;

public class GameController {

    private final Engine engine;
    private final EngineConfig config;

    private final Map<Button, Integer> keyMap = new HashMap<>();
    private final Map<Button, Integer> joyMap = new HashMap<>();
    private final Map<Button, AxisData> axisMap = new HashMap<>();

    private float axisX;
    private float axisY;

    private boolean anyKeyDown;
    private boolean anyKeyPressed;

    private boolean leftKeyDown;
    private boolean rightKeyDown;
    private boolean upKeyDown;
    private boolean downKeyDown;

    private boolean aDown;
    private boolean bDown;
    private boolean xDown;
    private boolean yDown;

    private boolean menuDown;
    private boolean menuKeyPressed;
    private boolean bPressed;

    private boolean anyPointerDown;

    private final Set<Integer> pointersDown = new HashSet<>();

    public GameController(Engine engine) {
        config = engine.getConfig();
        this.engine = engine;
        Settings settings = engine.getSettings();
        for (Button button : config.getDefaultKeyMapping().keySet()) {
            keyMap.put(button, settings.getKey(button));
        }
        for (Button button : config.getDefaultJoyMapping().keySet()) {
            joyMap.put(button, settings.getJoy(button));
        }
        for (Button button : config.getDefaultAxisMapping().keySet()) {
            axisMap.put(button, settings.getAxisData(button));
        }
    }

    public Integer getKeyCode(Button button) {
        return keyMap.get(button);
    }

    public Integer getJoyCode(Button button) {
        return joyMap.get(button);
    }

    public void setKeyCode(Button button, int code) {
        keyMap.put(button, code);
    }

    public void setJoyCode(Button button, int code) {
        joyMap.put(button, code);
    }

    public float getAxisY() {
        return axisY;
    }

    public void setAxisY(float value) {
        axisY = value;
    }

    public float getAxisX() {
        return axisX;
    }

    public void setAxisX(float value) {
        axisX = value;
    }

    public boolean isADown() {
        return aDown;
    }

    public void setADown(boolean value) {
        aDown = value;
    }

    public boolean isBDown() {
        return bDown;
    }

    public void setBDown(boolean value) {
        if (!value && bDown) {
            bPressed = true;
        }
        bDown = value;
    }

    public boolean isBPressed() {
        return bPressed;
    }

    public boolean isXDown() {
        return xDown;
    }

    public void setXDown(boolean value) {
        xDown = value;
    }

    public boolean isYDown() {
        return yDown;
    }

    public void setYDown(boolean value) {
        yDown = value;
    }

    public boolean isMenuDown() {
        return menuDown;
    }

    public boolean isMenuPressed() {
        return menuKeyPressed;
    }

    public boolean wantsToDrop() {
        return aDown && getAxisY() < -config.getPlayerMinVerticalAxis();
    }

    public void setMenuDown(boolean value) {
        if (!value && menuDown) {
            menuKeyPressed = true;
        }
        menuDown = value;
    }

    public void setLeftDown(boolean value) {
        if (!value) {
            setAxisX(rightKeyDown ? 1 : 0);
        } else {
            setAxisX(-1);
        }
        leftKeyDown = value;
    }

    public void setRightDown(boolean value) {
        if (!value) {
            setAxisX(leftKeyDown ? -1 : 0);
        } else {
            setAxisX(1);
        }
        rightKeyDown = value;
    }

    public void setDownDown(boolean value) {
        if (!value) {
            setAxisY(upKeyDown ? 1 : 0);
        } else {
            setAxisY(-1);
        }
        downKeyDown = value;
    }

    public void setUpDown(boolean value) {
        if (!value) {
            setAxisY(downKeyDown ? -1 : 0);
        } else {
            setAxisY(1);
        }
        upKeyDown = value;
    }

    public void setDown(Button button, boolean value) {
        if (button == Button.A) {
            setADown(value);
        } else if (button == Button.B) {
            setBDown(value);
        } else if (button == Button.X) {
            setXDown(value);
        } else if (button == Button.Y) {
            setYDown(value);
        } else if (button == Button.MENU) {
            setMenuDown(value);
        } else if (button == Button.LEFT) {
            setLeftDown(value);
        } else if (button == Button.RIGHT) {
            setRightDown(value);
        } else if (button == Button.DOWN) {
            setDownDown(value);
        } else if (button == Button.UP) {
            setUpDown(value);
        }
    }

    public boolean keyEvent(int keyCode, boolean down, boolean useKeyMap) {
        boolean result = true;
        Map<Button, Integer> map = useKeyMap ? keyMap : joyMap;
        if (keyCode == map.get(Button.LEFT)) {
            setLeftDown(down);
        } else if (keyCode == map.get(Button.RIGHT)) {
            setRightDown(down);
        } else if (keyCode == map.get(Button.UP)) {
            setUpDown(down);
        } else if (keyCode == map.get(Button.DOWN)) {
            setDownDown(down);
        } else if (keyCode == map.get(Button.A)) {
            setADown(down);
        } else if (keyCode == map.get(Button.B)) {
            setBDown(down);
        } else if (keyCode == map.get(Button.X)) {
            setXDown(down);
        } else if (keyCode == map.get(Button.Y)) {
            setYDown(down);
        } else if (keyCode == map.get(Button.MENU)) {
            setMenuDown(down);
        } else {
            result = false;
        }
        return result;
    }

    public AxisData getAxisData(Button button) {
        return axisMap.get(button);
    }

    public void setAxisData(Button button, int code, int sign) {
        AxisData data = axisMap.get(button);
        data.setCode(code);
        data.setSign(sign);
    }

    public boolean hasAxisData(Button button) {
        return axisMap.containsKey(button);
    }

    public Set<Button> getAxisKeys() {
        return axisMap.keySet();
    }

    public boolean isAnyKeyDown() {
        return aDown || bDown || xDown || yDown || menuDown
            || upKeyDown || downKeyDown || leftKeyDown || rightKeyDown
            || anyPointerDown;
    }

    public void update() {
        boolean anyKeyJustPressed = !anyKeyDown && isAnyKeyDown();
        if (anyKeyJustPressed) {
            anyKeyPressed = true;
        }
        anyKeyDown = isAnyKeyDown();

        // TODO: this looks fade specific
        Fade fade = engine.getFade();
        if (fade.isActive() && fade.getDirection() == Fade.Direction.OUT) {
            reset();
        }
        //
    }

    public boolean isAnyKeyPressed() {
        return anyKeyPressed;
    }

    public void postUpdate() {
        anyKeyPressed = false;
        menuKeyPressed = false;
        bPressed = false;
    }

    public void setPointerDown(int pointer, boolean value) {
        if (value) {
            pointersDown.add(pointer);
        } else {
            pointersDown.remove(pointer);
        }
        anyPointerDown = !pointersDown.isEmpty();
    }
    
    public void reset() {
        setRightDown(false);
        setLeftDown(false);
        setUpDown(false);
        setDownDown(false);
        setADown(false);
        setBDown(false);
        setXDown(false);
        setYDown(false);
        setMenuDown(false);
        anyPointerDown = false;
        pointersDown.clear();
    }

}
