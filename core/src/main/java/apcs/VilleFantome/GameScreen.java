package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameScreen implements Screen {
    private Main game;
    private SpriteBatch batch;
    private Stage stage;
    private boolean inputSet = false;

    // pause menu variables
    private enum State { RUNNING, PAUSED }
    private State state = State.RUNNING;
    private Texture pauseBg;
    private Texture resumeTex;
    private Texture quitTex;
    private ImageButton resumeButton;
    private ImageButton quitButton;

    // dialogue variables
    private Texture dialogue1;
    private Texture dialogue2;
    private Texture currentNarratorBox;
    private Sound typeSound;
    private int sceneState = 1;
    private float soundTimer = 0.0F;
    private boolean soundPlaying = true;

    // world and player variables
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

        stage = new Stage(new FitViewport(1280, 720));

        // dialogue assets
        dialogue1 = new Texture("Narrator_box1.png");
        dialogue2 = new Texture("scrapped letter 2.png");
        currentNarratorBox = dialogue1;
        typeSound = Gdx.audio.newSound(Gdx.files.internal("NarratorTypeSound.mp3"));
        typeSound.play(1.0F);

        // world assets
        backgroundTexture = new Texture("background1.png");
        playerIdle = new Texture("standing_still.png");

        // pause menu assets
        pauseBg = new Texture("pause_screen.png");

        resumeTex = new Texture("resume_button.png");
        resumeButton = new ImageButton(
            new TextureRegionDrawable(new TextureRegion(resumeTex))
        );
        resumeButton.setPosition(540, 400);
        resumeButton.setSize(200, 80);
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (state == State.PAUSED) {
                    state = State.RUNNING;
                    typeSound.resume();
                }
            }
        });

        quitTex = new Texture("quitbutton.png");
        quitButton = new ImageButton(
            new TextureRegionDrawable(new TextureRegion(quitTex))
        );
        quitButton.setPosition(540, 280);
        quitButton.setSize(200, 80);
        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (state == State.PAUSED) {
                    game.setScreen(new LoadingScreen(game));
                }
            }
        });

        stage.addActor(resumeButton);
        stage.addActor(quitButton);

        // animation frames for player moving
        leftFrames = new Texture[]{new Texture("left(1).png"), new Texture("left(2).png"), new Texture("left(3).png")};
        rightFrames = new Texture[]{new Texture("right(1).png"), new Texture("right(2).png"), new Texture("right(3).png")};
        currentPlayerTexture = playerIdle;

        inputSet = false;
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

        // 2. GAME LOGIC (only when running)
        if (state == State.RUNNING) {
            updateGame(delta);
        }

        // 3. INPUT PROCESSOR
        if (!inputSet) {
            Gdx.input.setInputProcessor(stage);
            inputSet = true;
        }

        // 4. DRAWING
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();
        if (sceneState == 3) {
            batch.draw(backgroundTexture, 0, 0, 1280, 720);
            batch.draw(currentPlayerTexture, playerX, playerY, 550, 400);
        }
        if (sceneState < 3) {
            batch.draw(currentNarratorBox, 0, 0, 1280, 720);
        }

        // Draw pause overlay last
        if (state == State.PAUSED) {
            batch.draw(pauseBg, 0, 0, 1280, 720);
        }
        batch.end();

        // Stage draws pause buttons on top
        if (state == State.PAUSED) {
            stage.act(delta);
            stage.draw();
        }
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
            playerX = Math.max(0, Math.min(playerX, 1280 - 550));
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        inputSet = false;
    }

    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
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
