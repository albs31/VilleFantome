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

    // --- PAUSE MENU VARIABLES ---
    private enum State { RUNNING, PAUSED }
    private State state = State.RUNNING;
    private Texture pauseBg;
    private Texture resumeTex;
    private Texture quitTex;
    private Rectangle resumeBounds;
    private Rectangle quitBounds;
    private Vector3 touchPoint;

    // --- DIALOGUE VARIABLES ---
    private Texture dialogue1;
    private Texture dialogue2;
    private Texture currentNarratorBox;
    private Sound typeSound;
    private int sceneState = 1;
    private float soundTimer = 0.0F;
    private boolean soundPlaying = true;

    // --- PLAYER & WORLD VARIABLES ---
    private Texture backgroundTexture;
    private Texture playerIdle;
    private Texture[] leftFrames;
    private Texture[] rightFrames;
    private Texture currentPlayerTexture;
    private float playerX = 600.0F;
    private float playerY = 20.0F;
    private float playerSpeed = 300.0F;
    private float animationTimer = 0.0F;
    private float frameDuration = 0.15F;

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

        // Assets - Fixed names to match your 'assets' folder
        dialogue1 = new Texture("Narrator_box1.png");
        dialogue2 = new Texture("scrapped letter 2.png");
        currentNarratorBox = dialogue1;
        typeSound = Gdx.audio.newSound(Gdx.files.internal("NarratorTypeSound.mp3"));
        typeSound.play(1.0F);

        backgroundTexture = new Texture("background1.png");
        playerIdle = new Texture("standing_still.png");
        
        // Pause Menu Assets
        pauseBg = new Texture("pause_screen.png");
        resumeTex = new Texture("resumebutton.png");
        quitTex = new Texture("quitbutton.png");
        resumeBounds = new Rectangle(540, 400, 200, 80);
        quitBounds = new Rectangle(540, 280, 200, 80);
        touchPoint = new Vector3();

        // Animation Frames
        leftFrames = new Texture[]{new Texture("left(1).png"), new Texture("left(2).png"), new Texture("left(3).png")};
        rightFrames = new Texture[]{new Texture("right(1).png"), new Texture("right(2).png"), new Texture("right(3).png")};
        currentPlayerTexture = playerIdle;
    }

    @Override
    public void render(float delta) {
        // 1. ESCAPE COMMAND
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (state == State.RUNNING) {
                state = State.PAUSED;
                typeSound.pause();
            } else {
                state = State.RUNNING;
                typeSound.resume();
            }
        }

        // 2. LOGIC BRANCHING
        if (state == State.RUNNING) {
            updateGame(delta);
        } else {
            handlePauseInput();
        }

        // 3. DRAWING
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        if (sceneState == 3) {
            batch.draw(backgroundTexture, 0, 0, 1280, 720);
            batch.draw(currentPlayerTexture, playerX, playerY, 550, 400);
        }
        if (sceneState < 3) {
            batch.draw(currentNarratorBox, 0, 0, 1280, 720);
        }

        // DRAW PAUSE OVERLAY LAST
        if (state == State.PAUSED) {
            batch.draw(pauseBg, 0, 0, 1280, 720);
            batch.draw(resumeTex, resumeBounds.x, resumeBounds.y, resumeBounds.width, resumeBounds.height);
            batch.draw(quitTex, quitBounds.x, quitBounds.y, quitBounds.width, quitBounds.height);
        }
        batch.end();
    }

    private void updateGame(float delta) {
        // Sound & Dialogue Logic
        if (soundPlaying) {
            soundTimer += delta;
            if (soundTimer > 4.0F) { typeSound.stop(); soundPlaying = false; }
        }

        if (Gdx.input.justTouched()) {
            if (sceneState == 1) {
                sceneState = 2;
                currentNarratorBox = dialogue2;
                typeSound.stop();
                soundPlaying = false;
            } else if (sceneState == 2) {
                sceneState = 3;
            }
        }

        // Movement Logic
        if (sceneState == 3) {
            boolean moving = false;
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                playerX -= playerSpeed * delta;
                animationTimer += delta;
                currentPlayerTexture = leftFrames[(int)(animationTimer / frameDuration) % 3];
                moving = true;
            } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                playerX += playerSpeed * delta;
                animationTimer += delta;
                currentPlayerTexture = rightFrames[(int)(animationTimer / frameDuration) % 3];
                moving = true;
            }
            if (!moving) { currentPlayerTexture = playerIdle; animationTimer = 0; }
            // playerX = Math.max(0, Math.min(playerX, 1280 - 550));
        }
    }

    private void handlePauseInput() {
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

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        batch.dispose();
        dialogue1.dispose();
        dialogue2.dispose();
        backgroundTexture.dispose();
        playerIdle.dispose();
        pauseBg.dispose();
        resumeTex.dispose();
        quitTex.dispose();
        typeSound.dispose();
        for (Texture t : leftFrames) t.dispose();
        for (Texture t : rightFrames) t.dispose();
    }
}