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
    // Replace dialogue1, dialogue2, etc. with this:
    private Texture[] dialogueScreens;
    private int currentDialogueIndex = 0;
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
        // List them in order here. You can add 100 here and the logic won't change!
        dialogueScreens = new Texture[] {
        new Texture("Narrator_box1.png"),
        new Texture("scrapped letter 2.png"),
        new Texture("Narrator_box2.png"),
        new Texture("Narrator_box3.png") // Example of adding more easily
        };
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
    // 1. ESCAPE COMMAND (Stays the same)
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
        if (state == State.RUNNING) {
            state = State.PAUSED;
            typeSound.pause();
        } else {
            state = State.RUNNING;
            typeSound.resume();
        }
    }

    // 2. GAME LOGIC (Only when running)
    if (state == State.RUNNING) {
        updateGame(delta);
    }

    // 3. INPUT PROCESSOR (Stays the same)
    if (!inputSet) {
        Gdx.input.setInputProcessor(stage);
        inputSet = true;
    }

    // 4. DRAWING
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    batch.setProjectionMatrix(stage.getCamera().combined);
    batch.begin();

    // --- SMART DIALOGUE / WORLD DRAWING ---
    
    // If we have finished all dialogue screens, draw the Town
    if (currentDialogueIndex >= dialogueScreens.length) { 
        batch.draw(backgroundTexture, 0, 0, 1280, 720);
        batch.draw(currentPlayerTexture, playerX, playerY, 550, 400);
    } 
    // Otherwise, draw whichever dialogue box we are currently on
    else { 
        batch.draw(dialogueScreens[currentDialogueIndex], 0, 0, 1280, 720);
    }

    // 5. PAUSE OVERLAY
    if (state == State.PAUSED) {
        batch.draw(pauseBg, 0, 0, 1280, 720);
    }
    
    batch.end();

    // 6. STAGE BUTTONS
    if (state == State.PAUSED) {
        stage.act(delta);
        stage.draw();
    }
}

   private void updateGame(float delta) {
    // 1. Dialogue Logic
    if (currentDialogueIndex < dialogueScreens.length) {
        if (Gdx.input.justTouched()) {
            currentDialogueIndex++; // Move to the next screen
            
            // If we just passed the last dialogue, stop the sound
            if (currentDialogueIndex >= dialogueScreens.length) {
                typeSound.stop();
                soundPlaying = false;
            }
        }
    }

    // 2. Movement Logic (This only runs AFTER dialogue is finished)
    if (currentDialogueIndex >= dialogueScreens.length) { 
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
    
        // Reset to idle if not moving
        if (!moving) { 
            currentPlayerTexture = playerIdle; 
            animationTimer = 0; 
        }

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
