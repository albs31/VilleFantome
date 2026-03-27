package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class LoadingScreen implements Screen {
    private Main game;
    private SpriteBatch batch;
    private Texture background;
    private Stage stage;
    private Texture playTex, saveTex, loreTex;
    private boolean inputSet = false;

    public LoadingScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        background = new Texture("finalloading.png");
        stage = new Stage(new FitViewport(1280, 720));

        // play button
        playTex = new Texture("play_button.png");
        ImageButton playButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(playTex)));
        playButton.setPosition(327, 305);
        playButton.setSize(155, 72);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                 Inventory.clearItems();
                game.setScreen(new WarningScreen(game));
            }
        });

        // save button
        saveTex = new Texture("save_button.png");
        ImageButton saveButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(saveTex)));
        saveButton.setPosition(327, 220);
        saveButton.setSize(155, 72);
        saveButton.addListener(new ClickListener() {
    @Override
    public void clicked(InputEvent event, float x, float y) {
        if (SaveManager.hasSave()) {
            Inventory.clearItems();
            SaveManager.loadInventory();
            float savedX = SaveManager.loadPlayerX();
            float savedY = SaveManager.loadPlayerY();
            int area = SaveManager.loadArea();
            String room = SaveManager.loadRoomScreen();

            switch (room) {
                case "pawnshop":
                    game.setScreen(new PawnShopScreen(game, savedX, savedY));
                    break;
                case "jhouse":
                    game.setScreen(new JHouseScreen(game, savedX, savedY));
                    break;
                case "mansion":
                    game.setScreen(new Mansion(game, savedX, savedY));
                    break;
                case "previousroom":
                    game.setScreen(new PreviousRoomScreen(game, savedX, savedY));
                    break;
                default: // "town"
                    game.setScreen(new GameScreen(game, true, savedX, savedY, area)); // ← area passed correctly
                    break;
            }
        } else {
            System.out.println("No save file found.");
        }
    }
});

        // lore button
        loreTex = new Texture("lore_button.png");
        ImageButton loreButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(loreTex)));
        loreButton.setPosition(327, 126);
        loreButton.setSize(155, 72);
        loreButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new LoreScreen(game));
            }
        });

        stage.addActor(playButton);
        stage.addActor(saveButton);
        stage.addActor(loreButton);

        inputSet = false;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!inputSet) {
            Gdx.input.setInputProcessor(stage);
            inputSet = true;
        }

        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();
        batch.draw(background, 0, 0, 1280, 720);
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        stage.dispose();
        playTex.dispose();
        saveTex.dispose();
        loreTex.dispose();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        inputSet = false;
    }

    @Override public void pause() {}
    @Override public void resume() {}
}