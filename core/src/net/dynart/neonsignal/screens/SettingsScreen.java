package net.dynart.neonsignal.screens;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.TimeUtils;

import net.dynart.neonsignal.core.controller.ControllerType;
import net.dynart.neonsignal.core.controller.ControllerTypeMap;
import net.dynart.neonsignal.core.SoundManager;
import net.dynart.neonsignal.core.ui.FadeToAction;
import net.dynart.neonsignal.core.ui.MenuButton;
import net.dynart.neonsignal.core.ui.MenuCursor;
import net.dynart.neonsignal.core.ui.MenuCursorItem;
import net.dynart.neonsignal.core.utils.StringUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.Settings;

public class SettingsScreen extends MenuScreen {

    private static final int FIRST_COLUMN_X = -450;
    private static final int SECOND_COLUMN_X = -200;
    private static final int OFFSET_Y = -110;

    private static final int ANALYTICS_Y = -200;

    private final Label soundLabel;
    private final Slider soundSlider;
    private final Label musicLabel;
    private final Slider musicSlider;
    private final Label controlLabel;
    private final Label selectedControlLabel;
    private final Group controlGroup;
    private final Image controlGroupBg;
    private final Label analyticsLabel;
    private final Label analyticsValueLabel;
    private final Map<Actor, Float> xPositions = new HashMap<>();
    private final Action backToMenuAction;
    private final Action backToPauseAction;
    private final Action customizeAction;
    private final SoundManager soundManager;
    private final Settings settings;

    private long soundLastTime;
    private boolean backToMenu;
    private boolean analyticsEnabled;
    private ControllerType controllerType;
    private ControllerType originalControllerType;

    public SettingsScreen(final Engine engine) {
        super(engine);

        settings = engine.getSettings();
        soundManager = engine.getSoundManager();

        analyticsEnabled = settings.isAnalyticsEnabled();
        controllerType = settings.getControllerType();

        group.addActor(menuCursor.getCursorImage());

        backToPauseAction = new FadeToAction(engine, "pause");
        backToMenuAction = new Action() {
            @Override
            public boolean act(float delta) {
                engine.moveToScreen("menu");
                return true;
            }
        };

        // sound volume
        soundLabel = new Label("Sound", styles.getDefaultLabelStyle());
        soundLabel.setAlignment(Align.bottomRight);
        soundLabel.setX(FIRST_COLUMN_X);
        soundLabel.setY(220 + OFFSET_Y);
        soundLabel.setWidth(220);
        soundLabel.setHeight(80);
        soundSlider = new Slider(0, 1, 0.001f, false, styles.getDefaultSliderStyle());
        soundSlider.setWidth(510);
        soundSlider.setX(SECOND_COLUMN_X + 2);
        soundSlider.setY(200 + OFFSET_Y);
        soundSlider.setValue(settings.getSoundVolume());
        soundSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                soundVolumeChanged();
            }
        });

        // music volume
        musicLabel = new Label("Music", styles.getDefaultLabelStyle());
        musicLabel.setAlignment(Align.bottomRight);
        musicLabel.setWidth(220);
        musicLabel.setX(FIRST_COLUMN_X);
        musicLabel.setHeight(80);
        musicLabel.setY(90 + OFFSET_Y);
        musicSlider = new Slider(0, 1, 0.001f, false, styles.getDefaultSliderStyle());
        musicSlider.setX(SECOND_COLUMN_X + 2);
        musicSlider.setY(70 + OFFSET_Y);
        musicSlider.setWidth(510);
        musicSlider.setValue(settings.getMusicVolume());
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                musicVolumeChanged();
            }
        });

        // control selector
        controlLabel = new Label("Control", styles.getDefaultLabelStyle());
        controlLabel.setAlignment(Align.center | Align.right);
        controlLabel.setWidth(220);
        controlLabel.setY(-66 + OFFSET_Y);
        controlLabel.setX(FIRST_COLUMN_X);
        controlLabel.setHeight(110);

        controlGroupBg = new Image(skin.getDrawable("transparent_white"));
        controlGroupBg.setWidth(480);
        controlGroupBg.setHeight(97);
        controlGroupBg.setX(SECOND_COLUMN_X + 10);
        controlGroupBg.setY(-63 + OFFSET_Y);

        MenuButton leftButton = createButton("left");
        leftButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                leftClicked();
            }
        });
        MenuButton rightButton = createButton("right");
        rightButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                rightClicked();
            }
        });
        rightButton.setX(SECOND_COLUMN_X + 147 + controlGroupBg.getWidth());
        MenuButton settingsButton = createButton("settings");
        customizeAction = new Action() {
            @Override
            public boolean act(float delta) {
                engine.moveToScreen("customize_" + controllerType.getName());
                return true;
            }
        };
        settingsButton.setX(SECOND_COLUMN_X + 259 + controlGroupBg.getWidth());
        settingsButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                moveOut(customizeAction);
            }
        });
        selectedControlLabel = new Label(StringUtil.camelize(settings.getControllerType().getName()), styles.getDefaultLabelStyle());
        selectedControlLabel.setAlignment(Align.center);
        selectedControlLabel.setWidth(339);
        selectedControlLabel.setHeight(110);
        selectedControlLabel.setX(90);
        controlGroup = new Table();
        controlGroup.setWidth(620);
        controlGroup.setHeight(110);
        controlGroup.setX(SECOND_COLUMN_X);
        controlGroup.setY(-70 + OFFSET_Y);
        controlGroup.addActor(leftButton);
        controlGroup.addActor(selectedControlLabel);
        controlGroup.addActor(rightButton);
        controlGroup.addActor(settingsButton);

        // analytics toggle
        analyticsLabel = new Label("Analytics", styles.getDefaultLabelStyle());
        analyticsLabel.setAlignment(Align.bottomRight);
        analyticsLabel.setWidth(220);
        analyticsLabel.setX(FIRST_COLUMN_X);
        analyticsLabel.setHeight(80);
        analyticsLabel.setY(ANALYTICS_Y + OFFSET_Y);
        analyticsValueLabel = new Label(analyticsEnabled ? "Enabled" : "Disabled", styles.getDefaultLabelStyle());
        analyticsValueLabel.setAlignment(Align.left);
        analyticsValueLabel.setWidth(220);
        analyticsValueLabel.setX(SECOND_COLUMN_X + 2);
        analyticsValueLabel.setHeight(80);
        analyticsValueLabel.setY(ANALYTICS_Y + OFFSET_Y);

        // add the actors and store the X positions
        List<Actor> allActors = new LinkedList<Actor>();
        allActors.add(soundLabel);
        allActors.add(soundSlider);
        allActors.add(musicLabel);
        allActors.add(musicSlider);
        allActors.add(controlLabel);
        allActors.add(controlGroupBg);
        allActors.add(controlGroup);
        allActors.add(analyticsLabel);
        allActors.add(analyticsValueLabel);
        for (Actor actor : allActors) {
            xPositions.put(actor, actor.getX());
            group.addActor(actor);
        }

        group.addActor(backButton);

        addSideBlackBars(stage);

        setUpCursor();
        adjustMenuCursorAndLabelForControllerType();
        menuCursor.setCurrentItem(soundSlider);
    }

    @Override
    public void show() {
        super.show();
        originalControllerType = settings.getControllerType();
        analyticsEnabled = settings.isAnalyticsEnabled();
        analyticsValueLabel.setText(analyticsEnabled ? "Enabled" : "Disabled");
    }

    void setBackToMenu(boolean value) {
        backToMenu = value;
    }

    private void setUpCursor() {
        MenuCursorItem item;
        item = menuCursor.addItem(backButton);
        item.setListener(MenuCursor.Event.ENTER, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                menuCursor.setCurrentItem(soundSlider);
                backClicked();
            }
        });
        item.setNeighbour(MenuCursor.Neighbour.DOWN, soundSlider);
        item.setNeighbour(MenuCursor.Neighbour.LEFT, soundSlider);
        item = menuCursor.addItem(soundSlider);
        item.setNeighbour(MenuCursor.Neighbour.DOWN, musicSlider);
        item.setNeighbour(MenuCursor.Neighbour.UP, backButton);
        item.setNeighbour(MenuCursor.Neighbour.RIGHT, backButton);
        item.setListener(MenuCursor.Event.LEFT, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                soundSlider.setValue(soundSlider.getValue() - 0.05f);
            }
        });
        item.setListener(MenuCursor.Event.RIGHT, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                soundSlider.setValue(soundSlider.getValue() + 0.05f);
            }
        });

        item = menuCursor.addItem(musicSlider);
        item.setNeighbour(MenuCursor.Neighbour.UP, soundSlider);
        item.setNeighbour(MenuCursor.Neighbour.DOWN, controlGroup);
        item.setListener(MenuCursor.Event.LEFT, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                musicSlider.setValue(musicSlider.getValue() - 0.05f);
            }
        });
        item.setListener(MenuCursor.Event.RIGHT, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                musicSlider.setValue(musicSlider.getValue() + 0.05f);
            }
        });

        item = menuCursor.addItem(controlGroup);
        item.setNeighbour(MenuCursor.Neighbour.UP, musicSlider);
        item.setNeighbour(MenuCursor.Neighbour.DOWN, analyticsValueLabel);
        item.setListener(MenuCursor.Event.LEFT, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                leftClicked();
            }
        });
        item.setListener(MenuCursor.Event.RIGHT, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                rightClicked();
            }
        });
        item.setListener(MenuCursor.Event.ENTER, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                moveOut(customizeAction);
            }
        });

        item = menuCursor.addItem(analyticsValueLabel);
        item.setNeighbour(MenuCursor.Neighbour.UP, controlGroup);
        item.setListener(MenuCursor.Event.ENTER, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                toggleAnalytics();
            }
        });
        item.setListener(MenuCursor.Event.LEFT, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                toggleAnalytics();
            }
        });
        item.setListener(MenuCursor.Event.RIGHT, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                toggleAnalytics();
            }
        });
    }

    private void toggleAnalytics() {
        analyticsEnabled = !analyticsEnabled;
        analyticsValueLabel.setText(analyticsEnabled ? "Enabled" : "Disabled");
    }

    private MenuButton createButton(String name) {
        MenuButton result = new MenuButton(engine, "");
        result.setWidth(110);
        result.setHeight(110);
        result.setIcon(new Image(skin.getDrawable("icon_" + name)));
        return result;
    }

    @Override
    public void backClicked() {
        if (isAnimating()) { return; }
        settings.setSoundVolume(soundSlider.getValue());
        settings.setMusicVolume(musicSlider.getValue());
        settings.setControllerType(controllerType);
        settings.setAnalyticsEnabled(analyticsEnabled);
        if (engine.getAnalyticsManager() != null) {
            engine.getAnalyticsManager().setEnabled(analyticsEnabled);
        }
        settings.save();
        menuCursor.adjustInUse();
        if (backToMenu) {
            moveOut(backToMenuAction);
        } else {
            fadeOut(backToPauseAction);
        }
    }

    private void soundVolumeChanged() {
        if (isAnimating()) { return; }
        soundManager.setVolume(soundSlider.getValue());
        if (TimeUtils.millis() - 300 > soundLastTime) {
            soundLastTime = TimeUtils.millis();
            soundManager.play("button_click");
        }
    }

    private void musicVolumeChanged() {
        if (isAnimating()) { return; }
        soundManager.setMusicVolume(musicSlider.getValue());
    }

    private int getControllerTypeIndex() {
        ControllerType[] array = ControllerTypeMap.getArray();
        for (int i = 0; i < array.length; i++) {
            if (array[i] == controllerType) {
                return i;
            }
        }
        return -1;
    }

    private ControllerType getControllerTypeByIndex(int index) {
        ControllerType[] array = ControllerTypeMap.getArray();
        return array[index];
    }

    private void leftClicked() {
        changeControllerType(-1);
    }

    private void rightClicked() {
        changeControllerType(+1);
    }

    private void changeControllerType(int direction) {
        if (isAnimating()) { return; }
        int index = getControllerTypeIndex();
        index += direction;
        if (index < 0) {
            index = ControllerTypeMap.values().size() - 1;
        }
        else if (index == ControllerTypeMap.values().size()) {
            index = 0;
        }
        controllerType = getControllerTypeByIndex(index);
        adjustMenuCursorAndLabelForControllerType();
    }

    private void adjustMenuCursorAndLabelForControllerType() {
        String name = StringUtil.capitalizeFirstChar(controllerType.getName());
        selectedControlLabel.setText(name);
        if (originalControllerType == ControllerType.TOUCH) {
            originalControllerType = null;
            settings.setControllerType(controllerType);
            settings.save();
            menuCursor.setCurrentItem(controlGroup);
            menuCursor.adjustInUse();
        }
    }

    void resetActorPositions() {
        for (Actor actor : xPositions.keySet()) {
            actor.setX(xPositions.get(actor));
        }
    }

    @Override
    public void moveIn() {
        moving = true;
        float right = SECOND_COLUMN_X + stage.getWidth() - 300f;
        float left = SECOND_COLUMN_X - stage.getWidth() + 300f;
        moveInActor(soundLabel, left, 0.15f);
        moveInActor(soundSlider, right, 0.15f);
        moveInActor(musicLabel, left, 0.10f);
        moveInActor(musicSlider, right, 0.10f);
        moveInActor(controlLabel, left, 0.05f);
        moveInActor(controlGroupBg, right, 0.05f);
        moveInActor(controlGroup, right, 0.05f);
        moveInActor(analyticsLabel, left, 0.0f);
        moveInActor(analyticsValueLabel, right, 0.0f);
        stage.addAction(Actions.sequence(Actions.delay(0.30f), movingFinishedAction));
    }

    private void moveInActor(Actor actor, float x, float delay) {
        actor.setX(x);
        actor.addAction(Actions.sequence(
            Actions.delay(delay),
            Actions.moveTo(xPositions.get(actor), actor.getY(),0.15f, Interpolation.sineOut)
        ));
    }

    private void moveOut(Action endAction) {
        if (isAnimating()) { return; }
        moving = true;
        float right = SECOND_COLUMN_X + stage.getWidth() - 300f;
        float left = SECOND_COLUMN_X - stage.getWidth() + 300f;
        moveOutActor(analyticsLabel, left, 0.0f);
        moveOutActor(analyticsValueLabel, right, 0.0f);
        moveOutActor(soundLabel, left, 0.0f);
        moveOutActor(soundSlider, right, 0.0f);
        moveOutActor(musicLabel, left, 0.05f);
        moveOutActor(musicSlider, right, 0.05f);
        moveOutActor(controlLabel, left, 0.10f);
        moveOutActor(controlGroupBg, right, 0.10f);
        moveOutActor(controlGroup, right, 0.10f);
        stage.addAction(Actions.sequence(
            Actions.delay(0.31f),
            movingFinishedAction,
            endAction
        ));
    }

    private void moveOutActor(Actor actor, float x, float delay) {
        actor.addAction(Actions.sequence(
            Actions.delay(delay),
            Actions.moveTo(x, actor.getY(),0.15f, Interpolation.sineIn)
        ));
    }


}

