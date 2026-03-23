package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
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

    private int sceneState = 1; 
    private float soundTimer = 0; // Timer to track sound duration
    private boolean soundPlaying = true;

    // Input Hitboxes for the Pause Menu
    private Rectangle resumeBounds, quitBounds;
    private Vector3 touchPoint;

   
    private Texture backgroundTexture;
    private Texture playerIdle;
    private Texture[] leftFrames;
    private Texture[] rightFrames;
    private Texture currentPlayerTexture;

   
    private float playerX;
    private float playerY;
    private float playerSpeed = 300.0F;

   
    private float animationTimer = 0.0F;
    private float frameDuration = 0.15F;

    public GameScreen(Main game) {
        this.game = game;
    }

    public void show() {
        this.batch = new SpriteBatch();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(1280.0F, 720.0F, this.camera);
        this.viewport.apply();
        this.camera.position.set(640.0F, 360.0F, 0.0F);

        dialogue1 = new Texture("Narrator_box1.png"); 
        dialogue2 = new Texture("scrapped letter 2.png"); 
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

            // player on screen lol
           this.playerX = Math.max(0.0F, Math.min(this.playerX, 1280.0F - 500.0F));
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
        if (sceneState < 3) {
            batch.draw(currentNarratorBox, 0, 0, 1280, 720);
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) viewport.update(width, height, true);
    }

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
