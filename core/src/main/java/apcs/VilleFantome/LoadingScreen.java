package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;


public class LoadingScreen implements Screen {

    private SpriteBatch batch;
    private Texture temploading;
    private OrthographicCamera camera;
    private Viewport viewport;

    @Override
    public void show() {

        batch = new SpriteBatch();
        temploading = new Texture("resizedtemploading.png");

        camera = new OrthographicCamera();
        viewport = new com.badlogic.gdx.utils.viewport.FillViewport(1280, 720, camera);

        viewport.apply();
        camera.position.set(640, 360, 0);
        camera.update();
    }

    @Override
    public void render(float delta) {

        viewport.apply(); // VERY IMPORTANT

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(temploading, 0, 0, 1280, 720);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        temploading.dispose();
    }
}