package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class WarningScreen implements Screen {
    private Main game;
    private SpriteBatch batch;
    private Texture warningTexture;
    private Stage stage;
    private boolean inputSet = false;

    public WarningScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        warningTexture = new Texture("warningscreen.png");

        stage = new Stage(new FitViewport(1280, 720));

        // Click anywhere on the screen to continue
        stage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game));
            }
        });

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
        batch.draw(warningTexture, 0, 0, 1280, 720);
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
        warningTexture.dispose();
        stage.dispose();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        inputSet = false;
    }

    @Override public void pause() {}
    @Override public void resume() {}
}
