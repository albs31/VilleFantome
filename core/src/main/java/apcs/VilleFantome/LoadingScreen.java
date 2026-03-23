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

    public LoadingScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        background = new Texture("finalloading.png");

        stage = new Stage(new FitViewport(1280, 720));
        Gdx.input.setInputProcessor(stage);

        // --- Play Button ---
        playTex = new Texture("play_button.png");
        ImageButton playButton = new ImageButton(
            new TextureRegionDrawable(new TextureRegion(playTex))
        );
        playButton.setPosition(327, 305);
        playButton.setSize(155, 72);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new WarningScreen(game));
            }
        });

        // --- Save Button ---
        saveTex = new Texture("save_button.png");
        ImageButton saveButton = new ImageButton(
            new TextureRegionDrawable(new TextureRegion(saveTex))
        );
        saveButton.setPosition(327, 220);
        saveButton.setSize(155, 72);
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new WarningScreen(game)); // replace with SaveScreen(game) when ready
            }
        });

        // --- Lore Button ---
        loreTex = new Texture("lore_button.png");
        ImageButton loreButton = new ImageButton(
            new TextureRegionDrawable(new TextureRegion(loreTex))
        );
        loreButton.setPosition(327, 126);
        loreButton.setSize(155, 72);
        loreButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new WarningScreen(game)); // replace with LoreScreen(game) when ready
            }
        });

        stage.addActor(playButton);
        stage.addActor(saveButton);
        stage.addActor(loreButton);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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
    }

    @Override public void pause() {}
    @Override public void resume() {}
}
