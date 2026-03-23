package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen implements Screen {
    private Main game;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture dialogue1;
    private Texture dialogue2;
    private Texture currentNarratorBox;
    private Sound typeSound;

    private int sceneState = 1; 
    private float soundTimer = 0; // Timer to track sound duration
    private boolean soundPlaying = true;

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        viewport.apply();
        camera.position.set(640, 360, 0);

        dialogue1 = new Texture("Narrator_box1.png"); 
        dialogue2 = new Texture("scrapped letter 3.png"); 
        currentNarratorBox = dialogue1;

        typeSound = Gdx.audio.newSound(Gdx.files.internal("NarratorTypeSound.mp3"));
        typeSound.play(1.0f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // SOUND TIMER LOGIC
        // This stops the sound after 2 seconds, even if the user hasn't clicked yet
        if (soundPlaying) {
            soundTimer += delta;
            if (soundTimer > 4.0f) { 
                typeSound.stop();
                soundPlaying = false;
            }
        }
        // CLICK LOGIC
        if (Gdx.input.justTouched()) {
            if (sceneState == 1) {
                sceneState = 2;
                currentNarratorBox = dialogue2;
                typeSound.stop(); // Stop sound immediately if they click early
                soundPlaying = false; 
            } else if (sceneState == 2) {
                sceneState = 3;
            }
        }

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        if (sceneState < 3) {
            batch.draw(currentNarratorBox, 0, 0, 1280, 720);
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        dialogue1.dispose();
        dialogue2.dispose();
        if (typeSound != null) typeSound.dispose();
    }

    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}