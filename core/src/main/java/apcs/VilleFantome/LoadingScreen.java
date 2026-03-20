package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class LoadingScreen implements Screen {
    private Main game;
    private SpriteBatch batch;
    private Texture background;
    private Texture debugTex; // For the highlight
    private OrthographicCamera camera;
    private Viewport viewport;
    private Rectangle playButtonBounds;
    private Vector3 touchPoint;

    public LoadingScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        background = new Texture("finalloading.png");
        
        // Create a 1x1 white texture for the debug box
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, 1);
        pixmap.fill();
        debugTex = new Texture(pixmap);
        pixmap.dispose();

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        viewport.apply();
        camera.position.set(640, 360, 0);

        // ADJUST THESE to move the box: (x, y, width, height)
        // show() method in LoadingScreen.java
// Increase X to move right, Decrease Y to move down
playButtonBounds = new Rectangle(330, 307, 150, 60); 
        touchPoint = new Vector3();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(background, 0, 0, 1280, 720);

        // --- DEBUG BOX: REMOVE THESE 3 LINES ONCE ALIGNED ---
        batch.setColor(1, 1, 1, 0.4f); // 40% transparent
        batch.draw(debugTex, playButtonBounds.x, playButtonBounds.y, playButtonBounds.width, playButtonBounds.height);
        batch.setColor(1, 1, 1, 1); // Reset transparency
        // ---------------------------------------------------

        batch.end();

        if (Gdx.input.justTouched()) {
            touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchPoint);

            if (playButtonBounds.contains(touchPoint.x, touchPoint.y)) {
                game.setScreen(new WarningScreen(game)); 
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) viewport.update(width, height, true);
    }

    @Override public void dispose() {
        batch.dispose();
        background.dispose();
        debugTex.dispose();
    }
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}