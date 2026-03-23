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

    private Player player; // Using our new class!
    private Texture backgroundTexture;

    private enum State { RUNNING, PAUSED }
    private State state = State.RUNNING;
    private Texture pauseBg, resumeTex, quitTex;
    private Rectangle resumeBounds, quitBounds;
    private Vector3 touchPoint;

    private Texture dialogue1, dialogue2, currentNarratorBox;
    private Sound typeSound;
    private int sceneState = 1;
    private float soundTimer = 0;
    private boolean soundPlaying = true;

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        camera.position.set(640, 360, 0);

        player = new Player(600, 20); // Initialize player
        backgroundTexture = new Texture("background1.png");

        // UI/Dialogue Assets
        dialogue1 = new Texture("Narrator_box1.png");
        dialogue2 = new Texture("scrapped letter 2.png");
        currentNarratorBox = dialogue1;
        typeSound = Gdx.audio.newSound(Gdx.files.internal("NarratorTypeSound.mp3"));
        typeSound.play(1.0f);

        pauseBg = new Texture("pause_screen.png");
        resumeTex = new Texture("resume_button.png");
        quitTex = new Texture("quitbutton.png");
        resumeBounds = new Rectangle(540, 400, 200, 80);
        quitBounds = new Rectangle(540, 280, 200, 80);
        touchPoint = new Vector3();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            state = (state == State.RUNNING) ? State.PAUSED : State.RUNNING;
            if (state == State.PAUSED) typeSound.pause(); else typeSound.resume();
        }

        if (state == State.RUNNING) {
            updateLogic(delta);
        } else {
            handlePauseInput();
        }

        draw();
    }

    private void updateLogic(float delta) {
        // Sound timer
        if (soundPlaying) {
            soundTimer += delta;
            if (soundTimer > 4.0f) { typeSound.stop(); soundPlaying = false; }
        }

        // Dialogue click-through
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

        // Only move player if we are in the "Game" scene (state 3)
        if (sceneState == 3) {
            player.update(delta);
        }
    }

    private void handlePauseInput() {
        if (Gdx.input.justTouched()) {
            touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchPoint);
            if (resumeBounds.contains(touchPoint.x, touchPoint.y)) state = State.RUNNING;
            else if (quitBounds.contains(touchPoint.x, touchPoint.y)) game.setScreen(new LoadingScreen(game));
        }
    }

    private void draw() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        if (sceneState == 3) {
            batch.draw(backgroundTexture, 0, 0, 1280, 720);
            player.draw(batch); // Let the player draw itself!
        }
        
        if (sceneState < 3) {
            batch.draw(currentNarratorBox, 0, 0, 1280, 720);
        }

        if (state == State.PAUSED) {
            batch.draw(pauseBg, 0, 0, 1280, 720);
            batch.draw(resumeTex, resumeBounds.x, resumeBounds.y, resumeBounds.width, resumeBounds.height);
            batch.draw(quitTex, quitBounds.x, quitBounds.y, quitBounds.width, quitBounds.height);
        }
        batch.end();
    }

    @Override public void dispose() {
        batch.dispose();
        player.dispose(); // Important!
        backgroundTexture.dispose();
        dialogue1.dispose();
        dialogue2.dispose();
        pauseBg.dispose();
        resumeTex.dispose();
        quitTex.dispose();
        typeSound.dispose();
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}