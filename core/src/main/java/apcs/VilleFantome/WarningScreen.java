package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class WarningScreen implements Screen {
    private Main game;
    private SpriteBatch batch;
    private Texture warningTexture;
    private OrthographicCamera camera;
    private Viewport viewport;

    public WarningScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        warningTexture = new Texture("warningscreen.png");

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280.0F, 720.0F, camera);
        viewport.apply();
        camera.position.set(640.0F, 360.0F, 0.0F);

        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(warningTexture, 0.0F, 0.0F, 1280.0F, 720.0F);
        batch.end();

       if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            Gdx.input.setInputProcessor(null);
            // Added 10, 20 as the starting coordinates for the first time Theo spawns
            game.setScreen(new GameScreen(game, false, 10, 20)); 
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        warningTexture.dispose();
    }

    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}
