package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class LoreScreen implements Screen {
    private Main game;
    private SpriteBatch batch;
    private Stage stage;
    private Texture[] pages;
    private int currentPage = 0;
    private boolean inputSet = false;

    public LoreScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        stage = new Stage(new FitViewport(1280, 720));

        pages = new Texture[] {
            new Texture("spoilerwarning.png"), // shown first
            new Texture("lore1.png"),
            new Texture("lore2.png"),
            new Texture("lore3.png"),
            new Texture("lore4.png"),
            new Texture("lore5.png"),
            new Texture("lore6.png")
        };

        currentPage = 0;
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

        // ESC always returns to loading screen
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new LoadingScreen(game));
            return;
        }

        // Click or right arrow to advance
        if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            if (currentPage < pages.length - 1) {
                currentPage++;
            } else {
                game.setScreen(new LoadingScreen(game)); // end of lore → back to menu
            }
        }

        // Left arrow to go back
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) && currentPage > 0) {
            currentPage--;
        }

        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();
        batch.draw(pages[currentPage], 0, 0, 1280, 720);
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
        stage.dispose();
        for (Texture t : pages) t.dispose();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        inputSet = false;
    }

    @Override public void pause() {}
    @Override public void resume() {}
}