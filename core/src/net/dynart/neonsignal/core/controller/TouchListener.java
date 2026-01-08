package net.dynart.neonsignal.core.controller;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import net.dynart.neonsignal.components.PlayerComponent;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.PlayerAbility;
import net.dynart.neonsignal.core.TextureManager;

import java.util.HashMap;
import java.util.Map;

import net.dynart.neonsignal.GameStage;
import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.EngineConfig;
import net.dynart.neonsignal.core.Settings;

public class TouchListener extends InputAdapter {

    private final EngineConfig config;
    private GameStage gameStage;
    private final Engine engine;
    private final GameController gameController;

    private boolean active;
    private final Settings settings;
    private final Vector2 screenPos = new Vector2(); // temporary variable, don't want to create always

    private final int buttonCount;
    private final String[] buttonNames;
    private final boolean[] buttonDown;
    private final Button[] buttons;
    private boolean specialButtonDown = false;
    private boolean joyDown = false;

    private final HashMap<Integer, Vector2> pointerDownPos = new HashMap<>();
    private final HashMap<Integer, Vector2> pointerCurrentPos = new HashMap<>();
    private final HashMap<Integer, Integer> pointerButton = new HashMap<>(); // pointer -> button association
    private int pointerSpecialButton = -1;
    private int pointerJoy = -1;

    // UI related
    private final Image[] buttonImages;
    private final Drawable[] upDrawables;
    private final Drawable[] downDrawables;
    private final Image joyTopImage;
    private final Image joyBottomImage;

    private final Map<String, PlayerAbility[]> abilityByActor = new HashMap<>();

    public TouchListener(Engine engine) {
        this.engine = engine;

        config = engine.getConfig();
        gameController = engine.getGameController();
        settings = engine.getSettings();
        TextureManager textureManager = engine.getTextureManager();
        Skin skin = textureManager.getSkin("ui");

        // create the buttons on the screen
        buttonNames = config.getTouchButtonNames();
        buttonCount = buttonNames.length;
        buttonDown = new boolean[buttonCount];
        buttons = new Button[buttonCount];
        buttonImages = new Image[buttonCount];
        upDrawables = new Drawable[buttonCount];
        downDrawables = new Drawable[buttonCount];
        for (int buttonIndex = 0; buttonIndex < buttonCount; buttonIndex++) {
            upDrawables[buttonIndex] = skin.getDrawable(buttonNames[buttonIndex] + "_button_up");
            downDrawables[buttonIndex] = skin.getDrawable(buttonNames[buttonIndex] + "_button_down");
            buttonImages[buttonIndex] = new Image(upDrawables[buttonIndex]);
            buttonDown[buttonIndex] = false;
            buttons[buttonIndex] = ButtonMap.getByName(buttonNames[buttonIndex]);
        }

        // create the joy on the screen
        joyBottomImage = new Image(skin.getDrawable("joy_bottom"));
        joyTopImage = new Image(skin.getDrawable("joy_top"));
        joyBottomImage.setVisible(false);
        joyTopImage.setVisible(false);

        // create abilities by actors
        PlayerAbility[] move = { PlayerAbility.MOVE };
        abilityByActor.put("joy_bottom", move);
        abilityByActor.put("joy_top", move);
        abilityByActor.put("a", PlayerAbility.ANY_JUMP);
        abilityByActor.put("b", PlayerAbility.ANY_PUNCH);
        abilityByActor.put("c", PlayerAbility.CHANGE_WEAPON);

        adjustActive();
    }

    public void addActorsToStage(GameStage gameStage) {
        this.gameStage = gameStage;
        for (int i = 0; i < buttonCount; i++) {
            gameStage.addActor(buttonImages[i]);
        }
        gameStage.addActor(joyBottomImage);
        gameStage.addActor(joyTopImage);
    }

    public void resize() {
        adjustActive();
        adjustButtons();
    }

    private void adjustActive() {
        active = !settings.mustCatchMouse();
    }

    private PlayerComponent getPlayer() {
        Entity entity = engine.getGameScene().getPlayer();
        if (entity != null) {
            return entity.getComponent(PlayerComponent.class);
        }
        return null;
    }

    private void adjustButtons() {
        PlayerComponent player = getPlayer();
        if (player == null) {
            return;
        }
        for (int i = 0; i < buttonCount; i++) {
            String name = buttonNames[i];
            Vector2 position = settings.getTouchPosition(name);
            if (position.x < 0) {
                position.x += gameStage.getWidth();
            }
            Image button = buttonImages[i];
            button.setPosition(position.x - button.getWidth() / 2f, position.y - button.getHeight() / 2f); // why the fuck the Align.center doesn't work?
            button.setVisible(active && player.hasAbility(abilityByActor.get(name)));
        }
    }

    // When we press a pointer down
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        if (!active) {
            return false;
        }

        // TODO: this looks gameplay specific, why is this here?
        PlayerComponent player = getPlayer();
        if (player != null && !player.hasAbility(PlayerAbility.MOVE)) {
            return false;
        }
        //

        if (!config.isMobile()) { // convert mouse button to pointer
            pointer = button;
        }

        boolean result = false;

        // set the down and the current position of the pointer
        screenPos.set(screenX, screenY);
        Vector2 stgPos = gameStage.screenToStageCoordinates(screenPos);
        pointerDownPos.put(pointer, stgPos.cpy());
        pointerCurrentPos.put(pointer, stgPos);

        // get the closest button to the pointer
        int closestButtonIndex = getClosestButtonIndex(pointer);

        // if there is a button under the pointer and not pressed yet..
        if (closestButtonIndex != -1 && !buttonDown[closestButtonIndex]) {
            // ..press it
            pressButton(closestButtonIndex, pointer);
            result = true;
        }

        // if the special button is not down and no button under the pointer and it isn't on the moving side..
        if (!specialButtonDown && closestButtonIndex == -1 && !isOnMovingSide(pointer)) {
            // ..press it
            pressSpecialButton(pointer);
            result = true;
        }

        // if the joy is not down and no button under the pointer and it is on the moving side..
        if (!joyDown && closestButtonIndex == -1 && isOnMovingSide(pointer)) {
            // ..press it
            pressJoy(pointer);
            result = true;
        }

        gameController.setPointerDown(pointer, true);

        return result;
    }

    // When we release a pointer up
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (!active) {
            return false;
        }

        if (!config.isMobile()) { // convert mouse button to pointer
            pointer = button;
        }

        boolean result = false;

        // set the current position of the pointer
        screenPos.set(screenX, screenY);
        pointerCurrentPos.put(pointer, gameStage.screenToStageCoordinates(screenPos));

        // if we pressed a button with the pointer earlier..
        if (pointerButton.containsKey(pointer)) {
            // ..release it
            releaseButton(pointerButton.get(pointer), pointer);
            result = true;
        }

        // if we pressed the special button with the pointer earlier..
        if (pointerSpecialButton == pointer) {
            // ..release it
            releaseSpecialButton();
            result = true;
        }

        // if we pressed the joy with the pointer earlier..
        if (pointerJoy == pointer) {
            // ..release it
            releaseJoy();
            result = true;
        }

        gameController.setPointerDown(pointer, false);

        return result;
    }

    // When we move a pointer
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!active) {
            return false;
        }

        // set the current position of the pointer
        screenPos.set(screenX, screenY);
        pointerCurrentPos.put(pointer, gameStage.screenToStageCoordinates(screenPos));

        if (pointerJoy != pointer) {
            return false;
        }

        // ..if the dragged pointer associated with the joy

        Vector2 downPos = pointerDownPos.get(pointer);
        Vector2 currentPos = pointerCurrentPos.get(pointer);

        // set the direction (maximum length: the radius of the joy_bottom)
        float radius = joyBottomImage.getWidth() / 2f - joyTopImage.getWidth() / 3f;
        Vector2 direction = new Vector2(currentPos);
        direction.sub(downPos);
        float length = direction.len();
        direction.nor();
        direction.x *= Math.min(length, radius);
        direction.y *= Math.min(length, radius);

        joyTopImage.setPosition(
            downPos.x + direction.x - joyTopImage.getWidth() / 2f,
            downPos.y + direction.y - joyTopImage.getHeight() / 2f
        );

        gameController.setAxisX(direction.x / radius);
        gameController.setAxisY(direction.y / radius);

        return true;
    }

    private void pressButton(int buttonIndex, int pointer) {
        buttonImages[buttonIndex].setDrawable(downDrawables[buttonIndex]); // the set view
        buttonDown[buttonIndex] = true; // set the button state as down
        pointerButton.put(pointer, buttonIndex); // associate the pointer with the button
        gameController.setDown(buttons[buttonIndex], true); // set the controller
    }

    private void releaseButton(int buttonIndex, int pointer) {
        buttonImages[buttonIndex].setDrawable(upDrawables[buttonIndex]); // set the view
        buttonDown[buttonIndex] = false; // set the button state as up
        pointerButton.remove(pointer); // remove the button association with the pointer
        gameController.setDown(buttons[buttonIndex], false); // set the controller
    }

    private void pressSpecialButton(int pointer) {
        specialButtonDown = true;
        pointerSpecialButton = pointer;
        gameController.setADown(true);
    }

    private void releaseSpecialButton() {
        specialButtonDown = false;
        pointerSpecialButton = -1;
        gameController.setADown(false);
    }

    private void pressJoy(int pointer) {
        Vector2 currentPos = pointerCurrentPos.get(pointer);
        joyDown = true;
        pointerJoy = pointer;
        joyBottomImage.setPosition(
            currentPos.x - joyBottomImage.getWidth() / 2,
            currentPos.y - joyBottomImage.getWidth() / 2
        );
        joyTopImage.setPosition(
            currentPos.x - joyTopImage.getWidth() / 2,
            currentPos.y - joyTopImage.getWidth() / 2
        );
        joyBottomImage.setVisible(true);
        joyTopImage.setVisible(true);
    }

    private void releaseJoy() {
        joyDown = false;
        pointerJoy = -1;
        gameController.setAxisX(0);
        gameController.setAxisY(0);
        joyBottomImage.setVisible(false);
        joyTopImage.setVisible(false);
    }

    // returns with the closest button index, -1 if not in range any of them
    private int getClosestButtonIndex(int pointer) {
        float x = pointerCurrentPos.get(pointer).x;
        float y = pointerCurrentPos.get(pointer).y;
        float minD = (float)Math.pow(config.getTouchButtonSize(), 2);
        int closestButton = -1;
        float closestD = Float.MAX_VALUE;
        PlayerComponent player = getPlayer();
        if (player == null) {
            return closestButton;
        }
        for (int j = 0; j < buttonImages.length; j++) {
            if (!player.hasAbility(abilityByActor.get(buttonNames[j]))) {
                continue;
            }
            Image button = buttonImages[j];
            float d = (float)Math.pow(x - button.getX() - button.getWidth() / 2f, 2) + (float)Math.pow(y - button.getY() - button.getHeight() / 2f, 2); // why the fuck Align.center doesn't work?
            if (d < minD && d < closestD) {
                closestD = d;
                closestButton = j;
            }
        }
        return closestButton;
    }

    private boolean isOnMovingSide(int pointer) {
        float halfWidth = gameStage.getWidth() / 2;
        boolean sidesSwitched = settings.isTouchSidesSwitched();
        return (pointerCurrentPos.get(pointer).x < halfWidth && !sidesSwitched)
            || (pointerCurrentPos.get(pointer).x > halfWidth && sidesSwitched);
    }

}
