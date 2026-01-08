package net.dynart.neonsignal.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;

import net.dynart.neonsignal.components.PlayerComponent;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.core.ui.FadeImage;
import net.dynart.neonsignal.core.Engine;

public class CompletedScreen extends MenuScreen {

    private static final float TEXT_COLUMN = -420;
    private static final float NUMBER_COLUMN = -100;
    private static final float PER_COLUMN = 10;
    private static final float FULL_COLUMN = 50;
    private static final float PERCENT_COLUMN = 200;

    private static final float TEXT_WIDTH = 300;
    private static final float NUMBER_WIDTH = 100;
    private static final float FULL_WIDTH = 100;
    private static final float PERCENT_WIDTH = 100;

    private static final float KNOCKOUT_ROW = 70;
    private static final float SECRET_ROW = -70;
    private static final float ITEM_ROW = 0;

    private GameScreen gameScreen;
    private FadeImage bgImage;

    private final Label knockoutNumberLabel;
    private final Label secretNumberLabel;
    private final Label itemNumberLabel;

    private final Label knockoutFullLabel;
    private final Label secretFullLabel;
    private final Label itemFullLabel;

    private final Label knockoutPercentLabel;
    private final Label secretPercentLabel;
    private final Label itemPercentLabel;

    public CompletedScreen(final Engine engine) {
        super(engine);
        clear = false;
        
        Label.LabelStyle ls = styles.getDefaultLabelStyle();

        knockoutNumberLabel = new Label("0", ls);
        knockoutNumberLabel.setAlignment(Align.right);
        knockoutNumberLabel.setWidth(NUMBER_WIDTH);
        knockoutNumberLabel.setPosition(NUMBER_COLUMN, KNOCKOUT_ROW);

        secretNumberLabel = new Label("0", ls);
        secretNumberLabel.setAlignment(Align.right);
        secretNumberLabel.setWidth(NUMBER_WIDTH);
        secretNumberLabel.setPosition(NUMBER_COLUMN, SECRET_ROW);

        itemNumberLabel = new Label("0", ls);
        itemNumberLabel.setAlignment(Align.right);
        itemNumberLabel.setWidth(NUMBER_WIDTH);
        itemNumberLabel.setPosition(NUMBER_COLUMN, ITEM_ROW);

        knockoutFullLabel = new Label("0", ls);
        knockoutFullLabel.setAlignment(Align.left);
        knockoutFullLabel.setWidth(FULL_WIDTH);
        knockoutFullLabel.setPosition(FULL_COLUMN, KNOCKOUT_ROW);

        secretFullLabel = new Label("0", ls);
        secretFullLabel.setAlignment(Align.left);
        secretFullLabel.setWidth(FULL_WIDTH);
        secretFullLabel.setPosition(FULL_COLUMN, SECRET_ROW);

        itemFullLabel = new Label("0", ls);
        itemFullLabel.setAlignment(Align.left);
        itemFullLabel.setWidth(FULL_WIDTH);
        itemFullLabel.setPosition(FULL_COLUMN, ITEM_ROW);

        knockoutPercentLabel = new Label("0%", ls);
        knockoutPercentLabel.setAlignment(Align.right);
        knockoutPercentLabel.setWidth(PERCENT_WIDTH);
        knockoutPercentLabel.setPosition(PERCENT_COLUMN, KNOCKOUT_ROW);

        secretPercentLabel = new Label("0%", ls);
        secretPercentLabel.setAlignment(Align.right);
        secretPercentLabel.setWidth(PERCENT_WIDTH);
        secretPercentLabel.setPosition(PERCENT_COLUMN, SECRET_ROW);

        itemPercentLabel = new Label("0%", ls);
        itemPercentLabel.setAlignment(Align.right);
        itemPercentLabel.setWidth(PERCENT_WIDTH);
        itemPercentLabel.setPosition(PERCENT_COLUMN, ITEM_ROW);

        Label knockoutTextLabel = new Label("Knockout", ls);
        knockoutTextLabel.setAlignment(Align.right);
        knockoutTextLabel.setPosition(TEXT_COLUMN, KNOCKOUT_ROW);
        knockoutTextLabel.setWidth(TEXT_WIDTH);

        Label secretTextLabel = new Label("Secret", ls);
        secretTextLabel.setAlignment(Align.right);
        secretTextLabel.setPosition(TEXT_COLUMN, SECRET_ROW);
        secretTextLabel.setWidth(TEXT_WIDTH);

        Label itemTextLabel = new Label("Item", ls);
        itemTextLabel.setAlignment(Align.right);
        itemTextLabel.setPosition(TEXT_COLUMN, ITEM_ROW);
        itemTextLabel.setWidth(TEXT_WIDTH);

        Label knockoutPerLabel = new Label("/", ls);
        knockoutPerLabel.setPosition(PER_COLUMN, KNOCKOUT_ROW);

        Label secretPerLabel = new Label("/", ls);
        secretPerLabel.setPosition(PER_COLUMN, SECRET_ROW);

        Label itemPerLabel = new Label("/", ls);
        itemPerLabel.setPosition(PER_COLUMN, ITEM_ROW);

        group.addActor(knockoutTextLabel);
        group.addActor(secretTextLabel);
        group.addActor(itemTextLabel);

        group.addActor(knockoutNumberLabel);
        group.addActor(secretNumberLabel);
        group.addActor(itemNumberLabel);

        group.addActor(knockoutPerLabel);
        group.addActor(secretPerLabel);
        group.addActor(itemPerLabel);

        group.addActor(knockoutFullLabel);
        group.addActor(secretFullLabel);
        group.addActor(itemFullLabel);

        group.addActor(knockoutPercentLabel);
        group.addActor(secretPercentLabel);
        group.addActor(itemPercentLabel);

        group.addActor(backButton);

        addSideBlackBars(stage);
    }


    @Override
    public void backClicked() {
        engine.moveToScreen("levels");
    }

    @Override
    public void init() {
        super.init();
        gameScreen = (GameScreen)engine.getScreen("game");
    }

    @Override
    public void show() {
        super.show();
        GameScene gameScene = gameScreen.getScene();
        PlayerComponent player = gameScene.getPlayer().getComponent(PlayerComponent.class);

        knockoutNumberLabel.setText(player.getKnockoutCount());
        secretNumberLabel.setText(player.getSecretCount());
        itemNumberLabel.setText(player.getItemCount());

        knockoutFullLabel.setText(gameScene.getEnemyCount());
        secretFullLabel.setText(gameScene.getSecretCount());
        itemFullLabel.setText(gameScene.getItemCount());

        if (gameScene.getEnemyCount() > 0) {
            int v = (int)((float) player.getKnockoutCount() / (float) gameScene.getEnemyCount() * 100f);
            knockoutPercentLabel.setText(v + "%");
        } else {
            knockoutPercentLabel.setText("n/a");
        }
        if (gameScene.getSecretCount() > 0) {
            int v = (int)((float)player.getSecretCount() / (float)gameScene.getSecretCount() * 100f);
            secretPercentLabel.setText(v + "%");
        } else {
            secretPercentLabel.setText("n/a");
        }
        if (gameScene.getItemCount() > 0) {
            int v = (int)((float)player.getItemCount() / (float)gameScene.getItemCount() * 100f);
            itemPercentLabel.setText(v + "%");
        } else {
            itemPercentLabel.setText("n/a");
        }
    }

    protected void addBackground() {
        bgImage = new FadeImage(skin.getDrawable("dialog_fix_bg"));
        stage.addActor(bgImage);
        bgImage.toBack();
    }

    @Override
    public void draw() {
        gameScreen.updateCamera();
        gameScreen.draw();
        super.draw();
    }

    @Override
    public void resize(int width, int height) {
        gameScreen.resize(width, height);
        bgImage.setSize(stage.getWidth(), stage.getHeight());
        super.resize(width, height);
    }
}
