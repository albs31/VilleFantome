package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen implements Screen {
    private Main game;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;

    // Game States
    private enum State { RUNNING, PAUSED }
    private State state = State.RUNNING;

    // Assets - Fixed to match your filenames exactly
    private Texture dialogue1, dialogue2, currentNarratorBox;
    private Texture pauseBg, resumeTex, quitTex;
    private Sound typeSound;

    // Logic Variables
    private int sceneState = 1; 
    private float soundTimer = 0;
    private boolean soundPlaying = true;

    // Input Hitboxes for the Pause Menu
    private Rectangle resumeBounds, quitBounds;
    private Vector3 touchPoint;

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

        // ASSET LOADING: Matches your Explorer sidebar exactly
        dialogue1 = new Texture("Narrator_box1.png"); 
        dialogue2 = new Texture("scrapped letter 3.png"); 
        currentNarratorBox = dialogue1;

        pauseBg = new Texture("pause screen.png"); 
        resumeTex = new Texture("resumebutton.png"); // Ensure these exist in assets
        quitTex = new Texture("quitbutton.png");

        // Button Positions (Adjust x, y to match your pause screen art)
        resumeBounds = new Rectangle(540, 400, 200, 80);
        quitBounds = new Rectangle(540, 280, 200, 80);
        touchPoint = new Vector3();

        typeSound = Gdx.audio.newSound(Gdx.files.internal("NarratorTypeSound.mp3"));
        typeSound.play(1.0f);
    }

    @Override
    public void render(float delta) {
        // ESC Toggle Logic
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (state == State.RUNNING) {
                state = State.PAUSED;
                typeSound.pause();
            } else {
                state = State.RUNNING;
                typeSound.resume();
            }
        }

        // Branching Logic
        if (state == State.RUNNING) {
            updateRunning(delta);
        } else {
            updatePaused();
        }

        draw();
    }

    private void updateRunning(float delta) {
        // Stops typewriter sound after a few seconds
        if (soundPlaying) {
            soundTimer += delta;
            if (soundTimer > 2.5f) { 
                typeSound.stop();
                soundPlaying = false;
            }
        }

        // Advance dialogue on click
        if (Gdx.input.justTouched()) {
            if (sceneState == 1) {
                sceneState = 2;
                currentNarratorBox = dialogue2;
                typeSound.stop();
                soundPlaying = false;
            } else if (sceneState == 2) {
                sceneState = 3; // Moves to blank screen state
            }
        }
    }

    private void updatePaused() {
        if (Gdx.input.justTouched()) {
            touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchPoint);

            if (resumeBounds.contains(touchPoint.x, touchPoint.y)) {
                state = State.RUNNING;
                typeSound.resume();
            } else if (quitBounds.contains(touchPoint.x, touchPoint.y)) {
                game.setScreen(new LoadingScreen(game)); 
            }
        }
    }

    private void draw() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        // Render Game
        if (sceneState < 3) {
            batch.draw(currentNarratorBox, 0, 0, 1280, 720);
        }

        // Render Pause Menu Overlay
        if (state == State.PAUSED) {
            batch.draw(pauseBg, 0, 0, 1280, 720);
            batch.draw(resumeTex, resumeBounds.x, resumeBounds.y, resumeBounds.width, resumeBounds.height);
            batch.draw(quitTex, quitBounds.x, quitBounds.y, quitBounds.width, quitBounds.height);
        }
        batch.end();
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }

    @Override
    public void dispose() {
        batch.dispose();
        dialogue1.dispose();
        dialogue2.dispose();
        pauseBg.dispose();
        resumeTex.dispose();
        quitTex.dispose();
        typeSound.dispose();
    }

    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}