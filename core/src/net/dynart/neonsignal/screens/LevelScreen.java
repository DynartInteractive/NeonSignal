package net.dynart.neonsignal.screens;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Align;

import net.dynart.neonsignal.core.LevelManager;
import net.dynart.neonsignal.core.World;
import net.dynart.neonsignal.core.ui.MenuCursor;
import net.dynart.neonsignal.core.ui.MenuCursorItem;
import net.dynart.neonsignal.core.ui.MenuCursorItemLevelData;
import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.Level;
import net.dynart.neonsignal.core.User;
import net.dynart.neonsignal.ui.LevelButton;

public class LevelScreen extends MenuScreen {

    private static final float WORLD_WIDTH = 900f;

    private final User user;
    private final GameScreen gameScreen;
    private final List<World> worlds;
    private final Map<String, LevelButton> levelButtons = new HashMap<>();
    private final Action startLevelAction;

    private Table headerTable;
    private Button slideLeftButton;
    private Button slideRightButton;
    private Group worldGroup;
    private Label headerLabel;
    private float startDragX;
    private float lastX;
    private int currentWorldIndex;
    private int lastDir;
    private Action backAction;
    private MenuCursor.Listener goToNextWorldListener;
    private MenuCursor.Listener goToPrevWorldListener;
    private MenuCursor.Listener levelSelectListener;
    private LevelButton firstButton;
    private Level levelToStart;
    private LevelButton lastSelectedLevelButton;

    public LevelScreen(final Engine engine) {
        super(engine);
        LevelManager levelManager = engine.getLevelManager();
        user = engine.getUser();
        worlds = levelManager.getWorlds();
        gameScreen = (GameScreen)engine.getScreen("game");

        group.addActor(menuCursor.getCursorImage());

        createHeader();
        createWorldGroup();
        createSliderButtons();
        createBackButton();

        addSideBlackBars(stage);

        setUpCursor();

        startLevelAction = new Action() {
            @Override
            public boolean act(float delta) {
                startLevel();
                return true;
            }
        };

        moveTo(0);
    }

    @Override
    public void show() {
        super.show();
        engine.getSoundManager().playMusic("main");
    }

    private void createHeader() {
        headerLabel = new Label("Title", styles.getDefaultLabelStyle());
        headerLabel.setAlignment(Align.center);
        headerTable = new Table();
        headerTable.setBackground(skin.getDrawable("header_bg"));
        headerTable.add(headerLabel).expand().fill().row();
        headerTable.setHeight(110);
        group.addActor(headerTable);
        headerTable.toBack();
    }

    private String getLevelHash(int worldIndex, int levelIndex) {
        return worldIndex + "_" + levelIndex;
    }

    private void createWorldGroup() {
        worldGroup = new Group();
        for (int worldIndex = 0; worldIndex < worlds.size(); worldIndex++) {
            World world = worlds.get(worldIndex);
            List<Level> levels = world.getLevels();
            for (int levelIndex = 0; levelIndex < levels.size(); levelIndex++) {
                Level level = levels.get(levelIndex);
                LevelButton levelButton = createLevelButton(level, worldIndex * WORLD_WIDTH, levelIndex, levels.size());
                worldGroup.addActor(levelButton);
                levelButtons.put(getLevelHash(worldIndex, levelIndex), levelButton);
            }
        }
        stage.addListener(createDragListener());
        group.addActor(worldGroup);
    }

    private void setUpCursor() {
        MenuCursorItem item = menuCursor.addItem(backButton);
        item.setListener(MenuCursor.Event.DOWN, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                menuCursor.setCurrentItem(lastSelectedLevelButton);
            }
        });
        item.setListener(MenuCursor.Event.ENTER, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                menuCursor.setCurrentItem(levelButtons.get(getLevelHash(0, 0)));
                backClicked();
            }
        });
        levelSelectListener = new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                if (isAnimating()) { return; }
                MenuCursorItemLevelData data = (MenuCursorItemLevelData)item.getData();
                Level level = data.getLevel();
                if (user.isLevelUnlocked(level)) {
                    levelToStart = level;
                    fadeOut(startLevelAction);
                }
            }
        };
        goToNextWorldListener = new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                moveCursorTo(item, 1);
            }
        };
        goToPrevWorldListener = new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                moveCursorTo(item, -1);
            }
        };
        for (int worldIndex = 0; worldIndex < worlds.size(); worldIndex++) {
            World world = worlds.get(worldIndex);
            List<Level> levels = world.getLevels();
            for (int levelIndex = 0; levelIndex < levels.size(); levelIndex++) {
                createCursorItem(worldIndex, levels, levelIndex);
            }
        }
    }

    private void createCursorItem(int worldIndex, List<Level> levels, int levelIndex) {
        final Level level = levels.get(levelIndex);
        final LevelButton levelButton = levelButtons.get(getLevelHash(worldIndex, levelIndex));
        MenuCursorItem item = menuCursor.addItem(levelButton);
        item.setData(new MenuCursorItemLevelData(level, worldIndex, levelIndex));
        item.setListener(MenuCursor.Event.ENTER, levelSelectListener);
        if (firstButton == null) {
            firstButton = levelButton;
            menuCursor.setCurrentItem(item);
        }

        int totalLevels = levels.size();

        // Calculate row/column layout (same logic as createLevelButton)
        int row, col, buttonsInRow1, buttonsInRow2;
        if (totalLevels <= 5) {
            // Single row (1-5 levels)
            row = 0;
            col = levelIndex;
            buttonsInRow1 = totalLevels;
            buttonsInRow2 = 0;
        } else {
            // Two rows (6-10 levels) - distribute evenly, top row gets more if odd
            buttonsInRow1 = (totalLevels + 1) / 2;
            buttonsInRow2 = totalLevels / 2;

            if (levelIndex < buttonsInRow1) {
                row = 0;
                col = levelIndex;
            } else {
                row = 1;
                col = levelIndex - buttonsInRow1;
            }
        }

        int buttonsInCurrentRow = (row == 0) ? buttonsInRow1 : buttonsInRow2;

        // RIGHT navigation
        if (col < buttonsInCurrentRow - 1) {
            // Not at the right edge of this row
            String neighbourHash = getLevelHash(worldIndex, levelIndex + 1);
            if (levelButtons.containsKey(neighbourHash)) {
                item.setNeighbour(MenuCursor.Neighbour.RIGHT, levelButtons.get(neighbourHash));
            }
        } else {
            // At the right edge, allow world transition
            item.setListener(MenuCursor.Event.RIGHT, goToNextWorldListener);
        }

        // LEFT navigation
        if (col > 0) {
            // Not at the left edge of this row
            String neighbourHash = getLevelHash(worldIndex, levelIndex - 1);
            if (levelButtons.containsKey(neighbourHash)) {
                item.setNeighbour(MenuCursor.Neighbour.LEFT, levelButtons.get(neighbourHash));
            }
        } else {
            // At the left edge, allow world transition
            item.setListener(MenuCursor.Event.LEFT, goToPrevWorldListener);
        }

        // DOWN navigation
        if (row == 0 && totalLevels > 3) {
            // On top row with two rows total - go to bottom row
            int targetIndex = levelIndex + buttonsInRow1;
            if (targetIndex < totalLevels) {
                String neighbourHash = getLevelHash(worldIndex, targetIndex);
                item.setNeighbour(MenuCursor.Neighbour.DOWN, levelButtons.get(neighbourHash));
            }
        }

        // UP navigation
        if (row > 0) {
            // On bottom row - go to top row
            int targetIndex = levelIndex - buttonsInRow1;
            String neighbourHash = getLevelHash(worldIndex, targetIndex);
            item.setNeighbour(MenuCursor.Neighbour.UP, levelButtons.get(neighbourHash));
        } else {
            // On the first row, go to back button
            item.setListener(MenuCursor.Event.UP, new MenuCursor.Listener() {
                @Override
                public void handle(MenuCursorItem item) {
                    lastSelectedLevelButton = levelButton;
                    menuCursor.setCurrentItem(backButton);
                }
            });
        }
    }

    private void moveCursorTo(MenuCursorItem item, int direction) {
        MenuCursorItemLevelData data = (MenuCursorItemLevelData)item.getData();
        int worldIndex = data.getWorldIndex();
        int nextWorldIndex = worldIndex + direction;
        if (nextWorldIndex < worlds.size() && nextWorldIndex > -1) {
            int levelIndex = data.getLevelIndex();
            World currentWorld = worlds.get(worldIndex);
            List<Level> currentLevels = currentWorld.getLevels();
            World nextWorld = worlds.get(nextWorldIndex);
            List<Level> nextLevels = nextWorld.getLevels();

            // Calculate current position
            int currentRow, currentCol, currentButtonsInRow1;
            if (currentLevels.size() <= 5) {
                currentRow = 0;
                currentCol = levelIndex;
                currentButtonsInRow1 = currentLevels.size();
            } else {
                currentButtonsInRow1 = (currentLevels.size() + 1) / 2;
                if (levelIndex < currentButtonsInRow1) {
                    currentRow = 0;
                    currentCol = levelIndex;
                } else {
                    currentRow = 1;
                    currentCol = levelIndex - currentButtonsInRow1;
                }
            }

            // Calculate next world layout
            int nextButtonsInRow1, nextButtonsInRow2;
            if (nextLevels.size() <= 5) {
                nextButtonsInRow1 = nextLevels.size();
                nextButtonsInRow2 = 0;
            } else {
                nextButtonsInRow1 = (nextLevels.size() + 1) / 2;
                nextButtonsInRow2 = nextLevels.size() / 2;
            }

            // Calculate target position in next world
            int nextLevelIndex;
            if (direction > 0) {
                // Moving right: go to leftmost column (0) in the same row
                if (currentRow == 0) {
                    nextLevelIndex = 0;
                } else if (nextLevels.size() > 3) {
                    nextLevelIndex = nextButtonsInRow1; // Start of row 2
                } else {
                    nextLevelIndex = 0; // Only one row
                }
            } else {
                // Moving left: go to rightmost available column in the same row
                if (currentRow == 0) {
                    nextLevelIndex = nextButtonsInRow1 - 1;
                } else if (nextLevels.size() > 3) {
                    nextLevelIndex = nextButtonsInRow1 + nextButtonsInRow2 - 1; // End of row 2
                } else {
                    nextLevelIndex = nextLevels.size() - 1; // End of single row
                }
            }

            // Ensure the target index is valid
            if (nextLevelIndex >= nextLevels.size()) {
                nextLevelIndex = nextLevels.size() - 1;
            }

            String nextHash = getLevelHash(nextWorldIndex, nextLevelIndex);
            menuCursor.setCurrentItem(levelButtons.get(nextHash));
            moveTo(nextWorldIndex);
        }
    }

    private LevelButton createLevelButton(final Level level, float offsetX, int levelIndex, int totalLevels) {
        LevelButton levelButton = new LevelButton(engine, level, levelIndex + 1);

        // Calculate layout dimensions
        int buttonSpacingX = 140;
        int buttonSpacingY = 165;
        float buttonWidth = 127; // LevelButton width - need to account for anchor at bottom-left

        // Determine row/column layout based on total levels
        int row, col, buttonsInRow1, buttonsInRow2;
        if (totalLevels <= 6) {
            // Single row (1-3 levels)
            row = 0;
            col = levelIndex;
            buttonsInRow1 = totalLevels;
            buttonsInRow2 = 0;
        } else {
            // Two rows (4-10 levels) - distribute evenly, top row gets more if odd
            buttonsInRow1 = (totalLevels + 1) / 2; // Ceiling of half
            buttonsInRow2 = totalLevels / 2;       // Floor of half

            if (levelIndex < buttonsInRow1) {
                row = 0;
                col = levelIndex;
            } else {
                row = 1;
                col = levelIndex - buttonsInRow1;
            }
        }

        // Get number of buttons in current row
        int buttonsInThisRow = (row == 0) ? buttonsInRow1 : buttonsInRow2;

        // Center each row based on its button count
        // Account for button anchor being at bottom-left by subtracting half width
        float rowOffsetX = -(buttonsInThisRow - 1) * buttonSpacingX / 2f;
        float x = col * buttonSpacingX + rowOffsetX + offsetX - buttonWidth / 2f;

        // Position vertically - weighted toward bottom to balance with title
        float y;
        if (totalLevels <= 5) {
            // Single row (1-3 levels) - position lower for visual balance with title
            y = -127.5f; // Centered between original top (-45) and bottom (-210)
        } else {
            // Two rows (4-10 levels) - use original positioning
            y = (1 - row) * buttonSpacingY - 210;
        }

        levelButton.setX(x);
        levelButton.setY(y);
        levelButton.setUnlocked(user.isLevelUnlocked(level));
        levelButton.setStars(user.getStars(level));
        levelButton.addClickListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (isAnimating()) { return; }
                levelToStart = level;
                fadeOut(startLevelAction);
            }
        });
        return levelButton;
    }

    private void startLevel() {
        if (isAnimating()) { return; }
        gameScreen.loadLevel(levelToStart);
        gameScreen.fadeIn();
        engine.moveToScreen("game");
    }

    private DragListener createDragListener() {
        return new DragListener() {
            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                if (isAnimating()) { return; }
                startDragX = x - worldGroup.getX();
                lastX = startDragX;
                worldGroup.clearActions();
                for (LevelButton levelButton : levelButtons.values()) {
                    levelButton.setDisabled(true);
                }

            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                if (isAnimating()) { return; }
                if (lastX != worldGroup.getX()) {
                    lastDir = (int)Math.signum(worldGroup.getX() - lastX);
                }
                lastX = worldGroup.getX();
                worldGroup.setX(x - startDragX);
            }

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                if (isAnimating()) { return; }
                for (LevelButton levelButton : levelButtons.values()) {
                    levelButton.setDisabled(false);
                }
                moveTo(currentWorldIndex - lastDir);
            }
        };
    }

    private void createSliderButtons() {
        slideLeftButton = new Button(skin.getDrawable("button_slide_left"));
        slideLeftButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                moveTo(currentWorldIndex - 1);
            }
        });
        group.addActor(slideLeftButton);

        slideRightButton = new Button(skin.getDrawable("button_slide_right"));
        slideRightButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                moveTo(currentWorldIndex + 1);
            }
        });
        group.addActor(slideRightButton);
    }

    private void createBackButton() {
        backAction = new Action() {
            @Override
            public boolean act(float delta) {
                engine.moveToScreen("menu");
                return true;
            }
        };
        group.addActor(backButton);
    }

    @Override
    public void backClicked() {
        if (isAnimating()) { return; }
        moveOut(backAction);
    }

    private void moveTo(int nextWorldIndex) {
        if (nextWorldIndex < 0) {
            nextWorldIndex = 0;
        } else if (nextWorldIndex >= worlds.size()){
            nextWorldIndex = worlds.size() - 1;
        }
        currentWorldIndex = nextWorldIndex;
        worldGroup.addAction(Actions.moveTo(-currentWorldIndex * WORLD_WIDTH, worldGroup.getY(), 0.2f, Interpolation.pow2Out));
        slideLeftButton.setVisible(currentWorldIndex != 0);
        slideRightButton.setVisible(currentWorldIndex != worlds.size() - 1);
        headerLabel.setText(worlds.get(currentWorldIndex).getTitle());
        updateArrowPositions();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        float sideBlackBarWidth = getSideBlackBarWidth();
        headerTable.setWidth(stage.getWidth() - sideBlackBarWidth * 2);
        headerTable.setX(-stage.getWidth() / 2f + sideBlackBarWidth);

        // Align arrows vertically with center of level button area
        updateArrowPositions();
    }

    private void updateArrowPositions() {
        // Calculate center Y of level button area
        // Level buttons are positioned around -127.5f (single row) or -45 to -210 (two rows)
        // Use -127.5f as the vertical center for arrow positioning
        float centerY = -127.5f;

        float arrowY = centerY - slideLeftButton.getHeight() / 2f;
        slideLeftButton.setX(getLeftButtonX());
        slideLeftButton.setY(arrowY);
        slideRightButton.setX(getRightButtonX());
        slideRightButton.setY(arrowY);
    }

    private float getLeftButtonX() {
        // Group origin is at screen center, so left edge is at -stage.getWidth()/2f
        return -stage.getWidth()/2f + 20f + getSideBlackBarWidth();
    }

    private float getRightButtonX() {
        // Group origin is at screen center, so right edge is at +stage.getWidth()/2f
        return stage.getWidth()/2f - slideRightButton.getWidth() - 20f - getSideBlackBarWidth();
    }

    @Override
    public void moveIn() {
        if (isAnimating()) { return; }
        moving = true;
        headerTable.setY(450);
        headerTable.addAction(Actions.moveTo(headerTable.getX(), 170, 0.15f, Interpolation.sineOut));
        worldGroup.setY(-450);
        worldGroup.addAction(Actions.moveTo(worldGroup.getX(), 0, 0.15f, Interpolation.sineOut));
        slideLeftButton.setX(getLeftButtonX() - 100f);
        slideLeftButton.addAction(Actions.moveTo(getLeftButtonX(), slideLeftButton.getY(), 0.15f, Interpolation.sineOut));
        slideRightButton.setX(getRightButtonX() + 100f);
        slideRightButton.addAction(Actions.moveTo(getRightButtonX(), slideRightButton.getY(), 0.15f, Interpolation.sineOut));
        stage.addAction(Actions.sequence(Actions.delay(0.16f), movingFinishedAction));
    }

    private void moveOut(Action endAction) {
        if (isAnimating()) { return; }
        moving = true;
        headerTable.addAction(Actions.moveTo(headerTable.getX(), 450, 0.15f, Interpolation.sineIn));
        worldGroup.addAction(Actions.moveTo(worldGroup.getX(), -450, 0.15f, Interpolation.sineIn));
        slideLeftButton.addAction(Actions.moveTo(getLeftButtonX() - 100f, slideLeftButton.getY(), 0.15f, Interpolation.sineIn));
        slideRightButton.addAction(Actions.moveTo(getRightButtonX() + 100f, slideRightButton.getY(), 0.15f, Interpolation.sineIn));
        stage.addAction(Actions.sequence(Actions.delay(0.16f), movingFinishedAction, endAction));
    }

}
