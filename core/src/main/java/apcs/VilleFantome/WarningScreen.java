package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
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
    
    // Add these to handle the "Stretch" fix
    private OrthographicCamera camera;
    private Viewport viewport;

    public WarningScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        warningTexture = new Texture("warningscreen.png");

        // 1. Setup Camera and Viewport (matching LoadingScreen's 1280x720)
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera); // FitViewport adds black bars to prevent stretching
        viewport.apply();
        camera.position.set(640, 360, 0);
    }

    @Override
public void render(float delta) {
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    camera.update();
    batch.setProjectionMatrix(camera.combined);

    batch.begin();
    batch.draw(warningTexture, 0, 0, 1280, 720);
    batch.end();

    // This detects a click/touch anywhere on the screen
    if (Gdx.input.justTouched()) {
        game.setScreen(new GameScreen(game)); 
    }
    }

    @Override
    public void resize(int width, int height) {
        // 4. Critical: This updates the viewport when the user drags the window corner
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