package net.dynart.neonsignal.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import net.dynart.neonsignal.core.TextureManager;
import net.dynart.neonsignal.core.controller.Button;
import net.dynart.neonsignal.core.listeners.DialogListener;
import net.dynart.neonsignal.core.DialogStage;
import net.dynart.neonsignal.core.Screen;
import net.dynart.neonsignal.core.ui.MenuCursor;
import net.dynart.neonsignal.core.ui.MenuCursorItem;
import net.dynart.neonsignal.core.ui.Styles;
import net.dynart.neonsignal.core.ui.MenuButton;
import net.dynart.neonsignal.core.Engine;

public class DialogScreen extends Screen {

    private static final int MAX_BUTTON_COUNT = 3;

    private final Image bgImage;
    private final Group group;
    private final Table table;
    private final Label textLabel;
    private final MenuButton[] buttons = new MenuButton[MAX_BUTTON_COUNT];
    private final Action switchToBackScreenAction;

    private Screen backScreen;
    private int buttonCount;
    private DialogListener listener;
    private MenuCursor menuCursor;
    private MenuCursorItem firstMenuCursorItem;

    public DialogScreen(final Engine engine) {
        super(engine);

        clear = false;

        Styles styles = engine.getStyles();
        TextureManager textureManager = engine.getTextureManager();
        Skin skin = textureManager.getSkin("ui");

        bgImage = new Image(skin.getDrawable("dialog_fix_bg"));
        stage.addActor(bgImage);

        group = new Group();

        textLabel = new Label("DialogType", styles.getDefaultLabelStyle());
        textLabel.setAlignment(Align.center);

        table = new Table();
        table.setBackground(skin.getDrawable("dialog_bg"));
        table.add(textLabel).expand().fill().row();
        table.add().height(65);
        group.addActor(table);

        switchToBackScreenAction = new Action() {
            public boolean act(float delta) {
                engine.setScreen(backScreen);
                return true;
            }
        };

        ClickListener clickListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (listener != null) {
                    Actor actor = event.getListenerActor();
                    listener.dialogButtonClicked((Integer)actor.getUserObject());
                }
            }
        };

        // if we click on the background, or hit "MENU" button, means: cancel
        bgImage.setUserObject(0);
        bgImage.addListener(clickListener);
        stage.addListener(new InputListener() {
            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                if (keycode == engine.getGameController().getKeyCode(Button.MENU)
                        || keycode == engine.getGameController().getKeyCode(Button.B)) {
                    if (listener != null) {
                        listener.dialogButtonClicked(0);
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });

        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new MenuButton(engine, "Button", styles.getDefaultButtonStyle());
            buttons[i].setWidth(250);
            buttons[i].setHeight(120);
            buttons[i].setUserObject(i);
            buttons[i].addListener(clickListener);
        }

        setUpCursor();

        group.addActor(menuCursor.getCursorImage());
        for (int i = 0; i < buttons.length; i++) {
            group.addActor(buttons[i]);
        }

        stage.addActor(group);
        addSideBlackBars(stage);
    }

    private void setUpCursor() {
        menuCursor = new MenuCursor(engine);
        MenuCursorItem item;
        MenuCursor.Listener selectListener = new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                listener.dialogButtonClicked((Integer)item.getData());
            }
        };
        for (int i = 0; i < buttons.length; i++) {
            item = menuCursor.addItem(buttons[i]);
            if (firstMenuCursorItem == null) {
                firstMenuCursorItem = item;
            }
            item.setData(i);
            if (i < buttons.length - 1) {
                item.setNeighbour(MenuCursor.Neighbour.RIGHT, buttons[i + 1]);
            }
            if (i > 0) {
                item.setNeighbour(MenuCursor.Neighbour.LEFT, buttons[i - 1]);
            }
            item.setListener(MenuCursor.Event.ENTER, selectListener);
        }
    }

    @Override
    public Stage createStage() {
        return new DialogStage(viewport, batch);
    }

    void setBackScreen(Screen screen) {
        backScreen = screen;
    }

    void setMenuCursorDisabled(boolean disabled) {
        menuCursor.setDisabled(disabled);
    }

    @Override
    public void show() {
        super.show();
        menuCursor.setCurrentItem(firstMenuCursorItem);
    }

    @Override
    public void draw() {
        backScreen.draw();
        super.draw();
    }

    void setButtonText(int index, String text) {
        buttons[index].setText(text);
    }

    void setButtonCount(int count) {
        buttonCount = count;
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setVisible(i < count);
        }
    }

    void setText(String text) {
        textLabel.setText(text);
    }

    public void setHeight(float height) {
        table.setHeight(height);
    }

    void setListener(DialogListener listener) {
        this.listener = listener;
    }

    @Override
    public void resize(int width, int height) {
        backScreen.resize(width, height);
        super.resize(width, height);
        bgImage.setSize(stage.getWidth(), stage.getHeight());
        adjustLayout();
        Gdx.input.setCursorCatched(false);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        menuCursor.update();
        if ((engine.getGameController().isMenuPressed() || engine.getGameController().isBPressed()) && listener != null) {
            listener.dialogButtonClicked(0);
        }

    }

    void adjustLayout() {
        table.setWidth(stage.getWidth() - 100);
        table.setX(50);
        table.setY((stage.getHeight() - table.getHeight()) / 2f);
        float y = table.getY() - 50;
        float x = (stage.getWidth() - 260 * buttonCount) / 2f + 5f;
        for (int i = 0; i < buttonCount; i++) {
            buttons[i].setPosition(x, y);
            x += 260;
        }
    }

    @Override
    public void moveIn() {
        bgImage.addAction(Actions.fadeIn(0.15f));
        group.setY(table.getHeight() * 2f);
        group.addAction(Actions.moveTo(0, 0, 0.15f, Interpolation.pow2Out));
    }

    void moveOut() {
        bgImage.addAction(Actions.fadeOut(0.15f));
        group.addAction(Actions.sequence(
            Actions.moveTo(0, -table.getHeight() * 2f, 0.15f, Interpolation.pow2In),
            switchToBackScreenAction
        ));
    }

}
